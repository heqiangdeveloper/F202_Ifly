//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.cata;

public class libisscata {
    private static final String tag = "libisscata";

    public libisscata() {
    }

    public static native void IndexCreate(CataNativeHandle var0, String var1, String var2, int var3, ICataListener var4);

    public static native void IndexCreateEx(CataNativeHandle var0, String var1, String var2, int var3, int var4, ICataListener var5);

    public static native void IndexDestroy(CataNativeHandle var0);

    public static native void IndexDropRes(CataNativeHandle var0);

    public static native void IndexAddIdxEntity(CataNativeHandle var0, String var1);

    public static native void IndexDelIdxEntity(CataNativeHandle var0, String var1);

    public static native void IndexEndIdxEntity(CataNativeHandle var0);

    public static native void SearchCreate(CataNativeHandle var0, String var1, String var2, ICataListener var3);

    public static native void SearchCreateEx(CataNativeHandle var0, String var1, String var2, int var3, ICataListener var4);

    public static native void SearchDestroy(CataNativeHandle var0);

    public static native String SearchSync(CataNativeHandle var0, String var1);

    public static native void SearchAsync(CataNativeHandle var0, String var1);

    public static native void SetParam(CataNativeHandle var0, int var1, int var2);

    static {
        System.loadLibrary("cata");
        System.loadLibrary("cataIndex");
        System.loadLibrary("cata-jni");
    }
}
