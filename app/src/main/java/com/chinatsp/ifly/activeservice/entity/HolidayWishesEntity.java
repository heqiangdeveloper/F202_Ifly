package com.chinatsp.ifly.activeservice.entity;

/**节日问候
 * Created by zhengxb on 2019/5/11.
 */

import java.util.List;

/**
 *
 * 节日问候
 *{/**
 * {
 "code": 0,
 "data": {
 "message": "GFDGS",
 "sceneData": {
 "festival": [
 {
 "date": "2019-03-08",
 "showName": "妇女节"
 },
 {
 "date": "2019-05-01",
 "showName": "劳动节"
 },
 {
 "date": "2019-05-02",
 "showName": "劳动节"
 },
 {
 "date": "2019-05-03",
 "showName": "劳动节"
 },
 {
 "date": "2019-05-04",
 "showName": "劳动节"
 },
 {
 "date": "2019-04-05",
 "showName": "清明节"
 },
 {
 "date": "2019-10-01",
 "showName": "国庆节"
 },
 {
 "date": "2019-10-02",
 "showName": "国庆节"
 },
 {
 "date": "2019-10-03",
 "showName": "国庆节"
 },
 {
 "date": "2019-10-04",
 "showName": "国庆节"
 },
 {
 "date": "2019-10-05",
 "showName": "国庆节"
 },
 {
 "date": "2019-10-06",
 "showName": "国庆节"
 },
 {
 "date": "2019-10-07",
 "showName": "国庆节"
 }
 ]
 }
 },
 "errCode": 0,
 "msg": "请求成功"
 }
 */

public class HolidayWishesEntity {
    public  int code;
    public FestivalData data;
    public int errCode;
    public String msg;

    public static  class FestivalData{
        public String message;
        public FestivalData.SceneData sceneData;

        public static class SceneData{
            public List<Festival> festival;
            public String version;
        }


    }
    public static class Festival{
        public String date;
        public String showName;
        public String content;

    }



}


