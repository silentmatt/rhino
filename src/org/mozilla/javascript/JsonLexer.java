package org.mozilla.javascript;

public class JsonLexer {
	private Token currentToken;
	private int offset;
	private int beginLexeme, endLexeme;

	private char[] input;

	public static final Token BOOLEAN = new Token("<Boolean>");
	public static final Token NUMBER = new Token("<Number>");
	public static final Token STRING = new Token("<String>");
	public static final Token NULL = new Token("'null'");
	public static final Token OPEN_BRACKET = new Token("'['");
	public static final Token CLOSE_BRACKET = new Token("']'");
	public static final Token OPEN_BRACE = new Token("'{'");
	public static final Token CLOSE_BRACE = new Token("'}'");
	public static final Token COMMA = new Token("','");
	public static final Token COLON = new Token("':'");

	public static final Token[] VALUE_START_TOKENS = new Token[] { NULL, BOOLEAN, NUMBER, STRING, OPEN_BRACKET, OPEN_BRACE };

	public JsonLexer(String input) {
		reset(input);
	}

	public void reset(String input) {
		this.input = input.toCharArray();
		this.beginLexeme = 0;
		this.endLexeme = 0;
		this.offset = 0;
		this.currentToken = null;
	}

	public boolean moveNext() {
		eatWhitespace();

		return
			eatOpenBrace() ||
			eatCloseBrace() ||
			eatOpenBracket() ||
			eatCloseBracket() ||
			eatComma() ||
			eatColon() ||
			eatNull() ||
			eatBoolean() ||
			eatNumber() ||
			eatString();
	}

	public String getLexeme() {
		return new String(input, beginLexeme, endLexeme - beginLexeme).trim();
	}

	public Token getToken() {
		return currentToken;
	}

	public long getOffset() {
		return offset;
	}

	public Number getNumberValue() {
		return Double.valueOf(getLexeme());
	}

	public Boolean getBooleanValue() {
		return Boolean.valueOf(getLexeme());
	}

	public String getStringValue() {
		return unescape();
	}

	private String unescape() {
		int start = beginLexeme;
		int end = endLexeme - 1;
		// Skip leading and trailing whitespace
		while (input[start] != '"') {
			++start;
		}
		while (input[end] != '"') {
			--end;
		}
		// Skip the leading quote
		++start;

		StringBuilder buffer = new StringBuilder(end - start);
		boolean escaping = false;

		for (int i = start; i < end; i += 1) {
			char c = input[i];

			if (escaping) {
				switch (c) {
				case '"':
					buffer.append('"');
					break;
				case '\\':
					buffer.append('\\');
					break;
				case '/':
					buffer.append('/');
					break;
				case 'b':
					buffer.append('\b');
					break;
				case 'f':
					buffer.append('\f');
					break;
				case 'n':
					buffer.append('\n');
					break;
				case 'r':
					buffer.append('\r');
					break;
				case 't':
					buffer.append('\t');
					break;
				case 'u':
					// interpret the following 4 characters as the hex of the unicode code point
					int codePoint = Integer.parseInt(new String(input, i + 1, 4), 16);
					buffer.appendCodePoint(codePoint);
					i += 4;
					break;
				default:
					throw new IllegalArgumentException("Illegal escape sequence: '\\" + c + "'");
				}
				escaping = false;
			} else {
				if (c == '\\') {
					escaping = true;
				} else {
					buffer.append(c);
				}
			}
		}

		return buffer.toString();
	}

	private boolean eatOpenBrace() { return eatToken('{', OPEN_BRACE); }
	private boolean eatCloseBrace() { return eatToken('}', CLOSE_BRACE); }
	private boolean eatOpenBracket() { return eatToken('[', OPEN_BRACKET); }
	private boolean eatCloseBracket() { return eatToken(']', CLOSE_BRACKET); }
	private boolean eatComma() { return eatToken(',', COMMA); }
	private boolean eatColon() { return eatToken(':', COLON); }

	private boolean eatNull() {
		eatWhitespace();
		beginLexeme();

		if ( !eat("null") ) return false;

		endLexeme(NULL);
		return true;
	}

	private boolean eatBoolean() {
		eatWhitespace();
		beginLexeme();

		if ( !eat("true") && !eat("false") ) return false;

		endLexeme(BOOLEAN);
		return true;
	}

	private boolean eatNumber() {
		eatWhitespace();
		beginLexeme();

		eat('-');

		if (!eatDec()) return false;
		while (eatDec()) ; // keep eating decimal digits

		if (eat('.')) {
			if (!eatDec()) return false;
			while (eatDec()) ; // keep eating decimal digits
		}

		if (eat('e', 'E')) {
			eat('-', '+');

			if (!eatDec()) return false;
			while (eatDec()) ;
		}

		endLexeme(NUMBER);
		return true;
	}

	private boolean eatString() {
		eatWhitespace();
		beginLexeme();

		if (!eat('"')) return false;

		while (!eat('"')) {
			if (finished()) return false;
			if (eat('\\')) {
				if (eatEscapable()) continue;
				if (eat('u') &&
					eatHex() &&
					eatHex() &&
					eatHex() &&
					eatHex() ) continue;

				// there's a problem
				return false;
			}
			offset += 1;
		}

		endLexeme(STRING);
		return true;
	}

	private boolean eatToken(char expected, Token token) {
		eatWhitespace();
		beginLexeme();

		if (!eat(expected)) return false;

		endLexeme(token);
		return true;
	}

	// eat the whole of expected
	private boolean eat(String expected) {
		int length = expected.length();
		if (length > (input.length-offset)) return false;

		for (int i = 0; i < length; i++) {
			if (input[offset+i] != expected.charAt(i)) {
				return false;
			}
		}

		this.offset += length;
		return true;
	}

	private boolean eat(char expected) {
		if (finished()) return false;

		if (input[offset] == expected) {
			this.offset += 1;
			return true;
		}

		return false;
	}

	private boolean eat(char expected1, char expected2) {
		if (finished()) return false;

		char c = input[offset];
		if (c == expected1 || c == expected2) {
			this.offset += 1;
			return true;
		}

		return false;
	}

	private static boolean isDecDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	private static boolean isHexDigit(char c) {
		return (c >= '0' && c <= '9') ||
		       (c >= 'a' && c <= 'f') ||
		       (c >= 'A' && c <= 'F');
	}

	private static boolean isEscapable(char c) {
		return c == '"' || c == '\\' || c == 'n' || c == 'r' ||
			c == 't' || c == '/' || c == 'b' || c == 'f';
	}

	private boolean eatDec() {
		if (finished()) return false;

		if (isDecDigit(input[offset])) {
			this.offset += 1;
			return true;
		}

		return false;
	}

	private boolean eatHex() {
		if (finished()) return false;

		if (isHexDigit(input[offset])) {
			this.offset += 1;
			return true;
		}

		return false;
	}

	private boolean eatEscapable() {
		if (finished()) return false;

		if (isEscapable(input[offset])) {
			this.offset += 1;
			return true;
		}

		return false;
	}

	private void eatWhitespace() {
		while (!finished() && Character.isWhitespace(input[offset])) offset += 1;
	}

	private void beginLexeme() {
		this.beginLexeme = this.offset;
		this.endLexeme = this.offset;
	}

	private void endLexeme(Token token) {
		this.endLexeme = offset;
		this.currentToken = token;
	}

	private boolean finished() {
		return offset >= input.length;
	}

	public static class Token {
		private Token(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}

		private String name;
	}
}