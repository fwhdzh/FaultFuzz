package edu.iscas.tcse.faultfuzz.control;

import edu.iscas.tcse.faultfuzz.Conf;

public abstract class AbstractTarget {

    public abstract void beforeTarget(Object data, Conf conf, Object... args);
    public abstract void doTarget();
    public abstract Object afterTarget();
}
