package edu.iscas.tcse.favtrigger.instrumenter.rocksdb;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyByteArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;

import edu.iscas.tcse.favtrigger.taint.FAVTaint;
import edu.iscas.tcse.favtrigger.tracing.RecordTaint;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;


public class RocksDBCV extends ClassVisitor implements Opcodes {

    private final String className;

    public RocksDBCV(ClassVisitor cv, String className) {
        super(Configuration.ASM_VERSION, cv);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        boolean isPublic = (access & ACC_PUBLIC) != 0;
        boolean isStatic = (access & ACC_STATIC) != 0;
        if (className.startsWith("org/rocksdb/RocksDB") && isPublic && !isStatic && name.startsWith("put$$")
                && desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/ColumnFamilyHandle;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/WriteOptions;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;)V")) {
            // org/rocksdb/RocksDB put method
            // public void put(ColumnFamilyHandle var1, WriteOptions var2, byte[] var3, byte[] var4)
            return new RocksDBPutMV(mv);
        }
        if (className.startsWith("org/rocksdb/AbstractWriteBatch") && name.startsWith("put$$")
                && desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/ColumnFamilyHandle;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;)V")) {
            // org/rocksdb/WriteBatch put method
            // public void put(ColumnFamilyHandle var1, byte[] var3, byte[] var4)
            return new RocksDBWriteBatchPutMV(mv);
        }
        if (className.startsWith("org/rocksdb/WriteBatch") && name.startsWith("<init>")) {
            // org/rocksdb/WriteBatch init method
            // public void WriteBatch()
            return new RocksDBWriteBatchInitMV(mv);
        }
        if (className.startsWith("org/rocksdb/RocksDB") && isPublic && !isStatic && name.startsWith("write$$")
                && desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/WriteOptions;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/WriteBatch;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;)V")) {
            // org/rocksdb/RocksDB write method
            // public void write(WriteOptions var1, WriteBatch var2)
            return new RocksDBWriteMV(mv);
        }
        if (className.startsWith("org/rocksdb/RocksDB") && isPublic && !isStatic && name.startsWith("get$$")
                && desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;Lorg/rocksdb/ColumnFamilyHandle;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/LazyByteArrayObjTags;" +
                "Ledu/columbia/cs/psl/phosphor/runtime/Taint;Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;)" +
                "Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;")) {
            // org/rocksdb/RocksDB get method
            // public byte[] get(ColumnFamilyHandle var1, byte[] var2)
            return new RocksDBGetMV(mv);
        }
        if (className.startsWith("org/rocksdb/RocksIterator") && isPublic && !isStatic && (name.startsWith("key$$") || name.startsWith("value$$"))
                && desc.startsWith("(Ledu/columbia/cs/psl/phosphor/runtime/Taint;" +
                "Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;)" +
                "Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;")) {
            // byte[] key()
            // byte[] value()
            return new RocksDBKeyValueMV(mv);
        }
        return mv;
    }

    public static boolean isApplicable(String className) {
        return "org/rocksdb/RocksIterator".equals(className) || "org/rocksdb/RocksDB".equals(className)
                || (className.startsWith("org/rocksdb/AbstractWriteBatch"));
        // return false;
    }

    public static void debugPrint(String info) {
        System.out.println("Debug Info: " + info);
    }

    public static TaintedReferenceWithObjTag helper(TaintedReferenceWithObjTag obj, String handlerName) {
        LazyByteArrayObjTags dataRef = (LazyByteArrayObjTags) obj.val;
        if (dataRef != null) {
            int byteLen = dataRef.val.length;
            FAVTaint.combineNewTaints(dataRef, 0, byteLen, byteLen, "","", "", "", "", handlerName);
        }
        /*
        LazyByteArrayObjTags val = (LazyByteArrayObjTags) obj.val;
        if (val != null) {
            Taint[] taints = val.taints;
            Taint taint = Taint.combineTaintArray(taints);
            if (taint == null) {
                System.out.println("Debug info, has taint: -- null");
            } else {
                System.out.println("Debug info, has taint: --" + taint.toString());
            }
        }
        */
        return obj;
    }

    public static void recordHelper(long timestamp, FileOutputStream out, String path, LazyByteArrayObjTags bytes, int off, int len) {
        try {
            /*
            Taint taint = Taint.combineTaintArray(bytes.taints);
            if (taint == null) {
                System.out.println("Debug info, has taint: -- null");
            } else {
                System.out.println("Debug info, has taint: -- " + taint.toString());
            }
            */
            RecordTaint.recordTaintsEntry(timestamp, out, path, bytes.val, bytes.taints, off, len, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void recordTaintHelper(long timestamp, FileOutputStream out, String path, Taint taint) {
        try {
            if (taint == null) {
                System.out.println("Debug info, has taint: -- null");
            } else {
                System.out.println("Debug info, has taint: -- " + taint.toString());
            }
            RecordTaint.recordTaintEntry(timestamp, out, path, (byte)0, taint, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
