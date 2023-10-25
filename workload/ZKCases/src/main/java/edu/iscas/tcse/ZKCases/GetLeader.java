package edu.iscas.tcse.ZKCases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class GetLeader {

	static String leader = null;
	static String failSH;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//ip:port;ip:port
		if(args.length<2) {
			return;
		}
		failSH = args[1];
		int waitMnts = 10;
		if(args.length >=3) {
			waitMnts = Integer.parseInt(args[2].trim());
		}
		String[] secs = args[0].trim().split(",");
		HashMap<String,Integer> ipToPort = new HashMap<String,Integer>();
		for(String sec:secs) {
			String[] eles = sec.trim().split(":");
			ipToPort.put(eles[0].trim(), Integer.parseInt(eles[1].trim()));
//			System.out.println("Get node "+eles[0].trim()+" "+Integer.parseInt(eles[1].trim()));
		}
		System.out.println("Polling nodes to find current leader ...");
		Thread findLeader = new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(leader == null) {
					try {
						Thread.currentThread().sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					for(String ip:ipToPort.keySet()) {
						String status = "";
						try {
							status = fourLetterWord(ip, ipToPort.get(ip));
						} catch (IOException e) {
							// TODO Auto-generated catch block
//							e.printStackTrace();
						}
						if(status.equals("leader")) {
							leader = ip;
							break;
						}
					}
				}
			}
			
		};
		findLeader.start();
		
		int spend = 0;
		while(leader == null) {
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			spend++;
			if(spend > (waitMnts*60)) {
				reportFailure("The leader does not online in "+waitMnts+" minutes!");
				findLeader.interrupt();
				System.exit(-1);
			}
		}
		System.out.println("Current leader is "+leader);
	}

	public static String fourLetterWord(String host, int port) throws IOException {
		String cmd = "srvr";
		String status = "";
		int timeout = 5000;
//		System.out.println("connecting to {} {}"+host+" "+port);
        Socket sock = null;
        InetSocketAddress hostaddress = host != null
            ? new InetSocketAddress(host, port)
            : new InetSocketAddress(InetAddress.getByName(null), port);
            sock = new Socket();
            sock.connect(hostaddress, timeout);
            sock.setSoTimeout(timeout);
        BufferedReader reader = null;
        try {
            OutputStream outstream = sock.getOutputStream();
            outstream.write(cmd.getBytes());
            outstream.flush();

            sock.shutdownOutput();

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
            	if (line.indexOf("Mode: ") != -1) {
					status = line.replaceAll("Mode: ", "").trim();
					System.out.println(host+" is:"+status);
				}
            }
            return status;
        } catch (SocketTimeoutException e) {
            throw new IOException("Exception while executing four letter word: " + cmd, e);
        } finally {
            sock.close();
            if (reader != null) {
                reader.close();
            }
        }
	}
	public static ArrayList<String> reportFailure(String info) {
		if(failSH != null) {
			String path = failSH;
			String workingDir = path.substring(0, path.lastIndexOf("/"));
			return RunCommand.run(path+" \""+info+"\"", workingDir);
			//return RunCommand.run(path);
		} else {
			return null;
		}
	}
}
