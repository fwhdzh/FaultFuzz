package edu.iscas.tcse.favtrigger.triggering;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import edu.iscas.tcse.favtrigger.MyLogger;
import edu.iscas.tcse.favtrigger.instrumenter.TriggerEvent;
import edu.columbia.cs.psl.phosphor.Configuration;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.tracing.FAVEntry;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;

public class WaitToExec { //for docker
    public static final String cannotSchedule = "FAV-CANNOT-SCHEDULE-THIS-CRASH-POINT";
    public static void checkCrashEvent(String path, String contentID) {
        Thread thread = Thread.currentThread();
        List<String> callstack = RecordTaint.getCallStack(thread, 4);
        FAVEntry entry = new FAVEntry();
        long procID = FAVTaint.getProcessID();
        String crashNode = FAVTaint.getIP();
        entry.PATH = path;
        entry.CALLSTACK = callstack;
        entry.ip = crashNode;

        handleCrashPoint(procID, crashNode, entry, callstack, path);
    }

    public static int currentIOID(List<String> callstack) {
    	return callstack.toString().hashCode();
    }

    public static void handleCrashPoint(long procID, String nodeIP, FAVEntry entry, List<String> callstack, String path) {
    	if(CurrentFaultSequence.faultSeq == null || CurrentFaultSequence.faultSeq.curFault == -1 || CurrentFaultSequence.faultSeq.curFault >= CurrentFaultSequence.faultSeq.seq.size()) {
    		//no faults to inject
//    		CrashTriggerMain.log(procID+" WaitToExec No faults to inject:"+CurrentFaultSequence.faultSeq.curFault
//    				+", "+CurrentFaultSequence.faultSeq.seq.size());
    		return;
    	}
    	Integer ioID = null;
    	int i = CurrentFaultSequence.faultSeq.curFault;
    	for(; i<CurrentFaultSequence.faultSeq.seq.size(); i++) {
    		if(currentIOID(callstack) == CurrentFaultSequence.faultSeq.seq.get(i).ioPt.ioID) {
    			ioID = CurrentFaultSequence.faultSeq.seq.get(i).ioPt.ioID;
    			break;
    		}
    	}

    	if(ioID != null) {
    		MyLogger.log(nodeIP+":"+procID+" System WaitToExec prepare to get permission to crash "+ Configuration.CONTROLLER_SOCKET
            		+", before writing "+path+", fault io ID:"+ioID+", curFaultIdx:"+CurrentFaultSequence.faultSeq.curFault
            		+", match fault index:"+i);

            String procInfo = "",fuzzCommand = "";
            try{
                //System.out.println(procID+" meet current crash point!");
                String[] secs = Configuration.CONTROLLER_SOCKET.split(":");
                Socket socket = new Socket(secs[0].trim(),Integer.parseInt(secs[1].trim()));
                //System.out.println(procID+" remote controller address:"+socket.getRemoteSocketAddress());
                DataInputStream inStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream objOut = new ObjectOutputStream(outStream);

//              procInfo = Long.toString(procID);
//              outStream.writeUTF(procInfo);
//              outStream.flush();
//                objOut.writeObject(entry);
//                objOut.flush();
                outStream.writeInt(ioID.intValue());
                outStream.writeUTF(nodeIP);
                Random rand = new Random();
                int id = rand.nextInt();
                outStream.writeUTF(nodeIP+":"+id);
                outStream.flush();
                //System.out.println(procID+"!!!!!WaitToExec Send msg to controller:"+procInfo);

                fuzzCommand = inStream.readUTF();
                CurrentFaultSequence.faultSeq.curFault = inStream.readInt();
                int curAppearIdx = inStream.readInt();
                //System.out.println(procID+"!!!!!WaitToExec Read msg from controller:"+controllerResponse);

                inStream.close();
                objOut.close();
                socket.close();

                String suffix = " ["+id+"] For io "+ioID+", received curFault index is "+CurrentFaultSequence.faultSeq.curFault
                		+", curAppearIdx is "+curAppearIdx+", my fault size is "+CurrentFaultSequence.faultSeq.seq.size();
                if(fuzzCommand.equals(TriggerEvent.CONTI.toString())
                		||fuzzCommand.equals(TriggerEvent.REBOOT.toString())) {
                	MyLogger.log(nodeIP+":"+procID+" System WaitToExec received keep exec command!"+fuzzCommand+suffix);
                    return;
                } else if (fuzzCommand.equals(TriggerEvent.CRASH.toString())) {
                	MyLogger.log(nodeIP+":"+procID+" System WaitToExec received crash command!"+fuzzCommand+suffix);
                    //crashCurNode();
                    while(true) {
                         try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else {
                	MyLogger.log(nodeIP+":"+procID+" System WaitToExec received abnormal message: "+fuzzCommand+suffix);
                }
            } catch(Exception e) {
            	MyLogger.log(nodeIP+":"+procID+" [ERROR] System WaitToExec got exception:"+e.getMessage());
                e.printStackTrace();
            }
            MyLogger.log(nodeIP+":"+procID+" [ERROR] System WaitToExec failed to trigger a current crash fault!!!"+fuzzCommand+", "+callstack);
    	} else {
//    		CrashTriggerMain.log(procID+"WaitToExec not the expected io ID:"+ioID);
    	}
    }

    public static void crashCurNode() {
        Runtime.getRuntime().halt(-1);
    }

   //-1: not similar
  	//0: total same
  	//1: similar path
  	//for substring, tolerate at most 1 similar difference
  	private static int similarStringWithSign(String str1, String str2, String sign){
  		int diff = 0;
  		if(!str1.contains(sign) || !str2.contains(sign)) {
  			return -1;
  		}
  		String[] sec1 = str1.split(sign);
  		String[] sec2 = str2.split(sign);

  		if(sec1.length  != sec2.length) {
  			return -1;
  		}
  		for(int i = 0; i< sec1.length; i++) {
  			if(!sec1[i].equals(sec2[i])) {
  				if(isHexNumberRex(sec1[i]) && isHexNumberRex(sec2[i])) {
  					diff++;
  					if(diff > 1) {
  						return -1;
  					} else {
  						continue;
  					}
  				} else {
  					return -1;
  				}
  			}
  		}
  		if(diff == 1) {
  			return 1;
  		} else {
  			return 0;
  		}
  	}

  	private static int similarStringWithSigns(String str1, String str2) {
  		String[] signs = new String[] {"-", "_", ",", "."};
  		for(String sign:signs) {
  			int similar = similarStringWithSign(str1, str2, sign);
  			if(similar != -1) {
  				return similar;
  			}
  		}
  		return -1;
  	}
  	//can tolerate at most 2 similar differences
  	public static boolean likelySamePath(String str1, String str2){
  		if(str1.startsWith(FAVPathType.FAVMSG.toString()) && str1.lastIndexOf("&") != -1) {
  			str1 = str1.substring(0, str1.lastIndexOf("&"));
  		}
  		if(str2.startsWith(FAVPathType.FAVMSG.toString()) && str2.lastIndexOf("&") != -1) {
  			str2 = str2.substring(0, str2.lastIndexOf("&"));
  		}
  		String[] secs1 = str1.split("/");
  		// System.out.println(secs1.length+" "+secs1[0]);
  		String[] secs2 = str2.split("/");
  		// System.out.println(secs2.length+" "+secs2[0]);
  		int diff = 0;
  		if(secs1.length != secs2.length) {
  			return false;
  		}
  		for(int i = 0; i< secs1.length; i++) {
  			if(!secs1[i].equals(secs2[i])) {
  				if(isHexNumberRex(secs1[i]) && isHexNumberRex(secs2[i])) {
					diff++;
					if(diff>2) {
						return false;
					}
				} else {
					int similar = similarStringWithSigns(secs1[i], secs2[i]);
					if(similar == 1) {
	  					diff++;
	  					if(diff>2) {
	  						return false;
	  					}
	  				} else if (similar == -1) {
	  					return false;
	  				}
				}
  			}
  		}
  		return true;
  	}

  	private static boolean isHexNumberRex(String str){
		String validate = "(?i)[0-9a-f]+";
		return str.matches(validate);
	}
}
