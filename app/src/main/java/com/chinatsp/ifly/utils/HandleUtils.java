package com.chinatsp.ifly.utils;


import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public class HandleUtils {

    private static HandleUtils instance;
    private Handler mHandler;

    public static final int MSG_DELAY_SRC_TTS = 1001;
    public static final int TIME_SRC_TTS = 300;

    public static HandleUtils getInstance(){
        if(instance==null){
            synchronized (HandleUtils.class){
                instance = new HandleUtils();
            }
        }
        return instance;
    }

    private HandleUtils(){
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    public final boolean postDelayed(Runnable r, long delayMillis)
    {
        return mHandler.postDelayed(r,delayMillis);
    }

    public final boolean sendMessageDelayed(Message msg, long delayMillis)
    {
        return mHandler.sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {

        return mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    public final boolean hasMessages(int what) {
        return mHandler.hasMessages(what);
    }

    public final void removeCallbacksAndMessages(Object token) {
        mHandler.removeCallbacksAndMessages(token);
    }

}
