package edu.iscas.tcse.faultfuzz.ctrl.utils;

import org.junit.Test;

import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;

public class FileUtilTest {
    @Test
    public void testLoadcurrentFaultPoint() {
        FaultSequence fs = FileUtil.loadcurrentFaultPoint("/data/fengwenhan/data/faultfuzz_replay_test/2_1f/zk363curFault");
        System.out.println(fs);
    }
}
