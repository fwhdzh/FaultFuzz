package edu.iscas.tcse.faultfuzz.ctrl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.iscas.tcse.faultfuzz.ctrl.Stat.LOG_LEVEL_SET;
public class StatTest {
    
    @Test
    public void testStatSet() {
        LOG_LEVEL_SET a = LOG_LEVEL_SET.DEBUG;
        LOG_LEVEL_SET b = LOG_LEVEL_SET.INFO;
        LOG_LEVEL_SET c = LOG_LEVEL_SET.WARN;
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertTrue(b.compareTo(c) < 0);
    }
}
