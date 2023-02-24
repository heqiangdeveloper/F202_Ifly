package com.chinatsp.ifly.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.iflytek.adapter.sr.SRAgent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AudioFocusUtils {
    private static final String TAG = "AudioFocusUtils";
    private AudioManager mAudioManager;
    private AudioFocusRequest mGainFocusReq;
    private Context mContext;
    private static AudioFocusUtils instance = null;

    private AudioFocusUtils(Context context) {
        mContext = context.getApplicationContext();
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static synchronized AudioFocusUtils getInstance(Context context) {
        if (instance == null) {
            instance = new AudioFocusUtils(context);
        }
        return instance;
    }

    public String getCurrentActiveAudioPkg() {
        Method getCurrentActiveAudioPkg = null;
        try {
            getCurrentActiveAudioPkg = mAudioManager.getClass().getMethod("getCurrentActiveAudioPkg");
        } catch (NoSuchMethodException e) {
            LogUtils.w(TAG, "NoSuchMethodException: getCurrentActiveAudioPkg:");
        }
        if (getCurrentActiveAudioPkg != null) {
            try {
                String audioPkg = (String) getCurrentActiveAudioPkg.invoke(mAudioManager);
                LogUtils.v(TAG, "active audio pkg:" + audioPkg);
                return audioPkg;
            } catch (IllegalAccessException e) {
                LogUtils.w(TAG, "IllegalAccessException e:" + e.toString());
            } catch (InvocationTargetException e) {
                LogUtils.w(TAG, "InvocationTargetException e:" + e.toString());
            }
        }
        return null;
    }

    public boolean isMasterMute() {
        Method isMasterMute = null;
        try {
            isMasterMute = mAudioManager.getClass().getMethod("isMasterMute");
        } catch (NoSuchMethodException e) {
            LogUtils.w(TAG, "NoSuchMethodException: isMasterMute");
        }
        if (isMasterMute != null) {
            try {
                boolean isMute = (boolean) isMasterMute.invoke(mAudioManager);
                LogUtils.v(TAG, "isMasterMute:" + isMute);
                return isMute;
            } catch (IllegalAccessException e) {
                LogUtils.w(TAG, "IllegalAccessException e:" + e.toString());
            } catch (InvocationTargetException e) {
                LogUtils.w(TAG, "InvocationTargetException e:" + e.toString());
            }
        }
        return false;
    }

    public int requestVoiceAudioFocus(int streamType) {
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
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int focusChange) {
                            LogUtils.d(TAG, "onAudioFocusChange:" + focusChange);
                            switch (focusChange) {
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://Pause playback
                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                case AudioManager.AUDIOFOCUS_LOSS://Stop playback
                                    TTSController.getInstance(mContext).stopTTS();
                                    TTSController.getInstance(mContext).onPlayInterrupted();
                                    EventBusUtils.sendMainMessage(MessageEvent.ACTION_GREY);
                                    Utils.exitVoiceAssistant();
                                    releaseVoiceAudioFocus();
                                   /* if (!FloatViewManager.getInstance(mContext).isHide()) {
                                        String activeAudioPkg = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
                                        if(AppConstant.PACKAGE_NAME_MUSIC.equals(activeAudioPkg)
                                                || AppConstant.PACKAGE_NAME_RADIO.equals(activeAudioPkg)
                                                ||AppConstant.PACKAGE_NAME_VCAR.equals(activeAudioPkg)
                                                ||AppConstant.PACKAGE_NAME_PHONE.equals(activeAudioPkg)) {
                                            FloatViewManager.getInstance(mContext).hide();
                                        } else {
                                            SRAgent.getInstance().stopSRSession();
                                        }
                                    } else {
                                        releaseVoiceAudioFocus();
                                    }*/
                                    break;
                                case AudioManager.AUDIOFOCUS_GAIN://Resume playback
                                   /* if (!FloatViewManager.getInstance(mContext).isHide()) {
                                        SRAgent.getInstance().startSRSession();
                                    } else {
                                        releaseVoiceAudioFocus();
                                    }*/
                                    break;
                            }
                        }
                    });
            mGainFocusReq = requestBuilder.build();
        }
        int ret = mAudioManager.requestAudioFocus(mGainFocusReq);
        LogUtils.d(TAG, "requestVoiceAudioFocus, ret=" + ret);
        return ret;
    }

    public void releaseVoiceAudioFocus() {
        if (mGainFocusReq != null) {
            int ret = mAudioManager.abandonAudioFocusRequest(mGainFocusReq);
            LogUtils.d(TAG, "releaseVoiceAudioFocus, ret=" + ret);
        }
    }
}
