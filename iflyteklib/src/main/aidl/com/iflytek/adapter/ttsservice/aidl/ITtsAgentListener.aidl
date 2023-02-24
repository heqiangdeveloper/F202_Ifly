package com.iflytek.adapter.ttsservice.aidl;

import com.iflytek.adapter.ttsservice.aidl.TtsServiceAidl;

interface ITtsAgentListener {
	void onPlayBegin();
	void onPlayCompleted();
	void onPlayInterrupted();
	void onProgressReturn(int textIndex, int textLength);
	long getClientId();
	void onTtsInited(boolean state, int errId);
} 