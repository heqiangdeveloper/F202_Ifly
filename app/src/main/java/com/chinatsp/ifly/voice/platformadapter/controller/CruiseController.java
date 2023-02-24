package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.sr.SRAgent;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2020/6/12.
 */

public class CruiseController extends BaseController{
    private final String TAG = "CruiseController";
    private Context mContext;
    private static CruiseController mCruiseController;
    private final int SHOW_MSG = 1001;
    private final int TIME_DELAY_SHOWING = 600;
    private final int HIDE_ASSISTANT = 1002;
    private final int SPEAKCCC14 = 1003;
    private final int SPEAKCCC15 = 1004;
    private String conditionId,defaultText;
    private int resId = 0;
    private int condition = 0;
    private SpannableStringBuilder builder;
    private CharSequence iconText = "";
    private final String SPEEDCHAR = "＃SPEED＃";
    private boolean isShowAssistant = true;
    private boolean isNeedHide = true;
    private final int WAITTIME = 10 * 1000;
    private final int STANDBYWAITTIME = 10 * 60 * 1000;
    private int mode = 0;
    public static final String RES = "RES";
    public static final String SET = "SET";
    public static final String RESSET = "RESSET";
    public static final String SWITCH = "SWITCH";

    public static CruiseController getInstance(Context mContext){
        if(mCruiseController == null){
            mCruiseController = new CruiseController(mContext);
        }
        return mCruiseController;
    }

    private CruiseController(Context mContext){
        this.mContext = mContext;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_MSG:
                    Map map = (Map) msg.obj;
                    conditionId = (String) map.get("conditionId");
                    defaultText = (String) map.get("defaultText");
                    condition = (int) map.get("condition");
                    resId = (int) map.get("resId");
                    builder = (SpannableStringBuilder) map.get("iconText");
                    //超时机制
                    SRAgent.getInstance().resetSrTimeCount();
                    TimeoutManager.saveSrState(mContext, TimeoutManager.WAIT_CRUISE_ACTION, "");

                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultText, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            if(conditionId == TtsConstant.CCC1CONDITION || conditionId == TtsConstant.CCC4CONDITION ||
                                    conditionId == TtsConstant.CCC10CONDITION || conditionId == TtsConstant.CCC14CONDITION ||
                                    conditionId == TtsConstant.CCC1_1CONDITION) {
                                EventBusUtils.sendIvFloatSmallViewVisible(SET);
                            }else if(conditionId == TtsConstant.CCC7CONDITION || conditionId == TtsConstant.CCC8CONDITION ||
                                    conditionId == TtsConstant.CCC15CONDITION || conditionId == TtsConstant.CCC7_1CONDITION){
                                EventBusUtils.sendIvFloatSmallViewVisible(RES);
                            }else if(conditionId == TtsConstant.CCC2CONDITION){
                                EventBusUtils.sendIvFloatSmallViewVisible(SWITCH);
                            }else if(conditionId == TtsConstant.CCC3_1CONDITION){
                                EventBusUtils.sendIvFloatSmallViewVisible(RESSET);
                            }else if(conditionId == TtsConstant.CCC9CONDITION){
                                EventBusUtils.sendSpeed(CarUtils.getInstance(mContext).getTargetCruiseSpeed() + "km/h");
                            }else {
                                EventBusUtils.sendIvFloatSmallViewVisible("");
                            }
                            EventBusUtils.sendResIdMessage(resId);
                            EventBusUtils.sendIconText(builder);
                            tts = Utils.replaceTts(tts,SPEEDCHAR,CarUtils.getInstance(mContext).getTargetCruiseSpeed() + "");
                            Log.d(TAG,"tts = " + tts);
                            if(!TextUtils.isEmpty(conditionId)) Utils.eventTrack(mContext, R.string.skill_cruise, R.string.scene_cruise, R.string.object_cruise_guide, conditionId, condition, tts);//埋点
                            Utils.startTTSOnlyNoSendMainMessage(tts, new TTSController.OnTtsStoppedListener() {
                                @Override
                                public void onPlayStopped() {
                                    Log.d(TAG,"speak end...");
                                    if(conditionId == TtsConstant.CCC1CONDITION  || conditionId == TtsConstant.CCC2CONDITION  ||
                                            conditionId == TtsConstant.CCC3_1CONDITION  || conditionId == TtsConstant.CCC4CONDITION  ||
                                            conditionId == TtsConstant.CCC7CONDITION  || conditionId == TtsConstant.CCC8CONDITION  ||
                                            conditionId == TtsConstant.CCC10CONDITION || conditionId == TtsConstant.CCC1_1CONDITION ||
                                            conditionId == TtsConstant.CCC7_1CONDITION){
                                        Log.d(TAG,"wait...");
                                        isNeedHide = true;
                                        mHandler.sendEmptyMessageDelayed(HIDE_ASSISTANT,WAITTIME);
                                    }else {
                                        Log.d(TAG,"hide direct...");
                                        isNeedHide = true;
                                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                                            FloatViewManager.getInstance(mContext).hide();
                                        }
                                    }
                                    //保存播报的id
//                                    SharedPreferencesUtils.saveString(mContext,AppConstant.KEY_LASTBROADCCC,conditionId);
//                                    if(conditionId == TtsConstant.CCC7CONDITION || conditionId == TtsConstant.CCC8CONDITION){
//                                        mHandler.sendEmptyMessageDelayed(SPEAKCCC15,STANDBYWAITTIME);//10分钟
//                                    }else if(conditionId == TtsConstant.CCC1CONDITION){
//                                        mHandler.sendEmptyMessageDelayed(SPEAKCCC14,STANDBYWAITTIME);//10分钟
//                                    }
                                }
                            });
                        }
                    });
                    break;
                case HIDE_ASSISTANT:
                    if(isNeedHide){
                        Log.d(TAG,"start hide...");
                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                    }
                    break;
                case SPEAKCCC14:
                    int cruiseControlStatus14 = CarUtils.getInstance(mContext).getCruiseControlStatus();
                    String lastBroadCCCid14 = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_LASTBROADCCC,"");
                    Log.d(TAG,"cruiseControlStatus14 = " + cruiseControlStatus14 + ",lastBroadCCCid14 = " + lastBroadCCCid14);
                    if(cruiseControlStatus14 == CarUtils.STANDBY && "ccC1".equals(lastBroadCCCid14)){//stand by
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC14CONDITION);
                    }
                    break;
                case SPEAKCCC15:
                    int cruiseControlStatus15 = CarUtils.getInstance(mContext).getCruiseControlStatus();
                    String lastBroadCCCid15 = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_LASTBROADCCC,"");
                    Log.d(TAG,"cruiseControlStatus15 = " + cruiseControlStatus15 + ",lastBroadCCCid15 = " + lastBroadCCCid15);
                    if(cruiseControlStatus15 == CarUtils.STANDBY && (("ccC7".equals(lastBroadCCCid15)) || ("ccC8".equals(lastBroadCCCid15)))){//stand by
                        CruiseController.getInstance(mContext).srAction(TtsConstant.CCC15CONDITION);
                    }
                    break;

            }
        }
    };

    public void srAction(String conditionId) {
        if(!isCriuseEnable()){
            return;
        }
        isNeedHide = false;
        try {
            mode = Settings.System.getInt(mContext.getContentResolver(), AppConstant.SHOW_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        switch (conditionId){
            case TtsConstant.CCC1CONDITION:
                conditionId = TtsConstant.CCC1CONDITION;
                defaultText = mContext.getString(R.string.ccC1);
                condition = R.string.condition_guide_ccc1;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC1);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC1_1CONDITION:
                conditionId = TtsConstant.CCC1_1CONDITION;
                defaultText = mContext.getString(R.string.ccC1_1);
                condition = R.string.condition_guide_ccc1_1;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC1_1);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC2CONDITION:
                conditionId = TtsConstant.CCC2CONDITION;
                defaultText = mContext.getString(R.string.ccC2);
                condition = R.string.condition_guide_ccc2;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC2);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC3_1CONDITION:
                conditionId = TtsConstant.CCC3_1CONDITION;
                defaultText = mContext.getString(R.string.ccC3_1);
                condition = R.string.condition_guide_ccc3_1;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC3_1);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC3CONDITION:
                conditionId = TtsConstant.CCC3CONDITION;
                defaultText = mContext.getString(R.string.ccC3);
                condition = R.string.condition_guide_ccc3;
                resId = mode == 0 ? R.mipmap.cruise1_9_night : R.mipmap.cruise1_9_day;
                iconText = mContext.getString(R.string.icon_ccC3);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC4CONDITION:
                conditionId = TtsConstant.CCC4CONDITION;
                defaultText = mContext.getString(R.string.ccC4);
                condition = R.string.condition_guide_ccc4;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC4);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC4_1CONDITION:
                conditionId = TtsConstant.CCC4_1CONDITION;
                defaultText = mContext.getString(R.string.ccC4_1);
                condition = R.string.condition_guide_ccc4_1;
                resId = mode == 0 ? R.mipmap.cruise1_5_night : R.mipmap.cruise1_5_day;
                iconText = mContext.getString(R.string.icon_ccC4_1);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC5CONDITION:
                conditionId = TtsConstant.CCC5CONDITION;
                defaultText = mContext.getString(R.string.ccC5);
                condition = R.string.condition_guide_ccc5;
                resId = mode == 0 ? R.mipmap.cruise1_5_night : R.mipmap.cruise1_5_day;
                iconText = mContext.getString(R.string.icon_ccC5);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC5_1CONDITION:
                conditionId = TtsConstant.CCC5_1CONDITION;
                defaultText = mContext.getString(R.string.ccC5_1);
                condition = R.string.condition_guide_ccc5_1;
                resId = mode == 0 ? R.mipmap.cruise1_5_night : R.mipmap.cruise1_5_day;
                iconText = mContext.getString(R.string.icon_ccC5_1);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC5_2CONDITION:
                conditionId = TtsConstant.CCC5_2CONDITION;
                defaultText = mContext.getString(R.string.ccC5_2);
                condition = R.string.condition_guide_ccc5_2;
                resId = mode == 0 ? R.mipmap.cruise1_5_night : R.mipmap.cruise1_5_day;
                iconText = mContext.getString(R.string.icon_ccC5_2);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC6CONDITION:
                conditionId = TtsConstant.CCC6CONDITION;
                defaultText = mContext.getString(R.string.ccC6);
                condition = R.string.condition_guide_ccc6;
                resId = mode == 0 ? R.mipmap.cruise1_9_night : R.mipmap.cruise1_9_day;
                iconText = mContext.getString(R.string.icon_ccC6);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC6_1CONDITION:
                conditionId = TtsConstant.CCC6_1CONDITION;
                defaultText = mContext.getString(R.string.ccC6_1);
                condition = R.string.condition_guide_ccc6_1;
                resId = mode == 0 ? R.mipmap.cruise1_9_night : R.mipmap.cruise1_9_day;
                iconText = mContext.getString(R.string.icon_ccC6);
                isShowAssistant = false;
                break;
            case TtsConstant.CCC7CONDITION:
                conditionId = TtsConstant.CCC7CONDITION;
                defaultText = mContext.getString(R.string.ccC7);
                condition = R.string.condition_guide_ccc7;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC7);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC7_1CONDITION:
                conditionId = TtsConstant.CCC7_1CONDITION;
                defaultText = mContext.getString(R.string.ccC7_1);
                condition = R.string.condition_guide_ccc7_1;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC7_1);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC8CONDITION:
                conditionId = TtsConstant.CCC8CONDITION;
                defaultText = mContext.getString(R.string.ccC8);
                condition = R.string.condition_guide_ccc8;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC8);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC9CONDITION:
                conditionId = TtsConstant.CCC9CONDITION;
                defaultText = mContext.getString(R.string.ccC9);
                condition = R.string.condition_guide_ccc9;
                resId = mode == 0 ? R.mipmap.cruise1_9_night : R.mipmap.cruise1_9_day;
                iconText = mContext.getString(R.string.icon_ccC9);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC10CONDITION:
                conditionId = TtsConstant.CCC10CONDITION;
                defaultText = mContext.getString(R.string.ccC10);
                condition = R.string.condition_guide_ccc10;
                resId = mode == 0 ? R.drawable.cruise_root : R.drawable.cruise_root;
                iconText = mContext.getString(R.string.icon_ccC10);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC11CONDITION:
                conditionId = TtsConstant.CCC11CONDITION;
                defaultText = mContext.getString(R.string.ccC11);
                condition = R.string.condition_guide_ccc11;
                resId = mode == 0 ? R.mipmap.cruise1_9_night : R.mipmap.cruise1_9_day;
                iconText = mContext.getString(R.string.icon_ccC11);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC12CONDITION:
                conditionId = TtsConstant.CCC12CONDITION;
                defaultText = mContext.getString(R.string.ccC12);
                condition = R.string.condition_guide_ccc12;
                resId = mode == 0 ? R.mipmap.cruise_exit_night : R.mipmap.cruise_exit_day;
                iconText = mContext.getString(R.string.icon_ccC13);
                isShowAssistant = false;
                break;
            case TtsConstant.CCC13CONDITION:
                conditionId = TtsConstant.CCC13CONDITION;
                defaultText = mContext.getString(R.string.ccC13);
                condition = R.string.condition_guide_ccc13;
                resId = mode == 0 ? R.mipmap.cruise_exit_night : R.mipmap.cruise_exit_day;
                iconText = mContext.getString(R.string.icon_ccC13);
                isShowAssistant = true;
                break;
            case TtsConstant.CCC14CONDITION:
                conditionId = TtsConstant.CCC14CONDITION;
                defaultText = mContext.getString(R.string.ccC14);
                condition = R.string.condition_guide_ccc14;
                resId = mode == 0 ? R.mipmap.cruise1_5_night : R.mipmap.cruise1_5_day;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC15CONDITION:
                conditionId = TtsConstant.CCC15CONDITION;
                defaultText = mContext.getString(R.string.ccC15);
                condition = R.string.condition_guide_ccc15;
                resId = mode == 0 ? R.mipmap.cruise1_5_night : R.mipmap.cruise1_5_day;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC16CONDITION:
                conditionId = TtsConstant.CCC16CONDITION;
                defaultText = mContext.getString(R.string.ccC16);
                condition = R.string.condition_guide_ccc16;
                resId = mode == 0 ? R.mipmap.cruise1_5_night : R.mipmap.cruise1_5_day;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC17CONDITION:
                conditionId = TtsConstant.CCC17CONDITION;
                defaultText = mContext.getString(R.string.ccC17);
                condition = R.string.condition_guide_ccc17;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC18CONDITION:
                conditionId = TtsConstant.CCC18CONDITION;
                defaultText = mContext.getString(R.string.ccC18);
                condition = R.string.condition_guide_ccc18;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC19CONDITION:
                conditionId = TtsConstant.CCC19CONDITION;
                defaultText = mContext.getString(R.string.ccC19);
                condition = R.string.condition_guide_ccc19;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC20CONDITION:
                conditionId = TtsConstant.CCC20CONDITION;
                defaultText = mContext.getString(R.string.ccC20);
                condition = R.string.condition_guide_ccc20;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC21CONDITION:
                conditionId = TtsConstant.CCC21CONDITION;
                defaultText = mContext.getString(R.string.ccC21);
                condition = R.string.condition_guide_ccc21;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC22CONDITION:
                conditionId = TtsConstant.CCC22CONDITION;
                defaultText = mContext.getString(R.string.ccC22);
                condition = R.string.condition_guide_ccc22;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC23CONDITION:
                conditionId = TtsConstant.CCC23CONDITION;
                defaultText = mContext.getString(R.string.ccC23);
                condition = R.string.condition_guide_ccc23;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC24CONDITION:
                conditionId = TtsConstant.CCC24CONDITION;
                defaultText = mContext.getString(R.string.ccC24);
                condition = R.string.condition_guide_ccc24;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC25CONDITION:
                conditionId = TtsConstant.CCC25CONDITION;
                defaultText = mContext.getString(R.string.ccC25);
                condition = R.string.condition_guide_ccc25;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
            case TtsConstant.CCC26CONDITION:
                conditionId = TtsConstant.CCC26CONDITION;
                defaultText = mContext.getString(R.string.ccC26);
                condition = R.string.condition_guide_ccc26;
                resId = R.mipmap.cruise1_5_night;
                isShowAssistant = false;
                break;
        }
        if(isShowAssistant && FloatViewManager.getInstance(mContext).isHide()) {
            showAssistant();
        }else{
            FloatViewManager.getInstance(mContext).hide();
        }
        iconText = Utils.replaceTts(iconText.toString(),SPEEDCHAR,CarUtils.getInstance(mContext).getTargetCruiseSpeed() + "");

        Message msg = new Message();
        msg.what = SHOW_MSG;
        Map<String,Object> map = new HashMap<>();
        map.put("conditionId",conditionId);
        map.put("defaultText",defaultText);
        map.put("condition",condition);
        map.put("resId",resId);
        map.put("iconText",transfertoBuilder(iconText));
        msg.obj = map;
        mHandler.sendMessageDelayed(msg,TIME_DELAY_SHOWING);
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    //获取定速巡航的配置字
    private boolean isCriuseEnable(){
        int ccs_enable = Utils.getInt(mContext, AppConstant.CCS_ENABLE,0);
        Log.d(TAG, "ccs_enable = " + ccs_enable);
        return ccs_enable == 1 ? true : false;
    }

    private SpannableStringBuilder transfertoBuilder(CharSequence text){
        if(!TextUtils.isEmpty(text)){
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            Pattern patternRES = Pattern.compile(RES);
            Pattern patternSET = Pattern.compile(SET);
            Pattern patternSWITCH = Pattern.compile("主开关");
            Matcher matcherRES = patternRES.matcher(text);
            Matcher matcherSET = patternSET.matcher(text);
            Matcher matcherSWITCH = patternSWITCH.matcher(text);

            Drawable drawableSET = mContext.getResources().getDrawable(R.mipmap.icon_set);
            Drawable drawableRES = mContext.getResources().getDrawable(R.mipmap.icon_res);
            Drawable drawableSWITCH = mContext.getResources().getDrawable(R.mipmap.icon_switch);
            drawableSET.setBounds(0,-5,95,19);
            drawableRES.setBounds(0,-5,95,19);
            drawableSWITCH.setBounds(0,0,36,32);
            while (matcherRES.find()) {
                ImageSpan imageSpan = new ImageSpan(drawableRES,ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, matcherRES.start(), matcherRES.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            while (matcherSET.find()) {
                ImageSpan imageSpan = new ImageSpan(drawableSET,ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, matcherSET.start(), matcherSET.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            while (matcherSWITCH.find()) {
                ImageSpan imageSpan = new ImageSpan(drawableSWITCH,ImageSpan.ALIGN_BASELINE);
                builder.setSpan(imageSpan, matcherSWITCH.start(), matcherSWITCH.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return builder;
        }else {
            return null;
        }
    }
}

