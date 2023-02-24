package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.ApaConstant;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IApaController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.SettingsUtil;

public class ApaController extends BaseController implements IApaController {

    private static final String TAG = "ApaController";
    private static ApaController mApaController;
    private Context mContext;
    private boolean isStop = true;
    //apa 是否需要语音播报 1.需要 0.不需要
    private static final String APA_ISNEED_PLAYTTS = "apa_isneed_playtts";
    private static final int APA_TTS_ON = 1;
    private static final int APA_TTS_OFF = 0;


    public static ApaController getInstance(Context c){
        if (mApaController == null) {
            mApaController = new ApaController(c);
        }
        return mApaController;
    }

    private ApaController(Context c){
        mContext = c;
    }

    public void startSpeakApa(){

        if(!isStop){
            Log.e(TAG, "startSpeakApa: "+isStop);
            return;
        }

        isStop = false;
        Log.d(TAG, "startSpeakApa: ");
        // 暂时屏蔽
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop){
                    String apaStatus =  Utils.getProperty("apa_voice_broadcast", "-1");
                    if("unknown".equals(apaStatus)||"-1".equals(apaStatus)){

                    }else{
                       handleApaChanged(apaStatus);
                       Utils.setProperty("apa_voice_broadcast", "-1");
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

    public void stopSpeakApa(){
        isStop = true;
        Log.d(TAG, "stopSpeakApa() called");
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

        int value = Settings.System.getInt(mContext.getContentResolver(),APA_ISNEED_PLAYTTS,APA_TTS_ON);
        if(value==APA_TTS_OFF){
            Log.e(TAG, "handleApaChanged: the apa do not tts value::"+value);
            return;
        }

        try {
            int apa = Integer.parseInt(apaStatus);
            String hexString = Integer.toHexString(apa);
            Log.d(TAG, "handleApaChanged() called with: hexString = [" + hexString + "]");
            switch (hexString){
                case ApaConstant.APA26:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC6CONDITION, mContext.getString(R.string.apa_c1), mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_active,R.string.object_apa_null,TtsConstant.PARKINGC6CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c1));
                    break;
                case ApaConstant.APA27:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC7CONDITION,mContext.getString(R.string.apa_c2),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_active,R.string.object_apa_null,TtsConstant.PARKINGC7CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c2));
                    break;
                case ApaConstant.APA3E:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC8CONDITION,mContext.getString(R.string.apa_c3),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_search,R.string.object_apa_null,TtsConstant.PARKINGC8CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c3));
                    break;
                case ApaConstant.APA01:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC9CONDITION,mContext.getString(R.string.apa_c4),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_search,R.string.object_apa_null,TtsConstant.PARKINGC9CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c4));
                    break;
                case ApaConstant.APA02:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC10CONDITION,mContext.getString(R.string.apa_c5),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_point,R.string.object_apa_null,TtsConstant.PARKINGC10CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c5));
                    break;
                case ApaConstant.APA48:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC11CONDITION,mContext.getString(R.string.apa_c6),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_point,R.string.object_apa_null,TtsConstant.PARKINGC11CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c6));
                    break;
                case ApaConstant.APA49:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC13CONDITION,mContext.getString(R.string.apa_c49),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_point,R.string.object_apa_null,TtsConstant.PARKINGC13CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c49));
                    break;
                case ApaConstant.APA03:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC14CONDITION,mContext.getString(R.string.apa_c7),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_mode,R.string.object_apa_null,TtsConstant.PARKINGC14CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c7));
                    break;
                case ApaConstant.APA04:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC15CONDITION,mContext.getString(R.string.apa_c8),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_notice,R.string.object_apa_null,TtsConstant.PARKINGC15CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c8));
                    break;
                case ApaConstant.APA05:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC16CONDITION,mContext.getString(R.string.apa_c9),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_notice,R.string.object_apa_null,TtsConstant.PARKINGC16CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c9));
                    break;
                case ApaConstant.APA06:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC17CONDITION,mContext.getString(R.string.apa_c10),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_notice,R.string.object_apa_null,TtsConstant.PARKINGC17CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c10));
                    break;
                case ApaConstant.APA07:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC18CONDITION,mContext.getString(R.string.apa_c11),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_notice,R.string.object_apa_null,TtsConstant.PARKINGC18CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c11));
                    break;
                case ApaConstant.APAA:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC20CONDITION,mContext.getString(R.string.apa_c12),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_interetupt,R.string.object_apa_null,TtsConstant.PARKINGC20CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c12));
                    break;
                case ApaConstant.APAB:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC21CONDITION,mContext.getString(R.string.apa_c34),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_interetupt,R.string.object_apa_null,TtsConstant.PARKINGC21CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c34));
                    break;
                case ApaConstant.APAC:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC22CONDITION,mContext.getString(R.string.apa_c33),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_interetupt,R.string.object_apa_null,TtsConstant.PARKINGC22CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c33));
                    break;
                case ApaConstant.APAD:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC23CONDITION,mContext.getString(R.string.apa_c31),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_interetupt,R.string.object_apa_null,TtsConstant.PARKINGC23CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c31));
                    break;
                case ApaConstant.APAE:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC24CONDITION,mContext.getString(R.string.apa_c32),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_interetupt,R.string.object_apa_null,TtsConstant.PARKINGC24CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c32));
                    break;
                case ApaConstant.APAF:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC25CONDITION,mContext.getString(R.string.apa_c36),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_interetupt,R.string.object_apa_null,TtsConstant.PARKINGC25CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c36));
                    break;
                case ApaConstant.APA10:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC26CONDITION,mContext.getString(R.string.apa_c13),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_recovery,R.string.object_apa_null,TtsConstant.PARKINGC26CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c13));
                    break;
                case ApaConstant.APA12:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC27CONDITION,mContext.getString(R.string.apa_c14),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_finish,R.string.object_apa_null,TtsConstant.PARKINGC27CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c14));
                    break;
                case ApaConstant.APA13:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC28CONDITION,mContext.getString(R.string.apa_c15),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out,R.string.object_apa_null,TtsConstant.PARKINGC28CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c15));
                    break;
                case ApaConstant.APA14:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC29CONDITION,mContext.getString(R.string.apa_c16),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out,R.string.object_apa_null,TtsConstant.PARKINGC29CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c16));
                    break;
                case ApaConstant.APA15:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC30CONDITION,mContext.getString(R.string.apa_c17),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_filed,R.string.object_apa_null,TtsConstant.PARKINGC30CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c17));
                    break;
                case ApaConstant.APA16:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC31CONDITION,mContext.getString(R.string.apa_c18),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_notice,R.string.object_apa_null,TtsConstant.PARKINGC31CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c18));
                    break;
                case ApaConstant.APA17:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC32CONDITION,mContext.getString(R.string.apa_c19),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_notice,R.string.object_apa_null,TtsConstant.PARKINGC32CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c19));
                    break;
                case ApaConstant.APA18:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC33CONDITION,mContext.getString(R.string.apa_c20),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_notice,R.string.object_apa_null,TtsConstant.PARKINGC33CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c20));
                    break;
                case ApaConstant.APA19:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC34CONDITION,mContext.getString(R.string.apa_c21),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_notice,R.string.object_apa_null,TtsConstant.PARKINGC34CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c21));
                    break;
                case ApaConstant.APA1c:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC37CONDITION,mContext.getString(R.string.apa_c22),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_auto,R.string.object_apa_null,TtsConstant.PARKINGC37CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c22));
                    break;
                case ApaConstant.APA1d:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC38CONDITION,mContext.getString(R.string.apa_c34),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_interrupt,R.string.object_apa_null,TtsConstant.PARKINGC38CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c34));
                    break;
                case ApaConstant.APA1e:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC39CONDITION,mContext.getString(R.string.apa_c33),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_interrupt,R.string.object_apa_null,TtsConstant.PARKINGC39CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c33));
                    break;
                case ApaConstant.APA1f:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC40CONDITION,mContext.getString(R.string.apa_c31),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_interrupt,R.string.object_apa_null,TtsConstant.PARKINGC40CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c31));
                    break;
                case ApaConstant.APA20:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC41CONDITION,mContext.getString(R.string.apa_c32),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_interrupt,R.string.object_apa_null,TtsConstant.PARKINGC41CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c32));
                    break;
                case ApaConstant.APA21:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC42CONDITION,mContext.getString(R.string.apa_c36),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_interrupt,R.string.object_apa_null,TtsConstant.PARKINGC42CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c36));
                    break;
                case ApaConstant.APA22:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC43CONDITION,mContext.getString(R.string.apa_c23),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_recovery,R.string.object_apa_null,TtsConstant.PARKINGC43CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c23));
                    break;
                case ApaConstant.APA25:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC44CONDITION,mContext.getString(R.string.apa_c24),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_out_finish,R.string.object_apa_null,TtsConstant.PARKINGC44CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c24));
                    break;
                case ApaConstant.APA34:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC45CONDITION,mContext.getString(R.string.apa_c25),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_quit,R.string.object_apa_null,TtsConstant.PARKINGC45CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c25));
                    break;
                case ApaConstant.APA35:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC46CONDITION,mContext.getString(R.string.apa_c26),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_quit,R.string.object_apa_null,TtsConstant.PARKINGC46CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c26));
                    break;
                case ApaConstant.APA36:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC47CONDITION,mContext.getString(R.string.apa_c27),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_quit,R.string.object_apa_null,TtsConstant.PARKINGC47CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c27));
                    break;
                case ApaConstant.APA37:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC48CONDITION,mContext.getString(R.string.apa_c28),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_quit,R.string.object_apa_null,TtsConstant.PARKINGC48CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c28));
                    break;
                case ApaConstant.APA39:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC52CONDITION,mContext.getString(R.string.apa_c39),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_quit,R.string.object_apa_null,TtsConstant.PARKINGC52CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c39));
                    break;
                case ApaConstant.APA3c:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC49CONDITION,mContext.getString(R.string.apa_c29),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_quit,R.string.object_apa_null,TtsConstant.PARKINGC49CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c29));
                    break;
                case ApaConstant.APA23:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC50CONDITION,mContext.getString(R.string.apa_c30),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_quit,R.string.object_apa_null,TtsConstant.PARKINGC50CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c30));
                    break;
                case ApaConstant.APA46:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC50_1CONDITION,mContext.getString(R.string.apa_c35),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_rpa_start,R.string.object_apa_null,TtsConstant.PARKINGC50_1CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c35));
                    break;
                case ApaConstant.APA4B:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC51CONDITION,mContext.getString(R.string.apa_c4b),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_rpa_start,R.string.object_apa_null,TtsConstant.PARKINGC51CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c4b));
                    break;
                case ApaConstant.APA4C:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC53CONDITION,mContext.getString(R.string.apa_c53),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_rpa_start,R.string.object_apa_null,TtsConstant.PARKINGC53CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c53));
                    break;
                case ApaConstant.APA4D:
                    Utils.getMessageWithTtsSpeakOnly(mContext, TtsConstant.PARKINGC54CONDITION,mContext.getString(R.string.apa_c54),mListener);
                    Utils.eventTrack(mContext,R.string.skill_apa,R.string.scene_apa_rpa_start,R.string.object_apa_null,TtsConstant.PARKINGC54CONDITION,R.string.condition_apa_null,mContext.getString(R.string.apa_c54));
                    break;
            }
        } catch (Exception e) {
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

    /**
     * 防止语音发呆
     */
    private TTSController.OnTtsStoppedListener mListener = new TTSController.OnTtsStoppedListener() {
        @Override
        public void onPlayStopped() {
            Utils.exitVoiceAssistant();
        }
    };
}
