package com.chinatsp.ifly.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.module.me.recommend.model.HuVoiceAsssitContentModel;
import com.chinatsp.ifly.module.me.recommend.service.VideoLoadService;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.speech.util.NetworkUtil;
import com.iflytek.sr.SrSession;

import java.security.NoSuchAlgorithmException;

public class NetStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetStateReceiver";

    private String conditionId = "";
    private String defaultText = "";
    private Handler handler = new Handler();
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTED = 0;
    private int BOOTED_TIME =  50 * 1000;
    private boolean ISALREADYSPEAKED_CONNECT = false;
    private boolean ISALREADYSPEAKED_DISCONNECT = true;
    private BluetoothDevice mHeadsetDevice = null;
    private boolean ISALREADYSPEAKED_BT_CONNECT = false;
    private boolean ISALREADYSPEAKED_BT_DISCONNECT = true;
    private long lastReceiveWifiConnectedTime = 0;
    private long lastReceiveWifiDisConnectedTime = 0;
    private long lastReceiveNetAvalibleTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "action==" + action);

        //收到开机广播进行主动服务
        boolean isFirstUse = SharedPreferencesUtils.getBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,false);
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (!isFirstUse && state == BluetoothAdapter.STATE_OFF && !ISALREADYSPEAKED_BT_DISCONNECT) {
                mHeadsetDevice = null;
                Log.d(TAG,"NetStateReceiver1 AppConfig.INSTANCE.isSaidBTClose = " + AppConfig.INSTANCE.isSaidBTClose);
                if(!AppConfig.INSTANCE.isSaidBTClose){
                    Log.d(TAG, "BT is unconnect");
                    conditionId = TtsConstant.SYSTEMC50CONDITION;
                    defaultText = context.getString(R.string.systemC50);
                    delayTtsSpeak(context);
                    BluePhoneManager.getInstance(context).BTUnconnect();
                }else {
                    AppConfig.INSTANCE.isSaidBTClose = false;
                }
                ISALREADYSPEAKED_BT_DISCONNECT = true;
                ISALREADYSPEAKED_BT_CONNECT = false;
                Utils.eventTrack(context,R.string.skill_system, R.string.scene_bt_status, R.string.object_bt_status_unconnect, TtsConstant.SYSTEMC50CONDITION, R.string.condition_system_default);
            }
        } else if (AppConstant.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            boolean isBtEnable = BluetoothAdapter.getDefaultAdapter().isEnabled();
            if (isBtEnable) {
                int connectState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
                BluetoothDevice hfpDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                boolean isProfileDevice = isProfileDevice(hfpDevice);
                if (!isFirstUse && connectState == STATE_CONNECTED && !ISALREADYSPEAKED_BT_CONNECT) {
                    Log.d(TAG, "BT is connect");
                    mHeadsetDevice = hfpDevice;
                    ISALREADYSPEAKED_BT_CONNECT = true;
                    ISALREADYSPEAKED_BT_DISCONNECT = false;
                    conditionId = TtsConstant.SYSTEMC49CONDITION;
                    defaultText = context.getString(R.string.systemC49);
                    Utils.eventTrack(context,R.string.skill_system, R.string.scene_bt_status, R.string.object_bt_status_connect, TtsConstant.SYSTEMC49CONDITION, R.string.condition_system_default);
                    delayTtsSpeak(context);
                } else if (!isFirstUse && connectState == STATE_DISCONNECTED && !ISALREADYSPEAKED_BT_DISCONNECT) {
                    if (isProfileDevice) {
                        mHeadsetDevice = null;
                        Log.d(TAG,"NetStateReceiver2 AppConfig.INSTANCE.isSaidBTClose = " + AppConfig.INSTANCE.isSaidBTClose);
                        if(!AppConfig.INSTANCE.isSaidBTClose){
                            Log.d(TAG, "BT is unconnect");
                            conditionId = TtsConstant.SYSTEMC50CONDITION;
                            defaultText = context.getString(R.string.systemC50);
                            delayTtsSpeak(context);
                        }else {
                            AppConfig.INSTANCE.isSaidBTClose = false;
                        }
                        ISALREADYSPEAKED_BT_DISCONNECT = true;
                        ISALREADYSPEAKED_BT_CONNECT = false;
                        Utils.eventTrack(context,R.string.skill_system, R.string.scene_bt_status, R.string.object_bt_status_unconnect, TtsConstant.SYSTEMC50CONDITION, R.string.condition_system_default);
                    }
                }
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null) {
                boolean isWifiConnected = networkInfo.isConnected();
                if (!isFirstUse && isWifiConnected && !ISALREADYSPEAKED_CONNECT) {
                    long now = System.currentTimeMillis();
                    if (now - lastReceiveWifiConnectedTime < 500) {
                        LogUtils.d(TAG, "ignore frequency wifi connected event");
                        return;
                    }
                    lastReceiveWifiConnectedTime = now;
                    conditionId = TtsConstant.SYSTEMC47CONDITION;
                    defaultText = context.getString(R.string.systemC47);
                    ISALREADYSPEAKED_CONNECT = true;
                    ISALREADYSPEAKED_DISCONNECT = false;
                    Utils.eventTrack(context,R.string.skill_system, R.string.scene_wifi_status, R.string.object_wifi_status_connect, TtsConstant.SYSTEMC47CONDITION, R.string.condition_system_default);
                    delayTtsSpeak(context);

                    if(!HuVoiceAsssitContentModel.getInstance().isNetwork){
                        Intent i = new Intent(context, VideoLoadService.class);
                        context.startService(i);
                    }
                } else if (!isFirstUse && !isWifiConnected && !ISALREADYSPEAKED_DISCONNECT) {
                    Log.d(TAG,"NetStateReceiver AppConfig.INSTANCE.isSaidWIFIClose = " + AppConfig.INSTANCE.isSaidWIFIClose);
                    long now = System.currentTimeMillis();
                    if (now - lastReceiveWifiDisConnectedTime < 500) {
                        LogUtils.d(TAG, "ignore frequency wifi disconnected event");
                        return;
                    }
                    lastReceiveWifiDisConnectedTime = now;

                    if (!AppConfig.INSTANCE.isSaidWIFIClose) {
                        conditionId = TtsConstant.SYSTEMC48CONDITION;
                        defaultText = context.getString(R.string.systemC48);
                        delayTtsSpeak(context);
                    }else {
                        AppConfig.INSTANCE.isSaidWIFIClose = false;
                    }
                    ISALREADYSPEAKED_DISCONNECT = true;
                    ISALREADYSPEAKED_CONNECT = false;
                    Utils.eventTrack(context,R.string.skill_system, R.string.scene_wifi_status, R.string.object_wifi_status_unconnect, TtsConstant.SYSTEMC48CONDITION, R.string.condition_system_default);
                }
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            boolean networkAvailable = NetworkUtil.isNetworkAvailable(context);
            if(!HuVoiceAsssitContentModel.getInstance().isNetwork && networkAvailable){
                Intent i = new Intent(context, VideoLoadService.class);
                context.startService(i);
            }
            if (networkAvailable && MXSdkManager.getInstance(context).isInited()) {
                LogUtils.d(TAG, "CONNECTIVITY_ACTION: networkAvailable");
                long now = System.currentTimeMillis();
                if (now - lastReceiveNetAvalibleTime < 500) {
                    LogUtils.d(TAG, "ignore frequency networkAvailable event");
                    return;
                }
                lastReceiveNetAvalibleTime = now;

                //MXSdkManager.getInstance(context).updateMyLocation();
            }
        }
    }

    private void delayTtsSpeak(final Context context) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        LogUtils.d("NetStateReceiver", "elapsedRealtime:" + elapsedRealtime);

        if (elapsedRealtime < BOOTED_TIME) { //开机时长小于80秒
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTTS(context);
                }
            }, BOOTED_TIME - elapsedRealtime);
        } else {
            startTTS(context);
        }
    }

    private void startTTS(Context context) {
        boolean ttsPlaying = TTSController.getInstance(context).isTtsPlaying();
        boolean isShown = !FloatViewManager.getInstance(context).isHide();
        if(!AppConfig.INSTANCE.ttsEngineInited){
            Log.e(TAG, "startTTS: the txz is not init!!!" );
            return;
        }
//        boolean hasfocus = context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg());
        Log.d(TAG, "startTTS: isShown = " + isShown + ",ttsPlaying = "+ttsPlaying );
        if(Utils.getCurrentPriority(context)> PriorityControler.PRIORITY_TWO){
            Log.e(TAG, "startTTS: "+Utils.getCurrentPriority(context));
            return;
        }
        int hicarConnected= Settings.Global.getInt(context.getContentResolver(),"hicar_connected",0);
        if(hicarConnected==1
                &&conditionId !=TtsConstant.SYSTEMC47CONDITION){  //hicar 连接情况下，蓝牙连接 断开不做播报
            Log.e(TAG, "startTTS: the hicar is connected!!!");
            return;
        }
//        if(!isShown&&!ttsPlaying){//在界面不显示的情况下播报
          if(!isShown){//在界面不显示的情况下播报，解决bug id1058306,9/17
            if(requestAudioFocus(context)==1) //申请到焦点就播报
                Utils.getMessageWithTtsSpeakOnly(false, context, conditionId, defaultText,PriorityControler.PRIORITY_TWO);
            else{
                Log.e(TAG, "startTTS: the audiofocus is loss!!!");
            }

           /* if (!hasfocus) {
                handler.removeCallbacksAndMessages(null);
                Utils.getMessageWithTtsSpeakOnly(false, context, conditionId, defaultText);
            } else {
                handler.postDelayed(ttsRunnable, 2000);
            }*/
        }
    }

    private Runnable ttsRunnable = new Runnable() {
        @Override
        public void run() {
            startTTS(BaseApplication.getInstance());
        }
    };

    /*
     *  判断是否是上次连接的设备
    *  device：设备参数
     */
    private boolean isProfileDevice(BluetoothDevice device) {
        Log.w(TAG, "isProfileDevice: mHeadsetDevice=" + mHeadsetDevice + ", device=" + device);
        if (mHeadsetDevice == null) {
            return true;
        }
        if (device == null) {
            return true;
        }
        String keepDeviceMac = mHeadsetDevice.getAddress();
        String deviceMac = device.getAddress();
        if(keepDeviceMac == null || deviceMac == null) {
            return false;
        }
        return keepDeviceMac.equals(deviceMac);
    }

    /**
     * 申请音频焦点
     */
    private int requestAudioFocus(Context context){
        int ret = 1;
        if(!context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())) {
            ret =  AudioFocusUtils.getInstance(context).requestVoiceAudioFocus(AudioManager.STREAM_ALARM);
        }
        return ret;
    }
}
