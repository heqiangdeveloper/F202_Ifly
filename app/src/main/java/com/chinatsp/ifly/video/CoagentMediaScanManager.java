package com.chinatsp.ifly.video;

/**
 * Created by ytkj on 2018/10/30.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class CoagentMediaScanManager {
    private Context mContext;
    private static String TAG = CoagentMediaScanManager.class.getName();
    private static CoagentMediaScanManager INSTANCE = new CoagentMediaScanManager();

    private CoagentMediaScanManager() {
    }

    public static CoagentMediaScanManager getMediaScanManager(Context context) {
        INSTANCE.mContext = context;
        return INSTANCE;
    }

    public int delDbDataByPath(String path) {
        path = this.replaceHBSChars(path);
        MjLog.i(TAG, "---delDbDataByPath>>>：" + path);
        int cnt = this.mContext.getContentResolver().delete(MediaDBConstants.FILES_URI, "path=?", new String[]{path});
        MjLog.i(TAG, "---delDbDataByPath>>> cnt：" + cnt);
        return cnt;
    }

    public void deleteAllFavorite() {
        MjLog.i(TAG, "---deleteAllFavorite");
        int cnt = this.mContext.getContentResolver().delete(MediaDBConstants.FAVORITE_URI, (String)null, (String[])null);
        MjLog.i(TAG, "---deleteAllFavorite>>> cnt：" + cnt);
    }

    private Uri getMediaUri(MediaConstantsDef.MEDIA_TYPE mediaType) {
        MjLog.i(TAG, "---getMediaUri>>> mediaType：" + mediaType);
        Uri uri = MediaDBConstants.MEDIA_URI;
        if(mediaType == MediaConstantsDef.MEDIA_TYPE.MUSIC) {
            uri = MediaDBConstants.AUDIO_URI;
        } else if(mediaType == MediaConstantsDef.MEDIA_TYPE.VIDEO) {
            uri = MediaDBConstants.VIDEO_URI;
        } else if(mediaType == MediaConstantsDef.MEDIA_TYPE.IMAGE) {
            uri = MediaDBConstants.IMAGE_URI;
        }

        MjLog.i(TAG, "---getMediaUri>>> uri：" + uri);
        return uri;
    }

    private String getPathLikeSelection() {
        return "path LIKE ?";
    }

    public Cursor getAllMedia(String path, MediaConstantsDef.MEDIA_TYPE mediaType) {
        path = this.replaceHBSChars(path);
        MjLog.i(TAG, "---getAllMedia>>> path：" + path + ",mediaType:" + mediaType);
        Uri mediaUri = this.getMediaUri(mediaType);
        String selection = this.getPathLikeSelection();
        String[] selectionArgs = new String[]{path + "%"};
        Cursor cursor = this.mContext.getContentResolver().query(mediaUri, MediaDBConstants.colums, selection, selectionArgs, (String)null);
        if(cursor == null) {
            MjLog.e(TAG, "---getAllMedia>>> path：" + path + ",mediaType:" + mediaType + ",cursor is NULL");
        } else {
            MjLog.e(TAG, "---getAllMedia>>> path：" + path + ",mediaType:" + mediaType + ",cnt: " + cursor.getCount());
        }

        return cursor;
    }



    public Cursor getFavoriteList(MediaConstantsDef.MEDIA_TYPE mediaType) {
        Uri uri = MediaDBConstants.MEDIA_URI;
        if(mediaType == MediaConstantsDef.MEDIA_TYPE.MUSIC) {
            uri = MediaDBConstants.AUDIO_URI;
        } else if(mediaType == MediaConstantsDef.MEDIA_TYPE.VIDEO) {
            uri = MediaDBConstants.VIDEO_URI;
        } else if(mediaType == MediaConstantsDef.MEDIA_TYPE.IMAGE) {
            uri = MediaDBConstants.IMAGE_URI;
        }

        Cursor cursor = this.mContext.getContentResolver().query(uri, (String[])null, "is_favorite=?", new String[]{"1"}, (String)null);
        if(cursor == null) {
            MjLog.e(TAG, "---getFavoriteList>>> cursor is NULL");
        } else {
            int cnt = cursor.getCount();
            MjLog.i(TAG, "---getFavoriteList>>> mediaType：" + mediaType + ",cnt:" + cnt);
        }

        return cursor;
    }

    public Cursor getID3CursorByPath(String path) {
        MjLog.e(TAG, "---getID3CursorByPath>>> path：" + path);
        if(TextUtils.isEmpty(path)) {
            MjLog.e(TAG, "---getID3CursorByPath>>> path is NULL");
            return null;
        } else {
            Cursor cursor = this.mContext.getContentResolver().query(MediaDBConstants.AUDIO_URI, MediaDBConstants.id3Colums, this.getPathLikeSelection(), new String[]{path + "%"}, (String)null);
            if(cursor == null) {
                MjLog.e(TAG, "---getID3CursorByPath>>> return cursor by path fail :" + path);
                return null;
            } else {
                int cnt = cursor.getCount();
                if(cnt > 0) {
                    cursor.moveToFirst();
                }

                MjLog.i(TAG, "---getID3CursorByPath>>> path :" + path + ",cnt:" + cnt);
                return cursor;
            }
        }
    }

    private String getTableViewName(MediaConstantsDef.MEDIA_TYPE mediaType) {
        String table = "media";
        if(mediaType == MediaConstantsDef.MEDIA_TYPE.MUSIC) {
            table = "audio";
        } else if(mediaType == MediaConstantsDef.MEDIA_TYPE.VIDEO) {
            table = "video";
        } else if(mediaType == MediaConstantsDef.MEDIA_TYPE.IMAGE) {
            table = "image";
        }

        return table;
    }

    public Cursor getMediaDirectorys(String path, MediaConstantsDef.MEDIA_TYPE mediaType) {
        path = this.replaceHBSChars(path);
        if(!path.endsWith("/")) {
            path = path + "/";
        }

        MjLog.i(TAG, "---getMediaDirectorys>>> path :" + path + ",mediaType:" + mediaType);
        String table = this.getTableViewName(mediaType);

        try {
            path = URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException var7) {
            var7.printStackTrace();
        }

        Uri dirUri = Uri.parse(MediaDBConstants.DIRECTORY_URI + "?table=" + table + "&path=" + path);
        MjLog.i(TAG, "---getMediaDirectorys>>> dirUri :" + dirUri);
        Cursor cursor = this.mContext.getContentResolver().query(dirUri, MediaDBConstants.colums, (String)null, (String[])null, (String)null);
        if(cursor == null) {
            MjLog.e(TAG, "---getMediaDirectorys>>> path :" + path + ",mediaType:" + mediaType + ",cursor is NULL");
        } else {
            int cnt = cursor.getCount();
            MjLog.i(TAG, "---getMediaDirectorys>>> path :" + path + ",mediaType:" + mediaType + ",cnt:" + cnt);
        }

        return cursor;
    }

    public Cursor getMediaFiles(String path, MediaConstantsDef.MEDIA_TYPE mediaType) {
        path = this.replaceHBSChars(path);
        MjLog.i(TAG, "---getMediaFiles>>> path :" + path);
        if(!path.endsWith("/")) {
            path = path + "/";
        }

        Uri mediaUri = this.getMediaUri(mediaType);
        Cursor cursor = this.mContext.getContentResolver().query(mediaUri, MediaDBConstants.colums, "parent_path = ?", new String[]{path}, (String)null);
        if(cursor == null) {
            MjLog.e(TAG, "---getMediaFiles>>> path :" + path + ",mediaType:" + mediaType + ",cursor is NULL");
        } else {
            int cnt = cursor.getCount();
            MjLog.i(TAG, "---getMediaFiles>>> path :" + path + ",mediaType:" + mediaType + ",cnt:" + cnt);
        }

        return cursor;
    }

    public Cursor getMediaList(String path, MediaConstantsDef.MEDIA_TYPE mediaType, boolean isFileMode) {
        MjLog.i(TAG, "---getMediaList>>> path :" + path + ",mediaType:" + mediaType + ",isFileMode:" + isFileMode);
        if(TextUtils.isEmpty(path)) {
            path = "/storage/udisk";
        }

        if(!path.endsWith("/")) {
            path = path + "/";
        }

        path = this.replaceHBSChars(path);
        MjLog.i(TAG, "---getMediaList next>>> path :" + path + ",mediaType:" + mediaType);
        Object cursor;
        if(isFileMode) {
            Cursor[] cursors = new Cursor[2];
            Cursor dirCursor = this.getMediaDirectorys(path, mediaType);
            cursors[0] = dirCursor;
            Cursor fileCursor = this.getMediaFiles(path, mediaType);
            cursors[1] = fileCursor;
            cursor = new MergeCursor(cursors);
        } else {
            cursor = this.getMediaFiles(path, mediaType);
        }

        if(cursor == null) {
            MjLog.e(TAG, "---getMediaList>>> path :" + path + ",mediaType:" + mediaType + ",cursor is NULL");
        } else {
            int cnt = ((Cursor)cursor).getCount();
            MjLog.i(TAG, "---getMediaList>>> path :" + path + ",mediaType:" + mediaType + ",cnt:" + cnt);
        }

        return (Cursor)cursor;
    }


    public MediaConstantsDef.SCAN_STATUS getScanStatus() {
        MjLog.i(TAG, "---getScanStatus>>> enter");
        Cursor cursor = null;

        MediaConstantsDef.SCAN_STATUS var3;
        try {
            cursor = this.mContext.getContentResolver().query(MediaDBConstants.SCAN_URI, (String[])null, (String)null, (String[])null, (String)null);
            String status;
            if(cursor == null || !cursor.moveToFirst()) {
                MjLog.e(TAG, "---getScanStatus>>> have no record");
                status = null;
                return null;
            }

            status = cursor.getString(0);
            MjLog.i(TAG, "---getScanStatus>>> status:" + status);
            var3 = MediaConstantsDef.SCAN_STATUS.valueOf(status);
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            cursor = null;
        }

        return var3;
    }

    public boolean isFavorite(String path) {
        path = this.replaceHBSChars(path);
        MjLog.i(TAG, "---isFavorite>>> path:" + path);
        boolean result = false;
        String selection = "path=?";
        String[] selectionArgs = new String[]{path};
        Cursor cursor = this.mContext.getContentResolver().query(MediaDBConstants.FAVORITE_URI, new String[]{"is_favorite"}, selection, selectionArgs, (String)null);

        try {
            if(cursor != null && cursor.moveToFirst()) {
                Integer is_favorite = Integer.valueOf(cursor.getInt(0));
                if(is_favorite.intValue() == 1) {
                    result = true;
                }

                MjLog.i(TAG, "---isFavorite>>> path:" + path + "," + "is_favorite" + ":" + is_favorite);
            }
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            cursor = null;
        }

        MjLog.i(TAG, "---isFavorite>>> path:" + path + ",result:" + result);
        return result;
    }

    public Cursor queryWithSQL(String sql) {
        sql = this.replaceHBSChars(sql);
        Cursor cursor = this.mContext.getContentResolver().query(MediaDBConstants.SQL_URI, (String[])null, sql, (String[])null, (String)null);
        if(cursor != null) {
            int var3 = cursor.getCount();
        }

        return cursor;
    }

    public void setFavorite(String path, boolean favorite) {
        path = this.replaceHBSChars(path);
        MjLog.i(TAG, "---setFavorite>>> path:" + path + ",favorite:" + favorite);
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("is_favorite", Boolean.valueOf(favorite));
        this.mContext.getContentResolver().insert(MediaDBConstants.FAVORITE_URI, values);
    }

    private String replaceHBSChars(String path) {
        return path;
    }
}
