package com.chinatsp.ifly.video;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频数据获取的接口
 * Created by ytkj on 2018/5/18.
 */

public interface VideoDataModel {

    ArrayList<VideoModel> getVideoList(Context context, String sqlPath, boolean isFileMode);

    List<VideoModel> getCurrentDirVideo(Context context, String sqlPath);

//    int getVideoCount(Context context, String path);
}
