package org.mozilla.javascript.tests;

import org.mozilla.javascript.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ScriptableObjectTest {
  private ScriptableObject obj;

  @Before
  public void setUp() {
    obj = new ScriptableObjectStub();
    Context.enter(); // this is needed for a property's getter function to be called
  }

  @After
  public void tearDown() {
    Context.exit();
  }

  @Test
  public void defineOwnPropertyWithEmptyPropertyDescriptorShouldBeWritableAndEnumerableAndConfigurable() {
    obj.defineOwnProperty("p", new PropertyDescriptor());

    assertNull(obj.get("p"));
    assertTrue(isEnumerable(obj, "p"));
    assertTrue(isConfigurable(obj, "p"));
    assertTrue(isWritable(obj, "p"));
  }

  @Test
  public void defineOwnPropertyWithAccessorPropertyDescriptorShouldAssignAttributesBasedOnTheDescriptor() {

    PropertyDescriptor desc = new PropertyDescriptor().
      enumerable(false).
      configurable(false).
      getter(new StubFunction(3)).
      setter(new StubFunction());

    obj.defineOwnProperty("p", desc);

    assertEquals(3, obj.get("p"));
    assertFalse(isEnumerable(obj, "p"));
    assertFalse(isConfigurable(obj, "p"));
  }

  @Test
  public void defineOwnPropertyWithDataPropertyDescriptorShouldAssignAttributesBasedOnTheDescriptor() {
    PropertyDescriptor desc = new PropertyDescriptor().
      value(3).
      enumerable(false).
      configurable(false).
      writable(false);

    obj.defineOwnProperty("p", desc);

    assertEquals(3, obj.get("p"));
    assertFalse(isEnumerable(obj, "p"));
    assertFalse(isConfigurable(obj, "p"));
    assertFalse(isWritable(obj, "p"));
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

  private static class ScriptableObjectStub extends ScriptableObject {
    public String getClassName() { return "testing stub"; }
  }
}
