package com.chinatsp.ifly.video;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.utils.LogUtils;
import com.iflytek.adapter.sr.SRAgent;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 媒体信息提供类
 */
public class VideoProvider {

    private static final String TAG = "xyj_VideoProvider";
    //使用Map在查找方面会效率高一点
    final ReentrantReadWriteLock rwlHDD = new ReentrantReadWriteLock();
    final ReentrantReadWriteLock rwlUSB = new ReentrantReadWriteLock();
    private List<VideoModel> mHDDList;
    private List<VideoModel> mUSBList;
    private Context mContext;
    public static final int LOAD_START = 1;
    public static final int LOAD_FINISHED = 2;
    private Map<Integer, Integer> mScanMap;


    public static VideoProvider getInstance() {
        return SingletonHolder.sInstance;
    }



    private static class SingletonHolder {
        private static final VideoProvider sInstance = new VideoProvider();
    }

    private VideoProvider() {
        mHDDList = new ArrayList<>();
        mUSBList = new ArrayList<>();
        mScanMap = new HashMap<Integer, Integer>();

    }


    public void setContext(Context c) {
        mContext = c;
        Log.d(TAG, "setContext() called with: c = [" + AppConstant.hddRoot + "]");
        onScanFinish(null, 1);
        boolean usb1Exit = StorageUtil.isPathExit(AppConstant.usbRoot, c);
        if (usb1Exit)
            onScanFinish(null, 2);
    }

    public VideoModel queryVideoByName(String name) {

        if(mHDDList!=null){
            for (int i = 0; i < mHDDList.size(); i++) {
                if(mHDDList.get(i).getName().equals(name))
                    return mHDDList.get(i);
            }
            for (int i = 0; i < mHDDList.size(); i++) {
                if(mHDDList.get(i).getName().contains(name))
                    return mHDDList.get(i);
            }
        }

        if(mUSBList!=null){
            for (int i = 0; i < mUSBList.size(); i++) {
                if(mUSBList.get(i).getName().equals(name))
                    return mUSBList.get(i);
            }
            for (int i = 0; i < mUSBList.size(); i++) {
                if(mUSBList.get(i).getName().contains(name))
                    return mUSBList.get(i);
            }
        }
        return null;
    }


    public void onUsbUnMounted() {
        mScanMap.put(2, -1);

        rwlUSB.writeLock().lock();
        if (mUSBList != null) {
            mUSBList.clear();
        }
        rwlUSB.writeLock().unlock();
        uploadVideoListToIfly(mHDDList,mUSBList);
    }

    public void onScanStart(Intent intent) {
        String udisk = intent.getStringExtra("scanPath");
        if (udisk == null) return;
        int flag = -1;
        if (udisk.contains("udisk2"))
            flag = 3;
        else if (udisk.contains("udisk"))
            flag = 2;
        else if (udisk.contains(AppConstant.hddRoot))
            flag = 1;
        mScanMap.put(flag, LOAD_START);
    }

    public void onScanFinish(Intent intent, int f) {
//        Log.d(TAG, "onScanFinish: "+Log.getStackTraceString(new Throwable()));
        int flag = -1;
        String udisk = null;
        if (intent == null)
            flag = f;
        else {
            udisk = intent.getStringExtra("scanPath");
            Log.d(TAG, "onScanFinish: udisk:" + udisk);
            if (udisk == null || "".equals(udisk)) return;
            if (udisk.contains("udisk2"))
                flag = 3;
            else if (udisk.contains("udisk"))
                flag = 2;
            else if (udisk.contains(AppConstant.hddRoot))
                flag = 1;
            else
                flag = -1;
            mScanMap.put(flag, LOAD_FINISHED);
        }

        Log.d(TAG, "onScanFinish() called with: flag = [" + flag + "]" + ".." + udisk + ".." + intent);
        if (flag == 1 || flag == 2) {
            notifyPicList(flag);
        }
    }

    private void notifyPicList(final int integer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "lh: call long:::" + integer);
                getVideoList(integer);
            }
        }).start();
    }

    //获取数据
    public List<VideoModel> getVideoList(int type) {
        String path = AppConstant.hddRoot;
        if (type == 2) {//USB
            path = AppConstant.usbRoot;
        }
        ArrayList<VideoModel> list = ModelManager.getVideoDataModel().getVideoList(mContext, path, false);
        if (type == 1) {//HDD
            rwlHDD.writeLock().lock();
            mHDDList.clear();
            mHDDList.addAll(list);
            rwlHDD.writeLock().unlock();
//            uploadVideoListToIfly(mHDDList);
        } else if (type == 2) {//usb
            rwlUSB.writeLock().lock();
            mUSBList.clear();
            mUSBList.addAll(list);
            rwlUSB.writeLock().unlock();
//            uploadVideoListToIfly(mUSBList);
        }
        uploadVideoListToIfly(mHDDList,mUSBList);
        Log.d(TAG, "lh:size:" + (list == null ? null : list.size()) + ",type:" + type);
        return list;
    }


    private void uploadVideoListToIfly(List<VideoModel> hdd,List<VideoModel> usb) {

        JSONArray mArray = new JSONArray();
        JSONObject mDict;
        try {
            for (int i = 0; i < hdd.size(); i++) {
                mDict = new JSONObject();
                mDict.put("id", i);
                mDict.put("name", hdd.get(i).getName());
                mArray.put(mDict);
            }
            for (int i = 0; i < usb.size(); i++) {
                mDict = new JSONObject();
                mDict.put("id", hdd.size()+i);
                mDict.put("name", usb.get(i).getName());
                mArray.put(mDict);
            }

            JSONObject result = new JSONObject();

            result.put("dictname", "video");
            result.put("dictcontant", mArray);

            JSONArray rA = new JSONArray();
            rA.put(result);

            JSONObject root = new JSONObject();
            root.put("grm", rA);
            LogUtils.d(TAG, "root:" + root.toString());
            SRAgent.getInstance().uploadDict(root.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private boolean isScanFinished(int type) {
        if (!mScanMap.containsKey(type))
            return false;
        if (mScanMap.get(type) == LOAD_FINISHED)
            return true;
        else
            return false;
    }

}
