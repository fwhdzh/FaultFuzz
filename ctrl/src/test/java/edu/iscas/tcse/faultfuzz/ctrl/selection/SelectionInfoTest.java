package edu.iscas.tcse.faultfuzz.ctrl.selection;

import org.junit.Assert;
import org.junit.Test;

public class SelectionInfoTest {

    @Test
    public void testIntergerHashSet() {
        SelectionInfo.tested_fault_id.add(1);
        SelectionInfo.tested_fault_id.add(3);
        Assert.assertTrue(SelectionInfo.tested_fault_id.contains(new Integer(1)));
    }
}
