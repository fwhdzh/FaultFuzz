package edu.iscas.tcse.favtrigger.instrumenter.annotation;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.APP_FAULT_BEFORE;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_RECORD_OUT;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_TIMESTAMP;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;

public class AnnotationBasedInjectionMethodVisitor extends TaintAdapter {

    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;


    public AnnotationBasedInjectionMethodVisitor(MethodVisitor mv, int access, String owner, String name,
            String descriptor, String signature,
            String[] exceptions, String originalDesc, NeverNullArgAnalyzerAdapter analyzer, String ownerSuperCname,
            String[] ownerInterfaces) {
        super(access, owner, name, descriptor, signature, exceptions, mv, analyzer);
        this.desc = descriptor;
        this.returnType = Type.getReturnType(desc);
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        this.name = name;
        this.owner = owner;
        this.ownerSuperCname = ownerSuperCname;
        this.ownerInterfaces = ownerInterfaces;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (InjectAnnotationIdentifier.match(owner, name, descriptor)) {
            /* 
             * prepare the paramaters of APP_FAULT_BEFORE
             */ 
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
            String path = owner + "." + name + descriptor;
            mv.visitLdcInsn(path);
            
            APP_FAULT_BEFORE.delegateVisit(mv);

            lvs.freeTmpLV(fileOutStream);
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

}
