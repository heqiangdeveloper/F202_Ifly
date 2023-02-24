package com.chinatsp.adapter.ttsservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import static android.os.ParcelFileDescriptor.MODE_WORLD_READABLE;

/**
 * 获取语音播报类型
 * Created by liqianwei on 2019/7/22.
 */

public class ActorUtils {
    public static  int getActor(Context c) {
        int actor = -10001;

        String stringActor = Settings.System.getString(c.getContentResolver(), "current_actor");  //与 ttsmanager 同步
        Log.d("TtsManager", "getActor() called with: stringActor = [" + stringActor + "]");
        actor = actorToValue(stringActor);
        return  actor;
    }

    public static int actorToValue(String actor){
        int value = 9;
        if(actor==null||actor.equals("")||actor.contains("小欧")){
            value = -10001;
        }else {  //说明是科大讯飞
            if("嘉嘉".equals(actor)){
                value = 9;
            }else  if("小燕".equals(actor)){
                value = 3;
            }else  if("小峰".equals(actor)){
                value = 4;
            }else  if("楠楠".equals(actor)){
                value = 7;
            }else  if("小倩".equals(actor)){
                value = 11;
            }else  if("晓蓉".equals(actor)){
                value = 14;
            }else  if("小美".equals(actor)){
                value = 15;
            }else  if("小强".equals(actor)){
                value = 24;
            }else  if("晓坤".equals(actor)){
                value = 25;
            }else  if("晓琳".equals(actor)){
                value = 22;
            }else  if("小雪".equals(actor)){
                value = 50110;
            }else  if("小师".equals(actor)){
                value = 51180;
            }else  if("小洁".equals(actor)){
                value = 50130;
            }else  if("晓梦".equals(actor)){
                value = 23;
            }else  if("老马".equals(actor)){
                value = 12;
            }else  if("晓婧".equals(actor)){
                value = 8;
            }
        }
        return value;
    }
}
