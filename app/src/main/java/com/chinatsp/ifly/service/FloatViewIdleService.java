package com.chinatsp.ifly.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.car.media.CarAudioManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.chinatsp.ifly.AppManager;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.JumpEntity;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.IflyUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.MyToast;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.ChangbaController;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.phone.bean.CallContact;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.sr.SrSession;

import java.util.List;
import java.util.Random;

public class FloatViewIdleService extends Service {

    private static final String TAG = "FloatViewIdleService";
    private static final String BCALL_STATUS = "bcall_status";
    private static final int BCALL_STATUS_NO_BCALL = 0;
    private static final int BCALL_STATUS_CALLING = 1;
    private static Handler mHandler;
    private final static int REFRESH_FLOAT_VIEW = 10;
    private final static int SHOW_FLOAT_VIEW = 11;
    private final static int HIDE_FLOAT_VIEW = 12;
    private final static int SHOW_FLOAT_VIEW_BY_OTHER = 13;
    private final static int SHOW_FLOAT_VIEW_BY_GUIDE = 14;
    private final static int ACTIVE_SERVICE = 13;
    private final static int SEND_PAUSE_CHANGBA = 16;
    private final static int SHOW_FLOAT_VIEW_BY_CHANGBA = 17;
    private final static int SHOW_FLOAT_VIEW_BY_REMOTE = 18;
    private final static int SHOW_FLOAT_VIEW_BY_GUIDE_TEACH= 19;
    private SRAgent srAgent;
    private long lastReceiveTime = 0;
    private FloatViewManager floatViewManager;
    private  LocalBroadcastReceiver localReceiver;

    public static void start(Context context) {
        Intent intent = new Intent(context, FloatViewIdleService.class);
        context.startService(intent);
    }

    public static void start(Context context, String action) {
        Intent intent = new Intent(context, FloatViewIdleService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initHandler();
        localReceiver = new LocalBroadcastReceiver();
        registerAwareVoiceReceiver();
        registerAssistantReceive();
        srAgent = SRAgent.getInstance();
        floatViewManager = FloatViewManager.getInstance(this);
    }

    private void registerAwareVoiceReceiver() {
        IntentFilter filter = new IntentFilter(AppConstant.ACTION_AWARE_VOICE_TOUCH);
        filter.addAction(AppConstant.ACTION_AWARE_VOICE_KEY);
//        filter.addAction(AppConstant.ACTION_SHOW_ASSISTANT);
        filter.addAction(AppConstant.ACTION_TEST_NOVICEGUIDE);
        filter.addAction(AppConstant.ACTION_HIDE_VOICE);
        registerReceiver(mReceiver, filter);
    }

    private void registerAssistantReceive(){

        IntentFilter filter = new IntentFilter(AppConstant.ACTION_SHOW_ASSISTANT);
        LocalBroadcastManager localBroadcastManager= LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(localReceiver, filter);

    }

    public class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //?????????????????????????????????????????????????????????????????????????????????????????????
            Log.d(TAG, "onReceive() called with: context = [" + "], intent = [" + intent.getAction() + "]");
            String type = intent.getStringExtra(AppConstant.EXTRA_SHOW_TYPE);
            boolean isFirstUse = SharedPreferencesUtils.getBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
//            if (!isFirstUse) {

            int active= Settings.Global.getInt(context.getContentResolver(),"hicarphone_active",0);//1???hicar??????????????????0????????????
            if(active==1){
                Log.e(TAG, "onDoMvwAction: hicar call "+active);
                return ;
            }

                int btStatus  = Settings.Global.getInt(context.getContentResolver(),BCALL_STATUS, BCALL_STATUS_NO_BCALL);
                Log.e(TAG, "onReceive: btStatus::::::"+btStatus);
                if(btStatus!=BCALL_STATUS_NO_BCALL){
                    Log.e(TAG, "onReceive: ???????????????");
                    MyToast.showToast(context, "?????????????????????????????????", true);
                    return;
                }else if (BluePhoneManager.getInstance(context).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                    LogUtils.d(TAG, "Call is Active, ignore");
                    MyToast.showToast(context, "???????????????????????????????????????", true);
                } else if(CarUtils.getInstance(context).isReverse()){
                    LogUtils.d(TAG, "??????????????????, ignore");
                    MyToast.showToast(context, "????????????????????????????????????????????????", true);
                } else if(!AppConfig.INSTANCE.ttsEngineInited){
                    LogUtils.e(TAG, "????????????????????????, ignore");
                }/*else if ("1".equals(Utils.getProperty("evs_disable_touch", "0"))) {//??????????????????????????????
                    LogUtils.d(TAG, "Quanjing high-priority state, ignore");
                } */else if(AppConstant.SHOW_BY_OTHER.equals(type)){//????????????????????????
                    lastReceiveTime = System.currentTimeMillis();;
                    mHandler.removeMessages(SHOW_FLOAT_VIEW_BY_OTHER);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_FLOAT_VIEW_BY_OTHER), 0);
                } else if(AppConstant.SHOW_BY_GUIDE.equals(type)){//????????????????????????????????????
                    String tts =intent.getStringExtra(AppConstant.EXTRA_SHOW_TTS);
                    lastReceiveTime = System.currentTimeMillis();;
                    mHandler.removeMessages(SHOW_FLOAT_VIEW_BY_GUIDE);
                    Message msg = mHandler.obtainMessage();
                    msg.what = SHOW_FLOAT_VIEW_BY_GUIDE;
                    msg.obj = tts;
                    mHandler.sendMessage(msg);
                }else if(AppConstant.SHOW_BY_REMOTE.equals(type)){//??????????????????????????????
                    lastReceiveTime = System.currentTimeMillis();;
                    mHandler.removeMessages(SHOW_FLOAT_VIEW_BY_REMOTE);
                    Message msg = mHandler.obtainMessage();
                    msg.what = SHOW_FLOAT_VIEW_BY_REMOTE;
                    mHandler.sendMessage(msg);
                }else {
                    Utils.openScreen(); //????????????
//                    String topPackage = ActivityManagerUtils.getInstance(context).getTopPackage();
//                    if(AppConstant.PACKAGE_NAME_CHANGBA.equals(topPackage) &&
//                            (ChangbaController.getInstance(context).RECORDACTIVITY).equals(ChangbaController.getInstance(context).initActivity)){
//                        //???????????????????????????
//                        ChangbaController.getInstance(context).sendMonitorCommandToCB(0x10121,0);
//                        //????????????
////                        Message msg = new Message();
////                        msg.what = SEND_PAUSE_CHANGBA;
////                        msg.obj = context;
////                        mHandler.sendMessageDelayed(msg,150);
//                        //?????????????????????
//                        AppConfig.INSTANCE.setMicWorkMode(CarAudioManager.MIC_MODE_DENOISE);
//                        //??????????????????
//                        Message msg1 = new Message();
//                        msg1.what = SHOW_FLOAT_VIEW_BY_CHANGBA;
//                        msg1.obj = "???";
//                        mHandler.sendMessage(msg1);
//                    }else {
                        long now = System.currentTimeMillis();
                        if (Math.abs((now - lastReceiveTime))> 500) {
                            //?????????????????????????????????????????????????????? 1:?????????,0:?????????
                            String playStatus = Utils.getProperty("vendor.audio.changba", "0");
                            Log.d(TAG, "playStatus:" + playStatus);
                            SharedPreferencesUtils.saveString(context,ChangbaController.CBPLAYSTATUS,playStatus);

                            String wakeword = DatastatManager.primitive;
                            DatastatManager.getInstance().wakeup_event_new(getApplicationContext(), "????????????", wakeword,"???????????????");

                            lastReceiveTime = now;
                            mHandler.removeMessages(SHOW_FLOAT_VIEW);
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_FLOAT_VIEW), 0);
                        } else {
                            LogUtils.d(TAG, "ignore receive SHOW_FLOAT_VIEW:"+Math.abs((now - lastReceiveTime)));
                        }
                    //}
                }
//            }
        }
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, "receive action: " + intent.getAction());
            if(AppConstant.ACTION_TEST_NOVICEGUIDE.equals( intent.getAction())){
                SharedPreferencesUtils.saveInt(context, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY,1);
                SharedPreferencesUtils.saveBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,true);

                //Intent i = new Intent(context, NoviceGuideActivity.class);
                Intent i = new Intent(context, GuideMainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                context.startActivity(i);
            }


            if (AppConstant.ACTION_AWARE_VOICE_TOUCH.equals(intent.getAction())) {
                DatastatManager.getInstance().recordUI_event(context, getString(R.string.event_id_float_click), "");
                String jumpModule = intent.getStringExtra("jumpModule");
                Log.d(TAG,"jumpModule ="+jumpModule);

         /*       int active= Settings.Global.getInt(context.getContentResolver(),"hicarphone_active",0);//1???hicar??????????????????0????????????
                if(active==1){
                    Log.e(TAG, "onDoMvwAction: hicar call "+active);
                    return ;
                }

                int btStatus  = Settings.Global.getInt(context.getContentResolver(),BCALL_STATUS, BCALL_STATUS_NO_BCALL);
                if(btStatus!=BCALL_STATUS_NO_BCALL){
                    Log.e(TAG, "onReceive: ???????????????");
                    MyToast.showToast(context, "?????????????????????????????????", true);
                    return;
                }else if (BluePhoneManager.getInstance(context).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                    MyToast.showToast(context, "???????????????????????????????????????", true);
                    LogUtils.d(TAG, "Call is Active, ignore");
                    return;
                }
                if (!IflyUtils.iflytekIsInited(FloatViewIdleService.this)) {
                    LogUtils.d(TAG, "iflytek is not ready");
                    return;
                }

                if(!AppConfig.INSTANCE.ttsEngineInited){
                    LogUtils.e(TAG, "????????????????????????, ignore");
                    return;
                }*/

                long now = System.currentTimeMillis();
                if (Math.abs((now - lastReceiveTime)) > 500) {
                    if (jumpModule!=null){
                        analysisJumpContent(jumpModule);
                        Log.d(TAG,"pkg ="+pkg);
                        Log.d(TAG,"action ="+action);
                        try {
                            if(pkg.equals(IFLY_PACKAGE)){
                                if (mJumpEntity.action.equals(ACTION_OPEN_VIDEO)&&(VideoListUtil.getInstance(context).getVideoPathById(mJumpEntity.videoId)!=null)) {
                                    //????????????????????????????????????????????????
                                    VideoListUtil.getInstance(context).jumpToVideoActivity(context, mJumpEntity.videoId);
                                }else if (mJumpEntity.action.equals(ACTION_OPEN_GUIDE)){
//                                showAssistant(context);
                                    mHandler.removeMessages(SHOW_FLOAT_VIEW_BY_GUIDE_TEACH);
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = SHOW_FLOAT_VIEW_BY_GUIDE_TEACH;
                                    msg.obj = mJumpEntity.instructTeach;
                                    mHandler.removeMessages(SHOW_FLOAT_VIEW_BY_OTHER);
                                    mHandler.sendMessage(mHandler.obtainMessage(SHOW_FLOAT_VIEW_BY_OTHER));
                                    mHandler.sendMessageDelayed(msg,600);
                                }else {
                                    Intent commandintent = new Intent(context, SettingsActivity.class);
                                    commandintent.putExtra("jumpModule",mJumpEntity);
                                    commandintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(commandintent);
                                }
                            }else {
                                //??????????????????????????????
                                Utils.startApp(context,pkg,action);
                            }
                        }catch (Exception e){
                            Log.d(TAG,"jump error");
                        }
                        DatastatManager.getInstance().wakeup_event(getApplicationContext(), "????????????", "");
                        DatastatManager.getInstance().provider_event(getApplicationContext(),"???????????????");
                    } else {
                          /* SharedPreferencesUtils.saveInt(context, AppConstant.KEY_WHICH_NAME, 0);
                        Settings.System.putString(context.getContentResolver(),"aware", MvwKeywordsUtil.getCurrentName(context));
                        lastReceiveTime = now;
                        mHandler.removeMessages(REFRESH_FLOAT_VIEW);
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(REFRESH_FLOAT_VIEW), 0);*/
                        Intent commandintent = new Intent(context, SettingsActivity.class);
                        commandintent.putExtra("jumpModule",mJumpEntity);
                        commandintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(commandintent);
                        DatastatManager.getInstance().wakeup_event(getApplicationContext(), "????????????", "");
                        DatastatManager.getInstance().provider_event(getApplicationContext(),"???????????????");
                    }

                } else {
                    LogUtils.d(TAG, "ignore receive REFRESH_FLOAT_VIEW:"+Math.abs((now - lastReceiveTime)));
                }
            } else if (AppConstant.ACTION_AWARE_VOICE_KEY.equals(intent.getAction())) {
                //??????????????? ?????????????????????????????????????????????????????? ?????????????????????
                boolean isFirstUse = SharedPreferencesUtils.getBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
                Log.d(TAG, "onReceive: isFirstUse"+isFirstUse);
                //if (!isFirstUse) {
                    String keyCode = intent.getStringExtra("Key_code");
                    String keyState = intent.getStringExtra("Key_state");
                    LogUtils.d(TAG, "keyCode:" + keyCode + " ,keyState:" + keyState);
                    String topPackage = ActivityManagerUtils.getInstance(context).getTopPackage();
                    if ("VR".equals(keyCode)) {

                        int active= Settings.Global.getInt(context.getContentResolver(),"hicarphone_active",0);//1???hicar??????????????????0????????????
                        if(active==1){
                            Log.e(TAG, "onDoMvwAction: hicar call "+active);
                            return ;
                        }

                        int btStatus  = Settings.Global.getInt(context.getContentResolver(),BCALL_STATUS, BCALL_STATUS_NO_BCALL);
                        if(btStatus!=BCALL_STATUS_NO_BCALL){
                            Log.e(TAG, "onReceive: ???????????????");
                            MyToast.showToast(context, "?????????????????????????????????", true);
                            return;
                        }else  if (BluePhoneManager.getInstance(context).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                            LogUtils.d(TAG, "Call is Active, ignore");
                            MyToast.showToast(context, "???????????????????????????????????????", true);
                        } else if (AppConstant.PACKAGE_NAME_CHEXIN.equals(topPackage) && "LONG_UP".equals(keyState)) {
                            LogUtils.d(TAG, "Chexin is fg and VR LONG_UP, ignore");
                        } else if(CarUtils.getInstance(context).isReverse()){
                            LogUtils.d(TAG, "??????????????????, ignore");
                            MyToast.showToast(context, "????????????????????????????????????????????????", true);
                        } else if(!AppConfig.INSTANCE.ttsEngineInited){
                            LogUtils.e(TAG, "????????????????????????, ignore");
                        }/*else if ("1".equals(Utils.getProperty("evs_disable_touch", "0"))) {//??????????????????????????????
                            LogUtils.d(TAG, "Quanjing high-priority state, ignore");
                        } */else if ("UP".equals(keyState) || "LONG_UP".equals(keyState)) {
                            Utils.openScreen(); //????????????

                            if (!IflyUtils.iflytekIsInited(FloatViewIdleService.this)) {
                                LogUtils.d(TAG, "iflytek is not ready");
                                return;
                            }

                            long now = System.currentTimeMillis();
                            if (Math.abs((now - lastReceiveTime)) > 500) {

                                SharedPreferencesUtils.saveInt(context, AppConstant.KEY_WHICH_NAME, 0);
                                Settings.System.putString(context.getContentResolver(),"aware", MvwKeywordsUtil.getCurrentName(context));

                                lastReceiveTime = now;
                                mHandler.removeMessages(REFRESH_FLOAT_VIEW);
                                mHandler.sendMessageDelayed(mHandler.obtainMessage(REFRESH_FLOAT_VIEW), 0);
                                AppManager.getAppManager().finishListActivity();

                                DatastatManager.getInstance().wakeup_event_new(getApplicationContext(), "????????????", "","???????????????");
//                                DatastatManager.getInstance().wakeup_event(getApplicationContext(), "????????????", "");
//                                DatastatManager.getInstance().provider_event(getApplicationContext(),"???????????????");
                            } else {
                                LogUtils.d(TAG, "ignore receive REFRESH_FLOAT_VIEW:"+Math.abs((now - lastReceiveTime)));
                            }
                        }
                    } else if("SRC".equals(keyCode) && "UP".equals(keyState)) { //?????????????????????????????????
//                        mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE_FLOAT_VIEW), 0);
                        Utils.exitVoiceAssistant();
//                        SourceManager.getInstance(FloatViewIdleService.this).notifySrcChanged();

                    }
                //}
            } else if (AppConstant.ACTION_SHOW_ASSISTANT.equals(intent.getAction())) {
                //?????????????????????????????????????????????????????????????????????????????????????????????
                Log.e(TAG, "onReceive: the receive not user");
              /*  boolean isFirstUse = SharedPreferencesUtils.getBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, true);
                if (!isFirstUse) {
                    if (BluePhoneManager.getInstance(context).getCallStatus() != CallContact.CALL_STATE_TERMINATED) {
                        LogUtils.d(TAG, "Call is Active, ignore");
                    } else if ("1".equals(Utils.getProperty("evs_disable_touch", "0"))) {//??????????????????????????????
                        LogUtils.d(TAG, "Quanjing high-priority state, ignore");
                    } else {
                        Utils.openScreen(); //????????????
                        long now = System.currentTimeMillis();
                        if (now - lastReceiveTime > 1500) {
                            lastReceiveTime = now;
                            mHandler.removeMessages(SHOW_FLOAT_VIEW);
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(SHOW_FLOAT_VIEW), 0);
                        } else {
                            LogUtils.d(TAG, "ignore receive SHOW_FLOAT_VIEW:"+lastReceiveTime);
                        }
                    }
                }*/
            }else if (AppConstant.ACTION_HIDE_VOICE.equals(intent.getAction())) {
//                LogUtils.d(TAG, "?????????????????????????????????");
                //??????????????????????????????????????????????????????
                //TODO ???????????????????????????????????????
                if(AppConfig.INSTANCE.settingFlag){
                    return;
                }
                mHandler.sendMessageDelayed(mHandler.obtainMessage(HIDE_FLOAT_VIEW), 0);
            }
        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //????????????, ??????????????????????????????/?????????????????????/????????????/?????????????????????
            //??????????????????
//            if (ActiveServiceModel.getInstance().getAbnormalWeather(true)) {
//                mHandler.sendMessageDelayed(mHandler.obtainMessage(ACTIVE_SERVICE), 2000);
//            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("HandlerLeak")
    protected void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REFRESH_FLOAT_VIEW:
                        updateFloatView(REFRESH_FLOAT_VIEW);
                        break;
                    case SHOW_FLOAT_VIEW:
                        updateFloatView(SHOW_FLOAT_VIEW);
                        break;
                    case HIDE_FLOAT_VIEW:
                        updateFloatView(HIDE_FLOAT_VIEW);
                        break;
                    case SHOW_FLOAT_VIEW_BY_OTHER:
                        updateFloatView(SHOW_FLOAT_VIEW_BY_OTHER);
                        break;
                    case SHOW_FLOAT_VIEW_BY_GUIDE:
                        updateFloatView(SHOW_FLOAT_VIEW_BY_GUIDE, (String) msg.obj);
                        break;
                    case SEND_PAUSE_CHANGBA:
                        ChangbaController.getInstance((Context)msg.obj).sendOriginalCommandToCB("Pause",null,0,null,true);
                        break;
                    case SHOW_FLOAT_VIEW_BY_CHANGBA:
                        updateFloatView(SHOW_FLOAT_VIEW_BY_CHANGBA, (String) msg.obj);
                        break;
                    case SHOW_FLOAT_VIEW_BY_REMOTE:
                        updateFloatView(SHOW_FLOAT_VIEW_BY_REMOTE);
                        break;
                    case SHOW_FLOAT_VIEW_BY_GUIDE_TEACH:
                        String tts = (String) msg.obj;
                        Utils.startTTS(tts, null);
                        break;
                }
            }
        };
    }

    private void updateFloatView(int action,String tts){
        if(action == SHOW_FLOAT_VIEW_BY_GUIDE){
            if (floatViewManager.isHide()) {
                floatViewManager.show(FloatViewManager.WARE_BY_GUIDE,tts);
            }else{
                //??????????????????????????????????????????tts ??????????????????????????????????????????
                //????????????????????????????????????????????? onplaybegin???????????????????????????
            }
        }else if(action == SHOW_FLOAT_VIEW_BY_CHANGBA){
            if (floatViewManager.isHide()) {
                floatViewManager.show(FloatViewManager.WARE_BY_CHANGBA,tts);
            }else{
                FloatViewManager.getInstance(FloatViewIdleService.this).startChangbaGreetingTTS();
            }
        }
    }

    private void updateFloatView(int action) {
        if (action == REFRESH_FLOAT_VIEW) { //???????????????????????????
            if (floatViewManager.isHide()) {
//                SeoptManager.getInstance().setDirection(libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_LEFT);
                floatViewManager.show(FloatViewManager.WARE_BY_KEY);
                //?????????????????????name(??????????????????????????????????????????????????????)
                SharedPreferencesUtils.saveInt(this, AppConstant.KEY_WHICH_NAME, 0);
            } else {
                Utils.setMastMute(FloatViewIdleService.this);
                mHandler.sendEmptyMessageDelayed(HIDE_FLOAT_VIEW,50);  //??????????????????????????? ???????????????
//                Utils.exitVoiceAssistant();
            }
        } else if (action == SHOW_FLOAT_VIEW_BY_REMOTE) { //????????????????????????
            if (floatViewManager.isHide()) {
                floatViewManager.show(FloatViewManager.WARE_BY_REMOTE);
            } else {
               //?????????????????????????????????????????????????????????????????????
            }
        }else if (action == SHOW_FLOAT_VIEW) { //??????????????????
            if (floatViewManager.isHide()) {
                floatViewManager.show(FloatViewManager.WARE_BY_VOICE);
            } else {
                if (AppConfig.INSTANCE.settingFlag) {
                    LogUtils.d(TAG, "settingFlag=true, ignore");
                    return;
                }

                //????????????????????????
                if (!srAgent.mSrArgu_New.scene.equals(SrSession.ISS_SR_SCENE_ALL)) {
                    srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
                }
                //??????SR??????
                srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");

                TTSController.getInstance(this).stopTTS();
//                SRAgent.getInstance().stopSRSession();
                EventBusUtils.sendTalkMessage(MessageEvent.ACTION_HIDE);

                boolean answerSwitch = SharedPreferencesUtils.getBoolean(this,AppConstant.KEY_SWITCH_ANSWER,true);
//                if(answerSwitch)  //?????????????????????????????????
                    startGreetingTTS();

                int srState = TimeoutManager.getSrState(this);
                if (srState == TimeoutManager.ORIGINAL) { //???????????????????????????
                    //???????????????
                    EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_GREETING, listenerForGreeting);
                } else {
                    //????????????
                    EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_WAKEUP, listenerForWakeup);

                    //????????????
                    SRAgent.getInstance().resetSrTimeCount();
                    TimeoutManager.saveSrState(this, TimeoutManager.ORIGINAL, "");
                }
            }
        } else if(action == SHOW_FLOAT_VIEW_BY_OTHER){
            if (floatViewManager.isHide()) {
                floatViewManager.show(FloatViewManager.WARE_BY_OTHER);
            }else{
                //??????????????????????????????????????????tts ??????????????????????????????????????????
                //????????????????????????????????????????????? onplaybegin???????????????????????????
            }
        } else {
            floatViewManager.hide();
        }
    }

    //??????????????????
    private AnimationImageView.OnFrameAnimationListener listenerForWakeup = new AnimationImageView.OnFrameAnimationListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onEnd() {
            //???????????????
            EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_GREETING, listenerForGreeting);
        }
    };

    //?????????????????????
    private AnimationImageView.OnFrameAnimationListener listenerForGreeting = new AnimationImageView.OnFrameAnimationListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onEnd() {
            //??????????????????
            //EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_NORMAL, null);

        }
    };

    private void startGreetingTTS() {
        boolean answerSwitch = SharedPreferencesUtils.getBoolean(this,AppConstant.KEY_SWITCH_ANSWER,true);
        TTSController.OnTtsStoppedListener stopedListener = new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
            }
        };

        //???????????????
        String presetAnswer = SharedPreferencesUtils.getString(this, AppConstant.KEY_CURRENT_ANSWER, AppConstant.DEFAULT_VALUE_CURRENT_ANSWER);
        if (AppConstant.DEFAULT_VALUE_CURRENT_ANSWER.equals(presetAnswer)) {
            String[] greetings = getResources().getStringArray(R.array.second_greetings);
            int i = new Random().nextInt(greetings.length);
            String conditionId = TtsConstant.MAINC4CONDITION;
            TtsUtils.getInstance(this).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
                @Override
                public void onSuccess(List<TtsInfo> ttsInfoList) {
                    //?????????????????????,???????????????????????????,???????????????,???????????????;
                    TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                    Log.d(TAG,"lh:conditionId1:" + conditionId+"???callback:"+ttsInfo.getTtsText());
                    String tts = ttsInfo.getTtsText();
                    tts = greetings[i];
                    Utils.startTTSNoVoice(tts,answerSwitch,stopedListener);
                    Utils.eventTrack(FloatViewIdleService.this, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware_again, conditionId, R.string.condition_mainC4,tts);

                }

                @Override
                public void onFail() {
                    String  tts = greetings[i];
                    Utils.startTTSNoVoice(tts,answerSwitch,stopedListener);
                    Utils.eventTrack(FloatViewIdleService.this, R.string.skill_main, R.string.scene_main_aware, R.string.object_main_aware_again, conditionId, R.string.condition_mainC4,tts);

                }
            });

        } else {
            Utils.startTTSNoVoice(presetAnswer,answerSwitch, stopedListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        LogUtils.d(TAG, "onDestroy");
    }

    private JumpEntity mJumpEntity;
    private String pkg="",action="";
    private static final String IFLY_PACKAGE = "com.oushang.voice";
    private static final String ACTION_OPEN_GUIDE = "openguide";
    private static final String ACTION_OPEN_VIDEO = "openvideo";
    public static final String ACTION_OPEN_MODULE = "openmodule";
    public static final String ACTION_OPEN_SKILL = "openskill";
    private void analysisJumpContent(String message){
        Log.d(TAG, "analysisJumpContent() called with: message = [" + message + "]");
        try {
            if(message.contains("//")){
                int pkgIndex = message.indexOf("//");
                int pkgEndIndex = message.indexOf("?");
                int firstEqual = message.indexOf("=");
                int firstAnd = message.indexOf("&");
                pkg = message.substring(pkgIndex+2,pkgEndIndex);
                action = message.substring(firstEqual+1,firstAnd);
                if(IFLY_PACKAGE.equals(pkg)){
                    if(ACTION_OPEN_GUIDE.equals(action)&&message.contains("instructTeach=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        int instructTeachIndex = message.indexOf("instructTeach=");
                        int lastAnd = message.lastIndexOf("&");
                        String instructTeach = message.substring(instructTeachIndex+14,lastAnd);
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.instructTeach = instructTeach;
                        mJumpEntity.action = action;
                        mJumpEntity.timeout = time;
                    }else if(ACTION_OPEN_VIDEO.equals(action)&&message.contains("videoId=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        int videoIndex = message.indexOf("videoId=");
                        int lastAnd = message.lastIndexOf("&");
                        String videoId = message.substring(videoIndex+8,lastAnd);
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.videoId = videoId;
                        mJumpEntity.action = action;
                        mJumpEntity.timeout = time;
                    }else if(ACTION_OPEN_SKILL.equals(action)&&message.contains("moduleName=")&&message.contains("skillName=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        mJumpEntity.action = action;
                        int moduleIndex = message.indexOf("moduleName=");
                        int skillIndex = message.indexOf("skillName=");
                        int secondAnd = message.indexOf("&",moduleIndex);
                        int lastAnd = message.lastIndexOf("&");
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.moduleName = message.substring(moduleIndex+11,secondAnd);
                        mJumpEntity.skillName = message.substring(skillIndex+10,lastAnd);
                        mJumpEntity.timeout = time;
                        Log.d(TAG, "analysisJumpContent() called with: moduleIndex = [" + moduleIndex + "]"+"..skillIndex:"+skillIndex+".."+secondAnd+"..."+timeIndex);
                    }else if(ACTION_OPEN_MODULE.equals(action)&&message.contains("moduleName=")&&message.contains("skillName=")&&message.contains("timeout=")){
                        mJumpEntity = new JumpEntity();
                        mJumpEntity.action = action;
                        int moduleIndex = message.indexOf("moduleName=");
                        int skillIndex = message.indexOf("skillName=");
                        int secondAnd = message.indexOf("&",moduleIndex);
                        int lastAnd = message.lastIndexOf("&");
                        int timeIndex = message.indexOf("timeout=");
                        String time = message.substring(timeIndex+8);
                        mJumpEntity.moduleName = message.substring(moduleIndex+11,secondAnd);
                        mJumpEntity.skillName = message.substring(skillIndex+10,lastAnd);
                        mJumpEntity.timeout = time;
                    }
                }
            }else return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "analysisJumpContent() called with: mJumpEntity = [" + mJumpEntity + "]");
    }

}
