package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;


public class IOPointTest {
    @Test
    public void testHumanString() throws IOException {
        IOPoint point = new IOPoint();
        point.ioID = 1324234234;
        point.ip = "172.30.0.1";
        point.appearIdx = 3;
        point.fwhIndex = 2;
        List<String> callStack = new ArrayList<>();
        callStack.add("edu.iscas.tcse.faultfuzz.ctrl.IOPointTest.testHumanString(IOPointTest.java:56)");
        callStack.add("edu.iscas.tcse.faultfuzz.ctrl.IOPointTest.testHumanString(IOPointTest.java:57)");
        callStack.add("edu.iscas.tcse.faultfuzz.ctrl.IOPointTest.testHumanString(IOPointTest.java:58)");
        point.CALLSTACK = callStack;
        point.PATH = "FAVMSG:172.30.0.2&3#1";
        String s = point.toString();
        System.out.println(s);
        IOPoint point2 = IOPoint.praseFromString(s);
        System.out.println(point2.toString());
        Assert.assertEquals(point2.ioID, point.ioID);
        Assert.assertEquals(point2.ip, point.ip);
        Assert.assertEquals(point2.appearIdx, point.appearIdx);
        Assert.assertEquals(point2.fwhIndex, point.fwhIndex);
        Assert.assertEquals(point2.CALLSTACK.size(), point.CALLSTACK.size());
        for (int i = 0; i < point2.CALLSTACK.size(); i++) {
            Assert.assertEquals(point2.CALLSTACK.get(i), point.CALLSTACK.get(i));
        }
        Assert.assertEquals(point2.PATH, point.PATH);
    }
}
