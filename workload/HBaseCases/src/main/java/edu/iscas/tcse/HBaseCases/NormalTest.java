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
public class NormalTest {
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
		String testName = "NormalTest";
		try {
			Logger.log(testName, " start .................");
			Logger.log(testName, " going to connect hbase ..............");
			HBaseUtils.buildConnect(args[0], args[1]);
			Logger.log(testName, "!!!!!!!!!!!CONNECTION WITH HBASE HAS BEEN BUILT!!!!!!!!!!!!!!!");
			
			//create table
			boolean createtable = MyTableSchema.createBigTable(4);//row start from 0, contains two column families
			Logger.log(testName, " created table "+MyTableSchema.myTable+" with 4 rows.");
			if(check) {
				if(!HBaseUtils.tableExists(MyTableSchema.myTable)) {
					String info = Logger.log(testName, " the talbe "+MyTableSchema.myTable+" does not exist after creating table");
					reportFailure(info);
				}
			}
			if(!createtable) {
				Logger.log(testName, " the table "+MyTableSchema.myTable+" was not created successfully, exit.");
				return;
			}
			
			//add a row
			boolean putrow400 = HBaseUtils.putRow(MyTableSchema.myTable, "4", MyTableSchema.colunmFamilies[0], MyTableSchema.qualifiers[0][0], "new value");
			Logger.log(testName, " put 'new value' to row '4' at table "
			+MyTableSchema.myTable+" at colunm "+MyTableSchema.colunmFamilies[0]
					+", with qualifier "+MyTableSchema.qualifiers[0][0]);
			
			//add a column
			boolean addcol2 = HBaseUtils.addColumnFamily(MyTableSchema.myTable, MyTableSchema.colunmFamilies[2]);
			Logger.log(testName, " add colunm "+MyTableSchema.colunmFamilies[2]
					+" to table "+MyTableSchema.myTable);
			
			//add a qualifier
			boolean putrow420_0 = HBaseUtils.putRow(MyTableSchema.myTable, "4", MyTableSchema.colunmFamilies[2], MyTableSchema.qualifiers[2][0], "I like dogs");
			Logger.log(testName, " put 'I like dogs' to row '4' at table "
					+MyTableSchema.myTable+" at colunm "+MyTableSchema.colunmFamilies[2]
							+", with qualifier "+MyTableSchema.qualifiers[2][0]);
			
			//modify the cell
			boolean putrow420_1 = HBaseUtils.putRow(MyTableSchema.myTable, "4", MyTableSchema.colunmFamilies[2], MyTableSchema.qualifiers[2][0], "I like cats");
			Logger.log(testName, " put 'I like cats' to row '4' at table "
					+MyTableSchema.myTable+" at colunm "+MyTableSchema.colunmFamilies[2]
							+", with qualifier "+MyTableSchema.qualifiers[2][0]);
			
			//delete a column
			boolean deletecol1 = HBaseUtils.deleteColumnFamily(MyTableSchema.myTable, MyTableSchema.colunmFamilies[1]);
			Logger.log(testName, " delete colunm "+MyTableSchema.colunmFamilies[1]+" of table "+MyTableSchema.myTable);
			
			//delete a row
			boolean deleterow3 = HBaseUtils.deleteRow(MyTableSchema.myTable, "3");
			Logger.log(testName, " delete row '3' in table "+MyTableSchema.myTable);
			
			//get a row
			Result rst = HBaseUtils.getRow(MyTableSchema.myTable, Integer.toString(2));
			Logger.log(testName, " get row 2 from table "+MyTableSchema.myTable+": "+rst.toString());
		
			
//			//add colunm asyn
//			HBaseUtils.addColumnFamilyAsync(MyTableSchema.myTable, MyTableSchema.colunmFamilies[1]);
//			Logger.log(testName, " add colunm "+MyTableSchema.colunmFamilies[1]
//					+" to table "+MyTableSchema.myTable);
//			
//			//clone table schema
//			HBaseUtils.cloneTableSchema(MyTableSchema.myTable, "cloned"+MyTableSchema.myTable, true);
//			Logger.log(testName, " created table "+"cloned"+MyTableSchema.myTable+" from talbe "+MyTableSchema.myTable);
//			
//			HBaseUtils.putRow("cloned"+MyTableSchema.myTable, "0", "cloned"+MyTableSchema.colunmFamilies[1], MyTableSchema.qualifiers[1][0], "new value");
//			Logger.log(testName, " put 'new value' to row '0' at table "
//			+"cloned"+MyTableSchema.myTable+" at colunm "+MyTableSchema.colunmFamilies[1]
//					+", with qualifier "+MyTableSchema.qualifiers[1][0]);
//			
//			if(check) {
//				Table t = HBaseUtils.getTable("cloned"+MyTableSchema.myTable);
//				if(t == null) {
//					String info = Logger.log(testName, " the talbe "+"cloned"+MyTableSchema.myTable+" does not exist!");
//					reportFailure(info);
//				} else {
//					
//				}
//			}
			
			//get regions of the table
			List<RegionInfo> regions = HBaseUtils.getRegionsOfATable(MyTableSchema.myTable);
			if(regions == null || regions.isEmpty()) {
				String info = Logger.log(testName, " the table "+MyTableSchema.myTable+"'s regions are null!");
				reportFailure(info);
			} else {
				Logger.log(testName, MyTableSchema.myTable+" contains "+regions.size()+" regions");
				Logger.log(testName, MyTableSchema.myTable+" has region 0 "+regions.get(0).getRegionNameAsString());
				
				//check rs of the region
				Collection<ServerName> servers = HBaseUtils.getRegionServers(true);
				if(servers == null || servers.isEmpty()) {
					String info = Logger.log(testName, " current region servers are null!");
					reportFailure(info);
				}
				
				ServerName rs = getRSFromRegion(servers, regions.get(0));
				if(rs == null) {
					String info = Logger.log(testName, " region 0 "+regions.get(0).getRegionNameAsString()+
							" is not on any region servers!");
					reportFailure(info);
				} else {
					Logger.log(testName, MyTableSchema.myTable+"'s region 0 is on RS "+rs.getAddress().toString());
				}
				
				//move the region to another rs
				HBaseUtils.move(regions.get(0));
				Logger.log(testName, "move region 0 '"+regions.get(0).getRegionNameAsString()+"' to another region server");
				
				//check current rs of the region
				rs = getRSFromRegion(servers, regions.get(0));
				Logger.log(testName, MyTableSchema.myTable+"'s region 0 is on RS "+rs.getAddress().toString());
			}

			if(check) {
				Logger.log(testName, "Let's check data for table "+MyTableSchema.myTable);
				Table t = HBaseUtils.getTable(MyTableSchema.myTable);
				if(t == null) {
					String info = Logger.log(testName, " the talbe "+MyTableSchema.myTable+" does not exist after a series of modifications");
					reportFailure(info);
				} else {
					Get row3 = new Get(Bytes.toBytes("3"));
					boolean row3Rst = true;
					try {
						row3Rst = t.exists(row3);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(row3Rst && deleterow3) {
						String info = Logger.log(testName, " row3 should have been deleted!");
						reportFailure(info);
					}
					Get row4 = new Get(Bytes.toBytes("4"));
					row4 = row4.readAllVersions();
					Result row4Rst = null;
					try {
						row4Rst = t.get(row4);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if((row4Rst == null || row4Rst.isEmpty()) && (putrow420_0 || putrow420_1 || putrow400)) {
						String info = Logger.log(testName, " row4 should exist!");
						reportFailure(info);
					} else if (row4Rst != null) {
						List<Cell> col00 = row4Rst.getColumnCells(Bytes.toBytes(MyTableSchema.colunmFamilies[0]), Bytes.toBytes(MyTableSchema.qualifiers[0][0]));
						List<Cell> col01 = row4Rst.getColumnCells(Bytes.toBytes(MyTableSchema.colunmFamilies[0]), Bytes.toBytes(MyTableSchema.qualifiers[0][1]));
						List<Cell> col10 = row4Rst.getColumnCells(Bytes.toBytes(MyTableSchema.colunmFamilies[1]), Bytes.toBytes(MyTableSchema.qualifiers[1][0]));
						List<Cell> col20 = row4Rst.getColumnCells(Bytes.toBytes(MyTableSchema.colunmFamilies[2]), Bytes.toBytes(MyTableSchema.qualifiers[2][0]));
						if(col01 != null && col01.size()>0) {
							String info = Logger.log(testName, " row4 col01 should be null:"+col01.size()+", "+col01.get(0).toString());
							reportFailure(info);
						}
						if(col10 != null && col10.size()>0) {
							String info = Logger.log(testName, " row4 col10 should be null:"+col10.size()+", "+col10.get(0).toString());
							reportFailure(info);
						}
						if(col00 == null || col00.size() != 1) {
							String info = Logger.log(testName, " row4 col00 should not be null and size should be 1:"+col00);
							reportFailure(info);
						} else {
							String value = Bytes.toString(CellUtil.cloneValue(col00.get(0)));
							String cell = HBaseUtils.getCell(MyTableSchema.myTable, "4", MyTableSchema.colunmFamilies[0], MyTableSchema.qualifiers[0][0]);
							if(!value.equals("new value")) {
								String info = Logger.log(testName, " row4 col00's value should be 'new value':"+value);
								reportFailure(info);
							}
						}
						if((col20 == null || col20.size() != 2) && putrow420_0 && putrow420_1) {
							String info = Logger.log(testName, " row4 col20 should not be null and size should be 2:"+(col20 == null?"null":col20.size()));
							reportFailure(info);
						} else if (putrow420_0 && putrow420_1){
							String value = Bytes.toString(CellUtil.cloneValue(col20.get(0)));
							if(!value.equals("I like cats")) {
								String info = Logger.log(testName, " row4 col20's lastest value should be 'I like cats':"+value);
								reportFailure(info);
							}
							value = Bytes.toString(CellUtil.cloneValue(col20.get(1)));
							if(!value.equals("I like dogs")) {
								String info = Logger.log(testName, " row4 col20's old value should be 'I like dogs':"+value);
								reportFailure(info);
							}
						} else if (putrow420_0 && !putrow420_1) {
							String value = Bytes.toString(CellUtil.cloneValue(col20.get(0)));
							if(!value.equals("I like dogs")) {
								String info = Logger.log(testName, " row4 col20's lastest value should be 'I like dogs':"+value);
								reportFailure(info);
							}
						} else if (!putrow420_0 && putrow420_1) {
							String value = Bytes.toString(CellUtil.cloneValue(col20.get(0)));
							if(!value.equals("I like cats")) {
								String info = Logger.log(testName, " row4 col20's lastest value should be 'I like cats':"+value);
								reportFailure(info);
							}
						}
					}
				}
				Logger.log(testName, "Finish checking data for table "+MyTableSchema.myTable);
			}
			
			//delete table
			MyTableSchema.deleteBigTable();
			Logger.log(testName, " deleted table "+MyTableSchema.myTable);
			if(check) {
				if(HBaseUtils.tableExists(MyTableSchema.myTable)) {
					String info = Logger.log(testName, " the talbe "+MyTableSchema.myTable+" was not deleted!");
					reportFailure(info);
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
