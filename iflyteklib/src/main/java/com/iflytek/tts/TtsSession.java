//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.tts;

import android.content.Context;
import android.util.Log;
import com.iflytek.speech.NativeHandle;
import com.iflytek.speech.libisstts;
import com.iflytek.speech.tts.ITTSListener;
import com.iflytek.speech.tts.ITTSService;
import com.iflytek.speech.tts.TtsPlayerInst;
import com.iflytek.speech.tts.TtsSolution;

public class TtsSession {
    private static int cnt = 0;
    private String tag;
    private static String mResDir = null;
    private ITtsListener mTtsListener;
    private TtsPlayerInst mItts;
    private static ITtsInitListener mTtsInitListener = null;
    private Context mContext;
    private static ITTSService mTTSService;
    private ITTSListener mTtsAidlListener;
    private Object lock;
    private final NativeHandle mNativeHandle;
    private com.iflytek.speech.ITtsListener mVoidTtsListener;

    public TtsSession(Context context, ITtsInitListener iTtsInitListener, String resDir) {
        this.tag = "TtsSession_" + cnt;
        this.mTtsListener = null;
        this.mItts = null;
        this.mContext = null;
        this.mTtsAidlListener = new ITTSListener() {
            public void onTTSPlayBegin() {
                if (TtsSession.this.mTtsListener != null) {
                    TtsSession.this.mTtsListener.onPlayBegin();
                }

            }

            public void onTTSPlayCompleted() {
                if (TtsSession.this.mTtsListener != null) {
                    TtsSession.this.mTtsListener.onPlayCompleted();
                }

            }

            public void onTTSPlayInterrupted() {
                if (TtsSession.this.mTtsListener != null) {
                    TtsSession.this.mTtsListener.onPlayInterrupted();
                }

            }

            public void onTTSProgressReturn(int nTextIndex, int nTextLen) {
                if (TtsSession.this.mTtsListener != null) {
                    TtsSession.this.mTtsListener.onProgressReturn(nTextIndex, nTextLen);
                }

            }
        };
        this.lock = new Object();
        this.mNativeHandle = new NativeHandle();
        this.mVoidTtsListener = new com.iflytek.speech.ITtsListener() {
            public void onDataReady() {
            }

            public void onProgress(int nTextIndex, int nTextLen) {
            }
        };
        synchronized(this.lock) {
            Log.d(this.tag, "new TtsSession()");
            ++cnt;
            this.mContext = context;
            mTtsInitListener = iTtsInitListener;
            mResDir = resDir;
            //this.initService();
        }
    }

    private void castInitState(boolean s, int e) {
        if (mTtsInitListener != null) {
            Log.d(this.tag, "castInitState(" + s + ", " + e + ")");
            mTtsInitListener.onTtsInited(s, e);
        }

    }

    public synchronized void initService() {
        Log.d(this.tag, "initService");
        if (this.mContext == null) {
            Log.d(this.tag, "initService: mContext == null.");
            (new Thread(new TtsSession.OnTtsInitedRunnable(false, 10106))).start();
        } else if (null == mResDir) {
            Log.d(this.tag, "initService: mResDir == null.");
            (new Thread(new TtsSession.OnTtsInitedRunnable(false, 10106))).start();
        } else {
            mTTSService = TtsSolution.getInstance();
            this.mItts = mTTSService.createTtsPlayerInst(mResDir);
            int initResRet = this.mItts.sessionInitState();
            Log.d(tag, "initService:initResRet:: "+initResRet);
            if (0 != initResRet) {
                Log.d(this.tag, "initRes is failed. ret = " + initResRet);
                (new Thread(new TtsSession.OnTtsInitedRunnable(false, initResRet))).start();
            } else {
                (new Thread(new TtsSession.OnTtsInitedRunnable(true, 0))).start();
            }
        }
    }

    public int sessionStart(ITtsListener ttsListener, int audioType) {
        Object var3 = this.lock;
        synchronized(this.lock) {
            if (mTTSService == null) {
                mTTSService = TtsSolution.getInstance();
            }

            this.mTtsListener = ttsListener;
            if (this.mItts == null) {
                this.mItts = mTTSService.createTtsPlayerInst(mResDir);
            }

            return this.mItts.sessionBegin(audioType);
        }
    }

    public int setParam(int id, int value) {
        if (mTTSService == null) {
            mTTSService = TtsSolution.getInstance();
        }

        if (this.mItts == null) {
            this.mItts = mTTSService.createTtsPlayerInst(mResDir);
        }

        return this.mItts.setParam(id, value);
    }

    public int setParamEx(int id, String strValue) {
        if (mTTSService == null) {
            mTTSService = TtsSolution.getInstance();
        }

        if (this.mItts == null) {
            this.mItts = mTTSService.createTtsPlayerInst(mResDir);
        }

        return this.mItts.setParamEx(id, strValue);
    }

    public int startSpeak(String text) {
        if (mTTSService == null) {
            mTTSService = TtsSolution.getInstance();
        }

        if (text != null && text.length() != 0) {
            if (this.mItts == null) {
                this.mItts = mTTSService.createTtsPlayerInst(mResDir);
            }

            return this.mItts.startSpeak(text, this.mTtsAidlListener);
        } else {
            return 10106;
        }
    }

    public int pauseSpeak() {
        if (mTTSService == null) {
            mTTSService = TtsSolution.getInstance();
        }

        if (this.mItts == null) {
            this.mItts = mTTSService.createTtsPlayerInst(mResDir);
        }

        return this.mItts.pauseSpeak();
    }

    public int resumeSpeak() {
        if (mTTSService == null) {
            mTTSService = TtsSolution.getInstance();
        }

        if (this.mItts == null) {
            this.mItts = mTTSService.createTtsPlayerInst(mResDir);
        }

        return this.mItts.resumeSpeak();
    }

    public int stopSpeak() {
        if (mTTSService == null) {
            mTTSService = TtsSolution.getInstance();
        }

        if (this.mItts == null) {
            this.mItts = mTTSService.createTtsPlayerInst(mResDir);
        }

        return this.mItts.stopSpeak();
    }

    public int sessionStop() {
        Object var1 = this.lock;
        synchronized(this.lock) {
            if (mTTSService == null) {
                mTTSService = TtsSolution.getInstance();
            }

            if (this.mItts == null) {
                this.mItts = mTTSService.createTtsPlayerInst(mResDir);
            }

            return this.mItts.sessionStop();
        }
    }

    public int startSynthToGetPcm(String text) {
        if (this.mNativeHandle.native_point == 0L) {
            libisstts.initRes(mResDir, 0);
            if (this.mNativeHandle.err_ret != 0) {
                return this.mNativeHandle.err_ret;
            }

            libisstts.create(this.mNativeHandle, this.mVoidTtsListener);
            if (this.mNativeHandle.err_ret != 0) {
                return this.mNativeHandle.err_ret;
            }
        }

        libisstts.start(this.mNativeHandle, text);
        return 0;
    }

    public int setParamSynthToGetPcm(int id, int value) {
        if (this.mNativeHandle.native_point == 0L) {
            return 10000;
        } else {
            libisstts.setParam(this.mNativeHandle, id, value);
            return this.mNativeHandle.err_ret;
        }
    }

    public int stopSynthToGetPcm() {
        if (this.mNativeHandle.native_point == 0L) {
            return 10000;
        } else {
            libisstts.stop(this.mNativeHandle);
            return this.mNativeHandle.err_ret;
        }
    }

    public int destroySynthToGetPcm() {
        if (this.mNativeHandle.native_point == 0L) {
            return 10000;
        } else {
            libisstts.destroy(this.mNativeHandle);
            this.mNativeHandle.native_point = 0L;
            int tmp = this.mNativeHandle.err_ret;
            this.mNativeHandle.err_ret = 0;
            return tmp;
        }
    }

    public int getAudioData(byte[] audioBuffer, int nBytes, int[] outBytes) {
        if (this.mNativeHandle.native_point == 0L) {
            return 10000;
        } else {
            libisstts.getAudioData(this.mNativeHandle, audioBuffer, nBytes, outBytes);
            return this.mNativeHandle.err_ret;
        }
    }

    private class OnTtsInitedRunnable implements Runnable {
        public boolean mBoolInitState;
        public int mIntErrorCode;

        public OnTtsInitedRunnable(boolean s, int e) {
            this.mBoolInitState = s;
            this.mIntErrorCode = e;
        }

        public void run() {
            synchronized(TtsSession.this.lock) {
                try {
                    Thread.currentThread();
                    Thread.sleep(5L);
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                }

                TtsSession.this.castInitState(this.mBoolInitState, this.mIntErrorCode);
            }
        }
    }
}
