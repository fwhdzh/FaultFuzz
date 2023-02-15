import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDPServer {
	public static void main(String[] args) throws Exception {
		// 使用Socket来接收
		String initTime = Long.toString(System.currentTimeMillis());
		String data = "";
		DatagramSocket socket = new DatagramSocket();
		while (true) {
			// 准备发送包裹，从键盘接收数据
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			// 读取一行
			String line = reader.readLine();
			data = initTime+":"+data+":"+line;
			if("bye".equals(line)) {
				data = line;
			}
			byte[] dataBytes = data.getBytes();
			DatagramPacket packet = new DatagramPacket(dataBytes, dataBytes.length, new InetSocketAddress("127.0.0.1", 6666));
			// 发送
			socket.send(packet);
			// 什么时候退出
			if ("bye".equals(line)) {
				break;
				}
			}
		socket.close();
		}
}
