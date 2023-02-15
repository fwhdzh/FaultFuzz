package edu.iscas.tcse.favtrigger.instrumenter.mapred;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.struct.LazyArrayObjTags;
import edu.iscas.tcse.favtrigger.taint.FAVTaintType;
import edu.iscas.tcse.favtrigger.taint.Source.FAVTagType;

import org.objectweb.asm.*;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.*;

import java.util.ArrayList;
import java.util.List;

public class MRAddRPCMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;

    public MRAddRPCMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
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

    private int remoteIpVar = -1;
    public void visitCode() {
//        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/mapred/MRTestMV", "test", "()V", false);
    	if(name.endsWith("$$FAV") && Configuration.USE_FAV && Configuration.FOR_MR) {
    		Type[] argTypes = Type.getArgumentTypes(desc);
    		taintServerSideArgs(argTypes, this.owner, this.name, this.desc, FAVTaintType.RPC.toString(), FAVTagType.APP.toString());
		}
        super.visitCode();
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

    public void taintServerSideArgs(Type[] argTypes, String cname, String mname,
            String desc, String type, String tag) {
    	int varIdx = 1;
    	int[] locals = new int[argTypes.length];
        int[] localLoadCode = new int[argTypes.length];
        int[] localStoreCode = new int[argTypes.length];
        for(int i = 0; i < argTypes.length; i++) {
        	locals[i] = varIdx;
            if(argTypes[i].getSort() == Type.OBJECT || argTypes[i].getSort() == Type.ARRAY) {
            	localLoadCode[i] = Opcodes.ALOAD;
            	localStoreCode[i] = Opcodes.ASTORE;
//            	methodVisitor.visitVarInsn(Opcodes.ALOAD, varIdx);
            	varIdx++;
            } else if(argTypes[i].getSort() == Type.DOUBLE) {
//            	methodVisitor.visitVarInsn(Opcodes.DLOAD, varIdx);
            	localLoadCode[i] = Opcodes.DLOAD;
            	localStoreCode[i] = Opcodes.DSTORE;
                varIdx += 2;
            } else if(argTypes[i].getSort() == Type.LONG) {
//            	methodVisitor.visitVarInsn(Opcodes.LLOAD, varIdx);
            	localLoadCode[i] = Opcodes.LLOAD;
            	localStoreCode[i] = Opcodes.LSTORE;
                varIdx += 2;
            } else if(argTypes[i].getSort() == Type.FLOAT) {
//            	methodVisitor.visitVarInsn(Opcodes.FLOAD, varIdx);
            	localLoadCode[i] = Opcodes.FLOAT;
            	localStoreCode[i] = Opcodes.FSTORE;
                varIdx += 2;
            } else if(argTypes[i].getSort() == Type.INT || argTypes[i].getSort() == Type.SHORT
                    || argTypes[i].getSort() == Type.BYTE || argTypes[i].getSort() == Type.CHAR
                    || argTypes[i].getSort() == Type.BOOLEAN) {
//            	methodVisitor.visitVarInsn(Opcodes.ILOAD, varIdx);
            	localLoadCode[i] = Opcodes.ILOAD;
            	localStoreCode[i] = Opcodes.ISTORE;
            	varIdx++;
            } else {
                //this would not happen
            }
        }
		for(int i = 1; i < argTypes.length;) {//skip this taint
			if(!argTypes[i].getDescriptor().equals((Configuration.TAINT_TAG_DESC))) {
				if(LazyArrayObjTags.class.isAssignableFrom(argTypes[i].getClass())) {
					super.visitVarInsn(localLoadCode[i], locals[i]);
					super.visitLdcInsn(cname);
					super.visitLdcInsn(mname);
					super.visitLdcInsn(desc);
					super.visitLdcInsn(type);
					super.visitLdcInsn(tag);
					super.visitVarInsn(localLoadCode[locals.length-1], locals[locals.length-1]);
					FAV_APP_TAINT_ARRAY_FULLY.delegateVisit(mv);
					i += 2;
				} else {
					i++;
				}
			} else {
				super.visitLdcInsn(cname);
				super.visitLdcInsn(mname);
				super.visitLdcInsn(desc);
				super.visitLdcInsn(type);
				super.visitLdcInsn(tag);
				super.visitVarInsn(localLoadCode[locals.length-1], locals[locals.length-1]);
				FAV_APP_NEW_TAINT.delegateVisit(mv);
				super.visitVarInsn(localStoreCode[i], locals[i]);
				i++;
			}
        }
	}

    public void visitInsn(int opcode) {
        //use lvs would introduce errors, avoid to use lvs
        if(opcode == ARETURN && name.endsWith("$$FAV")) {
        	Type[] argTypes = Type.getArgumentTypes(desc);
        	int varIdx = 1;
            for(int i = 0; i < argTypes.length; i++) {
                if(argTypes[i].getSort() == Type.OBJECT || argTypes[i].getSort() == Type.ARRAY) {
                	varIdx++;
                } else if(argTypes[i].getSort() == Type.DOUBLE) {
                    varIdx += 2;
                } else if(argTypes[i].getSort() == Type.LONG) {
                    varIdx += 2;
                } else if(argTypes[i].getSort() == Type.FLOAT) {
                    varIdx += 2;
                } else if(argTypes[i].getSort() == Type.INT || argTypes[i].getSort() == Type.SHORT
                        || argTypes[i].getSort() == Type.BYTE || argTypes[i].getSort() == Type.CHAR
                        || argTypes[i].getSort() == Type.BOOLEAN) {
                	varIdx++;
                } else {
                    //this would not happen
                }
            }
        	if(Configuration.USE_FAV && Configuration.FOR_MR) {
        		recordOrTriggerServerSideResponse(varIdx-1);
        	}
        }
        super.visitInsn(opcode);
    }

    public void recordOrTriggerServerSideResponse(int msgIdVar) {
		//[rtn]
    	super.visitInsn(DUP);
    	FAV_GET_TIMESTAMP.delegateVisit(mv);
    	super.visitInsn(DUP2_X1);
    	super.visitInsn(POP2);
    	//[rtn, time1, time2, rtn]
		FAV_GET_RECORD_OUT.delegateVisit(mv);
		//[rtn, time1, time2, rtn, out]

//        Label nullOutStream = new Label();
//        super.visitInsn(DUP);
//        super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

        super.visitInsn(DUP);
        super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
        super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

//        super.visitLabel(nullOutStream);
//        super.visitFrame(Opcodes.F_APPEND, 2, new Object[]{Opcodes.LONG, "java/io/FileOutputStream"}, 0, null);
        //[rtn, time1, time2, rtn, out]
        super.visitInsn(SWAP);
        //[rtn, time1, time2, out, rtn]

        super.visitVarInsn(Opcodes.ALOAD, msgIdVar);
        super.visitInsn(SWAP);
        //[rtn, time1, time2, out, msgid, rtn]
        //favtrigger: TODO: consider favPHOSPHOR_TAG
        FAV_APP_RECORD_OR_TRIGGER_TAINTEDOBJ.delegateVisit(mv);
        //[rtn]
	}
}
