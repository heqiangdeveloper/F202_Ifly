package com.iflytek.adapter.mvw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.Incs;
import com.iflytek.adapter.common.PcmRecorder;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.oneshot.OneShotConstant;
import com.iflytek.adapter.oneshot.OneShotManager;
import com.iflytek.mvw.IMvwListener;
import com.iflytek.mvw.MvwSession;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.seopt.SeoptManager;
import com.iflytek.seopt.SeoptUtil;
import com.iflytek.speech.ISSErrors;
import com.iflytek.speech.libissseopt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class MVWAgent {
    private static final String TAG = "xyj_MVWAgent";
    public static MVWAgent instance = null;
    public MvwSession mIvw1 = null;
    public MvwSession mIvw2 = null;
    private PcmRecorder mPcmRecorder;
    public boolean init_state = false;
    Handler mHandler = null;
    private boolean inited = false;
//    private int setTrackTemp = 64;
    private Context context;
    public long iMvwTrigTime;
    public boolean mGlobeMwv = false;//区分主唤醒词唤醒 和 其他免唤醒词唤醒

    private static final String resPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/iflytek/ica/res/MVWRes/FirstRes";
    private static final String resPath1 = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/iflytek/ica/res/MVWRes/SecondRes";

    private static final String resNaviPath = "/data/navi/0/iflytek/ica/res/MVWRes/FirstRes";
    private static final String resNaviPath1 = "/data/navi/0/iflytek/ica/res/MVWRes/SecondRes";

    /**
     * 用于判定是否为一路唤醒
     */
    Handler countHandler = null;

    int wvmCount = 0;
    private static final int MSG_MVW_COUNT = 1001;
    private String leftParam, rightParam;
    private final static String LEFT = "left";
    private final static String RIGHT = "right";
    public final static int MVM_COUNT = 5;// 0--1--2-- 3  判断免唤醒词的下限值

    @SuppressLint("HandlerLeak")
    public MVWAgent init(final Context context) {
        Log.d(TAG, "init");
        this.context = context.getApplicationContext();
        if (inited) {
            return instance;
        }
        if (0 != MvwSession.setMvwLanguage(MvwSession.ISS_MVW_LANG_CHN)) {
            Log.d(TAG, "Set lang error");
        }
        MvwSession.isCouldAppendAudioData();

        File file = new File(resPath);
        if(file.exists()){
            Log.d(TAG, "init() called with: context = [" + file.getAbsolutePath() + "]");
            mIvw1 = new MvwSession(context, mvwListener1, resPath);
            if (SeoptConstant.USE_SEOPT)
                mIvw2 = new MvwSession(context, mvwListener2, resPath1);
        }else {
            mIvw1 = new MvwSession(context, mvwListener1, resNaviPath);
            if (SeoptConstant.USE_SEOPT)
                mIvw2 = new MvwSession(context, mvwListener2, resNaviPath1);
        }


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Incs.MVW_MSG_WAKEUP: {
                        Bundle b = msg.getData();
                        int nMvwScene = b.getInt("nMvwScene");
                        int nMvwId = b.getInt("nMvwId");
                        int nMvwScore = b.getInt("nMvwScore");
                        String szRlt = b.getString("szRlt");
                        String lParam = b.getString("lParam");
                        Log.d(TAG, "------------\n唤醒成功---szRlt=" + szRlt
                                + "---nMvwScore=" + nMvwScore + "\n------------\n");

                        Log.d("xyj0000", "onDoMvwAction 1111");
                        //收到主唤醒词, 拉起语音助手悬浮界面
                        if (PlatformHelp.getInstance().getPlatformClient() != null) {
                            PlatformHelp.getInstance().getPlatformClient().onDoMvwAction(lParam);
                            if(nMvwScene==MvwSession.ISS_MVW_SCENE_GLOBAL&&(nMvwId>=0&&nMvwId<=MVM_COUNT)||lParam.contains("你好")/*nMvwScene==MvwSession.ISS_MVW_SCENE_CUSTOME&&(nMvwId==0&&nMvwId==1)*/){
                                iMvwTrigTime = System.currentTimeMillis();//这里还是要添加，避免二次唤醒导致语音退出
                                mGlobeMwv = true;
                                Log.w(TAG, "handleMessage: do not set time");
                            }else{
                                mGlobeMwv = false;
                                iMvwTrigTime = System.currentTimeMillis();
                                Log.w(TAG, "handleMessage: set the time::"+iMvwTrigTime);
                            }

                           /* if(!isOneShotMvw(lParam)) {
                                iMvwTrigTime = System.currentTimeMillis();
                            }*/
                        }
                        break;
                    }
                    default: {
                        Log.d(TAG, "ERROR :  unhandled message type.");
                    }
                }
            }
        };

        inited = true;
        countHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "handleMessage: "+wvmCount);
                switch (msg.what) {
                    case MSG_MVW_COUNT:
                        Log.i(TAG, "wvmCount3333 = " + wvmCount);
                        if (wvmCount == 1) {
                            // 是一路唤醒
                            Bundle bundle = msg.getData();
                            String lParam = bundle.getString("lParam");
                            String direction = bundle.getString("direction");
                            if ("left".equals(direction)) {
                                direction = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_LEFT;
                            } else {
                                direction = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT;
                            }
                            Log.i(TAG, "设置 seoptDirection 方向= " + direction);
                            SeoptManager.getInstance().setDirection(direction);
                            wvmCount = 0;
                        } else if (wvmCount == 2) {
                            // 是两路唤醒
                            Bundle bundle = msg.getData();
                            String lParam = bundle.getString("lParam");
                            Log.i(TAG, "leftParam = " + leftParam);
                            Log.i(TAG, "rightParam = " + rightParam);
                            String seopt_direction = SeoptUtil.
                                    getDecDirection(leftParam, rightParam);
                            Log.i(TAG, "设置 seoptDirection 方向= " + seopt_direction);
                            SeoptManager.getInstance().setDirection(seopt_direction);
                        } else {
                            // 异常情况
                            Log.w(TAG, "wvmCount == " + wvmCount);
                        }
                        wvmCount = 0;
                        break;
                    case Incs.MVW_MSG_WAKEUP:
                        String direction = "-1";
                        if (wvmCount == 1) {
                            // 是一路唤醒
                            Bundle bundle = msg.getData();
                            String lParam = bundle.getString("lParam");
                            direction = bundle.getString("direction");
                            if ("left".equals(direction)) {
                                direction = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_LEFT;
                            } else {
                                direction = libissseopt.ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT;
                            }
                        } else if (wvmCount == 2) {
                            // 是两路唤醒
                            Bundle bundle = msg.getData();
                            String lParam = bundle.getString("lParam");
                            direction = SeoptUtil.getDecDirection(leftParam, rightParam);
                        } else {
                            // 异常情况
                            Log.w(TAG, "wvmCount == " + wvmCount);
                        }
                        Log.d("xyj123456789", "wvmCount = " + wvmCount);
                        wvmCount = 0;

                        Bundle b = msg.getData();
                        int nMvwScene = b.getInt("nMvwScene");
                        int nMvwId = b.getInt("nMvwId");

                        Log.d(TAG, "handleMessage: nMvwScene:"+nMvwScene);
                        Log.d("xyj123456789", "direction1 = " + SeoptManager.getInstance().seopt_direction);
                        Log.d("xyj123456789", "direction2 = " + direction);

                        //免唤醒词不走声源定位逻辑，在这里不要判断声源方向是否相反
                        if (!TextUtils.isEmpty(SeoptManager.getInstance().seopt_direction) && !SeoptManager.getInstance().seopt_direction.equals(direction)&&nMvwScene==1) {
                            Log.e("xyj123456789", "two direction no equal, error ");
                            iMvwTrigTime = System.currentTimeMillis(); //规避进行识别
//                            return;
                        }

                        int nMvwScore = b.getInt("nMvwScore");
                        String szRlt = b.getString("szRlt");
                        String lParam = b.getString("lParam");
                        Log.d(TAG, "------------\n唤醒成功---szRlt=" + szRlt
                                + "---nMvwScore=" + nMvwScore + "---lParam=" + lParam + "\n------------\n");
                        //收到主唤醒词, 拉起语音助手悬浮界面
                        if (PlatformHelp.getInstance().getPlatformClient() != null) {
                            PlatformHelp.getInstance().getPlatformClient().onDoMvwAction(lParam);
                            if(!isOneShotMvw(lParam)) {
                                iMvwTrigTime = System.currentTimeMillis();
                            }
                        }
                        break;
                }
            }
        };
        return instance;
    }

    /**
     * {"nMvwScene":1, "nMvwId":28, "nMvwScore":1135, "wakeup":1, "nStartBytes":95040, "nEndBytes":114880, "nKeyword":"小欧我要", "PowerValue":1300541865984.000000}
     */
    private boolean isOneShotMvw(String lParam) {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(lParam);
            int nMvwScene = jsonObj.optInt("nMvwScene");
            int nMvwId = jsonObj.optInt("nMvwId");
            Log.d(TAG, "nMvwScene:" + nMvwScene + " ,nMvwId:" + nMvwId);
          /*  if (nMvwScene == MvwSession.ISS_MVW_SCENE_CUSTOME) {
                if (nMvwId >= 0 && nMvwId <= 4) { //小欧我要/想 免唤醒词
                    return true;
                }
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void setPcmRecorder(PcmRecorder pcmRecorder) {
        this.mPcmRecorder = pcmRecorder;
    }

    //识别时使用多个场景
    public void startMVWSession(int tspScene) {
        Log.d(TAG, "startMVWSession() called with: tspScene = [" + tspScene + "]"+Log.getStackTraceString(new Throwable()));
        if (!init_state) {
            Log.d(TAG, "ERROR:合成会话初始化未成功！\n");
            return;
        }
        Log.e(TAG, "startMVWSession:" + tspScene);
        if (mPcmRecorder == null) {
            Log.d(TAG, "ERROR:录音机为空！\n");
            return;
        }

        if (OneShotConstant.USE_ONESHOT) {
            OneShotManager.getInstance().clear();
        }

        mPcmRecorder.setMVWParams(mIvw1, mIvw2, TspSceneAdapter.convert2MvwScene(tspScene));
//        mPcmRecorder.setSleepInterval(setTrackTemp);
        mPcmRecorder.startMVWRecord();

        TspSceneAdapter.saveTspScene(context, tspScene);
    }

    public void release() {
        inited = false;
        init_state = false;

        stopMVWSession();
        if (null != mIvw1) {
            mIvw1.release();
            mIvw1 = null;
        }
        if (null != mIvw2) {
            mIvw2.release();
            mIvw2 = null;
        }
    }

    public static MVWAgent getInstance() {
        if (instance == null)
            instance = new MVWAgent();
        return instance;
    }

    public void stopMVWSession() {
        if (mPcmRecorder == null) {
            Log.d(TAG, "ERROR:录音机为空！\n");
            return;
        }
        mPcmRecorder.stopMVWRecord();
        Log.d(TAG, "停止录音.\n");
    }

    public void setMvwKeyWordsForRawData(int scene, String words) {
        Log.d(TAG, "start build mvw words");
        JSONObject objRoot = new JSONObject();
        JSONArray objArr = new JSONArray();
        String[] strWords = words.split(",");
        mPcmRecorder.setMVWParams(mIvw1, mIvw2, scene);

        for (int nIndex = 0; nIndex != strWords.length; nIndex++) {
            JSONObject objWord = new JSONObject();
            try {
                objWord.put("KeyWordId", nIndex);
                objWord.put("KeyWord", strWords[nIndex]);
                objWord.put("DefaultThreshold40", 0);
                objArr.put(objWord);
            } catch (JSONException e) {
                Log.d(TAG, "exception = " + e.toString());
                return;
            }
        }
        try {
            objRoot.put("Keywords", objArr);
            Log.d(TAG, "wakeup words = " + objRoot.toString());
            mPcmRecorder.setMvwKeyWords(scene, objRoot.toString());
        } catch (JSONException e) {
            Log.d(TAG, "exception = " + e.toString());
        }
    }

    /**
     * 设置唤醒词, 传入的是JSON数据
     *
     * @param scene
     * @param keywordJson
     */
    public void setMvwKeyWords(int scene, String keywordJson) {
        Log.d(TAG, "setMvwKeyWords() called with: scene = [" + scene + "], keywordJson = [" + keywordJson + "]");
        mPcmRecorder.setMVWParams(mIvw1, mIvw2, scene);
        mPcmRecorder.setMvwKeyWords(scene, keywordJson);
    }

    public void setMvwDefaultKey(int scene) {
        Log.d(TAG, "start restore mvw words");
        mPcmRecorder.setMVWParams(mIvw1, mIvw2, scene);

        mPcmRecorder.setMvwDefaultKey(scene);
    }

    private IMvwListener mvwListener1 = new IMvwListener() {

        @Override
        public void onVwWakeup(int nMvwScene, int nMvwId, int nMvwScore, String lParam) {
            Log.d(TAG, "wake up1111");
            Log.d(TAG, "lParam1 = " + lParam);
            MVWAgent.this.onVwWakeup(LEFT ,nMvwScene, nMvwId, nMvwScore, lParam);
        }

        @Override
        public void onVwInited(boolean state, int errId) {
            Log.d(TAG, "onVwInited： " + Thread.currentThread().getName());
            if (state) {
                init_state = true;
                Log.d(TAG, "多唤醒会话初始化成功111.\n");
            } else {
                if (errId == ISSErrors.REMOTE_EXCEPTION) {
                    Log.d(TAG, "MVW服务连接失败，尝试重新连接\n");
                    mIvw1.initService();
                }
            }
        }
    };

    private IMvwListener mvwListener2 = new IMvwListener() {

        @Override
        public void onVwWakeup(int nMvwScene, int nMvwId, int nMvwScore, String lParam) {
            Log.d(TAG, "wake up222");
            Log.d(TAG, "lParam2 = " + lParam);
            MVWAgent.this.onVwWakeup(RIGHT ,nMvwScene, nMvwId, nMvwScore, lParam);
        }

        @Override
        public void onVwInited(boolean state, int errId) {
            Log.d(TAG, "onVwInited： " + Thread.currentThread().getName());
            if (state) {
                init_state = true;
                Log.d(TAG, "多唤醒会话初始化成功222.\n");
            } else {
                if (errId == ISSErrors.REMOTE_EXCEPTION) {
                    // mTextView.append("MVW服务连接失败，尝试重新连接\n");
                    mIvw2.initService();
                }
            }
        }
    };

    private synchronized void onVwWakeup(final String direction, int nMvwScene, int nMvwId, int nMvwScore, String lParam) {
        Log.d(TAG, "onVwWakeup() called with: direction = [" + direction + "], nMvwScene = [" + nMvwScene + "], nMvwId = [" + nMvwId + "], nMvwScore = [" + nMvwScore + "], lParam = [" + lParam + "]");
        Message msg = new Message();
        msg.what = Incs.MVW_MSG_WAKEUP;
        Bundle b = new Bundle();
        b.putInt("nMvwScene", nMvwScene);
        b.putInt("nMvwId", nMvwId);
        b.putInt("nMvwScore", nMvwScore);
        b.putString("lParam", lParam);
        String szRlt = "null";
        boolean flag = true;  //是否处理声源定位
        if (nMvwScene == MvwSession.ISS_MVW_SCENE_GLOBAL) {
            //只有在隐藏的时候才将 seopt_direction 设置为空
            // TextUtils.isEmpty(SeoptManager.getInstance().seopt_direction) 屏蔽点这句话，每次唤醒都重新声源定位一次 如果右麦唤醒，然后左麦再唤醒？

            if (nMvwId >= 0 && nMvwId <=MVM_COUNT) {
                szRlt = "你好语音助理";
                Log.d(TAG, "onVwWakeup:seopt_direction:: "+SeoptManager.getInstance().seopt_direction+"..."+SeoptConstant.USE_SEOPT);
                if (SeoptConstant.USE_SEOPT /*&&
                        TextUtils.isEmpty(SeoptManager.getInstance().seopt_direction)*/) {
                    flag = false;
                    dealMVWCount(direction, lParam);
                }

            }
        }else if (nMvwScene == MvwSession.ISS_MVW_SCENE_CUSTOME) {
                //只有在隐藏的时候才将 seopt_direction 设置为空
                // TextUtils.isEmpty(SeoptManager.getInstance().seopt_direction) 屏蔽点这句话，每次唤醒都重新声源定位一次 如果右麦唤醒，然后左麦再唤醒？

                if (/*nMvwId == 0 || nMvwId ==1*/lParam.contains("你好")) {
                    szRlt = "你好语音助理";
                    Log.d(TAG, "onVwWakeup:seopt_direction:: " + SeoptManager.getInstance().seopt_direction + "..." + SeoptConstant.USE_SEOPT);
                    if (SeoptConstant.USE_SEOPT /*&&
                        TextUtils.isEmpty(SeoptManager.getInstance().seopt_direction)*/) {
                        flag = false;
                        dealMVWCount(direction, lParam);
                    }

                }
        }else {
            szRlt = "" + nMvwId;
        }

        b.putString("szRlt", szRlt);
        if (SeoptConstant.USE_SEOPT && flag) {  //如果是免唤醒词
            dealMVWDirection(direction, b, lParam);
        } else {   //如果是主唤醒词，直接启动界面  mhandler  wakeup
            msg.setData(b);
            msg.setTarget(mHandler);
            msg.sendToTarget();
        }

    }

    /**
     * 处理唤醒路数，获取声源方向
     *
     * @param direction 唤醒的方向 left right
     * @param lParam    唤醒中包含的参数json
     */
    public void dealMVWCount(String direction, String lParam) {
        Log.i(TAG, "dealMVWCount begind wvmCount=" + wvmCount);
        if ("left".equals(direction)) {
            leftParam = lParam;
        } else {
            rightParam = lParam;
        }
        if (wvmCount < 2) {
            wvmCount++;
            countHandler.removeCallbacksAndMessages(null);
            Message message = Message.obtain(countHandler);
            message.what = MSG_MVW_COUNT;
            Bundle bundle = new Bundle();
            bundle.putString("lParam", lParam);
            bundle.putString("direction", direction);
            message.setData(bundle);
            Log.i(TAG, "dealMVWCount begind wvmCount end::=" +wvmCount);
            if(wvmCount==1)
                countHandler.sendMessageDelayed(message, 350);
            else if(wvmCount==2)
                countHandler.sendMessageDelayed(message, 0);
        } else {
            Log.w(TAG, "wvmCount 异常为 ：" + wvmCount);
            wvmCount = 0;
        }
    }


    /**
     *
     */
    public void dealMVWDirection(String direction, Bundle bundle, String lParam) {
        Log.d(TAG, "dealMVWDirection::begind "+wvmCount);
        if ("left".equals(direction)) {
            leftParam = lParam;
        } else {
            rightParam = lParam;
        }
        if (wvmCount < 2) {
            wvmCount++;
            countHandler.removeCallbacksAndMessages(null);
            Message message = Message.obtain(countHandler);
            message.what = Incs.MVW_MSG_WAKEUP;
            bundle.putString("direction", direction);
            message.setData(bundle);
            Log.d(TAG, "dealMVWDirection: wvmCount end：：："+wvmCount);
            if(wvmCount==1)
                countHandler.sendMessageDelayed(message, 350);
            else if(wvmCount==2)
                countHandler.sendMessageDelayed(message, 0);
        } else {
            Log.w(TAG, "dealMVWDirection 异常为 ：" + wvmCount);
            wvmCount = 0;
        }
    }
}
