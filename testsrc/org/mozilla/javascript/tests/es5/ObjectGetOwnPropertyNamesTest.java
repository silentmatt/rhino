/*
 * Tests for the Object.getOwnPropertyNames(obj) method
 */
package org.mozilla.javascript.tests.es5;
import junit.framework.TestCase;
import org.mozilla.javascript.*;

public class ObjectGetOwnPropertyNamesTest extends TestCase {

  public void testShouldReturnAllPropertiesOfArg() {
    NativeObject object = new NativeObject();
    object.defineProperty("a", new NativeArray(0), ScriptableObject.EMPTY);
    object.defineProperty("b", new NativeArray(0), ScriptableObject.DONTENUM);

    Object result = eval("Object.getOwnPropertyNames(obj)", "obj", object);

    NativeArray names = (NativeArray) result;

    assertEquals(2, names.getLength());
    assertEquals("a", names.get(0, names));
    assertEquals("b", names.get(1, names));
  }

  private Object eval(String source, String id, Scriptable object) {
    Context cx = ContextFactory.getGlobal().enterContext();
    try {
      Scriptable scope = cx.initStandardObjects();
      scope.put(id, scope, object);
      return cx.evaluateString(scope, source, "source", 1, null);
    } finally {
      Context.exit();
    }
  }
}
