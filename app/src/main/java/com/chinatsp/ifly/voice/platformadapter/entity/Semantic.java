package com.chinatsp.ifly.voice.platformadapter.entity;

public class Semantic {

    /**
     * slots : {"endLoc":{"ori_loc":"公司","topic":"others","type":"USR_DEF"},"startLoc":{"ori_loc":"CURRENT_ORI_LOC"}}
     *
     *  {
     *       "slots": {
     *         "endLoc": {
     *           "ori_loc": "沙坪坝",
     *           "topic": "others"
     *         },
     *         "startLoc": {
     *           "ori_loc": "CURRENT_ORI_LOC"
     *         },
     *         "viaLoc": {
     *           "ori_loc": "火车站",
     *           "topic": "others"
     *         }
     *       }
     *     }
     */
    public SlotsBean slots;

    public static class SlotsBean {
        /**
         * endLoc : {"ori_loc":"公司","topic":"others","type":"USR_DEF"}
         * startLoc : {"ori_loc":"CURRENT_ORI_LOC"}
         */
        public EndLocBean endLoc;
        public EndLocBean viaLoc;
        public EndLocBean viaLocNext;
        public StartLocBean startLoc;
        /**
         * datetime : {"date":"2019-04-09","dateOrig":"明天","type":"DT_BASIC"}
         */
        public DatetimeBean datetime;

        /**
         *   "location": { "city": "首尔","cityAddr": "首尔", "type": "LOC_BASIC"},
         */
        public LocationBean location;
        /**
         *  "productName": "小t"
         */
        public String productName;
        public DateBean startDate;

        public PosRankBean posRank;
        public PageRankBean pageRank;

        public String  routeCondition;

        public String  naviInfo;

        public String  content;

        public String contentType;

        public String receiver;

        public String money;


        /**
         * "insType": "OPEN"
         */
        public String insType;

        public String tag;

        /**
         *  "waveband": "fm"
         */
        public String waveband;

        /**
         * 93.8
         */
        public String code;

        /**
         * "category": "方向盘按键"
         */
        public String category;

        /**
         * 亮度，音量调节中的具体值
         */
        public String series ;

        /**
         * "presenter": "王钢蛋"
         */
        public String presenter;
        /**
         * "artist": "刘德华",
         * "operation": "PLAY",
         * "service": "musicX",
         * "source": "谢谢你的爱",
         * "sourceType": "专辑"
         */
        public String artist;
        public String song;
        public String source;
        public String sourceType;
        public String album;
        public String moreArtist;
        public String band;
        public String gender;
        public String genre;
        public String area;
        public String lang;
        public String tags;
        public String version;
        public String episode;
        public String mediaSource;

        /**
         * "program": "二货一箩筐"
         */
        public String program;
        public String famous;
        public String name;
        public String nameOrig;
        public Object chapter;
        public String alias_name;
        //空调中的模式
        public String mode;
        public String modeValue;

        /**
         * "fuzzyPart": "4379"
         */
        public String fuzzyPart;
        /**
         *  "headNum": "189"
         */
        public String headNum;
        public String action;
		//public String nameValue;
        //氛围灯颜色
        public String color;

        //空调吹面，吹脚
        public String airflowDirection;
        //股票市场:深证/上证
        public String market;
        public String viewCmd;

        public String direction;

        public static class EndLocBean {
            /**
             * ori_loc : 公司
             * topic : others
             * type : USR_DEF
             */
            public String ori_loc;
            public String topic;
            public String type;
            public String city;
            public String cityAddr;
        }

        public static class StartLocBean {
            /**
             * ori_loc : CURRENT_ORI_LOC
             */
            public String ori_loc;
            public String type;
            public String city;
            public String cityAddr;
        }

        public static class DatetimeBean {
            /**
             * date : 2019-04-09
             * dateOrig : 明天
             * type : DT_BASIC
             */
            public String date;
            public String dateOrig;
            public String type;
        }

        public static class LocationBean {
            /**
             * city : 首尔
             * cityAddr : 首尔
             * type : LOC_BASIC
             */
            public String country;
            public String province;
            public String city;
            public String cityAddr;
            public String type;
        }

        public static class DateBean {
            /**
             * date : 2019-04-09
             * dateOrig : 明天
             * type : DT_BASIC
             */
            public String date;
            public String dateOrig;
            public String type;
        }

        public static class PosRankBean {
            public String direct;
            public String offset;
            public String ref;
            public String type;
        }
        public static class PageRankBean {
            public String direct;
            public String offset;
            public String ref;
            public String type;
        }

    }
}
