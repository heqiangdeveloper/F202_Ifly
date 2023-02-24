package com.chinatsp.ifly.module.me.settings.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.module.me.settings.contract.ActorSettingsContract;
import com.chinatsp.ifly.module.me.settings.presenter.ActorSettingsPresenter;
import com.chinatsp.ifly.module.me.settings.view.adapter.ActorSettingsAdapter;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.RecycleViewItemLine;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.iflytek.tts.ESpeaker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ActorSettingsFragment extends BaseFragment implements ActorSettingsContract.View {

    private static final String TAG = "ActorSettingsFragment";

    @BindView(R.id.btn_actor_confirm)
    Button btnActorConfirm;
    @BindView(R.id.btn_actor_cancel)
    Button btnActorCancel;
    @BindView(R.id.actor_recyclerView)
    RecyclerView actorRecyclerView;

    private SettingsActivity activity;
    private Context appContext;
    private ActorSettingsContract.Presenter presenter = new ActorSettingsPresenter(this);
    private List<String> mActors;
    private ActorSettingsAdapter mAdapter;
    private String currentActor;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingsActivity) context;
        appContext = context.getApplicationContext();
        presenter.bindActivity(activity);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("xyj", "ActorSettings onHiddenChanged: " + hidden);
        if(!hidden) {
            if(currentActor != null && currentActor.length() > 0) {
                currentActor = SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_ACTOR, mActors.get(0));
                int index = mActors.indexOf(currentActor);
                mAdapter.setCurrentActor(index);

                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.subscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBusUtils.sendMainMessage("随时说 “ 返回 ” ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
    }

    @Override
    protected void initData() {
        List<String> actors = presenter.getActors();
        mActors.clear();
        mActors.addAll(actors);

        currentActor = SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_ACTOR, mActors.get(0));
        int index = mActors.indexOf(currentActor);
        mAdapter.setCurrentActor(index);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {
        actorRecyclerView.setNestedScrollingEnabled(false);
        actorRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        RecycleViewItemLine line = new RecycleViewItemLine(activity, RecyclerView.HORIZONTAL, R.drawable.ic_divider_line);
        line.setHeaderDividerEnabled(true);
        actorRecyclerView.addItemDecoration(line);
        mActors = new ArrayList<>();
        mAdapter = new ActorSettingsAdapter(mActors);
        actorRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentActor = mActors.get(position);
                int index = mActors.indexOf(currentActor);
                setSpeaker(index);
                Utils.startTTSOnly(Utils.actorToString(currentActor),null);
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_settings_actor;
    }

    @OnClick({R.id.btn_actor_confirm, R.id.btn_actor_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_actor_confirm:
                SharedPreferencesUtils.saveString(appContext, AppConstant.KEY_CURRENT_ACTOR, currentActor);
                Settings.System.putString(activity.getContentResolver(), AppConstant.KEY_CURRENT_ACTOR, currentActor);  //与 ttsmanager 同步
//                int index = mActors.indexOf(currentActor);
//                setSpeaker(index);
//                Utils.startTTSOnly(Utils.actorToString(currentActor),null);
//                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
             /*   Utils.startTTS(Utils.actorToString(currentActor), new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                    }
                });*/
                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                DatastatManager.getInstance().recordUI_event(appContext, getString(R.string.event_id_actor_word), currentActor);
                break;
            case R.id.btn_actor_cancel:
                String actor = SharedPreferencesUtils.getString(appContext, AppConstant.KEY_CURRENT_ACTOR, mActors.get(0));
                int index = mActors.indexOf(actor);
                setSpeaker(index);
                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                break;
        }
    }

    private void setSpeaker(int i) {
        TTSController ttsController = TTSController.getInstance(appContext);
        switch (i) {
            case 0:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_TXZ);
                Log.d(TAG, "ivTTS_ROLE_TXZ");//小欧
                break;

            case 1:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_JIAJIA);
                Log.d(TAG, "ivTTS_ROLE_JIAJIA"); //嘉嘉
                break;
            case 2:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOYAN);
                Log.d(TAG, "ivTTS_ROLE_XIAOYAN"); //小燕
                break;
            case 3:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOFENG); //小凤
                Log.d(TAG, "ivTTS_ROLE_XIAOFENG");
                break;
            case 4:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_NANNAN); //楠楠
                Log.d(TAG, "ivTTS_ROLE_NANNAN");
                break;
            case 5:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOQIAN); //小倩
                Log.d(TAG, "ivTTS_ROLE_XIAOQIAN");
                break;
            case 6:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAORONG); //晓蓉
                Log.d(TAG, "ivTTS_ROLE_XIAORONG");
                break;
            case 7:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOMEI); //小美
                Log.d(TAG, "ivTTS_ROLE_XIAOMEI");
                break;
            case 8:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOQIANG); //小强
                Log.d(TAG, "ivTTS_ROLE_XIAOQIANG");
                break;
            case 9:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOKUN); //小坤
                Log.d(TAG, "ivTTS_ROLE_XIAOKUN");
                break;
            case 10:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOLIN); //小琳
                Log.d(TAG, "ivTTS_ROLE_XIAOLIN");
                break;
            case 11:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOXUE); //小雪
                Log.d(TAG, "ivTTS_ROLE_XIAOXUE");
                break;
            case 12:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOSHI); //小师
                Log.d(TAG, "ivTTS_ROLE_XIAOSHI");
                break;
            case 13:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOJIE); //小结
                Log.d(TAG, "ivTTS_ROLE_XIAOJIE");
                break;
            case 14:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOMENG); //小梦
                Log.d(TAG, "ivTTS_ROLE_XIAOMENG");
                break;
            case 15:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_LAOMA); //老马
                Log.d(TAG, "ivTTS_ROLE_LAOMA");
                break;
            case 16:
                ttsController.setParam(ESpeaker.ISS_TTS_PARAM_SPEAKER,
                        ESpeaker.ivTTS_ROLE_XIAOJING); //小婧
                Log.d(TAG, "ivTTS_ROLE_XIAOJING");
                break;

        }
    }
}
