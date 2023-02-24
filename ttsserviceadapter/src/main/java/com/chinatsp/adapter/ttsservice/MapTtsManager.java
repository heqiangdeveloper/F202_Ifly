package com.chinatsp.adapter.ttsservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.adapter.ttsservice.aidl.ITtsAgentListener;
import com.iflytek.adapter.ttsservice.aidl.TtsServiceAidl;

import java.io.PipedReader;
import java.io.PrintStream;

/**
 * 提供给其它应用进行TTS播报封装类,（带焦点申请和释放）, 使用时务必要先调用init()进行初始化
 */
public class MapTtsManager implements ITtsClientListener {
    private static final String TAG = "MapTtsManager";
    public static final int TYPE_MAP = 1001;
    private static MapTtsManager ttsManager;
    private Context mContext;
    private ITtsAgentListener mTtsListener;;
    private boolean mConnected = false;
    private TtsServiceAidl mTXZTtsService;
    private ITtsAgentListener agentTXZListener;
    private ITtsClientListener mClient;
    private int mStreamType  = AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE;
    public static final long MAP_ID = 1001L;

    private static final int MSG_RESTAR_CONN = 1;
    private static final int MSG_TTS_PLAYING = 2;
    private static final int TIME_RESTAR_CONN = 5000;
    private static final int TIME_RESET_TTSPLAYING = 1000*30;

    private boolean isTTsPlaying = false;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_RESTAR_CONN:
                    init(mTtsListener);
                    break;
                case MSG_TTS_PLAYING:
                    Log.e(TAG, "handleMessage: the tts is no compeled callback!!!!!");
                    isTTsPlaying = false;  //超时机制，防止tts没有回调，导致isTTsPlaying为true，继而导致导航不能播报
                    break;
            }
        }
    };

    public static MapTtsManager getInstance(Context context) {
        if (ttsManager == null) {
            ttsManager = new MapTtsManager(context);
        }
        return ttsManager;
    }

    private MapTtsManager(Context context) {
        this.mContext = context;

    }

    public void init(){
        init(null);
    }

    public void init(ITtsAgentListener listener) {
        mTtsListener = listener;
        mClient = this;
        initTtsService();
        Log.i(TAG, "startTTS init! custom");
    }

    public void setStreamType(int stream){
        Log.d(TAG, "setStreamType() called with: stream = [" + stream + "]");
        mStreamType = stream;
    }

    public void startTTS(String text) {
        Log.d(TAG, "startTTS() called with: text = [" + text + "], listener = ["  + "]");
        startTTS(text,mTtsListener);
    }

    public void startTTS(String text,ITtsAgentListener listener) {
        Log.d(TAG, "startTTS() called with: text = [" + text + "], listener = ["  + "]");
        try {
            if(!mConnected){
                Log.e(TAG, "startTTS: begin init !!!!");
                return;
            }

            if(isTTsPlaying){
                Log.e(TAG, "startTTS: the tts is playing!!!");
                return;
            }

            mTtsListener = listener;

            if (TextUtils.isEmpty(text)){
                agentTXZListener.onPlayInterrupted();
                return;
            }

            if (mTXZTtsService != null){
                mTXZTtsService.setParam(null,TYPE_MAP,mStreamType);
                mTXZTtsService.startSpeak(agentTXZListener,text);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    public void stopTTS() {
        try {
            isTTsPlaying = false;
            if(!mConnected){
                Log.e(TAG, "startTTS: begin init !!!!");
                return;
            }
            if (mTXZTtsService != null)
               mTXZTtsService.stopSpeak(agentTXZListener);
        } catch (RemoteException e) {
                e.printStackTrace();
        }
    }


    public void release() {
        Log.d(TAG, "release() called::"+mConnected);
        try {
            if (mConnected) {
                stopTTS();
                if(mTXZTtsService!=null)
                    mTXZTtsService.releaseClient(agentTXZListener);
                mContext.unbindService(mTXZConnection);
                agentTXZListener = null;
                mConnected = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onPlayBegin() {
        Log.i(TAG, "onPlayBegin");
        try {
            isTTsPlaying = true;
            if (mTtsListener != null) {
                mTtsListener.onPlayBegin();
            }
            if(mHandler.hasMessages(MSG_TTS_PLAYING))
                mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessageDelayed(MSG_TTS_PLAYING,TIME_RESET_TTSPLAYING);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayCompleted() {
        Log.i(TAG, "onPlayCompleted");
        try {
            isTTsPlaying = false;
            if (mTtsListener != null) {
                mTtsListener.onPlayCompleted();
            }
            if(mHandler.hasMessages(MSG_TTS_PLAYING))
                mHandler.removeCallbacksAndMessages(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayInterrupted() {
        Log.i(TAG, "onPlayInterrupted");
        try {
            isTTsPlaying = false;
            if (mTtsListener != null) {
                mTtsListener.onPlayInterrupted();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressReturn(int textindex, int textlen) {
    }

    @Override
    public void onTtsInited(boolean state, int errId) {
        Log.i(TAG, "onTtsInited! state:" + state + " ,errId:" + errId);
        try {
            isTTsPlaying = false;
            if (mTtsListener != null) {
                mTtsListener.onTtsInited(state,errId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initTtsService(){
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.chinatsp.ifly", "com.chinatsp.ifly.service.TxzTtsService");
        intent.setComponent(componentName);
        mContext.bindService(intent, this.mTXZConnection, Context.BIND_AUTO_CREATE);
    }


    //死亡接受者
    IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied() called");
            if(mTXZTtsService!=null) {
                try {
                    mTXZTtsService.asBinder().unlinkToDeath(deathRecipient, 0);
                    onPlayInterrupted();
                    isTTsPlaying = false;
                    mTXZTtsService = null;
                    agentTXZListener = null;
                    mConnected = false;
                    mHandler.sendEmptyMessageDelayed(MSG_RESTAR_CONN,TIME_RESTAR_CONN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private ServiceConnection mTXZConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, " mTXZConnection onServiceConnected");
            try {

                mTXZTtsService = TtsServiceAidl.Stub.asInterface(service);
                agentTXZListener = new AgentListener(mClient,MAP_ID);
                mTXZTtsService.registerClient(agentTXZListener,0);
                mConnected = true;
                isTTsPlaying = false;
                try {
                    //连接死亡监听
                    service.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "mTXZConnection onServiceDisconnected");
            try {
                if(mTXZTtsService!=null)
                    mTXZTtsService.releaseClient(agentTXZListener);
                mTXZTtsService = null;
                agentTXZListener = null;
                mConnected = false;
                isTTsPlaying = false;
                onPlayInterrupted();
                mHandler.sendEmptyMessageDelayed(MSG_RESTAR_CONN,TIME_RESTAR_CONN);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d(TAG, "onNullBinding() called with: name = [" + name + "]");
            try {
                if(mTXZTtsService!=null)
                    mTXZTtsService.releaseClient(agentTXZListener);
                mTXZTtsService = null;
                agentTXZListener = null;
                mConnected = false;
                isTTsPlaying = false;
                onPlayInterrupted();
                mHandler.sendEmptyMessageDelayed(MSG_RESTAR_CONN,TIME_RESTAR_CONN);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.d(TAG, "onBindingDied() called with: name = [" + name + "]");
            try {
                if(mTXZTtsService!=null)
                    mTXZTtsService.releaseClient(agentTXZListener);
                mTXZTtsService = null;
                agentTXZListener = null;
                isTTsPlaying = false;
                mConnected = false;
                mHandler.sendEmptyMessageDelayed(MSG_RESTAR_CONN,TIME_RESTAR_CONN);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };



}
