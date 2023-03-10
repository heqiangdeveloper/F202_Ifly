package com.chinatsp.ifly;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.car.CarNotConnectedException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.module.seachlist.SearchListFragment;
import com.chinatsp.ifly.module.stock.StockFragment;
import com.chinatsp.ifly.module.weather.WeatherFragment;
import com.chinatsp.ifly.utils.ActivityUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.utils.TiaoZiUtil;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.AnimationImageView;
import com.chinatsp.ifly.voice.platformadapter.PlatformAdapterClient;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.listener.IExtendCallback;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.car.VehicleAreaSeat.HVAC_ALL;
import static android.car.hardware.constant.HVAC.HVAC_ON;
import static android.car.hardware.hvac.CarHvacManager.ID_HVAC_POWER_ON;
import static android.hardware.automotive.vehicle.V2_0.VehicleAreaSeat.ROW_1_LEFT;

public class NoviceGuideActivity extends Activity implements View.OnClickListener {
    @BindView(R.id.star_iv1)
    ImageView starIv1;
    @BindView(R.id.star_iv2)
    ImageView starIv2;
    @BindView(R.id.star_iv3)
    ImageView starIv3;
    @BindView(R.id.star_iv4)
    ImageView starIv4;
    @BindView(R.id.fireworks_ll)
    LinearLayout fireworksLl;
    @BindView(R.id.iv_anim)
    AnimationImageView ivFloatSmallViewAnim;
    @BindView(R.id.iv_anim1)
    AnimationImageView fireworksAnim1;
    @BindView(R.id.iv_anim2)
    AnimationImageView fireworksAnim2;
    @BindView(R.id.iv_anim3)
    AnimationImageView fireworksAnim3;
    @BindView(R.id.iv_anim4)
    AnimationImageView fireworksAnim4;
    @BindView(R.id.go_bt)
    Button goBt;
    @BindView(R.id.pass_bt)
    Button passBt;
    @BindView(R.id.exit_bt)
    Button exitBt;
    @BindView(R.id.greeting_tv)
    TextView greetingTv;
    @BindView(R.id.star_ll)
    LinearLayout starLl;
    @BindView(R.id.root_fl)
    FrameLayout rootFl;
    @BindView(R.id.arrow_iv)
    ImageView arrowIv;
    @BindView(R.id.btn_iv)
    ImageView btnIv;
    @BindView(R.id.btn_rl)
    RelativeLayout btnRl;
    @BindView(R.id.settings_iv)
    ImageView settingsIv;
    private Context mContext;
    private String displayText;
    private String ttsText;
    private boolean isWelcomeText = true;
    private int CURRENTSTEP = 1;
    private int times = 0;
    private String TAG = "NoviceGuideActivity";
    private final int STEP1 = 1;
    private final int STEP2 = 2;
    private final int STEP3 = 3;
    private final int STEP4 = 4;
    private final int STEP5 = 5;
    private final int STEP6 = 6;
    private final int DANCE_CHARACTER_2 = 12;
    private final int DANCE_CHARACTER_3 = 13;
    private final int DANCE_CHARACTER_4 = 14;
    private final int DANCE_CHARACTER_5 = 15;
    private final int STEP3_1 = 31;
    private final int STEP5_1 = 51;
    private final int STEP5_2 = 52;
    private final int STEP5_3 = 53;
    private final int STEP5_4 = 54;
    private CountDownTimer countDownTimer = null;
    private final int waitNextStepTime = 5000;
    private TiaoZiUtil tiaoZiUtil = null;
    private boolean isCarControlMoreSaid = false;
    private boolean isCanSeeCanSaySaid = false;
    public AnimationDrawable animationDrawable1;//????????????--????????????
    public AnimationDrawable animationDrawable2;//????????????--????????????
    public AnimationDrawable animationDrawable3;//????????????--????????????
    public AnimationDrawable animationDrawable4;//????????????--????????????
    public AnimationDrawable animationDrawable;//??????????????????
    public AnimationDrawable failAnimationDrawable;//????????????--????????????
    public int celebrateAnimationDuration = 1;
    private ObjectAnimator objectAnimator;
    private SRAgent srAgent = SRAgent.getInstance();
    private int wordDuration = 150;
    private int fullScreenSizeWidth = 0;
    private int fullScreenSizeHeight = 0;
    private boolean isSuccessSTEP2 = false;
    private boolean isSuccessSTEP3 = false;
    private boolean isSuccessSTEP4 = false;
    private boolean isSuccessSTEP5 = false;
    private boolean isSpeaking = false;
    private SpannableString spannableString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noviceguide);
        ButterKnife.bind(this);
        mContext = NoviceGuideActivity.this;
        mContext.sendBroadcast(new Intent("close.mainactivity"));
        initView();
        registBroadcast();
    }

    private void registBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.ACTION_AWARE_VOICE_KEY);
        filter.addAction(AppConstant.MY_TEST_BROADCAST_WAKEUP);
        filter.addAction(AppConstant.MY_TEST_BROADCAST_OPEN_AC);
        filter.addAction(AppConstant.MY_TEST_BROADCAST_MX_MAKETEAM);
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "---action---" + action);
            if (!isSpeaking && AppConstant.ACTION_AWARE_VOICE_KEY.equals(action)) {
                Bundle bundle = intent.getExtras();
                String state = bundle.getString("Key_state");
                String code = bundle.getString("Key_code");
                Log.d(TAG, "state = " + state + ", code = " + code);
                if(state.equals("DOWN") && code.equals("VR") && CURRENTSTEP == STEP2){
                    //"??????????????????"?????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_VR), "??????");
                    isSuccessSTEP2 = true;
                    Log.d(TAG, "VR is clicked!");
                    isSuccessSTEP2 = true;
                    rootFl.setBackgroundColor(Color.WHITE);
                    setFullWindowSize(true);
                    setAllButtonStatus(false);
                    btnIv.setVisibility(View.GONE);
                    ivFloatSmallViewAnim.setVisibility(View.VISIBLE);
                    displayText = mContext.getString(R.string.greeting23);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                    greetingTv.setTextColor(Color.BLACK);
                    ttsText = mContext.getString(R.string.greetingtts5);

                    showFireworks(true);
                    lightStar(starIv1);
                    startSpeakAndChangeStar(CURRENTSTEP, ttsText);
                }
            }
            //"????????????" ????????????
            if (!isSpeaking && AppConstant.MY_TEST_BROADCAST_WAKEUP.equals(action) && CURRENTSTEP == STEP3) {
                //"??????????????????"?????????
                DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_voice_wake_up), "??????");
                Log.d(TAG, "\"????????????\" ????????????!");
                isSuccessSTEP3 = true;
                try {
                    //??????????????????1?????????????????????
                    spannableString = new SpannableString(greetingTv.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#55FFFF")), 4,
                            5, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    mHandler.sendEmptyMessageDelayed(DANCE_CHARACTER_2,100);
                } catch (Exception e) {

                }
            }
            //??????????????????
            if (!isSpeaking && AppConstant.MY_TEST_BROADCAST_OPEN_AC.equals(action) && CURRENTSTEP == STEP4) {
                //"???????????????"?????????
                DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_voice_car_control), "??????");
                Log.d(TAG, "\"????????????\" ????????????!");
                //??????????????????
                boolean isOpen = isAirOpen();
                sendBroadcastToACController();
                if(!isOpen){
                    changeAirStatus(HVAC_ON);
                }

                isSuccessSTEP4 = true;
                rootFl.setBackgroundResource(R.mipmap.ac);
                setFullWindowSize(true);
                setAllButtonStatus(false);
                ivFloatSmallViewAnim.setVisibility(View.VISIBLE);
                displayText = mContext.getString(R.string.greeting42);
                tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                greetingTv.setTextColor(Color.WHITE);
                ttsText = mContext.getString(R.string.greetingtts12);
                showFireworks(true);
                lightStar(starIv1);
                lightStar(starIv2);
                lightStar(starIv3);
                startSpeakAndChangeStar(CURRENTSTEP, ttsText);
            }
            //?????????????????????
            if (!isSpeaking && AppConstant.MY_TEST_BROADCAST_MX_MAKETEAM.equals(action) && CURRENTSTEP == STEP5) {
                //"????????????????????????"?????????
                DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_can_see_say), "??????");
                Log.d(TAG, "\"???????????????\"");
                isSuccessSTEP5 = true;
                try {
                    greetingTv.setText(mContext.getString(R.string.greeting52));
                    SpannableString spannableString = new SpannableString(greetingTv.getText().toString());
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#55FFFF")), 4,
                            spannableString.length() - 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    /*
                    *   1.??????1000ms,???????????????????????????????????????
                    *   2.??????2000ms,?????????????????????????????????
                    *   3.??????1000ms,??????????????????: ??????
                    *   4.?????????????????????6000ms,?????????????????????????????????????????????????????????????????????????????????????????????????????????
                     */
                    mHandler.sendEmptyMessageDelayed(STEP5_1,1000);
                } catch (Exception e) {

                }

            }
        }
    };

    private void sendBroadcastToACController(){
        mContext.sendBroadcast(new Intent(AppConstant.VOICE_ACTION));
        Log.d(TAG, "called sendBroadcastToACController....");
    }

    /**
     * ??????????????????
     * @return
     */
    private boolean isAirOpen() {
        try {
            int HVAC_LEFT = (int)(ROW_1_LEFT | VehicleAreaSeat.ROW_2_LEFT |
                    VehicleAreaSeat.ROW_2_CENTER);
            int HVAC_RIGHT = (int)(VehicleAreaSeat.ROW_1_RIGHT | VehicleAreaSeat.ROW_2_RIGHT);
            int mHVAC_ALL = HVAC_LEFT | HVAC_RIGHT;

            Log.d(TAG, "lh:get air status mHVAC_ALL = " + mHVAC_ALL);
            int status = AppConfig.INSTANCE.mCarHvacManager.getIntProperty(ID_HVAC_POWER_ON, mHVAC_ALL);
            Log.d(TAG, "lh:get air status???" + status);
            if (status == HVAC_ON) {
                return true;
            }
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ??????????????????
     */
    private void changeAirStatus(int status) {
        try {
            AppConfig.INSTANCE.mCarHvacManager.
                    setIntProperty(ID_HVAC_POWER_ON, HVAC_ALL, status);
            Log.d(TAG, "lh:change air status???" + status);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
            Log.d(TAG, "changeAirStatus Exception: " + e);
        }
    }

    private void startLocalApp(String packageNameTarget) {
        Log.i(TAG, "--????????????????????? APP=" + packageNameTarget);
        if (AppControlManager.getInstance(this).appIsExist(mContext, packageNameTarget)) {
            PackageManager packageManager = getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(packageNameTarget);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

            /**android.intent.action.MAIN?????????????????????
             */
            //intent.setAction("android.intent.action.MAIN");
            /**
             * FLAG_ACTIVITY_SINGLE_TOP:
             * ?????????????????????activity??????????????????activity,??????????????????????????????activity
             */
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            Log.i(TAG, "--?????????????????? APP=" + packageNameTarget);
        }
    }

    public void initAnimationResource(){
        animationDrawable = new AnimationDrawable();
        animationDrawable1 = new AnimationDrawable();
        animationDrawable2 = new AnimationDrawable();
        animationDrawable3 = new AnimationDrawable();
        animationDrawable4 = new AnimationDrawable();
        failAnimationDrawable = new AnimationDrawable();

        //animationDrawable
        for (int i = 1; i <= 32; i += 1) {
            int resId = Utils.getId(mContext, "come_" + String.format("%04d", i));
            animationDrawable.addFrame(mContext.getResources().getDrawable(resId), celebrateAnimationDuration);
        }
        //animationDrawable1
        for (int i = 1; i <= 34; i += 1) {
            int resId1 = Utils.getId(mContext, "redfire_" + String.format("%04d", i));
            animationDrawable1.addFrame(mContext.getResources().getDrawable(resId1), celebrateAnimationDuration);
        }
        //animationDrawable2
        for (int i = 1; i <= 35; i += 1) {
            int resId2 = Utils.getId(mContext, "trailwork_" + String.format("%04d", i));
            animationDrawable2.addFrame(mContext.getResources().getDrawable(resId2), celebrateAnimationDuration);
        }
        //animationDrawable3
        for (int i = 37; i <= 71; i += 1) {
            int resId3 = Utils.getId(mContext, "greenfire_" + String.format("%04d", i));
            animationDrawable3.addFrame(mContext.getResources().getDrawable(resId3), celebrateAnimationDuration);
        }
        //animationDrawable4
        for (int i = 1; i <= 33; i += 1) {
            int resId4 = Utils.getId(mContext, "goldenfire_" + String.format("%04d", i));
            animationDrawable4.addFrame(mContext.getResources().getDrawable(resId4), celebrateAnimationDuration);
        }
        //failAnimationDrawable
        for (int i = 1; i <= 37; i += 1) {
            int resId = Utils.getId(mContext, "fail_" + String.format("%04d", i));
            failAnimationDrawable.addFrame(mContext.getResources().getDrawable(resId), celebrateAnimationDuration);
        }

        animationDrawable.setOneShot(false);//????????????
        animationDrawable1.setOneShot(false);
        animationDrawable2.setOneShot(false);
        animationDrawable3.setOneShot(false);
        animationDrawable4.setOneShot(false);
        failAnimationDrawable.setOneShot(false);
    }

    public void loadAnimationResource() {
        animationDrawable.stop();
        animationDrawable.start();
        ivFloatSmallViewAnim.loadAnimation(animationDrawable, null);
        ivFloatSmallViewAnim.setVisibility(View.VISIBLE);
    }

    public void loadFailAnimationResource() {
        ivFloatSmallViewAnim.loadAnimation(failAnimationDrawable, null);
        ivFloatSmallViewAnim.setVisibility(View.VISIBLE);
    }

    public void showFireworks(boolean flag) {
        if (flag) {
            fireworksLl.setVisibility(View.VISIBLE);

            fireworksAnim1.loadAnimation(animationDrawable1, null);
            fireworksAnim2.loadAnimation(animationDrawable2, null);
            fireworksAnim3.loadAnimation(animationDrawable3, null);
            fireworksAnim4.loadAnimation(animationDrawable4, null);
        } else {
            fireworksLl.setVisibility(View.GONE);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DANCE_CHARACTER_2:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#55FFFF")), 5,
                            6, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    mHandler.sendEmptyMessageDelayed(DANCE_CHARACTER_3,100);
                    break;
                case DANCE_CHARACTER_3:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#55FFFF")), 6,
                            7, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    mHandler.sendEmptyMessageDelayed(DANCE_CHARACTER_4,100);
                    break;
                case DANCE_CHARACTER_4:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#55FFFF")), 7,
                            8, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    mHandler.sendEmptyMessageDelayed(DANCE_CHARACTER_5,100);
                    break;
                case DANCE_CHARACTER_5:
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#55FFFF")), 8,
                            9, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    greetingTv.setText(spannableString);
                    mHandler.sendEmptyMessageDelayed(STEP3_1,100);
                    break;
                case 0:
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration + 220);
                    break;
                case STEP1:
                    if (isWelcomeText) {
                        starLl.setVisibility(View.VISIBLE);
                        setPassButtonStatus(false);
                        isWelcomeText = false;
                    } else {
                        CURRENTSTEP = STEP2;
                    }
                    break;
                case STEP2:
                    if (isSuccessSTEP2) {
                        times = 0;
                    } else {
                        if (times < 3) {
                            ttsText = mContext.getString(R.string.greetingtts4);
                            displayText = mContext.getString(R.string.greeting21);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText);
                            startSpeak(STEP2, ttsText);
                        }
                        if (times == 3) {//??????????????????
                            DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_fail), "");
                            isSuccessSTEP2 = false;
                            loadFailAnimationResource();
                            goBt.setBackgroundResource(R.mipmap.btn_again);
                            exitBt.setBackgroundResource(R.mipmap.btn_exit);

                            ttsText = mContext.getString(R.string.greetingfail);
                            displayText = mContext.getString(R.string.greeting22);
                            btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                            rootFl.setBackgroundColor(Color.WHITE);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText);
                            greetingTv.setTextColor(Color.BLACK);
                            setPassButtonStatus(false);
                            startSpeak(STEP2, ttsText);
                        }
                    }
                    break;
                case STEP3:
                    if (isSuccessSTEP3) {
                        times = 0;
                    } else {
                        if (times < 3) {
                            ttsText = mContext.getString(R.string.greetingtts7);
                            displayText = mContext.getString(R.string.greeting31);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText);
                            startSpeak(STEP3, ttsText);
                        }
                        if (times == 3) {//??????????????????
                            DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_fail), "");
                            isSuccessSTEP3 = false;
                            loadFailAnimationResource();
                            goBt.setBackgroundResource(R.mipmap.btn_again);
                            exitBt.setBackgroundResource(R.mipmap.btn_exit);

                            ttsText = mContext.getString(R.string.greetingfail);
                            displayText = mContext.getString(R.string.greeting22);
                            rootFl.setBackgroundColor(Color.WHITE);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                            greetingTv.setTextColor(Color.BLACK);
                            setPassButtonStatus(false);
                            startSpeak(STEP3, ttsText);
                        }
                    }
                    break;
                case STEP4:
                    if (isSuccessSTEP4) {
                        times = 0;
                    } else {
                        if (times < 3) {
                            ttsText = mContext.getString(R.string.greetingtts11);
                            displayText = mContext.getString(R.string.greeting41);
                            rootFl.setBackgroundResource(R.mipmap.ac);
                            greetingTv.setTextColor(Color.WHITE);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText);
                            startSpeak(STEP4, ttsText);
                        }
                        if (times == 3) {//??????????????????
                            DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_fail), "");
                            isSuccessSTEP4 = false;
                            loadFailAnimationResource();
                            goBt.setBackgroundResource(R.mipmap.btn_again);
                            exitBt.setBackgroundResource(R.mipmap.btn_exit);

                            ttsText = mContext.getString(R.string.greetingfail);
                            displayText = mContext.getString(R.string.greeting22);
                            rootFl.setBackgroundColor(Color.WHITE);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                            greetingTv.setTextColor(Color.BLACK);
                            setPassButtonStatus(false);
                            startSpeak(STEP4, ttsText);
                        }
                    }
                    break;
                case STEP5:
                    if (isSuccessSTEP5) {
                        times = 0;
                    } else {
                        if (times == 1) {
                            btnRl.setPadding(0, 0, 0, 0);
                            arrowIv.setVisibility(View.VISIBLE);//?????? ??????
                            ttsText = mContext.getString(R.string.greetingtts15);
                            displayText = mContext.getString(R.string.greeting52);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                            greetingTv.setTextColor(Color.WHITE);
                            startSpeak(STEP5, ttsText);
                        } else if (times == 2 || times == 3) {
                            btnRl.setPadding(0, 0, 0, 0);
                            arrowIv.setVisibility(View.VISIBLE);//?????? ??????
                            ttsText = mContext.getString(R.string.greetingtts16);
                            displayText = mContext.getString(R.string.greeting52);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText);
                            greetingTv.setTextColor(Color.WHITE);
                            startSpeak(STEP5, ttsText);
                        } else if (times == 4) {//??????????????????
                            DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_fail), "");
                            isSuccessSTEP5 = false;
                            setFullWindowSize(true);
                            btnRl.setPadding(0, 0, 0, 0);
                            arrowIv.setVisibility(View.GONE);//?????? ??????
                            loadFailAnimationResource();
                            goBt.setBackgroundResource(R.mipmap.btn_again);
                            exitBt.setBackgroundResource(R.mipmap.btn_exit);

                            ttsText = mContext.getString(R.string.greetingfail);
                            displayText = mContext.getString(R.string.greeting22);
                            rootFl.setBackgroundColor(Color.WHITE);
                            tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                            greetingTv.setTextColor(Color.BLACK);
                            setPassButtonStatus(false);
                            startSpeak(STEP5, ttsText);
                        }
                    }
                    break;
                case STEP6:
                    goBt.setVisibility(View.VISIBLE);
                    goBt.setBackgroundResource(R.mipmap.btn_again);
                    passBt.setVisibility(View.INVISIBLE);
                    exitBt.setVisibility(View.VISIBLE);
                    exitBt.setBackgroundResource(R.mipmap.btn_iknow);

                    if (times == 1) {
                        ttsText = mContext.getString(R.string.greetingtts22);
                        displayText = mContext.getString(R.string.greeting62);
                        tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                        greetingTv.setTextColor(Color.WHITE);
                        arrowIv.setVisibility(View.VISIBLE);//?????? ??????
                        btnRl.setPadding(0, 0, 130, 0);
                        settingsIv.setVisibility(View.VISIBLE);//?????? ??????
                        startSpeak(STEP6, ttsText);
                    } else if (times == 2) {
                        ttsText = mContext.getString(R.string.greetingtts23);
                        displayText = mContext.getString(R.string.greetingtts23);
                        tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                        greetingTv.setTextColor(Color.WHITE);
                        arrowIv.setVisibility(View.GONE);//?????? ??????
                        settingsIv.setVisibility(View.GONE);//?????? ??????
                        startSpeak(STEP6, ttsText);
                    }
                    break;
                case STEP3_1:
                    rootFl.setBackgroundColor(Color.WHITE);
                    setFullWindowSize(true);
                    setAllButtonStatus(false);
                    loadAnimationResource();
                    displayText = mContext.getString(R.string.greeting33);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                    greetingTv.setTextColor(Color.BLACK);
                    ttsText = mContext.getString(R.string.greetingtts9);

                    showFireworks(true);
                    lightStar(starIv1);
                    lightStar(starIv2);
                    startSpeakAndChangeStar(CURRENTSTEP, ttsText);
                    break;
                case STEP5_1:
                    //?????????????????????????????????
                    AppControlManager.getInstance(mContext).startApp(AppConstant.PACKAGE_NAME_WECARNAVI);
                    mHandler.sendEmptyMessageDelayed(STEP5_2,2000);
                    break;
                case STEP5_2:
                    //??????????????????: ??????
                    MXSdkManager.getInstance(mContext).makeTeam();
                    mHandler.sendEmptyMessageDelayed(STEP5_3,1000);
                    break;
                case STEP5_3:
                    //?????????????????????????????????
                    isSpeaking = true;
                    Utils.startTTS(mContext.getString(R.string.greetingtts18), new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            isSpeaking = false;
                        }
                    });
                    mHandler.sendEmptyMessageDelayed(STEP5_4,6000);
                    break;
                case STEP5_4:
                    //????????????????????????
                    MXSdkManager.getInstance(mContext).backToMap(null);
                    //??????????????????????????????
                    AppControlManager.getInstance(mContext).setTopApp(mContext);

                    isSuccessSTEP5 = true;
                    rootFl.setBackgroundColor(Color.parseColor("#e0000000"));//??????
                    setFullWindowSize(true);
                    setAllButtonStatus(false);
                    arrowIv.setVisibility(View.GONE);//?????? ??????
                    settingsIv.setVisibility(View.GONE); //?????? s??????
                    loadAnimationResource();
                    ttsText = mContext.getString(R.string.greetingtts19);
                    displayText = mContext.getString(R.string.greeting55);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                    greetingTv.setTextColor(Color.WHITE);
                    showFireworks(true);
                    lightStar(starIv1);
                    lightStar(starIv2);
                    lightStar(starIv3);
                    lightStar(starIv4);
                    startSpeakAndChangeStar(CURRENTSTEP, ttsText);
                    break;
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && (CURRENTSTEP == STEP1)) {
            ttsText = mContext.getString(R.string.greetingtts1);
            isWelcomeText = true;
            setPassButtonStatus(false);
            startSpeak(CURRENTSTEP, ttsText);
            //??????????????????????????????
            mHandler.sendEmptyMessageDelayed(0, 900);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        Display display = getWindowManager().getDefaultDisplay(); // ?????????????????????
        fullScreenSizeWidth = display.getMode().getPhysicalWidth();
        fullScreenSizeHeight = display.getMode().getPhysicalHeight();

        //????????????
        fireworksLl.setVisibility(View.GONE);
        //?????????????????????
        initAnimationResource();
        loadAnimationResource();
        //???????????????????????????
        String stkCmd = Utils.getFromAssets(mContext, "stks/fu.json");
        srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stkCmd);
        srAgent.stopSRSession();
        srAgent.startSRSession();

        CURRENTSTEP = 1;
        times = 0;

        btnIv.setVisibility(View.GONE);//?????? ?????? ??????
        arrowIv.setVisibility(View.GONE);//?????? ??????
        settingsIv.setVisibility(View.GONE);//?????? ??????
        rootFl.setBackgroundColor(Color.WHITE);
        starLl.setVisibility(View.INVISIBLE);
        goBt.setOnClickListener(this);
        passBt.setOnClickListener(this);
        exitBt.setOnClickListener(this);

        goBt.setBackgroundResource(R.mipmap.btn_learn);
        ttsText = mContext.getString(R.string.greetingtts1);
        displayText = mContext.getString(R.string.greeting1);
        //??????????????????????????????
        //tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration + 220);
    }

    private void setAllButtonStatus(boolean isVisible) {
        if (isVisible) {
            goBt.setVisibility(View.VISIBLE);
            passBt.setVisibility(View.VISIBLE);
            exitBt.setVisibility(View.VISIBLE);
        } else {
            goBt.setVisibility(View.INVISIBLE);
            passBt.setVisibility(View.INVISIBLE);
            exitBt.setVisibility(View.INVISIBLE);
        }
    }

    //?????????????????????????????????
    private void setPassButtonStatus(boolean isVisible) {
        if (isVisible) {
            goBt.setVisibility(View.INVISIBLE);
            passBt.setVisibility(View.VISIBLE);
            exitBt.setVisibility(View.INVISIBLE);
        } else {
            goBt.setVisibility(View.VISIBLE);
            passBt.setVisibility(View.INVISIBLE);
            exitBt.setVisibility(View.VISIBLE);
        }
    }

    //??????TTS????????????????????????????????????
    private void startSpeakAndChangeStar(final int step, String text) {
        if (!isSpeaking) {
            isSpeaking = true;
            Utils.startTTS(text, new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    isSpeaking = false;
                    switch (step) {
                        case STEP1:
                            break;
                        case STEP2:
                            //lightStar(starIv1);
                            countDownTimer = new CountDownTimer(2000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    CURRENTSTEP = STEP2;
                                    isSuccessSTEP2 = true;
                                    passBt.callOnClick();//to step3
                                }
                            };
                            countDownTimer.start();
                            break;
                        case STEP3:
                            //lightStar(starIv1);
                            //lightStar(starIv2);
                            countDownTimer = new CountDownTimer(2000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    CURRENTSTEP = STEP3;
                                    isSuccessSTEP3 = true;
                                    passBt.callOnClick();//to step4
                                }
                            };
                            countDownTimer.start();
                            break;
                        case STEP4:
//                            lightStar(starIv1);
//                            lightStar(starIv2);
//                            lightStar(starIv3);

                            countDownTimer = new CountDownTimer(2000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    CURRENTSTEP = STEP4;
                                    if (!isCarControlMoreSaid) {
                                        isCarControlMoreSaid = true;
                                        displayText = mContext.getString(R.string.greeting43);
                                        tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                                        ttsText = mContext.getString(R.string.greetingtts13);
                                        isSuccessSTEP4 = true;
                                        showFireworks(false);
                                        startSpeakAndChangeStar(CURRENTSTEP, ttsText);
                                    } else {
                                        isSuccessSTEP4 = true;
                                        passBt.callOnClick();//to step5
                                    }
                                }
                            };
                            countDownTimer.start();
                            break;
                        case STEP5:
//                            lightStar(starIv1);
//                            lightStar(starIv2);
//                            lightStar(starIv3);
//                            lightStar(starIv4);
                            countDownTimer = new CountDownTimer(2000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    CURRENTSTEP = STEP5;
                                    if (!isCanSeeCanSaySaid) {
                                        isCanSeeCanSaySaid = true;
                                        ttsText = mContext.getString(R.string.greetingtts20);
                                        displayText = mContext.getString(R.string.greeting56);
                                        tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                                        isSuccessSTEP5 = true;
                                        startSpeakAndChangeStar(CURRENTSTEP, ttsText);
                                    } else {
                                        isSuccessSTEP5 = true;
                                        passBt.callOnClick();//to step6
                                    }
                                }
                            };
                            countDownTimer.start();
                            break;
                        case STEP6:
                            break;
                    }
                }
            });
        }
    }

    public void lightStar(ImageView iv) {
        iv.setBackgroundResource(R.mipmap.star_light);
    }

    private void startSpeak(final int step, String text) {
        if (!isSpeaking) {
            isSpeaking = true;
            Utils.startTTS(text, new TTSController.OnTtsStoppedListener() {

                @Override
                public void onPlayStopped() {
                    Log.d("hq", Thread.currentThread().getName());
                    isSpeaking = false;
                    switch (step) {
                        case STEP1:
                            setPassButtonStatus(true);
                            mHandler.sendEmptyMessage(step);
                            break;
                        case STEP2:
                            times++;
                            if (!isSuccessSTEP2 && times <= 3) {
                                setPassButtonStatus(true);
                                mHandler.sendEmptyMessageDelayed(step, waitNextStepTime);
                            } else {
                                setPassButtonStatus(false);
                            }
                            break;
                        case STEP3:
                            times++;
                            if (!isSuccessSTEP3 && times <= 3) {
                                setPassButtonStatus(true);
                                mHandler.sendEmptyMessageDelayed(step, waitNextStepTime);
                            } else {
                                setPassButtonStatus(false);
                            }
                            break;
                        case STEP4:
                            times++;
                            if (!isSuccessSTEP4 && times <= 3) {
                                setPassButtonStatus(true);
                                mHandler.sendEmptyMessageDelayed(step, waitNextStepTime);
                            } else {
                                setPassButtonStatus(false);
                            }
                            break;
                        case STEP5:
                            settingsIv.setVisibility(View.GONE);//?????? ??????
                            times++;
                            if (!isSuccessSTEP5 && times <= 4) {
                                setPassButtonStatus(true);
                                mHandler.sendEmptyMessageDelayed(step, waitNextStepTime);
                            } else {
                                setPassButtonStatus(false);
                            }
                            break;
                        case STEP6:
                            times++;
                            if (times <= 3) {
                                mHandler.sendEmptyMessageDelayed(step, waitNextStepTime);
                            }
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @OnClick({R.id.go_bt, R.id.pass_bt, R.id.exit_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go_bt:
                if (CURRENTSTEP == STEP1 || CURRENTSTEP == STEP2) {
                    if(CURRENTSTEP == STEP1){//???????????????????????????????????????
                        DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_welcome), "??????");
                    }else{//"????????????????????????"??????????????????????????????
                        DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_VR), "??????");
                    }

                    showFireworks(false);
                    setFullWindowSize(true);

                    isSuccessSTEP2 = false;
                    times = 0;
                    loadAnimationResource();
                    rootFl.setBackgroundResource(R.mipmap.wheel);

                    ttsText = mContext.getString(R.string.greetingtts3);
                    displayText = mContext.getString(R.string.greeting21);
                    greetingTv.setTextColor(Color.WHITE);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);

                    //????????????  ??????
                    btnIv.setVisibility(View.VISIBLE);
                    arrowIv.setVisibility(View.GONE);//?????? ??????
                    settingsIv.setVisibility(View.GONE);// ?????? ??????
                    objectAnimator = ObjectAnimator.ofFloat(btnIv, "alpha", 0f, 1f);
                    objectAnimator.setDuration(1000);
                    objectAnimator.setRepeatCount(ValueAnimator.INFINITE);//????????????
                    objectAnimator.setRepeatMode(ValueAnimator.REVERSE);//
                    objectAnimator.start();

                    CURRENTSTEP = STEP2;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP3) {
                    //"??????????????????"??????????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_voice_wake_up), "??????");
                    showFireworks(false);
                    setFullWindowSize(true);

                    isSuccessSTEP3 = false;
                    times = 0;
                    loadAnimationResource();
                    rootFl.setBackgroundColor(Color.WHITE);

                    btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                    arrowIv.setVisibility(View.GONE);//?????? ??????
                    settingsIv.setVisibility(View.GONE); //?????? ??????
                    ttsText = mContext.getString(R.string.greetingtts6);
                    displayText = mContext.getString(R.string.greeting31);
                    greetingTv.setText(displayText);
                    greetingTv.setTextColor(Color.BLACK);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);

                    CURRENTSTEP = STEP3;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP4) {
                    //"???????????????"??????????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_voice_car_control), "??????");
                    showFireworks(false);
                    setFullWindowSize(true);

                    isSuccessSTEP4 = false;
                    times = 0;
                    loadAnimationResource();
                    rootFl.setBackgroundResource(R.mipmap.ac);

                    btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                    arrowIv.setVisibility(View.GONE);//?????? ??????
                    settingsIv.setVisibility(View.GONE);//?????? ??????
                    ttsText = mContext.getString(R.string.greetingtts10);
                    displayText = mContext.getString(R.string.greeting41);
                    greetingTv.setText(displayText);
                    greetingTv.setTextColor(Color.WHITE);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);

                    CURRENTSTEP = STEP4;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP5) {
                    //"??????????????????"??????????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_can_see_say), "??????");
                    showFireworks(false);
                    setFullWindowSize(false);

                    isSuccessSTEP5 = false;
                    times = 0;
                    loadAnimationResource();
                    rootFl.setBackgroundColor(Color.parseColor("#e0000000"));

                    btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                    arrowIv.setVisibility(View.GONE);//?????? ??????
                    arrowIv.setVisibility(View.VISIBLE);
                    settingsIv.setVisibility(View.GONE);//?????? ??????
                    ttsText = mContext.getString(R.string.greetingtts14);
                    displayText = mContext.getString(R.string.greeting51);
                    greetingTv.setText(displayText);
                    greetingTv.setTextColor(Color.WHITE);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);

                    CURRENTSTEP = STEP5;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP6) {
                    //"?????????????????????"??????????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_all_step_success), "????????????");
                    isSuccessSTEP3 = false;
                    isSuccessSTEP4 = false;
                    isSuccessSTEP5 = false;
                    CURRENTSTEP = STEP1;
                    goBt.callOnClick();
                }

                break;
            case R.id.pass_bt:
                if (CURRENTSTEP == STEP2) {
                    //"??????????????????"????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_VR), "??????");
                    showFireworks(false);
                    setFullWindowSize(true);

                    isSuccessSTEP2 = true;
                    isSuccessSTEP3 = false;
                    times = 0;
                    rootFl.setBackgroundColor(Color.WHITE);
                    loadAnimationResource();
                    starIv1.setBackgroundResource(R.mipmap.star_light);
                    ttsText = mContext.getString(R.string.greetingtts6);
                    displayText = mContext.getString(R.string.greeting31);

                    btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                    greetingTv.setTextColor(Color.BLACK);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);

                    CURRENTSTEP = STEP3;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP3) {
                    //"????????????????????????"????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_voice_wake_up), "??????");
                    showFireworks(false);
                    setFullWindowSize(true);

                    isSuccessSTEP3 = true;
                    isSuccessSTEP4 = false;
                    times = 0;
                    rootFl.setBackgroundResource(R.mipmap.ac);
                    starIv1.setBackgroundResource(R.mipmap.star_light);
                    starIv2.setBackgroundResource(R.mipmap.star_light);
                    loadAnimationResource();
                    ttsText = mContext.getString(R.string.greetingtts10);
                    displayText = mContext.getString(R.string.greeting41);

                    btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                    arrowIv.setVisibility(View.GONE);
                    greetingTv.setTextColor(Color.WHITE);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                    CURRENTSTEP = STEP4;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP4) {
                    //"???????????????"????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_voice_car_control), "??????");
                    showFireworks(false);
                    setFullWindowSize(false);

                    isSuccessSTEP4 = true;
                    isSuccessSTEP5 = false;
                    times = 0;
                    rootFl.setBackgroundColor(Color.parseColor("#e0000000"));

                    starIv1.setBackgroundResource(R.mipmap.star_light);
                    starIv2.setBackgroundResource(R.mipmap.star_light);
                    starIv3.setBackgroundResource(R.mipmap.star_light);
                    loadAnimationResource();
                    ttsText = mContext.getString(R.string.greetingtts14);
                    displayText = mContext.getString(R.string.greeting51);

                    btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                    greetingTv.setTextColor(Color.WHITE);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);

                    CURRENTSTEP = STEP5;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP5) {
                    //"????????????????????????"????????????????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_can_see_say), "??????");
                    showFireworks(false);
                    setFullWindowSize(true);

                    isSuccessSTEP5 = true;
                    times = 0;
                    rootFl.setBackgroundColor(Color.parseColor("#e0000000"));
                    starIv1.setBackgroundResource(R.mipmap.star_light);
                    starIv2.setBackgroundResource(R.mipmap.star_light);
                    starIv3.setBackgroundResource(R.mipmap.star_light);
                    starIv4.setBackgroundResource(R.mipmap.star_light);
                    loadAnimationResource();
                    ttsText = mContext.getString(R.string.greetingtts21);
                    displayText = mContext.getString(R.string.greeting61);

                    btnIv.setVisibility(View.GONE);//?????? ?????? ??????
                    arrowIv.setVisibility(View.GONE);//?????? ??????
                    greetingTv.setTextColor(Color.WHITE);
                    tiaoZiUtil = new TiaoZiUtil(greetingTv, displayText, wordDuration);
                    CURRENTSTEP = STEP6;
                    setAllButtonStatus(false);//??????????????????
                    startSpeak(CURRENTSTEP, ttsText);
                } else if (CURRENTSTEP == STEP6) {

                }
                break;
            case R.id.exit_bt:
                if(CURRENTSTEP == STEP1){//"?????????????????????"?????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_welcome), "??????");
                } else if (CURRENTSTEP == STEP6) {//"???????????????"???????????????
                    DatastatManager.getInstance().recordUI_event(mContext, getString(R.string.event_id_all_step_success), "????????????");
                }

                SharedPreferencesUtils.saveInt(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY, 1);
                SharedPreferencesUtils.saveBoolean(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
                finish();
                break;
        }

    }

    /*
    *   ??????????????????????????????
    *   @param flag
    *   true:????????? false???????????????????????????????????????
     */
    public void setFullWindowSize(boolean flag) {
        Window window = getWindow();
        WindowManager.LayoutParams windowLayoutParams = window.getAttributes(); // ?????????????????????????????????
        if (flag) {
            windowLayoutParams.width = fullScreenSizeWidth; // ?????????????????????
        } else {
            windowLayoutParams.width = fullScreenSizeWidth - 130;  // ?????????????????????????????????130
        }
        windowLayoutParams.height = fullScreenSizeHeight;
        windowLayoutParams.gravity = Gravity.LEFT;
        window.setAttributes(windowLayoutParams);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtils.saveInt(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY, 1);
        SharedPreferencesUtils.saveBoolean(mContext, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY, false);
        mContext.unregisterReceiver(receiver);
        mHandler.removeCallbacks(null);
    }
}
