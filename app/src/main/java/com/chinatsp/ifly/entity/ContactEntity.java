package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

public class ContactEntity extends BaseEntity {
    /**
     * fuzzy_score : 0.9998999834060668
     * fuzzy_type : name
     * id : 18
     * location : {"city":"大连市","province":"辽宁"}
     * name : 你好棒
     * phoneNumber : 13998453903
     * teleOper : 移动
     */
    private double fuzzy_score;
    private String fuzzy_type;
    public String id;
    public LocationBean location;
    public String name;
    public String phoneNumber;
    public String teleOper;

    public static class LocationBean {
        /**
         * city : 大连市
         * province : 辽宁
         */
        public String city;
        public String province;
    }

    public ContactEntity(String name, String number) {
        this.name = name;
        this.phoneNumber = number;
    }

    @Override
    public String toString() {
        return "ContactEntity{" +
                "id=" + id +
                ", location=" + location +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", teleOper='" + teleOper + '\'' +
                '}';
    }
}
