import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketImpl;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoopTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String x = Long.toString(System.currentTimeMillis());  
		
		FileOutputStream out = new FileOutputStream("add-output-2341234@Test", false);
		FileOutputStream oout = new FileOutputStream(out.getFD());
		/*
		for(int i = 0; i< 2; i++) {
			out.write(x.getBytes());
		}
		oout.close();
		*/
	}
}


