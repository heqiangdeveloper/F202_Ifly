package com.chinatsp.ifly.utils;

import android.text.SpannableStringBuilder;

import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.entity.SREvent;
import com.chinatsp.ifly.view.AnimationImageView;

import org.greenrobot.eventbus.EventBus;

public class EventBusUtils {

    public static void sendSpeechingMessage(CharSequence mainMsg, CharSequence duptyMsg, String talkMsg,int resId) {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.eventType = MessageEvent.EventType.SPEECHING;
        messageEvent.mainMessage = mainMsg;
        messageEvent.deputyMessage = duptyMsg;
        messageEvent.talkMessage = talkMsg;
        messageEvent.resId = resId;
        EventBus.getDefault().post(messageEvent);
    }

    public static void sendResIdMessage(int resId) {
        sendSpeechingMessage(null, null, null,resId);
    }

    public static void sendMainMessage(CharSequence mainMsg) {
        sendSpeechingMessage(mainMsg, null, null,0);
    }

    public static void sendDeputyMessage(CharSequence deputy) {
        sendSpeechingMessage(null, deputy, null,0);
    }

    public static void sendTalkMessage(String talkMsg) {
        sendSpeechingMessage(null, null, talkMsg,0);
    }

    public static void sendEndSpeech(){
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.eventType = MessageEvent.EventType.ENDSPEECH;
        EventBus.getDefault().post(messageEvent);
    }

    public static void sendRestartSpeechTimeOut(){
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.eventType = MessageEvent.EventType.RESTARTSPEECH;
        EventBus.getDefault().post(messageEvent);
    }

    public static void sendExitMessage() {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.EXIT;
        EventBus.getDefault().post(artEvent);
    }

    /**
     * @param animType 0:wakeupAnim
     *                 1:normalAnim
     *                 2:listenAnim
     *                 3: recogAnim
     * @param animType animationListener
     */
    public static void sendAnimMessage(int animType, AnimationImageView.OnFrameAnimationListener animationListener) {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.ANIM;
        artEvent.animType = animType;
        artEvent.animationListener = animationListener;
        EventBus.getDefault().post(artEvent);
    }

    /**
     * @param leftExitShow 左下角退出图标显示或隐藏
     */
    public static void sendLeftImageMessage(boolean leftExitShow) {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.LEFT_IMG;
        artEvent.leftExitShow = leftExitShow;
        EventBus.getDefault().post(artEvent);
    }

    /**
     * @param rightImageType 右下角图片类型 0: 显示设置图片  1：显示语音设置二级界面后退图标
     */
    public static void sendRightImageMessage(int rightImageType) {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.RIGHT_IMG;
        artEvent.rightImageType = rightImageType;
        EventBus.getDefault().post(artEvent);
    }

    /**
     *  白天模式
     */
    public static void sendDayModeMessage() {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.DAY_MODE;
        EventBus.getDefault().post(artEvent);
    }

    /**
     *  黑夜模式
     */
    public static void sendNightModeMessage() {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.NIGHT_MODE;
        EventBus.getDefault().post(artEvent);
    }

    public static void sendSRResult(int id) {
        SREvent event = new SREvent();
        event.resultID = id;
        EventBus.getDefault().post(event);
    }

    /**
     * @param whichIcon 显示那个按键
     */
    public static void sendIvFloatSmallViewVisible(String whichIcon) {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.GONE;
        artEvent.whichIcon = whichIcon;
        EventBus.getDefault().post(artEvent);
    }

    /**
     * @param ssbuilder 图片文字内容
     */
    public static void sendIconText(SpannableStringBuilder ssbuilder) {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.ICONTEXT;
        artEvent.iconText = ssbuilder;
        EventBus.getDefault().post(artEvent);
    }

    /**
     * @param speed 设置速度
     */
    public static void sendSpeed(String speed) {
        ArtEvent artEvent = new ArtEvent();
        artEvent.eventType = ArtEvent.EventType.SPEED;
        artEvent.speed = speed;
        EventBus.getDefault().post(artEvent);
    }
}
