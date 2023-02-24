package com.chinatsp.ifly.guide.settings;

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
import com.chinatsp.ifly.guide.end.GuideEndFragment;
import com.chinatsp.ifly.guide.fail.GuideFailFragment;
import com.chinatsp.ifly.guide.step2.GuideStep2Fragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideSettingsFragment extends BaseFragment implements GuideSettingsContract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    TextView greetingTv;
    ImageView arrowIv;
    ImageView maskIv;
    private GuideMainActivity activity;
    private GuideSettingsContract.Presenter presenter = new GuideSettingsPresenter(this);
    private String ttsText;
    private String displayText;
    private final int waitNextStepTime = 5000;
    private BaseFragment mFragment;
    private final String TAG = "GuideEndFragment";
    private boolean isPassBtClicked = false;
    private final int ARROWSHOW = 1001;
    private final int MASKSHOW = 1002;

    public static GuideSettingsFragment newInstance(String dataList, int step) {
        GuideSettingsFragment fragment = new GuideSettingsFragment();
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ARROWSHOW:
                    arrowIv.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessageDelayed(MASKSHOW,500);
                    break;
                case MASKSHOW:
                    if(null != maskIv)maskIv.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void initData() {
        ttsText = activity.getString(R.string.greetingtts22);
        displayText = activity.getString(R.string.greeting62);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                mFragment = GuideEndFragment.newInstance("",2);
                ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
            }
        });
        TiaoZiUtilCallback tiaoZiUtil = new TiaoZiUtilCallback(null, greetingTv, displayText, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
            @Override
            public void onPlayStopped() {
                handler.sendEmptyMessageDelayed(ARROWSHOW,100);
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
        arrowIv = (ImageView)view.findViewById(R.id.arrow_iv);
        maskIv = (ImageView)view.findViewById(R.id.mask_iv);

        Utils.setFullWindowSize(activity,true,false);
        setStarStatus();
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

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_settings;
    }
}
