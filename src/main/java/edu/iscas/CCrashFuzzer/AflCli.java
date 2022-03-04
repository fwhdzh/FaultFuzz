package edu.iscas.CCrashFuzzer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class AflCli {

	public static void main(String[] args) throws AflException {
		// TODO Auto-generated method stub
		if(args.length < 3) {
			System.out.println("Please specify afl port! [ip] [port] [command:SAVE|STABLE]:"+Arrays.asList(args));
			return;
		}
		String serverIp = args[0].trim();
		int serverPort = Integer.parseInt(args[1].trim());
		String command = args[2].trim();
		if(!command.equals(AflCommand.SAVE.toString()) && !command.equals(AflCommand.STABLE.toString())) {
			System.err.println("Illegal command, should be [SAVE|STABLE]");
			return;
		}
		try{
		    Socket socket = new Socket(serverIp,serverPort);
		    DataInputStream inStream = new DataInputStream(socket.getInputStream());
		    DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
		    String clientMessage = "",serverMessage = "";
		    clientMessage = command;

		    System.out.println("AflCli send to "+serverIp+":"+clientMessage);
		    outStream.writeUTF(clientMessage);//request to save results
		    outStream.flush();

		    serverMessage = inStream.readUTF();//get notified
		    System.out.println("From "+serverIp+", AflCli receive:"+serverMessage);
		    outStream.close();
		    inStream.close();
		    socket.close();
		    if(!serverMessage.equals(AflCommand.FINISH.toString())) {
		    	throw new AflException("Save result is not FINISH:"+serverMessage);
		    }
		  } catch(IOException e){
			  e.printStackTrace();
		  } finally {
			  System.out.println("AflCli to "+serverIp+" exit!");
		  }
	}

	public static enum AflCommand {
		SAVE, STABLE,//command
		FINISH, //succ response
		TMOUT, //timeout response
		ILLEGAL //Illegal command
	}

	public static class AflException extends Exception {
		public AflException(String errorMessage) {
	        super(errorMessage);
	    }
	}
}
