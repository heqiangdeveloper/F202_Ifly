package com.chinatsp.ifly.module.me.recommend.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.module.me.recommend.model.HuVoiceAsssitContentModel;
import com.chinatsp.ifly.module.me.recommend.view.ManageFloatWindow;
import com.chinatsp.ifly.receiver.NetStateReceiver;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;

/**
 * ClassName: //开机下载小欧推荐视频文件
 * Function: //TODO
 * Reason: //TODO
 *
 * @author: qlf
 * @version: v1.0
 * @date : 2020/6/22
 */

public class VideoLoadService extends IntentService{
    private final static String TAG = "VideoLoadService";

    public VideoLoadService() {
        super("VideoLoadService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG,"onHandleIntent");
        String contentVersion = SharedPreferencesUtils.getString(this, AppConstant.KEY_TTS_CONTENT_VERSION, "");
        Log.d(TAG,"contentVersion="+contentVersion);
//        HuVoiceAsssitContentModel.getInstance().getHuVoiceAsssitContent(this,AppConfig.INSTANCE.token, "F202_01", contentVersion);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }
    /**
     * 复写onStartCommand()方法
     * 默认实现 = 将请求的Intent添加到工作队列里
     **/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
//        ManageFloatWindow.getInstance(this).showFloatView();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

}
