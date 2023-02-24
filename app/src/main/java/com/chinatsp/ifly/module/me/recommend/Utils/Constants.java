package com.chinatsp.ifly.module.me.recommend.Utils;

import android.net.Uri;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/8/11
 */

public class Constants {
    public static final String parent_path ="/storage/emulated/0/iflytek/ica/videocontent";   //暂时测试用这个路径，后续路径得修改,和视频联调
    public static final String AUTHORITY ="com.chinatsp.ifly.videodata";
    public static final Uri VIDEODATA_URL = Uri.parse("content://" + AUTHORITY + "/VideoData");

    public static final String video_content_path ="/system/media/video/videocontent";
    public static final String video_content_path2 ="/storage/emulated/0/iflytek/ica/videocontent";
    public static final String video_cover_content_path2 ="/storage/emulated/0/VoiceVideo/videocover";
}
