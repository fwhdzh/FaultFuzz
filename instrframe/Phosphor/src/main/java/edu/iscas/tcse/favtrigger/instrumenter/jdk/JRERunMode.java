package edu.iscas.tcse.favtrigger.instrumenter.jdk;

import java.io.FileOutputStream;
import java.io.IOException;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;
import edu.iscas.tcse.favtrigger.triggering.WaitToExec;
import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultPos;

public class JRERunMode {
	public static enum JREType {
		FILE, MSG, OTHER, CREATE, DELETE
	}
    public static void test(String s) {
        System.out.println("!!!!!!!!!"+s);
    }
    public static void test(boolean s) {
        System.out.println("!!!!!!!!!"+s);
    }

    public static Taint newFAVTaintOrEmpty(String cname, String mname, String desc, String type, String tag, String linkSource, String jretype) {
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(linkSource))) {
    		return Taint.emptyTaint();
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return Taint.emptyTaint();
    	}
//    	if(Configuration.RECORD_PHASE && Configuration.USE_FAV) {
//    		StackTraceElement[] callStack;
//        	callStack = Thread.currentThread().getStackTrace();
//        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
//        	for(int i = 7; i < callStack.length; ++i) {
//        		callStackString.add(callStack[i].toString());
//        	}
//        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
//        		return Taint.emptyTaint();
//        	}
//            if(Configuration.FAVDEBUG) {
//                System.out.println("FAVTrigger read a byte from:"+linkSource);
//            }
//            return FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
//        } else {
//            return Taint.emptyTaint();
//        }
    	if(Configuration.USE_FAV) {
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
//    	if(Configuration.RECORD_PHASE && Configuration.USE_FAV) {
//    		StackTraceElement[] callStack;
//        	callStack = Thread.currentThread().getStackTrace();
//        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
//        	for(int i = 7; i < callStack.length; ++i) {
//        		callStackString.add(callStack[i].toString());
//        	}
//        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
//        		return;
//        	}
//        	if(skipPath(linkSource)) {
//            	return;
//            }
//            if(Configuration.FAVDEBUG) {
//                System.out.println("FAVTrigger read "+rst+" bytes from:"+linkSource);
//            }
//            FAVTaint.combineNewTaints(obj, off, len, rst, cname, mname, desc, type, tag, linkSource);
//            //FAVTaint.combineNewTaints(obj, off, len, rst, cname, mname, desc, type, tag, linkSource);
//        } else {
//            return;
//        }
    	if(Configuration.USE_FAV) {
    		StackTraceElement[] callStack;
        	callStack = Thread.currentThread().getStackTrace();
        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
        	for(int i = 7; i < callStack.length; ++i) {
        		callStackString.add(callStack[i].toString());
        	}
        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
        		return;
        	}
        	// if(skipPath(linkSource)) {
            // 	return;
            // }
            // if(Configuration.FAVDEBUG) {
            //     System.out.println("FAVTrigger read "+rst+" bytes from:"+linkSource);
            // }
            FAVTaint.combineNewTaints(obj, off, len, rst, cname, mname, desc, type, tag, linkSource);
            //FAVTaint.combineNewTaints(obj, off, len, rst, cname, mname, desc, type, tag, linkSource);
        }
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
    public static void recordDeleteOrTrigger(long timestamp, FileOutputStream out, String path, Taint t, String jretype) {
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(path))) {
    		return;
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return;
    	}
    	if(Configuration.USE_FAV) {
            StackTraceElement[] callStack;
        	callStack = Thread.currentThread().getStackTrace();
        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
        	for(int i = 7; i < callStack.length; ++i) {
        		callStackString.add(callStack[i].toString());
        	}
        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
        		return;
        	}
        	try {
                // if(skipPath(path)) {
                // 	return;
                // }
                byte b = 0;
                //RecordTaint.recordTaintEntry(out, path, b, taint, RecordTaint.getMD5Hash(b));
                RecordTaint.recordTaintEntry(timestamp, out, path, b, t, "");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    		WaitToExec.checkCrashEvent(path, "");
        } else {
        	return;
        }
    }

    public static void recordOrTriggerCreateDelete(long timestamp, FileOutputStream out, String path, String jretype) {
       if(path == null || path.equals("")) {
           return;
       }
       if(!Configuration.JDK_FILE || skipPath(path)) {
    	   return;
       }
       if(Configuration.USE_FAV) {
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
    	if(Configuration.USE_FAV) {
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
    	if(Configuration.USE_FAV) {
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

    public static void recordByteOrTrigger(long timestamp, FileOutputStream out, String path, byte b, Taint taint, String jretype) {
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(path))) {
    		return;
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return;
    	}
    	if(Configuration.USE_FAV) {
        	StackTraceElement[] callStack;
        	callStack = Thread.currentThread().getStackTrace();
        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
        	for(int i = 7; i < callStack.length; ++i) {
        		callStackString.add(callStack[i].toString());
        	}
        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
        		return;
        	}
        	try {
                // if(Configuration.FAVDEBUG) {
                //     RecordTaint.printString("FAVTrigger: system is going to write a byte to "+path+", taint:"+taint);
                // }
                // if(skipPath(path)) {
                // 	return;
                // }
                //RecordTaint.recordTaintEntry(out, path, b, taint, RecordTaint.getMD5Hash(b));
                RecordTaint.recordTaintEntry(timestamp, out, path, b, taint, "");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			WaitToExec.checkCrashEvent(path, "");
        } else {
        	return;
        }
    }

    public static void recordBytesOrTrigger(long timestamp, FileOutputStream out, String path, LazyByteArrayObjTags bytes, int off, int len, String jretype) {
    	if(jretype.equals(JREType.FILE.toString()) && (!Configuration.JDK_FILE || skipPath(path))) {
    		return;
    	} else if (jretype.equals(JREType.MSG.toString()) && !Configuration.JDK_MSG) {
    		return;
    	}
    	if(Configuration.USE_FAV) {
        	StackTraceElement[] callStack;
        	callStack = Thread.currentThread().getStackTrace();
        	java.util.List<String> callStackString = new java.util.ArrayList<String>();
        	for(int i = 7; i < callStack.length; ++i) {
        		callStackString.add(callStack[i].toString());
        	}
        	if(callStackString.toString().contains("edu.iscas.tcse") || callStackString.toString().contains("edu.columbia.cs.psl.phosphor")) {
        		return;
        	}
        	try {
            	// if(skipPath(path)) {
                // 	return;
                // }
                // if(Configuration.FAVDEBUG) {
                //     System.out.println("FAVTrigger: goint to record the write to "+path);
                // }
                //RecordTaint.recordTaintsEntry(out, path, bytes.val,
                //bytes.taints, off, len,
                //RecordTaint.getMD5HashForBytes(bytes.val, off, len));
                RecordTaint.recordTaintsEntry(timestamp, out, path, bytes.val, bytes.taints, off, len, "");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			WaitToExec.checkCrashEvent(path, "");
        } else {
            return;
        }
    }
}
