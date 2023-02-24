package com.chinatsp.ifly.voice.platformadapter.controller;

import android.app.ActivityManager;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarSensorManager;
import android.car.hardware.cabin.CarCabinManager;
import android.car.hardware.constant.AVM;
import android.car.hardware.constant.DVR;
import android.car.hardware.constant.MCU;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.FaceDetector;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.base.BaseLifecycleCallback;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.VolumeView;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.ICMDController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.SettingsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.listener.IExtendCallback;
import com.iflytek.adapter.common.TspSceneAdapter;

import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_POS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNSHADE_POS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNSHADE_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_WIPER_SENSOR;
import static android.car.hardware.cabin.CarCabinManager.ID_DVR_SNAP_SHOOT;
import static com.chinatsp.ifly.GuideMainActivity.mContext;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;

public class CMDController extends BaseController implements ICMDController {
    private static final String TAG = "xyj_CMDController";
    private Context context;
    private static final int MSG_TTS = 1003;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VOLUME_VALUE = 40;
    private MyHandler myHandler = new MyHandler(this);
    private AudioManager mAudioManager;

    private final int BRIGHTNESS_MAX_VALUE = 100;
    private final int BRIGHTNESS_MIN_VALUE = 0;

    private final int PHONE_MAX_VALUE = 40;
    private final int PHONE_MIN_VALUE = 0;
    // 亮度最大
    private final static String BRIGHTNESS_MAX = "BRIGHTNESS_MAX";
    // 亮度最小
    private final static String BRIGHTNESS_MIN = "BRIGHTNESS_MIN";
    // 降低亮度
    private final static String BRIGHTNESS_MINUS = "BRIGHTNESS_MINUS";
    // 升高亮度
    private final static String BRIGHTNESS_PLUS = "BRIGHTNESS_PLUS";
    // 亮度调到某一具体值
    private final static String BRIGHTNESS_ADJUST = "BRIGHTNESS_ADJUST";

    // 增大音量
    public final static String VOLUME_PLUS = "VOLUME_PLUS";
    // 降低音量
    public final static String VOLUME_MINUS = "VOLUME_MINUS";
    // 音量调至某一具体值
    private final static String VOLUME_ADJUST = "VOLUME_ADJUST";
    // 静音
    private final static String VOLUME_MUTE = "VOLUME_MUTE";
    // 取消静音
    private final static String VOLUME_UNMUTE = "VOLUME_UNMUTE";
    //打开语音设置
    private static final String OPEN_VOICE = "OPEN_VOICE";
    //语音设置
    private static final String VOICE = "VOICE";

    //打开行车记录仪
    private static final String OPEN_TACHOGRAPH = "OPEN_TACHOGRAPH";
    //关闭行车记录仪
    private static final String CLOSE_TACHOGRAPH = "CLOSE_TACHOGRAPH";
    // 返回
    private static final String BACK = "BACK";
    // 返回主界面
    private static final String HOMEPAGE = "HOMEPAGE";

    // 拍照/我要拍照
    private final static String TAKE_PHOTO = "TAKE_PHOTO";

    //我要录像
    private final static String TAKE_VIDEO = "TAKE_VIDEO";

    // 关闭屏幕
    private final static String CLOSE_SCREEN = "CLOSE_SCREEN";
    //退出
    private final static String EXIT = "CLOSE";

    //退下
    private final static String SLEEP = "SLEEP";

    //系统设置
    private final static String OPEN_SYSTEM_SET = "OPEN_SYSTEM_SET";
    private final static String OPEN_SETTING_SET = "OPEN_SETTING_SET";


    //显示模式
    private int showMode;
    private final static String MODE_DAY = "MODE_DAY";
    private final static String MODE_NIGHT = "MODE_NIGHT";
    private final static String MODE_AUTO = "MODE_AUTO";

    private final static String MAP_U = "mapU";
    private final static String TELEPHONE = "telephone";
    private final static String BLUETOOTH = "bluetooth";
    private final static String BLUETOOTHPHONE = "bluetoothPhone";

    private final static String MEDIA = "media";
    private final static String SYSTEM = "system";
    private final static String CANCEL = "CANCEL";
    private final static String XIAOOU = "xiaoou";

    private String conditionId = "";

    private String resText = "";

    private String  defaultText = "";

    private String parameter = "";

    private String TYPE = "小欧";

    private int appName = 0;
    private int scene = 0;
    private int object = 0;
    private int condition = 0;
    private static final int MSG_SHOW_WORD= 1001;
    private static final int MSG_WAIT_DVR_OPEN= 1002;
    private static final int MSG_SEND_COMMAND = 1003;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOW_WORD:
                    Map map_word = (Map) msg.obj;
                    Utils.getTtsMessage(context, (String) map_word.get("conditionId"), (String) map_word.get("defaultText"), "", true,
                            (int)map_word.get("appName1"), (int)map_word.get("scene1"), (int)map_word.get("object1"),(int)map_word.get("condition1"),false);
                    break;
                case MSG_WAIT_DVR_OPEN:
                    int what = (int) msg.obj;
                    boolean isTachUsable = isDVRUsable();
                    if (!isTachUsable) {
                        changeDVRStatus(DVR.DVR_ON);
                    }
                    isTachUsable = isDVRUsable();
                    AppConstant.isCallDVR = true;
                    if (!isTachUsable) {
                        AppConstant.isCallDVR = false;
                        conditionId = TtsConstant.DVRC6CONDITION;
                        defaultText = context.getString(R.string.dvrC6);
                        scene = R.string.scene_excption;
                        object = R.string.object_dvr_excption2;
                        condition = R.string.condition_dvr6;

                        if(what == 1)
                            Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC24CONDITION,R.string.condition_null,defaultText,true);
                        else if(what == 2)
                            Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC25CONDITION,R.string.condition_null,defaultText,true);

                        if(FloatViewManager.getInstance(context).isHide()){
                            showAssistant();

                            Message msg1 = new Message();
                            Map map = new HashMap();
                            map.put("conditionId",conditionId);
                            map.put("defaultText",defaultText);
                            map.put("appName1",R.string.skill_dvr);
                            map.put("scene1",scene);
                            map.put("object1",object);
                            map.put("condition1",condition);

                            msg1.what = MSG_SHOW_WORD;
                            msg1.obj = map;
                            handler.sendMessageDelayed(msg1,600);
                        }else {
                            Utils.getTtsMessage(context, conditionId, defaultText, "", true,R.string.skill_dvr,
                                    scene, object,condition,false);
                        }
                    } else {
                        setDVRMode();//下发拍照或录制前，先设置DVR工作模式bug1068739
                        Message msg2 = new Message();
                        msg2.what = MSG_SEND_COMMAND;
                        msg2.obj = what;
                        handler.sendMessageDelayed(msg2,200);
                    }
                    break;
                case MSG_SEND_COMMAND:
                    int whats = (int) msg.obj;
                    if(whats == 1){
                        callTakePhoto();
                    }else{
                        callTakeVideo();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //行车记录仪APP的包名
    private String  DVRPackageName = "com.chinatsp.dvrcamera";
    public CMDController(Context context) {
        this.context = context.getApplicationContext();
        this.mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void showAssistant(int whichName) {

        //保存当前唤醒的name
        SharedPreferencesUtils.saveInt(context, AppConstant.KEY_WHICH_NAME, whichName);
        Settings.System.putString(context.getContentResolver(),"aware", MvwKeywordsUtil.getCurrentName(context));

        //判断是否是新手引导
//        boolean isFirstUse = SharedPreferencesUtils.getBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,false);
//        Log.d(TAG, "showAssistant: isFirstUse"+isFirstUse);
//        if(isFirstUse){
//            //发广播，唤醒“新手引导”
//            Intent broad = new Intent(AppConstant.MY_TEST_BROADCAST_WAKEUP);
//            context.sendBroadcast(broad);
//        }else {
            Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
//            context.sendBroadcast(broad);
            LocalBroadcastManager.getInstance(context).sendBroadcast(broad);
//        }


        EventBusUtils.sendRestartSpeechTimeOut();

    }

    /**
     * 退出
     */
    public void exit() {
        Utils.exit(context);
    }

    private void openSystemSet(){
        String condition = TtsConstant.SYSTEMC24CONDITION;
        String defaultText = context.getString(R.string.systemC24);
        SettingsUtil.startSetting(context, null);
        Utils.getMessageWithTtsSpeakOnly(context, condition, defaultText, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                if (!FloatViewManager.getInstance(context).isHide()) {
                    FloatViewManager.getInstance(context).hide();
                }
            }
        });
        Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, condition, R.string.condition_system_default);
    }

    @Override
    public void goToHome() {
        //360 全景在前台时，关闭
        if(CarController.getInstance(context).getAVMStatus() != AVM.AVM_OFF){
            CarController.getInstance(context).setAVMStatus(AVM.AVM_ON);
        }

        if(FloatViewManager.getInstance(context).isHide()){
            if(!MXSdkManager.getInstance(BaseApplication.getInstance()).isForeground()) {
                gotoHome();
            } else {
                MXSdkManager.getInstance(BaseApplication.getInstance()).exitSecondPage();
            }
        }else {
            Utils.startTTSOnly("好的", new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    if(!MXSdkManager.getInstance(BaseApplication.getInstance()).isForeground()) {
                        gotoHome();
                    } else {
                        MXSdkManager.getInstance(BaseApplication.getInstance()).exitSecondPage();
                    }
                    FloatViewManager.getInstance(context).hide();
                }
            });
        }
    }

    @Override
    public void stopTTS() {
        TTSController.getInstance(context).stopTTS();
        EventBusUtils.sendMainMessage(MessageEvent.ACTION_GREY);
        if(!FloatViewManager.getInstance(context).isHide() && !BaseLifecycleCallback.getInstance().isAppOnForeground()) {
            FloatViewManager.getInstance(context).hide();
        }
    }

    /**
     * 关闭屏幕并退出
     */
    private void closeScreenAndExit() {
        Utils.closeScreen();
        Utils.exitVoiceAssistant();
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        String text = "";
        if (!TextUtils.isEmpty(intentEntity.semantic.slots.insType)) {
            String insType = intentEntity.semantic.slots.insType;

            if (BRIGHTNESS_MAX.equals(insType) || BRIGHTNESS_MIN.equals(insType) || BRIGHTNESS_MINUS.equals(insType)
                    || BRIGHTNESS_PLUS.equals(insType) || BRIGHTNESS_ADJUST.equals(insType)) {
                //调节亮度
                changeBrightness(insType, intentEntity.semantic.slots.series);
            } else if (VOLUME_PLUS.equals(insType) || VOLUME_MINUS.equals(insType) || VOLUME_ADJUST.equals(insType)) {
                if (MAP_U.equals(intentEntity.semantic.slots.tag)) {
                    //导航音量控制
                    changeMapVoice(intentEntity.semantic.slots);
                } else if (TELEPHONE.equals(intentEntity.semantic.slots.tag)||BLUETOOTH.equals(intentEntity.semantic.slots.tag)
                        ||BLUETOOTHPHONE.equals(intentEntity.semantic.slots.tag)) {
                    //电话音量
                    changePhoneVolume(intentEntity.semantic.slots);
                } else if (MEDIA.equals(intentEntity.semantic.slots.tag)) {
                    // 媒体音量
                    changeMediaVolume(intentEntity.semantic.slots,false);
                } else if (SYSTEM.equals(intentEntity.semantic.slots.tag)
                ||XIAOOU.equals(intentEntity.semantic.slots.tag)) {
                    // 系统音量
                    changeSystemVolume(intentEntity.semantic.slots);
                } else if (TextUtils.isEmpty(intentEntity.semantic.slots.tag) && TextUtils.isEmpty(intentEntity.semantic.slots.series)) {
                    boolean isNaviBeforeVoice = SharedPreferencesUtils.getBoolean(context,AppConstant.KEY_NAVI_BEFORE_VOICE,false);
                    if(isNaviBeforeVoice){
                        changeMapVoiceMVW(intentEntity.semantic.slots,false);
                    }else {
                        //默认音量
                        changeMediaVolumeMVW(intentEntity.semantic.slots,false);
                    }
                } else
                    changeMediaVolume(intentEntity.semantic.slots,false); //默认音量
            } else if (VOLUME_MUTE.equals(insType)  && TextUtils.isEmpty(intentEntity.semantic.slots.tag)) {
                // 静音
                try{
                    AppConfig.INSTANCE.mCarAudioManager.setMasterMute(true,0);
                    // 系统：mute 静音/非静音 -》 volumeSwitch
                    // 应用：mute 播放/暂停
                    Log.d(TAG,"set mute...");
                }catch (CarNotConnectedException e){
                    Log.d(TAG,"failed to set mute...");
                }

                conditionId = TtsConstant.SYSTEMC45CONDITION;
                resText = context.getString(R.string.systemC45);
                defaultText = resText;
                Utils.getTtsMessage(context, conditionId, defaultText, parameter, true,R.string.skill_system, R.string.scene_sound, R.string.object_sound_mute, R.string.condition_system_default,false);
            } else if (VOLUME_UNMUTE.equals(insType)  && TextUtils.isEmpty(intentEntity.semantic.slots.tag)) {
                // 取消静音
//                if (AudioFocusUtils.getInstance(context).isMasterMute()) {
                    try {
                        AppConfig.INSTANCE.mCarAudioManager.setMasterMute(false, 0);
                        AppConstant.setMute =false;
                        Log.d(TAG,"cancel mute...");
                    } catch (CarNotConnectedException e) {
                        e.printStackTrace();
                        Log.d(TAG,"failed to cancel mute...");
                    }
//                }

                conditionId = TtsConstant.SYSTEMC46CONDITION;
                resText = context.getString(R.string.systemC46);
                defaultText = resText;
                Utils.getTtsMessage(context, conditionId, defaultText, parameter, true,R.string.skill_system, R.string.scene_sound, R.string.object_sound_mute_cancel, R.string.condition_system_default,false);
            } else if(VOLUME_UNMUTE.equals(insType) && !TextUtils.isEmpty(intentEntity.semantic.slots.tag)){
                //打开XX音量
                openVolume(intentEntity.semantic.slots);
            } else if(VOLUME_MUTE.equals(insType) && !TextUtils.isEmpty(intentEntity.semantic.slots.tag)) {
                //关闭XX音量
                closeVolume(intentEntity.semantic.slots);
            } else if (TextUtils.isEmpty(intentEntity.semantic.slots.tag)
                    && VOLUME_ADJUST.equals(insType)) {
                // 没有明确声道，默认媒体音量
                changeSystemVolume(intentEntity.semantic.slots);
            } else if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation)
                    && (OPEN_VOICE.equals(insType) || VOICE.equals(insType))) {
                //打开语音设置
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                resText = context.getString(R.string.systemC24);
                defaultText = resText;
                Utils.getMessageWithTtsSpeak(context, conditionId, defaultText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Intent intent = new Intent(context, SettingsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
            } else if (TAKE_PHOTO.equals(intentEntity.semantic.slots.insType) || TAKE_VIDEO.equals(intentEntity.semantic.slots.insType)) {
                int sdcardErrorStatus = CarUtils.getInstance(context).getSdcardErrorStatus();
                Log.d(TAG,"sdcard状态：" + sdcardErrorStatus);
                if (!checkAppInstalled(context, DVRPackageName)) {
                    conditionId = TtsConstant.DVRC5CONDITION;
                    defaultText = context.getString(R.string.dvrC5);
                    if(TAKE_PHOTO.equals(intentEntity.semantic.slots.insType))
                        Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC24CONDITION,R.string.condition_null,defaultText,true);
                    if(TAKE_VIDEO.equals(intentEntity.semantic.slots.insType))
                        Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC25CONDITION,R.string.condition_null,defaultText,true);
                    Utils.getTtsMessage(context, conditionId, defaultText, parameter, true,R.string.skill_dvr, R.string.scene_excption, R.string.object_dvr_excption1,R.string.condition_dvr5,false);
                } else {
                    if(sdcardErrorStatus == -1 || sdcardErrorStatus == DVR.DVR_SDCARD_ERROR_NO_ERROR){//SD卡无错误,-1是中间件报上来的默认值，说明SD卡无异常
                        boolean isDVRForeground =  AppControlManager.getInstance(mContext).isAppointForeground(DVRPackageName);
                        startApp(DVRPackageName);
                        int seconds = 0;
                        if(!isDVRForeground){
                            seconds = 2000;
                        }else{
                            seconds = 0;
                        }
                        Message msg = new Message();
                        msg.what = MSG_WAIT_DVR_OPEN;
                        if(TAKE_PHOTO.equals(intentEntity.semantic.slots.insType)){
                            msg.obj = 1;
                        }else{
                            msg.obj = 2;
                        }
                        handler.sendMessageDelayed(msg,seconds);
                    }else {//SD卡异常
                        startApp(DVRPackageName);
                        if(sdcardErrorStatus == DVR.DVR_SDCARD_ERROR_NO_SDCARD){//SD卡无挂载
                            if(TAKE_PHOTO.equals(intentEntity.semantic.slots.insType)){
                                conditionId = TtsConstant.DVRC2_1CONDITION;
                                defaultText = context.getString(R.string.dvrC2_1);
                                scene = R.string.scene_dvr;
                                object = R.string.object_dvr;
                            }else {
                                conditionId = TtsConstant.DVRC2_6CONDITION;
                                defaultText = context.getString(R.string.dvrC2_6);
                                scene = R.string.scene_dvr_video;
                                object = R.string.object_dvr_video;
                            }
                        }else if(sdcardErrorStatus == DVR.DVR_SDCARD_ERROR_INSUFFICIENT){//SD卡容量不足
                            if(TAKE_PHOTO.equals(intentEntity.semantic.slots.insType)){
                                conditionId = TtsConstant.DVRC2_3CONDITION;
                                defaultText = context.getString(R.string.dvrC2_3);
                                scene = R.string.scene_dvr_video;
                                object = R.string.object_dvr_video;
                            }else {
                                conditionId = TtsConstant.DVRC2_8CONDITION;
                                defaultText = context.getString(R.string.dvrC2_8);
                                scene = R.string.scene_dvr_video;
                                object = R.string.object_dvr_video;
                            }
                        }else{//SD卡其他异常
                            if(TAKE_PHOTO.equals(intentEntity.semantic.slots.insType)){
                                conditionId = TtsConstant.DVRC2_2CONDITION;
                                defaultText = context.getString(R.string.dvrC2_2);
                                scene = R.string.scene_dvr_video;
                                object = R.string.object_dvr_video;
                            }else {
                                conditionId = TtsConstant.DVRC2_7CONDITION;
                                defaultText = context.getString(R.string.dvrC2_7);
                                scene = R.string.scene_dvr_video;
                                object = R.string.object_dvr_video;
                            }
                        }

                        if(TAKE_PHOTO.equals(intentEntity.semantic.slots.insType))
                            Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC24CONDITION,R.string.condition_null,defaultText,true);
                        else if(TAKE_VIDEO.equals(intentEntity.semantic.slots.insType))
                            Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC25CONDITION,R.string.condition_null,defaultText,true);

                        if(FloatViewManager.getInstance(context).isHide()){
                            showAssistant();

                            Message msg = new Message();
                            Map map = new HashMap();
                            map.put("conditionId",conditionId);
                            map.put("defaultText",defaultText);
                            map.put("appName1",R.string.skill_dvr);
                            map.put("scene1",scene);
                            map.put("object1",object);
                            map.put("condition1",R.string.condition_dvr2);

                            msg.what = MSG_SHOW_WORD;
                            msg.obj = map;
                            handler.sendMessageDelayed(msg,600);
                        }else {
                            Utils.getTtsMessage(context, conditionId, defaultText, "", true,R.string.skill_dvr,
                                    scene, object,R.string.condition_dvr2,false);
                        }
                    }
                }
            }
//            else if (TAKE_VIDEO.equals(intentEntity.semantic.slots.insType)) {
//                //拍照/我要录像
//                if (!checkAppInstalled(context, DVRPackageName)) {
//                    conditionId = TtsConstant.DVRC5CONDITION;
//                    resText = context.getString(R.string.dvrC5);
//                    defaultText = resText;
//                    Utils.getTtsMessage(context, conditionId, defaultText, parameter, true,R.string.skill_dvr, R.string.scene_excption, R.string.object_dvr_excption1,R.string.condition_dvr5,false);
//                } else {
//                    boolean isTachUsable = isDVRUsable();
//                    if (!isTachUsable) {
//                        changeDVRStatus(DVR.DVR_ON);
//                    }
//                    isTachUsable = isDVRUsable();
//                    if (!isTachUsable) {
//                        conditionId = TtsConstant.DVRC6CONDITION;
//                        defaultText = context.getString(R.string.dvrC6);
//                        Utils.getTtsMessage(context, conditionId, defaultText, parameter, true,R.string.skill_dvr, R.string.scene_excption, R.string.object_dvr_excption2,R.string.condition_dvr6,false);
//                    } else {
//                        callTakeVideo();
//                        checkTakeVideoStatus();
//                    }
//                }
//            }
            else if (OPEN_TACHOGRAPH.equals(intentEntity.semantic.slots.insType)){
                //text = "好的，正在打开";
                if(!checkAppInstalled(context,DVRPackageName)){
                    conditionId = TtsConstant.DVRC5CONDITION;
                    resText = context.getString(R.string.dvrC5);
                    defaultText = resText;
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_excption;
                    object = R.string.object_dvr_excption1;
                    condition = R.string.condition_dvr5;
                }else {
                    conditionId = TtsConstant.DVRC3CONDITION;
                    resText = context.getString(R.string.dvrC3);
                    defaultText = resText;
                    //TODO 调用打开行程记录仪接口
                    //changeDVRStatus(CUSTOME_SWITCH_ON);
                    startApp(DVRPackageName);
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_dvr_app;
                    object = R.string.object_dvr_app1;
                    condition = R.string.condition_default;
                }
                Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
            } else if (CLOSE_TACHOGRAPH.equals(intentEntity.semantic.slots.insType)){
                if(!checkAppInstalled(context,DVRPackageName)){
                    conditionId = TtsConstant.DVRC5CONDITION;
                    resText = context.getString(R.string.dvrC5);
                    defaultText = resText;
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_excption;
                    object = R.string.object_dvr_excption1;
                    condition = R.string.condition_dvr5;
                }else {
                    //changeDVRStatus(CUSTOME_SWITCH_OFF);
                    exitApp(DVRPackageName);
                    conditionId = TtsConstant.DVRC4CONDITION;
                    resText = context.getString(R.string.dvrC4);
                    defaultText = resText;
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_dvr_app;
                    object = R.string.object_dvr_app2;
                    condition = R.string.condition_default;
                }
                Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
            } else if (BACK.equals(intentEntity.semantic.slots.insType)){
                //返回
                ActivityManagerUtils.getInstance(context).inputKeyBack();
                //text = "好的";
                conditionId = TtsConstant.SYSTEMC22CONDITION;
                resText = context.getString(R.string.systemC22);
                defaultText = resText;
                Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,R.string.skill_system, R.string.scene_homepage, R.string.object_back, R.string.condition_system_default,false);
            } else if (HOMEPAGE.equals(intentEntity.semantic.slots.insType)){
                //返回主界面
                gotoHome();
                //text = "好的";
                conditionId = TtsConstant.SYSTEMC23CONDITION;
                resText = context.getString(R.string.systemC23);
                defaultText = resText;
                Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,R.string.skill_system, R.string.scene_homepage, R.string.object_homepage, R.string.condition_system_default,false);
            } else if (CLOSE_SCREEN.equals(intentEntity.semantic.slots.insType)) {
                closeScreenAndExit();
            } else if (EXIT.equals(intentEntity.semantic.slots.insType)) {
                exit();
                conditionId = TtsConstant.MAINC11CONDITION;
                Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, conditionId, R.string.condition_mainC11);
            } else if(MODE_DAY.equals(insType) || MODE_NIGHT.equals(insType) || MODE_AUTO.equals(insType)) { //白天/黑夜/自动模式
//                doExceptonAction(context);
              if(MODE_DAY.equals(insType)) {
                    showMode = MXSdkManager.SHOW_DAY;
                } else if(MODE_NIGHT.equals(insType)) {
                    showMode = MXSdkManager.SHOW_NIGHT;
                } else if(MODE_AUTO.equals(insType)) {
                    showMode = MXSdkManager.SHOW_AUTO;
                }
                MXSdkManager mxSdkManager = MXSdkManager.getInstance(context);
                mxSdkManager.setShowMode(showMode, new IExtendCallback() {
                    @Override
                    public void success(ExtendBaseModel extendBaseModel) {
                        if(showMode == MXSdkManager.SHOW_DAY) {
                            mxSdkManager.startNaviTTS(context.getString(R.string.map_switch_daytime_ok), TtsConstant.NAVIC68CONDITION);
                            Utils.eventTrack(context, R.string.skill_navi, R.string.scene_navi_map_operation, R.string.object_day, TtsConstant.NAVIC68CONDITION, R.string.condition_navi68);
                        } else if(showMode == MXSdkManager.SHOW_NIGHT) {
                            EventBusUtils.sendNightModeMessage();
                            mxSdkManager.startNaviTTS(context.getString(R.string.map_switch_night_ok), TtsConstant.NAVIC66CONDITION);
                            Utils.eventTrack(context, R.string.skill_navi, R.string.scene_navi_map_operation, R.string.object_night, TtsConstant.NAVIC66CONDITION, R.string.condition_navi66);
                        } else if(showMode == MXSdkManager.SHOW_AUTO) {
                            mxSdkManager.startNaviTTS(context.getString(R.string.map_switch_automatic_ok), "");
                            //Utils.eventTrack(context, R.string.skill_navi, R.string.scene_navi_map_operation, R.string.object_night, TtsConstant.NAVIC66CONDITION, R.string.condition_navi66);
                        }
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        if(showMode == MXSdkManager.SHOW_DAY) {
                            mxSdkManager.startNaviTTS(context.getString(R.string.map_switch_daytime_fail), TtsConstant.NAVIC69CONDITION);
                            Utils.eventTrack(context, R.string.skill_navi, R.string.scene_navi_map_operation, R.string.object_day, TtsConstant.NAVIC69CONDITION, R.string.condition_navi69);
                        } else if(showMode == MXSdkManager.SHOW_NIGHT) {
                            mxSdkManager.startNaviTTS(context.getString(R.string.map_switch_night_fail), TtsConstant.NAVIC67CONDITION);
                            Utils.eventTrack(context, R.string.skill_navi, R.string.scene_navi_map_operation, R.string.object_day, TtsConstant.NAVIC67CONDITION, R.string.condition_navi67);
                        } else if(showMode == MXSdkManager.SHOW_AUTO) {
                            mxSdkManager.startNaviTTS(context.getString(R.string.map_switch_automatic_fail), "");
                            //Utils.eventTrack(context, R.string.skill_navi, R.string.scene_navi_map_operation, R.string.object_day, TtsConstant.NAVIC67CONDITION, R.string.condition_navi67);
                        }
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {
                    }
                });

            }else if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation)
                    &&  SLEEP.equals(insType)) {
                    exit();
            }else if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation)
                    &&  (OPEN_SYSTEM_SET.equals(insType)||OPEN_SETTING_SET.equals(insType))) {
                openSystemSet();
            } else if (PlatformConstant.Operation.INSTRUCTION.equals(intentEntity.operation)
                    &&  (CANCEL.equals(insType)
                    &&isFeedBackWords(intentEntity.text)
                    && TspSceneAdapter.getTspScene(context)==TspSceneAdapter.TSP_SCENE_FEEDBACK)) {   //  人脸识别语义

                    intentEntity.service = PlatformConstant.Service.VIEWCMD; //  人脸识别语义 并且 当前界面处于 人脸识别界面
                    intentEntity.operation = PlatformConstant.Operation.VIEWCMD;
                    Semantic semantic = new Semantic();
                    Semantic.SlotsBean slotsBean = new Semantic.SlotsBean();
                    slotsBean.viewCmd = intentEntity.text;
                    semantic.slots = slotsBean;
                    intentEntity.semantic = semantic;
                    if(ChairController.getInstance(context).isMusicToPlay())
                        ChairController.getInstance(context).srAction(intentEntity);
                    else
                        FeedBackController.getInstance(context).srAction(intentEntity);

            } else {
                doExceptonAction(context);
            }
        } else {
            doExceptonAction(context);
        }

    }

    private boolean isFeedBackWords(String text){
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        if(FeedBackController.getInstance(context).isSureWord(text))
            return true;
        if(FeedBackController.getInstance(context).isDenyWord(text))
            return true;
        return false;
    }

    /*
    *   DVR模式请求
    *   DVR.DVR_MODE_REAL_TIME 实时显示模式
    *   DVR.DVR_MODE_REPLAY 回放模式
    *   DVR.DVR_MODE_SETTING 设置模式
     */
    private void setDVRMode(){
        try{
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntProperty(CarCabinManager.ID_DVR_MODE,VEHICLE_AREA_TYPE_GLOBAL,DVR.DVR_MODE_REAL_TIME);
            Log.d(TAG,"setDVRMode success...");
        }catch (CarNotConnectedException e){
            Log.d(TAG,"setDVRMode fail...");
        }
    }

    private void callTakePhoto(){
        try{
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntProperty(CarCabinManager.ID_DVR_SNAP_SHOOT,VEHICLE_AREA_TYPE_GLOBAL,DVR.DVR_SNAP_SHOOT);
            Log.d(TAG,"callTakePhoto success...");
        }catch (CarNotConnectedException e){
            Log.d(TAG,"callTakePhoto fail...");
        }
    }

    private void callTakeVideo(){
        try{
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntProperty(CarCabinManager.ID_DVR_EMERGENCY_RECORD,VEHICLE_AREA_TYPE_GLOBAL,DVR.DVR_RECORD);
            Log.d(TAG,"callTakeVideo success...");
        }catch (CarNotConnectedException e){
            Log.d(TAG,"callTakeVideo fail...");
        }
    }

    public void checkTakePhotoStatus(int seconds,boolean isSuccess) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isSuccess){
                    CarUtils.takePhotoStatus = 0;
                    conditionId = DVRC1CONDITION;
                    defaultText = context.getString(R.string.dvrC1);
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_dvr;
                    object = R.string.object_dvr;
                    condition = R.string.condition_dvr1;
                }else{
                    CarUtils.takePhotoStatus = 0;
                    conditionId = TtsConstant.DVRC2CONDITION;
                    defaultText = context.getString(R.string.dvrC2);
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_dvr;
                    object = R.string.object_dvr;
                    condition = R.string.condition_dvr2;
                }
                Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC24CONDITION,R.string.condition_null,defaultText,true);

                if(FloatViewManager.getInstance(context).isHide()){
                    showAssistant();

                    Message msg = new Message();
                    Map map = new HashMap();
                    map.put("conditionId",conditionId);
                    map.put("defaultText",defaultText);
                    map.put("appName1",appName);
                    map.put("scene1",scene);
                    map.put("object1",object);
                    map.put("condition1",condition);

                    msg.what = MSG_SHOW_WORD;
                    msg.obj = map;
                    handler.sendMessageDelayed(msg,600);
                }else {
                    Utils.getTtsMessage(context, conditionId, defaultText, "", true,appName,
                            scene, object,condition,false);
                }
            }
        }, seconds);
    }

    public void checkTakeVideoStatus(int seconds,boolean isSuccess) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isSuccess){//1未进入录像；2已开始录像；3录制保存中
                    conditionId = DVRC2_4CONDITION;
                    defaultText = context.getString(R.string.dvrC2_4);
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_dvr;
                    object = R.string.object_dvr;
                    condition = R.string.condition_dvr1;
                }else{
                    conditionId = TtsConstant.DVRC2_5CONDITION;
                    defaultText = context.getString(R.string.dvrC2_5);
                    appName = R.string.skill_dvr;
                    scene = R.string.scene_dvr;
                    object = R.string.object_dvr;
                    condition = R.string.condition_dvr2;
                }
                Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_mvw_dvr, DatastatManager.primitive,R.string.object_take_photo,DatastatManager.response,TtsConstant.MHXC25CONDITION,R.string.condition_null,defaultText,true);

                if(FloatViewManager.getInstance(context).isHide()){
                    showAssistant();

                    Message msg = new Message();
                    Map map = new HashMap();
                    map.put("conditionId",conditionId);
                    map.put("defaultText",defaultText);
                    map.put("appName1",appName);
                    map.put("scene1",scene);
                    map.put("object1",object);
                    map.put("condition1",condition);

                    msg.what = MSG_SHOW_WORD;
                    msg.obj = map;
                    handler.sendMessageDelayed(msg,600);
                }else {
                    Utils.getTtsMessage(context, conditionId, defaultText, "", true,appName,
                            scene, object,condition,false);
                }
            }
        }, seconds);
    }

    /**
     * AVM默认开启设置状态 get
     * 0x1: ON
     * 0x2: OFF
     * @change_mode VehiclePropertyChangeMode:ON_CHANGE
     * @access VehiclePropertyAccess:READ
     * @getValue AVM.AVM_ON_ACK AVM.AVM_OFF_ACK
     */
    public static final int ID_CUSTOME_AVM_ON_OFF_STATUS= 0x2140E104;

    @Override
    public void mvwAction(MvwLParamEntity lParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity o) {

    }

    private static class MyHandler extends Handler {

        private final WeakReference<CMDController> cmdControllerWeakReference;

        private MyHandler(CMDController cmdController) {
            this.cmdControllerWeakReference = new WeakReference<>(cmdController);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final CMDController cmdController = cmdControllerWeakReference.get();
            if(cmdController == null) {
                LogUtils.d(TAG, "cmdController == null");
                return;
            }
            switch (msg.what) {
                case MSG_TTS:
                    cmdController.startTTS((String) msg.obj);
                    break;
            }
        }
    }

    /**
     * 更改亮度
     */
    private void changeBrightness(String insType, String series) {
        String text = "";
        int brightnessValue = 0;
        if (BRIGHTNESS_MAX.equals(insType)) {
            //最大亮度
            //text = "屏幕已经最亮了";
            conditionId = TtsConstant.SYSTEMC1CONDITION;
            resText = context.getString(R.string.systemC1);
            defaultText = resText;
            brightnessValue = BRIGHTNESS_MAX_VALUE / 10;
            appName = R.string.skill_system;
            scene = R.string.scene_bright;
            object = R.string.object_bright_max;
            condition = R.string.condition_system_default;
        } else if (BRIGHTNESS_MIN.equals(insType)) {
            //最小亮度
            //text = "屏幕已经最暗了";
            conditionId = TtsConstant.SYSTEMC2CONDITION;
            resText = context.getString(R.string.systemC2);
            defaultText = resText;
            brightnessValue = BRIGHTNESS_MIN_VALUE;
            appName = R.string.skill_system;
            scene = R.string.scene_bright;
            object = R.string.object_bright_min;
            condition = R.string.condition_system_default;
        } else if (BRIGHTNESS_MINUS.equals(insType)) {
            try {
                //brightnessValue = AppConfig.INSTANCE.mCarMcuManager.getIntProperty(CarMcuManager.ID_DISPLAY_BRIGHTNESS, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL);
                brightnessValue = AppConfig.INSTANCE.mCarPowerManager.getBrightness();
                if (brightnessValue == BRIGHTNESS_MIN_VALUE) {
                    //text = "屏幕已经最暗了";
                    conditionId = TtsConstant.SYSTEMC4CONDITION;
                    resText = context.getString(R.string.systemC4);
                    defaultText = resText;
                    brightnessValue = BRIGHTNESS_MIN_VALUE;
                    appName = R.string.skill_system;
                    scene = R.string.scene_bright;
                    object = R.string.object_bright_minus;
                    condition = R.string.condition_bright_is_min;
                } else {
                    brightnessValue -= 20;
                    if (brightnessValue < BRIGHTNESS_MIN_VALUE) {
                        brightnessValue = BRIGHTNESS_MIN_VALUE;
                    }
                    //text = "已将屏幕调暗";
                    conditionId = TtsConstant.SYSTEMC3CONDITION;
                    resText = context.getString(R.string.systemC3);
                    defaultText = resText;
                    brightnessValue = brightnessValue / 10;
                    appName = R.string.skill_system;
                    scene = R.string.scene_bright;
                    object = R.string.object_bright_minus;
                    condition = R.string.condition_bright_not_min;
                }
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
                doExceptonAction(context);
            }
        } else if (BRIGHTNESS_PLUS.equals(insType)) {
            try {
                //brightnessValue = AppConfig.INSTANCE.mCarMcuManager.getIntProperty(CarMcuManager.ID_DISPLAY_BRIGHTNESS, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL);
                brightnessValue = AppConfig.INSTANCE.mCarPowerManager.getBrightness();
                if (brightnessValue == BRIGHTNESS_MAX_VALUE) {
                    //text = "屏幕已经最亮了";
                    conditionId = TtsConstant.SYSTEMC6CONDITION;
                    resText = context.getString(R.string.systemC6);
                    defaultText = resText;
                    appName = R.string.skill_system;
                    scene = R.string.scene_bright;
                    object = R.string.object_bright_plus;
                    condition = R.string.condition_bright_is_max;
                } else {
                    brightnessValue += 20;
                    if (brightnessValue > BRIGHTNESS_MAX_VALUE) {
                        brightnessValue = BRIGHTNESS_MAX_VALUE;
                    }
                    //text = "已将屏幕调亮";
                    conditionId = TtsConstant.SYSTEMC5CONDITION;
                    resText = context.getString(R.string.systemC5);
                    defaultText = resText;
                    appName = R.string.skill_system;
                    scene = R.string.scene_bright;
                    object = R.string.object_bright_plus;
                    condition = R.string.condition_bright_not_max;
                }
                brightnessValue = brightnessValue / 10;

            } catch (CarNotConnectedException e) {
                e.printStackTrace();
                doExceptonAction(context);
            }

        } else if (BRIGHTNESS_ADJUST.equals(insType)) {
            brightnessValue = Integer.valueOf(series);
            brightnessValue = brightnessValue * 10;
            if (brightnessValue > BRIGHTNESS_MAX_VALUE) {
                brightnessValue = BRIGHTNESS_MAX_VALUE / 10;
                //text = "屏幕已调到最亮，当前亮度为" + brightnessValue;
                conditionId = TtsConstant.SYSTEMC8CONDITION;
                resText = context.getString(R.string.systemC8);
                defaultText = String.format(resText,brightnessValue);
                appName = R.string.skill_system;
                scene = R.string.scene_bright;
                object = R.string.object_bright_x;
                condition = R.string.condition_bright_beyond_max;
            } else if (brightnessValue < BRIGHTNESS_MIN_VALUE) {
                brightnessValue = BRIGHTNESS_MIN_VALUE;
                //text = "屏幕已调到最暗，当前亮度为" + brightnessValue;
                conditionId = TtsConstant.SYSTEMC9CONDITION;
                resText = context.getString(R.string.systemC9);
                defaultText = String.format(resText,brightnessValue);
                appName = R.string.skill_system;
                scene = R.string.scene_bright;
                object = R.string.object_bright_x;
                condition = R.string.condition_bright_beyond_min;
            } else {
                brightnessValue = brightnessValue / 10;
                //text = "已将屏幕亮度调到" + brightnessValue / 10;
                conditionId = TtsConstant.SYSTEMC7CONDITION;
                resText = context.getString(R.string.systemC7);
                defaultText = String.format(resText,brightnessValue);
                appName = R.string.skill_system;
                scene = R.string.scene_bright;
                object = R.string.object_bright_x;
                condition = R.string.condition_bright_is_normal;
            }
            parameter = brightnessValue + "";
        } else {
            doExceptonAction(context);
            return;
        }
        Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);

        try {
            //AppConfig.INSTANCE.mCarMcuManager.setIntProperty(CarMcuManager.ID_DISPLAY_BRIGHTNESS, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL, brightnessValue);
            AppConfig.INSTANCE.mCarPowerManager.setBrightness(brightnessValue * 10);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 更改导航音量
     */
    public void changeMapVoice(Semantic.SlotsBean slotsBean) {
        AppConstant.setMute =false;
        if (slotsBean == null) {
            Log.i(TAG, "slotsBean == null");
            doExceptonAction(context);
            return;
        }
        int volume = getStreamVolume(STREAM_NAVI);
        String text = "";
        TYPE = "导航";
        if (VOLUME_PLUS.equals(slotsBean.insType)) {
            if (volume <= MAX_VOLUME_VALUE - 3) {
                volume += 3;
                //text = "导航音量已调至" + volume;
                if(volume > MAX_VOLUME_VALUE){
                    volume = MAX_VOLUME_VALUE;
                }
                conditionId = TtsConstant.SYSTEMC26CONDITION;
                resText = context.getString(R.string.systemC26);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_navi_plus;
                condition = R.string.condition_navi_less_8;
            } else {
                //text = "导航音量已经最大了，现在音量是10";
                volume = MAX_VOLUME_VALUE;
                conditionId = TtsConstant.SYSTEMC27CONDITION;
                resText = context.getString(R.string.systemC27);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_navi_plus;
                condition = R.string.condition_navi_more_8;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
        } else if (VOLUME_MINUS.equals(slotsBean.insType)) {
            if (volume > 3) {
                volume -= 3;
                //text = "导航音量已调至" + volume;
                conditionId = TtsConstant.SYSTEMC28CONDITION;
                resText = context.getString(R.string.systemC28);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_navi_minus;
                condition = R.string.condition_navi_more_2;
            } else {
                //text = "导航音量已静音";
                volume = 0;
                conditionId = TtsConstant.SYSTEMC29CONDITION;
                resText = context.getString(R.string.systemC29);
                defaultText = resText;
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_navi_minus;
                condition = R.string.condition_navi_less_2;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
        } else if (VOLUME_ADJUST.equals(slotsBean.insType)) {
            volume = Integer.valueOf(slotsBean.series);
            if (volume >= MAX_VOLUME_VALUE) {
                volume = MAX_VOLUME_VALUE;
                //text = "导航音量已调到最大，当前音量为" + volume;
                conditionId = TtsConstant.SYSTEMC43CONDITION;
                resText = context.getString(R.string.systemC43);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_unnormal;
            } else if (volume <= 0) {
                volume = 0;
                //text = "导航音量已调为静音";
                conditionId = TtsConstant.SYSTEMC44CONDITION;
                resText = context.getString(R.string.systemC44);
                defaultText = String.format(resText,TYPE);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_0;
            } else {
                //text = "好的，导航音量已设置为" + volume;
                conditionId = TtsConstant.SYSTEMC42CONDITION;
                resText = context.getString(R.string.systemC42);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_normal;
            }
            parameter = volume + "";
            Utils.getTtsMessageWithTwoParams(context,conditionId,defaultText,TYPE,parameter,true,appName,scene,object,condition);
        } else {
            doExceptonAction(context);
            return;
        }

        //startTTS(text);
        setStreamVolume(STREAM_NAVI, volume);
    }

    /**
     * 更改导航音量
     */
    public void changeMapVoiceMVW(Semantic.SlotsBean slotsBean, boolean isMvw) {
        AppConstant.setMute =false;
        if (slotsBean == null) {
            Log.i(TAG, "slotsBean == null");
            doExceptonAction(context);
            return;
        }
        int volume = getStreamVolume(STREAM_NAVI);
        String text = "";
        TYPE = "导航";
        if (VOLUME_PLUS.equals(slotsBean.insType)) {
            if (volume <= MAX_VOLUME_VALUE - 3) {
                volume += 3;
                if(volume > MAX_VOLUME_VALUE){
                    volume = MAX_VOLUME_VALUE;
                }
                conditionId = TtsConstant.SYSTEMC41_3CONDITION;
                resText = context.getString(R.string.systemC41_3);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_plus;
                condition = R.string.condition_systemC41_3;
            } else {
                volume = MAX_VOLUME_VALUE;
                conditionId = TtsConstant.SYSTEMC41_4CONDITION;
                resText = context.getString(R.string.systemC41_4);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_plus;
                condition = R.string.condition_systemC41_4;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,isMvw);
        } else if (VOLUME_MINUS.equals(slotsBean.insType)) {
            if (volume > 3) {
                volume -= 3;
                conditionId = TtsConstant.SYSTEMC41_7CONDITION;
                resText = context.getString(R.string.systemC41_7);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_minus;
                condition = R.string.condition_systemC41_7;
            } else {
                volume = 0;
                conditionId = TtsConstant.SYSTEMC41_8CONDITION;
                resText = context.getString(R.string.systemC41_8);
                defaultText = resText;
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_minus;
                condition = R.string.condition_systemC41_8;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,isMvw);
        } else {
            doExceptonAction(context);
            return;
        }
        setStreamVolume(STREAM_NAVI, volume);
    }

    private void changePhoneVolume(Semantic.SlotsBean slotsBean) {
        AppConstant.setMute =false;
        if (slotsBean == null) {
            Log.i(TAG, "slotsBean == null");
            doExceptonAction(context);
            return;
        }
        int volume = getStreamVolume(STREAM_PHONE);
        String text = "";
        TYPE = "电话";
        if (VOLUME_PLUS.equals(slotsBean.insType)) {
            if (volume >= PHONE_MAX_VALUE) {
                //text = "电话音量已经最大了，现在音量是" + volume;
                volume = PHONE_MAX_VALUE;
                conditionId = TtsConstant.SYSTEMC31CONDITION;
                resText = context.getString(R.string.systemC31);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_phone_plus;
                condition = R.string.condition_phone_more_40;
            } else {
                volume += 3;
                if (volume > 40) {
                    volume = 40;
                }
                //text = "电话音量已调至" + volume;
                conditionId = TtsConstant.SYSTEMC30CONDITION;
                resText = context.getString(R.string.systemC30);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_phone_plus;
                condition = R.string.condition_phone_less_40;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
        } else if (VOLUME_MINUS.equals(slotsBean.insType)) {
            if (volume <= 0) {
                //text = "电话音量已静音";
                volume = 0;
                conditionId = TtsConstant.SYSTEMC33CONDITION;
                resText = context.getString(R.string.systemC33);
                defaultText = resText;
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_phone_minus;
                condition = R.string.condition_phone_less_3;
            } else {
                volume -= 3;
                if (volume <= 0) {
                    volume = 0;
                    //text = "电话音量已静音";
                    conditionId = TtsConstant.SYSTEMC33CONDITION;
                    resText = context.getString(R.string.systemC33);
                    defaultText = resText;
                    appName = R.string.skill_system;
                    scene = R.string.scene_sound;
                    object = R.string.object_sound_phone_minus;
                    condition = R.string.condition_phone_less_3;
                } else {
                    //text = "电话音量已调至" + volume;
                    conditionId = TtsConstant.SYSTEMC32CONDITION;
                    resText = context.getString(R.string.systemC32);
                    defaultText = String.format(resText,volume);
                    appName = R.string.skill_system;
                    scene = R.string.scene_sound;
                    object = R.string.object_sound_phone_minus;
                    condition = R.string.condition_phone_more_3;
                }
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
        } else if (VOLUME_ADJUST.equals(slotsBean.insType)) {
            volume = Integer.valueOf(slotsBean.series);
            if (volume > PHONE_MAX_VALUE) {
                volume = PHONE_MAX_VALUE;
                //text = "电话音量已调到最大，当前音量为" + volume;
                conditionId = TtsConstant.SYSTEMC43CONDITION;
                resText = context.getString(R.string.systemC43);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_unnormal;
            } else if (volume <= PHONE_MIN_VALUE) {
                volume = PHONE_MIN_VALUE;
                //text = "电话音量已调为静音";
                conditionId = TtsConstant.SYSTEMC44CONDITION;
                resText = context.getString(R.string.systemC44);
                defaultText = String.format(resText,TYPE);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_0;
            } else {
                //text = "好的，电话音量已设置为" + volume;
                conditionId = TtsConstant.SYSTEMC42CONDITION;
                resText = context.getString(R.string.systemC42);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_normal;
            }
            parameter = volume + "";
            Utils.getTtsMessageWithTwoParams(context,conditionId,defaultText,TYPE,parameter,true,appName,scene,object,condition);
        } else {
            doExceptonAction(context);
            return;
        }
        //startTTS(text);
        setStreamVolume(STREAM_PHONE, volume);
    }


    public void changeMediaVolume(Semantic.SlotsBean slotsBean,boolean isMvw) {
        AppConstant.setMute =false;
        if (slotsBean == null) {
            Log.i(TAG, "slotsBean == null");
            doExceptonAction(context);
            return;
        }
        int volume = getStreamVolume(STREAM_MEDIA);
        String text = "";
        TYPE = "媒体";
        if(TextUtils.isEmpty(slotsBean.tag)){
            TYPE = "";
        }
        if (VOLUME_PLUS.equals(slotsBean.insType)) {
            if (volume >= 40) {
                //text = "媒体音量已经最大了，现在音量是" + volume;
                volume = 40;
                conditionId = TtsConstant.SYSTEMC39CONDITION;
                resText = context.getString(R.string.systemC39);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_media_plus;
                condition = R.string.condition_media_more_37;
            } else {
                volume += 3;
                if (volume > 40) {
                    volume = 40;
                }
                //text = "媒体音量已调至" + volume;
                conditionId = TtsConstant.SYSTEMC38CONDITION;
                resText = context.getString(R.string.systemC38);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_media_plus;
                condition = R.string.condition_media_less_37;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,isMvw);
        } else if (VOLUME_MINUS.equals(slotsBean.insType)) {
            if (volume <= 0) {
                //text = "媒体音量已静音";
                conditionId = TtsConstant.SYSTEMC41CONDITION;
                resText = context.getString(R.string.systemC41);
                defaultText = resText;
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_media_minus;
                condition = R.string.condition_media_less_3;
            } else {
                volume -= 3;
                if (volume <= 0) {
                    volume = 0;
                    //text = "媒体音量已静音";
                    conditionId = TtsConstant.SYSTEMC41CONDITION;
                    resText = context.getString(R.string.systemC41);
                    defaultText = resText;
                    appName = R.string.skill_system;
                    scene = R.string.scene_sound;
                    object = R.string.object_sound_media_minus;
                    condition = R.string.condition_media_less_3;
                } else {
                    //text = "媒体音量已调至" + volume;
                    conditionId = TtsConstant.SYSTEMC40CONDITION;
                    resText = context.getString(R.string.systemC40);
                    defaultText = String.format(resText,volume);
                    appName = R.string.skill_system;
                    scene = R.string.scene_sound;
                    object = R.string.object_sound_media_minus;
                    condition = R.string.condition_media_more_3;
                }
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,isMvw);
        } else if (VOLUME_ADJUST.equals(slotsBean.insType)) {
            volume = Integer.valueOf(slotsBean.series);
            if (volume > 40) {
                volume = 40;
                //text = "媒体音量已调到最大，当前音量为" + volume;
                conditionId = TtsConstant.SYSTEMC43CONDITION;
                resText = context.getString(R.string.systemC43);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_unnormal;
            } else if (volume <= 0) {
                volume = 0;
                //text = "媒体音量已调为静音";
                conditionId = TtsConstant.SYSTEMC44CONDITION;
                resText = context.getString(R.string.systemC44);
                defaultText = String.format(resText,TYPE);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_0;
            } else {
                //text = "好的，媒体音量已设置为" + volume;
                conditionId = TtsConstant.SYSTEMC42CONDITION;
                resText = context.getString(R.string.systemC42);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_normal;
            }

            parameter = volume + "";
            Utils.getTtsMessageWithTwoParams(context,conditionId,defaultText,TYPE,parameter,true,appName,scene,object,condition);
        } else {
            doExceptonAction(context);
            return;
        }
        //startTTS(text);
        setStreamVolume(STREAM_MEDIA, volume);
    }

    public void changeMediaVolumeMVW(Semantic.SlotsBean slotsBean,boolean isMvw) {
        AppConstant.setMute =false;
        if (slotsBean == null) {
            Log.i(TAG, "slotsBean == null");
            doExceptonAction(context);
            return;
        }
        int volume = getStreamVolume(STREAM_MEDIA);
        TYPE = "媒体";
        if(TextUtils.isEmpty(slotsBean.tag)){
            TYPE = "";
        }
        if (VOLUME_PLUS.equals(slotsBean.insType)) {
            if (volume >= 40) {
                volume = 40;
                conditionId = TtsConstant.SYSTEMC41_2CONDITION;
                resText = context.getString(R.string.systemC41_2);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_plus;
                condition = R.string.condition_systemC41_2;
            } else {
                volume += 3;
                if (volume > 40) {
                    volume = 40;
                }
                conditionId = TtsConstant.SYSTEMC41_1CONDITION;
                resText = context.getString(R.string.systemC41_1);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_plus;
                condition = R.string.condition_systemC41_1;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,isMvw);
        } else if (VOLUME_MINUS.equals(slotsBean.insType)) {
            if (volume <= 3) {
                volume = 0;
                conditionId = TtsConstant.SYSTEMC41_6CONDITION;
                resText = context.getString(R.string.systemC41_6);
                defaultText = resText;
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_minus;
                condition = R.string.condition_systemC41_6;
            } else {
                volume -= 3;
                conditionId = TtsConstant.SYSTEMC41_5CONDITION;
                resText = context.getString(R.string.systemC41_5);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_minus;
                condition = R.string.condition_systemC41_5;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,isMvw);
        } else {
            doExceptonAction(context);
            return;
        }
        setStreamVolume(STREAM_MEDIA, volume);
    }

    private void changeSystemVolume(Semantic.SlotsBean slotsBean) {
        AppConstant.setMute =false;
        if (slotsBean == null) {
            Log.i(TAG, "slotsBean == null");
            doExceptonAction(context);
            return;
        }
        int volume = getStreamVolume(STREAM_SYSTEM);
        String text = "";
        TYPE = "小欧";
        if (VOLUME_PLUS.equals(slotsBean.insType)) {
            if (volume >= 40) {
                //text = "系统音量已经最大了，现在音量是" + volume;
                volume = 40;
                conditionId = TtsConstant.SYSTEMC35CONDITION;
                resText = context.getString(R.string.systemC35);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_system_plus;
                condition = R.string.condition_system_more_46;
            } else {
                volume += 3;
                if (volume > 40) {
                    volume = 40;
                }
                //text = "系统音量已调至" + volume;
                conditionId = TtsConstant.SYSTEMC34CONDITION;
                resText = context.getString(R.string.systemC34);
                defaultText = String.format(resText,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_system_plus;
                condition = R.string.condition_system_less_46;
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
        } else if (VOLUME_MINUS.equals(slotsBean.insType)) {
            if (volume <= 0) {
               // text = "系统音量已静音";
                conditionId = TtsConstant.SYSTEMC37CONDITION;
                resText = context.getString(R.string.systemC37);
                defaultText = resText;
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_system_minus;
                condition = R.string.condition_system_less_3;
            } else {
                volume -= 3;
                if (volume <= 0) {
                    volume = 0;
                    //text = "系统音量已静音";
                    conditionId = TtsConstant.SYSTEMC37CONDITION;
                    resText = context.getString(R.string.systemC37);
                    defaultText = resText;
                    appName = R.string.skill_system;
                    scene = R.string.scene_sound;
                    object = R.string.object_sound_system_minus;
                    condition = R.string.condition_system_less_3;
                } else {
                    //text = "系统音量已调至" + volume;
                    conditionId = TtsConstant.SYSTEMC36CONDITION;
                    resText = context.getString(R.string.systemC36);
                    defaultText = String.format(resText,volume);
                    appName = R.string.skill_system;
                    scene = R.string.scene_sound;
                    object = R.string.object_sound_system_minus;
                    condition = R.string.condition_system_more_3;
                }
            }
            parameter = volume + "";
            Utils.getTtsMessage(context,conditionId,defaultText,parameter,true,appName,scene,object,condition,false);
        } else if (VOLUME_ADJUST.equals(slotsBean.insType)) {
            volume = Integer.valueOf(slotsBean.series);
            if (volume > 40) {
                volume = 40;
                //text = "系统音量已调到最大，当前音量为" + volume;
                conditionId = TtsConstant.SYSTEMC43CONDITION;
                resText = context.getString(R.string.systemC43);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_unnormal;
            } else if (volume <= 0) {
                volume = 0;
                //text = "系统音量已调为静音";
                conditionId = TtsConstant.SYSTEMC44CONDITION;
                resText = context.getString(R.string.systemC44);
                defaultText = String.format(resText,TYPE);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_0;
            } else {
                //text = "好的，系统音量已设置为" + volume;
                conditionId = TtsConstant.SYSTEMC42CONDITION;
                resText = context.getString(R.string.systemC42);
                defaultText = String.format(resText,TYPE,volume);
                appName = R.string.skill_system;
                scene = R.string.scene_sound;
                object = R.string.object_sound_x;
                condition = R.string.condition_sound_normal;
            }
            parameter = volume + "";
            Utils.getTtsMessageWithTwoParams(context,conditionId,defaultText,TYPE,parameter,true,appName,scene,object,condition);
        } else {
            doExceptonAction(context);
            return;
        }
        //startTTS(text);
        setStreamVolume(STREAM_SYSTEM, volume);

    }

    //打开XX音量
    private void openVolume(Semantic.SlotsBean slotsBean) {
        int streamType = 0;
        int currentVolume = 0;
        int lastVolume = 0;
        AppConstant.setMute =false;
        if (slotsBean == null) {
            Log.i(TAG, "slotsBean == null");
            doExceptonAction(context);
            return;
        }
        if (MEDIA.equals(slotsBean.tag)) {// 媒体音量
            streamType = STREAM_MEDIA;
            TYPE = "媒体";
            currentVolume = getStreamVolume(STREAM_MEDIA);
            lastVolume = SharedPreferencesUtils.getInt(context, AppConstant.KEY_MEDIA_VOLUME, 0);
        } else if (SYSTEM.equals(slotsBean.tag) || XIAOOU.equals(slotsBean.tag)) {// 系统音量
            streamType = STREAM_SYSTEM;
            TYPE = "小欧";
            currentVolume = getStreamVolume(STREAM_SYSTEM);
            lastVolume = SharedPreferencesUtils.getInt(context, AppConstant.KEY_SYSTEM_VOLUME, 0);
        } else if (MAP_U.equals(slotsBean.tag)) {// 导航音量
            streamType = STREAM_NAVI;
            TYPE = "导航";
            currentVolume = getStreamVolume(STREAM_NAVI);
            lastVolume = SharedPreferencesUtils.getInt(context, AppConstant.KEY_MAPU_VOLUME, 0);
        } else if (TELEPHONE.equals(slotsBean.tag)) {// 电话音量
            streamType = STREAM_PHONE;
            TYPE = "电话";
            currentVolume = getStreamVolume(STREAM_PHONE);
            lastVolume = SharedPreferencesUtils.getInt(context, AppConstant.KEY_PHONE_VOLUME, 0);
        } else {
            doExceptonAction(context);
            return;
        }

        Log.d(TAG,"before: currentVolume = " + currentVolume + ",lastVolume = " + lastVolume);
        if(streamType == STREAM_PHONE || streamType == STREAM_SYSTEM || streamType == STREAM_MEDIA ){
            if (currentVolume == 0) {
                if(lastVolume == 0){
                    lastVolume += 3;
                }
                if (lastVolume > 40) {
                    lastVolume = 40;
                }
                setStreamVolume(streamType,lastVolume);
            } else {
                currentVolume += 3;
                if (currentVolume > 40) {
                    currentVolume = 40;
                }
                setStreamVolume(streamType,currentVolume);
            }
        }else if(streamType == STREAM_NAVI){//导航
            if (currentVolume == 0) {
                if(lastVolume == 0){
                    lastVolume += 2;
                }
                if (lastVolume > 10) {
                    lastVolume = 10;
                }
                setStreamVolume(streamType,lastVolume);
            } else {
                currentVolume += 2;
                if (currentVolume > 10) {
                    currentVolume = 10;
                }
                setStreamVolume(streamType,currentVolume);
            }
        }
        Log.d(TAG,"after: currentVolume = " + currentVolume + ",lastVolume = " + lastVolume);
        conditionId = TtsConstant.SYSTEMC442CONDITION;
        resText = context.getString(R.string.systemC44_2);
        defaultText = String.format(resText, TYPE);
        Utils.getTtsMessageWithTwoParams(context,conditionId,defaultText,TYPE,parameter,true,R.string.skill_system, R.string.scene_sound, R.string.object_sound_open, R.string.condition_system_default);
    }

    //关闭XX音量
    private void closeVolume(Semantic.SlotsBean slotsBean) {
        int streamType = STREAM_MEDIA;
        int volume = 0;
        if (MEDIA.equals(slotsBean.tag)) {// 媒体音量
            streamType = STREAM_MEDIA;
            TYPE = "媒体";
            volume = getStreamVolume(STREAM_MEDIA);
            if(volume != 0)SharedPreferencesUtils.saveInt(context,AppConstant.KEY_MEDIA_VOLUME,volume);
        } else if (SYSTEM.equals(slotsBean.tag) || XIAOOU.equals(slotsBean.tag)) {// 系统音量
            streamType = STREAM_SYSTEM;
            TYPE = "小欧";
            volume = getStreamVolume(STREAM_SYSTEM);
            if(volume != 0)SharedPreferencesUtils.saveInt(context,AppConstant.KEY_SYSTEM_VOLUME,volume);
        } else if (MAP_U.equals(slotsBean.tag)) {// 导航音量
            streamType = STREAM_NAVI;
            TYPE = "导航";
            volume = getStreamVolume(STREAM_NAVI);
            if(volume != 0)SharedPreferencesUtils.saveInt(context,AppConstant.KEY_MAPU_VOLUME,volume);
        } else if (TELEPHONE.equals(slotsBean.tag)) {// 电话音量
            streamType = STREAM_PHONE;
            TYPE = "电话";
            volume = getStreamVolume(STREAM_PHONE);
            if(volume != 0) SharedPreferencesUtils.saveInt(context,AppConstant.KEY_PHONE_VOLUME,volume);
        } else {
            doExceptonAction(context);
            return;
        }
        Log.d(TAG,"record volume = " + volume);
        setStreamVolume(streamType, 0);
        conditionId = TtsConstant.SYSTEMC441CONDITION;
        resText = context.getString(R.string.systemC44_1);
        defaultText = String.format(resText,TYPE);
        Utils.getTtsMessageWithTwoParams(context,conditionId,defaultText,TYPE,parameter,true,R.string.skill_system, R.string.scene_sound, R.string.object_sound_close, R.string.condition_system_default);
    }

    public final static int STREAM_SYSTEM = AudioAttributes.USAGE_NOTIFICATION;
    public final static int STREAM_MEDIA = AudioAttributes.USAGE_MEDIA;
    public final static int STREAM_PHONE = AudioAttributes.USAGE_VOICE_COMMUNICATION;
    public final static int STREAM_NAVI = AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE;

    /**
     * 获取最大音量
     *
     * @param type
     */
    private int getStreamMaxVolume(int type) {
        try {
            return AppConfig.INSTANCE.mCarAudioManager.getGroupMaxVolume(
                    AppConfig.INSTANCE.mCarAudioManager.getVolumeGroupIdForUsage(type));
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取音量
     *
     * @param type
     */
    private int getStreamVolume(int type) {
        try {
            return AppConfig.INSTANCE.mCarAudioManager.getGroupVolume(
                    AppConfig.INSTANCE.mCarAudioManager.getVolumeGroupIdForUsage(type));
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setStreamVolume(int type, int volume) {
        try {
            AppConfig.INSTANCE.mCarAudioManager.setGroupVolume(
                    AppConfig.INSTANCE.mCarAudioManager.getVolumeGroupIdForUsage(type), volume, 0);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
//        VolumeView vv = new VolumeView(context,new Handler());
//        vv.show(type,volume);
    }

    /**
     * 更改行车记录仪状态
     * int staus 打开 或 关闭
     */
    private void changeDVRStatus(int staus) {
        try {
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntProperty(CarCabinManager.ID_DVR_REC_SWITCH,VEHICLE_AREA_TYPE_GLOBAL,staus);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 行车记录仪是否打开
     *
     */
    private boolean isDVRUsable() {
        try {
            int staus = AppConfig.INSTANCE.mCarCabinManager.
                    getIntProperty(CarCabinManager.ID_DVR_REC_SWITCH,VEHICLE_AREA_TYPE_GLOBAL);
            Log.d(TAG, "isDVRUsable: staus: " + staus);
            if(staus == DVR.DVR_ON){
                return true;
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 行车记录仪 的照相结果
     *
     */
    private int getTakePhotoStatus() {
//        try {
//            //todo: 待确认
//            int staus = AppConfig.INSTANCE.mCarCabinManager.
//                    getIntProperty(CarCabinManager.ID_DVR_SNAPSHOOT_VIDEO_ACK,VEHICLE_AREA_TYPE_GLOBAL);
//            Log.d(TAG,"isTakePhotoSuccess staus = " + staus);
//            if(staus == DVR.DVR_SNAPSHOOT_VIDEO_HU_PHOTO_SUCCESS || staus == DVR.DVR_SNAPSHOOT_VIDEO_TBOX_PHOTO_SUCCESS){
//                return true;
//            }
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG,"takePhotoStatus = " + CarUtils.takePhotoStatus);
        return CarUtils.takePhotoStatus;
    }

    private int getTakeVideoStatus() {
//        try {
//            //todo: 待确认
//            int staus = AppConfig.INSTANCE.mCarCabinManager.
//                    getIntProperty(CarCabinManager.ID_DVR_SNAPSHOOT_VIDEO_ACK,VEHICLE_AREA_TYPE_GLOBAL);
//            Log.d(TAG,"isTakePhotoSuccess staus = " + staus);
//            if(staus == DVR.DVR_SNAPSHOOT_VIDEO_HU_PHOTO_SUCCESS || staus == DVR.DVR_SNAPSHOOT_VIDEO_TBOX_PHOTO_SUCCESS){
//                return true;
//            }
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG,"takeVideoStatus = " + CarUtils.takeVideoStatus);
        return CarUtils.takeVideoStatus;
    }

    /*
    * 判断是否安装了行车记录仪
    * Context context上下文
    * String pkgName应用包名
    * 返回：true为安装了，false为未安装
    * */
    private boolean checkAppInstalled(Context context,String pkgName) {
        if (pkgName== null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if(packageInfo == null) {
            return false;
        } else {
            return true;//true为安装了，false为未安装
        }
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broad);
    }
}
