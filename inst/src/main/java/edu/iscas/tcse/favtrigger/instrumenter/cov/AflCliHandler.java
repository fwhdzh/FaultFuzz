package edu.iscas.tcse.favtrigger.instrumenter.cov;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.favtrigger.MyLogger;;

public class AflCliHandler extends Thread {
    final Socket serverClient;
	final int clientNo;
	public AflCliHandler(Socket socket,  int id) {
		this.serverClient = socket;
		this.clientNo = id;
	}

	public void run() {
	    try{
	      DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
	      DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
	      String clientMessage = "", serverMessage = "";
	      clientMessage = inStream.readUTF();
	      System.out.println("From AFL Client-" +clientNo+ ": msg is :"+clientMessage);

	      serverMessage = AflCommand.FINISH.toString();
	      if(clientMessage.equals(AflCommand.SAVE.toString())) {
	    	  try{
		    	  JavaAfl.save_result();
		      } catch (Exception e) {
		    	  serverMessage = e.getMessage();
		    	  e.printStackTrace();
		      }
	      } else if (clientMessage.equals(AflCommand.STABLE.toString())) {
	    	  try{
		    	  boolean rst = JavaAfl.waitStable();
		    	  if(!rst) {
		    		  serverMessage = AflCommand.TMOUT.toString();
		    	  }
		      } catch (Exception e) {
		    	  serverMessage = e.getMessage();
		    	  e.printStackTrace();
		      }
	      } else if (clientMessage.equals(AflCommand.HEARTBEAT.toString())) {
				// serverMessage = AflCommand.HEATBEAT.toString();
				MyLogger.log("Recieve command HEARTBEAT");
				JavaAfl.ready = true;
		  } else if (clientMessage.equals(AflCommand.NOTREPLAY.toString())) {
				MyLogger.log("Recieve command NOTREPLAY");
				Configuration.REPLAY_NOW = false;
		  } else if (clientMessage.equals(AflCommand.DOREPLAY.toString())) {
				MyLogger.log("Recieve command DOREPLAY");
				Configuration.REPLAY_NOW = true;
		  } else if (clientMessage.equals(AflCommand.DETERMINE_CONTROL.toString())) {
				MyLogger.log("Recieve command DETERMINE_CONTROL");
				Configuration.DETERMINE_STATE = 2;
		  } else if (clientMessage.equals(AflCommand.DETERMINE_NORMAL.toString())) {
				MyLogger.log("Recieve command DETERMINE_NORMAL");
				Configuration.DETERMINE_STATE = 1;
		  } else if (clientMessage.equals(AflCommand.DETERMINE_NO_SEND.toString())) {
				MyLogger.log("Recieve command DETERMINE_NO_SEND");
				Configuration.DETERMINE_STATE = 0;
		  }
		  else {
	    	  serverMessage = "Illegal command: "+clientMessage;
	      }

	      //notify client
	      System.out.println("To AFL Client -" + clientNo + ":"+serverMessage);
	      outStream.writeUTF(serverMessage);
	      outStream.flush();
	      inStream.close();
	      outStream.close();
	      serverClient.close();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    } finally {
	      System.out.println("AFL Client -" + clientNo + " exit!! ");
	    }
	  }
}
