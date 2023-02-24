package com.chinatsp.ifly.entity;

import java.io.Serializable;

public class ReplaceTtsEntity implements Serializable {
    //待替换的内容
    private String originTts;
    //替换的内容
    private String replaceTts;

    public String getOriginTts() {
        return originTts;
    }

    public void setOriginTts(String originTts) {
        this.originTts = originTts;
    }

    public String getReplaceTts() {
        return replaceTts;
    }

    public void setReplaceTts(String replaceTts) {
        this.replaceTts = replaceTts;
    }
}
