package com.chinatsp.ifly.entity;

public class SearchPoiEvent {
    public String searchKey;
    public String topic;

    public SearchPoiEvent() {
    }

    public SearchPoiEvent(String searchKey, String topic) {
        this.searchKey = searchKey;
        this.topic = topic;
    }
}
