package com.chinatsp.ifly.activeservice;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.service.TxzTtsService;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.txznet.tts.OnlineTTSModule;
import com.txznet.tts.TXZTTSInitManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxb on 2019/6/14.
 */

public class AudioSynthesis {

    private static final String TAG = "AudioSynthesis";
    private Context mContext;

    private List<String> TTS_list;

    private final static int AUDIO_LOADING = 1;
    private final static int AUDIO_SAVE_PCM = 2;

    private static AudioSynthesis model;


    public AudioSynthesis(Context context) {
        this.mContext = context;
    }

    public static AudioSynthesis getInstance(Context context) {
        if (model == null) {
            model = new AudioSynthesis(context);
        }
        return model;
    }

    @SuppressLint("HandlerLeak")
    private android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case AUDIO_LOADING:
                    Log.e("zheng", "zheng  AUDIO_LOADING");

                    ArrayList<String> backupList = new ArrayList<>();
                    backupList.addAll(TTS_list);

                    Intent intent = new Intent(mContext, TxzTtsService.class);
                    intent.setAction(AppConstant.ACTION_AUDIOSYNTHESIS);
                    intent.putStringArrayListExtra("tts_list", backupList);
                    mContext.startService(intent);

                    mHandler.sendEmptyMessageDelayed(AUDIO_SAVE_PCM, 10 * 1000);
                    break;
                case AUDIO_SAVE_PCM:
                    Log.e("zheng", "zheng  AUDIO_SAVE_PCM");
                    TXZTTSInitManager.getInstance().savePCM(TTS_list,saveStatusListener);
                    break;

            }
        }
    };


    public void GetAudio() {
        Log.e("zheng", "zheng  GetAudio");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getAudio();
            }
        });
    }

    public void getAudio(){
        TTS_list = new ArrayList<>();

        //白天启动车机
        TtsUtils.getInstance(mContext).getTtsMessage(TtsConstant.MSGC40CONDITION, new TtsUtils.OnCallback() {
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                //若文案不止一条,则随机选择其中一条,若只有一条,则返回该条;
                for (int i = 0; i < ttsInfoList.size(); i++) {
                    TtsInfo ttsInfo = ttsInfoList.get(i);
                    if ("HI，#NOW#好".equals(ttsInfo.getTtsText())) {
                        TTS_list.add("HI，上午好");
                        TTS_list.add("HI，中午好");
                        TTS_list.add("HI，下午好");
                    } else {
                        TTS_list.add(ttsInfo.getTtsText());
                    }
                }
                GetAudio1();
            }

            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案
                TTS_list = new ArrayList<>();
                TTS_list.add("你好，有事随时叫我");
                TTS_list.add("HI，很高兴见到你");
                TTS_list.add("HI，你好吗？");
                TTS_list.add("HI，上午好");
                TTS_list.add("HI，中午好");
                TTS_list.add("HI，下午好");
                TTS_list.add("你好，愿你有个好心情！");
                GetAudio1();
            }
        });
    }

    public void GetAudio1() {
        //早上启动车机
        TtsUtils.getInstance(mContext).getTtsMessage(TtsConstant.MSGC41CONDITION, new TtsUtils.OnCallback() {
            TtsInfo ttsInfo = null;
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                for (int i = 0; i < ttsInfoList.size(); i++) {
                    ttsInfo = ttsInfoList.get(i);
                    TTS_list.add(ttsInfo.getTtsText());
                }
                GetAudio2();
            }
            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案
                TTS_list.add("早上好，昨晚睡得好吗？");
                TTS_list.add("早，小欧陪你开启正能量的一天");
                TTS_list.add("HI，早上好！");
                TTS_list.add("Good morning");
                TTS_list.add("HI，吃早餐了吗？");
                TTS_list.add("亲爱的，早安");
                TTS_list.add("早，很高兴陪你度过美好的一天");
                TTS_list.add("早上好，愿你今天有个好心情！");

                GetAudio2();

            }

        });
    }

    public void GetAudio2() {

        //晚上启动车机
        TtsUtils.getInstance(mContext).getTtsMessage(TtsConstant.MSGC42CONDITION, new TtsUtils.OnCallback() {
            TtsInfo ttsInfo = null;
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                for (int i = 0; i < ttsInfoList.size(); i++) {
                    ttsInfo = ttsInfoList.get(i);
                    TTS_list.add(ttsInfo.getTtsText());
                }
                GetAudio3();
            }
            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案
                TTS_list.add("晚上好，不管多晚，小欧都陪着你");
                TTS_list.add("HI，晚上好！");
                TTS_list.add("晚上好，出门注意安全");
                TTS_list.add("HI，晚上一起出去浪");
                TTS_list.add("晚上好，好想你呀");
                TTS_list.add("嘿，今天过得怎么样，给小欧讲讲");
                GetAudio3();
            }
        });

    }

    public void GetAudio3() {
        //超过一天未启动车机
        TtsUtils.getInstance(mContext).getTtsMessage(TtsConstant.MSGC43CONDITION, new TtsUtils.OnCallback() {
            TtsInfo ttsInfo = null;
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                for (int i = 0; i < ttsInfoList.size(); i++) {
                    ttsInfo = ttsInfoList.get(i);
                    TTS_list.add(ttsInfo.getTtsText());
                }
                GetAudio4();
            }
            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案

                TTS_list.add("HI，你昨天没出现，小欧一直在想你");
                TTS_list.add("一日不见，如隔三秋，可别忘了小欧");
                GetAudio4();
            }
        });

    }

    public void GetAudio4() {
        //超过三天未启动车机
        TtsUtils.getInstance(mContext).getTtsMessage(TtsConstant.MSGC44CONDITION, new TtsUtils.OnCallback() {
            TtsInfo ttsInfo = null;
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                for (int i = 0; i < ttsInfoList.size(); i++) {
                    ttsInfo = ttsInfoList.get(i);
                    TTS_list.add(ttsInfo.getTtsText());
                }
                GetAudio5();
            }
            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案

                TTS_list.add("好久不见，你最近好吗？");
                TTS_list.add("没有你的日子度日如年，期待与你天天见！");
                GetAudio5();
            }
        });

    }

    public void GetAudio5() {

        //五天以上未用车
        TtsUtils.getInstance(mContext).getTtsMessage(TtsConstant.MSGC45CONDITION, new TtsUtils.OnCallback() {
            TtsInfo ttsInfo = null;
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                for (int i = 0; i < ttsInfoList.size(); i++) {
                    ttsInfo = ttsInfoList.get(i);
                    TTS_list.add(ttsInfo.getTtsText());
                }
                GetAudio6();
            }
            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案
                TTS_list.add("HI，你好吗？");
                GetAudio6();

            }
        });

    }

    public void GetAudio6() {
        //断电场景
        TtsUtils.getInstance(mContext).getTtsMessage(TtsConstant.MSGC46CONDITION, new TtsUtils.OnCallback() {
            TtsInfo ttsInfo = null;
            @Override
            public void onSuccess(List<TtsInfo> ttsInfoList) {
                for (int i = 0; i < ttsInfoList.size(); i++) {
                    ttsInfo = ttsInfoList.get(i);
                    TTS_list.add(ttsInfo.getTtsText());
                }

                mHandler.sendEmptyMessageDelayed(AUDIO_LOADING, 20 * 1000);
            }
            @Override
            public void onFail() {
                //查询失败后,回调tts为空,由调用端自行决定是否使用默认tts文案
                TTS_list.add("HI，你好吗？");
                mHandler.sendEmptyMessageDelayed(AUDIO_LOADING, 20 * 1000);
            }
        });
    }

    private OnlineTTSModule.TTSSaveStatusListener saveStatusListener = new OnlineTTSModule.TTSSaveStatusListener() {
        @Override
        public void onSaveError() {
            Log.d(TAG, "onSaveError() called");
        }

        @Override
        public void onSaveSuccess(List<String> list) {
            Log.d(TAG, "onSaveSuccess() called with: list = [" + list + "]");
            if(list!=null&&TTS_list!=null){
                if(list.size()!=TTS_list.size()){
                    Log.e(TAG, "onSaveSuccess: list.size()::"+list.size()+"..TTS_list.size():::"+TTS_list.size());
                    return;
                }
            }
            if (list.size() != 0) {
                SaveContentprovider(list, TTS_list);
            }
        }

        @Override
        public void onSaveDelay() {
            Log.d(TAG, "onSaveDelay() called");
        }
    };

    private void SaveContentprovider(List<String> Audio_list, List<String> list) {
        for (int i = 0; i < Audio_list.size(); i++) {//添加id从3开始，id=1为节假日，id=2为热点信息
            FestivalProvide_Shared(mContext, i + 3, "/storage/emulated/0/txz/online/" + Audio_list.get(i), list.get(i));
            LogUtils.e("zheng", "zheng /storage/emulated/0/txz/online/" + Audio_list.get(i) + "   i + 3:" + (i + 3));
            LogUtils.e("zheng", "zheng 文案：" + list.get(i));
        }
    }

    /**
     * 设置ContentProvider数据共享
     *
     * @param mContext
     * @param id
     * @param response
     */
    private void FestivalProvide_Shared(Context mContext, int id, String response, String text) {
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
        values.put("festival_json", response);
        values.put("festival_text", text);
        resolver.insert(uri_user, values);
        Log.e("FestivalProvide_Shared","FestivalProvide_Shared id:"+id+"response:"+response+"text:"+text);
    }
}
