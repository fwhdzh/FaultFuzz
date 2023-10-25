import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

	public static void requestServer(String serverIP, int serverPort) {
		try {
			Socket socket = new Socket(serverIP, serverPort);
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
			String clientMessage = "", serverMessage = "";
			clientMessage = "hello, server!";
			outStream.writeUTF("hello, server");
			// outStream.writeUTF(" server!");
			outStream.flush();
			// outStream.close();
			serverMessage = inStream.readUTF();
			System.out.println(serverMessage);
			// FileOutputStream out = new FileOutputStream("add-output@client", false);
			// out.write(serverMessage.getBytes());
			// out.flush();
			// out.close();
			outStream.close();
			outStream.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		String serverIP = args[0];
		int serverPort = Integer.parseInt(args[1]);
		requestServer(serverIP, serverPort);
	}

}
