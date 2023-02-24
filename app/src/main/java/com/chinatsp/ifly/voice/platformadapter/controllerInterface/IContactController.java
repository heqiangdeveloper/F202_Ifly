package com.chinatsp.ifly.voice.platformadapter.controllerInterface;


import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.adapter.controllerInterface.IController;

public interface IContactController extends IController<IntentEntity, MvwLParamEntity, StkResultEntity> {
    void showContactActivity(String resultStr, String semanticStr);

    void startDial(ContactEntity contactEntity);

    void answerCall();

    void rejectCall();

    void exitVoiceAssistant();
}
