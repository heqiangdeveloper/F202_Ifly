package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.ApaConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IApaController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;

public class AVMController extends BaseController implements IApaController {

    private static final String TAG = "AVMController";
    private static AVMController mAVMController;
    private Context mContext;
    private boolean isStop = true;

    public static AVMController getInstance(Context c){
        if (mAVMController == null) {
            mAVMController = new AVMController(c);
        }
        return mAVMController;
    }

    private AVMController(Context c){
        mContext = c;
    }

    public void startSpeakAVM(){

        if(!isStop){
            Log.e(TAG, "startSpeakApa: "+isStop);
            return;
        }

        isStop = false;
        Log.d(TAG, "startSpeakApa: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop){
                    //虚拟按键引导播报
                    String avmStatus =  Utils.getProperty("avm_voice_broadcast", "-1");
                    Log.d(TAG, "run() called:::"+avmStatus);
                    if("unknown".equals(avmStatus)||"-1".equals(avmStatus)){

                    }else{
                        handleApaChanged(avmStatus);
                        Utils.setProperty("avm_voice_broadcast", "-1");
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public void stopSpeakAVM(){
        isStop = true;
        Log.d(TAG, "stopSpeakAVM() called");
    }

    /**
     * 处理 Apa 属性值
     * @param apaStatus
     *
     * 请立即接管车辆，系统异常     没有本地音频
     * 请注意周围环境，选择泊出方向
     *
     */
    private void handleApaChanged(String apaStatus) {
        Log.d(TAG, "handleApaChanged() called with: apaStatus = [" + apaStatus + "]");
        // 主要逻辑增加虚拟按键引导
        try {
            int tts =  Integer.parseInt(apaStatus);
            String ttsString = Integer.toHexString(tts);
            switch (ttsString){
                case ApaConstant.APA101:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA102:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA103:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA104:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA105:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
              /*  case ApaConstant.APA106:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA107:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;*/
                case ApaConstant.APA108:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA109:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA10a:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA10b:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
              /*  case ApaConstant.APA10c:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA10d:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA10e:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA10f:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA110:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;*/
                case ApaConstant.APA111:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA112:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
              /*  case ApaConstant.APA113:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA114:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA115:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA116:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA117:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA118:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA119:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;
                case ApaConstant.APA11a:
                    VirtualControl.getInstance().handle360Tts(ttsString);
                    return;*/



            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void srAction(IntentEntity intentEntity) {

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {

    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

}
