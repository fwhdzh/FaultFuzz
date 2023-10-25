package edu.iscas.tcse.HBaseCases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class CrashMasterTest {
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
		String testName = "CrashMasterTest";
		try {
			Logger.log(testName, " start .................");
			Logger.log(testName, " going to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
			Logger.log(testName, "!!!!!!!!!!!CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");
			boolean createTable = MyTableSchema.createBigTable(4);
			ServerName master = HBaseUtils.getMaster();
			HBaseUtils.stopMaster();
			Logger.log(testName, " stopped master "+master.getAddress().toString());
			if(!createTable) {
				Logger.log(testName, " the table "+MyTableSchema.myTable+" was not created successfully, exit.");
				return;
			}
			Result rst = HBaseUtils.getRow(MyTableSchema.myTable, Integer.toString(2));
			if(rst != null) {
				Logger.log(testName, " get row 2 from table "+MyTableSchema.myTable+": "+rst.toString());
				if(check) {
					boolean cell00 = rst.containsNonEmptyColumn(Bytes.toBytes(MyTableSchema.colunmFamilies[0]), Bytes.toBytes(MyTableSchema.qualifiers[0][0]));
					boolean cell01 = rst.containsNonEmptyColumn(Bytes.toBytes(MyTableSchema.colunmFamilies[0]), Bytes.toBytes(MyTableSchema.qualifiers[0][1]));
					boolean cell10 = rst.containsNonEmptyColumn(Bytes.toBytes(MyTableSchema.colunmFamilies[1]), Bytes.toBytes(MyTableSchema.qualifiers[1][0]));
					boolean cell11 = rst.containsNonEmptyColumn(Bytes.toBytes(MyTableSchema.colunmFamilies[1]), Bytes.toBytes(MyTableSchema.qualifiers[1][1]));
					if(!cell00 || !cell01 || !cell10 || !cell11) {
						String info = Logger.log(testName, " query of row 2 for table "+MyTableSchema.myTable+" has empty cells:"
								+cell00+" "+cell01+" "+cell10+" "+cell11);
						reportFailure(info);
					}
				}
			} else {
				String info = Logger.log(testName, " query of row 2 for table "+MyTableSchema.myTable+" is null!");
				reportFailure(info);
			}
			boolean deleteTable = MyTableSchema.deleteBigTable();

			if(check) {
				boolean t = HBaseUtils.tableExists(MyTableSchema.myTable);
				if(t && deleteTable) {
					String info = Logger.log(testName, " the table "+MyTableSchema.myTable+" was not deleted!");
					reportFailure(info);
				}
			}
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
