package edu.iscas.tcse.favtrigger.instrumenter.yarn;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

public class YarnInstrument {

    public static String combineIpWithMsgid(String ip, int msgid) {
        return FAVPathType.FAVMSG.toString()+":"+ip+"&"+msgid;
    }

    public static String combineIpWithMsgidForRead(String ip, int msgid) {
        return FAVPathType.FAVMSG.toString()+":READ"+ip+"&"+msgid;
    }

    public static String appendRead(String s) {
        return s+"*READ";
    }

    public static String appendBufSize(String s, int bufsize) {
        return s+"*"+bufsize;
    }

    public static String getRemoteAddrFromSource(String source) {
        //System.out.println("!!!GY getRemoteAddrFromSource:"+source);
        if(source.equals(FAVPathType.FAVMSG.toString()+":")) {//do not have the remote server info
            return "";
        } else {
            return source.substring(source.indexOf(":")+1, source.lastIndexOf("&"));
        }
    }

    public static Object getLinkSourceFromMsg(List list) {
        try {
            return list.get(0);
        } catch (IndexOutOfBoundsException e) {
            // System.out.println("!!!FAVTrigger get rpc message id from message wrong:"+e);
            // e.printStackTrace();
        }
        //maybe the client side was not run with the USE_FAV and FOR_YARN or
        //YARN_RPC configuration
        return FAVPathType.FAVMSG.toString()+":";
    }
    public static InetAddress getIPByName(String name) {
        InetAddress address = null;
        try {
           address = InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            System.out.println("!!!!!FAVTrigger storeYarnRpcClientSideSocket UnknownHostException");
        }
        return address;
     }
    public static String storeYarnRpcClientSideSocket(Class protocol, InetSocketAddress address) {
        //note!!!! address could be an unresolved address, e.g., RM1:8031
        //The later one would produce null getAddress() result
        if(YarnProtocols.isYarnProtocol(protocol.getName())) {
            if(address == null) {
                System.out.println("!!!FAVTrigger storeYarnRpcClientSideSocket get null address");
            } else if (address.getAddress() == null) {
                System.out.println("!!!FAVTrigger storeYarnRpcClientSideSocket cannot resolve the address");
                return address.getHostName();
            }
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

    public static void recordTaint(String s, LazyByteArrayObjTags b) {
        StackTraceElement[] callStack;
        Thread thread = Thread.currentThread();
        callStack = thread.getStackTrace();
        List<String> callStackString = new ArrayList<String>();
        for(int i = 5; i < callStack.length; ++i) {
            callStackString.add(callStack[i].toString());
        }
        Taint t = Taint.combineTaintArray(b.taints);
        if(t == null || t.isEmpty()) {
            System.out.println("!!!!!!!GY "+s+" record taint: is null or empty "+callStackString);
        } else {
            System.out.println("!!!!!!!GY "+s+" record taint NOT null or empty "+callStackString);
        }
        Integer i = 9;
    }
}
