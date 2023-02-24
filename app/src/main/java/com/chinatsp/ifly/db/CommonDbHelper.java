package com.chinatsp.ifly.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.chinatsp.ifly.utils.LogUtils;

public class CommonDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "CommonDbHelper";
    private static final String DATABASE_NAME = "common.db";
    private static final int VERSION = 4;
    private Context mContext;

    public CommonDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtils.d(TAG, "onCreate");
        createGuideBookTable(db);
        createTtsInfoTable(db);
        createCommandTable(db);
        createCommandBackUpTable(db);
    }

    public void createTtsInfoTable(SQLiteDatabase db) {
        String sql = "Create table IF NOT EXISTS " + TtsTableInfo.TTS_INFO_TABLE_NAME
                + "(" + TtsTableInfo.TTSINFO_COLUMNS.NUM + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TtsTableInfo.TTSINFO_COLUMNS.SKILL_ID + " TEXT, "
                + TtsTableInfo.TTSINFO_COLUMNS.SKILL_VERSION + " TEXT, "
                + TtsTableInfo.TTSINFO_COLUMNS.CONDITION_ID + " TEXT, "
                + TtsTableInfo.TTSINFO_COLUMNS.TTS_ID + " TEXT,"
                + TtsTableInfo.TTSINFO_COLUMNS.TTS_TEXT + " TEXT,"

                + TtsTableInfo.TTSINFO_COLUMNS.TTS_AVAILABLE + " INTEGER,"
                + TtsTableInfo.TTSINFO_COLUMNS.TTS_BASE_RESPENSE + " TEXT,"
                + TtsTableInfo.TTSINFO_COLUMNS.TTS_START_TIME + " TEXT,"
                + TtsTableInfo.TTSINFO_COLUMNS.TTS_END_TIME + " TEXT,"
                + TtsTableInfo.TTSINFO_COLUMNS.TTS_OFFLINE + " INTEGER"
                + ");";
        db.execSQL(sql);
        LogUtils.i(TAG, "lh:tts info table created");
    }

    public void deleteTtsAllData(SQLiteDatabase db) {
        String sql = "DELETE FROM " + TtsTableInfo.TTS_INFO_TABLE_NAME;
//        String updateSql = "update " +TtsTableInfo.TTS_INFO_TABLE_NAME+ " set num=0 where name=" +TtsTableInfo.TTS_INFO_TABLE_NAME;
        db.execSQL(sql);
//        db.execSQL(updateSql);
        LogUtils.i(TAG, "lh:delete all data");
    }

    public void deleteCommandAllData(SQLiteDatabase db) {
        String sql = "DELETE FROM " + CommandTableInfo.COMMAND_INFO_TABLE_NAME;
        db.execSQL(sql);
        LogUtils.i(TAG, "deleteCommandAllData:delete all data");
    }

    public void deleteBackUpCommandAllData(SQLiteDatabase db) {
        String sql = "DELETE FROM " + CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME;
        db.execSQL(sql);
        LogUtils.i(TAG, "deleteCommandAllData:delete all data");
    }

    private void createGuideBookTable(SQLiteDatabase db) {
        String sql = "Create table IF NOT EXISTS " + CommonContract.GUIDE_BOOK_TABLE_NAME
                + "(" + CommonContract.GUIDEBOOK_COLUMNS._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CommonContract.GUIDEBOOK_COLUMNS.SCENE + " TEXT, "
                + CommonContract.GUIDEBOOK_COLUMNS.PRIORITY + " TEXT, "
                + CommonContract.GUIDEBOOK_COLUMNS.COMMAND + " TEXT, "
                + CommonContract.GUIDEBOOK_COLUMNS.USAGE_COUNT + " LONG"
                + ");";
        db.execSQL(sql);
        LogUtils.i(TAG, "guide_book table created");
    }

    private void createCommandTable(SQLiteDatabase db) {
        String sql = "Create table IF NOT EXISTS " + CommandTableInfo.COMMAND_INFO_TABLE_NAME
                + "(" + CommandTableInfo.COMMAND_COLUMNS._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION_ID + " INTEGER, "
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_TYPE + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.ORDER + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.ISDISPLAY + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.INSTRUCTDESC + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.ITEM_ID + " INTEGER, "
                + CommandTableInfo.COMMAND_COLUMNS.ISRECOMMANDED + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_CONTENT + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_TEACH + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_URL + " TEXT ,"
                + CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_PATH + " TEXT "
                + ");";
        db.execSQL(sql);
        LogUtils.i(TAG, "COMMAND_INFO_TABLE_NAME table created");
    }

    private void createCommandBackUpTable(SQLiteDatabase db) {
        String sql = "Create table IF NOT EXISTS " + CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME
                + "(" + CommandTableInfo.COMMAND_COLUMNS._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION_ID + " INTEGER, "
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.MODULE_TYPE + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.ORDER + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.ISDISPLAY + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.INSTRUCTDESC + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.ITEM_ID + " INTEGER, "
                + CommandTableInfo.COMMAND_COLUMNS.ISRECOMMANDED + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_CONTENT + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_TEACH + " TEXT, "
                + CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_URL + " TEXT ,"
                + CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_PATH + " TEXT "
                + ");";
        db.execSQL(sql);
        LogUtils.i(TAG, "COMMAND_BACK_INFO_TABLE_NAME table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + TtsTableInfo.TTS_INFO_TABLE_NAME);
        createTtsInfoTable(db);
        createCommandTable(db);
        createCommandBackUpTable(db);
        Log.d(TAG, "onUpgrade() called with: db = [" + db + "], oldVersion = [" + oldVersion + "], newVersion = [" + newVersion + "]");
    }

}
