package com.chinatsp.ifly.utils;

import android.content.Context;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;

public class IflyUtils {

    //判断识别和唤醒初始化是否已初始化
    public static boolean iflytekIsInited(Context context) {

        String tip = context.getString(R.string.iflytek_not_ready);
        String conditionId = TtsConstant.MAINC16CONDITION;
        if (!SRAgent.getInstance().init_state || !MVWAgent.getInstance().init_state) {
            if(SRAgent.getInstance().init_error_msg == SrSession.ISS_SR_ERROR_FILE_NOT_FOUND){
                tip = context.getString(R.string.iflytek_file_not_found);
                conditionId = TtsConstant.MAINC17CONDITION;
            }
            Utils.getMessageWithTtsSpeak(context, conditionId, tip);
            Utils.eventTrack(context, R.string.skill_main, R.string.scene_main_exception, R.string.object_main_exception_2, TtsConstant.MAINC16CONDITION, R.string.condition_mainC16);
            return false;
        }
        return true;
    }
}
