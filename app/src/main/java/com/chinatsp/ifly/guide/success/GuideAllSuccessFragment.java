package com.chinatsp.ifly.guide.success;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.guide.settings.GuideSettingsFragment;
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

public class GuideAllSuccessFragment extends BaseFragment implements GuideSuccessContract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    TextView greetingTv;
    private GuideMainActivity activity;
    private GuideSuccessContract.Presenter presenter = new GuideSuccessPresenter(this);
    private String ttsText;
    private String displayText;
    private int step = 1;
    private BaseFragment mFragment;
    private TiaoZiUtil tiaoZiUtil;

    public static GuideAllSuccessFragment newInstance(String dataList, int step) {
        GuideAllSuccessFragment fragment = new GuideAllSuccessFragment();
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

    @Override
    protected void initData() {
        ttsText = activity.getString(R.string.greetingtts21);
        displayText = activity.getString(R.string.greeting61);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = allSuccess,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = allSuccess,time = 0,tts = " + ttsText);
                mFragment = GuideSettingsFragment.newInstance("",2);
                ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
            }
        });
        tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, GuideMainActivity.TIAOZIDURATION);
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
        return R.layout.fragment_guide_all_success;
    }
}
