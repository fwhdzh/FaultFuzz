import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPClient {
	 
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(6666);
        System.out.println(socket.getPort());
        System.out.println(socket.getLocalPort());
        while (true) {
            // 准备接收数据包裹
            byte[] buffer = new byte[1024];
            // 用来接收数据
            DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
            // 接收包裹，阻塞时接收
            socket.receive(packet);
            //packet.getLength() 将会返回接收内容后的length长度
            // 接收到的数据
            String receiveData = new String(packet.getData()).trim();
            FileOutputStream out = new FileOutputStream("add-output@Test", false);
    		out.write(receiveData.getBytes());
    		out.close();
            // 打印到控制台
            System.out.println(receiveData);
            // 什么时候退出
            if ("bye".equals(receiveData)) {
                break;
            }
        }
 
        // 关闭
        socket.close();
    }
}