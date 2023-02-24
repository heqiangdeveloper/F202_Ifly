//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.iflytek.speech.tts;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Environment;
import android.util.Log;
import com.iflytek.speech.ITtsListener;
import com.iflytek.speech.NativeHandle;
import com.iflytek.speech.libisstts;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TtsPlayer implements ITtsListener {
    private static int cnt = 0;
    private static int iInitState = -1;
    private String tag;
    private static final int mSampleRateInHz = 16000;
    private static final int mChannelConfig = 4;
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private final Lock mAudioTrackLock;
    private AudioTrack mAudioTrack;
    private int mMinBufferSizeInBytes;
    private ITTSListener mITTSListener;
    private int nPreTextIndex;
    private boolean mOnDataReadyFlag;
    private final NativeHandle mNativeHandle;
    private int mWriteBytes;
    private int mAudioTrackSteamState;
    private final Object mWorkingThreadSyncObj;
    private final TtsPlayer.AudioWriteWorkingFunc mAudioWriteWorkingFunc;
    private Thread mThreadAudioWrite;

    private static final String RES_DIR;

    static {
        RES_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "iflytek" + File.separator + "res" + File.separator + "tts";
    }

    private static class HolderClass {
        private static final TtsPlayer instance = new TtsPlayer(RES_DIR);

        private HolderClass() {
        }
    }

    public static TtsPlayer getInstance() {
        return TtsPlayer.HolderClass.instance;
    }


    private TtsPlayer(String strResDir) {
        this.tag = "xyj_TtsPlayer_" + cnt;
        this.mAudioTrackLock = new ReentrantLock();
        this.mAudioTrack = null;
        this.mMinBufferSizeInBytes = 0;
        this.mITTSListener = null;
        this.nPreTextIndex = -1;
        this.mOnDataReadyFlag = false;
        this.mNativeHandle = new NativeHandle();
        this.mWriteBytes = 0;
        this.mAudioTrackSteamState = 0;
        this.mWorkingThreadSyncObj = new Object();
        this.mAudioWriteWorkingFunc = new TtsPlayer.AudioWriteWorkingFunc();
        this.mThreadAudioWrite = null;
        Log.d(this.tag, "new TtsPlayer");
        Log.d(this.tag, "initRes start");
        iInitState = libisstts.initRes(strResDir, 0);
        Log.d(this.tag, "initRes end, ret is " + iInitState);
    }

    public void setListener(ITTSListener iTTSListener) {
        this.mITTSListener = iTTSListener;
    }

    public int Init(int streamType) {
        ++cnt;
        Log.d(this.tag, "Init");

        try {
            libisstts.destroy(this.mNativeHandle);
            libisstts.create(this.mNativeHandle, this);
            if (this.mNativeHandle.err_ret != 0) {
                return this.mNativeHandle.err_ret;
            } else {
                this.mMinBufferSizeInBytes = AudioTrack.getMinBufferSize(16000, 4, 2);
                Log.d(this.tag, "mMinBufferSizeInBytes=" + this.mMinBufferSizeInBytes + ".");
                if (this.mMinBufferSizeInBytes <= 0) {
                    Log.e(this.tag, "Error: AudioTrack.getMinBufferSize(16000, 4, 2) ret " + this.mMinBufferSizeInBytes);
                    return 10106;
                } else {
                    if (this.mAudioTrack == null) {
//                        int usage;
//                        int contentType;
//                        if (AudioManager.STREAM_ALARM == streamType) {
//                            usage = AudioAttributes.USAGE_ALARM;
//                            contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
//                        } else if (AudioManager.STREAM_MUSIC == streamType) {
//                            usage = AudioAttributes.USAGE_MEDIA;
//                            contentType = AudioAttributes.CONTENT_TYPE_MUSIC;
//                        } else {
//                            usage = AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY;
//                            contentType = AudioAttributes.CONTENT_TYPE_SPEECH;
//                        }
//
//                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                                .setUsage(usage)
//                                .setContentType(contentType)
//                                .build();
//                        AudioFormat audioFormat = new AudioFormat.Builder()
//                                .setSampleRate(mSampleRateInHz)
//                                .setEncoding(mAudioFormat)
//                                .setChannelMask(mChannelConfig)
//                                .build();
//                        this.mAudioTrack = new AudioTrack(audioAttributes, audioFormat, this.mMinBufferSizeInBytes * 3,
//                                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);

                        this.mAudioTrack = new AudioTrack(streamType, 16000, 4, 2, this.mMinBufferSizeInBytes * 3, 1);
                        if (this.mAudioTrack.getState() != 1) {
                            Log.e(this.tag, "Error: Can't init AudioRecord!");
                            return -1;
                        }

                        Log.d(this.tag, "new AudioTrack(streamType=" + streamType + ")");
                        this.mAudioTrack.setPlaybackPositionUpdateListener(new OnPlaybackPositionUpdateListener() {
                            public void onMarkerReached(AudioTrack track) {
                                if (TtsPlayer.this.mITTSListener != null && TtsPlayer.this.mAudioTrackSteamState == 0) {
                                    Log.d(TtsPlayer.this.tag, "mITTSListener.onTTSPlayCompleted() 2");
                                    TtsPlayer.this.mAudioTrackLock.lock();
                                    TtsPlayer.this.mAudioTrack.stop();
                                    TtsPlayer.this.mAudioTrack.flush();
                                    TtsPlayer.this.mAudioTrack.setNotificationMarkerPosition(0);
                                    TtsPlayer.this.mAudioTrackLock.unlock();
                                    TtsPlayer.this.mITTSListener.onTTSPlayCompleted();
                                } else {
                                    if (TtsPlayer.this.mITTSListener == null) {
                                        Log.d(TtsPlayer.this.tag, "mITTSListener is null!!!!!!");
                                    } else {
                                        Log.d(TtsPlayer.this.tag, "mAudioTrackSteamState is " + TtsPlayer.this.mAudioTrackSteamState + ",not AudioTrackSteamState.STREAM_STOPPED = 0");
                                    }

                                    Log.d(TtsPlayer.this.tag, "mITTSListener.onTTSPlayCompleted() faile.");
                                }

                            }

                            public void onPeriodicNotification(AudioTrack track) {
                            }
                        });
                        this.mAudioTrack.setNotificationMarkerPosition(0);
                    }

                    this.mAudioTrackSteamState = 0;
                    if (this.mThreadAudioWrite == null) {
                        this.mAudioWriteWorkingFunc.clearExitFlag();
                        this.mThreadAudioWrite = new Thread(this.mAudioWriteWorkingFunc, "mThreadAudioWrite");
                        this.mThreadAudioWrite.start();
                    }

                    return 0;
                }
            }
        } catch (IllegalArgumentException var3) {
            return 10106;
        }
    }

    public int SetParam(int nParam, int nValue) {
        Log.d(this.tag, "SetParam");
        if (this.mNativeHandle.native_point == 0L) {
            return 10000;
        } else {
            libisstts.setParam(this.mNativeHandle, nParam, nValue);
            return this.mNativeHandle.err_ret;
        }
    }

    public int SetParamEx(int nParam, String StrValue) {
        Log.d(this.tag, "SetParamEx");
        if (this.mNativeHandle.native_point == 0L) {
            return 10000;
        } else {
            libisstts.setParamEx(this.mNativeHandle, nParam, StrValue);
            return this.mNativeHandle.err_ret;
        }
    }

    public int Start(String text) {
        Log.d(this.tag, "Start");
        if (this.mAudioTrack != null && this.mThreadAudioWrite != null && this.mNativeHandle.native_point != 0L) {
            this.Stop();
            if (this.mAudioTrackSteamState == 3) {
                return 10000;
            } else {
                this.nPreTextIndex = -1;
                this.mOnDataReadyFlag = false;
                this.mWriteBytes = 0;
                Log.d(this.tag, "start text : " + text);
                libisstts.start(this.mNativeHandle, text);
                if (this.mNativeHandle.err_ret != 0) {
                    return this.mNativeHandle.err_ret;
                } else {
                    this.mAudioTrackSteamState = 1;
                    this.mAudioTrackLock.lock();
                    if (this.mAudioTrack != null) {
                        this.mAudioTrack.setNotificationMarkerPosition(0);
                        this.mAudioTrack.play();
                        Log.d(tag, "mAudioTrack:start play ");
                    }

                    this.mAudioTrackLock.unlock();
                    Object var2 = this.mWorkingThreadSyncObj;
                    synchronized(this.mWorkingThreadSyncObj) {
                        this.mWorkingThreadSyncObj.notifyAll();
                        return 0;
                    }
                }
            }
        } else {
            return 10000;
        }
    }

    public int Pause() {
        Log.d(this.tag, "Pause");
        if (this.mAudioTrack != null && this.mThreadAudioWrite != null && this.mNativeHandle.native_point != 0L) {
            if (this.mAudioTrackSteamState == 3) {
                return 10000;
            } else if (this.mAudioTrackSteamState == 0) {
                return 10000;
            } else if (this.mAudioTrackSteamState == 2) {
                return 0;
            } else {
                this.mAudioTrackSteamState = 2;
                this.mAudioTrackLock.lock();
                if (this.mAudioTrack != null) {
                    this.mAudioTrack.pause();
                }

                this.mAudioTrackLock.unlock();
                Object var1 = this.mWorkingThreadSyncObj;
                synchronized(this.mWorkingThreadSyncObj) {
                    this.mWorkingThreadSyncObj.notifyAll();
                    return 0;
                }
            }
        } else {
            return 10000;
        }
    }

    public int Resume() {
        Log.d(this.tag, "Resume");
        if (this.mAudioTrack != null && this.mThreadAudioWrite != null && this.mNativeHandle.native_point != 0L) {
            if (this.mAudioTrackSteamState == 3) {
                return 10000;
            } else if (this.mAudioTrackSteamState == 0) {
                return 10000;
            } else if (this.mAudioTrackSteamState == 1) {
                return 0;
            } else {
                this.mAudioTrackSteamState = 1;
                this.mAudioTrackLock.lock();
                if (this.mAudioTrack != null) {
                    this.mAudioTrack.play();
                }

                this.mAudioTrackLock.unlock();
                Object var1 = this.mWorkingThreadSyncObj;
                synchronized(this.mWorkingThreadSyncObj) {
                    this.mWorkingThreadSyncObj.notifyAll();
                    return 0;
                }
            }
        } else {
            return 10000;
        }
    }

    public int Stop() {
        Log.d(this.tag, "Stop");
        if (this.mAudioTrack != null && this.mThreadAudioWrite != null && this.mNativeHandle.native_point != 0L) {
            if (this.mAudioTrackSteamState == 3) {
                return 10000;
            } else if (this.mAudioTrackSteamState == 0) {
                return 0;
            } else {
                this.mAudioTrackSteamState = 0;
                libisstts.stop(this.mNativeHandle);
                this.mAudioTrackLock.lock();
                if (this.mAudioTrack != null) {
                    this.mAudioTrack.stop();
                    this.mAudioTrack.flush();
                }

                this.mAudioTrackLock.unlock();
                Object var1 = this.mWorkingThreadSyncObj;
                synchronized(this.mWorkingThreadSyncObj) {
                    this.mWorkingThreadSyncObj.notifyAll();
                }

                if (this.mITTSListener != null) {
                    this.mITTSListener.onTTSPlayInterrupted();
                }

                return 0;
            }
        } else {
            return 10000;
        }
    }

    public int Release() {
        Log.d(this.tag, "Release");
        this.mAudioTrackSteamState = 3;
        this.mAudioWriteWorkingFunc.setExitFlag();
        if (this.mThreadAudioWrite != null) {
            try {
                this.mThreadAudioWrite.join();
                this.mThreadAudioWrite = null;
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }
        }

        libisstts.destroy(this.mNativeHandle);
        return 0;
    }

    public int GetInitState() {
        return iInitState;
    }

    public void onDataReady() {
        Log.d(this.tag, "onDataReady");
        this.mWriteBytes = 0;
        this.mOnDataReadyFlag = true;
        if (this.mITTSListener != null) {
            this.mITTSListener.onTTSPlayBegin();
        }

        Object var1 = this.mWorkingThreadSyncObj;
        synchronized(this.mWorkingThreadSyncObj) {
            this.mWorkingThreadSyncObj.notifyAll();
        }
    }

    public void onProgress(int nTextIndex, int nTextLen) {
        if (this.nPreTextIndex < nTextIndex) {
            Log.d(this.tag, "onProgress(" + nTextIndex + ", " + nTextLen + ")");
            if (this.mITTSListener != null) {
                this.mITTSListener.onTTSProgressReturn(nTextIndex, nTextLen);
            }
        }

        this.nPreTextIndex = nTextIndex;
    }

    private class AudioWriteWorkingFunc implements Runnable {
        private boolean mExitFlag;

        private AudioWriteWorkingFunc() {
            this.mExitFlag = false;
        }

        public void clearExitFlag() {
            this.mExitFlag = false;
        }

        public void setExitFlag() {
            this.mExitFlag = true;
            synchronized(TtsPlayer.this.mWorkingThreadSyncObj) {
                TtsPlayer.this.mWorkingThreadSyncObj.notifyAll();
            }
        }

        public void run() {
            Log.d(tag, "AudioWriteWorkingFunc In.");
            if (TtsPlayer.this.mAudioTrack != null && TtsPlayer.this.mNativeHandle.native_point != 0L && TtsPlayer.this.mMinBufferSizeInBytes != 0) {
                byte[] buffer = new byte[TtsPlayer.this.mMinBufferSizeInBytes];
                Log.d(tag, "mBufferOnceSizeInBytes is " + TtsPlayer.this.mMinBufferSizeInBytes);

                label76:
                while(true) {
                    while(true) {
                        if (this.mExitFlag) {
                            break label76;
                        }

                        if (TtsPlayer.this.mAudioTrackSteamState == 1 && TtsPlayer.this.mOnDataReadyFlag) {
                            int[] buffer_size = new int[1];
                            libisstts.getAudioData(TtsPlayer.this.mNativeHandle, buffer, TtsPlayer.this.mMinBufferSizeInBytes, buffer_size);
                            int ret;
                            if (TtsPlayer.this.mNativeHandle.err_ret == 10004) {
                                Log.d(tag, "libisstts.getAudioData Completed.");
                                TtsPlayer.this.mAudioTrackSteamState = 0;
                                if (TtsPlayer.this.mITTSListener != null) {
                                    TtsPlayer.this.mAudioTrackLock.lock();
                                    ret = TtsPlayer.this.mWriteBytes / 2;
                                    Log.d(tag, "setNotificationMarkerPosition " + ret);
                                    TtsPlayer.this.mAudioTrack.setNotificationMarkerPosition(ret);
                                    TtsPlayer.this.mAudioTrackLock.unlock();
                                }
                            } else if (buffer_size[0] > 0) {
                                TtsPlayer.this.mAudioTrackLock.lock();
                                ret = TtsPlayer.this.mAudioTrack.write(buffer, 0, buffer_size[0]);
                                Log.d(tag, "run: write::"+ret);
                                TtsPlayer.this.mAudioTrackLock.unlock();
                                if (ret < 0) {
                                    Log.e(tag, "mAudioTrack.write(size=" + buffer_size[0] + ") ret " + ret);
                                    TtsPlayer.this.mAudioTrackSteamState = 0;
                                    Thread.yield();
                                } else {
                                    TtsPlayer.this.mWriteBytes = TtsPlayer.this.mWriteBytes + ret;
                                }
                            } else {
                                synchronized(TtsPlayer.this.mWorkingThreadSyncObj) {
                                    try {
                                        Log.d(tag, "Before wait(5)");
                                        TtsPlayer.this.mWorkingThreadSyncObj.wait(5L);
                                        Log.d(tag, "After wait(5)");
                                    } catch (InterruptedException var8) {
                                        var8.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            synchronized(TtsPlayer.this.mWorkingThreadSyncObj) {
                                if (TtsPlayer.this.mAudioTrackSteamState != 1 || !TtsPlayer.this.mOnDataReadyFlag) {
                                    try {
                                        TtsPlayer.this.mWorkingThreadSyncObj.wait(5L);
                                    } catch (InterruptedException var7) {
                                        var7.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Log.e(tag, "mAudioTrack==null || mNativeHandle.native_point == 0 || mMinBufferSizeInBytes==0, this should never happen.");
            }

            TtsPlayer.this.mAudioTrackLock.lock();
            if (TtsPlayer.this.mAudioTrack != null) {
                TtsPlayer.this.mAudioTrack.release();
                TtsPlayer.this.mAudioTrack = null;
            }

            TtsPlayer.this.mAudioTrackLock.unlock();
            Log.d(tag, "AudioWriteWorkingFunc Out.");
        }
    }

    private class AudioTrackSteamState {
        public static final int STREAM_STOPPED = 0;
        public static final int STREAM_RUNNING = 1;
        public static final int STREAM_PAUSED = 2;
        public static final int STREAM_RELEASED = 3;

        private AudioTrackSteamState() {
        }
    }
}
