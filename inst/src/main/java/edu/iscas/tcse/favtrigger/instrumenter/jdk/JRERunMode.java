package edu.iscas.tcse.favtrigger.instrumenter.jdk;

import java.io.FileOutputStream;
import java.io.IOException;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPos;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;
import edu.iscas.tcse.favtrigger.triggering.WaitToExec;

public class JRERunMode {
	public static enum JREType {
		FILE, MSG, OTHER, CREATE, DELETE
	}

    public static boolean skipPath(String path) {
        for(String str:Configuration.FILTER_PATHS) {
            if(path.startsWith(str)) {
                return true;
            }
        }
        if(Configuration.DATA_PATHS.size() == 0) {
            return false;
        }
        for(String str:Configuration.DATA_PATHS) {
            if(path.startsWith(str)) {
                return false;
            } else if ((path.substring(path.indexOf(":")+1)).startsWith(str)) {
            	return false;
            }
        }
        return true;
    }

    public static Taint newFAVTaintOrEmpty(String cname, String mname, String desc, String type, String tag, String linkSource, String jretype) {
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(linkSource))) {
    		return Taint.emptyTaint();
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return Taint.emptyTaint();
    	}
    	if(Configuration.USE_FAULT_FUZZ) {
    		StackTraceElement[] callStack;
        	callStack = Thread.currentThread().getStackTrace();
        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
        	for(int i = 7; i < callStack.length; ++i) {
        		callStackString.add(callStack[i].toString());
        	}
        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
        		return Taint.emptyTaint();
        	}
            // if(Configuration.FAVDEBUG) {
            //     System.out.println("FAVTrigger read a byte from:"+linkSource);
            // }
            return FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
        } else {
            return Taint.emptyTaint();
        }
    }

    public static void combineNewTaintsOrEmpty(LazyByteArrayObjTags obj, int off, int len, int rst, String cname, String mname, String desc, String type, String tag, String linkSource, String jretype) {
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(linkSource))) {
    		return;
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return;
    	}
    	if(Configuration.USE_FAULT_FUZZ) {
    		StackTraceElement[] callStack;
        	callStack = Thread.currentThread().getStackTrace();
        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
        	for(int i = 7; i < callStack.length; ++i) {
        		callStackString.add(callStack[i].toString());
        	}
        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
        		return;
        	}
            FAVTaint.combineNewTaints(obj, off, len, rst, cname, mname, desc, type, tag, linkSource);
        }
    }

    public static void recordOrTriggerCreateDelete(long timestamp, FileOutputStream out, String path, String jretype) {
       if(path == null || path.equals("")) {
           return;
       }
       if(!Configuration.JDK_FILE || skipPath(path)) {
    	   return;
       }
       if(Configuration.USE_FAULT_FUZZ) {
    	   try {
               RecordTaint.recordTaintEntry(timestamp, out, path, (byte)0, Taint.emptyTaint(), FaultPos.BEFORE.toString());
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
           try {
                WaitToExec.checkCrashEvent(path, FaultPos.BEFORE.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
       }
    }

    public static void recordOrTriggerBefore(long timestamp, FileOutputStream out, String path, String jretype) {
        // System.out.println("******Prepare Generate REC to "+path+" "+jretype+"******");
    	if(path == null || path.equals("") || out == null) {
            return;
        }
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(path))) {
    		return;
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return;
    	}
    	if(Configuration.USE_FAULT_FUZZ) {
    		try {
                // System.out.println("******Generate REC to "+path+" "+jretype+"******");
                RecordTaint.recordTaintEntry(timestamp, out, path, (byte)0, Taint.emptyTaint(), FaultPos.BEFORE.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                WaitToExec.checkCrashEvent(path, FaultPos.BEFORE.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    	}
    }

    public static void recordOrTriggerAfter(long timestamp, FileOutputStream out, String path, String jretype) {
    	if(path == null || path.equals("") || out == null) {
            return;
        }
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(path))) {
    		return;
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return;
    	}
    	if(Configuration.USE_FAULT_FUZZ) {
    		try {
                RecordTaint.recordTaintEntry(timestamp, out, path, (byte)0, Taint.emptyTaint(), FaultPos.AFTER.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                WaitToExec.checkCrashEvent(path, FaultPos.AFTER.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    	}
    }

}
