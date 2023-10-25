package edu.iscas.tcse.HBaseCases;

import java.util.ArrayList;
import java.util.List;

public class MyTableSchema {
	public static String myTable = "FAVMyInfo";
	public static String[] colunmFamilies = 
			new String[]{"Personal", "Office", "Interests"};
	public static String[][] qualifiers = {
			{"Name", "Gender", "Phone"}, 
			{"Position", "Phone", "Address"}, 
			{"Food", "Sports", "Movies"}};
	public static boolean createBigTable(int maxRow) {
		List<String> columnFamilies = new ArrayList<>();
		String tName = myTable;
		columnFamilies.add(colunmFamilies[0]);
		columnFamilies.add(colunmFamilies[1]);
		boolean rst = HBaseUtils.createTable(tName, columnFamilies);
		
		if(!rst) {
			return false;
		}
		
		for(int row = 0; row <maxRow; row ++) {
			for(int co =0; co<2; co++) {
				for(int qualifier = 0; qualifier < 2; qualifier++) {
					rst = HBaseUtils.putRow(tName, 
							Integer.toString(row), 
							colunmFamilies[co], 
							qualifiers[co][qualifier], 
							colunmFamilies[co]+"-"+qualifiers[co][qualifier]+row);
					if(!rst) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static boolean deleteBigTable() {
		return HBaseUtils.deleteTable(myTable);
	}
/*
 *              |     column family - Personal     |  column family - Office |  column family - interests
 *      row key |  Name    |     gender | phone    |Position| phone | address|  food | sports | Movies
 *                 Mary
 *                 John
 *                 Jack
 *                 Alice
 * 
 * 单元(Cell): 每一个 行键，列族和列标识共同组成一个单元，存储在单元里的数据称为单元数据，
 * 单元和单元数据也没有特定的数据类型，以二进制字节来存储。
 * 时间戳(Timestamp): 默认下每一个单元中的数据插入时都会用时间戳来进行版本标识。
 * 读取单元数据时，如果时间戳没有被指定，则默认返回最新的数据，写入新的单元数据时，
 * 如果没有设置时间戳，默认使用当前时间。每一个列族的单元数据的版本数量都被HBase单独维护，
 * 默认情况下HBase保留3个版本数据。
 * 
 */
}
