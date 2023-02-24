package com.iflytek.speech;

import com.iflytek.speech.NativeHandle;
import com.iflytek.speech.ITtsListener;

public class libisstts {
	static {
		//System.loadLibrary("ichip-jni");
		System.loadLibrary("itts-jni");
		System.loadLibrary("issauth");
		System.loadLibrary("hardinfo");
		System.loadLibrary("lesl");
	}
	public static native int initRes(String resDir, int mode);

	public static native int unInitRes();

	public static native void create(NativeHandle nativeHandle, ITtsListener iTtsListener);
	
	public static native void destroy(NativeHandle nativeHandle);

	public static native void setParam(NativeHandle nativeHandle, int param, int value);
	
	public static native void setParamEx(NativeHandle nativeHandle, int param, String strValue);

	public static native void start(NativeHandle nativeHandle, String text);

	public static native void getAudioData(NativeHandle nativeHandle, byte[] audioBuffer, int nBytes, int[] outBytes);

	public static native void stop(NativeHandle nativeHandle);
	
	public static native int setMachineCode(String code);
}
