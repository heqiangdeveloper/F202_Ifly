package com.chinatsp.ifly.voice.platformadapter.manager;
import android.annotation.SuppressLint;
import android.car.CarNotConnectedException;
import android.car.hardware.constant.MCU;
import android.car.hardware.mcu.CarMcuManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.chinatsp.ifly.ActiveServiceViewManager;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.entity.AllDimension;
import com.chinatsp.ifly.entity.CommandEvent;
import com.chinatsp.ifly.entity.ReplaceTtsEntity;
import com.chinatsp.ifly.source.Constant;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AllDimensionUtils;
import com.chinatsp.ifly.utils.AppConfig;
import com.chinatsp.ifly.utils.ConstantsApp;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.MVW_WORDS;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.MapController;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.entity.InstructionEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.example.mxextend.ExtendConstants;
import com.example.mxextend.IExtendApi;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.entity.ExtendTAStatusModel;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.entity.RouteSummaryModel;
import com.example.mxextend.entity.SearchResultModel;
import com.example.mxextend.listener.IExtendCallback;
import com.example.mxextend.listener.IExtendListener;
import com.example.mxextend.listener.IPageChangedListener;
import com.example.mxextend.listener.ISearchResultListener;
import com.example.mxextend.listener.ISuggestionResultListener;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.util.NetworkUtil;
import com.iflytek.sr.SrSession;
import com.mxnavi.busines.entity.PageOpreaData;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import static android.car.VehicleAreaType.VEHICLE_AREA_TYPE_GLOBAL;
import static com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager.ROUTE_PREFERENCE_ID_AVOIDING_CONGESTION;
import static com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager.ROUTE_PREFERENCE_ID_HIGH_SPEED;
import static com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager.ROUTE_PREFERENCE_ID_LESS_CHARGE;
import static com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager.ROUTE_PREFERENCE_ID_NO_SPEED;

public class MXSdkManager implements IExtendCallback, MVW_WORDS {

    private static final String TAG = "MXSdkManager";

//    public boolean isNaving = false;  //正在导航

    public int currentPage = 0;
    private static final int PAGE_FU_FRAGMENT_SHOW = 1;//首页可见
    private static final int PAGE_FU_FRAGMENT_HIDE = 2;//首页不可见

    private static final int PAGE_SEARCH_FRAGMENT_SHOW = 3;//检索页可见
    private static final int PAGE_SEARCH_FRAGMENT_HIDE = 4;//检索页不可见

    private static final int PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW = 5;//检索结果页可见
    private static final int PAGE_SEARCHRESULT_LIST_FRAGMENT_HIDE = 6;//检索结果页不可见

    private static final int PAGE_MULTIROUTE_FRAGMENT_SHOW = 7;//多路线页面可见
    private static final int PAGE_MULTIROUTE_FRAGMENT_HIDE = 8;//多路线页面不可见

    private static final int PAGE_ROUTEGUIDE_FRAGMENT_SHOW = 9;//导航页可见
    private static final int PAGE_ROUTEGUIDE_FRAGMENT_HIDE = 10;//导航页不可见

    private static final int PAGE_ROUTEPASS_FRAGMENT_SHOW = 11;//添加途径地页面可见
    private static final int PAGE_ROUTEPASS_FRAGMENT_HIDE = 12;//添加途径地页面不可见

    private static final int PAGE_POINTSELECT_FRAGMENT_SHOW = 13;//地图选点页面可见
    private static final int PAGE_POINTSELECT_FRAGMENT_HIDE = 14;//地图选点页面不可见

    //注：（设置页面有两个入口，1首页进入设置页面；2导航中进入设置页面）
    private static final int PAGE_SETTING_FRAGMENT_SHOW = 15;//设置页面可见
    private static final int PAGE_SETTING_FRAGMENT_HIDE = 16;//设置页面不可见

    private static final int VIEW_PICK_UP_SHOW = 17;//首页拾取页面可见
    private static final int VIEW_PICK_UP_HIDE = 18;//首页拾取页面不可见

    private static final int VIEW_MULTIROUTE_DETAIL_SHOW = 19;//多路线-路线详情页可见
    private static final int VIEW_MULTIROUTE_DETAIL_HIDE = 20;//多路线-路线详情页不可见

    private static final int VIEW_MULTIROUTE_PREFERENCE_SHOW = 21;//多路线-路线偏好页可见
    private static final int VIEW_MULTIROUTE_PREFERENCE_HIDE = 22;//多路线-路线偏好页不可见

    private static final int VIEW_MULTIROUTE_ROUTEPASS_SHOW = 23;//多路线-添加途径地页可见
    private static final int VIEW_MULTIROUTE_ROUTEPASS_HIDE = 24;//多路线-添加途径地页不可见

    private static final int PAGE_NEARBY_FRAGMENT_SHOW = 25;//周边检索页面可见
    private static final int PAGE_NEARBY_FRAGMENT_HIDE = 26;//周边检索页面不可见

    private static final int PAGE_FAVORITE_FRAGMENT_SHOW = 27;//收藏夹页面可见
    private static final int PAGE_FAVORITE_FRAGMENT_HIDE = 28;//收藏夹页面不可见

    public static final int VIEW_NAVI_EXIT_DIALOG_SHOW = 29;//导航中-退出导航dialog可见
    private static final int VIEW_NAVI_EXIT_DIALOG_HIDE = 30;//导航中-退出导航dialog不可见

    private static final int PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW = 31;//设置家或者公司页面可见
    private static final int PAGE_SET_HOMEORCOMP_FRAGMENT_HIDE = 32; //设置家或者公司不页面可见

    private static final int VIEW_MORE_FUNCTION_SHOW = 33; //主页菜单可见
    private static final int VIEW_MORE_FUNCTION_HIDE = 34; //主页菜单不可见

    private static final int PAGE_MY_FAVORITE_CAR_SHOW = 35; //我的爱车页面可见
    private static final int PAGE_MY_FAVORITE_CAR_HIDE = 36; //我的爱车页面不可见

    private static final int PAGE_SEARCHRESULT_NO_LIST_FRAGMENT_SHOW = 37;//设置-我的消息-详情 页面可见
    private static final int PAGE_SEARCHRESULT_NO_LIST_FRAGMENT_HIDE = 38; //设置-我的消息-详情 页面不可见

    private static final int PAGE_MESSAGE_PUSH_FRAGMENT_SHOW = 39; //设置-我的消息 页面可见
    private static final int PAGE_MESSAGE_PUSH_FRAGMENT_HIDE = 40; //设置-我的消息 页面不可见

    private static final int PAGE_TEAMTRIP_SETTING_FRAGMENT_SHOW = 41;//集结设置页面可见
    private static final int PAGE_TEAMTRIP_SETTING_FRAGMENT_HIDE = 42; //集结设置页面不可见

    private static final int PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW = 43; //集结点搜索可见
    private static final int PAGE_TEAMTRIP_DESTINATION_FRAGMENT_HIDE = 44; //集结点搜索不可见

    private static final int PAGE_SUGGESTION_FRAGMENT_SHOW = 45;//检索输入后的页面，可见
    private static final int PAGE_SUGGESTION_FRAGMENT_HIDE = 46; //检索输入后的页面，不可见

    private static final int PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_SHOW = 47; //检索结果页（设置集结点）页面可见
    private static final int PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_HIDE = 48; //检索结果页（设置集结点）页面不可见

    private static final int PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_SHOW = 49; //检索结果页（添加途径地）页面可见
    private static final int PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_HIDE = 50; //检索结果页（添加途径地）页面不可见

    private static final int PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW = 51; //检索结果页（设置家）页面可见
    private static final int PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_HIDE = 52; //检索结果页（设置家）页面不可见

    private static final int PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_SHOW = 53; //检索结果页（设置公司）页面可见
    private static final int PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_HIDE = 54; //检索结果页（设置公司）页面不可见

    private static final int PAGE_SEARCHRESULT_LIST_ADDFAVORITE_FRAGMENT_SHOW = 55;//检索结果页（收藏普通收藏点）页面可见
    private static final int PAGE_SEARCHRESULT_LIST_ADDFAVORITE_FRAGMENT_HIDE = 56; //检索结果页（收藏普通收藏点）页面不可见

    private static final int PAGE_SEARCHRESULT_LIST_SENDPHOME_FRAGMENT_SHOW = 57; //检索结果页（发送手机）页面可见
    private static final int PAGE_SEARCHRESULT_LIST_SENDPHOME_FRAGMENT_HIDE = 58; //检索结果页（发送手机）页面不可见

    private static final int PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW = 59; //检索结果页（微信位置）页面可见
    private static final int PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_HIDE = 60; //检索结果页（微信位置）页面不可见

    private static final int PAGE_FAVORITE_FRAGMENT_ADDPASS_SHOW = 61; //收藏夹（添加途径地）页面可见
    private static final int PAGE_FAVORITE_FRAGMENT_ADDPASS_HIDE = 62; //收藏夹（添加途径地）页面不可见

    private static final int PAGE_FAVORITE_FRAGMENT_TEAMTRIP_SHOW = 63; //收藏夹（集结点设置）页面可见
    private static final int PAGE_FAVORITE_FRAGMENT_TEAMTRIP_HIDE = 64; //收藏夹（集结点设置）页面可不见

    private static final int PAGE_POINTSELECT_TEAMTRIP_FRAGMENT_SHOW = 65; //地图选点（集结点设置）页面可见
    private static final int PAGE_POINTSELECT_TEAMTRIP_FRAGMENT_HIDE = 66; //地图选点（集结点设置）页面不可见

    private static final int PAGE_POINTSELECT_ADDPASS_FRAGMENT_SHOW = 67; //地图选点（添加途径地）页面可见
    private static final int PAGE_POINTSELECT_ADDPASS_FRAGMENT_HIDE = 68; //地图选点（添加途径地）页面不可见

    private static final int PAGE_POINTSELECT_ADDHOME_FRAGMENT_SHOW = 69; //地图选点（设置家）页面可见
    private static final int PAGE_POINTSELECT_ADDHOME_FRAGMENT_HIDE = 70; //地图选点（设置家）页面可不见

    private static final int PAGE_POINTSELECT_ADDCOMP_FRAGMENT_SHOW = 71; //地图选点（设置公司）页面可见
    private static final int PAGE_POINTSELECT_ADDCOMP_FRAGMENT_HIDE = 72; //地图选点（设置公司）页面不可见

    private static final int PAGE_POINTSELECT_ADDFAVORITE_FRAGMENT_SHOW = 73;//地图选点（添加普通收藏点）页面可见
    private static final int PAGE_POINTSELECT_ADDFAVORITE_FRAGMENT_HIDE = 74;//地图选点（添加普通收藏点）页面不可见

    private static final int PAGE_TEAM_TRIP_FRAGMENT_SHOW = 75;//集结页面可见
    private static final int PAGE_TEAM_TRIP_FRAGMENT_HIDE = 76;//集结页面不可见

    private static final int PAGE_TEAM_TRIP_SETTING_EXIT_DIAGOG_SHOW = 77;//退出集结dialog显示
    private static final int PAGE_TEAM_TRIP_SETTING_EXIT_DIAGOG_HIDE = 78;//退出集结dialog隐藏

    private static final int PAGE_FU_MESSAGEVIEW_SHOW = 79;//首页消息悬浮框显示
    private static final int PAGE_FU_MESSAGEVIEW_INDE = 80;//首页消息悬浮框隐藏

    private static final int VIEW_CLEAR_HISTORY_DIALOG_SHOW = 81;//删除历史记录dialog显示
    private static final int VIEW_CLEAR_HISTORY_DIALOG_HIDE = 82;//删除历史记录dialog隐藏


    //地图缩放
    private static final int MAP_OPERA_ZOOM = 1;
    //路况开关
    private static final int MAP_OPERA_SWITCH_ROAD_CONDITION = 0;
    //视图设置
    private static final int MAP_OPERA_VIEW_SETTING = 2;

    private static final int MSG_TTS = 101;
    private static final int MSG_TTS_WITH_REPLACE = 103;

    private Context mContext;
    private static MXSdkManager instance;
    private IExtendApi extendApi;
    private InstructionEntity innerEntity;
    private boolean inited;
    private MVWAgent mvwAgent ;
//    private SRAgent  srAgent ;

    private boolean fromSetHomeOrComp = true;

    @SuppressLint("HandlerLeak")
    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TTS:
                    Bundle bundle = (Bundle) msg.obj;
                    String ttsText = bundle.getString("ttsText");
                    String conditionId = bundle.getString("conditionId");
                    startNaviTTS(ttsText, conditionId);
                    break;
                case MSG_TTS_WITH_REPLACE:
                    Bundle bundleReplace = (Bundle) msg.obj;
                    final String ttsText1 = bundleReplace.getString("ttsText");
                    String conditionId1 = bundleReplace.getString("conditionId");
                    final String dis = bundleReplace.getString("distance");
                    final String time = bundleReplace.getString("time");
                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId1, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            String ttsText = tts;
                            if (TextUtils.isEmpty(tts)) {
                                ttsText = ttsText1;
                            } else {
                                List<ReplaceTtsEntity> replaceTtsEntityList = new ArrayList<>();
                                replaceTtsEntityList.add(Utils.initReplaceData("#DISTANCE#", dis));
                                replaceTtsEntityList.add(Utils.initReplaceData("#TIME#", time));
                                ttsText = Utils.replaceTtsList(ttsText, replaceTtsEntityList);
                            }

                            Utils.startTTS(ttsText);
                        }
                    });
                    break;
            }
        }
    };

    private MXSdkManager(Context context) {
        this.mContext = context;
    }

    public static MXSdkManager getInstance(Context context) {
        if (instance == null) {
            instance = new MXSdkManager(context.getApplicationContext());
        }
        return instance;
    }

    public void init() {
        LogUtils.d(TAG, "======init=====");
        TAExtendManager.getInstance().init(mContext);
        initExtendApi();
        mvwAgent = MVWAgent.getInstance();
        inited = true;
        updateMyLocation();
    }

    private void initExtendApi() {
        this.extendApi = TAExtendManager.getInstance().getApi();
        if (extendApi != null) {
            this.extendApi.addExtendListener(mNaviStatusChangedListener);
            this.extendApi.addPageChangedListener(mPageChangedListener);
        }
    }

    private boolean isExtendApiNull() {
        if (extendApi != null) {
            return false;
        } else {
            initExtendApi();
            LogUtils.e(TAG, "isExtendApiNull extendApi: " + extendApi);
            return extendApi == null;
        }
    }

    public boolean isInited() {
        return inited;
    }

    public void setInnerEntity(InstructionEntity innerEntity) {
        this.innerEntity = innerEntity;
    }

    //免唤醒指令
    public void MxMapOperation(MapController mapController, MvwLParamEntity mvwLParamEntity) {
        innerEntity = new InstructionEntity(mvwLParamEntity);
        LogUtils.d(TAG, "mvwAction  nMvwId:" + mvwLParamEntity.nMvwId  + "  nKeyword:" + mvwLParamEntity.nKeyword);

        /**
         *   case MvwSession.ISS_MVW_SCENE_ANSWER_CALL:
         *             case MvwSession.ISS_MVW_SCENE_OTHER:
         *             case MvwSession.ISS_MVW_SCENE_CUSTOME:
         */
        if (isMvwForNavi(mvwLParamEntity.nMvwScene)) {//导航场景
            switch (mvwLParamEntity.nKeyword) {

                case ID_FINISH_NAVI: //结束导航
                case ID_STOP_NAVI: //停止导航
                case ID_EXIT_NAVI: //退出导航
                    exitNavi(AppConstant.SOURCE_MWV);
                    break;
                case ID_ZOOMIN://放大地图
                    enlargeMap();
                    break;
                case ID_ZOOMOUT://缩小地图
                    reduceMap();
                    break;
                case ID_TRAFFIC_ON: //打开路况
                    openRoad();
                    break;
                case ID_TRAFFIC_OFF: //关闭路况
                    closeRoad();
                    break;
                case ID_TTS_OFF: //关闭播报
                case ID_TTS_OFF1:
                    closeNaviBroadcast();
                    break;
                case ID_TTS_ON: //打开播报
                case ID_TTS_ON1:
                    openNaviBroadcast();
                    break;
                case ID_GO_HOME: //回家
                    mapController.goHomeOrCompany(0, AppConstant.SOURCE_MWV);
                    break;
                case ID_GO_CAMPANY: //去公司
                    mapController.goHomeOrCompany(1, AppConstant.SOURCE_MWV);
                    break;

//                case 0: //高速优先
//                    highSpeed();
//                    break;
//                case 1: //不走高速
//                    noSpeed();
//                    break;
//                case 2://少收费
//                case 3: //避免收费
//                    lessCharge();
//                    break;
//                case 4://躲避拥堵
//                case 5://规避拥堵
//                    avoidingCongestion();
//                    break;
//                case 6: //沿途的加油站
//                    if (isForeground()) {
//                        mapController.alongTheWaySearch("加油站");
//                    }
//                    break;
//                case 7: //还有多久到
//                case 8: //还有多远到
//                    howFar();
//                    break;
//
//
//                case 18: //白天模式
//                    setShowMode(0, null);
//                    break;
//                case 19: //黑夜模式
//                    setShowMode(1, null);
//                    break;
//                case 20: //2D模式
//                case 21://2D视图
//                    D2Mode();
//                    break;
//                case 22: //3D模式
//                case 23://3D视图
//                    D3Mode();
//                    break;

            }
        }
//        else if (mvwLParamEntity.nMvwScene == 128) {
//
//            switch (mvwLParamEntity.nMvwId) {
//                case ID_HEAD_UP: //车头朝上
//                    headUp();
//                    break;
//                case ID_NORTH_UP: //正北朝上
//                    norihUp();
//                    break;
//                case ID_LOOK_ALL: //查看全局
//                    viaInfo();
//                    break;
//            }
//        }
    }

    private boolean isMvwForNavi(int nMvwScene) {
        return nMvwScene == MvwSession.ISS_MVW_SCENE_ANSWER_CALL
            || nMvwScene == MvwSession.ISS_MVW_SCENE_OTHER
            || nMvwScene == MvwSession.ISS_MVW_SCENE_CUSTOME;
    }

    public void norihUp(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).changeToNorthUP()){
            return;
        }
        /***********欧尚修改结束*****************/
        backToMap(new Callback() {
            @Override
            public void success() {

                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_VIEW_SETTING, 0, MXSdkManager.this);

            }
        });
    }

    public void headUp(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).changeToCarUP()){
            return;
        }
        /***********欧尚修改结束*****************/
        backToMap(new Callback() {
            @Override
            public void success() {

                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_VIEW_SETTING, 1, MXSdkManager.this);
            }
        });
    }


    public void viaInfo(){
        boolean isNaving = isNaving();
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).fullView(isNaving)){
            return;
        }
        /***********欧尚修改结束*****************/

        LogUtils.d(TAG, "查看全局 isNaving：" + isNaving);
        String ttsId = TtsConstant.NAVIC50CONDITION;
        if (!isNaving) {
            Bundle bundle = new Bundle();
            bundle.putString("ttsText", mContext.getString(R.string.map_prefer_no_in_naving));
            bundle.putString("conditionId", TtsConstant.NAVIC51CONDITION);
            Message msg = myHandler.obtainMessage(MSG_TTS, bundle);
            myHandler.sendMessageDelayed(msg, 2000);
        } else {
            if (isForeground()) {
                if (!isExtendApiNull())
                    extendApi.lookOverView();
                startNaviTTS(mContext.getString(R.string.map_start_navi_success), ttsId);
            } else {
                backToMap(new Callback() {
                    @Override
                    public void success() {
                        if (!isExtendApiNull())
                            extendApi.lookOverView();
                        startNaviTTS(mContext.getString(R.string.map_start_navi_success), ttsId);
                    }
                });
            }

            Utils.eventTrack(mContext,R.string.skill_navi, R.string.scene_navi_query, R.string.object_view_global, ttsId,R.string.condition_navi1);
        }
    }

    public void highSpeed(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).routePreference(ROUTE_PREFERENCE_ID_HIGH_SPEED,mContext.getString(R.string.map_prefer_highway_ok),
                TtsConstant.NAVIC72CONDITION,R.string.object_high_speed_priority,R.string.scene_navi_route_switch,isNaving())){
            return;
        }
        /***********欧尚修改结束*****************/

        //导航中，或在设置界面
        if (isNaving() || (isForeground() && currentPage == PAGE_SETTING_FRAGMENT_SHOW)) {
            if (!isExtendApiNull())
                extendApi.changeNaviRoutePrefer(8, this);
        }
        //多路线路线偏好页面
        else if (isForeground() && currentPage == VIEW_MULTIROUTE_PREFERENCE_SHOW) {
            if (!isExtendApiNull())
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 1, "8"), this);
        } else {
            startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);

        }
    }

    public void  noSpeed(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).routePreference(ROUTE_PREFERENCE_ID_NO_SPEED,mContext.getString(R.string.map_avoid_highway_ok),
                TtsConstant.NAVIC74CONDITION,R.string.object_no_high_speed,R.string.scene_navi_route_switch,isNaving())){
            return;
        }
        /***********欧尚修改结束*****************/

        if (isNaving() || (isForeground() && (currentPage == PAGE_SETTING_FRAGMENT_SHOW))) {
            if (!isExtendApiNull())
                extendApi.changeNaviRoutePrefer(1, this);
        }
        //多路线路线偏好页面
        else if (isForeground() && currentPage == VIEW_MULTIROUTE_PREFERENCE_SHOW) {
            if (!isExtendApiNull())
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 1, "1"), this);
        } else {
            startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);
        }
    }


    public void  lessCharge(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).routePreference(ROUTE_PREFERENCE_ID_LESS_CHARGE,mContext.getString(R.string.map_avoid_cost_ok),
                TtsConstant.NAVIC76CONDITION,R.string.object_avoid_charging,R.string.scene_navi_route_switch,isNaving())){
            return;
        }
        /***********欧尚修改结束*****************/

        if (isNaving() || (isForeground() && currentPage == PAGE_SETTING_FRAGMENT_SHOW)) {
            if (!isExtendApiNull())
                extendApi.changeNaviRoutePrefer(2, this);
        }
        //多路线路线偏好页面
        else if (isForeground() && currentPage == VIEW_MULTIROUTE_PREFERENCE_SHOW) {
            if (!isExtendApiNull())
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 1, "4"), this);
        } else {
            startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);
        }
    }


    public void avoidingCongestion(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).routePreference(ROUTE_PREFERENCE_ID_AVOIDING_CONGESTION,mContext.getString(R.string.map_avoid_traffic_ok),
                TtsConstant.NAVIC78CONDITION,R.string.object_avoid_congestion,R.string.scene_navi_route_switch,isNaving())){
            return;
        }
        /***********欧尚修改结束*****************/

        if (isNaving() || (isForeground() && currentPage == PAGE_SETTING_FRAGMENT_SHOW)) {
            if (!isExtendApiNull())
                extendApi.changeNaviRoutePrefer(4, this);
        }
        //多路线路线偏好页面
        else if (isForeground() && currentPage == VIEW_MULTIROUTE_PREFERENCE_SHOW) {
            if (!isExtendApiNull())
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 1, "4"), this);
        } else {
            startNaviTTS(mContext.getString(R.string.map_prefer_no_in_naving), TtsConstant.NAVIC47CONDITION);
        }
    }


    public void howFar(){
        boolean isNaving = isNaving();
        LogUtils.d(TAG, "还有多久到(远到) isNaving：" + isNaving);
        /***********欧尚修改开始*****************/
        GDSdkManager gdSdkManager=GDSdkManager.getInstance(mContext);
        if(gdSdkManager.isGDForeground()){//高德在前台
            if(gdSdkManager.isGDNaving()){//高德在导航
                //执行高德的"还有多远到"
                gdSdkManager.howFar();
                return;
            }
        }else{//高德在后台
            if(gdSdkManager.isGDNaving()&&!isNaving){//高德在导航并且美行没导航
                //执行高德的"还有多远到"
                gdSdkManager.howFar();
                return;
            }
        }
        /***********欧尚修改结束*****************/
        if (isNaving) {
            if (isExtendApiNull()) {
                return;
            }
            int remainTime = extendApi.getRemainTime();
            LogUtils.e(TAG, "remainTime：" + remainTime);
            int remainDistance = extendApi.getRemainDistance();
            LogUtils.e(TAG, "remainDistance：" + remainDistance);
            if (remainTime > 0 && remainDistance > 0) {
                String tts = calcRemainTimeAndDistance(remainTime, remainDistance);
                LogUtils.e(TAG, "tts：" + tts);
                Bundle bundle = new Bundle();
                bundle.putString("ttsText", tts);
              //  bundle.putString("conditionId", TtsConstant.NAVIC52CONDITION);
                Message msg = myHandler.obtainMessage(MSG_TTS, bundle);
                myHandler.sendMessageDelayed(msg, 2000);

                Utils.eventTrack(mContext,R.string.skill_navi, R.string.scene_navi_query, R.string.object_how_long, TtsConstant.NAVIC52CONDITION,R.string.condition_navi1, tts);
            } else {
                FloatViewManager.getInstance(mContext).hide();
            }
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("ttsText", mContext.getString(R.string.map_get_time_and_distance_fail));
            bundle.putString("conditionId", TtsConstant.NAVIC51CONDITION);
            Message msg = myHandler.obtainMessage(MSG_TTS, bundle);
            myHandler.sendMessageDelayed(msg, 2000);
        }
    }


    public void enlargeMap(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).enlargeMap()){
            return;
        }
        /***********欧尚修改结束*****************/

        if (FloatViewManager.getInstance(mContext).isHide() && !isForeground()) {
            LogUtils.i(TAG,"enlargeMap  FloatViewManager.getInstance(mContext).isHide() && !isForeground() return");
            return;
        }

        backToMap(new Callback() {
            @Override
            public void success() {
                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_ZOOM, 0, MXSdkManager.this);
            }
        });
    }

    public void reduceMap(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).reduceMap()){
            return;
        }
        /***********欧尚修改结束*****************/


        if (FloatViewManager.getInstance(mContext).isHide() && !isForeground()) {
            LogUtils.i(TAG,"reduceMap  FloatViewManager.getInstance(mContext).isHide() && !isForeground() return");
            return;
        }

        backToMap(new Callback() {
            @Override
            public void success() {
                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_ZOOM, 1, MXSdkManager.this);
            }
        });
    }

    public void openRoad(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).openRoad()){
            return;
        }
        /***********欧尚修改结束*****************/

        if (FloatViewManager.getInstance(mContext).isHide() && !isForeground()) {
            LogUtils.i(TAG,"openRoad  FloatViewManager.getInstance(mContext).isHide() && !isForeground() return");
            return;
        }

        backToMap(new Callback() {
            @Override
            public void success() {
                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_SWITCH_ROAD_CONDITION, 0, MXSdkManager.this);
            }
        });
    }

    public void closeRoad(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).closeRoad()){
            return;
        }
        /***********欧尚修改结束*****************/

        if (FloatViewManager.getInstance(mContext).isHide() && !isForeground()) {
            LogUtils.i(TAG,"closeRoad  FloatViewManager.getInstance(mContext).isHide() && !isForeground() return");
            return;
        }

        backToMap(new Callback() {
            @Override
            public void success() {
                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_SWITCH_ROAD_CONDITION, 1, MXSdkManager.this);
            }
        });
    }

    public void  closeNaviBroadcast(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).closeBroadcast()){
            return;
        }
        /***********欧尚修改结束*****************/
//        backToMap(new Callback() {
//            @Override
//            public void success() {
                if (!isExtendApiNull())
                    extendApi.setVolumeMute(true, MXSdkManager.this);
//            }
//        });
    }

    public void  openNaviBroadcast(){
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).openBroadcast()){
            return;
        }
        /***********欧尚修改结束*****************/
//        backToMap(new Callback() {
//            @Override
//            public void success() {
                if (!isExtendApiNull())
                    extendApi.setVolumeMute(false, MXSdkManager.this);
//            }
//        });
    }

    public void D2Mode() {
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).changeTo2D()){
            return;
        }
        /***********欧尚修改结束*****************/
        backToMap(new Callback() {
            @Override
            public void success() {
                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_VIEW_SETTING, 1, MXSdkManager.this);
            }
        });
    }

    public void D3Mode() {
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).changeTo3D()){
            return;
        }
        /***********欧尚修改结束*****************/
        backToMap(new Callback() {
            @Override
            public void success() {
                if (!isExtendApiNull())
                    extendApi.mapOpera(MAP_OPERA_VIEW_SETTING, 2, MXSdkManager.this);
            }
        });
    }

    private int mExitNaviSource = AppConstant.SOURCE_MWV;
    public void exitNavi(int source) {
        /***********欧尚修改开始*****************/
        boolean isExitGD=false;//是否 高德在后台并且退了高德
        GDSdkManager gdSdkManager=GDSdkManager.getInstance(mContext);
        if(gdSdkManager.isGDForeground()){//高德在前台
            if(gdSdkManager.isGDNaving()){//高德在导航
                gdSdkManager.exitNavi();
            }else{//高德未导航
                gdSdkManager.exitGDApp();
            }
            return;
        }else{//高德在后台
            if(gdSdkManager.isGDNaving()){//高德在导航
                gdSdkManager.exitNavi();
                isExitGD=true;
            }
        }
        /***********欧尚修改结束*****************/

        mExitNaviSource = source;
        boolean foreground = isForeground();
        boolean isNaving = isNaving();
        LogUtils.e(TAG, "foreground:" + foreground + "   isNaving:" + isNaving );
            if (isNaving) {
                backToMap(new Callback() {
                    @Override
                    public void success() {
                        if (!isExtendApiNull())
                            extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_GUIDE, 0, ""), MXSdkManager.this);
                    }
                });
            } else {
                /***********欧尚修改开始*****************/
                if(isExitGD){
                   return;
                }
                /***********欧尚修改结束*****************/

                startNaviTTS(mContext.getString(R.string.map_stop_navi_fail), TtsConstant.NAVIC47CONDITION);
//                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_stop_navi,TtsConstant.MHXC11CONDITION,R.string.condition_null,mContext.getString(R.string.map_stop_navi_fail));

                if (source == AppConstant.SOURCE_MWV) {
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_stop_navi,TtsConstant.MHXC11CONDITION,R.string.condition_null,mContext.getString(R.string.map_stop_navi_fail));
                } else {
                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_control,R.string.object_stop_navi,TtsConstant.NAVIC47CONDITION,R.string.condition_navi47,mContext.getString(R.string.map_stop_navi_fail));
                }
            }
    }

    public static final int SHOW_DAY = MCU.NIGHT_MODE_DAYTIME;
    public static final int SHOW_NIGHT = MCU.NIGHT_MODE_NIGHT;
    public static final int SHOW_AUTO = MCU.NIGHT_MODE_AUTO;

    /**
     * 设置显示模式
     *
     * @param mode MCU.NIGHT_MODE_DAYTIME MCU.NIGHT_MODE_NIGHT MCU.NIGHT_MODE_AUTO
     */
    public void setShowMode(int mode, IExtendCallback iExtendCallback) {
//        if (iExtendCallback == null) {
//            if (!isExtendApiNull())
//                extendApi.setDayNightStyle(mode, MXSdkManager.this);
//        } else {
//            if (!isExtendApiNull())
//                extendApi.setDayNightStyle(mode, iExtendCallback);
//        }

        if (getSystemMode() == mode) {
            if (iExtendCallback == null) {
                onFail(new ExtendErrorModel(0, "areadly "));
            } else {
                iExtendCallback.onFail(new ExtendErrorModel(0, "areadly "));
            }
        } else {
            setSystemMode(mode);
            if (iExtendCallback == null) {
                success(new ExtendBaseModel());
            } else {
                iExtendCallback.success(new ExtendBaseModel());
            }
        }



//        switch (mode) {
//            case 0:
//                //系统主题白天模式
//                setSystemMode(MCU.NIGHT_MODE_DAYTIME);
//                break;
//            case 1:
//                //系统主题黑夜模式
//                setSystemMode(MCU.NIGHT_MODE_NIGHT);
//                break;
//            case 2:
//                //系统主题自动模式
//                setSystemMode(MCU.NIGHT_MODE_AUTO);
//                break;
//        }
    }


    public void setSystemMode(int mode) {
        try {
            AppConfig.INSTANCE.mCarMcuManager.setIntProperty(CarMcuManager.ID_VENDOR_LIGHT_NIGHT_MODE_STATE, VEHICLE_AREA_TYPE_GLOBAL, mode);
        } catch (CarNotConnectedException e) {
            LogUtils.e(TAG, "e:" + e.getMessage());
        }
    }

    public int getSystemMode() {
        try {
            return AppConfig.INSTANCE.mCarMcuManager.getIntProperty(CarMcuManager.ID_VENDOR_LIGHT_NIGHT_MODE_STATE, VEHICLE_AREA_TYPE_GLOBAL);
        } catch (CarNotConnectedException e) {
            LogUtils.e(TAG, "e:" + e.getMessage());
        }
        return -1;
    }

    private void waitMapMultiInterface(String operation) {
        IntentEntity intentEntity = new IntentEntity();
        intentEntity.service = PlatformConstant.Service.MAP_U;
        intentEntity.operation = operation;
        MultiInterfaceUtils.getInstance(mContext).uploadAppStatusData(true, PlatformConstant.Service.MAP_U, "default");
        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
    }


    //可见即可说指令
    public void MxMapOperation(StkResultEntity stkResultEntity) {
        innerEntity = new InstructionEntity(stkResultEntity);
        LogUtils.e(TAG, "stkAction   id:" + innerEntity.id + "  text:" + innerEntity.text);
        if (isExtendApiNull() || !isForeground()) {
            LogUtils.e(TAG, "STK must run in foreground");
            return;
        }
        switch (innerEntity.id) {
            /**
             * 检索页可见 (30-33)
             */
            case 30: //返回
                if (currentPage == PAGE_SEARCH_FRAGMENT_SHOW) {
                    exitFromSearch(innerEntity.text);
                }
                break;
            case 31://收藏夹
                if (currentPage == PAGE_SEARCH_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_FRAGMENT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_box_search_with_network_before_input, innerEntity.text);
                }
                break;
            case 32://微信位置
                if (currentPage == PAGE_SEARCH_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_FRAGMENT, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_box_search_with_network_before_input, innerEntity.text);
                }
                break;
            case 33://分类检索
                if (currentPage == PAGE_SEARCH_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_FRAGMENT, 3, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_box_search_with_network_before_input, innerEntity.text);
                }
                break;

            /**
             * 检索结果页可见(40-45)
             */
            case 40: //返回
                if (currentPage == PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW) {
                    exitFromSearchResult(innerEntity.text);
                }
                break;
            case 41: //关闭
                if (currentPage == PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 42: //第一个
                if (currentPage == PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "0"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 43: //第二个
                if (currentPage == PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 44: //第三个
                if (currentPage == PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 45://出发
                if (currentPage == PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 46:
            case 47:
            case 48:
                if (currentPage == PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 2, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;

            /**
             * 多路线页面可见 (50-58)
             */
            case 50: //返回
                if (currentPage == PAGE_MULTIROUTE_FRAGMENT_SHOW) {//多路线规划-返回
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 3, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_multi_route_planning, innerEntity.text);
                }
                break;
            case 51://第一个
                if (currentPage == PAGE_MULTIROUTE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 4, "0"), this);
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,DatastatManager.primitive,R.string.object_mhcc13,DatastatManager.response,TtsConstant.MHXC13CONDITION,R.string.condition_null,null,true);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_multi_route_planning, innerEntity.text);
                }
                break;
            case 52://第二个
                if (currentPage == PAGE_MULTIROUTE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 4, "1"), this);
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,DatastatManager.primitive,R.string.object_mhcc13,DatastatManager.response,TtsConstant.MHXC13CONDITION,R.string.condition_null,null,true);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_multi_route_planning, innerEntity.text);
                }
                break;
            case 53://第三个
                if (currentPage == PAGE_MULTIROUTE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 4, "2"), this);
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,DatastatManager.primitive,R.string.object_mhcc13,DatastatManager.response,TtsConstant.MHXC13CONDITION,R.string.condition_null,null,true);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_multi_route_planning, innerEntity.text);
                }
                break;
            case 54://开始导航
                if (currentPage == PAGE_MULTIROUTE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 6, ""), this);
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_mhcc14,TtsConstant.MHXC14CONDITION,R.string.condition_null);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_multi_route_planning, innerEntity.text);
                }
                break;
            case 5000://推荐路线
            case 5001:
            case 5002:
            case 5003:
            case 5004:
            case 5005:
                if (currentPage == PAGE_MULTIROUTE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 5, innerEntity.text), this);
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,DatastatManager.primitive,R.string.object_mhcc13,DatastatManager.response,TtsConstant.MHXC13CONDITION,R.string.condition_null,null,true);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_multi_route_planning, innerEntity.text);
                }
                break;

            /**
             * 导航页
             */
            case 60: //设置
                if (currentPage == PAGE_ROUTEGUIDE_FRAGMENT_SHOW) {
                    extendApi.goSetting(this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_map_multi_route_planning, innerEntity.text);
                }
                break;
            /**
             * 主页
             */
            case 65: //设置
                if (currentPage == PAGE_FU_FRAGMENT_SHOW) {
                    extendApi.goSetting(this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 66: //组队
                if (currentPage == PAGE_FU_FRAGMENT_SHOW) {
                    extendApi.goTeamTrip();
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            /**
             * 添加途径地页面可见 (70-74)
             */
            case 70: //返回
                if (currentPage == PAGE_ROUTEPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_PASS, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 71://收藏夹
                if (currentPage == PAGE_ROUTEPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_PASS, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 72://微信位置
                if (currentPage == PAGE_ROUTEPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_PASS, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 73://分类检索
                if (currentPage == PAGE_ROUTEPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_PASS, 3, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 74://地图选点
                if (currentPage == PAGE_ROUTEPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_PASS, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_map_pickup_map_selection, innerEntity.text);
                }
                break;

            /**
             * 地图选点页面可见 (80-81)
             */
            case 80: //返回
                if (currentPage == PAGE_POINTSELECT_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 81: //设为途地
                if (currentPage == PAGE_POINTSELECT_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_map_pickup_map_selection, innerEntity.text);
                }
                break;

            /**
             * 设置页面可见 (90)
             */
            case 90: //返回
                if (currentPage == PAGE_SETTING_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SETTING, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_search_along, R.string.object_settings_navi_settings, innerEntity.text);
                }
                break;
            /**
             * 首页拾取页面可见 (100-104)
             */
            case 100: //关闭
                if (currentPage == VIEW_PICK_UP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_PICKUP, 3, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;

            case 101://收藏
                //地图选点页面可见
                if (currentPage == VIEW_PICK_UP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_PICKUP, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            case 102: //取消收藏
                //地图选点页面可见
                if (currentPage == VIEW_PICK_UP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_PICKUP, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            case 103://周边
                if (currentPage == VIEW_PICK_UP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_PICKUP, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            case 104://出发
                //地图选点页面可见
                if (currentPage == VIEW_PICK_UP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_PICKUP, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;

            /**
             *多路线-路线详情页可见 (110-111)
             */
            case 110: //返回
                if (currentPage == VIEW_MULTIROUTE_DETAIL_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 7, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_multi_route, R.string.object_map_route_details, innerEntity.text);
                }
                break;
            case 111://模拟导航
                if (currentPage == VIEW_MULTIROUTE_DETAIL_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 8, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_multi_route, R.string.object_map_route_details, innerEntity.text);
                }
                break;

            /**
             * 多路线-路线偏好页可见 (120-121)
             */
            case 120: //返回
                if (currentPage == VIEW_MULTIROUTE_PREFERENCE_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_multi_route, R.string.object_map_preference_route, innerEntity.text);
                }
                break;
            case 121://完成
                if (currentPage == VIEW_MULTIROUTE_PREFERENCE_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_multi_route, R.string.object_map_preference_route, innerEntity.text);
                }
                break;

            /**
             * 多路线-添加途径地页可见 (130-132)
             */
            case 130: //返回
                if (currentPage == VIEW_MULTIROUTE_ROUTEPASS_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_MULTIROUTE_ROUTE_PASS, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_multi_route, R.string.object_map_route_management, innerEntity.text);
                }
                break;
            case 131://添加途经地
                if (currentPage == VIEW_MULTIROUTE_ROUTEPASS_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_MULTIROUTE_ROUTE_PASS, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_multi_route, R.string.object_map_route_management, innerEntity.text);
                }
                break;
            case 132://开始导航
                if (currentPage == VIEW_MULTIROUTE_ROUTEPASS_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_MULTIROUTE_ROUTE_PASS, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_multi_route, R.string.object_map_route_management, innerEntity.text);
                }
                break;

            /**
             * 周边检索页面可见 (140-147)
             */
            case 140: //返回
                if (currentPage == PAGE_NEARBY_FRAGMENT_SHOW) {
                    exitFromNearSearch(innerEntity.text);
                }
                break;
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 1400:
            case 1401:
            case 1402:
            case 1403:
            case 1404:
            case 1405:
            case 1406:
            case 1407:
            case 1408:
            case 1409:
            case 1410:
            case 1411:
            case 1412:
            case 1413:
            case 1414:
            case 1415:
            case 1416:
            case 1417:
            case 1418:
            case 1419:
            case 1420:
            case 1421:
            case 1422:
            case 1423:

                if (currentPage == PAGE_NEARBY_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_NERABY_SEARCH, 1, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_directory_search, R.string.object_classified_retrieval_peripheral_retrieval, innerEntity.text);
                }
                break;

            /**
             * 收藏夹页面可见 (150-153)
             */
            case 150: //返回
                if (currentPage == PAGE_FAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            case 151: //家
                if (currentPage == PAGE_FAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            case 152: //公司
                if (currentPage == PAGE_FAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            case 153: //出发
                if (currentPage == PAGE_FAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            case 154:
            case 155:
            case 156:
                if (currentPage == PAGE_FAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 1, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            /**
             * 导航中-退出导航dialog可见 (160-161)
             */
            case 160: //确定
                if (currentPage == VIEW_NAVI_EXIT_DIALOG_SHOW) {
                TTSController.getInstance(mContext).stopTTS();
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_GUIDE, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_guided_view_functional_state_exit, innerEntity.text);
                }
                break;
            case 161: //取消
                if (currentPage == VIEW_NAVI_EXIT_DIALOG_SHOW) {
                    TTSController.getInstance(mContext).stopTTS();
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_GUIDE, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_start_or_end, R.string.object_map_guided_view_functional_state_exit, innerEntity.text);
                }
                break;

            /**
             * 设置家或者公司页面可见 (170-171)
             */
            case 170: //返回
                if (currentPage == PAGE_SUGGESTION_FRAGMENT_SHOW) {
                    exitFromSetHome(innerEntity.text);
                }
                break;
            case 171: //搜索
                if (currentPage == PAGE_SUGGESTION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SUGGESTION, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_map_retrieval_results_retrieva_results_list, innerEntity.text);
                }
                break;
            case 1700:
            case 1701:
            case 1702:
            case 1703:
            case 1704:
            case 1705:
            case 1706:
            case 1707:
            case 1708:
            case 1709:
            case 1710:
            case 1711:
            case 1712:
            case 1713:
            case 1714:
            case 1715:
            case 1716:
            case 1717:
            case 1718:
            case 1719:
                if (currentPage == PAGE_SUGGESTION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SUGGESTION, 2, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_map_retrieval_results_retrieva_results_list, innerEntity.text);
                }
                break;
            /**
             * 设置家或者公司页面可见 (180-183)
             */
            case 180: //返回
                if (currentPage == PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW) {
                    exitFromSetCompany(innerEntity.text);
                }
                break;
            case 181: //地图选点
                if (currentPage == PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SET_HOMEORCOMP_FRAGMENT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            case 182: //微信位置
                if (currentPage == PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SET_HOMEORCOMP_FRAGMENT, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            case 183: //清除历史
                if (currentPage == PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SET_HOMEORCOMP_FRAGMENT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;
            case 184:
            case 185:
                if (currentPage == PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SET_HOMEORCOMP_FRAGMENT, 3, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                }
                break;


            case 188://清空
                if (fromSetHomeOrComp) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SET_HOMEORCOMP_FRAGMENT, 5, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                    return;
                }
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 7, ""), this);
                Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                break;
            case 189://取消
                if (fromSetHomeOrComp) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SET_HOMEORCOMP_FRAGMENT, 6, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, innerEntity.text);
                    return;
                }
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 8, ""), this);
                Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                break;

            /**
             * 主页菜单可见 (190-187)
             */
            case 190: //返回
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 191: //微信位置
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 192: //回家
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 193: //去公司
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 3, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 194: //收藏夹
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 195: //设置
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 5, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 196: //离线数据
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 6, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            case 197: //集结
                if (currentPage == VIEW_MORE_FUNCTION_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_MORE, 7, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_home_menu, innerEntity.text);
                }
                break;
            /**
             * 地图选点（添加途径地）页面可见(200-201)
             */
            case 200:
                if (currentPage == PAGE_POINTSELECT_ADDPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            case 201:
                if (currentPage == PAGE_POINTSELECT_ADDPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            /**
             * 地图选点（设置家）页面可见(210-211)
             */
            case 210:
                if (currentPage == PAGE_POINTSELECT_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            case 211:
                if (currentPage == PAGE_POINTSELECT_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            /**
             * 地图选点（设置公司）页面可见(220-224)
             */
            case 220:
                if (currentPage == PAGE_POINTSELECT_ADDCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            case 221:
                if (currentPage == PAGE_POINTSELECT_ADDCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking, innerEntity.text);
                }
                break;
            /**
             * 检索结果页（设置家）页面可见(230-234)
             */
            case 230:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 231:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 232:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "0"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 233:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 234:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 235:
            case 236:
            case 237:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 2, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            /**
             * 检索结果页（设置公司）页面可见(240-244)
             */
            case 240:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 241:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 242:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "0"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 243:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            case 244:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, innerEntity.text);
                }
                break;
            /**
             * 检索结果页（设置集结点）页面可见(250-251)
             */
            case 250:
                if (currentPage == PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 251:
                if (currentPage == PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 252:
                if (currentPage == PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "0"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 253:
                if (currentPage == PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 254:
                if (currentPage == PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            /**
             * 检索结果页（添加途径地）页面可见(260-261)
             */
            case 260:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 261:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 262:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "0"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 263:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 264:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_additionally, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            /**
             * 检索结果页（收藏普通收藏点）页面可见(270-271)
             */
            case 270:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDFAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking_poi_details, innerEntity.text);
                }
                break;
            case 271:
                if (currentPage == PAGE_SEARCHRESULT_LIST_ADDFAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking_poi_details, innerEntity.text);
                }
                break;
            /**
             * 检索结果页（发送手机）页面可见(280-281)
             */
            case 280:
                if (currentPage == PAGE_SEARCHRESULT_LIST_SENDPHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
                }
                break;
            case 281:
                if (currentPage == PAGE_SEARCHRESULT_LIST_SENDPHOME_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                }
                break;
            /**
             * 检索结果页（微信位置）页面可见(290-294)
             */
            case 290:
                if (currentPage == PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_pickup_location_wechat, innerEntity.text);
                }
                break;
            case 291:
                if (currentPage == PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_pickup_location_wechat, innerEntity.text);
                }
                break;
            case 292:
                if (currentPage == PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "0"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_pickup_location_wechat, innerEntity.text);
                }
                break;
            case 293:
                if (currentPage == PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_pickup_location_wechat, innerEntity.text);
                }
                break;
            case 294:
                if (currentPage == PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_pickup_location_wechat, innerEntity.text);
                }
                break;
            case 295:
            case 296:
            case 297:
                if (currentPage == PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 2, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_pickup_location_wechat, innerEntity.text);
                }
                break;


            /**
             * 收藏夹（添加途径地）页面可见(300-303)
             */
            case 300: //返回
                if (currentPage == PAGE_FAVORITE_FRAGMENT_ADDPASS_SHOW) {
                    exitFromFavorite(innerEntity.text);
                }
                break;
            case 301: //家
                if (currentPage == PAGE_FAVORITE_FRAGMENT_ADDPASS_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 302: //公司
                if (currentPage == PAGE_FAVORITE_FRAGMENT_ADDPASS_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            case 303:
                if (currentPage == PAGE_FAVORITE_FRAGMENT_ADDPASS_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_means_search, innerEntity.text);
                }
                break;
            /**
             * 收藏夹（集结点设置）页面可见(310-313)
             */
            case 310: //返回
                if (currentPage == PAGE_FAVORITE_FRAGMENT_TEAMTRIP_SHOW) {
                    exitFromFavorite1(innerEntity.text);
                }
                break;
            case 311: //家
                if (currentPage == PAGE_FAVORITE_FRAGMENT_TEAMTRIP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 3, "1"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 312: //公司
                if (currentPage == PAGE_FAVORITE_FRAGMENT_TEAMTRIP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 3, "2"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 313:
                if (currentPage == PAGE_FAVORITE_FRAGMENT_TEAMTRIP_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;

            /**
             * 地图选点（集结点设置）页面可见(320-321)
             */
            case 320:
                if (currentPage == PAGE_POINTSELECT_TEAMTRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 321:
                if (currentPage == PAGE_POINTSELECT_TEAMTRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            /**
             * 地图选点（添加普通收藏点）页面可见(330-331)
             */
            case 330:
                if (currentPage == PAGE_POINTSELECT_ADDFAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking_poi_details, innerEntity.text);
                }
                break;
            case 331:
                if (currentPage == PAGE_POINTSELECT_ADDFAVORITE_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_POINT_SELECT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_pickup, R.string.object_map_picking_poi_details, innerEntity.text);
                }
                break;
            /**
             * 集结页面可见(340)
             */
            case 340:
                if (currentPage == PAGE_TEAM_TRIP_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAM_TRIP_FRAGMENT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_grouping, innerEntity.text);
                }
                break;
            /**
             * 集结设置页面可见集结页面可见(350-351)
             */
            case 350:
                if (currentPage == PAGE_TEAMTRIP_SETTING_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_SETTING_FRAGMENT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 351:
                if (currentPage == PAGE_TEAMTRIP_SETTING_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_SETTING_FRAGMENT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 352:
                if (currentPage == PAGE_TEAM_TRIP_SETTING_EXIT_DIAGOG_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_SETTING_FRAGMENT, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            case 353:
                if (currentPage == PAGE_TEAM_TRIP_SETTING_EXIT_DIAGOG_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_SETTING_FRAGMENT, 3, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_settings, innerEntity.text);
                }
                break;
            /**
             * 我的爱车页面可见(360-362)
             */
            case 360:
                if (currentPage == PAGE_MY_FAVORITE_CAR_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MY_FAVORITE_CAR, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_my_love_car, innerEntity.text);
                }
                break;
            case 361:
                if (currentPage == PAGE_MY_FAVORITE_CAR_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MY_FAVORITE_CAR, 1, "0"), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_my_love_car, innerEntity.text);
                }
                break;
            case 362:
                if (currentPage == PAGE_MY_FAVORITE_CAR_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MY_FAVORITE_CAR, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_my_love_car, innerEntity.text);
                }
                break;
            /**
             * 集结点搜索可见(370-375)
             */
            case 370:
                if (currentPage == PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;
            case 371:
                if (currentPage == PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;
            case 372:
                if (currentPage == PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 2, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;
            case 373:
                if (currentPage == PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 3, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;
            case 374:
                if (currentPage == PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 4, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;
            case 375:
                if (currentPage == PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 6, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;

            case 378:
            case 379:
                if (currentPage == PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_TEAMTRIP_DESTINATION_FRAGMENT, 5, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;
            case 660:
                if (currentPage == PAGE_FU_MESSAGEVIEW_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_PUSH_MESSAGE, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;
            case 661:
                if (currentPage == PAGE_FU_MESSAGEVIEW_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_FU_PUSH_MESSAGE, 1, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_invite_other_members, R.string.object_settings_team_up_aggregation_point_search, innerEntity.text);
                }
                break;

            //设置-我的消息 页面可见
            case 670:
                if (currentPage == PAGE_MESSAGE_PUSH_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MESSAGE_PUSH_FRAGMENT, 0, ""), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_my_news, innerEntity.text);
                }
                break;
            case 671:
            case 672:
            case 673:
                if (currentPage == PAGE_MESSAGE_PUSH_FRAGMENT_SHOW) {
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MESSAGE_PUSH_FRAGMENT, 1, innerEntity.text), this);
                    Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_home_page_menu, R.string.object_settings_my_news, innerEntity.text);
                }
                break;

        }
    }


    @Override
    public void success(ExtendBaseModel extendBaseModel) {

        if (innerEntity == null) {
            LogUtils.d(TAG, "success: innerEntity == null");
            return;
        }

        LogUtils.d(TAG, "success: " + innerEntity.text + " nMvwScene:" + innerEntity.nMvwScene + " id:" + innerEntity.id);
        if (isMvwForNavi(innerEntity.nMvwScene)) {
            String defaultTts = "",conditionId = "";
            switch (innerEntity.text) {
                case ID_HEAD_FIRST: //高速优先
                    defaultTts = mContext.getString(R.string.map_prefer_highway_ok);
                    conditionId = TtsConstant.NAVIC72CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_high_speed_priority, R.string.scene_navi_route_switch, R.string.condition_navi72);
                    break;
                case ID_NO_HEAD: //不走高速
                    defaultTts = mContext.getString(R.string.map_avoid_highway_ok);
                    conditionId = TtsConstant.NAVIC74CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_no_high_speed, R.string.scene_navi_route_switch, R.string.condition_navi74);
                    break;
                case ID_FEW_CHARGE: //少收费
                case ID_NO_CHARGE: //避免收费
                    defaultTts = mContext.getString(R.string.map_avoid_cost_ok);
                    conditionId = TtsConstant.NAVIC76CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_avoid_charging, R.string.scene_navi_route_switch, R.string.condition_navi76);
                    break;
                case ID_FEW_BLOCK: //躲避拥堵
                case ID_NO_BLOCK: //规避拥堵
                case ID_NO_BLOCK1: //规避拥堵
                    defaultTts = mContext.getString(R.string.map_avoid_traffic_ok);
                    conditionId = TtsConstant.NAVIC78CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_avoid_congestion, R.string.scene_navi_route_switch, R.string.condition_navi78);
                    break;
                case ID_FINISH_NAVI: //结束导航
                case ID_STOP_NAVI: //停止导航
                case ID_EXIT_NAVI://退出导航

                    if (!FloatViewManager.getInstance(mContext).isHide()) {
                        startTTS(mContext.getString(R.string.map_exit_navi_select), TtsConstant.NAVIC44CONDITION, new TTSController.OnTtsStoppedListener() {
                            @Override
                            public void onPlayStopped() {
                                waitMapMultiInterface(PlatformConstant.Operation.CLOSE_MAP);
                                //重新计算超时
                                SRAgent.getInstance().resetSrTimeCount();
                                TimeoutManager.saveSrState(mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                            }
                        });
                    } else {
                        startTTS(mContext.getString(R.string.map_exit_navi_select), TtsConstant.NAVIC44CONDITION);
                    }
                    if (mExitNaviSource == AppConstant.SOURCE_MWV) {
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_stop_navi,TtsConstant.MHXC21CONDITION,R.string.condition_null,mContext.getString(R.string.map_exit_navi_select));
                    } else {
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_control,R.string.object_stop_navi,TtsConstant.NAVIC44CONDITION,R.string.condition_navi44,mContext.getString(R.string.map_exit_navi_select));
                    }
                    break;
                case ID_ZOOMIN: //放大地图
                    defaultTts = mContext.getString(R.string.map_zoom_out_ok);
                    conditionId = TtsConstant.NAVIC54CONDITION;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_enlarge_map,TtsConstant.MHXC15CONDITION,R.string.condition_null,defaultTts);
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_enlarge_map, R.string.scene_navi_map_operation, R.string.condition_navi54);
                    break;
                case ID_ZOOMOUT: //缩小地图
                    defaultTts = mContext.getString(R.string.map_zoom_in_ok);
                    conditionId = TtsConstant.NAVIC56CONDITION;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_reduce_map,TtsConstant.MHXC16CONDITION,R.string.condition_null,defaultTts);
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_reduce_map, R.string.scene_navi_map_operation, R.string.condition_navi56);
                    break;
                case ID_TRAFFIC_ON: //打开路况
                    defaultTts = mContext.getString(R.string.map_road_condition_opened);
                    conditionId = TtsConstant.NAVIC70CONDITION;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_open_road,TtsConstant.MHXC17CONDITION,R.string.condition_null,defaultTts);
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_open_road, R.string.scene_navi_map_operation, R.string.condition_navi70);
                    break;
                case ID_TRAFFIC_OFF: //关闭路况
                    defaultTts = mContext.getString(R.string.map_road_condition_closed);
                    conditionId = TtsConstant.NAVIC71CONDITION;
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_close_road,TtsConstant.MHXC18CONDITION,R.string.condition_null,defaultTts);
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_close_road, R.string.scene_navi_map_operation, R.string.condition_navi71);
                    break;
                case ID_TTS_OFF: //关闭播报
                case ID_TTS_OFF1:
                    startNaviTTS(mContext.getString(R.string.map_close_navi_volume), "");
                    //播报状态：0 关闭 1：打开  其他code  异常
                    LogUtils.d(TAG, "volumeMut:" + extendApi.isVolumeMute());
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_close_speak,TtsConstant.MHXC19CONDITION,R.string.condition_null,mContext.getString(R.string.map_close_navi_volume));

                    break;
                case ID_TTS_ON: //打开播报
                case ID_TTS_ON1:
                    startNaviTTS(mContext.getString(R.string.map_open_navi_volume), "");
                    //播报状态：0 关闭 1：打开  其他code  异常
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_open_speak,TtsConstant.MHXC20CONDITION,R.string.condition_null,mContext.getString(R.string.map_open_navi_volume));
                    LogUtils.d(TAG, "volumeMut:" + extendApi.isVolumeMute());
                    break;
                case ID_DAY_MODE: //白天
                case ID_DAY_MODE1: //白天
                    defaultTts = mContext.getString(R.string.map_switch_daytime_ok);
                    conditionId = TtsConstant.NAVIC68CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_day, R.string.scene_navi_map_operation, R.string.condition_navi68);
                    break;
                case ID_NIGHT_MODE: //黑夜
                case ID_NIGHT_MODE1:
                case ID_NIGHT_MODE2:
                case ID_NIGHT_MODE3:
                    defaultTts = mContext.getString(R.string.map_switch_night_ok);
                    conditionId = TtsConstant.NAVIC66CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_night, R.string.scene_navi_map_operation, R.string.condition_navi66);
                    break;
                case ID_2D_MODE: //2D模式
                case ID_2D_VIEW: //2D视图
                    defaultTts = mContext.getString(R.string.map_switch_2d_ok);
                    conditionId = TtsConstant.NAVIC58CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_2d_view, R.string.scene_navi_map_operation, R.string.condition_navi58);
                    break;
                case ID_3D_MODE: //3D模式
                case ID_3D_VIEW: //3D视图
                    defaultTts = mContext.getString(R.string.map_switch_3d_ok);
                    conditionId = TtsConstant.NAVIC61CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_3d_view, R.string.scene_navi_map_operation, R.string.condition_navi61);
                    break;
                case ID_HEAD_UP: //车头朝上
                    defaultTts = mContext.getString(R.string.map_switch_headup_ok);
                    conditionId = TtsConstant.NAVIC62CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_head_up, R.string.scene_navi_map_operation, R.string.condition_navi62);
                    break;
                case ID_NORTH_UP: //正北朝上
                    defaultTts = mContext.getString(R.string.map_switch_northup_ok);
                    conditionId = TtsConstant.NAVIC64CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_upward_facing_north, R.string.scene_navi_map_operation, R.string.condition_navi64);
                    break;
                case ID_LOOK_ALL: //查看全局
                    defaultTts = mContext.getString(R.string.map_start_navi_success);
                    conditionId = TtsConstant.NAVIC50CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_view_global, R.string.scene_navi_query, R.string.condition_navi50);
                    break;
                case ID_HAWKEYE_MODE_0: //切换到小地图
                    defaultTts = mContext.getString(R.string.map_hawkeye_success);
                    conditionId = TtsConstant.NAVIC82CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_hawkeye_1, R.string.scene_hawkeye_switch, R.string.condition_navi82);
                    break;
                case ID_HAWKEYE_MODE_1: //切换到柱状图
                    defaultTts = mContext.getString(R.string.map_hawkeye_success);
                    conditionId = TtsConstant.NAVIC80CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_hawkeye_0, R.string.scene_hawkeye_switch, R.string.condition_navi80);
                    break;
                    default:
                        LogUtils.e(TAG, "success: not match: " + innerEntity.text + " nMvwScene:" + innerEntity.nMvwScene + " id:" + innerEntity.id);

                        //  文字不匹配，判断语义
                        onSuccess();


                        Utils.exitVoiceAssistant();
                        if (!TextUtils.isEmpty(innerEntity.text) && (innerEntity.text.equals(MVW_WORDS.ID_FINISH_NAVI)
                         || innerEntity.text.equals(MVW_WORDS.ID_STOP_NAVI) || innerEntity.text.equals(MVW_WORDS.ID_EXIT_NAVI))) {
//                            case ID_FINISH_NAVI: //结束导航
//                            case ID_STOP_NAVI: //停止导航
//                            case ID_EXIT_NAVI://退出导航
                            FloatViewManager.getInstance(mContext).sendAwareStateToMX(false);
                        }
            }
        }
    }

    private void onSuccess() {
//        if(PlatformConstant.Operation.ROUTE_PLAN.equals(innerEntity.operation)){
//
//        }
    }

    private void startNaviTTSAndEventTrack(String defaultTts, String conditionId, int objId, int scene, int condition) {
        startNaviTTS(defaultTts, conditionId);
        Utils.eventTrack(mContext,R.string.skill_navi, scene, objId,conditionId,condition);
    }

    @Override
    public void onFail(ExtendErrorModel extendErrorModel) {

        if (innerEntity == null) {
            LogUtils.d(TAG, "onFail: innerEntity == null");
            return;
        }

        LogUtils.d(TAG, "onFail: " + innerEntity.text + ", error: " + extendErrorModel.getErrorMessage()
                + " nMvwScene:" + innerEntity.nMvwScene + " id:" + innerEntity.id
                + ", errorCode:" + extendErrorModel.getErrorCode());
        if (isMvwForNavi(innerEntity.nMvwScene)) {//导航场景

            String defaultTts = "",conditionId = "";
            switch (innerEntity.text) {
                case ID_HEAD_FIRST: //高速优先
//                    startNaviTTS(mContext.getString(R.string.map_prefer_highway_fail), TtsConstant.NAVIC73CONDITION);

                    defaultTts = mContext.getString(R.string.map_prefer_highway_fail);
                    conditionId = TtsConstant.NAVIC73CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_high_speed_priority, R.string.scene_navi_route_switch, R.string.condition_navi73);
                    break;
                case ID_NO_HEAD: //不走高速
//                    startNaviTTS(mContext.getString(R.string.map_avoid_highway_fail), TtsConstant.NAVIC75CONDITION);

                    defaultTts = mContext.getString(R.string.map_avoid_highway_fail);
                    conditionId = TtsConstant.NAVIC75CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_no_high_speed, R.string.scene_navi_route_switch, R.string.condition_navi75);
                    break;
                case ID_FEW_CHARGE: //少收费
                case ID_NO_CHARGE: //避免收费
//                    startNaviTTS(mContext.getString(R.string.map_avoid_cost_fail), TtsConstant.NAVIC77CONDITION);

                    defaultTts = mContext.getString(R.string.map_avoid_cost_fail);
                    conditionId = TtsConstant.NAVIC77CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_avoid_charging, R.string.scene_navi_route_switch, R.string.condition_navi77);
                    break;
                case ID_FEW_BLOCK: //躲避拥堵
                case ID_NO_BLOCK: //规避拥堵
                case ID_NO_BLOCK1: //规避拥堵
//                    startNaviTTS(mContext.getString(R.string.map_avoid_traffic_fail), TtsConstant.NAVIC79CONDITION);

                    defaultTts = mContext.getString(R.string.map_avoid_traffic_fail);
                    conditionId = TtsConstant.NAVIC79CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_avoid_congestion, R.string.scene_navi_route_switch, R.string.condition_navi79);
                    break;
                case ID_ZOOMIN: //放大地图
//                    startNaviTTS(mContext.getString(R.string.map_zoom_out_fail), TtsConstant.NAVIC55CONDITION);

                    defaultTts = mContext.getString(R.string.map_zoom_out_fail);
                    conditionId = TtsConstant.NAVIC55CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_enlarge_map, R.string.scene_navi_map_operation, R.string.condition_navi55);
                    break;
                case ID_ZOOMOUT: //缩小地图
//                    startNaviTTS(mContext.getString(R.string.map_zoom_in_fail), TtsConstant.NAVIC57CONDITION);

                    defaultTts = mContext.getString(R.string.map_zoom_in_fail);
                    conditionId = TtsConstant.NAVIC57CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_reduce_map, R.string.scene_navi_map_operation, R.string.condition_navi57);
                    break;
                case ID_DAY_MODE: //白天
                case ID_DAY_MODE1: //白天
                    defaultTts = mContext.getString(R.string.map_switch_daytime_fail);
                    conditionId = TtsConstant.NAVIC69CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_day, R.string.scene_navi_map_operation, R.string.condition_navi69);
                    break;
                case ID_NIGHT_MODE: //黑夜
                case ID_NIGHT_MODE1:
                case ID_NIGHT_MODE2:
                case ID_NIGHT_MODE3:
                    defaultTts = mContext.getString(R.string.map_switch_night_fail);
                    conditionId = TtsConstant.NAVIC67CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_night, R.string.scene_navi_map_operation, R.string.condition_navi67);
                    break;
                case ID_2D_MODE: //2D模式
                case ID_2D_VIEW: //2D视图
                    defaultTts = mContext.getString(R.string.map_switch_2d_fail);
                    conditionId = TtsConstant.NAVIC59CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_2d_view, R.string.scene_navi_map_operation, R.string.condition_navi59);
                    break;
                case ID_3D_MODE: //3D模式
                case ID_3D_VIEW: //3D视图
//                    startNaviTTS(mContext.getString(R.string.map_switch_3d_fail), TtsConstant.NAVIC60CONDITION);

                    defaultTts = mContext.getString(R.string.map_switch_3d_fail);
                    conditionId = TtsConstant.NAVIC60CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_3d_view, R.string.scene_navi_map_operation, R.string.condition_navi60);
                    break;
                case ID_HEAD_UP: //车头朝上
//                    startNaviTTS(mContext.getString(R.string.map_switch_headup_fail), TtsConstant.NAVIC63CONDITION);

                    defaultTts = mContext.getString(R.string.map_switch_headup_fail);
                    conditionId = TtsConstant.NAVIC63CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_head_up, R.string.scene_navi_map_operation, R.string.condition_navi63);
                    break;
                case ID_NORTH_UP: //正北朝上
//                    startNaviTTS(mContext.getString(R.string.map_switch_northup_fail), TtsConstant.NAVIC65CONDITION);

                    defaultTts = mContext.getString(R.string.map_switch_northup_fail);
                    conditionId = TtsConstant.NAVIC65CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_upward_facing_north, R.string.scene_navi_map_operation, R.string.condition_navi65);
                    break;
                case ID_LOOK_ALL: //查看全局
//                    startNaviTTS(mContext.getString(R.string.map_start_navi_success), TtsConstant.NAVIC49CONDITION);

                    defaultTts = mContext.getString(R.string.map_start_navi_success);
                    conditionId = TtsConstant.NAVIC50CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_view_global, R.string.scene_navi_query, R.string.condition_navi50);
                    break;
                case ID_HAWKEYE_MODE_0: //切换到小地图
                    defaultTts = mContext.getString(R.string.map_hawkeye_success);
                    conditionId = TtsConstant.NAVIC83CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_hawkeye_1, R.string.scene_hawkeye_switch, R.string.condition_navi83);
                    break;
                case ID_HAWKEYE_MODE_1: //切换到柱状图
                    defaultTts = mContext.getString(R.string.map_hawkeye_success);
                    conditionId = TtsConstant.NAVIC81CONDITION;
                    startNaviTTSAndEventTrack(defaultTts, conditionId, R.string.object_hawkeye_0, R.string.scene_hawkeye_switch, R.string.condition_navi81);
                    break;
                default:
                    LogUtils.e(TAG, "onFail: not match: " + innerEntity.text + " nMvwScene:" + innerEntity.nMvwScene + " id:" + innerEntity.id);
//                    startNaviTTS(mContext.getString(R.string.map_start_navi_success), TtsConstant.NAVIC49CONDITION);
                    FloatViewManager.getInstance(mContext).hide();
            }
        }
    }

    @Override
    public void onJSONResult(JSONObject jsonObject) {

    }

    //判断地图的第一页是否在前台
    public boolean isForeground() {
        if (!inited) {
            TAExtendManager.getInstance().init(mContext);
        }

        isExtendApiNull();

        return AppConstant.PACKAGE_NAME_WECARNAVI.equals(ActivityManagerUtils.getInstance(mContext)
                .getTopPackage()) && extendApi != null && extendApi.isNaviScreen() == 1;
    }

    //回到地图的第一页
    public void backToMap(Callback callback) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        BaseApplication.getInstance().startActivity(intent);

        if (!inited) {
            TAExtendManager.getInstance().init(mContext);
        }

        if (!isExtendApiNull())
            extendApi.setNaviScreen(new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "success");
                if (callback != null) {
                    callback.success();
                }
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "onFail:" + extendErrorModel.getErrorCode());
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }

    /**
     * @param type  0 小地图模式； 1 路况条模式
     */
    public void setHawkeyeModel(int type) {
        LogUtils.d(TAG, "setHawkeyeModel: " + type);
        boolean isSuccess = extendApi.setHawkeyeModel(type) == 10000;

        if (isSuccess) {
            success(new ExtendBaseModel());
        } else {
            onFail(new ExtendErrorModel(-1, "setHawkeyeModel"));
        }
    }

    public interface Callback {
        void success();
    }

    public void startTTS(String defaultTts, String conditionId) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }
                Utils.startTTS(ttsText);
            }
        });
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

    private String calcRemainTimeAndDistance(int remainTime, int remainDistance) {
        String formatTimeS = formatTimeS(remainTime);
        String s = String.format("距离目的地还有%d%s", remainDistance, "米,") + formatTimeS;
        Log.e("zheng","zheng111111111111 calcRemainTimeAndDistance"+s);
        if (remainDistance < 1000) {
            return String.format("距离目的地还有%d%s", remainDistance, "米,") + formatTimeS;
        } else if (remainDistance % 1000 == 0) {
            return String.format("距离目的地还有%.1f%s", remainDistance / 1000, "公里,") + formatTimeS;
        } else {
            return String.format("距离目的地还有%.1f%s", remainDistance / 1000.0f, "公里,") + formatTimeS;
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
        } else {
            sb.append("预计").append(day).append("日").append(hour).append("点").append(min).append("分到达");
        }
        return sb.toString();
    }

    public void updateMyLocation() {
        LogUtils.d(TAG, "updateMyLocation: 获取当前位置");
        if (!isExtendApiNull())
            extendApi.showMyLocation(1, new IExtendCallback<LocationInfo>() {
            @Override
            public void success(LocationInfo locationInfo) {
                LogUtils.d(TAG, "endLoc:" + locationInfo);
                double longitude = locationInfo.getLongitude();
                double latitude = locationInfo.getLatitude();

                if (latitude == -1 || latitude == 4.9E-324) {
                    LogUtils.d(TAG, "纬度获取失败");
                    return;
                }
                if (longitude == -1 || longitude == 4.9E-324) {
                    LogUtils.d(TAG, "经度获取失败");
                    return;
                }
                SharedPreferencesUtils.saveString(mContext, AppConstant.LATITUDE, latitude + "");
                SharedPreferencesUtils.saveString(mContext, AppConstant.LONGITUDE, longitude + "");
                SharedPreferencesUtils.saveString(mContext, AppConstant.CITY_NAME, locationInfo.getCityName());
                if(SRAgent.getInstance().SrInstance != null) {
                    int ret1 = SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_LATITUDE, latitude + "");
                    int ret2 = SRAgent.getInstance().SrInstance.setParam(SrSession.ISS_SR_PARAM_LONGTITUDE, longitude + "");

                    if (ret1 == 0 && ret2 == 0) {
                        LogUtils.d(TAG, "纬度设置成功。latitude=" + latitude
                                + ",经度设置成功。longitude=" + longitude);
                    }
                }

            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "onFail:" + extendErrorModel.getErrorMessage());
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }

    private IExtendListener mNaviStatusChangedListener = new IExtendListener() {
        @Override
        public void onJSONReceived(JSONObject jsonObject) {
        }

        @Override
        public void onModelReceived(ExtendBaseModel receivedModel) {
            if (receivedModel.getExtendId() == ExtendConstants.ExtendId.NAVI_STATUS_CHANGED) {
                ExtendTAStatusModel mode = (ExtendTAStatusModel) receivedModel;
                switch (mode.getTAStatus()) {
                    case 1:
                        LogUtils.d(TAG, "导航开始  isExitShowShow: "  + isExitShowShow());
                        SRAgent.getInstance().setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");//将可见即可说清空，
                        if (TspSceneAdapter.getTspScene(mContext) != TspSceneAdapter.TSP_SCENE_NAVI &&
                                TspSceneAdapter.getTspScene(mContext) != TspSceneAdapter.TSP_SCENE_SELECT &&
                                TspSceneAdapter.getTspScene(mContext) != TspSceneAdapter.TSP_SCENE_DIALOG) {
                            TspSceneManager.getInstance().resetScrene(mContext,-1);
                        }
                        EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.NAVI,null,"start"));
                        break;
                    case 2:
                        LogUtils.d(TAG, "导航结束");
                        EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.NAVI,null,null));
                        if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_NAVI && !isForeground()) {
                            mvwAgent.stopMVWSession();
//                            mvwAgent.setMvwKeyWords(MvwSession.ISS_MVW_SCENE_ANSWER_CALL, Utils.getFromAssets(mContext, "mvw_answer_call.json"));
                            mvwAgent.startMVWSession(TspSceneAdapter.TSP_SCENE_GLOBAL);
                        }
                        break;
                    case 3:
                        EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.NAVI,null,null));
                        LogUtils.d(TAG, "模拟导航开始  isExitShowShow: "  + isExitShowShow());
                        if (TspSceneAdapter.getTspScene(mContext) != TspSceneAdapter.TSP_SCENE_NAVI && TspSceneAdapter.getTspScene(mContext) != TspSceneAdapter.TSP_SCENE_DIALOG) {
                            mvwAgent.stopMVWSession();
//                            mvwAgent.setMvwKeyWords(MvwSession.ISS_MVW_SCENE_ANSWER_CALL, Utils.getFromAssets(mContext, "mvw_navi.json"));
                            mvwAgent.startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
                        }
                        break;
                    case 4:
                        EventBus.getDefault().post(new CommandEvent(CommandEvent.CommadType.NAVI,null,null));
                        LogUtils.d(TAG, "模拟导航结束");
                        if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_NAVI && !isForeground()) {
                            mvwAgent.stopMVWSession();
//                            mvwAgent.setMvwKeyWords(MvwSession.ISS_MVW_SCENE_ANSWER_CALL, Utils.getFromAssets(mContext, "mvw_answer_call.json"));
                            mvwAgent.startMVWSession(TspSceneAdapter.TSP_SCENE_GLOBAL);
                        }
                        break;
                    case 5:
                        LogUtils.d(TAG, "进入路线规划页面");
                        /***********欧尚修改开始*****************/
                        //恢复美行播报（处理高德关闭美行播报后，由于高德异常退出时(被杀/关机)，未恢复美行播报的情况）
                        GDSdkManager.getInstance(mContext).openMXBroadCast();
                        /***********欧尚修改结束*****************/
                        break;
                }
            }
        }
    };

    public boolean isNaving() {
        if (!isExtendApiNull()) {
            try {
                int status = extendApi.getNaviState();
                LogUtils.d(TAG, "getNaviState --> " + status);
                if (status == 2) {
                    return true;
                } else {

                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private IPageChangedListener mPageChangedListener = new IPageChangedListener() {
        @Override
        public void onPageChanged(int i) {

            try {
                handlePageChanged(i);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    // 标记退出导航对话框是否显示
//    private boolean isExitDialogShow;
    private void handlePageChanged(int i) {

        /***********欧尚修改开始*****************/
        //恢复美行播报（处理高德关闭美行播报后，由于高德异常退出时(被杀/关机)，未恢复美行播报的情况）
        GDSdkManager.getInstance(mContext).openMXBroadCast();
        /***********欧尚修改结束*****************/

        boolean floatViewIsHide = FloatViewManager.getInstance(mContext).isHide();
        String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        boolean isNaving = isNaving();


//        if (i == VIEW_NAVI_EXIT_DIALOG_HIDE) {
//            isExitDialogShow = false;
//        } else if (i == VIEW_NAVI_EXIT_DIALOG_SHOW){
//            isExitDialogShow = true;
//        }


        LogUtils.d(TAG, "currentPage=" + i + "  topPackage: " + topPackage + "  isNaving: " + isNaving + "  floatViewIsHide: " + floatViewIsHide
        + "  isExitDialogShow: " + isExitShowShow()+"l.."+(i % 2));

        if (mvwAgent == null) {
            mvwAgent = MVWAgent.getInstance();
        }

        if (i % 2 == 0) { //page hide回调情况



            if (!AppConstant.PACKAGE_NAME_WECARNAVI.equals(topPackage)) {//地图第二屏也算地图场景
                currentPage = 0;
                if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_NAVI && !isNaving) {
                    LogUtils.d(TAG, "switch to MVW TSP_SCENE_GLOBAL");
                    mvwAgent.stopMVWSession();
                    mvwAgent.startMVWSession(TspSceneAdapter.TSP_SCENE_GLOBAL);
                }
                //恢复默认
                if (!AppConstant.PACKAGE_NAME_MUSIC.equals(topPackage) && !AppConstant.PACKAGE_NAME_RADIO.equals(topPackage)
                        && !mContext.getPackageName().equals(topPackage)) {
                       //不是主界面
                       // tts 没有播报，防止在播报过程中，动画时是被状态
                      //语音是显示状态
                    if (!floatViewIsHide&&!TTSController.getInstance(mContext).isTtsPlaying()&&!FloatViewManager.getInstance(mContext).isHide()) {
                        SRAgent srAgent=SRAgent.getInstance();
                        if (srAgent == null) {
                            return;
                        }
                        srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");
                        srAgent.mSrArgu_Old = null;
                        srAgent.stopSRSession();
                        srAgent.startSRSession();
                    }
                }
            } else if (i == PAGE_MULTIROUTE_FRAGMENT_HIDE) {
                // 线路规划界面消失后取消可见即可说  有时候没有回调 i=9造成可见即可说没有取消
                LogUtils.d(TAG, "i == PAGE_MULTIROUTE_FRAGMENT_HIDE   stopSrAndRestart()");
                //不是主界面
                // tts 没有播报，防止在播报过程中，动画时是被状态
                //语音是显示状态
                if(!floatViewIsHide&&!TTSController.getInstance(mContext).isTtsPlaying()&&!FloatViewManager.getInstance(mContext).isHide()){
                    SRAgent.getInstance().setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");//将可见即可说清空，
                    SRAgent.getInstance().mSrArgu_Old = null;
                }

            }
        } else {//page show回调情况
            currentPage = i;

            if (!AppConstant.PACKAGE_NAME_WECARNAVI.equals(topPackage) && TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_SELECT) {
                // 导航不在栈顶、同时在选择场景，不切换到导航场景 fix bug ID1017074
                LogUtils.e(TAG, " WECARNAVI not at top and  TspScene == TSP_SCENE_SELECT  return");
                return;
            }

            if(ActiveServiceViewManager.ActiveServiceView_Show){
                Log.e(TAG, "handlePageChanged: the weixin to car is shown,do not switch scene");
                return;
            }

            if (TspSceneAdapter.getTspScene(mContext) != TspSceneAdapter.TSP_SCENE_NAVI && !isExitShowShow()) {
                // 退出导航dialog可见，不切换场景
                LogUtils.d(TAG, "switch to NAVI SCENE");
                mvwAgent.stopMVWSession();
//                    mvwAgent.setMvwKeyWords(MvwSession.ISS_MVW_SCENE_ANSWER_CALL, Utils.getFromAssets(mContext, "mvw_navi.json"));
                mvwAgent.startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
            }
            String stkCmd = "";

            switch (i) {
                case PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW: //集结点搜索可见(370-375)
                    stkCmd = Utils.getFromAssets(mContext, "stks/teamtrip_destination.json");
                    fromSetHomeOrComp = false;
                    getSearchHistoryData(currentPage, floatViewIsHide, stkCmd);
                    return;
                case PAGE_MY_FAVORITE_CAR_SHOW: //我的爱车页面可见(360-362)
                    stkCmd = Utils.getFromAssets(mContext, "stks/my_favorite_car.json");
                    break;
                case PAGE_TEAMTRIP_SETTING_FRAGMENT_SHOW://集结设置页面可见集结页面可见(350-351)
                    stkCmd = Utils.getFromAssets(mContext, "stks/teamtrip_setting.json");
                    break;
                case PAGE_TEAM_TRIP_FRAGMENT_SHOW://集结页面可见(340)
                    stkCmd = Utils.getFromAssets(mContext, "stks/team_trip.json");
                    break;
                case PAGE_TEAM_TRIP_SETTING_EXIT_DIAGOG_SHOW://退出集结dialog显示(400-001)
                    stkCmd = Utils.getFromAssets(mContext, "stks/team_trip_setting_exit_dialog.json");
                    break;
                case PAGE_POINTSELECT_ADDFAVORITE_FRAGMENT_SHOW://地图选点（添加普通收藏点）页面可见(330-331)
                    //todo  美行反馈无收藏普通收藏点需求
                    stkCmd = Utils.getFromAssets(mContext, "stks/pointselect_addfavorite.json");
                    break;
                case PAGE_POINTSELECT_TEAMTRIP_FRAGMENT_SHOW: //地图选点（集结点设置）页面可见(320-321)
                    stkCmd = Utils.getFromAssets(mContext, "stks/pointselect_teamtrip.json");
                    break;
                case PAGE_FAVORITE_FRAGMENT_TEAMTRIP_SHOW: //收藏夹（集结点设置）页面可见(310-313)
                    stkCmd = Utils.getFromAssets(mContext, "stks/favorite_teamtrip.json");
                    break;
                case PAGE_FAVORITE_FRAGMENT_ADDPASS_SHOW: //收藏夹（添加途径地）页面可见(300-303)
                    stkCmd = Utils.getFromAssets(mContext, "stks/favorite_addpass.json");
                    break;
                case PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW: //微信位置页面可见(290-294)
                    stkCmd = Utils.getFromAssets(mContext, "stks/searchresult_list_wxposition.json");
                    getWeChatLocationData(currentPage, floatViewIsHide, stkCmd);
                    return;
                case PAGE_SEARCHRESULT_LIST_SENDPHOME_FRAGMENT_SHOW: //检索结果页（发送手机）页面可见(280-281)
                    //todo  美行反馈无发送手机需求
                    stkCmd = Utils.getFromAssets(mContext, "stks/searchresult_list_sendphome.json");
                    break;
                case PAGE_SEARCHRESULT_LIST_ADDFAVORITE_FRAGMENT_SHOW://检索结果页（收藏普通收藏点）页面可见(270-271)
                    //todo  美行反馈无收藏普通收藏点需求
                    stkCmd = Utils.getFromAssets(mContext, "stks/searchresult_list_addfavorite.json");
                    break;
                case PAGE_SEARCHRESULT_LIST_ADDPASS_FRAGMENT_SHOW://检索结果页（添加途径地）页面可见(260-261)
                    stkCmd = Utils.getFromAssets(mContext, "stks/searchresult_list_addpass.json");
                    break;
                case PAGE_SEARCHRESULT_LIST_TEAMTRIP_FRAGMENT_SHOW://检索结果页（设置集结点）页面可见(250-254)
                    stkCmd = Utils.getFromAssets(mContext, "stks/searchresult_list_teamtrip.json");
                    break;
                case PAGE_SEARCHRESULT_LIST_ADDCOMP_FRAGMENT_SHOW: //检索结果页（设置公司）页面可见(240-244)
                    stkCmd = Utils.getFromAssets(mContext, "stks/searchresult_list_addcomp.json");
                    break;
                case PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW: //检索结果页（设置家）页面可见(230-234)
                    stkCmd = Utils.getFromAssets(mContext, "stks/searchresult_list_addhome.json");
                    getWeChatLocationData(currentPage, floatViewIsHide, stkCmd);
                    break;
                case PAGE_POINTSELECT_ADDCOMP_FRAGMENT_SHOW: //地图选点（设置公司）页面可见(220-224)
                    stkCmd = Utils.getFromAssets(mContext, "stks/pointselect_addcomp.json");
                    break;
                case PAGE_POINTSELECT_ADDHOME_FRAGMENT_SHOW: //地图选点（设置家）页面可见(210-211)
                    stkCmd = Utils.getFromAssets(mContext, "stks/pointselect_addhome.json");
                    break;
                case PAGE_POINTSELECT_ADDPASS_FRAGMENT_SHOW: //地图选点（添加途径地）页面可见(200-201)
                    stkCmd = Utils.getFromAssets(mContext, "stks/pointselect_addpass.json");
                    break;
                case VIEW_MORE_FUNCTION_SHOW: //主页菜单可见(190-193)
                    stkCmd = Utils.getFromAssets(mContext, "stks/view_more_function.json");
                    break;
                case PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW: //设置家或者公司页面可见(180-183)
                    stkCmd = Utils.getFromAssets(mContext, "stks/set_homeorcomp.json");
                    fromSetHomeOrComp = true;
                    getSearchHistoryData(currentPage, floatViewIsHide, stkCmd);
                    return;
                case PAGE_SUGGESTION_FRAGMENT_SHOW: //设置家或者公司页面可见(170-171)
                    stkCmd = Utils.getFromAssets(mContext, "stks/page_suggestion.json");
                    getSuggestionData(currentPage, floatViewIsHide, stkCmd);
                    return;
                case VIEW_NAVI_EXIT_DIALOG_SHOW://导航中-退出导航dialog可见 (160-161)
                    mvwAgent.stopMVWSession();
                    mvwAgent.startMVWSession(TspSceneAdapter.TSP_SCENE_DIALOG);
                    return;
//                        stkCmd = Utils.getFromAssets(mContext, "stks/exit_navi.json");
//                        break;
                case PAGE_FAVORITE_FRAGMENT_SHOW://收藏夹页面可见 (150-153)
                    stkCmd = Utils.getFromAssets(mContext, "stks/favorite.json");
                    getFavoritesData(currentPage, floatViewIsHide, stkCmd);
                    return;
                case PAGE_NEARBY_FRAGMENT_SHOW://周边检索页面可见 (140-149, 1400-1423)
                    stkCmd = Utils.getFromAssets(mContext, "stks/page_nearby.json");
                    break;
                case VIEW_MULTIROUTE_ROUTEPASS_SHOW://多路线-添加途径地页可见 (130-131)
                    stkCmd = Utils.getFromAssets(mContext, "stks/view_multiroute_routepass.json");
                    break;
                case VIEW_MULTIROUTE_PREFERENCE_SHOW://多路线-路线偏好页可见 (120-121)
                    stkCmd = Utils.getFromAssets(mContext, "stks/view_multiroute_preference.json");
                    break;
                case VIEW_MULTIROUTE_DETAIL_SHOW://多路线-路线详情页可见 (100-101)
                    stkCmd = Utils.getFromAssets(mContext, "stks/view_multiroute_detail.json");
                    break;
                case VIEW_PICK_UP_SHOW://首页拾取页面可见 (100-104)
                    stkCmd = Utils.getFromAssets(mContext, "stks/view_pickup.json");
                    break;
                case PAGE_SETTING_FRAGMENT_SHOW://设置页面可见 (90) 设置界面中路线偏好/黑白天模式使用免唤醒实现
                    stkCmd = Utils.getFromAssets(mContext, "stks/setting.json");
                    break;
                case PAGE_POINTSELECT_FRAGMENT_SHOW://地图选点页面可见 (80-81)
                    stkCmd = Utils.getFromAssets(mContext, "stks/route_select.json");
                    break;
                case PAGE_ROUTEPASS_FRAGMENT_SHOW://添加途径地页面可见 (70-74)
                    stkCmd = Utils.getFromAssets(mContext, "stks/route_pass.json");
                    break;
                case PAGE_ROUTEGUIDE_FRAGMENT_SHOW://导航页可见 (60)
                   /* stkCmd = Utils.getFromAssets(mContext, "stks/route_guide.json");
                    break;*/
                    stopSrAndRestart();
                   return;
                case PAGE_MULTIROUTE_FRAGMENT_SHOW://多路线页面可见 (50-58)
                    stkCmd = Utils.getFromAssets(mContext, "stks/multi_route.json");
                    getRouteSummaryData(floatViewIsHide, stkCmd);
                    return;
                case PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW://检索结果页可见 (40-45)
                    stkCmd = Utils.getFromAssets(mContext, "stks/search_result.json");
                    getSearchPoiData(currentPage, floatViewIsHide, stkCmd);
                    return;
                case PAGE_SEARCH_FRAGMENT_SHOW://检索页可见 (30-33)
                    stkCmd = Utils.getFromAssets(mContext, "stks/search.json");
                    break;
                case PAGE_FU_FRAGMENT_SHOW://首页可见 (65-66)
                 /*   stkCmd = Utils.getFromAssets(mContext, "stks/fu.json");
                    break;*/
                    stopSrAndRestart();
                     return;
                case PAGE_FU_MESSAGEVIEW_SHOW://首页消息悬浮框显示（660-661）
                    stkCmd = Utils.getFromAssets(mContext, "stks/fu_message.json");
                    break;
                case PAGE_MESSAGE_PUSH_FRAGMENT_SHOW://设置-我的消息 页面可见
                    stkCmd = Utils.getFromAssets(mContext, "stks/my_message.json");
                    getWeChatLocationData(currentPage, floatViewIsHide, stkCmd);
                    return;

                case VIEW_CLEAR_HISTORY_DIALOG_SHOW://删除历史记录dialog显示
                    stkCmd = Utils.getFromAssets(mContext, "stks/clear_history.json");
                    break;

            }


            startSRSession(floatViewIsHide, stkCmd);

        }
    }

    private void startSRSession(boolean floatViewIsHide, String stkCmd) {

        SRAgent srAgent=SRAgent.getInstance();
        if (srAgent == null) {
            return;
        }

        srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_STKS, stkCmd);
        if (!floatViewIsHide) {
            //保存mSrArgu_Old为可见即可说模式，以便悬浮窗退出时恢复检索
            srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");
            srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
            srAgent.mSrArgu_Old.scene = SrSession.ISS_SR_SCENE_STKS;
            srAgent.mSrArgu_Old.szCmd = stkCmd;
        } else {
            srAgent.stopSRSession();
            srAgent.startSRSession();
        }
    }


    private List<AllDimension> getCurrentAllDimensions(int currentPage, List<LocationInfo> resultList) {
        List<AllDimension> allDimensions = new ArrayList<>();
        switch (currentPage) {
            //历史记录列表相关
            case PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_SET_HOMEORCOMP_FRAGMENT_STARTID, resultList, AllDimensionUtils.HISTORY_SIZE);
                break;
            case PAGE_TEAMTRIP_DESTINATION_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_TEAMTRIP_DESTINATION_FRAGMENT_STARTID, resultList, AllDimensionUtils.HISTORY_SIZE);
                break;


            //微信位置列表相关
            case PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_SEARCHRESULT_LIST_WXPOSITION_FRAGMENT_STARTID, resultList, AllDimensionUtils.WXPOSITION_SIZE);
                break;
            case PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_SEARCHRESULT_LIST_ADDHOME_FRAGMENT_STARTID, resultList, AllDimensionUtils.WXPOSITION_SIZE);
                break;
            case PAGE_MESSAGE_PUSH_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_MESSAGE_PUSH_FRAGMENT_STARTID, resultList, AllDimensionUtils.WXPOSITION_SIZE);
                break;


            //收藏夹列表相关
            case PAGE_FAVORITE_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_FAVORITE_FRAGMENT_STARTID, resultList, AllDimensionUtils.FAVORITE_SIZE);
                break;

            //检索结果集
            case PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_SEARCHRESULT_LIST_FRAGMENT_STARTID, resultList, AllDimensionUtils.SEARCHRESULT_SIZE);
                break;
            //关联词列表相关
            case PAGE_SUGGESTION_FRAGMENT_SHOW:
                allDimensions = AllDimensionUtils.getAllDimensions(AllDimensionUtils.PAGE_SUGGESTION_FRAGMENT_STARTID, resultList, AllDimensionUtils.SUGGESTION_SIZE);
                break;
        }

        LogUtils.d(TAG, "allDimensions:" + allDimensions);
        return allDimensions;
    }

    //获取检索页结果页结果集
    public void getRouteSummaryData(final boolean floatViewIsHide, final String stkCmd) {
        if (!isExtendApiNull())
            extendApi.getRouteSummaryList(new IExtendCallback<RouteSummaryModel>() {
            @Override
            public void success(RouteSummaryModel routeSummaryModel) {
                List<AllDimension> allDimensions = AllDimensionUtils.getAllDimensionsByRouteSummary(AllDimensionUtils.PAGE_MULTIROUTE_FRAGMENT_STARTID, routeSummaryModel.getResultList(), AllDimensionUtils.ROUTESUMMARY_SIZE);
                startSRSession(floatViewIsHide, AllDimensionUtils.getAllStkCmd(allDimensions, stkCmd));
                LogUtils.d(TAG, "getRouteSummaryData success:" + allDimensions.toString());
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "getRouteSummaryData errorCode:" + extendErrorModel.getErrorCode() + "  errorMessage:" + extendErrorModel.getErrorMessage());
                startSRSession(floatViewIsHide, stkCmd);
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });
    }

    //获取关联词列表数据
    public void getSuggestionData(final int currentPage, final boolean floatViewIsHide, final String stkCmd) {
        if (!isExtendApiNull())
            extendApi.addSuggestDataListener(new ISuggestionResultListener() {
            @Override
            public void onSuggestionResultChanged(List<LocationInfo> list) {
                LogUtils.d("onSearchResultChanged", list.toString());
                if (list.isEmpty()) {
                    startSRSession(floatViewIsHide, stkCmd);
                    return;
                }
                List<AllDimension> allDimensions = getCurrentAllDimensions(currentPage, list);
                startSRSession(floatViewIsHide, AllDimensionUtils.getAllStkCmd(allDimensions, stkCmd));
            }
        });

    }


    //获取检索页结果页结果集
    public void getSearchPoiData(final int currentPage, final boolean floatViewIsHide, final String stkCmd) {
        Log.d(TAG, "getSearchPoiData() called with: currentPage = [" + currentPage + "], floatViewIsHide = [" + floatViewIsHide + "], stkCmd = [" + stkCmd + "]"+isExtendApiNull());
        if (!isExtendApiNull()){
            extendApi.addSearchResultListener(new ISearchResultListener() {
                @Override
                public void onSearchResultChanged(List<LocationInfo> list) {
                    LogUtils.d(TAG, "onSearchResultChanged：" + list.toString());
                    if (list.isEmpty()) {
                        startSRSession(floatViewIsHide, stkCmd);
                        return;
                    }
                    List<AllDimension> allDimensions = getCurrentAllDimensions(currentPage, list);
                    startSRSession(floatViewIsHide, AllDimensionUtils.getAllStkCmd(allDimensions, stkCmd));
                }
            });
        }

    }


    //获取历史记录列表
    private void getSearchHistoryData(final int currentPage, final boolean floatViewIsHide, final String stkCmd) {
        if (!isExtendApiNull())
            extendApi.getSearchHistoryData(new IExtendCallback<SearchResultModel>() {
            @Override
            public void success(SearchResultModel searchResultModel) {
                LogUtils.d(TAG, "historyData searchResultModel==" + Arrays.toString(searchResultModel.getResultList().toArray()));
                List<AllDimension> allDimensions = getCurrentAllDimensions(currentPage, searchResultModel.getResultList());
                startSRSession(floatViewIsHide, AllDimensionUtils.getAllStkCmd(allDimensions, stkCmd));
            }

            @Override
            public void onFail(ExtendErrorModel errorModel) {
                LogUtils.d(TAG, "historyData errorCode:" + errorModel.getErrorCode() + "  errorMessage:" + errorModel.getErrorMessage());
                startSRSession(floatViewIsHide, stkCmd);
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });

    }


    //获取微信位置列表
    private void getWeChatLocationData(final int currentPage, final boolean floatViewIsHide, final String stkCmd) {
        if (!isExtendApiNull())
            extendApi.addSearchResultListener(new ISearchResultListener() {
            @Override
            public void onSearchResultChanged(List<LocationInfo> list) {
                LogUtils.d(TAG, "onSearchResultChanged：" + list.toString());
                if (list.isEmpty()) {
                    startSRSession(floatViewIsHide, stkCmd);
                    return;
                }
                List<AllDimension> allDimensions = getCurrentAllDimensions(currentPage, list);
                startSRSession(floatViewIsHide, AllDimensionUtils.getAllStkCmd(allDimensions, stkCmd));

            }
        });

    }


    //获取收藏夹列表
    private void getFavoritesData(final int currentPage, final boolean floatViewIsHide, final String stkCmd) {
        if (!isExtendApiNull())
            extendApi.getFavoriteList(new IExtendCallback<SearchResultModel>() {
            @Override
            public void success(SearchResultModel searchResultModel) {
                LogUtils.d(TAG, "FavoriteList searchResultModel==" + Arrays.toString(searchResultModel.getResultList().toArray()));
                List<AllDimension> allDimensions = getCurrentAllDimensions(currentPage, searchResultModel.getResultList());
                startSRSession(floatViewIsHide, AllDimensionUtils.getAllStkCmd(allDimensions, stkCmd));

            }

            @Override
            public void onFail(ExtendErrorModel errorModel) {
                LogUtils.d(TAG, "FavoriteList errorCode:" + errorModel.getErrorCode() + "  errorMessage:" + errorModel.getErrorMessage());
                startSRSession(floatViewIsHide, stkCmd);
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });
    }

    IExtendCallback startNaviCallback = new IExtendCallback() {
        @Override
        public void success(ExtendBaseModel extendBaseModel) {
            LogUtils.d(TAG, "startNaviCallback： success ");
//            String ttsText = mContext.getString(R.string.map_start_navi);
//            Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_control, R.string.object_start_navi, TtsConstant.NAVIC43CONDITION, R.string.condition_navi43, ttsText);
//            startNaviTTS(ttsText, TtsConstant.NAVIC43CONDITION);

        }

        @Override
        public void onFail(ExtendErrorModel extendErrorModel) {
            LogUtils.d(TAG, "startNaviCallback： onFail " + extendErrorModel.getErrorCode() + "  " + extendErrorModel.getErrorMessage());
            noToDo();
        }

        @Override
        public void onJSONResult(JSONObject jsonObject) {
            LogUtils.d(TAG, "startNaviCallback： onJSONResult ");
        }
    };

    public void startNavi() {
        LogUtils.d(TAG, "startNavi： currentPage " + currentPage);
        if (currentPage == PAGE_MULTIROUTE_FRAGMENT_SHOW) {
            if (!isExtendApiNull())
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_MULTI_ROUTE, 6, ""), startNaviCallback);
        } else if (currentPage == VIEW_MULTIROUTE_ROUTEPASS_SHOW) {
            if (!isExtendApiNull())
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_MULTIROUTE_ROUTE_PASS, 1, ""), startNaviCallback);
        } else {
            if(!FloatViewManager.getInstance(mContext).isHide())
                noToDo();//不进行后台播放
        }
    }

    private void noToDo() {
        if(FloatViewManager.getInstance(mContext).isHide()){
            Log.e(TAG, "noToDo: !!!"+FloatViewManager.getInstance(mContext).isHide());
            return;
        }

        String originTts = "#VOICENAME#";
        String replaceTts = Settings.System.getString(mContext.getContentResolver(), "aware");
        String defaultTts = Utils.replaceTts(mContext.getString(R.string.navi_cannot_begin), originTts, "" + replaceTts);
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MAINC14_1CONDITION, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                } else {
                    ttsText = Utils.replaceTts(tts, originTts, replaceTts);
                }
                Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                            EventBusUtils.sendExitMessage();
                        }
                    }
                });
            }
        });
    }


    public boolean isExitShowShow() {
        return currentPage == VIEW_NAVI_EXIT_DIALOG_SHOW;
    }


    public boolean isViewPickShow() {
        return currentPage == MXSdkManager.VIEW_PICK_UP_SHOW;
    }

    public boolean isPageFuFragmentShow() {
        return currentPage == MXSdkManager.PAGE_FU_FRAGMENT_SHOW;
    }

    public void collectLocation(LocationInfo info, String tag) {

        // {"slots":{"insType":"COLLECT"}} {"slots":{"insType":"COLLECT","tag":"家"}}  {"slots":{"insType":"COLLECT","tag":"公司"}}
        // 0=普通收藏点  1=家  2=公司

        int type = ConstantsApp.MAP_COLLECT_DEFAULT;
        if (mContext.getString(R.string.home).equals(tag)) {
            type = ConstantsApp.MAP_COLLECT_HOME;
        } else if (mContext.getString(R.string.company).equals(tag)) {
            type = ConstantsApp.MAP_COLLECT_COMPANY;
        }

        if (!isExtendApiNull())

            extendApi.collectByPoi(type, info, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "collectMyLocation success");
                ttsOnCollected();
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "collectByPoi onFail:" + extendErrorModel.getErrorCode());
                startNaviTTS(mContext.getString(R.string.no_network_tip), TtsConstant.MAINC19CONDITION);
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }

    private void ttsOnCollected(){
        startNaviTTS(mContext.getString(R.string.map_colect_current_Poi_ok),TtsConstant.NAVIC49CONDITION);
        Utils.eventTrack(mContext,R.string.skill_navi, R.string.scene_navi_control, R.string.object_collect_location,TtsConstant.NAVIC49CONDITION,R.string.condition_navi49, mContext.getString(R.string.condition_navi49));
    }

    public void collectCurrentPoi(String tag) {
        /***********欧尚修改开始*****************/
        if(GDSdkManager.getInstance(mContext).addCollection()){
            return;
        }
        /***********欧尚修改结束*****************/

        LogUtils.d(TAG, "currentPage:" + currentPage);
        if (isViewPickShow()) {
            if (!isExtendApiNull())
                extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.VIEW_PICKUP, 0, ""), new IExtendCallback() {
                @Override
                public void success(ExtendBaseModel extendBaseModel) {
                    if (NetworkUtil.isNetworkAvailable(mContext)) {//暂时这么处理。等美行在无网时回调onFail时再到OnFail里处理
                        ttsOnCollected();
                    } else {
                        startNaviTTS(mContext.getString(R.string.no_network_tip), TtsConstant.MAINC19CONDITION);
                    }
                }

                @Override
                public void onFail(ExtendErrorModel extendErrorModel) {
                    LogUtils.d(TAG, "onFail");
                    startNaviTTS(mContext.getString(R.string.no_network_tip), TtsConstant.MAINC19CONDITION);
                }

                @Override
                public void onJSONResult(JSONObject jsonObject) {

                }
            });
        } else {
//            startNaviTTS(mContext.getString(R.string.map_colect_current_Poi_fai),"");

            if (!isExtendApiNull())
                extendApi.showMyLocation(isForeground() ? 0 : 1, new IExtendCallback<LocationInfo>() {
                    @Override
                    public void success(LocationInfo locationInfo) {
                        collectLocation(locationInfo, tag);
                    }

                    @Override
                    public void onFail(ExtendErrorModel extendErrorModel) {
                        LogUtils.d(TAG, "collectCurrentPoi onFail:" + extendErrorModel.getErrorMessage());
                            startNaviTTS(mContext.getString(R.string.no_network_tip), TtsConstant.MAINC19CONDITION);
                    }

                    @Override
                    public void onJSONResult(JSONObject jsonObject) {

                    }
                });
        }
    }

    //open 0,close:1
    public void setMapOperaSwitchRoadCondition(int open) {
        if (!isExtendApiNull() && isForeground()) {

            extendApi.mapOpera(MAP_OPERA_SWITCH_ROAD_CONDITION, open, new IExtendCallback() {
                @Override
                public void success(ExtendBaseModel extendBaseModel) {
                    LogUtils.d(TAG, "setMapOperaSwitchRoadCondition success");
                    if (open == 0) {
                        startNaviTTS(mContext.getString(R.string.map_road_condition_opened), TtsConstant.NAVIC70CONDITION);
                    } else {
                        startNaviTTS(mContext.getString(R.string.map_road_condition_closed), TtsConstant.NAVIC71CONDITION);
                    }
                }

                @Override
                public void onFail(ExtendErrorModel extendErrorModel) {
                    LogUtils.d(TAG, "setMapOperaSwitchRoadCondition onFail");
                }

                @Override
                public void onJSONResult(JSONObject jsonObject) {

                }
            });

        }
    }


    public void startNaviTTS(String defaultTts, String conditionId) {
        Log.e("zheng","zheng1"+defaultTts);
        Utils.getMessageWithTtsSpeakOnly(mContext, conditionId, defaultTts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                    EventBusUtils.sendExitMessage();
                }
            }
        });
    }



    public void stopNaviMutual(String text) {
        innerEntity.id = -1;//防止播报两次
        innerEntity.text = innerEntity.text + "_finish";
        if (text.equals("确定")) {
            if (currentPage == VIEW_NAVI_EXIT_DIALOG_SHOW) {
                if (!isExtendApiNull())
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_GUIDE, 1, ""), MXSdkManager.this);
            }
        } else{
            if (currentPage == VIEW_NAVI_EXIT_DIALOG_SHOW) {
                if (!isExtendApiNull())
                    extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_ROUTE_GUIDE, 2, ""), MXSdkManager.this);
            }
        }
        if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
            EventBusUtils.sendExitMessage();
        }
        MultiInterfaceUtils.getInstance(mContext).uploadCmdDefaultData();
    }


    public void makeTeam() {
            if (!isExtendApiNull())
                extendApi.goTeamTrip();
            LogUtils.d("hqtest", "goTeamTrip called..");
        }



    public void exitSecondPage() {
        String text = "返回主页";

        innerEntity = new InstructionEntity();
        innerEntity.text = text;
        innerEntity.nMvwScene = 64;
        innerEntity.id = 1;

        LogUtils.d("xyj", "exitSecondPage  --> " + currentPage);
        switch (currentPage) {
            /**
             * 检索结果页可见(40-45)
             */
            case PAGE_SEARCHRESULT_LIST_FRAGMENT_SHOW: //返回
                    exitFromSearchResult(text);
                /**
                 * 检索页可见 (30-33)
                 */

            case PAGE_SEARCH_FRAGMENT_SHOW: //返回
                    exitFromSearch(text);
                break;

            /**
             * 周边检索页面可见 (140-147)
             */
            case PAGE_NEARBY_FRAGMENT_SHOW: //返回
                    exitFromNearSearch(text);
                break;

            /**
             * 设置家或者公司页面可见 (170-171)
             */
            case PAGE_SUGGESTION_FRAGMENT_SHOW: //返回
                    exitFromSetHome(text);
                break;

            /**
             * 设置家或者公司页面可见 (180-183)
             */
            case PAGE_SET_HOMEORCOMP_FRAGMENT_SHOW: //返回
                    exitFromSetCompany(text);
                break;

            /**
             * 收藏夹（添加途径地）页面可见(300-303)
             */
            case PAGE_FAVORITE_FRAGMENT_ADDPASS_SHOW: //返回
                    exitFromFavorite(text);
                break;

            /**
             * 收藏夹（集结点设置）页面可见(310-313)
             */
            case PAGE_FAVORITE_FRAGMENT_TEAMTRIP_SHOW: //返回
                    exitFromFavorite1(text);
                break;

            default:
                try {
                    extendApi.backToHomePage(new IExtendCallback() {
                        @Override
                        public void success(ExtendBaseModel extendBaseModel) {
                            LogUtils.d("xyj", "exitSecondPage  --> backToHomePage  success");
                        }

                        @Override
                        public void onFail(ExtendErrorModel extendErrorModel) {
                            LogUtils.d("xyj", "exitSecondPage  --> backToHomePage  onFail");
                        }

                        @Override
                        public void onJSONResult(JSONObject jsonObject) {
                            LogUtils.d("xyj", "exitSecondPage  --> backToHomePage  onJSONResult");
                        }
                    });
                } catch (Exception e) {}

        }
    }

    private void exitFromFavorite1(String text) {
        extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 0, ""), this);
        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_settings_team_up_aggregation_settings, text);
    }

    private void exitFromFavorite(String text) {
        extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_FAVORITE_FRAGMENT, 0, ""), this);
        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_means_search, text);
    }

    private void exitFromSetCompany(String text) {
        extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SET_HOMEORCOMP_FRAGMENT, 0, ""), this);
        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_search_favorites_setters_inc, text);
    }

    private void exitFromSetHome(String text) {
        extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SUGGESTION, 0, ""), this);
        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_collector, R.string.object_map_retrieval_results_retrieva_results_list, text);
    }

    private void exitFromNearSearch(String text) {
        extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_NERABY_SEARCH, 0, ""), this);
        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_directory_search, R.string.object_classified_retrieval_peripheral_retrieval, text);
    }

    private void exitFromSearch(String text) {
        extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_FRAGMENT, 0, ""), this);
        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_box_search_with_network_before_input, text);
    }

    private void exitFromSearchResult(String text) {
        extendApi.pageOprea(new PageOpreaData(ExtendConstants.PageId.PAGE_SEARCH_RESULT, 0, ""), this);
        Utils.eventTrack(mContext, R.string.skill_navi, R.string.scene_navi_box_retrieval, R.string.object_map_retrieval_results_list, text);
    }

    private void stopSrAndRestart(){
        Log.d(TAG, "stopSrAndRestart() called");
        boolean floatViewIsHide = FloatViewManager.getInstance(mContext).isHide();
        if (floatViewIsHide&&!TTSController.getInstance(mContext).isTtsPlaying()) {
            SRAgent srAgent=SRAgent.getInstance();
            if (srAgent == null) {
                return;
            }
            srAgent.stopSrOnly();
        }
    }
}
