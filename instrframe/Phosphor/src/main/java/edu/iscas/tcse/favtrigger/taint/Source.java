package edu.iscas.tcse.favtrigger.taint;

import java.io.Serializable;

public class Source extends SimplifiedSource implements Serializable {
	public enum FAVTagType {
		APP, JRE
	}

	private static final long serialVersionUID = 8396858496479570423L;
	//public int ID;
	//private int nodeID = -1;
	private String interalCName = "";
	private String mName = "";
	private String desc = "";
	//private int lineNum = -1;
	//private String vName = "";
	//private String callSite = "";
	//private long requestTime;
	private String taintType;
	//private long taintID;
	//private Object tag;
	//private String tagType;  //discard in the future
	public String linkSource; //where the data read from

	public Source(long tagID, String cName, String mName, String desc, String taintType, String linkSource) {
		//this.tagType = tagType;
		super(tagID);
		//this.nodeID = nodeID;
		this.interalCName = cName;
		this.mName = mName;
		this.desc = desc;
		this.taintType = taintType;
		//this.taintID = tagID;
		//this.tag = (Object) this.hashString();
		this.linkSource = linkSource;
	}

	public Source(String ip, String procId, long tagID, String cName, String mName, String desc, String taintType, String linkSource) {
        //this.tagType = tagType;
        super(ip, procId, tagID);
        //this.nodeID = nodeID;
        this.interalCName = cName;
        this.mName = mName;
        this.desc = desc;
        this.taintType = taintType;
        //this.taintID = tagID;
        //this.tag = (Object) this.hashString();
        this.linkSource = linkSource;
    }

	public Source(long tagID) {
		super(tagID);
	}

	@Override
	public String hashString() {
		String total = super.hashString()+"*"+this.interalCName+"*"+this.mName+"*"+this.desc
				+"*"+this.taintType+"*"+this.linkSource;
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