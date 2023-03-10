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

    //?????????
    private final static String MODE_RECYCLE_IN = "?????????";
    //?????????
    private final static String MODE_RECYCLE_OUT = "?????????";
    //????????????
    private final static String MODE_RECYCLE_AUTO = "????????????";

    // ??????
    private final static String MODE_DEFROST = "??????";

    //????????????
    private final static String MODE_COLD = "??????";
    //????????????
    private final static String MODE_HOT = "??????";

    //?????????
    private final static String MINUS_MORE = "MINUS_MORE";
    //?????????
    private final static String MINUS_LITTLE = "MINUS_LITTLE";
    // ?????????
    private final static String PLUS_MORE = "PLUS_MORE";
    //?????????
    private final static String PLUS_LITTLE = "PLUS_LITTLE";
    // ????????????/????????????
    private final static String PLUS = "PLUS";
    // ????????????/????????????
    private final static String MINUS = "MINUS";
    // ??????????????????
    private final static String MEDIUM = "MEDIUM";

    // ????????????/????????????
    private final static String MAX = "MAX";
    // ????????????
    private final static String MIN = "MIN";

    //??????
    private final static String WIND_CENTER = "??????";

    // ??????????????????
    private final static String REF_CUR = "CUR";

    private final static String REF_ZERO = "ZERO";

    //???
    private final static String AIR_FLOW_FACE = "???";
    //???
    private final static String AIR_FLOW_FOOT = "???";
    //????????????
    private final static String AIR_FLOW_FACE_FOOT = "????????????";

    private final static String FRONT_DEFROST = "?????????";

    private final static String REAR_DEFROST = "?????????";
    private final static String POST_DEFROST1 = "?????????";
    private final static String POST_DEFROST2 = "????????????";
    private final static String POST_DEFROST3 = "???????????????";
    private String resText = "";

    private String defaultText = "";

    //????????????????????????
    private final int MIDDLE_TEMP_AUTO_AC = 25;

    //????????????????????????
    private final int MIDDLE_TEMP_ELEC_AC = 8;

    //????????????????????????
    private final int MAX_TEMP_AUTO_AC = 33;

    //????????????????????????
    private final int MIN_TEMP_AUTO_AC = 17;

    //????????????????????????
    private final int MAX_TEMP_ELEC_AC = 16;

    //????????????????????????
    private final int MIN_TEMP_ELEC_AC = 1;

    //??????????????????
    private final int MAX_WIND_AC = 8;

    //??????????????????
    private final int MIN_WIND_AC = 1;
    //???????????????
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
                case FAN_SPEED_UP_MSG://????????????
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
                case FAN_SPEED_DOWN_MSG://????????????
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
        //???????????????
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
                POST_DEFROST2.equals(intentEntity.semantic.slots.name) || "?????????".equals(intentEntity.semantic.slots.name) ||
                "?????????".equals(intentEntity.semantic.slots.mode) || "??????".equals(intentEntity.semantic.slots.mode))){//?????????
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
        //????????????????????????,auto:?????????elec?????????
        String acType = Utils.getProperty("persist.vendor.vehicle.hvac", "auto");
        Log.d(TAG, "lh:acType:" + acType);
        boolean isAutoAir = acType.equalsIgnoreCase("auto") ? true : false;
        //??????????????????????????????
        boolean isOpen = isAirOpen();
        Log.i(TAG, "isOpen = " + isOpen);
        sendBroadcastToACController();
        if (PlatformConstant.Operation.OPEN.equals(insType)) {
            //????????????
//            if (!isOpen) {
//                //????????????????????????,???????????????????????????,???????????????
//                changeAirStatus(HVAC_ON);
//            }


            if (isAutoAir) {
                //??????????????????????????????????????????????????????????????????????????????????????????1010??????
                changeAirStatus(HVAC_ON);
                handler.sendEmptyMessageDelayed(CHECK_TEMP_MSG,600);
            } else {
                if (!isOpen) {
                    //????????????????????????,???????????????????????????,???????????????
                    changeAirStatus(HVAC_ON);
                }
                //???????????????????????????????????????????????????????????????AC?????????1010??????
                changeACStatus(HVAC_ON);
                defaultText = mContext.getString(R.string.acC2);
                getMessageWithTtsSpeak(ACC2CONDITION, defaultText);
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc, R.string.object_acc1, ACC2CONDITION, R.string.condition_acc_elec);
            }
        } else if (PlatformConstant.Operation.CLOSE.equals(insType)) {
            //????????????
            //if (isOpen) {
                //????????????????????????,???????????????????????????,???????????????
                changeAirStatus(HVAC_OFF);
            //}
            getMessageWithTtsSpeak(ACC3CONDITION, mContext.getString(R.string.acC3));
            Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc, R.string.object_acc2, ACC3CONDITION, R.string.condition_default);
        } else if (MODE_COLD.equals(mode)) { //????????????
            // TODO ??????????????????????????????
            if (!isOpen) { //???????????????
                changeAirStatus(HVAC_ON);
                getMessageWithTtsSpeak(ACC4CONDITION, mContext.getString(R.string.acC4));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode1, ACC4CONDITION, R.string.condition_acc_close);
            } else {
                getMessageWithTtsSpeak(ACC5CONDITION, mContext.getString(R.string.acC5));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode1, ACC5CONDITION, R.string.condition_acc_open);
            }
            if (isAutoAir) { //????????????
                setTemperature(22.0f);
            } else { //????????????
                setTempGear(5);
            }
            changeACStatus(HVAC_ON);
//            int acStatus = getACStatus();
//            if (acStatus != HVAC_ON_REQ) {
//                changeACStatus(HVAC_ON);
//            }
        } else if (MODE_HOT.equals(mode)) { //????????????
            // TODO ??????????????????????????????
            if (!isOpen) {//???????????????
                changeAirStatus(HVAC_ON);
                //text = "????????????????????????";
                getMessageWithTtsSpeak(ACC6CONDITION,  mContext.getString(R.string.acC6));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode2, ACC6CONDITION, R.string.condition_acc_close);
            } else {
                //text = "?????????????????????????????????????????????";
                getMessageWithTtsSpeak(ACC7CONDITION,  mContext.getString(R.string.acC7));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode2, ACC7CONDITION, R.string.condition_acc_open);
            }
            if (isAutoAir) { //????????????
                setTemperature(28.0f);
            } else { //????????????
                setTempGear(12);
            }
            changeACStatus(HVAC_OFF);
//            int acStatus = getACStatus();
//            if (acStatus != HVAC_OFF_REQ) {
//                changeACStatus(HVAC_OFF);
//            }
        } else if (MINUS_MORE.equals(temperature) || MINUS_LITTLE.equals(temperature)) {
            //?????????
            if (isAutoAir) {
                //????????????
                float temp;
                if (isOpen) {
                    // TODO ????????????????????????
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
                    //text = "?????????????????????????????????" + temp + "???";
                    resText = mContext.getString(R.string.acC8);
                    defaultText = String.format(resText,  parameter);
                    setTemperature(temp);
                    getMessageWithTtsSpeak(ACC8CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_mode, R.string.object_acc_mode3, R.string.condition_acc8);
                }
                //????????????????????????AC
//                if(getACStatus() != HVAC_ON_REQ){
//                    changeACStatus(HVAC_ON_REQ);
//                }
            } else {
                // ????????????
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
            //?????????
            if (isAutoAir) {
                //????????????
                if (isOpen) {
                    // TODO ????????????????????????
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
                //????????????????????????AC
//                if(getACStatus() != HVAC_OFF_REQ){
//                    changeACStatus(HVAC_OFF_REQ);
//                }
            } else {
                // ????????????
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
            //????????????
            handleTempratureChange(temperature,isAutoAir,isOpen);
        } else if (MINUS.equals(temperature)) {
            //????????????
            handleTempratureChange(temperature,isAutoAir,isOpen);
        } else if (MEDIUM.equals(temperature)) {
            //??????????????????
            if (isAutoAir) {
                //????????????
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
                //????????????
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
            //????????????
            try {
                String ref = tempObj.getString("ref");
                float offset = tempObj.getInt("offset");
                BigDecimal b = new BigDecimal(offset);
                offset = b.setScale(1,BigDecimal.ROUND_DOWN).floatValue();
                String direct = tempObj.getString("direct");
                if (REF_CUR.equals(ref)) {
                    //???????????????????????????
                    if ("+".equals(direct)) {
                        Log.e("zheng","zheng ????????????-----------------???????????????"+offset);
                        upTempValue(isAutoAir, isOpen, offset);
                    } else if ("-".equals(direct)) {
                        Log.e("zheng","zheng ????????????-----------------???????????????"+offset);
                        downTempValue(isAutoAir, isOpen, offset);
                    }else{
                        doExceptonAction(mContext);
                    }
                } else if (REF_ZERO.equals(ref)) {
                    //?????????????????????
                    setTempValue(isAutoAir, isOpen, offset);
                    Log.e("zheng","zheng ????????????-----------------????????????????????????"+offset);
                }else{
                    doExceptonAction(mContext);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                doExceptonAction(mContext);
            }
        } else if (temperatureGearObj != null) {
            //????????????
            try {
                String ref = temperatureGearObj.getString("ref");
                int offset = temperatureGearObj.getInt("offset");
                String direct = temperatureGearObj.getString("direct");
                if (REF_CUR.equals(ref)) {
                    //???????????????????????????
                    if ("+".equals(direct)) {
                        upGearValue(isAutoAir, isOpen, offset);
                    } else if ("-".equals(direct)) {
                        downGearValue(isAutoAir, isOpen, offset);
                    }else{
                        doExceptonAction(mContext);
                    }
                } else if (REF_ZERO.equals(ref)) {
                    //?????????????????????
                    setGearValue(isAutoAir, isOpen, offset);
                } else {
                    doExceptonAction(mContext);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                doExceptonAction(mContext);
            }
        } else if (MAX.equals(temperature)) {
            // ????????????
            if (isOpen) {
                //text = "?????????????????????";
                getMessageWithTtsSpeak(ACC41CONDITION, mContext.getString(R.string.acC41));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_high, ACC41CONDITION, R.string.condition_acc41);
            } else {
                changeAirStatus(HVAC_ON);
                //text = "????????????????????????????????????????????????";
                getMessageWithTtsSpeak(ACC40CONDITION, mContext.getString(R.string.acC40));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_high, ACC40CONDITION, R.string.condition_acc40);
            }
            if (isAutoAir) {
                setTemperature(getMaxTemperature());
            } else {
                setTempGear(getMaxGear());
            }
        } else if (MIN.equals(temperature)) {
            //????????????
            if (isOpen) {
                //text = "?????????????????????????????????????????????";
                defaultText = mContext.getString(R.string.acC45);
                getMessageWithTtsSpeak(ACC45CONDITION, mContext.getString(R.string.acC45));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_low, ACC45CONDITION, R.string.condition_acc45);
            } else {
                changeAirStatus(HVAC_ON);
                //text = "????????????????????????????????????????????????";
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
            //????????????
            handleFanSpeedChange(fanSpeed,isAutoAir,isOpen);
        } else if ((MINUS.equals(fanSpeed)) || (("-").equals(directFanSpeed)) && REF_CUR.equals(refFanSpeed)) {
            fanSpeed = MINUS;
            //????????????
            handleFanSpeedChange(fanSpeed,isAutoAir,isOpen);
        } else if (fanSpeedObj != null) {
            //???????????????x???
            try {
                String ref = fanSpeedObj.getString("ref");
                int offset = fanSpeedObj.getInt("offset");
                if (REF_ZERO.equals(ref)) {
                    //?????????????????????
                    if (offset > MAX_WIND_AC) {
                        offset = MAX_WIND_AC;
                        //text = "????????????????????????????????????" + offset + "???";
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
            //????????????
            setFanSpeed(MAX_WIND_AC);
            getMessageWithTtsSpeak(ACC68CONDITION, mContext.getString(R.string.acC68));
            Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_high, ACC68CONDITION, R.string.condition_default);
        } else if (MIN.equals(fanSpeed)) {
            //????????????
            setFanSpeed(MIN_WIND_AC);
            getMessageWithTtsSpeak(ACC70CONDITION, mContext.getString(R.string.acC70));
            Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_low, ACC70CONDITION, R.string.condition_default);
        } else if (WIND_CENTER.equals(fanSpeed)) {
            //????????????
            setFanSpeed(MIDDLE_WIND_AC);
            //text = "???????????????" + 4 + "???";
            resText = mContext.getString(R.string.acC69);
            defaultText = String.format(resText, MIDDLE_WIND_AC);
            getMessageWithTtsSpeak(ACC69CONDITION,defaultText,middle,MIDDLE_WIND_AC + "",R.string.skill_acc, R.string.scene_acc_cloud, R.string.object_acc_cloud_middle, R.string.condition_default);
        } else if (MODE_RECYCLE_IN.equals(mode)) {
            //?????????
            if(operation != null && operation.equals(PlatformConstant.Operation.SET)){//???????????????
                changeCircleMode(LOOP_INNER);
                getMessageWithTtsSpeak(ACC74CONDITION, mContext.getString(R.string.acC74));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle1, ACC74CONDITION, R.string.condition_default);
            }else if(operation != null && operation.equals(PlatformConstant.Operation.CLOSE)){//??????????????? = ???????????????
                changeCircleMode(LOOP_OUTSIDE);
                getMessageWithTtsSpeak(ACC75CONDITION, mContext.getString(R.string.acC75));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle2, ACC75CONDITION, R.string.condition_default);
            }else {
                doExceptonAction(mContext);
            }
        } else if (MODE_RECYCLE_OUT.equals(mode)) {
            //?????????
            if(operation != null && operation.equals(PlatformConstant.Operation.SET)){//???????????????
                changeCircleMode(LOOP_OUTSIDE);
                getMessageWithTtsSpeak(ACC75CONDITION, mContext.getString(R.string.acC75));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle2, ACC75CONDITION, R.string.condition_default);
            }else if(operation != null && operation.equals(PlatformConstant.Operation.CLOSE)){//??????????????? = ???????????????
                changeCircleMode(LOOP_INNER);
                getMessageWithTtsSpeak(ACC74CONDITION, mContext.getString(R.string.acC74));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle1, ACC74CONDITION, R.string.condition_default);
            }else {
                doExceptonAction(mContext);
            }
        } else if (MODE_RECYCLE_AUTO.equals(mode)) {
            //????????????
            //TODO ????????????????????????
            boolean supportAutoMode = false;
            if (supportAutoMode) {
                //TODO ??????????????????
                //text = "???????????????????????????";
                getMessageWithTtsSpeak(ACC77CONDITION, mContext.getString(R.string.acC77));
                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_circle, R.string.object_acc_circle3, ACC77CONDITION, R.string.condition_acc77);
            } else {
                //text = "??????????????????????????????";
                //text = "???????????????????????????";
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
        } else if (MODE_DEFROST.equals(mode) && (null != intentEntity.semantic.slots.airflowDirection)) {//????????????
            //???????????????airflowDirection??? ??????????????????,?????????????????????????????????????????? mode??????.
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
            //??????
            if(operation == null || (operation != null && !operation.equals(PlatformConstant.Operation.SET))){
                doExceptonAction(mContext);
            }else {
                if (!isOpen) {
                    changeAirStatus(HVAC_ON);
                    getMessageWithTtsSpeak(ACC78CONDITION, mContext.getString(R.string.acC78));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction1, ACC78CONDITION, R.string.condition_acc_close);
                } else {
                    //text = "?????????????????????";
                    getMessageWithTtsSpeak(ACC79CONDITION, mContext.getString(R.string.acC79));
                    Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_flow_direction, R.string.object_acc_flow_direction1, ACC79CONDITION, R.string.condition_acc_open);
                }
                changeAirFlowDirection(HVAC_FAN_DIRECTION_FACE);
            }
        } else if (AIR_FLOW_FOOT.equals(intentEntity.semantic.slots.airflowDirection)) {
            //??????
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
            //????????????
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
        } else if (FRONT_DEFROST.equals(intentEntity.semantic.slots.mode) || (MODE_DEFROST.equals(mode) && intentEntity.text.contains("?????????"))) {
            //?????????
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
//            //?????????
//            if (PlatformConstant.Operation.SET.equals(intentEntity.operation)) {
//                changeRearDefrost(HVAC_ON_REQ);
//                getMessageWithTtsSpeak(ACC88CONDITION, mContext.getString(R.string.acC88));
//                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_rear_defrost, R.string.object_acc_rear_defrost1, ACC88CONDITION, R.string.condition_default);
//
//            } else if (PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) {
//                //text = "????????????????????????";
//                changeRearDefrost(HVAC_OFF_REQ);
//                getMessageWithTtsSpeak(ACC89CONDITION, mContext.getString(R.string.acC89));
//                Utils.eventTrack(mContext,R.string.skill_acc, R.string.scene_acc_rear_defrost, R.string.object_acc_rear_defrost2, ACC89CONDITION, R.string.condition_default);
//            }else {
//                doExceptonAction(mContext);
//            }
//        }
        else if(POST_DEFROST1.equals(intentEntity.semantic.slots.mode) || POST_DEFROST3.equals(intentEntity.semantic.slots.mode) ||
                POST_DEFROST2.equals(intentEntity.semantic.slots.name) || "?????????".equals(intentEntity.semantic.slots.name) ||
                "?????????".equals(intentEntity.semantic.slots.mode) || "??????".equals(intentEntity.semantic.slots.mode)){//?????????????????????
            int postDefrostStatus = CarUtils.getInstance(mContext).getPostDefrostStatus();
            String name = getNameValue(intentEntity);
            Log.d(TAG, "postDefrostStatus = " + postDefrostStatus + ",name = " + name);
            if(false){//????????? ????????????????????????
                getMessageWithoutTtsSpeak(TtsConstant.DEFC5CONDITION,mContext.getString(R.string.defC5),"#NAME#",name,
                        R.string.skill_post_defrost,R.string.scene_post_defrost_error,R.string.object_post_defrost_less,R.string.condition_defC5);
            }else if(postDefrostStatus == VEHICLE.INVALID){//???????????????
                getMessageWithoutTtsSpeak(TtsConstant.DEFC6CONDITION,mContext.getString(R.string.defC6),"#NAME#",name,
                        R.string.skill_post_defrost,R.string.scene_post_defrost_error,R.string.object_post_defrost_other_error,R.string.condition_defC6);
            }else if(postDefrostStatus == VEHICLE.ON) {//?????????
                if(PlatformConstant.Operation.OPEN.equals(intentEntity.operation) || PlatformConstant.Operation.SET.equals(intentEntity.operation)){//??????
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC1CONDITION,mContext.getString(R.string.defC1),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_on,R.string.condition_defC1);
                }else if(PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)){//??????
                    AirController.getInstance(mContext).changeRearDefrost(HVAC_OFF_REQ);
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC4CONDITION,mContext.getString(R.string.defC4),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_off,R.string.condition_defC4);
                }else {
                    doExceptonAction(mContext);
                }
            }else if(postDefrostStatus == VEHICLE.OFF){//?????????
                if(PlatformConstant.Operation.OPEN.equals(intentEntity.operation) || PlatformConstant.Operation.SET.equals(intentEntity.operation)){//??????
                    AirController.getInstance(mContext).changeRearDefrost(HVAC_ON_REQ);
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC2CONDITION,mContext.getString(R.string.defC2),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_on,R.string.condition_defC2);
                }else if(PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)){//??????
                    getMessageWithoutTtsSpeak(TtsConstant.DEFC3CONDITION,mContext.getString(R.string.defC3),"#NAME#",name,
                            R.string.skill_post_defrost,R.string.scene_post_defrost_settings,R.string.object_post_defrost_off,R.string.condition_defC3);
                }else {
                    doExceptonAction(mContext);
                }
            }else {
                doExceptonAction(mContext);
            }
        } else {
            //????????????????????????
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

    //??????tts??????,??????????????????,??????
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

    //??????tts??????,??????????????????,??????
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

                    if("???????????????".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc40,TtsConstant.MHXC40CONDITION,R.string.condition_null, defaultTts);
                    }else if("???????????????".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc41,TtsConstant.MHXC41CONDITION,R.string.condition_null, defaultTts);
                    }else if("???????????????".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc42,TtsConstant.MHXC42CONDITION,R.string.condition_null, defaultTts);
                    }else if("???????????????".equals(DatastatManager.primitive)){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc43,TtsConstant.MHXC43CONDITION,R.string.condition_null, defaultTts);
                    }else
                        Utils.eventTrack(mContext,appName,scene,object, conditionId, condition, defaultTts);//??????

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
                Utils.eventTrack(mContext, appName, scene, object, conditionId, condition, defaultTts);//??????
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

    //???????????????????????????
    private void handleTempratureChange(String temperature,boolean isAutoAir,boolean isOpen){
        String parameter = "";
        if(PLUS.equals(temperature)){
            //????????????
            if (isAutoAir) {
                //????????????
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
                //????????????
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
            // ????????????
            if (isAutoAir) {
                //????????????
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
                //????????????
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

    //???????????????????????????
    private void handleFanSpeedChange(String fanSpeed,boolean isAutoAir,boolean isOpen){
        String parameter = "";
        if(PLUS.equals(fanSpeed)){
            //????????????
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
                  if("???????????????".equals(DatastatManager.primitive)){
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
            //????????????
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

                    if("???????????????".equals(DatastatManager.primitive)){
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
            if("???????????????".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc40,TtsConstant.MHXC40CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }else if("???????????????".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc41,TtsConstant.MHXC41CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }else if("???????????????".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc42,TtsConstant.MHXC42CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }else if("???????????????".equals(mvwLParamEntity.nKeyword)){
                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_air,R.string.object_mhcc43,TtsConstant.MHXC43CONDITION,R.string.condition_null, mContext.getString(R.string.ac_c90));
            }
            return;
        }
        //????????????????????????,auto:?????????elec?????????
        String acType = Utils.getProperty("persist.vendor.vehicle.hvac", "auto");
        Log.d(TAG, "lh:acType:" + acType);
        boolean isAutoAir = acType.equalsIgnoreCase("auto") ? true : false;
        //??????????????????????????????
        boolean isOpen = isAirOpen();
        Log.i(TAG, "isOpen = " + isOpen);
        sendBroadcastToACController();
        if("???????????????".equals(mvwLParamEntity.nKeyword)){
            handleTempratureChange(PLUS,isAutoAir,isOpen);
        }else if("???????????????".equals(mvwLParamEntity.nKeyword)){
            handleTempratureChange(MINUS,isAutoAir,isOpen);
        }else if("???????????????".equals(mvwLParamEntity.nKeyword)){
            handleFanSpeedChange(PLUS,isAutoAir,isOpen);
        }else if("???????????????".equals(mvwLParamEntity.nKeyword)){
            handleFanSpeedChange(MINUS,isAutoAir,isOpen);
        }
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }


    /**
     * ?????????????????????
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void upTempValue(boolean isAutoAir, boolean isOpen, float offset) {
        String text = "";
        String parameter ="";
        //???????????????????????????
        if (isAutoAir) {
            //????????????
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
                //text = "???????????????????????????????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC22);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC22CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc22);
            } else if ((curTemp == maxTemp) || (curTemp + offset) >= maxTemp) {
                //text = "??????????????????????????????" + maxTemp + "???";
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
                //text = "??????????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC24);
                parameter = String.format("%.1f",curTemp);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC24CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc24);
            }
        } else {
            //????????????
            int curGear = getCurGear();
            int maxGear = getMaxGear();
            //int offsetGear = getGearByTemp(offset);
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                curGear += offset;
                if (curGear > maxGear) {
                    curGear = maxGear;
                }
                //text = "???????????????????????????????????????" + curGear + "???";
                resText = mContext.getString(R.string.acC23);
                defaultText = String.format(resText, curGear);
                setTempGear(curGear);
                getMessageWithTtsSpeak(ACC23CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc23);
            } else if ((curGear == maxGear) || (curGear + offset) >= maxGear) {
                //text = "?????????????????????????????????" + curGear + "???";
                if(curGear != maxGear) setTempGear(maxGear);
                resText = mContext.getString(R.string.acC27);
                defaultText = String.format(resText, maxGear);
                getMessageWithTtsSpeak(ACC27CONDITION, defaultText, max, maxGear + "", R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc27);
            } else {
                curGear += offset;
                if (curGear > maxGear) {
                    curGear = maxGear;
                }
                //text = "??????????????????" + curGear + "???";
                resText = mContext.getString(R.string.acC26);
                defaultText = String.format(resText, curGear);
                setTempGear(curGear);
                getMessageWithTtsSpeak(ACC26CONDITION, defaultText,  num, curGear + "", R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc26);
            }

        }
    }

    /**
     * ?????????????????????
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void downTempValue(boolean isAutoAir, boolean isOpen, float offset) {
        String text = "";
        String parameter ="";
        //???????????????????????????
        if (isAutoAir) {
            //????????????
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
                //text = "???????????????????????????????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC34);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC34CONDITION, defaultText,  num, parameter, R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc34);
            } else if ((curTemp == minTemp) || (curTemp - offset) <= minTemp) {
                //text = "??????????????????????????????" + curTemp + "???";
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
                //text = "???????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC36);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC36CONDITION, defaultText,  num, parameter, R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc36);
            }
        } else {
            //????????????
            int curGear = getCurGear();
            int minGear = getMinGear();
            //int offsetGear = getGearByTemp(offset);
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                curGear -= offset;
                if (curGear < minGear) {
                    curGear = minGear;
                }
                //text = "???????????????????????????????????????" + curGear + "???";
                resText = mContext.getString(R.string.acC35);
                defaultText = String.format(resText, curGear);
                setTempGear(curGear);
                getMessageWithTtsSpeak(ACC35CONDITION, defaultText,  num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc35);
            } else if ((curGear == minGear) || (curGear - offset) <= minGear) {
                //text = "?????????????????????????????????" + curGear + "???";
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
     * ???????????????????????????
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
                //text = "?????????????????????????????????????????????" + maxTemp + "???";
                resText = mContext.getString(R.string.acC46);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC46CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc46);
            } else if (offset < minTemp) {
                curTemp = minTemp;
                parameter = String.format("%.1f",minTemp);
                //text = "?????????????????????????????????????????????" + minTemp + "???";
                resText = mContext.getString(R.string.acC47);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC47CONDITION, defaultText, min, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc47);
            } else {
                curTemp = offset;
                parameter = String.format("%.1f",offset);

                if (!isOpen) {
                    //text = "???????????????" + curTemp + "???";
                    resText = mContext.getString(R.string.acC50);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC50CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc50);
                } else {
                    //text = "??????????????????????????????????????????" + curTemp + "???";
                    resText = mContext.getString(R.string.acC52);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC52CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc52);
                }
            }
            setTemperature(curTemp);
        } else {
            //????????????
            float maxTemp = getMaxTemperature();
            float minTemp = getMinTemperature();
            int curGear;
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
            }
            if (offset >= maxTemp) {
                curGear = getMaxGear();
                //text = "??????????????????????????????????????????????????????????????????" + curGear + "???";
                resText = mContext.getString(R.string.acC48);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC48CONDITION, defaultText, max, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x, R.string.condition_acc48);
            } else if (offset < minTemp) {
                curGear = getMinGear();
                //text = "??????????????????????????????????????????????????????????????????" + curGear + "???";
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
                    //text = "???????????????????????????????????????????????????" + curGear + "???";
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
     * ?????????????????????
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void upGearValue(boolean isAutoAir, boolean isOpen, int offset) {
        String text = "";
        String parameter ="";
        if (isAutoAir) {
            //????????????
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
                //text = "???????????????????????????????????????" + temp + "???";
                resText = mContext.getString(R.string.acC22);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC22CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc22);
            } else if ((curTemp == maxTemp) || (curTemp + offset) >= maxTemp) {
                //text = "??????????????????????????????" + maxTemp + "???";
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
                //text = "??????????????????" + temp + "???";
                resText = mContext.getString(R.string.acC24);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC24CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_incr1, R.string.condition_acc24);
            }
        } else {
            //????????????
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
     * ?????????????????????
     *
     * @param isAutoAir
     * @param isOpen
     * @param offset
     */
    private void downGearValue(boolean isAutoAir, boolean isOpen, int offset) {
        String text = "";
        String parameter = "";
        if (isAutoAir) {
            //????????????
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
                //text = "???????????????????????????????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC34);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC34CONDITION, defaultText,  num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc34);
            } else if ((curTemp == minTemp) || (curTemp - offsetTemp) <= minTemp) {
                //text = "??????????????????????????????" + curTemp + "???";
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
                //text = "???????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC36);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC36CONDITION, defaultText,  num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc36);
            }
        } else {
            //????????????
            int curGear = getCurGear();
            int minGear = getMinGear();
            if (!isOpen) {
                changeAirStatus(HVAC_ON);
                //curGear = getCurGear();
                curGear -= offset;
                if (curGear < minGear) {
                    curGear = minGear;
                }
                //text = "???????????????????????????????????????" + curGear + "???";
                setTempGear(curGear);
                resText = mContext.getString(R.string.acC35);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC35CONDITION, defaultText,  num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc35);
            } else if ((curGear == minGear) || (curGear - offset) <= minGear) {
                //text = "?????????????????????????????????" + curGear + "???";
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
                //text = "??????????????????" + curGear + "???";
                resText = mContext.getString(R.string.acC38);
                defaultText = String.format(resText, curGear);
                getMessageWithTtsSpeak(ACC38CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_decr1, R.string.condition_acc38);
            }
        }
    }


    /**
     * ??????????????????
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
                //text = "?????????????????????????????????????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC54);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC54CONDITION, defaultText, max, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc54);
            } else if (offsetTemp < minTemp) {
                curTemp = minTemp;
                parameter = String.format("%.1f",curTemp);
                //text = "?????????????????????????????????????????????" + curTemp + "???";
                resText = mContext.getString(R.string.acC55);
                defaultText = String.format(resText, parameter);
                getMessageWithTtsSpeak(ACC55CONDITION, defaultText, min, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc55);
            } else {
                curTemp = offsetTemp;
                parameter = String.format("%.1f",curTemp);
                if (!isOpen) {
                    //text = "???????????????" + curTemp + "???";
                    resText = mContext.getString(R.string.acC58);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC58CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc58);
                } else {
                    //text = "??????????????????????????????????????????" + curTemp + "???";
                    resText = mContext.getString(R.string.acC60);
                    defaultText = String.format(resText, parameter);
                    getMessageWithTtsSpeak(ACC60CONDITION, defaultText, num, parameter,R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc60);
                }
            }
            setTemperature(curTemp);
        } else {
            //????????????
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
                    //text = "???????????????" + curGear + "???";
                    resText = mContext.getString(R.string.acC59);
                    defaultText = String.format(resText, curGear);
                    getMessageWithTtsSpeak(ACC59CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc59);
                } else {
                    //text = "??????????????????????????????????????????" + curGear + "???";
                    resText = mContext.getString(R.string.acC61);
                    defaultText = String.format(resText, curGear);
                    getMessageWithTtsSpeak(ACC61CONDITION, defaultText, num, curGear + "",R.string.skill_acc, R.string.scene_acc_tmp, R.string.object_acc_tmp_x1, R.string.condition_acc61);
                }
            }
            setTempGear(curGear);
        }
    }

    /**
     * ????????????????????????
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
     * ????????????????????????
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
     * ??????????????????
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
     * ??????????????????
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
            Log.i(TAG, "---------lh-----????????????????????????---" + status);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----????????????????????????CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????
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
                //TODO: ????????? ID_HVAC_AC_ON
                AppConfig.INSTANCE.mCarHvacManager.
                        setIntProperty(ID_HVAC_AC_ON, SEAT_ROW_1_LEFT, status);
                Log.d(TAG, "?????????????????????" + status);
            } catch (CarNotConnectedException e) {
                Log.d(TAG, "?????????????????????CarNotConnectedException" + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * ?????????????????????
     */
    private int getACStatus() {
        return CarUtils.acStatus;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private float getTemperature() {
        Log.d(TAG, "currentTemp = " + CarUtils.currentTemp);
        return CarUtils.currentTemp;
    }

    /**
     * ????????????
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
            Log.i(TAG, "---------lh-----????????????---" + temp);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----????????????CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    private float getMaxTemperature() {
        return MAX_TEMP_AUTO_AC;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private float getMinTemperature() {
        return MIN_TEMP_AUTO_AC;
    }


    /**
     * TODO ????????????????????????
     * ??????????????????????????????
     *
     * @return
     */
    private int getCurGear() {
        Log.d(TAG, "currentGear = " + CarUtils.currentGear);
        return CarUtils.currentGear;
    }

    /**
     * TODO ????????????????????????
     * ?????????????????????????????????1-16???
     *
     * @return
     */
    private int getMaxGear() {
        return MAX_TEMP_ELEC_AC;
    }

    /**
     * TODO ????????????????????????
     * ?????????????????????????????????1-16???
     *
     * @return
     */
    private int getMinGear() {
        return MIN_TEMP_ELEC_AC;
    }

    /**
     * TODO ????????????????????????
     * ??????????????????
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
            Log.i(TAG, "---------lh-----????????????????????????---" + temp);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----????????????????????????CarNotConnectedException---" + e);
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
     * ??????????????????
     *
     * @return
     */
    private int getFanSpeed() {
        Log.d(TAG, "currentFanSpeed = " + CarUtils.currentFanSpeed);
        return CarUtils.currentFanSpeed;

    }

    /**
     * ????????????
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
            Log.i(TAG, "---------lh-----????????????---" + fanSpeed);
        } catch (CarNotConnectedException e) {
            Log.e(TAG, "Failed to get HVAC int property", e);
        }
    }

    /**
     * ??????????????????
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
            Log.i(mTAG, "---------lh-----??????????????????---" + circleMode);
        } catch (CarNotConnectedException e) {
            Log.i(mTAG, "---------lh-----??????????????????CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * ????????????????????????
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
            Log.i(TAG, "---------lh-----????????????????????????---" + airflowDirection);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----????????????????????????CarNotConnectedException---" + e);
        }
    }

    /**
     * ?????????????????????
     */
    private void changeFrontDefrost(int status) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.
                    setIntProperty(ID_HVAC_DEFROSTER,
                            WINDOW_FRONT_WINDSHIELD, status);
            Log.i(TAG, "---------lh-----?????????????????????---" + status);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----?????????????????????CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????
     *
     * @param status
     */
    public void changeRearDefrost(int status) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.
                    setIntProperty(ID_HVAC_DEFROSTER,
                            WINDOW_REAR_WINDSHIELD, status);
            Log.i(TAG, "---------lh-----?????????????????????---" + status);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----?????????????????????CarNotConnectedException---" + e);
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
    *  ?????????????????????????????????name
     */
    private String getNameValue(IntentEntity intentEntity){
        if(POST_DEFROST1.equals(intentEntity.semantic.slots.mode) ||  "?????????".equals(intentEntity.semantic.slots.mode) ||
                "??????".equals(intentEntity.semantic.slots.mode)){
            return POST_DEFROST1;
        }else {
            return "??????????????????";
        }
    }
}
