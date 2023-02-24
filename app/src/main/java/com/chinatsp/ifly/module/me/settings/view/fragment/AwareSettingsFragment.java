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
import android.widget.Toast;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.module.me.settings.contract.AwareSettingsContract;
import com.chinatsp.ifly.module.me.settings.presenter.AwareSettingsPresenter;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.MyToast;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.view.ClearEditText;
import com.chinatsp.ifly.voice.platformadapter.utils.MvwKeywordsUtil;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.mvw.MvwSession;

import butterknife.BindView;
import butterknife.OnClick;

public class AwareSettingsFragment extends BaseFragment implements AwareSettingsContract.View,
        ClearEditText.InputTextListener, View.OnClickListener {
    @BindView(R.id.iv_answer_back)
    ImageView iv_answer_back;
    @BindView(R.id.clear_edittext)
    ClearEditText clearEdittext;
    @BindView(R.id.tv_aware_tip)
    TextView tvAwareTip;
    @BindView(R.id.btn_aware_restore)
    Button btnAwareRestore;
    @BindView(R.id.btn_aware_confirm)
    Button btnAwareConfirm;
    @BindView(R.id.btn_aware_cancel)
    Button btnAwareCancel;
    @BindView(R.id.iv_setting)
    ImageView ivBack;
    @BindView(R.id.tv_deputy_message2)
    TextView mTvMessage;

    private SettingsActivity activity;
    private Context appContext;
    private AwareSettingsContract.Presenter presenter = new AwareSettingsPresenter(this);
    private static final String CHINESE_REG = "[\\u4e00-\\u9fa5]{2,5}";//表示+表示一个或多个中文
    private int whichName = 0;

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
        whichName = SharedPreferencesUtils.getInt(appContext, AppConstant.KEY_WHICH_NAME, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
//        EventBusUtils.sendMainMessage("输入新的唤醒词或返回");
    }

    @Override
    protected void initData() {
        clearEdittext.setHint(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_3, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1));
        String tip = getString(R.string.ask_set_name_02);
        SpannableStringBuilder span = SpannableUtils.formatString(tip, 0, 6, Color.parseColor("#00a1ff"));
        mTvMessage.setText(span);
//        if (whichName == 0) {
//            clearEdittext.setHint(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_1, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1));
//        } else if(whichName == 1){
//            clearEdittext.setHint(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_2, AppConstant.DEFAULT_VALUE_CURRENT_NAME_2));
//        } else if(whichName == 2){
//            clearEdittext.setHint(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_3, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1));
//        }else
//            clearEdittext.setHint(SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_NAME_1, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1));
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
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_settings_aware;
    }


    @OnClick({R.id.btn_aware_restore, R.id.btn_aware_confirm, R.id.btn_aware_cancel,R.id.iv_answer_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_aware_restore:
//                if (whichName == 0) {
//                    SharedPreferencesUtils.saveString(appContext, AppConstant.KEY_CURRENT_NAME_1, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1);
//                    clearEdittext.setHint(AppConstant.DEFAULT_VALUE_CURRENT_NAME_1);
//                } else if(whichName == 1) {
//                    SharedPreferencesUtils.saveString(appContext, AppConstant.KEY_CURRENT_NAME_2, AppConstant.DEFAULT_VALUE_CURRENT_NAME_2);
//                    clearEdittext.setHint(AppConstant.DEFAULT_VALUE_CURRENT_NAME_2);
//                } else {
                    SharedPreferencesUtils.saveString(appContext, AppConstant.KEY_CURRENT_NAME_3, AppConstant.DEFAULT_VALUE_CURRENT_NAME_1);
                    clearEdittext.setHint(AppConstant.DEFAULT_VALUE_CURRENT_NAME_1);
                    SharedPreferencesUtils.saveInt(appContext, AppConstant.KEY_WHICH_NAME, 0);
//                }
                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_settings_aware_default), AppConstant.DEFAULT_VALUE_CURRENT_NAME_1);
                MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_CUSTOME);
                MVWAgent.getInstance().stopMVWSession();
                MVWAgent.getInstance().startMVWSession(TspSceneAdapter.getTspScene(appContext));  //恢复上一次的场景

//                }
                break;
            case R.id.btn_aware_confirm:
                String productName = clearEdittext.getText();

                if (!TextUtils.isEmpty(productName) && !productName.matches(CHINESE_REG)) {
//                    Toast.makeText(getContext(), "shibai", Toast.LENGTH_LONG).show();
                    return;
                }
//                if(productName!=null&&!productName.trim().isEmpty()){
//                    MyToast.showToast(appContext,"抱歉，你好不能设置为唤醒词", true);
//                    return;
//                }
                if(productName!=null&&!productName.trim().isEmpty()){  //取名字
//                    productName.replace("你好", "");
                    DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_settings_aware), productName);

                    String  globalStr = MvwKeywordsUtil.addMvwKeywordJson(appContext,productName);  //在方法里面保存有KEY_CURRENT_NAME_3 的值
                    MVWAgent.getInstance().setMvwDefaultKey(MvwSession.ISS_MVW_SCENE_CUSTOME);
                    MVWAgent.getInstance().setMvwKeyWords(MvwSession.ISS_MVW_SCENE_CUSTOME,
                            globalStr);
                    MVWAgent.getInstance().stopMVWSession();
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.getTspScene(appContext));  //恢复上一次的场景
                    SharedPreferencesUtils.saveInt(appContext, AppConstant.KEY_WHICH_NAME, 2);
                }  //如果没取名字保持默认名字索引
                activity.updateVoiceData();
                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                break;
            case R.id.btn_aware_cancel:
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
//        if (!text.matches(CHINESE_REG) && text.length() != 0) {
        if (!text.matches(CHINESE_REG)) {
            String tip = getString(R.string.tip_revise_aware);
            int start = tip.indexOf("1.");
            int end = tip.indexOf("2.");
            SpannableStringBuilder span = SpannableUtils.formatString(tip, start, end, Color.parseColor("#ff0000"));
            tvAwareTip.setText(span);
        } else {
            tvAwareTip.setText(R.string.tip_revise_aware);
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
