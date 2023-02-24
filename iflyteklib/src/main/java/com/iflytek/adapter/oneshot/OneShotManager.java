package com.iflytek.adapter.oneshot;

import com.iflytek.mvw.MvwSession;
import com.iflytek.sr.SrSession;

public class OneShotManager {

    private final static String TAG = "OneShotManager";

    private static OneShotManager instance = null;
    public CycleQueueOneShot cycleQueueOneShot;
    public static volatile double mEndBytes;
    public static volatile double mInsertBytes;

    public static OneShotManager getInstance() {
        if (instance == null) {
            synchronized (OneShotManager.class) {
                if (instance == null) {
                    instance = new OneShotManager();
                }
            }
        }
        return instance;
    }

    public void init() {
        cycleQueueOneShot = new CycleQueueOneShot();
    }

    public void clear() {
        if(cycleQueueOneShot!=null)
            cycleQueueOneShot.clear();
    }
    /**
     * 销毁资源
     */
    public void destroy() {
        cycleQueueOneShot = null;
        instance = null;
    }

    public void startOneShot(SrSession srSession, MvwSession mvwSession) {
        if(cycleQueueOneShot!=null)
            cycleQueueOneShot.start(srSession, mvwSession);
    }
}
