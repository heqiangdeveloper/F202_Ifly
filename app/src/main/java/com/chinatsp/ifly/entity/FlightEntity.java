package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

import java.util.ArrayList;

public class FlightEntity extends BaseEntity {
    //目的机场
    private String aPort;
    //航空公司
    private String airline;
    //航班
    private String flight;
    //飞机型号
    private String fligtInfo;
    //准点率
    private String punctualityRate;
    //出发机场
    private String dPort;
    //目的城市
    private String arriveCity;
    //出发城市
    private String departCity;
    //到达时间
    private String arriveTime;
    //起飞时间
    private String takeOffTime;
    //出发时间
    private String startTime;
    private ArrayList<FlightTicketInfo> ticketInfo = new ArrayList<>();

    public String getaPort() {
        return aPort;
    }

    public void setaPort(String aPort) {
        this.aPort = aPort;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getFilght() {
        return flight;
    }

    public void setFilght(String filght) {
        this.flight = filght;
    }

    public String getFligtInfo() {
        return fligtInfo;
    }

    public void setFligtInfo(String fligtInfo) {
        this.fligtInfo = fligtInfo;
    }

    public String getPunctualityRate() {
        return punctualityRate;
    }

    public void setPunctualityRate(String punctualityRate) {
        this.punctualityRate = punctualityRate;
    }

    public String getdPort() {
        return dPort;
    }

    public void setdPort(String dPort) {
        this.dPort = dPort;
    }

    public String getArriveCity() {
        return arriveCity;
    }

    public void setArriveCity(String arriveCity) {
        this.arriveCity = arriveCity;
    }

    public String getDepartCity() {
        return departCity;
    }

    public void setDepartCity(String departCity) {
        this.departCity = departCity;
    }

    public String getArriveTime() {
        return arriveTime;
    }

    public void setArriveTime(String arriveTime) {
        this.arriveTime = arriveTime;
    }

    public String getTakeOffTime() {
        return takeOffTime;
    }

    public void setTakeOffTime(String takeOffTime) {
        this.takeOffTime = takeOffTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public ArrayList<FlightTicketInfo> getTikectInfo() {
        return ticketInfo;
    }

    public void setTikectInfo(ArrayList<FlightTicketInfo> ticketInfo) {
        this.ticketInfo = ticketInfo;
    }

    @Override
    public String toString() {
        return "PlaneEntity{" +
                "aPort='" + aPort + '\'' +
                ", airline='" + airline + '\'' +
                ", flight='" + flight + '\'' +
                ", fligtInfo='" + fligtInfo + '\'' +
                ", punctualityRate='" + punctualityRate + '\'' +
                ", dPort='" + dPort + '\'' +
                ", arriveCity='" + arriveCity + '\'' +
                ", departCity='" + departCity + '\'' +
                ", arriveTime='" + arriveTime + '\'' +
                ", takeOffTime='" + takeOffTime + '\'' +
                ", startTime='" + startTime + '\'' +
                ", tikectInfo=" + ticketInfo +
                '}';
    }
}
