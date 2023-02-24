package com.iflytek.adapter.ttsservice.aidl;

import com.iflytek.adapter.ttsservice.aidl.ITtsAgentListener;

interface TtsServiceAidl {
	boolean registerClient(in ITtsAgentListener client, int streamType);
	boolean releaseClient(in ITtsAgentListener client);
	int setParam(in ITtsAgentListener client, int id, int value);
	int startSpeak(in ITtsAgentListener client, String text);
	int pauseSpeak(in ITtsAgentListener client);
	int resumeSpeak(in ITtsAgentListener client);
	int stopSpeak(in ITtsAgentListener client);
}