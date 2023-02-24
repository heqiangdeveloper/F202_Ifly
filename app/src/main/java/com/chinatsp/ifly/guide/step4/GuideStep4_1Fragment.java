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
import com.chinatsp.ifly.guide.settings.GuideSettingsFragment;
import com.chinatsp.ifly.guide.step3.GuideStep3Fragment;
import com.chinatsp.ifly.guide.success.GuideSuccessFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.TiaoZiUtilCallback;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;

import butterknife.BindView;
import butterknife.OnClick;

public class GuideStep4_1Fragment extends BaseFragment implements GuideStep4_1Contract.View {
    ImageView starIv1;
    ImageView starIv2;
    ImageView starIv3;
    ImageView starIv4;
    Button passBt;
    TextView greetingTv;
    ImageView arrowIv;
    private GuideMainActivity activity;
    private GuideStep4_1Contract.Presenter presenter = new GuideStep4_1Presenter(this);
    private String ttsText;
    private String displayText;
    private final int waitNextStepTime = 5000;
    private TiaoZiUtil tiaoZiUtil;
    private BaseFragment mFragment;
    private boolean isPassBtClicked = false;
    private SpannableString spannableString;
    private final int DANCE_CHARACTER_2 = 1000;
    private final int STEP4_1_1 = 1001;
    private final int STEP4_1_2 = 1002;
    private final int STEP4_1_3 = 1003;
    private final int STEP4_1_4 = 1004;
    private final int ARROW_VISIBLE = 1005;
    private final int PASSBTN_VISIBLE = 1006;
    private SRAgent srAgent = SRAgent.getInstance();

    public static GuideStep4_1Fragment newInstance(String dataList, int step) {
        GuideStep4_1Fragment fragment = new GuideStep4_1Fragment();
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
        filter.addAction(AppConstant.MY_TEST_BROADCAST_MX_MAKETEAM);
        activity.registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(GuideMainActivity.TAG, "---action---" + action);
            //“可见即可说”
            if (AppConstant.MY_TEST_BROADCAST_MX_MAKETEAM.equals(action)) {
                //"可见即可说唤醒页"，成功
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_can_see_say), "成功");
                Log.d(GuideMainActivity.TAG, "\"可见即可说\"");
                isPassBtClicked = true;
                try {
                    greetingTv.setText(activity.getString(R.string.greeting52));
                    spannableString = new SpannableString(greetingTv.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 4,
                            5, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    /*
                     *   1.停留1000ms,使用户能够看到“组队”点亮
                     *   2.停留2000ms,将美行从后台切换到前台
                     *   3.停留1000ms,调用美行接口: 组队
                     *   4.在导航界面停留6000ms,并同时播报：导航页面已发生变化，你发现了吗？之后，重新回到“新手引导”
                     */
                    handler.sendEmptyMessageDelayed(DANCE_CHARACTER_2, 100);
                } catch (Exception e) {

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
                case GuideMainActivity.LOOP1:
                    if(!isPassBtClicked){
                        //if(passBt != null) passBt.setVisibility(View.GONE);
                        ttsText = activity.getString(R.string.greetingtts16);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 4_1,time = 1,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 4_1,time = 1,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(GuideMainActivity.LOOP2,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case GuideMainActivity.LOOP2:
                    if(!isPassBtClicked){
                        //if(passBt != null) passBt.setVisibility(View.GONE);
                        ttsText = activity.getString(R.string.greetingtts16);
                        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 4_1,time = 2,tts = " + ttsText);
                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

                            @Override
                            public void onPlayStopped() {
                                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 4_1,time = 2,tts = " + ttsText);
                                handler.sendEmptyMessageDelayed(GuideMainActivity.FAIL,waitNextStepTime);
                            }
                        });
                    }
                    break;
                case GuideMainActivity.FAIL:
                    if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_can_see_say), "失败");
                    mFragment = GuideFailFragment.newInstance("",GuideMainActivity.STEP4);
                    ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case DANCE_CHARACTER_2:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(GuideMainActivity.SELECTCOLOR)), 5,
                            6, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    handler.sendEmptyMessageDelayed(STEP4_1_1,100);
                    break;
                case STEP4_1_1:
                    //将美行从后台切换到前台
                    AppControlManager.getInstance(activity).startApp(AppConstant.PACKAGE_NAME_WECARNAVI);
                    handler.sendEmptyMessageDelayed(STEP4_1_2,1000);
                    break;
                case STEP4_1_2:
                    //调用美行接口: 组队
                    MXSdkManager.getInstance(activity).makeTeam();
                    handler.sendEmptyMessageDelayed(STEP4_1_3,500);
                    break;
                case STEP4_1_3:
                    //提示导航页面已发生变化
                    Utils.startTTS(activity.getString(R.string.greetingtts18), new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            handler.sendEmptyMessageDelayed(STEP4_1_4,0);
                        }
                    });
                    break;
                case STEP4_1_4:
                    //将美行重置到首页
                    MXSdkManager.getInstance(activity).backToMap(null);
                    //将语音重新切换到前台
                    AppControlManager.getInstance(activity).setTopApp(activity);
                    handler.sendEmptyMessageDelayed(GuideMainActivity.SUCCESS,0);
                    break;
                case GuideMainActivity.SUCCESS:
                    mFragment = GuideSuccessFragment.newInstance("",GuideMainActivity.STEP4);
                    ActivityUtils.replaceFragmentToActivity2(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                    break;
                case ARROW_VISIBLE:
                    arrowIv.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessageDelayed(PASSBTN_VISIBLE,100);
                    break;
                case PASSBTN_VISIBLE:
                    if(null != passBt)passBt.setVisibility(View.VISIBLE);
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
        activity.unregisterReceiver(receiver);
    }

    @Override
    protected void initData() {
        //注册“可见即可说”
        registerSKTS();
        ttsText = activity.getString(R.string.greetingtts15);
        displayText = activity.getString(R.string.greeting52);
        Log.d(GuideMainActivity.TAG, "onPlayStart: step = 4_1,time = 0,tts = " + ttsText);
        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {

            @Override
            public void onPlayStopped() {
                Log.d(GuideMainActivity.TAG, "onPlayStopped: step = 4_1,time = 0,tts = " + ttsText);
                handler.sendEmptyMessageDelayed(GuideMainActivity.LOOP1,5000);
            }
        });
        tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, GuideMainActivity.TIAOZIDURATION, new TiaoZiUtil.OnTiaoZiStoppedListener() {
            @Override
            public void onPlayStopped() {
                handler.sendEmptyMessageDelayed(ARROW_VISIBLE,100);
            }
        });
    }

    public void registerSKTS(){
        String stkCmd = Utils.getFromAssets(activity, "stks/fu.json");
        srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stkCmd);
        srAgent.stopSRSession();
        srAgent.startSRSession();
    }

    @OnClick({R.id.pass_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.pass_bt:
                if(null != GuideMainActivity.mContext)DatastatManager.getInstance().recordUI_event(GuideMainActivity.mContext, GuideMainActivity.mContext.getString(R.string.event_id_can_see_say), "跳过");
                isPassBtClicked = true;
                mFragment = GuideSettingsFragment.newInstance("",2);
                ActivityUtils.replaceFragmentToActivity(activity.getSupportFragmentManager(), mFragment, R.id.framelayout_content);
                break;
        }
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
        passBt = (Button) view.findViewById(R.id.pass_bt);
        arrowIv = (ImageView) view.findViewById(R.id.arrow_iv);

        Utils.setFullWindowSize(activity,false,true);
        setStarStatus();
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

    @Override
    protected int getContentView() {
        return R.layout.fragment_guide_step4_1;
    }

}
