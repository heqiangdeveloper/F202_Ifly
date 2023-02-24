package com.chinatsp.ifly.video;

/**
 * Created by ytkj on 2018/7/6.
 */

public interface MediaBean {

    long getId();

    int getType();

    String getName();

    String getPath();

    boolean isDir();

    long getDuration();

}
