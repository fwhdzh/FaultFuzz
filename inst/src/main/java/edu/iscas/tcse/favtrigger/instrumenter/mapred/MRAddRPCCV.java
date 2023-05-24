package edu.iscas.tcse.favtrigger.instrumenter.mapred;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.instrumenter.LocalVariableManager;
import edu.columbia.cs.psl.phosphor.instrumenter.PrimitiveArrayAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.SpecialOpcodeRemovingMV;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;

public class MRAddRPCCV  extends ClassVisitor {
    private String className;
    private String superName;
    private String[] interfaces;
    private List<MethodNode> methodsToAddMsgIdParam = new LinkedList<>();
    private boolean generateExtraLVDebug;
    private boolean ignoreFrames;
    private Map<MethodNode, MethodNode> forMore = new HashMap<>();
    private boolean fixLdcClass;
    public static List<String> rpcMethods = new LinkedList<>();
    private boolean isInterface = false;

    public MRAddRPCCV(ClassVisitor cv, boolean skipFrames) {
        super(Configuration.ASM_VERSION, cv);
        this.ignoreFrames = skipFrames;
    }

    @Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// TODO Auto-generated method stub
    	this.className = name;
    	this.superName = superName;
    	this.interfaces = interfaces;
    	this.generateExtraLVDebug = name.equals("java/lang/invoke/MethodType");
    	this.fixLdcClass = (version & 0xFFFF) < Opcodes.V1_5;
    	if((access & Opcodes.ACC_INTERFACE) != 0) {
            isInterface = true;
        }
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        boolean isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if(!isInterface && !isStatic) {
        	NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, access, name, desc, mv);
            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, access, className, desc, fixLdcClass);
            MRAddRPCMV addrpcmv = new MRAddRPCMV(soc, access, this.className, name, desc, signature, exceptions, null, this.superName, this.interfaces);
//            MRTrackingRPCMV mrmv = new MRTrackingRPCMV(addrpcmv, access, this.className, name, desc, signature, exceptions, null, this.superName, this.interfaces);
            LocalVariableManager lvs = new LocalVariableManager(access, desc, addrpcmv, an, mv, generateExtraLVDebug);
            Type returnType = Type.getReturnType(desc);
            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
            addrpcmv.setLocalVariableSorter(lvs);
            return addrpcmv;
        } else {
        	return mv;
        }
	}
}
