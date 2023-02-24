//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.chinatsp.adapter.ttsservice;

import android.os.RemoteException;

import com.iflytek.adapter.ttsservice.aidl.ITtsAgentListener;

import java.util.Date;

public class AgentListener extends ITtsAgentListener.Stub {
    private long clientId;
    private ITtsClientListener client;

    public AgentListener(ITtsClientListener client) {
        this.client = client;
        Date date = new Date();
        this.clientId = date.getTime();
    }

    public AgentListener(ITtsClientListener client,long id) {
        this.client = client;
        clientId = id;
    }

    public void onPlayBegin() throws RemoteException {
        this.client.onPlayBegin();
    }

    public void onPlayCompleted() throws RemoteException {
        this.client.onPlayCompleted();
    }

    public void onPlayInterrupted() throws RemoteException {
        this.client.onPlayInterrupted();
    }

    public void onProgressReturn(int textIndex, int textLength) throws RemoteException {
        this.client.onProgressReturn(textIndex, textLength);
    }

    public long getClientId() throws RemoteException {
        return this.clientId;
    }

    public void onTtsInited(boolean state, int errId) throws RemoteException {
        this.client.onTtsInited(state, errId);
        if (state) {
            TtsServiceAgent.isConnected = true;
        }

    }
}
