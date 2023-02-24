package com.chinatsp.ifly.voice.platformadapter.controller;

import android.car.hardware.CarSensorEvent;
import android.car.hardware.constant.VEHICLE;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IChairController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.speech.util.NetworkUtil;

import static android.car.hardware.CarSensorEvent.GEAR_PARK;
import static android.car.hardware.CarSensorEvent.IGNITION_STATE_UNDEFINED;
import static android.car.hardware.constant.HVAC.LOOP_OUTSIDE;


public class ChairController extends BaseController implements IChairController {
    private static final int MSG_SHOW_WARN = 1001;
    private static final int MSG_SHOW_WORD = 1002;
    private static final String TAG = "ChairController";
    private static ChairController mChairController;
    public static final String MODE = "座椅模式";
    private static final String MODE_SAVE_SLEEP = "休息模式";
    public static final String MODE_SLEEP = "休息";
    private static final String MODE_DRIVING = "驾驶";
    private Context mContext;
    private String conditionId,defaultText,newChar,oldChar;
    //电动座椅 配置 0 ：未配置，  1：配置
    private static final String ELECCHAIR_TYPE = "persist.vendor.vehicle.dsm";
    private static final int VEHICLE_DSM_NO = 0;
    private static final int VEHICLE_DSM_HAVE = 1;
    private boolean isMusicToPlay = false;
    private int current;
    private ISpeechControlService mSpeechControlService;
    private static final  int TIME_DELAY_SHOWING = 600;
    private boolean isAnswered = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_WARN:
//                    SRAgent.getInstance().resetSrTimeCount();
//                    TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");

                    MVWAgent.getInstance().stopMVWSession();
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_WARN_SAVE_SLEEP);
                    conditionId = TtsConstant.SEATC17CONDITION;
                    defaultText = mContext.getString(R.string.seat_c17);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            Utils.startTTSOnly(tts,AppConstant.WarnSaveSleepModePriority);
                            Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_warn_chair_save, R.string.object_warn_chair_save, conditionId, R.string.condition_chair_c17,tts);
                        }
                    });

                    break;
                case MSG_SHOW_WORD:
                    IntentEntity intentEntity = (IntentEntity) msg.obj;
                    srAction(intentEntity);
                    break;
            }
        }
    };

    public static ChairController getInstance(Context c){
        if(mChairController==null)
            mChairController = new ChairController(c);
        return mChairController;
    }

    private ChairController(Context c){
        mContext = c;
    }

    @Override
    public void srAction(IntentEntity intentEntity) {

        int hasDsm = Utils.getInt(mContext,ELECCHAIR_TYPE,VEHICLE_DSM_NO);
        if(VEHICLE_DSM_NO==hasDsm){  //如果没有电动座椅，直接播报暂不支持该操作，返回
            Log.e(TAG, "srAction: have no vehicle dsm::"+hasDsm);
            //doExceptonAction(mContext);
            if(!isMvwWords(intentEntity,mContext.getString(R.string.seat_c14)))
                Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair_save, R.string.object_chair_save, TtsConstant.SEATC14CONDITION, R.string.condition_chair_c14, mContext.getString(R.string.seat_c14));
            getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.SEATC14CONDITION, mContext.getString(R.string.seat_c14));
            return;
        }

        if((MODE.equals(intentEntity.semantic.slots.mode)&&MODE_SLEEP.equals(intentEntity.semantic.slots.modeValue)) ||
                MODE_SAVE_SLEEP.equals(intentEntity.semantic.slots.mode)){
            handleSleepMode(intentEntity);//休息模式
        }else if(MODE.equals(intentEntity.semantic.slots.mode)&&MODE_DRIVING.equals(intentEntity.semantic.slots.modeValue)){
            handleDriveMode(intentEntity);//驾驶模式
        }else if(PlatformConstant.Operation.VIEWCMD.equals(intentEntity.operation)) {
            isMusicToPlay = false;
            if (intentEntity.semantic != null && intentEntity.semantic.slots != null && intentEntity.semantic.slots.viewCmd != null) {
                Log.d(TAG, "srAction() called with: intentEntity = [" + intentEntity.semantic.slots.viewCmd + "]");
                if (FeedBackController.getInstance(mContext).isSureWord(intentEntity.semantic.slots.viewCmd)) {
                    try {
                        if(FeedBackController.getInstance(mContext).isExtraSureWord(intentEntity.semantic.slots.viewCmd))
                            intentEntity.semantic.slots.viewCmd = "好的";
                        intentEntity.semantic.slots.modeValue = "座椅"; //音乐通过区分 modeValue 播报不同的内容
                        mSpeechControlService.dispatchSRAction(Business.MUSIC, intentEntity.convert2NlpVoiceModel());
                        unMuteMedia();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (FeedBackController.getInstance(mContext).isDenyWord(intentEntity.semantic.slots.viewCmd)) {
                    Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, TtsConstant.SEATC11CONDITION, R.string.condition_chair_c11, mContext.getString(R.string.seat_c11));
                    getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.MSGC90CONDITION, mContext.getString(R.string.msg_c90));
                } else if (FeedBackController.getInstance(mContext).isSureWord(intentEntity.text)) {
                    try {
                        if(FeedBackController.getInstance(mContext).isExtraSureWord(intentEntity.semantic.slots.viewCmd))
                            intentEntity.semantic.slots.viewCmd = "好的";
                        intentEntity.semantic.slots.modeValue = "座椅"; //音乐通过区分 modeValue 播报不同的内容
                        intentEntity.semantic.slots.viewCmd = intentEntity.text;
                        mSpeechControlService.dispatchSRAction(Business.MUSIC,intentEntity.convert2NlpVoiceModel());
                        unMuteMedia();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (FeedBackController.getInstance(mContext).isDenyWord(intentEntity.text)) {
                    Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, TtsConstant.SEATC11CONDITION, R.string.condition_chair_c11, mContext.getString(R.string.seat_c11));
                    getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.MSGC90CONDITION, mContext.getString(R.string.msg_c90));
                }else {
                    doExceptonAction(mContext);
                }
            } else {
                doExceptonAction(mContext);
            }
        }else
            doExceptonAction(mContext);
    }

    public void handleOnDoAction(IntentEntity intentEntity){
        if (intentEntity.semantic != null && intentEntity.semantic.slots != null && intentEntity.semantic.slots.viewCmd != null) {
            if (FeedBackController.getInstance(mContext).isSureWord(intentEntity.semantic.slots.viewCmd)) {//确定
                MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
                mvwLParamEntity.nMvwId = 6;
                mvwAction(mvwLParamEntity);
            } else if (FeedBackController.getInstance(mContext).isDenyWord(intentEntity.semantic.slots.viewCmd)) {//取消
                MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
                mvwLParamEntity.nMvwId = 1;
                mvwAction(mvwLParamEntity);
            }else {
                doExceptonAction(mContext);
            }
        }else {
            doExceptonAction(mContext);
        }
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    /**
     * 处理提醒保存休息模式
     */
    public void handleWarnSaveSleepMode(){
        if(CarUtils.powerStatus < CarSensorEvent.IGNITION_STATE_ON){
            return;
        }
        //判断播报场景优先级
        int priority = AppConstant.WarnSaveSleepModePriority;
        if(!Utils.checkPriority(mContext,priority)){
            Log.d(TAG,"checkPriority() = " + Utils.checkPriority(mContext,priority));
            return;
        }
        showAssistant();
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_WARN,TIME_DELAY_SHOWING);
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        Log.d(TAG, "mvwAction() called with: mvwLParamEntity = [" + mvwLParamEntity.nKeyword + "]");
        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_window,R.string.object_mhcc23,TtsConstant.MHXC23CONDITION,R.string.condition_null);
        isMusicToPlay = false;
        TTSController.getInstance(mContext).stopTTS();
        Log.d(TAG,"TspSceneAdapter.getTspScene(mContext) = " + TspSceneAdapter.getTspScene(mContext));
        if (mvwLParamEntity.nMvwId == 0
                ||mvwLParamEntity.nMvwId == 6
                ||mvwLParamEntity.nMvwId == 7
                ||mvwLParamEntity.nMvwId == 8
                ||mvwLParamEntity.nMvwId == 9
                ||mvwLParamEntity.nMvwId == 10){
            Log.d(TAG,"positive...");
            if(TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_WARN_SAVE_SLEEP){
                isAnswered = true;
                CarUtils.getInstance(mContext).saveMemoryChairMode(VEHICLE.ON);  //请求保存为休息模式
                Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_warn_chair_save, R.string.object_warn_chair_save, TtsConstant.SEATC18CONDITION, R.string.condition_chair_c18, mContext.getString(R.string.seat_c18));
                getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.SEATC18CONDITION, mContext.getString(R.string.seat_c18));
            }else {
                try {
                    mSpeechControlService.dispatchSRAction(Business.MUSIC, convert2NlpVoiceModel());
                    unMuteMedia();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }else {
            Log.d(TAG,"nagative...");
            if(TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_WARN_SAVE_SLEEP){
                isAnswered = true;
                CarUtils.getInstance(mContext).saveMemoryChairMode(VEHICLE.OFF);  //请求不保存为休息模式
                Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_warn_chair_save, R.string.object_warn_chair_save, TtsConstant.SEATC19CONDITION, R.string.condition_chair_c19, mContext.getString(R.string.seat_c19));
                getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.SEATC19CONDITION, mContext.getString(R.string.seat_c19));
            }else {
                Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, TtsConstant.SEATC11CONDITION, R.string.condition_chair_c11, mContext.getString(R.string.seat_c11));
                getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.MSGC90CONDITION, mContext.getString(R.string.msg_c90));
            }
        }
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }

    public void setInvalideType(){
        Log.d(TAG, "setInvalideType() called");
        if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK){
            isMusicToPlay = false;
            restartScrene();
        }else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_WARN_SAVE_SLEEP){
            if(!isAnswered){
                Log.d(TAG, "no answer and send signal...");
                CarUtils.getInstance(mContext).saveMemoryChairMode(VEHICLE.OFF);  //请求不保存为休息模式
            }
            restartScrene();
        }
    }
    /**
     * 处理驾驶模式
     */
    private void handleDriveMode(IntentEntity intentEntity) {
        Log.d(TAG, "handleDriveMode() called");
        int currentGear = getCurrentGear();
        if(CarUtils.getInstance(mContext).getSpeed() > 0){//车速大于0
            conditionId = TtsConstant.SEATC6CONDITION;
            defaultText = mContext.getString(R.string.seat_c6);
            Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                    Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c6,tts);
                }
            });
        }else if(currentGear == 0 || currentGear == GEAR_PARK) {//OFF时，发档位无效值0
            if(CarUtils.getInstance(mContext).isDrivingMode()){//当前处于驾驶模式
                Log.d(TAG, "handleSleepMode: in driving mode");
                conditionId = TtsConstant.SEATC1CONDITION;
                defaultText = mContext.getString(R.string.seat_c1);
                newChar = intentEntity.semantic.slots.modeValue;
                oldChar = "#MODEL#";
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        tts = tts.replace(oldChar, newChar);
                        Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                        Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c1,tts);
                    }
                });
            }else {
                Log.d(TAG, "handleSleepMode: brake disabled");
                int mFaceId = CarUtils.getInstance(mContext).getFaceId();
                Log.d(TAG, "handleDriveMode() called with: mFaceId = [" + mFaceId + "]");
                if (mFaceId > 0 && mFaceId <= 7) {
                    CarUtils.getInstance(mContext).setChairMode(mFaceId); //设置为驾驶模式
                }else if(mFaceId==0)
                    CarUtils.getInstance(mContext).setChairMode(10); //设置为驾驶模式
                conditionId = TtsConstant.SEATC2CONDITION;
                defaultText = mContext.getString(R.string.seat_c2);
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                        Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c2,tts);
                    }
                });
            }
        }else {
            conditionId = TtsConstant.SEATC7CONDITION;
            defaultText = mContext.getString(R.string.seat_c7);
            Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                    Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c7,tts);
                }
            });
        }
    }

    /**
     * 处理休息模式
     */
    private void handleSleepMode(IntentEntity intentEntity) {
        Log.d(TAG, "handleSleepMode: ");
        int power = getPowerType();
        int currentGear = getCurrentGear();
        if(power <= CarSensorEvent.IGNITION_STATE_ACC){//OFF,ACC
            Log.d(TAG, "handleSleepMode: not power on");
            sleepModeAction(intentEntity);
        }else {//ON，START
            Log.d(TAG, "handleSleepMode: power on");
            if(CarUtils.getInstance(mContext).getSpeed() > 0){//车速大于0
                if(MODE_SAVE_SLEEP.equals(intentEntity.semantic.slots.mode)){//保存休息模式
                    conditionId = TtsConstant.SEATC15CONDITION;
                    defaultText = mContext.getString(R.string.seat_c15);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                            Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair_save, R.string.object_chair_save, conditionId, R.string.condition_chair_c15,tts);
                        }
                    });
                }else {
                    conditionId = TtsConstant.SEATC6CONDITION;
                    defaultText = mContext.getString(R.string.seat_c6);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                            if(!isMvwWords(intentEntity,tts))
                                Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c6,tts);
                        }
                    });
                }
            }else if(currentGear == 0 || currentGear == GEAR_PARK){//OFF时，发档位无效值0
                sleepModeAction(intentEntity);
            }else {//非P档
                if(MODE_SAVE_SLEEP.equals(intentEntity.semantic.slots.mode)){//保存休息模式
                    conditionId = TtsConstant.SEATC16CONDITION;
                    defaultText = mContext.getString(R.string.seat_c16);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                            Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair_save, R.string.object_chair_save, conditionId, R.string.condition_chair_c16,tts);
                        }
                    });
                }else {
                    conditionId = TtsConstant.SEATC7CONDITION;
                    defaultText = mContext.getString(R.string.seat_c7);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                            if(!isMvwWords(intentEntity,tts))
                                Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c7,tts);
                        }
                    });
                }
            }
        }

    }

    public void sleepModeAction(IntentEntity intentEntity){
        if(CarUtils.getInstance(mContext).isSleepMode()){  //当前处于休息模式
            Log.d(TAG, "handleSleepMode: in sleep model");
            if(MODE_SAVE_SLEEP.equals(intentEntity.semantic.slots.mode)){//保存座椅休息模式
                conditionId = TtsConstant.SEATC12CONDITION;
                defaultText = mContext.getString(R.string.seat_c12);
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                        Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair_save, R.string.object_chair_save, conditionId, R.string.condition_chair_c12,tts);
                    }
                });
            }else {
                conditionId = TtsConstant.SEATC1CONDITION;
                defaultText = mContext.getString(R.string.seat_c1);
                newChar = intentEntity.semantic.slots.modeValue;
                oldChar = "#MODEL#";
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        tts = tts.replace(oldChar, newChar);
                        Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                        if(!isMvwWords(intentEntity,tts))
                            Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c1,tts);
                    }
                });
            }
        }else {
            if(MODE_SAVE_SLEEP.equals(intentEntity.semantic.slots.mode)){//保存座椅休息模式
                CarUtils.getInstance(mContext).saveChairMode(CarUtils.MODE_CHAIR_SLEEP);  //保存为休息模式
                conditionId = TtsConstant.SEATC13CONDITION;
                defaultText = mContext.getString(R.string.seat_c13);
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                        Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair_save, R.string.object_chair_save, conditionId, R.string.condition_chair_c13,tts);
                    }
                });
            }else {
                if(getCarBrake()){//制动踏板被踩下
                    Log.d(TAG, "handleSleepMode: brake enabled");
                    conditionId = TtsConstant.SEATC7_1CONDITION;
                    defaultText = mContext.getString(R.string.seat_c7_1);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                            if(!isMvwWords(intentEntity,tts))
                                Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c7_1,tts);
                        }
                    });
                }else {
                    Log.d(TAG, "handleSleepMode: brake disabled");
                    CarUtils.getInstance(mContext).setChairMode(CarUtils.MODE_CHAIR_SLEEP);  //设置为休息模式
                    handleAcAndWindow();//开启空调外循环和车窗
                    if(SRAgent.mMusicPlaying||SRAgent.mInRadioPlaying||SRAgent.mRadioPlaying) {  //有媒体源在播放 tts播报完退出
                        Log.d(TAG, "handleSleepMode() called with: intentEntity SRAgent.mMusicPlaying:"+SRAgent.mMusicPlaying+"..mInRadioPlaying:"+SRAgent.mInRadioPlaying+".."+SRAgent.mRadioPlaying);
                        conditionId = TtsConstant.SEATC4CONDITION;
                        defaultText = mContext.getString(R.string.seat_c4);
                        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                                if(!isMvwWords(intentEntity,tts))
                                    Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c4,tts);
                            }
                        });
                    }else  if(!SRAgent.mMusicPlaying&&!SRAgent.mInRadioPlaying&&!SRAgent.mRadioPlaying&&!NetworkUtil.isNetworkAvailable(mContext)){  //没有有媒体源在播放并且没有 tts播报完退出
                        Log.d(TAG, "handleSleepMode: no playing no network");
                        conditionId = TtsConstant.SEATC3CONDITION;
                        defaultText = mContext.getString(R.string.seat_c3);
                        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Utils.startTTSOnly(tts,mOnTtsStoppedListener);
                                if(!isMvwWords(intentEntity,tts))
                                    Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c3,tts);
                            }
                        });
                    }else{
                        Log.d(TAG, "handleSleepMode: goto play music");
                        SRAgent.getInstance().resetSrTimeCount();
                        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                        MultiInterfaceUtils.getInstance(mContext).uploadHotWrodsData();
                        conditionId = TtsConstant.SEATC5CONDITION;
                        defaultText = mContext.getString(R.string.seat_c5);
                        isMusicToPlay = true;
                        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Utils.startTTS(tts);
                                if(!isMvwWords(intentEntity,tts))
                                    Utils.eventTrack(mContext, R.string.skill_chair, R.string.scene_chair, R.string.object_control_chair, conditionId, R.string.condition_chair_c5,tts);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * 获取音乐是否去播放状态
     * 每次是识别之后设置为false，不用单独去设置这个值
     * @return
     */
    public boolean isMusicToPlay(){
        return isMusicToPlay;
    }

    private  TTSController.OnTtsStoppedListener mOnTtsStoppedListener = new TTSController.OnTtsStoppedListener() {
        @Override
        public void onPlayStopped() {
            FloatViewManager.getInstance(mContext).hide();
        }
    };

    //开启空调外循环和车窗
    private void handleAcAndWindow(){
        //开启外循环
        AirController.getInstance(mContext).changeCircleModeDelayed(LOOP_OUTSIDE,TAG);
        //若全车车窗关闭情况下，需要同时开启主驾车窗透气模式
        int position = CarController.getInstance(mContext).getWindowPosition(CarController.WINDOW_ALL);
        Log.d(TAG, "all window position = " + position);
        if(position == 0){
            CarController.getInstance(mContext).setWindowControl(10, CarController.WINDOW_LEFT_FRONT, TAG);
        }
        //暂不弹Toast提示
    }

    //获取当前的制动踏板状态
    public boolean getCarBrake(){
        Log.d(TAG, "getCarBrake = " + CarUtils.carBrake);
        return CarUtils.carBrake;
    }

    //获取当前的电源状态
    public int getPowerType(){
        Log.d(TAG, "getPowerType = " + CarUtils.powerStatus);
        return CarUtils.powerStatus;
    }

    //获取当前的档位
    public int getCurrentGear(){
        Log.d(TAG, "getCurrentGear = " + CarUtils.carGear);
        return CarUtils.carGear;
    }

    /**
     * 如果此时系统静音需要取消静音；如果媒体音量为0，需要将媒体音量调整到默认值10
     */
    private void unMuteMedia(){
        AppConstant.setMute = false;  //当界面 hide 时，获取为fasle，不用静音
        if(Utils.getStreamVolume(Utils.STREAM_MEDIA)==0||Utils.getStreamVolume(Utils.STREAM_MEDIA)>10){
            Utils.setStreamVolume(Utils.STREAM_MEDIA,10);
        }

    }

    private void getMessageWithoutTtsSpeakOnly(Context context, String conditionId,String defaultTTS){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });

            }
        });
    }

    private void restartScrene(){
        TspSceneManager.getInstance().resetScrene(mContext,current);
        current = -1;
    }

    private NlpVoiceModel convert2NlpVoiceModel() {
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "viewCmd";
        nlpVoiceModel.operation = "VIEWCMD";
        nlpVoiceModel.semantic = "{" + "\"slots\":{" + "\"viewCmd\":\"好的\"," + "\"modeValue\":\"座椅\"" + "}}";
        Log.d(TAG, "convert2NlpVoiceModel: " + nlpVoiceModel.semantic);
        return nlpVoiceModel;
    }

    public void DoMVWAction(IntentEntity intentEntity){
        showAssistant();
        Message msg = new Message();
        msg.what = MSG_SHOW_WORD;
        msg.obj = intentEntity;
        mHandler.sendMessageDelayed(msg,600);
    }

    private boolean isMvwWords(IntentEntity intentEntity,String tts){
        if("小欧我要休息".equals(intentEntity.text)){
            Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_chair_control,R.string.object_mhcc52,TtsConstant.MHXC52CONDITION,R.string.condition_null,tts);
            return true;
        }
        return false;
    }
}
