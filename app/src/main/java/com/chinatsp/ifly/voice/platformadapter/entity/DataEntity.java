package com.chinatsp.ifly.voice.platformadapter.entity;

import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.entity.PoiEntity;
import com.google.gson.JsonObject;

import java.util.List;

public class DataEntity {
    public List<JsonObject> result ;
    public JsonObject debug;
    public Object dataUrl ;
    public JsonObject error;
    /**
     * 导航到xxx 途径xxx  暂存途经点列表
     */
    public List<JsonObject> resultVia ;
    /**
     * 添加xxx和xx为途径点 暂存下一个途经点列表
     */
    public List<JsonObject> resultViaNext ;

    public List<PoiEntity> endPoi;
    public List<PoiEntity> viaPoi;
    public List<PoiEntity> viaPoiNext;

    /**
     * 暂存选择目的地
     */
    public BaseEntity endPoiResult;
    /**
     * 暂存选择途径点
     */
    public BaseEntity viaPoiResult;
}
