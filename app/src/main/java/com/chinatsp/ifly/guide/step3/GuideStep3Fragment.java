package com.chinatsp.ifly.guide.step3;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.car.CarNotConnectedException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.fail.GuideFailFragment;
import com.chinatsp.ifly.guide.step2.GuideStep2Fragment;
import com.chinatsp.ifly.guide.step4.GuideStep4Fragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.mvw.MvwSession;

import butterknife.BindView;
import butterknife.OnClick;

import static android.car.VehicleAreaSeat.HVAC_ALL;
import static android.car.hardware.constant.HVAC.HVAC_OFF;
import static android.car.hardware.constant.HVAC.HVAC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_POWER_ON;
import static android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat.ROW_1_LEFT;

public class GuideStep3Fragment extends BaseFragment implements GuideStep3Contract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    AnimationImageView ivFloatSmallViewAnim;
    TextView greetingTv;
    Button passBt;
    private GuideMainActivity activity;
    private GuideStep3Contract.Presenter presenter = new GuideStep3Presenter(this);
    public AnimationDrawable openAnimationDrawable;//说话;//说话
    public AnimationDrawable sayAnimationDrawable;//说话;//说话
    private String ttsText;
    private String displayText;
    private final int waitNextStepTime = 5000;
    private boolean isPassBtClicked = false;
    private BaseFragment mFragment;
    private final int DANCE_CHARACTER_2 = 12;
    private final int DANCE_CHARACTER_3 = 13;
    private final int DANCE_CHARACTER_4 = 14;
    private final int STEP3_1 = 31;
    private SpannableString spannableString;
    private final String TAG = "GuideStep3Fragment";

    public static GuideStep3Fragment newInstance(String dataList, int step) {
        GuideStep3Fragment fragment = new GuideStep3Fragment();
        Bundle bundle = new Bundle();
        bundle.putInt(GuideMainActivity.STEP, step);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (GuideMainActivity) context;
        presenter.bindActivity(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.subscribe();
        registBroadcast();
    }

    private void registBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.MY_TEST_BROADCAST_OPEN_AC);
        activity.registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "---action---" + action);
            if (AppConstant.MY_TEST_BROADCAST_OPEN_AC.equals(action)) {
                //"车控唤醒页"，成功
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_voice_car_control), "成功");
                Log.d(TAG, "\"打开空调\" 进行唤醒!");

                isPassBtClicked = true;
                try {
                    //先点亮文本，1秒后在提示通过
                    spannableString = new SpannableString(greetingTv.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 4,
                            5, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(DANCE_CHARACTER_2,100);
                } catch (Exception e) {
                    Log.d(TAG,"spannableString exception: " + e);
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if(!isPassBtClicked){
                        //if(passBt != null) passBt.setVisibility(View.GONE);
                        ttsText = activity.getString(R.string.greetingtts11);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 3,time = 1,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 3,time = 1,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(2,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case 2:
                    if(!isPassBtClicked){
                        //if(passBt != null) passBt.setVisibility(View.GONE);
                        ttsText = activity.getString(R.string.greetingtts11);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 3,time = 2,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 3,time = 2,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(3,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case 3:
                    if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_voice_car_control), "失败");
                    mFragment = GuideFailFragment.newInstance("",GuideMainActivity.STEP3);
                    ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case DANCE_CHARACTER_2:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 5,
                            6, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(DANCE_CHARACTER_3,100);
                    break;
                case DANCE_CHARACTER_3:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 6,
                            7, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(DANCE_CHARACTER_4,100);
                    break;
                case DANCE_CHARACTER_4:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 7,
                            8, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(STEP3_1,100);
                    break;
                case STEP3_1://to success
                    mFragment = GuideSuccessFragment.newInstance("",GuideMainActivity.STEP3);
                    ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case 4:
                    if(passBt != null) passBt.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @OnClick({R.id.pass_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.pass_bt:
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_voice_car_control), "跳过");
                isPassBtClicked = true;
                mFragment = GuideStep4Fragment.newInstance("",1);
                ActivityUtils.removeFragmentToActivity(activity.getSupportFragmentManager(), this, R.id.framelayout_content);
                ActivityUtils.addFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != GuideMainActivity.mContext)MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, Utils.getFromAssets(GuideMainActivity.mContext, "mvw_custom.json"));
        presenter.unSubscribe();
        handler.removeCallbacks(null);
        activity.unregisterReceiver(receiver);
    }

    @Override
    protected void initData() {
        closeAC();
//        if(null != GuideMainActivity.mContext)MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME, Utils.getFromAssets(GuideMainActivity.mContext, "mvw_custom_guide.json"));
        ttsText = activity.getString(R.string.greetingtts10);
        displayText = activity.getString(R.string.greeting41);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 3,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 3,time = 0,tts = " + ttsText);
                loadOpenAnimationResource();
                //handler.sendEmptyMessageDelayed(4,0);
                handler.sendEmptyMessageDelayed(1,waitNextStepTime);
            }
        });
        TiaoZiUtilCallback tiaoZiUtil = new TiaoZiUtilCallback(null, greetingTv, displayText, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
            @Override
            public void onPlayStopped() {
                handler.sendEmptyMessageDelayed(4,0);
            }
        });
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {
        starIv1 = (ImageView)view.findViewById(R.id.star_iv1);
        starIv2 = (ImageView)view.findViewById(R.id.star_iv2);
        starIv3 = (ImageView)view.findViewById(R.id.star_iv3);
        starIv4 = (ImageView)view.findViewById(R.id.star_iv4);
        ivFloatSmallViewAnim = (AnimationImageView) view.findViewById(R.id.iv_anim);
        greetingTv = (TextView)view.findViewById(R.id.greeting_tv);
        passBt = (Button)view.findViewById(R.id.pass_bt);

        Utils.setFullWindowSize(activity,true,false);
        passBt.setVisibility(View.GONE);
        setStarStatus();
        loadSayAnimationResource();
    }

    public void setStarStatus(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                starIv1.setBackgroundResource(R.mipmap.star_light);
                starIv2.setBackgroundResource(R.mipmap.star_light);
                starIv3.setBackgroundResource(R.mipmap.star_normal);
                starIv4.setBackgroundResource(R.mipmap.star_normal);
            }
        });
    }

    public void loadSayAnimationResource(){
        sayAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.say_guide_animation);
        //把AnimationDrawable设置为ImageView的背景
        ivFloatSmallViewAnim.setBackgroundDrawable(sayAnimationDrawable);

        sayAnimationDrawable.setOneShot(false);
        sayAnimationDrawable.start();
    }

    public void loadOpenAnimationResource(){
        openAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.open_guide_animation);
        //把AnimationDrawable设置为ImageView的背景
        ivFloatSmallViewAnim.setBackgroundDrawable(openAnimationDrawable);

        openAnimationDrawable.setOneShot(false);
        openAnimationDrawable.start();
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_step3;
    }

    public void closeAC(){
        //空调是否打开
        //boolean isOpen = isAirOpen();
        //if(!isOpen){
            sendBroadcastToACController();
            changeAirStatus(HVAC_OFF);
        //}
    }

    /**
     * 空调是否打开
     * @return
     */
    private boolean isAirOpen() {
        try {
            int HVAC_LEFT = (int)(ROW_1_LEFT | VehicleAreaSeat.ROW_2_LEFT |
                    VehicleAreaSeat.ROW_2_CENTER);
            int HVAC_RIGHT = (int)(VehicleAreaSeat.ROW_1_RIGHT | VehicleAreaSeat.ROW_2_RIGHT);
            int mHVAC_ALL = HVAC_LEFT | HVAC_RIGHT;

            Log.d(GuideMainActivity.TAG, "lh:get air status mHVAC_ALL = " + mHVAC_ALL);
            int status = AppConfig.INSTANCE.mCarHvacManager.getIntProperty(ID_HVAC_POWER_ON, mHVAC_ALL);
            Log.d(GuideMainActivity.TAG, "lh:get air status：" + status);
            if (status == HVAC_ON) {
                return true;
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更改空调状态
     */
    private void changeAirStatus(int status) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.
                    setIntProperty(ID_HVAC_POWER_ON, HVAC_ALL, status);
            Log.d(GuideMainActivity.TAG, "lh:change air status：" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
            Log.d(GuideMainActivity.TAG, "changeAirStatus Exception: " + e);
        }
    }

    private void sendBroadcastToACController(){
        activity.sendBroadcast(new Intent(AppConstant.VOICE_ACTION));
        Log.d(GuideMainActivity.TAG, "called sendBroadcastToACController....");
    }
}
