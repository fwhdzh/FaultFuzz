package edu.iscas.tcse.favtrigger.triggering;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import com.alibaba.fastjson.JSONObject;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.iscas.tcse.favtrigger.MyLogger;
import edu.iscas.tcse.favtrigger.instrumenter.TriggerEvent;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.tracing.FAVEntry;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;

public class WaitToExec { //for docker
    public static final String cannotSchedule = "FAV-CANNOT-SCHEDULE-THIS-CRASH-POINT";

	public static void triggerAndRecordFaultPoint(String path) throws IOException {
		checkFaultPoint(path);
		RecordTaint.recordFaultPoint(path);
	}

	public static void triggerAndRecordFaultPoint(FAVEntry entry) throws IOException {
		if (Configuration.EXEC_MODE == Configuration.EXEC_MODE_SET.FaultFuzz || Configuration.EXEC_MODE == Configuration.EXEC_MODE_SET.Replay) {
			// handleFaultPointInDeplayMode(procID, crashNode, entry, callstack, path);
			handleFaultPointInDeplayMode(entry);
		}

		RecordTaint.recordFaultPoint(entry);
	}

	public static void triggerAndRecordFaultPoint(List<String> callStack, String reportNodeIP, String path) throws IOException {
		MyLogger.log("checkFaultPoint begin! path is: "+path);
		MyLogger.log("callstack is :"+JSONObject.toJSONString(callStack));
		MyLogger.log("exec mode: " + Configuration.EXEC_MODE);
		long procID = FAVTaint.getProcessID();
		MyLogger.log("procID: " + procID);
		triggerAndRecordFaultPoint(callStack, reportNodeIP, path, procID);
	}

	public static void triggerAndRecordFaultPoint(List<String> callStack, String reportNodeIP, String path, long procID) throws IOException {
		FAVEntry entry = new FAVEntry();
		entry.PATH = path;
        entry.CALLSTACK = callStack;
        entry.ip = reportNodeIP;
		entry.procID = procID;
		triggerAndRecordFaultPoint(entry);
	}

	public static void checkFaultPoint(String path) {
		MyLogger.log("checkFaultPoint begin! path is: "+path);
		Thread thread = Thread.currentThread();
        List<String> callstack = RecordTaint.getCallStack(thread, 4);
		MyLogger.log("callstack is :"+JSONObject.toJSONString(callstack));
        FAVEntry entry = new FAVEntry();
        long procID = FAVTaint.getProcessID();
        String crashNode = FAVTaint.getIP();
        entry.PATH = path;
        entry.CALLSTACK = callstack;
        entry.ip = crashNode;

		// handleCrashPointInDeplayMode(procID, crashNode, entry, callstack, path);
		// MyLogger.log("replay mode: " + Configuration.REPLAY_MODE + ", determine state: " + Configuration.DETERMINE_STATE);

		MyLogger.log("exec mode: " + Configuration.EXEC_MODE);

		if (Configuration.EXEC_MODE == Configuration.EXEC_MODE_SET.FaultFuzz || Configuration.EXEC_MODE == Configuration.EXEC_MODE_SET.Replay) {
			handleFaultPointInDeplayMode(entry);
		}

		MyLogger.log("checkFaultPoint end!");
	}

    public static void checkCrashEvent(String path, String contentID) {
        Thread thread = Thread.currentThread();
        List<String> callstack = RecordTaint.getCallStack(thread, 4);
        FAVEntry entry = new FAVEntry();
        long procID = FAVTaint.getProcessID();
        String crashNode = FAVTaint.getIP();
        entry.PATH = path;
        entry.CALLSTACK = callstack;
        entry.ip = crashNode;

		MyLogger.log("exec mode: " + Configuration.EXEC_MODE);

		if (Configuration.EXEC_MODE == Configuration.EXEC_MODE_SET.FaultFuzz || Configuration.EXEC_MODE == Configuration.EXEC_MODE_SET.Replay) {
			// handleFaultPointInDeplayMode(procID, crashNode, entry, callstack, path);
			handleFaultPointInDeplayMode(entry);
		} 
    }

    public static int currentIOID(List<String> callstack) {
    	return callstack.toString().hashCode();
    }


	public static void handleFaultPointInDeplayMode(FAVEntry entry) {
		// long procID = FAVTaint.getProcessID();
		long procID = entry.procID;
		String nodeIP = entry.ip;
		List<String> callstack = entry.CALLSTACK;
		String path = entry.PATH;

		Integer ioID = currentIOID(callstack);
		MyLogger.log("WaitToExec arrives: " + ioID.intValue());
		// String procInfo = "";
		String fuzzCommand = "";
		try {
			Random rand = new Random();
			int id = rand.nextInt();
			String cliId = "" + nodeIP + ":" + id;
			String threadInfo  = "" + Thread.currentThread().getId() + ":" + Thread.currentThread().getName();
			String info = "";
			info = info + "WaitToExec handle ioID: " + ioID.intValue() + "\n";
			info = info + "WaitToExec handle nodeIP: " + nodeIP + "\n";
			info = info + "WaitToExec handle cliId" + cliId + "\n";
			info = info + "WaitToExec handle nodeIP path is " + path + "\n";
			info = info + "thread id " + threadInfo + "\n";
			MyLogger.log(info);
			if (Configuration.REPLAY_MODE) {
				if (!Configuration.REPLAY_NOW) {
					return;
				} 
			}  
			if (Configuration.DETERMINE_STATE == 0) {
				MyLogger.log("DETERMINE_STATE is 0! return!" );
				return;
			}
			String[] secs = Configuration.CONTROLLER_SOCKET.split(":");
			Socket socket = new Socket(secs[0].trim(), Integer.parseInt(secs[1].trim()));
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			outStream.writeInt(ioID.intValue());
			outStream.writeUTF(nodeIP);
			outStream.writeUTF(cliId);
			outStream.writeUTF(path);
			outStream.writeUTF(threadInfo);
			outStream.flush();
			MyLogger.log("" + cliId + "has been sent to controller!");
			fuzzCommand = inStream.readUTF();
			inStream.close();
			outStream.close();
			socket.close();
			String suffix = " [" + id + "] For io " + ioID;
			if (fuzzCommand.equals(TriggerEvent.CONTI.toString())
					|| fuzzCommand.equals(TriggerEvent.REBOOT.toString())) {
				MyLogger.log(nodeIP + ":" + procID + " System WaitToExec received keep exec command!" + fuzzCommand
						+ suffix);
				return;
			} else if (fuzzCommand.equals(TriggerEvent.CRASH.toString())) {
				MyLogger.log(nodeIP + ":" + procID + " System WaitToExec received crash command!" + fuzzCommand
						+ suffix);
				while (true) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				MyLogger.log(nodeIP + ":" + procID + " System WaitToExec received abnormal message: " + fuzzCommand
						+ suffix);
			}
		} catch (Exception e) {
			MyLogger.log(nodeIP + ":" + procID + " [ERROR] System WaitToExec got exception:" + e.getMessage());
			e.printStackTrace();
		}
		MyLogger.log(nodeIP + ":" + procID + " [ERROR] System WaitToExec failed to trigger a current crash fault!!!" + ", ioId: " + ioID + ", "
				+ fuzzCommand + ", " + callstack);
	}

    public static void crashCurNode() {
        Runtime.getRuntime().halt(-1);
    }

   
}
