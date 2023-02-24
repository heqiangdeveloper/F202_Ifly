package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.db.TtsInfoDbDao;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.entity.CarNumberEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.TtsText;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.proxy.IVehicleNetworkRequestCallback;
import com.chinatsp.proxy.VehicleNetworkManager;
import com.google.gson.Gson;
import com.iflytek.adapter.sr.SRAgent;
import java.util.HashMap;
import java.util.List;


/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/8/3
 */

public class ElseController extends BaseController {
    private final String TAG = "ElseController";
    private Context mContext;
    private static ElseController elseController;

    private ElseController(Context mContext){
        this.mContext = mContext;
    }

    public static ElseController getInstance(Context mContext){
        if(elseController == null){
            elseController = new ElseController(mContext);
        }
        return elseController;
    }

    public void srAction(IntentEntity intentEntity,String var1) {
        Log.d(TAG,"intentEntity ="+intentEntity.service);
        List<TtsInfo> ttsInfoList= TtsInfoDbDao.getInstance(mContext).queryTtsInfo("skill_expand_qa");
        Log.d(TAG,"ttsInfoList.size="+ttsInfoList.size());
        if (ttsInfoList.size()>0){
            for (int i=0;i<ttsInfoList.size();i++){
                String TtsTest= ttsInfoList.get(i).getTtsText();
                Log.d(TAG,"TtsTest="+TtsTest);
                if (TtsTest.contains(intentEntity.service)){
                    if(null != intentEntity.answer && null != intentEntity.answer.text){
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
                    break;
                }
                if (i==ttsInfoList.size()-1){
                    if(!TtsTest.contains(intentEntity.service)){
                        requeryOtherTtsInfo(intentEntity,var1);
                    }
                }
            }
        }else {
            requeryOtherTtsInfo(intentEntity,var1);
        }
    }


    //当该条件下存在对应service，post请求url接口
    private void requeryOtherTtsInfo(IntentEntity intentEntity,String var1){

        List<TtsInfo> ttsInfoList= TtsInfoDbDao.getInstance(mContext).queryTtsInfo("skill_expand_action");
        Log.d(TAG,"ttsInfoList.size="+ttsInfoList.size());
        if (ttsInfoList.size()>0){
            for (int i=0;i<ttsInfoList.size();i++){
                String mTtsTest= ttsInfoList.get(i).getTtsText();
                Log.d(TAG,"mTtsTest ="+mTtsTest);
                TtsText ttsTest = new Gson().fromJson(mTtsTest,TtsText.class);
                if (mTtsTest.contains(intentEntity.service)){
                    if(ttsTest.getUrl().isEmpty()){
                        exitVoice(mContext);
                    }else {
                        requestRul(ttsTest.getUrl(),var1);
                    }
                    break;
                }
                if (i==ttsInfoList.size()-1){
                    if(!mTtsTest.contains(intentEntity.service)){
                        exitVoice(mContext);
                    }
                }
            }
        }else {
            exitVoice(mContext);
        }
    }

    private void exitVoice(Context mContext){
        String normalMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
        if (!FloatViewManager.getInstance(mContext).isHide()) {
            timeoutAndExit("",normalMsg);
        }
    }


    private void timeoutAndExit(String conditionId, String defaultText) {
        SRAgent.getInstance().resetSrTimeCount();
        if(FloatViewManager.getInstance(mContext).isHide()) return;
        TTSController.OnTtsStoppedListener listener = new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                Utils.exitVoiceAssistant();
            }
        };
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                Utils.startTTSOnly(tts, listener);
            }
        });
    }

    private void requestRul(String url,String data){
        Log.d(TAG,"data==="+data);
        Log.d(TAG,"AppConfig.INSTANCE.token="+AppConfig.INSTANCE.token);
        if (AppConfig.INSTANCE.token==null|| TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("accesstoken", AppConfig.INSTANCE.token);
        params.put("data",data);
       VehicleNetworkManager.getInstance().requestNet(url,"POST",params, new IVehicleNetworkRequestCallback() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG,"onSuccess--------S ="+s);
                try {
                    CarNumberEntity carNumberEntity = new Gson().fromJson(s,CarNumberEntity.class);
                    carNumberEntity.getAnswer().getText();
                    if(null != carNumberEntity.getAnswer() && null != carNumberEntity.getAnswer().getText()){
                        startTTSOnly(carNumberEntity.getAnswer().getText(), new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                Utils.exitVoiceAssistant();
                            }
                        });
                    }
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG,"onError--------S ="+s);
                startTTSOnly(" 网络无法连接，请稍后再试", new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
            }

            @Override
            public void onProgress(float v) {

            }
        });
    }
}
