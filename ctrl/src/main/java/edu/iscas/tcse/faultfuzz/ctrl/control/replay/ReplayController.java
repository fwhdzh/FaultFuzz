package edu.iscas.tcse.faultfuzz.ctrl.control.replay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflException;
import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.Network;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractController;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultSequence;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultType;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.runtime.QueueEntryRuntime;

public class ReplayController 
// extends NormalController 
extends AbstractController
{
	public Thread serverThread;
	public ServerSocket serverSocket;

	public List<MaxDownNodes> currentCluster = new ArrayList<MaxDownNodes>();
	public Network network;

	public Set<ReplayCilentHandler> replayClients;

	public List<RunTimeIOPoint> faultPointList;
	public AtomicInteger index;
	public AtomicInteger fIndex;

	// private QueueEntry entry;
	public List<IOPoint> ioSeq;
	public FaultSequence faultSeq;
	

	public boolean arriveAllFaultPoint;

	public List<RunTimeIOPoint> arriveRunTimeIOPointList;
	public List<RunTimeIOPoint> actualRunTimeIOPointList;
	public int counter;

	public boolean finishFlag;

	public boolean existFaultNotMeet;

	public ReplayController(Cluster cluster, int port, Conf favconfig) {
		super(cluster, port, favconfig);

		currentCluster = MaxDownNodes.cloneCluster(favconfig.maxDownGroup);
		network = Network.constructNetworkFromMaxDOwnNodes(currentCluster);

		replayClients = Collections.synchronizedSet(new HashSet<ReplayCilentHandler>());
		faultPointList = Collections.synchronizedList(new ArrayList<RunTimeIOPoint>());
		index = new AtomicInteger(0);
		fIndex = new AtomicInteger(0);
		arriveAllFaultPoint = false;
		arriveRunTimeIOPointList = Collections.synchronizedList(new ArrayList<RunTimeIOPoint>());
		actualRunTimeIOPointList = Collections.synchronizedList(new ArrayList<RunTimeIOPoint>());
		counter = 0;
		finishFlag = false;

		existFaultNotMeet = false;
	}

	public static class ReplayControllerResult {
		boolean allPointsAreReplayed = false;
		boolean allFaultsAreReplayed = false;
		List<RunTimeIOPoint> actualRunTimeIOPointList;
		List<MaxDownNodes> finalCluster;
	}

	public ReplayControllerResult collectReplayResult() {
		ReplayControllerResult result = new ReplayControllerResult();
		result.allPointsAreReplayed = index.get() >= ioSeq.size();
		result.allFaultsAreReplayed = arriveAllFaultPoint;
		result.actualRunTimeIOPointList = actualRunTimeIOPointList;
		result.finalCluster = currentCluster;
		return result;
	}

	public void prepareFaultSeqAndIOSeq(QueueEntryRuntime entryRuntime) {
		// this.entry = entry;
		this.ioSeq = entryRuntime.ioSeq;
		this.faultSeq = entryRuntime.faultSeq;
		entryRuntime.faultSeq.reset();
		updataCurFaultFile(entryRuntime.faultSeq);
		Stat.log("Current fault sequence was prepared.");

	    arriveAllFaultPoint = faultSeq.isEmpty();
		
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
						Stat.log("actualRunTimeIOPointList size is : " + actualRunTimeIOPointList.size());
						Stat.log("The left RunTimeIOPointS are : ");
						String s = "";
						for (RunTimeIOPoint fpb : faultPointList) {
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
		System.out.println("Controller was stopped.");
	}


	public void continueAllRunTimeIOPoint() throws IOException, AflException, AbstractController.AbortFaultException {
		for (RunTimeIOPoint fpb : faultPointList) {
			fpb.cilentHander.doOperationToCluster(fpb);
		}
	}

	public abstract class AbstarctRunTimeIOPointComparator {

	}

	public class ConnectedNodeRunTimeIOPointComparator extends AbstarctRunTimeIOPointComparator{
		
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
				int ioID = inStream.readInt();
				String reportNodeIp = inStream.readUTF();
				String cliID = inStream.readUTF();
				String path = inStream.readUTF();
				String threadInfo = inStream.readUTF();
				String info = "";
				Stat.debug("" + id + "ReplayCilentHandler read ioID: " + ioID + "\n");
				Stat.debug("" + id + "ReplayCilentHandler read reportNodeIp: " + reportNodeIp + "\n");
				Stat.debug("" + id + "recieve cliID: " + cliID + " for ioID " + ioID + ", " + "\n");
				Stat.debug("" + id + "recieve path: " + path + "\n");
				Stat.debug("" + id + "recieve threadInfo: " + threadInfo + "\n");
				Stat.debug("" + id + "handle information: " + JSONObject.toJSONString(IOPoint.tansformPathToStrList(path, reportNodeIp)));
				RunTimeIOPoint b = new RunTimeIOPoint(ioID, reportNodeIp, cliID, path, this);
				faultPointList.add(b);
				arriveRunTimeIOPointList.add(b);
				Stat.debug("For now, faultPointList size is " + faultPointList.size());
				Stat.debug("add a element to faultPointList");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void replyToNode(DataOutputStream outStream, String command)
				throws IOException {
			outStream.writeUTF(command);;
			closeTheSocketConnection();
		}

		public void closeTheSocketConnection() throws IOException {
			socket.getOutputStream().flush();
			socket.close();
			replayClients.remove(this);
		}

		public void doOperationToCluster(FaultPoint p) throws IOException, AflException, AbstractController.AbortFaultException {
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			if (p.type.equals(FaultType.CRASH)) {
				replyToNode(outStream, "CRASH");
				String[] args = new String[3];
				args[0] = p.actualNodeIp;
				args[1] = String.valueOf(favconfig.AFL_PORT);
				args[2] = AflCommand.SAVE.toString();
				AflCli.interactWithNode(args);
				rst.add(Stat.log("Prepare to crash node " + p.actualNodeIp));
				List<String> crashRst = cluster.killNode(p.actualNodeIp, p.actualNodeIp);
				rst.addAll(crashRst);
				rst.add(Stat.log("node " + p.actualNodeIp + " was killed!"));
				MaxDownNodes.buildClusterStatus(currentCluster, p.actualNodeIp, FaultType.CRASH);
			} else if (p.type.equals(FaultType.REBOOT)) {
				if (MaxDownNodes.isAliveNode(currentCluster, p.actualNodeIp)) {
					throw new AbstractController.AbortFaultException("Restarting an alive node " + p.actualNodeIp + "!!!");
				}
				// Restart the node
				List<String> restartRst = cluster.restartNode(p.actualNodeIp);
				rst.addAll(restartRst);
				rst.add(Stat.log("node " + p.actualNodeIp + " was restarted!"));
				outStream = new DataOutputStream(socket.getOutputStream());
				replyToNode(outStream, "REBOOT");
				MaxDownNodes.buildClusterStatus(currentCluster, p.actualNodeIp, FaultType.REBOOT);
			} else if (p.type.equals(FaultType.NETWORK_DISCONNECTION)) {
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
				replyToNode(outStream, "CONTI");
			} else if (p.type.equals(FaultType.NETWORK_RECONNECTION)) {
				if (p.params == null || p.params.size() != 2) {
					throw new AflException("NETWORK_RECONNECTION fault should have two parameters!");
				}
				String sourceIp = p.params.get(0);
				String destIp = p.params.get(1);
				List<String> connectRst = cluster.networkConnect(sourceIp, destIp);
				if (connectRst != null) {
					rst.addAll(connectRst);
				}
				rst.add(Stat.log("network from  " + sourceIp + " to "+ destIp + " was connected!"));
				outStream = new DataOutputStream(socket.getOutputStream());
				replyToNode(outStream, "CONTI");
			} else {
				replyToNode(outStream, "CONTI");
			}
		}

		public void doOperationToCluster(IOPoint p) throws IOException, AflException, AbstractController.AbortFaultException {
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			replyToNode(outStream, "CONTI");
			Stat.log("reply CONTI for " + p.ioID + " to node " + p.ip + ", path: " + p.PATH);
		}

		public void doOperationToCluster(RunTimeIOPoint b) throws IOException, AflException, AbstractController.AbortFaultException {
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			replyToNode(outStream, "CONTI");
			Stat.log("reply CONTI for " + b.ioID + " to node " + b.reportNodeIp + ", path: " + b.path);
		}
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
			try {
				while (index.get() < ioSeq.size()) {
					IOPoint p = ioSeq.get(index.get());
					Stat.debug("ListScanner next index to check:  " + index.get());
					Stat.log("ListScanner next to wait: " + p.ioID + ", from " + p.ip + ", path: " + p.PATH);
					RunTimeIOPoint b = findAndRemoveRunTimeIOPointInList(p);
					while (b == null) {
						sleep(scanInternal);
						b = findAndRemoveRunTimeIOPointInList(p);
					}
					Stat.log("Find FaultPointBlocked! For now, faultPointList size is " + faultPointList.size());
					handleRunTimeIOPoint(b);
					index.getAndIncrement();
					actualRunTimeIOPointList.add(b);
				}
				Stat.log("All of IOPoints have been replayed!");
				finishFlag = true;
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
        public boolean checkFaultPointMatchRunTimeIOPointAndAppearIdx(FaultPoint fp, RunTimeIOPoint fpb) {
            boolean result = false;
            if (fp.ioPt.ioID == fpb.ioID && fp.ioPt.appearIdx == fp.curAppear) {
                result = true;
            }
            return result;
        }

		public boolean checkFaultPointMatchRunTimeIOPointAndAppearIdxWithCrashFuzzMode(FaultPoint fp, RunTimeIOPoint fpb) {
            boolean result = false;
            if (fp.ioPt.ioID == fpb.ioID && fp.ioPt.appearIdx <= fp.curAppear) {
                result = true;
            }
            return result;
        }

		public void handleRunTimeIOPoint(RunTimeIOPoint b) throws IOException, AflException, AbstractController.AbortFaultException {
            if (!arriveAllFaultPoint) {
				updataIOAppearIdxInFaultSeq(faultSeq, fIndex.get(), b.ioID);
                FaultPoint fp = faultSeq.seq.get(fIndex.get());
                if (checkFaultPointMatchRunTimeIOPointAndAppearIdx(fp, b)) {
					fp.actualNodeIp = fp.tarNodeIp;
                    // fp.actualNodeIp = b.reportNodeIp;
                    Stat.log("A FaultPoint is found! The faultPoint is: " + fp.type + ", ioID: " + fp.ioPt.ioID
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
                        Stat.log("Next faultPoint is: " + faultSeq.seq.get(nf).type + ", ioID: "
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

	public RunTimeIOPoint findAndRemoveRunTimeIOPointInList(IOPoint p) {
		RunTimeIOPoint result = null;
		for (int i = 0; i < faultPointList.size(); i++) {
			RunTimeIOPoint b = faultPointList.get(i);
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

	public static boolean checkReportInformationEqualToIOPoint(RunTimeIOPoint b, IOPoint i) {
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
			&& (RunTimeIOPoint.equalInPathInformation(reportNodeIp, path, nip, nPath))
			) {
			result = true;
		}
		return result;
	}

}
