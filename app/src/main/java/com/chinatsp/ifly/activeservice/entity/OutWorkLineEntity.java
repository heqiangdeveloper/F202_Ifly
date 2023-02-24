package com.chinatsp.ifly.activeservice.entity;

import java.util.List;

/**
 * Created by zhengxb on 2019/5/11.
 * <p>
 * {
 * "code": 0,
 * "data": {
 * "message": "现在回家预计需要628分钟，大约行驶6149公里，可以点击查看路况",
 * "sceneData": {
 * "traffic_condition": [
 * {
 * "distance": 998,
 * "status": "未知"
 * },
 * {
 * "distance": 4554,
 * "status": "畅通"
 * },
 * {
 * "distance": 597,
 * "status": "未知"
 * }
 * ],
 * "spend_time": 628,
 * "mileage": 6149
 * }
 * },
 * "errCode": 0,
 * "msg": "请求成功"
 * }
 */

public class OutWorkLineEntity {

    public int code;
    public InWorkData data;
    public int errCode;
    public String msg;

    public static class InWorkData{
        public String boardcastMessage;
        public String message;
        public SceneData sceneData;


        public static class SceneData{
            public int spend_time;
            public int mileage;
            public String home;
            public List<TrafficCondition> traffic_condition;
        }
    }

    public static class TrafficCondition{

        public float distance;
        public String status;

    }
}
