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
        //搜集所有的唤醒词，当无语义的时候匹配
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

            //重新计算超时
            SRAgent.getInstance().resetSrTimeCount();
            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
            //if (intentEntity.rc == PlatformConstant.SUCCESS) {
            if(PlatformConstant.Service.CARCONTROL.equals(intentEntity.service) &&
                    intentEntity.semantic != null && intentEntity.semantic.slots != null && ((intentEntity.semantic.slots.mode != null &&
                    (intentEntity.semantic.slots.mode).contains("后视镜")) || (intentEntity.semantic.slots.name != null &&
                    (intentEntity.semantic.slots.name).contains("后视镜")))){
                Log.d(TAG, "intentEntity.service = " + intentEntity.service);
                intentEntity.setService(PlatformConstant.Service.AIRCONTROL);
            }
            if("vehicleInfo".equals(intentEntity.service) && "查看胎压".equals(intentEntity.text)) {
                intentEntity.service = PlatformConstant.Service.APP;
                intentEntity.operation = PlatformConstant.Operation.LAUNCH;
            }
            if (PlatformConstant.Service.MAP_U.equals(intentEntity.service)) { //导航
                if (multiSemantic != null) { //二次交互处理
                    if (PlatformConstant.Service.MAP_U.equals(multiSemantic.service)
                            && PlatformConstant.Operation.OPEN.equals(multiSemantic.operation)) {
                        MultiInterfaceUtils.getInstance(mContext).uploadCmdDefaultData(); //上传桌面处于前台状态
                    } else if (PlatformConstant.Service.MAP_U.equals(multiSemantic.service)
                            && PlatformConstant.Operation.USR_POI_SET.equals(multiSemantic.operation)) {
                        try {
                            String topic = multiSemantic.semantic.slots.endLoc.topic;
                            intentEntity.semantic.slots.endLoc.topic = topic;
                        } catch (Exception e) {}

                    }

                    //记录的上一次识别场景为可见即可说，进行恢复
                    /*SRAgent srAgent = SRAgent.getInstance();
                    if (srAgent.mSrArgu_Old != null && SrSession.ISS_SR_SCENE_STKS.equals(srAgent.mSrArgu_Old.scene)) {
                        srAgent.mSrArgu_New = new SrSessionArgu(srAgent.mSrArgu_Old);
                        srAgent.mSrArgu_Old = null;
                        srAgent.startSRSession();
                    }*/
                    multiSemantic = null; //被处理了
                }
                curController = mapController;
                mapController.srAction(intentEntity);
            } else if (PlatformConstant.Service.TELEPHONE.equals(intentEntity.service)) { //打电话

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
            } else if (PlatformConstant.Service.WEATHER.equals(intentEntity.service)) { //天气
                if (weatherController == null) {
                    weatherController = new WeatherController(mContext);
                }
                curController = weatherController;
                weatherController.srAction(intentEntity);
            } else if (PlatformConstant.Service.PERSONALNAME.equals(intentEntity.service)) { // 取名字
                if (multiSemantic != null) { //二次交互处理
                    if (PlatformConstant.Service.PERSONALNAME.equals(multiSemantic.service)
                            && PlatformConstant.Operation.SET.equals(multiSemantic.operation)
                            &&!"".equals(intentEntity.text)) {
                        if(speechSetController.isSetFail(intentEntity.text)){
                            return "";
                        }
                        speechSetController.changeName(intentEntity.text);
                        multiSemantic = null; //被处理了
                        return "";
                    }
                    multiSemantic = null; //被处理了
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
                        if(SRAgent.mMusicPlaying){ //说明y音乐在播放，通知音乐释放焦点
                            mSpeechService.dispatchSRAction(Business.MUSIC, SourceManager.getInstance(mContext).changeSourceVoiceModel());
                        }

                        if (multiSemantic==null) {
                            mSpeechService.dispatchSRAction(Business.RADIO, intentEntity.convert2NlpVoiceModel());
                        }else {
                            mSpeechService.dispatchMutualAction(Business.RADIO, intentEntity.convert2MutualVoiceModel(multiSemantic));
                            multiSemantic = null; //被处理了
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (PlatformConstant.Service.MUSIC.equals(intentEntity.service)) { //音乐场景，交由音乐自已处理
//                if(("打开领唱".equals(intentEntity.text) || "打开原唱".equals(intentEntity.text)) &&
//                        (ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){
//                    ChangbaController.getInstance(mContext).handleMvwWords("打开领唱");
//                    return "";
//                }else if("切歌".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords("切歌");
//                    return "";
//                }else if(("上一页".equals(intentEntity.text) || "下一页".equals(intentEntity.text)) &&
//                        AppConstant.PACKAGE_NAME_CHANGBA.equals(ActivityManagerUtils.getInstance(mContext).getTopPackage()) &&
//                        (ChangbaController.getInstance(mContext).SONGLISTDETAILACTIVITY.equals(ChangbaController.getInstance(mContext).initActivity) ||
//                                ChangbaController.getInstance(mContext).PINYINCHOOSESONGACTIVITY.equals(ChangbaController.getInstance(mContext).initActivity))){
//                    MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
//                    mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_SELECT;
//                    if("上一页".equals(intentEntity.text)){
//                        mvwLParamEntity.nMvwId = 7;
//                    }else if("下一页".equals(intentEntity.text)){
//                        mvwLParamEntity.nMvwId = 6;
//                    }
//                    ChangbaController.getInstance(mContext).mvwAction(mvwLParamEntity);
//                    return "";
//                }
                    if (mSpeechService != null) {
                        try {
                            if(SRAgent.mRadioPlaying||SRAgent.mInRadioPlaying){ //说明电台在播放
                                mSpeechService.dispatchSRAction(Business.RADIO, SourceManager.getInstance(mContext).changeSourceVoiceModel());
                            }

                            if (multiSemantic==null) {
                                mSpeechService.dispatchSRAction(Business.MUSIC, intentEntity.convert2NlpVoiceModel());
                            }else {
                                mSpeechService.dispatchMutualAction(Business.MUSIC, intentEntity.convert2MutualVoiceModel(multiSemantic));
                                multiSemantic = null; //被处理了
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                }
//            } else if (PlatformConstant.Service.WEIXIN.equals(intentEntity.service)) { //车信场景，交由车信自已处理
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
            } else if (PlatformConstant.Service.JOKE.equals(intentEntity.service) || PlatformConstant.Service.NEWS.equals(intentEntity.service)) { //听笑话，听新闻交由电台处理
                if (mSpeechService != null) {
                    try {
                        if(SRAgent.mMusicPlaying){ //说明y音乐在播放，通知音乐释放焦点
                            mSpeechService.dispatchSRAction(Business.MUSIC, SourceManager.getInstance(mContext).changeSourceVoiceModel());
                        }

                        mSpeechService.dispatchSRAction(Business.RADIO, intentEntity.convert2NlpVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            } else if (PlatformConstant.Service.BAIKE.equals(intentEntity.service)) {//闲聊
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
//                if("关闭领唱".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords(intentEntity.text);
//                    return "";
//                }
                if (cmdController == null) {
                    cmdController = new CMDController(mContext);
                }
                cmdController.srAction(intentEntity);
            } else if (PlatformConstant.Service.APP.equals(intentEntity.service)) {
//                if(intentEntity.semantic != null && intentEntity.semantic.slots != null && "唱吧".equals(intentEntity.semantic.slots.name)
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
            }/* else if (PlatformConstant.Service.FEEDBACK.equals(intentEntity.service)){//我要吐槽
                EventBusUtils.sendTalkMessage(intentEntity.text);
                gotofeedback();
            }*/ else if (PlatformConstant.Service.VIDEO.equals(intentEntity.service)){//我要吐槽
                if (mVideoController == null) {
                    mVideoController = VideoController.getInstance(mContext);
                }
                mVideoController.srAction(intentEntity);
            }else if (PlatformConstant.Service.VIEWCMD.equals(intentEntity.service)){//我要吐槽
                if (mFeedBackController == null)
                    mFeedBackController = FeedBackController.getInstance(mContext);

                if(mChairController.isMusicToPlay())
                    mChairController.srAction(intentEntity);
                else
                    mFeedBackController.srAction(intentEntity);
            }
            else if (PlatformConstant.Service.CHANGBA.equals(intentEntity.service)){//唱吧
//                if("切歌".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords("切歌");
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
                    PlatformConstant.Service.CALC.equals(intentEntity.service)){//时间日期、诗词、计算器
                if (mAnswerController == null)
                    mAnswerController = AnswerController.getInstance(mContext);
                mAnswerController.srAction(intentEntity);
            } else if ("上传日志".equals(intentEntity.text)) {
                OsUploadUtils.getInstance(mContext).startLogApp(true);
            }else if ("保存日志".equals(intentEntity.text)) {
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


//            if (intentEntity.text.equals("我要吐槽") || intentEntity.text.equals("我想吐槽")) {
//                EventBusUtils.sendTalkMessage(intentEntity.text);
//                gotofeedback();
//            } else
            Log.d(TAG,"TspSceneAdapter.getTspScene(mContext) = " + TspSceneAdapter.getTspScene(mContext));
            if("iFlytekQA".equals(intentEntity.service)||"chat".equals(intentEntity.service)){   //无语义返回的字段
                Log.d(TAG, "onDoAction: "+intentEntity.answer.text);
                if(intentEntity.answer!=null){
                    Log.d(TAG, "onDoAction: "+intentEntity.answer.question.question);
                    EventBusUtils.sendTalkMessage(intentEntity.answer.question.question);

                    if (multiSemantic != null) { //处理二轮交互中，第二次用户说出producFtName的场景
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
                            //我要回家/去公司二次交互，第二次用户说出PoiName的场景
                            if (PlatformConstant.Operation.USR_POI_SET.equals(multiSemantic.operation)) {
                                String topic = multiSemantic.semantic.slots.endLoc.topic;
                                curController = mapController;
                                mapController.requestPoiData(intentEntity.text, topic);
                                //换个地方二次交互，第二次用户说出PoiName的场景
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
                                if (text.startsWith("沿途的")){
                                    text=text.replace("沿途的","").trim();
                                    mapController.alongTheWaySearch(text, topic);
                                    return "";
                                }
                                mapController.searchAddress(text);
                            }else { //闲聊兜底
                                Utils.eventTrack(mContext, R.string.skill_chat, R.string.scene_chat, R.string.object_chat, TtsConstant.CHATC1CONDITION, R.string.condition_default,intentEntity.answer.text);
                                if(chatController!=null&&!FloatViewManager.getInstance(mContext).isHide())
                                    chatController.srAction(intentEntity.answer.text);
                            }
                        } else if (PlatformConstant.Service.TELEPHONE.equals(multiSemantic.service)) {
                            //我想打电话二轮交互，第二次用户说出联系人的场景
                            if (PlatformConstant.Operation.DIAL.equals(multiSemantic.operation)) {
                                curController = contactController;
                                contactController.requestContactData(intentEntity.text);
                            }
                            //重播二轮交互，确认重拨或取消重拨
                            else if (PlatformConstant.Operation.INSTRUCTION.equals(multiSemantic.operation)
                                    && "REDIAL".equals(multiSemantic.semantic.slots.insType)) {
                                ContactEntity contact = new ContactEntity(multiSemantic.semantic.slots.name, multiSemantic.semantic.slots.code);
                                if (intentEntity.text.startsWith("确定")) {
                                    contactController.redial(contact);
                                } else if (intentEntity.text.startsWith("取消")) {
                                    contactController.cancelDial();
                                }else { //闲聊兜底
                                    Utils.eventTrack(mContext, R.string.skill_chat, R.string.scene_chat, R.string.object_chat, TtsConstant.CHATC1CONDITION, R.string.condition_default,intentEntity.answer.text);
                                    if(chatController!=null&&!FloatViewManager.getInstance(mContext).isHide())
                                        chatController.srAction(intentEntity.answer.text);
                                }
                            }
                            //回拨二轮交互，确认回拨或取消回拨
                            else if (PlatformConstant.Operation.INSTRUCTION.equals(multiSemantic.operation)
                                    && "CALLBACK".equals(multiSemantic.semantic.slots.insType)) {
                                ContactEntity contact = new ContactEntity(multiSemantic.semantic.slots.name, multiSemantic.semantic.slots.code);
                                if (intentEntity.text.startsWith("确定")) {
                                    contactController.callback(contact);
                                } else if (intentEntity.text.startsWith("取消")) {
                                    contactController.cancelDial();
                                }else { //闲聊兜底
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
//                            if (intentEntity.text.contains("确定")) {
//                                carController.confirmInteractive(multiSemantic);
//                            } else if (intentEntity.text.contains("取消")) {
//                                carController.cancelInteractive(multiSemantic);
//                            }
//                        }

                        //记录的上一次识别场景为可见即可说，进行恢复
                        SRAgent srAgent = SRAgent.getInstance();
                        if (srAgent.mSrArgu_Old != null && SrSession.ISS_SR_SCENE_STKS.equals(srAgent.mSrArgu_Old.scene)) {
                            srAgent.mSrArgu_New = new SrSessionArgu(srAgent.mSrArgu_Old);
                            srAgent.mSrArgu_Old = null;
                            srAgent.startSRSession();
                        }

                        multiSemantic = null; //被处理了
                        //重新计算超时
                        SRAgent.getInstance().resetSrTimeCount();
                        TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE);
                        return "";
                    }else  if (isMvwWord(intentEntity.text)) {      //如果是免唤醒词
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
                        LogUtils.e(TAG, "忽略该次SR识别: " + intentEntity.text);
                        return "";
                    } else if (isNaviWords(intentEntity.text)) {  //如果是导航的免唤醒词
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
                //todo 已实名验证情况，无实名验证暂未完成
                Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC19CONDITION, R.string.condition_mainC19);
            }*/ else {

                EventBusUtils.sendTalkMessage(intentEntity.text);

                if(AppConstant.PACKAGE_NAME_WECARNAVI.equals(ActivityManagerUtils.getInstance(mContext)
                        .getTopPackage())&&isPageByText(intentEntity.text)){  //主界面上说上一页 下一页
                    String exitMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
                    String normalMsg = String.format(mContext.getString(R.string.no_support), MvwKeywordsUtil.getCurrentName(mContext));
                    if (!FloatViewManager.getInstance(mContext).isHide()) {
                        timeoutAndExit("", normalMsg);
                    }
                    return "";
                }else if(doHandleFeedBackAction(intentEntity)){
                    Log.e(TAG, "onDoAction: is feedback screne");
                    return "";
                }else if("播放".equals(intentEntity.text) || "继续播放".equals(intentEntity.text)){
                    MvwLParamEntity entity = new MvwLParamEntity();
                    entity.nKeyword = "开始播放";
                    dispathchMediaControl(entity);
                    return "";
                }else if("暂停".equals(intentEntity.text) || "暂停播放".equals(intentEntity.text)){
                    MvwLParamEntity entity = new MvwLParamEntity();
                    entity.nKeyword = "暂停播放";
                    dispathchMediaControl(entity);
                    return "";
                }else if ("列表循环".equals(intentEntity.text)
                       ||"单曲循环".equals(intentEntity.text)
                        ||"随机播放".equals(intentEntity.text)
                        ||"顺序循环".equals(intentEntity.text)
                        ||"随机循环".equals(intentEntity.text)){
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
                }/*else if("关闭领唱".equals(intentEntity.text) || "关闭原唱".equals(intentEntity.text) || "打开领唱".equals(intentEntity.text) ||
                        "打开原唱".equals(intentEntity.text) || "打开伴唱".equals(intentEntity.text) || "关闭伴唱".equals(intentEntity.text) ||
                        "确定".equals(intentEntity.text)){
//                    ChangbaController.getInstance(mContext).handleMvwWords(intentEntity.text);
                    doExceptonAction(mContext);
                    return "";
                }*/else if(isImageWord(intentEntity.text)){  //dvr免唤醒词
                    String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
                    if(DetectionService.PACKAGE_DVR.equals(topPackage)){ //当前是在dvr界面
                        doHandlerCustomMvws(intentEntity.text);
                        return "";
                    }

                } else if(isNaviSceneByText(intentEntity.text)) {
                    handleNaviWords(intentEntity);
                    return "";
                }

                String text = mContext.getString(R.string.no_understand);
                if(intentEntity.rc==4){ //1057195 与欧尚确认统一播报不理解
                    if(doHandleFeedBackAction(intentEntity)){
                        Log.e(TAG, "onDoAction: is feedback screne");
                        return "";
                    }

                    if(!NetworkUtil.isNetworkAvailable(mContext))
                        text = mContext.getString(R.string.no_network_tip);  //无网络，播报网络未连接
//                    else if("1".equals(intentEntity.bislocalresult))
//                            text = mContext.getString(R.string.network_weka_tip);  //网络弱，播报网络不稳定
                    else{
                        timeoutAndRestartSR("",text);
                        return "";
                    }
                    timeoutAndExit("", text);
                    return "";

                }

                String exitMsg="抱歉我没听清楚，有需要再叫我";
                String normalMsg= "很抱歉我没有理解，可以换种说法吗";
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
            Utils.startTTS("请先打开导航再进行操作");
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
     * 将无语音的返回转花为唤醒词
     * @param text
     */
    private void srActionToMvwAction(String text) {
        Log.d(TAG, "srActionToMvwAction() called with: text = [" + text + "]");
        if(text==null||"".equals(text)){
            doExceptonAction(mContext);
            return;
        }
        MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
        if(text.equals("返回首页")
                ||text.equals("返回主页")
                ||text.equals("返回主界面")
                ||text.equals("返回桌面")
                ||text.equals("回到首页")
                ||text.equals("回到主页")
                ||text.equals("回到主界面")
                ||text.equals("回到桌面")
                ||text.equals("回首页")
                ||text.equals("回主页")
                ){
            cmdController.goToHome();
            return;
        }else if(text.equals("退下")){
            cmdController.exit();
            return;
        }else if(text.equals("闭嘴")){
            cmdController.stopTTS();
            return;
        }else if(text.equals("暂停播放")||text.equals("暂停")){
            mvwLParamEntity.nMvwId = 5;
            mvwLParamEntity.nKeyword = "暂停播放";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_stop, "", R.string.scene_interaction);
        }else if(text.equals("停止播放")){
            mvwLParamEntity.nMvwId = 11;
            mvwLParamEntity.nKeyword = "停止播放";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_stop, "", R.string.scene_interaction);
        }else if(text.equals("开始播放")||text.equals("播放")){
            mvwLParamEntity.nMvwId = 12;
            mvwLParamEntity.nKeyword = "开始播放";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_start, "", R.string.scene_interaction);
        }else if(text.equals("继续播放")){
            mvwLParamEntity.nMvwId = 13;
            mvwLParamEntity.nKeyword = "继续播放";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_start, "", R.string.scene_interaction);
        } else if(text.equals("上一曲")){
            mvwLParamEntity.nMvwId = 14;
            mvwLParamEntity.nKeyword = "上一曲";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("上一首")){
            mvwLParamEntity.nMvwId = 15;
            mvwLParamEntity.nKeyword = "上一首";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("上一台")){
            mvwLParamEntity.nMvwId = 16;
            mvwLParamEntity.nKeyword = "上一台";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("上一个")){
            mvwLParamEntity.nMvwId = 17;
            mvwLParamEntity.nKeyword = "上一个";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_pre, "", R.string.scene_interaction);
        }else if(text.equals("下一曲")){
            mvwLParamEntity.nMvwId = 18;
            mvwLParamEntity.nKeyword = "下一曲";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("下一首")){
            mvwLParamEntity.nMvwId = 19;
            mvwLParamEntity.nKeyword = "下一首";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("下一台")){
            mvwLParamEntity.nMvwId = 20;
            mvwLParamEntity.nKeyword = "下一台";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("下一个")){
            mvwLParamEntity.nMvwId = 21;
            mvwLParamEntity.nKeyword = "下一个";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("上一集")){
            mvwLParamEntity.nMvwId = 25;
            mvwLParamEntity.nKeyword = "上一集";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if(text.equals("下一集")){
            mvwLParamEntity.nMvwId = 26;
            mvwLParamEntity.nKeyword = "下一集";
            Utils.eventTrack(mContext,R.string.skill_global_nowake, R.string.scene_osset, R.string.scene_next, "", R.string.scene_interaction);
        }else if (text.equals("接听")) {  //语音界面起不来，只可能走唤醒逻辑
            if (BluePhoneManager.getInstance(mContext).getCallStatus() == CallContact.CALL_STATE_INCOMING) {
                contactController.answerCall();
            }else if(!FloatViewManager.getInstance(mContext).isHide()){
                doExceptonAction(mContext);
            }
            return;
        } else if (text.equals("挂断")) {//语音界面起不来，只可能走唤醒逻辑
            if (BluePhoneManager.getInstance(mContext).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                contactController.rejectCall();
            }else if(!FloatViewManager.getInstance(mContext).isHide()){
                doExceptonAction(mContext);
            }
            return;
        } else if (text.equals("声音大一点")) {
            Semantic.SlotsBean slotsBean  = new Semantic.SlotsBean();
            slotsBean.insType = CMDController.VOLUME_PLUS;
            cmdController.changeMediaVolume(slotsBean,true);
            return;
        }else if (text.equals("声音小一点")) {
            Semantic.SlotsBean slotsBean  = new Semantic.SlotsBean();
            slotsBean.insType = CMDController.VOLUME_MINUS;
            cmdController.changeMediaVolume(slotsBean,true);
            return;
        }else if (text.equals("小欧我要回家")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "小欧我要回家";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("小欧我要去公司")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "小欧我要去公司";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("关闭播报")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "关闭播报";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("关闭导航声音")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "关闭导航声音";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("打开播报")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "打开播报";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("打开导航声音")) {
                mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_OTHER;
                mvwLParamEntity.nKeyword = "打开导航声音";
                mapController.mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("小欧我要拍照")) {
           doHandlerDvrMvws(text);
            return;
        }else if (text.equals("小欧我要录像")) {
            doHandlerDvrMvws(text);
            return;
        }else if (text.equals("开始录屏")) {
            //TODO
            return;
        }else if (text.equals("停止录屏")) {
            //TODO
            return;
        }else if (text.equals("我要截屏")) {
            //TODO
            return;
        }else if (text.equals("温度高一点")) {
            mvwLParamEntity.nKeyword = "温度高一点";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("温度低一点")) {
            mvwLParamEntity.nKeyword = "温度低一点";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("风速大一点")) {
            mvwLParamEntity.nKeyword = "风速大一点";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("风速小一点")) {
            mvwLParamEntity.nKeyword = "风速小一点";
            AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
            return;
        }else if (text.equals("退出赛道模式")) {
            //TODO
            return;
        }else if (text.equals("小欧我要唱歌")) {
            //TODO
            return;
        }else if (text.equals("小欧我要抽烟")) {
            //TODO
            return;
        }else if (text.equals("小欧我要休息")) {
            //TODO
            return;
        }/*else if (text.equals("小欧打开后视仪")) {
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
        if(text!=null&&text.contains("上一页")
                ||(text!=null&&text.contains("下一页")))
            return true;
        return false;
    }

    @Override
    public void onSrNoHandleTimeout(String exitMsg, String normalMsg) {
        int srState = TimeoutManager.getSrState(mContext);
        Log.d(TAG, "onSrNoHandleTimeout() called with: exitMsg = [" + exitMsg + "], srState = [" + srState + "]");
        if (srState == TimeoutManager.NO_UNDERSTAND) { //连续两次未识别，退出
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
                    //未识别超时处理
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
        } else if (AppConstant.PACKAGE_NAME_MUSIC.equals(topPkgname)) {  //当前音乐在前台, 交由音乐自已处理
            if (mSpeechService != null) {
                try {
                    mSpeechService.dispatchStksAction(Business.MUSIC, stkResult.convert2CmdVoiceModel());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (AppConfig.INSTANCE.settingFlag) { //语音设置界面
            if (voiceSettingController == null) {
                voiceSettingController = new VoiceSettingController(mContext);
            }
            voiceSettingController.stkAction(stkResult);
        }

        return null;
    }

    @Override
    public void onSrTimeOut(int srTimeCount) { //不说话超时
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
        if (lastSrState == TimeoutManager.ORIGINAL) { //一直不说话
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
                    TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, ""); //清除srText
                    if(SearchListFragment.isShown){
                        if(curController!=null&&curController==contactController){
                            if(text!=null&&text.contains("多个号码"))
                                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_no_speak, TtsConstant.PHONEC29CONDITION, R.string.condition_phoneC29,text);
                            else
                                Utils.eventTrack(mContext, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_no_speak, TtsConstant.PHONEC30CONDITION, R.string.condition_phoneC30,text);
                        }else if(curController!=null&&curController==mapController){
                            Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_users_do_not_talk, TtsConstant.NAVIC36CONDITION, R.string.condition_navi36,text);
                        }
                        timeoutAndNotRestartSR("", text);//如果是在选择列表界面，播报完成之后不进行识别
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
                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, ""); //清除srText
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
        if (TextUtils.isEmpty(userToken)) {//未登陆
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
            normalMsg = "很抱歉我没有理解，可以换种说法吗？";
            exitMsg = "抱歉我没听清楚，有需要再叫我";
        } else if (wParam == ISSErrors.ISS_ERROR_NO_RESULT) {
            normalMsg = "很抱歉我没有理解，可以换种说法吗？";
            exitMsg = "抱歉我没听清楚，有需要再叫我";
        } else {
            normalMsg = "很抱歉我没有理解，可以换种说法吗";
            exitMsg = "抱歉我没听清楚，有需要再叫我";
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
            LogUtils.d(TAG, "处于倒车界面, ignore");
            if(mvwLParamEntity.nMvwScene==MvwSession.ISS_MVW_SCENE_IMAGE){
                //不做任何处理，在倒车界面 ，全景免唤醒词可以影响  不要根据场景判断，否则主唤醒词也会被响应
            } else
                return null;
        }

        if(!IflyUtils.iflytekIsInited(mContext)){
            Log.e(TAG, "onDoMvwAction: the ifly not inited!");
            return null;
        }


        int active= Settings.Global.getInt(mContext.getContentResolver(),"hicarphone_active",0);//1为hicar来电通话中，0为挂断；
        if(active==1){
            Log.e(TAG, "onDoMvwAction: hicar call "+active);
            return null;
        }


        /*if ("1".equals(Utils.getProperty("evs_disable_touch", "0"))){
            Log.e(TAG, "onDoMvwAction: 全景界面" );
            return null;
        }*/

        if(mDelayHandler.hasMessages(MSG_SR_FAILED))
            mDelayHandler.removeMessages(MSG_SR_FAILED);//由于语音开启识别失败，唤醒的时候，取消消息发送

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
            //如果语音助理显示，此时说免唤醒词，停止录音
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
                //全局唤醒MXSdkManager
                if (mvwLParamEntity.nMvwId >=0&&mvwLParamEntity.nMvwId<=3) {
                    //你好小欧
                    cmdController.showAssistant(0);
                } else if (mvwLParamEntity.nMvwId == 4 || mvwLParamEntity.nMvwId == 5) {
                    //你好欧尚
                    cmdController.showAssistant(1);
                }else if("小艺小艺".equals(mvwLParamEntity.nKeyword)){
                    try {
                        mSpeechService.dispatchMvwAction(Business.HICAR, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case MvwSession.ISS_MVW_SCENE_SELECT:
                //选择
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
                if(mvwLParamEntity.nMvwId == 2){  //针对讯飞做的临时补丁
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

                //当前不是在主动服务界面，由于是自定义唤醒词，不需要转化
                if(!ActiveServiceViewManager.ActiveServiceView_Show){
                    // ****** 0 和 1 保持不变 *************
                    if(mvwLParamEntity.nMvwId>=3&&mvwLParamEntity.nMvwId<=5){//针对 confirm 场景其他的唤醒词转化为 确定 和 取消
                        mvwLParamEntity.nMvwId = 1;
                    }else if(mvwLParamEntity.nMvwId>=6&&mvwLParamEntity.nMvwId<=10){//针对 confirm 场景其他的唤醒词转化为 确定 和 取消
                        mvwLParamEntity.nMvwId = 0;
                    }
                }


                //确定/取消
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
                    //回到主页
                    Utils.eventTrack(mContext,R.string.scene_homepage,R.string.object_homepage,DatastatManager.primitive,R.string.object_homepage,DatastatManager.response,TtsConstant.SYSTEMC23CONDITION,R.string.condition_default,mContext.getString(R.string.msg_tts_isnull),true);
                    cmdController.goToHome();
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_global,R.string.scene_gohome,TtsConstant.MHXC2CONDITION,R.string.condition_null,mContext.getString(R.string.msg_tts_isnull));
                } else if (mvwLParamEntity.nMvwId == 34) {
                    //退出
                    cmdController.exit();
                } else if (mvwLParamEntity.nMvwId == 33) {
                    //闭嘴
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_global,R.string.object_exit_voice,TtsConstant.MHXC22CONDITION,R.string.condition_null,"");
                    cmdController.stopTTS();
                    cmdController.exit();
                } else if (mvwLParamEntity.nMvwId >= 11
                        && mvwLParamEntity.nMvwId <= 22) { //多媒体
                    dispathchMediaControl(mvwLParamEntity);
                }else if ("上一集".equals(mvwLParamEntity.nKeyword)
                ||"下一集".equals(mvwLParamEntity.nKeyword)) { //多媒体
                    dispathchMediaControl(mvwLParamEntity);
                } else if (mvwLParamEntity.nMvwId == 25) { //接听
                    if (BluePhoneManager.getInstance(mContext).getCallStatus() == CallContact.CALL_STATE_INCOMING) {
                        contactController.answerCall();
                    }else if(!FloatViewManager.getInstance(mContext).isHide())
                        doExceptonAction(mContext);
                } else if (mvwLParamEntity.nMvwId == 26) { //挂断
                    if (BluePhoneManager.getInstance(mContext).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                        contactController.rejectCall();
                    }else if(!FloatViewManager.getInstance(mContext).isHide())
                        doExceptonAction(mContext);
                }else if (mvwLParamEntity.nMvwId ==23) { //声音大一点
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
                }else if (mvwLParamEntity.nMvwId ==24) { //声音小一点
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
                }else if ("小欧我要回家".equals(mvwLParamEntity.nKeyword)) {
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("小欧我要去公司".equals(mvwLParamEntity.nKeyword)) {
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("关闭播报".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("关闭导航声音".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("打开播报".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("打开导航声音".equals(mvwLParamEntity.nKeyword)) {
                    if(isHiCarFg()){
                        Log.e(TAG, "onDoMvwAction: the hicar is fg");
                        return "";
                    }
                        mapController.mvwAction(mvwLParamEntity);
                }else if ("小欧我要拍照".equals(mvwLParamEntity.nKeyword)) {
                     doHandlerDvrMvws(mvwLParamEntity.nKeyword);
                }else if ("小欧我要录像".equals(mvwLParamEntity.nKeyword)) {
                    doHandlerDvrMvws(mvwLParamEntity.nKeyword);
                }else if ("我要截屏".equals(mvwLParamEntity.nKeyword)) {
                    OsUploadUtils.getInstance(mContext).captureOrRecordScrenn(1,0);
                }else if ("开始录屏".equals(mvwLParamEntity.nKeyword)) {
                    OsUploadUtils.getInstance(mContext).captureOrRecordScrenn(2,0);
                }else if ("停止录屏".equals(mvwLParamEntity.nKeyword)) {
                    OsUploadUtils.getInstance(mContext).stopRecordScreen();
                }else if ("温度高一点".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("温度低一点".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("风速大一点".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("风速小一点".equals(mvwLParamEntity.nKeyword)) {
                    AirController.getInstance(mContext).mvwAction(mvwLParamEntity);
                }else if ("退出赛道模式".equals(mvwLParamEntity.nKeyword)) {
                    doExceptonAction(mContext);
                }else if ("小欧打开后视仪".equals(mvwLParamEntity.nKeyword)) {
                    doExceptonAction(mContext);
                }else if("小欧我要休息".equals(mvwLParamEntity.nKeyword)){
                    IntentEntity intentEntity = new IntentEntity();
                    intentEntity.semantic = new Semantic();
                    intentEntity.semantic.slots = new Semantic.SlotsBean();
                    intentEntity.semantic.slots.modeValue = ChairController.MODE_SLEEP;
                    intentEntity.semantic.slots.mode = ChairController.MODE;
                    intentEntity.text = "小欧我要休息";
                    ChairController.getInstance(mContext).DoMVWAction(intentEntity);
                }else if("小欧我要抽烟".equals(mvwLParamEntity.nKeyword)){
                    IntentEntity intentEntity = new IntentEntity();
                    intentEntity.semantic = new Semantic();
                    intentEntity.semantic.slots = new Semantic.SlotsBean();
                    intentEntity.semantic.slots.mode = CarController.SMOKING;
                    intentEntity.semantic.slots.name = CarController.SMOKING;
                    intentEntity.semantic.slots.action = "";
                    intentEntity.text = "小欧我要抽烟";
                    CarController.getInstance(mContext).srActionCar(intentEntity,"");
                }else if("小欧我要唱歌".equals(mvwLParamEntity.nKeyword)){
//                    IntentEntity intentEntity = new IntentEntity();
//                    intentEntity.semantic = null;
//                    intentEntity.text = "我想唱歌";
//                    ChangbaController.getInstance(mContext).handleOneShot(intentEntity);
                    doExceptonAction(mContext);
                }
                break;
            case MvwSession.ISS_MVW_SCENE_CUSTOME:
                Log.d(TAG, "onDoMvwAction() called with: mvwJson = [" + mvwLParamEntity.nKeyword + "]");
                if(mvwLParamEntity.nKeyword.contains("你好")){  //有自定义唤醒词
                    cmdController.showAssistant(2);//自定义唤醒词 8 和 9
                    DatastatManager.getInstance().wakeup_event(mContext, "语音唤醒", mvwLParamEntity.nKeyword);
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
                (mvwLParamEntity.nKeyword!=null&&mvwLParamEntity.nKeyword.contains("你好"))) {

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
        if (multiSemantic == null) { //没有待处理的二次交互语义
            multiSemantic = MultiInterfaceUtils.getInstance(mContext).readSavedMultiSemantic();
            Log.d(TAG, "multiSemantic: "+multiSemantic);
            if (multiSemantic != null) { //语义读到内存后删除本地的
                MultiInterfaceUtils.getInstance(mContext).clearMultiInterfaceSemantic();
            }
        } else {//给你取个名字－>导航到世界之窗，此时第一次保存的语义没有被处理，此后不予处理
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
     * 处理媒体控制指令
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
        if(/*"tv.newtv.vcar".equals(topPackage)||*/("com.qiyi.video.pad".equals(topPackage))){//如果央视影音在前台，不响应
            if(FloatViewManager.getInstance(mContext).isHide())  //免唤醒
                return;
            else{
                doExceptonAction(mContext);  //识别逻辑，防止发呆
                return;
            }
        }

        if("暂停播放".equals(word)||"停止播放".equals(word)){
            object = R.string.scene_stop;
            condition =TtsConstant.MHXC4CONDITION;
        }else if("开始播放".equals(word)||"继续播放".equals(word)){
            object = R.string.scene_start;
            condition =TtsConstant.MHXC3CONDITION;
        }else if("上一曲".equals(word)||"上一首".equals(word)||"上一个".equals(word)||"上一台".equals(word)||"上一集".equals(word)){
            object = R.string.scene_pre;
            condition =TtsConstant.MHXC6CONDITION;
        }else if("下一曲".equals(word)||"下一首".equals(word)||"下一个".equals(word)||"下一台".equals(word)||"下一集".equals(word)){
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

            if(DetectionService.ACTIVITY_VIDEO.equals(topActivity)){//如果当前视频在前台
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
                (ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){//唱吧
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
                (ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){//唱吧
            LogUtils.d(TAG, "curActiveFocus pkgName: com.changba.sd");
            ChangbaController.getInstance(mContext).handleNoWakeupWords(object);
        } else if (isdispose){//如果音频焦点在语音上，且没有处理免唤醒，则根据上一次的上报应用来处理
            LogUtils.d(TAG, "curActiveFocus pkgName: 5");
            if(SRAgent.getInstance().mMusicPlaying){  //如果音乐在播放，不管是前台还是后台，都给他处理
                if (mSpeechService != null) {
                    try {
                        Log.d(TAG, "dispathchMediaControl() called with: mMusicPlaying = [" + SRAgent.getInstance().mMusicPlaying + "]");
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_music,object,condition,R.string.condition_null);
                        mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if(SRAgent.getInstance().mInRadioPlaying||SRAgent.getInstance().mRadioPlaying){//如果电台在播放，不管是前台还是后台，都给他处理
                if (mSpeechService != null) {
                    try {
                        Log.d(TAG, "dispathchMediaControl() called with: mInRadioPlaying = [" + SRAgent.getInstance().mInRadioPlaying + "]"+"..."+SRAgent.getInstance().mRadioPlaying);
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_radio2,object,condition,R.string.condition_null);
                        mSpeechService.dispatchMvwAction(Business.RADIO, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ("internetRadio".equals(SRAgent.getInstance().AppStatus)||"radio".equals(SRAgent.getInstance().AppStatus)){// 都处于暂停状态，电台最后上报状态
                if (mSpeechService != null) {
                    try {
                        Log.d(TAG, "dispathchMediaControl: "+SRAgent.getInstance().AppStatus);
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_radio2,object,condition,R.string.condition_null);
                        mSpeechService.dispatchMvwAction(Business.RADIO, mvwLParamEntity.convert2CmdVoiceModel());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ("musicX".equals(SRAgent.getInstance().AppStatus)){//// 都处于暂停状态，音乐最后上报状态
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
                    mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel());  //默认音乐
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                mSpeechService.dispatchMvwAction(Business.MUSIC, mvwLParamEntity.convert2CmdVoiceModel()); //默认音乐
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 控制视频
     * @param object
     * public static final String EXTRA_ACTION_PLAY_VIDEO = "video_action_for_play"; //播放视频
     * public static final String EXTRA_ACTION_PAUSE_VIDEO = "video_action_for_pause";//暂停视频
     * public static final String EXTRA_ACTION_PRE_VIDEO = "video_action_for_pre";//上一曲
     * public static final String EXTRA_ACTION_NEXT_VIDEO = "video_action_for_next";//下一曲
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
                //tts为空,则用默认tts代替,避免tts不播报
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }

                String username = Settings.System.getString(context.getContentResolver(),"aware");
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);

                Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_exception, R.string.object_main_exception_5, TtsConstant.MAINC14CONDITION, R.string.condition_mainC13,ttsText);
                //TTS播报
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
                //tts为空,则用默认tts代替,避免tts不播报
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }
                String username = Settings.System.getString(context.getContentResolver(),"aware");
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);

                Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_exception, R.string.object_main_exception_5, TtsConstant.MAINC14CONDITION, R.string.condition_mainC13,ttsText);

                //TTS播报
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
                //tts为空,则用默认tts代替,避免tts不播报
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }
                String username = Settings.System.getString(context.getContentResolver(),"aware");
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);

                //TTS播报
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
            String text =  mContext.getString(R.string.no_network_tip);  //无网络，播报网络未连接
            timeoutAndExit("", text);
        }
    }

    private void doHandlerCctvMvws(MvwLParamEntity entity) {
        if("全屏播放".equals(entity.nKeyword)){
            try {
                mSpeechService.dispatchMvwAction(Business.CCTV,entity.convert2CmdVoiceModel());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理蓝牙电话接听 挂断逻辑
     * @param nKeyword
     */
    private void doHandlerCallMvws(String nKeyword) {
        if ("接听".equals(nKeyword)) { //接听
            if (BluePhoneManager.getInstance(mContext).getCallStatus() == CallContact.CALL_STATE_INCOMING) {
                contactController.answerCall();
            }else if(!FloatViewManager.getInstance(mContext).isHide())
                doExceptonAction(mContext);
        } else if ("挂断".equals(nKeyword)) { //挂断
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
            if("全屏播放".equals(word)){

            }else  if("退出全屏".equals(word)){

            }
        }*/else  if(TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DVR){
            if("看前面".equals(word)||"向前看".equals(word)||"朝前看".equals(word)){
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
            }else  if("看前边".equals(word)){
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
            }else  if("看后边".equals(word)||"向后看".equals(word)||"朝后看".equals(word)){
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
            }else  if("看后面".equals(word)){
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
            }else  if("看左面".equals(word)||"向左看".equals(word)||"朝左看".equals(word)){
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
            }else  if("看左边".equals(word)){
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

            }else  if("看右面".equals(word)||"向右看".equals(word)||"朝右看".equals(word)){
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
            }else  if("看右边".equals(word)){
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
            }else  if("看左右".equals(word)||"看两边".equals(word)||"看四周".equals(word)||"组合视图".equals(word)){
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
                                //tts为空,则用默认tts代替,避免tts不播报
                                if (TextUtils.isEmpty(tts)){
                                    ttsText = defaultTts;
                                }
                                String username = Settings.System.getString(mContext.getContentResolver(),"aware");
                                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);
                                //TTS播报
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
     * 针对确定场景下的语义返回补丁
     */
    private boolean doHandleFeedBackAction(IntentEntity intentEntity){
        if(isFeedBackWords(intentEntity.text)&&TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_FEEDBACK){ //  人脸识别语义
            intentEntity.service = PlatformConstant.Service.VIEWCMD; //  人脸识别语义 并且 当前界面处于 人脸识别界面
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
        }else if(isFeedBackWords(intentEntity.text)&&TspSceneAdapter.getTspScene(mContext)==TspSceneAdapter.TSP_SCENE_DRIVING_CARE){ //  人脸识别语义
            intentEntity.service = PlatformConstant.Service.VIEWCMD; //  人脸识别语义 并且 当前界面处于 人脸识别界面
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
                if (iController instanceof MapController) {  //地图无语义
                    MapController mapController = (MapController) iController;
                    mapController.onDoAction(intentEntity.text);
                    return true;
                } else if (iController instanceof ContactController) {//电话联系人无语义
                    ContactController contactController = (ContactController) iController;
                    contactController.onDoAction(intentEntity.text);
                    return true;
                }
            }
        }else {
            if ("确定".equals(intentEntity.text)||"取消".equals(intentEntity.text)){
                //只处理有退出导航弹框的情况
                if (MXSdkManager.getInstance(mContext).isExitShowShow()) {
                    mapController.stopNaviMutual(intentEntity.text);
                }else if(!FloatViewManager.getInstance(mContext).isHide()){
                    doExceptonAction(mContext);
                }
            }else {
                Utils.startTTSOnly("未处于列表选择界面", new TTSController.OnTtsStoppedListener() {
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
        if("小欧我要拍照".equals(nKeyword))
            slots.insType = "TAKE_PHOTO";
        if("小欧我要录像".equals(nKeyword))
            slots.insType = "TAKE_VIDEO";
        semantic.slots = slots;
        entity.semantic =semantic;
        return entity;
    }


    private void getMessageWithTtsSpeak(String conditionId, int defaultText) {
        if(!FloatViewManager.getInstance(mContext).isHide()){//语音显示的时候，走唤醒逻辑，进行播报规避发呆
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
