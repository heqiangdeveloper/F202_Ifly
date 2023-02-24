package com.chinatsp.ifly.activeservice.entity;

import java.util.List;

/**
 * Created by zhengxb on 2019/5/11.
 *
 * 上班路况提醒
 {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"gowork"}



 {
 "code": 0,
 "data": {
 "message": "现在去公司预计需要634分钟，大约行驶7954公里，可以点击查看路况",
 "sceneData": {
 "traffic_condition": [
 {
 "distance": 597,
 "status": "未知"
 },
 {
 "distance": 6102,
 "status": "畅通"
 },
 {
 "distance": 1255,
 "status": "未知"
 }
 ],
 "spend_time": 634,
 "mileage": 7954
 }
 },
 "errCode": 0,
 "msg": "请求成功"
 }
 */

public class InWorkLineEntity {
    public int code;
    public InWorkData data;
    public int errCode;
    public String msg;

    public static class InWorkData{
        public String boardcastMessage;
        public String message;
        public InWorkData.SceneData sceneData;


        public static class SceneData{
            public int spend_time;
            public int mileage;
            public String company;
            public List<TrafficCondition> traffic_condition;
        }
    }

    public static class TrafficCondition{

        public float distance;
        public String status;

    }

}
