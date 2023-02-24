package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

import java.util.List;

/**
 * Created by ytkj on 2019/5/23.
 */

public class AllDimension extends BaseEntity {

    private int id;

    private List<DimensionBean> dimension;

    public static class DimensionBean{

        private String field="map";

        private boolean spword=false;

        private String val;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public boolean isSpword() {
            return spword;
        }

        public void setSpword(boolean spword) {
            this.spword = spword;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return "DimensionBean{" +
                    "field='" + field + '\'' +
                    ", spword=" + spword +
                    ", val='" + val + '\'' +
                    '}';
        }
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<DimensionBean> getdimension() {
        return dimension;
    }

    public void setdimension(List<DimensionBean> dimension) {
        this.dimension = dimension;
    }

    @Override
    public String toString() {
        return "AllDimension{" +
                "id=" + id +
                ", dimension=" + dimension +
                '}';
    }
}
