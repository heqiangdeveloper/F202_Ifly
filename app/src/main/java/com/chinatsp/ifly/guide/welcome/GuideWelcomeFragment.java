package com.chinatsp.ifly.guide.welcome;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.chinatsp.ifly.guide.step1.GuideStep1Fragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideWelcomeFragment extends BaseFragment implements GuideWelcomeContract.View {
    Button go_Bt;
    Button exit_Bt;
    AnimationImageView ivFloatSmallViewAnim;
    TextView greeting_Tv;
    private GuideMainActivity activity;
    private GuideWelcomeContract.Presenter presenter = new GuideWelcomePresenter(this);
    public AnimationDrawable sayAnimationDrawable;//说话
    private String ttsText;
    private String displayText;
    private BaseFragment mFragment;

    public static GuideWelcomeFragment newInstance(String dataList, String answer) {
        GuideWelcomeFragment fragment = new GuideWelcomeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FullScreenActivity.DATA_LIST_STR, dataList);
        bundle.putString(FullScreenActivity.ANSWER_STR, answer);
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
                case 0:
                    TiaoZiUtilCallback tiaoZiUtil = new TiaoZiUtilCallback(null, greeting_Tv, displayText, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            handler.sendEmptyMessageDelayed(1, 0);
                        }
                    }

                    );
                    break;
                case 1:
                    go_Bt.setVisibility(View.VISIBLE);
                    exit_Bt.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @OnClick({R.id.go_bt, R.id.exit_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.go_bt:
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_welcome), "体验");
                mFragment = GuideStep1Fragment.newInstance("",GuideMainActivity.STEP1);
                ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                break;
            case R.id.exit_bt:
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_welcome), "退出");
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

    @Override
    public void onPause() {
        super.onPause();
        //activity.finish();
    }

    @Override
    protected void initData() {
        ttsText = activity.getString(R.string.greetingtts1);
        displayText = activity.getString(R.string.greeting1);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = welcome,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = welcome,time = 0,tts = " + ttsText);

            }
        });
        handler.sendEmptyMessageDelayed(0, 900);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {
        go_Bt = (Button)view.findViewById(R.id.go_bt);
        exit_Bt = (Button) view.findViewById(R.id.exit_bt);
        ivFloatSmallViewAnim = (AnimationImageView) view.findViewById(R.id.iv_anim);
        greeting_Tv = (TextView) view.findViewById(R.id.greeting_tv);

        initAnimationResource();
        go_Bt.setVisibility(View.GONE);
        exit_Bt.setVisibility(View.GONE);
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
        return R.layout.fragment_guide_welcome;
    }
}
