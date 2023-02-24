package com.chinatsp.ifly.voice.platformadapter.controllerInterface;


import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.entity.MXPoiEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.adapter.controllerInterface.IController;

public interface IMapController extends IController<IntentEntity, MvwLParamEntity, StkResultEntity> {
    void showPoiActivity(String resultStr, String semanticStr);

    void startNavigation(BaseEntity poiEntity);

    void naviToPoi(BaseEntity poiEntity);

    /***********欧尚修改开始*****************/
//    void requestAddPass(String poiName);
    void requestAddPass(BaseEntity entity);
    /***********欧尚修改结束*****************/

    void collectByPoi(MXPoiEntity entity, int type);
}
