package org.mozilla.javascript;

public class PropertyDescriptor implements Cloneable {
  private Object value = null;
  private Boolean enumerable = null;
  private Boolean writable = null;
  private Boolean configurable = null;

  // TODO these two values might be mutable, is it still okay to clone?
  private Callable getter = null; 
  private Callable setter = null;

  public PropertyDescriptor value(Object value) {
    PropertyDescriptor copy = this.copy();
    copy.value = value;
    return copy;
  }

  public PropertyDescriptor enumerable(boolean enumerable) {
    PropertyDescriptor copy = this.copy();
    copy.enumerable = enumerable;
    return copy;
  }

  public PropertyDescriptor writable(boolean writable) {
    PropertyDescriptor copy = this.copy();
    copy.writable = writable;
    return copy;
  }

  public PropertyDescriptor configurable(boolean configurable) {
    PropertyDescriptor copy = this.copy();
    copy.configurable = configurable;
    return copy;
  }

  public PropertyDescriptor getter(Callable getterFunction) {
    PropertyDescriptor copy = this.copy();
    copy.getter = getterFunction;
    return copy;
  }

  public PropertyDescriptor setter(Callable setterFunction) {
    PropertyDescriptor copy = this.copy();
    copy.setter = setterFunction;
    return copy;
  }

  public Object getValue() {
    return this.value;
  }

  public boolean isEnumerable() {
    return enumerable;
  }

  public boolean isWritable() {
    return writable;
  }

  public boolean isConfigurable() {
    return configurable;
  }

  public Callable getGetter() {
    return getter;
  }

  public Callable getSetter() {
    return setter;
  }

  private PropertyDescriptor copy() {
    try {
      return (PropertyDescriptor) this.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException("PropertyDescriptor does not support cloning", ex);
    }
  }

}
