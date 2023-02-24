package com.iflytek.tts;

public abstract interface ITtsListener
{
  public abstract void onPlayBegin();
  
  public abstract void onPlayCompleted();
  
  public abstract void onPlayInterrupted();
  
  public abstract void onProgressReturn(int paramInt1, int paramInt2);
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\tts\ITtsListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */