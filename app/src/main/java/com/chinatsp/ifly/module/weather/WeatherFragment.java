package com.chinatsp.ifly.module.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.entity.WeatherEntity;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.sr.SRAgent;

import java.util.List;
import butterknife.BindView;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;

public class WeatherFragment extends BaseFragment implements WeatherContract.View {
    private static final String TAG = "WeatherFragment";
    private static Context mContext;
    @BindView(R.id.tv_cur_temp)
    TextView tvCurTemp;
    @BindView(R.id.tv_max_temp)
    TextView tvMaxTemp;
    @BindView(R.id.tv_min_temp)
    TextView tvMinTemp;
    @BindView(R.id.tv_city)
    TextView tvCity;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.iv_weather_type)
    ImageView ivWeatherType;
    @BindView(R.id.tv_weather_type_desc)
    TextView tvWeatherTypeDesc;
    @BindView(R.id.tv_wind_dir_desc)
    TextView tvWindDirDesc;
    @BindView(R.id.tv_wind_power_desc)
    TextView tvWindPowerDesc;
    @BindView(R.id.tv_wash_car)
    TextView tvWashCar;
    @BindView(R.id.tv_wash_car_desc)
    TextView tvWashCarDesc;

    private FullScreenActivity activity;
    private WeatherContract.Presenter presenter = new WeatherPresenter(this);
    private List<WeatherEntity> mWeatherList;
    private Semantic mSemantic;
    private WeatherEntity mCurWeather;
    private final static int MSG_START_TTS = 1001;

    public static WeatherFragment newInstance(Context context, String dataList, String semantic) {
        mContext = context;
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FullScreenActivity.DATA_LIST_STR, dataList);
        bundle.putString(FullScreenActivity.SEMANTIC_STR, semantic);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FullScreenActivity) context;
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
//        EventBus.getDefault().post(new MessageEvent(MessageEvent.EventType.SPEECHING,
//                "你还可以查询明天的天气哦", "","",""));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.unSubscribe();
        MultiInterfaceUtils.getInstance(activity).uploadCmdDefaultData();
        handler.removeCallbacks(null);
    }

    @Override
    protected void initData() {
        int tmp = mCurWeather.getTemp();
        String conditionId = "";
        if (!TextUtils.isEmpty(mSemantic.slots.datetime.dateOrig) && "今天".equals(mSemantic.slots.datetime.dateOrig)) {
            if (mSemantic.slots.location != null && !TextUtils.isEmpty(mSemantic.slots.location.city) && !mSemantic.slots.location.city.equalsIgnoreCase("CURRENT_CITY")) {
                getTtsMessage(R.string.skill_weather, R.string.scene_weather, R.string.object_weather2, WEATHERC5CONDITION, R.string.condition_weather5);
            } else {
                getTtsMessage(R.string.skill_weather, R.string.scene_weather, R.string.object_weather1, WEATHERC1CONDITION, R.string.condition_weather1);
            }
        } else if (!TextUtils.isEmpty(mSemantic.slots.datetime.dateOrig) && "明天".equals(mSemantic.slots.datetime.dateOrig)) {
            if (mSemantic.slots.location != null && !TextUtils.isEmpty(mSemantic.slots.location.city) && !mSemantic.slots.location.city.equalsIgnoreCase("CURRENT_CITY")) {
                getTtsMessage(R.string.skill_weather, R.string.scene_weather, R.string.object_weather2, WEATHERC6CONDITION, R.string.condition_weather6);
            } else {
                getTtsMessage(R.string.skill_weather, R.string.scene_weather, R.string.object_weather1, WEATHERC2CONDITION, R.string.condition_weather2);
            }
            tmp = (mCurWeather.getLow() + mCurWeather.getHigh()) / 2;
        } else {
            tmp = (mCurWeather.getLow() + mCurWeather.getHigh()) / 2;
            if (mSemantic.slots.location != null && !TextUtils.isEmpty(mSemantic.slots.location.city) && !mSemantic.slots.location.city.equalsIgnoreCase("CURRENT_CITY")) {
                getTtsMessage(R.string.skill_weather, R.string.scene_weather, R.string.object_weather2, WEATHERC7CONDITION, R.string.condition_weather7);
            } else {
                getTtsMessage(R.string.skill_weather, R.string.scene_weather, R.string.object_weather1, WEATHERC3CONDITION, R.string.condition_weather7);
            }
        }

        tvCurTemp.setText("" + tmp);
        tvMaxTemp.setText(String.valueOf(mCurWeather.getHigh()));
        tvMinTemp.setText(String.valueOf(mCurWeather.getLow()));
        tvDate.setText(mSemantic.slots.datetime.dateOrig);
        tvCity.setText(mCurWeather.getCity());
        tvWeatherTypeDesc.setText(mCurWeather.getWeather());
        tvWindDirDesc.setText(mCurWeather.getWind());
        tvWindPowerDesc.setText(mCurWeather.getWindLevel() + "级");
        tvWashCarDesc.setText(mCurWeather.getAirQuality());
        updateWeather(mCurWeather.getWeather());
    }

    private void getTtsMessage(int appName, int scene, int object, String conditionId, int condition) {
        Utils.getMessageWithoutTtsSpeak(getContext(), conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = String.format(getString(R.string.weather_query_sucess), mCurWeather.getCity(), mSemantic.slots.datetime.dateOrig, mCurWeather.getWeather(), mCurWeather.getWind(), mCurWeather.getLow(), mCurWeather.getHigh(), mCurWeather.getAirQuality());
                } else {
                    defaultText = Utils.replaceTts(defaultText, "#CITY#", mCurWeather.getCity());
                    defaultText = Utils.replaceTts(defaultText, "#DESC#", mCurWeather.getWeather());
                    defaultText = Utils.replaceTts(defaultText, "#WIND#", mCurWeather.getWind());
                    defaultText = Utils.replaceTts(defaultText, "#LOWT#", mCurWeather.getLow() + "");
                    defaultText = Utils.replaceTts(defaultText, "#HIGHT#", mCurWeather.getHigh() + "");
                    defaultText = Utils.replaceTts(defaultText, "#AIR#", mCurWeather.getAirQuality());
                    defaultText = Utils.replaceTts(defaultText, "#DETE#", mSemantic.slots.datetime.dateOrig);
                }
                Utils.eventTrack(getContext(),appName,scene,object,conditionId,condition,defaultText);
                Message msg = handler.obtainMessage(MSG_START_TTS, defaultText);
                handler.sendMessage(msg);
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_TTS:
                    startTTS((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private void startTTS(String tts) {

        if(tts.equals(TTSController.getInstance(getActivity()).getTtsWords())
        &&TTSController.getInstance(getActivity()).isTtsPlaying())return;

        Utils.startTTS(tts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                Log.e("zheng","zheng  你还可以查询其他时间的天气哦");
                //重新计算超时
                SRAgent.getInstance().resetSrTimeCount();
                String text = "你还可以查询其他时间的天气哦";
                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, text);
              //  FloatViewManager.getInstance(getContext()).hide();
            }
        });
    }

    public void updateWeather(String weather) {
        if (weather.equals("晴")) {
            ivWeatherType.setImageResource(R.drawable.weather_qing);
        } else if (weather.equals("暴雨")) {
            ivWeatherType.setImageResource(R.drawable.weather_baoyu);
        } else if (weather.equals("暴雪")) {
            ivWeatherType.setImageResource(R.drawable.weather_baoxue);
        } else if (weather.equals("大雪")) {
            ivWeatherType.setImageResource(R.drawable.weather_daxue);
        } else if (weather.equals("大雨")) {
            ivWeatherType.setImageResource(R.drawable.weather_dayu);
        } else if (weather.equals("浮尘")) {
            ivWeatherType.setImageResource(R.drawable.weather_fuchen);
        } else if (weather.equals("雷阵雨")) {
            ivWeatherType.setImageResource(R.drawable.weather_leizhenyu);
        } else if (weather.equals("雷阵雨伴大到暴雨")) {
            ivWeatherType.setImageResource(R.drawable.weather_leizhenyubanyoubingbao);
        } else if (weather.equals("霾")) {
            ivWeatherType.setImageResource(R.drawable.weather_mai);
        } else if (weather.equals("沙尘暴")) {
            ivWeatherType.setImageResource(R.drawable.weather_shachenbao);
        } else if (weather.equals("雾")) {
            ivWeatherType.setImageResource(R.drawable.weather_wu);
        } else if (weather.equals("小雪")) {
            ivWeatherType.setImageResource(R.drawable.weather_xiaoxue);
        } else if (weather.equals("小雨")) {
            ivWeatherType.setImageResource(R.drawable.weather_xiaoyu);
        } else if (weather.equals("扬沙")) {
            ivWeatherType.setImageResource(R.drawable.weather_yangsha);
        } else if (weather.equals("阴")) {
            ivWeatherType.setImageResource(R.drawable.weather_yin);
        } else if (weather.equals("雨夹雪")) {
            ivWeatherType.setImageResource(R.drawable.weather_yujiaxue);
        } else if (weather.equals("阵雨")) {
            ivWeatherType.setImageResource(R.drawable.weather_zhenyu);
        } else if (weather.equals("中雪")) {
            ivWeatherType.setImageResource(R.drawable.weather_zhongxue);
        } else if (weather.equals("中雨")) {
            ivWeatherType.setImageResource(R.drawable.weather_zhongyu);
        }
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView(View view) {
        mSemantic = GsonUtil.stringToObject(getArguments().getString(FullScreenActivity.SEMANTIC_STR), Semantic.class);
        mWeatherList = GsonUtil.stringToList(getArguments().getString(FullScreenActivity.DATA_LIST_STR), WeatherEntity.class);

        for (int i = 0; i < mWeatherList.size(); i++) {
            if (mSemantic.slots.datetime.date.equals(mWeatherList.get(i).getDate())) {
                mCurWeather = mWeatherList.get(i);
                break;
            }
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_weather;
    }
}
