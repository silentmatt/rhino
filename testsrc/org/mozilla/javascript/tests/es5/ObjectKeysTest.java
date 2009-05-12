/*
 * Tests for the Object.keys(obj) method
 */
package org.mozilla.javascript.tests.es5;
import org.mozilla.javascript.*;
import static org.mozilla.javascript.tests.Evaluator.eval;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ObjectKeysTest {

  @Test
  public void shouldReturnOnlyEnumerablePropertiesOfArg() {
    NativeObject object = new NativeObject();
    object.defineProperty("a", new NativeArray(0), ScriptableObject.EMPTY);
    object.defineProperty("b", new NativeArray(0), ScriptableObject.EMPTY);
    object.defineProperty("c", new NativeArray(0), ScriptableObject.DONTENUM);

    Object result = eval("Object.keys(obj)", "obj", object);

    NativeArray keys = (NativeArray) result;

    assertEquals(2, keys.getLength());
    assertEquals("a", keys.get(0, keys));
    assertEquals("b", keys.get(1, keys));
  }

}
