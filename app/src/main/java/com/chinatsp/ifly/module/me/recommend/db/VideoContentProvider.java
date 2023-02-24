package com.chinatsp.ifly.module.me.recommend.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/8/10
 */

public class VideoContentProvider extends ContentProvider {
    private String table ="VideoData";
    private final static String TAG ="VideoContentProvider";

    private DBHelperUtils dbHelperUtils = null;
    private SQLiteDatabase db = null;
    @Override
    public boolean onCreate() {
        dbHelperUtils = VideoListUtil.getInstance(getContext()).helper;
        dbHelperUtils.getReadableDatabase();
        db = dbHelperUtils.getWritableDatabase();
        Log.d(TAG,"onCreate");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull  Uri uri, @Nullable  String[] projection, @Nullable  String selection, @Nullable  String[] selectionArgs, @Nullable String sortOrder) {
        Log.d(TAG,"query");
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(table);
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, null);
        Log.d(TAG,"cursor="+cursor.getCount());
        return cursor;
    }


    @Override
    public String getType(@NonNull  Uri uri) {
        return null;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable  ContentValues values) {
        Log.d(TAG,"insert");
        db.insert(table, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable  String selection, @Nullable String[] selectionArgs) {
        Log.d(TAG,"delete");
        int num= db.delete(table,selection,selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return num;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable  String[] selectionArgs) {
        Log.d(TAG,"update");
        int num = db.update(table, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG,"num="+num);
        return num ;
    }
}
