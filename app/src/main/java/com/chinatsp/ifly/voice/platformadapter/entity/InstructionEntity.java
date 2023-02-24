package com.chinatsp.ifly.voice.platformadapter.entity;

import com.chinatsp.ifly.utils.LogUtils;

import static org.greenrobot.eventbus.EventBus.TAG;

public class InstructionEntity {

    /**
     * 识别
     */
    public static final int TYPE_SR_ACTION = 0;
    /**
     * 免唤醒
     */
    public static final int TYPE_MVW =1;
    /**
     * 无语义
     */
    public static final int TYPE_DO_ACTION =2;
    /**
     * 可见即可说
     */
    public static final int TYPE_STK_ACTION=2;

    public int id;
    public String text;
    public int nMvwScene=0;

    public int type = -1;

    public InstructionEntity() {
    }

    public InstructionEntity(StkResultEntity stkResultEntity) {
        type = TYPE_STK_ACTION;
        if (stkResultEntity.selectList != null && stkResultEntity.selectList.list != null && stkResultEntity.selectList.list.size() > 0) {
            id = stkResultEntity.selectList.list.get(0).id;
            text = stkResultEntity.text;
        } else {
            LogUtils.e(TAG, stkResultEntity.text + "no return id");
            if ("收藏".equals(stkResultEntity.text)) {//针对收藏做特殊处理 {"bisMvwSTKS":"1","text":"收藏"}
                id = 101;
                text = stkResultEntity.text;
            }
        }
    }

    public InstructionEntity(MvwLParamEntity mvwLParamEntity) {
        type = TYPE_MVW;
        id = mvwLParamEntity.nMvwId;
        text = mvwLParamEntity.nKeyword;
        nMvwScene = mvwLParamEntity.nMvwScene;

    }
}