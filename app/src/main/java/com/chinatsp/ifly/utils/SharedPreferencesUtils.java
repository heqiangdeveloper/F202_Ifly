package com.chinatsp.ifly.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.chinatsp.ifly.api.constantApi.AppConstant;

public class SharedPreferencesUtils {
    private static final String PROJECTION = AppConstant.PREFERENCE_NAME;

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defaultValue);
    }

    public static void saveBoolean(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putBoolean(key, value);
        commit(editor);

    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        return preferences.getInt(key, defaultValue);
    }

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putInt(key, value);
        commit(editor);
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        return preferences.getString(key, defaultValue);
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(key, value);
        commit(editor);
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        return preferences.getFloat(key, defaultValue);
    }

    public static void saveFloat(Context context, String key, float value) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putFloat(key, value);
        commit(editor);
    }

    public static long getLong(Context context, String key, long defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        return preferences.getLong(key, defaultValue);
    }

    public static void saveLong(Context context, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(PROJECTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putLong(key, value);
        commit(editor);
    }

    private static void commit(final SharedPreferences.Editor editor) {
        ThreadPoolUtils.executeSingle(new Runnable() {
            @Override
            public void run() {
                editor.commit();
            }
        });
    }
}

