package edu.iscas.tcse.favtrigger.instrumenter.annotation.inner;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.APP_FAULT_BEFORE;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_RECORD_OUT;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.FAV_GET_TIMESTAMP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintAdapter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;

public class InnerAnnotationInjectMethodVisitor extends TaintAdapter{
    private final String desc;
    private final Type returnType;
    private final String name;
    private final boolean isStatic;
    private final boolean isPublic;
    private final String owner;
    private final String ownerSuperCname;
    private final String[] ownerInterfaces;


    public InnerAnnotationInjectMethodVisitor(MethodVisitor mv, int access, String owner, String name,
            String descriptor, String signature,
            String[] exceptions, String originalDesc, NeverNullArgAnalyzerAdapter analyzer, String ownerSuperCname,
            String[] ownerInterfaces) {
        super(access, owner, name, descriptor, signature, exceptions, mv, analyzer);
        this.desc = descriptor;
        this.returnType = Type.getReturnType(desc);
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
        this.name = name;
        this.owner = owner;
        this.ownerSuperCname = ownerSuperCname;
        this.ownerInterfaces = ownerInterfaces;

        this.methodDescriptor = descriptor;
    }

    private String methodDescriptor;

    private boolean needInject = false;

    

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // TODO Auto-generated method stub
       
        if (descriptor.startsWith("Ledu/iscas/tcse/favtrigger/instrumenter/annotation/Inject;")) {
            System.out.println("InnerAnnotationInjectMethodVisitor.visitAnnotation has visited an @Inject notation");
            System.out.println(name);
            System.out.println(methodDescriptor);
            System.out.println(descriptor);
            needInject = true;
        }
        return super.visitAnnotation(descriptor, visible);
    }




    @Override
    public void visitCode() {
        // TODO Auto-generated method stub

        if (needInject && Configuration.USE_INJECT_ANNOTATION) {
            FAV_GET_RECORD_OUT.delegateVisit(mv);
            int fileOutStream = lvs.getTmpLV();
            super.visitVarInsn(Opcodes.ASTORE, fileOutStream);

            Label nullOutStream = new Label();
            org.objectweb.asm.tree.FrameNode outfn = getCurrentFrameNode();
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitJumpInsn(Opcodes.IFNULL, nullOutStream);

            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            super.visitLdcInsn(0);  //set FAV_RECORD_TAG to false, avoid dead loop
            super.visitFieldInsn(Opcodes.PUTFIELD, "java/io/FileOutputStream", TaintUtils.FAV_RECORD_TAG, "Z");

            super.visitLabel(nullOutStream);
            acceptFn(outfn);

            
            FAV_GET_TIMESTAMP.delegateVisit(mv);
            super.visitVarInsn(Opcodes.ALOAD, fileOutStream);
            String path = owner + "." + name + methodDescriptor;
            mv.visitLdcInsn(path);
            
            APP_FAULT_BEFORE.delegateVisit(mv);

            lvs.freeTmpLV(fileOutStream);
        }

        super.visitCode();


    }

    public static void instrument(byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        reader.accept(new ClassVisitor(Configuration.ASM_VERSION) {

            int classVersion;
            int classAccess;
            String className;
            String classSignature;
            String classSuperName;
            String[] classInterfaces;
            
            @Override
            public void visit(int version, int access, String name, String signature, String superName,
                    String[] interfaces) {
                // TODO Auto-generated method stub
                super.visit(version, access, name, signature, superName, interfaces);
                classVersion = version;
                classAccess = access;
                className = name;
                classSignature = signature;
                classSuperName = superName;
                classInterfaces = interfaces;
            }

            @Override
            public MethodVisitor visitMethod(int methodAccess, String methodName, String methodDescriptor, String methodSignature,
                    String[] methodExceptions) {    
                return new InnerAnnotationInjectMethodVisitor(super.visitMethod(methodAccess, methodName, methodDescriptor, methodSignature, methodExceptions), methodAccess, className, methodName, methodDescriptor, methodSignature, methodExceptions, methodDescriptor, null, classSuperName, classInterfaces);
            }
            
        }, ClassReader.SKIP_CODE);
        return;
    }

    // java -cp $inst_jar edu.iscas.tcse.favtrigger.instrumenter.InjectAnnotationIdentifier **$info_file**

    public static void identify(String classFileFolder) throws IOException {

        /*
         * check if the classFileFolder exists
         */
        File f = new File(classFileFolder);
        if (!f.exists())
            throw new FileNotFoundException(classFileFolder + " does not exist.");
        if (f.isFile()) {
            byte[] classfileBuffer = Files.readAllBytes(Paths.get(classFileFolder));
            instrument(classfileBuffer);
        } else if (f.isDirectory()) {
            File[] classFiles = f.listFiles();
            for (File classFile : classFiles) {
                identify(classFile.getAbsolutePath());

            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Test InnerAnnotationInjectMethodVisitor");
        if (args.length > 1) {
            System.out.println("Usage: java -cp $inst_jar edu.iscas.tcse.favtrigger.instrumenter.InjectAnnotationIdentifier <classfile_folder>");
            return;
        }
        String classFileFolder = args[0];  
        // byte[] classfileBuffer = Files.readAllBytes(Paths.get(classFileFolder));
        identify(classFileFolder);
    }

}
