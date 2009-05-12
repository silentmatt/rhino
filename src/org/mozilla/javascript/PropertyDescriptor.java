package org.mozilla.javascript;

public class PropertyDescriptor implements Cloneable {
  protected Boolean enumerable = null;
  protected Boolean configurable = null;
  private Object value = null;
  private Boolean writable = null;
  // TODO these two values might be mutable, is it still okay to clone?
  private Callable getter = null; 
  private Callable setter = null;

  public PropertyDescriptor enumerable(Boolean enumerable) {
    PropertyDescriptor copy = this.copy();
    copy.enumerable = enumerable;
    return copy;
  }
  public PropertyDescriptor configurable(Boolean configurable) {
    PropertyDescriptor copy = this.copy();
    copy.configurable = configurable;
    return copy;
  }
  public PropertyDescriptor value(Object value) {
    if (this.isAccessorDescriptor()) 
      throw new UnsupportedOperationException("Cannot add value to an accessor property descriptor");

    PropertyDescriptor copy = this.copy();
    copy.value = value;
    return copy;
  }
  public PropertyDescriptor writable(Boolean writable) {
    if (this.isAccessorDescriptor()) 
      throw new UnsupportedOperationException("Cannot add writable to an accessor property descriptor");

    PropertyDescriptor copy = this.copy();
    copy.writable = writable;
    return copy;
  }
  public PropertyDescriptor getter(Callable getter) {
    if (this.isDataDescriptor()) 
      throw new UnsupportedOperationException("Cannot add getter to a data property descriptor");

    PropertyDescriptor copy = this.copy();
    copy.getter = getter;
    return copy;
  }
  public PropertyDescriptor setter(Callable setter) {
    if (this.isDataDescriptor()) 
      throw new UnsupportedOperationException("Cannot add setter to a data property descriptor");

    PropertyDescriptor copy = this.copy();
    copy.setter = setter;
    return copy;
  }

  public boolean isEnumerable() {
    return enumerable;
  }
  public boolean isConfigurable() {
    return configurable;
  }
  public Object getValue() {
    return this.value;
  }
  public boolean isWritable() {
    return writable;
  }
  public Callable getGetter() {
    return getter;
  }
  public Callable getSetter() {
    return setter;
  }

  public boolean isDataDescriptor() {
    return value != null || writable != null;
  }
  public boolean isAccessorDescriptor() {
    return getter != null || setter != null;
  }
  public boolean isGenericDescriptor() {
    return !isDataDescriptor() && !isAccessorDescriptor();
  }

  public NativeObject fromPropertyDescriptor() {
    NativeObject obj = new NativeObject();
    if (isDataDescriptor()) {
      if (value != null) obj.defineProperty("value", value, ScriptableObject.EMPTY);
      if (writable != null) obj.defineProperty("writable", writable, ScriptableObject.EMPTY);
    } else if (isAccessorDescriptor()) {
      if (getter != null) obj.defineProperty("get", getter, ScriptableObject.EMPTY);
      if (setter != null) obj.defineProperty("set", setter, ScriptableObject.EMPTY);
    }
    if (enumerable != null) obj.defineProperty("enumerable", enumerable, ScriptableObject.EMPTY);
    if (configurable != null) obj.defineProperty("configurable", configurable, ScriptableObject.EMPTY);
    return obj;
  }

  private PropertyDescriptor copy() {
    try {
      return (PropertyDescriptor) this.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException("PropertyDescriptor does not support cloning", ex);
    }
  }

}
