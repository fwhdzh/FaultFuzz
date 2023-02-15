package edu.iscas.tcse.favtrigger.tracing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.columbia.cs.psl.phosphor.runtime.Taint;

public class FAVEntry implements Serializable {
	public static enum EntryLabel {
		TIMESTAMP, THREADID, THREADOBJ, PATH, TAINT, CALLSTACK, APPEARIDX
	}
	public long TIMESTAMP;
	public long THREADID;
	public int THREADOBJ;
	public String PATH;
	public Taint TAINT;
    public List<String> CALLSTACK;
    public String procID;
    public String ip;
    public int entryAppearIdx;//start from 1
    public String taintMD5;
    public String recPosition;
    public Set<String> affectedMeta;
    public Set<FAVEntry> relatedEntries = new HashSet<>();
    public int newCovs = 0;

	public FAVEntry(long timestamp, long threadId, int threadObjId, String path, Taint taint, List<String> callstack) {
		super();
		this.TIMESTAMP = timestamp;
		this.THREADID = threadId;
		this.THREADOBJ = threadObjId;
		this.PATH = path;
		this.TAINT = taint;
		this.CALLSTACK = callstack;
	}

	public FAVEntry() {
		this.TIMESTAMP = -1;
		this.THREADID = -1;
		this.THREADOBJ = -1;
		this.PATH = "";
		this.TAINT = Taint.emptyTaint();
		this.CALLSTACK = new ArrayList<String>();
		this.procID = "";
		this.ip = "";
		this.entryAppearIdx = -1;
	}

	public String getUniqueID() {
	    if(this.PATH.contains(FAVPathType.FAVMSG.toString()) && this.PATH.contains("&")) {
	        String path = this.PATH.substring(0, this.PATH.lastIndexOf("&"));
	        return this.ip.hashCode()+" "+path.hashCode()+" "+this.CALLSTACK.toString().hashCode();
	    } else {
	        return this.ip.hashCode()+" "+this.PATH.hashCode()+" "+this.CALLSTACK.toString().hashCode();
	    }
	}

	public String getUniqueThreadID() {
		return THREADID+":"+THREADOBJ;
	}

	public String toString() {
		if(TAINT != null) {
			return "[Path="+PATH+", Taint="+TAINT.toString()+", CallStack="+CALLSTACK+"]";
		} else {
			return "[Path="+PATH+", CallStack="+CALLSTACK+"]";
		}
	}

	public String noTaintString() {
		return "[Path="+PATH+", CallStack="+CALLSTACK+"]";
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		int res = 17;
		res = 31 * res + (int)(TIMESTAMP^(TIMESTAMP>>>32));
		res = 31 * res + (int)(THREADID^(THREADID>>>32));
		res = 31 * res + THREADOBJ;
		res = 31 * res + (ip == null? 0:ip.hashCode());
		res = 31 * res + (procID == null? 0:procID.hashCode());
		res = 31 * res + (PATH == null? 0:PATH.hashCode());
		//res = 31 * res + (TAINT == null? 0:TAINT.hashCode());
		res = 31 * res + (CALLSTACK == null? 0:CALLSTACK.toString().hashCode());
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof FAVEntry) {
			FAVEntry e = (FAVEntry) obj;
			return this.ip.equals(e.ip) && this.procID.equals(e.procID) &&
			        this.TIMESTAMP == e.TIMESTAMP && this.THREADOBJ == e.THREADOBJ && this.THREADID == e.THREADID
					&& this.PATH.equals(e.PATH) && this.CALLSTACK.toString().equals(e.CALLSTACK.toString())
					&& (((this.TAINT == null || this.TAINT.isEmpty()) && (e.TAINT == null || e.TAINT.isEmpty()))
							|| (this.TAINT != null && e.TAINT != null && this.TAINT.containsOnlyLabels(e.TAINT.getLabels())));
		} else {
			return false;
		}
	}
}
