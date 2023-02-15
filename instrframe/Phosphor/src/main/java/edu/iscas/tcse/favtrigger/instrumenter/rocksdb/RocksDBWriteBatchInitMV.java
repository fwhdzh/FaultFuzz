package edu.iscas.tcse.favtrigger.instrumenter.rocksdb;

import edu.columbia.cs.psl.phosphor.Configuration;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RocksDBWriteBatchInitMV extends MethodVisitor implements Opcodes {
    static String combinedTaintFiledName = "ATTACHED_TAINT";

    public RocksDBWriteBatchInitMV(MethodVisitor mv) {
        super(Configuration.ASM_VERSION, mv);
    }

    @Override
    public void visitCode() {
        super.visitLdcInsn("RocksDB writeBatch.init method");
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "debugPrint", "(Ljava/lang/String;)V", false);

        super.visitVarInsn(ALOAD, 0);
        super.visitMethodInsn(INVOKESTATIC, "Ledu/columbia/cs/psl/phosphor/runtime/Taint;", "emptyTaint", "()Ledu/columbia/cs/psl/phosphor/runtime/Taint;", false);
        super.visitFieldInsn(PUTFIELD, "org/rocksdb/WriteBatch", combinedTaintFiledName, "Ledu/columbia/cs/psl/phosphor/runtime/Taint;");
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }
}
