package edu.iscas.tcse.favtrigger.instrumenter.jdk;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.Instrumenter;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.iscas.tcse.favtrigger.instrumenter.jdk.JRERunMode.JREType;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;
//import edu.iscas.tcse.favtrigger.taint.FAVTaintType;
//import edu.iscas.tcse.favtrigger.taint.Source.FAVTagType;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;

import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.FrameNode;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.*;


public class NIOTrackingMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;
    private boolean rpcRelated = false;

    public NIOTrackingMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
            String[] exceptions, String originalDesc, NeverNullArgAnalyzerAdapter analyzer,
            String superCname, String[] interfaces) {
        super(access, owner, name, descriptor, signature, exceptions, mv, analyzer);
        this.desc = descriptor;
        this.returnType = Type.getReturnType(desc);
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        this.name = name;
        this.owner = owner;
        this.ownerSuperCname = superCname;
        this.ownerInterfaces = interfaces;
        List<String> inters =  Arrays.asList(this.ownerInterfaces);
    }

    private int remoteIpVar = -1;
    private int initReadPos = -1;
    public void visitCode() {
    	super.visitCode();
    	if(Instrumenter.isSocketChannelWrite(this.owner, this.name, this.desc)
        ||Instrumenter.isSocketChannelRead(this.owner, this.name, this.desc)){
            //SocketChannelImpl write ByteBuffer
            Label done = new Label();

            FAV_GET_RECORD_OUT.delegateVisit(mv);
            int fileOutStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitJumpInsn(Opcodes.IFNULL, done);

            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

            super.visitVarInsn(ALOAD, 1);
            super.visitJumpInsn(Opcodes.IFNULL, done);

            FAV_GET_TIMESTAMP.delegateVisit(mv);
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            super.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(FAVPathType.FAVMSG.toString()+":");
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
            super.visitVarInsn(Opcodes.ALOAD, 0); //do not contain the port info
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/nio/ch/SocketChannelImpl", "getRemoteAddress", "()Ljava/net/SocketAddress;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
            "()Ljava/lang/String;", false);
            super.visitLdcInsn(JREType.MSG.toString());
            JRE_FAULT_BEFORE.delegateVisit(mv);

            lvs.freeTmpLV(fileOutStream);
            super.visitLabel(done);
            acceptFn(fn);
        }
    }

    public void visitInsn(int opcode) {
        //use lvs would introduce errors, avoid to use lvs
    	/*
        if(opcode == ARETURN) {
        	if (Instrumenter.isSocketChannelRead(this.owner, this.name, this.desc)) {
        		Label done = new Label();
                super.visitVarInsn(ALOAD, 2);
                super.visitJumpInsn(Opcodes.IFNULL, done);

                super.visitVarInsn(ALOAD, 2); //aload ByteBuffer
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", TaintUtils.FAV_GETBUFFERSHADOW_MT, "()"+Type.getDescriptor(LazyByteArrayObjTags.class), false);
                super.visitTypeInsn(Opcodes.CHECKCAST,Type.getInternalName(LazyByteArrayObjTags.class));
                super.visitVarInsn(ILOAD, initReadPos);
            	super.visitVarInsn(ALOAD, 2); //aload ByteBuffer
            	super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", "position", "()I", false);
            	super.visitLdcInsn(this.className);
            	super.visitLdcInsn(this.name);
            	super.visitLdcInsn(this.desc);
            	super.visitLdcInsn(FAVTaintType.SCI.toString());
                super.visitLdcInsn(FAVTagType.JRE.toString());
                super.visitVarInsn(Opcodes.ALOAD, 0); //do not contain the port info
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/nio/ch/SocketChannelImpl", "getRemoteAddress", "()Ljava/net/SocketAddress;", false);
                super.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/jdk/NIOTrackingMV",
                		"adhereTaintsToBytes", "("+Type.getDescriptor(LazyByteArrayObjTags.class)
                		+"IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);

                super.visitLabel(done);
            }
        }
        */
        super.visitInsn(opcode);
    }

    public static String getNIOWritePath(String path) {
    	if(Configuration.USE_FAV && Configuration.JDK_MSG && Configuration.USE_MSGID) {
    		path = path+"&"+RecordTaint.getMsgID();
    	}
    	return path;
    }

    public static void adhereTaintsToBytes(LazyByteArrayObjTags obj, int off, int len, String cname, String mname, String desc, String type, String tag, String remoteIP) {
    	if(Configuration.USE_FAV && Configuration.JDK_MSG) {
    		String linkSource = FAVPathType.FAVMSG.toString()+":"+remoteIP;
    		if(Configuration.USE_MSGID) {
    			linkSource = linkSource+"&"+RecordTaint.getMsgID();
    		}
        	FAVTaint.combineNewTaints(obj, off, len, len, cname, mname, desc, type, tag, linkSource);
    	}
    }
}
