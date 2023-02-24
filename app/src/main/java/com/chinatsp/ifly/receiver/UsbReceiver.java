package com.chinatsp.ifly.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.callback.UsbIntentListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 接受usb意图
 * Created by ytkj on 2018/4/28.
 */

public class UsbReceiver extends BroadcastReceiver {

    private static final String TAG = "xyj_UsbReceiver";

    private static List<UsbIntentListener> mListeners = new ArrayList<>();
    private static boolean isScanning = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String path = getRealPath(intent);
        Log.i(TAG, "111onReceive: action: " + action + " path: " + path);
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            // StorageVolume volume = intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
            // Log.i(TAG, "onReceive: volume: " + volume);
            String firstMount = intent.getStringExtra("firstMount");
            Log.i(TAG, "onReceive: firstMount: " + firstMount);
            if (!path.contains(AppConstant.hddRoot) &&
                    (TextUtils.isEmpty(firstMount) || !firstMount.equals("true"))) {
                // MyApplication.handClickToPause = false;  需求，插入u盘不取消之前的暂停操作
                for (UsbIntentListener listener : mListeners) {
                    listener.onUsbMounted(intent);
                }

            }
        } else if ((action.equals(Intent.ACTION_MEDIA_EJECT) && Build.VERSION.SDK_INT >= 21) ||
                (Intent.ACTION_MEDIA_UNMOUNTED.equals(action) && Build.VERSION.SDK_INT < 21)) {
            // android o, p用ACTION_MEDIA_EJECT广播，unmounted太慢
            // Log.i(TAG, "onReceive: path: " + intent.getData().toString());
            // StorageVolume volume = intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
            // Log.i(TAG, "onReceive: volume: " + volume);
            for (UsbIntentListener listener : mListeners) {
                listener.onUsbUnMounted(intent);
            }
        } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
//            if (ModelManager.getAudioDataModel().getPathColumnName().equals("_data")) {
                isScanning = true;
                for (UsbIntentListener listener : mListeners) {
                    listener.onScanStart(intent);
                }
//            }
        } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
//            if (ModelManager.getAudioDataModel().getPathColumnName().equals("_data")) {
                isScanning = false;
                for (UsbIntentListener listener : mListeners) {
                    listener.onScanFinish(intent);
                }
//            }
        } else if (action.equals("com.coagent.scanStatus")) {
            // 扫描状态广播 使用chinatsp扫描才用这个广播
//            if (ModelManager.getAudioDataModel().getPathColumnName().equals("path")) {
                int status = intent.getIntExtra("status", -1);
                Log.i(TAG, "onReceive: status: " + status);
                if (status == 1 || status == 5) {
                    // 扫描中...
                    isScanning = true;
                    for (UsbIntentListener listener : mListeners) {
                        listener.onScanStart(intent);
                    }
                } else if (status == 2) {
                    // 扫描结束
                    isScanning = false;
                    for (UsbIntentListener listener : mListeners) {
                        listener.onScanFinish(intent);
                    }
                }
//            }
        } else if (action.equals("com.Id3Parse.scanStatus")) {
//            if (ModelManager.getAudioDataModel().getPathColumnName().equals("path")) {
                // 扫描结束
                isScanning = false;
                for (UsbIntentListener listener : mListeners) {
                    listener.onScanFinish(intent);
                }
//            }
        } else if (action.equals("com.chinatsp.media.data_changed")) {
            for (UsbIntentListener listener : mListeners) {
                listener.onScanFinish(null);
            }
        }
    }

    public static String getRealPath(Intent intent) {
        String result = null;
        Uri usbData = intent.getData();
        if (usbData != null) {
            String realPath = usbData.getSchemeSpecificPart();
            if (!TextUtils.isEmpty(realPath)) {
                result = realPath.replace("///", "/");
            }
        }
        return result;
    }

    public static boolean isScanning() {
        return isScanning;
    }

    public static void registerUsbListen(UsbIntentListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public static void unRegisterUsbListen(UsbIntentListener listener) {
        mListeners.remove(listener);
    }

}
