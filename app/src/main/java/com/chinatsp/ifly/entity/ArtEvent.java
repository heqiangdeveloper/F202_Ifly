package com.chinatsp.ifly.entity;

import android.text.SpannableStringBuilder;

import com.chinatsp.ifly.view.AnimationImageView;

public class ArtEvent {
    public ArtEvent() {
    }

    public interface AnimType {
        int ANIM_WAKEUP = 0;
        int ANIM_NORMAL = 1;
        int ANIM_LISTENING = 2;
        int ANIM_RECOGNIZING_1 = 3;
        int ANIM_RECOGNIZING_2 = 4;
        int ANIM_RECOGNIZING_3 = 5;
        int ANIM_GREETING = 6;
    }

    public interface RightImageType {
        int IMAGE_SETTINGS = 0;
        int IMAGE_BACKUP = 1;
    }

    public enum EventType {
        ANIM , //悬浮框精灵动画
        EXIT, //退出
        LEFT_IMG, //悬浮框左下角IMAGE
        RIGHT_IMG,  //悬浮框右下角IMAGE
        DAY_MODE,  //白天模式
        NIGHT_MODE,  //黑夜模式
        GONE, //是否隐藏精灵
        ICONTEXT, //图片文字
        SPEED //速度
    }

    public EventType eventType; //事件类型
    public AnimationImageView.OnFrameAnimationListener animationListener;
    public int animType;
    public boolean leftExitShow; //左下角退出图标显示或隐藏
    public int rightImageType; //右下角图片类型 0: 显示设置图片  1：显示语音设置二级界面后退图标
    public String whichIcon; //显示那个按键
    public SpannableStringBuilder iconText;//要显示的图片文字内容
    public String speed; //速度

}
