package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

public class WeatherEntity extends BaseEntity {

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

    private String airData;
    private String airQuality;
    private String city;
    private String date;
    private long dateLong;
    private ExpBean exp;
    private int high;
    private String humidity;
    private String lastUpdateTime;
    private int low;
    private int pm25;
    private String province;
    private int temp;
    private String tempRange;
    private String weather;
    private int weatherType;
    private String wind;
    private int windLevel;

    public String getAirData() {
        return airData;
    }

    public void setAirData(String airData) {
        this.airData = airData;
    }

    public String getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(String airQuality) {
        this.airQuality = airQuality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDateLong() {
        return dateLong;
    }

    public void setDateLong(long dateLong) {
        this.dateLong = dateLong;
    }

    public ExpBean getExp() {
        return exp;
    }

    public void setExp(ExpBean exp) {
        this.exp = exp;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getPm25() {
        return pm25;
    }

    public void setPm25(int pm25) {
        this.pm25 = pm25;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getTempRange() {
        return tempRange;
    }

    public void setTempRange(String tempRange) {
        this.tempRange = tempRange;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getWeatherType() {
        return weatherType;
    }

    public void setWeatherType(int weatherType) {
        this.weatherType = weatherType;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public int getWindLevel() {
        return windLevel;
    }

    public void setWindLevel(int windLevel) {
        this.windLevel = windLevel;
    }

    public static class ExpBean {
        /**
         * xc : {"expName":"洗车指数","level":"非常适宜","prompt":"洗车后，可至少保持4天车辆清洁，非常适宜洗车。"}
         */

        private XcBean xc;

        public XcBean getXc() {
            return xc;
        }

        public void setXc(XcBean xc) {
            this.xc = xc;
        }

        public static class XcBean {
            /**
             * expName : 洗车指数
             * level : 非常适宜
             * prompt : 洗车后，可至少保持4天车辆清洁，非常适宜洗车。
             */

            private String expName;
            private String level;
            private String prompt;

            public String getExpName() {
                return expName;
            }

            public void setExpName(String expName) {
                this.expName = expName;
            }

            public String getLevel() {
                return level;
            }

            public void setLevel(String level) {
                this.level = level;
            }

            public String getPrompt() {
                return prompt;
            }

            public void setPrompt(String prompt) {
                this.prompt = prompt;
            }
        }
    }
}
