package edu.iscas.CCrashFuzzer.control.determine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

import edu.iscas.CCrashFuzzer.AflCli;
import edu.iscas.CCrashFuzzer.Cluster;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.FaultSequence;
import edu.iscas.CCrashFuzzer.IOPoint;
import edu.iscas.CCrashFuzzer.MaxDownNodes;
import edu.iscas.CCrashFuzzer.QueueEntry;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.AflCli.AflCommand;
import edu.iscas.CCrashFuzzer.AflCli.AflException;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.control.NormalController.AbortFaultException;
import edu.iscas.CCrashFuzzer.control.replay.ReplayController;

public class TryBestDeterminismController extends ReplayController{

    public TryBestDeterminismController(Cluster cluster, int port, Conf favconfig) {
        super(cluster, port, favconfig);
        //TODO Auto-generated constructor stub
    }

    public static class TryBestDeterminismControllerResult {
        boolean allFaultsAreInjected = false;
		List<FaultPointBlocked> actualFPBList;
        int iOPointsDeterminismControlled;
		List<MaxDownNodes> finalCluster;
	}

    public TryBestDeterminismControllerResult collectTryBestDeterminismControllerResult() {
		TryBestDeterminismControllerResult result = new TryBestDeterminismControllerResult();
		result.allFaultsAreInjected = arriveAllFaultPoint;
		result.actualFPBList = actualFPBList;
        result.iOPointsDeterminismControlled = index.get();
		result.finalCluster = currentCluster;
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

        public int maxWaitInternal = favconfig.DETERMINE_WAIT_TIME;

        @Override
        public void run() {
            Stat.log("ListScanner start...");
			
            Stat.log("ioList size is: " + ioSeq.size());
			// Stat.log("All the ioIDs are: " + entry.getIoSeqToIDString());
            Stat.log("All the ioIDs are: " + QueueEntry.getIoSeqToIDString(ioSeq));
            boolean needToTurnToNormalController = false;
			try {
				// boolean timeOut = false;
				// while (!timeOut && index.get() < entry.ioSeq.size()) {
				// while (index.get() < entry.ioSeq.size()) {
                while (!arriveAllFaultPoint) {
					IOPoint p = ioSeq.get(index.get());
					Stat.log("ListScanner next index to check:  " + index.get());
					Stat.log("ListScanner next to wait: " + p.ioID + ", from " + p.ip + ", path: " + p.PATH);
					// long timeCount = 0;
					FaultPointBlocked b = findAndRemoveFPBInList(p);
                    int waitTime = 0;
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
					Stat.log("Find FaultPointBlocked! For now, faultPointList size is " + faultPointList.size());

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
					}
                    // while (faultPointList.size() > 0 && !arriveAllFaultPoint) {
                    //     FaultPointBlocked b = faultPointList.get(0);
                    //     faultPointList.remove(0);
                    //     handleFPB(b);
                    //     actualFPBList.add(b);
                    // }
                }
				Stat.log("All of faults have been replayed!");

				finishFlag = true;

				AflCli.executeUtilSuccess(currentCluster, favconfig, AflCommand.DETERMINE_NO_SEND, 300000);
				while (faultPointList.size() > 0) {
					FaultPointBlocked b = faultPointList.get(0);
					faultPointList.remove(0);
					handleFPB(b);
					actualFPBList.add(b);
				}

				// if (timeOut) {
				// result = 1;
				// }
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
        }

        

        
    }

}
