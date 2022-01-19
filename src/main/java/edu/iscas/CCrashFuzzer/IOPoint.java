package edu.iscas.CCrashFuzzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		return "IOID=["+ioID+"]"+", AppearIdx=["+appearIdx+"], "+", CallStack="+CALLSTACK;
	}
}
