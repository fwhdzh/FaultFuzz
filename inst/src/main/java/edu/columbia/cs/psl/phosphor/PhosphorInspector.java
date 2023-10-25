package edu.columbia.cs.psl.phosphor;

import edu.columbia.cs.psl.phosphor.instrumenter.TaintTrackingClassVisitor;
import edu.columbia.cs.psl.phosphor.runtime.StringUtils;

public class PhosphorInspector {

    public static boolean isCollection(String internalName) {
        try {
            Class<?> c;
            if(TaintTrackingClassVisitor.IS_RUNTIME_INST && !internalName.startsWith("java/")) {
                return false;
            }
            c = Class.forName(internalName.replace("/", "."), false, Instrumenter.loader);
            if(java.util.Collection.class.isAssignableFrom(c)) {
                return true;
            }
        } catch(Throwable ex) {
            //
        }
        return false;
    }

    public static boolean isClassWithHashMapTag(String clazz) {
        return clazz.startsWith("java/lang/Boolean")
                || clazz.startsWith("java/lang/Character")
                || clazz.startsWith("java/lang/Byte")
                || clazz.startsWith("java/lang/Short");
    }

    public static boolean isIgnoredClassWithStubsButNoTracking(String owner) {
        return (StringUtils.startsWith(owner, "java/lang/invoke/MethodHandle")  && !"java/lang/invoke/MethodHandleImpl$Intrinsic".equals(owner))
                || (StringUtils.startsWith(owner, "java/lang/invoke/BoundMethodHandle") && !StringUtils.startsWith(owner, "java/lang/invoke/BoundMethodHandle$Factory"))
                || StringUtils.startsWith(owner, "java/lang/invoke/DelegatingMethodHandle")
                || owner.equals("java/lang/invoke/DirectMethodHandle");
    }

    public static boolean isIgnoredClass(String owner) {
        return Configuration.taintTagFactory.isIgnoredClass(owner)
                || (Configuration.ADDL_IGNORE != null && StringUtils.startsWith(owner, Configuration.ADDL_IGNORE))
                || StringUtils.startsWith(owner, "java/lang/Object")
                || StringUtils.startsWith(owner, "java/lang/Boolean")
                || StringUtils.startsWith(owner, "java/lang/Character")
                || StringUtils.startsWith(owner, "java/lang/Byte")
                || StringUtils.startsWith(owner, "java/lang/Short")
                || StringUtils.startsWith(owner, "org/jikesrvm")
                || StringUtils.startsWith(owner, "com/ibm/tuningfork")
                || StringUtils.startsWith(owner, "org/mmtk")
                || StringUtils.startsWith(owner, "org/vmmagic")
                || StringUtils.startsWith(owner, "java/lang/Number")
                || StringUtils.startsWith(owner, "java/lang/Comparable")
                || StringUtils.startsWith(owner, "java/lang/ref/SoftReference")
                || StringUtils.startsWith(owner, "java/lang/ref/Reference")
                // || StringUtils.startsWith(owner, "java/awt/image/BufferedImage")
                // || owner.equals("java/awt/Image")
                || StringUtils.startsWith(owner, "edu/columbia/cs/psl/phosphor")
                || StringUtils.startsWith(owner, "edu/iscas/tcse/favtrigger") //favtrigger: ignore favtrigger
                || StringUtils.startsWith(owner, "javafl") //favtrigger
                //|| StringUtils.startsWith(owner, "com/sun/jna") //favtrigger: ignore the third party libary JNA (java native access) for cassandra
                //|| StringUtils.startsWith(owner, "org/github/jamm") //favtrigger: ignore another agent jamm-0.3.0.jar for cassandra
                || StringUtils.startsWith(owner, "edu/gmu/swe/phosphor/ignored")
                || StringUtils.startsWith(owner, "sun/awt/image/codec/")
                || StringUtils.startsWith(owner, "com/sun/image/codec/")
                || StringUtils.startsWith(owner, "sun/reflect/Reflection") //was on last
                || owner.equals("java/lang/reflect/Proxy") //was on last
                || StringUtils.startsWith(owner, "sun/reflection/annotation/AnnotationParser") //was on last
                || StringUtils.startsWith(owner, "sun/reflect/MethodAccessor") //was on last
                || StringUtils.startsWith(owner, "org/apache/jasper/runtime/JspSourceDependent")
                || StringUtils.startsWith(owner, "sun/reflect/ConstructorAccessor") //was on last
                || StringUtils.startsWith(owner, "sun/reflect/SerializationConstructorAccessor")
                || StringUtils.startsWith(owner, "sun/reflect/GeneratedMethodAccessor")
                || StringUtils.startsWith(owner, "sun/reflect/GeneratedConstructorAccessor")
                || StringUtils.startsWith(owner, "sun/reflect/GeneratedSerializationConstructor")
                || StringUtils.startsWith(owner, "sun/awt/image/codec/")
                || StringUtils.startsWith(owner, "java/lang/invoke/LambdaForm")
                || StringUtils.startsWith(owner, "java/lang/invoke/LambdaMetafactory")
                || StringUtils.startsWith(owner, "edu/columbia/cs/psl/phosphor/struct/TaintedWith")
                || StringUtils.startsWith(owner, "java/util/regex/HashDecompositions"); //Huge constant array/hashmap
    }
    
}
