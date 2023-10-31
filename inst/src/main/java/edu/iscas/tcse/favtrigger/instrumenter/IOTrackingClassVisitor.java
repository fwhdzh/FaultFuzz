package edu.iscas.tcse.favtrigger.instrumenter;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_RECORD_OUT;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_TIMESTAMP;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.JRE_FAULT_BEFORE;
//import static edu.columbia.cs.psl.phosphor.Configuration.controlFlowManager;
import static org.objectweb.asm.Opcodes.ALOAD;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.DatagramPacket;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
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
//import edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.instrumenter.LocalVariableManager;
//import edu.columbia.cs.psl.phosphor.instrumenter.MyInstOrUninstChoosingMV;
//import edu.columbia.cs.psl.phosphor.instrumenter.MyTaintLoadCoercer;
//import edu.columbia.cs.psl.phosphor.instrumenter.NullDefaultTaintCheckingMethodVisitor;
//import edu.columbia.cs.psl.phosphor.instrumenter.NullMethodArgReindexer;
//import edu.columbia.cs.psl.phosphor.instrumenter.NullReflectionHidingMV;
//import edu.columbia.cs.psl.phosphor.instrumenter.NullUninstrumentedCompatMV;
import edu.columbia.cs.psl.phosphor.instrumenter.PrimitiveArrayAnalyzer;
//import edu.columbia.cs.psl.phosphor.instrumenter.PrimitiveBoxingFixer;
import edu.columbia.cs.psl.phosphor.instrumenter.SpecialOpcodeRemovingMV;
//import edu.columbia.cs.psl.phosphor.instrumenter.TaintTagFieldCastMV;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.runtime.TaintInstrumented;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.iscas.tcse.favtrigger.instrumenter.annotation.inner.InnerAnnotationInjectMethodVisitor;
import edu.iscas.tcse.favtrigger.instrumenter.hbase.HBaseTrackingMV;
import edu.iscas.tcse.favtrigger.instrumenter.hdfs.HDFSTrackingMV;
import edu.iscas.tcse.favtrigger.instrumenter.jdk.FileOperationMV;
import edu.iscas.tcse.favtrigger.instrumenter.jdk.JRERunMode.JREType;
import edu.iscas.tcse.favtrigger.instrumenter.jdk.NIOTrackingMV;
import edu.iscas.tcse.favtrigger.instrumenter.mapred.MRProtocols;
//import edu.iscas.tcse.favtrigger.instrumenter.mapred.MRTrackingMV;
import edu.iscas.tcse.favtrigger.instrumenter.rocksdb.RocksDBWriteBatchPutMV;
import edu.iscas.tcse.favtrigger.instrumenter.yarn.YarnProtocols;
import edu.iscas.tcse.favtrigger.instrumenter.zk.ZKTrackingMV;
import edu.iscas.tcse.favtrigger.tracing.CheckPath.LinkType;
import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

/**
 * CV responsibilities: Add a field to classes to track each instance's taint
 * Add a method for each primitive returning method to return the taint of that
 * return Add a field to hold temporarily the return taint of each primitive
 *
 * @author jon
 */
public class IOTrackingClassVisitor extends ClassVisitor {

    private static final boolean NATIVE_BOX_UNBOX = true;
    public static boolean IS_RUNTIME_INST = true;
    private static boolean FIELDS_ONLY = false;
    private static boolean DO_OPT = false;

    static {
        if(!DO_OPT && !IS_RUNTIME_INST) {
            System.err.println("WARN: OPT DISABLED");
        }
    }

    private List<FieldNode> fields;
    private boolean generateHashCode = false;
    private boolean generateEquals = false;
    private Map<MethodNode, MethodNode> forMore = new HashMap<>();
    private boolean hasSerialUID = false;
    private boolean addTaintField = false;
    private boolean ignoreFrames;
    private boolean generateExtraLVDebug;
    private Map<MethodNode, Type> methodsToAddWrappersForWithReturnType = new HashMap<>();
    private List<MethodNode> methodsToAddWrappersFor = new LinkedList<>();
    private List<MethodNode> methodsToAddNameOnlyWrappersFor = new LinkedList<>();
    private List<MethodNode> methodsToAddUnWrappersFor = new LinkedList<>();
    private Set<MethodNode> methodsToAddLambdaUnWrappersFor = new HashSet<>();
    private String className;
    private boolean isNormalClass;
    private boolean isInterface;
    private boolean addTaintMethod;
    private boolean isAnnotation;
    private boolean isAbstractClass;
    private boolean implementsComparable;
    private boolean implementsSerializable;
    private boolean fixLdcClass;
    private boolean isEnum;
    private boolean isUninstMethods;
    private String classSource;
    private String classDebug;
    private Set<String> aggressivelyReduceMethodSize;
    private String superName;
    private boolean isLambda;
    private HashMap<String, Method> superMethodsToOverride = new HashMap<>();
    private HashMap<String, Method> methodsWithErasedTypesToAddWrappersFor = new HashMap<>();
    private LinkedList<MethodNode> wrapperMethodsToAdd = new LinkedList<>();
    private List<FieldNode> extraFieldsToVisit = new LinkedList<>();
    private List<FieldNode> myFields = new LinkedList<>();
    private Set<String> myMethods = new HashSet<>();

    private String[] interfaces;//favtrigger

    public ZKEndVisitor zkEndVisitor;


    public IOTrackingClassVisitor(ClassVisitor cv, boolean skipFrames, List<FieldNode> fields) {
        super(Configuration.ASM_VERSION, cv);
        DO_OPT = DO_OPT && !IS_RUNTIME_INST;
        this.ignoreFrames = skipFrames;
        this.fields = fields;
    }

    public IOTrackingClassVisitor(ClassVisitor cv, boolean skipFrames, List<FieldNode> fields, Set<String> aggressivelyReduceMethodSize) {
        this(cv, skipFrames, fields);
        this.aggressivelyReduceMethodSize = aggressivelyReduceMethodSize;
    }

    public String getClassName() {
        return className;
    }

    public boolean isIgnoreFrames() {
        return ignoreFrames;
    }

    public boolean isFixLdcClass() {
        return fixLdcClass;
    }

    public boolean isGenerateExtraLVDebug() {
        return generateExtraLVDebug;
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
        this.classSource = source;
        this.classDebug = debug;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.interfaces = interfaces; //favtrigger
        addTaintField = true;
        if((access & Opcodes.ACC_ABSTRACT) != 0) {
            isAbstractClass = true;
        }
        if((access & Opcodes.ACC_INTERFACE) != 0) {
            addTaintField = false;
            isInterface = true;
        }
        if((access & Opcodes.ACC_ENUM) != 0) {
            isEnum = true;
            addTaintField = false;
        }

        if((access & Opcodes.ACC_ANNOTATION) != 0) {
            isAnnotation = true;
        }

        //Debugging - no more package-protected
        if((access & Opcodes.ACC_PRIVATE) == 0) {
            access = access | Opcodes.ACC_PUBLIC;
        }

        if(!superName.equals("java/lang/Object") && !PhosphorInspector.isIgnoredClass(superName)) {
            addTaintField = false;
            addTaintMethod = true;
        }
        if(name.equals("java/awt/image/BufferedImage") || name.equals("java/awt/image/Image")) {
            addTaintField = false;
        }

        isLambda = name.contains("$$Lambda$");

        isNormalClass = (access & Opcodes.ACC_ENUM) == 0 && (access & Opcodes.ACC_INTERFACE) == 0;

        super.visit(version, access, name, signature, superName, interfaces);
        this.visitAnnotation(Type.getDescriptor(TaintInstrumented.class), false);
        this.className = name;
        this.superName = superName;

        this.isUninstMethods = PhosphorInspector.isIgnoredClassWithStubsButNoTracking(className);
    }

    

	@Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if(PhosphorInspector.isIgnoredClass(this.className)) {
    		return super.visitMethod(access, name, desc, signature, exceptions);
    	}

    	if (name.contains(TaintUtils.METHOD_SUFFIX)) {
            //Some dynamic stuff might result in there being weird stuff here
            return new MethodVisitor(Configuration.ASM_VERSION) {
            };
        }

       
        

    	Type oldReturnType = Type.getReturnType(desc);
    	boolean isImplicitLightTrackingMethod = false;
    	if((access & Opcodes.ACC_NATIVE) == 0) {

            if (!CoverageMap.useIOInst(this.className, name, desc)) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
            // System.out.println("IOInst: " + this.className + " " + name + " " + desc);
    

      		 //not a native method
    		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    		SpecialOpcodeRemovingMV specialOpcodeRemovingMV = new SpecialOpcodeRemovingMV(mv, ignoreFrames, access, className, desc, fixLdcClass);
            mv = specialOpcodeRemovingMV;
            NeverNullArgAnalyzerAdapter analyzer =  new NeverNullArgAnalyzerAdapter(className, access, name, desc, mv);
//            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, access, className, desc, fixLdcClass);

            //make all the methods who calls a native method to call the wrapper.
     		UsingNativeWrapperMV nativemv = new UsingNativeWrapperMV(access, this.className, name, desc, signature, exceptions, analyzer);

     		FileOperationMV loinfomv = new FileOperationMV(nativemv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            loinfomv.setFields(fields);
//
            NIOTrackingMV niomv = new NIOTrackingMV(loinfomv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            niomv.setFields(fields);

//            LevelDBAPIModelMV leveldbapimv = new LevelDBAPIModelMV(boxFixer, access, className, name, newDesc, signature, exceptions, desc, analyzer);

            HDFSAPIModelMV hdfsapimv = new HDFSAPIModelMV(niomv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            hdfsapimv.setFields(fields);

            ZKAPIModelMV zkapimv = new ZKAPIModelMV(hdfsapimv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            zkapimv.setFields(fields);

//            MRTrackingMV mapredmv = new MRTrackingMV(zkapimv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
//            mapredmv.setFields(fields);

//            YarnTrackingMV yarnmv = new YarnTrackingMV(zkapimv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
//            yarnmv.setFields(fields);

            HDFSTrackingMV dfsmv = new HDFSTrackingMV(zkapimv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            dfsmv.setFields(fields);

            HBaseTrackingMV hbmv = new HBaseTrackingMV(dfsmv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            hbmv.setFields(fields);

     		ZKTrackingMV zkmv = new ZKTrackingMV(hbmv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            zkmv.setFields(fields);

//            TestMV tmv = new TestMV(niomv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
//            tmv.setFields(fields);

            // AnnotationBasedInjectionMethodVisitor amv = new AnnotationBasedInjectionMethodVisitor(zkmv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);
            // amv.setFields(fields);
            InnerAnnotationInjectMethodVisitor amv = new InnerAnnotationInjectMethodVisitor(zkmv, access, className, name, desc, signature, exceptions, desc, analyzer, this.superName, this.interfaces);


            // LocalVariableManager lvs = new LocalVariableManager(access, desc, zkmv, analyzer, mv, generateExtraLVDebug);
            LocalVariableManager lvs = new LocalVariableManager(access, desc, amv, analyzer, mv, generateExtraLVDebug);
            lvs.disable();
//            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));

            specialOpcodeRemovingMV.setLVS(lvs);
//            tmv.setLocalVariableSorter(lvs);
            dfsmv.setLocalVariableSorter(lvs);
            hbmv.setLocalVariableSorter(lvs);

            
            zkmv.setLocalVariableSorter(lvs);

            loinfomv.setLocalVariableSorter(lvs);
            niomv.setLocalVariableSorter(lvs);
            hdfsapimv.setLocalVariableSorter(lvs);
            zkapimv.setLocalVariableSorter(lvs);
//            mapredmv.setLocalVariableSorter(lvs);
//            yarnmv.setLocalVariableSorter(lvs);

            amv.setLocalVariableSorter(lvs);

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
             MethodNode rawMethod = new MethodNode(Configuration.ASM_VERSION, access, name, desc, signature, exceptions) {
                @Override
                public void visitEnd() {
                    super.visitEnd();
                    this.accept(prev);
                }
             };
             if(isNativeIO(className, name, desc)) {
                 // this is a native IO method. we want here to make a $wrapper method that will call the original one.
          		 MethodNode wrapper = new MethodNode(access, name, desc, signature, exceptions);
          		 methodsToAddWrappersFor.add(wrapper);
                 forMore.put(wrapper, rawMethod);
             }
             return rawMethod;
      	 }
    }

    @Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		// TODO Auto-generated method stub
    	if(PhosphorInspector.isIgnoredClassWithStubsButNoTracking(className)){
            return super.visitField(access, name, desc, signature, value);
        }
        //favtrigger: add field on yarn rpc client side to store des address
        if(Configuration.USE_FAULT_FUZZ && Configuration.FOR_YARN || Configuration.YARN_RPC) {
            if(YarnProtocols.isYarnProtocol(desc) && this.className.startsWith("org/apache/hadoop")){
                super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_RPC_SOCKET, "Ljava/lang/String;", null, null);
            }
        }
        if(Configuration.USE_FAULT_FUZZ && Configuration.FOR_MR || Configuration.MR_RPC) {
            if(MRProtocols.isMRProtocol(desc) && this.className.startsWith("org/apache/hadoop")){
                super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_RPC_SOCKET, "Ljava/lang/String;", null, null);
            }
        }
        if(Configuration.USE_FAULT_FUZZ && Configuration.FOR_HBASE || Configuration.HBASE_RPC) {
        	access = HbaseVisitFiled(access, name, desc);
        }
        //favtrigger: end
        if (!hasSerialUID && name.equals("serialVersionUID")) {
            hasSerialUID = true;
        }
        if((access & Opcodes.ACC_STATIC) == 0) {
            myFields.add(new FieldNode(access, name, desc, signature, value));
        }
        return super.visitField(access, name, desc, signature, value);
	}

    private int HbaseVisitFiled(int access, String name, String desc) {
        HBaseEndVisitor hBaseEndVisitor = new HBaseEndVisitor(this);
        return hBaseEndVisitor.ffVisitField(access, name, desc);
        // int mAcess = access;
        // if (this.className.endsWith("Service$BlockingStub") && name.equals("channel")
        //         && (desc.equals("Lorg/apache/hbase/thirdparty/com/google/protobuf/BlockingRpcChannel;")
        //                 || desc.equals("Lcom/google/protobuf/BlockingRpcChannel;"))) {
        //     mAcess = mAcess & ~Opcodes.ACC_PRIVATE;
        //     mAcess = mAcess & ~Opcodes.ACC_PROTECTED;
        //     mAcess = mAcess | Opcodes.ACC_PUBLIC;
        // }
        // return mAcess;
    }

	@Override
	public void visitEnd() {
        boolean goLightOnGeneratedStuff = className.equals("java/lang/Byte");// || isLambda;
        if(!hasSerialUID && !isInterface && !goLightOnGeneratedStuff) {
            super.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "serialVersionUIDPHOSPHOR_TAG", Configuration.TAINT_TAG_DESC, null, null);
        }
		// TODO Auto-generated method stub
    	//favtrigger: add a field for classes that need to record critical Taint
        //info, avoid dead loop
        if(addTaintField && !goLightOnGeneratedStuff) {
            super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_INST_MARK, "Z", null, 1);
        }
        if(NativeMethodInspector.isClassNeedToDecideIfRecordTaint(this.className)){
            super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_RECORD_TAG, "Z", null, 1);
        }
        if(this.className.equals("java/io/DataInputStream")){
            super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;", null, null);
        }
        if(this.className.equals("java/nio/ByteBuffer")){
            super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_BUFFER_MSGID_FIELD, "I", null, Integer.MAX_VALUE);
        }
        if(this.className.equals("org/apache/hadoop/fs/FSDataOutputStream")
            || this.className.equals("org/apache/hadoop/fs/FSDataInputStream")) {
                super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_HDFSSTREAM_PATH, "Ljava/lang/String;", null, null);
        }
        if(this.className.equals("org/fusesource/leveldbjni/internal/NativeDB")
            || this.className.equals("org/fusesource/leveldbjni/internal/NativeWriteBatch")) {
                super.visitField(Opcodes.ACC_PUBLIC, TaintUtils.FAV_PATH, "Ljava/lang/String;", null, null);
        }
        if (this.className.equals("org/rocksdb/WriteBatch")) {
            super.visitField(Opcodes.ACC_PUBLIC, RocksDBWriteBatchPutMV.combinedTaintFiledName, "Ledu/columbia/cs/psl/phosphor/runtime/Taint;", null, null); // Taint.emptyTaint()
        }
        if(this.className.equals("java/io/OutputStream")){
            super.visitField(Opcodes.ACC_PUBLIC, "favAddr", "Ljava/net/InetAddress;", null, null);
        }
        if(this.className.equals("java/io/InputStream")){
            super.visitField(Opcodes.ACC_PUBLIC, "favAddr", "Ljava/net/InetAddress;", null, null);
        }
        //favtrigger end
    	//favtrigger: add method to set a fileoutputstream object's
        //FAV_RECORD_TAG to false if not null
        if(this.className.equals("java/io/FileOutputStream")){
        	MethodVisitor mv;
            int acc = Opcodes.ACC_PUBLIC;
            String favDesc = "()V";
            mv = super.visitMethod(acc, TaintUtils.FAV_FILEOUT_NOTRECORD, favDesc, null, null);
            NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, TaintUtils.FAV_FILEOUT_NOTRECORD, favDesc, mv);
            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, acc, className, favDesc, fixLdcClass);
            LocalVariableManager lvs = new LocalVariableManager(acc, favDesc, soc, an, mv, generateExtraLVDebug);
            LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
            Type returnType = Type.getReturnType(favDesc);
            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
            GeneratorAdapter ga = new GeneratorAdapter(lvs, acc, TaintUtils.FAV_FILEOUT_NOTRECORD, favDesc);
            LabelNode start = new LabelNode(new Label());
            LabelNode end = new LabelNode(new Label());
            ga.visitCode();
            ga.visitLabel(start.getLabel());

            if((acc & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
            }

            Label nullOutStream = new Label();
            ga.visitVarInsn(Opcodes.ALOAD, 0);
            ga.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

            ga.visitVarInsn(Opcodes.ALOAD, 0);
            ga.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            ga.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

            ga.visitLabel(nullOutStream);

            ga.visitLabel(end.getLabel());
            ga.returnValue();
            for(LocalVariableNode n : lvsToVisit) {
                n.accept(ga);
            }
            ga.visitMaxs(0, 0);
            ga.visitEnd();
        }
        if(this.className.equals("java/nio/ByteBuffer")){
        	MethodVisitor mv;
            int acc = Opcodes.ACC_PUBLIC;
            String favDesc = "()[B";
            mv = super.visitMethod(acc, TaintUtils.FAV_GETBUFFERSHADOW_MT, favDesc, null, null);
            NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, TaintUtils.FAV_GETBUFFERSHADOW_MT, favDesc, mv);
            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, acc, className, favDesc, fixLdcClass);
            LocalVariableManager lvs = new LocalVariableManager(acc, favDesc, soc, an, mv, generateExtraLVDebug);
            LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
            Type returnType = Type.getReturnType(favDesc);
            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
            GeneratorAdapter ga = new GeneratorAdapter(lvs, acc, TaintUtils.FAV_GETBUFFERSHADOW_MT, favDesc);
            LabelNode start = new LabelNode(new Label());
            LabelNode end = new LabelNode(new Label());
            ga.visitCode();
            ga.visitLabel(start.getLabel());

            if((acc & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
            }

            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "hb", "[B");

            ga.visitLabel(end.getLabel());
            ga.returnValue();
            for(LocalVariableNode n : lvsToVisit) {
                n.accept(ga);
            }
            ga.visitMaxs(0, 0);
            ga.visitEnd();
        }
        if(Configuration.USE_FAULT_FUZZ && Configuration.FOR_HBASE || Configuration.HBASE_RPC) {
        	HBaseVisitEnd();
        }

        if(Configuration.USE_FAULT_FUZZ && Configuration.FOR_ZK) {
            ZKVisitEnd();
        }
        //favtrigger: end

        //favtrigger: add get path method FileInputStream & FileOutputStream
        if(this.className.equals("java/io/FileInputStream") || this.className.equals("java/io/FileOutputStream") || this.className.equals("java/io/RandomAccessFile")){
        	MethodVisitor mv;
            int acc = Opcodes.ACC_PUBLIC;
            mv = super.visitMethod(acc, TaintUtils.FAV_GETFILESTREAMPATH_MT, "()Ljava/lang/String;", null, null);
            NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, TaintUtils.FAV_FILEDESCRIPTOR_MT, "()Ljava/lang/String;", mv);
            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, acc, className, "()Ljava/lang/String;", fixLdcClass);
            LocalVariableManager lvs = new LocalVariableManager(acc, "()Ljava/lang/String;", soc, an, mv, generateExtraLVDebug);
            LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
            Type returnType = Type.getReturnType("()Ljava/lang/String;");
            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
            GeneratorAdapter ga = new GeneratorAdapter(lvs, acc, TaintUtils.FAV_GETFILESTREAMPATH_MT, "()Ljava/lang/String;");
            LabelNode start = new LabelNode(new Label());
            LabelNode end = new LabelNode(new Label());
            ga.visitCode();
            ga.visitLabel(start.getLabel());

            if((acc & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
            }

            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "path", "Ljava/lang/String;");
            int path = lvs.getTmpLV();
            ga.visitVarInsn(Opcodes.ASTORE, path);
            ga.visitVarInsn(Opcodes.ALOAD, path);
            Label nonNull = new Label();
            ga.visitJumpInsn(Opcodes.IFNONNULL, nonNull);
            ga.visitLdcInsn("");
            ga.visitVarInsn(Opcodes.ASTORE, path);
            ga.visitLabel(nonNull);
            ga.visitVarInsn(ALOAD, path);

            lvs.freeTmpLV(path);

            ga.visitLabel(end.getLabel());
            ga.returnValue();
            for(LocalVariableNode n : lvsToVisit) {
                n.accept(ga);
            }
            ga.visitMaxs(0, 0);
            ga.visitEnd();
        }
        //favtrigger: end

        //favtrigger: add check source method for FileDescriptor
        if(this.className.equals("java/io/FileDescriptor")){
            MethodVisitor mv;
            int acc = Opcodes.ACC_PUBLIC;
            mv = super.visitMethod(acc, TaintUtils.FAV_FILEDESCRIPTOR_MT, "()Ljava/lang/String;", null, null);
            NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, acc, TaintUtils.FAV_FILEDESCRIPTOR_MT, "()Ljava/lang/String;", mv);
            MethodVisitor soc = new SpecialOpcodeRemovingMV(an, ignoreFrames, acc, className, "()Ljava/lang/String;", fixLdcClass);
            LocalVariableManager lvs = new LocalVariableManager(acc, "()Ljava/lang/String;", soc, an, mv, generateExtraLVDebug);
            LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
            Type returnType = Type.getReturnType("()Ljava/lang/String;");
            lvs.setPrimitiveArrayAnalyzer(new PrimitiveArrayAnalyzer(returnType));
            GeneratorAdapter ga = new GeneratorAdapter(lvs, acc, TaintUtils.FAV_FILEDESCRIPTOR_MT, "()Ljava/lang/String;");
            LabelNode start = new LabelNode(new Label());
            LabelNode end = new LabelNode(new Label());
            ga.visitCode();
            ga.visitLabel(start.getLabel());

            if((acc & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, 0));
            }

            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(FileOutputStream.class));
            Label notFileOut = new Label();
            Label done = new Label();
            ga.visitJumpInsn(Opcodes.IFEQ, notFileOut);
            
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(FileOutputStream.class), TaintUtils.FAV_GETFILESTREAMPATH_MT, "()Ljava/lang/String;", false);
            ga.visitJumpInsn(Opcodes.GOTO, done);

            ga.visitLabel(notFileOut);
            Label notFileIn = new Label();
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitTypeInsn(Opcodes.INSTANCEOF, "java/io/FileInputStream");
            ga.visitJumpInsn(Opcodes.IFEQ, notFileIn);
            
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/FileInputStream", TaintUtils.FAV_GETFILESTREAMPATH_MT, "()Ljava/lang/String;", false);
            ga.visitJumpInsn(Opcodes.GOTO, done);

            ga.visitLabel(notFileIn);
            Label notRandomAcc = new Label();
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitTypeInsn(Opcodes.INSTANCEOF, "java/io/RandomAccessFile");
            ga.visitJumpInsn(Opcodes.IFEQ, notRandomAcc);
            
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/RandomAccessFile", TaintUtils.FAV_GETFILESTREAMPATH_MT, "()Ljava/lang/String;", false);
            ga.visitJumpInsn(Opcodes.GOTO, done);

            ga.visitLabel(notRandomAcc);
            Label notDatagramSocket = new Label();
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitTypeInsn(Opcodes.INSTANCEOF, "java/net/DatagramSocket");
            ga.visitJumpInsn(Opcodes.IFEQ, notDatagramSocket);
            ga.visitLdcInsn(FAVPathType.FAVMSG.toString()+":DatagramSocket");
            ga.visitJumpInsn(Opcodes.GOTO, done);

            ga.visitLabel(notDatagramSocket);
            Label notServerSocket = new Label();
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitTypeInsn(Opcodes.INSTANCEOF, "java/net/ServerSocket");
            ga.visitJumpInsn(Opcodes.IFEQ, notServerSocket);
            ga.visitLdcInsn(FAVPathType.FAVMSG.toString()+":ServerSocket");
            ga.visitJumpInsn(Opcodes.GOTO, done);

            ga.visitLabel(notServerSocket);
            Label notSocket = new Label();
            ga.visitVarInsn(ALOAD, 0);
            ga.visitFieldInsn(Opcodes.GETFIELD, className, "parent", Type.getDescriptor(Closeable.class));
            ga.visitTypeInsn(Opcodes.INSTANCEOF, "java/net/Socket");
            ga.visitJumpInsn(Opcodes.IFEQ, notSocket);
            ga.visitLdcInsn(FAVPathType.FAVMSG.toString()+":Socket");
            ga.visitJumpInsn(Opcodes.GOTO, done);

            ga.visitLabel(notSocket);
            //ga.visitLdcInsn("other");
            ga.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            ga.visitInsn(Opcodes.DUP);
            ga.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(LinkType.class), "OTHER", Type.getDescriptor(LinkType.class));
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(LinkType.class), "toString", "()Ljava/lang/String;", false);
            ga.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
            ga.visitLdcInsn(":");
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            ga.visitVarInsn(ALOAD, 0);
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder",
            "toString", "()Ljava/lang/String;", false);

            ga.visitLabel(done);

            ga.visitLabel(end.getLabel());
            ga.returnValue();
            for(LocalVariableNode n : lvsToVisit) {
                n.accept(ga);
            }
            ga.visitMaxs(0, 0);
            ga.visitEnd();
        }
        //favtrigger: end

        for(MethodNode m : methodsToAddWrappersFor) {
            if((m.access & Opcodes.ACC_NATIVE) != 0) {
            	// Generate wrapper for native method - a native wrapper
                generateNativeIOWrapper(m, m.name);
            }
        }

		super.visitEnd();
	}

    private void HBaseVisitEnd() {
        HBaseEndVisitor hBaseEndVisitor = new HBaseEndVisitor(this);
        hBaseEndVisitor.ffVisitEnd();
    }

    public void ZKVisitEnd() {
        ZKEndVisitor zkEndVisitor = new ZKEndVisitor(this);
        zkEndVisitor.ffVisitEnd();
    }

	public boolean isNativeIO(String className, String name, String desc) {
    	return (NativeMethodInspector.isNativeMethodNeedsRecordSecondPara(className, name, desc)
    			|| NativeMethodInspector.isNativeMethodNeedsRecordThirdPara(className, name, desc)
                || NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToThirdPara(className, name, desc)
                || NativeMethodInspector.isNativeMethodNeedsRecordDatagramPacket(className, name, desc)
                || NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToDatagramPacket(className, name, desc));
//                || Instrumenter.isNativeMethodNeedsNewRtnTaint(className, name, desc)
//                || Instrumenter.isNativeMethodNeedsCombineNewTaintToSecondPara(className, name, desc));
    }

    public class UsingNativeWrapperMV extends MethodVisitor implements Opcodes {

        protected String owner;
        protected String name;
        protected String descriptor;

        public UsingNativeWrapperMV(MethodVisitor methodVisitor) {
			super(Configuration.ASM_VERSION, methodVisitor);
			// TODO Auto-generated constructor stub
		}
		public UsingNativeWrapperMV(int api, MethodVisitor methodVisitor) {
			super(api, methodVisitor);
			// TODO Auto-generated constructor stub
		}
		public UsingNativeWrapperMV(int access, String className, String name, String desc, String signature, String[] exceptions, MethodVisitor mv) {
	        super(Configuration.ASM_VERSION, mv);
	        this.owner = className;
	        this.name = name;
	        this.descriptor = desc;
	    }

		@Override
		public void visitCode() {
			// TODO Auto-generated method stub
			super.visitCode();
			//favtrigger: set FAV_RECORD_TAG and FAV_MSGWRAPPER_TAG to true
	        if(NativeMethodInspector.isClassNeedToDecideIfRecordTaint(this.owner) && this.name.equals("<init>")){
	        	super.visitVarInsn(Opcodes.ALOAD, 0);
	        	super.visitInsn(Opcodes.ICONST_1);
	        	super.visitFieldInsn(Opcodes.PUTFIELD, this.owner, TaintUtils.FAV_RECORD_TAG, "Z");
	        }
	        if(this.owner.equals("java/io/DataInputStream") && this.name.equals("<init>")){
	        	super.visitVarInsn(Opcodes.ALOAD, 0);
	        	super.visitInsn(Opcodes.ACONST_NULL);
	        	super.visitFieldInsn(Opcodes.PUTFIELD, this.owner, TaintUtils.FAV_TAINT_PATH, "Ljava/lang/String;");
	        }
	        if(this.owner.equals("java/nio/ByteBuffer") && this.name.equals("<init>")){
	            super.visitVarInsn(Opcodes.ALOAD, 0);
	            super.visitLdcInsn(Integer.MAX_VALUE);
	            super.visitFieldInsn(Opcodes.PUTFIELD, this.owner, TaintUtils.FAV_BUFFER_MSGID_FIELD, "I");
	        }
	        if(NativeMethodInspector.isSocketChannelWrite(this.owner, this.name, this.descriptor)){
	        }
	        if (NativeMethodInspector.isSocketChannelRead(this.owner, this.name, this.descriptor)) {
	        }
	        if(this.owner.equals("sun/nio/ch/FileChannelImpl") && this.name.startsWith("write")) {

	        }
	        //favtrigger: end
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
			// TODO Auto-generated method stub
			if(!name.contains(TaintUtils.METHOD_SUFFIX) && (isNativeIO(owner, name, descriptor))
					) {
				name = name + TaintUtils.METHOD_SUFFIX;
			}
			super.visitMethodInsn(opcode, owner, name, descriptor);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			// TODO Auto-generated method stub
			if(!name.contains(TaintUtils.METHOD_SUFFIX) && (isNativeIO(owner, name, descriptor))
					) {
				name = name + TaintUtils.METHOD_SUFFIX;
			}
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
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
        if(isInterfaceMethod && forMore.get(m) != null && forMore.get(m).instructions.size() > 0){
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
        if(isInterfaceMethod) {
            ga.visitEnd();
            return;
        }
        ga.visitCode();
        ga.visitLabel(start.getLabel());
        if(!isLambda) {
            String descToCall = m.desc;
            boolean isUntaggedCall = false;
            int idx = 0;
            if((m.access & Opcodes.ACC_STATIC) == 0) {
                ga.visitVarInsn(ALOAD, 0);
                lvsToVisit.add(new LocalVariableNode("this", "L" + className + ";", null, start, end, idx));
                idx++; //this
            }
            for(Type t : argTypes) {
                ga.visitVarInsn(t.getOpcode(Opcodes.ILOAD), idx);

                idx += t.getSize();
            }
            int opcode;
            if((m.access & Opcodes.ACC_STATIC) == 0) {
                opcode = Opcodes.INVOKESPECIAL;
            } else {
                opcode = Opcodes.INVOKESTATIC;
            }
            if(NativeMethodInspector.isNativeMethodNeedsRecordSecondPara(this.className, m.name, m.desc)) {
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
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/FileDescriptor", TaintUtils.FAV_FILEDESCRIPTOR_MT, "()Ljava/lang/String;", false);
                ga.visitVarInsn(Opcodes.ASTORE, path);

                //check if the path is "", then it may be a write to the console,
                //and we just skip recording this operation
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
            	ga.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            	ga.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

                ga.visitLabel(nullOutStream);

                FAV_GET_TIMESTAMP.delegateVisit(ga);
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitVarInsn(Opcodes.ALOAD, path);
                ga.visitLdcInsn(JREType.FILE.toString());
                JRE_FAULT_BEFORE.delegateVisit(ga);

                ga.visitLabel(done1);

                ga.visitMethodInsn(opcode, className, methodNameToCall, descToCall, false);

                lvs.freeTmpLV(path);
                lvs.freeTmpLV(fileOutStream);
            } else if(NativeMethodInspector.isNativeMethodNeedsRecordThirdPara(this.className, m.name, m.desc)
            		|| NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToThirdPara(this.className, m.name, m.desc)) {
            	//SocketOutputStream socketWrite0 socketRead0
            	FAV_GET_RECORD_OUT.delegateVisit(ga);
                int fileOutStream = lvs.getTmpLV();
                ga.visitVarInsn(Opcodes.ASTORE, fileOutStream);

                Label nullOutStream = new Label();
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            	ga.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            	ga.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

                ga.visitLabel(nullOutStream);

                FAV_GET_TIMESTAMP.delegateVisit(ga);
                ga.visitVarInsn(Opcodes.ALOAD, fileOutStream);
                ga.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                ga.visitInsn(Opcodes.DUP);
                ga.visitLdcInsn(FAVPathType.FAVMSG.toString()+":");
                ga.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
                ga.visitVarInsn(Opcodes.ALOAD, 0);
                ga.visitFieldInsn(Opcodes.GETFIELD, this.className, "socket", "Ljava/net/Socket;");
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/Socket", "getInetAddress", "()Ljava/net/InetAddress;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                "()Ljava/lang/String;", false);
                ga.visitLdcInsn(JREType.MSG.toString());
                JRE_FAULT_BEFORE.delegateVisit(ga);

                lvs.freeTmpLV(fileOutStream);

                ga.visitMethodInsn(opcode, className, methodNameToCall, descToCall, false);
            } else if (NativeMethodInspector.isNativeMethodNeedsRecordDatagramPacket(this.className, m.name, m.desc)
            		|| NativeMethodInspector.isNativeMethodNeedsCombineNewTaintToDatagramPacket(this.className, m.name, m.desc)){
            	//PlainDatagramSocketImpl
            	ga.visitVarInsn(Opcodes.ALOAD, 2); //get the DatagramPacket var
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
            	ga.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            	ga.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

                ga.visitLabel(nullOutStream);

                ga.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                ga.visitInsn(Opcodes.DUP);
                ga.visitLdcInsn(FAVPathType.FAVMSG.toString()+":");
                ga.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
                ga.visitVarInsn(Opcodes.ALOAD, packet);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(DatagramPacket.class), "getAddress", "()Ljava/net/InetAddress;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
                ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
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
        for(LocalVariableNode n : lvsToVisit) {
            n.accept(ga);
        }
        ga.visitMaxs(0, 0);
        ga.visitEnd();
    }
}
