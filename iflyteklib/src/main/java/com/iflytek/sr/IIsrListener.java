package com.iflytek.sr;

public abstract interface IIsrListener
{
  public abstract void onSrMsgProc(long paramLong1, long paramLong2, String paramString);
  
  public abstract void onSrInited(boolean paramBoolean, int paramInt);
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\sr\IIsrListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */