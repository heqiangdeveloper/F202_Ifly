package com.chinatsp.ifly.video;

import android.util.Log;

/**
 * Created by ytkj on 2018/10/30.
 */

public class MjLog {
    private static final String PRE = "<mj> ";

    public MjLog() {
    }

    public static void e(String tag, String msg) {
        Log.e(tag, "<mj> " + msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, "<mj> " + msg);
    }

    public static void d(String tag, String msg) {
        Log.d(tag, "<mj> " + msg);
    }

    public static void i(String tag, String msg) {
        Log.i(tag, "<mj> " + msg);
    }

    public static void v(String tag, String msg) {
        Log.v(tag, "<mj> " + msg);
    }
}
