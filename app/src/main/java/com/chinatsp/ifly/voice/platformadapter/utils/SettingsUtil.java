package com.chinatsp.ifly.voice.platformadapter.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * 设置操作工具类
 */
public class SettingsUtil {

    public static final String SETTING_PACKAGE = "com.chinatsp.settings";
    private static final String SETTING_ACTIVITY = "com.chinatsp.settings.MainActivity";

    private static final String EXTRA_KEY_OPEN_SETTING = "open_setting";
    // 打开显示设置
    public static final String EXTRA_OPEN_SETTING_VALUE_DISPLAY = "open_display";
    // 打开音效设置
    public static final String EXTRA_OPEN_SETTING_VALUE_AUDIO = "open_audio";
    // 打开网络设置
    public static final String EXTRA_OPEN_SETTING_VALUE_NETWORK = "open_network";
    //打开移动网络
    public static final String EXTRA_OPEN_SETTING_NETWORK_TBOX = "open_network_tbox";
    // 打开时间设置
    public static final String EXTRA_OPEN_SETTING_VALUE_TIME = "open_time";
    // 打开蓝牙设置
    public static final String EXTRA_OPEN_SETTING_VALUE_BLUETOOTH = "open_bluetooth";
    // 打开外接设备设置
    public static final String EXTRA_OPEN_SETTING_VALUE_EXTERNAL_DEVICES = "open_external_devices";
    // 打开系统升级
    public static final String EXTRA_OPEN_SETTING_VALUE_SYSTEM_UPGRADE = "open_system_upgrade";
    // 打开版本信息
    public static final String EXTRA_OPEN_SETTING_VALUE_VERSION_INFO = "open_version_info";
    // 打开恢复出厂设置
    public static final String EXTRA_OPEN_SETTING_VALUE_FACTORY_RESET = "open_factory_reset";

    public static void startSetting(Context mContext , String type) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(SETTING_PACKAGE, SETTING_ACTIVITY));
        if (type != null) {
            intent.putExtra(EXTRA_KEY_OPEN_SETTING, type);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
