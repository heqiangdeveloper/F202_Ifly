package com.chinatsp.ifly.guide.success;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.car.CarNotConnectedException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.step2.GuideStep2Fragment;
import com.chinatsp.ifly.guide.step3.GuideStep3Fragment;
import com.chinatsp.ifly.guide.step3.GuideStep3_1Fragment;
import com.chinatsp.ifly.guide.step4.GuideStep4_2Fragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;

import static android.car.VehicleAreaSeat.HVAC_ALL;
import static android.car.hardware.constant.HVAC.HVAC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_POWER_ON;
import static android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat.ROW_1_LEFT;

public class GuideSuccessFragment extends BaseFragment implements GuideSuccessContract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    TextView greetingTv1;
    TextView greetingTv2;
    AnimationImageView ivFloatSmallViewAnim;
    AnimationImageView step1_fire_Iv1;
    AnimationImageView step1_fire_Iv2;
    AnimationImageView step2_fire_Iv1;
    AnimationImageView step2_fire_Iv2;
    AnimationImageView step2_fire_Iv3;
    AnimationImageView step3_fire_Iv1;
    AnimationImageView step3_fire_Iv2;
    AnimationImageView step3_fire_Iv3;
    AnimationImageView step4_fire_Iv1;
    AnimationImageView step4_fire_Iv2;
    AnimationImageView step4_fire_Iv3;

//    public AnimationDrawable animationDrawable1;//新手引导--红色烟花
//    public AnimationDrawable animationDrawable2;//新手引导--橙色烟花
//    public AnimationDrawable animationDrawable3;//新手引导--绿色烟花
//    public AnimationDrawable animationDrawable4;//新手引导--金色烟花
    private GuideMainActivity activity;
    private GuideSuccessContract.Presenter presenter = new GuideSuccessPresenter(this);
    public AnimationDrawable sayAnimationDrawable;//说话
    private String ttsText;
    private String displayText;
    private String displayText1;
    private String displayText2;
    private int step = 1;
    private BaseFragment mFragment;
    private TiaoZiUtilCallback tiaoZiUtilCallback;

    public static GuideSuccessFragment newInstance(String dataList, int step) {
        GuideSuccessFragment fragment = new GuideSuccessFragment();
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
        //EventBus.getDefault().register(this);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GuideMainActivity.STEP1:
                    //to Step2
                    mFragment = GuideStep2Fragment.newInstance("",GuideMainActivity.STEP1);
                    ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case GuideMainActivity.STEP2:
                    //to step3
                    mFragment = GuideStep3Fragment.newInstance("",GuideMainActivity.STEP2);
                    ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case GuideMainActivity.STEP3:
                    //to step3_1
                    mFragment = GuideStep3_1Fragment.newInstance("",GuideMainActivity.STEP3);
                    ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case GuideMainActivity.STEP4:
                    lightStar(starIv4,true);
                    handler.sendEmptyMessageDelayed(1000,1000);
                    break;
                case 1000:
                    mFragment = GuideStep4_2Fragment.newInstance("",GuideMainActivity.STEP4);
                    ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case 1001:
                    tiaoZiUtilCallback = new TiaoZiUtilCallback(null,greetingTv2, displayText2, 50, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
                        @Override
                        public void onPlayStopped() {

                        }
                    });
                case 2001:
                    setAllFiresGone();
                    lightStar(starIv1,true);
                    lightFireworks(step1_fire_Iv1,GuideMainActivity.animationDrawable2);
                    lightFireworks(step1_fire_Iv2,GuideMainActivity.animationDrawable4);
                    break;
                case 2002:
                    setAllFiresGone();
                    lightStar(starIv2,true);
                    lightFireworks(step2_fire_Iv1,GuideMainActivity.animationDrawable2);
                    lightFireworks(step2_fire_Iv2,GuideMainActivity.animationDrawable1);
                    lightFireworks(step2_fire_Iv3,GuideMainActivity.animationDrawable3);
                    break;
                case 2003:
                    setAllFiresGone();
                    lightStar(starIv3,true);
                    lightFireworks(step3_fire_Iv1,GuideMainActivity.animationDrawable4);
                    lightFireworks(step3_fire_Iv2,GuideMainActivity.animationDrawable3);
                    lightFireworks(step3_fire_Iv3,GuideMainActivity.animationDrawable1);
                    break;
                case 2004:
                    setAllFiresGone();
                    lightStar(starIv4,true);
                    lightFireworks(step4_fire_Iv1,GuideMainActivity.animationDrawable4);
                    lightFireworks(step4_fire_Iv2,GuideMainActivity.animationDrawable3);
                    lightFireworks(step4_fire_Iv3,GuideMainActivity.animationDrawable2);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
        handler.removeCallbacks(null);
    }

    @Override
    protected void initData() {
        //把AnimationDrawable设置为ImageView的背景
        lightStar(starIv1,false);
        lightStar(starIv2,false);
        lightStar(starIv3,false);
        lightStar(starIv4,false);
        ivFloatSmallViewAnim.setBackgroundDrawable(sayAnimationDrawable);
        sayAnimationDrawable.start();
        step = getArguments().getInt(GuideMainActivity.STEP);
        if(step == GuideMainActivity.STEP1){
            ttsText = activity.getString(R.string.greetingtts5);
            displayText = activity.getString(R.string.greeting23);
            displayText1 = displayText.substring(0,5);
            displayText2 = displayText.substring(5);
        }else if(step == GuideMainActivity.STEP2){
            lightStar(starIv1,true);
            ttsText = activity.getString(R.string.greetingtts9);
            displayText = activity.getString(R.string.greeting33);
            displayText1 = displayText.substring(0,5);
            displayText2 = displayText.substring(5);
        }else if(step == GuideMainActivity.STEP3){
            lightStar(starIv1,true);
            lightStar(starIv2,true);
            ttsText = activity.getString(R.string.greetingtts12);
            displayText = activity.getString(R.string.greeting42);
            displayText1 = displayText.substring(0,5);
            displayText2 = displayText.substring(5);
            openAC();
        }else if(step == GuideMainActivity.STEP4){
            lightStar(starIv1,true);
            lightStar(starIv2,true);
            lightStar(starIv3,true);
            ttsText = activity.getString(R.string.greetingtts19);
            displayText = activity.getString(R.string.greeting55);
            displayText1 = displayText;
            displayText2 = displayText;
            greetingTv2.setVisibility(View.GONE);
        }

        Log.d(GuideMainActivity.TAG, "onPlayStart: step = success," + "successStep = " + step + ",time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = success," + "successStep = " + step + ",time = 0,tts = " + ttsText);
                handler.sendEmptyMessageDelayed(step,0);
            }
        });
        Log.d("heqiang", "greeting1 =" + greetingTv1);
        tiaoZiUtilCallback = new TiaoZiUtilCallback(null,greetingTv1, displayText1, 50, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
            @Override
            public void onPlayStopped() {
                switch (step)
                {
                    case GuideMainActivity.STEP1:
                        handler.sendEmptyMessageDelayed(2001,0);
                        break;
                    case GuideMainActivity.STEP2:
                        handler.sendEmptyMessageDelayed(2002,0);
                        break;
                    case GuideMainActivity.STEP3:
                        handler.sendEmptyMessageDelayed(2003,0);
                        break;
                    case GuideMainActivity.STEP4:
                        handler.sendEmptyMessageDelayed(2004,0);
                        break;
                }
                //非第4步，还要显示第2行文字
                if(step != GuideMainActivity.STEP4) {
                    handler.sendEmptyMessageDelayed(1001, 100);
                }
            }
        });
    }

    public void lightStar(ImageView iv,boolean isLight) {
        if(isLight){
            iv.setBackgroundResource(R.mipmap.star_light);
        }else {
            iv.setBackgroundResource(R.mipmap.star_normal);
        }

    }

    public void setAllFiresGone() {
        step1_fire_Iv1.setVisibility(View.GONE);
        step1_fire_Iv2.setVisibility(View.GONE);
        step2_fire_Iv1.setVisibility(View.GONE);
        step2_fire_Iv2.setVisibility(View.GONE);
        step2_fire_Iv3.setVisibility(View.GONE);
        step3_fire_Iv1.setVisibility(View.GONE);
        step3_fire_Iv2.setVisibility(View.GONE);
        step3_fire_Iv3.setVisibility(View.GONE);
        step4_fire_Iv1.setVisibility(View.GONE);
        step4_fire_Iv2.setVisibility(View.GONE);
        step4_fire_Iv3.setVisibility(View.GONE);
    }

    public void lightFireworks(AnimationImageView iv,AnimationDrawable animationDrawable) {
//        if(null == iv)
//        {
//            return;
//        }
        iv.setVisibility(View.VISIBLE);
        iv.loadAnimation(animationDrawable,null);
    }

    public void openAC(){
        //空调是否打开
        boolean isOpen = isAirOpen();
        if(!isOpen){
            sendBroadcastToACController();
            changeAirStatus(HVAC_ON);
        }
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

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {
        starIv1 = (ImageView)view.findViewById(R.id.star_iv1);
        starIv2 = (ImageView)view.findViewById(R.id.star_iv2);
        starIv3 = (ImageView)view.findViewById(R.id.star_iv3);
        starIv4 = (ImageView)view.findViewById(R.id.star_iv4);
        greetingTv1  = (TextView)view.findViewById(R.id.greeting_tv1);
        greetingTv2  = (TextView)view.findViewById(R.id.greeting_tv2);
        ivFloatSmallViewAnim  = (AnimationImageView)view.findViewById(R.id.iv_anim);
        step1_fire_Iv1  = (AnimationImageView)view.findViewById(R.id.step1_fire_iv1);
        step1_fire_Iv2  = (AnimationImageView)view.findViewById(R.id.step1_fire_iv2);
        step2_fire_Iv1  = (AnimationImageView)view.findViewById(R.id.step2_fire_iv1);
        step2_fire_Iv2  = (AnimationImageView)view.findViewById(R.id.step2_fire_iv2);
        step2_fire_Iv3  = (AnimationImageView)view.findViewById(R.id.step2_fire_iv3);
        step3_fire_Iv1  = (AnimationImageView)view.findViewById(R.id.step3_fire_iv1);
        step3_fire_Iv2  = (AnimationImageView)view.findViewById(R.id.step3_fire_iv2);
        step3_fire_Iv3  = (AnimationImageView)view.findViewById(R.id.step3_fire_iv3);
        step4_fire_Iv1  = (AnimationImageView)view.findViewById(R.id.step4_fire_iv1);
        step4_fire_Iv2  = (AnimationImageView)view.findViewById(R.id.step4_fire_iv2);
        step4_fire_Iv3  = (AnimationImageView)view.findViewById(R.id.step4_fire_iv3);

        Utils.setFullWindowSize(activity,true,false);
        initAnimationResource();
    }

    public void initAnimationResource(){
        sayAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.say_guide_animation);
        sayAnimationDrawable.setOneShot(false);
        //sayAnimationDrawable.start();移至initData()中，否则会造成精灵卡顿
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_success;
    }
}
