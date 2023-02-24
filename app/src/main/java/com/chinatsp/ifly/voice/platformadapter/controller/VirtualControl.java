package com.chinatsp.ifly.voice.platformadapter.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.ApaConstant;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.service.FloatViewIdleService;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;

import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;

import okhttp3.Route;

public class VirtualControl extends BroadcastReceiver {

    private static String TAG = "VirtualControl";
    private static VirtualControl mVirtualControl;
    public static VirtualControl getInstance(){
        if(mVirtualControl==null){
            synchronized (VirtualControl.class){
                if(mVirtualControl==null)
                    mVirtualControl = new VirtualControl();
            }
        }
        return mVirtualControl;
    }

    private static final String ACTION_CHINATSP_VIRTUAL = "chinatsp.action.virtual.tts";

    private static final String EXTRA_LIGHT = "EXTRA_LIGHT";
    private static final String EXTRA_WASH_IN = "EXTRA_WASH_IN";
    private static final String EXTRA_WASH_OUT = "EXTRA_WASH_OUT";
    private static final String EXTRA_SLEEP_P = "EXTRA_SLEEP_P";
    private static final String EXTRA_SLEEP = "EXTRA_SLEEP";
    private static final String EXTRA_SLEEP_OUT = "EXTRA_SLEEP_OUT";
    private static final String EXTRA_NAVI_SPEAK_SWITCH = "EXTRA_NAVI_SPEAK_SWITCH";
    private static final String EXTRA_NAVI_LOAD_SWITCH = "EXTRA_NAVI_LOAD_SWITCH";
    private static final String EXTRA_NAVI_ANGLE = "EXTRA_NAVI_ANGLE";
    private static final String EXTRA_NAVI_CONTROL = "EXTRA_NAVI_CONTROL";
    private static final String EXTRA_NAVI_EXIT = "EXTRA_NAVI_EXIT";
    private static final String EXTRA_NAVI_ROUTE_PREFERENCE = "EXTRA_NAVI_ROUTE_PREFERENCE";
    private static final String EXTRA_NAVI_HOME = "EXTRA_NAVI_HOME";
    private static final String EXTRA_NAVI_YUN = "EXTRA_NAVI_YUN";
    private static final String EXTRA_NAVI_PLAY = "EXTRA_NAVI_PLAY";
    private static final String EXTRA_NAVI_NEXT = "EXTRA_NAVI_NEXT";
    private static final String EXTRA_NAVI_WEATHER = "EXTRA_NAVI_WEATHER";
    private static final String EXTRA_NAVI_COMPANY = "EXTRA_NAVI_COMPANY";

    private static final String EXTRA_360_2D_FRONT = "EXTRA_360_2D_FRONT";
    private static final String EXTRA_360_2D_BACK = "EXTRA_360_2D_BACK";
    private static final String EXTRA_360_2D_LEFT = "EXTRA_360_2D_LEFT";
    private static final String EXTRA_360_2D_RIGHT = "EXTRA_360_2D_RIGHT";
    private static final String EXTRA_360_2D_LEFT_RIGHT = "EXTRA_360_2D_LEFT_RIGHT";
    private static final String EXTRA_360_3D_FRONT = "EXTRA_360_3D_FRONT";
    private static final String EXTRA_360_3D_BACK = "EXTRA_360_3D_BACK";
    private static final String EXTRA_360_3D_LEFT = "EXTRA_360_3D_LEFT";
    private static final String EXTRA_360_3D_RIGHT = "EXTRA_360_3D_RIGHT";
    private static final String EXTRA_360_3D = "EXTRA_360_3D";
    private static final String EXTRA_360_2D = "EXTRA_360_2D";

    private static final String EXTRA_DVR_FRONT = "EXTRA_DVR_FRONT";
    private static final String EXTRA_DVR_BACK = "EXTRA_DVR_BACK";
    private static final String EXTRA_DVR_LEFT = "EXTRA_DVR_LEFT";
    private static final String EXTRA_DVR_RIGHT = "EXTRA_DVR_RIGHT";
    private static final String EXTRA_DVR_CONTROL = "EXTRA_DVR_CONTROL";

    private static final String EXTRA_SAI_EXIT = "EXTRA_SAI_EXIT";
    private static final String EXTRA_SAI_HOME = "EXTRA_SAI_HOME";

    private static final  int TIME_DELAY_SHOWING = 600;
    private static final  int TIME_DELAY_SHOWING_1000 = 1000;

    private Context mContext;

    private Handler mHandler = new Handler(){};
    private HashMap<String,String> mVirtualMaps;

    public void registerVirtualCast(Context context){

        mContext = context;
        mVirtualMaps = new HashMap<>();
        IntentFilter filter = new IntentFilter(ACTION_CHINATSP_VIRTUAL);
        context.registerReceiver(this,filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String extra = intent.getStringExtra("extra");
        String angle = intent.getStringExtra("angle");
        Log.d(TAG, "onReceive() called with: extra = [" + extra + "], angle = [" + angle + "]");
        if(EXTRA_LIGHT.equals(extra)){
            startTTS(R.string.key_btnC79,R.string.condition_btnc79,TtsConstant.GUIDEBTNC79CONDITION,mContext.getString(R.string.btn79));
        }else if(EXTRA_WASH_IN.equals(extra)){
            startTTSAlways(R.string.key_btnC80,R.string.condition_btnc80,TtsConstant.GUIDEBTNC80CONDITION,mContext.getString(R.string.btn80));
        } else if(EXTRA_WASH_OUT.equals(extra)){
            startTTSAlways(R.string.key_btnC81,R.string.condition_btnc81,TtsConstant.GUIDEBTNC81CONDITION,mContext.getString(R.string.btn81));
        }else if(EXTRA_SLEEP_P.equals(extra)){
            if(canTtsByGuide())
               startTTSShowUI(R.string.key_btnC82,R.string.condition_btnc82,TtsConstant.GUIDEBTNC82CONDITION,mContext.getString(R.string.btn82));
        }else if(EXTRA_SLEEP.equals(extra)){
            if(canTtsByGuide())
               startTTSShowUI(R.string.key_btnC83,R.string.condition_btnc83,TtsConstant.GUIDEBTNC83CONDITION,mContext.getString(R.string.btn83));
        }else if(EXTRA_SLEEP_OUT.equals(extra)){
            boolean guide = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_BROADCAST,AppConstant.VALUE_VOICE_SPEAK);
            if(guide)
                startTTSBySpeak(R.string.key_btnC83,R.string.condition_btnc83_1,TtsConstant.GUIDEBTNC83_1CONDITION,mContext.getString(R.string.btn83_1));
        }else if(EXTRA_NAVI_SPEAK_SWITCH.equals(extra)){
            if(canTtsBySpeak())
                startTTSOnce(R.string.key_btnC84,R.string.condition_btnc84,TtsConstant.GUIDEBTNC84CONDITION,mContext.getString(R.string.btn84));
        }else if(EXTRA_NAVI_LOAD_SWITCH.equals(extra)){
            if(canTtsBySpeak())
                startTTSOnce(R.string.key_btnC85,R.string.condition_btnc85,TtsConstant.GUIDEBTNC85CONDITION,mContext.getString(R.string.btn85));
        }else if(EXTRA_NAVI_ANGLE.equals(extra)){//TODO 等待联调，参数值写死
            if("3D模式".equals(angle))
                angle = "三<cnphone py=san1>D模式";
            startTTSAlwaysBySpeak(R.string.key_btnC86,R.string.condition_btnc86,TtsConstant.GUIDEBTNC86CONDITION,angle);
        }else if(EXTRA_NAVI_CONTROL.equals(extra)){
            if(canTtsByGuide())
                startTTSShowUI(R.string.key_btnC87,R.string.condition_btnc87,TtsConstant.GUIDEBTNC87CONDITION,mContext.getString(R.string.btn87));
        }else if(EXTRA_NAVI_EXIT.equals(extra)){
            if(canTtsByGuide())
                 startTTSShowUI(R.string.key_btnC88,R.string.condition_btnc88,TtsConstant.GUIDEBTNC88CONDITION,mContext.getString(R.string.btn88));
        }else if(EXTRA_NAVI_ROUTE_PREFERENCE.equals(extra)){
//            if(canTtsByGuide())
            boolean guide = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_GUIDE,AppConstant.VALUE_VOICE_GUIDE);
            if(guide)  //每次触发都播报
                startTTSShowUI(R.string.key_btnC89,R.string.condition_btnc89,TtsConstant.GUIDEBTNC89CONDITION,mContext.getString(R.string.btn89));
        }else if(EXTRA_NAVI_YUN.equals(extra)){
            if(canTtsByGuide())
                startTTSShowUI(R.string.key_btnC90,R.string.condition_btnc90,TtsConstant.GUIDEBTNC90CONDITION,mContext.getString(R.string.btn90));
        }else if(EXTRA_NAVI_PLAY.equals(extra)){
            if(canTtsByGuide())
                startTTSShowUI(R.string.key_btnC91,R.string.condition_btnc91,TtsConstant.GUIDEBTNC91CONDITION,mContext.getString(R.string.btn91));
        }else if(EXTRA_NAVI_NEXT.equals(extra)){
            if(canTtsByGuide())
                startTTSShowUI(R.string.key_btnC92,R.string.condition_btnc92,TtsConstant.GUIDEBTNC92CONDITION,mContext.getString(R.string.btn92));
        }else if(EXTRA_NAVI_WEATHER.equals(extra)){
            startTTS(R.string.key_btnC95,R.string.condition_btnc95,TtsConstant.GUIDEBTNC95CONDITION,mContext.getString(R.string.btn95));
        }else if(EXTRA_NAVI_COMPANY.equals(extra)){
            startTTS(R.string.key_btnC94,R.string.condition_btnc94,TtsConstant.GUIDEBTNC94CONDITION,mContext.getString(R.string.btn94));
        }else if(EXTRA_NAVI_HOME.equals(extra)){
            startTTS(R.string.key_btnC93,R.string.condition_btnc93,TtsConstant.GUIDEBTNC93CONDITION,mContext.getString(R.string.btn93));
        }

       /* else if(EXTRA_360_2D_FRONT.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC96CONDITION,mContext.getString(R.string.btn96));
        }else if(EXTRA_360_2D_BACK.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC97CONDITION,mContext.getString(R.string.btn97));
        }else if(EXTRA_360_2D_LEFT.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC98CONDITION,mContext.getString(R.string.btn98));
        }else if(EXTRA_360_2D_RIGHT.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC99CONDITION,mContext.getString(R.string.btn99));
        }else if(EXTRA_360_2D_LEFT_RIGHT.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC100CONDITION,mContext.getString(R.string.btn100));
        }else if(EXTRA_360_3D_FRONT.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC101CONDITION,mContext.getString(R.string.btn101));
        }else if(EXTRA_360_3D_BACK.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC102CONDITION,mContext.getString(R.string.btn102));
        }else if(EXTRA_360_3D_LEFT.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC103CONDITION,mContext.getString(R.string.btn103));
        }else if(EXTRA_360_3D_RIGHT.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC104CONDITION,mContext.getString(R.string.btn104));
        }else if(EXTRA_360_2D.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC105CONDITION,mContext.getString(R.string.btn105));
        }else if(EXTRA_360_3D.equals(extra)){
            startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
        }*/

        else if(EXTRA_DVR_FRONT.equals(extra)){
            startTTS(R.string.key_btnC107,R.string.condition_btnc107,TtsConstant.GUIDEBTNC107CONDITION,mContext.getString(R.string.btn107));
        }else if(EXTRA_DVR_BACK.equals(extra)){
            startTTS(R.string.key_btnC108,R.string.condition_btnc108,TtsConstant.GUIDEBTNC108CONDITION,mContext.getString(R.string.btn108));
        }else if(EXTRA_DVR_LEFT.equals(extra)){
            startTTS(R.string.key_btnC109,R.string.condition_btnc109,TtsConstant.GUIDEBTNC109CONDITION,mContext.getString(R.string.btn109));
        }else if(EXTRA_DVR_RIGHT.equals(extra)){
            startTTS(R.string.key_btnC110,R.string.condition_btnc110,TtsConstant.GUIDEBTNC110CONDITION,mContext.getString(R.string.btn110));
        }else if(EXTRA_DVR_CONTROL.equals(extra)){
            startTTS(R.string.key_btnC111,R.string.condition_btnc111,TtsConstant.GUIDEBTNC111CONDITION,mContext.getString(R.string.btn111));
        }

        else if(EXTRA_SAI_EXIT.equals(extra)){
            startTTS(R.string.key_btnC116,R.string.condition_btnc116,TtsConstant.GUIDEBTNC116CONDITION,mContext.getString(R.string.btn116));
        } else if(EXTRA_SAI_EXIT.equals(extra)){
            startTTS(R.string.key_btnC117,R.string.condition_btnc117,TtsConstant.GUIDEBTNC117CONDITION,mContext.getString(R.string.btn117));
        }

    }

    public void handle360Tts(String ttsString){
       switch (ttsString){
           case ApaConstant.APA101:
               start360TTS(R.string.key_btnC96,R.string.condition_btnc96,TtsConstant.GUIDEBTNC96CONDITION,mContext.getString(R.string.btn96));
               return;
           case ApaConstant.APA102:
               start360TTS(R.string.key_btnC97,R.string.condition_btnc97,TtsConstant.GUIDEBTNC97CONDITION,mContext.getString(R.string.btn97));
               return;
           case ApaConstant.APA103:
               start360TTS(R.string.key_btnC98,R.string.condition_btnc98,TtsConstant.GUIDEBTNC98CONDITION,mContext.getString(R.string.btn98));
               return;
           case ApaConstant.APA104:
               start360TTS(R.string.key_btnC99,R.string.condition_btnc99,TtsConstant.GUIDEBTNC99CONDITION,mContext.getString(R.string.btn99));
               return;
           case ApaConstant.APA105:
               start360TTS(R.string.key_btnC100,R.string.condition_btnc100,TtsConstant.GUIDEBTNC100CONDITION,mContext.getString(R.string.btn100));
               return;
          /* case ApaConstant.APA106:
               startTTS(TtsConstant.GUIDEBTNC101CONDITION,mContext.getString(R.string.btn101));
               return;
           case ApaConstant.APA107:
               startTTS(TtsConstant.GUIDEBTNC102CONDITION,mContext.getString(R.string.btn102));
               return;*/
           case ApaConstant.APA108:
               start360TTS(R.string.key_btnC101,R.string.condition_btnc101,TtsConstant.GUIDEBTNC101CONDITION,mContext.getString(R.string.btn101));
               return;
           case ApaConstant.APA109:
               start360TTS(R.string.key_btnC102,R.string.condition_btnc102,TtsConstant.GUIDEBTNC102CONDITION,mContext.getString(R.string.btn102));
               return;
           case ApaConstant.APA10a:
               start360TTS(R.string.key_btnC103,R.string.condition_btnc103,TtsConstant.GUIDEBTNC103CONDITION,mContext.getString(R.string.btn103));
               return;
           case ApaConstant.APA10b:
               start360TTS(R.string.key_btnC104,R.string.condition_btnc104,TtsConstant.GUIDEBTNC104CONDITION,mContext.getString(R.string.btn104));
               return;
          /* case ApaConstant.APA10c:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA10d:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA10e:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA10f:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA110:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;*/
           case ApaConstant.APA111:
//               start360TTS(TtsConstant.GUIDEBTNC105CONDITION,mContext.getString(R.string.btn105));
               return;
           case ApaConstant.APA112:
//               start360TTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           /*case ApaConstant.APA113:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;*/
        /*   case ApaConstant.APA114:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA115:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA116:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA117:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA118:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;
           case ApaConstant.APA119:
               startTTS(TtsConstant.GUIDEBTNC106CONDITION,mContext.getString(R.string.btn106));
               return;*/
       }
    }

    /**
     * 后台引导 洗车模式  休息模式
     * @param conditionId
     * @param defaultText
     */
    private void startTTSAlways(int virtureKey,int condition,String  conditionId,String defaultText) {
        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "startTTSShowUI: Utils.getCurrentPriority(mContext):::"+Utils.getCurrentPriority(mContext));
            return;
        }
        Log.d(TAG, "startTTS() called with: conditionId = [" + conditionId + "], defaultText = [" + defaultText + "]");
        boolean guide = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_GUIDE,AppConstant.VALUE_VOICE_GUIDE);
        if(guide){
            Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText,PriorityControler.PRIORITY_THREE,exitListener);
            Utils.eventTrack(mContext, R.string.skill_guide, virtureKey, virtureKey, conditionId, condition,defaultText);
        }

    }

    private void startTTSAlwaysBySpeak(int virtureKey,int condition,String  conditionId,String defaultText) {
        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "startTTSShowUI: Utils.getCurrentPriority(mContext):::"+Utils.getCurrentPriority(mContext));
            return;
        }
        Log.d(TAG, "startTTS() called with: conditionId = [" + conditionId + "], defaultText = [" + defaultText + "]");
        boolean speak = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_BROADCAST,AppConstant.VALUE_VOICE_SPEAK);
        if(speak){
            Utils.startTTSOnly(defaultText,PriorityControler.PRIORITY_THREE,exitListener);
//            Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText,PriorityControler.PRIORITY_THREE,exitListener);
            Utils.eventTrack(mContext, R.string.skill_guide, virtureKey, virtureKey, conditionId, condition,defaultText);
        }

    }

    /**
     * 后台引导
     * @param conditionId
     * @param defaultText
     */
    private void start360TTS(int virtureKey,int condition,String  conditionId,String defaultText) {
        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "startTTSShowUI: Utils.getCurrentPriority(mContext):::"+Utils.getCurrentPriority(mContext));
            return;
        }
        String hasSpeak =  mVirtualMaps.get(conditionId);//为空，说明没有播报过
        Log.d(TAG, "startTTS() called with: conditionId = [" + conditionId + "], defaultText = [" + defaultText + "]"+hasSpeak);
        if(can360TtsByGuide()&&hasSpeak==null){
            mVirtualMaps.put(conditionId,"1");
            Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText,PriorityControler.PRIORITY_THREE,exitListener);
            Utils.eventTrack(mContext, R.string.skill_guide, virtureKey, virtureKey, conditionId,condition,defaultText);
        }

    }

    /**
     * 后台引导
     * @param conditionId
     * @param defaultText
     */
    private void startTTS(int virtureKey,int condition, String  conditionId,String defaultText) {
        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "startTTSShowUI: Utils.getCurrentPriority(mContext):::"+Utils.getCurrentPriority(mContext));
            return;
        }
        String hasSpeak =  mVirtualMaps.get(conditionId);//为空，说明没有播报过
        Log.d(TAG, "startTTS() called with: conditionId = [" + conditionId + "], defaultText = [" + defaultText + "]"+hasSpeak);
        if(canTtsByGuide()&&hasSpeak==null){
            mVirtualMaps.put(conditionId,"1");
            Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText,PriorityControler.PRIORITY_THREE,exitListener);
            Utils.eventTrack(mContext, R.string.skill_guide, virtureKey, virtureKey, conditionId,condition,defaultText);
        }

    }

    /**
     * 通过车机触摸按钮或踩刹车退出休息模式
     * @param conditionId
     * @param defaultText
     */
    private void startTTSBySpeak(int virtureKey,int condition,String  conditionId,String defaultText) {
        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "startTTSShowUI: Utils.getCurrentPriority(mContext):::"+Utils.getCurrentPriority(mContext));
            return;
        }
        Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText,PriorityControler.PRIORITY_THREE,exitListener);
        Utils.eventTrack(mContext, R.string.skill_guide, virtureKey, virtureKey, conditionId, condition,defaultText);

    }

    /**
     * 弹窗引导
     * @param conditionId
     * @param defaultText
     */
    private void startTTSShowUI(int virtureKey,int condition,String  conditionId,String defaultText){
        int delayTime = TIME_DELAY_SHOWING;
        if(TtsConstant.GUIDEBTNC90CONDITION.equals(conditionId))
            delayTime = TIME_DELAY_SHOWING_1000;    //针对云标，延迟，防止进入地图界面系统卡顿，导致界面加载延迟
        String hasSpeak =  mVirtualMaps.get(conditionId);//为空，说明没有播报过
        if(hasSpeak!=null){
            Log.e(TAG, "startTTSShowUI: "+hasSpeak);
            return;
        }
        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "startTTSShowUI: Utils.getCurrentPriority(mContext):::"+Utils.getCurrentPriority(mContext));
            return;
        }
        if(!TtsConstant.GUIDEBTNC89CONDITION.equals(conditionId))  //路线偏好设置每次均播报
            mVirtualMaps.put(conditionId,"1");
        showAssistant();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText,PriorityControler.PRIORITY_THREE,exitListener);
            }
        },delayTime);
        Utils.eventTrack(mContext, R.string.skill_guide, virtureKey, virtureKey, conditionId, condition,defaultText);

    }

    private void requestAudioFocus(Context context){
        if(!context.getPackageName().equals(AudioFocusUtils.getInstance(context).getCurrentActiveAudioPkg())) {
            AudioFocusUtils.getInstance(context).requestVoiceAudioFocus(AudioManager.STREAM_ALARM);
        }
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    private void getMessageWithoutTtsSpeakOnly(Context context, String conditionId,String defaultTTS,TTSController.OnTtsStoppedListener listener){
        Utils.getMessageWithoutTtsSpeak(context, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTTS;
                }
                Utils.startTTSOnly(ttsText, PriorityControler.PRIORITY_THREE,listener);

            }
        });
    }

    /**
     * 语音引导
     * @return
     */
    private boolean can360TtsByGuide(){
        boolean guide = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_GUIDE,AppConstant.VALUE_VOICE_GUIDE);

        Log.d(TAG, "handle360Tts() called with: guide = [" + guide + "]"+CarUtils.getInstance(mContext).isReverse()+"...."+CarUtils.getInstance(mContext).getSpeed());
        if(!guide){
            Log.e(TAG, "handle360Tts: the guide::"+guide);
            return false;
        }

        if(CarUtils.getInstance(mContext).isReverse()){  //非R档车速大于1km/h，R档不限定车速
            return true;
        }

        //不是R档
        if(CarUtils.getInstance(mContext).getSpeed()>1)
            return true;
        else
            return false;
        /*if(CarUtils.getInstance(mContext).getSpeed()<TtsUtils.getInstance(mContext).getConfigureSpeed()){
            Log.e(TAG, "handle360Tts: speed:"+CarUtils.getInstance(mContext).getSpeed()+"..."+TtsUtils.getInstance(mContext).getConfigureSpeed());
            return false;
        }*/
    }

    /**
     * 语音引导
     * @return
     */
    private boolean canTtsByGuide(){
        boolean guide = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_GUIDE,AppConstant.VALUE_VOICE_GUIDE);

        Log.d(TAG, "handle360Tts() called with: guide = [" + guide + "]");
        if(!guide){
            Log.e(TAG, "handle360Tts: the guide::"+guide);
            return false;
        }
        if(CarUtils.getInstance(mContext).getSpeed()<TtsUtils.getInstance(mContext).getConfigureSpeed()){
            Log.e(TAG, "handle360Tts: speed:"+CarUtils.getInstance(mContext).getSpeed()+"..."+TtsUtils.getInstance(mContext).getConfigureSpeed());
            return false;
        }
        return true;
    }

    /**
     * 语音播报
     * @return
     */
    private boolean canTtsBySpeak(){
        boolean guide = SharedPreferencesUtils.getBoolean(mContext, AppConstant.KEY_VOICE_BROADCAST,AppConstant.VALUE_VOICE_SPEAK);

        Log.d(TAG, "handle360Tts() called with: guide = [" + guide + "]");
        if(!guide){
            Log.e(TAG, "handle360Tts: the guide::"+guide);
            return false;
        }
        if(CarUtils.getInstance(mContext).getSpeed()<TtsUtils.getInstance(mContext).getConfigureSpeed()){
            Log.e(TAG, "handle360Tts: speed:"+CarUtils.getInstance(mContext).getSpeed()+"..."+TtsUtils.getInstance(mContext).getConfigureSpeed());
            return false;
        }
        return true;
    }

    /**
     * 播报一次
     * @param conditionId
     * @param defaultText
     */
    private void startTTSOnce(int virtureKey,int condition, String  conditionId,String defaultText) {
        if(Utils.getCurrentPriority(mContext)>PriorityControler.PRIORITY_THREE){
            Log.e(TAG, "startTTSShowUI: Utils.getCurrentPriority(mContext):::"+Utils.getCurrentPriority(mContext));
            return;
        }
        String hasSpeak =  mVirtualMaps.get(conditionId);//为空，说明没有播报过
        Log.d(TAG, "startTTS() called with: conditionId = [" + conditionId + "], defaultText = [" + defaultText + "]"+hasSpeak);
        if(hasSpeak==null){
            mVirtualMaps.put(conditionId,"1");
            Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultText,PriorityControler.PRIORITY_THREE,exitListener);
            Utils.eventTrack(mContext, R.string.skill_guide, virtureKey, virtureKey, conditionId,condition,defaultText);
        }

    }

    private TTSController.OnTtsStoppedListener exitListener = new TTSController.OnTtsStoppedListener() {
        @Override
        public void onPlayStopped() {
            if (!FloatViewManager.getInstance(mContext).isHide()) {
                FloatViewManager.getInstance(mContext).hide();
            }
        }
    };

    public void clearTtsData(){
        if (mVirtualMaps != null) {
            mVirtualMaps.clear();
        }
    }
}
