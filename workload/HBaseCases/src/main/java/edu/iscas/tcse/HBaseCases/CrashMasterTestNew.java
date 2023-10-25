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
public class CrashMasterTestNew {
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
		String testName = "CrashMasterTestNew";
		try {
			Logger.log(testName, " start .................");
			Logger.log(testName, " going to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
			Logger.log(testName, "!!!!!!!!!!!CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");
			
			//create table
			// | Person |
			List<String> columnFamilies = new ArrayList<>();
			String tName = MyTableSchema.myTable;
			columnFamilies.add(MyTableSchema.colunmFamilies[0]);
			boolean rst = HBaseUtils.createTable(tName, columnFamilies);
			if(!rst) {
				String info = Logger.log(testName, " created table "+MyTableSchema.myTable+" failed!");
				reportFailure(info);
				return;
			} else {
				Logger.log(testName, " created table "+MyTableSchema.myTable+" with 0 Personal colunm.");
			}
			
			ServerName master = HBaseUtils.getMaster();
			HBaseUtils.stopMaster();
			Logger.log(testName, " stopped master "+master.getAddress().toString());
			
			//  | Person      |
			//0 | Mary{Name}  |
			boolean putRow0 = HBaseUtils.putRow(tName, 
					Integer.toString(0), 
					MyTableSchema.colunmFamilies[0], 
					MyTableSchema.qualifiers[0][0], 
					"Mary");
			if(!putRow0) {
				String info = Logger.log(testName, "put row 0 Mary to table "+MyTableSchema.myTable+" failed!");
				reportFailure(info);
			} else {
				Logger.log(testName, "put row 0 Mary to table "+MyTableSchema.myTable);
			}
			
			//check table data
			Table t = HBaseUtils.getTable(MyTableSchema.myTable);
			Logger.log(testName, " get table "+MyTableSchema.myTable+": "+t);
			if(t == null) {
				String info = Logger.log(testName, " the talbe "+MyTableSchema.myTable+" does not exist after a series of modifications");
				reportFailure(info);
			} else {
				Get row0 = new Get(Bytes.toBytes("0"));
				row0 = row0.readAllVersions();
				Result row0Rst = null;
				try {
					row0Rst = t.get(row0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Logger.log(testName, " get row 0 in table "+MyTableSchema.myTable+": "+row0Rst);
				if((row0Rst == null || row0Rst.isEmpty()) && putRow0) {
					String info = Logger.log(testName, " row0 should exist!");
					reportFailure(info);
				} else if (row0Rst != null) {
					List<Cell> col00 = row0Rst.getColumnCells(Bytes.toBytes(MyTableSchema.colunmFamilies[0]), Bytes.toBytes(MyTableSchema.qualifiers[0][0]));
					if((col00 == null || col00.size() != 1) && putRow0) {
						String info = Logger.log(testName, " row0 col00 should not be null and size should be 1:"+(col00 == null?"null":col00.size()));
						reportFailure(info);
					} else if (putRow0){
						String value = Bytes.toString(CellUtil.cloneValue(col00.get(0)));
						if(!value.equals("Mary")) {
							String info = Logger.log(testName, " row0 col00's lastest value should be 'Mary':"+value);
							reportFailure(info);
						}
					}
				}
			}
//			boolean disablerst = HBaseUtils.disableTable(MyTableSchema.myTable, false);
//			if(!disablerst) {
//				String info = Logger.log(testName, " disable table failed! Return.");
//				reportFailure(info);
//				return;
//			} else {
//				Logger.log(testName, " disabled table "+MyTableSchema.myTable);
//			}
////        	System.out.println("Is table disabled:"+tname+", "+disabled);
//			boolean trucated = HBaseUtils.truncateTable(MyTableSchema.myTable, true);
//			if(!trucated) {
//				String info = Logger.log(testName, " truncate table failed!");
//				reportFailure(info);
//			} else {
//				Logger.log(testName, " truncate table "+MyTableSchema.myTable+", "+trucated);
//			}
			
			boolean disablerst = HBaseUtils.disableTable(MyTableSchema.myTable, false);
			if(!disablerst) {
				String info = Logger.log(testName, " disable table 2nd failed! Return.");
				reportFailure(info);
				return;
			} else {
				Logger.log(testName, " disabled table 2nd "+MyTableSchema.myTable);
			}
			//delete table
			if(disablerst) {
				boolean deleted = HBaseUtils.deleteTable(MyTableSchema.myTable);
				if(!deleted) {
					String info = Logger.log(testName, " the talbe "+MyTableSchema.myTable+" was not deleted!");
					reportFailure(info);
				} else {
					Logger.log(testName, " deleted table "+MyTableSchema.myTable);
				}
			}
			
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
			return RunCommand.run(path+" \""+info+"\"", workingDir);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}
}
