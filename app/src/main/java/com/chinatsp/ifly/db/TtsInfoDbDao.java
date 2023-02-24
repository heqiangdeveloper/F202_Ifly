package com.chinatsp.ifly.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.db.entity.GuideBook;
import com.chinatsp.ifly.db.entity.ProjectInfo;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;

import java.util.ArrayList;
import java.util.List;

public class TtsInfoDbDao {
    private static final String TAG = "TtsInfoDbDao";
    private static volatile TtsInfoDbDao mInstance;
    private CommonDbHelper mDbHelper;

    public static TtsInfoDbDao getInstance(Context context) {
        if (mInstance == null) {
            synchronized (TtsInfoDbDao.class) {
                if (mInstance == null) {
                    mInstance = new TtsInfoDbDao(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    private TtsInfoDbDao(Context context) {
        if (mDbHelper == null) {
            mDbHelper = new CommonDbHelper(context);
        }
    }

    public void deleteAllData() {
        try {
            mDbHelper.deleteTtsAllData(mDbHelper.getWritableDatabase());
        }catch (SQLiteException e){

        }
    }

    //tts文案入库,因为在插入数据之前已经删除了表中所有数据,所以获取到的数据直接插入.
    public void updateTtsDb(List<TtsInfo> ttsInfoList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues;
        try {
            for (TtsInfo ttsInfo : ttsInfoList) {
                contentValues = createContentValues(ttsInfo);
                Log.d(TAG, "lh:ttsInfo:" + ttsInfo.toString());
                long rows = db.insert(TtsTableInfo.TTS_INFO_TABLE_NAME, null, contentValues);
                Log.d(TAG, "lh:insert return rows: " + rows);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "lh:Exception" +e.getStackTrace());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    //判断数据库中是否有该条tts数据.
    public boolean isTtsExist(String conditionId) {
        boolean ret = false;
        Cursor cursor = null;
        try {
            cursor = mDbHelper.getReadableDatabase().query(TtsTableInfo.TTS_INFO_TABLE_NAME, null,
                    TtsTableInfo.TTSINFO_COLUMNS.CONDITION_ID + "=?", new String[]{conditionId},
                    null, null, null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                ret = true;
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "isPackageExistInDB Exception: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }

    /*
     * 查询特定功能的tts文案信息
     * conditionId是功能id
     */
    public List<TtsInfo> queryTtsInfo(String conditionId) {
        Cursor cursor = null;
        List<TtsInfo> ttsInfoList = null;
        try {
            cursor = mDbHelper.getReadableDatabase().query(TtsTableInfo.TTS_INFO_TABLE_NAME, null,
                    TtsTableInfo.TTSINFO_COLUMNS.CONDITION_ID + "=?", new String[]{conditionId},
                    null, null, null);
            if (cursor != null) {
                ttsInfoList = new ArrayList<>();
                if (cursor.moveToFirst()) {
                    do {
                        TtsInfo ttsInfo = new TtsInfo();
//                        ttsInfo.setNum(cursor.getInt(0));
                        ttsInfo.setSkillId(cursor.getString(1));
                        ttsInfo.setSkillVersion(cursor.getString(2));
                        ttsInfo.setConditionId(cursor.getString(3));
                        ttsInfo.setTtsId(cursor.getString(4));
                        ttsInfo.setTtsText(cursor.getString(5));
                        ttsInfoList.add(ttsInfo);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ttsInfoList;
    }

    private ContentValues createContentValues(TtsInfo ttsInfo) {
        ContentValues values = new ContentValues();
//        values.put(TtsTableInfo.TTSINFO_COLUMNS.NUM, ttsInfo.getNum());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.SKILL_ID, ttsInfo.getSkillId());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.SKILL_VERSION, ttsInfo.getSkillVersion());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.CONDITION_ID, ttsInfo.getConditionId());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.TTS_ID, ttsInfo.getTtsId());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.TTS_TEXT, ttsInfo.getTtsText());

        values.put(TtsTableInfo.TTSINFO_COLUMNS.TTS_AVAILABLE, ttsInfo.getIsTtsAvailable());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.TTS_BASE_RESPENSE, ttsInfo.getBaseResponse());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.TTS_START_TIME, ttsInfo.getValid_starttime());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.TTS_END_TIME, ttsInfo.getVelid_endtime());
        values.put(TtsTableInfo.TTSINFO_COLUMNS.TTS_OFFLINE, ttsInfo.getOffline_broadcast());
        return values;
    }

    public ContentValues createContentValues(ProjectInfo projectInfo) {
        ContentValues values = new ContentValues();
        values.put(projectInfo.getProjectId(), projectInfo.getProjectId());
        values.put(projectInfo.getCurrentVersion(), projectInfo.getCurrentVersion());
        return values;
    }

    public void deleteById(String conditionId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rows = db.delete(TtsTableInfo.TTS_INFO_TABLE_NAME, TtsTableInfo.TTSINFO_COLUMNS.CONDITION_ID + "=?", new String[]{conditionId});
        LogUtils.d(TAG, "deleteById rowid:" + rows);
    }

}
