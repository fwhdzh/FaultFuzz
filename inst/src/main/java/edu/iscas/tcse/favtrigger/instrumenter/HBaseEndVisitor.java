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

public class HBaseEndVisitor extends ClassVisitor {
    IOTrackingClassVisitor ioTrackingCV;

    public HBaseEndVisitor(IOTrackingClassVisitor cv) {
        super(Configuration.ASM_VERSION, cv);
        //TODO Auto-generated constructor stub
        ioTrackingCV = cv;
    }

    public int ffVisitField(int access, String name, String desc) {
        String className = ioTrackingCV.getClassName();
        int mAcess = access;
        if (className.endsWith("Service$BlockingStub") && name.equals("channel")
                && (desc.equals("Lorg/apache/hbase/thirdparty/com/google/protobuf/BlockingRpcChannel;")
                        || desc.equals("Lcom/google/protobuf/BlockingRpcChannel;"))) {
            mAcess = mAcess & ~Opcodes.ACC_PRIVATE;
            mAcess = mAcess & ~Opcodes.ACC_PROTECTED;
            mAcess = mAcess | Opcodes.ACC_PUBLIC;
        }
        return mAcess;
    }

    public void ffVisitEnd() {
        String className = ioTrackingCV.getClassName();
        boolean ignoreFrames = ioTrackingCV.isIgnoreFrames();
        boolean fixLdcClass = ioTrackingCV.isFixLdcClass();
        boolean generateExtraLVDebug = ioTrackingCV.isGenerateExtraLVDebug();
        if(className.equals("org/apache/hadoop/hbase/ipc/AbstractRpcClient$BlockingRpcChannelImplementation")){
        	MethodVisitor mv;
            int acc = Opcodes.ACC_PUBLIC;
            String favDesc = "()Ljava/net/InetSocketAddress;";
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
            if(Configuration.IS_THIRD_PARTY_PROTO) {
            	ga.visitFieldInsn(Opcodes.GETFIELD, className, "addr", "Ljava/net/InetSocketAddress;");
            } else {
            	ga.visitFieldInsn(Opcodes.GETFIELD, className, "addr", "Lorg/apache/hadoop/hbase/net/Address;");
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/apache/hadoop/hbase/net/Address", "toSocketAddress", "()Ljava/net/InetSocketAddress;",false);
            }

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
