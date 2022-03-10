package edu.iscas.CCrashFuzzer.random;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.iscas.CCrashFuzzer.AflCli.AflCommand;
import edu.iscas.CCrashFuzzer.AflCli.AflException;
import edu.iscas.CCrashFuzzer.Cluster;
import edu.iscas.CCrashFuzzer.Conf;
import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
import edu.iscas.CCrashFuzzer.Mutation;
import edu.iscas.CCrashFuzzer.RunCommand;
import edu.iscas.CCrashFuzzer.Stat;
import edu.iscas.CCrashFuzzer.random.RandomFaultSequence.RandomFaultPoint;
import edu.iscas.CCrashFuzzer.utils.FileUtil;
//We do not trigger remote crash in this controller.
//This controller aims to trigger local crashes for systems deployed as processes in the same machine
public class RandomController {
	public Cluster cluster;
	public boolean running;
	public Set<Thread> clients;
	public int CONTROLLER_PORT = 8888;
	public RandomFaultSequence faultSequence; //store current crash point ID to the crash before point
    public Thread fautInjectionThread;
    public ServerSocket serverSocket;
    public boolean faultInjected;
    public boolean injectionAborted;//cannot schedule current fault sequence any more
    public ArrayList<String> rst;
    public Conf favconfig;
    List<MaxDownNodes> currentCluster = new ArrayList<MaxDownNodes>();
    public final int maxClients = 300;

    public RandomController(Cluster cluster, int port, Conf favconfig) {
    	this.cluster = cluster;
    	this.running = false;
    	this.CONTROLLER_PORT = port;
    	this.favconfig = favconfig;
    	this.faultInjected = false;
    	this.injectionAborted = false;
    	this.rst = new ArrayList<String>();
    	this.clients = Collections.synchronizedSet(new HashSet<Thread>());
    	currentCluster = Mutation.cloneCluster(favconfig.maxDownGroup);
    }

    public void startController() {
		running = true;
		fautInjectionThread = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					for(int cur_Fault = 0; cur_Fault < faultSequence.seq.size(); cur_Fault++) {
				    	RandomFaultPoint fault = faultSequence.seq.get(cur_Fault);
				    	
				    	Thread.currentThread().sleep(fault.waitTimeMillions);
				    	
				    	if(fault.stat.equals(FaultStat.CRASH)) {
				    		fault.actualNodeIp = fault.tarNodeIp;
							rst.add(Stat.log("Meet "+cur_Fault+"th fault point [CRASH]:"+fault));

							//Restart the node
			        		rst.add(Stat.log("Prepare to crash node "+fault.actualNodeIp));
			                List<String> crashRst = cluster.killNode(fault.actualNodeIp, fault.actualNodeIp);
			                rst.addAll(crashRst);
			                //CrashTriggerMain.generateFailureInfo(restartRst, point, acceptedCrashNode, CUR_CRASH_NODE_NAME, restarted, "restart-failure");
			                rst.add(Stat.log("node "+fault.actualNodeIp+" was killed!"));
			                
			                Mutation.buildClusterStatus(currentCluster, fault.actualNodeIp, FaultStat.CRASH);
						} else if(fault.stat.equals(FaultStat.REBOOT)) {
							fault.actualNodeIp = fault.tarNodeIp;
							rst.add(Stat.log("Meet "+cur_Fault+"th fault point[REBOOT]:"+fault));
							
							//Restart the node
			        		rst.add(Stat.log("Prepare to restart node "+fault.actualNodeIp));
			                List<String> restartRst = cluster.restartNode(fault.actualNodeIp);
			                rst.addAll(restartRst);
			                //CrashTriggerMain.generateFailureInfo(restartRst, point, acceptedCrashNode, CUR_CRASH_NODE_NAME, restarted, "restart-failure");
			                rst.add(Stat.log("node "+fault.actualNodeIp+" was restarted!"));
				            
			                Mutation.buildClusterStatus(currentCluster, fault.actualNodeIp, FaultStat.REBOOT);
						} 
				    }
					faultInjected = true;
		         } catch(Exception e) {
		            System.out.println(e);
		         }
			}
		};
		fautInjectionThread.start();
	}

	public void stopController() {
		running = false;
		File file = favconfig.CUR_CRASH_FILE;
		if(file.exists()) {
			file.delete();
		}
		System.out.println("Controller was stopped.");
	}

	public void prepareFaultSeq(RandomFaultSequence p) {
		if(p == null || p.isEmpty()) {
			this.faultInjected = true;
			Stat.log("No faults to inject in this round.");
		}
		faultSequence = p;
		faultSequence.reset();
		updataCurCrashPointFile();
		Stat.log("Current fault sequence was prepared.");
	}

	public void updataCurCrashPointFile() {
		if(faultSequence == null || faultSequence.isEmpty()) {
			File file = favconfig.CUR_CRASH_FILE;
			if(file.exists()) {
			    file.delete();
				if(favconfig.UPDATE_CRASH != null) {
		            String path = favconfig.UPDATE_CRASH.getAbsolutePath();
		            String workingDir = path.substring(0, path.lastIndexOf("/"));
		            RunCommand.run(path, workingDir);
		        }
			}
		} else {
			File tofile = favconfig.CUR_CRASH_FILE;
			if(faultSequence != null && !faultSequence.isEmpty()) {
				if (!tofile.getParentFile().exists()) {
		            tofile.getParentFile().mkdirs();
		        }

				try {
					FileWriter fw = new FileWriter(tofile);
					BufferedWriter bw = new BufferedWriter(fw);
					PrintWriter pw = new PrintWriter(bw);

					for(RandomFaultPoint p:faultSequence.seq) {
						pw.write("fault point="+p.toString().hashCode()+"\n");
						pw.write("event="+p.stat+"\n");
						pw.write("pos="+p.pos+"\n");
						pw.write("nodeIp="+p.tarNodeIp+"\n");
						pw.write("waitTime="+p.waitTimeMillions+"\n");
						pw.write("end"+"\n");
					}
					
					pw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(favconfig.UPDATE_CRASH != null) {
                String path = favconfig.UPDATE_CRASH.getAbsolutePath();
                String workingDir = path.substring(0, path.lastIndexOf("/"));
                RunCommand.run(path, workingDir);
            }
		}
	}

    public int getRandom(int start,int end) {
    	int num = (int) (Math.random()*(end-start+1)+start);
		return num;
	}
    
    public static class AbortFaultException extends Exception {
		public AbortFaultException(String errorMessage) {
	        super(errorMessage);
	    }
	}
}
