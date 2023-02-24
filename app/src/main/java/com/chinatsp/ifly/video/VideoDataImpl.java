package com.chinatsp.ifly.video;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 7寸获取视频数据的实现类
 * Created by ytkj on 2018/5/18.
 */

public class VideoDataImpl implements VideoDataModel {

    private VideoDataImpl() {
    }

    private static final class SingleHolder {
        private static final VideoDataModel INSTANCE = new VideoDataImpl();
    }

    protected static VideoDataModel getInstance() {
        return SingleHolder.INSTANCE;
    }

    @Override
    public ArrayList<VideoModel> getVideoList(Context context, String sqlPath, boolean isFileMode) {
        sqlPath = Untils.accuratePath(sqlPath);
        ArrayList<VideoModel> data = new ArrayList<>();
        if (context == null)return data;
        Cursor c = null;
        try {
            CoagentMediaScanManager manager = getCoagentMediaScanManager(context);
//            c = manager.queryWithSQL("select * from video where path like '" + sqlPath + "%'");
            c = manager.getAllMedia(sqlPath, MediaConstantsDef.MEDIA_TYPE.MUSIC);
            if (c != null) {
                VideoModel bean;
                List<String> folderList = new ArrayList<>();
                StringBuilder stringBuilder;
                while (c.moveToNext()) {
                    String path = c.getString(c.getColumnIndexOrThrow("path"));
                    if (isFileMode) {
                        String[] split1 = sqlPath.split(File.separator);
                        String[] split2 = path.split(File.separator);
                        if (split2.length - split1.length > 1) { // 1层文件夹以上
                            stringBuilder = new StringBuilder();
                            for (int i = 1; i < split1.length + 1; i++) {
                                stringBuilder.append(File.separator).append(split2[i]);
                            }
                            String dirPath = stringBuilder.toString();
                            if (folderList.contains(dirPath)) {
                                continue;
                            }
                            String folderName = split2[split1.length];
                            bean = new VideoModel();
                            bean.setName(folderName);
                            bean.setPath(dirPath);
                            bean.setIsFileOrDirect(true);
                            folderList.add(dirPath);
                            data.add(0, bean);
                            continue;
                        }
                    }
                    long _id = c.getLong(c.getColumnIndexOrThrow("_id"));
                    String name = c.getString(c.getColumnIndexOrThrow("name"));
//                    long duration = c.getLong(c.getColumnIndexOrThrow("duration"));
                    bean = new VideoModel();
                    bean.setId((int) _id);
                    bean.setName(name);
                    bean.setPath(path);
                    bean.setIsFileOrDirect(new File(path).isDirectory());
                    Log.d("liqw", "getVideoList: "+name);
//                    bean.setDuration(duration);
                    data.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return data;
    }

    @Override
    public List<VideoModel> getCurrentDirVideo(Context context, String sqlPath) {
        sqlPath = Untils.accuratePath(sqlPath);
        ArrayList<VideoModel> data = new ArrayList<>();
        Cursor c = null;
        try {
            CoagentMediaScanManager manager = getCoagentMediaScanManager(context);
            c = manager.getAllMedia(sqlPath, MediaConstantsDef.MEDIA_TYPE.VIDEO);
//            c = manager.queryWithSQL("select * from video where path like '" + sqlPath + "%'");
            if (c != null) {
                VideoModel bean;
                String dirPath = new File(sqlPath).getAbsolutePath();
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    String path = c.getString(c.getColumnIndexOrThrow("path"));
                    if (!new File(path).getParent().equals(dirPath)) {
                        continue;
                    }
                    long _id = c.getLong(c.getColumnIndexOrThrow("_id"));
                    String name = c.getString(c.getColumnIndexOrThrow("name"));
                    long duration = c.getLong(c.getColumnIndexOrThrow("duration"));
                    bean = new VideoModel();
                    bean.setId((int) _id);
                    bean.setName(name);
                    bean.setPath(path);
                    bean.setIsFileOrDirect(new File(path).isDirectory());
                    bean.setDuration(duration);
                    data.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return data;
    }

    private CoagentMediaScanManager getCoagentMediaScanManager(Context context) {
        return CoagentMediaScanManager.getMediaScanManager(context);
    }

//    @Override
//    public int getVideoCount(Context context, String path) {
//        return getVideoList(context, path, false).size();
//    }

}
