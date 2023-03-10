package com.iflytek.adapter.sr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.iflytek.adapter.PlatformClientListener;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.Incs;
import com.iflytek.adapter.common.PcmRecorder;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.seopt.SeoptManager;
import com.iflytek.speech.ISSErrors;
import com.iflytek.sr.IIsrListener;
import com.iflytek.sr.SrSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class SRAgent {
    private static final String TAG = "xyj_SRAgent";

    private static  String resPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/iflytek/ica/res/SRRes/";
    private static  String resNaviPath = "/data/navi/0/iflytek/ica/res/SRRes";
    private static final String machinePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/iflytek/ica/res/Active/machinecode/";

    private static final int INIT_SUC = 1;
    private static final int INIT_FAIL = 2;
    private static SRAgent instance = null;
    private Context context = null;
    private boolean inited = false;
    public boolean init_state = false;
    public int init_error_msg = 0;
    private PcmRecorder pcmRecorder;

    public SrSessionArgu mSrArgu_Old = null;
    public SrSessionArgu mSrArgu_New = null;
    public Handler mHandler = null;
    public SrSession SrInstance = null;
    private int srTimeCount = 0;
    private boolean isStart;
    public TextView textView;

    public static String AppStatus = null;
    public static boolean mMusicPlaying = false;
    public static boolean mInRadioPlaying = false;
    public static boolean mRadioPlaying = false;
    public static boolean mLocalvideoPlaying = false;
    public static boolean mVideoPlaying = false;
    private int restartCount = 0;

    public static boolean mHICARPlaying = false;

    private SRAgent() {}

    public void createSr(int lang) {
        if (SrInstance != null) {
            SrInstance.release();
            SrInstance = null;
        }

        File file = new File(resPath);
        if(!file.exists()){
            Log.d(TAG, "createSr() called with: lang = [" + file.getAbsolutePath() + "]");
            resPath = resNaviPath;
        }
        Log.d(TAG, "createSr() called with: resPath = [" + resPath + "]");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String Imei = tm.getDeviceId();
        String tuid = Settings.System.getString(context.getContentResolver(), "TUID");
        Log.d("heqq","tuid = " + tuid);
        if (tuid==null||TextUtils.isEmpty(tuid)) {
            tuid = "ifly" + Math.random() + Math.random() + Math.random()
                    + Math.random() + Math.random() + Math.random()
                    + Math.random() + Math.random() + Math.random()
                    + Math.random() + "tek";
        } else
            tuid = "ifly" + tuid+ "tek";

        String cpuinfo = getCPUSerial();
        Log.d(TAG, "createSr() called with: tuid = [" + tuid + "]");
        Log.d(TAG, "createSr: cpuinfo::"+cpuinfo);

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("bt_info","");
        map.put("cpu_info",cpuinfo);
        map.put("wlan_info","");
        map.put("sn_info","");
        map.put("vin_info",tuid);
        map.put("gps_info","");
        map.put("key",new String[]{"vin_info"});
        map.put("path","");
        JSONObject object = new JSONObject(map);
        Log.d(TAG, "createSr() called with: tuid = [" + object.toString() + "]");
        SrInstance = SrSession.getInstance(context, isrListener, lang,
                resPath, object.toString(), "1XUBAO-HT44VV-KHEA6Y");

    }

    public void setPcmRecorder(PcmRecorder pcmRecorder) {
        this.pcmRecorder = pcmRecorder;
    }

    @SuppressLint("HandlerLeak")
    public SRAgent init(final Context context) {
        Log.d(TAG, "init: inited=" + inited);
        this.context = context.getApplicationContext();

        if (inited) {
            return instance;
        }

        createSr(SrSession.ISS_SR_ACOUS_LANG_VALUE_MANDARIN);

        mSrArgu_New = new SrSessionArgu(SrSession.ISS_SR_SCENE_ALL, SrSession.ISS_SR_MODE_MIX_REC, 0);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Incs.MSG_TYPE_SR: {
                        // ?????????????????????????????????
                        Bundle b = msg.getData();
                        long uMsg = b.getLong("uMsg");
                        long wParam = b.getLong("wParam");
                        String lParam = b.getString("lParam");
                        if (uMsg != SrSession.ISS_SR_MSG_VolumeLevel) {
                            Log.d(TAG, "?????????????????????????????????: uMsg:" + uMsg + ", wParam:" + wParam + ", lParam: " + lParam);
                            //???????????? ????????????
                            SharedPreferences preferences = context.getSharedPreferences("com.chinatsp.ifly_preferences", Context.MODE_PRIVATE);
                            boolean isFirstUse = preferences.getBoolean("isFirstUse", false);
                            if(isFirstUse){
                                context.sendBroadcast(new Intent("com.chinatsp.ifly.mx.maketeam"));
                            }
                        }
                        //if (uMsg == 20055) {
                          //  //????????????
                          //  tts(lParam);
                        //} else 
						if (uMsg == ISSErrors.ISS_ERROR_ALREADY_EXIST) {
                            Log.d(TAG, "???????????????????????????.\n");
                        } else if (uMsg == SrSession.ISS_SR_MSG_VolumeLevel) {
//                            Log.d(TAG, "??????????????????." + wParam);
                        } else if (uMsg == SrSession.ISS_SR_MSG_InitStatus) {
                            if (wParam == ISSErrors.ISS_SUCCESS) {
                                Log.d(TAG, "????????????????????????.\n");
                                init_state = true;
                                init_error_msg = 0;
                                //??????????????????
                                SrInstance.setParam(SrSession.ISS_SR_PARAM_TRACE_LEVEL, SrSession.ISS_SR_PARAM_TRACE_LEVEL_VALUE_DEBUG);
                                //??????????????????????????????????????????????????????
                                SrInstance.setParam(SrSession.ISS_SR_PARAM_RESPONSE_TIMEOUT, "5000");
                                SrInstance.setParam(SrSession.ISS_SR_PARAM_PGS, SrSession.ISS_SR_PARAM_VALUE_ON);
                                //???????????????
                                if(SeoptConstant.USE_SEOPT) {
                                    SrInstance.setParam(SrSession.ISS_SR_PARAM_SEOPT_MODE, SrSession.ISS_SR_PARAM_VALUE_ON);
                                } else {
                                    SrInstance.setParam(SrSession.ISS_SR_PARAM_SEOPT_MODE, SrSession.ISS_SR_PARAM_VALUE_OFF);
                                }
                                Settings.System.putInt(context.getContentResolver(),"ifly_init",INIT_SUC);
                            } else {
                                Log.d(TAG, "ISS_SR_MSG_InitStatus ISS_ERROR_FAIL:????????????????????????.\n");
                                if (wParam == ISSErrors.ISS_ERROR_OUT_OF_MEMORY)
                                    Log.d(TAG, "ISS_SR_MSG_InitStatus ISS_ERROR_OUT_OF_MEMORY:????????????????????????,???????????????\n");
                            }
                            stopSRRecord();
                        } else if (uMsg == SrSession.ISS_SR_MSG_UpLoadDictToLocalStatus) {
                            if (PlatformHelp.getInstance().getPlatformClient() != null) {
                                PlatformHelp.getInstance().getPlatformClient().upLoadDictToLocalStatus(wParam,lParam);
                            }
                            if (wParam == ISSErrors.ISS_SUCCESS) {
                                Log.d(TAG, "?????????????????????????????????\n");
                                Log.d(TAG, lParam + "\n");
                            } else if (wParam == ISSErrors.ISS_ERROR_INVALID_JSON_FMT) {
                                Log.d(TAG, "?????????Json???????????????\n");
                            } else if (wParam == ISSErrors.ISS_ERROR_INVALID_JSON_INFO) {
                                Log.d(TAG, "?????????Json??????????????????????????????????????????\n");
                            } else {
                                Log.d(TAG, lParam + "\n");
                            }
                        } else if (uMsg == SrSession.ISS_SR_MSG_UpLoadDictToCloudStatus) {
                            if (PlatformHelp.getInstance().getPlatformClient() != null) {
                                PlatformHelp.getInstance().getPlatformClient().upLoadDictToCloudStatus(wParam,lParam);
                            }
                            if (wParam == ISSErrors.ISS_SUCCESS) {
                                Log.d(TAG, "?????????????????????????????????\n");
                                Log.d(TAG, lParam + "\n");
                            }
                            if (wParam == ISSErrors.ISS_ERROR_INVALID_JSON_FMT)
                                Log.d(TAG, "?????????Json???????????????\n");
                            if (wParam == ISSErrors.ISS_ERROR_INVALID_JSON_INFO)
                                Log.d(TAG, "?????????Json??????????????????????????????????????????\n");
                            if (wParam != ISSErrors.ISS_SUCCESS)
                                Log.d(TAG, "?????????????????????????????????, error=" + wParam + "\n");
                        } else if (uMsg == SrSession.ISS_SR_MSG_ResponseTimeout) {
                            Log.d(TAG, "????????????,??????????????????????????????????????????\n");
                            stopSRRecord();
                            stopSRSession();
                            srTimeout();

                        } else if (uMsg == SrSession.ISS_SR_MSG_SpeechStart) {
                            Log.d(TAG, "?????????????????????\n");
                            processSpeechStart();
                        } else if (uMsg == SrSession.ISS_SR_MSG_SpeechTimeOut) {
                            Log.d(TAG, "???????????????????????????????????????,????????????????????????\n");
                            stopSRRecord();
                            stopSRSession();
//                            srTimeout();

                        } else if (uMsg == SrSession.ISS_SR_MSG_SpeechEnd) {
                            Log.d(TAG, "???????????????????????????????????????????????????,????????????????????????\n");
                            long delayTimeMs = Math.abs(System.currentTimeMillis() - MVWAgent.getInstance().iMvwTrigTime);
                            if(delayTimeMs < 2000&&!MVWAgent.getInstance().mGlobeMwv) {
                                Log.e(TAG, "?????????????????????????????????\n");
                                if(!PcmRecorder.getInstnace().isSrStarted()){//
                                    stopSRRecord();  //????????????
                                    stopSRSession();//??????????????????
                                }
                                return;
                            }
                            stopSRRecord();
                            stopSRSession();//??????????????????
                            processRecogStart();
//                            stopSRRecord1();
//                            stopSRSession1();
//                            startSRSession1();
                        } else if (uMsg == SrSession.ISS_SR_MSG_Error) {
                            Log.d(TAG, "ISS_SR_MSG_Error:???????????????????????????????????????.\n");
                            long delayTimeMs = Math.abs(System.currentTimeMillis() - MVWAgent.getInstance().iMvwTrigTime);
                            if(delayTimeMs < 2000) {
                                Log.e(TAG, "?????????????????????????????????\n");
                                if(!PcmRecorder.getInstnace().isSrStarted()){//
                                    stopSRSession();
                                    startSRSession();
                                }
                                return;
                            }
                            stopSRRecord();
                            stopSRSession();
                            processSpeechEnd();
                            if (PlatformHelp.getInstance().getPlatformClient() != null) {
                                PlatformHelp.getInstance().getPlatformClient().onEngineException(wParam);
                            }
                        } else if (uMsg == SrSession.ISS_SR_MSG_Result) {
                            Log.d(TAG, "??????????????????????????????????????????????????????\n");
                            long delayTimeMs = Math.abs(System.currentTimeMillis() - MVWAgent.getInstance().iMvwTrigTime);
                            if(delayTimeMs < 2000&&!MVWAgent.getInstance().mGlobeMwv) {
                                Log.e(TAG, "?????????????????????????????????\n");
                                if(!PcmRecorder.getInstnace().isSrStarted()){  //????????????????????????????????????
                                    if(!PlatformHelp.getInstance().getPlatformClient().getTtsState()){   //???????????????????????????????????????????????????
                                        stopSRSession();
//                                        stopSrOnly(); ???????????????stop
                                        startSRSession();
                                    }
                                }
                                return;
                            }
                            stopSRRecord();
                            stopSRSession();
//                            stopSrOnly();  ???????????????stop
                            processRecogEnd();
//                            startSRSession1();

                            lParam = lParam.replaceAll("\n[ \r\t]*\n", "\n");
                            Log.d(TAG, "---????????????------\n" + lParam
                                    + "\n---????????????------\n");

                            boolean hasSemantic = hasSemantic(lParam);
                            if (PlatformHelp.getInstance().getPlatformClient() != null) {
                                if (hasSemantic) {
                                    PlatformHelp.getInstance().getPlatformClient().onNLPResult(lParam);
                                } else {
                                    PlatformHelp.getInstance().getPlatformClient().onDoAction(lParam);
                                }
                            }
                        } else if (uMsg == SrSession.ISS_SR_MSG_PreResult) {
                            Log.d(TAG, "---????????????------\n" + lParam + "\n");
                        } else if(uMsg == SrSession.ISS_SR_MSG_STKS_Result) {
                            Log.d(TAG, "??????ISS_SR_MSG_STKS_Result: uMsg");
                            if (PlatformHelp.getInstance().getPlatformClient() != null) {
                                PlatformHelp.getInstance().getPlatformClient().onStkAction(lParam);
                            }
                        }else if(uMsg == SrSession.ISS_SR_ERROR_FILE_NOT_FOUND){
                            Log.d(TAG, "handleMessage: ???????????????");
                            sendEmptyMessageDelayed(Incs.SR_MSG_RECREATE_SR,3000);
                            init_error_msg = SrSession.ISS_SR_ERROR_FILE_NOT_FOUND;
                        }else if(uMsg == SrSession.SR_MSG_SRResult){
						    tts(lParam);
                        }
                        break;
                    }
                    case Incs.SR_MSG_SETPARAM: {
                        // ??????setPatam??????????????????????????????
                        Bundle b = msg.getData();
                        if (!b.getBoolean("state")) {
                            Log.d(TAG, "GPS??????????????????????????????\n");
                            break;
                        }
                        double longitude = b.getDouble("longitude");
                        double latitude = b.getDouble("latitude");
                        Log.d(TAG, "??????????????????:longitude="
                                + String.valueOf(longitude) + ", latitude="
                                + String.valueOf(latitude) + "\n");
                        break;
                    }
                    case Incs.SR_MSG_ENDAUDIODATA_RETURN: {
                        // ????????????endAudioData???????????????
                        Bundle b = msg.getData();
                        int errid = b.getInt("errid");
                        if (errid == ISSErrors.ISS_ERROR_NO_SPEECH) {
                            Log.d(TAG, "??????????????????????????????\n");
                        }
                        stopSRRecord();
                        break;
                    }
                    case Incs.SR_DISPLAY_STRING: {
                        Bundle b = msg.getData();
                        Log.d(TAG, b.getString("str") + "\n");
                        break;
                    }
                    case Incs.SR_SETPARAMS_RESULT: {
                        // ??????GPS???????????????????????????????????????????????????????????????
                        Bundle bundle = msg.getData();
                        double longitude = bundle.getDouble("longitude");
                        double latitude = bundle.getDouble("latitude");
                        int lonId = bundle.getInt("lonId");
                        int laId = bundle.getInt("laId");
                        Log.d(TAG, "??????????????????:longitude=" + longitude
                                + ", latitude=" + latitude + "\n");
                        if (lonId == ISSErrors.ISS_SUCCESS) {
                            Log.d(TAG, "??????????????????\n");
                        } else {
                            Log.d(TAG, "??????????????????\n");
                        }
                        if (laId == ISSErrors.ISS_SUCCESS) {
                            Log.d(TAG, "??????????????????\n");
                        } else {
                            Log.d(TAG, "??????????????????\n");
                        }
                        break;
                    }
                    case Incs.SR_MSG_RECREATE_SR:
                        restartCount ++;
                        Log.d(TAG, "handleMessage() called with: SR_MSG_RECREATE_SR restartCount= [" + restartCount +"]");
                        if (SrInstance != null&&restartCount<=1) {
                            SrInstance.restartCreate(SrSession.ISS_SR_ACOUS_LANG_VALUE_MANDARIN,resPath);
                        }else {
                            Log.e(TAG, "handleMessage: -------------init 3 still error---------------------");
                            Settings.System.putInt(context.getContentResolver(),"ifly_init",INIT_FAIL);
                        }

                        break;
                    default: {
                        Log.d(TAG, "WARNING:: ??????????????????????????????\n");
                    }

                }

            }
        };

        inited = true;
        Log.d(TAG, "init end: inited=" + inited);

        return instance;
    }

    public void srTimeout() {
        srTimeCount++;
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            PlatformHelp.getInstance().getPlatformClient().onSrTimeOut(srTimeCount);
        }
    }

    private void processSpeechStart() {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            PlatformHelp.getInstance().getPlatformClient().onSpeechStart();
        }
    }

    private void processSpeechEnd() {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            PlatformHelp.getInstance().getPlatformClient().onSpeechEnd();
        }
    }

    private void processRecogStart() {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            PlatformHelp.getInstance().getPlatformClient().onRecognizeStart();
        }
    }

    public void processRecogEnd() {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            PlatformHelp.getInstance().getPlatformClient().onRecognizeEnd();
        }
    }

    private boolean hasSemantic(String lParam) {
        JSONObject jsonobj = null;
        try {
            jsonobj = new JSONObject(lParam);
            JSONObject semantic = jsonobj.optJSONObject("intent").optJSONObject("semantic");
            Log.d(TAG, "semantic:" + semantic);
            if (semantic != null) {
                JSONObject slots = semantic.getJSONObject("slots");
                if (slots != null) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "JSONException :" + e.getMessage());
        }

        return false;
    }

    public void release() {
        Log.d(TAG, "release");
        stopSRSession();
        SrInstance.release();

        mSrArgu_New = null;
        inited = false;
        init_state = false;
        init_error_msg = 0;
    }

    public void resetSession() {
        Log.d(TAG, "resetSession");
        if(SrInstance!=null)
            SrInstance.resetSession();
    }


    public static SRAgent getInstance() {
        if (instance == null)
            instance = new SRAgent();
        return instance;
    }

    public void setSrArgu_New(String srScene, String szCmd) {
        Log.d(TAG, "setSrArgu_New() called with: srScene = [" + srScene + "], szCmd = [" + szCmd + "]");
        this.mSrArgu_New.scene = srScene;
        this.mSrArgu_New.szCmd = szCmd;
        this.mSrArgu_New.onlyUploadToCloud = 0;
    }

    public void startSRSession1() {
//        if (!init_state) {
//            Log.d(TAG, "ERROR:?????????????????????????????????\n");
//            return;
//        }
//        if (pcmRecorder == null) {
//            return;
//        }
//
//        if (SeoptConstant.USE_SEOPT) {
//            SeoptManager.getInstance().setSrSession(SrInstance);
//            SeoptManager.getInstance().startSrRecord();
//        }
//        Log.e(TAG, "startSRSession");
//        pcmRecorder.reSetSrTime();
//        pcmRecorder.setSRParams(SrInstance, mHandler, mSrArgu_New);
//        pcmRecorder.startSRRecord();
//
//        if (SrSession.ISS_SR_SCENE_ALL.equals(mSrArgu_New.scene)) {
//            isSrSession = true;
//        }
//
//        //????????????????????????
//        if (mCarAudioManager != null) {
//            try {
//                mCarAudioManager.setMicMode(MIC_MODE_DENOISE);
//            } catch (CarNotConnectedException e) {
//                e.printStackTrace();
//            }
//        }
//        isStart = true;
    }

    public int startSRSession() {
        int errorID = -1;
        if (!init_state) {
            Log.d(TAG, "ERROR:?????????????????????????????????\n");
            return -errorID;
        }
        if (pcmRecorder == null) {
            return -errorID;
        }

        if (SeoptConstant.USE_SEOPT) {
            SeoptManager.getInstance().setSrSession(SrInstance);
            SeoptManager.getInstance().startSrRecord();
        }
        Log.e(TAG, "*******????????????  startSRSession");
        pcmRecorder.reSetSrTime();
        pcmRecorder.setSRParams(SrInstance, mHandler, mSrArgu_New);
        errorID = pcmRecorder.startSRRecord();

        SrInstance.isOneshot = false;

        return errorID;
    }


    public void stopSRSession1() {
//        if (!init_state) {
//            Log.d(TAG, "ERROR:?????????????????????????????????\n");
//            return;
//        }
//        if (pcmRecorder == null) {
//            return;
//        }
//        Log.d(TAG, "stopSRSession");
//        pcmRecorder.endSRAudioData();
//
//        isSrSession = false;
//        //????????????????????????
//        if (mCarAudioManager != null) {
//            try {
//                mCarAudioManager.setMicMode(MIC_MODE_ORIGINAL_RECORDING);
//            } catch (CarNotConnectedException e) {
//                e.printStackTrace();
//            }
//        }
//        //????????????????????????
//        if (PlatformHelp.getInstance().getPlatformClient() != null) {
//            PlatformHelp.getInstance().getPlatformClient().onRestoreMultiSemantic();
//        }
//        isStart = false;
    }

    public int stopSRSession() {
        if (!init_state) {
            Log.d(TAG, "ERROR:?????????????????????????????????\n");
            return 10000;
        }
        if (pcmRecorder == null) {
            return 10000;
        }
        Log.d(TAG, "**********???????????????stopSRSession");
        int errid = pcmRecorder.endSRAudioData();

        if (SeoptConstant.USE_SEOPT) {
            SeoptManager.getInstance().stopSrRecord();
        }
        return errid;
    }

    public void stopSrOnly(){
        Log.d(TAG, "stopSrOnly() called");
        if (pcmRecorder == null) {
            return;
        }
        Log.d(TAG, "**********???????????? stopSrOnly");
        pcmRecorder.stopSrSession();

    }


    public void stopSRRecord1() {
//        if (!init_state) {
//            Log.d(TAG, "ERROR:?????????????????????????????????\n");
//            return;
//        }
//        if (pcmRecorder == null) {
//            return;
//        }
//        pcmRecorder.stopSRRecord();
//
//        if (SeoptConstant.USE_SEOPT) {
//            SeoptManager.getInstance().stopSrRecord();
//        }
    }

    public void stopSRRecord() {
   //     if (isStart) {
     //       startSRSession1();
       // }
        if (!init_state) {
            Log.d(TAG, "ERROR:?????????????????????????????????\n");
            return;
        }
        if (pcmRecorder == null) {
            return;
        }
        pcmRecorder.stopSRRecord();

        if (SeoptConstant.USE_SEOPT){
            SeoptManager.getInstance().stopSrRecord();
        }
    }

    /**
     * ????????????
     *
     * @param text
     */
    public int uploadData(String text) {
        if (!init_state) {
            Log.d(TAG, "ERROR:?????????????????????????????????\n");
            return -1;
        }
        Log.d(TAG, text);
        UploadAppStatus(text);
        int errID = SrInstance.uploadData(text, mSrArgu_New.onlyUploadToCloud);
        Log.d(TAG, "uploadData id = " + errID);
        return errID;
    }

    /**
     * ????????????
     *
     * @param text
     */
    public int uploadDict(String text) {
        if (!init_state) {
            Log.d(TAG, "ERROR:?????????????????????????????????\n");
            return -1;
        }
//        Log.d(TAG, text);
        int errID = SrInstance.uploadDict(text, mSrArgu_New.onlyUploadToCloud);
        Log.d(TAG, "uploadDict id = " + errID);
        return errID;
    }

    IIsrListener isrListener = new IIsrListener() {
        @Override
        public void onSrMsgProc(long uMsg, long wParam, String lParam) {
            if(mHandler == null) {
                //?????????????????????
                return;
            }
            if (uMsg != SrSession.ISS_SR_MSG_VolumeLevel) {
                Log.d(TAG, "onSrMsgProc: uMsg:" + uMsg + ", wParam:" + wParam + ", lParam: " + lParam);
            }
            Message msg = new Message();
            msg.what = Incs.MSG_TYPE_SR;
            Bundle b = new Bundle();
            b.putLong("uMsg", uMsg);
            b.putLong("wParam", wParam);
            b.putString("lParam", lParam);
            msg.setData(b);
            msg.setTarget(mHandler);
            msg.sendToTarget();
        }

        @Override
        public void onSrInited(boolean state, int errId) {
            if (state) {
                Log.d(TAG, "??????????????????...");

                @SuppressLint("StaticFieldLeak")
                AsyncTask<String, String, String> asyncTask = new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        int activeKeyId = SrInstance.getActiveKey(resPath);
                        if (activeKeyId != ISSErrors.ISS_SUCCESS) {
                            Log.d(TAG, "??????????????????????????????????????????" + activeKeyId);
                        }
                        return null;
                    }
                };
                asyncTask.execute(null, null, null);
            } else {
                if (errId == ISSErrors.REMOTE_EXCEPTION) {
                    Log.d(TAG, "?????????????????????????????????????????????\n");
                    SrInstance.initService();
                }
            }
        }
    };

    public boolean isInited(){
        if(!inited)
            Log.e(TAG, "isInited: sragent not init");
        return inited;
    }

    public void setTimeoutTime(int timeoutTime) {
        if (!init_state) {
            Log.d(TAG, "ERROR:?????????????????????????????????\n");
            return;
        }
        Log.d(TAG, "setTimeoutTime:" + timeoutTime);
        SrInstance.setParam(SrSession.ISS_SR_PARAM_RESPONSE_TIMEOUT, String.valueOf(timeoutTime));
    }

    public void resetSrTimeCount() {
        srTimeCount = 0;
    }

    /**
     * ????????????
     */
    String oldStr = "";

    private void tts(String lParam) {
        try {
            Log.i("1234567890", "lParam = " + lParam);
            JSONObject root = new JSONObject(lParam);
            JSONObject rootObject = root.optJSONObject("text");
            if (null == rootObject) {
                return;
            }
            String pgs = rootObject.optString("pgs");
            JSONArray ws = rootObject.optJSONArray("ws");
            if (null != ws) {
                StringBuilder textBuilder = new StringBuilder();
                for (int i = 0; i < ws.length(); ++i) {
                    JSONObject jsonObject = ws.getJSONObject(i);
                    if (null != jsonObject) {
                        JSONArray cw = jsonObject.optJSONArray("cw");
                        if (null != cw) {
                            for (int j = 0; j < cw.length(); ++j) {
                                JSONObject jo = cw.getJSONObject(j);
                                if (null != jo) {
                                    String w = jo.optString("w");
                                    if (!TextUtils.isEmpty(w)) {
                                        textBuilder.append(w);
                                    }
                                }
                            }
                        }
                    }
                }
                if(PlatformHelp.getInstance().getPlatformClient()!=null)
                    PlatformHelp.getInstance().getPlatformClient().onPgsAction(textBuilder.toString());
                Log.i("1234567890", "textBuilder = " + textBuilder.toString());
              /*  boolean isApd = false;
                if (textBuilder.length() <= 0) {
                    return;
                }
                if ("apd".equals(pgs)) {
                    isApd = true;
//                    PGS_WINDOW.clearText();//????????????????????????????????????
//                    PGS_WINDOW.updateText(texts);//????????????????????????????????????
                } else if ("rpl".equals(pgs)) {
//                    PGS_WINDOW.appendText(texts);//?????????????????????????????????
                }
                if (isApd) {
                    oldStr = textBuilder.toString();
                    String textViewStr = textView.getText().toString();
                    textView.setText(textViewStr + textBuilder.toString());
                } else {
                    String textViewStr = textView.getText().toString();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(textViewStr);
                    stringBuilder.replace(stringBuilder.length() - oldStr.length(),
                            stringBuilder.length(), "");
                    oldStr = textBuilder.toString();
                    stringBuilder.append(oldStr);
                    textView.setText(stringBuilder.toString());
                }
                Log.i("1234567890" , textView.getText().toString()) ;*/
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????app????????????
     *
     * @param string
     */
    public void UploadAppStatus(String string){
        Log.d(TAG,":: default"+string);
        if (string.contains("musicX::default")){
            AppStatus = "musicX";
            if(string.contains("playing"))
                mMusicPlaying = true;
            else
                mMusicPlaying = false;
            mInRadioPlaying = false;
            mRadioPlaying = false;
            mLocalvideoPlaying = false;
            mVideoPlaying = false;
        }else if (string.contains("internetRadio::default")){
            AppStatus = "internetRadio";
            if(string.contains("playing"))
                mInRadioPlaying = true;
            else
                mInRadioPlaying = false;
            mMusicPlaying = false;
            mRadioPlaying = false;
            mLocalvideoPlaying = false;
            mVideoPlaying = false;
        }else if (string.contains("radio::default")){
            AppStatus = "radio";
            if(string.contains("playing"))
                mRadioPlaying = true;
            else
                mRadioPlaying = false;
            mMusicPlaying = false;
            mInRadioPlaying = false;
            mLocalvideoPlaying = false;
            mVideoPlaying = false;
        }else if (string.contains("localvideo::default")){
            //????????? mLocalvideoPlaying ????????????????????????????????? AppStatus ?????????????????????????????????
            //???????????????????????????????????????????????????????????????????????????
           /* AppStatus = "localvideo";
            if(string.contains("playing"))
                mLocalvideoPlaying = true;
            else
                mLocalvideoPlaying = false;*/
            mMusicPlaying = false;
            mInRadioPlaying = false;
            mRadioPlaying = false;
            mVideoPlaying = false;
        } else if (string.contains("video::default")){
            //????????? mVideoPlaying ????????????????????????????????? AppStatus ?????????????????????????????????
            /*AppStatus = "video";
            if(string.contains("playing"))
                mVideoPlaying = true;
            else
                mVideoPlaying = false;*/
            mMusicPlaying = false;
            mInRadioPlaying = false;
            mRadioPlaying = false;
            mLocalvideoPlaying = false;
        }else if(string.contains("hicar::default")){
            if(string.contains("playing"))
                mHICARPlaying = true;
            else
                mHICARPlaying = false;
        }
        Log.e(TAG,"UploadAppStatus::"+AppStatus+"..mInRadioPlaying:"+mInRadioPlaying+"...mRadioPlaying:"+mRadioPlaying+"...mMusicPlaying::"+mMusicPlaying+"...mLocalvideoPlaying::"+mLocalvideoPlaying+"..mVideoPlaying::"+mVideoPlaying);
    }

    @SuppressLint("MissingPermission")
    private String getCPUSerial() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Build.getSerial();
        }
        //??????CPU??????
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String cpu = null;
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((cpu = bufferedReader.readLine()) != null) {
                if (cpu.contains("Serial")) {
                    cpu = cpu.substring(cpu.indexOf(":") + 1).trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return cpu != null ? cpu.toUpperCase() : "0000000000000000";
    }
}
