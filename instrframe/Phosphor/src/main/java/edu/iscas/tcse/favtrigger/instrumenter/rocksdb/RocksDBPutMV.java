package edu.iscas.tcse.favtrigger.instrumenter.rocksdb;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RocksDBPutMV extends MethodVisitor implements Opcodes {
    public RocksDBPutMV(MethodVisitor mv) {
        super(Configuration.ASM_VERSION, mv);
    }

    @Override
    public void visitCode() {
        // org/rocksdb/RocksDB put method
        // public void put(ColumnFamilyHandle var1, WriteOptions var2, byte[] var3, byte[] var4)
        // put(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/ColumnFamilyHandle;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/WriteOptions;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;)V
        super.visitLdcInsn("RocksDB put method");
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
        super.visitVarInsn(ASTORE, 10);
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/record/instrumentation/RecordTaint", "getRecordOutStream", "()Ljava/io/FileOutputStream;", false);
        super.visitVarInsn(ASTORE, 11);
        super.visitVarInsn(ALOAD, 11);
        Label label = new Label();
        super.visitJumpInsn(IFNULL, label);
        super.visitVarInsn(ALOAD, 11);
        super.visitInsn(ICONST_0);
        super.visitFieldInsn(PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");
        super.visitVarInsn(ALOAD, 8);
        super.visitFieldInsn(GETFIELD, "edu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags", "val", "[B");
        super.visitInsn(ARRAYLENGTH);
        super.visitVarInsn(ISTORE, 12);
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/record/instrumentation/RecordTaint", "getTimestamp", "()J", false);
        super.visitVarInsn(ALOAD, 11);
        super.visitVarInsn(ALOAD, 10);
        super.visitVarInsn(ALOAD, 8);
        super.visitInsn(ICONST_0);
        super.visitVarInsn(ILOAD, 12);
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "recordHelper", "(JLjava/io/FileOutputStream;Ljava/lang/String;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;II)V", false);
        super.visitLabel(label);
        super.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/lang/String", "java/io/FileOutputStream"}, 0, null);
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }
}
