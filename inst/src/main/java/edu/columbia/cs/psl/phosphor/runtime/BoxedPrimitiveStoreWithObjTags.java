package edu.columbia.cs.psl.phosphor.runtime;

import edu.columbia.cs.psl.phosphor.struct.*;

public class BoxedPrimitiveStoreWithObjTags {

    public static WeakIdentityHashMap<Object, Taint> tags = new WeakIdentityHashMap<>();

    private BoxedPrimitiveStoreWithObjTags() {
        // Prevents this class from being instantiated
    }

    public static TaintedBooleanWithObjTag booleanValue(Boolean z) {
        TaintedBooleanWithObjTag ret = new TaintedBooleanWithObjTag();
        ret.val = z;
        return ret;
    }

    public static TaintedByteWithObjTag byteValue(Byte z) {
        TaintedByteWithObjTag ret = new TaintedByteWithObjTag();
        ret.val = z;
        return ret;
    }

    public static TaintedShortWithObjTag shortValue(Short z) {
        TaintedShortWithObjTag ret = new TaintedShortWithObjTag();
        ret.val = z;
        return ret;
    }

    public static TaintedCharWithObjTag charValue(Character z) {
        TaintedCharWithObjTag ret = new TaintedCharWithObjTag();
        ret.val = z.charValue();
        return ret;
    }

    public static Boolean valueOf(Taint tag, boolean z) {
        return Boolean.valueOf(z);
    }

    public static Byte valueOf(Taint tag, byte z) {
        return Byte.valueOf(z);
    }

    public static Character valueOf(Taint tag, char z) {
        return Character.valueOf(z);
    }

    public static Short valueOf(Taint tag, short z) {
        return Short.valueOf(z);
    }
}
