package edu.iscas.tcse.favtrigger.instrumenter.zk;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

import java.net.InetAddress;

import org.objectweb.asm.*;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.*;


public class ZKTrackingMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;

    public ZKTrackingMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
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
    }

    public void visitCode() {
    	if(this.className.equals("java/io/OutputStream") && this.name.equals("<init>")){
    		super.visitVarInsn(Opcodes.ALOAD, 0);
    		super.visitInsn(Opcodes.ACONST_NULL);
            super.visitFieldInsn(Opcodes.PUTFIELD, this.owner, "favAddr", "Ljava/net/InetAddress;");
        }
    	if(this.className.equals("java/io/InputStream") && this.name.equals("<init>")){
    		super.visitVarInsn(Opcodes.ALOAD, 0);
    		super.visitInsn(Opcodes.ACONST_NULL);
            super.visitFieldInsn(Opcodes.PUTFIELD, this.owner, "favAddr", "Ljava/net/InetAddress;");
        }
        super.visitCode();
    }

    public static void record(String s) {
    	System.out.println("GYFAV: "+s);
    }

    public static void record(String s, InetAddress addr) {
    	if(addr != null) {
    		System.out.println("GYFAV: write to "+addr.getHostAddress()+", "+s);
    	}
    }

    int readMsgId = -1;
    int shouldTaintMsg = -1;
    int remoteAddr = -1;
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
    	if (Configuration.FOR_ZK && isRecord(this.ownerSuperCname,this.ownerInterfaces)
    			&& this.name.equals("deserialize")
    			&& owner.equals("org/apache/jute/InputArchive")) {
    		if (name.startsWith("read")&&!name.startsWith("readRecord")) {
//        		Label done = new Label();
//        		org.objectweb.asm.tree.FrameNode fn = getCurrentFrameNode();
//        		super.visitVarInsn(ILOAD, shouldTaintMsg);
//        		super.visitJumpInsn(Opcodes.IFEQ, done);
//
//        		FAV_GET_RECORD_OUT.delegateVisit(mv);
//                int fileOutStream = lvs.getTmpLV();
//                super.visitVarInsn(Opcodes.ASTORE, fileOutStream);
//
//                Label nullOutStream = new Label();
//                org.objectweb.asm.tree.FrameNode outfn = getCurrentFrameNode();
//                super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
//                super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);
//
//                super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
//                super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
//                super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");
//
//                super.visitLabel(nullOutStream);
//                acceptFn(outfn);
//
//                FAV_GET_TIMESTAMP.delegateVisit(mv);
//                super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
//            	super.visitVarInsn(ALOAD, remoteAddr);
//            	super.visitVarInsn(ILOAD, readMsgId);
//                super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
//                        "combineIpWithMsgidForRead", "(Ljava/lang/String;I)Ljava/lang/String;", false);
//                APP_FAULT_BEFORE.delegateVisit(mv);
//
//                lvs.freeTmpLV(fileOutStream);
//
//        		super.visitLabel(done);
//        		acceptFn(fn);
        	}
    	}
        super.visitMethodInsn(opcode, owner, name, desc, isInterface);
        if(Configuration.FOR_ZK && isRecord(this.ownerSuperCname,this.ownerInterfaces)
    			&& this.name.equals("serialize")
    			&& owner.equals("org/apache/jute/OutputArchive")
    			&& name.startsWith("startRecord")) {
        	Label done = new Label();
    		org.objectweb.asm.tree.FrameNode fn = getCurrentFrameNode();

    		super.visitVarInsn(ALOAD, 1);
    		super.visitTypeInsn(INSTANCEOF, "org/apache/jute/BinaryOutputArchive");
    		super.visitJumpInsn(Opcodes.IFEQ, done);

    		super.visitVarInsn(ALOAD, 1);
    		super.visitTypeInsn(CHECKCAST, "org/apache/jute/BinaryOutputArchive");
    		super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/jute/BinaryOutputArchive",
    				"getAddr$$FAV", "()Ljava/net/InetAddress;", false);
    		super.visitJumpInsn(Opcodes.IFNULL, done);

	        FAV_GET_RECORD_OUT.delegateVisit(mv);
            int fileOutStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

            Label nullOutStream = new Label();
            org.objectweb.asm.tree.FrameNode outfn = getCurrentFrameNode();
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

            super.visitLabel(nullOutStream);
            acceptFn(outfn);

    		FAV_NEW_MSGID.delegateVisit(mv);
    		int msgid = lvs.getTmpLV();
            super.visitVarInsn(ISTORE, msgid);

            FAV_GET_TIMESTAMP.delegateVisit(mv);
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);

            super.visitVarInsn(ALOAD, 1);
    		super.visitTypeInsn(CHECKCAST, "org/apache/jute/BinaryOutputArchive");
    		super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/jute/BinaryOutputArchive",
    				"getAddr$$FAV", "()Ljava/net/InetAddress;", false);
    		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
            super.visitVarInsn(ILOAD, msgid);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);

            /*
            super.visitTypeInsn(NEW, "org/apache/jute/ToStringOutputArchive");
    		super.visitInsn(DUP);
    		super.visitTypeInsn(NEW, "java/io/ByteArrayOutputStream");
    		super.visitInsn(DUP);
    		super.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V", false);
    		int arrayOut = lvs.getTmpLV();
    		super.visitVarInsn(ASTORE, arrayOut);
    		super.visitVarInsn(ALOAD, arrayOut);
    		super.visitMethodInsn(INVOKESPECIAL, "org/apache/jute/ToStringOutputArchive", "<init>", "(Ljava/io/OutputStream;)V", false);
    		int stringOut = lvs.getTmpLV();
    		super.visitVarInsn(ASTORE, stringOut);
    		super.visitVarInsn(ALOAD, 0);
    		super.visitVarInsn(ALOAD, 1);
    		super.visitVarInsn(ALOAD, stringOut);
    		super.visitVarInsn(ALOAD, 3);
    		super.visitVarInsn(ALOAD, 4);
    		super.visitVarInsn(ALOAD, 5);
    		super.visitMethodInsn(INVOKEVIRTUAL, this.owner, this.name, this.desc, false);
    		super.visitVarInsn(ALOAD, arrayOut);
    		NEW_EMPTY_TAINT.delegateVisit(mv);
    		super.visitTypeInsn(NEW, Type.getInternalName(TaintedReferenceWithObjTag.class));
    		super.visitInsn(DUP);
    		super.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(TaintedReferenceWithObjTag.class), "<init>", "()V", false);
    		super.visitMethodInsn(INVOKEVIRTUAL, "java/io/ByteArrayOutputStream",
    				"toByteArray"+TaintUtils.METHOD_SUFFIX,
    				"("+Configuration.TAINT_TAG_DESC+Type.getDescriptor(TaintedReferenceWithObjTag.class)
    				+")"+Type.getDescriptor(TaintedReferenceWithObjTag.class), false);
    		super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
    		super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyByteArrayObjTags.class));

    		super.visitFieldInsn(GETFIELD, Type.getInternalName(LazyByteArrayObjTags.class), "val", "[B");
    		super.visitInsn(ARRAYLENGTH);
    		super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "appendBufSize", "(Ljava/lang/String;I)Ljava/lang/String;", false);
    		*/

    		APP_FAULT_BEFORE.delegateVisit(mv);

//            FAV_APP_RECORD_OR_TRIGGER_FULLY.delegateVisit(mv);
            lvs.freeTmpLV(fileOutStream);
//            lvs.freeTmpLV(arrayOut);
//	        lvs.freeTmpLV(stringOut);

    		super.visitVarInsn(Opcodes.ALOAD, 1);
            super.visitVarInsn(ILOAD, msgid);
    		super.visitLdcInsn("favmsg");
    		super.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, "writeInt", "(ILjava/lang/String;)V", true);
    		lvs.freeTmpLV(msgid);

    		super.visitLabel(done);
    		acceptFn(fn);
        } else if (Configuration.FOR_ZK && isRecord(this.ownerSuperCname,this.ownerInterfaces)
    			&& this.name.equals("deserialize")
    			&& owner.equals("org/apache/jute/InputArchive")) {
        	if(name.startsWith("startRecord")) {
        		super.visitInsn(Opcodes.ICONST_0);
            	shouldTaintMsg = lvs.createPermanentLocalVariable(boolean.class, "FAV_TAINT_MSG");
                super.visitVarInsn(ISTORE, shouldTaintMsg);
                super.visitLdcInsn(Integer.MAX_VALUE);
                readMsgId = lvs.createPermanentLocalVariable(int.class, "FAV_READ_MSGID");
                super.visitVarInsn(ISTORE, readMsgId);
//                super.visitInsn(Opcodes.ACONST_NULL);
                super.visitLdcInsn("");
                remoteAddr = lvs.createPermanentLocalVariable(String.class, "FAV_RM_ADDR");
                super.visitVarInsn(ASTORE, remoteAddr);

            	Label done = new Label();
        		org.objectweb.asm.tree.FrameNode fn = getCurrentFrameNode();

        		super.visitVarInsn(ALOAD, 1);
        		super.visitTypeInsn(INSTANCEOF, "org/apache/jute/BinaryInputArchive");
        		super.visitJumpInsn(Opcodes.IFEQ, done);

        		super.visitVarInsn(ALOAD, 1);
        		super.visitTypeInsn(CHECKCAST, "org/apache/jute/BinaryInputArchive");
        		super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/jute/BinaryInputArchive",
        				"getAddr$$FAV", "()Ljava/net/InetAddress;", false);
        		super.visitJumpInsn(Opcodes.IFNULL, done);

        		super.visitInsn(Opcodes.ICONST_1);
        		super.visitVarInsn(ISTORE, shouldTaintMsg);

        		super.visitVarInsn(ALOAD, 1);
        		super.visitTypeInsn(CHECKCAST, "org/apache/jute/BinaryInputArchive");
        		super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/jute/BinaryInputArchive",
        				"getAddr$$FAV", "()Ljava/net/InetAddress;", false);
        		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
                super.visitVarInsn(ASTORE, remoteAddr);

        		super.visitVarInsn(Opcodes.ALOAD, 1);
        		super.visitLdcInsn("favmsg");
        		super.visitMethodInsn(INVOKEINTERFACE, owner, "readInt", "(Ljava/lang/String;)I", true);
        		super.visitVarInsn(ISTORE, readMsgId);

        		FAV_GET_RECORD_OUT.delegateVisit(mv);
                int fileOutStream = lvs.getTmpLV();
                super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

                Label nullOutStream = new Label();
                org.objectweb.asm.tree.FrameNode outfn = getCurrentFrameNode();
                super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

                super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
                super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

                super.visitLabel(nullOutStream);
                acceptFn(outfn);

                FAV_GET_TIMESTAMP.delegateVisit(mv);
                super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            	super.visitVarInsn(ALOAD, remoteAddr);
            	super.visitVarInsn(ILOAD, readMsgId);
                super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                        "combineIpWithMsgidForRead", "(Ljava/lang/String;I)Ljava/lang/String;", false);

                APP_FAULT_BEFORE.delegateVisit(mv);

                lvs.freeTmpLV(fileOutStream);

        		super.visitLabel(done);
        		acceptFn(fn);
        	} else if (name.startsWith("read")&&!name.startsWith("readRecord")) {
//        		Type rtnType = Type.getReturnType(desc);
//        		Label done = new Label();
//        		org.objectweb.asm.tree.FrameNode fn = getCurrentFrameNode();
//
//        		super.visitVarInsn(ILOAD, shouldTaintMsg);
//        		super.visitJumpInsn(Opcodes.IFEQ, done);
//
//        		super.visitLdcInsn(className);
//            	super.visitLdcInsn(this.name);
//            	super.visitLdcInsn(this.desc);
//            	super.visitLdcInsn(FAVTaintType.ZKMSG.toString());
//            	super.visitLdcInsn(FAVTagType.APP.toString());
//            	super.visitVarInsn(ALOAD, remoteAddr);
//            	super.visitVarInsn(ILOAD, readMsgId);
//                super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
//                        "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
//                FAV_APP_TAINT_PRIMITIVE.delegateVisit(mv);
//        		super.visitTypeInsn(Opcodes.CHECKCAST, rtnType.getInternalName());
//
//        		super.visitLabel(done);
//        		acceptFn(fn);
        	}
        }
    }

    public static void record(Taint addr) {
    	if(addr != null) {
        	System.out.println("GYFAV: write taint "+addr);
    	}
    }
    public static void check(InetAddress addr) {
    	if(addr != null) {
        	System.out.println("GYFAV: read from "+addr.getHostAddress());
    	}
    }
    public static void check(InetAddress addr, int msg, String ip) {
    	if(addr != null) {
        	System.out.println("GYFAV: read from "+addr.getHostAddress()+", msg:"+msg+", ip:"+ip);
    	}
    }
    public static boolean isRecord(String superName,String[] interfaces) {
    	if(superName.equals("org/apache/jute/Record")) {
    		return true;
    	}
    	for(String i:interfaces) {
    		if(i.equals("org/apache/jute/Record")) {
        		return true;
        	}
    	}
    	return false;
    }

    public static boolean isOutputStream(String superName,String[] interfaces) {
    	if(superName.equals("java/io/OutputStream")) {
    		return true;
    	}
    	for(String i:interfaces) {
    		if(i.equals("java/io/OutputStream")) {
        		return true;
        	}
    	}
    	return false;
    }
    public static boolean isInputStream(String superName,String[] interfaces) {
    	if(superName.equals("java/io/InputStream")) {
    		return true;
    	}
    	for(String i:interfaces) {
    		if(i.equals("java/io/InputStream")) {
        		return true;
        	}
    	}
    	return false;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
		// TODO Auto-generated method stub
    	if(isOutputStream(this.ownerSuperCname,this.ownerInterfaces)
				&& this.name.equals("<init>")
				&& this.owner.equals(owner)//favtrigger: TODO: find a better way to verify the object is this
				&& descriptor.equals("Ljava/io/OutputStream;")
				&& opcode == Opcodes.PUTFIELD
				&& (!Configuration.FOR_YARN || (Configuration.FOR_YARN && this.name.equals("out")))
//				&& this.name.equals("out") //favtrigger: this may cause omission and fail tracking zk
				// but without this, will fail mapreduce
				) {
			//[this, out]
			//[this, out, out]
			super.visitInsn(DUP);
			super.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(java.io.OutputStream.class), "favAddr", "Ljava/net/InetAddress;");
			//[this, out, out.favAddr]
			super.visitVarInsn(ALOAD, 0);
			super.visitInsn(Opcodes.SWAP);
			//[this, out, this, out.favAddr]
			super.visitFieldInsn(Opcodes.PUTFIELD, this.owner, "favAddr", "Ljava/net/InetAddress;");
		} else if(isInputStream(this.ownerSuperCname,this.ownerInterfaces)
				&& this.name.equals("<init>")
				&& this.owner.equals(owner)
				&& descriptor.equals("Ljava/io/InputStream;")
				&& (!Configuration.FOR_YARN || (Configuration.FOR_YARN && this.name.equals("in")))
//				 && this.name.equals("in") //favtrigger: this may cause omission and fail tracking zk
				&& opcode == Opcodes.PUTFIELD) {
			//[this, in]
			//[this, in, in]
			super.visitInsn(DUP);
			super.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(java.io.InputStream.class), "favAddr", "Ljava/net/InetAddress;");
			//[this, in, in.favAddr]
			super.visitVarInsn(ALOAD, 0);
			super.visitInsn(Opcodes.SWAP);
			//[this, in, this, in.favAddr]
			super.visitFieldInsn(Opcodes.PUTFIELD, this.owner, "favAddr", "Ljava/net/InetAddress;");
		}
		super.visitFieldInsn(opcode, owner, name, descriptor);
	}

	public void visitInsn(int opcode) {
        //use lvs would introduce errors, avoid to use lvs
        if(opcode == ARETURN) {
        	if(this.owner.equals("java/net/Socket") && this.name.equals("getOutputStream")) {
        		super.visitInsn(DUP);
        		super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(java.io.OutputStream.class));
        		super.visitVarInsn(ALOAD, 0);
        		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, "getInetAddress", "()Ljava/net/InetAddress;", false);
        		super.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(java.io.OutputStream.class), "favAddr", "Ljava/net/InetAddress;");
        	}
        	if(this.owner.equals("java/net/Socket") && this.name.equals("getInputStream")) {
        		super.visitInsn(DUP);
        		super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(java.io.InputStream.class));
        		super.visitVarInsn(ALOAD, 0);
        		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, "getInetAddress", "()Ljava/net/InetAddress;", false);
        		super.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(java.io.InputStream.class), "favAddr", "Ljava/net/InetAddress;");
        	}
        }
        super.visitInsn(opcode);
    }

}
