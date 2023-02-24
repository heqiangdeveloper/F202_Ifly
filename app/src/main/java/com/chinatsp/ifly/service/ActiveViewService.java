package com.chinatsp.ifly.service;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.activeservice.AudioSynthesis;
import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.commonService.okHttpRequestService;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.callback.onConfirmCallback;
import com.chinatsp.ifly.db.TtsInfoDbDao;
import com.chinatsp.ifly.module.me.recommend.model.HuVoiceAsssitContentModel;
import com.chinatsp.ifly.remote.RemoteManager;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.LocateManager;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import okhttp3.internal.Util;

import static com.chinatsp.ifly.api.activeservice.ActiveServiceModel.Activit_tts_msg;


/**
 * Created by zhengxbon 2019/5/11.
 * 主动服务的后台服务
 */

public class ActiveViewService extends Service {

    private static final String TAG = "ActiveViewService";

    public static Handler mHandler;

    public final static int VIEW_ABNORMAL_WEATHER = 1;
    public final static int VIEW_DESTINATION_WEATHER = 2;
    public final static int VIEW_OIL_SHORTAGE = 3;
    public final static int VIEW_ELEC_SHORTAGE = 4;
    public final static int VIEW_IN_WORK_LINE = 5;
    public final static int VIEW_OUT_WORK_LINE = 6;
    public final static int VIEW_BIRTHDAY_WISHES = 7;
    public final static int VIEW_HOLIDAY_WISHES = 8;
    public final static int VIEW_REPAIR_STATION = 9;
    public final static int VIEW_NULL_AUDIO = 10;
    public final static int VIEW_MORE_FAULT = 11;
    public final static int VIEW_POPUP_NEWS = 12;
    public final static int VIEW_OS_SWND = 13;
    public final static int GET_TTS_MESSAGE = 1003;
    public final static int POPUP_NEWS_PUSH = 21;
    public final static int OS_SEND_PUSH = 22;
    public final static int VIEW_LOCK_UNLOCK_CLOSED = 23;
    public final static int VIEW_LOCK_CLOSED = 24;
    public final static int VIEW_UNLOCK_CLOSED = 25;
    public final static int VIEW_UNLOCK_CLOSED_SIGNAL_5 = 26;

    public static String Activt_OIL_SHORTAGE;
    public static String Activt_Fault_INFo;

    public static Double popup_news_latitude = 0.0;
    public static Double popup_news_longitude = 0.0;

    private final static int ACTIVE_SERVICE = 13;
    private final static int ACTIVE_COMMAND = 14;
    private final static int ACTIVE_REGIST = 15;
    private static Context mContext;

    private ActiveServiceViewManager activeServiceViewManager;

    //设置优先级  生日>上下班>异常天气
    private int getloading = 0;
    private int gettoken = 0;
    private int getCommandtoken = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"lh:service start");
        initHandler();
        activeServiceViewManager = ActiveServiceViewManager.getInstance(this);
        ActiveServiceModel.getInstance().FestivalProvide_Shared(mContext, 1, "com.chinatsp.ifly", "com.chinatsp.ifly");
        ActiveServiceModel.getInstance().FestivalProvide_Shared(mContext, 2, "com.chinatsp.ifly", "com.chinatsp.ifly");
        mHandler.sendEmptyMessageDelayed(ACTIVE_SERVICE,35000);
        mHandler.sendEmptyMessageDelayed(VIEW_NULL_AUDIO, 60000);
        //延时时间大于ACTIVE_SERVICE 是为了第一次下载视频时，是防止第一拿到的视频黑名单为空
        mHandler.sendEmptyMessageDelayed(ACTIVE_COMMAND, 40000);
        mHandler.sendEmptyMessageDelayed(ACTIVE_REGIST, 40000);
        registerActServiceReceiver();

    }


    /*
     * 获取TTS播报数据
     */
    public static void getTtsMessage() {
        Log.d(TAG, "lh:getTtsMessage" );
        if(AppConfig.INSTANCE.token!=null&&!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            Log.d(TAG, "lh:get token-" + AppConfig.INSTANCE.token);
            String projectId = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_TTS_PROJECT_ID, "F202_02");
            String projectVersion = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_TTS_PROJECT_VERSION, "");
            if (!TtsInfoDbDao.getInstance(mContext).isTtsExist(TtsConstant.MAINC1CONDITION)){
                okHttpRequestService.getInstance().getHuTtsData(mContext, AppConfig.INSTANCE.token, projectId, "");
                Log.d(TAG,"SQLite is null ---------projectId: "+projectId+"  projectVersion "+projectVersion);
            }else {
                okHttpRequestService.getInstance().getHuTtsData(mContext, AppConfig.INSTANCE.token, projectId, projectVersion);
                Log.d(TAG,"SQLite is  not null-------------projectId: "+projectId+"  projectVersion "+projectVersion);
            }
        } else {
            Log.d(TAG, "lh:getTtsMessage fail: token is null");
        }

    }


    @SuppressLint("HandlerLeak")
    protected void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String[] location = LocateManager.getInstance(mContext).getLocation();
                String locationInfo = null;
                if (location != null) {
                    locationInfo = location[1] + "," + location[0];
                }
                Log.d(TAG,"zheng"+locationInfo);

                switch (msg.what) {
                    case ACTIVE_SERVICE:

                        getTtsMessage();

                        if(AppConfig.INSTANCE.token!=null&&!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {

                            Log.d(TAG, "ActiveViewService  Token不为空--------------------------------");
                            //节假日
//                            ActiveServiceModel.getInstance().HolidayWishes(activeServiceViewManager, mContext);
                            ActiveServiceModel.getInstance().GetAdvertisHttp(mContext);

                            //生日判断
                            ActiveServiceModel.getInstance().BirthdayWishes(mContext,activeServiceViewManager, new onConfirmCallback() {
                                @Override
                                public void onCallback(boolean isSuccess) {
                                    if (!isSuccess) {//如果不是生日的时候
                                        mHandler.sendEmptyMessage(VIEW_ABNORMAL_WEATHER);
                                    } else {//如果是生日的时候则延迟一分钟发送
                                        mHandler.sendEmptyMessageDelayed(VIEW_ABNORMAL_WEATHER, 50 * 1000);
                                    }
                                }
                            });
//                            mHandler.sendEmptyMessage(VIEW_ABNORMAL_WEATHER);

                        }else {
                            gettoken++;
                            if (gettoken<3){
                                mHandler.sendEmptyMessageDelayed(ACTIVE_SERVICE,10 * 1000);
                                AppConfig.INSTANCE.updateToken();
                            }

                            Log.d(TAG, "ActiveViewService  Token--------------------------------");
                        }

                        break;

                    case VIEW_ABNORMAL_WEATHER: //上下班时间判断
                        if (locationInfo!=null&&TextUtils.isEmpty(locationInfo)) {
                            getloading++;
                            if (getloading <= 3) {
                                sendEmptyMessageDelayed(VIEW_ABNORMAL_WEATHER, 5000);
                            }
                        } else {
                            //上下班时间判断
                            InWork(mContext,locationInfo);
                        }
                        break;

                    //异常天气
                    case VIEW_DESTINATION_WEATHER:
                       /* if (locationInfo!=null&&!TextUtils.isEmpty(locationInfo)) {
                            ActiveServiceModel.getInstance().AbnormalWeather(activeServiceViewManager, locationInfo, mContext);
                        }*/
                        break;


                    case VIEW_NULL_AUDIO://在检查到改目录为空时，自动合成音频文件

//                        Set_AudioSynthesis("/storage/emulated/0/txz/online/");
                        break;
                    case POPUP_NEWS_PUSH://微信位置
                        ActiveServiceModel.Popup_news_message = (String) msg.obj;
                        ActiveServiceModel.Activit_tts_msg = ActiveServiceModel.Popup_news_message;
                        activeServiceViewManager.show(VIEW_POPUP_NEWS);
                        break;
                    case OS_SEND_PUSH://oushan style
                        ActiveServiceModel.Os_send_message = (String) msg.obj;
                        ActiveServiceModel.Activit_tts_msg =  ActiveServiceModel.Os_send_message;
                        activeServiceViewManager.show(VIEW_OS_SWND);
                        break;
                    case ACTIVE_COMMAND:
                        Log.d(TAG, "handleMessage() called with: getcommand = [" + AppConfig.INSTANCE.token + "]");
                        if(AppConfig.INSTANCE.token!=null&&!TextUtils.isEmpty(AppConfig.INSTANCE.token)){
                            String contentVersion = SharedPreferencesUtils.getString(mContext, AppConstant.KEY_TTS_CONTENT_VERSION, "");
                            Log.d(TAG,"contentVersion="+contentVersion);
                            HuVoiceAsssitContentModel.getInstance().getHuVoiceAsssitContent(mContext,AppConfig.INSTANCE.token, "F202_02",contentVersion);
                        }else{
                            getCommandtoken++;
                            if (getCommandtoken<10){
                                mHandler.sendEmptyMessageDelayed(ACTIVE_COMMAND,5 * 1000);
                                AppConfig.INSTANCE.updateToken();
                            }

                            Log.d(TAG, "ActiveViewService   getcommand Token--------------------------------");
                        }

                        break;
                    case ACTIVE_REGIST:
                        AppConfig.INSTANCE.registerManageListener();
                        break;

                }
            }
        };
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ActiveViewService.class);
        context.startService(intent);
        mContext = context;
    }

    public static void startGetCOmmand() {
        mHandler.removeMessages(ACTIVE_COMMAND);
        mHandler.sendEmptyMessageDelayed(ACTIVE_COMMAND, 1000);
    }

    public static void startGetTtsMessage() {
        mHandler.removeMessages(GET_TTS_MESSAGE);
        mHandler.sendEmptyMessageDelayed(GET_TTS_MESSAGE, 1000);
    }


    public static void init(Context context){
        Log.d(TAG,"lh:active start by hand");
        mHandler.sendEmptyMessage(ACTIVE_SERVICE);
        mHandler.sendEmptyMessageDelayed(GET_TTS_MESSAGE, 30000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mActServiceReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * 判断是否为工作日，时间是否符合上下班时间、不是上下班则判断异常天气
     */
    public void InWork(Context mContext,String location) {

        //上班导航
        ActiveServiceModel.getInstance().InWorkLine(mContext,activeServiceViewManager, location, new onConfirmCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess){
                    //异常天气
                    mHandler.sendEmptyMessageDelayed(VIEW_DESTINATION_WEATHER,20*1000);
                }else {
                    mHandler.sendEmptyMessage(VIEW_DESTINATION_WEATHER);
                }
                Log.d(TAG,"上班导航 isSuccess "+isSuccess);
            }
        });
        //下班导航
        ActiveServiceModel.getInstance().OutWorkLine(mContext,activeServiceViewManager, location, new onConfirmCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess){
                    //异常天气
                    mHandler.sendEmptyMessageDelayed(VIEW_DESTINATION_WEATHER,20*1000);
                }else {
                    mHandler.sendEmptyMessage(VIEW_DESTINATION_WEATHER);
                }
                Log.d(TAG,"下班导航 isSuccess "+isSuccess);
            }
        });


    }


    /**
     *在检查到改目录为空时，自动合成音频文件
     */
    private void Set_AudioSynthesis(String string){
        File file = new File(string);
        if (!file.exists()){
            //在检查到改目录为空时，自动合成音频文件
            AudioSynthesis.getInstance(mContext).GetAudio();
        }
    }


    private void registerActServiceReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.ACTION_POPUP_NEWS_DETAIL);
        filter.addAction(AppConstant.ACTION_RECEIVE_STYLE);
        registerReceiver(mActServiceReceiver, filter);
    }


    private BroadcastReceiver mActServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"//欧尚style"+intent.getAction());
            if (/*AppConstant.ACTION_POPUP_NEWS_DETAIL.equals(intent.getAction())*/
                    AppConstant.ACTION_RECEIVE_STYLE.equals(intent.getAction())) {// TODO ica 项目 微信位置由这个广播接收
                Log.d(TAG,"-------//美行"+intent.getAction());
                //判断播报场景优先级
                int priority = AppConstant.SendToCarPriority;
                Log.d("heqiangq","Utils.getCurrentPriority(mContext) = " + Utils.getCurrentPriority(mContext));
                if(!Utils.checkPriority(mContext,priority)){
                    Log.d(TAG,"checkPriority() = " + Utils.checkPriority(mContext,priority));
                    return;
                }
                Utils.openScreen();//打开屏幕

                //屏蔽掉
                /*final String name = intent.getStringExtra("popup_news_name");
                final String address = intent.getStringExtra("popup_news_address");
                popup_news_latitude = intent.getDoubleExtra("popup_news_latitude", 0);
                popup_news_longitude = intent.getDoubleExtra("popup_news_longitude", 0);
                final String type = intent.getStringExtra("popup_news_type");*/
                //移植微信倒车逻辑
                popup_news_latitude = intent.getDoubleExtra("os_send_latitude", 0);
                popup_news_longitude = intent.getDoubleExtra("os_send_longitude", 0);
                final String address = intent.getStringExtra("os_send_address");
                final String type = intent.getStringExtra("popup_news_type");
                final String name = intent.getStringExtra("os_send_name");
                int sendtype = intent.getIntExtra("sendtype",0);  //消息类型  0 -微信倒车    1-顺风耳

                if(sendtype!=0){
                    Log.e(TAG, "onReceive: sendtype::"+sendtype);
                    RemoteManager.getInstance(ActiveViewService.this).weChatTocar(popup_news_latitude,popup_news_longitude,address,name);
                    return;
                }
                Log.d(TAG,"-------//美行 name:"+name +" address  :"+address+"  type :"+type+"..."+sendtype);
//                activeServiceViewManager.show(VIEW_POPUP_NEWS);
//                ActiveServiceModel.Activit_tts_msg = mContext.getString(R.string.msg_to_popup);
                String defaultText = mContext.getString(R.string.msg_to_popup);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MSGC48CONDITION, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                        String speakText = tts;
                        if (TextUtils.isEmpty(tts)) {
                            speakText = defaultText;
                        }
                        Log.d(TAG, "onConfirm: speakText = " + speakText);
                        Message msg = new Message();
                        msg.what = POPUP_NEWS_PUSH;
                        msg.obj = speakText;
                        mHandler.sendMessage(msg);
                    }
                });
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_weichat,R.string.object_active_tuisong,TtsConstant.MSGC48CONDITION,R.string.condition_chexinC1);

            }
           /* else if (AppConstant.ACTION_RECEIVE_STYLE.equals(intent.getAction())) {//欧尚style TODO 两个action相同，暂时保留
                Log.d(TAG,"-------//欧尚style"+intent.getAction());
                //判断播报场景优先级
                int priority = AppConstant.SendToCarPriority;
                if(!Utils.checkPriority(mContext,priority)){
                    Log.d(TAG,"checkPriority() = " + Utils.checkPriority(mContext,priority));
                    return;
                }
                Utils.openScreen();//打开屏幕
                popup_news_latitude = intent.getDoubleExtra("os_send_latitude", 0);
                popup_news_longitude = intent.getDoubleExtra("os_send_longitude", 0);
                final String address = intent.getStringExtra("os_send_address");
                final String type = intent.getStringExtra("popup_news_type");
                final String name = intent.getStringExtra("os_send_name");
//                activeServiceViewManager.show(VIEW_OS_SWND);
//                ActiveServiceModel.Activit_tts_msg = mContext.getString(R.string.msg_to_os_send);
                String defaultText = mContext.getString(R.string.msg_to_os_send);
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MSGC51CONDITION, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        Log.d(TAG, "onConfirm: tts = " + tts + "defaultText = " + defaultText);
                        String speakText = tts;
                        if (TextUtils.isEmpty(tts)) {
                            speakText = defaultText;
                        }
                        Log.d(TAG, "onConfirm: speakText = " + speakText);
                        Message msg = new Message();
                        msg.what = OS_SEND_PUSH;
                        msg.obj = speakText;
                        mHandler.sendMessage(msg);
                    }
                });
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_ossendcar,R.string.object_active_tuisong,TtsConstant.MSGC51CONDITION,R.string.condition_chexinC1);
                Log.d(TAG,"-------//欧尚style:"+name +" address  :"+address+"  type :"+type);
            }*/
        }

    };
}
