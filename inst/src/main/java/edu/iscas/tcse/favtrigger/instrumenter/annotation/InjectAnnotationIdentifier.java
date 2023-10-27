package edu.iscas.tcse.favtrigger.instrumenter.annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import com.alibaba.fastjson.JSONObject;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.iscas.tcse.favtrigger.MyLogger;

public class InjectAnnotationIdentifier {

    public static List<AnnotatedMethodInfo> annotatedMethodList = new ArrayList<>();

    public class AnnotatedMethodInfo {

        public int classVersion;
        public int classAccess;
        public String className;
        public String classSignature;
        public String classSuperName;
        public String[] classInterfaces;

        public int methodAccess;
        public String methodName;
        public String methodDescriptor;
        public String methodSignature;
        public String[] methodExceptions;

        public AnnotatedMethodInfo(int classVersion, int classAccess, String className, String classSignature,
                String classSuperName, String[] classInterfaces, int methodAccess, String methodName,
                String methodDescriptor, String methodSignature, String[] methodExceptions) {
            this.classVersion = classVersion;
            this.classAccess = classAccess;
            this.className = className;
            this.classSignature = classSignature;
            this.classSuperName = classSuperName;
            this.classInterfaces = classInterfaces;
            this.methodAccess = methodAccess;
            this.methodName = methodName;
            this.methodDescriptor = methodDescriptor;
            this.methodSignature = methodSignature;
            this.methodExceptions = methodExceptions;
        }
    }
    
    public static boolean match(String className, String methodName, String methodDescriptor) {
        for (AnnotatedMethodInfo info : annotatedMethodList) {
            if (info.className.equals(className) && info.methodName.equals(methodName) && info.methodDescriptor.equals(methodDescriptor)) {
                return true;
            }
        }
        return false;
    }
    
    public List<AnnotatedMethodInfo> identify(byte[] classfileBuffer) {
        List<AnnotatedMethodInfo> result = new ArrayList<>();
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
                return new MethodVisitor(Configuration.ASM_VERSION) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean annotationVisible) {
                        System.out.println("visitAnnotation: " + annotationDescriptor);
                        if (annotationDescriptor.equals("Ledu/iscas/tcse/favtrigger/instrumenter/Inject;")) {
                            result.add(new AnnotatedMethodInfo(classVersion, classAccess, className, classSignature, classSuperName, classInterfaces, methodAccess, methodName, methodDescriptor, methodSignature, methodExceptions));
                            System.out.println("access: " + methodAccess + ", name: " + methodName + ", descriptor: " + methodDescriptor + ", signature: " + methodSignature);
                        }
                        return super.visitAnnotation(annotationDescriptor, annotationVisible);
                    }
                };
            }
            
        }, ClassReader.SKIP_CODE);
        return result;
    }

    public List<AnnotatedMethodInfo> identify(String classFileFolder) throws IOException {
        List<AnnotatedMethodInfo> result = new ArrayList<>();
        /*
         * check if the classFileFolder exists
         */
        File f = new File(classFileFolder);
        if (!f.exists()) throw new FileNotFoundException(classFileFolder + " does not exist.");
        /*
         * Indentify all the annotated methods in the file or the files in the folder.
         */
        if (f.isFile()) {
            byte[] classfileBuffer = Files.readAllBytes(Paths.get(classFileFolder));
            List<AnnotatedMethodInfo> classIndentifyResult = identify(classfileBuffer);
            result.addAll(classIndentifyResult);
        } else if (f.isDirectory()) {
            File[] classFiles = f.listFiles();
            for (File classFile : classFiles) {
                List<AnnotatedMethodInfo> classIndentifyResult = identify(classFile.getAbsolutePath());
                result.addAll(classIndentifyResult);
            }
        }
        return result;
    }

    public static void recordAnnotatedMethod(String informationStoreFile) throws IOException{
        String annotatedMethodListJson = JSONObject.toJSONString(annotatedMethodList);
        Files.write(Paths.get(informationStoreFile), annotatedMethodListJson.getBytes());
    }

    public static void retrieveAnnotatedMethod(String informationStoreFile) throws IOException{
        MyLogger.log("Begin to retrieve annotated method information from " + informationStoreFile + "...");
        String annotatedMethodListJson = new String(Files.readAllBytes(Paths.get(informationStoreFile)));
        annotatedMethodList = JSONObject.parseArray(annotatedMethodListJson, AnnotatedMethodInfo.class);
        MyLogger.log("Retrieved annotated method information: " + JSONObject.toJSONString(annotatedMethodList));
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");
        if (args.length > 2) {
            System.out.println("Usage: java -cp $inst_jar edu.iscas.tcse.favtrigger.instrumenter.InjectAnnotationIdentifier $class_dir $info_file");
            return;
        }
        String classFileFolder = args[0];  
        InjectAnnotationIdentifier identifier = new InjectAnnotationIdentifier();
        annotatedMethodList = identifier.identify(classFileFolder);
        /*
         * Write the annotated method information to a file. Override the file if it exists.
         */
        String informationStoreFile = args[1];
        recordAnnotatedMethod(informationStoreFile);
        annotatedMethodList.clear();
        retrieveAnnotatedMethod(informationStoreFile);
        System.out.println("Retrieved annotated method information: " + JSONObject.toJSONString(annotatedMethodList));
        System.out.println("Done!");
    }
}
