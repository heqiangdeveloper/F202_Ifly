package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;

import com.chinatsp.ifly.entity.PoiEntity;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IPromptController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;

import java.util.ArrayList;
import java.util.List;

public class PromptController implements IPromptController {
    private static String TAG = "MapController";
    private Context mContext;
    private IntentEntity intentEntity;
    public static final int MSG_SELECT = 1001;
    public static final int MSG_NAVIGATION = 1002;
    private List<PoiEntity> poiEntityList = new ArrayList<>();

    public PromptController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void srAction(IntentEntity intentEntity) {

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {

    }
    @Override
    public void stkAction(StkResultEntity o) {

    }
}
