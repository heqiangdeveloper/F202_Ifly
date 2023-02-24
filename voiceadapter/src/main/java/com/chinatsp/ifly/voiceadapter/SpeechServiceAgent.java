package com.chinatsp.ifly.voiceadapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.chinatsp.ifly.ISpeechControlListener;
import com.chinatsp.ifly.ISpeechControlService;

public class SpeechServiceAgent {
    private static final String TAG = "SpeechServiceAgent";
    private boolean isConnected = false;
    public static final int NOT_INIT_ERROR = -1002;
    private static SpeechServiceAgent self;
    private Context mContext;
    private int business;
    private ISpeechClientListener client;
    private ISpeechControlService mSpeechService;
    private ISpeechControlListener agentListener;

    private  ServiceConnect mServiceConnect;

    public void setServiceConnect(ServiceConnect mServiceConnect) {
        this.mServiceConnect = mServiceConnect;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mSpeechService = ISpeechControlService.Stub.asInterface(service);
            isConnected = true;
            if (mServiceConnect!=null){
                mServiceConnect.success();
            }
            try {
                agentListener = new SpeechAgentListener(client);
                mSpeechService.registerSpeechListener(business, agentListener, agentListener.hashCode());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            isConnected = false;
            mSpeechService = null;
            if (mServiceConnect!=null){
                mServiceConnect.fail();
            }
            if (mContext != null && client != null) {
                initService(mContext, business, client);
            }
        }
    };

    public static SpeechServiceAgent getInstance() {
        if (self == null) {
            self = new SpeechServiceAgent();
        }

        return self;
    }

    private SpeechServiceAgent() {
    }

    /**
     * 初始化服务接口
     * @param context
     * @param business Business.MUSIC  Business.RADIO
     * @param client
     * @return
     */
    public boolean initService( Context context, int business, ISpeechClientListener client) {
        this.mContext = context;
        this.business = business;
        this.client = client;
        if (isConnected()) {
            return true;
        } else {
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName("com.chinatsp.ifly", "com.chinatsp.ifly.service.SpeechRemoteService");
            intent.setComponent(componentName);
            return mContext.bindService(intent, this.mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void releaseService() {
        if (isConnected()) {
            try {
                this.mSpeechService.unregisterSpeechListener(agentListener.hashCode());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            this.mContext.unbindService(this.mConnection);
            this.client = null;
            this.mSpeechService = null;
            this.isConnected = false;
        }

    }

    public int registerStksCommand(String stksJson) throws RemoteException {
        if (this.mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            return mSpeechService.registerStksCommand(stksJson);
        }
    }

    public int uploadAppStatus(String statusJson) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            return this.mSpeechService.uploadAppStatus(statusJson);
        }
    }

    public void waitMultiInterface(String service, String operation) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return ;
        } else {
            this.mSpeechService.waitMultiInterface(service,operation);
        }
    }

    public int tts(boolean showText, String text, SpeechTtsStopListener listener) throws RemoteException {
        Log.d(TAG, "tts() called with: showText = [" + showText + "], text = [" + text + "], listener = ["  + "]");
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            this.mSpeechService.tts(showText, text, listener);
            return 0;
        }
    }


    public int getMessageWithTtsSpeak(boolean showText,String conditionId,String defaultTts) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
                this.mSpeechService.getMessageWithTtsSpeak(showText,conditionId,defaultTts);
            return 0;

        }
    }

    public int getMessageWithTtsSpeakListener(boolean showText, String conditionId, String defaultTts, SpeechTtsStopListener listener) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            this.mSpeechService.getMessageWithTtsSpeakListener(showText,conditionId,defaultTts,listener);
            return 0;

        }
    }

    public int getMessageWithoutTtsSpeak(String conditionId, SpeechTtsResultListener listener) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            this.mSpeechService.getMessageWithoutTtsSpeak(conditionId,listener);
            return 0;

        }
    }


    public boolean floatViewIsHide() throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return false;
        }
        return this.mSpeechService.isHide();
    }

    public int hideVoiceAssistant() throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            this.mSpeechService.hideVoiceAssistant();
            return 0;
        }
    }

    public int uploadAppDict(String dictJson) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            return this.mSpeechService.uploadAppDict(dictJson);
        }
    }

    public int onSearchWeChatContactListResult(String resultJsonArray) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            this.mSpeechService.onSearchWeChatContactListResult(resultJsonArray);
            return 0;
        }
    }

    public String getCurrentCityName() throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return "";
        } else {
            return this.mSpeechService.getCurrentCityName();
        }
    }

    public int resetSrTimeOut(String text) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
             this.mSpeechService.resetSrTimeOut(text);
            return 0;
        }
    }

    public int stopTts() throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            this.mSpeechService.stopTts();
            return 0;
        }
    }

    public int releaseAudioFoucs(String pkg) throws RemoteException {
        if (mSpeechService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(mContext, business, client);
            }
            return NOT_INIT_ERROR;
        } else {
            this.mSpeechService.releaseAudioFoucs(pkg);
            return 0;
        }
    }


    public ISpeechClientListener getClient() {
        return this.client;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public interface ServiceConnect {

        void success();
        void fail();
    }
}
