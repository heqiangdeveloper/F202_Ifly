package com.iflytek.seopt;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.speech.NativeHandle;
import com.iflytek.speech.libissseopt;
import com.iflytek.sr.SrSession;

public class SeoptManager {

    private final static String TAG = "xyj_SeoptManager";

    private static SeoptManager instance = null;
    public NativeHandle phISSSeopt = null;
    public BlockingQueueSr blockingQueueSr;

    /**
     * 全局窄波束降噪方向
     */
    public String seopt_direction = "";

    public static SeoptManager getInstance() {
        if (instance == null) {
            synchronized (SeoptManager.class) {
                if (instance == null) {
                    instance = new SeoptManager();
                }
            }
        }

        return instance;
    }

    public void init() {
        blockingQueueSr = new BlockingQueueSr();
        initSeopt();
    }

    public void setSrSession(SrSession srSession) {
        blockingQueueSr.setSrSession(srSession);
    }

    public void startSrRecord() {
        blockingQueueSr.startSrRecord();
    }

    public void stopSrRecord() {
        blockingQueueSr.stopSrRecord();
    }

    private void initSeopt() {
        if (phISSSeopt == null) {
            phISSSeopt = new NativeHandle();
            int err = 0;
            String mSeoptResPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/iflytek/res/seopt/";
            err = libissseopt.create(phISSSeopt, mSeoptResPath);
            err = libissseopt.setParam(phISSSeopt, libissseopt.ISS_SEOPT_PARAM_WORK_MODE, libissseopt.ISS_SEOPT_PARAM_WORK_MODE_VALUE_MAB_VAD_ONLY);
        }
    }


    /**
     * 设置窄波束方向
     *
     * @param direction
     */
    public void setDirection(String direction) {
        Log.d(TAG, "****************setDirection() called with: direction = [" + direction + "]********");
        int err = 0;
//        if (TextUtils.isEmpty(seopt_direction)) {
            seopt_direction = direction;
            err = libissseopt.setParam(phISSSeopt, libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX, seopt_direction);
            Log.d(TAG, "setDirection() called with: direction = [" + direction + "]");
//        }

    }

    /**
     * 退出本次会话
     */
    public void exitSession() {
        Log.e(TAG, "********** exitSession() called************");
        seopt_direction = "";
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        libissseopt.destroy(phISSSeopt);
        phISSSeopt = null;
        blockingQueueSr = null;
        instance = null;
    }
}
