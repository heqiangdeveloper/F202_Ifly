package com.chinatsp.ifly.callback;

import android.content.Intent;

/**
 * Created by ytkj on 2018/6/29.
 */

public interface UsbIntentListener {

    void onUsbMounted(Intent intent);

    void onUsbUnMounted(Intent intent);

    void onScanStart(Intent intent);

    void onScanFinish(Intent intent);
}
