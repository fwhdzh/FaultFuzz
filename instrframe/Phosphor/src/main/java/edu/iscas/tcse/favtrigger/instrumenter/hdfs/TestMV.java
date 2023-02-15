package edu.iscas.tcse.favtrigger.instrumenter.hdfs;

import edu.columbia.cs.psl.phosphor.Configuration;

import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;

import java.util.Arrays;

import org.objectweb.asm.*;

public class TestMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;

    public TestMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
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

    private int remoteIpVar = -1;

    @Override
	public void visitParameter(String name, int access) {
		// TODO Auto-generated method stub
    	if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitParameter: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitParameter(name, access);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitAnnotationDefault: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		return super.visitAnnotationDefault();
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitAnnotation: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		return super.visitAnnotation(descriptor, visible);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitTypeAnnotation: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitAnnotableParameterCount: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitAnnotableParameterCount(parameterCount, visible);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitParameterAnnotation: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		return super.visitParameterAnnotation(parameter, descriptor, visible);
	}

	@Override
	public void visitAttribute(Attribute attribute) {
		// TODO Auto-generated method stub

		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitAttribute: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitAttribute(attribute);
	}

	@Override
	public void visitCode() {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitCode: "+"LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitCode();
	}

	@Override
	public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
			System.out.println("haha before visitFrame: "+" local:"+Arrays.asList(local) +",   stack:"+Arrays.asList(stack));
    		System.out.println("haha before visitFrame: "+" LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitFrame(type, numLocal, local, numStack, stack);
	}

	@Override
	public void visitInsn(int opcode) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitInsn: "+opcode+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitInsn(opcode);
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitIntInsn: "+opcode+"|"+operand+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitIntInsn(opcode, operand);
	}

	@Override
	public void visitVarInsn(int opcode, int var) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitVarInsn: "+opcode+"|"+var+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitVarInsn(opcode, var);
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitTypeInsn: "+opcode+"|"+type+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitTypeInsn(opcode, type);
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitFieldInsn: "+opcode+"|"+owner+"|"+name+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitFieldInsn(opcode, owner, name, descriptor);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
		// TODO Auto-generated method stub
		super.visitMethodInsn(opcode, owner, name, descriptor);
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
			Object... bootstrapMethodArguments) {
		// TODO Auto-generated method stub
		super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitJumpInsn: "+opcode+"|"+label+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitJumpInsn(opcode, label);
	}

	@Override
	public void visitLabel(Label label) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitLabel: "+label+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitLabel(label);
	}

	@Override
	public void visitLdcInsn(Object value) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitLdcInsn: "+value+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitLdcInsn(value);
	}

	@Override
	public void visitIincInsn(int var, int increment) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitIincInsn: "+var+"|"+increment+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitIincInsn(var, increment);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
		// TODO Auto-generated method stub
		super.visitTableSwitchInsn(min, max, dflt, labels);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		// TODO Auto-generated method stub
		super.visitLookupSwitchInsn(dflt, keys, labels);
	}

	@Override
	public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
		// TODO Auto-generated method stub
		super.visitMultiANewArrayInsn(descriptor, numDimensions);
	}

	@Override
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
		// TODO Auto-generated method stub
		return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitTryCatchBlock: "+start+"|"+end+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor,
			boolean visible) {
		// TODO Auto-generated method stub
		return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
	}

	@Override
	public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end,
			int index) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitLocalVariable: "+name+"|"+descriptor+"|"+signature+"|"+start+"|"+end+"|"+index
    				+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitLocalVariable(name, descriptor, signature, start, end, index);
	}

	@Override
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
			int[] index, String descriptor, boolean visible) {
		// TODO Auto-generated method stub
		return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
	}

	@Override
	public void visitLineNumber(int line, Label start) {
		// TODO Auto-generated method stub
		super.visitLineNumber(line, start);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitMaxs: "+maxStack+"|"+maxLocals
    				+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public void visitEnd() {
		// TODO Auto-generated method stub
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitEnd: "
    				+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
		super.visitEnd();
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
    			&& this.name.equals("registerDatanode")) {
    		System.out.println("haha before visitMethodInsn: "+opcode+"|"+owner+"|"+name+"|"+desc+"|"+isInterface
    				+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
    	}
//    	if(this.owner.endsWith("DFSAdmin") && this.name.startsWith("setSafeMode")
//    			&& (name.startsWith("setSafeMode")
//    					|| name.startsWith("getProxiesForAllNameNodesInNameservice"))) {
//    		super.visitLdcInsn(name+desc);
//    		super.visitMethodInsn(INVOKESTATIC,
//    				"edu/iscas/tcse/favtrigger/instrumenter/hdfs/HDFSInstrument", "recordString", "(Ljava/lang/String;)V", false);
//    	}
        if(Configuration.USE_FAV && Configuration.FOR_HDFS || Configuration.HDFS_RPC) {
            if(HDFSProtocols.isHDFSProtocol(owner) && name.equals("registerDatanode")) {
            	super.visitLdcInsn(this.desc+" * "+desc+"  ");
        		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
            			&& this.name.equals("registerDatanode")) {
            		System.out.println("haha before getTmpLV: "
            				+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
            	}
        		org.objectweb.asm.tree.FrameNode fn = getCurrentFrameNode();
        		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
            			&& this.name.equals("registerDatanode")) {
            		System.out.println("haha before getTmpLV currentFrame: "
            				+", LOCALS:"+fn.local+",   STACKS:"+fn.stack);
            	}
                int fileOutStream = lvs.getTmpLV();
        		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
            			&& this.name.equals("registerDatanode")) {
            		System.out.println("haha after getTmpLV: "+fileOutStream
            				+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
            	}
                super.visitVarInsn(Opcodes.ASTORE, fileOutStream);
        		if(this.owner.equals("org/apache/hadoop/hdfs/protocolPB/DatanodeProtocolClientSideTranslatorPB")
            			&& this.name.equals("registerDatanode")) {
            		System.out.println("haha after ASTORE to tmp: "
            				+", LOCALS:"+this.analyzer.locals+",   STACKS:"+this.analyzer.stack);
            	}
            	super.visitLdcInsn(this.owner+" * "+owner+"  ");
            	super.visitLdcInsn(this.name+" * "+name+"  ");
            	super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            	super.visitMethodInsn(Opcodes.INVOKESTATIC,
            			"edu/iscas/tcse/favtrigger/instrumenter/hdfs/HDFSTrackingMV",
            			"test", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);

                //generate message id and record rpc call at client side
                String requestType = HDFSProtocols.getRequestProtoInternalTypeFromDesc(desc);
//                System.out.println("!!!!!!!!!GY "+this.owner+"."+this.name);
                Type[] argTypes = Type.getArgumentTypes(desc);
                int[] vars = new int[argTypes.length];

                lvs.freeTmpLV(fileOutStream);
	            /*
                vars[0] = lvs.getTmpLV();
                if(argTypes[argTypes.length - 1].getSort() == Type.OBJECT || argTypes[argTypes.length - 1].getSort() == Type.ARRAY) {
                    super.visitVarInsn(ASTORE, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.DOUBLE) {
                    super.visitVarInsn(Opcodes.DSTORE, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.LONG) {
                    super.visitVarInsn(Opcodes.LSTORE, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.FLOAT) {
                    super.visitVarInsn(Opcodes.FSTORE, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.INT || argTypes[argTypes.length - 1].getSort() == Type.SHORT
                        || argTypes[argTypes.length - 1].getSort() == Type.BYTE || argTypes[argTypes.length - 1].getSort() == Type.CHAR
                        || argTypes[argTypes.length - 1].getSort() == Type.BOOLEAN) {
                    super.visitVarInsn(ISTORE, vars[0]);
                } else {
                    //this would not happen
                }

                if(argTypes[argTypes.length - 1].getSort() == Type.OBJECT || argTypes[argTypes.length - 1].getSort() == Type.ARRAY) {
                    super.visitVarInsn(ALOAD, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.DOUBLE) {
                    super.visitVarInsn(Opcodes.DLOAD, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.LONG) {
                    super.visitVarInsn(Opcodes.LSTORE, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.FLOAT) {
                    super.visitVarInsn(Opcodes.FLOAD, vars[0]);
                } else if(argTypes[argTypes.length - 1].getSort() == Type.INT || argTypes[argTypes.length - 1].getSort() == Type.SHORT
                        || argTypes[argTypes.length - 1].getSort() == Type.BYTE || argTypes[argTypes.length - 1].getSort() == Type.CHAR
                        || argTypes[argTypes.length - 1].getSort() == Type.BOOLEAN) {
                    super.visitVarInsn(ILOAD, vars[0]);
                } else {
                    //this would not happen
                }
	            lvs.freeTmpLV(vars[0]);
	            for(int i = argTypes.length - 1; i >= 0; i--) {
	                vars[i] = lvs.getTmpLV();
	                if(argTypes[i].getSort() == Type.OBJECT || argTypes[i].getSort() == Type.ARRAY) {
	                    super.visitVarInsn(ASTORE, vars[i]);
	                } else if(argTypes[i].getSort() == Type.DOUBLE) {
	                    super.visitVarInsn(Opcodes.DSTORE, vars[i]);
	                } else if(argTypes[i].getSort() == Type.LONG) {
	                    super.visitVarInsn(Opcodes.LSTORE, vars[i]);
	                } else if(argTypes[i].getSort() == Type.FLOAT) {
	                    super.visitVarInsn(Opcodes.FSTORE, vars[i]);
	                } else if(argTypes[i].getSort() == Type.INT || argTypes[i].getSort() == Type.SHORT
	                        || argTypes[i].getSort() == Type.BYTE || argTypes[i].getSort() == Type.CHAR
	                        || argTypes[i].getSort() == Type.BOOLEAN) {
	                    super.visitVarInsn(ISTORE, vars[i]);
	                } else {
	                    //this would not happen
	                }
	            }
//	            int protocol = lvs.getTmpLV();
//	            super.visitVarInsn(ASTORE, protocol);
//	            super.visitVarInsn(ALOAD, protocol);
	            for(int i = 0; i < argTypes.length; i++) {
	                if(argTypes[i].getSort() == Type.OBJECT || argTypes[i].getSort() == Type.ARRAY) {
	                    super.visitVarInsn(ALOAD, vars[i]);
	                } else if(argTypes[i].getSort() == Type.DOUBLE) {
	                    super.visitVarInsn(Opcodes.DLOAD, vars[i]);
	                } else if(argTypes[i].getSort() == Type.LONG) {
	                    super.visitVarInsn(Opcodes.LLOAD, vars[i]);
	                } else if(argTypes[i].getSort() == Type.FLOAT) {
	                    super.visitVarInsn(Opcodes.FLOAD, vars[i]);
	                } else if(argTypes[i].getSort() == Type.INT || argTypes[i].getSort() == Type.SHORT
	                        || argTypes[i].getSort() == Type.BYTE || argTypes[i].getSort() == Type.CHAR
	                        || argTypes[i].getSort() == Type.BOOLEAN) {
	                    super.visitVarInsn(ILOAD, vars[i]);
	                } else {
	                    //this would not happen
	                }
	            }
	            /*
//	            super.visitLdcInsn(owner+"."+name);
//	            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/yarn/YarnInstrument",
//	                    "recordString", "(Ljava/lang/String;)V", false);
                recordOrTriggerRpcRequest(protocol, requestType);
                for(int i = 0; i < vars.length; i++) {
	                lvs.freeTmpLV(vars[i]);
	            }
	            lvs.freeTmpLV(protocol);
	            */
            }

            if(this.owner.startsWith("org/apache/hadoop")
                    && !this.owner.equals("org/apache/hadoop/ipc/RPC")
                    && this.name.equals("<init>") //TODO: several protocols do not get proxy in <init>
                    && owner.equals("org/apache/hadoop/ipc/RPC")
                    && (name.startsWith("getProxy") || name.startsWith("getProtocolProxy")
                            || name.startsWith("waitForProtocolProxy") || name.startsWith("waitForProxy"))
                    && opcode == Opcodes.INVOKESTATIC) {
//                 checkAndStoreRpcSocket(desc);
            }
        }

        super.visitMethodInsn(opcode, owner, name, desc, isInterface);
    }
}
