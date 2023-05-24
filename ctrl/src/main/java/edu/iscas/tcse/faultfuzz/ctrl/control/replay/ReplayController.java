package edu.iscas.tcse.faultfuzz.ctrl.control.replay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;
import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.IOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.Network;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.RunCommand;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflException;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultStat;
import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractController;
import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractDeterminismTarget.FaultSeqAndIOSeq;
import edu.iscas.tcse.faultfuzz.ctrl.control.NormalController.AbortFaultException;
import edu.iscas.tcse.faultfuzz.ctrl.utils.FileUtil;

public class ReplayController 
// extends NormalController 
extends AbstractController
{
	public Thread serverThread;
	public ServerSocket serverSocket;

	public List<MaxDownNodes> currentCluster = new ArrayList<MaxDownNodes>();
	public Network network;

	public Set<ReplayCilentHandler> replayClients;

	public List<FaultPointBlocked> faultPointList;
	public AtomicInteger index;
	public AtomicInteger fIndex;

	// private QueueEntry entry;
	public List<IOPoint> ioSeq;
	public FaultSequence faultSeq;
	

	public boolean arriveAllFaultPoint;

	public List<FaultPointBlocked> arriveFPBList;
	public List<FaultPointBlocked> actualFPBList;
	public int counter;

	public boolean finishFlag;

	public boolean existFaultNotMeet;

	public ReplayController(Cluster cluster, int port, Conf favconfig) {
		super(cluster, port, favconfig);

		currentCluster = MaxDownNodes.cloneCluster(favconfig.maxDownGroup);
		network = Network.constructNetworkFromMaxDOwnNodes(currentCluster);

		replayClients = Collections.synchronizedSet(new HashSet<ReplayCilentHandler>());
		faultPointList = Collections.synchronizedList(new ArrayList<FaultPointBlocked>());
		index = new AtomicInteger(0);
		fIndex = new AtomicInteger(0);
		arriveAllFaultPoint = false;
		arriveFPBList = Collections.synchronizedList(new ArrayList<FaultPointBlocked>());
		actualFPBList = Collections.synchronizedList(new ArrayList<FaultPointBlocked>());
		counter = 0;
		finishFlag = false;

		existFaultNotMeet = false;
	}

	public static class ReplayControllerResult {
		boolean allPointsAreReplayed = false;
		boolean allFaultsAreReplayed = false;
		List<FaultPointBlocked> actualFPBList;
		List<MaxDownNodes> finalCluster;
	}

	public ReplayControllerResult collectReplayResult() {
		ReplayControllerResult result = new ReplayControllerResult();
		result.allPointsAreReplayed = index.get() >= ioSeq.size();
		result.allFaultsAreReplayed = arriveAllFaultPoint;
		result.actualFPBList = actualFPBList;
		result.finalCluster = currentCluster;
		return result;
	}

	public static class FaultPointBlocked {
		public int ioID;
		public String reportNodeIp;
		public String cliId;
		public String path;
		public ReplayCilentHandler cilentHander;

		public FaultPointBlocked(int ioID, String reportNodeIp, String cliId, String path,
				ReplayCilentHandler cilentHander) {
			this.ioID = ioID;
			this.reportNodeIp = reportNodeIp;
			this.cliId = cliId;
			this.path = path;
			this.cilentHander = cilentHander;
		}

		public FaultPointBlocked(int ioID, String reportNodeIp, String path) {
			this.ioID = ioID;
			this.reportNodeIp = reportNodeIp;
			this.path = path;
		}

		public static void recordFPBList(List<FaultPointBlocked> fpbList, String filepath) {
			String message = JSONObject.toJSONString(fpbList);
			FileOutputStream out;
			try {
				out = new FileOutputStream(filepath, false);
				out.write(message.getBytes());
				out.write("\n".getBytes());
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		public static List<FaultPointBlocked> recoverFPBList(String filepath) {
			List<FaultPointBlocked> result = new ArrayList<>();
			File file = new File(filepath);
			List<String> oriList;
			try {
				oriList = Files.readAllLines(file.toPath());
				String s = oriList.get(0);
				List<FaultPointBlocked> c = JSON.parseArray(s, FaultPointBlocked.class);
				Stat.log(JSONObject.toJSONString(c));
				result = c;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Deprecated
		public static boolean equalInPathPreFix(String path1, String path2) {
			// return path1.split("&")[0].equals(path2.split("&")[0]);
			boolean result = false;
			List<String> sList1 = extractInformationFromPath(path1);
			List<String> sList2 = extractInformationFromPath(path2);
			if (sList1.size() != sList2.size()) {
				return false;
			}
			switch (sList1.get(0)) {
				case "not msg":
					result = sList1.get(1).equals(sList2.get(1));
					break;
				case "write":
				case "read":
					if (!sList1.get(1).equals(sList2.get(1))) {
						result = false;
						break;
					}
					// String nodeMsgIndex1 = sList1.get(2).split("#")[1];
					// String nodeMsgIndex2 = sList2.get(2).split("#")[1];
					// result = nodeMsgIndex1.equals(nodeMsgIndex2);

					result = true;
					break;
			}
			return result;
		}

		public static boolean equalInPathInformation(String reportNode1, String path1, String reportNode2, String path2) {
			boolean result = false;
			List<String> sList1 = tansformPathToStrList(path1, reportNode1);
			List<String> sList2 = tansformPathToStrList(path2, reportNode2);
			if (sList1.size() != sList2.size()) {
				return false;
			}
			switch (sList1.get(0)) {
				case "not msg":
					// result = sList1.get(1).equals(sList2.get(1));
					result = true;
					break;
				case "write":
				case "read":
					if (!sList1.get(1).equals(sList2.get(1))) {
						result = false;
						break;
					}
					if (!sList1.get(2).equals(sList2.get(2))) {
						result = false;
						break;
					}
					// String nodeMsgIndex1 = sList1.get(2).split("#")[1];
					// String nodeMsgIndex2 = sList2.get(2).split("#")[1];
					// result = nodeMsgIndex1.equals(nodeMsgIndex2);

					result = true;
					break;
			}
			return result;
		}

		// public boolean equalInPathInDeterministicControl(String path) {
		// 	// return this.path.equals(path);
		// 	// return this.path.split("&")[0].equals(path.split("&")[0]);
		// 	return equalInPathPreFix(this.path, path);
		// }

		// public boolean equalInDeterministicControl(FaultPointBlocked b) {
		// 	if ((this.ioID == b.ioID)
		// 			&& (this.reportNodeIp.equals(b.reportNodeIp))
		// 			&& (equalInPathInDeterministicControl(b.path))) {
		// 		return true;
		// 	}
		// 	return false;
		// }

	}

	public void prepareFaultSeqAndIOSeq(FaultSeqAndIOSeq seqPair) {
		// this.entry = entry;
		this.ioSeq = seqPair.ioSeq;
		this.faultSeq = seqPair.faultSeq;
		seqPair.faultSeq.reset();
		updataCurCrashPointFile(seqPair.faultSeq);
		Stat.log("Current fault sequence was prepared.");

	    arriveAllFaultPoint = faultSeq.isEmpty();
		
	}

	public void updataCurCrashPointFile(FaultSequence faultSequence) {
		if (faultSequence == null || faultSequence.isEmpty()) {
			File file = favconfig.CUR_CRASH_FILE;
			if (file.exists()) {
				file.delete();
				if (favconfig.UPDATE_CRASH != null) {
					String path = favconfig.UPDATE_CRASH.getAbsolutePath();
					String workingDir = path.substring(0, path.lastIndexOf("/"));
					RunCommand.run(path, workingDir);
				}
			}
		} else {
			FileUtil.genereteFaultSequenceFile(faultSequence, favconfig.CUR_CRASH_FILE);

			if (favconfig.UPDATE_CRASH != null) {
				String path = favconfig.UPDATE_CRASH.getAbsolutePath();
				String workingDir = path.substring(0, path.lastIndexOf("/"));
				RunCommand.run(path, workingDir);
			}
		}
	}

	// @Override
	public void startController() {
		startSeverThread();
		startScanThread();
	}

	protected void startSeverThread() {
		running = true;
		counter = 0;
		serverThread = new Thread() {

			@Override
			public void run() {

				// TODO Auto-generated method stub
				try {
					Stat.log("Controller port:" + CONTROLLER_PORT);
					serverSocket = new ServerSocket(CONTROLLER_PORT);
					Stat.log("Controller started ...");

					while (running) {
						while (replayClients.size() > maxClients) {
							Thread.currentThread().sleep(500);
						}
						Socket socket = serverSocket.accept(); // server accept the client connection request
						counter++;
						String s = "Accept a socket!";
						s = s + "assign the socket to ReplayCilentHandler id :" + counter;
						Stat.debug(s);
						// System.out.println("a client "+counter+" was
						// connected"+socket.getRemoteSocketAddress());
						ReplayCilentHandler sct = new ReplayCilentHandler(socket, counter); // send the request to a separate thread
						sct.start();
						replayClients.add(sct);
					}
					serverSocket.close();

				} catch (Exception e) {
					synchronized (faultPointList) {
						e.printStackTrace();
						Stat.log("recieve total socket count: " + counter);
						Stat.log("faultPointList size is : " + faultPointList.size());
						Stat.log("actualFPBList size is : " + actualFPBList.size());
						Stat.log("The left FPBS are : ");
						String s = "";
						for (FaultPointBlocked fpb : faultPointList) {
							s = s + JSONObject.toJSONString(fpb) + "\n";
						}
						Stat.log(s);
					}
					
				}
			}
		};
		serverThread.start();
		Stat.log("controller serverThread has started!");
	}

	protected void startScanThread() {
		ListScanner scanThread = new ListScanner();
		scanThread.start();
		Stat.log("controller scanThread has started!");
	}

	public void stopController() {
		running = false;
		try {
			if(serverThread.isAlive()) {
				serverThread.interrupt();
			}
			for(ReplayCilentHandler t: replayClients) {
				if(t.isAlive()) {
					t.interrupt();
				}
			}
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("exception when stopping controller ...");
			e.printStackTrace();
		}
		File file = favconfig.CUR_CRASH_FILE;
		if(file.exists()) {
			System.out.println("Detete cur crash file.");
			file.delete();
		}
		System.out.println("Controller was stopped.");

		
	}


	public void continueAllFPB() throws IOException, AflException, AbortFaultException {
		for (FaultPointBlocked fpb : faultPointList) {
			fpb.cilentHander.doOperationToCluster(fpb);
		}
	}

	public class ReplayCilentHandler extends Thread {
		final Socket socket;
		final int id;

		public ReplayCilentHandler(Socket socket, int id) {
			this.socket = socket;
			this.id = id;
		}

		@Override
		public void run() {
			try {
				Stat.debug("ReplayCilentHandler start...");
				DataInputStream inStream = new DataInputStream(socket.getInputStream());
				// DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
				// ObjectInputStream objIn = new ObjectInputStream(inStream);

				int ioID = inStream.readInt();
				// Stat.log("ReplayCilentHandler read ioID: " + ioID);
				String reportNodeIp = inStream.readUTF();
				// Stat.log("ReplayCilentHandler read reportNodeIp: " + reportNodeIp);
				String cliID = inStream.readUTF();
				// Stat.log("recieve cliID: " + cliID + " for ioID " + ioID + ", ");
				String path = inStream.readUTF();
				String threadInfo = inStream.readUTF();
				// Stat.log("recieve path: " + path);
				String info = "";
				Stat.debug("" + id + "ReplayCilentHandler read ioID: " + ioID + "\n");
				Stat.debug("" + id + "ReplayCilentHandler read reportNodeIp: " + reportNodeIp + "\n");
				Stat.debug("" + id + "recieve cliID: " + cliID + " for ioID " + ioID + ", " + "\n");
				Stat.debug("" + id + "recieve path: " + path + "\n");
				Stat.debug("" + id + "recieve threadInfo: " + threadInfo + "\n");
				Stat.debug("" + id + "handle information: " + JSONObject.toJSONString(tansformPathToStrList(path, reportNodeIp)));
				// info = info + "ReplayCilentHandler read ioID: " + ioID + "\n";
				// info = info + "ReplayCilentHandler read reportNodeIp: " + reportNodeIp + "\n";
				// info = info + "recieve cliID: " + cliID + " for ioID " + ioID + ", " + "\n";
				// info = info + "recieve path: " + path + "\n";
				// info = info + "recieve threadInfo: " + threadInfo + "\n";
				// info = info + "handle information: " + JSONObject.toJSONString(tansformPathToStrList(path, reportNodeIp));
				// Stat.debug(info);
				FaultPointBlocked b = new FaultPointBlocked(ioID, reportNodeIp, cliID, path, this);
				faultPointList.add(b);
				arriveFPBList.add(b);
				// addOrReplaceFPBtoList(b);
				Stat.debug("For now, faultPointList size is " + faultPointList.size());
				Stat.debug("add a element to faultPointList");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// public int addOrReplaceFPBtoList(FaultPointBlocked b) {
		// 	int result = -1;
		// 	for (int i = 0; i < faultPointList.size(); i++) {
		// 		FaultPointBlocked lb = faultPointList.get(i);
		// 		if (lb.equalInDeterministicControl(b)) {
		// 			faultPointList.set(i, b);
		// 			result = i;
		// 			break;
		// 		}
		// 	}
		// 	if (result == -1) {
		// 		faultPointList.add(b);
		// 	}
		// 	return result;
		// }

		public void replyToNode(DataOutputStream outStream, String command, int curFault, int curAppear)
				throws IOException {
			outStream.writeUTF(command);
			outStream.writeInt(curFault);
			outStream.writeInt(curAppear);
			closeTheSocketConnection();
		}

		public void closeTheSocketConnection() throws IOException {
			socket.getOutputStream().flush();
			// socket.getInputStream().close();
			// socket.getOutputStream().close();
			socket.close();

			replayClients.remove(this);
		}

		// public void addToFaultPointList(int ioID, String reportNodeIp) {
		// FaultPointBlocked b = new FaultPointBlocked(ioID, reportNodeIp, this);
		// faultPointList.add(b);
		// }

		public void doOperationToCluster(FaultPoint p) throws IOException, AflException, AbortFaultException {
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			if (p.stat.equals(FaultStat.CRASH)) {
				replyToNode(outStream, "CRASH", index.get(), p.curAppear);
				String[] args = new String[3];
				args[0] = p.actualNodeIp;
				args[1] = String.valueOf(favconfig.AFL_PORT);
				args[2] = AflCommand.SAVE.toString();
				AflCli.interactWithNode(args);
				rst.add(Stat.log("Prepare to crash node " + p.actualNodeIp));
				List<String> crashRst = cluster.killNode(p.actualNodeIp, p.actualNodeIp);
				rst.addAll(crashRst);
				rst.add(Stat.log("node " + p.actualNodeIp + " was killed!"));
				MaxDownNodes.buildClusterStatus(currentCluster, p.actualNodeIp, FaultStat.CRASH);
			} else if (p.stat.equals(FaultStat.REBOOT)) {
				if (MaxDownNodes.isAliveNode(currentCluster, p.actualNodeIp)) {
					throw new AbortFaultException("Restarting an alive node " + p.actualNodeIp + "!!!");
				}
				// Restart the node
				List<String> restartRst = cluster.restartNode(p.actualNodeIp);
				rst.addAll(restartRst);
				rst.add(Stat.log("node " + p.actualNodeIp + " was restarted!"));
				outStream = new DataOutputStream(socket.getOutputStream());
				replyToNode(outStream, "REBOOT", fIndex.get(), p.curAppear);
				MaxDownNodes.buildClusterStatus(currentCluster, p.actualNodeIp, FaultStat.REBOOT);
			} else if (p.stat.equals(FaultStat.NETWORK_DISCONNECT)) {
				// List<String> msgInfo = p.ioPt.getTotalInformationAboutMsgFromPath();
				// String sourceIp = msgInfo.get(1);
				// String destIp = msgInfo.get(2);
				if (p.params == null || p.params.size() != 2) {
					throw new AflException("NETWORK_DISCONNECT fault should have two parameters!");
				}
				String sourceIp = p.params.get(0);
				String destIp = p.params.get(1);
				List<String> disConnectRst = cluster.networkDisConnect(sourceIp, destIp);
				if (disConnectRst != null) {
					rst.addAll(disConnectRst);
				}
				rst.add(Stat.log("network from  " + sourceIp + " to "+ destIp + " was disconnected!"));
				outStream = new DataOutputStream(socket.getOutputStream());
				replyToNode(outStream, "CONTI", fIndex.get(), p.curAppear);
			} else if (p.stat.equals(FaultStat.NETWORK_CONNECT)) {
				if (p.params == null || p.params.size() != 2) {
					throw new AflException("NETWORK_CONNECT fault should have two parameters!");
				}
				String sourceIp = p.params.get(0);
				String destIp = p.params.get(1);
				List<String> connectRst = cluster.networkConnect(sourceIp, destIp);
				if (connectRst != null) {
					rst.addAll(connectRst);
				}
				rst.add(Stat.log("network from  " + sourceIp + " to "+ destIp + " was connected!"));
				outStream = new DataOutputStream(socket.getOutputStream());
				replyToNode(outStream, "CONTI", fIndex.get(), p.curAppear);
			} else {
				replyToNode(outStream, "CONTI", fIndex.get(), p.curAppear);
			}
		}

		public void doOperationToCluster(IOPoint p) throws IOException, AflException, AbortFaultException {
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			replyToNode(outStream, "CONTI", fIndex.get(), 0);
			Stat.log("reply CONTI for " + p.ioID + " to node " + p.ip + ", path: " + p.PATH);
		}

		public void doOperationToCluster(FaultPointBlocked b) throws IOException, AflException, AbortFaultException {
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			replyToNode(outStream, "CONTI", fIndex.get(), 0);
			Stat.log("reply CONTI for " + b.ioID + " to node " + b.reportNodeIp + ", path: " + b.path);
		}

		// public String getStringOfStat(FaultPoint p) {
		// 	String result = "result";
		// 	switch (p.stat) {
		// 		case CRASH:
		// 			result = "CRASH";
		// 			break;
		// 		case REBOOT:
		// 			result = "REBOOT";
		// 			break;
		// 		case NO:
		// 			result = "NO";
		// 			break;
		// 		default:
		// 			result = "Not in Enum";
		// 			break;
		// 	}
		// 	return result;
		// }

	}

	public class ListScanner extends Thread {

		public long scanInternal = 10;
		public long maxTime = 30000;

		public int result = -1;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Stat.log("ListScanner start...");
			Stat.log("ioList size is: " + ioSeq.size());
			// Stat.log("All the ioIDs are: " + entry.getIoSeqToIDString());
			Stat.debug("All the ioIDs are: " + QueueEntry.getIoSeqToIDString(ioSeq));
			try {
				// boolean timeOut = false;
				// while (!timeOut && index.get() < entry.ioSeq.size()) {
				while (index.get() < ioSeq.size()) {
					IOPoint p = ioSeq.get(index.get());
					Stat.debug("ListScanner next index to check:  " + index.get());
					Stat.log("ListScanner next to wait: " + p.ioID + ", from " + p.ip + ", path: " + p.PATH);
					// long timeCount = 0;
					FaultPointBlocked b = findAndRemoveFPBInList(p);
					while (b == null) {
						sleep(scanInternal);
						b = findAndRemoveFPBInList(p);
					}
					Stat.log("Find FaultPointBlocked! For now, faultPointList size is " + faultPointList.size());

					// if (!arriveAllFaultPoint) {
					// 	updataIOAppearIdxInFaultSeq(entry.faultSeq, fIndex.get(), b.ioID);
					// 	FaultPoint fp = entry.faultSeq.seq.get(fIndex.get());
					// 	if (checkFaultPointMatchIOPoint(fp, p)) {
					// 		fp.actualNodeIp = p.ip;
					// 		Stat.log("A FaultPoint is found! The faultPoint is: " + fp.stat + ", ioID: " + fp.ioPt.ioID
					// 				+ ", ip: " + fp.ioPt.ip + ", path: " + fp.ioPt.PATH);
					// 		b.cilentHander.doOperationToCluster(fp);
					// 		int nf = fIndex.incrementAndGet();
					// 		Stat.log(
					// 				"Next faultPoint index: " + nf + ", faultPoint size: " + entry.faultSeq.seq.size());
					// 		if (nf >= entry.faultSeq.seq.size()) {
					// 			Stat.log("Arrive all FaultPoint!");
					// 			arriveAllFaultPoint = true;
					// 			// break;
					// 		} else {
					// 			Stat.log("Next faultPoint is: " + entry.faultSeq.seq.get(nf).stat + ", ioID: "
					// 					+ entry.faultSeq.seq.get(nf).ioPt.ioID + ", ip: "
					// 					+ entry.faultSeq.seq.get(nf).ioPt.ip + ", path: "
					// 					+ entry.faultSeq.seq.get(nf).ioPt.PATH);
					// 		}
					// 	} else {
					// 		b.cilentHander.doOperationToCluster(p);
					// 	}
					// } else {
					// 	b.cilentHander.doOperationToCluster(p);
					// }

					handleFPB(b);

					index.getAndIncrement();
					actualFPBList.add(b);
				}
				Stat.log("All of IOPoints have been replayed!");
				finishFlag = true;

				// if (timeOut) {
				// result = 1;
				// }
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}

		public void updataIOAppearIdxInFaultSeq(FaultSequence faultSequence, int curFault, int ioID) {
			for (int i = curFault; i < faultSequence.seq.size(); i++) {
				FaultPoint p = faultSequence.seq.get(i);
				if (p.ioPt.ioID == ioID && p.curAppear < p.ioPt.appearIdx) {
					// meet the a fault point, check appear indexes
					p.curAppear++;
					Stat.log("The " + i + "fault point curAppear is updated to " + p.curAppear);
				}
			}
		}

		public boolean checkFaultPointMatchIOPoint(FaultPoint fp, IOPoint io) {
			boolean result = false;
			if (fp.ioPt.ioID == io.ioID && fp.ioPt.appearIdx == io.appearIdx) {
				result = true;
			}
			return result;
		}

		/*
         * It seems we only consider appearIdx of whole cluster, not the appearIdx of each node.
         * It is not reasonable. However, NormalTarget just do this.
         * Need to be improved.
         */
        public boolean checkFaultPointMatchFPBAndAppearIdx(FaultPoint fp, FaultPointBlocked fpb) {
            boolean result = false;
            if (fp.ioPt.ioID == fpb.ioID && fp.ioPt.appearIdx == fp.curAppear) {
                result = true;
            }
            return result;
        }

		public boolean checkFaultPointMatchFPBAndAppearIdxWithCrashFuzzMode(FaultPoint fp, FaultPointBlocked fpb) {
            boolean result = false;
            if (fp.ioPt.ioID == fpb.ioID && fp.ioPt.appearIdx <= fp.curAppear) {
                result = true;
            }
            return result;
        }

		public void handleFPB(FaultPointBlocked b) throws IOException, AflException, AbortFaultException {
            if (!arriveAllFaultPoint) {
                
				updataIOAppearIdxInFaultSeq(faultSeq, fIndex.get(), b.ioID);
                FaultPoint fp = faultSeq.seq.get(fIndex.get());
				

                if (checkFaultPointMatchFPBAndAppearIdx(fp, b)) {
					fp.actualNodeIp = fp.tarNodeIp;
                    // fp.actualNodeIp = b.reportNodeIp;
                    Stat.log("A FaultPoint is found! The faultPoint is: " + fp.stat + ", ioID: " + fp.ioPt.ioID
                            + ", ip: " + fp.ioPt.ip + ", path: " + fp.ioPt.PATH);
                    b.cilentHander.doOperationToCluster(fp);
                    int nf = fIndex.incrementAndGet();
                    Stat.log(
                            "Next faultPoint index: " + nf + ", faultPoint size: " + faultSeq.seq.size());
                    if (nf >= faultSeq.seq.size()) {
                        Stat.log("Arrive all FaultPoint!");
                        arriveAllFaultPoint = true;
                        // break;
                    } else {
                        Stat.log("Next faultPoint is: " + faultSeq.seq.get(nf).stat + ", ioID: "
                                + faultSeq.seq.get(nf).ioPt.ioID + ", ip: "
                                + faultSeq.seq.get(nf).ioPt.ip + ", path: "
                                + faultSeq.seq.get(nf).ioPt.PATH);
                    }
                } else {
                    b.cilentHander.doOperationToCluster(b);
                }
            } else {
                b.cilentHander.doOperationToCluster(b);
            }
        }

	}

	public static List<String> extractInformationFromPath(String path) {
		List<String> result = new ArrayList<>();
		String type = "not msg";
		if (path.startsWith("FAVMSG") && (!path.contains("READ"))) {
			type = "write";
		}
		if (path.startsWith("FAVMSG:READ")) {
			type = "read";
		}
		result.add(type);
		if (type.equals("write")) {
			String connectionNode = path.split("&")[0].split(":")[1];
			result.add(connectionNode);
			String msgId = path.split("&")[1];
			result.add(msgId);
		}
		if (type.equals("read")) {
			String sourIP = path.split("&")[0].split("READ")[1];
			result.add(sourIP);
			String msgId = path.split("&")[1];
			result.add(msgId);
		}
		if (type.equals("not msg")) {
			String ioInfo = path;
			result.add(ioInfo);
		}
		return result;
	}

	public static List<String> tansformPathToStrList(String path, String reportIP) {
		List<String> result = new ArrayList<>();
		result = extractInformationFromPath(path);
		if (result.get(0).equals("write")) {
			result.add(1, reportIP);
		}
		if (result.get(0).equals("read")) {
			result.add(2, reportIP);
		}
		return result;
	}

	private FaultPointBlocked findWriteMsgFPBInList(String sourIP, String destIP) {
		FaultPointBlocked result = null;
		for (int i = 0; i < faultPointList.size(); i++) {
			FaultPointBlocked b = faultPointList.get(i);
			List<String> pathList = tansformPathToStrList(b.path, b.reportNodeIp);
			if (pathList.get(0).equals("write")) {
				String bsour = pathList.get(1);
				String bdest = pathList.get(2);
				if (bdest.equals(destIP) && bsour.equals(sourIP)) {
					Stat.log("Find A write msg which has waited, node: " + bsour + ", path: " + b.path);
					result = b;
					break;
				}
			}
		}
		return result;
	}

	public FaultPointBlocked findAndRemoveFPBInList(IOPoint p) {
		FaultPointBlocked result = null;
		for (int i = 0; i < faultPointList.size(); i++) {
			FaultPointBlocked b = faultPointList.get(i);
			if (checkReportInformationEqualToIOPoint(b, p)) {
				result = b;
				break;
			}
		}
		if (result != null) {
			faultPointList.remove(result);
		}
		return result;
	}

	public static boolean checkReportInformationEqualToIOPoint(FaultPointBlocked b, IOPoint i) {
		boolean result = checkReportInformationEqualToIOPoint(b.ioID, b.reportNodeIp, b.path, i);
		return result;
	}

	public static boolean checkReportInformationEqualToIOPoint(int ioID, String reportNodeIp, String path, IOPoint i) {
		boolean result = false;
		int nIOID = i.computeIoID();
		String nip = i.ip;
		String nPath = i.PATH;
		if ((nIOID == ioID) && (nip.equals(reportNodeIp))
			// && (nPath.equals(path))
			// && (nPath.split("&")[0].equals(path.split("&")[0]))
			// && (FaultPointBlocked.equalInPathPreFix(path, nPath))
			&& (FaultPointBlocked.equalInPathInformation(reportNodeIp, path, nip, nPath))
			) {
			result = true;
		}
		return result;
	}

}
