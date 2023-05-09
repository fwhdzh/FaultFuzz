package edu.iscas.CCrashFuzzer;

import java.io.IOException;

import org.junit.Test;

public class ConfTest {
    
    @Test
    public void testLoadConfiguration() throws IOException {
        // String filePath = this.getClass().getResource("ConfTest.class").getPath().split("/ConfTest.class")[0] + "/confTest.properties";
        // Stat.log(filePath);
        // File f = new File(filePath);
        // if (f.exists()) {
        //     Conf conf = new Conf(f);
        //     conf.loadConfiguration();
        //     Assert.assertEquals(conf.AFL_PORT, 12081);
        //     Assert.assertEquals(conf.RECOVERY_MODE, false);
        //     Assert.assertEquals(conf.RECOVERY_FUZZINFO_PATH, "/data/fengwenhan/data/crashfuzz_fwh/FuzzInfo.txt");
        // }
        
    }

    @Test
    public void fwhTest() throws IOException {
        // Conf.s = new HashSet<>();
        // Conf.s.add(FaultStat.CRASH);
        // Conf.s.add(FaultStat.REBOOT);
        // Conf.s.add(FaultStat.NETWORK_DISCONNECT);
        // Conf.s.add(FaultStat.CRASH);
        // Conf.s.add(FaultStat.REBOOT);
        // Stat.log(Conf.s.size());
        // Assert.assertEquals(Conf.s.size(), 3);
    }
}
