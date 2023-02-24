package com.iflytek.mvw;

public abstract interface IMvwListener {
    public abstract void onVwInited(boolean paramBoolean, int paramInt);

    public abstract void onVwWakeup(int paramInt1, int paramInt2, int paramInt3, String paramString);
}
