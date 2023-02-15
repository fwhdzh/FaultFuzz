package edu.iscas.tcse.favtrigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.TaintedIntWithObjTag;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;

public class TestByteBuffer {

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        Configuration.USE_MSGID = true;
        Configuration.JDK_MSG = true;
        byte[] bytes = "Hello, world!".getBytes();
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        System.out.println("original data:"+new String(bb.array())+", "+bb.position()+", "+bb.limit());
        ByteBuffer bbMsg = RecordTaint.newByteBufferWithMsgID(1, bb, "address");
        System.out.println("wrapped data for write:"+new String(bbMsg.array())+", "+bbMsg.position()+", "+bbMsg.limit());
        int written = mockWrite(bbMsg);
        System.out.println("written rst:"+written);
        TaintedIntWithObjTag taintedWritten = new TaintedIntWithObjTag(Taint.emptyTaint(), written);
        System.out.println("actual written rst:"+RecordTaint.updateWriteByteBufferResult(taintedWritten, bbMsg.position(), bb).val);
        ByteBuffer bbRead = ByteBuffer.allocate(1024);
        ByteBuffer bbWait = RecordTaint.newByteBufferWaitMsgID(bbRead);
        int read = mockRead(bbWait);
        System.out.println("read rst:"+read);
        TaintedIntWithObjTag taintedRead = new TaintedIntWithObjTag(Taint.emptyTaint(), read);
        System.out.println("actual read rst:"+RecordTaint.restoreReadByteBufferResult(taintedRead, bbRead, null, bbWait, "", "", "", "", "", "").val);
        System.out.println(new String(bbRead.array()));
    }
    
    public static int mockWrite(ByteBuffer bb) throws IOException {
        FileOutputStream out = new FileOutputStream("add-output@Test", false);
        FileChannel ch = out.getChannel();
        return ch.write(bb);
    }

    public static int mockRead(ByteBuffer bb) throws IOException {
        FileInputStream out = new FileInputStream("add-output@Test");
        FileChannel ch = out.getChannel();
        return ch.read(bb);
    }
}
