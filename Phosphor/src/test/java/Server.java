import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.iscas.tcse.favtrigger.instrumenter.TriggerEvent;

public class Server {
	public static String initTime = "";
	public static void main(String[] args) throws IOException  
    { 
		System.out.println("Start server....");
		//initTime = Long.toString(System.currentTimeMillis());
		//FileOutputStream out = new FileOutputStream("add-output@Test", false);
		//out.write(("Server:"+initTime).getBytes());
		//out.close();
          
        try{
            ServerSocket server=new ServerSocket(Integer.parseInt(args[0]));
            int counter=0;
            System.out.println("Server Started ....");
            while(true){
              counter++;
              Socket serverClient=server.accept();  //server accept the client connection request
              System.out.println(" >> " + "Client No:" + counter + " started!");
              ClientHandler sct = new ClientHandler(serverClient,counter); //send  the request to a separate thread
              sct.start();
              if(counter == 2) {
            	  break;
              }
            }
          }catch(Exception e){
        	  e.printStackTrace();
          }
    } 


	public static class ClientHandler extends Thread {
	    final Socket serverClient; 
		final int clientNo;
		public ClientHandler(Socket socket,  int id) {
			this.serverClient = socket;
			this.clientNo = id;
		}
		
		public void run(){
		    try{
		      DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
		      DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
		      String clientMessage="", serverMessage="";
		      clientMessage=inStream.readUTF();
		        System.out.println("From Client-" +clientNo+ ": msg is :"+clientMessage);
		        serverMessage="From Server to Client-" + clientNo + ":" + initTime;
		        outStream.writeUTF(serverMessage);
		        outStream.flush();
		      inStream.close();
		      outStream.close();
		      serverClient.close();
		    }catch(Exception e){
		    	e.printStackTrace();
		    }finally{
		      System.out.println("Client -" + clientNo + " exit!! ");
		    }
		  }

	}
}
