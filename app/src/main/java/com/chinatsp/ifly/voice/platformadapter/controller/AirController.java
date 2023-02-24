package com.chinatsp.ifly.voice.platformadapter.controller;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.constant.HVAC;
import android.car.hardware.constant.VEHICLE;
import android.car.hardware.hvac.CarHvacManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.entity.EventTrackingEntity;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IAirController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

import static android.car.VehicleAreaSeat.HVAC_ALL;
import static android.car.hardware.constant.HVAC.HVAC_FAN_DIRECTION_FACE;
import static android.car.hardware.constant.HVAC.HVAC_FAN_DIRECTION_FACE_FLOOR;
import static android.car.hardware.constant.HVAC.HVAC_FAN_DIRECTION_FLOOR;
import static android.car.hardware.constant.HVAC.HVAC_FAN_DIRECTION_FLOOR_DEFROST;
import static android.car.hardware.constant.HVAC.HVAC_OFF;
import static android.car.hardware.constant.HVAC.HVAC_OFF_REQ;
import static android.car.hardware.constant.HVAC.HVAC_ON;
import static android.car.hardware.constant.HVAC.HVAC_ON_REQ;
import static android.car.hardware.constant.HVAC.LOOP_INNER;
import static android.car.hardware.constant.HVAC.LOOP_OUTSIDE;
import static android.car.hardware.constant.VehicleAreaId.SEAT_ROW_1_LEFT;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_FRONT_WINDSHIELD;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_REAR_WINDSHIELD;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_AC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_DEFROSTER;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_FAN_DIRECTION;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_FAN_SPEED;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_FAN_SPEED_ACK;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_FAN_SPEED_ADJUST;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_POWER_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_RECIRC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_TEMPERATURE_LV_SET;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_TEMPERATURE_SET;
import static android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat.ROW_1_LEFT;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;

public class AirController extends BaseController implements IAirController {
    private final static String TAG = "AirController";

    private Context mContext;

    private IntentEntity intentEntity;

    //内循环
    private final static String MODE_RECYCLE_IN = "内循环";
    //外循环
    private final static String MODE_RECYCLE_OUT = "外循环";
    //自动循环
    private final static String MODE_RECYCLE_AUTO = "自动循环";

    // 除霜
    private final static String MODE_DEFROST = "除霜";

    //制冷模式
    private final static String MODE_COLD = "制冷";
    //制热模式
    private final static String MODE_HOT = "制热";

    //太热了
    private final static String MINUS_MORE = "MINUS_MORE";
    //有点热
    private final static String MINUS_LITTLE = "MINUS_LITTLE";
    // 太冷了
    private final static String PLUS_MORE = "PLUS_MORE";
    //有点冷
    private final static String PLUS_LITTLE = "PLUS_LITTLE";
    // 温度增高/风量增高
    private final static String PLUS = "PLUS";
    // 温度降低/风量降低
    private final static String MINUS = "MINUS";
    // 温度设为中档
    private final static String MEDIUM = "MEDIUM";

    // 温度最高/风量最高
    private final static String MAX = "MAX";
    // 温度最低
    private final static String MIN = "MIN";

    //中风
    private final static String WIND_CENTER = "中风";

    // 升高两度参数
    private final static String REF_CUR = "CUR";

    private final static String REF_ZERO = "ZERO";

    //面
    private final static String AIR_FLOW_FACE = "面";
    //脚
    private final static String AIR_FLOW_FOOT = "脚";
    //吹面吹脚
    private final static String AIR_FLOW_FACE_FOOT = "吹面吹脚";

    private final static String FRONT_DEFROST = "前除霜";

    private final static String REAR_DEFROST = "后除霜";
    private final static String POST_DEFROST1 = "后除霜";
    private final static String POST_DEFROST2 = "外后视镜";
    private final static String POST_DEFROST3 = "后视镜加热";
    private String resText = "";

    private String defaultText = "";

    //自动空调温度中档
    private final int MIDDLE_TEMP_AUTO_AC = 25;

    //电动空调温度中档
    private final int MIDDLE_TEMP_ELEC_AC = 8;

    //自动空调最高温度
    private final int MAX_TEMP_AUTO_AC = 33;

    //自动空调最低温度
    private final int MIN_TEMP_AUTO_AC = 17;

    //电动空调最高档位
    private final int MAX_TEMP_ELEC_AC = 16;

    //电动空调最低档位
    private final int MIN_TEMP_ELEC_AC = 1;

    //最高风量档位
    private final int MAX_WIND_AC = 8;

    //最低风量档位
    private final int MIN_WIND_AC = 1;
    //风量中档位
    private final int MIDDLE_WIND_AC = 4;
    private EventTrackingEntity eventTrackingEntity;
    private final String num = "#NUM#";
    private final String max = "#MAXNUM#";
    private final String min = "#MINNUM#";
    private final String middle = "#MIDDLENUM#";
    private static final int AC_POWER_MSG = 1001;
    private static final int TEAMP_MSG = 1002;
    private static final int GEAR_MSG = 1003;
    private static final int AC_MSG = 1004;
    private static final int AIR_FLOW_MSG = 1005;
    private static final int FAN_SPEED_MSG = 1006;
    private static final int CIRCLE_MODE_MSG = 1007;
    private static final int FAN_SPEED_UP_MSG = 2001;
    private static final int FAN_SPEED_DOWN_MSG = 2002;
    private static final int CHECK_TEMP_MSG = 3001;
    private int status = 0;
    private float temp = 0;
    private int gear = 0;
    private static AirController mAirController;

    public static AirController getInstance(Context c){
        if(mAirController == null)
            mAirController  = new AirController(c);
        return mAirController;
    }

    public AirController(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void srAction(IntentEntity intentEntity) {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case AC_POWER_MSG:
                    status = (int)msg.obj;
                    changeAirStatusDelayed(status);
                    break;
                case TEAMP_MSG:
                    temp = (float)msg.obj;
                    setTemperatureDelayed(temp);
                    break;
                case GEAR_MSG:
                    gear = (int)msg.obj;
                    setTempGearDelayed(gear);
                    break;
                case AC_MSG:
                    status = (int)msg.obj;
                    changeACStatusDelayed(status);
                    break;
                case AIR_FLOW_MSG:
                    status = (int)msg.obj;
                    changeAirFlowDirectionDelayed(status);
                    break;
                case FAN_SPEED_MSG:
                    status = (int)msg.obj;
                    setFanSpeedDelyed(status);
                    break;
                case CIRCLE_MODE_MSG:
                    status = (int)msg.obj;
                    changeCircleModeDelayed(status,TAG);
                    break;
                case FAN_SPEED_UP_MSG://风量升高
                    int curFanSpeed = getFanSpeed();
                    curFanSpeed++;
                    if (curFanSpeed > MAX_WIND_AC) {
                        curFanSpeed = MAX_WIND_AC;
                    }
                    setFanSpeedDelyed(curFanSpeed);
                    resText = mContext.getString(R.string.acC62);
                    defaultText = String.format(resText, curFanSpeed);
                    getMessageWithTtsSpeak(ACC62CONDITION, defaultText,num,  curFanSpeed + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_incr, R.string.condition_acc62);
                    break;
                case FAN_SPEED_DOWN_MSG://风量降低
                    int curFanSpeed2 = getFanSpeed();
                    curFanSpeed2--;
                    if (curFanSpeed2 < MIN_WIND_AC) {
                        curFanSpeed2 = MIN_WIND_AC;
                    }
                    setFanSpeedDelyed(curFanSpeed2);
                    resText = mContext.getString(R.string.acC65);
                    defaultText = String.format(resText, curFanSpeed2);
                    getMessageWithTtsSpeak(ACC65CONDITION,defaultText,num,curFanSpeed2 + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_decr, R.string.condition_acc65);
                    break;
                case CHECK_TEMP_MSG:
                    String parameter = String.format("%.1f",getTemperature());
                    resText = mContext.getString(R.string.acC1);
                    defaultText = String.format(resText,parameter);
                    getMessageWithTtsSpeak(ACC1CONDITION, defaultText,num,parameter,R.string.skill_acc, R.string.scene_acc, R.string.object_acc1, R.string.condition_acc_auto);
                    break;
                default:
                    break;
            }
        }
    };

    public void srActionAir(IntentEntity intentEntity, String intentStr) {
        this.intentEntity = intentEntity;
        //说话人提示
        EventBusUtils.sendTalkMessage(intentEntity.text);
        String category = intentEntity.semantic.slots.category;
        String insType = intentEntity.semantic.slots.insType;
        String mode = intentEntity.semantic.slots.mode;
        String operation = intentEntity.operation;
        JSONObject tempObj = null;
        JSONObject temperatureGearObj = null;
        JSONObject fanSpeedObj = null;
        String directFanSpeed = "";
        String refFanSpeed = "";
        String temperature = "";
        String fanSpeed = "";
        eventTrackingEntity = null;
        try {
            JSONObject obj = new JSONObject(intentStr);
            JSONObject intentObj = obj.getJSONObject("intent");
            JSONObject semanticObj = intentObj.getJSONObject("semantic");
            JSONObject slotsObj = semanticObj.getJSONObject("slots");
            if (slotsObj != null && slotsObj.has("temperature")) {
                temperature = slotsObj.getString("temperature");
                try {
                    tempObj = new JSONObject(temperature);
                } catch (JSONException e) {
                    Log.i(TAG, "e == " + e);
                }
            }
            if (slotsObj.has("temperatureGear")) {
                String temperatureGear = slotsObj.getString("temperatureGear");
                try {
                    temperatureGearObj = new JSONObject(temperatureGear);
                } catch (JSONException e) {
                    Log.i(TAG, "e == " + e);
                }
            }
            if (slotsObj.has("fanSpeed")) {
                fanSpeed = slotsObj.getString("fanSpeed");
                try {
                    fanSpeedObj = new JSONObject(fanSpeed);
                    refFanSpeed = fanSpeedObj.getString("ref");
                    directFanSpeed = fanSpeedObj.getString("direct");
                } catch (JSONException e) {
                    Log.i(TAG, "e == " + e);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String parameter = "";
        int type = getIgnitionStatus();
        Log.d(TAG,"power type = " + type);
        if(type <= CarSensorEvent.IGNITION_STATE_ACC &&
                (POST_DEFROST1.equals(intentEntity.semantic.slots.mode) || POST_DEFROST3.equals(intentEntity.semantic.slots.mode) ||
                POST_DEFROST2.equals(intentEntity.semantic.slots.name) || "后视镜".equals(intentEntity.semantic.slots.name) ||
                "后除雾".equals(intentEntity.semantic.slots.mode) || "除霜".equals(intentEntity.semantic.slots.mode))){//后除霜
            String name = getNameValue(intentEntity);
            defaultText = mContext.getString(R.string.defC7);
            getMessageWithoutTtsSpeak(DEFC7CONDITION,mContext.getString(R.string.defC7),"#NAME#",name,
                    R.string.skill_post_defrost,R.string.scene_post_defrost_error,R.string.object_post_defrost_other_error,R.string.condition_defC7);
            return;
        }
        if (type <= CarSensorEvent.IGNITION_STATE_ACC) {
            getMessageWithTtsSpeak(ACC90CONDITION, mContext.getString(R.string.ac_c90));
            Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_excption, R.string.object_exception_other, TtsConstant.ACC90CONDITION, R.string.condition_acc_ac_off);
            return;
        }
        //获取空调类型接口,auto:自动；elec：电动
        String acType = Utils.getProperty("persist.vendor.vehicle.hvac", "auto");
        Log.d(TAG, "lh:acType:" + acType);
        boolean isAutoAir = acType.equalsIgnoreCase("auto") ? true : false;
        //获取空调是否打开接口
        boolean isOpen = isAirOpen();
        Log.i(TAG, "isOpen = " + isOpen);
        sendBroadcastToACController();
        if (PlatformConstant.Operation.OPEN.equals(insType)) {
            //打开空调
//            if (!isOpen) {
//                //若空调为关闭状态,则下发指令打开空调,否则不处理
//                changeAirStatus(HVAC_ON);
//            }


            if (isAutoAir) {
                //自动空调，打开空调时，不管空调是否开启均要发送打开空调指令，1010需求
                changeAirStatus(HVAC_ON);
                handler.sendEmptyMessageDelayed(CHECK_TEMP_MSG,600);
            } else {
                if (!isOpen) {
                    //若空调为关闭状态,则下发指令打开空调,否则不处理
                    changeAirStatus(HVAC_ON);
                }
                //电动空调，不管空调是否开启均要发送打开空调AC指令，1010需求
                changeACStatus(HVAC_ON);
                defaultText = mContext.getString(R.string.acC2);
                getMessageWithTtsSpeak(ACC2CONDITION, defaultText);
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc, R.string.object_acc1, ACC2CONDITION, R.string.condition_acc_elec);
            }
        } else if (PlatformConstant.Operation.CLOSE.equals(insType)) {
            //关闭空调
            //if (isOpen) {
                //若空调为打开状态,则下发指令关闭空调,否则不处理
                changeAirStatus(HVAC_OFF);
            //}
            getMessageWithTtsSpeak(ACC3CONDITION, mContext.getString(R.string.acC3));
            Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc, R.string.object_acc2, ACC3CONDITION, R.string.condition_default);
        } else if (MODE_COLD.equals(mode)) { //制冷模式
            // TODO 获取当前空调是否打开
            if (!isOpen) { //空调未打开
                changeAirStatus(HVAC_ON);
                getMessageWithTtsSpeak(ACC4CONDITION, mContext.getString(R.string.acC4));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode1, ACC4CONDITION, R.string.condition_acc_close);
            } else {
                getMessageWithTtsSpeak(ACC5CONDITION, mContext.getString(R.string.acC5));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode1, ACC5CONDITION, R.string.condition_acc_open);
            }
            if (isAutoAir) { //自动空调
                setTemperature(22.0f);
            } else { //电动空调
                setTempGear(5);
            }
            changeACStatus(HVAC_ON);
//            int acStatus = getACStatus();
//            if (acStatus != HVAC_ON_REQ) {
//                changeACStatus(HVAC_ON);
//            }
        } else if (MODE_HOT.equals(mode)) { //制热模式
            // TODO 获取当前空调是否打开
            if (!isOpen) {//空调未打开
                changeAirStatus(HVAC_ON);
                //text = "已切换为制热模式";
                getMessageWithTtsSpeak(ACC6CONDITION,  mContext.getString(R.string.acC6));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode2, ACC6CONDITION, R.string.condition_acc_close);
            } else {
                //text = "已为你打开空调，现在是制热模式";
                getMessageWithTtsSpeak(ACC7CONDITION,  mContext.getString(R.string.acC7));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode2, ACC7CONDITION, R.string.condition_acc_open);
            }
            if (isAutoAir) { //自动空调
                setTemperature(28.0f);
            } else { //电动空调
                setTempGear(12);
            }
            changeACStatus(HVAC_OFF);
//            int acStatus = getACStatus();
//            if (acStatus != HVAC_OFF_REQ) {
//                changeACStatus(HVAC_OFF);
//            }
        } else if (MINUS_MORE.equals(temperature) || MINUS_LITTLE.equals(temperature)) {
            //太热了
            if (isAutoAir) {
                //自动空调
                float temp;
                if (isOpen) {
                    // TODO 获取当前空调温度
                    temp = getTemperature();
                    temp -= 2;
                    if (temp < getMinTemperature()) {
                        temp = getMinTemperature();
                    }
                    setTemperature(temp);
                    resText = mContext.getString(R.string.acC9);
                    parameter = String.format("%.1f",temp);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC9CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode3, R.string.condition_acc9);
                } else {
                    temp = 22;
                    changeAirStatus(HVAC_ON);
                    parameter = String.format("%.1f",temp);
                    //text = "空调已打开，现在温度是" + temp + "度";
                    resText = mContext.getString(R.string.acC8);
                    defaultText = String.format(resText,  parameter);
                    setTemperature(temp);
                    getMessageWithTtsSpeak(ACC8CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode3, R.string.condition_acc8);
                }
                //自动空调取消控制AC
//                if(getACStatus() != HVAC_ON_REQ){
//                    changeACStatus(HVAC_ON_REQ);
//                }
            } else {
                // 电动空调
                int gear;
                boolean isGearChanges = false;
                if (isOpen) {
                    gear = getCurGear();
                    if(gear == MIN_TEMP_ELEC_AC){
                        isGearChanges = false;
                    }else {
                        isGearChanges = true;
                    }
                    gear--;
                    if (gear < getMinGear()) {
                        gear = getMinGear();
                    }
                    getMessageWithTtsSpeak(ACC11CONDITION,  mContext.getString(R.string.acC11));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode3, ACC11CONDITION, R.string.condition_acc11);
                } else {
                    changeAirStatus(HVAC_ON);
                    gear = 5;
                    isGearChanges = true;
                    getMessageWithTtsSpeak(ACC10CONDITION, mContext.getString(R.string.acC10));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode3, ACC10CONDITION, R.string.condition_acc10);
                }
                //if(getACStatus() != HVAC_ON_REQ){
                    changeACStatus(HVAC_ON_REQ);
                //}
                if(isGearChanges){
                    setTempGear(gear);
                }
            }
        } else if (PLUS_MORE.equals(temperature) || PLUS_LITTLE.equals(temperature)) {
            //太冷了
            if (isAutoAir) {
                //自动空调
                if (isOpen) {
                    // TODO 获取当前空调温度
                    float temp = getTemperature();
                    temp += 2;
                    if (temp > getMaxTemperature()) {
                        temp = getMaxTemperature();
                    }
                    parameter = String.format("%.1f",temp);
                    setTemperature(temp);
                    resText = mContext.getString(R.string.acC13);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC13CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode4, R.string.condition_acc13);
                } else {
                    changeAirStatus(HVAC_ON);
                    int temp = 28;
                    parameter = temp + "";
                    resText = mContext.getString(R.string.acC12);
                    defaultText = String.format(resText, parameter);
                    setTemperature(temp);
                    getMessageWithTtsSpeak(ACC12CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode4, R.string.condition_acc12);
                }
                //自动空调取消控制AC
//                if(getACStatus() != HVAC_OFF_REQ){
//                    changeACStatus(HVAC_OFF_REQ);
//                }
            } else {
                // 电动空调
                int gear;
                boolean isGearChanges = false;
                if (isOpen) {
                    gear = getCurGear();
                    if(gear == MAX_TEMP_ELEC_AC){
                        isGearChanges = false;
                    }else {
                        isGearChanges = true;
                    }
                    gear++;
                    if(gear > getMaxGear()){
                        gear = getMaxGear();
                    }
                    getMessageWithTtsSpeak(ACC15CONDITION, mContext.getString(R.string.acC15));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode4, ACC15CONDITION, R.string.condition_acc15);
                } else {
                    changeAirStatus(HVAC_ON);
                    gear = 12;
                    isGearChanges = true;
                    getMessageWithTtsSpeak(ACC14CONDITION, mContext.getString(R.string.acC14));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode4, ACC14CONDITION, R.string.condition_acc14);
                }
                //if(getACStatus() != HVAC_OFF_REQ){
                    changeACStatus(HVAC_OFF_REQ);
                //}
                if(isGearChanges){
                    setTempGear(gear);
                }
            }
        } else if (PLUS.equals(temperature)) {
            //温度增高
            handleTempratureChange(temperature,isAutoAir,isOpen);
        } else if (MINUS.equals(temperature)) {
            //温度降低
            handleTempratureChange(temperature,isAutoAir,isOpen);
        } else if (MEDIUM.equals(temperature)) {
            //温度设为中档
            if (isAutoAir) {
                //自动空调
                if(!isOpen){
                    changeAirStatus(HVAC_ON);
                    setTemperature(MIDDLE_TEMP_AUTO_AC);
                    resText = mContext.getString(R.string.acC42);
                    defaultText = String.format(resText, MIDDLE_TEMP_AUTO_AC);
                    getMessageWithTtsSpeak(ACC42CONDITION, defaultText, num, MIDDLE_TEMP_AUTO_AC + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_middle, R.string.condition_acc42);
                }else {
                    setTemperature(MIDDLE_TEMP_AUTO_AC);
                    resText = mContext.getString(R.string.acC42_1);
                    defaultText = String.format(resText, MIDDLE_TEMP_AUTO_AC);
                    getMessageWithTtsSpeak(ACC42_1CONDITION, defaultText, num, MIDDLE_TEMP_AUTO_AC + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_middle, R.string.condition_acc42_1);
                }

            } else {
                //电动空调
                if(!isOpen){
                    changeAirStatus(HVAC_ON);
                    setTempGear(MIDDLE_TEMP_ELEC_AC);
                    resText = mContext.getString(R.string.acC43);
                    defaultText = String.format(resText, MIDDLE_TEMP_ELEC_AC);
                    getMessageWithTtsSpeak(ACC43CONDITION, defaultText, num, MIDDLE_TEMP_ELEC_AC + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_middle, R.string.condition_acc43);
                }else {
                    setTempGear(MIDDLE_TEMP_ELEC_AC);
                    resText = mContext.getString(R.string.acC43_1);
                    defaultText = String.format(resText, MIDDLE_TEMP_ELEC_AC);
                    getMessageWithTtsSpeak(ACC43_1CONDITION, defaultText, num, MIDDLE_TEMP_ELEC_AC + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_middle, R.string.condition_acc43_1);
                }
            }
        } else if (tempObj != null) {
            //调节温度
            try {
                String ref = tempObj.getString("ref");
                float offset = tempObj.getInt("offset");
                BigDecimal b = new BigDecimal(offset);
                offset = b.setScale(1,BigDecimal.ROUND_DOWN).floatValue();
                String direct = tempObj.getString("direct");
                if (REF_CUR.equals(ref)) {
                    //调节一定的温度幅度
                    if ("+".equals(direct)) {
                        Log.e("zheng","zheng 调节温度-----------------增加温度："+offset);
                        upTempValue(isAutoAir, isOpen, offset);
                    } else if ("-".equals(direct)) {
                        Log.e("zheng","zheng 调节温度-----------------降低温度："+offset);
                        downTempValue(isAutoAir, isOpen, offset);
                    }else{
                        doExceptonAction(mContext);
                    }
                } else if (REF_ZERO.equals(ref)) {
                    //调节到固定温度
                    setTempValue(isAutoAir, isOpen, offset);
                    Log.e("zheng","zheng 调节温度-----------------调节到固定温度："+offset);
                }else{
                    doExceptonAction(mContext);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                doExceptonAction(mContext);
            }
        } else if (temperatureGearObj != null) {
            //调节档位
            try {
                String ref = temperatureGearObj.getString("ref");
                int offset = temperatureGearObj.getInt("offset");
                String direct = temperatureGearObj.getString("direct");
                if (REF_CUR.equals(ref)) {
                    //调节一定的档位幅度
                    if ("+".equals(direct)) {
                        upGearValue(isAutoAir, isOpen, offset);
                    } else if ("-".equals(direct)) {
                        downGearValue(isAutoAir, isOpen, offset);
                    }else{
                        doExceptonAction(mContext);
                    }
                } else if (REF_ZERO.equals(ref)) {
                    //调节到固定档位
                    setGearValue(isAutoAir, isOpen, offset);
                } else {
                    doExceptonAction(mContext);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                doExceptonAction(mContext);
            }
        } else if (MAX.equals(temperature)) {
            // 温度最高
            if (isOpen) {
                //text = "温度已升到最高";
                getMessageWithTtsSpeak(ACC41CONDITION, mContext.getString(R.string.acC41));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_high, ACC41CONDITION, R.string.condition_acc41);
            } else {
                changeAirStatus(HVAC_ON);
                //text = "已为你打开空调，并将温度调到最高";
                getMessageWithTtsSpeak(ACC40CONDITION, mContext.getString(R.string.acC40));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_high, ACC40CONDITION, R.string.condition_acc40);
            }
            if (isAutoAir) {
                setTemperature(getMaxTemperature());
            } else {
                setTempGear(getMaxGear());
            }
        } else if (MIN.equals(temperature)) {
            //温度最低
            if (isOpen) {
                //text = "温度已降到最低，小心不要感冒哦";
                defaultText = mContext.getString(R.string.acC45);
                getMessageWithTtsSpeak(ACC45CONDITION, mContext.getString(R.string.acC45));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_low, ACC45CONDITION, R.string.condition_acc45);
            } else {
                changeAirStatus(HVAC_ON);
                //text = "已为你打开空调，并将温度调到最低";
                defaultText = mContext.getString(R.string.acC44);
                getMessageWithTtsSpeak(ACC44CONDITION, mContext.getString(R.string.acC44));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_low, ACC44CONDITION, R.string.condition_acc44);
            }
            if (isAutoAir) {
                setTemperature(getMinTemperature());
            } else {
                setTempGear(getMinGear());
            }
        } else if ((PLUS.equals(fanSpeed)) || (("+").equals(directFanSpeed)) && REF_CUR.equals(refFanSpeed)) {
            fanSpeed = PLUS;
            //风量增高
            handleFanSpeedChange(fanSpeed,isAutoAir,isOpen);
        } else if ((MINUS.equals(fanSpeed)) || (("-").equals(directFanSpeed)) && REF_CUR.equals(refFanSpeed)) {
            fanSpeed = MINUS;
            //风量降低
            handleFanSpeedChange(fanSpeed,isAutoAir,isOpen);
        } else if (fanSpeedObj != null) {
            //风量调节为x档
            try {
                String ref = fanSpeedObj.getString("ref");
                int offset = fanSpeedObj.getInt("offset");
                if (REF_ZERO.equals(ref)) {
                    //调节到某一档位
                    if (offset > MAX_WIND_AC) {
                        offset = MAX_WIND_AC;
                        //text = "已将风量开到最大，现在为" + offset + "档";
                        resText = mContext.getString(R.string.acC72);
                        defaultText = String.format(resText, offset);
                        getMessageWithTtsSpeak(ACC72CONDITION,defaultText,max,offset + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_x, R.string.condition_acc72);
                    } else if (offset < MIN_WIND_AC) {
                        offset = MIN_WIND_AC;
                        resText = mContext.getString(R.string.acC73);
                        defaultText = String.format(resText, offset);
                        getMessageWithTtsSpeak(ACC73CONDITION,defaultText,min,offset + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_x, R.string.condition_acc73);
                    } else {
                        resText = mContext.getString(R.string.acC71);
                        defaultText = String.format(resText, offset);
                        getMessageWithTtsSpeak(ACC71CONDITION,defaultText,num,offset + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_x, R.string.condition_acc71);
                    }
                    setFanSpeed(offset);
                }else{
                    doExceptonAction(mContext);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                doExceptonAction(mContext);
            }
        } else if (MAX.equals(fanSpeed)) {
            //风量最高
            setFanSpeed(MAX_WIND_AC);
            getMessageWithTtsSpeak(ACC68CONDITION, mContext.getString(R.string.acC68));
            Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_high, ACC68CONDITION, R.string.condition_default);
        } else if (MIN.equals(fanSpeed)) {
            //风量最低
            setFanSpeed(MIN_WIND_AC);
            getMessageWithTtsSpeak(ACC70CONDITION, mContext.getString(R.string.acC70));
            Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_low, ACC70CONDITION, R.string.condition_default);
        } else if (WIND_CENTER.equals(fanSpeed)) {
            //风量中档
            setFanSpeed(MIDDLE_WIND_AC);
            //text = "风量已调到" + 4 + "档";
            resText = mContext.getString(R.string.acC69);
            defaultText = String.format(resText, MIDDLE_WIND_AC);
            getMessageWithTtsSpeak(ACC69CONDITION,defaultText,middle,MIDDLE_WIND_AC + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_middle, R.string.condition_default);
        } else if (MODE_RECYCLE_IN.equals(mode)) {
            //内循环
            if(operation != null && operation.equals(PlatformConstant.Operation.SET)){//打开内循环
                changeCircleMode(LOOP_INNER);
                getMessageWithTtsSpeak(ACC74CONDITION, mContext.getString(R.string.acC74));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle1, ACC74CONDITION, R.string.condition_default);
            }else if(operation != null && operation.equals(PlatformConstant.Operation.CLOSE)){//关闭内循环 = 打开外循环
                changeCircleMode(LOOP_OUTSIDE);
                getMessageWithTtsSpeak(ACC75CONDITION, mContext.getString(R.string.acC75));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle2, ACC75CONDITION, R.string.condition_default);
            }else {
                doExceptonAction(mContext);
            }
        } else if (MODE_RECYCLE_OUT.equals(mode)) {
            //外循环
            if(operation != null && operation.equals(PlatformConstant.Operation.SET)){//打开外循环
                changeCircleMode(LOOP_OUTSIDE);
                getMessageWithTtsSpeak(ACC75CONDITION, mContext.getString(R.string.acC75));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle2, ACC75CONDITION, R.string.condition_default);
            }else if(operation != null && operation.equals(PlatformConstant.Operation.CLOSE)){//关闭外循环 = 打开内循环
                changeCircleMode(LOOP_INNER);
                getMessageWithTtsSpeak(ACC74CONDITION, mContext.getString(R.string.acC74));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle1, ACC74CONDITION, R.string.condition_default);
            }else {
                doExceptonAction(mContext);
            }
        } else if (MODE_RECYCLE_AUTO.equals(mode)) {
            //自动循环
            //TODO 是否支持自动循环
            boolean supportAutoMode = false;
            if (supportAutoMode) {
                //TODO 设置自动循环
                //text = "已打开自动循环模式";
                getMessageWithTtsSpeak(ACC77CONDITION, mContext.getString(R.string.acC77));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle3, ACC77CONDITION, R.string.condition_acc77);
            } else {
                //text = "抱歉，暂不支持此功能";
                //text = "小欧还不会这个操作";
//                Random random = new Random();
//                int n = random.nextInt(2);
//                String[] resTexts = mContext.getResources().getStringArray(R.array.acC76);
//                if (n == 0) {
//                    defaultText = resTexts[0];
//                } else {
//                    defaultText = resTexts[1];
//                }
//                getMessageWithTtsSpeak(ACC76CONDITION, defaultText);
//                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle3, ACC76CONDITION, R.string.condition_acc76);
                doExceptonAction(mContext);
            }
        } else if (MODE_DEFROST.equals(mode) && (null != intentEntity.semantic.slots.airflowDirection)) {//除霜模式
            //除霜模式的airflowDirection与 吹脚模式相同,除霜模式与吹脚模式的区别在于 mode不同.
            if (AIR_FLOW_FOOT.equals(intentEntity.semantic.slots.airflowDirection)) {
                if(operation == null || (operation != null && !operation.equals(PlatformConstant.Operation.SET))){
                    doExceptonAction(mContext);
                }else {
                    if (!isOpen) {
                        changeAirStatus(HVAC_ON);
                        getMessageWithTtsSpeak(ACC84CONDITION, mContext.getString(R.string.acC84));
                        Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction4, ACC84CONDITION, R.string.condition_acc_close);
                    } else {
                        getMessageWithTtsSpeak(ACC85CONDITION, mContext.getString(R.string.acC85));
                        Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction4, ACC85CONDITION, R.string.condition_acc_open);
                    }
                    changeAirFlowDirection(HVAC_FAN_DIRECTION_FLOOR_DEFROST);
                }
            }else {
                doExceptonAction(mContext);
            }
        } else if (AIR_FLOW_FACE.equals(intentEntity.semantic.slots.airflowDirection)) {
            //吹面
            if(operation == null || (operation != null && !operation.equals(PlatformConstant.Operation.SET))){
                doExceptonAction(mContext);
            }else {
                if (!isOpen) {
                    changeAirStatus(HVAC_ON);
                    getMessageWithTtsSpeak(ACC78CONDITION, mContext.getString(R.string.acC78));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction1, ACC78CONDITION, R.string.condition_acc_close);
                } else {
                    //text = "已打开吹脸模式";
                    getMessageWithTtsSpeak(ACC79CONDITION, mContext.getString(R.string.acC79));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction1, ACC79CONDITION, R.string.condition_acc_open);
                }
                changeAirFlowDirection(HVAC_FAN_DIRECTION_FACE);
            }
        } else if (AIR_FLOW_FOOT.equals(intentEntity.semantic.slots.airflowDirection)) {
            //吹脚
            if(operation == null || (operation != null && !operation.equals(PlatformConstant.Operation.SET))){
                doExceptonAction(mContext);
            }else {
                if (!isOpen) {
                    changeAirStatus(HVAC_ON);
                    getMessageWithTtsSpeak(ACC80CONDITION, mContext.getString(R.string.acC80));
                    Utils.eventTrack(mContext, R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction2, ACC80CONDITION, R.string.condition_acc_close);
                } else {
                    getMessageWithTtsSpeak(ACC81CONDITION, mContext.getString(R.string.acC81));
                    Utils.eventTrack(mContext, R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction2, ACC81CONDITION, R.string.condition_acc_open);
                }
                changeAirFlowDirection(HVAC_FAN_DIRECTION_FLOOR);
            }
        } else if (AIR_FLOW_FACE_FOOT.equals(intentEntity.semantic.slots.airflowDirection)) {
            //吹面吹脚
            if(operation == null || (operation != null && !operation.equals(PlatformConstant.Operation.SET))){
                doExceptonAction(mContext);
            }else {
                if (!isOpen) {
                    changeAirStatus(HVAC_ON);
                    getMessageWithTtsSpeak(ACC82CONDITION, mContext.getString(R.string.acC82));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction3, ACC82CONDITION, R.string.condition_acc_close);
                } else {
                    getMessageWithTtsSpeak(ACC83CONDITION, mContext.getString(R.string.acC83));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction3, ACC83CONDITION, R.string.condition_acc_open);
                }
                changeAirFlowDirection(HVAC_FAN_DIRECTION_FACE_FLOOR);
            }
        } else if (FRONT_DEFROST.equals(intentEntity.semantic.slots.mode) || (MODE_DEFROST.equals(mode) && intentEntity.text.contains("前除霜"))) {
            //前除霜
            if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
                changeFrontDefrost(HVAC_ON_REQ);
                getMessageWithTtsSpeak(ACC86CONDITION, mContext.getString(R.string.acC86));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_front_defrost, R.string.object_acc_front_defrost1, ACC86CONDITION, R.string.condition_default);
            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
                changeFrontDefrost(HVAC_OFF_REQ);
                getMessageWithTtsSpeak(ACC87CONDITION, mContext.getString(R.string.acC87));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_front_defrost, R.string.object_acc_front_defrost2, ACC87CONDITION, R.string.condition_default);
            }else {
                doExceptonAction(mContext);
            }
        }
//        else if (REAR_DEFROST.equals(intentEntity.semantic.slots.mode)) {
//            //后除霜
//            if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
//                changeRearDefrost(HVAC_ON_REQ);
//                getMessageWithTtsSpeak(ACC88CONDITION, mContext.getString(R.string.acC88));
//                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_rear_defrost, R.string.object_acc_rear_defrost1, ACC88CONDITION, R.string.condition_default);
//
//            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
//                //text = "已关闭后除霜模式";
//                changeRearDefrost(HVAC_OFF_REQ);
//                getMessageWithTtsSpeak(ACC89CONDITION, mContext.getString(R.string.acC89));
//                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_rear_defrost, R.string.object_acc_rear_defrost2, ACC89CONDITION, R.string.condition_default);
//            }else {
//                doExceptonAction(mContext);
//            }
//        }
        else if(POST_DEFROST1.equals(intentEntity.semantic.slots.mode) || POST_DEFROST3.equals(intentEntity.semantic.slots.mode) ||
                POST_DEFROST2.equals(intentEntity.semantic.slots.name) || "后视镜".equals(intentEntity.semantic.slots.name) ||
                "后除雾".equals(intentEntity.semantic.slots.mode) || "除霜".equals(intentEntity.semantic.slots.mode)){//后除霜模式设置
            int postDefrostStatus = CarUtils.getInstance(mContext).getPostDefrostStatus();
            String name = getNameValue(intentEntity);
            Log.d(TAG, "postDefrostStatus = " + postDefrostStatus + ",name = " + name);
            if(false){//无配置 默认全系都有配置
                getMessageWithoutTtsSpeak(TtsConstant.DEFC5CONDITION,mContext.getString(R.string.defC5),"#NAME#",name,
                        R.string.skill_post_defrost,R.string.scene_post_defrost_error,R.string.object_post_defrost_less,R.string.condition_defC5);
            }else if(postDefrostStatus == VEHICLE.INVALID){//控制器异常
                getMessageWithoutTtsSpeak(TtsConstant.DEFC6CONDITION,mContext.getString(R.string.defC6),"#NAME#",name,
                        R.string.skill_post_defrost,R.string.scene_post_defrost_error,R.string.object_post_defrost_other_error,R.string.condition_defC6);
            }else if(postDefrostStatus == VEHICLE.ON) {//已打开
                if(PlatformConstant.Operation.OPEN.equals(intentEntity.operation) || PlatformConstant.Operation.SET.equals(intentEntity.operation)){//打开
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC1CONDITION,mContext.getString(R.string.defC1),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_on,R.string.condition_defC1);
                }else if(PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)){//关闭
                    AirController.getInstance(mContext).changeRearDefrost(HVAC_OFF_REQ);
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC4CONDITION,mContext.getString(R.string.defC4),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_off,R.string.condition_defC4);
                }else {
                    doExceptonAction(mContext);
                }
            }else if(postDefrostStatus == VEHICLE.OFF){//未打开
                if(PlatformConstant.Operation.OPEN.equals(intentEntity.operation) || PlatformConstant.Operation.SET.equals(intentEntity.operation)){//打开
                    AirController.getInstance(mContext).changeRearDefrost(HVAC_ON_REQ);
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC2CONDITION,mContext.getString(R.string.defC2),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_on,R.string.condition_defC2);
                }else if(PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)){//关闭
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC3CONDITION,mContext.getString(R.string.defC3),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_off,R.string.condition_defC3);
                }else {
                    doExceptonAction(mContext);
                }
            }else {
                doExceptonAction(mContext);
            }
        } else {
            //判断电源是否关闭
            boolean isPowerOn = getIgnitionStatus() >= CarSensorEvent.IGNITION_STATE_ACC ? true : false;
            if (!isPowerOn) {
                getMessageWithTtsSpeak(ACC90CONDITION, mContext.getString(R.string.ac_c90));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_excption, R.string.object_exception_other, TtsConstant.ACC90CONDITION, R.string.condition_acc_ac_off);
            } else {
                String[] resTexts = mContext.getResources().getStringArray(R.array.acC76);
                Random random = new Random();
                int n = random.nextInt(2);
                if (n == 0) {
                    defaultText = resTexts[0];
                } else {
                    defaultText = resTexts[1];
                }
                getMessageWithTtsSpeak(ACC76CONDITION, defaultText);
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_excption, R.string.object_exception_other, TtsConstant.ACC76CONDITION, R.string.condition_acc_ac_exception);
            }
        }
    }

    //获取tts文案,然后进行播报,埋点
    private void getMessageWithTtsSpeak(String conditionId, String defaultText) {
        boolean isFirstUse = SharedPreferencesUtils.getBoolean(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
        if (!isFirstUse) {
            Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultText, new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    if (!FloatViewManager.getInstance(mContext).isHide()) {
                        FloatViewManager.getInstance(mContext).hide();
                    }
                }
            });
        }
    }

    //获取tts文案,然后进行播报,埋点
    private void getMessageWithTtsSpeak(String conditionId,String defaultText,String orig,String replace,int appName, int scene, int object, int condition) {
        boolean isFirstUse = SharedPreferencesUtils.getBoolean(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
        if (!isFirstUse) {
            Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    Log.d(TAG, "tts = " + tts + ",defaultText = " + defaultText);
                    String defaultTts = tts;
                    if (TextUtils.isEmpty(tts)) {
                        defaultTts = defaultText;
                    } else {
                        if (!TextUtils.isEmpty(orig)) {
                            defaultTts = Utils.replaceTts(tts, orig, replace);
                        }
                    }
                    defaultTts = transformParam(defaultTts);
                    Log.d(TAG, "transform defaultTts = " + defaultTts);

                    if("温度高一点".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc40,TtsConstant.MHXC40CONDITION,R.string.condition_null, defaultTts);
                    }else if("温度低一点".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc41,TtsConstant.MHXC41CONDITION,R.string.condition_null, defaultTts);
                    }else if("风速大一点".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc42,TtsConstant.MHXC42CONDITION,R.string.condition_null, defaultTts);
                    }else if("风速小一点".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc43,TtsConstant.MHXC43CONDITION,R.string.condition_null, defaultTts);
                    }else
                        Utils.eventTrack(mContext,appName,scene,object, conditionId, condition, defaultTts);//埋点

                    Utils.startTTSOnly(defaultTts, new TTSController.OnTtsStoppedListener() {
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
    }

    private void getMessageWithoutTtsSpeak(String conditionId, String defaultText, String replaceText, String nameValue,int appName, int scene, int object, int condition) {
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
                Utils.eventTrack(mContext, appName, scene, object, conditionId, condition, defaultTts);//埋点
                Utils.startTTSOnly(defaultTts, new TTSController.OnTtsStoppedListener() {
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

    //处理温度升高或降低
    private void handleTempratureChange(String temperature,boolean isAutoAir,boolean isOpen){
        String parameter = "";
        if(PLUS.equals(temperature)){
            //温度升高
            if (isAutoAir) {
                //自动空调
                float curTemp = getTemperature();
                float maxTemp = MAX_TEMP_AUTO_AC;
                if (!isOpen) {
                    changeAirStatus(HVAC_ON);
                    curTemp += 2;
                    if (curTemp > getMaxTemperature()) {
                        curTemp = getMaxTemperature();
                    }
                    parameter = String.format("%.1f",curTemp);

                    setTemperature(curTemp);
                    resText = mContext.getString(R.string.acC16);
                    defaultText = String.format(resText,  parameter);
                    getMessageWithTtsSpeak(ACC16CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr, R.string.condition_acc16);
                } else {
                    if (curTemp == maxTemp) {
                        resText = mContext.getString(R.string.acC19);
                        parameter = String.format("%.1f",maxTemp);
                        defaultText = String.format(resText, parameter);
                        getMessageWithTtsSpeak(ACC19CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr, R.string.condition_acc19);
                    } else {
                        curTemp += 2;
                        if (curTemp > maxTemp) {
                            curTemp = maxTemp;
                        }
                        parameter = String.format("%.1f",curTemp);

                        setTemperature(curTemp);
                        resText = mContext.getString(R.string.acC18);
                        defaultText = String.format(resText, parameter);
                        getMessageWithTtsSpeak(ACC18CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr, R.string.condition_acc18);
                    }
                }
            } else {
                //电动空调
                int curGear = getCurGear();
                int maxGear = MAX_TEMP_ELEC_AC;
                if (!isOpen) {
                    curGear += 2;
                    if (curGear > maxGear) {
                        curGear = maxGear;
                    }
                    resText = mContext.getString(R.string.acC17);
                    defaultText = String.format(resText, curGear);
                    parameter = curGear + "";
                    changeAirStatus(HVAC_ON);
                    setTempGear(curGear);
                    getMessageWithTtsSpeak(ACC17CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr, R.string.condition_acc17);
                } else {
                    if (curGear == maxGear) {
                        resText = mContext.getString(R.string.acC21);
                        defaultText = String.format(resText, curGear);
                        parameter = curGear + "";
                        getMessageWithTtsSpeak(ACC21CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr, R.string.condition_acc21);
                    } else {
                        curGear += 2;
                        if (curGear > maxGear) {
                            curGear = maxGear;
                        }
                        resText = mContext.getString(R.string.acC20);
                        defaultText = String.format(resText, curGear);
                        parameter = curGear + "";
                        setTempGear(curGear);
                        getMessageWithTtsSpeak(ACC20CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr, R.string.condition_acc20);
                    }
                }
            }
        }else if(MINUS.equals(temperature)){
            // 温度降低
            if (isAutoAir) {
                //自动空调
                float curTemp = getTemperature();
                float minTemp = getMinTemperature();
                if (!isOpen) {
                    changeAirStatus(HVAC_ON);
                    curTemp -= 2;
                    if (curTemp < minTemp) {
                        curTemp = minTemp;
                    }
                    parameter = String.format("%.1f",curTemp);
                    setTemperature(curTemp);
                    resText = mContext.getString(R.string.acC28);
                    defaultText = String.format(resText,  parameter);
                    getMessageWithTtsSpeak(ACC28CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr, R.string.condition_acc28);
                } else if (curTemp == minTemp) {
                    parameter = String.format("%.1f",minTemp);
                    resText = mContext.getString(R.string.acC31);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC31CONDITION, defaultText, min, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr, R.string.condition_acc31);
                } else {
                    curTemp -= 2;
                    if (curTemp < minTemp) {
                        curTemp = minTemp;
                    }
                    parameter = String.format("%.1f",curTemp);
                    setTemperature(curTemp);
                    resText = mContext.getString(R.string.acC30);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC30CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr, R.string.condition_acc30);
                }
            } else {
                //电动空调
                int curGear = getCurGear();
                int minGear = getMinGear();
                if (!isOpen) {
                    curGear -= 2;
                    if (curGear < minGear) {
                        curGear = minGear;
                    }
                    resText = mContext.getString(R.string.acC29);
                    defaultText = String.format(resText, curGear);

                    changeAirStatus(HVAC_ON);
                    setTempGear(curGear);
                    getMessageWithTtsSpeak(ACC29CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr, R.string.condition_acc29);
                } else if (curGear == minGear) {
                    resText = mContext.getString(R.string.acC33);
                    defaultText = String.format(resText, curGear);
                    getMessageWithTtsSpeak(ACC33CONDITION, defaultText, min, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr, R.string.condition_acc33);
                } else {
                    curGear -= 2;
                    if (curGear < minGear) {
                        curGear = minGear;
                    }
                    resText = mContext.getString(R.string.acC32);
                    defaultText = String.format(resText, curGear);
                    setTempGear(curGear);
                    getMessageWithTtsSpeak(ACC32CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr, R.string.condition_acc32);
                }
            }
        }
    }

    //处理风速升高或降低
    private void handleFanSpeedChange(String fanSpeed,boolean isAutoAir,boolean isOpen){
        String parameter = "";
        if(PLUS.equals(fanSpeed)){
            //风量增高
            int curFanSpeed = getFanSpeed();
            if (isOpen) {
                if (curFanSpeed < (MAX_WIND_AC - 1)) {
                    curFanSpeed++;
                    resText = mContext.getString(R.string.acC63);
                    defaultText = String.format(resText, curFanSpeed);
                    parameter = curFanSpeed + "";
                    getMessageWithTtsSpeak(ACC63CONDITION, defaultText,num,parameter,R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_incr, R.string.condition_acc63);
                } else {
                    curFanSpeed++;
                    if (curFanSpeed > MAX_WIND_AC) {
                        curFanSpeed = MAX_WIND_AC;
                    }
                    getMessageWithTtsSpeak(ACC64CONDITION, mContext.getString(R.string.acC64));
                  if("风速大一点".equals(DatastatManager.primitive)){
                      Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc42,TtsConstant.MHXC42CONDITION,R.string.condition_null, mContext.getString(R.string.acC64));
                  }else
                      Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_incr, ACC64CONDITION, R.string.condition_acc64);
                }
                setFanSpeed(curFanSpeed);
            } else {
                changeAirStatus(HVAC_ON);
                handler.sendEmptyMessageDelayed(FAN_SPEED_UP_MSG,400);
            }
        }else if(MINUS.equals(fanSpeed)){
            //风量降低
            int curFanSpeed = getFanSpeed();
            if (isOpen) {
                if (curFanSpeed > (MIN_WIND_AC + 1)) {
                    curFanSpeed--;
                    resText = mContext.getString(R.string.acC66);
                    defaultText = String.format(resText, curFanSpeed);
                    getMessageWithTtsSpeak(ACC66CONDITION, defaultText,num, curFanSpeed + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_decr, R.string.condition_acc66);
                } else {
                    curFanSpeed--;
                    if (curFanSpeed < MIN_WIND_AC) {
                        curFanSpeed = MIN_WIND_AC;
                    }
                    getMessageWithTtsSpeak(ACC67CONDITION,mContext.getString(R.string.acC67));

                    if("风速小一点".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc43,TtsConstant.MHXC43CONDITION,R.string.condition_null, mContext.getString(R.string.acC67));
                    }else
                        Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_decr, ACC67CONDITION, R.string.condition_acc67);
                }
                setFanSpeed(curFanSpeed);
            } else {
                changeAirStatus(HVAC_ON);
                handler.sendEmptyMessageDelayed(FAN_SPEED_DOWN_MSG,400);
            }
        }
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        int type = getIgnitionStatus();
        Log.d(TAG,"mvwAction power type = " + type);
        if (type <= CarSensorEvent.IGNITION_STATE_ACC) {
            getMessageWithTtsSpeak(ACC90CONDITION, mContext.getString(R.string.ac_c90));
            if("温度高一点".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc40,TtsConstant.MHXC40CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }else if("温度低一点".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc41,TtsConstant.MHXC41CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }else if("风速大一点".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc42,TtsConstant.MHXC42CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }else if("风速小一点".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc43,TtsConstant.MHXC43CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }
            return;
        }
        //获取空调类型接口,auto:自动；elec：电动
        String acType = Utils.getProperty("persist.vendor.vehicle.hvac", "auto");
        Log.d(TAG, "lh:acType:" + acType);
        boolean isAutoAir = acType.equalsIgnoreCase("auto") ? true : false;
        //获取空调是否打开接口
        boolean isOpen = isAirOpen();
        Log.i(TAG, "isOpen = " + isOpen);
        sendBroadcastToACController();
        if("温度高一点".equals(mvwLParamEntity.nKeyword)){
            handleTempratureChange(PLUS,isAutoAir,isOpen);
        }else if("温度低一点".equals(mvwLParamEntity.nKeyword)){
            handleTempratureChange(MINUS,isAutoAir,isOpen);
        }else if("风速大一点".equals(mvwLParamEntity.nKeyword)){
            handleFanSpeedChange(PLUS,isAutoAir,isOpen);
        }else if("风速小一点".equals(mvwLParamEntity.nKeyword)){
            handleFanSpeedChange(MINUS,isAutoAir,isOpen);
        }
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }


    /**
     * 升高一定的温度
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void upTempValue(boolean isAutoAir, boolean isOpen, float offset) {
        String text = "";
        String parameter ="";
        //调节一定的温度幅度
        if (isAutoAir) {
            //自动空调
            float curTemp = getTemperature();
            float maxTemp = getMaxTemperature();
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                curTemp += offset;
                if (curTemp > maxTemp) {
                    curTemp = maxTemp;
                }
                parameter = String.format("%.1f",curTemp);
                setTemperature(curTemp);
                //text = "已为你打开空调，温度升高到" + curTemp + "度";
                resText = mContext.getString(R.string.acC22);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC22CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc22);
            } else if ((curTemp == maxTemp) || (curTemp + offset) >= maxTemp) {
                //text = "现在已升至最高温度，" + maxTemp + "度";
                if(curTemp != maxTemp) setTemperature(maxTemp);
                resText = mContext.getString(R.string.acC25);
                parameter = String.format("%.1f",maxTemp);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC25CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc25);
            } else {
                curTemp += offset;
                if (curTemp > maxTemp) {
                    curTemp = maxTemp;
                }
                setTemperature(curTemp);
                //text = "温度已升高到" + curTemp + "度";
                resText = mContext.getString(R.string.acC24);
                parameter = String.format("%.1f",curTemp);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC24CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc24);
            }
        } else {
            //电动空调
            int curGear = getCurGear();
            int maxGear = getMaxGear();
            //int offsetGear = getGearByTemp(offset);
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                curGear += offset;
                if (curGear > maxGear) {
                    curGear = maxGear;
                }
                //text = "已为你打开空调，温度升高到" + curGear + "档";
                resText = mContext.getString(R.string.acC23);
                defaultText = String.format(resText, curGear);
                setTempGear(curGear);
                getMessageWithTtsSpeak(ACC23CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc23);
            } else if ((curGear == maxGear) || (curGear + offset) >= maxGear) {
                //text = "温度已升到最高，现在是" + curGear + "档";
                if(curGear != maxGear) setTempGear(maxGear);
                resText = mContext.getString(R.string.acC27);
                defaultText = String.format(resText, maxGear);
                getMessageWithTtsSpeak(ACC27CONDITION, defaultText, max, maxGear + "", R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc27);
            } else {
                curGear += offset;
                if (curGear > maxGear) {
                    curGear = maxGear;
                }
                //text = "温度已升高到" + curGear + "档";
                resText = mContext.getString(R.string.acC26);
                defaultText = String.format(resText, curGear);
                setTempGear(curGear);
                getMessageWithTtsSpeak(ACC26CONDITION, defaultText,  num, curGear + "", R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc26);
            }

        }
    }

    /**
     * 降低一定的温度
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void downTempValue(boolean isAutoAir, boolean isOpen, float offset) {
        String text = "";
        String parameter ="";
        //调节一定的温度幅度
        if (isAutoAir) {
            //自动空调
            float curTemp = getTemperature();
            float minTemp = getMinTemperature();
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                //curTemp = getTemperature();
                curTemp -= offset;
                if (curTemp < minTemp) {
                    curTemp = minTemp;
                }
                parameter = String.format("%.1f",curTemp);
                setTemperature(curTemp);
                //text = "已为你打开空调，温度降低到" + curTemp + "度";
                resText = mContext.getString(R.string.acC34);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC34CONDITION, defaultText,  num, parameter, R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc34);
            } else if ((curTemp == minTemp) || (curTemp - offset) <= minTemp) {
                //text = "现在已降至最低温度，" + curTemp + "度";
                if(curTemp != minTemp) setTemperature(minTemp);
                resText = mContext.getString(R.string.acC37);
                parameter = String.format("%.1f",minTemp);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC37CONDITION, defaultText,  min, parameter, R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc37);
            } else {
                curTemp -= offset;
                if (curTemp < minTemp) {
                    curTemp = minTemp;
                }
                parameter = String.format("%.1f",curTemp);
                setTemperature(curTemp);
                //text = "温度已降到" + curTemp + "度";
                resText = mContext.getString(R.string.acC36);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC36CONDITION, defaultText,  num, parameter, R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc36);
            }
        } else {
            //电动空调
            int curGear = getCurGear();
            int minGear = getMinGear();
            //int offsetGear = getGearByTemp(offset);
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                curGear -= offset;
                if (curGear < minGear) {
                    curGear = minGear;
                }
                //text = "已为你打开空调，温度降低到" + curGear + "档";
                resText = mContext.getString(R.string.acC35);
                defaultText = String.format(resText, curGear);
                setTempGear(curGear);
                getMessageWithTtsSpeak(ACC35CONDITION, defaultText,  num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc35);
            } else if ((curGear == minGear) || (curGear - offset) <= minGear) {
                //text = "温度已降到最低，现在是" + curGear + "档";
                if(curGear != minGear) setTempGear(minGear);
                resText = mContext.getString(R.string.acC39);
                defaultText = String.format(resText, minGear);
                getMessageWithTtsSpeak(ACC39CONDITION, defaultText, min, minGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc39);
            } else {
                curGear -= offset;
                if (curGear < minGear) {
                    curGear = minGear;
                }
                resText = mContext.getString(R.string.acC38);
                defaultText = String.format(resText, curGear);
                setTempGear(curGear);
                getMessageWithTtsSpeak(ACC38CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc38);
            }
        }
    }


    /**
     * 设置某一固定温度值
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void setTempValue(boolean isAutoAir, boolean isOpen, float offset) {
        String text = "";
        String parameter = "";
        if (isAutoAir) {
            float maxTemp = getMaxTemperature();
            float minTemp = getMinTemperature();
            float curTemp;
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
            }
            if (offset > maxTemp) {
                curTemp = maxTemp;
                parameter = String.format("%.1f",maxTemp);
                //text = "超出温度范围，现在是最高温度，" + maxTemp + "度";
                resText = mContext.getString(R.string.acC46);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC46CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc46);
            } else if (offset < minTemp) {
                curTemp = minTemp;
                parameter = String.format("%.1f",minTemp);
                //text = "超出温度范围，现在是最低温度，" + minTemp + "度";
                resText = mContext.getString(R.string.acC47);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC47CONDITION, defaultText, min, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc47);
            } else {
                curTemp = offset;
                parameter = String.format("%.1f",offset);

                if (!isOpen) {
                    //text = "温度已调到" + curTemp + "度";
                    resText = mContext.getString(R.string.acC50);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC50CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc50);
                } else {
                    //text = "已为你打开空调，并将温度调到" + curTemp + "度";
                    resText = mContext.getString(R.string.acC52);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC52CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc52);
                }
            }
            setTemperature(curTemp);
        } else {
            //电动空调
            float maxTemp = getMaxTemperature();
            float minTemp = getMinTemperature();
            int curGear;
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
            }
            if (offset >= maxTemp) {
                curGear = getMaxGear();
                //text = "暂不支持具体温度调节。温度已升到最高，现在是" + curGear + "档";
                resText = mContext.getString(R.string.acC48);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC48CONDITION, defaultText, max, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc48);
            } else if (offset < minTemp) {
                curGear = getMinGear();
                //text = "暂不支持具体温度调节。温度已降到最低，现在是" + curGear + "档";
                resText = mContext.getString(R.string.acC49);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC49CONDITION, defaultText, min, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc49);
            } else {
                curGear = getGearByTemp(offset);
                if(curGear > getMaxGear()){
                    curGear = getMaxGear();
                }
                if(curGear < getMinGear()){
                    curGear = getMinGear();
                }
                if (!isOpen) {
                    //text = "暂不支持具体温度调节，已将温度调到" + curGear + "档";
                    resText = mContext.getString(R.string.acC51);
                    defaultText = String.format(resText, curGear);
                    getMessageWithTtsSpeak(ACC51CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc51);
                } else {
                    resText = mContext.getString(R.string.acC53);
                    defaultText = String.format(resText, curGear);
                    getMessageWithTtsSpeak(ACC53CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc53);
                }
            }
            setTempGear(curGear);
        }
    }

    /**
     * 升高一定的档位
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void upGearValue(boolean isAutoAir, boolean isOpen, int offset) {
        String text = "";
        String parameter ="";
        if (isAutoAir) {
            //自动空调
            float curTemp = getTemperature();
            float maxTemp = getMaxTemperature();
            float offsetTemp = getTempByGear(offset);
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                //curTemp = getTemperature();
                curTemp += offsetTemp;
                if (curTemp > maxTemp) {
                    curTemp = maxTemp;
                }
                parameter = String.format("%.1f",curTemp);
                setTemperature(curTemp);
                //text = "已为你打开空调，温度升高到" + temp + "度";
                resText = mContext.getString(R.string.acC22);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC22CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc22);
            } else if ((curTemp == maxTemp) || (curTemp + offset) >= maxTemp) {
                //text = "现在已升至最高温度，" + maxTemp + "度";
                if(curTemp != maxTemp) setTemperature(maxTemp);
                resText = mContext.getString(R.string.acC25);
                parameter = String.format("%.1f",maxTemp);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC25CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc25);
            } else {
                curTemp += offsetTemp;
                if (curTemp > maxTemp) {
                    curTemp = maxTemp;
                }
                parameter = String.format("%.1f",curTemp);
                setTemperature(curTemp);
                //text = "温度已升高到" + temp + "度";
                resText = mContext.getString(R.string.acC24);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC24CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc24);
            }
        } else {
            //电动空调
            int curGear = getCurGear();
            int maxGear = getMaxGear();
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                //curGear = getCurGear();
                curGear += offset;
                if (curGear > maxGear) {
                    curGear = maxGear;
                }
                setTempGear(curGear);
                resText = mContext.getString(R.string.acC23);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC23CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc23);
            } else if ((curGear == maxGear) || (curGear + offset) >= maxGear) {
                if(curGear != maxGear) setTempGear(maxGear);
                resText = mContext.getString(R.string.acC27);
                defaultText = String.format(resText, maxGear);
                getMessageWithTtsSpeak(ACC27CONDITION, defaultText,  max, maxGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc27);
            } else {
                curGear += offset;
                if (curGear > maxGear) {
                    curGear = maxGear;
                }
                setTempGear(curGear);
                resText = mContext.getString(R.string.acC26);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC26CONDITION, defaultText,  num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc26);
            }
        }
    }


    /**
     * 降低一定的档位
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void downGearValue(boolean isAutoAir, boolean isOpen, int offset) {
        String text = "";
        String parameter = "";
        if (isAutoAir) {
            //自动空调
            float curTemp = getTemperature();
            float minTemp = getMinTemperature();
            float offsetTemp = getTempByGear(offset);
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                curTemp -= offsetTemp;
                if (curTemp < minTemp) {
                    curTemp = minTemp;
                }
                parameter = String.format("%.1f",curTemp);

                setTemperature(curTemp);
                //text = "已为你打开空调，温度降低到" + curTemp + "度";
                resText = mContext.getString(R.string.acC34);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC34CONDITION, defaultText,  num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc34);
            } else if ((curTemp == minTemp) || (curTemp - offsetTemp) <= minTemp) {
                //text = "现在已降至最低温度，" + curTemp + "度";
                if(curTemp != minTemp) setTemperature(minTemp);
                resText = mContext.getString(R.string.acC37);
                parameter = String.format("%.1f",minTemp);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC37CONDITION, defaultText,  min, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc37);
            } else {
                curTemp -= offsetTemp;
                if (curTemp < minTemp) {
                    curTemp = minTemp;
                }
                parameter = String.format("%.1f",curTemp);
                setTemperature(curTemp);
                //text = "温度已降到" + curTemp + "度";
                resText = mContext.getString(R.string.acC36);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC36CONDITION, defaultText,  num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc36);
            }
        } else {
            //电动空调
            int curGear = getCurGear();
            int minGear = getMinGear();
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                //curGear = getCurGear();
                curGear -= offset;
                if (curGear < minGear) {
                    curGear = minGear;
                }
                //text = "已为你打开空调，温度降低到" + curGear + "档";
                setTempGear(curGear);
                resText = mContext.getString(R.string.acC35);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC35CONDITION, defaultText,  num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc35);
            } else if ((curGear == minGear) || (curGear - offset) <= minGear) {
                //text = "温度已降到最低，现在是" + curGear + "档";
                if(curGear != minGear) setTempGear(minGear);
                resText = mContext.getString(R.string.acC39);
                defaultText = String.format(resText, minGear);
                getMessageWithTtsSpeak(ACC39CONDITION, defaultText, min, minGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc39);
            } else {
                curGear -= offset;
                if (curGear < minGear) {
                    curGear = minGear;
                }
                setTempGear(curGear);
                //text = "温度已降低到" + curGear + "档";
                resText = mContext.getString(R.string.acC38);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC38CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc38);
            }
        }
    }


    /**
     * 设置固定档位
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void setGearValue(boolean isAutoAir, boolean isOpen, int offset) {
        String text = "";
        String parameter = "";
        if (isAutoAir) {
            float maxTemp = getMaxTemperature();
            float minTemp = getMinTemperature();
            float curTemp;
            float offsetTemp = getTempByGear(offset);
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
            }
            if (offsetTemp > maxTemp) {
                curTemp = maxTemp;
                parameter = String.format("%.1f",curTemp);
                //text = "超出温度范围，现在是最高温度，" + curTemp + "度";
                resText = mContext.getString(R.string.acC54);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC54CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc54);
            } else if (offsetTemp < minTemp) {
                curTemp = minTemp;
                parameter = String.format("%.1f",curTemp);
                //text = "超出温度范围，现在是最低温度，" + curTemp + "度";
                resText = mContext.getString(R.string.acC55);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC55CONDITION, defaultText, min, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc55);
            } else {
                curTemp = offsetTemp;
                parameter = String.format("%.1f",curTemp);
                if (!isOpen) {
                    //text = "温度已调到" + curTemp + "度";
                    resText = mContext.getString(R.string.acC58);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC58CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc58);
                } else {
                    //text = "已为你打开空调，并将温度调到" + curTemp + "度";
                    resText = mContext.getString(R.string.acC60);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC60CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc60);
                }
            }
            setTemperature(curTemp);
        } else {
            //电动空调
            int maxGear = getMaxGear();
            int minGear = getMinGear();
            int curGear;
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
            }
            if (offset > maxGear) {
                curGear = maxGear;
                resText = mContext.getString(R.string.acC56);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC56CONDITION, defaultText, max, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc56);
            } else if (offset < minGear) {
                curGear = minGear;
                resText = mContext.getString(R.string.acC57);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC57CONDITION, defaultText, min, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc57);
            } else {
                curGear = offset;
                if (!isOpen) {
                    //text = "温度已调到" + curGear + "档";
                    resText = mContext.getString(R.string.acC59);
                    defaultText = String.format(resText, curGear);
                    getMessageWithTtsSpeak(ACC59CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc59);
                } else {
                    //text = "已为你打开空调，并将温度调到" + curGear + "档";
                    resText = mContext.getString(R.string.acC61);
                    defaultText = String.format(resText, curGear);
                    getMessageWithTtsSpeak(ACC61CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc61);
                }
            }
            setTempGear(curGear);
        }
    }

    /**
     * 根据温度获取档位
     */
    private int getGearByTemp(float temp) {
        int gear;
        float minTemp = getMinTemperature();
        if (temp == 25) {
            gear = 8;
        } else if (temp < 25) {
            gear = (int) (temp - minTemp + 1);
        } else {
            gear = (int) (temp - minTemp + 2);
        }
        return gear;
    }


    /**
     * 根据档位获取温度
     */
    private float getTempByGear(int gear) {
        float temp;
        if (gear == 8 || gear == 9) {
            temp = 25;
        } else if (gear < 8) {
            temp = 17 + gear;
        } else {
            temp = 16 + gear;
        }
        return temp;
    }


    /**
     * 空调是否打开
     *
     * @return
     */
    private boolean isAirOpen() {
        if (CarUtils.airStatus == HVAC_ON) {
            return true;
        }else {
            return false;
        }
    }

    /**
     * 更改空调状态
     */
    private void changeAirStatus(int status) {
        Message msg = new Message();
        msg.what = AC_POWER_MSG;
        msg.obj = status;
        handler.sendMessageDelayed(msg,0);
    }

    private void changeAirStatusDelayed(int status) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.
                    setIntProperty(ID_HVAC_POWER_ON, HVAC_ALL, status);
            Log.i(TAG, "---------lh-----更改开关空调状态---" + status);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----更改开关空调状态CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * 更改压缩机状态
     *
     * @param status
     */
    private void changeACStatus(int status) {
        Message msg = new Message();
        msg.what = AC_MSG;
        msg.obj = status;
        handler.sendMessageDelayed(msg,600);
    }

    private void changeACStatusDelayed(int status) {
        int acStatus = getACStatus();
        if (acStatus != status) {
            try {
                //TODO: 待确认 ID_HVAC_AC_ON
                AppConfig.INSTANCE.mCarHvacManager.
                        setIntProperty(ID_HVAC_AC_ON, SEAT_ROW_1_LEFT, status);
                Log.d(TAG, "更改压缩机状态" + status);
            } catch (CarNotConnectedException e) {
                Log.d(TAG, "更改压缩机状态CarNotConnectedException" + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取压缩机状态
     */
    private int getACStatus() {
        return CarUtils.acStatus;
    }

    /**
     * 获取当前温度
     *
     * @return
     */
    private float getTemperature() {
        Log.d(TAG, "currentTemp = " + CarUtils.currentTemp);
        return CarUtils.currentTemp;
    }

    /**
     * 设置温度
     *
     * @return
     */
    private void setTemperature(float temp) {
        if (temp < 18) {
            temp=17.5f;
        }
        if (temp > 32) {
            temp=32.5f;
        }
        Message msg = new Message();
        msg.what = TEAMP_MSG;
        msg.obj = temp;
        handler.sendMessageDelayed(msg,400);
    }

    private void setTemperatureDelayed(float temp) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.setFloatProperty(ID_HVAC_TEMPERATURE_SET, SEAT_ROW_1_LEFT, temp);
            Log.i(TAG, "---------lh-----设置温度---" + temp);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----设置温度CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * 自动空调获取最高温度
     *
     * @return
     */
    private float getMaxTemperature() {
        return MAX_TEMP_AUTO_AC;
    }

    /**
     * 获取最低温度
     *
     * @return
     */
    private float getMinTemperature() {
        return MIN_TEMP_AUTO_AC;
    }


    /**
     * TODO 需要填充具体方法
     * 电动空调获取当前档位
     *
     * @return
     */
    private int getCurGear() {
        Log.d(TAG, "currentGear = " + CarUtils.currentGear);
        return CarUtils.currentGear;
    }

    /**
     * TODO 需要填充具体方法
     * 电动空调获取最高档位（1-16）
     *
     * @return
     */
    private int getMaxGear() {
        return MAX_TEMP_ELEC_AC;
    }

    /**
     * TODO 需要填充具体方法
     * 电动空调获取最低档位（1-16）
     *
     * @return
     */
    private int getMinGear() {
        return MIN_TEMP_ELEC_AC;
    }

    /**
     * TODO 需要填充具体方法
     * 设置温度档位
     *
     * @param temp
     */
    private void setTempGear(int temp) {
        Message msg = new Message();
        msg.what = GEAR_MSG;
        msg.obj = temp;
        handler.sendMessageDelayed(msg,400);
    }

    private void setTempGearDelayed(int temp) {
        int newTemp = gearTemp(temp);
        try {
            AppConfig.INSTANCE.mCarHvacManager.setIntProperty(
                    ID_HVAC_TEMPERATURE_LV_SET,
                    SEAT_ROW_1_LEFT, newTemp);
            Log.i(TAG, "---------lh-----设置空调当前档位---" + temp);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----设置空调当前档位CarNotConnectedException---" + e);
        }
    }

    private int gearTemp(int temp){
        int gearTempLabel = 1;
        switch (temp){
            case 1:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_1;
                break;
            case 2:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_2;
                break;
            case 3:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_3;
                break;
            case 4:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_4;
                break;
            case 5:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_5;
                break;
            case 6:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_6;
                break;
            case 7:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_7;
                break;
            case 8:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_8;
                break;
            case 9:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_9;
                break;
            case 10:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_10;
                break;
            case 11:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_11;
                break;
            case 12:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_12;
                break;
            case 13:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_13;
                break;
            case 14:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_14;
                break;
            case 15:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_15;
                break;
            case 16:
                gearTempLabel = HVAC.HVAC_TEMP_LV_LEVEL_16;
                break;
        }
        return  gearTempLabel;
    }
    /**
     * 获取当前风量
     *
     * @return
     */
    private int getFanSpeed() {
        Log.d(TAG, "currentFanSpeed = " + CarUtils.currentFanSpeed);
        return CarUtils.currentFanSpeed;

    }

    /**
     * 设置风量
     *
     * @param fanSpeed
     */
    private void setFanSpeed(int fanSpeed) {
        Message msg = new Message();
        msg.what = FAN_SPEED_MSG;
        msg.obj = fanSpeed;
        handler.sendMessageDelayed(msg,300);
    }

    private void setFanSpeedDelyed(int fanSpeed) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.setIntProperty(
                    ID_HVAC_FAN_SPEED_ADJUST,
                    SEAT_ROW_1_LEFT, fanSpeed);
            Log.i(TAG, "---------lh-----设置风量---" + fanSpeed);
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "Failed to get HVAC int property", e);
        }
    }

    /**
     * 设置循环模式
     *
     * @param circleMode
     */
    private void changeCircleMode(int circleMode) {
        Message msg = new Message();
        msg.what = CIRCLE_MODE_MSG;
        msg.obj = circleMode;
        handler.sendMessageDelayed(msg,200);
    }

    public void changeCircleModeDelayed(int circleMode, String mTAG) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.setIntProperty
                    (ID_HVAC_RECIRC_ON,
                            SEAT_ROW_1_LEFT, circleMode);
            Log.i(mTAG, "---------lh-----设置循环模式---" + circleMode);
        } catch (CarNotConnectedException e) {
            Log.i(mTAG, "---------lh-----设置循环模式CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * 设置空调出风模式
     *
     * @param airflowDirection
     */
    private void changeAirFlowDirection(int airflowDirection) {
        Message msg = new Message();
        msg.what = AIR_FLOW_MSG;
        msg.obj = airflowDirection;
        handler.sendMessageDelayed(msg,200);
    }

    private void changeAirFlowDirectionDelayed(int airflowDirection) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.setIntProperty(ID_HVAC_FAN_DIRECTION,
                    SEAT_ROW_1_LEFT,
                    airflowDirection);
            Log.i(TAG, "---------lh-----设置空调出风模式---" + airflowDirection);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----设置空调出风模式CarNotConnectedException---" + e);
        }
    }

    /**
     * 更改前除霜状态
     */
    private void changeFrontDefrost(int status) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.
                    setIntProperty(ID_HVAC_DEFROSTER,
                            WINDOW_FRONT_WINDSHIELD, status);
            Log.i(TAG, "---------lh-----更改前除霜状态---" + status);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----更改前除霜状态CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * 更改后除霜状态
     *
     * @param status
     */
    public void changeRearDefrost(int status) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.
                    setIntProperty(ID_HVAC_DEFROSTER,
                            WINDOW_REAR_WINDSHIELD, status);
            Log.i(TAG, "---------lh-----更改后除霜状态---" + status);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----更改后除霜状态CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    private int getIgnitionStatus() {
        return CarUtils.powerStatus;
    }

    private String transformParam(String param){
        if(param.contains(".0")){
            param = param.replace(".0","");
        }else if(param.contains(". 0")){
            param = param.replace(". 0","");
        }
        return param;
    }

    private void sendBroadcastToACController(){
        mContext.sendBroadcast(new Intent(AppConstant.VOICE_ACTION));
        Log.d(TAG, "called sendBroadcastToACController....");
    }

    /*
    *  获取后除霜技能所对应的name
     */
    private String getNameValue(IntentEntity intentEntity){
        if(POST_DEFROST1.equals(intentEntity.semantic.slots.mode) ||  "后除雾".equals(intentEntity.semantic.slots.mode) ||
                "除霜".equals(intentEntity.semantic.slots.mode)){
            return POST_DEFROST1;
        }else {
            return "外后视镜加热";
        }
    }
}
