package com.chinatsp.ifly.entity;

public class VoiceSubSettingsEvent {
    public enum SubSettingsItem {
        ANSWER_SETTING,  //应答语
        RETURN,  //返回
        AWARE_SETTING,  //应答语
    }
    public SubSettingsItem item;

    public VoiceSubSettingsEvent(SubSettingsItem item) {
        this.item = item;
    }
}