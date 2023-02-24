package com.chinatsp.ifly.activeservice.entity;

import java.util.List;

/**
 * {
 "code": 0,
 "data": {
 "boardcastMessage": "开始语音播报~你所在的城市为河北省",
 "message": "你好测试人员，你所在的城市是河北省",
 "sceneData": {
 "city": "河北省"
 }
 },
 "errCode": 0,
 "msg": "请求成功"
 }
 */

/**
 * {"token":"6828d858-28a6-4dfa-8919-46a1dae3e08a","taskId":"hotspot","data":{"gps":"114.78716659999998,37.042333299999996"}}
 */

public class HotspotEntity {

    public int code;
    public Data data;
    public int errCode;
    public String msg;

    public static class  Data{
        public String boardcastMessage;
        public String message;
        public SceneData sceneData;

        public static class SceneData{
            public String city;
        }
    }

}
