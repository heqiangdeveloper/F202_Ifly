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
import android.widget.TextView;
import android.widget.Toast;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.module.me.settings.contract.AwareSettingsContract;
import com.chinatsp.ifly.module.me.settings.contract.LogSettingsContract;
import com.chinatsp.ifly.module.me.settings.presenter.AwareSettingsPresenter;
import com.chinatsp.ifly.module.me.settings.presenter.LogSettingsPresenter;
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

public class LogSettingsFragment extends BaseFragment implements LogSettingsContract.View {

    private SettingsActivity activity;
    private Context appContext;
    private LogSettingsPresenter presenter = new LogSettingsPresenter(this);

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
//        EventBusUtils.sendMainMessage("输入新的唤醒词或返回");
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void initListener() {

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
        return R.layout.fragment_settings_log;
    }


    @OnClick({})
    public void onViewClicked(View view) {
        switch (view.getId()) {

        }
    }
}
