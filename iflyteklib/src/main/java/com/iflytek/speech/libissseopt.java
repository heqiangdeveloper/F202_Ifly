package com.iflytek.speech;

import android.util.Log;

public class libissseopt {
	private static final String tag = "libissseopt";
	private static libissseopt instance = null;

	static {
		System.loadLibrary("issse");
		System.loadLibrary("issauth");
		System.loadLibrary("lesl");
		System.loadLibrary("hardinfo");
	}

	final public static int ISS_SEOPT_FRAME_SHIFT 				= 256;					///< samples of seopt processing once
	
	final public static int ISS_SEOPT_INPUT_CHANNELS			= 4;					///< processing input audio channels, when use XF6010SYE
	final public static int ISS_SEOPT_INPUT_CHANNELS_HARDWARD	= 2;					///< processing input audio channels

	final public static String ISS_SEOPT_PARAM_BEAM_INDEX				= "beam_index";			///< param beamIndex id
	final public static String ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_INIT	= "-1";
	final public static String ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_LEFT	= "0";					///< param beamIndex value 0, means output the left channel audio
	final public static String ISS_SEOPT_PARAM_BEAM_INDEX_VALUE_RIGTHT	= "2";					///< param beamIndex value 2, means output the right channel audio
	

	//该模式是使用XF6010SYE降噪模式时用的，不对外开放配置
	final public static String ISS_SEOPT_PARAM_WORK_MODE				= "work_mode";	///< param work_mode id
	final public static String ISS_SEOPT_PARAM_WORK_MODE_VALUE_MAB_VAD_ONLY	="0";			///< param work_mode value 0, vad out from two beams, for XF6010SYE
	final public static String ISS_SEOPT_PARAM_WORK_MODE_VALUE_MAB         	= "1";			///< param work_mode value 1, means mab mode on and mae mode off
	final public static String ISS_SEOPT_PARAM_WORK_MODE_VALUE_MAE         	= "2";			///< param work_mode value 2, means mab mode off and mae mode on
	final public static String ISS_SEOPT_PARAM_WORK_MODE_VALUE_MAB_AND_MAE 	= "3";			///< param work_mode value 3, means mab mode on and mae mode off
	final public static String ISS_SEOPT_PARAM_VALUE_MAE_FAKE 				= "4";			///< param work_mode value 4, mae fake mode, for XF6010SYE
	
	// 以下为套件3.5所用常量名，请尽快切换为以下常量名
	final public static String ISS_SEOPT_PARAM_VALUE_MAB_VAD_ONLY_MODE	="0";			///< param work_mode value 0, vad out from two beams, for XF6010SYE
	final public static String ISS_SEOPT_PARAM_VALUE_MAB_MODE         	= "1";			///< param work_mode value 1, means mab mode on and mae mode off
	final public static String ISS_SEOPT_PARAM_VALUE_MAE_MODE         	= "2";			///< param work_mode value 2, means mab mode off and mae mode on
	final public static String ISS_SEOPT_PARAM_VALUE_MAB_AND_MAE_MODE 	= "3";			///< param work_mode value 3, means mab mode on and mae mode off
	
	
	final public static String ISS_SEOPT_PARAM_RESOLUTION_TYPE 	= "resolution_type";		///< sampling digit of audio
	final public static String ISS_SEOPT_PARAM_RESOLUTION_TYPE_VALUE_16BIT 	= "0";		///< sampling digit of audio
	final public static String ISS_SEOPT_PARAM_RESOLUTION_TYPE_VALUE_32BIT 	= "1";		///< sampling digit of audio
	
	
	final public static String ISS_SEOPT_PARAM_GAIN				= "gain";
	final public static String ISS_SEOPT_PARAM_MODULATED		= "modulated";	
	final public static String  ISS_SEOPT_PARAM_SAMPLING_DIGIT	= "sampling_digit";	///< sampling digit of audio
	
	final public static String  ISS_SEOPT_PARAM_MODULATED_TYPE_OFF	= "0";
	final public static String  ISS_SEOPT_PARAM_MODULATED_TYPE_HARD	= "1";
	final public static String  ISS_SEOPT_PARAM_MODULATED_TYPE_SOFT	= "2";

	final public static String  ISS_SEOPT_PARAM_VALUE_ON			= "on";                ///< On
	final public static String  ISS_SEOPT_PARAM_VALUE_OFF			= "off"; 
	
	
	public static native int create(NativeHandle nativeHandle, String resPath);
	
	public static native int destroy(NativeHandle nativeHandle);
	
	public static native int process(NativeHandle nativeHandle, byte[] bufIn, int samplesIn, byte[] bufOut, int[] paramOut);
	
	public static native int setParam(NativeHandle nativeHandle, String param, String szParamValue);

	public static native int getGSCPower(NativeHandle nativeHandle, int index, float[] paramOut);
	
	public static native int demodulate(NativeHandle nativeHandle, byte[] bufIn, int samplesIn, byte[] bufOut, int[] paramOut);
	
	public static native int demodulateEx(NativeHandle nativeHandle, byte[] bufIn, int samplesIn, byte[] bufOutL, byte[] bufOutH, int[] paramOut);
	
}
