package edu.iscas.tcse.favtrigger.instrumenter.hbase;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.iscas.tcse.favtrigger.instrumenter.yarn.YarnProtocols;
import edu.iscas.tcse.favtrigger.taint.FAVTaintType;
import edu.iscas.tcse.favtrigger.taint.Source.FAVTagType;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.*;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.*;
import org.objectweb.asm.tree.FrameNode;


public class HBaseTrackingMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;
    private boolean rpcRelated = false;

    public HBaseTrackingMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
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
    	for(String s:ownerInterfaces) {
    		if(s.endsWith("Service$BlockingInterface")) {
    			rpcRelated = true;
    			break;
    		}
    	}
    }

    private int remoteIpVar = -1;
    public void visitCode() {
//    	if(this.name.startsWith("addColumn$$")) {
//    		System.out.println("crashfuzz addColumn:"+this.owner+" "+this.name
//    				+" "+((this.ownerSuperCname.endsWith("Service$BlockingInterface") || rpcRelated)
//        			&& ((Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("("+Configuration.TAINT_TAG_DESC+"Lorg/apache/hbase/thirdparty/com/google/protobuf/RpcController;"))
//        					|| (!Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("("+Configuration.TAINT_TAG_DESC+"Lcom/google/protobuf/RpcController;")))
//        			&& !this.owner.endsWith("$BlockingStub")
//        			&& Configuration.USE_FAV && Configuration.FOR_HBASE)
//        			+" "+(this.owner.endsWith("Service$BlockingStub")
//        			&& ((Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("("+Configuration.TAINT_TAG_DESC+"Lorg/apache/hbase/thirdparty/com/google/protobuf/RpcController;"))
//        					|| (!Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("("+Configuration.TAINT_TAG_DESC+"Lcom/google/protobuf/RpcController;")))
//        			&& (this.ownerSuperCname.endsWith("Service$BlockingInterface") || rpcRelated)
//        			&& (Configuration.USE_FAV && Configuration.FOR_HBASE || Configuration.HBASE_RPC)));
//    	}

    	if((this.ownerSuperCname.endsWith("Service$BlockingInterface") || rpcRelated)
    			&& ((Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lorg/apache/hbase/thirdparty/com/google/protobuf/RpcController;"))
    					|| (!Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lcom/google/protobuf/RpcController;")))
    			&& !this.owner.endsWith("$BlockingStub")
    			&& Configuration.USE_FAV && Configuration.FOR_HBASE) {
    		//server side rpc
    		String requestType = YarnProtocols.getRequestProtoInternalTypeFromDesc(this.desc);
//            System.out.println("crashfuzz server receive:"+this.owner+" "+this.name);
            attachTaintTagsToMessage(2, requestType, this.owner, this.name, this.desc, FAVTaintType.RPC.toString(), FAVTagType.APP.toString(), true);


//    		super.visitLdcInsn("serverside "+owner+"."+name+desc);
//    		super.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/hbase/HBaseTrackingMV",
//    				"print", "(Ljava/lang/String;)V", false);
    	} else if(this.owner.endsWith("Service$BlockingStub")
    			&& ((Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lorg/apache/hbase/thirdparty/com/google/protobuf/RpcController;"))
    					|| (!Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lcom/google/protobuf/RpcController;")))
    			&& (this.ownerSuperCname.endsWith("Service$BlockingInterface") || rpcRelated)
    			&& (Configuration.USE_FAV && Configuration.FOR_HBASE || Configuration.HBASE_RPC)) {
    		//client side rpc
    		String requestType = YarnProtocols.getRequestProtoInternalTypeFromDesc(desc);
    		super.visitVarInsn(ALOAD, 0);
    		if(Configuration.IS_THIRD_PARTY_PROTO) {
    			super.visitFieldInsn(GETFIELD, this.owner, "channel", "Lorg/apache/hbase/thirdparty/com/google/protobuf/BlockingRpcChannel;");
                super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation");
                super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation",
                		"getAddr$$FAV", "()Ljava/net/InetSocketAddress;", false);
    		} else {
    			super.visitFieldInsn(GETFIELD, this.owner, "channel", "Lcom/google/protobuf/BlockingRpcChannel;");
                super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation");
                super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation",
                		"getAddr$$FAV", "()Ljava/net/InetSocketAddress;", false);
    		}
            int socketAddr = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, socketAddr);
//            System.out.println("crashfuzz client send:"+this.owner+" "+this.name);
    		recordOrTriggerRpcRequest(socketAddr, requestType);


//    		super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
//    		super.visitInsn(Opcodes.DUP);
//    		super.visitLdcInsn("clientside "+this.owner+this.name+" |invoke| "+owner+"."+name+desc);
//    		super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
//    		int sBuilder = lvs.getTmpLV();
//    		super.visitVarInsn(ASTORE, sBuilder);
//
//    		super.visitVarInsn(ALOAD, sBuilder);
//            super.visitVarInsn(ALOAD, 0);
//            super.visitFieldInsn(GETFIELD, this.owner, "channel", "Lorg/apache/hbase/thirdparty/com/google/protobuf/BlockingRpcChannel;");
//            super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation");
//            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation",
//            		"getAddr$$FAV", "()Ljava/net/InetSocketAddress;", false);
//            super.visitMethodInsn(INVOKEVIRTUAL, "java/net/InetSocketAddress",
//            		"toString", "()Ljava/lang/String;", false);
//            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//            super.visitVarInsn(ASTORE, sBuilder);
//
//            super.visitVarInsn(ALOAD, sBuilder);
//    		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
//    		super.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/hbase/HBaseTrackingMV",
//    				"print", "(Ljava/lang/String;)V", false);
//    		lvs.freeTmpLV(sBuilder);
   		lvs.freeTmpLV(socketAddr);
    	}
        super.visitCode();
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        if(Configuration.USE_FAV && Configuration.FOR_HBASE || Configuration.HBASE_RPC) {
        	if(owner.endsWith("Service$BlockingInterface") && !this.name.equals("callBlockingMethod")) {//avoid server side calls
//        		Type[] args = Type.getArgumentTypes(desc);
//                int[] vars = new int[args.length];
//                Type rtnType = Type.getReturnType(desc);
//                for(int i = args.length - 1; i >= 0; i--) {
//                    vars[i] = lvs.getTmpLV();
//                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
//                        super.visitVarInsn(ASTORE, vars[i]);
//                    } else if(args[i].getSort() == Type.DOUBLE) {
//                        super.visitVarInsn(Opcodes.DSTORE, vars[i]);
//                    } else if(args[i].getSort() == Type.LONG) {
//                        super.visitVarInsn(Opcodes.LSTORE, vars[i]);
//                    } else if(args[i].getSort() == Type.FLOAT) {
//                        super.visitVarInsn(Opcodes.FSTORE, vars[i]);
//                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
//                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
//                            || args[i].getSort() == Type.BOOLEAN) {
//                        super.visitVarInsn(ISTORE, vars[i]);
//                    } else {
//                        //this would not happen
//                    }
//                }
//                int proxy = lvs.getTmpLV();
//                super.visitVarInsn(ASTORE, proxy);
//
//        		super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
//        		super.visitInsn(Opcodes.DUP);
//        		super.visitLdcInsn("clientside "+this.owner+this.name+" |invoke| "+owner+"."+name+desc);
//        		super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
//        		int sBuilder = lvs.getTmpLV();
//        		super.visitVarInsn(ASTORE, sBuilder);
//
//        		FrameNode fn = getCurrentFrameNode();
//                super.visitVarInsn(ALOAD, proxy);
//                super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hbase/regionserver/RSRpcServices");
//                Label finish = new Label();
//                super.visitJumpInsn(Opcodes.IFEQ, finish);
//                super.visitVarInsn(ALOAD, sBuilder);
//                super.visitLdcInsn(" is RSRpcServices ");
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//                super.visitVarInsn(ALOAD, proxy);
//                super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/hbase/regionserver/RSRpcServices");
//                super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/hbase/regionserver/RSRpcServices",
//                		"getSocketAddress", "()Ljava/net/InetSocketAddress;", false);
//                super.visitMethodInsn(INVOKEVIRTUAL, "java/net/InetSocketAddress",
//                		"toString", "()Ljava/lang/String;", false);
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//                super.visitVarInsn(ASTORE, sBuilder);
//                super.visitLabel(finish);
//                acceptFn(fn);
//
//                fn = getCurrentFrameNode();
//                super.visitVarInsn(ALOAD, proxy);
//                super.visitTypeInsn(Opcodes.INSTANCEOF, "org/apache/hadoop/hbase/master/MasterRpcServices");
//                Label finish1 = new Label();
//                super.visitJumpInsn(Opcodes.IFEQ, finish1);
//                super.visitVarInsn(ALOAD, sBuilder);
//                super.visitLdcInsn(" is MasterRpcServices ");
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//                super.visitVarInsn(ALOAD, proxy);
//                super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/hbase/master/MasterRpcServices");
//                super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/hbase/master/MasterRpcServices",
//                		"getSocketAddress", "()Ljava/net/InetSocketAddress;", false);
//                super.visitMethodInsn(INVOKEVIRTUAL, "java/net/InetSocketAddress",
//                		"toString", "()Ljava/lang/String;", false);
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//                super.visitVarInsn(ASTORE, sBuilder);
//                super.visitLabel(finish1);
//                acceptFn(fn);
//
//                fn = getCurrentFrameNode();
//                super.visitVarInsn(ALOAD, proxy);
//                String insInterName = owner.replaceAll("BlockingInterface", "BlockingStub");
//                super.visitTypeInsn(Opcodes.INSTANCEOF, insInterName);
//                Label finish2 = new Label();
//                super.visitJumpInsn(Opcodes.IFEQ, finish2);
//                super.visitVarInsn(ALOAD, sBuilder);
//                super.visitLdcInsn(" is BlockingStub ");
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//                super.visitVarInsn(ALOAD, proxy);
//                super.visitTypeInsn(CHECKCAST, insInterName);
//                super.visitFieldInsn(GETFIELD, insInterName, "channel", "Lorg/apache/hbase/thirdparty/com/google/protobuf/BlockingRpcChannel;");
//                super.visitTypeInsn(CHECKCAST, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation");
//                super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation",
//                		"getAddr$$FAV", "()Ljava/net/InetSocketAddress;", false);
//                super.visitMethodInsn(INVOKEVIRTUAL, "java/net/InetSocketAddress",
//                		"toString", "()Ljava/lang/String;", false);
//                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//                super.visitVarInsn(ASTORE, sBuilder);
//                super.visitLabel(finish2);
//                acceptFn(fn);
//
//        		super.visitVarInsn(ALOAD, sBuilder);
//        		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
//        		super.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/hbase/HBaseTrackingMV",
//        				"print", "(Ljava/lang/String;)V", false);
//
//        		super.visitVarInsn(ALOAD, proxy);
//                for(int i = 0; i < args.length; i++) {
//                    if(args[i].getSort() == Type.OBJECT || args[i].getSort() == Type.ARRAY) {
//                        super.visitVarInsn(ALOAD, vars[i]);
//                    } else if(args[i].getSort() == Type.DOUBLE) {
//                        super.visitVarInsn(Opcodes.DLOAD, vars[i]);
//                    } else if(args[i].getSort() == Type.LONG) {
//                        super.visitVarInsn(Opcodes.LLOAD, vars[i]);
//                    } else if(args[i].getSort() == Type.FLOAT) {
//                        super.visitVarInsn(Opcodes.FLOAD, vars[i]);
//                    } else if(args[i].getSort() == Type.INT || args[i].getSort() == Type.SHORT
//                            || args[i].getSort() == Type.BYTE || args[i].getSort() == Type.CHAR
//                            || args[i].getSort() == Type.BOOLEAN) {
//                        super.visitVarInsn(ILOAD, vars[i]);
//                    } else {
//                        //this would not happen
//                    }
//                }
//        		lvs.freeTmpLV(sBuilder);
//        		for(int i = 0; i < vars.length; i++) {
//                    lvs.freeTmpLV(vars[i]);
//                }
//        		lvs.freeTmpLV(proxy);
        	}
        }
        super.visitMethodInsn(opcode, owner, name, desc, isInterface);
    }

    public void recordOrTriggerRpcRequest(int socketAddr, String msgInternalT) {
//        int response = lvs.getTmpLV();
//        super.visitVarInsn(Opcodes.ASTORE, response);
//        int rtnholder = lvs.getTmpLV();
//        super.visitVarInsn(Opcodes.ASTORE, rtnholder);
//        int requestTaint = lvs.getTmpLV();
//        super.visitVarInsn(Opcodes.ASTORE, requestTaint);
//        int request = lvs.getTmpLV();
//        super.visitVarInsn(Opcodes.ASTORE, request);

    	FAV_NEW_MSGID.delegateVisit(mv);
        int msgid = lvs.getTmpLV();
        super.visitVarInsn(ISTORE, msgid);
        //add msg id to request
    	if(Configuration.IS_THIRD_PARTY_PROTO) {
    		super.visitMethodInsn(INVOKESTATIC, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field",
                    "newBuilder", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder;", false);
//            FAV_NEW_MSGID.delegateVisit(mv);
//            int msgid = lvs.getTmpLV();
//            super.visitVarInsn(ISTORE, msgid);
            FAV_CURRENT_IP.delegateVisit(mv);
            super.visitVarInsn(ILOAD, msgid);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
            super.visitMethodInsn(INVOKESTATIC, "org/apache/hbase/thirdparty/com/google/protobuf/ByteString", "copyFrom", "([B)Lorg/apache/hbase/thirdparty/com/google/protobuf/ByteString;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "addLengthDelimited", "(Lorg/apache/hbase/thirdparty/com/google/protobuf/ByteString;)Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "build", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field;", false);
            int field = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, field);
            super.visitMethodInsn(INVOKESTATIC, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet",
                    "newBuilder", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitIntInsn(Opcodes.SIPUSH, Configuration.PROTO_MSG_ID_TAG);
            super.visitVarInsn(ALOAD, field);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder",
                    "addField", "(ILorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field;)Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder",
                    "build", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet;", false);
            int newunknown = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, newunknown);
            super.visitVarInsn(ALOAD, 2);
            String msgBuilderInternal = msgInternalT+"$Builder";
            super.visitMethodInsn(INVOKEVIRTUAL, msgInternalT,
                    "toBuilder", "()L"+msgBuilderInternal+";", false);
            super.visitVarInsn(ALOAD, newunknown);

            //NOTE:!!! hbase have diffirent return type for mergeUnknownFields, compared to hadoop
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal,
                    "mergeUnknownFields", "(Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet;)L"+msgBuilderInternal+";", false);
            super.visitTypeInsn(Opcodes.CHECKCAST,msgBuilderInternal);

            String msgDesc = "L"+msgInternalT+";";
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal,
                    "build", "()"+msgDesc, false);
            super.visitVarInsn(ASTORE, 2);
            //
            lvs.freeTmpLV(field);
            lvs.freeTmpLV(newunknown);
    	} else {
    		super.visitMethodInsn(INVOKESTATIC, "com/google/protobuf/UnknownFieldSet$Field",
                    "newBuilder", "()Lcom/google/protobuf/UnknownFieldSet$Field$Builder;", false);
//            FAV_NEW_MSGID.delegateVisit(mv);
//            int msgid = lvs.getTmpLV();
//            super.visitVarInsn(ISTORE, msgid);
            FAV_CURRENT_IP.delegateVisit(mv);
            super.visitVarInsn(ILOAD, msgid);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
            super.visitMethodInsn(INVOKESTATIC, "com/google/protobuf/ByteString", "copyFrom", "([B)Lcom/google/protobuf/ByteString;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "addLengthDelimited", "(Lcom/google/protobuf/ByteString;)Lcom/google/protobuf/UnknownFieldSet$Field$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "build", "()Lcom/google/protobuf/UnknownFieldSet$Field;", false);
            int field = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, field);
            super.visitMethodInsn(INVOKESTATIC, "com/google/protobuf/UnknownFieldSet",
                    "newBuilder", "()Lcom/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitIntInsn(Opcodes.SIPUSH, Configuration.PROTO_MSG_ID_TAG);
            super.visitVarInsn(ALOAD, field);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Builder",
                    "addField", "(ILcom/google/protobuf/UnknownFieldSet$Field;)Lcom/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Builder",
                    "build", "()Lcom/google/protobuf/UnknownFieldSet;", false);
            int newunknown = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, newunknown);
            super.visitVarInsn(ALOAD, 2);
            String msgBuilderInternal = msgInternalT+"$Builder";
            super.visitMethodInsn(INVOKEVIRTUAL, msgInternalT,
                    "toBuilder", "()L"+msgBuilderInternal+";", false);
            super.visitVarInsn(ALOAD, newunknown);

            //NOTE:!!! hbase have diffirent return type for mergeUnknownFields, compared to hadoop
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal,
                    "mergeUnknownFields", "(Lcom/google/protobuf/UnknownFieldSet;)Lcom/google/protobuf/GeneratedMessage$Builder;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST,msgBuilderInternal);

            String msgDesc = "L"+msgInternalT+";";
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal,
                    "build", "()"+msgDesc, false);
            super.visitVarInsn(ASTORE, 2);
            //
            lvs.freeTmpLV(field);
            lvs.freeTmpLV(newunknown);
    	}

        if(Configuration.USE_FAV && Configuration.FOR_HBASE) {
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
            super.visitVarInsn(ALOAD, socketAddr);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
            super.visitVarInsn(ILOAD, msgid);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
            APP_FAULT_BEFORE.delegateVisit(mv);
            lvs.freeTmpLV(fileOutStream);
        }

//        super.visitVarInsn(Opcodes.ALOAD, request);
//        super.visitVarInsn(Opcodes.ALOAD, requestTaint);
//        super.visitVarInsn(Opcodes.ALOAD, rtnholder);
//        super.visitVarInsn(Opcodes.ALOAD, response);
//
//        lvs.freeTmpLV(request);
//        lvs.freeTmpLV(requestTaint);
//        lvs.freeTmpLV(rtnholder);
//        lvs.freeTmpLV(response);
        lvs.freeTmpLV(msgid);
    }

    public void visitInsn(int opcode) {
        //use lvs would introduce errors, avoid to use lvs
        if(opcode == ARETURN) {
        	if((this.ownerSuperCname.endsWith("Service$BlockingInterface") || rpcRelated)
        			&& ((Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lorg/apache/hbase/thirdparty/com/google/protobuf/RpcController;"))
        					|| (!Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lcom/google/protobuf/RpcController;")))
        			&& !this.owner.endsWith("$BlockingStub")
        			&& (Configuration.USE_FAV && Configuration.FOR_HBASE || Configuration.HBASE_RPC)) {
        		//server side send response
        		String responseType = YarnProtocols.getResponseProtoInternalTypeFromDesc(desc);

//                System.out.println("crashfuzz server send:"+this.owner+"."+this.name);
                recordOrTriggerRpcResponse(responseType);
        	} else if(this.owner.endsWith("Service$BlockingStub")
        			&& ((Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lorg/apache/hbase/thirdparty/com/google/protobuf/RpcController;"))
        					|| (!Configuration.IS_THIRD_PARTY_PROTO && this.desc.startsWith("(Lcom/google/protobuf/RpcController;")))
        			&& (this.ownerSuperCname.endsWith("Service$BlockingInterface") || rpcRelated)
        			&& Configuration.USE_FAV && Configuration.FOR_HBASE) {
        		//client side receive response
        		String responseType = YarnProtocols.getResponseProtoInternalTypeFromDesc(desc);
//                super.visitTypeInsn(CHECKCAST, responseType);
        		int res = lvs.getTmpLV();
                super.visitVarInsn(ASTORE, res);
                attachTaintTagsToMessage(res, responseType, this.owner, this.name, this.desc, FAVTaintType.RPC.toString(), FAVTagType.APP.toString(), false);
                super.visitVarInsn(ALOAD, res);

                lvs.freeTmpLV(res);
        	}
        }
        super.visitInsn(opcode);
    }

    public void attachTaintTagsToMessage(int msgVar, String msgInternalType, String cname, String mname,
            String desc, String type, String tag, boolean recordRemoteIP) {
//    	System.out.println("crashfuzz:"+cname);
        //crash fuzz record read
        FAV_GET_RECORD_OUT.delegateVisit(mv);
        int fileOutStream = lvs.getTmpLV();
        super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

        Label nullOutStream = new Label();
        FrameNode outFn = getCurrentFrameNode();
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
        super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

        super.visitLabel(nullOutStream);
        acceptFn(outFn);

        FAV_GET_TIMESTAMP.delegateVisit(mv);
        super.visitVarInsn(Opcodes.ALOAD, fileOutStream);

        if(Configuration.IS_THIRD_PARTY_PROTO) {
            super.visitVarInsn(ALOAD, msgVar);
            super.visitMethodInsn(INVOKEVIRTUAL, msgInternalType,
                    "getUnknownFields", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet;", false);
            super.visitIntInsn(Opcodes.SIPUSH, Configuration.PROTO_MSG_ID_TAG);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet",
                    "getField", "(I)Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field",
                    "getLengthDelimitedList", "()Ljava/util/List;", false);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "getLinkSourceFromMsg", "(Ljava/util/List;)Ljava/lang/Object;", false);
            int rawsource = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, rawsource);
            super.visitLdcInsn(FAVPathType.FAVMSG.toString()+":");
            int stringsource = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, stringsource);
            Label done = new Label();
            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(ALOAD, rawsource);
            super.visitTypeInsn(INSTANCEOF, "org/apache/hbase/thirdparty/com/google/protobuf/ByteString");
            super.visitJumpInsn(Opcodes.IFEQ, done);
            super.visitVarInsn(ALOAD, rawsource);
            super.visitTypeInsn(CHECKCAST, "org/apache/hbase/thirdparty/com/google/protobuf/ByteString");
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/ByteString",
                    "toStringUtf8", "()Ljava/lang/String;", false);
            super.visitVarInsn(ASTORE, stringsource);
            super.visitLabel(done);
            acceptFn(fn);
            if(recordRemoteIP) {
                super.visitVarInsn(ALOAD, stringsource);
                super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                        "getRemoteAddrFromSource", "(Ljava/lang/String;)Ljava/lang/String;", false);
                remoteIpVar = lvs.createPermanentLocalVariable(String.class, "FAV_REMOTE_IP");
                super.visitVarInsn(ASTORE, remoteIpVar);
            }
            super.visitVarInsn(ALOAD, stringsource);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "appendRead", "(Ljava/lang/String;)Ljava/lang/String;", false);
            lvs.freeTmpLV(stringsource);
            lvs.freeTmpLV(rawsource);
        } else {
        	super.visitVarInsn(ALOAD, msgVar);
            super.visitMethodInsn(INVOKEVIRTUAL, msgInternalType,
                    "getUnknownFields", "()Lcom/google/protobuf/UnknownFieldSet;", false);
            super.visitIntInsn(Opcodes.SIPUSH, Configuration.PROTO_MSG_ID_TAG);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet",
                    "getField", "(I)Lcom/google/protobuf/UnknownFieldSet$Field;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Field",
                    "getLengthDelimitedList", "()Ljava/util/List;", false);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "getLinkSourceFromMsg", "(Ljava/util/List;)Ljava/lang/Object;", false);
            int rawsource = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, rawsource);
            super.visitLdcInsn(FAVPathType.FAVMSG.toString()+":");
            int stringsource = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, stringsource);
            Label done = new Label();
            FrameNode fn = getCurrentFrameNode();
            super.visitVarInsn(ALOAD, rawsource);
            super.visitTypeInsn(INSTANCEOF, "com/google/protobuf/ByteString");
            super.visitJumpInsn(Opcodes.IFEQ, done);
            super.visitVarInsn(ALOAD, rawsource);
            super.visitTypeInsn(CHECKCAST, "com/google/protobuf/ByteString");
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/ByteString",
                    "toStringUtf8", "()Ljava/lang/String;", false);
            super.visitVarInsn(ASTORE, stringsource);
            super.visitLabel(done);
            acceptFn(fn);
            if(recordRemoteIP) {
                super.visitVarInsn(ALOAD, stringsource);
                super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                        "getRemoteAddrFromSource", "(Ljava/lang/String;)Ljava/lang/String;", false);
                remoteIpVar = lvs.createPermanentLocalVariable(String.class, "FAV_REMOTE_IP");
                super.visitVarInsn(ASTORE, remoteIpVar);
            }
            super.visitVarInsn(ALOAD, stringsource);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "appendRead", "(Ljava/lang/String;)Ljava/lang/String;", false);
            lvs.freeTmpLV(stringsource);
            lvs.freeTmpLV(rawsource);
        }

        APP_FAULT_BEFORE.delegateVisit(mv);
        //[rtn]

        lvs.freeTmpLV(fileOutStream);
    }

    public void recordOrTriggerRpcResponse(String msgInternalT) {
        int response = lvs.getTmpLV();
        super.visitVarInsn(ASTORE, response);


        FAV_NEW_MSGID.delegateVisit(mv);
        int msgid = lvs.getTmpLV();
        super.visitVarInsn(ISTORE, msgid);
        if(Configuration.IS_THIRD_PARTY_PROTO) {
        	super.visitMethodInsn(INVOKESTATIC, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet",
                    "newBuilder", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitIntInsn(Opcodes.SIPUSH, Configuration.PROTO_MSG_ID_TAG);
            //[response, UnknownFieldSet$Builder, msgTag]
            super.visitMethodInsn(INVOKESTATIC, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field",
                    "newBuilder", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder;", false);
            FAV_CURRENT_IP.delegateVisit(mv);
//            FAV_NEW_MSGID.delegateVisit(mv);
//            int msgid = lvs.getTmpLV();
//            super.visitVarInsn(ISTORE, msgid);
            super.visitVarInsn(ILOAD, msgid);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
            super.visitMethodInsn(INVOKESTATIC, "org/apache/hbase/thirdparty/com/google/protobuf/ByteString", "copyFrom", "([B)Lorg/apache/hbase/thirdparty/com/google/protobuf/ByteString;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "addLengthDelimited", "(Lorg/apache/hbase/thirdparty/com/google/protobuf/ByteString;)Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "build", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field;", false);
            //[response, UnknownFieldSet$Builder, msgTag, Field]
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder",
                    "addField", "(ILorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Field;)Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet$Builder",
                    "build", "()Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet;", false);
            int newunknown = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, newunknown);
//            //[response, UnknownFieldSet]
//            super.visitInsn(SWAP);
//            //[UnknownFieldSet, response]
            String msgBuilderInternal = msgInternalT+"$Builder";
            super.visitVarInsn(ALOAD, response);
            super.visitMethodInsn(INVOKEVIRTUAL, msgInternalT,
                    "toBuilder", "()L"+msgBuilderInternal+";", false);
            super.visitVarInsn(ALOAD, newunknown);
//            //[UnknownFieldSet, response$builder]
//            super.visitInsn(SWAP);
//            //[response$builder, UnknownFieldSet]
    //
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal,
          "mergeUnknownFields", "(Lorg/apache/hbase/thirdparty/com/google/protobuf/UnknownFieldSet;)L"+msgBuilderInternal+";", false);
//            super.visitTypeInsn(Opcodes.CHECKCAST,msgBuilderInternal);
            String msgDesc = "L"+msgInternalT+";";
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal, "build", "()"+msgDesc, false);
            super.visitVarInsn(ASTORE, response);
//            //[newresponse]
            lvs.freeTmpLV(newunknown);
        } else {
        	super.visitMethodInsn(INVOKESTATIC, "com/google/protobuf/UnknownFieldSet",
                    "newBuilder", "()Lcom/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitIntInsn(Opcodes.SIPUSH, Configuration.PROTO_MSG_ID_TAG);
            //[response, UnknownFieldSet$Builder, msgTag]
            super.visitMethodInsn(INVOKESTATIC, "com/google/protobuf/UnknownFieldSet$Field",
                    "newBuilder", "()Lcom/google/protobuf/UnknownFieldSet$Field$Builder;", false);
            FAV_CURRENT_IP.delegateVisit(mv);
//            FAV_NEW_MSGID.delegateVisit(mv);
//            int msgid = lvs.getTmpLV();
//            super.visitVarInsn(ISTORE, msgid);
            super.visitVarInsn(ILOAD, msgid);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B", false);
            super.visitMethodInsn(INVOKESTATIC, "com/google/protobuf/ByteString", "copyFrom", "([B)Lcom/google/protobuf/ByteString;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "addLengthDelimited", "(Lcom/google/protobuf/ByteString;)Lcom/google/protobuf/UnknownFieldSet$Field$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Field$Builder",
                    "build", "()Lcom/google/protobuf/UnknownFieldSet$Field;", false);
            //[response, UnknownFieldSet$Builder, msgTag, Field]
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Builder",
                    "addField", "(ILcom/google/protobuf/UnknownFieldSet$Field;)Lcom/google/protobuf/UnknownFieldSet$Builder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "com/google/protobuf/UnknownFieldSet$Builder",
                    "build", "()Lcom/google/protobuf/UnknownFieldSet;", false);
            int newunknown = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, newunknown);
//            //[response, UnknownFieldSet]
//            super.visitInsn(SWAP);
//            //[UnknownFieldSet, response]
            String msgBuilderInternal = msgInternalT+"$Builder";
            super.visitVarInsn(ALOAD, response);
            super.visitMethodInsn(INVOKEVIRTUAL, msgInternalT,
                    "toBuilder", "()L"+msgBuilderInternal+";", false);
            super.visitVarInsn(ALOAD, newunknown);
//            //[UnknownFieldSet, response$builder]
//            super.visitInsn(SWAP);
//            //[response$builder, UnknownFieldSet]
    //
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal,
          "mergeUnknownFields", "(Lcom/google/protobuf/UnknownFieldSet;)Lcom/google/protobuf/GeneratedMessage$Builder;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST,msgBuilderInternal);
            String msgDesc = "L"+msgInternalT+";";
            super.visitMethodInsn(INVOKEVIRTUAL, msgBuilderInternal, "build", "()"+msgDesc, false);
//            //[newresponse]
            super.visitVarInsn(ASTORE, response);
            lvs.freeTmpLV(newunknown);
        }

        if(Configuration.USE_FAV && Configuration.FOR_MR) {
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
            //get remote ip from remoteIpVar
            super.visitVarInsn(ALOAD, remoteIpVar);
            super.visitVarInsn(ILOAD, msgid);
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
            APP_FAULT_BEFORE.delegateVisit(mv);
            //[rtn]

            lvs.freeTmpLV(fileOutStream);
        }

        super.visitVarInsn(ALOAD, response);
        lvs.freeTmpLV(response);
        lvs.freeTmpLV(msgid);
    }

    public static void print(String s) {
    	StackTraceElement[] callStack;
    	callStack = Thread.currentThread().getStackTrace();
    	List<String> callStackString = new ArrayList<String>();
    	for(int i = 0; i < callStack.length; ++i) {
    		callStackString.add(callStack[i].toString());
    	}
    	System.out.println("FAV HBASE: "+s+" "+callStackString);
    }
}
