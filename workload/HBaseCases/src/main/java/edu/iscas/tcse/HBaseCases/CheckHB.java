package edu.iscas.tcse.HBaseCases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.TableDescriptor;

public class CheckHB {

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
		try {
			System.out.println("Test case1 start .................");
			System.out.println("Goint to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
	        System.out.println("!!!!!!!!!!!CHECK HB CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");
			List<String> columnFamilies = new ArrayList<>();
			String tName = "check";
			String cf1 = "CF1";
			String cf2 = "CF2";
			String cf3 = "CF3";
			columnFamilies.add(cf1);
			columnFamilies.add(cf2);
			columnFamilies.add(cf3);
			HBaseUtils.createTable(tName, columnFamilies);
			
			System.out.println("Test case1 created a table '"+tName+"' with column families:"+columnFamilies);
			
			
			//check#######################
			if(check) {
				List<TableDescriptor> tables = HBaseUtils.listTable();
				if(tables == null || tables.size()!=1) {
					reportFailure("The number of the tables in HBase is not 1 after create table '"+tName+"':"+tables.size());
				}
			}
			
			String row1 = "row1";
			String row2 = "row2";
			String row3 = "row3";
			HBaseUtils.putRow(tName, row1, cf1, "row1-cf1-a", "value1");
			if(check) {
				String rst = HBaseUtils.getCell(tName, row1, cf1, "row1-cf1-a");
				if(rst == null || !rst.equals("value1")) {
					reportFailure(tName+" Put 'row1-cf1-a' failed:"+rst.toString());
				}
			}
			
			HBaseUtils.putRow(tName, row1, cf1, "row1-cf1-b", "value2");
			if(check) {
				String rst = HBaseUtils.getCell(tName, row1, cf1, "row1-cf1-b");
				if(rst == null || !rst.equals("value2")) {
					reportFailure(tName+" Put 'row1-cf1-b' failed:"+rst.toString());
				}
			}
			
			HBaseUtils.putRow(tName, row1, cf2, "row1-cf2-a", "value3");
			if(check) {
				String rst = HBaseUtils.getCell(tName, row1, cf2, "row1-cf2-a");
				if(rst == null || !rst.equals("value3")) {
					reportFailure(tName+" Put 'row1-cf2-a' failed:"+rst.toString());
				}
			}
			
			HBaseUtils.putRow(tName, row2, cf3, "row2-cf3-b", "value4");
			if(check) {
				String rst = HBaseUtils.getCell(tName, row2, cf3, "row2-cf3-b");
				if(rst == null || !rst.equals("value4")) {
					reportFailure(tName+" Put 'row2-cf3-b' failed:"+rst.toString());
				}
			}
			
			String cell = HBaseUtils.getCell(tName, row1, cf1, "row1-cf1-b");
			System.out.println("Test check get cell at table '"+tName+"', row '"+row1+"', column "
					+"'"+cf1+"', key 'row1-cf1-b': "+cell);
			ResultScanner scanner = HBaseUtils.getScanner(tName);
			System.out.println("Test check going to scan table '"+tName+"'");
			Iterator<Result> iter = scanner.iterator();
			while(iter.hasNext()) {
				Result s = iter.next();
				if(s != null && s.current() != null) {
					System.out.println("**********");
					System.out.println("timestamp:"+s.current().getTimestamp());
					System.out.println("RowArray:"+new String(s.current().getRowArray()));
					System.out.println("FamilyArray:"+new String(s.current().getFamilyArray()));
					System.out.println("ValueArray:"+new String(s.current().getValueArray()));
					System.out.println("QualifierArray:"+new String(s.current().getQualifierArray()));
				}
			}
			HBaseUtils.deleteRow(tName, row1);
			cell = HBaseUtils.getCell(tName, row1, cf1, "row1-cf1-b");
			System.out.println("Test check get cell at table '"+tName+"', row '"+row1+"', column "
					+"'"+cf1+"', key 'row1-cf1-b': "+cell);
			HBaseUtils.deleteTable(tName);
			System.out.println("Test check deleted table '"+tName+"'");
			
			if(check) {
				List<TableDescriptor> tables = HBaseUtils.listTable();
				if(tables != null && tables.size()!=0) {
					reportFailure("The table 'test' was not deleted:"+tables.size());
				}
			}
		} finally {
	        if (HBaseUtils.connection != null) {
	            try {
	            	HBaseUtils.connection.close();
	            } catch (IOException e) {
	                System.err.println("error occurs "+e.getMessage());
	            }
	        }
	    }
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
	
	public static ArrayList<String> killNode(String nodeip) {
		if(stopNodeSH != null) {
			String path = stopNodeSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" "+nodeip, workingDir);
			//return RunCommand.run(path+" "+nodeId+" "+nodeName);
		} else {
			return null;
		}
	}
	
	public static ArrayList<String> startNode(String nodeip) {
		if(startNodeSH != null) {
			String path = startNodeSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" "+nodeip, workingDir);
			//return RunCommand.run(path+" "+nodeId+" "+nodeName);
		} else {
			return null;
		}
	}
}
