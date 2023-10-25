package edu.iscas.tcse.favtrigger.instrumenter;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_RECORD_OUT;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_TIMESTAMP;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.JRE_FAULT_BEFORE;

import java.net.DatagramPacket;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.NativeMethodInspector;
import edu.columbia.cs.psl.phosphor.PhosphorInspector;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.LocalVariableManager;
import edu.columbia.cs.psl.phosphor.instrumenter.PrimitiveArrayAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.SpecialOpcodeRemovingMV;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.runtime.TaintInstrumented;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.iscas.tcse.favtrigger.instrumenter.jdk.JRERunMode.JREType;
import edu.iscas.tcse.favtrigger.instrumenter.zk.ZKTrackingMV;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

public class FWHZKTrackingClassVisitor extends ClassVisitor {

    /**
     * Initialize on constructor
     */
    private static boolean DO_OPT = false;
    public static boolean IS_RUNTIME_INST = true;
    private boolean ignoreFrames;
    private List<FieldNode> fields;

    /**
     * Initialize on override visit
     */
    private String className;
    private String[] interfaces;
    private boolean addTaintField = false;
    private boolean isAbstractClass;
    private boolean isInterface;
    private boolean isLambda;
    private String superName;

    /**
     * It seems they never be initilized
     */
    private boolean generateExtraLVDebug;
    private boolean fixLdcClass;

    private Map<MethodNode, MethodNode> forMore = new HashMap<>();
    private List<MethodNode> methodsToAddWrappersFor = new LinkedList<>();

    static {
        if (!DO_OPT && !IS_RUNTIME_INST) {
            System.err.println("WARN: OPT DISABLED");
        }
    }

    public FWHZKTrackingClassVisitor(ClassVisitor cv, boolean skipFrames, List<FieldNode> fields) {
        super(Configuration.ASM_VERSION, cv);
        DO_OPT = DO_OPT && !IS_RUNTIME_INST;
        this.ignoreFrames = skipFrames;
        this.fields = fields;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.interfaces = interfaces; // favtrigger
        addTaintField = true;
        if ((access & Opcodes.ACC_ABSTRACT) != 0) {
            isAbstractClass = true;
        }
        if ((access & Opcodes.ACC_INTERFACE) != 0) {
            addTaintField = false;
            isInterface = true;
        }
        if ((access & Opcodes.ACC_ENUM) != 0) {
            // isEnum = true;
            addTaintField = false;
        }

        if ((access & Opcodes.ACC_ANNOTATION) != 0) {
            // isAnnotation = true;
        }

        // Debugging - no more package-protected
        if ((access & Opcodes.ACC_PRIVATE) == 0) {
            access = access | Opcodes.ACC_PUBLIC;
        }

        if (!superName.equals("java/lang/Object") && !PhosphorInspector.isIgnoredClass(superName)) {
            addTaintField = false;
            // addTaintMethod = true;
        }
        if (name.equals("java/awt/image/BufferedImage") || name.equals("java/awt/image/Image")) {
            addTaintField = false;
        }

        isLambda = name.contains("$$Lambda$");

        // isNormalClass = (access & Opcodes.ACC_ENUM) == 0 && (access &
        // Opcodes.ACC_INTERFACE) == 0;

        super.visit(version, access, name, signature, superName, interfaces);
        this.visitAnnotation(Type.getDescriptor(TaintInstrumented.class), false);
        this.className = name;
        this.superName = superName;

        // this.isUninstMethods =
        // Instrumenter.isIgnoredClassWithStubsButNoTracking(className);
    }

    public boolean isNativeIO(String className, String name, String desc) {
        return (NativeMethodInspector.isNativeMethodNeedsRecordSecondPara(className, name, desc)
                || NativeMethodInspector.isNativeMethodNeedsRecordThirdPara(className, name, desc)
                || NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToThirdPara(className, name, desc)
                || NativeMethodInspector.isNativeMethodNeedsRecordDatagramPacket(className, name, desc)
                || NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToDatagramPacket(className, name, desc));
        // || Instrumenter.isNativeMethodNeedsNewRtnTaint(className, name, desc)
        // || Instrumenter.isNativeMethodNeedsCombineNewTaintToSecondPara(className,
        // name, desc));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (PhosphorInspector.isIgnoredClass(this.className)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
        if (name.contains(TaintUtils.METHOD_SUFFIX)) {
            // Some dynamic stuff might result in there being weird stuff here
            return new MethodVisitor(Configuration.ASM_VERSION) {
            };
        }
        if ((access & Opcodes.ACC_NATIVE) == 0) {

            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            SpecialOpcodeRemovingMV specialOpcodeRemovingMV = new SpecialOpcodeRemovingMV(mv, ignoreFrames, access,
                    className, desc, fixLdcClass);
            mv = specialOpcodeRemovingMV;
            NeverNullArgAnalyzerAdapter analyzer = new NeverNullArgAnalyzerAdapter(className, access, name, desc, mv);

            ZKTrackingMV zkmv = new ZKTrackingMV(mv, access, className, name, desc, signature, exceptions, desc,
                    analyzer, this.superName, this.interfaces);
            zkmv.setFields(fields);

            // TestMV tmv = new TestMV(niomv, access, className, name, desc, signature,
            // exceptions, desc, analyzer, this.superName, this.interfaces);
            // tmv.setFields(fields);

            LocalVariableManager lvs = new LocalVariableManager(access, desc, zkmv, analyzer, mv, generateExtraLVDebug);
            lvs.disable();
            zkmv.setLocalVariableSorter(lvs);

            final MethodVisitor prev = lvs;
      		 MethodNode rawMethod = new MethodNode(Configuration.ASM_VERSION, access, name, desc, signature, exceptions) {
      			@Override
 	            protected LabelNode getLabelNode(Label l) {
 	                if(!Configuration.READ_AND_SAVE_BCI) {
 	                    return super.getLabelNode(l);
 	                }
 	                if(!(l.info instanceof LabelNode)) {
 	                    l.info = new LabelNode(l);
 	                }
 	                return (LabelNode) l.info;
 	            }

 	            @Override
 	            public void visitEnd() {
 	                super.visitEnd();
 	                this.accept(prev);
 	            }

 	            @Override
 	            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
 	                super.visitFrame(type, nLocal, local, nStack, stack);
 	            }
      		 };

             return rawMethod;
        } else {
            // this is a native method
            final MethodVisitor prev = super.visitMethod(access, name, desc, signature, exceptions);
            MethodNode rawMethod = new MethodNode(Configuration.ASM_VERSION, access, name, desc, signature,
                    exceptions) {
                @Override
                public void visitEnd() {
                    super.visitEnd();
                    this.accept(prev);
                }
            };
            if (isNativeIO(className, name, desc)) {
                // this is a native IO method. we want here to make a $wrapper method that will
                // call the original one.
                MethodNode wrapper = new MethodNode(access, name, desc, signature, exceptions);
                methodsToAddWrappersFor.add(wrapper);
                forMore.put(wrapper, rawMethod);
            }
            return rawMethod;
        }
    }

    @Override
    public void visitEnd() {
        if (this.className.equals("org/apache/jute/BinaryOutputArchive")) {
            MethodVisitor mv;
            int acc = Opcodes.ACC_PUBLIC;
            String favDesc = "()Ljava/net/InetAddress;";
            mv = super.visitMethod(acc, "getAddr$$FAV", favDesc, null, null);
            NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, "getAddr$$FAV", favDesc,
                    mv);
            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, acc, className, favDesc, fixLdcClass);
            LocalVariableManager lvs = new LocalVariableManager(acc, favDesc, soc, an, mv, generateExtraLVDebug);
            LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
            Type returnType = Type.getReturnType(favDesc);
            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
            GeneratorAdapter ga = new GeneratorAdapter(lvs, acc, "getAddr$$FAV", favDesc);
            LabelNode start = new LabelNode(new Label());
            LabelNode end = new LabelNode(new Label());
            ga.visitCode();
            ga.visitLabel(start.getLabel());

            if ((acc & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(Opcodes.ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
            }

            ga.visitVarInsn(Opcodes.ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "out", "Ljava/io/DataOutput;");
            ga.visitTypeInsn(Opcodes.CHECKCAST, "java/io/DataOutputStream");
            ga.visitFieldInsn(Opcodes.GETFIELD, "java/io/DataOutputStream", "favAddr", "Ljava/net/InetAddress;");

            ga.visitLabel(end.getLabel());
            ga.returnValue();
            for (LocalVariableNode n : lvsToVisit) {
                n.accept(ga);
            }
            ga.visitMaxs(0, 0);
            ga.visitEnd();
        }
        if (this.className.equals("org/apache/jute/BinaryInputArchive")) {
            MethodVisitor mv;
            int acc = Opcodes.ACC_PUBLIC;
            String favDesc = "()Ljava/net/InetAddress;";
            mv = super.visitMethod(acc, "getAddr$$FAV", favDesc, null, null);
            NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, "getAddr$$FAV", favDesc,
                    mv);
            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, acc, className, favDesc, fixLdcClass);
            LocalVariableManager lvs = new LocalVariableManager(acc, favDesc, soc, an, mv, generateExtraLVDebug);
            LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
            Type returnType = Type.getReturnType(favDesc);
            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
            GeneratorAdapter ga = new GeneratorAdapter(lvs, acc, "getAddr$$FAV", favDesc);
            LabelNode start = new LabelNode(new Label());
            LabelNode end = new LabelNode(new Label());
            ga.visitCode();
            ga.visitLabel(start.getLabel());

            if ((acc & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(Opcodes.ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
            }

            ga.visitVarInsn(Opcodes.ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "in", "Ljava/io/DataInput;");
            ga.visitTypeInsn(Opcodes.CHECKCAST, "java/io/DataInputStream");
            ga.visitFieldInsn(Opcodes.GETFIELD, "java/io/DataInputStream", "favAddr", "Ljava/net/InetAddress;");

            ga.visitLabel(end.getLabel());
            ga.returnValue();
            for (LocalVariableNode n : lvsToVisit) {
                n.accept(ga);
            }
            ga.visitMaxs(0, 0);
            ga.visitEnd();
        }

        for (MethodNode m : methodsToAddWrappersFor) {
            if ((m.access & Opcodes.ACC_NATIVE) != 0) {
                // Generate wrapper for native method - a native wrapper
                generateNativeIOWrapper(m, m.name);
            }
        }

    }

    private void generateNativeIOWrapper(MethodNode m, String methodNameToCall) {
        String[] exceptions = new String[m.exceptions.size()];
        exceptions = m.exceptions.toArray(exceptions);
        Type[] argTypes = Type.getArgumentTypes(m.desc);

        LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
        LabelNode start = new LabelNode(new Label());
        LabelNode end = new LabelNode(new Label());
        boolean isStatic = ((Opcodes.ACC_STATIC) & m.access) != 0;
        Type origReturn = Type.getReturnType(m.desc);
        MethodVisitor mv;
        int acc = m.access & ~Opcodes.ACC_NATIVE;
        boolean isInterfaceMethod = isInterface;
        if (isInterfaceMethod && forMore.get(m) != null && forMore.get(m).instructions.size() > 0) {
            isInterfaceMethod = false;
        }
        if (!isInterfaceMethod) {
            acc = acc & ~Opcodes.ACC_ABSTRACT;
        }
        if (m.name.equals("<init>")) {
            mv = super.visitMethod(acc, m.name, m.desc, m.signature, exceptions);
        } else {
            mv = super.visitMethod(acc, m.name + TaintUtils.METHOD_SUFFIX, m.desc, m.signature, exceptions);
        }
        NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, m.access, m.name, m.desc, mv);
        MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, m.access, className, m.desc, fixLdcClass);
        LocalVariableManager lvs = new LocalVariableManager(acc, m.desc, soc, an, mv, generateExtraLVDebug);
        lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(origReturn));
        GeneratorAdapter ga = new GeneratorAdapter(lvs, acc, m.name + TaintUtils.METHOD_SUFFIX, m.desc);
        if (isInterfaceMethod) {
            ga.visitEnd();
            return;
        }
        ga.visitCode();
        ga.visitLabel(start.getLabel());
        if (!isLambda) {
            String descToCall = m.desc;
            boolean isUntaggedCall = false;
            int idx = 0;
            if ((m.access & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(Opcodes.ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, idx));
                idx++; // this
            }
            for (Type t : argTypes) {
                ga.visitVarInsn(t.getOpcode(Opcodes.ILOAD), idx);

                idx += t.getSize();
            }
            int opcode;
            if ((m.access & Opcodes.ACC_STATIC) == 0) {
                opcode = Opcodes.INVOKESPECIAL;
            } else {
                opcode = Opcodes.INVOKESTATIC;
            }
            if (NativeMethodInspector.isNativeMethodNeedsRecordSecondPara(this.className, m.name, m.desc)) {
                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, this.className, TaintUtils.FAV_RECORD_TAG, "Z");
                Label done1 = new Label();
                ga.visitJumpInsn(Opcodes.IFEQ, done1);

                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, this.className, "path", "Ljava/lang/String;");
                int path = lvs.getTmpLV();
                ga.visitVarInsn(Opcodes.ASTORE, path);
                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, this.className, "path", "Ljava/lang/String;");
                Label updatePath = new Label();
                ga.visitJumpInsn(Opcodes.IFNONNULL, updatePath);

                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, this.className, "fd", "Ljava/io/FileDescriptor;");
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/FileDescriptor", TaintUtils.FAV_FILEDESCRIPTOR_MT,
                        "()Ljava/lang/String;", false);
                ga.visitVarInsn(Opcodes.ASTORE, path);

                // check if the path is "", then it may be a write to the console,
                // and we just skip recording this operation
                ga.visitLabel(updatePath);
                ga.visitVarInsn(Opcodes.ALOAD, path);
                ga.visitLdcInsn("");
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                ga.visitJumpInsn(Opcodes.IFNE, done1);

                FAV_GET_RECORD_OUT.delegateVisit(ga);
                int fileOutStream = lvs.getTmpLV();
                ga.visitVarInsn(Opcodes.ASTORE, fileOutStream);

                Label nullOutStream = new Label();
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitLdcInsn(0); // set FAV_RECORD_TAG to false, avoid dead loop
                ga.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

                ga.visitLabel(nullOutStream);

                FAV_GET_TIMESTAMP.delegateVisit(ga);
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitVarInsn(Opcodes.ALOAD, path);
                ga.visitLdcInsn(JREType.FILE.toString());
                JRE_FAULT_BEFORE.delegateVisit(ga);

                ga.visitLabel(done1);

                ga.visitMethodInsn(opcode, className, methodNameToCall, descToCall, false);

                // ga.visitVarInsn(Opcodes.ALOAD, 0);
                // ga.visitFieldInsn(Opcodes.GETFIELD, this.className,
                // TaintUtils.FAV_RECORD_TAG, "Z");
                // Label done2 = new Label();
                // ga.visitJumpInsn(Opcodes.IFEQ, done2);

                // FAV_GET_TIMESTAMP.delegateVisit(ga);
                // ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                // ga.visitVarInsn(Opcodes.ALOAD, path);
                // ga.visitLdcInsn(JREType.FILE.toString());
                // JRE_FAULT_AFTER.delegateVisit(ga);

                // ga.visitLabel(done2);
                lvs.freeTmpLV(path);
                lvs.freeTmpLV(fileOutStream);
            } else if (NativeMethodInspector.isNativeMethodNeedsRecordThirdPara(this.className, m.name, m.desc)
                    || NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToThirdPara(this.className, m.name, m.desc)) {
                // SocketOutputStream socketWrite0 socketRead0
                FAV_GET_RECORD_OUT.delegateVisit(ga);
                int fileOutStream = lvs.getTmpLV();
                ga.visitVarInsn(Opcodes.ASTORE, fileOutStream);

                Label nullOutStream = new Label();
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitLdcInsn(0); // set FAV_RECORD_TAG to false, avoid dead loop
                ga.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

                ga.visitLabel(nullOutStream);

                FAV_GET_TIMESTAMP.delegateVisit(ga);
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                ga.visitInsn(Opcodes.DUP);
                ga.visitLdcInsn(FAVPathType.FAVMSG.toString() + ":");
                ga.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V",
                        false);
                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, this.className, "socket", "Ljava/net/Socket;");
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/Socket", "getInetAddress",
                        "()Ljava/net/InetAddress;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress",
                        "()Ljava/lang/String;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                        "()Ljava/lang/String;", false);
                ga.visitLdcInsn(JREType.MSG.toString());
                JRE_FAULT_BEFORE.delegateVisit(ga);

                lvs.freeTmpLV(fileOutStream);

                ga.visitMethodInsn(opcode, className, methodNameToCall, descToCall, false);
            } else if (NativeMethodInspector.isNativeMethodNeedsRecordDatagramPacket(this.className, m.name, m.desc)
                    || NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToDatagramPacket(this.className, m.name,
                            m.desc)) {
                // PlainDatagramSocketImpl
                ga.visitVarInsn(Opcodes.ALOAD, 2); // get the DatagramPacket var
                ga.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(DatagramPacket.class));
                int packet = lvs.getTmpLV();
                ga.visitVarInsn(Opcodes.ASTORE, packet);

                FAV_GET_RECORD_OUT.delegateVisit(ga);
                int fileOutStream = lvs.getTmpLV();
                ga.visitVarInsn(Opcodes.ASTORE, fileOutStream);

                Label nullOutStream = new Label();
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitLdcInsn(0); // set FAV_RECORD_TAG to false, avoid dead loop
                ga.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

                ga.visitLabel(nullOutStream);

                ga.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                ga.visitInsn(Opcodes.DUP);
                ga.visitLdcInsn(FAVPathType.FAVMSG.toString() + ":");
                ga.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V",
                        false);
                ga.visitVarInsn(Opcodes.ALOAD, packet);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(DatagramPacket.class), "getAddress",
                        "()Ljava/net/InetAddress;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress",
                        "()Ljava/lang/String;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                        "()Ljava/lang/String;", false);
                int path = lvs.getTmpLV();
                ga.visitVarInsn(Opcodes.ASTORE, path);

                FAV_GET_TIMESTAMP.delegateVisit(ga);
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitVarInsn(Opcodes.ALOAD, path);
                ga.visitLdcInsn(JREType.MSG.toString());
                JRE_FAULT_BEFORE.delegateVisit(ga);

                lvs.freeTmpLV(fileOutStream);
                lvs.freeTmpLV(path);
                lvs.freeTmpLV(packet);

                ga.visitMethodInsn(opcode, className, methodNameToCall, descToCall, false);
            } else {
                ga.visitMethodInsn(opcode, className, methodNameToCall, descToCall, false);
            }
        }

        ga.visitLabel(end.getLabel());

        ga.returnValue();
        for (LocalVariableNode n : lvsToVisit) {
            n.accept(ga);
        }
        ga.visitMaxs(0, 0);
        ga.visitEnd();
    }

}
