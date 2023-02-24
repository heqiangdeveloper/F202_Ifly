package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IMusicController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.speech.util.NetworkUtil;

import java.lang.ref.WeakReference;

public class MusicController extends BaseController implements IMusicController {
    private static final String TAG = "MusicController";
    private static MusicController instance;
    private static final int MSG_TTS = 1000;
    private static final int MSG_OPEN_MUSIC = 1001;
    public Context context;
    private IntentEntity intentEntity;
    private String mBtAudioState = "";
    private ISpeechControlService mSpeechControlService;

    private static  String AUDIOFOCUS_LOSS = "longLose";
    private static  String AUDIOFOCUS_LOSS_TRANSIENT = "shortLose";
    private static  String AUDIOFOCUS_GAIN = "longGet";
    private boolean isColdStart = false;


    private MyHandler myHandler = new MyHandler(this);

    public static MusicController getInstance(Context c){
        if(instance==null){
            synchronized (MusicController.class){
                if(instance==null)
                    instance = new MusicController(c);
            }
        }
        return instance;
    }

    private MusicController(Context context) {
        this.context = context.getApplicationContext();
        IntentFilter filter = new IntentFilter("com.chinatsp.isbtaudio");
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    mBtAudioState = intent.getStringExtra("isBtAudio");
                    Log.d(TAG, "onReceive() called with: mBtAudioState = [" + mBtAudioState + "], intent = [" + intent + "]");
                }
            }
        },filter);
    }

    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        this.intentEntity = intentEntity;

        Log.d(TAG, "srAction() called with: mBtAudioState = [" + mBtAudioState + "]");
        if(AUDIOFOCUS_LOSS_TRANSIENT.equals(mBtAudioState)){  //如果是短时间失去焦点，
            String mainMsg = context.getString(R.string.systemC22);
            Utils.getMessageWithoutTtsSpeak(context, TtsConstant.YINYUEC48CONDITION, mainMsg, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    Utils.startTTS(tts, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            startApp(AppConstant.PACKAGE_NAME_MUSIC);
                            Utils.exitVoiceAssistant();
                        }
                    });
                }
            });
            return;
        }



        startApp(AppConstant.PACKAGE_NAME_MUSIC);

        if (NetworkUtil.isNetworkAvailable(context)) {
            String mainMsg = context.getString(R.string.music_open_have_net);
            Utils.eventTrack(context,R.string.skill_music, R.string.music_open, R.string.music_open1, TtsConstant.YINYUEC1CONDITION, R.string.music_mr);
            Utils.getMessageWithoutTtsSpeak(context, TtsConstant.YINYUEC1CONDITION, mainMsg, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    Message msg = myHandler.obtainMessage(MSG_OPEN_MUSIC, tts);
                    myHandler.sendMessageDelayed(msg, 1000);
                }
            });
        } else {
            String mainMsg = context.getString(R.string.music_open_no_net);
            Utils.eventTrack(context,R.string.skill_music, R.string.music_open, R.string.music_open1, TtsConstant.YINYUEC2CONDITION, R.string.music_noie);
            Utils.getMessageWithoutTtsSpeak(context, TtsConstant.YINYUEC1CONDITION, mainMsg, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    Message msg = myHandler.obtainMessage(MSG_OPEN_MUSIC, mainMsg);
                    myHandler.sendMessageDelayed(msg, 1000);
                }
            });
        }
    }

    @Override
    public void mvwAction(MvwLParamEntity lParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity o) {

    }

    /**
     * 这里需要判断是不是远程启动
     */
    public void startNavi(){
        if(!isColdStart){
            Log.e(TAG, "startNavi: "+isColdStart);
            return;
        }
        if (mSpeechControlService != null) {
            try {
                mSpeechControlService.dispatchSRAction(Business.MUSIC, openMusicByVoiceModel());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private static class MyHandler extends Handler {

        private final WeakReference<MusicController> musicControllerWeakReference;

        private MyHandler(MusicController musicController) {
            this.musicControllerWeakReference = new WeakReference<>(musicController);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            final MusicController musicController = musicControllerWeakReference.get();
            if (musicController == null) {
                LogUtils.d(TAG, "musicController == null");
                return;
            }
            switch (msg.what) {
                case MSG_TTS:
                    musicController.startTTS((String) msg.obj);
                    break;
                case MSG_OPEN_MUSIC:
                    musicController.waitOpenMusicMultiInterface();
                    String word = (String) msg.obj;
                    if (musicController.isForeground()) {
                        musicController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                //重新计算超时
                                if("音乐已打开，请检查网络".equals(word)){
                                    Log.d(TAG, "onPlayStopped: 111");
                                    FloatViewManager.getInstance(musicController.context).hide();
                                }else {
                                    Log.d(TAG, "onPlayStopped: 222");
                                    SRAgent.getInstance().resetSrTimeCount();
                                    TimeoutManager.saveSrState(musicController.context, TimeoutManager.UNDERSTAND_ONCE, "");
                                }
                            }
                        });
                    } else {
                        Message msg2 = new Message();
                        msg2.what = msg.what;
                        msg2.obj = msg.obj;
                        sendMessageDelayed(msg2, 2 * 1000);
                    }
                    break;
            }
        }
    }

    private boolean isForeground() {
        return AppConstant.PACKAGE_NAME_MUSIC.equals(ActivityManagerUtils.getInstance(context).getTopPackage());
    }

    private void waitOpenMusicMultiInterface() {
        LogUtils.d(TAG, "waitOpenMusicMultiInterface with data");

        //上传音乐位于前台状态
        String curFocusPackage = SharedPreferencesUtils.getString(context, AppConstant.KEY_AUDIO_FOCUS_PKGNAME, "");
        boolean isPlaying = AppConstant.PACKAGE_NAME_MUSIC.equals(curFocusPackage);
        MultiInterfaceUtils.getInstance(context).uploadMediaStatusData(true, PlatformConstant.Service.MUSIC, isPlaying, null, null);
        //打开音乐二次交互无法处理
        //MultiInterfaceUtils.getInstance(context).saveMultiInterfaceSemantic(intentEntity);
    }

    private NlpVoiceModel openMusicByVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "musicX";
        nlpVoiceModel.text = "开始导航";
        nlpVoiceModel.operation = "RANDOM_SEARCH";
        nlpVoiceModel.semantic ="{\"slots\":{}}";
        nlpVoiceModel.dataEntity ="{\"result\":null}";
        return nlpVoiceModel;
    }

    public NlpVoiceModel controlModelByVoice(String mode){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "musicX";
        nlpVoiceModel.operation = "INSTRUCTION";
        if("列表循环".equals(mode)||"顺序循环".equals(mode))
             nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"LOOP\"}}";
        else if("单曲循环".equals(mode))
            nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"CYCLE\"}}";
        else if("随机播放".equals(mode)||"随机循环".equals(mode))
            nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"RANDOM\"}}";
        return nlpVoiceModel;
    }
}
