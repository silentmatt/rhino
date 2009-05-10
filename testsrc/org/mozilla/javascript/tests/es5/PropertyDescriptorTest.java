package org.mozilla.javascript.tests.es5;
import junit.framework.TestCase;
import org.mozilla.javascript.*;

public class PropertyDescriptorTest extends TestCase {
  private PropertyDescriptor desc;

  public void testShouldInitializeDataDescriptorThroughBuilderMethods() {
    desc = new PropertyDescriptor().value("a").enumerable(true).writable(true).configurable(true);
    assertEquals("a", desc.getValue());
    assertEquals(true, desc.isEnumerable());
    assertEquals(true, desc.isWritable());
    assertEquals(true, desc.isConfigurable());
  }

  public void testShouldInitialiseAccessorDescriptorThroughBuilderMethods() {
    Callable getterFunc = new StubCallable();
    Callable setterFunc = new StubCallable();
    desc = new PropertyDescriptor().getter(getterFunc).setter(setterFunc).enumerable(true).configurable(true);
    assertEquals(getterFunc, desc.getGetter());
    assertEquals(setterFunc, desc.getSetter());
    assertEquals(true, desc.isEnumerable());
    assertEquals(true, desc.isConfigurable());
  }


  private static class StubCallable implements Callable {
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      return null;
    }
  }


}
