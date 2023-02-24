package com.chinatsp.ifly.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * 删除202讯飞资源包
 */
public class ResDeleter {

    private final Context mContext;
    private static final String MP3Path = "/sdcard/iflytek/mp3";
    private static final String RESPath = "/sdcard/iflytek/res";
    private static final String VIDEOPath = "/sdcard/iflytek/Video";

    public ResDeleter(Context context) {
        mContext = context;
    }

    public void deleteRes(){
        File mp3File = new File(MP3Path);
        File resFile = new File(RESPath);
        File videoFile = new File(VIDEOPath);
        if(mp3File.exists()){
            deleteDir(mp3File);
        }


        if(resFile.exists()){
            deleteDir(resFile);
        }

        if(videoFile.exists()){
            deleteDir(videoFile);
        }
    }

    private  boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if(children==null) return false;
            for (int i=0; i<children.length; i++) {

                if("Active".equals(children[i]))
                    continue;
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
