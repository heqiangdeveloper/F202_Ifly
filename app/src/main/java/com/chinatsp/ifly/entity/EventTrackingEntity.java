package com.chinatsp.ifly.entity;

import java.io.Serializable;

public class EventTrackingEntity implements Serializable {
    public String appName;//技能
    public String scene;//场景
    public String object;//意图
    public String condition;//条件
    public String conditionId;//条件ID
    public String ori;//待替换字符串
    public String replace;//替换字符串
    public String tts;//tts文案
    public String primitive; //语音原语
    public String response; //语音返回的json字符串
}
