//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.mvw;

import android.content.Context;
import android.util.Log;
import com.iflytek.speech.mvw.IMVWListener;
import com.iflytek.speech.mvw.IMVWService;
import com.iflytek.speech.mvw.MVWSolution;

public class MvwSession {
    private static final String TAG = "MvwSession";
    private Context context = null;
    private IMVWService mIMVW = null;
    private IMvwListener mvwListener = null;
    private String resDir = null;
    private boolean singletonflag = false;
    private static MvwSession instance = null;
    public static final int ISS_MVW_SCENE_GLOBAL = 1;
    public static final int ISS_MVW_SCENE_CONFIRM = 2;
    public static final int ISS_MVW_SCENE_SELECT = 4;
    public static final int ISS_MVW_SCENE_ANSWER_CALL = 8;
    public static final int ISS_MVW_SCENE_CUSTOME = 16;
    public static final int ISS_MVW_SCENE_OTHER = 64;
    public static final int ISS_MVW_SCENE_KTV = 128;
    public static final int ISS_MVW_SCENE_IMAGE = 256;
    public static final int ISS_MVW_SCENE_CCTV = 512;
    public static final int ISS_MVW_SCENE_CALL = 1024;

    public static final String ISS_MVW_PARAM_AEC = "mvw_enable_aec";
    public static final String ISS_MVW_PARAM_LSA = "mvw_enable_lsa";
    public static final String ISS_MVW_PARAM_VALUE_ON = "on";
    public static final String ISS_MVW_PARAM_VALUE_OFF = "off";
    public static final String ISS_MVW_PARAM_TMP_LOG_DIR = "TmpLogDir";
    public static final int ISS_MVW_LANG_CHN = 0;
    public static final int ISS_MVW_LANG_ENG = 1;
    private IMVWListener mvwAidlListener = new IMVWListener() {
        public void onMVWWakeup(int nMvwScene, int nMvwId, int nMvwScore, String lParam) {
            if (MvwSession.this.mvwListener == null) {
                Log.d("MvwSession", "mvw listener is null");
            } else {
                MvwSession.this.mvwListener.onVwWakeup(nMvwScene, nMvwId, nMvwScore, lParam);
            }
        }

        @Override
        public void onMVWMsgProc_(long uMsg, long wParam, String lParam) {
            Log.d("MvwSession", "onMVWMsgProc_, uMsg:" + uMsg + " ,wParam:" + wParam + " ,lParam:" + lParam);
        }
    };
    private Object lock = new Object();

    public static MvwSession getInstance(Context context, IMvwListener mvwListener, String resDir) {
        if (context != null && mvwListener != null && resDir != null) {
            if (instance == null) {
                synchronized(MvwSession.class) {
                    if (instance == null) {
                        instance = new MvwSession(context, mvwListener, resDir, true);
                    }
                    instance.initService();
                }
            }

            return instance;
        } else {
            return null;
        }
    }

    public static int setMvwLanguage(int nLangType) {
        int errid = MVWSolution.setMvwLanguage(nLangType);
        Log.d("MvwSession", "setMvwLanguage return " + errid);
        return errid;
    }

    public static boolean isCouldAppendAudioData() {
        boolean flag = MVWSolution.isCouldAppendAudioData();
        Log.d("MvwSession", "isCouldAppendAudioData return " + flag);
        return flag;
    }

    private MvwSession(Context context, IMvwListener mvwListener, String resDir, boolean singletonflag) {
        Object var5 = this.lock;
        synchronized(this.lock) {
            Log.d("MvwSession", "new MvwSession");
            this.context = context;
            this.mvwListener = mvwListener;
            this.resDir = resDir;
            this.singletonflag = singletonflag;
            //this.initService();
        }
    }

    public MvwSession(Context context, IMvwListener mvwListener, String resDir) {
        Object var4 = this.lock;
        synchronized(this.lock) {
            Log.d("MvwSession", "new MvwSession");
            this.context = context;
            this.mvwListener = mvwListener;
            this.resDir = resDir;
            this.singletonflag = false;
            this.initService();
        }
    }

    private void castInitState(boolean s, int e) {
        if (this.mvwListener != null) {
            this.mvwListener.onVwInited(s, e);
        }

    }

    public synchronized void initService() {
        Log.d("MvwSession", "initService");
        if (this.mIMVW != null) {
            Log.d("MvwSession", "Already inited.");
            (new Thread(new MvwSession.OnMvwInitedRunnable(true, 0))).start();
        } else if (this.context != null && this.mvwListener != null) {
            this.mIMVW = new MVWSolution();
            int err = this.mIMVW.initMvw(this.resDir, this.mvwAidlListener);
            Log.d(TAG, "initService() called  err:::"+err);
            if (err == 0) {
                (new Thread(new MvwSession.OnMvwInitedRunnable(true, 0))).start();
            } else {
                (new Thread(new MvwSession.OnMvwInitedRunnable(false, err))).start();
            }

        } else {
            Log.d("MvwSession", "Context or mvwListener is null");
            (new Thread(new MvwSession.OnMvwInitedRunnable(false, 10106))).start();
        }
    }

    public synchronized int start(int nMvwScene) {
        if (this.mIMVW == null) {
            Log.d("MvwSession", "start mIMVW is null");
            return 10000;
        } else {
            int errId = this.mIMVW.startMvw(nMvwScene);
            if (errId == 10000) {
                errId = 0;
            }

            Log.d("MvwSession", "start return " + errId);
            return errId;
        }
    }

    public synchronized int addStartScene(int nMvwScene) {
        if (this.mIMVW == null) {
            Log.d("MvwSession", "addStartScene mIMVW is null");
            return 10000;
        } else {
            int errId = this.mIMVW.addStartMvwScene(nMvwScene);
            if (errId == 10000) {
                errId = 0;
            }

            Log.d("MvwSession", "start return " + errId);
            return errId;
        }
    }

    public synchronized int appendAudioData(byte[] audioBuffer) {
        if (this.mIMVW == null) {
            return 10000;
        } else if (audioBuffer == null) {
            Log.d("MvwSession", "appendAudioData return ISS_SUCCESS.(audioBuffer is null)");
            return 0;
        } else {
            int errid = this.mIMVW.appendAudioData(audioBuffer, audioBuffer.length);
            return errid;
        }
    }

    public synchronized int setThreshold(int nMvwScene, int nMvwId, int threshold) {
        if (this.mIMVW == null) {
            return 10000;
        } else {
            int errid = this.mIMVW.setThreshold(nMvwScene, nMvwId, threshold);
            Log.d("MvwSession", "setThreshold return " + errid);
            return errid;
        }
    }

    public synchronized int setParam(String szParam, String szParamValue) {
        if (this.mIMVW == null) {
            return 10000;
        } else {
            int errid = this.mIMVW.setParam(szParam, szParamValue);
            Log.d("MvwSession", "setParam return " + errid);
            return errid;
        }
    }

    public synchronized int stop() {
        if (this.mIMVW == null) {
            return 10000;
        } else {
            int errid = this.mIMVW.stopMvw();
            Log.d("MvwSession", "stop return " + errid);
            return errid;
        }
    }

    public synchronized int stopScene(int nMvwScene) {
        if (this.mIMVW == null) {
            return 10000;
        } else {
            int errid = this.mIMVW.stopMvwScene(nMvwScene);
            Log.d("MvwSession", "stopScene return " + errid);
            return errid;
        }
    }

    public synchronized int release() {
        if (this.mIMVW == null) {
            return 10000;
        } else {
            int err = this.mIMVW.releaseMvw();
            this.mIMVW = null;
            if (this.singletonflag) {
                instance = null;
            }

            return err;
        }
    }

    public synchronized int setMvwKeyWords(int nMvwScene, String szWords) {
        if (this.mIMVW == null) {
            return 10000;
        } else {
            int errid = this.mIMVW.setMvwKeyWords(nMvwScene, szWords);
            Log.d("MvwSession", "setMvwKeyWords return " + errid);
            return errid;
        }
    }

    public synchronized int setMvwDefaultKeyWords(int nMvwScene) {
        if (this.mIMVW == null) {
            return 10000;
        } else {
            int errid = this.mIMVW.setMvwDefaultKeyWords(nMvwScene);
            Log.d("MvwSession", "setMvwDefaultKeyWords return " + errid);
            return errid;
        }
    }

    private class OnMvwInitedRunnable implements Runnable {
        public boolean mBoolInitState;
        public int mIntErrorCode;

        public OnMvwInitedRunnable(boolean s, int e) {
            this.mBoolInitState = s;
            this.mIntErrorCode = e;
        }

        public void run() {
            synchronized(MvwSession.this.lock) {
                try {
                    Thread.currentThread();
                    Thread.sleep(5L);
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                }

                MvwSession.this.castInitState(this.mBoolInitState, this.mIntErrorCode);
            }
        }
    }
}
