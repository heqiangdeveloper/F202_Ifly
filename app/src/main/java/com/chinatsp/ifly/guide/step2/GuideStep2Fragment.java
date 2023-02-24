package com.chinatsp.ifly.guide.step2;

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
import com.chinatsp.ifly.guide.step3.GuideStep3Fragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;

import butterknife.OnClick;

public class GuideStep2Fragment extends BaseFragment implements GuideStep2Contract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    AnimationImageView ivFloatSmallViewAnim;
    TextView greetingTv;
    Button passBt;
    private GuideMainActivity activity;
    private GuideStep2Contract.Presenter presenter = new GuideStep2Presenter(this);
    public AnimationDrawable comeAnimationDrawable;//说话
    private String ttsText;
    private String displayText;
    private BaseFragment mFragment;
    private final String TAG = "GuideStep2Fragment";
    private SpannableString spannableString;
    private final int DANCE_CHARACTER_2 = 12;
    private final int DANCE_CHARACTER_3 = 13;
    private final int DANCE_CHARACTER_4 = 14;
    private final int DANCE_CHARACTER_5 = 15;
    private final int STEP3_1 = 31;
    private final int waitNextStepTime = 5000;
    private boolean isPassBtClicked = false;

    public static GuideStep2Fragment newInstance(String dataList, int step) {
        GuideStep2Fragment fragment = new GuideStep2Fragment();
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
        registBroadcast();
    }

    private void registBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.MY_TEST_BROADCAST_WAKEUP);
        activity.registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "---action---" + action);
            if (AppConstant.MY_TEST_BROADCAST_WAKEUP.equals(action)) {
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_voice_wake_up), "成功");
                Log.d(TAG, "\"你好小欧\" 进行唤醒!");
                isPassBtClicked = true;
                try {
                    //先点亮文本，1秒后在提示通过
                    spannableString = new SpannableString(greetingTv.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 4,
                            5, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(DANCE_CHARACTER_2,100);
                } catch (Exception e) {
                    Log.d(TAG,"spannableString exception: " + e);
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
                    //if(passBt != null) passBt.setVisibility(View.GONE);
                    if(!isPassBtClicked){
                        ttsText = activity.getString(R.string.greetingtts7);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 2,time = 1,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 2,time = 1,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(2,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case 2:
                    //if(passBt != null) passBt.setVisibility(View.GONE);
                    if(!isPassBtClicked){
                        ttsText = activity.getString(R.string.greetingtts7);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 2,time = 2,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 2,time = 2,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(3,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case 3:
                    if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_voice_wake_up), "失败");
                    mFragment = GuideFailFragment.newInstance("",GuideMainActivity.STEP2);
                    ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case DANCE_CHARACTER_2:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 5,
                            6, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(DANCE_CHARACTER_3,100);
                    break;
                case DANCE_CHARACTER_3:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 6,
                            7, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(DANCE_CHARACTER_4,100);
                    break;
                case DANCE_CHARACTER_4:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 7,
                            8, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    if(greetingTv != null) greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(STEP3_1,100);
                    break;
                case STEP3_1://to success
                    mFragment = GuideSuccessFragment.newInstance("",GuideMainActivity.STEP2);
                    ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case 4:
                    if(passBt != null) passBt.setVisibility(View.VISIBLE);
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
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_voice_wake_up), "跳过");
                isPassBtClicked = true;
                mFragment = GuideStep3Fragment.newInstance("",2);
                ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
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
        ttsText = activity.getString(R.string.greetingtts6);
        displayText = activity.getString(R.string.greeting31);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 2,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 2,time = 0,tts = " + ttsText);
                //handler.sendEmptyMessageDelayed(4,0);
                handler.sendEmptyMessageDelayed(1,5000);
            }
        });
        TiaoZiUtilCallback tiaoZiUtil = new TiaoZiUtilCallback(null, greetingTv, displayText, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtilCallback.OnTiaoZiStoppedListener() {
            @Override
            public void onPlayStopped() {
                handler.sendEmptyMessageDelayed(4,0);
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
        greetingTv = (TextView)view.findViewById(R.id.greeting_tv);
        passBt = (Button)view.findViewById(R.id.pass_bt);

        Utils.setFullWindowSize(activity,true,false);
        passBt.setVisibility(View.GONE);
        setStarStatus();
        initAnimationResource();
    }

    public void setStarStatus(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                starIv1.setBackgroundResource(R.mipmap.star_light);
                starIv2.setBackgroundResource(R.mipmap.star_normal);
                starIv3.setBackgroundResource(R.mipmap.star_normal);
                starIv4.setBackgroundResource(R.mipmap.star_normal);
            }
        });
    }

    public void initAnimationResource(){
        comeAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.come_guide_animation);
        //把AnimationDrawable设置为ImageView的背景
        ivFloatSmallViewAnim.setBackgroundDrawable(comeAnimationDrawable);

        comeAnimationDrawable.setOneShot(false);
        comeAnimationDrawable.start();
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_step2;
    }
}
