package com.chinatsp.ifly.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    //把日期转为字符串  
    public static String converToString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return df.format(date);
    }

    //把字符串转为日期  
    public static Date converToDate(String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            //e.printStackTrace();
        }
        return null;
    }

    //获取两个日期之间的间隔天数
    public static int getGapCount(Date startDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }


}  