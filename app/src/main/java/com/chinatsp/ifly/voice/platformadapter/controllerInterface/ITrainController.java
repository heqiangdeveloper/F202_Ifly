package com.chinatsp.ifly.voice.platformadapter.controllerInterface;

import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.adapter.controllerInterface.IController;

public interface ITrainController extends IController<IntentEntity, MvwLParamEntity, StkResultEntity> {
    void showTrainActivity(String resultStr, String semantic);
}