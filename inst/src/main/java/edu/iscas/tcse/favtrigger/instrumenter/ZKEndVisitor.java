package edu.iscas.tcse.favtrigger.instrumenter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.instrumenter.LocalVariableManager;
import edu.columbia.cs.psl.phosphor.instrumenter.PrimitiveArrayAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.SpecialOpcodeRemovingMV;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;

public class ZKEndVisitor extends ClassVisitor {

    IOTrackingClassVisitor ioTrackingCV;

    // public ZKEndVisitor(ClassVisitor cv) {
    //     super(Configuration.ASM_VERSION, cv);
    //     //TODO Auto-generated constructor stub
    // }

    public ZKEndVisitor(IOTrackingClassVisitor cv) {
        super(Configuration.ASM_VERSION, cv);
        //TODO Auto-generated constructor stub
        ioTrackingCV = cv;
    }

    public void ffVisitEnd() {
        String className = ioTrackingCV.getClassName();
        boolean ignoreFrames = ioTrackingCV.isIgnoreFrames();
        boolean fixLdcClass = ioTrackingCV.isFixLdcClass();
        boolean generateExtraLVDebug = ioTrackingCV.isGenerateExtraLVDebug();
        if(Configuration.USE_FAULT_FUZZ && (Configuration.FOR_ZK || Configuration.ZK_CLI)) {
        	if(className.equals("org/apache/jute/BinaryOutputArchive")){
            	MethodVisitor mv;
                int acc = Opcodes.ACC_PUBLIC;
                String favDesc = "()Ljava/net/InetAddress;";
                mv = super.visitMethod(acc, "getAddr$$FAV", favDesc, null, null);
                NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, "getAddr$$FAV", favDesc, mv);
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

                if((acc & Opcodes.ACC_STATIC) == 0) {
                    ga.visitVarInsn(Opcodes.ALOAD, 0);
                    lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
                }

                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, className, "out", "Ljava/io/DataOutput;");
                ga.visitTypeInsn(Opcodes.CHECKCAST, "java/io/DataOutputStream");
                ga.visitFieldInsn(Opcodes.GETFIELD, "java/io/DataOutputStream", "favAddr", "Ljava/net/InetAddress;");

                ga.visitLabel(end.getLabel());
                ga.returnValue();
                for(LocalVariableNode n : lvsToVisit) {
                    n.accept(ga);
                }
                ga.visitMaxs(0, 0);
                ga.visitEnd();
            }
        }
        if(Configuration.USE_FAULT_FUZZ && (Configuration.FOR_ZK || Configuration.ZK_CLI)) {
        	if(className.equals("org/apache/jute/BinaryInputArchive")){
            	MethodVisitor mv;
                int acc = Opcodes.ACC_PUBLIC;
                String favDesc = "()Ljava/net/InetAddress;";
                mv = super.visitMethod(acc, "getAddr$$FAV", favDesc, null, null);
                NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, "getAddr$$FAV", favDesc, mv);
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

                if((acc & Opcodes.ACC_STATIC) == 0) {
                    ga.visitVarInsn(Opcodes.ALOAD, 0);
                    lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
                }

                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, className, "in", "Ljava/io/DataInput;");
                ga.visitTypeInsn(Opcodes.CHECKCAST, "java/io/DataInputStream");
                ga.visitFieldInsn(Opcodes.GETFIELD, "java/io/DataInputStream", "favAddr", "Ljava/net/InetAddress;");

                ga.visitLabel(end.getLabel());
                ga.returnValue();
                for(LocalVariableNode n : lvsToVisit) {
                    n.accept(ga);
                }
                ga.visitMaxs(0, 0);
                ga.visitEnd();
            }
        }
    }
    
}
