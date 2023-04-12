package edu.iscas.CCrashFuzzer.control;

import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;

public abstract class AbstractNormalTarget {
    public abstract int run_target(FaultSequence seq, Conf util, String testID, long waitMinutes);
}
