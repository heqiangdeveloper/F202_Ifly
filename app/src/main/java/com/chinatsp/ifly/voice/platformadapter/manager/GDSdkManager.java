package com.chinatsp.ifly.voice.platformadapter.manager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.entity.MXPoiEntity;
import com.chinatsp.ifly.entity.PoiEntity;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.MyToast;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.MapController;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.example.mxextend.IExtendApi;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.listener.IExtendCallback;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.APPCENTERC1CONDITION;
import static com.chinatsp.ifly.voice.platformadapter.controller.MapController.COMPANYPOIT;
import static com.chinatsp.ifly.voice.platformadapter.controller.MapController.CURRENTPOI;
import static com.chinatsp.ifly.voice.platformadapter.controller.MapController.MSG_OPEN_NAVI;
import static com.chinatsp.ifly.voice.platformadapter.controller.MapController.MSG_SHOW_MYLOCATION;

/**
 * 控制高德地图管理类
 *
 * 接入高德后修改到的文件：
 * BaseApplication.java     MapController.java  MXSdkManager.java
 * SearchListFragment.java  IMapController.java  event_tracking.xml
 * AppControlManager.java  VoiceSettingsFragment.java fragment_voice_settings.xml
 *
 * @auther zhaokai
 * @date 2019/11/6 10:46
 **/
public class GDSdkManager {

    private final static String TAG=GDSdkManager.class.getSimpleName();
    private static GDSdkManager mInstance;
    private Context mContext;
    private Handler handler=new Handler();

    private final static String GDMapPackageName="com.autonavi.amapauto";//高德
    private final static String MXPackageName="com.tencent.wecarnavi";//美行
    private static boolean isGDNaving=false;//高德地图是否正在导航
    private int routeRemainDistance;//路径剩余距离，对应的值为 int 类型，单位：米
    private int routeRemainTime;//路径剩余时间，对应的值为 int 类型，单位：秒
    private boolean isCanEnlarge=true;//是否可以继续放大
    private boolean isCanReduce=true;//是否可以继续缩小
    public final static int ROUTE_PREFERENCE_ID_HIGH_SPEED=20;//高速优先
    public final static int ROUTE_PREFERENCE_ID_NO_SPEED=3;//不走高速
    public final static int ROUTE_PREFERENCE_ID_LESS_CHARGE=1;//避免收费
    public final static int ROUTE_PREFERENCE_ID_AVOIDING_CONGESTION=4;//躲避拥堵
    private int midPointCount;//途径点个数

    private static final int MSG_SPECIAL_NAVI = 1004;
    private IExtendApi extendApi;
    //    private int lastMuteStatusMX=1;//在打开高德前，美行播报状态：0 关闭 1：打开  其他code  异常
    private int lastMuteStatusGD=0;//在高德onStop时，高德的播报状态  0打开 1关闭
    private String KEY_MUTE_MX="KEY_MUTE_MX";

    public final static String KEY_VOICE_NAV="voice_nav";//语音导航设置
    public final static int VALUE_VOICE_NAV_DEFAULT=0;//系统默认（即美行地图）
    public final static int VALUE_VOICE_NAV_AMAP=1;//高德地图

    private int delayedCommand=0;//等高德打开后，再执行的指令
    private final static int COMMAND_HOME_COMPANY=1;
    private final static int COMMAND_NAV=2;
    private final static int COMMAND_OPEN_NAV=3;
    private final static int COMMAND_FULL_VIEW=4;
    private final static int COMMAND_ROUTE_PREFERENCE=5;

    private GDSdkManager(Context mContext) {
        this.mContext=mContext;
        TAExtendManager.getInstance().init(mContext);
        extendApi = TAExtendManager.getInstance().getApi();
    }

    public static GDSdkManager getInstance(Context mContext) {
        if (mInstance == null) {
            synchronized (GDSdkManager.class) {
                if (mInstance == null) {
                    mInstance = new GDSdkManager(mContext);
                }
            }
        }
        return mInstance;
    }

    /*****************************一、提供给语音项目调用的方法开始**********************************************/
    /**
     * 1.判断高德地图是否在前台显示
     */
    public boolean isGDForeground() {
        try {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(50);

            if (runningTaskInfos != null && runningTaskInfos.size() != 0) {

                //1.高德直接在栈顶
                String packageName0=(runningTaskInfos.get(0).baseActivity).getPackageName();
                LogUtils.d(TAG,"isGDForeground packageName0="+packageName0);
                if(GDMapPackageName.equals(packageName0)){
                    return true;
                }

                //2.判断高德和美行谁在更靠近栈顶
                for (int i = 0; i <runningTaskInfos.size() ; i++) {
                    String packageName=(runningTaskInfos.get(i).baseActivity).getPackageName();
                    LogUtils.d(TAG,"isGDForeground i="+i+" packageName="+packageName);
                    if(GDMapPackageName.equals(packageName)){
                        return true;
                    }else if(MXPackageName.equals(packageName)){
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 2.高德地图是否正在导航
     */
    public boolean isGDNaving() {
        return isGDNaving&&isProcessExist(mContext,GDMapPackageName);
    }

    /**
     * 3.注册高德地图的广播监听
     */
    public void registerGDReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("AUTONAVI_STANDARD_BROADCAST_SEND");
        mContext.registerReceiver(new GDReceiver(), filter);
    }

    /**
     * 4.回家/回公司
     * @param destType 0: 家 1：公司
     * @param source MVW: 由唤醒触发 SR：由识别触发
     * @param extendApi 美行api
     * @return true 美行不执行指令 （"语音导航"选中了"高德地图"或者高德在前台）   false 美行执行指令
     */
    private int destType;
    private int source;
    private MapController.MyHandler myHandler;
    public boolean goHomeOrCompany(final int destType,final int source,IExtendApi extendApi,MapController.MyHandler myHandler){
        LogUtils.d(TAG,"goHomeOrCompany() destType="+destType+" source="+source);
        this.destType=destType;
        this.source=source;
        this.extendApi=extendApi;
        this.myHandler=myHandler;

        switch (dispatchCommand(COMMAND_HOME_COMPANY)){
            case 1://由高德执行指令
                goHomeOrCompanyGD();
                return true;
            case 3://高德和美行都不执行指令
                return true;
            default://由美行执行指令
                return false;
        }

    }

    private void goHomeOrCompanyGD(){
        //获取在美行设置的家/公司的信息  （小欧语音 0: 家 1：公司  ， 美行 1: 家 2：公司  ，所以这里参数要+1）
        extendApi.getHomeOrCompanyData(destType+1, new IExtendCallback<LocationInfo>() {
            @Override
            public void success(final LocationInfo locationInfo) {
                LogUtils.d(TAG,"获取家/公司信息成功：locationInfo="+locationInfo.toString());

                //家/公司为空，播放未设置语音
                if(TextUtils.isEmpty(locationInfo.getName())||locationInfo.getLongitude()==0||locationInfo.getLatitude()==0){
                    homeOrCompanyNullTTS(destType,source,myHandler);
                    return;
                }

                //播放开始导航语音
                if (destType == 0) {//家
                    Utils.eventTrack(mContext,R.string.skill_navi_gd,R.string.scene_launch_navi,R.string.object_go_home,TtsConstant.NAVIC15CONDITION,R.string.condition_navi15);
                    Utils.getMessageWithTtsSpeak(mContext, TtsConstant.NAVIC15CONDITION, mContext.getString(R.string.map_navi_to_home), new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            //开始高德导航
                            naviToDestination(locationInfo.getName(),locationInfo.getLongitude(),locationInfo.getLatitude());
                            if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                                EventBusUtils.sendExitMessage();
                            }
                        }
                    });
                } else {//公司
                    final String companyName = locationInfo.getName();
                    final String defaultTTS = Utils.replaceTts(mContext.getString(R.string.map_navi_to_company), COMPANYPOIT,companyName);
                    Utils.eventTrack(mContext,R.string.skill_navi_gd,R.string.scene_launch_navi,R.string.object_go_to_company, TtsConstant.NAVIC17CONDITION,R.string.condition_navi17);
                    Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.NAVIC17CONDITION, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            String ttsText = tts;
                            if (TextUtils.isEmpty(tts)) {
                                ttsText = defaultTTS;
                            } else {
                                ttsText = Utils.replaceTts(tts, COMPANYPOIT, companyName);
                            }
                            Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                                @Override
                                public void onPlayStopped() {
                                    //开始高德导航
                                    naviToDestination(locationInfo.getName(),locationInfo.getLongitude(),locationInfo.getLatitude());
                                    if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                                        EventBusUtils.sendExitMessage();
                                    }
                                }
                            });
                        }
                    });
                }

                //给高德设置家/公司
                setHomeOrCompany(locationInfo.getName(),locationInfo.getAddress(),locationInfo.getLongitude(),locationInfo.getLatitude(),destType+1);
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                startTTS("",mContext.getString(R.string.sorry_plan_failed));
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }


    /**
     * 5.导航到目的地
     */
    private BaseEntity poiEntity;
    public boolean naviToPoi(BaseEntity poiEntity) {
        LogUtils.d(TAG,"naviToPoi()");
        this.poiEntity=poiEntity;
        switch (dispatchCommand(COMMAND_NAV)){
            case 1://由高德执行指令
                naviToPoiGD();
                return true;
            case 3://由高德和美行都不执行指令
                return true;
            default://由美行执行指令
                return false;
        }

    }

    private void naviToPoiGD(){
        LocationInfo navinLocInfo=new LocationInfo();
        if (poiEntity instanceof PoiEntity) {
            navinLocInfo.setName( ((PoiEntity) poiEntity).getName());
            navinLocInfo.setAddress( ((PoiEntity) poiEntity).getAddress());
            navinLocInfo.setLatitude(Double.valueOf( ((PoiEntity) poiEntity).getLatitude()));
            navinLocInfo.setLongitude(Double.valueOf( ((PoiEntity) poiEntity).getLongitude()));
        } else if (poiEntity instanceof MXPoiEntity) {
            navinLocInfo.setName( ((MXPoiEntity) poiEntity).getName());
            navinLocInfo.setAddress( ((MXPoiEntity) poiEntity).getAddress());
            navinLocInfo.setLatitude(Double.valueOf( ((MXPoiEntity) poiEntity).getLatitude()));
            navinLocInfo.setLongitude(Double.valueOf( ((MXPoiEntity) poiEntity).getLongitude()));
        }
        LogUtils.d(TAG, "locationInfo:"+navinLocInfo.toString());

        //开始高德导航
        naviToDestination(navinLocInfo.getName(),navinLocInfo.getLongitude(),navinLocInfo.getLatitude());
    }

    /**
     * 6.退出高德软件
     */
    public void exitGDApp(){
        LogUtils.d(TAG,"exitGD() 退出高德APP");
        exitGD(10021);
    }

    /**
     * 7.退出高德导航
     */
    public void exitNavi(){
        LogUtils.d(TAG,"exitNavi() 退出高德导航");
        exitGD(10010);
        isGDNaving=false;
    }

    /**
     * 8.还有多久到（还有多久） 还有多远到（还有多远）
     */
    public void howFar(){
        LogUtils.e(TAG, "howFar() routeRemainDistance=" + routeRemainDistance+"米  routeRemainTime="+routeRemainTime+"秒");
        if (routeRemainTime > 0 && routeRemainDistance > 0) {
            String tts = calcRemainTimeAndDistance(routeRemainTime, routeRemainDistance);
            startTTS(tts);
            Utils.eventTrack(mContext,R.string.skill_navi_gd, R.string.scene_navi_query, R.string.object_how_long, TtsConstant.NAVIC52CONDITION,R.string.condition_navi1);
        } else {
            FloatViewManager.getInstance(mContext).hide();
        }
    }

    /**
     * 9.我在哪里
     */
    public void showMyLocation(){
        LogUtils.d(TAG,"showMyLocation()");
        if(isGDForeground()){
            showMyLocationGD();
        }
    }

    /**
     * 10.2D模式（2D视图）
     */
    public boolean changeTo2D(){
        if(isGDForeground()){
            carUP2D();
            String defaultTts = mContext.getString(R.string.map_switch_2d_ok);
            String conditionId = TtsConstant.NAVIC58CONDITION;
            startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_2d_view, R.string.scene_navi_map_operation);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 11.3D模式（3D视图）
     */
    public boolean changeTo3D(){
        if(isGDForeground()){
            carUP3D();
            String defaultTts = mContext.getString(R.string.map_switch_3d_ok);
            String conditionId = TtsConstant.NAVIC61CONDITION;
            startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_3d_view, R.string.scene_navi_map_operation);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 12.车头朝上（只有2d）
     */
    public boolean changeToCarUP(){
        if(isGDForeground()){
            carUP2D();
            String defaultTts = mContext.getString(R.string.map_switch_headup_ok);
            String conditionId = TtsConstant.NAVIC62CONDITION;
            startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_head_up, R.string.scene_navi_map_operation);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 13.正北朝上（只有2d）
     */
    public boolean changeToNorthUP(){
        if(isGDForeground()){
            northUP2D();
            String defaultTts = mContext.getString(R.string.map_switch_northup_ok);
            String conditionId = TtsConstant.NAVIC64CONDITION;
            startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_upward_facing_north, R.string.scene_navi_map_operation);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 14.放大地图
     */
    public boolean enlargeMap(){
        if(isGDForeground()){
            if(isCanEnlarge){
                enlargeMapGD();
                String defaultTts = mContext.getString(R.string.map_zoom_out_ok);
                String conditionId = TtsConstant.NAVIC54CONDITION;
                startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_enlarge_map, R.string.scene_navi_map_operation);
            }else{
                String defaultTts = mContext.getString(R.string.map_zoom_out_fail);
                String conditionId = TtsConstant.NAVIC55CONDITION;
                startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_enlarge_map, R.string.scene_navi_map_operation);
            }
            return true;
        }else{
            return false;
        }
    }

    /**
     * 15.缩小地图
     */
    public boolean reduceMap(){
        if(isGDForeground()){
            if(isCanReduce){
                reduceMapGD();
                String defaultTts = mContext.getString(R.string.map_zoom_in_ok);
                String conditionId = TtsConstant.NAVIC56CONDITION;
                startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_reduce_map, R.string.scene_navi_map_operation);
            }else{
                String defaultTts = mContext.getString(R.string.map_zoom_in_fail);
                String conditionId = TtsConstant.NAVIC57CONDITION;
                startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_reduce_map, R.string.scene_navi_map_operation);
            }
            return true;
        }else{
            return false;
        }
    }

    /**
     * 14.打开路况
     */
    public boolean openRoad(){
        if(isGDForeground()){
            openRoadGD();
            String defaultTts = mContext.getString(R.string.map_road_condition_opened);
            String conditionId = TtsConstant.NAVIC70CONDITION;
            startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_open_road, R.string.scene_navi_map_operation);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 15.关闭路况
     */
    public boolean closeRoad(){
        if(isGDForeground()){
            closeRoadGD();
            String defaultTts = mContext.getString(R.string.map_road_condition_closed);
            String conditionId = TtsConstant.NAVIC71CONDITION;
            startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_close_road, R.string.scene_navi_map_operation);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 16.路线偏好设置
     */
    private int rf_type;
    private String rf_defaultTts;
    private String rf_conditionId;
    private int rf_objId;
    private int rf_scene;

    public boolean routePreference(int type,String defaultTts,String conditionId,int objId, int scene,boolean isMxNaving){
        rf_type=type;
        rf_defaultTts=defaultTts;
        rf_conditionId=conditionId;
        rf_objId=objId;
        rf_scene=scene;
        boolean isGDNaving=isGDNaving();
        if(isGDForeground()){
            if(isGDNaving){
                routePreferenceGD();
            }else{
                startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);
            }
            return true;
        }else{
            if(isGDNaving&&!isMxNaving){//高德在后台，高德在导航并且美行没导航时，打开高德执行高德
                go2GD(COMMAND_ROUTE_PREFERENCE);
                return true;
            }else{
                return false;
            }
        }
    }

    private void routePreferenceGD(){
        routePreferenceGD(rf_type);
        startNaviTTSAndEventTrack(rf_defaultTts, rf_conditionId, rf_objId, rf_scene);
    }


    /**
     * 17.关闭播报
     */
    public boolean closeBroadcast(){
        if(isGDForeground()){
            setCasualMute(1);
            startNaviTTS(mContext.getString(R.string.map_close_navi_volume), "");
            //播报状态：0 关闭 1：打开  其他code  异常
            LogUtils.d(TAG, "volumeMut:" + extendApi.isVolumeMute());
            Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_close_speak,TtsConstant.MHXC18CONDITION,R.string.condition_null,mContext.getString(R.string.map_close_navi_volume));
            return true;
        }else{
            return false;
        }

    }

    /**
     * 18.打开播报
     */
    public boolean openBroadcast(){
        if(isGDForeground()){
            setCasualMute(0);
            startNaviTTS(mContext.getString(R.string.map_open_navi_volume), "");
            //播报状态：0 关闭 1：打开  其他code  异常
            Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_open_speak,TtsConstant.MHXC19CONDITION,R.string.condition_null,mContext.getString(R.string.map_open_navi_volume));
            LogUtils.d(TAG, "volumeMut:" + extendApi.isVolumeMute());
            return true;
        }else{
            return false;
        }

    }

    /**
     * 19.获取途径点个数（每次开始导航时，GDReceiver中会返回）
     */
    public int getPassPointCount(){
        return midPointCount;
    }

    /**
     * 20.发起沿途搜索（搜索结果在GDReceiver中返回）
     */
    public boolean alongTheWaySearch(int type){
        if(isGDForeground()){
            if(isGDNaving()){
                alongTheWaySearchGD(type);
            }else{
                startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);
            }
            return true;
        }else{
            return false;
        }
    }

    /**
     * 21.添加途经点
     */
    public boolean addPassPoint(String name,String lon,String lat){
        if(isGDForeground()){
            if(isGDNaving()){
                double lonD=Double.valueOf(lon);
                double latD=Double.valueOf(lat);
                addPassPointGD(name,lonD,latD);
            }else{
                startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);
            }
            return true;
        }else{
            return false;
        }
    }

    /**
     * 22.添加收藏
     */
    public boolean addCollection(){
        if(isGDForeground()){
            addCollectionGD();
            //tts播报“好的”，然后隐藏小欧
            startTTS(mContext.getString(R.string.msg_tts_isnull));
            Utils.eventTrack(mContext,R.string.skill_navi_gd, R.string.skill_navi_gd, R.string.scene_navi_control,TtsConstant.NAVIC49CONDITION,R.string.condition_navi1);
            return true;
        }else{
            return false;
        }
    }

    /**
     * 23.查看全局
     */
    public boolean fullView(boolean isMxNaving){
        boolean isGDNaving=isGDNaving();
        if(isGDForeground()){
            if(isGDNaving){
                fullViewGD();
            }else{
                startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);
            }
            return true;
        }else{
            if(isGDNaving&&!isMxNaving){//高德在后台，高德在导航并且美行没导航时，打开高德执行高德
                go2GD(COMMAND_FULL_VIEW);
                return true;
            }else{
                return false;
            }
        }
    }

    private void fullViewGD(){
        fullViewGD(0);
        startNaviTTS(mContext.getString(R.string.map_start_navi_success), TtsConstant.NAVIC49CONDITION);
        Utils.eventTrack(mContext,R.string.skill_navi_gd, R.string.object_view_global, R.string.scene_navi_query, TtsConstant.NAVIC49CONDITION,R.string.condition_navi1);
    }

    /**
     * 24.发送高德关闭时的广播
     */
    public void sendCloseBroadcast(){
        sendGDStopNavBroadcast();
        sendGDOnStopBroadcast();
    }

    /**
     * 25.打开导航
     */
    private String openNavi_conditionId;
    private String openNavi_defaultTts;
    public boolean openNavi(String conditionId, String defaultTts,MapController.MyHandler myHandler,boolean isMxNaving){
        LogUtils.d(TAG,"openNavi()");
        this.openNavi_conditionId=conditionId;
        this.openNavi_defaultTts=defaultTts;
        this.myHandler=myHandler;

        switch (dispatchCommand(COMMAND_OPEN_NAV)){
            case 1://由高德执行指令
                openNavi();
                return true;
            case 3://高德和美行都不执行指令
                return true;
            default://由美行执行指令
                if(isGDNaving&&!isMxNaving){//高德在导航并且美行没导航时，打开高德
                    openNavi();
                    return true;
                }else{
                    return false;
                }
        }

    }
    private void openNavi(){
        LogUtils.d(TAG,"openNavi() 打开高德");
        AppControlManager.getInstance(BaseApplication.getInstance()).startApp("高德");
        Utils.getMessageWithoutTtsSpeak(mContext, openNavi_conditionId, openNavi_defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                if (TextUtils.isEmpty(tts)) {
                    tts = openNavi_defaultTts;
                }
                Message msg = myHandler.obtainMessage(MSG_OPEN_NAVI, tts);
                myHandler.sendMessageDelayed(msg, 1000);

                Utils.eventTrack(mContext,R.string.skill_navi_gd,R.string.scene_launch_navi,R.string.object_unspecified_search,openNavi_conditionId,R.string.condition_navi2, tts);
            }
        });
    }

    /*****************************一、提供给语音项目调用的方法结束**********************************************/





    /*****************************二、调用高德的方法开始*****************************************************/
    /**
     * GD1.开始高德导航
     */
    private void naviToDestination(String poiName,double lon,double lat){
        LogUtils.d(TAG,"naviToDestination() poiName="+poiName+" lon="+lon+" lat="+lat);

//        //隐藏小欧  播报同时开始导航，不能马上隐藏
//        if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
//            EventBusUtils.sendExitMessage();
//        }

        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10038);
        intent.putExtra("POINAME",poiName);
        intent.putExtra("LAT",lat);
        intent.putExtra("LON",lon);
        intent.putExtra("DEV",0);//起终点是否偏移 0（不需要国测加密） 1（需要国测加密）
        intent.putExtra("STYLE",0);
        intent.putExtra("SOURCE_APP","Third App");
        mContext.sendBroadcast(intent);
    }

    /**
     * GD2.退出高德
     * @param type  10021退出软件  10010退出导航
     */
    private void exitGD(int type){
        LogUtils.d(TAG,"exitGD() 发送退出广播 type="+type);
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", type);
        mContext.sendBroadcast(intent);

        //tts播报“好的”，然后隐藏小欧
        startTTS(mContext.getString(R.string.msg_tts_isnull));
    }

    /**
     * GD3.给高德设置家/公司
     * @param EXTRA_TYPE 1家  2公司
     */
    private void setHomeOrCompany(String name,String address,double lon,double lat,int EXTRA_TYPE){
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10058);
        intent.putExtra("LAT", lat);
        intent.putExtra("LON", lon);
        intent.putExtra("POINAME", name);
        intent.putExtra("ADDRESS", address);
        intent.putExtra("EXTRA_TYPE", EXTRA_TYPE);
        intent.putExtra("DEV",0);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD4.查看我的位置(在高德地图左侧显示我的位置详情，并且地图移动到当前定位)
     */
    private void showMyLocationGD(){
        LogUtils.d(TAG,"showMyLocationGD()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10008);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD5.2D车上
     */
    private void carUP2D(){
        LogUtils.d(TAG,"carUP2D()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE",2);
        intent.putExtra("EXTRA_OPERA",0);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD6.2D北上
     */
    private void northUP2D(){
        LogUtils.d(TAG,"northUP2D()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE",2);
        intent.putExtra("EXTRA_OPERA",1);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD7.3D车上
     */
    private void carUP3D(){
        LogUtils.d(TAG,"carUP3D()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE",2);
        intent.putExtra("EXTRA_OPERA",2);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD8.放大地图
     */
    private void enlargeMapGD(){
        LogUtils.d(TAG,"enlargeMapGD()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE",1);
        intent.putExtra("EXTRA_OPERA",0);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD9.缩小地图
     */
    private void reduceMapGD(){
        LogUtils.d(TAG,"reduceMapGD()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE",1);
        intent.putExtra("EXTRA_OPERA",1);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD10.开启实时路况
     */
    private void openRoadGD(){
        LogUtils.d(TAG,"openRoad()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE",0);
        intent.putExtra("EXTRA_OPERA",0);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD11.关闭实时路况
     */
    private void closeRoadGD(){
        LogUtils.d(TAG,"closeRoad()");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10027);
        intent.putExtra("EXTRA_TYPE",0);
        intent.putExtra("EXTRA_OPERA",1);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD12.路线偏好设置（导航场景下）
     *避免收费` |`1`
     `多策略算路` |`2`
     `不走高速` |`3`
     `躲避拥堵` |`4`
     `不走高速且避免收费` |`5`
     `不走高速且躲避拥堵` |`6`
     `躲避收费和拥堵` |`7`
     `不走高速躲避收费和拥堵` |`8`
     `高速优先` |`20`
     `躲避拥堵且高速优先` |`24`
     `地图内部默认算路原则`|`-1`
     */
    private void routePreferenceGD(int type){
        LogUtils.d(TAG,"routePreference() type="+type);
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10005);
        intent.putExtra("NAVI_ROUTE_PREFER", type);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD13.设置静音（永久）
     * @param value  0 取消静音  1开启静音
     */
    private void setCasualMute(int value){
        LogUtils.d(TAG,"setCasualMute() 打开(0)/关闭(1)高德播报 value="+value);
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10047);
        intent.putExtra("EXTRA_MUTE", value);//永久静音
        intent.putExtra("EXTRA_CASUAL_MUTE", value);//临时静音
        mContext.sendBroadcast(intent);
    }

    /**
     *  GD14.发起沿途搜索
     * @param type 1 : 卫生间 2 : ATM 3 : 维修站 4 : 加油站
     */
    private void alongTheWaySearchGD(int type){
        LogUtils.d(TAG,"alongTheWaySearchGD() 发起沿途搜索 type="+type);
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10057);
        intent.putExtra("EXTRA_SEARCHTYPE", type);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD15.添加途经点
     */
    private void addPassPointGD(String name,double lon,double lat){
        LogUtils.d(TAG,"addPassPointGD() name="+name+" lon="+lon+" lat="+lat);
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 12104);
        intent.putExtra("EXTRA_NAVI_VIA_MODIFY", 1);//0: 删除全部途径点 1: 增加一个途径点 2: 删除一个途径点
        intent.putExtra("EXTRA_MIDNAME", name);
        intent.putExtra("EXTRA_MIDLON", lon);
        intent.putExtra("EXTRA_MIDLAT", lat);
        mContext.sendBroadcast(intent);
    }


    /**
     * GD16.收藏当前位置
     */
    private void addCollectionGD(){
        LogUtils.d(TAG,"addCollection() 收藏当前位置");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 11003);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD17.全览
     * @param isShow  0 进入全览 1 退出全览
     */
    private void fullViewGD(int isShow){
        LogUtils.d(TAG,"fullViewGD() 全览 isShow="+isShow);
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10006);
        intent.putExtra("EXTRA_IS_SHOW", isShow);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD18.发送高德结束导航广播
     */
    private void sendGDStopNavBroadcast(){
        LogUtils.d(TAG,"sendStopNavBroadcast() 发送高德结束导航广播");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_SEND");
        intent.putExtra("KEY_TYPE", 10019);
        intent.putExtra("EXTRA_STATE", 9);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD19.发送高德退到后台广播
     */
    private void sendGDOnStopBroadcast(){
        LogUtils.d(TAG,"sendOnStopBroadcast() 发送高德退到后台广播");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_SEND");
        intent.putExtra("KEY_TYPE", 10019);
        intent.putExtra("EXTRA_STATE", 4);
        mContext.sendBroadcast(intent);
    }

    /**
     * GD20.发送高德 获取当前静音状态 广播
     */
    private void sendGDGetMuteStatusBroadcast(){
        LogUtils.d(TAG,"sendGDGetMuteStatusBroadcast() 发送高德广播 获取当前静音状态 ");
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10071);
        mContext.sendBroadcast(intent);
    }


    /*****************************二、调用高德的方法结束*****************************************************/






    /*****************************三、其它的方法开始*****************************************************/

    /**
     * 判断进程是否存活
     * @param progressName 进程名称
     */
    private static boolean isProcessExist(Context context, String progressName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists ;
        if (am != null) {
            lists = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : lists) {
                if (progressName.equals(appProcess.processName)) {
                    return true;
                }
            }
        }
        isGDNaving=false;//如果高德进程不存在，则设置是否正在导航为false
        return false;
    }

    /**
     * 语音导航指令分发
     * @return  1 由高德执行导航指令
     *          2 由美行执行导航指令
     *          3 高德和美行都暂不执行导航指令
     */
    private int dispatchCommand(int command){
        LogUtils.d(TAG,"dispatchCommand() command="+command);
        if(isVoiceNavGD()){//"语音导航"选中的是"高德地图"
            if(isGDInstalled()){//已安装高德
                if(isGDForeground()){//高德已打开，高德执行指令
                    return 1;
                }else{//高德未打开，打开高德后，再执行指令
                    go2GD(command);
                    return 3;
                }
            }else{//未安装高德 跳转应用中心
                go2AppStore();
                return 3;
            }
        }else if(isGDForeground()){//高德在前台，高德执行指令
            return 1;
        }else{//美行执行指令
            return 2;
        }
    }

    /**
     * "语音导航"是否选中的是"高德地图"
     */
    private boolean isVoiceNavGD(){
//        int voice_nav= SharedPreferencesUtils.getInt(mContext, KEY_VOICE_NAV, VALUE_VOICE_NAV_DEFAULT);
        int voice_nav=Settings.Global.getInt(mContext.getContentResolver(),KEY_VOICE_NAV,VALUE_VOICE_NAV_DEFAULT);
        return VALUE_VOICE_NAV_AMAP==voice_nav;
    }

    /**
     * 是否已安装高德地图
     */
    private boolean isGDInstalled(){
        return AppControlManager.getInstance(mContext).appIsExistByName(mContext,"高德");
    }

    /**
     * 未安装高德地图 跳转应用中心
     */
    private void go2AppStore(){
        AppControlManager.getInstance(BaseApplication.getInstance()).startApp("应用中心");
        MyToast.showToast(mContext,"请先下载高德地图", true);
        startTTS("请先下载高德地图");
        //隐藏小欧
        if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
            EventBusUtils.sendExitMessage();
        }
    }

    /**
     * 跳转高德
     */
    private void go2GD(int command){
        AppControlManager.getInstance(BaseApplication.getInstance()).startApp("高德");
//        startTTS("正在打开高德地图");//这里不能播报，因为打开高德后，执行导航指令，会有其他的播报，会导致这个播报音播报不全。

        //隐藏小欧  //这里不能隐藏小欧，因为打开高德后，有时候还需要回复小欧。例如 说"导航到"，启动高德后，还需要回复小欧 "导航到哪里"
//        if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
//            EventBusUtils.sendExitMessage();
//        }

        //等高德打开后，再执行的指令
        delayedCommand=command;
    }

    public void startTTS(String defaultTts, String conditionId, TTSController.OnTtsStoppedListener listener) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }
                Utils.startTTS(ttsText, listener);
            }
        });
    }

    public void startTTS(String conditionId,String defaultTts) {
        Utils.getMessageWithTtsSpeak(mContext, conditionId, defaultTts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                    EventBusUtils.sendExitMessage();
                }
            }
        });
    }

    public void startTTS(String defaultTts) {
        startNaviTTS(defaultTts,"");
    }

    private void startNaviTTSAndEventTrack(String defaultTts, String conditionId, int objId, int scene) {
        startTTS(conditionId,defaultTts);
        Utils.eventTrack(mContext,R.string.skill_navi_gd, scene, objId,conditionId,R.string.condition_navi1);
    }

    private void startNaviTTS(String defaultTts, String conditionId) {
        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultTts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                    EventBusUtils.sendExitMessage();
                }
            }
        });
    }

    private String calcRemainTimeAndDistance(int remainTime, int remainDistance) {
        String formatTimeS = formatTimeS(remainTime);
        String s = String.format("距离目的地还有%d%s", remainDistance, "米,") + formatTimeS;
        Log.e("zheng","zheng111111111111 calcRemainTimeAndDistance"+s);
        if (remainDistance < 1000) {
            return String.format("距离目的地还有%d%s", remainDistance, "米,") + formatTimeS;
        } else {
            return String.format("距离目的地还有%.1f%s", remainDistance/100*100 / 1000.0f, "公里,") + formatTimeS;//高德地图没有四舍五入，而是直接舍去了十位和个位，所以这里/100*100
        }
    }

    private static String formatTimeS(long seconds) {
        Calendar calendar = Calendar.getInstance();
        LogUtils.d(TAG, DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar.getTime()).toString());
        int oriDay = calendar.get(Calendar.DAY_OF_MONTH);
        long millis = calendar.getTimeInMillis() + seconds * 1000;
        calendar.setTimeInMillis(millis);
        LogUtils.d(TAG, DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar.getTime()).toString());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        StringBuffer sb = new StringBuffer();
        if (day - oriDay == 1) {
            sb.append("预计明天").append(hour).append("点").append(min).append("分到达");
        } else if (day - oriDay == 2) {
            sb.append("预计后天").append(hour).append("点").append(min).append("分到达");
        } else if (day == oriDay) {
            sb.append("预计").append(hour).append("点").append(min).append("分到达");
        }
        return sb.toString();
    }

    /**
     * 家/公司未设置，播放语音
     */
    private void homeOrCompanyNullTTS(final int destType,final int source,final MapController.MyHandler myHandler){
        LogUtils.d(TAG, "homeOrCompanyNull()");

        //未设置家的地址 || 未设置公司的地址
        if (FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
            FloatViewManager.getInstance(mContext).show(FloatViewManager.WARE_BY_OTHER);
        }
        final String mainMsg;
        String conditionId = "";
        int condition;
        int object;
        if (destType == 0) {//家
            conditionId = TtsConstant.NAVIC14CONDITION;
            mainMsg = mContext.getString(R.string.map_no_set_home_site);
            condition=R.string.condition_navi14;
            object=R.string.object_go_home;
        } else {//公司
            conditionId = TtsConstant.NAVIC16CONDITION;
            mainMsg = mContext.getString(R.string.map_no_set_company_site);
            condition=R.string.condition_navi16;
            object=R.string.object_go_to_company;
        }

        Utils.eventTrack(mContext,R.string.skill_navi_gd,R.string.scene_launch_navi,object,conditionId,condition);
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = mainMsg;
                }

                Message msg = myHandler.obtainMessage(MSG_SPECIAL_NAVI, ttsText);
                Bundle data = new Bundle();
                data.putInt("destType", destType);
                data.putInt("source", source);
                msg.setData(data);
                myHandler.sendMessage(msg);
            }
        });

    }


    /**
     * 监听高德发出的广播
     */
    class GDReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle!=null){
                int type = bundle.getInt("KEY_TYPE");
                int state = bundle.getInt("EXTRA_STATE");
//                LogUtils.d(TAG,"监听到高德导航状态  type="+type+"  state="+state);
                if(10019==type){//地图状态广播
                    switch (state){
                        case 0://启动Application
                            LogUtils.d(TAG,"监听到高德导航状态  启动Application state="+state);
                            break;
                        case 3://进入前台 onStart
                            LogUtils.d(TAG,"监听到高德导航状态  进入前台onStart state="+state);
                            //执行延迟命令（设置了优先高德，当语音控制导航时，先打开高德，再执行命令）
                            doDelayCommand();
                            //关闭美行播报
                            if(extendApi!=null){
                                int lastMuteStatusMX= SharedPreferencesUtils.getInt(mContext, KEY_MUTE_MX, -1);
                                if(lastMuteStatusMX!=1&&!isGDNaving()){
                                    //条件1.高德未导航，才保存美行的播报状态（因为如果高德在导航，那么上一次高德退到后台时，并没有恢复美行播报状态）
                                    //条件2.高德开始导航，退到后台，如果通过发起导航的方式再次启动，高德会先执行一次退出导航，从而让条件1的判断通过，导致保存了美行播报关闭的状态。所以再增加一个条件lastMuteStatusMX!=1
                                    lastMuteStatusMX=extendApi.isVolumeMute();//美行播报状态：0 关闭 1：打开  其他code  异常
                                    SharedPreferencesUtils.saveInt(mContext, KEY_MUTE_MX, lastMuteStatusMX);
                                    LogUtils.d(TAG,"保存美行播报状态 lastMuteStatusMX="+lastMuteStatusMX);
                                }
                                extendApi.setVolumeMute(true, new IExtendCallback() {
                                    @Override
                                    public void success(ExtendBaseModel extendBaseModel) {
                                        LogUtils.d(TAG,"关闭美行播报  success");
                                    }

                                    @Override
                                    public void onFail(ExtendErrorModel extendErrorModel) {
                                        LogUtils.d(TAG,"关闭美行播报 onFail");
                                    }

                                    @Override
                                    public void onJSONResult(JSONObject jsonObject) {
                                        LogUtils.d(TAG,"关闭美行播报 onJSONResult jsonObject="+jsonObject.toString());
                                    }
                                });
                            }
                            //获取之前高德的播报状态，如果是0(打开)就打开高德播报
                            if(lastMuteStatusGD==0){
                                setCasualMute(0);//打开高德播报
                            }
                            break;
                        case 4://进入后台 onStop
                            LogUtils.d(TAG,"监听到高德导航状态  进入后台onStop state="+state);
                            //打开美行播报
                            openMXBroadCast();
                            break;
                        case 8://开始导航
                        case 10://开始模拟导航
                        case 11://暂停模拟导航
                            isGDNaving=true;
                            LogUtils.d(TAG,"监听到高德导航状态   开始导航 state="+state+"  isGDNaving="+isGDNaving);
                            break;
                        case 9://结束导航
                        case 12://停止模拟导航
                        case 39://到达目的地通知
                            isGDNaving=false;
                            LogUtils.d(TAG,"监听到高德导航状态  结束导航 state="+state+"  isGDNaving="+isGDNaving);
                            break;
                    }
                }else if(10001==type){//引导信息广播
                    routeRemainDistance=bundle.getInt("ROUTE_REMAIN_DIS");
                    routeRemainTime=bundle.getInt("ROUTE_REMAIN_TIME");
                    LogUtils.d(TAG,"监听到高德引导信息  routeRemainDistance="+routeRemainDistance+"  routeRemainTime="+routeRemainTime);
                }else if(10074==type){//比例尺放大缩小通知
                    int zoomType=bundle.getInt("EXTRA_ZOOM_TYPE");//0 放大 1 缩小
                    boolean isCanZoom=bundle.getBoolean("EXTRA_CAN_ZOOM",true);//是否能继续缩放
                    switch (zoomType){
                        case 0:
                            isCanEnlarge=isCanZoom;
                            isCanReduce=true;
                            break;
                        case 1:
                            isCanReduce=isCanZoom;
                            isCanEnlarge=true;
                    }
                    LogUtils.d(TAG,"监听到高德比例尺放大缩小通知  zoomType="+zoomType+"  isCanZoom="+isCanZoom+" isCanEnlarge="+isCanEnlarge+" isCanReduce="+isCanReduce);
                }else if(10056==type){//路径规划完后，高德地图会发送当前道路的信息给第三方系统
                    String json=bundle.getString("EXTRA_ROAD_INFO");
                    LogUtils.d(TAG,"监听到高德路径规划完后的道路信息  json="+json);
                    try {
                        JSONObject jsonObject= new JSONObject(json);
                        midPointCount=jsonObject.getInt("midPoisNum");//中途点个数
                        LogUtils.d(TAG,"高德中途点个数  midPointCount="+midPointCount);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(10057==type){//监听沿途搜索返回的结果
                    String json=bundle.getString("EXTRA_SEARCH_ALONG_THE_WAY");
                    LogUtils.d(TAG,"监听沿途搜索返回的结果  json="+json);
                    try {
                        AlongTheWayBean alongTheWayBean = new Gson().fromJson(json, AlongTheWayBean.class);
                        //该功能暂停开发...
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(10072==type){//监听 获取高德当前静音（播报）状态 返回的结果
                    int mute=bundle.getInt("EXTRA_MUTE");//0:取消永久静音(打开播报)；1；永久静音(关闭播报)
                    int casualMute=bundle.getInt("EXTRA_CASUAL_MUTE");//0:取消临时静音(打开播报)；1；临时静音 (关闭播报)
                    LogUtils.d(TAG,"监听 获取高德当前静音（播报）状态 返回的结果  0打开播报/1关闭播报 永久静音mute="+mute+" 临时静音casualMute="+casualMute);
                    try {
                        //记录高德播报状态
                        lastMuteStatusGD=mute;
                        //关闭高德播报
                        setCasualMute(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 打开美行播报
     */
    public void openMXBroadCast(){
        LogUtils.d(TAG,"openMXBroadCast()");

        if(extendApi!=null){
            int lastMuteStatusMX= SharedPreferencesUtils.getInt(mContext, KEY_MUTE_MX, -1);
            LogUtils.d(TAG,"获取保存的美行播报状态 lastMuteStatusMX="+lastMuteStatusMX);
            if(lastMuteStatusMX==1){//1.如果在启动高德之前，美行播报是打开的，则在退出高德时，才打开美行播报
                boolean isGDNaving=isGDNaving();
                LogUtils.d(TAG,"isGDNaving="+isGDNaving);

                if(!isGDNaving){//2.如果高德退到后台，并且未导航，才打开美行播报

                    //3. 测试发现，在启动高德后，也有可能触发美行的页面切换监听，从而去执行恢复美行关闭高德的操作，导致高德播报被关闭，所以这里增加一个判断。
                    if(isGDForeground()){
                        return;
                    }

                    LogUtils.d(TAG,"打开美行播报");
                    extendApi.setVolumeMute(false, new IExtendCallback() {
                        @Override
                        public void success(ExtendBaseModel extendBaseModel) {
                            LogUtils.d(TAG,"打开美行播报  success");
                            SharedPreferencesUtils.saveInt(mContext, KEY_MUTE_MX, -1);
                        }

                        @Override
                        public void onFail(ExtendErrorModel extendErrorModel) {
                            LogUtils.d(TAG,"打开美行播报 onFail");
                        }

                        @Override
                        public void onJSONResult(JSONObject jsonObject) {
                            LogUtils.d(TAG,"打开美行播报 onJSONResult jsonObject="+jsonObject.toString());
                        }
                    });

                    //发送广播给高德，获取高德当前静音（播报）状态，收到广播回调后，记录高德播报状态并关闭高德播报
                    sendGDGetMuteStatusBroadcast();
                }
            }
        }
    }

    /**
     * 执行延迟命令（说完指令，等高德打开后，再执行指令）
     */
    private void doDelayCommand(){
        switch (delayedCommand){
            case COMMAND_HOME_COMPANY:
                goHomeOrCompanyGD();
                break;
            case COMMAND_NAV:
                naviToPoiGD();
                break;
            case COMMAND_OPEN_NAV:
                openNavi();
                break;
            case COMMAND_FULL_VIEW:
                fullViewGD();
                break;
            case COMMAND_ROUTE_PREFERENCE:
                routePreferenceGD();
                break;
        }
        delayedCommand=0;
    }

    /**
     * 沿途搜索返回的结果bean
     */
    public static class AlongTheWayBean{
        private int search_result_size;//沿途搜索结果个数
        private int search_type;//沿途搜索类别 1：WC；2：ATM；3：维修站；4：加油站
        private int poi_distance;//单位（米）
        private List<PoiInfoBean> poi_info;

        public int getSearch_result_size() {
            return search_result_size;
        }

        public void setSearch_result_size(int search_result_size) {
            this.search_result_size = search_result_size;
        }

        public int getSearch_type() {
            return search_type;
        }

        public void setSearch_type(int search_type) {
            this.search_type = search_type;
        }

        public int getPoi_distance() {
            return poi_distance;
        }

        public void setPoi_distance(int poi_distance) {
            this.poi_distance = poi_distance;
        }

        public List<PoiInfoBean> getPoi_info() {
            return poi_info;
        }

        public void setPoi_info(List<PoiInfoBean> poi_info) {
            this.poi_info = poi_info;
        }
    }

    public static class PoiInfoBean{
        private String poi_Longitude;//经度
        private String poi_distance;//距离 米
        private String poi_Latitude;//维度
        private String poi_addr;//地址
        private String poi_name;//名称

        public String getPoi_Longitude() {
            return poi_Longitude;
        }

        public void setPoi_Longitude(String poi_Longitude) {
            this.poi_Longitude = poi_Longitude;
        }

        public String getPoi_distance() {
            return poi_distance;
        }

        public void setPoi_distance(String poi_distance) {
            this.poi_distance = poi_distance;
        }

        public String getPoi_Latitude() {
            return poi_Latitude;
        }

        public void setPoi_Latitude(String poi_Latitude) {
            this.poi_Latitude = poi_Latitude;
        }

        public String getPoi_addr() {
            return poi_addr;
        }

        public void setPoi_addr(String poi_addr) {
            this.poi_addr = poi_addr;
        }

        public String getPoi_name() {
            return poi_name;
        }

        public void setPoi_name(String poi_name) {
            this.poi_name = poi_name;
        }
    }

    /*****************************三、其它的方法结束*****************************************************/



}
