package com.chinatsp.ifly.entity;

public class CommandEvent {

    public enum CommadType {
        JUMP,//小欧跳转
        RECOMMEND,//小欧推荐
        COMMAND,//所有指令
        SHOW,
        HIDE,
        NAVI,
        GEAR,
    }
    public CommadType type;
    public String text;
    public String message;

    public CommandEvent() {
    }

    public CommandEvent(CommadType type, String text, String message) {
        this.type = type;
        this.text = text;
        this.message = message;
    }


}