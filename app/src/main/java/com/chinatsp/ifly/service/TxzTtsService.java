package com.chinatsp.ifly.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.adapter.ttsservice.MapTtsManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.LoadClass;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.utils.AESUtil;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.ttsservice.aidl.ITtsAgentListener;
import com.iflytek.adapter.ttsservice.aidl.TtsServiceAidl;
import com.iflytek.speech.util.NetworkUtil;
import com.txznet.tts.OfflineTTSModuleNew;
import com.txznet.tts.OnlineTTSModule;
import com.txznet.tts.TXZTTSInitManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TxzTtsService extends Service {

    public static final String TAG = "xyj_TxzTtsService";
    public static final int MAX_CLIENT_NUMBERS = 10;
    private static final String ACTION__TTS_INITED = "action.chinatsp.ifly.tts.init";
    private HashMap<Long, ITtsAgentListener> clientMap = new HashMap(MAX_CLIENT_NUMBERS);
    private boolean inited;
    private ITtsAgentListener curClient;

    private String action;
    private ArrayList<String> tts_list;
    private int infoType;
    private String message;
    private Handler mainHandler = new Handler();

    private Set<String> list = new HashSet<>();
    private HandlerThread handlerThread;
    private Handler handler;
    private SpeakRunnable speakRunnable;
    private String textNow;
    private boolean isPrepare;
    private int mMapStream = 12;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        handlerThread = new HandlerThread("speak");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initOfflineTts();
                initTxztts();
            }
        },1000*5);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            action = intent.getAction();
            if (AppConstant.ACTION_AUDIOSYNTHESIS.equals(action)) {
                tts_list = intent.getStringArrayListExtra("tts_list");
            } else if (AppConstant.ACTION_ACTIVESERVICE.equals(action)) {
                infoType = intent.getIntExtra("info_type", ActiveServiceModel.HOLIDAY);
                message = intent.getStringExtra("message");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }



    private TtsServiceAidl.Stub ttsService = new TtsServiceAidl.Stub() {
        @Override
        public boolean registerClient(ITtsAgentListener client, int streamType) throws RemoteException {
            if (client == null) {
                LogUtils.d(TAG, "client is null");
                return false;
            } else {
                LogUtils.d(TAG, "client is not null :" + client.getClientId()+"..");
                if(clientMap.containsKey(client.getClientId())){
                    Log.e(TAG, "registerClient: have the key");
                    clientMap.remove(client.getClientId());  //规避二次重连绑定不了的问题
//                    return false;
                }
                TxzTtsService.this.clientMap.put(client.getClientId(), client);
                try {
                    client.onTtsInited(inited, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }

        @Override
        public boolean releaseClient(ITtsAgentListener client) throws RemoteException {
            LogUtils.d(TAG, "releaseClient :" + client.getClientId());
            TxzTtsService.this.clientMap.remove(client.getClientId());
            return true;
        }

        @Override
        public int setParam(ITtsAgentListener client, int id, int value) throws RemoteException {
            Log.d(TAG, "setParam() called with: client = ["  + "], id = [" + id + "], value = [" + value + "]");
            if(id== MapTtsManager.TYPE_MAP){  //说明这是地图播报 在 MapTtsManager 定义
//                TXZTTSInitManager.getInstance().setDefaultStream(value);
                mMapStream = value;
            }
            return 0;
        }

        @Override
        public int startSpeak(ITtsAgentListener client, String text) throws RemoteException {

            Log.d(TAG, "startSpeak() called with: client = [" + client.getClientId() + "], text = [" + text + "]");

            //导航播报的时候，语音申请了焦点，导航停止了播报，
            // 语音此时要回调onPlayInterrupted，使导航的播报标志位置为false
            //语音没有主动的调用stoptts方法
            try {
                if(client!=null&&curClient!=null&&curClient.asBinder().isBinderAlive()){
                    if(curClient.getClientId()==MapTtsManager.MAP_ID
                    &&client.getClientId()!=MapTtsManager.MAP_ID){
                        curClient.onPlayInterrupted();
                    }

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if(client!=null&&client.getClientId()==MapTtsManager.MAP_ID){//1001在mapttsmanager定义
                //设置流类型，在setParam已定义
                //如果音频焦点在语音，不做播报
                //播放本地合成音频
                if(getPackageName().equals(AudioFocusUtils.getInstance(TxzTtsService.this).getCurrentActiveAudioPkg())){
                    return -1;
                }

                Utils.eventTrack(TxzTtsService.this, R.string.skill_main,R.string.scene_main_navi_tts,R.string.object_main_navi_tts, TtsConstant.MAINCNAVITTS,R.string.condition_default,text);

                text = text.trim();
                if (TextUtils.isEmpty(text)
                        ||text.equals("，")
                        ||text.equals("。")
                        ||text.equals(",")
                        ||text.equals("."))//如果是特殊字符，播放一小段音频
                    text = "<pause type=#3>";//这个标签也是播一小段空内容

                //清除上一个 curClient 防止回调，导致申请焦点（导航自己申请焦点）
                curClient = TxzTtsService.this.clientMap.get(client.getClientId());
                TXZTTSInitManager.getInstance().speakOffline(text,mMapStream,AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE ,offlineListener);
                return 0;
            }

            if(!inited){
                Log.e(TAG, "startSpeak: the txz is not inited!!" );
                return -1;
            }

            curClient = TxzTtsService.this.clientMap.get(client.getClientId());
            if (curClient == null) {
                LogUtils.d(TAG, "curClient == null or inited == false, exception:"+client.getClientId());
                return -1;
            }

            //最新的txz sdk 设置流类型
//            TXZTTSInitManager.getInstance().setDefaultStream(AudioAttributes.USAGE_ALARM);

            text = LoadClass.getInstance().getStringNew(text);
            textNow = text;
            isPrepare = false;
            //如果文本属于录音或者没有网络，直接走离线
            if ((list.contains(text) || !NetworkUtil.isNetworkAvailable(getApplicationContext()))) {
                LogUtils.d(TAG, "speak: offline");
                TXZTTSInitManager.getInstance().speakOffline(text,offlineListener);
            } else {
                LogUtils.d(TAG, "speak: online");
                String encode = null;
                try {
                    encode = URLEncoder.encode(text, "UTF-8");//仅对在线合成做符号转换
                    TXZTTSInitManager.getInstance().speakOnline(encode,onlineListener);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //长度超过30超时时间为4500ms.未超过为1200ms
                if (text.length() <= 30) {
                    handler.removeCallbacks(speakRunnable);
                    speakRunnable = new SpeakRunnable(text);//本地的就不做符号转换了
                    handler.postDelayed(speakRunnable, 2000);//0729
                } else {
                    handler.removeCallbacks(speakRunnable);
                    speakRunnable = new SpeakRunnable(text);
                    handler.postDelayed(speakRunnable, 4500);
                }
            }
            return 0;
        }

        @Override
        public int pauseSpeak(ITtsAgentListener client) throws RemoteException {
            return 0;
        }

        @Override
        public int resumeSpeak(ITtsAgentListener client) throws RemoteException {
            return 0;
        }

        @Override
        public int stopSpeak(ITtsAgentListener client) throws RemoteException {
            if (inited) {
                try {
                    Log.d("xyj", "stopSpeak in txzttsservice!!! ");
                    if(client!=null&&client.getClientId()==MapTtsManager.MAP_ID){//地图调用stop
                        if(curClient!=null&&curClient.getClientId()!=MapTtsManager.MAP_ID)//当前不是地图在播报，在无需调用cancel
                            return -1;
                        else{  //是地图在播报，则调用cancel
                            TXZTTSInitManager.getInstance().cancelSpeak();
                        }

                    }else { //不是地图调用stop，则直接cancelspeak
                        TXZTTSInitManager.getInstance().cancelSpeak();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }


            }
            return 0;
        }
    };

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called with: intent = [" + intent + "]");
        return this.ttsService;
    }


    private void SaveContentprovider(List<String> Audio_list, List<String> list) {
        for (int i = 0; i < Audio_list.size(); i++) {//添加id从3开始，id=1为节假日，id=2为热点信息
            FestivalProvide_Shared(getApplicationContext(), i + 3, "/storage/emulated/0/txz/online/" + Audio_list.get(i), list.get(i));
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

    private class SpeakRunnable implements Runnable {
        private String text;

        public SpeakRunnable(String text) {
            this.text = text;
        }

        @Override
        public void run() {
            LogUtils.d(TAG, "SpeakRunnable: " + text + " ,isPrepare:" + isPrepare);
            if (!isPrepare) {
                isPrepare = true;
                TXZTTSInitManager.getInstance().speakOffline(text,offlineListener);
            }
        }
    }


    private void sendTtsInited(){
        Intent intent = new Intent(ACTION__TTS_INITED);
        intent.putExtra("init",true);
        sendBroadcast(intent);
    }


    private OfflineTTSModuleNew.TTSStatusListener offlineListener = new OfflineTTSModuleNew.TTSStatusListener() {
        @Override
        public void onSuccess() {
            LogUtils.d(TAG, "offlineListener:; txz tts onSuccess:"+curClient);
            if (curClient != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            curClient.onPlayCompleted();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void onCancel() {
            LogUtils.d(TAG, "offlineListener::onCancel: "+curClient);
            handler.removeCallbacks(speakRunnable);
            if (curClient != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            curClient.onPlayInterrupted();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void onError() {
            LogUtils.d(TAG, "offlineListener::onError: "+isPrepare + getPackageName().equals(AudioFocusUtils.getInstance(TxzTtsService.this).getCurrentActiveAudioPkg()));
            try {
                if(curClient!=null&&curClient.getClientId()==MapTtsManager.MAP_ID){
                    mainHandler.post(new Runnable() {   //导航播报错误，直接回调成功标志，避免导航一直不发声
                        @Override
                        public void run() {
                            try {
                                curClient.onPlayCompleted();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (!isPrepare) {
                    handler.removeCallbacks(speakRunnable);
                    TXZTTSInitManager.getInstance().speakOffline(textNow+" ",offlineListener);//加一段空格，防止播报离线音频时错误，陷入死循环
                }else if(getPackageName().equals(AudioFocusUtils.getInstance(TxzTtsService.this).getCurrentActiveAudioPkg())){//说明是语音播报错误
                    if(!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide())
                        Utils.exitVoiceAssistant();
                    else
                        AudioFocusUtils.getInstance(TxzTtsService.this).releaseVoiceAudioFocus();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStart() {
            LogUtils.d(TAG, "offlineListener::txz tts onStart::"+curClient);
            if (curClient != null) {
                isPrepare = true;
                handler.removeCallbacks(speakRunnable);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            curClient.onPlayBegin();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    private OnlineTTSModule.TTSOnlineStatusListener onlineListener = new OnlineTTSModule.TTSOnlineStatusListener() {
        @Override
        public void onSuccess() {
            LogUtils.d(TAG, "onlineListener:::txz tts onSuccess::"+curClient);
            if (curClient != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            curClient.onPlayCompleted();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void onCancel() {
            LogUtils.d(TAG, "onlineListener:::onCancel: "+curClient);
            handler.removeCallbacks(speakRunnable);
            if (curClient != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            curClient.onPlayInterrupted();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void onError() {
            LogUtils.d(TAG, "onlineListener:::onError: "+getPackageName().equals(AudioFocusUtils.getInstance(TxzTtsService.this).getCurrentActiveAudioPkg()));
            if (!isPrepare) {
                handler.removeCallbacks(speakRunnable);
                TXZTTSInitManager.getInstance().speakOffline(textNow+" ",offlineListener);//加一段空格，防止播报离线音频时错误，陷入死循环
            }else  if(getPackageName().equals(AudioFocusUtils.getInstance(TxzTtsService.this).getCurrentActiveAudioPkg())){
                if(!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide())
                    Utils.exitVoiceAssistant();
                else
                    AudioFocusUtils.getInstance(TxzTtsService.this).releaseVoiceAudioFocus();
            }
        }

        @Override
        public void onStart() {
            LogUtils.d(TAG, "onlineListener:::txz tts onStart::"+curClient);
            if (curClient != null) {
                isPrepare = true;
                handler.removeCallbacks(speakRunnable);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            curClient.onPlayBegin();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void onDelay() {
            LogUtils.d(TAG, "onDelay: ");
            if (!isPrepare) {
                handler.removeCallbacks(speakRunnable);
                TXZTTSInitManager.getInstance().speakOffline(textNow,offlineListener);
            }
        }
    };


    //同步已经有录音的文件，如果有录音了就直接走离线播报
    private void initOfflineTts(){
        list = new HashSet<>();
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/iflytek/ica/tts/externalTTS/tts.txt");
            if(file.exists()){
            }else
                file = new File("/data/navi/0/iflytek/ica/tts/externalTTS/tts.txt");
            if (!file.exists()) {
                LogUtils.d(TAG, "initArray: 文件不存在");
                return;
            }
            InputStream is = new FileInputStream(file);
            BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bfReader.readLine()) != null) {
                if (line.trim().length() <= 0) {
                    continue;
                }
                //从txt文件中读取时，有时会在文件头加入一个"\uFEFF"，但是日志里无法显示。但是会影响匹配，故要去除
                list.add(line.replace("\uFEFF", ""));
            }
            LogUtils.d(TAG, "initArray: " + list.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initTxztts(){
        String urlSpeakTTS = SharedPreferencesUtils.getString(TxzTtsService.this,AppConstant.KEY_URL_SPEAKTTS,"");
        String uslGetToken = SharedPreferencesUtils.getString(TxzTtsService.this,AppConstant.KEY_URL_GETTOKEN,"");

        File file = new File("/sdcard/iflytek/ica/tts/externalTTS/");
        if(file.exists()){
            Log.d(TAG, "run: "+file.getAbsolutePath());
            TXZTTSInitManager.getInstance().setDefaultFilePath("/sdcard/iflytek/ica/tts/externalTTS/");
        }else
            TXZTTSInitManager.getInstance().setDefaultFilePath("/data/navi/0/iflytek/ica/tts/externalTTS/");


        //初始化放置在系统中
        TXZTTSInitManager.getInstance().setResourceFilePath("/system/media/tts/externalTTS/");

        inited = TXZTTSInitManager.getInstance().init(TxzTtsService.this,urlSpeakTTS,uslGetToken,null,null);
        AppConfig.INSTANCE.ttsEngineInited = inited;

        if(inited)
            sendTtsInited();

        try {
            for (Map.Entry<Long, ITtsAgentListener> entry : clientMap.entrySet()) {

                ITtsAgentListener listener = entry.getValue();
                listener.onTtsInited(inited, 0);
                Log.d(TAG, "run() called:::"+entry.getKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "run() called::"+inited+":..."+Thread.currentThread().getName());
    }


}
