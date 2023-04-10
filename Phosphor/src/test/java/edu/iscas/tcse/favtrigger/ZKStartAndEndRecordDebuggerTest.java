package edu.iscas.tcse.favtrigger;

import org.junit.Test;

import edu.iscas.tcse.favtrigger.debugger.ZKStartAndEndRecordDebugger;

import java.util.Random;

import static org.junit.Assert.*;

public class ZKStartAndEndRecordDebuggerTest {

    @Test
    public void testGetOwner() {
        ZKStartAndEndRecordDebugger z = new ZKStartAndEndRecordDebugger();
        String s = z.getClass().getName().replace(".", "/");
        String s2 = ZKStartAndEndRecordDebugger.getInternalName();
        System.out.println(s2);
        assertEquals(s, s2);
    }
}
