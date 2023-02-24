package com.iflytek.seopt;

import android.util.Log;

import com.iflytek.sr.SrSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueueSr {
    private final static String TAG = "BlockingQueueSr";

    private Container container;
    private Consumer consumer;
    private byte[] cacheBuffer;
    private String direction;
    private FileOutputStream buffer_outSr1;
    private FileOutputStream buffer_outSr2;
    private static final boolean DEBUG = false;

    public BlockingQueueSr() {
        container = new Container();
        consumer = new Consumer(null);
        consumer.start();
    }

    public SrSession getSrSession() {
        return consumer.srSession;
    }

    public void setSrSession(SrSession srSession) {
        consumer.srSession = srSession;
    }

    public void startSrRecord() {
        consumer.isRunning = true;
    }

    public void stopSrRecord() {
        consumer.isRunning = false;
        container.clear();
    }

    /**
     * 生产数组
     *
     * @param bytes
     */
    public void produce(byte[] bytes) {
        VoiceByte voiceByte1 = new VoiceByte(bytes);
        try {
            container.produce(voiceByte1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 定义装数组的容器
     */
    private static class Container {
        // 定义容纳voiceByte 的容器
        LinkedBlockingQueue<VoiceByte> linkedBlockingQueue = new LinkedBlockingQueue<VoiceByte>(2048);

        // 生产数组
        public void produce(VoiceByte voiceByte) throws InterruptedException {
            linkedBlockingQueue.put(voiceByte);
        }

        // 消费数组，取数组
        public synchronized VoiceByte[] consume() throws InterruptedException {
            VoiceByte[] voiceByteArr = linkedBlockingQueue.toArray(new VoiceByte[0]);
            linkedBlockingQueue.clear();
            return voiceByteArr;
        }

        //清除数据
        public void clear() {
            linkedBlockingQueue.clear();
        }
    }

    // 定义数组消费者
    public class Consumer extends Thread {
        private SrSession srSession;
        public boolean isRunning;

        public Consumer(SrSession srSession) {
            this.srSession = srSession;
        }

        public void run() {
            try {
                while (true) {
                    if (isRunning && srSession != null) {
                        VoiceByte[] voiceByteArr = container.consume();
                        if (voiceByteArr == null || voiceByteArr.length == 0) {
                            Thread.sleep(1408 / 128);
                            continue;
                        }
                        int arrSize = 0;
                        for (int i = 0; i < voiceByteArr.length; i++) {
                            VoiceByte voiceByte = voiceByteArr[i];
                            arrSize += voiceByte.getLocalBytes().length;
                        }

                        byte[] bytes = new byte[arrSize];
                        byte[] b;
                        int size = 0;
                        for (int i = 0; i < voiceByteArr.length; i++) {
                            VoiceByte voiceByte = voiceByteArr[i];
                            b = voiceByte.getLocalBytes();
                            System.arraycopy(b, 0, bytes, size, b.length);
                            size += b.length;
                        }

                        if(DEBUG) {
                            try {
                                if (buffer_outSr1 == null) {
                                    buffer_outSr1 = new FileOutputStream(
                                            new File("/sdcard/seopt_test/buffer_outSr1_"
                                                    + System.currentTimeMillis() / 1000 + ".pcm"),
                                            true);
                                }
                                buffer_outSr1.write(bytes);
                                buffer_outSr1.close();
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                        }
                        byte[] localBytes = getSeoptByte(bytes);

                        if(DEBUG) {
                            try {
                                if (buffer_outSr2 == null) {
                                    buffer_outSr2 = new FileOutputStream(
                                            new File("/sdcard/seopt_test/buffer_outSr2_"
                                                    + System.currentTimeMillis() / 1000 + ".pcm"), true);
                                }
                                buffer_outSr2.write(localBytes);
                                buffer_outSr2.close();
                            } catch (IOException e) {
                                // e.printStackTrace();
                            }
                        }
                        int monoLength = localBytes.length / 2;

                        byte[][] lbytes = SeoptUtil.splitStereoPcm(localBytes);
                        byte[] leftData = lbytes[0];
                        byte[] rightData = lbytes[1];
                        SeoptUtil.appendSrData(SeoptManager.getInstance().phISSSeopt,
                                srSession, leftData, rightData, monoLength);
                    } else {
                        cacheBuffer = null;
                    }
                    Thread.sleep(1408 / 128);
                }
            } catch (InterruptedException ex) {
                Log.e(TAG, "ex == " + ex);
            }
        }
    }

    /**
     * 取1024的整数倍，其他的留在下一次
     *
     * @param buffer
     * @return
     */
    public byte[] getSeoptByte(byte[] buffer) {
        byte[] srBuffer;
        if (cacheBuffer == null) {
            cacheBuffer = new byte[buffer.length];
            System.arraycopy(buffer, 0, cacheBuffer, 0, buffer.length);
        } else {
            byte[] localBuffer = new byte[cacheBuffer.length + buffer.length];
            System.arraycopy(cacheBuffer, 0, localBuffer, 0, cacheBuffer.length);
            System.arraycopy(buffer, 0, localBuffer, cacheBuffer.length, buffer.length);
            cacheBuffer = new byte[localBuffer.length];
            System.arraycopy(localBuffer, 0, cacheBuffer, 0, localBuffer.length);
        }
        int length = cacheBuffer.length % 1024;
        if (length != 0) {
            srBuffer = new byte[cacheBuffer.length - length];
            System.arraycopy(cacheBuffer, 0, srBuffer, 0, srBuffer.length);
            byte[] localBuffer = new byte[length];
            System.arraycopy(cacheBuffer, srBuffer.length, localBuffer, 0, length);
            cacheBuffer = new byte[length];
            System.arraycopy(localBuffer, 0, cacheBuffer, 0, length);
        } else {
            srBuffer = new byte[cacheBuffer.length];
            System.arraycopy(cacheBuffer, 0, srBuffer, 0, srBuffer.length);
            cacheBuffer = null;
        }
        return srBuffer;
    }


}
