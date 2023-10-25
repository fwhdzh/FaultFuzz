package edu.iscas.tcse.faultfuzz.ctrl.model;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class FaultPoint {
	public IOPoint ioPt;
	public int ioPtIdx;
	public FaultType type;
	public FaultPos pos;//before or after
	
	// field tarNodeIp will be replcaed by field params since there are some faults that
	// need more than one parameter, such as networkConnect.
	@Deprecated
	public String tarNodeIp;

	/**
	 * Crashfuzz only use this field to determine which node to inject 
	 * in runtime. But not stored in fault sequence. <p>
	 * 
	 * In paricular, if in a test, targerNode is node 1 and actual node is node 2.
	 * After this test, the fault sequence is still thought to inject fault to node 1.
	 * And the actualNode will be reset to null in reset function. 
	 */
	public String actualNodeIp;  //fill at run time
	public int curAppear;


	public List<String> params;

	public FaultPoint() {
	}

	public FaultPoint(IOPoint ioPt, int ioPtIdx, FaultType type, FaultPos pos, String tarNodeIp,
			String actualNodeIp) {
		this.ioPt = ioPt;
		this.ioPtIdx = ioPtIdx;
		this.type = type;
		this.pos = pos;
		this.tarNodeIp = tarNodeIp;
		this.actualNodeIp = actualNodeIp;
	}

	public String toString() {
		return "FaultPoint=[ IOPoint=[" + ioPt.toString() + "]" + ", FaultType=[" + type + "], "
				+ ", FaultPos=[" + pos + "]"
				+ ", tarNodeIp=[" + tarNodeIp + "]"
				+ ", actualNodeIp=[" + actualNodeIp + "]"
				+ " ]";
	}

	public String getFaultInfo() {
		return ioPt.ioID+ioPt.appearIdx+pos.toString()+type.toString();
	}

	public int getFaultID() {
		int result = (ioPt.CALLSTACK+type.toString()+tarNodeIp).hashCode();
		return result;
	}

	public int getFaultIDWithOutIPInfo() {
		int result = (ioPt.CALLSTACK+type.toString()).hashCode();
		return result;
	}

	public String toJSONString() {
		String result = JSONObject.toJSONString(this);
		return result;
	}

	public static FaultPoint parseFromJSONString(String jsonStr) {
		FaultPoint result = JSON.parseObject(jsonStr, FaultPoint.class);
		return result;
	}

}