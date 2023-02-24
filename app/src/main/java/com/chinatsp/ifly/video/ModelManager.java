package com.chinatsp.ifly.video;




/**
 * 封装获取各模块实现类的获取
 * Created by ytkj on 2018/6/19.
 */

public class ModelManager {



    public static VideoDataModel getVideoDataModel() {
        return VideoDataImpl.getInstance();
    }




}
