package com.chinatsp.ifly.utils;

import android.app.Activity;
import android.car.CarNotConnectedException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioAttributes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.AppManager;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.MainActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.entity.EventTrackingEntity;
import com.chinatsp.ifly.entity.ReplaceTtsEntity;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.listener.IExtendCallback;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.tts.ESpeaker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC40CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC42CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC43CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC50CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC51CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC52CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.ACC53CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SKYLIGHTC13CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SYSTEMC47CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SYSTEMC48CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SYSTEMC49CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.SYSTEMC50CONDITION;

public class Utils {
    private static final String TAG = "Utils";
    public static final int STREAM_SYSTEM = AudioAttributes.USAGE_NOTIFICATION;
    public static final int STREAM_MEDIA = AudioAttributes.USAGE_MEDIA;
    public static final int STREAM_PHONE = AudioAttributes.USAGE_VOICE_COMMUNICATION;
    public static final int STREAM_NAVI = AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE;

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }

    /**
     * 获取资源文件的id
     *
     * @param context
     * @param resName
     * @return
     */
    public static int getId(Context context, String resName) {
        return context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
    }

    public static void hideKeyBoard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && activity.getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (activity.getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static String getFromAssets(Context context, String fileName) {
        String line = "";
        StringBuffer Result = new StringBuffer();
        InputStreamReader inputReader = null;
        try {
            inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            while ((line = bufReader.readLine()) != null) {
                Result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Result.toString();
    }

    public static void startApp(Context mContext,String packageName,String action) {
        LogUtils.d(TAG, "startApp packageName="+packageName);
        String className = Utils.getLauncherActivityNameByPackageName(mContext, packageName);
        if(!TextUtils.isEmpty(className)) {
            Intent intent = new Intent();
            if(!TextUtils.isEmpty(action)) {
                intent.setAction(action);
            }
            intent.setClassName(packageName, className);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    public static String getLauncherActivityNameByPackageName(Context context, String packageName) {
        String className = "";
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageName);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
/*        ResolveInfo resolveinfo = resolveInfos.iterator().next();
        if (resolveinfo != null) {
            className = resolveinfo.activityInfo.name;
        }*/
        //未找到包名，迭代器越界报错
        Iterator mIterator = resolveInfos.iterator();
        if (mIterator.hasNext()){
            ResolveInfo resolveinfo = (ResolveInfo) mIterator.next();
            if (resolveinfo != null) {
                className = resolveinfo.activityInfo.name;
            }
        }
        return className;
    }

    //获取时间格式：16:45
    public static String getTimeForHour(String time) {
        String tmp = "";
        if(time==null||"".equals(time))
            return tmp;
        String[] tmpTimeArray = time.split(" ");
        time = tmpTimeArray[tmpTimeArray.length - 1];
        int index = time.lastIndexOf(":");
        tmp = time.substring(0, index);
        return tmp;
    }

    public static int getDay(String startTime, String endTime) {
        String differenceFormat = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-mm-dd, 会出现时间不对, 因为小写的mm是代表: 秒
        Date dateTime1 = null;
        Date dateTime2 = null;
        int days = 0;
        try {
            dateTime1 = sdf.parse(startTime);
            dateTime2 = sdf.parse(endTime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(dateTime1);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(dateTime2);
            int day1 = calendar1.get(Calendar.DAY_OF_YEAR);
            int day2 = calendar2.get(Calendar.DAY_OF_YEAR);

            days = day2 - day1;
            Log.d("getDay:", "相差天数:" + days);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }

    public static String getTimeForDay(String time) {
        if (TextUtils.isEmpty(time)) return "";
        String tmp = "";
        int totalTime = Integer.parseInt(time);
        int hours = totalTime / 60;
        int minutes = totalTime - hours * 60;
        if (hours > 0) {
            tmp = hours + "小时";
        }
        tmp = tmp + minutes + "分钟";
        return tmp;
    }

    /*
     * author:liuhong
     * date: 2019-05-16
     * 通过数据库查询TTS文案,查询完成后进行TTS播报.
     * TTS语音播报,此方法仅适用于没有通配符的文案。
     * defaultTts: 默认tts,存放在ttslib.xml文件中,在数据库查询tts失败或者未查询到tts时使用.防止TTS不播报的情况
     */
    public static void getMessageWithTtsSpeak(Context context, String conditionId, String defaultTts) {
        getMessageWithTtsSpeak(context,conditionId,defaultTts,null);
    }

    //通过数据库查询TTS文案,不进行TTS播报,只回调TTS文案给调用端,由调用端自行决定是否播报.
    public static void getMessageWithoutTtsSpeak(Context context, String conditionId, final TtsUtils.OnConfirmInterface onConfirmInterface) {
        Log.d(TAG,"lh:conditionId4:" +conditionId);
        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG,"lh:conditionId4:" + conditionId+"，callback:"+ttsInfo.getTtsText());
                String tts = ttsInfo.getTtsText();
                if (ttsInfo.getTtsText().isEmpty()){
                    tts = context.getString(R.string.msg_tts_isnull).toString();
                }
                onConfirmInterface.onConfirm(tts);
            }

            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案
                onConfirmInterface.onConfirm("");
            }
        });
    }

    /*
     * author:liuhong
     * date: 2019-05-16
     * 通过数据库查询TTS文案,查询完成后进行TTS播报.
     * TTS语音播报,此方法仅适用于没有通配符的文案。
     * defaultTts: 默认tts,存放在ttslib.xml文件中,在数据库查询tts失败或者未查询到tts时使用.防止TTS不播报的情况
     */
    public static void getMessageWithTtsSpeak(Context context, String conditionId, String defaultTts, TTSController.OnTtsStoppedListener listener) {
        Log.d(TAG,"lh:conditionId1:" + conditionId+",defaultTts:"+defaultTts);

        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG,"lh:conditionId1:" + conditionId+"，callback:"+ttsInfo.getTtsText());
                String tts = ttsInfo.getTtsText();
                if (ttsInfo.getTtsText().isEmpty()){
                    tts = context.getString(R.string.msg_tts_isnull).toString();
                }
                startTTS(tts,listener);
            }

            @Override
            public void onFail() {
                startTTS(defaultTts,listener);
            }
        });
    }

    public static void getMessageWithTtsSpeak(Context context, String conditionId, String defaultTts, TTSController.OnTtsStoppedListener listener,boolean answerSwitch) {
        Log.d(TAG,"lh:conditionId1:" + conditionId+",defaultTts:"+defaultTts);

        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG,"lh:conditionId1:" + conditionId+"，callback:"+ttsInfo.getTtsText());
                String tts = ttsInfo.getTtsText();
                if (ttsInfo.getTtsText().isEmpty()){
                    tts = context.getString(R.string.msg_tts_isnull).toString();
                }
                startTTSNoVoice(tts,answerSwitch,listener);
            }

            @Override
            public void onFail() {
                startTTSNoVoice(defaultTts,answerSwitch,listener);
            }
        });
    }

    //只进行播报接口，不进行UI显示和回调
    public static void getMessageWithTtsSpeakOnly(Context context, String conditionId, String defaultTts, TTSController.OnTtsStoppedListener listener) {
        Log.d(TAG, "lh:conditionId2:" + conditionId + ",defaultTts:" + defaultTts);

        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG, "lh:conditionId2:" + conditionId + "，callback:" + ttsInfo.getTtsText());
                String words = ttsInfo.getTtsText();
                if (words.isEmpty()){
                    words = context.getString(R.string.msg_tts_isnull).toString();
                }
                startTTSOnly(words, listener);
            }

            @Override
            public void onFail() {
                startTTSOnly(defaultTts, listener);
            }
        });
    }

    //播报接口，showText: true进行UI显示和回调.  false : 不进行UI显示和回调
    public static void getMessageWithTtsSpeakOnly(boolean showText, Context context, String conditionId, String defaultTts) {
        Log.d(TAG, "lh:conditionId2:" + conditionId + ",defaultTts:" + defaultTts);

        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG, "lh:conditionId2:" + conditionId + "，callback:" + ttsInfo.getTtsText());
                if(showText) {
                    startTTSOnly(ttsInfo.getTtsText());
                } else {
                    TTSController.getInstance(context).startTTSOnly(ttsInfo.getTtsText(),PriorityControler.PRIORITY_FOUR);
                }
                //WIFI或蓝牙连接状态变化时，需要弹出toast
                if(conditionId == SYSTEMC47CONDITION || conditionId == SYSTEMC48CONDITION || conditionId == SYSTEMC49CONDITION || conditionId == SYSTEMC50CONDITION){
                    MyToast.showToast(context,ttsInfo.getTtsText(), true);
                }
            }

            @Override
            public void onFail() {
                if(showText) {
                    startTTSOnly(defaultTts);
                } else {
                    TTSController.getInstance(context).startTTSOnly(defaultTts,PriorityControler.PRIORITY_FOUR);
                }
                //WIFI或蓝牙连接状态变化时，需要弹出toast
                if(conditionId == SYSTEMC47CONDITION || conditionId == SYSTEMC48CONDITION || conditionId == SYSTEMC49CONDITION || conditionId == SYSTEMC50CONDITION){
                    MyToast.showToast(context,defaultTts, true);
                }
            }
        });
    }

    //通过数据库查询TTS文案,不进行TTS播报,只回调TTS文案给调用端,由调用端自行决定是否播报.
    public static void getMessageWithoutTtsSpeak(Context context, String conditionId, String defaultText, TtsUtils.OnConfirmInterface onConfirmInterface) {
        Log.d(TAG, "lh:conditionId3:" +  conditionId);
        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG,"lh:conditionId3:" + conditionId+"，callback:"+ttsInfo.getTtsText());
                String tts = ttsInfo.getTtsText();
                if (ttsInfo.getTtsText().isEmpty()){
                    tts = context.getString(R.string.msg_tts_isnull).toString();
                }
                onConfirmInterface.onConfirm(tts);
            }

            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案
                onConfirmInterface.onConfirm(defaultText);
            }
        });
    }

    public static void startTTS(String ttsText, TTSController.OnTtsStoppedListener listener) {
        Context context = BaseApplication.getInstance();
        if(!FloatViewManager.getInstance(context).isHide()) {
            EventBusUtils.sendMainMessage(ttsText);
        }
        TTSController.getInstance(context).startTTS(ttsText,PriorityControler.PRIORITY_FOUR,listener);
    }


    /**
     * 唤醒不播报
     * @param ttsText
     * @param listener
     */
    public static void startTTSNoVoice(String ttsText,boolean answerSwitch, TTSController.OnTtsStoppedListener listener) {
        Context context = BaseApplication.getInstance();
        if(!answerSwitch)
            ttsText = "。";
        TTSController.getInstance(context).startTTS(ttsText, PriorityControler.PRIORITY_FOUR,listener);

        if(!FloatViewManager.getInstance(context).isHide()) {
            EventBusUtils.sendMainMessage(ttsText);
        }

    }

    public static void startTTS(String text) {
        startTTS(text, null);
    }

    /**
     * 播报并且在播报结束不重新开启识别，不更新动画
     *
     * @param ttsText
     * @param listener
     */
    public static void startTTSOnly(String ttsText, TTSController.OnTtsStoppedListener listener) {
        Context context = BaseApplication.getInstance();
        if (!FloatViewManager.getInstance(context).isHide()) {
            EventBusUtils.sendMainMessage(ttsText);
        }
        TTSController.getInstance(context).startTTSOnly(ttsText, PriorityControler.PRIORITY_FOUR,listener);
    }

    /**
     * 播报并且在播报结束不重新开启识别，不更新动画
     *
     * @param ttsText
     * @param listener
     */
    public static void startTTSOnlyNoSendMainMessage(String ttsText, TTSController.OnTtsStoppedListener listener) {
        Context context = BaseApplication.getInstance();
//        if (!FloatViewManager.getInstance(context).isHide()) {
//            EventBusUtils.sendMainMessage(ttsText);
//        }
        TTSController.getInstance(context).startTTSOnly(ttsText, PriorityControler.PRIORITY_FOUR,listener);
    }

    public static void startTTSOnly(String text) {
        startTTSOnly(text, null);
    }

    public static void eventTrack(Context context,int appName, int scene, int object, String conditionId, int condition) {
        EventTrackingEntity entity = Utils.initEventTracking(appName, scene, object, conditionId, condition);
        DatastatManager.getInstance().eventTracking(context, entity);
    }

    public static void eventTrack(Context context,int appName, int scene, int object, String conditionId, int condition,String tts) {
        EventTrackingEntity entity = Utils.initEventTracking(appName, scene, object, conditionId, condition,tts);
        DatastatManager.getInstance().eventTracking(context, entity);
    }
    public static void eventTrack(Context context,int appName, int scene, String primitive,int object, String response,String conditionId, int condition,String tts,boolean insert) {
        EventTrackingEntity entity = Utils.initEventTracking(appName, scene,primitive, object,response,conditionId, condition,tts);
        DatastatManager.getInstance().eventTracking(context, entity,insert);
    }

    //埋点信息
    public static void eventTrack(Context context,int appName, int scene, int object, String condition) {
        EventTrackingEntity entity = Utils.initEventTracking(appName, scene, object, condition);
        DatastatManager.getInstance().eventTracking(context, entity);
    }

    //适用于有多个变量需要替换的文案.
    public static String replaceTtsList(String ttsDb, List<ReplaceTtsEntity> replaceList) {
        if (replaceList != null && replaceList.size() > 0) {
            for (ReplaceTtsEntity replaceTts : replaceList) {
                if (ttsDb.contains(replaceTts.getOriginTts())) {
                    ttsDb = ttsDb.replace(replaceTts.getOriginTts(), replaceTts.getReplaceTts());
                }
            }
        }
        return ttsDb;
    }

    //适用于只有一个变量替换的文案
    public static String replaceTts(String ttsDb, String originTts,String replaceTts) {
        if (TextUtils.isEmpty(ttsDb)) {
            return "";
        }
        if (TextUtils.isEmpty(originTts) || TextUtils.isEmpty(replaceTts)) {
            return ttsDb;
        }
        if (ttsDb.contains(originTts)) {
            ttsDb = ttsDb.replace(originTts, replaceTts);

        }
        return ttsDb;
    }

    public static ReplaceTtsEntity initReplaceData(String originTts, String replaceTts) {
        ReplaceTtsEntity replaceTtsEntity = new ReplaceTtsEntity();
        replaceTtsEntity.setOriginTts(originTts);
        replaceTtsEntity.setReplaceTts(replaceTts);
        return replaceTtsEntity;
    }


    public static EventTrackingEntity initEventTracking(int appName,int scene,int object,String conditionId,int condition){
        EventTrackingEntity entity = new EventTrackingEntity();
        Context context = BaseApplication.getInstance();
        entity.appName = context.getString(appName);
        entity.scene = context.getString(scene);
        entity.object = context.getString(object);
        entity.condition = context.getString(condition);
        entity.conditionId = conditionId;
        return entity;
    }

    public static EventTrackingEntity initEventTracking(int appName,int scene,int object,String conditionId,int condition,String tts){
        EventTrackingEntity entity = new EventTrackingEntity();
        Context context = BaseApplication.getInstance();
        entity.appName = context.getString(appName);
        entity.scene = context.getString(scene);
        entity.object = context.getString(object);
        entity.condition = context.getString(condition);
        entity.conditionId = conditionId;
        entity.tts = tts;
        return entity;
    }

    public static EventTrackingEntity initEventTracking(int appName, int scene, String primitive,int object, String response,String conditionId, int condition,String tts){
        EventTrackingEntity entity = new EventTrackingEntity();
        Context context = BaseApplication.getInstance();
        entity.appName = context.getString(appName);
        entity.scene = context.getString(scene);
        entity.primitive = primitive;
        entity.object = context.getString(object);
        entity.response = response;
        entity.condition = context.getString(condition);
        entity.conditionId = conditionId;
        entity.tts = tts;
        return entity;
    }

    public static EventTrackingEntity initEventTracking(int appName,int scene,int object,String condition){
        EventTrackingEntity entity = new EventTrackingEntity();
        Context context = BaseApplication.getInstance();
        entity.appName = context.getString(appName);
        entity.scene = context.getString(scene);
        entity.object = context.getString(object);
        entity.condition = condition;
        return entity;
    }

    public static void getTtsMessage(Context mContext, String conditionId, final String defaultTts,final String param,boolean isHideView,int appName, int scene, int object,int condition,boolean isMvw) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                Log.d(TAG,"getTtsMessage tts is: " + tts);
                String ttsText = tts;
                String flag = "";
                if (TextUtils.isEmpty(tts) || !tts.contains("#")) {
                    if (!TextUtils.isEmpty(tts)){
                        ttsText = tts;
                    }else {
                        ttsText = defaultTts;
                    }
                }
                if(ttsText.contains("#NUM#")){
                    flag = "#NUM#";
                }else if(ttsText.contains("#MAXNUM#")){
                    flag = "#MAXNUM#";
                }else if(ttsText.contains("#MINNUM#")){
                    flag = "#MINNUM#";
                }else if(ttsText.contains("#MIDDLENUM#")){
                    flag = "#MIDDLENUM#";
                }else if(ttsText.contains("#PERCENT#")){
                    flag = "#PERCENT#";
                }else if(ttsText.contains("#WINDOW#")){
                    flag = "#WINDOW#";
                }
                ttsText = Utils.replaceTts(ttsText, flag, param);
                Log.d(TAG, "getTtsMessage ttsText is: " + ttsText);
                if(isMvw){
                    if(TtsConstant.SYSTEMC41_1CONDITION.equals(conditionId)
                            ||TtsConstant.SYSTEMC41_2CONDITION.equals(conditionId)
                            ||TtsConstant.SYSTEMC41_3CONDITION.equals(conditionId)
                            ||TtsConstant.SYSTEMC41_4CONDITION.equals(conditionId)
                    ){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_global,R.string.object_volumn_up,TtsConstant.MHXC7CONDITION,R.string.condition_null,ttsText);
                    }else if(TtsConstant.SYSTEMC41_5CONDITION.equals(conditionId)
                            ||TtsConstant.SYSTEMC41_6CONDITION.equals(conditionId)
                            ||TtsConstant.SYSTEMC41_7CONDITION.equals(conditionId)
                            ||TtsConstant.SYSTEMC41_8CONDITION.equals(conditionId)
                    ){
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_global,R.string.object_volumn_down,TtsConstant.MHXC8CONDITION,R.string.condition_null,ttsText);
                    }
                }else
                Utils.eventTrack(mContext, appName, scene, object,conditionId, condition, ttsText);
                if (isHideView) {
                    Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            if (!FloatViewManager.getInstance(mContext).isHide()) {
                                FloatViewManager.getInstance(mContext).hide();
                            }
                        }
                    });
                } else {
                    Utils.startTTS(ttsText);
                }
            }
        });
    }

    public static void getTtsMessageWithTwoParams(Context mContext, String conditionId, final String defaultTts,final String param1,final String param2,boolean isHideView,int appName, int scene, int object,int condition){
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                Log.d(TAG,"getTtsMessageWithTwoParams tts is: " + tts);
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                } else {
                    if(tts.contains("#TYPE#") && tts.contains("#NUM#")){
                        ttsText = ttsText.replaceFirst("#TYPE#",param1);
                        ttsText = ttsText.replaceFirst("#NUM#",param2);
                    }else if(tts.contains("#TYPE#") && tts.contains("#MAXNUM#")){
                        ttsText = ttsText.replaceFirst("#TYPE#",param1);
                        ttsText = ttsText.replaceFirst("#MAXNUM#",param2 );
                    }else if(tts.contains("#TYPE#")){
                        ttsText = ttsText.replaceFirst("#TYPE#",param1);
                    }else if(tts.contains("#NUM#")){
                        ttsText = ttsText.replaceFirst("#NUM#",param2);
                    }else if(tts.contains("#MAXNUM#")){
                        ttsText = ttsText.replaceFirst("#MAXNUM#",param2);
                    }else if(tts.contains("#MINNUM#")){
                        ttsText = ttsText.replaceFirst("#MINNUM#",param2);
                    }else{
                        ttsText = defaultTts;
                    }
                }
                Log.d(TAG,"getTtsMessageWithTwoParams ttsText is: " + ttsText);
                Utils.eventTrack(mContext, appName, scene, object,conditionId, condition, ttsText);
                Utils.startTTS(ttsText,new TTSController.OnTtsStoppedListener(){
                    @Override
                    public void onPlayStopped() {
                        if(isHideView){
                            if(!FloatViewManager.getInstance(mContext).isHide()){
                                FloatViewManager.getInstance(mContext).hide();
                            }
                        }
                    }
                });
            }
        });
    }
    /**
     * 打开屏幕
     */
    public static void openScreen() {
        Intent intent = new Intent("com.chinatsp.START_STANDBY");
        intent.putExtra("OP_SCREEN", "ON");
        intent.setPackage("com.chinatsp.settings");
        BaseApplication.getInstance().startService(intent);
    }

    /**
     * 关闭屏幕
     */
    public static void closeScreen() {
        Intent intent = new Intent("com.chinatsp.START_STANDBY");
        intent.putExtra("OP_SCREEN", "OFF");
        intent.setPackage("com.chinatsp.settings");
        BaseApplication.getInstance().startService(intent);
    }

    public static void exit(Context context) {
        Log.d(TAG, "exit: "+FloatViewManager.getInstance(context).isHide()+"..."+AppManager.getAppManager().currentActivity());
        if (!FloatViewManager.getInstance(context).isHide() /*|| (AppManager.getAppManager().currentActivity() != null //未知补丁，继续增加判断，不是mainactivity
                                                                &&!(AppManager.getAppManager().currentActivity() instanceof MainActivity))*/) {
//            SRAgent.getInstance().stopSRSession();

            AppManager.getAppManager().finishListActivity();

            String[] greetings = context.getResources().getStringArray(R.array.user_exit);
            int i = new Random().nextInt(greetings.length);
            TTSController.OnTtsStoppedListener listener = new TTSController.OnTtsStoppedListener() {
                @Override
                public void onPlayStopped() {
                    exitVoiceAssistant();
                }
            };

            Utils.getMessageWithoutTtsSpeak(context, TtsConstant.MAINC11CONDITION, greetings[i], new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    Utils.eventTrack(context,R.string.skill_main,R.string.scene_main_exit,DatastatManager.primitive,R.string.object_main_exit,DatastatManager.response,TtsConstant.MAINC11CONDITION,R.string.condition_default,tts,true);
                    Utils.startTTSOnly(tts, listener);
                    Utils.eventTrack(context,R.string.skill_global_nowake,R.string.scene_main_exit,R.string.object_main_exit,TtsConstant.MHXC22CONDITION,R.string.condition_default,tts);
                }
            });
        }
    }

    public static void exitVoiceAssistant() {
        if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
            FloatViewManager.getInstance(BaseApplication.getInstance()).hide();
        } else {
            LogUtils.i(TAG, "exitVoiceAssistant isHide == true");
        }
    }

    public static void setMastMute(Context context){
        boolean needRestoreMute = AppConstant.setMute;
        if(needRestoreMute) {
            try {
                AppConfig.INSTANCE.mCarAudioManager.setMasterMute(true, 0);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static Integer getInt(Context context, String key, int def) {
        Integer result = def;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            //参数类型
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getInt = SystemProperties.getMethod("getInt", paramTypes);
            //参数
            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new Integer(def);
            result = (Integer) getInt.invoke(SystemProperties, params);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            //如果key超过32个字符则抛出该异常
            Log.w(TAG, "key超过32个字符");
        } catch (Exception e) {
            result = def;
        }
        return result;
    }

    public static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int actorToValue(String actor){
        Log.d(TAG, "actorToValue() called with: actor = [" + actor + "]");
        int value = ESpeaker.ivTTS_ROLE_JIAJIA;
        if(actor==null||"".equals(actor)||actor.contains("小欧")){
            value = -10001;
        }else {  //说明是科大讯飞
           if(actor.contains("嘉嘉")){
               value = ESpeaker.ivTTS_ROLE_JIAJIA;
           }else  if(actor.contains("小燕")){
               value = ESpeaker.ivTTS_ROLE_XIAOYAN;
           }else  if(actor.contains("小峰")){
               value = ESpeaker.ivTTS_ROLE_XIAOFENG;
           }else  if(actor.contains("楠楠")){
               value = ESpeaker.ivTTS_ROLE_NANNAN;
           }else  if(actor.contains("小倩")){
               value = ESpeaker.ivTTS_ROLE_XIAOQIAN;
           }else  if(actor.contains("晓蓉")){
               value = ESpeaker.ivTTS_ROLE_XIAORONG;
           }else  if(actor.contains("小美")){
               value = ESpeaker.ivTTS_ROLE_XIAOMEI;
           }else  if(actor.contains("小强")){
               value = ESpeaker.ivTTS_ROLE_XIAOQIANG;
           }else  if(actor.contains("晓坤")){
               value = ESpeaker.ivTTS_ROLE_XIAOKUN;
           }else  if(actor.contains("晓琳")){
               value = ESpeaker.ivTTS_ROLE_XIAOLIN;
           }else  if(actor.contains("小雪")){
               value = ESpeaker.ivTTS_ROLE_XIAOXUE;
           }else  if(actor.contains("小师")){
               value = ESpeaker.ivTTS_ROLE_XIAOSHI;
           }else  if(actor.contains("小洁")){
               value = ESpeaker.ivTTS_ROLE_XIAOJIE;
           }else  if(actor.contains("晓梦")){
               value = ESpeaker.ivTTS_ROLE_XIAOMENG;
           }else  if(actor.contains("老马")){
               value = ESpeaker.ivTTS_ROLE_LAOMA;
           }else  if(actor.contains("晓婧")){
               value = ESpeaker.ivTTS_ROLE_XIAOJING;
           }
        }
        return value;
    }

    public static String actorToString(String actor){
        StringBuilder builder = new StringBuilder("你好我是");
        if(actor.length()>=2)
            builder.append(actor.substring(0,2));
        return builder.toString();
    }


    /**
     * 获取版本信息
     * @param context
     * @return
     */
    public static int packageCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    /*
     *   设置页面是否全屏显示
     *   @param flag
     *   true:全屏， false不全屏（右边留出相应尺寸）
     */
    public static void setFullWindowSize(Activity activity,boolean isFullSize,boolean isTranslucent) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams windowLayoutParams = window.getAttributes(); // 获取对话框当前的参数值
        if (isFullSize) {
            windowLayoutParams.width = 1920; // 宽度设置为全屏
        } else {
            windowLayoutParams.width = 1920 - 130;  // 宽度设置为屏幕宽度减去130
        }
        windowLayoutParams.height = 720;
        windowLayoutParams.gravity = Gravity.LEFT;
        if(isTranslucent){
            windowLayoutParams.alpha = 0.5f;//０全透明．1不透明．
        }else {
            windowLayoutParams.alpha = 1.0f;
        }
        window.setAttributes(windowLayoutParams);
    }

    /**
     * 获取音量
     *
     * @param type
     */
    public static int getStreamVolume(int type) {
        try {
            return AppConfig.INSTANCE.mCarAudioManager.getGroupVolume(
                    AppConfig.INSTANCE.mCarAudioManager.getVolumeGroupIdForUsage(type));
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void setStreamVolume(int type, int volume) {
        try {
            AppConfig.INSTANCE.mCarAudioManager.setGroupVolume(
                    AppConfig.INSTANCE.mCarAudioManager.getVolumeGroupIdForUsage(type), volume, 0);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    public static void gotoHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        BaseApplication.getInstance().startActivity(intent);

        TAExtendManager.getInstance().getApi().setNaviScreen(new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d("BaseController", "success");
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d("BaseController", "onFail:" + extendErrorModel.getErrorCode());
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }

    /////////////////////语音播报优先级增加接口   start/////////////////////////////

    /**
     * 获取当前语音播报的优先级
     * 先进行比较判断，如果要播报的优先级高于现在的优先级，则进行播报
     * 如果要播报的优先级低于现在的优先级，则不要调用播报的接口，否则会出现界面显示要进行播报的文案
     * @return
     */
    public static int getCurrentPriority(Context c){
        return TTSController.getInstance(c).getmCurrentPriority();
    }

    public static void resetCurrentPriority(Context c){
        TTSController.getInstance(c).resetCurrentPriority();
    }

    public static boolean checkPriority(Context mContext,int priority){
        if(Utils.getCurrentPriority(mContext)> priority){
            return false;
        }
        Utils.exitVoiceAssistant();
        ActiveServiceViewManager.getInstance(mContext).clearCurrentWindow();
        return true;
    }


    /*
     * author:liuhong
     * date: 2019-05-16
     * 通过数据库查询TTS文案,查询完成后进行TTS播报.
     * TTS语音播报,此方法仅适用于没有通配符的文案。
     * defaultTts: 默认tts,存放在ttslib.xml文件中,在数据库查询tts失败或者未查询到tts时使用.防止TTS不播报的情况
     */
    public static void getMessageWithTtsSpeak(Context context, String conditionId, String defaultTts, int priority,TTSController.OnTtsStoppedListener listener) {
        Log.d(TAG,"lh:conditionId1:" + conditionId+",defaultTts:"+defaultTts);

        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG,"lh:conditionId1:" + conditionId+"，callback:"+ttsInfo.getTtsText());
                String tts = ttsInfo.getTtsText();
                if (ttsInfo.getTtsText().isEmpty()){
                    tts = context.getString(R.string.msg_tts_isnull).toString();
                }
                startTTS(tts,priority,listener);
            }

            @Override
            public void onFail() {
                startTTS(defaultTts,priority,listener);
            }
        });
    }





    //播报接口，showText: true进行UI显示和回调.  false : 不进行UI显示和回调
    public static void getMessageWithTtsSpeakOnly(boolean showText, Context context, String conditionId, String defaultTts,int priority) {
        Log.d(TAG, "lh:conditionId2:" + conditionId + ",defaultTts:" + defaultTts);

        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG, "lh:conditionId2:" + conditionId + "，callback:" + ttsInfo.getTtsText());
                if(showText) {
                    startTTSOnly(ttsInfo.getTtsText(),priority);
                } else {
                    TTSController.getInstance(context).startTTSOnly(ttsInfo.getTtsText(),priority);
                }
                //WIFI或蓝牙连接状态变化时，需要弹出toast（0825蓝牙已连接、已断开由蓝牙电话弹toast）
                if(conditionId == SYSTEMC47CONDITION || conditionId == SYSTEMC48CONDITION){
                    MyToast.showToast(context,ttsInfo.getTtsText(), true);
                }
            }

            @Override
            public void onFail() {
                if(showText) {
                    startTTSOnly(defaultTts,priority);
                } else {
                    TTSController.getInstance(context).startTTSOnly(defaultTts,priority);
                }
                //WIFI或蓝牙连接状态变化时，需要弹出toast
                if(conditionId == SYSTEMC47CONDITION || conditionId == SYSTEMC48CONDITION){
                    MyToast.showToast(context,defaultTts, true);
                }
            }
        });
    }

    public static void startTTSOnly(String text,int priority) {
        startTTSOnly(text, null);
    }


    public static void startTTS(String ttsText,int priority, TTSController.OnTtsStoppedListener listener) {
        Context context = BaseApplication.getInstance();
        if(!FloatViewManager.getInstance(context).isHide()) {
            EventBusUtils.sendMainMessage(ttsText);
        }
        TTSController.getInstance(context).startTTS(ttsText,priority,listener);
    }

    /**
     * 播报并且在播报结束不重新开启识别，不更新动画
     *
     * @param ttsText
     * @param listener
     */
    public static void startTTSOnly(String ttsText,int priority, TTSController.OnTtsStoppedListener listener) {
        Context context = BaseApplication.getInstance();
        if (!FloatViewManager.getInstance(context).isHide()) {
            EventBusUtils.sendMainMessage(ttsText);
        }
        TTSController.getInstance(context).startTTSOnly(ttsText, priority,listener);
    }

    //只进行播报接口，不进行UI显示和回调
    public static void getMessageWithTtsSpeakOnly(Context context, String conditionId, String defaultTts, int priority,TTSController.OnTtsStoppedListener listener) {
        Log.d(TAG, "lh:conditionId2:" + conditionId + ",defaultTts:" + defaultTts);

        TtsUtils.getInstance(context).getTtsMessage(conditionId, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                TtsInfo ttsInfo = ttsInfoList.get(new Random().nextInt(ttsInfoList.size()));
                Log.d(TAG, "lh:conditionId2:" + conditionId + "，callback:" + ttsInfo.getTtsText());
                String words = ttsInfo.getTtsText();
                if (words.isEmpty()){
                    words = context.getString(R.string.msg_tts_isnull).toString();
                }
                startTTSOnly(words, priority,listener);
            }

            @Override
            public void onFail() {
                startTTSOnly(defaultTts,priority, listener);
            }
        });
    }


    /////////////////////语音播报优先级增加接口   end/////////////////////////////

    public static void sendBroadcastForHideSoftBoard(Context c) {
        try {
            /*
            adb shell am broadcast -a com.sinovoice.hcicloudinputvehicle --es action "hide"
             */
            Intent intent = new Intent("com.sinovoice.hcicloudinputvehicle");
            intent.putExtra("action", "hide");
            c.sendBroadcast(intent);
        } catch (Exception e) {}
    }
}
