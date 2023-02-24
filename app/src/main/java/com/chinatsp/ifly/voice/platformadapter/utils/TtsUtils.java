package com.chinatsp.ifly.voice.platformadapter.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.db.TtsInfoDbDao;
import com.chinatsp.ifly.db.entity.ProjectInfo;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import kotlin.jvm.internal.PropertyReference0Impl;


public class TtsUtils {
    private static final String TAG = "TtsUtils";
    private static TtsUtils sInstance;
    private Context mContext;
    private HashMap<String,String> mTTSMarks;
    private int mConfigureSpeed = 60;
    private String mTryGuideTts ;


    private Handler mHandler ;
    private TtsUtils(Context context) {
        this.mContext = context;
        HandlerThread thread = new HandlerThread("getTTS");
        thread.start();
        mHandler = new android.os.Handler(thread.getLooper());
        mTTSMarks= new HashMap<>();
    }

    public static TtsUtils getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TtsUtils.class) {
                if (sInstance == null) {
                    sInstance = new TtsUtils(context);
                }
            }
        }
        return sInstance;
    }

    //获取Tts文案信息
    public void getTtsMessage(final String conditionId, final OnCallback callback) {
        Log.d(TAG, " start-----getTtsMessage() called with: conditionId = [" + conditionId + "]");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(conditionId)) {
                    callback.onFail();
                    return;
                }
                Log.d(TAG, "run() called::"+Thread.currentThread().getName()+"...."+conditionId);
                List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(conditionId);
                if (ttsInfoList == null || ttsInfoList.size() == 0) {
                    callback.onFail();
                } else {
                    callback.onSuccess(ttsInfoList);
                }
            }
        });
       /* ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(conditionId)) {
                    callback.onFail();
                    return;
                }
                List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(conditionId);
                if (ttsInfoList == null || ttsInfoList.size() == 0) {
                    callback.onFail();
                } else {
                    callback.onSuccess(ttsInfoList);
                }
            }
        });*/
    }

    public void getTXZConfigeration(){
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                String urlSpeakTTS,uslGetToken,clientId,clientSecret;
                List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MAINCURLSPEAKTTS);
                if (ttsInfoList != null&&ttsInfoList.size()>0) {
                    urlSpeakTTS = ttsInfoList.get(0).getTtsText();
                    Log.d(TAG, "run() called:"+urlSpeakTTS);
                }else
                    urlSpeakTTS = "";

                ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MAINCURLGETTOKEN);
                if (ttsInfoList != null&&ttsInfoList.size()>0) {
                    uslGetToken = ttsInfoList.get(0).getTtsText();
                    Log.d(TAG, "run() called:"+uslGetToken);
                }else
                    uslGetToken = "";

                SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_URL_SPEAKTTS,urlSpeakTTS);
                SharedPreferencesUtils.saveString(mContext, AppConstant.KEY_URL_GETTOKEN,uslGetToken);

               /* ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MAINCCLIENTID);
                if (ttsInfoList != null&&ttsInfoList.size()>0) {
                    clientId = ttsInfoList.get(0).getTtsText();
                    String sClientId = AESUtil.decrypt(clientId,"changanttsF202_01");
                    Log.d(TAG, "run() called:sClientId::"+sClientId);
                }

                ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MAINCCLIENTSECRET);
                if (ttsInfoList != null&&ttsInfoList.size()>0) {
                    clientSecret = ttsInfoList.get(0).getTtsText();
                    String sClentSecret = AESUtil.decrypt(clientSecret,"changanttsF202_01");
                    Log.d(TAG, "run() called:sClientId::"+sClentSecret);
                }*/
            }
        });
    }

    public void getTTsMarks(){
        List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MAINMARK);
        for (int i = 0; i <ttsInfoList.size() ; i++) {
            String text = ttsInfoList.get(i).getTtsText();
            int index = text.indexOf("|");
            mTTSMarks.put(text.substring(0,index),text.substring(index+1,text.length()));
            Log.d(TAG, "getTTsMarks() called:"+text.substring(0,index)+"..."+text.substring(index+1,text.length()));
        }
    }

    public String replaceTTSMark(String text){
        for(String key:mTTSMarks.keySet()){
             if(text.contains(key)){
                 text = text.replace(key,mTTSMarks.get(key));
                 return text;
             }
        }
        return text;
    }

    public interface OnCallback {
        void onSuccess(List<TtsInfo> ttsInfoList);

        void onFail();
    }

    public interface OnConfirmInterface {
        void onConfirm(String tts);
    }

    public void getOnlineConfigureSpeed(){
        List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MINSPEED);
        try {
            if(ttsInfoList.size()>0)
                mConfigureSpeed = Integer.parseInt(ttsInfoList.get(0).getTtsText());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getOnlineConfigureSpeed() called::"+mConfigureSpeed);
    }

    public int getConfigureSpeed(){
        return mConfigureSpeed;
    }

    public void getDbTryGuideTts(){
        List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MAINC12CONDITION);
        try {
            if(ttsInfoList.size()>0)
                mTryGuideTts = ttsInfoList.get(0).getTtsText();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getDbTryGuideTts() called::"+mTryGuideTts);
    }

    public String getTryGuideTts(){
        return mTryGuideTts;
    }

    //获取顺风耳显示列表数据，通过setting共享给顺风耳app
    public void getFfrTts(){
        StringBuilder builder = new StringBuilder();
        List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo(TtsConstant.MAINCSFE);

        if(ttsInfoList!=null&&ttsInfoList.size()==1){
            try {
                Log.d(TAG, "getFfrTts() called:"+ttsInfoList.get(0));
                JSONArray jsonarray = new JSONArray(ttsInfoList.get(0).getTtsText());
                List lists = new ArrayList<>();
                for (int i = 0; i < jsonarray.length(); i++) {
                    String name = (String) jsonarray.getJSONObject(i).get("name");
                    int visible = (int) jsonarray.getJSONObject(i).get("isvisible");
                    if(visible==1){
                        if(!"".equals(builder.toString()))
                            builder.append(",");
                        builder.append(name);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getFfrTts() called:"+builder.toString());
        Settings.Global.putString( mContext.getContentResolver(),"voice_support_phone_list",builder.toString());
    }


}
