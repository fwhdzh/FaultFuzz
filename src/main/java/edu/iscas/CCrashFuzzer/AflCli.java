package edu.iscas.CCrashFuzzer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class AflCli {

	public static void main(String[] args) throws AflException {
		// TODO Auto-generated method stub
		if(args.length < 1) {
			System.out.println("Please specify afl port! [ip] [port]");
			return;
		}
		String serverIp = "127.0.0.1";
		int serverPort;
		if(args.length == 1) {
			serverPort = Integer.parseInt(args[0]);
		} else {
			serverIp = args[0].trim();
			serverPort = Integer.parseInt(args[1]);
		}
		try{
		    Socket socket = new Socket(serverIp,serverPort);
		    DataInputStream inStream = new DataInputStream(socket.getInputStream());
		    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
		    String clientMessage = "",serverMessage = "";
		    clientMessage = "SAVE";

		    System.out.println("AflCli send to "+serverIp+":"+clientMessage);
		    outStream.writeUTF(clientMessage);//request to save results
		    outStream.flush();

		    serverMessage = inStream.readUTF();//get notified
		    System.out.println("From "+serverIp+", AflCli receive:"+serverMessage);
		    if(!serverMessage.equals("FINISH")) {
		    	throw new AflException("Save result is not FINISH:"+serverMessage);
		    }
		    outStream.close();
		    inStream.close();
		    socket.close();
		  } catch(IOException e){
			  e.printStackTrace();
		  } finally {
			  System.out.println("AflCli to "+serverIp+" exit!");
		  }
	}

	public static class AflException extends Exception {
		public AflException(String errorMessage) {
	        super(errorMessage);
	    }
	}
}
