package com.chinatsp.ifly.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.chinatsp.ifly.utils.LogUtils;
import com.iflytek.adapter.common.PcmRecorder;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.oneshot.OneShotConstant;
import com.iflytek.adapter.oneshot.OneShotManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.seopt.SeoptManager;

public class PCMRecorderService extends Service {

    private static final String TAG = "PCMRecorderService";

    public static PcmRecorder pcmRecorder;
    private MVWAgent mvwAgent;
    private SRAgent srAgent;

    public static void start(Context context) {
        LogUtils.d(TAG, "start");
        Intent intent = new Intent(context, PCMRecorderService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "PCMRecorderService onCreate");
        try {
            if (SeoptConstant.USE_SEOPT) {
                SeoptManager.getInstance().init();
            }

            if (OneShotConstant.USE_ONESHOT) {
                OneShotManager.getInstance().init();
            }
            pcmRecorder = PcmRecorder.getInstnace();
            pcmRecorder.start();
        } catch (Exception e) {
            LogUtils.d(TAG, "create recorder thread error");
        }

        mvwAgent = MVWAgent.getInstance().init(getApplicationContext());
        srAgent = SRAgent.getInstance().init(getApplicationContext());
        mvwAgent.setPcmRecorder(pcmRecorder);
        srAgent.setPcmRecorder(pcmRecorder);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "PCMRecorderService onDestroy");
        mvwAgent.release();
        srAgent.release();
        try {
            if (pcmRecorder != null) {
                pcmRecorder.stopThread();
                pcmRecorder.join();    // 等待线程退出
            }
        } catch (Exception e) {
            LogUtils.d(TAG, "stop recorder thread error");
        }
        if (SeoptConstant.USE_SEOPT) {
            SeoptManager.getInstance().destroy();
        }
    }
}
