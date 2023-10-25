package edu.iscas.tcse.favtrigger.instrumenter.hdfs;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;

public class HDFSInstrument {

    public static String storeHDFSRpcClientSideSocket(Class protocol, InetSocketAddress address) {
        if(HDFSProtocols.isHDFSProtocol(protocol.getName())) {
            return address.getAddress().getHostAddress();
        } else {
            return null;
        }
    }

    public static void checkTaint(String s, TaintedReferenceWithObjTag obj) {
        Taint t = Taint.combineTaintArray(((LazyByteArrayObjTags) obj.val).taints);
        recordTaint(s, t);
    }

    public static void recordTaint(String s, Taint t) {
        StackTraceElement[] callStack;
        Thread thread = Thread.currentThread();
        callStack = thread.getStackTrace();
        List<String> callStackString = new ArrayList<String>();
        for(int i = 5; i < callStack.length; ++i) {
            callStackString.add(callStack[i].toString());
        }
        if(t == null || t.isEmpty()) {
            System.out.println("!!!!!!!GY "+s+" record taint: is null or empty "+callStackString);
        } else {
            System.out.println("!!!!!!!GY "+s+" record taint NOT null or empty "+callStackString);
        }
    }
    public static void recordString(String s) {
        StackTraceElement[] callStack;
        Thread thread = Thread.currentThread();
        callStack = thread.getStackTrace();
        List<String> callStackString = new ArrayList<String>();
        for(int i = 5; i < callStack.length; ++i) {
            callStackString.add(callStack[i].toString());
        }
        System.out.println("!!!!!!!GY TEST HDFS: "+s+" "+callStackString);
    }
}
