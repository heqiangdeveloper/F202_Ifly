package com.chinatsp.ifly.voice.platformadapter.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.service.DetectionService;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.phone.BtPhoneSkillManager;
import com.chinatsp.phone.adapter.BtRespondAdapter;
import com.chinatsp.phone.bean.BtSearcherContact;
import com.chinatsp.phone.bean.CallContact;
import com.chinatsp.phone.transact.BTPhoneManagerProxy;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.ISSErrors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BluePhoneManager {
    private static final String TAG = "xyj_"+BtRespondAdapter.TAG;
    public static final int TAB_DIALPAD = 0;
    public static final int TAB_CONTACTS = 1;
    public static final int TAB_SETTINGS = 2;

    public boolean Phone_Contact_State = false;

    private BluetoothAdapter mBluetoothAdapter;
    private static BluePhoneManager instance;
    private Context mContext;
    private List<BtSearcherContact> contactList;
    public boolean waitingSyncContact = false;
    private int callStatus = CallContact.CALL_STATE_TERMINATED;
    private int currentPage = -1;
    public boolean isIflySync = false;  //讯飞是否正在同步联系人
    private boolean mHasTts = false;//是否播报过
    private int current;

    private static final int MSG_TTS = 1;
    private static final int MSG_HIDE = 2;
    private static final int DELAY_TTS = 3*1000;
    private static final int DELAY_HIDE = 1*1000;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_TTS:
                    if(callStatus!=CallContact.CALL_STATE_INCOMING) return;
                    String tts = (String) msg.obj;
                    Utils.startTTS(tts, PriorityControler.PRIORITY_THREE,new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            Log.d(TAG, "lh: play complete");
                            Message msg = obtainMessage();
                            msg.what = MSG_TTS;
                            msg.obj = tts;
                            sendMessageDelayed(msg, DELAY_TTS);
                            //播报完成.
//                            BtPhoneSkillManager.getInstance().notifyIncomingTtsPlayFinish(true);
                        }
                    });
                    break;
                case MSG_HIDE:
                    Utils.exitVoiceAssistant();
                    break;
            }

        }
    };


    private BluePhoneManager(Context context) {
        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BtPhoneSkillManager.getInstance().register(mContext, "com.chinatsp.ifly",btRespondAdapter);
    }

    public void init() {
//        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BtPhoneSkillManager.getInstance().register(mContext, "com.chinatsp.ifly",btRespondAdapter);
        initialize();
    }

    public static BluePhoneManager getInstance(Context context) {
        if (instance == null) {
            instance = new BluePhoneManager(context.getApplicationContext());
        }
        return instance;
    }

    private void initialize() {
        boolean btEnabled = isBtEnabled();
        boolean btConnected = isBtConnected();
        boolean btdownLoading = BtPhoneSkillManager.getInstance().isContactsDownloading();
        LogUtils.d(TAG, "BtEnabled:" + btEnabled + " ,btConnected:" + btConnected+";.btdownLoading"+btdownLoading);
        if (btConnected) {
            if(btdownLoading){
                return;
            }
            contactList = getPartContacts();
            if (contactList != null && contactList.size() > 0) {
                Log.d(TAG, "initialize: contactList.size()::"+contactList.size());
                Phone_Contact_State = true;
                isIflySync = false;
                ThreadPoolUtils.executeSingle(new Runnable() {
                    @Override
                    public void run() {
                        uploadContactDict(contactList);
                    }
                });
//                uploadContactDict(contactList);
            }
        }
    }

    public boolean isBtEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isBtConnected() {
        return BtPhoneSkillManager.getInstance().getBtConnection() == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean isForeground() {
        return AppConstant.PACKAGE_NAME_PHONE.equals(ActivityManagerUtils.getInstance(mContext).getTopPackage());
    }

    public int getCallStatus() {
        return callStatus;
    }

    private BtRespondAdapter btRespondAdapter = new BtRespondAdapter() {

        @Override
        public void onNullBinding(ComponentName name) throws RemoteException {
            super.onNullBinding(name);
            BtPhoneSkillManager.getInstance().register(mContext, "com.chinatsp.ifly", this);
        }

        @Override
        public void onCallStateChanged(int state, final CallContact currentCall) throws RemoteException {
            LogUtils.d(TAG, "onCallStateChanged state=" + state+"...callStatus："+callStatus);
            switch (state) {
                case CallContact.CALL_STATE_DIALING://拨号中
                    if (state != callStatus) {
                        uploadPhoneStatus(true, "call");
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            if(mHandler.hasMessages(MSG_HIDE))
                                mHandler.removeCallbacksAndMessages(null);
                            mHandler.sendEmptyMessageDelayed(MSG_HIDE,DELAY_HIDE);
//                            FloatViewManager.getInstance(mContext).hide();//延迟消失，否则媒体会播放一段
                        }
                        if(current!=TspSceneAdapter.TSP_SCENE_CALL)
                            current = TspSceneAdapter.getTspScene(mContext);
                    }
                    break;
                case CallContact.CALL_STATE_INCOMING://来电
                    if (state != callStatus) {
                        uploadPhoneStatus(true, "call");
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                        if(current!=TspSceneAdapter.TSP_SCENE_CALL)
                            current = TspSceneAdapter.getTspScene(mContext);
                        MVWAgent.getInstance().stopMVWSession();
                        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_CALL);
                    }
                    break;
                case CallContact.CALL_STATE_ACTIVE://通话中
                    //在通话中禁用识别和唤醒
                    if (state != callStatus) {
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                        MVWAgent.getInstance().stopMVWSession();
                        if(current!=TspSceneAdapter.TSP_SCENE_CALL)
                            current = TspSceneAdapter.getTspScene(mContext);
                    }

                    removeTtsMsg();
                   /* if(mHasTts){
                           TTSController.getInstance(mContext).stopTTS();
                            AudioFocusUtils.getInstance(mContext).releaseVoiceAudioFocus();

                    }*/
                    mHasTts = false;
                    break;
                case CallContact.CALL_STATE_TERMINATED://结束通话
                    if (state != callStatus) {
                        callStatus = state;//先赋值，防止后面获取状态是仍然是来电状态
                        uploadPhoneStatus(isForeground(), "default");
                        restartScrene();
//                        MVWAgent.getInstance().stopMVWSession();
//                        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.getTspScene(mContext));  //恢复上一次的场景
                    }

                    removeTtsMsg();
                    if(mHasTts){
                        TTSController.getInstance(mContext).stopTTS();
                        AudioFocusUtils.getInstance(mContext).releaseVoiceAudioFocus();
                    }

                    mHasTts = false;
                    break;
            }
            callStatus = state;
        }

        @Override
        public void onContactsChanged(boolean isContactsFinish) throws RemoteException {
            LogUtils.d(TAG, "onContactsChanged isContactsFinish=" + isContactsFinish+".."+waitingSyncContact);
            LogUtils.d(TAG, "onContactsChanged isContactsFinish=" + BtPhoneSkillManager.getInstance().getContactsSize());

         /*   if(contactList!=null&&contactList.size()>0){
                Log.e(TAG, "onContactsChanged: the contactList not null::"+contactList.size());
                return;
            }*/

            isIflySync = false;

            if (isContactsFinish) {
                contactList = getPartContacts();;
                Phone_Contact_State = true;
                Log.d(TAG, "onContactsChanged() called with: 联系人同步完成isContactsFinish = [" + isContactsFinish + "]");
                ThreadPoolUtils.executeSingle(new Runnable() {
                    @Override
                    public void run() {
                        uploadContactDict(contactList);
                    }
                });
            }
        }

        @Override
        public void onBtConnectionChanged(boolean state) throws RemoteException { //蓝牙是否已连接回调
            LogUtils.d(TAG, "onBtConnectionChanged state=" + state);
            if (BTPhoneManagerProxy.getInstance().getConnectionState(null) == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onBtConnectionChanged() called with: state = [" + BTPhoneManagerProxy.getInstance().getConnectionState(null) + "]");
                Phone_Contact_State = false;
                isIflySync = false;
                waitingSyncContact = false;
                if (contactList != null) {
                    contactList.clear();
                }
                if(mHasTts){
                    TTSController.getInstance(mContext).stopTTS();
                    AudioFocusUtils.getInstance(mContext).releaseVoiceAudioFocus();
                }
                mHasTts = false;
                removeTtsMsg();
            }

        }

        @Override
        public void onBtPhoneItemChanged(int state) throws RemoteException {
            /**
             * public static final int DIAL_HIDE = 0;
             * public static final int DIAL_SHOW = 1;
             *
             * public static final int CONTACTS_HIDE = 2;
             * public static final int CONTACTS_SHOW = 3;
             *
             * public static final int BTSETTINGS_HIDE = 4;
             * public static final int BTSETTINGS_SHOW = 5;
             */
            LogUtils.d(TAG, "onBtPhoneItemChanged state=" + state);
            if (state % 2 == 0) { //page hide回调情况
                if (!isForeground()) {
                    currentPage = -1;
                    if (callStatus != CallContact.CALL_STATE_TERMINATED) {
                        uploadPhoneStatus(false, "call");
                    } else {
                        MultiInterfaceUtils.getInstance(mContext).uploadCmdDefaultData();
                    }
                }
            } else {//page show回调情况
                if (currentPage == -1) {
                    currentPage = state;
                    if (callStatus != CallContact.CALL_STATE_TERMINATED) {
                        uploadPhoneStatus(true, "call");
                    } else {
                        uploadPhoneStatus(true, "default");
                    }
                }
            }
        }

        @Override
        public void onPlayIncomingTts(String name, String number) throws RemoteException {
            super.onPlayIncomingTts(name, number);
           if(mHasTts){
               return;
           }
            mHasTts = true;
            uploadPhoneStatus(true, "call");
            LogUtils.d(TAG, "来电 incomingCall, name:" + name + ",number:" + number);
            String defaultText = mContext.getString(R.string.bt_incoming_warning);
            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.PHONEC36CONDITION, defaultText, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String replacePhone = name;
                    if (replacePhone==null||TextUtils.isEmpty(replacePhone)) {
                        replacePhone = number;
                    }
                    tts = Utils.replaceTts(tts, "#PHONE#", replacePhone);
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_ope, R.string.object_phone_ope_1, TtsConstant.PHONEC36CONDITION, R.string.condition_phoneC36,tts);

                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_TTS;
                    msg.obj = tts;
                    mHandler.sendMessage(msg);
                }
            });

        }
    };



    private List<BtSearcherContact> getPartContacts() {
        List targetContacts = new ArrayList();
        targetContacts.clear();
        final int partLength = 200;
        final int min = 1;
        final int total = BtPhoneSkillManager.getInstance().getContactsSize();
        final int part = total / partLength;
        Log.i(TAG, "part = " + part);
        if (part >= min) {
            for (int i = 1; i <= part; i++) {
                List<BtSearcherContact> contacts = BtPhoneSkillManager.getInstance().getContacts((i - 1) * partLength, i * partLength);
                if (isNonEmptyCollection(contacts)) {
                    Log.i(TAG, "getPartContacts :: splitCount >= min :: for in in in : contacts = " + contacts.size());
                    targetContacts.addAll(contacts);
                } else {
                    Log.i(TAG, "getPartContacts :: this i = " + i
                            + ":: getContacts(start, end) is empty.");
                }
            }
            List<BtSearcherContact> contacts = BtPhoneSkillManager.getInstance().getContacts(part * partLength, total);
            if (isNonEmptyCollection(contacts)) {
                Log.i(TAG, "getPartContacts :: splitCount >= min :: for out out out  : contacts = " + contacts.size());
                targetContacts.addAll(contacts);
            } else {
                Log.i(TAG, "getPartContacts :: splitCount >= min :: for out out out : is the last get "
                        + ":: getContacts(start, end) is empty.");
            }
        } else {
            List<BtSearcherContact> contacts = BtPhoneSkillManager.getInstance().getContacts();
            if (isNonEmptyCollection(contacts)) {
                Log.i(TAG, "getPartContacts : splitCount < min else : contacts = " + contacts.size());
                targetContacts.addAll(contacts);
            } else {
                Log.i(TAG, "getPartContacts :: splitCount < min :: getContacts is empty.");
            }
        }
        Log.d(TAG, "getPartContacts: targetContacts：："+targetContacts.size());
        return targetContacts;
    }

    private boolean isNonEmptyCollection(List<BtSearcherContact> contacts) {
        return contacts != null && !contacts.isEmpty();
    }

    private void uploadContactDict(List<BtSearcherContact> contacts) {
        JSONArray mArray = new JSONArray();
        BtSearcherContact contact;


        for (int i = 0; i < contacts.size(); i++) {
            try {
                contact = contacts.get(i);
                if (contact.numbers!=null&&!TextUtils.isEmpty(contact.numbers) && !"null".equals(contact.numbers)) {
                    List<String> aNums = BtSearcherContact.getNumbers(contact.numbers);
                    JSONObject mDict;
                    for (String number : aNums) {
                        mDict = new JSONObject();
                        mDict.put("id", String.valueOf(i));
                        mDict.put("name", contact.name);
                        mDict.put("phoneNumber", number);
                        mArray.put(mDict);
//                        Log.d(TAG, "uploadContactDict: "+contact.name);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "mArray.put: error");
            }
        }
        

//        Log.d(TAG, "uploadContactDict() called with: contacts = [" + num + "]");
        JSONObject result = new JSONObject();
        try {
            result.put("dictname", "contact");
            result.put("dictcontant", mArray);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "result.put: error");
        }
        JSONArray rA = new JSONArray();
        rA.put(result);
        JSONObject root = new JSONObject();
        try {
            root.put("grm", rA);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "root.put: error");
        }

//        Log.d(TAG,"uploadContactDict root:"+root.toString());
        if(!SRAgent.getInstance().init_state){
            Log.e(TAG, "uploadContactDict: the sr not inited!!");
            return;
        }

        if(contacts!=null&&contacts.size()==0){
            String grm = Utils.getFromAssets(mContext, "contack_null.json");
            Log.d(TAG, "uploadContactDict() called with: grm = [" + grm + "]");
            SRAgent.getInstance().uploadDict(grm);
            return;
        }

        SRAgent.getInstance().uploadDict(root.toString());
    }

    private void uploadPhoneStatus(boolean isFg, final String status) {
        LogUtils.d(TAG, "uploadPhoneStatus: " + status);
        MultiInterfaceUtils.getInstance(mContext).uploadBluePhoneStatusData(isFg, PlatformConstant.Service.TELEPHONE, status);
    }

    public void startDial(ContactEntity contactEntity) {
        LogUtils.d(TAG, "startDial:" + contactEntity.toString());
        BtPhoneSkillManager.getInstance().dial(contactEntity.phoneNumber);
    }

    public void answerCall() {
        LogUtils.d(TAG, "answerCall");
        Log.d(TAG, "-----------lh--answerCall-");
        BtPhoneSkillManager.getInstance().answerCall();
    }

    public void rejectCall() {
        LogUtils.d(TAG, "rejectCall");
        Log.d(TAG, "-----------lh--rejectCall-");
        BtPhoneSkillManager.getInstance().rejectCall();
    }

    public void cancelCall() {
        LogUtils.d(TAG, "cancelCall");
        BtPhoneSkillManager.getInstance().cancelCall();
    }

    public List<BtSearcherContact> requestContactData(String contactName) {
        if (contactList != null) {
            List<BtSearcherContact> filterContact = new ArrayList<>();
            for (BtSearcherContact contact : contactList) {
                if (contact.name.contains(contactName)) {
                    filterContact.add(contact);
                }
            }
            return filterContact;
        }
        return null;
    }

    public CallContact getLastOutgoingCall() {
        return BtPhoneSkillManager.getInstance().getLastOutgoingCall();
    }

    public CallContact getLastIncomingCall() {
        return BtPhoneSkillManager.getInstance().getLastIncomingCall();
    }

    public void openBtPhoneTab(int i) {
        switch (i) {
            case TAB_DIALPAD:
                BtPhoneSkillManager.getInstance().openBtPhoneDialpad();
                break;
            case TAB_CONTACTS:
                BtPhoneSkillManager.getInstance().openBtPhoneContacts();
                break;
            case TAB_SETTINGS:
                BtPhoneSkillManager.getInstance().openBtPhoneBtSettings();
                break;
        }
    }

    public boolean isContactsSwitchOn() {
        return BtPhoneSkillManager.getInstance().isContactsSwitchOn();
    }

    public boolean isContactsDownloading() {
        return BtPhoneSkillManager.getInstance().isContactsDownloading();
    }


    /**
     * 蓝牙断开连接时，传一个空数组给讯飞，替换原来的联系人列表
     */
    public void BTUnconnect(){
        contactList = BtPhoneSkillManager.getInstance().getContacts();
        Log.e(TAG,"BT is unconnect------contactList1:"+contactList.toString());
        String s = "{\"grm\":[{\"dictname\":\"contact\",\"dictcontant\":[]}]}";
        Log.e(TAG,"BT is unconnect------contactList1-----------:"+s);
        SRAgent.getInstance().uploadDict(s);
    }

    /**
     * 同步完联系人是否播报
     */
    public void setSyncContactState(){
        waitingSyncContact = true;
    }

    public void upLoadDictToCloudStatus(long wParam,String param) {
        Log.d(TAG, "upLoadDictToCloudStatus() called with: param = [" + param + "]");
//        isIflySync = false;
    }


    public void upLoadDictToLocalStatus(long wParam,String param) {
        Log.d(TAG, "upLoadDictToLocalStatus() called with: wParam = [" + wParam + "], param = [" + param + "]"+waitingSyncContact);
        if(wParam == ISSErrors.ISS_SUCCESS){
            if(param!=null&&param.contains("contact")){
                Log.d(TAG, "upLoadDictToLocalStatus: 本地联系人资源同步成功");
                Phone_Contact_State = true;
                isIflySync = true;
                if (waitingSyncContact) {
                    waitingSyncContact = false;
                    if(FloatViewManager.getInstance(mContext).isHide()&&!TTSController.getInstance(mContext).isTtsPlaying())
                        Utils.getMessageWithTtsSpeak(mContext, TtsConstant.PHONEC7CONDITION, mContext.getString(R.string.bt_contact_sync_completed));
                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_dial, R.string.object_phone_with_name, TtsConstant.PHONEC7CONDITION,  R.string.condition_phoneC7);
                }
            }
        } else if (wParam == ISSErrors.ISS_ERROR_INVALID_JSON_FMT) {
            Log.d(TAG, "输入的Json格式有问题\n");
        } else if (wParam == ISSErrors.ISS_ERROR_INVALID_JSON_INFO) {
            Log.d(TAG, "没有从Json输入中提取到必要的个性化数据\n");
        } else {
            if(param!=null&&param.contains("contact")){
                Log.d(TAG, "upLoadDictToLocalStatus: 联系人同步失败，再次同步");
                ThreadPoolUtils.executeSingle(new Runnable() {
                    @Override
                    public void run() {
                        List<BtSearcherContact>  contacts= getPartContacts();;
                        uploadContactDict(contacts);
                    }
                });
            }
        }

    }

    public void removeTtsMsg(){
        if(mHandler.hasMessages(MSG_TTS))
            mHandler.removeMessages(MSG_TTS);
    }

    private void restartScrene(){
        if(current!=TspSceneAdapter.TSP_SCENE_CALL)
           TspSceneManager.getInstance().resetScrene(mContext,current);
        else
            TspSceneManager.getInstance().resetScrene(mContext,-1);
        current = -1;
    }
}
