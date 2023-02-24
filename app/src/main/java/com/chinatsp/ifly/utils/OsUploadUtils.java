package com.chinatsp.ifly.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.oushang.uploadservice.adapter.OSUploadServiceAgent;
import com.oushang.uploadservice.adapter.callback.ScreenCaptureCallback;
import com.oushang.uploadservice.adapter.callback.ScreenRecordCallback;

import java.io.File;

/**
 * ClassName: //TODO
 * Function: //截屏录屏功能
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/7/9
 */

public class OsUploadUtils {
    private final static String TAG ="OsUploadUtils";

    private static OsUploadUtils osUploadUtils;
    private Context mContext;
    public OsUploadUtils(Context context) {
        this.mContext =context;
    }
    public static synchronized OsUploadUtils getInstance(Context context){
        if (osUploadUtils==null){
            return new OsUploadUtils(context);
        }
        return osUploadUtils;
    }

    private Boolean isRecording = false;

    /**
     * 截屏或者录屏
     * @param keyType 1 截屏，2 录屏
     * @param time   录屏时间（未指定时间time=0）
     */
    public void captureOrRecordScrenn(int keyType,long time){
        FloatViewManager.getInstance(mContext).hide();
        ThreadPoolUtils.execute(new ScrennHandle(keyType,time));
    }

    class ScrennHandle implements Runnable {
        private int keyType;
        private long time;

        public ScrennHandle(int keyType, long time) {
            this.keyType = keyType;
            this.time = time;
        }

        @Override
        public void run() {
            switch(keyType){
                case 1:
                    startCaptureScrenn();
                    break;
                case 2:
                    if (time ==0){
                        startRecordScreen();
                    }else {
                        startTimeRecordScreen(time);
                    }
                    break;
            }
        }
    }

    //截屏
    private void startCaptureScrenn(){
        Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_mvw_capture, DatastatManager.primitive,R.string.objecte_mvw_factory,DatastatManager.response,TtsConstant.MHXC33CONDITION, R.string.condition_default,null,true);

        Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_capture_screen, R.string.object_start_capture_screen, TtsConstant.FACTORYC3CONDITION, R.string.condition_default);

        OSUploadServiceAgent.getInstance().startCaptureScreen(new ScreenCaptureCallback() {
            @Override
            public void onCaptureSuccess(String s) {
                Log.d(TAG,"onCaptureSuccess:S ="+s);
                String conditionId = TtsConstant.FACTORYC4CONDITION;
                Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC4), exitListener);
                Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_capture_screen, R.string.object_success_capture_screen, TtsConstant.FACTORYC4CONDITION, R.string.condition_default);
                Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_capture_screen, R.string.object_success_capture_screen, TtsConstant.MHXC33CONDITION, R.string.condition_default);
            }

            @Override
            public void onCaptureFail(String s) {
                Log.d(TAG,"onCaptureFail:S ="+s);
                String conditionId = TtsConstant.FACTORYC5CONDITION;
                Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC5), exitListener);
                Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_capture_screen, R.string.object_fail_capture_screen, TtsConstant.FACTORYC5CONDITION, R.string.condition_default);
            }

            @Override
            public void onUSBSuccess(String s) {
                Log.d(TAG,"onUSBSuccess:S ="+s);
                sendFilePasteChanger();
            }

            @Override
            public void onUSBFail(String s) {
                Log.d(TAG,"onUSBFail:S ="+s);
            }

            @Override
            public void onDiskSuccess(String s) {
                Log.d(TAG,"onDiskSuccess:S ="+s);
            }

            @Override
            public void onDiskFail(String s) {
                Log.d(TAG,"onDiskFail:S ="+s);
            }
        });
    }

   //开始录屏 默认最大录屏时间3分钟
   private void startRecordScreen(){

       Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_mvw_capture, DatastatManager.primitive,R.string.objecte_mvw_factory,DatastatManager.response,TtsConstant.MHXC31CONDITION, R.string.condition_default,null,true);


       if (isRecording){
            String conditionId = TtsConstant.FACTORYC6_1CONDITION;
            Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC6_1), exitListener);
            Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_record_screen, R.string.object_start_record_screen, TtsConstant.FACTORYC6_1CONDITION, R.string.condition_inrecording);
        }
        OSUploadServiceAgent.getInstance().startRecordScreen(new ScreenRecordCallback() {
            @Override
            public void onRecordStart() {
                Log.d(TAG,"onRecordStart");
                isRecording = true;
                String conditionId = TtsConstant.FACTORYC6CONDITION;
                Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC6), exitListener);
                Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_record_screen, R.string.object_start_record_screen, TtsConstant.FACTORYC6CONDITION, R.string.condition_no_inrecording);
            }

            @Override
            public void onRecordSuccess(String s) {
                Log.d(TAG,"onRecordSuccess:S ="+s);
                isRecording = false;
                String conditionId = TtsConstant.FACTORYC8CONDITION;
                Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC8), exitListener);
                Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_record_screen, R.string.object_success_record_screen, TtsConstant.FACTORYC8CONDITION, R.string.condition_default);
            }

            @Override
            public void onRecordFail(String s) {
                Log.d(TAG,"onRecordFail:S ="+s);
                isRecording = false;
                String conditionId = TtsConstant.FACTORYC9CONDITION;
                Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC9), exitListener);
                Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_record_screen, R.string.object_fail_record_screen, TtsConstant.FACTORYC9CONDITION, R.string.condition_default,mContext.getString(R.string.factoryC9));
            }

            @Override
            public void onUSBSuccess(String s) {
                Log.d(TAG,"onUSBSuccess:S ="+s);
            }

            @Override
            public void onUSBFail(String s) {
                Log.d(TAG,"onUSBFail:S ="+s);
            }
        });
    }

    //指定时间录屏  最大录屏时间 180 单位秒
    private void startTimeRecordScreen(long time){
        OSUploadServiceAgent.getInstance().startRecordScreen(time, new ScreenRecordCallback() {
            @Override
            public void onRecordStart() {
                Log.d(TAG,"onRecordStart");
            }

            @Override
            public void onRecordSuccess(String s) {
                Log.d(TAG,"onRecordSuccess:S ="+s);
            }

            @Override
            public void onRecordFail(String s) {
                Log.d(TAG,"onRecordFail:S ="+s);
            }

            @Override
            public void onUSBSuccess(String s) {
                Log.d(TAG,"onUSBSuccess:S ="+s);
            }

            @Override
            public void onUSBFail(String s) {
                Log.d(TAG,"onUSBFail:S ="+s);
            }
        });
    }

    //停止录屏
    public void stopRecordScreen(){
        Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_mvw_capture, DatastatManager.primitive,R.string.objecte_mvw_factory,DatastatManager.response,TtsConstant.MHXC32CONDITION, R.string.condition_default,null,true);

        if (!isRecording){
            String conditionId = TtsConstant.FACTORYC7_1CONDITION;
            Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC7_1), exitListener);
            Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_record_screen, R.string.object_stop_record_screen, TtsConstant.FACTORYC7_1CONDITION, R.string.condition_not_start_record, mContext.getString(R.string.factoryC7_1));
            return;
        }
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String conditionId = TtsConstant.FACTORYC7CONDITION;
                Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(R.string.factoryC7), exitListener);
                Utils.eventTrack(mContext, R.string.skill_capture_screnn, R.string.scene_record_screen, R.string.object_stop_record_screen, TtsConstant.FACTORYC7CONDITION, R.string.condition_started_record,mContext.getString(R.string.factoryC7));
                OSUploadServiceAgent.getInstance().stopRecordScreen();
            }
        });
    }

    private TTSController.OnTtsStoppedListener exitListener = new TTSController.OnTtsStoppedListener() {
        @Override
        public void onPlayStopped() {
            if (!FloatViewManager.getInstance(mContext).isHide()) {
                FloatViewManager.getInstance(mContext).hide();
            }
        }
    };

    private void sendFilePasteChanger() {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File("/storage/udisk/screenCapture")));
        mContext.sendBroadcast(scanIntent);
    }

    /**
     * 启动日志app
     * @param isUpload  true 上传  false 保存
     */
    public void startLogApp(boolean isUpload){

        String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        if(!"com.oushang.uploadservice".equals(topPackage)){
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.oushang.uploadservice", "com.oushang.uploadservice.MainActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        }

        if(isUpload){
            Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.FACTORYC10CONDITION, mContext.getString(R.string.factoryC10), exitListener);
        }else{
            Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.FACTORYC12CONDITION, mContext.getString(R.string.factoryC12), exitListener);
        }
    }
}
