package edu.iscas.tcse.favtrigger.instrumenter.mapred;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

public class MRInstrument {

    public static Object getLinkSourceFromMsg(List list) {
        try {
            return list.get(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("!!!FAVTrigger get rpc message id from message wrong:"+e);
            e.printStackTrace();
        }
        //maybe the client side was not run with the USE_FAV and FOR_YARN or
        //YARN_RPC configuration
        return FAVPathType.FAVMSG.toString()+":";
    }

    public static String storeYarnRpcClientSideSocket(Class protocol, InetSocketAddress address) {
        if(MRProtocols.isMRProtocol(protocol.getName())) {
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

}
