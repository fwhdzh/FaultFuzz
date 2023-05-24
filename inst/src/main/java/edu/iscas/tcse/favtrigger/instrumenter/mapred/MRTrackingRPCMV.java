package edu.iscas.tcse.favtrigger.instrumenter.mapred;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;

import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MRTrackingRPCMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;

    public MRTrackingRPCMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
            String[] exceptions, NeverNullArgAnalyzerAdapter analyzer,
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

    public static void test() {
    	StackTraceElement[] callStack;
    	callStack = Thread.currentThread().getStackTrace();
    	List<String> callStackString = new ArrayList<String>();
    	for(int i = 0; i < callStack.length; ++i) {
    		callStackString.add(callStack[i].toString());
    	}
    	System.out.println("!!!!!!!!!!!!!!!!!!!!GY Test"+callStackString);
    }

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
		// TODO Auto-generated method stub
		if((Configuration.USE_FAV && Configuration.FOR_MR || Configuration.MR_RPC)
				&& (MRProtocols.isMRRPCs(owner) && name.endsWith(TaintUtils.METHOD_SUFFIX))) {
			String newName = name + "$$FAV$$";
			Type[] argTypes = Type.getArgumentTypes(descriptor);
			Type oldReturnType = Type.getReturnType(descriptor);
        	LinkedList<Type> newArgTypes = new LinkedList<>();
        	newArgTypes.add(Type.getObjectType(owner));
        	for (Type t : argTypes) {
        		newArgTypes.add(t);
        	}
        	Type[] newArgs = new Type[newArgTypes.size()];
            newArgs = newArgTypes.toArray(newArgs);
        	String newDesc = Type.getMethodDescriptor(oldReturnType, newArgs);
			super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, newName, newDesc, true);

//			String newName = name + "$$FAV";
//			Type[] argTypes = Type.getArgumentTypes(descriptor);
//			Type oldReturnType = Type.getReturnType(descriptor);
//        	LinkedList<Type> newArgTypes = new LinkedList<>();
//        	for (Type t : argTypes) {
//        		newArgTypes.add(t);
//        	}
//        	newArgTypes.add(Type.getType("Ljava/lang/String;"));
//        	Type[] newArgs = new Type[newArgTypes.size()];
//            newArgs = newArgTypes.toArray(newArgs);
//        	String newDesc = Type.getMethodDescriptor(oldReturnType, newArgs);
//        	super.visitLdcInsn("taskrpc");
//			super.visitMethodInsn(opcode, owner, newName, newDesc, isInterface);

//			super.visitLdcInsn("!!!!!!!!!!testFAVTEST "+name+"$$FAV"+"$$");
//			super.visitMethodInsn(INVOKESTATIC, owner, name+"$$FAV"+"$$", "(Ljava/lang/String;)V", true);
//			super.visitMethodInsn(INVOKESTATIC, "org/apache/hadoop/mapred/TaskUmbilicalProtocol", "test$$FAVTEST", "()V", true);
//			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		} else {
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
		}
	}

}
