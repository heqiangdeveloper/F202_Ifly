package com.chinatsp.ifly.module.me.recommend.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/7/6
 */

public class DBHelperUtils extends SQLiteOpenHelper {
	private static String TABLE = "VideoData";

	public DBHelperUtils(Context context, String name, CursorFactory factory,
                         int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
//		arg0.execSQL("create table "
//				+ TABLE
//				+ "(_id Integer primary key autoincrement,read text(10), time text(20),pic_path text(30),path text(30),name text(30),parent_path text(50))");
		arg0.execSQL("create table "
				+ TABLE
				+ "(_id Integer ,read text(10), time text(20),pic_path text(30),path text(30),name text(30),parent_path text(50))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}


}
