package edu.iscas.tcse.faultfuzz.ctrl;

import java.io.IOException;

import org.junit.Test;

public class ConfTest {

    @Test
    public void testLoadConfiguration() throws IOException {
        // String filePath =
        // this.getClass().getResource("ConfTest.class").getPath().split("/ConfTest.class")[0]
        // + "/confTest.properties";
        // Stat.log(filePath);
        // File f = new File(filePath);
        // if (f.exists()) {
        // Conf conf = new Conf(f);
        // conf.loadConfiguration();
        // Assert.assertEquals(conf.AFL_PORT, 12081);
        // Assert.assertEquals(conf.RECOVERY_MODE, false);
        // Assert.assertEquals(conf.RECOVERY_FUZZINFO_PATH,
        // "/data/fengwenhan/data/crashfuzz_fwh/FuzzInfo.txt");
        // Assert.assertEquals(2, Conf.faultTypeList.size());
        // Assert.assertEquals(FaultType.CRASH, Conf.faultTypeList.get(0));
        // Assert.assertEquals(FaultType.NETWORK_DISCONNECTION,
        // Conf.faultTypeList.get(1));
        // Assert.assertEquals(12090, conf.CONTROLLER_PORT);
        // }
    }

    @Test
    public void test12100() {
        // try {
        //     ServerSocket serverSocket = new ServerSocket(12100);
        //     System.out.println("Server is running. Waiting for a connection...");

        //     Thread clientThread = new Thread(() -> {
        //         try {
        //             Thread.sleep(3000);
        //             Socket socket = new Socket("39.104.112.98", 12100);
        //             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        //             out.println("Hello World");
        //             out.close();
        //             socket.close();
        //         } catch (IOException | InterruptedException e) {
        //             e.printStackTrace();
        //         }
        //     });

        //     clientThread.start();

        //     Socket clientSocket = serverSocket.accept();
        //     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //     String message = in.readLine();
        //     System.out.println("Received message from client: " + message);
        //     in.close();
        //     clientSocket.close();
        //     serverSocket.close();

        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }
}
