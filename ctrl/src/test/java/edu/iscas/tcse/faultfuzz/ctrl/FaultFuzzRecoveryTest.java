package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

public class FaultFuzzRecoveryTest {

    @Test
    public void testRecoverCandidateQueue() throws IOException {
        // File confFile = new File("/data/fengwenhan/code/faultfuzz/package/zk-3.6.3/zk.properties");
        // Conf conf = new Conf(confFile);
        // conf.loadConfiguration();
        // FaultFuzzRecovery recover = new FaultFuzzRecovery(conf);
        Conf conf = Mockito.mock(Conf.class);
        // Mockito.when(conf.MAX_FAULTS).thenReturn(10);
        conf.MAX_FAULTS = 10;
        List<MaxDownNodes> maxDownGroup = conf.parseMaxDownGroup("2:{172.30.0.2,172.30.0.3,172.30.0.4,172.30.0.5,172.30.0.6}");
        // Mockito.when(conf.maxDownGroup).thenReturn(maxDownGroup);
        conf.maxDownGroup = maxDownGroup;
        FaultFuzzRecovery recover = new FaultFuzzRecovery(conf);
        recover.recoverCandidateQueue("/data/fengwenhan/data/faultfuzz_recovery_test");
    }

}
