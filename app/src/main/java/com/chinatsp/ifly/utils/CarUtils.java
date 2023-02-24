package com.chinatsp.ifly.utils;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.VehicleAreaDoor;
import android.car.VehicleAreaSeat;
import android.car.VehicleAreaType;
import android.car.VehicleAreaWindow;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.cabin.CarCabinManager;
import android.car.hardware.constant.AVM;
import android.car.hardware.constant.DVR;
import android.car.hardware.constant.HVAC;
import android.car.hardware.constant.VEHICLE;
import android.car.hardware.constant.VehicleAreaId;
import android.car.hardware.hvac.CarHvacManager;
import android.car.hardware.mcu.CarMcuManager;
import android.content.Context;
import android.hardware.automotive.vehicle.V2_0.BCallTYPE;
import android.hardware.automotive.vehicle.V2_0.DoorLockStatus;
import android.hardware.automotive.vehicle.V2_0.VehicleLightState;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.autofill.AutofillId;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.entity.CommandEvent;
import com.chinatsp.ifly.module.seachlist.SearchListFragment;
import com.chinatsp.ifly.remote.RemoteManager;
import com.chinatsp.ifly.voice.platformadapter.controller.AVMController;
import com.chinatsp.ifly.voice.platformadapter.controller.ApaController;
import com.chinatsp.ifly.voice.platformadapter.controller.CMDController;
import com.chinatsp.ifly.voice.platformadapter.controller.ChairController;
import com.chinatsp.ifly.voice.platformadapter.controller.CruiseController;
import com.chinatsp.ifly.voice.platformadapter.controller.DrivingCareController;
import com.chinatsp.ifly.voice.platformadapter.controller.KeyGuideController;
import org.greenrobot.eventbus.EventBus;

import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.controller.VirtualControl;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;

import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static android.car.VehicleAreaWindow.WINDOW_ROW_1_LEFT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_1_RIGHT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_2_LEFT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_2_RIGHT;
import static android.car.hardware.CarSensorEvent.GEAR_THIRD;
import static android.car.hardware.CarSensorManager.SENSOR_RATE_NORMAL;
import static android.car.hardware.cabin.CarCabinManager.ID_ADAS_AVH_SWITCH;
import static android.car.hardware.cabin.CarCabinManager.ID_ADAS_HDC_CONTROL_ON;
import static android.car.hardware.cabin.CarCabinManager.ID_AVH_AVAILABLE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_AUTO_HEAD_LIGHT;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_BODY_LIGHT_SMART_WELCOMELIGHT_ON;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_LIGHT_ATMO_BRIGHT_LEVEL;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_LIGHT_ATMO_COLOR_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_LIGHT_ATMO_ON;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_POS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_POS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_SUNVISOR_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNSHADE_POS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNSHADE_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_UP_DOWN_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_UP_DOWN_STATUS_NEW;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_WIPER;
import static android.car.hardware.cabin.CarCabinManager.ID_CRUISE_TARGET_SPEED;
import static android.car.hardware.cabin.CarCabinManager.ID_DOOR_LOCK_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_DOOR_POS;
import static android.car.hardware.cabin.CarCabinManager.ID_DRIVRER_ENG_TORQUE_OVER_ACC;
import static android.car.hardware.cabin.CarCabinManager.ID_DVR_REC_SWITCH;
import static android.car.hardware.cabin.CarCabinManager.ID_DVR_SNAP_SHOOT;
import static android.car.hardware.cabin.CarCabinManager.ID_FAULT_SYSTEM_CODE_INFO_ALL;
import static android.car.hardware.cabin.CarCabinManager.ID_FOG_LIGHTS_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_HEADLIGHTS_STATE;
import static android.car.hardware.cabin.CarCabinManager.ID_KEY_QUERY_INFO;
import static android.car.hardware.cabin.CarCabinManager.ID_KEY_SMART_LOCK;
import static android.car.hardware.cabin.CarCabinManager.ID_KEY_SMART_UNLOCK;
import static android.car.hardware.cabin.CarCabinManager.ID_RESTMODE_SAVE_REQ;
import static android.car.hardware.cabin.CarCabinManager.ID_SAFE_BELT_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_STARTUP_BTN_STATUS;
import static android.car.hardware.cabin.CarCabinManager.ID_WINDOWAPA_INITED;
import static android.car.hardware.cabin.CarCabinManager.ID_WINDOW_LOCK;
import static android.car.hardware.cabin.CarCabinManager.ID_WINDOW_POS;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_CTRL_STATUS;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_ENABLE;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_RESUME;
import static android.car.hardware.constant.VEHICLE.KEY_ACC_SET;
import static android.car.hardware.constant.VEHICLE.KEY_APA_SWITCH;
import static android.car.hardware.constant.VEHICLE.KEY_CRUISE_UNAVAIL_DISPLAY;
import static android.car.hardware.constant.VEHICLE.KEY_ESP_AUTO_HOLD;
import static android.car.hardware.constant.VEHICLE.KEY_ESP_SWITCH;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_FRONT_WINDSHIELD;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_REAR_WINDSHIELD;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_ROOF_TOP_1;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_ROOF_TOP_2;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_AC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_AUTO_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_DEFROSTER;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_FAN_DIRECTION;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_FAN_SPEED_ACK;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_MAX_AC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_POWER_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_RECIRC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_TEMPERATURE_LV_SET;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_TEMPERATURE_SET;
import static java.lang.Float.toHexString;
import static android.car.hardware.mcu.CarMcuManager.ID_VENDOR_ECALL_STATE;


public class CarUtils {

    private static final String TAG = "CarUtils";
    public static final int MODE_CHAIR_SLEEP = 0X08;
    public static final int MODE_CHAIR_DRIVING = 0X05;
    private static CarUtils mCarUtils;
    private Context mContext;
    private CarCabinManager mCarCabinManager = null;
    private CarMcuManager mCarMcuManager = null;
    private CarSensorManager mCarSensorManager = null;
    private CarHvacManager mCarHvacManager = null;
    private boolean isVoiceGuideOpen = false;
    private boolean isVoiceBroadcastOpen = false;
    private static final int MAXSPEED60 = 60;
    private static final int MAXSPEED100 = 100;
    private static final int MSG_BTNC20 = 1020;
    private static final int MSG_BTNC21 = 1021;
    private static final int MSG_BTNC38_UNLOCK = 10381;
    private static final int MSG_BTNC38_LOCK = 10382;
    private static final int MSG_BTNC44 = 1044;
    private static final int MSG_BTNC45 = 1045;
    private static final int MSG_BTNC46 = 1046;
    private static final int MSG_BTNC47_ON = 10471;
    private static final int MSG_BTNC47_OFF = 10472;
    private static final int MSG_BTNC48_1_ON = 104811;
    private static final int MSG_BTNC48_1_OFF = 104812;
    private static final int MSG_BTNC49_1_ON = 104911;
    private static final int MSG_BTNC49_1_OFF = 104912;
    private static final int MSG_BTNC51 = 1051;
    private static final int MSG_BTNC45_2 = 10452;
    private static final int MSG_BTNC50 = 1050;
    private static final int MSG_BTNC52 = 1052;
    private static final int MSG_BTNC53_INNER = 10531;
    private static final int MSG_BTNC53_OUTSIDE = 10532;
    private static final int MSG_BTNC54_ON = 10541;
    private static final int MSG_BTNC54_OFF = 10542;
    private static final int MSG_STANDBY_SET = 20103;
    private static final int MSG_STANDBY_SET_CC5 = 20105;
    private static final int MSG_STANDBY_RES = 2011;
    private static final int MSG_CCC4 = 2014;
    private static final int MSG_STANDBY_SET_CCC19 = 20119;
    private static final int MSG_STANDBY_RES_BROADCAST = 20111;
    private static final int MSG_CCC1 = 2001;
    private static final int MSG_CCC6 = 2006;
    private static final int MSG_CCC7 = 2007;
    private static final int MSG_CCC9 = 2009;
    private static final int MSG_CCC12 = 2012;
    private static final int MSG_CCC16 = 2016;
    private static final int MSG_CCC17 = 2017;
    private static final int MSG_CCC18_CCC24 = 201824;
    private static final int MSG_CCC22 = 2022;
    private static final int MSG_CCC23 = 2023;
    private static final int MSG_CCC25 = 2025;
    private static final int WAITTIME = 400;
    private static final int ACTIONWAITTIME = 850;
    private static final int COUNTPOWERTIME = 3001;
    private static final int COUNTPOWERTIMEFORAUTOWIPER = 3002;
    private static boolean isPowerOn = false;
    private static boolean isPowerOnForAutoWiper = false;
    private static final int CCSWITCHPRESSED = 4001;
    private static final int BRAKEPRESSED = 4002;
    private static final int SETPRESSED = 4003;
    private static final int RESPRESSED = 4004;
    private static final int DIRECTIONPRESSED = 4005;
    private static final int DOORLOCKSWITCHPRESSED = 4006;
    private static boolean isCCSwitchPressed = false;
    private static boolean isBrakePressed = false;
    private static boolean isSETPressed = false;
    private static boolean isRESPressed = false;
    public static boolean isCCC3On = false;

    public static final int OFF = 0;
    public static final int ACTIVE = 1;
    public static final int STANDBY = 2;

    public static int windowsPowerStatus = -1;
    public static int topWindowRunningStatus = -1;
    public static int topWindowPosition = -1;
    public static int topSunShadeStatus = -1;
    public static int topSunshadePosition = -1;
    public static int outsideRainStatus = -1;
    public static int topWindowStatus = -1;
    public static int left1carWindowPos = 0;
    public static int left2carWindowPos = 0;
    public static int right1carWindowPos = 0;
    public static int right2carWindowPos = 0;
    public static int lampLight = 0;
    public static int lampColor = 0;
    public static int lampSwitch = 0;
    public static int powerStatus = -1;
    public static float currentTemp = 0;
    public static int currentGear = 0;
    public static int currentFanSpeed = 1;
    public static int currentFanDirection = 1;
    public static int airStatus = 1;
    public static int acStatus = 1;
    public static int takePhotoStatus = -1;
    public static int takeVideoStatus = -1;
    public static int carGear = -1;
    public static int avmStatus = 0;
    public static int autoStatus = 0;
    public static int maxACStatus = 0;
    public static boolean carBrake = false;
    public static boolean carBrakeEMS = false;
    private int mChairMode = 0;
    public int mApaStatus = 0;
    public static int mApaOnStatus = 0;
    private int mReverseStatus;
    private int mFaceId = 1;
    public int engineStatus = -1;
    public int avmLineStatus = 0;
    public int avmDisplayForm = 0;
    private int frontDefrostStatus = VEHICLE.OFF;
    private int postDefrostStatus = VEHICLE.OFF;
    private int welcomeVisitorsSignal = 0;
    private int drivingModeGuideSingnal = 0;
    private int lockStatus = 0;
    private int unLockStatus = 0;
    private int warnSaveChairStatus = 0;
    private boolean isNightOn = false;
    private int driverLeft1carWindowPowerStatus = -1;
    private int driverLeft2carWindowPowerStatus = -1;
    private int driverRight1carWindowPowerStatus = -1;
    private int driverRight2carWindowPowerStatus = -1;
    private int left2carWindowPowerStatus = -1;
    private int right1carWindowPowerStatus = -1;
    private int right2carWindowPowerStatus = -1;
    private int left1carWindowStudyStatus = -1;
    private int left2carWindowStudyStatus = -1;
    private int right1carWindowStudyStatus = -1;
    private int right2carWindowStudyStatus = -1;
    private int driverBuckledStatus = -1;
    private int airCircleMode = -1;
    private int driverDoorStatus = VEHICLE.OFF;
    private int headLightsStatus = -1;
    private int autoHoldStatus = -1;
    private int autoHoldAvailableStatus = 0;
    private int carGearNew = -1;
    private int currentCarGearNew = -1;
    private int unallowStartSignal = -1;
    private int startUpButtonSignal = -1;
    private int RESSwitchStatus = -1;
    private int SETSwitchStatus = -1;
    private int cruiseControlStatus = 0;
    private int targetCruiseSpeed = 0;
    private int cruiseUnavailDisplay = 0;
    private int left2DoorStatus = VEHICLE.OFF;
    private int right1DoorStatus = VEHICLE.OFF;
    private int right2DoorStatus = VEHICLE.OFF;
    private int driverDoorLockStatus = DoorLockStatus.UNLOCKED;
    private int pressStatus = 0;
    private int mBCallStatus = BCallTYPE.STATE_IDLE;
    private int ccSwitchStatus = -1;
    private int rearDefrostButtonStatus = 0;
    private int acSwitchStatus = 0;
    private boolean doorLockSwitchStatus = true;
    private int doorLockSwitchStatusNum = 0;
    private boolean windowlockSwitchStatus = false;
    private int powerDistributionStatus = 0;
	private int trunkPosition = 0;
    private int trunkStatus = 0;
    private int trunkSwitch = VEHICLE.NO;
    private int shutfaceSwitch = 0;
    private int luggageUnlockSignal = 0;
    private int sdcardErrorStatus = 0;
    private boolean isDirectionPressed = false;

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BTNC21:
                    if(trunkStatus == VEHICLE.TRUNKDOOR_CLOSING){
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC21CONDITION,R.string.btnC21,"","",
                                R.string.skill_key,R.string.scene_trunk_outside_close,R.string.scene_trunk_outside_close,R.string.condition_btnC21);
                    }
                    break;
                case MSG_BTNC20:
                    if(trunkStatus == VEHICLE.TRUNKDOOR_OPENING){
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC20CONDITION,R.string.btnC20,"","",
                                R.string.skill_key,R.string.scene_trunk_outside_open,R.string.scene_trunk_outside_open,R.string.condition_btnC20);
                    }
                    break;
                case MSG_BTNC38_UNLOCK:
                    Log.d(TAG, "doorLockSwitchStatusNum: " + doorLockSwitchStatusNum);
                    if(doorLockSwitchStatusNum == 2){
                        int driverDoorStatusUNLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_1_LEFT);
                        int left2DoorStatusUNLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_2_LEFT);
                        int right1DoorStatusUNLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_1_RIGHT);
                        int right2DoorStatusUNLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_2_RIGHT);
                        Log.d(TAG, "driverDoorStatusUNLOCKED: " + driverDoorStatusUNLOCKED + ",left2DoorStatusUNLOCKED: " + left2DoorStatusUNLOCKED +
                                ",right1DoorStatusUNLOCKED: " + right1DoorStatusUNLOCKED + ",right2DoorStatusUNLOCKED: " + right2DoorStatusUNLOCKED);
                        if(driverDoorStatusUNLOCKED == VEHICLE.OFF && left2DoorStatusUNLOCKED == VEHICLE.OFF && right1DoorStatusUNLOCKED == VEHICLE.OFF && right2DoorStatusUNLOCKED == VEHICLE.OFF){
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC38CONDITION, R.string.btnC38,"＃ACTION＃","解锁",
                                    R.string.skill_key,R.string.scene_door_lock_switch,R.string.scene_door_lock_switch,R.string.condition_btnC38);
                        }
                    }
                    break;
                case MSG_BTNC38_LOCK:
                    Log.d(TAG, "doorLockSwitchStatusNum: " + doorLockSwitchStatusNum);
                    if(doorLockSwitchStatusNum == 1){
                        int driverDoorStatusLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_1_LEFT);
                        int left2DoorStatusLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_2_LEFT);
                        int right1DoorStatusLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_1_RIGHT);
                        int right2DoorStatusLOCKED = getDoorStatus(VehicleAreaDoor.DOOR_ROW_2_RIGHT);
                        Log.d(TAG, "driverDoorStatusLOCKED: " + driverDoorStatusLOCKED + ",left2DoorStatusLOCKED: " + left2DoorStatusLOCKED +
                                ",right1DoorStatusLOCKED: " + right1DoorStatusLOCKED + ",right2DoorStatusLOCKED: " + right2DoorStatusLOCKED);
                        if(driverDoorStatusLOCKED == VEHICLE.OFF && left2DoorStatusLOCKED == VEHICLE.OFF && right1DoorStatusLOCKED == VEHICLE.OFF && right2DoorStatusLOCKED == VEHICLE.OFF){
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC38CONDITION, R.string.btnC38,"＃ACTION＃","闭锁",
                                    R.string.skill_key,R.string.scene_door_lock_switch,R.string.scene_door_lock_switch,R.string.condition_btnC38);
                        }
                    }
                    break;
                case MSG_BTNC44:
                    if(autoStatus == VEHICLE.ON){
                        AppConstant.SPEAKTTSONCE_BTNC44 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_BROADCAST_AND_SHOW_GUIDE_ORDER, TtsConstant.GUIDEBTNC44CONDITION, R.string.btnC44, "", "",
                                R.string.skill_key,R.string.scene_auto_air_switch,R.string.scene_auto_air_switch,R.string.condition_btnC44);
                    }
                    break;
                case MSG_BTNC45:
                    if(autoStatus == VEHICLE.ON){
                        AppConstant.SPEAKTTSONCE_BTNC45 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST, TtsConstant.GUIDEBTNC45CONDITION, R.string.btnC45, "", "",
                                R.string.skill_key,R.string.scene_auto_air_switch,R.string.scene_auto_air_switch,R.string.condition_btnC45);
                    }
                    break;
                case MSG_BTNC46:
                    if(frontDefrostStatus == VEHICLE.ON){
                        AppConstant.SPEAKTTSONCE_BTNC46 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_BROADCAST_AND_SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC46CONDITION, R.string.btnC46,"","",
                                R.string.skill_key,R.string.scene_front_defrost,R.string.scene_front_defrost,R.string.condition_btnC46);
                    }
                    break;
                case MSG_BTNC47_ON:
                    if(frontDefrostStatus == VEHICLE.ON){
                        String defaultText = mContext.getString(R.string.btnC47);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC47CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃ACTION＃", "开启");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts, "开启");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                Utils.eventTrack(mContext, R.string.skill_key,R.string.scene_front_defrost,R.string.scene_front_defrost,TtsConstant.GUIDEBTNC47CONDITION,R.string.condition_btnC47, defaultTts);//埋点
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });
                    }
                    break;
                case MSG_BTNC47_OFF:
                    if(frontDefrostStatus == VEHICLE.OFF){
                        String defaultText = mContext.getString(R.string.btnC47);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC47CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃ACTION＃", "关闭");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts, "关闭");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                Utils.eventTrack(mContext, R.string.skill_key,R.string.scene_front_defrost,R.string.scene_front_defrost,TtsConstant.GUIDEBTNC47CONDITION,R.string.condition_btnC47, defaultTts);//埋点
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });
                    }
                    break;
                case MSG_BTNC48_1_ON:
                    if(postDefrostStatus == VEHICLE.ON){
                        AppConstant.SPEAKTTSONCE_BTNC48_1 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_BROADCAST_AND_SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC48_1CONDITION, R.string.btnC48_1,"＃ACTION＃","开启",
                                R.string.skill_key,R.string.scene_rear_defrost,R.string.scene_rear_defrost,R.string.condition_btnC48_1);
                    }
                    break;
                case MSG_BTNC48_1_OFF:
                    if(postDefrostStatus == VEHICLE.OFF){
                        AppConstant.SPEAKTTSONCE_BTNC48_1 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_BROADCAST_AND_SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC48_1CONDITION, R.string.btnC48_1,"＃ACTION＃","关闭",
                                R.string.skill_key,R.string.scene_rear_defrost,R.string.scene_rear_defrost,R.string.condition_btnC48_1);
                    }
                    break;
                case MSG_BTNC49_1_ON:
                    if(postDefrostStatus == VEHICLE.ON){
                        String defaultText = mContext.getString(R.string.btnC49_1);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC49_1CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃ACTION＃", "开启");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts, "开启");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                Utils.eventTrack(mContext, R.string.skill_key,R.string.scene_rear_defrost,R.string.scene_rear_defrost,TtsConstant.GUIDEBTNC49_1CONDITION,R.string.condition_btnC49_1, defaultTts);//埋点
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });
                    }
                    break;
                case MSG_BTNC49_1_OFF:
                    if(postDefrostStatus == VEHICLE.OFF){
                        String defaultText = mContext.getString(R.string.btnC49_1);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC49_1CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃ACTION＃", "关闭");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts, "关闭");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                Utils.eventTrack(mContext, R.string.skill_key,R.string.scene_rear_defrost,R.string.scene_rear_defrost,TtsConstant.GUIDEBTNC49_1CONDITION,R.string.condition_btnC49_1, defaultTts);//埋点
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });
                    }
                    break;
                case MSG_BTNC50:
                    if(airStatus == VEHICLE.OFF){
                        AppConstant.SPEAKTTSONCE_BTNC50 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC50CONDITION, R.string.btnC50,"","",
                                R.string.skill_key,R.string.scene_close_switch,R.string.scene_close_switch,R.string.condition_btnC50);
                    }
                    break;
                case MSG_BTNC51:
                    if(acStatus == VEHICLE.ON){
                        AppConstant.SPEAKTTSONCE_BTNC51 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_BROADCAST_AND_SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC51CONDITION, R.string.btnC51,"","",
                                R.string.skill_key,R.string.scene_air_ac_switch,R.string.scene_air_ac_switch,R.string.condition_btnC51);
                    }
                    break;
                case MSG_BTNC45_2:
                    if(acStatus == VEHICLE.ON){
                        AppConstant.SPEAKTTSONCE_BTNC45 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC45CONDITION, R.string.btnC45,"","",
                                R.string.skill_key,R.string.scene_air_ac_switch,R.string.scene_air_ac_switch,R.string.condition_btnC45_ac);
                    }
                    break;
                case MSG_BTNC52:
                    String replaceText = "吹面模式";
                    if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FACE){
                        replaceText = "吹面模式";
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                    }else if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FLOOR){
                        replaceText = "吹脚模式";
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                    }else if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FACE_FLOOR){
                        replaceText = "吹面吹脚模式";
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                    }else if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FLOOR_DEFROST){
                        replaceText = "吹脚除霜模式";
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                    }
                    break;
                case MSG_BTNC53_INNER:
                    if(airCircleMode == HVAC.LOOP_INNER){
//                        KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC53CONDITION, R.string.btnC53,"＃MODE＃","内循环",
//                                R.string.skill_key,R.string.scene_inner_outside,R.string.scene_inner_outside,R.string.condition_btnC53);
                        String defaultText = mContext.getString(R.string.btnC53);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC53CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃MODE＃", "内循环");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts,"内循环");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                Utils.eventTrack(mContext, R.string.skill_key, R.string.scene_inner_outside,R.string.scene_inner_outside, TtsConstant.GUIDEBTNC53CONDITION, R.string.condition_btnC53, defaultTts);//埋点
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });
                    }
                    break;
                case MSG_BTNC53_OUTSIDE:
                    if(airCircleMode == HVAC.LOOP_OUTSIDE){
//                        KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC53CONDITION, R.string.btnC53,"＃MODE＃","外循环",
//                                R.string.skill_key,R.string.scene_inner_outside,R.string.scene_inner_outside,R.string.condition_btnC53);
                        String defaultText = mContext.getString(R.string.btnC53);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC53CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃MODE＃", "外循环");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts,"外循环");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                Utils.eventTrack(mContext, R.string.skill_key, R.string.scene_inner_outside,R.string.scene_inner_outside, TtsConstant.GUIDEBTNC53CONDITION, R.string.condition_btnC53, defaultTts);//埋点
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });

                    }
                    break;
                case MSG_BTNC54_ON:
                    if(maxACStatus == HVAC.HVAC_ON){
                        AppConstant.SPEAKTTSONCE_BTNC54 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC54CONDITION, R.string.btnC54,"＃ACTION＃","开启",
                                R.string.skill_key,R.string.scene_max_ac_switch,R.string.scene_max_ac_switch,R.string.condition_btnC54);
                    }
                    break;
                case MSG_BTNC54_OFF:
                    if(maxACStatus == HVAC.HVAC_OFF){
                        AppConstant.SPEAKTTSONCE_BTNC54 = true;
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC54CONDITION, R.string.btnC54,"＃ACTION＃","关闭",
                                R.string.skill_key,R.string.scene_max_ac_switch,R.string.scene_max_ac_switch,R.string.condition_btnC54);
                    }
                    break;
                case MSG_CCC1:
                    if(isCCSwitchPressed){
                        if(getSpeed() < 40){
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC1_1CONDITION);
                        }else {
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC1CONDITION);
                        }
                    }
                    break;
                case MSG_CCC4:
                    if(cruiseControlStatus == STANDBY){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC4CONDITION);
                    }
                    break;
                case MSG_STANDBY_SET_CC5:
                    if(cruiseUnavailDisplay == VEHICLE.VEHICLE_ON_ACK){
                        if(getSpeed() < 40){
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC5CONDITION);
                        }else if(carGear < GEAR_THIRD){//自动挡（没有自动挡的车型）
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC5_2CONDITION);
                        }
                    }
                    break;
                case MSG_STANDBY_SET:
                    if(isSETPressed){
                        isCCC3On = true;
                        if(AppConstant.isFirstUseCruise){
                            AppConstant.isFirstUseCruise = false;
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC3_1CONDITION);
                        }else {
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC3CONDITION);
                        }
                    }else if(isRESPressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC9CONDITION);
                    }
                    break;
                case MSG_STANDBY_SET_CCC19:
                    if(cruiseUnavailDisplay == VEHICLE.VEHICLE_ON_ACK){
                        if(getSpeed() < 40){
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC19CONDITION);
                        }else if(carGear < GEAR_THIRD){//自动挡（没有手动挡的车型）
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC21CONDITION);
                        }
                    }
                    break;
                case MSG_CCC18_CCC24:
                    if(isSETPressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC18CONDITION);
                    }else if(isRESPressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC24CONDITION);
                    }
                    break;
                case MSG_STANDBY_RES:
                    if(cruiseUnavailDisplay == VEHICLE.VEHICLE_ON_ACK){
                        if(getSpeed() < 40){
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC5CONDITION);
                        }else if(carGear < GEAR_THIRD){//自动挡（没有自动挡的车型）
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC5_2CONDITION);
                        }
                    }else if(isRESPressed && cruiseControlStatus == STANDBY){
                        if(getSpeed() < 40){
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC4_1CONDITION);
                        }else {
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC4CONDITION);
                        }
                    }
                    break;
                case MSG_CCC9:
                    if(isRESPressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC9CONDITION);
                    }
                    break;
                case MSG_STANDBY_RES_BROADCAST:
                    if(isRESPressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC24CONDITION);
                    }
                    break;
                case MSG_CCC6:
                    int targetCruiseSpeedFront = (int) msg.obj;
                    Log.d(TAG,"after 3s targetCruiseSpeed = " + targetCruiseSpeed);
                    if(cruiseControlStatus == ACTIVE && targetCruiseSpeed == targetCruiseSpeedFront){
                        if(AppConstant.isFirstAdjustCruiseSpeed){
                            AppConstant.isFirstAdjustCruiseSpeed = false;
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC6_1CONDITION);
                        }else if(targetCruiseSpeed < MAXSPEED100){
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC6CONDITION);
                        }else if(targetCruiseSpeed >= MAXSPEED100){
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC16CONDITION);
                        }
                    }
                    break;
                case MSG_CCC7:
                    if(isBrakePressed){
                        if(AppConstant.isFirstPressBrake){
                            AppConstant.isFirstPressBrake = false;
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC7CONDITION);
                        }else {
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC7_1CONDITION);
                        }
                    }
                    break;
                case MSG_CCC12:
                    if(ccSwitchStatus == VEHICLE.NO){//正常退出
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC12CONDITION);
                    }else if(ccSwitchStatus == VEHICLE.YES && powerStatus == CarSensorEvent.IGNITION_STATE_ON){//异常退出
                        //CruiseController.getInstance(mContext).srAction(TtsConstant.CCC13CONDITION);
                    }
                    break;
                case MSG_CCC16:
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC16CONDITION);
                    break;
                case MSG_CCC17:
                    if(isCCSwitchPressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC17CONDITION);
                    }
                    break;
                case MSG_CCC22:
                    int targetCruiseSpeedFront22 = (int) msg.obj;
                    Log.d(TAG,"after 3s targetCruiseSpeed = " + targetCruiseSpeed);
                    if(cruiseControlStatus == ACTIVE && targetCruiseSpeed == targetCruiseSpeedFront22) {
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC22CONDITION);
                    }
                    break;
                case MSG_CCC23:
                    if(isBrakePressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC23CONDITION);
                    }
                    break;
                case MSG_CCC25:
                    if(isCCSwitchPressed){
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC25CONDITION);
                    }else {
                        //CruiseController.getInstance(mContext).srAction(TtsConstant.CCC26CONDITION);
                    }
                    break;
                case COUNTPOWERTIME:
                    if(powerDistributionStatus == CarSensorEvent.IGNITION_STATE_ON){
                        isPowerOn = true;
                    }
                    break;
                case COUNTPOWERTIMEFORAUTOWIPER:
                    isPowerOnForAutoWiper = true;
                    break;
                case CCSWITCHPRESSED:
                    isCCSwitchPressed = false;
                    break;
                case BRAKEPRESSED:
                    isBrakePressed = false;
                    break;
                case SETPRESSED:
                    isSETPressed = false;
                    break;
                case RESPRESSED:
                    isRESPressed = false;
                    break;
                case DIRECTIONPRESSED:
                    isDirectionPressed = false;
                    break;
                case DOORLOCKSWITCHPRESSED:
                    doorLockSwitchStatusNum = 0;
                    break;
            }
        }
    };

    public static CarUtils getInstance(Context c){
        if(mCarUtils==null)
            mCarUtils = new CarUtils(c);
        return mCarUtils;
    }

    private CarUtils(Context c){
        try {
            mContext = c;
            mCarCabinManager = (CarCabinManager) AppConfig.INSTANCE.mCarApi.getCarManager(Car.CABIN_SERVICE);
            mCarCabinManager.registerCallback(carCabinEventCallback);

            mCarMcuManager = (CarMcuManager) AppConfig.INSTANCE.mCarApi.getCarManager(Car.CAR_MCU_SERVICE);
            mCarMcuManager.registerCallback(mcuEventCallback,new int[]{CarMcuManager.ID_REVERSE_SIGNAL,CarMcuManager.ID_MCU_LOST_CANID,
                    CarMcuManager.ID_NIGHT_MODE,CarMcuManager.ID_VENDOR_ECALL_STATE
                    ,CarMcuManager.ID_TSP_REPORT_MSG,CarMcuManager.ID_EMS_PARKING_BRAKE
                    ,CarMcuManager.ID_VENDOR_BCALL_STATE,CarMcuManager.ID_SEND_TO_CAR});

            mCarSensorManager = (CarSensorManager)AppConfig.INSTANCE.mCarApi.getCarManager(Car.SENSOR_SERVICE);
            mCarSensorManager.registerListener(carSensorEventCallback,CarSensorManager.SENSOR_TYPE_POWER_STATE , 100);
            mCarSensorManager.registerListener(carSensorEventCallback,CarSensorManager.SENSOR_TYPE_IGNITION_STATE , 100);
            mCarSensorManager.registerListener(carSensorEventCallback,CarSensorManager.SENSOR_TYPE_GEAR, SENSOR_RATE_NORMAL);
            mCarSensorManager.registerListener(carSensorEventCallback,CarSensorManager.SENSOR_TYPE_PARKING_BRAKE, SENSOR_RATE_NORMAL);
            mCarSensorManager.registerListener(carSensorEventCallback,CarSensorManager.SENSOR_TYPE_GEAR_NEW, SENSOR_RATE_NORMAL);
            //mCarSensorManager.registerListener(carSensorEventCallback,CarSensorManager.SENSOR_TYPE_EMS_PARKING_BRAKE, SENSOR_RATE_NORMAL);


            mCarHvacManager = (CarHvacManager) AppConfig.INSTANCE.mCarApi.getCarManager(Car.HVAC_SERVICE);
            if (mCarHvacManager!=null){
                mCarHvacManager.registerCallback(carHvacEventCallback);
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    public CarCabinManager.CarCabinEventCallback carCabinEventCallback = new CarCabinManager.CarCabinEventCallback() {
        @Override
        public void onChangeEvent(CarPropertyValue carPropertyValue) {
            int propertyId = (int)carPropertyValue.getPropertyId();
            int areaId = (int) carPropertyValue.getAreaId();
//            Log.d(TAG, "onChangeEvent() called with: propertyId = [" + propertyId + "]");
            if (propertyId == CarCabinManager.ID_DMS_CURRENT_POS){
                mChairMode = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 座椅运动模式(default:0) --------------mChairMode: "+mChairMode);
            }else if (propertyId == CarCabinManager.ID_ADAS_APA_ACTIVE_ON){
                mApaOnStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn APA开关状态(default:-1) --------------mApaOnStatus: " + mApaOnStatus);
            }else if (propertyId == CarCabinManager.ID_ADAS_APA_PARK_NOTICE_INFO){
                mApaStatus = (int) carPropertyValue.getValue();
                Log.d(TAG,"mApaStatus = " + mApaStatus);
                if(mApaStatus>0 ){
                    ApaController.getInstance(mContext).startSpeakApa();
                }else if(mApaStatus== 0){  //关闭
                    ApaController.getInstance(mContext).stopSpeakApa();
                }
                Log.d(TAG, "onChangeEvent() called with: mApaStatus = [" + mApaStatus + "]");
            }else if (propertyId == ID_BODY_WINDOW_SUNROOF_CONTROL_STATE){//天窗运动状态
                topWindowRunningStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 天窗运动状态(default:-1) --------------topWindowRunningStatus: "+topWindowRunningStatus);
            }else if (propertyId == ID_BODY_WINDOW_SUNSHADE_STATE){//遮阳帘运动状态
                topSunShadeStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 遮阳帘运动状态(default:-1) --------------topSunShadeStatus: "+topSunShadeStatus);
            }else if(propertyId == ID_BODY_WINDOW_SUNROOF_CONTROL_POS){//天窗的位置
                topWindowPosition = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 天窗的位置(default:-1) --------------topWindowPosition: "+topWindowPosition);
            }else if(propertyId == ID_BODY_WINDOW_SUNSHADE_POS){//遮阳帘的位置
                topSunshadePosition = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 遮阳帘的位置(default:-1) --------------topSunshadePosition: "+topSunshadePosition);
            }else if(propertyId == ID_BODY_WINDOW_SUNROOF_POS){//天窗状态
                topWindowStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 天窗状态(default:-1) --------------topWindowStatus: "+topWindowStatus);
            }else if(propertyId == ID_WINDOW_POS){//车窗位置
                if(areaId == VehicleAreaWindow.WINDOW_ROW_1_LEFT) {//左前车窗位置
                    left1carWindowPos = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 左前车窗位置(default:0) ------left1carWindowPos: "+left1carWindowPos);
                }else if(areaId == VehicleAreaWindow.WINDOW_ROW_1_RIGHT) {//右前车窗位置
                    right1carWindowPos = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 右前车窗位置(default:0) ------right1carWindowPos: "+right1carWindowPos);
                }else if(areaId == VehicleAreaWindow.WINDOW_ROW_2_LEFT) {//左后车窗位置
                    left2carWindowPos = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 左后车窗位置(default:0) ------left2carWindowPos: "+left2carWindowPos);
                }else if(areaId == VehicleAreaWindow.WINDOW_ROW_2_RIGHT) {//右后车窗位置
                    right2carWindowPos = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 右后车窗位置(default:0) ------right2carWindowPos: "+right2carWindowPos);
                }
            }else if(propertyId == ID_BODY_LIGHT_ATMO_COLOR_STATE){//氛围灯颜色
                lampColor  = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 氛围灯颜色(default:0) --------------lampColor: "+lampColor);
                SharedPreferencesUtils.saveInt(mContext, AppConstant.KEY_LAMP_COLOR, lampColor);//默认红色
            }else if(propertyId == ID_BODY_LIGHT_ATMO_BRIGHT_LEVEL){//氛围灯亮度
                lampLight = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 氛围灯亮度(default:0) --------------lampLight: "+lampLight);
                SharedPreferencesUtils.saveInt(mContext, AppConstant.KEY_LAMP_LIGHT, lampLight);//默认5
            } else if(propertyId == ID_BODY_LIGHT_ATMO_ON){//氛围灯开关
                lampSwitch = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 氛围灯开关(default:0) --------------lampSwitch: "+lampSwitch);
                SharedPreferencesUtils.saveInt(mContext, AppConstant.KEY_LAMP_SWITCH, lampSwitch);//默认关
            } else if(propertyId== CarCabinManager.ID_AVM_DISPLAY_SWITCH){
                avmStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 全景状态(default:0) --------------avmStatus: "+avmStatus);
                if (avmStatus == AVM.AVM_ON) {
//                  Utils.exitVoiceAssistant();
                    AVMController.getInstance(mContext).startSpeakAVM();
                    current = TspSceneAdapter.getTspScene(mContext);
                    MVWAgent.getInstance().stopMVWSession();
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_DVR);
                } else if (avmStatus == AVM.AVM_OFF) { //退出视频全景,不处理视频
                    AVMController.getInstance(mContext).stopSpeakAVM();
                    restartScrene();
                }
            }else if (propertyId == ID_DVR_SNAP_SHOOT){//照相的结果
                int value = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:照相的结果 isTakePhotoSuccess = " + value);
                //if(value != 0) takePhotoStatus = value;
                if(AppConstant.isCallDVR){
                    AppConstant.isCallDVR = false;
                    CMDController cmdController = new CMDController(mContext);
                    if(value == DVR.DVR_SNAP_SHOOT_SUCCESS){
                        cmdController.checkTakePhotoStatus(0,true);
                    }else if(value == DVR.DVR_SNAP_SHOOT_FAILED){
                        cmdController.checkTakePhotoStatus(0,false);
                    }
                }
            } else if (propertyId == CarCabinManager.ID_DVR_EMERGENCY_RECORD){//录制的结果ID_DVR_EMERGENCY_RECORD
                int value = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:录制的结果 isTakeVideoSuccess = " + value);
                //if(value != 0) takeVideoStatus = value;
                if(AppConstant.isCallDVR){
                    AppConstant.isCallDVR = false;
                    CMDController cmdController = new CMDController(mContext);
                    if(value == DVR.DVR_EMERGENCY_RECORD_SAVING || value == DVR.DVR_EMERGENCY_RECORD_SUCCESS){
                        cmdController.checkTakeVideoStatus(0,true);
                    }else if(value == DVR.DVR_SNAP_SHOOT_FAILED){
                        cmdController.checkTakeVideoStatus(0,false);
                    }
                }
            }else if (propertyId == CarCabinManager.ID_FACE_REC_ID){ //人脸识别Id
                if (areaId == VehicleAreaSeat.SEAT_ROW_1_LEFT) {
                    mFaceId = (int) carPropertyValue.getValue();
//                    updateSaveMode();
                    Log.i(TAG, "=onChangeEvent=get=mFaceId==" + mFaceId + "==areaId==" + areaId);
                }
            }
//            else if(propertyId == ID_DRIVING_MODE_GUIDE_ACK){//上下坡，超车信号
//                drivingModeGuideSingnal = (int) carPropertyValue.getValue();
//                Log.d(TAG, "lh:body turn 驾驶模式引导(default:0) --------------drivingModeGuideSingnal: " + drivingModeGuideSingnal);
//                DrivingModeGuideController.getInstance(mContext).dispatchCommand(drivingModeGuideSingnal);
//            }
            else if(propertyId == ID_BODY_BODY_LIGHT_SMART_WELCOMELIGHT_ON){//迎宾信号
                welcomeVisitorsSignal = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 迎宾功能反馈信号(default:0) --------------welcomeVisitorsSignal: " + welcomeVisitorsSignal);
//                if(welcomeVisitorsSignal == 1 || welcomeVisitorsSignal == 4 || welcomeVisitorsSignal == 5)
//                    ActiveServiceModel.getInstance().handleWelcomeVisitors(mContext,welcomeVisitorsSignal,getLockStatus(),getunLockStatus());
            }else if(propertyId == ID_KEY_SMART_LOCK){//迎宾信号  闭锁
                lockStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 迎宾信号  闭锁(default:0) --------------lockStatus: " + lockStatus);
            }else if(propertyId == ID_KEY_SMART_UNLOCK){//迎宾信号  解锁
                unLockStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 迎宾信号  解锁(default:0) --------------unLockStatus: " + unLockStatus);
            }else if(propertyId == ID_RESTMODE_SAVE_REQ){//提醒座椅休息模式保存
                warnSaveChairStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 提醒座椅休息模式保存--------------warnSaveChairStatus: " + warnSaveChairStatus);
                if(warnSaveChairStatus == VEHICLE.REQUEST)
                    ChairController.getInstance(mContext).handleWarnSaveSleepMode();
            }else if(propertyId == ID_WINDOWAPA_INITED){//车窗防夹学习状态
                if(areaId == WINDOW_ROW_1_LEFT){//主驾
                    left1carWindowStudyStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 主驾防夹学习状态(default:-1) --------------left1carWindowStudyStatus: " + left1carWindowStudyStatus);
                }else if(areaId == WINDOW_ROW_1_RIGHT){//副驾
                    right1carWindowStudyStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 副驾防夹学习状态(default:-1) --------------right1carWindowStudyStatus: " + right1carWindowStudyStatus);
                }else if(areaId == WINDOW_ROW_2_LEFT){//左后
                    left2carWindowStudyStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 左后防夹学习状态(default:-1) --------------left2carWindowStudyStatus: " + left2carWindowStudyStatus);
                }else if(areaId == WINDOW_ROW_2_RIGHT){//右后
                    right2carWindowStudyStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 右后防夹学习状态(default:-1) --------------right2carWindowStudyStatus: " + right2carWindowStudyStatus);
                }
            }else if(propertyId == ID_BODY_WINDOW_UP_DOWN_STATUS){//主驾侧的车窗开关状态
                if(areaId == WINDOW_ROW_1_LEFT){//主驾
                    driverLeft1carWindowPowerStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 主驾侧主驾开关状态(default:-1) --------------driverLeft1carWindowPowerStatus: " + driverLeft1carWindowPowerStatus);
                    if((driverLeft1carWindowPowerStatus == VEHICLE.WINDOW_UPING || driverLeft1carWindowPowerStatus == VEHICLE.WINDOW_DOWNING ||
                            driverLeft1carWindowPowerStatus == VEHICLE.WINDOW_OPEN) && getLeft1carWindowStudyStatus() == VEHICLE.UNLEARNED){
                        //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC41CONDITION,CarController.WINDOW_LEFT_FRONT2.replace("车窗",""));
                    }
                }else if(areaId == WINDOW_ROW_1_RIGHT){//副驾
                    driverRight1carWindowPowerStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 主驾侧副驾开关状态(default:-1) --------------driverRight1carWindowPowerStatus: " + driverRight1carWindowPowerStatus);
                    if((driverRight1carWindowPowerStatus == VEHICLE.WINDOW_UPING || driverRight1carWindowPowerStatus == VEHICLE.WINDOW_DOWNING ||
                            driverRight1carWindowPowerStatus == VEHICLE.WINDOW_OPEN) && getRight1carWindowStudyStatus() == VEHICLE.UNLEARNED){
                        //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC41CONDITION,CarController.WINDOW_RIGHT_FRONT2.replace("车窗",""));
                    }
                }else if(areaId == WINDOW_ROW_2_LEFT){//左后
                    driverLeft2carWindowPowerStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 主驾侧左后开关状态(default:-1) --------------driverLeft2carWindowPowerStatus: " + driverLeft2carWindowPowerStatus);
                    if((driverLeft2carWindowPowerStatus == VEHICLE.WINDOW_UPING || driverLeft2carWindowPowerStatus == VEHICLE.WINDOW_DOWNING ||
                            driverLeft2carWindowPowerStatus == VEHICLE.WINDOW_OPEN) && getLeft2carWindowStudyStatus() == VEHICLE.UNLEARNED){
                       //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC41CONDITION,CarController.WINDOW_LEFT_BACK.replace("车窗",""));
                    }
                }else if(areaId == WINDOW_ROW_2_RIGHT){//右后
                    driverRight2carWindowPowerStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 主驾侧右后开关状态(default:-1) --------------driverRight2carWindowPowerStatus: " + driverRight2carWindowPowerStatus);
                    if((driverRight2carWindowPowerStatus == VEHICLE.WINDOW_UPING || driverRight2carWindowPowerStatus == VEHICLE.WINDOW_DOWNING ||
                            driverRight2carWindowPowerStatus == VEHICLE.WINDOW_OPEN) && getRight2carWindowStudyStatus() == VEHICLE.UNLEARNED){
                        //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC41CONDITION,CarController.WINDOW_RIGHT_BACK.replace("车窗",""));
                    }
                }
            }else if(propertyId == ID_BODY_WINDOW_UP_DOWN_STATUS_NEW){//除主驾侧，每个车窗开关的状态
                if(areaId == WINDOW_ROW_1_RIGHT){//副驾
                    right1carWindowPowerStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 副驾开关状态(default:-1) --------------right1carWindowPowerStatus: " + right1carWindowPowerStatus);
                    if((right1carWindowPowerStatus == VEHICLE.WINDOW_UPING || right1carWindowPowerStatus == VEHICLE.WINDOW_DOWNING ||
                            right1carWindowPowerStatus == VEHICLE.WINDOW_OPEN) && getRight1carWindowStudyStatus() == VEHICLE.UNLEARNED){
                        //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC42CONDITION,CarController.WINDOW_RIGHT_FRONT2.replace("车窗",""));
                    }
                }else if(areaId == WINDOW_ROW_2_LEFT){//左后
                    left2carWindowPowerStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 左后开关状态(default:-1) --------------left2carWindowPowerStatus: " + left2carWindowPowerStatus);
                    if((left2carWindowPowerStatus == VEHICLE.WINDOW_UPING || left2carWindowPowerStatus == VEHICLE.WINDOW_DOWNING ||
                            left2carWindowPowerStatus == VEHICLE.WINDOW_OPEN) && getLeft2carWindowStudyStatus() == VEHICLE.UNLEARNED){
                        //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC42CONDITION,CarController.WINDOW_LEFT_BACK.replace("车窗",""));
                    }
                }else if(areaId == WINDOW_ROW_2_RIGHT){//右后
                    right2carWindowPowerStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 右后开关状态(default:-1) --------------right2carWindowPowerStatus: " + right2carWindowPowerStatus);
                    if((right2carWindowPowerStatus == VEHICLE.WINDOW_UPING || right2carWindowPowerStatus == VEHICLE.WINDOW_DOWNING ||
                            right2carWindowPowerStatus == VEHICLE.WINDOW_OPEN) && getRight2carWindowStudyStatus() == VEHICLE.UNLEARNED){
                        //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC42CONDITION,CarController.WINDOW_RIGHT_BACK.replace("车窗",""));
                    }
                }
            }else if(propertyId == ID_SAFE_BELT_STATUS){//主驾安全带扣状态
                Integer[] value = (Integer[]) carPropertyValue.getValue();
                driverBuckledStatus = value[0];
                Log.d(TAG, "lh:body turn 主驾安全带扣状态(default:-1) --------------driverBuckledStatus: " + driverBuckledStatus);
                //if(driverBuckledStatus == 0)KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC78CONDITION, R.string.btnC78,"","");
            }else if(propertyId == ID_DOOR_POS){//车门是否关好
                if(areaId == VehicleAreaDoor.DOOR_ROW_1_LEFT){
                    int driverDoorStatusCurrent = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 主驾车门状态(default:-1) --------------driverDoorStatusCurrent: " + driverDoorStatusCurrent);
                    if(driverDoorStatusCurrent == VEHICLE.OFF){//主驾车门关上
                       RemoteManager.getInstance(mContext).notifyDoorStatus(0);
                    }
                    //4个车门有从close变为open的
                    if(driverDoorStatusCurrent == VEHICLE.ON && driverDoorStatus == VEHICLE.OFF && right1DoorStatus == VEHICLE.OFF && left2DoorStatus == VEHICLE.OFF && right2DoorStatus == VEHICLE.OFF){
                        if(powerStatus == CarSensorEvent.IGNITION_STATE_OFF && carGearNew == CarSensorEvent.GEAR_PARK){
                            //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC76CONDITION, R.string.btnC76,"","");
                        }
                    }
                    driverDoorStatus = driverDoorStatusCurrent;
                }else if(areaId == VehicleAreaDoor.DOOR_ROW_1_RIGHT){
                    int right1DoorStatusCurrent = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 副驾车门状态(default:-1) --------------right1DoorStatusCurrent: " + right1DoorStatusCurrent);
                    //4个车门有从close变为open的
                    if(right1DoorStatusCurrent == VEHICLE.ON && driverDoorStatus == VEHICLE.OFF && right1DoorStatus == VEHICLE.OFF && left2DoorStatus == VEHICLE.OFF && right2DoorStatus == VEHICLE.OFF){
                        if(powerStatus == CarSensorEvent.IGNITION_STATE_OFF && carGearNew == CarSensorEvent.GEAR_PARK){
                            //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC76CONDITION, R.string.btnC76,"","");
                        }
                    }
                    right1DoorStatus = right1DoorStatusCurrent;
                }else if(areaId == VehicleAreaDoor.DOOR_ROW_2_LEFT){
                    int left2DoorStatusCurrent = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 左后车门状态(default:-1) --------------left2DoorStatusCurrent: " + left2DoorStatusCurrent);
                    //4个车门有从close变为open的
                    if(left2DoorStatusCurrent == VEHICLE.ON && driverDoorStatus == VEHICLE.OFF && right1DoorStatus == VEHICLE.OFF && left2DoorStatus == VEHICLE.OFF && right2DoorStatus == VEHICLE.OFF){
                        if(powerStatus == CarSensorEvent.IGNITION_STATE_OFF && carGearNew == CarSensorEvent.GEAR_PARK){
                            //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC76CONDITION, R.string.btnC76,"","");
                        }
                    }
                    left2DoorStatus = left2DoorStatusCurrent;
                }else if(areaId == VehicleAreaDoor.DOOR_ROW_2_RIGHT){
                    int right2DoorStatusCurrent = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 右后车门状态(default:-1) --------------right2DoorStatusCurrent: " + right2DoorStatusCurrent);
                    //4个车门有从close变为open的
                    if(right2DoorStatusCurrent == VEHICLE.ON && driverDoorStatus == VEHICLE.OFF && right1DoorStatus == VEHICLE.OFF && left2DoorStatus == VEHICLE.OFF && right2DoorStatus == VEHICLE.OFF){
                        if(powerStatus == CarSensorEvent.IGNITION_STATE_OFF && carGearNew == CarSensorEvent.GEAR_PARK){
                            //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC76CONDITION, R.string.btnC76,"","");
                        }
                    }
                    right2DoorStatus = right2DoorStatusCurrent;
                }
            }else if(propertyId == ID_HEADLIGHTS_STATE){//近光灯状态
                headLightsStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 近光灯状态(default:-1) --------------headLightsStatus: " + headLightsStatus);
            }else if (propertyId == ID_BODY_WINDOW_WIPER) {//雨刮
                int value = (int) carPropertyValue.getValue();
                if (areaId == WINDOW_FRONT_WINDSHIELD) { //前雨刮BCM_FrontWiperSwitchStatus 自动雨刮档5,高速挡2，间歇档3，低速档1，Once4
                    Log.d(TAG, "lh:body turn 前雨刮状态(default:-1) --------------value: " + value);
                    if (value == VEHICLE.WIPER_OFF) {//前雨刮OFF

                    } else if (powerStatus == CarSensorEvent.IGNITION_STATE_ON && value == VEHICLE.WIPER_MIST) {//间隙刮刷
                        //KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC65CONDITION, R.string.btnC65,"","");
                    }else if (isPowerOnForAutoWiper && powerStatus == CarSensorEvent.IGNITION_STATE_ON && value == VEHICLE.WIPER_AUTO) {//自动挡
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC66CONDITION, R.string.btnC66,"","",
                                R.string.skill_key,R.string.scene_auto_wiper,R.string.scene_auto_wiper,R.string.condition_btnC66);
                    }
                } else if (areaId == WINDOW_REAR_WINDSHIELD) {//后雨刮BCM_RearWiperSwitchStatus 开启2，关闭1
                    Log.d(TAG, "lh:body turn 后雨刮状态(default:-1) --------------value: " + value);
                    if (isPowerOn && powerStatus == CarSensorEvent.IGNITION_STATE_ON && value == VEHICLE.OFF) {//后雨刮OFF
//                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC69CONDITION, R.string.btnC69,"＃ACTION＃","关闭",
//                                R.string.skill_key,R.string.scene_rear_wiper_switch,R.string.scene_rear_wiper_switch,R.string.condition_btnC69);
                    } else if (isPowerOn && powerStatus == CarSensorEvent.IGNITION_STATE_ON && value == VEHICLE.ON) {//后雨刮ON
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC69CONDITION, R.string.btnC69,"＃ACTION＃","开启",
                                R.string.skill_key,R.string.scene_rear_wiper_switch,R.string.scene_rear_wiper_switch,R.string.condition_btnC69);
                    }
                }
            }else if (propertyId == ID_FOG_LIGHTS_STATE) {//后雾灯BCM_RearFoglampSwitchStatus request 0 ,norequest无上报
                if(areaId == WINDOW_REAR_WINDSHIELD){
                    int tmpIntStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 后雾灯状态(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
                    if (powerStatus == CarSensorEvent.IGNITION_STATE_ON && getHeadLightsStatus() == VehicleLightState.OFF && tmpIntStatus == 0) {//近光未开启时
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_COMMAND,TtsConstant.GUIDEBTNC62CONDITION, R.string.btnC62,"","",
                                R.string.skill_key,R.string.scene_fog_light,R.string.scene_fog_light,R.string.condition_btnC62);
                    }
                }
            }else if (propertyId == ID_BODY_AUTO_HEAD_LIGHT) {//自动大灯BCM_AutoHeadlightSwitchStatus
                int tmpIntStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 自动大灯状态(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
                if (powerStatus == CarSensorEvent.IGNITION_STATE_ON && tmpIntStatus == VEHICLE.ON) {
                    KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC61CONDITION, R.string.btnC61,"＃ACTION＃","开启",
                            R.string.skill_key,R.string.scene_auto_head_light_switch,R.string.scene_auto_head_light_switch,R.string.condition_btnC61);
                }else if (powerStatus == CarSensorEvent.IGNITION_STATE_ON && tmpIntStatus == VEHICLE.OFF){
//                    KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC61CONDITION, R.string.btnC61,"＃ACTION＃","关闭",
//                            R.string.skill_key,R.string.scene_auto_head_light_switch,R.string.scene_auto_head_light_switch,R.string.condition_btnC61);
                }
            }else if (propertyId == CarCabinManager.ID_BODY_WINDOW_SUNROOF_STATUS) {//天窗按键
                int value_sunroof = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 天窗按键状态(default:-1) --------------value_sunroof: " + value_sunroof);
                if(getSpeed() > MAXSPEED60){
                    if (value_sunroof == VEHICLE.ON) {//天窗开启按钮
                        Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC57 = " + AppConstant.SPEAKTTSONCE_BTNC57);
                        if(!AppConstant.SPEAKTTSONCE_BTNC57) {//一个点火周期播报一次
                            AppConstant.SPEAKTTSONCE_BTNC57 = true;
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC57CONDITION, R.string.btnC57,"","",
                                    R.string.skill_key,R.string.scene_sunroof_switch,R.string.scene_sunroof_switch,R.string.condition_btnC57);
                        }
                    } else if (value_sunroof == VEHICLE.OFF) {//天窗关闭按钮
                        Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC58 = " + AppConstant.SPEAKTTSONCE_BTNC58);
                        if(!AppConstant.SPEAKTTSONCE_BTNC58) {//一个点火周期播报一次
                            AppConstant.SPEAKTTSONCE_BTNC58 = true;
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC58CONDITION, R.string.btnC58,"","",
                                    R.string.skill_key,R.string.scene_sunroof_switch,R.string.scene_sunroof_switch,R.string.condition_btnC58);
                        }
                    }
                }
            }else if(propertyId == CarCabinManager.ID_BODY_WINDOW_SUNVISOR_STATUS){//遮阳帘按键
                int value_sunvisor = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 遮阳帘按键状态(default:-1) --------------value_sunvisor: " + value_sunvisor);
                if(getSpeed() > MAXSPEED60){
                    if (value_sunvisor == VEHICLE.ON) { //遮阳帘开启按钮
                        Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC59 = " + AppConstant.SPEAKTTSONCE_BTNC59);
                        if(!AppConstant.SPEAKTTSONCE_BTNC59) {//一个点火周期播报一次
                            AppConstant.SPEAKTTSONCE_BTNC59 = true;
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC59CONDITION, R.string.btnC59,"","",
                                    R.string.skill_key,R.string.scene_sunshade_switch,R.string.scene_sunshade_switch,R.string.condition_btnC59);
                        }
                    } else if (value_sunvisor == VEHICLE.OFF) { //遮阳帘关闭按钮
                        Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC60 = " + AppConstant.SPEAKTTSONCE_BTNC60);
                        if(!AppConstant.SPEAKTTSONCE_BTNC60) {//一个点火周期播报一次
                            AppConstant.SPEAKTTSONCE_BTNC60 = true;
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_ORDER,TtsConstant.GUIDEBTNC60CONDITION, R.string.btnC60,"","",
                                    R.string.skill_key,R.string.scene_sunshade_switch,R.string.scene_sunshade_switch,R.string.condition_btnC60);
                        }
                    }
                }
            }else if(propertyId == ID_KEY_QUERY_INFO) {
                Integer[] value = (Integer[]) carPropertyValue.getValue();
                if((null == value) || (null != value && value.length < 2)){
                    return;
                }
                Log.d(TAG,"value[0] = " + value[0] + ",value[1] = " + value[1]);
                if ((value[0] == VEHICLE.KEY_HVAC_FAN_SPEED_DOWN_1 || value[0] == VEHICLE.KEY_HVAC_FAN_SPEED_UP_1)) { //风量调节
                    if(value[1] == VEHICLE.YES){//按下
                        Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC55 = " + AppConstant.SPEAKTTSONCE_BTNC55);
                        if(getSpeed() > MAXSPEED100){
                            if(!AppConstant.SPEAKTTSONCE_BTNC55) {//一个点火周期播报一次
                                AppConstant.SPEAKTTSONCE_BTNC55 = true;
                                KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC55CONDITION, R.string.btnC55,"","",
                                        R.string.skill_key,R.string.scene_wind_switch,R.string.scene_wind_switch,R.string.condition_btnC55);
                            }
                        }
                    }
                }else if (value[0] == VEHICLE.KEY_HVAC_MAX_AC_SWITCH) {//最大制冷
                    int tmpIntStatus = value[1];
                    Log.d(TAG, "lh:body turn 最大制冷按键状态(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
                    if(tmpIntStatus == VEHICLE.YES){
                        if(maxACStatus == HVAC.HVAC_ON){
                            Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC54 = " + AppConstant.SPEAKTTSONCE_BTNC54);
                            if(!AppConstant.SPEAKTTSONCE_BTNC54) {//一个点火周期播报一次
                                mHandler.sendEmptyMessageDelayed(MSG_BTNC54_OFF,WAITTIME);
                            }
                        }else if(maxACStatus == HVAC.HVAC_OFF){
                            Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC54 = " + AppConstant.SPEAKTTSONCE_BTNC54);
                            if(!AppConstant.SPEAKTTSONCE_BTNC54) {//一个点火周期播报一次
                                mHandler.sendEmptyMessageDelayed(MSG_BTNC54_ON,WAITTIME);
                            }
                        }
                    }
                } else if (value[0] == VEHICLE.KEY_HVAC_RECIRC_MODE) {//空调循环模式
                    if(value[1] == VEHICLE.YES) {//按下
                        if(airCircleMode == HVAC.LOOP_INNER){
                            mHandler.sendEmptyMessageDelayed(MSG_BTNC53_OUTSIDE,WAITTIME);
                        }else if(airCircleMode == HVAC.LOOP_OUTSIDE){
                            mHandler.sendEmptyMessageDelayed(MSG_BTNC53_INNER,WAITTIME);
                        }
                    }
                } else if (value[0] == VEHICLE.KEY_HVAC_FAN_DIRECTION_SWITCH) { //吹风模式
                    if(value[1] == VEHICLE.YES) {//按下
//                        mHandler.sendEmptyMessageDelayed(MSG_BTNC52, 150);//实测100ms左右
                        isDirectionPressed = true;
                        mHandler.sendEmptyMessageDelayed(DIRECTIONPRESSED,150);
                    }
                } else if (value[0] == VEHICLE.KEY_AC_SWITCH) {//压缩机开关
                    acSwitchStatus = value[1];
                    if(acSwitchStatus == VEHICLE.YES){
                        Log.d(TAG, "lh:body turn 空调按键状态(default:-1) --------------acSwitchStatus: " + acSwitchStatus);
                        if(acStatus == VEHICLE.OFF){
                            if(getSpeed() > MAXSPEED100){
                                Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC51 = " + AppConstant.SPEAKTTSONCE_BTNC51);
                                if(!AppConstant.SPEAKTTSONCE_BTNC51) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC51,WAITTIME);
                                }
                            }else {
                                if(!AppConstant.SPEAKTTSONCE_BTNC45) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC45_2,WAITTIME);
                                }
                            }
                        }
                    }
                } else if (value[0] == VEHICLE.KEY_HVAC_POWER_OFF) { //空调关闭
                    Log.d(TAG, "lh:body turn 空调关闭");
                    if(value[1] == VEHICLE.YES) {//按下
                        if(airStatus == VEHICLE.ON){
                            if(getSpeed() > MAXSPEED100){
                                Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC50 = " + AppConstant.SPEAKTTSONCE_BTNC50);
                                if(!AppConstant.SPEAKTTSONCE_BTNC50) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC50,WAITTIME);
                                }
                            }
                        }
                    }
                } else if (value[0] == VEHICLE.KEY_REAR_DEFROST) { //后除霜FP_RearDefrosterSwitch
                    rearDefrostButtonStatus = value[1];//1 按下，其他值 未按下
                    Log.d(TAG, "lh:body turn 后除霜按键状态(default:-1) --------------rearDefrostButtonStatus: " + rearDefrostButtonStatus);
                    if (rearDefrostButtonStatus == VEHICLE.YES) { //后除霜按键按下，且后除霜已打开
                        if(postDefrostStatus == VEHICLE.OFF){
                            if(getSpeed() > MAXSPEED100){
                                Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC48_1 = " + AppConstant.SPEAKTTSONCE_BTNC48_1);
                                if(!AppConstant.SPEAKTTSONCE_BTNC48_1) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC48_1_ON,WAITTIME);
                                }
                            }else {
                                mHandler.sendEmptyMessageDelayed(MSG_BTNC49_1_ON,WAITTIME);
                            }
                        }else if(postDefrostStatus == VEHICLE.ON){
                            if(getSpeed() > MAXSPEED100){
                                Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC48_1 = " + AppConstant.SPEAKTTSONCE_BTNC48_1);
                                if(!AppConstant.SPEAKTTSONCE_BTNC48_1) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC48_1_OFF,WAITTIME);
                                }
                            }else {
                                mHandler.sendEmptyMessageDelayed(MSG_BTNC49_1_OFF,WAITTIME);
                            }
                        }
                    }
                } else if (value[0] == VEHICLE.KEY_HVAC_FRONT_AUTO) {//空调自动模式
                    int tmpIntStatus = value[1];
                    Log.d(TAG, "lh:body turn 空调自动模式按键状态(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
                    if(tmpIntStatus == VEHICLE.YES){//按下
                        if(autoStatus == VEHICLE.OFF){//AC_FrAutoSt 为OFF
                            if(getSpeed() > MAXSPEED100){
                                Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC44 = " + AppConstant.SPEAKTTSONCE_BTNC44);
                                if(!AppConstant.SPEAKTTSONCE_BTNC44) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC44,WAITTIME);
                                }
                            } else {
                                Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC45 = " + AppConstant.SPEAKTTSONCE_BTNC45);
                                if(!AppConstant.SPEAKTTSONCE_BTNC45) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC45,WAITTIME);
                                }
                            }
                        }
                    }
                }else if (value[0] == VEHICLE.KEY_FRONT_DEFROST) {//前除霜
                    int tmpIntStatus = value[1];
                    Log.d(TAG, "lh:body turn 前除霜按键状态(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
                    if (tmpIntStatus == VEHICLE.YES) { //前除霜已打开
                        if(getSpeed() > MAXSPEED100){
                            if(frontDefrostStatus == VEHICLE.OFF){
                                Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC46 = " + AppConstant.SPEAKTTSONCE_BTNC46);
                                if(!AppConstant.SPEAKTTSONCE_BTNC46) {//一个点火周期播报一次
                                    mHandler.sendEmptyMessageDelayed(MSG_BTNC46,WAITTIME);
                                }
                            }
                        }else {
                            if(frontDefrostStatus == VEHICLE.ON){
                                mHandler.sendEmptyMessageDelayed(MSG_BTNC47_OFF,WAITTIME);
                            }else if(frontDefrostStatus == VEHICLE.OFF){
                                mHandler.sendEmptyMessageDelayed(MSG_BTNC47_ON,WAITTIME);
                            }
                        }
                    }
                } else if ((value[0] == VEHICLE.KEY_TEMP_UP_1 || value[0] == VEHICLE.KEY_TEMP_DOWN_1)) {//温度调节
                    if(getSpeed() > MAXSPEED100 && (value[1] == VEHICLE.YES)){//1按下
                        Log.d(TAG, "AppConstant.SPEAKTTSONCE_BTNC43 = " + AppConstant.SPEAKTTSONCE_BTNC43);
                        if(!AppConstant.SPEAKTTSONCE_BTNC43){//一个点火周期播报一次
                            AppConstant.SPEAKTTSONCE_BTNC43 = true;
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC43CONDITION, R.string.btnC43,"","",
                                    R.string.skill_key,R.string.scene_temp_switch,R.string.scene_temp_switch,R.string.condition_btnC43);
                        }
                    }
                } else if (value[0] == KEY_APA_SWITCH) {//自动泊车
                    int tmpIntStatus = value[1];
                    Log.d(TAG, "lh:body turn 自动泊车按键(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
                    if (getSpeed() > 30 && tmpIntStatus == VEHICLE.YES) { //已打开
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_COMMAND,TtsConstant.GUIDEBTNC35CONDITION, R.string.btnC35,"","",
                                R.string.skill_key,R.string.scene_apa,R.string.scene_apa,R.string.condition_btnC35);
                    }
                } else if (value[0] == KEY_ESP_AUTO_HOLD) {//自动驻车 对手件是不满足功能要求的,先注释0820
//                    int tmpIntStatus = value[1];
//                    Log.d(TAG, "lh:body turn 自动驻车按键(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
//                    boolean isAUTOHOLDValid = autoHoldAvailableStatus == VEHICLE.ON ? true : false;
//                    Log.d(TAG, "isAUTOHOLDValid = " + isAUTOHOLDValid);
//                    if (tmpIntStatus == VEHICLE.YES) { //已打开
//                        if(CarUtils.powerStatus >= CarSensorEvent.IGNITION_STATE_ON){//发动机已启动
//                            if(getDriverDoorStatus() == VEHICLE.VEHICLE_ON_ACK && isAUTOHOLDValid){
//                                KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC30CONDITION, R.string.btnC30,"","");
//                            }else if(getDriverDoorStatus() == VEHICLE.VEHICLE_OFF_ACK && driverBuckledStatus == 1 && isAUTOHOLDValid){
//                                KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC33CONDITION, R.string.btnC33,"","");
//                            }else {
//                                KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC29CONDITION, R.string.btnC29,"＃ACTION＃","开启");
//                            }
//                        }else {//发动机未启动
//                            if(getDriverDoorStatus() == VEHICLE.VEHICLE_OFF_ACK && isAUTOHOLDValid){
//                                KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC31CONDITION, R.string.btnC31,"","");
//                            }else if(getDriverDoorStatus() == VEHICLE.VEHICLE_ON_ACK && isAUTOHOLDValid){
//                                KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC32CONDITION, R.string.btnC32,"","");
//                            }
//                        }
//                        //TODO 延时3秒判断，开启AUTOHOLD是否成功
//                    }else if(tmpIntStatus == VEHICLE.NO){//关闭
//                        KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC29CONDITION, R.string.btnC29,"＃ACTION＃","关闭");
//                    }
                } else if (value[0] == KEY_ESP_SWITCH) {//电子稳定系统
                    int tmpIntStatus = value[1];
                    Log.d(TAG, "lh:body turn 电子稳定系统按键(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
                    if(CarUtils.powerStatus < CarSensorEvent.IGNITION_STATE_ON) {//发动机未启动
                        //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC4CONDITION, R.string.btnC4,"","");不支持1054379
                    }else if (powerStatus == CarSensorEvent.IGNITION_STATE_ON && tmpIntStatus == VEHICLE.NO) { //关闭
                        //KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC6CONDITION, R.string.btnC6,"","");
                    } else if (powerStatus == CarSensorEvent.IGNITION_STATE_ON && tmpIntStatus == VEHICLE.YES) {//打开
                        //KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC7CONDITION, R.string.btnC7,"","");
                    }
                }else if(value[0] == KEY_ACC_CTRL_STATUS){//车辆定速巡航状态 off0,active 1,standby 2
                    int cruiseControlStatusCurrent = value[1];
                    Log.d(TAG, "lh:body turn 车辆定速巡航状态(default:-1) --------------cruiseControlStatus: " + cruiseControlStatus);
                    Log.d(TAG, "lh:body turn 车辆定速巡航状态(default:-1) --------------cruiseControlStatusCurrent: " + cruiseControlStatusCurrent);
                    if(isVoiceGuideOpen()){
                        if(cruiseControlStatusCurrent == STANDBY && cruiseControlStatus == ACTIVE && false){//踩下离合踏板(自动挡车无离合踏板)
                            CruiseController.getInstance(mContext).srAction(TtsConstant.CCC8CONDITION);
                        }else if(cruiseControlStatusCurrent == ACTIVE && cruiseControlStatus == STANDBY){
                            isCCC3On = true;
                            mHandler.sendEmptyMessageDelayed(MSG_STANDBY_SET,WAITTIME);//ccc3,ccc9
                        }else if((cruiseControlStatusCurrent == STANDBY && cruiseControlStatus == OFF)){
                            mHandler.sendEmptyMessageDelayed(MSG_CCC1,WAITTIME);
                        }else if(cruiseControlStatusCurrent == OFF && (cruiseControlStatus == ACTIVE || cruiseControlStatus == STANDBY)){
                            mHandler.sendEmptyMessageDelayed(MSG_CCC12,WAITTIME);//判断是ccc12,还是ccc13
                        }else if(cruiseControlStatusCurrent == STANDBY && cruiseControlStatus == ACTIVE){
                            mHandler.sendEmptyMessageDelayed(MSG_CCC7,WAITTIME);
                        }
                    }else if(isVoiceBroadcastOpen()){
                        if(cruiseControlStatusCurrent == STANDBY && cruiseControlStatus == ACTIVE){
                            mHandler.sendEmptyMessageDelayed(MSG_CCC23,WAITTIME);
                        }else if(cruiseControlStatusCurrent == ACTIVE && cruiseControlStatus == STANDBY){
                            mHandler.sendEmptyMessageDelayed(MSG_CCC18_CCC24,WAITTIME);//ccc18,ccc24
                        }else if(cruiseControlStatusCurrent == STANDBY && cruiseControlStatus == OFF) {
                            mHandler.sendEmptyMessageDelayed(MSG_CCC17, WAITTIME);
                        }else if(cruiseControlStatusCurrent == OFF && (cruiseControlStatus == ACTIVE || cruiseControlStatus == STANDBY)){
                            mHandler.sendEmptyMessageDelayed(MSG_CCC25,WAITTIME);//判断是ccc25，还是ccc26
                        }
                    }
                    cruiseControlStatus = cruiseControlStatusCurrent;
                }else if(value[0] == KEY_ACC_SET){//SET-
                    SETSwitchStatus = value[1];
                    Log.d(TAG, "lh:body turn SET-按键(default:-1) --------------SETSwitchStatus: " + SETSwitchStatus);
                    if(SETSwitchStatus == VEHICLE.VEHICLE_ON_ACK) {//按下
                        isSETPressed = true;
                        mHandler.sendEmptyMessageDelayed(SETPRESSED,ACTIONWAITTIME);

                        if(isVoiceGuideOpen()){
                            if(cruiseControlStatus == OFF){
                                CruiseController.getInstance(mContext).srAction(TtsConstant.CCC2CONDITION);
                            }
                        }else if(isVoiceBroadcastOpen()){

                        }
                    }else{
                        if(isVoiceGuideOpen()){
                            if(cruiseControlStatus == STANDBY){
                                mHandler.sendEmptyMessageDelayed(MSG_STANDBY_SET_CC5,WAITTIME);
                            }
                        }else if(isVoiceBroadcastOpen()){
                            if(cruiseControlStatus == STANDBY){
                                mHandler.sendEmptyMessageDelayed(MSG_STANDBY_SET_CCC19,WAITTIME);
                            }
                        }
                    }
                }else if(value[0] == KEY_ACC_RESUME){//RES+
                    RESSwitchStatus = value[1];
                    Log.d(TAG, "lh:body turn RES+按键(default:-1) --------------RESSwitchStatus: " + RESSwitchStatus);
                    if(RESSwitchStatus == VEHICLE.VEHICLE_ON_ACK){//按下
                        isRESPressed = true;
                        mHandler.sendEmptyMessageDelayed(RESPRESSED,ACTIONWAITTIME);

                        if(isVoiceGuideOpen()){
                            if(cruiseControlStatus == OFF){
                                CruiseController.getInstance(mContext).srAction(TtsConstant.CCC2CONDITION);
                            }
                        }else if(isVoiceBroadcastOpen()){

                        }
                    }else {//弹起
                        if(isVoiceGuideOpen()){
                            if(cruiseControlStatus == STANDBY){
                                mHandler.sendEmptyMessageDelayed(MSG_STANDBY_RES,WAITTIME);
                            }
                        }else if(isVoiceBroadcastOpen()){

                        }
                    }
                }else if(value[0] == KEY_CRUISE_UNAVAIL_DISPLAY){//巡航条件不满足显示  收不到上报0828
                    cruiseUnavailDisplay = value[1];
                    Log.d(TAG, "lh:body turn 巡航条件不满足显示(default:-1) --------------cruiseUnavailDisplay: " + cruiseUnavailDisplay);
                }else if(value[0] == KEY_ACC_ENABLE){// ACC使能开关 ON 1;OFF 2
                    ccSwitchStatus = value[1];
                    Log.d(TAG, "lh:body turn CC使能开关(default:-1) --------------ccSwitchStatus: " + ccSwitchStatus);
                    if(ccSwitchStatus == VEHICLE.YES){//按下
                        isCCSwitchPressed = true;
                        mHandler.sendEmptyMessageDelayed(CCSWITCHPRESSED,ACTIONWAITTIME);
                    }
                }else if(value[0] == VEHICLE.KEY_PTS_SWITCH){//按下电动背门内开关
                    int trunkSwitchCurrent = value[1];
                    Log.d(TAG, "按下电动背门内开关---------trunkSwitchCurrent: " + trunkSwitchCurrent + ",trunkSwitch:" + trunkSwitch);
                    Log.d(TAG, "按下电动背门内开关---------trunkPosition: " + trunkPosition + ",carGearNew:" + carGearNew +",trunkStatus:" + trunkStatus);
                    if((trunkSwitchCurrent == VEHICLE.YES && trunkSwitch == VEHICLE.NO) &&
                            carGearNew != CarSensorEvent.GEAR_PARK && trunkPosition == 0){
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_COMMAND,TtsConstant.GUIDEBTNC17CONDITION,R.string.btnC17,"","",
                                R.string.skill_key,R.string.scene_trunk_inner,R.string.scene_trunk_inner,R.string.condition_btnC17);
                    }else if((trunkSwitchCurrent == VEHICLE.YES && trunkSwitch == VEHICLE.NO) &&
                            carGearNew == CarSensorEvent.GEAR_PARK && (trunkPosition >= 0 && trunkPosition <= 5) && trunkStatus == VEHICLE.TRUNKDOOR_OPENING){
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC18CONDITION,R.string.btnC18,"","",
                                R.string.skill_key,R.string.scene_trunk_inner,R.string.scene_trunk_inner,R.string.condition_btnC18);
                    }else if((trunkSwitchCurrent == VEHICLE.YES && trunkSwitch == VEHICLE.NO) &&
                            carGearNew == CarSensorEvent.GEAR_PARK && trunkStatus == VEHICLE.TRUNKDOOR_CLOSING){
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC19CONDITION,R.string.btnC19,"","",
                                R.string.skill_key,R.string.scene_trunk_inner,R.string.scene_trunk_inner,R.string.condition_btnC19);
                    }
                    trunkSwitch = trunkSwitchCurrent;
                }
            }else if (propertyId == ID_WINDOW_LOCK) {//车窗解闭锁开关BCM_WindowlockSwitchStatus false解锁，true闭锁
                if(areaId == VehicleAreaWindow.WINDOW_ALL_WINDSHIELD){
                    boolean windowlockSwitchStatusCurrent = (boolean) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 车窗解闭锁开关状态(default:-1) --------------windowlockSwitchStatus: " + windowlockSwitchStatus);
                    Log.d(TAG, "lh:body turn 车窗解闭锁开关状态(default:-1) --------------windowlockSwitchStatusCurrent: " + windowlockSwitchStatusCurrent);
                    if (windowlockSwitchStatus && !windowlockSwitchStatusCurrent) {//解锁
                        String defaultText = mContext.getString(R.string.btnC40);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC40CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃ACTION＃", "解锁");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts,"解锁");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                Utils.eventTrack(mContext, R.string.skill_key, R.string.scene_window_lock_switch,R.string.scene_window_lock_switch, TtsConstant.GUIDEBTNC40CONDITION, R.string.condition_btnC40, defaultTts);//埋点
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });
                    } else if(!windowlockSwitchStatus && windowlockSwitchStatusCurrent){//闭锁
                        String defaultText = mContext.getString(R.string.btnC40);
                        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.GUIDEBTNC40CONDITION, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(tts)) {
                                    defaultTts = defaultText;
                                }
                                defaultTts = Utils.replaceTts(defaultTts, "＃ACTION＃", "闭锁");
                                defaultTts = KeyGuideController.getInstance(mContext).replaceChar(defaultTts,"闭锁");
                                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                                MyToast.showToast(mContext,defaultTts,true);
                            }
                        });
                    }
                    windowlockSwitchStatus = windowlockSwitchStatusCurrent;
                }
            } else if (propertyId == ID_DOOR_LOCK_STATUS) {//驾驶侧门锁状态信号BCM_DoorlockSwitchStatus false解锁，true闭锁
                doorLockSwitchStatus = (boolean) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 车门锁解闭锁开关(default:-1) --------------doorLockSwitchStatus: " + doorLockSwitchStatus);
                if(!doorLockSwitchStatus){//变为解锁,400ms后将doorLockSwitchStatusNum重置
                    doorLockSwitchStatusNum = 2;
                    mHandler.sendEmptyMessageDelayed(DOORLOCKSWITCHPRESSED,400);
                }else {//闭锁
                    doorLockSwitchStatusNum = 1;
                    mHandler.sendEmptyMessageDelayed(DOORLOCKSWITCHPRESSED,400);
                    if(driverDoorStatus == VEHICLE.ON || left2DoorStatus == VEHICLE.ON || right1DoorStatus == VEHICLE.ON || right2DoorStatus == VEHICLE.ON){
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.SHOW_GUIDE_COMMAND,TtsConstant.GUIDEBTNC39CONDITION, R.string.btnC39,"","",
                                R.string.skill_key,R.string.scene_door_lock_switch,R.string.scene_door_lock_switch,R.string.condition_btnC39);
                    }
                }
            }
//            else if (propertyId == ID_VENDOR_DRIVER_MODE_SET) {//驾驶模式切换拨片
//                int tmpIntStatus = (int) carPropertyValue.getValue();
//                Log.d(TAG, "lh:body turn 驾驶模式切换拨片(default:-1) --------------tmpIntStatus: " + tmpIntStatus);
//                //驾驶模式播报在CarController中进行
//            }
            else if(propertyId == ID_ADAS_AVH_SWITCH){//自动驻车开关
                autoHoldStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 自动驻车开关(default:-1) --------------autoHoldStatus: " + autoHoldStatus);
            }else if(propertyId == ID_FAULT_SYSTEM_CODE_INFO_ALL){//回家功能BCM_BuzzerWarningMode
                Integer[] value = (Integer[]) carPropertyValue.getValue();
                if((null == value) || (null != value && value.length >= 30)){
                    unallowStartSignal = value[29];//档位报警信号
                    Log.d(TAG, "lh:body turn 档位报警信号(default:-1) --------------unallowStartSignal: " + unallowStartSignal);
                    Log.d(TAG, "lh:body turn 蜂鸣器报警模式(default:-1) --------------value[28].intValue(): " + value[28].intValue());
                    if(CarUtils.powerStatus == CarSensorEvent.IGNITION_STATE_OFF && value[28].intValue() == VEHICLE.YES){//蜂鸣器报警模式
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC63CONDITION, R.string.btnC63,"","",
                                R.string.skill_key,R.string.scene_follow_home,R.string.scene_follow_home,R.string.condition_btnC63);
                    }else {
                        //暂不开发
                    }
                }
            }else if(propertyId == ID_ADAS_HDC_CONTROL_ON){//陡坡缓降开关 对手件是不满足功能要求的,先注释0820
//                int HDCButtonStatus = (int) carPropertyValue.getValue();
//                Log.d(TAG, "lh:body turn 陡坡缓降开关(default:-1) --------------HDCButtonStatus: " + HDCButtonStatus);
//                if(getSpeed() > 40){
//                    KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC23CONDITION, R.string.btnC23,"","");
//                }else if(getSpeed() > 35){
//                    KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC22CONDITION, R.string.btnC22,"","");
//                }else if(HDCButtonStatus == VEHICLE.HDC_STATUS_STANDBY || HDCButtonStatus == VEHICLE.HDC_STATUS_ON){
//                    KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC25CONDITION, R.string.btnC25,"","");
//                }else if(HDCButtonStatus == VEHICLE.HDC_STATUS_OFF){
//                    KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC24CONDITION, R.string.btnC24,"","");
//                }
            }else if(propertyId == ID_STARTUP_BTN_STATUS){//启动按钮
                startUpButtonSignal = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 启动按钮状态信号(default:-1) --------------startUpButtonSignal: " + startUpButtonSignal);
            }else if(propertyId == ID_CRUISE_TARGET_SPEED){//巡航目标车速
                targetCruiseSpeed = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 巡航目标车速(default:0) --------------targetCruiseSpeed: " + targetCruiseSpeed);
                if(isVoiceGuideOpen()){
//                    if(cruiseControlStatus == ACTIVE && !isSETPressed){//为防止激活定速巡航后，再次播报速度
//                        Message msg = new Message();
//                        msg.what =  MSG_CCC6;
//                        msg.obj = targetCruiseSpeed;
//                        mHandler.sendMessageDelayed(msg,500 * 6);//3s无变化
//                    }
                    if(cruiseControlStatus == ACTIVE && !isSETPressed && targetCruiseSpeed >= MAXSPEED100){
                        Message msg = new Message();
                        msg.what =  MSG_CCC16;
                        msg.obj = targetCruiseSpeed;
                        mHandler.sendMessageDelayed(msg,0);//3s无变化
                    }
                }else if(isVoiceBroadcastOpen()){
//                    if(cruiseControlStatus == ACTIVE && !isSETPressed){//为防止激活定速巡航后，再次播报速度
//                        Message msg = new Message();
//                        msg.what =  MSG_CCC22;
//                        msg.obj = targetCruiseSpeed;
//                        mHandler.sendMessageDelayed(msg,500 * 6);//3s无变化
//                    }
                }
            }else if(propertyId == ID_DRIVRER_ENG_TORQUE_OVER_ACC){//EMS_ECGPOvrd  0加速；1减速
                //0820需求,删除ccc10,ccc11
//                int engTorqueStatus = (int) carPropertyValue.getValue();
//                Log.d(TAG, "lh:body turn 油门请求(default:0) --------------engTorqueStatus: " + engTorqueStatus);
//                if(isVoiceGuideOpen()){
//                    if(engTorqueStatus == VEHICLE.VEHICLE_OFF_ACK && cruiseControlStatus == 1 && (AppConstant.ccc10SpeakTimes < 3)){
//                        AppConstant.ccc10SpeakTimes++;
//                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC10CONDITION);//一个点火周期仅播报3次
//                    }else if(engTorqueStatus == VEHICLE.VEHICLE_ON_ACK && cruiseControlStatus == 1){
//                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC11CONDITION);
//                    }
//                }else if(isVoiceBroadcastOpen()){
//
//                }
            }else if(propertyId==CarCabinManager.ID_KEEP_DRIVING_TIME){
                float value = (float) carPropertyValue.getValue();
                DrivingCareController.getInstance(mContext).dispatchCommand(value);
                Log.d(TAG, "onChangeEvent() called with: ID_KEEP_DRIVING_TIME = [" + value + "]");
            }else if(propertyId == ID_AVH_AVAILABLE){//AUTOHOLD功能有效否
                autoHoldAvailableStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn AUTOHOLD功能有效否(default:0) --------------autoHoldAvailableStatus: " + autoHoldAvailableStatus);
            }else if(propertyId == CarCabinManager.ID_DOOR_LOCK_STATUS_NEW){//驾驶侧车门解闭锁开关BCM_DriverDoorLockStatus 闭锁5，解锁6
                if(areaId == VehicleAreaDoor.DOOR_ROW_1_LEFT){
                    driverDoorLockStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 驾驶侧车门解闭锁(default:0) --------------driverDoorLockStatus: " + driverDoorLockStatus);
                    if(driverDoorLockStatus == DoorLockStatus.UNLOCKED){
                        mHandler.sendEmptyMessageDelayed(MSG_BTNC38_UNLOCK,200);
                    }else if(driverDoorLockStatus == DoorLockStatus.LOCKED){
                        mHandler.sendEmptyMessageDelayed(MSG_BTNC38_LOCK,200);
                    }
                }
            }else if(propertyId == CarCabinManager.ID_AVM_DISPLAY_FORM){//360全景 视图切源信号
                avmDisplayForm = (int) carPropertyValue.getValue();
                Log.d(TAG, "onChangeEvent: avm display form::"+avmDisplayForm);
            }else if(propertyId == CarCabinManager.ID_PEPS_POWER_DISTRIBUTION_STATUS){//电源分配状态信号
                int powerDistributionStatusCurrent = (int) carPropertyValue.getValue();
                Log.d(TAG, "lh:body turn 电源分配状态信号(default:0) --------------powerDistributionStatusCurrent: " + powerDistributionStatusCurrent);
                if(powerDistributionStatusCurrent != powerDistributionStatus && powerDistributionStatusCurrent == CarSensorEvent.IGNITION_STATE_ON){
                    isPowerOn = false;
                    mHandler.sendEmptyMessageDelayed(COUNTPOWERTIME,1000);
                }
                powerDistributionStatus = powerDistributionStatusCurrent;
            }else if(propertyId == CarCabinManager.ID_BODY_DOOR_TRUNK_DOOR_POS){//尾门位置
                trunkPosition = (int) carPropertyValue.getValue();
                Log.d(TAG, "尾门位置trunkPosition: " + trunkPosition);
            }else if(propertyId == CarCabinManager.ID_BODY_DOOR_TRUNK_DOOR_STATE){//尾门工作状态
                trunkStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "尾门工作状态trunkStatus: " + trunkStatus);
            }else if(propertyId == CarCabinManager.ID_PTS_SHUTFACE_SWITCH){//背门内开关状态信号
                int shutfaceSwitchCurrent = (int) carPropertyValue.getValue();
                Log.d(TAG, "背门内开关状态shutfaceSwitchCurrent: " + shutfaceSwitchCurrent);
                if(shutfaceSwitchCurrent == VEHICLE.VEHICLE_ON_ACK && shutfaceSwitch == VEHICLE.VEHICLE_OFF_ACK){
                    mHandler.sendEmptyMessageDelayed(MSG_BTNC21,500);
                }
                shutfaceSwitch = shutfaceSwitchCurrent;
            }else if(propertyId == CarCabinManager.ID_PEPS_LUGGAGE_UNLOCK){//行李箱解锁信号
                int luggageUnlockSignalCurrent = (int) carPropertyValue.getValue();
                Log.d(TAG, "行李箱解锁信号luggageUnlockSignalCurrent: " + luggageUnlockSignalCurrent);
                if((luggageUnlockSignalCurrent == VEHICLE.LUGGAGE_UNLOCK_PE && luggageUnlockSignal == VEHICLE.LUGGAGE_UNLOCK_INACTIVE) &&
                        (trunkPosition >= 0 && trunkPosition <= 5)){
                    mHandler.sendEmptyMessageDelayed(MSG_BTNC20,150);
                }
                luggageUnlockSignal = luggageUnlockSignalCurrent;
            }else if(propertyId == CarCabinManager.ID_DVR_SDCARD_ERROR_STATUS_ACK){//sdcard错误状态反馈
                sdcardErrorStatus = (int) carPropertyValue.getValue();
                Log.d(TAG, "sdcard错误状态反馈sdcardErrorStatus: " + sdcardErrorStatus);
            }
        }

        @Override
        public void onErrorEvent(int i, int i1) {

        }
    };


    private CarMcuManager.CarMcuEventCallback mcuEventCallback = new CarMcuManager.CarMcuEventCallback(){

        @Override
        public void onChangeEvent(CarPropertyValue carPropertyValue) {
//            Log.d(TAG, "onChangeEvent id = " + toHexString(carPropertyValue.getPropertyId()));
            switch (carPropertyValue.getPropertyId()){
                case CarMcuManager.ID_REVERSE_SIGNAL: //倒车
                    int isReverse = (int) carPropertyValue.getValue();
                    mReverseStatus = isReverse;
                    if (isReverse == VEHICLE.ON) {
                        Utils.exitVoiceAssistant();
                    } else if (isReverse == VEHICLE.OFF){
                        Utils.exitVoiceAssistant();
                    }
                    break;
                case CarMcuManager.ID_MCU_LOST_CANID:
                    Integer[] values = (Integer[]) carPropertyValue.getValue();
                    if((null == values) || (null != values && values.length < 7)){
                        return;
                    }
                    avmLineStatus = values[6].intValue();
                    Log.d(TAG, "lh:body turn 全景线状态(default:0) --------------avmLineStatus: "+avmLineStatus);
                    break;
                case CarMcuManager.ID_NIGHT_MODE:
                    //是否开启小灯 true开启；false关闭
                    isNightOn = (boolean) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 小灯状态(default:false) --------------isNightOn: "+isNightOn);
                    break;
                case ID_VENDOR_ECALL_STATE: //sos
                    //KeyGuideController.getInstance(mContext).handleCarMcuAction(ID_VENDOR_ECALL_STATE);
                    break;
                case CarMcuManager.ID_TSP_REPORT_MSG:
                    RemoteManager.getInstance(mContext).getTspMsg(mContext);
                    break;
                case CarMcuManager.ID_SEND_TO_CAR:
                    int status = (int) carPropertyValue.getValue();
                    RemoteManager.getInstance(mContext).sendToCar(status);
                    break;
                case CarMcuManager.ID_EMS_PARKING_BRAKE://制动踏板EMS_BrakePedalStatus 1 Pressed,0 Not Pressed
                    pressStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "lh:body turn 制动踏板(default:0) --------------pressStatus: "+pressStatus);
                    if(pressStatus == VEHICLE.YES){//踩下
                        isBrakePressed = true;
                        mHandler.sendEmptyMessageDelayed(BRAKEPRESSED,ACTIONWAITTIME);
                    }
                    break;
                case CarMcuManager.ID_VENDOR_BCALL_STATE:
                    int bcallstatus = (int) carPropertyValue.getValue();

                    if(bcallstatus!= BCallTYPE.STATE_IDLE){
                        Utils.exitVoiceAssistant();
                    }

                    Log.d(TAG, "onChangeEvent() bcallstatus called with: carPropertyValue = [" + bcallstatus + "]");
                    break;
            }
        }

        @Override
        public void onErrorEvent(int i, int i1) {

        }
    };

    /**
     * 获取发动机状态 点火状态
     */

    private final CarSensorManager.OnSensorChangedListener carSensorEventCallback = new CarSensorManager.OnSensorChangedListener() {
        @Override
        public void onSensorChanged(CarSensorEvent carSensorEvent) {
            switch (carSensorEvent.sensorType) {
                case CarSensorManager.SENSOR_TYPE_POWER_STATE :
                    int powerStatusCurrent =  carSensorEvent.getPowerStateData(null).powerState;       //获取电源状态
                    Log.d(TAG, "lh:body turn 获取电源状态(default:-1) --------------powerStatus: " + powerStatus);
                    Log.d(TAG, "lh:body turn 获取电源状态(default:-1) --------------powerStatusCurrent: " + powerStatusCurrent);
                    if(powerStatusCurrent != powerStatus && powerStatusCurrent == CarSensorEvent.IGNITION_STATE_ON){//增加防抖
                        VirtualControl.getInstance().clearTtsData();
                        initSpeakTTSOnce();//初始化一个点火周期内的播报次数
                        initCruiseFlag();//初始化一个点火周期内定速巡航是否激活
                        //智能迎宾由车控实现，语音屏蔽bug1062134
//                        if(getWelcomeVisitorsSignal() == 1 || getWelcomeVisitorsSignal() == 4 || getWelcomeVisitorsSignal() == 5)
//                            ActiveServiceModel.getInstance().handleWelcomeVisitors(mContext,getWelcomeVisitorsSignal(),getLockStatus(),getunLockStatus());
                        //换挡手柄逻辑
                        if(startUpButtonSignal == VEHICLE.VEHICLE_ON_ACK && isManualMode(carGearNew)){
                            //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC74CONDITION, R.string.btnC74,"","");
                        }
                        //改用电源分配状态信号PEPS_PowerDistributionStatus判断
//                        isPowerOn = false;
//                        mHandler.sendEmptyMessageDelayed(COUNTPOWERTIME,1000);
                        isPowerOnForAutoWiper = false;//上电的时候，上层应用做了一个等待逻辑，3秒内发生变化不提示雨刮自动挡，1063355
                        mHandler.sendEmptyMessageDelayed(COUNTPOWERTIMEFORAUTOWIPER,3000);
                    }
                    if(powerStatusCurrent != powerStatus && powerStatusCurrent == CarSensorEvent.IGNITION_STATE_ACC){//换挡手柄逻辑
                        if(unallowStartSignal == VEHICLE.VEHICLE_ON_ACK && isManualMode(carGearNew)){
                            //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC73CONDITION, R.string.btnC73,"","");
                        }else if(unallowStartSignal == VEHICLE.VEHICLE_ON_ACK && (carGearNew == carSensorEvent.GEAR_DRIVE || carGearNew == carSensorEvent.GEAR_REVERSE)){
                            //KeyGuideController.getInstance(mContext).handleCarCabinAction(true,TtsConstant.GUIDEBTNC72CONDITION, R.string.btnC72,"","");
                        }
                    }
                    powerStatus = powerStatusCurrent;
                    break;
                case CarSensorManager.SENSOR_TYPE_IGNITION_STATE://获取发动机状态,仅用于 打开自动泊车
                    engineStatus = carSensorEvent.getIgnitionStateData(null).ignitionState;
                    Log.d(TAG, "lh:body turn 获取发动机点火状态(default:-1) --------------engineStatus: " + engineStatus);
                    break;
                case CarSensorManager.SENSOR_TYPE_GEAR://档位
                    carGear = carSensorEvent.getGearData(null).gear;
                    if(carGear == 0){//0是上报的无效值，不做处理
                        return;
                    }
                    EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.GEAR,null,null));
                    RemoteManager.getInstance(mContext).notifyGearStatus(carGear);
                    Log.d(TAG, "lh:body turn 获取当前的档位(default:0) --------------carGear: "+carGear);
                    break;
                case CarSensorManager.SENSOR_TYPE_PARKING_BRAKE://制动踏板BCM_BrakePedalStatus  true踩下
                    carBrake = carSensorEvent.getParkingBrakeData(null).isEngaged;
                    Log.d(TAG, "lh:body turn 获取当前的制动踏板的值(default:false) --------------carBrake: "+carBrake);
                    break;
                case CarSensorManager.SENSOR_TYPE_GEAR_NEW://档位显示
                    carGearNew = carSensorEvent.getShowGearData(null).showGear;
                    Log.d(TAG, "lh:body turn 获取当前的档位(default:0) --------------currentCarGearNew: "+currentCarGearNew);
                    Log.d(TAG, "lh:body turn 获取当前的档位(default:0) --------------carGearNew: "+carGearNew);

                    if(isManualMode(currentCarGearNew) && !isManualMode(carGearNew)){//从手动挡切换到自动挡
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC70CONDITION, R.string.btnC70,"","",
                                R.string.skill_key,R.string.scene_manual_gear,R.string.scene_manual_gear,R.string.condition_btnC70);
                    }else if(!isManualMode(currentCarGearNew) && isManualMode(carGearNew)){//从自动挡切换到手动挡
                        KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC71CONDITION, R.string.btnC71,"","",
                                R.string.skill_key,R.string.scene_manual_gear,R.string.scene_manual_gear,R.string.condition_btnC71);
                    }
                    currentCarGearNew = carGearNew;
                    break;
//                case CarSensorManager.SENSOR_TYPE_EMS_PARKING_BRAKE://制动踏板EMS_BrakePedalStatus true踩下 实际使用BCM_BrakePedalStatus
//                    carBrakeEMS = carSensorEvent.getEmsParkingBrakeData(null).isEngaged;
//                    Log.d(TAG, "lh:body turn 获取当前的制动踏板EMS的值(default:false) --------------carBrakeEMS: "+carBrakeEMS);
//                    break;
                default:
                    break;
            }
        }
    };

    public CarHvacManager.CarHvacEventCallback carHvacEventCallback = new CarHvacManager.CarHvacEventCallback() {
        @Override
        public void onChangeEvent(CarPropertyValue carPropertyValue) {
            switch (carPropertyValue.getPropertyId()) {
                case ID_HVAC_POWER_ON://空调的开关状态
                    if((int) carPropertyValue.getAreaId() == VehicleAreaSeat.HVAC_ALL){
                        airStatus = (int) carPropertyValue.getValue();
                        Log.d(TAG, "onChangeEvent: airStatus = " + airStatus);
                    }
                    break;
                case ID_HVAC_TEMPERATURE_SET://当前温度
                    currentTemp = (float) carPropertyValue.getValue();
                    if (currentTemp==17.5f) {
                        currentTemp=17.0f;
                    }
                    if (currentTemp==32.5f) {
                        currentTemp=33.0f;
                    }
                    Log.d(TAG, "onChangeEvent: currentTemp = " + currentTemp);
                    break;
                case ID_HVAC_TEMPERATURE_LV_SET://当前档位
                    currentGear = (int) carPropertyValue.getValue();
                    Log.d(TAG, "onChangeEvent: currentGear = " + currentGear);
                    break;
                case ID_HVAC_FAN_SPEED_ACK://当前风量
                    currentFanSpeed = (int) carPropertyValue.getValue();
                    Log.d(TAG, "onChangeEvent: currentFanSpeed = " + currentFanSpeed);
                    break;
                case ID_HVAC_FAN_DIRECTION://当前的吹风模式
                    currentFanDirection = (int) carPropertyValue.getValue();
                    Log.d(TAG, "onChangeEvent: currentFanDirection = " + currentFanDirection);
                    String replaceText = "吹面模式";
                    if(isDirectionPressed){
                        if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FACE){
                            replaceText = "吹面模式";
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                    R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                        }else if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FLOOR){
                            replaceText = "吹脚模式";
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                    R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                        }else if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FACE_FLOOR){
                            replaceText = "吹面吹脚模式";
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                    R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                        }else if(currentFanDirection == HVAC.HVAC_FAN_DIRECTION_FLOOR_DEFROST){
                            replaceText = "吹脚除霜模式";
                            KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC52CONDITION, R.string.btnC52,"＃MODE＃",replaceText,
                                    R.string.skill_key,R.string.scene_wind,R.string.scene_wind,R.string.condition_btnC52);
                        }
                    }
                    break;
                case ID_HVAC_AC_ON://当前压缩机开关状态
                    acStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "onChangeEvent: acStatus = " + acStatus);
                    break;
                case ID_HVAC_DEFROSTER://后除霜信号BCM_RearDefrosterstatus
                    if((int) carPropertyValue.getAreaId() == WINDOW_FRONT_WINDSHIELD){
                        frontDefrostStatus = (int) carPropertyValue.getValue();
                        Log.d(TAG, "lh:body turn 前除霜信号(default:0) --------------frontDefrostStatus: " + frontDefrostStatus);
                    }else if((int) carPropertyValue.getAreaId() == WINDOW_REAR_WINDSHIELD){
                        postDefrostStatus = (int) carPropertyValue.getValue();
                        Log.d(TAG, "lh:body turn 后除霜信号(default:0) --------------postDefrostStatus: " + postDefrostStatus);
                    }
                    break;
                case ID_HVAC_RECIRC_ON://内外循环
                    airCircleMode = (int) carPropertyValue.getValue();
                    Log.d(TAG, "onChangeEvent: airCircleMode = " + airCircleMode);
                    break;
                case ID_HVAC_AUTO_ON://AUTO状态
                    autoStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "onChangeEvent: autoStatus = " + autoStatus);
                    break;
                case ID_HVAC_MAX_AC_ON://MAX AC状态
                    maxACStatus = (int) carPropertyValue.getValue();
                    Log.d(TAG, "onChangeEvent: maxACStatus = " + maxACStatus);
                    break;
            }
        }

        @Override
        public void onErrorEvent(int i, int i1) {

        }
    };

    public int getEngineStatus(){
        return engineStatus;
    }

    /**
     * 是否处于休息模式
     * 0x01驾驶模式，0x02礼让模式，0x03休息模式
     */
    public boolean isSleepMode(){
        if(mChairMode == VEHICLE.DMS_CURRENT_POS_REST){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 是否处于休息模式
     * 0x01驾驶模式，0x02礼让模式，0x03休息模式
     */
    public boolean isDrivingMode(){  //驾驶模式
        if(mChairMode == VEHICLE.DMS_CURRENT_POS_DRIVING){
            return true;
        }else {
            return false;
        }
    }

    public int getFaceId(){
        return mFaceId;
    }

    /**0x1-0x7（驾驶模式）/ 0x8（休息模式）
     * 设置座椅模式
     * @param value
     */
    public void setChairMode(int  value){
        try {
            if (mCarCabinManager == null) {
                return;
            }
            mCarCabinManager.setIntProperty(CarCabinManager.ID_SEAT_POS_SELECT,
                    VehicleAreaSeat.SEAT_ROW_1_LEFT, value );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**0x1-0x7（驾驶模式）/ 0x8（休息模式）
     * 保存座椅模式
     * @param value
     */
    public void saveChairMode(int  value){
        try {
            if (mCarCabinManager == null) {
                return;
            }
            mCarCabinManager.setIntProperty(CarCabinManager.ID_SEAT_MEMORY_SELECT,
                    VehicleAreaSeat.SEAT_ROW_1_LEFT, value );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求保存座椅休息模式信号
     */
    public void saveMemoryChairMode(int  value){
        try {
            if (mCarCabinManager == null) {
                return;
            }
            mCarCabinManager.setIntProperty(CarCabinManager.ID_SEAT_MEMORY_SET,
                    VehicleAreaSeat.SEAT_ROW_1_LEFT, value );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否处于倒车界面
     * @return
     */
    public boolean isReverse(){
        return mReverseStatus==VEHICLE.ON;
    }

    /**
     * 获取车速
     */
    public float getSpeed() {
        float status = 0;
        try {
            status = AppConfig.INSTANCE.mCarMcuManager.getFloatProperty(CarMcuManager.ID_PERF_VEHICLE_SPEED,
                    VEHICLE_AREA_TYPE_GLOBAL);
            Log.d(TAG, "lh:get speed(default:-1)---" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return status;
    }

    // @data_enum 1-inactive 2-end
    //语音播报结束通知dms控制器，可以监听状态
    public void setDmsStatus(int value){
        Log.d(TAG, "setDmsStatus() called with: value = [" + value + "]"+"....mCarCabinManager:"+(mCarCabinManager!=null));
        try {
            if (mCarCabinManager != null) {
                mCarCabinManager.setIntProperty(CarCabinManager.ID_DMS_VOICE_END, VEHICLE_AREA_TYPE_GLOBAL, value);
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    public int getAvmLineStatus(){
        return avmLineStatus;
    }

    public int getPostDefrostStatus(){
        return postDefrostStatus;
    }

    public int getWelcomeVisitorsSignal(){
        return welcomeVisitorsSignal;
    }

    public int getLockStatus(){
        return lockStatus;
    }

    public int getunLockStatus(){
        return unLockStatus;
    }

    public boolean getNightStatus(){
        return isNightOn;
    }

    public int getLeft1carWindowStudyStatus(){
        return left1carWindowStudyStatus;
    }

    public int getLeft2carWindowStudyStatus(){
        return left2carWindowStudyStatus;
    }

    public int getRight1carWindowStudyStatus(){ return right1carWindowStudyStatus;}

    public int getRight2carWindowStudyStatus(){ return right2carWindowStudyStatus;}

    public int getAirCircleMode(){ return airCircleMode;}

    public int getDriverDoorStatus(){ return driverDoorStatus;}

    public int getHeadLightsStatus(){ return headLightsStatus;}

    public void initSpeakTTSOnce(){
        AppConstant.SPEAKTTSONCE_BTNC43 = false;
        AppConstant.SPEAKTTSONCE_BTNC44 = false;
        AppConstant.SPEAKTTSONCE_BTNC45 = false;
        AppConstant.SPEAKTTSONCE_BTNC46 = false;
        AppConstant.SPEAKTTSONCE_BTNC47 = false;
        AppConstant.SPEAKTTSONCE_BTNC48_1 = false;
        AppConstant.SPEAKTTSONCE_BTNC49 = false;
        AppConstant.SPEAKTTSONCE_BTNC50 = false;
        AppConstant.SPEAKTTSONCE_BTNC51 = false;
        AppConstant.SPEAKTTSONCE_BTNC52 = false;
        AppConstant.SPEAKTTSONCE_BTNC53 = false;
        AppConstant.SPEAKTTSONCE_BTNC54 = false;
        AppConstant.SPEAKTTSONCE_BTNC55 = false;
        AppConstant.SPEAKTTSONCE_BTNC57 = false;
        AppConstant.SPEAKTTSONCE_BTNC58 = false;
        AppConstant.SPEAKTTSONCE_BTNC59 = false;
        AppConstant.SPEAKTTSONCE_BTNC60 = false;
        AppConstant.isfirst = false;
    }

    public void initCruiseFlag(){
        AppConstant.isFirstUseCruise = true;
        AppConstant.isFirstAdjustCruiseSpeed = true;
        AppConstant.ccc10SpeakTimes = 0;
        AppConstant.isFirstPressBrake = true;
    }

    public int getAutoHoldAvailableStatus(){ return autoHoldAvailableStatus;}

    private boolean isManualMode(int signal){//是否为手动挡
        if(signal == CarSensorEvent.GEAR_FIRST || signal == CarSensorEvent.GEAR_SECOND || signal == CarSensorEvent.GEAR_THIRD ||
                signal == CarSensorEvent.GEAR_FOURTH || signal == CarSensorEvent.GEAR_FIFTH || signal == CarSensorEvent.GEAR_SIXTH ||
                signal == CarSensorEvent.GEAR_SEVENTH || signal == CarSensorEvent.GEAR_EIGHTH){
            return true;
        }
        return false;
    }

    public boolean isVoiceGuideOpen(){
        isVoiceGuideOpen = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_GUIDE, AppConstant.VALUE_VOICE_GUIDE);
        Log.d(TAG,"isVoiceGuideOpen: " + isVoiceGuideOpen);
        return isVoiceGuideOpen;
    }

    public boolean isVoiceBroadcastOpen(){
        isVoiceBroadcastOpen = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_BROADCAST, AppConstant.VALUE_VOICE_GUIDE);
        Log.d(TAG, "isVoiceBroadcastOpen: " + isVoiceBroadcastOpen);
        return isVoiceBroadcastOpen;
    }

    public int getTargetCruiseSpeed(){
        return targetCruiseSpeed;
    }

    public boolean isApaOpen(){
        return mApaStatus>0;
    }

    public boolean isAvmOpen(){
        return avmStatus == AVM.ON;
    }

    public int getCruiseControlStatus(){
        return  cruiseControlStatus;
    }

    /**
     * 全景DVR显示视角
     *
     * @param value DVR.DVR_VISUAL_ANGLE_FRONT  前
     *              DVR.DVR_VISUAL_ANGLE_REAR   后
     *              DVR.DVR_VISUAL_ANGLE_LEFT   左
     *              DVR.DVR_VISUAL_ANGLE_RIGHT  右
     *              DVR.DVR_VISUAL_ANGLE_ALL    组合视图
     */
    public void setDvrVisualAngle(int value) {
        if(mCarCabinManager!=null){
            try {
                mCarCabinManager.setIntProperty(CarCabinManager.ID_DVR_DISPLAY_VISION, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL,value);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     *  public static final int AVM_DISPLAY_ALL_3D_FRONT = 8;
     *  public static final int AVM_DISPLAY_ALL_3D_REAR = 9;
     *  public static final int AVM_DISPLAY_ALL_3D_LEFT = 10;
     *  public static final int AVM_DISPLAY_ALL_3D_RIGHT = 11;
     * @param value
     */
    public void setAVMVisualAngle(int value) {
        if(mCarCabinManager!=null){
            try {
                mCarCabinManager.setIntProperty(CarCabinManager.ID_AVM_DISPLAY_FORM, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL,value);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    private int current;
    private void restartScrene(){
        Log.d(TAG, "restartScrene() called");
        TspSceneManager.getInstance().resetScrene(mContext,current);
        current = -1;
    }

    /**上传远程状态信息
     * 设置座椅模式
     * @param value
     */
    public void setRemoteStatus(boolean  value){
        try {
            Log.d(TAG, "setRemoteStatus() called with: value = [" + value + "]"+mCarMcuManager);
            if (mCarMcuManager == null) {
                return;
            }
            if(value) //预约导航
                mCarMcuManager.setIntArrayProperty(CarMcuManager.ID_REMOTE_VOICE_MODE_EVENT,
                        VEHICLE_AREA_TYPE_GLOBAL, new int[]{1,2,0,0});
            else  //取消导航
                mCarMcuManager.setIntArrayProperty(CarMcuManager.ID_REMOTE_VOICE_MODE_EVENT,
                        VEHICLE_AREA_TYPE_GLOBAL, new int[]{1,1,0,0});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取四个车门的初始状态
    public int getDoorStatus(int areaId){
        int status = VEHICLE.OFF;
        try {
            status = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(CarCabinManager.ID_DOOR_POS, areaId);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return status;
    }

    /*
    *  DVR.DVR_SDCARD_ERROR_NO_ERROR；SD卡无错误
    *  DVR.DVR_SDCARD_ERROR_NO_SDCARD；SD卡无挂载
    *  DVR.DVR_SDCARD_ERROR_INSUFFICIENT；SD卡容量不足
     */
    public int getSdcardErrorStatus(){
        return sdcardErrorStatus;
    }

    //获取背门的位置
    public int getTrunkPosition(){
        return trunkPosition;
    }

    /*
    *  下发云眼激活指令
     */
    public void setCloudEyeUsage(int value) {
        if(mCarMcuManager != null){
            try {
                mCarMcuManager.setIntProperty(CarMcuManager.ID_HU_RVACTIVE, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL,value);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    *  获取云眼激活指令
     */
    public int getCloudEyeUsage() {
        int status = 0;
        if(mCarMcuManager != null){
            try {
                status = mCarMcuManager.getIntProperty(CarMcuManager.ID_HU_RVACTIVE_STATUS, VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }
        return status;
    }
}
