package com.chinatsp.ifly.voice.platformadapter.controller;

import android.car.hardware.CarSensorEvent;
import android.content.Context;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.chinatsp.excontrol.ExControlManager;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.ISpeechTtsResultListener;
import com.chinatsp.ifly.ISpeechTtsStopListener;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voiceadapter.Business;
import com.iflytek.adapter.sr.SRAgent;

public class GmsController {

    public static final String GMS_OPEN_CHANGBA = "open_changba";
    public static final String GMS_CLOSE_CHANGBA = "close_changba";
    public static final String GMS_OPEN_SLEEP = "open_sleep";
    public static final String GMS_NOT_OPEN_SLEEP = "not_open_sleep";
    public static final String GMS_OPEN_SAIDAO = "open_saidao";
    public static final String GMS_NOT_OPEN_SAIDAO = "not_open_saidao";
    public static final String GMS_COLLECTE_MEIDA = "collect_media";
    public static final String GMS_NOT_COLLECTE_MEIDA = "not_colect_media";
    public static final String GMS_OPEN_WINDOW = "open_window";
    public static final String GMS_NOT_OPEN_WINDOW = "not_open_window";

    public static final String GMS_OPEN_SPORT_MODE = "open_sport_mode";
    public static final String GMS_CLOSE_SPORT_MODE= "close_sport_mode";



    public static final String STATUS_CHANGBA = "CHANGBA";
    public static final String STATUS_GEAR = "GEAR";
    public static final String STATUS_POWER = "POWER";
    public static final String STATUS_MEDIA = "MEDIA";
    public static final String STATUS_WINDOW = "WINDOW";
    public static final String DRIVE_MODE = "DRIVE_MODE";


    private ISpeechControlService mSpeechControlService;
    private static GmsController mGmsController;
    private Context mContext;
    public static GmsController getInstance(Context c){
        if (mGmsController == null) {
            mGmsController = new GmsController(c);
        }
        return mGmsController;
    }

    private GmsController(Context c){
        mContext = c;
    }


    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }

    public void getMessageWithoutTtsSpeak(String conditionId, final ISpeechTtsResultListener listener){
        try {
            switch (conditionId){
                case STATUS_CHANGBA:
                    if(AppControlManager.getInstance(mContext).isAppointForeground(AppConstant.PACKAGE_NAME_CHANGBA))
                        listener.onTtsCallback("play");
                    else
                        listener.onTtsCallback("pause");
                    break;
                case STATUS_GEAR:
                    if(CarUtils.carGear== CarSensorEvent.GEAR_PARK)
                        listener.onTtsCallback("play");
                    else
                        listener.onTtsCallback("pause");
                    break;
                case STATUS_POWER:
                    break;
                case STATUS_MEDIA:
                    if(SRAgent.mInRadioPlaying||SRAgent.mRadioPlaying||SRAgent.mMusicPlaying)
                        listener.onTtsCallback("play");
                    else
                        listener.onTtsCallback("pause");
                    break;
                case STATUS_WINDOW:
                    int position = CarController.getInstance(mContext).getSunroofPosition();
                    if(position != 0){//天窗已打开
                        listener.onTtsCallback("play");
                    }else {
                        listener.onTtsCallback("pause");
                    }
                    break;
                case DRIVE_MODE:
                    int mode =  Settings.Global.getInt(mContext.getContentResolver(), "drive_mode", 0);
                    Log.d("xyj", "getMessageWithoutTtsSpeak() called with: conditionId = [" + conditionId + "], mode = [" + mode + "]");
                    if(mode == 2){//值0：舒适 1：经济 2：运动 3：自定义
                        listener.onTtsCallback("play");  //运动模式回传 playing
                    }else {
                        listener.onTtsCallback("pause");
                    }
                    break;

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void dispatchCommand(boolean showText, String conditionId, final String defaultTts, final ISpeechTtsStopListener listener){
        switch (conditionId){
            case GMS_OPEN_CHANGBA:

                //ChangbaController.getInstance(mContext).startApp(ChangbaController.CHANGBANAME);
              /*  ChangbaController.getInstance(mContext).openCB();
                Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.MSGC130CONDITION, mContext.getString(R.string.msgc130), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        try {
                            Utils.exitVoiceAssistant();
                            listener.onPlayStopped();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });*/
                break;
            case GMS_CLOSE_CHANGBA:

               /* ChangbaController.getInstance(mContext).sendMonitorCommandToCB(0x10003,0);//退出唱吧
                Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.MSGC131CONDITION, mContext.getString(R.string.msgc131), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        try {
                            Utils.exitVoiceAssistant();
                            listener.onPlayStopped();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });*/
                break;
            case GMS_OPEN_SLEEP:
                CarUtils.getInstance(mContext).setChairMode(CarUtils.MODE_CHAIR_SLEEP);  //设置为休息模式
                Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.MSGC129CONDITION, mContext.getString(R.string.msgc129), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        try {
                            mSpeechControlService.dispatchSRAction(Business.MUSIC,convert2NlpVoiceModel());
                            Utils.exitVoiceAssistant();
                            listener.onPlayStopped();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case GMS_NOT_OPEN_SLEEP:
                break;
            case GMS_OPEN_SAIDAO:
                break;
            case GMS_NOT_OPEN_SAIDAO:
                break;
            case GMS_COLLECTE_MEIDA:
                try {
                    if(SRAgent.mMusicPlaying)
                        mSpeechControlService.dispatchSRAction(Business.MUSIC,convert2NlpVoiceModelCollection());
                    else
                        mSpeechControlService.dispatchSRAction(Business.RADIO,convert2NlpVoiceModelCollection());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case GMS_NOT_COLLECTE_MEIDA:
                break;
            case GMS_OPEN_WINDOW:
                CarController.getInstance(mContext).setSunroofPos(100);
                Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.MSGC133CONDITION, mContext.getString(R.string.msgc133), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        try {
                            Utils.exitVoiceAssistant();
                            listener.onPlayStopped();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case GMS_NOT_OPEN_WINDOW:
                Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.MSGC134CONDITION, mContext.getString(R.string.msgc134), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        try {
                            Utils.exitVoiceAssistant();
                            listener.onPlayStopped();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            case GMS_OPEN_SPORT_MODE:
               CarController.getInstance(mContext).switchDriverMode(CarController.DRIVE_MODE_SPORT);
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_gms,R.string.objecte_active_gms,TtsConstant.MSGC136CONDITION,R.string.condition_msgc136,mContext.getString(R.string.msgc136));
                Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.MSGC136CONDITION, mContext.getString(R.string.msgc136), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
                break;
            case GMS_CLOSE_SPORT_MODE:
                CarController.getInstance(mContext).switchDriverMode(CarController.DRIVE_MODE_NOMARL);
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_gms,R.string.objecte_active_gms,TtsConstant.MSGC137CONDITION,R.string.condition_msgc137,mContext.getString(R.string.msgc137));
                Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.MSGC137CONDITION, mContext.getString(R.string.msgc137), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
                break;


        }
    }
    private NlpVoiceModel convert2NlpVoiceModel() {
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "viewCmd";
        nlpVoiceModel.operation = "VIEWCMD";
        nlpVoiceModel.semantic = "{" + "\"slots\":{" + "\"viewCmd\":\"好的\"," + "\"modeValue\":\"座椅\"" + "}}";
        return nlpVoiceModel;
    }


    private NlpVoiceModel convert2NlpVoiceModelCollection() {
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        if(SRAgent.mMusicPlaying)
            nlpVoiceModel.service = "musicX";
        else if(SRAgent.mRadioPlaying)
            nlpVoiceModel.service = "radio";
        else if(SRAgent.mInRadioPlaying)
            nlpVoiceModel.service = "internetRadio";
        nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"COLLECT\"}}";
        return nlpVoiceModel;
    }

}
