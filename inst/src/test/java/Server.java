import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

public class Server {
	public static String initTime = "";

	public static void runServer(int serverPort) {
		try {
			ServerSocket server = new ServerSocket(serverPort);
			int counter = 0;
			System.out.println("Server Started ....");
			while (true) {
				counter++;
				Socket serverClient = server.accept(); // server accept the client connection request
				System.out.println(" >> " + "Client No:" + counter + " started!");
				ClientHandler sct = new ClientHandler(serverClient, counter); // send the request to a separate thread
				sct.start();
				if (counter == 2) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Start server....");
		// initTime = Long.toString(System.currentTimeMillis());
		// FileOutputStream out = new FileOutputStream("add-output@Test", false);
		// out.write(("Server:"+initTime).getBytes());
		// out.close();

		int serverPort = Integer.parseInt(args[0]);
		runServer(serverPort);
	}

	public static class ClientHandler extends Thread {
		final Socket serverClient;
		final int clientNo;

		public ClientHandler(Socket socket, int id) {
			this.serverClient = socket;
			this.clientNo = id;
		}

		public void run() {
			try {
				DataInputStream inStream = new DataInputStream(serverClient.getInputStream());
				DataOutputStream outStream = new DataOutputStream(serverClient.getOutputStream());
				String clientMessage = "", serverMessage = "";
				clientMessage = inStream.readUTF();
				System.out.println("From Client-" + clientNo + ": msg is :" + clientMessage);
				serverMessage = "From Server to Client-" + clientNo + ":" + initTime;
				outStream.writeUTF(serverMessage);
				outStream.flush();
				inStream.close();
				outStream.close();
				serverClient.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println("Client -" + clientNo + " exit!! ");
			}
		}

	}

	@Test
	public void testServer() throws InterruptedException {
		
		/*
		 * Assign a new thread to run Server
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Start server....");
				int serverPort = 12091;
				runServer(serverPort);
			}
		}).start();

		Thread.sleep(2000);

		/*
		 * Assign a new thread to run Client
		 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("Start client....");
				String serverIP = "172.24.58.115";
				int serverPort = 12091;
				Client.requestServer(serverIP, serverPort);
			}
		}).start();
	}
}
