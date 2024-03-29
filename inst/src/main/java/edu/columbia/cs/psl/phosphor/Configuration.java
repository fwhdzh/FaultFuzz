package edu.columbia.cs.psl.phosphor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import edu.columbia.cs.psl.phosphor.control.ControlFlowManager;
import edu.columbia.cs.psl.phosphor.control.standard.StandardControlFlowManager;
import edu.columbia.cs.psl.phosphor.instrumenter.DataAndControlFlowTagFactory;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintTagFactory;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintTrackingClassVisitor;
import edu.columbia.cs.psl.phosphor.runtime.DerivedTaintListener;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.runtime.TaintSourceWrapper;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;

public class Configuration {

    public static final int ASM_VERSION = Opcodes.ASM7;

    public static final String TAINTED_REF_OBJTAG_DESC = "Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;";
    public static final String TAINT_TAG_DESC = "Ledu/columbia/cs/psl/phosphor/runtime/Taint;";
    public static final String TAINT_TAG_INTERNAL_NAME = "edu/columbia/cs/psl/phosphor/runtime/Taint";

    public static final String TAINT_TAG_ARRAY_DESC = "Ledu/columbia/cs/psl/phosphor/struct/LazyArrayObjTags;";
    public static final String TAINT_TAG_ARRAY_INTERNAL_NAME = "edu/columbia/cs/psl/phosphor/struct/LazyArrayObjTags";
    public static final Object TAINT_TAG_STACK_TYPE = "edu/columbia/cs/psl/phosphor/runtime/Taint";
    public static final String MULTI_TAINT_HANDLER_CLASS = "edu/columbia/cs/psl/phosphor/runtime/Taint";
    public static final String TAINTED_INT_INTERNAL_NAME = "edu/columbia/cs/psl/phosphor/struct/TaintedIntWithObjTag";
    public static final String TAINTED_INT_DESC = "L" + TAINTED_INT_INTERNAL_NAME + ";";
    public static final int TAINT_LOAD_OPCODE = Opcodes.ALOAD;
    public static final int TAINT_STORE_OPCODE = Opcodes.ASTORE;
    public static final Class TAINT_TAG_OBJ_CLASS = (Taint.class);
    public static boolean SKIP_LOCAL_VARIABLE_TABLE = false;
    public static String ADDL_IGNORE = null;
    public static boolean REFERENCE_TAINTING = true;
    public static boolean DATAFLOW_TRACKING = true; //default
    public static boolean ARRAY_INDEX_TRACKING = false;
    public static boolean IMPLICIT_TRACKING = true;
    public static boolean IMPLICIT_LIGHT_TRACKING;
    public static boolean IMPLICIT_HEADERS_NO_TRACKING = false;
    public static boolean IMPLICIT_EXCEPTION_FLOW = false;
    public static boolean WITHOUT_BRANCH_NOT_TAKEN = false;
    public static boolean SINGLE_TAINT_LABEL = false;
    public static boolean ANNOTATE_LOOPS = false;
    public static boolean WITH_ENUM_BY_VAL = false;
    public static boolean WITH_UNBOX_ACMPEQ = false;
    public static boolean PREALLOC_STACK_OPS = false;
    public static boolean WITHOUT_PROPAGATION = false;
    public static boolean WITHOUT_FIELD_HIDING = false;
    public static boolean READ_AND_SAVE_BCI = false;
    public static boolean ALWAYS_CHECK_FOR_FRAMES = false;
    public static boolean REENABLE_CACHES = false;
    public static Class<? extends ClassVisitor> PRIOR_CLASS_VISITOR = null;
    public static ControlFlowManager controlFlowManager;
    public static boolean QUIET_MODE = false;

    public static Set<String> ignoredMethods = new HashSet<>();  //favtrigger: phosphor use it to manually specify methods to skip intrumentation
    /*
     * Derived configuration values
     */
    public static boolean OPT_CONSTANT_ARITHMETIC = !IMPLICIT_TRACKING && !IMPLICIT_LIGHT_TRACKING;

    public static Class<? extends TaintAdapter> extensionMethodVisitor;
    public static Class<? extends ClassVisitor> extensionClassVisitor;
    public static TaintTagFactory taintTagFactory = new DataAndControlFlowTagFactory();
    public static TaintSourceWrapper autoTainter = new TaintSourceWrapper();
    public static DerivedTaintListener derivedTaintListener = new DerivedTaintListener();
    public static boolean WITH_HEAVY_OBJ_EQUALS_HASHCODE = false;

    
    // public static boolean TAINT_THROUGH_SERIALIZATION = true;

    public static final int PROTO_MSG_ID_TAG = 1024;
    public static boolean IS_THIRD_PARTY_PROTO = true;

    public static boolean USE_FAULT_FUZZ = false; //favtrigger, default not to use record and trigger logic

    public static boolean USE_MSGID = false; //favtrigger

    public static boolean FOR_JAVA = false; //favtrigger
    
    public static boolean JDK_MSG = false; //favtrigger
    public static boolean JDK_FILE = false; //favtrigger
    
    public static boolean FOR_ZK = false; //favtrigger
    public static boolean ZK_API = false;

    public static boolean FOR_HDFS = false; //favtrigger
    public static boolean HDFS_RPC = false;
    public static boolean HDFS_API = false;

    public static boolean FOR_HBASE = false; //favtrigger
    public static boolean HBASE_RPC = false;

    public static boolean FOR_YARN = false; //favtrigger
    public static boolean YARN_RPC = false; //pass ip and msgid, but not record or generate taints
    
    
    public static boolean FOR_MR = false; //favtrigger
    public static boolean MR_RPC = false;
    
    // public static boolean ZK_CLI = false; //favtrigger
    public static boolean ASYC_TRACE = false; //favtrigger

    public static String OBSERVER_HOME;
    
    public static String CACHE_DIR = null;
    public static String FAV_RECORD_PATH = null; //favtrigger
    public static String COV_PATH = null; //favtrigger

    public static List<String> FILTER_PATHS = new ArrayList<String>();
    public static List<String> DATA_PATHS = new ArrayList<String>();

    public static String AFL_ALLOW;
    public static List<String> AFL_ALLOWLIST = new ArrayList<String>();
    public static String AFL_DENY;
    public static List<String> AFL_DENYLIST  = new ArrayList<String>();

    public static String IO_ALLOW;
    public static List<String> IO_ALLOWLIST = new ArrayList<String>();
    public static String IO_DENY;
    public static List<String> IO_DENYLIST  = new ArrayList<String>();

    public static String CONTROLLER_SOCKET = "127.0.0.1:8888";
    public static final Class TAINT_TAG_OBJ_ARRAY_CLASS = (Taint[].class); //favtrigger

    // public static boolean FAVDEBUG = false;

    // configurable
    public static long SAVE_RESULT_INTERNAL = 10000;

    // not configurable by user
    public static long TAINT_MSG_RAND_SEED = 0;

    // not stable for now
    // public static boolean REPLAY_MODE = false;
    // public static boolean REPLAY_NOW = false;

    // for annotation instrument
    public static String ANNOTATION_FILE = null;

    public enum EXEC_MODE_SET {
        // CrashFuzz,
        // Replay,
        FaultFuzz
    }

    public static EXEC_MODE_SET EXEC_MODE = EXEC_MODE_SET.FaultFuzz;

    /**
     * -1: not determined
     * 0: no send
     * 1: normal
     * 2: control
     */
    public static int DETERMINE_STATE = 2;

    public static boolean USE_INJECT_ANNOTATION = false;

    private Configuration() {
        // Prevents this class from being instantiated
    }

    public static void init() {
        if(controlFlowManager == null) {
            controlFlowManager = new StandardControlFlowManager();
        }
        OPT_CONSTANT_ARITHMETIC = !IMPLICIT_TRACKING && !IMPLICIT_LIGHT_TRACKING;
        if(IMPLICIT_TRACKING) {
            ARRAY_INDEX_TRACKING = true;
        }

        if(TaintTrackingClassVisitor.class != null && TaintTrackingClassVisitor.class.getClassLoader() != null) {
            URL r = TaintTrackingClassVisitor.class.getClassLoader().getResource("phosphor-mv");
            if(r != null) {
                try {
                    Properties props = new Properties();
                    props.load(r.openStream());
                    if(props.containsKey("extraMV")) {
                        extensionMethodVisitor = (Class<? extends TaintAdapter>) Class.forName(props.getProperty("extraMV"));
                    }
                    if(props.containsKey("extraCV")) {
                        extensionClassVisitor = (Class<? extends ClassVisitor>) Class.forName(props.getProperty("extraCV"));
                    }
                    if(props.containsKey("taintTagFactory")) {
                        taintTagFactory = (TaintTagFactory) Class.forName(props.getProperty("taintTagFactory")).newInstance();
                    }
                    if(props.containsKey("derivedTaintListener")) {
                        derivedTaintListener = (DerivedTaintListener) Class.forName(props.getProperty("derivedTaintListener")).newInstance();
                    }
                } catch(IOException ex) {
                    //fail silently
                } catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Method {
        final String name;
        final String owner;

        public Method(String name, String owner) {
            this.name = name;
            this.owner = owner;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((owner == null) ? 0 : owner.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if(obj == null) {
                return false;
            }
            if(getClass() != obj.getClass()) {
                return false;
            }
            Method other = (Method) obj;
            if(name == null) {
                if(other.name != null) {
                    return false;
                }
            } else if(!name.equals(other.name)) {
                return false;
            }
            if(owner == null) {
                return other.owner == null;
            } else {
                return owner.equals(other.owner);
            }
        }
    }
}
