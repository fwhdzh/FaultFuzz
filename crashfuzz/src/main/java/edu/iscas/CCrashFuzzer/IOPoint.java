package edu.iscas.CCrashFuzzer;

import java.util.ArrayList;
import java.util.List;

import edu.iscas.CCrashFuzzer.FaultSequence.FaultPos;

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

	
	public List<String> getTotalInformationAboutMsgFromPath() {
		List<String> result = new ArrayList<>();
		if (!PATH.startsWith("FAVMSG")){
			return null;
		}
		// if the path is "FAVMSG:172.30.0.1&3#1"
		if (PATH.startsWith("FAVMSG") && (!PATH.startsWith("FAVMSG:READ"))) {
			result.add("WRITE");
			result.add(ip);
			result.add(PATH.substring("FAVMSG:".length()).split("&")[0]);
			result.add(PATH.split("&")[1]);
		}
		// if the path is "FAVMSG:READ172.30.0.1&3#1"
		if (PATH.startsWith("FAVMSG:READ")) {
			result.add("READ");
			Stat.debug(PATH);
			result.add(PATH.substring("FAVMSG:READ".length()).split("&")[0]);
			result.add(ip);
			result.add(PATH.split("&")[1]);
		}
		return result;
	}
}
