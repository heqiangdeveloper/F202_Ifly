package com.chinatsp.ifly.service;

import android.app.Service;
import android.car.CarNotConnectedException;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.ISpeechControlListener;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.ISpeechTtsResultListener;
import com.chinatsp.ifly.ISpeechTtsStopListener;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.MutualVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.remote.RemoteManager;
import com.chinatsp.ifly.source.SourceManager;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.HandleUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.FeedBackController;
import com.chinatsp.ifly.voice.platformadapter.controller.GmsController;
import com.chinatsp.ifly.voice.platformadapter.controller.MusicController;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.controller.VehicleControl;
import com.chinatsp.ifly.voice.platformadapter.controller.VideoController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.iflytek.adapter.common.PcmRecorder;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.sr.SrSession;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpeechRemoteService extends Service {
    private static String TAG = "SpeechRemoteService";
    private SRAgent srAgent = SRAgent.getInstance();
    private List<ListenerObject> mCallbacks = new ArrayList<>();
    private HandleUtils mHandleUtils;

    class ListenerObject {
        int business;
        ISpeechControlListener listener;
        int key;
    }

    @Override
    public void onCreate() {
        LogUtils.d(TAG, "======== onCreate ======== ");
        mHandleUtils = HandleUtils.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final ISpeechControlService.Stub mBinder = new ISpeechControlService.Stub() {
        @Override
        public void registerSpeechListener(int business, ISpeechControlListener listener, int key) {
            SpeechRemoteService.this.registerSpeechListener(business, listener, key);
        }

        @Override
        public void unregisterSpeechListener(int key) {
            SpeechRemoteService.this.unregisterSpeechListener(key);
        }

        @Override
        public int registerStksCommand(String stksJson) {
            SpeechRemoteService.this.registerStksCommand(stksJson);
            return 0;
        }

        @Override
        public int uploadAppStatus(String statusJson) {
            if(statusJson.contains("video::default")){
                VideoController.getInstance(SpeechRemoteService.this).uploadAppStatus(statusJson);
            }
            return srAgent.uploadData(statusJson);
        }

        @Override
        public int uploadAppDict(String dictJson) {
            Log.d(TAG, "dictJson:" + dictJson);
            return srAgent.uploadDict(dictJson);
        }

        @Override
        public void tts(boolean showText, String text, ISpeechTtsStopListener listener) {
            SpeechRemoteService.this.tts(showText, null,text, listener);
        }

        @Override
        public void hideVoiceAssistant() {
            SpeechRemoteService.this.hideVoiceAssistant();
        }

        @Override
        public boolean isHide() {
            return SpeechRemoteService.this.isHide();
        }

        @Override
        public void dispatchSRAction(int bussiness, NlpVoiceModel nlpVoiceModel) {
            SpeechRemoteService.this.dispatchSRAction(bussiness, nlpVoiceModel);
        }

        @Override
        public void dispatchMvwAction(int bussiness, CmdVoiceModel cmdVoiceModel) {
            SpeechRemoteService.this.dispatchMvwAction(bussiness, cmdVoiceModel);
        }

        @Override
        public void dispatchStksAction(int bussiness, CmdVoiceModel cmdVoiceModel) {
            SpeechRemoteService.this.dispatchStksAction(bussiness, cmdVoiceModel);
        }

        @Override
        public void dispatchMutualAction(int bussiness, MutualVoiceModel mutualVoiceModel) {
            SpeechRemoteService.this.dispatchMutualAction(bussiness, mutualVoiceModel);
        }


        @Override
        public void onSearchWeChatContactListResult(String resultJsonArray) {
            LogUtils.d(TAG, "onSearchWeChatContactListResult:\n" + resultJsonArray);
        }

        @Override
        public void getMessageWithTtsSpeak(boolean showText, String conditionId, String defaultTts) {
            SpeechRemoteService.this.getMessageWithTtsSpeak(showText, conditionId, defaultTts);
        }

        @Override
        public void getMessageWithTtsSpeakListener(boolean showText, String conditionId, String defaultTts, ISpeechTtsStopListener listener) {
            SpeechRemoteService.this.getMessageWithTtsSpeakListener(showText, conditionId, defaultTts, listener);
        }

        @Override
        public void getMessageWithoutTtsSpeak(String conditionId, ISpeechTtsResultListener listener) {
            SpeechRemoteService.this.getMessageWithoutTtsSpeak(conditionId, listener);
        }

        @Override
        public String getCurrentCityName() throws RemoteException {
            return SpeechRemoteService.this.getCurrentCityName();
        }

        @Override
        public void waitMultiInterface(String service, String operation) {
            SpeechRemoteService.this.waitMultiInterface(service,operation);
        }

        @Override
        public void resetSrTimeOut(String text) throws RemoteException {
            SpeechRemoteService.this.resetSrTimeOut(text);
        }

        @Override
        public void stopTts() throws RemoteException {

            SpeechRemoteService.this.stopTts();

        }

        @Override
        public void releaseAudioFoucs(String pkg) throws RemoteException {
            SpeechRemoteService.this.releaseAudioFoucs(pkg);
        }

    };



    private String getCurrentCityName() {
        return SharedPreferencesUtils.getString(this, AppConstant.CITY_NAME, "??????");
    }

    private void resetSrTimeOut(String text) {
        //??????????????????
        SRAgent.getInstance().resetSrTimeCount();
        TimeoutManager.saveSrState(BaseApplication.getInstance().getApplicationContext(), TimeoutManager.UNDERSTAND_ONCE, text);
    }

    private void stopTts() {
        TTSController.getInstance(this).stopTTS();
    }

    private void releaseAudioFoucs(String pkg){
        if(Business.CAR.equals(pkg)){
            if(FloatViewManager.getInstance(this).isHide()
                    &&this.getPackageName().equals(AudioFocusUtils.getInstance(this).getCurrentActiveAudioPkg())){
                LogUtils.d(TAG, "releaseAudioFoucs");
                AudioFocusUtils.getInstance(this).releaseVoiceAudioFocus();
            }
        }

    }

    /**
     * ????????????
     *
     * @param business      1:??????  2?????????
     * @param nlpVoiceModel
     */
    public void dispatchSRAction(int business, NlpVoiceModel nlpVoiceModel) {
        if(!isHide())
            AppConstant.setMute =false;
        try {
            for (int i = mCallbacks.size() - 1; i > -1; i--) {
                ListenerObject listenerObject = mCallbacks.get(i);
                if (listenerObject.business == business) {
                    listenerObject.listener.onSrAction(nlpVoiceModel);
                    break;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     *
     * @param business      1:??????  2?????????
     * @param cmdVoiceModel
     */
    public void dispatchMvwAction(int business, CmdVoiceModel cmdVoiceModel) {
        if(!isHide())
            AppConstant.setMute =false;
        try {
            for (int i = mCallbacks.size() - 1; i > -1; i--) {
                ListenerObject listenerObject = mCallbacks.get(i);
                if (listenerObject.business == business) {
                    listenerObject.listener.onMvwAction(cmdVoiceModel);
                    break;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????
     *
     * @param business      1:??????  2?????????
     * @param cmdVoiceModel
     */
    public void dispatchStksAction(int business, CmdVoiceModel cmdVoiceModel) {
        try {
            for (int i = mCallbacks.size() - 1; i > -1; i--) {
                ListenerObject listenerObject = mCallbacks.get(i);
                if (listenerObject.business == business) {
                    listenerObject.listener.onStksAction(cmdVoiceModel);
                    break;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * ??????????????????
     *
     * @param business         1:??????  2?????????
     * @param mutualVoiceModel
     */
    public void dispatchMutualAction(int business, MutualVoiceModel mutualVoiceModel) {
        try {
            for (int i = mCallbacks.size() - 1; i > -1; i--) {
                ListenerObject listenerObject = mCallbacks.get(i);
                if (listenerObject.business == business) {
                    listenerObject.listener.onMutualAction(mutualVoiceModel);
                    break;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        MultiInterfaceUtils.getInstance(this).uploadCmdDefaultData();
    }

    public void registerStksCommand(String stksJson) {
        if(!srAgent.isInited()){
            Log.e(TAG, "registerStksCommand: register error:srAgent.isInited()::"+srAgent.isInited());
            return;
        }
        srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stksJson);
        if (!FloatViewManager.getInstance(this).isHide()) {
            //??????mSrArgu_Old?????????????????????????????????????????????????????????
            srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");
            srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
            srAgent.mSrArgu_Old.scene = SrSession.ISS_SR_SCENE_STKS;
            srAgent.mSrArgu_Old.szCmd = stksJson;
        } else {
            srAgent.stopSRSession();
            srAgent.startSRSession();
        }
    }

    /*
     * author:liuhong
     * date: 2019-05-16
     * ?????????????????????TTS??????,?????????????????????TTS??????.
     * TTS????????????,????????????????????????????????????????????????
     * defaultTts: ??????tts,?????????ttslib.xml?????????,??????????????????tts????????????????????????tts?????????.??????TTS??????????????????
     */
    public void getMessageWithTtsSpeak(boolean showText, String conditionId, final String defaultTts) {
        Log.d(TAG, "lh:conditionId:" + conditionId + ",defaultTts:" + defaultTts);
        getMessageWithTtsSpeakListener(showText, conditionId, defaultTts, null);
    }

    /*
     * author:liuhong
     * date: 2019-05-16
     * ?????????????????????TTS??????,?????????????????????TTS??????.
     * TTS????????????,????????????????????????????????????????????????
     * defaultTts: ??????tts,?????????ttslib.xml?????????,??????????????????tts????????????????????????tts?????????.??????TTS??????????????????
     */
    public void getMessageWithTtsSpeakListener(final boolean showText, String conditionId, final String defaultTts, final ISpeechTtsStopListener listener) {
        Log.d(TAG, "lh:conditionId:"  + conditionId + ",defaultTts:" + defaultTts);


        //???????????????????????????????????????????????????????????????????????????????????????
       if(TtsConstant.FEEDBACK_ATTENTION.equals(conditionId)
               ||TtsConstant.FEEDBACK_SMOKING.equals(conditionId)
               ||TtsConstant.FEEDBACK_CALL.equals(conditionId)
               ||TtsConstant.FEEDBACK_TIRING.equals(conditionId)
               ||TtsConstant.FEEDBACK_TIRING_TWO.equals(conditionId)){
           FeedBackController.getInstance(this).dispatchCommand(conditionId);
           return;
       }

       //????????????
        if(GmsController.GMS_OPEN_CHANGBA.equals(conditionId)
                ||GmsController.GMS_CLOSE_CHANGBA.equals(conditionId)
                ||GmsController.GMS_OPEN_SLEEP.equals(conditionId)
                ||GmsController.GMS_NOT_OPEN_SLEEP.equals(conditionId)
                ||GmsController.GMS_OPEN_SAIDAO.equals(conditionId)
                ||GmsController.GMS_NOT_OPEN_SAIDAO.equals(conditionId)
                ||GmsController.GMS_COLLECTE_MEIDA.equals(conditionId)
                ||GmsController.GMS_NOT_COLLECTE_MEIDA.equals(conditionId)
                ||GmsController.GMS_OPEN_WINDOW.equals(conditionId)
                ||GmsController.GMS_NOT_OPEN_WINDOW.equals(conditionId)
                ||GmsController.GMS_OPEN_SPORT_MODE.equals(conditionId)
                ||GmsController.GMS_CLOSE_SPORT_MODE.equals(conditionId)){
            GmsController.getInstance(this).dispatchCommand(showText,conditionId,defaultTts,listener);
            return;
        }

        //???????????????????????????????????????????????????
        if(TtsConstant.REMOTEVOICEC1.equals(conditionId)
                ||TtsConstant.REMOTEVOICEC2.equals(conditionId)){
            RemoteManager.getInstance(SpeechRemoteService.this).resetNaviSpeak();
            return;
        }

        //????????????
        if(TtsConstant.GUIDEBTNC1CONDITION.equals(conditionId)){
            NlpVoiceModel nlpVoiceModel = SourceManager.getInstance(this).changeSourceVoiceModel();
//            dispatchSRAction(Business.RADIO,nlpVoiceModel); //????????????????????????
            Log.d(TAG, "getMessageWithTtsSpeakListener: ddddddddd");
//            return; ???????????????????????????
        }

        /**
         * ????????????????????????
         */

        if(PriorityControler.getInstance(this).isVehicleTts(conditionId)){
            if(Utils.getCurrentPriority(this)>PriorityControler.PRIORITY_THREE){
                Log.e(TAG, "getMessageWithTtsSpeakListener: "+Utils.getCurrentPriority(this));
                if(listener!=null) {
                    try {
                        listener.onPlayStopped();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            Utils.exitVoiceAssistant();

            PriorityControler.getInstance(this).handleVehicleMaidian(conditionId);
        }

       /* if(TtsConstant.MSGC7CONDITION.equals(conditionId)
                ||TtsConstant.MSGC8CONDITION.equals(conditionId)
                ||TtsConstant.MSGC13CONDITION.equals(conditionId)
                ||TtsConstant.MSGC14CONDITION.equals(conditionId)){
            VehicleControl.getInstance(this).dispatchCommand(showText,conditionId,defaultTts,listener);
            return;
        }*/


        TtsUtils.getInstance(this).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //?????????????????????,???????????????????????????,???????????????,???????????????;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                tts(showText, conditionId,ttsInfo.getTtsText(), listener);
            }

            @Override
            public void onFail() {
                tts(showText,conditionId, defaultTts, listener);
            }
        });
    }

    //?????????????????????TTS??????,?????????TTS??????,?????????TTS??????????????????,????????????????????????????????????.
    public void getMessageWithoutTtsSpeak(String conditionId, final ISpeechTtsResultListener listener) {
        Log.d(TAG, "lh:conditionId:" +  conditionId);

        if(GmsController.STATUS_CHANGBA.equals(conditionId)
           ||GmsController.STATUS_GEAR.equals(conditionId)
                ||GmsController.STATUS_POWER.equals(conditionId)
                ||GmsController.STATUS_MEDIA.equals(conditionId)
                ||GmsController.DRIVE_MODE.equals(conditionId)
                ||GmsController.STATUS_WINDOW.equals(conditionId)){
            GmsController.getInstance(SpeechRemoteService.this).getMessageWithoutTtsSpeak(conditionId,listener);
            return;
        }


        TtsUtils.getInstance(this).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //?????????????????????,???????????????????????????,???????????????,???????????????;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG, "lh:tts callback:" +  ttsInfo.getTtsText());
                try {
                    listener.onTtsCallback(ttsInfo.getTtsText());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
                //???????????????,??????tts??????,??????????????????????????????????????????tts??????
                try {
                    listener.onTtsCallback("");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void tts(boolean showText, String conditionId,String text, final ISpeechTtsStopListener listener) {
        TTSController.OnTtsStoppedListener onTtsStoppedListener = new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                try {

                   /* if(TtsConstant.REMOTEVOICEC1.equals(conditionId)
                    ||TtsConstant.REMOTEVOICEC2.equals(conditionId)){
                        MXSdkManager.getInstance(SpeechRemoteService.this).startNavi();
                        RemoteManager.getInstance(SpeechRemoteService.this).resetNaviSpeak();
                    }*/

                   /* boolean needRestoreMute = SharedPreferencesUtils.getBoolean(SpeechRemoteService.this, AppConstant.KEY_NEED_RESTORE_MUTE, false);

                    if(needRestoreMute) {
                        AppConfig.INSTANCE.mCarAudioManager.setMasterMute(true, 0);
                    }*/

                    if (listener != null) {
                        listener.onPlayStopped();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //????????????
        if("????????????".equals(text)||"QQ??????".equals(text)
                ||"USB??????".equals(text)||"????????????".equals(text)
                ||"????????????".equals(text)||"????????????".equals(text)||"?????????".equals(text)){
            boolean guide = SharedPreferencesUtils.getBoolean(SpeechRemoteService.this, AppConstant.KEY_VOICE_BROADCAST,AppConstant.VALUE_VOICE_SPEAK);
            if(!guide){
                Log.e(TAG, "tts: guide::"+guide);
                return;
            }
            NlpVoiceModel nlpVoiceModel = SourceManager.getInstance(this).changeSourceVoiceModel();
            dispatchSRAction(Business.RADIO,nlpVoiceModel); //????????????????????????
            Log.d(TAG, "getMessageWithTtsSpeakListener: ddddddddd");
//            return; ???????????????????????????
        }

        if (showText) {
      /*      if("????????????".equals(text)) //?????????????????????????????????????????????????????????
                mHandleUtils.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utils.startTTS(text, onTtsStoppedListener);
                    }
                },HandleUtils.TIME_SRC_TTS);
            else*/
               Utils.startTTS(text, onTtsStoppedListener);
        } else {
            TTSController.getInstance(this).startTTSOnly(text, onTtsStoppedListener);
        }
    }

    public void hideVoiceAssistant() {
        if (!FloatViewManager.getInstance(this).isHide()) {
            FloatViewManager.getInstance(this).hide(FloatViewManager.TYPE_HIDE_OTHER);
        }
    }

    public void waitMultiInterface(String service, String operation) {
        MultiInterfaceUtils.getInstance(this).uploadAppStatusData(true, service, "default");

        IntentEntity intentEntity = new IntentEntity();
        intentEntity.service = service;
        intentEntity.operation = operation;

        MultiInterfaceUtils.getInstance(this).saveMultiInterfaceSemantic(intentEntity);
    }

    public boolean isHide() {
        return FloatViewManager.getInstance(this).isHide();
    }

    public void registerSpeechListener(int business, ISpeechControlListener listener, int key) {
        LogUtils.d(TAG, "registerSpeechListener start,mListenerList.size : " + mCallbacks.size()+"business::"+business);
        if (listener == null) {
            LogUtils.e(TAG, "register listener is null");
            return;
        }
        synchronized (this) {
            ListenerObject obj = new ListenerObject();
            obj.business = business;
            obj.key = key;
            obj.listener = listener;
            mCallbacks.add(obj);
        }
        LogUtils.d(TAG, "registerSpeechListener end, mListenerList.size : " + mCallbacks.size());
    }

    protected void unregisterSpeechListener(int key) {
        synchronized (this) {
            LogUtils.d(TAG, "unregisterSpeechListener start,mListenerList.size : " + mCallbacks.size());
            for (int i = 0; i < mCallbacks.size(); i++) {
                int k = mCallbacks.get(i).key;
                if (k == key) {
                    mCallbacks.remove(i);
                    break;
                }
            }
            LogUtils.d(TAG, "unregisterSpeechListener end,mListenerList.size : " + mCallbacks.size());
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "======== onDestroy ======== ");
        mCallbacks.clear();
    }
}
