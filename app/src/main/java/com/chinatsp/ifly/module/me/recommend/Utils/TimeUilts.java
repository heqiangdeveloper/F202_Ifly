package com.chinatsp.ifly.module.me.recommend.Utils;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/6/19
 */

public class TimeUilts {

    public static String timeTraToHMS(long time, boolean showH) {
        int hGet = (int) (time / (1000 * 60 * 60));
        int mGet = (int) (time - hGet * 1000 * 60 * 60) / (1000 * 60);
        int sGet = (int) (time - hGet * 1000 * 60 * 60 - mGet * 1000 * 60) / 1000;

        String H = "";
        String M = "00:";
        String S = "00";
        if (hGet > 0 || showH) {
            H = hGet + ":";
            if (hGet < 10) {
                H = "0" + hGet + ":";
            }
        }

        if (mGet > 0) {
            M = mGet + ":";
            if (mGet < 10) {
                M = "0" + mGet + ":";
            }
        }

        if (sGet > 0) {
            S = sGet + "";
            if (sGet < 10) {
                S = "0" + sGet;
            }
        }
        String hms = H + M + S;
        // Untils.LogX(TAG, "timeTraToHMS()---time:" + time + "--hms:" + hms);
        return hms;
    }

    public static String timeTraToHMS(long time) {

        return timeTraToHMS(time, false);

    }

}
