package edu.iscas.tcse.faultfuzz.ctrl.control;

public abstract class AbstractTarget {

    // protected abstract void beforeTarget(Object data, Conf conf, Object... args);
    protected abstract void beforeTarget(Object data, Object... args);
    protected abstract void doTarget();
    protected abstract Object afterTarget();
}
