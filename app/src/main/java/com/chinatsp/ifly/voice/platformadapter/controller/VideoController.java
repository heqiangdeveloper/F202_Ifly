package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.tv.TvView;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.callback.UsbIntentListener;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.module.seachlist.SearchListFragment;
import com.chinatsp.ifly.receiver.UsbReceiver;
import com.chinatsp.ifly.service.DetectionService;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.video.VideoModel;
import com.chinatsp.ifly.video.VideoProvider;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IAppInterface;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.google.gson.JsonObject;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;

import java.util.List;
import java.util.Random;

import static com.chinatsp.ifly.api.constantApi.TtsConstant.VIDEOC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.VIDEOC2CONDITION;

/**
 * Created by ytkj on 2019/9/2.
 */

public class VideoController extends BaseController implements IAppInterface, UsbIntentListener {

    private static final String EXTRA_VIDEO_PLAY_STATUS = "com.chinatsp.music.video_play_status";//播放状态
    private static final String TAG = "VideoController";
    private static final String ACTION_IFLY_CCTV  ="action.ifly.control.cctv";
    private static final String EXTRA_IFLY  ="action.ifly.control.cctv";
    private static VideoController instance;
    private static final String VIDEO = "视频";
    private Context mContext;
    private VideoStatuReceive mVideoStatuReceive;
    private ISpeechControlService mSpeechControlService;
    private int current;
    private boolean mVideoFg = false;


    public static VideoController getInstance(Context context){
        if(instance==null){
            synchronized (VideoController.class){
                if(instance==null)
                    instance = new VideoController(context);
            }
        }
        return instance;
    }

    private VideoController(Context context){
        Log.e(TAG, "VideoController: " );
        mContext = context;

        mVideoStatuReceive = new VideoStatuReceive();
        IntentFilter filter = new IntentFilter(EXTRA_VIDEO_PLAY_STATUS);
        context.registerReceiver(mVideoStatuReceive, filter);
//        UsbReceiver.registerUsbListen(this);
//        VideoProvider.getInstance().setContext(context);
    }

    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }
    @Override
    public void srAction(IntentEntity intentEntity) {

        String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        String topActivity = ActivityManagerUtils.getInstance(mContext).getTopActivity();
        //说明在本地视频界面
        if(AppConstant.PACKAGE_NAME_MUSIC.equals(topPackage)&& DetectionService.ACTIVITY_VIDEO.equals(topActivity)){
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null||intentEntity.semantic.slots.insType==null){
                doExceptonAction(mContext);
                return;
            }

            if(PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation)){
                if("PLAY".equals(intentEntity.semantic.slots.insType)){
                    controlVideoByBroadcast(R.string.scene_start);
                }else if("PAUSE".equals(intentEntity.semantic.slots.insType)){
                    controlVideoByBroadcast(R.string.scene_stop);
                } else
                    doExceptonAction(mContext);
            }else
                doExceptonAction(mContext);
            return;
        }

        //在央视音影界面
        try {
            if(!AppConstant.PACKAGE_NAME_VCAR.equals(topPackage)){
                ComponentName componentName = new ComponentName("tv.newtv.vcar", "tv.newtv.vcar.view.splash.OSplashActivity");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.putExtra(EXTRA_IFLY, DatastatManager.response);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                mContext.startActivity(intent);
            }else {
                mSpeechControlService.dispatchSRAction(Business.CCTV,intentEntity.convert2NlpVoiceModel());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "srAction() called with: intentEntity = [" +  DatastatManager.response+ "]");
//        doExceptonAction(mContext);


/*        if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation)){
            if ("OPEN".equals(intentEntity.semantic.slots.insType)) {
                Utils.getMessageWithTtsSpeakOnly(mContext, VIDEOC1CONDITION, mContext.getString(R.string.systemC24), exitListener);
                Utils.eventTrack(mContext, R.string.skill_vedio, R.string.scene_vedio, R.string.object_vedio1, VIDEOC1CONDITION, R.string.condition_default);
                startApp("视频");
            }else if ("CLOSE".equals(intentEntity.semantic.slots.insType)) {
                String conditionId = TtsConstant.VIDEOC2CONDITION;
                String defaultText = mContext.getString(R.string.systemC25);
                Utils.eventTrack(mContext, R.string.skill_vedio, R.string.scene_vedio, R.string.object_vedio2, VIDEOC2CONDITION, R.string.condition_default);
                Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultText, exitListener);
                exitApp("视频");
            } else
                doExceptonAction(mContext);
        }else if (PlatformConstant.Operation.QUERY.equals(intentEntity.operation)){
            String videoName = intentEntity.semantic.slots.name;
            VideoModel model = VideoProvider.getInstance().queryVideoByName(videoName);

            if(model==null){
                String defaultTts = mContext.getString(R.string.videoC4);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.VIDEOC4CONDITION, defaultTts, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        tts = Utils.replaceTts(tts, "#NAME#", videoName);
                        startTTSOnly(tts, exitListener);
                        Utils.eventTrack(mContext, R.string.skill_vedio, R.string.scene_vedio, R.string.object_vedio1, TtsConstant.VIDEOC4CONDITION, R.string.videoC4);
                    }
                });

            }else {
                String defaultTts = mContext.getString(R.string.videoC3);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.VIDEOC3CONDITION, defaultTts, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        String name = model.getName();
                        int index = name.lastIndexOf(".");
                        name = name.substring(0,index);
                        tts = Utils.replaceTts(tts, "#NAME#", name);
                        startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                Utils.exitVoiceAssistant();
                                playVideo(model);
                            }
                        });
                        Utils.eventTrack(mContext, R.string.skill_vedio, R.string.scene_vedio, R.string.object_vedio1, TtsConstant.VIDEOC3CONDITION, R.string.videoC3);
                    }
                });
            }
        }else
            doExceptonAction(mContext);*/




//
        

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    public void uploadAppStatus(String statusJson){
       if(statusJson.contains("fg")&&!mVideoFg){//在前台
           mVideoFg = true;
           current = TspSceneAdapter.getTspScene(mContext);
           MVWAgent.getInstance().stopMVWSession();
           MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_VIDEO);
       }else if(statusJson.contains("bg")&&mVideoFg){ //在后台
           mVideoFg = false;
           restartScrene();
       }
    }

    private TTSController.OnTtsStoppedListener exitListener = new TTSController.OnTtsStoppedListener() {
        @Override
        public void onPlayStopped() {
            if (!FloatViewManager.getInstance(mContext).isHide()) {
                FloatViewManager.getInstance(mContext).hide();
            }
        }
    };

    @Override
    public void onUsbMounted(Intent intent) {

    }

    @Override
    public void onUsbUnMounted(Intent intent) {
        VideoProvider.getInstance().onUsbUnMounted();
    }

    @Override
    public void onScanStart(Intent intent) {
        Log.d(TAG, "onScanStart() called with: intent = [" + intent + "]");
        VideoProvider.getInstance().onScanStart(intent);
    }

    @Override
    public void onScanFinish(Intent intent) {
        Log.d(TAG, "onScanFinish() called with: intent = [" + intent + "]");
        VideoProvider.getInstance().onScanFinish(intent, -1);
    }

    private void playVideo(VideoModel model){
        Intent it = new Intent("com.coagent.intent.action.video");
        if(model.getPath().contains("udisk"))
            it.putExtra("dataType", 2);// 1是本地 2是usb1 3是usb2
        else
            it.putExtra("dataType", 1);// 1是本地 2是usb1 3是usb2
        it.putExtra("extra_vedio_from", "ifly");// 从哪里打开
        it.putExtra("id", model.getId()+"");
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(it);
        } catch (ActivityNotFoundException exception) {
        }
    }

    private class VideoStatuReceive extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
            boolean playStatus = intent.getBooleanExtra("isPlaying",false);
            boolean fg = intent.getBooleanExtra("isFg",false);
            updateVideoData(playStatus,fg);
            Log.d(TAG, "onReceive() called with: playStatus = [" + playStatus + "], fg = [" + fg + "]");
        }
    }

    private void updateVideoData(boolean playStatus,boolean fg){
        JsonObject dataInfo = new JsonObject();
        JsonObject data = new JsonObject();
        data.add("dataInfo", dataInfo);

        JsonObject serviceScene = new JsonObject();
        serviceScene.addProperty("activeStatus", fg ? "fg" : "bg");
        serviceScene.add("data", data);
        serviceScene.addProperty("sceneStatus", playStatus ? "playing" : "paused");

        String service_scene =  "localvideo::default";
        JsonObject UserData = new JsonObject();
        UserData.add(service_scene, serviceScene);

        JsonObject root = new JsonObject();
        root.add("UserData", UserData);

        LogUtils.d(TAG, "root:\n" + root);

        int errId = SRAgent.getInstance().uploadData(root.toString());
        Log.d(TAG, "updateVideoData() called with: playStatus = [" + playStatus + "], errId = [" + errId + "]");
    }

    private void restartScrene(){
        TspSceneManager.getInstance().resetScrene(mContext,current);
        current = -1;
    }

    private NlpVoiceModel openCCTVByVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "app";
        nlpVoiceModel.operation = "LAUNCH";
        nlpVoiceModel.semantic ="{\"slots\":{\"name\":\"央视影音\"}}";
        return nlpVoiceModel;
    }

    private void controlVideoByBroadcast(int object) {
        Log.d(TAG, "controlVideoByBroadcast() called with: object = [" + object + "]");
        Intent intent = new Intent();
        intent.putExtra("action","ifly");
//        intent.setPackage("com.chinatsp.music");
        switch (object){
            case R.string.scene_start:
                intent.setAction("video_action_for_play");
                break;
            case R.string.scene_stop:
                intent.setAction("video_action_for_pause");
                break;
            case R.string.scene_pre:
                intent.setAction("video_action_for_pre");
                Utils.eventTrack(mContext, R.string.skill_vedio, R.string.screne_video_switch, R.string.object_switch_pev, TtsConstant.VIDEOC5CONDITION, R.string.condition_default,mContext.getString(R.string.videoC5));
                break;
            case R.string.scene_next:
                intent.setAction("video_action_for_next");
                Utils.eventTrack(mContext, R.string.skill_vedio, R.string.screne_video_switch, R.string.object_switch_next, TtsConstant.VIDEOC7CONDITION, R.string.condition_default,mContext.getString(R.string.videoC7));
                break;
        }
        mContext.sendBroadcast(intent);
    }
}
