package edu.iscas.tcse.ZKCases.generator;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import edu.iscas.tcse.faultfuzz.ctrl.Generator;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;

public class ZKClient {
    public ZooKeeper zk;
    public Generator g = new Generator();

    public void connect() throws IOException {
        String quorumServers = "127.0.0.1:12050";
        int sessionTimeout = 15000;
        
        zk = new ZooKeeper(quorumServers, sessionTimeout, null);
    }

    public void create() throws KeeperException, InterruptedException {
        Stat.log("create znode /bug");
        zk.create("/bug", "hello".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    public void delete() throws InterruptedException, KeeperException {
        Stat.log("delete znode /bug");
        zk.delete("/bug", -1);
    }

    public void setData() throws InterruptedException, KeeperException {
        Stat.log("set data of znode /bug to world");
        zk.setData("/bug", "world".getBytes(), -1);
    }

    public void setData(String data) throws InterruptedException, KeeperException {
        Stat.log("set data of znode /bug to world");
        zk.setData("/bug", data.getBytes(), -1);
    }

    public void getData() throws InterruptedException, KeeperException {
        Stat.log("get data of znode /bug");
        byte[] bytes = zk.getData("/bug", false, null);
        System.out.println(new String(bytes));
    }
}
