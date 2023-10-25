package edu.iscas.tcse.HBaseCases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.client.Result;

public class CrashRSTest {
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
		String testName = "CrashRSTest";
		try {
			Logger.log(testName, " start .................");
			Logger.log(testName, " going to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
			Logger.log(testName, "!!!!!!!!!!!CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");
			MyTableSchema.createBigTable(4);
			Logger.log(testName, " created table "+MyTableSchema.myTable+" with 4 rows.");
			List<RegionInfo> regions = HBaseUtils.getRegionsOfATable(MyTableSchema.myTable);
			Logger.log(testName, MyTableSchema.myTable+" contains "+regions.size()+" regions");
			Collection<ServerName> servers = HBaseUtils.getRegionServers(true);
			ServerName rs = getRSFromRegion(servers, regions.get(0));
			if(stopNodeSH != null) {
				String path = stopNodeSH;
				String workingDir = path.substring(0, path.lastIndexOf("/"));
				RunCommand.run(path+" "+rs.getAddress().getHostname(), workingDir);
				//return RunCommand.run(path+" "+nodeId+" "+nodeName);
			}
			
//			HBaseUtils.stopRegionServer(rs.getAddress().toString());
			Logger.log(testName, " restart region server:"+rs.getAddress().toString());
			Result rst = HBaseUtils.getRow(MyTableSchema.myTable, Integer.toString(2));
			Logger.log(testName, " get row 2 from table "+MyTableSchema.myTable+": "+rst.toString());regions = HBaseUtils.getRegionsOfATable(MyTableSchema.myTable);
			
			servers = HBaseUtils.getRegionServers(true);
			rs = getRSFromRegion(servers, regions.get(0));
			Logger.log(testName, MyTableSchema.myTable+" contains "+regions.size()+" regions, region 0 is on RS "+rs.getAddress().toString());
			
			if(check) {
				if(rst == null) {
					reportFailure(testName+" get null row 2!");
				}
			}
			MyTableSchema.deleteBigTable();
			Logger.log(testName, " deleted table "+MyTableSchema.myTable);
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
	public static ArrayList<String> reportFailure(String info) {
		if(failSH != null) {
			String path = failSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" "+info, workingDir);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}
}
