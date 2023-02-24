package com.chinatsp.ifly.module.me.recommend.urlhttp;

import java.io.InputStream;

public class RealResponse {
    public InputStream inputStream;
    public InputStream errorStream;
    public int code;
    public long contentLength;
    public Exception exception;
}
