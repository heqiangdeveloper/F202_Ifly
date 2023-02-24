package com.chinatsp.ifly;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.car.CarNotConnectedException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.db.CommandDbDao;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.db.entity.GuideBook;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.EventTrackingEntity;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.service.DetectionService;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.HandleUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.view.CruiseAnimaController;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.ChairController;
import com.chinatsp.ifly.voice.platformadapter.controller.ChangbaController;
import com.chinatsp.ifly.voice.platformadapter.controller.CruiseController;
import com.chinatsp.ifly.voice.platformadapter.controller.DrivingCareController;
import com.chinatsp.ifly.voice.platformadapter.controller.FeedBackController;
import com.chinatsp.ifly.voice.platformadapter.controller.MapController;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.controller.VehicleControl;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.GuideBookUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.ifly.voiceadapter.Business;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.seopt.SeoptManager;
import com.iflytek.speech.ISSErrors;
import com.iflytek.speech.libissseopt;
import com.iflytek.sr.SrSession;
import com.txznet.tts.TXZTTSInitManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
public class FloatViewManager {
    private final String TAG = "xyj_FloatViewManager";

    //1:通过语音唤醒  2：通过方向盘或触摸唤醒  3：其它唤醒方式
    public static final int WARE_BY_VOICE = 1;
    public static final int WARE_BY_KEY = 2;
    public static final int WARE_BY_OTHER = 3;
    public static final int WARE_BY_GUIDE = 4;
    public static final int WARE_BY_CHANGBA = 5;
    public static final int WARE_BY_REMOTE = 6;

    public static final int TYPE_HIDE_IFLY = 0;//语音自己
    public static final int TYPE_HIDE_PHONE = 1;//电话
    public static final int TYPE_HIDE_OTHER = 2;//第三方

    private static FloatViewManager floatViewManager;
    private Context mContext;
    private boolean isHide = true;
    public boolean isChangeMode = false;
    private WindowManager winManager;
    private WindowManager.LayoutParams params;

    private int displayWidth;
    private int displayHeight;

    private FloatSmallView floatSmallView;
    private SRAgent srAgent;
    private List<GuideBook> guideBookList;
    private CharSequence defaultGuideTip;
    private int ANIM_DURATION_TIME = 0; //floatview动画持续时间+UI显示耗时
    private Handler handler = new Handler(){};
    private ISpeechControlService mSpeechControlService;

    public static final String ACTION_INCOMING_CALL_FOR_EXTRA = "com.chinatsp.phone.click.calling";
    public static final String ACTION_CLICK_CALLING_FOR_EXTRA = "com.chinatsp.phone.permission";

    private FloatViewManager(Context context) {
        this.mContext = context.getApplicationContext();
        winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        srAgent = SRAgent.getInstance();
        //width:1920 height:720
        displayWidth = winManager.getDefaultDisplay().getWidth();
        displayHeight = winManager.getDefaultDisplay().getHeight();

        //getTryGuideMessageList();

        registerBroadcaster();
    }

    private void getTryGuideMessageList() {
        //获取引导提示语
        GuideBookUtils.getInstance(mContext).getGuideTipMessageList(new GuideBookUtils.OnCallback() {

            @Override
            public void onSuccess(List<GuideBook> guideBooks) {
                LogUtils.d(TAG, "GuideBookUtils onSuccess:" + guideBooks.size());
                guideBookList = guideBooks;
            }

            @Override
            public void onFail() {
                LogUtils.d(TAG, "GuideBookUtils onFail");
                String[] guideTips = mContext.getResources().getStringArray(R.array.auto_guide_tips);
                int i = new Random().nextInt(guideTips.length);
                String guideCommand = String.format("试着说 “ %s ”", guideTips[i]);
                defaultGuideTip = SpannableUtils.formatString(guideCommand, 4, guideCommand.length(), Color.parseColor("#00a1ff"));
            }
        });
    }

    public static  FloatViewManager getInstance(Context context) {
        if (floatViewManager == null) {
            synchronized (FloatViewManager.class){
                if(floatViewManager==null)
                    floatViewManager = new FloatViewManager(context);
            }
        }
        return floatViewManager;
    }

    /**
     *
     * @param way 1:通过语音唤醒  2：通过方向盘或触摸唤醒  3：其它唤醒方式
     */
    public void show(final int way) {

        if (!isHide) {
            LogUtils.d(TAG, "isHide == false");
            return;
        }

        Log.d(TAG, "show() called with: way = [" + way + "]");

        //播放之前，清除切源播放逻辑
        HandleUtils.getInstance().removeCallbacksAndMessages(null);

        hideHicar();

        hideSysmtemUI();

        EventBus.getDefault().register(this);


        floatSmallView = getFloatSmallView();
        if (floatSmallView != null) {
            if (floatSmallView.getParent() == null) {
                LogUtils.d(TAG, "addView");
                winManager.addView(floatSmallView, params);

                //异步工作
                asyncShowWorks(way);
            }
        }
        srAgent.stopSRRecord();
        srAgent.stopSRSession();
        isHide = false;
        Log.d(TAG, "show: isHide::"+isHide);
        sendBroadcastForHideSoftBoard();

        FeedBackController.getInstance(mContext).setInvalideType();  //放在最后，作为恢复场景使用
        ChairController.getInstance(mContext).setInvalideType();
        DrivingCareController.getInstance(mContext).setInvalideType();
    }

    private String mGuideTts;
    public void show(final int way,String tts) {
        Log.d(TAG, "show() called with: way = [" + way + "], tts = [" + tts + "]");
        if (!isHide) {
            LogUtils.d(TAG, "isHide == false");
            return;
        }

        mGuideTts = tts;

        hideSysmtemUI();

        EventBus.getDefault().register(this);

        floatSmallView = getFloatSmallView();
        if (floatSmallView != null) {
            if (floatSmallView.getParent() == null) {
                LogUtils.d(TAG, "addView");
                winManager.addView(floatSmallView, params);

                //异步工作
                asyncShowWorks(way);
            }
        }
        srAgent.stopSRRecord();
        srAgent.stopSRSession();
        isHide = false;
        Log.d(TAG, "show: isHide::"+isHide);
        sendBroadcastForHideSoftBoard();

        FeedBackController.getInstance(mContext).setInvalideType();  //放在最后，作为恢复场景使用
        ChairController.getInstance(mContext).setInvalideType();

//        ManageFloatWindow.getInstance(mContext).removeView();
    }

    private void sendBroadcastForHideSoftBoard() {
        try {
            /*
            adb shell am broadcast -a com.sinovoice.hcicloudinputvehicle --es action "hide"
             */
            Intent intent = new Intent("com.sinovoice.hcicloudinputvehicle");
            intent.putExtra("action", "hide");
            mContext.sendBroadcast(intent);
        } catch (Exception e) {}
    }

    private void asyncShowWorks(int way) {
        ThreadPoolUtils.executeSingle(new Runnable() {
            @Override
            public void run() {

                //保存音频焦点包名
                saveAudioFocusPkg();

                requestAudioFocus();  //先申请音频焦点，防止第一个声音太大

                //播放唤醒动画
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //播报并开启识别
                        if(way==FloatViewManager.WARE_BY_REMOTE){//语音远程启动，播报日常问候
                            startWelcomeTTS(way);
                        }else if(way!=FloatViewManager.WARE_BY_GUIDE)
                            startGreetingTTS(way);
                        else{
                            CharSequence guideTip = SpannableUtils.formatString(mGuideTts, 4, mGuideTts.length(), Color.parseColor("#00a1ff"));
                            EventBusUtils.sendDeputyMessage(guideTip);

                        }

                        EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_WAKEUP, listenerForWakeup);
                    }
                }, ANIM_DURATION_TIME);

               //停止播放，由于增加了语音引导，界面刚起来的时候，可能就收到了播放完成的回调，导致语音又消失
//                TTSController.getInstance(mContext).stopTTS();//1013 暂时屏蔽掉，避免时序问题，先调用stop导致语音发呆

                //TODO 后申请
//                requestAudioFocus();  //申请音频焦点

                //发送状态栏隐藏广播
                setStatusBarVisible(false);
                //发送给美行更新桌面动效
                sendAwareStateToMX(true);
                //发送给悬浮球隐藏
//                EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.HIDE,null,null));
                //超时机制
                SRAgent.getInstance().resetSrTimeCount();
                TimeoutManager.saveSrState(mContext, TimeoutManager.ORIGINAL, "");

                //获取当前状态并上传
                uploadAppStatus();

                //解除静音  暂时屏蔽掉，防止出现语音播报无声的问题
     /*           if (AudioFocusUtils.getInstance(mContext).isMasterMute()) {
                    try {
//                        TXZTTSInitManager.getInstance().setBackBufferSize(AppConstant.TXZ_COMPELTED_150); //这个方法可以设置后端静音时间，单位ms
                        AppConfig.INSTANCE.mCarAudioManager.setMasterMute(false, 0);
                    } catch (CarNotConnectedException e) {
                        e.printStackTrace();
                    }
                    AppConstant.setMute =true;
                } else {
                    AppConstant.setMute =false;
                }*/

            }
        });
    }

    private void uploadAppStatus() {
        String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        String topActivity = ActivityManagerUtils.getInstance(mContext).getTopActivity();
        String activeAudioPkg = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
        if(MXSdkManager.getInstance(mContext).isForeground()  &&
                (MXSdkManager.getInstance(mContext).isNaving()||MXSdkManager.getInstance(mContext).isViewPickShow()||MXSdkManager.getInstance(mContext).isPageFuFragmentShow())) {//地图在前台
                MultiInterfaceUtils.getInstance(mContext).uploadAppStatusData(true, PlatformConstant.Service.MAP_U, "default");
        }  else {
            MultiInterfaceUtils.getInstance(mContext).uploadAppStatusNaviBackground();
        }

        if(AppConstant.PACKAGE_NAME_MUSIC.equals(topPackage) || AppConstant.PACKAGE_NAME_MUSIC.equals(activeAudioPkg)) {//音乐在前台或在播放
//            boolean isPlaying = AppConstant.PACKAGE_NAME_MUSIC.equals(activeAudioPkg);
            boolean isPlaying = SRAgent.mMusicPlaying;
            MultiInterfaceUtils.getInstance(mContext).uploadMediaStatusData(true, PlatformConstant.Service.MUSIC,  isPlaying, null, null);
        }

        if(AppConstant.PACKAGE_NAME_RADIO.equals(topPackage) || AppConstant.PACKAGE_NAME_RADIO.equals(activeAudioPkg)) {//电台在前台或在播放
//            boolean isPlaying = AppConstant.PACKAGE_NAME_RADIO.equals(activeAudioPkg);
            boolean isPlaying = SRAgent.mInRadioPlaying;
            MultiInterfaceUtils.getInstance(mContext).uploadMediaStatusData(true, PlatformConstant.Service.INTERNETRADIO, isPlaying, null, null);
        }

        //央视音影在前台播放
        if(AppConstant.PACKAGE_NAME_VCAR.equals(topPackage)){
            boolean isPlaying = SRAgent.mVideoPlaying;
            MultiInterfaceUtils.getInstance(mContext).uploadMediaStatusData(true, PlatformConstant.Service.VIDEO, isPlaying, null, null);
        }
        //本地视频在前台播放
        if(AppConstant.PACKAGE_NAME_MUSIC.equals(topPackage)&& DetectionService.ACTIVITY_VIDEO.equals(topActivity)){
            boolean isPlaying = SRAgent.mLocalvideoPlaying;
            MultiInterfaceUtils.getInstance(mContext).uploadMediaStatusData(true, PlatformConstant.Service.VIDEO, isPlaying, null, null);
        }
    }

    public void hide(){
        hide(TYPE_HIDE_IFLY);
    }

    /**
     * 是不是语音自己消失
     * 第三方调用为false
     * 此时音频焦点延迟释放，否则在切换时会短暂播放上一个音频的声音
     */
    public void hide(int type) {
        Log.d(TAG, "hide() called"+Log.getStackTraceString(new Throwable()));
        if (isHide) {
            LogUtils.d(TAG, "isHide == true");
            return;
        }

        mGuideTts = ""; //将所有制令指令教学的引导语清空

        // 界面消失的时候，设置播报状态为false
        FeedBackController.getInstance(mContext).setTtsPlayStatus(false);
        DrivingCareController.getInstance(mContext).setTtsPlayStatus(false);

        EventBusUtils.sendSRResult(ISSErrors.ISS_SUCCESS);  //取消开启识别失败的消息发送

        EventBus.getDefault().unregister(this);

        //暂时屏蔽掉，防止出现语音播报无声的问题
        //恢复静音状态  有3 个地方做了容错措施，
        //1 在收到按键通知时，先静音，再延迟 50ms，调用 hide方法
        //2 电台 音乐，先静音，在调用 onstop 方法通知音乐 电台 播报结束，再hide
        // 3 本地
        boolean needRestoreMute = AppConstant.setMute;
        if(needRestoreMute) {
            try {
//                TXZTTSInitManager.getInstance().setBackBufferSize(AppConstant.TXZ_COMPELTED_0); //这个方法可以设置后端静音时间，单位ms
                if (!AudioFocusUtils.getInstance(mContext).isMasterMute())
                    AppConfig.INSTANCE.mCarAudioManager.setMasterMute(true, 0);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }

        //停止TTS播报 放在子线程，防止阻塞
//        TTSController.getInstance(mContext).stopTTS();
//
//        AudioFocusUtils.getInstance(mContext).releaseVoiceAudioFocus();

       /* ThreadPoolUtils.schheduleRunnale(new Runnable() {
            @Override
            public void run() {

            }
        },50);*/

        AppManager.getAppManager().finishListActivity();// Activity消失的慢一些，先调用
        if (floatSmallView != null) {
            //关闭唤醒动画
            floatSmallView.stopAnim();

            winManager.removeView(floatSmallView);
            params = null;
            floatSmallView = null;

            asyncHideWorks(type);
        }
        TimeoutManager.clearSrState(mContext);
        isHide = true;

        FeedBackController.getInstance(mContext).setInvalideType();  //放在最后，作为恢复场景使用
        ChairController.getInstance(mContext).setInvalideType();
        ChangbaController.getInstance(mContext).setInvalideType();
        VehicleControl.getInstance(mContext).setInvalideType();
        DrivingCareController.getInstance(mContext).setInvalideType();
        Utils.resetCurrentPriority(mContext);
//        ManageFloatWindow.getInstance(mContext).showFloatView();
    }

    /**
     *
     * @param type
     * true 语音自己调用，不用延迟
     * false 第三方调用，延迟处理
     */
    private void asyncHideWorks(int type) {

        ThreadPoolUtils.executeSingle(new Runnable() {
            @Override
            public void run() {
                saveExitTime();

                //停止TTS播报
                TTSController.getInstance(mContext).stopTTS();
                Log.d(TAG, "asyncHideWorks() called:type:::"+type);
                if(type==TYPE_HIDE_IFLY||type==TYPE_HIDE_OTHER)
                   AudioFocusUtils.getInstance(mContext).releaseVoiceAudioFocus();
                else if(type==TYPE_HIDE_PHONE){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            AudioFocusUtils.getInstance(mContext).releaseVoiceAudioFocus();
                        }
                    },1800);
                } else
                    AudioFocusUtils.getInstance(mContext).releaseVoiceAudioFocus();

              /*  //暂时屏蔽掉，防止出现语音播报无声的问题
                //恢复静音状态
                boolean needRestoreMute = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_NEED_RESTORE_MUTE, false);
                if(needRestoreMute) {
                    try {
                        AppConfig.INSTANCE.mCarAudioManager.setMasterMute(true, 0);
                    } catch (CarNotConnectedException e) {
                        e.printStackTrace();
                    }
                }*/

                //清除二次交互语义
                if (MultiInterfaceUtils.getInstance(mContext) != null) {
                    MultiInterfaceUtils.getInstance(mContext).clearMultiInterfaceSemantic();
                }
                if(srAgent!=null)
                    srAgent.resetSession();
                //关闭SR识别
                srAgent.stopSRSession();
                //记录的上一次识别场景为可见即可说，进行恢复
                if (srAgent.mSrArgu_Old != null && SrSession.ISS_SR_SCENE_STKS.equals(srAgent.mSrArgu_Old.scene)) {
                    srAgent.mSrArgu_New = new SrSessionArgu(srAgent.mSrArgu_Old);
                    srAgent.mSrArgu_Old = null;
                    srAgent.startSRSession();
                }

                //发送状态栏显示广播
                setStatusBarVisible(true);
                //发送给美行更新桌面动效
                sendAwareStateToMX(false);
                //发送给悬浮球显示
//                EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.SHOW,null,null));
                //上传默认状态
                MultiInterfaceUtils.getInstance(mContext).uploadCmdDefaultData();
                SeoptManager.getInstance().exitSession();

            }
        });
    }

    public void startChangbaGreetingTTS(){
        startGreetingTTS(FloatViewManager.WARE_BY_CHANGBA);
    }

    private void startGreetingTTS(int way) {
        String greeting = "";
        String conditionId = "";
        String userName = "";
        String replaceGreeting = "";
        boolean answerSwitch = SharedPreferencesUtils.getBoolean(mContext,AppConstant.KEY_SWITCH_ANSWER,true);
        String presetAnswer = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_CURRENT_ANSWER, AppConstant.DEFAULT_VALUE_CURRENT_ANSWER);
        EventTrackingEntity trackingEntity = null;
        if (AppConstant.DEFAULT_VALUE_CURRENT_ANSWER.equals(presetAnswer)) {
            if (way == WARE_BY_VOICE||way == WARE_BY_KEY) { //通过语音唤醒或者通过方控键或触摸唤醒
                if (ActiveServiceModel.getInstance().isTodayBirthday&&!AppConstant.isfirst) {
                    //首次点火判断是否是生日
                    AppConstant.isfirst = true;
                    conditionId = TtsConstant.MAINC1_3CONDITION;
                    greeting = mContext.getResources().getString(R.string.happy_birthday);
                    Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC1_3CONDITION, R.string.condition_mainC1_3,greeting);
                } else {
                    if (isTodayFirstAware()) {
                        String[] greetings = mContext.getResources().getStringArray(R.array.first_aware_greetings);
                        userName = mContext.getString(R.string.default_username);
                        String firstGreeting = mContext.getString(R.string.main_C1);
                        Calendar cal = Calendar.getInstance();
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        int minute = cal.get(Calendar.MINUTE);
                        LogUtils.d(TAG, "hour:" + hour + ",minute:" + minute);
                        conditionId = TtsConstant.MAINC1CONDITION;

//                        trackingEntity = Utils.initEventTracking(R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC1CONDITION, R.string.condition_mainC1);

                        if (hour >= 5 && hour < 9) {//早上05：00-08：59
                            replaceGreeting = greetings[0];
                            greeting = String.format(firstGreeting, userName, greetings[0]);
                        } else if (hour >= 9 && hour * 60 + minute < 11 * 60 + 30) { //上午 09：00-11：29
                            replaceGreeting = greetings[1];
                            greeting = String.format(firstGreeting, userName, greetings[1]);
                        } else if (hour * 60 + minute > 11 * 60 + 30 && hour < 14) {//中午11：30-13：59
                            replaceGreeting = greetings[2];
                            greeting = String.format(firstGreeting, userName, greetings[2]);
                        } else if (hour >= 14 && hour < 18) {//下午14：00-17：59
                            replaceGreeting = greetings[3];
                            greeting = String.format(firstGreeting, userName, greetings[3]);
                        } else if ((hour >= 18 && hour < 24) || (hour >= 0 && hour < 5)) { //晚上18：00-04：59
                            replaceGreeting = greetings[4];
                            greeting = String.format(firstGreeting, userName, greetings[4]);
                        } else {
                            replaceGreeting = greetings[5];
                            greeting = String.format(firstGreeting, userName, greetings[5]);
                        }
                    } else {
                        if (way == WARE_BY_VOICE) {
                            String[] greetings = mContext.getResources().getStringArray(R.array.aware_greetings);
                            conditionId = TtsConstant.MAINC2CONDITION;
                            int i = new Random().nextInt(greetings.length);
                            greeting = greetings[i];
                        } else if (way == WARE_BY_KEY) {
                            trackingEntity = Utils.initEventTracking(R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC3CONDITION, R.string.condition_mainC3);
                            greeting = mContext.getString(R.string.aware_greeting_by_manual);
                            conditionId = TtsConstant.MAINC3CONDITION;
                        }
                    }
                }
            } else if(way == WARE_BY_CHANGBA){
                greeting = "。";
            } else {
                //不做处理
                LogUtils.d(TAG, "Undefine case");
            }
            if("。".equals(greeting)){
                Utils.startTTSNoVoice(greeting,answerSwitch, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        //EventBusUtils.sendDeputyMessage(MessageEvent.ACTION_HIDE);
                    }
                });
            }else if (!TextUtils.isEmpty(conditionId) || !TextUtils.isEmpty(greeting)) {
                getTtsMessage(conditionId, greeting, userName, replaceGreeting,answerSwitch);
            }
        } else {
            //TTS播报
            if(way == FloatViewManager.WARE_BY_CHANGBA){
                LogUtils.d(TAG, "Undefine case111");
                Utils.startTTSNoVoice("。",answerSwitch, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        //EventBusUtils.sendDeputyMessage(MessageEvent.ACTION_HIDE);
                    }
                });
            }else if(way == FloatViewManager.WARE_BY_OTHER){
                //不做处理
                LogUtils.d(TAG, "Undefine case22");
            }else {//方控 语音
                Utils.startTTSNoVoice(presetAnswer,answerSwitch, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC1_2CONDITION, R.string.condition_mainC1_2,presetAnswer);
                    }
                });
            }
            //显示试着说引导词
            showTryGuideText();
        }

        //记录当前识别模式
        if (!srAgent.mSrArgu_New.scene.equals(SrSession.ISS_SR_SCENE_ALL)) {
            srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
        }
        //开启SR识别
        srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");

        if(trackingEntity != null) {
            DatastatManager.getInstance().eventTracking(mContext, trackingEntity);
        }
    }

    private void startWelcomeTTS(int way) {
        String greeting = "";
        String  conditionId = TtsConstant.MSGC40CONDITION;
        int conditon = R.string.condition_msgc40;
        String replaceGreeting = "";

        String[] dayTts = mContext.getResources().getStringArray(R.array.msgC40);
        String[] morningTts = mContext.getResources().getStringArray(R.array.msgC41);
        String[] nightTts = mContext.getResources().getStringArray(R.array.msgC42);

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);



        if (hour >= 5 && hour < 9) {//早上05：00-08：59
            conditionId = TtsConstant.MSGC41CONDITION;
            greeting = morningTts[new Random().nextInt(morningTts.length)];
            conditon = R.string.condition_msgc41;
        } else if (hour >= 9 && hour * 60 + minute < 11 * 60 + 30) { //上午 09：00-11：29
            conditionId = TtsConstant.MSGC40CONDITION;
            conditon = R.string.condition_msgc40;
            greeting = dayTts[new Random().nextInt(dayTts.length)];
            replaceGreeting = "上午";
        } else if (hour * 60 + minute >= 11 * 60 + 30 && hour < 14) {//中午11：30-13：59
            conditionId = TtsConstant.MSGC40CONDITION;
            conditon = R.string.condition_msgc40;
            greeting = dayTts[new Random().nextInt(dayTts.length)];
            replaceGreeting = "中午";
        } else if (hour >= 14 && hour < 18) {//下午14：00-17：59
            conditionId = TtsConstant.MSGC40CONDITION;
            conditon = R.string.condition_msgc40;
            greeting = dayTts[new Random().nextInt(dayTts.length)];
            replaceGreeting = "下午";
        } /*else if ((hour >= 18 && hour < 24) || (hour >= 0 && hour < 5)) { //晚上18：00-04：59
            conditionId = TtsConstant.MSGC42CONDITION;
            greeting = String.format(firstGreeting, userName, greetings[4]);
        } */else {
            conditionId = TtsConstant.MSGC42CONDITION;
            conditon = R.string.condition_msgc42;
            greeting = nightTts[new Random().nextInt(nightTts.length)];
        }

        //记录当前识别模式
        if (!srAgent.mSrArgu_New.scene.equals(SrSession.ISS_SR_SCENE_ALL)) {
            srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
        }
        //开启SR识别
        srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");

        getTtsMessage(conditionId,conditon,greeting,replaceGreeting);

    }

    private void getTtsMessage(final String conditionId, int conditon,final String defaultTts,final String replaceGreeting) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                //tts为空,则用默认tts代替,避免tts不播报
                if (TextUtils.isEmpty(ttsText)){
                    ttsText = defaultTts;
                }

                ttsText = Utils.replaceTts(ttsText, "#NOW#", replaceGreeting);

                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
                Utils.eventTrack(mContext, R.string.skill_active, R.string.scene_active_welcome, R.string.object_active_welcome, conditionId, conditon,ttsText);
                //显示试着说引导词
                showTryGuideText();
            }
        });
    }


    private void showTryGuideText() {
        //显示底部消息
        String defaultTts = mContext.getString(R.string.default_try_guide);
        String guideTts = TtsUtils.getInstance(mContext).getTryGuideTts();
//        if(guideTts!=null)
//            EventBusUtils.sendDeputyMessage(String.format("试着说 “ %s ”", guideTts));
//        else { //从所有指令取出引导语
        //解决bugID1049858,不用判空，直接随机播报
            CommandDbDao.getInstance(mContext).queryTryGuideTts(new CommandDbDao.TryGuideTtsInterface() {
                @Override
                public void onTryGuideTtsFound(CommandInfo info) {
                    String text = "";
                    CharSequence  tts;
                    if(info==null||info.getInstructContent()==null
                            ||"".equals(info.getInstructContent())||info.getInstructContent().trim().equals("null")){
                        text=String.format("试着说 “ %s ”", defaultTts);
                        tts = SpannableUtils.formatString(text, 4, text.length(), Color.parseColor("#00a1ff"));
                    }/*else if("0".equals(info.getModuleType())){
                        text=String.format("免唤醒直接说 “ %s ”", info.getInstructContent());
                        tts = SpannableUtils.formatString(text, 7, text.length(), Color.parseColor("#00a1ff"));
                    }*/else{
                        text=String.format("试着说 “ %s ”", info.getInstructContent());
                        tts = SpannableUtils.formatString(text, 4, text.length(), Color.parseColor("#00a1ff"));

                    }
                    EventBusUtils.sendDeputyMessage(tts);
                }
            });
        }

 /*       GuideBookUtils.getInstance(mContext).getGuideTipMessageList(new GuideBookUtils.OnCallback() {

            @Override
            public void onSuccess(List<GuideBook> guideBooks) {
                guideBookList = guideBooks;

                //显示底部消息
                if (!BaseLifecycleCallback.getInstance().isAppOnForeground()) {
                    Log.d(TAG, "guideBookList.size() = " + guideBookList.size());
                    //氛围灯 配置 0 ：未配置，  1：配置
                    int hasLamp = Utils.getInt(mContext, CarController.AMBIENT_TYPE, 0);
                    Log.d(TAG, "hasLamp = " + hasLamp);
                    String guideCommand = "";
                    if (guideBookList.size() > 1) {
                        GuideBook guideBook = guideBookList.get(new Random().nextInt(guideBookList.size()));
                        Log.d(TAG, "before guideBook.command = " + guideBook.command + ",guideBook.scene = " + guideBook.scene);
                        if (hasLamp == 0) {//没有氛围灯
                            if (guideBook.command.contains(CarController.LAMP)) {
                                List<GuideBook> noLampGuideList = new ArrayList<GuideBook>();
                                for (GuideBook mGuideBook : guideBookList) {
                                    if (!mGuideBook.command.contains(CarController.LAMP)) {
                                        noLampGuideList.add(mGuideBook);
                                    }
                                }
                                Log.d(TAG,"noLampGuideList.size() = " + noLampGuideList.size());
                                if(noLampGuideList.size() != 0){
                                    guideBook = noLampGuideList.get(new Random().nextInt(noLampGuideList.size()));
                                }
                            }

                            if(!guideBook.command.contains(CarController.LAMP)){
                                guideCommand = String.format("试着说 “ %s ”", guideBook.command);
                                GuideBookUtils.getInstance(mContext).updateUsageCount(guideBook);
                            }else{
                                String[] guideTips = mContext.getResources().getStringArray(R.array.auto_guide_tips);
                                int i = new Random().nextInt(guideTips.length);
                                guideCommand = String.format("试着说 “ %s ”", guideTips[i]);
                            }
                        }else {//有氛围灯
                            guideCommand = String.format("试着说 “ %s ”", guideBook.command);
                            GuideBookUtils.getInstance(mContext).updateUsageCount(guideBook);
                        }

                        CharSequence guideTip = SpannableUtils.formatString(guideCommand, 4, guideCommand.length(), Color.parseColor("#00a1ff"));
                        EventBusUtils.sendDeputyMessage(guideTip);
                    }else{
                        String[] guideTips = mContext.getResources().getStringArray(R.array.auto_guide_tips);
                        int i = new Random().nextInt(guideTips.length);
                        guideCommand = String.format("试着说 “ %s ”", guideTips[i]);
                        defaultGuideTip = SpannableUtils.formatString(guideCommand, 4, guideCommand.length(), Color.parseColor("#00a1ff"));
                        EventBusUtils.sendDeputyMessage(defaultGuideTip);
                    }
                }
            }

            @Override
            public void onFail() {
                String[] guideTips = mContext.getResources().getStringArray(R.array.auto_guide_tips);
                int i = new Random().nextInt(guideTips.length);
                String guideCommand = String.format("试着说 “ %s ”", guideTips[i]);
                defaultGuideTip = SpannableUtils.formatString(guideCommand, 4, guideCommand.length(), Color.parseColor("#00a1ff"));
                EventBusUtils.sendDeputyMessage(defaultGuideTip);
            }
        });*/

//            Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC2CONDITION, R.string.condition_mainC2);

//    }

    private void getTtsMessage(final String conditionId, final String defaultTts,final String username,final String replaceGreeting,boolean answerSwitch) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                //tts为空,则用默认tts代替,避免tts不播报
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                } else {
                    if (TtsConstant.MAINC1CONDITION.equals(conditionId)) {
                        Log.d(TAG, "lh:tts:" + conditionId + ",text:" + ttsText);
                        ttsText = Utils.replaceTts(tts, "#NAME#", username);
                        ttsText = Utils.replaceTts(ttsText, "#NOW#", replaceGreeting);
                    }
                }
               /* //延迟播报，防止在静音情况下播报的时候第一个字被吞音   1012 先申请焦点就不再需要延迟
                String intts = ttsText;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utils.startTTSNoVoice(intts,answerSwitch,null);
                    }
                },100);*/

                //TTS播报
                Utils.startTTSNoVoice(ttsText,answerSwitch, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        //EventBusUtils.sendDeputyMessage(MessageEvent.ACTION_HIDE);
                    }
                });

                if(TtsConstant.MAINC1CONDITION.equals(conditionId))
                    Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC1CONDITION, R.string.condition_mainC1,ttsText);
                else if(TtsConstant.MAINC2CONDITION.equals(conditionId))
                    Utils.eventTrack(mContext, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware, TtsConstant.MAINC2CONDITION, R.string.condition_mainC2,ttsText);



                    //显示试着说引导词
                showTryGuideText();
            }
        });
    }

    //唤醒动画回调
    private AnimationImageView.OnFrameAnimationListener listenerForWakeup = new AnimationImageView.OnFrameAnimationListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onEnd() {
            //打招呼动画
            EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_GREETING, listenerForGreeting);
        }
    };

    //打招呼动画回调
    private AnimationImageView.OnFrameAnimationListener listenerForGreeting = new AnimationImageView.OnFrameAnimationListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onEnd() {
            boolean answerSwitch = SharedPreferencesUtils.getBoolean(mContext,AppConstant.KEY_SWITCH_ANSWER,true);
            //语音唤醒应答语为关时，不播报语音，此时需要开启识别动画
            if(!isHide&&!answerSwitch){
                SRAgent.getInstance().startSRSession();
                EventBusUtils.sendTalkMessage(MessageEvent.ACTION_ANIM);
                EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_LISTENING, null);
            }

            //进入常态动画
            //EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_NORMAL, null);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateFloatView(MessageEvent messageEvent) {
        if (floatSmallView == null) {
            LogUtils.w(TAG, "floatSmallView == null");
            return;
        }

        switch (messageEvent.eventType) {
            case SPEECHING:
                //设置语音交互中界面

                floatSmallView.setSpeechView(messageEvent);
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateFloatView(ArtEvent artEvent) {
        if (floatSmallView == null) {
            LogUtils.w(TAG, "floatSmallView == null");
            return;
        }
        switch (artEvent.eventType) {
            case ANIM:
              /*  if (AppConfig.INSTANCE.settingFlag) {
                    if (artEvent.animType == ArtEvent.AnimType.ANIM_NORMAL) {
                        floatSmallView.stopAnim();
                        floatSmallView.setAnim(ArtEvent.AnimType.ANIM_NORMAL, artEvent.animationListener);
                    }
                    return;
                }*/
                floatSmallView.setAnim(artEvent.animType, artEvent.animationListener);
                break;
            case EXIT:
                hide();
                break;
            case LEFT_IMG:
                floatSmallView.setLeftImageVisible(artEvent.leftExitShow);
                break;
            case RIGHT_IMG:
                floatSmallView.setRightImageSource(artEvent.rightImageType);
                break;
            case DAY_MODE:
                floatSmallView.updateDayNightMode(true);
                break;
            case NIGHT_MODE:
                floatSmallView.updateDayNightMode(false);
                break;
            case GONE:
                floatSmallView.setIvFloatSmallViewAnimVisible(artEvent.whichIcon);
                break;
            case ICONTEXT:
                floatSmallView.setIconText(artEvent.iconText);
                break;
            case SPEED:
                floatSmallView.setSpeed(artEvent.speed);
                break;

        }
    }

    public boolean isHide() {
        return isHide;
    }

    private FloatSmallView getFloatSmallView() {
        if (floatSmallView == null) {
            floatSmallView = new FloatSmallView(mContext);
        }
        if (params == null) {
            params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
            params.format = PixelFormat.RGBA_8888;
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.width = floatSmallView.getFloatViewWidth();
            params.height = floatSmallView.getFloatViewHeight();
            params.x = displayWidth - params.width / 5;
            params.y = 0;
        } else {
            params.width = floatSmallView.getFloatViewWidth();
            params.height = floatSmallView.getFloatViewHeight();
        }
        //添加动画
        params.windowAnimations = R.style.FloatViewAnim;
        return floatSmallView;
    }

    /**
     * 判断是否是当日第一次唤醒
     */
    private boolean isTodayFirstAware() {
        String lastTime = SharedPreferencesUtils.getString(mContext, "LastLoginTime", "2018-03-18");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String todayTime = df.format(new Date());// 获取当前的日期

        if (lastTime.equals(todayTime)) { //如果两个时间段相等
            LogUtils.d("FloatViewManager", "today no first aware");
            return false;
        } else {
            LogUtils.d("FloatViewManager", "today first aware");
            return true;
        }
    }

    /**
     * 保存每次退出的时间
     */
    private void saveExitTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        String exitLoginTime = df.format(new Date());// 获取当前的日期
        SharedPreferencesUtils.saveString(mContext, "LastLoginTime", exitLoginTime);
    }

    class FloatSmallView extends LinearLayout {

        private int mWidth;
        private int mHeight;
        private EditText tvTalk1;
        private EditText tvTalk2;
        private ImageView ivCruiseControl;
        private TextView tvMain;
        private TextView tvDeputy;
        private ImageView ivExit;
        private ImageView ivSettings;
        private LinearLayout llVoice;
        private RelativeLayout rlRoot;
        private AnimationImageView ivFloatSmallViewAnim;
        private List<SpannableString> spanList1;
        private List<SpannableString> spanList2;
        private Handler handler = new Handler();
        private int rightType;
        private ImageView headMainIv;
        private ImageView headSecondIv;
        private Runnable loadingAnimRunnable1;
        private Runnable loadingAnimRunnable2;
        private LinearLayout llAnim;
        private ImageView ivBtnSet;
        private ImageView ivBtnRes;
        private ImageView ivBtSwitch;
        private ImageView ivBtSet_Res;
        private FrameLayout fl_cruise_control;
        private ObjectAnimator objectAnimator;
        private TextView tvSpeed;
        private Runnable setTalk1InvisibleRunnable = new Runnable() {
            @Override
            public void run() {
                tvTalk1.setText("");
                tvTalk1.setVisibility(View.GONE);
            }
        };
        private Runnable setTalk2InvisibleRunnable = new Runnable() {
            @Override
            public void run() {
                tvTalk2.setVisibility(View.GONE);
            }
        };


        public FloatSmallView(final Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.layout_floatview_small, this);

            llAnim = view.findViewById(R.id.ll_anim);
            ivBtnSet = view.findViewById(R.id.iv_btn_set);
            ivBtnRes = view.findViewById(R.id.iv_btn_res);
            ivBtSwitch = view.findViewById(R.id.iv_btn_switch);
            ivBtSet_Res = view.findViewById(R.id.iv_btn_res_set);
            fl_cruise_control = view.findViewById(R.id.fl_cruise_control);
            ivCruiseControl = view.findViewById(R.id.iv_cruise_control);
            tvMain = view.findViewById(R.id.tv_main_message);
            tvDeputy = view.findViewById(R.id.tv_deputy_message);
            tvTalk1 = view.findViewById(R.id.tv_talk1_message);
            tvTalk2 = view.findViewById(R.id.tv_talk2_message);
            ivExit = view.findViewById(R.id.iv_close);
            ivSettings = view.findViewById(R.id.iv_setting);
            ivFloatSmallViewAnim = view.findViewById(R.id.iv_anim);
            headMainIv = view.findViewById(R.id.head_main_iv);
            headSecondIv = view.findViewById(R.id.head_second_iv);
            llVoice = view.findViewById(R.id.ll_voice);
            rlRoot = view.findViewById(R.id.rl_root);
            tvSpeed = view.findViewById(R.id.tv_speed);

            setLeftImageVisible(true);
            setRightImageSource(0);
            ivExit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();

                    //清除二次交互语义
//                    if (MultiInterfaceUtils.getInstance(mContext) != null) {
//                        MultiInterfaceUtils.getInstance(mContext).clearMultiInterfaceSemantic();
//                    }
                    if (PlatformHelp.getInstance().getPlatformClient() != null) {
                        PlatformHelp.getInstance().getPlatformClient().onRestoreMultiSemantic();
                    }
                }
            });

            spanList1 = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                int resId = Utils.getId(mContext, "loading_blue_dots" + String.format("%01d", i));
                ImageSpan imgSpan = new ImageSpan(mContext, resId);
                SpannableString spannableString = new SpannableString(" ");
                spannableString.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanList1.add(spannableString);
            }

            spanList2 = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                int resId = Utils.getId(mContext, "loading_red_dots" + String.format("%01d", i));
                ImageSpan imgSpan = new ImageSpan(mContext, resId);
                SpannableString spannableString = new SpannableString(" ");
                spannableString.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spanList2.add(spannableString);
            }

            loadingAnimRunnable1 = new LoadingRunnable(tvTalk1, spanList1);
            loadingAnimRunnable2 = new LoadingRunnable(tvTalk2, spanList2);

            mWidth = 500;
            mHeight = displayHeight;
        }

        public int getFloatViewWidth() {
            return mWidth;
        }

        public int getFloatViewHeight() {
            return mHeight;
        }

        /**
         * 语音交互中
         */
        public void setSpeechView(MessageEvent messageEvent) {
            Log.d(TAG, "setSpeechView() called with: seopt_direction = [" + SeoptManager.getInstance().seopt_direction+ "]");
            if (AppConfig.INSTANCE.settingFlag) {
                tvTalk1.setVisibility(View.GONE);
                tvTalk2.setVisibility(View.GONE);
                tvDeputy.setText("");
                handler.removeCallbacksAndMessages(null);
                if (!TextUtils.isEmpty(messageEvent.mainMessage)) {
                    String mainStr = messageEvent.mainMessage.toString();
                    if (!MessageEvent.ACTION_HIDE.equals(mainStr) && !MessageEvent.ACTION_GREY.equals(mainStr)) {
                        tvMain.setText(messageEvent.mainMessage);
                    }
                }
                return;
            }
            String talkStr = messageEvent.talkMessage;

            if (SeoptConstant.USE_SEOPT && SeoptManager.getInstance().seopt_direction.
                    equals(libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT)) {  // 使用窄波束
                // 副驾
                headMainIv.setImageResource(R.drawable.ic_voice_unselected);
                headSecondIv.setImageResource(R.drawable.ic_right_voice);

                if (!TextUtils.isEmpty(talkStr)) {
                    ivFloatSmallViewAnim.setVisibility(VISIBLE);
                    fl_cruise_control.setVisibility(GONE);
                    if (MessageEvent.ACTION_ANIM.equals(talkStr)) {
                        tvTalk2.setVisibility(View.VISIBLE);
                        tvTalk2.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.talk_push_up_in));
                        tvTalk2.setText("");
                        tvTalk2.setBackgroundResource(R.drawable.bg_drive_small_copilot);
                        handler.post(loadingAnimRunnable2);
                        handler.removeCallbacks(setTalk2InvisibleRunnable);

                    } else if (MessageEvent.ACTION_HIDE.equals(talkStr)) {
                        handler.removeCallbacksAndMessages(null);
                        tvTalk2.setVisibility(View.GONE);
                        tvTalk2.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.talk_push_up_out));
                    } else {
                        handler.removeCallbacksAndMessages(null);
                        tvTalk2.setVisibility(View.VISIBLE);
                        tvTalk2.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.talk_push_up_in));
                        tvTalk2.setText(talkStr);
                        tvTalk2.setSelection(tvTalk2.getText().length());
                        if (talkStr.length() < 6) {
                            tvTalk2.setBackgroundResource(R.drawable.bg_drive_small_copilot);
                        } else {
                            tvTalk2.setBackgroundResource(R.drawable.bg_drive_copilot);
                        }
                        tvTalk2.setPadding(30, 0, 30, 0);
                        handler.postDelayed(setTalk2InvisibleRunnable, 2000);
                    }
                }

            } else {
                //主驾
                headMainIv.setImageResource(R.drawable.ic_left_voice);
                headSecondIv.setImageResource(R.drawable.ic_voice_unselected);

                Log.d(TAG, "setSpeechView: talkStr:::"+talkStr);

                if (!TextUtils.isEmpty(talkStr)) {
                    ivFloatSmallViewAnim.setVisibility(VISIBLE);
                    fl_cruise_control.setVisibility(GONE);
                    if (MessageEvent.ACTION_ANIM.equals(talkStr)) {
                        tvTalk1.setVisibility(View.VISIBLE);
                        tvTalk1.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.talk_push_up_in));
                        tvTalk1.setText("");
                        tvTalk1.setBackgroundResource(R.drawable.bg_drive_grey);
                        handler.post(loadingAnimRunnable1);
                        handler.removeCallbacks(setTalk1InvisibleRunnable);

                    } else if (MessageEvent.ACTION_HIDE.equals(talkStr)) {
                        handler.removeCallbacksAndMessages(null);
                        tvTalk1.setVisibility(View.GONE);
                        tvTalk1.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.talk_push_up_out));
                    } else {
                       /* handler.removeCallbacksAndMessages(null);
                        tvTalk1.setVisibility(View.VISIBLE);
                        tvTalk1.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.talk_push_up_in));
                        tvTalk1.setText(talkStr);
                        if (talkStr.length() < 6) {
                            tvTalk1.setBackgroundResource(R.drawable.bg_drive_grey);
                            tvTalk1.setPadding(30, 30, 30, 40);
                        } else {
                            tvTalk1.setBackgroundResource(R.drawable.bg_drive_grey);
                            tvTalk1.setPadding(30, 30, 30, 40);
                        }

                        handler.postDelayed(setTalk1InvisibleRunnable, 2000);*/

                        if(tvTalk1.getVisibility()!=View.VISIBLE){
                            tvTalk1.setVisibility(View.VISIBLE);
                            tvTalk1.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.talk_push_up_in));
                        }
                        talkStr = talkStr.replace("。","");//不显示句号
                        if(!talkStr.equals(tvTalk1.getText().toString())){
                            tvTalk1.setText(talkStr);
                            tvDeputy.setVisibility(GONE);
                            tvTalk1.setSelection(tvTalk1.getText().length());
                            if (talkStr.length() < 6) {
                                tvTalk1.setBackgroundResource(R.drawable.bg_drive_grey);
                                tvTalk1.setPadding(20, 0, 20, 20);
                            } else {
                                tvTalk1.setBackgroundResource(R.drawable.bg_drive_grey);
                                tvTalk1.setPadding(20, 0, 20, 20);
                            }
                            handler.removeCallbacksAndMessages(null);
                            handler.postDelayed(setTalk1InvisibleRunnable, 2000);
                        }
                    }
                }
            }

            if(messageEvent.resId != 0 && (fl_cruise_control.getVisibility() != VISIBLE ||
                    ivCruiseControl.getBackground() != mContext.getDrawable(messageEvent.resId))){
                fl_cruise_control.setVisibility(VISIBLE);
                ivCruiseControl.setImageResource(messageEvent.resId);
            }

            //主消息
            Runnable setMainInvisibleRunnale = new Runnable() {
                @Override
                public void run() {
                    if(tvMain.getTag() != null) {
                        tvMain.setText("");
                    }
                }
            };

            Log.d(TAG, "setSpeechView:mainMessage::: "+messageEvent.mainMessage);

            if (!TextUtils.isEmpty(messageEvent.mainMessage)) {
                if("。".equals(messageEvent.mainMessage))return;
                String mianMsg = messageEvent.mainMessage.toString();
                mianMsg = replaceMultWords(mianMsg);
                mianMsg = replacePhoneNum(mianMsg);
                if (MessageEvent.ACTION_HIDE.equals(mianMsg)) {
                    tvMain.setTag(null);
                    tvMain.setText("");
                    handler.removeCallbacks(setMainInvisibleRunnale);
                } else if (MessageEvent.ACTION_GREY.equals(mianMsg)) {
                    tvMain.setTextColor(mContext.getColor(R.color.action_grey));
                    tvMain.setTag(mContext.getColor(R.color.action_grey));
                    handler.postDelayed(setMainInvisibleRunnale, 5000);
                } else {
                    tvMain.setTag(null);
                    tvMain.setTextColor(mContext.getColor(R.color.white));
                    tvMain.setText(mianMsg);
                    handler.removeCallbacks(setMainInvisibleRunnale);
                }
            }

            //底部消息
            if (!TextUtils.isEmpty(messageEvent.deputyMessage)) {
                Log.d(TAG, "setSpeechView: "+messageEvent.deputyMessage);
                if (MessageEvent.ACTION_HIDE.equals(messageEvent.deputyMessage.toString())) {// "-1"做为特殊字符
                    tvDeputy.setText("");
                } else {
                    tvDeputy.setText(messageEvent.deputyMessage);
                    tvDeputy.setVisibility(VISIBLE);
                }
            }

        }

        public void setIvFloatSmallViewAnimVisible(String whichIcon){
            tvTalk1.setVisibility(GONE);
            tvTalk2.setVisibility(GONE);
            //恢复为原来的状态
            fl_cruise_control.setVisibility(VISIBLE);
            fl_cruise_control.setScaleX((float) 1.0);
            fl_cruise_control.setScaleY((float) 1.0);
            fl_cruise_control.setAlpha((float) 1.0);
            fl_cruise_control.setTranslationY(0);
            ivFloatSmallViewAnim.setVisibility(VISIBLE);
            tvSpeed.setVisibility(View.GONE);
            if(!TextUtils.isEmpty(whichIcon)){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivFloatSmallViewAnim.setVisibility(GONE);
                    }
                },300);
                if(CruiseController.SET.equals(whichIcon)){
                    CruiseAnimaController.getInstance().starPlayAnima(fl_cruise_control,ivBtnSet);
                }else if(CruiseController.RES.equals(whichIcon)){
                    CruiseAnimaController.getInstance().starPlayAnima(fl_cruise_control,ivBtnRes);
                }else if(CruiseController.SWITCH.equals(whichIcon)){
                    CruiseAnimaController.getInstance().starPlayAnima(fl_cruise_control,ivBtSwitch);
                }else if(CruiseController.RESSET.equals(whichIcon)){
                    CruiseAnimaController.getInstance().starPlayAnima(fl_cruise_control,ivBtSet_Res);
                }
            }else {
                ivFloatSmallViewAnim.setVisibility(VISIBLE);
            }
        }

        public void setIconText(SpannableStringBuilder builder){
            if(null != builder) tvMain.setText(builder);
        }

        public void setSpeed(String speed){
            //重新设置ivCruiseControl宽高,将表盘缩小
            ViewGroup.LayoutParams params = ivCruiseControl.getLayoutParams();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            ivCruiseControl.setLayoutParams(params);
            ivCruiseControl.setPadding(0,20,0,20);

            if(!TextUtils.isEmpty(speed)) tvSpeed.setText(speed);
            tvSpeed.setVisibility(VISIBLE);
        }

        public void setLeftImageVisible(boolean visible) {
            ivExit.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }

        public void setRightImageSource(int type) {
            rightType = type;
//            if (type == 0) {
                ivSettings.setImageResource(R.drawable.ic_setting);
                ivSettings.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SettingsActivity.class);
                        intent.putExtra("jumpName",AppConstant.VOICE_SETTINGS_ID);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                });
//            } else {
//                int mode = 0;
//                try {
//                    mode = Settings.System.getInt(mContext.getContentResolver(), AppConstant.SHOW_MODE);
//                } catch (Settings.SettingNotFoundException e) {
//                    e.printStackTrace();
//                }
//                ivSettings.setImageResource(mode == 0 ? R.drawable.ic_right_back : R.drawable.ic_right_back_day);
//                ivSettings.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        AppManager.getAppManager().finishActivity();
////                        mContext.sendBroadcast(new Intent(AppConstant.ACTION_SHOW_ASSISTANT));
//                        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
//                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
//                    }
//                });
//            }
        }

        public void setAnim(int animType, AnimationImageView.OnFrameAnimationListener animationListener) {
            ivFloatSmallViewAnim.stopAnimation();
            switch (animType) {
                case ArtEvent.AnimType.ANIM_WAKEUP:
                    if (AppConfig.INSTANCE.wakeupAnim != null) {
                        ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.wakeupAnim, animationListener);
                    }
                    break;
                case ArtEvent.AnimType.ANIM_NORMAL:
                    if (AppConfig.INSTANCE.normalAnim != null) {
                        ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.normalAnim, animationListener);
                    }
                    break;
                case ArtEvent.AnimType.ANIM_LISTENING:
                    if (AppConfig.INSTANCE.listenAnim != null) {
                        ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.listenAnim, animationListener);
                    }
                    break;
                case ArtEvent.AnimType.ANIM_RECOGNIZING_1:
                    if (AppConfig.INSTANCE.recogAnim_1 != null) {
                        ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.recogAnim_1, animationListener);
                    }
                    break;
                case ArtEvent.AnimType.ANIM_RECOGNIZING_2:
                    if (AppConfig.INSTANCE.recogAnim_2 != null) {
                        ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.recogAnim_2, animationListener);
                    }
                    break;
                case ArtEvent.AnimType.ANIM_RECOGNIZING_3:
                    if (AppConfig.INSTANCE.recogAnim_3 != null) {
                        ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.recogAnim_3, animationListener);
                    }
                    break;
                case ArtEvent.AnimType.ANIM_GREETING:
                    if (SeoptConstant.USE_SEOPT) {
                        if (SeoptManager.getInstance().seopt_direction.equals(libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT)) {
                            if (AppConfig.INSTANCE.greetingAnimSlave != null) {//副麦
                                ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.greetingAnimSlave, animationListener);
                                break;
                            }
                        }
                    }
                    if (AppConfig.INSTANCE.greetingAnimMaster != null) {
                        ivFloatSmallViewAnim.loadAnimation(AppConfig.INSTANCE.greetingAnimMaster, animationListener);
                    }

                    break;
            }
        }

        private void stopAnim() {
            ivFloatSmallViewAnim.stopAnimation();
        }

        public void updateDayNightMode(boolean dayMode) {
            Log.d("xyj000", "updateDayNightMode:" + dayMode);
            isChangeMode = true;
            rlRoot.setBackgroundResource(dayMode ? R.color.floatview_day_bg : R.color.floatview_night_bg);
            tvMain.setTextColor(mContext.getColor(dayMode ? R.color.floatview_day_white : R.color.floatview_night_white));
            ivExit.setImageResource(dayMode ? R.drawable.ic_close_day : R.drawable.ic_close_night);
            llVoice.setBackgroundResource(dayMode ? R.drawable.bg_voice_day : R.drawable.bg_voice_night);
            if(rightType == 0) {
                ivSettings.setImageResource(dayMode ? R.drawable.ic_setting_day : R.drawable.ic_setting_night);
            } else {
                ivSettings.setImageResource(dayMode ? R.drawable.ic_right_back_day : R.drawable.ic_right_back);
            }
        }

        public class LoadingRunnable implements Runnable {
            int i = 0;
            private TextView textView;
            private List<SpannableString> spanList;

            public LoadingRunnable(TextView textView, List<SpannableString> spanList) {
                this.spanList = spanList;
                this.textView = textView;
            }

            @Override
            public void run() {
                textView.setPadding(40,0,40,20);
                textView.setText(spanList.get(i));
                i++;
                if (i > 2) {
                    i = 0;
                }
                handler.postDelayed(this, 500);
            }
        }
    }

    private void setStatusBarVisible(boolean visible) {
        LogUtils.d(TAG, "setStatusBarVisible:" + visible);
        Intent intent = new Intent();
        intent.putExtra("packageName", mContext.getPackageName());
        if (visible) {
            intent.setAction(AppConstant.ACTION_SHOW_CUSTOM_STATUS_BAR);
        } else {
            intent.setAction(AppConstant.ACTION_HIDE_CUSTOM_STATUS_BAR);
        }
        intent.setPackage(AppConstant.PACKAGE_NAME_SYSTEMUI);
        mContext.sendBroadcast(intent);
    }

    public void sendAwareStateToMX(boolean visible) {
        LogUtils.d(TAG, "sendAwareStateToMX:" + visible);
        Intent intent = new Intent();
        if (visible) {
            intent.setAction(AppConstant.ACTION_VOICE_AWARED);
        } else {
            intent.setAction(AppConstant.ACTION_VOICE_DISAPPEAR);
        }
        intent.setPackage(AppConstant.PACKAGE_NAME_WECARNAVI);
        mContext.sendBroadcast(intent);
    }

    /**
     * 替换多音字
     * @param words
     * @return
     */
    private String replaceMultWords(String words){
        if(words!=null&&words.contains("<cnphone py=tiao2>")){
          /*  int first = words.indexOf("<");
            int last = words.indexOf(">");
            String replaceWords = words.substring(first,last+1);*/
            words = words.replace("<cnphone py=tiao2>","");
        }

        return words;
    }


    /**
     * 号码不显示标签
     * @param words
     * @return
     */
    private String replacePhoneNum(String words) {
        if(words!=null&&words.contains("<")&&words.contains(">")){

            int firstL = words.indexOf("<");
            int firstR = words.indexOf(">");
            String  first = words.substring(firstL,firstR+1);
            words = words.replace(first,"");

            if(words.contains("<")&&words.contains(">")){
                firstL = words.indexOf("<");
                firstR = words.indexOf(">");
                first = words.substring(firstL,firstR+1);
                words = words.replace(first,"");
            }

            return words;
        }
        return words;
    }
    /**
     * 申请音频焦点
     */
    private void requestAudioFocus(){
       /* String activeAudioPkg = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
        if (activeAudioPkg != null) {
            SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_AUDIO_FOCUS_PKGNAME, activeAudioPkg);
        }*/
        if(!mContext.getPackageName().equals(AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg())) {
            AudioFocusUtils.getInstance(mContext).requestVoiceAudioFocus(AudioManager.STREAM_ALARM);
        }
    }

    /**
     * 耗时操作，拆开来做
     */
    private void saveAudioFocusPkg(){
        String activeAudioPkg = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
        if (activeAudioPkg != null) {
            SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_AUDIO_FOCUS_PKGNAME, activeAudioPkg);
        }
    }

    private void hideSysmtemUI(){
        Intent intent = new Intent();
        intent.setAction(ACTION_INCOMING_CALL_FOR_EXTRA);
        mContext.sendBroadcast(intent,ACTION_CLICK_CALLING_FOR_EXTRA);
    }

// ====================================   增加空调语言提示  =====================================
    private void registerBroadcaster() {
        //驾驶模式引导模拟
        IntentFilter mFilter = new IntentFilter("com.chinatsp.welcome.visitors.warn");//测试用
        IntentFilter mFilterFocus = new IntentFilter( "android.media.SOURCE_CHANGED_ACTION");//监听导航焦点变化
        mContext.registerReceiver(mBroadcaster, mFilter);
        mContext.registerReceiver(mBroadcaster, mFilterFocus);
//        try {
//            IntentFilter filter = new IntentFilter("com.chinatsp.AUTOSTART");
//            mContext.registerReceiver(mBroadcaster, filter);
//
//            LogUtils.d(TAG, "air_tts_message_registerBroadcaster");
//        } catch (Exception e) {
//        }
    }

    private BroadcastReceiver mBroadcaster = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.chinatsp.welcome.visitors.warn")){
               //ttsMessage(); 通过按键引导播报
                if(intent.getIntExtra("id",1) == 1){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC1CONDITION);
                }else if(intent.getIntExtra("id",1) == 111){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC1_1CONDITION);
                }else if(intent.getIntExtra("id",1) == 2){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC2CONDITION);
                }else if(intent.getIntExtra("id",1) == 31){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC3_1CONDITION);
                }else if(intent.getIntExtra("id",1) == 3){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC3CONDITION);
                }else if(intent.getIntExtra("id",1) == 4){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC4CONDITION);
                }else if(intent.getIntExtra("id",1) == 41){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC4_1CONDITION);
                }else if(intent.getIntExtra("id",1) == 5){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC5CONDITION);
                }else if(intent.getIntExtra("id",1) == 51){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC5_1CONDITION);
                }else if(intent.getIntExtra("id",1) == 52){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC5_2CONDITION);
                }else if(intent.getIntExtra("id",1) == 6){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC6CONDITION);
                }else if(intent.getIntExtra("id",1) == 61){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC6_1CONDITION);
                }else if(intent.getIntExtra("id",1) == 7){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC7CONDITION);
                }else if(intent.getIntExtra("id",1) == 71){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC7_1CONDITION);
                }else if(intent.getIntExtra("id",1) == 8){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC8CONDITION);
                }else if(intent.getIntExtra("id",1) == 9){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC9CONDITION);
                }else if(intent.getIntExtra("id",1) == 10){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC10CONDITION);
                }else if(intent.getIntExtra("id",1) == 11){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC11CONDITION);
                }else if(intent.getIntExtra("id",1) == 12){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC12CONDITION);
                }else if(intent.getIntExtra("id",1) == 13){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC13CONDITION);
                }else if(intent.getIntExtra("id",1) == 14){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC14CONDITION);
                }else if(intent.getIntExtra("id",1) == 15){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC15CONDITION);
                }else if(intent.getIntExtra("id",1) == 16){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC16CONDITION);
                }else if(intent.getIntExtra("id",1) == 17){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC17CONDITION);
                }else if(intent.getIntExtra("id",1) == 18){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC18CONDITION);
                }else if(intent.getIntExtra("id",1) == 19){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC19CONDITION);
                }else if(intent.getIntExtra("id",1) == 20){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC20CONDITION);
                }else if(intent.getIntExtra("id",1) == 21){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC21CONDITION);
                }else if(intent.getIntExtra("id",1) == 22){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC22CONDITION);
                }else if(intent.getIntExtra("id",1) == 23){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC23CONDITION);
                }else if(intent.getIntExtra("id",1) == 24){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC24CONDITION);
                }else if(intent.getIntExtra("id",1) == 25){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC25CONDITION);
                }else if(intent.getIntExtra("id",1) == 26){
                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC26CONDITION);
                }
            }else {
                //DrivingModeGuideController.getInstance(mContext).dispatchCommand(intent.getIntExtra("type",2));
//                ActiveServiceModel.getInstance().handleWelcomeVisitors(mContext,intent.getIntExtra("signal",1),
//                        intent.getIntExtra("lock",1),intent.getIntExtra("unLock",2));
                //ChairController.getInstance(mContext).handleWarnSaveSleepMode();
                //CarController.getInstance(mContext).handWindowWarn(TtsConstant.GUIDEBTNC42CONDITION,CarController.WINDOW_RIGHT_BACK.replace("车窗",""));
//                if(intent.getIntExtra("id",1) == 1){
//                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC1CONDITION);
//                }else if(intent.getIntExtra("id",1) == 31){
//                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC3_1CONDITION);
//                }else if(intent.getIntExtra("id",1) == 2){
//                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC2CONDITION);
//                }else if(intent.getIntExtra("id",1) == 4){
//                    CruiseController.getInstance(mContext).srAction(TtsConstant.CCC4CONDITION);
//                }

                //KeyGuideController.getInstance(mContext).handleCarCabinAction(false,TtsConstant.GUIDEBTNC37CONDITION, R.string.btnC37,"＃MODE＃","自定义模式");
                String callingPackage = intent.getStringExtra("android.media.EXTRA_PACKAGE_NAME_TO");
                String fromPackage = intent.getStringExtra("android.media.EXTRA_PACKAGE_NAME_FROM");
                Log.d("heqiangq", "getCurrentActiveAudioPkg = " + AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg());
                Log.d("heqiangq", "callingPackage = " + callingPackage + ",fromPackage = " + fromPackage);
                if (AppConstant.PACKAGE_NAME_WECARNAVI.equals(callingPackage)) {
                    Log.d("heqiangq", "导航播报中。。。");
                    SharedPreferencesUtils.saveBoolean(mContext,AppConstant.KEY_NAVI_BROADCAST,true);
                }else {
                    Log.d("heqiangq", "导航未播报。。。");
                    SharedPreferencesUtils.saveBoolean(mContext,AppConstant.KEY_NAVI_BROADCAST,false);
                }
                if(AppConstant.PACKAGE_NAME_IFLY.equals(callingPackage) && AppConstant.PACKAGE_NAME_WECARNAVI.equals(fromPackage)){
                    SharedPreferencesUtils.saveBoolean(mContext,AppConstant.KEY_NAVI_BEFORE_VOICE,true);
                }else {
                    SharedPreferencesUtils.saveBoolean(mContext,AppConstant.KEY_NAVI_BEFORE_VOICE,false);
                }
            }
        }
    };

    private void ttsMessage() {

        LogUtils.d(TAG, "air_tts_message_ttsMessage");
        String defaultTts = mContext.getString(R.string.air_default_msgC92);
        String conditionId =  TtsConstant.MSGC92CONDITION;


        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultTts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                    EventBusUtils.sendExitMessage();
                }
            }
        });

        Utils.eventTrack(mContext,R.string.air_warm, R.string.air_warm, R.string.condition_air_msgC92_logic,
                conditionId,R.string.condition_air_msgC92);
    }

    private void showGuideTts(String text){

    }

    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }

    private void hideHicar() {
        String topActivity = ActivityManagerUtils.getInstance(mContext).getTopActivity();
        if(SRAgent.mHICARPlaying&&DetectionService.ACTIVITY_HICAR.equals(topActivity)){
            try {
                CmdVoiceModel cmdVoiceModel = new CmdVoiceModel();
                cmdVoiceModel.id = 1;
                cmdVoiceModel.text = "小欧小欧";
                cmdVoiceModel.response= DatastatManager.response;
                cmdVoiceModel.hide= isHide?0:1;
                mSpeechControlService.dispatchMvwAction(Business.HICAR, cmdVoiceModel);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
