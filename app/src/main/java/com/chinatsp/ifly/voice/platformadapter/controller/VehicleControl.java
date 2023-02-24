package com.chinatsp.ifly.voice.platformadapter.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.automotive.vehicle.V2_0.VehicleAPAIndication;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.SearchEvent;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.ISpeechTtsStopListener;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.entity.SearchPoiEvent;
import com.chinatsp.ifly.service.ActiveViewService;
import com.chinatsp.ifly.service.SpeechRemoteService;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IVehicleInterface;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Random;

/**
 * 主要用来处理车况相关的语义
 */
public class VehicleControl extends BaseController implements IVehicleInterface {

    private static final String TAG = "VehicleControl";
    private static VehicleControl mVehicleControl;
    private static final  int MSGC_7CONDITION = 1001;
    private static final  int MSGC_8CONDITION = 1002;
    private static final  int MSGC_13CONDITION = 1003;
    private static final  int MSGC_14CONDITION = 1004;
    private static final  int TIME_DELAY_SHOWING = 10;
    private static final int TYPE_OIL = 1;
    private static final int TYPE_EMG = 2;
    private static final int TYPE_IDEL = -1;
    private Context mContext;
    private int current;
    private int mType = TYPE_IDEL ;

    public static VehicleControl getInstance(Context c){
        if(mVehicleControl==null)
            mVehicleControl = new VehicleControl(c);
        return mVehicleControl;
    }

    private VehicleControl(Context c){
        mContext = c;
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSGC_7CONDITION:
                   handleOilMessage(msg,TtsConstant.MSGC7CONDITION);
                   break;
                case MSGC_8CONDITION:
                    handleOilMessage(msg,TtsConstant.MSGC8CONDITION);
                    break;
                case MSGC_13CONDITION:
                    handleEmgMessage(msg,TtsConstant.MSGC13CONDITION);
                case MSGC_14CONDITION:
                    handleEmgMessage(msg,TtsConstant.MSGC14CONDITION);
                    break;
            }
        }
    };

    public void onDoAction(String text){
        TTSController.getInstance(mContext).stopTTS();
        handleResult(text);
    }

    private void handleResult(String text) {
        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_window,R.string.object_mhcc23,TtsConstant.MHXC23CONDITION,R.string.condition_null);
        if (TextUtils.isEmpty(text)) {
            doExceptonAction(mContext);
        } else if ("确定".equals(text)
                ||"好的".equals(text)
                ||"好啊".equals(text)
                ||"可以".equals(text)
                ||"需要".equals(text)
                ||"同意".equals(text)){
            if(mType==TYPE_OIL){
                EventBus.getDefault().post(new SearchPoiEvent("加油站", PlatformConstant.Topic.GAS_STATION));
            }else if(mType == TYPE_EMG){
                EventBus.getDefault().post(new SearchPoiEvent("充电桩", PlatformConstant.Topic.CHARGING_PILE));
            } else {
                doExceptonAction(mContext);
            }
        }else {
            if(mType==TYPE_OIL){
                getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.MSGC11CONDITION,mContext.getString(R.string.msg_C11),R.string.object_oil,R.string.condition_oil_select);
            }else if(mType == TYPE_EMG){
                getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC17CONDITION,mContext.getString(R.string.msg_C17),R.string.object_emg,R.string.condition_emg_select);
            }else {
                doExceptonAction(mContext);
            }
        }
    }


    @Override
    public void srAction(IntentEntity intentEntity) {

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        TTSController.getInstance(mContext).stopTTS();
        handleResult(mvwLParamEntity.nKeyword);
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    public void dispatchCommand(boolean showText, String conditionId, final String defaultTts, final ISpeechTtsStopListener listener){

//        current = TspSceneAdapter.getTspScene(mContext);
//        MVWAgent.getInstance().stopMVWSession();
//        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_VEHICLE);
//
//
//        //显示语义助理
//        showAssistant();
        mHandler.removeCallbacksAndMessages(null);

        Message msg = mHandler.obtainMessage();
        msg.obj = defaultTts;
        if(TtsConstant.MSGC7CONDITION.equals(conditionId)){
            msg.what = MSGC_7CONDITION;
        }else if(TtsConstant.MSGC8CONDITION.equals(conditionId)){
            msg.what = MSGC_8CONDITION;
        }else if(TtsConstant.MSGC13CONDITION.equals(conditionId)){
            msg.what = MSGC_13CONDITION;
        }else if(TtsConstant.MSGC14CONDITION.equals(conditionId)){
            msg.what = MSGC_14CONDITION;
        }
        mHandler.sendMessageDelayed(msg,TIME_DELAY_SHOWING);
    }

    public void setInvalideType(){
        Log.d(TAG, "setInvalideType() called");
        if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_VEHICLE){
            restartScrene();
            mType = TYPE_IDEL ;
        }
    }


    private void restartScrene(){
        TspSceneManager.getInstance().resetScrene(mContext,current);
        current = -1;
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    /**
     * 油量不足提醒
     * @param msg
     */
    private void handleOilMessage(Message msg,String conditionid) {
        ActiveServiceViewManager.getInstance(mContext).show(ActiveViewService.VIEW_OIL_SHORTAGE,conditionid, (String) msg.obj);
      /*  mType = TYPE_OIL;
        SRAgent.getInstance().resetSrTimeCount();
        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
        current = TspSceneAdapter.getTspScene(mContext);
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_VEHICLE);
        Utils.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
            }
        });
*/
    }

    /**
     * 电量不足提醒
     * @param msg
     */
    private void handleEmgMessage(Message msg,String conditionid) {
        ActiveServiceViewManager.getInstance(mContext).show(ActiveViewService.VIEW_ELEC_SHORTAGE,conditionid, (String) msg.obj);
      /*  mType = TYPE_EMG;
        SRAgent.getInstance().resetSrTimeCount();
        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
        current = TspSceneAdapter.getTspScene(mContext);
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_VEHICLE);
        Utils.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
            }
        });*/
    }

    private void getMessageWithoutTtsSpeakOnly(Context context, String conditionId,String defaultTTS, int object,int condition){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.eventTrack(mContext, R.string.skill_active,R.string.scene_feed,object,conditionId, condition,ttsText);
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });

            }
        });
    }
}
