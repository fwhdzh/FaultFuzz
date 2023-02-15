package edu.iscas.tcse.favtrigger.instrumenter;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;
import edu.iscas.tcse.favtrigger.taint.FAVTaintType;
import edu.iscas.tcse.favtrigger.taint.Source.FAVTagType;
//import edu.columbia.cs.psl.phosphor.struct.TaintedPrimitiveWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.FrameNode;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.*;


//model use of hdfs as a client
public class HDFSAPIModelMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;

    public HDFSAPIModelMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
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

    @Override
    public void visitCode() {
        // TODO Auto-generated method stub
        super.visitCode();
//        if((owner.equals("org/apache/hadoop/fs/FSDataInputStream") || owner.equals("org/apache/hadoop/hdfs/client/HdfsDataInputStream")
//                || owner.equals("java/io/DataInputStream")) && name.startsWith("read")) {
//            if(name.equals("read$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+"Ljava/nio/ByteBuffer;"+Configuration.TAINT_TAG_DESC+Type.getDescriptor(TaintedIntWithObjTag.class)+")"+Type.getDescriptor(TaintedIntWithObjTag.class))) {
//                super.visitVarInsn(ALOAD, 2);
//                super.visitTypeInsn(CHECKCAST, "java/nio/ByteBuffer");
//                super.visitMethodInsn(INVOKEVIRTUAL, "java/nio/ByteBuffer", "position", "()I", false);
//                bufferPos = lvs.createPermanentLocalVariable(int.class, "FAV_BUF_POS");
//                super.visitVarInsn(ISTORE, bufferPos);
//            }
//        }
    }

    public static boolean canFindHdfsDataInputStream() {
    	try {
    		Class.forName("org.apache.hadoop.hdfs.client.HdfsDataInputStream");
    	} catch (Exception e) {
    		return false;
    	}
    	return true;
    }

    public static boolean canFindHdfsDataOutputStream() {
    	try {
    		Class.forName("org.apache.hadoop.hdfs.client.HdfsDataOutputStream");
    	} catch (Exception e) {
    		return false;
    	}
    	return true;
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
    	if(Configuration.USE_FAV && Configuration.HDFS_API) {
    		if((owner.equals("org/apache/hadoop/fs/FSDataInputStream") || owner.equals("org/apache/hadoop/hdfs/client/HdfsDataInputStream")
                    || owner.equals("java/io/DataInputStream") || owner.equals("java/io/DataInput")) && name.startsWith("read") && !Configuration.FOR_JAVA
    				&& (opcode != Opcodes.INVOKESTATIC || (opcode == Opcodes.INVOKESTATIC && name.equals("readUTF")
                    		&& desc.equals("(Ljava/io/DataInput;)Ljava/lang/String;")))) {
                Type[] args = Type.getArgumentTypes(desc);
                int[] vars = new int[args.length];
                Type rtnType = Type.getReturnType(desc);
                for(int i = args.length - 1; i >= 0; i--) {
                    vars[i] = lvs.getTmpLV();
                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
                        super.visitVarInsn(ASTORE, vars[i]);
                    } else if(args[i].getSort() == Type.DOUBLE) {
                        super.visitVarInsn(Opcodes.DSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.LONG) {
                        super.visitVarInsn(Opcodes.LSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.FLOAT) {
                        super.visitVarInsn(Opcodes.FSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
                            || args[i].getSort() == Type.BOOLEAN) {
                        super.visitVarInsn(ISTORE, vars[i]);
                    } else {
                        //this would not happen
                    }
                }
                int input = -1;
                if(opcode != Opcodes.INVOKESTATIC) {
                	input = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, input);
                } else {
                	input = vars[0];
                }

                FrameNode fn = getCurrentFrameNode();
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "canFindHdfsDataInputStream", "()Z", false);
                Label finish = new Label();
                super.visitJumpInsn(Opcodes.IFEQ, finish);
                super.visitVarInsn(ALOAD, input);
                super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataInputStream");
                super.visitJumpInsn(Opcodes.IFEQ, finish);
                super.visitVarInsn(ALOAD, input);
                super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
                super.visitVarInsn(ALOAD, input);
                super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/fs/FSDataInputStream");
                super.visitFieldInsn(GETFIELD, "org/apache/hadoop/fs/FSDataInputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");
                super.visitFieldInsn(PUTFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
                super.visitLabel(finish);
                acceptFn(fn);

                if(opcode != Opcodes.INVOKESTATIC) {
                	super.visitVarInsn(ALOAD, input);
                }
                for(int i = 0; i < args.length; i++) {
                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
                        super.visitVarInsn(ALOAD, vars[i]);
                    } else if(args[i].getSort() == Type.DOUBLE) {
                        super.visitVarInsn(Opcodes.DLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.LONG) {
                        super.visitVarInsn(Opcodes.LLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.FLOAT) {
                        super.visitVarInsn(Opcodes.FLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
                            || args[i].getSort() == Type.BOOLEAN) {
                        super.visitVarInsn(ILOAD, vars[i]);
                    } else {
                        //this would not happen
                    }
                }

                if((owner.equals("org/apache/hadoop/fs/FSDataInputStream") || owner.equals("org/apache/hadoop/hdfs/client/HdfsDataInputStream")
                        || owner.equals("java/io/DataInputStream")) && name.startsWith("read")) {
                    if((desc.startsWith("()") && !rtnType.equals(Type.VOID_TYPE))
                    		|| opcode == Opcodes.INVOKESTATIC) {
                    	triggerFaultReadBefore(input);
//                        Label readfinish = new Label();
//                        FrameNode fn1 = getCurrentFrameNode();
//                        super.visitVarInsn(ALOAD, input);
//                        super.visitTypeInsn(Opcodes.INSTANCEOF, "java/io/DataInputStream");
//                        super.visitJumpInsn(Opcodes.IFEQ, readfinish);
//                        super.visitVarInsn(ALOAD, input);
//                        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
//                        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
//                        super.visitJumpInsn(Opcodes.IFNULL, readfinish);
//
//                        super.visitVarInsn(ALOAD, input);
//                        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
//                        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
//                        int path = lvs.getTmpLV();
//                        super.visitVarInsn(ASTORE, path);
//                        fauntTriggerBefore(path);
//
//                        super.visitVarInsn(ALOAD, input);
//                        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
//                        super.visitInsn(ACONST_NULL);
//                        super.visitFieldInsn(PUTFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
//
//                        super.visitLabel(readfinish);
//                        acceptFn(fn1);
//                        lvs.freeTmpLV(path);
                    }  else if (name.equals("readFully") && desc.equals("([B)V")) {
                        triggerFaultReadBefore(input);
                    } else if (name.equals("readFully") && desc.equals("(J[B)V")) {
                    	triggerFaultReadBefore(input);
                    } else if (name.equals("readFully") && desc.equals("([BII)V")) {
                    	triggerFaultReadBefore(input);
                    }  else if (name.equals("readFully") && desc.equals("(J[BII)V")) {
                    	triggerFaultReadBefore(input);
                    } else if (name.equals("read") && desc.equals("([B)I")) {
                    	triggerFaultReadBefore(input);
                    } else if (name.equals("read") && desc.equals("([BII)I")) {
                    	triggerFaultReadBefore(input);
                    } else if (name.equals("read") && desc.equals("(J[BII)I")) {
                    	triggerFaultReadBefore(input);
                    } else if(name.equals("read") && desc.equals("(Ljava/nio/ByteBuffer;)I")) {
                    	triggerFaultReadBefore(input);
                    }
                }

                super.visitMethodInsn(opcode, owner, name, desc, isInterface);//perform original call

                for(int i = 0; i < vars.length; i++) {
                    lvs.freeTmpLV(vars[i]);
                }
                if(opcode != Opcodes.INVOKESTATIC) {
                	lvs.freeTmpLV(input);
                }
            } else if((owner.equals("org/apache/hadoop/fs/FSDataOutputStream") || owner.equals("org/apache/hadoop/hdfs/client/HdfsDataOutputStream")
                    || owner.equals("java/io/DataOutputStream") || owner.equals("java/io/DataOutput")) && name.startsWith("write") && !Configuration.FOR_JAVA
            		&& (opcode != Opcodes.INVOKESTATIC || (opcode == Opcodes.INVOKESTATIC && name.equals("writeUTF")
                    && desc.equals("(Ljava/lang/String;Ljava/io/DataOutput;)I")))) {
                Type[] args = Type.getArgumentTypes(desc);
                int[] vars = new int[args.length];

                for(int i = args.length - 1; i >= 0; i--) {
                    vars[i] = lvs.getTmpLV();
                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
                        super.visitVarInsn(ASTORE, vars[i]);
                    } else if(args[i].getSort() == Type.DOUBLE) {
                        super.visitVarInsn(Opcodes.DSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.LONG) {
                        super.visitVarInsn(Opcodes.LSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.FLOAT) {
                        super.visitVarInsn(Opcodes.FSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
                            || args[i].getSort() == Type.BOOLEAN) {
                        super.visitVarInsn(ISTORE, vars[i]);
                    } else {
                        //this would not happen
                    }
                }
                int output = -1;
                if(opcode != Opcodes.INVOKESTATIC) {
                	output = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, output);
                } else {
                	output = vars[1];
                }

                FrameNode fn = getCurrentFrameNode();
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "canFindHdfsDataOutputStream", "()Z", false);
                Label finish = new Label();
                super.visitJumpInsn(Opcodes.IFEQ, finish);
                super.visitVarInsn(ALOAD, output);
                super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataOutputStream");
                super.visitJumpInsn(Opcodes.IFEQ, finish);

                super.visitVarInsn(ALOAD, output);
                super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/fs/FSDataOutputStream");
                super.visitFieldInsn(GETFIELD, "org/apache/hadoop/fs/FSDataOutputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");
                int path = lvs.getTmpLV();
                super.visitVarInsn(ASTORE, path);

                fauntTriggerBefore(path);

                super.visitLabel(finish);
                acceptFn(fn);

                if(opcode != Opcodes.INVOKESTATIC) {
                	super.visitVarInsn(ALOAD, output);
                }
                for(int i = 0; i < args.length; i++) {
                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
                        super.visitVarInsn(ALOAD, vars[i]);
                    } else if(args[i].getSort() == Type.DOUBLE) {
                        super.visitVarInsn(Opcodes.DLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.LONG) {
                        super.visitVarInsn(Opcodes.LLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.FLOAT) {
                        super.visitVarInsn(Opcodes.FLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
                            || args[i].getSort() == Type.BOOLEAN) {
                        super.visitVarInsn(ILOAD, vars[i]);
                    } else {
                        //this would not happen
                    }
                }
                for(int i = 0; i < vars.length; i++) {
                    lvs.freeTmpLV(vars[i]);
                }
                if(opcode != Opcodes.INVOKESTATIC) {
                	lvs.freeTmpLV(output);
                }
                lvs.freeTmpLV(path);
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            } else if (owner.equals("org/apache/hadoop/fs/FileSystem") && !name.equals("<init>")) {
            	Type[] args = Type.getArgumentTypes(desc);
                int[] vars = new int[args.length];

                for(int i = args.length - 1; i >= 0; i--) {
                    vars[i] = lvs.getTmpLV();
                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
                        super.visitVarInsn(ASTORE, vars[i]);
                    } else if(args[i].getSort() == Type.DOUBLE) {
                        super.visitVarInsn(Opcodes.DSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.LONG) {
                        super.visitVarInsn(Opcodes.LSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.FLOAT) {
                        super.visitVarInsn(Opcodes.FSTORE, vars[i]);
                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
                            || args[i].getSort() == Type.BOOLEAN) {
                        super.visitVarInsn(ISTORE, vars[i]);
                    } else {
                        //this would not happen
                    }
                }
                if(name.equals("rename")
                        && desc.startsWith("(Lorg/apache/hadoop/fs/Path;Lorg/apache/hadoop/fs/Path;")) {
                	super.visitVarInsn(ALOAD, vars[0]);
                    super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
                    int path = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, path);
                    fauntTriggerBefore(path);
                	lvs.freeTmpLV(path);
                } else if (name.equals("delete")
                        && desc.startsWith("(Lorg/apache/hadoop/fs/Path;")) {
                	super.visitVarInsn(ALOAD, vars[0]);
                    super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
                    int path = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, path);
                    fauntTriggerBefore(path);
                	lvs.freeTmpLV(path);
                }
                for(int i = 0; i < args.length; i++) {
                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
                        super.visitVarInsn(ALOAD, vars[i]);
                    } else if(args[i].getSort() == Type.DOUBLE) {
                        super.visitVarInsn(Opcodes.DLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.LONG) {
                        super.visitVarInsn(Opcodes.LLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.FLOAT) {
                        super.visitVarInsn(Opcodes.FLOAD, vars[i]);
                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
                            || args[i].getSort() == Type.BOOLEAN) {
                        super.visitVarInsn(ILOAD, vars[i]);
                    } else {
                        //this would not happen
                    }
                }

                super.visitMethodInsn(opcode, owner, name, desc, isInterface);

                if(name.equals("open")
                        && desc.startsWith("(Lorg/apache/hadoop/fs/Path;")) {
                    int input = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, input);
                    FrameNode fn = getCurrentFrameNode();
                    super.visitVarInsn(ALOAD, input);
                    super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataInputStream");
                    Label finish = new Label();
                    super.visitJumpInsn(Opcodes.IFEQ, finish);

                    super.visitVarInsn(ALOAD, input);
                    super.visitVarInsn(ALOAD, vars[0]);
                    super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
                    super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataInputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");
                    // super.visitLdcInsn("return open()");
                    // super.visitVarInsn(ALOAD, vars[1]);
                    // super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
                    // super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "print", "(Ljava/lang/String;Ljava/lang/String;)V", false);

                    super.visitLabel(finish);
                    acceptFn(fn);
                    super.visitVarInsn(ALOAD, input);
                    lvs.freeTmpLV(input);
                } else if(name.equals("open")
                        && desc.startsWith("(Lorg/apache/hadoop/fs/PathHandle;")) {
                    int input = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, input);
                    FrameNode fn = getCurrentFrameNode();
                    super.visitVarInsn(ALOAD, input);
                    super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataInputStream");
                    Label finish = new Label();
                    super.visitJumpInsn(Opcodes.IFEQ, finish);

                    super.visitVarInsn(ALOAD, input);
                    super.visitVarInsn(ALOAD, vars[0]);
                    super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/PathHandle", "toByteArray", "()[B", false);
                    super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "getString", "([B)Ljava/lang/String;", false);
                    super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataInputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");
                    // super.visitLdcInsn("return open(pathhandle)");
                    // super.visitVarInsn(ALOAD, vars[1]);
                    // super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/PathHandle", "toByteArray", "()[B", false);
                    // super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "getString", "([B)Ljava/lang/String;", false);
                    // super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "print", "(Ljava/lang/String;Ljava/lang/String;)V", false);

                    super.visitLabel(finish);
                    acceptFn(fn);
                    super.visitVarInsn(ALOAD, input);
                    lvs.freeTmpLV(input);
                } else if(name.equals("create")
                        && desc.startsWith("(Lorg/apache/hadoop/fs/Path;")) {
                    int output = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, output);
                    FrameNode fn = getCurrentFrameNode();
                    super.visitVarInsn(ALOAD, output);
                    super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataOutputStream");
                    Label finish = new Label();
                    super.visitJumpInsn(Opcodes.IFEQ, finish);

                    super.visitVarInsn(ALOAD, output);
                    super.visitVarInsn(ALOAD, vars[0]);
                    super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
                    super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataOutputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");

                    super.visitLabel(finish);
                    acceptFn(fn);
                    super.visitVarInsn(ALOAD, output);
                    lvs.freeTmpLV(output);
                } else if(opcode == Opcodes.INVOKESTATIC && owner.equals("org/apache/hadoop/fs/FileSystem") && name.equals("create")
                        && desc.startsWith("(Lorg/apache/hadoop/fs/FileSystem;Lorg/apache/hadoop/fs/Path;")) {
                    int output = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, output);
                    FrameNode fn = getCurrentFrameNode();
                    super.visitVarInsn(ALOAD, output);
                    super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataOutputStream");
                    Label finish = new Label();
                    super.visitJumpInsn(Opcodes.IFEQ, finish);

                    super.visitVarInsn(ALOAD, output);
                    super.visitVarInsn(ALOAD, vars[1]);
                    super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
                    super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataOutputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");

                    super.visitLabel(finish);
                    acceptFn(fn);
                    super.visitVarInsn(ALOAD, output);
                    lvs.freeTmpLV(output);
                } else if(name.equals("appendFile") || name.equals("createFile")) {
                	super.visitLdcInsn("may be FSDataOutputStreamBuilder ");
                	super.visitLdcInsn(this.name+this.desc);
                    super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "print", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                } else if(name.equals("append") && desc.startsWith("(Lorg/apache/hadoop/fs/Path;")) {
                    int output = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, output);
                    FrameNode fn = getCurrentFrameNode();
                    super.visitVarInsn(ALOAD, output);
                    super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataOutputStream");
                    Label finish = new Label();
                    super.visitJumpInsn(Opcodes.IFEQ, finish);

                    super.visitVarInsn(ALOAD, output);
                    super.visitVarInsn(ALOAD, vars[0]);
                    super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
                    super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataOutputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");

                    super.visitLabel(finish);
                    acceptFn(fn);
                    super.visitVarInsn(ALOAD, output);
                    lvs.freeTmpLV(output);
                }

                for(int i = 0; i < vars.length; i++) {
                    lvs.freeTmpLV(vars[i]);
                }
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            }
    	} else {
    		super.visitMethodInsn(opcode, owner, name, desc, isInterface);
    	}
    }

    public void fauntTriggerBefore(int path) {

//        super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
//        super.visitInsn(Opcodes.DUP);
//        super.visitLdcInsn("lala");
//        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
//        super.visitVarInsn(Opcodes.ALOAD, path);
//        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
//        super.visitVarInsn(ASTORE, path);

    	FAV_GET_RECORD_OUT.delegateVisit(mv);
        int fileOutStream = lvs.getTmpLV();
        super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

        Label nullOutStream = new Label();
        FrameNode fn = getCurrentFrameNode();
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
        super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

        super.visitLabel(nullOutStream);
        acceptFn(fn);

        FAV_GET_TIMESTAMP.delegateVisit(mv);
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        // super.visitVarInsn(ALOAD, 0);
        // super.visitFieldInsn(GETFIELD, this.owner, TaintUtils.FAV_RPC_SOCKET, "Ljava/lang/String;");
        super.visitVarInsn(ALOAD, path);
        APP_FAULT_BEFORE.delegateVisit(mv);
        lvs.freeTmpLV(fileOutStream);
    }

    public void recordOrTriggerHDFSBytesWrites(int bytes, int path, int off, int len) {
    	FAV_GET_RECORD_OUT.delegateVisit(mv);
        int fileOutStream = lvs.getTmpLV();
        super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

        Label nullOutStream = new Label();
        FrameNode fn = getCurrentFrameNode();
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
        super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

        super.visitLabel(nullOutStream);
        acceptFn(fn);

        FAV_GET_TIMESTAMP.delegateVisit(mv);
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitVarInsn(ALOAD, path);
        super.visitVarInsn(ALOAD, bytes);
        super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyByteArrayObjTags.class));
        super.visitVarInsn(ILOAD, off);
        super.visitVarInsn(ILOAD, len);
        FAV_APP_RECORD_OR_TRIGGER.delegateVisit(mv);
        lvs.freeTmpLV(fileOutStream);
    }

    public void recordOrTriggerHDFSWrites(int taint, int path) {
    	FAV_GET_RECORD_OUT.delegateVisit(mv);
        int fileOutStream = lvs.getTmpLV();
        super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

        Label nullOutStream = new Label();
        FrameNode fn = getCurrentFrameNode();
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
        super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

        super.visitLabel(nullOutStream);
        acceptFn(fn);

        FAV_GET_TIMESTAMP.delegateVisit(mv);
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitVarInsn(ALOAD, path);
        super.visitVarInsn(ALOAD, taint);
        FAV_APP_RECORD_OR_TRIGGER_TAINT.delegateVisit(mv);
        lvs.freeTmpLV(fileOutStream);
    }

    public static void print(String s, String info) {
        StackTraceElement[] callStack;
        callStack = Thread.currentThread().getStackTrace();
        List<String> callStackString = new ArrayList<String>();
        for(int i = 0; i < callStack.length; ++i) {
            callStackString.add(callStack[i].toString());
        }
        System.out.println("!!!GY HDFS print: "+s+" | "+info+" "+callStackString);
    }

    @Override
    public void visitInsn(int opcode) {
        // TODO Auto-generated method stub
        //if(opcode == ARETURN) {
    	if(Configuration.USE_FAV && Configuration.HDFS_API && OpcodesUtil.isReturnOpcode(opcode)) {
//    		attachFilePathToStream(opcode);
    	}
        super.visitInsn(opcode);
    }

    public static String getString(byte[] bytes) {
    	return new String(bytes);
    }

    public void attachFilePathToStream(int opcode) {
        if(opcode == ARETURN && owner.equals("org/apache/hadoop/fs/FileSystem") && name.equals("open$$PHOSPHORTAGGED")
                && this.desc.startsWith("("+Configuration.TAINT_TAG_DESC+"Lorg/apache/hadoop/fs/Path;") && !Configuration.FOR_JAVA) {
            int taintedInput = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, taintedInput);
            super.visitVarInsn(ALOAD, taintedInput);
            super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedReferenceWithObjTag.class));
            super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
            super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/fs/FSDataInputStream");
            int input = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, input);
            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(ALOAD, input);
            super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataInputStream");
            Label finish = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, finish);

            super.visitVarInsn(ALOAD, input);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
            super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataInputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");
            // super.visitLdcInsn("return open()");
            // super.visitVarInsn(ALOAD, 2);
            // super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
            // super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "print", "(Ljava/lang/String;Ljava/lang/String;)V", false);

            super.visitLabel(finish);
            acceptFn(fn);
            super.visitVarInsn(ALOAD, taintedInput);
            lvs.freeTmpLV(taintedInput);
            lvs.freeTmpLV(input);
        }
        if(opcode == ARETURN && owner.equals("org/apache/hadoop/fs/FileSystem") && name.equals("open$$PHOSPHORTAGGED")
                && this.desc.startsWith("("+Configuration.TAINT_TAG_DESC+"Lorg/apache/hadoop/fs/PathHandle;") && !Configuration.FOR_JAVA) {
            int taintedInput = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, taintedInput);
            super.visitVarInsn(ALOAD, taintedInput);
            super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedReferenceWithObjTag.class));
            super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
            super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/fs/FSDataInputStream");
            int input = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, input);
            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(ALOAD, input);
            super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataInputStream");
            Label finish = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, finish);

            super.visitVarInsn(ALOAD, input);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/PathHandle", "toByteArray", "()[B", false);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "getString", "([B)Ljava/lang/String;", false);
            super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataInputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");
            // super.visitLdcInsn("return open(pathhandle)");
            // super.visitVarInsn(ALOAD, 2);
            // super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/PathHandle", "toByteArray", "()[B", false);
            // super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "getString", "([B)Ljava/lang/String;", false);
            // super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "print", "(Ljava/lang/String;Ljava/lang/String;)V", false);

            super.visitLabel(finish);
            acceptFn(fn);
            super.visitVarInsn(ALOAD, taintedInput);
            lvs.freeTmpLV(taintedInput);
            lvs.freeTmpLV(input);
        }
        if(opcode == ARETURN && owner.equals("org/apache/hadoop/fs/FileSystem") && name.equals("create$$PHOSPHORTAGGED")
                && this.desc.startsWith("("+Configuration.TAINT_TAG_DESC+"Lorg/apache/hadoop/fs/Path;") && !Configuration.FOR_JAVA) {
            int taintedOutput = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, taintedOutput);
            super.visitVarInsn(ALOAD, taintedOutput);
            super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedReferenceWithObjTag.class));
            super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
            super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/fs/FSDataOutputStream");
            int output = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, output);
            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(ALOAD, output);
            super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataOutputStream");
            Label finish = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, finish);

            super.visitVarInsn(ALOAD, output);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
            super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataOutputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");

            super.visitLabel(finish);
            acceptFn(fn);
            super.visitVarInsn(ALOAD, taintedOutput);
            lvs.freeTmpLV(taintedOutput);
            lvs.freeTmpLV(output);
        }
        if(opcode == ARETURN && this.isStatic && owner.equals("org/apache/hadoop/fs/FileSystem") && name.equals("create$$PHOSPHORTAGGED")
                && this.desc.startsWith("(Lorg/apache/hadoop/fs/FileSystem;"+Configuration.TAINT_TAG_DESC+"Lorg/apache/hadoop/fs/Path;") && !Configuration.FOR_JAVA) {
            int taintedOutput = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, taintedOutput);
            super.visitVarInsn(ALOAD, taintedOutput);
            super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedReferenceWithObjTag.class));
            super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
            super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/fs/FSDataOutputStream");
            int output = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, output);
            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(ALOAD, output);
            super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataOutputStream");
            Label finish = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, finish);

            super.visitVarInsn(ALOAD, output);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
            super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataOutputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");

            super.visitLabel(finish);
            acceptFn(fn);
            super.visitVarInsn(ALOAD, taintedOutput);
            lvs.freeTmpLV(taintedOutput);
            lvs.freeTmpLV(output);
        }
        if(opcode == ARETURN && owner.equals("org/apache/hadoop/fs/FileSystem")
        		&& (name.equals("appendFile$$PHOSPHORTAGGED") || name.equals("createFile$$PHOSPHORTAGGED")) && !Configuration.FOR_JAVA) {
        	super.visitLdcInsn("may be FSDataOutputStreamBuilder ");
        	super.visitLdcInsn(this.name+this.desc);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/HDFSAPIModelMV", "print", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
        if(opcode == ARETURN && owner.equals("org/apache/hadoop/fs/FileSystem") && name.equals("append$$PHOSPHORTAGGED") && !Configuration.FOR_JAVA) {
            int taintedOutput = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, taintedOutput);
            super.visitVarInsn(ALOAD, taintedOutput);
            super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedReferenceWithObjTag.class));
            super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
            super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/fs/FSDataOutputStream");
            int output = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, output);
            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(ALOAD, output);
            super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hdfs/client/HdfsDataOutputStream");
            Label finish = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, finish);

            super.visitVarInsn(ALOAD, output);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/fs/Path", "toString", "()Ljava/lang/String;", false);
            super.visitFieldInsn(PUTFIELD, "org/apache/hadoop/fs/FSDataOutputStream", TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;");

            super.visitLabel(finish);
            acceptFn(fn);
            super.visitVarInsn(ALOAD, taintedOutput);
            lvs.freeTmpLV(taintedOutput);
            lvs.freeTmpLV(output);
        }

//        if((owner.equals("org/apache/hadoop/fs/FSDataInputStream") || owner.equals("org/apache/hadoop/hdfs/client/HdfsDataInputStream")
//                || owner.equals("java/io/DataInputStream")) && name.startsWith("read")) {
//            Type rtnType = Type.getReturnType(desc);
//            if(opcode == ARETURN && desc.startsWith("("+Configuration.TAINT_TAG_DESC+")") && !rtnType.equals(Type.VOID_TYPE)) {
//                int rtnHodler = lvs.getTmpLV();
//                Label finish = new Label();
//                super.visitVarInsn(ASTORE, rtnHodler);
//                FrameNode fn1 = getCurrentFrameNode();
//                super.visitVarInsn(ALOAD, 0);
//                super.visitTypeInsn(Opcodes.INSTANCEOF, "java/io/DataInputStream");
//                super.visitJumpInsn(Opcodes.IFEQ, finish);
//                super.visitVarInsn(ALOAD, 0);
//                super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
//                super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
//                super.visitJumpInsn(Opcodes.IFNULL, finish);
//
//                super.visitVarInsn(ALOAD, rtnHodler);
//                super.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(TaintedPrimitiveWithObjTag.class));
//                super.visitJumpInsn(Opcodes.IFEQ, finish);
//
//                super.visitVarInsn(ALOAD, rtnHodler);
//                super.visitLdcInsn(this.owner);
//                super.visitLdcInsn(this.name);
//                super.visitLdcInsn(this.desc);
//                super.visitLdcInsn(FAVTaintType.HDFSREAD.toString());
//                super.visitLdcInsn(FAVTagType.APP.toString());
//                super.visitVarInsn(ALOAD, 0);
//                super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
//                super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
//                FAV_APP_TAINT_PRIMITIVE.delegateVisit(mv);
//                super.visitVarInsn(ASTORE, rtnHodler);
//
//                super.visitVarInsn(ALOAD, 0);
//                super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
//                super.visitInsn(ACONST_NULL);
//                super.visitFieldInsn(PUTFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
//
//                super.visitLabel(finish);
//                acceptFn(fn1);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                lvs.freeTmpLV(rtnHodler);
//            } else if (name.equals("readFully$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+Type.getDescriptor(LazyByteArrayObjTags.class)+Configuration.TAINT_TAG_DESC+")V")) {
//                taintBytesFully(0, 2);
//            } else if (name.equals("readFully$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+"J"+Configuration.TAINT_TAG_DESC+Type.getDescriptor(LazyByteArrayObjTags.class)+Configuration.TAINT_TAG_DESC+")V")) {
//                taintBytesFully(0, 5);
//            } else if (name.equals("readFully$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+Type.getDescriptor(LazyByteArrayObjTags.class)+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+")V")) {
//                taintBytes(0, 2, 4, 6);
//            }  else if (name.equals("readFully$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+"J"+Configuration.TAINT_TAG_DESC+Type.getDescriptor(LazyByteArrayObjTags.class)+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+")V")) {
//                taintBytes(0, 5, 7, 9);
//            } else if (opcode == ARETURN && name.equals("read$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+Type.getDescriptor(LazyByteArrayObjTags.class)+Configuration.TAINT_TAG_DESC+Type.getDescriptor(TaintedIntWithObjTag.class)+")"+Type.getDescriptor(TaintedIntWithObjTag.class))) {
//                int rtnHodler = lvs.getTmpLV();
//                super.visitVarInsn(ASTORE, rtnHodler);
//                super.visitInsn(Opcodes.ICONST_0);
//                int off = lvs.getTmpLV();
//                super.visitVarInsn(ISTORE, off);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedIntWithObjTag.class));
//                super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedIntWithObjTag.class), "val", "I");
//                int len = lvs.getTmpLV();
//                super.visitVarInsn(ISTORE, len);
//                taintBytes(0, 2, off, len);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                lvs.freeTmpLV(rtnHodler);
//                lvs.freeTmpLV(off);
//                lvs.freeTmpLV(len);
//            } else if (opcode == ARETURN && name.equals("read$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+Type.getDescriptor(LazyByteArrayObjTags.class)+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+Type.getDescriptor(TaintedIntWithObjTag.class)+")"+Type.getDescriptor(TaintedIntWithObjTag.class))) {
//                int rtnHodler = lvs.getTmpLV();
//                super.visitVarInsn(ASTORE, rtnHodler);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedIntWithObjTag.class));
//                super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedIntWithObjTag.class), "val", "I");
//                int len = lvs.getTmpLV();
//                super.visitVarInsn(ISTORE, len);
//                taintBytes(0, 2, 4, len);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                lvs.freeTmpLV(rtnHodler);
//                lvs.freeTmpLV(len);
//            } else if (opcode == ARETURN && name.equals("read$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+"J"+Configuration.TAINT_TAG_DESC+Type.getDescriptor(LazyByteArrayObjTags.class)+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+"I"+Configuration.TAINT_TAG_DESC+Type.getDescriptor(TaintedIntWithObjTag.class)+")"+Type.getDescriptor(TaintedIntWithObjTag.class))) {
//            	int rtnHodler = lvs.getTmpLV();
//                super.visitVarInsn(ASTORE, rtnHodler);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedIntWithObjTag.class));
//                super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedIntWithObjTag.class), "val", "I");
//                int len = lvs.getTmpLV();
//                super.visitVarInsn(ISTORE, len);
//                taintBytes(0, 5, 7, len);//long type parameter will occupy two places: long, long_2nd
//                super.visitVarInsn(ALOAD, rtnHodler);
//                lvs.freeTmpLV(rtnHodler);
//                lvs.freeTmpLV(len);
//            } else if(opcode == ARETURN && name.equals("read$$PHOSPHORTAGGED") && desc.equals("("+Configuration.TAINT_TAG_DESC+"Ljava/nio/ByteBuffer;"+Configuration.TAINT_TAG_DESC+Type.getDescriptor(TaintedIntWithObjTag.class)+")"+Type.getDescriptor(TaintedIntWithObjTag.class))) {
//                int rtnHodler = lvs.getTmpLV();
//                super.visitVarInsn(ASTORE, rtnHodler);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                super.visitTypeInsn(CHECKCAST, Type.getInternalName(TaintedIntWithObjTag.class));
//                super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedIntWithObjTag.class), "val", "I");
//                int len = lvs.getTmpLV();
//                super.visitVarInsn(ISTORE, len);
//                super.visitVarInsn(ALOAD, 2);
//                super.visitTypeInsn(CHECKCAST, "java/nio/ByteBuffer");
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", TaintUtils.FAV_GETBUFFERSHADOW_MT, "()"+Type.getDescriptor(LazyByteArrayObjTags.class), false);
//                super.visitTypeInsn(Opcodes.CHECKCAST,Type.getInternalName(LazyByteArrayObjTags.class));
//                int bytes = lvs.getTmpLV();
//                super.visitVarInsn(ASTORE, bytes);
//                taintBytes(0, bytes, bufferPos, len);
//                super.visitVarInsn(ALOAD, rtnHodler);
//                lvs.freeTmpLV(rtnHodler);
//                lvs.freeTmpLV(len);
//                lvs.freeTmpLV(bytes);
//            }
//        }
    }
    private int bufferPos = -1;

    public void taintBytes(int input, int bytes, int off, int len) {
        Label finish = new Label();
        FrameNode fn1 = getCurrentFrameNode();
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(Opcodes.INSTANCEOF, "java/io/DataInputStream");
        super.visitJumpInsn(Opcodes.IFEQ, finish);
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
        super.visitJumpInsn(Opcodes.IFNULL, finish);

        super.visitVarInsn(ALOAD, bytes);
        super.visitTypeInsn(CHECKCAST, Type.getInternalName(LazyByteArrayObjTags.class));
        super.visitVarInsn(ILOAD, off);
        super.visitVarInsn(ILOAD, len);
        super.visitLdcInsn(this.owner);
        super.visitLdcInsn(this.name);
        super.visitLdcInsn(this.desc);
        super.visitLdcInsn(FAVTaintType.HDFSREAD.toString());
        super.visitLdcInsn(FAVTagType.APP.toString());
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
        FAV_APP_TAINT_BYTES.delegateVisit(mv);
        super.visitVarInsn(ASTORE, bytes);

        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitInsn(ACONST_NULL);
        super.visitFieldInsn(PUTFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");

        super.visitLabel(finish);
        acceptFn(fn1);
    }
    public void taintBytesFully(int input, int bytes) {
        Label finish = new Label();
        FrameNode fn1 = getCurrentFrameNode();
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(Opcodes.INSTANCEOF, "java/io/DataInputStream");
        super.visitJumpInsn(Opcodes.IFEQ, finish);
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
        super.visitJumpInsn(Opcodes.IFNULL, finish);

        super.visitVarInsn(ALOAD, bytes);
        super.visitTypeInsn(CHECKCAST, Type.getInternalName(LazyByteArrayObjTags.class));
        super.visitLdcInsn(this.owner);
        super.visitLdcInsn(this.name);
        super.visitLdcInsn(this.desc);
        super.visitLdcInsn(FAVTaintType.HDFSREAD.toString());
        super.visitLdcInsn(FAVTagType.APP.toString());
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
        FAV_APP_TAINT_BYTES_FULLY.delegateVisit(mv);
        super.visitVarInsn(ASTORE, bytes);

        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitInsn(ACONST_NULL);
        super.visitFieldInsn(PUTFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");

        super.visitLabel(finish);
        acceptFn(fn1);
    }
    public void triggerFaultReadBefore(int input) {
        Label finish = new Label();
        FrameNode fn1 = getCurrentFrameNode();
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(Opcodes.INSTANCEOF, "java/io/DataInputStream");
        super.visitJumpInsn(Opcodes.IFEQ, finish);
        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
        super.visitJumpInsn(Opcodes.IFNULL, finish);

        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitFieldInsn(GETFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
        int path = lvs.getTmpLV();
        super.visitVarInsn(ASTORE, path);

//        super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
//        super.visitInsn(Opcodes.DUP);
//        super.visitLdcInsn("haha");
//        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
//        super.visitVarInsn(Opcodes.ALOAD, path);
//        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
//        super.visitVarInsn(ASTORE, path);

        fauntTriggerBefore(path);

        super.visitVarInsn(ALOAD, input);
        super.visitTypeInsn(CHECKCAST, "java/io/DataInputStream");
        super.visitInsn(ACONST_NULL);
        super.visitFieldInsn(PUTFIELD, "java/io/DataInputStream", TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");

        super.visitLabel(finish);
        acceptFn(fn1);
    }
}
