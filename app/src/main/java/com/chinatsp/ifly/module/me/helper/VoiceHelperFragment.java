package com.chinatsp.ifly.module.me.helper;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.entity.VoiceSubSettingsEvent;
import com.chinatsp.ifly.module.me.settings.view.adapter.VoiceHelperAdapter;
import com.chinatsp.ifly.module.me.settings.view.entity.VoiceHelperModel;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.SpannableUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;

public class VoiceHelperFragment extends BaseFragment implements VoiceHelperContract.View {
    private final static String TAG = "VoiceHelperFragment";
    private SettingsActivity activity;
    private Context appContext;
    private VoiceHelperContract.Presenter presenter = new VoiceHelperPresenter(this);
    @BindView(R.id.gv_helper)
    GridView mGridViewHelper;
    @BindView(R.id.tv_voice_helper_title)
    TextView tvHeaderTitle;
    private VoiceHelperAdapter voiceHelperAdapter;
    private int currentPosition = -1;
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
//        EventBusUtils.sendMainMessage("说出新的唤醒词或取消");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initData() {
        final List<VoiceHelperModel> voiceHelperModelList = new ArrayList<>();
        voiceHelperModelList.add(initNavi(0));
        voiceHelperModelList.add(initNavi(1));
        voiceHelperModelList.add(initNavi(2));
        voiceHelperModelList.add(initNavi(3));
        voiceHelperModelList.add(initNavi(4));
        voiceHelperModelList.add(initNavi(5));
//        voiceHelperModelList.add(initNavi(6));
        voiceHelperModelList.add(initNavi(7));
//        voiceHelperModelList.add(initNavi(8));
//        voiceHelperModelList.add(initNavi(9));
//        voiceHelperModelList.add(initNavi(10));

        String text = activity.getResources().getString(R.string.voice_helper_hint);
        SpannableStringBuilder string = SpannableUtils.formatString(text, 5, 13,  Color.parseColor("#00a1ff"), 17, text.length(), Color.parseColor("#00a1ff"));
        tvHeaderTitle.setText(string);

        voiceHelperAdapter = new VoiceHelperAdapter(getContext(), voiceHelperModelList);
        mGridViewHelper.setAdapter(voiceHelperAdapter);
        mGridViewHelper.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "lh:onItemSelected-" + position + ",id=" + id);
                voiceHelperModelList.get(position).isCheck = true;
                if (currentPosition != -1) {
                    voiceHelperModelList.get(currentPosition).isCheck = false;
                }
                currentPosition = position;
                voiceHelperAdapter.notifyDataSetChanged();
            }
        });
    }

    private VoiceHelperModel initNavi(int flag) {
        VoiceHelperModel voiceHelperModel = new VoiceHelperModel();
        voiceHelperModel.isCheck = false;
        if (flag == 0) { //导航
            String[] navi = getContext().getResources().getStringArray(R.array.welcome_navi);
            voiceHelperModel.functionName = getString(R.string.funcition_navi);
            String[] tmpNavi = getRandomArray(4, navi);
            if (tmpNavi != null || tmpNavi.length >= 4) {
                voiceHelperModel.action1 = tmpNavi[0];
                voiceHelperModel.action2 = tmpNavi[1];
                voiceHelperModel.action3 = tmpNavi[2];
                voiceHelperModel.action4 = tmpNavi[3];
            }
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_navi);
        } else if (flag == 1) {//音乐
            String[] music = getContext().getResources().getStringArray(R.array.welcome_music);
            voiceHelperModel.functionName = getString(R.string.funcition_music);
            String[] tmpMusic = getRandomArray(4, music);
            if (tmpMusic != null || tmpMusic.length >= 4) {
                voiceHelperModel.action1 = tmpMusic[0];
                voiceHelperModel.action2 = tmpMusic[1];
                voiceHelperModel.action3 = tmpMusic[2];
                voiceHelperModel.action4 = tmpMusic[3];
            }
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_music);
        } else if (flag == 2) { //电台
            String[] radio = getContext().getResources().getStringArray(R.array.welcome_radio);
            voiceHelperModel.functionName = getString(R.string.funcition_radio);
            String[] tmpRadio = getRandomArray(4, radio);
            if (tmpRadio != null || tmpRadio.length >= 4) {
                voiceHelperModel.action1 = tmpRadio[0];
                voiceHelperModel.action2 = tmpRadio[1];
                voiceHelperModel.action3 = tmpRadio[2];
                voiceHelperModel.action4 = tmpRadio[3];
            }
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_radio);
        } else if (flag == 3) {//电话
            String[] tel = getContext().getResources().getStringArray(R.array.welcome_tel);
            voiceHelperModel.functionName = getString(R.string.funcition_tel);
            voiceHelperModel.action1 = tel[0];
            voiceHelperModel.action2 = tel[1];
            voiceHelperModel.action3 = tel[2];
            voiceHelperModel.action4 = tel[3];
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_tel);
        } else if (flag == 4) {//车辆控制
            String[] car_control = getContext().getResources().getStringArray(R.array.welcome_car_control);
            voiceHelperModel.functionName = getString(R.string.funcition_car_control);
            String[] tmpCarControl = getRandomArray(4, car_control);
            if (tmpCarControl != null || tmpCarControl.length >= 4) {
                voiceHelperModel.action1 = tmpCarControl[0];
                voiceHelperModel.action2 = tmpCarControl[1];
                voiceHelperModel.action3 = tmpCarControl[2];
                voiceHelperModel.action4 = tmpCarControl[3];
            }
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_car_control);
        } else if (flag == 5) {//系统控制
            String[] system_control = getContext().getResources().getStringArray(R.array.welcome_system_control);
            voiceHelperModel.functionName = getString(R.string.funcition_system_control);
            String[] tmpSystemControl = getRandomArray(4, system_control);
            if (tmpSystemControl != null || tmpSystemControl.length >= 4) {
                voiceHelperModel.action1 = tmpSystemControl[0];
                voiceHelperModel.action2 = tmpSystemControl[1];
                voiceHelperModel.action3 = tmpSystemControl[2];
                voiceHelperModel.action4 = tmpSystemControl[3];
            }
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_system_control);
        } else if (flag == 6) {//车信
            String[] chexin = getContext().getResources().getStringArray(R.array.welcome_chexin);
            voiceHelperModel.functionName = getString(R.string.funcition_chexin);
            String[] tmpChexin = getRandomArray(4, chexin);
            if (tmpChexin != null || tmpChexin.length >= 4) {
                voiceHelperModel.action1 = tmpChexin[0];
                voiceHelperModel.action2 = tmpChexin[1];
                voiceHelperModel.action3 = tmpChexin[2];
                voiceHelperModel.action4 = tmpChexin[3];
            }
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_chexin);
        } else if (flag == 7) {//资讯查询
            String[] news = getContext().getResources().getStringArray(R.array.welcome_news);
            voiceHelperModel.functionName = getString(R.string.funcition_news);
            String[] tmpNews = getRandomArray(4, news);
            if (tmpNews != null || tmpNews.length >= 4) {
                voiceHelperModel.action1 = tmpNews[0];
                voiceHelperModel.action2 = tmpNews[1];
                voiceHelperModel.action3 = tmpNews[2];
                voiceHelperModel.action4 = tmpNews[3];
            }
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_news);
        } else if (flag == 8) {//车辆百科
            String[] car_baike = getContext().getResources().getStringArray(R.array.welcome_cars_baike);
            voiceHelperModel.functionName = getString(R.string.funcition_cars_baike);
            voiceHelperModel.action1 = car_baike[0];
            voiceHelperModel.action2 = car_baike[1];
            voiceHelperModel.action3 = car_baike[2];
            voiceHelperModel.action4 = car_baike[3];
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_baike);
        }else if (flag == 9){//按键查询
            String[] key_search = getContext().getResources().getStringArray(R.array.welcome_key_search);
            voiceHelperModel.functionName = getString(R.string.function_key_search);
            voiceHelperModel.action1 = key_search[0];
            voiceHelperModel.action2 = key_search[1];
            voiceHelperModel.action3 = key_search[2];
            voiceHelperModel.action4 = key_search[3];
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_key_search);
        }else if (flag == 10) {//吐槽
            String[] tucao = getContext().getResources().getStringArray(R.array.welcome_tucao);
            voiceHelperModel.functionName = getString(R.string.funcition_tucao);
            voiceHelperModel.action1 = tucao[0];
            voiceHelperModel.action2 = tucao[1];
            voiceHelperModel.action3 = tucao[2];
            voiceHelperModel.action4 = tucao[3];
            voiceHelperModel.picDrawable = getContext().getDrawable(R.drawable.pic_tucao);
        }
        return voiceHelperModel;
    }

    /**
     * 使用一个List来保存数组，每次随机取出一个移除一个。
     */
    public String[] getRandomArray(int n, String[] strArray) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < strArray.length; i++) {
            list.add(strArray[i]);
        }
        Random random = new Random();

        // 当取出的元素个数大于数组的长度时，返回null
        if (n > list.size()) {
            return null;
        }

        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            // 去一个随机数，随机范围是list的长度
            int index = random.nextInt(list.size());
            result[i] = list.get(index);
            list.remove(index);
        }
        return result;
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_voice_helper;
    }

    @Subscribe
    public void enterVoiceSubSettings(VoiceSubSettingsEvent subSettingsEvent) {
        switch (subSettingsEvent.item) {
            case RETURN:
                activity.switchFragment(AppConstant.VOICE_SETTINGS_ID);
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("xyj", "VoiceSettings  onHiddenChanged:" + hidden);
        if (!hidden) {

            String stkCmd = Utils.getFromAssets(appContext, "stks/voice_helper.json");
            srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stkCmd);
            srAgent.stopSRSession();
            srAgent.startSRSession();

            String tipStr = "试着说\n“ 返回 ”";
            TTSController.getInstance(appContext).stopTTS();
//            TTSController.getInstance(appContext).startTTS(tipStr);
            SpannableStringBuilder span = SpannableUtils.formatString(tipStr, 0, 3, Color.parseColor("#999999"), 4, tipStr.length(), Color.parseColor("#00a1ff"));
            EventBusUtils.sendMainMessage(span);
        }
    }
}
