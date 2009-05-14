package org.mozilla.javascript.tests;

import org.mozilla.javascript.*;

public class StubFunction extends ScriptableObject implements Function {
  private Object returnValue;

  public StubFunction(Object returnValue) {
    this.returnValue = returnValue;
  }
  public StubFunction() {
    this(null);
  }

  public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) { 
    return returnValue;
  }

  public Scriptable construct(Context cx, Scriptable scope, Object[] args) { return null; }

  public String getClassName() { return "test class StubFunction"; }
}

