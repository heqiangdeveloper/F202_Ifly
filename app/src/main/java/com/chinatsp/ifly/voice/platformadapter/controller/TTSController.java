package com.chinatsp.ifly.voice.platformadapter.controller;

import android.app.PendingIntent;
import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.adapter.ttsservice.ITtsClientListener;
import com.chinatsp.adapter.ttsservice.OnTtsInitedListener;
import com.chinatsp.adapter.ttsservice.TtsServiceAgent;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.HandleUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformAdapterClient;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.phone.bean.CallContact;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.sr.SRAgent;

public class TTSController implements ITtsClientListener {

    private static final String TAG = "xyj_TTSController";
    private static TTSController mTTSController;
    private TtsServiceAgent agent = null;
    private Context context;
    private OnTtsStoppedListener mTtsStoppedListener;
    private boolean isTtsPlaying;
    private OnTtsInitedListener mTtsInitedListener;
    private int streamType;
    private boolean ttsOnly;
    private String mTtsWords;
    private int mCurrentPriority = PriorityControler.PRIORITY_IDEL;

    public static TTSController getInstance(Context context) {
        if (mTTSController == null) {
            synchronized (TTSController.class){
                if(mTTSController==null){
                    mTTSController = new TTSController(context.getApplicationContext());
                }
            }
        }
        return mTTSController;
    }

    private TTSController(Context context) {
        this.context = context;
    }

    public void init(int streamType, OnTtsInitedListener listener) {
        this.streamType = streamType;
        this.mTtsInitedListener = listener;
        agent = TtsServiceAgent.getInstance();

        String actor = SharedPreferencesUtils.getString(context, AppConstant.KEY_CURRENT_ACTOR,  AppConstant.DEFAULT_VALUE_CURRENT_ACTOR);
        int value = Utils.actorToValue(actor);
        agent.initService(this, context.getApplicationContext(), value, streamType);
        LogUtils.i(TAG, "startTTS init!");
    }
    public void startTTS(String words) {
        LogUtils.i(TAG, "startTTS words:" + words);
        startTTS(words, PriorityControler.PRIORITY_FOUR,null);
        ttsOnly = false;
    }

    public void startTTSOnly(String words) {
        LogUtils.i(TAG, "startTTSOnly words:" + words);
        startTTSOnly(words, PriorityControler.PRIORITY_FOUR,null);
        ttsOnly = true;
    }

    public void startTTS(String words,int priority) {
        LogUtils.i(TAG, "startTTS words:" + words);
        startTTS(words, priority,null);
        ttsOnly = false;
    }

    public void startTTSOnly(String words,int priority) {
        LogUtils.i(TAG, "startTTSOnly words:" + words);
        startTTSOnly(words, priority,null);
        ttsOnly = true;
    }

    private OnTtsStoppedListener mDefaultTtsStoppedListener = new OnTtsStoppedListener() {
        @Override
        public void onPlayStopped() {
            Log.i(TAG,"mDefaultTtsStoppedListener onPlayStopped");
            if (!FloatViewManager.getInstance(context).isHide() && !ttsOnly) {
                EventBusUtils.sendMainMessage(MessageEvent.ACTION_GREY);
            }else
                EventBusUtils.sendEndSpeech();
        }
    };

    public void startTTS(final String words, OnTtsStoppedListener listener) {
        Log.d(TAG, "startTTS() called with: words = [" + words);
        startTTSInner(words, PriorityControler.PRIORITY_FOUR,listener);
        ttsOnly = false;
    }

    public void startTTSOnly(final String words, OnTtsStoppedListener listener) {
        LogUtils.i(TAG, "startTTSOnly words:" + words);
        startTTSInner(words,PriorityControler.PRIORITY_FOUR,listener);
        ttsOnly = true;
    }


    public void startTTS(final String words,int priority, OnTtsStoppedListener listener) {
        Log.d(TAG, "startTTS() called with: words = [" + words);
        startTTSInner(words, priority,listener);
        ttsOnly = false;
    }

    public void startTTSOnly(final String words,int priority, OnTtsStoppedListener listener) {
        LogUtils.i(TAG, "startTTSOnly words:" + words);
        startTTSInner(words,priority,listener);
        ttsOnly = true;
    }

    private void startTTSInner(String words,int priority, OnTtsStoppedListener listener) {
        if(agent==null){
            Log.e(TAG, "startTTSInner: the agent is null");
            if(FloatViewManager.getInstance(context).isHide()
                    &&context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())){
                Log.d(TAG, "startTTSInner: releaseVoiceAudioFocus");
                AudioFocusUtils.getInstance(context).releaseVoiceAudioFocus();
            }
            mCurrentPriority = PriorityControler.PRIORITY_IDEL;
            return;
        }

        if (TextUtils.isEmpty(words)) {
            LogUtils.d(TAG, "startTTS words is empty");
//            return;
            words = "。";  //为空的时候，播报。，防止发呆
        }



        words = TtsUtils.getInstance(context).replaceTTSMark(words);
        Log.d(TAG, "startTTSInner: replaceTTSMark::"+words+"....priority------>"+priority);


        //如果tts过长，只播报前面 100 个字
        if(words.length()>150){
            Log.d(TAG, "startTTSInner: "+words.length());
            words = words.substring(0,100);
        }


        if("。".equals(words)){
            words = "<pause type=#3>";//这个标签也是播一小段空内容
        }

        if(mCurrentPriority>priority){
            Log.e(TAG, "startTTSInner: mCurrentPriority:"+mCurrentPriority+"....priority::"+priority);
            return;
        }

        //播放之前，清除切源播放逻辑
        HandleUtils.getInstance().removeCallbacksAndMessages(null);

        PriorityControler.getInstance(context).hideOtherFloatView();

        mCurrentPriority = priority;

        this.mTtsStoppedListener = listener;

        //动态设置超时时间, 播报文本共用时间+5秒默认超时 (播报速度与机器性能相关,延时不一定准确)
//        int timeoutTime = words.length() * 300 + 5 * 1000;
//        SRAgent.getInstance().setTimeoutTime(timeoutTime);

        try {
            int request = 1;
            if(!context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())) {
                request = AudioFocusUtils.getInstance(context).requestVoiceAudioFocus(AudioManager.STREAM_ALARM);
            }
            if(request!=1&&!words.equals(context.getString(R.string.btn119))){
                mCurrentPriority = PriorityControler.PRIORITY_IDEL;
                Log.e(TAG, "startTTSInner: request audio error");
                return;
            }
            mTtsWords = words;

            int code = agent.startSpeak(words);
            if(code==-1||code==TtsServiceAgent.NOT_INIT_ERROR){//说明播放失败，界面没有显示，并且语音占据焦点，需要释放
                if(FloatViewManager.getInstance(context).isHide()
                        &&context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())){
                    Log.d(TAG, "startTTSInner: releaseVoiceAudioFocus");
                    AudioFocusUtils.getInstance(context).releaseVoiceAudioFocus();
                }
                mCurrentPriority = PriorityControler.PRIORITY_IDEL;
                mTtsWords = null;
            }

            LogUtils.i(TAG, "resultcode:" + code);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        agent.releaseService();
        mTtsStoppedListener = null;
        isTtsPlaying = false;
    }

    public void stopTTS() {
        LogUtils.i(TAG, "stopTTS words" + Log.getStackTraceString(new Throwable()));
        int code = 0;
        try {
            code = agent.stopSpeak();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        LogUtils.i(TAG, "resultcode:" + code);
    }

    public void setParam(int id, int value) {
        LogUtils.i(TAG, "setParam id:" + id + " value:" + value);
        int code = 0;
        try {
            code = agent.setParam(id, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        LogUtils.i(TAG, "resultcode:" + code);
    }

    public boolean isTtsPlaying() {
        return isTtsPlaying;
    }

    @Override
    public void onPlayBegin() {
        LogUtils.i(TAG, "onPlayBegin:" + System.currentTimeMillis()+".."+ttsOnly);
        isTtsPlaying = true;

        if(!FloatViewManager.getInstance(context).isHide()) {
            SRAgent.getInstance().stopSRRecord();
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    SRAgent.getInstance().stopSRSession();
                }
            });
        }

        //Tts申请焦点
        if(!context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())) {
            AudioFocusUtils.getInstance(context).requestVoiceAudioFocus(streamType);
        }
        if(!FloatViewManager.getInstance(context).isHide()) {
            EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_NORMAL, null);
        }
    }

    @Override
    public void onPlayCompleted() {
        LogUtils.i(TAG, "onPlayCompleted:" + System.currentTimeMillis() + ",ttsOnly:" + ttsOnly);
        isTtsPlaying = false;

        mDefaultTtsStoppedListener.onPlayStopped();

        if (mTtsStoppedListener != null) {
            mTtsStoppedListener.onPlayStopped();
            LogUtils.i(TAG, "mTtsStoppedListener.onPlayStopped()");
        } else {
            LogUtils.i(TAG, "mTtsStoppedListener == null");
        }

        mTtsStoppedListener = null;

        //二轮交互语义处理
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            PlatformHelp.getInstance().getPlatformClient().onRestoreMultiSemantic();
        }

        if(!FloatViewManager.getInstance(context).isHide() && !ttsOnly) {
//            SRAgent.getInstance().stopSRRecord();
//            SRAgent.getInstance().stopSRSession();  在 begin 已经调用过了，这里不要再次调用，耗时操作
            int returnId = SRAgent.getInstance().startSRSession();
            EventBusUtils.sendSRResult(returnId);
            EventBusUtils.sendTalkMessage(MessageEvent.ACTION_ANIM);
            EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_LISTENING, null);
        }

        //助手界面已退出，Tts直接释放焦点
        if (FloatViewManager.getInstance(context).isHide() && context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())) {
            if(BluePhoneManager.getInstance(context).getCallStatus() == CallContact.CALL_STATE_INCOMING
                    ||BluePhoneManager.getInstance(context).getCallStatus() == CallContact.CALL_STATE_ACTIVE){//来电中或者通话中，在通话中是失去焦点导致释放焦点
                Log.e(TAG, "onPlayCompleted: "+BluePhoneManager.getInstance(context).getCallStatus());
            }else
               AudioFocusUtils.getInstance(context).releaseVoiceAudioFocus();
        }

        mTtsWords = null;
        mCurrentPriority = PriorityControler.PRIORITY_IDEL;
        //恢复默认5秒超时时间
//        SRAgent.getInstance().setTimeoutTime(5 * 1000);
    }

    @Override
    public void onPlayInterrupted() {
        LogUtils.i(TAG, "onPlayInterrupted");
        isTtsPlaying = false;
        mTtsWords = null;
        mCurrentPriority = PriorityControler.PRIORITY_IDEL;
       /* if (mTtsStoppedListener != null) {
            mTtsStoppedListener.onPlayStopped();
        }*/
    }

    @Override
    public void onProgressReturn(int textindex, int textlen) {
        //LogUtils.i(TAG, "onProgressReturn:" + textindex + " " + textlen);
    }

    @Override
    public void onTtsInited(boolean state, int errId) {
        LogUtils.i(TAG, "onTtsInited! state:" + state + " ,errId:" + errId);
        if (mTtsInitedListener != null) {
            if (state) {
                //mTtsInitedListener.onTtsInited();
                LogUtils.d("heqiang", "TTSController onTtsInited");
                AppConfig.INSTANCE.ttsEngineInited = true;
                agent.initTtsParam();
            }
        } else {
            LogUtils.d(TAG, "No set TtsInitedListener");
        }
    }

    public interface OnTtsStoppedListener {
        void onPlayStopped();
    }

    public void releaseVoiceAudioFocus(){
        Log.d(TAG, "releaseVoiceAudioFocus() called::"+AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg());
        if (FloatViewManager.getInstance(context).isHide()&&context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())) {
            AudioFocusUtils.getInstance(context).releaseVoiceAudioFocus();
        }
    }

    public int getmCurrentPriority(){
        return mCurrentPriority;
    }

    public void resetCurrentPriority()
    {
         mCurrentPriority = PriorityControler.PRIORITY_IDEL;
    }

    public String getTtsWords(){
        return mTtsWords;
    }
}
