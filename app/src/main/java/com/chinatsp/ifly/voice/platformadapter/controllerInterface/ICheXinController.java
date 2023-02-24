package com.chinatsp.ifly.voice.platformadapter.controllerInterface;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.adapter.controllerInterface.IController;

/**
 * Created by ytkj on 2019/7/4.
 */

public interface ICheXinController  extends IController<IntentEntity, MvwLParamEntity, StkResultEntity> {
    void showCheXinActivity(String resultStr, String semanticStr);
    void dispatchSRAction(IntentEntity intentEntity);
    void dispatchSRAction(int nMvwId);
    void startSend(int nMvwId);
}
