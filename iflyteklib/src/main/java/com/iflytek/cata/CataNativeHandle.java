/*    */
package com.iflytek.cata;

/*    */
/*    */ public class CataNativeHandle {
    /*    */   public int err_ret;
    /*    */   public int native_point;

    /*    */
    /*    */
    public int getErr_ret() {
        /*  8 */
        return this.err_ret;
        /*    */
    }

    /*    */
    /*    */
    public int getNative_point() {
        /* 12 */
        return this.native_point;
        /*    */
    }

    /*    */
    /*    */
    public void setErr_ret(int err_ret) {
        /* 16 */
        this.err_ret = err_ret;
        /*    */
    }

    /*    */
    /*    */
    public void setNative_point(int native_point) {
        /* 20 */
        this.native_point = native_point;
        /*    */
    }

    /*    */
    /*    */
    public void reSet() {
        /* 24 */
        this.err_ret = 0;
        /* 25 */
        this.native_point = 0;
        /*    */
    }
    /*    */
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\cata\CataNativeHandle.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */