package edu.iscas.tcse.favtrigger.instrumenter.jdk;

import edu.columbia.cs.psl.phosphor.TaintUtils;
//import edu.columbia.cs.psl.phosphor.Instrumenter;
//import edu.columbia.cs.psl.phosphor.TaintUtils;
//import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.iscas.tcse.favtrigger.instrumenter.jdk.JRERunMode.JREType;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.FrameNode;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.*;


public class FileOperationMV extends TaintAdapter implements Opcodes {

	private final String desc;
    private final Type returnType;
    private final Type originalRtn;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;
    private boolean rpcRelated = false;

    public FileOperationMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
            String[] exceptions, String originalDesc, NeverNullArgAnalyzerAdapter analyzer,
            String superCname, String[] interfaces) {
        super(access, owner, name, descriptor, signature, exceptions, mv, analyzer);
        this.desc = descriptor;
        this.returnType = Type.getReturnType(desc);
        this.originalRtn = Type.getReturnType(originalDesc);
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

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
		// TODO Auto-generated method stub
		if(owner.equals("java/io/FileSystem") && (name.equals("createFileExclusively") || name.equals("delete"))) {
			 //boolean createFileExclusively(String pathname)
			 //public abstract boolean delete(File f);
		 	Type[] args = Type.getArgumentTypes(descriptor);
             int[] vars = new int[args.length];
             Type rtnType = Type.getReturnType(descriptor);
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
	         if (name.startsWith("createFileExclusively")) {//for create, the znode path is also a kind of important resource
	         	super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                 super.visitInsn(Opcodes.DUP);
                 super.visitLdcInsn(FAVPathType.CREALC.toString()+":");
                 super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
                 super.visitVarInsn(ALOAD, vars[0]);
                 super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                 super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString","()Ljava/lang/String;", false);
				 mv.visitLdcInsn(JREType.CREATE.toString());
	         } else {
	         	super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                 super.visitInsn(Opcodes.DUP);
                 super.visitLdcInsn(FAVPathType.DELLC.toString()+":");
                 super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
                 super.visitVarInsn(ALOAD, vars[0]);
                 super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);
                 super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                 super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString","()Ljava/lang/String;", false);
	             mv.visitLdcInsn(JREType.DELETE.toString());
			 }
	         JRE_RECORD_OR_TRIGGER_CREATEDEL.delegateVisit(mv);

	         lvs.freeTmpLV(fileOutStream);

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
		 }
		 if(this.owner.equals("java/io/FileOutputStream") && this.name.equals("<init>")
		 		&& owner.equals("java/io/FileOutputStream") && name.startsWith("open")) {
		 	Type[] args = Type.getArgumentTypes(descriptor);
		 	int[] vars = new int[args.length];
	           Type rtnType = Type.getReturnType(descriptor);
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
	         FrameNode fn0 = getCurrentFrameNode();
		 	super.visitVarInsn(ALOAD, vars[0]);
		 	FAV_IS_RECORD_FILE.delegateVisit(mv);
		 	Label done = new Label();
             super.visitJumpInsn(Opcodes.IFNE, done);

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
	         super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
             super.visitInsn(Opcodes.DUP);
             super.visitLdcInsn(FAVPathType.CREALC.toString()+":");
             super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
             super.visitVarInsn(ALOAD, vars[0]);
             super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
             super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString","()Ljava/lang/String;", false);
	         mv.visitLdcInsn(JREType.CREATE.toString());
	         JRE_RECORD_OR_TRIGGER_CREATEDEL.delegateVisit(mv);

	         lvs.freeTmpLV(fileOutStream);

             super.visitLabel(done);
             acceptFn(fn0);

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
		 }
		super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
	}
}