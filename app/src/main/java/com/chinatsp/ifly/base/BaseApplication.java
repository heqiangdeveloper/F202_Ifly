package com.chinatsp.ifly.base;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.chinatsp.adapter.ttsservice.OnTtsInitedListener;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.db.CommandDbDao;
import com.chinatsp.ifly.db.GuideBookDbDao;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.module.me.recommend.view.ManageFloatWindow;
import com.chinatsp.ifly.receiver.NetStateReceiver;
import com.chinatsp.ifly.service.ActiveViewService;
import com.chinatsp.ifly.service.FloatViewIdleService;
import com.chinatsp.ifly.service.InitializeService;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.service.PCMRecorderService;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformAdapterClient;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.controller.VirtualControl;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.LocateManager;
import com.example.loginarar.LoginManager;
import com.example.mediasdk.MediaManager;
import com.iflytek.adapter.PlatformHelp;
import com.oushang.uploadservice.adapter.OSUploadServiceAgent;
import com.txznet.tts.TXZTTSInitManager;


public class BaseApplication extends Application {

    private static final String TAG = "BaseApplication";
    private static BaseApplication instance;
    private PlatformAdapterClient platformClient;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_ACCESSIBILITY) {
                if (!setAccessibility()) {
                    mHandler.removeMessages(MSG_ACCESSIBILITY);
                    mHandler.sendEmptyMessageDelayed(MSG_ACCESSIBILITY, DELAY_MSG_ACCESSIBILITY);
                }
            }
        }
    };

    public static synchronized BaseApplication getInstance() {
        if (null == instance) {
            instance = new BaseApplication();
        }
        return instance;
    }

    public BaseApplication() {
    }

    /**
     * 这个最先执行
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    /**
     * 程序启动的时候执行
     */
    @Override
    public void onCreate() {
        LogUtils.d("Application", "  onCreate::version code;"+ Utils.packageCode(this));
        super.onCreate();
        instance = this;
        BaseLifecycleCallback.getInstance().init(this);
        OSUploadServiceAgent.getInstance().bindService(this);
        //开启录音机服务
        PCMRecorderService.start(this);

        //在子线程中初始化
        InitializeService.start(this);

        //开启助手悬浮窗管理服务
        FloatViewIdleService.start(this);
        //开启主动服务管理服务
        ActiveViewService.start(this);
        //绑定登录服务
        LoginManager.getInstance().bindLoginService(this);

        // 给助理传递实现PlatformClientListener接口的对象
        platformClient = new PlatformAdapterClient(this);
        PlatformHelp.getInstance().setPlatformClient(platformClient);

        //注册wifi和蓝牙连接动态变化广播
        registerNetStateReceiver();

        //初始化获取"试着说" "其他人都在说"引导文案数据库
//        initGuideBookDb();

        //初始化默认语音所有指令集
        CommandDbDao.getInstance(this).initBackUp();

        initCar();

        updateToken();

        LocateManager.getInstance(this).init();

        //初始化tts播报引擎
        ttsEngineInit();

        registerModeChangeObserver();

        //TODO OTA中改变seopt开关无效，通过代码设置一次，后面可将下面代码删除
        boolean openSeopt = SharedPreferencesUtils.getBoolean(this, "open_seopt", false);
        if(!openSeopt) {
            Utils.setProperty("persist.sys.switch.seopt", "1");
            SharedPreferencesUtils.saveBoolean(this, "open_seopt", true);
        }

        /***********欧尚修改开始*****************/
        GDSdkManager.getInstance(this).registerGDReceiver();
        /***********欧尚修改结束*****************/

        initDetection(this);

        VirtualControl.getInstance().registerVirtualCast(this);

        //语音悬浮穿进行初始化
        ManageFloatWindow.getInstance(this);

        MediaManager.getInstance().bind(this);

        initVoiceSetData();
    }

    //防止恢复出厂设置时，失效
    private void initVoiceSetData() {
        boolean guide = SharedPreferencesUtils.getBoolean(instance, AppConstant.KEY_VOICE_GUIDE,AppConstant.VALUE_VOICE_GUIDE);
        boolean broadcast = SharedPreferencesUtils.getBoolean(instance, AppConstant.KEY_VOICE_BROADCAST,AppConstant.VALUE_VOICE_SPEAK);
        SharedPreferencesUtils.saveBoolean(instance, AppConstant.KEY_VOICE_GUIDE,guide);
        SharedPreferencesUtils.saveBoolean(instance, AppConstant.KEY_VOICE_BROADCAST,broadcast);
        Log.d(TAG,"broadcast ="+broadcast+",guide="+guide);
    }

    private void registerModeChangeObserver() {
        ContentResolver resolver = getContentResolver();
        Uri uri = Settings.System.getUriFor(AppConstant.SHOW_MODE);
        resolver.registerContentObserver(uri, false, new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                boolean isDark = Settings.System.getInt(getContentResolver(), AppConstant.SHOW_MODE, 0) == 0;
                if(!FloatViewManager.getInstance(getApplicationContext()).isHide()) {
                    if(isDark) {
                        EventBusUtils.sendNightModeMessage();
                    } else {
                        EventBusUtils.sendDayModeMessage();
                    }
                }
            }
        });
    }

    private void updateToken() {
        AppConfig.INSTANCE.updateToken();
    }

    /**
     * 延迟 5s 启动，避免资源紧张，txz 服务没有启动
     */
    private void ttsEngineInit() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TTSController.getInstance(BaseApplication.this).init(AudioManager.STREAM_ALARM, new OnTtsInitedListener() {
                    @Override
                    public void onTtsInited() {
//                        LogUtils.d("Application", "TTSController onTtsInited");
//                        AppConfig.INSTANCE.ttsEngineInited = true;
                    }
                });
//                TXZTTSInitManager.getInstance().setDefaultFilePath("/sdcard/iflytek/tts/externalTTS/");
            }
        },5000);

    }

    private void initCar() {
        AppConfig.INSTANCE.initCar(this);
    }

    private void initGuideBookDb() {
        GuideBookDbDao.getInstance(instance).init();
    }

    private void registerNetStateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(AppConstant.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(new NetStateReceiver(), filter);
    }

    /**
     * 程序终止的时候执行
     */
    @Override
    public void onTerminate() {
        LogUtils.d("Application", "onTerminate");
        if (platformClient != null) {
            platformClient.unRegisterCallback();
        }
        super.onTerminate();
    }

    /**
     * 低内存的时候执行
     */
    @Override
    public void onLowMemory() {
        LogUtils.d("Application", "onLowMemory");
        super.onLowMemory();
    }

    /**
     * HOME键退出应用程序
     * 程序在内存清理的时候执行
     */
    @Override
    public void onTrimMemory(int level) {
        LogUtils.d("Application", "onTrimMemory");
        super.onTrimMemory(level);
    }

    /**
     * onConfigurationChanged
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtils.d("Application", "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    private static final String  enabledServicesBuilder  =
            "com.chinatsp.ifly/com.chinatsp.ifly.service.DetectionService";
    private static final int     MSG_ACCESSIBILITY       = 1;
    private static final int     DELAY_MSG_ACCESSIBILITY = 2000;

    private void initDetection(Context context) {
        Log.i(TAG, "Init");
        mHandler.removeMessages(MSG_ACCESSIBILITY);
        mHandler.sendEmptyMessageDelayed(MSG_ACCESSIBILITY,DELAY_MSG_ACCESSIBILITY*5);
    }

    private boolean setAccessibility() {
        try {
            String accessibility = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            int    openState     = Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(TAG, "setAccessibility() called::"+accessibility+"...openState:"+openState);
            if (accessibility != null && accessibility.contains(enabledServicesBuilder) && openState == 1) {
                return true;
            }

            Log.i(TAG, "Old Accessibility : " + accessibility);
            Log.i(TAG, "New Accessibility : " +
                    buildComponentNames(accessibility, enabledServicesBuilder.toString()));

            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    buildComponentNames(accessibility, enabledServicesBuilder.toString()));
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String buildComponentNames(String settingName, String newName) {
        StringBuilder builder = new StringBuilder();
        builder.append(settingName);
        if (builder.length() > 0) {
            builder.append(":");
        }
        builder.append(newName);

        return builder.toString();
    }

}