//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.adapter.ttsservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.iflytek.adapter.ttsservice.aidl.ITtsAgentListener;
import com.iflytek.adapter.ttsservice.aidl.TtsServiceAidl;
import com.iflytek.tts.TtsSession;


public class TtsService extends Service {
    public static final String TAG = "com.iflytek.ttsservice";
    public static final int MAX_CLIENT_NUMBERS = 10;
    public static final int NO_SESSION_ERROR = -1001;
    private ClientMap<Long, TtsSession> clientMap = new ClientMap(MAX_CLIENT_NUMBERS);
    private TtsServiceAidl.Stub ttsService = new TtsServiceAidl.Stub() {
        @Override
        public boolean registerClient(ITtsAgentListener client, int streamType) throws RemoteException {
            if (client == null) {
                Log.d(TAG, "client is null");
            } else {
                Log.d(TAG, "client is not null");
            }

            TtsSession session = SessionDispatcher.getInstance().getSession(TtsService.this.getApplicationContext(), client, streamType);
            TtsService.this.clientMap.putClient(client.getClientId(), session);
            return true;
        }

        @Override
        public boolean releaseClient(ITtsAgentListener client) throws RemoteException {
            TtsSession session = TtsService.this.clientMap.get(client.getClientId());
            session.sessionStop();
            TtsService.this.clientMap.removeClient(client.getClientId());
            return true;
        }

        @Override
        public int setParam(ITtsAgentListener client, int id, int value) throws RemoteException {
            TtsSession session = TtsService.this.clientMap.get(client.getClientId());
            return session != null ? session.setParam(id, value) : NO_SESSION_ERROR;
        }

        @Override
        public int startSpeak(ITtsAgentListener client, String text) throws RemoteException {
            TtsSession session = TtsService.this.clientMap.get(client.getClientId());
            return session != null ? session.startSpeak(text) : NO_SESSION_ERROR;
        }

        @Override
        public int pauseSpeak(ITtsAgentListener client) throws RemoteException {
            TtsSession session = TtsService.this.clientMap.get(client.getClientId());
            return session != null ? session.pauseSpeak() : NO_SESSION_ERROR;
        }

        @Override
        public int resumeSpeak(ITtsAgentListener client) throws RemoteException {
            TtsSession session = TtsService.this.clientMap.get(client.getClientId());
            return session != null ? session.resumeSpeak() : NO_SESSION_ERROR;
        }

        @Override
        public int stopSpeak(ITtsAgentListener client) throws RemoteException {
            TtsSession session = TtsService.this.clientMap.get(client.getClientId());
            return session != null ? session.stopSpeak() : NO_SESSION_ERROR;
        }
    };

    public TtsService() {
    }

    public IBinder onBind(Intent intent) {
        return this.ttsService;
    }
}
