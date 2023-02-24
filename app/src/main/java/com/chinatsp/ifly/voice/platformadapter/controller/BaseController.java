package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.utils.HandleUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.listener.IExtendCallback;
import com.iflytek.adapter.common.TspSceneAdapter;

import org.json.JSONObject;

public abstract class BaseController {

    private static final String TAG = "BaseController";
    /**
     * 启动应用
     * @param name 支持应用title或包名
     */
    public void startApp(String name) {
        AppControlManager.getInstance(BaseApplication.getInstance()).startApp(name);
    }
    /**
     * 关闭应用
     * @param name 支持应用title或包名
     */
    public void exitApp( String name) {
        AppControlManager.getInstance(BaseApplication.getInstance()).closeApp(name);
    }

    public void gotoHome() {

        TAExtendManager.getInstance().getApi().setNaviScreen(new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d("BaseController", "success");
                HandleUtils.getInstance().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        BaseApplication.getInstance().startActivity(intent);
                    }
                },200);

            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d("BaseController", "onFail:" + extendErrorModel.getErrorCode());
                HandleUtils.getInstance().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        BaseApplication.getInstance().startActivity(intent);
                    }
                },200);
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });





    }

    public void startTTS(String text, TTSController.OnTtsStoppedListener listener) {
        Utils.startTTS(text, listener);
    }

    public void startTTS(String text) {
        startTTS(text, null);
    }

    public void startTTSOnly(String text, TTSController.OnTtsStoppedListener listener) {
        Utils.startTTSOnly(text, listener);
    }

    public void startTTSOnly(String text) {
        startTTSOnly(text, null);
    }

    public void finish(){

    }

    /**
     * 形成闭环操作
     */
    protected void doExceptonAction(Context context){
       LogUtils.d(TAG, "doExceptonAction() called");
        String defaultTts =  String.format(context.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(context));
        Utils.getMessageWithoutTtsSpeak(context, TtsConstant.MAINC14CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                //tts为空,则用默认tts代替,避免tts不播报
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }

                String username = Settings.System.getString(context.getContentResolver(),"aware");
                if (TextUtils.isEmpty(username)) {
                    // 有时候为空  bug Id  ID1017837
                    username = "我";
                }
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);

                Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_exception, R.string.object_main_exception_5, TtsConstant.MAINC14CONDITION, R.string.condition_mainC13,ttsText);

                //TTS播报
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        //FloatViewManager.getInstance(context).hide();
                        Utils.exitVoiceAssistant();
                    }
                });

            }
        });

    }


    /**
     * 将语义转化为索引
     * @return
     */
    protected int slotToIndex(Semantic.SlotsBean.PageRankBean pageRank){
        if(pageRank==null)
            return 0;
        if("MAX".equals(pageRank.ref))  //最后一页
            return 6;
        else {
            if("--".equals(pageRank.direct)){  //上一页
                return 7;
            }else if("++".equals(pageRank.direct)){  //下一页
                return 8;
            }else
                return Integer.parseInt(pageRank.offset)+8;
        }
    }

    /**
     * 将语义转化为索引
     * @return
     */
    protected MvwLParamEntity selectWordToIndex(String word){
        MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
        mvwLParamEntity.nMvwScene = TspSceneAdapter.TSP_SCENE_SELECT;
        if(word.contains("一个")){  //第一个 防止只识别到 一个
            mvwLParamEntity.nMvwId = 0;
        }else  if(word.contains("二个")){ //二个
            mvwLParamEntity.nMvwId = 1;
        }else  if(word.contains("三个")){ //三个
            mvwLParamEntity.nMvwId = 2;
        }else  if(word.contains("四个")){ //四个
            mvwLParamEntity.nMvwId = 3;
        }else  if(word.contains("五个")){  //五个
            mvwLParamEntity.nMvwId = 4;
        }else  if(word.contains("六个")){  //六个
            mvwLParamEntity.nMvwId = 5;
        }else  if(word.contains("最后一页")){
            mvwLParamEntity.nMvwId = 6;
        }else  if(word.contains("上一页")){
            mvwLParamEntity.nMvwId = 7;
        }else  if(word.contains("下一页")){
            mvwLParamEntity.nMvwId = 8;
        }else  if(word.contains("第一页")){
            mvwLParamEntity.nMvwId = 9;
        }else  if(word.contains("二页")){ //第二页
            mvwLParamEntity.nMvwId = 10;
        }else  if(word.contains("三页")){ //第三页
            mvwLParamEntity.nMvwId = 11;
        }else  if(word.contains("四页")){ //第四页
            mvwLParamEntity.nMvwId = 12;
        }else  if(word.contains("五页")){ //第五页
            mvwLParamEntity.nMvwId = 13;
        }else  if(word.contains("六页")){ //第六页
            mvwLParamEntity.nMvwId = 14;
        }else  if(word.contains("七个")){ //第六页
            mvwLParamEntity.nMvwId = 15;
        }else  if(word.contains("八个")){ //第六页
            mvwLParamEntity.nMvwId = 16;
        }else  if(word.contains("九个")){ //第六页
            mvwLParamEntity.nMvwId = 17;
        }else  if(word.contains("七页")){ //第六页
            mvwLParamEntity.nMvwId = 18;
        }else  if(word.contains("八页")){ //第六页
            mvwLParamEntity.nMvwId = 19;
        }else  if(word.contains("九页")){ //第六页
            mvwLParamEntity.nMvwId = 20;
        }else  if(word.contains("一首")){ //第六页
            mvwLParamEntity.nMvwId = 21;
        }else  if(word.contains("二首")){ //第六页
            mvwLParamEntity.nMvwId = 22;
        }else  if(word.contains("三首")){ //第六页
            mvwLParamEntity.nMvwId = 23;
        }else  if(word.contains("四首")){ //第六页
            mvwLParamEntity.nMvwId = 24;
        }else  if(word.contains("五首")){ //第六页
            mvwLParamEntity.nMvwId = 25;
        }else  if(word.contains("六首")){ //第六页
            mvwLParamEntity.nMvwId = 26;
        }else  if(word.contains("七首")){ //第六页
            mvwLParamEntity.nMvwId = 27;
        }else  if(word.contains("八首")){ //第六页
            mvwLParamEntity.nMvwId = 28;
        }else  if(word.contains("九首")){ //第六页
            mvwLParamEntity.nMvwId = 29;
        }else  if(word.contains("一条")){ //第六页
            mvwLParamEntity.nMvwId = 30;
        }else  if(word.contains("二条")){ //第六页
            mvwLParamEntity.nMvwId = 31;
        }else  if(word.contains("三条")){ //第六页
            mvwLParamEntity.nMvwId = 32;
        }else  if(word.contains("四条")){ //第六页
            mvwLParamEntity.nMvwId = 33;
        }else  if(word.contains("五条")){ //第六页
            mvwLParamEntity.nMvwId = 34;
        }else  if(word.contains("六条")){ //第六页
            mvwLParamEntity.nMvwId = 35;
        }else  if(word.contains("七条")){ //第六页
            mvwLParamEntity.nMvwId = 36;
        }else  if(word.contains("八条")){ //第六页
            mvwLParamEntity.nMvwId = 37;
        }else  if(word.contains("九条")){ //第六页
            mvwLParamEntity.nMvwId = 38;
        } else  if(word.contains("确定")){
            mvwLParamEntity.nMvwId = 0;
        }else  if(word.contains("取消")){
            mvwLParamEntity.nMvwId = 1;
        } else
            mvwLParamEntity.nMvwId = 0;

        return  mvwLParamEntity;
    }
}
