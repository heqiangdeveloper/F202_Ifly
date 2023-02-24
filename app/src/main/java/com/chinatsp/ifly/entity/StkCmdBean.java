package com.chinatsp.ifly.entity;

import java.util.List;

/**
 * Created by ytkj on 2019/5/24.
 */

public class StkCmdBean {

    private List<AllDimension> list;
    private String type="poi";
    private List<String> nliFieldSearch;

    public List<AllDimension> getAllDimensions() {
        return list;
    }

    public void setAllDimensions(List<AllDimension> allDimensions) {
        this.list = allDimensions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getNliFieldSearch() {
        return nliFieldSearch;
    }

    public void setNliFieldSearch(List<String> nliFieldSearch) {
        this.nliFieldSearch = nliFieldSearch;
    }

    @Override
    public String toString() {
        return "StkCmdBean{" +
                "allDimensions=" + list +
                ", type='" + type + '\'' +
                ", nliFieldSearch=" + nliFieldSearch +
                '}';
    }
}
