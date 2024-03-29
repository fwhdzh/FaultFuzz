package edu.columbia.cs.psl.phosphor.instrumenter;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.BOX_IF_NECESSARY;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.COMBINE_TAGS;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.ENSURE_UNBOXED;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_PRINT_STRING;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.NEW_EMPTY_TAINT;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.Instrumenter;
import edu.columbia.cs.psl.phosphor.NativeMethodInspector;
import edu.columbia.cs.psl.phosphor.PhosphorInspector;
import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.ReferenceArrayTarget;
import edu.columbia.cs.psl.phosphor.instrumenter.asm.OffsetPreservingLabel;
import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.NativeHelper;
import edu.columbia.cs.psl.phosphor.runtime.ReflectionMasker;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyDoubleArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.LazyReferenceArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.PowerSetTree;
import edu.columbia.cs.psl.phosphor.struct.TaintedDoubleWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedLongWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedPrimitiveWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedReferenceWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.TaintedWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.columbia.cs.psl.phosphor.struct.multid.MultiDTaintedArray;
import edu.columbia.cs.psl.phosphor.struct.multid.MultiDTaintedArrayWithObjTag;
// import edu.iscas.tcse.favtrigger.instrumenter.RubbyPassingMV;

public class TaintPassingMV extends TaintAdapter implements Opcodes {
// public class TaintPassingMV extends RubbyPassingMV {

    static final String BYTE_NAME = "java/lang/Byte";
    static final String BOOLEAN_NAME = "java/lang/Boolean";
    static final String INTEGER_NAME = "java/lang/Integer";
    static final String FLOAT_NAME = "java/lang/Float";
    static final String LONG_NAME = "java/lang/Long";
    static final String CHARACTER_NAME = "java/lang/Character";
    static final String DOUBLE_NAME = "java/lang/Double";
    static final String SHORT_NAME = "java/lang/Short";
    private final int lastArg;
    private final Type[] paramTypes;
    private final Type originalMethodReturnType;
    private final Type newReturnType;
    private final String name;
    private final boolean isStatic;
    private final String owner;
    private final String descriptor;
    private final MethodVisitor passThroughMV;
    private final boolean rewriteLVDebug;
    private final boolean isLambda;
    private final boolean isObjOutputStream;
    private final ControlFlowPropagationPolicy controlFlowPolicy;
    private final List<MethodNode> wrapperMethodsToAdd;
    private final Set<Label> exceptionHandlers = new HashSet<>();
    ReferenceArrayTarget referenceArrayTarget;
    int line = 0;
    private boolean isIgnoreAllInstrumenting;
    private boolean isRawInstruction = false;
    private boolean isTaintlessArrayStore = false;
    private boolean doNotUnboxTaints;
    private boolean isAtStartOfExceptionHandler;

    private final String originalDesc;//favtrigger

    public TaintPassingMV(MethodVisitor mv, int access, String owner, String name, String descriptor, String signature,
                          String[] exceptions, String originalDesc, NeverNullArgAnalyzerAdapter analyzer,
                          MethodVisitor passThroughMV, LinkedList<MethodNode> wrapperMethodsToAdd,
                          ControlFlowPropagationPolicy controlFlowPolicy) {
       super(access, owner, name, descriptor, signature, exceptions, mv, analyzer);
    	// super(mv, access, owner, name, descriptor, signature, exceptions, originalDesc, analyzer, passThroughMV, wrapperMethodsToAdd, controlFlowPolicy);
        Configuration.taintTagFactory.instrumentationStarting(access, name, descriptor);
        this.isLambda = this.isIgnoreAllInstrumenting = owner.contains("$Lambda$");
        this.name = name;
        this.owner = owner;
        this.wrapperMethodsToAdd = wrapperMethodsToAdd;
        this.rewriteLVDebug = owner.equals("java/lang/invoke/MethodType");
        this.passThroughMV = passThroughMV;
        this.descriptor = descriptor;
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.isObjOutputStream = (owner.equals("java/io/ObjectOutputStream") && name.startsWith("writeObject0"))
                || (owner.equals("java/io/ObjectInputStream") && name.startsWith("defaultReadFields"));
        this.paramTypes = calculateParamTypes(isStatic, descriptor);
        this.lastArg = paramTypes.length - 1;
        this.originalMethodReturnType = Type.getReturnType(originalDesc);
        this.newReturnType = Type.getReturnType(descriptor);
        this.controlFlowPolicy = controlFlowPolicy;
        this.originalDesc = originalDesc;
    }

    private int tmpWriteBuffer = -1;
    private int tmpReadBuffer = -1;
    @Override
    public void visitCode() {
        super.visitCode();
        Configuration.taintTagFactory.methodEntered(owner, name, descriptor, passThroughMV, lvs, this);
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
            //SocketChannelImpl write ByteBuffer
            /* mv this to NIOTrackingMV
            FAV_NEW_MSGID.delegateVisit(mv);
            int msgId = lvs.getTmpLV();
            super.visitVarInsn(ISTORE, msgId);
            super.visitVarInsn(ILOAD, msgId);
            super.visitVarInsn(ALOAD, 2);
            super.visitVarInsn(Opcodes.ALOAD, 0); //do not contain the port info
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/nio/ch/SocketChannelImpl", "getRemoteAddress", "()Ljava/net/SocketAddress;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
            FAV_BUFFER_WITH_MSGID.delegateVisit(mv);
            tmpWriteBuffer = lvs.createPermanentLocalVariable(ByteBuffer.class, "favWrappedBuffer");
            super.visitVarInsn(ASTORE, tmpWriteBuffer);

            Label done = new Label();
            super.visitVarInsn(ALOAD, 2);
            super.visitJumpInsn(Opcodes.IFNULL, done);

            FAV_GET_RECORD_OUT.delegateVisit(mv);
            int fileOutStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

            Label nullOutStream = new Label();
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

            super.visitLabel(nullOutStream);

            FAV_GET_TIMESTAMP.delegateVisit(mv);
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            super.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(FAVPathType.FAVMSG.toString()+":");
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
            super.visitVarInsn(Opcodes.ALOAD, 0); //do not contain the port info
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/nio/ch/SocketChannelImpl", "getRemoteAddress", "()Ljava/net/SocketAddress;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitLdcInsn("&");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitVarInsn(ILOAD, msgId);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
            "()Ljava/lang/String;", false);
            super.visitVarInsn(ALOAD, 2); //aload ByteBuffer
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", TaintUtils.FAV_GETBUFFERSHADOW_MT, "()"+Type.getDescriptor(LazyByteArrayObjTags.class), false);
            super.visitTypeInsn(Opcodes.CHECKCAST,Type.getInternalName(LazyByteArrayObjTags.class));
            super.visitVarInsn(ALOAD, 2); //aload ByteBuffer
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", "position", "()I", false);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", "limit", "()I", false);
            super.visitLdcInsn(JREType.MSG.toString());
            JRE_FAV_RECORD_BYTES_OR_TRIGGER.delegateVisit(mv);

            lvs.freeTmpLV(fileOutStream);
            lvs.freeTmpLV(msgId);
            super.visitLabel(done);
            */
        }
        if (NativeMethodInspector.isSocketChannelRead(this.owner, this.name, this.descriptor)) {
            /* comment this out for TODO: zookeeper does not neet this
            super.visitVarInsn(ALOAD, 2); //aload ByteBuffer
            FAV_BUFFER_WAIT_MSGID.delegateVisit(mv);//prepare new bytebuffer for receiving bytes with message ids
            tmpReadBuffer = lvs.createPermanentLocalVariable(ByteBuffer.class, "favWrappedBuffer");
            super.visitVarInsn(ASTORE, tmpReadBuffer);
            */
        }
        if(this.owner.equals("sun/nio/ch/FileChannelImpl") && this.name.startsWith("write")) {

        }
        //favtrigger: end
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
        exceptionHandlers.add(handler);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        if(!isIgnoreAllInstrumenting && !isRawInstruction) {
            // If accessing an argument, then map it to its taint argument
            int shadowVar = var < lastArg && TaintUtils.isShadowedType(paramTypes[var]) ? var + 1 : lvs.varToShadowVar.get(var);
            controlFlowPolicy.visitingIncrement(var, shadowVar);
        }
        Configuration.taintTagFactory.iincOp(var, increment, mv, lvs, this);
        mv.visitIincInsn(var, increment);
    }

    @Override
    public void visitLabel(Label label) {
        if(!isIgnoreAllInstrumenting && Configuration.READ_AND_SAVE_BCI && label instanceof OffsetPreservingLabel) {
            Configuration.taintTagFactory.insnIndexVisited(((OffsetPreservingLabel) label).getOriginalPosition());
        }
        if(exceptionHandlers.contains(label)) {
            isAtStartOfExceptionHandler = true;
        }
        super.visitLabel(label);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
        if(isAtStartOfExceptionHandler) {
            isAtStartOfExceptionHandler = false;
            controlFlowPolicy.generateEmptyTaint(); //TODO exception reference taint is here
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if(isIgnoreAllInstrumenting) {
            super.visitVarInsn(opcode, var);
            return;
        }
        int shadowVar = getShadowVar(var, opcode);
        switch(opcode) {
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
            case Opcodes.ALOAD:
                super.visitVarInsn(opcode, var);
                super.visitVarInsn(ALOAD, shadowVar);
                if(getTopOfStackObject() == Opcodes.TOP) {
                    throw new IllegalStateException();
                }
                return;
            case Opcodes.ISTORE:
            case Opcodes.LSTORE:
            case Opcodes.FSTORE:
            case Opcodes.DSTORE:
            case Opcodes.ASTORE:
                controlFlowPolicy.visitingLocalVariableStore(opcode, var);
                /*
                //favtrigger: record the astore operation
                if(!Configuration.FOR_JAVA){
                    System.out.println("instrument ASTORE");
                    super.visitInsn(Opcodes.DUP);
                    super.visitMethodInsn(INVOKESTATIC, "edu/iscas/tcse/favtrigger/record/instrumentation/RecordTaint", "recordTaint",  "("+Configuration.TAINT_TAG_DESC+")V", false);
                }
                //end favtrigger
                */
                super.visitVarInsn(ASTORE, shadowVar);
                super.visitVarInsn(opcode, var);
        }
    }

    private int getShadowVar(int local, int opcode) {
        int shadowVar;
        if(local == 0 && !isStatic) {
            // Accessing "this" so no-op, die here so we never have to worry about uninitialized this later on.
            shadowVar = 1;
        } else if(local < lastArg && paramTypes[local] != null && TaintUtils.getShadowTaintType(paramTypes[local].getDescriptor()) != null) {
            // Accessing an arg; remap it
            shadowVar = local + 1;
            if(opcode == LLOAD || opcode == LSTORE || opcode == DSTORE || opcode == DLOAD) {
                shadowVar++;
            }
        } else {
            // Not accessing an arg
            if(!lvs.varToShadowVar.containsKey(local)) {
                lvs.varToShadowVar.put(local, lvs.newShadowLV(Type.getType(Configuration.TAINT_TAG_DESC), local));
            }
            shadowVar = lvs.varToShadowVar.get(local);
        }
        return shadowVar;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    	Type descType = Type.getType(desc);
        if(isIgnoreAllInstrumenting) {
            super.visitFieldInsn(opcode, owner, name, desc);
            return;
        }
        if(descType.getSort() == Type.ARRAY && descType.getDimensions() > 1) {
            desc = MultiDTaintedArray.getTypeForType(descType).getDescriptor();
        }
        boolean isIgnoredTaint = PhosphorInspector.isIgnoredClass(owner) || PhosphorInspector.isIgnoredClassWithStubsButNoTracking(owner);
        if(Instrumenter.isUninstrumentedField(owner, name) || isIgnoredTaint) {
            switch(opcode) {
                case GETFIELD:
                    super.visitInsn(POP);
                case GETSTATIC:
                    //need to turn into a wrapped type
                    super.visitFieldInsn(opcode, owner, name, desc);
                    if(descType.getSort() == Type.ARRAY) {
                        Type wrapperType = TaintUtils.getWrapperType(descType);
                        BOX_IF_NECESSARY.delegateVisit(mv);
                        super.visitTypeInsn(CHECKCAST, wrapperType.getInternalName());
                    } else if(desc.equals("Ljava/lang/Object;")) {
                        BOX_IF_NECESSARY.delegateVisit(mv);
                    }
                    NEW_EMPTY_TAINT.delegateVisit(mv);
                    return;
                case PUTFIELD:
                    //obj taint val taint
                    super.visitInsn(POP);
                    //obj taint val
                    if(descType.getSort() == Type.ARRAY || descType.getSort() == Type.OBJECT) {
                        ENSURE_UNBOXED.delegateVisit(mv);
                        mv.visitTypeInsn(Opcodes.CHECKCAST, descType.getInternalName());
                    }
                    if(descType.getSize() == 2) {
                        super.visitInsn(DUP2_X1);
                        super.visitInsn(POP2);
                    } else {
                        super.visitInsn(SWAP);
                    }
                    super.visitInsn(POP);
                    super.visitFieldInsn(opcode, owner, name, desc);
                    return;
                case PUTSTATIC:
                    super.visitInsn(POP);
                    if(descType.getSort() == Type.ARRAY || descType.getSort() == Type.OBJECT) {
                        ENSURE_UNBOXED.delegateVisit(mv);
                        mv.visitTypeInsn(Opcodes.CHECKCAST, descType.getInternalName());
                    }
                    super.visitFieldInsn(opcode, owner, name, desc);
                    return;
            }
        }

        boolean thisIsTracked = TaintUtils.isShadowedType(descType);

        if(opcode == PUTFIELD || opcode == PUTSTATIC) {
            controlFlowPolicy.visitingFieldStore(opcode, owner, name, desc);
        }
        Configuration.taintTagFactory.fieldOp(opcode, owner, name, desc, mv, lvs, this, thisIsTracked);
        switch(opcode) {
            case Opcodes.GETSTATIC:
                if(owner.equals("org/apache/commons/math3/util/FastMathLiteralArrays") && name.equals("LN_MANT")) {
                    //favtrigger: dong modified it to skip a hadoop too large multi-dimession array bug
                    super.visitFieldInsn(opcode, owner, name, descType.getDescriptor());
                    if(name.equals("LN_MANT")) { // 2 dimension double array
                        super.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
                        super.visitIntInsn(Opcodes.BIPUSH, Type.DOUBLE);
                        super.visitIntInsn(Opcodes.BIPUSH, 2);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArrayWithObjTag.class), "initWithEmptyTaints", "([Ljava/lang/Object;II)Ljava/lang/Object;", false);
                        super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyReferenceArrayObjTags.class));
                    } else { // 1 dimension double array
                        super.visitTypeInsn(Opcodes.CHECKCAST, "Ljava/lang/Object;");
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArrayWithObjTag.class), "boxIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                        super.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyDoubleArrayObjTags.class));
                    }
                } else if(TaintUtils.isWrappedTypeWithSeparateField(descType)) {
                    Type wrapper = TaintUtils.getWrapperType(descType);
                    super.visitLdcInsn(new ReferenceArrayTarget(desc));
                    super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_WRAPPER_FIELD, wrapper.getDescriptor());
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
                if(TaintUtils.isShadowedType(descType)) {
                    super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_FIELD, TaintUtils.getShadowTaintType(descType.getDescriptor()));
                }
                break;
            case Opcodes.GETFIELD:
                // [objectref taint1]
                super.visitInsn(SWAP);
                super.visitInsn(DUP);
                if(TaintUtils.isWrappedTypeWithSeparateField(descType)) {
                    Type wrapper = TaintUtils.getWrapperType(descType);
                    super.visitLdcInsn(new ReferenceArrayTarget(desc));
                    super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_WRAPPER_FIELD, wrapper.getDescriptor());
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
                // [taint1 objectref value]
                if(descType.getSize() == 1) {
                    super.visitInsn(DUP_X2);
                    super.visitInsn(POP);
                } else {
                    super.visitInsn(DUP2_X2);
                    super.visitInsn(POP2);
                }
                // [value taint1 objectref]
                super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_FIELD, TaintUtils.getShadowTaintType(desc));
                // [value taint1 taint2]
                controlFlowPolicy.visitingInstanceFieldLoad(owner, name, desc);
                COMBINE_TAGS.delegateVisit(mv);
                // [value taint]
                break;
            case Opcodes.PUTSTATIC:
                if(TaintUtils.isShadowedType(descType)) {
                    String shadowType = TaintUtils.getShadowTaintType(desc);
                    super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_FIELD, shadowType);
                }
                if(TaintUtils.isWrappedTypeWithSeparateField(descType)) {
                    Type wrapper = TaintUtils.getWrapperType(descType);
                    if(descType.getDimensions() == 1) {
                        super.visitInsn(DUP);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, wrapper.getInternalName(), "unwrap", "(" + wrapper.getDescriptor() + ")" + TaintUtils.getUnwrappedType(wrapper).getDescriptor(), false);
                        if(descType.getSort() != Type.OBJECT) {
                            super.visitTypeInsn(CHECKCAST, descType.getInternalName());
                        }
                        super.visitFieldInsn(opcode, owner, name, desc);
                    }
                    super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_WRAPPER_FIELD, wrapper.getDescriptor());
                } else {
                    //:
                    //favtrigger: end
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
                break;
            case Opcodes.PUTFIELD:
                // RT R V T
                if(TaintUtils.isShadowedType(descType)) {
                    String shadowType = TaintUtils.getShadowTaintType(desc);
                    if(Type.getType(desc).getSize() == 2) {
                        // R T VV T
                        int tmp = lvs.getTmpLV(Type.getType(Configuration.TAINT_TAG_DESC));
                        super.visitVarInsn(ASTORE, tmp);
                        //R T VV
                        super.visitInsn(DUP2_X2);
                        // VV R T VV
                        super.visitInsn(POP2);
                        // VV R T
                        super.visitInsn(POP);
                        //VV R
                        super.visitInsn(DUP_X2);
                        super.visitInsn(DUP_X2);
                        super.visitInsn(POP);
                        //R R VV
                        super.visitFieldInsn(opcode, owner, name, desc);
                        //R T
                        super.visitVarInsn(ALOAD, tmp);
                        super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_FIELD, shadowType);
                        lvs.freeTmpLV(tmp);
                        // System.exit(-1);
                        return;
                    } else {
                        //R T V T
                        super.visitInsn(DUP2_X2);
                        //V T R T V T
                        super.visitInsn(POP2);
                        //V T R T
                        super.visitInsn(POP); //TODO reference taint on field owner?
                        //V T R
                        super.visitInsn(DUP_X2);
                        //R V T R
                        super.visitInsn(SWAP);
                        //R V R T
                        super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_FIELD, shadowType);
                        //T R V R
                    }
                }
                if(TaintUtils.isWrappedTypeWithSeparateField(descType)) {
                    Type wrapper = TaintUtils.getWrapperType(descType);
                    if(descType.getDimensions() == 1) {
                        //Owner ArrayWrapper
                        super.visitInsn(DUP2);
                        //Owner ArrayWrapper Owner ArrayWrapper
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, wrapper.getInternalName(), "unwrap", "(" + wrapper.getDescriptor() + ")" + TaintUtils.getUnwrappedType(wrapper).getDescriptor(), false);
                        if(descType.getElementType().getSort() == Type.OBJECT) {
                            super.visitTypeInsn(CHECKCAST, descType.getInternalName());
                        }
                        super.visitFieldInsn(opcode, owner, name, desc);
                    }
                    super.visitFieldInsn(opcode, owner, name + TaintUtils.TAINT_WRAPPER_FIELD, wrapper.getDescriptor());
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        if(isIgnoreAllInstrumenting) {
            super.visitIntInsn(opcode, operand);
            return;
        }
        switch(opcode) {
            case TaintUtils.IGNORE_EVERYTHING:
                isIgnoreAllInstrumenting = true;
                break;
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                super.visitIntInsn(opcode, operand);
                /*
                //favtrigger:
                super.visitLdcInsn(this.owner);
                super.visitLdcInsn(this.name);
                super.visitLdcInsn(this.descriptor);
                super.visitLdcInsn(FAVTaintType.INT.toString());
                super.visitLdcInsn(FAVTagType.APP.toString());
                //super.visitLdcInsn("");
                super.visitLdcInsn("");

                NEW_FAV_TAINT_OR_EMPTY.delegateVisit(mv);
                //favtrigger: end
                */
                //favtrigger: comment
                controlFlowPolicy.generateEmptyTaint();
                //favtrigger: comment end
                break;
            case Opcodes.NEWARRAY:
                super.visitInsn(SWAP);
                super.visitIntInsn(opcode, operand);
                String arType = MultiDTaintedArray.getTaintArrayInternalName(operand);
                String arrayDescriptor = MultiDTaintedArray.getArrayDescriptor(operand);
                super.visitMethodInsn(INVOKESTATIC, arType, "factory", "(" + Configuration.TAINT_TAG_DESC + arrayDescriptor + ")L" + arType + ";", false);
                controlFlowPolicy.generateEmptyTaint();
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        if(isIgnoreAllInstrumenting) {
            super.visitMultiANewArrayInsn(desc, dims);
            return;
        }
        Type arrayType = Type.getType(desc);
        StringBuilder methodToCall = new StringBuilder("MULTIANEWARRAY_");
        StringBuilder descToCall = new StringBuilder("(");
        for(int i = 0; i < dims; i++) {
            descToCall.append('I');
            descToCall.append(Configuration.TAINT_TAG_DESC);
        }
        if(arrayType.getElementType().getSort() == Type.OBJECT) {
            methodToCall.append("REFERENCE");
            if(dims == arrayType.getDimensions()) {
                descToCall.append("Ljava/lang/Class;");
                super.visitLdcInsn(arrayType.getElementType());
            }
        } else {
            methodToCall.append(arrayType.getElementType().getDescriptor());
        }
        methodToCall.append('_');
        methodToCall.append(arrayType.getDimensions());
        methodToCall.append("DIMS");

        descToCall.append(")");
        descToCall.append(Type.getDescriptor(LazyReferenceArrayObjTags.class));
        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArray.class), methodToCall.toString(), descToCall.toString(), false);

        controlFlowPolicy.generateEmptyTaint(); //TODO array reference taint?
    }

    @Override
    public void visitLdcInsn(Object cst) {
        if(cst instanceof ReferenceArrayTarget) {
            this.referenceArrayTarget = (ReferenceArrayTarget) cst;
        } else if(cst instanceof PhosphorInstructionInfo) {
            controlFlowPolicy.visitingPhosphorInstructionInfo((PhosphorInstructionInfo) cst);
            super.visitLdcInsn(cst);
        } else {
            super.visitLdcInsn(cst);
            if(!isIgnoreAllInstrumenting) {
                controlFlowPolicy.generateEmptyTaint();
            }
        }
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if(isIgnoreAllInstrumenting) {
            super.visitTypeInsn(opcode, type);
            return;
        }
        switch(opcode) {
            case Opcodes.ANEWARRAY:
                if(!Configuration.WITHOUT_PROPAGATION) {
                    Type t = Type.getObjectType(type);
                    //L TL
                    super.visitInsn(SWAP);
                    if(t.getSort() == Type.ARRAY) {
                        type = TaintUtils.getWrapperType(t).getInternalName();
                    }
                    super.visitTypeInsn(opcode, type);
                    //2D arrays are just 1D arrays, not wrapped 1Darrays
                    Type arType = Type.getType(LazyReferenceArrayObjTags.class);
                    super.visitMethodInsn(INVOKESTATIC, arType.getInternalName(), "factory", "(" + Configuration.TAINT_TAG_DESC + "[Ljava/lang/Object;)" + arType.getDescriptor(), false);
                    //TODO what should we make the reference taint be here? how shoudl we use the array length?
                    controlFlowPolicy.generateEmptyTaint();
                }
                break;
            case Opcodes.NEW:
                super.visitTypeInsn(opcode, type);
                //favtrigger start
                //sometimes cannot generate empty taint here. because it will cause empty
                //LazyByteArrayObjTags.taints
                /*
                if(Configuration.FOR_JAVA){
                    controlFlowPolicy.generateEmptyTaint();
                } else {
                	super.visitLdcInsn(this.owner);
                    super.visitLdcInsn(this.name);
                    super.visitLdcInsn(this.descriptor);
                    super.visitLdcInsn(FAVTaintType.NEW.toString());
                    super.visitLdcInsn(FAVTagType.APP.toString());
                    //super.visitLdcInsn("");
                    super.visitLdcInsn(type);

                    NEW_FAV_TAINT_OR_EMPTY.delegateVisit(mv);
                }
                */
                //favtrigger end
                //favtrigger: comment
                controlFlowPolicy.generateEmptyTaint();  //favtrigger: new taint tag for a new instance
                //favtrigger: comment end
                break;
            case Opcodes.CHECKCAST:
                checkCast(type);
                break;
            case Opcodes.INSTANCEOF:
                instanceOf(type);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void instanceOf(String type) {
        // [ref taint]
        Type t = Type.getObjectType(type);
        if (TaintUtils.isWrappedType(t)) {
            if (t.getSort() == Type.ARRAY && t.getElementType().getSort() == Type.OBJECT) {
                //Need to get the underlying type
                super.visitInsn(SWAP);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(TaintUtils.class), "ensureUnboxed", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                super.visitTypeInsn(INSTANCEOF, type);
                super.visitInsn(SWAP);
                // [z taint]
                controlFlowPolicy.visitingInstanceOf();
                return;
            }
            type = TaintUtils.getWrapperType(t).getInternalName();
        }
        super.visitInsn(SWAP);
        super.visitTypeInsn(INSTANCEOF, type);
        super.visitInsn(SWAP);
        // [z taint]
        controlFlowPolicy.visitingInstanceOf();
    }

    private void checkCast(String type) {
        Type t = Type.getObjectType(type);
        super.visitInsn(SWAP);
        if(TaintUtils.isWrappedType(t)) {
            super.visitTypeInsn(Opcodes.CHECKCAST, TaintUtils.getWrapperType(t).getInternalName());
        } else {
            super.visitTypeInsn(CHECKCAST, type);
        }
        super.visitInsn(SWAP);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        boolean hasNewName = !TaintUtils.remapMethodDescAndIncludeReturnHolder(bsm.getTag() != Opcodes.H_INVOKESTATIC, desc).equals(desc);
        String newDesc = TaintUtils.remapMethodDescAndIncludeReturnHolder(bsm.getTag() != Opcodes.H_INVOKESTATIC, desc, false, true);
        boolean isPreAllocatedReturnType = TaintUtils.isPreAllocReturnType(desc);
        if(Configuration.IMPLICIT_TRACKING || Configuration.IMPLICIT_HEADERS_NO_TRACKING) {
            hasNewName = true;
            newDesc = TaintUtils.remapMethodDescAndIncludeReturnHolderNoControlStack(bsm.getTag() != Opcodes.H_INVOKESTATIC, desc, false);
        }
        if(isPreAllocatedReturnType && Type.getReturnType(desc).getSort() == Type.OBJECT) {
            //Don't change return type
            isPreAllocatedReturnType = false;
            newDesc = Type.getMethodDescriptor(Type.getReturnType(desc), Type.getArgumentTypes(newDesc));
            newDesc = newDesc.replace(Type.getDescriptor(TaintedReferenceWithObjTag.class), "");
            doNotUnboxTaints = true;
        }
        int opcode = INVOKEVIRTUAL;
        if(bsm.getTag() == Opcodes.H_INVOKESTATIC) {
            opcode = INVOKESTATIC;
        }

        if(isPreAllocatedReturnType) {
            Type t = Type.getReturnType(newDesc);
            super.visitVarInsn(ALOAD, lvs.getPreAllocatedReturnTypeVar(t));
        }
        Type origReturnType = Type.getReturnType(desc);
        Type returnType = TaintUtils.getContainerReturnType(Type.getReturnType(desc));
        if(!name.contains("<") && hasNewName) {
            name += TaintUtils.METHOD_SUFFIX;
        }
        //if you call a method and instead of passing a primitive array you pass ACONST_NULL, we need to insert another ACONST_NULL in the stack
        //for the taint for that array
        Type[] args = Type.getArgumentTypes(newDesc);
        int argsSize = 0;
        int numErasedTypes = 0;
        for(Type type : args) {
            argsSize += type.getSize();
            if(TaintUtils.isWrappedTypeWithErasedType(type)){
                numErasedTypes++;
            }
        }

        boolean isCalledOnAPrimitiveArrayType = false;
        if(opcode == INVOKEVIRTUAL) {
            if(analyzer.stack.get(analyzer.stack.size() - argsSize - 1) == null) {
                System.out.println("NULL on stack for calllee???" + analyzer.stack + " argsize " + argsSize);
            }
            Type callee = getTypeForStackType(analyzer.stack.get(analyzer.stack.size() - argsSize - 1));
            if(callee.getSort() == Type.ARRAY) {
                isCalledOnAPrimitiveArrayType = true;
            }
        }
        if (bsmArgs != null) {
            if (bsm.getName().equals("metafactory") || bsm.getName().equals("altMetafactory")) {
                //This is a lambda
                Type samMethodType = (Type) bsmArgs[0]; //The method type to be implemented by the function object at runtime
                Handle implMethod = (Handle) bsmArgs[1]; //The method type to be called. Might have more params than samMethodType, because some args can be captured from the InvokeDynamic
                Type instantiatedMethodType = (Type) bsmArgs[2]; //The signature and return type for enforcing dynamically, same as samMethodType or specialization of types

                boolean isNEW = implMethod.getTag() == Opcodes.H_NEWINVOKESPECIAL;
                boolean isVirtual = (implMethod.getTag() == Opcodes.H_INVOKEVIRTUAL) || implMethod.getTag() == Opcodes.H_INVOKESPECIAL || implMethod.getTag() == Opcodes.H_INVOKEINTERFACE;

                if (PhosphorInspector.isIgnoredClass(implMethod.getOwner())
                        || Instrumenter.isIgnoredMethod(implMethod.getOwner(), implMethod.getName(), implMethod.getDesc())
                        || !TaintUtils.remapMethodDescAndIncludeReturnHolder(isVirtual || isNEW, implMethod.getDesc()).equals(implMethod.getDesc())) {
                    //No matter what, we need to remap all of the handles

                    bsmArgs[0] = Type.getMethodType(TaintUtils.remapMethodDescAndIncludeReturnHolder(true, samMethodType.getDescriptor()));
                    bsmArgs[2] = Type.getMethodType(TaintUtils.remapMethodDescAndIncludeReturnHolder(true, instantiatedMethodType.getDescriptor(), false,false));
                    //Might need to add more erased types to samMethodType if there it's an object but on instantiated it's an array
                    StringBuilder additionalErasedTypeHolders = new StringBuilder();
                    Type[] samMethodArgs = samMethodType.getArgumentTypes();
                    Type[] instantiatedMethodArgs = instantiatedMethodType.getArgumentTypes();
                    for(int i = 0; i < samMethodArgs.length; i++){
                        if(TaintUtils.isWrappedTypeWithErasedType(samMethodArgs[i])) {
                            additionalErasedTypeHolders.append(instantiatedMethodArgs[i]);
                        }
                    }
                    if(TaintUtils.isErasedReturnType(samMethodType.getReturnType())){
                        additionalErasedTypeHolders.append(samMethodType.getReturnType());
                    }

                    boolean needToAddReturnHolder = samMethodType.getReturnType().getSort() == Type.VOID && Type.getReturnType(implMethod.getDesc()).getSort() != Type.VOID;
                    /*For the implMethodType, we might need to generate a bridge method to:
                        + Add a return holder if the return type is not used by the caller (samMethodType return is void, implMethod return is not void)
                        + Any newInvokeSpecial - assuming that we are expecting a non-void return
                        + If the implmenetation method returns a primtiive type but SAM has a non-void, we need to handle boxing it
                        + Add a reference taint for "this" if we are calling a non-static method
                     */

                    boolean needToBoxPrimitiveReturn = samMethodType.getReturnType().getSort() == Type.OBJECT && TaintUtils.isPrimitiveType(Type.getReturnType(implMethod.getDesc()));
                    LinkedList<LocalVariableNode> lvsToVisit = new LinkedList<>();
                    LabelNode start = new LabelNode(new Label());
                    LabelNode end = new LabelNode(new Label());

                    Type implReturnType = Type.getReturnType(implMethod.getDesc());
                    String wrapperImplDesc = implMethod.getDesc();
                    if (isVirtual) {
                        //Pass the receiver type as an arg, too, since we are now doing a static bridge method instead of a virtual method
                        String implOwner = implMethod.getOwner();
                        if(args.length > 0){
                            //JVM's lambda validator allows for adaptation of receiver but not other args
                            //since we will make all virtual methods into static lambdas, we better make sure it will
                            //match exactly.
                            implOwner = args[0].getInternalName();
                        }
                        wrapperImplDesc = "(L" + implOwner + ";" + wrapperImplDesc.substring(1);
                    }
                    if (needToAddReturnHolder || needToBoxPrimitiveReturn) {
                        //Need to make the wrapper return a type that the impl method wouldn't return otherwise, and add the return type to desc
                        //Or, just need to change the type from prim to ref
                        wrapperImplDesc = Type.getMethodDescriptor(samMethodType.getReturnType(), Type.getArgumentTypes(wrapperImplDesc));
                    }
                    if (isNEW) {
                        //The return type of the wrapper should be a taintedReference
                        implReturnType = Type.getObjectType(implMethod.getOwner());
                        wrapperImplDesc = Type.getMethodDescriptor(Type.getObjectType(implMethod.getOwner()), Type.getArgumentTypes(wrapperImplDesc));
                    }
                    //If the impl method has a return type that doesn't match the instantiated method type, we'll have a gross class cast exception because we are using it as an arg
                    Type instantiatedReturnType = instantiatedMethodType.getReturnType();
                    if(instantiatedReturnType.getSort() == Type.OBJECT && implReturnType.getSort() == Type.OBJECT && !implReturnType.equals(instantiatedReturnType)){
                        wrapperImplDesc = Type.getMethodDescriptor(instantiatedReturnType, Type.getArgumentTypes(wrapperImplDesc));
                    }


                    int locationOfFakeReferenceTaint = -1;
                    //Add the taint after any parameters that are being statically bound.
                    locationOfFakeReferenceTaint = Type.getArgumentTypes(desc).length;
                    wrapperImplDesc = TaintUtils.remapMethodDescAndIncludeReturnHolder(locationOfFakeReferenceTaint, wrapperImplDesc, false, false);
                    locationOfFakeReferenceTaint = locationOfFakeReferenceTaint * 2;
                    locationOfFakeReferenceTaint += numErasedTypes;

                    if(additionalErasedTypeHolders.length()> 0){
                        String newInstantiatedDesc = ((Type) bsmArgs[2]).getDescriptor();
                        newInstantiatedDesc = newInstantiatedDesc.substring(0,newInstantiatedDesc.indexOf(')')) +
                                additionalErasedTypeHolders + newInstantiatedDesc.substring(newInstantiatedDesc.indexOf(')'));
                        bsmArgs[2] = Type.getMethodType(newInstantiatedDesc);
                        wrapperImplDesc = wrapperImplDesc.substring(0, wrapperImplDesc.indexOf(')')) + additionalErasedTypeHolders + wrapperImplDesc.substring(wrapperImplDesc.indexOf(')'));
                    }

                    MethodNode wrapperMethod = new MethodNode(Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC, "invokeDynamicHelper$" + implMethod.getOwner().replace('/', '$') + "$" + implMethod.getName().replace("<", "").replace(">", "") + "$" + wrapperMethodsToAdd.size(), wrapperImplDesc, null, null);
                    bsmArgs[1] = new Handle(H_INVOKESTATIC, className, wrapperMethod.name, wrapperMethod.desc, true);

                    wrapperMethodsToAdd.add(wrapperMethod);
                    NeverNullArgAnalyzerAdapter an = new NeverNullArgAnalyzerAdapter(className, Opcodes.ACC_STATIC, wrapperMethod.name, wrapperMethod.desc, wrapperMethod);
                    GeneratorAdapter ga = new GeneratorAdapter(an, Opcodes.ACC_STATIC, wrapperMethod.name, wrapperMethod.desc);
                    Type[] newArgs = Type.getArgumentTypes(wrapperMethod.desc);

                    String descOfMethodToCall = TaintUtils.remapMethodDescAndIncludeReturnHolder(isNEW || isVirtual, implMethod.getDesc());
                    ga.visitCode();
                    //We load either all args, or all but the return type
                    if (isNEW) {
                        ga.visitTypeInsn(NEW, implMethod.getOwner());
                        ga.visitInsn(DUP);
                        NEW_EMPTY_TAINT.delegateVisit(ga);
                    }
                    if(PhosphorInspector.isIgnoredClass(implMethod.getOwner()) || Instrumenter.isIgnoredMethod(implMethod.getOwner(), implMethod.getName(), implMethod.getDesc())) {
                        //Load only unwrapped args, call uninst, box if necessary
                        int idx = 1;
                        if(isVirtual) {
                            ga.visitVarInsn(Opcodes.ALOAD, (locationOfFakeReferenceTaint == 0 ? 1 : 0));
                            idx = 3;
                        }

                        for(Type t : Type.getArgumentTypes(implMethod.getDesc())) {

                            if(t.getDescriptor().equals(Configuration.TAINT_TAG_DESC)){
                                idx++;
                                continue;
                            }
                            ga.visitVarInsn(t.getOpcode(Opcodes.ILOAD), idx);


                            if(TaintUtils.isWrappedType(t)) {
                                Type wrapper = TaintUtils.getWrapperType(t);
                                ga.visitMethodInsn(Opcodes.INVOKESTATIC, wrapper.getInternalName(), "unwrap", "(" + wrapper.getDescriptor() + ")" + TaintUtils.getUnwrappedType(wrapper).getDescriptor(), false);
                                if(TaintUtils.getUnwrappedType(wrapper).getDescriptor() != t.getDescriptor()) {
                                    ga.visitTypeInsn(Opcodes.CHECKCAST, t.getInternalName());
                                }

                                lvsToVisit.add(new LocalVariableNode("phosphorNativeWrapArg" + idx, TaintUtils.getWrapperType(t).getDescriptor(), null, start, end, idx));
                            } else {
                                lvsToVisit.add(new LocalVariableNode("phosphorNativeWrapArg" + idx, t.getDescriptor(), null, start, end, idx));
                            }
                            if (t.getDescriptor().equals("[Lsun/security/pkcs11/wrapper/CK_ATTRIBUTE;")) {
                                ga.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArray.class), "unboxCK_ATTRIBUTE", "([Lsun/security/pkcs11/wrapper/CK_ATTRIBUTE;)[Lsun/security/pkcs11/wrapper/CK_ATTRIBUTE;", false);
                            } else if (t.getDescriptor().equals("Ljava/lang/Object;") || (t.getSort() == Type.ARRAY && t.getElementType().getDescriptor().equals("Ljava/lang/Object;"))) {
                                // Need to make sure that it's not a boxed primitive array
                                ga.visitInsn(Opcodes.DUP);
                                ga.visitInsn(Opcodes.DUP);
                                Label isOK = new Label();
                                ga.visitTypeInsn(Opcodes.INSTANCEOF, "[" + Type.getDescriptor(LazyArrayObjTags.class));
                                ga.visitInsn(Opcodes.SWAP);
                                ga.visitTypeInsn(Opcodes.INSTANCEOF, Type.getInternalName(LazyArrayObjTags.class));
                                ga.visitInsn(Opcodes.IOR);
                                ga.visitJumpInsn(Opcodes.IFEQ, isOK);
                                if (className.equals("sun/misc/Unsafe")) {
                                    ga.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArrayWithObjTag.class), "unboxRawOnly1D", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                                } else if (className.equals("sun/reflect/NativeMethodAccessorImpl") && "invoke0".equals(implMethod.getName()) && Type.getType(Object.class).equals(t)) {
                                    ga.loadArg(0);
                                    ga.visitInsn(Opcodes.SWAP);
                                    ga.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArray.class), "unboxMethodReceiverIfNecessary", "(Ljava/lang/reflect/Method;Ljava/lang/Object;)Ljava/lang/Object;", false);
                                } else {
                                    ga.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArrayWithObjTag.class), "unboxRaw", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                                }
                                if (t.getSort() == Type.ARRAY) {
                                    ga.visitTypeInsn(Opcodes.CHECKCAST, t.getInternalName());
                                }
                                FrameNode fn = TaintAdapter.getCurrentFrameNode(an);
                                fn.type = Opcodes.F_NEW;
                                ga.visitLabel(isOK);
                                TaintAdapter.acceptFn(fn, ga);
                            } else if (t.getSort() == Type.ARRAY && t.getDimensions() > 1) {
                                // Need to unbox it!!
                                ga.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArrayWithObjTag.class), "unboxRaw", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                                ga.visitTypeInsn(Opcodes.CHECKCAST, t.getInternalName());
                            }
                            idx += t.getSize();
                        }
                        if (isNEW) {
                            ga.visitMethodInsn(INVOKESPECIAL, implMethod.getOwner(), "<init>", implMethod.getDesc(), false);
                        } else {
                            int opcodeToCall;
                            boolean isInterface = false;
                            switch (implMethod.getTag()) {
                                case H_INVOKESTATIC:
                                    opcodeToCall = INVOKESTATIC;
                                    break;
                                case H_INVOKEINTERFACE:
                                    opcodeToCall = INVOKEINTERFACE;
                                    isInterface = true;
                                    break;
                                case H_INVOKESPECIAL:
                                    opcodeToCall = INVOKESPECIAL;
                                    break;
                                case H_INVOKEVIRTUAL:
                                    opcodeToCall = INVOKEVIRTUAL;
                                    break;
                                default:
                                    throw new UnsupportedOperationException();
                            }
                            String nameToCall = implMethod.getName();
                            ga.visitMethodInsn(opcodeToCall, implMethod.getOwner(), nameToCall, implMethod.getDesc(), isInterface);
                        }
                        //box the return type
                        int returnIdx = -1;
                        idx = 0;
                        for(Type t : newArgs){
                            if(t.getDescriptor().startsWith("Ledu/columbia/cs/psl/phosphor/struct/Tainted")){
                                returnIdx = idx;
                                break;
                            }
                            idx += t.getSize();
                        }
                        Type origReturn = Type.getReturnType(implMethod.getDesc());
                        Type newReturn = Type.getReturnType(wrapperImplDesc);
                        if(origReturn != newReturn) {
                            if(origReturn.getSort() == Type.ARRAY && origReturn.getDimensions() > 1) {
                                Label isOK = new Label();
                                ga.visitInsn(Opcodes.DUP);
                                ga.visitJumpInsn(Opcodes.IFNULL, isOK);
                                ga.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
                                ga.visitIntInsn(Opcodes.BIPUSH, origReturn.getElementType().getSort());
                                ga.visitIntInsn(Opcodes.BIPUSH, origReturn.getDimensions());
                                ga.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArrayWithObjTag.class), "initWithEmptyTaints", "([Ljava/lang/Object;II)Ljava/lang/Object;", false);
                                FrameNode fn = TaintAdapter.getCurrentFrameNode(an);
                                fn.stack.set(fn.stack.size() - 1, "java/lang/Object");
                                ga.visitLabel(isOK);
                                fn.type = Opcodes.F_NEW;
                                TaintAdapter.acceptFn(fn, ga);
                                ga.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(LazyReferenceArrayObjTags.class));
                            } else if(origReturn.getSort() == Type.ARRAY) {
                                if(origReturn.getElementType().getSort() == Type.OBJECT) {
                                    TaintAdapter.createNewTaintArray("[Ljava/lang/Object;", an, lvs, lvs);
                                } else {
                                    TaintAdapter.createNewTaintArray(origReturn.getDescriptor(), an, lvs, lvs);
                                }
                            } else if(origReturn.getDescriptor().equals("Ljava/lang/Object;")) {
                                BOX_IF_NECESSARY.delegateVisit(ga);
                            }
                            if (origReturn.getSize() == 1) {
                                an.visitVarInsn(ALOAD, returnIdx);
                                ga.visitInsn(Opcodes.SWAP);
                                if (origReturn.getSort() == Type.OBJECT || origReturn.getSort() == Type.ARRAY) {
                                    ga.visitFieldInsn(Opcodes.PUTFIELD, newReturn.getInternalName(), "val", "Ljava/lang/Object;");
                                } else {
                                    if (newReturn.getDescriptor().equals("Ledu/columbia/cs/psl/phosphor/struct/TaintedReferenceWithObjTag;")) {
                                        ga.box(origReturn);
                                        ga.visitFieldInsn(Opcodes.PUTFIELD, newReturn.getInternalName(), "val", "Ljava/lang/Object;");
                                    } else {
                                        ga.visitFieldInsn(Opcodes.PUTFIELD, newReturn.getInternalName(), "val", origReturn.getDescriptor());
                                    }
                                }
                                an.visitVarInsn(ALOAD, returnIdx);
                                Configuration.taintTagFactory.generateEmptyTaint(ga);
                                ga.visitFieldInsn(Opcodes.PUTFIELD, newReturn.getInternalName(), "taint", Configuration.TAINT_TAG_DESC);
                                an.visitVarInsn(ALOAD, returnIdx);
                                if (origReturn.getSort() == Type.OBJECT && implMethod.getName().equals("invoke0") && className.contains("MethodAccessor")) {
                                    ga.visitInsn(Opcodes.DUP);
                                    ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, newReturn.getInternalName(), "unwrapPrimitives", "()V", false);
                                }
                            } else {
                                int retIdx = lvs.getPreAllocatedReturnTypeVar(newReturn);
                                an.visitVarInsn(ALOAD, retIdx);
                                ga.visitInsn(Opcodes.DUP_X2);
                                ga.visitInsn(Opcodes.POP);
                                ga.visitFieldInsn(Opcodes.PUTFIELD, newReturn.getInternalName(), "val", origReturn.getDescriptor());
                                an.visitVarInsn(ALOAD, retIdx);
                                Configuration.taintTagFactory.generateEmptyTaint(ga);
                                ga.visitFieldInsn(Opcodes.PUTFIELD, newReturn.getInternalName(), "taint", Configuration.TAINT_TAG_DESC);
                                an.visitVarInsn(ALOAD, retIdx);
                            }
                        } else if (origReturn.getSort() != Type.VOID && (origReturn.getDescriptor().equals("Ljava/lang/Object;") || origReturn.getDescriptor().equals("[Ljava/lang/Object;"))) {
                            //Check to see if the top of the stack is a primitive array, adn if so, box it.
                            ga.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(MultiDTaintedArrayWithObjTag.class),
                                    "boxIfNecessary", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                            if (origReturn.getSort() == Type.ARRAY) {
                                ga.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
                            }
                        }
                    } else {
                        int c = 0;
                        int nArgsLoaded = 0;
                        for (int i = 0; i < newArgs.length; i++) {
                            boolean isLastArg = newArgs[i].getDescriptor().startsWith("Ledu/columbia/cs/psl/phosphor/struct/Tainted");
                            if ((isNEW || needToBoxPrimitiveReturn) && isLastArg) {
                                /*if NEW, last arg is the return wrapper, doesn't get passed to <init>
                                similarly, if we need to use a different return wrapper, don't load it
                                */
                                break;
                            }
                            if (i != locationOfFakeReferenceTaint && !TaintUtils.isWrappedTypeWithErasedType(newArgs[i])) { //don't load the taint for a wrapper calling a static method
                                ga.visitVarInsn(newArgs[i].getOpcode(ILOAD), c);
                                nArgsLoaded++;
                            }
                            c += newArgs[i].getSize();
                            if (isLastArg) {
                                break;
                            }
                        }
                        if (isNEW) {
                            ga.visitMethodInsn(INVOKESPECIAL, implMethod.getOwner(), "<init>", descOfMethodToCall, false);
                        } else {
                            int opcodeToCall;
                            boolean isInterface = false;
                            switch (implMethod.getTag()) {
                                case H_INVOKESTATIC:
                                    opcodeToCall = INVOKESTATIC;
                                    break;
                                case H_INVOKEINTERFACE:
                                    opcodeToCall = INVOKEINTERFACE;
                                    isInterface = true;
                                    break;
                                case H_INVOKESPECIAL:
                                    opcodeToCall = INVOKESPECIAL;
                                    break;
                                case H_INVOKEVIRTUAL:
                                    opcodeToCall = INVOKEVIRTUAL;
                                    break;
                                default:
                                    throw new UnsupportedOperationException();
                            }
                            String nameToCall = implMethod.getName();
                            if (!implMethod.getDesc().equals(descOfMethodToCall)) {
                                nameToCall += TaintUtils.METHOD_SUFFIX;
                            }
                            if (needToAddReturnHolder || needToBoxPrimitiveReturn) {
                                //generate a return holder of the right kind
                                Type wrappedReturnType = Type.getReturnType(descOfMethodToCall);
                                ga.visitTypeInsn(NEW, wrappedReturnType.getInternalName());
                                ga.visitInsn(DUP);
                                ga.visitMethodInsn(INVOKESPECIAL, wrappedReturnType.getInternalName(), "<init>", "()V", false);
                                nArgsLoaded++;
                            }
                            int nArgsNeeded = Type.getArgumentTypes(descOfMethodToCall).length + (isVirtual ? 1 : 0);
                            for(int i = nArgsLoaded; i < nArgsNeeded; i++){ //handle any additional erased types
                                ga.visitInsn(ACONST_NULL);
                            }
                            ga.visitMethodInsn(opcodeToCall, implMethod.getOwner(), nameToCall, descOfMethodToCall, isInterface);
                        }
                        //Load return value
                        Type newWrapperReturnType = Type.getReturnType(wrapperMethod.desc);
                        if (newWrapperReturnType.getSort() != Type.VOID) {

                            if (needToBoxPrimitiveReturn) {
                                ga.visitVarInsn(ALOAD, c);
                                ga.visitInsn(SWAP);
                                ga.visitMethodInsn(INVOKEVIRTUAL, newWrapperReturnType.getInternalName(), "fromPrimitive", "(" + Type.getDescriptor(TaintedPrimitiveWithObjTag.class) + ")V", false);
                                ga.visitVarInsn(ALOAD, c);
                            } else if (isNEW) {
                                ga.visitVarInsn(ALOAD, c);
                                ga.visitInsn(SWAP);
                                ga.visitFieldInsn(PUTFIELD, newWrapperReturnType.getInternalName(), "val", "Ljava/lang/Object;");
                                ga.visitVarInsn(ALOAD, c);
                                ga.visitInsn(DUP);
                                NEW_EMPTY_TAINT.delegateVisit(ga);
                                ga.visitFieldInsn(PUTFIELD, newWrapperReturnType.getInternalName(), "taint", Type.getDescriptor(Taint.class));
                            }
                            //else... we are a wrapper calling a method and returning type as is
                        }
                    }
                    ga.returnValue();
                    ga.visitMaxs(0, 0);
                    ga.visitEnd();
                }
            } else {
                if(bsmArgs.length > 1 && bsmArgs[1] instanceof Handle) {
                    Type t = Type.getMethodType(((Handle) bsmArgs[1]).getDesc());
                    if(TaintUtils.isPrimitiveType(t.getReturnType())) {
                        Type _t = (Type) bsmArgs[0];
                        if(_t.getReturnType().getSort() == Type.VOID) {
                            //Manually add the return type here;
                            StringBuilder nd = new StringBuilder();
                            nd.append('(');
                            for(Type a : _t.getArgumentTypes()) {
                                nd.append(a.getDescriptor());
                            }
                            nd.append(TaintUtils.getContainerReturnType(t.getReturnType()));
                            nd.append(")V");
                            bsmArgs[0] = Type.getMethodType(nd.toString());
                        }
                        _t = (Type) bsmArgs[2];
                        if(_t.getReturnType().getSort() == Type.VOID) {
                            //Manually add the return type here;
                            StringBuilder nd = new StringBuilder();
                            nd.append('(');
                            for(Type a : _t.getArgumentTypes()) {
                                nd.append(a.getDescriptor());
                            }
                            nd.append(TaintUtils.getContainerReturnType(t.getReturnType()));
                            nd.append(")V");
                            bsmArgs[2] = Type.getMethodType(nd.toString());
                        }
                    }
                }
                for(int k = 0; k < bsmArgs.length; k++) {
                    Object o = bsmArgs[k];
                    if(o instanceof Handle) {
                        String nameH = ((Handle) o).getName();
                        boolean isVirtual = (((Handle) o).getTag() == Opcodes.H_INVOKEVIRTUAL) || ((Handle) o).getTag() == Opcodes.H_INVOKESPECIAL || ((Handle) o).getTag() == Opcodes.H_INVOKEINTERFACE;

                        if (!PhosphorInspector.isIgnoredClass(((Handle) o).getOwner()) && !PhosphorInspector.isIgnoredClassWithStubsButNoTracking(((Handle) o).getOwner()) && !Instrumenter.isIgnoredMethod(((Handle) o).getOwner(), nameH, ((Handle) o).getDesc()) &&
                                !TaintUtils.remapMethodDescAndIncludeReturnHolder(isVirtual, ((Handle) o).getDesc()).equals(((Handle) o).getDesc())) {
                            bsmArgs[k] = new Handle(((Handle) o).getTag(), ((Handle) o).getOwner(), nameH + (nameH.equals("<init>") ? "" : TaintUtils.METHOD_SUFFIX), TaintUtils.remapMethodDescAndIncludeReturnHolder(isVirtual, ((Handle) o).getDesc()), ((Handle) o).isInterface());
                        }
                    } else if (o instanceof Type) {//favtrigger: rubby methods can use here!!!!!!
                        Type t = (Type) o;
                        bsmArgs[k] = Type.getMethodType(TaintUtils.remapMethodDescAndIncludeReturnHolder(true, t.getDescriptor()));
                    }
                }
            }
        }
        if(hasNewName && !PhosphorInspector.isIgnoredClass(bsm.getOwner()) && !PhosphorInspector.isIgnoredClassWithStubsButNoTracking(bsm.getOwner())) {
            if(!Instrumenter.isIgnoredMethod(bsm.getOwner(), bsm.getName(), bsm.getDesc()) && !TaintUtils.remapMethodDescAndIncludeReturnHolder(true, bsm.getDesc()).equals(bsm.getDesc())) {
                bsm = new Handle(bsm.getTag(), bsm.getOwner(), bsm.getName() + TaintUtils.METHOD_SUFFIX, TaintUtils.remapMethodDescAndIncludeReturnHolder(true, bsm.getDesc()), bsm.isInterface());
            }
        }
        for(Type arg : Type.getArgumentTypes(newDesc)) {
            if(TaintUtils.isWrappedTypeWithErasedType(arg)) {
                super.visitInsn(ACONST_NULL);
            }
        }
        super.visitInvokeDynamicInsn(name, newDesc, bsm, bsmArgs);
        if(Type.getReturnType(newDesc).getSort() != Type.VOID) {
            NEW_EMPTY_TAINT.delegateVisit(mv);
        }
        if(isCalledOnAPrimitiveArrayType) {
            if(Type.getReturnType(desc).getSort() == Type.VOID) {
                super.visitInsn(POP);
            } else if(analyzer.stack.size() >= 2) {
                //this is so dumb that it's an array type.
                super.visitInsn(SWAP);
                super.visitInsn(POP);
            }
        }
        if(doNotUnboxTaints) {
            doNotUnboxTaints = false;
            return;
        }
        String taintType = TaintUtils.getShadowTaintType(Type.getReturnType(desc).getDescriptor());
        if(taintType != null) {
            super.visitInsn(DUP);
            if(origReturnType.getSort() == Type.OBJECT) {
                super.visitFieldInsn(GETFIELD, returnType.getInternalName(), "val", "Ljava/lang/Object;");
                super.visitTypeInsn(CHECKCAST, origReturnType.getInternalName());
            } else {
                super.visitFieldInsn(GETFIELD, returnType.getInternalName(), "val", origReturnType.getDescriptor());
            }
            if(origReturnType.getSize() == 2) {
                super.visitInsn(DUP2_X1);
                super.visitInsn(POP2);
            } else {
                super.visitInsn(SWAP);
            }
            super.visitFieldInsn(GETFIELD, returnType.getInternalName(), "taint", taintType);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        controlFlowPolicy.visitingMaxs();
        if(rewriteLVDebug) {
            Label end = new Label();
            super.visitLabel(end);
        }
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
        if(isIgnoreAllInstrumenting || isRawInstruction) {
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            return;
        }

        if(name.equals("getProperty") && this.owner.equals("org/eclipse/jdt/core/tests/util/Util")) {
            // Workaround for eclipse benchmark
            super.visitInsn(POP); //remove the taint
            owner = Type.getInternalName(ReflectionMasker.class);
            name = "getPropertyHideBootClasspath";
        }
        if(((owner.equals(INTEGER_NAME) || owner.equals(BYTE_NAME) || owner.equals(BOOLEAN_NAME) || owner.equals(CHARACTER_NAME)
                || owner.equals(SHORT_NAME) || owner.equals(LONG_NAME) || owner.equals(FLOAT_NAME) || owner.equals(DOUBLE_NAME))) && name.equals("<init>")){
            Type[] args = Type.getArgumentTypes(desc);
            if(args.length == 1 && args[0].getSort() != Type.OBJECT) {
                // [uninitThis (boxed type), thisTaint, val (primitive), valTaint]
                int primitiveSize = args[0].getSize();
                // Check that a duplicate of boxed type being initialized is actually on the stack
                if(analyzer.stack.size() >= 5 + primitiveSize
                        && analyzer.stack.get(analyzer.stack.size() - (3 + primitiveSize)) instanceof Label
                        && analyzer.stack.get(analyzer.stack.size() - (3 + primitiveSize)) ==
                        analyzer.stack.get(analyzer.stack.size() - (5 + primitiveSize))) {
                    // [uninitThis (boxed type), thisTaint, uninitThis (boxed type), thisTaint, val (primitive), valTaint]
                    int tmp1 = lvs.getTmpLV();
                    super.visitVarInsn(ASTORE, tmp1);
                    int tmp2 = lvs.getTmpLV();
                    super.visitVarInsn(args[0].getOpcode(ISTORE), tmp2);
                    //newBoxedType instanceTaint newBoxedType instanceTaint
                    super.visitInsn(POP2);
                    super.visitInsn(POP);
                    //newBoxedType
                    super.visitVarInsn(ALOAD, tmp1);
                    //newBoxedType primTaint
                    super.visitInsn(DUP2);
                    //newBoxed primTaint newBoxedPrimTaint
                    super.visitVarInsn(args[0].getOpcode(ILOAD), tmp2);
                    super.visitVarInsn(ALOAD, tmp1);
                    lvs.freeTmpLV(tmp1);
                    lvs.freeTmpLV(tmp2);
                }
            }
        }
        if(isBoxUnboxMethodToWrap(owner, name)) {
            if(name.equals("valueOf")) {
                switch(owner) {
                    case BYTE_NAME:
                        name = "valueOfB";
                        break;
                    case BOOLEAN_NAME:
                        name = "valueOfZ";
                        break;
                    case CHARACTER_NAME:
                        name = "valueOfC";
                        break;
                    case SHORT_NAME:
                        name = "valueOfS";
                        break;
                    case INTEGER_NAME:
                        name = "valueOfI";
                        break;
                    case FLOAT_NAME:
                        name = "valueOfF";
                        break;
                    case LONG_NAME:
                        name = "valueOfJ";
                        break;
                    case DOUBLE_NAME:
                        name = "valueOfD";
                        break;
                    default:
                        throw new UnsupportedOperationException(owner);
                }
            }
            if(opcode != INVOKESTATIC) {
                opcode = INVOKESTATIC;
                desc = "(L" + owner + ";" + desc.substring(1);
            }
            owner = "edu/columbia/cs/psl/phosphor/runtime/RuntimeBoxUnboxPropagator";
        }
        boolean isPreAllocatedReturnType = TaintUtils.isPreAllocReturnType(desc);
        if(PhosphorInspector.isClassWithHashMapTag(owner) && name.equals("valueOf")) {
            Type[] args = Type.getArgumentTypes(desc);
            if(args[0].getSort() != Type.OBJECT) {
                super.visitInsn(SWAP);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false);
                super.visitInsn(SWAP);
            }
            return;
        }

        Type ownerType = Type.getObjectType(owner);
        if(owner.startsWith("edu/columbia/cs/psl/phosphor") && !name.equals("printConstraints") && !name.equals("hasNoDependencies") && !desc.equals("(I)V") && !owner.endsWith("Tainter") && !owner.endsWith("CharacterUtils")
                && !name.equals("getPHOSPHOR_TAG") && !name.equals("setPHOSPHOR_TAG") && !owner.equals("edu/columbia/cs/psl/phosphor/runtime/RuntimeBoxUnboxPropagator")
                && !owner.equals(Type.getInternalName(PowerSetTree.class))
                && !owner.equals("edu/columbia/cs/psl/phosphor/util/IgnoredTestUtil")
                && !owner.equals(Configuration.TAINT_TAG_INTERNAL_NAME)
                && !owner.equals(TaintTrackingClassVisitor.CONTROL_STACK_INTERNAL_NAME)
                && !name.equals("favPHOSPHOR_TAG")) {
            Configuration.taintTagFactory.methodOp(opcode, owner, name, desc, isInterface, mv, lvs, this);
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            if(Type.getReturnType(desc).getSort() != Type.VOID) {
                NEW_EMPTY_TAINT.delegateVisit(mv);
            }
            return;
        }
        if(opcode == INVOKEVIRTUAL && TaintUtils.isWrappedType(ownerType)) {
            owner = MultiDTaintedArray.getTypeForType(ownerType).getInternalName();
        }
        if(opcode == INVOKEVIRTUAL && name.equals("clone") && desc.startsWith("()")) {
            if(owner.equals("java/lang/Object") && TaintUtils.isWrapperType(getTopOfStackType())) {
                owner = getTopOfStackType().getInternalName();
            } else {
                //TODO reference tainting - should we still pass the tag to custom clone implementations?
                //we need to do it like this for arrays either way.
                super.visitInsn(SWAP);//T A
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
                //T A
                super.visitInsn(SWAP);
                return;
            }
        }
        if(opcode == INVOKEVIRTUAL && name.equals("getClass") && desc.equals("()Ljava/lang/Class;")) {
            super.visitInsn(SWAP);
            if(isObjOutputStream) {
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            } else {
                super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(NativeHelper.class), "getClassOrWrapped", "(Ljava/lang/Object;)Ljava/lang/Class;", false);
            }
            super.visitInsn(SWAP);
            return;
        }
        if((owner.equals("java/lang/System") || owner.equals("java/lang/VMSystem") || owner.equals("java/lang/VMMemoryManager")) && name.equals("arraycopy")
                && !desc.equals("(Ljava/lang/Object;ILjava/lang/Object;IILjava/lang/DCompMarker;)V")) {
            owner = Type.getInternalName(TaintUtils.class);
        }
        if((Configuration.IMPLICIT_TRACKING || Configuration.IMPLICIT_HEADERS_NO_TRACKING) && opcode == INVOKEVIRTUAL && owner.equals("java/lang/Object") && (name.equals("equals") || name.equals("hashCode"))) {
            Type callee = getTopOfStackType();
            if(name.equals("equals")) {
                callee = getStackTypeAtOffset(1);
            }
            if(callee.getSort() == Type.OBJECT) {
                String calledOn = callee.getInternalName();
                try {
                    Class<?> in = Class.forName(calledOn.replace('/', '.'), false, TaintPassingMV.class.getClassLoader());
                    if(!in.isInterface() && !PhosphorInspector.isIgnoredClass(calledOn) && !PhosphorInspector.isIgnoredClassWithStubsButNoTracking(calledOn)) {
                        owner = calledOn;
                    }
                } catch(Throwable t) {
                    //if not ignored, can still make an invokeinterface
                    if(!PhosphorInspector.isIgnoredClass(calledOn) && !PhosphorInspector.isIgnoredClassWithStubsButNoTracking(calledOn)) {
                        owner = Type.getInternalName(TaintedWithObjTag.class);
                        opcode = INVOKEINTERFACE;
                        isInterface = true;
                    }

                }
            }
        }
        if(opcode == INVOKEVIRTUAL && Configuration.WITH_HEAVY_OBJ_EQUALS_HASHCODE && (name.equals("equals")
                || name.equals("hashCode")) && owner.equals("java/lang/Object")) {
            opcode = INVOKESTATIC;
            owner = Type.getInternalName(NativeHelper.class);
            if(name.equals("equals")) {
                desc = "(Ljava/lang/Object;Ljava/lang/Object;)Z";
            } else {
                desc = "(Ljava/lang/Object;)I";
            }
        }
        //to reduce how much we need to wrap, we will only rename methods that actually have a different descriptor
        boolean hasNewName = !TaintUtils.remapMethodDescAndIncludeReturnHolder(opcode != INVOKESTATIC, desc).equals(desc);
        if((PhosphorInspector.isIgnoredClass(owner) || PhosphorInspector.isIgnoredClassWithStubsButNoTracking(owner) || Instrumenter.isIgnoredMethod(owner, name, desc)) && !isInternalTaintingClass(owner) && !name.equals("arraycopy")) {
            Type[] args = Type.getArgumentTypes(desc);
            int argsSize = 0;
            //Remove all taints
            int[] tmp = new int[args.length];
            Type[] ts = new Type[args.length];
            for(int i = 0; i < args.length; i++) {
                Type expected = args[args.length - i - 1];
                argsSize += expected.getSize();
                Type t = getTopOfStackType();
                ts[i] = t;
                tmp[i] = lvs.getTmpLV(t);
                super.visitInsn(Opcodes.POP);
                if(TaintUtils.isWrapperType(t)) {
                    Type unwrapped = TaintUtils.getUnwrappedType(t);
                    super.visitMethodInsn(INVOKESTATIC, t.getInternalName(), "unwrap", "(" + t.getDescriptor() + ")" + unwrapped.getDescriptor(), false);
                    if(unwrapped.getDescriptor().equals("[Ljava/lang/Object;")) {
                        super.visitTypeInsn(CHECKCAST, expected.getInternalName());
                    }
                }
                super.visitVarInsn(t.getOpcode(ISTORE), tmp[i]);
            }
            if(opcode != INVOKESTATIC) {
                super.visitInsn(POP); //remove reference taint
            }
            for(int i = args.length - 1; i >= 0; i--) {
                super.visitVarInsn(ts[i].getOpcode(ILOAD), tmp[i]);
                lvs.freeTmpLV(tmp[i]);
            }
            boolean isCalledOnAPrimitiveArrayType = false;
            if(opcode == INVOKEVIRTUAL) {
                Type callee = getTypeForStackType(analyzer.stack.get(analyzer.stack.size() - argsSize - 1));
                if(callee.getSort() == Type.ARRAY) {
                    isCalledOnAPrimitiveArrayType = true;
                }
            }
            Configuration.taintTagFactory.methodOp(opcode, owner, name, desc, isInterface, mv, lvs, this);
            super.visitMethodInsn(opcode, owner, name, desc, isInterface);

            Type returnType = Type.getReturnType(desc);
            if(returnType.getDescriptor().endsWith("Ljava/lang/Object;") || returnType.getSort() == Type.ARRAY) {
                BOX_IF_NECESSARY.delegateVisit(mv);
                if(TaintUtils.isWrappedType(returnType)) {
                    super.visitTypeInsn(Opcodes.CHECKCAST, TaintUtils.getWrapperType(returnType).getInternalName());
                }
            }
            if(returnType.getSort() != Type.VOID) {
                controlFlowPolicy.generateEmptyTaint();
            }
            return;
        }
        String newDesc = TaintUtils.remapMethodDescAndIncludeReturnHolder(opcode != INVOKESTATIC, desc);
        if(Configuration.IMPLICIT_TRACKING || Configuration.IMPLICIT_HEADERS_NO_TRACKING) {
            if((isInternalTaintingClass(owner) || owner.startsWith("[")) && !name.equals("getControlFlow") && !name.startsWith("hashCode") && !name.startsWith("equals")) {
                newDesc = newDesc.replace(TaintTrackingClassVisitor.CONTROL_STACK_DESC, "");
            } else {
                super.visitVarInsn(ALOAD, lvs.getIndexOfMasterControlLV());
            }
            if(owner.startsWith("[")) {
                hasNewName = false;
            }
        }
        if(isPreAllocatedReturnType) {
            Type t = Type.getReturnType(newDesc);
            super.visitVarInsn(ALOAD, lvs.getPreAllocatedReturnTypeVar(t));
        }
        Type origReturnType = Type.getReturnType(desc);
        Type returnType = TaintUtils.getContainerReturnType(Type.getReturnType(desc));

        if(!name.contains("<") && hasNewName) {
            name += TaintUtils.METHOD_SUFFIX;
        }

        Type[] args = Type.getArgumentTypes(newDesc);
        Type[] argsInReverse = new Type[args.length];
        int argsSize = 0;

        //Before we go, push enough NULL's on the stack to account for the extra args we add to disambiguate wrappers.
        for(int i = 0; i < args.length; i++) {
            argsInReverse[args.length - i - 1] = args[i];
            argsSize += args[i].getSize();
        }
        for (Type t : Type.getArgumentTypes(desc)) {
            if (TaintUtils.isWrappedTypeWithErasedType(t)) {
                super.visitInsn(ACONST_NULL);
            }
        }
        if (TaintUtils.isErasedReturnType(origReturnType)) {
            super.visitInsn(ACONST_NULL);
        }
        boolean isCalledOnAPrimitiveArrayType = false;
        if(opcode == INVOKEVIRTUAL) {
            if(analyzer.stack.get(analyzer.stack.size() - argsSize - 1) == null) {
                System.out.println("NULL on stack for calllee???" + analyzer.stack + " argsize " + argsSize);
            }
            Type callee = getTypeForStackType(analyzer.stack.get(analyzer.stack.size() - argsSize - 1));
            if(callee.getSort() == Type.ARRAY) {
                isCalledOnAPrimitiveArrayType = true;
            }
            if(callee.getDescriptor().equals("Ljava/lang/Object;") && !owner.equals("java/lang/Object")) {
                //AALOAD can result in a type of "java/lang/Object" on the stack (if there are no stack map frames)
                //If this happened, we need to force a checkcast.
                LocalVariableNode[] tmpLVs = storeToLocals(args.length);
                super.visitTypeInsn(CHECKCAST, owner);
                for(int i = tmpLVs.length - 1; i >= 0; i--) {
                    super.visitVarInsn(Type.getType(tmpLVs[i].desc).getOpcode(ILOAD), tmpLVs[i].index);
                }
                freeLVs(tmpLVs);
            }
        }
        Configuration.taintTagFactory.methodOp(opcode, owner, name, newDesc, isInterface, mv, lvs, this);

        //favtrigger: for SocketChannelImpl
        if(NativeMethodInspector.isSocketChannelWrite(this.owner, this.name, this.descriptor) && owner.startsWith("sun/nio/ch/IOUtil") && name.equals("write"+TaintUtils.METHOD_SUFFIX)) {
            /* COMMENT this out for future work, zookeeper does not need
            int rtnHolder = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, rtnHolder);
            int dispatcherTaint = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, dispatcherTaint);
            int dispatcher = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, dispatcher);
            int longTaint = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, longTaint);
            int longV = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.LSTORE, longV);
            int bufTaint = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, bufTaint);
            super.visitInsn(POP);
            super.visitVarInsn(Opcodes.ALOAD, tmpWriteBuffer);
            super.visitVarInsn(Opcodes.ALOAD, bufTaint);
            super.visitVarInsn(Opcodes.LLOAD, longV);
            super.visitVarInsn(Opcodes.ALOAD, longTaint);
            super.visitVarInsn(Opcodes.ALOAD, dispatcher);
            super.visitVarInsn(Opcodes.ALOAD, dispatcherTaint);
            super.visitVarInsn(Opcodes.ALOAD, rtnHolder);

        	super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        	int written = lvs.getTmpLV();
        	super.visitVarInsn(Opcodes.ASTORE, written);

        	super.visitVarInsn(Opcodes.ALOAD, written);
        	super.visitVarInsn(Opcodes.ALOAD, tmpWriteBuffer);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", "position", "()I", false);
        	super.visitVarInsn(Opcodes.ALOAD, 2);
        	FAV_UPDATE_WRITE_BUFFER.delegateVisit(mv);

        	lvs.freeTmpLV(rtnHolder);
            lvs.freeTmpLV(bufTaint);
            lvs.freeTmpLV(written);
            lvs.freeTmpLV(dispatcherTaint);
            lvs.freeTmpLV(dispatcher);
            lvs.freeTmpLV(longTaint);
            lvs.freeTmpLV(longV);
            */
            super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        } else if (NativeMethodInspector.isSocketChannelRead(this.owner, this.name, this.descriptor) && owner.startsWith("sun/nio/ch/IOUtil") && name.equals("read"+TaintUtils.METHOD_SUFFIX)) {
            //SocketChannelImpl read ByteBuffer
            /* comment this out for future work, zookeeper does need this
        	int rtnHolder = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, rtnHolder);
            int dispatcherTaint = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, dispatcherTaint);
            int dispatcher = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, dispatcher);
            int longTaint = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, longTaint);
            int longV = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.LSTORE, longV);
            int bufTaint = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, bufTaint);
            super.visitInsn(Opcodes.POP);
            super.visitVarInsn(Opcodes.ALOAD, tmpReadBuffer);
            super.visitVarInsn(Opcodes.ALOAD, bufTaint);
            super.visitVarInsn(Opcodes.LLOAD, longV);
            super.visitVarInsn(Opcodes.ALOAD, longTaint);
            super.visitVarInsn(Opcodes.ALOAD, dispatcher);
            super.visitVarInsn(Opcodes.ALOAD, dispatcherTaint);
            super.visitVarInsn(Opcodes.ALOAD, rtnHolder);

            super.visitMethodInsn(opcode, owner, name, newDesc, isInterface); //call the method
            int read = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, read);

            super.visitVarInsn(Opcodes.ALOAD, read);
            super.visitVarInsn(Opcodes.ALOAD, 2);
            super.visitVarInsn(ALOAD, 2); //aload LazyByteArrayObjTags
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", TaintUtils.FAV_GETBUFFERSHADOW_MT, "()"+Type.getDescriptor(LazyByteArrayObjTags.class), false);
            super.visitTypeInsn(Opcodes.CHECKCAST,Type.getInternalName(LazyByteArrayObjTags.class));
            super.visitVarInsn(Opcodes.ALOAD, tmpReadBuffer);
            super.visitLdcInsn(this.className);
            super.visitLdcInsn(this.name);
            super.visitLdcInsn(this.originalDesc);
            super.visitLdcInsn(FAVTaintType.SCI.toString());
            super.visitLdcInsn(FAVTagType.JRE.toString());
            super.visitVarInsn(Opcodes.ALOAD, 0); //do not contain the port info
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/nio/ch/SocketChannelImpl", "getRemoteAddress", "()Ljava/net/SocketAddress;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "getHostAddress", "()Ljava/lang/String;", false);
            FAV_BUFFER_WITHOUT_MSGID.delegateVisit(mv);

            lvs.freeTmpLV(rtnHolder);
            lvs.freeTmpLV(bufTaint);
            lvs.freeTmpLV(read);
            lvs.freeTmpLV(dispatcherTaint);
            lvs.freeTmpLV(dispatcher);
            lvs.freeTmpLV(longTaint);
            lvs.freeTmpLV(longV);
            */
            super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        } else if (NativeMethodInspector.isSocketChannelWriteArray(this.owner, this.name,
                this.descriptor) && owner.startsWith("sun/nio/ch/IOUtil") &&
                name.equals("write"+TaintUtils.METHOD_SUFFIX)) {
          //SocketChannelImpl write ByteBuffer[]
            /*
            super.visitVarInsn(ALOAD, 2); //load LazyReferenceArrayObjTags that stores ByteBuffer[]
            super.visitTypeInsn(Opcodes.CHECKCAST,Type.getInternalName(LazyReferenceArrayObjTags.class));
            super.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(LazyReferenceArrayObjTags.class), "val", "[Ljava/lang/Object;");
            int bufferArray = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, bufferArray);
            super.visitVarInsn(ALOAD, bufferArray);
            super.visitInsn(Opcodes.ARRAYLENGTH);
            int length = lvs.getTmpLV(Type.INT_TYPE);
            super.visitVarInsn(ISTORE, length);

            super.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            int beforePositions = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, beforePositions);
            super.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            int limits = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, limits);
            super.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            int lazyBytesArray = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, lazyBytesArray);

            super.visitLdcInsn(0);
            super.visitVarInsn(ILOAD, length);
            //super.visitInsn(Opcodes.ICONST_0);
            Label callMethod = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, callMethod);

            super.visitLdcInsn(0);
            int index = lvs.getTmpLV(Type.INT_TYPE);
            super.visitVarInsn(ISTORE, index);
            Label checkCondition = new Label();
            Label meetCondition = new Label();
            //super.visitJumpInsn(Opcodes.GOTO, checkCondition);

            super.visitLabel(meetCondition);
            super.visitVarInsn(ALOAD, beforePositions);
            super.visitVarInsn(ALOAD, bufferArray);
            super.visitVarInsn(ILOAD, index);
            super.visitInsn(IALOAD);
            super.visitTypeInsn(Opcodes.CHECKCAST,"java/nio/ByteBuffer");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", "position", "()I", false);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", false);

            super.visitVarInsn(ALOAD, limits);
            super.visitVarInsn(ALOAD, bufferArray);
            super.visitVarInsn(ILOAD, index);
            super.visitInsn(IALOAD);
            super.visitTypeInsn(Opcodes.CHECKCAST,"java/nio/ByteBuffer");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", "limit", "()I", false);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", false);

            super.visitVarInsn(Opcodes.ALOAD, lazyBytesArray);
            super.visitVarInsn(ALOAD, bufferArray);
            super.visitVarInsn(ILOAD, index);
            super.visitInsn(IALOAD);
            super.visitTypeInsn(Opcodes.CHECKCAST,"java/nio/ByteBuffer");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", TaintUtils.FAV_GETBUFFERSHADOW_MT, "()"+Type.getDescriptor(LazyByteArrayObjTags.class), false);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", false);

            super.visitIincInsn(index, 1);

            super.visitLabel(checkCondition);
            super.visitVarInsn(ILOAD, index);
            super.visitVarInsn(ILOAD, length);
            super.visitJumpInsn(Opcodes.IF_ICMPLT, meetCondition);

            super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            super.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(PathType.FAVMSG.toString()+":");
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
            super.visitVarInsn(Opcodes.ALOAD, 0); //do not contain the port info
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/nio/ch/SocketChannelImpl", "getRemoteAddress", "()Ljava/net/SocketAddress;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "toString", "()Ljava/lang/String;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            super.visitVarInsn(Opcodes.ALOAD, lazyBytesArray);
            super.visitVarInsn(Opcodes.ALOAD, beforePositions);
            super.visitVarInsn(Opcodes.ALOAD, limits);
            FAV_TRIGGER_BYTEBUFFER_ARRAY.delegateVisit(mv);
            super.visitInsn(Opcodes.POP);

            super.visitLabel(callMethod);

            super.visitMethodInsn(opcode, owner, name, newDesc, isInterface); //call the method

            //check the size of the bytebuffer array
            super.visitLdcInsn(0);
            super.visitVarInsn(ILOAD, length);
            //super.visitInsn(Opcodes.ICONST_0);
            Label done = new Label();
            super.visitJumpInsn(Opcodes.IFEQ, done);

            //try to get the output stream to record info
            FAV_GET_RECORD_OUT.delegateVisit(mv);
            int fileOutStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

            Label nullOutStream = new Label();
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

            super.visitLabel(nullOutStream);

            super.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            int lens = lvs.getTmpLV();
            super.visitVarInsn(ASTORE, lens);

            //record info for every bytebuffer
            super.visitLdcInsn(0);
            int recIdx = lvs.getTmpLV(Type.INT_TYPE);
            super.visitVarInsn(ISTORE, recIdx);
            Label decideRecord = new Label();
            Label computeLens = new Label();

            super.visitLabel(computeLens);

            super.visitVarInsn(ALOAD, lens);
            super.visitVarInsn(ALOAD, bufferArray);
            super.visitVarInsn(ILOAD, recIdx);
            super.visitInsn(IALOAD);
            super.visitTypeInsn(Opcodes.CHECKCAST,"java/nio/ByteBuffer");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/nio/ByteBuffer", "position", "()I", false);
            super.visitVarInsn(ALOAD, beforePositions);
            super.visitVarInsn(ILOAD, recIdx);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST,"java/lang/Integer");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            super.visitInsn(ISUB);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", false);

            super.visitIincInsn(recIdx, 1);

            super.visitLabel(decideRecord);
            super.visitVarInsn(ILOAD, recIdx);
            super.visitVarInsn(ILOAD, length);
            super.visitJumpInsn(Opcodes.IF_ICMPLT, computeLens);

            FAV_GET_TIMESTAMP.delegateVisit(mv);
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            super.visitInsn(Opcodes.DUP);
            super.visitLdcInsn(PathType.FAVMSG.toString()+":");
            super.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
            super.visitVarInsn(Opcodes.ALOAD, 0); //do not contain the port info
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "sun/nio/ch/SocketChannelImpl", "getRemoteAddress", "()Ljava/net/SocketAddress;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/net/InetSocketAddress");
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetSocketAddress", "getAddress", "()Ljava/net/InetAddress;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/InetAddress", "toString", "()Ljava/lang/String;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            super.visitVarInsn(Opcodes.ALOAD, lazyBytesArray);
            super.visitVarInsn(Opcodes.ALOAD, beforePositions);
            super.visitVarInsn(Opcodes.ALOAD, lens);
            super.visitVarInsn(Opcodes.ALOAD, limits);
            FAV_RECORD_BYTEBUFFER_ARRAY.delegateVisit(mv);
            super.visitInsn(Opcodes.POP);

            super.visitLabel(done);
            lvs.freeTmpLV(recIdx);
            lvs.freeTmpLV(index);
            lvs.freeTmpLV(length);
            lvs.freeTmpLV(limits);
            lvs.freeTmpLV(bufferArray);
            lvs.freeTmpLV(beforePositions);
            lvs.freeTmpLV(fileOutStream);
            lvs.freeTmpLV(lens);
            lvs.freeTmpLV(lazyBytesArray);
            */
            //favtrigger: debug important point
            // super.visitLdcInsn("!!!!!!!!!!FAVTrigger goint to WRITE bytebuffer array! This may introduce faults!!!!!!!!!!!!!!!");
            // FAV_PRINT_STRING.delegateVisit(mv);
            super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        } else if (NativeMethodInspector.isSocketChannelReadArray(this.owner, this.name,
                this.descriptor) && owner.startsWith("sun/nio/ch/IOUtil") &&
                name.equals("read"+TaintUtils.METHOD_SUFFIX)) {
            super.visitLdcInsn("!!!!!!!!!!FAVTrigger goint to READ bytebuffer array! This may introduce faults!!!!!!!!!!!!!!!");
            FAV_PRINT_STRING.delegateVisit(mv);
            super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        } else {
        	super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        }
        //super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        //favtrigger: end

        //favtrigger: test sendRpcRequest
        /*
        if(owner.equals("org/apache/jute/OutputArchive")
    			&& name.startsWith("writeRecord")
    			) {
        	FAV_GET_RECORD_OUT.delegateVisit(mv);
            int fileOutStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

            //super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            //super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/FileOutputStream", TaintUtils.FAV_FILEOUT_NOTRECORD, "()V", false);

            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitLdcInsn("!!!OutputArchive!!!"+this.owner+"."+this.name);
        	FAV_RECORD_STRING.delegateVisit(mv);
            lvs.freeTmpLV(fileOutStream);
        }
        */
        /*
    	if(this.owner.equals("org/apache/hadoop/ipc/Client$Connection") && this.name.startsWith("sendRpcRequest")
    			&& owner.equals("org/apache/hadoop/util/ProtoUtil")
    			&& name.startsWith("makeRpcRequestHeader")
    			) {
    		FAV_GET_RECORD_OUT.delegateVisit(mv);
            int fileOutStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/FileOutputStream", TaintUtils.FAV_FILEOUT_NOTRECORD, "()V", false);

            super.visitTypeInsn(NEW, "java/io/ByteArrayOutputStream");
        	super.visitInsn(DUP);
        	super.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V", false);
        	int byteStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, byteStream);
            super.visitTypeInsn(NEW, "org/apache/hadoop/ipc/ResponseBuffer");
        	super.visitInsn(DUP);
        	super.visitMethodInsn(INVOKESPECIAL, "org/apache/hadoop/ipc/ResponseBuffer", "<init>", "()V", false);
        	int resBuffer = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, resBuffer);

            super.visitVarInsn(ALOAD, 2);
        	super.visitTypeInsn(CHECKCAST,"org/apache/hadoop/ipc/Client$Call");
        	//super.visitFieldInsn(GETFIELD, "org/apache/hadoop/ipc/Client$Call", "rpcRequestPHOSPHOR_TAG", Configuration.TAINT_TAG_DESC);
        	//super.visitFieldInsn(GETFIELD, "org/apache/hadoop/ipc/Client$Call", "idPHOSPHOR_TAG", Configuration.TAINT_TAG_DESC);
        	super.visitFieldInsn(GETFIELD, "org/apache/hadoop/ipc/Client$Call", "rpcRequest", "Lorg/apache/hadoop/io/Writable;");
        	super.visitMethodInsn(INVOKESTATIC, "org/apache/hadoop/ipc/RpcWritable", "wrap", "(Ljava/lang/Object;)Lorg/apache/hadoop/ipc/RpcWritable;", false);
        	super.visitVarInsn(Opcodes.ALOAD, resBuffer);
        	super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/ipc/RpcWritable", "writeTo", "(Lorg/apache/hadoop/ipc/ResponseBuffer;)V", false);

        	super.visitVarInsn(Opcodes.ALOAD, resBuffer);
        	super.visitVarInsn(Opcodes.ALOAD, byteStream);
        	super.visitMethodInsn(INVOKEVIRTUAL, "org/apache/hadoop/ipc/ResponseBuffer", "writeTo", "(Ljava/io/OutputStream;)V", false);

        	super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
        	super.visitVarInsn(Opcodes.ALOAD, byteStream);
        	super.visitFieldInsn(GETFIELD, "java/io/ByteArrayOutputStream", "bufPHOSPHOR_WRAPPER", Type.getDescriptor(LazyByteArrayObjTags.class));
        	super.visitFieldInsn(GETFIELD, Type.getInternalName(LazyByteArrayObjTags.class), "taints", "["+Configuration.TAINT_TAG_DESC);
        	int taints = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, taints);
            super.visitVarInsn(Opcodes.ALOAD, taints);
            super.visitLdcInsn(0);
            super.visitVarInsn(Opcodes.ALOAD, taints);
            super.visitInsn(ARRAYLENGTH);
            FAV_RECORD_TAINTS.delegateVisit(mv);

        	//super.visitLdcInsn("test");
        	//FAV_RECORD_STRING.delegateVisit(mv);
            lvs.freeTmpLV(fileOutStream);
            lvs.freeTmpLV(byteStream);
            lvs.freeTmpLV(resBuffer);
            lvs.freeTmpLV(taints);
    	}
        */
    	//favtrigger:end

        //favtrigger: comment
        //super.visitMethodInsn(opcode, owner, name, newDesc, isInterface);
        //favtrigger: comment end
        if(isCalledOnAPrimitiveArrayType) {
            if(Type.getReturnType(desc).getSort() == Type.VOID) {
                super.visitInsn(POP);
            } else if(analyzer.stack.size() >= 2) {
                //this is so dumb that it's an array type.
                super.visitInsn(SWAP);
                super.visitInsn(POP);
            }
        }
        if(isTaintlessArrayStore) {
            isTaintlessArrayStore = false;
            return;
        }
        if(doNotUnboxTaints) {
            doNotUnboxTaints = false;
            return;
        }
        String taintType = TaintUtils.getShadowTaintType(Type.getReturnType(desc).getDescriptor());
        if(taintType != null) {
            FrameNode fn = getCurrentFrameNode();
            fn.type = Opcodes.F_NEW;
            super.visitInsn(DUP);
            String taintTypeRaw = Configuration.TAINT_TAG_DESC;
            if(origReturnType.getSort() == Type.OBJECT || origReturnType.getSort() == Type.ARRAY) {
                super.visitFieldInsn(GETFIELD, returnType.getInternalName(), "val", "Ljava/lang/Object;");
                if(TaintUtils.isWrappedType(origReturnType)) {
                    super.visitTypeInsn(CHECKCAST, TaintUtils.getWrapperType(origReturnType).getInternalName());
                } else {
                    super.visitTypeInsn(CHECKCAST, origReturnType.getInternalName());
                }
            } else {
                super.visitFieldInsn(GETFIELD, returnType.getInternalName(), "val", origReturnType.getDescriptor());
            }
            if(origReturnType.getSize() == 2) {
                super.visitInsn(DUP2_X1);
                super.visitInsn(POP2);
            } else {
                super.visitInsn(SWAP);
            }
            super.visitFieldInsn(GETFIELD, returnType.getInternalName(), "taint", taintTypeRaw);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if(isLambda && OpcodesUtil.isReturnOpcode(opcode)) {
            visitLambdaReturn(opcode);
        } else if(opcode == TaintUtils.RAW_INSN) {
            isRawInstruction = !isRawInstruction;
        } else if(opcode == TaintUtils.IGNORE_EVERYTHING) {
            isIgnoreAllInstrumenting = !isIgnoreAllInstrumenting;
            Configuration.taintTagFactory.signalOp(opcode, null);
            super.visitInsn(opcode);
        } else if(opcode == TaintUtils.NO_TAINT_STORE_INSN) {
            isTaintlessArrayStore = true;
        } else if(isIgnoreAllInstrumenting || isRawInstruction || opcode == NOP || opcode == TaintUtils.FOLLOWED_BY_FRAME) {
            // if(OpcodesUtil.isReturnOpcode(opcode) && this.owner.equals("org/jruby/parser/YyTables") && this.name.startsWith("yyTable1")){
            // 	super.visitLdcInsn("FAV DEBUG yytable1 visitInsn isIgnoreAllInstrumenting here");
        	// 	super.visitMethodInsn(Opcodes.INVOKESTATIC, "edu/iscas/tcse/favtrigger/instrumenter/hbase/HBaseTrackingMV",
        	// 			"print", "(Ljava/lang/String;)V", false);
            // }
            //favtrigger: gy try to fix a phosphor error
            //e.g., TaintedReferenceWithObjTag func() {
            //        return (LazyShortArrayObjTags)MultiDTaintedArray.boxIfNecessary(var10000); //isIgnoreAllInstrumenting is true
            //      }
            //return type error
            //++
            if(OpcodesUtil.isReturnOpcode(opcode) && this.newReturnType.getInternalName().equals(Type.getInternalName(TaintedReferenceWithObjTag.class))) {
            	NEW_EMPTY_TAINT.delegateVisit(mv);
                visitReturn(opcode);
            } else {
                super.visitInsn(opcode);
            }
            //++
            //favtrigger: gy fix phosphor error end
            //favtrigger: gy fix phosphor error
            // - super.visitInsn(opcode);
        } else if(OpcodesUtil.isArrayLoad(opcode)) {
            visitArrayLoad(opcode);
        } else if(OpcodesUtil.isArrayStore(opcode)) {
            visitArrayStore(opcode);
        } else if(OpcodesUtil.isPushConstantOpcode(opcode)) {
            super.visitInsn(opcode);
            /*
            //favtrigger:
            super.visitLdcInsn(this.owner);
            super.visitLdcInsn(this.name);
            super.visitLdcInsn(this.descriptor);
            super.visitLdcInsn(FAVTaintType.CONSTANT.toString());
            super.visitLdcInsn(FAVTagType.APP.toString());
            //super.visitLdcInsn("");
            super.visitLdcInsn("");

            NEW_FAV_TAINT_OR_EMPTY.delegateVisit(mv);
            //favtrigger: end
            */
            //favtrigger: comment
            controlFlowPolicy.generateEmptyTaint();
            //favtrigger: comment end
        } else if(OpcodesUtil.isReturnOpcode(opcode)) {
            visitReturn(opcode);
        } else if(OpcodesUtil.isArithmeticOrLogicalInsn(opcode) || opcode == ARRAYLENGTH) {
            Configuration.taintTagFactory.stackOp(opcode, mv, lvs, this);
        } else if(opcode >= POP && opcode <= SWAP) {
            visitPopDupOrSwap(opcode);
        } else if(opcode == MONITORENTER || opcode == MONITOREXIT) {
            super.visitInsn(POP);
            super.visitInsn(opcode);
        } else if(opcode == ATHROW) {
            controlFlowPolicy.onMethodExit(opcode);
            super.visitInsn(POP);
            super.visitInsn(opcode);
        } else {
            throw new IllegalArgumentException("Unknown opcode: " + opcode);
        }
    }

    /**
     * @param opcode the opcode of the instruction originally to be visited either POP, POP2, DUP, DUP2, DUP_X1, DUP_X2,
     *               DUP2_X1, or SWAP
     */
    private void visitPopDupOrSwap(int opcode) {
        switch(opcode) {
            case POP:
                super.visitInsn(POP2);
                break;
            case POP2:
                super.visitInsn(POP);
                if(getTopOfStackType().getSize() != 2) {
                    super.visitInsn(POP);
                }
                super.visitInsn(POP2);
                break;
            case DUP:
                super.visitInsn(Opcodes.DUP2);
                break;
            case DUP2:
                Object topOfStack = analyzer.stack.get(analyzer.stack.size() - 2);
                //0 1 -> 0 1 2 3
                if(getStackElementSize(topOfStack) == 1) {
                    DUPN_XU(4, 0);
                } else {
                    // DUP2, top of stack is double
                    //VVT -> VVT VVT
                    int top = lvs.getTmpLV();
                    super.visitInsn(DUP_X2);
                    //TVVT
                    super.visitInsn(TaintUtils.IS_TMP_STORE);
                    super.visitVarInsn(ASTORE, top);
                    //TVV
                    super.visitInsn(DUP2_X1);
                    //VVTVV
                    super.visitVarInsn(ALOAD, top);
                    // VVT VVT
                    lvs.freeTmpLV(top);
                }
                break;
            case DUP_X1:
                super.visitInsn(DUP2_X2);
                break;
            case DUP_X2:
                //X?X? VT -> VTXX?VT
                if(getStackElementSize(analyzer.stack.get(analyzer.stack.size() - 4)) == 2) {
                    // With long/double under
                    //XXT VT -> VT XXTVT
                    DUPN_XU(2, 2); //fixed
                } else {
                    // With 1word under
                    DUPN_XU(2, 4);
                }
                break;
            case DUP2_X1:
                //ATBTCT -> BTCTATBTCT (0 1 2 3 4)
                topOfStack = analyzer.stack.get(analyzer.stack.size() - 2);
                if(getStackElementSize(topOfStack) == 1) {
                    //ATBTCT -> BTCTATBTCT
                    DUPN_XU(4, 2);
                } else {
                    //ATBBT -> BBTATBBT
                    DUPN_XU(2, 2);
                }
                break;
            case DUP2_X2:
                topOfStack = analyzer.stack.get(analyzer.stack.size() - 2);
                if(getStackElementSize(topOfStack) == 1) {
                    //Second must be a single word
                    //??TBTCT ->
                    Object third = analyzer.stack.get(analyzer.stack.size() - 6);
                    if(getStackElementSize(third) == 1) {
                        //ATBTCTDT -> CTDTATBTCTDT
                        DUPN_XU(4, 4);
                    } else {
                        //Two single words above a tainted double word
                        //AATBTCT -> BTCTAATBTCT
                        DUPN_XU(4, 3);
                    }
                } else {
                    //Top is 2 words
                    //??TBBT ->
                    Object third = analyzer.stack.get(analyzer.stack.size() - 5);
                    if(getStackElementSize(third) == 1) {
                        //ATBTCCT -> CCTATBTCCT
                        DUPN_XU(2, 4);
                    } else {
                        DUPN_XU(2, 2);
                    }
                }
                break;
            case Opcodes.SWAP:
                super.visitInsn(DUP2_X2);
                super.visitInsn(POP2);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * stack_pre = [value] or [] if opcode is RETURN
     * stack_post = []
     *
     * @param opcode the opcode of the instruction originally to be visited either RETURN, ARETURN, IRETURN, DRETURN,
     *               FRETURN, or LRETURN
     */
    private void visitLambdaReturn(int opcode) {
        // Do we need to box?
        if(newReturnType.getDescriptor().contains("edu/columbia/cs/psl/phosphor/struct")) {
            //Probably need to box...
            int returnHolder = lastArg - 1;
            super.visitVarInsn(ALOAD, returnHolder);
            if(opcode == LRETURN || opcode == DRETURN) {
                super.visitInsn(DUP_X2);
                super.visitInsn(POP);
            } else {
                super.visitInsn(SWAP);
            }
            String valDesc = opcode == ARETURN ? "Ljava/lang/Object;" : originalMethodReturnType.getDescriptor();
            super.visitFieldInsn(PUTFIELD, newReturnType.getInternalName(), "val", valDesc);
            super.visitVarInsn(ALOAD, returnHolder);
            super.visitInsn(DUP);
            NEW_EMPTY_TAINT.delegateVisit(mv);
            super.visitFieldInsn(PUTFIELD, newReturnType.getInternalName(), "taint", Configuration.TAINT_TAG_DESC);
            super.visitInsn(ARETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    /**
     * stack_pre = [value taint] or [] if opcode is RETURN
     * stack_post = []
     *
     * @param opcode the opcode of the instruction originally to be visited either RETURN, ARETURN, IRETURN, DRETURN,
     *               FRETURN, or LRETURN
     */
    private void visitReturn(int opcode) {
        controlFlowPolicy.onMethodExit(opcode);
        if(opcode == RETURN) {
            Configuration.taintTagFactory.stackOp(opcode, mv, lvs, this);
            super.visitInsn(opcode);
            return;
        }
        int retIdx = lvs.getPreAllocatedReturnTypeVar(newReturnType);
        super.visitVarInsn(ALOAD, retIdx);
        super.visitInsn(SWAP);
        super.visitFieldInsn(PUTFIELD, newReturnType.getInternalName(), "taint", Configuration.TAINT_TAG_DESC);
        super.visitVarInsn(ALOAD, retIdx);
        if(opcode == DRETURN || opcode == LRETURN) {
            super.visitInsn(DUP_X2);
            super.visitInsn(POP);
        } else {
            super.visitInsn(SWAP);
        }
        String valDesc = opcode == ARETURN ? "Ljava/lang/Object;" : originalMethodReturnType.getDescriptor();
        super.visitFieldInsn(PUTFIELD, newReturnType.getInternalName(), "val", valDesc);
        super.visitVarInsn(ALOAD, retIdx);
        Configuration.taintTagFactory.stackOp(opcode, mv, lvs, this);
        super.visitInsn(ARETURN);
    }

    /**
     * stack_pre = [arrayref, reference-taint, index, index-taint, value, value-taint]
     * stack_post = []
     *
     * @param opcode the opcode of the instruction originally to be visited either IASTORE, LASTORE,
     *               FASTORE,DASTORE, BASTORE, CASTORE, SASTORE, or AASTORE.
     */
    private void visitArrayStore(int opcode) {
        // A T I T V T
        controlFlowPolicy.visitingArrayStore(opcode);
        int valuePosition = analyzer.stack.size() - (opcode == LASTORE || opcode == DASTORE ? 3 : 2);
        int indexPosition = valuePosition - 2;
        int arrayRefPosition = indexPosition - 2;
        MethodRecord setMethod;
        if(analyzer.stack.get(arrayRefPosition) == Opcodes.NULL) {
            setMethod = TaintMethodRecord.getTaintedArrayRecord(opcode, "");
        } else {
            String arrayReferenceType = (String) analyzer.stack.get(arrayRefPosition);
            if(arrayReferenceType.startsWith("[") || !arrayReferenceType.contains("Lazy")) {
                throw new IllegalStateException("Calling XASTORE on " + arrayReferenceType);
            }
            setMethod = TaintMethodRecord.getTaintedArrayRecord(opcode, arrayReferenceType);
        }
        setMethod.delegateVisit(mv);
        isTaintlessArrayStore = false;
    }

    /**
     * stack_pre = [arrayref, reference-taint, index, index-taint]
     * stack_post = [value, value-taint]
     *
     * @param opcode the opcode of the instruction originally to be visited. This opcode is either LALOAD, DALOAD,
     *               IALOAD, FALOAD, BALOAD, CALOAD, SALOAD, or AALOAD.
     */
    private void visitArrayLoad(int opcode) {
        LocalVariableNode[] d = storeToLocals(3);
        loadLV(2, d);
        loadLV(1, d);
        loadLV(0, d);
        int arrayRefPosition = analyzer.stack.size() - 4;
        MethodRecord getMethod;
        if(analyzer.stack.get(arrayRefPosition) == Opcodes.NULL) {
            getMethod = TaintMethodRecord.getTaintedArrayRecord(opcode, "");
        } else {
            String arrayReferenceType = (String) analyzer.stack.get(arrayRefPosition);
            if(arrayReferenceType.startsWith("[") || !arrayReferenceType.contains("Lazy")) {
                throw new IllegalStateException("Calling XALOAD on " + arrayReferenceType);
            }
            getMethod = TaintMethodRecord.getTaintedArrayRecord(opcode, arrayReferenceType);
        }
        int preAllocated = lvs.getPreAllocatedReturnTypeVar(Type.getType(getMethod.getReturnType()));
        super.visitVarInsn(ALOAD, preAllocated);
        getMethod.delegateVisit(mv);
        unwrap(getMethod.getReturnType());
        // [value, value-taint]
        loadLV(2, d);
        freeLVs(d);
        super.visitInsn(SWAP);
        // [value, reference-taint, value-taint]
        controlFlowPolicy.visitingArrayLoad(opcode);
        COMBINE_TAGS.delegateVisit(mv);
    }

    /**
     * stack_pre = [TaintedPrimitiveWithObjTag]
     * stack_post = [value value-taint]
     *
     * @param wrapperType the type of the TaintedPrimitiveWithObjTag instance to be unwrapped
     */
    private void unwrap(Class<?> wrapperType) {
        super.visitInsn(DUP);
        String wrapperName = Type.getInternalName(wrapperType);
        String valueType = Type.getDescriptor(TaintUtils.getUnwrappedClass(wrapperType));
        super.visitFieldInsn(GETFIELD, wrapperName, "val", valueType);
        if(TaintedReferenceWithObjTag.class.equals(wrapperType) && referenceArrayTarget != null) {
            Type originalArrayType = Type.getType(referenceArrayTarget.getOriginalArrayType());
            String castTo = Type.getType(originalArrayType.getDescriptor().substring(1)).getInternalName();
            if(originalArrayType.getDimensions() == 2) {
                castTo = TaintUtils.getWrapperType(Type.getType(castTo)).getInternalName();
            } else if(originalArrayType.getDimensions() > 2) {
                castTo = Type.getInternalName(LazyReferenceArrayObjTags.class);
            }
            super.visitTypeInsn(CHECKCAST, castTo);
        }
        if(TaintedLongWithObjTag.class.equals(wrapperType) || TaintedDoubleWithObjTag.class.equals(wrapperType)) {
            super.visitInsn(DUP2_X1);
            super.visitInsn(POP2);
        } else {
            super.visitInsn(SWAP);
        }
        super.visitFieldInsn(GETFIELD, wrapperName, "taint", Configuration.TAINT_TAG_DESC);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label[] labels) {
        if(!isIgnoreAllInstrumenting) {
            Configuration.taintTagFactory.tableSwitch(min, max, defaultLabel, labels, mv, lvs, this);
            controlFlowPolicy.visitTableSwitch(min, max, defaultLabel, labels);
        }
        super.visitTableSwitchInsn(min, max, defaultLabel, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label[] labels) {
        if(!isIgnoreAllInstrumenting) {
            Configuration.taintTagFactory.lookupSwitch(defaultLabel, keys, labels, mv, lvs, this);
            controlFlowPolicy.visitLookupSwitch(defaultLabel, keys, labels);
        }
        super.visitLookupSwitchInsn(defaultLabel, keys, labels);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if(!isIgnoreAllInstrumenting) {
            unboxForReferenceCompare(opcode);
            Configuration.taintTagFactory.jumpOp(opcode, label, mv, lvs, this);
            controlFlowPolicy.visitingJump(opcode, label);
        }
        super.visitJumpInsn(opcode, label);
    }

    private void unboxForReferenceCompare(int opcode) {
        if((opcode == IF_ACMPEQ || opcode == IF_ACMPNE) && Configuration.WITH_UNBOX_ACMPEQ
                && !owner.equals("java/io/ObjectOutputStream$HandleTable")) {
            // v1 t1 v2 t2
            super.visitInsn(SWAP);
            ENSURE_UNBOXED.delegateVisit(mv);
            super.visitInsn(SWAP);
            // v1 t1 v2* t2
            super.visitInsn(DUP2_X2);
            // v2* t2 v1 t1 v2* t2
            super.visitInsn(POP2);
            // v2* t2 v1 t1
            super.visitInsn(SWAP);
            ENSURE_UNBOXED.delegateVisit(mv);
            super.visitInsn(SWAP);
            // v2* t2 v1* t1
            super.visitInsn(DUP2_X2);
            // v1* t1 v2* t2 v1* t1
            super.visitInsn(POP2);
            // v1* t1 v2* t2
        }
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        this.line = line;
        Configuration.taintTagFactory.lineNumberVisited(line);
    }

    /**
     * Returns whether a class with the specified name is used by Phosphor for "internal" tainting. Calls to methods in
     * internal tainting classes from instrumented classes are remapped to the appropriate "$$PHOSPHORTAGGED" version
     * even if the internal tainting class is not instrumented by Phosphor. This requires internal tainting classes to
     * provide instrumented versions of any method that may be invoked by a classes that is instrumented by Phosphor.
     *
     * @param owner the name of class being checking
     * @return true if a class with the specified name is used by Phosphor for internal tainting
     * @see MultiTainter
     */
    private static boolean isInternalTaintingClass(String owner) {
        return owner.startsWith("edu/columbia/cs/psl/phosphor/runtime/")
                || Configuration.taintTagFactory.isInternalTaintingClass(owner)
                || owner.startsWith("edu/gmu/swe/phosphor/ignored/runtime/");
    }

    private static boolean isBoxUnboxMethodToWrap(String owner, String name) {
        if((owner.equals(INTEGER_NAME) || owner.equals(BYTE_NAME) || owner.equals(BOOLEAN_NAME) || owner.equals(CHARACTER_NAME)
                || owner.equals(SHORT_NAME) || owner.equals(LONG_NAME) || owner.equals(FLOAT_NAME) || owner.equals(DOUBLE_NAME))
                && (name.equals("toString") || name.equals("toHexString") || name.equals("toOctalString") || name.equals("toBinaryString")
                || name.equals("toUnsignedString") || name.equals("valueOf"))) {
            return true;
        }
        switch(owner) {
            case BOOLEAN_NAME:
                return name.equals("parseBoolean") || name.equals("booleanValue");
            case BYTE_NAME:
                return name.equals("parseByte") || name.equals("byteValue");
            case CHARACTER_NAME:
                return name.equals("digit") || name.equals("forDigit") || name.equals("charValue");
            case DOUBLE_NAME:
                return name.equals("parseDouble") || name.equals("doubleValue");
            case FLOAT_NAME:
                return name.equals("parseFloat") || name.equals("floatValue");
            case INTEGER_NAME:
                return name.equals("getChars") || name.equals("parseInt") || name.equals("parseUnsignedInt") || name.equals("intValue");
            case LONG_NAME:
                return name.equals("getChars") || name.equals("parseLong") || name.equals("parseUnsignedLong") || name.equals("longValue");
            case SHORT_NAME:
                return name.equals("parseShort") || name.equals("shortValue");
            default:
                return false;
        }
    }

    public static Type[] calculateParamTypes(boolean isStatic, String descriptor) {
        Type[] newArgTypes = Type.getArgumentTypes(descriptor);
        int lastArg = isStatic ? 0 : 1; // If non-static, then arg[0] = this
        for(Type t : newArgTypes) {
            lastArg += t.getSize();
        }
        Type[] paramTypes = new Type[lastArg + 1];
        int n = (isStatic ? 0 : 1);
        for(Type newArgType : newArgTypes) {
            paramTypes[n] = newArgType;
            n += newArgType.getSize();
        }
        return paramTypes;
    }
}