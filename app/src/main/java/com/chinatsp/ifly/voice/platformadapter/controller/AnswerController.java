package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.util.Log;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;

/**
 * Created by Administrator on 2020/6/12.
 */

public class AnswerController extends BaseController{
    private final String TAG = "AnswerController";
    private Context mContext;
    private static AnswerController mAnswerController;

    public static AnswerController getInstance(Context mContext){
        if(mAnswerController == null){
            mAnswerController = new AnswerController(mContext);
        }
        return mAnswerController;
    }

    private AnswerController(Context mContext){
        this.mContext = mContext;
    }

    public void srAction(IntentEntity intentEntity) {
        if(/*PlatformConstant.Service.DATETIME.equals(intentEntity.service) ||*/ PlatformConstant.Service.CALC.equals(intentEntity.service)){
            if(null != intentEntity.answer && null != intentEntity.answer.text){
                Utils.eventTrack(mContext, R.string.skill_calc, R.string.scene_calc, R.string.condition_calc, TtsConstant.CALCC1CONDITION, R.string.condition_calcC1,intentEntity.answer.text);
                startTTSOnly(intentEntity.answer.text, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
            }else {
                Log.d(TAG,"intentEntity.answer.text is null");
                doExceptonAction(mContext);
            }
        }else if(PlatformConstant.Service.POETRY.equals(intentEntity.service)){
            if(null != intentEntity.answer && null != intentEntity.answer.display_text){
                Utils.eventTrack(mContext, R.string.skill_poetry, R.string.scene_poetry, R.string.condition_poetry, TtsConstant.POETRYC1CONDITION, R.string.condition_poetryC1,intentEntity.answer.display_text);
                startTTSOnly(intentEntity.answer.display_text, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
            }else {
                Log.d(TAG,"intentEntity.answer.display_text is null");
                doExceptonAction(mContext);
            }
        }else if(PlatformConstant.Service.DATETIME.equals(intentEntity.service)){
            if("HOWFAR".equals(intentEntity.operation)){
                if(null != intentEntity.answer && null != intentEntity.answer.display_text){
                    Utils.eventTrack(mContext, R.string.skill_data, R.string.scene_data, R.string.condition_data, TtsConstant.DATETIMEXC1CONDITION, R.string.condition_datetimeXC1,intentEntity.answer.display_text);
                    startTTSOnly(intentEntity.answer.display_text, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            Utils.exitVoiceAssistant();
                        }
                    });
                }else {
                    Log.d(TAG,"intentEntity.answer.display_text is null");
                    doExceptonAction(mContext);
                }
            } else if(null != intentEntity.answer && null != intentEntity.answer.text){
                Utils.eventTrack(mContext, R.string.skill_data, R.string.scene_data, R.string.condition_data, TtsConstant.DATETIMEXC1CONDITION, R.string.condition_datetimeXC1,intentEntity.answer.text);
                startTTSOnly(intentEntity.answer.text, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
            }else {
                Log.d(TAG,"intentEntity.answer.display_text is null");
                doExceptonAction(mContext);
            }
        }
        else {
            doExceptonAction(mContext);
        }
    }
}

