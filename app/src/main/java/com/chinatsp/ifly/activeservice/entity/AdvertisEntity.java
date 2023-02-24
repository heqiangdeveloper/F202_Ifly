package com.chinatsp.ifly.activeservice.entity;

import java.util.List;

/**
 * Created by ytkj on 2019/9/21.
 */

public class AdvertisEntity {
    public int status_code;
    public AdvertisData data;

    public static class AdvertisData{
       public List<PageItems> pageItems;

    }

    public static class PageItems{
        public String publishTime;
        public boolean showPermanentCalendar;
        public String aiResUrl;
        public int level;
        public int showTime;
        public String welcomeUrl;
        public ItemMap itemMap;
        public String type;
        public String sortCode;
        public String resId;
        public String audioUrl;
        public String videoUrl;
        public String name;
        public String startTime;
        public String endTime;
        public String status;

    }
    public static class ItemMap{
        public String pushTypeId;
        public String videoResName;
        public String rangeType;
        public String test;
        public String subject;
        public String colorOfLump;
        public String remark;
        public String dialogUrl;
        public String logoUrl;
        public String voiceContent;
        public String logoResName;
        public int deleted;
        public String appFit;
        public String bgColor;
        public String copywriting;
        public String tenantId;
        public String audioResName;


    }
}
