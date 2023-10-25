package edu.iscas.tcse.HBaseCases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MetaTableAccessor;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.client.TableState;
import org.apache.hadoop.hbase.util.Bytes;

public class ConnectTest {
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
		String testName = "ConnectTest";
		try {
			Logger.log(testName, " start .................");
			Logger.log(testName, " going to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
			Logger.log(testName, "!!!!!!!!!!!CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");
			try {
	            HBaseAdmin admin = (HBaseAdmin) HBaseUtils.connection.getAdmin();
	            Logger.log(testName, " got admin");
	            // 删除表前需要先禁用表
	            List<TableDescriptor> tables = admin.listTableDescriptors();
	            Logger.log(testName, " got tables "+tables);
	            TableDescriptorBuilder tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf("connect"));
	            Logger.log(testName, " got tableDescriptor "+tableDescriptor);
	            List<String> columnFamilies = new ArrayList();
	            columnFamilies.add("CF1");
	            columnFamilies.add("CF2");
	            columnFamilies.forEach(columnFamily -> {
	                ColumnFamilyDescriptorBuilder cfDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(columnFamily));
	                Logger.log(testName, " for "+columnFamily+", got cfDescriptorBuilder");
	                cfDescriptorBuilder.setMaxVersions(1);
	                ColumnFamilyDescriptor familyDescriptor = cfDescriptorBuilder.build();
	                Logger.log(testName, " for "+columnFamily+", build cfDescriptorBuilder");
	                tableDescriptor.setColumnFamily(familyDescriptor);
	                Logger.log(testName, " for "+columnFamily+", set familyDescriptor");
	            });

	            Table metaHTable =  MetaTableAccessor.getMetaHTable(admin.getConnection());
	            Logger.log(testName, " get meta table: "+metaHTable);
	            Get get = new Get(TableName.valueOf("connect").getName()).addColumn(HConstants.TABLE_FAMILY, HConstants.TABLE_STATE_QUALIFIER);
	            Logger.log(testName, " create connect get "+get);
	            Result result = metaHTable.get(get);
	            Logger.log(testName, " meta get result "+result);
	            TableState state = MetaTableAccessor.getTableState(result);
	            Logger.log(testName, " check table state first: "+state);
	            state = MetaTableAccessor.getTableState(admin.getConnection(), TableName.valueOf("connect"));
            	Logger.log(testName, " check table state: "+state);
	            boolean exist = admin.tableExists(TableName.valueOf("connect"));
	            Logger.log(testName, " table exist: "+exist);
	            if (exist) {
	            	admin.disableTable(TableName.valueOf("connect"));
	            	admin.deleteTable(TableName.valueOf("connect"));
	            	Logger.log(testName, " delete table.");
	            }
	            admin.createTable(tableDescriptor.build());
	            Logger.log(testName, " created table "+tableDescriptor);
	            tables = admin.listTableDescriptors();
	            Logger.log(testName, " got tables "+tables);
	            admin.disableTable(TableName.valueOf("connect"));
	            admin.deleteTable(TableName.valueOf("connect"));
            	Logger.log(testName, " delete table.");
	        } catch (Exception e) {
	            e.printStackTrace();
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
}
