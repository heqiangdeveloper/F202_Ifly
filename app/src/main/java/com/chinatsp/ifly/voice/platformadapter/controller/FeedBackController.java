package com.chinatsp.ifly.voice.platformadapter.controller;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.car.hardware.constant.VEHICLE;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
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

public class FeedBackController extends BaseController implements IFeedBackInterface {

    private static final String TAG = "FeedBackController";
    private Context mContext;
    public static FeedBackController mFeedBackController;
    private static final  int MSG_ATTENTION = 1001;
    private static final  int MSG_SMOKING = 1002;
    private static final  int MSG_CALL = 1003;
    private static final  int MSG_TIRING = 1004;
    private static final  int MSG_TIRING_TWO = 1005;
    private static final int  MSG_OPEN_BT = 1006;
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

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_ATTENTION:
                    handleAttention();
                    break;
                case MSG_SMOKING:
                    handleSmoking();
                    break;
                case MSG_CALL:
                    handleCall();
                    break;
                case MSG_TIRING:
                    handleTired();
                    break;
                case MSG_TIRING_TWO:
                    handleTiredTwo();
                    break;
                case MSG_OPEN_BT:
                    BluetoothAdapter.getDefaultAdapter().enable();
                    break;
            }
        }
    };

    public static FeedBackController getInstance(Context c){
        if(mFeedBackController==null)
            mFeedBackController = new FeedBackController(c);
        return mFeedBackController;
    }


    @Override
    public void srAction(IntentEntity intentEntity) {

        isPlaying = false;   //二次交互不再做防抖，设置为false

        //通知 dms ，可以监听信号了
        CarUtils.getInstance(mContext).setDmsStatus(VEHICLE.ON);

          if(PlatformConstant.Operation.VIEWCMD.equals(intentEntity.operation)){
              if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.viewCmd!=null){
                  Log.d(TAG, "srAction() called with: intentEntity = [" + intentEntity.semantic.slots.viewCmd + "]");
                   if(isSureWord(intentEntity.semantic.slots.viewCmd)){
                       if(mCurrentType==MSG_SMOKING) {
                           mCarController.confirmSmoking(intentEntity);
                       }else {
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
                       }
                   }else if(isDenyWord(intentEntity.semantic.slots.viewCmd)){
                       if(mCurrentType==MSG_SMOKING){
                           getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC81CONDITION,mContext.getString(R.string.msg_c81),R.string.sobject_smoking,R.string.condition_feed_c81);
                       } else{
                           if (mMediaType == MEDIA_MUSIC)
                               getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90CONDITION,mContext.getString(R.string.msg_c90),R.string.object_tired,R.string.condition_feed_c90);
                           else
                               getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90_2CONDITION,mContext.getString(R.string.msg_c90_2),R.string.object_tired,R.string.condition_feed_c90_2);
                       }


                   } else if(isSureWord(intentEntity.text)){
                      if(mCurrentType==MSG_SMOKING) {
                          mCarController.confirmSmoking(intentEntity);
                      }else {
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
                      }
                  }else if(isDenyWord(intentEntity.text)){
                      if(mCurrentType==MSG_SMOKING){
                          getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC81CONDITION,mContext.getString(R.string.msg_c81),R.string.sobject_smoking,R.string.condition_feed_c81);
                      } else{
                          if (mMediaType == MEDIA_MUSIC)
                              getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90CONDITION,mContext.getString(R.string.msg_c90),R.string.object_tired,R.string.condition_feed_c90);
                          else
                              getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90_2CONDITION,mContext.getString(R.string.msg_c90_2),R.string.object_tired,R.string.condition_feed_c90_2);
                      }


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
                if (mCurrentType == MSG_SMOKING) {
                    mCarController.confirmSmoking(null);
                } else {
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
                }
            } else {
                if (mCurrentType == MSG_SMOKING) {
                    getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC81CONDITION,mContext.getString(R.string.msg_c81),R.string.sobject_smoking,R.string.condition_feed_c81);
                } else {
                    if (mMediaType == MEDIA_MUSIC)
                        getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90CONDITION,mContext.getString(R.string.msg_c90),R.string.object_tired,R.string.condition_feed_c90);
                    else
                        getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC90_2CONDITION,mContext.getString(R.string.msg_c90_2),R.string.object_tired,R.string.condition_feed_c90_2);
                }
            }
        } else
            doExceptonAction(mContext);

    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    public void dispatchCommand(String command){
        Log.d(TAG, "dispatchCommand() called with: command = [" + command + "]");

        if(BluePhoneManager.getInstance(mContext).getCallStatus()!= CallContact.CALL_STATE_TERMINATED){
            Log.e(TAG, "dispatchCommand: CallContact.CALL_STATE_TERMINATED");
            return;
        }

        if(isPlaying){
            Log.e(TAG, "dispatchCommand: isPlaying"+isPlaying);
            return;
        }
        if(TtsConstant.FEEDBACK_CALL.equals(command)&&BluePhoneManager.getInstance(mContext).isBtConnected()){
            Log.e(TAG, "the bt is connect ,ignore");
            return;
        }

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


        //通知 dms ，不要监听信号
        CarUtils.getInstance(mContext).setDmsStatus(VEHICLE.OFF);

        if(TtsConstant.FEEDBACK_ATTENTION.equals(command)){  //注意力
            mHandler.sendEmptyMessageDelayed(MSG_ATTENTION,TIME_DELAY_SHOWING);
        }else if(TtsConstant.FEEDBACK_SMOKING.equals(command)){
            mHandler.sendEmptyMessageDelayed(MSG_SMOKING,TIME_DELAY_SHOWING);
        }else if(TtsConstant.FEEDBACK_CALL.equals(command)){
            mHandler.sendEmptyMessageDelayed(MSG_CALL,TIME_DELAY_SHOWING);
        }else if(TtsConstant.FEEDBACK_TIRING.equals(command)){
            mHandler.sendEmptyMessageDelayed(MSG_TIRING,TIME_DELAY_SHOWING);
        }else if(TtsConstant.FEEDBACK_TIRING_TWO.equals(command)){
            mHandler.sendEmptyMessageDelayed(MSG_TIRING_TWO,TIME_DELAY_SHOWING);
        }
    }

    public void setCarController(CarController carController){
        this.mCarController = carController;
    }

    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }

    public void setInvalideType(){
        Log.d(TAG, "setInvalideType() called");
        if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK){
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
        if(isPlaying){
            CarUtils.getInstance(mContext).setDmsStatus(VEHICLE.ON);  //通知dms可以监听信号了
        }
        isPlaying = playing;
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    private void handleSmoking(){


        SRAgent.getInstance().resetSrTimeCount();
        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");

        mCurrentType = MSG_SMOKING;
//        MultiInterfaceUtils.getInstance(mContext).uploadHotWrodsData();
        if (mCarController != null) {
           boolean doHandler =  mCarController.handleFeedbackCommand();//当前处于二次交互
           if(!doHandler){
               current = TspSceneAdapter.getTspScene(mContext);
               MultiInterfaceUtils.getInstance(mContext).uploadHotWrodsData();
           }
        }
    }

    private void handleTired(){

        SRAgent.getInstance().resetSrTimeCount();
        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
        mCurrentType = MSG_TIRING;
        Log.d(TAG, "handleTired() called:mMediaType:"+mMediaType+".."+SRAgent.mMusicPlaying+"...."+SRAgent.mInRadioPlaying+".."+NetworkUtil.isNetworkAvailable(mContext));
        if(!NetworkUtil.isNetworkAvailable(mContext)){  //车机无网络
            getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC85CONDITION,mContext.getString(R.string.msg_c85),R.string.object_tired,R.string.condition_feed_c85);
        }else if(NetworkUtil.isNetworkAvailable(mContext)){  //车机有网络
            if(!SRAgent.mMusicPlaying&&!SRAgent.mInRadioPlaying&&!SRAgent.mRadioPlaying){  //且无媒体源在播放
                current = TspSceneAdapter.getTspScene(mContext);
                MultiInterfaceUtils.getInstance(mContext).uploadHotWrodsData();
                boolean radio =mRandom.nextBoolean();
                Log.d(TAG, "handleTired() called::radio::::::"+radio);
                if(radio){
                    mMediaType = MEDIA_RADIO;
                    getMessageWithoutTtsSpeak(mContext,TtsConstant.MSGC84_1CONDITION,mContext.getString(R.string.msg_c84_1),R.string.object_tired,R.string.condition_feed_c84_1);
                }else {
                    mMediaType = MEDIA_MUSIC;
                    getMessageWithoutTtsSpeak(mContext,TtsConstant.MSGC84CONDITION,mContext.getString(R.string.msg_c84),R.string.object_tired,R.string.condition_feed_c84);
                }
            }else {  //有媒体源在播放
                if(Utils.getStreamVolume(Utils.STREAM_MEDIA)!=0&&!AudioFocusUtils.getInstance(mContext).isMasterMute()&&SRAgent.mInRadioPlaying){  //非静音 并且是在线电台
                    getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC86CONDITION,mContext.getString(R.string.msg_c86),R.string.object_tired,R.string.condition_feed_c86);
                }else {  //静音
                    mMediaType = MEDIA_RADIO;
                    current = TspSceneAdapter.getTspScene(mContext);
                    MultiInterfaceUtils.getInstance(mContext).uploadHotWrodsData();
                    getMessageWithoutTtsSpeak(mContext,TtsConstant.MSGC86_1CONDITION,mContext.getString(R.string.msg_c86_1),R.string.object_tired,R.string.condition_feed_c86_1);
                }
            }

        }
    }

    private void handleTiredTwo(){
        getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC86_1CONDITION,mContext.getString(R.string.msg_c86_1),R.string.object_tired,R.string.condition_feed_c86_1);
    }

    private void handleAttention(){
        getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC91CONDITION,mContext.getString(R.string.msg_c91),R.string.object_attention,R.string.condition_feed_c91);
    }

    private void handleCall(){
        Log.d(TAG, "handleCall() called");
        if(!BluePhoneManager.getInstance(mContext).isBtConnected()){
            getMessageWithoutTtsSpeakOnly(mContext, TtsConstant.MSGC82CONDITION, mContext.getString(R.string.msg_c82), new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    Utils.exitVoiceAssistant();
                    SettingsUtil.startSetting(mContext, SettingsUtil.EXTRA_OPEN_SETTING_VALUE_BLUETOOTH);
                    mHandler.sendEmptyMessageDelayed(MSG_OPEN_BT, 1500);
                }
            });
        }else {
            getMessageWithoutTtsSpeakOnly(mContext,TtsConstant.MSGC83CONDITION,mContext.getString(R.string.msg_c83),R.string.object_call,R.string.condition_feed_c83);
        }
    }

    private void getMessageWithoutTtsSpeak(Context context, String conditionId,String defaultTTS,int object,int condition){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_feed,object,conditionId, condition,ttsText);
                Utils.startTTS(ttsText,PriorityControler.PRIORITY_THREE, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                    }
                });

            }
        });
    }


    private void getMessageWithoutTtsSpeakOnly(Context context, String conditionId,String defaultTTS, int object,int condition){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_feed,object,conditionId, condition,ttsText);
                Utils.startTTSOnly(ttsText,PriorityControler.PRIORITY_THREE, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });

            }
        });
    }

    private void getMessageWithoutTtsSpeakOnly(Context context, String conditionId,String defaultTTS,TTSController.OnTtsStoppedListener listener){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_feed,R.string.object_call,TtsConstant.MSGC82CONDITION, R.string.condition_feed_c82,ttsText);
                Utils.startTTSOnly(ttsText, PriorityControler.PRIORITY_THREE,listener);

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

    private FeedBackController(Context c){
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


}
