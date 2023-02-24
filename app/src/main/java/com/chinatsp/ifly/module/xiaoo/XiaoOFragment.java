package com.chinatsp.ifly.module.xiaoo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.utils.AppConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;

public class XiaoOFragment extends BaseFragment implements XiaoOContract.View {

    @BindView(R.id.iv_xiaoo)
    ImageView ivXiaoo;
    @BindView(R.id.iv_xiaoo_voice)
    LinearLayout ivXiaooVoice;
    @BindView(R.id.tv_xiaoo_talk)
    TextView tvXiaooTalk;
    @BindView(R.id.iv_xiaoo_setting)
    ImageView ivXiaooSetting;
    @BindView(R.id.tv_xiaoo_prompt)
    TextView tvXiaooPrompt;

    private Activity activity;
    private XiaoOContract.Presenter presenter = new XiaoOPresenter(this);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
        EventBus.getDefault().unregister(this);
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
    protected int getContentView() {
        return R.layout.fragment_xiaoo;
    }

    @Subscribe
    public void onEvent(MessageEvent event) {
        if(event.eventType == MessageEvent.EventType.SPEECHING) {
//            tvXiaooPrompt.setText(event.mainMessage);
        }
    }

    @OnClick({R.id.iv_xiaoo_voice, R.id.iv_xiaoo_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_xiaoo_voice:
                Toast.makeText(activity, "快说啊...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_xiaoo_setting:
                Toast.makeText(activity, "跳转设置...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }
}
