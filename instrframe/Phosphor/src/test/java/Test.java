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

public class Test {
    public static class Data{
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
		StackTraceElement[] callStack;
    	callStack = Thread.currentThread().getStackTrace();
    	List<String> callStackString = new ArrayList<String>();
    	for(int i = 0; i < callStack.length; ++i) {
    		callStackString.add(callStack[i].toString());
    	}
		FAVEntry entry = new FAVEntry();
		entry.PATH = "path";
		entry.CALLSTACK = callStackString;
		entry.ip = "crashNode";
		ObjectOutputStream objOut = new ObjectOutputStream(oout);
		objOut.writeObject(entry);
		objOut.flush();
		File f = new File("add-output@Test");
		FileInputStream inStream = new FileInputStream(f);
		ObjectInputStream objIn = new ObjectInputStream(inStream);
		FAVEntry readentry = (FAVEntry) objIn.readObject();
		System.out.println(readentry.PATH);
		System.out.println(readentry.CALLSTACK);
		System.out.println(readentry.ip);
		
		InetAddress ip4 = Inet4Address.getLocalHost();
        //return ip4.getHostAddress();
        System.out.println(ip4.toString());
        System.out.println(ip4.getHostName());
        System.out.println(ip4.getCanonicalHostName());
        System.out.println(ip4.getHostAddress());
        System.out.println(ip4.toString().replace("/", "|").replace("|", "/"));
        inStream.close();
        out.close();
        f.delete();
        System.out.println(f.exists());
        
        Data d1 = new Data();
        d1.v = "hello";
        
        Data d2 = new Data();
        d2.v = "world";
        Taint t1 = Taint.withLabel(d1);
        Taint t2 = Taint.withLabel(d2);
        Taint t3 = Taint.withLabel("gy");
        Taint t4 = null;
        System.out.println(t1.toString());
        System.out.println(Taint.combineTags(t1, t2));
        System.out.println(Taint.combineTags(t1, t3));
        System.out.println(Taint.combineTags(t1, t4));
        Taint[] actualTaints = new Taint[3];
        Taint rst = Taint.combineTaintArray(actualTaints);
        System.out.println(rst.toString());
        System.out.println(Taint.emptyTaint());
        Taint t5 = Taint.withLabel(SpecialLabel.CONSTANT);
        Taint t6 = Taint.withLabel(SpecialLabel.CONSTANT);
        System.out.println(Taint.combineTags(t5, t6));
        SpecialLabel[] only = new SpecialLabel[] {SpecialLabel.CONSTANT};
        System.out.println(t5);
        System.out.println(t5.containsLabel(SpecialLabel.CONSTANT));
        System.out.println(t5.containsOnlyLabels(only));
        
        System.out.println(getLineNumber());
        
        String mes1 = Long.toString(System.currentTimeMillis());
        byte[] mesb1 = mes1.getBytes();
        String md5mes1 = RecordTaint.getMD5HashForBytes(mesb1,0,mesb1.length);
        System.out.println(md5mes1);
        System.out.println(md5mes1.getBytes().length);
        String mes2 = Long.toString(System.currentTimeMillis());
        byte[] mesb2 = mes2.getBytes();
        String md5mes2 = RecordTaint.getMD5HashForBytes(mesb2,0,mesb2.length);
        System.out.println(md5mes2);
        System.out.println(md5mes2.getBytes().length);
        System.out.println(RecordTaint.getMD5HashForBytes(mesb2,0,mesb2.length));
        System.out.println(Long.toString(System.currentTimeMillis()).getBytes().length);
        
        byte[] newbytes = new byte[5];
        System.out.println(new String(newbytes));
        System.out.println((new String(newbytes)).equals(new String(new byte[5])));
        System.out.println(mesb1);
        System.out.println(mes1);
        System.out.println(new String(mesb1));
        System.out.println("mes bytes 0:"+new String(mesb1, 0, 1));
        System.out.println("mes bytes 2:"+new String(mesb1, 1, 1));
        
        int num = 1269317992;
        byte[] msg = "hello, world!".getBytes();
        byte[] newMsg = RecordTaint.newBytesWithMsgID(num, msg, 0, msg.length);
        byte[] numBytes = RecordTaint.intToByteArray(num);
        System.out.println(RecordTaint.byteArrayToInt(numBytes));
        byte[] testNumBytes = new byte[4];
        for(int i=0;i<4;i++) {
            testNumBytes[i] = numBytes[i];
        }
        System.out.println(RecordTaint.byteArrayToInt(testNumBytes));
        
        String info = "test";
        byte[] recMsg = new byte[100];
        LazyByteArrayObjTags old = new LazyByteArrayObjTags(recMsg);
        int len = RecordTaint.restoreMsgBytes(newMsg, old, 0, newMsg.length, newMsg.length, info, info, info, info, info, info);
        System.out.println(new String(old.val, 0, len));
//        System.out.println(old.taints[0]);
        
        File directory = new File("");
        String courseFile = directory.getAbsolutePath();
        System.out.println(courseFile);
        System.out.println(RecordTaint.getTimestamp());
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


