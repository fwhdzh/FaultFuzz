package edu.iscas.CCrashFuzzer;

public abstract class AbstractFuzzTarget {
    public abstract int run_target(FaultSequence seq, Conf util, String testID, long waitMinutes);
}
