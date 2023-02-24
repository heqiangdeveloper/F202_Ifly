package com.chinatsp.ifly.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {

    public static boolean DEBUG_LOG = true;
    public static boolean APP_TAG = true;
    public static String mAppTag = "xyj";

    private static String getAppTag() {
        return mAppTag;
    }

    public static void v(String tag, String msg) {
        if (DEBUG_LOG) {
            Log.v(getAppTag(), getLogMsg(tag, msg));
        }
    }


    public static void d(String tag, String msg) {
        if (DEBUG_LOG) {
            Log.d(getAppTag(), getLogMsg(tag, msg));
        }
    }

    public static void i(String tag, String msg) {
        Log.i(getAppTag(), getLogMsg(tag, msg));
    }

    public static void w(String tag, String msg) {
        Log.w(getAppTag(), getLogMsg(tag, msg));
    }

    public static void w(String tag, String msg, Throwable e) {
        Log.w(getAppTag(), getLogMsg(tag, msg + " Exception: " + getExceptionMsg(e)));
    }

    public static void e(String tag, String msg) {
        Log.e(getAppTag(), getLogMsg(tag, msg));
    }

    public static void e(String tag, String msg, Throwable e) {
        Log.e(getAppTag(), getLogMsg(tag, msg + " Exception: " + getExceptionMsg(e)));
    }

    public static String getExceptionMsg(Throwable e) {
        StringWriter sw = new StringWriter(1024);
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    private static String getLogMsg(String tag, String msg) {
        if (APP_TAG) {
            return "[" + tag + "] " + msg;
        }
        return msg;
    }

    //超出打印
    private static final int MAX_LENGTH = 3900;
    public static void debugLarge(String tag, String content) {
        if (content.length() > MAX_LENGTH) {
            String part = content.substring(0, MAX_LENGTH);
            Log.d(tag, part);
            part = content.substring(MAX_LENGTH, content.length());
            if ((content.length() - MAX_LENGTH) > MAX_LENGTH) {
                debugLarge(tag, part);
            } else {
                Log.d(tag, part);
            }
        } else {
            Log.d(tag, content);
        }
    }
}
