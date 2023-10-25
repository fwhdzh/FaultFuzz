package edu.iscas.tcse.faultfuzz.ctrl.control;

import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;

public abstract class AbstractNormalTarget {
    public abstract int run_target(FaultSequence seq, Conf util, String testID, long waitMinutes);
}
