package com.chinatsp.ifly.db.entity;

import android.database.Cursor;

import com.chinatsp.ifly.db.TtsTableInfo;

import java.io.Serializable;

public class TtsInfo implements Serializable {
    //模块id
    private String skillId;
    private String skillVersion;
    //功能id
    private String conditionId;
    //tts 文案id
    private String ttsId;
    //tts文案信息
    private String ttsText;
    private String valid_starttime;
    private String velid_endtime;
    private String offline_broadcast;
    private int baseResponse;
    private int isTtsAvailable;

    public String getValid_starttime() {
        return valid_starttime;
    }

    public void setValid_starttime(String valid_starttime) {
        this.valid_starttime = valid_starttime;
    }

    public String getVelid_endtime() {
        return velid_endtime;
    }

    public void setVelid_endtime(String velid_endtime) {
        this.velid_endtime = velid_endtime;
    }

    public String getOffline_broadcast() {
        return offline_broadcast;
    }

    public void setOffline_broadcast(String offline_broadcast) {
        this.offline_broadcast = offline_broadcast;
    }

    public int getBaseResponse() {
        return baseResponse;
    }

    public void setBaseResponse(int baseResponse) {
        this.baseResponse = baseResponse;
    }

    public int getIsTtsAvailable() {
        return isTtsAvailable;
    }

    public void setIsTtsAvailable(int isTtsAvailable) {
        this.isTtsAvailable = isTtsAvailable;
    }

    public String getSkillId() {
        return skillId;
    }

    public void setSkillId(String skillId) {
        this.skillId = skillId;
    }

    public String getSkillVersion() {
        return skillVersion;
    }

    public void setSkillVersion(String skillVersion) {
        this.skillVersion = skillVersion;
    }

    public String getConditionId() {
        return conditionId;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getTtsId() {
        return ttsId;
    }

    public void setTtsId(String ttsId) {
        this.ttsId = ttsId;
    }

    public String getTtsText() {
        return ttsText;
    }

    public void setTtsText(String ttsText) {
        this.ttsText = ttsText;
    }

    @Override
    public String toString() {
        return "TtsInfo{" +
                "skillId='" + skillId + '\'' +
                ", skillVersion='" + skillVersion + '\'' +
                ", conditionId='" + conditionId + '\'' +
                ", ttsId='" + ttsId + '\'' +
                ", ttsText='" + ttsText + '\'' +
                '}';
    }
}
