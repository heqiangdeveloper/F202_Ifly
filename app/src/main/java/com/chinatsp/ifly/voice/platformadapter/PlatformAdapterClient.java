package com.chinatsp.ifly.voice.platformadapter;

import android.car.hardware.constant.AVM;
import android.car.hardware.constant.DVR;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.automotive.vehicle.V2_0.VehicleDisplayForm;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.activeservice.ActiveController;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.entity.RegisterEvent;
import com.chinatsp.ifly.entity.SREvent;
import com.chinatsp.ifly.module.seachlist.SearchListFragment;
import com.chinatsp.ifly.remote.RemoteManager;
import com.chinatsp.ifly.service.DetectionService;
import com.chinatsp.ifly.service.SpeechRemoteService;
import com.chinatsp.ifly.source.SourceManager;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.IflyUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.MVW_WORDS;
import com.chinatsp.ifly.utils.OsUploadUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.AirController;
import com.chinatsp.ifly.voice.platformadapter.controller.AnswerController;
import com.chinatsp.ifly.voice.platformadapter.controller.AppController;
import com.chinatsp.ifly.voice.platformadapter.controller.CMDController;
import com.chinatsp.ifly.voice.platformadapter.controller.CarController;
import com.chinatsp.ifly.voice.platformadapter.controller.ChairController;
import com.chinatsp.ifly.voice.platformadapter.controller.ChangbaController;
import com.chinatsp.ifly.voice.platformadapter.controller.ChatController;
import com.chinatsp.ifly.voice.platformadapter.controller.ContactController;
import com.chinatsp.ifly.voice.platformadapter.controller.DrivingCareController;
import com.chinatsp.ifly.voice.platformadapter.controller.DrivingModeGuideController;
import com.chinatsp.ifly.voice.platformadapter.controller.ElseController;
import com.chinatsp.ifly.voice.platformadapter.controller.FeedBackController;
import com.chinatsp.ifly.voice.platformadapter.controller.FlightController;
import com.chinatsp.ifly.voice.platformadapter.controller.GmsController;
import com.chinatsp.ifly.voice.platformadapter.controller.KeyGuideController;
import com.chinatsp.ifly.voice.platformadapter.controller.KeyQueryController;
import com.chinatsp.ifly.voice.platformadapter.controller.MapController;
import com.chinatsp.ifly.voice.platformadapter.controller.MusicController;
import com.chinatsp.ifly.voice.platformadapter.controller.SpeechSetController;
import com.chinatsp.ifly.voice.platformadapter.controller.StockController;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.controller.TrainController;
import com.chinatsp.ifly.voice.platformadapter.controller.VehicleControl;
import com.chinatsp.ifly.voice.platformadapter.controller.VideoController;
import com.chinatsp.ifly.voice.platformadapter.controller.VoiceSettingController;
import com.chinatsp.ifly.voice.platformadapter.controller.WeatherController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.KeyWordsEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MultiSemantic;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.chinatsp.phone.bean.CallContact;
import com.example.loginarar.LoginManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iflytek.adapter.PlatformClientListener;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.controllerInterface.IController;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.ISSErrors;
import com.iflytek.speech.util.NetworkUtil;
import com.iflytek.sr.SrSession;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chinatsp.ifly.api.constantApi.TtsConstant.AVMCUSTOM;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SKYLIGHTC23CONDITION;
import static com.chinatsp.ifly.utils.Utils.startTTSOnly;

public class PlatformAdapterClient implements PlatformClientListener {

    private static String TAG = "xyj_PlatformAdapterClient";
    private Context mContext;
    private MapController mapController;
    private ContactController contactController;
    private WeatherController weatherController;
    private ChatController chatController;
    private CMDController cmdController;
    private AppController appController;
    private SpeechSetController speechSetController;
    private MusicController musicController;
    private CarController carController;
    private ChairController mChairController;
    private KeyGuideController mKeyGuideController;
    private IController<IntentEntity, MvwLParamEntity, StkResultEntity> curController;
    private MultiSemantic multiSemantic;
    private AirController airController;
    private SoftReference<List<String>> mvwWordsReference;
    private SoftReference<List<String>> naviWordsReference;
    private SoftReference<List<String>> selectWordsReference;
    private SoftReference<List<String>> gloableWordsReference;
//    private SoftReference<List<String>> ktvWordsReference;
    private SoftReference<List<String>> cctvWordsReference;
    private SoftReference<List<String>> imageWordsReference;
    private SoftReference<List<String>> callWordsReference;
    private KeyQueryController keyQueryController;
    private ISpeechControlService mSpeechService;
    private FlightController flightController;
    private TrainController trainController;
    private StockController stockController;
    private ActiveController activeController;
    private VoiceSettingController voiceSettingController;
//    private CheXinController cheXinController;
    private VideoController mVideoController;
    private FeedBackController mFeedBackController;
    private ChangbaController mChangbaController;
    private AnswerController mAnswerController;
    private VehicleControl mVehicleControl;
    private DrivingCareController mDrivingCareController;
    private DrivingModeGuideController mDrivingModeGuideController;
    private GmsController mGmsController;
    private RemoteManager mRemoteManager;
    private Handler mHandler;
    private DelayHandler mDelayHandler;
    private int WHAT_RECONGIZING = 100;
    private int MSG_SR_FAILED = 101;
    private int DELAY_SR_FAILED_ACTION = 5*1000;
    private int MSG_SHOW_ASSISTANT = 102;

    public PlatformAdapterClient(Context context) {
        EventBus.getDefault().register(this);
        this.mContext = context;
        this.mHandler = new Handler();
        mDelayHandler = new DelayHandler();
        mapController = new MapController(context);
        contactController = ContactController.getInstance(context);
        cmdController = new CMDController(context);
        chatController = new ChatController(context);
        flightController = new FlightController(context);
        trainController = new TrainController(context);
        stockController = new StockController(context);
        activeController = new ActiveController(context);
        mVideoController = VideoController.getInstance(mContext);
        musicController = MusicController.getInstance(context);
        carController = CarController.getInstance(context);
        mChangbaController =ChangbaController.getInstance(mContext);
        mKeyGuideController = KeyGuideController.getInstance(mContext);
        mChairController = ChairController.getInstance(mContext);
        mFeedBackController = FeedBackController.getInstance(mContext);
        mVehicleControl = VehicleControl.getInstance(mContext);
        mGmsController = GmsController.getInstance(mContext);
        mFeedBackController.setCarController(carController);
        mDrivingCareController = DrivingCareController.getInstance(mContext);
        mRemoteManager = RemoteManager.getInstance(mContext);
        Intent serviceIntent = new Intent(context, SpeechRemoteService.class);
        mContext.bindService(serviceIntent, mConn, Context.BIND_AUTO_CREATE);
        //??????????????????????????????????????????????????????
        initMvwWords();

    }

    @Subscribe
    public void onEvent(RegisterEvent event) {
        Log.d(TAG, "lh:onEvent:" + event.eventType);
        if (event.eventType) {
            if (keyQueryController == null) {
                keyQueryController = new KeyQueryController(mContext);
            }
            //keyQueryController.registerCallBackEvent();

            if(airController == null){
                airController = new AirController(mContext);
            }
            //airController.registerCallBackEvent();

            if(carController == null){
                carController = CarController.getInstance(mContext);
            }
            //carController.registerCallBackEvent();

            if(cmdController == null){
                cmdController = new CMDController(mContext);
            }
            //cmdController.registerCallBackEvent();

            if(mFeedBackController==null)
                mFeedBackController = FeedBackController.getInstance(mContext);
            mFeedBackController.setCarController(carController);

            if (mChairController == null) {
                mChairController = ChairController.getInstance(mContext);
            }

            if(mDrivingModeGuideController == null){
                mDrivingModeGuideController = DrivingModeGuideController.getInstance(mContext);
            }

            if(mChangbaController == null){
                mChangbaController = ChangbaController.getInstance(mContext);
            }

            if(mKeyGuideController == null){
                mKeyGuideController = KeyGuideController.getInstance(mContext);
            }

            if(mVehicleControl==null)
                mVehicleControl = VehicleControl.getInstance(mContext);

            if(mDrivingCareController==null)
                mDrivingCareController = DrivingCareController.getInstance(mContext);

            if(mGmsController==null)
                mGmsController = GmsController.getInstance(mContext);

            if (mRemoteManager == null) {
                mRemoteManager = RemoteManager.getInstance(mContext);
            }

        }

        CarUtils.getInstance(mContext);

    }

    @Subscribe
    public void onSRSesult(SREvent event) {
        Log.d(TAG, "onSRSesult() called with: event = [" + event.resultID + "]");
        if(event.resultID ==  ISSErrors.ISS_SUCCESS){
            if(mDelayHandler.hasMessages(MSG_SR_FAILED))
                mDelayHandler.removeMessages(MSG_SR_FAILED);
        }else  if(event.resultID ==  ISSErrors.ISS_ERROR_BUILDING_GRM){
            if(mDelayHandler.hasMessages(MSG_SR_FAILED))
                mDelayHandler.removeMessages(MSG_SR_FAILED);
            mDelayHandler.sendEmptyMessageDelayed(MSG_SR_FAILED,DELAY_SR_FAILED_ACTION);
        }

    }



    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected() called with: name = [" + name + "], service = [" + service + "]");
            mSpeechService = ISpeechControlService.Stub.asInterface(service);
            if(mFeedBackController!=null)
                mFeedBackController.setSpeechService(mSpeechService);
            if(mChairController!=null)
                mChairController.setSpeechService(mSpeechService);
            if(musicController!=null)
                musicController.setSpeechService(mSpeechService);
            if(mDrivingCareController!=null)
                mDrivingCareController.setSpeechService(mSpeechService);
            if(mGmsController!=null)
                mGmsController.setSpeechService(mSpeechService);
            if(mVideoController!=null)
                mVideoController.setSpeechService(mSpeechService);
            if(mRemoteManager!=null)
                mRemoteManager.setSpeechService(mSpeechService);
            FloatViewManager.getInstance(mContext).setSpeechService(mSpeechService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
            mSpeechService = null;
            if(mFeedBackController!=null)
                mFeedBackController.setSpeechService(mSpeechService);
            if(mChairController!=null)
                mChairController.setSpeechService(mSpeechService);
            if(musicController!=null)
                musicController.setSpeechService(mSpeechService);
            if(mDrivingCareController!=null)
                mDrivingCareController.setSpeechService(mSpeechService);
            if(mGmsController!=null)
                mGmsController.setSpeechService(mSpeechService);
            if(mVideoController!=null)
                mVideoController.setSpeechService(mSpeechService);
            if(mRemoteManager!=null)
                mRemoteManager.setSpeechService(mSpeechService);
            FloatViewManager.getInstance(mContext).setSpeechService(mSpeechService);
        }
    };

    private void initMvwWords() {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(Utils.getFromAssets(mContext, "mvw_other.json")).getAsJsonObject();
                List<KeyWordsEntity> mvwGlobalWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                jsonObject = parser.parse(Utils.getFromAssets(mContext, "mvw_navi.json")).getAsJsonObject();
                List<KeyWordsEntity> mvwNaviWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                jsonObject = parser.parse(Utils.getFromAssets(mContext, "mvw_select.json")).getAsJsonObject();
                List<KeyWordsEntity> mvwSelectWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                jsonObject = parser.parse(Utils.getFromAssets(mContext, "mvw_confirm.json")).getAsJsonObject();
                List<KeyWordsEntity> mvwConfirmWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                jsonObject = parser.parse(Utils.getFromAssets(mContext, "ktv.json")).getAsJsonObject();
//                List<KeyWordsEntity> mvwChangbaWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                jsonObject = parser.parse(Utils.getFromAssets(mContext, "cctv.json")).getAsJsonObject();
                List<KeyWordsEntity> mvwcctvWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                jsonObject = parser.parse(Utils.getFromAssets(mContext, "image.json")).getAsJsonObject();
                List<KeyWordsEntity> mvwImageWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                jsonObject = parser.parse(Utils.getFromAssets(mContext, "call.json")).getAsJsonObject();
                List<KeyWordsEntity> mvwCallWords = GsonUtil.stringToList(jsonObject.get("Keywords").toString(), KeyWordsEntity.class);

                List<KeyWordsEntity> mvwKeywordEntities = new ArrayList<>();
                mvwKeywordEntities.addAll(mvwGlobalWords);
                mvwKeywordEntities.addAll(mvwNaviWords);
                mvwKeywordEntities.addAll(mvwSelectWords);
                mvwKeywordEntities.addAll(mvwConfirmWords);
//                mvwKeywordEntities.addAll(mvwChangbaWords);
                mvwKeywordEntities.addAll(mvwcctvWords);
                mvwKeywordEntities.addAll(mvwImageWords);
                mvwKeywordEntities.addAll(mvwCallWords);

                List<String> mvwWords = new ArrayList<>();
                for (KeyWordsEntity entity : mvwKeywordEntities) {
                    mvwWords.add(entity.KeyWord);
                }
                mvwWordsReference = new SoftReference<>(mvwWords);

                List<String> naviWords = new ArrayList<>();
                for (KeyWordsEntity entity : mvwNaviWords) {
                    naviWords.add(entity.KeyWord);
                }
               /* for (KeyWordsEntity entity : mvwNaviExtWords) {
                    naviWords.add(entity.KeyWord);
                }*/
                naviWordsReference = new SoftReference<>(naviWords);

                List<String> selectWords = new ArrayList<>();
                for (KeyWordsEntity entity : mvwSelectWords) {
                    selectWords.add(entity.KeyWord);
                }
                for (KeyWordsEntity entity : mvwConfirmWords) {
                    selectWords.add(entity.KeyWord);
                }
                selectWordsReference = new SoftReference<>(selectWords);

                List<String> gloableWords = new ArrayList<>();
                for (KeyWordsEntity entity : mvwGlobalWords) {
                    gloableWords.add(entity.KeyWord);
                }
                gloableWordsReference = new SoftReference<>(gloableWords);

                List<String> ktvWords = new ArrayList<>();
              /*  for (KeyWordsEntity entity : mvwChangbaWords) {
                    ktvWords.add(entity.KeyWord);
                }
                ktvWordsReference = new SoftReference<>(ktvWords);*/

                List<String> cctvWords = new ArrayList<>();
                for (KeyWordsEntity entity : mvwcctvWords) {
                    cctvWords.add(entity.KeyWord);
                }
                cctvWordsReference= new SoftReference<>(cctvWords);

                List<String> imageWords = new ArrayList<>();
                for (KeyWordsEntity entity : mvwImageWords) {
                    imageWords.add(entity.KeyWord);
                }
                imageWordsReference = new SoftReference<>(imageWords);

                List<String> callWords = new ArrayList<>();
                for (KeyWordsEntity entity : mvwCallWords) {
                    callWords.add(entity.KeyWord);
                }
                callWordsReference = new SoftReference<>(callWords);

            }
        });
    }

    @Override
    public String onNLPResult(String var1) {
        LogUtils.d(TAG, "onNLPResult: " + var1);

        try {
            JSONObject jsonObject = new JSONObject(var1);
            String intentStr = jsonObject.getString("intent");
            IntentEntity intentEntity;
            try {
                intentEntity = GsonUtil.stringToObject(intentStr, IntentEntity.class);
            } catch (Exception e) {
                LogUtils.e(TAG, "fromJson exception: " + e.getMessage());
                String normalMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
                Utils.startTTSOnly(normalMsg, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                    }
                });
                return "";

            }
            LogUtils.d(TAG, "operation:" + intentEntity.operation+"..:"+intentEntity.service);
            LogUtils.d(TAG, "operation:sid::" + intentEntity.sid);
            EventBusUtils.sendTalkMessage(intentEntity.text);

//            SharedPreferencesUtils.saveString(mContext, AppConstant.SPEECH_IFLY_RESPONSE, intentStr);
//            SharedPreferencesUtils.saveString(mContext,AppConstant.SPEECH_IFLY_PRIMITIVE,intentEntity.text);

            DatastatManager.primitive = intentEntity.text;
            DatastatManager.response = intentStr;

            //??????????????????
            SRAgent.getInstance().resetSrTimeCount();
            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
            //if (intentEntity.rc == PlatformConstant.SUCCESS) {
            if(PlatformConstant.Service.CARCONTROL.equals(intentEntity.service) &&
                    intentEntity.semantic != null && intentEntity.semantic.slots != null && ((intentEntity.semantic.slots.mode != null &&
                    (intentEntity.semantic.slots.mode).contains("?????????")) || (intentEntity.semantic.slots.name != null &&
                    (intentEntity.semantic.slots.name).contains("?????????")))){
                Log.d(TAG, "intentEntity.service = " + intentEntity.service);
                intentEntity.setService(PlatformConstant.Service.AIRCONTROL);
            }
            if("vehicleInfo".equals(intentEntity.service) && "????????????".equals(intentEntity.text)) {
                intentEntity.service = PlatformConstant.Service.APP;
                intentEntity.operation = PlatformConstant.Operation.LAUNCH;
            }
            if (PlatformConstant.Service.MAP_U.equals(intentEntity.service)) { //??????
                if (multiSemantic != null) { //??????????????????
                    if (PlatformConstant.Service.MAP_U.equals(multiSemantic.service)
                            && PlatformConstant.Operation.OPEN.equals(multiSemantic.operation)) {
                        MultiInterfaceUtils.getInstance(mContext).uploadCmdDefaultData(); //??????????????????????????????
                    } else if (PlatformConstant.Service.MAP_U.equals(multiSemantic.service)
                            && PlatformConstant.Operation.USR_POI_SET.equals(multiSemantic.operation)) {
                        try {
                            String topic = multiSemantic.semantic.slots.endLoc.topic;
                            intentEntity.semantic.slots.endLoc.topic = topic;
                        } catch (Exception e) {}

                    }

                    //???????????????????????????????????????????????????????????????
                    /*SRAgent srAgent = SRAgent.getInstance();
                    if (srAgent.mSrArgu_Old != null && SrSession.ISS_SR_SCENE_STKS.equals(srAgent.mSrArgu_Old.scene)) {
                        srAgent.mSrArgu_New = new SrSessionArgu(srAgent.mSrArgu_Old);
                        srAgent.mSrArgu_Old = null;
                        srAgent.startSRSession();
                    }*/
                    multiSemantic = null; //????????????
                }
                curController = mapController;
                mapController.srAction(intentEntity);
            } else if (PlatformConstant.Service.TELEPHONE.equals(intentEntity.service)) { //?????????

                if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation) &&
                        (PlatformConstant.InsType.CONFIRM.equals(intentEntity.semantic.slots.insType)
                                || PlatformConstant.InsType.QUIT.equals(intentEntity.semantic.slots.insType))) {
                    if (multiSemantic != null) {
                        if (PlatformConstant.Service.TELEPHONE.equals(multiSemantic.service)
                                && PlatformConstant.Operation.INSTRUCTION.equals(multiSemantic.operation)) {

                            if ("REDIAL".equals(multiSemantic.semantic.slots.insType)) {
                                ContactEntity contact = new ContactEntity(multiSemantic.semantic.slots.name, multiSemantic.semantic.slots.code);
                                if (PlatformConstant.InsType.CONFIRM.equals(intentEntity.semantic.slots.insType)) {
                                    contactController.redial(contact);
                                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_ope, R.string.object_phone_ope_5, TtsConstant.PHONEC40CONDITION, R.string.condition_phoneC40);
                                } else if (PlatformConstant.InsType.QUIT.equals(intentEntity.semantic.slots.insType)) {
                                    contactController.cancelDial();
                                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_ope, R.string.object_phone_ope_6, TtsConstant.PHONEC41CONDITION, R.string.condition_phoneC41);
                                }
                            } else if ("CALLBACK".equals(multiSemantic.semantic.slots.insType)) {
                                ContactEntity contact = new ContactEntity(multiSemantic.semantic.slots.name, multiSemantic.semantic.slots.code);
                                if (PlatformConstant.InsType.CONFIRM.equals(intentEntity.semantic.slots.insType)) {
                                    contactController.callback(contact);
                                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_ope, R.string.object_phone_ope_5, TtsConstant.PHONEC40CONDITION, R.string.condition_phoneC40);
                                } else if (PlatformConstant.InsType.QUIT.equals(intentEntity.semantic.slots.insType)) {
                                    contactController.cancelDial();
                                    Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_ope, R.string.object_phone_ope_6, TtsConstant.PHONEC41CONDITION, R.string.condition_phoneC41);
                                }
                            }
                        }
                        multiSemantic = null;
                        return "";
                    }
                }
                curController = contactController;
                contactController.srAction(intentEntity);
            } else if (PlatformConstant.Service.WEATHER.equals(intentEntity.service)) { //??????
                if (weatherController == null) {
                    weatherController = new WeatherController(mContext);
                }
                curController = weatherController;
                weatherController.srAction(intentEntity);
            } else if (PlatformConstant.Service.PERSONALNAME.equals(intentEntity.service)) { // ?????????
                if (multiSemantic != null) { //??????????????????
                    if (PlatformConstant.Service.PERSONALNAME.equals(multiSemantic.service)
                            && PlatformConstant.Operation.SET.equals(multiSemantic.operation)
                            &&!"".equals(intentEntity.text)) {
                        if(speechSetController.isSetFail(intentEntity.text)){
                            return "";
                        }
                        speechSetController.changeName(intentEntity.text);
                        multiSemantic = null; //????????????
                        return "";
                    }
                    multiSemantic = null; //????????????
                }

                if (speechSetController == null) {
                    speechSetController = new SpeechSetController(mContext);
                }
                speechSetController.srAction(intentEntity);
            } else if (PlatformConstant.Service.RADIO.equals(intentEntity.service)
                    || PlatformConstant.Service.INTERNETRADIO.equals(intentEntity.service)) {
                Log.e(TAG,"zheng------"+intentEntity.service);
                if (mSpeechService != null) {
                    try {
                        if(SRAgent.mMusicPlaying){ //??????y??????????????????????????????????????????
                            mSpeechService.dispatchSRAction(Business.MUSIC, SourceManager.getInstance(mContext).changeSourceVoiceModel());
                        }

                        if (multiSemantic==null) {
                            mSpeechService.dispatchSRAction(Business.RADIO, intentEntity.convert2NlpVoiceModel());
                        }else {
                            mSpeechService.dispatchMutualAction(Business.RADIO, intentEntity.convert2MutualVoiceModel(multiSemantic));
                            multiSemantic = null; //????????????
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (PlatformConstant.Service.MUSIC.equals(intentEntity.service)) { //???????????????????????????????????????
//                if(("????????????".equals(intentEntity.text) || "????????????".equals(intentEntity.text)) &&
//                        (ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){
//                    ChangbaController.getInstance(mContext).handleMvwWords("????????????");
//                    return "";
//                }else if("??????".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords("??????");
//                    return "";
//                }else if(("?????????".equals(intentEntity.text) || "?????????".equals(intentEntity.text)) &&
//                        AppConstant.PACKAGE_NAME_CHANGBA.equals(ActivityManagerUtils.getInstance(mContext).getTopPackage()) &&
//                        (ChangbaController.getInstance(mContext).SONGLISTDETAILACTIVITY.equals(ChangbaController.getInstance(mContext).initActivity) ||
//                                ChangbaController.getInstance(mContext).PINYINCHOOSESONGACTIVITY.equals(ChangbaController.getInstance(mContext).initActivity))){
//                    MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
//                    mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_SELECT;
//                    if("?????????".equals(intentEntity.text)){
//                        mvwLParamEntity.nMvwId = 7;
//                    }else if("?????????".equals(intentEntity.text)){
//                        mvwLParamEntity.nMvwId = 6;
//                    }
//                    ChangbaController.getInstance(mContext).mvwAction(mvwLParamEntity);
//                    return "";
//                }
                    if (mSpeechService != null) {
                        try {
                            if(SRAgent.mRadioPlaying||SRAgent.mInRadioPlaying){ //?????????????????????
                                mSpeechService.dispatchSRAction(Business.RADIO, SourceManager.getInstance(mContext).changeSourceVoiceModel());
                            }

                            if (multiSemantic==null) {
                                mSpeechService.dispatchSRAction(Business.MUSIC, intentEntity.convert2NlpVoiceModel());
                            }else {
                                mSpeechService.dispatchMutualAction(Business.MUSIC, intentEntity.convert2MutualVoiceModel(multiSemantic));
                                multiSemantic = null; //????????????
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                }
//            } else if (PlatformConstant.Service.WEIXIN.equals(intentEntity.service)) { //???????????????????????????????????????
//                if (cheXinController == null) {
//                    cheXinController = new CheXinController(mContext,mSpeechService);
//                }
//                curController = cheXinController;
//                if (multiSemantic!=null){
//                    if (intentEntity.semantic.slots.contentType==null) {
//                        intentEntity.semantic.slots.contentType = multiSemantic.semantic.slots.contentType;
//                    }
//                    multiSemantic=null;
//                }
//                cheXinController.srAction(intentEntity);
            } else if (PlatformConstant.Service.JOKE.equals(intentEntity.service) || PlatformConstant.Service.NEWS.equals(intentEntity.service)) { //???????????????????????????????????????
                if (mSpeechService != null) {
                    try {
                        if(SRAgent.mMusicPlaying){ //??????y??????????????????????????????????????????
                            mSpeechService.dispatchSRAction(Business.MUSIC, SourceManager.getInstance(mContext).changeSourceVoiceModel());
                        }

                        mSpeechService.dispatchSRAction(Business.RADIO, intentEntity.convert2NlpVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            } else if (PlatformConstant.Service.BAIKE.equals(intentEntity.service)) {//??????
                if (chatController == null) {
                    chatController = new ChatController(mContext);
                }
                chatController.srAction(intentEntity);

            } else if (PlatformConstant.Service.HELP.equals(intentEntity.service)) {
                if (keyQueryController == null) {
                    keyQueryController = new KeyQueryController(mContext);
                }
                keyQueryController.srAction(intentEntity);
            } else if (PlatformConstant.Service.CMD.equals(intentEntity.service)) {
//                if("????????????".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords(intentEntity.text);
//                    return "";
//                }
                if (cmdController == null) {
                    cmdController = new CMDController(mContext);
                }
                cmdController.srAction(intentEntity);
            } else if (PlatformConstant.Service.APP.equals(intentEntity.service)) {
//                if(intentEntity.semantic != null && intentEntity.semantic.slots != null && "??????".equals(intentEntity.semantic.slots.name)
//                        && !PlatformConstant.Operation.EXIT.equals(intentEntity.operation)){
//                    if (mChangbaController == null)
//                        mChangbaController = ChangbaController.getInstance(mContext);
//                    curController = mChangbaController;
//                    mChangbaController.srAction(intentEntity);
//                }else {
                    if (appController == null) {
                        appController = new AppController(mContext,mSpeechService);
                    }
                    appController.srAction(intentEntity);
                //}
            } else if (PlatformConstant.Service.AIRCONTROL.equals(intentEntity.service)) {
                if (airController == null) {
                    airController = new AirController(mContext);
                }
                airController.srActionAir(intentEntity, var1);

            } else if (PlatformConstant.Service.CARCONTROL.equals(intentEntity.service)) {
                if (carController == null) {
                    carController = CarController.getInstance(mContext);
                }
//                if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation) &&
//                        (PlatformConstant.InsType.CONFIRM.equals(intentEntity.semantic.slots.insType)
//                                || PlatformConstant.InsType.QUIT.equals(intentEntity.semantic.slots.insType))) {
//                    if (multiSemantic != null) {
//                        if (PlatformConstant.InsType.CONFIRM.equals(intentEntity.semantic.slots.insType)) {
//                            carController.confirmInteractive(multiSemantic);
//                        } else if (PlatformConstant.InsType.QUIT.equals(intentEntity.semantic.slots.insType)) {
//                            carController.cancelInteractive(multiSemantic);
//                        }
//                        multiSemantic = null;
//                    }
//                }else {
//                    carController.srAction(intentEntity);
//                }
                carController.srActionCar(intentEntity,var1);
            } else if (PlatformConstant.Service.Flight.equals(intentEntity.service)) {
                if (flightController == null) {
                    flightController = new FlightController(mContext);
                }
                curController = flightController;
                flightController.srAction(intentEntity);
            } else if (PlatformConstant.Service.TRAIN.equals(intentEntity.service)) {
                if (trainController == null) {
                    trainController = new TrainController(mContext);
                }
                curController = trainController;
                trainController.srAction(intentEntity);
            } else if (PlatformConstant.Service.STOCK.equals(intentEntity.service)) {
                if (stockController == null) {
                    stockController = new StockController(mContext);
                }
                curController = stockController;
                stockController.srAction(intentEntity);
            }/* else if (PlatformConstant.Service.FEEDBACK.equals(intentEntity.service)){//????????????
                EventBusUtils.sendTalkMessage(intentEntity.text);
                gotofeedback();
            }*/ else if (PlatformConstant.Service.VIDEO.equals(intentEntity.service)){//????????????
                if (mVideoController == null) {
                    mVideoController = VideoController.getInstance(mContext);
                }
                mVideoController.srAction(intentEntity);
            }else if (PlatformConstant.Service.VIEWCMD.equals(intentEntity.service)){//????????????
                if (mFeedBackController == null)
                    mFeedBackController = FeedBackController.getInstance(mContext);

                if(mChairController.isMusicToPlay())
                    mChairController.srAction(intentEntity);
                else
                    mFeedBackController.srAction(intentEntity);
            }
            else if (PlatformConstant.Service.CHANGBA.equals(intentEntity.service)){//??????
//                if("??????".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords("??????");
//                    return "";
//                }
//                if (mChangbaController == null)
//                    mChangbaController = ChangbaController.getInstance(mContext);
//                curController = mChangbaController;
//                mChangbaController.srAction(intentEntity);
                doExceptonAction(mContext);
            }
            else if (PlatformConstant.Service.DATETIME.equals(intentEntity.service) ||
                    PlatformConstant.Service.POETRY.equals(intentEntity.service) ||
                    PlatformConstant.Service.CALC.equals(intentEntity.service)){//?????????????????????????????????
                if (mAnswerController == null)
                    mAnswerController = AnswerController.getInstance(mContext);
                mAnswerController.srAction(intentEntity);
            } else if ("????????????".equals(intentEntity.text)) {
                OsUploadUtils.getInstance(mContext).startLogApp(true);
            }else if ("????????????".equals(intentEntity.text)) {
                OsUploadUtils.getInstance(mContext).startLogApp(false);
            }
            else {
                ElseController.getInstance(mContext).srAction(intentEntity,var1 );
/*                String exitMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
                String normalMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
                if (!FloatViewManager.getInstance(mContext).isHide()) {
//                    onSrNoHandleTimeout(exitMsg, normalMsg);
                    timeoutAndExit("",normalMsg);
                }*/
            }


//            } else {
//                SRAgent.getInstance().setSrSession(true);
//                LogUtils.e(TAG, "INVALID_REQUEST rc code");
//            }

        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, e.toString());
            doExceptonAction(mContext);
        }
        return null;
    }

    @Override
    public String onDoAction(String var1) {
        LogUtils.d(TAG, "onDoAction: " + var1);
        try {
            JSONObject jsonObject = new JSONObject(var1);
            String intentStr = jsonObject.getString("intent");
            IntentEntity intentEntity;
            try {
                intentEntity = GsonUtil.stringToObject(intentStr, IntentEntity.class);
            } catch (Exception e) {
                LogUtils.e(TAG, "fromJson exception: " + e.getMessage());
                return "";
            }
            LogUtils.d(TAG, "onDoAction:" + intentEntity.text);
            LogUtils.d(TAG, "intelligentAnswer:" + intentEntity.intelligentAnswer);
            LogUtils.d(TAG, "intentEntity.rc:" + intentEntity.rc);
            LogUtils.d(TAG, "intentEntity.sid:" + intentEntity.sid);
//            SharedPreferencesUtils.saveString(mContext,AppConstant.SPEECH_IFLY_RESPONSE,var1);
//            SharedPreferencesUtils.saveString(mContext,AppConstant.SPEECH_IFLY_PRIMITIVE,intentEntity.text);

            DatastatManager.primitive = intentEntity.text;
            DatastatManager.response = var1;


//            if (intentEntity.text.equals("????????????") || intentEntity.text.equals("????????????")) {
//                EventBusUtils.sendTalkMessage(intentEntity.text);
//                gotofeedback();
//            } else
            Log.d(TAG,"TspSceneAdapter.getTspScene(mContext) = " + TspSceneAdapter.getTspScene(mContext));
            if("iFlytekQA".equals(intentEntity.service)||"chat".equals(intentEntity.service)){   //????????????????????????
                Log.d(TAG, "onDoAction: "+intentEntity.answer.text);
                if(intentEntity.answer!=null){
                    Log.d(TAG, "onDoAction: "+intentEntity.answer.question.question);
                    EventBusUtils.sendTalkMessage(intentEntity.answer.question.question);

                    if (multiSemantic != null) { //?????????????????????????????????????????????producFtName?????????
//                        EventBusUtils.sendTalkMessage(intentEntity.text);
                        LogUtils.d(TAG, "multiSemantic: " + multiSemantic.operation);
                        LogUtils.d(TAG, "semantic:" + GsonUtil.objectToString(multiSemantic.semantic));
                        if (PlatformConstant.Service.PERSONALNAME.equals(multiSemantic.service)
                                && PlatformConstant.Operation.SET.equals(multiSemantic.operation)) {
                            if(speechSetController.isSetFail(intentEntity.text)){
                                return "";
                            }
                            speechSetController.changeName(intentEntity.text);
                        } else if (PlatformConstant.Service.MAP_U.equals(multiSemantic.service)) {
                            //????????????/?????????????????????????????????????????????PoiName?????????
                            if (PlatformConstant.Operation.USR_POI_SET.equals(multiSemantic.operation)) {
                                String topic = multiSemantic.semantic.slots.endLoc.topic;
                                curController = mapController;
                                mapController.requestPoiData(intentEntity.text, topic);
                                //????????????????????????????????????????????????PoiName?????????
                            } else if (PlatformConstant.Operation.ALONG_SEARCH.equals(multiSemantic.operation)) {
                                curController = mapController;
                                String topic = "";
                                try {
                                    topic = intentEntity.semantic.slots.endLoc.topic;
                                } catch (Exception e) {}
                                mapController. alongTheWaySearch(intentEntity.text, topic);
                            }else if (PlatformConstant.Operation.CLOSE_MAP.equals(multiSemantic.operation)){
                                mapController.stopNaviMutual(intentEntity.text);
                            }else if (PlatformConstant.Operation.OPEN.equals(multiSemantic.operation)||PlatformConstant.Operation.QUERY.equals(multiSemantic.operation)){
                                curController = mapController;

                                String text = intentEntity.text;
                                String topic = "";
                                try {
                                    topic = intentEntity.semantic.slots.endLoc.topic;
                                } catch (Exception e) {}
                                if (text.startsWith("?????????")){
                                    text=text.replace("?????????","").trim();
                                    mapController.alongTheWaySearch(text, topic);
                                    return "";
                                }
                                mapController.searchAddress(text);
                            }else { //????????????
                                Utils.eventTrack(mContext, R.string.skill_chat, R.string.scene_chat, R.string.object_chat, TtsConstant.CHATC1CONDITION, R.string.condition_default,intentEntity.answer.text);
                                if(chatController!=null&&!FloatViewManager.getInstance(mContext).isHide())
                                    chatController.srAction(intentEntity.answer.text);
                            }
                        } else if (PlatformConstant.Service.TELEPHONE.equals(multiSemantic.service)) {
                            //?????????????????????????????????????????????????????????????????????
                            if (PlatformConstant.Operation.DIAL.equals(multiSemantic.operation)) {
                                curController = contactController;
                                contactController.requestContactData(intentEntity.text);
                            }
                            //????????????????????????????????????????????????
                            else if (PlatformConstant.Operation.INSTRUCTION.equals(multiSemantic.operation)
                                    && "REDIAL".equals(multiSemantic.semantic.slots.insType)) {
                                ContactEntity contact = new ContactEntity(multiSemantic.semantic.slots.name, multiSemantic.semantic.slots.code);
                                if (intentEntity.text.startsWith("??????")) {
                                    contactController.redial(contact);
                                } else if (intentEntity.text.startsWith("??????")) {
                                    contactController.cancelDial();
                                }else { //????????????
                                    Utils.eventTrack(mContext, R.string.skill_chat, R.string.scene_chat, R.string.object_chat, TtsConstant.CHATC1CONDITION, R.string.condition_default,intentEntity.answer.text);
                                    if(chatController!=null&&!FloatViewManager.getInstance(mContext).isHide())
                                        chatController.srAction(intentEntity.answer.text);
                                }
                            }
                            //????????????????????????????????????????????????
                            else if (PlatformConstant.Operation.INSTRUCTION.equals(multiSemantic.operation)
                                    && "CALLBACK".equals(multiSemantic.semantic.slots.insType)) {
                                ContactEntity contact = new ContactEntity(multiSemantic.semantic.slots.name, multiSemantic.semantic.slots.code);
                                if (intentEntity.text.startsWith("??????")) {
                                    contactController.callback(contact);
                                } else if (intentEntity.text.startsWith("??????")) {
                                    contactController.cancelDial();
                                }else { //????????????
                                    Utils.eventTrack(mContext, R.string.skill_chat, R.string.scene_chat, R.string.object_chat, TtsConstant.CHATC1CONDITION, R.string.condition_default,intentEntity.answer.text);
                                    if(chatController!=null&&!FloatViewManager.getInstance(mContext).isHide())
                                        chatController.srAction(intentEntity.answer.text);
                                }
                            }
                        }else if (PlatformConstant.Service.MUSIC.equals(multiSemantic.service)) {
                            if (mSpeechService != null) {
                                try {
                                    mSpeechService.dispatchMutualAction(Business.MUSIC, intentEntity.convert2MutualVoiceModel(multiSemantic));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (PlatformConstant.Service.RADIO.equals(multiSemantic.service)|| PlatformConstant.Service.INTERNETRADIO.equals(intentEntity.service)) {
                            if (mSpeechService != null) {
                                try {
                                    mSpeechService.dispatchMutualAction(Business.RADIO, intentEntity.convert2MutualVoiceModel(multiSemantic));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else if (PlatformConstant.Service.VIDEO.equals(multiSemantic.service)|| PlatformConstant.Operation.LAUNCH.equals(intentEntity.operation)) {
                            if (mSpeechService != null) {
                                try {
                                    mSpeechService.dispatchMutualAction(Business.CCTV, intentEntity.convert2MutualVoiceModel(multiSemantic));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
//                        else if (PlatformConstant.Service.CARCONTROL.equals(multiSemantic.operation)) {
//                            if (carController == null) return "";
//                            if (intentEntity.text.contains("??????")) {
//                                carController.confirmInteractive(multiSemantic);
//                            } else if (intentEntity.text.contains("??????")) {
//                                carController.cancelInteractive(multiSemantic);
//                            }
//                        }

                        //???????????????????????????????????????????????????????????????
                        SRAgent srAgent = SRAgent.getInstance();
                        if (srAgent.mSrArgu_Old != null && SrSession.ISS_SR_SCENE_STKS.equals(srAgent.mSrArgu_Old.scene)) {
                            srAgent.mSrArgu_New = new SrSessionArgu(srAgent.mSrArgu_Old);
                            srAgent.mSrArgu_Old = null;
                            srAgent.startSRSession();
                        }

                        multiSemantic = null; //????????????
                        //??????????????????
                        SRAgent.getInstance().resetSrTimeCount();
                        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE);
                        return "";
                    }else  if (isMvwWord(intentEntity.text)) {      //?????????????????????
                        if(isNaviSceneByText(intentEntity.text)) {
                            handleNaviWords(intentEntity);
                            return "";
                        } else if(isSelectSceneByText(intentEntity.text)) {
                           doHandleSelectAction(intentEntity);
                        }else if(isGloableWord(intentEntity.text)){
                            srActionToMvwAction(intentEntity.text);
                            return "";
                        }/*else if(isKtvWord(intentEntity.text)){
                            ChangbaController.getInstance(mContext).handleMvwWords(intentEntity.text);
                            return "";
                        }*//*else if(isCCTVWord(intentEntity.text)){
                            //TODO
                            return "";
                        }*/else if(isImageWord(intentEntity.text)){
                            doHandlerCustomMvws(intentEntity.text);
                            return "";
                        }else if(isCallWord(intentEntity.text)){
                            doHandlerCallMvws(intentEntity.text);
                            return "";
                        }

                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            SRAgent.getInstance().startSRSession();
                        }
                        LogUtils.e(TAG, "????????????SR??????: " + intentEntity.text);
                        return "";
                    } else if (isNaviWords(intentEntity.text)) {  //??????????????????????????????
                        handleNaviWords(intentEntity);
                        return "";
                    }else if(doHandleFeedBackAction(intentEntity)){
                        Log.e(TAG, "onDoAction: is feedback screne");
                        return "";
                    }
                    Log.d(TAG, "onDoAction: "+FeedBackController.getInstance(mContext).getShowType()+"..."+FeedBackController.getInstance(mContext).getUnderstandCound());

                    Utils.eventTrack(mContext, R.string.skill_chat, R.string.scene_chat, R.string.object_chat, TtsConstant.CHATC1CONDITION, R.string.condition_default,intentEntity.answer.text);
                    if(chatController!=null&&!FloatViewManager.getInstance(mContext).isHide())
                        chatController.srAction(intentEntity.answer.text);
                }

            } /*else if ((PlatformConstant.Service.WEATHER.equals(intentEntity.service) || PlatformConstant.Service.STOCK.equals(intentEntity.service) ||
                    PlatformConstant.Service.BAIKE.equals(intentEntity.service) ||  PlatformConstant.Service.NEWS.equals(intentEntity.service) ||
                    PlatformConstant.Service.Flight.equals(intentEntity.service) ||
                    PlatformConstant.Service.TRAIN.equals(intentEntity.service)) && !NetworkUtil.isNetworkAvailable(mContext)) {
                String mainMsg = mContext.getString(R.string.no_network_tip);
                Utils.startTTS(mainMsg);
                //todo ???????????????????????????????????????????????????
                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC19CONDITION, R.string.condition_mainC19);
            }*/ else {

                EventBusUtils.sendTalkMessage(intentEntity.text);

                if(AppConstant.PACKAGE_NAME_WECARNAVI.equals(ActivityManagerUtils.getInstance(mContext)
                        .getTopPackage())&&isPageByText(intentEntity.text)){  //???????????????????????? ?????????
                    String exitMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
                    String normalMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
                    if (!FloatViewManager.getInstance(mContext).isHide()) {
                        timeoutAndExit("", normalMsg);
                    }
                    return "";
                }else if(doHandleFeedBackAction(intentEntity)){
                    Log.e(TAG, "onDoAction: is feedback screne");
                    return "";
                }else if("??????".equals(intentEntity.text) || "????????????".equals(intentEntity.text)){
                    MvwLParamEntity entity = new MvwLParamEntity();
                    entity.nKeyword = "????????????";
                    dispathchMediaControl(entity);
                    return "";
                }else if("??????".equals(intentEntity.text) || "????????????".equals(intentEntity.text)){
                    MvwLParamEntity entity = new MvwLParamEntity();
                    entity.nKeyword = "????????????";
                    dispathchMediaControl(entity);
                    return "";
                }else if ("????????????".equals(intentEntity.text)
                       ||"????????????".equals(intentEntity.text)
                        ||"????????????".equals(intentEntity.text)
                        ||"????????????".equals(intentEntity.text)
                        ||"????????????".equals(intentEntity.text)){
                    String  lastFocus = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_AUDIO_FOCUS_PKGNAME, "");
                    if(AppConstant.PACKAGE_NAME_MUSIC.equals(lastFocus)){
                        try {
                            mSpeechService.dispatchSRAction(Business.MUSIC, MusicController.getInstance(mContext).controlModelByVoice(intentEntity.text));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return "";
                    }

                } else if(isSelectSceneByText(intentEntity.text)) {
                    doHandleSelectAction(intentEntity);
                }/*else if("????????????".equals(intentEntity.text) || "????????????".equals(intentEntity.text) || "????????????".equals(intentEntity.text) ||
                        "????????????".equals(intentEntity.text) || "????????????".equals(intentEntity.text) || "????????????".equals(intentEntity.text) ||
                        "??????".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords(intentEntity.text);
                    doExceptonAction(mContext);
                    return "";
                }*/else if(isImageWord(intentEntity.text)){  //dvr????????????
                    String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
                    if(DetectionService.PACKAGE_DVR.equals(topPackage)){ //????????????dvr??????
                        doHandlerCustomMvws(intentEntity.text);
                        return "";
                    }

                } else if(isNaviSceneByText(intentEntity.text)) {
                    handleNaviWords(intentEntity);
                    return "";
                }

                String text = mContext.getString(R.string.no_understand);
                if(intentEntity.rc==4){ //1057195 ????????????????????????????????????
                    if(doHandleFeedBackAction(intentEntity)){
                        Log.e(TAG, "onDoAction: is feedback screne");
                        return "";
                    }

                    if(!NetworkUtil.isNetworkAvailable(mContext))
                        text = mContext.getString(R.string.no_network_tip);  //?????????????????????????????????
//                    else if("1".equals(intentEntity.bislocalresult))
//                            text = mContext.getString(R.string.network_weka_tip);  //?????????????????????????????????
                    else{
                        timeoutAndRestartSR("",text);
                        return "";
                    }
                    timeoutAndExit("", text);
                    return "";

                }

                String exitMsg="??????????????????????????????????????????";
                String normalMsg= "????????????????????????????????????????????????";
                onSrNoHandleTimeout(exitMsg, normalMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, e.toString());
            doExceptonAction(mContext);
        }

        return "";
    }

    private void handleNaviWords(IntentEntity intentEntity) {
        if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_NAVI){
            mapController.onDoAction(intentEntity.text);
        }else {
            Utils.startTTS("?????????????????????????????????");
        }
    }

    private boolean isNaviWords(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        for (int i = 0; i < MVW_WORDS.navi.length; i++) {
            if (text.equals(MVW_WORDS.navi[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isFeedBackWords(String text){
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        if(FeedBackController.getInstance(mContext).isSureWord(text))
            return true;
        if(FeedBackController.getInstance(mContext).isDenyWord(text))
            return true;
        return false;
    }


    /**
     * ???????????????????????????????????????
     * @param text
     */
    private void srActionToMvwAction(String text) {
        Log.d(TAG, "srActionToMvwAction() called with: text = [" + text + "]");
        if(text==null||"".equals(text)){
            doExceptonAction(mContext);
            return;
        }
        MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
        if(text.equals("????????????")
                ||text.equals("????????????")
                ||text.equals("???????????????")
                ||text.equals("????????????")
                ||text.equals("????????????")
                ||text.equals("????????????")
                ||text.equals("???????????????")
                ||text.equals("????????????")
                ||text.equals("?????????")
                ||text.equals("?????????")
                ){
            cmdController.goToHome();
            return;
        }else if(text.equals("??????")){
            cmdController.exit();
            return;
        }else if(text.equals("??????")){
            cmdController.stopTTS();
            return;
        }else if(text.equals("????????????")||text.equals("??????")){
            mvwLParamEntity.nMvwId = 5;
            mvwLParamEntity.nKeyword = "????????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_stop, "", R.string.scene_interaction);
        }else if(text.equals("????????????")){
            mvwLParamEntity.nMvwId = 11;
            mvwLParamEntity.nKeyword = "????????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_stop, "", R.string.scene_interaction);
        }else if(text.equals("????????????")||text.equals("??????")){
            mvwLParamEntity.nMvwId = 12;
            mvwLParamEntity.nKeyword = "????????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_start, "", R.string.scene_interaction);
        }else if(text.equals("????????????")){
            mvwLParamEntity.nMvwId = 13;
            mvwLParamEntity.nKeyword = "????????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_start, "", R.string.scene_interaction);
        } else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 14;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 15;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 16;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 17;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 18;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 19;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 20;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 21;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 25;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("?????????")){
            mvwLParamEntity.nMvwId = 26;
            mvwLParamEntity.nKeyword = "?????????";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if (text.equals("??????")) {  //????????????????????????????????????????????????
            if (BluePhoneManager.getInstance(mContext).getCallStatus() == CallContact.CALL_STATE_INCOMING) {
                contactController.answerCall();
            }else if(!FloatViewManager.getInstance(mContext).isHide()){
                doExceptonAction(mContext);
            }
            return;
        } else if (text.equals("??????")) {//????????????????????????????????????????????????
            if (BluePhoneManager.getInstance(mContext).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                contactController.rejectCall();
            }else if(!FloatViewManager.getInstance(mContext).isHide()){
                doExceptonAction(mContext);
            }
            return;
        } else if (text.equals("???????????????")) {
            Semantic.SlotsBean slotsBean  = new Semantic.SlotsBean();
            slotsBean.insType = CMDController.VOLUME_PLUS;
            cmdController.changeMediaVolume(slotsBean,true);
            return;
        }else if (text.equals("???????????????")) {
            Semantic.SlotsBean slotsBean  = new Semantic.SlotsBean();
            slotsBean.insType = CMDController.VOLUME_MINUS;
            cmdController.changeMediaVolume(slotsBean,true);
            return;
        }else if (text.equals("??????????????????")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "??????????????????";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("?????????????????????")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "?????????????????????";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("????????????")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "????????????";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("??????????????????")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "??????????????????";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("????????????")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "????????????";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("??????????????????")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "??????????????????";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("??????????????????")) {
           doHandlerDvrMvws(text);
            return;
        }else if (text.equals("??????????????????")) {
            doHandlerDvrMvws(text);
            return;
        }else if (text.equals("????????????")) {
            //TODO
            return;
        }else if (text.equals("????????????")) {
            //TODO
            return;
        }else if (text.equals("????????????")) {
            //TODO
            return;
        }else if (text.equals("???????????????")) {
            mvwLParamEntity.nKeyword = "???????????????";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("???????????????")) {
            mvwLParamEntity.nKeyword = "???????????????";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("???????????????")) {
            mvwLParamEntity.nKeyword = "???????????????";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("???????????????")) {
            mvwLParamEntity.nKeyword = "???????????????";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("??????????????????")) {
            //TODO
            return;
        }else if (text.equals("??????????????????")) {
            //TODO
            return;
        }else if (text.equals("??????????????????")) {
            //TODO
            return;
        }else if (text.equals("??????????????????")) {
            //TODO
            return;
        }/*else if (text.equals("?????????????????????")) {
            //TODO
            return;
        }*/else {
            doExceptonAction(mContext);
            return;
        }
        dispathchMediaControl(mvwLParamEntity);
    }


    private boolean isNaviSceneByText(String text) {
        if (naviWordsReference != null && naviWordsReference.get() != null) {
            List<String> naviWords = naviWordsReference.get();
            return naviWords.contains(text);
        }
        return false;
    }

    private boolean isSelectSceneByText(String text) {
        if (selectWordsReference != null && selectWordsReference.get() != null) {
            List<String> selectWords = selectWordsReference.get();
            for (int i = selectWords.size() - 1; i >= 0; i--) {
                if(selectWords.get(i).contains(text))
                    return true;
            }
//            return selectWords.contains(text);
        }
        return false;
    }

    private boolean isMvwWord(String text) {
        if (mvwWordsReference != null && mvwWordsReference.get() != null) {
            List<String> mvwWords = mvwWordsReference.get();
            for (int i = mvwWords.size() - 1; i >= 0; i--) {
               if(mvwWords.get(i).contains(text))
                   return true;
            }
        } else {
            initMvwWords();
        }
        return false;
    }

    private boolean isGloableWord(String text){
        if (gloableWordsReference != null && gloableWordsReference.get() != null) {
            List<String> mvwWords = gloableWordsReference.get();
            for (int i = mvwWords.size() - 1; i >= 0; i--) {
                if(mvwWords.get(i).contains(text))
                    return true;
            }
//            return mvwWords.contains(text);
        } else {
            initMvwWords();
        }
        return false;
    }

    private boolean isCallWord(String text){
        if (callWordsReference != null && callWordsReference.get() != null) {
            List<String> mvwWords = callWordsReference.get();
            for (int i = mvwWords.size() - 1; i >= 0; i--) {
                if(mvwWords.get(i).contains(text))
                    return true;
            }
        } else {
            initMvwWords();
        }
        return false;
    }

    private boolean isImageWord(String text){
        if (imageWordsReference != null && imageWordsReference.get() != null) {
            List<String> mvwWords = imageWordsReference.get();
            for (int i = mvwWords.size() - 1; i >= 0; i--) {
                if(mvwWords.get(i).contains(text))
                    return true;
            }
//            return mvwWords.contains(text);
        } else {
            initMvwWords();
        }
        return false;
    }

    private boolean isCCTVWord(String text){
        if (cctvWordsReference != null && cctvWordsReference.get() != null) {
            List<String> mvwWords = cctvWordsReference.get();
            for (int i = mvwWords.size() - 1; i >= 0; i--) {
                if(mvwWords.get(i).contains(text))
                    return true;
            }
//            return mvwWords.contains(text);
        } else {
            initMvwWords();
        }
        return false;
    }

   /* private boolean isKtvWord(String text){
        if (ktvWordsReference != null && ktvWordsReference.get() != null) {
            List<String> mvwWords = ktvWordsReference.get();
            for (int i = mvwWords.size() - 1; i >= 0; i--) {
                if(mvwWords.get(i).contains(text))
                    return true;
            }
//            return mvwWords.contains(text);
        } else {
            initMvwWords();
        }
        return false;
    }*/

    private boolean isPageByText(String text){
        if(text!=null&&text.contains("?????????")
                ||(text!=null&&text.contains("?????????")))
            return true;
        return false;
    }

    @Override
    public void onSrNoHandleTimeout(String exitMsg, String normalMsg) {
        int srState = TimeoutManager.getSrState(mContext);
        Log.d(TAG, "onSrNoHandleTimeout() called with: exitMsg = [" + exitMsg + "], srState = [" + srState + "]");
        if (srState == TimeoutManager.NO_UNDERSTAND) { //??????????????????????????????
            Utils.startTTSOnly(exitMsg, new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    Utils.exitVoiceAssistant();
                }
            });
        } else {
            Utils.startTTS(normalMsg, new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    //?????????????????????
                    TimeoutManager.saveSrState(mContext, TimeoutManager.NO_UNDERSTAND);
                }
            });
        }
    }

    @Override
    public String onStkAction(String var1) {
        LogUtils.d(TAG, "onStkAction: " + var1);

        StkResultEntity stkResult = GsonUtil.stringToObject(var1, StkResultEntity.class);
        if (stkResult == null) {
            LogUtils.d(TAG, "stkResult == null");
            return null;
        }
        EventBusUtils.sendTalkMessage(stkResult.text);
//        SharedPreferencesUtils.saveString(mContext, AppConstant.SPEECH_IFLY_RESPONSE, var1);
//        SharedPreferencesUtils.saveString(mContext, AppConstant.SPEECH_IFLY_PRIMITIVE, stkResult.text);

        DatastatManager.primitive = stkResult.text;
        DatastatManager.response = var1;

        String topPkgname = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        Log.d(TAG, "onStkAction() called with: topPkgname = [" + topPkgname + "]");
        if (MXSdkManager.getInstance(mContext).isForeground()) {
            mapController.stkAction(stkResult);
        } else if (AppConstant.PACKAGE_NAME_RADIO.equals(topPkgname)) {
            if (mSpeechService != null) {
                try {
                    mSpeechService.dispatchStksAction(Business.RADIO, stkResult.convert2CmdVoiceModel());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (AppConstant.PACKAGE_NAME_MUSIC.equals(topPkgname)) {  //?????????????????????, ????????????????????????
            if (mSpeechService != null) {
                try {
                    mSpeechService.dispatchStksAction(Business.MUSIC, stkResult.convert2CmdVoiceModel());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (AppConfig.INSTANCE.settingFlag) { //??????????????????
            if (voiceSettingController == null) {
                voiceSettingController = new VoiceSettingController(mContext);
            }
            voiceSettingController.stkAction(stkResult);
        }

        return null;
    }

    @Override
    public void onSrTimeOut(int srTimeCount) { //???????????????
        LogUtils.d(TAG, "onSrTimeOut:" + srTimeCount+".."+TimeoutManager.getSrState(mContext));

        if (FloatViewManager.getInstance(mContext).isHide()) {
            LogUtils.d(TAG, "onSrTimeOut, floatView isHide=true");
            return;
        }

        EventBusUtils.sendTalkMessage(MessageEvent.ACTION_HIDE);
        EventBusUtils.sendDeputyMessage(MessageEvent.ACTION_HIDE);

        String text;
        String conditionId = "";
        int lastSrState = TimeoutManager.getSrState(mContext);
        if (lastSrState == TimeoutManager.ORIGINAL) { //???????????????
            if (srTimeCount == 1) {
                conditionId = TtsConstant.MAINC5CONDITION;
                text = mContext.getString(R.string.timeout_no_speak_1st);
                timeoutAndRestartSR(conditionId, text);
                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_1, TtsConstant.MAINC5CONDITION, R.string.condition_mainC5);
            } else if (srTimeCount == 2) {
                conditionId = TtsConstant.MAINC6CONDITION;
                text = mContext.getString(R.string.timeout_no_speak_2nd);
                timeoutAndRestartSR(conditionId, text);
                 Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_1, TtsConstant.MAINC6CONDITION, R.string.condition_mainC6);
            } else if (srTimeCount == 3) {
                conditionId = TtsConstant.MAINC7CONDITION;
                text = mContext.getString(R.string.timeout_and_exit);
                timeoutAndExit(conditionId, text);

                 Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_1, TtsConstant.MAINC7CONDITION, R.string.condition_mainC7);
            }
        } else if (lastSrState == TimeoutManager.NO_UNDERSTAND) {
            if (srTimeCount == 1) {
                conditionId = TtsConstant.MAINC8CONDITION;
                text = mContext.getString(R.string.timeout_no_understand_1st);
                timeoutAndRestartSR(conditionId, text);

                 Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_2, TtsConstant.MAINC8CONDITION, R.string.condition_mainC8);
            } else if (srTimeCount >= 2) {
                conditionId = TtsConstant.MAINC9CONDITION;
                text = mContext.getString(R.string.timeout_and_exit);
                timeoutAndExit(conditionId, text);

                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_2, TtsConstant.MAINC9CONDITION, R.string.condition_mainC9);
            }
        } else if (lastSrState == TimeoutManager.UNDERSTAND_ONCE) {
            text = TimeoutManager.getSrText(mContext);
            if (srTimeCount == 1) {
                if (TextUtils.isEmpty(text)) {
                    if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK){
                        if(FeedBackController.getInstance(mContext).getUnderstandCound()==1){
                            text = mContext.getString(R.string.search_list_select_timeout_2);
                            conditionId = TtsConstant.MAINC10_2CONDITION;
                            Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_4, TtsConstant.MAINC10_2CONDITION, R.string.condition_mainC10_2);
                            timeoutAndExit(conditionId, text);
                        }else {
                            Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_4, TtsConstant.MAINC10_1CONDITION, R.string.condition_mainC10_1);
                            FeedBackController.getInstance(mContext).setUnderstandCound();
                            timeoutAndRestartSR(TtsConstant.MAINC10_1CONDITION,mContext.getString(R.string.tire_no_unstanstand_one));
                        }
                    } else{
                        text = mContext.getString(R.string.timeout_and_exit);
                        conditionId = TtsConstant.MAINC10CONDITION;
                        Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_3, TtsConstant.MAINC10CONDITION, R.string.condition_mainC10);
                        timeoutAndExit(conditionId, text);
                    }
                } else {
                    TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, ""); //??????srText
                    if(SearchListFragment.isShown){
                        if(curController!=null&&curController==contactController){
                            if(text!=null&&text.contains("????????????"))
                                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_no_speak, TtsConstant.PHONEC29CONDITION, R.string.condition_phoneC29,text);
                            else
                                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_no_speak, TtsConstant.PHONEC30CONDITION, R.string.condition_phoneC30,text);
                        }else if(curController!=null&&curController==mapController){
                            Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_users_do_not_talk, TtsConstant.NAVIC36CONDITION, R.string.condition_navi36,text);
                        }
                        timeoutAndNotRestartSR("", text);//??????????????????????????????????????????????????????????????????
                    } else
                        timeoutAndRestartSR("", text);
                }

            } else if (srTimeCount >= 2) {
                text = mContext.getString(R.string.timeout_and_exit);
                if(SearchListFragment.isShown){
                    if(curController!=null&&curController==contactController){
                        Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_no_speak, TtsConstant.PHONEC31CONDITION, R.string.condition_phoneC31,text);
                    }else if(curController!=null&&curController==mapController){
                        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_users_do_not_talk, TtsConstant.NAVIC37CONDITION, R.string.condition_navi37,text);
                    }
                } else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK)
                    Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_4, TtsConstant.MAINC10_2CONDITION, R.string.condition_mainC10_2);
                else
                    Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_3, TtsConstant.MAINC10CONDITION, R.string.condition_mainC10);

                timeoutAndExit(conditionId, text);
            }
        }
    }


    public void onSelectSrTimeOut(int srTimeCount){
        if (FloatViewManager.getInstance(mContext).isHide()) {
            LogUtils.d(TAG, "onSrTimeOut, floatView isHide=true");
            return;
        }

        EventBusUtils.sendTalkMessage(MessageEvent.ACTION_HIDE);
        EventBusUtils.sendDeputyMessage(MessageEvent.ACTION_HIDE);

        String text;
        String conditionId = "";
        if (srTimeCount == 1) {
            text = TimeoutManager.getSrText(mContext);
            conditionId = TtsConstant.MAINC10CONDITION;

            if (TextUtils.isEmpty(text)) {
                text = mContext.getString(R.string.timeout_and_exit);
                timeoutAndExit(conditionId, text);
                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC10CONDITION, R.string.condition_mainC10);
            } else {
                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, ""); //??????srText
                timeoutAndRestartSR("", text);
            }

        } else if (srTimeCount >= 2) {
            text = mContext.getString(R.string.timeout_and_exit);
            timeoutAndExit(conditionId, text);
        }
    }

    private void timeoutAndRestartSR(String conditionId, String defaultText) {
        Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText, new TTSController.OnTtsStoppedListener(){
            @Override
            public void onPlayStopped() {
            }
        });

    }

    private void timeoutAndNotRestartSR(String conditionId, String defaultText) {
        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultText, new TTSController.OnTtsStoppedListener(){
            @Override
            public void onPlayStopped() {
            }
        });

    }

    private void gotofeedback() {
        Intent intent = new Intent();
        intent.setPackage("com.jidouauto.carletter");
        intent.setAction("changan.os.action.start.feedback.view");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("packName", mContext.getPackageName());
        mContext.sendBroadcast(intent);

        String conditionId;
        String defaultText;
        String userToken = LoginManager.getInstance().getUserToken();
        Log.d(TAG,"---------------lh----userToken:"+userToken);
        if (TextUtils.isEmpty(userToken)) {//?????????
            conditionId = TtsConstant.MAINC20CONDITION;
            defaultText = mContext.getString(R.string.user_no_login_guide);
            Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nologin, R.string.object_main_nologin, conditionId, R.string.condition_mainC20);
            Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultText, new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    FloatViewManager.getInstance(mContext).hide();
                }
            });
        }
//        else {
//            conditionId = TtsConstant.FEEDBACKC1CONDITION;
//            defaultText = mContext.getString(R.string.feedbackC1);
//            Utils.eventTrack(mContext, R.string.skill_tuchao, R.string.scene_start_tuchao, R.string.object_start_tuchao, conditionId, R.string.condition_default);
//        }

    }

    private void openRecognize() {
        Log.d(TAG,"lh:send feedback broadcast");
        Intent intent = new Intent();
        intent.setPackage("com.jidouauto.carletter");
        intent.setAction("changan.os.action.operate.feedback.motion");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("packName", mContext.getPackageName());
        intent.putExtra("start_feedback", true);
        mContext.sendBroadcast(intent);
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

    @Override
    public void onEngineException(final long wParam) {

        DatastatManager.primitive = "";
        DatastatManager.response = "";

        mHandler.removeMessages(WHAT_RECONGIZING);
        if (FloatViewManager.getInstance(mContext).isHide()) {
            LogUtils.e(TAG, "onEngineException, floatView isHide=true");
            if(!SRAgent.getInstance().SrInstance.isOneshot){
                LogUtils.d(TAG, "onEngineException, isOneshot=false");
                return;
            }
        }

        if(TTSController.getInstance(mContext).isTtsPlaying()) {
            LogUtils.e(TAG, "onEngineException, TtsPlaying ignore exception SR result");
            if(!FloatViewManager.getInstance(mContext).isHide()) {
                SRAgent.getInstance().startSRSession();
            }
            return;
        }

       if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK) {
           if (FeedBackController.getInstance(mContext).getUnderstandCound() == 0) {
               TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
               timeoutAndRestartSR(TtsConstant.MAINC10_1CONDITION, mContext.getString(R.string.tire_no_unstanstand_one));
               Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_4, TtsConstant.MAINC10_1CONDITION, R.string.condition_mainC10_1);
               FeedBackController.getInstance(mContext).setUnderstandCound();
               return;
           } else {
               onSrTimeOut(1);
               return;
           }
       }


        String exitMsg;
        String normalMsg;
        if (wParam == ISSErrors.ISS_ERROR_GET_RESULT_TIMEOUT) {
            normalMsg = "???????????????????????????????????????????????????";
            exitMsg = "??????????????????????????????????????????";
        } else if (wParam == ISSErrors.ISS_ERROR_NO_RESULT) {
            normalMsg = "???????????????????????????????????????????????????";
            exitMsg = "??????????????????????????????????????????";
        } else {
            normalMsg = "????????????????????????????????????????????????";
            exitMsg = "??????????????????????????????????????????";
        }
        Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_exception, R.string.object_main_exception_1, TtsConstant.MAINC13CONDITION, R.string.condition_mainC13);
        EventBusUtils.sendTalkMessage(MessageEvent.ACTION_HIDE);
        onSrNoHandleTimeout(exitMsg, normalMsg);
    }


    @Override
    public String onDoMvwAction(String mvwJson) {
        LogUtils.d(TAG, "onDoMvwAction:" + mvwJson);
        Log.e(TAG,"zheng ------..:"+SRAgent.getInstance().AppStatus);
        MvwLParamEntity mvwLParamEntity = GsonUtil.stringToObject(mvwJson, MvwLParamEntity.class);
        if (mvwLParamEntity == null) {
            LogUtils.d(TAG, "mvwLParamEntity == null");
            return null;
        }


        if(CarUtils.getInstance(mContext).isReverse()){
            LogUtils.d(TAG, "??????????????????, ignore");
            if(mvwLParamEntity.nMvwScene==MvwSession.ISS_MVW_SCENE_IMAGE){
                //???????????????????????????????????? ?????????????????????????????????  ????????????????????????????????????????????????????????????
            } else
                return null;
        }

        if(!IflyUtils.iflytekIsInited(mContext)){
            Log.e(TAG, "onDoMvwAction: the ifly not inited!");
            return null;
        }


        int active= Settings.Global.getInt(mContext.getContentResolver(),"hicarphone_active",0);//1???hicar??????????????????0????????????
        if(active==1){
            Log.e(TAG, "onDoMvwAction: hicar call "+active);
            return null;
        }


        /*if ("1".equals(Utils.getProperty("evs_disable_touch", "0"))){
            Log.e(TAG, "onDoMvwAction: ????????????" );
            return null;
        }*/

        if(mDelayHandler.hasMessages(MSG_SR_FAILED))
            mDelayHandler.removeMessages(MSG_SR_FAILED);//?????????????????????????????????????????????????????????????????????

        DatastatManager.primitive = mvwLParamEntity.nKeyword;
        DatastatManager.response = mvwJson;

        if (mvwLParamEntity.nMvwScene == MvwSession.ISS_MVW_SCENE_GLOBAL &&
                ((mvwLParamEntity.nMvwId >= 0 && mvwLParamEntity.nMvwId <= MVWAgent.MVM_COUNT) )
               /* ||((mvwLParamEntity.nMvwScene == MvwSession.ISS_MVW_SCENE_CUSTOME)
                &&(mvwLParamEntity.nMvwId >=0&&mvwLParamEntity.nMvwId <=4))*/
                ||((mvwLParamEntity.nMvwScene == MvwSession.ISS_MVW_SCENE_CUSTOME)
                &&(mvwLParamEntity.nMvwId ==0||mvwLParamEntity.nMvwId ==1))
                ) {



            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    int errid = SRAgent.getInstance().stopSRSession();
                    Log.d(TAG, "stopSRSession run() called::errid::"+errid);
                    if(errid==0)
                        SRAgent.getInstance().stopSrOnly();
                }
            });
            EventBusUtils.sendTalkMessage(MessageEvent.ACTION_HIDE);
        } else {
            Log.d(TAG, "onDoMvwAction() called with: nKeyword = [" + mvwLParamEntity.nKeyword + "]");
            EventBusUtils.sendTalkMessage(mvwLParamEntity.nKeyword);
            //???????????????????????????????????????????????????????????????
            if(!FloatViewManager.getInstance(mContext).isHide()){
                SRAgent.getInstance().stopSRRecord();
                SRAgent.getInstance().processRecogEnd();
                ThreadPoolUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        int errid = SRAgent.getInstance().stopSRSession();
                        Log.d(TAG, "isHide run() called::errid::"+errid);
                        if(errid==0)
                            SRAgent.getInstance().stopSrOnly();
                    }
                });
//
            }
        }

//        DatastatManager.getInstance().wakeup_event(mContext, "3", mvwLParamEntity.nKeyword);

        Log.d(TAG, "onDoMvwAction() called with: nMvwScene = [" + mvwLParamEntity.nMvwScene + "]"+mvwLParamEntity.nMvwId);
        Log.d(TAG, "onDoMvwAction: curController::"+curController);
        switch (mvwLParamEntity.nMvwScene) {
            case MvwSession.ISS_MVW_SCENE_GLOBAL:
                //????????????MXSdkManager
                if (mvwLParamEntity.nMvwId >=0&&mvwLParamEntity.nMvwId<=3) {
                    //????????????
                    cmdController.showAssistant(0);
                } else if (mvwLParamEntity.nMvwId == 4 || mvwLParamEntity.nMvwId == 5) {
                    //????????????
                    cmdController.showAssistant(1);
                }else if("????????????".equals(mvwLParamEntity.nKeyword)){
                    try {
                        mSpeechService.dispatchMvwAction(Business.HICAR, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case MvwSession.ISS_MVW_SCENE_SELECT:
                //??????
                if(AppConstant.PACKAGE_NAME_CHANGBA.equals(ActivityManagerUtils.getInstance(mContext).getTopPackage()) &&
                        (ChangbaController.getInstance(mContext).SONGLISTDETAILACTIVITY.equals(ChangbaController.getInstance(mContext).initActivity) ||
                                ChangbaController.getInstance(mContext).PINYINCHOOSESONGACTIVITY.equals(ChangbaController.getInstance(mContext).initActivity))){
                    curController = mChangbaController;
                }
                if (curController != null) {
                    curController.mvwAction(mvwLParamEntity);
                }
                break;
            case MvwSession.ISS_MVW_SCENE_CONFIRM:
                if(mvwLParamEntity.nMvwId == 2){  //??????????????????????????????
                    cmdController.exit();
                    break;
                }

                if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK){
                    if(mChairController!=null&&mChairController.isMusicToPlay()){
                        mChairController.mvwAction(mvwLParamEntity);
                        break;
                    } else if(mFeedBackController!=null){
                        mFeedBackController.mvwAction(mvwLParamEntity);
                        break;
                    }else {
                        doExceptonAction(mContext);
                        break;
                    }
                }else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DRIVING_GUIDE){
                    if(mDrivingModeGuideController != null){
                        mDrivingModeGuideController.mvwAction(mvwLParamEntity);
                    }
                }else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_WARN_SAVE_SLEEP){
                    if(mChairController != null){
                        mChairController.mvwAction(mvwLParamEntity);
                    }
                }else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_CHANGBA){
                    if(mChangbaController != null){
                        mChangbaController.mvwAction(mvwLParamEntity);
                    }
                }else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_VEHICLE){
                    if(mVehicleControl != null){
                        mVehicleControl.mvwAction(mvwLParamEntity);
                    }
                }else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DRIVING_CARE){
                    if(mDrivingCareController != null){
                        mDrivingCareController.mvwAction(mvwLParamEntity);
                    }
                } else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DIALOG){
                    if (mapController != null) {
                        mapController.mvwAction(mvwLParamEntity);
                    }
                }

                //?????????????????????????????????????????????????????????????????????????????????
                if(!ActiveServiceViewManager.ActiveServiceView_Show){
                    // ****** 0 ??? 1 ???????????? *************
                    if(mvwLParamEntity.nMvwId>=3&&mvwLParamEntity.nMvwId<=5){//?????? confirm ????????????????????????????????? ?????? ??? ??????
                        mvwLParamEntity.nMvwId = 1;
                    }else if(mvwLParamEntity.nMvwId>=6&&mvwLParamEntity.nMvwId<=10){//?????? confirm ????????????????????????????????? ?????? ??? ??????
                        mvwLParamEntity.nMvwId = 0;
                    }
                }


                //??????/??????
                if (curController != null) {
                    curController.mvwAction(mvwLParamEntity);
                }
                if (activeController !=null||ActiveServiceViewManager.ActiveServiceView_Show){
                    activeController.mvwAction(mvwLParamEntity);
                }else
                    doExceptonAction(mContext);

                break;
            case MvwSession.ISS_MVW_SCENE_ANSWER_CALL:
                if(isHiCarFg()){
                    Log.e(TAG, "onDoMvwAction: the hicar is fg");
                    return "";
                }

                if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_NAVI) {
                    curController = mapController;
                    mapController.mvwAction(mvwLParamEntity);
                } else {
                    LogUtils.d(TAG, "ISS_MVW_SCENE_ANSWER_CALL todo :" + mvwLParamEntity.nKeyword);
                }
                break;
            case MvwSession.ISS_MVW_SCENE_OTHER:
                if (mvwLParamEntity.nMvwId >= 0 && mvwLParamEntity.nMvwId <= 10) {
                    //????????????
                    Utils.eventTrack(mContext,R.string.scene_homepage,R.string.object_homepage,DatastatManager.primitive,R.string.object_homepage,DatastatManager.response,TtsConstant.SYSTEMC23CONDITION,R.string.condition_default,mContext.getString(R.string.msg_tts_isnull),true);
                    cmdController.goToHome();
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_global,R.string.scene_gohome,TtsConstant.MHXC2CONDITION,R.string.condition_null,mContext.getString(R.string.msg_tts_isnull));
                } else if (mvwLParamEntity.nMvwId == 34) {
                    //??????
                    cmdController.exit();
                } else if (mvwLParamEntity.nMvwId == 33) {
                    //??????
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_global,R.string.object_exit_voice,TtsConstant.MHXC22CONDITION,R.string.condition_null,"");
                    cmdController.stopTTS();
                    cmdController.exit();
                } else if (mvwLParamEntity.nMvwId >= 11
                        && mvwLParamEntity.nMvwId <= 22) { //?????????
                    dispathchMediaControl(mvwLParamEntity);
                }else if ("?????????".equals(mvwLParamEntity.nKeyword)
                ||"?????????".equals(mvwLParamEntity.nKeyword)) { //?????????
                    dispathchMediaControl(mvwLParamEntity);
                } else if (mvwLParamEntity.nMvwId == 25) { //??????
                    if (BluePhoneManager.getInstance(mContext).getCallStatus() == CallContact.CALL_STATE_INCOMING) {
                        contactController.answerCall();
                    }else if(!FloatViewManager.getInstance(mContext).isHide())
                        doExceptonAction(mContext);
                } else if (mvwLParamEntity.nMvwId == 26) { //??????
                    if (BluePhoneManager.getInstance(mContext).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                        contactController.rejectCall();
                    }else if(!FloatViewManager.getInstance(mContext).isHide())
                        doExceptonAction(mContext);
                }else if (mvwLParamEntity.nMvwId ==23) { //???????????????
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }

                    Semantic.SlotsBean slotsBean  = new Semantic.SlotsBean();
                    boolean isNaviBroadcast = SharedPreferencesUtils.getBoolean(mContext,AppConstant.KEY_NAVI_BROADCAST,false);
                    if(isNaviBroadcast) {
                        slotsBean.insType = CMDController.VOLUME_PLUS;
                        cmdController.changeMapVoiceMVW(slotsBean,true);
                    }else {
                        slotsBean.insType = CMDController.VOLUME_PLUS;
                        cmdController.changeMediaVolumeMVW(slotsBean,true);
                    }
                }else if (mvwLParamEntity.nMvwId ==24) { //???????????????
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                    Semantic.SlotsBean slotsBean  = new Semantic.SlotsBean();
                    boolean isNaviBroadcast = SharedPreferencesUtils.getBoolean(mContext,AppConstant.KEY_NAVI_BROADCAST,false);
                    if(isNaviBroadcast) {
                        slotsBean.insType = CMDController.VOLUME_MINUS;
                        cmdController.changeMapVoiceMVW(slotsBean,true);
                    }else {
                        slotsBean.insType = CMDController.VOLUME_MINUS;
                        cmdController.changeMediaVolumeMVW(slotsBean,true);
                    }
                }else if ("??????????????????".equals(mvwLParamEntity.nKeyword)) {
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("?????????????????????".equals(mvwLParamEntity.nKeyword)) {
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("????????????".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("??????????????????".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("????????????".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("??????????????????".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("??????????????????".equals(mvwLParamEntity.nKeyword)) {
                     doHandlerDvrMvws(mvwLParamEntity.nKeyword);
                }else if ("??????????????????".equals(mvwLParamEntity.nKeyword)) {
                    doHandlerDvrMvws(mvwLParamEntity.nKeyword);
                }else if ("????????????".equals(mvwLParamEntity.nKeyword)) {
                    OsUploadUtils.getInstance(mContext).captureOrRecordScrenn(1,0);
                }else if ("????????????".equals(mvwLParamEntity.nKeyword)) {
                    OsUploadUtils.getInstance(mContext).captureOrRecordScrenn(2,0);
                }else if ("????????????".equals(mvwLParamEntity.nKeyword)) {
                    OsUploadUtils.getInstance(mContext).stopRecordScreen();
                }else if ("???????????????".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("???????????????".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("???????????????".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("???????????????".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("??????????????????".equals(mvwLParamEntity.nKeyword)) {
                    doExceptonAction(mContext);
                }else if ("?????????????????????".equals(mvwLParamEntity.nKeyword)) {
                    doExceptonAction(mContext);
                }else if("??????????????????".equals(mvwLParamEntity.nKeyword)){
                    IntentEntity intentEntity = new IntentEntity();
                    intentEntity.semantic = new Semantic();
                    intentEntity.semantic.slots = new Semantic.SlotsBean();
                    intentEntity.semantic.slots.modeValue = ChairController.MODE_SLEEP;
                    intentEntity.semantic.slots.mode = ChairController.MODE;
                    intentEntity.text = "??????????????????";
                    ChairController.getInstance(mContext).DoMVWAction(intentEntity);
                }else if("??????????????????".equals(mvwLParamEntity.nKeyword)){
                    IntentEntity intentEntity = new IntentEntity();
                    intentEntity.semantic = new Semantic();
                    intentEntity.semantic.slots = new Semantic.SlotsBean();
                    intentEntity.semantic.slots.mode = CarController.SMOKING;
                    intentEntity.semantic.slots.name = CarController.SMOKING;
                    intentEntity.semantic.slots.action = "";
                    intentEntity.text = "??????????????????";
                    CarController.getInstance(mContext).srActionCar(intentEntity,"");
                }else if("??????????????????".equals(mvwLParamEntity.nKeyword)){
//                    IntentEntity intentEntity = new IntentEntity();
//                    intentEntity.semantic = null;
//                    intentEntity.text = "????????????";
//                    ChangbaController.getInstance(mContext).handleOneShot(intentEntity);
                    doExceptonAction(mContext);
                }
                break;
            case MvwSession.ISS_MVW_SCENE_CUSTOME:
                Log.d(TAG, "onDoMvwAction() called with: mvwJson = [" + mvwLParamEntity.nKeyword + "]");
                if(mvwLParamEntity.nKeyword.contains("??????")){  //?????????????????????
                    cmdController.showAssistant(2);//?????????????????? 8 ??? 9
                    DatastatManager.getInstance().wakeup_event(mContext, "????????????", mvwLParamEntity.nKeyword);
                }
                break;
            case MvwSession.ISS_MVW_SCENE_KTV:
//            case MvwSession.ISS_MVW_SCENE_CCTV:
            case MvwSession.ISS_MVW_SCENE_IMAGE:
                doHandlerCustomMvws(mvwLParamEntity.nKeyword);
                break;
            case MvwSession.ISS_MVW_SCENE_CALL:
                doHandlerCallMvws(mvwLParamEntity.nKeyword);
                break;
            case MvwSession.ISS_MVW_SCENE_CCTV:
                doHandlerCctvMvws(mvwLParamEntity);
                break;
        }

        if (mvwLParamEntity.nMvwScene == MvwSession.ISS_MVW_SCENE_GLOBAL &&
                (mvwLParamEntity.nMvwId >= 0 && mvwLParamEntity.nMvwId <= MVWAgent.MVM_COUNT)
           || mvwLParamEntity.nMvwScene == MvwSession.ISS_MVW_SCENE_CUSTOME &&
                (mvwLParamEntity.nKeyword!=null&&mvwLParamEntity.nKeyword.contains("??????"))) {

        } else {
            SRAgent.getInstance().resetSrTimeCount();
            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE);
        }

        return null;
    }




    @Override
    public IController getCurController() {
        return curController;
    }

    @Override
    public void onSpeechStart() {
        EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_LISTENING, null);
    }

    @Override
    public void onSpeechEnd() {
        EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_LISTENING, null);
    }

    @Override
    public void onRecognizeStart() {
        EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_RECOGNIZING_1, new AnimationImageView.OnFrameAnimationListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: onRecognizeStart");
                mHandler.sendEmptyMessageDelayed(WHAT_RECONGIZING, 3000);
            }

            @Override
            public void onEnd() {
                Log.d(TAG, "onEnd: onRecognizeStart:"+mHandler.hasMessages(WHAT_RECONGIZING));
                if(mHandler.hasMessages(WHAT_RECONGIZING)) {
                    EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_RECOGNIZING_2, null);
                }
            }
        });
        EventBusUtils.sendDeputyMessage(MessageEvent.ACTION_HIDE);
//        EventBusUtils.sendTalkMessage(MessageEvent.ACTION_HIDE);
    }

    @Override
    public void onRecognizeEnd() {
        EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_RECOGNIZING_3, new AnimationImageView.OnFrameAnimationListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "onStart: onRecognizeEnd");
                mHandler.removeMessages(WHAT_RECONGIZING);
            }

            @Override
            public void onEnd() {
                Log.d(TAG, "onEnd: onRecognizeEnd");
                mHandler.removeMessages(WHAT_RECONGIZING);
                EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_NORMAL, null);
            }
        });
    }

    @Override
    public void onRestoreMultiSemantic() {
        Log.d(TAG, "onRestoreMultiSemantic");
        if (multiSemantic == null) { //????????????????????????????????????
            multiSemantic = MultiInterfaceUtils.getInstance(mContext).readSavedMultiSemantic();
            Log.d(TAG, "multiSemantic: "+multiSemantic);
            if (multiSemantic != null) { //????????????????????????????????????
                MultiInterfaceUtils.getInstance(mContext).clearMultiInterfaceSemantic();
            }
        } else {//?????????????????????>??????????????????????????????????????????????????????????????????????????????????????????
            multiSemantic = null;
        }
    }

    @Override
    public boolean getTtsState() {
        return TTSController.getInstance(mContext).isTtsPlaying();
    }

    @Override
    public void upLoadDictToCloudStatus(long wParam,String param) {
        BluePhoneManager.getInstance(mContext).upLoadDictToCloudStatus(wParam,param);
    }

    @Override
    public void upLoadDictToLocalStatus(long wParam,String param) {
        BluePhoneManager.getInstance(mContext).upLoadDictToLocalStatus(wParam,param);
    }

    @Override
    public void onPgsAction(String pgsJson) {
        EventBusUtils.sendTalkMessage(pgsJson);
    }

    public void unRegisterCallback() {
        EventBus.getDefault().unregister(this);
        if (keyQueryController != null) {
            keyQueryController.unRegisterCallbackEvent();
        }
        if (carController != null){
            carController.finish();
        }
    }

    /**
     * ????????????????????????
     */
    private void dispathchMediaControl(MvwLParamEntity mvwLParamEntity){

        if(isHiCarFg()) {
            Log.e(TAG, "dispathchMediaControl: the hicar is fg");
            return;
        }

        int mvwId = mvwLParamEntity.nMvwId;
        String word = mvwLParamEntity.nKeyword;
        int object = 0;
        String condition = "";
        boolean isdispose = true;
        String curActiveFocus;
        String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        String topActivity = ActivityManagerUtils.getInstance(mContext).getTopActivity();
        Log.d(TAG, "dispathchMediaControl() called with: topPackage = [" + topPackage + "]"+"...topActivity::"+topActivity);
        if(/*"tv.newtv.vcar".equals(topPackage)||*/("com.qiyi.video.pad".equals(topPackage))){//???????????????????????????????????????
            if(FloatViewManager.getInstance(mContext).isHide())  //?????????
                return;
            else{
                doExceptonAction(mContext);  //???????????????????????????
                return;
            }
        }

        if("????????????".equals(word)||"????????????".equals(word)){
            object = R.string.scene_stop;
            condition =TtsConstant.MHXC4CONDITION;
        }else if("????????????".equals(word)||"????????????".equals(word)){
            object = R.string.scene_start;
            condition =TtsConstant.MHXC3CONDITION;
        }else if("?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)){
            object = R.string.scene_pre;
            condition =TtsConstant.MHXC6CONDITION;
        }else if("?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)){
            object = R.string.scene_next;
            condition =TtsConstant.MHXC5CONDITION;
        }

        if (FloatViewManager.getInstance(mContext).isHide()) {
            curActiveFocus = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
        } else {
            curActiveFocus = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_AUDIO_FOCUS_PKGNAME, "");
        }

        LogUtils.d(TAG, "curActiveFocus pkgName: " + curActiveFocus + ",topPackage:" + topPackage);

        if (AppConstant.PACKAGE_NAME_RADIO.equals(topPackage)){
            LogUtils.d(TAG, "curActiveFocus pkgName: 1");
            if (mSpeechService != null) {
                try {
                    isdispose = false;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_radio2,object,condition,R.string.condition_null);
                    mSpeechService.dispatchMvwAction(Business.RADIO, mvwLParamEntity.convert2CmdVoiceModel());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if (AppConstant.PACKAGE_NAME_MUSIC.equals(topPackage)){
            LogUtils.d(TAG, "curActiveFocus pkgName: 2");

            if(DetectionService.ACTIVITY_VIDEO.equals(topActivity)){//???????????????????????????
                controlVideoByBroadcast(object);
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_video,object,condition,R.string.condition_null);
                return;
            }
            else if (mSpeechService != null) {
                try {
                    isdispose = false;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_music,object,condition,R.string.condition_null);
                    mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mvwLParamEntity.nMvwId == 5){
                if (mSpeechService != null) {
                    try {
                        isdispose = false;
                        mSpeechService.dispatchMvwAction(Business.RADIO, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }else if (AppConstant.PACKAGE_NAME_HICAR.equals(topPackage)){//HICAR
            Log.d(TAG, "dispathchMediaControl() called with: PACKAGE_NAME_HICAR = [" + AppConstant.PACKAGE_NAME_HICAR + "]");
            try {
                isdispose = false;
                mSpeechService.dispatchMvwAction(Business.HICAR, mvwLParamEntity.convert2CmdVoiceModel());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else if (AppConstant.PACKAGE_NAME_VCAR.equals(topPackage)){//cctv
            Log.d(TAG, "dispathchMediaControl() called with: PACKAGE_NAME_VCAR = [" + AppConstant.PACKAGE_NAME_VCAR + "]");
            try {
                isdispose = false;
                mSpeechService.dispatchMvwAction(Business.CCTV, mvwLParamEntity.convert2CmdVoiceModel());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (AppConstant.PACKAGE_NAME_CHANGBA.equals(topPackage) &&
                (ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){//??????
            LogUtils.d(TAG, "curActiveFocus pkgName: hhhhhcom.changba.sd");
            isdispose = false;
            ChangbaController.getInstance(mContext).handleNoWakeupWords(object);
        } else if (AppConstant.PACKAGE_NAME_RADIO.equals(curActiveFocus)){
            LogUtils.d(TAG, "curActiveFocus pkgName: 3");
            if (mSpeechService != null) {
                try {
                    isdispose = false;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_radio2,object,condition,R.string.condition_null);
                    mSpeechService.dispatchMvwAction(Business.RADIO, mvwLParamEntity.convert2CmdVoiceModel());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if (AppConstant.PACKAGE_NAME_MUSIC.equals(curActiveFocus)){
            LogUtils.d(TAG, "curActiveFocus pkgName: 4");
            if (mSpeechService != null) {
                try {
                    isdispose = false;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_music,object,condition,R.string.condition_null);
                    mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (AppConstant.PACKAGE_NAME_CHANGBA.equals(curActiveFocus) &&
                (ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){//??????
            LogUtils.d(TAG, "curActiveFocus pkgName: com.changba.sd");
            ChangbaController.getInstance(mContext).handleNoWakeupWords(object);
        } else if (isdispose){//??????????????????????????????????????????????????????????????????????????????????????????????????????
            LogUtils.d(TAG, "curActiveFocus pkgName: 5");
            if(SRAgent.getInstance().mMusicPlaying){  //?????????????????????????????????????????????????????????????????????
                if (mSpeechService != null) {
                    try {
                        Log.d(TAG, "dispathchMediaControl() called with: mMusicPlaying = [" + SRAgent.getInstance().mMusicPlaying + "]");
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_music,object,condition,R.string.condition_null);
                        mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if(SRAgent.getInstance().mInRadioPlaying||SRAgent.getInstance().mRadioPlaying){//?????????????????????????????????????????????????????????????????????
                if (mSpeechService != null) {
                    try {
                        Log.d(TAG, "dispathchMediaControl() called with: mInRadioPlaying = [" + SRAgent.getInstance().mInRadioPlaying + "]"+"..."+SRAgent.getInstance().mRadioPlaying);
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_radio2,object,condition,R.string.condition_null);
                        mSpeechService.dispatchMvwAction(Business.RADIO, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ("internetRadio".equals(SRAgent.getInstance().AppStatus)||"radio".equals(SRAgent.getInstance().AppStatus)){// ????????????????????????????????????????????????
                if (mSpeechService != null) {
                    try {
                        Log.d(TAG, "dispathchMediaControl: "+SRAgent.getInstance().AppStatus);
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_radio2,object,condition,R.string.condition_null);
                        mSpeechService.dispatchMvwAction(Business.RADIO, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ("musicX".equals(SRAgent.getInstance().AppStatus)){//// ????????????????????????????????????????????????
                if (mSpeechService != null) {
                    try {
                        Log.d(TAG, "dispathchMediaControl: "+SRAgent.getInstance().AppStatus);
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_music,object,condition,R.string.condition_null);
                        mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else {
                try {
                    mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel());  //????????????
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel()); //????????????
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ????????????
     * @param object
     * public static final String EXTRA_ACTION_PLAY_VIDEO = "video_action_for_play"; //????????????
     * public static final String EXTRA_ACTION_PAUSE_VIDEO = "video_action_for_pause";//????????????
     * public static final String EXTRA_ACTION_PRE_VIDEO = "video_action_for_pre";//?????????
     * public static final String EXTRA_ACTION_NEXT_VIDEO = "video_action_for_next";//?????????
     */
    private void controlVideoByBroadcast(int object) {
        Log.d(TAG, "controlVideoByBroadcast() called with: object = [" + object + "]");
        Intent intent = new Intent();
        intent.putExtra("action","ifly");
//        intent.setPackage("com.chinatsp.music");
        switch (object){
            case R.string.scene_start:
                intent.setAction("video_action_for_play");
                break;
            case R.string.scene_stop:
                intent.setAction("video_action_for_pause");
                break;
            case R.string.scene_pre:
                intent.setAction("video_action_for_pre");
                Utils.eventTrack(mContext, R.string.skill_vedio, R.string.screne_video_switch, R.string.object_switch_pev, TtsConstant.VIDEOC5CONDITION, R.string.condition_default,mContext.getString(R.string.videoC5));
                break;
            case R.string.scene_next:
                intent.setAction("video_action_for_next");
                Utils.eventTrack(mContext, R.string.skill_vedio, R.string.screne_video_switch, R.string.object_switch_next, TtsConstant.VIDEOC7CONDITION, R.string.condition_default,mContext.getString(R.string.videoC7));
                break;
        }
        mContext.sendBroadcast(intent);
    }

    protected void doExceptonAction(Context context){
        LogUtils.d(TAG, "doExceptonAction() called");
        String defaultTts =  String.format(context.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(context));
        Utils.getMessageWithoutTtsSpeak(context, TtsConstant.MAINC14CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                //tts??????,????????????tts??????,??????tts?????????
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }

                String username = Settings.System.getString(context.getContentResolver(),"aware");
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);

                Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_exception, R.string.object_main_exception_5, TtsConstant.MAINC14CONDITION, R.string.condition_mainC13,ttsText);
                //TTS??????
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
//                        FloatViewManager.getInstance(context).hide();
                        Utils.exitVoiceAssistant();
                    }
                });

            }
        });

    }


    protected void doExceptonAction(Context context,String id){
        LogUtils.d(TAG, "doExceptonAction() called");
        String defaultTts =  context.getString(R.string.navi_cannot_begin);
        Utils.getMessageWithoutTtsSpeak(context, id, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                //tts??????,????????????tts??????,??????tts?????????
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }
                String username = Settings.System.getString(context.getContentResolver(),"aware");
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);

                Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_exception, R.string.object_main_exception_5, TtsConstant.MAINC14CONDITION, R.string.condition_mainC13,ttsText);

                //TTS??????
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        FloatViewManager.getInstance(context).hide();
                    }
                });

            }
        });

    }

    protected void doMediaExceptonAction(Context context,String id){
        LogUtils.d(TAG, "doExceptonAction() called");
        String defaultTts =  context.getString(R.string.yinyue_c38);
        Utils.getMessageWithoutTtsSpeak(context, id, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                //tts??????,????????????tts??????,??????tts?????????
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }
                String username = Settings.System.getString(context.getContentResolver(),"aware");
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);

                //TTS??????
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        FloatViewManager.getInstance(context).hide();
                    }
                });

            }
        });

    }


    private class DelayHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String text =  mContext.getString(R.string.no_network_tip);  //?????????????????????????????????
            timeoutAndExit("", text);
        }
    }

    private void doHandlerCctvMvws(MvwLParamEntity entity) {
        if("????????????".equals(entity.nKeyword)){
            try {
                mSpeechService.dispatchMvwAction(Business.CCTV,entity.convert2CmdVoiceModel());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ???????????????????????? ????????????
     * @param nKeyword
     */
    private void doHandlerCallMvws(String nKeyword) {
        if ("??????".equals(nKeyword)) { //??????
            if (BluePhoneManager.getInstance(mContext).getCallStatus() == CallContact.CALL_STATE_INCOMING) {
                contactController.answerCall();
            }else if(!FloatViewManager.getInstance(mContext).isHide())
                doExceptonAction(mContext);
        } else if ("??????".equals(nKeyword)) { //??????
            if (BluePhoneManager.getInstance(mContext).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                contactController.rejectCall();
            }else if(!FloatViewManager.getInstance(mContext).isHide())
                doExceptonAction(mContext);
        }
    }

    private void doHandlerDvrMvws(String nKeyword){
        IntentEntity entity = controlDvrBySr(nKeyword);
        if(entity!=null&&cmdController!=null)
            cmdController.srAction(entity);
    }

    private void doHandlerCustomMvws(String word){
        Log.d(TAG, "doHandlerCustomMvws() called with: word = [" + word + "]");
        if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_CHANGBA){
            ChangbaController.getInstance(mContext).handleMvwWords(word);
        }/*else  if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_VIDEO){
            if("????????????".equals(word)){

            }else  if("????????????".equals(word)){

            }
        }*/else  if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DVR){
            if("?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)){
              if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                  CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_FRONT);
                  Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC45CONDITION, R.string.condition_mhxc45,"");
              }else if(CarUtils.getInstance(mContext).isAvmOpen()){
                  if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                      CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DFRONT);
                  else
                      CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLFRONT);
                  Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC45CONDITION, R.string.condition_mhxc45,"");
              }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
            }else  if("?????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_FRONT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC45CONDITION, R.string.condition_mhxc45,"");
                }else if(CarUtils.getInstance(mContext).isAvmOpen()){
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DFRONT);
                    else
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLFRONT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC45CONDITION, R.string.condition_mhxc45,"");
                }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
            }else  if("?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_REAR);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC46CONDITION, R.string.condition_mhxc46,"");
                }else if(CarUtils.getInstance(mContext).isAvmOpen()) {
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DREAR);
                    else
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLREAR);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC46CONDITION, R.string.condition_mhxc46,"");
                }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
            }else  if("?????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_REAR);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC46CONDITION, R.string.condition_mhxc46,"");
                }else if(CarUtils.getInstance(mContext).isAvmOpen()){
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DREAR);
                    else
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLREAR);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC46CONDITION, R.string.condition_mhxc46,"");

                }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
            }else  if("?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_LEFT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC47CONDITION, R.string.condition_mhxc47,"");
                }else if(CarUtils.getInstance(mContext).isAvmOpen()) {
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DLEFT);
                    else
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLLEFT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC47CONDITION, R.string.condition_mhxc47,"");

                }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
            }else  if("?????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_LEFT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC47CONDITION, R.string.condition_mhxc47,"");
                }else if(CarUtils.getInstance(mContext).isAvmOpen()){
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DLEFT);
                    else
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLLEFT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC47CONDITION, R.string.condition_mhxc47,"");
                }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);

            }else  if("?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_RIGHT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC48CONDITION, R.string.condition_mhxc48,"");

                }else if(CarUtils.getInstance(mContext).isAvmOpen()){
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DRIGHT);
                    else
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLRIGHT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC48CONDITION, R.string.condition_mhxc48,"");
                }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
            }else  if("?????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_RIGHT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC48CONDITION, R.string.condition_mhxc48,"");
                }else if(CarUtils.getInstance(mContext).isAvmOpen()){
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT)
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALL3DRIGHT);
                    else
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLRIGHT);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC48CONDITION, R.string.condition_mhxc48,"");
                }
                getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
            }else  if("?????????".equals(word)||"?????????".equals(word)||"?????????".equals(word)||"????????????".equals(word)){
                if(DetectionService.PACKAGE_DVR.equals(AppConstant.mCurrentPkg)){
                    CarUtils.getInstance(mContext).setDvrVisualAngle(DVR.DVR_VISUAL_ANGLE_ALL);
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_DVR, R.string.skill_avm_dvr, TtsConstant.MHXC49CONDITION, R.string.condition_mhxc49,"");
                }else if(CarUtils.getInstance(mContext).isAvmOpen()){
                    if(CarUtils.getInstance(mContext).avmDisplayForm>= AVM.AVM_DISPLAY_ALL_3D_RIGHT
                            &&!FloatViewManager.getInstance(mContext).isHide()){
                        String defaultTts =  mContext.getString(R.string.main_c14_1);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MAINC14_1CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                String ttsText = tts;
                                //tts??????,????????????tts??????,??????tts?????????
                                if (TextUtils.isEmpty(tts)){
                                    ttsText = defaultTts;
                                }
                                String username = Settings.System.getString(mContext.getContentResolver(),"aware");
                                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);
                                //TTS??????
                                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                                    @Override
                                    public void onPlayStopped() {
                                        Utils.exitVoiceAssistant();
                                    }
                                });

                            }
                        });
                    } else {
                        CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.LEFTRIGHT);
                        getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
                    }
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_AVM, R.string.skill_avm_dvr, TtsConstant.MHXC49CONDITION, R.string.condition_mhxc49,"");
                }

            }
        }else
            doExceptonAction(mContext);
    }


    /**
     * ??????????????????????????????????????????
     */
    private boolean doHandleFeedBackAction(IntentEntity intentEntity){
        if(isFeedBackWords(intentEntity.text)&&TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK){ //  ??????????????????
            intentEntity.service = PlatformConstant.Service.VIEWCMD; //  ?????????????????? ?????? ?????????????????? ??????????????????
            intentEntity.operation = PlatformConstant.Operation.VIEWCMD;
            Semantic semantic = new Semantic();
            Semantic.SlotsBean slotsBean = new Semantic.SlotsBean();
            slotsBean.viewCmd = intentEntity.text;
            semantic.slots = slotsBean;
            intentEntity.semantic = semantic;
//                        FeedBackController.getInstance(mContext).srAction(intentEntity);

            if(ChairController.getInstance(mContext).isMusicToPlay())
                ChairController.getInstance(mContext).srAction(intentEntity);
            else
                FeedBackController.getInstance(mContext).srAction(intentEntity);

            return true;
        }else if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK){
            if(FeedBackController.getInstance(mContext).getUnderstandCound()==0
                    ||DrivingCareController.getInstance(mContext).getUnderstandCound()==0){
                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                timeoutAndRestartSR(TtsConstant.MAINC10_1CONDITION,mContext.getString(R.string.tire_no_unstanstand_one));
                if(FeedBackController.getInstance(mContext).getUnderstandCound()==0){
                    Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_4, TtsConstant.MAINC10_1CONDITION, R.string.condition_mainC10_1);
                    FeedBackController.getInstance(mContext).setUnderstandCound();
                }else  if(DrivingCareController.getInstance(mContext).getUnderstandCound()==0){
                    Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_nospeak, R.string.object_main_nospeak_4, TtsConstant.MAINC10_1CONDITION, R.string.condition_mainC10_1);
                    DrivingCareController.getInstance(mContext).setUnderstandCound();
                }

                return true;
            }else {
                onSrTimeOut(1);
                return true;
            }
        }else if(isFeedBackWords(intentEntity.text)&&TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_VEHICLE){
            VehicleControl.getInstance(mContext).onDoAction(intentEntity.text);
            return true;
        }else if(isFeedBackWords(intentEntity.text)&&TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DRIVING_CARE){ //  ??????????????????
            intentEntity.service = PlatformConstant.Service.VIEWCMD; //  ?????????????????? ?????? ?????????????????? ??????????????????
            intentEntity.operation = PlatformConstant.Operation.VIEWCMD;
            Semantic semantic = new Semantic();
            Semantic.SlotsBean slotsBean = new Semantic.SlotsBean();
            slotsBean.viewCmd = intentEntity.text;
            semantic.slots = slotsBean;
            intentEntity.semantic = semantic;

            DrivingCareController.getInstance(mContext).srAction(intentEntity);

            return true;
        }else if(isFeedBackWords(intentEntity.text)&&ActiveServiceViewManager.ActiveServiceView_Show){
            if (activeController != null) {
                activeController.setActiveIdentify(intentEntity.text);
            }
            return true;
        }
        return false;
    }

    private boolean doHandleSelectAction(IntentEntity intentEntity){
        if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_SELECT) {
            IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
            if (iController != null) {
                if (iController instanceof MapController) {  //???????????????
                    MapController mapController = (MapController) iController;
                    mapController.onDoAction(intentEntity.text);
                    return true;
                } else if (iController instanceof ContactController) {//????????????????????????
                    ContactController contactController = (ContactController) iController;
                    contactController.onDoAction(intentEntity.text);
                    return true;
                }
            }
        }else {
            if ("??????".equals(intentEntity.text)||"??????".equals(intentEntity.text)){
                //???????????????????????????????????????
                if (MXSdkManager.getInstance(mContext).isExitShowShow()) {
                    mapController.stopNaviMutual(intentEntity.text);
                }else if(!FloatViewManager.getInstance(mContext).isHide()){
                    doExceptonAction(mContext);
                }
            }else {
                Utils.startTTSOnly("???????????????????????????", new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
            }

        }
        return false;
    }

    private IntentEntity controlDvrBySr(String nKeyword){
        IntentEntity entity = new IntentEntity();
        entity.service = "cmd";
        entity.operation = "INSTRUCTION";
        Semantic semantic = new Semantic();
        Semantic.SlotsBean slots = new Semantic.SlotsBean();
        if("??????????????????".equals(nKeyword))
            slots.insType = "TAKE_PHOTO";
        if("??????????????????".equals(nKeyword))
            slots.insType = "TAKE_VIDEO";
        semantic.slots = slots;
        entity.semantic =semantic;
        return entity;
    }


    private void getMessageWithTtsSpeak(String conditionId, int defaultText) {
        if(!FloatViewManager.getInstance(mContext).isHide()){//??????????????????????????????????????????????????????????????????
            Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(defaultText), new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    Utils.exitVoiceAssistant();
                }
            });
        }

    }

    private boolean isHiCarFg(){
        String topActivity = ActivityManagerUtils.getInstance(mContext).getTopActivity();
        if(SRAgent.mHICARPlaying&&DetectionService.ACTIVITY_HICAR.equals(topActivity))
            return true;
        else return false;
    }


}
