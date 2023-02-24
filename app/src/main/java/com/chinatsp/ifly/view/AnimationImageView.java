package com.chinatsp.ifly.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class AnimationImageView extends AppCompatImageView {

    private Handler handler = new Handler();

    public AnimationImageView(Context context) {
        super(context);
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface OnFrameAnimationListener {
        /**
         * 动画开始播放后调用
         */
        void onStart();

        /**
         * 动画结束播放后调用
         */
        void onEnd();
    }

    /**
     * 不带动画监听的播放
     *
     * @param resId
     */
    public void loadAnimation(int resId) {
        setImageResource(resId);
        AnimationDrawable anim = (AnimationDrawable) getDrawable();
        anim.start();
    }

    /**
     * 不带动画监听的播放
     *
     * @param drawable
     */
    public void loadAnimation(AnimationDrawable drawable) {
        setImageDrawable(drawable);
        AnimationDrawable anim = (AnimationDrawable) getDrawable();
        anim.start();
    }

    /**
     * 带动画监听的播放
     *
     * @param drawable
     * @param listener
     */
    public void loadAnimation(AnimationDrawable drawable, final OnFrameAnimationListener listener) {
        setImageDrawable(drawable);
        AnimationDrawable anim = (AnimationDrawable) getDrawable();
        anim.start();
        if (listener != null) {
            // 调用回调函数onStart
            listener.onStart();
        }

        // 计算动态图片所花费的事件
        int durationTime = 0;
        for (int i = 0; i < anim.getNumberOfFrames(); i++) {
            durationTime += anim.getDuration(i);
        }

        // 动画结束后
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (listener != null) {
                    // 调用回调函数onEnd
                    listener.onEnd();
                }
            }
        }, durationTime);
    }

    /**
     * 不带动画监听的播放
     */
    public void stopAnimation() {
        AnimationDrawable anim = (AnimationDrawable) getDrawable();
        if (anim != null) {
            anim.stop();
        }
    }
}