package com.chinatsp.ifly;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.car.CarNotConnectedException;
import android.car.hardware.constant.VEHICLE;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.activeservice.birthday.BirthdayViewManager;
import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.entity.SearchPoiEvent;
import com.chinatsp.ifly.service.ActiveViewService;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.AppController;
import com.chinatsp.ifly.voice.platformadapter.controller.CarController;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.example.mxextend.IExtendApi;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.entity.SearchResultModel;
import com.example.mxextend.listener.IExtendCallback;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.mvw.MvwSession;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static android.car.hardware.cabin.CarCabinManager.ID_KEY_SMART_LOCK;
import static android.car.hardware.cabin.CarCabinManager.ID_KEY_SMART_UNLOCK;
import static android.car.hardware.constant.VehicleAreaId.WINDOW_REAR_WINDSHIELD;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_DEFROSTER;

public class ActiveServiceViewManager {

    private final String TAG = "ActiveServiceViewManager";

    private static ActiveServiceViewManager viewManager;
    private Context mContext;
    private WindowManager winManager;
    private HashMap<Integer, PushView> pushViewMap = new HashMap<>();
    private HashMap<Integer, WindowManager.LayoutParams> paramsMap = new HashMap<>();
    private Handler handler = new Handler();
    private int displayWidth;
    private int displayHeight;
    public static boolean ActiveServiceView_Show = false;
    private int oldTspSecene;
    private boolean FloatView_isHide = false;
    private static boolean OIL_show = true;
    private boolean isAlreadyReceivedBroadcast = false;

    private ActiveServiceViewManager(Context context) {
        this.mContext = context.getApplicationContext();
        winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        displayWidth = winManager.getDefaultDisplay().getWidth();
        displayHeight = winManager.getDefaultDisplay().getHeight();
    }

    public static synchronized ActiveServiceViewManager getInstance(Context context) {
        if (viewManager == null) {
            viewManager = new ActiveServiceViewManager(context);
        }
        return viewManager;
    }

    public void clearCurrentWindow(){
        if(null != pushViewMap && pushViewMap.size() >= 1){
            Log.e("heqiangq", "pushViewMap.size() > 1");
            int oldType = 0;
            if(TTSController.getInstance(mContext).isTtsPlaying()) TTSController.getInstance(mContext).stopTTS();
            for(Iterator<Map.Entry<Integer, PushView>> it = pushViewMap.entrySet().iterator(); it.hasNext();){
                Map.Entry<Integer, PushView> item = it.next();
                oldType = item.getKey();
                //it.remove();
                winManager.removeView(pushViewMap.get(oldType));
                pushViewMap.remove(oldType);
                paramsMap.remove(oldType);
            }
            ActiveServiceView_Show = false;
            Log.e("heqiangq", "pushViewMap.size() = 0");
        }
    }

    public void show(int type) {
       show(type,"","");
    }

    public void show(int type,String contidionid,String tts) {
        //先清除比自己优先级低的那个主动服务
        clearCurrentWindow();
        Log.e("heqiangq", "zheng  show-----------------");
        Log.d("heqiangq","pushViewMap.get(type)  = "+ pushViewMap.get(type) + "ActiveServiceView_Show = " + ActiveServiceView_Show );
        if (pushViewMap.get(type) == null && !ActiveServiceView_Show) {
            PushView pushView = new PushView(mContext, type,contidionid,tts);
            WindowManager.LayoutParams params = getParams(pushViewMap.size());
            pushViewMap.put(type, pushView);
            paramsMap.put(type, params);
            winManager.addView(pushView, params);
            ActiveServiceView_Show = true;
            if (pushViewMap.size() <= 1) {//判断当前Active窗口只有一个
                Log.e("zheng  show", "zheng  show");
                oldTspSecene = TspSceneAdapter.getTspScene(mContext);
//                MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CONFIRM, Utils.getFromAssets(mContext, "mvw_active.json"));
                MVWAgent.getInstance().stopMVWSession();
                MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_CONFIRM);
            }
        }
        Log.e("zheng", "zheng----------show  type："+type);
    }


    public void hide(int type) {
        Log.d(TAG, "hide: pushViewMap.size = " + pushViewMap.size());
        if (pushViewMap.get(type) != null) {
            pushViewMap.get(type).unregisterBroadcast();//注销“确定”，“取消”广播
            winManager.removeView(pushViewMap.get(type));
            Log.d(TAG, "hide: winManager.removeView called...");
            pushViewMap.remove(type);
            paramsMap.remove(type);
        }
        if (pushViewMap.size() > 0) {
            //更新已存在pushView的位置
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Set<Integer> typeSet = pushViewMap.keySet();
                    List<Integer> keyList = new ArrayList<>(typeSet);
                    for (int i = 0; i < keyList.size(); i++) {
                        PushView pushView = pushViewMap.get(keyList.get(i));
                        WindowManager.LayoutParams params = paramsMap.get(keyList.get(i));
                        params.y = 20 + i * (params.height + 15);
                        winManager.updateViewLayout(pushView, params);
                    }
                }
            }, 200);
        }
        //停止TTS播报
        if(TTSController.getInstance(mContext).isTtsPlaying()) TTSController.getInstance(mContext).stopTTS();
        ActiveServiceView_Show = false;
        if (pushViewMap.size() == 0) {//判断当前Active窗口为0
//            MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_CONFIRM);
            TspSceneManager.getInstance().resetScrene(mContext,oldTspSecene);
        }
    }

    private WindowManager.LayoutParams getParams(int pushViewCount) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT > 25) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.width = 1300; // pushView宽
        params.height = 200; // pushView高
        params.x = displayWidth / 2 - params.width / 2;
        params.y = 20 + pushViewCount * (params.height + 15);
        return params;
    }

    class PushView extends LinearLayout {

        private static final int ACTION_ALLOWED = 0x10;
        private static final int ACTION_IGNORED = 0x11;
        private static final int ACTION_ALLOWED_TIMEOUT = 0x12;
        private static final int ACTION_REFRESH_UI = 0x13;
        private static final int ACTION_VIEWONCLICK = 0x14;
        private static final int ACTION_CHECKSIGNAL = 0x15;
        private static final int ACTION_OPEN_CARCONTROL = 0x16;
        private int type;
        private ImageView ivIcon;
        private TextView tvTitle;
        private TextView tvSubTitle;
        private TextView tvTime;
        private TextView tvAllow;
        private TextView tvDeny;
        private int countDownTime = 15;
        private Handler mUiHandler;
        private Handler mHandlerTimer;
        private boolean One_TTS = false;
        private int checkSignalDelay = 1000;
        private int checkCarControlDelay = 400;
        private Message checkMsg = new Message();
        private Map checkMap = new HashMap();
        private int checkTimes = 0;
        private boolean isClickButton = false;

        @SuppressLint("HandlerLeak")
        public PushView(final Context context, final int type) {
            this(context,type,"","");
        }



        @SuppressLint("HandlerLeak")
        public PushView(final Context context, final int type,String contionid,String tts) {
            super(context);
            this.type = type;

            View view = LayoutInflater.from(context).inflate(R.layout.layout_push_view, this);
            ivIcon = view.findViewById(R.id.iv_push_icon);
            tvTitle = view.findViewById(R.id.tv_push_title);
            tvSubTitle = view.findViewById(R.id.tv_push_subtitle);
            tvAllow = view.findViewById(R.id.tv_push_allow);
            tvDeny = view.findViewById(R.id.tv_push_deny);


            IntentFilter filter = new IntentFilter();
            filter.addAction(AppConstant.ACTION_INITIATIVE_START);
            filter.addAction(AppConstant.ACTION_INITIATIVE_SHUTDOWN);
            context.registerReceiver(mActServiceReceiver, filter);

//            if (FloatViewManager.getInstance(context).isHide()) {
//                FloatView_isHide = true;
//            }

            FloatView_isHide = true;
            if (type == ActiveViewService.VIEW_ABNORMAL_WEATHER) {
                Log.e("zheng","zheng ------------: 天气异常提醒：FloatView_isHide"+FloatView_isHide);
                tvTitle.setText(context.getString(R.string.msg_to_weather));
                tvTitle.setTextColor(context.getColor(R.color.push_title_normal));
                tvSubTitle.setText(ActiveServiceModel.getInstance().AbnormalWeather_message);
                //tvAllow.setText("是");
                tvAllow.setVisibility(View.GONE);
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.weather));
              //  SendNotification(context, R.color.push_title_warning, context.getString(R.string.msg_to_weather), ActiveServiceModel.getInstance().AbnormalWeather_message);
                if (FloatView_isHide) {
//                    startTTS(ActiveServiceModel.getInstance().AbnormalWeather_message,bundle);
                    Log.e("zheng","zheng ------------: 天气异常提醒-------FloatView_isHide-------"+FloatView_isHide);
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.getInstance().AbnormalWeather_message);
                }

                ActiveServiceModel.getInstance().uploadCallback("badWeather");
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_weather,R.string.object_abnormal_weather,
                        TtsConstant.MSGC30CONDITION,R.string.condition_chexinC1,ActiveServiceModel.getInstance().AbnormalWeather_message);
            } else if (type == ActiveViewService.VIEW_OIL_SHORTAGE /*&& OIL_show*/) {
//                OIL_show = false;
                tvTitle.setText(context.getString(R.string.msg_to_no_oil));
                tvTitle.setTextColor(context.getColor(R.color.push_title_warning));
                tvSubTitle.setText(context.getString(R.string.msg_to_car_oil_not_enough));
                tvDeny.setText(mContext.getString(R.string.msg_to_cancel_nav));
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_oil_shortage));
                SendNotification(context, R.drawable.active_oil_shortage, context.getString(R.string.msg_to_no_oil), tts,false);
                TTSController.getInstance(context).startTTSOnly(tts,PriorityControler.PRIORITY_TWO);
                if(TtsConstant.MSGC7CONDITION.equals(contionid))
                   Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_oil,R.string.object_active_tuisong,TtsConstant.MSGC7CONDITION,R.string.condition_msgc7);
                else
                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_oil,R.string.object_active_tuisong,TtsConstant.MSGC8CONDITION,R.string.condition_msgc8);
            } else if (type == ActiveViewService.VIEW_ELEC_SHORTAGE) {
                tvTitle.setText(context.getString(R.string.msg_to_elec));
                tvTitle.setTextColor(context.getColor(R.color.push_title_warning));
                tvSubTitle.setText(context.getString(R.string.msg_to_car_electricity_not_enough));
                tvDeny.setText(mContext.getString(R.string.msg_to_cancel_nav));
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_elec_shortage));
                SendNotification(context, R.drawable.active_elec_shortage, context.getString(R.string.msg_to_elec), tts,false);
                TTSController.getInstance(context).startTTSOnly(tts,PriorityControler.PRIORITY_TWO);
                if(TtsConstant.MSGC13CONDITION.equals(contionid))
                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_dian,R.string.object_active_tuisong,TtsConstant.MSGC13CONDITION,R.string.condition_msgc13);
                else
                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_dian,R.string.object_active_tuisong,TtsConstant.MSGC14CONDITION,R.string.condition_msgc14);
            } else if (type == ActiveViewService.VIEW_IN_WORK_LINE) {
                Log.e("zheng", "zheng----------show  gowork：FloatView_isHide"+FloatView_isHide);
                tvTitle.setText(context.getString(R.string.msg_to_got_work));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                tvSubTitle.setText(ActiveServiceModel.getInstance().InWorkLine_message);
                tvDeny.setText(mContext.getString(R.string.msg_to_cancel_nav));
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_work_line));
                SendNotification(context, R.drawable.active_work_line, context.getString(R.string.msg_to_got_work), ActiveServiceModel.getInstance().InWorkLine_message ,false);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.getInstance().InWorkLine_message,AppConstant.InOutWorkPriority);
                }
                ActiveServiceModel.getInstance().uploadCallback("gowork");
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_gowork,R.string.object_active_tuisong,TtsConstant.MSGC1CONDITION,R.string.condition_chexinC1,ActiveServiceModel.getInstance().InWorkLine_message);

            } else if (type == ActiveViewService.VIEW_OUT_WORK_LINE) {
                tvTitle.setText(context.getString(R.string.msg_to_go_home));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                tvSubTitle.setText(ActiveServiceModel.getInstance().OutWorkLine_message);
                tvDeny.setText(mContext.getString(R.string.msg_to_cancel_nav));
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_work_line));
                SendNotification(context, R.drawable.active_work_line, context.getString(R.string.msg_to_go_home), ActiveServiceModel.getInstance().OutWorkLine_message,false);
                if (FloatView_isHide) {
//                    startTTS(ActiveServiceModel.getInstance().OutWorkLine_message);
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.getInstance().OutWorkLine_message,AppConstant.InOutWorkPriority);
                }
                Log.e("zheng", "zheng----------show  go home："+type);
                ActiveServiceModel.getInstance().uploadCallback("backHome");
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_gohome,R.string.object_active_tuisong,TtsConstant.MSGC4CONDITION,R.string.condition_chexinC1,ActiveServiceModel.getInstance().OutWorkLine_message);
            }
            else if (type == ActiveViewService.VIEW_REPAIR_STATION) {
                tvTitle.setText(context.getString(R.string.msg_to_go_station));
                tvTitle.setTextColor(context.getColor(R.color.push_title_warning));
                tvSubTitle.setText(ActiveViewService.Activt_Fault_INFo);
                tvAllow.setText(context.getString(R.string.msg_to_go_weixiux));
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_carifon));
              //  SendNotification(context, R.drawable.active_carifon, context.getString(R.string.msg_to_go_station), ActiveViewService.Activt_Fault_INFo);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveViewService.Activt_Fault_INFo);
                }
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_guzhang,R.string.object_active_tuisong,TtsConstant.MSGC22CONDITION,R.string.condition_active2);
            } else if (type == ActiveViewService.VIEW_MORE_FAULT) {//多个故障信息提示
                tvTitle.setText(context.getString(R.string.msg_to_go_station));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_warning));
                tvSubTitle.setText(ActiveViewService.Activt_Fault_INFo);
                tvAllow.setText(context.getString(R.string.msg_to_go_weixiux));
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_carifon));
              //  SendNotification(context, R.drawable.active_carifon, context.getString(R.string.msg_to_go_station), ActiveViewService.Activt_Fault_INFo);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveViewService.Activt_Fault_INFo);
                }
                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_guzhang,R.string.object_active_tuisong,TtsConstant.MSGC22CONDITION,R.string.condition_active1);
            }else if (type == ActiveViewService.VIEW_POPUP_NEWS){////微信位置
                tvTitle.setText(mContext.getString(R.string.msg_to_popup_nav));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                tvSubTitle.setText(ActiveServiceModel.Popup_news_message);
                tvDeny.setText(mContext.getString(R.string.msg_to_cancel_nav));
                tvAllow.setTextColor(Color.WHITE);
                tvDeny.setTextColor(Color.WHITE);
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.activite_service_wechat_logo));
                SendNotification(context, R.drawable.activite_service_wechat_logo, mContext.getString(R.string.msg_to_popup_nav), ActiveServiceModel.Popup_news_message,true);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.Popup_news_message,AppConstant.SendToCarPriority);
                }
                //埋点已写在ActiveViewService.java中
            }else if (type == ActiveViewService.VIEW_OS_SWND){//oushang style位置
                tvTitle.setText(mContext.getString(R.string.msg_to_phone_nav));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                tvSubTitle.setText(ActiveServiceModel.Os_send_message);
                tvDeny.setText(mContext.getString(R.string.msg_to_cancel_nav));
                tvAllow.setTextColor(Color.WHITE);
                tvDeny.setTextColor(Color.WHITE);
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.activite_service_oushang_logo));
                SendNotification(context, R.drawable.activite_service_oushang_logo, mContext.getString(R.string.msg_to_phone_nav), ActiveServiceModel.Os_send_message,true);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.Os_send_message,AppConstant.SendToCarPriority);
                }
                //埋点已写在ActiveViewService.java中
            }else if (type == ActiveViewService.VIEW_LOCK_UNLOCK_CLOSED){//迎宾  解锁闭锁都关闭
                tvTitle.setText(mContext.getString(R.string.msg_lock_unlock_title));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                //tvSubTitle.setText(ActiveServiceModel.Active_Content_str);
                tvSubTitle.setText(ActiveServiceModel.Lock_unLock_closed_message);
                tvAllow.setText(mContext.getString(R.string.msg_to_sure_nav));
                tvAllow.setTextColor(Color.WHITE);
                tvDeny.setTextColor(Color.WHITE);
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_service_welcome_user));
                SendNotification(context, R.drawable.active_service_welcome_user, mContext.getString(R.string.msg_lock_unlock_title), ActiveServiceModel.Lock_unLock_closed_message,false);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.Lock_unLock_closed_message,AppConstant.WelcomeUserPriority);
                }
                //埋点已写在ActiveServiceModel.java中
            }else if (type == ActiveViewService.VIEW_UNLOCK_CLOSED){//迎宾  解锁关闭
                tvTitle.setText(mContext.getString(R.string.msg_unlock_title));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                //tvSubTitle.setText(ActiveServiceModel.Active_Content_str);
                tvSubTitle.setText(ActiveServiceModel.UnLock_closed_message);
                tvAllow.setText(mContext.getString(R.string.msg_to_sure_nav));
                tvAllow.setTextColor(Color.WHITE);
                tvDeny.setTextColor(Color.WHITE);
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_service_welcome_user));
                SendNotification(context, R.drawable.active_service_welcome_user, mContext.getString(R.string.msg_unlock_title), ActiveServiceModel.UnLock_closed_message,false);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.UnLock_closed_message,AppConstant.WelcomeUserPriority);
                }
                //埋点已写在ActiveServiceModel.java中
            }else if (type == ActiveViewService.VIEW_LOCK_CLOSED){//迎宾  闭锁关闭
                tvTitle.setText(mContext.getString(R.string.msg_lock_title));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                //tvSubTitle.setText(ActiveServiceModel.Active_Content_str);
                tvSubTitle.setText(ActiveServiceModel.Lock_closed_message);
                tvAllow.setText(mContext.getString(R.string.msg_to_sure_nav));
                tvAllow.setTextColor(Color.WHITE);
                tvDeny.setTextColor(Color.WHITE);
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_service_welcome_user));
                SendNotification(context, R.drawable.active_service_welcome_user, mContext.getString(R.string.msg_lock_title), ActiveServiceModel.Lock_closed_message,false);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.Lock_closed_message,AppConstant.WelcomeUserPriority);
                }
                //埋点已写在ActiveServiceModel.java中
            }else if (type == ActiveViewService.VIEW_UNLOCK_CLOSED_SIGNAL_5){//迎宾  闭锁关闭，且signal = 5
                tvTitle.setText(mContext.getString(R.string.msg_unlock_title));
                tvTitle.setTextColor(mContext.getColor(R.color.push_title_normal));
                //tvSubTitle.setText(ActiveServiceModel.Active_Content_str);
                tvSubTitle.setText(ActiveServiceModel.UnLock_closed_message_signal_5);
                tvAllow.setText(mContext.getString(R.string.msg_to_sure_nav));
                tvAllow.setTextColor(Color.WHITE);
                tvDeny.setTextColor(Color.WHITE);
                ivIcon.setImageDrawable(getResources().getDrawable(R.drawable.active_service_welcome_user));
                SendNotification(context, R.drawable.active_service_welcome_user, mContext.getString(R.string.msg_unlock_title), ActiveServiceModel.UnLock_closed_message_signal_5,false);
                if (FloatView_isHide) {
                    TTSController.getInstance(context).startTTSOnly(ActiveServiceModel.UnLock_closed_message_signal_5, AppConstant.WelcomeUserPriority);
                }
                //埋点已写在ActiveServiceModel.java中
            }

            //tvDeny.setText(getTimeText("“关闭”", countDownTime));
            setCancelText(type,15);
            tvAllow.setOnClickListener(new PositiveOnclickListener());
            tvDeny.setOnClickListener(new NegativeOnclickListener());
            //view.setOnClickListener(new CarDetailsOnclickListener());

            final HandlerThread thread = new HandlerThread(TAG);
            thread.start();

            mUiHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case ACTION_ALLOWED:
                            //TODO 进行主动服务跳转
                            hide(type);
                            thread.quitSafely();
                            One_TTS = false;
                            isAlreadyReceivedBroadcast = false;
                            IExtendApi extendApi = TAExtendManager.getInstance().getApi();
                            if (type == ActiveViewService.VIEW_ABNORMAL_WEATHER) {
                                //异常天气
                            } else if (type == ActiveViewService.VIEW_OIL_SHORTAGE) {
                                //油量不足，导航去加油站
                                boolean isNaving = MXSdkManager.getInstance(mContext).isNaving();
                                if (isNaving) {
                                    //在导航界面
                                    SearchAlongRoute(extendApi, "加油站", context,TtsConstant.MSGC10CONDITION,mContext.getString(R.string.msgc10));
                                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_oil,R.string.object_active_xuanzhe,TtsConstant.MSGC10CONDITION,R.string.condition_msgc10);
                                } else {
                                    MXSdkManager.getInstance(mContext).backToMap(new MXSdkManager.Callback() {
                                        @Override
                                        public void success() {
                                            KeyWordSearch(extendApi, "加油站", context,TtsConstant.MSGC9CONDITION,mContext.getString(R.string.msgc10));
                                        }
                                    });
                                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_oil,R.string.object_active_xuanzhe,TtsConstant.MSGC9CONDITION,R.string.condition_msgc9);
                                }
                            } else if (type == ActiveViewService.VIEW_ELEC_SHORTAGE) {
                                //电量不足，导航去充电桩
                                boolean isNaving = MXSdkManager.getInstance(mContext).isNaving();
                                if (isNaving) {
                                    //在导航界面
                                    SearchAlongRoute(extendApi, "充电站", context,TtsConstant.MSGC16CONDITION,mContext.getString(R.string.msgc16));
                                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_dian,R.string.object_active_xuanzhe,TtsConstant.MSGC16CONDITION,R.string.condition_msgc16);
                                } else {
                                    MXSdkManager.getInstance(mContext).backToMap(new MXSdkManager.Callback() {
                                        @Override
                                        public void success() {
                                            KeyWordSearch(extendApi, "充电站", context,TtsConstant.MSGC15CONDITION,mContext.getString(R.string.msgc15));
                                        }
                                    });
                                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_dian,R.string.object_active_xuanzhe,TtsConstant.MSGC15CONDITION,R.string.condition_msgc15);
                                }
                            } else if (type == ActiveViewService.VIEW_IN_WORK_LINE) {
                                //导航去上班
                                naviToPoi(ActiveServiceModel.getInstance().InWorkLine_Lon,ActiveServiceModel.getInstance().InWorkLine_Lat);
                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_ONE,mContext,TtsConstant.MSGC2CONDITION, context.getString(R.string.msg_to_route),
                                        R.string.skill_active,R.string.scene_active_gowork,R.string.object_active_xuanzhe,R.string.condition_active12);
                            } else if (type == ActiveViewService.VIEW_OUT_WORK_LINE) {
                                //导航回家
                                naviToPoi(ActiveServiceModel.getInstance().OutWorkLine_Lon,ActiveServiceModel.getInstance().OutWorkLine_Lat);
                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_ONE,mContext,TtsConstant.MSGC5CONDITION, context.getString(R.string.msg_to_route),
                                        R.string.skill_active,R.string.scene_active_gohome,R.string.object_active_xuanzhe,R.string.condition_active12);
                            } else if (type == ActiveViewService.VIEW_BIRTHDAY_WISHES) {
                                //用户生日
                                BirthdayViewManager.getInstance(context).show(ActiveServiceModel.getInstance().BirthdayWishes_message);
                            }  else if (type == ActiveViewService.VIEW_HOLIDAY_WISHES) {
                                //去维修站
                                String string_nave = AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg();
                                if (AppConstant.PACKAGE_NAME_WECARNAVI.equals(string_nave)) {
                                    //在导航界面
                                    SearchAlongRoute(extendApi, "长安汽车维修站", context,"","");
                                } else {
                                    KeyWordSearch(extendApi, "长安汽车维修站", context,"","");
                                }
                                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_guzhang,R.string.object_active_xuanzhe,TtsConstant.MSGC27CONDITION,R.string.condition_active5);
                            }else if (type == ActiveViewService.VIEW_POPUP_NEWS){
                                naviToPoi(ActiveViewService.popup_news_longitude,ActiveViewService.popup_news_latitude);
                                if(isClickButton){
                                    startTtsAndCallbackPriority(PriorityControler.PRIORITY_THREE,TtsConstant.MSGC49CONDITION,R.string.msg_to_route);
                                    DatastatManager.getInstance().recordUI_event(context, context.getResources().getString(R.string.event_id_wechatsendcar_allow), "");
                                }else{
                                    getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_THREE,mContext,TtsConstant.MSGC49CONDITION, context.getString(R.string.msg_to_route),
                                            R.string.skill_active,R.string.scene_active_weichat,R.string.object_active_xuanzhe,R.string.condition_active12);
                                }
                            }else if (type == ActiveViewService.VIEW_OS_SWND){
                                naviToPoi(ActiveViewService.popup_news_longitude,ActiveViewService.popup_news_latitude);
                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_THREE,mContext,TtsConstant.MSGC52CONDITION, context.getString(R.string.msg_to_route),
                                        R.string.skill_active,R.string.scene_active_ossendcar,R.string.object_active_xuanzhe,R.string.condition_active12);
                            }else if (type == ActiveViewService.VIEW_LOCK_UNLOCK_CLOSED){//迎宾 解锁闭锁关闭
                                changeLockStatus(ID_KEY_SMART_UNLOCK, VEHICLE.ON);
                                changeLockStatus(ID_KEY_SMART_LOCK, VEHICLE.ON);
                                mUiHandler.sendEmptyMessageDelayed(ACTION_OPEN_CARCONTROL,checkCarControlDelay);

                                //1秒后，检查信号是否执行成功
                                checkMap.put("sendSignal",ID_KEY_SMART_UNLOCK);
                                checkMap.put("sendValue", VEHICLE.ON);
                                checkMsg.obj = checkMap;
                                checkMsg.what = ACTION_CHECKSIGNAL;
                                mUiHandler.sendMessageDelayed(checkMsg,checkSignalDelay);

                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_TWO,mContext,TtsConstant.MSGC94CONDITION, context.getString(R.string.msgC94),
                                        R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,R.string.condition_msgC94);
                            }else if (type == ActiveViewService.VIEW_UNLOCK_CLOSED || type == ActiveViewService.VIEW_UNLOCK_CLOSED_SIGNAL_5){//迎宾 解锁关闭
                                changeLockStatus(ID_KEY_SMART_UNLOCK, VEHICLE.ON);
                                mUiHandler.sendEmptyMessageDelayed(ACTION_OPEN_CARCONTROL,checkCarControlDelay);

                                //1秒后，检查信号是否执行成功
                                checkMap.put("sendSignal",ID_KEY_SMART_UNLOCK);
                                checkMap.put("sendValue",VEHICLE.ON);
                                checkMsg.obj = checkMap;
                                checkMsg.what = ACTION_CHECKSIGNAL;
                                mUiHandler.sendMessageDelayed(checkMsg,checkSignalDelay);

                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_TWO,mContext,TtsConstant.MSGC94CONDITION, context.getString(R.string.msgC94),
                                        R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,R.string.condition_msgC94);
                            }else if (type == ActiveViewService.VIEW_LOCK_CLOSED){//迎宾 闭锁关闭
                                changeLockStatus(ID_KEY_SMART_LOCK, VEHICLE.ON);
                                mUiHandler.sendEmptyMessageDelayed(ACTION_OPEN_CARCONTROL,checkCarControlDelay);

                                //1秒后，检查信号是否执行成功
                                checkMap.put("sendSignal",ID_KEY_SMART_LOCK);
                                checkMap.put("sendValue",VEHICLE.ON);
                                checkMsg.obj = checkMap;
                                checkMsg.what = ACTION_CHECKSIGNAL;
                                mUiHandler.sendMessageDelayed(checkMsg,checkSignalDelay);

                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_TWO,mContext,TtsConstant.MSGC94CONDITION, context.getString(R.string.msgC94),
                                        R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,R.string.condition_msgC94);
                            }
                            break;
                        case ACTION_IGNORED:
                        case ACTION_ALLOWED_TIMEOUT:
                            hide(type);
                            thread.quitSafely();
                            isAlreadyReceivedBroadcast = false;
                            Log.d(TAG,"One_TTS = " + One_TTS);
                            if (type == ActiveViewService.VIEW_ABNORMAL_WEATHER && One_TTS) {
                             //   TTSController.getInstance(context).startTTSOnly(mContext.getString(R.string.msg_takecare_secure));
                                One_TTS = false;
                                Log.e("zheng1","zheng1------------ One_TTS "+One_TTS);
                            } else if (type == ActiveViewService.VIEW_OIL_SHORTAGE && One_TTS) {
                                startTtsAndCallback(TtsConstant.MSGC12CONDITION, R.string.msg_oil_not_enough);
                                One_TTS = false;
                                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_oil,R.string.object_active_xuanzhe,TtsConstant.MSGC12CONDITION,R.string.condition_active5);
                            } else if (type == ActiveViewService.VIEW_ELEC_SHORTAGE && One_TTS) {
                                startTtsAndCallback(TtsConstant.MSGC17CONDITION, R.string.msg_to_battery_charging);
                                One_TTS = false;
                                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_dian,R.string.object_active_xuanzhe,TtsConstant.MSGC17CONDITION,R.string.condition_active5);
                            } else if (type == ActiveViewService.VIEW_IN_WORK_LINE && One_TTS) {
                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_ONE,mContext,TtsConstant.MSGC3CONDITION, context.getString(R.string.msg_call_me_if_need),
                                        R.string.skill_active,R.string.scene_active_gowork,R.string.object_active_xuanzhe,R.string.condition_active13);
                                One_TTS = false;
                            } else if (type == ActiveViewService.VIEW_OUT_WORK_LINE && One_TTS) {
                                getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_ONE,mContext,TtsConstant.MSGC6CONDITION, context.getString(R.string.msg_call_me_if_need),
                                        R.string.skill_active,R.string.scene_active_gohome,R.string.object_active_xuanzhe,R.string.condition_active13);
                                One_TTS = false;
                            } else if (type == ActiveViewService.VIEW_BIRTHDAY_WISHES && One_TTS) {
                                startTtsAndCallback(TtsConstant.MSGC3CONDITION, R.string.msg_call_me_if_need);
                                One_TTS = false;
                            } else if (type == ActiveViewService.VIEW_HOLIDAY_WISHES && One_TTS) {
                                startTtsAndCallback(TtsConstant.MSGC3CONDITION, R.string.msg_call_me_if_need);
                                One_TTS = false;
                            } else if (type == ActiveViewService.VIEW_HOLIDAY_WISHES && One_TTS) {
                                startTtsAndCallback(TtsConstant.MSGC29CONDITION, R.string.msg_to_repair);
                                One_TTS = false;
                                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_guzhang,R.string.object_active_xuanzhe,TtsConstant.MSGC29CONDITION,R.string.condition_active5);
                            } else if (type == ActiveViewService.VIEW_MORE_FAULT && One_TTS) {
                                startTtsAndCallback(TtsConstant.MSGC29CONDITION, R.string.msg_to_repair);
                                One_TTS = false;
                                Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_guzhang,R.string.object_active_xuanzhe,TtsConstant.MSGC29CONDITION,R.string.condition_active5);
                            }else if (type == ActiveViewService.VIEW_POPUP_NEWS && One_TTS) {
                                if(msg.obj == null){
                                    if(isClickButton){
                                        startTtsAndCallbackPriority(PriorityControler.PRIORITY_THREE,TtsConstant.MSGC50CONDITION,R.string.msg_call_me_if_need);
                                        DatastatManager.getInstance().recordUI_event(context, context.getResources().getString(R.string.event_id_wechatsendcar_cancel), "");
                                    }else {
                                        getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_THREE,mContext,TtsConstant.MSGC50CONDITION, context.getString(R.string.msg_call_me_if_need),
                                                R.string.skill_active,R.string.scene_active_weichat,R.string.object_active_xuanzhe,R.string.condition_active13);
                                    }
                                }
                                One_TTS = false;
                            }else if (type == ActiveViewService.VIEW_OS_SWND && One_TTS) {
                                if(msg.obj == null){
                                    getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_THREE,mContext,TtsConstant.MSGC53CONDITION, context.getString(R.string.msg_call_me_if_need),
                                            R.string.skill_active,R.string.scene_active_ossendcar,R.string.object_active_xuanzhe,R.string.condition_active13);
                                }
                                One_TTS = false;
                            }else if (type == ActiveViewService.VIEW_LOCK_UNLOCK_CLOSED && One_TTS) {//迎宾 闭锁解锁关闭
                                changeLockStatus(ID_KEY_SMART_LOCK, VEHICLE.OFF);
                                changeLockStatus(ID_KEY_SMART_UNLOCK, VEHICLE.OFF);
                                mUiHandler.sendEmptyMessageDelayed(ACTION_OPEN_CARCONTROL,checkCarControlDelay);

                                //1秒后，检查信号是否执行成功
                                checkMap.put("sendSignal",ID_KEY_SMART_LOCK);
                                checkMap.put("sendValue",VEHICLE.OFF);
                                checkMsg.obj = checkMap;
                                checkMsg.what = ACTION_CHECKSIGNAL;
                                mUiHandler.sendMessageDelayed(checkMsg,checkSignalDelay);

                                if(msg.obj == null){
                                    getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_TWO,mContext,TtsConstant.MSGC95CONDITION, context.getString(R.string.msgC95),
                                            R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,R.string.condition_msgC95);
                                }
                                One_TTS = false;
                            }else if ((type == ActiveViewService.VIEW_UNLOCK_CLOSED || type == ActiveViewService.VIEW_UNLOCK_CLOSED_SIGNAL_5) && One_TTS) {//迎宾 解锁关闭
                                changeLockStatus(ID_KEY_SMART_UNLOCK, VEHICLE.OFF);
                                mUiHandler.sendEmptyMessageDelayed(ACTION_OPEN_CARCONTROL,checkCarControlDelay);

                                //1秒后，检查信号是否执行成功
                                checkMap.put("sendSignal",ID_KEY_SMART_UNLOCK);
                                checkMap.put("sendValue",VEHICLE.OFF);
                                checkMsg.obj = checkMap;
                                checkMsg.what = ACTION_CHECKSIGNAL;
                                mUiHandler.sendMessageDelayed(checkMsg,checkSignalDelay);

                                if(msg.obj == null){
                                    getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_TWO,mContext,TtsConstant.MSGC95CONDITION, context.getString(R.string.msgC95),
                                            R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,R.string.condition_msgC95);
                                }
                                One_TTS = false;
                            }else if (type == ActiveViewService.VIEW_LOCK_CLOSED && One_TTS) {//迎宾 闭锁关闭
                                changeLockStatus(ID_KEY_SMART_LOCK, VEHICLE.OFF);
                                mUiHandler.sendEmptyMessageDelayed(ACTION_OPEN_CARCONTROL,checkCarControlDelay);

                                //1秒后，检查信号是否执行成功
                                checkMap.put("sendSignal",ID_KEY_SMART_LOCK);
                                checkMap.put("sendValue",VEHICLE.OFF);
                                checkMsg.obj = checkMap;
                                checkMsg.what = ACTION_CHECKSIGNAL;
                                mUiHandler.sendMessageDelayed(checkMsg,checkSignalDelay);

                                if(msg.obj == null){
                                    getMessageWithoutTtsSpeak(PriorityControler.PRIORITY_TWO,mContext,TtsConstant.MSGC95CONDITION, context.getString(R.string.msgC95),
                                            R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,R.string.condition_msgC95);
                                }
                                One_TTS = false;
                            }
                            break;
                        case ACTION_REFRESH_UI:
                            //tvDeny.setText(getTimeText("“关闭”", countDownTime));
                            setCancelText(type,countDownTime);
                            break;

                        case ACTION_VIEWONCLICK:

                            Intent intent = new Intent();
                            intent.setPackage(AppController.PACKAGE_NAME_NAVI);
                            intent.setAction(AppController.OPEN_VC_FROM_OTHERS);
                            intent.putExtra(AppController.EXTRA_KEY_OPEN_VC, AppController.EXTRA_OPEN_SETTING_VALUE_CT);
                            context.sendBroadcast(intent);

                            break;
                        case ACTION_CHECKSIGNAL:
                            Log.d(TAG,"receive check: " + checkTimes);
                            Map mMap = (Map)msg.obj;
                            int getValue = mMap.get("sendSignal").equals(ID_KEY_SMART_UNLOCK) ? CarUtils.getInstance(mContext).getunLockStatus() :
                                    CarUtils.getInstance(mContext).getLockStatus();
                            Log.d(TAG,"getValue = " + getValue + ",sendValue = " + (int)mMap.get("sendValue"));
                            if(checkTimes < 3 && getValue != (int)mMap.get("sendValue")){
                                checkTimes++;
                                Log.d(TAG,"check: " + checkTimes);

                                //再发送一遍
                                Message message = new Message();
                                checkMap.put("sendSignal",mMap.get("sendSignal"));
                                checkMap.put("sendValue",mMap.get("sendValue"));
                                message.obj = checkMap;
                                message.what = ACTION_CHECKSIGNAL;

                                mUiHandler.sendMessageDelayed(message,checkSignalDelay);
                            }else {
                                checkTimes = 0;
                            }
                            break;
                        case ACTION_OPEN_CARCONTROL:
                            CarController.getInstance(mContext).openCarController(CarController.EXTRA_OPEN_SETTING_VALUE_VC,CarController.OPEN_VC_TYPE_CARSETTINGS,TAG);
                            break;
                        default:
                            break;

                    }
                }
            };

            mHandlerTimer = new Handler(thread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Log.d(TAG, "mHandlerTimer:" + Thread.currentThread().getName());
                    if(ActiveServiceView_Show){
                        if (countDownTime > 0) {
                            countDownTime--;
                            mHandlerTimer.sendEmptyMessageDelayed(100, 1000);
                            mUiHandler.sendEmptyMessage(ACTION_REFRESH_UI);
                        } else {
                            //mUiHandler.sendEmptyMessage(ACTION_ALLOWED_TIMEOUT);
                            Log.d(TAG, "countTimer: One_TTS = " + One_TTS);
                            if(!One_TTS){
                                One_TTS = true;
                                Message message = new Message();
                                if(type == ActiveViewService.VIEW_ABNORMAL_WEATHER || type == ActiveViewService.VIEW_LOCK_UNLOCK_CLOSED ||
                                        type == ActiveViewService.VIEW_LOCK_CLOSED || type == ActiveViewService.VIEW_UNLOCK_CLOSED ||
                                        type == ActiveViewService.VIEW_UNLOCK_CLOSED_SIGNAL_5){//坏天气
                                    message.what = ACTION_IGNORED;
                                    message.obj = true;
                                }else {
                                    message.what = ACTION_ALLOWED;
                                    message.obj = true;
                                }
                                isClickButton = false;
                                mUiHandler.sendMessage(message);
                            }
                        }
                    }
                }
            };
            //开启倒计时
            mHandlerTimer.sendEmptyMessageDelayed(100, 1000);
        }

        public void setCancelText(int type,int seconds) {
            Log.d(TAG, "setCancelText: seconds = " + seconds);
            String text = "确定(" + seconds + "s)";
            if(type == ActiveViewService.VIEW_ABNORMAL_WEATHER  || type == ActiveViewService.VIEW_LOCK_UNLOCK_CLOSED ||
                    type == ActiveViewService.VIEW_LOCK_CLOSED || type == ActiveViewService.VIEW_UNLOCK_CLOSED ||
                    type == ActiveViewService.VIEW_UNLOCK_CLOSED_SIGNAL_5){
                text = "关闭(" + seconds + "s)";
            }
            Spannable WordtoSpan = new SpannableString(text);
            if (seconds >= 10) {
                WordtoSpan.setSpan(new AbsoluteSizeSpan(28), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                WordtoSpan.setSpan(new AbsoluteSizeSpan(20), 5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                WordtoSpan.setSpan(new AbsoluteSizeSpan(28), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                WordtoSpan.setSpan(new AbsoluteSizeSpan(20), 4, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if(type == ActiveViewService.VIEW_ABNORMAL_WEATHER || type == ActiveViewService.VIEW_LOCK_UNLOCK_CLOSED ||
                    type == ActiveViewService.VIEW_LOCK_CLOSED || type == ActiveViewService.VIEW_UNLOCK_CLOSED ||
                    type == ActiveViewService.VIEW_UNLOCK_CLOSED_SIGNAL_5){
                tvDeny.setText(WordtoSpan);
            }else {
                tvAllow.setText(WordtoSpan);
            }
        }


        private void startTtsAndCallback(String conditionId, int defaultTts) {
            Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String ttsText = tts;
                    if (TextUtils.isEmpty(tts)) {
                        ttsText = mContext.getString(defaultTts);
                    }
                    TTSController.getInstance(mContext).startTTSOnly(ttsText);
                }
            });
        }

        private void startTtsAndCallbackPriority(int priority,String conditionId, int defaultTts) {
            Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String ttsText = tts;
                    if (TextUtils.isEmpty(tts)) {
                        ttsText = mContext.getString(defaultTts);
                    }
                    TTSController.getInstance(mContext).startTTSOnly(ttsText,priority);
                }
            });
        }

        private void getMessageWithoutTtsSpeak(int priority,Context context, String conditionId,String defaultTTS,int appName,int scene,int object,int condition){
            Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String ttsText = tts;
                    if (TextUtils.isEmpty(tts)){
                        ttsText = defaultTTS;
                    }
                    Utils.eventTrack(mContext, appName,scene,object,conditionId, condition,ttsText);
                    TTSController.getInstance(mContext).startTTSOnly(ttsText,priority);
                }
            });
        }

//        private void uploadCallback(Bundle bundle) {
//            if (bundle == null) return;
//            String token = bundle.getString("track_token", "");
//            String taskId = bundle.getString("tash_id", "");
//            String json = bundle.getString("json", "");
//            ActiveServiceModel.getInstance().uploadCallback(mContext, token, taskId, json);
//
//            Log.e("zheng","zheng ------------: uploadCallback   "+token+taskId+json);
//        }

        private String getTimeText(String text, int count) {
            if (text != null && text.length() > 0) {
                int index = text.indexOf("(");
                if (index > 0) {
                    text = text.substring(0, index);
                    return (text + " (" + count + "秒)");
                } else {
                    return (text + " (" + count + "秒)");
                }
            }
            return "";
        }

        class PositiveOnclickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                isClickButton = true;
                mUiHandler.sendEmptyMessage(ACTION_ALLOWED);
            }
        }

        class NegativeOnclickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                //mUiHandler.sendEmptyMessage(ACTION_IGNORED);
                Log.d(TAG, "Negative onClick: One_TTS = " + One_TTS);
                if(!One_TTS){
                    One_TTS = true;
                    isClickButton = true;
                    mUiHandler.sendEmptyMessage(ACTION_IGNORED);
                }
            }
        }

        //打开车况详情界面
//        class CarDetailsOnclickListener implements View.OnClickListener {
//
//            @Override
//            public void onClick(View v) {
//                mUiHandler.sendEmptyMessage(ACTION_VIEWONCLICK);
//            }
//        }

        private BroadcastReceiver mActServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "receive broad: " + intent.getAction());
                Log.d(TAG, "receiver: isAlreadyReceivedBroadcast = " + isAlreadyReceivedBroadcast);
                if (!isAlreadyReceivedBroadcast && AppConstant.ACTION_INITIATIVE_START.equals(intent.getAction()) && ActiveServiceView_Show) {
                    Log.d(TAG, "receive ACTION_INITIATIVE_START");
                    isAlreadyReceivedBroadcast = true;
                    isClickButton = false;
                    mUiHandler.sendEmptyMessage(ACTION_ALLOWED);
                }
                if (!isAlreadyReceivedBroadcast && AppConstant.ACTION_INITIATIVE_SHUTDOWN.equals(intent.getAction()) && ActiveServiceView_Show) {
                    Log.d(TAG, "receive ACTION_INITIATIVE_SHUTDOWN" );
                    isAlreadyReceivedBroadcast = true;
                    One_TTS = true;
                    isClickButton = false;
                    mUiHandler.sendEmptyMessage(ACTION_IGNORED);
                }
            }

        };

        public void unregisterBroadcast(){
            if(mActServiceReceiver != null){
                mContext.unregisterReceiver(mActServiceReceiver);
            }
        }

    }


    /**
     * 导航去附近的长安汽车维修站、导航去充电站、导航去加油站
     * 根据关键字搜索
     *
     * @param extendApi
     * @param location
     * @param context
     */
    public void KeyWordSearch(IExtendApi extendApi, final String location, final Context context,String contidionid,String deftts) {

        extendApi.keywordSearch(location, new IExtendCallback<SearchResultModel>() {
            @Override
            public void success(SearchResultModel searchResultModel) {

                int size = searchResultModel.getResultList().size();

                Utils.getMessageWithoutTtsSpeak(context, contidionid, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        if(tts==null||"".equals(tts))
                            tts = deftts;
                        tts = tts.replace("#NUM#",size+"");
                        TTSController.getInstance(context).startTTSOnly(tts);
                    }
                });
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, TAG + "onFail:" + extendErrorModel.getErrorMessage());
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });


    }

    /**
     * 导航去回家、导航去公司
     * destType 0-回家  1-回公司
     * directNavi 0-进入导航;1-进入规划结果页
     */
    public void GoToWork_Home(IExtendApi extendApi, int destType) {


        extendApi.specialPoiNavi(destType, 1, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                //已执行成功

            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                //获取错误码
                int errorCode = extendErrorModel.getErrorCode();
                //获取错误码对应描述信息
                String errorMessage = extendErrorModel.getErrorMessage();
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });
    }

    /**
     * 沿途的加油站、充电站、长安维修站
     */
    private void SearchAlongRoute(IExtendApi extendApi, final String location, final Context context,String contidionid,String deftts) {
        extendApi.searchAlongRoute(location, new IExtendCallback<SearchResultModel>() {
            @Override
            public void success(SearchResultModel searchResultModel) {
                int size = searchResultModel.getResultList().size();
                Utils.getMessageWithoutTtsSpeak(context, contidionid, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        if(tts==null||"".equals(tts))
                            tts = deftts;
                        tts = tts.replace("#NUM#",size+"");
                        TTSController.getInstance(context).startTTSOnly(tts);
                    }
                });

            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d("test", "onFail:" + extendErrorModel.getErrorCode());
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }


    /**
     * 发送通知到通知中心显示
     *
     * @param context
     * @param icon
     * @param title
     * @param text
     */
    public void SendNotification(Context context, int icon, String title, String text ,boolean send) {
        Double latitude;
        Double longitude;
        if (send){//如果是微信、欧尚style 发送位置到车机
            latitude = ActiveViewService.popup_news_latitude;
            longitude = ActiveViewService.popup_news_longitude;
            Log.e(TAG,"SendNotification 微信、欧尚style 发送位置到车机"+latitude+longitude);
        }else {//如果是上下班位置
            latitude = ActiveServiceModel.InWorkLine_Lat;
            longitude = ActiveServiceModel.InWorkLine_Lon;
            Log.e(TAG,"SendNotification 如果是上下班位置到车机"+latitude+longitude);
        }
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("test_notification", "test_notification", NotificationManager.IMPORTANCE_HIGH);
        channel.setBypassDnd(true);    //设置绕过免打扰模式
        channel.canBypassDnd();       //检测是否绕过免打扰模式
        //channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上显示这条通知
        channel.setDescription("description of this notification");
        channel.setLightColor(Color.GREEN);
        channel.setName("name of this notification");
        channel.setShowBadge(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        channel.enableVibration(true);
        notifyManager.createNotificationChannel(channel);

        //获取NotificationManager实例
        //实例化NotificationCompat.Builde并设置相关属性
        StringBuffer data = new StringBuffer();
        data.append("{\"app_package\":\"com.chinatsp.ifly\",\"data\":{" +
                " \"lat\":" + latitude + ",\"lng\":" + longitude + "}}");

        Log.e(TAG,"SendNotification 微信、欧尚style 发送位置到车机:      "+latitude+longitude);
        Notification.Builder builder = new Notification.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                .setTicker(data)
                //设置小图标
                .setSmallIcon(icon)
                //设置通知标题
                .setContentTitle(title)
                .setChannelId("test_notification")
                //设置通知内容
                .setContentText(text);
        notifyManager.notify(1, builder.build());
    }

    /**
     * 回到桌面
     */
    public void GoToHomeLauncher(Context context){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);

        TAExtendManager.getInstance().getApi().setNaviScreen(new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                Log.d("test", "success");
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                Log.d("test", "onFail:" + extendErrorModel.getErrorCode());
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });
    }

    /**
     * 根据经纬度发起导航
     *
     */
    public void naviToPoi(Double Lon , Double Lat) {

        LocationInfo locationInfo = new LocationInfo();
        locationInfo.setName("");
        locationInfo.setAddress( "");
        locationInfo.setLatitude(Lat);
        locationInfo.setLongitude(Lon);

        LogUtils.d(TAG, "locationInfo:"+locationInfo.toString());
        TAExtendManager.getInstance().getApi().naviToPoi(locationInfo, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "naviToPoi：success");
            }

            @Override
            public void onFail(ExtendErrorModel errorModel) {
                LogUtils.d(TAG, "naviToPoi：error==" + errorModel.getErrorCode());

            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });
    }

    /**
     * 更改解闭锁状态
     */
    public void changeLockStatus(int propertyId,int status) {
        try {
            AppConfig.INSTANCE.mCarCabinManager.
                    setIntProperty(propertyId, VEHICLE_AREA_TYPE_GLOBAL, status);
            Log.i(TAG, "---------lh-----更改更改解闭锁状态：propertyId = " + propertyId + ",status = " + status);
        } catch (CarNotConnectedException e) {
            Log.i(TAG, "---------lh-----更改更改解闭锁状态CarNotConnectedException---" + e);
            e.printStackTrace();
        }
    }
}
