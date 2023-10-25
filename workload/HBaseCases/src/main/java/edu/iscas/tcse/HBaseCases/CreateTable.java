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

public class CreateTable {

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
			System.out.println("Test CreateTable start .................");
			System.out.println("Goint to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
	        System.out.println("!!!!!!!!!!!CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");
			List<String> columnFamilies = new ArrayList<>();
			String tName = "test";
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
					reportFailure("The number of the tables in HBase is not 1 after create table 'test':"+tables.size());
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
