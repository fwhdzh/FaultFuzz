import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;  
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.iscas.tcse.favtrigger.instrumenter.CoverageMap;
import edu.iscas.tcse.favtrigger.instrumenter.TriggerEvent;
import edu.iscas.tcse.favtrigger.tracing.FAVEntry;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;  
  
public class Simple {
	static List<String> strs;
	public static class Data {
		String s;
		Float t;
		Double x;
		float tt;
		double xx;
		int i;
		public Data(String s) {
			this.s = s;
		}
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int x = 16;
		int y = (1 << x);//1左移16位
		System.out.println(y);
		
		System.out.println(y >> 3);//8个byte组成一组, 8192组，64位计算机
		System.out.println(y >> 2);//4个byte组成一组, 16384组，32位计算机，为了提高读取速度，程序每次读一组数据进行比较
		System.out.println(y >> 1);
		Random rand = new Random(87978234);//加了seed后，每次生成的随机数序列是确定的
		System.out.println("******************");
		System.out.println(rand.nextInt(10));
		System.out.println(rand.nextInt(10));
		System.out.println(rand.nextInt(10));
		System.out.println(rand.nextInt(10));
		System.out.println(rand.nextInt(10));
		System.out.println(rand.nextInt(10));
		
		File f = new File("test1/test");
		System.out.println(f.getParentFile().getAbsolutePath());
	}
	public static void test(Data d) {
		
	}
	public static String getServerName(String hostName, int port, long startcode) {
//	    return hostName+ "." + port + "." + startcode;
		String x = Long.toString(System.currentTimeMillis())+Long.toString(System.currentTimeMillis());
		Long ll = System.currentTimeMillis();
		return  x;
	  }
}  
 