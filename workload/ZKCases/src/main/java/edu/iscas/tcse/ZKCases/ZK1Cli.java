package edu.iscas.tcse.ZKCases;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

public class ZK1Cli {
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
		String testName = "ZK1Cli";
		
		Logger.log(testName, " start .................");
//		String connectString = "127.0.0.1:2181; 127.0.0.1:2182; 127.0.0.1:2183"; // 会话超时时间
		
		int sessionTimeout = 15000; // 创建zookeeper连接（是异步连接，所以需要让主线程阻塞，在连接成功后，让主线程继续执行）
//		ZooKeeper zk = null;
//		try {
//			zk = new ZooKeeper(connectString, sessionTimeout, null);
//			Logger.log(testName, " build connection with zookeeper");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Logger.log(testName, " cannot connect to zookeeper, return.");
//			return;
//		}
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
//		stopNode();
		try {
			Logger.log(testName, "going to create znode /bug hello");
			zk.create("/bug", "hello".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			Logger.log(testName, "created znode /bug hello");
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "create /bug hello failed! return.");
				reportFailure(info);
				return;
			}
		}
		
		try {
			Logger.log(testName, "going to create znode /delete hello");
			zk.create("/delete", "hello".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			Logger.log(testName, "created znode /delete hello");
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "create /delete hello failed! return.");
				reportFailure(info);
				return;
			}
		}
		
		try {
			Logger.log(testName, "going to set znode /bug nice");
			zk.setData("/bug", "nice".getBytes(), -1);
			Logger.log(testName, "set znode /bug nice");
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "set /bug nice failed! return.");
				reportFailure(info);
				return;
			}
		}
		
		try {
			Logger.log(testName, "going to read znode /bug");
			byte[] data = zk.getData("/bug", false, null);
			String content = "nice";
			if(!(new String(data)).equals(content)) {
				if(check) {
					String info = Logger.log(testName, "content of /bug is not "+content+":"+(new String(data)));
					reportFailure(info);
				}
			}
			Logger.log(testName, "read znode /bug:"+(new String(data)));
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "read /bug failed!");
				reportFailure(info);
			}
		}
		
		try {
			Logger.log(testName, "going to delete znode /delete ");
			zk.delete("/delete", -1);
			Logger.log(testName, "deleted znode /delete ");
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "delete /bug failed!");
				reportFailure(info);
			}
		}
		
		try {
			Logger.log(testName, "going to create ephemeral znode /eph ephem");
			zk.create("/eph", "ephem".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			Logger.log(testName, "created ephemeral znode /eph ephem");
		} catch (KeeperException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(check) {
				String info = Logger.log(testName, "create ephemeral znode /bug ephem failed!");
				reportFailure(info);
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
	public static ArrayList<String> stopNode() {
		if(stopNodeSH != null) {
			String path = stopNodeSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path, workingDir);
			//return RunCommand.run(path+" "+nodeId+" "+nodeName);
		} else {
			return null;
		}
	}
}
