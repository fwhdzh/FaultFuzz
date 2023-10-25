package edu.columbia.cs.psl.phosphor;

import edu.columbia.cs.psl.phosphor.runtime.StringUtils;

public class NativeMethodInspector {

    //favtrigger: start
    public static boolean isClassNeedToDecideIfRecordTaint(String clazz) {
        return clazz.equals("java/io/FileOutputStream")
                || clazz.equals("java/io/RandomAccessFile");
    }

    public static boolean isNativeMethodNeedsRecordSecondPara(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "java/io/FileOutputStream") && StringUtils.startsWith(mname, "write") && StringUtils.startsWith(desc, "(IZ)V"))
        || (StringUtils.startsWith(clazz, "java/io/FileOutputStream") && StringUtils.startsWith(mname, "writeBytes") && StringUtils.startsWith(desc, "([BIIZ)V"))
        || (StringUtils.startsWith(clazz, "java/io/RandomAccessFile") && StringUtils.startsWith(mname, "write0") && StringUtils.startsWith(desc, "(I)V"))
        || (StringUtils.startsWith(clazz, "java/io/RandomAccessFile") && StringUtils.startsWith(mname, "writeBytes") && StringUtils.startsWith(desc, "([BII)V"));
    }

    public static boolean isNativeMethodNeedsRecordThirdPara(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "java/net/SocketOutputStream") && StringUtils.startsWith(mname, "socketWrite0") && StringUtils.startsWith(desc, "(Ljava/io/FileDescriptor;[BII)V"));
    }

    public static boolean isNativeMethodNeedsRecordDatagramPacket(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "java/net/PlainDatagramSocketImpl") && StringUtils.startsWith(mname, "send") && StringUtils.startsWith(desc, "(Ljava/net/DatagramPacket;)V"));
    }

    public static boolean isNativeMethodNeedsCombineNewTaintToDatagramPacket(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "java/net/PlainDatagramSocketImpl") && StringUtils.startsWith(mname, "receive0") && StringUtils.startsWith(desc, "(Ljava/net/DatagramPacket;)V"));
    }

    public static boolean isNativeMethodNeedsNewRtnTaint(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "java/io/FileInputStream") && StringUtils.startsWith(mname, "read0") && StringUtils.startsWith(desc, "()I"))
        || (StringUtils.startsWith(clazz, "java/io/RandomAccessFile") && StringUtils.startsWith(mname, "read0") && StringUtils.startsWith(desc, "()I"));
    }

    public static boolean isNativeMethodNeedsCombineNewTaintToSecondPara(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "java/io/FileInputStream") && StringUtils.startsWith(mname, "readBytes") && StringUtils.startsWith(desc, "([BII)I"))
        || (StringUtils.startsWith(clazz, "java/io/RandomAccessFile") && StringUtils.startsWith(mname, "readBytes") && StringUtils.startsWith(desc, "([BII)I"));
    }

    public static boolean isNativeMethodNeedsCombineNewTaintToThirdPara(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "java/net/SocketInputStream") && StringUtils.startsWith(mname, "socketRead0") && StringUtils.startsWith(desc, "(Ljava/io/FileDescriptor;[BIII)I"));
    }

    // seem no usage
    public static boolean isFileChannelWrite(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/FileChannelImpl") && StringUtils.startsWith(mname, "write") && StringUtils.startsWith(desc, "(Ljava/nio/ByteBuffer;)I"));
    }

    // seem no usage
    public static boolean isFileChannelRead(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/SocketChannelImpl") && StringUtils.startsWith(mname, "read") && StringUtils.startsWith(desc, "(Ljava/nio/ByteBuffer;)I"));
    }

    public static boolean isSocketChannelRead(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/SocketChannelImpl") && StringUtils.startsWith(mname, "read") && StringUtils.startsWith(desc, "(Ljava/nio/ByteBuffer;)I"));
    }

    public static boolean isSocketChannelReadArray(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/SocketChannelImpl") && StringUtils.startsWith(mname, "read") && StringUtils.startsWith(desc, "([Ljava/nio/ByteBuffer;II)J"));
    }

    public static boolean isSocketChannelWrite(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/SocketChannelImpl") && StringUtils.startsWith(mname, "write") && StringUtils.startsWith(desc, "(Ljava/nio/ByteBuffer;)I"));
    }

    public static boolean isSocketChannelWrite2(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/SocketChannelImpl") && StringUtils.startsWith(mname, "write") && StringUtils.startsWith(desc, "(Ljava/nio/ByteBuffer;)I"));
    }

    public static boolean isSocketChannelWriteArray(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/SocketChannelImpl") && StringUtils.startsWith(mname, "write") && StringUtils.startsWith(desc, "([Ljava/nio/ByteBuffer;II)J"));
    }

    public static boolean isSocketChannelWriteArray2(String clazz, String mname, String desc) {
        return (StringUtils.startsWith(clazz, "sun/nio/ch/SocketChannelImpl") && StringUtils.startsWith(mname, "write") && StringUtils.startsWith(desc, "([Ljava/nio/ByteBuffer;II)J"));
    }
    //favtrigger: end
    
}
