package com.chinatsp.ifly.module.me.settings.view.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.module.me.settings.contract.AnswerSettingsContract;
import com.chinatsp.ifly.module.me.settings.presenter.AnswerSettingsPresenter;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.view.ClearEditText;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

public class AnswerSettingsFragment extends BaseFragment implements AnswerSettingsContract.View,
        ClearEditText.InputTextListener, View.OnClickListener {
    @BindView(R.id.iv_answer_back)
    ImageView iv_answer_back;
    @BindView(R.id.clear_edittext)
    ClearEditText clearEdittext;
    @BindView(R.id.tv_answer_tip)
    TextView tvAnswerTip;
    @BindView(R.id.btn_answer_restore)
    Button btnAnswerRestore;
    @BindView(R.id.btn_answer_confirm)
    Button btnAnswerConfirm;
    @BindView(R.id.btn_answer_cancel)
    Button btnAnswerCancel;
    @BindView(R.id.iv_setting)
    ImageView ivBack;
    @BindView(R.id.tv_deputy_message2)
    TextView mTvMessage;

    private SettingsActivity activity;
    private Context appContext;
    private AnswerSettingsContract.Presenter presenter = new AnswerSettingsPresenter(this);
//    private static final String CHINESE_REG = "[\\u4e00-\\u9fa5]{1,8}";//表示+表示一个或多个中文
    private static final String CHINESE_REG = "[\\u4e00-\\u9fa5_a-zA-Z0-9]{1,8}";//表示+表示一个或多个中文
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
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.EventType.SPEECHING,
                "输入应答语或返回","","",0 ));
    }

    @Override
    protected void initData() {
        clearEdittext.setHint(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_ANSWER, AppConstant.DEFAULT_VALUE_CURRENT_ANSWER));
//        String text = SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_ANSWER, AppConstant.DEFAULT_VALUE_CURRENT_ANSWER);
//        if(text!=null&&!"".equals(text))
//            clearEdittext.setText(text);
        String tip = getString(R.string.ask_set_name_02);
        SpannableStringBuilder span = SpannableUtils.formatString(tip, 0, 6, Color.parseColor("#00a1ff"));
        mTvMessage.setText(span);
    }

    @Override
    protected void initListener() {
        clearEdittext.setInputTextListener(this);
        ivBack.setOnClickListener(this);
    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_settings_answer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
    }

    @OnClick({R.id.btn_answer_restore, R.id.btn_answer_confirm, R.id.btn_answer_cancel,R.id.iv_answer_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_answer_restore:
                SharedPreferencesUtils.saveString(appContext, AppConstant.KEY_CURRENT_ANSWER, AppConstant.DEFAULT_VALUE_CURRENT_ANSWER);
                clearEdittext.setText(AppConstant.DEFAULT_VALUE_CURRENT_ANSWER);
                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_settings_pre_answer_default), AppConstant.DEFAULT_VALUE_CURRENT_ANSWER);
                break;
            case R.id.btn_answer_confirm:
                String answerStr = clearEdittext.getText();

                if(answerStr==null){
                    activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                    return;
                }

                if (!TextUtils.isEmpty(answerStr) && answerStr.matches(CHINESE_REG)) {
                    SharedPreferencesUtils.saveString(appContext, AppConstant.KEY_CURRENT_ANSWER, answerStr);
                    activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                    DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_settings_pre_answer), answerStr);
                    tvAnswerTip.setTextColor(Color.parseColor("#999999"));
                } else {
                    return;
//                    SharedPreferencesUtils.saveString(appContext, AppConstant.KEY_CURRENT_ANSWER, answerStr);
//                    activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                }
                break;
            case R.id.btn_answer_cancel:
                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                break;
            case R.id.iv_answer_back:
                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                break;
        }
    }

    @Override
    public void afterTextChanged(String text) {
        Log.d("xyj", "callback afterTextChanged: " + text);
        if (!text.matches(CHINESE_REG)) {
//            if(text!=null&&!"".equals(text))
                tvAnswerTip.setTextColor(Color.RED);
//            else
//                tvAnswerTip.setTextColor(Color.parseColor("#999999"));
        } else {
            tvAnswerTip.setTextColor(Color.parseColor("#999999"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_setting:
                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                break;
        }
    }
}
