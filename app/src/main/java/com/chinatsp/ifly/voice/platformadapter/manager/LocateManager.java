package com.chinatsp.ifly.voice.platformadapter.manager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.example.mxextend.IExtendApi;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.listener.ILocationUpdateCallBack;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.sr.SrSession;


/**
 * Created by ytkj on 2019/8/27.
 */

public class LocateManager{

    private static final String TAG = "LocateManager";

    private volatile static LocateManager instance;

    private Context context;

    private IExtendApi extendApi;

    public LocateManager(Context context) {
        this.context = context.getApplicationContext();
        TAExtendManager.getInstance().init(context);
        this.extendApi = TAExtendManager.getInstance().getApi();
    }

    public static LocateManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LocateManager.class) {
                if (instance == null) {
                    instance = new LocateManager(context);
                }
            }
        }
        return instance;
    }


    public void init() {
        extendApi.addLocationUpdateCallBack(mLocationUpdateCallBack);
    }


    public String[] getLocation() {
        String[] latLng;
        String localLatitude = SharedPreferencesUtils.getString(context, AppConstant.LATITUDE, "");
        String localLongitude = SharedPreferencesUtils.getString(context, AppConstant.LONGITUDE, "");
        if (!TextUtils.isEmpty(localLatitude) && !TextUtils.isEmpty(localLongitude)) {
            latLng = new String[2];
            latLng[0] = localLatitude;
            latLng[1] = localLongitude;
            Log.d(TAG, "getLocation from sharedPrefs: latitude" + localLatitude + " ,longitude:" +localLongitude);
            return latLng;
        }


        return null;
    }

    double latitude;
    double longitude;

    private ILocationUpdateCallBack mLocationUpdateCallBack=new ILocationUpdateCallBack() {
        @Override
        public void onLocationUpdateCallBack(LocationInfo locationInfo) {
            if (locationInfo != null) {
                 latitude = locationInfo.getLatitude();
                 longitude = locationInfo.getLongitude();

                 if (count % 20 == 0) {// 避免打印太多
                     LogUtils.d(TAG, "onLocationUpdateCallBack : Lat: " + latitude + " Lng: " + longitude);
                 }
                count ++;
                if (latitude == -1 || latitude == 4.9E-324) {
                    LogUtils.d(TAG, "纬度获取失败");
                    return;
                }
                if (longitude == -1 || longitude == 4.9E-324) {
                    LogUtils.d(TAG, "经度获取失败");
                    return;
                }
                if (!mHandler.hasMessages(MSG)) {
                    mHandler.sendEmptyMessage(MSG);
                }
            }
        }
    };

    private static final int MSG = 100;

    int count = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG) {
                String localLatitude = SharedPreferencesUtils.getString(context, AppConstant.LATITUDE, "");
                String localLongitude = SharedPreferencesUtils.getString(context, AppConstant.LONGITUDE, "");
                if (TextUtils.isEmpty(localLatitude) || TextUtils.isEmpty(localLongitude) || !localLatitude.equals("" + latitude) || !localLongitude.equals("" + longitude)) {//如果本地没有保存或者保存的地址和实时获取的的经纬度不一样则保存并且上传讯飞
                    LogUtils.d(TAG, "重新保存");
                    SharedPreferencesUtils.saveString(context, AppConstant.LATITUDE, latitude + "");
                    SharedPreferencesUtils.saveString(context, AppConstant.LONGITUDE, longitude + "");
                    //更新位置给讯飞
                    if (SRAgent.getInstance().SrInstance != null) {
                        int ret1 = SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_LATITUDE, latitude + "");
                        int ret2 = SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_LONGTITUDE, longitude + "");

                        if (ret1 == 0 && ret2 == 0) {
                            LogUtils.d(TAG, "纬度设置成功。latitude=" + latitude
                                    + ",经度设置成功。longitude=" + longitude);
                        }
                    }
                }
                mHandler.sendEmptyMessageDelayed(MSG,5*1000);
            }
        }
    };

}
