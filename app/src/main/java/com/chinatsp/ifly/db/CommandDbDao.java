package com.chinatsp.ifly.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.db.entity.CommandInfo;
import com.chinatsp.ifly.db.entity.ProjectInfo;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.module.me.recommend.bean.HuVoiceAssitContentBean;
import com.chinatsp.ifly.module.me.recommend.model.HuVoiceAsssitContentModel;
import com.chinatsp.ifly.utils.ImageUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.ThreadPoolUtils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CommandDbDao {
    private static final String TAG = "CommandDbDao";
    private static volatile CommandDbDao mInstance;
    private CommonDbHelper mDbHelper;
    private Context mContext;
    private int index = 1;
    private String instructPaht = Environment.getExternalStorageDirectory().getAbsolutePath() + "/iflytek/ica/instruct/instruct_init.json";
    private  String instructPaht2 = "/data/navi/0/iflytek/ica/instruct/instruct_init.json";
    private String instructRealPath ="";
    public static CommandDbDao getInstance(Context context) {
        if (mInstance == null) {
            synchronized (CommandDbDao.class) {
                if (mInstance == null) {
                    mInstance = new CommandDbDao(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    private CommandDbDao(Context context) {
        if (mDbHelper == null) {
            mDbHelper = new CommonDbHelper(context);
        }
        mContext = context;
    }

    public void initBackUp(){
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                preSetCommandDb();
            }
        });
    }

    public void deleteAllData() {
        try {
            mDbHelper.deleteCommandAllData(mDbHelper.getWritableDatabase());
        }catch (SQLiteException e){

        }
    }

    public void updateCommandDb(List<HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean> infoList, HuVoiceAsssitContentModel.onCommandCallbackInterface l) {
        Log.d(TAG, "updateCommandDb: ");

        List<CommandInfo> commands = changeBean(infoList);
        index = 1;
        Observable.fromIterable(commands).map(new Function<CommandInfo, Integer>() {
            @Override
            public Integer apply(CommandInfo info) throws Exception {
                AppConstant.DOWNLOAD_FINISEH = false;
                String iconPath = ImageUtils.getInstance(BaseApplication.getInstance().getApplicationContext()).getCommandIcon(info);
                info.setIconPath(iconPath);
                return index++;
            }

        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer info) throws Exception {
                        if(info==commands.size()){
                            Log.d(TAG, "accept() called with: info = begin add data to dababase["  + "]");
                            CommandProvider.getInstance(mContext).deleteCommandData();
                            updateTtsDb(commands);
                            AppConstant.DOWNLOAD_FINISEH = true;
                            if(l!=null)
                                l.onCommandDownloadStatus(true);
                        }
                    }

                });

    }


    public List<String> queryModules(){
        Cursor cursor = null;
        List<String> commandTypes = new ArrayList<>();
        String table = CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME;
        if(AppConstant.DOWNLOAD_FINISEH)
            table = CommandTableInfo.COMMAND_INFO_TABLE_NAME;
        Log.d(TAG, "queryModules() called::"+table+"...AppConstant.DOWNLOAD_FINISEH:"+AppConstant.DOWNLOAD_FINISEH);
        try {
            cursor = mDbHelper.getReadableDatabase().query(true,table, new String[]{CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME},
                    null,null,
                    CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME, null, null,null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Log.d(TAG, "queryModules: "+cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME)));
                        commandTypes.add(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME)));
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
       return commandTypes;
    }


    public List<String> queryModelSkills(String conditionId) {

        Cursor cursor = null;
        String table = CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME;
        if(AppConstant.DOWNLOAD_FINISEH)
            table = CommandTableInfo.COMMAND_INFO_TABLE_NAME;
        List<String> infos = new ArrayList<>();
        Log.d(TAG, "queryModelSkills() called with: conditionId = [" + conditionId + "]"+table+"...AppConstant.DOWNLOAD_FINISEH:"+AppConstant.DOWNLOAD_FINISEH);
        try {
            cursor = mDbHelper.getReadableDatabase().query(true,table, new String[]{CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME},
                    CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME + "=?", new String[]{conditionId},
                    null, null, null,null);
            while (cursor != null&&cursor.moveToNext()) {
                Log.d(TAG, "queryModelSkills: "+cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME)));
                infos.add(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }


    /*
     * 查询特定功能的tts文案信息
     * conditionId是功能id
     */
    public ArrayList<CommandInfo> queryModelSkillContents(String model,String conditionId) {

        Cursor cursor = null;
        ArrayList<CommandInfo> infos = new ArrayList<>();
        String table = CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME;
        if(AppConstant.DOWNLOAD_FINISEH)
            table = CommandTableInfo.COMMAND_INFO_TABLE_NAME;
        Log.d(TAG, "queryModelSkillContents() called with: conditionId = [" + conditionId + "]"+table+"...AppConstant.DOWNLOAD_FINISEH:"+AppConstant.DOWNLOAD_FINISEH);
        try {
            cursor = mDbHelper.getReadableDatabase().query(table, null,
                    CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME + "=? and "+CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME +"=?", new String[]{conditionId,model},
                    null, null, null,null);
            while (cursor != null&&cursor.moveToNext()) {
                Log.d(TAG, "queryModelSkillContents: "+cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_CONTENT))+cursor.getCount());
                CommandInfo info = new CommandInfo();
                info.setModuleVersionId(cursor.getInt(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION_ID)));
                info.setModuleVersion(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION)));
                info.setModuleName(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME)));
                info.setModuleType(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_TYPE)));
                info.setOrder(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ORDER)));
                info.setSkillName(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME)));
                info.setIsdisplay(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ISDISPLAY)));
                info.setInstructdesc(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.INSTRUCTDESC)));
                info.setItemId(cursor.getInt(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ITEM_ID)));
                info.setIsrecommanded(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ISRECOMMANDED)));
                info.setInstructContent(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_CONTENT)));
                info.setInstructTeach(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_TEACH)));
                info.setIconPath(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_PATH)));
                infos.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }


    private void updateTtsDb(List<CommandInfo> commands) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues;
        try {
            for (CommandInfo ttsInfo : commands) {
                contentValues = createContentValues(ttsInfo);
                long rows = db.insert(CommandTableInfo.COMMAND_INFO_TABLE_NAME, null, contentValues);
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
        CommandProvider.getInstance(mContext).initCommandTypeInfo();
    }


    private List<CommandInfo> changeBean(List<HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean> infoList) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        if (infoList != null && infoList.size() > 0) {
            for (HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean instructsBean : infoList) {

                List<HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean.ModuleSkillsBean> skillList = instructsBean.getModuleSkills();
                if (skillList != null && skillList.size() > 0) {
                    for (HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean.ModuleSkillsBean skillBean: skillList) {

                        List<HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean.ModuleSkillsBean.InstructDetailsBean> detailList = skillBean.getInstructDetails();
                        if (detailList != null && detailList.size() > 0) {
                            for (HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean.ModuleSkillsBean.InstructDetailsBean detail : detailList) {
                                CommandInfo info = new CommandInfo();
                                info.setModuleVersionId(instructsBean.getModuleVersionId());
                                info.setModuleVersion(instructsBean.getModuleVersion());
                                info.setModuleName(instructsBean.getModuleName());
                                info.setModuleType(instructsBean.getModuleType());
                                info.setOrder(instructsBean.getOrder());
                                info.setSkillName(skillBean.getSkillName());
                                info.setIsdisplay(skillBean.getIsDisplay());
                                info.setInstructdesc(skillBean.getInstructDesc());
                                info.setItemId(detail.getItemId());
                                info.setIsrecommanded(detail.getIsRecommanded());
                                info.setInstructContent(detail.getInstructContent());
                                info.setInstructTeach(detail.getInstructTeach());
                                info.setSkillIconUrl(skillBean.getSkillIconUrl());
                                commandInfoList.add(info);
                            }
                        }
                    }
                }
            }
        }
        return commandInfoList;
    }

    private ContentValues createContentValues(CommandInfo ttsInfo) {
        ContentValues values = new ContentValues();
        values.put(CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION_ID, ttsInfo.getModuleVersionId());
        values.put(CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION, ttsInfo.getModuleVersion());
        values.put(CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME, ttsInfo.getModuleName());
        values.put(CommandTableInfo.COMMAND_COLUMNS.MODULE_TYPE, ttsInfo.getModuleType());
        values.put(CommandTableInfo.COMMAND_COLUMNS.ORDER, ttsInfo.getOrder());
        values.put(CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME, ttsInfo.getSkillName());
        values.put(CommandTableInfo.COMMAND_COLUMNS.ISDISPLAY, ttsInfo.getIsdisplay());
        values.put(CommandTableInfo.COMMAND_COLUMNS.INSTRUCTDESC, ttsInfo.getInstructdesc());
        values.put(CommandTableInfo.COMMAND_COLUMNS.ITEM_ID, ttsInfo.getItemId());
        values.put(CommandTableInfo.COMMAND_COLUMNS.ISRECOMMANDED, ttsInfo.getIsrecommanded());
        values.put(CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_CONTENT, ttsInfo.getInstructContent());
        values.put(CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_TEACH, ttsInfo.getInstructTeach());
        values.put(CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_URL, ttsInfo.getSkillIconUrl());
        values.put(CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_PATH, ttsInfo.getIconPath());

        return values;
    }

    private boolean isCommandPathChanged(){
        String path =SharedPreferencesUtils.getString(mContext,"command_path",instructPaht2);
        File file = new File(path);
        if (file.exists()){
            return true;
        }else {
            return false;
        }
    }

    private void preSetCommandDb(){

//        String instructPaht = Environment.getExternalStorageDirectory() + "/iflytek/ica/instruct/instruct_init.json";
        Log.d(TAG,"isCommandPathChanged="+isCommandPathChanged());
       if(isTtsExist()&&isCommandPathChanged()){
           Log.e(TAG, "preSetCommandDb: the database is exist");
           CommandProvider.getInstance(mContext).initCommandTypeInfo();
           return;
       }

       File file = new File(instructPaht);
       if(!file.exists()){
           Log.e(TAG, "preSetCommandDb: the instruct_init not exist");
           file = new File(instructPaht2);
           if(!file.exists()){
               Log.e(TAG, "preSetCommandDb: the instruct_init not exist");
               return;
           }else {
               instructRealPath = instructPaht2;
           }
       }else {
           instructRealPath = instructPaht;
       }
        SharedPreferencesUtils.saveString(mContext,"command_path",instructRealPath);
        LogUtils.debugLarge(TAG,"preSetCommandDb: "+readFromSDC());

        HuVoiceAssitContentBean huVoiceAssitContentBean = new Gson().fromJson(readFromSDC(),HuVoiceAssitContentBean.class);
        updateBackupCommandDb(huVoiceAssitContentBean.getData().getNewContentVersionData().getInstructs());
    }


    private boolean isTtsExist() {
        boolean ret = false;
        Cursor cursor = null;
        try {
            cursor = mDbHelper.getReadableDatabase().query(CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME, null,
                    null, null,
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

    private String readFromSD()  {
        StringBuilder sb = new StringBuilder("");
        String instructPaht = Environment.getExternalStorageDirectory() + "/iflytek/ica/instruct/instruct_init.json";
        //打开文件输入流
        FileInputStream input = null;
        try {
            input = new FileInputStream(instructPaht);
            byte[] temp = new byte[1024];
            int len = 0;
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private String readFromSDC(){
        String line="";
        StringBuffer stringBuffer = new StringBuffer();
        FileInputStream fis = null;
        try {
//            String file = Environment.getExternalStorageDirectory() + "/iflytek/ica/instruct/instruct_init.json";
            fis = new FileInputStream(instructRealPath);
            InputStreamReader reader = new InputStreamReader(fis,"UTF-8"); //设置属性，防止部分文字乱码
            BufferedReader br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }
            br.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuffer.toString();
    }

    private void updateBackupCommandDb(List<HuVoiceAssitContentBean.DataBean.NewContentVersionDataBean.InstructsBean> infoList) {
        Log.d(TAG, "updateCommandDb: ");

        List<CommandInfo> commands = changeBean(infoList);
        index = 1;
        Observable.fromIterable(commands).map(new Function<CommandInfo, Integer>() {
            @Override
            public Integer apply(CommandInfo info) throws Exception {
                String iconPath = ImageUtils.getInstance(BaseApplication.getInstance().getApplicationContext()).getCommandIcon(info);
                info.setIconPath(iconPath);
                return index++;
            }

        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer info) throws Exception {
                        if(info==commands.size()){
//                            mDbHelper.deleteBackUpCommandAllData(mDbHelper.getWritableDatabase());
                            updateBackupTtsDb(commands);
                        }
                    }

                });

    }


    private void updateBackupTtsDb(List<CommandInfo> commands) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues;
        try {
            for (CommandInfo ttsInfo : commands) {
                contentValues = createContentValues(ttsInfo);
                long rows = db.insert(CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME, null, contentValues);
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
        CommandProvider.getInstance(mContext).initCommandTypeInfo();
    }
    public void queryTryGuideTts(TryGuideTtsInterface listener) {
        Cursor cursor = null;
        CommandInfo info = new CommandInfo();
        String table = CommandTableInfo.COMMAND_BACK_INFO_TABLE_NAME;
        if(AppConstant.DOWNLOAD_FINISEH)
            table = CommandTableInfo.COMMAND_INFO_TABLE_NAME;
        Log.d(TAG, "queryTryGuideTts() called with: conditionId = ["  + "]"+table+"...AppConstant.DOWNLOAD_FINISEH:"+AppConstant.DOWNLOAD_FINISEH);
        try {
            cursor = mDbHelper.getReadableDatabase().query(table, null,
                    CommandTableInfo.COMMAND_COLUMNS.ISRECOMMANDED + " = 1 ", null,
                    null, null, null,null);
            Log.d(TAG, "queryTryGuideTts() called::"+cursor.getCount());
            if(cursor!=null&&cursor.getCount()>0){
                Random r = new Random();
                int index= r.nextInt(cursor.getCount());
                if(cursor.moveToPosition(index)){
                    info.setModuleVersionId(cursor.getInt(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION_ID)));
                    info.setModuleVersion(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_VERSION)));
                    info.setModuleName(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_NAME)));
                    info.setModuleType(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.MODULE_TYPE)));
                    info.setOrder(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ORDER)));
                    info.setSkillName(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.SKILL_NAME)));
                    info.setIsdisplay(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ISDISPLAY)));
                    info.setInstructdesc(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.INSTRUCTDESC)));
                    info.setItemId(cursor.getInt(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ITEM_ID)));
                    info.setIsrecommanded(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.ISRECOMMANDED)));
                    info.setInstructContent(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_CONTENT)));
                    info.setInstructTeach(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.INSTRUCT_TEACH)));
                    info.setIconPath(cursor.getString(cursor.getColumnIndex(CommandTableInfo.COMMAND_COLUMNS.SKILL_ICON_PATH)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        listener.onTryGuideTtsFound(info);

    }

    public interface TryGuideTtsInterface{
        void onTryGuideTtsFound(CommandInfo info);
    }




}
