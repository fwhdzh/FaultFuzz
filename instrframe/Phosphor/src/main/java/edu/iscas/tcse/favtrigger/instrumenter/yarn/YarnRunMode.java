package edu.iscas.tcse.favtrigger.instrumenter.yarn;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;
import edu.iscas.tcse.favtrigger.triggering.WaitToExec;

public class YarnRunMode {
    public static void recordYarnRpcOrTrigger(long timestamp, FileOutputStream out, String path, LazyByteArrayObjTags bytes) {
        if(Configuration.RECORD_PHASE) {
//            System.out.println("!!!!!!GY yarn record"+Taint.combineTaintArray(bytes.taints));
            try {
                StackTraceElement[] callStack;
                callStack = Thread.currentThread().getStackTrace();
                List<String> callStackString = new ArrayList<String>();
                for(int i = 3; i < callStack.length; ++i) {
                    callStackString.add(callStack[i].toString());
                }
                RecordTaint.recordTaintsEntry(timestamp, out, path, bytes.val, bytes.taints, 0, bytes.val.length, "");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            WaitToExec.checkCrashEvent(path, "");
        }
//        System.out.println("!!!!GY recordYarnRpcOrTrigger:"+path+", taint:"+Taint.combineTaintArray(bytes.taints));
    }

    public static LazyByteArrayObjTags getTaintedBytes(LazyByteArrayObjTags bytes, String cname, String mname,
            String desc, String type, String tag, String linkSource) {
        if(bytes == null || bytes.val == null) {
            return bytes;
        }
        if(Configuration.RECORD_PHASE) {
            for(int i = 0; i < bytes.val.length; i++) {
                Taint t = FAVTaint.newFAVTaint(cname, mname, desc, type, tag, linkSource);
                bytes.setTaint(i, t);
            }
            return bytes;
        } else {
            return bytes;
        }
//        return bytes;
    }
}
