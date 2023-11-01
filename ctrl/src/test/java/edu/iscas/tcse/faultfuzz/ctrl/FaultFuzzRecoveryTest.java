package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class FaultFuzzRecoveryTest {

    @Test
    public void test() {
        String input = "MAP_1785(130)";
        Pattern pattern = Pattern.compile("MAP_(\\d+)\\(\\d+\\)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String number = matcher.group(1);
            System.out.println("Extracted Number: " + number);
        } else {
            System.out.println("No number found in the string.");
        }
    }

    @Test
    public void testRecoverCandidateQueue() throws IOException {;
        // Conf conf = Mockito.mock(Conf.class);
        // conf.MAX_FAULTS = 10;
        // List<MaxDownNodes> maxDownGroup = conf.parseMaxDownGroup("2:{172.30.0.2,172.30.0.3,172.30.0.4,172.30.0.5,172.30.0.6}");
        // conf.maxDownGroup = maxDownGroup;
        // conf.CUR_FAULT_FILE = new File("faultUnderTest");
        // FaultFuzzRecovery recover = new FaultFuzzRecovery(conf);
        // recover.recoverCandidateQueue("/data/fengwenhan/data/faultfuzz_recovery_test");
    }

}
