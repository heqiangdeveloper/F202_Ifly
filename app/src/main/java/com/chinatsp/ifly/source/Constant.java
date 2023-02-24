package com.chinatsp.ifly.source;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class Constant {
    /** 0.DEBUG 1.IMX8 2.北汽 3.S111 4.3A20 */
    public static int Project = 4;

    public static String hddRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
   // public static String hddRoot = "/data/media/";
    public static String usbRoot = "/storage/udisk";
    public static String picSelectPath = "";
    public static String videoSelectPath = "";
    public static String playPathKey = "PLAYPTH";
    public static String version = "v1.3";
    /** 1.音乐 2.视频 3.图片 4.蓝牙音乐 */
    public static int lastPlayType = 0;
    public static final String ActionTunerApp = "";

    public static final int PLAY_STATE_PLAY = 1;
    public static final int PLAY_STATE_PAUSE = 0;
    public static final int PLAY_STATE_STOP = -1;

}
