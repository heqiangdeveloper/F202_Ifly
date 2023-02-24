package com.chinatsp.ifly.api.activeservice;


import android.annotation.SuppressLint;
import android.car.hardware.CarSensorEvent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.activeservice.birthday.BirthdayViewManager;
import com.chinatsp.ifly.activeservice.entity.AbnormalWeatherEntity;
import com.chinatsp.ifly.activeservice.entity.AdvertisEntity;
import com.chinatsp.ifly.activeservice.entity.BirthdayWishesEntity;
import com.chinatsp.ifly.activeservice.entity.HolidayWishesEntity;
import com.chinatsp.ifly.activeservice.entity.HotspotEntity;
import com.chinatsp.ifly.activeservice.entity.InWorkLineEntity;
import com.chinatsp.ifly.activeservice.entity.OutWorkLineEntity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.callback.onConfirmCallback;
import com.chinatsp.ifly.module.me.recommend.Utils.FileUtils;
import com.chinatsp.ifly.module.me.recommend.urlhttp.CallBackUtil;
import com.chinatsp.ifly.module.me.recommend.urlhttp.UrlHttpUtil;
import com.chinatsp.ifly.service.ActiveViewService;
import com.chinatsp.ifly.service.TxzTtsService;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.OkHttpUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.proxy.IVehicleNetworkRequestCallback;
import com.chinatsp.proxy.VehicleNetworkManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.txznet.tts.TXZTTSInitManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.http.conn.scheme.HostNameResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ActiveServiceModel {
    private static final String TAG = "ActiveServiceModel";
    public final static int VIEW_ABNORMAL_WEATHER = 1;
    public final static int VIEW_IN_WORK_LINE = 5;
    public final static int VIEW_OUT_WORK_LINE = 6;
    public final static int VIEW_BIRTHDAY_WISHES = 7;
    public final static int VIEW_HOLIDAY_WISHES = 8;
    public final static int VIEW_REPAIR_STATION = 9;
    public final static int GET_AD = 20;
    public final static int GET_AD_IMAGE = 21;
    public final static int GET_AD_VIDEO = 211;
    public final static int CHECK_IF_TXZ_TTS_INITED = 22;
    public final static int ACTION_START_TTS_BROADCAST = 23;
    private static ActiveServiceModel model;
    public static String BirthdayWishes_message;

    public static ArrayList<String> festival_time;
    public static ArrayList<String> festival_message;

    public static String AbnormalWeather_message;
    public static String InWorkLine_message;
    public static String OutWorkLine_message;
    public static String Popup_news_message;
    public static String Os_send_message;
    public static String Lock_unLock_closed_message;
    public static String Lock_closed_message;
    public static String UnLock_closed_message;
    public static String UnLock_closed_message_signal_5;
    private ActiveServiceViewManager activeServiceViewManager;

    public static final int HOLIDAY = 1;
    public static final int HOTSPOT = 2;

    private String Hotspot_message;

    private static String BadWeather_Loading;

    public static Double OutWorkLine_Lon;
    public static Double OutWorkLine_Lat;

    public static Double InWorkLine_Lon;
    public static Double InWorkLine_Lat;

    public static String Active_Content_str = "";
    public static String Activit_tts_msg = "";
    private int GET_AD_TIMES = 0;
    private int GET_AD_IMAGE_TIMES = 0;
    private int GET_AD_IMAGE_TIMES_COUNT = 6;
    private int CHECK_IF_TXZ_TTS_INITED_TIMES = 0;
    private int GET_AD_VIDEO_TIMES = 0;
    private String rootPath = "sdcard/chinatsp/Advertis/";
    private String imageFileName = "advertising.jpg";
    private String videoFileName = "advertising.mp4";
    public Boolean isTodayBirthday =false;

    @SuppressLint("HandlerLeak")
    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case VIEW_ABNORMAL_WEATHER://坏天气
                    activeServiceViewManager.show(VIEW_ABNORMAL_WEATHER);
                    break;
                case VIEW_IN_WORK_LINE://上班
                    activeServiceViewManager.show(VIEW_IN_WORK_LINE);
                    break;
                case VIEW_OUT_WORK_LINE://回家
                    activeServiceViewManager.show(VIEW_OUT_WORK_LINE);
                    break;
                case VIEW_BIRTHDAY_WISHES://生日
                    activeServiceViewManager.show(VIEW_BIRTHDAY_WISHES);
                    break;
                case VIEW_HOLIDAY_WISHES://节假日
                    activeServiceViewManager.show(VIEW_HOLIDAY_WISHES);
                    break;
                case VIEW_REPAIR_STATION://去维修站
                    activeServiceViewManager.show(VIEW_REPAIR_STATION);
                    break;
                case GET_AD:
                    GetAdvertisHttp((Context) msg.obj);
                    break;
                case GET_AD_IMAGE:
                    Map map = (Map) msg.obj;
                    GetImage((String) map.get("jsonStr"),(String) map.get("publishTime"), (String) map.get("uir") ,(Context) map.get("context"));
                    break;
                case GET_AD_VIDEO:
                    Map mapVideo = (Map) msg.obj;
                    GetVideo((String) mapVideo.get("jsonStr"),(String) mapVideo.get("publishTime"), (String) mapVideo.get("uir") ,(Context) mapVideo.get("context"));
                    break;
                case CHECK_IF_TXZ_TTS_INITED:
                    Map map1 = (Map) msg.obj;
                    handleWelcomeVisitors((Context) map1.get("context"),(int)map1.get("signal"),(int)map1.get("lockStatus"),(int)map1.get("unLockStatus"));
                    break;
                case ACTION_START_TTS_BROADCAST:
                    Map map2 = (Map) msg.obj;
                    startTTSBroadcast((Context) map2.get("context"),(String) map2.get("replaceText"),(int)map2.get("signal"),
                            (int)map2.get("lockStatus"),(int)map2.get("unLockStatus"));
                    break;
            }
        }
    };


    public static ActiveServiceModel getInstance() {
        if (model == null) {
            model = new ActiveServiceModel();
        }
        return model;
    }


    /**
     * add by zhengxb 2019/05/11
     * 请求成功之后回调
     */
    public void uploadCallback(String taskId) {

        Log.d("zheng", "zheng  uploadCallback");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", AppConfig.INSTANCE.token);
        jsonObject.addProperty("taskId", taskId);


        if (taskId.equals("badWeather")) {
            jsonObject.addProperty("gps", BadWeather_Loading);
        }

        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE_TRACK_URL, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d("zheng", "zheng----------uploadCallback----" + response + "  taskId:" + taskId);
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "zheng:upload exception-" + e);
            }
        }, jsonObject.toString());
    }

    /**
     * add by zhengxb 2019/05/11
     * 坏天气的网络数据请求
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"badWeather","data":{"gps":"114.78716659999998,37.042333299999996"}}
     */
    public void AbnormalWeather(ActiveServiceViewManager viewManager, String locationinfo, Context mContext) {
        if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            try {
                AbnormalWeather(viewManager, locationinfo, mContext, AppConfig.INSTANCE.token);
            } catch (Exception e) {
                Log.d(TAG, " 服务器数据出错: ");
            }

        } else {
            Log.d(TAG, "AbnormalWeather token is null");
        }
    }


    /**
     * add by zhengxb 2019/05/11
     * 坏天气的网络数据请求
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"badWeather","data":{"gps":"114.78716659999998,37.042333299999996"}}
     */
    public void AbnormalWeather(ActiveServiceViewManager viewManager, String locationinfo, Context mContext, String token) {
        Log.d(TAG, "zheng:request weather-token:" + token + ",taskId:badWeather");
        activeServiceViewManager = viewManager;
        BadWeather_Loading = locationinfo;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("taskId", "badWeather");
        JsonObject jsondata = new JsonObject();

        jsondata.addProperty("gps", locationinfo);
        jsonObject.add("data", jsondata);

        Log.d(TAG, TAG + "异常天气");

        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "异常气请求: " + response);
                AbnormalWeatherEntity abnormalWeatherEntity = null;
                try{
                    abnormalWeatherEntity = GsonUtil.stringToObject(response, AbnormalWeatherEntity.class);
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }

                if (null != abnormalWeatherEntity && (abnormalWeatherEntity.code == 0) && "请求成功".equals(abnormalWeatherEntity.msg)) {
                    AbnormalWeather_message = abnormalWeatherEntity.data.boardcastMessage;
                    mHandler.sendEmptyMessageDelayed(VIEW_ABNORMAL_WEATHER, 1 * 20 * 1000);
                    Activit_tts_msg = abnormalWeatherEntity.data.boardcastMessage;
                    Log.d(TAG, "异常天气请求--成功--");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "坏天气请求Exception: " + e);
            }
        }, jsonObject.toString());

        //热点信息网络请求
        Hostspot(mContext, locationinfo);
    }


    /**
     * add by zhengxb   2019/05/11
     * 节日问候的网络数据请求
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"festival"}
     */
    public void HolidayWishes(ActiveServiceViewManager viewManager, final Context mContext, String token) {
        Log.d(TAG, "zheng:request holiday-token:" + token + ",taskId:festival");
        activeServiceViewManager = viewManager;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("taskId", "festival");

        Log.d(TAG, TAG + "节日问候");

        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "zheng:节日问候的网络数据请求: " + response);
                HolidayWishesEntity holidayWishesEntity = null;
                try{
                    holidayWishesEntity = GsonUtil.stringToObject(response, HolidayWishesEntity.class);
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }

                if (null != holidayWishesEntity && (holidayWishesEntity.code == 0) && "请求成功".equals(holidayWishesEntity.msg)) {

                    SharedPreferencesUtils.saveString(mContext, "Festival_version", holidayWishesEntity.data.sceneData.version);
                    String festival_version = SharedPreferencesUtils.getString(mContext, "Festival_version", "2019-01-01");

                    Log.d(TAG, "zheng:节日问候的网络数据请求:------------------- " + festival_version + " version: " + holidayWishesEntity.data.sceneData.version);
                    festival_time = new ArrayList<>();
                    festival_message = new ArrayList<>();
                    if (!festival_version.equals(holidayWishesEntity.data.sceneData.version)) {

                        for (int i = 0; i < holidayWishesEntity.data.sceneData.festival.size(); i++) {
                            festival_time.add(holidayWishesEntity.data.sceneData.festival.get(i).date);
                            festival_message.add(holidayWishesEntity.data.sceneData.festival.get(i).content);
                        }
                        Log.d(TAG, "zheng festival_message.size:" + festival_message.size() + "   festival_time.size():" + festival_time.size());

                        Intent intent = new Intent(mContext, TxzTtsService.class);
                        intent.setAction(AppConstant.ACTION_FESTIVL);
                        intent.putStringArrayListExtra("festival_time", festival_time);
                        intent.putStringArrayListExtra("festival_message", festival_message);
                        mContext.startService(intent);

                        ArrayList<String> backupList = new ArrayList<>();
                        backupList.addAll(festival_message);
//                        TXZTTSInitManager.getInstance().savePCM(backupList);  节日问候不再需要
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, " 节日问候的网络数据请求Exception: " + e);
            }
        }, jsonObject.toString());
    }

    /**
     * add by zhengxb   2019/05/11
     * 节日问候的网络数据请求
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"festival"}
     */
    public void HolidayWishes(ActiveServiceViewManager viewManager, final Context mContext) {
        if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            try {
                HolidayWishes(viewManager, mContext, AppConfig.INSTANCE.token);
            } catch (Exception e) {
                Log.d(TAG, " 服务器数据出错: ");
            }
        } else {
            Log.d(TAG, "HolidayWishes token is null");
        }
    }

    /**
     * add by zhengxb   2019/05/11
     * 生日问候的网络数据请求
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"birthday"}
     */

    public void BirthdayWishes(Context context, ActiveServiceViewManager viewManager, onConfirmCallback callback) {
        if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            try {
                BirthdayWish(context, viewManager, AppConfig.INSTANCE.token, callback);
            } catch (Exception e) {
                Log.d(TAG, " 服务器数据出错: ");
            }

        } else {
            Log.d(TAG, "BirthdayWishes token is null");
        }
    }

    private void BirthdayWish(Context context, ActiveServiceViewManager viewManager, String token, onConfirmCallback callback) {
        Log.d(TAG, "zheng:request birthday-token:" + token + ",taskId:birthday");

        activeServiceViewManager = viewManager;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("taskId", "birthday");

        Log.d(TAG, TAG + " 生日问候");

        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, ":生日问候的网络数据请求: " + response);
                BirthdayWishesEntity birthdayWishesEntity = null;
                try{
                    birthdayWishesEntity = GsonUtil.stringToObject(response, BirthdayWishesEntity.class);
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }

                if (null != birthdayWishesEntity && (birthdayWishesEntity.code == 0) && "请求成功".equals(birthdayWishesEntity.msg)) {
                    Log.d(TAG, "生日问候的网络数据请求成功");

                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

                    try {
                        if (TextUtils.isEmpty(birthdayWishesEntity.data.sceneData.birthday.substring(5))) {
                            callback.onCallback(false);
                            return;
                        } else if (sdf.format(date).equals(birthdayWishesEntity.data.sceneData.birthday.substring(5))) {
                            BirthdayWishes_message = birthdayWishesEntity.data.boardcastMessage;
                            isTodayBirthday = true;
//                        sendHandler(VIEW_BIRTHDAY_WISHES,token,"birthday","");
//                        mHandler.sendEmptyMessageDelayed(VIEW_BIRTHDAY_WISHES, 1 * 1000);
                            //用户生日
                            callback.onCallback(true);
//                            BirthdayViewManager.getInstance(context).show(ActiveServiceModel.getInstance().BirthdayWishes_message);
                            uploadCallback("birthday");
                            Activit_tts_msg = birthdayWishesEntity.data.boardcastMessage;
                            Utils.eventTrack(context,R.string.skill_active,R.string.scene_active_birs,R.string.object_active_tuisong,TtsConstant.MSGC32CONDITION,R.string.condition_active5);
                        } else {
                            callback.onCallback(false);
                        }
                    } catch (Exception e) {
                        callback.onCallback(false);
                        Log.d(TAG, " 生日问候Exception: 时间为空" + e);
                    }

                } else {
                    callback.onCallback(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, " 生日问候的网络数据请求Exception: " + e);
                callback.onCallback(false);
            }
        }, jsonObject.toString());
    }

    /**
     * add by zhengxb   2019/05/11
     * 上班路况提醒
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"gowork"}
     */
    public void InWorkLine(Context mContext,ActiveServiceViewManager viewManager, String locationinfo, onConfirmCallback callback) {
        if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            try {
                InWorkLine(mContext,viewManager, locationinfo, AppConfig.INSTANCE.token, callback);
            } catch (Exception e) {
                Log.d(TAG, " 服务器数据出错: ");
            }

        } else {
            Log.d(TAG, "InWorkLine token is null");
        }
    }

    /**
     * add by zhengxb   2019/05/11
     * 上班路况提醒
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"gowork"}
     */
    public void InWorkLine(Context mContext,ActiveServiceViewManager viewManager, String locationinfo, String token, onConfirmCallback callback) {
        Log.d(TAG, "zheng:request inwork line-token:" + token + ",taskId:gowork");

        activeServiceViewManager = viewManager;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("taskId", "gowork");

        JsonObject jsondata = new JsonObject();
        jsondata.addProperty("gps", locationinfo);
        jsonObject.add("data", jsondata);
        Log.d(TAG, TAG + " 上班路况");
        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, " 上班路况提醒网络数据请求： " + response);
                InWorkLineEntity inWorkLineEntity = null;
                try{
                    //response = "<html><body><h1>502 Bad Gateway</h1>"; for test
                    inWorkLineEntity = GsonUtil.stringToObject(response, InWorkLineEntity.class);
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }

                if (null != inWorkLineEntity && (inWorkLineEntity.code == 0) && "请求成功".equals(inWorkLineEntity.msg)) {
                    Log.d(TAG, "zheng----------上班路况提醒请求成功----" + inWorkLineEntity.data.boardcastMessage);
                    Log.d(TAG, "zheng----------上班路况提醒请求成功----");
                    //判断播报场景优先级
                    int priority = AppConstant.InOutWorkPriority;
                    if(!Utils.checkPriority(mContext,priority)){
                        Log.d(TAG,"checkPriority() = " + Utils.checkPriority(mContext,priority));
                        return;
                    }
                    InWorkLine_message = inWorkLineEntity.data.boardcastMessage;
                    mHandler.sendEmptyMessage(VIEW_IN_WORK_LINE);
                    callback.onCallback(true);

                    String string = inWorkLineEntity.data.sceneData.company;

                    String[] strs = string.split(",");

                    InWorkLine_Lon = Double.valueOf(strs[0].toString().trim());
                    InWorkLine_Lat = Double.valueOf(strs[1].toString().trim());
                    Log.d("zheng4", "zheng4---------" + InWorkLine_Lon + InWorkLine_Lat);
                    Activit_tts_msg = inWorkLineEntity.data.boardcastMessage;

                } else {
                    callback.onCallback(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onCallback(false);
                Log.d(TAG, " 上班路况提醒网络数据请求Exception： " + e);
            }
        }, jsonObject.toString());

    }

    /**
     * add by zhengxb   2019/05/11
     * 下班路况提醒
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"backHome"}
     */
    public void OutWorkLine(Context mContext,ActiveServiceViewManager viewManager, String locationinfo, onConfirmCallback callback) {
        if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            try {
                OutWorkLine(mContext,viewManager, locationinfo, AppConfig.INSTANCE.token, callback);
            } catch (Exception e) {
                Log.d(TAG, " 服务器数据出错: ");
            }
        } else {
            Log.d(TAG, "OutWorkLine token is null");
        }
    }

    /**
     * add by zhengxb   2019/05/11
     * 下班路况提醒
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"backHome"}
     */
    public void OutWorkLine(Context mContext,ActiveServiceViewManager viewManager, String locationinfo, String token, onConfirmCallback callback) {
        Log.d(TAG, "zheng:request outwork line-token:" + token + ",taskId:backHome");

        activeServiceViewManager = viewManager;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("taskId", "backHome");

        JsonObject jsondata = new JsonObject();

        jsondata.addProperty("gps", locationinfo);
        jsonObject.add("data", jsondata);

        Log.d(TAG, TAG + " 下班路况");
        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "下班路况请求： " + response);
                OutWorkLineEntity outWorkLineEntity = null;
                try{
                    outWorkLineEntity = GsonUtil.stringToObject(response, OutWorkLineEntity.class);
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }

                Log.d(TAG, "outWorkLineEntity = null ? " + (outWorkLineEntity == null));
                if (null != outWorkLineEntity && (outWorkLineEntity.code == 0) && "请求成功".equals(outWorkLineEntity.msg)) {
                    //判断播报场景优先级
                    int priority = AppConstant.InOutWorkPriority;
                    if(!Utils.checkPriority(mContext,priority)){
                        Log.d(TAG,"checkPriority() = " + Utils.checkPriority(mContext,priority));
                        return;
                    }
                    OutWorkLine_message = outWorkLineEntity.data.boardcastMessage;
                    mHandler.sendEmptyMessage(VIEW_OUT_WORK_LINE);
                    callback.onCallback(true);

                    String string = outWorkLineEntity.data.sceneData.home;
                    String[] strs = string.split(",");

                    OutWorkLine_Lon = Double.valueOf(strs[0].toString().trim());
                    OutWorkLine_Lat = Double.valueOf(strs[1].toString().trim());
                    Activit_tts_msg = outWorkLineEntity.data.boardcastMessage;
                } else {
                    callback.onCallback(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, " 下班路况请求Exception： " + e);
                callback.onCallback(false);
            }
        }, jsonObject.toString());
    }


    /**
     * 目的天气的网络请求
     */
    public void DestWeather(Context mContext, String startGps, String endGps) {
        if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            try {
                DestWeather(mContext, startGps, endGps, AppConfig.INSTANCE.token);
            } catch (Exception e) {
                Log.d(TAG, " 服务器数据出错: ");
            }
        } else {
            Log.d(TAG, "DestWeather token is null");
        }
    }

    /**
     * 目的天气的网络请求
     */
    public void DestWeather(Context mContext, String startGps, String endGps, String token) {
        Log.d(TAG, "zheng:request des weather-token:" + token + ",taskId:destWeather");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("taskId", "destWeather");
        JsonObject jsondata = new JsonObject();
        jsondata.addProperty("startGps", startGps);
        jsondata.addProperty("endGps", endGps);
        jsonObject.add("data", jsondata);

        Log.d(TAG, TAG + "目的天气");

        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "zheng 目的天气请求成功： " + response);
                AbnormalWeatherEntity abnormalWeatherEntity = null;
                try{
                    abnormalWeatherEntity = GsonUtil.stringToObject(response, AbnormalWeatherEntity.class);
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }

                if (null != abnormalWeatherEntity && (abnormalWeatherEntity.code == 0) && "请求成功".equals(abnormalWeatherEntity.msg)) {
                    Log.d(TAG, "zheng 目的天气请求成功-----------： ");
                    Intent intent = new Intent();
                    intent.setAction(AppConstant.ACTIVE_DestWeather);
                    Bundle b = new Bundle();
                    b.putString("province", abnormalWeatherEntity.data.sceneData.province);
                    b.putString("city", abnormalWeatherEntity.data.sceneData.city);
                    b.putString("windpower", abnormalWeatherEntity.data.sceneData.windpower);
                    b.putString("weather", abnormalWeatherEntity.data.sceneData.weather);
                    b.putString("temperature", abnormalWeatherEntity.data.sceneData.temperature);
                    b.putString("humidity", abnormalWeatherEntity.data.sceneData.humidity);
                    b.putString("winddirection", abnormalWeatherEntity.data.sceneData.winddirection);
                    intent.addFlags(Intent.FLAG_RECEIVER_NO_ABORT | 0x01000000);
                    intent.putExtras(b);
                    mContext.sendBroadcast(intent);

                    uploadCallback("destWeather");
                    Activit_tts_msg = abnormalWeatherEntity.data.sceneData.province;
                    Utils.eventTrack(mContext, R.string.skill_active, R.string.scene_active_muweather, R.string.object_active_tuisong, TtsConstant.MSGC21CONDITION, R.string.condition_chexinC1);
                }

            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "目的天气请求Exception： " + e);
            }
        }, jsonObject.toString());

    }


    /**
     * add by zhengxb   2019/05/11
     * 热点信息的网络数据请求
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"birthday"}
     */

    public void Hostspot(Context mContext, String locationinfo) {
        if (!TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            try {
                Hostspot(mContext, locationinfo, AppConfig.INSTANCE.token);
            } catch (Exception e) {
                Log.d(TAG, " 服务器数据出错: ");
            }
        } else {
            Log.d(TAG, "Hostspot token is null");
        }
    }

    /**
     * add by zhengxb   2019/05/11
     * 热点信息的网络数据请求
     * {"token":"86b7a34f-eb7b-47d0-b534-a5af909b506e","taskId":"birthday"}
     */

    public void Hostspot(Context mContext, String locationinfo, String token) {
        Log.d(TAG, "zheng:request hotspot-token:" + token + ",taskId:hotspot");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", token);
        jsonObject.addProperty("taskId", "hotspot");

        JsonObject jsondata = new JsonObject();
        jsondata.addProperty("gps", locationinfo);
        jsonObject.add("data", jsondata);

        Log.d(TAG, TAG + " 热点信息");

        OkHttpUtils.postJson(AppConstant.ACTIVE_SERVICE, new OkHttpUtils.ResultCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "zheng 热点信息的网络数据请求: " + response);
                HotspotEntity hotspotEntity = null;
                try{
                    hotspotEntity = GsonUtil.stringToObject(response, HotspotEntity.class);
                }catch (Exception e){
                    Log.d(TAG, " 服务器数据出错: ");
                }
                if (null != hotspotEntity && (hotspotEntity.code == 0) && "请求成功".equals(hotspotEntity.msg)) {
                    Hotspot_message = hotspotEntity.data.boardcastMessage;

                    Intent intent = new Intent(mContext, TxzTtsService.class);
                    intent.setAction(AppConstant.ACTION_ACTIVESERVICE);
                    intent.putExtra("info_type", HOTSPOT);
                    intent.putExtra("message", Hotspot_message);
                    mContext.startService(intent);

//                    TXZTTSInitManager.getInstance().savePCM(Hotspot_message);
                    uploadCallback("hotspot");
                    Activit_tts_msg = hotspotEntity.data.boardcastMessage;
                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_active_host,R.string.object_active_tuisong,TtsConstant.MSGC47CONDITION,R.string.condition_active5);
                } else {
                    FestivalProvide_Shared(mContext, 2, "com.chinatsp.ifly", "com.chinatsp.ifly");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, " 热点信息的网络数据请求Exception: " + e);
            }
        }, jsonObject.toString());

    }


    /**
     *开机广告图片获取
     */

    public void GetAdvertisHttp(Context mContext) {

        if (AppConfig.INSTANCE.token==null||TextUtils.isEmpty(AppConfig.INSTANCE.token)) {
            return;
        }
        String mill=  System.currentTimeMillis()+"";
        HashMap<String,String> params = new HashMap<>();
        params.put("access_token",AppConfig.INSTANCE.token);
        params.put("timestamp",mill);
        Log.e(TAG,"GetAdvertis_URL"+AppConstant.GetAdvertis_URL);
        VehicleNetworkManager.getInstance().requestNet(AppConstant.GetAdvertis_URL, "POST", params, new IVehicleNetworkRequestCallback() {
            @Override
            public void onSuccess(String s) {
                GET_AD_TIMES = 0;
                //Log.d(TAG,"GetAdvertisHttp onSuccess: s = " + s);
                List<AdvertisEntity.PageItems> pageItemsList = new ArrayList<AdvertisEntity.PageItems>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    AdvertisEntity advertisEntity = GsonUtil.stringToObject(s,AdvertisEntity.class);
                    AdvertisEntity.PageItems pageItems = null;
                    for (int i = 0; i < advertisEntity.data.pageItems.size();i++){
                        pageItems = advertisEntity.data.pageItems.get(i);
                        if ("active".equals(pageItems.status.toString()) &&
                                //"CA_CV_TENANT".equals(pageItems.itemMap.tenantId) &&
                                sdf.parse(pageItems.publishTime).before(sdf.parse(pageItems.endTime))){
                            pageItemsList.add(pageItems);
                        }
                    }
                    getActiveAdvertis(pageItemsList,mContext);
                }catch (Exception e) {
                    Log.d(TAG,"GetAdvertisHttp Exception"+e);
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG,"GetAdvertisHttp erro "+s);
                GET_AD_TIMES++;
                Log.d(TAG, "onError: GET_AD_TIMES = " + GET_AD_TIMES);
                if(GET_AD_TIMES < 3){
                    Message getAdMsg = new Message();
                    getAdMsg.what = GET_AD;
                    getAdMsg.obj = mContext;
                    mHandler.sendMessageDelayed(getAdMsg,100);
                }
            }

            @Override
            public void onProgress(float v) {

            }
        });
    }

    public void getActiveAdvertis(List<AdvertisEntity.PageItems> pageItemsList,Context mContext){
        //获取时间最新的那张图片
        Date currentDate = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        AdvertisEntity.PageItems selectPageItem = null;
        int index = 0;
        long minDiff = 0;
        long diff = 0;
        Log.d(TAG, "pageItemsList.size = " + ( pageItemsList != null ? pageItemsList.size() : 0 ) + "");
        if(pageItemsList == null || pageItemsList.size() == 0){
            Log.d(TAG, "getActiveAdvertis: 没有可用的广告资源");
        } else if(pageItemsList.size() == 1){
            selectPageItem = pageItemsList.get(0);
        }else {
            try {
                List<Date> dateList = new ArrayList<>();
                for (int i = 0; i < pageItemsList.size(); i++) {
                    dateList.add(sdf.parse(pageItemsList.get(i).publishTime));
                }
                //将日期排序
                Collections.sort(dateList);
                //找到离当前时间最近的那个时间
                if (dateList.get(0).before(currentDate)) {
                    minDiff = currentDate.getTime() - dateList.get(0).getTime();
                } else {
                    minDiff = dateList.get(0).getTime() - currentDate.getTime();
                }

                for (int j = 1; j < dateList.size(); j++) {
                    if (dateList.get(j).before(currentDate)) {
                        diff = currentDate.getTime() - dateList.get(j).getTime();
                    } else {
                        diff = dateList.get(j).getTime() - currentDate.getTime();
                    }

                    if (diff <= minDiff) {
                        minDiff = diff;
                        index = j;
                    }
                }
                Log.d(TAG, "index = " + index);

                //找到最近时间对应的图片
                Calendar targetCalendar = Calendar.getInstance();
                targetCalendar.setTime(dateList.get(index));
                Calendar newCalendar = Calendar.getInstance();
                for (int k = 0; k < pageItemsList.size(); k++) {
                    newCalendar.setTime(sdf.parse(pageItemsList.get(k).publishTime));
                    if (newCalendar.get(Calendar.YEAR) == targetCalendar.get(Calendar.YEAR) &&
                            newCalendar.get(Calendar.MONTH) == targetCalendar.get(Calendar.MONTH) &&
                              newCalendar.get(Calendar.DAY_OF_MONTH) == targetCalendar.get(Calendar.DAY_OF_MONTH) &&
                              newCalendar.get(Calendar.HOUR) == targetCalendar.get(Calendar.HOUR) &&
                                      newCalendar.get(Calendar.MINUTE) == targetCalendar.get(Calendar.MINUTE) &&
                                      newCalendar.get(Calendar.SECOND) == targetCalendar.get(Calendar.SECOND)) {
                        selectPageItem = pageItemsList.get(k);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG,"parse date exception: " + e);
            }
        }

        if (selectPageItem != null && selectPageItem.welcomeUrl != null) {
            Log.d(TAG, "getActiveAdvertis: selectPageItem.startTime = " + selectPageItem.startTime + ",endTime = " +
                            selectPageItem.endTime + ",welcomeUrl = " + selectPageItem.welcomeUrl + ",videoUrl = " + selectPageItem.videoUrl);
            String lastAdPublishTime = SharedPreferencesUtils.getString(mContext,AppConstant.KEY_LAST_AD_PUBLISHTIME,"");
            Log.d(TAG, "getActiveAdvertis: lastAdPublishTime = " + lastAdPublishTime + ",selectPageItem.publishTime = " + selectPageItem.publishTime);
            Log.d(TAG,"isAdImageExist() = " + isAdImageExist() + ",isAdVideoExist() = " + isAdVideoExist());
            if((!lastAdPublishTime.equals(selectPageItem.publishTime)) || !isAdImageExist() || !isAdVideoExist()){//当有最新的图片发布或之前下载的图片没有了，就需要去下载
                Gson gson = new Gson();
                String gsonStr = gson.toJson(selectPageItem);
                if(!TextUtils.isEmpty(selectPageItem.welcomeUrl)){
                    GetImage(gsonStr,selectPageItem.publishTime,selectPageItem.welcomeUrl, mContext);
                }else if(!TextUtils.isEmpty(selectPageItem.videoUrl)){
                    GetVideo(gsonStr,selectPageItem.publishTime,selectPageItem.videoUrl, mContext);
                }
            }
        }
    }

    private boolean isAdImageExist(){
        String adImagePath = rootPath + imageFileName;
        File dirFile = new File(adImagePath);
        if (dirFile.exists() && (dirFile.length() != 0)) {//判断文件是否存在
            return true;
        }
        return false;
    }

    private boolean isAdVideoExist(){
        String adImagePath = rootPath + videoFileName;
        File dirFile = new File(adImagePath);
        if (dirFile.exists() && (dirFile.length() != 0)) {//判断文件是否存在
            return true;
        }
        return false;
    }

    /**
     * 设置ContentProvider数据共享
     *
     * @param mContext
     * @param id
     * @param response
     */
    public void FestivalProvide_Shared(Context mContext, int id, String response, String text) {
        Uri uri_user = Uri.parse("content://com.chinatsp.ifly.festival/festival");
        // 先删除表中数据在插入
        ContentValues values = new ContentValues();
        ContentResolver resolver = mContext.getContentResolver();
        //删除条件
        String whereClause = "_id=?";
        //删除条件参数
        String[] whereArgs = {String.valueOf(id)};
        resolver.delete(uri_user, whereClause, whereArgs);

        values.put("_id", id);
        values.put("festival_time", "");
        values.put("festival_json", response);
        values.put("festival_text", text);
        resolver.insert(uri_user, values);
    }

    public void GetVideo(String jsonStr,String publishTime,String uir, Context mContext) {
        Log.d(TAG, "GetVideo-----");
        UrlHttpUtil.downloadFile(uir,0,0, new CallBackUtil.CallBackFile(""+ rootPath,videoFileName) {
            @Override
            public void onFailure(int code, String errorMessage,int position,int id) {
                Log.d(TAG, "GetVideo----- onFailure，GET_AD_VIDEO_TIMES = " + GET_AD_VIDEO_TIMES);
                GET_AD_VIDEO_TIMES++;
                if(GET_AD_VIDEO_TIMES < GET_AD_IMAGE_TIMES_COUNT){
                    sendGetAdVideoMessage(jsonStr,publishTime,uir,mContext);
                }
            }

            @Override
            public void onProgress(float progress, long total,int position,int id) {
                super.onProgress(progress, total,position,id);
            }

            @Override
            public void onResponse(File response,int position,int id) {
                Log.d(TAG,"GetVideo success..");
                GET_AD_VIDEO_TIMES = 0;
                Settings.System.putString(mContext.getContentResolver(),"advertiseInfo", jsonStr);
                Log.d(TAG,"advertiseInfo = " + jsonStr);
                SharedPreferencesUtils.saveString(mContext,AppConstant.KEY_LAST_AD_PUBLISHTIME,publishTime);
            }
        });
    }

    public void GetImage(String jsonStr,String publishTime,String uir, Context mContext) {
        Log.d(TAG, "GetImage-----");
        if (uir != null && !uir.isEmpty()) {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(uir)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "GetImage----- onFailure，GET_AD_IMAGE_TIMES = " + GET_AD_IMAGE_TIMES);
                    GET_AD_IMAGE_TIMES++;
                    if(GET_AD_IMAGE_TIMES < GET_AD_IMAGE_TIMES_COUNT){
                        sendGetAdMessage(jsonStr,publishTime,uir,mContext);
                    }
                }

                public void onResponse(Call call, Response response) throws IOException {
                    try{
                        String fileMD5 = response.header("full-file-md5");
                        //InputStream inputStream = response.body().byteStream();//得到图片的流
                        byte[] b = response.body().bytes();
                        String downloadMD5 = DigestUtils.md5Hex(b);
                        //Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        Log.d(TAG, "fileMD5 = " + fileMD5);
                        Log.d(TAG, "downloadMD5 = " + downloadMD5);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);

                        if(null != fileMD5 && fileMD5.equals(downloadMD5)){
                            GET_AD_IMAGE_TIMES = 0;
                            //存储鸡汤文，发布时间给设置，welcome调用
                            Settings.System.putString(mContext.getContentResolver(),"advertiseInfo", jsonStr);
                            Log.d(TAG,"jsonStr = " + jsonStr);
                            //Settings.System.putString(mContext.getContentResolver(),"publishTime", publishTime);
                            SharedPreferencesUtils.saveString(mContext,AppConstant.KEY_LAST_AD_PUBLISHTIME,publishTime);
                            //添加“鸡汤”水印
//                    WatermarkSettings.getmInstance(mContext);
//                    bitmap = WatermarkSettings.createWatermark(bitmap,"每一个不曾起舞的日子，都是对生命的辜负");
                            SaveImage(bitmap);
                        }else {
                            Log.d(TAG, "GetImage----- onResponse，GET_AD_IMAGE_TIMES = " + GET_AD_IMAGE_TIMES);
                            GET_AD_IMAGE_TIMES++;
                            if(GET_AD_IMAGE_TIMES < GET_AD_IMAGE_TIMES_COUNT){
                                sendGetAdMessage(jsonStr,publishTime,uir,mContext);
                            }
                        }
                    }catch (Exception e){
                        Log.d(TAG,"GetImage Exception : " + e);
                    }
                }
            });
        }

    }

    public void sendGetAdMessage(String jsonStr,String mPublishTime,String mUir, Context mContext){
        Message getImageMsg = new Message();
        getImageMsg.what = GET_AD_IMAGE;
        Map map = new HashMap();
        map.put("jsonStr",jsonStr);
        map.put("publishTime",mPublishTime);
        map.put("uir",mUir);
        map.put("context",mContext);
        getImageMsg.obj = map;
        mHandler.sendMessageDelayed(getImageMsg,100);
    }

    public void sendGetAdVideoMessage(String jsonStr,String mPublishTime,String mUir, Context mContext){
        Message getImageMsg = new Message();
        getImageMsg.what = GET_AD_VIDEO;
        Map map = new HashMap();
        map.put("jsonStr",jsonStr);
        map.put("publishTime",mPublishTime);
        map.put("uir",mUir);
        map.put("context",mContext);
        getImageMsg.obj = map;
        mHandler.sendMessageDelayed(getImageMsg,100);
    }

    public void SaveImage(Bitmap bitmap) {
        Log.d(TAG, " GetImage save--------------");
        FileOutputStream out = null;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) // 判断是否可以对SDcard进行操作
        {      // 获取SDCard指定目录下

            File dirFile = new File(rootPath);  //目录转化成文件夹
            if (!dirFile.exists()) {                //如果不存在，那就建立这个文件夹
                dirFile.mkdirs();
            }                            //文件夹有啦，就可以保存图片啦
            File file = new File(rootPath, "advertising1" + ".jpg");// 在SDcard的目录下创建图片文,以当前时间为其命名
            Log.d(TAG, " GetImage advertising save -----------------advertising.jpg-");
            try {
                out = new FileOutputStream(file);
                if(null != bitmap) bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.flush();
                out.close();
                if(null != bitmap) bitmap.recycle();
                String string1 = rootPath + imageFileName;
                File file1 = new File(string1);
                file1.delete();
                //确保out.flush() 和out.close()执行完毕，然后更改图片保存目录名字
                File file2 = new File(rootPath+"advertising1.jpg");
                file2.renameTo(file1);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"---"+e);
            }
        }
    }

    public void handleWelcomeVisitors(Context mContext,int signal,int mLockStatus,int munLockStatus){
        Log.d(TAG,"signal = " + signal + ",mLockStatus = " + mLockStatus + ",munLockStatus = " + munLockStatus);
        Log.d(TAG,"CHECK_IF_TXZ_TTS_INITED_TIMES = " + CHECK_IF_TXZ_TTS_INITED_TIMES);
        Log.d(TAG,"CarUtils.powerStatus; = " + CarUtils.powerStatus);
        if(CarUtils.powerStatus >= CarSensorEvent.IGNITION_STATE_ON){
            int priority = AppConstant.WelcomeUserPriority;
            Log.d("heqiangq","Utils.getCurrentPriority(mContext) = " + Utils.getCurrentPriority(mContext));
            if(!Utils.checkPriority(mContext,priority)){
                Log.d(TAG,"checkPriority() = " + Utils.checkPriority(mContext,priority));
                return;
            }
            if((signal == 5 && munLockStatus == 2) || (mLockStatus == 2 && munLockStatus == 2) ||
                    (mLockStatus == 2 && munLockStatus == 1) || (mLockStatus == 1 && munLockStatus == 2)){
                Log.d(TAG,"AppConfig.INSTANCE.ttsEngineInited = " + AppConfig.INSTANCE.ttsEngineInited);
                if(!AppConfig.INSTANCE.ttsEngineInited){//检查TXZ TTS引擎是否已完成初始化
                    CHECK_IF_TXZ_TTS_INITED_TIMES++;
                    if(CHECK_IF_TXZ_TTS_INITED_TIMES >= 60){//1分钟
                        Log.d(TAG,"ttsEngineInited failed for one minute...");
                        CHECK_IF_TXZ_TTS_INITED_TIMES = 0;
                        return;
                    }
                    Message msg = new Message();
                    HashMap<Object,Object> map = new HashMap<Object,Object>();
                    map.put("context",mContext);
                    map.put("signal",signal);
                    map.put("lockStatus",mLockStatus);
                    map.put("unLockStatus",munLockStatus);
                    msg.what = CHECK_IF_TXZ_TTS_INITED;
                    msg.obj = map;
                    mHandler.sendMessageDelayed(msg,1000);
                    return;
                }
                CHECK_IF_TXZ_TTS_INITED_TIMES = 0;
                String replaceText = getReasonStr(signal);
                Message msg1 = new Message();
                HashMap<Object,Object> map1 = new HashMap<Object,Object>();
                map1.put("context",mContext);
                map1.put("signal",signal);
                map1.put("replaceText",replaceText);
                map1.put("lockStatus",mLockStatus);
                map1.put("unLockStatus",munLockStatus);
                msg1.what = ACTION_START_TTS_BROADCAST;
                msg1.obj = map1;
                mHandler.sendMessageDelayed(msg1,5000);
            }
        }
    }

    private void startTTSBroadcast(Context mContext,String replaceText,int signal,int mLockStatus,int munLockStatus){
        Log.d(TAG,"startTTSBroadcast....");
        if(signal == 5){
            if(munLockStatus == 2){
                Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MSGC93_1CONDITION, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        String ttsText = tts;
                        if (TextUtils.isEmpty(tts)) {
                            ttsText = mContext.getString(R.string.msgC93_1);
                        }
                        ttsText = ttsText.replace("#REASON#",replaceText);
                        ActiveServiceModel.UnLock_closed_message_signal_5 = ttsText;
                        ActiveServiceModel.Activit_tts_msg = ttsText;
                        Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,TtsConstant.MSGC93_1CONDITION,R.string.condition_msgC93_1);
                        ActiveServiceViewManager.getInstance(mContext).show(ActiveViewService.VIEW_UNLOCK_CLOSED_SIGNAL_5);
                    }
                });
            }
        }else if(mLockStatus == 2 && munLockStatus == 2){
            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MSGC93CONDITION, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String ttsText = tts;
                    if (TextUtils.isEmpty(tts)) {
                        ttsText = mContext.getString(R.string.msgC93);
                    }
                    ttsText = ttsText.replace("#REASON#",replaceText);
                    ActiveServiceModel.Lock_unLock_closed_message = ttsText;
                    ActiveServiceModel.Activit_tts_msg = ttsText;
                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,TtsConstant.MSGC93CONDITION,R.string.condition_msgC93);
                    ActiveServiceViewManager.getInstance(mContext).show(ActiveViewService.VIEW_LOCK_UNLOCK_CLOSED);
                }
            });
        }else if(mLockStatus == 1 && munLockStatus == 2){
            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MSGC93_1CONDITION, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String ttsText = tts;
                    if (TextUtils.isEmpty(tts)) {
                        ttsText = mContext.getString(R.string.msgC93_1);
                    }
                    ttsText = ttsText.replace("#REASON#",replaceText);
                    ActiveServiceModel.UnLock_closed_message = ttsText;
                    ActiveServiceModel.Activit_tts_msg = ttsText;
                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,TtsConstant.MSGC93_1CONDITION,R.string.condition_msgC93_1);
                    ActiveServiceViewManager.getInstance(mContext).show(ActiveViewService.VIEW_UNLOCK_CLOSED);
                }
            });
        }else if(mLockStatus == 2 && munLockStatus == 1){
            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MSGC93_2CONDITION, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String ttsText = tts;
                    if (TextUtils.isEmpty(tts)) {
                        ttsText = mContext.getString(R.string.msgC93_2);
                    }
                    ttsText = ttsText.replace("#REASON#",replaceText);
                    ActiveServiceModel.Lock_closed_message = ttsText;
                    ActiveServiceModel.Activit_tts_msg = ttsText;
                    Utils.eventTrack(mContext,R.string.skill_active,R.string.scene_welcome_user,R.string.object_welcome_user,TtsConstant.MSGC93_2CONDITION,R.string.condition_msgC93_2);
                    ActiveServiceViewManager.getInstance(mContext).show(ActiveViewService.VIEW_LOCK_CLOSED);
                }
            });
        }

    }

    private String getReasonStr(int signal){
        if(signal == 1){
            //ActiveServiceModel.Active_Content_str = "因钥匙在检测区域内，持续10分钟未启动车辆，为减少电量损耗，钥匙靠近解锁功能已关闭，确定重新开启？";
            return "钥匙持续十分钟在迎宾区域";
        }else if(signal == 4){
            //ActiveServiceModel.Active_Content_str = "因超过三天未启动车辆，为减少电量损耗，钥匙靠近解锁功能已关闭，确定重新开启？";
            return "停车超过三天";
        }else if(signal == 5){
            //ActiveServiceModel.Active_Content_str = "因触发3次钥匙靠近解锁功能但未启动车辆，为减少电量损耗，钥匙靠近解锁功能已关闭，确定重新开启？";
            return "钥匙靠近解锁三次未开门";
        }
        return "";
    }
}