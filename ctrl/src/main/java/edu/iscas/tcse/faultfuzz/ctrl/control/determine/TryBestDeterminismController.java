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
import edu.iscas.tcse.faultfuzz.ctrl.MaxDownNodes;
import edu.iscas.tcse.faultfuzz.ctrl.Stat;
import edu.iscas.tcse.faultfuzz.ctrl.control.AbstractController;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.ReplayController;
import edu.iscas.tcse.faultfuzz.ctrl.control.replay.RunTimeIOPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.FaultPoint;
import edu.iscas.tcse.faultfuzz.ctrl.model.IOPoint;

public class TryBestDeterminismController extends ReplayController{
	
	List<FaultPoint> injectedFaultPointList = new ArrayList<>(); 

    public TryBestDeterminismController(Cluster cluster, int port, Conf favconfig) {
        super(cluster, port, favconfig);
        //TODO Auto-generated constructor stub
    }

    public static class TryBestDeterminismControllerResult {
        boolean allFaultsAreInjected = false;
		List<RunTimeIOPoint> actualRunTimeIOPointList;
        int iOPointsDeterminismControlled;
		List<MaxDownNodes> finalCluster;

		List<FaultPoint> injectedFaultPointList; 
	}

    public TryBestDeterminismControllerResult collectTryBestDeterminismControllerResult() {
		TryBestDeterminismControllerResult result = new TryBestDeterminismControllerResult();
		result.allFaultsAreInjected = arriveAllFaultPoint;
		result.actualRunTimeIOPointList = actualRunTimeIOPointList;
        result.iOPointsDeterminismControlled = index.get();
		result.finalCluster = currentCluster;

		result.injectedFaultPointList = injectedFaultPointList;
		return result;
	}

	
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
			printTheIndexOfFaultPoint();
            boolean needToTurnToNormalController = false;
			try {
                while (!arriveAllFaultPoint) {
					IOPoint p = ioSeq.get(index.get());
					Stat.debug("ListScanner next index to check:  " + index.get());
					Stat.log("ListScanner next to wait: " + p.ioID + ", from " + p.ip + ", path: " + p.PATH);
					RunTimeIOPoint b = findAndRemoveRunTimeIOPointInList(p);
                    int waitTime = 0;
					int maxWaitInternal = maxWaitInternalInNormalIOPoint;
					if (index.get() == 0) {
						maxWaitInternal = maxWaitInternal * 3;
					}
					while (b == null && waitTime < maxWaitInternal) {
						sleep(scanInternal);
                        waitTime += scanInternal;
						b = findAndRemoveRunTimeIOPointInList(p);
					}
                    if (waitTime >= maxWaitInternal) {
                        Stat.log("ListScanner wait time out! The ioID is: " + p.ioID + ", path: " + p.PATH);
                        needToTurnToNormalController = !arriveAllFaultPoint;
                        break;
                    }
					Stat.debug("Find FaultPointBlocked! For now, faultPointList size is " + faultPointList.size());
                    handleRunTimeIOPoint(b);
					index.getAndIncrement();
					actualRunTimeIOPointList.add(b);
				}
                if (needToTurnToNormalController) {
					Stat.log("Begin to normal control, left faults size is " + faultPointList.size());
					AflCli.executeUtilSuccess(currentCluster, favconfig, AflCommand.DETERMINE_NORMAL, 300000);
					while (!arriveAllFaultPoint) {
						if (faultPointList.size() > 0) {
							RunTimeIOPoint b = faultPointList.get(0);
							faultPointList.remove(0);
							handleRunTimeIOPoint(b);
							actualRunTimeIOPointList.add(b);
						}
						// we won't wait so long time to ensure the bug is not triggered.
						// Give up some accuracy, get more effience
						if (actualRunTimeIOPointList.size() > Math.min(indexOfFaultPoint[indexOfFaultPoint.length-1] * 6, ioSeq.size() * 4 )) {
							break;
						}
					}
                }
				if (arriveAllFaultPoint) {
					Stat.log("All of faults have been replayed!");
				} else {
					Stat.log("We will give up this test since the I/O point to inject fault didn't appear for now...");
				}
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
				freeAllRunTimeIOPoint();
				finishFlag = true;
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
        }

		synchronized public void freeAllRunTimeIOPoint() throws IOException, AflException, AbstractController.AbortFaultException {
			while (faultPointList.size() > 0) {
				RunTimeIOPoint b = faultPointList.get(0);
				faultPointList.remove(0);
				handleRunTimeIOPoint(b);
				actualRunTimeIOPointList.add(b);
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
						freeAllRunTimeIOPoint();
					} catch (IOException | AflException | AbstractController.AbortFaultException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				result = flag;
			}
		}

		@Override
        public void handleRunTimeIOPoint(RunTimeIOPoint b) throws IOException, AflException, AbstractController.AbortFaultException {
            if (!arriveAllFaultPoint) {
				updataIOAppearIdxInFaultSeq(faultSeq, fIndex.get(), b.ioID);
                FaultPoint fp = faultSeq.seq.get(fIndex.get());
                if (checkFaultPointMatchRunTimeIOPointAndAppearIdx(fp, b)
					|| checkFaultPointMatchRunTimeIOPointAndAppearIdxWithCrashFuzzMode(fp, b) 	// this is the approach for CrashFuzz. Need to modify in future!!!
				) {
					fp.actualNodeIp = fp.tarNodeIp;
                    // fp.actualNodeIp = b.reportNodeIp;
                    Stat.log("A FaultPoint is found! The faultPoint is: " + fp.type + ", ioID: " + fp.ioPt.ioID
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

}
