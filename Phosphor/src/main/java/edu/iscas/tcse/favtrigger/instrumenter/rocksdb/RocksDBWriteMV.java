package edu.iscas.tcse.favtrigger.instrumenter.rocksdb;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RocksDBWriteMV extends MethodVisitor implements Opcodes {
    public RocksDBWriteMV(MethodVisitor mv) {
        super(Configuration.ASM_VERSION, mv);
    }

    @Override
    public void visitCode() {
        // RocksDB write method
        // public void write(WriteOptions var1, WriteBatch var2)
        // write(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/WriteOptions;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/WriteBatch;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;)V

        super.visitLdcInsn("RocksDB write method");
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "debugPrint", "(Ljava/lang/String;)V", false);

        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/record/instrumentation/RecordTaint", "getTimestamp", "()J", false);
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/record/instrumentation/RecordTaint", "getRecordOutStream", "()Ljava/io/FileOutputStream;", false);
        super.visitInsn(DUP);
        super.visitInsn(ICONST_0);
        super.visitFieldInsn(PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");
        super.visitLdcInsn("/RocksDB/WriteBatch");
        super.visitVarInsn(ALOAD, 4);
        super.visitFieldInsn(GETFIELD, "org/rocksdb/WriteBatch", RocksDBWriteBatchPutMV.combinedTaintFiledName, "Ledu/columbia/cs/psl/phosphor/runtime/Taint;");
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "recordTaintHelper", "(JLjava/io/FileOutputStream;Ljava/lang/String;Ledu/columbia/cs/psl/phosphor/runtime/Taint;)V", false);

        /*
        //
        // data(Ledu/columbia/cs/psl/phosphor/runtime/Taint;
        // Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;)
        // Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;
        mv.visitLdcInsn("/RocksDB/WriteBatch");
        mv.visitVarInsn(ASTORE, 6);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(NEW, "edu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "edu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag", "<init>", "()V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/rocksdb/WriteBatch", "data$$PHOSPHORTAGGED", "(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;)Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;", false);
        mv.visitVarInsn(ASTORE, 7);
        mv.visitVarInsn(ALOAD, 7);
        mv.visitFieldInsn(GETFIELD, "edu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag", "val", "Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, "edu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags");
        mv.visitVarInsn(ASTORE, 8);
        mv.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/record/instrumentation/RecordTaint", "getRecordOutStream", "()Ljava/io/FileOutputStream;", false);
        mv.visitVarInsn(ASTORE, 9);
        mv.visitVarInsn(ALOAD, 9);
        Label label = new Label();
        mv.visitJumpInsn(IFNULL, label);
        mv.visitVarInsn(ALOAD, 9);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");
        mv.visitVarInsn(ALOAD, 8);
        mv.visitFieldInsn(GETFIELD, "edu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags", "val", "[B");
        mv.visitInsn(ARRAYLENGTH);
        mv.visitVarInsn(ISTORE, 10);
        mv.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/record/instrumentation/RecordTaint", "getTimestamp", "()J", false);
        mv.visitVarInsn(ALOAD, 9);
        mv.visitVarInsn(ALOAD, 6);
        mv.visitVarInsn(ALOAD, 8);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 10);
        mv.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "recordHelper", "(JLjava/io/FileOutputStream;Ljava/lang/String;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;II)V", false);
        mv.visitLabel(label);
        mv.visitFrame(Opcodes.F_FULL, 10, new Object[]{"org/rocksdb/RocksDB", "edu/columbia/cs/psl/phosphor/runtime/Taint", "org/rocksdb/WriteOptions", "edu/columbia/cs/psl/phosphor/runtime/Taint", "org/rocksdb/WriteBatch", "edu/columbia/cs/psl/phosphor/runtime/Taint", "java/lang/String", "edu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag", "edu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags", "java/io/FileOutputStream"}, 0, new Object[]{});
        super.visitCode();
         */
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }
}
