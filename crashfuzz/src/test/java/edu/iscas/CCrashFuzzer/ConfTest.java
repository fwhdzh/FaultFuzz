package edu.iscas.CCrashFuzzer;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

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
}
