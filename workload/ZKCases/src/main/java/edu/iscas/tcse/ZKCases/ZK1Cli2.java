package edu.iscas.tcse.ZKCases;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZK1Cli2 {
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
		String testName = "ZK1Cli2";
		
		Logger.log(testName, " start .................");
//		String connectString = "127.0.0.1:2181; 127.0.0.1:2182; 127.0.0.1:2183"; // 会话超时时间
		
		int sessionTimeout = 15000; // 创建zookeeper连接（是异步连接，所以需要让主线程阻塞，在连接成功后，让主线程继续执行）
		RecoverableZooKeeper zk = null;
		try {
			zk = new RecoverableZooKeeper(testName, connectString, sessionTimeout, null);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			String info = Logger.log(testName, " cannot connect to zookeeper, return.");
			reportFailure(info);
		}
		Logger.log(testName, " build connection with zookeeper");
		try {
			byte[] data = zk.getData("/eph", false, null);
			if(check && data != null) {
				String info = Logger.log(testName, "incorrectly got ephemeral znode /eph created by cli1!");
				reportFailure(info);
			}
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.log(testName, "cannot read ephemeral znode /eph, expected since cli1 was exited");
		}
		
		try {
			byte[] data = zk.getData("/delete", false, null);
			if(check && data != null) {
				String info = Logger.log(testName, "incorrectly got znode /delete created by cli1!");
				reportFailure(info);
			}
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.log(testName, "cannot read znode /delete, expected since it was deleted");
		}
		
		try {
			byte[] data = zk.getData("/bug", false, null);
			String content = "nice";
			if(!(new String(data)).equals(content)) {
				if(check) {
					String info = Logger.log(testName, "content of /bug is not "+content+":"+(new String(data)));
					reportFailure(info);
				}
			}
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String info = Logger.log(testName, "cannot read znode /bug!");
			reportFailure(info);
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
