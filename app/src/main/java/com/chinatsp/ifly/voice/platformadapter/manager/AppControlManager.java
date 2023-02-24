package com.chinatsp.ifly.voice.platformadapter.manager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.iflytek.adapter.sr.SRAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class AppControlManager {
    private final static String TAG = "AppControlManager";
    private Context mContext;
    private PackageManager mPackageManager;
    private ArrayList<AppInfo> mAppList;
    public static AppControlManager instance;

    PackageReceiver mReceiver = new PackageReceiver();

    private static final String CAMERA = "相机";
    private AppControlManager(Context context) {
        this.mContext = context;
    }

    public static AppControlManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppControlManager(context);
        }
        return instance;
    }

    private class PackageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive," + intent.getAction());
            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                if (packageName == null || packageName.length() == 0) {
                    return;
                }

                if (replacing && Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadAllAppsByBatch();
                        uploadAppDict();
                    }
                }).start();

            } else {
                Log.e(TAG, "onReceive,unknown action,:" + intent.getAction());
            }
        }
    }

    public class AppInfo {
        public String packageName;
        public String title;
        public Intent intent;
        public ResolveInfo info;

        AppInfo(ResolveInfo info, String title, Intent intent) {
            this.info = info;
            this.packageName = info.activityInfo.applicationInfo.packageName;
            this.intent = intent;
            this.title = title;
        }
    }

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadAllAppsByBatch();
                uploadAppDict();
            }
        }).start();

        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mReceiver, filter);
    }

    private void uploadAppDict() {
        JSONArray mArray = new JSONArray();
        JSONObject mDict;
        try {
            if (mAppList != null) {
                for (int i = 0; i < mAppList.size(); i++) {
                    String appName = mAppList.get(i).title;
                    mDict = new JSONObject();
                    mDict.put("id", i);
                    mDict.put("name", appName);
                    mArray.put(mDict);
                }
            }
            //增加特殊的APP name, 使其具有app语义
            mDict = new JSONObject();
            mDict.put("id", mAppList.size());
            mDict.put("name", "车控");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 1);
            mDict.put("name", "驾驶员中心");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 2);
            mDict.put("name", "车况");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 3);
            mDict.put("name", "显示设置");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 4);
            mDict.put("name", "音效设置");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 5);
            mDict.put("name", "系统升级");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 6);
            mDict.put("name", "恢复出厂");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 7);
            mDict.put("name", "听歌识曲");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 8);
            mDict.put("name", "音效");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 9);
            mDict.put("name", "移动网络");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 10);
            mDict.put("name", "蓝牙设置");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 11);
            mDict.put("name", "网络设置");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 12);
            mDict.put("name", "视频");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 13);
            mDict.put("name", "胎压检测");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 14);
            mDict.put("name", "高德导航");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 15);
            mDict.put("name", "百度导航");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 16);
            mDict.put("name", "微信到车");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 17);
            mDict.put("name", "微信位置发送到车");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 18);
            mDict.put("name", "微信发送到车");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 19);
            mDict.put("name", "工厂模式");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 20);
            mDict.put("name", "工厂设置");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 21);
            mDict.put("name", "手机映射");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 22);
            mDict.put("name", "hicar");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 23);
            mDict.put("name", "云眼后视仪");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 24);
            mDict.put("name", "后视仪");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 25);
            mDict.put("name", "车辆设置");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 26);
            mDict.put("name", "华为hicar");
            mArray.put(mDict);

           /* mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 23);
            mDict.put("name", "云眼相机");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 24);
            mDict.put("name", "云眼车载相机");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 25);
            mDict.put("name", "相机");
            mArray.put(mDict);

            mDict = new JSONObject();
            mDict.put("id", mAppList.size() + 26);
            mDict.put("name", "车载相机");
            mArray.put(mDict);*/


        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject result = new JSONObject();
        try {
            result.put("dictname", "app");
            result.put("dictcontant", mArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray rA = new JSONArray();
        rA.put(result);

        JSONObject root = new JSONObject();
        try {
            root.put("grm", rA);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LogUtils.d(TAG, "root:"+root.toString());
        SRAgent.getInstance().uploadDict(root.toString());
    }

    public void destroy() {
        mContext.unregisterReceiver(mReceiver);
    }

    private void loadAllAppsByBatch() {
        LogUtils.d(TAG, "loadAllAppsByBatch");
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        mPackageManager = mContext.getPackageManager();
        List<ResolveInfo> apps = mPackageManager.queryIntentActivities(mainIntent, 0);

        synchronized (AppControlManager.this) {
            if (mAppList != null) {
                mAppList.clear();
            } else {
                mAppList = new ArrayList<>(apps.size());
            }
            for (int i = 0; i < apps.size(); i++) {
                ResolveInfo info = apps.get(i);

                ComponentName componentName = new ComponentName(info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name);
                int launchFlags = Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(componentName);
                intent.setFlags(launchFlags);
                if(CAMERA.equals(info.loadLabel(mPackageManager).toString()))
                    continue;
                mAppList.add(new AppInfo(info, info.loadLabel(mPackageManager).toString(), intent));
            }
            LogUtils.d(TAG, "mAppList size:" + mAppList.size());
        }
    }

    public void startApp(String name) {
        Log.d(TAG, "StartApp," + name);
        if (name!=null&&name.isEmpty()) {
            Log.e(TAG, "StartApp,name is null or empty");
            return;
        }
        Intent intent = null;
        synchronized (AppControlManager.this) {
            AppInfo info;
            for (int i = 0; i < mAppList.size(); i++) {
                info = mAppList.get(i);
                if (info.title != null && (info.title.equalsIgnoreCase(name) || info.title.contains(name))) {
                    intent = info.intent;
                    break;
                }
            }

            if (null == intent) {
                for (int i = 0; i < mAppList.size(); i++) {
                    info = mAppList.get(i);
                    if (info.packageName.equals(name)) {
                        intent = info.intent;
                        break;
                    }
                }
            }
        }

        String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        Log.d(TAG, "startApp() called with: topPackage = [" + topPackage + "]");
        if("tv.newtv.vcar".equals(topPackage)&&"央视影音".equals(name))
            return;
        if("央视影音".equals(name)){
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        Log.d(TAG, "StartApp, " + intent);
        if (null != intent) {
            mContext.startActivity(intent);
        } else {
            Log.w(TAG, "StartApp,can't find [" + name + "]");
        }
    }

    public void closeApp(String name) {
        Log.d(TAG, "closeApp," + name);
        if (name.isEmpty()) {
            Log.e(TAG, "closeApp,name is null or empty");
            return;
        }
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = null;

        synchronized (AppControlManager.this) {
            AppInfo info;
            for (int i = 0; i < mAppList.size(); i++) {
                info = mAppList.get(i);
                if (info.title != null && (info.title.equalsIgnoreCase(name) || info.title.contains(name))) {
                    packageName = info.packageName;
                    break;
                }
            }

            if (null == packageName) {
                for (int i = 0; i < mAppList.size(); i++) {
                    info = mAppList.get(i);
                    if (info.packageName.equals(name)) {
                        packageName = info.packageName;
                        break;
                    }
                }
            }

            Log.d(TAG, "CloseApp," + name + "," + packageName);
            if("com.autonavi.amapauto".equals(packageName)||("com.baidu.BaiduMap.auto").equals(packageName)){
                ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                Method method = null;
                try {
                    Log.d(TAG, "closeApp() called with: name = [" + packageName + "]");
                    method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
                    method.invoke(mActivityManager, packageName);  //packageName是需要强制停止的应用程序包名
                    /***********欧尚修改开始*****************/
                    GDSdkManager.getInstance(mContext).sendCloseBroadcast();
                    /***********欧尚修改结束*****************/
                }  catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            if (null != packageName) {
                List<RunningTaskInfo> tasks = am.getRunningTasks(5);
                for (int i = 0; i < tasks.size(); i++) {
                    if(!packageName.equals(tasks.get(0).topActivity.getPackageName())){  //如果第一个应用不是要关闭的应用，直接返回
                        Log.e(TAG, "closeApp: "+tasks.get(0).topActivity.getPackageName());
                        return;
                    }

                    if(!packageName.equals(tasks.get(i).topActivity.getPackageName())) {
//                        int taskID = tasks.get(i).id;
//                        am.moveTaskToFront(taskID, ActivityManager.MOVE_TASK_WITH_HOME);
                        Utils.gotoHome();
                        break;
                    }
                }
            } else {
                Utils.getMessageWithTtsSpeakOnly(mContext, "", "没有找到可以关闭的应用", new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                    }
                });
                Log.w(TAG, "CloseApp,can't find [" + name + "]");
            }
        }
    }
    /**
     * 将本应用置顶到最前端
     * 当本应用位于后台时，则将它切换到最前端
     *
     * @param context
     */
    public void setTopApp(Context context) {
        /**获取ActivityManager*/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);

        /**获得当前运行的task(任务)*/
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            /**找到本应用的 task，并将它切换到前台*/
            if (taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                Log.d("hqtest","find my task.. ");
                activityManager.moveTaskToFront(taskInfo.id, 0);
                break;
            }
        }
    }

    /**
     * 当指定应用位于后台时，则将它切换到最前端
     */
    public void setTopApp(Context context,String packageName) {
        /**获取ActivityManager*/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);

        /**获得当前运行的task(任务)*/
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            /**找到本应用的 task，并将它切换到前台*/
            if (taskInfo.topActivity.getPackageName().equals(packageName)) {
                activityManager.moveTaskToFront(taskInfo.id, 0);
                break;
            }
        }
    }

    /**
     * 判断本地是否已经安装好了指定的应用程序包
     *
     * @param packageNameTarget ：待判断的 App 包名，如 微博 com.sina.weibo
     * @return 已安装时返回 true,不存在时返回 false
     */
    public boolean appIsExist(Context context, String packageNameTarget) {
        if (!"".equals(packageNameTarget.trim())) {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            for (PackageInfo packageInfo : packageInfoList) {
                String packageNameSource = packageInfo.packageName;
                if (packageNameSource.equals(packageNameTarget)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean appIsExistByName(Context context, String name){
        AppInfo info;
        if (!"".equals(name.trim())) {
            for (int i = 0; i < mAppList.size(); i++) {
                info = mAppList.get(i);
                if (info.title != null && (info.title.equalsIgnoreCase(name) || info.title.contains(name))) {
                   return true;
                }
            }
        }
        return false;
    }

    //判断指定的应用是否位于最前端
    public boolean isAppointForeground(String packageName) {
        try {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(50);
            if (runningTaskInfos != null && runningTaskInfos.size() != 0) {
                String packageName0=(runningTaskInfos.get(0).baseActivity).getPackageName();
                if(packageName.equals(packageName0)){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 判断应用是否已经启动
     *
     * @param context     上下文对象
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }
}
