//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.cata;

import android.content.Context;
import android.util.Log;

public class CataSession implements ICataService {
    public static final int ISS_CATA_LANG_MANDARIN = 1;
    public static final int ISS_CATA_LANG_ENGLISH = 2;
    public static final int ISS_CATA_LANG_CANTONESE = 3;
    public static final int ONLY_RAW = 0;
    public static final int RAW_AND_PINYIN = 1;
    public static final int TAG = 2;
    public static final int RAW_AND_TJIU = 3;
    public static final int RAW_AND_PINYIN_AND_TJIU = 4;
    private static int mIndexInstCnt = 0;
    private static int mSearchInstCnt = 0;
    private static final String tag = "CataSolution";
    private static CataSession instance = null;
    private static String mResDir;
    private static ICataInitListener mInitListener = null;
    private Context mContext = null;

    private CataSession(Context ctx, ICataInitListener l, String resDir) {
        if (null != ctx) {
            this.mContext = ctx;
            mInitListener = l;
            mResDir = resDir;
            this.initService(false);
        }
    }

    public static CataSession getInstance(Context context, ICataInitListener cataInitListener, String resDir) {
        if (instance == null) {
            Class var3 = CataSession.class;
            synchronized (CataSession.class) {
                if (instance == null) {
                    instance = new CataSession(context, cataInitListener, resDir);
                }
            }
        }

        return instance;
    }

    public CataIndexInst createCataIndexInst() {
        return new CataSession.CataIndexInstIm();
    }

    public CataSearchInst createCataSearchInst() {
        return new CataSession.CataSearchInstIm();
    }

    public synchronized void initService(boolean notUse) {
        if (mInitListener != null) {
            mInitListener.onCataInited(true, 0);
        }

    }

    private class CataSearchInstIm implements CataSearchInst {
        private String tag;
        private CataNativeHandle cataNativeHandle;

        public CataSearchInstIm() {
            this.tag = "CataSearchInst_" + CataSession.mSearchInstCnt;
            CataSession.mSearchInstCnt++;
            this.cataNativeHandle = new CataNativeHandle();
        }

        public int create(String resNames, ICataListener cataSearchListener) {
            this.cataNativeHandle.reSet();
            libisscata.SearchCreate(this.cataNativeHandle, CataSession.mResDir, resNames, cataSearchListener);
            Log.d(this.tag, "libisscata.SearchCreate(mResDir=" + CataSession.mResDir + ", resNames=" + resNames + ") ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int createEx(String resNames, int langType, ICataListener cataSearchListener) {
            this.cataNativeHandle.reSet();
            libisscata.SearchCreateEx(this.cataNativeHandle, CataSession.mResDir, resNames, langType, cataSearchListener);
            Log.d(this.tag, "libisscata.SearchCreate(mResDir=" + CataSession.mResDir + ", resNames=" + resNames + "langType=" + langType + ") ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public String searchSync(String query) {
            Log.d(this.tag, "Calling libisscata.SearchSync(" + query + ").");
            return libisscata.SearchSync(this.cataNativeHandle, query);
        }

        public int searchAsync(String query) {
            libisscata.SearchAsync(this.cataNativeHandle, query);
            Log.d(this.tag, "libisscata.SearchAsync(" + query + ") ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int destroy() {
            libisscata.SearchDestroy(this.cataNativeHandle);
            Log.d(this.tag, "libisscata.SearchDestroy ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int setParam(int paramtype, int paramvalue) {
            libisscata.SetParam(this.cataNativeHandle, paramtype, paramvalue);
            Log.d(this.tag, "libisscata.setParam ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }
    }

    private class CataIndexInstIm implements CataIndexInst {
        private String tag;
        private CataNativeHandle cataNativeHandle;

        public CataIndexInstIm() {
            this.tag = "CataIndexInst_" + CataSession.mIndexInstCnt;
            CataSession.mIndexInstCnt++;
            this.cataNativeHandle = new CataNativeHandle();
        }

        public int create(String resName, ICataListener cataSearchListener) {
            this.cataNativeHandle.reSet();
            libisscata.IndexCreate(this.cataNativeHandle, CataSession.mResDir, resName, 0, cataSearchListener);
            Log.d(this.tag, "libisscata.IndexCreate(mResDir=" + CataSession.mResDir + ", resName=" + resName + ") ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int createEx(String resName, int langType, ICataListener cataSearchListener) {
            this.cataNativeHandle.reSet();
            libisscata.IndexCreateEx(this.cataNativeHandle, CataSession.mResDir, resName, langType, 0, cataSearchListener);
            Log.d(this.tag, "libisscata.IndexCreateEx(mResDir=" + CataSession.mResDir + ", resName=" + resName + "langType=" + langType + ")ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int reCreate(String resName, ICataListener cataSearchListener) {
            this.cataNativeHandle.reSet();
            libisscata.IndexCreate(this.cataNativeHandle, CataSession.mResDir, resName, 1, cataSearchListener);
            Log.d(this.tag, "libisscata.IndexReCreate(mResDir=" + CataSession.mResDir + ", resName=" + resName + ") ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int reCreateEx(String resName, int langType, ICataListener cataSearchListener) {
            this.cataNativeHandle.reSet();
            libisscata.IndexCreateEx(this.cataNativeHandle, CataSession.mResDir, resName, langType, 1, cataSearchListener);
            Log.d(this.tag, "libisscata.IndexReCreateEx(mResDir=" + CataSession.mResDir + ", resName=" + resName + "langType=" + langType + ") ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int drop() {
            libisscata.IndexDropRes(this.cataNativeHandle);
            Log.d(this.tag, "libisscata.IndexDropRes ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int addIdxEntity(String data) {
            libisscata.IndexAddIdxEntity(this.cataNativeHandle, data);
            return this.cataNativeHandle.err_ret;
        }

        public int delIdxEntity(String data) {
            libisscata.IndexDelIdxEntity(this.cataNativeHandle, data);
            return this.cataNativeHandle.err_ret;
        }

        public int endIdxEntity() {
            libisscata.IndexEndIdxEntity(this.cataNativeHandle);
            Log.d(this.tag, "libisscata.endIdxEntity ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }

        public int destroy() {
            libisscata.IndexDestroy(this.cataNativeHandle);
            Log.d(this.tag, "libisscata.IndexDestroy ret " + this.cataNativeHandle.err_ret);
            return this.cataNativeHandle.err_ret;
        }
    }
}
