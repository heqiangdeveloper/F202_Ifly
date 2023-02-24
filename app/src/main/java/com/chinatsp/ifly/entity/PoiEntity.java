package com.chinatsp.ifly.entity;

import android.text.TextUtils;

import com.chinatsp.ifly.base.BaseEntity;

/**
 * poi实体类
 */
public class PoiEntity extends BaseEntity {
    public int flag;// 行数标识 0标识占一行 1标识占两行
    private String address ;// 深南大道9028-2号深圳益田威斯汀酒店1楼
    private String area ;//南山区
    private String category ; //餐饮服务;餐饮相关;餐饮相关场所
    private String city ;//深圳市
    private String distance ; //1946908.0
    private String latitude ; //22.537618
    private String longitude ; //113.976036
    private String name ; //威斯汀酒店·大堂吧
    private String phone ; //0755-26988888
    private String photoUrl ; //http://store.is.autonavi.com/showpic/6be01fbaa8639cd88319dbc67d75656c
    private String poiType ; //endLoc
    private String province ; //广东省
    private String source_category ;//gaode_poi
    private String score; //4.8
    private String price; //343.6
    private String entrLocation; //"114.307207,30.955466"

//    public PoiEntity(String address, String area, String category, String city, String distance, String latitude, String longitude, String name, String phone, String photoUrl, String poiType, String province, String source_category) {
//        this.address = address;
//        this.area = area;
//        this.category = category;
//        this.city = city;
//        this.distance = distance;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.name = name;
//        this.phone = phone;
//        this.photoUrl = photoUrl;
//        this.poiType = poiType;
//        this.province = province;
//        this.source_category = source_category;
//    }

    public String getNaviLatitude() {
        if (TextUtils.isEmpty(entrLocation)) {
            return "";
        }
        try {
            String[] arr = entrLocation.split(",");
            return arr[1];
        } catch (Exception e) {}
        return "";
    }

    public String getNaviLongitude() {
        if (TextUtils.isEmpty(entrLocation)) {
            return "";
        }
        try {
            String[] arr = entrLocation.split(",");
            return arr[0];
        } catch (Exception e) {}
        return "";
    }

    public String getEntrLocation() {
        return entrLocation;
    }

    public void setEntrLocation(String entrLocation) {
        this.entrLocation = entrLocation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPoiType() {
        return poiType;
    }

    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getSource_category() {
        return source_category;
    }

    public void setSource_category(String source_category) {
        this.source_category = source_category;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "PoiEntity{" +
                "flag=" + flag +
                ", address='" + address + '\'' +
                ", area='" + area + '\'' +
                ", category='" + category + '\'' +
                ", city='" + city + '\'' +
                ", distance='" + distance + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", poiType='" + poiType + '\'' +
                ", province='" + province + '\'' +
                ", source_category='" + source_category + '\'' +
                ", score='" + score + '\'' +
                ", price='" + price + '\'' +
                ", entrLocation='" + entrLocation + '\'' +
                '}';
    }
}
