package org.mozilla.javascript.tests.es5;

import org.mozilla.javascript.*;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import org.junit.Test;

public class PropertyDescriptorTest {
  private final PropertyDescriptor blank = new PropertyDescriptor();
  private final Callable getter = new StubCallable();
  private final Callable setter = new StubCallable();

  @Test
  public void shouldInitializeDataDescriptorThroughBuilderMethods() {
    PropertyDescriptor desc = blank.value("a").enumerable(true).writable(true).configurable(true);
    assertEquals("a", desc.getValue());
    assertEquals(true, desc.isEnumerable());
    assertEquals(true, desc.isWritable());
    assertEquals(true, desc.isConfigurable());
  }

  @Test
  public void shouldInitialiseAccessorDescriptorThroughBuilderMethods() {
    PropertyDescriptor desc = blank.getter(getter).setter(setter).enumerable(true).configurable(true);
    assertEquals(getter, desc.getGetter());
    assertEquals(setter, desc.getSetter());
    assertEquals(true, desc.isEnumerable());
    assertEquals(true, desc.isConfigurable());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowGetterBeSetOnceValueHasBeenSet() {
    blank.value("a").getter(getter);
  }
  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowGetterToBeSetOnceWritableHasBeenSet() {
    blank.writable(true).getter(getter);
  }
  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowSetterBeSetOnceValueHasBeenSet() {
    blank.value("a").setter(setter);
  }
  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowSetterToBeSetOnceWritableHasBeenSet() {
    blank.writable(true).setter(setter);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowValueToBeSetOnceGetterHasBeenSet() {
    blank.getter(getter).value("a");
  }
  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowWritableToBeSetOnceGetterHasBeenSet() {
    blank.getter(getter).writable(true);
  }
  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowValueToBeSetOnceSetterHasBeenSet() {
    blank.setter(setter).value("a");
  }
  @Test(expected = UnsupportedOperationException.class)
  public void shouldNotAllowWritableToBeSetOnceSetterHasBeenSet() {
    blank.setter(setter).writable(true);
  }

  @Test
  public void shouldBeDataDescriptorOnlyWhenValueOrWritableIsSet() {
    assertFalse(blank.isDataDescriptor());
    assertFalse(blank.enumerable(true).isDataDescriptor());
    assertFalse(blank.configurable(true).isDataDescriptor());
    assertFalse(blank.getter(getter).isDataDescriptor());
    assertFalse(blank.setter(setter).isDataDescriptor());
    assertTrue(blank.value("a").isDataDescriptor());
    assertTrue(blank.writable(true).isDataDescriptor());
  }

  @Test
  public void shouldBeAccessorDescriptorOnlyWhenGetterOrSetterIsSet() {
    assertFalse(blank.isAccessorDescriptor());
    assertFalse(blank.enumerable(true).isAccessorDescriptor());
    assertFalse(blank.configurable(true).isAccessorDescriptor());
    assertFalse(blank.value("a").isAccessorDescriptor());
    assertFalse(blank.writable(true).isAccessorDescriptor());
    assertTrue(blank.getter(getter).isAccessorDescriptor());
    assertTrue(blank.setter(setter).isAccessorDescriptor());
  }

  @Test
  public void shouldBeGenericDescriptorOnlyWhenNotDataOrAccessorDescriptor() {
    assertTrue(blank.isGenericDescriptor());
    assertTrue(blank.enumerable(true).isGenericDescriptor());
    assertTrue(blank.configurable(true).isGenericDescriptor());
    assertFalse(blank.value("a").isGenericDescriptor());
    assertFalse(blank.writable(true).isGenericDescriptor());
    assertFalse(blank.getter(getter).isGenericDescriptor());
    assertFalse(blank.setter(setter).isGenericDescriptor());
  }

  @Test
  public void fromPropertyDescriptorShouldCreateValidObjectForDataDescriptor() {
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
