package com.chinatsp.ifly.voice.platformadapter.controller;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.entity.ArtEvent;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.entity.MessageListEvent;
import com.chinatsp.ifly.utils.ActivityManagerUtils;
import com.chinatsp.ifly.utils.AudioFocusUtils;
import com.chinatsp.ifly.utils.EventBusUtils;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.SharedPreferencesUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IChangbaController;
import com.chinatsp.ifly.voice.platformadapter.entity.IntentEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.MvwLParamEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.SongInfoEntity;
import com.chinatsp.ifly.voice.platformadapter.entity.StkResultEntity;
import com.chinatsp.ifly.voice.platformadapter.manager.AppControlManager;
import com.chinatsp.ifly.voice.platformadapter.manager.BluePhoneManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.chinatsp.phone.bean.CallContact;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;
import com.iflytek.mvw.MvwSession;
import com.iflytek.speech.util.NetworkUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chinatsp.ifly.api.constantApi.TtsConstant.SKYLIGHTC23CONDITION;

/**
 * Created by Administrator on 2020/6/12.
 */

public class ChangbaController extends BaseController implements IChangbaController{
    private final String TAG = "ChangbaController";
    public static final String CHANGBANAME = "??????";
    private Context mContext;
    private static ChangbaController mChangbaController;
    private static final int SEARCH = 1001;
    private static final int STARTCB = 1002;
    private static final int REQUEST_SONG_NUMS = 1003;
    private static final int SELECT_WHICH_ONE = 1004;
    private static final int NEXT_PAGE = 1005;
    private static final int LAST_PAGE = 1006;
    private static final int REQUEST_PAGE_NUMS = 1007;
    private static final int REQUEST_PAUSE = 1008;
    private static final int REQUEST_PLAY = 1009;
    private static final int OPEN_ORIGINAL = 1010;
    private static final int OPEN_ACCOMPANY = 1011;
    private static final int NEXT_SONG = 1012;
    private static final int CHECK_ACTIVITY = 1013;
    private static final int LAST_PAGE_TTS = 10014;
    private static final int NEXT_PAGE_TTS = 10015;
    private static final int SELECT_WHICH_ONE_TTS = 10016;
    private static final int REQUEST_SONG_NUMS_RESULT = 10017;
    private static final int REQUEST_SONG_NUMS_RESULT_2 = 10018;
    private static final int SKIP_HEAD = 10019;
    private static final int MSG_SHOW_WORD = 2001;
    private static final int RESEARCH = 3001;
    private final String ARTIST = "Artist";
    private final String SONG = "Song";
    private int page = 0;
    private int totalPage = 0;
    private boolean isFirstPage = true;
    private boolean isLastPage = false;
    public int playStatus = 1;
    private int count = INITCOUNT;
    private static final int INITCOUNT = -100;
    private List<SongInfoEntity> songInfoLists;
    private boolean isStartSelect = true;
    private String currentAritist = "";
    private String currentSong = "";
    private String conditionId = "";
    private String defaultText = "";
    private int originalStatus = 2;
    private boolean isSongListNull = true;
    public String initActivity = "";
    private String MAINACTIVITY = "com.changba.tv.module.main.ui.MainActivity";//?????????
    public String SONGLISTDETAILACTIVITY = "com.changba.tv.module.choosesong.ui.SongListDetailActivity";//????????????
    public String RECORDACTIVITY = "com.changba.tv.module.singing.ui.activity.RecordActivity";//????????????
    private String WECHATQRCODELOGINACTIVITY= "com.changba.tv.login.WechatQrcodeLoginActivity";//??????????????????
    private String SUBSCRIBEACTIVITY= "com.changba.tv.module.account.ui.activity.SubscribeActivity";//????????????
    public String PINYINCHOOSESONGACTIVITY= "com.changba.tv.module.choosesong.ui.activity.PinYinChooseSongActivity";//????????????

    private boolean isInLogin = false;
    private boolean isInVip = false;
    private static final String ISLOGIN = "isLogin";
    private static final String ISVIP = "isVip";
    private boolean isHandling = false;
    private boolean isSkipSuccess = false;
    private boolean isNeedSearch = false;
    private boolean callSearch = false;
    private final static String OPEN_CHANGBA = "??????????????????????????????????????????";
    private boolean isOpenChangba = true;
    private static final String TRIGGER = "triggerWhat";
    private static final String GETSONGNUMS = "songNum";
    private static final String ISFOREGROUND = "isForeground";
    public static final String CBPLAYSTATUS = "cbPlayStatus";
    public static final String CBCURRENTARTIST = "currentArtist";
    public static final String CBCURRENTSONG = "currentSong";
    public int WAITTIME = 0;
    private boolean isC27Speak = false;
    private boolean isC29Speak = false;
    private int appName = 0;
    private int scene = 0;
    private int object = 0;
    private int condition = 0;

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean isLogin = SharedPreferencesUtils.getBoolean(mContext,ISLOGIN,false);
            boolean isVip = SharedPreferencesUtils.getBoolean(mContext,ISVIP,false);
            Log.d(TAG,"isLogin = " + isLogin + ",isVip = " + isVip);
            switch (msg.what) {
                case STARTCB:
                    break;
                case SEARCH:
                    if(msg.obj != null){
                        Log.d(TAG,"send search bd...");
                        Intent i = new Intent();
                        i.setComponent(new ComponentName("com.changba.sd","com.changba.tv.order.AudioOrderReceiver"));
                        i.putExtra("Command","Search");
                        Map map = (Map) msg.obj;
                        if(!TextUtils.isEmpty((String)map.get(ARTIST))){
                            i.putExtra(ARTIST,(String) map.get(ARTIST));
                            Log.d(TAG,"send search bd,Artist = " + (String) map.get(ARTIST));
                        }
                        if(!TextUtils.isEmpty((String)map.get(SONG))){
                            i.putExtra(SONG,(String)map.get(SONG));
                            Log.d(TAG,"send search bd,Song = " + (String) map.get(SONG));
                        }

                        mContext.sendBroadcast(i);

//                        mHandler.sendEmptyMessageDelayed(REQUEST_SONG_NUMS,1000);
//                        mHandler.sendEmptyMessageDelayed(REQUEST_PAGE_NUMS,1500);

                        //3.5s?????????????????????????????????
                        //mHandler.sendEmptyMessageDelayed(CHECK_ACTIVITY,3500);
                    }
                    break;
                case REQUEST_SONG_NUMS://?????????????????????????????????
                    sendMonitorCommandToCB(0x10090,0);
                    break;
                case REQUEST_PAGE_NUMS://??????????????????
                    sendMonitorCommandToCB(0x10110,0);
                    break;
                case SELECT_WHICH_ONE:
                    Map map = (Map) msg.obj;
                    int index = (int)map.get("index");
                    SharedPreferencesUtils.saveString(mContext,TRIGGER,GETSONGNUMS);
                    mHandler.sendEmptyMessageDelayed(REQUEST_SONG_NUMS,0);
                    Message msg1 = new Message();
                    Map<String,Integer> map1 = new HashMap<>();
                    map1.put("index",index);
                    msg1.what = SELECT_WHICH_ONE_TTS;
                    msg1.obj = map1;
                    mHandler.sendMessageDelayed(msg1,1600);//??????1200
                    break;
                case SELECT_WHICH_ONE_TTS:
                    Map map2 = (Map) msg.obj;
                    int index2 = (int)map2.get("index");
                    Log.d(TAG,"index = " + index2 + ",count = " + count);
                    appName = R.string.skill_changba;
                    scene = R.string.scene_changba_select;
                    if((FloatViewManager.getInstance(mContext).isHide()) && (count == 0 || count == -2)){
                        //????????????????????????????????????????????????
                    }else if((!FloatViewManager.getInstance(mContext).isHide()) && (count == 0 || count == -2)){
                        doExceptionAction();
                    } else if(index2 > count){
                        //?????????????????????
                        MVWAgent.getInstance().stopMVWSession();
                        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                        conditionId = TtsConstant.CHANGBAC32CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC32);
                        object = R.string.object_changba_select_which;
                        condition = R.string.condition_changbaC32;
                        getTtsMessageAndHide(false,conditionId,defaultText,"","",count + "",appName,scene,object,condition);
                    }else if(songInfoLists.get(index2 - 1).isVip() && !isLogin){
                        //??????????????????
                        conditionId = TtsConstant.CHANGBAC31_1CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC31_1);
                        object = R.string.object_changba_select_which;
                        condition = R.string.condition_changbaC31_1;
                        getTtsMessageAndHideClick(true,conditionId,defaultText,"","","",0x10100,index2,appName,scene,object,condition);
                    }else if(songInfoLists.get(index2 - 1).isVip() && isLogin && !isVip){
                        //??????????????????
                        conditionId = TtsConstant.CHANGBAC31_2CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC31_2);
                        object = R.string.object_changba_select_which;
                        condition = R.string.condition_changbaC31_2;
                        getTtsMessageAndHideClick(true,conditionId,defaultText,"","","",0x10100,index2,appName,scene,object,condition);
                    }else {
                        //??????????????????
                        if(!FloatViewManager.getInstance(mContext).isHide()){
                            conditionId = TtsConstant.CHANGBAC31CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC31);
                            object = R.string.object_changba_select_which;
                            condition = R.string.condition_changbaC31;
                            getTtsMessageAndHideClick(true,conditionId,defaultText,"","", "",0x10100,index2,appName,scene,object,condition);
                        }else {
                            sendMonitorCommandToCB(0x10100,index2);//??????????????????????????????????????????????????????
                        }
                    }
                    break;
                case NEXT_PAGE:
                    //?????????????????????
                    MVWAgent.getInstance().stopMVWSession();
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                    if(isLastPage){
                        Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC28CONDITION, (String) mContext.getText(R.string.changbaC28));
                        Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_select, R.string.object_changba_select_next,TtsConstant.CHANGBAC28CONDITION,R.string.condition_changbaC28);
                    }else {
                        sendOriginalCommandToCB("NextPage",null,0,null,true);
                        mHandler.sendEmptyMessageDelayed(REQUEST_PAGE_NUMS,1600);
                        if(!FloatViewManager.getInstance(mContext).isHide()){
                            if(!isC27Speak){
                                Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC27CONDITION, (String) mContext.getText(R.string.changbaC27));
                                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_select, R.string.object_changba_select_next,TtsConstant.CHANGBAC27CONDITION,R.string.condition_changbaC27);
                                isC27Speak = true;
                            }else {
                                Utils.getMessageWithTtsSpeak(mContext, null, "???");
                            }
                        }
                    }

//                    mHandler.sendEmptyMessageDelayed(REQUEST_PAGE_NUMS,0);
//                    mHandler.sendEmptyMessageDelayed(NEXT_PAGE_TTS,1600);
                    break;
                case NEXT_PAGE_TTS:
                    //?????????????????????
                    MVWAgent.getInstance().stopMVWSession();
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                    if(isLastPage){
                        Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC28CONDITION, (String) mContext.getText(R.string.changbaC28));
                        Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_select, R.string.object_changba_select_next,TtsConstant.CHANGBAC28CONDITION,R.string.condition_changbaC28);
                    }else {
                        sendOriginalCommandToCB("NextPage",null,0,null,true);
                        if(!FloatViewManager.getInstance(mContext).isHide()){
                            Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC27CONDITION, (String) mContext.getText(R.string.changbaC27));
                        }
                    }
                    break;
                case LAST_PAGE:
                    //?????????????????????
                    MVWAgent.getInstance().stopMVWSession();
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                    if(isFirstPage){
                        Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC30CONDITION, (String) mContext.getText(R.string.changbaC30));
                        Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_select, R.string.object_changba_select_last,TtsConstant.CHANGBAC30CONDITION,R.string.condition_changbaC30);
                    }else {
                        sendOriginalCommandToCB("PrePage",null,0,null,true);
                        mHandler.sendEmptyMessageDelayed(REQUEST_PAGE_NUMS,1600);
                        if(!FloatViewManager.getInstance(mContext).isHide()){
                            if(!isC29Speak){
                                Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC29CONDITION, (String) mContext.getText(R.string.changbaC29));
                                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_select, R.string.object_changba_select_last,TtsConstant.CHANGBAC29CONDITION,R.string.condition_changbaC29);
                                isC29Speak = true;
                            }else {
                                Utils.getMessageWithTtsSpeak(mContext, null, "???");
                            }
                        }
                    }

                    //mHandler.sendEmptyMessageDelayed(REQUEST_PAGE_NUMS,0);
                    //mHandler.sendEmptyMessageDelayed(LAST_PAGE_TTS,1600);
                    break;
                case LAST_PAGE_TTS:
                    //?????????????????????
                    MVWAgent.getInstance().stopMVWSession();
                    MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                    if(isFirstPage){
                        Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC30CONDITION, (String) mContext.getText(R.string.changbaC30));
                    }else {
                        sendOriginalCommandToCB("PrePage",null,0,null,true);
                        if(!FloatViewManager.getInstance(mContext).isHide()){
                            Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC29CONDITION, (String) mContext.getText(R.string.changbaC29));
                        }
                    }
                    break;
                case REQUEST_PAUSE:
                    String curActiveFocus = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
                    String topPackage = ActivityManagerUtils.getInstance(mContext).getTopPackage();
                    Log.d(TAG, "REQUEST_PAUSE: curActiveFocus = " + curActiveFocus + ",topPackage = " + topPackage);
                    //???????????????????????????????????????/????????????????????????????????????????????????????????????????????????????????????????????????
                    if(AppConstant.PACKAGE_NAME_CHANGBA.equals(curActiveFocus) && !AppConstant.PACKAGE_NAME_CHANGBA.equals(topPackage)){
                        sendOriginalCommandToCB("Pause",null,0,null,true);
                        break;
                    }
                    appName = R.string.skill_changba;
                    scene = R.string.scene_changba_select;
                    object = R.string.object_changba_control_pause;
                    String cbPlayStatus = SharedPreferencesUtils.getString(mContext,CBPLAYSTATUS,"0");
                    Log.d(TAG, "handleMessage: cbPlayStatus = " + cbPlayStatus);
                    if("1".equals(cbPlayStatus)){//?????????
                        conditionId = TtsConstant.CHANGBAC33CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC33);
                        condition = R.string.condition_changbaC33;
                    }else if("0".equals(cbPlayStatus)){//?????????
                        conditionId = TtsConstant.CHANGBAC34CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC34);
                        condition = R.string.condition_changbaC34;
                    }

                    if(!FloatViewManager.getInstance(mContext).isHide()){
                        //getTtsMessageAndHide(true,conditionId,defaultText,"","", "");
                        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
                            @Override
                            public void onConfirm(String tts) {
                                String defaultTts = tts;
                                if (TextUtils.isEmpty(defaultTts)) {
                                    defaultTts = defaultText;
                                }
                                Utils.startTTS(defaultTts, new TTSController.OnTtsStoppedListener() {
                                    @Override
                                    public void onPlayStopped() {
                                        if (!FloatViewManager.getInstance(mContext).isHide()) {
                                            FloatViewManager.getInstance(mContext).hide();
                                        }
                                        //????????????????????????????????????????????????
                                        sendOriginalCommandToCB("Pause",null,0,null,true);
                                        if(!TextUtils.isEmpty(conditionId)) Utils.eventTrack(mContext, appName,scene,object,conditionId,condition,defaultText);
                                    }
                                });
                            }
                        });

                        Log.d(TAG,"request_pause,FloatViewManager is show...");
                    }else {
                        //????????????????????????
                        sendOriginalCommandToCB("Pause",null,0,null,true);
                        Log.d(TAG,"request_pause,FloatViewManager is hide and no speak...");
                    }
                    break;
                case REQUEST_PLAY:
                    Log.d(TAG, "handleMessage: playStatus = " + playStatus);
                    String curActiveFocus_play = AudioFocusUtils.getInstance(mContext).getCurrentActiveAudioPkg();
                    String topPackage_play = ActivityManagerUtils.getInstance(mContext).getTopPackage();
                    Log.d(TAG, "REQUEST_PLAY: curActiveFocus = " + curActiveFocus_play + ",topPackage = " + topPackage_play);
                    if(AppConstant.PACKAGE_NAME_CHANGBA.equals(curActiveFocus_play) && !AppConstant.PACKAGE_NAME_CHANGBA.equals(topPackage_play)){
                        sendOriginalCommandToCB("Play",null,0,null,true);
                        break;
                    }
                    String cbPlayStatus2 = SharedPreferencesUtils.getString(mContext,CBPLAYSTATUS,"0");
                    Log.d(TAG, "handleMessage: cbPlayStatus2 = " + cbPlayStatus2);
                    appName = R.string.skill_changba;
                    scene = R.string.scene_changba_control;
                    object = R.string.object_changba_control_play;
                    if("1".equals(cbPlayStatus2)){//?????????
                        conditionId = TtsConstant.CHANGBAC35CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC35);
                        condition = R.string.condition_changbaC35;
                    }else if("0".equals(cbPlayStatus2)){//?????????
                        sendOriginalCommandToCB("Play",null,0,null,true);
                        conditionId = TtsConstant.CHANGBAC36CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC36);
                        condition = R.string.condition_changbaC36;
                    }

                    if(!FloatViewManager.getInstance(mContext).isHide()){
                        Log.d(TAG,"request_paly,FloatViewManager is show...");
                        getTtsMessageAndHide(true,conditionId,defaultText,"","", "",appName,scene,object,condition);
                    }else {
                        Log.d(TAG,"request_paly,FloatViewManager is hide and no speak...");
                        //????????????????????????
                    }
                    break;
                case OPEN_ORIGINAL://????????????
//                    if(originalStatus == 1){//??????
//                        getTtsMessageAndHide(true,TtsConstant.CHANGBAC42CONDITION,(String)mContext.getString(R.string.changbaC42),null,null,null);
//                    }else if(originalStatus == 2){//??????
//                        if(!FloatViewManager.getInstance(mContext).isHide()) {//?????????
//                            getTtsMessageAndHide(true,TtsConstant.CHANGBAC41CONDITION,(String)mContext.getString(R.string.changbaC41),null,null,null);
//                        }
//                    }
                    appName = R.string.skill_changba;
                    scene = R.string.scene_changba_control;
                    conditionId = TtsConstant.CHANGBAC41CONDITION;
                    defaultText = (String) mContext.getText(R.string.changbaC41);
                    object = R.string.object_changba_control_orignal;
                    condition = R.string.condition_changbaC41;

                    if(!FloatViewManager.getInstance(mContext).isHide()) {//?????????
                        getTtsMessageAndHide(true,null,"??????",null,null,null, appName,scene,object,condition);
                    }
                    //?????? ????????????
                    sendOriginalCommandToCB("SwitchOriginal",null,0,"Flag",true);
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc26,TtsConstant.MHXC26CONDITION,R.string.condition_null);
                    break;
                case OPEN_ACCOMPANY://????????????
//                    if(originalStatus == 1){//??????
//                        if(!FloatViewManager.getInstance(mContext).isHide()) {//?????????
//                            getTtsMessageAndHide(true,TtsConstant.CHANGBAC39CONDITION,(String)mContext.getString(R.string.changbaC39),null,null,null);
//                        }
//                        //?????? ????????????
//                        sendOriginalCommandToCB("SwitchOriginal",null,0,"Flag",false);
//                    }else if(originalStatus == 2){//??????
//                        getTtsMessageAndHide(true,TtsConstant.CHANGBAC40CONDITION,(String)mContext.getString(R.string.changbaC40),null,null,null);
//                    }
                    appName = R.string.skill_changba;
                    scene = R.string.scene_changba_control;
                    conditionId = TtsConstant.CHANGBAC39CONDITION;
                    defaultText = (String) mContext.getText(R.string.changbaC39);
                    object = R.string.object_changba_control_vice;
                    condition = R.string.condition_changbaC39;

                    if(!FloatViewManager.getInstance(mContext).isHide()) {//?????????
                        getTtsMessageAndHide(true,null,"??????",null,null,null,appName,scene,object,condition);
                    }
                    //?????? ????????????
                    sendOriginalCommandToCB("SwitchOriginal",null,0,"Flag",false);
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc27,TtsConstant.MHXC27CONDITION,R.string.condition_null);
                    break;
                case NEXT_SONG:
                    Log.d(TAG,"NEXT_SONG isSongListNull = " + isSongListNull);
                    appName = R.string.skill_changba;
                    scene = R.string.scene_changba_control;
                    object = R.string.object_changba_control_next;

                    if(isSongListNull){//????????????
                        conditionId = TtsConstant.CHANGBAC37_1CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC37_1);
                        condition = R.string.condition_changbaC37_1;
                        getTtsMessageAndHide(true,TtsConstant.CHANGBAC37_1CONDITION,(String)mContext.getString(R.string.changbaC37_1),null,null,null,
                                appName,scene,object,condition);
                    }else {//???????????????
                        sendOriginalCommandToCB("Play next",null,0,null,true);
                        conditionId = TtsConstant.CHANGBAC37CONDITION;
                        defaultText = (String) mContext.getText(R.string.changbaC37);
                        condition = R.string.condition_changbaC37;
                        if(!FloatViewManager.getInstance(mContext).isHide()){//?????????
                            getTtsMessageAndHide(true,TtsConstant.CHANGBAC37CONDITION,(String)mContext.getString(R.string.changbaC37),null,null,null,
                                    appName,scene,object,condition);
                        }
                    }
                    break;
                case CHECK_ACTIVITY:
                    currentAritist = SharedPreferencesUtils.getString(mContext,CBCURRENTARTIST,"");
                    currentSong = SharedPreferencesUtils.getString(mContext,CBCURRENTSONG,"");
                    if(WECHATQRCODELOGINACTIVITY.equals(initActivity)){//????????????
                        isHandling = false;
                        appName = R.string.skill_changba;
                        scene = R.string.scene_changba_play;
                        if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                            conditionId = TtsConstant.CHANGBAC23CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC23);
                            object = R.string.object_changba_play_song_singer;
                            condition = R.string.condition_changbaC23;
                        }else if(!TextUtils.isEmpty(currentAritist)){
                            conditionId = TtsConstant.CHANGBAC15CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC15);
                            object = R.string.object_changba_play_singer;
                            condition = R.string.condition_changbaC15;
                        }else if(!TextUtils.isEmpty(currentSong)){
                            conditionId = TtsConstant.CHANGBAC7CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC7);
                            object = R.string.object_changba_play_song;
                            condition = R.string.condition_changbaC7;
                        }
                        if(!FloatViewManager.getInstance(mContext).isHide())
                            getTtsMessageAndHide(true,conditionId,defaultText,currentAritist,currentSong,"", appName,scene,object,condition);
                    }else if(SUBSCRIBEACTIVITY.equals(initActivity)){//????????????
                        isHandling = false;
                        appName = R.string.skill_changba;
                        scene = R.string.scene_changba_play;
                        if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                            conditionId = TtsConstant.CHANGBAC23_1CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC23_1);
                            object = R.string.object_changba_play_song_singer;
                            condition = R.string.condition_changbaC23_1;
                        }else if(!TextUtils.isEmpty(currentAritist)){
                            conditionId = TtsConstant.CHANGBAC15_1CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC15_1);
                            object = R.string.object_changba_play_singer;
                            condition = R.string.condition_changbaC15_1;
                        }else if(!TextUtils.isEmpty(currentSong)){
                            conditionId = TtsConstant.CHANGBAC7_1CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC7_1);
                            object = R.string.object_changba_play_song;
                            condition = R.string.condition_changbaC7_1;
                        }
                        if(!FloatViewManager.getInstance(mContext).isHide())
                            getTtsMessageAndHide(true,conditionId,defaultText,currentAritist,currentSong,"",appName,scene,object,condition);
                    }else if(RECORDACTIVITY.equals(initActivity)){//????????????
                        isHandling = false;

                        if(!isCallIncoming())TspSceneManager.getInstance().switchSceneToChangba(mContext);//?????????????????????
                        appName = R.string.skill_changba;
                        scene = R.string.scene_changba_play;
                        if(isVip){
                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                conditionId = TtsConstant.CHANGBAC23_2CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC23_2);
                                object = R.string.object_changba_play_song_singer;
                                condition = R.string.condition_changbaC23_2;
                            }else if(!TextUtils.isEmpty(currentAritist)){
                                conditionId = TtsConstant.CHANGBAC15_2CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC15_2);
                                object = R.string.object_changba_play_singer;
                                condition = R.string.condition_changbaC15_2;
                            }else if(!TextUtils.isEmpty(currentSong)){
                                conditionId = TtsConstant.CHANGBAC7_2CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC7_2);
                                object = R.string.object_changba_play_song;
                                condition = R.string.condition_changbaC7_2;
                            }
                        }else {
                            Log.d(TAG, "handleMessage: currentAritist = " + currentAritist + ",currentSong = " + currentSong);
                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                conditionId = TtsConstant.CHANGBAC22CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC22);
                                object = R.string.object_changba_play_song_singer;
                                condition = R.string.condition_changbaC22;
                            }else if(!TextUtils.isEmpty(currentAritist)){
                                conditionId = TtsConstant.CHANGBAC14CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC14);
                                object = R.string.object_changba_play_singer;
                                condition = R.string.condition_changbaC14;
                            }else if(!TextUtils.isEmpty(currentSong)){
                                conditionId = TtsConstant.CHANGBAC6CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC6);
                                object = R.string.object_changba_play_song;
                                condition = R.string.condition_changbaC6;
                            }
                        }
                        if(!FloatViewManager.getInstance(mContext).isHide())
                            getTtsMessageAndHide(true,conditionId,defaultText,currentAritist,currentSong,"",appName,scene,object,condition);
                    }else if(SONGLISTDETAILACTIVITY.equals(initActivity)) {//??????????????????
                        Log.d(TAG,"start to get count...");
                        isHandling = false;

//                        mHandler.sendEmptyMessageDelayed(REQUEST_SONG_NUMS,0);
//                        if(msg.obj == null){
//                            //count = 0;
//                            Log.d(TAG,"start to get count..1.");
//                            Message checkMsg = new Message();
//                            checkMsg.what = REQUEST_SONG_NUMS_RESULT;
//                            checkMsg.obj = 1;//???????????????????????????
//                            mHandler.sendMessageDelayed(checkMsg,1500);//??????????????????1130
//                        }else {
//                            Log.d(TAG,"start to get count..2.");
//                            Message checkMsg = new Message();
//                            checkMsg.what = REQUEST_SONG_NUMS_RESULT;
//                            checkMsg.obj = 2;//???????????????????????????
//                            mHandler.sendMessageDelayed(checkMsg,1200);
//                        }
                    }else {
                        isHandling = false;
                    }
                    break;
                case REQUEST_SONG_NUMS_RESULT:
//                    if(count > 0){
//                        isHandling = false;
//
//                        Log.d(TAG,"?????????" + count + "?????????");
//                        if(count == 1){//?????????????????????
//                            currentAritist = SharedPreferencesUtils.getString(mContext,"currentAritist","");
//                            currentSong = SharedPreferencesUtils.getString(mContext,"currentSong","");
//                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC22CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC22);
//                            }else if(!TextUtils.isEmpty(currentAritist)){
//                                conditionId = TtsConstant.CHANGBAC14CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC14);
//                            }else if(!TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC6CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC6);
//                            }
//                            if(!FloatViewManager.getInstance(mContext).isHide()){
//                                Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
//                                    @Override
//                                    public void onConfirm(String tts) {
//                                        String ttsText = tts;
//                                        if (TextUtils.isEmpty(tts)){
//                                            ttsText = defaultText;
//                                        }
//                                        ttsText = Utils.replaceTts(ttsText,"#SINGER#",currentAritist);
//                                        ttsText = Utils.replaceTts(ttsText,"#SONG#",currentSong);
//
//                                        Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {
//                                            @Override
//                                            public void onPlayStopped() {
//                                                sendMonitorCommandToCB(0x10100,1);//??????????????????
//                                                FloatViewManager.getInstance(mContext).hide();
//                                            }
//                                        });
//                                    }
//                                });
//                            }
//                        }else if(!FloatViewManager.getInstance(mContext).isHide()){
//                            //?????????????????????
//                            MVWAgent.getInstance().stopMVWSession();
//                            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
//                            Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC26CONDITION, (String) mContext.getText(R.string.changbaC26));
//                        }
//                    }else if(count == INITCOUNT){
//                        Log.d(TAG,"????????????????????????");
//                        int checkTimes = (int)msg.obj;
//                        Log.d(TAG,"checkTimes = " + checkTimes);
//                        if(checkTimes == 1){
//                            Message checkMsgSecond = new Message();
//                            checkMsgSecond.what = CHECK_ACTIVITY;
//                            checkMsgSecond.obj = 2;//???????????????????????????
//                            mHandler.sendMessageDelayed(checkMsgSecond,800);
//                        }else {
//                            isHandling = false;
//
//                            currentAritist = SharedPreferencesUtils.getString(mContext,"currentAritist","");
//                            currentSong = SharedPreferencesUtils.getString(mContext,"currentSong","");
//                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC24CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC24);
//                            }else if(!TextUtils.isEmpty(currentAritist)){
//                                conditionId = TtsConstant.CHANGBAC16CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC16);
//                            }else if(!TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC8CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC8);
//                            }
//
//                            if(!FloatViewManager.getInstance(mContext).isHide()){
//                                getTtsMessageAndHide(true,conditionId,defaultText,currentAritist,currentSong,"");
//                            }
//                        }
//                    }else if(count == -1){
//                        isHandling = false;
//
//                        Log.d(TAG,"??????????????????????????????");
//                        if(!isLogin) {//?????????
//                            currentAritist = SharedPreferencesUtils.getString(mContext,"currentAritist","");
//                            currentSong = SharedPreferencesUtils.getString(mContext,"currentSong","");
//                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC23CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC23);
//                            }else if(!TextUtils.isEmpty(currentAritist)){
//                                conditionId = TtsConstant.CHANGBAC15CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC15);
//                            }else if(!TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC7CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC7);
//                            }
//                        }else if(isLogin && !isVip) {//???????????????????????????????????????????????????
//                            currentAritist = SharedPreferencesUtils.getString(mContext,"currentAritist","");
//                            currentSong = SharedPreferencesUtils.getString(mContext,"currentSong","");
//                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC23_1CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC23_1);
//                            }else if(!TextUtils.isEmpty(currentAritist)){
//                                conditionId = TtsConstant.CHANGBAC15_1CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC15_1);
//                            }else if(!TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC7_1CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC7_1);
//                            }
//                        }else if(isLogin && isVip) {//???????????????????????????????????????????????????
//                            currentAritist = SharedPreferencesUtils.getString(mContext,"currentAritist","");
//                            currentSong = SharedPreferencesUtils.getString(mContext,"currentSong","");
//                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC23_2CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC23_2);
//                            }else if(!TextUtils.isEmpty(currentAritist)){
//                                conditionId = TtsConstant.CHANGBAC15_2CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC15_2);
//                            }else if(!TextUtils.isEmpty(currentSong)){
//                                conditionId = TtsConstant.CHANGBAC7_2CONDITION;
//                                defaultText = (String) mContext.getText(R.string.changbaC7_2);
//                            }
//                        }
//                        sendMonitorCommandToCB(0x10100,1);//??????????????????????????????????????????????????????
//                        if(!FloatViewManager.getInstance(mContext).isHide()){
//                            getTtsMessageAndHide(true,conditionId,defaultText,currentAritist,currentSong,"");
//                        }
//                    }else if(count == -2){
//                        isHandling = false;
//
//                        Log.d(TAG,"????????????");
//                        currentAritist = SharedPreferencesUtils.getString(mContext,"currentAritist","");
//                        currentSong = SharedPreferencesUtils.getString(mContext,"currentSong","");
//                        if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
//                            conditionId = TtsConstant.CHANGBAC25CONDITION;
//                            defaultText = (String) mContext.getText(R.string.changbaC25);
//                        }else if(!TextUtils.isEmpty(currentAritist)){
//                            conditionId = TtsConstant.CHANGBAC17CONDITION;
//                            defaultText = (String) mContext.getText(R.string.changbaC17);
//                        }else if(!TextUtils.isEmpty(currentSong)){
//                            conditionId = TtsConstant.CHANGBAC9CONDITION;
//                            defaultText = (String) mContext.getText(R.string.changbaC9);
//                        }
//                        if(!FloatViewManager.getInstance(mContext).isHide()){
//                            getTtsMessageAndHide(true,conditionId,defaultText,"","","");
//                        }
//                    }else {
//                        isHandling = false;
//                        Log.d(TAG,"?????????-10000");
//                    }
                    break;
                case REQUEST_SONG_NUMS_RESULT_2:
                    break;
                case SKIP_HEAD://????????????
                    appName = R.string.skill_changba;
                    scene = R.string.scene_changba_control;
                    object = R.string.object_changba_control_skip;
                    if(isSkipSuccess){//??????????????????
                        if(!FloatViewManager.getInstance(mContext).isHide()) {//?????????
                            conditionId = TtsConstant.CHANGBAC43CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC43);
                            condition = R.string.condition_changbaC43;
                            getTtsMessageAndHide(true,TtsConstant.CHANGBAC43CONDITION,(String)mContext.getString(R.string.changbaC43),null,null,null,
                                    appName,scene,object,condition);
                        }
                    }else{//??????????????????
                        if(!FloatViewManager.getInstance(mContext).isHide()) {//?????????
                            conditionId = TtsConstant.CHANGBAC44CONDITION;
                            defaultText = (String) mContext.getText(R.string.changbaC44);
                            condition = R.string.condition_changbaC44;
                            getTtsMessageAndHide(true,TtsConstant.CHANGBAC44CONDITION,(String)mContext.getString(R.string.changbaC44),null,null,null,
                                    appName,scene,object,condition);
                        }
                    }
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc27_2,TtsConstant.MHXC27_2CONDITION,R.string.condition_null);
                    break;
                case MSG_SHOW_WORD:
                    Map mapOneShot = (Map) msg.obj;
                    IntentEntity intentEntity = (IntentEntity)mapOneShot.get("intentEntity");
                    srAction(intentEntity);
                    break;
                case RESEARCH:
                    openCBAction(false,conditionId,"","","");

                    Map<String,String> mapResearch = new HashMap<>();
                    mapResearch.put(ARTIST,SharedPreferencesUtils.getString(mContext,CBCURRENTARTIST,""));
                    mapResearch.put(SONG,SharedPreferencesUtils.getString(mContext,CBCURRENTSONG,""));
                    Message msgResearch = new Message();
                    msgResearch.what = SEARCH;
                    msgResearch.obj = mapResearch;
                    mHandler.sendMessageDelayed(msgResearch,100);
                    callSearch = false;
                    break;
            }
        }
    };

    public static ChangbaController getInstance(Context mContext){
        if(mChangbaController == null){
            mChangbaController = new ChangbaController(mContext);
        }
        return mChangbaController;
    }

    private ChangbaController(Context mContext){
        this.mContext = mContext;
        registerCBBroadcast(mContext);
    }

    private void registerCBBroadcast(Context mContext){
        IntentFilter intentFilter = new IntentFilter("os.com.oushang.autoclick.ACTION_CB_DATA_FEEDBACK");
        mContext.registerReceiver(cbReceiver,intentFilter);

        //??????????????????????????????
        IntentFilter intentFilter2 = new IntentFilter("os.com.oushang.autoclick.ACTION_CB_ACTIVITY_CHANGE");
        mContext.registerReceiver(cbActivityReceiver,intentFilter2);

        //?????????????????????
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("com.oushang.cb.SONGNAME");
        mContext.registerReceiver(cbSongNameReceiver,intentFilter3);
    }

    @Override
    public void srAction(IntentEntity intentEntity) {
        boolean isChangBaForeground = AppControlManager.getInstance(mContext).isAppointForeground(AppConstant.PACKAGE_NAME_CHANGBA);
        boolean isNetworkAvailable = NetworkUtil.isNetworkAvailable(mContext);
        SharedPreferencesUtils.saveBoolean(mContext,ISFOREGROUND,isChangBaForeground);
        String artist = "";
        String song = "";

        count = INITCOUNT;//???count??????
        if(intentEntity.semantic != null && intentEntity.semantic.slots != null && intentEntity.semantic.slots.pageRank != null){//???????????????
            MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
            mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_SELECT;
            if("?????????".equals(intentEntity.text)){
                mvwLParamEntity.nMvwId = 7;
            }else if("?????????".equals(intentEntity.text)){
                mvwLParamEntity.nMvwId = 6;
            }
            mvwAction(mvwLParamEntity);
        }else if(intentEntity.semantic != null && intentEntity.semantic.slots != null && intentEntity.semantic.slots.posRank != null &&
                intentEntity.semantic.slots.posRank.offset != null){//?????????
            MvwLParamEntity mvwLParamEntity = new MvwLParamEntity();
            mvwLParamEntity.nMvwScene = MvwSession.ISS_MVW_SCENE_SELECT;
            if("1".equals(intentEntity.semantic.slots.posRank.offset)){
                mvwLParamEntity.nMvwId = 21;
            }else if("2".equals(intentEntity.semantic.slots.posRank.offset)){
                mvwLParamEntity.nMvwId = 22;
            }else if("3".equals(intentEntity.semantic.slots.posRank.offset)){
                mvwLParamEntity.nMvwId = 23;
            }else if("4".equals(intentEntity.semantic.slots.posRank.offset)){
                mvwLParamEntity.nMvwId = 24;
            }else if("5".equals(intentEntity.semantic.slots.posRank.offset)){
                mvwLParamEntity.nMvwId = 25;
            }else {
                mvwLParamEntity.nMvwId = 26;
            }
            mvwAction(mvwLParamEntity);
        }else if(intentEntity.semantic != null && intentEntity.semantic.slots != null && intentEntity.semantic.slots.song != null
                && (intentEntity.semantic.slots.artist != null || intentEntity.semantic.slots.band != null)){//?????????artist???song
            if(RECORDACTIVITY.equals(initActivity)){//???????????????????????????????????????????????????????????????????????????
                Log.d(TAG,"exit recordactivity");
                sendMonitorCommandToCB(0x10002,0);
                isNeedSearch = true;
            }else {
                isNeedSearch = false;
            }
            if(intentEntity.semantic.slots.artist != null){
                artist = intentEntity.semantic.slots.artist;
            }else {
                artist = intentEntity.semantic.slots.band;
            }
            song = intentEntity.semantic.slots.song;
            Log.d(TAG,"artist = " + artist + ",song = " + song);

            SharedPreferencesUtils.saveString(mContext,CBCURRENTARTIST,artist);
            SharedPreferencesUtils.saveString(mContext,CBCURRENTSONG,song);

            if(!isChangBaForeground && isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC18CONDITION);
                getTtsMessage(false,TtsConstant.CHANGBAC18CONDITION,(String) mContext.getText(R.string.changbaC18),artist,song);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_song_singer,TtsConstant.CHANGBAC18CONDITION,R.string.condition_changbaC18);
                if(!AppControlManager.getInstance(mContext).isAppAlive(mContext,AppConstant.PACKAGE_NAME_CHANGBA)){
                    WAITTIME = 2000;
                }else {
                    WAITTIME = 100;
                }
                openCBAction(false,conditionId,"","","");
                Map<String,String> map = new HashMap<>();
                map.put(SONG,song);
                map.put(ARTIST,artist);
                Message msg = new Message();
                msg.what = SEARCH;
                msg.obj = map;
                if(isNeedSearch){
                    //mHandler.sendMessageDelayed(msg,600);
                    //isNeedSearch = false;
                }else {
                    mHandler.sendMessageDelayed(msg,WAITTIME);
                }
                callSearch = false;
            } else if(!isChangBaForeground && !isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC19CONDITION);
                openCBAction(false,conditionId,"","","");
                getTtsMessage(true,TtsConstant.CHANGBAC19CONDITION,(String) mContext.getText(R.string.changbaC19),artist,song);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_song_singer,TtsConstant.CHANGBAC19CONDITION,R.string.condition_changbaC19);
            }else if(isChangBaForeground && !isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC20CONDITION);
                getTtsMessage(true,TtsConstant.CHANGBAC20CONDITION,(String) mContext.getText(R.string.changbaC20),artist,song);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_song_singer,TtsConstant.CHANGBAC20CONDITION,R.string.condition_changbaC20);
            } else {
                WAITTIME = 0;
                Map<String,String> map = new HashMap<>();
                map.put(SONG,song);
                map.put(ARTIST,artist);
                Message msg = new Message();
                msg.what = SEARCH;
                msg.obj = map;
                if(isNeedSearch){
                    //mHandler.sendMessageDelayed(msg,600);
                    //isNeedSearch = false;
                }else {
                    mHandler.sendMessageDelayed(msg,WAITTIME);
                }
                callSearch = false;
            }
        } else if(intentEntity.semantic != null && intentEntity.semantic.slots != null && intentEntity.semantic.slots.song != null){//?????????song
            if(RECORDACTIVITY.equals(initActivity)){
                Log.d(TAG,"exit recordactivity");
                sendMonitorCommandToCB(0x10002,0);
                isNeedSearch = true;
            }else {
                isNeedSearch = false;
            }
            song = intentEntity.semantic.slots.song;
            Log.d(TAG,"song = " + song);

            SharedPreferencesUtils.saveString(mContext,CBCURRENTARTIST,"");
            SharedPreferencesUtils.saveString(mContext,CBCURRENTSONG,song);

            if(!isChangBaForeground && isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC2CONDITION);
                getTtsMessage(false,TtsConstant.CHANGBAC2CONDITION,(String) mContext.getText(R.string.changbaC2),null,song);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_song,TtsConstant.CHANGBAC2CONDITION,R.string.condition_changbaC2);
                if(!AppControlManager.getInstance(mContext).isAppAlive(mContext,AppConstant.PACKAGE_NAME_CHANGBA)){
                    WAITTIME = 2000;
                }else {
                    WAITTIME = 100;
                }
                openCBAction(false,conditionId,"","","");
                Map<String,String> map = new HashMap<>();
                map.put(SONG,song);
                Message msg = new Message();
                msg.what = SEARCH;
                msg.obj = map;
                if(isNeedSearch){
//                    mHandler.sendMessageDelayed(msg,600);
                    //isNeedSearch = false;
                }else {
                    mHandler.sendMessageDelayed(msg,WAITTIME);
                }
                callSearch = false;
            } else if(!isChangBaForeground && !isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC3CONDITION);
                openCBAction(false,conditionId,"","","");
                getTtsMessage(true,TtsConstant.CHANGBAC3CONDITION,(String) mContext.getText(R.string.changbaC3),null,song);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_song,TtsConstant.CHANGBAC3CONDITION,R.string.condition_changbaC3);
            }else if(isChangBaForeground && !isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC4CONDITION);
                getTtsMessage(true,TtsConstant.CHANGBAC4CONDITION,(String) mContext.getText(R.string.changbaC4),null,song);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_song,TtsConstant.CHANGBAC4CONDITION,R.string.condition_changbaC4);
            } else {
                WAITTIME = 0;
                Map<String,String> map = new HashMap<>();
                map.put(SONG,song);
                Message msg = new Message();
                msg.what = SEARCH;
                msg.obj = map;
                if(isNeedSearch){
                    //mHandler.sendMessageDelayed(msg,600);
                    //isNeedSearch = false;
                }else {
                    mHandler.sendMessageDelayed(msg,WAITTIME);
                }
                callSearch = false;
            }
        }else if(intentEntity.semantic != null && intentEntity.semantic.slots != null && (intentEntity.semantic.slots.artist != null ||
                intentEntity.semantic.slots.band != null)){//?????????artist??????
            if(RECORDACTIVITY.equals(initActivity)){
                Log.d(TAG,"exit recordactivity");
                sendMonitorCommandToCB(0x10002,0);
                isNeedSearch = true;
            }else {
                isNeedSearch = false;
            }

            if(intentEntity.semantic.slots.artist != null){
                artist = intentEntity.semantic.slots.artist;
            }else {
                artist = intentEntity.semantic.slots.band;
            }
            Log.d(TAG,"artist = " + artist);

            SharedPreferencesUtils.saveString(mContext,CBCURRENTARTIST,artist);
            SharedPreferencesUtils.saveString(mContext,CBCURRENTSONG,"");

            if(!isChangBaForeground && isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC10CONDITION);
                getTtsMessage(false,TtsConstant.CHANGBAC10CONDITION,(String) mContext.getText(R.string.changbaC10),artist,null);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_singer,TtsConstant.CHANGBAC10CONDITION,R.string.condition_changbaC10);
                if(!AppControlManager.getInstance(mContext).isAppAlive(mContext,AppConstant.PACKAGE_NAME_CHANGBA)){
                    WAITTIME = 2000;
                }else {
                    WAITTIME = 100;
                }
                openCBAction(false,conditionId,"","","");
                Map<String,String> map = new HashMap<>();
                map.put(ARTIST,artist);
                Message msg = new Message();
                msg.what = SEARCH;
                msg.obj = map;
                if(isNeedSearch){
                    //mHandler.sendMessageDelayed(msg,600);
                    //isNeedSearch = false;
                }else {
                    mHandler.sendMessageDelayed(msg,WAITTIME);
                }
                callSearch = false;
            } else if(!isChangBaForeground && !isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC11CONDITION);
                openCBAction(false,conditionId,"","","");
                getTtsMessage(true,TtsConstant.CHANGBAC11CONDITION,(String) mContext.getText(R.string.changbaC11),artist,null);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_singer,TtsConstant.CHANGBAC11CONDITION,R.string.condition_changbaC11);
            }else if(isChangBaForeground && !isNetworkAvailable){
                Log.d(TAG,"condtionId = " + TtsConstant.CHANGBAC12CONDITION);
                getTtsMessage(true,TtsConstant.CHANGBAC12CONDITION,(String) mContext.getText(R.string.changbaC12),artist,null);
                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_play_singer,TtsConstant.CHANGBAC12CONDITION,R.string.condition_changbaC12);
            } else {
                WAITTIME = 0;
                Map<String,String> map = new HashMap<>();
                map.put(ARTIST,artist);
                Message msg = new Message();
                msg.what = SEARCH;
                msg.obj = map;
                if(isNeedSearch){
                    //mHandler.sendMessageDelayed(msg,600);
                    //isNeedSearch = false;
                }else {
                    mHandler.sendMessageDelayed(msg,WAITTIME);
                }
                callSearch = false;
            }
        }else {//????????????
            SharedPreferencesUtils.saveString(mContext,CBCURRENTARTIST,"");
            SharedPreferencesUtils.saveString(mContext,CBCURRENTSONG,"");

            boolean isHide = intentEntity.semantic==null;//?????????????????????

            if(OPEN_CHANGBA.contains(intentEntity.text)){
                isOpenChangba = true;
            }else {
                isOpenChangba = false;
            }
            if(AppControlManager.getInstance(mContext).isAppAlive(mContext,AppConstant.PACKAGE_NAME_CHANGBA) &&
                    !AppControlManager.getInstance(mContext).isAppointForeground(AppConstant.PACKAGE_NAME_CHANGBA) &&
                    !(ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){
                //??????????????????????????????????????????
                Log.d(TAG,"not singing...");
                //?????????????????????????????????????????????????????????????????????????????????????????????
                openCBAction(false,conditionId,"","","");
                if(isOpenChangba){
                    getTtsMessage(false,TtsConstant.CHANGBAC1_1CONDITION,(String) mContext.getText(R.string.changbaC1_1),null,null);
                    if(isHide)
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc27_1,TtsConstant.MHXC27_1ONDITION,R.string.condition_null,(String) mContext.getText(R.string.changbaC1_1));
                    else
                        Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_open,TtsConstant.CHANGBAC1_1CONDITION,R.string.condition_changbaC1_1);
                }else {
                    getTtsMessage(false,TtsConstant.CHANGBAC1_3CONDITION,(String) mContext.getText(R.string.changbaC1_3),null,null);
                    if(isHide)
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc27_1,TtsConstant.MHXC27_1ONDITION,R.string.condition_null,(String) mContext.getText(R.string.changbaC1_1));
                    else
                        Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_open2,TtsConstant.CHANGBAC1_3CONDITION,R.string.condition_changbaC1_3);
                }
            }else if(AppControlManager.getInstance(mContext).isAppAlive(mContext,AppConstant.PACKAGE_NAME_CHANGBA) &&
                    !AppControlManager.getInstance(mContext).isAppointForeground(AppConstant.PACKAGE_NAME_CHANGBA) &&
                    (ChangbaController.getInstance(mContext).RECORDACTIVITY).equals(ChangbaController.getInstance(mContext).initActivity)){
                //??????????????????????????????????????????
                Log.d(TAG,"singing...");
                openCBAction(false,conditionId,"","","");
                getTtsMessage(true,TtsConstant.CHANGBAC1_2CONDITION,(String) mContext.getText(R.string.changbaC1_2),null,null);
                if(isHide)
                    Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc27_1,TtsConstant.MHXC27_1ONDITION,R.string.condition_null,(String) mContext.getText(R.string.changbaC1_1));
                else
                    Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_open,TtsConstant.CHANGBAC1_2CONDITION,R.string.condition_changbaC1_2);
            }else {//?????????????????????
                Log.d(TAG,"launch changba...");
                openCBAction(false,conditionId,"","","");
                if(isOpenChangba){
                    getTtsMessage(false,TtsConstant.CHANGBAC1CONDITION,(String) mContext.getText(R.string.changbaC1),null,null);
                    if(isHide)
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc27_1,TtsConstant.MHXC27_1ONDITION,R.string.condition_null,(String) mContext.getText(R.string.changbaC1_1));
                    else
                        Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_open,TtsConstant.CHANGBAC1CONDITION,R.string.condition_changbaC1);
                }else {
                    getTtsMessage(false,TtsConstant.CHANGBAC1_3CONDITION,(String) mContext.getText(R.string.changbaC1_3),null,null);
                    if(isHide)
                        Utils.eventTrack(mContext,R.string.skill_global_nowake,R.string.scene_KTV,R.string.object_mhcc27_1,TtsConstant.MHXC27_1ONDITION,R.string.condition_null,(String) mContext.getText(R.string.changbaC1_1));
                    else
                        Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_play, R.string.object_changba_open2,TtsConstant.CHANGBAC1_3CONDITION,R.string.condition_changbaC1_3);
                }
            }
        }

    }

    private BroadcastReceiver cbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action: " + action);
            if("os.com.oushang.autoclick.ACTION_CB_DATA_FEEDBACK".equals(action)){
                //TspSceneManager.getInstance().switchSceneToChangba(mContext);//?????????????????????
                isHandling = false;
                String type = intent.getStringExtra("type");//??????????????????
                Log.d(TAG,"type = " + type);
                String trigger = SharedPreferencesUtils.getString(mContext,TRIGGER,"");
                Log.d(TAG,"trigger = " + trigger);
                switch (type){
                    case "song_count"://????????????????????????
                        count = intent.getIntExtra("count",-10000);
                        Log.d(TAG, "count = " + count);

                        String songInfoJson = intent.getStringExtra("songInfoJson");
                        Log.d(TAG,"songInfoJson = " + songInfoJson);
                        try{
                            songInfoLists  = GsonUtil.stringToList(songInfoJson,SongInfoEntity.class);
                        }catch (Exception e){
                            Log.d(TAG,"songInfoListsException : " + e);
                        }

                        if(GETSONGNUMS.equals(trigger)){
                            SharedPreferencesUtils.saveString(mContext,TRIGGER,"");
                            break;
                        }
                        currentAritist = SharedPreferencesUtils.getString(mContext,CBCURRENTARTIST,"");
                        currentSong = SharedPreferencesUtils.getString(mContext,CBCURRENTSONG,"");
                        appName = R.string.skill_changba;
                        scene = R.string.scene_changba_play;
                        if(count > 0){
                            Log.d(TAG,"?????????" + count + "?????????");
                            if(count == 1){//?????????????????????
                                if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC22CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC22);
                                    object = R.string.object_changba_play_song_singer;
                                    condition = R.string.condition_changbaC22;
                                }else if(!TextUtils.isEmpty(currentAritist)){
                                    conditionId = TtsConstant.CHANGBAC14CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC14);
                                    object = R.string.object_changba_play_singer;
                                    condition = R.string.condition_changbaC14;
                                }else if(!TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC6CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC6);
                                    object = R.string.object_changba_play_song;
                                    condition = R.string.condition_changbaC6;
                                }
                                if(!FloatViewManager.getInstance(mContext).isHide()){
                                    Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
                                        @Override
                                        public void onConfirm(String tts) {
                                            String ttsText = tts;
                                            if (TextUtils.isEmpty(tts)){
                                                ttsText = defaultText;
                                            }
                                            ttsText = Utils.replaceTts(ttsText,"#SINGER#",currentAritist);
                                            ttsText = Utils.replaceTts(ttsText,"#SONG#",currentSong);
                                            if(!TextUtils.isEmpty(conditionId)) Utils.eventTrack(mContext, appName,scene,object,conditionId,condition,ttsText);
                                            Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {
                                                @Override
                                                public void onPlayStopped() {
                                                    FloatViewManager.getInstance(mContext).hide();
                                                    sendMonitorCommandToCB(0x10100,1);//??????????????????
                                                }
                                            });
                                        }
                                    });
                                }
                            }else if(!FloatViewManager.getInstance(mContext).isHide()){
                                //?????????????????????
                                MVWAgent.getInstance().stopMVWSession();
                                MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                                Utils.getMessageWithTtsSpeak(mContext, TtsConstant.CHANGBAC26CONDITION, (String) mContext.getText(R.string.changbaC26));
                                Utils.eventTrack(mContext, R.string.skill_changba, R.string.scene_changba_select, R.string.object_changba_select,TtsConstant.CHANGBAC26CONDITION,R.string.condition_changbaC26);
                            }
                        }else if(count == 0){
                            Log.d(TAG,"????????????????????????");
//                            int checkTimes = (int)msg.obj;
//                            Log.d(TAG,"checkTimes = " + checkTimes);
//                            if(checkTimes == 1){
//                                Message checkMsgSecond = new Message();
//                                checkMsgSecond.what = CHECK_ACTIVITY;
//                                checkMsgSecond.obj = 2;//???????????????????????????
//                                mHandler.sendMessageDelayed(checkMsgSecond,800);
//                            }else {
//                                isHandling = false;

                                if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC24CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC24);
                                    object = R.string.object_changba_play_song_singer;
                                    condition = R.string.condition_changbaC24;
                                }else if(!TextUtils.isEmpty(currentAritist)){
                                    conditionId = TtsConstant.CHANGBAC16CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC16);
                                    object = R.string.object_changba_play_singer;
                                    condition = R.string.condition_changbaC16;
                                }else if(!TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC8CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC8);
                                    object = R.string.object_changba_play_song;
                                    condition = R.string.condition_changbaC8;
                                }

                                if(!FloatViewManager.getInstance(mContext).isHide()){
                                    getTtsMessageAndHide(true,conditionId,defaultText,currentAritist,currentSong,"",
                                            appName,scene,object,condition);
                                }
                            //}
                        }else if(count == -1){
                            Log.d(TAG,"??????????????????????????????");
                            boolean isLogin = SharedPreferencesUtils.getBoolean(mContext,ISLOGIN,false);
                            boolean isVip = SharedPreferencesUtils.getBoolean(mContext,ISVIP,false);
                            Log.d(TAG,"isLogin = " + isLogin + ",isVip = " + isVip);
                            if(!isLogin) {//?????????
                                if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC23CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC23);
                                    object = R.string.object_changba_play_song_singer;
                                    condition = R.string.condition_changbaC23;
                                }else if(!TextUtils.isEmpty(currentAritist)){
                                    conditionId = TtsConstant.CHANGBAC15CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC15);
                                    object = R.string.object_changba_play_singer;
                                    condition = R.string.condition_changbaC15;
                                }else if(!TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC7CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC7);
                                    object = R.string.object_changba_play_song;
                                    condition = R.string.condition_changbaC7;
                                }
                            }else if(isLogin && !isVip) {//???????????????????????????????????????????????????
                                if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC23_1CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC23_1);
                                    object = R.string.object_changba_play_song_singer;
                                    condition = R.string.condition_changbaC23_1;
                                }else if(!TextUtils.isEmpty(currentAritist)){
                                    conditionId = TtsConstant.CHANGBAC15_1CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC15_1);
                                    object = R.string.object_changba_play_singer;
                                    condition = R.string.condition_changbaC15_1;
                                }else if(!TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC7_1CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC7_1);
                                    object = R.string.object_changba_play_song;
                                    condition = R.string.condition_changbaC7_1;
                                }
                            }else if(isLogin && isVip) {//???????????????????????????????????????????????????
                                if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC23_2CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC23_2);
                                    object = R.string.object_changba_play_song_singer;
                                    condition = R.string.condition_changbaC23_2;
                                }else if(!TextUtils.isEmpty(currentAritist)){
                                    conditionId = TtsConstant.CHANGBAC15_2CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC15_2);
                                    object = R.string.object_changba_play_singer;
                                    condition = R.string.condition_changbaC15_2;
                                }else if(!TextUtils.isEmpty(currentSong)){
                                    conditionId = TtsConstant.CHANGBAC7_2CONDITION;
                                    defaultText = (String) mContext.getText(R.string.changbaC7_2);
                                    object = R.string.object_changba_play_song;
                                    condition = R.string.condition_changbaC7_2;
                                }
                            }
                            if(!FloatViewManager.getInstance(mContext).isHide()){
                                getTtsMessageAndHideClick(true,conditionId,defaultText,currentAritist,currentSong,"",0x10100,
                                        1,appName,scene,object,condition);
                            }
                        }else if(count == -2){
                            Log.d(TAG,"????????????");
                            if(!TextUtils.isEmpty(currentAritist) && !TextUtils.isEmpty(currentSong)){
                                conditionId = TtsConstant.CHANGBAC25CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC25);
                                object = R.string.object_changba_play_song_singer;
                                condition = R.string.condition_changbaC25;
                            }else if(!TextUtils.isEmpty(currentAritist)){
                                conditionId = TtsConstant.CHANGBAC17CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC17);
                                object = R.string.object_changba_play_singer;
                                condition = R.string.condition_changbaC17;
                            }else if(!TextUtils.isEmpty(currentSong)){
                                conditionId = TtsConstant.CHANGBAC9CONDITION;
                                defaultText = (String) mContext.getText(R.string.changbaC9);
                                object = R.string.object_changba_play_song;
                                condition = R.string.condition_changbaC9;
                            }
                            if(!FloatViewManager.getInstance(mContext).isHide()){
                                getTtsMessageAndHide(true,conditionId,defaultText,"","","",
                                        appName,scene,object,condition);
                            }
                        }else {
                            Log.d(TAG,"?????????-10000");
                        }

                        break;
                    case "which_page"://???????????????
                        isFirstPage = intent.getBooleanExtra("isFirstPage",false);//??????????????????
                        isLastPage = intent.getBooleanExtra("isLastPage",false);//?????????????????????
                        Log.d(TAG,"isFirstPage = " + isFirstPage + ",isLastPage = " + isLastPage);
                        break;
                    case "play_status"://????????????
                        playStatus = intent.getIntExtra("status",0);//1????????????2?????????
                        Log.d(TAG,"playStatus = " + playStatus);
                        break;
                    case "original_status"://??????????????????????????????
                        originalStatus = intent.getIntExtra("status",0);//1?????????2??????
                        Log.d(TAG,"originalStatus = " + originalStatus);
                        break;
                    case "login_state"://???????????? true ?????????
                        isInLogin = intent.getBooleanExtra("isLogin",false);
                        Log.d(TAG,"isInLogin = " + isInLogin);
                        SharedPreferencesUtils.saveBoolean(mContext,ISLOGIN,isInLogin);
                        break;
                    case "vip_state"://????????????  true ?????????
                        isInVip = intent.getBooleanExtra("isVip",false);
                        Log.d(TAG,"isInVip = " + isInVip);
                        SharedPreferencesUtils.saveBoolean(mContext,ISVIP,isInVip);
                        break;
                    case "skip_prelude"://??????????????????????????????
                        isSkipSuccess = intent.getBooleanExtra("isSkipSuccess",false);
                        Log.d(TAG,"isSkipSuccess = " + isSkipSuccess);
                        break;
                }
            }
        }
    };

    private BroadcastReceiver cbActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action: " + action);
            if ("os.com.oushang.autoclick.ACTION_CB_ACTIVITY_CHANGE".equals(action)) {
                String activity = intent.getStringExtra("cbActivity");//?????????activity
                Log.d(TAG, "activity = " + activity);

                //???????????????????????????????????????????????????????????????????????????????????????????????????????????????,????????????????????????bug1060496
                boolean isBackground = !SharedPreferencesUtils.getBoolean(mContext,ISFOREGROUND,false);
                boolean isSongList = SONGLISTDETAILACTIVITY.equals(initActivity) || PINYINCHOOSESONGACTIVITY.equals(initActivity);
                Log.d(TAG, "onReceive: isBackground = " + isBackground + ",isSongList = " + isSongList);
                currentAritist = SharedPreferencesUtils.getString(mContext,CBCURRENTARTIST,"");
                currentSong = SharedPreferencesUtils.getString(mContext,CBCURRENTSONG,"");
                //if(callSearch && !(isBackground && isSongList)){
                if(callSearch && (!TextUtils.isEmpty(currentAritist) || !TextUtils.isEmpty(currentSong))){
                    callSearch = false;
                    String artist = SharedPreferencesUtils.getString(mContext,CBCURRENTARTIST,"");
                    String song = SharedPreferencesUtils.getString(mContext,CBCURRENTSONG,"");
                    Log.d(TAG,"callSearch: artist = " + artist + ",song = " + song);

                    Map<String,String> map = new HashMap<>();
                    map.put(ARTIST,artist);
                    map.put(SONG,song);
                    Message msg = new Message();
                    msg.what = SEARCH;
                    msg.obj = map;
                    mHandler.sendMessageDelayed(msg,0);
                }
                switch (activity) {
                    case "com.changba.tv.module.singing.ui.activity.RecordActivity"://????????????
                        initActivity = RECORDACTIVITY;
                        isHandling = false;
                        if(!isCallIncoming())TspSceneManager.getInstance().switchSceneToChangba(mContext);//?????????????????????
                        if(!isHandling && !FloatViewManager.getInstance(mContext).isHide()){
                            isHandling = true;
                            mHandler.sendEmptyMessageDelayed(CHECK_ACTIVITY,2500);//??????????????????????????????????????????
                        }
                        break;
                    case "com.changba.tv.module.choosesong.ui.SongListDetailActivity"://??????????????????
                        initActivity = SONGLISTDETAILACTIVITY;
                        if(!isCallIncoming()){
                            //?????????????????????
                            MVWAgent.getInstance().stopMVWSession();
                            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                        }
                        Log.d(TAG,"isHandling = " + isHandling);
                        if(!isHandling && (!TextUtils.isEmpty(currentAritist) || !TextUtils.isEmpty(currentSong))){
                            isHandling = true;
                            //mHandler.sendEmptyMessageDelayed(CHECK_ACTIVITY,0);
                            count = INITCOUNT;
                            SharedPreferencesUtils.saveString(mContext,TRIGGER,"");
                            mHandler.sendEmptyMessageDelayed(REQUEST_SONG_NUMS,0);
                            mHandler.sendEmptyMessageDelayed(REQUEST_PAGE_NUMS,100);
                        }
                        break;
                    case "com.changba.tv.module.main.ui.MainActivity"://?????????
                        Log.d(TAG,"call search and isNeedSearch = " + isNeedSearch);
                        initActivity = MAINACTIVITY;
                        if(!isCallIncoming()){
                            //????????????????????????
                            MVWAgent.getInstance().stopMVWSession();
                            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
                        }
                        if(isNeedSearch){
                            isNeedSearch = false;
                            Map<String,String> map = new HashMap<>();
                            if(!TextUtils.isEmpty(currentAritist)){
                                map.put(ARTIST,currentAritist);
                            }
                            if(!TextUtils.isEmpty(currentSong)){
                                map.put(SONG,currentSong);
                            }
                            Message msg = new Message();
                            msg.what = SEARCH;
                            msg.obj = map;
                            mHandler.sendMessage(msg);
                        }
                        break;
                    case "com.changba.tv.login.WechatQrcodeLoginActivity"://??????????????????
                        initActivity = WECHATQRCODELOGINACTIVITY;
                        if(!isCallIncoming()){
                            //????????????????????????
                            MVWAgent.getInstance().stopMVWSession();
                            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
                        }
//                        isHandling = false;
//                        if(!isHandling && !FloatViewManager.getInstance(mContext).isHide()){
//                            isHandling = true;
//                            mHandler.sendEmptyMessageDelayed(CHECK_ACTIVITY,0);
//                        }
                        break;
                    case "com.changba.tv.module.account.ui.activity.SubscribeActivity"://????????????
                        initActivity = SUBSCRIBEACTIVITY;
                        if(!isCallIncoming()){
                            //????????????????????????
                            MVWAgent.getInstance().stopMVWSession();
                            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
                        }
//                        isHandling = false;
//                        if(!isHandling && !FloatViewManager.getInstance(mContext).isHide()){
//                            isHandling = true;
//                            mHandler.sendEmptyMessageDelayed(CHECK_ACTIVITY,0);
//                        }
                        break;
                    case "com.changba.tv.module.choosesong.ui.activity.PinYinChooseSongActivity":
                        initActivity = PINYINCHOOSESONGACTIVITY;
                        if(!isCallIncoming()){
                            //?????????????????????
                            MVWAgent.getInstance().stopMVWSession();
                            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
                        }
                        break;
                    default:
                        if(!isCallIncoming()){
                            //????????????????????????
                            MVWAgent.getInstance().stopMVWSession();
                            MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_NAVI);
                        }
                        break;
                }
            }
        }
    };

    private BroadcastReceiver cbSongNameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action: " + action);
            if ("com.oushang.cb.SONGNAME".equals(action)) {
                String cSong = intent.getStringExtra("cur_Songname");
                String cArtist = intent.getStringExtra("cur_Artist");
                String nSong = intent.getStringExtra("next_Songname");
                String nArtist = intent.getStringExtra("next_Artist");
                Log.d(TAG,"cSong = " + cSong + ",cArtist = " + cArtist + ",nSong = " + nSong + ",nArtist = " + nArtist);
                //????????????????????????nSong = ??????????????? nArtist = ???????????????
                if("???????????????".equals(nSong) && "???????????????".equals(nArtist)){
                    isSongListNull = true;
                }else {
                    isSongListNull = false;
                }
                Log.d(TAG,"receive isSongListNull = " + isSongListNull);
            }
        }
    };

    @Override
    public void stkAction(StkResultEntity stkResultEntity) {

    }

    @Override
    public void mvwAction(MvwLParamEntity mvwLParamEntity) {
        switch (mvwLParamEntity.nMvwScene) {
            case MvwSession.ISS_MVW_SCENE_GLOBAL:
                LogUtils.i(TAG, "TSP_SCENE_GLOBAL");
                break;
            case MvwSession.ISS_MVW_SCENE_SELECT:
                selectItem(mvwLParamEntity);
                break;
            case MvwSession.ISS_MVW_SCENE_CONFIRM:
                confirm(mvwLParamEntity);
                break;
        }
    }

    public void onDoAction(String text){
        Message msg = new Message();
        if("????????????".equals(text)){
            mHandler.sendEmptyMessageDelayed(REQUEST_PAUSE,0);
        }else if("????????????".equals(text)){
            mHandler.sendEmptyMessageDelayed(REQUEST_PLAY,0);
        }else if("?????????".equals(text)){
            mHandler.sendEmptyMessageDelayed(NEXT_SONG,0);
        }else if("?????????".equals(text)){
            msg.what = LAST_PAGE;
            mHandler.sendMessage(msg);
        }else if("?????????".equals(text)){
            msg.what = NEXT_PAGE;
            mHandler.sendMessage(msg);
        }else if("?????????".equals(text) || "?????????".equals(text)){
            Map<String,Integer> map = new HashMap<>();
            map.put("index",1);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if("?????????".equals(text) || "?????????".equals(text)){
            Map<String,Integer> map = new HashMap<>();
            map.put("index",2);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if("?????????".equals(text) || "?????????".equals(text)){
            Map<String,Integer> map = new HashMap<>();
            map.put("index",3);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if("?????????".equals(text) || "?????????".equals(text)){
            Map<String,Integer> map = new HashMap<>();
            map.put("index",4);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if("?????????".equals(text) || "?????????".equals(text)){
            Map<String,Integer> map = new HashMap<>();
            map.put("index",5);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else {
            Map<String,Integer> map = new HashMap<>();
            map.put("index",6);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }
    }

    private void selectItem(MvwLParamEntity mvwLParamEntity) {
        MessageListEvent messageEvent = new MessageListEvent();
        Message msg = new Message();
        Map<String,Integer> map = new HashMap<>();
        if (mvwLParamEntity.nMvwId < 6) {
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            map.put("index",mvwLParamEntity.nMvwId + 1);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        } else if (mvwLParamEntity.nMvwId == 7 || mvwLParamEntity.nMvwId == 9 ) {//????????? ?????????
            messageEvent.eventType = MessageListEvent.ListEventType.LAST_PAGE;
            msg.what = LAST_PAGE;
            mHandler.sendMessage(msg);
        } else if (mvwLParamEntity.nMvwId == 6 || mvwLParamEntity.nMvwId == 8 ) {//???????????? ?????????
            messageEvent.eventType = MessageListEvent.ListEventType.NEXT_PAGE;
            msg.what = NEXT_PAGE;
            mHandler.sendMessage(msg);
        }else if (mvwLParamEntity.nMvwId == 21) {//?????????
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            map.put("index",1);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if (mvwLParamEntity.nMvwId == 22) {//?????????
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            map.put("index",2);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if (mvwLParamEntity.nMvwId == 23) {//?????????
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            map.put("index",3);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if (mvwLParamEntity.nMvwId == 24) {//?????????
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            map.put("index",4);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else if (mvwLParamEntity.nMvwId == 25) {//?????????
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = mvwLParamEntity.nMvwId;
            map.put("index",5);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        } else if (mvwLParamEntity.nMvwId >= 26 || (mvwLParamEntity.nMvwId >= 15 && mvwLParamEntity.nMvwId <= 17 )) {//????????? ???7,8,9???
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            map.put("index",6);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }else { //?????????
            messageEvent.eventType = MessageListEvent.ListEventType.NEXT_PAGE;
            msg.what = NEXT_PAGE;
            mHandler.sendMessage(msg);
        }
        Log.d(TAG,"start next or last page...");
    }

    private void confirm(MvwLParamEntity mvwLParamEntity) {
        switch (mvwLParamEntity.nMvwId) {
            case 0:
                LogUtils.i(TAG, "??????");
                MessageListEvent messageEvent = new MessageListEvent();
                messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
                messageEvent.index = mvwLParamEntity.nMvwId;
                Message msg = new Message();
                Map<String,Integer> map = new HashMap<>();
                map.put("index",mvwLParamEntity.nMvwId + 1);
                msg.what = SELECT_WHICH_ONE;
                msg.obj = map;
                mHandler.sendMessage(msg);
                break;
            case 1:
                //??????
                LogUtils.i(TAG, "??????");
                Utils.exitVoiceAssistant();
                break;
        }
    }

    private void getTtsMessageAndHideClick(boolean isNeedHide,String conditionId, String defaultTts,String artist, String song,String maxNum,int action,
                                           int param,int appName,int scene,int object,int condition) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = defaultTts;
                }

                defaultText = Utils.replaceTts(defaultText,"#SINGER#",artist);
                defaultText = Utils.replaceTts(defaultText,"#SONG#",song);
                defaultText = Utils.replaceTts(defaultText,"#MAXNUM#",maxNum);

                if(!TextUtils.isEmpty(conditionId)) Utils.eventTrack(mContext, appName,scene,object,conditionId,condition,defaultText);
                Utils.startTTS(defaultText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (isNeedHide && !FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                        sendMonitorCommandToCB(action,param);//??????????????????????????????????????????????????????
                    }
                });
            }
        });
    }

    private void getTtsMessageAndHide(boolean isNeedHide,String conditionId, String defaultTts,String artist, String song,String maxNum,int appName, int scene, int object, int condition) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = defaultTts;
                }

                defaultText = Utils.replaceTts(defaultText,"#SINGER#",artist);
                defaultText = Utils.replaceTts(defaultText,"#SONG#",song);
                defaultText = Utils.replaceTts(defaultText,"#MAXNUM#",maxNum);

                if(!TextUtils.isEmpty(conditionId))Utils.eventTrack(mContext, appName,scene,object,conditionId,condition,defaultText);
                Utils.startTTS(defaultText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (isNeedHide && !FloatViewManager.getInstance(mContext).isHide()) {
                            FloatViewManager.getInstance(mContext).hide();
                        }
                    }
                });
            }
        });
    }

    private void getTtsMessage(boolean isNeedExit,String conditionId, String defaultTts,String artist, String song) {
        Utils.getMessageWithoutTtsSpeak(mContext, conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String defaultText = tts;
                if (TextUtils.isEmpty(defaultText)) {
                    defaultText = defaultTts;
                }

                StartTTS(isNeedExit,conditionId,defaultText,artist,song);
//                if(!FloatViewManager.getInstance(mContext).isHide()){
//                    StartTTS(isNeedExit,conditionId,defaultText,artist,song);
//                }else {
//                    openCBAction(isNeedExit,conditionId,tts,artist,song);
//                }
            }
        });
    }

    private void StartTTS(boolean isNeedExit,String conditionId,String tts,String singer,String song){
        if(conditionId == TtsConstant.CHANGBAC1CONDITION || conditionId == TtsConstant.CHANGBAC1_1CONDITION ||
                conditionId == TtsConstant.CHANGBAC1_2CONDITION || conditionId == TtsConstant.CHANGBAC1_3CONDITION) {
            //????????????????????????????????????????????????
            MultiInterfaceUtils.getInstance(mContext).uploadChangbaStatusData(true);
        }
        Utils.startTTSOnly(tts, new TTSController.OnTtsStoppedListener() {
            @Override
            public void onPlayStopped() {
                boolean isChangBaForeground = AppControlManager.getInstance(mContext).isAppointForeground(AppConstant.PACKAGE_NAME_CHANGBA);
                if(!isChangBaForeground){
                    Log.d(TAG, "onPlayStopped: openCB again");
                    mHandler.sendEmptyMessageDelayed(RESEARCH,0);
                }
                if(isNeedExit){
                    Utils.exitVoiceAssistant();
                }else {
                    if(conditionId == TtsConstant.CHANGBAC1CONDITION || conditionId == TtsConstant.CHANGBAC1_1CONDITION ||
                            conditionId == TtsConstant.CHANGBAC1_2CONDITION || conditionId == TtsConstant.CHANGBAC1_3CONDITION){
                        SRAgent.getInstance().startSRSession();
                        EventBusUtils.sendTalkMessage(MessageEvent.ACTION_ANIM);
                        EventBusUtils.sendAnimMessage(ArtEvent.AnimType.ANIM_LISTENING, null);
                    }
                }
            }
        });
    }

    private void openCBAction(boolean isNeedExit,String conditionId,String tts,String singer,String song){
        //???????????????MainActivity????????????????????????
        callSearch = true;
        if(!AppControlManager.getInstance(mContext).isAppAlive(mContext,AppConstant.PACKAGE_NAME_CHANGBA)){
            Log.d(TAG,"launch changba...");
            //startApp(CHANGBANAME);
            openCB();
        }else if(!AppControlManager.getInstance(mContext).isAppointForeground(AppConstant.PACKAGE_NAME_CHANGBA)){
            Log.d(TAG,"set changba top...");
            callSearch = false;
            AppControlManager.getInstance(mContext).setTopApp(mContext,AppConstant.PACKAGE_NAME_CHANGBA);
        }

    }

    //?????????????????????????????????
    public void sendMonitorCommandToCB(int command,int param){
        //adb shell am broadcast -a os.com.oushang.autoclick.ACTION_CONTROL_CB  --ei command 0x10090
        Intent i = new Intent();
        i.setAction("os.com.oushang.autoclick.ACTION_CONTROL_CB");
        i.putExtra("command",command);
        i.putExtra("index",param);
        mContext.sendBroadcast(i);
        Log.d(TAG,"sendMonitorCommandToCB: " + command + ",param = " + param);
    }

    //?????????????????????????????????
    public void sendOriginalCommandToCB(String Command,String paramIntKey,int paramIntValue,String paramBooleanKey,boolean paramBooleanValue){
        //adb shell am broadcast -n com.changba.sd/com.changba.tv.order.AudioOrderReceiver  -e Command Pause
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.changba.sd","com.changba.tv.order.AudioOrderReceiver"));
        i.putExtra("Command",Command);
        if(null != paramIntKey){
            i.putExtra(paramIntKey,paramIntValue);
        }
        if(null != paramBooleanKey){
            i.putExtra(paramBooleanKey,paramBooleanValue);
        }
        mContext.sendBroadcast(i);
        Log.d(TAG,"sendOriginalCommandToCB: " + Command + ",paramIntKey: " + paramIntKey + ",paramIntValue = " + paramIntValue +
               "paramBooleanKey: " + paramBooleanKey + ",paramBooleanValue = " + paramBooleanValue);
    }

    public void handleNoWakeupWords(int object){
        //?????????????????????????????????????????????????????? 1:?????????,0:?????????
        String playStatusPro = Utils.getProperty("vendor.audio.changba", "0");
        Log.d(TAG, "playStatusPro:" + playStatusPro);
        if(object == R.string.scene_stop){//??????
            //?????????????????????????????????????????????????????????
            if(FloatViewManager.getInstance(mContext).isHide()){
                //ChangbaController.getInstance(mContext).sendMonitorCommandToCB(0x10121,0);
                //playStatus = "1".equals(playStatusPro) ? 1:2;
                SharedPreferencesUtils.saveString(mContext,CBPLAYSTATUS,playStatusPro);
            }
            mHandler.sendEmptyMessageDelayed(REQUEST_PAUSE,0);
        }else if(object == R.string.scene_start){//??????
            //?????????????????????????????????????????????????????????
            if(FloatViewManager.getInstance(mContext).isHide()){
                //ChangbaController.getInstance(mContext).sendMonitorCommandToCB(0x10121,0);
                //playStatus = "1".equals(playStatusPro) ? 1:2;
                SharedPreferencesUtils.saveString(mContext,CBPLAYSTATUS,playStatusPro);
            }
            mHandler.sendEmptyMessageDelayed(REQUEST_PLAY,0);
        }else if(object == R.string.scene_next){//?????????
            //??????????????????????????????
            ChangbaController.getInstance(mContext).sendMonitorCommandToCB(0x10121,0);
            mHandler.sendEmptyMessageDelayed(NEXT_SONG,0);
        }else if(object == R.string.scene_pre){//?????????
            doExceptionAction();
        }
    }

    public void handleMvwWords(String word){
        if("????????????".equals(word) || "????????????".equals(word) || "????????????".equals(word)){
            //ChangbaController.getInstance(mContext).sendMonitorCommandToCB(0x10122,0);
            mHandler.sendEmptyMessageDelayed(OPEN_ORIGINAL,250);
        }else  if("????????????".equals(word) || "????????????".equals(word) || "????????????".equals(word)){
            //ChangbaController.getInstance(mContext).sendMonitorCommandToCB(0x10122,0);
            mHandler.sendEmptyMessageDelayed(OPEN_ACCOMPANY,250);
        }else  if("?????????".equals(word) || "??????".equals(word)){
            mHandler.sendEmptyMessageDelayed(NEXT_SONG,0);
        }else if("????????????".equals(word)){
            ChangbaController.getInstance(mContext).sendMonitorCommandToCB(0x10130,0);
            mHandler.sendEmptyMessageDelayed(SKIP_HEAD,250);
        }else if("??????".equals(word)){
            MessageListEvent messageEvent = new MessageListEvent();
            messageEvent.eventType = MessageListEvent.ListEventType.SELECT_WHICH_ONE;
            messageEvent.index = 0;
            Message msg = new Message();
            Map<String,Integer> map = new HashMap<>();
            map.put("index",1);
            msg.what = SELECT_WHICH_ONE;
            msg.obj = map;
            mHandler.sendMessage(msg);
        }
    }

    public void openCB(){
        Intent i = new Intent();
        i.setComponent(new ComponentName(AppConstant.PACKAGE_NAME_CHANGBA,MAINACTIVITY));
        int launchFlags = Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setFlags(launchFlags);
        mContext.startActivity(i);
    }

    private boolean isCallIncoming(){
        boolean isCallIncoming = BluePhoneManager.getInstance(mContext).getCallStatus() == CallContact.CALL_STATE_INCOMING;
        Log.d(TAG,"isCallIncoming = " + isCallIncoming);
        return isCallIncoming;
    }

    private void showAssistant(){
        Intent broad = new Intent(AppConstant.ACTION_SHOW_ASSISTANT);
        broad.putExtra(AppConstant.EXTRA_SHOW_TYPE,AppConstant.SHOW_BY_OTHER);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broad);
    }

    public void handleOneShot(IntentEntity intentEntity){
        showAssistant();

        Message msg = new Message();
        Map map = new HashMap();
        map.put("intentEntity",intentEntity);

        msg.what = MSG_SHOW_WORD;
        msg.obj = map;
        mHandler.sendMessageDelayed(msg,600);
    }

    private void doExceptionAction(){
        String defaultTts =  mContext.getString(R.string.main_c14_1);
        Utils.getMessageWithoutTtsSpeak(mContext, TtsConstant.MAINC14_1CONDITION, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                //tts??????,????????????tts??????,??????tts?????????
                if (TextUtils.isEmpty(tts)){
                    ttsText = defaultTts;
                }
                String username = Settings.System.getString(mContext.getContentResolver(),"aware");
                ttsText = Utils.replaceTts(ttsText, "#VOICENAME#", username);
                //TTS??????
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        Utils.exitVoiceAssistant();
                    }
                });

            }
        });
    }

    public void setInvalideType(){
        Log.d(TAG, "setInvalideType() called");
        isStartSelect = true;
        SharedPreferencesUtils.saveString(mContext,CBCURRENTARTIST,"");
        SharedPreferencesUtils.saveString(mContext,CBCURRENTSONG,"");
        callSearch = false;
        isC27Speak = false;
        isC29Speak = false;
    }
}

