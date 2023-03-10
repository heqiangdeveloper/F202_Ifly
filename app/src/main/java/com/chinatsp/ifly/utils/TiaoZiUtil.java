package com.chinatsp.ifly.utils;

import android.widget.TextView;

/**
 * Created by Administrator on 2019/6/15.
 */

public class TiaoZiUtil {
    private static TextView tv;
    private static String s;
    private static int length;
    private static long time;
    static int n = 0;
    private static int nn;
    private OnTiaoZiStoppedListener onTiaoZiStoppedListener;

    public TiaoZiUtil(TextView tv, String s, long time) {
        this.tv = tv;//textview
        this.s = s;//字符串
        this.time = time;//间隔时间
        this.length = s.length();
        startTv(n);//开启线程
    }

    public TiaoZiUtil(TextView tv, String s, long time, OnTiaoZiStoppedListener onTiaoZiStoppedListener) {
        this.onTiaoZiStoppedListener = onTiaoZiStoppedListener;
        this.tv = tv;//textview
        this.s = s;//字符串
        this.time = time;//间隔时间
        this.length = s.length();
        startTv2(n);//开启线程
    }

    public TiaoZiUtil(TextView tv, String s) {
        this.tv = tv;//textview
        tv.setText(s);
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
                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(stv);
                        }
                    });
                    Thread.sleep(time);//休息片刻
                    nn = n + 1;//n+1；多截取一个
                    if (nn <= length) {//如果还有汉字，那么继续开启线程，相当于递归的感觉
                        startTv(nn);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }

    public void startTv2(final int n) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String stv = s.substring(0, n);//截取要填充的字符串
                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(stv);
                        }
                    });
                    Thread.sleep(time);//休息片刻
                    nn = n + 1;//n+1；多截取一个
                    if (nn <= length) {//如果还有汉字，那么继续开启线程，相当于递归的感觉
                        startTv2(nn);
                    }else {
                        onTiaoZiStoppedListener.onPlayStopped();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }
}
