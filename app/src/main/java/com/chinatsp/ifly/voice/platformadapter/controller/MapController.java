package com.chinatsp.ifly.voice.platformadapter.controller;
import android.car.hardware.constant.MCU;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.AppManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.entity.MXPoiEntity;
import com.chinatsp.ifly.entity.MessageListEvent;
import com.chinatsp.ifly.entity.MultiChoiceEvent;
import com.chinatsp.ifly.entity.PoiEntity;
import com.chinatsp.ifly.entity.SearchPoiEvent;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.MVW_WORDS;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IMapController;
import com.chinatsp.ifly.voice.platformadapter.entity.DataEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.InstructionEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.example.mxextend.IExtendApi;
import com.example.mxextend.TAExtendManager;
import com.example.mxextend.entity.ExtendBaseModel;
import com.example.mxextend.entity.ExtendErrorModel;
import com.example.mxextend.entity.LocationInfo;
import com.example.mxextend.entity.SearchResultModel;
import com.example.mxextend.listener.IExtendCallback;
import com.google.gson.JsonObject;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.TimeoutManager;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.controllerInterface.IController;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.adapter.sr.SrSessionArgu;
import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.util.NetworkUtil;
import com.iflytek.sr.SrSession;
import com.mxnavi.busines.entity.ExtendPoi;
import com.mxnavi.busines.entity.ModifyNaviViaModel;
import com.mxnavi.busines.entity.RequestRouteExModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CARSETTINGC1CONDITION;
import static com.chinatsp.ifly.api.constantApi.TtsConstant.CARSETTINGC2CONDITION;
public class MapController extends BaseController implements IMapController,MVW_WORDS {
    private static String TAG = "MapController";
    private static final String KEY_POI = "KEY_POI";
    private static final String KEY_POI_NAME = "poiName";
    private static final String KEY_POI_NAME_KEY = "poiName_key";
    private static final String KEY_POI_SIZE = "poi_size";
    private static final String KEY_POI_SEARCH_WORD = "KEY_POI_SEARCH_WORD";
    private static final String KEY_POI_ENTITY = "poiEntity";
    private static final String KEY_POI_ENTITY_VIA = "poiEntity_via";
    private Context mContext;
    private IntentEntity intentEntity;
    private static final int MSG_TTS = 1000;
    public static final int MSG_OPEN_NAVI = 1001;/*********??????????????????  private????????? public  ??????????????????*************/
    private static final int MSG_START_NAVI = 1002;
    private static final int MSG_SPECIAL_NAVI = 1004;
    private static final int MSG_SEARCH_RESULT_SELECT = 1005;
    private static final int MSG_ADD_PASS = 1006;
    private static final int MSG_ERROR_POI = 1007;
    public static final int MSG_SHOW_MYLOCATION = 1008;/*********??????????????????  private????????? public  ??????????????????*************/
    private static final int MSG_START_NAVI_WITH_POINT = 1009;// ?????????xx?????????xx

    private MyHandler myHandler = new MyHandler(this);
    private MXSdkManager mxSdkManager;
    private IExtendApi extendApi;
    private int poiType = -1;
    private String poiTopic;
    private static final int POI_TYPE_START_NAVI = 1; //??????????????????
    private static final int POI_TYPE_ADD_PASS = 2; //???????????????
    private static final int POI_TYPE_SPECIAL_NAVI = 3; //?????????/??????

    private final static String POI = "#POI#";
    private final static String POI1 = "#POI1#";
    private final static String POI2 = "#POI2#";
    private final static String NUM = "#NUM#";
    private final static String POITYPE ="#POITYPE#";
    public final static String COMPANYPOIT ="#COMPANYPOI#";/*********??????????????????  private????????? public  ??????????????????*************/
    public final static String  CURRENTPOI= "#CURRENTPOI#";/*********??????????????????  private????????? public  ??????????????????*************/
//    private final static String  KEYWORD= "#KEYWORD#";


    private LocationInfo navinLocInfo;


    public MapController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mxSdkManager = MXSdkManager.getInstance(context);
        TAExtendManager.getInstance().init(mContext);
        this.extendApi = TAExtendManager.getInstance().getApi();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onSearchPoiEvent(SearchPoiEvent searchPoiEvent) {
        String searchKey = searchPoiEvent.searchKey;
        String topic = searchPoiEvent.topic;
        if (!TextUtils.isEmpty(searchKey) && ! TextUtils.isEmpty(topic)) {
            requestPoiData(searchKey, topic);
        } else {
            doExceptonAction(mContext);
        }
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        this.intentEntity = intentEntity;
//        LogUtils.d(TAG, "intentEntity:"+GsonUtil.objectToString(intentEntity));
        if (PlatformConstant.Operation.QUERY.equals(intentEntity.operation)) {

            if (FloatViewManager.getInstance(mContext).isHide()) {
                LogUtils.i(TAG, "FloatViewManager.getInstance(mContext).isHide()  return!!!");
                return;
            }

            if (intentEntity.text.equals("????????????")||intentEntity.text.equals("??????")){
                wantOpenNavi();
                return;
             }else if (intentEntity.text.equals("????????????")){
                mxSdkManager.startNavi();
                return;
             }

            Semantic.SlotsBean.EndLocBean end = intentEntity.semantic.slots.endLoc;
            Semantic.SlotsBean.EndLocBean viaLoc = intentEntity.semantic.slots.viaLoc;

            if (end != null) {
                doQuery(intentEntity);
            } else if (viaLoc != null){
                //  ??????????????????????????? semantic:{"slots":{"viaLoc":{"ori_loc":"???????????????","topic":"others"}}}
                doPassAway(intentEntity);
            } else {
                wantOpenNavi();
            }

        } else if (PlatformConstant.Operation.OPEN.equals(intentEntity.operation)) {
            //????????????
            String[] ttsText = mContext.getResources().getStringArray(R.array.open_navi);
            String conditionId=TtsConstant.NAVIC1CONDITION;
            String defaultTts = ttsText[new Random().nextInt(ttsText.length)];
//            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_open_navi,conditionId,R.string.condition_navi1);
            openNavi(conditionId,defaultTts);
        } else if (PlatformConstant.Operation.LOCATE.equals(intentEntity.operation)) {
                //???????????? semantic:{"slots":{"endLoc":{"ori_loc":"CURRENT_ORI_LOC"},"startLoc":{"ori_loc":"CURRENT_ORI_LOC"}}}
            if (intentEntity.semantic.slots.endLoc != null && "CURRENT_ORI_LOC".equals(intentEntity.semantic.slots.endLoc.ori_loc)) {
                showMyLocation();
                //?????????????????? semantic:{"slots":{"endLoc":{"ori_loc":"?????????","topic":"others"},"startLoc":{"ori_loc":"CURRENT_ORI_LOC"}}}
            } else if(intentEntity.semantic.slots.endLoc != null && intentEntity.semantic.slots.startLoc != null) {
                doQuery(intentEntity);
            } else {
                doExceptonAction(mContext);
            }
        } else if (PlatformConstant.Operation.ALONG_SEARCH.equals(intentEntity.operation)) {
            //????????????
            if (intentEntity.semantic.slots.endLoc != null && intentEntity.semantic.slots.endLoc.ori_loc != null) {
                alongTheWaySearch(intentEntity.semantic.slots.endLoc.ori_loc, intentEntity.semantic.slots.endLoc.topic);
            } else {
                doExceptonAction(mContext);
            }
        } else if (PlatformConstant.Operation.USR_POI_QUERY.equals(intentEntity.operation)) {//??????????????????
            if (intentEntity.semantic.slots.endLoc != null && "USR_DEF".equals(intentEntity.semantic.slots.endLoc.type)) {
                if ("???".equals(intentEntity.semantic.slots.endLoc.ori_loc)) {
                    goHomeOrCompany(0, AppConstant.SOURCE_SR);
                } else if ("??????".equals(intentEntity.semantic.slots.endLoc.ori_loc)) {
                    goHomeOrCompany(1, AppConstant.SOURCE_SR);
                } else {
                    doExceptonAction(mContext);
                }
            } else {
                doExceptonAction(mContext);
            }
        } else if (PlatformConstant.Operation.USR_POI_SET.equals(intentEntity.operation)) {//???????????????????????????

            if (hasResultList(intentEntity)) {
                List<MXPoiEntity> mxPoiEntityList = new ArrayList<MXPoiEntity>();
                if (intentEntity.data != null && intentEntity.data.result!= null) {
                    List<PoiEntity> poiEntityList = GsonUtil.stringToList(intentEntity.data.result.toString(), PoiEntity.class);
                    mxPoiEntityList = MXPoiEntity.wrapIfly(poiEntityList);
                }
                startSelectSpecialNaviData(mxPoiEntityList, null, intentEntity.semantic.slots.endLoc.ori_loc, intentEntity.semantic.slots.endLoc.topic);
            } else if (intentEntity.semantic.slots.endLoc != null && "USR_SET".equals(intentEntity.semantic.slots.endLoc.type)) {
                requestPoiData(intentEntity.semantic.slots.endLoc.ori_loc, intentEntity.semantic.slots.endLoc.topic);
            } else {
                doExceptonAction(mContext);
            }
        } else if (PlatformConstant.Operation.CANCEL.equals(intentEntity.operation)) { //??????
            Utils.exitVoiceAssistant();
            startTTS("?????????");
        } else if (PlatformConstant.Operation.COLLECT.equals(intentEntity.operation)) { //??????
            // {"slots":{"insType":"COLLECT"}} {"slots":{"insType":"COLLECT","tag":"???"}}  {"slots":{"insType":"COLLECT","tag":"??????"}}
            // 0=???????????????  1=???  2=??????
            mxSdkManager.collectCurrentPoi(intentEntity.semantic.slots.tag);
            navinLocInfo=null;
        }
        /***********??????????????????*****************/
//        else if (PlatformConstant.Operation.OPEN_TRAFFIC_INFO.equals(intentEntity.operation)) {
//                mxSdkManager.setMapOperaSwitchRoadCondition(0);
//        } else if (PlatformConstant.Operation.CLOSE_TRAFFIC_INFO.equals(intentEntity.operation)) {
//            mxSdkManager.setMapOperaSwitchRoadCondition(1);
//        }
        /***********??????????????????*****************/
        else if(PlatformConstant.Operation.CLOSE.equals(intentEntity.operation)) { //??????????????????????????????????????????
            Utils.exit(mContext);
        } else if(PlatformConstant.Operation.QUERY_TRAFFIC_INFO.equals(intentEntity.operation)){  //??????????????????????????????????????????????????????????????????
           if(intentEntity.text!=null&&intentEntity.text.contains("????????????")){
               if(intentEntity.text!=null&&intentEntity.text.contains("??????")){
                   String defaultText = mContext.getString(R.string.carsettingC1);
                   openCarController(CarController.EXTRA_OPEN_SETTING_VALUE_VC, CarController.OPEN_VC_TYPE_CARSETTINGS);
                   startTTS(CARSETTINGC1CONDITION, defaultText);
                   Utils.eventTrack(mContext,R.string.skill_car_setting, R.string.scene_car_setting, R.string.object_car_setting1,CARSETTINGC1CONDITION,R.string.condition_default);
               }else if(intentEntity.text!=null&&intentEntity.text.contains("??????")){
                   String defaultText = mContext.getString(R.string.carsettingC2);
                   closeCarController();
                   startTTS(CARSETTINGC2CONDITION, defaultText);
                   Utils.eventTrack(mContext,R.string.skill_car_setting, R.string.scene_car_setting, R.string.object_car_setting2,CARSETTINGC2CONDITION,R.string.condition_default);
               }else {
                   doExceptonAction(mContext);
               }
           }else {
               doExceptonAction(mContext);
           }
        }else if(PlatformConstant.Operation.CONFIRM.equals(intentEntity.operation)){
            if (intentEntity.text.equals("????????????")||intentEntity.text.equals("??????")){
                wantOpenNavi();
                return;
            }else if (intentEntity.text.equals("??????")){
                srSelectItem( 0);
            }else {
                doExceptonAction(mContext);
            }

        }else if(PlatformConstant.Operation.CLOSE_MAP.equals(intentEntity.operation)||PlatformConstant.Operation.CANCEL_MAP.equals(intentEntity.operation)){
             setInnerEntityToMXSdk(-1, ID_EXIT_NAVI,8, InstructionEntity.TYPE_SR_ACTION);
             mxSdkManager.exitNavi(AppConstant.SOURCE_SR);
        }else if(PlatformConstant.Operation.POS_RANK.equals(intentEntity.operation)){
            //??????
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null||intentEntity.semantic.slots.posRank==null||intentEntity.semantic.slots.posRank.offset==null) {
                doExceptonAction(mContext);
                return;
            }
            MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
            try {
                mvwLParamEntity.nMvwId = Integer.parseInt(intentEntity.semantic.slots.posRank.offset) - 1;
                mvwLParamEntity.nMvwId = mvwLParamEntity.nMvwId < 0 ? 0 : mvwLParamEntity.nMvwId;
                mvwLParamEntity.nMvwId = mvwLParamEntity.nMvwId > 4 ? 4 : mvwLParamEntity.nMvwId;
            } catch (Exception e) {}
            mvwLParamEntity.nMvwScene = TspSceneAdapter.TSP_SCENE_SELECT;
            selectItem(mvwLParamEntity);
        }else if(PlatformConstant.Operation.PAGE_RANK.equals(intentEntity.operation)){
            //??????
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null||intentEntity.semantic.slots.pageRank==null||intentEntity.semantic.slots.pageRank.offset==null) {
                doExceptonAction(mContext);
                return;
            }
            srSelectItem(slotToIndex(intentEntity.semantic.slots.pageRank));
        }else if(PlatformConstant.Operation.RESET_POI.equals(intentEntity.operation)){
            Message msg = myHandler.obtainMessage(MSG_ERROR_POI, mContext.getString(R.string.poi_find_error));
            myHandler.sendMessageDelayed(msg, 1000);
        }else if(PlatformConstant.Operation.ZOOM_IN.equals(intentEntity.operation)){
            //????????????
            setInnerEntityToMXSdk(-1,ID_ZOOMIN,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.enlargeMap();
        }else if(PlatformConstant.Operation.ZOOM_OUT.equals(intentEntity.operation)){
            //????????????
            setInnerEntityToMXSdk(-1,ID_ZOOMOUT,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.reduceMap();
        }else if(PlatformConstant.Operation.OPEN_TRAFFIC_INFO.equals(intentEntity.operation)){
            //????????????
            setInnerEntityToMXSdk(-1,ID_TRAFFIC_ON,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.openRoad();
        }else if(PlatformConstant.Operation.CLOSE_TRAFFIC_INFO.equals(intentEntity.operation)){
            //????????????
            setInnerEntityToMXSdk(-1,ID_TRAFFIC_OFF,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.closeRoad();
        }else if(PlatformConstant.Operation.DISPLAY_MODE_DAY.equals(intentEntity.operation)){
            //????????????
            setInnerEntityToMXSdk(-1,ID_DAY_MODE,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.setShowMode(MXSdkManager.SHOW_DAY, null);
//            doExceptonAction(mContext);
        }else if(PlatformConstant.Operation.DISPLAY_MODE_NIGHT.equals(intentEntity.operation)){
            //????????????
            setInnerEntityToMXSdk(-1,ID_NIGHT_MODE,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.setShowMode(MXSdkManager.SHOW_NIGHT, null);
//            doExceptonAction(mContext);
        }else if(PlatformConstant.Operation.ROUTE_PLAN.equals(intentEntity.operation)){
            //????????????
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null || TextUtils.isEmpty(intentEntity.semantic.slots.routeCondition)) {
                doExceptonAction(mContext);
                return;
            }
            if (intentEntity.semantic.slots.routeCondition.startsWith("HIGH_FIRST")){
                //????????????
                setInnerEntityToMXSdk(-1,ID_HEAD_FIRST,8, InstructionEntity.TYPE_SR_ACTION);
                mxSdkManager.highSpeed();
            }else if (intentEntity.semantic.slots.routeCondition.startsWith("NOT_HIGH_FIRST")){
                //????????????
                setInnerEntityToMXSdk(-1,ID_NO_HEAD,8, InstructionEntity.TYPE_SR_ACTION);
                mxSdkManager.noSpeed();
            }else if (intentEntity.semantic.slots.routeCondition.startsWith("FREE")){
                //????????????
                setInnerEntityToMXSdk(-1,ID_NO_CHARGE,8, InstructionEntity.TYPE_SR_ACTION);
                mxSdkManager.lessCharge();
            }else if (intentEntity.semantic.slots.routeCondition.startsWith("AVOID_ROUND")){
                //????????????
                setInnerEntityToMXSdk(-1,ID_FEW_BLOCK,8, InstructionEntity.TYPE_SR_ACTION);
                mxSdkManager.avoidingCongestion();
            }else {
                doExceptonAction(mContext);
            }
        }else if(PlatformConstant.Operation.NAVI_INFO.equals(intentEntity.operation)){
            if(intentEntity.semantic==null||intentEntity.semantic.slots==null) {
                doExceptonAction(mContext);
                return;
            }
            if ("DISTANCE_REMAIN".equals(intentEntity.semantic.slots.naviInfo)) {
                //????????????
                setInnerEntityToMXSdk(-1,ID_HOW_FAR,8, InstructionEntity.TYPE_SR_ACTION);
                mxSdkManager.howFar();
            }else if ("TIME_REMAIN".equals(intentEntity.semantic.slots.naviInfo)){
                //????????????
                setInnerEntityToMXSdk(-1,ID_HOW_LONG,8, InstructionEntity.TYPE_SR_ACTION);
                mxSdkManager.howFar();
            }
            else if ("VIA_INFO".equals(intentEntity.semantic.slots.naviInfo)){
                //????????????
                setInnerEntityToMXSdk(-1,ID_LOOK_ALL,8, InstructionEntity.TYPE_SR_ACTION);
                mxSdkManager.viaInfo();
            }else {
                doExceptonAction(mContext);
            }
        }else if(PlatformConstant.Operation.VIEW_TRANS_2D.equals(intentEntity.operation)
        || PlatformConstant.Operation.VIEW_TRANS_HEAD_UP.equals(intentEntity.operation)){
                  //???????????????2D??????
                if (TextUtils.isEmpty(intentEntity.text)){
                    doExceptonAction(mContext);
                    return;
                }
                if (intentEntity.text.equals(ID_HEAD_UP)){
                    setInnerEntityToMXSdk(-1,ID_HEAD_UP,8, InstructionEntity.TYPE_SR_ACTION);
                }else {
                    setInnerEntityToMXSdk(-1,ID_2D_MODE,8, InstructionEntity.TYPE_SR_ACTION);
                }
                mxSdkManager.headUp();
        }else if(PlatformConstant.Operation.VIEW_TRANS_NORTH_UP.equals(intentEntity.operation)){
            //????????????
            setInnerEntityToMXSdk(-1, ID_NORTH_UP, 8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.norihUp();
        }else if(PlatformConstant.Operation.VIEW_TRANS_3D.equals(intentEntity.operation)){
            //3D??????
            setInnerEntityToMXSdk(-1,ID_3D_MODE,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.D3Mode();
        } else if(PlatformConstant.Operation.VIEW_HISTOGRAM_MAP.equals(intentEntity.operation)){
            //??????????????????/?????????/?????????
            setInnerEntityToMXSdk(-1,ID_HAWKEYE_MODE_1,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.setHawkeyeModel(1);
        } else if(PlatformConstant.Operation.VIEW_EAGLE_EYE_MAP.equals(intentEntity.operation)){
            //??????????????????/?????????
            setInnerEntityToMXSdk(-1,ID_HAWKEYE_MODE_0,8, InstructionEntity.TYPE_SR_ACTION);
            mxSdkManager.setHawkeyeModel(0);
        } else if(PlatformConstant.Operation.PASS_AWAY.equals(intentEntity.operation)){
            try {
                doPassAway(intentEntity);
            } catch (Exception e) {
                doExceptonAction(mContext);
            }

        } else {
            doExceptonAction(mContext);
        }
    }

    private void doPassAway(IntentEntity intentEntity) {
        Semantic.SlotsBean.EndLocBean end = intentEntity.semantic.slots.endLoc;
        Semantic.SlotsBean.EndLocBean viaLoc = intentEntity.semantic.slots.viaLoc;

        // semantic:{"slots":{"viaLoc":{"ori_loc":"??????","topic":"others"},"viaLocNext":{"city":"??????","ori_loc":"??????","ori_loc_fullname":"??????","topic":"others"}}}

        if (NetworkUtil.isNetworkAvailable(mContext)) {
            if (hasResultList(intentEntity)) { //???????????????
                LogUtils.d(TAG, "??????????????????");
                // ??????????????? semantic:{"slots":{"viaLoc":{"ori_loc":"?????????","topic":"others"}}}
                if (viaLoc != null) {
                    Semantic.SlotsBean.EndLocBean viaLocNext = intentEntity.semantic.slots.viaLocNext;

                    if (mxSdkManager.isNaving()) {

                        if (viaLocNext != null) {
                            filterEndPoi(intentEntity.data);
                            addPassPoints(intentEntity);
                        } else {
                            xueFeiToMeixinRoute(intentEntity, viaLoc.ori_loc);
                        }
                    } else {

                        if (viaLocNext != null) {
                            filterEndPoi(intentEntity.data);
                            intentEntity.data.result = intentEntity.data.resultVia;
                            intentEntity.data.resultVia = intentEntity.data.resultViaNext;
                            intentEntity.data.endPoi = intentEntity.data.viaPoi;
                            intentEntity.data.viaPoi = intentEntity.data.viaPoiNext;
                            intentEntity.semantic.slots.endLoc = intentEntity.semantic.slots.viaLoc;
                            intentEntity.semantic.slots.viaLoc = intentEntity.semantic.slots.viaLocNext;
                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                                    TtsConstant.NAVIC6_16CONDITION,R.string.condition_navi6_16, mContext.getString(R.string.condition_navi18));
                            startNaviWithPoint(intentEntity);
                        } else {
                            // semantic:{"slots":{"viaLoc":{"ori_loc":"????????????","topic":"others"}}}
                            intentEntity.semantic.slots.endLoc = new Semantic.SlotsBean.EndLocBean();
                            intentEntity.semantic.slots.endLoc.ori_loc = viaLoc.ori_loc;
                            intentEntity.semantic.slots.endLoc.topic = viaLoc.topic;
                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point,
                                    TtsConstant.NAVIC6_1CONDITION,R.string.condition_navi6_1, mContext.getString(R.string.condition_navi18));
                            startSelect(viaLoc.ori_loc);
                        }
                    }
                } else if (end != null) {
                    startSelect(end.ori_loc);
                } else {
                    doExceptonAction(mContext);
                    LogUtils.e(TAG, "doPassAway doExceptonAction");
                }
            }else {
                //???????????????????????????????????????
                doPassFromMeixin(end, viaLoc);
            }
        } else {
            doPassFromMeixin(end, viaLoc);
        }

    }

    public static void justEventTrack(Context context,int appName, int scene, int object, String conditionId, int condition,String defaultTts) {

    }

    private void doPassFromMeixin(Semantic.SlotsBean.EndLocBean end, Semantic.SlotsBean.EndLocBean viaLoc) {
        LogUtils.d(TAG, "??????????????????");
        Semantic.SlotsBean.EndLocBean viaLocNext = intentEntity.semantic.slots.viaLocNext;

        if (viaLoc != null) {
            if (mxSdkManager.isNaving()) {
                if (viaLocNext != null) {
                    searchPoiFromMeixin(intentEntity.semantic.slots.viaLoc.ori_loc);
                } else {
//                  alongTheWaySearch(viaLoc.ori_loc, viaLoc.topic);
                    searchPoiFromMeixin(viaLoc.ori_loc);
                }
            } else {
                if (viaLocNext != null) {
                    intentEntity.semantic.slots.endLoc = intentEntity.semantic.slots.viaLoc;
                    intentEntity.semantic.slots.viaLoc = intentEntity.semantic.slots.viaLocNext;
                    searchPoiFromMeixin(intentEntity.semantic.slots.endLoc.ori_loc);
                } else {
                    getFromMeixin(viaLoc);
                }
            }
        } else if (end != null) {
            getFromMeixin(end);
        } else {
            doExceptonAction(mContext);
        }
    }

    /**
     * ?????????????????????
     * @param intentEntity
     */
    private void addPassPoints(IntentEntity intentEntity) {
        LogUtils.i(TAG, "addPassPoints  viaPoi: " + intentEntity.data.viaPoi.size() + "  viaPoiNext: " + intentEntity.data.viaPoiNext.size());
        int passPointNum = extendApi.getPassPointNum();
        if (passPointNum >= MAX_POINT) {
            pointMax();
        } else if (passPointNum == 1) {// ???????????????????????????
//            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
//                    TtsConstant.NAVIC6_18CONDITION,R.string.condition_navi18, mContext.getString(R.string.condition_navi18));
            intentEntity.data.result = intentEntity.data.resultVia;
            // ??????????????????
            intentEntity.semantic.slots.viaLocNext = null;
            intentEntity.data.resultViaNext = null;
            xueFeiToMeixinRoute(intentEntity, intentEntity.semantic.slots.viaLoc.ori_loc);
        } else {// ?????????????????????
            List<PoiEntity> viaPoi = intentEntity.data.viaPoi;
            List<PoiEntity> viaNextPoi = intentEntity.data.viaPoiNext;
            if (viaPoi.size() > 1) {
                if (viaNextPoi.size() > 1) {
                    // ????????????????????????????????????
                    moreOneVia(viaPoi.get(0).getName(), viaPoi.size() +"", GsonUtil.objectToString(intentEntity.semantic)
                            , intentEntity.semantic.slots.viaLoc.ori_loc, GsonUtil.objectToString(intentEntity.data.resultVia),TtsConstant.NAVIC6_17CONDITION,
                            mContext.getString(R.string.add_point_first_sure),POI1);
                } else if (viaNextPoi.size() == 1) {
                    // ???????????????????????????????????????????????????????????????
                    moreOneVia(viaPoi.get(0).getName(), viaPoi.size() +"", GsonUtil.objectToString(intentEntity.semantic)
                            , intentEntity.semantic.slots.viaLoc.ori_loc, GsonUtil.objectToString(intentEntity.data.resultVia),TtsConstant.NAVIC6_17CONDITION,
                            mContext.getString(R.string.add_point_first_sure),POI1);
                } else {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    intentEntity.semantic.slots.viaLocNext = null;
                    moreOneVia(viaPoi.get(0).getName(), viaPoi.size() +"", GsonUtil.objectToString(intentEntity.semantic)
                            , intentEntity.semantic.slots.viaLoc.ori_loc, GsonUtil.objectToString(intentEntity.data.resultVia),TtsConstant.NAVIC6_17CONDITION,
                            mContext.getString(R.string.add_point_first_sure),POI1);
                }
            } else if (viaPoi.size() == 1) {
                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                        TtsConstant.NAVIC6_18CONDITION,R.string.condition_navi6_18, mContext.getString(R.string.condition_navi18));
                if (viaNextPoi.size() > 1) {
                    // ?????????????????????????????????????????????????????????????????????????????????
                    intentEntity.data.viaPoiResult = viaPoi.get(0);
                    moreOneVia(viaNextPoi.get(0).getName(), viaNextPoi.size() +"", GsonUtil.objectToString(intentEntity.semantic)
                            , intentEntity.semantic.slots.viaLoc.ori_loc, GsonUtil.objectToString(intentEntity.data.resultViaNext),TtsConstant.NAVIC6_17CONDITION,
                            mContext.getString(R.string.add_point_first_sure),POI1);
                } else if (viaNextPoi.size() == 1) {
                    // ?????????????????????????????????????????????
                    addTwoPassPoint(viaPoi.get(0), viaNextPoi.get(0));
                } else {
                    // ???????????????????????????????????????????????????
                    PoiEntity addPoi = viaPoi.get(0);
                    String ttsId = TtsConstant.NAVIC6_23CONDITION;
                    Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            String ttsText = tts;
                            if (TextUtils.isEmpty(tts)) {
                                ttsText = mContext.getString(R.string.add_point_first);
                            }
                            ttsText = Utils.replaceTts(ttsText, POI, addPoi.getName());
                            Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                            myHandler.sendMessage(msg);

                            requestAddPass(addPoi);

                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                                    ttsId,R.string.condition_navi6_23, ttsText);
                        }
                    });
                }
            } else {
                if (viaNextPoi.size() > 1) {
                    // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    intentEntity.semantic.slots.viaLoc = intentEntity.semantic.slots.viaLocNext;
                    intentEntity.semantic.slots.viaLocNext = null;
                    moreOneVia(viaNextPoi.get(0).getName(), viaNextPoi.size() +"", GsonUtil.objectToString(intentEntity.semantic)
                    , intentEntity.semantic.slots.viaLoc.ori_loc, GsonUtil.objectToString(intentEntity.data.resultViaNext),TtsConstant.NAVIC6_19CONDITION,
                            mContext.getString(R.string.add_point_second),POI2);
                } else if (viaNextPoi.size() == 1) {// ??????????????????????????????????????????????????????????????????
                    PoiEntity addPoi = viaNextPoi.get(0);
                    String ttsId = TtsConstant.NAVIC6_20CONDITION;
                    Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            String ttsText = tts;
                            if (TextUtils.isEmpty(tts)) {
                                ttsText = mContext.getString(R.string.add_point);
                            }
                            ttsText = Utils.replaceTts(ttsText, POI2, addPoi.getName());
                            Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                            myHandler.sendMessage(msg);

                            requestAddPass(addPoi);

                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                                    ttsId,R.string.condition_navi6_20, ttsText);
                        }
                    });
                } else {// ????????????
                    String ttsId = TtsConstant.NAVIC6_21CONDITION;
                    Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            String ttsText = tts;
                            if (TextUtils.isEmpty(tts)) {
                                ttsText = mContext.getString(R.string.no_poi_search_results);
                            }
                            LogUtils.d(TAG, "ttsText:" + ttsText);
                            Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                            myHandler.sendMessageDelayed(msg, 100);

                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                                    ttsId,R.string.condition_navi6_21, ttsText);
                        }
                    });
                }
            }
        }
    }

    private void doQuery(IntentEntity intentEntity) {
        Semantic.SlotsBean.EndLocBean end = intentEntity.semantic.slots.endLoc;
        Semantic.SlotsBean.EndLocBean via = intentEntity.semantic.slots.viaLoc;
        if (NetworkUtil.isNetworkAvailable(mContext)) {
            if (hasResultList(intentEntity)) { //???????????????
                LogUtils.d(TAG, "??????????????????");
                if (end != null) {
                    if (PlatformConstant.Topic.ROUTEP.equals(end.topic)) {  //???????????????????????????????????????
                        String searchKey = end.ori_loc;
                        xueFeiToMeixinRoute(intentEntity, searchKey);
                    } else if (PlatformConstant.Topic.HOME.equals(end.topic)//??????????????????????????????????????????
                                    || PlatformConstant.Topic.COMPANY.equals(end.topic)) {
                        String searchKey = end.ori_loc;
                        List<PoiEntity> poiEntityList = GsonUtil.stringToList(intentEntity.data.result.toString(), PoiEntity.class);
                        List<MXPoiEntity> mxPoiEntityList = MXPoiEntity.wrapIfly(poiEntityList);
                        startSelectSpecialNaviData(mxPoiEntityList, GsonUtil.objectToString(intentEntity.semantic), searchKey, end.topic);
                    } else if (via != null){
                        // ?????????xxx ??????xxx ????????????????????????????????????????????????
                        // semantic-{"slots":{"endLoc":{"ori_loc":"?????????","topic":"others"},"startLoc":{"ori_loc":"CURRENT_ORI_LOC"},"
                        // viaLoc":{"ori_loc":"?????????","topic":"others"}}}
                        filterEndPoi(intentEntity.data);
                        startNaviWithPoint(intentEntity);
                    } else {// ?????????xxxx

                        startSelect(end.ori_loc);
                    }
                } else {
                    doExceptonAction(mContext);
                }
            } else {
                //???????????????????????????????????????
                doQueryFromMeixin(end);
            }
        } else {
            doQueryFromMeixin(end);
        }
    }

    private void doQueryFromMeixin(Semantic.SlotsBean.EndLocBean end) {
        LogUtils.d(TAG, "??????????????????");
        Semantic.SlotsBean.EndLocBean via = intentEntity.semantic.slots.viaLoc;
        if (end != null && via != null) {
            // ?????????xxx ??????xxx
            searchPoiFromMeixin(end.ori_loc);
        } else {
            getFromMeixin(end);
        }
    }


    public void searchPoiFromMeixin(final String searchKey) {
        try {
            //??????????????????????????????????????????
            LogUtils.d(TAG, "searchPoiFromMeixin  searchKey: " + searchKey);
            extendApi.requestPoiData(searchKey, new IExtendCallback<SearchResultModel>() {
                @Override
                public void success(SearchResultModel searchResultModel) {
                    ArrayList<LocationInfo> resultList = searchResultModel.getResultList();
                    LogUtils.d(TAG, "searchPoiFromMeixin success:  resultList: " + resultList.size());

//                    for (LocationInfo info : resultList) {
//                        LogUtils.d(TAG, info.toString());
//                    }
                    //???????????????wrap??????????????????
                    List<JsonObject> jsonResult = getJsonResult(resultList);
//                    startSelectSpecialNaviData(mxPoiEntityList, null, searchKey, topic);

                    Semantic.SlotsBean.EndLocBean end = intentEntity.semantic.slots.endLoc;
                    Semantic.SlotsBean.EndLocBean via = intentEntity.semantic.slots.viaLoc;
                    Semantic.SlotsBean.EndLocBean viaNext = intentEntity.semantic.slots.viaLocNext;

                    if (end != null && via != null) {
                        // ?????????xxx ??????xxx
                        if (intentEntity.data.result == null) {
                            // ????????????
                            intentEntity.data.result = jsonResult;
                            try {
                                intentEntity.data.endPoi = GsonUtil.stringToList(jsonResult.toString(), PoiEntity.class);
                            } catch (Exception e) {}
                            searchPoiFromMeixin(via.ori_loc);
                        } else {
                            // ???????????????
                            intentEntity.data.resultVia = jsonResult;
                            try {
                                intentEntity.data.viaPoi = GsonUtil.stringToList(jsonResult.toString(), PoiEntity.class);
                            } catch (Exception e) {}
                            startNaviWithPoint(intentEntity);
                        }
                    } else if (via != null && viaNext != null) {
                        // ??????xxx???xxx????????????
                        if (intentEntity.data.resultVia == null) {
                            // ???????????????
                            intentEntity.data.resultVia = jsonResult;
                            try {
                                intentEntity.data.viaPoi = GsonUtil.stringToList(jsonResult.toString(), PoiEntity.class);
                            } catch (Exception e) {}
                            searchPoiFromMeixin(intentEntity.semantic.slots.viaLocNext.ori_loc);
                        } else {
                            // ???????????????
                            intentEntity.data.resultViaNext = jsonResult;
                            try {
                                intentEntity.data.viaPoiNext = GsonUtil.stringToList(jsonResult.toString(), PoiEntity.class);
                            } catch (Exception e) {}
                            addPassPoints(intentEntity);
                        }
                    } else {
                        List<MXPoiEntity> mxPoiEntityList = MXPoiEntity.wrapMx(resultList);
                        startSelectAlongWaySearchData(mxPoiEntityList,  GsonUtil.objectToString(intentEntity.semantic), searchKey);
                    }
                }

                private List<JsonObject> getJsonResult(ArrayList<LocationInfo> resultList) {
                    List<JsonObject> result = new ArrayList<>();
                    for (LocationInfo locationInfo: resultList) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("name", locationInfo.getName());
                        jsonObject.addProperty("address", locationInfo.getAddress());
                        jsonObject.addProperty("latitude", locationInfo.getLatitude());
                        jsonObject.addProperty("longitude", locationInfo.getLongitude());
                        jsonObject.addProperty("cityName", locationInfo.getCityName());
                        jsonObject.addProperty("phone", locationInfo.getPhone());
                        jsonObject.addProperty("distance", locationInfo.getDistance());
                        jsonObject.addProperty("entrLocation", locationInfo.getNaviLongitude() + "," + locationInfo.getNaviLatitude());
                        result.add(jsonObject);
                    }
                    return result;
                }

                @Override
                public void onFail(ExtendErrorModel extendErrorModel) {
                    int errorCode = extendErrorModel.getErrorCode();
                    LogUtils.d(TAG, "searchPoiFromMeixin errorCode:"+errorCode+"  ???????????????"+NetworkUtil.isNetworkAvailable(mContext));

                    if (errorCode == -10050) {
                        noOne();
                        return;
                    }

                    if (!NetworkUtil.isNetworkAvailable(mContext)) {
                        String mainMsg = mContext.getString(R.string.no_network_tip);
                        startTTS(TtsConstant.MAINC19CONDITION,mainMsg);
                    } else {

                        if (errorCode == 10009 || errorCode == 10019) {
                            startTTS("",mContext.getString(R.string.network_weka_tip));
                        } else {
                            noOne();
                        }

                    }
                }

                @Override
                public void onJSONResult(JSONObject jsonObject) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            doExceptonAction(mContext);
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMultiChoiceEvent(MultiChoiceEvent event) {

        LogUtils.i(TAG, "onMultiChoiceEvent: " + event);
        try {
            switch (event.mEventType) {
                case MultiChoiceEvent.EVENT_END:// ??????????????????????????????????????????
                    intentEntity.data.endPoiResult = event.mEntity;
                    List<JsonObject> viaList = intentEntity.data.resultVia;
                    List<PoiEntity> viaPoi = intentEntity.data.viaPoi;
                    if (viaList != null) {
                        if (viaList.size() > 1) {
                            moreOneVia(viaPoi.get(0).getName(), viaList.size() + "", GsonUtil.objectToString(intentEntity.semantic),
                                    intentEntity.semantic.slots.viaLoc.ori_loc, GsonUtil.objectToString(viaList), TtsConstant.NAVIC6_11CONDITION
                            ,mContext.getString(R.string.make_sure_add_naame_as_point), POI1);
                        } else if (viaList.size() == 1) {
                            toNavi(event.mEntity, viaPoi.get(0));
                        } else {
                            toNavi(event);
                        }
                    } else {
                        toNavi(event);
                    }
                    break;
                case MultiChoiceEvent.EVENT_VIA: // ?????????????????????????????????
                    toNavi(intentEntity.data.endPoiResult, event.mEntity);
                    break;
                case MultiChoiceEvent.EVENT_ADD_POINTS:
                    if (intentEntity.data.viaPoiResult != null) {
                        // ????????????????????????
                        addTwoPassPoint(intentEntity.data.viaPoiResult, event.mEntity);
                    } else {
                        // ?????????????????????
                        intentEntity.data.viaPoiResult = event.mEntity;

                        if (intentEntity.data.resultViaNext.size() > 1) {
                            moreOneVia(intentEntity.data.viaPoiNext.get(0).getName(), intentEntity.data.viaPoiNext.size() +"", GsonUtil.objectToString(intentEntity.semantic)
                                    , intentEntity.semantic.slots.viaLocNext.ori_loc, GsonUtil.objectToString(intentEntity.data.resultViaNext),TtsConstant.NAVIC6_17CONDITION,
                                    mContext.getString(R.string.add_point_first_sure),POI1);
                        } else if (intentEntity.data.resultViaNext.size() == 1) {
                            addTwoPassPoint(intentEntity.data.viaPoiResult, intentEntity.data.viaPoiNext.get(0));
                        } else {
                            requestAddPass(event.mEntity);
                        }


                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            doExceptonAction(mContext);
        }

    }

    private void addTwoPassPoint(BaseEntity first, BaseEntity second) {

        LogUtils.i(TAG, "addTwoPassPoint: " + first + "   " +second);
        ExtendPoi poiFirst = getExendPoiFromPoi(first);
        ExtendPoi poiSecond = getExendPoiFromPoi(second);

        RequestRouteExModel requeset = new RequestRouteExModel();
        ArrayList<ExtendPoi> list = new ArrayList<ExtendPoi>();
        list.add(poiFirst);
        list.add(poiSecond);
        requeset.setMidExtendPois(list);
        extendApi.requestRouteEx(requeset, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "addTwoPassPoint: success");

                // ??????
                String ttsId = TtsConstant.NAVIC6_22CONDITION;
                Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        String ttsText = tts;
                        if (TextUtils.isEmpty(tts)) {
                            ttsText = mContext.getString(R.string.add_point_both);
                        }
                        ttsText = Utils.replaceTts(ttsText, POI, first.getName());
                        ttsText = Utils.replaceTts(ttsText, POI2, second.getName());
                        Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                        myHandler.sendMessage(msg);

                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                                ttsId,R.string.condition_navi6_22, ttsText);
                    }
                });
               }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "addTwoPassPoint: onFail");
                // ??????
                String ttsId1 = TtsConstant.NAVIC6_24CONDITION;
                Utils.getMessageWithoutTtsSpeak(mContext, ttsId1, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        String ttsText = tts;
                        if (TextUtils.isEmpty(tts)) {
                            ttsText = mContext.getString(R.string.add_point_both_fail);
                        }
                        Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                        myHandler.sendMessage(msg);

                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                                ttsId1,R.string.condition_navi6_24, ttsText);
                    }
                });
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
                LogUtils.d(TAG, "addTwoPassPoint: onJSONResult");
            }
        });

    }

    private void toNavi(MultiChoiceEvent event) {
        AppManager.getAppManager().finishListActivity();
        String conditionId = TtsConstant.NAVIC18CONDITION;
        String name = getName(event.mEntity);
        String defaultTts = Utils.replaceTts(mContext.getString(R.string.navi_to_soon), POI, name);
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }else {
                    ttsText = Utils.replaceTts(tts, POI, name);
                }
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });

                naviToPoi(event.mEntity);
                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi18, ttsText);
            }
        });
    }

    private String getName(BaseEntity poiEntity) {

        if (poiEntity instanceof PoiEntity) {
            return ((PoiEntity) poiEntity).getName();
        } else if (poiEntity instanceof MXPoiEntity) {
            return ((MXPoiEntity) poiEntity).getName();
        }
        return "";
    }

    private void toNavi(BaseEntity endPoiResult, BaseEntity viaPoiResult) {
        AppManager.getAppManager().finishListActivity();
        String ttsId = TtsConstant.NAVIC6_12CONDITION;
        Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String ttsText) {
                if (TextUtils.isEmpty(ttsText)) {
                    ttsText = mContext.getString(R.string.one_poi_search_result1);
                }
                ttsText = Utils.replaceTts(ttsText, POI, getName(endPoiResult));
                ttsText = Utils.replaceTts(ttsText, POI1, getName(viaPoiResult));
                LogUtils.d(TAG, "ttsText:" + ttsText);
                Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                myHandler.sendMessageDelayed(msg, 100);

                naviToPoiWithPoint(endPoiResult, viaPoiResult);
                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_navi,ttsId,R.string.condition_navi6_12, ttsText);
            }
        });
    }

    /**
     * // ?????????xxx ??????xxx
     * @param intentEntity
     */
    private void startNaviWithPoint(IntentEntity intentEntity) {

        Semantic semantic = intentEntity.semantic;
        List<JsonObject> endLoc = intentEntity.data.result;
        List<JsonObject> viaLoc = intentEntity.data.resultVia;
        List<PoiEntity> endPoi = intentEntity.data.endPoi;
        List<PoiEntity> viaPoi = intentEntity.data.viaPoi;

        String endStr = "";
        String viaStr = "";
        String semanticStr = "";
        try {
            endStr = GsonUtil.objectToString(endLoc);
        } catch (Exception e) {}
        try {
            viaStr = GsonUtil.objectToString(viaLoc);
        } catch (Exception e) {}
        try {
            semanticStr = GsonUtil.objectToString(semantic);
        } catch (Exception e) {}

        LogUtils.d(TAG, "startNaviWithPoint  endPoi:" + endPoi.size() + "  viaPoi: " + viaPoi.size() + "  slots: " + GsonUtil.objectToString(semantic));

        if (endLoc.size() > 1) {
            String desName = endPoi.get(0).getName();
            String oldStr = POI1;
            String searchKey = semantic.slots.endLoc.ori_loc;
            int size = endPoi.size();
            if (viaLoc.size() > 1) {
                // * ????????????????????????????????????	naviC6_8	???????????????#POI1#?????????????????????	???????????????#POI1#???????????????????????????????????????	  ?????????????????????#POI#??????#POI1#

                showPoiActivity(endStr, semanticStr);
                String condition = TtsConstant.NAVIC6_8CONDITION;
                String text = Utils.replaceTts(mContext.getString(R.string.make_sure_navigate_to_desName), oldStr, ""+desName);
                moreOneTTS(text, desName, oldStr, searchKey, condition, size);
            } else if (viaLoc.size() == 1) {
                // ?????????????????????????????????????????????  ?????????????????????#POI#??????#POI1#
                showPoiActivity(endStr, semanticStr);
                String condition = TtsConstant.NAVIC6_8CONDITION;
                String text = Utils.replaceTts(mContext.getString(R.string.make_sure_navigate_to_desName), oldStr, ""+desName);
                moreOneTTS(text, desName, oldStr, searchKey, condition, size);
            } else {
                // ??????????????????????????????   ???????????????????????????????????????
                showPoiActivity(endStr, semanticStr);
                moreOne(desName, searchKey, size);
            }
        } else if (endLoc.size() == 1) {

            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_navi,
                    TtsConstant.NAVIC6_9CONDITION,R.string.condition_navi6_9, mContext.getString(R.string.condition_navi18));

            if (viaLoc.size() > 1) {
                // * ???????????????????????????????????????	naviC6_9			???????????????????????????
                // * ??????????????????????????????	naviC6_11	???????????????#POI1#???????????????????????????????????????	  ?????????????????????#POI#??????#POI1#
                intentEntity.data.endPoiResult = endPoi.get(0);
                moreOneVia(viaPoi.get(0).getName(), viaLoc.size() + "", GsonUtil.objectToString(intentEntity.semantic),
                        intentEntity.semantic.slots.viaLoc.ori_loc, viaStr, TtsConstant.NAVIC6_11CONDITION,
                        mContext.getString(R.string.make_sure_add_naame_as_point), POI1);
            } else if (viaLoc.size() == 1) {
                // * ?????????????????????????????????	naviC6_12	?????????????????????#POI#??????#POI1#	#POI#????????????????????????POI???#POI1#???????????????????????????POI??????
                toNavi(endPoi.get(0), viaPoi.get(0));
            } else {
                // * ???????????????????????????	naviC6_13	????????????????????????????????????????????????		???????????????????????????????????????
                naviNoVia(endPoi);
            }
        } else {
            //    * ?????????????????????????????????	naviC6_10	?????????????????????????????????????????????		??????????????????
            naviViaNone();
        }
    }

    private void naviNoVia(List<PoiEntity> endPoi) {
        String conditionId = TtsConstant.NAVIC6_13CONDITION;
        String finalMainMessage = mContext.getString(R.string.no_poi_search_result1);
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = finalMainMessage;
                }
                Message msg = myHandler.obtainMessage(MSG_START_NAVI, ttsText);
                Bundle data = new Bundle();
                data.putSerializable(KEY_POI_ENTITY, endPoi.get(0));
                msg.setData(data);
                myHandler.sendMessageDelayed(msg, 100);
                Utils.eventTrack(mContext,R.string.skill_navi, R.string.scene_launch_navi, R.string.object_add_point_navi, conditionId, R.string.condition_navi6_13, ttsText);
            }
        });
    }

//    private void naviViaOnlyOne(List<PoiEntity> endPoi, List<PoiEntity> viaPoi) {
//        String poiKey = POI;
//        String poiValue = endPoi.get(0).getName();
//        String mainMessage = Utils.replaceTts(mContext.getString(R.string.one_poi_search_result1), poiKey, poiValue);
//        String poi1Key = POI1;
//        String poi1Value = viaPoi.get(0).getName();
//        mainMessage =Utils.replaceTts(mainMessage, poi1Key, poi1Value);
//        String conditionId = TtsConstant.NAVIC6_12CONDITION;
//        String finalMainMessage = mainMessage;
//        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
//            @Override
//            public void onConfirm(String tts) {
//                String ttsText = tts;
//                if (TextUtils.isEmpty(tts)) {
//                    ttsText = finalMainMessage;
//                } else {
//                    ttsText = Utils.replaceTts(ttsText, poiKey,poiValue);
//                    ttsText = Utils.replaceTts(ttsText, poi1Key,poi1Value);
//                }
//                Message msg = myHandler.obtainMessage(MSG_START_NAVI_WITH_POINT, ttsText);
//                Bundle data = new Bundle();
//                data.putSerializable(KEY_POI_ENTITY, endPoi.get(0));
//                data.putSerializable(KEY_POI_ENTITY_VIA, viaPoi.get(0));
//                msg.setData(data);
//                myHandler.sendMessageDelayed(msg, 1000);
//                Utils.eventTrack(mContext,R.string.skill_navi, R.string.scene_launch_navi, R.string.object_add_point_navi, conditionId, R.string.condition_navi5, ttsText);
//            }
//        });
//    }

    private void naviViaNone() {
        String ttsId = TtsConstant.NAVIC6_10CONDITION;
        Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = mContext.getString(R.string.no_poi_search_result);
                }
                LogUtils.d(TAG, "ttsText:" + ttsText);
                Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                myHandler.sendMessageDelayed(msg, 100);

                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_navi,ttsId,R.string.condition_navi6_10, ttsText);
            }
        });
    }

    private void filterEndPoi(DataEntity dataEntity) {
        /**
         * {"address":"????????????","area":"?????????","category":"??????????????????;?????????;????????????","city":"?????????","distance":"734483.0","id":"B001B15Y1R",
         * "latitude":"30.580285","longitude":"114.300514","name":"?????????","phone":"",
         * "poiType":"viaLocNext","province":"?????????","source_category":"gaode_poi"}
         */
        List<JsonObject> result = dataEntity.result;
        List<JsonObject> endList = new ArrayList<JsonObject>();
        List<JsonObject> viaList = new ArrayList<JsonObject>();
        List<JsonObject> viaNextList = new ArrayList<JsonObject>();
        try {
            for (JsonObject  item: result) {
                String poiType = item.get("poiType").getAsString();
                if ("endLoc".equals(poiType)) {
                    endList.add(item);
                } else if ("viaLoc".equals(poiType)) {
                    viaList.add(item);
                } else if ("viaLocNext".equals(poiType)) {
                    viaNextList.add(item);
                }
            }

            LogUtils.d(TAG, "filterEndPoi --> befroe: " + result.size() + "  endList: " + endList.size() + "  viaList: " + viaList.size()
            + "  viaNextList: " + viaNextList.size());
            result.clear();
            result.addAll(endList);
            dataEntity.resultVia = viaList;
            dataEntity.resultViaNext = viaNextList;
            try {
                dataEntity.endPoi = GsonUtil.stringToList(endList.toString(), PoiEntity.class);
            } catch (Exception e) {}
            try {
                dataEntity.viaPoi = GsonUtil.stringToList(viaList.toString(), PoiEntity.class);
            } catch (Exception e) {}
            try {
                dataEntity.viaPoiNext = GsonUtil.stringToList(viaNextList.toString(), PoiEntity.class);
            } catch (Exception e) {}
        } catch (Exception e) {}
    }

    private void xueFeiToMeixinRoute(IntentEntity intentEntity, String searchKey) {
        List<MXPoiEntity> mxPoiEntityList = new ArrayList<MXPoiEntity>();
        if (intentEntity.data != null && intentEntity.data.result!= null) {
            List<PoiEntity> poiEntityList = GsonUtil.stringToList(intentEntity.data.result.toString(), PoiEntity.class);
            mxPoiEntityList = MXPoiEntity.wrapIfly(poiEntityList);
        }
        startSelectAlongWaySearchData(mxPoiEntityList, GsonUtil.objectToString(intentEntity.semantic), searchKey);
    }

    private void getFromMeixin(Semantic.SlotsBean.EndLocBean end) {
        if (end != null && !TextUtils.isEmpty(end.ori_loc)) {
            requestPoiData(end.ori_loc, end.topic);
        } else {
            doExceptonAction(mContext);
        }
    }

    private boolean hasResultList(IntentEntity intentEntity) {
        return intentEntity.data != null && intentEntity.data.result != null && intentEntity.data.result.size() > 0;
    }


    @Override
    public void showPoiActivity(String resultStr, String semanticStr) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semanticStr);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI);
        if (intentEntity.semantic.slots.endLoc != null && intentEntity.semantic.slots.endLoc.topic != null) {
            if (intentEntity.semantic.slots.endLoc.topic.equals(PlatformConstant.Topic.OTHERS)) {
                String oriLoc = intentEntity.semantic.slots.endLoc.ori_loc;
                String[] toiletSays = mContext.getResources().getStringArray(R.array.topic_toilet_says);
                String[] resortSays = mContext.getResources().getStringArray(R.array.topic_resort_says);
                for (String toilet : toiletSays) {
                    if (toilet.contains(oriLoc)) {
                        intent.putExtra(FullScreenActivity.TOPIC, PlatformConstant.Topic.TOILET);
                        break;
                    }
                }
                for (String resort : resortSays) {
                    if (resort.contains(oriLoc)) {
                        intent.putExtra(FullScreenActivity.TOPIC, PlatformConstant.Topic.RESORT);
                        break;
                    }
                }
                intent.putExtra(FullScreenActivity.TOPIC, intentEntity.semantic.slots.endLoc.topic);
            } else {
                intent.putExtra(FullScreenActivity.TOPIC, intentEntity.semantic.slots.endLoc.topic);
            }
        }
        mContext.startActivity(intent);
    }

    @Override
    public void startNavigation(BaseEntity poiEntity) {
        /***********??????????????????*****************/
        if(GDSdkManager.getInstance(mContext).naviToPoi(poiEntity)){
            return;
        }
        /***********??????????????????*****************/
        mxSdkManager.backToMap(new MXSdkManager.Callback() {
            @Override
            public void success() {
//                Utils.exitVoiceAssistant();
                naviToPoi(poiEntity);
            }
        });
    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        LogUtils.i(TAG, "mvwAction  nMvwScene" + mvwLParamEntity.nMvwScene);
        switch (mvwLParamEntity.nMvwScene) {
            case MvwSession.ISS_MVW_SCENE_GLOBAL:
                LogUtils.i(TAG, "TSP_SCENE_GLOBAL");
                break;
            case MvwSession.ISS_MVW_SCENE_SELECT:
                EventBusUtils.sendRestartSpeechTimeOut();
                selectItem(mvwLParamEntity);
                break;
            case MvwSession.ISS_MVW_SCENE_CONFIRM:
                EventBusUtils.sendRestartSpeechTimeOut();
                confirm(mvwLParamEntity);
                break;
            case MvwSession.ISS_MVW_SCENE_ANSWER_CALL:
            case MvwSession.ISS_MVW_SCENE_OTHER:
            case MvwSession.ISS_MVW_SCENE_CUSTOME:
//                if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_NAVI) {
                    mxSdkManager.MxMapOperation(this, mvwLParamEntity);
//                }
                break;
        }
    }

    private void srSelectItem(int nMvwId){
        //???????????????????????????
        try {
            if(TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_SELECT){
                //????????????
                IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
                if (iController != null) {
                    if (iController instanceof MapController) {
                        MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
                        mvwLParamEntity.nMvwId = nMvwId;
                        mvwLParamEntity.nMvwScene = TspSceneAdapter.TSP_SCENE_SELECT;
                        selectItem(mvwLParamEntity);
                    }
                }
            } else {
                doExceptonAction(mContext);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            doExceptonAction(mContext);
        }
    }

    // 3???????????? ???????????????
    private void selectItem(MvwLParamEntity mvwLParamEntity) {
        Log.i("selectItem", mvwLParamEntity.nMvwId + "  ");
        if (mvwLParamEntity.nMvwId < 6) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId == 6) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.FINAL_PAGE;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId == 7) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.LAST_PAGE;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId == 8) {
            MessageListEvent messageEvent1 = new MessageListEvent();
            messageEvent1.eventType = MessageListEvent.ListEventType.NEXT_PAGE;
            EventBus.getDefault().post(messageEvent1);
        } else if (mvwLParamEntity.nMvwId >=9&&mvwLParamEntity.nMvwId <=14) { //?????????
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_PAGE;
            messageEvent.index = mvwLParamEntity.nMvwId - 8;
            EventBus.getDefault().post(messageEvent);
        } else if (mvwLParamEntity.nMvwId >=15&&mvwLParamEntity.nMvwId <=17) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId-9;
            EventBus.getDefault().post(messageEvent);
        }else if (mvwLParamEntity.nMvwId >=18&&mvwLParamEntity.nMvwId <=20) { //?????????
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_PAGE;
            messageEvent.index = mvwLParamEntity.nMvwId - 11;
            EventBus.getDefault().post(messageEvent);
        }else if (mvwLParamEntity.nMvwId >=21&&mvwLParamEntity.nMvwId <=29) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId-21;
            EventBus.getDefault().post(messageEvent);
        }else if (mvwLParamEntity.nMvwId >=30&&mvwLParamEntity.nMvwId <=38) {
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId-30;
            EventBus.getDefault().post(messageEvent);
        }else doExceptonAction(mContext);
    }

    private void confirm(MvwLParamEntity mvwLParamEntity) {
        String top = ActivityManagerUtils.getInstance(mContext).getTopPackage();
        LogUtils.i(TAG, "confirm  currentPage: "  + mxSdkManager.currentPage +  "  nMvwId: " + mvwLParamEntity.nMvwId
         +  " isExitShowShow:" + mxSdkManager.isExitShowShow() + "  top: " + top);
        // ?????????????????????????????????????????????????????????????????????
        if(mxSdkManager.isExitShowShow() && AppConstant.PACKAGE_NAME_WECARNAVI.equals(top)){
            switch (mvwLParamEntity.nMvwId) {
                case 0:
                    LogUtils.i(TAG, "??????");
                    TTSController.getInstance(mContext).stopTTS();
                    TTSController.getInstance(mContext).releaseVoiceAudioFocus();
                    stopNaviMutual("??????");
                    break;
                case 1:
                    //??????
                    LogUtils.i(TAG, "??????");
                    TTSController.getInstance(mContext).stopTTS();
                    TTSController.getInstance(mContext).releaseVoiceAudioFocus();
                    stopNaviMutual("??????");

                    // ?????????????????????????????????????????????

                    LogUtils.d(TAG, "switch to NAVI SCENE navi dialog");
                    MVWAgent.getInstance().stopMVWSession();
//                    mvwAgent.setMvwKeyWords(MvwSession.ISS_MVW_SCENE_ANSWER_CALL, Utils.getFromAssets(mContext, "mvw_navi.json"));
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
                    break;
            }
            return;
        }

        switch (mvwLParamEntity.nMvwId) {
            case 0:
                LogUtils.i(TAG, "??????");
                MessageListEvent messageEvent = new MessageListEvent();
                messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
                messageEvent.index = mvwLParamEntity.nMvwId;
                EventBus.getDefault().post(messageEvent);
                break;
            case 1:
                //??????

                if (TspSceneAdapter.getTspScene(mContext) == TspSceneAdapter.TSP_SCENE_SELECT) {
                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_end_selection,TtsConstant.NAVIC38CONDITION,R.string.condition_navi3, mContext.getString(R.string.search_list_select_timeout_2));
                }

                LogUtils.i(TAG, "??????");
                Utils.exitVoiceAssistant();
                break;
        }
    }

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {
        mxSdkManager.MxMapOperation(stkResultEntity);
    }

    private void wantOpenNavi(){
        String conditionId=TtsConstant.NAVIC2CONDITION;
        String defaultTts = "??????????????????";
//        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_unspecified_search,conditionId,R.string.condition_navi2);
        openNavi(conditionId,defaultTts);
        return;
    }

    //????????????
    private void openNavi(String conditionId, String defaultTts) {
        /***********??????????????????*****************/
        if(GDSdkManager.getInstance(mContext).openNavi(conditionId,defaultTts,myHandler,mxSdkManager.isNaving())){
            return;
        }
        /***********??????????????????*****************/
        mxSdkManager.backToMap(new MXSdkManager.Callback() {
            @Override
            public void success() {
                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        if (TextUtils.isEmpty(tts)) {
                            tts = defaultTts;
                        }
                        Message msg = myHandler.obtainMessage(MSG_OPEN_NAVI, tts);
                        myHandler.sendMessageDelayed(msg, 100);

                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_unspecified_search,conditionId,R.string.condition_navi2, tts);
                    }
                });
            }
        });

    }

    public void naviToPoi(BaseEntity poiEntity) {

        navinLocInfo = getLocationFromPoi(poiEntity);

        LogUtils.d(TAG, "locationInfo:"+navinLocInfo.toString());
        extendApi.naviToPoi(navinLocInfo, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "naviToPoi???success");
            }

            @Override
            public void onFail(ExtendErrorModel errorModel) {
                LogUtils.d(TAG, "naviToPoi???error==" + errorModel.getErrorCode());
                startTTS("",mContext.getString(R.string.sorry_plan_failed));
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });
    }

    private LocationInfo getLocationFromPoi(BaseEntity entity) {
        LocationInfo locationInfo = new LocationInfo();
        locationInfo.setName(entity.getName());
        locationInfo.setAddress(entity.getAddress());
        locationInfo.setLatitude(Double.valueOf(entity.getLatitude()));
        locationInfo.setLongitude(Double.valueOf(entity.getLongitude()));

        String naviLat = entity.getNaviLatitude();
        String naviLon = entity.getNaviLongitude();
        if (!TextUtils.isEmpty(naviLat)) {
            locationInfo.setNaviLatitude(Double.valueOf(naviLat));
        }
        if (!TextUtils.isEmpty(naviLon)) {
            locationInfo.setNaviLongitude(Double.valueOf(naviLon));
        }
        return locationInfo;
    }

    private void naviToPoiWithPoint(BaseEntity poiEntity, BaseEntity viaEntity) {

        /***********??????????????????*****************/
        if(GDSdkManager.getInstance(mContext).naviToPoi(poiEntity)){
            return;
        }
        /***********??????????????????*****************/
        mxSdkManager.backToMap(new MXSdkManager.Callback() {
            @Override
            public void success() {
//                Utils.exitVoiceAssistant();
                naviToPoiWithPointExe(poiEntity, viaEntity);
            }
        });

    }

    private void naviToPoiWithPointExe(BaseEntity poiEntity, BaseEntity viaEntity) {
        navinLocInfo=getLocationFromPoi(poiEntity);
        ExtendPoi endPoi = getExendPoiFromPoi(poiEntity);
        ExtendPoi viaPoi = getExendPoiFromPoi(viaEntity);

        LogUtils.d(TAG, "naviToPoiWithPoint:"+navinLocInfo.toString() + " viaEntity: " + endPoi.toString());

        RequestRouteExModel requeset = new RequestRouteExModel();
        requeset.setEndExtendPoi(endPoi);
        ArrayList<ExtendPoi> list = new ArrayList<ExtendPoi>();
        list.add(viaPoi);
        requeset.setMidExtendPois(list);
        extendApi.requestRouteEx(requeset, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "naviToPoiWithPoint: success");
                /**
                 * ?????????????????????	naviC6_14			???????????????????????????????????????????????????
                 * ?????????????????????	naviC6_15	?????????????????????????????????
                 */
                Utils.eventTrack(mContext,R.string.skill_navi, R.string.scene_launch_navi, R.string.object_add_point_navi, TtsConstant.NAVIC6_14CONDITION, R.string.condition_navi6_14, mContext.getString(R.string.condition_default));
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "naviToPoiWithPoint: onFail");
                Utils.eventTrack(mContext,R.string.skill_navi, R.string.scene_launch_navi, R.string.object_add_point_navi, TtsConstant.NAVIC6_15CONDITION, R.string.condition_navi6_15, mContext.getString(R.string.condition_default));
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
                LogUtils.d(TAG, "naviToPoiWithPoint: onJSONResult");
            }
        });
    }

    private ExtendPoi getExendPoiFromPoi(BaseEntity entity) {
        ExtendPoi extendPoi = new ExtendPoi();
        extendPoi.setPoiName(entity.getName());
        extendPoi.setAddress(entity.getAddress());
        extendPoi.setLatitude(Double.valueOf(entity.getLatitude()));
        extendPoi.setLongitude(Double.valueOf(entity.getLongitude()));

        String naviLat = entity.getNaviLatitude();
        String naviLon = entity.getNaviLongitude();
        if (!TextUtils.isEmpty(naviLat)) {
            extendPoi.setNaviLatitude(Double.valueOf(naviLat));
        }
        if (!TextUtils.isEmpty(naviLon)) {
            extendPoi.setNaviLongitude(Double.valueOf(naviLon));
        }

        return extendPoi;
    }

    public void requestPoiData(final String searchKey, final String topic) {
        try {
            //??????????????????????????????????????????
            LogUtils.d(TAG, "requestPoiData  searchKey: " + searchKey + "  topic: " + topic);
            extendApi.requestPoiData(searchKey, new IExtendCallback<SearchResultModel>() {
                @Override
                public void success(SearchResultModel searchResultModel) {
                    LogUtils.d(TAG, "success:");
                    ArrayList<LocationInfo> resultList = searchResultModel.getResultList();
                    for (LocationInfo info : resultList) {
                        LogUtils.d(TAG, info.toString());
                    }

                    //???????????????wrap??????????????????
                    List<MXPoiEntity> mxPoiEntityList = MXPoiEntity.wrapMx(resultList);
                    startSelectSpecialNaviData(mxPoiEntityList, null, searchKey, topic);
                }

                @Override
                public void onFail(ExtendErrorModel extendErrorModel) {
                    int errorCode = extendErrorModel.getErrorCode();
                    LogUtils.d(TAG, "errorCode:"+errorCode+"  ???????????????"+NetworkUtil.isNetworkAvailable(mContext));

                    if (errorCode == -10050) {
                        noOne();
                        return;
                    }

                    if (!NetworkUtil.isNetworkAvailable(mContext)) {
                        String mainMsg = mContext.getString(R.string.no_network_tip);
                        startTTS(TtsConstant.MAINC19CONDITION,mainMsg);
                    } else {

                        if (errorCode == 10009 || errorCode == 10019) {
                            startTTS("",mContext.getString(R.string.network_weka_tip));
                        } else {
                            noOne();
                        }

                    }
                }

                @Override
                public void onJSONResult(JSONObject jsonObject) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            doExceptonAction(mContext);
        }

    }

    public void showMyLocation() {
        /***********??????????????????*****************/
        GDSdkManager.getInstance(mContext).showMyLocation();
        /***********??????????????????*****************/

        int i = 1; //????????????????????????
        if (mxSdkManager.isForeground()) {
            i = 0; //?????????????????????
        }
        extendApi.showMyLocation(i, new IExtendCallback<LocationInfo>() {
            @Override
            public void success(LocationInfo locationInfo) {
                String location = locationInfo.getAddress() + locationInfo.getName();
                final String ttsText = Utils.replaceTts(mContext.getString(R.string.where_i_am), CURRENTPOI, location);
                final String conditionId=TtsConstant.NAVIC53CONDITION;
//                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_query,R.string.object_query_location,conditionId,R.string.condition_navi53);
                Utils.getMessageWithoutTtsSpeak(mContext,conditionId, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        String ttsInfo = tts;
                        if (TextUtils.isEmpty(tts)) {
                            ttsInfo = ttsText;
                        }else {
                            ttsInfo=Utils.replaceTts(tts, CURRENTPOI, location);
                        }
                        Message msg = myHandler.obtainMessage(MSG_SHOW_MYLOCATION, ttsInfo);
                        myHandler.sendMessageDelayed(msg, 100);

                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_query,R.string.object_query_location,conditionId,R.string.condition_navi53, ttsInfo);
                    }
                });

//                Utils.eventTrack(mContext,R.string.skill_navi, R.string.skill_navi, R.string.scene_navi_query,conditionId,R.string.condition_navi1);

            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "onFail:" + extendErrorModel.getErrorMessage());

                final String ttsText = mContext.getString(R.string.where_i_am_fail);
                final String conditionId=TtsConstant.NAVIC53_1CONDITION;
                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_query,R.string.object_query_location,conditionId,R.string.condition_navi53_1, ttsText);
                Utils.getMessageWithTtsSpeak(mContext, conditionId, ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }

    /**
     * ????????????
     * @param searchKey1
     */
    public void alongTheWaySearch(final String searchKey1, String topic) {
        boolean isNaving = mxSdkManager.isNaving();
        final String  searchKey = searchKey1.toUpperCase();
        LogUtils.d(TAG, "alongTheWaySearch()  searchKey:"+searchKey+"  isNaving:"+isNaving);

//        /***********??????????????????*****************/
//        if(GDSdkManager.getInstance(mContext).isGDNaving()){//???????????????
//            isNaving=true;
//        }
//        /***********??????????????????*****************/

        if (!isNaving) {
            Message msg = myHandler.obtainMessage(MSG_TTS, mContext.getString(R.string.map_along_way_search_fail));
            myHandler.sendMessageDelayed(msg, 100);
        } else {

            if (!isSupportType(searchKey, topic)) {
                String defaultTTS = Utils.replaceTts(mContext.getString(R.string.map_along_way_search_not_support), POITYPE, ""+searchKey);
//                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along,TtsConstant.NAVIC13CONDITION,R.string.condition_navi13);
                ttsWithoutFindLongWay(TtsConstant.NAVIC13CONDITION, defaultTTS, searchKey, R.string.condition_navi13);
                return;
            }

            if (!isPassPointAddable()) {
                return;
            }

            //????????????????????????????????????
            extendApi.requestAlongRouteData(searchKey, new IExtendCallback<SearchResultModel>() {
                @Override
                public void success(SearchResultModel searchResultModel) {
                    LogUtils.d(TAG, "success:" + searchResultModel);
                    ArrayList<LocationInfo> resultList = searchResultModel.getResultList();

                    for (LocationInfo info : resultList) {
                        LogUtils.d(TAG, info.toString());
                    }
                    //???????????????wrap??????????????????
                    List<MXPoiEntity> mxPoiEntityList = MXPoiEntity.wrapMx(resultList);
                    startSelectAlongWaySearchData(mxPoiEntityList, null, searchKey);
                }

                @Override
                public void onFail(ExtendErrorModel extendErrorModel) {
                    int errorCode = extendErrorModel.getErrorCode();
                    LogUtils.d(TAG, "onFail:" + errorCode);
                    if (!NetworkUtil.isNetworkAvailable(mContext)) {
                        String mainMsg = mContext.getString(R.string.no_network_tip);
                        startTTS(TtsConstant.MAINC19CONDITION,mainMsg);
                    } else {
                        String defaultTTS = Utils.replaceTts(mContext.getString(R.string.no_along_way_search_result), POITYPE, ""+searchKey);
//                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along,TtsConstant.NAVIC12CONDITION,R.string.condition_navi12);
                        ttsWithoutFindLongWay(TtsConstant.NAVIC12CONDITION, defaultTTS, searchKey, R.string.condition_navi12);
                    }
                }

                @Override
                public void onJSONResult(JSONObject jsonObject) {
                }
            });
        }
    }

    private boolean isSupportType(String searchKey, String topic) {

        if (TextUtils.isEmpty(topic)) {
            //?????????????????????????????????, ?????????????????????????????????????????????????????????ATM?????????????????????????????????
            List<String> supportTypes = Arrays.asList(mContext.getResources().getStringArray(R.array.map_along_way_search_types));
            return supportTypes.contains(searchKey);
        }

        for (int i = 0; i < PlatformConstant.ALONG_TOPIC.length; i++) {
            if (topic.equals(PlatformConstant.ALONG_TOPIC[i])) {
                return true;
            }
        }

        return false;
    }

    private static final int MAX_POINT = 2;
    /**
     * ?????????????????????????????????
     * @return  ??????????????????
     */
    private boolean isPassPointAddable() {
        //???????????????????????????????????????
        int passPointNum = extendApi.getPassPointNum();
        if (passPointNum >= MAX_POINT) {
            pointMax();
            return false;
        }
        return true;
    }

    private void pointMax() {
        boolean isFromDoPassAway = PlatformConstant.Operation.QUERY.equals(intentEntity.operation) || PlatformConstant.Operation.PASS_AWAY.equals(intentEntity.operation);
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.NAVIC10CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = mContext.getString(R.string.map_along_way_search_max_passpoint);
                }
                Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                myHandler.sendMessageDelayed(msg, 10);

                if (isFromDoPassAway) {

                    if (intentEntity.semantic.slots.viaLocNext != null) {
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two,
                                TtsConstant.NAVIC6_25CONDITION,R.string.condition_navi6_25, mContext.getString(R.string.condition_navi18));
                    } else {
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point,TtsConstant.NAVIC6_7CONDITION,R.string.condition_navi6_7, ttsText);
                    }
                } else {
                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along,TtsConstant.NAVIC10CONDITION,R.string.condition_navi10, ttsText);
                }


            }
        });
    }

    /**
     *
     * @param conditionId
     * @param defaultText
     * @param searchKey
     * @param conditionIdRes
     */
    private void ttsWithoutFindLongWay(String conditionId, final String defaultText, final String searchKey, final int conditionIdRes) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultText;
                } else {
                    ttsText = Utils.replaceTts(ttsText, POITYPE, searchKey);
                }
                Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                myHandler.sendMessageDelayed(msg, 100);

                switch (conditionId) {
                    default:
//                    case TtsConstant.NAVIC12CONDITION:
//                    case TtsConstant.NAVIC13CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along,conditionId, conditionIdRes, ttsText);
                        break;
                    case TtsConstant.NAVIC6_4CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point,conditionId, conditionIdRes, ttsText);
                        break;
                }

            }
        });
    }

    /**
     * ???????????????
     * @param mxPoiEntityList
     * @param semanticStr
     * @param searchKey
     */
    private void startSelectAlongWaySearchData(List<MXPoiEntity> mxPoiEntityList, String semanticStr, final String searchKey) {

        if (!isPassPointAddable()) {
            return;
        }

        String resultStr = GsonUtil.objectToString(mxPoiEntityList);
        LogUtils.d(TAG, "jsonArray:" + resultStr);

        if (TextUtils.isEmpty(semanticStr)) {//????????????
            Semantic semantic = GsonUtil.stringToObject(Utils.getFromAssets(mContext, "default_semantic.json"), Semantic.class);
            semantic.slots.endLoc.ori_loc = searchKey;
            semantic.slots.endLoc.topic = PlatformConstant.Topic.ROUTEP;
            semanticStr = GsonUtil.objectToString(semantic);
        }

        boolean isFromDoPassAway = PlatformConstant.Operation.QUERY.equals(intentEntity.operation) || PlatformConstant.Operation.PASS_AWAY.equals(intentEntity.operation);

        try {
            //????????????
            if (mxPoiEntityList == null || mxPoiEntityList.size() <= 0) {
                LogUtils.w(TAG, "poiEntityList.size()<=0");

                if (isFromDoPassAway) {
                    // ????????????xxx,??????xxx????????????
                    String defalutTTS = mContext.getString(R.string.no_along_way_search_result1);
                    ttsWithoutFindLongWay(TtsConstant.NAVIC6_4CONDITION, defalutTTS, searchKey, R.string.condition_navi6_4);
                    return;
                }

                String defalutTTS = Utils.replaceTts(mContext.getString(R.string.no_along_way_search_result), POITYPE, ""+searchKey);
                ttsWithoutFindLongWay(TtsConstant.NAVIC12CONDITION, defalutTTS, searchKey, R.string.condition_navi12);
                return;
            }

//            if (mxPoiEntityList.size() > 1) {
//                MXPoiEntity temp = mxPoiEntityList.get(0);
//                mxPoiEntityList.clear();
//                mxPoiEntityList.add(temp);
//                resultStr = GsonUtil.objectToString(mxPoiEntityList);
//            }

            //??????????????????
            if (mxPoiEntityList.size() == 1) {

                final String ttsId = TtsConstant.NAVIC24CONDITION;
                Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        if (TextUtils.isEmpty(tts)) {
                            tts = mContext.getString(R.string.map_set_Point_site);
                        }
                        final String mainMessage =Utils.replaceTts(tts, "#POI#", ""+mxPoiEntityList.get(0).getName());
                        Message msg = myHandler.obtainMessage(MSG_ADD_PASS, mainMessage);
                        Bundle data = new Bundle();
                        data.putSerializable(KEY_POI, mxPoiEntityList.get(0));
                        /***********??????????????????*****************/
                        data.putSerializable("lon",mxPoiEntityList.get(0).getLongitude());
                        data.putSerializable("lat",mxPoiEntityList.get(0).getLatitude());
                        /***********??????????????????*****************/
                        msg.setData(data);
                        myHandler.sendMessageDelayed(msg, 100);
//                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along, TtsConstant.NAVIC24CONDITION, R.string.condition_navi8);

                        if (isFromDoPassAway) {
                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point, TtsConstant.NAVIC6_3CONDITION, R.string.condition_navi6_3 , mainMessage);
                        } else {
                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along, ttsId, R.string.condition_navi8 , mainMessage);
                        }

                    }
                });


                return;
            }
            //????????????
            final String name = mxPoiEntityList.get(0).getName();
            final String size = mxPoiEntityList.size() + "";

            String ttsId = isFromDoPassAway ? TtsConstant.NAVIC6_2CONDITION : TtsConstant.NAVIC11CONDITION;

            moreOneVia(name, size, semanticStr, searchKey, resultStr, ttsId, mContext.getString(R.string.make_sure_add_naame_as_point), POI1);
        } catch (Exception e) {
            LogUtils.e(TAG, "e = " + e.toString());
            doExceptonAction(mContext);
        }
    }

    private void moreOneVia(String name, String size, String semanticStr, String searchKey, String resultStr, String ttsId, String defaultText, String replaceKey) {
        showAlongWaySearchPoiActivity(resultStr, semanticStr);

        final String finalMainMessage = Utils.replaceTts(defaultText, replaceKey, ""+name);
//            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along, ttsId,R.string.condition_navi11);
        Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = finalMainMessage;
                } else {
                    ttsText = Utils.replaceTts(ttsText, replaceKey, name);
                }
                Message msg = myHandler.obtainMessage(MSG_SEARCH_RESULT_SELECT, ttsText);
                Bundle data = new Bundle();
                data.putString(KEY_POI_NAME, name);
                data.putString(KEY_POI_NAME_KEY, replaceKey);
                data.putString(KEY_POI_SIZE, size);
                data.putString(KEY_POI_SEARCH_WORD, searchKey);
                msg.setData(data);
                myHandler.sendMessageDelayed(msg, 100);

                switch (ttsId) {
//                    case TtsConstant.NAVIC11CONDITION:
                    default:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_search_along, ttsId,R.string.condition_navi11, ttsText);
                        break;
                    case TtsConstant.NAVIC6_2CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point, ttsId,R.string.condition_navi6_2, ttsText);
                        break;
                    case TtsConstant.NAVIC6_11CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_navi, ttsId,R.string.condition_navi6_11, ttsText);
                        break;
                    case TtsConstant.NAVIC6_17CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two, ttsId,R.string.condition_navi6_17, ttsText);
                        break;
                    case TtsConstant.NAVIC6_19CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_two, ttsId,R.string.condition_navi6_19, ttsText);
                        break;
                }

            }
        });
        poiType = POI_TYPE_ADD_PASS;
    }

    private void startSelectSpecialNaviData(List<MXPoiEntity> mxPoiEntityList, String semanticStr, String searchKey, String topic) {
        String resultStr = GsonUtil.objectToString(mxPoiEntityList);
        LogUtils.d(TAG, "jsonArray:" + resultStr);

        if (TextUtils.isEmpty(semanticStr)) {//????????????
            Semantic semantic = GsonUtil.stringToObject(Utils.getFromAssets(mContext, "default_semantic.json"), Semantic.class);
            semantic.slots.endLoc.ori_loc = searchKey;
            semantic.slots.endLoc.topic = topic;
            semanticStr = GsonUtil.objectToString(semantic);
        }

        try {
            //????????????
            int size = mxPoiEntityList.size();
            if (size <= 0) {
                noOne();
                return;
            }

            //??????????????????
            if (size == 1) {
                String desName = mxPoiEntityList.get(0).getName();
                onlyOne(mxPoiEntityList.get(0),desName, topic);
                return;
            }

            showSpecialNaviPoiActivity(resultStr, semanticStr, topic);
            String desName = mxPoiEntityList.get(0).getName();
            //????????????
            moreOne(desName, searchKey, size);
            poiTopic = topic;
        } catch (Exception e) {
//            LogUtils.e(TAG, "e = " + e.toString());
            e.printStackTrace();
        }
    }

    //2:??????????????? ?????????????????? ????????????
    private void startSelect(String ori_loc) {
        final List<PoiEntity> poiEntityList = new ArrayList<>();
        try {
            poiEntityList.addAll(GsonUtil.stringToList(intentEntity.data.result.toString(), PoiEntity.class));
            int size = poiEntityList.size();
            //????????????
            if (size <= 0) {
                noOne();
                return;
            }

            //??????????????????
            if (size == 1) {
                String desName = poiEntityList.get(0).getName();
                onlyOne(poiEntityList.get(0),desName, "");
                return;
            }

            showPoiActivity(intentEntity.data.result.toString(), GsonUtil.objectToString(intentEntity.semantic));
            String desName = poiEntityList.get(0).getName();
            //????????????
            moreOne(desName, ori_loc, size);
        } catch (Exception e) {
            LogUtils.e(TAG, "e = " + e.toString());
        }

    }

    public void  noOne(){
        LogUtils.w(TAG, "poiEntityList.size()<=0");

        String ttsId = TtsConstant.NAVIC6CONDITION;
        Utils.getMessageWithoutTtsSpeak(mContext, ttsId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = mContext.getString(R.string.no_poi_search_result);
                }
                LogUtils.d(TAG, "ttsText:" + ttsText);
                Message msg = myHandler.obtainMessage(MSG_TTS, ttsText);
                myHandler.sendMessageDelayed(msg, 100);

                if (isNearBySearch()) {
                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_navigation_to_destination,TtsConstant.NAVIC9CONDITION,R.string.condition_navi9, ttsText);
                } else {
                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_navigation_to_destination,ttsId,R.string.condition_navi6, ttsText);
                }

            }
        });
    }

    public void  onlyOne(BaseEntity poiEntity, String desName, String topic){
        String mainMessage =Utils.replaceTts(mContext.getString(R.string.one_poi_search_result), POI1, ""+desName);
        String conditionId = TtsConstant.NAVIC5CONDITION;
        String poiKey = POI1;

        int scene = R.string.scene_launch_navi;
        int object = R.string.object_navigation_to_destination;
        int condition = R.string.condition_navi5;

        if (poiEntity instanceof  MXPoiEntity) {
            if (PlatformConstant.Topic.HOME.equals(topic)) {
                poiKey = "#POI#";
                mainMessage = Utils.replaceTts(mContext.getResources().getString(R.string.map_set_home_site), poiKey, desName);
                conditionId = TtsConstant.NAVIC19CONDITION;
//                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi19);
                scene = R.string.scene_navi_select_destination;
                object = R.string.object_choose_the_number;
                condition = R.string.condition_navi19;
                collectByPoi((MXPoiEntity) poiEntity, 1);
            } else if (PlatformConstant.Topic.COMPANY.equals(topic)) {
                poiKey = "#POI#";
                mainMessage = Utils.replaceTts(mContext.getResources().getString(R.string.map_set_company_site), poiKey, desName);
                conditionId = TtsConstant.NAVIC20CONDITION;
//                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi20);
                scene = R.string.scene_navi_select_destination;
                object = R.string.object_choose_the_number;
                condition = R.string.condition_navi20;
                collectByPoi((MXPoiEntity) poiEntity, 2);
            }
        }


        String finalMainMessage = mainMessage;
        String finalPoiKey = poiKey;
        int finalScene = scene;
        int finalObject = object;
        int finalCondition = condition;
        String finalConditionId = conditionId;
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = finalMainMessage;
                } else {
                    ttsText = Utils.replaceTts(ttsText, finalPoiKey,desName);
                }
                Message msg = myHandler.obtainMessage(MSG_START_NAVI, ttsText);
                Bundle data = new Bundle();
                data.putSerializable(KEY_POI_ENTITY, poiEntity);
                msg.setData(data);
                myHandler.sendMessageDelayed(msg, 100);

//                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_navigation_to_destination,TtsConstant.NAVIC5CONDITION,R.string.condition_navi5);

                if (isNearBySearch()) {
                    Utils.eventTrack(mContext,R.string.skill_navi, finalScene, finalObject, TtsConstant.NAVIC8CONDITION, R.string.condition_navi8, ttsText);
                } else {
                    Utils.eventTrack(mContext,R.string.skill_navi, finalScene, finalObject, finalConditionId, finalCondition, ttsText);
                }

            }
        });
    }


    public void  moreOne(String desName, String ori_loc, int size){
//        MVWAgent.getInstance().stopMVWSession();
//        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
        boolean isKeyWord = intentEntity.text.endsWith("??????");
        String oldStr = POI1;
        String mainMessage = Utils.replaceTts(mContext.getString(R.string.make_sure_navigate_to_desName), oldStr, ""+desName);
        String condition = TtsConstant.NAVIC4CONDITION;
        if (isKeyWord) {
            mainMessage = Utils.replaceTts(mContext.getString(R.string.there_are_multiple_keyword_select_number), oldStr, ""+desName);
        } else if (intentEntity != null && intentEntity.semantic != null &&
                intentEntity.semantic.slots != null && intentEntity.semantic.slots.endLoc != null &&
                !PlatformConstant.Topic.OTHERS.equals(intentEntity.semantic.slots.endLoc.topic)) {
            // topic??????others??????POI1=ori_loc
            String string = mContext.getString(R.string.there_are_multiple_keyword_select_number1);
            string = String.format(string, size);
            mainMessage = Utils.replaceTts(string, oldStr, ""+ori_loc);
            condition = TtsConstant.NAVIC4_1CONDITION;
            desName = ori_loc;// ????????????????????????????????????
        }

        moreOneTTS(mainMessage,desName,oldStr, ori_loc, condition, size);
    }


    public void  moreOneTTS(String mainMessage, String desName, String oldStr, String searchKey, String condition, int size){
        Utils.getMessageWithoutTtsSpeak(mContext, condition, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = mainMessage;
                } else {
                    ttsText = Utils.replaceTts(ttsText, oldStr, desName);
                    ttsText = Utils.replaceTts(ttsText, NUM, size+"");
                }
                Message msg = myHandler.obtainMessage(MSG_SEARCH_RESULT_SELECT, ttsText);
                Bundle data = new Bundle();
                data.putString(KEY_POI_NAME, desName);
                data.putString(KEY_POI_NAME_KEY, oldStr);
                data.putString(KEY_POI_SIZE, size + "");
                data.putString(KEY_POI_SEARCH_WORD, searchKey);
                msg.setData(data);
                myHandler.sendMessageDelayed(msg, 100);

                switch (condition) {
                    case TtsConstant.NAVIC4CONDITION:
                        if (isNearBySearch()) {
                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_navigation_to_destination,TtsConstant.NAVIC7CONDITION,R.string.condition_navi7, ttsText);
                        } else {
                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_navigation_to_destination,condition,R.string.condition_navi4, ttsText);
                        }

                        break;
                    case TtsConstant.NAVIC4_1CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_navigation_to_destination,condition,R.string.condition_navi4_1, ttsText);
                        break;
                    case TtsConstant.NAVIC6_8CONDITION:
                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point_navi,condition,R.string.condition_navi6_8, ttsText);
                        break;
                }

            }
        });
        poiType = POI_TYPE_START_NAVI;
    }

    private boolean isNearBySearch() {
        if (TextUtils.isEmpty(intentEntity.text)) {
            return false;
        }
        return intentEntity.text.contains("??????") || intentEntity.text.contains("??????") || intentEntity.text.contains("??????");
    }

    @Override
    public void requestAddPass(BaseEntity entity) {
        //??????????????????
//        Utils.exitVoiceAssistant();

//        /***********??????????????????*****************/
//        if(GDSdkManager.getInstance(mContext).addPassPoint(poiName,lon,lat)){
//            return;
//        }
//        /***********??????????????????*****************/

        if (!isPassPointAddable()) {
            return;
        }

        ExtendPoi poi = getExendPoiFromPoi(entity);
        ModifyNaviViaModel modifyNaviViaModel = new ModifyNaviViaModel(poi);

        boolean isFromDoPassAway = PlatformConstant.Operation.QUERY.equals(intentEntity.operation) || PlatformConstant.Operation.PASS_AWAY.equals(intentEntity.operation);

        LogUtils.d(TAG, "requestAddPass ============= " + entity.getName() + "  lon: " + entity.getLongitude() + "  lat: " + entity.getLatitude() + "  isFromDoPassAway: " + isFromDoPassAway);
        extendApi.requestAddPass(modifyNaviViaModel, new IExtendCallback<ExtendBaseModel>() {

            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "requestAddPass onSuccess");
                if (isFromDoPassAway) {
                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point,
                            TtsConstant.NAVIC6_5CONDITION,R.string.condition_navi6_5, mContext.getString(R.string.condition_navi18));
                }
            }

            @Override
            public void onFail(ExtendErrorModel errorModel) {
                LogUtils.d(TAG, "requestAddPass onFail:" + errorModel.getErrorCode());
                if (isFromDoPassAway) {
                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_add_point,
                            TtsConstant.NAVIC6_6CONDITION,R.string.condition_navi6_6, mContext.getString(R.string.add_point_both_fail));
                }
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }

    @Override
    public void collectByPoi(final MXPoiEntity entity, final int type) {
        LocationInfo info = new LocationInfo();
        info.setName(entity.getName());
        info.setAddress(entity.getAddress());
        info.setCityName(entity.getCityName());
        info.setLatitude(Double.valueOf(entity.getLatitude()));
        info.setLongitude(Double.valueOf(entity.getLongitude()));
        extendApi.collectByPoi(type, info, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                LogUtils.d(TAG, "collectByPoi success??? type:" + type + ",name:" + entity.getName());
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                LogUtils.d(TAG, "collectByPoi onFail:" + extendErrorModel.getErrorCode());
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {
            }
        });
    }


//    private void showListActivity(String endStr, String viaStr, String semanticStr) {
//        Intent intent = new Intent(mContext, FullScreenActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
//        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semanticStr);
//        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI);
//        intent.putExtra(FullScreenActivity.TOPIC, PlatformConstant.Topic.ROUTEP);
//        mContext.startActivity(intent);
//    }

    private void showAlongWaySearchPoiActivity(String resultStr, String semanticStr) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semanticStr);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI);
        intent.putExtra(FullScreenActivity.TOPIC, PlatformConstant.Topic.ROUTEP);
        mContext.startActivity(intent);
    }

    private void showSpecialNaviPoiActivity(String resultStr, String semanticStr, String topic) {
        Intent intent = new Intent(mContext, FullScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FullScreenActivity.DATA_LIST_STR, resultStr);
        intent.putExtra(FullScreenActivity.SEMANTIC_STR, semanticStr);
        intent.putExtra(FullScreenActivity.TYPE, AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI);
        intent.putExtra(FullScreenActivity.TOPIC, topic);
        mContext.startActivity(intent);
    }

    /**
     * @param destType 0: ??? 1?????????
     * @param source   MVW: ??????????????? SR??????????????????
     */
    public void specialPoiNavi(final int destType, final int source) {
        extendApi.specialPoiNavi(destType, 1, new IExtendCallback() {
            @Override
            public void success(ExtendBaseModel extendBaseModel) {
                if (destType == 0) {
//                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_go_home,TtsConstant.NAVIC15CONDITION,R.string.condition_navi15);
                    startTTS( TtsConstant.NAVIC15CONDITION, mContext.getString(R.string.map_navi_to_home));
                    Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.NAVIC15CONDITION, new TtsUtils.OnConfirmInterface() {
                        @Override
                        public void onConfirm(String tts) {
                            if (TextUtils.isEmpty(tts)) {
                                tts = mContext.getString(R.string.map_navi_to_home);
                            }
                            if (source == AppConstant.SOURCE_MWV) {
                                Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_go_home,TtsConstant.MHXC11CONDITION,R.string.condition_navi15,tts);
                            } else {
                                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_go_home,TtsConstant.NAVIC15CONDITION,R.string.condition_navi15,tts);
                            }

                        }
                    });
                } else {
                    extendApi.getHomeOrCompanyData(2, new IExtendCallback<LocationInfo>() {
                        @Override
                        public void success(LocationInfo locationInfo) {
                            final String companyName = locationInfo.getName();
                            final String defaultTTS = Utils.replaceTts(mContext.getString(R.string.map_navi_to_company), COMPANYPOIT,companyName);
//                            Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_go_to_company,TtsConstant.NAVIC17CONDITION,R.string.condition_navi17);
                            Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.NAVIC17CONDITION, new TtsUtils.OnConfirmInterface() {
                                @Override
                                public void onConfirm(String tts) {
                                    if (!TextUtils.isEmpty(tts)) {
                                        tts = Utils.replaceTts(tts, COMPANYPOIT, companyName);
                                    } else {
                                        tts = defaultTTS;
                                    }
                                    Utils.startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
                                        @Override
                                        public void onPlayStopped() {
                                            if (!FloatViewManager.getInstance(mContext).isHide()) {
                                                FloatViewManager.getInstance(mContext).hide();
                                            }
                                        }
                                    });
//                                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_go_to_company,TtsConstant.MHXC11CONDITION,R.string.condition_null,tts);

                                    if (source == AppConstant.SOURCE_MWV) {
                                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,R.string.object_go_to_company,TtsConstant.MHXC12CONDITION,R.string.condition_navi17,tts);
                                    } else {
                                        Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,R.string.object_go_to_company,TtsConstant.NAVIC17CONDITION,R.string.condition_navi17,tts);
                                    }
                                }
                            });
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
            }

            @Override
            public void onFail(ExtendErrorModel extendErrorModel) {
                int errorCode = extendErrorModel.getErrorCode();
                LogUtils.d(TAG, "onFail: " + extendErrorModel.getErrorCode());

                //????????????????????? || ????????????????????????
                if (errorCode == -10030 || errorCode == -10031) {
                    if (FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()) {
                        FloatViewManager.getInstance(mContext).show(FloatViewManager.WARE_BY_OTHER);
                    }
                    final String mainMsg;
                    String conditionId = "";
                    int condition;
                    int object;
                    if (destType == 0) {
                        conditionId = TtsConstant.NAVIC14CONDITION;
                        mainMsg = mContext.getString(R.string.map_no_set_home_site);
                        condition=R.string.condition_navi14;
                        object=R.string.object_go_home;
                    } else {
                        conditionId = TtsConstant.NAVIC16CONDITION;
                        mainMsg = mContext.getString(R.string.map_no_set_company_site);
                        condition=R.string.condition_navi16;
                        object=R.string.object_go_to_company;
                    }

//                    Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,object,conditionId,condition);
                    String finalConditionId = conditionId;
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

                            if (source == AppConstant.SOURCE_MWV) {
                                if(TtsConstant.NAVIC14CONDITION.equals(finalConditionId))
                                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,object, TtsConstant.MHXC11CONDITION,condition, ttsText); //???
                                else  //??????
                                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_navi,object, TtsConstant.MHXC12CONDITION,condition, ttsText);
                            } else {
                                Utils.eventTrack(mContext,R.string.skill_navi,R.string.scene_launch_navi,object, finalConditionId,condition, ttsText);
                            }
                        }
                    });

                }
            }

            @Override
            public void onJSONResult(JSONObject jsonObject) {

            }
        });
    }

    // ????????????id?????????text
    public void setInnerEntityToMXSdk(int id, String text, int nMvwScene, int type) {
        InstructionEntity innerEntity=new InstructionEntity();
        innerEntity.id=id;
        innerEntity.text=text;
        innerEntity.nMvwScene=nMvwScene;
        innerEntity.type = type;
        mxSdkManager.setInnerEntity(innerEntity);
    }

    /**
     * ?????????????????????
     * @param text
     */
    public void onDoAction(String text) {
        Log.d(TAG, "onDoAction() called with: text = [" + text + "]");
        if(text==null||"".equals(text)){
            doExceptonAction(mContext);
        }else {
            if (text.equals(ID_HEAD_FIRST)){
                 setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                 mxSdkManager.highSpeed();
            }else if (text.equals(ID_NO_HEAD)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.noSpeed();
            }else if (text.equals(ID_FEW_CHARGE)||text.equals(ID_NO_CHARGE)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.lessCharge();
            }else if (text.equals(ID_FEW_BLOCK)||text.equals(ID_NO_BLOCK) || ID_NO_BLOCK1.equals(text)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.avoidingCongestion();
            }else if (text.contains(ID_HOW_LONG)||text.contains(ID_HOW_FAR)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.howFar();
            }else if (text.equals(ID_FINISH_NAVI)||text.equals(ID_STOP_NAVI)||text.equals(ID_EXIT_NAVI)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.exitNavi(AppConstant.SOURCE_SR);
            }else if (text.equals(ID_ZOOMIN)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.enlargeMap();
            }else if (text.equals(ID_ZOOMOUT)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.reduceMap();
            }else if (text.equals(ID_TRAFFIC_ON)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.openRoad();
            }else if (text.equals(ID_TRAFFIC_OFF)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.closeRoad();
            }else if (text.equals(ID_TTS_OFF) || ID_TTS_OFF1.equals(text)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.closeNaviBroadcast();
            }else if (text.equals(ID_TTS_ON) || ID_TTS_ON1.equals(text)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.openNaviBroadcast();
            }else if (text.equals(ID_DAY_MODE) || text.equals(ID_DAY_MODE1)){
//                doExceptonAction(mContext);
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.setShowMode(MXSdkManager.SHOW_DAY, null);
            }else if (text.equals(ID_NIGHT_MODE) || ID_NIGHT_MODE1.equals(text) || ID_NIGHT_MODE2.equals(text) || ID_NIGHT_MODE3.equals(text)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.setShowMode(MXSdkManager.SHOW_NIGHT, null);
//                doExceptonAction(mContext);
            }else if (text.equals(ID_2D_MODE)||text.equals(ID_2D_VIEW)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.D2Mode();
            }else if (text.equals(ID_3D_MODE)||text.equals(ID_3D_VIEW)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.D3Mode();
            }else if (text.equals(ID_HEAD_UP)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.D3Mode();
            }else if (text.equals(ID_NORTH_UP)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.D3Mode();
            }else if (text.contains(ID_LOOK_ALL)){
                setInnerEntityToMXSdk(-1,text,8, InstructionEntity.TYPE_DO_ACTION);
                mxSdkManager.D3Mode();
            }
            else if(text.equals("??????")||text.equals("??????")) {
                confirm(selectWordToIndex(text));
            } else{
                selectItem(selectWordToIndex(text));
            }
        }
    }

    /*********??????????????????  private????????? public  ??????????????????*************/
    public static class MyHandler extends Handler {

        private final WeakReference<MapController> mapControllerWeakReference;

        private MyHandler(MapController mapController) {
            this.mapControllerWeakReference = new WeakReference<>(mapController);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            final MapController mapController = mapControllerWeakReference.get();
            if (mapController == null) {
                LogUtils.d(TAG, "mapController == null");
                return;
            }
            Bundle data;
            switch (msg.what) {
                case MSG_TTS:
                    if (FloatViewManager.getInstance(mapController.mContext).isHide()) {
                        LogUtils.d(TAG, "MSG_TTS   FloatViewManager isHide   return!!!!!!");
                        return;
                    }
                    mapController.startTTS("",(String) msg.obj);
                    break;
                case MSG_OPEN_NAVI:
                    mapController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            mapController.waitOpenNaviMultiInterface();
                            //??????????????????
                            SRAgent.getInstance().resetSrTimeCount();
                            TimeoutManager.saveSrState(mapController.mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                        }
                    });
                    break;
                case MSG_START_NAVI:
                    data = msg.getData();
                    Utils.checkNotNull(data, "bundle can't be null");
                    final BaseEntity poiEntity = (BaseEntity) data.getSerializable(KEY_POI_ENTITY);
                    mapController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            Utils.exitVoiceAssistant();
                        }
                    });
                    // ICA????????????????????????????????????
                    mapController.startNavigation(poiEntity);
                    break;
//                    case MSG_START_NAVI_WITH_POINT:
//                        data = msg.getData();
//                        Utils.checkNotNull(data, "bundle can't be null");
//                        final BaseEntity endPoi = (BaseEntity) data.getSerializable(KEY_POI_ENTITY);
//                        final BaseEntity viaPoi = (BaseEntity) data.getSerializable(KEY_POI_ENTITY_VIA);
//                        mapController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
//                            @Override
//                            public void onPlayStopped() {
//                                Utils.exitVoiceAssistant();
//                            }
//                        });
//                        // ICA????????????????????????????????????
//                        mapController.naviToPoiWithPoint(endPoi, viaPoi);
//                    break;
                case MSG_SPECIAL_NAVI:
                    data = msg.getData();
                    int destType = data.getInt("destType");
                    int source = data.getInt("source");
                    mapController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            mapController.waitSpecialNaviMultiInterface(destType, source);
                            //??????????????????
                            SRAgent.getInstance().resetSrTimeCount();
                            TimeoutManager.saveSrState(mapController.mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                        }
                    });
                    break;
                case MSG_SEARCH_RESULT_SELECT:
                    data = msg.getData();
                    Utils.checkNotNull(data, "bundle can't be null");
                    final String poiName = data.getString(KEY_POI_NAME);
                    String poiKey = data.getString(KEY_POI_NAME_KEY);
                    String ttsText = (String) msg.obj;
                    LogUtils.e(TAG, "poiName???" + poiName + "   poiKey: " + poiKey + "  ttsText: " + ttsText);
                    //??????????????????
                    SRAgent.getInstance().resetSrTimeCount();

                    String searchKey = data.getString(KEY_POI_SEARCH_WORD);

                    // ?????????????????????????????????
                    String size = data.getString(KEY_POI_SIZE);
                    String text = "????????????"+ size +"???"+ searchKey +"???????????????????????????????????????????????????";
                    TimeoutManager.saveSrState(mapController.mContext, TimeoutManager.UNDERSTAND_ONCE, text);

                    // ????????????????????????????????????
                    if (!TextUtils.isEmpty(ttsText)) {
                        Utils.replaceTts(ttsText, poiKey, poiName);
                    }
                    mapController.startTTSOnly(ttsText);
                    break;
                case MSG_ADD_PASS:
                    data = msg.getData();
                    Utils.checkNotNull(data, "bundle can't be null");
                    final BaseEntity entity = (BaseEntity) data.get(KEY_POI);
                    /***********??????????????????*****************/
                    final String lon2 = data.getString("lon");
                    final String lat2 = data.getString("lat");
                    /***********??????????????????*****************/
                    mapController.startTTSOnly((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            /***********??????????????????*****************/
//                            mapController.requestAddPass(poiName2);
                            Utils.exitVoiceAssistant();
                            mapController.requestAddPass(entity);
                            /***********??????????????????*****************/
                        }
                    });
                    break;
                case MSG_ERROR_POI:
                    mapController.startTTS((String) msg.obj, new TTSController.OnTtsStoppedListener() {
                        @Override
                        public void onPlayStopped() {
                            mapController.waitCorrectWrongAddressMultiInterface();
                            //??????????????????
                            SRAgent.getInstance().resetSrTimeCount();
                            TimeoutManager.saveSrState(mapController.mContext, TimeoutManager.UNDERSTAND_ONCE, "");
                        }
                    });
                    break;
                case MSG_SHOW_MYLOCATION:
                    mapController.startTTS((String)msg.obj );
                    break;
            }
        }
    }

    private void waitOpenNaviMultiInterface() {
        LogUtils.d(TAG, "waitOpenNaviMultiInterface");

        //??????persPOISet??????
        MultiInterfaceUtils.getInstance(mContext).uploadMapUPersPOISetData();
        MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity);
    }

    /**
     * @param destType 0: ??? 1?????????
     * @param source   MVW: ??????????????? SR??????????????????
     */
    private void waitSpecialNaviMultiInterface(int destType, int source) {
        LogUtils.d(TAG, "waitSpecialNaviMultiInterface, destType: " + destType + ",source:" + source);

        if (source == AppConstant.SOURCE_MWV) {
            //????????????????????????
            //MultiInterfaceUtils.getInstance(mContext).uploadCmdDefaultData();
            MultiInterfaceUtils.getInstance(mContext).uploadMapUPersPOISetData();
        } else {
            //??????persPOISet??????
            MultiInterfaceUtils.getInstance(mContext).uploadMapUPersPOISetData();
        }
        //?????????????????????
        SRAgent srAgent = SRAgent.getInstance();
        if (FloatViewManager.getInstance(mContext).isHide()) {
            if (!srAgent.mSrArgu_New.scene.equals(SrSession.ISS_SR_SCENE_ALL)) {
                srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
                srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");
                srAgent.stopSRSession();
                srAgent.startSRSession();
            }
        }

        if (!FloatViewManager.getInstance(mContext).isHide()) {
            //?????????????????????????????????
            IntentEntity intentEntity1 = new IntentEntity();
            intentEntity1.service = PlatformConstant.Service.MAP_U;
            intentEntity1.operation = PlatformConstant.Operation.USR_POI_SET;
            Semantic semantic = GsonUtil.stringToObject(Utils.getFromAssets(mContext, "default_semantic.json"), Semantic.class);
            semantic.slots.endLoc.ori_loc = (destType == 0) ? PlatformConstant.Topic.HOME : PlatformConstant.Topic.COMPANY;
            semantic.slots.endLoc.topic = (destType == 0) ? PlatformConstant.Topic.HOME : PlatformConstant.Topic.COMPANY;
            intentEntity1.semantic = semantic;
            MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity1);
        }

    }

    private void waitCorrectWrongAddressMultiInterface() {
        LogUtils.d(TAG, "waitCorrectWrongPOIMultiInterface");

        //?????????????????????
        SRAgent srAgent = SRAgent.getInstance();
        if (FloatViewManager.getInstance(mContext).isHide()) {
            if (!srAgent.mSrArgu_New.scene.equals(SrSession.ISS_SR_SCENE_ALL)) {
                srAgent.mSrArgu_Old = new SrSessionArgu(srAgent.mSrArgu_New);
                srAgent.setSrArgu_New(SrSession.ISS_SR_SCENE_ALL, "");
                srAgent.stopSRSession();
                srAgent.startSRSession();
            }
        }

        IntentEntity intentEntity1;
        Semantic semantic;
        switch (poiType) {
            case POI_TYPE_START_NAVI:
                //??????persPOISet??????
                MultiInterfaceUtils.getInstance(mContext).uploadMapUPersPOISetData();
                //??????????????????->??????????????????????????????
                intentEntity1 = new IntentEntity();
                intentEntity1.service = PlatformConstant.Service.MAP_U;
                intentEntity1.operation = PlatformConstant.Operation.OPEN;
                semantic = GsonUtil.stringToObject(Utils.getFromAssets(mContext, "default_semantic.json"), Semantic.class);
                semantic.slots.insType = "OPEN";
                intentEntity1.semantic = semantic;
                MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity1);
                break;
            case POI_TYPE_ADD_PASS:
                //????????????????????????
                intentEntity1 = new IntentEntity();
                intentEntity1.service = PlatformConstant.Service.MAP_U;
                intentEntity1.operation = PlatformConstant.Operation.ALONG_SEARCH;
                MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity1);
                break;
            case POI_TYPE_SPECIAL_NAVI:
                //???????????????????????????
                intentEntity1 = new IntentEntity();
                intentEntity1.service = PlatformConstant.Service.MAP_U;
                intentEntity1.operation = PlatformConstant.Operation.USR_POI_SET;
                semantic = GsonUtil.stringToObject(Utils.getFromAssets(mContext, "default_semantic.json"), Semantic.class);
                semantic.slots.endLoc.ori_loc = poiTopic;
                semantic.slots.endLoc.topic = poiTopic;
                intentEntity1.semantic = semantic;
                MultiInterfaceUtils.getInstance(mContext).saveMultiInterfaceSemantic(intentEntity1);
                break;
        }
    }


    public  void  stopNaviMutual(String text){
        mxSdkManager.stopNaviMutual(text);
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

    public void goHomeOrCompany(final int destType, final int source) {
        /***********??????????????????*****************/
        if(GDSdkManager.getInstance(mContext).goHomeOrCompany(destType,source,extendApi,myHandler)){
            return;
        }
        /***********??????????????????*****************/

        if (mxSdkManager.isForeground()) {
            specialPoiNavi(destType, source);
        } else {
            mxSdkManager.backToMap(new MXSdkManager.Callback() {
                @Override
                public void success() {
                    LogUtils.d(TAG, "goHomeOrCompany success");
                    specialPoiNavi(destType, source);
                }
            });
        }
    }

    private void openCarController(String value, int type) {
        Intent intent = new Intent();
        intent.setPackage(CarController.PACKAGE_NAME_NAVI);
        intent.setAction(CarController.OPEN_VC_FROM_OTHERS);
        intent.putExtra(CarController.EXTRA_KEY_OPEN_VC, value);
        intent.putExtra(CarController.EXTRA_KEY_OPEN_VC_TYPE, type);
        mContext.sendBroadcast(intent);
    }

    private void closeCarController() {
        mxSdkManager.backToMap(null);
    }

    public void  searchAddress(String text){

        if (TextUtils.isEmpty(text)){
            doExceptonAction(mContext);
            return;
        }

        if (text.endsWith("??????")){
            goHomeOrCompany(0, AppConstant.SOURCE_SR);
            return;
        }

        if (text.endsWith("?????????")){
            goHomeOrCompany(1, AppConstant.SOURCE_SR);
            return;
        }

        if (text.startsWith("?????????")){
            text=text.replace("?????????","").trim();
            requestPoiData(text, "others");
            return;
        }

        if (text.startsWith("?????????")){
            text=text.replace("?????????","").trim();
            requestPoiData(text, "others");
            return;
        }

        if (text.startsWith("???")){
            text=text.replace("???","").trim();
            requestPoiData(text, "others");
            return;
        }
            requestPoiData(text, "others");
            return;

     }

}
