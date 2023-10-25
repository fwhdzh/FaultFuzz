package edu.iscas.tcse.faultfuzz.ctrl.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class FaultSequence implements Iterable<FaultPoint> {

	private static FaultSequence empty;

	public static FaultSequence createAnEmptyFaultSequence() {
		FaultSequence result = new FaultSequence();
		result.seq = new ArrayList<FaultPoint>();
		return result;
	}

	public boolean isEmpty() {
		if (empty == null) {
			empty = createAnEmptyFaultSequence();
		}
		return this.seq.size() == 0;
	}

	public FaultSequence() {
		seq = new ArrayList<FaultPoint>();
	}

	public void reset() {
		for(FaultPoint p:seq) {
			p.actualNodeIp = null;
			p.curAppear = 0;
		}
	}
	
	public List<FaultPoint> seq; //only contain the points that inject fault

	@Override
    public Iterator<FaultPoint> iterator() {
        return seq.iterator();
    }

	public int getFaultSeqID() {
		String s = "";
		for(FaultPoint p:seq) {
			s += p.getFaultInfo();
		}
		return s.hashCode();
	}

	public String toJSONString() {
		String result = JSONObject.toJSONString(this);
		return result;
	}

	public FaultSequence fromJSONString(String s) {
		FaultSequence result = JSON.parseObject(s, FaultSequence.class);
		return result;
	}
	
	public String toString() {
		return seq.size()+" faults: "+seq.toString();
	}
}
