package com.chinatsp.ifly.entity;

public class MessageEvent {

    public static final String ACTION_HIDE = "-1";
    public static final String ACTION_GREY = "-2";
    public static final String ACTION_ANIM = "-3";
    public enum EventType {
        SPEECHING,//说话交互中
        ENDSPEECH,//语音播报完成  针对选择列表场景
        RESTARTSPEECH,//重新计算超时  针对选择列表场景
    }

    public EventType eventType;
    public CharSequence mainMessage;//主提示信息
    public CharSequence deputyMessage;//底部副提示信息
    public String talkMessage;//说话内容
    public int resId;//图片资源id

    public MessageEvent() {
    }

    public MessageEvent(EventType eventType, CharSequence mainMessage, CharSequence deputyMessage, String talkMessage,int resId) {
        this.eventType = eventType;
        this.mainMessage = mainMessage;
        this.deputyMessage = deputyMessage;
        this.talkMessage = talkMessage;
        this.resId = resId;
    }
}