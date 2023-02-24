package com.iflytek.adapter.common;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import com.iflytek.adapter.oneshot.OneShotConstant;
import com.iflytek.adapter.oneshot.OneShotManager;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.mvw.MvwSession;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.seopt.SeoptManager;
import com.iflytek.seopt.SeoptUtil;
import com.iflytek.speech.ISSErrors;
import com.iflytek.sr.SrSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.locks.ReentrantLock;

import static android.os.Process.THREAD_PRIORITY_URGENT_AUDIO;
public class PcmRecorder extends Thread {

    private static final String tag = "xyj_PcmRecorderSR";
    public SrSession seSession = null;

    public MvwSession mvw1 = null;
    public MvwSession mvw2 = null;
    private int scene = 0;

    private AudioRecord mRecorder = null;
    private Handler handler = null;
    private SrSessionArgu sessionArgu = new SrSessionArgu(
            SrSession.ISS_SR_SCENE_ALL, SrSession.ISS_SR_MODE_MIX_REC, 0);

    public SrTime srTime = new SrTime();

    // 记录一路识别Session的时间
    public class SrTime {
        public long iStartSrTime = 0; // 开始识别的时间
        public long iStartRecordTime = 0; // 开始录音时间
        public long iGetFirstAudioTime = 0; // 取得第一块录音的时间
        public long iSpeechStartTime = 0; // 检测到语音开始的时间
        public long iSpeechEndTime = 0; // 检测到语音结束的时间
        public long iEndAudioDataTime = 0; // 主动结束录音的时间
        public long iRecEndTime = 0; // 识别结束时间

        public void reSet() {
            iStartSrTime = 0;
            iStartRecordTime = 0;
            iGetFirstAudioTime = 0;
            iSpeechStartTime = 0;
            iSpeechEndTime = 0;
            iEndAudioDataTime = 0;
            iRecEndTime = 0;
        }
    }

    public void reSetSrTime() {
        srTime.reSet();
    }

    /**
     * 0:uninit
     * 1:recording SR
     * 2:recording VW
     * 3:recording MVW
     * 4:stop recording
     **/
    private static final int UNINIT = 0;
    private static final int RECORDING_SR = 1;
    private static final int RECORDING_VW = 2;
    private static final int RECORDING_MVW = 4;
    private static final int STOP_RECORDING = 8;
    private int STATE = UNINIT;
    private int MASK = 0xff;

    private int buffersize = -1;
    private byte[] buffer = null;
    private boolean isRunning = false;
    private ReentrantLock lock = new ReentrantLock();

    private int setTrack = 64;
    private int SET_CHANNELS = AudioFormat.CHANNEL_IN_MONO;

    public String strMvwWords = null;
    private static PcmRecorder mPcmRecorder;

    private PcmRecorder() {
        isRunning = true;
        android.os.Process.setThreadPriority(THREAD_PRIORITY_URGENT_AUDIO);
    }

    public static PcmRecorder getInstnace(){
        if(mPcmRecorder==null){
            synchronized (PcmRecorder.class){
                if(mPcmRecorder==null)
                    mPcmRecorder = new PcmRecorder();
            }
        }
        return mPcmRecorder;
    }

    /**
     * 停止线程
     */
    public void stopThread() {
        isRunning = false;
    }

    /**
     * 设置SR录音所需的参数
     *
     * @param srInstance
     * @param h
     * @param sessionArgu
     */
    public void setSRParams(SrSession srInstance, Handler h,
                            SrSessionArgu sessionArgu) {
        this.seSession = srInstance;
        this.handler = h;
        this.sessionArgu = sessionArgu;
    }

    /**
     * 设置MVW录音所需的参数
     *
     * @param mvw1
     * @param mvw2
     */
    public void setMVWParams(MvwSession mvw1, MvwSession mvw2, int scene) {
        this.mvw1 = mvw1;
        this.mvw2 = mvw2;
        this.scene = scene;
    }

    /**
     * 开始SR录音
     */
    public int startSRRecord() {
        Log.d(tag, "startSRRecord: ");
        if (seSession == null) {
            return -10010;
        }
        int errStartid = 300;
        srTime.iStartSrTime = System.currentTimeMillis(); // 开始一次识别的时间
        Log.d(tag, "startSRRecord() called:scene:"+sessionArgu.scene+"..sessionArgu:"+sessionArgu.mode+"..szCmd:"+sessionArgu.szCmd);
        if (SrSession.ISS_SR_SCENE_BUILD_GRM_MVW.equals(sessionArgu.scene)) {
            JSONObject objRoot = new JSONObject();
            JSONArray objArr = new JSONArray();
            String[] strWords = strMvwWords.split(",");
            for (int nIndex = 0; nIndex != strWords.length; nIndex++) {
                JSONObject objWord = new JSONObject();
                try {
                    objWord.put("KeyWordId", nIndex);
                    objWord.put("KeyWord", strWords[nIndex]);
                    objWord.put("DefaultThreshold40", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                objArr.put(objWord);
            }
            try {
                objRoot.put("Keywords", objArr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(tag, "wakeup words = " + objRoot.toString());
            errStartid = seSession.start(sessionArgu.scene, sessionArgu.mode,
                    objRoot.toString());
        } else if (SrSession.ISS_SR_SCENE_STKS.equals(sessionArgu.scene)) { //可见即可说使用本地识别模式
            Log.d(tag, "start ISS_SR_SCENE_STKS: " + sessionArgu.szCmd);
            errStartid = seSession.start(sessionArgu.scene, SrSession.ISS_SR_MODE_LOCAL_REC,
                    sessionArgu.szCmd);
            Log.e(tag, "start ISS_SR_SCENE_STKS:, ERR_ID=" + errStartid);
        } else if (sessionArgu.mode == SrSession.ISS_SR_MODE_LOCAL_NLP) {
            errStartid = seSession.start(sessionArgu.scene, sessionArgu.mode,
                    Incs.NLPszCmd);
        } else if (sessionArgu.mode == SrSession.ISS_SR_MODE_LOCAL_CMDLIST) {
            errStartid = seSession.start(sessionArgu.scene, sessionArgu.mode,
                    Incs.szCmd);
        } else {
            errStartid = seSession.start(sessionArgu.scene, sessionArgu.mode,
                    null);
        }
        Log.d(tag, "session start end");
//        if (sessionArgu.track == 128) {
//            setTrack = 128;
//            SET_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
//        } else if (sessionArgu.track == 64) {
//            setTrack = 64;
//            SET_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
//        }
        if (errStartid != ISSErrors.ISS_SUCCESS) {
            Log.e(tag, "start sr failed, ERR_ID=" + errStartid);
            return errStartid;
        }

        buffer = new byte[buffersize];
        STATE |= RECORDING_SR;
        Log.d(tag, "STATE = " + STATE);

        return errStartid;
    }

    /**
     * 停止SR录音
     */
    public int endSRAudioData() {
        if (seSession == null) {
            return 10000;
        }

        STATE &= (MASK & ~RECORDING_SR);

        int errid = seSession.endAudioData();

        srTime.iEndAudioDataTime = System.currentTimeMillis() - srTime.iStartSrTime;

//        Message msg = new Message();
//        msg.what = Incs.SR_MSG_ENDAUDIODATA_RETURN;
//        Bundle b = new Bundle();
//        b.putInt("errid", errid);
//        msg.setData(b);
//        msg.setTarget(handler);
//        msg.sendToTarget();
        Log.d(tag, "PcmRecorderSR endSRAudioData, errid:" + errid+"..STATE:"+STATE);
        return errid;
    }

    public void stopSrSession(){
        if (seSession != null) {
            seSession.stop();
        }
    }

    public void stopSRRecord() {
        if (seSession == null) {
            return;
        }
        STATE &= (MASK & ~RECORDING_SR);

        Log.d(tag, "stopSRRecord STATE:" + STATE);

    }

    /**
     * 开始MVW录音
     */
    public void startMVWRecord() {
        int id1 = 0,id2=0;
        if(mvw1!=null)
           id1 = mvw1.start(scene);
        if(mvw2!=null)
            id2 = mvw2.start(scene);
        int id = id1 + id2;
        if (id != ISSErrors.ISS_SUCCESS) {
            Log.d(tag, "mVW SessionStart error,id = " + id);
        }
        buffer = new byte[buffersize];
        STATE |= RECORDING_MVW;
        Log.d(tag, "Start mvw Session STATE:" + STATE);
    }

    /**
     * 开始MVW录音
     */
    public void addStartMVWRecord() {
        int id1 = 0,id2=0;
        if(mvw1!=null)
           id1 = mvw1.addStartScene(scene);
        if(mvw2!=null)
           id2 = mvw2.addStartScene(scene);
        int id = id1 + id2;
        if (id != ISSErrors.ISS_SUCCESS) {
            Log.d(tag, "mVW addStartScene error,id = " + id);
        }
        buffer = new byte[buffersize];
        STATE |= RECORDING_MVW;
    }

    /**
     * 停止VW录音
     */
    public void stopMVWRecord() {
        int id1 = 0,id2=0;
        if(mvw1!=null)
            id1 = mvw1.stop();
        if (mvw2!=null) {
            id2 = mvw2.stop();
        }
        int id = id1 + id2;
        if (id != ISSErrors.ISS_SUCCESS) {
            Log.d(tag, "mVW stopMVWRecord error,id = " + id);
        }
        STATE &= (MASK & ~RECORDING_MVW);
        Log.d(tag, "Stop mvw Session STATE:" + STATE);
    }

    /**
     * 设置主唤醒词
     */
    public int setMvwKeyWords(int scence, String words) {
        Log.d(tag, "setMvwKeyWords " + words);
        int id1 = 0,id2=0;
        if(mvw1!=null)
            id1 = mvw1.setMvwKeyWords(scence, words);
        if(mvw2!=null)
            id2 = mvw2.setMvwKeyWords(scence, words);
        int id = id1 + id2;
        if (id != ISSErrors.ISS_SUCCESS) {
            Log.d(tag, "mVW setMvwKeyWords error,id = " + id);
        }
        return id;
    }

    /**
     * 设置默认
     */
    public int setMvwDefaultKey(int scence) {
        Log.d(tag, "setMvwDefaultKey() called with: scence = [" + scence + "]");
        int id1 = 0,id2=0;
        if(mvw1!=null)
            id1 = mvw1.setMvwDefaultKeyWords(scence);
        if(mvw2!=null)
            id2 = mvw2.setMvwDefaultKeyWords(scence);
        int id = id1 + id2;
        if (id != ISSErrors.ISS_SUCCESS) {
            Log.d(tag, "mVW setMvwKeyWords error,id = " + id);
        }
        return id;
    }

    public void changeChannel(int channelConfig) {
        // 改变录音的声道
        lock.lock();
        try {
            buffersize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE,
                    channelConfig, DEFAULT_AUDIO_FORMAT);
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
            }
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    DEFAULT_SAMPLE_RATE, channelConfig, DEFAULT_AUDIO_FORMAT,
                    buffersize * 3);
            if (mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                Log.e(tag, "Error: AudioRecord state == STATE_UNINITIALIZED");
                return;
            }
            mRecorder.startRecording();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        lock.lock();
        try {
            if (SeoptConstant.USE_SEOPT) {
                setTrack = 128;
                SET_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
            }
            buffersize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE,
                    SET_CHANNELS, DEFAULT_AUDIO_FORMAT);

            Log.d(tag, "buffersize=" + String.valueOf(buffersize));
            mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    DEFAULT_SAMPLE_RATE, SET_CHANNELS, DEFAULT_AUDIO_FORMAT,
                    buffersize * 3);
            if (mRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
                Log.e(tag, "Error: AudioRecord state == STATE_UNINITIALIZED");
                return;
            }
            mRecorder.startRecording();
        } finally {
            lock.unlock();
        }

        while (isRunning) {
            try {
                if ((STATE & RECORDING_MVW) != 0) {
                    int count = mRecorder.read(buffer, 0, buffersize);
                    if (count > 0) {
                        if (OneShotConstant.USE_ONESHOT && !SeoptConstant.USE_SEOPT) {
                            OneShotManager.getInstance().cycleQueueOneShot.produce(buffer);
                        }
                        if (SeoptConstant.USE_SEOPT) {
                            byte[][] bytes = SeoptUtil.splitStereoPcm(buffer);
                            byte[] leftData = bytes[0];
                            byte[] rightData = bytes[1];
                            mvw1.appendAudioData(leftData);
                            mvw2.appendAudioData(rightData);

                            if (OneShotConstant.USE_ONESHOT) {
                                OneShotManager.getInstance().cycleQueueOneShot.produce(leftData);
                            }
                        } else {
                            mvw1.appendAudioData(buffer);
//                        mvw2.appendAudioData(buffer);
                        }

                    }

                    if ((STATE & RECORDING_SR) != 0) {
                        if (srTime.iStartRecordTime == 0) {
                            // 需要重新开启录音，免得送入开始缓存的录音 导致识别出错
                            if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                                mRecorder.stop();
                                mRecorder.startRecording();
                            }
                            srTime.iStartRecordTime = System.currentTimeMillis() - srTime.iStartSrTime;
                        }
                        if (srTime.iGetFirstAudioTime == 0) {
                            // 获取第一块录音的时间
                            srTime.iGetFirstAudioTime = System.currentTimeMillis() - srTime.iStartSrTime;
                        }
                        if (SeoptConstant.USE_SEOPT) {
                            SeoptManager.getInstance().blockingQueueSr.produce(buffer);
                        } else {
                            seSession.appendAudioData(buffer);
                        }

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(buffersize / (setTrack));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
        }
    }

    private static final int DEFAULT_SAMPLE_RATE = 16 * 1000;
    private static final short DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_TIMER_INTERVAL = 40;
    // private static final short DEFAULT_CHANNELS =
    // AudioFormat.CHANNEL_IN_MONO;

    public boolean isSrStarted(){
        Log.d(tag, "isSrStarted() called  STATE:"+STATE);
        return (STATE & RECORDING_SR) != 0;
    }

}
