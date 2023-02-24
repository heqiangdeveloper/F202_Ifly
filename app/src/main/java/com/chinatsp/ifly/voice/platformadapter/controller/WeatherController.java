package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.EventTrackingEntity;
import com.chinatsp.ifly.entity.WeatherEntity;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IWeatherController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.speech.util.NetworkUtil;

import java.lang.ref.WeakReference;
import java.util.List;

import okhttp3.internal.Util;

import static com.chinatsp.ifly.api.constantApi.TtsConstant.*;

public class WeatherController extends BaseController implements IWeatherController {
    private static final String TAG = "WeatherController";
    private static final int MSG_TTS = 1001;

    private Context mContext;
    private MyHandler myHandler = new MyHandler(this);

    public WeatherController(Context context) {
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void showWeatherActivity(String resultStr, String semantic) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_WEATHER);
        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semantic);
        mContext.startActivity(intent);
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        //查询天气
        if (PlatformConstant.Operation.QUERY.equals(intentEntity.operation)) {
            String mainMsg;
            if (intentEntity.data != null && intentEntity.data.result != null && intentEntity.data.result.size() > 0) {
                boolean find = false;
                if (intentEntity.semantic.slots.datetime != null) {
                    String queryDateStr = intentEntity.semantic.slots.datetime.date;
                    List<WeatherEntity> weatherEntityList = GsonUtil.stringToList(intentEntity.data.result.toString(), WeatherEntity.class);
                    for (WeatherEntity entity : weatherEntityList) {
                        if (entity.getDate().equals(queryDateStr)) {
                            find = true;
                            break;
                        }
                    }
                }
                if (find) {
                    showWeatherActivity(intentEntity.data.result.toString(), GsonUtil.objectToString(intentEntity.semantic));
                    mainMsg = "";
                } else {//超出支持的日期
                    String defaultTts = mContext.getString(R.string.no_support_exceed_date_weather_query);
                    if (intentEntity.semantic.slots.location != null && !intentEntity.semantic.slots.location.city.equalsIgnoreCase("CURRENT_CITY")) {
                        getTtsMessage(WEATHERC8CONDITION, defaultTts, "",R.string.skill_weather, R.string.scene_weather, R.string.object_weather2, R.string.condition_weather8);
                    } else {
                        getTtsMessage(WEATHERC4CONDITION, defaultTts, "",R.string.skill_weather, R.string.scene_weather, R.string.object_weather1, R.string.condition_weather4);
                    }
                }
            } else {
                LogUtils.e(TAG, "intentEntity.data.result");
                if((intentEntity.data != null && intentEntity.data.result != null && intentEntity.data.result.size() == 0) ||
                        (intentEntity.data != null && intentEntity.data.error != null)){
                    String defaultText = mContext.getString(R.string.network_weka_tip);  //云端查询超时，播报网络不稳定
                    Message msg = myHandler.obtainMessage(MSG_TTS, defaultText);
                    myHandler.sendMessage(msg);
                } else if (!NetworkUtil.isNetworkAvailable(mContext)) {
                    mainMsg = mContext.getString(R.string.no_network_tip);
                    Message msg = myHandler.obtainMessage(MSG_TTS, mainMsg);
                    myHandler.sendMessageDelayed(msg, 2000);
                } else { //不支持的城市查询
                    if (intentEntity.semantic.slots.location != null) {
                        String city = intentEntity.semantic.slots.location.city;
                        if (("CURRENT_CITY").equals(city)){
                            city = mContext.getString(R.string.no_support_city_weather_query_gai);
                        }else if(city == null && intentEntity.semantic.slots.location.country != null){
                            city = intentEntity.semantic.slots.location.country;
                        }else if(city == null && intentEntity.semantic.slots.location.province != null){
                            city = intentEntity.semantic.slots.location.province;
                        }else if(city == null && intentEntity.semantic.slots.location.country == null && intentEntity.semantic.slots.location.province == null){
                            city = "国外";
                        }
                        Log.e(TAG,"城市---"+city);
                        String defaultTts = String.format(mContext.getString(R.string.no_support_city_weather_query), city);
                        Log.e(TAG,"defaultTts---"+defaultTts);
                        getTtsMessage(WEATHERC9CONDITION,defaultTts, city,R.string.skill_weather, R.string.scene_weather, R.string.object_weather3, R.string.condition_default);
                    }
                }
            }
        } else {
            if (intentEntity.semantic.slots.location != null) {

                String city = intentEntity.semantic.slots.location.city;
                if (("CURRENT_CITY").equals(city)){
                    city = mContext.getString(R.string.no_support_city_weather_query_gai);
                }
                String defaultTts = String.format(mContext.getString(R.string.no_support_city_weather_query), city);
                getTtsMessage(WEATHERC9CONDITION, defaultTts, city,R.string.skill_weather, R.string.scene_weather, R.string.object_weather3, R.string.condition_default);
            }else
                doExceptonAction(mContext);
        }

    }

    @Override
    public void mvwAction(MvwLParamEntity lParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity o) {

    }

    private void getTtsMessage(String conditionId, String defaultTts, String city,int appName, int scene, int object, int condition) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = defaultTts;
                } else {
                    if (!TextUtils.isEmpty(city)) {
                        defaultText = Utils.replaceTts(tts, "#CITY#", city);
                    }
                }
                Utils.eventTrack(mContext,appName,scene,object,conditionId,condition,defaultText);
                Message msg = myHandler.obtainMessage(MSG_TTS, defaultText);
                myHandler.sendMessage(msg);
            }
        });
    }

    private static class MyHandler extends Handler {

        private final WeakReference<WeatherController> weatherControllerWeakReference;

        private MyHandler(WeatherController weatherController) {
            this.weatherControllerWeakReference = new WeakReference<>(weatherController);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            final WeatherController weatherController = weatherControllerWeakReference.get();
            if (weatherController == null) {
                LogUtils.d(TAG, "weatherController == null");
                return;
            }
            switch (msg.what) {
                case MSG_TTS:
                    weatherController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {

                        @Override
                        public void onPlayStopped() {
                            //重新计算超时
//                            SRAgent.getInstance().resetSrTimeCount();
//                            String text = "你还可以查询明天的天气哦";
//                            TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, text);
                            FloatViewManager.getInstance(weatherController.mContext).hide();
                        }
                    });
                    break;
                default:
                    break;
            }

        }
    }
}
