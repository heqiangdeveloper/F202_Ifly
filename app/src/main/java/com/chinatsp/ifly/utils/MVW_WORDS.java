package com.chinatsp.ifly.utils;

public interface MVW_WORDS {

    String[] navi = {
            MVW_WORDS.ID_FINISH_NAVI, MVW_WORDS.ID_STOP_NAVI, MVW_WORDS.ID_EXIT_NAVI, MVW_WORDS.ID_ZOOMIN, MVW_WORDS.ID_ZOOMOUT,
            MVW_WORDS.ID_TRAFFIC_ON, MVW_WORDS.ID_TRAFFIC_OFF, MVW_WORDS.ID_TTS_OFF, MVW_WORDS.ID_TTS_ON, MVW_WORDS.ID_GO_HOME,
            MVW_WORDS.ID_GO_CAMPANY,
            //以下不是免唤醒词，在识别和无语义中保留
            MVW_WORDS.ID_HEAD_FIRST, MVW_WORDS.ID_NO_HEAD, MVW_WORDS.ID_FEW_CHARGE, MVW_WORDS.ID_NO_CHARGE, MVW_WORDS.ID_FEW_BLOCK,
            MVW_WORDS.ID_NO_BLOCK,MVW_WORDS.ID_NO_BLOCK1, MVW_WORDS.ID_GAS_STATION, MVW_WORDS.ID_HOW_LONG, MVW_WORDS.ID_HOW_FAR, MVW_WORDS.ID_DAY_MODE,
            MVW_WORDS.ID_NIGHT_MODE, MVW_WORDS.ID_AUTO_MODE, MVW_WORDS.ID_2D_MODE, MVW_WORDS.ID_2D_VIEW, MVW_WORDS.ID_3D_MODE,
            MVW_WORDS.ID_3D_VIEW, MVW_WORDS.ID_HEAD_UP, MVW_WORDS.ID_NORTH_UP, MVW_WORDS.ID_LOOK_ALL, MVW_WORDS.ID_I_GO_HOME,
            MVW_WORDS.ID_I_GO_COMPANY, MVW_WORDS.ID_TTS_ON1, MVW_WORDS.ID_TTS_OFF1,
            MVW_WORDS.ID_NIGHT_MODE1, MVW_WORDS.ID_NIGHT_MODE2, MVW_WORDS.ID_NIGHT_MODE3,MVW_WORDS.ID_DAY_MODE1
    };

    /**
     * 结束导航
     */
    final String ID_FINISH_NAVI = "结束导航";

    /**
     * 停止导航
     */
    String ID_STOP_NAVI = "停止导航";

    /**
     * 退出导航
     */
    String ID_EXIT_NAVI = "退出导航";

    /**
     * 放大地图
     */
    String ID_ZOOMIN = "放大地图";

    /**
     * 缩小地图
     */
    String ID_ZOOMOUT = "缩小地图";

    /**
     * 打开路况
     */
    String ID_TRAFFIC_ON = "打开路况";

    /**
     * 关闭路况
     */
    String ID_TRAFFIC_OFF = "关闭路况";

    /**
     * 小欧我要回家
     */
    String ID_GO_HOME = "小欧我要回家";

    /**
     * 小欧我要去公司
     */
    String ID_GO_CAMPANY = "小欧我要去公司";

    /**
     * 关闭播报
     */
    String ID_TTS_OFF = "关闭播报";

    /**
     * 打开播报
     */
    String ID_TTS_ON = "打开播报";

    /**
     * 关闭导航声音
     */
    String ID_TTS_OFF1 = "关闭导航声音";

    /**
     * 打开导航声音
     */
    String ID_TTS_ON1 = "打开导航声音";



    /**
     * 高速优先
     */
    String ID_HEAD_FIRST = "高速优先";

    /**
     * 不走高速
     */
    String ID_NO_HEAD = "不走高速";

    /**
     * 少收费
     */
    String ID_FEW_CHARGE = "少收费";

    /**
     * 避免收费
     */
    String ID_NO_CHARGE = "避免收费";

    /**
     * 躲避拥堵
     */
    String ID_FEW_BLOCK = "躲避拥堵";

    /**
     * 规避拥堵
     */
    String ID_NO_BLOCK = "规避拥堵";

    /**
     * 规避拥堵
     */
    String ID_NO_BLOCK1 = "避免拥堵";

    /**
     * 沿途的加油站
     */
    String ID_GAS_STATION = "沿途的加油站";

    /**
     * 还有多久
     */
    String ID_HOW_LONG = "还有多久";

    /**
     * 还有多远
     */
    String ID_HOW_FAR = "还有多远";

    /**
     * 白天模式
     */
    String ID_DAY_MODE = "白天模式";
    String ID_DAY_MODE1 = "切换至白天模式";

    /**
     * 黑夜模式
     */
    String ID_NIGHT_MODE = "黑夜模式";
    String ID_NIGHT_MODE1 = "夜间模式";
    String ID_NIGHT_MODE2 = "切换至黑夜模式";
    String ID_NIGHT_MODE3 = "切换至夜间模式";

    /**
     * 自动模式
     */
    String ID_AUTO_MODE = "自动模式";

    /**
     * 2D模式
     */
    String ID_2D_MODE = "2D模式";

    /**
     * 2D视图
     */
    String ID_2D_VIEW = "2D视图";

    /**
     * 3D模式
     */
    String ID_3D_MODE = "3D模式";

    /**
     * 3D视图
     */
    String ID_3D_VIEW = "3D视图";

    /**
     * 车头朝上
     */
    String ID_HEAD_UP = "车头朝上";

    /**
     * 正北朝上
     */
    String ID_NORTH_UP = "正北朝上";

    /**
     * 查看全局
     */
    String ID_LOOK_ALL = "查看全局";

    /**
     * 我要回家
     */
    String ID_I_GO_HOME = "我要回家";

    /**
     * 我要去公司
     */
    String ID_I_GO_COMPANY = "我要去公司";


    String ID_HAWKEYE_MODE_0 = "切换到小地图";
    String ID_HAWKEYE_MODE_1 = "切换到柱状图";

}
