package com.chinatsp.ifly.remote;

import android.car.hardware.CarSensorEvent;
import android.car.hardware.constant.VEHICLE;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.ISpeechControlService;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.entity.TspInfo;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.CarUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voiceadapter.Business;
import com.chinatsp.proxy.IVehicleNetworkCallback;
import com.chinatsp.proxy.IVehicleNetworkRequestCallback;
import com.chinatsp.proxy.VehicleNetworkManager;
import com.example.mxextend.IExtendApi;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.listener.IExtendCallback;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.speech.util.NetworkUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * 顺风耳远程控制技能
 */
public class RemoteManager {

    private static final String TAG = "RemoteManager";
    private static RemoteManager instance;
    private static final String TYPE_NAVI = "ReservedGetOnCar";
    private boolean mWelcomeSpeak = false;
    private boolean mNaviSpeak = false;
    private boolean mWeSendToCar = false;
    private int mGearStatus;
    private IExtendApi extendApi;
    private TspInfo.Content mContent;
    private Context mContext;
    private ISpeechControlService mSpeechControlService;
    private static final int MSG_GET_TOKEN = 1;
    private static final int DELAY_TOKEN_TIME = 1000*3;
    private int mGetCount =  -1;
    public static RemoteManager getInstance(Context c){
        if(instance==null){
            synchronized (RemoteManager.class){
                if(instance==null)
                    instance = new RemoteManager(c);
            }
        }
        return instance;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_GET_TOKEN:
                    updateToken();
                    break;
            }
        }
    };

    private RemoteManager(Context c){
        mContext = c;
        this.extendApi = TAExtendManager.getInstance().getApi();
    }

    public void setSpeechService(ISpeechControlService service){
        mSpeechControlService = service;
    }

    /**
     * 获取远程启动类型
     * @param c
     */
    public void getTspMsg(Context c) {
        Log.d(TAG, "getTspMsg() called with: c = [" + c + "]");
        if(AppConfig.INSTANCE.token==null){
            Log.e(TAG, "getTspMsg: AppConfig.INSTANCE.token is null");
            if(mHandler.hasMessages(MSG_GET_TOKEN))
                mHandler.removeMessages(MSG_GET_TOKEN);
            mHandler.sendEmptyMessageDelayed(MSG_GET_TOKEN,DELAY_TOKEN_TIME);
            return;
        }
        String current_ucs = getUCS(c);
        String url = String.format("https://%s/api/hu/2.0/getNotifyMsg", current_ucs);

        HashMap<String, String> params = new HashMap<>();
        String mill=  System.currentTimeMillis()+"";
        params.put("access_token",AppConfig.INSTANCE.token);
        params.put("timestamp",mill);
        VehicleNetworkManager.getInstance().requestNet(url, "POST", params, new IVehicleNetworkRequestCallback() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess() called with: s = [" + s + "]");
                analysisTspInfo(s);
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onError() called with: i = [" + i + "], s = [" + s + "]");
            }

            @Override
            public void onProgress(float v) {

            }
        });
    }

    //微信到车 激活 此功能 : OS 接收到 0x0就执行GPS部分逻辑 , 接收到 0x1启动顺风耳逻辑
    //语音预约 激活 此功能 :首先OS从后台接口获取到状态后通知tbox . 然后等待tbox响应 .tbox响应0 执行GPS部分逻辑 .
    // 接收到 0x1 启动顺风耳逻辑.
    //(注 : 顺风耳逻辑启动需顺风耳应用模块监控解锁/门开 才开启应用)
    public void sendToCar(int status){
        if(status!= VEHICLE.YES){
            Log.e(TAG, "sendToCar: status::"+status);
            return;
        }
    }

    private void getTspMsg(String token) {
        Log.d(TAG, "getTspMsg() called with: token = [" + token + "]");

        String current_ucs = getUCS(mContext);
        String url = String.format("https://%s/api/hu/2.0/getNotifyMsg", current_ucs);

        HashMap<String, String> params = new HashMap<>();
        String mill=  System.currentTimeMillis()+"";
        params.put("access_token",token);
        params.put("timestamp",mill);
        VehicleNetworkManager.getInstance().requestNet(url, "POST", params, new IVehicleNetworkRequestCallback() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess() called with: s = [" + s + "]");
                analysisTspInfo(s);
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onError() called with: i = [" + i + "], s = [" + s + "]");
            }

            @Override
            public void onProgress(float v) {

            }
        });
    }




    /**
     * 在检测到p--非 p档时，获取是否远程启动
     * 如果是，给音乐传递随机播放语义
     * @return
     */
    public boolean isNaviSpeak(){
        return mNaviSpeak;
    }

    /**
     * 在开始导航，播放音乐完成之后重置，防止播放两次
     */
    public void resetNaviSpeak(){
        beginNavi();
        mNaviSpeak =false;
    }

    /**
     * 通知 P 档发生变化
     * 同时判断是否需要启动导航，避免每次都启动导航
     */
    public void notifyGearStatus(int gear){
        Log.d(TAG, "notifyGearStatus() called with: gear = [" + gear + "]"+mNaviSpeak+"...mWeSendToCar:"+mWeSendToCar);
         if(gear!=CarSensorEvent.GEAR_PARK&&mNaviSpeak){
             mNaviSpeak = false;
             if(playMusic()){//当前音乐正在播放时，直接导航
                 Utils.getMessageWithTtsSpeak(mContext, TtsConstant.REMOTEVOICEC2, mContext.getString(R.string.remotevoiceC2), new TTSController.OnTtsStoppedListener() {
                     @Override
                     public void onPlayStopped() {
                         Utils.exitVoiceAssistant();
                         beginNavi();
                     }
                 });
             }
         }else if(gear!=CarSensorEvent.GEAR_PARK&&mWeSendToCar){
             mWeSendToCar  = false;
             if(playMusic()){//当前音乐正在播放时，直接导航
                 Utils.getMessageWithTtsSpeak(mContext, TtsConstant.REMOTEVOICEC2, mContext.getString(R.string.remotevoiceC2), new TTSController.OnTtsStoppedListener() {
                     @Override
                     public void onPlayStopped() {
                         Utils.exitVoiceAssistant();
                         beginNavi();
                     }
                 });
             }
         }
    }


    public void notifyDoorStatus(int door){
        Log.d(TAG, "notifyDoorStatus() called with: door = [" + door + "]"+"...mWeSendToCar:"+mWeSendToCar);
        if(mWelcomeSpeak){
            speakWelcomeTts();
//            startNavi(mContent);
        }else if(mWeSendToCar){
            speakWelcomeTts();
//            startNavi(mContent);
        }
        mWelcomeSpeak = false;  //清空，防止下次继续播放，保证只播报一次
    }


    public void weChatTocar(Double latitude,Double longitude,String address,String name){
        Log.d(TAG, "weChatTocar() called with: latitude = [" + latitude + "], longitude = [" + longitude + "], address = [" + address + "], mContent = [" + mContent + "]");
        if(mContent==null)
            getTspMsg(mContext);  //开机tbox发送广播时，如果有数据就不再获取数据
       /* mNaviSpeak = false;//微信位置倒车覆盖预约导航的 ，二选一
        mWeSendToCar = true;
        TspInfo.Param param= new TspInfo.Param();
        param.address = address;
        param.latitude = latitude+"";
        param.longitude = longitude+"";
        param.reserved = true;

        TspInfo.Content content = new TspInfo.Content();
        content.msg_param = param;

        mContent = content;
        startNavi(mContent);*/

    }



    private void analysisTspInfo(String s){
        TspInfo info = GsonUtil.stringToObject(s, TspInfo.class);
        List<TspInfo.Content> contents =  info.data;
        if(contents==null){
            Log.e(TAG, "analysisTspInfo: the contents is null");
            return;
        }
        Log.d(TAG, "analysisTspInfo: contents:"+contents.size());
        for (int i = 0; i <contents.size() ; i++) {
            if(TYPE_NAVI.equals(contents.get(i).msg_type)){
                mContent = contents.get(i);
//                startNavi(contents.get(i));//这里应该根据 p 档状态回调，临时测试
                if(mContent.msg_param.reserved){
                    uploadTspResult(true);
                    startNavi(mContent);
                    mWelcomeSpeak = true;
                    mNaviSpeak = true;
                    mWeSendToCar = false;  //预约导航的覆盖微信倒车的，二选一
                }else{
                    uploadTspResult(false);
                    mWelcomeSpeak = false;
                    mNaviSpeak = false;
                }
                return;
            }
        }
    }


    private void speakWelcomeTts(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_REMOTE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }


    //音乐电台没有播放，且有网络，则推荐歌曲
    private boolean playMusic(){
        if(SRAgent.mInRadioPlaying||SRAgent.mRadioPlaying||SRAgent.mMusicPlaying){
            Log.e(TAG, "playMusic: have video playing!!");
            return true;
        }

        if(!NetworkUtil.isNetworkAvailable(mContext)){
            Log.e(TAG, "playMusic: have no net!!!");
            return true;
        }
        try {
            if (mSpeechControlService != null) {
                mSpeechControlService.dispatchSRAction(Business.MUSIC, convert2NlpVoiceModel());
                return false;
            }else{
                Log.e(TAG, "playMusic: the mSpeechControlService::is null!!!!!!");
                return true;
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    private NlpVoiceModel convert2NlpVoiceModel() {
        NlpVoiceModel nlpVoiceModel = new NlpVoiceModel();
        nlpVoiceModel.service = "viewCmd";
        nlpVoiceModel.operation = "VIEWCMD";
        nlpVoiceModel.semantic = "{" + "\"slots\":{" + "\"viewCmd\":\"好的\"," + "\"modeValue\":\"预约导航\"" + "}}";
        Log.d(TAG, "convert2NlpVoiceModel: " + nlpVoiceModel.semantic);
        return nlpVoiceModel;
    }

    //发起导航
    private void beginNavi(){
        Log.d(TAG, "beginNavi() called");
        MXSdkManager.getInstance(mContext).startNavi();
    }

    //导航规划
    private void startNavi(TspInfo.Content content){

        Log.d(TAG, "startNavi() called with: content = [" + content + "]");

        if(!content.msg_param.reserved){
            Log.e(TAG, "startNavi: reserved---->"+content.msg_param.reserved);
            return;
        }

        LocationInfo navinLocInfo=new LocationInfo();
        navinLocInfo.setAddress(content.msg_param.reservedAddress);
//        navinLocInfo.setName(;
        navinLocInfo.setLatitude(Double.valueOf( content.msg_param.latitude));
        navinLocInfo.setLongitude(Double.valueOf( content.msg_param.longitude));

        LogUtils.d(TAG, "locationInfo:"+navinLocInfo.toString());
        extendApi.navigation(navinLocInfo, false,new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "naviToPoi：success");

            }

            @Override
            public void onFail(ExtendErrorModel errorModel) {
                Log.d(TAG, "onFail() called with: errorModel = [" + errorModel + "]");

            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
                Log.d(TAG, "onJSONResult() called with: jsonObject = [" + jsonObject + "]");
            }
        });

    }


    private   String getUCS(Context context) {
        String temp = null;
        try {
            temp = Settings.System.getString(context.getContentResolver(), "Host");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(temp)){
            temp = "incall.changan.com.cn";
        }
        return temp;
    }


    private void uploadTspResult(boolean result){

        CarUtils.getInstance(mContext).setRemoteStatus(result);

        if(AppConfig.INSTANCE.token==null){
            Log.e(TAG, "getTspMsg: AppConfig.INSTANCE.token is null");
            return;
        }
        String code = "0";
        if(result)
            code = "1";
        String current_ucs = getUCS(mContext);
        String url = String.format("https://%s/api/hu/2.0/consumeNotifyMsg", current_ucs);
        HashMap<String, String> params = new HashMap<>();
        String mill=  System.currentTimeMillis()+"";
        params.put("access_token",AppConfig.INSTANCE.token);
        params.put("timestamp",mill);
        params.put("msg_id",mContent.msg_id);
//        params.put("cmd_result_code",code);
        VehicleNetworkManager.getInstance().requestNet(url, "POST", params, new IVehicleNetworkRequestCallback() {
            @Override
            public void onSuccess(String s) {
                Log.d(TAG, "onSuccess() called with: s = [" + s + "]");
            }

            @Override
            public void onError(int i, String s) {
                Log.d(TAG, "onError() called with: i = [" + i + "], s = [" + s + "]");
            }

            @Override
            public void onProgress(float v) {

            }
        });

    }

    private void updateToken() {
        VehicleNetworkManager.getInstance().getToken(new IVehicleNetworkCallback() {
            @Override
            public void onCompleted(String s) {
                LogUtils.d("AppConfig", "getToken:" + s);
                getTspMsg(s);
                mHandler.removeCallbacksAndMessages(null);
                mGetCount = -1;
            }

            @Override
            public void onException(int i, String s) {
                LogUtils.e("AppConfig", "getToken onException:" + i+"...mGetCount::"+mGetCount);
                mGetCount++;
                if(mGetCount<5){
                    mHandler.sendEmptyMessageDelayed(MSG_GET_TOKEN,DELAY_TOKEN_TIME);
                }
            }
        });
    }

}
