package edu.iscas.tcse.bt;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import edu.iscas.tcse.favtrigger.triggering.WaitToExec;

public class SocketServer extends Thread {

    private int port;

    public SocketServer(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        startServer(port);
    }

    private void startServer(int port) throws IOException {
        System.out.println("The server waiting...");
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket = serverSocket.accept();
        System.out.println("has connected to client");
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[100];
        // RecordTaint.recordFaultPoint("FAVMSG:READ127.0.0.1&1#1");
        // WaitToExec.checkFaultPoint("FAVMSG:READ127.0.0.1&1#1");
        WaitToExec.triggerAndRecordFaultPoint("FAVMSG:READ127.0.0.1&1#1");
        int len = in.read(buffer);
        System.out.println(new String(buffer, 0, len));

        in.close();

        
        
        socket.close();
    }

    @Override
    public void run() {
        super.run();
        try {
            startServer(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        SocketServer server = new SocketServer(12001);
        server.startServer();
        // Main.printFlag();
    }
}
