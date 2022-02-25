package edu.iscas.CCrashFuzzer;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

import edu.iscas.CCrashFuzzer.Conf.MaxDownNodes;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultPoint;
import edu.iscas.CCrashFuzzer.FaultSequence.FaultStat;
//We do not trigger remote crash in this controller.
//This controller aims to trigger local crashes for systems deployed as processes in the same machine
public class Controller {
	public Cluster cluster;
	public boolean running;
	public Set<Thread> clients;
	public int CONTROLLER_PORT = 8888;
	public FaultSequence faultSequence; //store current crash point ID to the crash before point
    public Thread serverThread;
    public ServerSocket serverSocket;
    public boolean faultInjected;
    public ArrayList<String> rst;
    public Conf favconfig;
    List<MaxDownNodes> currentCluster = new ArrayList<MaxDownNodes>();

    public Controller(Cluster cluster, int port, Conf favconfig) {
    	this.cluster = cluster;
    	this.running = false;
    	this.CONTROLLER_PORT = port;
    	this.favconfig = favconfig;
    	this.faultInjected = false;
    	this.rst = new ArrayList<String>();
    	this.clients = Collections.synchronizedSet(new HashSet<Thread>());
    	currentCluster.addAll(favconfig.maxDownGroup);
    }

    public void startController() {
		running = true;
		serverThread = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
				    Stat.log("Controller port:"+CONTROLLER_PORT);
					serverSocket = new ServerSocket(CONTROLLER_PORT);
				    Stat.log("Controller started ...");
		            int counter = 0;
		            while(running){
		            	counter++;
		            	Socket socket = serverSocket.accept();  //server accept the client connection request
		            	//System.out.println("a client "+counter+" was connected"+socket.getRemoteSocketAddress());
		            	ClientHandler sct = new ClientHandler(socket,counter); //send  the request to a separate thread
		            	sct.start();
		            	clients.add(sct);
		            }
		            serverSocket.close();
		         } catch(Exception e) {
		            System.out.println(e);
		         }
			}
		};
		serverThread.start();
	}

	public void stopController() {
		running = false;
		try {
			serverSocket.close();
			for(Thread t:clients) {
				t.join();
			}
			serverThread.join();
		} catch (InterruptedException | IOException | NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File file = favconfig.CUR_CRASH_FILE;
		if(file.exists()) {
			file.delete();
		}
		System.out.println("Controller was stopped.");
	}

	public void prepareFaultSeq(FaultSequence p) {
		if(p == null || p.isEmpty()) {
			this.faultInjected = true;
			Stat.log("No faults to inject in this round.");
		}
		faultSequence = p;
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

			if (!tofile.getParentFile().exists()) {
	            tofile.getParentFile().mkdirs();
	        }

			try {
				FileWriter fw = new FileWriter(tofile);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);

				for(FaultPoint p:faultSequence.seq) {
					pw.write("fault point="+p.toString().hashCode()+"\n");
					pw.write("event="+p.stat+"\n");
					pw.write("pos="+p.pos+"\n");
					pw.write("nodeIp="+p.tarNodeIp+"\n");
					pw.write("ioID="+p.ioPt.ioID+"\n");
					pw.write("ioCallStack="+p.ioPt.CALLSTACK+"\n");
					pw.write("ioAppearIdx="+p.ioPt.appearIdx+"\n");
					pw.write("end"+"\n");
				}
				
				pw.close();

				if(favconfig.UPDATE_CRASH != null) {
                    String path = favconfig.UPDATE_CRASH.getAbsolutePath();
                    String workingDir = path.substring(0, path.lastIndexOf("/"));
                    RunCommand.run(path, workingDir);
                }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public class ClientHandler extends Thread {
	    final Socket socket;
		final int id;
		public ClientHandler(Socket socket,  int id) {
			this.socket = socket;
			this.id = id;
		}

		@Override
		public void run() {
			try{
				//System.out.println("ClientHandler "+id+" was started!"+socket.getLocalPort()+":"+socket.getRemoteSocketAddress());
				DataInputStream inStream = new DataInputStream(socket.getInputStream());
				DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
				ObjectInputStream objIn = new ObjectInputStream(inStream);
				int ioID = inStream.readInt();
				String reportNodeIp = inStream.readUTF();
				//System.out.println("ClientHandler-" +id+ ": msg is :"+mess);
				synchronized(faultSequence) {
					//System.out.println("Client "+id+" enter synchronized area: "+socket.getRemoteSocketAddress());
					if(!faultSequence.isEmpty()) {
						if(faultInjected) {
							//all faults have been tested
	                        outStream.writeUTF("CONTI");
	                        outStream.writeInt(faultSequence.curFault);
//							//System.out.println("Send continue response to client "+id+":"+socket.getRemoteSocketAddress());
							outStream.flush();
							inStream.close();
							outStream.close();
							socket.close();
						} else {
							FaultPoint p = faultSequence.seq.get(faultSequence.curFault);
							if(p.ioPt.ioID == ioID && faultSequence.curAppear < p.ioPt.appearIdx) {
								faultSequence.curAppear++;
								if(faultSequence.curAppear == p.ioPt.appearIdx) {
									faultSequence.curAppear = 0;
									faultSequence.curFault++;
									
									if(p.stat.equals(FaultStat.CRASH)) {
										p.actualNodeIp = reportNodeIp;
										for(int i = faultSequence.curFault; i< faultSequence.seq.size(); i++) {
											if(faultSequence.seq.get(i).stat.equals(FaultStat.REBOOT)
													&& faultSequence.seq.get(i).tarNodeIp.equals(p.tarNodeIp)) {
												faultSequence.seq.get(i).actualNodeIp = reportNodeIp;
												break;
											}
										}
										
										rst.add(Stat.log("Meet current fault point:"+p));
										
										outStream.writeUTF("CRASH");
										outStream.writeInt(faultSequence.curFault);
//										//System.out.println("Send continue response to client "+id+":"+socket.getRemoteSocketAddress());
										outStream.flush();
										inStream.close();
										outStream.close();
										socket.close();
										
										String[] args = new String[2];
										args[0] = p.actualNodeIp;
										args[1] = String.valueOf(favconfig.AFL_PORT);
										AflCli.main(args);
										
										//Restart the node
						        		rst.add(Stat.log("Prepare to crash node "+p.actualNodeIp));
						                List<String> crashRst = cluster.killNode(p.actualNodeIp, p.actualNodeIp);
						                rst.addAll(crashRst);
						                //CrashTriggerMain.generateFailureInfo(restartRst, point, acceptedCrashNode, CUR_CRASH_NODE_NAME, restarted, "restart-failure");
						                rst.add(Stat.log("node "+p.actualNodeIp+" was killed!"));
						                
						                Mutation.buildClusterStatus(currentCluster, p.actualNodeIp, FaultStat.CRASH);
									} else if(p.stat.equals(FaultStat.REBOOT)) {
										rst.add(Stat.log("Meet current fault point:"+p));
										
										//Restart the node
						        		rst.add(Stat.log("Prepare to restart node "+p.actualNodeIp+" before continue on node "+reportNodeIp));
						                List<String> restartRst = cluster.restartNode(p.actualNodeIp);
						                rst.addAll(restartRst);
						                //CrashTriggerMain.generateFailureInfo(restartRst, point, acceptedCrashNode, CUR_CRASH_NODE_NAME, restarted, "restart-failure");
						                rst.add(Stat.log("node "+p.actualNodeIp+" was restarted!"));
							            
						                outStream.writeUTF("CONTI");
										outStream.writeInt(faultSequence.curFault);
//										//System.out.println("Send continue response to client "+id+":"+socket.getRemoteSocketAddress());
										outStream.flush();
										inStream.close();
										outStream.close();
										socket.close();
										
						                Mutation.buildClusterStatus(currentCluster, p.actualNodeIp, FaultStat.REBOOT);
									} else {
										//no need to inject faults
				                        outStream.writeUTF("CONTI");
										outStream.writeInt(faultSequence.curFault);
//										//System.out.println("Send continue response to client "+id+":"+socket.getRemoteSocketAddress());
										outStream.flush();
										inStream.close();
										outStream.close();
										socket.close();
									}
									
									if(faultSequence.curFault >= faultSequence.seq.size()) {
										faultInjected = true;
									}
								} else {
									//not the expected appear idx
			                        outStream.writeUTF("CONTI");
									outStream.writeInt(faultSequence.curFault);
//									//System.out.println("Send continue response to client "+id+":"+socket.getRemoteSocketAddress());
									outStream.flush();
									inStream.close();
									outStream.close();
									socket.close();
								}
							} else {
								//not the expected io ID
		                        outStream.writeUTF("CONTI");
								outStream.writeInt(faultSequence.curFault);
//								//System.out.println("Send continue response to client "+id+":"+socket.getRemoteSocketAddress());
								outStream.flush();
								inStream.close();
								outStream.close();
								socket.close();
							}
						}
					} else {
//					    rst.add(Stat.log("Controller already has an accepted node id is "+acceptedCrashNode));
                        outStream.writeUTF("CONTI");
						outStream.writeInt(faultSequence.curFault);
//						//System.out.println("Send continue response to client "+id+":"+socket.getRemoteSocketAddress());
						outStream.flush();
						inStream.close();
						outStream.close();
						socket.close();
					}
				}
		    } catch(Exception ex) {
		    	System.out.println(ex);
		    } finally {
				clients.remove(this);
				//System.out.println("ClientHandler-" + id + " exit!! ");
		    }
		}
	}

    public int getRandom(int start,int end) {
    	int num = (int) (Math.random()*(end-start+1)+start);
		return num;
	}
}
