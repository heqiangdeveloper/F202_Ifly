package com.chinatsp.ifly.activeservice.entity;

/**
 * 异常天气
 * Created by zhengxb on 2019/5/11.
 */


/**
 * {
 "code": 0,
 "data": {
          "message": "今天河北,天气状况多云,温度27，风向东风力≤3行车注意安全。",
            "sceneData": {
                "province": "河北",
                 "city": "南和县",
                 "windpower": "≤3",
                  "weather": "多云",
                 " temperature": "27",
                  "humidity": "34",
                 "reporttime": "2019-05-10 14:56:47",
                 " winddirection": "东"
               }
          },
 "errCode": 0,
 "msg": "请求成功"
 }
 */

public class AbnormalWeatherEntity {
    public int code;
    public  AbnormalData data;
    public int errCode;
    public String msg;


    public static class AbnormalData{
        public String boardcastMessage;
        public String message;
        public SceneData sceneData;

        public static class SceneData{

            public String province;
            public String city;
            public String windpower;
            public String weather;
            public String temperature;
            public String humidity;
            public String reporttime;
            public String winddirection;

        }

    }
}
