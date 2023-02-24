package com.iflytek.speech.tts;

public abstract interface TtsPlayerInst
{
  public abstract int sessionBegin(int paramInt);
  
  public abstract int setParam(int paramInt1, int paramInt2);
  
  public abstract int setParamEx(int paramInt, String paramString);
  
  public abstract int startSpeak(String paramString, ITTSListener paramITTSListener);
  
  public abstract int pauseSpeak();
  
  public abstract int resumeSpeak();
  
  public abstract int stopSpeak();
  
  public abstract int sessionStop();
  
  public abstract int sessionInitState();
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\speech\tts\TtsPlayerInst.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */