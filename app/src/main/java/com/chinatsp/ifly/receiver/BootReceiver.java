package com.chinatsp.ifly.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.NoviceGuideActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.service.FloatViewIdleService;
import com.chinatsp.ifly.module.me.recommend.service.VideoLoadService;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.example.loginarar.LoginManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("BootReceiver", "action:" + intent.getAction());

        //新手引导
       /* int identifyTimes = SharedPreferencesUtils.getInt(context, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY,-1);
        String userToken = LoginManager.getInstance().getUserToken();
        Log.d("BootReceiver","--lh--userToken:"+userToken);
        if (TextUtils.isEmpty(userToken)) {//未登陆
            SharedPreferencesUtils.saveInt(context, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY,-1);
            SharedPreferencesUtils.saveBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,false);
        }else {//已登录
            if(identifyTimes == -1){
                identifyTimes = 1;
                SharedPreferencesUtils.saveInt(context, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY,identifyTimes);
                SharedPreferencesUtils.saveBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,true);

                Intent i = new Intent(context, GuideMainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                context.startActivity(i);
            }else {
                SharedPreferencesUtils.saveInt(context, AppConstant.PREFERENCE_NOVICE_GUIDE_TIMES_KEY,1);
                SharedPreferencesUtils.saveBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,false);
            }
        }*/
         SharedPreferencesUtils.saveBoolean(context,AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,false);
        //收到开机广播进行主动服务
        boolean isFirstUse = SharedPreferencesUtils.getBoolean(context, AppConstant.PREFERENCE_NOVICE_GUIDE_KEY,false);
        if(!isFirstUse){
            FloatViewIdleService.start(context, intent.getAction());
        }
        Intent intent1 = new Intent(context, VideoLoadService.class);
        context.startService(intent1);
    }
}
