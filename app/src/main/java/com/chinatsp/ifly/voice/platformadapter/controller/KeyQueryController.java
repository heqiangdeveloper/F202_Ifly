package com.chinatsp.ifly.voice.platformadapter.controller;

import android.annotation.SuppressLint;
import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.VehicleLightSwitch;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.cabin.CarCabinManager;
import android.car.hardware.constant.AVM;
import android.car.hardware.constant.DVR;
import android.car.hardware.constant.HVAC;
import android.car.hardware.constant.VEHICLE;
import android.car.hardware.mcu.CarMcuManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.automotive.vehicle.V2_0.VehicleLightState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.CarKeyViewManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IKeyQueryController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static android.car.VehicleAreaWindow.WINDOW_ROW_1_LEFT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_1_RIGHT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_2_LEFT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_2_RIGHT;
import static android.car.hardware.cabin.CarCabinManager.ID_AVM_DEF_OPEN;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_AUTO_HEAD_LIGHT;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_EMERGENCY_LIGHT;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_TURN_LEFT_SIGNAL_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_TURN_RIGHT_SIGNAL_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_SUNVISOR_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_UP_DOWN_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_WASH;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_WIPER;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_WIPER_INTERVAL;
import static android.car.hardware.cabin.CarCabinManager.ID_DOOR_LOCK_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_DVR_EMERGENCY_RECORD;
import static android.car.hardware.cabin.CarCabinManager.ID_DVR_SNAP_SHOOT;
import static android.car.hardware.cabin.CarCabinManager.ID_FOG_LIGHTS_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_HEADLIGHTS_SWITCH;
import static android.car.hardware.cabin.CarCabinManager.ID_HIGH_BEAM_LIGHTS_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_NIGHT_MODE;
import static android.car.hardware.cabin.CarCabinManager.ID_VENDOR_DRIVER_MODE_SET;
import static android.car.hardware.cabin.CarCabinManager.ID_WINDOW_LOCK;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_CANCEL;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_INDICATE;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_RESUME;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_SET;
import static android.car.hardware.constant.VEHICLE.KEY_APA_SWITCH;
import static android.car.hardware.constant.VEHICLE.KEY_ESP_AUTO_HOLD;
import static android.car.hardware.constant.VEHICLE.KEY_ESP_SWITCH;
import static android.car.hardware.constant.VehicleAreaId.SEAT_ROW_1_LEFT;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_FRONT_WINDSHIELD;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_REAR_WINDSHIELD;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_ROOF_TOP_1;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_ROOF_TOP_2;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_FAN_DIRECTION;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_RECIRC_ON;
import static android.car.hardware.mcu.CarMcuManager.ID_VENDOR_ECALL_STATE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_ABAT_VENT_SWITCH_OFF;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_ABAT_VENT_SWITCH_ON;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_AC_MAX;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_AC_SWITH;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_AIR_CONDITION_AUTO;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_AIR_CONDITION_OFF;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_AIR_CYCLE_MODE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_AUTO_HOLD;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_AUTO_PARK;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_CAMERA;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_CAMERA_EMERGE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_CRUISE_CANCEL;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_CRUISE_MAIN;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_DANGER_BTN;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_DEFROST_BACK;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_DEFROST_FRONT;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_DOOR_LOCK;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_DRIVE_CHANGE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_ELEC_PARKING;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_ESC;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_HANGUP;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_LIGHT;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_MAIN_DRIVE_FALL;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_METER_CHANGE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_METER_CONFIRM;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_METER_PAGE_LAST;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_METER_PAGE_NEXT;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_MUTE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_OTHER_DRIVE_FALL;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_SCUTTLE_SWITCH_OFF;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_SCUTTLE_SWITCH_ON;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_SOS;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_SRC;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_TEL;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_TEMP_CHANGE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_TRUNK;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_VIEW_OVERALL;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_VOLDOWN;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_VOLUP;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_WINDOW_LOCK;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_WIND_CHANGE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_WIND_MODE;
import static com.chinatsp.ifly.CarKeyViewManager.KeyType.KEY_WIPER;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;

public class KeyQueryController extends BaseController implements IKeyQueryController {
    private final static String TAG = "KeyQueryController";
    private Context mContext;
    private Handler mHandler;
    private int countDownTime = 10;
    private final static int MSG_TTS = 100;
    private final static int MSG_WAIT_KEY = 101;
    private final static int MSG_WAIT_ENGINE = 102;
    private final static int MSG_COUNT_DOWN = 103;
    private final static int START_TTS = 108;
    private final static int CRUISE_INDICATE_ON = 1000;//???????????????
    private final static int CRUISE_INDICATE_OFF = 1001;//???????????????
    private final static int CRUISE_CANCEL_ON = 1002;//???????????????
    private final static int CRUISE_CANCEL_OFF = 1003;//???????????????
    private final static int CRUISE_SWITCH_SET = 1004;//??????/??????-
    private final static int CRUISE_SWITCH_RESUME = 1005;//??????/??????+
    private final static int METER_PAGE_LAST = 1006;//???????????????
    private final static int METER_PAGE_NEXT = 1007;//???????????????
    private final static int METER_CONFIRM = 1008;//????????????
    private final static int METER_CHANGE = 1009;//????????????
    private final static int ESC_ON = 1010;//????????????????????????-??????
    private final static int ESC_OFF = 1011;//????????????????????????-??????
    private final static int TRUNK_ON = 1012;//?????????-??????
    private final static int TRUNK_OFF = 1013;//?????????-??????
    private final static int AUTO_PARK_ON = 1014;//????????????-??????
    private final static int AUTO_PARK_OFF = 1015;//????????????-??????
    private final static int WINDOW_LOCK = 1016; //?????????????????????-??????
    private final static int WINDOW_UNLOCK = 1017; //?????????????????????-??????
    private final static int MAIN_DRIVE_FALL = 1018; //????????????????????????
    private final static int OTHER_DOOR_FALL = 1019;//??????????????????????????????
    private final static int TEMP_CHANGE = 1020; // ?????????????????????????????????
    private final static int DRIVE_CHANGE_NORMAL = 1021;//????????????????????????-????????????
    private final static int DRIVE_CHANGE_SPORT = 1022;//????????????????????????-????????????
    private final static int DRIVE_CHANGE_ECO = 1023;//????????????????????????-????????????
    private final static int AIR_CONDITION_OFF = 1024;//????????????
    private final static int DANGER_BTN = 1025;//????????????
    private final static int AIR_CONDITION_AUTO = 1026;//??????????????????
    private final static int WIND_MODE = 1027;//????????????
    private final static int AIR_CYCLE_MODE = 1028;//????????????????????????
    private final static int AC_MAX = 1029;//??????????????????
    private final static int WIND_CHANGE = 1030;//????????????
    private final static int CAMERA = 1031;//?????????????????????
    private final static int CAMERA_EMERGE = 1032;//????????????
    private final static int SOS = 1033;//????????????ID_BODY_DOME_LIGHT
    private final static int DOME_LIGHT_1 = 1034; //?????????????????????-??????
    private final static int DOME_LIGHT_2 = 1035; //?????????????????????-??????
    private final static int SCUTTLE_SWITCH_ON = 1036; //?????????
    private final static int SCUTTLE_SWITCH_OFF = 1037; //?????????
    private final static int ABAT_VENT_SWITCH_ON = 1038;//???????????????
    private final static int ABAT_VENT_SWITCH_OFF = 1039;//???????????????
    private final static int TOP_LIGHT_SWITCH = 1040;//????????????
    private final static int SEAT_ADJUST = 1041;//????????????
    private final static int AUTO_HEAD_STATUS = 1042;//??????????????????-????????????
    private final static int LOW_BEAM_STATUS = 1043;//??????????????????-??????(??????)
    private final static int POSITION_LAMP_STATUS = 1044;//??????????????????-??????
    private final static int TURN_INDICATOR_LEFT = 1045;//??????????????????-????????????
    private final static int TURN_INDICATOR_RIGHT = 1046;//??????????????????-????????????
    private final static int REAR_FOGLAMP_STATUS = 1047;//??????????????????-?????????
    private final static int HIGH_BEAM_STATUS = 1048;//??????????????????-??????
    private final static int FRONT_WIPER_STATUS_0 = 1049;//??????????????????-?????????OFF
    private final static int FRONT_WIPER_STATUS_1 = 1050;//??????????????????-????????????
    private final static int FRONT_WIPER_STATUS_2 = 1051;//??????????????????-????????????
    private final static int FRONT_WIPER_STATUS_3 = 1052;//??????????????????-??????
    private final static int FRONT_WIPER_STATUS_4 = 1053;//??????????????????-????????????
    private final static int FRONT_WASH_STATUS = 1054;//??????????????????-?????????
    private final static int FRONT_WIPER_INTERVAL_TIME = 1055;//??????????????????-????????????
    private final static int REAR_WASH_STATUS_ON = 1056;//??????????????????-?????????ON
    private final static int REAR_WASH_STATUS_OFF = 1057;//??????????????????-?????????OFF
    private final static int VIEW_OVERALL_ON = 1058;//????????????
    private final static int VIEW_OVERALL_OFF = 1059;//????????????
    private final static int ELEC_PARKING_SWITCH_ON = 1060;//??????????????????-??????
    private final static int ELEC_PARKING_SWITCH_OFF = 1061;//??????????????????-??????
    private final static int AC_SWITCH_ON = 1062; // ???????????????-??????
    private final static int AC_SWITCH_OFF = 1063; // ???????????????-??????
    private final static int DEFROST_FRONT_ON = 1064; //?????????-??????
    private final static int DEFROST_FRONT_OFF = 1065; //?????????-??????
    private final static int DEFROST_BACK_ON = 1066; //?????????-??????
    private final static int DEFROST_BACK_OFF = 1067; //?????????-??????
    private final static int DOOR_LOCK = 1068; //?????????????????????-??????
    private final static int DOOR_UNLOCK = 1069; //?????????????????????-??????
    private final static int AUTO_HOLD_ON = 1070;//????????????-??????
    private final static int AUTO_HOLD_OFF = 1071;//????????????-??????

    // ???????????????
    private final static int SPEED_LIMIT = 7;
    private final static String ACTION_BROADCAST_REV = "com.txznet.adapter.recv";
    private final static String ACTION_BROADCAST_SEND = "com.txznet.adapter.send";
    private final static String ACTION_REV = "btn.notify";
    //???????????????
    private final static String ACTION_FRONT_DEFROST = "ac.front.defrost";
    private final static int KEY_QUERY_EXIT = 0;//??????????????????
    private final static int KEY_QUERY_IN = 1;//??????????????????
    private final static String SRC = "SRC";//??????
    private final static String VOLUP = "VOLUP";//?????????
    private final static String VOLDOWN = "VOLDOWN";//?????????
    private final static String MUTE = "MUTE";//??????
    private final static String TEL = "PRE";//?????????
    private final static String HANDUP = "NEXT";//?????????
    private final static String VR = "VR";//??????

    private CarCabinManager carCabinManager = null;
    public CarSensorManager mCarSensorManager;
    boolean isAnswer = false;
    private static int ignitionState = -1;

    @SuppressLint("HandlerLeak")
    public KeyQueryController(final Context mContext) {
        this.mContext = mContext;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_TTS:
                        startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                //Utils.exitVoiceAssistant();
                            }
                        });
                        break;
                    case MSG_WAIT_KEY:
                        startTTSOnly("???????????????????????????????????????????????????", new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                FloatViewManager.getInstance(mContext).hide();
                            }
                        });
                        exit();
                        break;
                    case MSG_WAIT_ENGINE:
                        //??????????????????
                        isAnswer = false;
                        mHandler.removeCallbacksAndMessages(null);
                        CarKeyViewManager.getInstance(mContext).hideActionTipView();
                        startTTSOnly("?????????????????????????????????????????????" +
                                "????????????????????????????????? ??????????????????", new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                FloatViewManager.getInstance(mContext).hide();
                            }
                        });
//                        exit();
                        break;
                    case MSG_COUNT_DOWN:
                        //?????????
                        countDownTime--;
                        CarKeyViewManager.getInstance(mContext).updateTimeView(countDownTime);
                        sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);
                        break;
                    case START_TTS:
                        startTTS((String) msg.obj);
                        break;
                    case CRUISE_INDICATE_ON://???????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise, R.string.object_cruise, ANJIANC1CONDITION, R.string.condition_cruise1, R.string.anjian_c1, KEY_CRUISE_MAIN);
                        } else {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise, R.string.object_cruise, ANJIANC2CONDITION, R.string.condition_cruise2, R.string.anjian_c2, KEY_CRUISE_MAIN);
                        }
                        break;
                    case CRUISE_INDICATE_OFF://???????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise, R.string.object_cruise, ANJIANC3CONDITION, R.string.condition_cruise3, R.string.anjian_c3, KEY_CRUISE_MAIN);
                        } else {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise, R.string.object_cruise, ANJIANC4CONDITION, R.string.condition_cruise4, R.string.anjian_c4, KEY_CRUISE_MAIN);
                        }
                        break;
                    case CRUISE_CANCEL_ON:////????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise_cancel, R.string.object_cruise_cancel, ANJIANC5CONDITION, R.string.condition_cruise_cancel1, R.string.anjian_c5, KEY_CRUISE_CANCEL);
                        } else {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise_cancel, R.string.object_cruise_cancel, ANJIANC6CONDITION, R.string.condition_cruise_cancel2, R.string.anjian_c6, KEY_CRUISE_CANCEL);
                        }
                        break;
                    case CRUISE_CANCEL_OFF:////????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise_cancel, R.string.object_cruise_cancel, ANJIANC7CONDITION, R.string.condition_cruise_cancel3, R.string.anjian_c7, KEY_CRUISE_CANCEL);
                        } else {
                            showKeyInfo(R.string.skill_key_wheel, R.string.scene_cruise_cancel, R.string.object_cruise_cancel, ANJIANC8CONDITION, R.string.condition_cruise_cancel4, R.string.anjian_c8, KEY_CRUISE_CANCEL);
                        }
                        break;
                    case CRUISE_SWITCH_RESUME://??????/??????+
                        break;
                    case CRUISE_SWITCH_SET://??????/??????-
                        break;
                    case METER_PAGE_LAST://???????????????
                        showKeyInfo(R.string.skill_key_wheel, R.string.scene_meter, R.string.object_meter, ANJIANC21CONDITION, R.string.condition_anjianC21, R.string.anjian_c21, KEY_METER_PAGE_LAST);
                        break;
                    case METER_PAGE_NEXT://???????????????
                        showKeyInfo(R.string.skill_key_wheel, R.string.scene_meter, R.string.object_meter, ANJIANC22CONDITION, R.string.condition_anjianC22, R.string.anjian_c22, KEY_METER_PAGE_NEXT);
                        break;
                    case METER_CONFIRM://????????????
                        showKeyInfo(R.string.skill_key_wheel, R.string.scene_meter, R.string.object_meter, ANJIANC23CONDITION, R.string.condition_anjianC23, R.string.anjian_c23, KEY_METER_CONFIRM);
                        break;
                    case METER_CHANGE://????????????
                        showKeyInfo(R.string.skill_key_wheel, R.string.scene_meter_change, R.string.object_meter_change, ANJIANC24CONDITION, R.string.condition_default, R.string.anjian_c24, KEY_METER_CHANGE);
                        break;
                    case ESC_ON://????????????????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_esc, R.string.object_esc, ANJIANC31CONDITION, R.string.condition_esc1, R.string.anjian_c31, KEY_ESC);
                        } else {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_esc, R.string.object_esc, ANJIANC33CONDITION, R.string.condition_esc3, R.string.anjian_c33, KEY_ESC);
                        }
                        break;
                    case ESC_OFF://????????????????????????-??????,
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_esc, R.string.object_esc, ANJIANC32CONDITION, R.string.condition_esc2, R.string.anjian_c32, KEY_ESC);
                        } else {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_esc, R.string.object_esc, ANJIANC34CONDITION, R.string.condition_esc4, R.string.anjian_c34, KEY_ESC);
                        }
                        break;
                    case VIEW_OVERALL_ON://????????????-??????
                        showKeyInfo(R.string.skill_key_meter, R.string.scene_overall, R.string.object_overall, ANJIANC35CONDITION, R.string.condition_overall_on, R.string.anjian_c35, KEY_VIEW_OVERALL);
                        break;
                    case VIEW_OVERALL_OFF://????????????-??????
                        showKeyInfo(R.string.skill_key_meter, R.string.scene_overall, R.string.object_overall, ANJIANC36CONDITION, R.string.condition_overall_off, R.string.anjian_c36, KEY_VIEW_OVERALL);
                        break;
                    case TRUNK_ON://?????????
                        if (isRunning()) {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_trunk, R.string.object_trunk, ANJIANC37CONDITION, R.string.condition_trunk1, R.string.anjian_c37, KEY_TRUNK);
                        } else {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_trunk, R.string.object_trunk, ANJIANC38CONDITION, R.string.condition_trunk2, R.string.anjian_c38, KEY_TRUNK);
                        }
                        break;
                    case TRUNK_OFF:
                        if (isRunning()) {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_trunk, R.string.object_trunk, ANJIANC37CONDITION, R.string.condition_trunk1, R.string.anjian_c37, KEY_TRUNK);
                        } else {
                            showKeyInfo(R.string.skill_key_meter, R.string.scene_trunk, R.string.object_trunk, ANJIANC39CONDITION, R.string.condition_trunk3, R.string.anjian_c39, KEY_TRUNK);
                        }
                        break;
                    case ELEC_PARKING_SWITCH_ON://??????????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_default, R.string.scene_elec_parking, R.string.object_elec_parking, ANJIANC40CONDITION, R.string.condition_elec_parking1, R.string.anjian_c40, KEY_ELEC_PARKING);
                        } else {
                            showKeyInfo(R.string.skill_default, R.string.scene_elec_parking, R.string.object_elec_parking, ANJIANC42CONDITION, R.string.condition_elec_parking3, R.string.anjian_c42, KEY_ELEC_PARKING);
                        }
                        break;
                    case ELEC_PARKING_SWITCH_OFF://??????????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_default, R.string.scene_elec_parking, R.string.object_elec_parking, ANJIANC41CONDITION, R.string.condition_elec_parking2, R.string.anjian_c41, KEY_ELEC_PARKING);
                        } else {
                            showKeyInfo(R.string.skill_default, R.string.scene_elec_parking, R.string.object_elec_parking, ANJIANC43CONDITION, R.string.condition_elec_parking4, R.string.anjian_c43, KEY_ELEC_PARKING);
                        }
                        break;
                    case AUTO_HOLD_ON://????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_hold, R.string.object_auto_hold, ANJIANC44CONDITION, R.string.condition_auto_hold1, R.string.anjian_c44, KEY_AUTO_HOLD);
                        } else {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_hold, R.string.object_auto_hold, ANJIANC46CONDITION, R.string.condition_auto_hold3, R.string.anjian_c46, KEY_AUTO_HOLD);
                        }
                        break;
                    case AUTO_HOLD_OFF://????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_hold, R.string.object_auto_hold, ANJIANC45CONDITION, R.string.condition_auto_hold2, R.string.anjian_c45, KEY_AUTO_HOLD);
                        } else {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_hold, R.string.object_auto_hold, ANJIANC47CONDITION, R.string.condition_auto_hold4, R.string.anjian_c47, KEY_AUTO_HOLD);
                        }
                        break;
                    case AUTO_PARK_ON://????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_park, R.string.object_auto_park, ANJIANC48CONDITION, R.string.condition_auto_park1, R.string.anjian_c48, KEY_AUTO_PARK);
                        } else {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_park, R.string.object_auto_park, ANJIANC50CONDITION, R.string.condition_auto_park3, R.string.anjian_c50, KEY_AUTO_PARK);
                        }
                        break;
                    case AUTO_PARK_OFF://????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_park, R.string.object_auto_park, ANJIANC49CONDITION, R.string.condition_auto_park2, R.string.anjian_c49, KEY_AUTO_PARK);
                        } else {
                            showKeyInfo(R.string.skill_default, R.string.scene_auto_park, R.string.object_auto_park, ANJIANC51CONDITION, R.string.condition_auto_park4, R.string.anjian_c51, KEY_AUTO_PARK);
                        }
                        break;
                    case DRIVE_CHANGE_NORMAL://????????????????????????-????????????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_drive_mode, R.string.object_drive_mode, ANJIANC52CONDITION, R.string.condition_drive_mode1, R.string.anjian_c52, KEY_DRIVE_CHANGE);
                        break;
                    case DRIVE_CHANGE_SPORT://????????????????????????-????????????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_drive_mode, R.string.object_drive_mode, ANJIANC53CONDITION, R.string.condition_drive_mode2, R.string.anjian_c53, KEY_DRIVE_CHANGE);
                        break;
                    case DRIVE_CHANGE_ECO://????????????????????????-????????????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_drive_mode, R.string.object_drive_mode, ANJIANC54CONDITION, R.string.condition_drive_mode3, R.string.anjian_c54, KEY_DRIVE_CHANGE);
                        break;
                    case DOOR_LOCK://?????????????????????-??????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_door_lock, R.string.object_door_lock, ANJIANC55CONDITION, R.string.condition_door_lock, R.string.anjian_c55, KEY_DOOR_LOCK);
                        break;
                    case DOOR_UNLOCK://?????????????????????-??????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_door_lock, R.string.object_door_lock, ANJIANC56CONDITION, R.string.condition_door_unlock, R.string.anjian_c56, KEY_DOOR_LOCK);
                        break;
                    case WINDOW_LOCK://?????????????????????-??????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_window_lock, R.string.object_window_lock, ANJIANC57CONDITION, R.string.condition_window_lock, R.string.anjian_c57, KEY_WINDOW_LOCK);
                        break;
                    case WINDOW_UNLOCK://?????????????????????-??????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_window_lock, R.string.object_window_lock, ANJIANC58CONDITION, R.string.condition_window_unlock, R.string.anjian_c58, KEY_WINDOW_LOCK);
                        break;
                    case MAIN_DRIVE_FALL://????????????????????????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_main_drive_fall, R.string.object_main_drive_fall, ANJIANC59CONDITION, R.string.condition_default, R.string.anjian_c59, KEY_MAIN_DRIVE_FALL);
                        break;
                    case OTHER_DOOR_FALL://??????????????????????????????
                        showKeyInfo(R.string.skill_key_window, R.string.scene_other_drive_fall, R.string.object_other_drive_fall, ANJIANC60CONDITION, R.string.condition_default, R.string.anjian_c60, KEY_OTHER_DRIVE_FALL);
                        break;
                    case TEMP_CHANGE://??????????????????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_temp_change, R.string.object_temp_change, ANJIANC61CONDITION, R.string.condition_default, R.string.anjian_c61, KEY_TEMP_CHANGE);
                        break;
                    case AC_SWITCH_ON://???????????????-??????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_ac_switch, R.string.object_ac_switch, ANJIANC62CONDITION, R.string.condition_ac_switch_on, R.string.anjian_c62, KEY_AC_SWITH);
                        break;
                    case AC_SWITCH_OFF://???????????????-??????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_ac_switch, R.string.object_ac_switch, ANJIANC63CONDITION, R.string.condition_ac_switch_off, R.string.anjian_c63, KEY_AC_SWITH);
                        break;
                    case DEFROST_FRONT_ON://?????????-??????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_defrost_front, R.string.object_defrost_front, ANJIANC64CONDITION, R.string.condition_defrost_front_on, R.string.anjian_c64, KEY_DEFROST_FRONT);
                        break;
                    case DEFROST_FRONT_OFF://?????????-??????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_defrost_front, R.string.object_defrost_front, ANJIANC65CONDITION, R.string.condition_defrost_front_off, R.string.anjian_c65, KEY_DEFROST_FRONT);
                        break;
                    case DEFROST_BACK_ON://?????????-??????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_defrost_back, R.string.object_defrost_back, ANJIANC66CONDITION, R.string.condition_defrost_back_on, R.string.anjian_c66, KEY_DEFROST_BACK);
                        break;
                    case DEFROST_BACK_OFF://?????????-??????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_defrost_back, R.string.object_defrost_back, ANJIANC67CONDITION, R.string.condition_defrost_back_off, R.string.anjian_c67, KEY_DEFROST_BACK);
                        break;
                    case AIR_CONDITION_OFF://????????????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_close_air, R.string.object_close_air, ANJIANC68CONDITION, R.string.condition_default, R.string.anjian_c68, KEY_AIR_CONDITION_OFF);
                        break;
                    case DANGER_BTN://????????????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_danger_btn, R.string.object_danger_btn, ANJIANC69CONDITION, R.string.condition_default, R.string.anjian_c69, KEY_DANGER_BTN);
                        break;
                    case AIR_CONDITION_AUTO://??????????????????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_auto_air, R.string.object_auto_air, ANJIANC70CONDITION, R.string.condition_default, R.string.anjian_c70, KEY_AIR_CONDITION_AUTO);
                        break;
                    case WIND_MODE://????????????
                        showWindMode();
                        break;
                    case AIR_CYCLE_MODE://????????????????????????
                        showRecycleMode();
                        break;
                    case AC_MAX://??????????????????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_max_ac, R.string.object_max_ac, ANJIANC78CONDITION, R.string.condition_default, R.string.anjian_c78, KEY_AC_MAX);
                        break;
                    case WIND_CHANGE://????????????
                        showKeyInfo(R.string.skill_key_center, R.string.scene_wind_change, R.string.object_wind_change, ANJIANC79CONDITION, R.string.condition_default, R.string.anjian_c79, KEY_WIND_CHANGE);
                        break;
                    case CAMERA://?????????????????????
                        showKeyInfo(R.string.skill_key_rearview, R.string.scene_dvr_snap_shoot, R.string.object_dvr_snap_shoot, ANJIANC80CONDITION, R.string.condition_default, R.string.anjian_c80, KEY_CAMERA);
                        break;
                    case CAMERA_EMERGE://????????????
                        showKeyInfo(R.string.skill_key_rearview, R.string.scene_dvr_emergency_record, R.string.object_dvr_emergency_record, ANJIANC81CONDITION, R.string.condition_default, R.string.anjian_c81, KEY_CAMERA_EMERGE);
                        break;
                    case SOS://????????????
                        showKeyInfo(R.string.skill_key_rearview, R.string.scene_sos, R.string.object_sos, ANJIANC82CONDITION, R.string.condition_default, R.string.anjian_c82, KEY_SOS);
                        break;
                    case DOME_LIGHT_1://?????????????????????-??????
                        break;
                    case DOME_LIGHT_2://?????????????????????-??????
                        break;
                    case SCUTTLE_SWITCH_ON://?????????
                        showKeyInfo(R.string.skill_key_rearview, R.string.scene_sunroof_on_off, R.string.object_sunroof_on_off, ANJIANC83CONDITION, R.string.condition_sunroof_on, R.string.anjian_c83, KEY_SCUTTLE_SWITCH_ON);
                        break;
                    case SCUTTLE_SWITCH_OFF://?????????
                        showKeyInfo(R.string.skill_key_rearview, R.string.scene_sunroof_on_off, R.string.object_sunroof_on_off, ANJIANC84CONDITION, R.string.condition_sunroof_off, R.string.anjian_c84, KEY_SCUTTLE_SWITCH_OFF);
                        break;
                    case ABAT_VENT_SWITCH_ON://????????????
                        showKeyInfo(R.string.skill_key_rearview, R.string.scene_sunshade_on_off, R.string.object_sunshade_on_off, ANJIANC85CONDITION, R.string.condition_sunshade_on, R.string.anjian_c85, KEY_ABAT_VENT_SWITCH_ON);
                        break;
                    case ABAT_VENT_SWITCH_OFF://????????????
                        showKeyInfo(R.string.skill_key_rearview, R.string.scene_sunshade_on_off, R.string.object_sunshade_on_off, ANJIANC86CONDITION, R.string.condition_sunshade_off, R.string.anjian_c86, KEY_ABAT_VENT_SWITCH_OFF);
                        break;
                    case AUTO_HEAD_STATUS://??????????????????-????????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_auto_head, R.string.object_auto_head, ANJIANC94CONDITION, R.string.condition_min, R.string.anjian_c94, KEY_LIGHT);
                        } else {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_auto_head, R.string.object_auto_head, ANJIANC95CONDITION, R.string.condition_max, R.string.anjian_c95, KEY_LIGHT);
                        }
                        break;
                    case LOW_BEAM_STATUS://??????????????????-??????(??????)
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_auto_head, R.string.object_auto_head, ANJIANC96CONDITION, R.string.condition_min, R.string.anjian_c94, KEY_LIGHT);
                        } else {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_auto_head, R.string.object_auto_head, ANJIANC97CONDITION, R.string.condition_max, R.string.anjian_c95, KEY_LIGHT);
                        }
                        break;
                    case POSITION_LAMP_STATUS://??????????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_position_lamp, R.string.object_position_lamp, ANJIANC98CONDITION, R.string.condition_min, R.string.anjian_c94, KEY_LIGHT);
                        } else {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_position_lamp, R.string.object_position_lamp, ANJIANC99CONDITION, R.string.condition_max, R.string.anjian_c95, KEY_LIGHT);
                        }
                        break;
                    case TURN_INDICATOR_LEFT://??????????????????-????????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_indicator_left, R.string.object_indicator_left, ANJIANC102CONDITION, R.string.condition_min, R.string.anjian_c94, KEY_LIGHT);
                        } else {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_indicator_left, R.string.object_indicator_left, ANJIANC103CONDITION, R.string.condition_max, R.string.anjian_c95, KEY_LIGHT);
                        }
                        break;
                    case TURN_INDICATOR_RIGHT://??????????????????-????????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_indicator_right, R.string.object_indicator_right, ANJIANC104CONDITION, R.string.condition_min, R.string.anjian_c94, KEY_LIGHT);
                        } else {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_indicator_right, R.string.object_indicator_right, ANJIANC105CONDITION, R.string.condition_max, R.string.anjian_c95, KEY_LIGHT);
                        }
                        break;
                    case REAR_FOGLAMP_STATUS://??????????????????-?????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_rear_foglamp, R.string.object_rear_foglamp, ANJIANC106CONDITION, R.string.condition_min, R.string.anjian_c94, KEY_LIGHT);
                        } else {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_rear_foglamp, R.string.object_rear_foglamp, ANJIANC107CONDITION, R.string.condition_max, R.string.anjian_c95, KEY_LIGHT);
                        }
                        break;
                    case HIGH_BEAM_STATUS://??????????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_high_beam, R.string.object_high_beam, ANJIANC108CONDITION, R.string.condition_min, R.string.anjian_c94, KEY_LIGHT);
                        } else {
                            showKeyInfo(R.string.skill_key_light, R.string.scene_high_beam, R.string.object_high_beam, ANJIANC109CONDITION, R.string.condition_max, R.string.anjian_c95, KEY_LIGHT);
                        }
                        break;
                    case FRONT_WIPER_STATUS_0://??????????????????-?????????OFF
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper0, R.string.object_front_wiper0, ANJIANC124CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper0, R.string.object_front_wiper0, ANJIANC125CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case FRONT_WIPER_STATUS_1://??????????????????-????????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper1, R.string.object_front_wiper1, ANJIANC114CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper1, R.string.object_front_wiper1, ANJIANC115CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case FRONT_WIPER_STATUS_2://??????????????????-????????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper2, R.string.object_front_wiper2, ANJIANC116CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper2, R.string.object_front_wiper2, ANJIANC117CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case FRONT_WIPER_STATUS_3://??????????????????-??????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper3, R.string.object_front_wiper3, ANJIANC110CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper3, R.string.object_front_wiper3, ANJIANC111CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case FRONT_WIPER_STATUS_4://??????????????????-????????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper4, R.string.object_front_wiper4, ANJIANC112CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wiper4, R.string.object_front_wiper4, ANJIANC113CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case FRONT_WASH_STATUS://??????????????????-?????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wash, R.string.object_front_wash, ANJIANC118CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_front_wash, R.string.object_front_wash, ANJIANC119CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case FRONT_WIPER_INTERVAL_TIME://??????????????????-????????????
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_interval_time, R.string.object_interval_time, ANJIANC120CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_interval_time, R.string.object_interval_time, ANJIANC121CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case REAR_WASH_STATUS_ON://??????????????????-?????????ON
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_rear_on, R.string.object_rear_on, ANJIANC126CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_rear_on, R.string.object_rear_on, ANJIANC127CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    case REAR_WASH_STATUS_OFF://??????????????????-?????????OFF
                        if (isMinSpeed()) {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_rear_off, R.string.object_rear_off, ANJIANC128CONDITION, R.string.condition_min, R.string.anjian_c110, KEY_WIPER);
                        } else {
                            showKeyInfo(R.string.skill_key_wiper, R.string.scene_rear_off, R.string.object_rear_off, ANJIANC129CONDITION, R.string.condition_max, R.string.anjian_c111, KEY_WIPER);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void unRegisterCallbackEvent() {
        Log.d(TAG, "lh:unRegisterCallbackEvent--");
        try {
            if (carCabinManager != null) {
                carCabinManager.unregisterCallback(carCabinEventCallback);
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    /*
     * ????????????????????????7Km/h
     */
    private boolean isMinSpeed() {
        boolean ismin = false;
        float speed = getSpeed();
        if (speed < 7) {
            ismin = true;
        }
        return ismin;
    }

    public void registerCallBackEvent() {
        Log.d(TAG, "lh:registerCallBackEvent--");
        if (AppConfig.INSTANCE.mCarApi != null) {
            try {
                isAnswer = false;
                carCabinManager = (CarCabinManager) AppConfig.INSTANCE.mCarApi.getCarManager(Car.CABIN_SERVICE);

                mCarSensorManager = (CarSensorManager)AppConfig.INSTANCE.mCarApi.getCarManager(Car.SENSOR_SERVICE);

                if (mCarSensorManager!=null){
                    mCarSensorManager.registerListener(listener,CarSensorManager.SENSOR_TYPE_POWER_STATE, 100);
                }

                carCabinManager.registerCallback(carCabinEventCallback);

            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        //???????????????
//        EventBusUtils.sendTalkMessage(intentEntity.text);
        Log.d(TAG, "lh:registerReceiver----");

        if(intentEntity.semantic==null||intentEntity.semantic.slots==null)
            doExceptonAction(mContext);

        String category = intentEntity.semantic.slots.category;
        if (!TextUtils.isEmpty(category)) {
            boolean isKey = category.contains("??????") || category.contains("??????")
                    || category.contains("??????") || category.contains("??????")
                    || category.contains("??????") || category.contains("??????")
                    || category.contains("??????");

            if (isKey) {
                doExceptonAction(mContext);
                //???????????????.??????int???:5???START???5:ON
              /*  int type = getACStatus();
                if (type >= CarSensorEvent.IGNITION_STATE_ACC) {
                    mContext.registerReceiver(receiver, new IntentFilter(ACTION_BROADCAST_REV));
                    startTTS("??????????????????????????????");
                    isAnswer = true;
                    sendKeyAction(KEY_QUERY_IN);
                    mHandler.sendEmptyMessageDelayed(MSG_WAIT_KEY, 10 * 1000);
                    CarKeyViewManager.getInstance(mContext).showActionTipView(CarKeyViewManager.ActionType.ACTION_TYPE_KEY);
                } else {
                    startTTS("??????????????????????????????????????????????????????");
                    mHandler.sendEmptyMessageDelayed(MSG_WAIT_ENGINE, 10 * 1000);
                    CarKeyViewManager.getInstance(mContext).showActionTipView(CarKeyViewManager.ActionType.ACTION_TYPE_ENGINE);
                }
                countDownTime = 10;
                CarKeyViewManager.getInstance(mContext).updateTimeView(countDownTime);
                mHandler.sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);*/
            } else if (PlatformConstant.Operation.LAUNCH.equals(intentEntity.operation) && "??????".equals(category)) {
                //??????????????????
                Utils.getMessageWithTtsSpeak(mContext, TtsConstant.SYSTEMC24CONDITION, mContext.getString(R.string.systemC24), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Intent intent = new Intent(mContext, SettingsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(SettingsActivity.KEY_ID, AppConstant.VOICE_COMMAND_ID);
                        mContext.startActivity(intent);
                    }
                });
            }else
                doExceptonAction(mContext);
        } else {
            //?????????/???????????????
            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.HELP1CONDITION, mContext.getString(R.string.helpC1), new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                   /* Message msg = mHandler.obtainMessage(MSG_TTS, tts);
                    mHandler.sendMessageDelayed(msg, 1000);*/
                    Utils.eventTrack(mContext, R.string.skill_help, R.string.scene_help, R.string.condition_help, TtsConstant.HELP1CONDITION, R.string.condition_helpC1,tts);
                    startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            //????????? ????????????
                            Intent intent = new Intent(mContext, SettingsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(SettingsActivity.KEY_ID,AppConstant.VOICE_COMMAND_ID);
                            mContext.startActivity(intent);
                        }
                    });
                }
            });


        }
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }


    private void sendKeyAction(int status) {
        //????????????????????????
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putInt("status", status);
        b.putString("action", "btn.request");
        i.setAction(ACTION_BROADCAST_SEND);
        i.putExtra("key_type", 4200);
        i.putExtras(b);
        mContext.sendBroadcast(i);
    }

    private int getACStatus() {
//        int status = -1;
//        try {
//            CarSensorEvent mIgnitionEvent = AppConfig.INSTANCE.mCarSensorManager.getLatestSensorEvent(CarSensorManager.SENSOR_TYPE_IGNITION_STATE);
//            status = mIgnitionEvent.getIgnitionStateData(null).ignitionState;
//            Log.d(TAG, "lh:get ignition status(default:-1)----" + status);
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
//        return status;
        return  ignitionState;

    }

    public void exit() {
        sendKeyAction(KEY_QUERY_EXIT);
        mHandler.removeCallbacksAndMessages(null);
        try {
            //????????????????????????????????????
            mContext.unregisterReceiver(receiver);
        } catch (Exception e) {
            // TODO: handle exception
        }
        CarKeyViewManager.getInstance(mContext).hideActionTipView();
        isAnswer = false;
    }

    private void sendHandleMessage(int what, Object value) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = value;
        mHandler.sendMessage(msg);
    }

    private void showKeyInfo(int appName, int scene, int object, String conditionId, int condition, int defaultTts, CarKeyViewManager.KeyType keyType) {
        if (keyType != null) {
            Log.d(TAG, "lh:keytype:" + keyType);
            CarKeyViewManager.getInstance(mContext).showKeyTipView(keyType);
        }
        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(defaultTts), new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                if (!FloatViewManager.getInstance(mContext).isHide()) {
                    FloatViewManager.getInstance(mContext).hide();
                }
            }
        });
        exit();
        Utils.eventTrack(mContext,appName,scene,object,conditionId,condition);
    }

    //?????????????????????????????????????????????;
    public CarCabinManager.CarCabinEventCallback carCabinEventCallback = new CarCabinManager.CarCabinEventCallback() {

        @Override
        public void onChangeEvent(CarPropertyValue carPropertyValue) {
            if (!isAnswer) return;
            Log.d(TAG, "lh:properId:" + carPropertyValue.getPropertyId() + ",status:" + carPropertyValue.getStatus() + ",hexProperty:" + Integer.toHexString(carPropertyValue.getPropertyId()));
            if (carPropertyValue.getPropertyId() == CarCabinManager.ID_KEY_QUERY_INFO) {
                Integer[] value = (Integer[]) carPropertyValue.getValue();
                Log.d(TAG, "lh:anjian---key:" + value[0] + "???is pressed???" + (value.length > 1 ? value[1] : null) + ",status:" + carPropertyValue.getStatus() + ",getAreaId:" + carPropertyValue.getAreaId() + ",value.length=" + value.length);
                if (value == null || value.length == 0) return;
                if (value[0] == VEHICLE.KEY_IP_UP) { //?????????????????????
                    mHandler.sendEmptyMessage(METER_PAGE_LAST);
                } else if (value[0] == VEHICLE.KEY_IP_DOWN) {//?????????????????????
                    mHandler.sendEmptyMessage(METER_PAGE_NEXT);
                } else if (value[0] == VEHICLE.KEY_IP_CONFIRM) {//??????????????????
                    mHandler.sendEmptyMessage(METER_CONFIRM);
                } else if (value[0] == VEHICLE.KEY_IP_MENU) { //???????????????
                    mHandler.sendEmptyMessage(METER_CHANGE);
                } else if (value[0] == VEHICLE.KEY_EPB_SWITCH) { //??????????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) { //?????????
                        mHandler.sendEmptyMessage(ELEC_PARKING_SWITCH_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) {
                        mHandler.sendEmptyMessage(ELEC_PARKING_SWITCH_OFF);
                    }
                } else if (value[0] == VEHICLE.KEY_HVAC_RECIRC_MODE) {//??????????????????
                    mHandler.sendEmptyMessage(AIR_CYCLE_MODE);
                } else if (value[0] == VEHICLE.KEY_PTS_SWITCH) {//???????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.NO) {//??????????????????
                        mHandler.sendEmptyMessage(TRUNK_OFF);
                    } else if (tmpIntStatus == VEHICLE.YES) {//??????????????????
                        mHandler.sendEmptyMessage(TRUNK_ON);
                    }
                } else if (value[0] == VEHICLE.KEY_HVAC_FRONT_AUTO) {//??????????????????
                    mHandler.sendEmptyMessage(AIR_CONDITION_AUTO);
                } else if (value[0] == VEHICLE.KEY_FRONT_DEFROST) {//?????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) { //??????????????????
                        mHandler.sendEmptyMessage(DEFROST_FRONT_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) { //??????????????????
                        mHandler.sendEmptyMessage(DEFROST_FRONT_OFF);
                    }
                } else if (value[0] == VEHICLE.KEY_REAR_DEFROST) { //?????????
//                    noSpeedAction(DEFROST_BACK, value[1]);
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) { //??????????????????
                        mHandler.sendEmptyMessage(DEFROST_BACK_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) { //??????????????????
                        mHandler.sendEmptyMessage(DEFROST_BACK_OFF);
                    }
                } else if (value[0] == VEHICLE.KEY_HVAC_POWER_OFF) { //????????????
                    mHandler.sendEmptyMessage(AIR_CONDITION_OFF);
                } else if (value[0] == VEHICLE.KEY_HVAC_FAN_DIRECTION_SWITCH) { //????????????
                    mHandler.sendEmptyMessage(WIND_MODE);
                } else if ((value[0] == VEHICLE.KEY_HVAC_FAN_SPEED_UP || value[0] == VEHICLE.KEY_HVAC_FAN_SPEED_DOWN)) { //????????????
                    mHandler.sendEmptyMessage(WIND_CHANGE);
                } else if ((value[0] == VEHICLE.KEY_TEMP_UP || value[0] == VEHICLE.KEY_TEMP_DOWN)) {
                    mHandler.sendEmptyMessage(TEMP_CHANGE);
                } else if (value[0] == VEHICLE.KEY_HVAC_MAX_AC_SWITCH) {//????????????
                    mHandler.sendEmptyMessage(AC_MAX);
                } else if (value[0] == KEY_ESP_AUTO_HOLD) {//????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) { //?????????
                        mHandler.sendEmptyMessage(AUTO_HOLD_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) {//?????????
                        mHandler.sendEmptyMessage(AUTO_HOLD_OFF);
                    }
                } else if (value[0] == KEY_APA_SWITCH) {//????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) { //?????????
                        mHandler.sendEmptyMessage(AUTO_PARK_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) {//?????????
                        mHandler.sendEmptyMessage(AUTO_PARK_OFF);
                    }
                } else if (value[0] == VEHICLE.KEY_AC_SWITCH) {//???????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) { //?????????????????????
                        mHandler.sendEmptyMessage(AC_SWITCH_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) {//?????????????????????
                        mHandler.sendEmptyMessage(AC_SWITCH_OFF);
                    }
                } else if (value[0] == KEY_ESP_SWITCH) {//??????????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.NO) { //??????
                        mHandler.sendEmptyMessage(ESC_OFF);
                    } else if (tmpIntStatus == VEHICLE.YES) {//??????
                        mHandler.sendEmptyMessage(ESC_ON);
                    }
                } else if (value[0] == KEY_ACC_INDICATE) {//???????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) {
                        mHandler.sendEmptyMessage(CRUISE_INDICATE_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) {
                        mHandler.sendEmptyMessage(CRUISE_INDICATE_OFF);
                    }
                } else if (value[0] == KEY_ACC_CANCEL) {//????????????
                    int tmpIntStatus = value[1];
                    if (tmpIntStatus == VEHICLE.YES) {
                        mHandler.sendEmptyMessage(CRUISE_CANCEL_ON);
                    } else if (tmpIntStatus == VEHICLE.NO) {
                        mHandler.sendEmptyMessage(CRUISE_CANCEL_OFF);
                    }
                } else if (value[0] == KEY_ACC_RESUME) {
                    sendHandleMessage(METER_PAGE_LAST, null);
                } else if (value[0] == KEY_ACC_SET) {
                    sendHandleMessage(METER_PAGE_LAST, null);
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_AUTO_HEAD_LIGHT) {//????????????
                Log.d(TAG, "lh:body auto head light----status:");
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VEHICLE.ON || tmpIntStatus == VEHICLE.OFF) {
                    mHandler.sendEmptyMessage(AUTO_HEAD_STATUS);
                }
            } else if (carPropertyValue.getPropertyId() == ID_HEADLIGHTS_SWITCH) {//??????(??????)
                Log.d(TAG, "lh:headlights switch----status:");
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VehicleLightSwitch.ON || tmpIntStatus == VehicleLightSwitch.OFF) {
                    mHandler.sendEmptyMessage(LOW_BEAM_STATUS);
                }
            } else if (carPropertyValue.getPropertyId() == ID_NIGHT_MODE) {//??????
                Log.d(TAG, "lh:night mode---status:");
                mHandler.sendEmptyMessage(POSITION_LAMP_STATUS);
            } else if (carPropertyValue.getPropertyId() == ID_BODY_TURN_LEFT_SIGNAL_STATE) {//????????????
                Log.d(TAG, "lh:body turn left----status:");
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VehicleLightState.ON || tmpIntStatus == VehicleLightState.OFF) {
                    mHandler.sendEmptyMessage(TURN_INDICATOR_LEFT);
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_TURN_RIGHT_SIGNAL_STATE) {//????????????
                Log.d(TAG, "lh:body turn right sinal----status:");
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VehicleLightState.ON || tmpIntStatus == VehicleLightState.OFF) {
                    mHandler.sendEmptyMessage(TURN_INDICATOR_RIGHT);
                }
            } else if (carPropertyValue.getPropertyId() == ID_FOG_LIGHTS_STATE) {//?????????
                Log.d(TAG, "lh:fog lights----status:");
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VehicleLightState.ON || tmpIntStatus == VehicleLightState.OFF) {
                    mHandler.sendEmptyMessage(REAR_FOGLAMP_STATUS);
                }
            } else if (carPropertyValue.getPropertyId() == ID_HIGH_BEAM_LIGHTS_STATE) {//??????
                Log.d(TAG, "lh:high beam lights----status:");
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VehicleLightState.ON || tmpIntStatus == VehicleLightState.OFF) {
                    mHandler.sendEmptyMessage(HIGH_BEAM_STATUS);
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_WINDOW_WIPER) {
                Object obj = carPropertyValue.getValue();
                if (obj instanceof Integer) {
                    int areaId = carPropertyValue.getAreaId();
                    int value = (int) carPropertyValue.getValue();
                    if (areaId == WINDOW_FRONT_WINDSHIELD) { //?????????
                        if (value == VEHICLE.WIPER_OFF) {//?????????OFF
                            mHandler.sendEmptyMessage(FRONT_WIPER_STATUS_0);
                        } else if (value == VEHICLE.WIPER_LOW) {//????????????
                            mHandler.sendEmptyMessage(FRONT_WIPER_STATUS_1);
                        } else if (value == VEHICLE.WIPER_HIGH) {//????????????
                            mHandler.sendEmptyMessage(FRONT_WIPER_STATUS_2);
                        } else if (value == VEHICLE.WIPER_INT) {//??????
                            mHandler.sendEmptyMessage(FRONT_WIPER_STATUS_3);
                        } else if (value == VEHICLE.WIPER_MIST) {//????????????
                            mHandler.sendEmptyMessage(FRONT_WIPER_STATUS_4);
                        }
                    } else if (areaId == WINDOW_REAR_WINDSHIELD) {//?????????
                        if (value == VEHICLE.WIPER_OFF) {//?????????OFF
                            mHandler.sendEmptyMessage(REAR_WASH_STATUS_OFF);
                        } else if (value == VEHICLE.WIPER_LOW || value == VEHICLE.WIPER_HIGH || value == VEHICLE.WIPER_INT || value == VEHICLE.WIPER_MIST) {//?????????ON
                            mHandler.sendEmptyMessage(REAR_WASH_STATUS_ON);
                        }
                    }
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_WINDOW_WASH) {//?????????
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VEHICLE.ON || tmpIntStatus == VEHICLE.OFF) {
                    mHandler.sendEmptyMessage(FRONT_WASH_STATUS);
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_WINDOW_WIPER_INTERVAL) {//????????????
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VEHICLE.ON || tmpIntStatus == VEHICLE.OFF) {
                    mHandler.sendEmptyMessage(FRONT_WIPER_INTERVAL_TIME);
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_WINDOW_SUNROOF_SUNVISOR_STATUS) {//??????/???????????????
                int areaId = carPropertyValue.getAreaId();
                if (areaId == WINDOW_ROOF_TOP_1) { //??????
                    int tmpIntStatus = (int) carPropertyValue.getValue();
                    if (tmpIntStatus == VEHICLE.SUNROOF_OPENING) {//??????????????????
                        mHandler.sendEmptyMessage(SCUTTLE_SWITCH_ON);
                    } else if (tmpIntStatus == VEHICLE.SUNROOF_CLOSING) {//??????????????????
                        mHandler.sendEmptyMessage(SCUTTLE_SWITCH_OFF);
                    }
                } else if (areaId == WINDOW_ROOF_TOP_2) {//?????????
                    int tmpIntStatus = (int) carPropertyValue.getValue();
                    if (tmpIntStatus == VEHICLE.SUNVISOR_OPENING) { //?????????????????????
                        mHandler.sendEmptyMessage(ABAT_VENT_SWITCH_ON);
                    } else if (tmpIntStatus == VEHICLE.SUNVISOR_CLOSING) { //?????????????????????
                        mHandler.sendEmptyMessage(ABAT_VENT_SWITCH_OFF);
                    }
                }
            } else if (carPropertyValue.getPropertyId() == ID_DVR_EMERGENCY_RECORD) {//??????????????????
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == DVR.DVR_EMERGENCY_RECORD_SUCCESS || tmpIntStatus == DVR.DVR_EMERGENCY_RECORD_FAILED || tmpIntStatus == DVR.DVR_EMERGENCY_RECORD_SAVING || tmpIntStatus == DVR.DVR_EMERGENCY_RECORD_REASSURE) {
                    mHandler.sendEmptyMessage(CAMERA_EMERGE);
                }
            } else if (carPropertyValue.getPropertyId() == ID_DVR_SNAP_SHOOT) {//????????????
                int value = (int) carPropertyValue.getValue();
                if (value == DVR.DVR_SNAP_SHOOT_SUCCESS || value == DVR.DVR_SNAP_SHOOT_FAILED) {
                    mHandler.sendEmptyMessage(CAMERA);
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_EMERGENCY_LIGHT) {//????????????
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VehicleLightState.ON || tmpIntStatus == VehicleLightState.OFF) {
                    mHandler.sendEmptyMessage(DANGER_BTN);
                }
            } else if (carPropertyValue.getPropertyId() == ID_BODY_WINDOW_UP_DOWN_STATUS) {
                int areaId = carPropertyValue.getAreaId();
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (areaId == WINDOW_ROW_1_LEFT) { //????????????????????????
                    if (tmpIntStatus == VEHICLE.ON || tmpIntStatus == VEHICLE.OFF) {
                        mHandler.sendEmptyMessage(MAIN_DRIVE_FALL);
                    }
                } else if (areaId == WINDOW_ROW_1_RIGHT || areaId == WINDOW_ROW_2_LEFT || areaId == WINDOW_ROW_2_RIGHT) {//????????????????????????
                    if (tmpIntStatus == VEHICLE.ON || tmpIntStatus == VEHICLE.OFF) {
                        mHandler.sendEmptyMessage(OTHER_DOOR_FALL);
                    }
                }
            } else if (carPropertyValue.getPropertyId() == ID_WINDOW_LOCK) {//?????????????????????
                boolean tmpBooleanStatus = (boolean) carPropertyValue.getValue();
                if (tmpBooleanStatus) {//??????
                    mHandler.sendEmptyMessage(WINDOW_LOCK);
                } else {//??????
                    mHandler.sendEmptyMessage(WINDOW_UNLOCK);
                }
            } else if (carPropertyValue.getPropertyId() == ID_DOOR_LOCK_STATUS) {//????????????????????????
                Log.d(TAG, "lh:door lock----status:" + carPropertyValue.getValue());
                boolean tmpBooleanStatus = (boolean) carPropertyValue.getValue();
                if (tmpBooleanStatus) {//??????
                    mHandler.sendEmptyMessage(DOOR_LOCK);
                } else {//??????
                    mHandler.sendEmptyMessage(DOOR_UNLOCK);
                }
            } else if (carPropertyValue.getPropertyId() == ID_VENDOR_DRIVER_MODE_SET) {//????????????????????????
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == VEHICLE.DRIVING_MODE_NORMAL) { //????????????
                    mHandler.sendEmptyMessage(DRIVE_CHANGE_NORMAL);
                } else if (tmpIntStatus == VEHICLE.DRIVING_MODE_SPORT) { //????????????
                    mHandler.sendEmptyMessage(DRIVE_CHANGE_SPORT);
                } else if (tmpIntStatus == VEHICLE.DRIVING_MODE_ECO) { //????????????
                    mHandler.sendEmptyMessage(DRIVE_CHANGE_ECO);
                }
            } else if (carPropertyValue.getPropertyId() == ID_AVM_DEF_OPEN) {//????????????
                int tmpIntStatus = (int) carPropertyValue.getValue();
                if (tmpIntStatus == AVM.AVM_ON) {//?????????
                    mHandler.sendEmptyMessage(VIEW_OVERALL_ON);
                } else if (tmpIntStatus == AVM.AVM_OFF) { //?????????
                    mHandler.sendEmptyMessage(VIEW_OVERALL_OFF);
                }
            } else if (carPropertyValue.getPropertyId() == ID_VENDOR_ECALL_STATE) {//sos
                int value = (int) carPropertyValue.getValue();
                mHandler.sendEmptyMessage(SOS);
            }
        }

        @Override
        public void onErrorEvent(int i, int i1) {

        }
    };

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "lh:action---" + action);
            if (ACTION_BROADCAST_REV.equals(action)) {
                int keyType = intent.getIntExtra("key_type", 0);
                Log.d(TAG, "lh:keyType---" + keyType);
                keyType = 3300;
                if (keyType == 3300) {
                    Bundle bundle = intent.getExtras();
                    String bAction = bundle.getString("action");
                    Log.d(TAG, "lh:bundle---" + bundle.toString() + ",action:" + bAction);
                    bAction = "btn.notify";
                    if ("btn.notify".equals(bAction)) {
                        CarKeyViewManager.getInstance(mContext).hideActionTipView();
                        mHandler.removeCallbacksAndMessages(null);
                        String status = bundle.getString("status");
                        Log.d(TAG, "lh:status---" + status);
//                      String status = intent.getStringExtra("status");
                        boolean needSpeed = false;
                        String text = "";
                        // TODO ??????????????????????????????
                        if (SRC.equals(status) || VOLUP.equals(status) || VOLDOWN.equals(status)
                                || MUTE.equals(status) || VR.equals(status) || TEL.equals(status) || HANDUP.equals(status)) {
                            //???????????????????????????????????????
                            noSpeedAction(status);
                        }
                    }
                }

            }
        }
    };

    /**
     * ????????????
     */
    public float getSpeed() {
        float status = -1;
        try {
            status = AppConfig.INSTANCE.mCarMcuManager.getFloatProperty(CarMcuManager.ID_PERF_VEHICLE_SPEED,
                    VEHICLE_AREA_TYPE_GLOBAL);
            Log.d(TAG, "lh:get speed(default:-1)---" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return status;
    }

    private void showWindMode() {
        int mCycleStatus = getWindMode();
        if (mCycleStatus == HVAC.HVAC_FAN_DIRECTION_FACE) { //????????????
            showKeyInfo(R.string.skill_key_center, R.string.scene_wind_mode, R.string.object_wind_mode, ANJIANC71CONDITION, R.string.condition_wind_mode1, R.string.anjian_c71, KEY_WIND_MODE);
        } else if (mCycleStatus == HVAC.HVAC_FAN_DIRECTION_FLOOR) {//????????????
            showKeyInfo(R.string.skill_key_center, R.string.scene_wind_mode, R.string.object_wind_mode, ANJIANC72CONDITION, R.string.condition_wind_mode2, R.string.anjian_c72, KEY_WIND_MODE);
        } else if (mCycleStatus == HVAC.HVAC_FAN_DIRECTION_FACE_FLOOR) {//??????????????????
            showKeyInfo(R.string.skill_key_center, R.string.scene_wind_mode, R.string.object_wind_mode, ANJIANC73CONDITION, R.string.condition_wind_mode3, R.string.anjian_c73, KEY_WIND_MODE);
        } else {//??????????????????
            showKeyInfo(R.string.skill_key_center, R.string.scene_wind_mode, R.string.object_wind_mode, ANJIANC74CONDITION, R.string.condition_wind_mode4, R.string.anjian_c74, KEY_WIND_MODE);
        }
    }

    private void showRecycleMode() {
        //????????????????????????
        int mCycleStatus = getAirCircleMode();
        if (mCycleStatus == HVAC.LOOP_INNER) { //???????????????
            showKeyInfo(R.string.skill_key_center, R.string.scene_recycle_mode, R.string.object_recycle_mode, ANJIANC75CONDITION, R.string.condition_recycle_mode1, R.string.anjian_c75, KEY_AIR_CYCLE_MODE);
        } else if (mCycleStatus == HVAC.LOOP_OUTSIDE) {//???????????????
            showKeyInfo(R.string.skill_key_center, R.string.scene_recycle_mode, R.string.object_recycle_mode, ANJIANC76CONDITION, R.string.condition_recycle_mode2, R.string.anjian_c76, KEY_AIR_CYCLE_MODE);
        } else {//??????????????????
            showKeyInfo(R.string.skill_key_center, R.string.scene_recycle_mode, R.string.object_recycle_mode, ANJIANC77CONDITION, R.string.condition_recycle_mode3, R.string.anjian_c77, KEY_AIR_CYCLE_MODE);
        }
    }

    private int getAirCircleMode() {
        int mCycleStatus = -1;
        try {
            mCycleStatus = AppConfig.INSTANCE.mCarHvacManager.getIntProperty(ID_HVAC_RECIRC_ON, SEAT_ROW_1_LEFT);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "lh:get circle mode:" + mCycleStatus);
        return mCycleStatus;
    }

    private int getWindMode() {
        int mCycleStatus = -1;
        try {
            mCycleStatus = AppConfig.INSTANCE.mCarHvacManager.getIntProperty(ID_HVAC_FAN_DIRECTION, SEAT_ROW_1_LEFT);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "lh:get wind mode:" + mCycleStatus);
        return mCycleStatus;
    }

    private boolean isRunning() {
        float speed = getSpeed();
        if (speed > 0) {
            return true;
        }
        return false;
    }

    /**
     * ??????????????????????????????
     *
     * @param type
     */
    private void noSpeedAction(String type) {
        String text = "";
        String conditionId = "";
        CarKeyViewManager.KeyType keyType = null;
        if (SRC.equals(type)) {//???????????????
            showKeyInfo(R.string.skill_key_wheel, R.string.scene_src, R.string.object_src, ANJIANC25CONDITION, R.string.condition_default, R.string.anjian_c25, KEY_SRC);
        } else if (VOLUP.equals(type)) {//???????????????
            showKeyInfo(R.string.skill_key_wheel, R.string.scene_voice, R.string.object_voice, ANJIANC26CONDITION, R.string.condition_volup, R.string.anjian_c26, KEY_VOLUP);
        } else if (VOLDOWN.equals(type)) {//???????????????
            showKeyInfo(R.string.skill_key_wheel, R.string.scene_voice, R.string.object_voice, ANJIANC27CONDITION, R.string.condition_voldown, R.string.anjian_c27, KEY_VOLDOWN);
        } else if (MUTE.equals(type)) {//????????????
            showKeyInfo(R.string.skill_key_wheel, R.string.scene_voice, R.string.object_voice, ANJIANC28CONDITION, R.string.condition_mute, R.string.anjian_c28, KEY_MUTE);
        } else if (HANDUP.equals(type)) {//????????????????????????????????????
            showKeyInfo(R.string.skill_key_wheel, R.string.scene_hang_up, R.string.object_hang_up, ANJIANC29CONDITION, R.string.condition_default, R.string.anjian_c29, KEY_HANGUP);
        } else if (TEL.equals(type)) {//????????????????????????????????????
            showKeyInfo(R.string.skill_key_wheel, R.string.scene_tel, R.string.object_tel, ANJIANC30CONDITION, R.string.condition_default, R.string.anjian_c30, KEY_TEL);
        }
    }

    /**
     * ????????????????????? ????????????
     */

    private final CarSensorManager.OnSensorChangedListener listener = new CarSensorManager.OnSensorChangedListener() {
        @Override
        public void onSensorChanged(CarSensorEvent carSensorEvent) {
            Log.d(TAG, "lh:body turn ??????????????????:"+carSensorEvent.sensorType);
            switch (carSensorEvent.sensorType) {
                case CarSensorManager.SENSOR_TYPE_POWER_STATE:
                    //ignitionState =  carSensorEvent.getIgnitionStateData(null).ignitionState;       //????????????????????? ????????????
                    ignitionState =  carSensorEvent.getPowerStateData(null).powerState;
                    Log.d(TAG, "lh:body turn ????????????????????? ????????????(default:-1) --------------status:"+ignitionState);

                    break;
                default:
                    break;
            }
        }
    };
}
