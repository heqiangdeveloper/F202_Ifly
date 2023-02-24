package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.ISpeechSetController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.mvw.MvwSession;

import java.lang.ref.WeakReference;

public class SpeechSetController extends BaseController implements ISpeechSetController {

    private static final String TAG = "SpeechSetController";
    private Context mContext;
    private static final int MSG_TTS = 1000;
    private static final int MSG_GUIDE_USER = 1001;
    private static final int MSG_TTS_HIDE = 1002;
    private IntentEntity intentEntity;
    private MyHandler myHandler = new MyHandler(this);

    public SpeechSetController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void changeName(String productName) {
        Log.d(TAG, "changeName() called with: productName = [" + productName + "]");
        productName = productName.replace("你好", "");
//        int whichName = SharedPreferencesUtils.getInt(mContext, AppConstant.KEY_WHICH_NAME, 0);
        String globalStr;
      /*  if (whichName == 0) {
            String name2 = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_CURRENT_NAME_2, "");
            globalStr = MvwKeywordsUtil.getChangeKeywordJson(mContext, productName, name2);
        } else {
            String name1 = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_CURRENT_NAME_1, "");
            globalStr = MvwKeywordsUtil.getChangeKeywordJson(mContext, name1, productName);
        }*/
        globalStr = MvwKeywordsUtil.addMvwKeywordJson(mContext,productName);
        MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME,
                globalStr);
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.getTspScene(mContext));  //恢复上一次的场景
        SharedPreferencesUtils.saveInt(mContext, AppConstant.KEY_WHICH_NAME, 2);  //防止进入设置界面，显示名字不一致
//        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_GLOBAL);

        String productName2 = productName;
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MAINC21CONDITION, mContext.getString(R.string.make_name), new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                tts = tts.replace(TtsConstant.VOICENAME, productName2);
                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_change_or_ask, R.string.object_main_change, TtsConstant.MAINC21CONDITION, R.string.condition_mainC21,tts);

                myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_TTS_HIDE, tts), 1000);
            }
        });

        SRAgent.getInstance().resetSession();
    }

    @Override
    public void getName() {
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MAINC22CONDITION, mContext.getString(R.string.ask_name), new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                tts = tts.replace("#VOICENAME#", MvwKeywordsUtil.getCurrentName(mContext));
                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_change_or_ask, R.string.object_main_ask, TtsConstant.MAINC22CONDITION, R.string.condition_mainC22,tts);
                myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_TTS, tts), 1000);
            }
        });

    }

    public void guideUserSetName() {
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MAINC21_1CONDITION, mContext.getString(R.string.set_name), new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_change_or_ask, R.string.object_main_change, TtsConstant.MAINC21_1CONDITION, R.string.condition_mainC21_1,tts);
                myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_GUIDE_USER, tts), 1000);
            }
        });
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        Log.d(TAG, "srAction() called with: intentEntity = [" + intentEntity.operation + "]");
        this.intentEntity = intentEntity;
        //特殊处理
        if ("你的名字叫什么".equals(intentEntity.text)) {
            getName();
            return;
        }

        if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
            //取名字情况
            if (intentEntity.text.contains("取个名字") || intentEntity.text.contains("起个名字")
                    ||intentEntity.text.contains("改个名字") || intentEntity.text.contains("取的名字")
                    || intentEntity.text.contains("改的名字")
                    || intentEntity.text.contains("起的名字")) {
                if (intentEntity.semantic.slots.productName != null) {  //带名字
                    String productName = intentEntity.semantic.slots.productName;
                    LogUtils.d(TAG, "productName：" + productName);
                    if (isSetFail(productName)){
                        return;
                    }
                    changeName(productName);
                } else { //不带名字，引导用户
                    guideUserSetName();
                }
            }else {
               //TODO 进行规避
                if (intentEntity.semantic.slots.productName != null) {  //带名字
                    String productName = intentEntity.semantic.slots.productName;
                    LogUtils.d(TAG, "productName：" + productName);
                    if (isSetFail(productName)){
                        return;
                    }
                    changeName(productName);
                } else { //不带名字，引导用户
                    guideUserSetName();
                }
            }

        } else if (PlatformConstant.Operation.QUERY.equals(intentEntity.operation)) {

            getName();
        } else {
//            baiduNlpProcess(mContext, intentEntity.text);
//            startTTS("该功能还未实现");
            doExceptonAction(mContext);

        }

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity o) {

    }
    private static final String CHINESE_REG = "[\\u4e00-\\u9fa5]{2,5}";//表示+表示一个或多个中文
    public boolean isSetFail(String productName){
        Log.d(TAG,"productName ="+productName);
        if (!productName.matches(CHINESE_REG))
        {
            Log.d(TAG,"true ");
            String tip = mContext.getString(R.string.tip_revise_aware);
            String set_fail = mContext.getString(R.string.set_fail);
            int start = tip.indexOf("1.");
            int end = tip.indexOf("2.");
            String errName = tip.substring(start+2,end);
            startTTSOnly(set_fail+errName, new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    Utils.exitVoiceAssistant();
                }
            });
            return true;
        }
        return false;
    }

    private static class MyHandler extends Handler {

        private final WeakReference<SpeechSetController> setControllerWeakReference;

        private MyHandler(SpeechSetController speechSetController) {
            this.setControllerWeakReference = new WeakReference<>(speechSetController);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final SpeechSetController speechSetController = setControllerWeakReference.get();
            if (speechSetController == null) {
                LogUtils.d(TAG, "speechSetController == null");
                return;
            }
            switch (msg.what) {
                case MSG_TTS:
                    speechSetController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            Utils.exitVoiceAssistant();
                        }
                    });
                    break;
                case MSG_TTS_HIDE:
                    speechSetController.startTTSOnly(String.valueOf(msg.obj), new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            FloatViewManager.getInstance(speechSetController.mContext).hide();
                        }
                    });
                    break;
                case MSG_GUIDE_USER:
                    speechSetController.startTTS(String.valueOf(msg.obj), new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            speechSetController.waitChangeNameMultiInterface();
                            //重新计算超时
                            SRAgent.getInstance().resetSrTimeCount();
                            TimeoutManager.saveSrState(speechSetController.mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                        }
                    });
                    break;
            }
        }
    }

    private void waitChangeNameMultiInterface() {
        LogUtils.d(TAG, "waitChangeNameMultiInterface");

        //保存二次交互语义
        if (intentEntity == null) {
            IntentEntity intentEntity1 = new IntentEntity();
            intentEntity1.service = PlatformConstant.Service.PERSONALNAME;
            intentEntity1.operation = PlatformConstant.Operation.SET;
            intentEntity1.semantic = new Semantic();
            MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity1);
        } else {
            MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
        }
    }
}
