package com.chinatsp.ifly.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chinatsp.ifly.api.activeservice.ActiveServiceModel;
import com.chinatsp.ifly.voice.platformadapter.manager.LocateManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;

/**
 * Created by zxb on 2019/6/27.
 */

public class MXReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String[] location = LocateManager.getInstance(context).getLocation();
        String locationInfo = null;
        if (location != null) {
            locationInfo = location[1] + "," + location[0];
        }

        //接收美行地图导航时发送过来的广播
        //起点纬度
        Double startLat = intent.getDoubleExtra("startLat",0);
        //起点经度
        Double startLon = intent.getDoubleExtra("startLon",0);


        String start_location = startLon+"," + startLat;
        //终点纬度
        Double endLat = intent.getDoubleExtra("endLat",0);
        //终点经度
        Double endLon = intent.getDoubleExtra("endLon",0);
        String end_location = endLon+","+endLat;

//        ActiveServiceModel.getInstance().DestWeather(context,locationInfo,end_location);

    }
}
