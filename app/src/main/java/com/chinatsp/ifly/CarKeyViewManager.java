package com.chinatsp.ifly;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chinatsp.ifly.utils.LogUtils;

public class CarKeyViewManager {
    private final String TAG = "CarKeyViewManager";
    private final static int MSG_HIDE_KEY = 101;

    private int displayWidth;
    private int displayHeight;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_HIDE_KEY:
                    // 隐藏按键提示框
                    hideKeyTipView();
                    break;
            }
        }
    };

    public enum ActionType {
        ACTION_TYPE_ENGINE, ACTION_TYPE_KEY
    }

    // 底部显示的提示的按键类型
    public enum KeyType {
        KEY_CRUISE_MAIN, KEY_SRC,KEY_MUTE,KEY_VOLUP,KEY_VOLDOWN,KEY_VR,KEY_TEL,KEY_HANGUP,KEY_ESC,KEY_METER_PAGE_LAST,KEY_METER_PAGE_NEXT,KEY_METER_CONFIRM,KEY_METER_CHANGE,KEY_TRUNK,KEY_TEMP_CHANGE,KEY_DEFROST_FRONT,KEY_DEFROST_BACK,KEY_AIR_CONDITION_OFF,KEY_DANGER_BTN,KEY_AIR_CONDITION_AUTO,KEY_WIND_MODE,KEY_AIR_CYCLE_MODE,KEY_AC_MAX,KEY_WIND_CHANGE,KEY_CAMERA,KEY_CAMERA_EMERGE,KEY_SOS,KEY_SCUTTLE_SWITCH_ON,KEY_SCUTTLE_SWITCH_OFF,KEY_ABAT_VENT_SWITCH_ON,KEY_ABAT_VENT_SWITCH_OFF,KEY_DOOR_LOCK,KEY_WINDOW_LOCK,KEY_DRIVE_CHANGE,KEY_VIEW_OVERALL,KEY_LIGHT,KEY_WIPER,KEY_ELEC_PARKING,KEY_AC_SWITH,KEY_MAIN_DRIVE_FALL,KEY_OTHER_DRIVE_FALL,KEY_CRUISE_CANCEL,KEY_AUTO_PARK,KEY_AUTO_HOLD

    }

    private static CarKeyViewManager carViewManager;
    private Context mContext;
    private WindowManager winManager;

    private KeyTipView keyTipView;
    private ActionTipView actionTipView;

    private CarKeyViewManager(Context context) {
        this.mContext = context.getApplicationContext();
        winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        displayWidth = winManager.getDefaultDisplay().getWidth();
        displayHeight = winManager.getDefaultDisplay().getHeight();
    }

    public static synchronized CarKeyViewManager getInstance(Context context) {
        if (carViewManager == null) {
            carViewManager = new CarKeyViewManager(context);
        }
        return carViewManager;
    }

    /**
     * 显示行为提示悬浮窗
     */
    public void showActionTipView(ActionType actionType) {
        if (actionTipView == null) {
            actionTipView = new ActionTipView(mContext);
        }
        actionTipView.updateTipView(actionType);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.START|Gravity.TOP;
        params.width = 720;
        params.height = 490;
        params.x = 350;
        params.y = 115;
        if (actionTipView.getParent() == null) {
            LogUtils.d(TAG, "addView");
            winManager.addView(actionTipView, params);
        }
    }

    /**
     * 关闭行为提示悬浮窗
     */
    public void hideActionTipView() {
        if (actionTipView != null) {
            //关闭唤醒动画
            winManager.removeView(actionTipView);
            actionTipView = null;
        }
    }

    public void updateTimeView(int time) {
        if (actionTipView != null) {
            actionTipView.updateTimeView(time);
        }
    }

    public void updateTipView(ActionType actionType) {
        if (actionTipView != null) {
            actionTipView.updateTipView(actionType);
        }
    }

    /**
     * 显示按键提示悬浮窗
     */
    public void showKeyTipView(KeyType keyType) {
        if (keyType == null) return;
        if (keyTipView == null) {
            keyTipView = new KeyTipView(mContext);
        }
        Log.d(TAG,"lh:keyType:"+keyType);
        keyTipView.updateTipView(keyType);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.START | Gravity.TOP;
//        params.width = 324;
        params.width = 389;
        params.height = 224;
        params.x = 548;
        params.y = 248;
        if (keyTipView.getParent() == null) {
            LogUtils.d(TAG, "addView");
            winManager.addView(keyTipView, params);
        }
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_KEY, 2 * 1000);
    }

    /**
     * 关闭按键提示悬浮窗
     */
    public void hideKeyTipView() {
        if (keyTipView != null) {
            winManager.removeView(keyTipView);
            keyTipView = null;
        }
    }


    /**
     * 启动发动机和按下按键的提示view
     */
    class ActionTipView extends LinearLayout implements View.OnClickListener {

        private TextView top_tip_tv;
        private ImageView center_iv;
        private TextView bot_tip_tv;
        private ImageView ivExit;

        public ActionTipView(final Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.layout_car_tip_action, this);

            top_tip_tv = view.findViewById(R.id.top_tip_tv);
            center_iv = view.findViewById(R.id.center_iv);
            bot_tip_tv = view.findViewById(R.id.bot_tip_tv);
            ivExit = view.findViewById(R.id.iv_close);
            ivExit.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_close:
                    hideActionTipView();
                    break;
            }
        }

        public void updateTimeView(int time) {
            if (bot_tip_tv != null) {
                bot_tip_tv.setText(mContext.getString(R.string.key_query_tip_countdown, String.valueOf(time)));
            }
        }

        public void updateTipView(ActionType actionType) {
            switch (actionType) {
                case ACTION_TYPE_KEY:
                    top_tip_tv.setText("请按下你要查询的按键");
                    center_iv.setImageResource(R.drawable.key_down);
                    break;
                case ACTION_TYPE_ENGINE:
                    top_tip_tv.setText("请启动发动机");
                    center_iv.setImageResource(R.drawable.key_engine);

                    break;
            }
        }
    }

    /**
     * 按键提示view
     */
    class KeyTipView extends LinearLayout {
        private ImageView keyIv;
        private TextView keyTv;

        public KeyTipView(final Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.layout_car_tip_key, this);

            keyIv = view.findViewById(R.id.key_iv);
            keyTv = view.findViewById(R.id.key_tv);
        }

        public void updateTipView(KeyType keyType) {
            switch (keyType) {
                case KEY_CRUISE_MAIN:
                    // 定速巡航主开关
                    keyTv.setText("定速巡航开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_SRC:
                    // 媒体切换
                    keyTv.setText("媒体切换按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_MUTE:
                    // 静音
                    keyTv.setText("音量静音按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_VOLDOWN:
                    // 音量-
                    keyTv.setText("音量降低按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_VR:
                    // 语音
                    keyTv.setText("语音按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_VOLUP:
                    // 音量+
                    keyTv.setText("音量调高按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_TEL:
                    // 接电话/上一曲
                    keyTv.setText("电话接听或切换下一首按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_HANGUP:
                    // 挂电话/下一曲
                    keyTv.setText("电话挂断或切换上一首按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_AC_MAX:
                    // 最大制冷
                    keyTv.setText("最大制冷");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_ESC:
                    // 电子稳定系统
                    keyTv.setText("电子稳定系统");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_METER_PAGE_LAST:
                    // 仪表上翻页
                    keyTv.setText("仪表上翻页");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_METER_CONFIRM:
                    // 仪表确定
                    keyTv.setText("仪表确定");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_METER_CHANGE:
                    // 仪表页面切换
                    keyTv.setText("仪表页面切换");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_TRUNK:
                    // 后备箱
                    keyTv.setText("后备箱开启");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_TEMP_CHANGE:
                    // 温度调节面板
                    keyTv.setText("温度调节面板");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_DEFROST_FRONT:
                    // 前除霜开关
                    keyTv.setText("前除霜开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_DEFROST_BACK:
                    // 后除霜开关
                    keyTv.setText("后除霜开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_AIR_CONDITION_OFF:
                    // 空调关闭按键
                    keyTv.setText("空调关闭按键");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_DANGER_BTN:
                    // 紧急报警开关
                    keyTv.setText("紧急报警开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_AIR_CONDITION_AUTO:
                    // 自动空调开关
                    keyTv.setText("自动空调开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_WIND_MODE:
                    // 空调吹风模式切换
                    keyTv.setText("空调吹风模式切换");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_AIR_CYCLE_MODE:
                    // 空调循环模式切换
                    keyTv.setText("空调循环模式切换");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_WIND_CHANGE:
                    // 空调风量调节
                    keyTv.setText("空调风量调节");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_CAMERA:
                    // 行车记录仪拍照
                    keyTv.setText("行车记录仪拍照");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_CAMERA_EMERGE:
                    // 紧急录制
                    keyTv.setText("紧急录制");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_SOS:
                    // 紧急求助
                    keyTv.setText("紧急求助");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_SCUTTLE_SWITCH_ON:
                    // 天窗开启
                    keyTv.setText("天窗开启");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_SCUTTLE_SWITCH_OFF:
                    // 天窗关闭
                    keyTv.setText("天窗关闭");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_ABAT_VENT_SWITCH_ON:
                    // 遮阳帘开启
                    keyTv.setText("遮阳帘开启");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_ABAT_VENT_SWITCH_OFF:
                    // 遮阳帘关闭
                    keyTv.setText("遮阳帘关闭");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_DOOR_LOCK:
                    // 车门解闭锁
                    keyTv.setText("车门解闭锁");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_WINDOW_LOCK:
                    // 车窗锁止
                    keyTv.setText("车窗锁止");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_DRIVE_CHANGE:
                    // 驾驶模式切换
                    keyTv.setText("驾驶模式切换");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_VIEW_OVERALL:
                    // 全景影像开关
                    keyTv.setText("全景影像开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_LIGHT:
                    // 灯光调节手柄
                    keyTv.setText("灯光调节手柄");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_WIPER:
                    // 雨刮调节手柄
                    keyTv.setText("雨刮调节手柄");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_ELEC_PARKING:
                    // 电子驻车开关
                    keyTv.setText("电子驻车开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_AC_SWITH:
                    // 空调制冷开关
                    keyTv.setText("空调制冷开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_MAIN_DRIVE_FALL:
                    // 主驾车窗一键升降
                    keyTv.setText("主驾车窗一键升降");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_OTHER_DRIVE_FALL:
                    // 车门门窗升降
                    keyTv.setText("车门门窗升降");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_CRUISE_CANCEL:
                    // 巡航取消
                    keyTv.setText("巡航取消");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_AUTO_PARK:
                    // 自动泊车系统开关
                    keyTv.setText("自动泊车系统开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                case KEY_AUTO_HOLD:
                    // 自动驻车开关
                    keyTv.setText("自动驻车开关");
                    //TODO 更新按键图标
                    keyIv.setImageResource(R.drawable.key_src);
                    break;
                    default:
                        break;
            }

        }
    }
}
