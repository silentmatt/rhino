/*
 * Tests for the Object.keys(obj) method
 */
package org.mozilla.javascript.tests.es5;
import junit.framework.TestCase;
import org.mozilla.javascript.*;
import java.util.Map;

public class ObjectGetOwnPropertyDescriptorTest extends TestCase {

  public void testShouldReturnObjectWithPropertiesValueEnumerableWritableConfigurable() {
    NativeObject object = new NativeObject();
    object.defineProperty("a", "1", ScriptableObject.EMPTY);

    Object result = eval("Object.getOwnPropertyDescriptor(obj, 'a')", "obj", object);

    Map descriptor = (Map) result;
    assertEquals(4, descriptor.size());
    assertEquals("1", descriptor.get("value"));
    assertEquals("true", descriptor.get("enumerable"));
    assertEquals("true", descriptor.get("writable"));
    assertEquals("true", descriptor.get("configurable"));
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
