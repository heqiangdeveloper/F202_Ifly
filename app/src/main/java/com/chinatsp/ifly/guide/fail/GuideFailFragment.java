package com.chinatsp.ifly.guide.fail;

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

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.step1.GuideStep1Fragment;
import com.chinatsp.ifly.guide.step2.GuideStep2Fragment;
import com.chinatsp.ifly.guide.step3.GuideStep3Fragment;
import com.chinatsp.ifly.guide.step4.GuideStep4Fragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideFailFragment extends BaseFragment implements GuideFailContract.View {
    TextView greetingTv;
    Button againBt;
    Button exitBt;
    AnimationImageView ivFloatSmallViewAnim;
    private GuideMainActivity activity;
    private GuideFailContract.Presenter presenter = new GuideFailPresenter(this);
    public AnimationDrawable failAnimationDrawable;//说话
    private String ttsText;
    private String displayText;
    private int step;
    private BaseFragment mFragment;

    public static GuideFailFragment newInstance(String dataList, int step) {
        GuideFailFragment fragment = new GuideFailFragment();
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

    @OnClick({R.id.again_bt, R.id.exit_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.again_bt:
                switch (step){
                    case 1:
                        mFragment = GuideStep1Fragment.newInstance("",GuideMainActivity.STEP1);
                        break;
                    case 2:
                        mFragment = GuideStep2Fragment.newInstance("",GuideMainActivity.STEP2);
                        break;
                    case 3:
                        mFragment = GuideStep3Fragment.newInstance("",GuideMainActivity.STEP3);
                        break;
                    case 4:
                        mFragment = GuideStep4Fragment.newInstance("",GuideMainActivity.STEP4);
                        break;
                }
                ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                break;
            case R.id.exit_bt:
                SharedPreferencesUtils.saveInt(activity, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY, 1);
                SharedPreferencesUtils.saveBoolean(activity, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
                activity.finish();
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
                    againBt.setVisibility(View.VISIBLE);
                    exitBt.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void initData() {
        if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_fail), "");
        step = getArguments().getInt(GuideMainActivity.STEP);
        ttsText = activity.getString(R.string.greetingfail);
        displayText = activity.getString(R.string.greeting22);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = fail,time = 0,failstep = " + step + ",tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = fail,time = 0,failstep = " + step + ",tts = " + ttsText);
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
        againBt = (Button)view.findViewById(R.id.again_bt);
        exitBt = (Button)view.findViewById(R.id.exit_bt);
        greetingTv = (TextView)view.findViewById(R.id.greeting_tv);
        ivFloatSmallViewAnim = (AnimationImageView)view.findViewById(R.id.iv_anim);

        Utils.setFullWindowSize(activity,true,false);
        againBt.setVisibility(View.GONE);
        exitBt.setVisibility(View.GONE);
        initAnimationResource();
    }

    public void initAnimationResource(){
        failAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.fail_guide_animation);
        //把AnimationDrawable设置为ImageView的背景
        ivFloatSmallViewAnim.setBackgroundDrawable(failAnimationDrawable);

        failAnimationDrawable.setOneShot(false);
        failAnimationDrawable.start();
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_fail;
    }
}
