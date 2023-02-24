//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.chinatsp.adapter.ttsservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Log;

import com.iflytek.adapter.ttsservice.aidl.ITtsAgentListener;
import com.iflytek.adapter.ttsservice.aidl.TtsServiceAidl;

import java.security.cert.TrustAnchor;

public class TtsServiceAgent {
    private static final String TAG = "TtsServiceAgent";
    static boolean isConnected = false;
    public static final int NOT_INIT_ERROR = -1002;
    private static TtsServiceAgent self;
    private boolean useTxz;
    private int mActor;
    private int streamType;
    private Context mContext;
    private ITtsClientListener client;
    private TtsServiceAidl mTtsService;
    private TtsServiceAidl mTXZTtsService;
    private TtsServiceAidl mIFlyTtsService;
    private ITtsAgentListener agentTXZListener;
    private ITtsAgentListener agentIFlyListener;
    private ITtsAgentListener agentListener;

    private boolean mTxzConnected,mIflyConnected;

    private static final int MSG_RESTAR_CONN = 1;
    private static final int TIME_RESTAR_CONN = 1000;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_RESTAR_CONN:
                    TtsServiceAgent.this.initService(TtsServiceAgent.this.client, TtsServiceAgent.this.mContext, TtsServiceAgent.this.mActor, TtsServiceAgent.this.streamType);
                    break;
            }
        }
    };

    private ServiceConnection mIFlyConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "mIFlyConnection onServiceConnected");
            TtsServiceAgent.this.mIFlyTtsService = TtsServiceAidl.Stub.asInterface(service);
            mIflyConnected = true;
            try {
                TtsServiceAgent.this.agentIFlyListener = new AgentListener(TtsServiceAgent.this.client);
                TtsServiceAgent.this.mIFlyTtsService.registerClient(TtsServiceAgent.this.agentIFlyListener, TtsServiceAgent.this.streamType);
                initTtsParam();
            } catch (RemoteException var4) {
                var4.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "mIFlyConnection onServiceDisconnected");
//            TtsServiceAgent.isConnected = false;
            mIflyConnected = false;
            TtsServiceAgent.this.mIFlyTtsService = null;
            if (TtsServiceAgent.this.mContext != null && TtsServiceAgent.this.client != null) {
                TtsServiceAgent.this.initService(TtsServiceAgent.this.client, TtsServiceAgent.this.mContext, TtsServiceAgent.this.mActor, TtsServiceAgent.this.streamType);
            }
        }
    };

    private ServiceConnection mTXZConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, " mTXZConnection onServiceConnected");
            TtsServiceAgent.this.mTXZTtsService = TtsServiceAidl.Stub.asInterface(service);

            try {
                TtsServiceAgent.this.agentTXZListener = new AgentListener(TtsServiceAgent.this.client);
                TtsServiceAgent.this.mTXZTtsService.registerClient(TtsServiceAgent.this.agentTXZListener, TtsServiceAgent.this.streamType);
                mTxzConnected = true;
                initTtsParam();
            } catch (RemoteException var4) {
                var4.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "mTXZConnection onServiceDisconnected");
            TtsServiceAgent.isConnected = false;
            TtsServiceAgent.this.mTXZTtsService = null;
            mHandler.sendEmptyMessageDelayed(MSG_RESTAR_CONN,TIME_RESTAR_CONN);
          /*  if (TtsServiceAgent.this.mContext != null && TtsServiceAgent.this.client != null) {
                TtsServiceAgent.this.initService(TtsServiceAgent.this.client, TtsServiceAgent.this.mContext, TtsServiceAgent.this.mActor, TtsServiceAgent.this.streamType);
            }*/
            mTxzConnected = false;
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d(TAG, "onNullBinding() called with: name = [" + name + "]");
            TtsServiceAgent.isConnected = false;
            TtsServiceAgent.this.mTXZTtsService = null;
            mHandler.sendEmptyMessageDelayed(MSG_RESTAR_CONN,TIME_RESTAR_CONN);
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.d(TAG, "onBindingDied() called with: name = [" + name + "]");
            TtsServiceAgent.isConnected = false;
            TtsServiceAgent.this.mTXZTtsService = null;
            mHandler.sendEmptyMessageDelayed(MSG_RESTAR_CONN,TIME_RESTAR_CONN);
        }
    };

    public static TtsServiceAgent getInstance() {
        if (self == null) {
            self = new TtsServiceAgent();
        }

        return self;
    }

    private TtsServiceAgent() {
    }

    /**
     * 已废弃，useTxz 参数不再使用，为了防止其他app报错，暂时保留，后期去掉
     * @param client
     * @param context
     * @param useTxz
     * @param streamType
     * @return
     * @deprecated
     */
    public boolean initService(ITtsClientListener client, Context context, boolean useTxz, int streamType) {
        Log.d(TAG, "initService() called with: client = ["  + "], context = [" + context + "], useTxz = [" + useTxz + "], streamType = [" + streamType + "]");
        this.client = client;
        this.useTxz = useTxz;
        this.streamType = streamType;
        this.mContext = context;
        if (this.isConnected()) {
            return true;
        } else {
            Intent intent = new Intent();

            ComponentName componentName = new ComponentName("com.chinatsp.ifly", "com.chinatsp.ifly.service.TxzTtsService");
            intent.setComponent(componentName);
            mContext.bindService(intent, this.mTXZConnection, Context.BIND_AUTO_CREATE);


            componentName = new ComponentName("com.chinatsp.ifly", "com.iflytek.adapter.ttsservice.TtsService");
            intent.setComponent(componentName);
//            mContext.bindService(intent, this.mIFlyConnection, Context.BIND_AUTO_CREATE);

            return true;
        }
    }

    public boolean initService(ITtsClientListener client, Context context, int actor, int streamType) {
        Log.d(TAG, "initService() called with: client = ["  + "], context = [" + context + "], actor = [" + actor + "], streamType = [" + streamType + "]");
        this.client = client;
        this.mActor = actor;
        this.streamType = streamType;
        this.mContext = context;
        if (this.isConnected()) {
            return true;
        } else {
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName("com.chinatsp.ifly", "com.chinatsp.ifly.service.TxzTtsService");
            intent.setComponent(componentName);
            mContext.bindService(intent, this.mTXZConnection, Context.BIND_AUTO_CREATE);


            componentName = new ComponentName("com.chinatsp.ifly", "com.iflytek.adapter.ttsservice.TtsService");
            intent.setComponent(componentName);
//            mContext.bindService(intent, this.mIFlyConnection, Context.BIND_AUTO_CREATE);

            return true;
        }
    }


    public void releaseService() {
        if (this.isConnected()) {
            try {
                this.stopSpeak();
//                this.mTtsService.releaseClient(this.agentListener);
                if(mTXZTtsService!=null)
                    mTXZTtsService.releaseClient(agentTXZListener);
                if(mIFlyTtsService!=null)
                  mIFlyTtsService.releaseClient(agentIFlyListener);
            } catch (RemoteException var2) {
                var2.printStackTrace();
            }

            this.mContext.unbindService(this.mTXZConnection);
//            this.mContext.unbindService(this.mIFlyConnection);
            this.client = null;
            this.mTtsService = null;
            isConnected = false;
        }

    }

    public int startSpeak(String text) throws RemoteException {
        Log.d("TtsServiceAgent", "startSpeak() called with: text = [" + text + "]");
        if (this.mTtsService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(this.client, this.mContext, this.mActor, this.streamType);
            }

            return NOT_INIT_ERROR;
        } else {
            if(mTtsService==mIFlyTtsService)
                return this.mIFlyTtsService.startSpeak(this.agentIFlyListener, text);
            else
                return this.mTXZTtsService.startSpeak(this.agentTXZListener, text);
        }
    }

    public int pauseSpeak() throws RemoteException {
        if (this.mTtsService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(this.client, this.mContext, this.mActor, this.streamType);
            }

            return NOT_INIT_ERROR;
        } else {
            if(mTtsService==mIFlyTtsService)
                return this.mTtsService.pauseSpeak(this.agentIFlyListener);
            else
                return this.mTtsService.pauseSpeak(this.agentTXZListener);
        }
    }

    public int resumeSpeak() throws RemoteException {
        if (this.mTtsService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(this.client, this.mContext, this.mActor, this.streamType);
            }

            return NOT_INIT_ERROR;
        } else {
            if(mTtsService==mIFlyTtsService)
                return this.mTtsService.resumeSpeak(this.agentIFlyListener);
            else
                return this.mTtsService.resumeSpeak(this.agentTXZListener);
        }
    }

    public int stopSpeak() throws RemoteException {
        if (this.mTtsService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(this.client, this.mContext, this.mActor, this.streamType);
            }

            return NOT_INIT_ERROR;
        } else {
            if(mTtsService==mIFlyTtsService)
                return this.mTtsService.stopSpeak(this.agentIFlyListener);
            else
                return this.mTtsService.stopSpeak(this.agentTXZListener);
        }
    }

    public int setParam(int id, int value) throws RemoteException {
        Log.d(TAG, "setParam() called with: id = [" + id + "], value = [" + value + "]");
        if (this.mTtsService == null) {
            isConnected = false;
            if (this.client != null && this.mContext != null) {
                this.initService(this.client, this.mContext, this.mActor, this.streamType);
            }

            return NOT_INIT_ERROR;
        } else {
            if(value==-10001){  //说明使用的是同行者
                mTtsService = mTXZTtsService;
                Log.d(TAG, "setParam: mTtsService is mTXZTtsService");
                return 0;
            }else {  //说明使用的是讯飞
                if(mIFlyTtsService==null){
                    this.initService(this.client, this.mContext, this.mActor, this.streamType);
                    Log.e(TAG, "setParam: mIFlyTtsService is null");
                    return NOT_INIT_ERROR;
                }
                mTtsService = mIFlyTtsService;
                agentListener = agentIFlyListener;
                Log.d(TAG, "setParam: mTtsService is mIFlyTtsService");
                return this.mTtsService.setParam(agentIFlyListener, id, value);
            }

        }
    }

    public ITtsClientListener getClient() {
        return this.client;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isConnected() {
//        return mTxzConnected&&mIflyConnected;
        return  mTxzConnected;
    }

    /**
     * 1.选用同行者播报还是讯飞播放
     * 2.讯飞播报设置播报声优
     */
    public void initTtsParam(){
//        try {
            if(!isConnected()){
                Log.d(TAG, "initTtsParam: "+mIflyConnected+".."+mTxzConnected);
                return;
            }
            Log.d(TAG, "initTtsParam: actor::"+mActor);
//            if(mActor ==-10001){  //使用同行者播报
                mTtsService = mTXZTtsService;
                agentListener = agentTXZListener;
                Log.d(TAG, "initTtsParam: mTtsService is  mTXZTtsService");
//            }else {  //使用讯飞播报，设置播报声优
//                mTtsService = mIFlyTtsService;
//                agentListener = agentIFlyListener;
//                setParam(1280,mActor);
//                Log.d(TAG, "initTtsParam: mTtsService is  mIFlyTtsService");
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

    }

}
