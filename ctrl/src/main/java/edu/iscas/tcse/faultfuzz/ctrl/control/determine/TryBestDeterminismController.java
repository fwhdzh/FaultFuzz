package edu.iscas.tcse.faultfuzz.ctrl.control.determine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflException;
import edu.iscas.tcse.faultfuzz.ctrl.Cluster;
import edu.iscas.tcse.faultfuzz.ctrl.Conf;
import edu.iscas.tcse.faultfuzz.ctrl.FaultSequence.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.IOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.QueueEntry;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.NormalController.AbortFaultException;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayController;

public class TryBestDeterminismController extends ReplayController{
	
	List<FaultPoint> injectedFaultPointList = new ArrayList<>(); 

    public TryBestDeterminismController(Cluster cluster, int port, Conf favconfig) {
        super(cluster, port, favconfig);
        //TODO Auto-generated constructor stub
    }

    public static class TryBestDeterminismControllerResult {
        boolean allFaultsAreInjected = false;
		List<FaultPointBlocked> actualFPBList;
        int iOPointsDeterminismControlled;
		List<MaxDownNodes> finalCluster;

		List<FaultPoint> injectedFaultPointList; 
	}

    public TryBestDeterminismControllerResult collectTryBestDeterminismControllerResult() {
		TryBestDeterminismControllerResult result = new TryBestDeterminismControllerResult();
		result.allFaultsAreInjected = arriveAllFaultPoint;
		result.actualFPBList = actualFPBList;
        result.iOPointsDeterminismControlled = index.get();
		result.finalCluster = currentCluster;

		result.injectedFaultPointList = injectedFaultPointList;
		return result;
	}


    // @Override
    // public void startController() {
        // TODO Auto-generated method stub
        // running = true;
		// counter = 0;
		// serverThread = new Thread() {

		// 	@Override
		// 	public void run() {

		// 		// TODO Auto-generated method stub
		// 		try {
		// 			Stat.log("Controller port:" + CONTROLLER_PORT);
		// 			serverSocket = new ServerSocket(CONTROLLER_PORT);
		// 			Stat.log("Controller started ...");

		// 			while (running) {
		// 				while (replayClients.size() > maxClients) {
		// 					Thread.currentThread().sleep(500);
		// 				}
		// 				Socket socket = serverSocket.accept(); // server accept the client connection request
		// 				counter++;
		// 				String s = "Accept a socket!";
		// 				s = s + "assign the socket to ReplayCilentHandler id :" + counter;
		// 				Stat.log(s);
		// 				// System.out.println("a client "+counter+" was
		// 				// connected"+socket.getRemoteSocketAddress());
		// 				ReplayCilentHandler sct = new ReplayCilentHandler(socket, counter); // send the request to a separate thread
		// 				sct.start();
		// 				replayClients.add(sct);
		// 			}
		// 			serverSocket.close();

		// 		} catch (Exception e) {
		// 			e.printStackTrace();
		// 			Stat.log("recieve total socket count: " + counter);
		// 			Stat.log("faultPointList size is : " + faultPointList.size());
		// 			Stat.log("actualFPBList size is : " + actualFPBList.size());
		// 			Stat.log("The left FPBS are : ");
		// 			String s = "";
		// 			for (FaultPointBlocked fpb : faultPointList) {
		// 				s = s + JSONObject.toJSONString(fpb) + "\n";
		// 			}
		// 			Stat.log(s);
		// 		}
		// 	}
		// };
		// serverThread.start();

		// ListScanner scanThread = new ListScanner();
		// scanThread.start();
    // }

	
	@Override
	protected void startScanThread() {
		// TODO Auto-generated method stub
		TryBestDeterminismListScanner scanThread = new TryBestDeterminismListScanner();
		scanThread.start();
	}

    public class TryBestDeterminismListScanner extends ListScanner {

        public int maxWaitInternalInNormalIOPoint = favconfig.DETERMINE_WAIT_TIME;

		public int[] indexOfFaultPoint;

		public void printTheIndexOfFaultPoint() {
			indexOfFaultPoint = new int[faultSeq.seq.size()];
			for (int t = 0; t < faultSeq.seq.size(); t++) {
				FaultPoint fp = faultSeq.seq.get(t);
				int currentAppear = 0;
				int index = -1;
				for (int i = 0; i < ioSeq.size(); i++) {
					if (ioSeq.get(i).ioID == fp.ioPt.ioID) {
						currentAppear++;
					}
					if (currentAppear == fp.ioPt.appearIdx) {
						index = i;
						break;
					}
				}
				if (currentAppear == fp.ioPt.appearIdx) {
					Stat.log("the " + t + "th fault point index is: " + index);
					indexOfFaultPoint[t] = index;
				} else {
					Stat.log("the " + t + "th fault point couldn't be found in faultSeq!");
				}
			}
		}

        @Override
        public void run() {
            Stat.log("ListScanner start...");
			
            Stat.log("ioList size is: " + ioSeq.size());
			// Stat.log("All the ioIDs are: " + entry.getIoSeqToIDString());
            Stat.debug("All the ioIDs are: " + QueueEntry.getIoSeqToIDString(ioSeq));
			printTheIndexOfFaultPoint();
            boolean needToTurnToNormalController = false;
			try {
				// boolean timeOut = false;
				// while (!timeOut && index.get() < entry.ioSeq.size()) {
				// while (index.get() < entry.ioSeq.size()) {
                while (!arriveAllFaultPoint) {
					IOPoint p = ioSeq.get(index.get());
					Stat.debug("ListScanner next index to check:  " + index.get());
					Stat.log("ListScanner next to wait: " + p.ioID + ", from " + p.ip + ", path: " + p.PATH);
					// long timeCount = 0;
					FaultPointBlocked b = findAndRemoveFPBInList(p);
                    int waitTime = 0;

					int maxWaitInternal = maxWaitInternalInNormalIOPoint;
					if (index.get() == 0) {
						maxWaitInternal = maxWaitInternal * 3;
					}

					while (b == null && waitTime < maxWaitInternal) {
						sleep(scanInternal);
                        waitTime += scanInternal;
						b = findAndRemoveFPBInList(p);
					}
                    if (waitTime >= maxWaitInternal) {
                        Stat.log("ListScanner wait time out! The ioID is: " + p.ioID + ", path: " + p.PATH);
                        needToTurnToNormalController = !arriveAllFaultPoint;
                        break;
                    }
					Stat.debug("Find FaultPointBlocked! For now, faultPointList size is " + faultPointList.size());

                    handleFPB(b);

					index.getAndIncrement();
					actualFPBList.add(b);
				}

                if (needToTurnToNormalController) {

					Stat.log("Begin to normal control, left faults size is " + faultPointList.size());
					// AflCli.executeCliCommandToCluster(currentCluster, favconfig, AflCommand.DETERMINE_NORMAL, 300000);
					AflCli.executeUtilSuccess(currentCluster, favconfig, AflCommand.DETERMINE_NORMAL, 300000);

					while (!arriveAllFaultPoint) {
						if (faultPointList.size() > 0) {
							FaultPointBlocked b = faultPointList.get(0);
							faultPointList.remove(0);
							handleFPB(b);
							actualFPBList.add(b);
						}
						// we won't wait so long time to ensure the bug is not triggered.
						// Give up some accuracy, get more effience
						if (actualFPBList.size() > Math.min(indexOfFaultPoint[indexOfFaultPoint.length-1] * 6, ioSeq.size() * 4 )) {
							break;
						}
					}
                }

				if (arriveAllFaultPoint) {
					Stat.log("All of faults have been replayed!");
				} else {
					Stat.log("We will give up this test since the I/O point to inject fault didn't appear for now...");
				}
				
				// boolean aflCommandResult =  AflCli.executeUtilSuccess(currentCluster, favconfig, AflCommand.DETERMINE_NO_SEND, 300000);

				List<AflCliNoSendThread> aflCliNoSendThreadList = new ArrayList<>();
				List<String> aliveNodes = AflCli.getAliveNodesInCluster(currentCluster);
				for (String node: aliveNodes) {
					String[] args = new String[3];
					args[0] = node;
					args[1] = String.valueOf(favconfig.AFL_PORT);
					args[2] = AflCommand.DETERMINE_NO_SEND.toString();
					AflCliNoSendThread aCliNoSendThread = new AflCliNoSendThread(args);
					aflCliNoSendThreadList.add(aCliNoSendThread);
					aCliNoSendThread.start();
				}
				for (AflCliNoSendThread aflCliNoSendThread: aflCliNoSendThreadList) {
					/**
					 * These thread will finish as they are designed. The parameter of join is meaningless. 
					 */
					aflCliNoSendThread.join(600000);
				}
				boolean aflCommandResult = true;
				for (AflCliNoSendThread aflCliNoSendThread: aflCliNoSendThreadList) {
					if (aflCliNoSendThread.result == false) {
						aflCommandResult = false;
						break;
					}
				}

				if (!aflCommandResult) {
					finishFlag = true;
					return;
				}

				freeAllFPB();

				finishFlag = true;
				

				// if (timeOut) {
				// result = 1;
				// }
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
        }

		synchronized public void freeAllFPB() throws IOException, AflException, AbortFaultException {
			while (faultPointList.size() > 0) {
				FaultPointBlocked b = faultPointList.get(0);
				faultPointList.remove(0);
				handleFPB(b);
				actualFPBList.add(b);
			}
		}

		public class AflCliNoSendThread extends Thread {
			private final CountDownLatch mDoneSignal;
			private final String[] args;

			public boolean result;

			public AflCliNoSendThread(CountDownLatch mDoneSignal, String[] args) {
				this.mDoneSignal = mDoneSignal;
				this.args = args;
			}

			public AflCliNoSendThread(String[] args) {
				this.mDoneSignal = null;
				this.args = args;
			}

			@Override
			public void run() {
				boolean flag = AflCli.executeUtilSuccess(args);
				if (flag) {
					try {
						freeAllFPB();
					} catch (IOException | AflException | AbortFaultException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				result = flag;
			}
		}

		@Override
        public void handleFPB(FaultPointBlocked b) throws IOException, AflException, AbortFaultException {
            if (!arriveAllFaultPoint) {
                
				updataIOAppearIdxInFaultSeq(faultSeq, fIndex.get(), b.ioID);
                FaultPoint fp = faultSeq.seq.get(fIndex.get());

				if (checkAFaultCouldNotArrive(fp, b)) {

				} 

				
                if (checkFaultPointMatchFPBAndAppearIdx(fp, b)
					|| checkFaultPointMatchFPBAndAppearIdxWithCrashFuzzMode(fp, b) 	// this is the approach for CrashFuzz. Need to modify in future!!!
				) {
					fp.actualNodeIp = fp.tarNodeIp;
                    // fp.actualNodeIp = b.reportNodeIp;
                    Stat.log("A FaultPoint is found! The faultPoint is: " + fp.stat + ", ioID: " + fp.ioPt.ioID
                            + ", ip: " + fp.ioPt.ip + ", path: " + fp.ioPt.PATH);

					injectedFaultPointList.add(fp);

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

		public boolean checkAFaultCouldNotArrive(FaultPoint fp, FaultPointBlocked fpb) {
            boolean result = false;
            // if (fp.ioPt.ioID == fpb.ioID && fp.ioPt.appearIdx == fp.curAppear) {
            //     result = true;
            // }

            return result;
        }
        
    }

}
