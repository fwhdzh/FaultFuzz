package edu.iscas.tcse.favtrigger.taint;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;

public class FAVTaint {
	private static volatile long localTags = Long.MIN_VALUE;
	private static volatile long jreTags = Long.MIN_VALUE;

    private FAVTaint() {
        // Prevents this class from being instantiated
    }

    public static Taint specialFAVTaint(String labelName) {
		long tagID;
    	int nodeID;
    	String ip;
    	tagID = Long.MIN_VALUE;
    	Source source = new Source(tagID, "special", "special", "special", "special", labelName);
    	Taint t = Taint.withLabel(source);
        return t;
    }

    public static int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        //System.out.println(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0]).intValue();
    }

    public static String getIP() {
    	try {
			InetAddress ip4 = Inet4Address.getLocalHost();
			//return ip4.getHostAddress();
			return ip4.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	return "IP";
    }

    public static Object getNewLable(String cname, String mname, String desc, String type, String tag, String linkSource) {
    	long tagID;
    	int nodeID;
    	String ip;
    	tagID = localTags++;
    	/*
    	if(tag.equals(FAVTagType.APP.toString())){
        	tagID = localTags++;
        	nodeID = getProcessID();
    	} else {
    		tagID = jreTags++;
    		nodeID = -1;
    	}
    	*/
    	//Source source = new Source(nodeID, tagID, cname, mname, desc, type, linkSource);
    	Source source = new Source(tagID, cname, mname, desc, type, linkSource);
    	//SimplifiedSource source = new SimplifiedSource(nodeID, tagID);

    	return source;
    }

    public static Taint newFAVTaint(String cname, String mname, String desc, String type, String tag, String linkSource) {
//        if(linkSource.equals("/dev/urandom")) {
//        	//ignore data read from file /dev/urandom
//        	return Taint.emptyTaint();
//        }
//    	Taint t = Taint.withLabel(getNewLable(cname, mname, desc, type, tag, linkSource));
//        return t;
    	return Taint.emptyTaint();
    }

    public static int getLineNumber() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[9];
        int line = e.getLineNumber();
        return line;
    }

    public static Taint newInstanceFAVTaint(String newTpye, String cname, String mname, String desc, String type, String tag) throws ClassNotFoundException {
    	if(Class.forName(newTpye).isAssignableFrom(Exception.class)) {
    		Taint t = Taint.emptyTaint();
    		return t;
    	} else {
    		Taint t = Taint.withLabel(getNewLable(cname, mname, desc, type, tag, ""));
    		return t;
    	}
    }

    public static void combineNewTaints(LazyByteArrayObjTags obj, int off, int len, int rst, String cname, String mname, String desc, String type, String tag, String linkSource) {
//    	if (obj == null) {
//            return;
//        }
//
//    	for(int i = off; i < off+len && i < off+rst; i++) {
//    		Taint nt = newFAVTaint(cname, mname, desc, type, tag, linkSource);
//    		obj.setTaint(i, nt);
//    	}
    	return;
    }
}