package com.chinatsp.ifly.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.chinatsp.ifly.AppManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.EventBusUtils;

/**
 * 生命周期管理类
 */
public class BaseLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "BaseLifecycleCallback";
    private boolean isAppOnforeground = false;

    public static BaseLifecycleCallback getInstance() {
        return HolderClass.INSTANCE;
    }

    private final static class HolderClass {
        private final static BaseLifecycleCallback INSTANCE = new BaseLifecycleCallback();
    }


    private BaseLifecycleCallback() {
    }

    /**
     * 必须在 Application 的 onCreate 方法中调用
     */
    public void init(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated() called with: activity = [" + activity.toString() + "]");
        AppManager.getAppManager().addActivity(activity);
        if(activity instanceof SettingsActivity) {
            EventBusUtils.sendRightImageMessage(ArtEvent.RightImageType.IMAGE_BACKUP);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed:" + activity.toString());
        isAppOnforeground = true;
        EventBusUtils.sendLeftImageMessage(false);
        EventBusUtils.sendRightImageMessage(ArtEvent.RightImageType.IMAGE_BACKUP);

        if(activity instanceof SettingsActivity) {
            Log.d(TAG, "onActivityResumed:" + activity.getLocalClassName());
//            AppConfig.INSTANCE.settingFlag = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onActivityStopped:" + activity.toString());
        isAppOnforeground = false;
        /**
         * 10-15 10:01:08.737  2709  2709 D BaseLifecycleCallback: onActivityResumed:com.chinatsp.ifly.FullScreenActivity@25b32ff
         * 10-15 10:01:08.994  2709  2709 D BaseLifecycleCallback: onActivityStopped:com.chinatsp.ifly.FullScreenActivity@4b20fb7
         * Activity生命周期时序不可控，隐藏Activity的stop方法在显示Activity的resume方法之后，需判断stack顶部Activity类型
         */
        Activity currentActivity = AppManager.getAppManager().currentActivity();
        if (currentActivity != null || currentActivity instanceof  FullScreenActivity) {
            // 选择列表在栈顶，不显示关闭按钮  fix bug ID1065141
            Log.d(TAG, "onActivityStopped:  currentActivity != null || currentActivity instanceof  FullScreenActivity");
        } else {
            EventBusUtils.sendLeftImageMessage(true);
        }

        if(activity instanceof SettingsActivity) {
            Log.d(TAG, "onActivityStopped:" + activity.getLocalClassName());
            EventBusUtils.sendRightImageMessage(ArtEvent.RightImageType.IMAGE_SETTINGS);
//            AppConfig.INSTANCE.settingFlag = false;
        } else if(activity instanceof FullScreenActivity) {
            Log.d(TAG, "onActivityStopped:" + activity.getLocalClassName());
            EventBusUtils.sendRightImageMessage(ArtEvent.RightImageType.IMAGE_SETTINGS);
//            AppConfig.INSTANCE.settingFlag = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "onActivityDestroyed() called with: activity = [" + activity.toString() + "]");
        AppManager.getAppManager().removeActivity(activity);
        if(activity instanceof SettingsActivity) {
//            EventBusUtils.sendRightImageMessage(ArtEvent.RightImageType.IMAGE_SETTINGS);
        }
    }

    public boolean isAppOnForeground() {
        Log.d(TAG, "isAppOnForeground: foregroundActivityCount:"+isAppOnforeground);
        return isAppOnforeground;
    }
}
