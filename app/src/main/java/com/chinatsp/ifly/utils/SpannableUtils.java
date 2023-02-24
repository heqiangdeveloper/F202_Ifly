package com.chinatsp.ifly.utils;


import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class SpannableUtils {


    public static SpannableStringBuilder formatString(String text, int start1, int end1,  int color1, int start2, int end2,  int color2) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
        //用颜色标记
        stringBuilder.setSpan(new ForegroundColorSpan(color1), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(new ForegroundColorSpan(color2), start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringBuilder;
    }

    public static SpannableStringBuilder formatString(String text, int start, int end, int color) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
        //用颜色标记
        stringBuilder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringBuilder;
    }
}
