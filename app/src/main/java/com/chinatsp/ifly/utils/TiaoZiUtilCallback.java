package com.chinatsp.ifly.utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

/**
 * Created by Administrator on 2019/6/15.
 */

public class TiaoZiUtilCallback {
    private TextView tv;
    private String s;
    private int length;
    private long time;
    static int n = 0;
    private int nn;
    private OnTiaoZiStoppedListener onTiaoZiStoppedListener;
    private String color;
    private SpannableStringBuilder sb;
    private ForegroundColorSpan fcs;

    public TiaoZiUtilCallback(String color,TextView tv, String s, long time, OnTiaoZiStoppedListener onTiaoZiStoppedListener) {
        this.color = color;
        this.onTiaoZiStoppedListener = onTiaoZiStoppedListener;
        this.tv = tv;//textview
        this.s = s;//字符串
        this.time = time;//间隔时间
        this.length = s.length();
        startTv(n);//开启线程
    }

    public interface OnTiaoZiStoppedListener {
        void onPlayStopped();
    }

    public void startTv(final int n) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String stv = s.substring(0, n);//截取要填充的字符串
                    if(null != tv){
                        tv.post(new Runnable() {
                            @Override
                            public void run() {
//                                if(!TextUtils.isEmpty(color)){
//                                    sb = new SpannableStringBuilder(stv); // 包装字体内容
//                                    fcs = new ForegroundColorSpan(Color.parseColor(color)); // 设置字体颜色
//
//                                    if(n <= 5){
//                                        sb.setSpan(fcs, 0, n, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                                    }
//                                }
                                tv.setText(stv);
                            }
                        });
                    }

                    Thread.sleep(time);//休息片刻
                    nn = n + 1;//n+1；多截取一个
                    if (nn <= length) {//如果还有汉字，那么继续开启线程，相当于递归的感觉
                        startTv(nn);
                    }else {
                        if(null !=onTiaoZiStoppedListener){
                            onTiaoZiStoppedListener.onPlayStopped();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }
}
