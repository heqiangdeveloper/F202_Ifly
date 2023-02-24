package com.chinatsp.ifly.video;

/**
 * Created by ytkj on 2018/10/30.
 */

import android.net.Uri;

public class MediaDBConstants {
    public static final String AUTHORITY = "CoagentMedia";
    public static final String USB_ROOT = "/storage/udisk";
    public static final String HDD_ROOT = "/storage/emulated/0";
    private static final Uri CONTENT_URI = Uri.parse("content://CoagentMedia");
    public static final Uri AUDIO_URI;
    public static final Uri DIRECTORY_URI;
    public static final Uri FAVORITE_URI;
    public static final Uri FILES_URI;
    public static final Uri SCAN_URI;
    public static final Uri SQL_URI;
    public static final Uri VIDEO_URI;
    public static final Uri IMAGE_URI;
    public static final Uri MEDIA_URI;
    public static final String TABLE = "table";
    public static final String TABLE_FAVORITE = "favorite";
    public static final String TABLE_FILES = "files";
    public static final String VIEW_AUDIO = "audio";
    public static final String VIEW_IMAGE = "image";
    public static final String VIEW_MEDIA = "media";
    public static final String VIEW_VIDEO = "video";
    public static final String C_FILES_AUDIO_ALBUM = "album";
    public static final String C_FILES_AUDIO_ARTIST = "artist";
    public static final String C_FILES_AUDIO_DURATION = "duration";
    public static final String C_FILES_AUDIO_EXIST = "audio_exist";
    public static final String C_FILES_AUDIO_TITLE = "title";
    public static final String C_FILES_FILE_EXIST = "file_exist";
    public static final String C_FILES_ID3_EXIST = "id3_exist";
    public static final String C_FILES_IMAGE_EXIST = "image_exist";
    public static final String C_FILES_IS_DIRECTORY = "is_directory";
    public static final String C_FILES_MEDIA_TYPE = "media_type";
    public static final String C_FILES_MOD_TIME = "mod_time";
    public static final String C_FILES_NAME = "name";
    public static final String C_FILES_NAME_PY = "name_py";
    public static final String C_FILES_PARENT_PATH = "parent_path";
    public static final String C_FILES_PATH = "path";
    public static final String C_FILES_VIDEO_EXIST = "video_exist";
    public static final String C_ID = "_id";
    public static final String C_IS_FAVORITE = "is_favorite";
    public static final String[] colums;
    public static final String[] id3Colums;
    public static final String DIRECTORY = "is_directory";
    public static final String PATH = "path";
    public static final String SCAN = "scan";
    public static final String SQL = "RawSql";

    public MediaDBConstants() {
    }

    static {
        AUDIO_URI = Uri.withAppendedPath(CONTENT_URI, "audio");
        DIRECTORY_URI = Uri.withAppendedPath(CONTENT_URI, "directory");
        FAVORITE_URI = Uri.withAppendedPath(CONTENT_URI, "favorite");
        FILES_URI = Uri.withAppendedPath(CONTENT_URI, "files");
        SCAN_URI = Uri.withAppendedPath(CONTENT_URI, "scan");
        SQL_URI = Uri.withAppendedPath(CONTENT_URI, "RawSql");
        VIDEO_URI = Uri.withAppendedPath(CONTENT_URI, "video");
        IMAGE_URI = Uri.withAppendedPath(CONTENT_URI, "image");
        MEDIA_URI = Uri.withAppendedPath(CONTENT_URI, "media");
        colums = new String[]{"_id", "name", "path", "parent_path", "is_directory", "media_type", "is_favorite"};
        id3Colums = new String[]{"_id", "path", "title", "album", "artist", "duration"};
    }
}
