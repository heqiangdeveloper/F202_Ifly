package com.chinatsp.ifly.entity;

import java.util.List;

public class TtsEntity {
    public String msg;
    public String code;
    public String errCode;
    public String projectid;
    public ttsVersionInfo data;
    public class ttsVersionInfo{
        public String oldversion;
        public String newversion;
        public List<skillTtsInfo> newversiondata;
    }

    public class skillTtsInfo{
        public String skillid;
        public String skillversion;
        public List<conditionTtsInfo> data;

        @Override
        public String toString() {
            return "skillTtsInfo{" +
                    "skillid='" + skillid + '\'' +
                    ", skillversion='" + skillversion + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

    public class conditionTtsInfo{
        public int isTtsAvailable;
        public String conditionid;
        public List<ttsTextInfo> data;

        @Override
        public String toString() {
            return "conditionTtsInfo{" +
                    "conditionid='" + conditionid + '\'' +
                    ", data=" + data +
                    '}';
        }
    }

    public class ttsTextInfo{
        public String ttsid;
        public String ttsText;
        public String valid_starttime;
        public String velid_endtime;
        public String offline_broadcast;
        public int baseResponse;

        @Override
        public String toString() {
            return "ttsTextInfo{" +
                    "ttsid='" + ttsid + '\'' +
                    ", ttsText='" + ttsText + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "TtsEntity{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", errorCode='" + errCode + '\'' +
                ", projectid='" + projectid + '\'' +
                ", data=" + data +
                '}';
    }
}
