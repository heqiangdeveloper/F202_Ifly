package com.chinatsp.ifly.guide.step3;

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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.fail.GuideFailFragment;
import com.chinatsp.ifly.guide.step4.GuideStep4Fragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideStep3_1Fragment extends BaseFragment implements GuideStep3_1Contract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    AnimationImageView ivFloatSmallViewAnim;
    TextView greetingTv1;
    TextView greetingTv2;
    TextView greetingTv3;
    private SpannableString spannableString;
    private GuideMainActivity activity;
    private GuideStep3_1Contract.Presenter presenter = new GuideStep3_1Presenter(this);
    public AnimationDrawable openAnimationDrawable;//说话;//说话
    public AnimationDrawable sayAnimationDrawable;//说话;//说话
    private String ttsText;
    private final int waitNextStepTime = 5000;
    private BaseFragment mFragment;
    private String displayText;
    private String displayText1;
    private String displayText2;
    private String displayText3;
    private TiaoZiUtil tiaoZiUtil;

    public static GuideStep3_1Fragment newInstance(String dataList, int step) {
        GuideStep3_1Fragment fragment = new GuideStep3_1Fragment();
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
                    spannableString = new SpannableString(greetingTv1.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 7,
                            greetingTv1.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    greetingTv1.setText(spannableString);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv2, displayText2, GuideMainActivity.TIAOZIDURATION,new TiaoZiUtil.OnTiaoZiStoppedListener(){
                        @Override
                        public void onPlayStopped() {
                            handler.sendEmptyMessageDelayed(2,0);
                        }
                    });
                    break;
                case 2:
                    spannableString = new SpannableString(greetingTv2.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 0,
                            7, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    greetingTv2.setText(spannableString);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv3, displayText3, GuideMainActivity.TIAOZIDURATION,new TiaoZiUtil.OnTiaoZiStoppedListener(){
                        @Override
                        public void onPlayStopped() {
                           // handler.sendEmptyMessageDelayed(3,0);
                        }
                    });
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
        ttsText = activity.getString(R.string.greetingtts13);
        displayText = activity.getString(R.string.greeting43);
        displayText1 = displayText.substring(0,17);
        displayText2 = displayText.substring(17,27);
        displayText3 = displayText.substring(27);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 3_1,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 3_1,time = 0,tts = " + ttsText);
                mFragment = GuideStep4Fragment.newInstance("",GuideMainActivity.STEP3);
                ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
            }
        });

        tiaoZiUtil = new TiaoZiUtil(greetingTv1, displayText1, GuideMainActivity.TIAOZIDURATION,new TiaoZiUtil.OnTiaoZiStoppedListener(){
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
        greetingTv3 = (TextView)view.findViewById(R.id.greeting_tv3);

        Utils.setFullWindowSize(activity,true,false);
        setStarStatus();
        loadSayAnimationResource();
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

    public void loadSayAnimationResource(){
        sayAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.say_guide_animation);
        //把AnimationDrawable设置为ImageView的背景
        ivFloatSmallViewAnim.setBackgroundDrawable(sayAnimationDrawable);

        sayAnimationDrawable.setOneShot(false);
        sayAnimationDrawable.start();
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_step3_1;
    }
}
