package edu.iscas.tcse.favtrigger.triggering;

import java.util.List;

public class IOPoint {
	int ioID;
	int appearIdx;

	public long TIMESTAMP;
	public long THREADID;
	public int THREADOBJ;
	public String PATH;
    public List<String> CALLSTACK;
    public String procID;
    public String ip;
	public String toString() {
		return "IOID=["+CALLSTACK.hashCode()+"]"+", AppearIdx=["+appearIdx+"], "+", CallStack="+CALLSTACK;
	}
}
