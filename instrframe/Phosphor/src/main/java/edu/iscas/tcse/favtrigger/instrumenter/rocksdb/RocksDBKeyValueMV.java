package edu.iscas.tcse.favtrigger.instrumenter.rocksdb;

import edu.columbia.cs.psl.phosphor.Configuration;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RocksDBKeyValueMV extends MethodVisitor implements Opcodes {
    public RocksDBKeyValueMV(MethodVisitor mv) {
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
            super.visitLdcInsn("RocksDB key/value method");
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "debugPrint", "(Ljava/lang/String;)V", false);
            super.visitLdcInsn("/RocksDB/KeyValue");
            super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "helper", "(Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;Ljava/lang/String;)Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;", false);
        }
        super.visitInsn(opcode);
    }
}
