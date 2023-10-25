package edu.iscas.tcse.HBaseCases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
public class CheckHBase {
	static String stopNodeSH;
	static String startNodeSH;
	static String failSH;
	static boolean check = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args[2].equals("check")) {
			check = true;
		}
		startNodeSH = args[3];
		stopNodeSH = args[4];
		failSH = args[5];
		String testName = "CheckHBase";
		try {
			Logger.log(testName, " start .................");
			Logger.log(testName, " going to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
			Logger.log(testName, "!!!!!!!!!!!CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");

			Collection<ServerName> servers = HBaseUtils.getRegionServers(true);
			for(ServerName rs : servers) {
				List<RegionInfo> rsRegions = HBaseUtils.getRegionsOnARS(rs);
				Logger.log(testName, "region server "+rs.getHostname()+" contains "+rsRegions.size()+" regions");
				for(RegionInfo region:rsRegions) {
					Logger.log(testName, "region server "+rs.getHostname()+" contains region:"+region.isMetaRegion()
					+", "+region.getRegionNameAsString());
				}
			}

			//delete table
//			MyTableSchema.deleteBigTable();
//			Logger.log(testName, " deleted table "+MyTableSchema.myTable);
//			if(check) {
//				if(HBaseUtils.tableExists(MyTableSchema.myTable)) {
//					String info = Logger.log(testName, " the talbe "+MyTableSchema.myTable+" was not deleted!");
//					reportFailure(info);
//				}
//			}
			Logger.log(testName, "exit successfully!");
		} finally {
	        if (HBaseUtils.connection != null) {
	            try {
	            	HBaseUtils.connection.close();
	            } catch (IOException e) {
	            	Logger.log(testName, "error occurs when closing connection "+e.getMessage());
	            }
	        }
	    }
	}
	public static ServerName getRSFromRegion(Collection<ServerName> servers, RegionInfo region) {
		for(ServerName rs : servers) {
			List<RegionInfo> regions = HBaseUtils.getRegionsOnARS(rs);
			for(RegionInfo r:regions) {
				if(r.getRegionId() == region.getRegionId()) {
					return rs;
				}
			}
		}
		return null;
	}
	public static boolean moveRegionToNonMetaRS(String tname) {
		List<RegionInfo> regions = HBaseUtils.getRegionsOfATable(tname);
		Collection<ServerName> servers = HBaseUtils.getRegionServers(true);
		ServerName metaRS = null;
		ServerName tableRS = null;
		ServerName nonMetaRS = null;
		for(ServerName rs : servers) {
			List<RegionInfo> rsRegions = HBaseUtils.getRegionsOnARS(rs);
			for(RegionInfo r:rsRegions) {
				if(r.isMetaRegion()) {
					metaRS = rs;
				} else if(r.getRegionId() == regions.get(0).getRegionId()) {
					tableRS = rs;
				}
			}
		}
		if(metaRS != null && tableRS!= null && metaRS.equals(tableRS)) {
			for(ServerName rs : servers) {
				if(!rs.equals(metaRS)) {
					nonMetaRS = rs;
					break;
				}
			}
			if(nonMetaRS != null) {
				HBaseUtils.move(regions.get(0), nonMetaRS);
				Logger.log("CrashMetaTest", "move table region to region "+nonMetaRS.getHostname());
				return true;
			}
		}
		return false;
	}
	public static ArrayList<String> reportFailure(String info) {
		if(failSH != null) {
			String path = failSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" \""+info+"\"", workingDir);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}
	public static ArrayList<String> stopMeta() {
		if(stopNodeSH != null) {
			String path = stopNodeSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path, workingDir);
			//return RunCommand.run(path+" "+nodeId+" "+nodeName);
		} else {
			return null;
		}
	}
	public static ArrayList<String> startMeta() {
		if(startNodeSH != null) {
			String path = startNodeSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path, workingDir);
			//return RunCommand.run(path+" "+nodeId+" "+nodeName);
		} else {
			return null;
		}
	}
}
