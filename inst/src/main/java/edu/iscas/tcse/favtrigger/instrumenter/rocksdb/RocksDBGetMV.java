package edu.iscas.tcse.favtrigger.instrumenter.rocksdb;

import edu.columbia.cs.psl.phosphor.Configuration;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RocksDBGetMV extends MethodVisitor implements Opcodes {
    public RocksDBGetMV(MethodVisitor mv) {
        super(Configuration.ASM_VERSION, mv);
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        // get(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/ColumnFamilyHandle;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;)
        // Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;

        if (opcode == ARETURN) {
            super.visitLdcInsn("RocksDB get method");
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "debugPrint", "(Ljava/lang/String;)V", false);

            super.visitTypeInsn(NEW, "java/lang/StringBuilder");
            super.visitInsn(DUP);
            super.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            super.visitLdcInsn("/RocksDB/ColumnFamily-");
            super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitTypeInsn(NEW, "java/lang/String");
            super.visitInsn(DUP);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(INVOKEVIRTUAL, "org/rocksdb/ColumnFamilyHandle", "getName", "()[B", false);
            super.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([B)V", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);

            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "helper",
                    "(Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;Ljava/lang/String;)Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;", false);
        }
        super.visitInsn(opcode);
    }
}
