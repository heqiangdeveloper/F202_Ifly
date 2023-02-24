package com.iflytek.adapter.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TimeoutManager {
    private static final String TAG = "TimeoutManager";
    public static final int ORIGINAL = 0;  //原始值
    public static final int NO_UNDERSTAND = 2; //未理解
    public static final int UNDERSTAND_ONCE = 3; //已完成一次识别
    public static final int WAIT_CRUISE_ACTION = 4;//定速巡航，等待用户的操作

    public static void saveSrState(Context context, int state) {
        Log.d(TAG, "saveSrState() called with: context = ["  + "], state = [" + state + "]");
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences("com.chinatsp.ifly_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("sr_state", state);
        editor.apply();
    }

    public static void saveSrState(Context context, int state, String msg) {
        Log.d(TAG, "saveSrState() called with: context = ["  + "], state = [" + state + "], msg = [" + msg + "]");
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences("com.chinatsp.ifly_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putInt("sr_state", state);
        editor.putString("sr_text", msg == null ? "" : msg);
        editor.apply();
    }

    public static int getSrState(Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences("com.chinatsp.ifly_preferences",
                Context.MODE_PRIVATE);
        return preferences.getInt("sr_state", 0);
    }

    public static String getSrText(Context context) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences("com.chinatsp.ifly_preferences",
                        Context.MODE_PRIVATE);
        return preferences.getString("sr_text", "");
    }

    public static void  clearSrState(Context context) {
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences("com.chinatsp.ifly_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString("sr_text","");
        editor.putInt("sr_state", 0);
        editor.apply();
    }
}
