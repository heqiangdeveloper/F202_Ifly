package com.iflytek.adapter;

import com.iflytek.adapter.controllerInterface.IController;

public interface PlatformClientListener {
    String onNLPResult(String actionJson);

    String onDoMvwAction(String mvwJson);

    String onDoAction(String actionJson);

    String onStkAction(String actionJson);

    void onSrTimeOut(int srTimeCount);

    void onSrNoHandleTimeout(String exitMsg, String normalMsg);

    void onEngineException(long wParam);

    IController getCurController();

    void onSpeechStart();

    void onSpeechEnd();

    void onRecognizeStart();

    void onRecognizeEnd();

    void onRestoreMultiSemantic();

    boolean getTtsState();

    void upLoadDictToCloudStatus(long wParam,String param);

    void upLoadDictToLocalStatus(long wParam,String param);

    void onPgsAction(String pgsJson);
}