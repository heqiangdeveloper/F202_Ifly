package com.iflytek.speech;

import android.util.Log;

import com.iflytek.speech.mvw.IMVWListener;

public class libissmvw {
	private static final String tag = "libissmvw";
	private static libissmvw instance = null;

	static {
		System.loadLibrary("issauth");
		System.loadLibrary("issmvw");
	}

	public static native synchronized int create(NativeHandle nativeHandle,String resDir, IMVWListener iMvwListener);
    public static native synchronized int createEx(NativeHandle nativeHandle,String resDir, IMVWListener iMvwListener);

	public static native synchronized int destroy(NativeHandle nativeHandle);

	public static native synchronized int setThreshold(NativeHandle nativeHandle,int nMvwScene, int nMvwId,
			int threshold);
			
	public static native synchronized int setParam(NativeHandle nativeHandle,String szParam, String szParamValue);
	
	public static native synchronized int start(NativeHandle nativeHandle,int nMvwScene);
	public static native synchronized int addstartscene(NativeHandle nativeHandle,int nMvwScene);

	public static native synchronized int appendAudioData(NativeHandle nativeHandle,byte[] AudioBuffer,
			int nNumberOfByte);

	public static native synchronized int stop(NativeHandle nativeHandle);
	public static native synchronized int stopscene(NativeHandle nativeHandle,int nMvwScene);
	public static native synchronized int setMvwKeyWords(NativeHandle nativeHandle,int nMvwScene, String szWords);
	public static native synchronized int setMvwDefaultKeyWords(NativeHandle nativeHandle,int nMvwScene);
	public static native synchronized int setMvwLanguage(int nLangType);
	public static native synchronized boolean isCouldAppendAudioData();
    public static native synchronized int setIvwOptimizeUpload(NativeHandle nativeHandle, boolean bEnableFlag, int nMaxAudioCnt);
    public static native synchronized int setIvwOptimizeDownload(NativeHandle nativeHandle);
    public static native synchronized int vprRegStart(NativeHandle nativeHandle, int nMvwScene, int nVoiceId);
    public static native synchronized int vprRegAudioWrite(NativeHandle nativeHandle, byte[] audioBuffer, int nAudioLen, int nMvwScene, int nVoiceId);
    public static native synchronized int vprRegStop(NativeHandle nativeHandle, int nMvwScene, int nVoiceId);
    public static native synchronized int vprStart(NativeHandle nativeHandle, int nMvwScene, int nVoiceId);
    public static native synchronized int vprStop(NativeHandle nativeHandle, int nMvwScene, int nVoiceId);
	public static native synchronized int vprSetThreshold(NativeHandle nativeHandle, int nMvwScene, double jvprthreshold);
}
