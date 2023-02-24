package com.chinatsp.ifly;

import android.app.Activity;

import java.util.Stack;

public class AppManager {

    /**
     * 栈
     */
    private static Stack<Activity> activityStack;
    private volatile static AppManager instance;

    private AppManager() {
    }

    /**
     * 单一实例
     */
    public static AppManager getAppManager() {
        if (instance == null) {
            synchronized (AppManager.class) {
                if (instance == null) {
                    instance = new AppManager();
                }
            }
        }
        return instance;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
 //为解决修改bugID1063633导致引起的黑屏问题
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_from_right);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        if (activityStack == null || activityStack.size() == 0) {
            return null;
        }
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        if (activityStack != null && activityStack.size() > 0) {
            Activity activity = activityStack.lastElement();
            finishActivity(activity);
        }
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activityStack == null) {
            return;
        }
        if (activity != null && !activity.isFinishing()) {
            activityStack.remove(activity);
            activity.finish();
            activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_from_right);
            activity = null;
        }
    }

    /**
     * 移除指定的Activity
     *
     * @param activity
     */
    public void removeActivity(Activity activity) {
        if (activityStack == null) {
            return;
        }
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        if (activityStack == null) {
            return;
        }
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 退出选择列表界面
     */
    public void finishListActivity() {
        if (activityStack == null) {
            return;
        }
        Activity activity;
        for (int i=0; i<activityStack.size(); i++) {
            activity = activityStack.get(i);
            if(activity instanceof FullScreenActivity){
                activity.finish();
                activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_from_right);
            }
        }
//        activityStack.clear();
    }

//    /**
//     * 退出应用程序
//     */
//    public void appExit(Boolean isBackground) {
//        try {
//            //finish所有activity
//            finishAllActivity();
//            //杀死进程
//            android.os.Process.killProcess(android.os.Process.myPid());
//        } catch (Exception ignored) {
//        } finally {
//            if (!isBackground) {
//                System.exit(0);
//            }
//        }
//    }

}
