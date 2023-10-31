package edu.columbia.cs.psl.phosphor.control.fav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.PhosphorOption;

public class PhosphorOptionTest {
    @Test
    public void testConfigure() {
        String args[] = {"-saveResultInternal", "100000", "-useFaultFuzz", "true", "-annotationFile", "/data/fengwenhan/code/faultfuzz/info.txt"};
        PhosphorOption.configure(true, args);
        assertEquals(Configuration.SAVE_RESULT_INTERNAL, 100000);
        assertTrue(Configuration.USE_FAULT_FUZZ);
        assertEquals(Configuration.ANNOTATION_FILE, "/data/fengwenhan/code/faultfuzz/info.txt");
    }
}
