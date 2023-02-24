package com.iflytek.speech.tts;

public abstract interface ITTSListener
{
  public abstract void onTTSPlayBegin();
  
  public abstract void onTTSPlayCompleted();
  
  public abstract void onTTSPlayInterrupted();
  
  public abstract void onTTSProgressReturn(int paramInt1, int paramInt2);
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\speech\tts\ITTSListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */