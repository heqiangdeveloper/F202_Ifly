package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

import java.util.ArrayList;
import java.util.List;

public class TrainEntity extends BaseEntity {
    //到达时间：2019-05-10 12:41:00
    private String arrivalTime;
    //终点站,取值为终
    private String endStationType;
    //结束时间：今天12:41:00
    private String endtime_for_voice;
    //结束时间戳
    private String endtimestamp;
    //起始站
    private String originStation;
    //车票信息
    private List<TrainTicketInfo> price = new ArrayList<>();
    //距离
    private String runDistance;
    //时长
    private String runTime;
    //起始站,取值为始
    private String startStationType;
    //开始时间:2019-05-10 11:22:00
    private String startTime;
    //开始时间:今天11:22:00
    private String starttime_for_voice;
    //开始时间戳
    private String starttimestamp;
    //终点站
    private String terminalStation;
    //列车号
    private String trainNo;
    //列车类型
    private String trainType;
    private String url;

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getEndStationType() {
        return endStationType;
    }

    public void setEndStationType(String endStationType) {
        this.endStationType = endStationType;
    }

    public String getEndtime_for_voice() {
        return endtime_for_voice;
    }

    public void setEndtime_for_voice(String endtime_for_voice) {
        this.endtime_for_voice = endtime_for_voice;
    }

    public String getEndtimestamp() {
        return endtimestamp;
    }

    public void setEndtimestamp(String endtimestamp) {
        this.endtimestamp = endtimestamp;
    }

    public String getOriginStation() {
        return originStation;
    }

    public void setOriginStation(String originStation) {
        this.originStation = originStation;
    }

    public List<TrainTicketInfo> getPrice() {
        return price;
    }

    public void setPrice(List<TrainTicketInfo> price) {
        this.price = price;
    }

    public String getRunDistance() {
        return runDistance;
    }

    public void setRunDistance(String runDistance) {
        this.runDistance = runDistance;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public String getStartStationType() {
        return startStationType;
    }

    public void setStartStationType(String startStationType) {
        this.startStationType = startStationType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStarttime_for_voice() {
        return starttime_for_voice;
    }

    public void setStarttime_for_voice(String starttime_for_voice) {
        this.starttime_for_voice = starttime_for_voice;
    }

    public String getStarttimestamp() {
        return starttimestamp;
    }

    public void setStarttimestamp(String starttimestamp) {
        this.starttimestamp = starttimestamp;
    }

    public String getTerminalStation() {
        return terminalStation;
    }

    public void setTerminalStation(String terminalStation) {
        this.terminalStation = terminalStation;
    }

    public String getTrainNo() {
        return trainNo;
    }

    public void setTrainNo(String trainNo) {
        this.trainNo = trainNo;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
