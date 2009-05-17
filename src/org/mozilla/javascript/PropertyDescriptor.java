package org.mozilla.javascript;

public class PropertyDescriptor implements Cloneable {
  protected Boolean enumerable = null;
  protected Boolean configurable = null;
  private Object value = null;
  private Boolean writable = null;
  // TODO these two values might be mutable, is it still okay to clone?
  private Function getter = null;
  private Function setter = null;

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
  public PropertyDescriptor getter(Function getter) {
    if (this.isDataDescriptor()) 
      throw new UnsupportedOperationException("Cannot add getter to a data property descriptor");

    PropertyDescriptor copy = this.copy();
    copy.getter = getter;
    return copy;
  }
  public PropertyDescriptor setter(Function setter) {
    if (this.isDataDescriptor()) 
      throw new UnsupportedOperationException("Cannot add setter to a data property descriptor");

    PropertyDescriptor copy = this.copy();
    copy.setter = setter;
    return copy;
  }

  public Boolean getEnumerable() {
    return enumerable;
  }
  public Boolean getConfigurable() {
    return configurable;
  }
  public Boolean getWritable() {
    return writable;
  }
  public Object getValue() {
    return this.value;
  }
  public Function getGetter() {
    return getter;
  }
  public Function getSetter() {
    return setter;
  }

  public boolean getEnumerableOrDefault() {
    return enumerable == null ? false : enumerable;
  }
  public boolean getConfigurableOrDefault() {
    return configurable == null ? false : configurable;
  }
  public boolean getWritableOrDefault() {
    return writable == null ? false : writable;
  }
  public Object getValueOrDefault() {
    return value == null ? Undefined.instance : value;
  }
  public Object getGetterOrDefault() {
    return getter == null ? Undefined.instance : getter;
  }
  public Object getSetterOrDefault() {
    return setter == null ? Undefined.instance : setter;
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

  /*
   * Implementation of [[FromPropertyDescriptor]] in 8.10.4 of the spec.
   */
  public NativeObject fromPropertyDescriptor() {
    NativeObject obj = new NativeObject();
    if (isDataDescriptor()) {
      obj.defineProperty("value", getValueOrDefault(), ScriptableObject.EMPTY);
      obj.defineProperty("writable", getWritableOrDefault(), ScriptableObject.EMPTY);
    } else if (isAccessorDescriptor()) {
      obj.defineProperty("get", getGetterOrDefault(), ScriptableObject.EMPTY);
      obj.defineProperty("set", getSetterOrDefault(), ScriptableObject.EMPTY);
    }
    obj.defineProperty("enumerable", getEnumerableOrDefault(), ScriptableObject.EMPTY);
    obj.defineProperty("configurable", getConfigurableOrDefault(), ScriptableObject.EMPTY);
    return obj;
  }

  /*
   * Implementation of [[ToPropertyDescriptor]] in 8.10.5 of the spec.
   */
  public static PropertyDescriptor toPropertyDescriptor(Scriptable attributes) {
    PropertyDescriptor desc = new PropertyDescriptor();
    if (attributes.has("enumerable", attributes)) {
      Object value = attributes.get("enumerable", attributes);
      desc = desc.enumerable(ScriptRuntime.toBoolean(value));
    }
    if (attributes.has("configurable", attributes)) {
      Object value = attributes.get("configurable", attributes);
      desc = desc.configurable(ScriptRuntime.toBoolean(value));
    }
    if (attributes.has("value", attributes)) {
      Object value = attributes.get("value", attributes);
      desc = desc.value(value);
    }
    if (attributes.has("writable", attributes)) {
      Object value = attributes.get("writable", attributes);
      desc = desc.writable(ScriptRuntime.toBoolean(value));
    }
    if (attributes.has("get", attributes)) {
      Object value = attributes.get("get", attributes);
      if ( !(value instanceof Function) ) {
        throw ScriptRuntime.typeError1("getter not callable", ScriptRuntime.toString(value));
      }
      desc = desc.getter((Function) value);
    }
    if (attributes.has("set", attributes)) {
      Object value = attributes.get("set", attributes);
      if ( !(value instanceof Function) ) {
        throw ScriptRuntime.typeError1("setter not callable", ScriptRuntime.toString(value));
      }
      desc = desc.setter((Function) value);
    }

    return desc;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || !(obj instanceof PropertyDescriptor)) return false;
    PropertyDescriptor that = (PropertyDescriptor) obj;
    return
      this.enumerable == that.enumerable &&
      this.writable == that.writable &&
      this.configurable == that.configurable &&
      areEqual(this.value, that.value) &&
      areEqual(this.getter, that.getter) &&
      areEqual(this.setter, that.setter);
  }

  @Override
  public int hashCode() {
    return hash(enumerable, 0) * hash(configurable, 1) * hash(writable, 2) *
      hash(value, 3) * hash(getter, 4) * hash(setter, 5);
  }

  @Override
  public String toString() {
    return String.format(
        "PropertyDescriptor[enumerable: %s, configurable: %s, writable: %s, value: %s, getter: %s, setter: %s]",
       enumerable, configurable, writable, value, getter, setter);
  }

  private static boolean areEqual(Object a, Object b) {
    return ((a==null) && (b==null)) || ((a!=null) && a.equals(b));
  }
  private static int hash(Object o, int shift) {
    return (o == null ? 1 : o.hashCode()) << shift;
  }

  private PropertyDescriptor copy() {
    try {
      return (PropertyDescriptor) this.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException("PropertyDescriptor does not support cloning", ex);
    }
  }
}
