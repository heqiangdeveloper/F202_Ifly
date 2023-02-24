package com.chinatsp.ifly.guide.step4;

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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.fail.GuideFailFragment;
import com.chinatsp.ifly.guide.settings.GuideSettingsFragment;
import com.chinatsp.ifly.guide.success.GuideAllSuccessFragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideStep4_2Fragment extends BaseFragment implements GuideStep4_2Contract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    AnimationImageView ivFloatSmallViewAnim;
    TextView greetingTv1;
    TextView greetingTv2;
    TextView greetingTv3;
    private GuideMainActivity activity;
    private GuideStep4_2Contract.Presenter presenter = new GuideStep4_2Presenter(this);
    private String ttsText;
    private String displayText;
    private String displayText1;
    private String displayText2;
    private String displayText3;
    private final int waitNextStepTime = 5000;
    public AnimationDrawable sayAnimationDrawable;//说话;//说话
    private TiaoZiUtilCallback tiaoZiUtilCallback;
    private BaseFragment mFragment;
    private boolean isPassBtClicked = false;
    private SpannableString spannableString;
    private final int DANCE_CHARACTER_2 = 1000;
    private final int STEP4_1 = 31;

    public static GuideStep4_2Fragment newInstance(String dataList, int step) {
        GuideStep4_2Fragment fragment = new GuideStep4_2Fragment();
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
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    spannableString = new SpannableString(greetingTv1.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 0,
                            greetingTv1.getText().length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    greetingTv1.setText(spannableString);
                    tiaoZiUtilCallback = new TiaoZiUtilCallback(null,greetingTv2, displayText2, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            handler.sendEmptyMessageDelayed(2,0);
                        }
                    });
                    break;
                case 2:
                    tiaoZiUtilCallback = new TiaoZiUtilCallback(null,greetingTv3, displayText3, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
                        @Override
                        public void onPlayStopped() {

                        }
                    });
                    break;
                case 3:
                    mFragment = GuideAllSuccessFragment.newInstance("",GuideMainActivity.STEP2);
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
        ttsText = activity.getString(R.string.greetingtts20);
        displayText = activity.getString(R.string.greeting56);
//        displayText1 = displayText.substring(0,14);
//        displayText2 = displayText.substring(14,28);
//        displayText3 = displayText.substring(28);
        displayText1 = displayText.substring(0,9);
        displayText2 = displayText.substring(10,23);
        displayText3 = displayText.substring(23);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 4_2,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 4_2,time = 0,tts = " + ttsText);
                handler.sendEmptyMessageDelayed(3,100);
            }
        });

        tiaoZiUtilCallback = new TiaoZiUtilCallback(GuideMainActivity.SELECTCOLOR,greetingTv1, displayText1, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
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
        greetingTv1 = (TextView)view.findViewById(R.id.greeting_tv1);
        greetingTv2 = (TextView)view.findViewById(R.id.greeting_tv2);
        greetingTv3 = (TextView)view.findViewById(R.id.greeting_tv3);
        ivFloatSmallViewAnim = (AnimationImageView) view.findViewById(R.id.iv_anim);

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
                starIv4.setBackgroundResource(R.mipmap.star_light);
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
        return R.layout.fragment_guide_step4_2;
    }
}
