package com.chinatsp.ifly.guide.step4;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
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
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;

public class GuideStep4Fragment extends BaseFragment implements GuideStep4Contract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    AnimationImageView ivFloatSmallViewAnim;
    TextView greetingTv1;
    TextView greetingTv2;
    private GuideMainActivity activity;
    private GuideStep4Contract.Presenter presenter = new GuideStep4Presenter(this);
    public AnimationDrawable sayAnimationDrawable;//说话
    private String ttsText;
    private String displayText;
    private String displayText1;
    private String displayText2;
    private final int waitNextStepTime = 5000;
    private TiaoZiUtilCallback tiaoZiUtilCallback;
    private BaseFragment mFragment;

    public static GuideStep4Fragment newInstance(String dataList, int step) {
        GuideStep4Fragment fragment = new GuideStep4Fragment();
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
                case 1:
                    tiaoZiUtilCallback = new TiaoZiUtilCallback(null,greetingTv2, displayText2, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
                        @Override
                        public void onPlayStopped() {

                        }
                    });
                    break;
                case 2:
                    mFragment = GuideStep4_1Fragment.newInstance("",GuideMainActivity.STEP3);
                    ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
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
        ttsText = activity.getString(R.string.greetingtts14);
        displayText = activity.getString(R.string.greeting51);
        displayText1 = displayText.substring(0,4);
        displayText2 = displayText.substring(4);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 4,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 4,time = 0,tts = " + ttsText);
                handler.sendEmptyMessageDelayed(2,100);
            }
        });
        tiaoZiUtilCallback = new TiaoZiUtilCallback(null,greetingTv1, displayText1, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
            @Override
            public void onPlayStopped() {
                handler.sendEmptyMessageDelayed(1,0);
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
        greetingTv1 = (TextView)view.findViewById(R.id.greeting_tv1);
        greetingTv2 = (TextView)view.findViewById(R.id.greeting_tv2);

        Utils.setFullWindowSize(activity,false,true);
        setStarStatus();
        initAnimationResource();
    }

    public void setStarStatus(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                starIv1.setBackgroundResource(R.mipmap.star_light);
                starIv2.setBackgroundResource(R.mipmap.star_light);
                starIv3.setBackgroundResource(R.mipmap.star_light);
                starIv4.setBackgroundResource(R.mipmap.star_normal);
            }
        });
    }

    public void initAnimationResource(){
        sayAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.say_guide_animation);
        //把AnimationDrawable设置为ImageView的背景
        ivFloatSmallViewAnim.setBackgroundDrawable(sayAnimationDrawable);

        sayAnimationDrawable.setOneShot(false);
        sayAnimationDrawable.start();
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_step4;
    }
}
