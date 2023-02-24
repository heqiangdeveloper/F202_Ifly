package com.chinatsp.ifly;

import com.chinatsp.ifly.ISpeechControlListener;
import com.chinatsp.ifly.ISpeechTtsStopListener;
import com.chinatsp.ifly.aidlbean.CmdVoiceModel;
import com.chinatsp.ifly.aidlbean.NlpVoiceModel;
import com.chinatsp.ifly.ISpeechTtsResultListener;
import com.chinatsp.ifly.aidlbean.MutualVoiceModel;
interface ISpeechControlService {
    void registerSpeechListener(in int bussiness, ISpeechControlListener listener, in int key);
    void unregisterSpeechListener(in int key);
    int registerStksCommand(in String stksJson);
    int uploadAppStatus(in String statusJson);
    int uploadAppDict(in String dictJson);
    void waitMultiInterface(in String service,in String operation);
    void tts(in boolean showText, in String text, ISpeechTtsStopListener listener);
    void hideVoiceAssistant();
    boolean isHide();
    void dispatchSRAction(in int bussiness, in NlpVoiceModel nlpVoiceModel);
    void dispatchMvwAction(in int bussiness, in CmdVoiceModel cmdVoiceModel);
    void dispatchStksAction(in int bussiness, in CmdVoiceModel cmdVoiceModel);
    void dispatchMutualAction(in int bussiness, in MutualVoiceModel mutualVoiceModel);
    void onSearchWeChatContactListResult(in String resultJsonArray);
    void getMessageWithTtsSpeak(in boolean showText,in String conditionId,in String defaultTts);
    void getMessageWithTtsSpeakListener(in boolean showText,in String conditionId, in String defaultTts, ISpeechTtsStopListener listener);
    void getMessageWithoutTtsSpeak(in String conditionId, ISpeechTtsResultListener listener);
    String getCurrentCityName();
    void resetSrTimeOut(in String text);
    void stopTts();
    void releaseAudioFoucs(String pkg);
}
