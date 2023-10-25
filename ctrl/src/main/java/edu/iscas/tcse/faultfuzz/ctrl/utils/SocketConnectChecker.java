package edu.iscas.tcse.faultfuzz.ctrl.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.iscas.tcse.faultfuzz.ctrl.AflCli;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflCommand;
import edu.iscas.tcse.faultfuzz.ctrl.AflCli.AflException;

public class SocketConnectChecker {

    private void work() throws UnknownHostException, IOException {

        String serverIp = "172.30.0.6";
        int serverPort = 12081;
        String command = "STABLE";

        Socket socket = new Socket(serverIp, serverPort);
        DataInputStream inStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        String clientMessage = "", serverMessage = "";
        clientMessage = command;

        System.out.println("AflCli send to " + serverIp + ":" + clientMessage);
        outStream.writeUTF(clientMessage);// request to save results
        outStream.flush();

        serverMessage = inStream.readUTF();// get notified
        System.out.println("From " + serverIp + ", AflCli receive:" + serverMessage);
        outStream.close();
        inStream.close();
        socket.close();
    }

    private void helloWorld() {
        // List<String> acdList = Arrays.asList(AflCommand.values());
        // List<String> acdList =
        // Stream.of(AflCommand.values()).map(AflCommand::name).collect(Collectors.toList());
        // Stat.log(JSONObject.toJSONString(acdList));
        String[] args = new String[3];
        args[0] = "172.30.0.6";
        args[1] = String.valueOf(12081);
        args[2] = AflCommand.HEARTBEAT.toString();
        try {
            AflCli.interactWithNode(args);
        } catch (AflException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        SocketConnectChecker sc = new SocketConnectChecker();
        // sc.work();

        sc.helloWorld();
    }
}
