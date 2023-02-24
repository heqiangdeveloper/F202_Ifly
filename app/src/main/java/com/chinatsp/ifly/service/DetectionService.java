package com.chinatsp.ifly.service;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.CommandEvent;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;

import org.greenrobot.eventbus.EventBus;


public class DetectionService extends AccessibilityService {
    private static final String TAG = "DetectionService";

    private static final String PACKAGE_RADIO       = "com.edog.car.oushangsdk";
    private static final String PACKAGE_MUSIC = "com.chinatsp.music";
    private static final String PACKAGE_LAUNCHER = "com.tencent.wecarnavi";
    private static final String PACKAGE_TEL = "com.chinatsp.phone";

    private static final String PACKAGE_CHANGBA = "com.changba.sd";
    private static final String PACKAGE_VIDEO = "tv.newtv.vcar";
    public static final String PACKAGE_DVR = "com.chinatsp.dvrcamera";
    public static final String PACKAGE_NAVI = "com.tencent.wecarnavi";
    public static final String PACKAGE_IFLY = "com.chinatsp.ifly";
    public static final String ACTIVITY_VIDEO = "com.chinatsp.music.video.VideoActivity";
    public static final String ACTIVITY_HICAR = "com.huawei.hicar.demoapp.HiCarDemoActivity";

    private static final String[] PACKAGES   = new String[]{PACKAGE_RADIO, PACKAGE_MUSIC,PACKAGE_LAUNCHER};
    private String mLastPackage;
    private int current;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);

            if(am.getRunningTasks(1).size()<=0)
                return;

            ComponentName   cn = am.getRunningTasks(1).get(0).topActivity;

            String packageName = cn.getPackageName();
            if(packageName.equals(mLastPackage)){
                return;
            }
            Log.d(TAG, "getPackageInfo() called::packageName::"+packageName+".."+".."+cn.getClassName()+TspSceneAdapter.getTspScene(this));



//            if(PACKAGE_CHANGBA.equals(mLastPackage)
//                    ||PACKAGE_VIDEO.equals(mLastPackage)
//                    ||PACKAGE_DVR.equals(mLastPackage))
//                TspSceneManager.getInstance().resetCustomWvms(this);

            AppConstant.mCurrentPkg = packageName;
//            if(PACKAGE_NAVI.equals(packageName)){//当前在导航界面  显示悬浮球
//                EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.SHOW,null,null));
//            }else  //隐藏悬浮球
//                EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.HIDE,null,null));


         /*   if(PACKAGE_IFLY.equals(packageName)){
                return;
            }*/

        /*    if(PACKAGE_CHANGBA.equals(packageName)){
                current = TspSceneAdapter.getTspScene(this);
                TspSceneManager.getInstance().switchSceneToChangba(this);
            }else if(PACKAGE_VIDEO.equals(packageName)){
                current = TspSceneAdapter.getTspScene(this);
                TspSceneManager.getInstance().switchSceneToVideo(this);
            }else*/ if(PACKAGE_DVR.equals(packageName)){
                current = TspSceneAdapter.getTspScene(this);
                TspSceneManager.getInstance().switchSceneToDVR(this);
            }else if(PACKAGE_DVR.equals(mLastPackage)){
                restartScrene();
           }

            mLastPackage = packageName;

        }
    }

    @Override
    public void onInterrupt() {
    }


    private void restartScrene(){
        Log.d(TAG, "restartScrene() called");
        TspSceneManager.getInstance().resetScrene(this,current);
        current = -1;
    }


}
