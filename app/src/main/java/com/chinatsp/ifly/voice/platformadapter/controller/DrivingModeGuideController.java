package com.chinatsp.ifly.voice.platformadapter.controller;

import android.bluetooth.BluetoothAdapter;
import android.car.CarNotConnectedException;
import android.car.hardware.cabin.CarCabinManager;
import android.car.hardware.constant.VEHICLE;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.excontrol.ExControlManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IFeedBackInterface;
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
import com.iflytek.mvw.MvwSession;

import java.util.ArrayList;
import java.util.Random;

import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static android.car.VehicleAreaWindow.WINDOW_ROOF_TOP_1;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_POS;
import static com.chinatsp.ifly.voice.platformadapter.controller.CarController.DRIVE_MODE_SPORT;
import static com.chinatsp.ifly.voice.platformadapter.controller.CarController.NAVI;

/**
 * Created by Administrator on 2020/4/7.
 */

public class DrivingModeGuideController extends BaseController implements IFeedBackInterface {
    private static DrivingModeGuideController mDrivingModeGuideController;
    private Context mContext;
    private static ExControlManager mControlManager;
    private final String TAG = "DrivingModeGuideController";
    private int lastDriveMode = 0;
    private int oldTspSecene;
    private static final int MSG_UP_SLOP = 1001;
    private static final int MSG_DOWN_SLOP = 1002;
    private static final int MSG_END_UP_SLOP = 1003;
    private static final int MSG_OVERTAKE = 1004;
    private static final int MSG_END_OVERTAKE = 1005;
    private static final int DELAYTIME = 600;
    private ArrayList<String> mSureContainer;
    private ArrayList<String> mExtraContainer;
    private ArrayList<String> mDenyContainer;
    private boolean isPlaying = false;   //如果语音正在播报，下个信号过来是不再播报

    private DrivingModeGuideController(Context c){
        mContext = c;
        mSureContainer = new ArrayList<>();
        mSureContainer.add("好的");
        mSureContainer.add("好啊");
        mSureContainer.add("可以");
        mSureContainer.add("需要");
        mSureContainer.add("确定");
        mSureContainer.add("同意");
        mSureContainer.add("好的呀");
        mSureContainer.add("好呀");
        mSureContainer.add("好的啊");
        mSureContainer.add("好的呀|好的啊");
        mSureContainer.add("好的啊|好的呀");

        mDenyContainer =  new ArrayList<>();
        mDenyContainer.add("不好");
        mDenyContainer.add("不行");
        mDenyContainer.add("不需要");
        mDenyContainer.add("取消");
        mDenyContainer.add("不可以");
        mDenyContainer.add("不用");
        mDenyContainer.add("不要");

        mExtraContainer = new ArrayList<>();
        mExtraContainer.add("好的呀");
        mExtraContainer.add("好呀");
        mExtraContainer.add("好的啊");
        mExtraContainer.add("好的呀|好的啊");
        mExtraContainer.add("好的啊|好的呀");

    }

    public static DrivingModeGuideController getInstance(Context c){
        mControlManager = ExControlManager.getInstance(c, NAVI);
        if(mDrivingModeGuideController==null)
            mDrivingModeGuideController = new DrivingModeGuideController(c);
        return mDrivingModeGuideController;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            SRAgent.getInstance().resetSrTimeCount();
//            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");

            Log.d(TAG,"before speak secene = " + TspSceneAdapter.getTspScene(mContext));
            switch (msg.what){
                case MSG_UP_SLOP:
                    getMessageWithoutTtsSpeak(false,mContext, TtsConstant.DRIVRINGC4CONDITION,mContext.getString(R.string.driving_mode_c4),
                            R.string.skill_driving_mode,
                            R.string.scene_driving_mode_guide,
                            R.string.object_driving_mode_guide,
                            R.string.condition_driving_c4);
                    break;
                case MSG_DOWN_SLOP:
                    getMessageWithoutTtsSpeak(false,mContext, TtsConstant.DRIVRINGC7CONDITION,mContext.getString(R.string.driving_mode_c7),
                            R.string.skill_driving_mode,
                            R.string.scene_driving_mode_guide,
                            R.string.object_driving_mode_guide,
                            R.string.condition_driving_c7);
                    break;
                case MSG_END_UP_SLOP:
                    getMessageWithoutTtsSpeak(false,mContext, TtsConstant.DRIVRINGC5CONDITION,mContext.getString(R.string.driving_mode_c5),
                            R.string.skill_driving_mode,
                            R.string.scene_driving_mode_guide,
                            R.string.object_driving_mode_guide,
                            R.string.condition_driving_c5);
                    break;
                case MSG_OVERTAKE:
                    getMessageWithoutTtsSpeak(false,mContext, TtsConstant.DRIVRINGC6CONDITION,mContext.getString(R.string.driving_mode_c6),
                            R.string.skill_driving_mode,
                            R.string.scene_driving_mode_guide,
                            R.string.object_driving_mode_guide,
                            R.string.condition_driving_c6);
                    break;
                case MSG_END_OVERTAKE:
                    getMessageWithoutTtsSpeak(false,mContext, TtsConstant.DRIVRINGC6_1CONDITION,mContext.getString(R.string.driving_mode_c6_1),
                            R.string.skill_driving_mode,
                            R.string.scene_driving_mode_guide,
                            R.string.object_driving_mode_guide,
                            R.string.condition_driving_c6_1);
                    break;
            }
            //MultiInterfaceUtils.getInstance(mContext).uploadHotWrodsData();//上传场景
            Log.d(TAG,"after speak secene = " + TspSceneAdapter.getTspScene(mContext));
            MVWAgent.getInstance().stopMVWSession();
            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_DRIVING_GUIDE);
        }
    };

    private void showAssistant(){
        Log.d(TAG,"call assistant show..");
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    public void dispatchCommand(int type){
//        if(FloatViewManager.getInstance(mContext).isHide()){
//            isPlaying = false;
//        }else {
//            isPlaying = true;
//        }
//        if(isPlaying){//防抖处理
//            Log.e(TAG, "dispatchCommand: isPlaying"+isPlaying);
//            return;
//        }

        //isPlaying = true;

        oldTspSecene = TspSceneAdapter.getTspScene(mContext);
        Log.d(TAG,"type = " + type +",oldTspSecene = " + oldTspSecene);
        //显示语义助理
        if(type >= 1 && type <= 5){
            showAssistant();
            if(type == VEHICLE.DRIVING_MODE_GUIDE_UPHILL_REMIND){//上坡
                mHandler.sendEmptyMessageDelayed(MSG_UP_SLOP,DELAYTIME);
            }else if(type == VEHICLE.DRIVING_MODE_GUIDE_DOWNHILL_REMIND){//下坡
                mHandler.sendEmptyMessageDelayed(MSG_DOWN_SLOP,DELAYTIME);
            }else if(type == VEHICLE.DRIVING_MODE_GUIDE_UPHILL_REDUCTION_REMIND) {//上坡结束
                mHandler.sendEmptyMessageDelayed(MSG_END_UP_SLOP,DELAYTIME);
            } else if(type == VEHICLE.DRIVING_MODE_GUIDE_OVERSPEED_REMIND){//超车
                mHandler.sendEmptyMessageDelayed(MSG_OVERTAKE,DELAYTIME);
            } else if(type == 5){//超速结束
                mHandler.sendEmptyMessageDelayed(MSG_END_OVERTAKE,DELAYTIME);
            }
        }
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        Log.d(TAG, "mvwAction() called with: mvwLParamEntity = [" + mvwLParamEntity.nKeyword + "]");
        Log.d(TAG, "mvwAction() called with: mvwLParamEntity.nMvwScene = [" + mvwLParamEntity.nMvwScene + "]");
        if (mvwLParamEntity.nMvwScene == MvwSession.ISS_MVW_SCENE_CONFIRM) {
            Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_window,R.string.object_mhcc23,TtsConstant.MHXC23CONDITION,R.string.condition_null);
            TTSController.getInstance(mContext).stopTTS();
            if (mvwLParamEntity.nMvwId == 0
                    ||mvwLParamEntity.nMvwId == 6
                    ||mvwLParamEntity.nMvwId == 7
                    ||mvwLParamEntity.nMvwId == 8
                    ||mvwLParamEntity.nMvwId == 9
                    ||mvwLParamEntity.nMvwId == 10) {//同意
                sendAnswer(VEHICLE.ON);
                getMessageWithoutTtsSpeak(true,mContext, TtsConstant.DRIVRINGC8CONDITION,mContext.getString(R.string.driving_mode_c8),
                        R.string.skill_driving_mode,
                        R.string.scene_driving_mode_guide,
                        R.string.object_driving_mode_guide,
                        R.string.condition_driving_c8);
            } else {//拒绝
                sendAnswer(VEHICLE.OFF);
                getMessageWithoutTtsSpeak(true,mContext, TtsConstant.DRIVRINGC9CONDITION,mContext.getString(R.string.driving_mode_c9),
                        R.string.skill_driving_mode,
                        R.string.scene_driving_mode_guide,
                        R.string.object_driving_mode_guide,
                        R.string.condition_driving_c9);
            }

            Log.d(TAG,"after answer secene = " + TspSceneAdapter.getTspScene(mContext));
            //恢复场景
//            MVWAgent.getInstance().stopMVWSession();
//            MVWAgent.getInstance().startMVWSession(oldTspSecene);
        } else
            doExceptonAction(mContext);
    }

    @Override
    public void srAction(IntentEntity intentEntity) {

    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    /**
     * 发送用户的回答
     */
    public void sendAnswer(int value) {
        try {
            AppConfig.INSTANCE.mCarCabinManager.setIntProperty(CarCabinManager.ID_DRIVING_MODE_GUIDE_ALLOW,
                    VEHICLE_AREA_TYPE_GLOBAL, value);
            Log.d(TAG, "lh:发送用户的回答 =" + value);
        } catch (CarNotConnectedException e) {
            Log.d(TAG, "lh:发送用户的回答Exception = " + e);
            e.printStackTrace();
        }
    }

    public void setInvalideType(){
        //Log.d(TAG, "setInvalideType() called");
        Log.d(TAG,"after call setInvalideType secene = " + TspSceneAdapter.getTspScene(mContext));
        if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DRIVING_GUIDE){
            restartScrene();
        }
    }

    private void restartScrene(){
        TspSceneManager.getInstance().resetScrene(mContext,oldTspSecene);
        oldTspSecene = -1;
    }

    private void getMessageWithoutTtsSpeak(boolean isHide,Context context, String conditionId,String defaultTTS,int appName,int scene,int object,int condition){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.eventTrack(mContext, appName,scene,object,conditionId, condition,ttsText);
                Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if(isHide) Utils.exitVoiceAssistant();
                    }
                });

            }
        });
    }
}
