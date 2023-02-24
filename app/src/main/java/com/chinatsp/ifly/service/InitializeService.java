package com.chinatsp.ifly.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.car.media.CarAudioManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.module.me.recommend.view.ManageFloatWindow;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AssetCopyer;
import com.chinatsp.ifly.utils.LoadClass;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.ResDeleter;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.mvw.MvwSession;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.sr.SrSession;

import java.io.File;
import java.io.IOException;

public class InitializeService extends IntentService {
    private static final String TAG = "InitializeService";
    private static final String ACTION_INIT = "initApplication";
    private static final int MSG_INIT_FINISH = 100;
    private static final int MSG_CHECK_INIT_STATUS = 101;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_INIT_FINISH) {
                Context context = getApplicationContext();

                //默认SR场景
                SRAgent.getInstance().setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");

                //日志输出级别
                SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_TRACE_LEVEL, SrSession.ISS_SR_PARAM_TRACE_LEVEL_VALUE_DEBUG);
                //从开启录音后到检测到前端点的超时时间
                SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_RESPONSE_TIMEOUT, "5000");
                boolean spot_talk = SharedPreferencesUtils.getBoolean(InitializeService.this, AppConstant.KEY_SPOT_TALK,AppConstant.VALUE_SPOT_TALK);
                if(spot_talk)
                   SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_PGS, SrSession.ISS_SR_PARAM_VALUE_ON);
                else
                    SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_PGS, SrSession.ISS_SR_PARAM_VALUE_OFF);
                //窄波束开关
                if(SeoptConstant.USE_SEOPT) {
                    SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_SEOPT_MODE, SrSession.ISS_SR_PARAM_VALUE_ON);
                } else {
                    SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_SEOPT_MODE, SrSession.ISS_SR_PARAM_VALUE_OFF);
                }

                //请缓存
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_GLOBAL);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_CONFIRM);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_SELECT);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_ANSWER_CALL);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_OTHER);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_KTV);
//                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_CCTV);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_IMAGE);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_CUSTOME);

                //增加自定义唤醒词
                String defineName1 = SharedPreferencesUtils.getString(InitializeService.this, AppConstant.KEY_CURRENT_NAME_3, "");
                if(defineName1!=null&&!defineName1.isEmpty()){
                    String otherStr = MvwKeywordsUtil.addMvwKeywordJson(InitializeService.this,defineName1);
                    MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, otherStr);
                }


                //默认唤醒词
//                MVWAgent.getInstance().init(context).setMvwKeyWords(MvwSession.ISS_MVW_SCENE_GLOBAL, Utils.getFromAssets(context, "mvw_global.json"));
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CONFIRM, Utils.getFromAssets(context, "mvw_confirm.json"));
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_SELECT, Utils.getFromAssets(context, "mvw_select.json"));
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_ANSWER_CALL, Utils.getFromAssets(context, "mvw_navi.json"));
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_OTHER, Utils.getFromAssets(context, "mvw_other.json"));
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_KTV, Utils.getFromAssets(context, "ktv.json"));
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CCTV, Utils.getFromAssets(context, "cctv.json"));
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_IMAGE, Utils.getFromAssets(context, "image.json"));

                //起来默认是导航页
                MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);

                //设置Mic工作模式为降噪模式
//                if (SeoptConstant.USE_SEOPT){
                    LogUtils.d(TAG, "setMicMode to " + CarAudioManager.MIC_MODE_DENOISE);
                    AppConfig.INSTANCE.setMicWorkMode(CarAudioManager.MIC_MODE_DENOISE);
               /* } else{
                    LogUtils.d(TAG, "setMicMode to " + CarAudioManager.MIC_MODE_WORK_MODEL);
                    AppConfig.INSTANCE.setMicWorkMode(CarAudioManager.MIC_MODE_WORK_MODEL);
                }*/


                //初始化美行SDK
                MXSdkManager.getInstance(context).init();
                //初始化电话jar
                BluePhoneManager.getInstance(context).init();
                //初始化APP管理类
                AppControlManager.getInstance(context).init();
                //异步任务
                ThreadPoolUtils.executeSingle(new AsyncTask());

                //导入同行者多音字json
                LoadClass.getInstance().loadPolyphone();

//                ManageFloatWindow.getInstance(InitializeService.this).showFloatView();

            } else if (msg.what == MSG_CHECK_INIT_STATUS) {
                if(!SRAgent.getInstance().init_state || !MVWAgent.getInstance().init_state) {
                    sendEmptyMessageDelayed(MSG_CHECK_INIT_STATUS, 500);
                } else {
                    sendEmptyMessage(MSG_INIT_FINISH);
                }
            }
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, InitializeService.class);
        intent.setAction(ACTION_INIT);
        context.startService(intent);
    }

    public InitializeService() {
        super("InitializeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT.equals(action)) {
                initApplication();
            }
        }
    }

    private void initApplication() {
        AppConfig.INSTANCE.initConfig(getApplicationContext());
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_INIT_STATUS, 500);
    }

    private void uploadHuToken() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
                    AppConfig.INSTANCE.uploadHuTokenToIfly(AppConfig.INSTANCE.token);
                } else {
                    LogUtils.d(TAG, "uploadHuToken fail, token is null");
                }
            }
        }, 5 * 1000);
    }
    private class AsyncTask implements Runnable {
        @Override
        public void run() {
            //上传token
            uploadHuToken();

            //拷贝语音命令集
            try {
                new AssetCopyer(InitializeService.this).copy();
                new ResDeleter((InitializeService.this)).deleteRes();//删除 202 资源包
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
