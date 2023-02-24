package com.chinatsp.ifly.utils;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarSensorManager;
import android.car.hardware.cabin.CarCabinManager;
import android.car.hardware.hvac.CarHvacManager;
import android.car.hardware.mcu.CarMcuManager;
import android.car.hardware.power.CarPowerManager;
import android.car.media.CarAudioManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.entity.RegisterEvent;
import com.chinatsp.ifly.service.ActiveViewService;
import com.chinatsp.proxy.IVehicleNetworkCallback;
import com.chinatsp.proxy.VehicleNetworkManager;
import com.google.gson.JsonObject;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.speech.util.NetworkUtil;
import com.iflytek.sr.SrSession;

import org.greenrobot.eventbus.EventBus;

import java.security.NoSuchAlgorithmException;
import java.util.logging.LogManager;

public enum AppConfig {

    INSTANCE;

    public AnimationDrawable wakeupAnim; //唤醒动画
    public AnimationDrawable normalAnim; //常态/说话动画
    public AnimationDrawable listenAnim; //倾听动画
    public AnimationDrawable recogAnim_1; //识别动画阶段1
    public AnimationDrawable recogAnim_2; //识别动画阶段2
    public AnimationDrawable recogAnim_3; //识别动画阶段3
    public AnimationDrawable greetingAnimMaster; //主驾打招呼动画
    public AnimationDrawable greetingAnimSlave; //副驾打招呼动画

    public CarAudioManager mCarAudioManager;
    public CarMcuManager mCarMcuManager;
    public CarHvacManager mCarHvacManager;
    public CarCabinManager mCarCabinManager;
    public CarSensorManager mCarSensorManager;
    public CarPowerManager mCarPowerManager;
    public Car mCarApi;
    public String token;

    public boolean settingFlag = false;
    public boolean ttsEngineInited = false;
    public boolean isSaidWIFIClose = false;
    public boolean isSaidBTClose = false;
    private Context mContext;
    private static final int RECONNECT_CAR = 1001;

    public void initConfig(Context context){
        wakeupAnim = new AnimationDrawable();
        wakeupAnim.setOneShot(true);
        for (int i = 39; i <= 39; i += 1) {
            int resId = Utils.getId(context, "hx__" + String.format("%05d", i));
            wakeupAnim.addFrame(context.getResources().getDrawable(resId), 50);
        }

        normalAnim = new AnimationDrawable();
        normalAnim.setOneShot(false);
        for (int i = 284; i <= 301; i += 1) {
            int resId = Utils.getId(context, "sh__" + String.format("%05d", i));
            normalAnim.addFrame(context.getResources().getDrawable(resId), 60);
        }

        listenAnim = new AnimationDrawable();
        listenAnim.setOneShot(false);
        for (int i = 150; i <= 209; i += 1) {
            int resId = Utils.getId(context, "qt__" + String.format("%05d", i));
            listenAnim.addFrame(context.getResources().getDrawable(resId), 60);
        }

        recogAnim_1 = new AnimationDrawable();
        recogAnim_1.setOneShot(true);
        for (int i = 216; i <= 232; i += 1) {
            int resId = Utils.getId(context, "sb__" + String.format("%05d", i));
            recogAnim_1.addFrame(context.getResources().getDrawable(resId), 50);
        }

        recogAnim_2 = new AnimationDrawable();
        recogAnim_2.setOneShot(false);
        for (int i = 233; i <= 252; i += 1) {
            int resId = Utils.getId(context, "sb__" + String.format("%05d", i));
            recogAnim_2.addFrame(context.getResources().getDrawable(resId), 50);
        }

        recogAnim_3 = new AnimationDrawable();
        recogAnim_3.setOneShot(true);
        for (int i = 253; i <= 284; i += 1) {
            int resId = Utils.getId(context, "sb__" + String.format("%05d", i));
            recogAnim_3.addFrame(context.getResources().getDrawable(resId), 50);
        }

        greetingAnimMaster = new AnimationDrawable();
        greetingAnimMaster.setOneShot(true);
        for (int i = 42; i <= 57; i += 1) { //打招呼动画阶段1
            int resId = Utils.getId(context, "dzh__" + String.format("%05d", i));
            greetingAnimMaster.addFrame(context.getResources().getDrawable(resId), 10);
        }
        for (int i = 66; i <= 84; i += 1) { //打招呼动画阶段2
            int resId = Utils.getId(context, "dzh02__" + String.format("%05d", i));
            greetingAnimMaster.addFrame(context.getResources().getDrawable(resId), 10);
        }
        for (int i = 108; i <= 116; i += 1) {//打招呼动画阶段3
            int resId = Utils.getId(context, "dzh03__" + String.format("%05d", i));
            greetingAnimMaster.addFrame(context.getResources().getDrawable(resId), 20);
        }

        greetingAnimSlave = new AnimationDrawable();
        greetingAnimSlave.setOneShot(true);
        for (int i = 526; i <= 541; i += 1) { //打招呼动画阶段1
            int resId = Utils.getId(context, "dzh__" + String.format("%05d", i));
            greetingAnimSlave.addFrame(context.getResources().getDrawable(resId), 10);
        }
        for (int i = 551; i <= 569; i += 1) { //打招呼动画阶段2
            int resId = Utils.getId(context, "dzh02__" + String.format("%05d", i));
            greetingAnimSlave.addFrame(context.getResources().getDrawable(resId), 10);
        }
        for (int i = 582; i <= 590; i += 1) {//打招呼动画阶段3
            int resId = Utils.getId(context, "dzh03__" + String.format("%05d", i));
            greetingAnimSlave.addFrame(context.getResources().getDrawable(resId), 20);
        }
    }

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECONNECT_CAR:
                    initCar(mContext);
                    break;
            }
        }
    };

    public void initCar(Context context) {
        mContext = context;
        mCarApi = Car.createCar(context, mServiceConnection);
        mCarApi.connect();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d("AppConfig", "onServiceConnected " + name);

            try {
                if (mCarAudioManager == null) {
                    mCarAudioManager = (CarAudioManager) mCarApi.getCarManager(Car.AUDIO_SERVICE);

                    LogUtils.d("AppConfig", "setMicMode to " + CarAudioManager.MIC_MODE_DENOISE);
//                    setMicWorkMode(CarAudioManager.MIC_MODE_DENOISE);
                   /* if (SeoptConstant.USE_SEOPT){
                        LogUtils.d("AppConfig", "setMicMode to " + CarAudioManager.MIC_MODE_DENOISE);
                        AppConfig.INSTANCE.setMicWorkMode(CarAudioManager.MIC_MODE_DENOISE);
                    } else{
                        LogUtils.d("AppConfig", "setMicMode to " + CarAudioManager.MIC_MODE_WORK_MODEL);
                        AppConfig.INSTANCE.setMicWorkMode(CarAudioManager.MIC_MODE_WORK_MODEL);
                    }
*/
                }
                if (mCarMcuManager == null) {
                    mCarMcuManager = (CarMcuManager) mCarApi.getCarManager(Car.CAR_MCU_SERVICE);
                }
                if (mCarHvacManager == null) {
                    mCarHvacManager = (CarHvacManager) mCarApi.getCarManager(Car.HVAC_SERVICE);
                }
                if (mCarCabinManager == null) {
                    mCarCabinManager = (CarCabinManager) mCarApi.getCarManager(Car.CABIN_SERVICE);
                    RegisterEvent registerEvent = new RegisterEvent();
                    registerEvent.eventType = true;
                    EventBus.getDefault().post(registerEvent);
                }

                if(mCarSensorManager == null){
                    mCarSensorManager = (CarSensorManager) mCarApi.getCarManager(Car.SENSOR_SERVICE);
                }

                if(mCarPowerManager == null){
                    mCarPowerManager = (CarPowerManager) mCarApi.getCarManager(Car.POWER_SERVICE);
                }

            } catch (Exception e) {
                LogUtils.e("AppConfig", "Car not connected");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.e("AppConfig", "onServiceDisconnected");
            //1500ms后再次尝试重连carservice
            mHandler.sendEmptyMessageDelayed(RECONNECT_CAR,1500);
        }
    };

    public void registerManageListener(){
        try {
            Log.d("AppConfig", "registerManageListener() called");
            VehicleNetworkManager.getInstance().registerCallBackListener(new IVehicleNetworkCallback() {
                @Override
                public void onCompleted(String s) {
                    Log.d("AppConfig", "registerCallBackListener onCompleted() called with: s = [" + s + "]");
                    if(token!=null&&token.equals(s)){
                        Log.e("AppConfig", "onCompleted: the token is same!!");
                        return;
                    }
                    token = s;
                    //更新huToken到讯飞
                    if (SRAgent.getInstance().init_state && SRAgent.getInstance().SrInstance != null) {
                        uploadHuTokenToIfly(s);
                    }
                    //更新TTS文案
                    if (needUpdateHuTts(BaseApplication.getInstance())) {
                        ActiveViewService.startGetTtsMessage();
                    }
                    ActiveViewService.startGetCOmmand();
                }

                @Override
                public void onException(int i, String s) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateToken() {
        VehicleNetworkManager.getInstance().getToken(new IVehicleNetworkCallback() {
            @Override
            public void onCompleted(String s) {
                LogUtils.d("AppConfig", "getToken:" + s);
                token = s;
                //更新huToken到讯飞
                if (SRAgent.getInstance().init_state && SRAgent.getInstance().SrInstance != null) {
                    uploadHuTokenToIfly(s);
                }
            }

            @Override
            public void onException(int i, String s) {
                LogUtils.e("AppConfig", "getToken onException:" + i);
            }
        });


      /*  VehicleNetworkManager.getInstance().registerCallBackListener(new IVehicleNetworkListenerInterface() {
            @Override
            public void exception(int i) {
                LogUtils.w("AppConfig", "exception:" + i);
            }

            @Override
            public void accessTokenUpdated(String s) {
                LogUtils.w("AppConfig", "accessTokenUpdated:" + s);
                token = s;
                //更新huToken到讯飞
                if (SRAgent.getInstance().init_state && SRAgent.getInstance().SrInstance != null) {
                    uploadHuTokenToIfly(s);
                }
                //更新TTS文案
                if (needUpdateHuTts(BaseApplication.getInstance())) {
                    ActiveViewService.startGetTtsMessage();
                }
                ActiveViewService.startGetCOmmand();
            }

            @Override
            public void serviceStatusUpdated(String s) throws RemoteException {
                LogUtils.w("AppConfig", "serviceStatusUpdated:" + s);
            }

            @Override
            public void ucsHostUpdated(String s) throws RemoteException {
                LogUtils.w("AppConfig", "ucsHostUpdated:" + s);
            }

            @Override
            public void onCompleted(String s) throws RemoteException {
                LogUtils.w("AppConfig", "onCompleted:" + s);
            }

            @Override
            public void onException(int i, String s) throws RemoteException {
                LogUtils.w("AppConfig", "onException:" + i + "," + s);
            }

            @Override
            public IBinder asBinder() {
                return null;
            }
        });
    }

    *//**
     * 强制获取Token
     *//*
    public void RequestUpdateAccessToken(){
        VehicleNetworkManager.getInstance().requestUpdateAccessToken(new IVehicleNetworkCallback() {
            @Override
            public void onCompleted(String s) {
                LogUtils.w("zheng22", "requestUpdateAccessToken onCompleted:" + s);
                token = s;
            }

            @Override
            public void onException(int i, String s) {
                LogUtils.w("AppConfig", "requestUpdateAccessToken onException:" + s);
            }
        });*/
    }


    public void uploadHuTokenToIfly(String huToken) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", huToken);
        SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_USERPARAMS, jsonObject.toString());
    }

    private boolean needUpdateHuTts(Context context) {
        if (!NetworkUtil.isNetworkAvailable(context)) return false;

        long currentTime = System.currentTimeMillis();
        long lastUpdateTime = SharedPreferencesUtils.getLong(context, AppConstant.LAST_UPDATE_TTS_TIME, -1);
        return ((currentTime - lastUpdateTime) >= (AppConstant.ONE_DAY));
    }

    public void setMicWorkMode(int mode) {
        if(mCarAudioManager != null) {
            ThreadPoolUtils.executeSingle(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCarAudioManager.setMicMode(mode);
                    } catch (CarNotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
