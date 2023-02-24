package com.chinatsp.ifly.entity;

import java.security.PublicKey;
import java.util.List;

/**
 * ClassName    （类名）
 * Function.     （类说明）
 * Reason        （意图）
 *
 * @author ytkj    （开发者名字）
 * @version 1.0（版本）
 * @date 2020/7/31 10:51    （创建时间）
 */

public class TspInfo {


    public int status_code;
    public String error_msg;
    public List<Content> data;


    public static class Content{
       public String msg_id;
       public String msg_type;
       public Param msg_param;
    }

    public static class Param{
        public String engineStartTime;
        public String task_id;
        public boolean reserved;
        public String reservedAddress;
        public String reservedGetInTheCarTime;
        public String longitude;
        public String latitude;
        public String address;
        public String coord_type;
        public int route_type;
        public List<Pathway> pathway;

        @Override
        public String toString() {
            return "Param{" +
                    "engineStartTime='" + engineStartTime + '\'' +
                    ", task_id='" + task_id + '\'' +
                    ", reserved=" + reserved +
                    ", reservedAddress='" + reservedAddress + '\'' +
                    ", reservedGetInTheCarTime='" + reservedGetInTheCarTime + '\'' +
                    ", longitude='" + longitude + '\'' +
                    ", latitude='" + latitude + '\'' +
                    ", address='" + address + '\'' +
                    ", coord_type='" + coord_type + '\'' +
                    ", route_type=" + route_type +
                    ", pathway=" + pathway +
                    '}';
        }
    }

    public static class Pathway{
        public String longitude;
        public String latitude;
    }


    @Override
    public String toString() {
        return "TspInfo{" +
                "status_code=" + status_code +
                ", error_msg='" + error_msg + '\'' +
                ", data=" + data +
                '}';
    }
}
