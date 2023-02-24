package com.iflytek.adapter.oneshot;

import android.util.Log;

import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.mvw.MvwSession;
import com.iflytek.seopt.SeoptConstant;
import com.iflytek.seopt.SeoptManager;
import com.iflytek.seopt.SeoptUtil;
import com.iflytek.sr.SrSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CycleQueueOneShot {
    private final static String TAG = "BlockingQueueSr";

    private Container container;
    private FileOutputStream buffer_outSr1;
    private FileOutputStream buffer_outSr2;
    private FileOutputStream buffer_outSr3;
    private Object lock = new Object();
    private static final boolean DEBUG = false;

    public CycleQueueOneShot() {
        this.container = new Container();
    }

    public void start(SrSession srSession, MvwSession mvwSession) {
        new Consumer(srSession, mvwSession).start();
    }

    /**
     * 生产数组
     *
     * @param bytes
     */
    public void produce(byte[] bytes) {
        container.produce(bytes);
        synchronized (lock) {
            OneShotManager.mInsertBytes += bytes.length;
        }
    }

    /**
     * 重置缓冲区
     *
     */
    public void clear() {
        synchronized (lock) {
            OneShotManager.mInsertBytes = 0;
        }
        container.clear();
    }

    /**
     * 定义装数组的容器
     */
    private static class Container {
        CycleQueue cycleQueue = new CycleQueue(OneShotConstant.MAX_BUFFER_SIZE);

        // 生产数组
        public void produce(byte[] voiceByte) {
            cycleQueue.produce(voiceByte);
        }

        // 一次消费全部数据
        public byte[] consume() {
            return cycleQueue.consumeAll();
        }

        //消除缓冲区
        public void clear() {
            cycleQueue.clear();
        }
    }

    // 定义数组消费者
    public class Consumer extends Thread {
        private SrSession srSession;
        private MvwSession mvwSession;

        public Consumer(SrSession srSession, MvwSession mvwSession) {
            this.srSession = srSession;
            this.mvwSession = mvwSession;
        }

        @Override
        public void run() {
            if (srSession != null && mvwSession != null) {
                byte[] voiceBytes = container.consume();
                if (voiceBytes == null || voiceBytes.length == 0) {
                    return;
                }
                if (DEBUG) {
                    try {
                        buffer_outSr1 = new FileOutputStream(
                                new File("/sdcard/oneshot_test/buffer_outSr1_"
                                        + System.currentTimeMillis() / 1000 + ".pcm"),
                                true);
                        buffer_outSr1.write(voiceBytes);
                        buffer_outSr1.close();
                    } catch (IOException e) {
                    }
                }
                byte[] localBytes = getOneShotByte(voiceBytes);
                Log.e("xyj123", "localBytes:" + localBytes.length);
                if (localBytes.length > 0) {
                    if (DEBUG) {
                        try {
                            buffer_outSr2 = new FileOutputStream(
                                    new File("/sdcard/oneshot_test/buffer_outSr2_"
                                            + System.currentTimeMillis() / 1000 + ".pcm"), true);
                            buffer_outSr2.write(localBytes);
                            buffer_outSr2.close();
                        } catch (IOException e) {
                        }
                    }
                    if (SeoptConstant.USE_SEOPT) {
                        byte[] byteMerger = SeoptUtil.byteMerger(localBytes);
                        if (DEBUG) {
                            try {
                                buffer_outSr3 = new FileOutputStream(
                                        new File("/sdcard/oneshot_test/buffer_outSr3_"
                                                + System.currentTimeMillis() / 1000 + ".pcm"), true);
                                buffer_outSr3.write(localBytes);
                                buffer_outSr3.close();
                            } catch (IOException e) {
                            }
                        }
                        srSession.start(SrSession.ISS_SR_SCENE_ALL, SrSession.ISS_SR_MODE_MIX_REC, "");
                        srSession.appendAudioData(byteMerger);

                    } else {
                        srSession.start(SrSession.ISS_SR_SCENE_ALL, SrSession.ISS_SR_MODE_MIX_REC, "");
                        srSession.appendAudioData(localBytes);
                    }
                    srSession.isOneshot = true;
                }

                if (SeoptConstant.USE_SEOPT) {
                    MVWAgent.getInstance().mIvw1.stop();
                    MVWAgent.getInstance().mIvw1.start(MvwSession.ISS_MVW_SCENE_GLOBAL);

                    MVWAgent.getInstance().mIvw2.stop();
                    MVWAgent.getInstance().mIvw2.start(MvwSession.ISS_MVW_SCENE_GLOBAL);
                } else {
                    mvwSession.stop();
                    mvwSession.start(MvwSession.ISS_MVW_SCENE_GLOBAL);
                }
                clear();

                Log.e("xyj123", "deal oneshot completed");

            } else {
                Log.d("xyj123", "srSession=" + srSession + " ,mvwSession=" + mvwSession);
            }
        }

        private byte[] getOneShotByte(byte[] buffer) {
            byte[] localBuffer = new byte[0];
            Log.e("xyj123", "getOneShotByte mEndBytes:" + OneShotManager.mEndBytes + " ,mInsertBytes:" + OneShotManager.mInsertBytes + " ,buffer:" + buffer.length);
            int newBufferSize = (int) (OneShotManager.mInsertBytes - OneShotManager.mEndBytes);
            if (newBufferSize < 0 || newBufferSize > buffer.length) {
                Log.e("xyj123", " exception: newBufferSize < 0 || newBufferSize > buffer.length");
                return localBuffer;
            }
            localBuffer = new byte[newBufferSize];
            System.arraycopy(buffer, (buffer.length - newBufferSize), localBuffer, 0, newBufferSize);
            return localBuffer;
        }
    }

}
