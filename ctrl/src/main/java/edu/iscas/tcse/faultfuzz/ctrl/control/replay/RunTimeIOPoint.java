package edu.iscas.tcse.faultfuzz.ctrl.control.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayController.ReplayCilentHandler;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;

public class RunTimeIOPoint {
	public int ioID;
	public String reportNodeIp;
	public String cliId;
	public String path;
	public ReplayCilentHandler cilentHander;

	public RunTimeIOPoint(int ioID, String reportNodeIp, String cliId, String path,
			ReplayCilentHandler cilentHander) {
		this.ioID = ioID;
		this.reportNodeIp = reportNodeIp;
		this.cliId = cliId;
		this.path = path;
		this.cilentHander = cilentHander;
	}

	public RunTimeIOPoint(int ioID, String reportNodeIp, String path) {
		this.ioID = ioID;
		this.reportNodeIp = reportNodeIp;
		this.path = path;
	}

	public static void recordRunTimeIOPointList(List<RunTimeIOPoint> fpbList, String filepath) {
		String message = JSONObject.toJSONString(fpbList);
		FileOutputStream out;
		try {
			out = new FileOutputStream(filepath, false);
			out.write(message.getBytes());
			out.write("\n".getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<RunTimeIOPoint> recoverRunTimeIOPointList(String filepath) {
		List<RunTimeIOPoint> result = new ArrayList<>();
		File file = new File(filepath);
		List<String> oriList;
		try {
			oriList = Files.readAllLines(file.toPath());
			String s = oriList.get(0);
			List<RunTimeIOPoint> c = JSON.parseArray(s, RunTimeIOPoint.class);
			Stat.log(JSONObject.toJSONString(c));
			result = c;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean equalInPathInformation(String reportNode1, String path1, String reportNode2, String path2) {
		boolean result = false;
		List<String> sList1 = IOPoint.tansformPathToStrList(path1, reportNode1);
		List<String> sList2 = IOPoint.tansformPathToStrList(path2, reportNode2);
		if (sList1.size() != sList2.size()) {
			return false;
		}
		switch (sList1.get(0)) {
			case "not msg":
				// result = sList1.get(1).equals(sList2.get(1));
				result = true;
				break;
			case "write":
			case "read":
				if (!sList1.get(1).equals(sList2.get(1))) {
					result = false;
					break;
				}
				if (!sList1.get(2).equals(sList2.get(2))) {
					result = false;
					break;
				}
				// String nodeMsgIndex1 = sList1.get(2).split("#")[1];
				// String nodeMsgIndex2 = sList2.get(2).split("#")[1];
				// result = nodeMsgIndex1.equals(nodeMsgIndex2);

				result = true;
				break;
		}
		return result;
	}

}