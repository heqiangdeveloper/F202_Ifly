package com.iflytek.speech.sr;

public abstract interface ISRService
{
  public abstract int setMachineCode(String paramString);

  public abstract int setSerialNumber(String paramString);

  public abstract int getActiveKey(String paramString);

  public abstract String create(String paramString, ISRListener paramISRListener);

  public abstract String createEx(int paramInt, String paramString, ISRListener paramISRListener);

  public abstract int sessionStart(String paramString1, String paramString2, int paramInt, String paramString3);

  public abstract int uploadDict(String paramString1, String paramString2, int paramInt);

  public abstract int uploadData(String paramString1, String paramString2, int paramInt);

  public abstract int setParam(String paramString1, String paramString2, String paramString3);

  public abstract int appendAudioData(String paramString, byte[] paramArrayOfByte, int paramInt);

  public abstract int endAudioData(String paramString);

  public abstract int sessionStop(String paramString);

  public abstract int destroy(String paramString);

  public abstract int resetSession(String paramString);

  public abstract String mspSearch(String paramString1, String paramString2, String paramString3);

  public abstract String localNli(String paramString1, String paramString2, String paramString3);
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\speech\sr\ISRService.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */