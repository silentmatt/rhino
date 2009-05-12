/*
 * Tests for the Object.getOwnPropertyDescriptor(obj, prop) method
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

    NativeObject descriptor = (NativeObject) result;
    assertEquals(4, descriptor.size());
    assertTrue(descriptor.containsKey("value"));
    assertTrue(descriptor.containsKey("enumerable"));
    assertTrue(descriptor.containsKey("writable"));
    assertTrue(descriptor.containsKey("configurable"));
  }

  public void testContentsOfPropertyDescriptorShouldReflectAttributesOfProperty() {
    NativeObject descriptor;
    NativeObject object = new NativeObject();
    object.defineProperty("a", "1", ScriptableObject.EMPTY);
    object.defineProperty("b", "2", ScriptableObject.DONTENUM | ScriptableObject.READONLY | ScriptableObject.PERMANENT);

    descriptor = (NativeObject) eval("Object.getOwnPropertyDescriptor(obj, 'a')", "obj", object);
    assertEquals("1",  descriptor.get("value"));
    assertEquals(true, descriptor.get("enumerable"));
    assertEquals(true, descriptor.get("writable"));
    assertEquals(true, descriptor.get("configurable"));

    descriptor = (NativeObject) eval("Object.getOwnPropertyDescriptor(obj, 'b')", "obj", object);
    assertEquals("2",  descriptor.get("value"));
    assertEquals(false, descriptor.get("enumerable"));
    assertEquals(false, descriptor.get("writable"));
    assertEquals(false, descriptor.get("configurable"));
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
