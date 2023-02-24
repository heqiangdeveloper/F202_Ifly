package com.iflytek.cata;

public abstract interface CataSearchInst {
    public abstract int create(String paramString, ICataListener paramICataListener);

    public abstract int createEx(String paramString, int paramInt, ICataListener paramICataListener);

    public abstract String searchSync(String paramString);

    public abstract int searchAsync(String paramString);

    public abstract int destroy();

    public abstract int setParam(int paramInt1, int paramInt2);
}


/* Location:              C:\Users\ytkj\Desktop\speechsuitepack.jar!\com\iflytek\cata\CataSearchInst.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */