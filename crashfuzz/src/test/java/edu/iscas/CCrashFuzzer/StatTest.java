package edu.iscas.CCrashFuzzer;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.iscas.CCrashFuzzer.Stat.LOG_LEVEL;
public class StatTest {
    
    @Test
    public void testStatSet() {
        LOG_LEVEL a = LOG_LEVEL.DEBUG;
        LOG_LEVEL b = LOG_LEVEL.INFO;
        LOG_LEVEL c = LOG_LEVEL.WARN;
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertTrue(b.compareTo(c) < 0);
    }
}
