//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.chinatsp.ifly.voiceadapter;
import android.os.RemoteException;
import com.chinatsp.ifly.ISpeechTtsStopListener;

public class SpeechTtsStopListener extends ISpeechTtsStopListener.Stub {
    private ISpeechTtsStopListener client;

    public SpeechTtsStopListener(ISpeechTtsStopListener client) {
        this.client = client;
    }

    @Override
    public void onPlayStopped() throws RemoteException {
        if(client != null) {
            client.onPlayStopped();
        }
    }
}
