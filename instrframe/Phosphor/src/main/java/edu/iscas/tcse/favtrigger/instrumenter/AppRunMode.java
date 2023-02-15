package edu.iscas.tcse.favtrigger.instrumenter;

import java.io.FileOutputStream;
import java.io.IOException;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyBooleanArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyCharArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyDoubleArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyFloatArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyIntArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyLongArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyReferenceArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyShortArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedPrimitiveWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;
import edu.iscas.tcse.favtrigger.triggering.WaitToExec;
import edu.iscas.tcse.favtrigger.triggering.FaultSequence.FaultPos;

public class AppRunMode {
    public static void recordOrTriggerFully(long timestamp, FileOutputStream out, String path, LazyByteArrayObjTags bytes) {
    	// if(path.startsWith("FAVMSG:")) {
    	// 	java.util.List<String> stacks = RecordTaint.getCallStack(Thread.currentThread());
    	// 	System.out.println("!!!GY TEST record to "+path+" | "+Taint.combineTaintArray(bytes.taints)+stacks);
    	// }
    	if(path == null || path.equals("")) {
            return;
        }
    	try {
            RecordTaint.recordTaintsEntry(timestamp, out, path, bytes.val, bytes.taints, 0, bytes.val.length, "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	WaitToExec.checkCrashEvent(path, "");
    }

    public static void recordOrTrigger(long timestamp, FileOutputStream out, String path, LazyByteArrayObjTags bytes, int off, int len) {
   	// if(path.startsWith("ZK:")) {
   	// 	System.out.println("!!!GY TEST record to "+path+" | "+Taint.combineTaintArray(bytes.taints));
   	// }
    	if(path == null || path.equals("")) {
            return;
        }
    	try {
            RecordTaint.recordTaintsEntry(timestamp, out, path, bytes.val, bytes.taints, off, len, "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        WaitToExec.checkCrashEvent(path, "");
    }

    public static void recordOrTriggerTaint(long timestamp, FileOutputStream out, String path, Taint t) {
        if(path == null || path.equals("")) {
            return;
        }
        try {
            RecordTaint.recordTaintEntry(timestamp, out, path, (byte)0, t, "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        WaitToExec.checkCrashEvent(path, "");
    }

    public static void recordOrTriggerBefore(long timestamp, FileOutputStream out, String path) {
        if(path == null || path.equals("") || out == null) {
            return;
        }
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

    public static void recordOrTriggerAfter(long timestamp, FileOutputStream out, String path) {
        if(path == null || path.equals("") || out == null) {
            return;
        }
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

    public static void recordOrTriggerTaintedObj(long timestamp, FileOutputStream out, String path, TaintedPrimitiveWithObjTag obj) {
    	if(path == null || path.equals("")) {
            return;
        }
    	Taint t = Taint.emptyTaint();
    	if(obj instanceof TaintedReferenceWithObjTag) {
    		TaintedReferenceWithObjTag referObj = (TaintedReferenceWithObjTag) obj;
    		if(referObj.val instanceof LazyArrayObjTags) {
    			LazyArrayObjTags arrayWithTaints = (LazyArrayObjTags) referObj.val;
    			t = Taint.combineTaintArray(arrayWithTaints.taints);
    		} else {
    			t = obj.taint;
    		}
    	} else {
    		t = obj.taint;
    	}
        try {
            RecordTaint.recordTaintEntry(timestamp, out, path, (byte)0, t, "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        WaitToExec.checkCrashEvent(path, "");
    }

    public static void recordOrTriggerTaintedObjAndFavTaint(long timestamp, FileOutputStream out, String path, TaintedPrimitiveWithObjTag obj, Taint favTaint) {
    	if(path == null || path.equals("")) {
            return;
        }
    	Taint t = Taint.emptyTaint();
    	if(obj instanceof TaintedReferenceWithObjTag) {
    		TaintedReferenceWithObjTag referObj = (TaintedReferenceWithObjTag) obj;
    		if(referObj.val instanceof LazyArrayObjTags) {
    			LazyArrayObjTags arrayWithTaints = (LazyArrayObjTags) referObj.val;
    			t = Taint.combineTaintArray(arrayWithTaints.taints);
    		} else {
    			t = obj.taint;
    			t = Taint.combineTags(t, favTaint);
    		}
    	} else {
    		t = obj.taint;
    	}
        try {
            RecordTaint.recordTaintEntry(timestamp, out, path, (byte)0, t, "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        WaitToExec.checkCrashEvent(path, "");
    }

    public static LazyByteArrayObjTags getTaintedBytesFully(LazyByteArrayObjTags bytes, String cname, String mname,
            String desc, String type, String tag, String linkSource) {
        return getTaintedBytes(bytes, 0, bytes.val.length, cname, mname, desc, type, tag, linkSource);
    }

    public static LazyByteArrayObjTags getTaintedBytes(LazyByteArrayObjTags bytes, int off, int len, String cname, String mname,
            String desc, String type, String tag, String linkSource) {
    	if(bytes == null || bytes.val == null) {
            return bytes;
        }
    	for(int i = off; i < len; i++) {
            Taint t = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
            bytes.setTaint(i, t);
        }
        // if(linkSource.startsWith("hdfs:")) {
    	// 	System.out.println("!!!GY TEST gettaintedbytes from "+linkSource+" | "+Taint.combineTaintArray(bytes.taints));
    	// }
        return bytes;
//        if(Configuration.RECORD_PHASE) {
//            for(int i = off; i < len; i++) {
//                Taint t = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
//                bytes.setTaint(i, t);
//            }
//            if(linkSource.startsWith("hdfs:")) {
//        		System.out.println("!!!GY TEST gettaintedbytes from "+linkSource+" | "+Taint.combineTaintArray(bytes.taints));
//        	}
//            return bytes;
//        } else {
//            return bytes;
//        }
    }

    public static TaintedPrimitiveWithObjTag getTaintedPrimitive(TaintedPrimitiveWithObjTag taintedVal, String cname, String mname, String desc, String type, String tag, String linkSource) {
//    	System.out.println("!!!GY TEST get data from "+linkSource);
//    	if(linkSource.startsWith("ZK")) {
//    		System.out.println("!!!GY ZK TEST get data from "+linkSource);
//    	}
//    	if(Configuration.RECORD_PHASE) {//it is okay to overwrite an existing taint here.
//    		if(taintedVal instanceof TaintedReferenceWithObjTag) {
//    			TaintedReferenceWithObjTag refObj = (TaintedReferenceWithObjTag) taintedVal;
//    			if(refObj.val instanceof LazyArrayObjTags) {
//    				LazyArrayObjTags arrayVal = (LazyArrayObjTags) refObj.val;
//    				refObj.val = getTaintedArrayFully(arrayVal, cname, mname, desc, type, tag, linkSource);
//    				return taintedVal;
//    			}
//    		}
//            taintedVal.taint = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
//            if(linkSource.startsWith("hdfs:")) {
//        		System.out.println("!!!GY TEST gettaintedbytes from "+linkSource+" | "+taintedVal.taint);
//        	}
//            return taintedVal;
//        } else {
//            return taintedVal;
//        }
    	if(taintedVal instanceof TaintedReferenceWithObjTag) {
			TaintedReferenceWithObjTag refObj = (TaintedReferenceWithObjTag) taintedVal;
			if(refObj.val instanceof LazyArrayObjTags) {
				LazyArrayObjTags arrayVal = (LazyArrayObjTags) refObj.val;
				refObj.val = getTaintedArrayFully(arrayVal, cname, mname, desc, type, tag, linkSource);
				return taintedVal;
			}
		}
        taintedVal.taint = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
        // if(linkSource.startsWith("hdfs:")) {
    	// 	System.out.println("!!!GY TEST gettaintedbytes from "+linkSource+" | "+taintedVal.taint);
    	// }
        return taintedVal;
    }

    public static LazyArrayObjTags getTaintedArrayFully(LazyArrayObjTags array, String cname, String mname, String desc, String type, String tag, String linkSource) {
    	if(array == null) {
            return array;
        }
    	int len = 0;
    	if(array instanceof LazyBooleanArrayObjTags) {
    		len = ((LazyBooleanArrayObjTags) array).val.length;
    	} else if(array instanceof LazyByteArrayObjTags) {
    		len = ((LazyByteArrayObjTags) array).val.length;
    	} else if(array instanceof LazyCharArrayObjTags) {
    		len = ((LazyCharArrayObjTags) array).val.length;
    	} else if(array instanceof LazyDoubleArrayObjTags) {
    		len = ((LazyDoubleArrayObjTags) array).val.length;
    	} else if(array instanceof LazyFloatArrayObjTags) {
    		len = ((LazyFloatArrayObjTags) array).val.length;
    	} else if(array instanceof LazyIntArrayObjTags) {
    		len = ((LazyIntArrayObjTags) array).val.length;
    	} else if(array instanceof LazyLongArrayObjTags) {
    		len = ((LazyLongArrayObjTags) array).val.length;
    	} else if(array instanceof LazyReferenceArrayObjTags) {
    		len = ((LazyReferenceArrayObjTags) array).val.length;
    	} else if(array instanceof LazyShortArrayObjTags) {
    		len = ((LazyShortArrayObjTags) array).val.length;
    	}
//        if(Configuration.RECORD_PHASE) {
//            for(int i = 0; i < len; i++) {
//                Taint t = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
//                array.setTaint(i, t);
//            }
//            return array;
//        } else {
//            return array;
//        }
        for(int i = 0; i < len; i++) {
            Taint t = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
            array.setTaint(i, t);
        }
        return array;
    }

    public static Taint getNewTaint(String cname, String mname, String desc, String type, String tag, String linkSource) {
//    	System.out.println("!!!GY TEST get new taint from "+linkSource);
//    	if(linkSource.startsWith("ZK")) {
//    		System.out.println("!!!GY ZK TEST get new taint from "+linkSource);
//    	}
//    	if(Configuration.RECORD_PHASE) {//it is okay to overwrite an existing taint here.
//            return FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
//        } else {
//            return Taint.emptyTaint();
//        }
    	return FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
    }
}
