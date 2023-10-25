package edu.iscas.tcse.bt;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import edu.iscas.tcse.favtrigger.instrumenter.annotation.Inject;
import edu.iscas.tcse.favtrigger.triggering.WaitToExec;

public class SocketCilent {

    private Socket clientSocket;

    @Inject
    public void startConnection(String ip, int port) throws IOException {
        System.out.println("Client begin connect");
        clientSocket = new Socket(ip, port);
        if (!clientSocket.isConnected()) {
            System.out.println("Client connect fail");
            return;
        }
        System.out.println("Client connect success");
        OutputStream out = clientSocket.getOutputStream();
        String hello = "Hello";
        byte[] buffer= hello.getBytes();
        // RecordTaint.recordFaultPoint("FAVMSG:127.0.0.1&1#1");
        // WaitToExec.checkFaultPoint("FAVMSG:127.0.0.1&1#1");
        WaitToExec.triggerAndRecordFaultPoint("FAVMSG:127.0.0.1&1#1");
        out.write(buffer);
        System.out.println("Client write finish");
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        SocketCilent client = new SocketCilent();
        client.startConnection("127.0.0.1", 12001);
    }


}
