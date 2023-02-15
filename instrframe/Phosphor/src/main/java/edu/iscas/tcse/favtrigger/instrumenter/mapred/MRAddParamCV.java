package edu.iscas.tcse.favtrigger.instrumenter.mapred;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.Instrumenter;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.LocalVariableManager;
import edu.columbia.cs.psl.phosphor.instrumenter.PrimitiveArrayAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.SpecialOpcodeRemovingMV;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.struct.LazyArrayObjTags;
import edu.iscas.tcse.favtrigger.taint.FAVTaintType;
import edu.iscas.tcse.favtrigger.taint.Source.FAVTagType;

public class MRAddParamCV  extends ClassVisitor {
    private String className;
    private String superName;
    private String[] interfaces;
    private List<MethodNode> methodsToAddMsgIdParam = new LinkedList<>();
    private boolean generateExtraLVDebug;
    private boolean ignoreFrames;
    private Map<MethodNode, MethodNode> forMore = new HashMap<>();
    private boolean fixLdcClass;
    private boolean isInterface = false;

    public MRAddParamCV(ClassVisitor cv, boolean skipFrames) {
        super(Configuration.ASM_VERSION, cv);
        this.ignoreFrames = skipFrames;
    }

    @Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// TODO Auto-generated method stub
    	this.className = name;
    	this.superName = superName;
    	this.interfaces = interfaces;
    	this.generateExtraLVDebug = name.equals("java/lang/invoke/MethodType");
    	this.fixLdcClass = (version & 0xFFFF) < Opcodes.V1_5;
    	if((access & Opcodes.ACC_INTERFACE) != 0) {
            isInterface = true;
        }
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        if((Configuration.USE_FAV && Configuration.FOR_MR || Configuration.MR_RPC) && MRProtocols.isMRRPCs(className) && name.endsWith(TaintUtils.METHOD_SUFFIX) && !isStatic && isPublic) {
        	MethodVisitor rawMV = super.visitMethod(access, name, desc, signature, exceptions);
        	MethodNode rawMethod = new MethodNode(Configuration.ASM_VERSION, access, name, desc, signature, exceptions);
            methodsToAddMsgIdParam.add(rawMethod);
        	return rawMV;
        } else if ((Configuration.USE_FAV && Configuration.FOR_MR || Configuration.MR_RPC) && (MRProtocols.isMRRPCs(superName) || MRProtocols.isMRRPCs(className, interfaces))
        		&& name.endsWith(TaintUtils.METHOD_SUFFIX) && !isStatic && isPublic) {
        	MethodVisitor rawMV = super.visitMethod(access, name, desc, signature, exceptions);
        	MethodNode rawMethod = new MethodNode(Configuration.ASM_VERSION, access, name, desc, signature, exceptions);
            methodsToAddMsgIdParam.add(rawMethod);
        	return rawMV;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

	@Override
	public void visitEnd() {
//		if(this.className.equals("org/apache/hadoop/mapred/TaskUmbilicalProtocol")) {
//			int access = Opcodes.ACC_STATIC;
//			access = access | Opcodes.ACC_PUBLIC;
//			MethodVisitor methodVisitor = super.visitMethod(access, "test$$FAVTEST", "()V", null, null);
//            methodVisitor.visitCode();
//            Label label0 = new Label();
//            methodVisitor.visitLabel(label0);
//            methodVisitor.visitLineNumber(40, label0);
//            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//            methodVisitor.visitLdcInsn("!!!!!!!!!!test$$FAVTEST");
//            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//            Label label1 = new Label();
//            methodVisitor.visitLabel(label1);
//            methodVisitor.visitLineNumber(41, label1);
//            methodVisitor.visitInsn(Opcodes.RETURN);
//            methodVisitor.visitMaxs(2, 0);
//            methodVisitor.visitEnd();
//		}
		for(MethodNode m:methodsToAddMsgIdParam) {
			String newName = m.name + "$$FAV";
        	Type oldReturnType = Type.getReturnType(m.desc);
        	Type[] argTypes = Type.getArgumentTypes(m.desc);
        	LinkedList<Type> newArgTypes = new LinkedList<>();
        	for (Type t : argTypes) {
        		newArgTypes.add(t);
        	}
        	newArgTypes.add(Type.getType("Ljava/lang/String;"));
        	Type[] newArgs = new Type[newArgTypes.size()];
            newArgs = newArgTypes.toArray(newArgs);
        	String newDesc = Type.getMethodDescriptor(oldReturnType, newArgs);
        	String[] exceptions = new String[m.exceptions.size()];
        	exceptions = m.exceptions.toArray(exceptions);
			if(isInterface) {
				MethodVisitor mv = super.visitMethod(m.access, newName, newDesc, null, exceptions);
				mv.visitEnd();

				String staticNewName = newName +"$$";
				LinkedList<Type> staticNewArgTypes = new LinkedList<>();
				staticNewArgTypes.add(Type.getObjectType(this.className));
				for (Type t : argTypes) {
					staticNewArgTypes.add(t);
	        	}
				Type[] staticNewArgs = new Type[staticNewArgTypes.size()];
				staticNewArgs = staticNewArgTypes.toArray(staticNewArgs);
				String staticNewDesc = Type.getMethodDescriptor(oldReturnType, staticNewArgs);
//				int staticAccess = m.access | Opcodes.ACC_STATIC;
				int staticAccess = Opcodes.ACC_STATIC;
				staticAccess = staticAccess | Opcodes.ACC_PUBLIC;

//                MethodVisitor testmv = super.visitMethod(staticAccess, staticNewName, "(Ljava/lang/String;)V", null, exceptions);
//				testmv.visitCode();
//	            Label label0 = new Label();
//	            testmv.visitLabel(label0);
//	            testmv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//	            testmv.visitVarInsn(Opcodes.ALOAD, 0);
////	            methodVisitor.visitLdcInsn("!!!!!!!!!!testFAVTEST "+staticNewName);
//	            testmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//	            Label label1 = new Label();
//	            testmv.visitLabel(label1);
//	            testmv.visitInsn(Opcodes.RETURN);
//	            testmv.visitMaxs(2, 1);
//	            testmv.visitEnd();

//				//NOTE: The following code can only be used in jdk 8
//				String staticNewName = newName +"$$";
//				LinkedList<Type> staticNewArgTypes = new LinkedList<>();
//				Type tt = Type.getObjectType(this.className);
//				System.out.println("!!!!!!!!!!!!!!!GY ADD "+tt.getDescriptor());
//				staticNewArgTypes.add(tt);
//				for (Type t : argTypes) {
//					staticNewArgTypes.add(t);
//	        	}
//				Type[] staticNewArgs = new Type[staticNewArgTypes.size()];
//				staticNewArgs = staticNewArgTypes.toArray(staticNewArgs);
//				String staticNewDesc = Type.getMethodDescriptor(oldReturnType, staticNewArgs);
////				int staticAccess = m.access | Opcodes.ACC_STATIC;
//				int staticAccess = Opcodes.ACC_STATIC;
//				staticAccess = staticAccess | Opcodes.ACC_PUBLIC;

//				MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, staticNewName, staticNewDesc, null, null);
				MethodVisitor methodVisitor = super.visitMethod(staticAccess, staticNewName, staticNewDesc, null, exceptions);
	            methodVisitor.visitCode();
	            Label start = new Label();
	            methodVisitor.visitLabel(start);
	            int varIdx = 0;
	            int[] locals = new int[staticNewArgs.length];
	            int[] localLoadCode = new int[staticNewArgs.length];
	            for(int i = 0; i < staticNewArgs.length; i++) {
                	locals[i] = varIdx;
	                if(staticNewArgs[i].getSort() == Type.OBJECT || staticNewArgs[i].getSort() == Type.ARRAY) {
	                	localLoadCode[i] = Opcodes.ALOAD;
//	                	methodVisitor.visitVarInsn(Opcodes.ALOAD, varIdx);
	                	varIdx++;
	                } else if(staticNewArgs[i].getSort() == Type.DOUBLE) {
//	                	methodVisitor.visitVarInsn(Opcodes.DLOAD, varIdx);
	                	localLoadCode[i] = Opcodes.DLOAD;
		                varIdx += 2;
	                } else if(staticNewArgs[i].getSort() == Type.LONG) {
//	                	methodVisitor.visitVarInsn(Opcodes.LLOAD, varIdx);
	                	localLoadCode[i] = Opcodes.LLOAD;
		                varIdx += 2;
	                } else if(staticNewArgs[i].getSort() == Type.FLOAT) {
//	                	methodVisitor.visitVarInsn(Opcodes.FLOAD, varIdx);
	                	localLoadCode[i] = Opcodes.FLOAT;
		                varIdx += 2;
	                } else if(staticNewArgs[i].getSort() == Type.INT || staticNewArgs[i].getSort() == Type.SHORT
	                        || staticNewArgs[i].getSort() == Type.BYTE || staticNewArgs[i].getSort() == Type.CHAR
	                        || staticNewArgs[i].getSort() == Type.BOOLEAN) {
//	                	methodVisitor.visitVarInsn(Opcodes.ILOAD, varIdx);
	                	localLoadCode[i] = Opcodes.ILOAD;
	                	varIdx++;
	                } else {
	                    //this would not happen
	                }
	            }
	            FAV_NEW_MSGID.delegateVisit(methodVisitor);
	            methodVisitor.visitVarInsn(Opcodes.ISTORE, varIdx);
	            int msgid = varIdx;
	            varIdx++;

	            if(Configuration.USE_FAV && Configuration.FOR_MR) {
	            	NEW_EMPTY_TAINT.delegateVisit(methodVisitor);
	            	methodVisitor.visitVarInsn(Opcodes.ASTORE, varIdx);
	            	int taint = varIdx;
	            	varIdx++;
	            	methodVisitor.visitVarInsn(Opcodes.ALOAD, taint);
	            	for(int i = 2; i< staticNewArgs.length; i++) {//skip protocol and its taint
	            		if(staticNewArgs[i].getDescriptor().equals((Configuration.TAINT_TAG_DESC))) {
	            			methodVisitor.visitVarInsn(Opcodes.ALOAD, locals[i]);
		    				COMBINE_TAGS.delegateVisit(methodVisitor);
		    			} else if (LazyArrayObjTags.class.isAssignableFrom(staticNewArgs[i].getClass())) {
		    				methodVisitor.visitVarInsn(Opcodes.ALOAD, locals[i]);
		    				methodVisitor.visitFieldInsn(Opcodes.GETFIELD, argTypes[i].getInternalName(), "taints", "["+Configuration.TAINT_TAG_DESC);
		    				COMBINE_TAGS_ARRAY.delegateVisit(methodVisitor);
		    				COMBINE_TAGS.delegateVisit(methodVisitor);
		    			} else if (staticNewArgs[i].getSort() == Type.OBJECT && !Instrumenter.isIgnoredClass(staticNewArgs[i].getInternalName())){
		    				methodVisitor.visitVarInsn(Opcodes.ALOAD, locals[i]);
		    				methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, staticNewArgs[i].getInternalName(), "fav" + TaintUtils.TAINT_FIELD, "()Ljava/lang/Object;", false);
		    				methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, Configuration.TAINT_TAG_INTERNAL_NAME);
		    				COMBINE_TAGS.delegateVisit(methodVisitor);
						}
	            	}
	            	methodVisitor.visitVarInsn(Opcodes.ASTORE, taint);

	            	FAV_GET_TIMESTAMP.delegateVisit(methodVisitor);
	        		FAV_GET_RECORD_OUT.delegateVisit(methodVisitor);
	        		methodVisitor.visitInsn(Opcodes.DUP);
	        		methodVisitor.visitInsn(Opcodes.ICONST_0);  //set FAV_RECORD_TAG to false, avoid dead loop
	        		methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");
	        		methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);//aload protocol
	        		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "org/apache/hadoop/ipc/RPC", "getServerAddress", "(Ljava/lang/Object;)Ljava/net/InetSocketAddress;", false);
	        		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
	        		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
	        		methodVisitor.visitVarInsn(Opcodes.ILOAD, msgid);
	        		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/mapred/MRInstrument",
		                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
	        		methodVisitor.visitVarInsn(Opcodes.ALOAD, taint);
//	        		NEW_EMPTY_TAINT.delegateVisit(methodVisitor);
	                FAV_APP_RECORD_OR_TRIGGER_TAINT.delegateVisit(methodVisitor);
	            }

	            for(int i = 0; i< locals.length; i++) {
	            	methodVisitor.visitVarInsn(localLoadCode[i], locals[i]);
	            }
//	            methodVisitor.visitInsn(Opcodes.ACONST_NULL); //add null msgid string
	            FAV_CURRENT_IP.delegateVisit(methodVisitor);
	            methodVisitor.visitVarInsn(Opcodes.ILOAD, msgid);
	            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/mapred/MRInstrument",
	                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
	            methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, staticNewArgs[0].getInternalName(), newName, newDesc, true);

	            if(oldReturnType.equals(Type.VOID_TYPE)) {
	            	methodVisitor.visitInsn(Opcodes.RETURN);
	            } else {
	            	if(Configuration.USE_FAV && Configuration.FOR_MR) {
	            		methodVisitor.visitLdcInsn(className);
	            		methodVisitor.visitLdcInsn(m.name);
	            		methodVisitor.visitLdcInsn(m.desc);
	            		methodVisitor.visitLdcInsn(FAVTaintType.RPC.toString());
	            		methodVisitor.visitLdcInsn(FAVTagType.APP.toString());
	            		methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);//aload protocol
		        		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "org/apache/hadoop/ipc/RPC", "getServerAddress", "(Ljava/lang/Object;)Ljava/net/InetSocketAddress;", false);
		        		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
		        		methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
		        		methodVisitor.visitVarInsn(Opcodes.ILOAD, msgid);
		        		methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/mapred/MRInstrument",
			                    "combineIpWithMsgid", "(Ljava/lang/String;I)Ljava/lang/String;", false);
		        		FAV_APP_TAINT_PRIMITIVE.delegateVisit(methodVisitor);
		        		methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, oldReturnType.getInternalName());
	            	}
	            	methodVisitor.visitInsn(Opcodes.ARETURN);
	            }

	            Label end = new Label();
	            methodVisitor.visitLabel(end);
	            if(Configuration.USE_FAV && Configuration.FOR_MR) {
	            	int maxstack = (staticNewArgs.length+1)> 7? (staticNewArgs.length+1):7;
	            	methodVisitor.visitMaxs(maxstack, staticNewArgs.length+2);
//	            	methodVisitor.visitMaxs(staticNewArgs.length+1, staticNewArgs.length);
	            } else {
	            	methodVisitor.visitMaxs(staticNewArgs.length+1, staticNewArgs.length+1);
	            }
	            methodVisitor.visitEnd();
			} else {
				MethodVisitor mv;
	            mv = super.visitMethod(m.access, newName, newDesc, null, exceptions);
	            NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, m.access, newName, newDesc, mv);
	            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, m.access, className, newDesc, fixLdcClass);
	            LocalVariableManager lvs = new LocalVariableManager(m.access, newDesc, soc, an, mv, generateExtraLVDebug);
	            LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
	            Type returnType = Type.getReturnType(newDesc);
	            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
	            GeneratorAdapter ga = new GeneratorAdapter(lvs, m.access, newName, newDesc);
	            LabelNode start = new LabelNode(new Label());
	            LabelNode end = new LabelNode(new Label());
	            ga.visitCode();
	            ga.visitLabel(start.getLabel());

	            int varIdx = 0;
	            if((m.access & Opcodes.ACC_STATIC) == 0) {
	                ga.visitVarInsn(Opcodes.ALOAD, 0);
	                varIdx = 1;
	                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
	            }

	            ga.visitVarInsn(Opcodes.ALOAD, 0);
	            for(int i = 0; i < argTypes.length; i++) {
	                if(argTypes[i].getSort() == Type.OBJECT || argTypes[i].getSort() == Type.ARRAY) {
	                	ga.visitVarInsn(Opcodes.ALOAD, varIdx);
		                varIdx++;
	                } else if(argTypes[i].getSort() == Type.DOUBLE) {
	                	ga.visitVarInsn(Opcodes.DLOAD, varIdx);
		                varIdx += 2;
	                } else if(argTypes[i].getSort() == Type.LONG) {
	                	ga.visitVarInsn(Opcodes.LLOAD, varIdx);
		                varIdx += 2;
	                } else if(argTypes[i].getSort() == Type.FLOAT) {
	                	ga.visitVarInsn(Opcodes.FLOAD, varIdx);
		                varIdx += 2;
	                } else if(argTypes[i].getSort() == Type.INT || argTypes[i].getSort() == Type.SHORT
	                        || argTypes[i].getSort() == Type.BYTE || argTypes[i].getSort() == Type.CHAR
	                        || argTypes[i].getSort() == Type.BOOLEAN) {
	                	ga.visitVarInsn(Opcodes.ILOAD, varIdx);
		                varIdx++;
	                } else {
	                    //this would not happen
	                }
	            }
	            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, m.name, m.desc, false);

	            ga.visitLabel(end.getLabel());
	            ga.returnValue();
	            for(LocalVariableNode n : lvsToVisit) {
	                n.accept(ga);
	            }
	            ga.visitMaxs(0, 0);
	            ga.visitEnd();
			}
		}
		super.visitEnd();
	}
}
