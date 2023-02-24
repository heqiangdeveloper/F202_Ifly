package com.chinatsp.ifly.activeservice;

import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.FeedBackController;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IAppInterface;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.iflytek.mvw.MvwSession;

/**
 * Created by zxb on 2019/6/6.
 */

public class ActiveController implements IAppInterface {
    private Context context;

    public ActiveController(Context mContext) {
        this.context = mContext;

    }

    @Override
    public void srAction(IntentEntity intentEntity) {

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        switch (mvwLParamEntity.nMvwScene) {
            case MvwSession.ISS_MVW_SCENE_CONFIRM:
                setActiveIdentify(mvwLParamEntity.nKeyword);
                break;

        }
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    public void setActiveIdentify(String keyword) {
        if(FeedBackController.getInstance(context).isSureWord(keyword)) {
            context.sendBroadcast(new Intent(AppConstant.ACTION_INITIATIVE_START));
        } else  {
            context.sendBroadcast(new Intent(AppConstant.ACTION_INITIATIVE_SHUTDOWN));
        }

    }
}
