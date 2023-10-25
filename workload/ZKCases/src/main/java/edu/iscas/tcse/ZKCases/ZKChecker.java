package edu.iscas.tcse.ZKCases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

import org.apache.zookeeper.data.Stat;

public class ZKChecker {
	static String stopNodeSH;
	static String startNodeSH;
	static String failSH;
	static boolean check = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String connectString = args[0];
		failSH = args[1];
		String testName = "ZKChecker";
		
		Logger.log(testName, " start .................");
//		String connectString = "127.0.0.1:2181, 127.0.0.1:2182, 127.0.0.1:2183"; // 会话超时时间
		
		String[] alives = connectString.split(",");
		
		List<String> leaders = new ArrayList<String>();
		//check server status
		for(String alive:alives) {
			String[] eles = alive.trim().split(":");
			String ip = eles[0].trim();
			int port = Integer.parseInt(eles[1].trim());
			String status = "";
			try {
				status = GetLeader.fourLetterWord(ip, port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				String error = Logger.log(testName, " cannot connect to zookeeper "+alive+".");
				reportFailure(error);
			}
			if(status.equals("leader")) {
				leaders.add(alive);
			}
		}
		if(leaders.size() != 1) {
			String error = Logger.log(testName, " current leader size is not 1, leaders are:"+leaders);
			reportFailure(error);
		}
		
		int sessionTimeout = 15000; // 创建zookeeper连接（是异步连接，所以需要让主线程阻塞，在连接成功后，让主线程继续执行）
		
		HashMap<String,HashMap<String,ZNodeInfo>> serverToDataInfo = new HashMap<String,HashMap<String,ZNodeInfo>>();
		for(String alive:alives) {
			alive = alive.trim();
			try {
				ZooKeeper zk = new ZooKeeper(alive, sessionTimeout, null);
				HashMap<String,ZNodeInfo> keyToZNode = new HashMap<String,ZNodeInfo>();
				getAllZNodes("/", zk, keyToZNode);
				serverToDataInfo.put(alive, keyToZNode);
				zk.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				String error = Logger.log(testName, " cannot connect to zookeeper "+alive+" return.");
				reportFailure(error);
				return;
			} catch (KeeperException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				String error = Logger.log(testName, " KeeperException when get children / to zookeeper "+alive+" return.");
				reportFailure(error);
				return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(serverToDataInfo.keySet().size()<alives.length) {
			String error = Logger.log(testName, " collected data tree info is less than alive nodes "+alives.length+", return.");
			reportFailure(error);
			return;
		}
		List<String> servers = new ArrayList<String>(serverToDataInfo.keySet());
		String base = servers.get(0);
		HashMap<String,ZNodeInfo> baseInfo = serverToDataInfo.get(base);
		for(int i = 1; i< servers.size(); i++) {
			String compare = servers.get(i);
			HashMap<String,ZNodeInfo> compareInfo = serverToDataInfo.get(compare);
			if(baseInfo.keySet().size() != compareInfo.keySet().size()) {
				String error = Logger.log(testName,
						"server "+base+" and server "+compare+" have different number of znodes:"
						+baseInfo.keySet()+"  "+compareInfo.keySet());
				reportFailure(error);
				return;
			}
			
			for(String path:baseInfo.keySet()) {
				ZNodeInfo basenode = baseInfo.get(path);
				ZNodeInfo comparenode = compareInfo.get(path);
				if(basenode == null || comparenode == null) {
					String error = Logger.log(testName,
							"server "+base+" and server "+compare+" have different znode "+path+":"
							+(basenode == null? base+" is null":"")
							+"   "
							+(comparenode == null? compare+" is null":""));
					reportFailure(error);
//					return;
				}
				if(basenode.toString().contains("ERROR")) {
					String error = Logger.log(testName,
							"server "+base+" have ERROR info:"+basenode.toString());
					reportFailure(error);
//					return;
				}
				if(comparenode.toString().contains("ERROR")) {
					String error = Logger.log(testName,
							"server "+compare+" have ERROR info:"+comparenode.toString());
					reportFailure(error);
//					return;
				}
				if(!comparenode.toString().equals(basenode.toString())) {
					String error = Logger.log(testName,
							"server "+base+" and server "+compare+" have different znode on path "+path+":"
							+base+" is:"+ basenode.toString()
							+"   "
							+compare+" is:"+ comparenode.toString());
					reportFailure(error);
//					return;
				}
			}
		}
	}
	public static class ZNodeInfo{
		public String value;
		public String path;
		public Stat stat;
		public String toString() {
			return path+" "+value+" "+stat.toString();
		}
	}
	public static void getAllZNodes(String path, ZooKeeper zk, HashMap<String,ZNodeInfo> keyToZNode) throws KeeperException, InterruptedException {
		List<String> list = zk.getChildren(path, null);
		if(list == null || list.isEmpty()) {
			return;
		}
		for(String s:list) {
			if(path.equals("/")) {
				Stat stat = new Stat();
				byte[] data = zk.getData(path+s, null, stat);
				ZNodeInfo info = new ZNodeInfo();
				info.path = path+s;
				info.value = (new String(data));
				info.stat = stat;
				keyToZNode.put(info.path, info);
				getAllZNodes(info.path, zk, keyToZNode);
			} else {
				Stat stat = new Stat();
				byte[] data = zk.getData(path+"/"+s, null, stat);
				ZNodeInfo info = new ZNodeInfo();
				info.path = path+"/"+s;
				info.value = (new String(data));
				info.stat = stat;
				if(info.path.equals("/zookeeper/config")) {
					continue;
				}
				keyToZNode.put(info.path, info);
				getAllZNodes(info.path, zk, keyToZNode);
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
