package com.chinatsp.ifly.entity;

import android.support.annotation.NonNull;

import java.io.Serializable;

public  class FlightTicketInfo implements  Serializable {
    //票类型:全票或折扣票;
    private String discount;
    //票价
    private String price;
    //剩余张数
    private String remainingStatus;
    //机舱类型:公务舱,经济舱等
    private String seatType;

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRemainingStatus() {
        return remainingStatus;
    }

    public void setRemainingStatus(String remainingStatus) {
        this.remainingStatus = remainingStatus;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    @Override
    public String toString() {
        return "TikectInfo{" +
                "discount='" + discount + '\'' +
                ", price='" + price + '\'' +
                ", remainingStatus='" + remainingStatus + '\'' +
                ", seatType='" + seatType + '\'' +
                '}';
    }
}

