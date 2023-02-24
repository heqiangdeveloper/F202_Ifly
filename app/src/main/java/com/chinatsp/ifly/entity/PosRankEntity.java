package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

public class PosRankEntity extends BaseEntity {

    /**
     * airData : 暂不支持查询空气质量指数
     * airQuality : 良
     * city : 北京
     * date : 2019-04-03
     * dateLong : 1554220800000
     * exp : {"xc":{"expName":"洗车指数","level":"非常适宜","prompt":"洗车后，可至少保持4天车辆清洁，非常适宜洗车。"}}
     * high : 20
     * humidity : 18%
     * lastUpdateTime : 2019-04-03 16:55:08
     * low : 3
     * pm25 : 98
     * province : 北京
     * temp : 19
     * tempRange : 3℃~20℃
     * weather : 晴
     * weatherType : 0
     * wind : 西南风4级
     * windLevel : 4
     */

    private String direct;
    private String offset;
    private String ref;
    private String type;

    public String getDirect() {
        return direct;
    }

    public void setDirect(String direct) {
        this.direct = direct;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
