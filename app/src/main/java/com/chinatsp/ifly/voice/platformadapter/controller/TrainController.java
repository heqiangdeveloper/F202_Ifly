package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.EventTrackingEntity;
import com.chinatsp.ifly.entity.MessageListEvent;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.ITrainController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.mvw.MvwSession;
import org.greenrobot.eventbus.EventBus;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;

public class TrainController extends BaseController implements ITrainController {
    private static final String TAG = "TrainController";
    private Context mContext;

    public TrainController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        if (PlatformConstant.Operation.QUERY.equals(intentEntity.operation)) {
            if (intentEntity.data != null && intentEntity.data.result != null) { //返回有数据
                if (intentEntity.data.result.size() < 1){
                    getTtsMessageOnly(TRAINC2CONDITION,R.string.train_query_no_data);
                    Utils.eventTrack(mContext,R.string.skill_train,R.string.scene_train,R.string.object_train,TRAINC2CONDITION,R.string.condition_train2);
                }else{
                    getTtsMessage(TRAINC1CONDITION,R.string.train_query_has_data);
                    Utils.eventTrack(mContext,R.string.skill_train,R.string.scene_train,R.string.object_train,TRAINC1CONDITION,R.string.condition_train1);
                    showTrainActivity(intentEntity.data.result.toString(), GsonUtil.objectToString(intentEntity.semantic));
                }

            }else{
                getTtsMessageOnly(TRAINC3CONDITION,R.string.train_query_fail);
                Utils.eventTrack(mContext,R.string.skill_train,R.string.scene_train,R.string.object_train,TRAINC3CONDITION,R.string.condition_train3);
            }
        }else
            doExceptonAction(mContext);
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        switch (mvwLParamEntity.nMvwScene) {
            case MvwSession.ISS_MVW_SCENE_GLOBAL:
                LogUtils.i(TAG, "TSP_SCENE_GLOBAL");
                break;
            case MvwSession.ISS_MVW_SCENE_SELECT:
                selectItem(mvwLParamEntity);
                break;
            case MvwSession.ISS_MVW_SCENE_CONFIRM:
                confirm(mvwLParamEntity);
                break;
        }

    }

    @Override
    public void stkAction(StkResultEntity o) {

    }

    @Override
    public void showTrainActivity(String resultStr, String semantic) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_TRAIN);
        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semantic);
        Log.d(TAG,"=======semantic："+semantic);
        mContext.startActivity(intent);
    }

    private void getTtsMessage(String conditionId, int defaultTts) {

        Utils.getMessageWithTtsSpeak(mContext, conditionId, mContext.getString(defaultTts), new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                //重新计算超时
                SRAgent.getInstance().resetSrTimeCount();
                String text = "选择请说第几个，翻页请说下一页"; //未定义
                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, text);
            }
        });
    }

    private void getTtsMessageOnly(String conditionId, int defaultTts) {

        Utils.getMessageWithTtsSpeak(mContext, conditionId, mContext.getString(defaultTts), new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
               FloatViewManager.getInstance(mContext).hide();
            }
        });
    }

    private void selectItem(MvwLParamEntity mvwLParamEntity) {

        if (mvwLParamEntity.nMvwId < 6) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId == 6) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.FINAL_PAGE;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId == 7) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.LAST_PAGE;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId == 8) {
            MessageListEvent messageEvent1 = new MessageListEvent();
            messageEvent1.eventType = MessageListEvent.ListEventType.NEXT_PAGE;
            EventBus.getDefault().post(messageEvent1);
        } else if (mvwLParamEntity.nMvwId >=9&&mvwLParamEntity.nMvwId <=14) { //第几页
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_PAGE;
            messageEvent.index = mvwLParamEntity.nMvwId - 8;
            EventBus.getDefault().post(messageEvent);
        }else if (mvwLParamEntity.nMvwId >=15&&mvwLParamEntity.nMvwId <=17) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId-9;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId >=18&&mvwLParamEntity.nMvwId <=20) { //第几页
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_PAGE;
            messageEvent.index = mvwLParamEntity.nMvwId - 11;
            EventBus.getDefault().post(messageEvent);
        }else if (mvwLParamEntity.nMvwId >=21&&mvwLParamEntity.nMvwId <=29) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId-21;
            EventBus.getDefault().post(messageEvent);
        }else if (mvwLParamEntity.nMvwId >=30&&mvwLParamEntity.nMvwId <=38) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId-30;
            EventBus.getDefault().post(messageEvent);
        }else doExceptonAction(mContext);
    }

    private void confirm(MvwLParamEntity mvwLParamEntity) {
        switch (mvwLParamEntity.nMvwId) {
            case 0:
                LogUtils.i(TAG, "确定");
                MessageListEvent messageEvent = new MessageListEvent();
                messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
                messageEvent.index = mvwLParamEntity.nMvwId;
                EventBus.getDefault().post(messageEvent);
                break;
            case 1:
                //取消
                LogUtils.i(TAG, "取消");
                Utils.exitVoiceAssistant();
                break;
        }
    }

}
