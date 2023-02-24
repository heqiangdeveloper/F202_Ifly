package com.chinatsp.ifly.guide.step1;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.chinatsp.ifly.guide.step3.GuideStep3Fragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.guide.welcome.GuideWelcomeContract;
import com.chinatsp.ifly.guide.welcome.GuideWelcomePresenter;
import com.chinatsp.ifly.module.stock.StockContract;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideStep1Fragment extends BaseFragment implements GuideStep1Contract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    TextView greetingTv;
    ImageView btnIv;
    Button passBt;
    ImageView maskIv;
    private GuideMainActivity activity;
    private GuideStep1Contract.Presenter presenter = new GuideStep1Presenter(this);
    public AnimationDrawable sayAnimationDrawable;//说话
    private String ttsText;
    private String displayText;
    private ObjectAnimator objectAnimator;
    private int times = 0;
    private final int waitNextStepTime = 5000;
    private BaseFragment mFragment;
    private final String TAG = "GuideEndFragment";
    private boolean isPassBtClicked = false;

    public static GuideStep1Fragment newInstance(String dataList, int step) {
        GuideStep1Fragment fragment = new GuideStep1Fragment();
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
        filter.addAction(AppConstant.ACTION_AWARE_VOICE_KEY);
        activity.registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "---action---" + action);
            if (AppConstant.ACTION_AWARE_VOICE_KEY.equals(action)) {
                Bundle bundle = intent.getExtras();
                String state = bundle.getString("Key_state");
                String code = bundle.getString("Key_code");
                Log.d(TAG, "state = " + state + ", code = " + code);
                if (state.equals("DOWN") && code.equals("VR")) {
                    //"方向盘唤醒页"，成功
                    Log.d(TAG, "step1 onReceive: Thread.currentThread().getName() :" + Thread.currentThread().getName());
                    if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_VR), "成功");
                    Log.d(TAG, "VR is clicked!");

                    isPassBtClicked = true;
                    mFragment = GuideSuccessFragment.newInstance("",GuideMainActivity.STEP1);
                    ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
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
                        ttsText = activity.getString(R.string.greetingtts4);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 1,time = 1,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 1,time = 1,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(2,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case 2:
                    if(!isPassBtClicked){
                        //if(passBt != null) passBt.setVisibility(View.GONE);
                        ttsText = activity.getString(R.string.greetingtts4);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 1,time = 2,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 1,time = 2,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(3,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case 3:
                    if(null != GuideMainActivity.mContext) DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_VR), "失败");
                    mFragment = GuideFailFragment.newInstance("",GuideMainActivity.STEP1);
                    ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case 4:
                    initAnimationResource();
                    break;
                case 5:
                    if(passBt != null) passBt.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    if(maskIv != null) maskIv.setVisibility(View.VISIBLE);
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
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_VR), "跳过");
                isPassBtClicked = true;
                mFragment = GuideStep2Fragment.newInstance("",1);
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
        presenter.unSubscribe();
        handler.removeCallbacks(null);
        activity.unregisterReceiver(receiver);
    }

    @Override
    protected void initData() {
        setStarStatus();
        ttsText = activity.getString(R.string.greetingtts3);
        displayText = activity.getString(R.string.greeting21);
        passBt.setVisibility(View.GONE);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 1,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 1,time = 0,tts = " + ttsText);
                //handler.sendEmptyMessageDelayed(5,0);
                handler.sendEmptyMessageDelayed(1,waitNextStepTime);
            }
        });
        TiaoZiUtilCallback tiaoZiUtil = new TiaoZiUtilCallback(null,greetingTv, displayText, GuideMainActivity.TIAOZIDURATION,
                new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
            @Override
            public void onPlayStopped() {
                handler.sendEmptyMessageDelayed(6,0);//蒙版
                handler.sendEmptyMessageDelayed(5,100);//跳过
                handler.sendEmptyMessageDelayed(4,500);//显示唤醒按键
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
        greetingTv = (TextView)view.findViewById(R.id.greeting_tv);
        btnIv = (ImageView)view.findViewById(R.id.btn_iv);
        passBt = (Button)view.findViewById(R.id.pass_bt);
        maskIv = (ImageView)view.findViewById(R.id.mask_iv);

        Utils.setFullWindowSize(activity,true,false);
        //passBt.setVisibility(View.GONE);
        //setStarStatus();
    }

    public void initAnimationResource(){
        //语音按钮  动画
        btnIv.setVisibility(View.VISIBLE);
        objectAnimator = ObjectAnimator.ofFloat(btnIv, "alpha", 0f, 1f);
        objectAnimator.setDuration(1000);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);//
        objectAnimator.start();
    }

    public void setStarStatus(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                starIv1.setBackgroundResource(R.mipmap.star_normal);
                starIv2.setBackgroundResource(R.mipmap.star_normal);
                starIv3.setBackgroundResource(R.mipmap.star_normal);
                starIv4.setBackgroundResource(R.mipmap.star_normal);
            }
        });
    }
    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_step1;
    }
}
