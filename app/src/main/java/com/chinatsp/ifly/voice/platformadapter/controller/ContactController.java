package com.chinatsp.ifly.voice.platformadapter.controller;
import android.bluetooth.BluetoothAdapter;
import android.car.CarNotConnectedException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
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
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.entity.MessageListEvent;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IContactController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.phone.BtPhoneSkillManager;
import com.chinatsp.phone.bean.BtSearcherContact;
import com.chinatsp.phone.bean.CallContact;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.controllerInterface.IController;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.mvw.MvwSession;
import com.iflytek.sr.SrSession;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ContactController extends BaseController implements IContactController {
    private static final String TAG = "ContactController";
    private static final String KEY_CALL = "contact";
    private static final String KEY_NAME = "name";
    private static final String KEY_TYPE = "type";
    private Context mContext;
    private IntentEntity intentEntity;
    private BluePhoneManager bluePhoneManager;
    private static ContactController mInstance;
    private static final int MSG_TTS = 1000;
    private static final int MSG_SEARCH_RESULT_SELECT = 1001;
    private static final int MSG_MAKE_DIAL = 1002;
    private static final int MSG_MAKE_DIAL_2ND = 1003;
    private static final int MSG_MAKE_REDIAL = 1004;
    private static final int MSG_MAKE_REDIAL_2ND = 1005;
    private static final int MSG_MAKE_CALLBACK = 1006;
    private static final int MSG_MAKE_CALLBACK_2ND = 1007;
    private static final int MSG_OPEN_BT = 1008;
    private static final int MSG_ERROR_CONTACT = 1009;
    private static final int MSG_TTS_WITH_EXIT = 1010;

    private static final int MSG_DELAY_EXIT = 1011;
    private static boolean Start_tts;
    private MyHandler myHandler = new MyHandler(this);

    public static ContactController getInstance(Context context){
        if(mInstance==null){
            synchronized (ContactController.class){
                if(mInstance==null)
                    mInstance = new ContactController(context);
            }
        }
        return mInstance;
    }
    private ContactController(Context context) {
        this.mContext = context.getApplicationContext();
        this.bluePhoneManager = BluePhoneManager.getInstance(context);
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        this.intentEntity = intentEntity;

        if (PlatformConstant.Operation.DIAL.equals(intentEntity.operation)) {
            Log.d(TAG, "srAction: BluePhoneManager.getInstance(mContext).Phone_Contact_State::"+BluePhoneManager.getInstance(mContext).Phone_Contact_State);
            if (intentEntity.data != null && intentEntity.data.result != null) { //返回有数据

                if(dialRescueCall())  //是否是救援号码
                    return;

                if (!bluePhoneManager.isBtConnected()) {
                    bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_SETTINGS);
                    getTtsMessage(TtsConstant.PHONEC8CONDITION, mContext.getString(R.string.bt_no_connect), MSG_OPEN_BT);
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC8CONDITION, R.string.condition_phoneC8);
                } else  if (intentEntity.semantic.slots!=null&&!TextUtils.isEmpty(intentEntity.semantic.slots.code)) {//打电话给指定号码 不需要同步联系人
                    String conditionId = TtsConstant.PHONEC11CONDITION;
                    String defaultText = mContext.getString(R.string.bt_make_dial_number);
                    String oldChar = "#PHONENUM#";
                    String newChar = "<figure>"+intentEntity.semantic.slots.code+"</figure type=ordinal>";
                    //String mainMsg = String.format(mContext.getString(R.string.bt_make_dial_number), intentEntity.semantic.slots.code);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            tts = tts.replace(oldChar, newChar);
                            Message msg = myHandler.obtainMessage(MSG_MAKE_DIAL, tts);
                            Bundle data = new Bundle();
                            data.putSerializable(KEY_CALL, new ContactEntity(intentEntity.semantic.slots.code, intentEntity.semantic.slots.code));
                            msg.setData(data);
                            myHandler.sendMessageDelayed(msg, 1000);
                            Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_number, TtsConstant.PHONEC11CONDITION, R.string.condition_phoneC11,tts);
                        }
                    });
                }else if (!bluePhoneManager.isContactsSwitchOn()) {
                    Log.d(TAG, "srAction:bluePhoneManager.isContactsSwitchOn():: "+bluePhoneManager.isContactsSwitchOn());
                    bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_SETTINGS);
                    getTtsMessage(TtsConstant.PHONEC8_1CONDITION, mContext.getString(R.string.bt_no_open_contact_switch), MSG_TTS_WITH_EXIT);
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC8_1CONDITION, R.string.condition_phoneC8_1);
                } else  if (!BluePhoneManager.getInstance(mContext).Phone_Contact_State){
                    if(bluePhoneManager.isContactsDownloading()){  //联系人没有同步完成，判断是否正在同步
                        getTtsMessage(TtsConstant.PHONEC6CONDITION, mContext.getString(R.string.bt_contact_sync_incomplete), MSG_TTS_WITH_EXIT);
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC6CONDITION, R.string.condition_phoneC6);
                        bluePhoneManager.setSyncContactState();
                        return;
                    }else {
                        bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_SETTINGS);
                        getTtsMessage(TtsConstant.PHONEC8_2CONDITION, mContext.getString(R.string.bt_no_open_contact_switch_2), MSG_TTS_WITH_EXIT);
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC8_2CONDITION, R.string.bt_no_open_contact_switch_1);
                        Log.e(TAG,"no ContactsDownloading" );
                    }
                    bluePhoneManager.setSyncContactState();
                } else {
//                    if(!bluePhoneManager.isIflySync){  //联系人没有同步完成，判断是否正在同步
//                        getTtsMessage(TtsConstant.PHONEC6CONDITION, mContext.getString(R.string.bt_contact_sync_incomplete), MSG_TTS_WITH_EXIT);
//                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC6CONDITION, R.string.condition_phoneC6);
//                        bluePhoneManager.setSyncContactState();
//                        return;
//                    }else {//本地联系人同步完成，并且讯飞资源也构建成功
                    //TODO 既然有数据，说明同步完成，只是讯飞没有同步完成的回调，0909 bugid 1055827
                        List<ContactEntity> contactEntityList = new ArrayList<>();
                        contactEntityList.addAll(GsonUtil.stringToList(intentEntity.data.result.toString(), ContactEntity.class));
                        startSelect(null,contactEntityList);
//                    }
                }
            } else {
                //拨打救援号码
                if(dialRescueCall())    //是否是救援号码
                    return;

                if (!bluePhoneManager.isBtConnected()) {
                    Log.d(TAG, "srAction: bluePhoneManager.isBtConnected():"+bluePhoneManager.isBtConnected());
                    bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_SETTINGS);
                    getTtsMessage(TtsConstant.PHONEC8CONDITION, mContext.getString(R.string.bt_no_connect), MSG_OPEN_BT);
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC8CONDITION, R.string.condition_phoneC8);
                } else  if (!TextUtils.isEmpty(intentEntity.semantic.slots.code)) {//打电话给指定号码 不需要同步联系人
                    String conditionId = TtsConstant.PHONEC11CONDITION;
                    String defaultText = mContext.getString(R.string.bt_make_dial_number);
                    String oldChar = "#PHONENUM#";
                    String newChar = "<figure>"+intentEntity.semantic.slots.code+"</figure type=ordinal>";
                    //String mainMsg = String.format(mContext.getString(R.string.bt_make_dial_number), intentEntity.semantic.slots.code);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            tts = tts.replace(oldChar, newChar);
                            Message msg = myHandler.obtainMessage(MSG_MAKE_DIAL, tts);
                            Bundle data = new Bundle();
                            data.putSerializable(KEY_CALL, new ContactEntity(intentEntity.semantic.slots.code, intentEntity.semantic.slots.code));
                            msg.setData(data);
                            myHandler.sendMessageDelayed(msg, 1000);
                            Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC11CONDITION, R.string.condition_phoneC11,tts);
                        }
                    });
                } else if (!bluePhoneManager.isContactsSwitchOn()) {
                    Log.e(TAG,"呼叫指定的联系人");
                    bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_SETTINGS);
                    getTtsMessage(TtsConstant.PHONEC8_1CONDITION, mContext.getString(R.string.bt_no_open_contact_switch), MSG_TTS_WITH_EXIT);
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC8_1CONDITION, R.string.condition_phoneC8_1);
                } else  if (!BluePhoneManager.getInstance(mContext).Phone_Contact_State){
                    if(bluePhoneManager.isContactsDownloading()){   //联系人没有同步完成，判断是否正在同步
                        getTtsMessage(TtsConstant.PHONEC6CONDITION, mContext.getString(R.string.bt_contact_sync_incomplete), MSG_TTS_WITH_EXIT);
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC6CONDITION, R.string.condition_phoneC6);
                        bluePhoneManager.setSyncContactState();
                        return;
                    }
                    Log.d(TAG, "srAction: BluePhoneManager.getInstance(mContext).Phone_Contact_State::"+BluePhoneManager.getInstance(mContext).Phone_Contact_State);
                    bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_SETTINGS);
                    bluePhoneManager.setSyncContactState();
                    getTtsMessage(TtsConstant.PHONEC8_1CONDITION, mContext.getString(R.string.bt_no_open_contact_switch), MSG_TTS_WITH_EXIT);
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC8_1CONDITION, R.string.condition_phoneC8_1);
                } else  if (BluePhoneManager.getInstance(mContext).Phone_Contact_State&&!bluePhoneManager.isIflySync){
                   //讯飞资源没有构建成功
                    getTtsMessage(TtsConstant.PHONEC6CONDITION, mContext.getString(R.string.bt_contact_sync_incomplete), MSG_TTS_WITH_EXIT);
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC6CONDITION, R.string.condition_phoneC6);
                    bluePhoneManager.setSyncContactState();
                    return;
                }/* else if (intentEntity.answer.text.contains("|")) {
                    FamilyCall(intentEntity.semantic.slots.name);

                }*/ else {
                    if (TextUtils.isEmpty(intentEntity.semantic.slots.name)) { //未指明联系人的呼叫
                        getTtsMessage(TtsConstant.PHONEC1CONDITION, mContext.getString(R.string.bt_make_dial), MSG_MAKE_DIAL_2ND);
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_without_name, TtsConstant.PHONEC1CONDITION, R.string.condition_phoneC1);
                    }else if(intentEntity.semantic.slots.name!=null&&!"".equals(intentEntity.semantic.slots.name)){
                        ttsfindNoContact();
                    }else
                        doExceptonAction(mContext);
                }
            }
        } else if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation)) {
            if ("REDIAL".equals(intentEntity.semantic.slots.insType)) { //重拨
                if(bluePhoneManager.isContactsSwitchOn()){
                    CallContact outgoingCall = bluePhoneManager.getLastOutgoingCall();
                    if (outgoingCall != null) {
                        LogUtils.d(TAG, "重拨 outgoingCall, name:" + outgoingCall.getName() + ",number:" + outgoingCall.getNumber());
                        String defaultTts = mContext.getString(R.string.bt_make_redial_confirm);
                        getRecallTtsMessage(TtsConstant.PHONEC38CONDITION, defaultTts, outgoingCall.getName(), outgoingCall.getNumber(), MSG_MAKE_REDIAL_2ND);
                    } else {
                        LogUtils.e(TAG, "outgoingCall == null");
                        Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, mContext.getString(R.string.bt_no_call_log));
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                }else {
                    Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, mContext.getString(R.string.bt_no_call_log));
                    myHandler.sendMessageDelayed(msg, 1000);
                }

            } else if ("CALLBACK".equals(intentEntity.semantic.slots.insType)) { //回拨
                if(bluePhoneManager.isContactsSwitchOn()){
                    CallContact incomingCall = bluePhoneManager.getLastIncomingCall();
                    if (incomingCall != null) {
                        LogUtils.d(TAG, "回拨 outgoingCall, name:" + incomingCall.getName() + ",number:" + incomingCall.getNumber());
                        String defaultTts = mContext.getString(R.string.bt_make_callback_confirm);
                        getRecallTtsMessage(TtsConstant.PHONEC39CONDITION, defaultTts, incomingCall.getName(), incomingCall.getNumber(), MSG_MAKE_CALLBACK_2ND);
                    } else {
                        LogUtils.e(TAG, "incomingCall == null");
                        Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, mContext.getString(R.string.bt_no_call_log));
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                }else {
                    Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, mContext.getString(R.string.bt_no_call_log));
                    myHandler.sendMessageDelayed(msg, 1000);
                }

            } else if ("OPEN".equals(intentEntity.semantic.slots.insType) || "missed".equals(intentEntity.semantic.slots.insType)) { //打开电话, 查看未接来电
                bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_DIALPAD);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC44CONDITION, mContext.getString(R.string.app_open_success), new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts);
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                });
                if ("OPEN".equals(intentEntity.semantic.slots.insType)) {
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_app, R.string.object_phone_app_1, TtsConstant.PHONEC44CONDITION, R.string.condition_phoneC44);
                }
            } else if ("EXIT".equals(intentEntity.semantic.slots.insType)) { //关闭电话,关闭通讯录
                AppControlManager.getInstance(mContext).closeApp("电话");
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC43CONDITION, mContext.getString(R.string.app_close_success), new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts);
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                });
                if ("EXIT".equals(intentEntity.semantic.slots.insType) && intentEntity.text.contains("电话")) {
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_app, R.string.object_phone_app_2, TtsConstant.PHONEC45CONDITION, R.string.condition_phoneC45);
                } else {
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_contact_ope, R.string.object_contact_ope_2, TtsConstant.PHONEC43CONDITION, R.string.condition_phoneC43);
                }
            } else if ("CONTACTS".equals(intentEntity.semantic.slots.insType) ) {//查看通话录
                bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_CONTACTS);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC42CONDITION, mContext.getString(R.string.app_open_success), new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts);
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                });
                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_contact_ope, R.string.object_contact_ope_1, TtsConstant.PHONEC42CONDITION, R.string.condition_phoneC42);
            } else if ("records".equals(intentEntity.semantic.slots.insType)){//查看通话记录
                bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_DIALPAD);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC42CONDITION, mContext.getString(R.string.app_open_success), new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts);
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                });
                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_contact_ope, R.string.object_contact_ope_1, TtsConstant.PHONEC42CONDITION, R.string.condition_phoneC42);
            } else if ("QUIT".equals(intentEntity.semantic.slots.insType)) { //取消拨打
                bluePhoneManager.cancelCall();
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC32CONDITION, mContext.getString(R.string.bt_cancel_call), new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Message msg = myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts);
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                });
                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_8, TtsConstant.PHONEC32CONDITION, R.string.condition_phoneC32);
            }else if("SILENT".equals(intentEntity.semantic.slots.insType)){
                // 静音
                try{
                    AppConfig.INSTANCE.mCarAudioManager.setMasterMute(true,0);
                    Log.d(TAG,"set mute...");
                }catch (CarNotConnectedException e){
                    Log.d(TAG,"failed to set mute...");
                }

                String conditionId = TtsConstant.SYSTEMC45CONDITION;
                String resText = mContext.getString(R.string.systemC45);
                String defaultText = resText;
                Utils.getTtsMessage(mContext, conditionId, defaultText, "", true,R.string.skill_phone, R.string.scene_sound, R.string.object_sound_mute, R.string.condition_system_default,false);
            } else
                doExceptonAction(mContext);
        }else if(PlatformConstant.Operation.POS_RANK.equals(intentEntity.operation)){
            //判空
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null||intentEntity.semantic.slots.posRank==null||intentEntity.semantic.slots.posRank.offset==null) {
                doExceptonAction(mContext);
                return;
            }
            srSelectItem(Integer.parseInt(intentEntity.semantic.slots.posRank.offset)-1);
        }else if(PlatformConstant.Operation.PAGE_RANK.equals(intentEntity.operation)){
            //判空
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null||intentEntity.semantic.slots.pageRank==null||intentEntity.semantic.slots.pageRank.offset==null) {
                doExceptonAction(mContext);
                return;
            }
            srSelectItem(slotToIndex(intentEntity.semantic.slots.pageRank));
        }else if(PlatformConstant.Operation.CANCEL.equals(intentEntity.operation)){  //不是他
            srSelectItem(16);
        }else if(PlatformConstant.Operation.CONFIRM.equals(intentEntity.operation)){  //确定
            MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
            mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_CONFIRM;
            mvwLParamEntity.nMvwId = 0;
            mvwAction(mvwLParamEntity);
        }/*else if (PlatformConstant.Operation.CANCEL.equals(intentEntity.operation)) { //取消
            Utils.exitVoiceAssistant();
            startTTS("已取消");
        }*/ else {  //其他异常
            doExceptonAction(mContext);
        }
    }

    private void srSelectItem(int nMvwId){
        //判断是否在选择场景
        try {
            if(TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_SELECT){
                //蓝牙电话界面
                IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
                if (iController != null) {
                    if (iController instanceof ContactController) {
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

    private void getRecallTtsMessage(String conditionId, final String defaultTts, final String name, final String number, final int what) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                if (TextUtils.isEmpty(tts)) {
                    tts = defaultTts;
                }
                if (TextUtils.isEmpty(name)) {
                    tts = Utils.replaceTts(tts, "#PHONE#", number);
                } else {
                    tts = Utils.replaceTts(tts, "#PHONE#", name);
                }
                if(TtsConstant.PHONEC39CONDITION.equals(conditionId))
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_ope, R.string.object_phone_ope_4, TtsConstant.PHONEC39CONDITION, R.string.condition_phoneC39,tts);
                else if(TtsConstant.PHONEC38CONDITION.equals(conditionId))
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_ope, R.string.object_phone_ope_3, TtsConstant.PHONEC38CONDITION, R.string.condition_phoneC38,tts);

                Message msg = myHandler.obtainMessage(what, tts);
                Bundle data = new Bundle();
                data.putSerializable(KEY_CALL, new ContactEntity(name, number));
                msg.setData(data);
                myHandler.sendMessageDelayed(msg, 1000);
            }
        });
    }

    private void getTtsMessage(String conditionId, final String defaultTts, final int what) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }
                Message msg = myHandler.obtainMessage(what, ttsText);
                myHandler.sendMessageDelayed(msg, 100);
            }
        });
    }


    private void startSelect(String name,List<ContactEntity> contactEntityList) {
        Log.e(TAG, "startSelect-"+name);
        try {
            String noActionName = name;
            String conditionId;
            String defaultText;
            String newChar;
            String oldChar;
            String newNum;

            //没有结果
            if (contactEntityList.size() <= 0) {
                    LogUtils.w(TAG, "contactEntityList.size()<=0");

                    //带名字
                    conditionId = TtsConstant.PHONEC5CONDITION;
                    defaultText = mContext.getString(R.string.bt_cannot_find_contact_name);
                    newChar = intentEntity.semantic.slots.name;
                    oldChar = "#CONTACT#";
                    String finalNewChar = mContext.getString(R.string.bt_cannot_find_contact_no_name);
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            if (TextUtils.isEmpty(newChar)) {
                                tts = tts.replace(oldChar, finalNewChar);
                            }else {
                                tts = tts.replace(oldChar, newChar);
                            }
                            Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC5CONDITION, R.string.condition_phoneC5,tts);
                            myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts), 1000);
                        }
                    });
                    //String mainMsg = String.format(mContext.getString(R.string.bt_cannot_find_contact_name), intentEntity.semantic.slots.name);
                    //myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_TTS, mainMsg), 1000);


                return;
            }

            //只有一个结果
            if (contactEntityList.size() == 1) {
                if (!TextUtils.isEmpty(intentEntity.semantic.slots.fuzzyPart)) {
                    conditionId = TtsConstant.PHONEC16CONDITION;
                    defaultText = mContext.getString(R.string.one_number_tail_search_result);
                    newChar = "<figure>"+intentEntity.semantic.slots.fuzzyPart+"</figure type=ordinal>";
                    oldChar = "#TAIL#";
                    newNum = "";
                    Log.e("zheng","zheng8888888"+intentEntity);
                    //mainMessage = String.format(mContext.getString(R.string.one_number_tail_search_result), intentEntity.semantic.slots.fuzzyPart);
                } else if (!TextUtils.isEmpty(intentEntity.semantic.slots.headNum)) {
                    conditionId = TtsConstant.PHONEC15CONDITION;
                    defaultText = mContext.getString(R.string.one_number_header_search_result);
                    newChar = intentEntity.semantic.slots.headNum;
                    oldChar = "#HEAD#";
                    newNum = "";
                    //mainMessage = String.format(mContext.getString(R.string.one_number_header_search_result), intentEntity.semantic.slots.headNum);
                } else {
                    Log.e("zheng","zheng1111");
                    conditionId = TtsConstant.PHONEC2CONDITION;
                    defaultText = mContext.getString(R.string.one_number_search_result);
                    newChar = contactEntityList.get(0).name;
                    oldChar = "#CONTACT#";
                    newNum = "";
                    //mainMessage = String.format(mContext.getString(R.string.one_number_search_result), contactEntityList.get(0).name);

                }
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        tts = tts.replace(oldChar, newChar);
                        Log.e("zheng", "zheng1111hujiao" + oldChar + newChar);
                        Log.e("zheng", "zheng1111hujiao tts" + tts);
                        Log.e("zheng", "zheng1111hujiao tts" + contactEntityList.get(0));
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC2CONDITION, R.string.condition_phoneC2,tts);
                        Message msg = myHandler.obtainMessage(MSG_MAKE_DIAL, tts);
                        Bundle data = new Bundle();
                        data.putSerializable(KEY_CALL, contactEntityList.get(0));
                        msg.setData(data);
                        myHandler.sendMessageDelayed(msg, 1000);
                    }
                });
                return;
            }

//            if(intentEntity!=null&&intentEntity.data!=null&&intentEntity.data.result!=null)
//                showContactActivity(intentEntity.data.result.toString(), GsonUtil.objectToString(intentEntity.semantic));
//            else
                showContactActivity(contactEntityList);

//            MVWAgent.getInstance().stopMVWSession();
//            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);

            HashSet<String> idSet = new HashSet<>();
            for (ContactEntity entity : contactEntityList) {
                idSet.add(entity.id+entity.hashCode());
            }
            LogUtils.d(TAG, "idSet size:" + idSet.size());

            if (!TextUtils.isEmpty(intentEntity.semantic.slots.fuzzyPart) && !TextUtils.isEmpty(intentEntity.semantic.slots.headNum)) {
                conditionId = TtsConstant.PHONEC18CONDITION;
                defaultText = mContext.getString(R.string.more_number_tail_search_result);
                newChar = intentEntity.semantic.slots.fuzzyPart;
                oldChar = "#TAIL#";
                newNum = "";
                //mainMessage = String.format(mContext.getString(R.string.more_number_tail_search_result), intentEntity.semantic.slots.fuzzyPart);
            } else if (!TextUtils.isEmpty(intentEntity.semantic.slots.headNum)) {
                conditionId = TtsConstant.PHONEC17CONDITION;
                defaultText = mContext.getString(R.string.more_number_header_search_result);
                newChar = intentEntity.semantic.slots.headNum;
                oldChar = "#HEAD#";
                newNum = "";
                //mainMessage = String.format(mContext.getString(R.string.more_number_header_search_result), intentEntity.semantic.slots.headNum);
            } else if (idSet.size() > 1) {
                if(intentEntity.answer!=null
                        &&intentEntity.answer.text!=null
                        &&(intentEntity.answer.text.contains("相似的联系人")
                        ||(intentEntity.answer.text.contains("还是")))
                        || (noActionName!=null&&!noActionName.isEmpty())    ){
                    conditionId = TtsConstant.PHONEC4CONDITION;
                    defaultText = mContext.getString(R.string.more_contact_search_result);
                    if(noActionName==null||noActionName.isEmpty())
                        newChar = intentEntity.semantic.slots.name;
                    else
                        newChar = noActionName;
                    newNum = idSet.size()+"";
                    oldChar = "#CONTACT#";
                } else {
                    conditionId = TtsConstant.PHONEC3CONDITION;
                    defaultText = mContext.getString(R.string.more_number_search_result);
                    newChar = contactEntityList.get(0).name;
                    oldChar = "#CONTACT#";
                    newNum = idSet.size()+"";
                }

                //mainMessage = String.format(mContext.getString(R.string.more_contact_search_result), intentEntity.semantic.slots.name);

            } else {
                conditionId = TtsConstant.PHONEC3CONDITION;
                defaultText = mContext.getString(R.string.more_number_search_result);
                newChar = contactEntityList.get(0).name;
                oldChar = "#CONTACT#";
                newNum = idSet.size()+"";
                //mainMessage = String.format(mContext.getString(R.string.more_number_search_result), contactEntityList.get(0).name);

            }
            Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    tts = tts.replace(oldChar, newChar);
                    if(newNum!=null&&!"".equals(newNum))
                        tts = tts.replace("#NUM#", newNum);
                    if(TtsConstant.PHONEC4CONDITION.equals(conditionId))
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC4CONDITION, R.string.condition_phoneC4,tts);
                    else if(TtsConstant.PHONEC3CONDITION.equals(conditionId))
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC3CONDITION, R.string.condition_phoneC3,tts);

                 /*    String contactTts = tts;
                      startTTSOnly(contactTts, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            //重新计算超时
                            SRAgent.getInstance().resetSrTimeCount();
                            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, contactTts);

                        }
                    });*/

                    Message msg = myHandler.obtainMessage(MSG_SEARCH_RESULT_SELECT, tts);
                    Bundle data = new Bundle();
                    String name = intentEntity.semantic.slots.name;
                    if(noActionName!=null&&!noActionName.isEmpty())
                        name = noActionName;
                    if(name == null || name.isEmpty())
                        name = contactEntityList.get(0).name;
                    data.putString(KEY_NAME, name);
                    data.putString(KEY_TYPE, TtsConstant.PHONEC4CONDITION.equals(conditionId)? "contacts" : "numbers");
                    msg.setData(data);
                    myHandler.sendMessageDelayed(msg, 1000);
                }
            });

        } catch (Exception e) {
            LogUtils.e(TAG, "e = " + e.toString());
        }

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        switch (mvwLParamEntity.nMvwScene) {
            case MvwSession.ISS_MVW_SCENE_GLOBAL:
                LogUtils.i(TAG, "TSP_SCENE_GLOBAL");
                break;
            case MvwSession.ISS_MVW_SCENE_SELECT:
                EventBusUtils.sendRestartSpeechTimeOut();
                selectItem(mvwLParamEntity);
                break;
            case MvwSession.ISS_MVW_SCENE_CONFIRM:
                EventBusUtils.sendRestartSpeechTimeOut();
                confirm(mvwLParamEntity);
                break;
        }

    }

    @Override
    public void stkAction(StkResultEntity o) {

    }

    @Override
    public void showContactActivity(String resultStr, String semantic) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semantic);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT);
        mContext.startActivity(intent);
    }


    public void showContactActivity(List<ContactEntity> contactEntityList) {
        AppConstant.mContactLists = contactEntityList;
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT);
        mContext.startActivity(intent);
    }

    @Override
    public void startDial(ContactEntity contactEntity) {
        bluePhoneManager.startDial(contactEntity);
    }

    @Override
    public void answerCall() {
        Utils.eventTrack(mContext,R.string.skill_phone,R.string.scene_dial, DatastatManager.primitive,R.string.object_answer,DatastatManager.response,TtsConstant.PARKINGC36_1CONDITION,R.string.condition_default,"",true);
        bluePhoneManager.answerCall();
        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_dial,R.string.object_answer,TtsConstant.MHXC9CONDITION,R.string.condition_null);
    }

    @Override
    public void rejectCall() {
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC37CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = mContext.getString(R.string.bt_make_rejectCall);
                }
                Utils.eventTrack(mContext,R.string.skill_phone,R.string.scene_dial, DatastatManager.primitive,R.string.object_recject,DatastatManager.response,TtsConstant.PARKINGC37CONDITION,R.string.condition_default,ttsText,true);

                BluePhoneManager.getInstance(mContext).removeTtsMsg();

                if(AudioFocusUtils.getInstance(mContext).requestVoiceAudioFocus(AudioManager.STREAM_ALARM)==AudioManager.AUDIOFOCUS_GAIN){
                    startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            bluePhoneManager.rejectCall();
                        }
                    });
                } else
                    bluePhoneManager.rejectCall();  //申请不到语音焦点，说明蓝牙电话在线

                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_dial,R.string.object_recject,TtsConstant.MHXC10CONDITION,R.string.condition_null,ttsText);
            }
        });

    }

    @Override
    public void exitVoiceAssistant() {
        Utils.exitVoiceAssistant();
    }

    public void redial(ContactEntity contactEntity) {
        String mainMessage = mContext.getString(R.string.bt_make_redial);
        getRecallTtsMessage(TtsConstant.PHONEC40CONDITION, mainMessage, contactEntity.name, contactEntity.phoneNumber, MSG_MAKE_REDIAL);
    }

    public void callback(ContactEntity contactEntity) {
        String mainMessage = mContext.getString(R.string.bt_make_callback);
        getRecallTtsMessage(TtsConstant.PHONEC40CONDITION, mainMessage, contactEntity.name, contactEntity.phoneNumber, MSG_MAKE_CALLBACK);
    }

    public void cancelDial() {
        getTtsMessage(TtsConstant.PHONEC41CONDITION, mContext.getString(R.string.bt_cancel_dial), MSG_TTS_WITH_EXIT);
//        Message msg = myHandler.obtainMessage(MSG_TTS, mContext.getString(R.string.bt_cancel_dial));
//        myHandler.sendMessageDelayed(msg, 1000);
    }

    public void requestContactData(String contactName) {
        LogUtils.d(TAG, " ContactController requestContactData:" + contactName);

        List<BtSearcherContact> callContacts = bluePhoneManager.requestContactData(contactName);
        LogUtils.d(TAG, " ContactController requestContactData:---" + callContacts);
        if (callContacts.size() > 0) {
            LogUtils.d(TAG, " ContactController callContacts        !=null:---" + callContacts);
            List<ContactEntity> contactEntityList = new ArrayList<>();
            ContactEntity entity;
            for (BtSearcherContact contact : callContacts) {
                if(contact.numbers!=null){
                    List<String > aNums = BtSearcherContact.getNumbers(contact.numbers);
                    if(aNums!=null&&!aNums.isEmpty()){
                        for (String number : aNums) {
                            entity = new ContactEntity(contact.name, number);
                            contactEntityList.add(entity);
                        }
                    }else {
                        entity = new ContactEntity(contact.name, contact.numbers);
                        contactEntityList.add(entity);
                    }

                }
            }
            startSelect(contactName,contactEntityList);
        } else {//二次交互的时候用户说的号码不存在电话本时
            if (isNumeric(contactName)) {//二次交互的时候用户说的是数字号码时，直接拨打该号码
                Log.e(TAG, "ContactController callContacts  数字联系人"+contactName);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC11CONDITION, mContext.getString(R.string.bt_make_dial_number), new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        tts = tts.replace("#PHONENUM#", contactName);
                        Message msg = myHandler.obtainMessage(MSG_MAKE_DIAL, tts);
                        Bundle data = new Bundle();
                        data.putSerializable(KEY_CALL, new ContactEntity(contactName, contactName));
                        msg.setData(data);
                        myHandler.sendMessageDelayed(msg, 1000);
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC11CONDITION, R.string.condition_phoneC11);
                    }
                });
            } else {//二次交互的时候用户说的名字不存在
                LogUtils.d(TAG, " ContactController callContacts == null:---" + contactName);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC5CONDITION, mContext.getString(R.string.bt_cannot_find_contact_name), new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        LogUtils.d(TAG, " ContactController---------------" + contactName);
                        tts = tts.replace("#CONTACT#", contactName);
                        myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts), 1000);
                    }
                });
            }
        }
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

    private static class MyHandler extends Handler {

        private final WeakReference<ContactController> contactControllerWeakReference;

        private MyHandler(ContactController contactController) {
            this.contactControllerWeakReference = new WeakReference<>(contactController);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            final ContactController contactController = contactControllerWeakReference.get();
            if (contactController == null) {
                LogUtils.d(TAG, "contactController == null");
                return;
            }
            final ContactEntity contactEntity;
            switch (msg.what) {
                case MSG_TTS:
                    contactController.startTTSOnly((String) msg.obj);
                    break;
                case MSG_SEARCH_RESULT_SELECT:
                    Bundle bundle = msg.getData();
                    Utils.checkNotNull(bundle, "bundle can't be null");
                    final String name = bundle.getString(KEY_NAME);
                    final String type = bundle.getString(KEY_TYPE);
                    contactController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            //重新计算超时
                            SRAgent.getInstance().resetSrTimeCount();
                            String conditionId;
                            String defaultText;
                            if ("contacts".equals(type)) {
                                //text = String.format("找到多个%s，请说第几个或取消", name);
                                conditionId = TtsConstant.PHONEC30CONDITION;
                                defaultText = contactController.mContext.getString(R.string.bt_timeout_more_contacts);

                            } else {
                                //text = String.format("%s有多个号码，请说第几个或取消", name);
                                conditionId = TtsConstant.PHONEC29CONDITION;
                                defaultText = contactController.mContext.getString(R.string.bt_timeout_more_numbers);

                            }
                            Utils.getMessageWithoutTtsSpeak(contactController.mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                                @Override
                                public void onConfirm(String tts) {
                                    tts = tts.replace("#CONTACT#", name);
                                    TimeoutManager.saveSrState(contactController.mContext, TimeoutManager.UNDERSTAND_ONCE, tts);
                                }
                            });
                        }
                    });
                    break;
                case MSG_MAKE_DIAL:
                    Log.e("zheng","zheng  MSG_MAKE_DIAL");
                    Start_tts = true;
                    bundle = msg.getData();
                    Utils.checkNotNull(bundle, "bundle can't be null");
                    contactEntity = (ContactEntity) bundle.getSerializable(KEY_CALL);
                    contactController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            Start_tts = false;
                            Log.e("zheng","zheng  MSG_MAKE_DIAL 111"+Start_tts);
                            contactController.startDial(contactEntity);

                            if(hasMessages(MSG_DELAY_EXIT)) //延迟 500ms 隐藏显示框，防止有短暂的媒体音
                                removeMessages(MSG_DELAY_EXIT);
                            sendEmptyMessageDelayed(MSG_DELAY_EXIT,500);
//                            Utils.exitVoiceAssistant();
                        }
                    });
                    break;
                case MSG_MAKE_DIAL_2ND:
                    contactController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            contactController.waitTelephoneMultiInterface(null);
                        }
                    });
                    break;
                case MSG_MAKE_REDIAL:
                    bundle = msg.getData();
                    Utils.checkNotNull(bundle, "bundle can't be null");
                    contactEntity = (ContactEntity) bundle.getSerializable(KEY_CALL);
                    contactController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            contactController.startDial(contactEntity);
                            if(hasMessages(MSG_DELAY_EXIT)) //延迟 500ms 隐藏显示框，防止有短暂的媒体音
                                removeMessages(MSG_DELAY_EXIT);
                            sendEmptyMessageDelayed(MSG_DELAY_EXIT,500);
                        }
                    });
                    break;
                case MSG_MAKE_REDIAL_2ND:
                    bundle = msg.getData();
                    Utils.checkNotNull(bundle, "bundle can't be null");
                    contactEntity = (ContactEntity) bundle.getSerializable(KEY_CALL);
                    contactController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            contactController.waitTelephoneMultiInterface(contactEntity);
                        }
                    });
                    break;
                case MSG_MAKE_CALLBACK:
                    bundle = msg.getData();
                    Utils.checkNotNull(bundle, "bundle can't be null");
                    contactEntity = (ContactEntity) bundle.getSerializable(KEY_CALL);
                    contactController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            contactController.startDial(contactEntity);
                            if(hasMessages(MSG_DELAY_EXIT)) //延迟 500ms 隐藏显示框，防止有短暂的媒体音
                                removeMessages(MSG_DELAY_EXIT);
                            sendEmptyMessageDelayed(MSG_DELAY_EXIT,500);
                        }
                    });
                    break;
                case MSG_MAKE_CALLBACK_2ND:
                    bundle = msg.getData();
                    Utils.checkNotNull(bundle, "bundle can't be null");
                    contactEntity = (ContactEntity) bundle.getSerializable(KEY_CALL);
                    contactController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            contactController.waitTelephoneMultiInterface(contactEntity);
                        }
                    });
                    break;
                case MSG_OPEN_BT:
                    contactController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            BluetoothAdapter.getDefaultAdapter().enable();
                            FloatViewManager.getInstance(contactController.mContext).hide();
                        }
                    });
                    break;
                case MSG_ERROR_CONTACT:
                    contactController.waitContactNameMultiInterface();
                    contactController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                        }
                    });
                    break;
                case MSG_TTS_WITH_EXIT:
                    contactController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            FloatViewManager.getInstance(contactController.mContext).hide();
                            Log.e(TAG,"联系人等原因未同步");
                        }
                    });
                    break;
                case MSG_DELAY_EXIT:
                    if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()){
                        FloatViewManager.getInstance(BaseApplication.getInstance()).hide(FloatViewManager.TYPE_HIDE_PHONE);
                    }
                        break;
            }

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

                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC32CONDITION, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        String ttsText = tts;
                        if (TextUtils.isEmpty(tts)) {
                            ttsText = mContext.getString(R.string.bt_cancel_call);
                        }
                        startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_8, TtsConstant.PHONEC32CONDITION, R.string.condition_phoneC32,mContext.getString(R.string.bt_cancel_call));
                                Utils.exitVoiceAssistant();
                            }
                        });
                    }
                });

                break;
        }
    }

    private void waitTelephoneMultiInterface(ContactEntity contactEntity) {
        LogUtils.d(TAG, "waitTelephoneMultiInterface with data:" + (contactEntity != null));
        if(contactEntity==null)
            MultiInterfaceUtils.getInstance(mContext).uploadBluePhoneStatusData(true, PlatformConstant.Service.TELEPHONE, "default");
        else
            MultiInterfaceUtils.getInstance(mContext).uploadBluePhoneStatusData(true, PlatformConstant.Service.TELEPHONE, "call");
        //保存二次交互语义
        if (contactEntity != null) {
            intentEntity.semantic.slots.name = contactEntity.name;
            intentEntity.semantic.slots.code = contactEntity.phoneNumber;
        }
        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
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

        IntentEntity intentEntity = new IntentEntity();
        intentEntity.service = PlatformConstant.Service.TELEPHONE;
        intentEntity.operation = PlatformConstant.Operation.DIAL;

        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
    }


    /**
     * 验证是否为数字
     */
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 打电话给妈妈、爸爸、哥哥、弟弟、妹妹、姐姐
     * @param string
     * @return
     */
    public boolean FamilyCall(String string) {
        if (string.contains("妈")) {
            FequestFamilyCall_MF(string,"妈","妈妈","老妈","妈咪","阿妈","俺妈");
            Log.e(TAG, "    text" + string);
            return true;
        } else if (string.contains("爸")) {
            FequestFamilyCall_MF(string,"爸","爸爸","老爸","阿爸","俺爸","老爹");
            //爸|爸爸|老爸|阿爸|俺爸
            Log.e(TAG, "     text" + intentEntity.semantic.slots.name);
            return true;
        } else if (string.contains("哥")) {
            //哥哥|大哥|老哥|哥|俺哥
            FequestFamilyCall(string,"哥","哥哥","大哥","老哥","俺哥","兄长");
            Log.e(TAG, "   text " + intentEntity.semantic.slots.name);
            return true;
        } else if (string.contains("弟")) {
            //弟弟|老弟|小弟|俺弟
            FequestFamilyCall(string,"弟","弟弟","小弟","老弟","俺弟","阿弟");
            Log.e(TAG, " text" + intentEntity.semantic.slots.name);
            return true;
        } else if (string.contains("姐")) {
            //姐姐|老姐|大姐|姐|俺姐
            FequestFamilyCall(string,"姐","姐姐","老姐","大姐","俺姐","姊姊");
            Log.e(TAG, "    text " + intentEntity.semantic.slots.name);
            return true;
        } else if (string.contains("妹")) {
            //妹妹`老妹`小妹`俺妹
            FequestFamilyCall(string,"妹","妹妹","小妹","老妹","妹子","俺妹");
            Log.e(TAG, "     text" + intentEntity.semantic.slots.name);
            return true;
        }
        return false;
    }


    public void FequestFamilyCall_MF(String string1, String string2, String string3, String string4, String string5, String string6, String string7) {
        Log.e(TAG, "   string1::::  " + string1);
        List<BtSearcherContact> contacts = BtPhoneSkillManager.getInstance().getContacts();
        boolean no_family_p = true;
        BtSearcherContact contact;
        for (int i = 0; i < contacts.size(); i++) {
            contact = contacts.get(i);
            if (!TextUtils.isEmpty(contact.numbers) && !"null".equals(contact.numbers)) {
                List<String> aNums = BtSearcherContact.getNumbers(contact.numbers);
                for (String number : aNums) {
                    if (string1.equals(contact.name)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                      //  requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.equals(string2)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                        // requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.contains(string3)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                        //  requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.contains(string4)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                        //   requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.contains(string5)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                        //   requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.contains(string6)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                        //   requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.contains(string7)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                        //   requestContactData(contact.name);
                        no_family_p = false;
                    }
                }
            }
        }
        if (no_family_p){
            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC5CONDITION, mContext.getString(R.string.bt_cannot_find_contact_name), new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    LogUtils.d(TAG, " ContactController---------------" + intentEntity.semantic.slots.alias_name);
                    if (TextUtils.isEmpty(intentEntity.semantic.slots.alias_name)){
                        tts = "抱歉，没找到联系人";
                    }else {
                        tts = tts.replace("#CONTACT#", intentEntity.semantic.slots.alias_name);
                    }
//                    myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts), 1000);
                    getTtsMessage(TtsConstant.PHONEC1CONDITION, tts, MSG_MAKE_DIAL_2ND);
                }
            });
        }
    }


    public void FequestFamilyCall(String string1, String string2, String string3, String string4, String string5, String string6, String string7) {
        Log.e(TAG, "   string1::::  " + string1);
        List<BtSearcherContact> contacts = BtPhoneSkillManager.getInstance().getContacts();
        boolean no_family_p = true;
        BtSearcherContact contact;
        for (int i = 0; i < contacts.size(); i++) {
            contact = contacts.get(i);
            if (!TextUtils.isEmpty(contact.numbers) && !"null".equals(contact.numbers)) {
                List<String> aNums = BtSearcherContact.getNumbers(contact.numbers);
                for (String number : aNums) {
//                    if (string1.contains(contact.name)) {
//
//                        RequestFamilyCall(contact.name,contact.number);
//                      //  requestContactData(contact.name);
//                        no_family_p = false;
//                    } else
                        if (contact.name.equals(string2)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                       // requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.equals(string3)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                     //  requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.equals(string4)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                     //   requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.equals(string5)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                     //   requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.equals(string6)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                     //   requestContactData(contact.name);
                        no_family_p = false;
                    } else if (contact.name.equals(string7)) {
                        RequestFamilyCall(contact.name,contact.firstNumber);
                     //   requestContactData(contact.name);
                        no_family_p = false;
                    }
                }
            }
        }
        if (no_family_p){
            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC5CONDITION, mContext.getString(R.string.bt_cannot_find_contact_name), new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    LogUtils.d(TAG, " ContactController---------------" + intentEntity.semantic.slots.alias_name);
                    if (TextUtils.isEmpty(intentEntity.semantic.slots.alias_name)){
                        tts = "抱歉，没找到联系人";
                    }else {
                        tts = tts.replace("#CONTACT#", intentEntity.semantic.slots.alias_name);
                    }
//                    myHandler.sendMessageDelayed(myHandler.obtainMessage(MSG_TTS_WITH_EXIT, tts), 1000);
                    getTtsMessage(TtsConstant.PHONEC1CONDITION, tts, MSG_MAKE_DIAL_2ND);
                }
            });
        }
    }

    public void RequestFamilyCall(String name, String phoneNumber){
        String conditionId = TtsConstant.PHONEC11CONDITION;
        String defaultText = mContext.getString(R.string.bt_make_dial_number);
        String oldChar = "#PHONENUM#";
        String newChar =  name;
        //String mainMsg = String.format(mContext.getString(R.string.bt_make_dial_number), intentEntity.semantic.slots.code);
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                tts = tts.replace(oldChar, newChar);
                Message msg = myHandler.obtainMessage(MSG_MAKE_DIAL, tts);
                Bundle data = new Bundle();
                data.putSerializable(KEY_CALL, new ContactEntity(name, phoneNumber));
                msg.setData(data);
                myHandler.sendMessageDelayed(msg, 1000);
                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC11CONDITION, R.string.condition_phoneC11);
            }
        });
    }


    private boolean dialRescueCall(){
        //拨打救援电话，就是打开一点通的逻辑
        if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.name!=null&&intentEntity.semantic.slots.name.contains("救援")){
            if (!bluePhoneManager.isBtConnected()) {
                bluePhoneManager.openBtPhoneTab(BluePhoneManager.TAB_SETTINGS);
                getTtsMessage(TtsConstant.PHONEC8CONDITION, mContext.getString(R.string.bt_no_connect), MSG_OPEN_BT);
                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC8CONDITION, R.string.condition_phoneC8);
            }else {

                String conditionId = TtsConstant.PHONEC2CONDITION;
                String defaultText = mContext.getString(R.string.one_number_search_result);
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        tts = tts.replace("#CONTACT#","救援");
                        startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                Start_tts = false;
                                AppControlManager.getInstance(BaseApplication.getInstance()).startApp("一点通");
                                Utils.exitVoiceAssistant();
                            }
                        });
                    }
                });


            }
            return true;
        }
        return false;
    }

    private void ttsfindNoContact(){
        Log.d(TAG, "ttsfindNoContact() called");
        String conditionId;
        String defaultText;
        String newChar;
        String oldChar;

        //带名字
        conditionId = TtsConstant.PHONEC5CONDITION;
        defaultText = mContext.getString(R.string.bt_cannot_find_contact_name);
        newChar = intentEntity.semantic.slots.name;
        oldChar = "#CONTACT#";
        String finalNewChar = mContext.getString(R.string.bt_cannot_find_contact_no_name);
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                if (TextUtils.isEmpty(newChar)) {
                    tts = tts.replace(oldChar, finalNewChar);
                }else {
                    tts = tts.replace(oldChar, newChar);
                }
                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC5CONDITION, R.string.condition_phoneC5,tts);
                startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                      Utils.exitVoiceAssistant();
                    }
                });
            }
        });



    }

}
