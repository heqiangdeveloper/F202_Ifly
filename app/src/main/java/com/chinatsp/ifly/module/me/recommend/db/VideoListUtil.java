package com.chinatsp.ifly.module.me.recommend.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.db.TtsInfoDbDao;
import com.chinatsp.ifly.db.TtsTableInfo;
import com.chinatsp.ifly.db.entity.TtsInfo;
import com.chinatsp.ifly.module.me.recommend.Utils.Constants;
import com.chinatsp.ifly.module.me.recommend.Utils.FileUtils;
import com.chinatsp.ifly.module.me.recommend.bean.VideoDataBean;
import com.chinatsp.ifly.module.me.recommend.urlhttp.UrlHttpUtil;
import com.chinatsp.ifly.utils.LogUtils;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName: //TODO
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/7/6
 */


public class VideoListUtil {
    private static final String TAG = "VideoListUtil";
    private static Context mContext;
    public DBHelperUtils helper;
    private static String TABLE = "VideoData";
//    public List<VideoDataBean> list;
    private static VideoListUtil videoListUtil;

    public static VideoListUtil getInstance(Context context) {
        mContext = context;
        if (videoListUtil == null) {
            videoListUtil = new VideoListUtil(context);
        }
        return videoListUtil;
    }

    private VideoListUtil(Context context) {
        if (this.helper == null) {
            this.helper = new DBHelperUtils(context, TABLE, null, 1);
        }
    }

    public List<VideoDataBean> getWholeList() {
        List<VideoDataBean>  list = new ArrayList<VideoDataBean>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try {
            File file;
            Cursor cursor = db.rawQuery("select * from " + TABLE + " ORDER BY read,time", null);
            while (cursor.moveToNext()) {
                VideoDataBean videoDataBean = new VideoDataBean(cursor.getInt(cursor
                        .getColumnIndex("_id")), cursor.getString(cursor
                        .getColumnIndex("read")), cursor.getString(cursor
                        .getColumnIndex("time")), cursor.getString(cursor
                        .getColumnIndex("pic_path")), cursor.getString(cursor
                        .getColumnIndex("path")), cursor.getString(cursor
                        .getColumnIndex("name")), cursor.getString(cursor
                        .getColumnIndex("parent_path")));
                file = new File(videoDataBean.getVideoPath());
                if (file.exists()) {
                    Log.d(TAG, "list.size =" + list.size());
                    list.add(videoDataBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            db.close();
        }
        return list;
    }


    public List<VideoDataBean> getWholeListByDate() {
        List<VideoDataBean> list = new ArrayList<VideoDataBean>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try {
            File file;
            Cursor cursor = db.rawQuery("select * from " + TABLE + " ORDER BY time", null);
            while (cursor.moveToNext()) {
                VideoDataBean videoDataBean = new VideoDataBean(cursor.getInt(cursor
                        .getColumnIndex("_id")), cursor.getString(cursor
                        .getColumnIndex("read")), cursor.getString(cursor
                        .getColumnIndex("time")), cursor.getString(cursor
                        .getColumnIndex("pic_path")), cursor.getString(cursor
                        .getColumnIndex("path")), cursor.getString(cursor
                        .getColumnIndex("name")), cursor.getString(cursor
                        .getColumnIndex("parent_path")));
                file = new File(videoDataBean.getVideoPath());
                if (file.exists()) {
                    Log.d(TAG, "list.size =" + list.size());
                    list.add(videoDataBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            db.close();
        }
        return list;
    }

    //判断数据库中是否有该条tts数据.
    public boolean isVideoExist(String fileName) {
        Log.d(TAG, "fileName =" + fileName);
        boolean ret = false;
        Cursor cursor = null;
        try {
            cursor = helper.getReadableDatabase().query(TABLE, null,
                    "name" + "=?", new String[]{fileName},
                    null, null, null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                ret = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "isVideoExist: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }


    public boolean isVideoExistById(int videoId) {
        Log.d(TAG, "videoId =" + videoId);
        boolean ret = false;
        Cursor cursor = null;
        try {
            cursor = helper.getReadableDatabase().query(TABLE, null,
                    "_id" + "=?", new String[]{videoId+""},
                    null, null, null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                ret = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "isVideoExist: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return ret;
    }

    public void insert(VideoDataBean videoDataBean) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.execSQL(
                    "insert into "
                            + TABLE
                            + "(_id,read,time,pic_path,path,name,parent_path) values(?,?,?,?,?,?,?)",
                    new Object[]{videoDataBean.getId(), videoDataBean.getRead(), videoDataBean.getTime(),
                            videoDataBean.getPicPath(), videoDataBean.getVideoPath(), videoDataBean.getVideoName(), videoDataBean.getParent_path()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            db.close();
        }
    }


    public boolean noReadVideoExist() {
        if (getWholeList().size() != 0 && getWholeList().get(0).getRead().equalsIgnoreCase("true")) {
            return false;
        } else {
            return true;
        }
    }

    public void update(String isRead, String path) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("read", isRead);
        db.update(TABLE, values, "path=?", new String[]{path});
    }

    public int getIdByPath(String path) {
        String id;
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(TABLE, null, "path=?", new String[]{path}, null, null, null);
        while (cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex("_id"));
            Log.d(TAG, "id =" + id);
            return Integer.parseInt(id);
        }
        return -1;
    }

    public String getVideoPathById(String id) {
        Log.d(TAG, "id=" + id);
        String path;
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(TABLE, null, "_id=?", new String[]{id}, null, null, null);
        while (cursor.moveToNext()) {
            path = cursor.getString(cursor.getColumnIndex("path"));
            Log.d(TAG, "path =" + path);
            return path;
        }
        return null;
    }

    public String getVideoCoverPathById(String id) {
        Log.d(TAG, "id=" + id);
        String path;
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(TABLE, null, "_id=?", new String[]{id}, null, null, null);
        while (cursor.moveToNext()) {
            path = cursor.getString(cursor.getColumnIndex("pic_path"));
            Log.d(TAG, "pic_path =" + path);
            return path;
        }
        return null;
    }


    public String getVideoNameById(String id) {
        String videoName;
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(TABLE, null, "_id=?", new String[]{id}, null, null, null);
        while (cursor.moveToNext()) {
            videoName = cursor.getString(cursor.getColumnIndex("name"));
            Log.d(TAG, "videoName =" + videoName);
            return videoName;
        }
        return null;
    }

    public String rawQueryNextOrPre(String path, Boolean isNext) {
        Log.d(TAG, "path =" + path);
        SQLiteDatabase db = helper.getWritableDatabase();
        String id, video_path;
        Cursor cursor = db.query(TABLE, null, "path=?", new String[]{path}, null, null, null);
        while (cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex("_id"));
            Log.d(TAG, "id =" + id);
            int newId;
            if (isNext) {
                newId = Integer.parseInt(id) + 1;
            } else {
                newId = Integer.parseInt(id) - 1;
            }
            Cursor cursor1 = db.query(TABLE, null, "_id=?", new String[]{newId + ""}, null, null, null);
            if (cursor1.moveToNext()) {
                video_path = cursor1.getString(cursor.getColumnIndex("path"));
                Log.d(TAG, "moveToNext:video_path =" + video_path);
                return video_path;
            }
        }
        return path;
    }

    public void deleteByVideoId(int videoId) {
        Log.d(TAG, "deleteById:videoId =" + videoId);
        boolean isVideoDelete = FileUtils.deleteFile(getVideoPathById(videoId + ""));
        boolean isVideoCoverDelete = FileUtils.deleteFile(getVideoCoverPathById(videoId + ""));
        Log.d(TAG, "isVideoDelete =" + isVideoDelete + ",isVideoCoverDelete" + isVideoCoverDelete);
        mContext.getContentResolver().delete(Constants.VIDEODATA_URL, "_id=?", new String[]{String.valueOf(videoId)});
    }

    public void delete(int videoDataBean) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Log.d(TAG, "delete");
        try {
            db.execSQL("delete from " + TABLE + " where _id = ?",
                    new Object[]{videoDataBean});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public void deleteAllList() {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.execSQL("delete from " + TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            db.close();
        }

    }

    public void jumpToVideoActivity(Context context, String videoId) {
        String name = VideoListUtil.getInstance(context).getVideoNameById(videoId);
        Log.d(TAG, "name =" + name);
        List<VideoDataBean> videoDataBeanList = VideoListUtil.getInstance(context).getWholeList();
        int ss = 0;
        for (int i = 0; i < videoDataBeanList.size(); i++) {
            String path = videoDataBeanList.get(i).getVideoPath();
            if (name.equals(path)) {
                ss = i;
            }
        }
        Intent intent = new Intent("com.coagent.intent.action.video");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ps", ss + 1);
        intent.putExtra("name", name);
        intent.putExtra("data", "VideoData");
        intent.putExtra("dataType", 8);
        intent.putExtra("extra_vedio_from", "videoInIfly");
        intent.putExtra("id", videoId);
        context.startActivity(intent);
    }

    private List<Integer> getBlackListVideoId(){
        List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo("mainC_blacklist_video");
        List<Integer> integerList = new ArrayList<>();
        Log.d(TAG,"ttsInfoList ="+ttsInfoList);
        if (ttsInfoList.size() > 0) {
            for (TtsInfo TtsInfo : ttsInfoList) {
                if (TtsInfo.getTtsText().contains("|")) {
                    String ttText = TtsInfo.getTtsText();
                    int size = countStr(ttText, "|");
                    for (int i = 0; i <= size; i++) {
                        Log.d(TAG, "i=" + i);
                        int videoId = -1;
                        if (i == size) {
                            videoId = Integer.parseInt(ttText);
                        } else {
                            int index = ttText.indexOf("|");
                            videoId = Integer.parseInt(ttText.substring(0, index));
                            ttText = ttText.substring(index + 1, ttText.length());
                        }
                        Log.d(TAG, "videoId =" + videoId);
                        deleteByVideoId(videoId);
                        integerList.add(videoId);
                    }
                }else {
                    integerList.add(Integer.valueOf(TtsInfo.getTtsText()));
                }
            }
            return integerList;
        }
        return integerList;
    }

    public void DeleteBlackListVideo() {
        List<TtsInfo> ttsInfoList = TtsInfoDbDao.getInstance(mContext).queryTtsInfo("mainC_blacklist_video");
        if (ttsInfoList.size() > 0) {
            for (TtsInfo TtsInfo : ttsInfoList) {
                if (TtsInfo.getTtsText().contains("|")) {
                    String ttText = TtsInfo.getTtsText();
                    int size = countStr(ttText, "|");
                    for (int i = 0; i <= size; i++) {
                        Log.d(TAG, "i=" + i);
                        int videoId = -1;
                        if (i == size) {
                            videoId = Integer.parseInt(ttText);
                        } else {
                            int index = ttText.indexOf("|");
                            videoId = Integer.parseInt(ttText.substring(0, index));
                            ttText = ttText.substring(index + 1, ttText.length());
                        }
                        Log.d(TAG, "videoId =" + videoId);
                        deleteByVideoId(videoId);
                    }
                }
            }
        }
    }

    public void testDeleVideoId() {
        String ttText = "31|32|28|35|30";
        int size = countStr(ttText, "|");
        if (!ttText.contains("|")) {
            return;
        }
        for (int i = 0; i <= size; i++) {
            Log.d(TAG, "i=" + i);
            int videoId = -1;
            if (i == size) {
                videoId = Integer.parseInt(ttText);
            } else {
                int index = ttText.indexOf("|");
                videoId = Integer.parseInt(ttText.substring(0, index));
                ttText = ttText.substring(index + 1, ttText.length());
                deleteByVideoId(videoId);
            }
        }
        counter = 0;
    }

    private int counter = 0;

    private int countStr(String str1, String str2) {
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else if (str1.indexOf(str2) != -1) {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) +
                    str2.length()), str2);
            return counter;
        }
        return 0;
    }


    private List<String> videoNameList = new ArrayList<>();
    private List<String> videoCoverList = new ArrayList<>();
    private synchronized List<String> getVideoCoverList(){
        List<File> fileList =new ArrayList<>();
        File scannerDirectory = new File(Constants.video_cover_content_path2);
        File scannerDirectory1 = new File(Constants.video_content_path);
        fileList.add(scannerDirectory);
        fileList.add(scannerDirectory1);
        for (File f_file:fileList){
            if (f_file.isDirectory()) {
                for (File file : f_file.listFiles()) {
                    String path = file.getAbsolutePath();
                    if (path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png")) {
                        Log.d(TAG,"path ="+path);
                        String name = file.getName().replace(".png","");
                        if (name.equalsIgnoreCase("no_wakeup")){
                            name = mContext.getResources().getString(R.string.no_wakeup);
                        }else if (name.equalsIgnoreCase("basic_function")){
                            name = mContext.getResources().getString(R.string.basic_function);
                        }
                        if (!videoNameList.contains(name)){
                            videoNameList.add(name);
                            videoCoverList.add(path);
                        }
                    }
                }
            }
        }
        return videoCoverList;
    }
    private List<Integer> integerList =new ArrayList<>();
    private String AUTHORITY ="com.chinatsp.ifly.videodata";
    private Uri videoUri = Uri.parse("content://" + AUTHORITY + "/VideoData");
    public void initVideoData(){
        int videoId=0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());
        getVideoCoverList();
        Log.d(TAG,"integerList="+integerList);
        if (integerList!=null&&integerList.size()==0){
            Log.d(TAG,"integerList="+integerList.size());
            try {
                integerList = getBlackListVideoId();
            }catch (Exception e){
                Log.d(TAG,"get black list error");
            }
        }
        for (int i=0;i<videoCoverList.size();i++){
            String picPath = videoCoverList.get(i);
//            String videoPath = videoContentList.get(i);
            String videoPath = picPath.replace("videocover","videocontent").replace("png","mp4");
            if(isVideoExist(videoNameList.get(i))){
                Log.e(TAG, "isVideoExist:"+videoPath);
                continue;
            }
            if (videoNameList.get(i).contains("小欧语音基础功能")){
                videoId = 7;
            }else if (videoNameList.get(i).contains("小欧语音免唤醒指令")){
                videoId = 8;
            }
            if((integerList!=null)&& integerList.contains(videoId)){
                deleteByVideoId(videoId);
                continue;
            }
            ContentValues values = new ContentValues();
            values.put("_id",videoId);
            values.put("read", "false");
            values.put("time", date);
            values.put("pic_path", picPath);
            values.put("path", videoPath);
            values.put("name", videoNameList.get(i));
            values.put("parent_path", Constants.video_content_path);
            mContext.getContentResolver().insert(videoUri, values);
        }
        Log.d(TAG,"videoCoverList ="+videoCoverList.size());
    }
}
