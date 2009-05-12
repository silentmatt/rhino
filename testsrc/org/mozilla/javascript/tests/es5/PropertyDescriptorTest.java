package org.mozilla.javascript.tests.es5;

import junit.framework.TestCase;
import org.mozilla.javascript.*;
import java.util.Arrays;

public class PropertyDescriptorTest extends TestCase {
  private final PropertyDescriptor blank = new PropertyDescriptor();
  private final Callable getter = new StubCallable();
  private final Callable setter = new StubCallable();

  public void testShouldInitializeDataDescriptorThroughBuilderMethods() {
    PropertyDescriptor desc = blank.value("a").enumerable(true).writable(true).configurable(true);
    assertEquals("a", desc.getValue());
    assertEquals(true, desc.isEnumerable());
    assertEquals(true, desc.isWritable());
    assertEquals(true, desc.isConfigurable());
  }

  public void testShouldInitialiseAccessorDescriptorThroughBuilderMethods() {
    PropertyDescriptor desc = blank.getter(getter).setter(setter).enumerable(true).configurable(true);
    assertEquals(getter, desc.getGetter());
    assertEquals(setter, desc.getSetter());
    assertEquals(true, desc.isEnumerable());
    assertEquals(true, desc.isConfigurable());
  }

  public void testShouldNotAllowGetterOrSetterToBeSetOnceValueOrWritableHaveBeenSet() {
    for (PropertyDescriptor desc : Arrays.asList(blank.value("a"), blank.writable(true))) {
      try {
        desc.getter(getter);
        fail("Descriptor cannot contain both value/writable and a getter");
      } catch(UnsupportedOperationException ex) {
        // this was expected
      }
      try {
        desc.setter(setter);
        fail("Descriptor cannot contain both value/writable and a setter");
      } catch(UnsupportedOperationException ex) {
        // this was expected
      }
    }
  }

  public void testShouldNotAllowValueOrWritableToBeSetOnceGetterOrSetterHaveBeenSet() {
    for (PropertyDescriptor desc : Arrays.asList(blank.getter(getter), blank.setter(setter))) {
      try {
        desc.value("value");
        fail("Descriptor cannot contain both getter/setter and a value");
      } catch(UnsupportedOperationException ex) {
        // this was expected
      }
      try {
        desc.writable(true);
        fail("Descriptor cannot contain both getter/setter and a writable");
      } catch(UnsupportedOperationException ex) {
        // this was expected
      }
    }
  }

  public void testShouldBeDataDescriptorOnlyWhenValueOrWritableIsSet() {
    assertFalse(blank.isDataDescriptor());
    assertFalse(blank.enumerable(true).isDataDescriptor());
    assertFalse(blank.configurable(true).isDataDescriptor());
    assertFalse(blank.getter(getter).isDataDescriptor());
    assertFalse(blank.setter(setter).isDataDescriptor());
    assertTrue(blank.value("a").isDataDescriptor());
    assertTrue(blank.writable(true).isDataDescriptor());
  }

  public void testShouldBeAccessorDescriptorOnlyWhenGetterOrSetterIsSet() {
    assertFalse(blank.isAccessorDescriptor());
    assertFalse(blank.enumerable(true).isAccessorDescriptor());
    assertFalse(blank.configurable(true).isAccessorDescriptor());
    assertFalse(blank.value("a").isAccessorDescriptor());
    assertFalse(blank.writable(true).isAccessorDescriptor());
    assertTrue(blank.getter(getter).isAccessorDescriptor());
    assertTrue(blank.setter(setter).isAccessorDescriptor());
  }

  public void testShouldBeGenericDescriptorOnlyWhenNotDataOrAccessorDescriptor() {
    assertTrue(blank.isGenericDescriptor());
    assertTrue(blank.enumerable(true).isGenericDescriptor());
    assertTrue(blank.configurable(true).isGenericDescriptor());
    assertFalse(blank.value("a").isGenericDescriptor());
    assertFalse(blank.writable(true).isGenericDescriptor());
    assertFalse(blank.getter(getter).isGenericDescriptor());
    assertFalse(blank.setter(setter).isGenericDescriptor());
  }

  public void testFromPropertyDescriptorShouldCreateValidObjectForDataDescriptor() {
    NativeObject expected = new NativeObject();
    expected.defineProperty("value", Integer.valueOf(1), ScriptableObject.EMPTY);
    expected.defineProperty("writable", Boolean.TRUE, ScriptableObject.EMPTY);
    expected.defineProperty("enumerable", Boolean.TRUE, ScriptableObject.EMPTY);
    expected.defineProperty("configurable", Boolean.TRUE, ScriptableObject.EMPTY);

    PropertyDescriptor desc = new PropertyDescriptor().value(1).writable(true).enumerable(true).configurable(true);
    NativeObject actual = desc.fromPropertyDescriptor();

    assertEquals(expected.entrySet(), actual.entrySet());
  }

  private static class StubCallable implements Callable {
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
      return null;
    }
  }
}
