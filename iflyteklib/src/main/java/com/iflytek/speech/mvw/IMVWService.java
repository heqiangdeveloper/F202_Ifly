package com.iflytek.speech.mvw;

public abstract interface IMVWService
{
  public abstract int initMvw(String paramString, IMVWListener paramIMVWListener);
  
  public abstract int startMvw(int paramInt);
  
  public abstract int addStartMvwScene(int paramInt);
  
  public abstract int appendAudioData(byte[] paramArrayOfByte, int paramInt);
  
  public abstract int setThreshold(int paramInt1, int paramInt2, int paramInt3);
  
  public abstract int setParam(String paramString1, String paramString2);
  
  public abstract int stopMvw();
  
  public abstract int stopMvwScene(int paramInt);
  
  public abstract int releaseMvw();
  
  public abstract int setMvwKeyWords(int paramInt, String paramString);
  
  public abstract int setMvwDefaultKeyWords(int paramInt);
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\speech\mvw\IMVWService.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */