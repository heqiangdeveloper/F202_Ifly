package com.chinatsp.ifly.voice.platformadapter.controller;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.constant.VEHICLE;
import android.car.hardware.hvac.CarHvacManager;
import android.car.hardware.mcu.CarMcuManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IAppInterface;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.SettingsUtil;
import com.chinatsp.ifly.voiceadapter.Business;
import com.chinatsp.phone.transact.BTPhoneManagerProxy;
import com.example.loginarar.LoginManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC90CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.APPCENTERC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.APPCENTERC2CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CARCONDITIONC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CARCONDITIONC2CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CARCONTROLC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CARCONTROLC2CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CARSETTINGC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CHANGBAC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.FILEMANAGEMENTC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.FILEMANAGEMENTC2CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.PHONEC42CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.PHONEC43CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SERVICEMARTC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SERVICEMARTC2CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.USERCENTERC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.USERCENTERC2CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.VIDEOC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.VIDEOC2CONDITION;

public class AppController extends BaseController implements IAppInterface {
    private final static String TAG = "AppController";
    private Context context;
    private WifiManager wifiManager;
    private CarSensorManager mCarSensorManager = null;
    private CarHvacManager mCarHvacManager = null;
    private static int powerStatus = -1;
    private static final String BLE = "??????";
    private static final String BLE_SET = "????????????";
    private static final String WIFI = "wifi";
    private static final String BLE_OPEN_TEXT = "????????????";
    private static final String BLE_EXIT_TEXT = "????????????";
    private static final String SYSTEM_SET = "????????????";
    private static final String SET = "??????";
    private static final String NET = "??????";
    private static final String NET_SET = "????????????";
    private static final String NET_EXIT_TEXT = "????????????";
    private static final String MOBILE_NET = "????????????";
    private static final String DISPLAY = "????????????";
    private static final String VOICE = "????????????";
    private static final String EFFECT = "??????";
    private static final String SYSTEM_UPDATE = "????????????";
    private static final String SYSTEM_RESET = "????????????";
    private static final String VIDEO = "??????";
    private static final String TIRE= "????????????";
    private static final String FACTORY= "????????????";
    private static final String FACTORY_SET= "????????????";
    private static final String CAR_SETTINGS= "????????????";
    //???????????? ?????? 0 ???????????????  1?????????
    public static final String FACE_TYPE = "persist.vendor.vehicle.face";
    //???????????????
    public static final String PACKAGE_NAME_NAVI = "com.tencent.wecarnavi";
    //???????????????action
    public static final String OPEN_VC_FROM_OTHERS = "open_vc_from_others";
    //????????????app key
    public static final String EXTRA_KEY_OPEN_VC = "open_vc";
    //????????????frag value
    public static final String EXTRA_OPEN_SETTING_VALUE_VC = "frag_vc";
    //????????????frag value
    public static final String EXTRA_OPEN_SETTING_VALUE_CT = "frag_ct";
    //??????????????????frag value
    public static final String EXTRA_OPEN_SETTING_VALUE_UC = "frag_uc";
    //????????????type key
    public static final String EXTRA_KEY_OPEN_VC_TYPE = "vc_type";
    // ????????????
    public static final int OPEN_VC_TYPE_FAST_CONTROL = 20;

    public static final int OPEN_VC_TYPE_CAR_SETTINGS = 22;

    //????????????type key
    public static final String EXTRA_KEY_OPEN_CT_TYPE = "ct_type";

    //????????????
    public static final int OPEN_CT_TYPE_PRESSURE = 11;

    private ISpeechControlService mSpeechService;

    private static final int OPEN_WIFI = 1001;
    private static final int OPEN_BT = 1002;
    private static final int CHECK_CLOUDEYE_OPEN = 1003;
    private static final int CHECK_CLOUDEYE_EXIT = 1004;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OPEN_WIFI:
                    wifiManager.setWifiEnabled(true);
                    break;
                case OPEN_BT:
                    BluetoothAdapter.getDefaultAdapter().enable();
                    BTPhoneManagerProxy.getInstance().sayOpenBT();
                    break;
                case CHECK_CLOUDEYE_EXIT:
                    IntentEntity intentEntityExit = (IntentEntity) msg.obj;
                    int statusExit = CarUtils.getInstance(context).getCloudEyeUsage();
                    Log.d(TAG, "statusExit = " + statusExit);
                    if(statusExit == VEHICLE.NO_REQUEST){//??????????????????????????????????????????
                        Utils.getMessageWithTtsSpeakOnly(context, TtsConstant.BACKVISIONC2_1, context.getString(R.string.backvisionC2_1), exitListener);
                        Utils.eventTrack(context, R.string.skill_cloun_eye, R.string.scene_eye_exception, R.string.object_no_eye, TtsConstant.BACKVISIONC2_1, R.string.condition_backvisionC2_1,context.getString(R.string.backvisionC2_1));
                    }else {
                        try {
                            Utils.getMessageWithTtsSpeakOnly(context, TtsConstant.BACKVISIONC2, context.getString(R.string.backvisionC2), exitListener);
                            Utils.eventTrack(context, R.string.skill_cloun_eye, R.string.scene_close_eye, R.string.object_open_close_eye, TtsConstant.BACKVISIONC2, R.string.condition_default,context.getString(R.string.backvisionC2));
                            mSpeechService.dispatchSRAction(Business.CLOUDEYE, intentEntityExit.convert2NlpVoiceModel());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case CHECK_CLOUDEYE_OPEN:
                    IntentEntity intentEntityOpen = (IntentEntity) msg.obj;
                    int statusOpen = CarUtils.getInstance(context).getCloudEyeUsage();
                    Log.d(TAG, "statusOpen = " + statusOpen);
                    if(statusOpen == VEHICLE.NO_REQUEST){//??????????????????????????????????????????
                        Utils.getMessageWithTtsSpeakOnly(context, TtsConstant.BACKVISIONC2_1, context.getString(R.string.backvisionC2_1), exitListener);
                        Utils.eventTrack(context, R.string.skill_cloun_eye, R.string.scene_eye_exception, R.string.object_no_eye, TtsConstant.BACKVISIONC2_1, R.string.condition_backvisionC2_1,context.getString(R.string.backvisionC2_1));
                    }else {
                        try {
                            Utils.getMessageWithTtsSpeakOnly(context, TtsConstant.BACKVISIONC1, context.getString(R.string.backvisionC1), exitListener);
                            Utils.eventTrack(context, R.string.skill_cloun_eye, R.string.scene_open_eye, R.string.object_open_close_eye, TtsConstant.BACKVISIONC1, R.string.condition_default,context.getString(R.string.backvisionC1));
                            mSpeechService.dispatchSRAction(Business.CLOUDEYE, intentEntityOpen.convert2NlpVoiceModel());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public AppController(Context context, ISpeechControlService speechService) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mSpeechService = speechService;
    }

    private TTSController.OnTtsStoppedListener exitListener = new TTSController.OnTtsStoppedListener() {
        @Override
        public void onPlayStopped() {
            if (!FloatViewManager.getInstance(context).isHide()) {
                FloatViewManager.getInstance(context).hide();
            }
        }
    };

    @Override
    public void srAction(IntentEntity intentEntity) {
        String defaultText = "";
        String conditionId = "";
        int hasFace = 0;
        if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.name!=null){
            if("????????????".equals(intentEntity.semantic.slots.name)
                    ||"????????????????????????".equals(intentEntity.semantic.slots.name)
                    ||"??????????????????".equals(intentEntity.semantic.slots.name))
                intentEntity.semantic.slots.name = "??????????????????";
        }
        if (PlatformConstant.Operation.LAUNCH.equals(intentEntity.operation)) {
            if (BLE.equals(intentEntity.semantic.slots.name)||BLE_SET.equals(intentEntity.semantic.slots.name)) { //??????
                if (BLE_OPEN_TEXT.equals(intentEntity.text)) {//????????????
                    conditionId = TtsConstant.SYSTEMC10CONDITION;
                    String[] texts = context.getResources().getStringArray(R.array.systemC10);
                    int i = new Random().nextInt(texts.length);
                    defaultText = texts[i];
                    Utils.eventTrack(context, R.string.skill_system, R.string.scene_bt, R.string.object_bt_open, conditionId, R.string.condition_system_default);
                } else {//??????????????????
                    conditionId = TtsConstant.SYSTEMC24CONDITION;
                    defaultText = context.getString(R.string.systemC24);
                    Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
                }
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_BLUETOOTH);
                handler.sendEmptyMessageDelayed(OPEN_BT, 1500);
            } else if (WIFI.equals(intentEntity.semantic.slots.name)) { //??????wifi
                conditionId = TtsConstant.SYSTEMC14CONDITION;
                String[] texts = context.getResources().getStringArray(R.array.systemC14);
                int i = new Random().nextInt(texts.length);
                defaultText = texts[i];
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_NETWORK);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_wifi, R.string.object_wifi_open, conditionId, R.string.condition_system_default);
                handler.sendEmptyMessageDelayed(OPEN_WIFI, 1500);
            } else if (SYSTEM_SET.equals(intentEntity.semantic.slots.name) || SET.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(null);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            } else if (NET.equals(intentEntity.semantic.slots.name)||NET_SET.equals(intentEntity.semantic.slots.name)) { //????????????
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_NETWORK);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            } else if (MOBILE_NET.equals(intentEntity.semantic.slots.name)) { //??????????????????
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_NETWORK_TBOX);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            } else if (DISPLAY.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_DISPLAY);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            } else if (VOICE.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_AUDIO);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            } else if (EFFECT.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_AUDIO);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            }/* else if (SYSTEM_UPDATE.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_SYSTEM_UPGRADE);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            } */else if (SYSTEM_RESET.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.SYSTEMC24CONDITION;
                defaultText = context.getString(R.string.systemC24);
                startSetting(SettingsUtil.EXTRA_OPEN_SETTING_VALUE_FACTORY_RESET);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_open, conditionId, R.string.condition_system_default);
            } else if ("?????????".equals(intentEntity.semantic.slots.name)||"????????????".equalsIgnoreCase(intentEntity.semantic.slots.name)) {
                BluePhoneManager.getInstance(context).openBtPhoneTab(BluePhoneManager.TAB_CONTACTS);
                Utils.getMessageWithTtsSpeakOnly(context, PHONEC42CONDITION, context.getString(R.string.app_open_success), exitListener);
                Utils.eventTrack(context, R.string.skill_phone, R.string.scene_phone_contact_ope, R.string.object_contact_ope_1, TtsConstant.PHONEC42CONDITION,  R.string.condition_phoneC42);
            } else if ("??????".equals(intentEntity.semantic.slots.name) || "??????".equals(intentEntity.semantic.slots.name)
                    || "???????????????".equals(intentEntity.semantic.slots.name) /*|| "??????".equals(intentEntity.semantic.slots.name)*/ || "????????????".equals(intentEntity.semantic.slots.name)
                    ||"????????????".equalsIgnoreCase(intentEntity.semantic.slots.name)
                    ||"??????".equalsIgnoreCase(intentEntity.semantic.slots.name)
                    ||CAR_SETTINGS.equalsIgnoreCase(intentEntity.semantic.slots.name)) {
                if ("??????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.CARCONTROLC1CONDITION;
                    defaultText = context.getString(R.string.systemC24);
                    openCarController(EXTRA_OPEN_SETTING_VALUE_VC);
                    Utils.eventTrack(context, R.string.skill_car_control, R.string.scene_car_control, R.string.object_car_control1, CARCONTROLC1CONDITION, R.string.condition_default);
                } else if ("??????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.CARCONDITIONC1CONDITION;
                    defaultText = context.getString(R.string.systemC24);
                    openCarController(EXTRA_OPEN_SETTING_VALUE_CT);
                    Utils.eventTrack(context, R.string.skill_car_condition, R.string.scene_car_condition, R.string.object_car_condition1, CARCONDITIONC1CONDITION, R.string.condition_default);
                } else if ("???????????????".equals(intentEntity.semantic.slots.name) || "????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.USERCENTERC1CONDITION;
                    defaultText = context.getString(R.string.systemC24);
                    openCarController(EXTRA_OPEN_SETTING_VALUE_UC);
                    Utils.eventTrack(context, R.string.skill_usercenter, R.string.scene_usercenter, R.string.object_usercenter1, USERCENTERC1CONDITION, R.string.condition_default);
                }else if (CAR_SETTINGS.equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.CARSETTINGC1CONDITION;
                    defaultText = context.getString(R.string.carsettingC1);
                    openCarController(EXTRA_OPEN_SETTING_VALUE_VC,OPEN_VC_TYPE_CAR_SETTINGS);
                    Utils.eventTrack(context, R.string.skill_car_settings, R.string.scene_car_settings, R.string.object_carsettingC1, CARSETTINGC1CONDITION, R.string.condition_default);
                } /*else if ("??????".equals(intentEntity.semantic.slots.name) || "????????????".equals(intentEntity.semantic.slots.name)) {
                    startApp("??????");
                    String userToken = LoginManager.getInstance().getUserToken();
                    Log.d(TAG,"---------------lh----userToken:"+userToken);
                    if (TextUtils.isEmpty(userToken)) {//?????????
                        conditionId = TtsConstant.MAINC20CONDITION;
                        defaultText = context.getString(R.string.user_no_login_guide);
                        Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_nologin, R.string.object_main_nologin, conditionId, R.string.condition_mainC20);
                    } else {
                        conditionId = TtsConstant.CHEXINC40CONDITION;
                        defaultText = context.getString(R.string.systemC24);
                        Utils.eventTrack(context, R.string.skill_chexin, R.string.scene_open_close_cheixn, R.string.open_chexin, conditionId, R.string.condition_chexinC40);
                    }
                }*/else if ("????????????".equals(intentEntity.semantic.slots.name) || "??????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.TPMC1CONDITION;
                    defaultText = context.getString(R.string.tpmC1);
                    openCarStatuController(EXTRA_OPEN_SETTING_VALUE_CT,OPEN_CT_TYPE_PRESSURE);
                    Utils.eventTrack(context, R.string.skill_tpm, R.string.scene_tpm, R.string.object_tpm1, conditionId, R.string.condition_default);
                }
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
            } else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                if (mSpeechService != null) {
                    try {
                        mSpeechService.dispatchSRAction(Business.MUSIC, intentEntity.convert2NlpVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        doExceptonAction(context);
                    }
                } else {
                    doExceptonAction(context);
                }
            }else if ("QQ??????".equalsIgnoreCase(intentEntity.semantic.slots.name)){
                Log.e("zheng","zheng  QQ?????? ");
             /*   MusicController musicController = new MusicController(context);
                musicController.srAction(intentEntity);*/

                try {
                    if(mSpeechService!=null){
                        mSpeechService.dispatchSRAction(Business.MUSIC,openMusicByVoiceModel());
                    }else {
                        doExceptonAction(context);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    doExceptonAction(context);
                }

            }
            else if ("????????????".equalsIgnoreCase(intentEntity.semantic.slots.name)){
//                Utils.getMessageWithTtsSpeakOnly(context, VIDEOC1CONDITION, context.getString(R.string.systemC24), exitListener);
//                Utils.eventTrack(context, R.string.skill_radio, R.string.scene_radio, R.string.object_radio1, VIDEOC1CONDITION, R.string.condition_default);
//                startApp("??????");
                try {
                    if(mSpeechService!=null){
                        if(intentEntity.text!=null&&intentEntity.text.contains("??????????????????"))//????????????????????????????????????
                            mSpeechService.dispatchSRAction(Business.RADIO,playRadioByVoiceModel());
                        else if(intentEntity.text!=null&&intentEntity.text.contains("??????????????????")){//????????????????????????????????????
//                            mSpeechService.dispatchSRAction(Business.RADIO,playRadioByVoiceModel());
                            Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), new TTSController.OnTtsStoppedListener() {
                                @Override
                                public void onPlayStopped() {
                                    Utils.exitVoiceAssistant();
                                    startApp(intentEntity.semantic.slots.name);
                                }
                            });
                        }
                        else
                            mSpeechService.dispatchSRAction(Business.RADIO,openRadioByVoiceModel());
                    }else {
                        doExceptonAction(context);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    doExceptonAction(context);
                }

            }else if (FACTORY.equals(intentEntity.semantic.slots.name)||FACTORY_SET.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.FACTORYC1CONDITION;
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, context.getString(R.string.factoryC1), exitListener);
                openFactoryApp();
                Utils.eventTrack(context, R.string.skill_factory, R.string.scene_factory, R.string.object_open_factory, conditionId, R.string.condition_default);
            } else {
                if ("????????????".equals(intentEntity.semantic.slots.name)||VIDEO.equals(intentEntity.semantic.slots.name)) {
                    Utils.getMessageWithTtsSpeakOnly(context, VIDEOC1CONDITION, context.getString(R.string.systemC24), exitListener);
                    Utils.eventTrack(context, R.string.skill_vedio, R.string.scene_vedio, R.string.object_vedio1, VIDEOC1CONDITION, R.string.condition_default);
                    startApp(intentEntity.semantic.slots.name);
                } else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), exitListener);
                    Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter1, APPCENTERC1CONDITION, R.string.condition_default);
                    startApp(intentEntity.semantic.slots.name);
                } else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    Utils.getMessageWithTtsSpeakOnly(context, FILEMANAGEMENTC1CONDITION, context.getString(R.string.systemC24), exitListener);
                    Utils.eventTrack(context, R.string.skill_filemanagement, R.string.scene_filemanagement, R.string.object_filemanagement1, FILEMANAGEMENTC1CONDITION, R.string.condition_default);
                    startApp(intentEntity.semantic.slots.name);
                }else if("????????????".equals(intentEntity.semantic.slots.name)){
                    Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), exitListener);
                    //Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter1, APPCENTERC1CONDITION, R.string.condition_default);
                    startApp(intentEntity.semantic.slots.name);
                }else if("????????????".equals(intentEntity.semantic.slots.name)){
                    Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), exitListener);
                    //Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter1, APPCENTERC1CONDITION, R.string.condition_default);
                    startApp(intentEntity.semantic.slots.name);
                }else if("?????????".equals(intentEntity.semantic.slots.name)){
                    Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            if (!FloatViewManager.getInstance(context).isHide())
                                FloatViewManager.getInstance(context).hide();
                            startApp(intentEntity.semantic.slots.name);
                        }
                    });
                }else if ("????????????".equals(intentEntity.semantic.slots.name)){
                    Utils.getMessageWithTtsSpeakOnly(context, SERVICEMARTC1CONDITION, context.getString(R.string.systemC24), exitListener);
                    Utils.eventTrack(context, R.string.skill_servicemart, R.string.scene_servicemart, R.string.object_servicemart1, SERVICEMARTC1CONDITION, R.string.condition_default);
                    startApp(intentEntity.semantic.slots.name);
                }/*else if ("????????????".equals(intentEntity.semantic.slots.name)
                        ||"??????????????????".equals(intentEntity.semantic.slots.name)
                        ||"??????".equals(intentEntity.semantic.slots.name)
                        ||"????????????".equals(intentEntity.semantic.slots.name)){
                    try {
                        Utils.getMessageWithTtsSpeakOnly(context, TtsConstant.OUTSIDECAMERAC5, context.getString(R.string.outsidecameraC5), exitListener);
                        mSpeechService.dispatchSRAction(Business.CLOUDEYE, intentEntity.convert2NlpVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }*/else if ("???????????????".equals(intentEntity.semantic.slots.name)
                        ||"?????????".equals(intentEntity.semantic.slots.name)){
                    CarUtils.getInstance(context).setCloudEyeUsage(VEHICLE.REQUEST);
                    //???????????????????????????????????????????????????
                    Message msg = new Message();
                    msg.what = CHECK_CLOUDEYE_OPEN;
                    msg.obj = intentEntity;
                    handler.sendMessageDelayed(msg,500);
                } else if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.name!=null){
                    if("????????????".equals(intentEntity.semantic.slots.name))
                        intentEntity.semantic.slots.name = "????????????";
                    else if("????????????".equals(intentEntity.semantic.slots.name))
                        intentEntity.semantic.slots.name = "????????????";
                    else if("????????????".equals(intentEntity.semantic.slots.name)){
                        hasFace = Utils.getInt(context,FACE_TYPE,0);
                        Log.d(TAG,"when open hasFace = " + hasFace);
                        if(hasFace == 0){
                            Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                            return;
                        }
                    }else if("????????????".equals(intentEntity.semantic.slots.name)
                            ||"??????hicar".equals(intentEntity.semantic.slots.name)
                            || "hicar".equals(intentEntity.semantic.slots.name)){
                        intentEntity.semantic.slots.name = "HiCar";
                        Utils.getMessageWithTtsSpeakOnly(context, TtsConstant.HICARC1, context.getString(R.string.hicarC1), exitListener);
                        Utils.eventTrack(context, R.string.skill_hicar, R.string.scene_hicar, R.string.object_open_hicar, TtsConstant.HICARC1, R.string.condition_default,context.getString(R.string.hicarC1));
                        startApp(intentEntity.semantic.slots.name);
                        return;
                    }


                    if(AppControlManager.getInstance(context).appIsExistByName(context,intentEntity.semantic.slots.name)){

                        if("?????????".equals(intentEntity.semantic.slots.name)){
                            Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), new TTSController.OnTtsStoppedListener() {
                                @Override
                                public void onPlayStopped() {
                                    Utils.exitVoiceAssistant();
                                    startApp(intentEntity.semantic.slots.name);
                                }
                            });
                        }else if("????????????".equals(intentEntity.semantic.slots.name)){

                            VideoController.getInstance(context).srAction(intentEntity);
                         /*   Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), new TTSController.OnTtsStoppedListener() {
                                @Override
                                public void onPlayStopped() {
                                    Utils.exitVoiceAssistant();
//                                    startApp(intentEntity.semantic.slots.name);
                                }
                            });
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startApp(intentEntity.semantic.slots.name);
                                }
                            },1000);*/

                        }else if(ChangbaController.CHANGBANAME.equals(intentEntity.semantic.slots.name)){
//                            Utils.getMessageWithTtsSpeakOnly(context, CHANGBAC1CONDITION, context.getString(R.string.changbaC1), new TTSController.OnTtsStoppedListener() {
//                                @Override
//                                public void onPlayStopped() {
//                                    Utils.exitVoiceAssistant();
//                                    //startApp(intentEntity.semantic.slots.name);
//                                    ChangbaController.getInstance(context).openCB();
//                                }
//                            });
                            Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                        } else {
                            Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), exitListener);
                            //Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter1, APPCENTERC1CONDITION, R.string.condition_default);
                            startApp(intentEntity.semantic.slots.name);
                        }

                    }
                    else
                        Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                }else {
                    Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                }
            }
        } else if (PlatformConstant.Operation.EXIT.equals(intentEntity.operation)) {
            if (BLE.equals(intentEntity.semantic.slots.name)||BLE_SET.equals(intentEntity.semantic.slots.name)) {
                Log.d(TAG, "srAction: "+intentEntity.text+".."+intentEntity.semantic.slots.name);
                if (BLE_EXIT_TEXT.equals(intentEntity.text)||BLE.equals(intentEntity.semantic.slots.name)) {//????????????
                    conditionId = TtsConstant.SYSTEMC12CONDITION;
                    String[] texts = context.getResources().getStringArray(R.array.systemC12);
                    int i = new Random().nextInt(texts.length);
                    defaultText = texts[i];
                    Utils.eventTrack(context, R.string.skill_system, R.string.scene_bt, R.string.object_bt_exit, conditionId, R.string.condition_system_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    AppConfig.INSTANCE.isSaidBTClose = true;
                    BluetoothAdapter.getDefaultAdapter().disable();
                } else {//??????????????????
                    conditionId = TtsConstant.SYSTEMC25CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_exit, conditionId, R.string.condition_system_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    exitApp("????????????");
//                    AppConfig.INSTANCE.isSaidBTClose = true;
                }

                Log.d(TAG, " AppConfig.INSTANCE.isSaidBTClose = " + AppConfig.INSTANCE.isSaidBTClose);

            } else if (WIFI.equals(intentEntity.semantic.slots.name)) {
                Log.d(TAG, "srAction: "+intentEntity.text);
                conditionId = TtsConstant.SYSTEMC16CONDITION;
                String[] texts = context.getResources().getStringArray(R.array.systemC16);
                int i = new Random().nextInt(texts.length);
                defaultText = texts[i];
                AppConfig.INSTANCE.isSaidWIFIClose = true;

                wifiManager.setWifiEnabled(false);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_wifi, R.string.object_wifi_exit, conditionId, R.string.condition_system_default);
                Log.d(TAG, "AppConstant.isSaidWIFIClose = " + AppConfig.INSTANCE.isSaidWIFIClose);
            } else if (SYSTEM_SET.equals(intentEntity.semantic.slots.name) || SET.equals(intentEntity.semantic.slots.name) ||
                    NET.equals(intentEntity.semantic.slots.name) || DISPLAY.equals(intentEntity.semantic.slots.name) ||
                    VOICE.equals(intentEntity.semantic.slots.name) /*|| SYSTEM_UPDATE.equals(intentEntity.semantic.slots.name)*/
                    ||MOBILE_NET.equals(intentEntity.semantic.slots.name)||
                    SYSTEM_RESET.equals(intentEntity.semantic.slots.name)
                    ||NET_SET.equals(intentEntity.semantic.slots.name)
                    ||EFFECT.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.SYSTEMC25CONDITION;
                defaultText = context.getString(R.string.systemC25);
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                Utils.eventTrack(context, R.string.skill_system, R.string.scene_setting, R.string.object_setting_exit, conditionId, R.string.condition_system_default);
                exitApp("????????????");
            } else if ("?????????".equals(intentEntity.semantic.slots.name) || "????????????".equals(intentEntity.semantic.slots.name)
                    || "????????????".equals(intentEntity.semantic.slots.name)) {
                Utils.getMessageWithTtsSpeakOnly(context, PHONEC43CONDITION, context.getString(R.string.systemC25), exitListener);
                exitApp("??????");
                Utils.eventTrack(context, R.string.skill_phone, R.string.scene_phone_contact_ope, R.string.object_contact_ope_2, TtsConstant.PHONEC43CONDITION, R.string.condition_phoneC43);
            } else if ("??????".equals(intentEntity.semantic.slots.name) || "??????".equals(intentEntity.semantic.slots.name)
                    || "???????????????".equals(intentEntity.semantic.slots.name) || "????????????".equals(intentEntity.semantic.slots.name)
                /*|| "??????".equals(intentEntity.semantic.slots.name)*/
                    || "????????????".equals(intentEntity.semantic.slots.name)
                    || CAR_SETTINGS.equals(intentEntity.semantic.slots.name)) {
                if ("??????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.CARCONTROLC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_car_control, R.string.scene_car_control, R.string.object_car_control2, CARCONTROLC2CONDITION, R.string.condition_default);
                } else if ("??????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.CARCONDITIONC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_car_condition, R.string.scene_car_condition, R.string.object_car_condition2, CARCONDITIONC2CONDITION, R.string.condition_default);
                } else if ("???????????????".equals(intentEntity.semantic.slots.name) || "????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.USERCENTERC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_usercenter, R.string.scene_usercenter, R.string.object_usercenter2, USERCENTERC2CONDITION, R.string.condition_default);
                }else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.TPMC2CONDITION;
                    defaultText = context.getString(R.string.tpmC2);
                    Utils.eventTrack(context, R.string.skill_tpm, R.string.scene_tpm, R.string.object_tpm2, conditionId, R.string.condition_default);
                }else if (CAR_SETTINGS.equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.CARSETTINGC2CONDITION;
                    defaultText = context.getString(R.string.carsettingC2);
                    Utils.eventTrack(context, R.string.skill_car_settings, R.string.scene_car_settings, R.string.object_carsettingC2, TtsConstant.CARSETTINGC2CONDITION, R.string.condition_default);
                }

                /*else if ("??????".equals(intentEntity.semantic.slots.name)) {
                    conditionId =TtsConstant.CHEXINC41CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_chexin, R.string.scene_open_close_cheixn, R.string.close_chexin, conditionId, R.string.condition_chexinC41);
                }*/
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                gotoHome();
            } else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                if (mSpeechService != null) {
                    try {
                        mSpeechService.dispatchSRAction(Business.MUSIC, intentEntity.convert2NlpVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        doExceptonAction(context);
                    }
                }else {
                    doExceptonAction(context);
                }
            }else if (FACTORY.equals(intentEntity.semantic.slots.name)||FACTORY_SET.equals(intentEntity.semantic.slots.name)) {
                conditionId = TtsConstant.FACTORYC2CONDITION;
                Utils.getMessageWithTtsSpeakOnly(context, conditionId, context.getString(R.string.factoryC2), exitListener);
                closeFactoryApp();
                Utils.eventTrack(context, R.string.skill_factory, R.string.scene_factory, R.string.object_close_factory, conditionId, R.string.condition_default);
            } else {
                if ("????????????".equals(intentEntity.semantic.slots.name)||VIDEO.equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.VIDEOC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_vedio, R.string.scene_vedio, R.string.object_vedio2, VIDEOC2CONDITION, R.string.condition_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    exitApp(intentEntity.semantic.slots.name);
                } else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.APPCENTERC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter2, APPCENTERC2CONDITION, R.string.condition_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    exitApp(intentEntity.semantic.slots.name);
                } else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.FILEMANAGEMENTC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_filemanagement, R.string.scene_filemanagement, R.string.object_filemanagement2, FILEMANAGEMENTC2CONDITION, R.string.condition_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    exitApp(intentEntity.semantic.slots.name);
                }else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.APPCENTERC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    //Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter2, APPCENTERC2CONDITION, R.string.condition_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    exitApp(intentEntity.semantic.slots.name);
                }else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.APPCENTERC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    //Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter2, APPCENTERC2CONDITION, R.string.condition_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    exitApp(intentEntity.semantic.slots.name);
                }else if ("????????????".equals(intentEntity.semantic.slots.name)
                        ||"??????hicar".equals(intentEntity.semantic.slots.name)
                        || "hicar".equals(intentEntity.semantic.slots.name)) {
                    intentEntity.semantic.slots.name = "HiCar";
                    conditionId = TtsConstant.APPCENTERC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_hicar, R.string.scene_hicar, R.string.object_close_hicar, TtsConstant.HICARC2, R.string.condition_default,context.getString(R.string.hicarC2));
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            try {
                                exitApp(intentEntity.semantic.slots.name);
                                mSpeechService.dispatchMvwAction(Business.HICAR, closeHicarByVoiceModel());
                                Utils.exitVoiceAssistant();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else if ("????????????".equals(intentEntity.semantic.slots.name)){
                    conditionId = TtsConstant.APPCENTERC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter2, APPCENTERC2CONDITION, R.string.condition_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    try {
                        if(mSpeechService!=null){
                            mSpeechService.dispatchSRAction(Business.RADIO,closeRadioByVoiceModel());
                        }else {
                            doExceptonAction(context);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        doExceptonAction(context);
                    }
//                    exitApp("??????");
                }else if ("QQ??????".equalsIgnoreCase(intentEntity.semantic.slots.name)){
                    conditionId = TtsConstant.APPCENTERC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter2, APPCENTERC2CONDITION, R.string.condition_default);
//                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);

                    try {
                        if(mSpeechService!=null){
                            mSpeechService.dispatchSRAction(Business.MUSIC,closeMusicByVoiceModel());
                        }else {
                            doExceptonAction(context);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        doExceptonAction(context);
                    }
//                    exitApp("??????");
                }else if ("????????????".equals(intentEntity.semantic.slots.name)) {
                    conditionId = TtsConstant.SERVICEMARTC2CONDITION;
                    defaultText = context.getString(R.string.systemC25);
                    Utils.eventTrack(context, R.string.skill_servicemart, R.string.scene_servicemart, R.string.object_servicemart2, SERVICEMARTC2CONDITION, R.string.condition_default);
                    Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                    exitApp(intentEntity.semantic.slots.name);
                }else if ("???????????????".equals(intentEntity.semantic.slots.name)
                        ||"?????????".equals(intentEntity.semantic.slots.name)){
                    CarUtils.getInstance(context).setCloudEyeUsage(VEHICLE.REQUEST);
                    //???????????????????????????????????????????????????
                    Message msg = new Message();
                    msg.what = CHECK_CLOUDEYE_EXIT;
                    msg.obj = intentEntity;
                    handler.sendMessageDelayed(msg,500);
                }/*else if ("????????????".equals(intentEntity.semantic.slots.name)
                        ||"??????????????????".equals(intentEntity.semantic.slots.name)
                        ||"??????".equals(intentEntity.semantic.slots.name)
                        ||"????????????".equals(intentEntity.semantic.slots.name)){
                    try {
                        Utils.getMessageWithTtsSpeakOnly(context, TtsConstant.OUTSIDECAMERAC6, context.getString(R.string.outsidecameraC6), exitListener);
                        mSpeechService.dispatchSRAction(Business.CLOUDEYE, intentEntity.convert2NlpVoiceModel());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }*/else if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.name!=null){
                    if("????????????".equals(intentEntity.semantic.slots.name))
                        intentEntity.semantic.slots.name = "????????????";
                    else if("????????????".equals(intentEntity.semantic.slots.name))
                        intentEntity.semantic.slots.name = "????????????";
                    else if("????????????".equals(intentEntity.semantic.slots.name)){
                        hasFace = Utils.getInt(context,FACE_TYPE,0);
                        Log.d(TAG,"when close hasFace = " + hasFace);
                        if(hasFace == 0){
                            Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                            return;
                        }
                    }else if("??????".equals(intentEntity.semantic.slots.name)){
//                        conditionId = TtsConstant.APPCENTERC2CONDITION;
//                        defaultText = context.getString(R.string.systemC25);
//                        Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter2, APPCENTERC2CONDITION, R.string.condition_default);
//                        Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
//                        //closeAPP(context,AppConstant.PACKAGE_NAME_CHANGBA);
//                        ChangbaController.getInstance(context).sendMonitorCommandToCB(0x10003,0);
                        Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                        return;
                    }else if("????????????".equals(intentEntity.semantic.slots.name)){
                        VideoController.getInstance(context).srAction(intentEntity);
                        return;
                    }

                    if(AppControlManager.getInstance(context).appIsExistByName(context,intentEntity.semantic.slots.name)){
                        conditionId = TtsConstant.APPCENTERC2CONDITION;
                        defaultText = context.getString(R.string.systemC25);
                        Utils.eventTrack(context, R.string.skill_appcenter, R.string.scene_appcenter, R.string.object_appcenter2, APPCENTERC2CONDITION, R.string.condition_default);
                        Utils.getMessageWithTtsSpeakOnly(context, conditionId, defaultText, exitListener);
                        exitApp(intentEntity.semantic.slots.name);
                    } else {
                        Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                    }
                }else {
                        Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                }

            }
        }else if(PlatformConstant.Operation.QUERY.equals(intentEntity.operation)){
            if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.name!=null){
                 if(VIDEO.equals(intentEntity.semantic.slots.name)){
                     Utils.getMessageWithTtsSpeakOnly(context, VIDEOC1CONDITION, context.getString(R.string.systemC24), exitListener);
                     Utils.eventTrack(context, R.string.skill_vedio, R.string.scene_vedio, R.string.object_vedio1, VIDEOC1CONDITION, R.string.condition_default);
                     startApp(intentEntity.semantic.slots.name);
                }else if("????????????".equals(intentEntity.semantic.slots.name)){
                     VideoController.getInstance(context).srAction(intentEntity);
                 }else if("?????????".equals(intentEntity.semantic.slots.name)){
                     if(AppControlManager.getInstance(context).appIsExistByName(context,intentEntity.semantic.slots.name)){
                         Utils.getMessageWithTtsSpeakOnly(context, APPCENTERC1CONDITION, context.getString(R.string.systemC24), new TTSController.OnTtsStoppedListener() {
                             @Override
                             public void onPlayStopped() {
                                 Utils.exitVoiceAssistant();
                                 startApp(intentEntity.semantic.slots.name);
                             }
                         });
                     } else
                         Utils.getMessageWithTtsSpeakOnly(context, "", "?????????????????????????????????", exitListener);
                 } else
                    doExceptonAction(context);
            }else
                doExceptonAction(context);
        } else
            doExceptonAction(context);
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {
    }

    private void startSetting(String type) {
        SettingsUtil.startSetting(context, type);
    }

    private void openCarController(String value) {
        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME_NAVI);
        intent.setAction(OPEN_VC_FROM_OTHERS);
        intent.putExtra(EXTRA_KEY_OPEN_VC, value);
        if (EXTRA_OPEN_SETTING_VALUE_VC.equals(value)) {
            intent.putExtra(EXTRA_KEY_OPEN_VC_TYPE, OPEN_VC_TYPE_FAST_CONTROL);
        }
        context.sendBroadcast(intent);
        Log.d(TAG, "lh:openCarController:" + value);
    }

    private void openCarController(String value,int type) {
        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME_NAVI);
        intent.setAction(OPEN_VC_FROM_OTHERS);
        intent.putExtra(EXTRA_KEY_OPEN_VC, value);
        intent.putExtra(EXTRA_KEY_OPEN_VC_TYPE, type);
        context.sendBroadcast(intent);
        Log.d(TAG, "lh:openCarController:" + value);
    }

    private void openCarStatuController(String value,int type) {
        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME_NAVI);
        intent.setAction(OPEN_VC_FROM_OTHERS);
        intent.putExtra(EXTRA_KEY_OPEN_VC, value);
        //????????????????????????????????????????????? (??????????????????,????????????)
        intent.putExtra(EXTRA_KEY_OPEN_CT_TYPE,type);
        context.sendBroadcast(intent);
        Log.d(TAG, "lh:openCarController:" + value);
    }

    private void openFactoryApp(){
        try {
            final String pkg = "com.chinatsp.tfactoryapp";
            final String cls = "com.chinatsp.tfactoryapp.MainActivity";
            final String action = "ACTION_CHINATSP_FACTORY";
            //final String action = "android.intent.action.MAIN";
            ComponentName componentName = new ComponentName(pkg, cls);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setAction(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void closeFactoryApp(){
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, "com.chinatsp.tfactoryapp");  //packageName??????????????????????????????????????????
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeAPP(Context mContext,String packageName){
        ActivityManager mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            Log.d(TAG, "closeApp() called with: name = [" + packageName + "]");
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);  //packageName??????????????????????????????????????????
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NlpVoiceModel openMusicByVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "musicX";
        nlpVoiceModel.operation = "INSTRUCTION";
//        nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"OPEN\"}}";
        nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"QQMUSIC\"}}";
        return nlpVoiceModel;
    }

    private NlpVoiceModel closeMusicByVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "musicX";
        nlpVoiceModel.operation = "INSTRUCTION";
        nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"CLOSE\"}}";
        return nlpVoiceModel;
    }


    private NlpVoiceModel playRadioByVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "radio";
        nlpVoiceModel.operation = "PLAY";
        nlpVoiceModel.semantic ="{\"slots\":{\"name\":\"????????????\"}}";
        return nlpVoiceModel;
    }


    private NlpVoiceModel openRadioByVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "radio";
        nlpVoiceModel.operation = "INSTRUCTION";
        nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"OPEN\"}}";
        return nlpVoiceModel;
    }

    private NlpVoiceModel closeRadioByVoiceModel(){
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "radio";
        nlpVoiceModel.operation = "INSTRUCTION";
        nlpVoiceModel.semantic ="{\"slots\":{\"insType\":\"CLOSE\"}}";
        return nlpVoiceModel;
    }


    private CmdVoiceModel closeHicarByVoiceModel() {
        CmdVoiceModel cmdVoiceModel = new CmdVoiceModel();
        cmdVoiceModel.id = 1001;
        cmdVoiceModel.text = "????????????";
        cmdVoiceModel.response= DatastatManager.response;
        cmdVoiceModel.hide= FloatViewManager.getInstance(BaseApplication.getInstance().getApplicationContext()).isHide()?0:1;
        return cmdVoiceModel;
    }
}
