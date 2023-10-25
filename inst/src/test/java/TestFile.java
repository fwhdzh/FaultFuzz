import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.taint.SpecialLabel;
import edu.iscas.tcse.favtrigger.tracing.FAVEntry;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;

public class TestFile {
    public static class Data implements Serializable {
        String v;
        public String toString() {
            return v;
        }
    }

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		String x = Long.toString(System.currentTimeMillis())+Long.toString(System.currentTimeMillis());

		FileOutputStream out = new FileOutputStream("add-output@Test", false);
		FileOutputStream oout = new FileOutputStream(out.getFD());
		//out.write(x.getBytes());
		//oout.write(x.getBytes());
		//oout.write(x.getBytes());
		//oout.close();
		System.out.println(RecordTaint.getCallStack(Thread.currentThread()));
		oout.write(x.getBytes());
		oout.close();
		
		File f = new File("add-output@Test");
		FileInputStream inStream = new FileInputStream(f);
		byte[] content = new byte[1024];
		inStream.read(content);
		System.out.println(new String(content));
		inStream.close();
		
		/*
		StackTraceElement[] callStack;
    	callStack = Thread.currentThread().getStackTrace();
    	List<String> callStackString = new ArrayList<String>();
    	for(int i = 0; i < callStack.length; ++i) {
    		callStackString.add(callStack[i].toString());
    	}
    	Data entry = new Data();
		entry.v = "path";
		ObjectOutputStream objOut = new ObjectOutputStream(oout);
		objOut.writeObject(entry);
		objOut.flush();
		File f = new File("add-output@Test");
		FileInputStream inStream = new FileInputStream(f);
		ObjectInputStream objIn = new ObjectInputStream(inStream);
		Data readentry = (Data) objIn.readObject();
		System.out.println(readentry.v);
		
        inStream.close();
        out.close();
        */
        f.delete();
        System.out.println(f.exists());
	}
	public static int getLineNumber() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];
        System.out.println(stacktrace[0].getClassName());
        System.out.println(stacktrace[1].getClassName());
        int line = e.getLineNumber();
        return line;
    }
}


