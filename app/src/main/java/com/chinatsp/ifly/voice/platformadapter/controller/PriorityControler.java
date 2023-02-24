package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.content.Intent;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PriorityControler {

    private static final String ACTION_HIDE_VIEW = "action_ifly_hide_view";
    public static final int PRIORITY_IDEL=-1;
    public static final int PRIORITY_ONE = 1;
    public static final int PRIORITY_TWO = 2;
    public static final int PRIORITY_THREE = 3;
    public static final int PRIORITY_FOUR = 4;
    private static PriorityControler instance;
    private Context mContext;
    private List<String> mVehicleTts ;

//     public static final String ACTION_LEFT_POPUP_WINDOW_SHOW = "com.chinatsp.systemui.ACTION_LEFT_POPUP_WINDOW_SHOW";
//     public static final String KEY_TYPE = "key_type";// MUSIC、VOLUME、DRIVE_MODE、WINPER_MODE

    public static PriorityControler getInstance(Context c){
        if(instance==null){
            synchronized (PriorityControler.class){
                if(instance==null)
                    instance = new PriorityControler(c);
            }
        }
        return instance;
    }

    private PriorityControler(Context c){
        mContext = c.getApplicationContext();
        mVehicleTts = new ArrayList<>();

        //单量不足，不做播报
//        mVehicleTts.add("msgC7");
//        mVehicleTts.add("msgC8");
//
//        mVehicleTts.add("msgC13");
//        mVehicleTts.add("msgC14");

        //故障提醒不做播报
//        mVehicleTts.add("msgC22");
//        mVehicleTts.add("msgC23");
//        mVehicleTts.add("msgC24");
//        mVehicleTts.add("msgC25");
//        mVehicleTts.add("msgC26");
//        mVehicleTts.add("msgC26_1");
        mVehicleTts.add("msgC122");
        mVehicleTts.add("btnC80");

        //电源管理不做播报
//        mVehicleTts.add("btnC26");
//        mVehicleTts.add("btnC27");
//        mVehicleTts.add("btnC28");

    }


    public boolean isVehicleTts(String conditionId){
        if(mVehicleTts.contains(conditionId))
            return true;
        return false;
    }

    public void hideOtherFloatView(){
        Intent intent = new Intent(ACTION_HIDE_VIEW);
        mContext.sendBroadcast(intent);
    }

    public void handleVehicleMaidian(String conditionid){
        if("msgC122".equals(conditionid)){
            String tts = mContext.getString(R.string.msgC122);
            Utils.eventTrack(mContext, R.string.skill_active, R.string.scene_active_friend, R.string.object_active_friend, TtsConstant.MSGC122CONDITION, R.string.condition_msgC122,tts);
        }else if("btnC80".equals(conditionid)){
            String tts = mContext.getString(R.string.btn80);
            Utils.eventTrack(mContext, R.string.skill_guide, R.string.key_btnC80, R.string.key_btnC80,TtsConstant.GUIDEBTNC80CONDITION,R.string.condition_btnc80,tts);
        }

    }
}
