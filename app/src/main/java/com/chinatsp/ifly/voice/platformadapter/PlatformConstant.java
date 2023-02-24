package com.chinatsp.ifly.voice.platformadapter;


public class PlatformConstant {

    public static final int SUCCESS = 0;//操作成功。
    public static final int INVALID_REQUEST = 1;//无效请求， 通常在用户提交的请求参数有误时返回。
    public static final int OPERATION_FAILED = 3;//业务操作失败， 通常该业务出现了异常状况，如无法访问到结果数据等。错误信息在 error 字段描述，具体参见业务相关的错误信息。
    public static final int UNKNOWN_SERVICE = 4;//服务不理解或不能处理该文本。 文本没有匹配的服务场景*/

    public class Service {
        public static final String MAP_U = "mapU";
        public static final String JOKE = "joke";
        public static final String NEWS = "news";
        public static final String BAIKE = "baike";
        public static final String MUSIC = "musicX";
        public static final String WEATHER = "weather";
        public static final String TELEPHONE = "telephone";
        public static final String AIRCONTROL = "airControl";
        public static final String CARCONTROL = "carControl"; //打开车窗
        public static final String PERSONALNAME = "personalName"; //取个名字
        public static final String CMD = "cmd";
        public static final String APP = "app";
        public static final String RADIO = "radio";
        public static final String INTERNETRADIO = "internetRadio";
        public static final String HELP = "help";
        public static final String Flight = "flight";
        public static final String TRAIN = "train";
        public static final String STOCK = "stock";
 		public static final String WEIXIN = "weixin";
        public static final String FEEDBACK = "feedback";
        public static final String VIDEO = "video";
        public static final String VIEWCMD = "viewCmd";
        public static final String CHANGBA = "changBa";
        public static final String DATETIME = "datetimeX";
        public static final String POETRY = "poetry";
        public static final String CALC = "calc";

        public static final String SKILL_QA = "garbageClassify";
        public static final String SKILL_ACTION = "carNumber";
    }

    public class Operation {
        public static final String QUERY = "QUERY";
        public static final String OPEN = "OPEN";
        public static final String LOCATE = "LOCATE";
        public static final String DIAL = "DIAL";
        public static final String PLAY = "PLAY";
        public static final String INSTRUCTION = "INSTRUCTION";
        public static final String SET = "SET";
        public static final String USR_POI_QUERY = "USR_POI_QUERY";
        public static final String USR_POI_SET = "USR_POI_SET";
        public static final String ALONG_SEARCH = "ALONG_SEARCH";
        public static final String CANCEL = "CANCEL";
        public static final String LAUNCH = "LAUNCH";
        public static final String EXIT = "EXIT";
        public static final String CLOSE = "CLOSE";
        public static final String NEXT = "NEXT";
        public static final String COLLECT  = "COLLECT";
        public static final String OPEN_TRAFFIC_INFO   ="OPEN_TRAFFIC_INFO";
        public static final String CLOSE_TRAFFIC_INFO   ="CLOSE_TRAFFIC_INFO";
        public static final String CLOSE_MAP="CLOSE_MAP";
        public static final String CANCEL_MAP="CANCEL_MAP";
        public static final String SEND="SEND";
        public static final String QUERY_TRAFFIC_INFO="QUERY_TRAFFIC_INFO";
        public static final String CONFIRM="CONFIRM";
        public static final String POS_RANK="POS_RANK";
        public static final String PAGE_RANK="PAGE_RANK";
        public static final String RESET_POI="RESET_POI";
        public static final String ZOOM_IN="ZOOM_IN";
        public static final String ZOOM_OUT="ZOOM_OUT";
        public static final String DISPLAY_MODE_DAY="DISPLAY_MODE_DAY";
        public static final String DISPLAY_MODE_NIGHT="DISPLAY_MODE_NIGHT";
        public static final String ROUTE_PLAN="ROUTE_PLAN";
        public static final String NAVI_INFO="NAVI_INFO";
        public static final String VIEW_TRANS_2D="VIEW_TRANS_2D";
        public static final String VIEW_TRANS_HEAD_UP = "VIEW_TRANS_HEAD_UP";
        public static final String VIEW_TRANS_NORTH_UP="VIEW_TRANS_NORTH_UP";
        public static final String VIEW_TRANS_3D="VIEW_TRANS_3D";
        public static final String PASS_AWAY="PASS_AWAY";
        public static final String SILENT="SILENT";
        public static final String VIEWCMD="VIEWCMD";

        public static final String VIEW_HISTOGRAM_MAP = "VIEW_HISTOGRAM_MAP";//切换到柱状图/路况图/路况条
        public static final String VIEW_EAGLE_EYE_MAP = "VIEW_EAGLE_EYE_MAP";//切换到小地图/鹰眼图
    }

    public class InsType {
        public static final String CONFIRM = "CONFIRM";
        public static final String QUIT = "QUIT";
    }

    public class Topic {
        public static final String HOTEL = "hotel";  //酒店
        public static final String RESTAURANT = "restaurant"; //餐馆
        public static final String PARKING = "parking"; //停车场
        public static final String GAS_STATION = "gas_station"; //加油站
        public static final String CHARGING_PILE = "charging_pile"; //充电桩
        public static final String HOME = "home"; //家 ：自定义
        public static final String COMPANY = "company"; //公司 ： 自定义
        public static final String ROUTEP = "routep"; //途经点 ： 自定义
        public static final String TOILET = "toilet"; //洗手间/卫生间/厕所 ： 自定义
        public static final String RESORT = "resort"; //公园/游乐场/游乐园 ： 自定义
        public static final String OTHERS = "others";
        public static final String SPOT = "scenic_spot";//景点
        public static final String ATM = "ATM";//ATM
        public static final String SAVING_STATION = "serving_station";//维修站

    }

    /**
     * 沿途搜索支持类型
     */
    public static final String[] ALONG_TOPIC = {
            Topic.GAS_STATION,
            Topic.CHARGING_PILE,
            Topic.ATM,
            Topic.TOILET,
            Topic.SAVING_STATION,
    };

    public class ContentType {
        public static final String TEXT = "text";
        public static final String REDPACKET = "redpacket";
        public static final String POSITION = "position";

    }


}
