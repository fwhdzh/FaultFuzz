import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static void main(String[] args) throws IOException  
    { 
		try{
		    Socket socket=new Socket(args[0],Integer.parseInt(args[1]));
		    DataInputStream inStream=new DataInputStream(socket.getInputStream());
		    DataOutputStream outStream=new DataOutputStream(socket.getOutputStream());
		    String clientMessage="",serverMessage="";
		    clientMessage = "hello, server!";
		      outStream.writeUTF("hello, server");
		      //outStream.writeUTF(" server!");
		      outStream.flush();
		      //outStream.close();
		      serverMessage=inStream.readUTF();
		      System.out.println(serverMessage);
		      //FileOutputStream out = new FileOutputStream("add-output@client", false);
		      //out.write(serverMessage.getBytes());
		      //out.flush();
		      //out.close();
		    outStream.close();
		    outStream.close();
		    socket.close();
		  }catch(Exception e){
			  e.printStackTrace();
		  }
    } 


}
