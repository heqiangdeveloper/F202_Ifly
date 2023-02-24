package com.chinatsp.ifly.entity;

import com.chinatsp.ifly.base.BaseEntity;

public class StockEntity extends BaseEntity {
    //收盘价
    private String closingPrice;
    //当前价格
    private String currentPrice;
    //最高
    private String highPrice;
    private String logoUrl;
    //最低
    private String lowPrice;
    private String mbmChartUrl;
    //股票名称
    private String name;
    //开盘价格
    private String openingPrice;
    //涨跌率
    private String riseRate;
    //涨跌额
    private String riseValue;
    //资源名称:金融界
    private String sourceName;
    //股票代码
    private String stockCode;
    //更新时间
    private String updateDateTime;
    private String url;

    public String getClosingPrice() {
        return closingPrice;
    }

    public void setClosingPrice(String closingPrice) {
        this.closingPrice = closingPrice;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(String highPrice) {
        this.highPrice = highPrice;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(String lowPrice) {
        this.lowPrice = lowPrice;
    }

    public String getMbmChartUrl() {
        return mbmChartUrl;
    }

    public void setMbmChartUrl(String mbmChartUrl) {
        this.mbmChartUrl = mbmChartUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(String openingPrice) {
        this.openingPrice = openingPrice;
    }

    public String getRiseRate() {
        return riseRate;
    }

    public void setRiseRate(String riseRate) {
        this.riseRate = riseRate;
    }

    public String getRiseValue() {
        return riseValue;
    }

    public void setRiseValue(String riseValue) {
        this.riseValue = riseValue;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
