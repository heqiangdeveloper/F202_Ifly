package com.chinatsp.ifly.voice.platformadapter.controllerInterface;

import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.adapter.controllerInterface.IController;

public interface IStockController extends IController<IntentEntity, MvwLParamEntity, StkResultEntity> {
    void showStockActivity(String resultStr, String answerText);
}