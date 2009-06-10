package org.mozilla.javascript;

import org.mozilla.javascript.JsonLexer.Token;

import static org.mozilla.javascript.JsonLexer.BOOLEAN;
import static org.mozilla.javascript.JsonLexer.CLOSE_BRACE;
import static org.mozilla.javascript.JsonLexer.CLOSE_BRACKET;
import static org.mozilla.javascript.JsonLexer.COLON;
import static org.mozilla.javascript.JsonLexer.COMMA;
import static org.mozilla.javascript.JsonLexer.NULL;
import static org.mozilla.javascript.JsonLexer.NUMBER;
import static org.mozilla.javascript.JsonLexer.OPEN_BRACE;
import static org.mozilla.javascript.JsonLexer.OPEN_BRACKET;
import static org.mozilla.javascript.JsonLexer.STRING;
import static org.mozilla.javascript.JsonLexer.VALUE_START_TOKENS;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
	public JsonParser(Context cx, Scriptable scope) {
		this.cx = cx;
		this.scope = scope;
	}

	private Context cx;
	private Scriptable scope;

	private Object readNull(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, NULL);
		lexer.moveNext();
		return null;
	}

	private Boolean readBoolean(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, BOOLEAN);
		Boolean bool = lexer.getBooleanValue();
		lexer.moveNext();
		return bool;
	}

	private Number readNumber(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, NUMBER);
		Number num = lexer.getNumberValue();
		lexer.moveNext();
		return num;
	}

	private String readString(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, STRING);
		String str = lexer.getStringValue();
		lexer.moveNext();
		return str;
	}

	private Scriptable newObject() {
		return cx.newObject(scope);
	}

	private Scriptable readObject(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer, OPEN_BRACE);
		expectMoveNext(lexer, STRING, CLOSE_BRACE);
		Scriptable object = newObject();

		while (isCurrentToken(lexer, STRING)) {
			String string = readString(lexer);
			expectCurrentToken(lexer,COLON);
			expectMoveNext(lexer, VALUE_START_TOKENS);
			Object value = readValue(lexer);
			object.put(string, object, value);
			if (isCurrentToken(lexer, CLOSE_BRACE))
				break;
			else {
				expectCurrentToken(lexer,COMMA);
				expectMoveNext(lexer, VALUE_START_TOKENS);
			}
		}

		expectCurrentToken(lexer, CLOSE_BRACE);
		lexer.moveNext();

		return object;
	}

	private Scriptable newArray(List<Object> items) {
		return cx.newArray(scope, items.toArray());
	}

	private Scriptable readArray(JsonLexer lexer) throws ParseException {
		expectCurrentToken(lexer,OPEN_BRACKET);
		expectMoveNext(lexer, NULL, BOOLEAN, NUMBER, STRING, OPEN_BRACKET, OPEN_BRACE, CLOSE_BRACKET);

		List<Object> array = new ArrayList<Object>();

		while (isCurrentToken(lexer, VALUE_START_TOKENS)) {
			array.add(readValue(lexer));

			if (isCurrentToken(lexer, CLOSE_BRACKET)) {
				break;
			} else {
				expectCurrentToken(lexer,COMMA);
				expectMoveNext(lexer, VALUE_START_TOKENS);
			}
		}

		expectCurrentToken(lexer,CLOSE_BRACKET);
		lexer.moveNext();

		return newArray(array);
	}

	private Object readValue(JsonLexer lexer) throws ParseException {
		if (isCurrentToken(lexer, NULL)) {
			return readNull(lexer);
		} else if (isCurrentToken(lexer, BOOLEAN)) {
			return readBoolean(lexer);
		} else if (isCurrentToken(lexer, NUMBER)) {
			return readNumber(lexer);
		} else if (isCurrentToken(lexer, STRING)) {
			return readString(lexer);
		} else if (isCurrentToken(lexer, OPEN_BRACKET)) {
			return readArray(lexer);
		} else if (isCurrentToken(lexer, OPEN_BRACE)) {
			return readObject(lexer);
		} else {
			throw new ParseException(lexer.getLexeme(), VALUE_START_TOKENS);
		}
	}

	/**
	 * Checks that the current token is <tt>expected</tt>, and if not throws a ParseException
	 */
	private void expectCurrentToken(JsonLexer lexer, Token expected) throws ParseException {
		if (!isCurrentToken(lexer, expected)) {
			throw new ParseException(lexer.getLexeme(), expected);
		}
	}

	/**
	 * Checks that the current token is one of <tt>expected</tt>, and if not throws a ParseException
	 */
	private void expectCurrentToken(JsonLexer lexer, Token... expected) throws ParseException {
		if (!isCurrentToken(lexer, expected)) {
			throw new ParseException(lexer.getLexeme(), expected);
		}
	}

	/**
	 * Attempts to move the lexer to the next token, and throws a ParseException with the expected next token types if not successful
	 */
	private void expectMoveNext(JsonLexer lexer, Token... expected) throws ParseException {
		if (!lexer.moveNext()) {
			throw new ParseException("no valid tokens from " + lexer.getOffset(), expected);
		}
		expectCurrentToken(lexer, expected);
	}

	private boolean isCurrentToken(JsonLexer lexer, Token tok) {
		return lexer.getToken() == tok;
	}

	private boolean isCurrentToken(JsonLexer lexer, Token... toks) {
		for (Token tok : toks) {
			if (lexer.getToken() == tok)
				return true;
		}
		return false;
	}

	public static class ParseException extends Exception {

		private ParseException(String message) {
			super(message);
		}

		private ParseException(String found, Token... expected) {
			super(buildMessage(found, expected));
		}

		private static String buildMessage(String found, Token... expected) {
			StringBuffer buffer = new StringBuffer("Expected: ");

			for (int i = 0; i < expected.length; i++) {
				buffer.append(expected[i].toString());
				if (i < expected.length - 1)
					buffer.append(" or ");
			}

			buffer.append(" , Found: '" + found + "'");
			
			return buffer.toString();
		}
	}

	public Object parseValue(String json) throws ParseException {
		return readValue(initLexer(json, VALUE_START_TOKENS));
	}

	private JsonLexer initLexer(String json, Token... firstToken) throws ParseException {
		JsonLexer lexer = new JsonLexer(json);
		expectMoveNext(lexer, firstToken);
		return lexer;
	}
}
