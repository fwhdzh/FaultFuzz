package edu.iscas.tcse.ZKCases;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZK2Cli2 {
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
		String testName = "ZK2Cli2";
		
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
		boolean setData = false;
		while(tryIdx < 2) {
			try {
				tryIdx++;
				zk.setData("/bug", "nice".getBytes(), -1);
				setData =true;
				Logger.log(testName, "set znode /bug nice");
				break;
			} catch (KeeperException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(e instanceof ConnectionLossException) {
					if(tryIdx == 1) {
						Logger.log(testName, "got ConnectionLossException, try set /bug nice again");
					} else {
						Logger.log(testName, "ConnectionLossException when set /bug nice");
					}
				} else {
					if(check) {
						String info = Logger.log(testName, "set /bug nice failed!");
						reportFailure(info);
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
