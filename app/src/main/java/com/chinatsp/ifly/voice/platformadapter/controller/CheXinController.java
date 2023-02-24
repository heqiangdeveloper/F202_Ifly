package com.chinatsp.ifly.voice.platformadapter.controller;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.entity.CheXinEntity;
import com.chinatsp.ifly.entity.MessageListEvent;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.ICheXinController;
import com.chinatsp.ifly.voice.platformadapter.entity.DataEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.example.loginarar.LoginManager;
import com.google.gson.JsonObject;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.controllerInterface.IController;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.util.NetworkUtil;
import com.iflytek.sr.SrSession;
import org.greenrobot.eventbus.EventBus;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
public class CheXinController extends BaseController implements ICheXinController {
    private static final String TAG = "CheXinController";
    private static final int MSG_TTS = 1000;
    private static final int MSG_TTS_AND_DISPATCH = 1001;
    private static final int MSG_SEARCH_RESULT_SELECT = 1002;
    private static final int MSG_ERROR_CONTACT = 1003;
    private static final int MSG_CHEXIN_SEND = 1004;
    private Context mContext;
    private MyHandler myHandler = new MyHandler(this);
    private ISpeechControlService mSpeechService;
    private IntentEntity intentEntity;
    private static final String PACKAGE_CHEXIN = "com.jidouauto.carletter";
    private int nMvwId;

    private static  final String CONTACT="#CONTACT#";

    public CheXinController(Context context, ISpeechControlService speechService) {
        this.mContext = context.getApplicationContext();
        this.mSpeechService = speechService;
        LogUtils.d(TAG, "mSpeechService:" + mSpeechService);
    }


    @Override
    public void srAction(IntentEntity intentEntity) {
        this.intentEntity = intentEntity;
        LogUtils.d(TAG, "operation:" + intentEntity.operation);
        LogUtils.d(TAG, "semantic:" + GsonUtil.objectToString(intentEntity.semantic));
        LogUtils.d(TAG, "text:" + intentEntity.text);
        if (PlatformConstant.Operation.SEND.equals(intentEntity.operation)) {
            //发消息大分支
            if (PlatformConstant.ContentType.TEXT.equals(intentEntity.semantic.slots.contentType)) {
                //我想发消息
                if (TextUtils.isEmpty(intentEntity.semantic.slots.receiver)) {
                    if (cheXinNormal(TtsConstant.CHEXINC2CONDITION)) {
                        //有网已登录且车信打开
                        String conditionId = TtsConstant.CHEXINC1CONDITION;
                        String mainMsg = mContext.getString(R.string.chexin_send_who);
                        Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_without_specifying_object,conditionId,R.string.condition_chexinC1);
                        startTTSandMultiInterface(conditionId, mainMsg);
                    }
                } else {
                    //我想发消息给【张三】
                    if (intentEntity.semantic.slots.content == null) {
                        if (cheXinNormal(TtsConstant.CHEXINC2CONDITION)) {
                            final List<CheXinEntity> cheXinEntityList = new ArrayList<>();
                            try {
                                if (intentEntity.data != null) {
                                    cheXinEntityList.addAll(GsonUtil.stringToList(intentEntity.data.result.toString(), CheXinEntity.class));
                                }
                                if (cheXinEntityList.size() <= 0) {
                                    String conditionId = TtsConstant.CHEXINC5CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_no_contact), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_with_specifying_object,conditionId,R.string.condition_chexinC5);
                                    noOne(conditionId, defaultTts);
                                } else if (cheXinEntityList.size() == 1) {
                                    String conditionId = TtsConstant.CHEXINC3CONDITION;
                                    String defaultTts = mContext.getString(R.string.chexin_leave_message_after_drop);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_with_specifying_object,conditionId,R.string.condition_chexinC3);
                                    onlyOne(conditionId, defaultTts);
                                } else {
                                    String conditionId = TtsConstant.CHEXINC4CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_which_one), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_with_specifying_object,conditionId,R.string.condition_chexinC4);
                                    moreOne(conditionId, defaultTts);
                                }
                            } catch (Exception e) {
                                LogUtils.e(TAG, "srAction e = " + e.toString());
                            }
                        }
                    } else {
                        if (cheXinNormal(TtsConstant.CHEXINC2CONDITION)) {
                            //我想发消息给【张三】，【周末烧烤】
                            final List<CheXinEntity> cheXinEntityList = new ArrayList<>();
                            try {
                                if (intentEntity.data != null) {
                                    cheXinEntityList.addAll(GsonUtil.stringToList(intentEntity.data.result.toString(), CheXinEntity.class));
                                }
                                if (cheXinEntityList.size() <= 0) {
                                    String conditionId = TtsConstant.CHEXINC8CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_no_contact), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_with_specifying_object_and_content,conditionId,R.string.condition_chexinC8);
                                    noOne(conditionId, defaultTts);
                                } else if (cheXinEntityList.size() == 1) {
                                    String conditionId = TtsConstant.CHEXINC6CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_sending_message), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_with_specifying_object_and_content,conditionId,R.string.condition_chexinC6);
                                    onlyOne(conditionId, defaultTts);
                                } else {
                                    String conditionId = TtsConstant.CHEXINC7CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_send_which_one), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_with_specifying_object_and_content,conditionId,R.string.condition_chexinC7);
                                    moreOne(conditionId, defaultTts);
                                }
                            } catch (Exception e) {
                                LogUtils.e(TAG, "srAction e = " + e.toString());
                            }
                        }
                    }
                }

                //发红包大分支
            } else if (PlatformConstant.ContentType.REDPACKET.equals(intentEntity.semantic.slots.contentType)) {
                //我想发红包
                if (TextUtils.isEmpty(intentEntity.semantic.slots.receiver)) {
                    if (cheXinNormal(TtsConstant.CHEXINC10CONDITION)) {
                        //有网已登录且车信打开
                        String conditionId = TtsConstant.CHEXINC9CONDITION;
                        String mainMsg = mContext.getString(R.string.chexin_send_who);
                        Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_without_specifying_object,conditionId,R.string.condition_chexinC9);
                        startTTSandMultiInterface(conditionId, mainMsg);
                    }
                } else {
                    //我想发红包给【张三】
                    if (intentEntity.semantic.slots.money == null) {
                        if (cheXinNormal(TtsConstant.CHEXINC10CONDITION)) {
                            final List<CheXinEntity> cheXinEntityList = new ArrayList<>();
                            try {
                                if (intentEntity.data != null) {
                                    cheXinEntityList.addAll(GsonUtil.stringToList(intentEntity.data.result.toString(), CheXinEntity.class));
                                }
                                if (cheXinEntityList.size() <= 0) {
                                    String conditionId = TtsConstant.CHEXINC13CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_no_contact), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_with_specifying_object,conditionId,R.string.condition_chexinC13);
                                    noOne(conditionId, defaultTts);
                                } else if (cheXinEntityList.size() == 1) {
                                    String conditionId = TtsConstant.CHEXINC11CONDITION;
                                    String defaultTts = mContext.getString(R.string.chexin_input_amount);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_with_specifying_object,conditionId,R.string.condition_chexinC11);
                                    onlyOne(conditionId, defaultTts);
                                } else {
                                    String conditionId = TtsConstant.CHEXINC12CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_which_one), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_with_specifying_object,conditionId,R.string.condition_chexinC12);
                                    moreOne(conditionId, defaultTts);
                                }
                            } catch (Exception e) {
                                LogUtils.e(TAG, "srAction e = " + e.toString());
                            }
                        }
                    } else {
                        //给【张三】发【10块】钱红包
                        if (cheXinNormal(TtsConstant.CHEXINC10CONDITION)) {
                            final List<CheXinEntity> cheXinEntityList = new ArrayList<>();
                            try {
                                if (intentEntity.data != null) {
                                    cheXinEntityList.addAll(GsonUtil.stringToList(intentEntity.data.result.toString(), CheXinEntity.class));
                                }
                                if (cheXinEntityList.size() <= 0) {
                                    String conditionId = TtsConstant.CHEXINC16CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_no_contact), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_with_specifying_object_and_determine_amount,conditionId,R.string.condition_chexinC16);
                                    noOne(conditionId, defaultTts);
                                } else if (cheXinEntityList.size() == 1) {
                                    String conditionId = TtsConstant.CHEXINC14CONDITION;
                                    String defaultTts = mContext.getString(R.string.chexin_confirmation_amount);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_with_specifying_object_and_determine_amount,conditionId,R.string.condition_chexinC14);
                                    onlyOne(conditionId, defaultTts);
                                } else {
                                    String conditionId = TtsConstant.CHEXINC15CONDITION;
                                    String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_send_red_envelopes_to_which_one), CONTACT, intentEntity.semantic.slots.receiver);
                                    Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_with_specifying_object_and_determine_amount,conditionId,R.string.condition_chexinC15);
                                    moreOne(conditionId, defaultTts);
                                }
                            } catch (Exception e) {
                                LogUtils.e(TAG, "srAction e = " + e.toString());
                            }
                        }
                    }
                }

                //发位置大分支
            } else if (PlatformConstant.ContentType.POSITION.equals(intentEntity.semantic.slots.contentType)) {
                //我想分享位置
                if (TextUtils.isEmpty(intentEntity.semantic.slots.receiver)) {
                    if (cheXinNormal(TtsConstant.CHEXINC18CONDITION)) {
                        //有网已登录且车信打开
                        String conditionId = TtsConstant.CHEXINC17CONDITION;
                        String mainMsg = mContext.getString(R.string.chexin_send_who);
                        Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_position,R.string.object_send_position_without_specifying_object,conditionId,R.string.condition_chexinC17);
                        startTTSandMultiInterface(conditionId, mainMsg);
                    }
                } else {
                    //我想分享位置给【张三】
                    if (cheXinNormal(TtsConstant.CHEXINC10CONDITION)) {
                        final List<CheXinEntity> cheXinEntityList = new ArrayList<>();
                        try {
                            if (intentEntity.data != null) {
                                cheXinEntityList.addAll(GsonUtil.stringToList(intentEntity.data.result.toString(), CheXinEntity.class));
                            }
                            if (cheXinEntityList.size() <= 0) {
                                String conditionId = TtsConstant.CHEXINC21CONDITION;
                                String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_no_contact), CONTACT, intentEntity.semantic.slots.receiver);
                                Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_position,R.string.object_send_position_with_specifying_object,conditionId,R.string.condition_chexinC21);
                                noOne(conditionId, defaultTts);
                            } else if (cheXinEntityList.size() == 1) {
                                String conditionId = TtsConstant.CHEXINC19CONDITION;
                                String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_send_position), CONTACT, intentEntity.semantic.slots.receiver);
                                Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_position,R.string.object_send_position_with_specifying_object,conditionId,R.string.condition_chexinC19);
                                onlyOne(conditionId, defaultTts);
                            } else {
                                String conditionId = TtsConstant.CHEXINC20CONDITION;
                                String defaultTts = Utils.replaceTts(mContext.getString(R.string.chexin_which_one), CONTACT, intentEntity.semantic.slots.receiver);
                                Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_position,R.string.object_send_position_with_specifying_object,conditionId,R.string.condition_chexinC20);
                                moreOne(conditionId, defaultTts);
                            }
                        } catch (Exception e) {
                            LogUtils.e(TAG, "srAction e = " + e.toString());
                        }
                    }
                }
            }
            //播报未读消息大分支
        } else if (PlatformConstant.Operation.LAUNCH.equals(intentEntity.operation)) {
            if (cheXinNormal(TtsConstant.CHEXINC2CONDITION)) {
                dispatchSRAction(intentEntity);
            }
        }else if (PlatformConstant.Operation.POS_RANK.equals(intentEntity.operation)) {
            //判空
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null||intentEntity.semantic.slots.posRank==null||intentEntity.semantic.slots.posRank.offset==null) {
                doExceptonAction(mContext);
                return;
            }
            MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
            try {
                mvwLParamEntity.nMvwId = Integer.parseInt(intentEntity.semantic.slots.posRank.offset) - 1;
                mvwLParamEntity.nMvwId = mvwLParamEntity.nMvwId < 0 ? 0 : mvwLParamEntity.nMvwId;
                mvwLParamEntity.nMvwId = mvwLParamEntity.nMvwId > 4 ? 4 : mvwLParamEntity.nMvwId;
            } catch (Exception e) {}
            mvwLParamEntity.nMvwScene = TspSceneAdapter.TSP_SCENE_SELECT;
            selectItem(mvwLParamEntity);
        } else {
            doExceptonAction(mContext);
        }
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        switch (mvwLParamEntity.nMvwScene) {
            case MvwSession.ISS_MVW_SCENE_SELECT:
                selectItem(mvwLParamEntity);
                break;
            case MvwSession.ISS_MVW_SCENE_CONFIRM:
                confirm(mvwLParamEntity);
                break;
        }
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    @Override
    public void showCheXinActivity(String resultStr, String semanticStr) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semanticStr);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN);
        mContext.startActivity(intent);
    }

    private void srSelectItem(int nMvwId){
        //判断是否在选择场景
        try {
            if(TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_SELECT){
               //蓝牙电话界面
                IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
                if (iController != null) {
                    if (iController instanceof CheXinController) {
                        MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
                        mvwLParamEntity.nMvwId = nMvwId;
                        mvwLParamEntity.nMvwScene = TspSceneAdapter.TSP_SCENE_SELECT;
                        selectItem(mvwLParamEntity);
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            doExceptonAction(mContext);
        }
    }

    private boolean cheXinNormal(String conditionId) {
        //无网时
        if (!NetworkUtil.isNetworkAvailable(mContext)) {
            startTTS(TtsConstant.MAINC19CONDITION, mContext.getString(R.string.no_network_tip));
            return false;
        }
        //有网未登录时
        String userToken = LoginManager.getInstance().getUserToken();
        if (TextUtils.isEmpty(userToken)) {
            LoginManager.getInstance().sendBroadcastToScanCodeView(mContext);
            startTTS(conditionId, mContext.getString(R.string.chexin_no_login));
            if (conditionId.equals(TtsConstant.CHEXINC2CONDITION)){
                Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_message,R.string.object_send_message_without_specifying_object,conditionId,R.string.condition_chexinC2);
            }else if (conditionId.equals(TtsConstant.CHEXINC10CONDITION)){
                Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_red_packet,R.string.object_send_red_envelopes_without_specifying_object,conditionId,R.string.condition_chexinC10);
            }else if (conditionId.equals(TtsConstant.CHEXINC18CONDITION)){
                Utils.eventTrack(mContext,R.string.skill_chexin,R.string.scene_send_position,R.string.object_send_position_without_specifying_object,conditionId,R.string.condition_chexinC18);
            }
            return false;
        }

        if (!PACKAGE_CHEXIN.equals(ActivityManagerUtils.getInstance(mContext).getTopPackage())){
            startTTSOnly(mContext.getString(R.string.chexin_open), new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    startApp("车信");
                    if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                        EventBusUtils.sendExitMessage();
                    }
                }
            });

            return false;
        }

        return true;
    }


    private class MyHandler extends Handler {

        private final WeakReference<CheXinController> cheXinControllerWeakReference;

        private MyHandler(CheXinController cheXinController) {
            this.cheXinControllerWeakReference = new WeakReference<>(cheXinController);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            final CheXinController cheXinController = cheXinControllerWeakReference.get();
            if (cheXinController == null) {
                LogUtils.d(TAG, "cheXinController == null");
                return;
            }
            Bundle data;
            String ttsText;
            String conditionId;
            switch (msg.what) {
                case MSG_TTS:
                    data = msg.getData();
                    Utils.checkNotNull(data, "bundle can't be null");
                    ttsText = data.getString("ttsText");
                    conditionId = data.getString("conditionId");
                    startTTS(conditionId, ttsText);
                    break;
                case MSG_TTS_AND_DISPATCH:
                    data = msg.getData();
                    Utils.checkNotNull(data, "bundle can't be null");
                    ttsText = data.getString("ttsText");
                    conditionId = data.getString("conditionId");
                    Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, ttsText, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                                EventBusUtils.sendExitMessage();
                            }
                            dispatchSRAction(intentEntity);
                        }
                    });
                    break;
                case MSG_SEARCH_RESULT_SELECT:
                    data = msg.getData();
                    Utils.checkNotNull(data, "bundle can't be null");
                    ttsText = data.getString("ttsText");
                    conditionId = data.getString("conditionId");
                    Utils.getMessageWithTtsSpeak(mContext, conditionId, ttsText, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            //重新计算超时
                            SRAgent.getInstance().resetSrTimeCount();
                            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                        }
                    });
                    break;
                case MSG_ERROR_CONTACT:
                    cheXinController.waitContactNameMultiInterface();
                    data = msg.getData();
                    Utils.checkNotNull(data, "bundle can't be null");
                    ttsText = data.getString("ttsText");
                    conditionId = data.getString("conditionId");
                    Utils.getMessageWithTtsSpeak(mContext, conditionId, ttsText, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            //重新计算超时
                            SRAgent.getInstance().resetSrTimeCount();
                            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                        }
                    });
                    break;
                case MSG_CHEXIN_SEND:
                    Utils.exitVoiceAssistant();
                    dispatchSRAction(nMvwId);
                    break;
            }
        }
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
        } else if (mvwLParamEntity.nMvwId == 16) { //不是他
            String conditionId = TtsConstant.CHEXINC36CONDITION;
            String ttsText = mContext.getString(R.string.chexin_find_contact_error);
            sendMessage(MSG_ERROR_CONTACT, conditionId, ttsText);
        } else { //第几页
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_PAGE;
            messageEvent.index = mvwLParamEntity.nMvwId - 8;
            EventBus.getDefault().post(messageEvent);
        }
    }


    private void noOne(String conditionId, String defaultTts) {
        LogUtils.w(TAG, "cheXinEntityList.size()<=0");
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                } else {
                    ttsText = Utils.replaceTts(ttsText, CONTACT, intentEntity.semantic.slots.receiver);
                }
                sendMessage(MSG_TTS, conditionId, ttsText);
            }
        });
    }


    private void onlyOne(String conditionId, String defaultTts) {
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.CHEXINC3CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }
                sendMessage(MSG_TTS_AND_DISPATCH, conditionId, ttsText);
            }
        });
    }


    private void moreOne(String conditionId, String defaultTts) {
        showCheXinActivity(intentEntity.data.result.toString(), GsonUtil.objectToString(intentEntity.semantic));

        //多个结果
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.NAVIC4CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                } else {
                    ttsText = Utils.replaceTts(ttsText, CONTACT, intentEntity.semantic.slots.receiver);
                }
                sendMessage(MSG_SEARCH_RESULT_SELECT, conditionId, ttsText);
            }
        });
    }


    private void sendMessage(int what, String conditionId, String ttsText) {
        if (TextUtils.isEmpty(ttsText)) {
            return;
        }
        if (ttsText.indexOf(CONTACT) != -1) {
            ttsText = ttsText.replaceAll(CONTACT, "");
        }
        Message msg = myHandler.obtainMessage(what);
        Bundle data = new Bundle();
        data.putString("conditionId", conditionId);
        data.putString("ttsText", ttsText);
        msg.setData(data);
        myHandler.sendMessageDelayed(msg, 1000);
    }

    @Override
    public void dispatchSRAction(IntentEntity intentEntity) {
        if (mSpeechService != null) {
            try {
                mSpeechService.dispatchSRAction(Business.CHEXIN, intentEntity.convert2NlpVoiceModel());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispatchSRAction(int nMvwId) {
        if (mSpeechService != null) {
            try {
                mSpeechService.dispatchSRAction(Business.CHEXIN, convert2NlpVoiceModel(nMvwId));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startSend(int nMvwId) {
        this.nMvwId = nMvwId;
        Message msg = myHandler.obtainMessage(MSG_CHEXIN_SEND);
        myHandler.sendMessageDelayed(msg, 1 * 1000);
    }


    private NlpVoiceModel convert2NlpVoiceModel(int nMvwId) {
        NlpVoiceModel nlpVoiceModel = intentEntity.convert2NlpVoiceModel();
        nlpVoiceModel.dataEntity = GsonUtil.objectToString(getDataEntity(nMvwId));
        return nlpVoiceModel;
    }

    private DataEntity getDataEntity(int nMvwId) {
        DataEntity dataEntity = new DataEntity();
        dataEntity.result= new ArrayList<JsonObject>();
        dataEntity.result.add(intentEntity.data.result.get(nMvwId));
        dataEntity.debug = intentEntity.data.debug;
        dataEntity.dataUrl = intentEntity.data.dataUrl;
        dataEntity.error = intentEntity.data.error;
        return dataEntity;
    }



    public void startTTS(String conditionId, String defaultTts) {
        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultTts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                if (!FloatViewManager.getInstance(mContext).isHide()) {
                    FloatViewManager.getInstance(mContext).hide();
                }
            }
        });
    }


    private void waitContactNameMultiInterface() {
        //"不是他"免唤醒说法
        SRAgent srAgent = SRAgent.getInstance();
        if (FloatViewManager.getInstance(mContext).isHide()) {
            if (!srAgent.mSrArgu_New.scene.equals(SrSession.ISS_SR_SCENE_ALL)) {
                srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
                srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");
                srAgent.stopSRSession();
                srAgent.startSRSession();
            }
        }
        MultiInterfaceUtils.getInstance(mContext).uploadAppStatusData(true, PlatformConstant.Service.WEIXIN, "default");
        IntentEntity mIntentEntity = new IntentEntity();
        mIntentEntity.service = PlatformConstant.Service.WEIXIN;
        mIntentEntity.operation = intentEntity.operation;
        mIntentEntity.semantic = intentEntity.semantic;
        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(mIntentEntity);
    }

    public void startTTSandMultiInterface(String conditionId,String mainMsg){
        Utils.getMessageWithTtsSpeak(mContext, conditionId, mainMsg, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                waitContactNameMultiInterface();
                //重新计算超时
                SRAgent.getInstance().resetSrTimeCount();
                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
            }
        });
    }


    /**
     * 选择场景下，走无语音的返回
     * @param text
     */
    public void onDoAction(String text) {
        Log.d(TAG, "onDoAction() called with: text = [" + text + "]");
        if(text==null||"".equals(text)){
            doExceptonAction(mContext);
        }else {
            if(text.contains("确定")||text.contains("取消")){
                confirm(selectWordToIndex(text));
            }else
                selectItem(selectWordToIndex(text));
        }
    }

}
