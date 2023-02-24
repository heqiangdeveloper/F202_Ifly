package com.iflytek.cata;

public abstract interface CataIndexInst {
    public abstract int create(String paramString, ICataListener paramICataListener);

    public abstract int createEx(String paramString, int paramInt, ICataListener paramICataListener);

    public abstract int reCreate(String paramString, ICataListener paramICataListener);

    public abstract int reCreateEx(String paramString, int paramInt, ICataListener paramICataListener);

    public abstract int drop();

    public abstract int addIdxEntity(String paramString);

    public abstract int delIdxEntity(String paramString);

    public abstract int endIdxEntity();

    public abstract int destroy();
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\cata\CataIndexInst.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */