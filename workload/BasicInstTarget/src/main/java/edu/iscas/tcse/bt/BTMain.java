package edu.iscas.tcse.bt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.iscas.tcse.favtrigger.instrumenter.annotation.Inject;
import edu.iscas.tcse.favtrigger.triggering.WaitToExec;

public class BTMain {

    static int record = 0;

    @Inject
    public int add(int a, int b) {
        int result = a + b;
        return result;
    }

    @Inject
    public static int mul(int a, int b) {
        int result = a * b;
        return result;
    }

    public static void printSome() {
        // System.out.println("FWH print some in FWHMethodVisitor!");
    }

    public static void printFlag() {

    }

    // public static native void write(int b, boolean append) throws IOException;
    // private native void writeBytes(byte b[], int off, int len, boolean append)
    // throws IOException;

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Hello Phosphor");

        Thread.sleep(10000);

        int a = 2;
        int b = a + 1;
        int c = mul(a, b);
        record = c;
        System.out.println(c);
        System.out.println(mul(b, c));
        String filePath = "/data/fengwenhan/data/faultfuzz_bt/1.txt";
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream outputStream = new FileOutputStream(file);
        // RecordTaint.recordFaultPoint("/home/fengwenhan/data/phosphor_test/test_out/1.txt");
        // WaitToExec.checkFaultPoint("/home/fengwenhan/data/phosphor_test/test_out/1.txt");
        WaitToExec.triggerAndRecordFaultPoint(filePath);
        
        outputStream.write(54); // ASCII for 6

        Thread thread = new SocketServer(12001);
        thread.start();

        Thread.sleep(1000);

        SocketCilent client = new SocketCilent();
        client.startConnection("127.0.0.1", 12001);

        Thread.sleep(5000);

        printFlag();

        Thread.sleep(5000);

        long timeInternal = 5000;
        long seconds = 0;
        long secondsLimit = 1200;
        while (true) {
            try {
                Thread.sleep(timeInternal);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            seconds += timeInternal / 1000;
            System.out.println("Main thread is running! " + seconds + " seconds passed!");
            if (seconds > secondsLimit) {
                break;
            }
        }

    }
}
