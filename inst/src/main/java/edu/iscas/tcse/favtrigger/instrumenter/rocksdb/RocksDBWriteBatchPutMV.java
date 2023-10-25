package edu.iscas.tcse.favtrigger.instrumenter.rocksdb;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class RocksDBWriteBatchPutMV extends MethodVisitor implements Opcodes {

    public static String combinedTaintFiledName = "ATTACHED_TAINT";

    public RocksDBWriteBatchPutMV(MethodVisitor mv) {
        super(Configuration.ASM_VERSION, mv);
    }

    @Override
    public void visitCode() {
        // org/rocksdb/WriteBatch put method
        // public void put(ColumnFamilyHandle var1, byte[] var3, byte[] var4)
        // put(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/ColumnFamilyHandle;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;
        // Ledu/columbia/cs/psl/phosphor/runtime/Taint;)V
        super.visitLdcInsn("RocksDB writeBatch.put method");
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBCV", "debugPrint", "(Ljava/lang/String;)V", false);

        super.visitVarInsn(ALOAD, 0);
        super.visitTypeInsn(CHECKCAST, "org/rocksdb/WriteBatch");
        super.visitInsn(DUP);
        super.visitFieldInsn(GETFIELD, "org/rocksdb/WriteBatch", combinedTaintFiledName, "Ledu/columbia/cs/psl/phosphor/runtime/Taint;");
        super.visitVarInsn(ALOAD, 6);
        super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/rocksdb/RocksDBWriteBatchPutMV", "combineBatchTaint", "(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;)Ledu/columbia/cs/psl/phosphor/runtime/Taint;", false);
        super.visitFieldInsn(PUTFIELD, "org/rocksdb/WriteBatch", combinedTaintFiledName, "Ledu/columbia/cs/psl/phosphor/runtime/Taint;");
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    public static Taint combineBatchTaint(Taint taint, LazyByteArrayObjTags tags) {
        System.out.println("Put method: Combine taint");
        if (taint == null) {
            System.out.println("taint is null");
            taint = Taint.emptyTaint();
        }
        if (tags == null) {
            System.out.println("tags is null");
            return taint;
        }
        Taint[] taints = tags.taints;
        if (taints == null) {
            System.out.println("tags' taints is null");
            return taint;
        }
        System.out.println("taints and tag's taints are not null");
        return Taint.combineTags(taint, Taint.combineTaintArray(taints));
    }
}
