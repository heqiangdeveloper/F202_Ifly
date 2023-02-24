package com.chinatsp.ifly.guide.end;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.fail.GuideFailFragment;
import com.chinatsp.ifly.guide.step1.GuideStep1Contract;
import com.chinatsp.ifly.guide.step1.GuideStep1Fragment;
import com.chinatsp.ifly.guide.step2.GuideStep2Fragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideEndFragment extends BaseFragment implements GuideEndContract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    TextView greetingTv;
    Button iknowBt;
    Button againBt;
    AnimationImageView ivFloatSmallViewAnim;
    public AnimationDrawable sayAnimationDrawable;//说话
    private GuideMainActivity activity;
    private GuideEndContract.Presenter presenter = new GuideEndPresenter(this);
    private String ttsText;
    private String displayText;
    private BaseFragment mFragment;

    public static GuideEndFragment newInstance(String dataList, int step) {
        GuideEndFragment fragment = new GuideEndFragment();
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

    @OnClick({R.id.iknow_bt,R.id.again_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iknow_bt:
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_all_step_success), "我知道了");
                SharedPreferencesUtils.saveInt(activity, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY, 1);
                SharedPreferencesUtils.saveBoolean(activity, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
                activity.finish();
                break;
            case R.id.again_bt:
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_all_step_success), "再次体验");
                mFragment = GuideStep1Fragment.newInstance("",1);
                ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
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
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    iknowBt.setVisibility(View.VISIBLE);
                    againBt.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void initData() {
        ttsText = activity.getString(R.string.greetingtts23);
        displayText = activity.getString(R.string.greetingtts23);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = end,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = end,time = 0,tts = " + ttsText);
                handler.sendEmptyMessageDelayed(1,0);
            }
        });
        TiaoZiUtil tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, GuideMainActivity.TIAOZIDURATION);
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
        iknowBt = (Button)view.findViewById(R.id.iknow_bt);
        againBt = (Button)view.findViewById(R.id.again_bt);
        ivFloatSmallViewAnim = (AnimationImageView)view.findViewById(R.id.iv_anim);

        Utils.setFullWindowSize(activity,true,false);
        iknowBt.setVisibility(View.GONE);
        againBt.setVisibility(View.GONE);
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
                starIv4.setBackgroundResource(R.mipmap.star_light);
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
        return R.layout.fragment_guide_end;
    }
}
