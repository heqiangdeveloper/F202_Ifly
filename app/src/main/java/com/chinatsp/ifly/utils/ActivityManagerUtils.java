package com.chinatsp.ifly.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityManagerUtils {

    private Context mContext;
    private static ActivityManagerUtils instance = null;

    private ActivityManagerUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized ActivityManagerUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ActivityManagerUtils(context);
        }
        return instance;
    }

    public boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        try {
            if (rti.size() == 0) {
                return true;
            } else {
                if (rti.get(0).topActivity.getPackageName().equals(mContext.getPackageName()))
                    return false;
                else
                    return getHomes().contains(rti.get(0).topActivity.getPackageName());
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    /**
     * 判断服务是否开启
     *
     * @return
     */
    public boolean isServiceRunning(String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    public boolean packageProccessIsExist(String pkgName) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
        if (appProcessList != null && appProcessList.size() != 0) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                if (pkgName.equals(appProcess.processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getTopPackage() {
        try {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            //获取正在运行的task列表，其中1表示最近运行的task，通过该数字限制列表中task数目，最近运行的靠前
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

            if (runningTaskInfos != null && runningTaskInfos.size() != 0) {
                return (runningTaskInfos.get(0).baseActivity).getPackageName();
            }
        } catch (Exception e) {
            LogUtils.d("ActivityManagerUtils", "栈顶应用:" + e);
        }
        return "";
    }

    public String getTopActivity(){
        try {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            //获取正在运行的task列表，其中1表示最近运行的task，通过该数字限制列表中task数目，最近运行的靠前
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

            if (runningTaskInfos != null && runningTaskInfos.size() != 0) {
                return (runningTaskInfos.get(0).topActivity).getClassName();
            }
        } catch (Exception e) {
            LogUtils.d("ActivityManagerUtils", "栈顶应用:" + e);
        }
        return "";
    }

    /**
     * 调用返回键
     */
    public void inputKeyBack() {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
