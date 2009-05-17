package org.mozilla.javascript.tests.es5;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tests.*;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class PropertyDescriptorTest {
  private final PropertyDescriptor blank = new PropertyDescriptor();
  private final Function getter = new StubFunction();
  private final Function setter = new StubFunction();

  @Test
  public void shouldInitializeDataDescriptorThroughBuilderMethods() {
    PropertyDescriptor desc = blank.value("a").enumerable(true).writable(true).configurable(true);
    assertEquals("a", desc.getValue());
    assertEquals(true, desc.getEnumerable());
    assertEquals(true, desc.getWritable());
    assertEquals(true, desc.getConfigurable());
  }

  @Test
  public void shouldInitialiseAccessorDescriptorThroughBuilderMethods() {
    PropertyDescriptor desc = blank.getter(getter).setter(setter).enumerable(true).configurable(true);
    assertEquals(getter, desc.getGetter());
    assertEquals(setter, desc.getSetter());
    assertEquals(true, desc.getEnumerable());
    assertEquals(true, desc.getConfigurable());
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
  public void defaultAttributesShouldBeUndefinedAndFalse() {
    assertEquals(Undefined.instance, blank.getValueOrDefault());
    assertEquals(Undefined.instance, blank.getGetterOrDefault());
    assertEquals(Undefined.instance, blank.getSetterOrDefault());
    assertEquals(false, blank.getEnumerableOrDefault());
    assertEquals(false, blank.getConfigurableOrDefault());
    assertEquals(false, blank.getWritableOrDefault());
  }

  @Test
  public void propertyDescriptorsWithTheSameSettingsShouldBeEqual() {
    PropertyDescriptor a = new PropertyDescriptor(), b = new PropertyDescriptor();
    assertEquals(a, a);
    assertEquals(a, b);
    assertEquals(a.value("hi"), b.value("hi"));
    assertThat(a.value("hi"), is(not(a.value("lo"))));
  }

  @Test
  public void fromPropertyDescriptorShouldSetDefaultValuesForAnAccessorPropertyDescriptor() {
    NativeObject obj = blank.getter(getter).fromPropertyDescriptor();
    assertEquals(Undefined.instance, obj.get("set", obj));
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

    PropertyDescriptor desc = blank.value(1).writable(true).enumerable(true).configurable(true);
    NativeObject actual = desc.fromPropertyDescriptor();

    assertEquals(expected.entrySet(), actual.entrySet());
  }

  @Test
  public void fromPropertyDescriptorShouldSetDefaultValuesForAGenericPropertyDescriptor() {
    NativeObject obj = blank.fromPropertyDescriptor();
    assertEquals(false, obj.get("enumerable"));
    assertEquals(false, obj.get("configurable"));
  }

  @Test
  public void fromPropertyDescriptorShouldSetDefaultValuesForADataPropertyDescriptor() {
    assertEquals(false, blank.value(1).fromPropertyDescriptor().get("writable"));

    NativeObject obj = blank.writable(true).fromPropertyDescriptor();
    assertEquals(Undefined.instance, obj.get("value", obj));
  }

  @Test
  public void toPropertyDescriptorShouldCreateEmptyDescriptorFromEmptyObjectArgument() {
    PropertyDescriptor expected = new PropertyDescriptor();
    PropertyDescriptor actual = PropertyDescriptor.toPropertyDescriptor(new NativeObject());
    assertEquals(expected, actual);
  }

  @Test
  public void toPropertyDescriptorShouldCreateDescriptorWithAttributesProvidedByArgument() {
    Scriptable attribs = new NativeObject();
    attribs.put("enumerable", attribs, true);
    attribs.put("configurable", attribs, true);
    attribs.put("value", attribs, 1);
    attribs.put("writable", attribs, true);

    PropertyDescriptor expected = new PropertyDescriptor().
      enumerable(true).configurable(true).value(1).writable(true);
    PropertyDescriptor actual = PropertyDescriptor.toPropertyDescriptor(attribs);
    assertEquals(expected, actual);
  }
}
