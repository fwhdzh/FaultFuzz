package edu.iscas.tcse.faultfuzz.ctrl;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultPos;

public class IOPoint {
	public int ioID;
	public int appearIdx;

	public int fwhIndex;

	public long TIMESTAMP;
	public long THREADID;
	public int THREADOBJ;
	public String PATH;
    public List<String> CALLSTACK;
    public String procID;
    public String ip;
    public int newCovs = 0;
    public FaultPos pos;//before or after
	public String toString() {
		return "IOID=[" + ioID + "]" + ", IOIP=[" + ip + "], AppearIdx=[" + appearIdx + "]"
				+ ", FwhIndex=[" + fwhIndex + "]"
				+ ", CallStack=" + CALLSTACK
				+ ", Path=" + PATH;
	}
	public int computeIoID() {
		return CALLSTACK.toString().hashCode();
	}

	
	public List<String> retrieveTotalInformationAboutMsgFromPath() {
		// List<String> result = new ArrayList<>();
		// if (!PATH.startsWith("FAVMSG")){
		// 	return null;
		// }
		// // if the path is "FAVMSG:172.30.0.1&3#1"
		// if (PATH.startsWith("FAVMSG") && (!PATH.startsWith("FAVMSG:READ"))) {
		// 	result.add("WRITE");
		// 	result.add(ip);
		// 	result.add(PATH.substring("FAVMSG:".length()).split("&")[0]);
		// 	result.add(PATH.split("&")[1]);
		// }
		// // if the path is "FAVMSG:READ172.30.0.1&3#1"
		// if (PATH.startsWith("FAVMSG:READ")) {
		// 	result.add("READ");
		// 	Stat.debug(PATH);
		// 	result.add(PATH.substring("FAVMSG:READ".length()).split("&")[0]);
		// 	result.add(ip);
		// 	result.add(PATH.split("&")[1]);
		// }
		// return result;

		List<String> result = extractInformationFromPath(PATH);
		return result;
	}

	public static List<String> extractInformationFromPath(String path) {
		List<String> result = new ArrayList<>();
		String type = "not msg";
		if (path.startsWith("FAVMSG") && (!path.contains("READ"))) {
			type = "write";
		}
		if (path.startsWith("FAVMSG:READ")) {
			type = "read";
		}
		result.add(type);
		if (type.equals("write")) {
			String connectionNode = path.split("&")[0].split(":")[1];
			result.add(connectionNode);
			String msgId = path.split("&")[1];
			result.add(msgId);
		}
		if (type.equals("read")) {
			String sourIP = path.split("&")[0].split("READ")[1];
			result.add(sourIP);
			String msgId = path.split("&")[1];
			result.add(msgId);
		}
		if (type.equals("not msg")) {
			String ioInfo = path;
			result.add(ioInfo);
		}
		return result;
	}
}
