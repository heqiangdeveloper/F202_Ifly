package com.chinatsp.ifly.voice.platformadapter.controller;

import android.bluetooth.BluetoothAdapter;
import android.car.hardware.constant.VEHICLE;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IFeedBackInterface;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.SettingsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.chinatsp.phone.bean.CallContact;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Random;

public class DrivingCareController extends BaseController implements IFeedBackInterface {

    private static final String TAG = "DrivingCareController";
    private Context mContext;
    public static DrivingCareController mFeedBackController;
    private static final String PARAM = "#NUM#";
    private static final String EMS_DRIVE_TIME = "drive_time";
    private static final String FATIGUE_CARE_ON = "drive_care_on";
    private static final int CARE_ON = 2;
    private static final int CARE_OFF = 1;
    private static final  int MSG_TIRING = 1004;
    private static final  int TIME_DELAY_SHOWING = 600;
    public static final  int INVALID_TYPE = -1101;
    private static final int MEDIA_MUSIC = 1;
    private static final int MEDIA_RADIO = 2;
    private static final int MEDIA_INVALID = -1;

    private ArrayList<String> mSureContainer;
    private ArrayList<String> mExtraContainer;
    private ArrayList<String> mDenyContainer;
    private CarController mCarController;
    private ISpeechControlService mSpeechControlService;
    private int mCurrentType = INVALID_TYPE;

    private int mMediaType =MEDIA_INVALID;
    private int mUnderstandCount = 0;
    private boolean isPlaying = false;   //如果语音正在播报，下个信号过来是不再播报
    private Random mRandom;
    private int current;
    private float mDrivingTime = 0f;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_TIRING:
                    handleTired();
                    break;
            }
        }
    };

    public static DrivingCareController getInstance(Context c){
        if(mFeedBackController==null)
            mFeedBackController = new DrivingCareController(c);
        return mFeedBackController;
    }


    @Override
    public void srAction(IntentEntity intentEntity) {

        isPlaying = false;   //二次交互不再做防抖，设置为false

          if(PlatformConstant.Operation.VIEWCMD.equals(intentEntity.operation)){
              if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.viewCmd!=null){
                  Log.d(TAG, "srAction() called with: intentEntity = [" + intentEntity.semantic.slots.viewCmd + "]");
                   if(isSureWord(intentEntity.semantic.slots.viewCmd)){
                       try {
                           intentEntity.semantic.slots.modeValue = "疲劳";  //音乐通过区分 modeValue 播报不同的内容
                           if(isExtraSureWord(intentEntity.semantic.slots.viewCmd))
                               intentEntity.semantic.slots.viewCmd = "好的";
                           if(mMediaType==MEDIA_MUSIC)
                               mSpeechControlService.dispatchSRAction(Business.MUSIC, convert2NlpVoiceModel());
                           else
                               mSpeechControlService.dispatchSRAction(Business.RADIO,convert2NlpVoiceModel());
                           unMuteMedia();
                       } catch (RemoteException e) {
                           e.printStackTrace();
                       }
                   }else if(isDenyWord(intentEntity.semantic.slots.viewCmd)){
                       if (mMediaType == MEDIA_MUSIC)
                           getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.MSGC90CONDITION, mContext.getString(R.string.msg_c90),"", R.string.object_tired, R.string.condition_feed_c90);
                       else
                           getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.MSGC90_2CONDITION, mContext.getString(R.string.msg_c90_2), "",R.string.object_tired, R.string.condition_feed_c90_2);
                   } else if(isSureWord(intentEntity.text)){

                          try {
                              intentEntity.semantic.slots.modeValue = "疲劳";  //音乐通过区分 modeValue 播报不同的内容
                              if(isExtraSureWord(intentEntity.semantic.slots.viewCmd))
                                  intentEntity.semantic.slots.viewCmd = "好的";
                              intentEntity.semantic.slots.viewCmd = intentEntity.text;
                              if(mMediaType==MEDIA_MUSIC)
                                  mSpeechControlService.dispatchSRAction(Business.MUSIC, convert2NlpVoiceModel());
                              else
                                  mSpeechControlService.dispatchSRAction(Business.RADIO, convert2NlpVoiceModel());
                              unMuteMedia();
                          } catch (RemoteException e) {
                              e.printStackTrace();
                          }

                  }else if(isDenyWord(intentEntity.text)){
                          if (mMediaType == MEDIA_MUSIC)
                              getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90CONDITION,mContext.getString(R.string.msg_c90),"",R.string.object_tired,R.string.condition_feed_c90);
                          else
                              getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90_2CONDITION,mContext.getString(R.string.msg_c90_2),"",R.string.object_tired,R.string.condition_feed_c90_2);



                  }else
                       doExceptonAction(mContext);
              }else
                  doExceptonAction(mContext);
          }else
              doExceptonAction(mContext);
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        Log.d(TAG, "mvwAction() called with: mvwLParamEntity = [" + mvwLParamEntity.nKeyword + "]");
        if (mvwLParamEntity.nMvwScene == MvwSession.ISS_MVW_SCENE_CONFIRM) {
            Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_window,R.string.object_mhcc23,TtsConstant.MHXC23CONDITION,R.string.condition_null);
            isPlaying = false;   //二次交互不再做防抖，设置为false
            //通知 dms ，可以监听信号了
            CarUtils.getInstance(mContext).setDmsStatus(VEHICLE.ON);
            TTSController.getInstance(mContext).stopTTS();
            if (mvwLParamEntity.nMvwId == 0
                    ||mvwLParamEntity.nMvwId == 6
                    ||mvwLParamEntity.nMvwId == 7
                    ||mvwLParamEntity.nMvwId == 8
                    ||mvwLParamEntity.nMvwId == 9
                    ||mvwLParamEntity.nMvwId == 10) {

                    try {
                        //intentEntity.semantic.slots.modeValue = "疲劳";  //音乐通过区分 modeValue 播报不同的内容
                        if (mMediaType == MEDIA_MUSIC)
                            mSpeechControlService.dispatchSRAction(Business.MUSIC, convert2NlpVoiceModel());
                        else
                            mSpeechControlService.dispatchSRAction(Business.RADIO, convert2NlpVoiceModel());
                        unMuteMedia();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

            } else {

                    if (mMediaType == MEDIA_MUSIC)
                        getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC91_7CONDITION,mContext.getString(R.string.msg_c90),"",R.string.object_tired,R.string.condition_feed_c90);
                    else
                        getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC91_9CONDITION,mContext.getString(R.string.msg_c90_2),"",R.string.object_tired,R.string.condition_feed_c90_2);

            }
        } else
            doExceptonAction(mContext);

    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    public void dispatchCommand(float command){
        Log.d(TAG, "dispatchCommand() called with: command = [" + command + "]");

        if(BluePhoneManager.getInstance(mContext).getCallStatus()!= CallContact.CALL_STATE_TERMINATED){
            Log.e(TAG, "dispatchCommand: CallContact.CALL_STATE_TERMINATED");
            return;
        }

        int careSwith = Settings.System.getInt(mContext.getContentResolver(), FATIGUE_CARE_ON, CARE_ON);

        if(CARE_OFF ==careSwith){
            Log.e(TAG, "dispatchCommand: careSwith：："+careSwith);
            return;
        }

        float defaultTime= Settings.System.getFloat(mContext.getContentResolver(), EMS_DRIVE_TIME, 3.0f);
        if(command<defaultTime){
            Log.e(TAG, "dispatchCommand: defaultTime::"+defaultTime+".....command:::"+command);
            return;
        }

        if(command%defaultTime!=0){
            Log.e(TAG, "dispatchCommand: command%defaultTime!=0::"+(command%defaultTime!=0));
            return;
        }

        mDrivingTime = command;

       /* if(isPlaying){
            Log.e(TAG, "dispatchCommand: isPlaying"+isPlaying);
            return;
        }*/

        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "getMessageWithTtsSpeakListener: "+Utils.getCurrentPriority(mContext));
            return;
        }


        DatastatManager.primitive = "";
        DatastatManager.response = "";
        ActiveServiceModel.Activit_tts_msg = "";

        isPlaying = true;

        mMediaType = MEDIA_INVALID;
        mCurrentType = INVALID_TYPE;
        mUnderstandCount = 0;

        //显示语义助理
        showAssistant();
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_TIRING, TIME_DELAY_SHOWING);

    }

    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }

    public void setInvalideType(){
        Log.d(TAG, "setInvalideType() called");
        if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DRIVING_CARE){
            mCurrentType = INVALID_TYPE;
            mMediaType = MEDIA_INVALID;
            mUnderstandCount = 0;
            restartScrene();
        }
    }

    public int getShowType(){
        Log.d(TAG, "getShowType() called::"+mCurrentType);
        return mCurrentType;
    }

    public void setUnderstandCound(){
        Log.d(TAG, "setInvalideType() called");
        mUnderstandCount = 1;
    }

    public int getUnderstandCound(){
        Log.d(TAG, "getShowType() called::"+mUnderstandCount);
        return mUnderstandCount;
    }

    /**
     * 界面消失的时候设置为false，防止播报被中断没有回调
     * @param playing
     */
    public void setTtsPlayStatus(boolean playing){
        Log.d(TAG, "setTtsPlayStatus() called with: playing = [" + playing + "]"+"..isPlaying:"+isPlaying);
        isPlaying = playing;
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }


    private void handleTired(){

        String param = ""+mDrivingTime;
        if(param.endsWith(".0")){
            int time = (int) mDrivingTime;
            param = ""+time;
        }
        Log.d(TAG, "handleTired() called::"+param);

        SRAgent.getInstance().resetSrTimeCount();
        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
        mCurrentType = MSG_TIRING;
        Log.d(TAG, "handleTired() called:mMediaType:"+mMediaType+".."+SRAgent.mMusicPlaying+"...."+SRAgent.mInRadioPlaying+".."+NetworkUtil.isNetworkAvailable(mContext));
        if(!NetworkUtil.isNetworkAvailable(mContext)){  //车机无网络
            getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC91_3CONDITION,mContext.getString(R.string.msg_c91_3),param,R.string.object_care,R.string.condition_msgC91_3);
        }else if(NetworkUtil.isNetworkAvailable(mContext)){  //车机有网络
            if(!SRAgent.mMusicPlaying&&!SRAgent.mInRadioPlaying&&!SRAgent.mRadioPlaying){  //且无媒体源在播放
                current = TspSceneAdapter.getTspScene(mContext);
                switchScreneToDriving();
                boolean radio =mRandom.nextBoolean();
                Log.d(TAG, "handleTired() called::radio::::::"+radio);
                if(radio){
                    mMediaType = MEDIA_RADIO;
                    getMessageWithoutTtsSpeak(mContext,TtsConstant.MSGC91_2CONDITION,mContext.getString(R.string.msg_c91_2),param,R.string.object_care,R.string.condition_msgC91_2);
                }else {
                    mMediaType = MEDIA_MUSIC;
                    getMessageWithoutTtsSpeak(mContext,TtsConstant.MSGC91_1CONDITION,mContext.getString(R.string.msg_c91_1),param,R.string.object_care,R.string.condition_msgC91_1);
                }
            }else {  //有媒体源在播放
                if(Utils.getStreamVolume(Utils.STREAM_MEDIA)!=0&&!AudioFocusUtils.getInstance(mContext).isMasterMute()&&SRAgent.mInRadioPlaying){  //非静音 并且是在线电台
                    getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC91_4CONDITION,mContext.getString(R.string.msg_c91_4),param,R.string.object_care,R.string.condition_msgC91_4);
                }else {  //静音
                    mMediaType = MEDIA_RADIO;
                    current = TspSceneAdapter.getTspScene(mContext);
                    switchScreneToDriving();
                    getMessageWithoutTtsSpeak(mContext,TtsConstant.MSGC91_5CONDITION,mContext.getString(R.string.msg_c91_5),param,R.string.object_care,R.string.condition_msgC91_5);
                }
            }

        }
    }




    private void getMessageWithoutTtsSpeak(Context context, String conditionId,String defaultTTS,String param,int object,int condition){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                ttsText = ttsText.replace(PARAM,param);
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_drive_car,object,conditionId, condition,ttsText);
                Utils.startTTS(ttsText,PriorityControler.PRIORITY_THREE, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                    }
                });

            }
        });
    }


    private void getMessageWithoutTtsSpeakOnly(Context context, String conditionId,String defaultTTS,String param, int object,int condition){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                ttsText = ttsText.replace(PARAM,param);
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_drive_car,object,conditionId, condition,ttsText);
                Utils.startTTSOnly(ttsText,PriorityControler.PRIORITY_THREE, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });

            }
        });
    }


    public boolean isSureWord(String word){
        for(int i=0;i<mSureContainer.size();i++){
            if(mSureContainer.get(i).equals(word))
                return true;
        }
           return false;
    }

    /**
     * 增加额外不在自定义范围的词汇，没有和音乐 电台联调，需要转化为 好的 处理
     * @param word
     * @return
     */
    public boolean isExtraSureWord(String word){
        for(int i=0;i<mExtraContainer.size();i++){
            if(mExtraContainer.get(i).equals(word))
                return true;
        }
        return false;
    }

    public boolean isDenyWord(String word){
        for(int i=0;i<mDenyContainer.size();i++){
            if(mDenyContainer.get(i).equals(word))
                return true;
        }
        return false;
    }

    private DrivingCareController(Context c){
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

        mRandom = new Random();

    }

    /**
     * 如果此时系统静音需要取消静音；如果媒体音量为0，需要将媒体音量调整到默认值20
     */
    private void unMuteMedia(){
        AppConstant.setMute =false;  //当界面 hide 时，获取为fasle，不用静音
        if(Utils.getStreamVolume(Utils.STREAM_MEDIA)==0){
            Utils.setStreamVolume(Utils.STREAM_MEDIA,20);
        }
    }

    private NlpVoiceModel convert2NlpVoiceModel() {
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "viewCmd";
        nlpVoiceModel.operation = "VIEWCMD";
        nlpVoiceModel.semantic = "{" + "\"slots\":{" + "\"viewCmd\":\"好的\"," + "\"modeValue\":\"疲劳\"" + "}}";
        Log.d(TAG, "convert2NlpVoiceModel: " + nlpVoiceModel.semantic);
        return nlpVoiceModel;
    }

    private void restartScrene(){
        TspSceneManager.getInstance().resetScrene(mContext,current);
        current = -1;
    }

    private void switchScreneToDriving(){
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_DRIVING_CARE);
    }

}
