import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;  
import java.net.DatagramSocket;  
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import edu.iscas.tcse.favtrigger.instrumenter.TriggerEvent;
import edu.iscas.tcse.favtrigger.tracing.FAVEntry;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;  
  
public class ByteBufferTest {
	static List<String> strs;
	public static class Data {
		String s;
	}
	/*
	public static void main(String[] args) throws CloneNotSupportedException, IOException {
		
		String s = "hello";
		byte[] bbb = s.getBytes();
		System.out.println(bbb.hashCode());
		
		ByteBuffer bytes = ByteBuffer.allocate(100);
		bytes.put(s.getBytes());
		bytes.put(" world!".getBytes());
		System.out.println(bytes.position());
		System.out.println(bytes.limit());
		
		System.out.println("**************************");
		bytes.flip();
		System.out.println(bytes.position());
		System.out.println(bytes.limit());
		System.out.println("**************************");
		bytes.position(3);
		System.out.println(bytes.position());
		System.out.println(bytes.limit());
		System.out.println("**************************");
		
		FileOutputStream out = new FileOutputStream("add-output@Test");
		out.getChannel().write(bytes);
		System.out.println(out.getFD());
		out.write(bytes.array());
		out.close();

		System.out.println(bytes.position());
		System.out.println(bytes.limit());
		
		bytes.flip();
		System.out.println("**************************");
		System.out.println(bytes.position());
		System.out.println(bytes.limit());
		File file = new File("./add-output@Test");
		

		FileInputStream is = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file);
        FileChannel inf = is.getChannel();
        FileChannel outf = fos.getChannel();
        
        bytes.flip();
        System.out.println(bytes.position());
		System.out.println(bytes.limit());
        int rst = outf.write(bytes);
        System.out.println(bytes.position());
		System.out.println(bytes.limit());
        System.out.println(rst);
        outf.close();

        ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);
        System.out.println(buf.position());
		System.out.println(buf.limit());
        int rst2 = inf.read(buf);
        System.out.println(buf.position());
		System.out.println(buf.limit());
        System.out.println(rst2);
        inf.close();
        

        System.out.println(new String(buf.array()));
		test();
     }  
     */
	
	public static void test() {
		int len = 10;
		int[] bytes = new int[len];
		int[] bytes2 = new int[len];
		int[] ar = new int[0];
		Data d = new Data();
		d.s = "hi!";
		Object obj = d;
		Data s = (Data) obj;
		System.out.println(s.s);
		for(int i = 0; i< 10; i++) {
			bytes[i] = bytes2[i];
		}
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		List<Integer> x = new ArrayList<Integer>();
		x.add(99);
		int num = Thread.currentThread().getStackTrace()[1].getLineNumber();
		System.out.println(num);
		int rst = 100 - x.get(0);
		 num = Thread.currentThread().getStackTrace()[1].getLineNumber();
		System.out.println(num);
		//write(time);
	}

	public static void write(String s) throws IOException {
		FileOutputStream out = new FileOutputStream("add-output@Test", false);
		out.write(s.getBytes());
		out.close();
	}
}  
 