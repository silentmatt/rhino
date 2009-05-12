/*
 * Tests for the Object.getOwnPropertyNames(obj) method
 */
package org.mozilla.javascript.tests.es5;
import junit.framework.TestCase;
import org.mozilla.javascript.*;
import static org.mozilla.javascript.tests.Evaluator.eval;

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

}
