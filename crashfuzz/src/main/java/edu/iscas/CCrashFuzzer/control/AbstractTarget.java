package edu.iscas.CCrashFuzzer.control;

import edu.iscas.CCrashFuzzer.Conf;

public abstract class AbstractTarget {

    public abstract void beforeTarget(Object data, Conf conf, Object... args);
    public abstract void doTarget();
    public abstract Object afterTarget();
}
