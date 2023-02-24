package com.chinatsp.ifly.entity;

import java.io.Serializable;

public class TrainTicketInfo implements Serializable {
    //座位类型
    private String name;
    //预定地址
    private String orderUrl;
    //剩余席位
    private String remainingStatus;
    //价格
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public String getRemainingStatus() {
        return remainingStatus;
    }

    public void setRemainingStatus(String remainingStatus) {
        this.remainingStatus = remainingStatus;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
