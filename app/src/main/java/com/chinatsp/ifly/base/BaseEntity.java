package com.chinatsp.ifly.base;

import java.io.Serializable;

public class BaseEntity implements Serializable {

    public String getName() {
        return "";
    }

    public String getAddress() {
        return "";
    }

    public String getLatitude() {
        return "";
    }

    public String getLongitude() {
        return "";
    }

    /**
     * 引导纬度
     * @return
     */
    public String getNaviLatitude() {
        return "";
    }

    /**
     * 引导经度
     * @return
     */
    public String getNaviLongitude() {
        return "";
    }

}
