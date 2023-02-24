package com.chinatsp.ifly.voice.platformadapter.controller;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.VehicleAreaDoor;
import android.car.VehicleAreaType;
import android.car.VehicleAreaWindow;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.cabin.CarCabinManager;
import android.car.hardware.constant.APA;
import android.car.hardware.constant.AVM;
import android.car.hardware.constant.HVAC;
import android.car.hardware.constant.LIGHT;
import android.car.hardware.constant.VEHICLE;
import android.car.hardware.hvac.CarHvacManager;
import android.car.hardware.mcu.CarMcuManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.automotive.vehicle.V2_0.DoorLockStatus;
import android.hardware.automotive.vehicle.V2_0.VehicleDisplayForm;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.excontrol.ExControlManager;
import com.chinatsp.excontrol.listener.DriveModeChangeListenr;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.ICarController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MultiSemantic;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.seopt.SeoptManager;
import com.iflytek.speech.libissseopt;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.internal.Util;

import static android.car.VehicleAreaSeat.HVAC_ALL;
import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static android.car.VehicleAreaWindow.WINDOW_ROOF_TOP_1;
import static android.car.VehicleAreaWindow.WINDOW_ROW_1_LEFT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_1_RIGHT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_2_LEFT;
import static android.car.VehicleAreaWindow.WINDOW_ROW_2_RIGHT;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_LIGHT_ATMO_BRIGHT_LEVEL;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_LIGHT_ATMO_ON;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_POS;
import static android.car.hardware.cabin.CarCabinManager.ID_BODY_WINDOW_WIPER;
import static android.car.hardware.cabin.CarCabinManager.ID_WINDOW_POS;
import static android.car.hardware.constant.HVAC.HVAC_OFF_REQ;
import static android.car.hardware.constant.HVAC.HVAC_ON;
import static android.car.hardware.constant.HVAC.HVAC_ON_REQ;
import static android.car.hardware.constant.HVAC.HVAC_REQ;
import static android.car.hardware.constant.HVAC.LOOP_OUTSIDE;
import static android.car.hardware.constant.VEHICLE.SUNROOF_MOVEMENT_STOPPED;
import static android.car.hardware.constant.VehicleAreaId.GLOBAL_ROW_1_LEFT;
import static android.car.hardware.constant.VehicleAreaId.SEAT_ROW_1_LEFT;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_FRONT_WINDSHIELD;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_RECIRC_ON;
import static android.car.hardware.mcu.CarMcuManager.ID_VENDOR_OFF_LINE_STATUS;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.PARKINGC3CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.PARKINGC4CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.PARKINGC4_2CONDITION;

public class CarController extends BaseController implements ICarController {
    private final static String TAG = "CarController";
    private Context mContext;
    public final static String SMOKING = "抽烟模式";
    private final static String SKYLIGHT = "天窗";
    private final static String SKYLIGHT_OPEN = "天窗透气";
    private final static String SKYLIGHT_OPEN2 = "天窗通风";
    private final static String SKYLIGHT_OPEN3 = "打开透气";
    private final static String ABAT_VENT = "遮阳帘";
    private final static String DRIVING_MODE = "驾驶模式";
    public final static String LAMP = "氛围灯";
    private final static String LAMPLIGHTPLUS = "PLUS";
    private final static String LAMPLIGHTMINUS = "MINUS";
    private final static String LAMPLIGHTPLUS_1 = "高";
    private final static String LAMPLIGHTPLUS_2 = "低";
    private final static String TRUNK = "后备箱";
    public final static String WINDOW_ALL = "所有窗户";
    public final static String WINDOW_LEFT_FRONT = "左前车窗";
    private final static String WINDOW_RIGHT_FRONT = "右前车窗";
    public final static String WINDOW_LEFT_FRONT2 = "主驾车窗";
    public final static String WINDOW_RIGHT_FRONT2 = "副驾车窗";
    public final static String WINDOW_LEFT_BACK = "左后车窗";
    public final static String WINDOW_RIGHT_BACK = "右后车窗";
    private final static String WINDOW_BACK = "后车窗";
    private final static String WINDOW = "窗户";
    private final static String WIPER = "雨刮器";
    private final static String PARK_AUTO = "自动泊车";
    private final static String PRESSURE_MONITOR = "胎压监测";
    private final static String SLIDE_DOOR_LEFT = "左侧滑门";
    private final static String SLIDE_DOOR_RIGHT = "右侧滑门";
    private final static String SLIDE_DOOR_CENTER = "中滑门";
    private final static String SLIDE_DOOR_ALL = "所有滑门";
    private final static String FRONT_SHROUD = "电动前罩";
    //全景影像
    private final static String PANO_IMAGE = "360";
    //车辆设置
    private final static String CAR_SETTING = "车辆设置";
    private final static String COLORS = "红色、橙色、黄色、绿色、紫色、蓝色、粉色";

    private final static String SET = "SET";
    private final static String MODE_DRIVER = "驾驶模式";
    private final static String DRIVING_MODE_ECO = "经济";
    private final static String DRIVING_MODE_SPORT = "运动";
    private final static String DRIVING_MODE_NOMARL = "舒适";
    private final static String DRIVING_MODE_CUS = "自定义";
    public static final int DRIVE_MODE_NOMARL = 0;  //舒适模式
    public static final int DRIVE_MODE_ECO = 1; //经济模式
    public static final int DRIVE_MODE_SPORT = 2;   //运动模式
    public static final int DRIVE_MODE_CUST = 3;  //自定义模式(不支持界面跳转)

    public static final int LOCAL = 1;
    public static final int NAVI = 2;

    private String conditionId = "";
    private String defaultText;
    //导航的包名
    public static final String PACKAGE_NAME_NAVI = "com.tencent.wecarnavi";
    //打开车控的action
    public static final String OPEN_VC_FROM_OTHERS = "open_vc_from_others";
    //打开车况app key
    public static final String EXTRA_KEY_OPEN_VC = "open_vc";
    //打开车控type key
    public static final String EXTRA_KEY_OPEN_VC_TYPE = "vc_type";
    //打开车控frag value
    public static final String EXTRA_OPEN_SETTING_VALUE_VC = "frag_vc";
    //车灯
    public static final int OPEN_VC_TYPE_CARLIGHT = 21;
    //车辆
    public static final int OPEN_VC_TYPE_CARSETTINGS = 22;
    //打开车况type key
    public static final String EXTRA_KEY_OPEN_CT_TYPE = "ct_type";
    //胎压检查
    public static final int OPEN_CT_TYPE_PRESSURE = 11;
    //车况体检
    public static final int OPEN_CT_TYPE_TEST= 10;
    //打开车况frag value
    public static final String EXTRA_OPEN_SETTING_VALUE_CT = "frag_ct";
    private static final int CLOSE_SHADE = 1000;
    private static final int MSG_START_TTS = 1001;
    private static final int MSG_StartTTS = 1002;
    private static final int MSG_SHOW_ASSISTANT= 1003;
    private static final int CLOSE_SUNROOF = 1004;
    private static final int MSG_SHOW_WORD= 1005;
    private boolean isContinueNext = true;
    private static boolean StartTTS = false;
    //氛围灯 配置 0 ：未配置，  1：配置
    public static final String AMBIENT_TYPE = "persist.vendor.vehicle.atmo.enable";
    //自动泊车 配置 0 ：未配置，  1：配置
    public static final String APA_TYPE = "persist.vendor.vehicle.apa";
    private int appName = 0;
    private int scene = 0;
    private int object = 0;
    private int condition = 0;
    private int airStatus = 1;
    private ExControlManager mControlManager;
    private static CarController mCarController;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CLOSE_SUNROOF:
                    setAbatVentStatus(0);//关闭遮阳帘
                    break;
                case CLOSE_SHADE:
                    int abatPos = getAbatVentPos();
                    setAbatVentStatus(0);
                    //遮阳帘位置: 100-全开,0-全关
                    Map map = (Map) msg.obj;
                    checkAbatVentSettingStatus(false,abatPos,(int)map.get("appName"),(int)map.get("scene"),(int)map.get("object"),(int)map.get("condition"));
                    break;
                case MSG_START_TTS:
                    startTTS((String) msg.obj);
                    break;
                case MSG_StartTTS:
                    if (!StartTTS){//播报被打断，播报完成接口未成功调用
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                    }
                    break;
                case MSG_SHOW_ASSISTANT:
                    Map map1 = (Map) msg.obj;
                    startSpeak((String) (map1.get("conditionId")),(String) (map1.get("position")));
                    break;
                case MSG_SHOW_WORD:
                    Map map_word = (Map) msg.obj;
                    getMessageWithoutTtsSpeak((String) map_word.get("conditionId"), (String) map_word.get("defaultText"), "#WINDOW#", "",
                            (int)map_word.get("appName1"),(int)map_word.get("scene1"),(int)map_word.get("object1"),(int)map_word.get("condition1"));
                    break;
                default:
                    break;
            }
        }
    };

    public static CarController getInstance(Context c){
        if(mCarController == null)
            mCarController  = new CarController(c);
        return mCarController;
    }


    private CarController(Context mContext) {
        this.mContext = mContext;
        mControlManager = ExControlManager.getInstance(mContext, NAVI);//本地安装车控测试时，这里改成LOCAL
        //用于监听运动模式的变化，实体按键有变化，车控界面驾驶模式跟着变化，此事件由车控发出，与车控保持一致
        mControlManager.registerListener(new DriveModeChangeListenr() {
            @Override
            public void driveModeChange(int i) {
                Log.d(TAG,"i = " + i);
                if (i == 3) { //自定义模式
                    KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC37CONDITION, R.string.btnC37,"＃MODE＃","自定义模式",
                            R.string.skill_key,R.string.scene_drive_mode_switch,R.string.scene_drive_mode_switch,R.string.condition_btnC37);
                } else if (i == 2) { //运动模式
                    KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC37CONDITION, R.string.btnC37,"＃MODE＃","运动模式",
                            R.string.skill_key,R.string.scene_drive_mode_switch,R.string.scene_drive_mode_switch,R.string.condition_btnC37);
                } else if (i == 1) { //经济模式
                    KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC37CONDITION, R.string.btnC37,"＃MODE＃","经济模式",
                            R.string.skill_key,R.string.scene_drive_mode_switch,R.string.scene_drive_mode_switch,R.string.condition_btnC37);
                }else if (i == 0) { //舒适模式
                    KeyGuideController.getInstance(mContext).handleCarCabinAction(AppConstant.HIDE_BROADCAST,TtsConstant.GUIDEBTNC37CONDITION, R.string.btnC37,"＃MODE＃","舒适模式",
                            R.string.skill_key,R.string.scene_drive_mode_switch,R.string.scene_drive_mode_switch,R.string.condition_btnC37);
                }
            }
        });
    }

    @Override
    public void srAction(IntentEntity intentEntity) {

    }

    public void srActionCar(IntentEntity intentEntity, String intentStr) {
        //TODO 调用车控相关接口
        EventBusUtils.sendTalkMessage(intentEntity.text);
        JSONObject nameValueObj = null;
        String nameValue = "";
        try {
            JSONObject obj = new JSONObject(intentStr);
            JSONObject intentObj = obj.getJSONObject("intent");
            JSONObject semanticObj = intentObj.getJSONObject("semantic");
            JSONObject slotsObj = semanticObj.getJSONObject("slots");

            if (slotsObj.has("nameValue")) {
                nameValue = slotsObj.getString("nameValue");
                try {
                    nameValueObj = new JSONObject(nameValue);
                } catch (JSONException e) {
                    Log.i(TAG, "e == " + e);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        conditionId = "";
//        Log.d(TAG, "-----------lh---name----" + intentEntity.semantic.slots.name + ",operation:" + intentEntity.operation);
        int type = getACStatus();
        Log.d(TAG,"power type = " + type);
        if(intentEntity!=null&&intentEntity.semantic!=null
                &&intentEntity.semantic.slots!=null
                &&intentEntity.semantic.slots.mode!=null
                && ("座椅模式".equals(intentEntity.semantic.slots.mode) || "休息模式".equals(intentEntity.semantic.slots.mode))){
            if("打开休息模式".equals(intentEntity.text) || "休息模式".equals(intentEntity.text)){
                intentEntity.semantic.slots.mode = "座椅模式";
                intentEntity.semantic.slots.modeValue = "休息";
            }
            ChairController.getInstance(mContext).srAction(intentEntity);
            Log.e(TAG, "srAction: "+intentEntity.semantic.slots.mode);
            return;
        }

        int engineStatus = CarUtils.getInstance(mContext).getEngineStatus();
        Log.d(TAG,"engineStatus = " + engineStatus);
        if(engineStatus <= CarSensorEvent.IGNITION_STATE_ACC && intentEntity.semantic.slots.name != null && PARK_AUTO.equals(intentEntity.semantic.slots.name)){
            conditionId = PARKINGC4_2CONDITION;
            defaultText = mContext.getString(R.string.parkingC4_2);
            getMessageWithoutTtsSpeak(PARKINGC4_2CONDITION,mContext.getString(R.string.parkingC4_2),"","",
                    R.string.skill_auto_park,R.string.scene_auto_park_app,R.string.object_apa_start,R.string.condition_parkingC4_2);
            return;
        }
        if(type <= CarSensorEvent.IGNITION_STATE_ACC && intentEntity.semantic.slots.name != null && WIPER.equals(intentEntity.semantic.slots.name)){//雨刮
            conditionId = WIPERC4CONDITION;
            defaultText = mContext.getString(R.string.wiperC4);
            getMessageWithoutTtsSpeak(WIPERC4CONDITION,mContext.getString(R.string.wiperC4),"","",
                    R.string.skill_wiper,R.string.scene_excption,R.string.object_wiper_exception,R.string.condition_wiper4);
            return;
        }

        if (type <= CarSensorEvent.IGNITION_STATE_OFF) {
            if(!TextUtils.isEmpty(intentEntity.semantic.slots.mode) && SMOKING.equals(intentEntity.semantic.slots.mode)){//抽烟模式
                if(FloatViewManager.getInstance(mContext).isHide()){
                    showAssistant();

                    Message msg = new Message();
                    Map map = new HashMap();
                    map.put("conditionId",SKYLIGHTC23CONDITION);
                    map.put("defaultText",mContext.getString(R.string.carwindow_c23));
                    map.put("appName1",R.string.skill_carwindow);
                    map.put("scene1",R.string.scene_excption);
                    map.put("object1",R.string.object_exception_other);
                    map.put("condition1",R.string.condition_carwindow23);

                    msg.what = MSG_SHOW_WORD;
                    msg.obj = map;
                    handler.sendMessageDelayed(msg,600);
                }else {
                    conditionId = SKYLIGHTC23CONDITION;
                    defaultText = mContext.getString(R.string.carwindow_c23);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                    Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_excption, R.string.object_exception_other,conditionId,R.string.condition_carwindow23);
                }
            }else if(intentEntity.semantic.slots.name == null){
                doExceptonAction(mContext);
            }else if((intentEntity.semantic.slots.name).contains("车窗") || (intentEntity.semantic.slots.name).contains("窗户")){//车窗
                conditionId = CARWINDOWC23CONDITION;
                defaultText = mContext.getString(R.string.carwindow_c23);
                getMessageWithTtsSpeak(conditionId, defaultText);
                Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_excption, R.string.object_exception_other,conditionId,R.string.condition_carwindow23);
            }else if(SKYLIGHT.equals(intentEntity.semantic.slots.name) || SKYLIGHT_OPEN.equals(intentEntity.semantic.slots.name) ||
                    SKYLIGHT_OPEN2.equals(intentEntity.semantic.slots.name)){//天窗
                conditionId = SKYLIGHTC29CONDITION;
                defaultText = mContext.getString(R.string.skylight_c29);
                getMessageWithTtsSpeak(conditionId, defaultText);
                Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_excption, R.string.object_exception_other,conditionId,R.string.condition_skylight29);
            }else if(ABAT_VENT.equals(intentEntity.semantic.slots.name)){//遮阳帘
                conditionId = SKYLIGHTC30CONDITION;
                defaultText = mContext.getString(R.string.skylight_c30);
                getMessageWithTtsSpeak(conditionId, defaultText);
                Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_excption, R.string.object_exception_other,conditionId,R.string.condition_skylight30);
            }else if(DRIVING_MODE.equals(intentEntity.semantic.slots.mode)){
                getMessageWithoutTtsSpeak(DRIVRINGC3CONDITION,mContext.getString(R.string.driving_mode_c3),"","",R.string.skill_driving_mode,R.string.scene_driving_mode,R.string.object_driving_mode,R.string.condition_driving_c3);
            }
            else if(LAMP.equals(intentEntity.semantic.slots.name)) {//氛围灯
                conditionId = ATMOSPHERELAMPC2_1CONDITION;
                defaultText = mContext.getString(R.string.atmospherelampC2_1);
                getMessageWithTtsSpeak(conditionId, defaultText);
                Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp, R.string.object_open_close_atmospherelamp_exception,
                        ATMOSPHERELAMPC2_1CONDITION,R.string.condition_atmospherelampC2_1);
            } else if(PANO_IMAGE.equals(intentEntity.semantic.slots.name)){//全景
                conditionId = PANORAMICC3CONDITION;
                defaultText = mContext.getString(R.string.panoramicC3);
                getMessageWithTtsSpeak(conditionId, defaultText);
                Utils.eventTrack(mContext, R.string.skill_panoramic, R.string.scene_panoramic, R.string.object_panoramic,conditionId,R.string.condition_panoramic3);
            }
            else {
                doExceptonAction(mContext);
            }
            return;
        }
        if (SKYLIGHT.equals(intentEntity.semantic.slots.name) || SKYLIGHT_OPEN.equals(intentEntity.semantic.slots.name) ||
                SKYLIGHT_OPEN2.equals(intentEntity.semantic.slots.name) || (!TextUtils.isEmpty(intentEntity.semantic.slots.mode) &&
                SMOKING.equals(intentEntity.semantic.slots.mode))) { //天窗
            sunroofControl(intentEntity,nameValue);
        } else if (ABAT_VENT.equals(intentEntity.semantic.slots.name)) {
            if(PlatformConstant.Operation.OPEN.equals(intentEntity.operation) ||
                    PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)){
                boolean abatVentStatus = checkAbatVentRunning();
                if(!abatVentStatus){//未处于运动状态中
                    //遮阳帘位置: 100-全开,0-全关
                    int abatPos = getAbatVentPos();
                    if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                        //checkSunroofRunningStatus(true);
                        //遮阳帘打开
                        if (abatPos == 100) {
                            //text = "遮阳帘已打开";
                            getMessageWithTtsSpeak(SKYLIGHTC17CONDITION, R.string.skylightC17);
                            Utils.eventTrack(mContext,R.string.skill_skylight, R.string.scene_skylight_abatvent, R.string.object_skylight_abatvent1,SKYLIGHTC17CONDITION,R.string.condition_skylight17);
                        } else {
                            //text = "好的，正在打开遮阳帘";
                            conditionId = SKYLIGHTC16CONDITION;
                            defaultText = mContext.getString(R.string.skylightC16);
                            setAbatVentStatus(100);
                            checkAbatVentSettingStatus(true,abatPos,R.string.skill_skylight, R.string.scene_skylight_abatvent, R.string.object_skylight_abatvent1,R.string.condition_skylight16);
                        }
                    } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                        //checkSunroofRunningStatus(false);
                        //遮阳帘打开
                        if (abatPos > 0) { //遮阳帘未完全关闭
                            //获取天窗位置
                            int windowsStatus = getSunroofPosition();
                            //获取天窗状态
                            int mTopWindowStatus = getSunroofStatus();
                            if(mTopWindowStatus == VEHICLE.SUNROOF_FULLY_CLOSE || windowsStatus == 0) { //天窗已完全关闭
                                conditionId = SKYLIGHTC18CONDITION;
                                defaultText = mContext.getString(R.string.skylightC18);
                                setAbatVentStatus(0);
                                checkAbatVentSettingStatus(false,abatPos,R.string.skill_skylight, R.string.scene_skylight_abatvent, R.string.object_skylight_abatvent2,R.string.condition_skylight18);
                            }else {//天窗未完全关闭
                                conditionId = SKYLIGHTC19CONDITION;
                                defaultText = mContext.getString(R.string.skylightC19);
                                //关闭天窗
                                setSunroofPos(0);
                                Message msg = new Message();
                                msg.what = CLOSE_SHADE;
                                Map map = new HashMap();
                                map.put("appName",R.string.skill_skylight);
                                map.put("scene",R.string.scene_skylight_abatvent);
                                map.put("object",R.string.object_skylight_abatvent2);
                                map.put("condition",R.string.condition_skylight19);
                                msg.obj = map;
                                handler.sendMessageDelayed(msg, 300);
                            }
                        } else { //遮阳帘完全关闭
                            //text = "遮阳帘已关闭";
                            getMessageWithTtsSpeak(SKYLIGHTC20CONDITION, R.string.skylightC20);
                            Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_abatvent, R.string.object_skylight_abatvent2,SKYLIGHTC20CONDITION,R.string.condition_skylight20);
                        }
                    }else {
                        doExceptonAction(mContext);
                    }
                }
            }else {
                doExceptonAction(mContext);
            }
        }
        else if (LAMP.equals(intentEntity.semantic.slots.name)) {
            //氛围灯 配置 0 ：未配置，  1：配置
            int hasLamp = Utils.getInt(mContext, AMBIENT_TYPE, 0);
            Log.d(TAG, "hasLamp = " + hasLamp);
            if(hasLamp == 0){//没有氛围灯
                getMessageWithTtsSpeak(ATMOSPHERELAMPC9CONDITION,R.string.atmospherelampC9);
                Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_excption, R.string.object_exception, TtsConstant.ATMOSPHERELAMPC9CONDITION, R.string.condition_atmospherelampC9);
                return;
            }
            boolean isNightOn = CarUtils.getInstance(mContext).getNightStatus();
            Log.d(TAG,"isNightOn = " + isNightOn);
            if(!isNightOn){
                getMessageWithTtsSpeak(ATMOSPHERELAMPC2_3CONDITION, R.string.atmospherelampC2_3);
                Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp, R.string.object_open_close_atmospherelamp_exception,ATMOSPHERELAMPC2_3CONDITION,R.string.condition_atmospherelampC2_3);
                return;
            }
            Log.d(TAG,"CarUtils.lampSwitch = " + CarUtils.lampSwitch + ",CarUtils.lampLight = " + CarUtils.lampLight);
            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                if(CarUtils.lampSwitch == VEHICLE.OFF){
                    int light = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_LIGHT,VEHICLE.OFF);
                    int color = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_COLOR,5);
                    Log.d(TAG, "srActionCar1: light = " + light + ",color = " + color);
                    setLampParams(VEHICLE.ON,light,color);
                    checkLampSwitcherStatus(VEHICLE.ON);
                }else {
                    getMessageWithTtsSpeak(ATMOSPHEREAMPC1CONDITION, R.string.atmospherelampC1);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp, R.string.object_atmospherelamp1,ATMOSPHEREAMPC1CONDITION,R.string.condition_default);
                }
            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                if(CarUtils.lampSwitch == VEHICLE.ON){
                    int light = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_LIGHT,VEHICLE.OFF);
                    int color = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_COLOR,5);
                    Log.d(TAG, "srActionCar2: light = " + light + ",color = " + color);
                    setLampParams(VEHICLE.OFF,light,color);
                    checkLampSwitcherStatus(VEHICLE.OFF);
                }else {
                    getMessageWithTtsSpeak(ATMOSPHERELAMPC2CONDITION, R.string.atmospherelampC2);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp, R.string.object_atmospherelamp2,ATMOSPHERELAMPC2CONDITION,R.string.condition_default);
                }
            }else if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
                if (null != intentEntity.semantic.slots.color) {
                    //切换氛围灯颜色
                    if (COLORS.contains(intentEntity.semantic.slots.color)) {
                        int light = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_LIGHT,VEHICLE.OFF);
                        int color = getColor(intentEntity.semantic.slots.color);
                        Log.d(TAG, "srActionCar3: light = " + light + ",color = " + color);
                        setLampParams(VEHICLE.ON,light,color);
                        checkLampColorStatus(color);
                    } else {
                        openCarController(EXTRA_OPEN_SETTING_VALUE_VC, OPEN_VC_TYPE_CARLIGHT,TAG);
                        getMessageWithTtsSpeak(ATMOSPHERELAMPC4CONDITION, R.string.atmospherelampC4);
                        Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp_color, R.string.object_atmospherelamp_color,ATMOSPHERELAMPC4CONDITION,R.string.condition_atmospherelamp_color2);
                    }
                } else if (LAMPLIGHTPLUS.equals(nameValue) || LAMPLIGHTPLUS_1.equals(nameValue)) {
                    //亮一点
                    int currentLampLight = getLampLight();
                    currentLampLight += 1;
                    if (currentLampLight > 10) {
                        currentLampLight = 10;
                    }
                    int color = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_COLOR,64);
                    setLampParams(VEHICLE.ON,currentLampLight,color);
                    checkLampLightStatus(0,currentLampLight,LAMPLIGHTPLUS);
                } else if (LAMPLIGHTMINUS.equals(nameValue) || LAMPLIGHTPLUS_2.equals(nameValue)) {
                    //暗一点
                    int currentLampLight = getLampLight();
                    currentLampLight -= 1;
                    if (currentLampLight < 1) {
                        currentLampLight = 1;
                    }
                    int color = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_COLOR,64);
                    setLampParams(VEHICLE.ON,currentLampLight,color);
                    checkLampLightStatus(0,currentLampLight,LAMPLIGHTMINUS);
                }else if(null != nameValueObj){
                    try {
                        int offset = nameValueObj.getInt("offset");
                        int light = 5;
                        String replaceText = "";
                        if (offset < 1) {
                            light = 1;
                        }else if(offset > 10){
                            light = 10;
                        }else {
                            light = offset;
                        }

                        int color = SharedPreferencesUtils.getInt(mContext,AppConstant.KEY_LAMP_COLOR,64);
                        setLampParams(VEHICLE.ON,light,color);
                        checkLampLightStatus(offset,light,"");
                    }catch (Exception e){
                        Log.d(TAG,"nameValueObj exception: " + e);
                    }
                } else{
                    doExceptonAction(mContext);
                }
            }else {
                doExceptonAction(mContext);
            }
        } else if (TRUNK.equals(intentEntity.semantic.slots.name)) {//后备箱
//            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
//                boolean isAuto = isHaveTrunk();
//                if (isAuto) {
//                    checkTrunkExcption(type);
//                    //获取电动后备箱位置
//                    int status = getTrunkPos();
//                    if (status == 100) {
//                        //text = "后备箱已打开";
//                        getMessageWithTtsSpeak(BACKDOORC3CONDITION, R.string.backdoorC3);
//                        Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_backdoor, R.string.object_backdoor1,BACKDOORC3CONDITION,R.string.condition_backdoor3);
//                    } else {
//                        //text = "好的，正在打开后备箱";
//                        conditionId = BACKDOORC2CONDITION;
//                        defaultText = mContext.getString(R.string.backdoorC2);
//                        setTrunkStatus(VEHICLE.ON);
//                        checkTrunkMovementStatus(true);
//                        Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_backdoor, R.string.object_backdoor1,BACKDOORC2CONDITION,R.string.condition_backdoor2);
//                        return;
//                    }
//                } else {
//                    //text = "抱歉，你爱车的后备箱无法电动打开";
//                    getMessageWithTtsSpeak(BACKDOORC1CONDITION, R.string.backdoorC1);
//                    Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_backdoor, R.string.object_backdoor1,BACKDOORC1CONDITION,R.string.condition_backdoor1);
//                }
//            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
//                boolean isAuto = isHaveTrunk();
//                if (isAuto) {//有装载电动后备箱
//                    checkTrunkExcption(type);
//                    //获取电动后备箱位置:0-100；0:全关；100：全开
//                    int status = getTrunkPos();
//                    if (status == 0) { //电动后备箱完全关闭
//                        getMessageWithTtsSpeak(BACKDOORC6CONDITION, R.string.backdoorC6);
//                        Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_backdoor, R.string.object_backdoor2,BACKDOORC6CONDITION,R.string.condition_backdoor6);
//                    } else {
//                        conditionId = BACKDOORC5CONDITION;
//                        defaultText = mContext.getString(R.string.backdoorC5);
//                        setTrunkStatus(VEHICLE.OFF);
//                        checkTrunkMovementStatus(false);
//                        Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_backdoor, R.string.object_backdoor2,BACKDOORC5CONDITION,R.string.condition_backdoor5);
//                        return;
//                    }
//                } else {
//                    //无电动后备箱
//                    getMessageWithTtsSpeak(BACKDOORC4CONDITION, R.string.backdoorC4);
//                    Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_backdoor, R.string.object_backdoor2,BACKDOORC4CONDITION,R.string.condition_backdoor1);
//                }
//            }
//            getMessageWithTtsSpeak(BACKDOORC4CONDITION, R.string.backdoorC4);
//            Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_backdoor, R.string.object_backdoor2,BACKDOORC4CONDITION,R.string.condition_backdoor1);
            doExceptonAction(mContext);
        } else if (WINDOW_ALL.equals(intentEntity.semantic.slots.name)
                || WINDOW_LEFT_FRONT.equals(intentEntity.semantic.slots.name)
                || WINDOW_RIGHT_FRONT.equals(intentEntity.semantic.slots.name)
                || WINDOW_LEFT_BACK.equals(intentEntity.semantic.slots.name)
                || WINDOW_RIGHT_BACK.equals(intentEntity.semantic.slots.name)
                || WINDOW_LEFT_FRONT2.equals(intentEntity.semantic.slots.name)
                || WINDOW_RIGHT_FRONT2.equals(intentEntity.semantic.slots.name)
                || WINDOW_BACK.equals(intentEntity.semantic.slots.name)) {
            //左前车窗
            windowControl(intentEntity,nameValue);
        } else if (WINDOW.equals(intentEntity.semantic.slots.name)) {
            //车窗
            if (SeoptManager.getInstance().seopt_direction.
                    equals(libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT)) {
                //TODO 调用打开副驾车窗接口
                intentEntity.semantic.slots.name = WINDOW_RIGHT_FRONT;
            } else {
                //TODO 调用打开主驾车窗接口
                intentEntity.semantic.slots.name = WINDOW_LEFT_FRONT;
            }
            windowControl(intentEntity,nameValue);
        } else if (WIPER.equals(intentEntity.semantic.slots.name)) {//雨刮器
            //电源未处于ON档
//            Log.d(TAG, "type = " + type);
//            if (type == CarSensorEvent.IGNITION_STATE_OFF) {
//                getMessageWithTtsSpeak(WIPERC4CONDITION, R.string.wiper_c4);
//                Utils.eventTrack(mContext, R.string.skill_wiper, R.string.scene_excption, R.string.object_wiper_exception,WIPERC4CONDITION,R.string.condition_wiper4);
//                return;
//            }
            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                //text = "雨刮打开了";
                conditionId = WIPERC1CONDITION;
                defaultText = mContext.getString(R.string.wiperC1);
                //打开前雨刮：HU发送CAN信号HU_WiperSpReq，并取值0x3:-低速档，
                setWindowPiper(VEHICLE.WIPER_LOW);
                checkWiperSettingStatus(true,R.string.skill_wiper, R.string.scene_wiper, R.string.object_wiper1,R.string.condition_default);
            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                //text = "好的，雨刮关闭了";
                conditionId = WIPERC2CONDITION;
                defaultText = mContext.getString(R.string.wiperC2);
                setWindowPiper(VEHICLE.WIPER_OFF);
                checkWiperSettingStatus(false,R.string.skill_wiper, R.string.scene_wiper, R.string.object_wiper2,R.string.condition_default);
            }else {
                doExceptonAction(mContext);
            }
        } else if (PARK_AUTO.equals(intentEntity.semantic.slots.name)) {
            //自动泊车
            //APA 配置 0 ：未配置，  1：配置
            int hasAPA = Utils.getInt(mContext, APA_TYPE, 0);
            Log.d(TAG, "hasAPA = " + hasAPA);
            if(hasAPA == 0){//没有APA
                String username = Settings.System.getString(mContext.getContentResolver(),"aware");
                if (TextUtils.isEmpty(username)) {
                    username = "我";
                }
                getMessageWithoutTtsSpeak(PARKINGC4_1CONDITION,mContext.getString(R.string.parkingC4_1),"#VOICENAME#",username,
                        R.string.skill_auto_park,R.string.scene_auto_park_app,R.string.object_apa_start,R.string.condition_parkingC4_1);
                return;
            }
            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                if (!isMinSpeed()) {//判断车速
                    getMessageWithoutTtsSpeak(PARKINGC2CONDITION,mContext.getString(R.string.parkingC2),"","",R.string.skill_auto_park,R.string.scene_auto_park_app,R.string.object_apa_start,R.string.condition_parkingC2);
                } else if (CarUtils.mApaOnStatus == 5) {//泊车系统有异常
                    getMessageWithoutTtsSpeak(PARKINGC4CONDITION,mContext.getString(R.string.parkingC4),"","",R.string.skill_auto_park,R.string.scene_auto_park_app,R.string.object_apa_start,R.string.condition_parkingC4);
                } else {//发送打开自动泊车的指令
                    openAPA(APA.ON);
                    ApaController.getInstance(mContext).startSpeakApa();//  TODO 临时方案补丁
                    //检查有没有成功打开
                    long startTime = System.currentTimeMillis();
                    ThreadPoolUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            while (true){
                                if((System.currentTimeMillis() - startTime) / 1000 <= 3 && CarUtils.mApaOnStatus == APA.ACTIVE_SEARCHING){
                                    Log.d(TAG,"open APA success");
                                    getMessageWithoutTtsSpeak(PARKINGC1CONDITION,mContext.getString(R.string.parkingC1),"","",R.string.skill_auto_park,R.string.scene_auto_park_app,R.string.object_apa_start,R.string.condition_parkingC1);
                                    break;
                                }else if((System.currentTimeMillis() - startTime) / 1000 > 3){
                                    Log.d(TAG,"open APA timeout");
                                    ApaController.getInstance(mContext).stopSpeakApa();//TODO 临时方案补丁
                                    getMessageWithoutTtsSpeak(PARKINGC3CONDITION,mContext.getString(R.string.parkingC3),"","",R.string.skill_auto_park,R.string.scene_auto_park_app,R.string.object_apa_start,R.string.condition_parkingC3);
                                    break;
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }else{
                doExceptonAction(mContext);
            }
        } else if (PANO_IMAGE.equals(intentEntity.semantic.slots.name)) {
            if(intentEntity.semantic.slots.mode != null && intentEntity.semantic.slots.mode.equals(PANO_IMAGE)){
                //全景影像
                if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                    int avmLineStatus = CarUtils.getInstance(mContext).getAvmLineStatus();
                    Log.d(TAG,"getAvmLineStatus = " + avmLineStatus);
                    if(avmLineStatus == 1){//全景线被拔出
                        getMessageWithTtsSpeak(PANORAMICC2CONDITION, R.string.panoramicC2);
                        Utils.eventTrack(mContext, R.string.skill_panoramic, R.string.scene_panoramic, R.string.object_panoramic,PANORAMICC2CONDITION,R.string.condition_panoramic2);
                    }else {
                        //打开
                        if (getAVMStatus() != AVM.AVM_ON) {
                            setAVMStatus(AVM.AVM_ON);
                        }
                        getMessageWithTtsSpeak(PANORAMICC1CONDITION, R.string.panoramicC1);
                        Utils.eventTrack(mContext, R.string.skill_panoramic, R.string.scene_panoramic, R.string.object_panoramic,PANORAMICC1CONDITION,R.string.condition_panoramic1);
                    }
                }
                else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                    //关闭 ICA打开关闭都发ON，AVM自己去取反
                    if (getAVMStatus() != AVM.AVM_OFF) {
                        setAVMStatus(AVM.AVM_ON);
                    }
                    getMessageWithTtsSpeak(PANORAMICC5CONDITION, R.string.panoramicC5);
                }
                else {
                    doExceptonAction(mContext);
                }
            }else if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)){
                if("右".equals(intentEntity.semantic.slots.direction)){
                    CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLRIGHT);
                    getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
                }else if("左".equals(intentEntity.semantic.slots.direction)){
                    CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLLEFT);
                    getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
                }else if("前".equals(intentEntity.semantic.slots.direction)){
                    CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLFRONT);
                    getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
                }else if("后".equals(intentEntity.semantic.slots.direction)) {
                    CarUtils.getInstance(mContext).setAVMVisualAngle(VehicleDisplayForm.ALLREAR);
                    getMessageWithTtsSpeak(AVMCUSTOM, R.string.avm_custom);
                }else
                    doExceptonAction(mContext);
            } else {
                doExceptonAction(mContext);
            }

        } else if (PRESSURE_MONITOR.equals(intentEntity.semantic.slots.name)) {
            //胎压监测
            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                //text = "好的，正在打开";
                openPressureController(OPEN_CT_TYPE_PRESSURE);
                getMessageWithTtsSpeak(TPMC1CONDITION, R.string.tpmC1);
                Utils.eventTrack(mContext, R.string.skill_tpm, R.string.scene_tpm, R.string.object_tpm1,TPMC1CONDITION,R.string.condition_default);
            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                //text = "已关闭";
                gotoHome();
                getMessageWithTtsSpeak(TPMC2CONDITION, R.string.tpmC2);
                Utils.eventTrack(mContext, R.string.skill_tpm, R.string.scene_tpm, R.string.object_tpm2,TPMC2CONDITION,R.string.condition_default);
            } else {
                doExceptonAction(mContext);
            }
        } else if (SLIDE_DOOR_LEFT.equals(intentEntity.semantic.slots.name) ||
                SLIDE_DOOR_RIGHT.equals(intentEntity.semantic.slots.name) ||
                SLIDE_DOOR_ALL.equals(intentEntity.semantic.slots.name) ||
                SLIDE_DOOR_CENTER.equals(intentEntity.semantic.slots.name)) {
            //左侧滑门 右侧滑门 中滑门 所有滑门
            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                getMessageWithTtsSpeak(SLIDINGDOORC1CONDITION, R.string.slidingdoorC1);
                Utils.eventTrack(mContext, R.string.skill_slidingdoor, R.string.scene_slidingdoor, R.string.object_slidingdoor1,SLIDINGDOORC1CONDITION,R.string.condition_default);
            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                getMessageWithTtsSpeak(SLIDINGDOORC2CONDITION, R.string.slidingdoorC2);
                Utils.eventTrack(mContext, R.string.skill_slidingdoor, R.string.scene_slidingdoor, R.string.object_slidingdoor2,SLIDINGDOORC2CONDITION,R.string.condition_default);
            } else {
                doExceptonAction(mContext);
            }
        } else if (FRONT_SHROUD.equals(intentEntity.semantic.slots.name)) {
            //前罩
            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                //text = "抱歉，这个操作还不支持";
                //text = "抱歉，我没办法打开前罩";
                Random random = new Random();
                int n = random.nextInt(2);
                String[] resTexts = mContext.getResources().getStringArray(R.array.frontcoverC1);
                if (n == 0) {
                    defaultText = resTexts[0];
                } else {
                    defaultText = resTexts[1];
                }
                getMessageWithTtsSpeak(FRONTCOVERC1CONDITION, defaultText);
                Utils.eventTrack(mContext,R.string.skill_frontcover, R.string.scene_frontcover, R.string.object_frontcover1,FRONTCOVERC1CONDITION,R.string.condition_default);
            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                //text = "抱歉，这个操作还不支持";
                //text = "抱歉，我没办法打开前罩";
                Random random = new Random();
                int n = random.nextInt(2);
                String[] resTexts = mContext.getResources().getStringArray(R.array.frontcoverC2);
                if (n == 0) {
                    defaultText = resTexts[0];
                } else {
                    defaultText = resTexts[1];
                }
                getMessageWithTtsSpeak(FRONTCOVERC2CONDITION, defaultText);
                Utils.eventTrack(mContext,R.string.skill_frontcover, R.string.scene_frontcover, R.string.object_frontcover2,FRONTCOVERC2CONDITION,R.string.condition_default);
            } else {
                doExceptonAction(mContext);
            }
        } else if (CAR_SETTING.equals(intentEntity.semantic.slots.name)) {
            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
                //打开车控-车辆页
                defaultText = mContext.getString(R.string.carsettingC1);
                openCarController(EXTRA_OPEN_SETTING_VALUE_VC, OPEN_VC_TYPE_CARSETTINGS,TAG);
                getMessageWithTtsSpeak(CARSETTINGC1CONDITION,defaultText);
                Utils.eventTrack(mContext,R.string.skill_car_setting, R.string.scene_car_setting, R.string.object_car_setting1,CARSETTINGC1CONDITION,R.string.condition_default);
            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                defaultText = mContext.getString(R.string.carsettingC2);
                closeCarController();
                getMessageWithTtsSpeak(CARSETTINGC2CONDITION,defaultText);
                Utils.eventTrack(mContext,R.string.skill_car_setting, R.string.scene_car_setting, R.string.object_car_setting2,CARSETTINGC2CONDITION,R.string.condition_default);
            } else {
                doExceptonAction(mContext);
            }
        }else if(SET.equals(intentEntity.operation)){
            if(intentEntity.semantic!=null&&intentEntity.semantic.slots!=null&&intentEntity.semantic.slots.mode!=null&&intentEntity.semantic.slots.name!=null){
                if(MODE_DRIVER.equals(intentEntity.semantic.slots.mode)){
                    if(DRIVING_MODE_ECO.equals(intentEntity.semantic.slots.name)
                            ||DRIVING_MODE_SPORT.equals(intentEntity.semantic.slots.name)
                            ||DRIVING_MODE_NOMARL.equals(intentEntity.semantic.slots.name)
                            ||(DRIVING_MODE_CUS.equals(intentEntity.semantic.slots.name))){
                        handldeDriverMode(intentEntity.semantic.slots.name);
                    }else
                        doExceptonAction(mContext);
                }else
                    doExceptonAction(mContext);
            }else
                doExceptonAction(mContext);
        } else {
            doExceptonAction(mContext);
        }
    }

    /**
     * 处理驾驶模式
     * @param name
     */
    private void handldeDriverMode(String name) {
        Log.d(TAG, "handldeDriverMode() called with: name = [" + name + "]");
        int drvieMode = mControlManager.getCurrentDrvieMode();
        int type = getACStatus();
//        String carType = getCarType();
//        Log.d(TAG,"power type = " + type+"...drvieMode:"+drvieMode+".."+carType);

       /* if(!AppConstant.DCT_LEV5.equals(carType)){
            doExceptonAction(mContext);
            return;
        }*/

        if (type <= CarSensorEvent.IGNITION_STATE_OFF){
            getMessageWithoutTtsSpeak(DRIVRINGC3CONDITION,mContext.getString(R.string.driving_mode_c3),"",name,R.string.skill_driving_mode,R.string.scene_driving_mode,R.string.object_driving_mode,R.string.condition_driving_c3);
        }else {
            if((DRIVING_MODE_ECO.equals(name)&&drvieMode==DRIVE_MODE_ECO)   //当前处于经济模式
                ||(DRIVING_MODE_SPORT.equals(name)&&drvieMode==DRIVE_MODE_SPORT) //当前处于运动模式
                ||(DRIVING_MODE_NOMARL.equals(name)&&drvieMode==DRIVE_MODE_NOMARL)
                 ||(DRIVING_MODE_CUS.equals(name)&&drvieMode==DRIVE_MODE_CUST)){//当前处于舒适模式
                getMessageWithoutTtsSpeak(DRIVRINGC1CONDITION,mContext.getString(R.string.driving_mode_c1),"#MODEL#",name,R.string.skill_driving_mode,R.string.scene_driving_mode,R.string.object_driving_mode,R.string.condition_driving_c1);
                return;
            }else if(DRIVING_MODE_ECO.equals(name))
                mControlManager.setDrvieMode(DRIVE_MODE_ECO);
            else  if(DRIVING_MODE_SPORT.equals(name))
                mControlManager.setDrvieMode(DRIVE_MODE_SPORT);
            else  if(DRIVING_MODE_NOMARL.equals(name))
                mControlManager.setDrvieMode(DRIVE_MODE_NOMARL);
            else  if(DRIVING_MODE_CUS.equals(name))
                mControlManager.setDrvieMode(DRIVE_MODE_CUST);
            else {
                doExceptonAction(mContext);
                return;
            }
            getMessageWithoutTtsSpeak(DRIVRINGC2CONDITION,mContext.getString(R.string.driving_mode_c2),"#MODEL#",name,R.string.skill_driving_mode,R.string.scene_driving_mode,R.string.object_driving_mode,R.string.condition_driving_c2);
        }
    }

    //手势识别调用
    public void switchDriverMode(int mode){
        Log.d(TAG, "switchDriverMode() called with: mode = [" + mode + "]");
        int type = getACStatus();
        if (type <= CarSensorEvent.IGNITION_STATE_OFF){
            Log.e(TAG, "switchDriverMode: type：："+type);
            return;
        }
        if (mControlManager != null) {
            mControlManager.setDrvieMode(mode);
        }
    }

    //打开胎压监测
    public void openPressureController(int ctType) {
        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME_NAVI);
        intent.setAction(OPEN_VC_FROM_OTHERS);
        intent.putExtra(EXTRA_KEY_OPEN_VC, EXTRA_OPEN_SETTING_VALUE_CT);
        intent.putExtra(EXTRA_KEY_OPEN_CT_TYPE, ctType);
        mContext.sendBroadcast(intent);
        Log.d(TAG, "lh:open pressure:" + EXTRA_OPEN_SETTING_VALUE_CT + ",ct_type:" + ctType);
    }

    public void openCarController(String value, int type, String tag) {

        Intent intent = new Intent();
        intent.setPackage(PACKAGE_NAME_NAVI);
        intent.setAction(OPEN_VC_FROM_OTHERS);
        intent.putExtra(EXTRA_KEY_OPEN_VC, value);
        intent.putExtra(EXTRA_KEY_OPEN_VC_TYPE, type);
        mContext.sendBroadcast(intent);
        Log.d(tag, "lh:openCarController:" + value);
    }

    private void closeCarController() {
        MXSdkManager.getInstance(mContext).backToMap(null);
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    /**
     * 控制天窗
     *
     * @param intentEntity
     */
    private void sunroofControl(IntentEntity intentEntity,String nameValue) {
        if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation) && !(intentEntity.text).contains(SKYLIGHT_OPEN) &&
                !(intentEntity.text).contains(SKYLIGHT_OPEN2) && !(intentEntity.text).contains(SKYLIGHT_OPEN3)) {//打开天窗
            openSunroof(intentEntity);
        } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {//关闭天窗
            closeSunroof(intentEntity);
        } else {//天窗开一半，天窗透气
            openSunroofVentilation(intentEntity,nameValue);
        }
    }

    public void openSunroofVentilation(IntentEntity intentEntity,String nameValue) {
        boolean sunroofStatus = checkSunroofRunningStatus();
        if(!sunroofStatus){
            //天窗位置
            int topWindowPosition = getSunroofPosition();
            //天窗状态
            int mTopWindowStatus = getSunroofStatus();
            String action = intentEntity.semantic.slots.action;
            if (!TextUtils.isEmpty(action) && PlatformConstant.Operation.OPEN.equals(action) && !TextUtils.isEmpty(nameValue) && !nameValue.equals("LITTLE")) { //天窗打开几分之几
                int value = 100;
                if (nameValue.contains("一半") || nameValue.contains("二分之一")) {
                    value = 50;
                } else if (nameValue.contains("三分之一")) {
                    value = 33;
                } else if (nameValue.contains("四分之一")) {
                    value = 25;
                } else if (nameValue.contains("三分之二")) {
                    value = 66;
                } else if (nameValue.contains("四分之三")) {
                    value = 75;
                }

                Log.d(TAG, "天窗当前的位置topWindowPosition = " + topWindowPosition + ",天窗目标位置value = " + value);
                if (Math.abs(topWindowPosition - value) <= 3) { //车窗处于指定位置
                    conditionId = SKYLIGHTC13CONDITION;
                    defaultText = String.format(mContext.getString(R.string.skylight_c13), nameValue);
                    Utils.getTtsMessage(mContext,conditionId,defaultText,nameValue,true,R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off2,R.string.condition_skylight13,false);
                } else if (mTopWindowStatus == VEHICLE.SUNROOF_FULLY_UP || mTopWindowStatus == VEHICLE.SUNROOF_TILT_STOP || topWindowPosition == 0) { //处于全关位置或全起翘，起翘段
//                    int rainingStatus = getRainingStatus();
//                    if (rainingStatus == 1 || rainingStatus == 2) {//检测到下雨
//                        Utils.getMessageWithTtsSpeak(mContext, SKYLIGHTC11CONDITION, mContext.getString(R.string.skylight_c11), new TTSController.OnTtsStoppedListener() {
//                            @Override
//                            public void onPlayStopped() {
//                                MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
//                            }
//                        });
//                        Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off2,SKYLIGHTC11CONDITION,R.string.condition_skylight11);
//                    } else if (rainingStatus == 0) {//未检测到下雨
                        Log.d(TAG, "lh:no rainning,open: " + nameValue);
                        conditionId = SKYLIGHTC10CONDITION;
                        defaultText = String.format(mContext.getString(R.string.skylight_c10), nameValue);
                        setSunroofPos(value);
                        checkSunroofMonitorStatus(true, nameValue,topWindowPosition, value,R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off2,R.string.condition_skylight10);
                    //}
                } else {//天窗已打开,但不处于指定位置
                    conditionId = TtsConstant.SKYLIGHTC12CONDITION;
                    defaultText = String.format(mContext.getString(R.string.skylight_c12), nameValue);
                    setSunroofPos(value);
                    checkSunroofMonitorStatus(true, nameValue, topWindowPosition,value,R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off2,R.string.condition_skylight12);
                }
            } else if((((intentEntity.text).contains(SKYLIGHT_OPEN) || (intentEntity.text).contains(SKYLIGHT_OPEN2) || (intentEntity.text).contains(SKYLIGHT_OPEN3)) &&
                    TextUtils.isEmpty(action)) || (PlatformConstant.Operation.OPEN.equals(action) && !TextUtils.isEmpty(nameValue) && nameValue.equals("LITTLE"))){ //天窗透气
                //天窗已处于透气位置
                if((Math.abs(topWindowPosition - 6) <= 5 && mTopWindowStatus == VEHICLE.SUNROOF_SLIDE_STOP) ||
                        mTopWindowStatus == VEHICLE.SUNROOF_FULLY_UP){
                    getMessageWithTtsSpeak(SKYLIGHTC1CONDITION, R.string.skylight_c1);
                    Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_mode, R.string.object_skylight_mode1,SKYLIGHTC1CONDITION,R.string.condition_skylight1);
                } else {
//                    int rainingStatus = getRainingStatus();
//                    if (rainingStatus == 1 || rainingStatus == 2) {//检测到下雨
//                        Utils.getMessageWithTtsSpeak(mContext, SKYLIGHTC3CONDITION, mContext.getString(R.string.skylight_c3), new TTSController.OnTtsStoppedListener() {
//                            @Override
//                            public void onPlayStopped() {
//                                MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
//                            }
//                        });
//                        Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_mode, R.string.object_skylight_mode1,SKYLIGHTC3CONDITION,R.string.condition_skylight3);
//                    } else if (rainingStatus == 0) {//未检测到下雨
                        conditionId = SKYLIGHTC2CONDITION;
                        defaultText = mContext.getString(R.string.skylight_c2);
                        //设置天窗透气：0x06 为透气模式
                        setSunroofPos(6);
                        checkSunroofMonitorStatus(false, "", topWindowPosition,6,R.string.skill_skylight, R.string.scene_skylight_mode, R.string.object_skylight_mode1,R.string.condition_skylight2);
                    //}
                }
            }else if((!TextUtils.isEmpty(intentEntity.semantic.slots.mode) && SMOKING.equals(intentEntity.semantic.slots.mode))){//抽烟模式
                //打开外循环
                AirController.getInstance(mContext).changeCircleModeDelayed(LOOP_OUTSIDE,TAG);

//                Log.d(TAG,"getAllWindowOpenStatus() = " + getAllWindowOpenStatus() + ",topWindowPosition = " + topWindowPosition + ",left1carWindowPos = " + CarUtils.left1carWindowPos);
//                if(!getAllWindowOpenStatus() && (topWindowPosition == 0) && (CarUtils.left1carWindowPos != 127)){
//                    setWindowControl(10, WINDOW_LEFT_FRONT, TAG);//设置主驾车窗透气
//
//                    getMessageWithTtsSpeak(SMOKEC1CONDITION, R.string.smokeC1);
//                    Utils.eventTrack(mContext, R.string.skill_smart_car_control, R.string.scene_smoke, R.string.object_smoke,SMOKEC1CONDITION,R.string.condition_smokeC1);
//                }else if(!getAllWindowOpenStatus() && (topWindowPosition == 0) && (CarUtils.left1carWindowPos == 127)){
//                    setSunroofPos(6);//设置天窗透气
//
//                    getMessageWithTtsSpeak(SMARTHOMEC2CONDITION, R.string.smokeC2);
//                    Utils.eventTrack(mContext, R.string.skill_smart_car_control, R.string.scene_smoke, R.string.object_smoke,SMOKEC2CONDITION,R.string.condition_smokeC2);
//                }else if(getAllWindowOpenStatus()){
//                    getMessageWithTtsSpeak(SMOKEC3CONDITION, R.string.smokeC3);
//                    Utils.eventTrack(mContext, R.string.skill_smart_car_control, R.string.scene_smoke, R.string.object_smoke,SMOKEC3CONDITION,R.string.condition_smokeC3);
//                }else{
//                    getMessageWithTtsSpeak(SMOKEC4CONDITION, R.string.smokeC4);
//                    Utils.eventTrack(mContext, R.string.skill_smart_car_control, R.string.scene_smoke, R.string.object_smoke,SMOKEC4CONDITION,R.string.condition_smokeC4);
//                }

                //0820需求,透气位置设置为20
                int driverWindowPos = getWindowPosition(WINDOW_LEFT_FRONT);
                Log.d(TAG, "driverWindowPos = " + driverWindowPos);
                if(driverWindowPos == 0 || driverWindowPos == 127){
                    setWindowControl(20, WINDOW_LEFT_FRONT, TAG);
                    checkWindowSettingStatusForSmoke(driverWindowPos);
                }else {
                    if(FloatViewManager.getInstance(mContext).isHide()){
                        showAssistant();

                        Message msg = new Message();
                        Map map = new HashMap();
                        map.put("conditionId",SMOKEC3CONDITION);
                        map.put("defaultText",mContext.getString(R.string.smokeC3));
                        map.put("appName1",R.string.skill_smart_car_control);
                        map.put("scene1",R.string.scene_smoke);
                        map.put("object1",R.string.object_smoke);
                        map.put("condition1",R.string.condition_smokeC3);

                        msg.what = MSG_SHOW_WORD;
                        msg.obj = map;
                        handler.sendMessageDelayed(msg,600);
                    }else {
                        getMessageWithTtsSpeak(SMOKEC3CONDITION, R.string.smokeC3);
                        Utils.eventTrack(mContext, R.string.skill_smart_car_control, R.string.scene_smoke, R.string.object_smoke,SMOKEC3CONDITION,R.string.condition_smokeC3);
                    }
                }
            } else{//异常
                doExceptonAction(mContext);
            }
        }
    }

    /*
    *   天窗的运动状态
    *   true运动中，false1未运动
     */
    private boolean checkSunroofRunningStatus() {
        //获取天窗运动状态
        int windowsRunning = getSunroofRunningStatus();
        if (windowsRunning == VEHICLE.SUNROOF_MOVEMENT_OPENING_TILT || windowsRunning == VEHICLE.SUNROOF_MOVEMENT_OPENING_SLIDE) {
            getMessageWithTtsSpeak(SKYLIGHTC23CONDITION, R.string.skylight_c23);
            Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_excption, R.string.object_skylight_exception2,SKYLIGHTC23CONDITION,R.string.condition_skylight23);
            Log.d(TAG, "lh：sunroof is openning,start excption:" + windowsRunning);
            return true;
        }else if (windowsRunning == VEHICLE.SUNROOF_MOVEMENT_CLOSING_TILT || windowsRunning == VEHICLE.SUNROOF_MOVEMENT_CLOSING_SLIDE) {
            getMessageWithTtsSpeak(SKYLIGHTC24CONDITION, R.string.skylight_c24);
            Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_excption, R.string.object_skylight_exception2,SKYLIGHTC24CONDITION,R.string.condition_skylight24);
            Log.d(TAG, "lh：sunroof is closing,start excption:" + windowsRunning);
            return true;
        }else {
            return false;
        }
    }

    private void openSunroof(IntentEntity intentEntity) {
        boolean sunroofStatus = checkSunroofRunningStatus();
        if(!sunroofStatus){
            if (!TextUtils.isEmpty(intentEntity.semantic.slots.name)) { //用户说法:打开天窗
                //获取天窗位置
                int windowsPos = getSunroofPosition();
                //天窗状态
                int mTopWindowStatus = getSunroofStatus();
                Log.d(TAG, "mTopWindowStatus = " + mTopWindowStatus +",windowsPos = " + windowsPos);
                if (mTopWindowStatus == VEHICLE.SUNROOF_FULLY_OPEN && (100 - windowsPos) <= 2) { //天窗已完全打开
                    getMessageWithTtsSpeak(SKYLIGHTC8CONDITION, R.string.skylight_c8);
                    Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off1,SKYLIGHTC8CONDITION,R.string.condition_skylight8);
                } else if (mTopWindowStatus == VEHICLE.SUNROOF_FULLY_UP || mTopWindowStatus == VEHICLE.SUNROOF_TILT_STOP || windowsPos == 0) { //天窗未打开
//                    int rainingStatus = getRainingStatus();
//                    if (rainingStatus == 1 || rainingStatus == 2) {//检测到下雨
//                        //保存语义,以便二次交互
//                        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
//                        getMessageWithTtsSpeak(SKYLIGHTC7CONDITION, R.string.skylight_c7);
//                        Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off1,SKYLIGHTC7CONDITION,R.string.condition_skylight7);
//                    } else if (rainingStatus == 0) { //未检测到下雨
                        conditionId = SKYLIGHTC6CONDITION;
                        defaultText = mContext.getString(R.string.skylight_c6);
                        setSunroofPos(100);
                        checkSunroofMonitorStatus(false, "", windowsPos,100,R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off1,R.string.condition_skylight6);
                    //}
                } else { //天窗部分打开
                    conditionId = SKYLIGHTC9CONDITION;
                    defaultText = mContext.getString(R.string.skylight_c9);
                    setSunroofPos(100);
                    checkSunroofMonitorStatus(false, "", windowsPos,100,R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off1,R.string.condition_skylight9);
                }
            }
        }
    }

    private void closeSunroof(IntentEntity intentEntity) {
        boolean sunroofStatus = checkSunroofRunningStatus();
        if(!sunroofStatus){
            //获取天窗位置
            int windowsPos = getSunroofPosition();
            //天窗状态
            int mTopWindowStatus = getSunroofStatus();
            Log.d(TAG, "天窗状态mTopWindowStatus = " + mTopWindowStatus +",天窗位置windowsPos = " + windowsPos);
            if (mTopWindowStatus == VEHICLE.SUNROOF_FULLY_CLOSE && windowsPos == 0) {
                if (intentEntity.text.contains("透气")) {//关闭天窗透气
                    getMessageWithTtsSpeak(SKYLIGHTC4CONDITION, R.string.skylight_c4);
                    Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_mode, R.string.object_skylight_mode2,SKYLIGHTC4CONDITION,R.string.condition_skylight4);
                } else {
                    getMessageWithTtsSpeak(SKYLIGHTC14CONDITION, R.string.skylight_c14);
                    Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off3,SKYLIGHTC14CONDITION,R.string.condition_skylight14);

                    //如果此时遮阳帘没关闭，再把遮阳帘关闭了
                    int abatPos = getAbatVentPos();
                    if(abatPos != 0){
                        setAbatVentStatus(0);
                    }
                }
            } else {
                setSunroofPos(0);
                if (intentEntity.text.contains("透气")) {//关闭天窗透气
                    conditionId = SKYLIGHTC5CONDITION;
                    defaultText = mContext.getString(R.string.skylight_c5);
                    appName = R.string.skill_skylight;
                    scene = R.string.scene_skylight_mode;
                    object = R.string.object_skylight_mode2;
                    condition = R.string.condition_skylight5;
                } else {
                    //如果此时遮阳帘没关闭，再把遮阳帘关闭了
//                    int abatPos = getAbatVentPos();
//                    if(abatPos != 0){//关闭遮阳帘
//                        Message msg = new Message();
//                        msg.what = CLOSE_SUNROOF;
//                        Map map = new HashMap();
//                        msg.obj = map;
//                        handler.sendMessageDelayed(msg, 300);
//                    }

                    conditionId = SKYLIGHTC15CONDITION;
                    defaultText = mContext.getString(R.string.skylight_c15);
                    appName = R.string.skill_skylight;
                    scene = R.string.scene_skylight_on_off;
                    object = R.string.object_skylight_on_off3;
                    condition = R.string.condition_skylight15;
                }
                checkSunroofMonitorStatus(false, "",windowsPos, 0,appName,scene,object,condition);
                Log.d(TAG, "lh:关闭天窗时读到的天窗位置windowsPos=" + windowsPos);
            }
        }
    }

    /**
     * 控制车窗
     *
     * @param intentEntity
     */
    private void windowControl(IntentEntity intentEntity,String nameValue) {
        String windowName = intentEntity.semantic.slots.name;
        if (windowName.contains("车窗")) {
            windowName = windowName.replace("车窗", "");
        }else if (windowName.contains("窗户")) {
            windowName = windowName.replace("窗户", "");
        }
        String name = intentEntity.semantic.slots.name;
        String mode = intentEntity.semantic.slots.mode;
        Log.d(TAG, "name = " + name + ",mode = " + mode);
        int windowsPos = getWindowPosition(name);
        Log.d(TAG, windowName + "位置为： " + windowsPos);
        if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
            if(TextUtils.isEmpty(mode)){
                if (windowsPos >= 95 && windowsPos <= 100) { //车窗处于全开状态
                    defaultText = String.format(mContext.getString(R.string.carwindowC1), windowName);
                    getMessageWithoutTtsSpeak(CARWINDOWC1CONDITION, defaultText, "#WINDOW#", windowName, R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_open,R.string.condition_carwindow1);
                } else if (windowsPos == 0) { //车窗处于关闭状态
//                    checkWindowLockStatus(name);
//                    int rainingStatus = getRainingStatus();
//                    if (rainingStatus == 1 || rainingStatus == 2) { //检测到下雨
//                        defaultText = String.format(mContext.getString(R.string.carwindowC4), windowName);
//                        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
//                        getMessageWithoutTtsSpeak(CARWINDOWC4CONDITION, defaultText, "#WINDOW#", windowName);
//                        Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_open,CARWINDOWC4CONDITION,R.string.condition_carwindow4);
//                    } else if (rainingStatus == 0) { //未检测到下雨
                        conditionId = CARWINDOWC3CONDITION;
                        defaultText = String.format(mContext.getString(R.string.carwindowC3), windowName);
                        setWindowControl(100, name, TAG);
                        checkWindowSettingStatus(windowsPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_open,R.string.condition_carwindow3);
                    //}
                } else { //车窗处于半开状态
                    //checkWindowLockStatus(name);
                    conditionId = CARWINDOWC2CONDITION;
                    defaultText = String.format(mContext.getString(R.string.carwindowC2), windowName);
                    setWindowControl(100, name, TAG);
                    checkWindowSettingStatus(windowsPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_open,R.string.condition_carwindow2);
                }
            }else {//打开车窗透气
                if (Math.abs(windowsPos - 10) <= 3) { //车窗处于透气位置
                    defaultText = String.format(mContext.getString(R.string.carwindowC9), windowName);
                    getMessageWithoutTtsSpeak(CARWINDOWC9CONDITION, defaultText, "#WINDOW#", windowName,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_touqi,R.string.condition_carwindow9);
                } else if (windowsPos == 0) { //处于全关位置
//                    checkWindowLockStatus(name);
//                    int rainingStatus = getRainingStatus();
//                    if (rainingStatus == 1 || rainingStatus == 2) { //检测到下雨
//                        defaultText = String.format(mContext.getString(R.string.carwindowC12), windowName);
//                        getMessageWithoutTtsSpeak(CARWINDOWC12CONDITION, defaultText, "#WINDOW#", windowName);
//                        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);//保存语意
//                        Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_touqi,CARWINDOWC12CONDITION,R.string.condition_carwindow12);
//                    } else if (rainingStatus == 0) { //未检测到下雨
                        conditionId = CARWINDOWC11CONDITION;
                        defaultText = String.format(mContext.getString(R.string.carwindowC11), windowName);
                        setWindowControl(10, name, TAG);
                        checkWindowSettingStatus(windowsPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_touqi,R.string.condition_carwindow11);
                    //}
                } else {//车窗处于非全全关位置
                    //checkWindowLockStatus(name);
                    conditionId = CARWINDOWC10CONDITION;
                    defaultText = String.format(mContext.getString(R.string.carwindowC10), windowName);
                    setWindowControl(10, name, TAG);
                    checkWindowSettingStatus(windowsPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_touqi,R.string.condition_carwindow10);
                }
            }
        } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
            if (windowsPos == 0) { //车窗完全关闭
                conditionId = CARWINDOWC7CONDITION;
                defaultText = String.format(mContext.getString(R.string.carwindowC7), windowName);
                getMessageWithoutTtsSpeak(CARWINDOWC7CONDITION, defaultText, "#WINDOW#", windowName,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_close,R.string.condition_carwindow7);
            } else {
                //checkWindowLockStatus(name);
                conditionId = CARWINDOWC8CONDITION;
                defaultText = String.format(mContext.getString(R.string.carwindowC8), windowName);
                setWindowControl(0, name, TAG);
                checkWindowSettingStatus(windowsPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_close,R.string.condition_carwindow8);
            }
        } else if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
            String action = intentEntity.semantic.slots.action;
            if (!TextUtils.isEmpty(action) && PlatformConstant.Operation.OPEN.equals(action)) { //打开车窗百分之几
                int value = 100;
                if (nameValue.contains("一半") || nameValue.contains("二分之一")) {
                    value = 50;
                } else if (nameValue.contains("三分之一")) {
                    value = 33;
                } else if (nameValue.contains("四分之一")) {
                    value = 25;
                } else if (nameValue.contains("三分之二")) {
                    value = 66;
                } else if (nameValue.contains("四分之三")) {
                    value = 75;
                }

                if (Math.abs(windowsPos -value) <= 3) { //车窗处于指定位置
                    conditionId = CARWINDOWC15CONDITION;
                    defaultText = String.format(mContext.getString(R.string.carwindowC15), windowName);
                    getMessageWithoutTtsSpeak(conditionId, defaultText, "#WINDOW#", windowName,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_percent,R.string.condition_carwindow15);
                } else if (windowsPos == 0) { //处于全关位置
//                    checkWindowLockStatus(name);
//                    int rainingStatus = getRainingStatus();
//                    if (rainingStatus == 1 || rainingStatus == 2) {//检测到下雨
//                        defaultText = String.format(mContext.getString(R.string.carwindowC18), windowName);
//                        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
//                        getMessageWithoutTtsSpeak(CARWINDOWC17CONDITION, defaultText, "#WINDOW#", windowName);
//                        Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_percent,CARWINDOWC17CONDITION,R.string.condition_carwindow17);
//                    } else if (rainingStatus == 0) {//未检测到下雨
                        conditionId = CARWINDOWC17CONDITION;
                        defaultText = String.format(mContext.getString(R.string.carwindowC17), windowName);
                        setWindowControl(value, name, TAG);
                        checkWindowSettingStatus(windowsPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_percent,R.string.condition_carwindow17);
                    //}
                } else {//车窗处于非指定位置
                    //checkWindowLockStatus(name);
                    conditionId = CARWINDOWC16CONDITION;
                    defaultText = String.format(mContext.getString(R.string.carwindowC16), windowName);
                    setWindowControl(value, name, TAG);
                    checkWindowSettingStatus(windowsPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_percent,R.string.condition_carwindow16);
                }
            }else {
                doExceptonAction(mContext);
            }
        } else {
            doExceptonAction(mContext);
        }
    }

    /**
     * 二次交互-场景: 检测到下雨,仍要打开车窗
     *
     * @param intentEntity
     */
//    private void confirmWindowControl(MultiSemantic intentEntity,String nameValue) {
//        String windowName = intentEntity.semantic.slots.name;
//        if (windowName.contains("车窗")) {
//            windowName = windowName.replace("车窗", "");
//        }else if (windowName.contains("窗户")) {
//            windowName = windowName.replace("窗户", "");
//        }
//        String name = intentEntity.semantic.slots.name;
//        String mode = intentEntity.semantic.slots.mode;
//        int windowPos  = getWindowPosition(name);
//        if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {//完全打开车窗
//            if(TextUtils.isEmpty(mode)){//打开车窗
//                //checkWindowLockStatus(name);
//                conditionId = CARWINDOWC5CONDITION;
//                defaultText = String.format(mContext.getString(R.string.carwindowC5), windowName);
//                setWindowControl(100, name, TAG);
//                checkWindowSettingStatus(windowPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_open,R.string.condition_carwindow5);
//            }else {//打开车窗透气
//                //checkWindowLockStatus(name);
//                conditionId = CARWINDOWC13CONDITION;
//                defaultText = String.format(mContext.getString(R.string.carwindowC13), windowName);
//                setWindowControl(10, name, TAG);
//                checkWindowSettingStatus(windowPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_touqi,R.string.condition_carwindow13);
//            }
//        } else if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
//            String action = intentEntity.semantic.slots.action;
//            //检测车窗锁止状态,车窗锁止ON时,不响应非主驾的车窗控制
//            //checkWindowLockStatus(name);
//            if (!TextUtils.isEmpty(action) && PlatformConstant.Operation.OPEN.equals(action)) { //打开车窗百分之几
//                int value = 100;
//                if (nameValue.contains("一半") || nameValue.contains("二分之一")) {
//                    value = 50;
//                } else if (nameValue.contains("三分之一")) {
//                    value = 33;
//                } else if (nameValue.contains("四分之一")) {
//                    value = 25;
//                } else if (nameValue.contains("三分之二")) {
//                    value = 66;
//                } else if (nameValue.contains("四分之三")) {
//                    value = 75;
//                }
//                conditionId = CARWINDOWC19CONDITION;
//                defaultText = String.format(mContext.getString(R.string.carwindowC19), windowName);
//                setWindowControl(value, name, TAG);
//                checkWindowSettingStatus(windowPos,name, true,R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_percent,R.string.condition_carwindow19);
//            }
//        }
//    }

    //用户说确定进行二次交互
//    public void confirmInteractive(MultiSemantic intentEntity) {
//        conditionId = "";
//        Log.d(TAG, "lh:second interval" + intentEntity.semantic.slots.name + ",operation:" + intentEntity.operation);
//        if (WINDOW_ALL.equals(intentEntity.semantic.slots.name)
//                || WINDOW_LEFT_FRONT.equals(intentEntity.semantic.slots.name)
//                || WINDOW_RIGHT_FRONT.equals(intentEntity.semantic.slots.name)
//                || WINDOW_LEFT_BACK.equals(intentEntity.semantic.slots.name)
//                || WINDOW_RIGHT_BACK.equals(intentEntity.semantic.slots.name)
//                || WINDOW_LEFT_FRONT2.equals(intentEntity.semantic.slots.name)
//                || WINDOW_RIGHT_FRONT2.equals(intentEntity.semantic.slots.name)) {
//            //车窗二次交互,包括打开车窗,车窗打开几分之几,车窗透气
//            confirmWindowControl(intentEntity,nameValue);
//        } else if (SKYLIGHT.equals(intentEntity.semantic.slots.name)) {
//            int initPos = getSunroofPosition();
//            //天窗二次交互,包括打开天窗,天窗打开几分之几,天窗透气
//            if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {//打开天窗
//                conditionId = SKYLIGHTC6CONDITION;
//                defaultText = mContext.getString(R.string.skylight_c6);
//                setSunroofPos(100);
//                checkSunroofMonitorStatus(false, "",initPos, 100,R.string.skill_skylight, R.string.scene_skylight_mode, R.string.object_skylight_mode2,R.string.condition_skylight6);
//            } else if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {//天窗开一半
//                String action = intentEntity.semantic.slots.action;
//                if (!TextUtils.isEmpty(action) && PlatformConstant.Operation.OPEN.equals(action)) { //打开天窗
//                    String nameValue = intentEntity.semantic.slots.nameValue;
//                    int value = 100;
//                    if (nameValue.contains("一半") || nameValue.contains("二分之一")) {
//                        value = 50;
//                    } else if (nameValue.contains("三分之一")) {
//                        value = 33;
//                    } else if (nameValue.contains("四分之一")) {
//                        value = 25;
//                    } else if (nameValue.contains("三分之二")) {
//                        value = 66;
//                    } else if (nameValue.contains("四分之三")) {
//                        value = 75;
//                    }
//                    conditionId = SKYLIGHTC12CONDITION;
//                    defaultText = String.format(mContext.getString(R.string.skylight_c12), nameValue);
//                    setSunroofPos(value);
//                    checkSunroofMonitorStatus(true, nameValue, initPos,value,R.string.skill_skylight, R.string.scene_skylight_on_off, R.string.object_skylight_on_off2,R.string.condition_skylight12);
//                } else { //天窗透氣
//                    //天窗状态
//                    conditionId = SKYLIGHTC2CONDITION;
//                    defaultText = mContext.getString(R.string.skylight_c2);
//                    //设置天窗透气：0x06 为透气模式
//                    setSunroofPos(6);
//                    checkSunroofMonitorStatus(false, "", initPos,6,R.string.skill_skylight, R.string.scene_skylight_mode, R.string.object_skylight_mode1,R.string.condition_skylight2);
//                }
//            }
//        }
//    }

    private void cancelWindowControl(MultiSemantic intentEntity) {
        String windowName = intentEntity.semantic.slots.name;
        if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {//完全打开车窗
            getMessageWithTtsSpeak(CARWINDOWC6CONDITION, R.string.carwindowC6);
            Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_open,CARWINDOWC6CONDITION,R.string.condition_carwindow6);
        } else if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
            String action = intentEntity.semantic.slots.action;
            if (!TextUtils.isEmpty(action) && PlatformConstant.Operation.OPEN.equals(action)) { //打开车窗百分之几
                getMessageWithTtsSpeak(CARWINDOWC20CONDITION, R.string.carwindowC20);
                Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_percent,CARWINDOWC20CONDITION,R.string.condition_carwindow20);
            } else { //打开车窗透气
                getMessageWithTtsSpeak(CARWINDOWC14CONDITION, R.string.carwindowC14);
                Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_carwindow, R.string.object_carwindow_touqi,CARWINDOWC14CONDITION,R.string.condition_carwindow14);
            }
        }

        //抽烟模式的二次交互
        getMessageWithTtsSpeak(MSGC81CONDITION,R.string.msg_c81);
    }

    //用户说取消,则取消该操作
    public void cancelInteractive(MultiSemantic intentEntity) {
        if (WINDOW_ALL.equals(intentEntity.semantic.slots.name)
                || WINDOW_LEFT_FRONT.equals(intentEntity.semantic.slots.name)
                || WINDOW_RIGHT_FRONT.equals(intentEntity.semantic.slots.name)
                || WINDOW_LEFT_BACK.equals(intentEntity.semantic.slots.name)
                || WINDOW_RIGHT_BACK.equals(intentEntity.semantic.slots.name)
                || WINDOW_LEFT_FRONT2.equals(intentEntity.semantic.slots.name)
                || WINDOW_RIGHT_FRONT2.equals(intentEntity.semantic.slots.name)) {
            //车窗二次交互,包括打开车窗,车窗打开几分之几,车窗透气
            cancelWindowControl(intentEntity);
        }
    }

    /**
     * 获取全景影像开关状态
     *
     * @return AVM.AVM_ON_ACK , AVM.AVM_OFF_ACK
     */
    public int getAVMStatus() {
        return CarUtils.avmStatus;
    }

    /**
     * 设置全景影像开关
     *
     * @param status AVM.AVM_OFF AVM.AVM_ON
     */
    public void setAVMStatus(int status) {
        try {
            AppConfig.INSTANCE.mCarCabinManager.setIntProperty
                    (CarCabinManager.ID_AVM_DISPLAY_SWITCH,
                            VEHICLE_AREA_TYPE_GLOBAL, status);
            Log.d(TAG, "lh:set AVM status:" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

//    //胎压监测
//    goToVcSetting(this,EXTRA_OPEN_SETTING_VALUE_CT,OPEN_CT_TYPE_PRESSURE);

    /**
     * 氛围灯是否打开
     *
     * @return
     */
    private boolean isLambOpen() {
        try {
            int status = AppConfig.INSTANCE.mCarCabinManager.
                    getIntProperty(ID_BODY_LIGHT_ATMO_ON, GLOBAL_ROW_1_LEFT);
            Log.d(TAG, "lh: is lamb open:" + status);
            if (status == LIGHT.ON) {
                return true;
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更改氛围灯状态
     */
    private void changeLampStatus(int status) {
        try {
            //todo: 氛围灯状态参数待考证.
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntProperty(ID_BODY_LIGHT_ATMO_ON, GLOBAL_ROW_1_LEFT, status);
            Log.d(TAG, "lh:change lamp status:" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    private int getColor(String colorName){
        int color = 64;
        if (colorName.contains("红色")) {
            color = 64;
        } else if (colorName.contains("橙色")) {
            color = 60;
        } else if (colorName.contains("黄色")) {
            color = 56;
        } else if (colorName.contains("绿色")) {
            color = 40;
        } else if (colorName.contains("紫色")) {
            color = 13;
        } else if (colorName.contains("蓝色")) {
            color = 20;
        } else if(colorName.contains("粉色")){
            color = 10;
        }
        return color;
    }

    /**
     * 更改氛围灯颜色,颜色值与车控保持一致
     * int[0]: 氛围灯开关\n  on
     * int[1]: 氛围灯亮度\n
     * int[2]: 氛围灯颜色\n
     * int[3]: 氛围灯记忆使能\n  on
     * int[4]: 语音氛围灯开关设置\n  on
     */
    private void setLampParams(int switcher,int light,int color) {
        int[] temp = new int[5] ;
        temp[0] = switcher;
        temp[1] = light;
        temp[2] = color;
        temp[3] = VEHICLE.ON;
        temp[4] = VEHICLE.ON;

        try {
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntArrayProperty(CarCabinManager.ID_BODY_LIGHT_ATMO_ALL, VEHICLE_AREA_TYPE_GLOBAL, temp);
            Log.d(TAG,"set===设置氛围灯控制==value==" + temp);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
            Log.d(TAG,"set===设置氛围灯控制Exception" + e);
        }
    }

    /**
     * 更改氛围灯亮度
     */
    private void setLampLight(int status) {
        try {
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntProperty(ID_BODY_LIGHT_ATMO_BRIGHT_LEVEL, GLOBAL_ROW_1_LEFT, status);
            Log.d(TAG, "lh:set lamp light:" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取氛围灯当前的亮度
     */
    private int getLampLight() {
        return CarUtils.lampLight;
    }

    private void checkSunroofMonitorStatus(boolean isReplace, String nameValue,int initPos, int pos, int appName, int scene, int object,int condition) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int status = getSunroofRunningStatus();
                int position = getSunroofPosition();
                Log.d(TAG, "初始位置initPos =" + initPos + ",当前位置position = " + position + ",目标位置pos = " + pos + ",天窗运动状态status =" + status);
                if (status == VEHICLE.SUNROOF_MOVEMENT_STOPPED && position == initPos) {
                    conditionId = TtsConstant.SKYLIGHTC27CONDITION;
                    defaultText = mContext.getString(R.string.skylight_c27);
                    Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_excption, R.string.object_exception_other,SKYLIGHTC27CONDITION,R.string.condition_skylight27);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                    Log.d(TAG, "天窗未响应");
                    return;
                }

                if (!TextUtils.isEmpty(conditionId)) {
                    if (isReplace) {
                        getMessageWithoutTtsSpeak(conditionId, defaultText, "#PERCENT#", nameValue, appName, scene, object,condition);
                    } else {
                        Utils.eventTrack(mContext, appName, scene, object,conditionId,condition);
                        getMessageWithTtsSpeak(conditionId, defaultText);
                    }
                }
            }
        }, 900);
    }

    private void checkLampColorStatus(int status) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "checkLampColorStatus: CarUtils.lampColor = " + CarUtils.lampColor);
                if(status == CarUtils.lampColor){
                    conditionId = TtsConstant.ATMOSPHERELAMPC3CONDITION;
                    defaultText = mContext.getString(R.string.atmospherelampC3);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp_color, R.string.object_atmospherelamp_color,ATMOSPHERELAMPC3CONDITION,R.string.condition_atmospherelamp_color1);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                }else {
                    conditionId = TtsConstant.ATMOSPHERELAMPC11CONDITION;
                    defaultText = mContext.getString(R.string.atmospherelampC11);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_excption, R.string.object_exception_other,ATMOSPHERELAMPC11CONDITION,R.string.condition_atmospherelampC11);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                }
            }
        }, 500);
    }

    private void checkLampSwitcherStatus(int switcher) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "checkLampSwitcherStatus: CarUtils.lampSwitch = " + CarUtils.lampSwitch);
                if(VEHICLE.ON == switcher && switcher == CarUtils.lampSwitch){//已打开
                    getMessageWithTtsSpeak(ATMOSPHEREAMPC1CONDITION, R.string.atmospherelampC1);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp, R.string.object_atmospherelamp1,ATMOSPHEREAMPC1CONDITION,R.string.condition_default);
                }else if(VEHICLE.OFF == switcher && switcher == CarUtils.lampSwitch){//已关闭
                    getMessageWithTtsSpeak(ATMOSPHERELAMPC2CONDITION, R.string.atmospherelampC2);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp, R.string.object_atmospherelamp2,ATMOSPHERELAMPC2CONDITION,R.string.condition_default);
                }else {//异常
                    conditionId = TtsConstant.ATMOSPHERELAMPC11CONDITION;
                    defaultText = mContext.getString(R.string.atmospherelampC11);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_excption, R.string.object_exception_other,ATMOSPHERELAMPC11CONDITION,R.string.condition_atmospherelampC11);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                }
            }
        }, 500);
    }

    private void checkLampLightStatus(int offset,int light,String nameValue) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "checkLampLightStatus: CarUtils.lampLight = " + CarUtils.lampLight);
                String replaceText = "";
                if(LAMPLIGHTPLUS.equals(nameValue) && light == CarUtils.lampLight){//增大亮度
                    Random random = new Random();
                    int n = random.nextInt(2);
                    String[] resTexts = mContext.getResources().getStringArray(R.array.atmospherelampC5);
                    if (n == 0) {
                        defaultText = resTexts[0];
                    } else {
                        defaultText = resTexts[1];
                    }
                    getMessageWithTtsSpeak(ATMOSPHERELAMPC5CONDITION, defaultText);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp_light, R.string.object_atmospherelamp_light1,ATMOSPHERELAMPC5CONDITION,R.string.condition_default);
                }else if(LAMPLIGHTMINUS.equals(nameValue) && light == CarUtils.lampLight){//减小亮度
                    Random random = new Random();
                    int n = random.nextInt(2);
                    String[] resTexts = mContext.getResources().getStringArray(R.array.atmospherelampC6);
                    if (n == 0) {
                        defaultText = resTexts[0];
                    } else {
                        defaultText = resTexts[1];
                    }
                    getMessageWithTtsSpeak(ATMOSPHERELAMPC6CONDITION, defaultText);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_atmospherelamp_light, R.string.object_atmospherelamp_light2,ATMOSPHERELAMPC6CONDITION,R.string.condition_default);
                }else if("".equals(nameValue) && light == CarUtils.lampLight){//亮度设为X
                    if (offset < 1) {
                        conditionId = ATMOSPHERELAMPC5_3CONDITION;
                        defaultText = mContext.getString(R.string.atmospherelampC5_3);
                        replaceText = "#MINNUM#";
                        appName = R.string.skill_atmospherelamp;
                        scene = R.string.scene_atmospherelamp_light;
                        object = R.string.object_atmospherelamp_light3;
                        condition = R.string.condition_lamp_light_min;
                    }else if(offset > 10){
                        conditionId = ATMOSPHERELAMPC5_1CONDITION;
                        defaultText = mContext.getString(R.string.atmospherelampC5_1);
                        replaceText = "#MAXNUM#";
                        appName = R.string.skill_atmospherelamp;
                        scene = R.string.scene_atmospherelamp_light;
                        object = R.string.object_atmospherelamp_light3;
                        condition = R.string.condition_lamp_light_max;
                    }else {
                        conditionId = ATMOSPHERELAMPC5_2CONDITION;
                        defaultText = mContext.getString(R.string.atmospherelampC5_2);
                        replaceText = "";
                        appName = R.string.skill_atmospherelamp;
                        scene = R.string.scene_atmospherelamp_light;
                        object = R.string.object_atmospherelamp_light3;
                        condition = R.string.condition_lamp_light_normal;
                    }
                    getMessageWithoutTtsSpeak(conditionId, defaultText, replaceText, light+"",appName,scene,object,condition);
                } else {//异常
                    conditionId = TtsConstant.ATMOSPHERELAMPC11CONDITION;
                    defaultText = mContext.getString(R.string.atmospherelampC11);
                    Utils.eventTrack(mContext, R.string.skill_atmospherelamp, R.string.scene_excption, R.string.object_exception_other,ATMOSPHERELAMPC11CONDITION,R.string.condition_atmospherelampC11);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                }
            }
        }, 500);
    }

    /**
     * 获取天窗电机状态
     *
     * @return AVM.AVM_ON_ACK , AVM.AVM_OFF_ACK
     */
//    public int getSunroofMonitorStatus() {
////        int windowsStatus = -1;
////        try {
////            windowsStatus = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(CarCabinManager.ID_BODY_WINDOW_SUNROOF_MOTOR_STATE,
////                    VEHICLE_AREA_TYPE_GLOBAL);
////            Log.d(TAG, "lh:get sunroof monitor status:(default:-1):" + windowsStatus);
////
////        } catch (CarNotConnectedException e) {
////            e.printStackTrace();
////        }
//        Log.d(TAG, "lh:天窗电机状态windowsPowerStatus=" + windowsPowerStatus);
//        return windowsPowerStatus;
//    }


    /**
     * 获取天窗运动状态
     *
     * @return AVM.AVM_ON_ACK , AVM.AVM_OFF_ACK
     */
    public int getSunroofRunningStatus() {
//        int windowsStatus = -1;
//        try {
//            windowsStatus = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_STATE,
//                    WINDOW_ROOF_TOP_1);
//            Log.d(TAG, "lh:获取天窗运动状态windowsStatus = " + windowsStatus);
//
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
//        return windowsStatus;
        Log.d(TAG, "读取的天窗运动状态为" + CarUtils.topWindowRunningStatus);
        return CarUtils.topWindowRunningStatus;
    }

    /**
     * 设置天窗状态
     *
     * @return AVM.AVM_ON_ACK , AVM.AVM_OFF_ACK
     */
    public void setSunroofStatus(boolean isOpen) {
        Log.d(TAG, "lh:set sunroof status:" + isOpen);
        int[] int_open = new int[2];
        int_open[0] = 0x1;
        int_open[1] = 0x2;

        int[] int_close = new int[2];
        int_close[0] = 0x1;
        int_close[1] = 0x1;

        try {
            //WINDOW_ROOF_TOP_1 表示天窗;WINDOW_ROOF_TOP_2;表示遮阳帘
            AppConfig.INSTANCE.mCarCabinManager.setIntArrayProperty(CarCabinManager.ID_BODY_SUNROOF_SUNVISOR_ON_OFF, WINDOW_ROOF_TOP_1, isOpen ? int_open : int_close);
            Log.d(TAG, "lh:set sunroof status:" + (isOpen ? int_open : int_close));
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取天窗位置
     */
    public int getSunroofPosition() {
        /*int windowsStatus = -1;
        try {
            windowsStatus = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(CarCabinManager.ID_BODY_WINDOW_SUNROOF_CONTROL_POS,
                    VEHICLE_AREA_TYPE_GLOBAL);
            Log.d(TAG, "lh:获取的天窗位置:windowsStatus=" + windowsStatus);

        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return windowsStatus;*/
        Log.d(TAG, "lh:获取的天窗位置:topWindowPosition=" + CarUtils.topWindowPosition);
        return CarUtils.topWindowPosition;
    }

    /**
     * 获取天窗状态
     *
     * @return AVM.AVM_ON_ACK , AVM.AVM_OFF_ACK
     */
    public int getSunroofStatus() {
//        int windowsStatus = -1;
//        try {
//            windowsStatus = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(CarCabinManager.ID_BODY_WINDOW_SUNROOF_SUNVISOR_STATUS,
//                    WINDOW_ROOF_TOP_1);
//            Log.d(TAG, "lh:get sunroof status(default:-1):" + windowsStatus);
//
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG, "读取的天窗状态为" + CarUtils.topWindowStatus);
        return CarUtils.topWindowStatus;
    }

    /**
     * 设置遮阳帘状态
     *
     * @return AVM.AVM_ON_ACK , AVM.AVM_OFF_ACK
     */
    public void setAbatVentStatus(int value) {
        try {
            //WINDOW_ROOF_TOP_1 表示天窗;WINDOW_ROOF_TOP_2;表示遮阳帘
            AppConfig.INSTANCE.mCarCabinManager.setIntProperty(CarCabinManager.ID_BODY_WINDOW_SUNSHADE_POS, VEHICLE_AREA_TYPE_GLOBAL, value);
            //AppConfig.INSTANCE.mCarCabinManager.setIntProperty(CarCabinManager.ID_BODY_WINDOW_SUNSHADE_POS, WINDOW_ROOF_TOP_1, value);
            Log.d(TAG, "lh:设置遮阳帘的打开值:" + value);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    //检查后备箱异常,包括发动机是否启动,车辆是否静止,后备箱是否处于运动中(正在打开/正在关闭)
    private void checkTrunkExcption(int type) {
        //电源未处于ON档
        if (type == CarSensorEvent.IGNITION_STATE_OFF) {
            getMessageWithTtsSpeak(BACKDOORC7CONDITION, R.string.backdoor_c7);
            Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_excption, R.string.object_backdoor_exception,BACKDOORC7CONDITION,R.string.condition_backdoor_exception1);
            return;
        }
        float speed = CarUtils.getInstance(mContext).getSpeed();
        if (speed > 0) {
            getMessageWithTtsSpeak(BACKDOORC8CONDITION, R.string.backdoor_c8);
            Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_excption, R.string.object_backdoor_exception,BACKDOORC8CONDITION,R.string.condition_backdoor_exception2);
            return;
        }
        int status = getTrunkRunningStatus();
        if (status == 3) {//电动北门正在开启中
            getMessageWithTtsSpeak(BACKDOORC9CONDITION, R.string.backdoorC9);
            Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_excption, R.string.object_backdoor_exception_running,BACKDOORC9CONDITION,R.string.condition_backdoor9);
            return;
        } else if (status == 4) { //电动北门正在关闭中;
            getMessageWithTtsSpeak(BACKDOORC10CONDITION, R.string.backdoorC10);
            Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_excption, R.string.object_backdoor_exception_running,BACKDOORC10CONDITION,R.string.condition_backdoor10);
            return;
        }
    }

    //检查遮阳帘运动状态,是否处于 正在开启/正在关闭 中
    //return true 关闭中 或 开启中， false未运动
    private boolean checkAbatVentRunning() {
        int status = getAbatVentRunningStatus();
        if (status == VEHICLE.SUNSHADE_OPENING_SLIDE) {//遮阳帘正在开启中
            getMessageWithTtsSpeak(SKYLIGHTC25CONDITION, R.string.skylight_c25);
            Utils.eventTrack(mContext,R.string.skill_skylight, R.string.scene_excption, R.string.object_skylight_exception3,SKYLIGHTC25CONDITION,R.string.condition_skylight25);
            return true;
        } else if (status == VEHICLE.SUNSHADE_CLOSING_SLIDE) { //遮阳帘正在关闭中;
            getMessageWithTtsSpeak(SKYLIGHTC26CONDITION, R.string.skylight_c26);
            Utils.eventTrack(mContext,R.string.skill_skylight, R.string.scene_excption, R.string.object_skylight_exception3,SKYLIGHTC26CONDITION,R.string.condition_skylight26);
            return true;
        }else{
            return false;
        }
    }

    //检查遮阳帘指令下发是否成功
    private void checkAbatVentSettingStatus(boolean isOpen,int initPos,int appName, int scene, int object, int condition) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int status = getAbatVentRunningStatus();
                int pos = getAbatVentPos();
                Log.d(TAG, "遮阳帘isOpen = " + isOpen + "当前位置pos = " + pos + ",初始位置initPos =" + initPos + ",运动状态status = " + status);
                if (isOpen) {
                    if (status != VEHICLE.SUNSHADE_OPENING_SLIDE) {//遮阳帘未处于运动状态
                        if (pos == initPos) {
                            conditionId = SKYLIGHTC28CONDITION;
                            defaultText = mContext.getString(R.string.skylight_c28);
                            Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_excption, R.string.object_exception_other, SKYLIGHTC28CONDITION, R.string.condition_skylight28);
                            getMessageWithTtsSpeak(conditionId, defaultText);
                            return;
                        }
                    }
                } else {
                    if (status != VEHICLE.SUNSHADE_CLOSING_SLIDE) { //遮阳帘未处于运动状态
                        if (pos == initPos) {
                            conditionId = SKYLIGHTC28CONDITION;
                            defaultText = mContext.getString(R.string.skylight_c28);
                            Utils.eventTrack(mContext, R.string.skill_skylight, R.string.scene_excption, R.string.object_exception_other, SKYLIGHTC28CONDITION, R.string.condition_skylight28);
                            getMessageWithTtsSpeak(conditionId, defaultText);
                            return;
                        }
                    }
                }
                if (!TextUtils.isEmpty(conditionId)) {
                    Utils.eventTrack(mContext, appName,scene,object, conditionId,condition);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                }
            }
        }, 900);
    }

    /**
     * 获取遮阳帘运动状态
     *
     * @return VEHICLE.SUNROOF_MOVEMENT_
     * 0x1: opening slide，表明遮阳帘正在开启中，此时判定遮阳帘正在处于开启状态；
     * 0x2: closing slide ，表明遮阳帘正在关闭中，此时判定遮阳帘正在处于关闭状态；
     * 0x0: stopped，表明遮阳帘正在静止中。
     */
    public int getAbatVentRunningStatus() {
//        int status = -1;
//        try {
//            status = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(ID_BODY_WINDOW_SUNSHADE_STATE,
//                    GLOBAL_ROW_1_LEFT);
//            Log.d(TAG, "获取遮阳帘运动状态：status=" + status);
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG, "获取遮阳帘运动状态topSunShadeStatus = " + CarUtils.topSunShadeStatus);
        return CarUtils.topSunShadeStatus;
    }

    /**
     * 获取遮阳帘位置
     * 100--则判定遮阳帘处于全开位置，
     * 0--则判断遮阳帘处于全关位置。
     */
    public int getAbatVentPos() {
        //int status = -1;
//        try {
//            topSunshadePosition = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(ID_BODY_WINDOW_SUNSHADE_POS,
//                    VEHICLE_AREA_TYPE_GLOBAL);
//            Log.d(TAG, "lh:读取遮阳帘的位置status=" + topSunshadePosition);
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG, "获取遮阳帘位置topSunshadePosition = " + CarUtils.topSunshadePosition);
        return CarUtils.topSunshadePosition;
    }
    /**
     * 设置后备箱开关
     *
     * @return VEHICLE.OFF VEHICLE.ON
     */
    public void setTrunkStatus(int status) {
        try {
            AppConfig.INSTANCE.mCarCabinManager.setIntProperty
                    (CarCabinManager.ID_BODY_PTS_REMOTE_COMMAND,
                            VEHICLE_AREA_TYPE_GLOBAL, status);
            Log.d(TAG, "lh:set trunk status:" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void checkTrunkMovementStatus(boolean isOpen) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int status = getTrunkRunningStatus();
                if (isOpen) {//检测打开后备箱是否成功
                    if (status != VEHICLE.ON) {
                        int pos = getTrunkPos();
                        if (pos != 100) {
                            conditionId = BACKDOORC11CONDITION;
                            defaultText = mContext.getString(R.string.backdoorC11);
                            Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_excption, R.string.object_exception_other, BACKDOORC11CONDITION, R.string.condition_backdoor11);
                        }
                    }
                } else { //检测关闭后备箱是否成功
                    if (status != VEHICLE.OFF) {
                        int pos = getTrunkPos();
                        if (pos != 0) {
                            conditionId = BACKDOORC11CONDITION;
                            defaultText = mContext.getString(R.string.backdoorC11);
                            Utils.eventTrack(mContext, R.string.skill_backdoor, R.string.scene_excption, R.string.object_exception_other, BACKDOORC11CONDITION, R.string.condition_backdoor11);
                        }
                    }
                }

                if (!TextUtils.isEmpty(conditionId)) {
                    getMessageWithTtsSpeak(conditionId, defaultText);
                }
            }
        }, 500);
    }

    /**
     * 获取后备箱运动状态
     * PTS_GateStatus=0x3:Opening，表明背门正在开启中；
     * 当PTS_GateStatus=0x4:Closing，表明背门正在关闭中；
     * 当PTS_GateStatus=0x5:Stop，表明背门处于停止中
     */
    public int getTrunkRunningStatus() {
        int status = -1;
        try {
            status = AppConfig.INSTANCE.mCarCabinManager.getIntProperty
                    (CarCabinManager.ID_BODY_DOOR_TRUNK_DOOR_STATE,
                            GLOBAL_ROW_1_LEFT);
            Log.d(TAG, "lh:get trunk running status(default:-1):" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * 获取后备箱开关
     *
     * @return AVM.AVM_ON_ACK , AVM.AVM_OFF_ACK
     */
    public int getTrunkPos() {
        int status = -1;
        try {
            status = AppConfig.INSTANCE.mCarCabinManager.getIntProperty
                    (CarCabinManager.ID_BODY_DOOR_TRUNK_DOOR_POS,
                            GLOBAL_ROW_1_LEFT);
            Log.d(TAG, "lh:get trunk pos(default:-1)---" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return status;
    }

    private boolean isHaveTrunk() {
        //返回下线配置是否有电动后备箱
        try {
            byte[] offline = AppConfig.INSTANCE.mCarMcuManager.getByteArrayProperty(ID_VENDOR_OFF_LINE_STATUS, VEHICLE_AREA_TYPE_GLOBAL);
            boolean isHaveTrunk = (offline[5] >> 4 & 0x1) == 0 ? false : true;
            Log.d(TAG, "lh:have trunk:" + (isHaveTrunk ? "YES" : "NO") + ",VALUE：" + (offline[5] >> 5 & 0x1));
            return isHaveTrunk;
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下发车窗控制指令
     *
     * @param value 0:关闭 100:打开 10:透气
     */
    public void setWindowControl(int value, String status, String mTAG) {
        int all_windown = (int) (WINDOW_ROW_1_LEFT | WINDOW_ROW_1_RIGHT | WINDOW_ROW_2_LEFT | WINDOW_ROW_2_RIGHT);
        if(status.equals(WINDOW_BACK)){//后车窗
            try {
                AppConfig.INSTANCE.mCarCabinManager.setIntProperty(ID_WINDOW_POS, WINDOW_ROW_2_LEFT, value);
                AppConfig.INSTANCE.mCarCabinManager.setIntProperty(ID_WINDOW_POS, WINDOW_ROW_2_RIGHT, value);
                Log.d(mTAG, "---------lh---setWindowControl==value==" + value + ",status:" + status);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }else {
            if (status.equals(WINDOW_LEFT_FRONT) || status.equals(WINDOW_LEFT_FRONT2)) {//左前车窗
                all_windown = WINDOW_ROW_1_LEFT;
            } else if (status.equals(WINDOW_RIGHT_FRONT) || status.equals(WINDOW_RIGHT_FRONT2)) {//右前车窗
                all_windown = WINDOW_ROW_1_RIGHT;
            } else if (status.equals(WINDOW_LEFT_BACK)) {//左后车窗
                all_windown = WINDOW_ROW_2_LEFT;
            } else if (status.equals(WINDOW_RIGHT_BACK)) {//右后车窗
                all_windown = WINDOW_ROW_2_RIGHT;
            }

            try {
                AppConfig.INSTANCE.mCarCabinManager.setIntProperty(ID_WINDOW_POS, all_windown, value);
                Log.d(mTAG, "---------lh---setWindowControl==value==" + value + ",status:" + status + ",window:" + all_windown);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkWiperSettingStatus(boolean isOpen,int appName, int scene, int object, int condition) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int status = getWiperStatus();
                if (isOpen) {//打开雨刮
                    if (status != VEHICLE.WIPER_LOW) {
                        conditionId = WIPERC5CONDITION;
                        defaultText = mContext.getString(R.string.wiperC5);
                        Utils.eventTrack(mContext, R.string.skill_wiper, R.string.scene_excption, R.string.object_exception_other,WIPERC5CONDITION,R.string.condition_wiper5);
                        getMessageWithTtsSpeak(conditionId, defaultText);
                        return;
                    }
                } else {//关闭雨刮
                    if (status != VEHICLE.WIPER_OFF) {
                        conditionId = WIPERC5CONDITION;
                        defaultText = mContext.getString(R.string.wiperC5);
                        Utils.eventTrack(mContext, R.string.skill_wiper, R.string.scene_excption, R.string.object_exception_other,WIPERC5CONDITION,R.string.condition_wiper5);
                        getMessageWithTtsSpeak(conditionId, defaultText);
                        return;
                    }
                }

                if (!TextUtils.isEmpty(conditionId)) {
                    Utils.eventTrack(mContext, appName,scene,object,conditionId,condition);
                    getMessageWithTtsSpeak(conditionId, defaultText);
                }
            }
        }, 500);
    }

    /**
     * 控制雨刮开关
     */
    private void setWindowPiper(int value) {
        try {
            AppConfig.INSTANCE.mCarCabinManager.setIntProperty(ID_BODY_WINDOW_WIPER, WINDOW_FRONT_WINDSHIELD, value);
            Log.d(TAG, "---------lh---控制雨刮开关==value==" + value);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取雨刮开关状态
     * 打开前雨刮--判断前雨刮开关状态信号BCM_FrontWiperStatus=0x1，判断是否执行成功
     * 关闭前雨刮--判断前雨刮开关状态信号BCM_FrontWiperStatus=0x0，判断是否执行成功，
     */
    private int getWiperStatus() {
        int status = 0;
        try {
            status = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(ID_BODY_WINDOW_WIPER, WINDOW_FRONT_WINDSHIELD);
            Log.d(TAG, "---------lh---获取雨刮开关状态==value==" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * 获取车窗状态
     * 0:关闭 100:打开 90:透气
     */
    public int getWindowPosition(String status) {
        int position = -1;
        Log.d(TAG,"CarUtils.left1carWindowPos = " + CarUtils.left1carWindowPos + ",CarUtils.right1carWindowPos = " + CarUtils.right1carWindowPos +
                ",CarUtils.left2carWindowPos = " + CarUtils.left2carWindowPos + ",CarUtils.right2carWindowPos = " + CarUtils.right2carWindowPos);
        if (status.equals(WINDOW_LEFT_FRONT) || status.equals(WINDOW_LEFT_FRONT2)) {//左前车窗
            position = CarUtils.left1carWindowPos;
        } else if (status.equals(WINDOW_RIGHT_FRONT) || status.equals(WINDOW_RIGHT_FRONT2)) {//右前车窗
            position = CarUtils.right1carWindowPos;
        } else if (status.equals(WINDOW_LEFT_BACK)) {//左后车窗
            position = CarUtils.left2carWindowPos;
        } else if (status.equals(WINDOW_RIGHT_BACK)) {//右后车窗
            position = CarUtils.right2carWindowPos;
        } else if(status.equals(WINDOW_ALL)) {//所有车窗
            position = (int)((CarUtils.left1carWindowPos + CarUtils.right1carWindowPos + CarUtils.left2carWindowPos + CarUtils.right2carWindowPos) / 4.0) ;
        }else if(status.equals(WINDOW_BACK)){//后车窗
            position = (int)((CarUtils.left2carWindowPos + CarUtils.right2carWindowPos) / 2.0) ;
        } else{//默认左前车窗
            position = CarUtils.left1carWindowPos;
        }
        return position;
    }

    //获取是否下雨
//    private int getRainingStatus() {
//        /*int status = 0;
//        try {
//            status = AppConfig.INSTANCE.mCarCabinManager.getIntProperty(ID_BODY_WINDOW_WIPER_SENSOR,
//                    VEHICLE_AREA_TYPE_GLOBAL);
//            Log.d(TAG, "lh:是否下雨的值status=" + status);
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }*/
//        return CarUtils.outsideRainStatus;
//    }

    //电源开关状态
    private int getACStatus() {
//        int status = -1;
//        try {
//            CarSensorEvent mIgnitionEvent = AppConfig.INSTANCE.mCarSensorManager.getLatestSensorEvent(CarSensorManager.SENSOR_TYPE_IGNITION_STATE);
//            status = mIgnitionEvent.getIgnitionStateData(null).ignitionState;//主动获取的点火状态
//            Log.d(TAG, "lh:get ignition status----" + status);
//        } catch (CarNotConnectedException e) {
//            e.printStackTrace();
//        }
        Log.d(TAG,"读取的电源状态CarUtils.powerStatus = " + CarUtils.powerStatus);
        return CarUtils.powerStatus;
    }

    private void getTtsMessage(String conditionId, String defaultTts, String replaceText, String nameValue) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = defaultTts;
                } else {
                    defaultText = Utils.replaceTts(tts, replaceText, nameValue);
                }
                Message msg = handler.obtainMessage();
                msg.obj = defaultText;
                msg.what = MSG_START_TTS;
                handler.sendMessage(msg);
            }
        });
    }

    private void getMessageWithTtsSpeak(String conditionId, int defaultText) {
        StartTTS = false;
        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, mContext.getString(defaultText), new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                StartTTS = true;
                if (!FloatViewManager.getInstance(mContext).isHide()) {
                    FloatViewManager.getInstance(mContext).hide();
                }
            }
        });
//        handler.sendEmptyMessageDelayed(MSG_StartTTS,2500);
    }

    private void getMessageWithTtsSpeak(String conditionId, String defaultText) {
        StartTTS = false;
        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultText, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                StartTTS = true;
                if (!FloatViewManager.getInstance(mContext).isHide()) {
                    FloatViewManager.getInstance(mContext).hide();
                }
            }
        });
//        handler.sendEmptyMessageDelayed(MSG_StartTTS,5000);
    }

    private void getMessageWithoutTtsSpeak(String conditionId, String defaultText, String replaceText, String nameValue,int appName, int scene, int object, int condition) {
        StartTTS = false;
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText + ",replaceText = " + replaceText);
                String defaultTts = tts;
                if (TextUtils.isEmpty(tts)) {
                    defaultTts = defaultText;
                }
                defaultTts = Utils.replaceTts(defaultTts, replaceText, nameValue);
                defaultTts = String.format(defaultTts, nameValue);
                Log.d(TAG, "onConfirm: defaultTts = " + defaultTts);
                if("小欧我要抽烟".equals(DatastatManager.primitive))
                    Utils.eventTrack(mContext, R.string.skill_global_nowake, R.string.scene_mvw_smoking, DatastatManager.primitive,R.string.skill_smart_car_control,DatastatManager.response, MHXC53CONDITION, R.string.condition_null, defaultTts,true);//埋点

                Utils.eventTrack(mContext, appName, scene, object, conditionId, condition, defaultTts);//埋点
                Utils.startTTSOnly(defaultTts, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        StartTTS = true;
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                    }
                });
            }
        });
//        handler.sendEmptyMessageDelayed(MSG_StartTTS,4000);
    }

    /**
     * 获取车窗锁止状态
     */
    public boolean getWindowLockStatus(String window) {
        //默认左前车窗
        int all_windown = (int) (WINDOW_ROW_1_LEFT | WINDOW_ROW_1_RIGHT | WINDOW_ROW_2_LEFT | WINDOW_ROW_2_RIGHT);
        if (window.equals(WINDOW_LEFT_FRONT) || window.equals(WINDOW_LEFT_FRONT2)) {//左前车窗
            all_windown = WINDOW_ROW_1_LEFT;
        } else if (window.equals(WINDOW_RIGHT_FRONT) || window.equals(WINDOW_RIGHT_FRONT2)) {//右前车窗
            all_windown = WINDOW_ROW_1_RIGHT;
        } else if (window.equals(WINDOW_LEFT_BACK)) {//左后车窗
            all_windown = WINDOW_ROW_2_LEFT;
        } else if (window.equals(WINDOW_RIGHT_BACK)) {//右后车窗
            all_windown = WINDOW_ROW_2_RIGHT;
        }
        boolean status = false;
        try {
            status = AppConfig.INSTANCE.mCarCabinManager.getBooleanProperty(CarCabinManager.ID_WINDOW_LOCK,
                    all_windown);
            Log.d(TAG, "lh:get window lock status---" + status + ",window:" + window + "，area:" + all_windown);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return status;
    }

    private void checkWindowLockStatus(String status) {
        if (status.equals(WINDOW_RIGHT_FRONT) || status.equals(WINDOW_LEFT_BACK) || status.equals(WINDOW_RIGHT_BACK)) {
            boolean windowLockStatus = getWindowLockStatus(status);
            if (windowLockStatus) { //车门锁止,则不响应非主驾车窗控制
                getMessageWithTtsSpeak(TtsConstant.CARWINDOWC21CONDITION, mContext.getString(R.string.carwindow_c21));
                Utils.eventTrack(mContext, R.string.skill_carwindow, R.string.scene_excption, R.string.object_exception,CARWINDOWC21CONDITION,R.string.condition_carwindow21);

                return;
            }
        }
    }

    /**
     * 设置天窗位置
     */
    public void setSunroofPos(int pos) {
        try {
            //AppConfig.INSTANCE.mCarCabinManager.setIntProperty(ID_BODY_WINDOW_SUNROOF_CONTROL_POS,
            //        WINDOW_ROOF_TOP_1, pos);WINDOW_ROOF_TOP_2
            AppConfig.INSTANCE.mCarCabinManager.setIntProperty(ID_BODY_WINDOW_SUNROOF_CONTROL_POS,
                    WINDOW_ROOF_TOP_1, pos);
            Log.d(TAG, "lh:设置天窗的位置pos =" + pos);
        } catch (CarNotConnectedException e) {
            Log.d(TAG, "lh:设置天窗的位置Exception = " + e);
            e.printStackTrace();
        }
    }

    //检测车窗控制下发是否成功
    private void checkWindowSettingStatus(int initPos,final String windowName, boolean isReplace,int appName, int scene, int object, int condition) {
        int left_front = CarUtils.left1carWindowPos;
        int right_front = CarUtils.right1carWindowPos;
        int left_behind = CarUtils.left2carWindowPos;
        int right_behind = CarUtils.right2carWindowPos;
        Log.d(TAG, "checkWindowSettingStatus: left_front = " + left_front + ",right_front = " + right_front +
                ",left_behind = " + left_behind + ",right_behind = " + right_behind);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int appName1 = appName;
                int scene1 = scene;
                int object1 = object;
                int condition1 = condition;
                //if(status != VEHICLE.WINDOW_DOWNING && status != VEHICLE.WINDOW_UPING) {//未处于上升或下降
                    if (windowName.equals(WINDOW_ALL)) {//全部车窗
                        if (left_front == CarUtils.left1carWindowPos && right_front == CarUtils.right1carWindowPos &&
                                left_behind == CarUtils.left2carWindowPos && right_behind == CarUtils.right2carWindowPos) {
                            if (left_front != 127 && right_front != 127 && left_behind != 127 && right_behind != 127) {//未处于防夹位置
                                conditionId = CARWINDOWC22CONDITION;
                                defaultText = mContext.getString(R.string.carwindow_c22);
                                appName1 = R.string.skill_carwindow;
                                scene1 = R.string.scene_excption;
                                object1 = R.string.object_exception_other;
                                condition1 = R.string.condition_carwindow22;
                            } else {//处于防夹位置
                                conditionId = CARWINDOWC24CONDITION;
                                defaultText = mContext.getString(R.string.carwindow_c24);
                                appName1 = R.string.skill_carwindow;
                                scene1 = R.string.scene_excption;
                                object1 = R.string.object_exception_other;
                                condition1 = R.string.condition_carwindow24;
                            }
                        }
                    } else if(windowName.equals(WINDOW_BACK)) {//后车窗
                        if (left_behind == CarUtils.left2carWindowPos && right_behind == CarUtils.right2carWindowPos) {
                            if (left_behind != 127 && right_behind != 127) {//未处于防夹位置
                                conditionId = CARWINDOWC22CONDITION;
                                defaultText = mContext.getString(R.string.carwindow_c22);
                                appName1 = R.string.skill_carwindow;
                                scene1 = R.string.scene_excption;
                                object1 = R.string.object_exception_other;
                                condition1 = R.string.condition_carwindow22;
                            } else {//处于防夹位置
                                conditionId = CARWINDOWC24CONDITION;
                                defaultText = mContext.getString(R.string.carwindow_c24);
                                appName1 = R.string.skill_carwindow;
                                scene1 = R.string.scene_excption;
                                object1 = R.string.object_exception_other;
                                condition1 = R.string.condition_carwindow24;
                            }
                        }
                    } else {
                        if (initPos == getWindowPosition(windowName)) {
                            if (initPos == 127) {//处于防夹位置
                                conditionId = CARWINDOWC24CONDITION;
                                defaultText = mContext.getString(R.string.carwindow_c24);
                                appName1 = R.string.skill_carwindow;
                                scene1 = R.string.scene_excption;
                                object1 = R.string.object_exception_other;
                                condition1 = R.string.condition_carwindow24;
                            } else {//未处于防夹位置
                                conditionId = CARWINDOWC22CONDITION;
                                defaultText = mContext.getString(R.string.carwindow_c22);
                                appName1 = R.string.skill_carwindow;
                                scene1 = R.string.scene_excption;
                                object1 = R.string.object_exception_other;
                                condition1 = R.string.condition_carwindow22;
                            }
                        }
                    }
                //}

                String name = windowName;
                if (windowName.contains("车窗")) {
                    name = windowName.replace("车窗", "");
                }else if(windowName.contains("窗户")){
                    name = windowName.replace("窗户", "");
                }
                getMessageWithoutTtsSpeak(conditionId, defaultText, "#WINDOW#", name,appName1,scene1,object1,condition1);
            }
        }, 1000);
    }

    //检测车窗控制下发是否成功，只用于抽烟模式
    private void checkWindowSettingStatusForSmoke(int initPos) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int appName1 = 0;
                int scene1 = 0;
                int object1 = 0;
                int condition1 = 0;
                if (initPos == CarUtils.left1carWindowPos) {
                    conditionId = SMOKEC2CONDITION;
                    defaultText = mContext.getString(R.string.smokeC2);
                    appName1 = R.string.skill_smart_car_control;
                    scene1 = R.string.scene_smoke;
                    object1 = R.string.object_smoke;
                    condition1 = R.string.condition_smokeC2;
                }else {
                    conditionId = SMOKEC1CONDITION;
                    defaultText = mContext.getString(R.string.smokeC1);
                    appName1 = R.string.skill_smart_car_control;
                    scene1 = R.string.scene_smoke;
                    object1 = R.string.object_smoke;
                    condition1 = R.string.condition_smokeC1;
                }

                if(FloatViewManager.getInstance(mContext).isHide()){
                    showAssistant();

                    Message msg = new Message();
                    Map map = new HashMap();
                    map.put("conditionId",conditionId);
                    map.put("defaultText",defaultText);
                    map.put("appName1",appName1);
                    map.put("scene1",scene1);
                    map.put("object1",object1);
                    map.put("condition1",condition1);

                    msg.what = MSG_SHOW_WORD;
                    msg.obj = map;
                    handler.sendMessageDelayed(msg,600);
                }else {
                    getMessageWithoutTtsSpeak(conditionId, defaultText, "#WINDOW#", "",appName1,scene1,object1,condition1);
                }
            }
        }, 1000);
    }

    @Override
    public void finish() {
        super.finish();
        handler.removeCallbacks(null);
    }

    //判断天窗是否打开透气，所有车窗是否关闭
    private boolean isSunroofAndAllWindowOpen(){
        //天窗已处于抽烟模式：天窗透气，天窗开一半，1/3,1/4,2/3,3/4，全开
        boolean isSunroofOpen = false;
        boolean isAllWindowOpen = false;
        int mTopWindowStatus = getSunroofStatus();
        Log.d(TAG,"CarUtils.topWindowPosition = " + CarUtils.topWindowPosition);
        if((Math.abs(CarUtils.topWindowPosition - 6) <= 5 && mTopWindowStatus == VEHICLE.SUNROOF_SLIDE_STOP) ||
                mTopWindowStatus == VEHICLE.SUNROOF_FULLY_UP || (CarUtils.topWindowPosition >= 6 && mTopWindowStatus != VEHICLE.SUNROOF_TILT_STOP) ||
                mTopWindowStatus == VEHICLE.SUNROOF_FULLY_OPEN){
            isSunroofOpen = true;
        }else {
            isSunroofOpen = false;
        }

        isAllWindowOpen = getAllWindowOpenStatus();
        if(!isSunroofOpen && !isAllWindowOpen){
            return false;
        }
        return true;
    }

    //所有车窗是否打开
    private boolean getAllWindowOpenStatus(){
        boolean isAllWindowOpen = false;
        int position = getWindowPosition(WINDOW_ALL);
        Log.d(TAG, "isSunroofAndAllWindowOpen: position = " + position);
        if (position != 0) { //车窗处于打开位置：透气，全开，1/2，1/3,1/4
            isAllWindowOpen = true;
        }else {
            isAllWindowOpen = false;
        }
        return isAllWindowOpen;
    }

    //背门是否打开
    private boolean isTrunkOpen(){
        boolean isTrunkOpen = false;
        int position = CarUtils.getInstance(mContext).getTrunkPosition();
        Log.d(TAG, "getTrunkPosition: " + position);
        if (position != 0) {
            isTrunkOpen = true;
        }else {
            isTrunkOpen = false;
        }
        return isTrunkOpen;
    }

    //所有车门是否关闭
    private boolean isDoorOpen(){
        boolean isDoorOpen = false;
        int driverDoorStatus = CarUtils.getInstance(mContext).getDoorStatus(VehicleAreaDoor.DOOR_ROW_1_LEFT);
        int left2DoorStatus = CarUtils.getInstance(mContext).getDoorStatus(VehicleAreaDoor.DOOR_ROW_2_LEFT);
        int right1DoorStatus = CarUtils.getInstance(mContext).getDoorStatus(VehicleAreaDoor.DOOR_ROW_1_RIGHT);
        int right2DoorStatus = CarUtils.getInstance(mContext).getDoorStatus(VehicleAreaDoor.DOOR_ROW_2_RIGHT);

        Log.d(TAG, "isDoorOpen: " + isDoorOpen);
        if(driverDoorStatus == VEHICLE.OFF && left2DoorStatus == VEHICLE.OFF && right1DoorStatus == VEHICLE.OFF && right2DoorStatus == VEHICLE.OFF){
            isDoorOpen = false;
        }else {
            isDoorOpen = true;
        }
        return isDoorOpen;
    }

    /**
     * 空调是否打开
     */
    private boolean isAirOpen() {
        if (CarUtils.airStatus == HVAC_ON) {
            return true;
        }else {
            return false;
        }
    }

    //获取当前空调的循环模式
    private boolean getCircleMode() {
        //先判断空调是否打开
        boolean isOpen = isAirOpen();
        Log.d(TAG,"isOpen = " + isOpen);
        if(!isOpen){
            return false;
        }

        //再判断循环模式
        int circleMode = 0;
        try {
            circleMode = AppConfig.INSTANCE.mCarHvacManager.getIntProperty(ID_HVAC_RECIRC_ON,
                            SEAT_ROW_1_LEFT);
            Log.i(TAG, "---------lh-----获取到空调循环模式---" + circleMode);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----设置循环模式CarNotConnectedException---" + e);
            e.printStackTrace();
        }

        if(circleMode == HVAC.LOOP_OUTSIDE){//外循环
            return true;
        }else {
            return false;
        }
    }

    public boolean handleFeedbackCommand(){
        Log.d(TAG, "handleFeedbackCommand() called");
        boolean isSunroofOpen = isSunroofAndAllWindowOpen();//天窗及车窗是否打开
        boolean isTrunkOpen = isTrunkOpen();//背门是否打开
        boolean isDoorOpen = isDoorOpen();//车门是否打开
        boolean isLeft1carWindowPos127 = CarUtils.left1carWindowPos == 127 ? true:false;//主驾车窗是否处于防夹保护
        boolean isOutsideOpen = getCircleMode();
        boolean isAirOpen = isAirOpen();

        if (!isAirOpen) {//空调默认在外循环状态，即使空调未打开也处于外循环状态
            isOutsideOpen = true;
        }

        Log.d(TAG, "handleFeedbackCommand: isSunroofOpen = " + isSunroofOpen + ",isTrunkOpen = " + isTrunkOpen +
                ",isDoorOpen = " + isDoorOpen + ",isLeft1carWindowPos127 = " + isLeft1carWindowPos127 +
                ",isOutsideOpen = " + isOutsideOpen + ",isAirOpen = " + isAirOpen);

        String[] texts = null;
        boolean isNeedHide = false;
        if(!isSunroofOpen && !isTrunkOpen && !isDoorOpen && !isOutsideOpen && !isLeft1carWindowPos127){
            //天窗、车门、背门、及所有车窗关闭，空调外循环关闭且主驾车窗未进入防夹保护
            conditionId = MSGC76CONDITION;
            condition = R.string.condition_feed_c76;
            texts = mContext.getResources().getStringArray(R.array.msg_c76);
        }else if(!isSunroofOpen && !isTrunkOpen && !isDoorOpen && !isOutsideOpen && isLeft1carWindowPos127){
            //天窗、车门、背门、及所有车窗关闭，空调外循环关闭且主驾车窗已进入防夹保护
            conditionId = MSGC76_1CONDITION;
            condition = R.string.condition_feed_c76_1;
            texts = mContext.getResources().getStringArray(R.array.msg_c76_1);
        }else if(!isSunroofOpen && !isTrunkOpen && !isDoorOpen && isOutsideOpen && !isLeft1carWindowPos127){
            //天窗、车门、背门、及所有车窗关闭，空调外循环打开且主驾车窗未进入防夹保护
            conditionId = MSGC77CONDITION;
            condition = R.string.condition_feed_c77;
            texts = mContext.getResources().getStringArray(R.array.msg_c77);
        }else if(!isSunroofOpen && !isTrunkOpen && !isDoorOpen && isOutsideOpen && isLeft1carWindowPos127){
            //天窗、车门、背门、及所有车窗关闭，空调外循环打开且主驾车窗已进入防夹保护
            conditionId = MSGC77_1CONDITION;
            condition = R.string.condition_feed_c77_1;
            texts = mContext.getResources().getStringArray(R.array.msg_c77_1);
        }else if((isSunroofOpen || isTrunkOpen || isDoorOpen) && !isOutsideOpen){
            //天窗、车门、背门、及所有车窗任意设备存在打开情况，空调处于内循环状态
            conditionId = MSGC78CONDITION;
            condition = R.string.condition_feed_c78;
            texts = mContext.getResources().getStringArray(R.array.msg_c78);
        }else if((isSunroofOpen || isTrunkOpen || isDoorOpen) && isOutsideOpen){
            //天窗、车门、背门、及所有车窗任意设备存在打开情况，空调外循环打开
            conditionId = MSGC79CONDITION;
            condition = R.string.condition_feed_c79;
            texts = mContext.getResources().getStringArray(R.array.msg_c79);
            isNeedHide = true;
        }
        //保存是哪个条件下触发的抽烟提醒，后面的二次交互说确定后，需要根据不同的条件执行不同的操作
        SharedPreferencesUtils.saveString(mContext,AppConstant.SMOKECONDITION,conditionId);
        int i = new Random().nextInt(texts.length);
        defaultText = texts[i];
        if(isNeedHide){
            getMessageSpeakAndHide(mContext,conditionId,condition,defaultText);
            return true;
        }else {
            getMessageWithoutTtsSpeak(mContext,conditionId,condition,defaultText);
            return false;
        }
    }

    /*
    /抽烟模式的二次交互
     */
    public void confirmSmoking(IntentEntity intentEntity){
        String recordConditionId = SharedPreferencesUtils.getString(mContext,AppConstant.SMOKECONDITION,MSGC76CONDITION);
        Log.d(TAG,"recordConditionId = " + recordConditionId);
        if(MSGC76CONDITION.equals(recordConditionId)){//开启主驾车窗透气模式，开启空调外循环模式
            setWindowControl(20, WINDOW_LEFT_FRONT, TAG);
            AirController.getInstance(mContext).changeCircleModeDelayed(LOOP_OUTSIDE,TAG);
        }else if(MSGC76_1CONDITION.equals(recordConditionId)){//开启天窗透气模式，开启空调外循环模式
            setSunroofPos(6);
            AirController.getInstance(mContext).changeCircleModeDelayed(LOOP_OUTSIDE,TAG);
        }else if(MSGC77CONDITION.equals(recordConditionId)){//开启主驾车窗透气模式（车窗开启开度暂定为20%）
            setWindowControl(20, WINDOW_LEFT_FRONT, TAG);
        }else if(MSGC77_1CONDITION.equals(recordConditionId)){//开启天窗透气模式
            setSunroofPos(6);
        }else if(MSGC78CONDITION.equals(recordConditionId)){//开启空调外循环模式
            AirController.getInstance(mContext).changeCircleModeDelayed(LOOP_OUTSIDE,TAG);
        }
        String[] texts = mContext.getResources().getStringArray(R.array.msg_c80);
        defaultText = texts[new Random().nextInt(texts.length)];
        getMessageSpeakAndHide(mContext,MSGC80CONDITION,R.string.condition_feed_c80,defaultText);
    }

    public void handWindowWarn(String conditionId,String position){
        //唤起小欧
        Log.d(TAG,"call assistant show..");
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);

        Map<String,String> map = new HashMap<>();
        map.put("conditionId",conditionId);
        map.put("position",position);
        Message msg = new Message();
        msg.obj = map;
        msg.what = MSG_SHOW_ASSISTANT;
        handler.sendMessageDelayed(msg,600);
    }

    private void startSpeak(String conditionId,String position){
        String defaultTts = "";
        if(conditionId == TtsConstant.GUIDEBTNC41CONDITION){
            defaultTts = Utils.replaceTts(mContext.getString(R.string.btnC41),"#POSITION#",position);
        }else if(conditionId == TtsConstant.GUIDEBTNC42CONDITION){
            defaultTts = Utils.replaceTts(mContext.getString(R.string.btnC42),"#POSITION#",position);
        }

        getMessageWithoutBurying(mContext, conditionId, defaultTts);
    }

    private void getMessageWithoutBurying(Context context, String conditionId,String defaultTTS){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        FloatViewManager.getInstance(mContext).hide();
                    }
                });

            }
        });
    }

    private void getMessageWithoutTtsSpeak(Context context, String conditionId,int condition,String defaultTTS){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                ActiveServiceModel.Activit_tts_msg = ttsText;
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_feed,R.string.sobject_smoking, conditionId, condition,ttsText);
                Utils.startTTS(ttsText, PriorityControler.PRIORITY_THREE,new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {

                    }
                });

            }
        });
    }

    private void getMessageSpeakAndHide(Context context, String conditionId,int condition,String defaultTTS){
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                ActiveServiceModel.Activit_tts_msg = ttsText;
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_feed,R.string.sobject_smoking, conditionId,
                        condition,ttsText);
                Utils.startTTS(ttsText,PriorityControler.PRIORITY_THREE,new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                    }
                });
            }
        });
    }

    private void openAPA(int status){
        try {
            AppConfig.INSTANCE.mCarCabinManager.setIntProperty
                    (CarCabinManager.ID_ADAS_APA_ACTIVE_ON  ,
                            VEHICLE_AREA_TYPE_GLOBAL, status);
            Log.d(TAG, "lh:set APA status:" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
            Log.d(TAG, "lh:set APA status :" + e);
        }
    }

    /*
     * 判断速度是否超过30Km/h
     */
    private boolean isMinSpeed() {
        if (CarUtils.getInstance(mContext).getSpeed() < 30) {
            return true;
        }
        return false;
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }
}
