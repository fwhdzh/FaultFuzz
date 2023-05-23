package edu.iscas.tcse.faultfuzz.control;

import edu.iscas.tcse.faultfuzz.Conf;
import edu.iscas.tcse.faultfuzz.FaultSequence;

public abstract class AbstractNormalTarget {
    public abstract int run_target(FaultSequence seq, Conf util, String testID, long waitMinutes);
}
