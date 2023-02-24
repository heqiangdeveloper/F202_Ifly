package com.chinatsp.ifly.source;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.chinatsp.ifly.base.BaseApplication;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * u盘工具类
 * Created by ytkj on 2018/6/11.
 */

public class StorageUtil {

    private static String TAG = StorageUtil.class.getSimpleName();

    public static boolean isUsbExit() {
        File storage = new File(Constant.usbRoot).getParentFile();
        if (storage.getName().contains("udisk")) {
            if (isPathExit(storage.getAbsolutePath())) {
                return true;
            }
        } else {
            File[] files = storage.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("udisk") && isPathExit(file.getAbsolutePath())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public synchronized static boolean isPathExit(String path) {
        boolean result = false;
        Method method;
        try {
            StorageManager storageManager = (StorageManager) BaseApplication.getInstance()
                    .getSystemService(Context.STORAGE_SERVICE);
            method = StorageManager.class.getDeclaredMethod("getVolumeState", String.class);
            String state = (String) method.invoke(storageManager, path);
            // Log.i("StorageUtil", "isPathExit: path: " + path + " state: " + state);
            if (Environment.MEDIA_CHECKING.equals(state) || Environment.MEDIA_MOUNTED.equals(state)) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List<String> getCanWriteUdisk() {
        List<String> udisks = new ArrayList<>();
        File storage = new File(Constant.usbRoot).getParentFile();
        if (storage.getName().contains("udisk")) {
            if (isPathExit(storage.getAbsolutePath())) {
                udisks.add(storage.getAbsolutePath());
            }
        } else {
            File[] files = storage.listFiles();
            for (File file : files) {
                if (file.getName().contains("udisk") && isPathExit(file.getAbsolutePath())) {
                    udisks.add(file.getAbsolutePath());
                }
            }
        }
        return udisks;
    }

    public static String getUuid(String udisk) {
        Log.i(TAG, "getUuid: file: " + udisk);
        try {
            StorageManager storageManager = (StorageManager) BaseApplication.getInstance()
                    .getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
            StorageVolume[] storageVolumes = (StorageVolume[]) getVolumeList.invoke(storageManager);
            Method getVolumeState = StorageManager.class.getDeclaredMethod("getVolumeState", String.class);
            for (StorageVolume storageVolume : storageVolumes) {
                Method getPath = StorageVolume.class.getMethod("getPath");
                String path = (String) getPath.invoke(storageVolume);
                String state = (String) getVolumeState.invoke(storageManager, path);
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    if (udisk.equals(path) || udisk.equals(path + File.separator)) {
                        String uuid = storageVolume.getUuid();
                        Log.i(TAG, "getUuid: uuid: " + uuid);
                        return uuid;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
