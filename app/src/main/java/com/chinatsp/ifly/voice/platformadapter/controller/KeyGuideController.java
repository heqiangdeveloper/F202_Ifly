package com.chinatsp.ifly.voice.platformadapter.controller;

import android.car.hardware.constant.HVAC;
import android.car.hardware.constant.VEHICLE;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.CarKeyViewManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.phone.bt.context.App;
import com.iflytek.adapter.sr.SRAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

import okhttp3.internal.Util;

import static android.car.hardware.mcu.CarMcuManager.ID_VENDOR_ECALL_STATE;

/**
 * Created by Administrator on 2020/7/16.
 */

public class KeyGuideController extends BaseController {
    private Context mContext;
    private final String TAG = "KeyGuideController";
    private static KeyGuideController mKeyGuideController;
    private static final int MSG_START_SPEAK = 1001;
    private int TIME_DELAY_SHOWING = 600;
    private final static String SRC = "SRC";//切源
    private final static String MUTE = "MUTE";//静音
    private boolean isNeedShow = false;

    public static KeyGuideController getInstance(Context c){
        if(mKeyGuideController == null)
            mKeyGuideController = new KeyGuideController(c);
        return mKeyGuideController;
    }

    public KeyGuideController(Context context){
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstant.ACTION_AWARE_VOICE_KEY);
        intentFilter.addAction(AppConstant.STATUS_BAR_PLAY_INFO_ACTION);
//        mContext.registerReceiver(receiver, intentFilter);
    }

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage (Message msg){
            switch (msg.what) {
                case MSG_START_SPEAK:
                    break;
            }
        }
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "lh:action---" + action);
            if (AppConstant.ACTION_AWARE_VOICE_KEY.equals(action)) {
                String keyCode = intent.getStringExtra("Key_code");
                String keyState = intent.getStringExtra("Key_state");
                boolean isAudioPlaying = isAudioPlaying();
                LogUtils.d(TAG, "keyCode:" + keyCode + " ,keyState:" + keyState + ",isAudioPlaying = " + isAudioPlaying);
                if (!isAudioPlaying && MUTE.equals(keyCode) && "DOWN".equals(keyState)) {//静音
                    if(!AudioFocusUtils.getInstance(context).isMasterMute()){
                        //handleCarCabinAction(false,TtsConstant.GUIDEBTNC2CONDITION,R.string.btnC2,"","");
                    }else {
                        //handleCarCabinAction(false,TtsConstant.GUIDEBTNC3CONDITION,R.string.btnC3,"","");
                    }
                }else if(SRC.equals(keyCode) && "DOWN".equals(keyState)){//切源按下
                    //isSRCDown = true;
                }
            }else{
                String source = intent.getStringExtra("source");
                String sourceChar = "USB音乐";
                //USB,USB音乐 TUNER,广播电台 HDD,本机音乐 BTAUDIO,蓝牙音乐 OTHERS, KAOLA,欧尚电台 OL_MUSIC在线音乐
                if("USB".equals(source)){
                    sourceChar = "USB音乐";
                }else if("TUNER".equals(source)){
                    sourceChar = "广播电台";
                }else if("HDD".equals(source)){
                    sourceChar = "本机音乐";
                }else if("BTAUDIO".equals(source)){
                    sourceChar = "蓝牙音乐";
                }else if("KAOLA".equals(source)){
                    sourceChar = "欧尚电台";
                }else if("OL_MUSIC".equals(source)){
                    sourceChar = "在线音乐";
                }
//                if(isSRCDown){
//                    isSRCDown = false;
//                    handleCarCabinAction(false,TtsConstant.GUIDEBTNC1CONDITION,R.string.btnC1,"#SOURCE#",sourceChar);
//                }
            }
        }
    };

    private boolean isAudioPlaying(){
        String activeAudioPkg = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
        if(AppConstant.PACKAGE_NAME_MUSIC.equals(activeAudioPkg) || AppConstant.PACKAGE_NAME_RADIO.equals(activeAudioPkg)
                || AppConstant.PACKAGE_NAME_VCAR.equals(activeAudioPkg) || AppConstant.PACKAGE_NAME_CHANGBA.equals(activeAudioPkg)){
            return true;
        }
        return false;
    }

    public void handleCarMcuAction(int propertyId){
        switch (propertyId){
            case ID_VENDOR_ECALL_STATE:
                boolean isAuthored = true;
                if(!isAuthored){
                    handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC56CONDITION, R.string.btnC56,"","",
                            R.string.skill_key,R.string.scene_sos_switch,R.string.scene_sos_switch,R.string.condition_btnC56);
                }else{
                    handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC56_1CONDITION, R.string.btnC56_1,"","",
                            R.string.skill_key,R.string.scene_sos_switch,R.string.scene_sos_switch,R.string.condition_btnC56_1);
                }
                break;
        }
    }

    //语音弹窗引导
    public void handleCarCabinAction(int type,String conditionId,int defaultTts,String replaceChar,String replaceText,int appName, int scene, int object, int condition){
        if(!isIBCMEnable()){
            return;
        }
        Log.d(TAG,"type = " + type + ",conditionId = " + conditionId + ",defaultTts = " + defaultTts + ",replaceText = " + replaceText);
        if(conditionId != TtsConstant.GUIDEBTNC2CONDITION && conditionId != TtsConstant.GUIDEBTNC3CONDITION){
            //判断当前需不需要播报,静音键不需要判断
            if(type == AppConstant.HIDE_BROADCAST || type == AppConstant.SHOW_BROADCAST){
                if(!CarUtils.getInstance(mContext).isVoiceBroadcastOpen()){
                    return;
                }
                //判断需不需要显示语音弹窗
                if(type == AppConstant.HIDE_BROADCAST){
                    isNeedShow = false;
                }else {
                    isNeedShow = true;
                }
            }else if(type == AppConstant.HIDE_GUIDE_COMMAND || type == AppConstant.HIDE_GUIDE_ORDER ||
                    type == AppConstant.SHOW_GUIDE_COMMAND || type == AppConstant.SHOW_GUIDE_ORDER){
                if(!CarUtils.getInstance(mContext).isVoiceGuideOpen()){
                    return;
                }
                //判断需不需要显示语音弹窗
                if(type == AppConstant.HIDE_GUIDE_COMMAND || type == AppConstant.HIDE_GUIDE_ORDER){
                    isNeedShow = false;
                }else {
                    isNeedShow = true;
                }
            }else {//弹窗语音播报（不限车速）+ 弹窗语音引导（指令类）：两个开关有一个开着就播报
                if(!CarUtils.getInstance(mContext).isVoiceGuideOpen() && !CarUtils.getInstance(mContext).isVoiceBroadcastOpen()){
                    return;
                }
                isNeedShow = true;
            }
        }
        Log.d(TAG,"isNeedShow = " + isNeedShow);
        //判断播报场景的优先级
        int priority = AppConstant.KeyGuidePriority;
        if(!Utils.checkPriority(mContext,priority)){
            Log.e(TAG, "getMessageWithTtsSpeakListener: " + Utils.checkPriority(mContext,priority));
            return;
        }

        String replaceString = replaceText;
        int delayTime = 0;
        if(conditionId == TtsConstant.GUIDEBTNC29CONDITION){
            //延时3秒判断，开启AUTOHOLD是否成功
            delayTime = 3000;
            String defaultText = Utils.replaceTts(mContext.getString(defaultTts),replaceChar,replaceText);
            String defaultTextFail = mContext.getString(R.string.btnC34);
            String conditionIdFail = TtsConstant.GUIDEBTNC34CONDITION;
            Log.d(TAG,"defaultText = " + defaultText);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(CarUtils.getInstance(mContext).getAutoHoldAvailableStatus() != VEHICLE.ON && defaultText.contains("开启")){
                        if(isNeedShow){
                            showAssistantSpeakTTS(priority,conditionIdFail,defaultTextFail,replaceChar,replaceString,appName,scene,object,condition);
                        }else {
                            speakTTSBehind(priority,conditionIdFail,defaultTextFail,replaceChar,replaceString,appName,scene,object,condition);
                        }
                    }else {
                        if(isNeedShow){
                            showAssistantSpeakTTS(priority,conditionId,defaultText,replaceChar,replaceString,appName,scene,object,condition);
                        }else {
                            speakTTSBehind(priority,conditionId,defaultText,replaceChar,replaceString,appName,scene,object,condition);
                        }
                    }
                }
            },delayTime);
            return;
        }

        String defaultText = Utils.replaceTts(mContext.getString(defaultTts),replaceChar,replaceText);
        Log.d(TAG,"defaultText = " + defaultText);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isNeedShow){
                    showAssistantSpeakTTS(priority,conditionId,defaultText,replaceChar,replaceString,appName,scene,object,condition);
                }else {
                    speakTTSBehind(priority,conditionId,defaultText,replaceChar,replaceString,appName,scene,object,condition);
                }
            }
        },delayTime);
    }

    //后台播报
    private void speakTTSBehind(int priority,String conditionId,String defaultText, String replaceText, String nameValue,int appName, int scene, int object, int condition){
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText + ",replaceText = " + replaceText);
                String defaultTts = tts;
                if (TextUtils.isEmpty(tts)) {
                    defaultTts = defaultText;
                }
                defaultTts = Utils.replaceTts(defaultTts, replaceText, nameValue);
                defaultTts = replaceChar(defaultTts, nameValue);
                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                Utils.eventTrack(mContext, appName, scene, object, conditionId, condition, defaultTts);//埋点
                Utils.startTTSOnly(defaultTts,priority, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                        if(conditionId == TtsConstant.GUIDEBTNC34CONDITION){
                            //播报完成后跳转到车况体检页面，并开始体检
                            CarController.getInstance(mContext).openPressureController(CarController.OPEN_CT_TYPE_TEST);
                        }
                    }
                });
            }
        });
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    //弹窗播报
    private void showAssistantSpeakTTS(int priority,String conditionId,String defaultText, String replaceText, String nameValue,int appName, int scene, int object, int condition){
        if(FloatViewManager.getInstance(mContext).isHide()) showAssistant();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText + ",replaceText = " + replaceText);
                        String defaultTts = tts;
                        if (TextUtils.isEmpty(tts)) {
                            defaultTts = defaultText;
                        }
                        defaultTts = Utils.replaceTts(defaultTts, replaceText, nameValue);
                        defaultTts = replaceChar(defaultTts, nameValue);
                        Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                        Utils.eventTrack(mContext, appName, scene, object, conditionId, condition, defaultTts);//埋点
                        Utils.startTTSOnly(defaultTts,priority, new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                if (!FloatViewManager.getInstance(mContext).isHide()) {
                                    FloatViewManager.getInstance(mContext).hide();
                                }
                                if(conditionId == TtsConstant.GUIDEBTNC34CONDITION){
                                    //播报完成后跳转到车况体检页面，并开始体检
                                    CarController.getInstance(mContext).openPressureController(CarController.OPEN_CT_TYPE_TEST);
                                }
                            }
                        });
                    }
                });
            }
        },TIME_DELAY_SHOWING);
    }

    public String replaceChar(String defaultText,String nameValue){
        if(defaultText.contains("MODE")){
            return Utils.replaceTts(defaultText, "#MODE#", nameValue);
        }else if(defaultText.contains("ACTION")){
            return Utils.replaceTts(defaultText, "#ACTION#", nameValue);
        }else {
            return defaultText;
        }
    }

    //获取语音引导的配置字
    private boolean isIBCMEnable(){
        int ibcm_enable = Utils.getInt(mContext, AppConstant.IBCM_ENABLE,0);
        Log.d(TAG, "ibcm_enable = " + ibcm_enable);
        return ibcm_enable == 1 ? true : false;
    }
}
