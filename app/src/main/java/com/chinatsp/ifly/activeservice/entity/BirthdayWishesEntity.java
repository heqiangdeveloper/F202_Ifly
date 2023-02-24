package com.chinatsp.ifly.activeservice.entity;

/**生日问候
 * Created by zhengxb on 2019/5/11.
 */

/**
 *
 * 节日问候
 *{"code": 0,"data": {"message": "GFDGS","sceneData": {"birthday": ""}},"errCode": 0,"msg": "请求成功"}
 */

public class BirthdayWishesEntity {
    public int code;
    public Data data;
    public String errCode;
    public String msg;

    public static class  Data{
        public String boardcastMessage;
        public String message;
        public SceneData sceneData;

        public static class SceneData{
            public String birthday;
        }
    }



}


