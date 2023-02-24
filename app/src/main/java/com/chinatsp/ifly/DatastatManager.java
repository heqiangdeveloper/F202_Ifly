package com.chinatsp.ifly;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.entity.EventTrackingEntity;
import com.chinatsp.ifly.module.me.recommend.bean.VideoDataBean;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.voice.platformadapter.manager.LocateManager;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatastatManager {
    private Uri mUriDatastat;
    private final static String TAG = "DatastatManage";

    private DatastatManager() {
        mUriDatastat = Uri.parse("content://com.chinatsp.datastat/datastat");
    }

    public static DatastatManager getInstance() {
        return DatastatManagerImp.dataManager;
    }

    private static class DatastatManagerImp {
        private final static DatastatManager dataManager = new DatastatManager();
    }

    /**
     * ui_event  (ui事件使用)
     * "event_id": "事件编码id",//由1级编码与2级编码拼接而成，见文档<br></br>
     * "value": "事件设置值",<br></br>
     * "lat": "纬度",<br></br>
     * "lng": "经度",<br></br>
     * "timestamp": "1554971235",<br></br>
     * "systemversion": "系统版本号",<br></br>
     * "appversion": "应用版本号",<br></br>
     * "protversion": "数据采集协议的版本号"<br></br>
     */
    public void recordUI_event(Context mContext, String event_id, String value) {
        String e = "\"event_id\":" + "\"" + event_id + "\","
                + "\"value\":" + "\"" + value + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob:" + data);
        record(mContext, "ui_event", data,false);
    }

    /* wakeup  (语音唤醒使用)
     * wakeuptype: 语音唤醒方式
     * wakewords: 语音唤醒词
     * lat: 纬度
     * lng: 经度
     * timestamp: 1554971235
     * systemversion: 系统版本号
     * appversion: 应用版本号
     * protversion: 数据采集协议的版本号
     */
    public void wakeup_event(Context mContext, String wakeuptype, String wakewords) {
        String e = "\"wakeuptype\":" + "\"" + wakeuptype + "\","
                + "\"wakewords\":" + "\"" + wakewords + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "wakeup", data,false);
    }

    public void wakeup_event_new(Context mContext, String wakeuptype, String wakewords,String provider) {
        String e = "\"wakeuptype\":" + "\"" + wakeuptype + "\","
                + "\"wakewords\":" + "\"" + wakewords + "\","
                + "\"provider\":" + "\"" + provider + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "wakeup", data,false);
    }

    public void provider_event(Context mContext, String provider) {
        String e = "\"provider\":" + "\"" + provider + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "wakeup", data,false);
    }

    public void voiceEventTracking(Context mContext, int appName, int scene, int object) {
        if (TextUtils.isEmpty(mContext.getString(appName)))
            return;

        String e = "\"appName\":" + "\"" + mContext.getString(appName) + "\","
                + "\"scene\":" + "\"" + mContext.getString(scene) + "\","
                + "\"object\":" + "\"" + mContext.getString(object) + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "voice_asssit", data,false);
    }

    public static String primitive = "";
    public static String response = "";

    /**
     * voice_asssit  (语音使用)
     * appName: 技能
     * scene: 场景
     * object: 意图
     * response: 语音返回的json字符串
     * tts: 文案
     * condition: 条件名称
     * conditionid: 条件ID
     * primitive: 语音原语"
     * lat: 纬度
     * lng: 经度
     * timestamp: 1554971235
     * provider: 讯飞语音云
     * systemversion": 系统版本号
     * appversion": "应用版本号
     * protversion: 数据采集协议的版本号
     */
    public void eventTracking(Context mContext, EventTrackingEntity entity) {

        String skillMain = mContext.getString(R.string.skill_main);
        String skillActive = mContext.getString(R.string.skill_active);
        String objectUserChoice = mContext.getString(R.string.object_active_xuanzhe);
        entity.response = response;
        entity.primitive = primitive;


        if(("WIFI状态变化播报").equals(entity.scene)
                || ("蓝牙状态变化播报").equals(entity.scene)){
            entity.response = "";
            entity.primitive = "";
        }


        if(skillActive.equals(entity.appName)){
            if(entity.tts==null||entity.tts.equals(""))
                entity.tts = ActiveServiceModel.Activit_tts_msg;
            if(!mContext.getString(R.string.object_tired).equals(entity.object)
                    &&!mContext.getString(R.string.sobject_smoking).equals(entity.object)){
                entity.response = "";
            }

            if(!objectUserChoice.equals(entity.object)
            &&!mContext.getString(R.string.object_tired).equals(entity.object)
                    &&!mContext.getString(R.string.sobject_smoking).equals(entity.object)){
                entity.primitive = "";
            }
        }

        if (!TextUtils.isEmpty(entity.conditionId)&&(entity.tts==null||entity.tts.equals(""))) {  //tts 不为空时，直接埋点不用获取文案
            TtsUtils.getInstance(mContext).getTtsMessage(entity.conditionId, new TtsUtils.OnCallback() {
                @Override
                public void onSuccess(List<TtsInfo> ttsInfoList) {
                    String result = ttsInfoList.get(0).getTtsText();
                    if (!TextUtils.isEmpty(result)) {
                        entity.tts = result;
                    }
                    eventTrackingInner(mContext, entity,false);
                }

                @Override
                public void onFail() {
                    if (TextUtils.isEmpty(entity.tts)) {
                        entity.tts = "";
                    }
                    eventTrackingInner(mContext, entity,false);
                }
            });
        } else {
            eventTrackingInner(mContext, entity,false);
        }
    }

    //针对免唤醒，直接埋点，不清除数据
    public void eventTracking(Context mContext, EventTrackingEntity entity,boolean insert) {
        Log.d(TAG, "eventTracking() called with: mContext = [" + mContext + "], entity = [" + entity + "], insert = [" + insert + "]");
        eventTrackingInner(mContext, entity,insert);
    }

    //点击指令模块
    public void command_event(Context mContext,String moduleName,String skillName){
        String e = "\"moduleName\":" + "\"" + moduleName + "\","
                + "\"skillName\":" + "\"" + skillName + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "command_event", data,false);
    }

    //更新指令数量
    public void update_command_event(Context mContext,String number,String name){
        String e = "\"number\":" + "\"" + number + "\","
                + "\"name\":" + "\"" + name + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "command_info", data,false);
    }

    public void update_video_state(Context mContext, List<VideoDataBean> videoDataBeanList){
        JSONObject root = new JSONObject();
        JSONArray languages = new JSONArray();
        try {
            for (VideoDataBean videoDataBean:videoDataBeanList){
                JSONObject jo = new JSONObject();
                try {
                    String state ="";
                    if (videoDataBean.getRead().equalsIgnoreCase("false")){
                        state ="未读";
                    }else {
                        state ="已读";
                    }
                    jo.put(videoDataBean.getVideoName(),state);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                languages.put(jo);
            }
            root.put("plate",languages);
            String[] latLng = LocateManager.getInstance(mContext).getLocation();
            if(latLng == null) {
                latLng = new String[] {"", ""};
            }
            String e = "\"event_id\":" + "\"" + 4227 + "\","
                    + "\"value\":"  + root.toString() + ",";
            String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
            String data = "{" + e + pubStr + "}";
            Log.d(TAG, "update_video_state" + data);
            record(mContext, "ui_event", data,false);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    //桌面点击跳转
    public void float_jumpmodule_event(Context mContext,String copy,String modular){
        String e = "\"copy\":" + "\"" + copy + "\","
                + "\"modular\":" + "\"" + modular + "\",";
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicString(latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "float_jumpmodule", data,false);
    }


    private void eventTrackingInner(Context mContext, EventTrackingEntity entity,boolean insert) {
        String response = "\"response\":" + "\"" + entity.response + "\",";
        if (entity.response!=null&&!"".equals(entity.response)&&GsonUtil.isJson(entity.response)){
            response = "\"response\":"  + entity.response + ",";
        }
        String e = "\"appName\":" + "\"" + entity.appName + "\","
                + "\"scene\":" + "\"" + entity.scene + "\","
                + "\"object\":" + "\"" + entity.object + "\","
                + "\"tts\":" + "\"" + entity.tts + "\","
                + "\"condition\":" + "\"" + entity.condition + "\","
                + "\"conditionid\":" + "\"" + entity.conditionId + "\","
                + "\"primitive\":" + "\"" + entity.primitive + "\","
                + response;
        String[] latLng = LocateManager.getInstance(mContext).getLocation();
        if(latLng == null) {
            latLng = new String[] {"", ""};
        }
        String pubStr = getPublicAssistant(mContext, latLng[0], latLng[1], getTimestamp(), getOSversion(), getAppVersion(mContext), getProtoVersion());
        String data = "{" + e + pubStr + "}";
        Log.d(TAG, "lh:mob" + data);
        record(mContext, "voice_asssit", data,insert);
    }

    private String getPublicAssistant(Context context, String lat, String lng, String timestamp, String systemversion, String appversion, String protversion) {
        return "\"lat\":" + "\"" + lat + "\","
                + "\"lng\":" + "\"" + lng + "\","
                + "\"timestamp\":" + "\"" + timestamp + "\","
                + "\"provider\":" + "\"" + context.getString(R.string.speech_ifly) + "\","
                + "\"systemversion\":" + "\"" + systemversion + "\","
                + "\"appversion\":" + "\"" + appversion + "\","
                + "\"protversion\":" + "\"" + protversion + "\"";
    }

    private String getPublicString(String lat, String lng, String timestamp, String systemversion, String appversion, String protversion) {
        return "\"lat\":" + "\"" + lat + "\","
                + "\"lng\":" + "\"" + lng + "\","
                + "\"timestamp\":" + "\"" + timestamp + "\","
                + "\"systemversion\":" + "\"" + systemversion + "\","
                + "\"appversion\":" + "\"" + appversion + "\","
                + "\"protversion\":" + "\"" + protversion + "\"";
    }

    /**
     * 传入固定的catagray和data字段；
     * 其中catagray为固定的类型（music_fm，local_video，voice_asssit，app_traffic_statistic，app_open，carcheck，wakeup，ui_event）
     * data为json格式的字符串{}
     */
    public void record(Context mContext, String catagray, String data,boolean insert) {
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put("catagray", catagray);
                values.put("data", data);
                ContentResolver resolver = mContext.getContentResolver();
                try {
                    Uri uri = resolver.insert(mUriDatastat, values);
                    //插入数据库结果判断
                    Log.d(TAG, "lh:insert result:" + (uri != null));

                    if(insert) //只出入数据，不清除记录
                        return;
                    if (mContext != null && "voice_asssit".equals(catagray)) {
                        //埋点后，复原讯飞原语
                        if(!data.contains("WIFI状态变化播报") && !data.contains("蓝牙状态变化播报") && !data.contains("主动服务技能")){
                            primitive = "";
                            response = "";
                            Log.e(TAG,"zheng");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getTimestamp() {
        long time = System.currentTimeMillis();
        return "" + time;
    }

    private String getOSversion() {
        return Build.DISPLAY;
    }

    private String getAppVersion(Context mContext) {
        String verName = "1.0";
        try {
            verName = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "v" + verName;
    }

    private String getProtoVersion() {
        return "v4.3";
    }
}
