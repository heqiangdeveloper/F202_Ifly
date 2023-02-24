package com.chinatsp.ifly.activeservice.festival;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Carson_Ho on 17/6/6.
 */
public class DBHelper extends SQLiteOpenHelper {

    // 数据库名
    private static final String DATABASE_NAME = "finch.db";

    // 表名
    public static final String USER_TABLE_NAME = "festival";


    private static final int DATABASE_VERSION = 2;
    //数据库版本号

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 创建表格
        db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT, festival_time TEXT, festival_json TEXT, festival_text TEXT )");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)   {
        if (oldVersion <2){
            try {
                String sql = "Alter table "+USER_TABLE_NAME+" add column festival_time TEXT ";
                db.execSQL(sql);// 测试版本中存在了festival_time列，避免异常加入该异常捕获
            }catch (Exception e){
                Log.e("SQLiteOpenHelper","SQLiteOpenHelper Exception ");
            }

        }
    }
}
