package com.iflytek.tts;

public abstract interface IPlayerListener
{
  public abstract void onPlayBegin();
  
  public abstract void onProgress(int paramInt1, int paramInt2);
  
  public abstract void onPlayInterrupted();
  
  public abstract void onFocusGain();
  
  public abstract void onPlayCompleted();
  
  public abstract void onError(int paramInt);
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\tts\IPlayerListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */