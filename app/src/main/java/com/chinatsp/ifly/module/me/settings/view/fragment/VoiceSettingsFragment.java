package com.chinatsp.ifly.module.me.settings.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinatsp.ifly.AppManager;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.VoiceSubSettingsEvent;
import com.chinatsp.ifly.module.me.settings.contract.VoiceSettingsContract;
import com.chinatsp.ifly.module.me.settings.presenter.VoiceSettingsPresenter;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.MyToast;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;

import static com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager.KEY_VOICE_NAV;
import static com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager.VALUE_VOICE_NAV_AMAP;
import static com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager.VALUE_VOICE_NAV_DEFAULT;

public class VoiceSettingsFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, VoiceSettingsContract.View {
    private final static String TAG ="VoiceSettingsFragment";
    @BindView(R.id.cb_voice_guide)
    CheckBox cbVoiceGuide;
    @BindView(R.id.cb_voice_broadcast)
    CheckBox cbVoiceBroadcast;
    @BindView(R.id.cb_voice_position)
    CheckBox cbVoicePosition;
    @BindView(R.id.tv_settings_pre_answer)
    TextView tvSettingsAnswer;
    @BindView(R.id.tv_settings_aware)
    TextView tvSettingsAware;
    @BindView(R.id.iv_setting)
    ImageView ivBack;
    @BindView(R.id.tv_deputy_message2)
    TextView mTvMessage;

    @BindView(R.id.rl_root)
    RelativeLayout rl_root;

    @BindView(R.id.ll_control)
    LinearLayout ll_control;

    private SettingsActivity activity;
    private Context appContext;
    private VoiceSettingsContract.Presenter presenter = new VoiceSettingsPresenter(this);
    private SRAgent srAgent = SRAgent.getInstance();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
        appContext = context.getApplicationContext();
        presenter.bindActivity(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.subscribe();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void initData() {
        boolean guide = SharedPreferencesUtils.getBoolean(appContext, AppConstant.KEY_VOICE_GUIDE,AppConstant.VALUE_VOICE_GUIDE);
        Settings.System.putInt(activity.getContentResolver(),  AppConstant.KEY_VOICE_GUIDE, guide==true?1:0);
        cbVoiceGuide.setChecked(guide);

        boolean broadcast = SharedPreferencesUtils.getBoolean(appContext, AppConstant.KEY_VOICE_BROADCAST,AppConstant.VALUE_VOICE_SPEAK);
        cbVoiceBroadcast.setChecked(broadcast);

        boolean spot_talk = SharedPreferencesUtils.getBoolean(appContext, AppConstant.KEY_SPOT_TALK,AppConstant.VALUE_SPOT_TALK);
        cbVoicePosition.setChecked(spot_talk);
        Log.d(TAG,"broadcast ="+broadcast+",guide="+guide+",spot_talk="+spot_talk);
    }

    @Override
    protected void initListener() {
        cbVoiceBroadcast.setOnCheckedChangeListener(this);
        cbVoiceGuide.setOnCheckedChangeListener(this);
        cbVoicePosition.setOnCheckedChangeListener(this);
        tvSettingsAnswer.setOnClickListener(this);
        tvSettingsAware.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }

    @Override
    protected void initView(View view) {

        String tip = getString(R.string.ask_set_name_02);
        SpannableStringBuilder span = SpannableUtils.formatString(tip, 0, 13, Color.parseColor("#00a1ff"));
        mTvMessage.setText(span);

//        tvSettingsAware.setText(MvwKeywordsUtil.getCurrentName(getContext()));
//
//        boolean answerSwitch =  SharedPreferencesUtils.getBoolean(appContext, AppConstant.KEY_SWITCH_ANSWER,true);
//        cbSwitch.setChecked(answerSwitch);

        /*********************欧尚修改开始**********************/
        //初始化"语音导航"单选按钮
        //initVoiceNavRadioButton();
        /*********************欧尚修改结束**********************/
    }

    public void updateVoiceData(){
        tvSettingsAware.setText(MvwKeywordsUtil.getCurrentName(getContext()));
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_voice_settings;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.cb_voice_guide:
                SharedPreferencesUtils.saveBoolean(appContext, AppConstant.KEY_VOICE_GUIDE,isChecked);
                //cbVoiceGuide.isChecked(isChecked);
                Settings.System.putInt(activity.getContentResolver(),  AppConstant.KEY_VOICE_GUIDE, isChecked==true?1:0);
                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_cb_voice_guide), isChecked ? "开" : "关");
                break;
            case R.id.cb_voice_broadcast:
                SharedPreferencesUtils.saveBoolean(appContext, AppConstant.KEY_VOICE_BROADCAST,isChecked);
                //cbVoiceBroadcast.setClickable(isChecked);
                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_cb_voice_broadcast), isChecked ? "开" : "关");
                break;
                //修改为边说边识别
            case R.id.cb_voice_position:
                SharedPreferencesUtils.saveBoolean(appContext, AppConstant.KEY_SPOT_TALK,isChecked);
                if(isChecked)
                    SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_PGS, SrSession.ISS_SR_PARAM_VALUE_ON);
                else
                    SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_PGS, SrSession.ISS_SR_PARAM_VALUE_OFF);
                //cbVoicePosition.setClickable(isChecked);
                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_cb_voice_position), isChecked ? "开" : "关");
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_settings_pre_answer:
                activity.switchFragment(AppConstant.VOICE_SUBSETTINGS_ANSWER_ID);
//                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_cb_voice_guide), isChecked ? "开" : "关");
                break;
            case R.id.tv_settings_aware:
                activity.switchFragment(AppConstant.VOICE_SUBSETTINGS_AWARE_ID);
//                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_cb_voice_guide), isChecked ? "开" : "关");
                break;
            case R.id.iv_setting:
                activity.finish();
                break;

        }
    }

    @Subscribe
    public void enterVoiceSubSettings(VoiceSubSettingsEvent subSettingsEvent) {
        switch (subSettingsEvent.item) {
            case ANSWER_SETTING:
                tvSettingsAnswer.performClick();
                break;
            case RETURN:
                int fragmentId = activity.getCurrentFragmentId();
                if (fragmentId == AppConstant.VOICE_SETTINGS_ID || fragmentId == AppConstant.VOICE_HELPER_ID) {
                    AppManager.getAppManager().finishActivity();
                    appContext.sendBroadcast(new Intent(AppConstant.ACTION_SHOW_ASSISTANT));
                } else {
                    activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                }
                break;
            case AWARE_SETTING:
                tvSettingsAware.performClick();
                break;

        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            //tvSettingsActor.setText(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_ACTOR, AppConstant.DEFAULT_VALUE_CURRENT_ACTOR));
//            int whichName = SharedPreferencesUtils.getInt(appContext, AppConstant.KEY_WHICH_NAME, 0);
//            if (whichName == 0) {
//                tvSettingsAware.setText(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_1, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1));
//            } else if((whichName == 1)){
//                tvSettingsAware.setText(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_2, AppConstant.DEFAULT_VALUE_CURRENT_NAME_2));
//            } else if((whichName == 2)){
                tvSettingsAware.setText(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_3, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1));
//            } else
//                tvSettingsAware.setText(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_1, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1));
            tvSettingsAnswer.setText(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_ANSWER, AppConstant.DEFAULT_VALUE_CURRENT_ANSWER));

         /*   String stkCmd = Utils.getFromAssets(appContext, "stks/voice_setting.json");
            srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stkCmd);
            srAgent.stopSRSession();
            srAgent.startSRSession();

            boolean answerSwitch =  SharedPreferencesUtils.getBoolean(appContext, AppConstant.KEY_SWITCH_ANSWER,true);
            //cbSwitch.setChecked(answerSwitch);
            tvSettingsAnswer.setClickable(answerSwitch);

            //String tipStr = "试着说\n“ 应答语 ” \n“ 返回 ” \n“ 修改唤醒词 ”";
            //SpannableStringBuilder span = SpannableUtils.formatString(tipStr, 0, 3, Color.parseColor("#999999"), 4, tipStr.length(), Color.parseColor("#00a1ff"));
            String tipStr = "试着通过\n“你好小欧”唤醒我";
            SpannableStringBuilder span = SpannableUtils.formatString(tipStr, 5, 11, Color.parseColor("#00a1ff"));
            tvSettingsAnswer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EventBusUtils.sendMainMessage(span);
                    EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_NORMAL, null);
//                    TTSController.getInstance(appContext).stopTTS();
                }
            }, 500);*/
        }
    }
}
