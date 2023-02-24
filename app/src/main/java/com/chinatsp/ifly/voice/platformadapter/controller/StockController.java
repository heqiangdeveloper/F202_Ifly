package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.entity.ReplaceTtsEntity;
import com.chinatsp.ifly.entity.StockEntity;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IStockController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.chinatsp.ifly.api.constantApi.TtsConstant.STOCKC4CONDITION;

public class StockController extends BaseController implements IStockController {
    private static final String TAG = "TrainController";
    private Context mContext;
    private IntentEntity intentEntity;
    private static final int MSG_TTS = 1000;

    public StockController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        this.intentEntity = intentEntity;
//        EventBusUtils.sendTalkMessage(intentEntity.text);
        //下一页
        if (PlatformConstant.Operation.QUERY.equals(intentEntity.operation)) {
            //DatastatManager.getInstance().voiceEventTracking(mContext, R.string.skill_stock, R.string.scene_stock, R.string.object_stock);
            if (intentEntity.data != null && intentEntity.data.result.size()>0) { //返回有数据
                showStockActivity(intentEntity.data.result.toString(),"");
            } else {
                showNoPermitAction(intentEntity.semantic);
            }
        }else {
            doExceptonAction(mContext);
        }
    }

    private void showNoPermitAction(Semantic semantic) {
        if (semantic != null) {
            String name = "";
            if (!TextUtils.isEmpty(semantic.slots.name)) {
                name = semantic.slots.name;
            } else if (!TextUtils.isEmpty(semantic.slots.code)) {
                name = semantic.slots.code;
            }

            String tts = String.format(mContext.getString(R.string.stock_no_data), name);
            getTtsMessage(STOCKC4CONDITION, tts, null,name);
            Utils.eventTrack(mContext,R.string.skill_stock, R.string.scene_stock, R.string.object_stock, STOCKC4CONDITION, R.string.condition_stock4);
        }
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
    }

    @Override
    public void stkAction(StkResultEntity o) {
    }

    @Override
    public void showStockActivity(String resultStr,String answerText) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.ANSWER_STR,answerText);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_STOCK);
        mContext.startActivity(intent);
    }

    private void StartTTS(String tts){
        Utils.startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
//                FloatViewManager.getInstance(mContext).hide();
                Utils.exitVoiceAssistant();
            }
        });
    }


    private void getTtsMessage(String conditionId,String defaultTts, StockEntity stockEntity,String name) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = defaultTts;
                } else {
                    if (stockEntity != null) {
                        defaultText = Utils.replaceTts(defaultText, "#STOCK#", stockEntity.getName());
                        defaultText = Utils.replaceTts(defaultText, "#PRICE#", stockEntity.getCurrentPrice());
                        defaultText = Utils.replaceTts(defaultText, "#VALUE#", stockEntity.getRiseValue());
                        defaultText = Utils.replaceTts(defaultText, "#RATE#", stockEntity.getRiseRate());
                    }else{
                        defaultText = Utils.replaceTts(defaultText, "#STOCK#", name);
                    }
                }
                StartTTS(defaultText);
            }
        });
    }
}
