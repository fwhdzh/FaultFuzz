package edu.iscas.tcse.favtrigger.instrumenter;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_RECORD_OUT;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_TIMESTAMP;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_APP_RECORD_OR_TRIGGER_FULLY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FrameNode;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_APP_TAINT_BYTES_FULLY;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.iscas.tcse.favtrigger.taint.FAVTaintType;
import edu.iscas.tcse.favtrigger.taint.Source.FAVTagType;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

public class LevelDBAPIModelMV extends TaintAdapter implements Opcodes {
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;

    //take znode path as path, znode value as value, record value taint to path
    //specifically record delete operation with taint empty
    //take every return value of every public method as a read
    public LevelDBAPIModelMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
            String[] exceptions, String originalDesc, NeverNullArgAnalyzerAdapter analyzer) {
        super(access, owner, name, descriptor, signature, exceptions, mv, analyzer);
        this.desc = descriptor;
        this.returnType = Type.getReturnType(desc);
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        this.name = name;
        this.owner = owner;
    }

    private void recordPut(int keyVar, int valueVar) {
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
        super.visitLdcInsn(FAVPathType.LVJNIDB+":");
        super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
        super.visitVarInsn(ALOAD, 0);
        super.visitFieldInsn(GETFIELD, this.owner, TaintUtils.FAV_PATH, "Ljava/lang/String;");
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        super.visitLdcInsn("/");
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        super.visitVarInsn(ALOAD, keyVar);
        super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyByteArrayObjTags.class));
        super.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(LazyByteArrayObjTags.class), "val", "[B");
        super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/fusesource/leveldbjni/JniDBFactory", "asString", "([B)Ljava/lang/String;", false);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
//        super.visitLdcInsn("LevelDBPATH");
        super.visitVarInsn(ALOAD, valueVar);
        FAV_APP_RECORD_OR_TRIGGER_FULLY.delegateVisit(mv);
        lvs.freeTmpLV(fileOutStream);
    }

    @Override
    public void visitCode() {
        // TODO Auto-generated method stub
        super.visitCode();
        if(Configuration.USE_FAV) {
             if(this.isPublic && this.owner.equals("org/fusesource/leveldbjni/internal/NativeDB")
                     && this.name.startsWith("put")) {
 //                super.visitLdcInsn("enter "+" put "+this.desc);
 //                super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/LevelDBAPIModelMV",
 //                        "record", "(Ljava/lang/String;)V", false);
                 recordPut(4, 6);
             }
             if(this.isPublic && this.owner.equals("org/fusesource/leveldbjni/internal/NativeWriteBatch")
                     && this.name.startsWith("put")) {
                 recordPut(2, 4);
             }
        }
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, desc, isInterface);
    }
    public static void checkFile(File f) {
        StackTraceElement[] callStack;
        callStack = Thread.currentThread().getStackTrace();
        List<String> callStackString = new ArrayList<String>();
        for(int i = 3; i < callStack.length; ++i) {
            callStackString.add(callStack[i].toString());
        }
        try {
            f.getCanonicalPath();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("!!!!!GY level db write to file "+f.getAbsolutePath()+", "+callStackString);
    }
    public static void record(String f) {
        StackTraceElement[] callStack;
        callStack = Thread.currentThread().getStackTrace();
        List<String> callStackString = new ArrayList<String>();
        for(int i = 3; i < callStack.length; ++i) {
            callStackString.add(callStack[i].toString());
        }
        System.out.println("!!!!!GY level db record "+f+", "+callStackString);
    }

    public void visitInsn(int opcode) {
        //use lvs would introduce errors, avoid to use lvs
         if(Configuration.USE_FAV && opcode == ARETURN && this.isStatic && this.isPublic
                 && this.owner.equals("org/fusesource/leveldbjni/internal/NativeDB")
                 && this.name.startsWith("open$$PHOSPHORTAGGED")) {
             //[rtn]
             super.visitInsn(DUP);
             //[rtn, rtn]
             super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
             //[rtn, rtn.val]
             super.visitTypeInsn(Opcodes.CHECKCAST, "org/fusesource/leveldbjni/internal/NativeDB");
             super.visitVarInsn(ALOAD, 2);//aload path parameter
             super.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);
             //[rtn, rtn.val, pathString]
             super.visitFieldInsn(PUTFIELD,"org/fusesource/leveldbjni/internal/NativeDB", TaintUtils.FAV_PATH, "Ljava/lang/String;");

//             //[rtn]
//             super.visitInsn(DUP);
//             super.visitInsn(DUP);
//             //[rtn, rtn, rtn]
//             super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
//             //[rtn, rtn, rtn.val]
//             super.visitTypeInsn(Opcodes.CHECKCAST, "org/fusesource/leveldbjni/internal/NativeDB");
//             super.visitInsn(DUP);
//             //[rtn, rtn, rtn.val, rtn.val]
//             super.visitVarInsn(ALOAD, 2);//aload path parameter
//             //[rtn, rtn, rtn.val, rtn.val, pathFile]
//             super.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;", false);
//             //[rtn, rtn, rtn.val, rtn.val, path]
//             super.visitFieldInsn(PUTFIELD,"org/fusesource/leveldbjni/internal/NativeDB", TaintUtils.FAV_PATH, "Ljava/lang/String;");
//             //[rtn, rtn, newVar]
//             super.visitFieldInsn(PUTFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
//             //[rtn]
         } else if (Configuration.USE_FAV && opcode == ARETURN && this.isPublic
                 && this.owner.equals("org/fusesource/leveldbjni/internal/NativeDB")
                 && this.name.startsWith("get$$PHOSPHORTAGGED")) {
             //[rtn]
             super.visitInsn(DUP);
             super.visitInsn(DUP);
             //[rtn, rtn, rtn]
             super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
             super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyByteArrayObjTags.class));
             super.visitLdcInsn(this.owner);
             super.visitLdcInsn(this.name);
             super.visitLdcInsn(this.desc);
             super.visitLdcInsn(FAVTaintType.LVDBREAD.toString());
             super.visitLdcInsn(FAVTagType.APP.toString());

             //linksource
             super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
             super.visitInsn(Opcodes.DUP);
             super.visitLdcInsn(FAVPathType.LVJNIDB+":");
             super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
             super.visitVarInsn(ALOAD, 0);
             super.visitFieldInsn(GETFIELD, this.owner, TaintUtils.FAV_PATH, "Ljava/lang/String;");
             super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                     "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
             super.visitLdcInsn("/");
             super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                     "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
             super.visitVarInsn(ALOAD, 4);
             super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyByteArrayObjTags.class));
             super.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(LazyByteArrayObjTags.class), "val", "[B");
             super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/fusesource/leveldbjni/JniDBFactory", "asString", "([B)Ljava/lang/String;", false);
             super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
                     "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
             super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);

             FAV_APP_TAINT_BYTES_FULLY.delegateVisit(mv);
             super.visitFieldInsn(PUTFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
         } else if (Configuration.USE_FAV && opcode == ARETURN && this.isPublic
                 && this.owner.equals("org/fusesource/leveldbjni/internal/JniDB")
                 && this.name.startsWith("createWriteBatch")) {
             //[rtn]
             super.visitInsn(DUP);
             //[rtn, rtn]
             super.visitFieldInsn(GETFIELD, Type.getInternalName(TaintedReferenceWithObjTag.class), "val", "Ljava/lang/Object;");
             //[rtn, rtn.val]
             super.visitTypeInsn(Opcodes.CHECKCAST, "org/fusesource/leveldbjni/internal/JniWriteBatch");
             super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/fusesource/leveldbjni/internal/JniWriteBatch",
                     "writeBatch", "()Lorg/fusesource/leveldbjni/internal/NativeWriteBatch;", false);
             super.visitVarInsn(ALOAD, 0);
             super.visitFieldInsn(GETFIELD, "org/fusesource/leveldbjni/internal/JniDB", "db", "Lorg/fusesource/leveldbjni/internal/NativeDB;");
             super.visitFieldInsn(GETFIELD, "org/fusesource/leveldbjni/internal/NativeDB", TaintUtils.FAV_PATH, "Ljava/lang/String;");
             super.visitFieldInsn(PUTFIELD, "org/fusesource/leveldbjni/internal/NativeWriteBatch", TaintUtils.FAV_PATH, "Ljava/lang/String;");
         }
        super.visitInsn(opcode);
    }

    private void storeDBPath(String owner, int pathVar) {
        //current element on the top stack is the NativeDB object or NativeWriteBatch object
//        int rtn = lvs.getTmpLV();
//        super.visitVarInsn(ASTORE, rtn);
        super.visitVarInsn(ALOAD, pathVar);
    }
}
