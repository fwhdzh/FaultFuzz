package edu.iscas.tcse.favtrigger.debugger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.iscas.tcse.favtrigger.tracing.RecordTaint;

public class ZKStartAndEndRecordDebugger {
    public static void debugWithCallStack(String s) {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [Deminer] - INFO - "+s;
        rst = rst + "thread name: " + Thread.currentThread().getName() + "\n";
        rst = rst + "thread id: " + Thread.currentThread().getId() + "\n";
        List<String> callstack = RecordTaint.getCallStack(Thread.currentThread(), 0);
        rst = rst + "callstack: " + callstack.toString();
        System.out.println(rst);
    }

    public static void debug(String s) {
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        String rst = df.format(day)+" [Deminer] - INFO - "+s;
        rst = rst + "thread name: " + Thread.currentThread().getName();
        // List<String> callstack = RecordTaint.getCallStack(Thread.currentThread(), 0);
        // rst = rst + "callstack: " + callstack.toString();
        System.out.println(rst);
    }

    public static String getInternalName() {
        String result = "";
        result = ZKStartAndEndRecordDebugger.class.getName().replace(".", "/");
        return result;
    }
}
