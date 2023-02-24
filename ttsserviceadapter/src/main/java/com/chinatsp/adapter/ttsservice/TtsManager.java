package com.chinatsp.adapter.ttsservice;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

/**
 * 提供给其它应用进行TTS播报封装类,（带焦点申请和释放）, 使用时务必要先调用init()进行初始化
 */
public class TtsManager implements ITtsClientListener {
    private static final String TAG = "TtsManager";
    private static TtsManager ttsManager;
    private TtsServiceAgent agent = null;
    private Context context;
    private OnTtsPlayStateListener mTtsPlayStateListener;
    private OnTtsInitedListener mTtsInitedListener;
    private AudioManager mAudioManager;
    private AudioFocusRequest mGainFocusReq;
    private int streamType;
    private ChangeContentObserver mChangeContentObserver = null;

    public static TtsManager getInstance(Context context) {
        if (ttsManager == null) {
            ttsManager = new TtsManager(context);
        }
        return ttsManager;
    }

    private TtsManager(Context context) {
        this.context = context;
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mChangeContentObserver = new ChangeContentObserver();
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("current_actor"), true, mChangeContentObserver);

    }

    public void init(OnTtsInitedListener listener) {
        int actor = ActorUtils.getActor(context);
        Log.d(TAG, "init() called with: actor = [" + actor + "]");
       this.init(actor, AudioManager.STREAM_ALARM, listener);
        Log.i(TAG, "startTTS init! default" );
    }

    public void init(int actor, int streamType, OnTtsInitedListener listener) {
        this.streamType = streamType;
        this.mTtsInitedListener = listener;
        agent = TtsServiceAgent.getInstance();
        agent.initService(this, context.getApplicationContext(), actor, streamType);
        Log.i(TAG, "startTTS init! custom");
    }

    public void startTTS(String text) {
        startTTS(text, null);
    }

    public void startTTS(String text, OnTtsPlayStateListener listener) {
        Log.i(TAG, "startTTS text:" + text);
        if (TextUtils.isEmpty(text)) {
            Log.d(TAG, "startTTS words is empty");
            return;
        }
        this.mTtsPlayStateListener = listener;
        try {
            int code = agent.startSpeak(text);
            Log.i(TAG, "resultcode:" + code);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        agent.releaseService();
        context.getContentResolver().unregisterContentObserver(mChangeContentObserver);
    }

    public void stopTTS() {
        Log.i(TAG, "stopTTS words");
        try {
            int code = agent.stopSpeak();
            Log.i(TAG, "resultcode:" + code);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPlayBegin() {
        Log.i(TAG, "onPlayBegin");
        requestTtsAudioFocus();
        if (mTtsPlayStateListener != null) {
            mTtsPlayStateListener.onPlayBegin();
        }
    }

    @Override
    public void onPlayCompleted() {
        Log.i(TAG, "onPlayCompleted");
        releaseTtsAudioFocus();
        if (mTtsPlayStateListener != null) {
            mTtsPlayStateListener.onPlayStop();
        }
        mTtsPlayStateListener = null;
    }

    @Override
    public void onPlayInterrupted() {
        Log.i(TAG, "onPlayInterrupted");
        releaseTtsAudioFocus();
        if (mTtsPlayStateListener != null) {
            mTtsPlayStateListener.onPlayStop();
        }
        mTtsPlayStateListener = null;
    }

    @Override
    public void onProgressReturn(int textindex, int textlen) {
    }

    @Override
    public void onTtsInited(boolean state, int errId) {
        Log.i(TAG, "onTtsInited! state:" + state + " ,errId:" + errId);
        if(mTtsInitedListener != null) {
            if(state) {
                mTtsInitedListener.onTtsInited();
            }
        }  else {
            Log.d(TAG, "No set TtsInitedListener");
        }
    }

    public interface OnTtsPlayStateListener {
        void onPlayBegin();

        void onPlayStop();
    }

    private void requestTtsAudioFocus() {
        if (mGainFocusReq == null) {
            int usage;
            int contentType;
            if (AudioManager.STREAM_ALARM == streamType) {
                usage = AudioAttributes.USAGE_ALARM;
                contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
            } else if (AudioManager.STREAM_MUSIC == streamType) {
                usage = AudioAttributes.USAGE_MEDIA;
                contentType = AudioAttributes.CONTENT_TYPE_MUSIC;
            } else {
                usage = AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY;
                contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
            }

            AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
            attributesBuilder.setUsage(usage)
                    .setContentType(contentType);
            AudioFocusRequest.Builder requestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            requestBuilder.setAudioAttributes(attributesBuilder.build())
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            Log.d(TAG, "onAudioFocusChange:" + focusChange);
                            switch (focusChange) {
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://Pause playback
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                case AudioManager.AUDIOFOCUS_LOSS://Stop playback
                                    stopTTS();
                                    TtsManager.getInstance(context).onPlayInterrupted();
                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN://Resume playback
                                    break;
                            }
                        }
                    });
            mGainFocusReq = requestBuilder.build();
        }
        int ret = mAudioManager.requestAudioFocus(mGainFocusReq);
        Log.d(TAG, "requestTtsAudioFocus, ret=" + ret);
    }

    public void releaseTtsAudioFocus() {
        if (mGainFocusReq != null) {
            int ret = mAudioManager.abandonAudioFocusRequest(mGainFocusReq);
            Log.d(TAG, "releaseTtsAudioFocus, ret=" + ret);
            mGainFocusReq = null;
        }
    }


    /**
     * 监听数据变化，当 actorsettingfragment改变发音人时，这里也需要变动
     */
    class ChangeContentObserver extends ContentObserver {
        public ChangeContentObserver (Handler handler) {
            super(handler);
        }
        public ChangeContentObserver () {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int actor = ActorUtils.getActor(context);
            try {
                agent.setParam(1280,actor);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d("TAG", "ChangeContentObserver --> onChange(" + actor + ") ");
        }
    }

}
