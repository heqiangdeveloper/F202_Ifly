package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

/**
 * Created by ytkj on 2019/7/4.
 */

public class CheXinEntity extends BaseEntity {
    /**
     * fuzzy_score : 1
     * fuzzy_type : receiver
     * id : 9a4373b481a544d78142d9f8c0de6478,1
     * name : 大哥
     * originalname : https://img.cs.leshangche.com/os-style/os-style-app/2019/05/30/1559193947632.jpg
     */

    public int fuzzy_score;
    public String fuzzy_type;
    public String id;
    public String name;
    public String originalname;
    public int conversationType;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheXinEntity that = (CheXinEntity) o;

        if (fuzzy_score != that.fuzzy_score) return false;
        if (conversationType != that.conversationType) return false;
        if (fuzzy_type != null ? !fuzzy_type.equals(that.fuzzy_type) : that.fuzzy_type != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return originalname != null ? originalname.equals(that.originalname) : that.originalname == null;
    }

    @Override
    public int hashCode() {
        int result = fuzzy_score;
        result = 31 * result + (fuzzy_type != null ? fuzzy_type.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (originalname != null ? originalname.hashCode() : 0);
        result = 31 * result + conversationType;
        return result;
    }

    @Override
    public String toString() {
        return "CheXinEntity{" +
                "fuzzy_score=" + fuzzy_score +
                ", fuzzy_type='" + fuzzy_type + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", originalname='" + originalname + '\'' +
                ", conversationType=" + conversationType +
                '}';
    }
}
