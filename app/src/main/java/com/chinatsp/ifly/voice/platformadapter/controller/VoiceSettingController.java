package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;

import com.chinatsp.ifly.entity.VoiceSubSettingsEvent;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IMusicController;
import com.chinatsp.ifly.voice.platformadapter.entity.InstructionEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;

import org.greenrobot.eventbus.EventBus;

public class VoiceSettingController extends BaseController implements IMusicController {
    private static final String TAG = "VoiceSettingController";
    private Context context;
    private IntentEntity intentEntity;

    public VoiceSettingController(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        this.intentEntity = intentEntity;
    }

    @Override
    public void mvwAction(MvwLParamEntity lParamEntity) {
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {
        InstructionEntity innerEntity = new InstructionEntity(stkResultEntity);
        LogUtils.e(TAG, "id:" + innerEntity.id + "  text:" + innerEntity.text);
        switch (innerEntity.id) {
            case 10: //应答语
                EventBus.getDefault().post(new VoiceSubSettingsEvent(VoiceSubSettingsEvent.SubSettingsItem.ANSWER_SETTING));
                break;
            case 11: //返回
                EventBus.getDefault().post(new VoiceSubSettingsEvent(VoiceSubSettingsEvent.SubSettingsItem.RETURN));
                break;
            case 12: //修改唤醒词
                EventBus.getDefault().post(new VoiceSubSettingsEvent(VoiceSubSettingsEvent.SubSettingsItem.AWARE_SETTING));
                break;
            default:
                    doExceptonAction(context);

        }
    }
}
