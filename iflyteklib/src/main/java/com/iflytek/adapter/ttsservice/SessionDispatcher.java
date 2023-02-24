//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.adapter.ttsservice;

import android.content.Context;
import android.os.Environment;
import android.os.RemoteException;

import com.iflytek.adapter.ttsservice.aidl.ITtsAgentListener;
import com.iflytek.tts.ITtsInitListener;
import com.iflytek.tts.ITtsListener;
import com.iflytek.tts.TtsSession;

import java.io.File;

public class SessionDispatcher implements ITtsInitListener {
    private static SessionDispatcher self;
    private static final String RES_DIR;
    private TtsSession ttsSession;
    private ITtsListener ttsListener;
    private ITtsAgentListener client;
    private int streamType;

    static {
        RES_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "iflytek" + File.separator + "res" + File.separator + "tts";
    }

    public SessionDispatcher() {
    }

    public static SessionDispatcher getInstance() {
        return self == null ? new SessionDispatcher() : self;
    }

    public void clear() {
        ttsSession = null;
        ttsListener = null;
        client = null;
    }

    public synchronized TtsSession getSession(Context context, ITtsAgentListener clientListener, int streamType) {
        this.client = clientListener;
        this.streamType = streamType;
        this.ttsSession = new TtsSession(context, this, RES_DIR);
        this.ttsSession.initService();
        this.ttsListener = new ITtsListener() {
            @Override
            public void onPlayBegin() {
                if (client != null) {
                    try {
                        client.onPlayBegin();
                    } catch (RemoteException var2) {
                        var2.printStackTrace();
                    }

                }
            }

            @Override
            public void onPlayCompleted() {
                if (client != null) {
                    try {
                        client.onPlayCompleted();
                    } catch (RemoteException var2) {
                        var2.printStackTrace();
                    }

                }
            }

            @Override
            public void onPlayInterrupted() {
                if (client != null) {
                    try {
                        client.onPlayInterrupted();
                    } catch (RemoteException var2) {
                        var2.printStackTrace();
                    }

                }
            }

            @Override
            public void onProgressReturn(int textIndex, int textLength) {
                if (client != null) {
                    try {
                        client.onProgressReturn(textIndex, textLength);
                    } catch (RemoteException var4) {
                        var4.printStackTrace();
                    }

                }
            }
        };
        return ttsSession;
    }

    @Override
    public void onTtsInited(boolean state, int errId) {
        if (!state && errId == 20001) {
            ttsSession.initService();
        }

        ttsSession.sessionStart(ttsListener, streamType);

        if (client != null) {
            try {
                client.onTtsInited(state, errId);
            } catch (RemoteException var4) {
                var4.printStackTrace();
            }

        }
    }
}
