package com.chinatsp.ifly.utils;

/**
 * Created by Administrator on 2019/7/5.
 */

public class GPS {
    private double lat;
    private double lon;

    public GPS(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String toString() {
        return "lon:" + lon + "," + "lat:" + lat;
    }
}
