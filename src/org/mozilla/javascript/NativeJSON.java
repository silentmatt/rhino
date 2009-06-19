/* -*- Mode: java; tab-width: 4; indent-tabs-mode: 1; c-basic-offset: 4 -*-
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Norris Boyd
 *   Matthew Crumley
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.javascript;

import java.util.Stack;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class implements the JSON native object.
 * See ECMA 15.12.
 * @author Matthew Crumley
 */

final class NativeJSON extends IdScriptableObject
{
    static final long serialVersionUID = -4567599697595654984L;

    private static final Object JSON_TAG = "JSON";

    static void init(Scriptable scope, boolean sealed)
    {
        NativeJSON obj = new NativeJSON();
        obj.activatePrototypeMap(MAX_ID);
        obj.setPrototype(getObjectPrototype(scope));
        obj.setParentScope(scope);
        if (sealed) { obj.sealObject(); }
        ScriptableObject.defineProperty(scope, "JSON", obj,
                                        ScriptableObject.DONTENUM);
    }

    private NativeJSON()
    {
    }

    @Override
    public String getClassName() { return "JSON"; }

    @Override
    protected void initPrototypeId(int id)
    {
        if (id <= LAST_METHOD_ID) {
            String name;
            int arity;
            switch (id) {
              case Id_toSource:  arity = 0; name = "toSource";  break;
              case Id_parse:     arity = 2; name = "parse";     break;
              case Id_stringify: arity = 3; name = "stringify"; break;
              default: throw new IllegalStateException(String.valueOf(id));
            }
            initPrototypeMethod(JSON_TAG, id, name, arity);
        } else {
            throw new IllegalStateException(String.valueOf(id));
        }
    }

    @Override
    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope,
                             Scriptable thisObj, Object[] args)
    {
        if (!f.hasTag(JSON_TAG)) {
            return super.execIdCall(f, cx, scope, thisObj, args);
        }
        int methodId = f.methodId();
        switch (methodId) {
            case Id_toSource:
                return "JSON";

            case Id_parse: {
                String jtext = ScriptRuntime.toString(args, 0);
                Object reviver = null;
                if (args.length > 1) {
                    reviver = args[1];
                }
                try {
                    Object unfiltered = (new JsonParser(cx, scope)).parseValue(jtext);
                    if (reviver instanceof Callable) {
                        Scriptable root = cx.newObject(scope);
                        root.put("", root, unfiltered);
                        return walk(cx, scope, (Callable) reviver, root, "");
                    }
                    else {
                        return unfiltered;
                    }
                }
                catch (JsonParser.ParseException ex) {
                    throw ScriptRuntime.constructError("SyntaxError", ex.getMessage());
                }
            }

            case Id_stringify: {
                Object value = null, replacer = null, space = null;
                switch (args.length) {
                    default:
                    case 3: space = args[2];
                    case 2: replacer = args[1];
                    case 1: value = args[0];
                    case 0:
                }
                return stringify(cx, scope, value, replacer, space);
            }

            default: throw new IllegalStateException(String.valueOf(methodId));
        }
    }

    private static Object walk(Context cx, Scriptable scope, Callable reviver, Scriptable holder, Object name) {
        Object val = null;
        if (name instanceof Number) {
            val = holder.get(((Number) name).intValue(), holder);
        }
        else {
            val = holder.get(name.toString(), holder);
        }

        if (val instanceof Scriptable && !(val instanceof Callable)) {
            Scriptable scriptableVal = ((Scriptable) val);
            if (scriptableVal.getClassName().equals("Array")) {
                int len = (int) ScriptRuntime.toNumber(scriptableVal.get("length", scriptableVal));
                for (int i = 0; i < len; i++) {
                    Object newElement = walk(cx, scope, reviver, scriptableVal, Integer.valueOf(i));
                    scriptableVal.put(i, scriptableVal, newElement);
                }
            }
            else {
                Object[] keys = scriptableVal.getIds(); // XXX: 2.b.i -- is this correct?
                for (int i = 0; i < keys.length; i++) {
                    String p = keys[i].toString();
                    Object newElement = walk(cx, scope, reviver, scriptableVal, p);
                    if (newElement == Undefined.instance) {
                        scriptableVal.delete(p);
                    }
                    else {
                        scriptableVal.put(p, scriptableVal, newElement);
                    }
                }
            }
        }

        return reviver.call(cx, scope, holder, new Object[] { name, val });
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static class StringifyState {
        StringifyState(Context cx, Scriptable scope, String indent, String gap, Scriptable replacer, Object space) {
            this.cx = cx;
            this.scope = scope;

            this.indent = indent;
            this.gap = gap;
            this.replacer = replacer;
            this.space = space;
        }

        Stack<Scriptable> stack = new Stack<Scriptable>();
        String indent;
        String gap;
        Scriptable replacer;
        Object space;

        Context cx;
        Scriptable scope;
    }

    private static String stringify(Context cx, Scriptable scope, Object value, Object replacer, Object space) {
        String indent = "";
        String gap = "";

        if (space instanceof Scriptable) {
            if (((Scriptable) space).getClassName().equals("Number")) {
                space = ScriptRuntime.toNumber(space);
            }
            else if (((Scriptable) space).getClassName().equals("String")) {
                space = ScriptRuntime.toString(space);
            }
        }

        if (space instanceof Number) {
            space = Math.min(((Number) space).doubleValue(), 100.0);
            gap = repeat(' ', ((Number) space).intValue());
        }
        else if (space instanceof String) {
            gap = (String) space;
        }

        StringifyState state = new StringifyState(cx, scope,
            indent,
            gap,
            (replacer instanceof Scriptable) ? (Scriptable) replacer : null,
            space);

        ScriptableObject wrapper = new NativeObject();
        wrapper.defineProperty("", value, 0);
        return str("", wrapper, state);
    }

    private static String str(Object key, Scriptable holder, StringifyState state) {
        Object value = null;
        if (key instanceof String) {
            value = ScriptableObject.getProperty(holder, (String) key);
        }
        else {
            value = ScriptableObject.getProperty(holder, ((Number) key).intValue());
        }

        if (value instanceof Scriptable) {
            Object toJSON = ScriptableObject.getProperty((Scriptable) value, "toJSON");
            if (toJSON instanceof Callable) {
                value = ScriptableObject.callMethod(state.cx, (Scriptable) value, "toJSON", new Object[] { key });
            }
        }

        if (state.replacer instanceof Callable) {
            value = ((Callable) state.replacer).call(state.cx, state.scope, holder, new Object[] { key, value });
        }

        if (value == null) {
            return "null";
        }
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? "true" : "false";
        }

        if (value instanceof Scriptable) {
            if (((Scriptable) value).getClassName().equals("Number")) {
                value = ScriptRuntime.toNumber(value);
            }
            else if (((Scriptable) value).getClassName().equals("String")) {
                value = ScriptRuntime.toString(value);
            }
        }

        if (value instanceof String) {
            return quote((String) value);
        }

        if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (d == d && d != Double.POSITIVE_INFINITY && d != Double.NEGATIVE_INFINITY) {
                return ScriptRuntime.toString(value);
            }
            else {
                return "null";
            }
        }

        if (value instanceof Scriptable && !(value instanceof Callable)) {
            if (((Scriptable) value).getClassName().equals("Array")) {
                return ja((Scriptable) value, state);
            }
            return jo((Scriptable) value, state);
        }

        return null;
    }

    private static String join(Collection objs, String delimiter) {
        if (objs == null || objs.isEmpty()) {
            return "";
        }
        Iterator iter = objs.iterator();
        StringBuffer buffer = new StringBuffer(iter.next().toString());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next().toString());
        }
        return buffer.toString();
    }

    private static String jo(Scriptable value, StringifyState state) {
        if (state.stack.search(value) != -1) {
            throw ScriptRuntime.typeError0("msg.structure.is.cyclical");
        }
        state.stack.push(value);

        String stepback = state.indent;
        state.indent = state.indent + state.gap;
        Object[] k = null;
        if (state.replacer != null && state.replacer.getClassName().equals("Array")) {
            // XXX: 5.a Is this correct?
            Object[] ids = state.replacer.getIds();
            Vector<Object> v = new Vector<Object>();
            for (int i = 0; i < ids.length; i++) {
                // XXX: json2.js only uses string members, but I couldn't find anything in the spec about that
                if (ids[i] instanceof Number) {
                    v.add(state.replacer.get(((Number) ids[i]).intValue(), state.replacer));
                }
            }
            k = v.toArray();
        }
        else {
            // XXX: 6.a -- Is this correct?
            k = value.getIds();
        }

        Vector<String> partial = new Vector<String>();

        for (int i = 0; i < k.length; i++) {
            Object p = k[i];
            String strP = str(p, value, state);
            if (strP != null) {
                String member = quote(p.toString()) + ":";
                if (state.gap.length() > 0) {
                    member = member + " ";
                }
                member = member + strP;
                partial.add(member);
            }
        }

        String finalValue;

        if (partial.isEmpty()) {
            finalValue = "{}";
        }
        else {
            if (state.gap.length() == 0) {
                finalValue = '{' + join(partial, ",") + '}';
            }
            else {
                String separator = ",\n" + state.indent;
                String properties = join(partial, separator);
                finalValue = "{\n" + state.indent + properties + '\n' + stepback + '}';
            }
        }

        state.stack.pop();
        state.indent = stepback;
        return finalValue;
    }

    private static String ja(Scriptable value, StringifyState state) {
        if (state.stack.search(value) != -1) {
            throw ScriptRuntime.typeError0("msg.structure.is.cyclical");
        }
        state.stack.push(value);

        String stepback = state.indent;
        state.indent = state.indent + state.gap;
        Vector<String> partial = new Vector<String>();

        int len = (int) ScriptRuntime.toNumber(value.get("length", value));
        for (int index = 0; index < len; index++) {
            String strP = str(Integer.valueOf(index), value, state);
            if (strP == null) {
                partial.add("null");
            }
            else {
                partial.add(strP);
            }
        }

        String finalValue;

        if (partial.isEmpty()) {
            finalValue = "[]";
        }
        else {
            if (state.gap.length() == 0) {
                finalValue = '[' + join(partial, ",") + ']';
            }
            else {
                String separator = ",\n" + state.indent;
                String properties = join(partial, separator);
                finalValue = "[\n" + state.indent + properties + '\n' + stepback + ']';
            }
        }

        state.stack.pop();
        state.indent = stepback;
        return finalValue;
    }

    private static String quote(String string) {
        StringBuilder product = new StringBuilder(string.length());
        product.append('"');
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            switch (c) {
                case '"':
                    product.append("\\\"");
                    break;
                case '\\':
                    product.append("\\\\");
                    break;
                case '\b':
                    product.append("\\b");
                    break;
                case '\f':
                    product.append("\\f");
                    break;
                case '\n':
                    product.append("\\n");
                    break;
                case '\r':
                    product.append("\\r");
                    break;
                case '\t':
                    product.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        product.append("\\u");
                        String hex = Integer.toString(c, 16);
                        switch (hex.length()) {
                            case 1:
                                product.append("000");
                                break;
                            case 2:
                                product.append("00");
                                break;
                            case 3:
                                product.append("0");
                                break;
                        }
                        product.append(hex);
                    }
                    else {
                        product.append(c);
                    }
                    break;
            }
        }
        product.append('"');
        return product.toString();
    }

// #string_id_map#

    @Override
    protected int findPrototypeId(String s)
    {
        int id;
// #generated# Last update: 2009-05-25 16:01:00 EDT
        L0: { id = 0; String X = null; int c;
            L: switch (s.length()) {
            case 5: X="parse";id=Id_parse; break L;
            case 8: X="toSource";id=Id_toSource; break L;
            case 9: X="stringify";id=Id_stringify; break L;
            }
            if (X!=null && X!=s && !X.equals(s)) id = 0;
        }
// #/generated#
        return id;
    }

    private static final int
        Id_toSource     = 1,
        Id_parse        = 2,
        Id_stringify    = 3,
        LAST_METHOD_ID  = 3,
        MAX_ID          = 3;

// #/string_id_map#
}
