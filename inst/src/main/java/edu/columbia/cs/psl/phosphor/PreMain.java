package edu.columbia.cs.psl.phosphor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.util.Random;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodTooLargeException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.instrumenter.HidePhosphorFromASMCV;
import edu.columbia.cs.psl.phosphor.instrumenter.PowerMockUtilCV;
import edu.columbia.cs.psl.phosphor.instrumenter.asm.OffsetPreservingClassReader;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.commons.OurJSRInlinerAdapter;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.commons.OurSerialVersionUIDAdder;
import edu.columbia.cs.psl.phosphor.runtime.TaintInstrumented;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.columbia.cs.psl.phosphor.struct.TaintedWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.iscas.tcse.favtrigger.instrumenter.CoverageMap;
import edu.iscas.tcse.favtrigger.instrumenter.IOTrackingClassVisitor;
import edu.iscas.tcse.favtrigger.instrumenter.annotation.InjectAnnotationIdentifier;
import edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAfl;
import edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAflInstrument.InstrumentationOptions;
import edu.iscas.tcse.favtrigger.instrumenter.cov.JavaAflInstrument.InstrumentingClassVisitor;
//import edu.iscas.tcse.favtrigger.instrumenter.CodeCoverageCV;
//import edu.iscas.tcse.favtrigger.instrumenter.mapred.MRAddParamCV;
// import edu.iscas.tcse.favtrigger.instrumenter.mapred.MRAddRPCCV;
//import edu.iscas.tcse.favtrigger.instrumenter.mapred.MRTrackingCV;
//import edu.iscas.tcse.favtrigger.instrumenter.rocksdb.RocksDBCV;
import edu.iscas.tcse.favtrigger.taint.FAVTaint;

public class PreMain {

    public static boolean DEBUG = System.getProperty("phosphor.debug") != null;
    public static boolean RUNTIME_INST = false;
    public static boolean INSTRUMENTATION_EXCEPTION_OCCURRED = false;
    public static ClassLoader bigLoader = PreMain.class.getClassLoader();
    public static String ip = "IP";//favtrigger
    public static String proc = "PROC";//favtrigger
    public static String myHome;
    //public static long processID = 0;

    /**
     * As I write this I realize what a multithreaded classloader mess this can create... let's see how bad it is.
     */
    public static ClassLoader curLoader;
    private static Instrumentation instrumentation;

    private PreMain() {
        // Prevents this class from being instantiated
    }

    public static void premain$$PHOSPHORTAGGED(String args, Instrumentation inst, ControlFlowStack ctrl) {
        Configuration.IMPLICIT_TRACKING = true;
        Configuration.init();
        premain(args, inst);
    }

    public static void premain(String args, Instrumentation inst) {
    	//ip = FAVTaint.getIP();
    	//processID = FAVTaint.getProcessID();
        inst.addTransformer(new ClassSupertypeReadingTransformer());
        RUNTIME_INST = true;
        if(args != null) {
            PhosphorOption.configure(true, parseArgs(args));
        }
        if(Configuration.FOR_YARN) {
            Configuration.JDK_MSG = false;
        }
        if (Configuration.IO_ALLOWLIST != null && Configuration.IO_ALLOWLIST.size() > 0) {
            for (String s : Configuration.IO_ALLOWLIST) {
                System.out.println("IO allowlist: " + s);
            }
        }
        if (Configuration.IO_DENYLIST != null && Configuration.IO_DENYLIST.size() > 0) {
            for (String s : Configuration.IO_DENYLIST) {
                System.out.println("IO denylist: " + s);
            }
        }

        if (Configuration.CACHE_DIR == null) {
            Configuration.CACHE_DIR = Configuration.OBSERVER_HOME + "/CacheFolder";
            File f = new File(Configuration.CACHE_DIR);
            if (!f.exists()) {
                if (!f.mkdir()) {
                    // The cache directory did not exist and the attempt to create it failed
                    System.err.printf("Failed to create cache directory: %s. Generated files are not being cached.\n",
                            Configuration.CACHE_DIR);
                    Configuration.CACHE_DIR = null;
                }
            }
        }

        if (Configuration.FAV_RECORD_PATH == null) {
            Configuration.FAV_RECORD_PATH = Configuration.OBSERVER_HOME + "/fav-rst";
            File f = new File(Configuration.FAV_RECORD_PATH);
            if (!f.exists()) {
                if (!f.mkdir()) {
                    // The cache directory did not exist and the attempt to create it failed
                    System.err.printf("Failed to create cache directory: %s. Generated files are not being cached.\n",
                            Configuration.FAV_RECORD_PATH);
                    Configuration.FAV_RECORD_PATH = null;
                }
            }
        }

        if (Configuration.COV_PATH == null) {
            Configuration.COV_PATH = Configuration.OBSERVER_HOME + "/fuzzcov";
            File f = new File(Configuration.COV_PATH);
            if (!f.exists()) {
                if (!f.mkdir()) {
                    System.err.printf("Failed to create cache directory: %s. Generated files are not being cached.\n",
                            Configuration.COV_PATH);
                    // The cache directory did not exist and the attempt to create it failed
                    Configuration.COV_PATH = null;
                }
                File file = new File(Configuration.COV_PATH.trim() + "/" + FAVTaint.getIP().replace("/", "_") + "-"
                        + FAVTaint.getProcessID() + "/" + "fuzzcov");
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                Configuration.COV_PATH = file.getAbsolutePath();
                CoverageMap.coverOutFile = file;
                JavaAfl.coverOutFile = file;
            }
        }

        // if(System.getProperty("phosphorCacheDirectory") != null) {
        //     Configuration.CACHE_DIR = System.getProperty("phosphorCacheDirectory");
        //     File f = new File(Configuration.CACHE_DIR);
        //     if(!f.exists()) {
        //         if(!f.mkdir()) {
        //             // The cache directory did not exist and the attempt to create it failed
        //             System.err.printf("Failed to create cache directory: %s. Generated files are not being cached.\n", Configuration.CACHE_DIR);
        //             Configuration.CACHE_DIR = null;
        //         }
        //     }
        // }

        // InjectAnnotationIdentifier.retrieveAnnotatedMethod(args);
        if (Configuration.ANNOTATION_FILE != null) {
            try {
                InjectAnnotationIdentifier.retrieveAnnotatedMethod(Configuration.ANNOTATION_FILE);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println(FAVTaint.getProcessID()
                +" Running with FAVTrigger! Use FaultFuzz:"+Configuration.USE_FAULT_FUZZ
                +", for Yarn:"+Configuration.FOR_YARN
                +", for MapReduce:"+Configuration.FOR_MR
                +", for HDFS:"+Configuration.FOR_HDFS
                +", for HBase:"+Configuration.FOR_HBASE
                +", for ZK:"+Configuration.FOR_ZK
                +", fav_home:"+myHome
                +", IS_THIRD_PARTY_PROTO:"+Configuration.IS_THIRD_PARTY_PROTO
                +", jdk_msg:"+Configuration.JDK_MSG
                +", use msgId:"+Configuration.USE_MSGID
                +", jdk_file:"+Configuration.JDK_FILE
                +", ZK_API:"+Configuration.ZK_API
                +", HDFS_API:"+Configuration.HDFS_API
                +", exec_mode:" + Configuration.EXEC_MODE
                +", determine_state:" + Configuration.DETERMINE_STATE   //only meanful for FaultFuzz mode
                +", annotation_file:" + Configuration.ANNOTATION_FILE
                + ", useInjectAnnotation: " + Configuration.USE_INJECT_ANNOTATION
                + ", ioAllow: " + Configuration.IO_ALLOW
                + ", ioDeny: " + Configuration.IO_DENY
                + ", observerHome: " + Configuration.OBSERVER_HOME
                +", Record Path:"+Configuration.FAV_RECORD_PATH
                + ", cacheDir: " + Configuration.CACHE_DIR
                + ", covPath: " + Configuration.COV_PATH

        );
        if(Instrumenter.loader == null) {
            Instrumenter.loader = bigLoader;
        }
        // Ensure that BasicSourceSinkManager & anything needed to call isSourceOrSinkOrTaintThrough gets initialized
//        BasicSourceSinkManager.getInstance().isSourceOrSinkOrTaintThrough(Object.class);
        inst.addTransformer(new PCLoggingTransformer());
//        inst.addTransformer(new SourceSinkTransformer(), true);
        instrumentation = inst;

    	System.out.println("bilibili premain end");
    }

    private static String[] parseArgs(String argString) {
        String[] args = argString.split(",");
        SinglyLinkedList<String> argList = new SinglyLinkedList<>();
        for(String arg : args) {
            int split = arg.indexOf('=');
            if(split == -1) {
                argList.addLast("-" + arg);
            } else {
                String option = arg.substring(0, split);
                String value = arg.substring(split + 1);
                argList.addLast("-" + option);
                argList.addLast(value);
            }
        }
        return argList.toArray(new String[0]);
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static final class PCLoggingTransformer extends PhosphorBaseTransformer {
        static boolean innerException = false;
        static MessageDigest md5inst;

        public PCLoggingTransformer() {
            TaintUtils.VERIFY_CLASS_GENERATION = System.getProperty("phosphor.verify") != null;
        }

        @Override
        public byte[] transform(ClassLoader loader, final String className2, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws IllegalClassFormatException {
        	return _transform(loader, className2, classBeingRedefined, protectionDomain, classfileBuffer);
        }

        static byte[] instrumentWithRetry(ClassReader cr, byte[] classFileBuffer, boolean isiFace, String className, boolean skipFrames, boolean upgradeVersion, List<FieldNode> fields, Set<String> methodsToReduceSizeOf, boolean traceClass) throws InstantiationException {
            TraceClassVisitor debugTracer = null;
            try {
                try {
                    ClassWriter cw = new HackyClassWriter(null, ClassWriter.COMPUTE_MAXS);
                    ClassVisitor _cv = cw;
                    _cv = new ClassVisitor(Opcodes.ASM7, cw) {
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            if(name.endsWith("$PHOSPHORTAGGED$$PHOSPHORTAGGED")){
                                throw new IllegalArgumentException();
                            }
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    };
                    if(traceClass) {
                        System.out.println("Saving " + className + " to debug-preinst/");
                        File f = new File("debug-preinst/" + className.replace("/", ".") + ".class");
                        if(!f.getParentFile().isDirectory() && !f.getParentFile().mkdirs()) {
                            System.err.println("Failed to make debug directory: " + f);
                        } else {
                            try {
                                FileOutputStream fos = new FileOutputStream(f);
                                fos.write(classFileBuffer);
                                fos.close();
                            } catch(Exception ex2) {
                                ex2.printStackTrace();
                            }
                        }
                        debugTracer = new TraceClassVisitor(null, null);
                        _cv = debugTracer;
                    }
                    if(Configuration.extensionClassVisitor != null) {
                        Constructor<? extends ClassVisitor> extra = Configuration.extensionClassVisitor.getConstructor(ClassVisitor.class, Boolean.TYPE);
                        _cv = extra.newInstance(_cv, skipFrames);
                    }
                    if(DEBUG || TaintUtils.VERIFY_CLASS_GENERATION) {
                        _cv = new CheckClassAdapter(_cv, false);
                    }

                    //add code
                    InstrumentationOptions options = new InstrumentationOptions(100, false, true);
                    Random random;
                    if (options.deterministic) {
                    	//the generated random sequence will be same under same seed.
                        random = new Random(java.util.Arrays.hashCode(classFileBuffer));
                    } else {
                        random = new Random();
                    }
                    if(CoverageMap.applyCov(className) && !Configuration.FOR_JAVA) {
//                        System.out.println("************add InstrumentingClassVisitor*************");
                        _cv = new InstrumentingClassVisitor(_cv, random, options, className);
                    }

                    _cv = new IOTrackingClassVisitor(_cv, skipFrames, fields, methodsToReduceSizeOf);

                    if(!isiFace) {
                    	_cv = new OurSerialVersionUIDAdder(_cv);
                    }

                    if(PowerMockUtilCV.isApplicable(className)) {
                        _cv = new PowerMockUtilCV(_cv);
                    }
                    if(Configuration.PRIOR_CLASS_VISITOR != null) {
                        try {
                            Constructor<? extends ClassVisitor> extra = Configuration.PRIOR_CLASS_VISITOR.getConstructor(ClassVisitor.class, Boolean.TYPE);
                            _cv = extra.newInstance(_cv, skipFrames);
                        } catch(Exception e) {
                            //
                        }
                    }
                    _cv = new HidePhosphorFromASMCV(_cv, upgradeVersion);

                    cr.accept(_cv, ClassReader.EXPAND_FRAMES);
                    byte[] instrumentedBytes = cw.toByteArray();
                    if (!traceClass && (DEBUG || TaintUtils.VERIFY_CLASS_GENERATION)) {

                        ClassReader cr2 = new ClassReader(instrumentedBytes);
                        try {
                            cr2.accept(new CheckClassAdapter(new ClassWriter(0), true), ClassReader.EXPAND_FRAMES);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            File f = new File("debug-verify/" + className.replace("/", ".") + ".class");
                            if (!f.getParentFile().isDirectory() && !f.getParentFile().mkdirs()) {
                                System.err.println("Failed to make debug directory: " + f);
                            } else {
                                try {
                                    FileOutputStream fos = new FileOutputStream(f);
                                    fos.write(instrumentedBytes);
                                    fos.close();
                                } catch (Exception ex2) {
                                    ex2.printStackTrace();
                                }
                                System.out.println("Saved broken class to " + f);
                            }
                        }
                    }

                    return instrumentedBytes;
                } catch(MethodTooLargeException ex) {
                    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!"+ex.getCodeSize());
                    if(methodsToReduceSizeOf == null) {
                        methodsToReduceSizeOf = new HashSet<>();
                    }
                    methodsToReduceSizeOf.add(ex.getMethodName() + ex.getDescriptor());
                    return instrumentWithRetry(cr, classFileBuffer, isiFace, className, skipFrames, upgradeVersion, fields,  methodsToReduceSizeOf, false);
                }
            } catch (Throwable ex) {
                INSTRUMENTATION_EXCEPTION_OCCURRED = true;
                if (!traceClass) {
                    System.err.println("Exception occurred while instrumenting " + className + ":");
                    ex.printStackTrace();
                    instrumentWithRetry(cr, classFileBuffer, isiFace, className, skipFrames, upgradeVersion, fields,  methodsToReduceSizeOf, true);
                    return classFileBuffer;
                }
                ex.printStackTrace();
                System.err.println("method so far:");
                try {
                    PrintWriter pw = new PrintWriter(new FileWriter("lastClass.txt"));
                    debugTracer.p.print(pw);
                    pw.flush();
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
                return classFileBuffer;
            }
        }

        public static byte[] _transform(ClassLoader loader, final String className2, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        	ClassReader cr = (Configuration.READ_AND_SAVE_BCI ? new OffsetPreservingClassReader(classfileBuffer) : new ClassReader(classfileBuffer));
        	String className = cr.getClassName();
            curLoader = loader;
            if(PhosphorInspector.isIgnoredClass(className)) {
                return classfileBuffer;
            }

            Configuration.taintTagFactory.instrumentationStarting(className);
            try {
                ClassNode cn = new ClassNode();
                cr.accept(cn, (Configuration.ALWAYS_CHECK_FOR_FRAMES ? 0 : ClassReader.SKIP_CODE));
                boolean skipFrames = false;
                boolean upgradeVersion = false;
                if(className.equals("org/jruby/parser/Ruby20YyTables")) {
                    cn.version = 51;
                    upgradeVersion = true;
                }
                if(cn.version >= 100 || cn.version <= 50 || className.endsWith("$Access4JacksonSerializer") || className.endsWith("$Access4JacksonDeSerializer")) {
                    skipFrames = true;
                } else if(Configuration.ALWAYS_CHECK_FOR_FRAMES) {
                    for(MethodNode mn : cn.methods) {
                        boolean hasJumps = false;
                        boolean foundFrame = false;
                        AbstractInsnNode ins = mn.instructions.getFirst();
                        if(!mn.tryCatchBlocks.isEmpty()) {
                            hasJumps = true;
                        }
                        while(ins != null) {
                            if(ins instanceof JumpInsnNode || ins instanceof TableSwitchInsnNode || ins instanceof LookupSwitchInsnNode) {
                                hasJumps = true;
                            }
                            if(ins instanceof FrameNode) {
                                foundFrame = true;
                                break;
                            }
                            ins = ins.getNext();
                        }
                        if(foundFrame) {
                            break;
                        }
                        if(hasJumps) {
                            skipFrames = true;
                            break;
                        }
                    }
                }
                if(cn.visibleAnnotations != null) {
                    for(Object o : cn.visibleAnnotations) {
                        AnnotationNode an = (AnnotationNode) o;
                        if(an.desc.equals(Type.getDescriptor(TaintInstrumented.class))) {
                            // System.out.println("***********INSTRUMENTED:"+className);
                            return classfileBuffer;
                        }
                    }
                }
                if(cn.interfaces != null) {
                    for(Object s : cn.interfaces) {
                        if(s.equals(Type.getInternalName(TaintedWithObjTag.class))) {
                            return classfileBuffer;
                        }
                    }
                }
                for(Object mn : cn.methods) {
                    if(((MethodNode) mn).name.equals("getPHOSPHOR_TAG")) {
                        return classfileBuffer;
                    }
                }
                for(Object fn : cn.fields) {
                    if(((FieldNode) fn).name.equals(TaintUtils.FAV_INST_MARK)) {
                    	// System.out.println("***********INSTRUMENTED field:"+className);
                        return classfileBuffer;
                    }
                }
                if(Configuration.CACHE_DIR != null) {
                    String cacheKey = className.replace("/", ".");
                    File f = new File(Configuration.CACHE_DIR + File.separator + cacheKey + ".md5sum");
                    if(f.exists()) {
                        try {
                            FileInputStream fis = new FileInputStream(f);
                            byte[] cachedDigest = new byte[1024];
                            fis.read(cachedDigest);
                            fis.close();
                            if(md5inst == null) {
                                md5inst = MessageDigest.getInstance("MD5");
                            }
                            byte[] checksum;
                            synchronized(md5inst) {
                                checksum = md5inst.digest(classfileBuffer);
                            }
                            boolean matches = true;
                            if(checksum.length > cachedDigest.length) {
                                matches = false;
                            }
                            if(matches) {
                                for(int i = 0; i < checksum.length; i++) {
                                    if(checksum[i] != cachedDigest[i]) {
                                        matches = false;
                                        break;
                                    }
                                }
                            }
                            if(matches) {
                                return Files.readAllBytes(new File(Configuration.CACHE_DIR + File.separator + cacheKey + ".class").toPath());
                            }
                        } catch(Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
                if(DEBUG) {
                    try {
                        File debugDir = new File("debug-preinst");
                        if(!debugDir.exists()) {
                            debugDir.mkdir();
                        }
                        File f = new File("debug-preinst/" + className.replace("/", ".") + ".class");
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(classfileBuffer);
                        fos.close();
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }

                boolean isiFace = (cn.access & Opcodes.ACC_INTERFACE) != 0;
                List<FieldNode> fields = new LinkedList<>();
                for(FieldNode node : cn.fields) {
                    fields.add(node);
                }
                if(skipFrames) {
                    // This class is old enough to not guarantee frames.
                    // Generate new frames for analysis reasons, then make sure
                    // to not emit ANY frames.
                    ClassWriter cw = new HackyClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    cr.accept(new ClassVisitor(Configuration.ASM_VERSION, cw) {
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                            return new OurJSRInlinerAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc, signature, exceptions);
                        }
                    }, 0);
                    cr = (Configuration.READ_AND_SAVE_BCI ? new OffsetPreservingClassReader(cw.toByteArray()) : new ClassReader(cw.toByteArray()));
                }
                // Find out if this class already has frames
                TraceClassVisitor cv;
                try {
                    byte[] instrumentedBytes = instrumentWithRetry(cr, classfileBuffer, isiFace, className, skipFrames, upgradeVersion, fields, null, false);

                    if(DEBUG) {
                        File f = new File("debug/" + className + ".class");
                        f.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(instrumentedBytes);
                        fos.close();
                    }

                    if(Configuration.CACHE_DIR != null) {
                        String cacheKey = className.replace("/", ".");
                        File f = new File(Configuration.CACHE_DIR + File.separator + cacheKey + ".class");
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(instrumentedBytes);
                        fos.close();
                        if(md5inst == null) {
                            md5inst = MessageDigest.getInstance("MD5");
                        }
                        byte[] checksum;
                        synchronized(md5inst) {
                            checksum = md5inst.digest(classfileBuffer);
                        }
                        f = new File(Configuration.CACHE_DIR + File.separator + cacheKey + ".md5sum");
                        fos = new FileOutputStream(f);

                        fos.write(checksum);
                        fos.close();
                        return instrumentedBytes;
                    }
                    return instrumentedBytes;
                } catch(Throwable ex) {
                    ex.printStackTrace();
                    throw new IllegalStateException(ex);
                }
            } finally {
                Configuration.taintTagFactory.instrumentationEnding(className);
            }
        }

        private static byte[] processBoolean(byte[] classFileBuffer) {
            ClassReader cr = new ClassReader(classFileBuffer);
            ClassNode cn = new ClassNode(Configuration.ASM_VERSION);
            cr.accept(cn, 0);
            boolean addField = true;
            for(Object o : cn.fields) {
                FieldNode fn = (FieldNode) o;
                if(fn.name.equals("valueOf")) {
                    addField = false;
                    break;
                }
            }
            for(Object o : cn.methods) {
                MethodNode mn = (MethodNode) o;
                if(mn.name.startsWith("toUpperCase") || mn.name.startsWith("codePointAtImpl") || mn.name.startsWith("codePointBeforeImpl")) {
                    mn.access = mn.access | Opcodes.ACC_PUBLIC;
                }
            }
            if(addField) {
                cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "valueOf", "Z", null, false));
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                return cw.toByteArray();
            }
            return classFileBuffer;
        }

        private static final class HackyClassWriter extends ClassWriter {

            private HackyClassWriter(ClassReader classReader, int flags) {
                super(classReader, flags);
            }

            private Class<?> getClass(String name) throws ClassNotFoundException {
                if(RUNTIME_INST) {
                    throw new ClassNotFoundException();
                }
                try {
                    return Class.forName(name.replace("/", "."), false, bigLoader);
                } catch(SecurityException e) {
                    throw new ClassNotFoundException("Security exception when loading class");
                } catch(Throwable e) {
                    throw new ClassNotFoundException();
                }
            }

            protected String getCommonSuperClass(String type1, String type2) {
                Class<?> c, d;
                try {
                    c = getClass(type1);
                    d = getClass(type2);
                } catch(ClassNotFoundException | ClassCircularityError e) {
                    return "java/lang/Object";
                }
                if(c.isAssignableFrom(d)) {
                    return type1;
                }
                if(d.isAssignableFrom(c)) {
                    return type2;
                }
                if(c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while(!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }
        }
    }
}
