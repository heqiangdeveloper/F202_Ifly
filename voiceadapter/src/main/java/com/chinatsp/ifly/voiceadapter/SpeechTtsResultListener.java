package com.chinatsp.ifly.voiceadapter;

import android.os.RemoteException;

import com.chinatsp.ifly.ISpeechTtsResultListener;

public class SpeechTtsResultListener extends ISpeechTtsResultListener.Stub {
    ISpeechTtsResultListener client;

    public SpeechTtsResultListener(ISpeechTtsResultListener client) {
        this.client = client;
    }

    @Override
    public void onTtsCallback(String ttsMessage) throws RemoteException {
        client.onTtsCallback(ttsMessage);
    }
}
