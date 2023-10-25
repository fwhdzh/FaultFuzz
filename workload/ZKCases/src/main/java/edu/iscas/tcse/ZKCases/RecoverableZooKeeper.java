package edu.iscas.tcse.ZKCases;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

public class RecoverableZooKeeper {
	  private ZooKeeper zk;
	  private final int retryCounter;
	  // An identifier of this process in the cluster
	  private final Watcher watcher;
	  private final int sessionTimeout;
	  private final String quorumServers;
	  private final String testname;
	  
	  public RecoverableZooKeeper(String testname, String quorumServers, int sessionTimeout,
		      Watcher watcher)
		    throws IOException {
		    // TODO: Add support for zk 'chroot'; we don't add it to the quorumServers String as we should.
		    this.retryCounter = 10;

		    this.testname = testname;
		    Logger.log(testname, "Process identifier={"+testname+"} connecting to ZooKeeper ensemble={"+quorumServers+"}");


		    this.watcher = watcher;
		    this.sessionTimeout = sessionTimeout;
		    this.quorumServers = quorumServers;

		    try {
		      checkZk();
		    } catch (Exception x) {
		      /* ignore */
		    }
		  }
	  
	  /**
	   * Try to create a ZooKeeper connection. Turns any exception encountered into a
	   * KeeperException.OperationTimeoutException so it can retried.
	   * @return The created ZooKeeper connection object
	   * @throws KeeperException if a ZooKeeper operation fails
	   */
	  protected synchronized ZooKeeper checkZk() throws KeeperException {
	    if (this.zk == null) {
	      try {
	        this.zk = new ZooKeeper(quorumServers, sessionTimeout, watcher);
	      } catch (IOException ex) {
	    	  Logger.log(testname, "Unable to create ZooKeeper Connection"+ex);
	        throw new KeeperException.OperationTimeoutException();
	      }
	    }
	    return zk;
	  }
	  
	  /**
	   * delete is an idempotent operation. Retry before throwing exception.
	   * This function will not throw NoNodeException if the path does not
	   * exist.
	   */
	  public void delete(String path, int version) throws InterruptedException, KeeperException {
	      int curRetry = retryCounter;
	      boolean isRetry = false; // False for first attempt, true for all retries.
	      while (true) {
	    	  curRetry--;
	        try {
	          checkZk().delete(path, version);
	          return;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case NONODE:
	              if (isRetry) {
	            	  Logger.log(testname, "Node " + path + " already deleted. Assuming a " +
	                    "previous attempt succeeded.");
	                return;
	              }
	              Logger.log(testname, "Node {"+path+"} already deleted, retry={"+isRetry+"}");
	              throw e;

	            case CONNECTIONLOSS:
	            case OPERATIONTIMEOUT:
	            case REQUESTTIMEOUT:
	              retryOrThrow(curRetry, e, "delete");
	              break;

	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	        isRetry = true;
	      }
	  }

	  /**
	   * exists is an idempotent operation. Retry before throwing exception
	   * @return A Stat instance
	   */
	  public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	      while (true) {
	    	  curRetry--;
	        try {
	          Stat nodeStat = checkZk().exists(path, watcher);
	          return nodeStat;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case CONNECTIONLOSS:
	            case OPERATIONTIMEOUT:
	            case REQUESTTIMEOUT:
	              retryOrThrow(curRetry, e, "exists");
	              break;

	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	      }
	  }

	  /**
	   * exists is an idempotent operation. Retry before throwing exception
	   * @return A Stat instance
	   */
	  public Stat exists(String path, boolean watch) throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	      while (true) {
	    	  curRetry--;
	        try {
	          Stat nodeStat = checkZk().exists(path, watch);
	          return nodeStat;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case CONNECTIONLOSS:
	              retryOrThrow(retryCounter, e, "exists");
	              break;
	            case OPERATIONTIMEOUT:
	              retryOrThrow(retryCounter, e, "exists");
	              break;

	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	      }
	  }

	  private void retryOrThrow(int retryCounter, KeeperException e,
	      String opName) throws KeeperException {
	    if (retryCounter <= 0) {
	    	Logger.log(testname, "ZooKeeper {"+opName+"} failed after {"+this.retryCounter+"} attempts");
	      throw e;
	    }
	    Logger.log(testname, "Retry, connectivity issue (JVM Pause?); quorum={"+quorumServers+"},exception={"+e+"}");
	  }

	  /**
	   * getChildren is an idempotent operation. Retry before throwing exception
	   * @return List of children znodes
	   */
	  public List<String> getChildren(String path, Watcher watcher)
	    throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	      while (true) {
	    	  curRetry--;
	        try {
	          List<String> children = checkZk().getChildren(path, watcher);
	          return children;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case CONNECTIONLOSS:
	            case OPERATIONTIMEOUT:
	            case REQUESTTIMEOUT:
	              retryOrThrow(retryCounter, e, "getChildren");
	              break;

	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	      }
	  }

	  /**
	   * getChildren is an idempotent operation. Retry before throwing exception
	   * @return List of children znodes
	   */
	  public List<String> getChildren(String path, boolean watch)
	    throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	      while (true) {
	    	  curRetry--;
	        try {
	          List<String> children = checkZk().getChildren(path, watch);
	          return children;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case CONNECTIONLOSS:
	              retryOrThrow(retryCounter, e, "getChildren");
	              break;
	            case OPERATIONTIMEOUT:
	              retryOrThrow(retryCounter, e, "getChildren");
	              break;

	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	      }
	  }

	  /**
	   * getData is an idempotent operation. Retry before throwing exception
	   * @return Data
	   */
	  public byte[] getData(String path, Watcher watcher, Stat stat)
	    throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	      while (true) {
	    	  curRetry--;
	        try {
	          byte[] revData = checkZk().getData(path, watcher, stat);
	          return revData;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case CONNECTIONLOSS:
	            case OPERATIONTIMEOUT:
	            case REQUESTTIMEOUT:
	              retryOrThrow(retryCounter, e, "getData");
	              break;

	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	      }
	  }

	  /**
	   * getData is an idempotent operation. Retry before throwing exception
	   * @return Data
	   */
	  public byte[] getData(String path, boolean watch, Stat stat)
	    throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	      while (true) {
	    	  curRetry--;
	        try {
	          byte[] revData = checkZk().getData(path, watch, stat);
	          return revData;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case CONNECTIONLOSS:
	              retryOrThrow(retryCounter, e, "getData");
	              break;
	            case OPERATIONTIMEOUT:
	              retryOrThrow(retryCounter, e, "getData");
	              break;

	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	      }
	  }

	  /**
	   * setData is NOT an idempotent operation. Retry may cause BadVersion Exception
	   * Adding an identifier field into the data to check whether
	   * badversion is caused by the result of previous correctly setData
	   * @return Stat instance
	   */
	  public Stat setData(String path, byte[] data, int version)
	    throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	      byte[] newData = data;
	      boolean isRetry = false;
	      long startTime;
	      while (true) {
	    	  curRetry--;
	        try {
	          Stat nodeStat = checkZk().setData(path, newData, version);
	          return nodeStat;
	        } catch (KeeperException e) {
	          switch (e.code()) {
	            case CONNECTIONLOSS:
	            case OPERATIONTIMEOUT:
	            case REQUESTTIMEOUT:
	              retryOrThrow(retryCounter, e, "setData");
	              break;
	            case BADVERSION:
	              if (isRetry) {
	                // try to verify whether the previous setData success or not
	                try{
	                  Stat stat = new Stat();
	                  byte[] revData = checkZk().getData(path, false, stat);
	                  if(Arrays.equals(revData, newData)) {
	                    // the bad version is caused by previous successful setData
	                    return stat;
	                  }
	                } catch(KeeperException keeperException){
	                  // the ZK is not reliable at this moment. just throwing exception
	                  throw keeperException;
	                }
	              }
	            // throw other exceptions and verified bad version exceptions
	            default:
	              throw e;
	          }
	        }
	        Thread.currentThread().sleep(500);
	        isRetry = true;
	      }
	  }

	  /**
	   * <p>
	   * NONSEQUENTIAL create is idempotent operation.
	   * Retry before throwing exceptions.
	   * But this function will not throw the NodeExist exception back to the
	   * application.
	   * </p>
	   * <p>
	   * But SEQUENTIAL is NOT idempotent operation. It is necessary to add
	   * identifier to the path to verify, whether the previous one is successful
	   * or not.
	   * </p>
	   *
	   * @return Path
	   */
	  public String create(String path, byte[] data, List<ACL> acl,
	      CreateMode createMode)
	    throws KeeperException, InterruptedException {
		  byte[] newData = data;
	      switch (createMode) {
	        case EPHEMERAL:
	        case PERSISTENT:
	          return createNonSequential(path, newData, acl, createMode);

	        case EPHEMERAL_SEQUENTIAL:
	        case PERSISTENT_SEQUENTIAL:
	          return createSequential(path, newData, acl, createMode);

	        default:
	          throw new IllegalArgumentException("Unrecognized CreateMode: " +
	              createMode);
	      }
	  }

	  private String createNonSequential(String path, byte[] data, List<ACL> acl,
	      CreateMode createMode) throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	    boolean isRetry = false; // False for first attempt, true for all retries.
	    long startTime;
	    while (true) {
	    	curRetry--;
	      try {
	        String nodePath = checkZk().create(path, data, acl, createMode);
	        return nodePath;
	      } catch (KeeperException e) {
	        switch (e.code()) {
	          case NODEEXISTS:
	            if (isRetry) {
	              // If the connection was lost, there is still a possibility that
	              // we have successfully created the node at our previous attempt,
	              // so we read the node and compare.
	              byte[] currentData = checkZk().getData(path, false, null);
	              if (currentData != null &&
	            		  Arrays.equals(currentData, data)) {
	                // We successfully created a non-sequential node
	                return path;
	              }
	              Logger.log(testname, "Node " + path + " already exists with " +
	                  new String(currentData) + ", could not write " +
	                  new String(data));
	              throw e;
	            }
	            Logger.log(testname, "Node {"+path+"} already exists");
	            throw e;

	          case CONNECTIONLOSS:
	          case OPERATIONTIMEOUT:
	          case REQUESTTIMEOUT:
	            retryOrThrow(retryCounter, e, "create");
	            break;

	          default:
	            throw e;
	        }
	      }
	      Thread.currentThread().sleep(500);
	      isRetry = true;
	    }
	  }

	  private String createSequential(String path, byte[] data,
	      List<ACL> acl, CreateMode createMode)
	    throws KeeperException, InterruptedException {
		  int curRetry = retryCounter;
	    boolean first = true;
	    String newPath = path;
	    while (true) {
	    	curRetry--;
	      try {
	        if (!first) {
	          // Check if we succeeded on a previous attempt
	          String previousResult = findPreviousSequentialNode(newPath);
	          if (previousResult != null) {
	            return previousResult;
	          }
	        }
	        first = false;
	        String nodePath = checkZk().create(newPath, data, acl, createMode);
	        return nodePath;
	      } catch (KeeperException e) {
	        switch (e.code()) {
	          case CONNECTIONLOSS:
	          case OPERATIONTIMEOUT:
	          case REQUESTTIMEOUT:
	            retryOrThrow(retryCounter, e, "create");
	            break;

	          default:
	            throw e;
	        }
	      }
	      Thread.currentThread().sleep(500);
	    }
	  }

	  private String findPreviousSequentialNode(String path)
	    throws KeeperException, InterruptedException {
	    int lastSlashIdx = path.lastIndexOf('/');
	    assert(lastSlashIdx != -1);
	    String parent = path.substring(0, lastSlashIdx);
	    String nodePrefix = path.substring(lastSlashIdx+1);
	    List<String> nodes = checkZk().getChildren(parent, false);
	    List<String> matching = filterByPrefix(nodes, nodePrefix);
	    for (String node : matching) {
	      String nodePath = parent + "/" + node;
	      Stat stat = checkZk().exists(nodePath, false);
	      if (stat != null) {
	        return nodePath;
	      }
	    }
	    return null;
	  }

	  /**
	   * Filters the given node list by the given prefixes.
	   * This method is all-inclusive--if any element in the node list starts
	   * with any of the given prefixes, then it is included in the result.
	   *
	   * @param nodes the nodes to filter
	   * @param prefixes the prefixes to include in the result
	   * @return list of every element that starts with one of the prefixes
	   */
	  private static List<String> filterByPrefix(List<String> nodes,
	      String... prefixes) {
	    List<String> lockChildren = new ArrayList<>();
	    for (String child : nodes){
	      for (String prefix : prefixes){
	        if (child.startsWith(prefix)){
	          lockChildren.add(child);
	          break;
	        }
	      }
	    }
	    return lockChildren;
	  }
	  
	  public synchronized long getSessionId() {
	    return zk == null ? -1 : zk.getSessionId();
	  }

	  public synchronized void close() throws InterruptedException {
	    if (zk != null) {
	      zk.close();
	    }
	  }

	  public synchronized States getState() {
	    return zk == null ? null : zk.getState();
	  }

	  public synchronized ZooKeeper getZooKeeper() {
	    return zk;
	  }

	  public synchronized byte[] getSessionPasswd() {
	    return zk == null ? null : zk.getSessionPasswd();
	  }

	  public void sync(String path, AsyncCallback.VoidCallback cb, Object ctx) throws KeeperException {
	    checkZk().sync(path, cb, ctx);
	  }

}
