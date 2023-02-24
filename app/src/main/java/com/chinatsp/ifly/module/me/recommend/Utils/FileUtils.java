package com.chinatsp.ifly.module.me.recommend.Utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.chinatsp.ifly.module.me.recommend.bean.VideoDataBean;
import com.chinatsp.ifly.module.me.recommend.db.VideoListUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class FileUtils {
    private static final String TAG ="FileUtils";
    private static  final String firstFolder = "VoiceVideo";
    private static FileUtils fileUtils;
    private static Context mContext;
    public static FileUtils getInstance(Context context) {
        mContext = context;
        if (fileUtils == null) {
            fileUtils = new FileUtils();
        }
        return fileUtils;
    }

    //判断是否安装SDCard
    public static boolean isSdOk(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        return false;
    }
    //创建一个文件夹，用来存放下载的文件
    public static File getRootFile(String direName){
        File sd = Environment.getExternalStorageDirectory();
        File rootFile = new File(sd,firstFolder);
        if (!rootFile.exists()){
            rootFile.mkdirs();
        }
        //创建二级目录
        File dirSecondFile = new File(rootFile+File.separator+direName+File.separator);
        if (!dirSecondFile.exists()){
            dirSecondFile.mkdirs();
        }
        return dirSecondFile;
    }

    public static boolean deleteFile(String filePath) {
        Log.d("filePath","filePath="+filePath);
        if (filePath==null){
            return false;
        }
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    public  boolean isUsableSpace(){
        List<VideoDataBean> videoDataBeanList= VideoListUtil.getInstance(mContext).getWholeListByDate();
        Log.d(TAG,"videoDataBeanList::: ="+videoDataBeanList.size());
        for (int i=0;i<videoDataBeanList.size();i++){
            Log.d(TAG,"videoDataBeanList.get(i) ="+i+"----"+videoDataBeanList.get(i).getTime());
        }
        File file =Environment.getExternalStorageDirectory();
        long totalSpace= file.getTotalSpace();
        long usableSpace= file.getUsableSpace();
        Log.d(TAG,"size ="+"totalSpace"+totalSpace/(1024*1024)+",usableSpace="+usableSpace/(1024*1024));
        Log.d(TAG,"usableSpace/totalSpace="+div(usableSpace,totalSpace,2));
        if (div(usableSpace,totalSpace,2)>=0.15){
            return true;
        }else {
            return false;
        }
    }

    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private Handler mHandler =  new Handler(Looper.getMainLooper());


    public void deleteSpaceIsUsable(ResultCallback resultCallback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file =Environment.getExternalStorageDirectory();
                long totalSpace= file.getTotalSpace();
                long usableSpace= file.getUsableSpace();
                List<VideoDataBean> videoDataBeanList= VideoListUtil.getInstance(mContext).getWholeListByDate();
                for (int i=0;i<=videoDataBeanList.size();i++){
                    deleteFile(videoDataBeanList.get(i).getVideoPath());
                    Log.d(TAG,"usableSpace ="+usableSpace+",totalSpace="+totalSpace);
                    if ((usableSpace/totalSpace>=0.15)){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                resultCallback.isOnSuccess(true);
                            }
                        });
                        Log.d(TAG,"mHandler.post");
                        break;
                    }else {
                        resultCallback.isOnSuccess(false);
                    }
                }
            }
        }).start();
    }

    public static abstract class ResultCallback{
        public abstract void isOnSuccess(boolean isUsable);
    }



    public void writeStringToFile(String json, String filePath) {
        File txt = new File(filePath);
        if (!txt.exists()) {
            try {
                txt.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] bytes = json.getBytes();
        int b = json.length();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(txt);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
