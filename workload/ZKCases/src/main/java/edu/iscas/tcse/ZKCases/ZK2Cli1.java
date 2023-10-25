package edu.iscas.tcse.ZKCases;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZK2Cli1 {
	static String stopNodeSH;
	static String startNodeSH;
	static String failSH;
	static boolean check = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String connectString = args[0];
		if(args[1].equals("check")) {
			check = true;
		}
		startNodeSH = args[2];
		stopNodeSH = args[3];
		failSH = args[4];
		String testName = "ZK2Cli1";
		
		Logger.log(testName, " start .................");
//		String connectString = "127.0.0.1:2181; 127.0.0.1:2182; 127.0.0.1:2183"; // 会话超时时间
		
		int sessionTimeout = 15000; // 创建zookeeper连接（是异步连接，所以需要让主线程阻塞，在连接成功后，让主线程继续执行）
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(connectString, sessionTimeout, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.log(testName, " cannot connect to zookeeper, return.");
			return;
		}
		Logger.log(testName, " build connection with zookeeper");
		int tryIdx = 0;
		while(tryIdx < 2) {
			try {
				tryIdx++;
				zk.create("/bug", "hello".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				Logger.log(testName, "created znode /bug hello");
				break;
			} catch (KeeperException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(e instanceof ConnectionLossException) {
					if(tryIdx == 1) {
						Logger.log(testName, "got ConnectionLossException, try create /bug hello again");
					} else {
						Logger.log(testName, "ConnectionLossException when create /bug hello, return!");
						return;
					}
				} else {
					if(check) {
						String info = Logger.log(testName, "create /bug hello failed! return.");
						reportFailure(info);
						return;
					}
				}
			}
		}
		try {
			zk.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
