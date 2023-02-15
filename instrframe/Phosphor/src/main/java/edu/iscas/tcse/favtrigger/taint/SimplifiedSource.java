package edu.iscas.tcse.favtrigger.taint;

import edu.columbia.cs.psl.phosphor.PreMain;

public class SimplifiedSource {
	public String procId;
	protected final long taintID;
	public String ip;

	public SimplifiedSource(long taintID) {
        super();
        this.taintID = taintID;
        this.ip = PreMain.ip;
        this.procId = PreMain.proc;
    }

	public SimplifiedSource(String ip, String procId, long taintID) {
        super();
        this.taintID = taintID;
        this.ip = ip;
        this.procId = procId;
    }

	public String getNodeID() {
		return procId;
	}

	public long getTaintID() {
		return taintID;
	}

	public String hashString() {
		String total = null;
		total = this.ip+"*"+this.procId+"*"+Long.toString(taintID);
		return total;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return hashString();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof Source) {
			Source o = (Source) obj;
			return (this.hashString().equals(o.hashString()));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.hashString().hashCode();
	}
}
