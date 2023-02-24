package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;
import com.example.mxextend.entity.LocationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 美行poi实体类
 */
public class MXPoiEntity extends BaseEntity {

    private String name;
    private String address;
    private String longitude;
    private String latitude;
    private String cityName;
    private String phone;
    private String distance;
    private String naviLatitude;
    private String naviLongitude;

    public MXPoiEntity(LocationInfo locationInfo) {
        this.name = locationInfo.getName();
        this.address = locationInfo.getAddress();
        this.latitude = String.valueOf(locationInfo.getLatitude());
        this.longitude = String.valueOf(locationInfo.getLongitude());
        this.cityName = locationInfo.getCityName();
        this.phone = locationInfo.getPhone();
        this.distance = String.valueOf(locationInfo.getDistance());
        this.naviLatitude = String.valueOf(locationInfo.getNaviLatitude());
        this.naviLongitude = String.valueOf(locationInfo.getNaviLongitude());
    }

    public MXPoiEntity(PoiEntity poiEntity) {
        this.name = poiEntity.getName();
        this.address = poiEntity.getAddress();
        this.latitude = poiEntity.getLatitude();
        this.longitude = poiEntity.getLongitude();
        this.cityName = poiEntity.getCity();
        this.phone = poiEntity.getPhone();
        this.distance = poiEntity.getDistance();
        this.naviLatitude = String.valueOf(poiEntity.getNaviLatitude());
        this.naviLongitude = String.valueOf(poiEntity.getNaviLongitude());
    }

    public static List<MXPoiEntity> wrapMx(List<LocationInfo> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        List<MXPoiEntity> entityWrapperList = new ArrayList<>(entityList.size());
        for (LocationInfo entity : entityList) {
            entityWrapperList.add(new MXPoiEntity(entity));
        }
        return entityWrapperList;
    }

    public static List<MXPoiEntity> wrapIfly(List<PoiEntity> entityList) {
        if (entityList == null || entityList.size() == 0) {
            return null;
        }
        List<MXPoiEntity> entityWrapperList = new ArrayList<>(entityList.size());
        for (PoiEntity entity : entityList) {
            entityWrapperList.add(new MXPoiEntity(entity));
        }
        return entityWrapperList;
    }

    public String getNaviLatitude() {
        return naviLatitude;
    }

    public void setNaviLatitude(String naviLatitude) {
        this.naviLatitude = naviLatitude;
    }

    public String getNaviLongitude() {
        return naviLongitude;
    }

    public void setNaviLongitude(String naviLongitude) {
        this.naviLongitude = naviLongitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "MXPoiEntity{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", cityName='" + cityName + '\'' +
                ", phone='" + phone + '\'' +
                ", distance='" + distance + '\'' +
                ", naviLatitude='" + naviLatitude + '\'' +
                ", naviLongitude='" + naviLongitude + '\'' +
                '}';
    }
}
