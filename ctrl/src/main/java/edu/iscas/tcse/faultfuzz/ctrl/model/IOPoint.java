package edu.iscas.tcse.faultfuzz.ctrl.model;

import java.util.ArrayList;
import java.util.List;

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

	public static IOPoint praseFromString(String str) {
		String ioIDPrefix = "IOID=[";
		String ipPrefix = "], IOIP=[";
		String appearIdxPrefix = "], AppearIdx=[";
		String fwhIndexPrefix = "], FwhIndex=[";
		String CALLSTACKPrefix = "], CallStack=";
		String PathPrefix = ", Path=";
		String ioIDStr = str.substring(str.indexOf(ioIDPrefix) + ioIDPrefix.length(), str.indexOf(ipPrefix));
		int ioID = Integer.parseInt(ioIDStr);
		String ipStr = str.substring(str.indexOf(ipPrefix) + ipPrefix.length(), str.indexOf(appearIdxPrefix));
		String appearIdxStr = str.substring(str.indexOf(appearIdxPrefix) + appearIdxPrefix.length(), str.indexOf(fwhIndexPrefix));
		int appearIdx = Integer.parseInt(appearIdxStr);
		String fwhIndexStr = str.substring(str.indexOf(fwhIndexPrefix) + fwhIndexPrefix.length(), str.indexOf(CALLSTACKPrefix));
		int fwhIndex = Integer.parseInt(fwhIndexStr);
		String callStackStr = str.substring(str.indexOf(CALLSTACKPrefix) + CALLSTACKPrefix.length(), str.indexOf(PathPrefix));
		callStackStr = callStackStr.substring(1, callStackStr.length() - 1);
		List<String> callStack = new ArrayList<>();
		for (String s : callStackStr.split(",")) {
			callStack.add(s.trim());
		}
		String pathStr = str.substring(str.indexOf(PathPrefix) + PathPrefix.length());
		IOPoint ioPoint= new IOPoint();
		ioPoint.ioID = ioID;
		ioPoint.ip = ipStr;
		ioPoint.appearIdx = appearIdx;
		ioPoint.fwhIndex = fwhIndex;
		ioPoint.CALLSTACK = callStack;
		ioPoint.PATH = pathStr;
		return ioPoint;
	}

	
	public List<String> retrieveTotalInformationAboutMsgFromPath() {
		// List<String> result = extractInformationFromPath(PATH);
		List<String> result = tansformPathToStrList(PATH, ip);
		return result;
	}

	public static List<String> tansformPathToStrList(String path, String reportIP) {
		List<String> result = new ArrayList<>();
		result = extractInformationFromPath(path);
		if (result.get(0).equals("write")) {
			result.add(1, reportIP);
		}
		if (result.get(0).equals("read")) {
			result.add(2, reportIP);
		}
		return result;
	}

	private static List<String> extractInformationFromPath(String path) {
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
