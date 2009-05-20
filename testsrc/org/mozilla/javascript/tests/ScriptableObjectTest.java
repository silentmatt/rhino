package org.mozilla.javascript.tests;

import org.mozilla.javascript.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ScriptableObjectTest {
  private StubScriptableObject obj;
  private PropertyDescriptor blank;

  @Before
  public void setUp() {
    obj = new StubScriptableObject();
    blank = new PropertyDescriptor();
    Context.enter(); // this is needed for a property's getter function to be called
  }

  @After
  public void tearDown() {
    Context.exit();
  }

  @Test
  public void defineOwnPropertyOnNewPropertyWithEmptyPropertyDescriptorShouldHaveDefaultAttributeValues() {
    obj.defineOwnProperty("p", new PropertyDescriptor());

    assertEquals(Undefined.instance, obj.get("p", obj));
    assertEquals(false, isEnumerable(obj, "p"));
    assertEquals(false, isConfigurable(obj, "p"));
    assertEquals(false, isWritable(obj, "p"));
  }

  @Test
  public void defineOwnPropertyWithAccessorPropertyDescriptorShouldAssignAttributesBasedOnTheDescriptor() {

    PropertyDescriptor desc = new PropertyDescriptor().
      enumerable(true).
      configurable(true).
      getter(new StubFunction(3)).
      setter(new StubFunction());

    obj.defineOwnProperty("p", desc);

    assertEquals(3, obj.get("p"));
    assertTrue(isEnumerable(obj, "p"));
    assertTrue(isConfigurable(obj, "p"));
  }

  @Test
  public void defineOwnPropertyWithDataPropertyDescriptorShouldAssignAttributesBasedOnTheDescriptor() {
    PropertyDescriptor desc = new PropertyDescriptor().
      value(3).
      enumerable(true).
      configurable(true).
      writable(true);

    obj.defineOwnProperty("p", desc);

    assertEquals(3, obj.get("p"));
    assertTrue(isEnumerable(obj, "p"));
    assertTrue(isConfigurable(obj, "p"));
    assertTrue(isWritable(obj, "p"));
  }

  @Test
  public void defineOwnPropertyWithExistingPropertyAndEmptyDescriptorShouldLeavePropertyUnchanged() {
    PropertyDescriptor original = new PropertyDescriptor().value(3).enumerable(true).configurable(true).writable(true);

    obj.defineOwnProperty("p", original);
    obj.defineOwnProperty("p", new PropertyDescriptor());

    assertEquals(original, obj.getOwnPropertyDescriptor("p"));
  }

  @Test
  public void defineOwnPropertyWithExistingPropertyAndSameDescriptorShouldLeavePropertyUnchanged() {
    PropertyDescriptor original = new PropertyDescriptor().value(3).enumerable(true).configurable(true).writable(true);

    obj.defineOwnProperty("p", original);
    obj.defineOwnProperty("p", original);

    assertEquals(original, obj.getOwnPropertyDescriptor("p"));
  }

  @Test(expected = EcmaError.class)
  public void defineOwnPropertyShouldNotAllowChangingConfigurableFromFalseToTrue() {
    obj.defineOwnProperty("p", blank.configurable(false));
    obj.defineOwnProperty("p", blank.configurable(true));
  }

  @Test(expected = EcmaError.class)
  public void defineOwnPropertyShouldNotAllowChangingEnumerableWhenConfigurableIsFalse() {
    obj.defineOwnProperty("p", blank.enumerable(true).configurable(false));
    obj.defineOwnProperty("p", blank.enumerable(false));
  }

  @Test(expected = EcmaError.class)
  public void defineOwnPropertyShouldNotAllowChangingWritableFromFalseToTrueWhenConfigurableIsFalse() {
    obj.defineOwnProperty("p", blank.writable(false).configurable(false));
    obj.defineOwnProperty("p", blank.writable(true));
  }

  @Test(expected = EcmaError.class)
  public void defineOwnPropertyShouldNotAllowChangingValueWhenWritableIsFalse() {
    obj.defineOwnProperty("x", blank.value(1).writable(false).configurable(false));
    obj.defineOwnProperty("x", blank.value(2));
   
    obj.defineOwnProperty("y", blank.value(1).writable(false).configurable(true));
    obj.defineOwnProperty("y", blank.value(2));
  }

  @Test(expected = EcmaError.class)
  public void defineOwnPropertyShouldNotAllowChangingGetterWhenConfigurableIsFalse() {
    obj.defineOwnProperty("p", blank.getter(new StubFunction()).configurable(false));
    obj.defineOwnProperty("p", blank.getter(new StubFunction()));
  }

  @Test(expected = EcmaError.class)
  public void defineOwnPropertyShouldNotAllowChangingSetterWhenConfigurableIsFalse() {
    obj.defineOwnProperty("p", blank.setter(new StubFunction()).configurable(false));
    obj.defineOwnProperty("p", blank.setter(new StubFunction()));
  }

  @Test(expected = EcmaError.class)
  public void defineOwnPropertyShouldNotAllowChangingTypeOfPropertyDescriptorWhenConfigurableIsFalse() {
    obj.defineOwnProperty("p", blank.value(3).configurable(false));
    obj.defineOwnProperty("p", blank.getter(new StubFunction()));
  }

  @Test
  public void defineOwnPropertyShouldAllowChangingWritableFromTrueToFalseWhenConfigurableIsFalse() {
    obj.defineOwnProperty("p", blank.writable(true).configurable(false));
    obj.defineOwnProperty("p", blank.writable(false));
    assertEquals(false, obj.getOwnPropertyDescriptor("p").getWritable());
  }

  @Test
  public void defineOwnPropertyShouldAllowSettingEnumerableToTheSameValueWhenConfigurableIsFalse() {
    obj.defineOwnProperty("p", blank.enumerable(false).configurable(false));
    obj.defineOwnProperty("p", blank.enumerable(false));
    assertEquals(false, obj.getOwnPropertyDescriptor("p").getEnumerable());
  }

  @Test
  public void defineOwnPropertyShouldAllowChangingBetweenDataAndAccessorPropertyWhenConfigurableIsTrue() {
    obj.defineOwnProperty("p", blank.value(3).configurable(true));
    obj.defineOwnProperty("p", blank.getter(new StubFunction(4)));
    assertEquals(4, obj.get("p"));

    obj.defineOwnProperty("q", blank.getter(new StubFunction(3)).configurable(true));
    obj.defineOwnProperty("q", blank.value(4));
    assertEquals(4, obj.get("q"));
  }

  @Test
  public void defineOwnPropertyShouldAllowChangingAttributesToTrueWhenConfigurableIsTrue() {
    obj.defineOwnProperty("p", blank.enumerable(false).writable(false).configurable(true));

    PropertyDescriptor des = obj.getOwnPropertyDescriptor("p");
    assertEquals(false, des.getEnumerable());
    assertEquals(false, des.getWritable());

    obj.defineOwnProperty("p", blank.enumerable(true ).writable(true ));

    PropertyDescriptor desc = obj.getOwnPropertyDescriptor("p");
    assertEquals(true, desc.getEnumerable());
    assertEquals(true, desc.getWritable());
  }

  private boolean isEnumerable(ScriptableObject obj, String name) {
    int attributes = obj.getAttributes(name);
    return (attributes & ScriptableObject.DONTENUM) == 0;
  }
  private boolean isConfigurable(ScriptableObject obj, String name) {
    int attributes = obj.getAttributes(name);
    return (attributes & ScriptableObject.PERMANENT) == 0;
  }
  private boolean isWritable(ScriptableObject obj, String name) {
    int attributes = obj.getAttributes(name);
    return (attributes & ScriptableObject.READONLY) == 0;
  }

  private static class StubScriptableObject extends ScriptableObject {
    public String getClassName() { return "testing stub"; }

    @Override
    public PropertyDescriptor getOwnPropertyDescriptor(String name) {
      return super.getOwnPropertyDescriptor(name);
    }
  }
}
