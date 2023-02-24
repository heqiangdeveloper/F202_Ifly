package com.chinatsp.ifly.api.constantApi;

import android.os.Environment;

import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.voice.platformadapter.controller.PriorityControler;

import java.nio.file.FileAlreadyExistsException;
import java.util.List;

public class AppConstant {
    public static final int TYPE_FRAGMENT_SEARCH_LIST_CONTACT = 0;
    public static final int TYPE_FRAGMENT_SEARCH_LIST_POI = 1;
    public static final int TYPE_FRAGMENT_SEARCH_LIST_PLANE = 2;
    public static final int TYPE_FRAGMENT_SEARCH_LIST_TRAIN = 3;
    public static final int TYPE_FRAGMENT_SEARCH_LIST_MXPOI = 4; //沿途搜索
    public static final int TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI = 5; //特殊导航（回家/去公司）
    public static final int MAX_FRAGMENT_SEARCH_LIST_TYPE = 5;
    public static final int TYPE_FRAGMENT_SEARCH_LIST_CHEXIN = -1;

    public static final int TYPE_FRAGMENT_STOCK = 6;
    public static final int TYPE_FRAGMENT_WEATHER = 7;

    public static final int MAX_PER_PAGE_4 = 4;
    public static final int MAX_PER_PAGE_3 = 3;

    public static final int VOICE_SETTINGS_ID = 1;
    public static final int VOICE_HELPER_ID = 2;
    public static final int VOICE_SUBSETTINGS_ANSWER_ID = 3;
    public static final int VOICE_SUBSETTINGS_AWARE_ID = 4;
    public static final int VOICE_SUBSETTINGS_ACTOR_ID = 5;
    public static final int VOICE_RECOMMEND_ID =6;
    public static final int VOICE_COMMAND_ID =7;
    public static final int VOICE_DETAIL_SR =8;
    public static final int VOICE_DETAIL_MVW =9;
    public static final int VOICE_SUBSETTINGS_LOG_ID = 10;

    public static final String PREFERENCE_NAME = "com.chinatsp.ifly_preferences";
    public static final String KEY_CURRENT_ACTOR = "current_actor";
    public static final String KEY_CURRENT_NAME_1 = "current_name_1";
    public static final String KEY_CURRENT_NAME_2 = "current_name_2";
    public static final String KEY_CURRENT_NAME_3 = "current_name_3";
    public static final String KEY_CURRENT_ANSWER = "current_answer";
    public static final String KEY_SWITCH_ANSWER = "switch_answer";
    public static final String KEY_VOICE_GUIDE = "voice_guide";
    public static final String KEY_VOICE_BROADCAST = "voice_broadcast";
    public static final String KEY_VOICE_POSITION = "voice_position";
    public static final String KEY_SPOT_TALK = "spot_talk";
    public static final String KEY_WHICH_NAME = "which_name";
    public static final String KEY_AUDIO_FOCUS_PKGNAME = "audio_focus";
    public static final String LAST_UPDATE_TTS_TIME = "lastUpdateTtsTime";
    public static final String KEY_NEED_RESTORE_MUTE = "need_restore_mute";
    public static final String KEY_MEDIA_VOLUME = "mediaVolume";
    public static final String KEY_SYSTEM_VOLUME = "systemVolume";
    public static final String KEY_PHONE_VOLUME = "phoneVolume";
    public static final String KEY_MAPU_VOLUME = "mapUVolume";
    public static final String KEY_RELIEF_SHOWN = "relief";
    public static final String KEY_LAMP_COLOR = "lamp_color";
    public static final String KEY_LAMP_LIGHT = "lamp_light";
    public static final String KEY_LAMP_SWITCH = "lamp_switch";
    //需求设计默认为开
    public static final boolean VALUE_VOICE_GUIDE = true;
    public static final boolean VALUE_VOICE_SPEAK = true;
    public static final boolean VALUE_SPOT_TALK = false;

    public static final String KEY_URL_SPEAKTTS = "mainC_Url_SpeakTTS";
    public static final String KEY_URL_GETTOKEN = "mainC_Url_GetToken";
    public static final String KEY_CLIENT_ID = "mainC_client_id";
    public static final String KEY_CLIENT_SECRET = "mainC_client_secret";
    public static final String KEY_LAST_AD_PUBLISHTIME = "lastAdPublishTime";
    public static final String KEY_NAVI_BROADCAST = "isNaviBroadcast";
    public static final String KEY_NAVI_BEFORE_VOICE = "isNaviBeforeVoice";

    public static final String DEFAULT_VALUE_CURRENT_ACTOR = "小欧-甜美自然";
    public static final String DEFAULT_VALUE_CURRENT_NAME_1 = "小欧";
    public static final String DEFAULT_VALUE_CURRENT_NAME_2 = "欧尚";
    public static final String DEFAULT_VALUE_CURRENT_ANSWER = "系统预设";

    public static final long ONE_MINUTE = 1000 * 60;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_WEEK = 7 * ONE_DAY;

    //记录上次播报的定速巡航id
    public static final String KEY_LASTBROADCCC = "lastBroadCCCId";

    //外界触发语音发出的广播(使用方控或触摸唤醒)
    public static final String ACTION_AWARE_VOICE_KEY = "com.coagent.intent.action.KEY_CHANGED";
    public static final String ACTION_AWARE_VOICE_TOUCH = "com.chinatsp.ifly.AWARE_VOICE";
    //监听音源切换的广播
    public static final String STATUS_BAR_PLAY_INFO_ACTION = "play_status_bar_play_info_ACTION";

    public static final String ACTION_HIDE_VOICE= "com.chinatsp.ifly.HIDE_VOICE";

    //唤醒识别时发出的广播
    public static final String ACTION_SHOW_ASSISTANT = "com.chinatsp.ifly.SHOW_ASSISTANT";
    public static final String EXTRA_SHOW_TYPE = "show_type";
    public static final String EXTRA_SHOW_TTS = "show_tts";
    public static final String SHOW_BY_OTHER= "show_by_ohter";
    public static final String SHOW_BY_GUIDE= "show_by_guide";
    public static final String SHOW_BY_REMOTE= "show_by_remote";

    //主动服务
    public static final String ACTION_INITIATIVE_SERVICE = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_INITIATIVE_START = "com.chinatsp.ifly.ACTION_INITIATIVE_START";
    public static final String ACTION_INITIATIVE_SHUTDOWN = "com.chinatsp.ifly.ACTION_INITIATIVE_SHUTDOWN";
    public static final String ACTION_INITIATIVE_MEIXING_WEATHER_ACTION = "mxnavi.action.weather.position";
    public final static String ACTION_POPUP_NEWS_DETAIL = "popup_news_detail";
    public final static String ACTION_RECEIVE_STYLE = "os_send2car_ui";//欧尚style


    public static final String ACTION_AUDIOSYNTHESIS = "AudioSynthesis";
    public static final String ACTION_FESTIVL = "ActiveFestival";
    public static final String ACTION_ACTIVESERVICE = "ActiveService";

    //美行包名
    public static final String PACKAGE_NAME_WECARNAVI = "com.tencent.wecarnavi";
    //电台包名
    public static final String PACKAGE_NAME_RADIO = "com.edog.car.oushangsdk";
    //电话包名
    public static final String PACKAGE_NAME_PHONE = "com.chinatsp.phone";
    //systemui包名
    public static final String PACKAGE_NAME_SYSTEMUI = "com.android.systemui";
    //音乐包名
    public static final String PACKAGE_NAME_MUSIC = "com.chinatsp.music";
    //车信包名
    public static final String PACKAGE_NAME_CHEXIN = "com.jidouauto.carletter";
    //HICAR包名
    public static final String PACKAGE_NAME_HICAR = "com.huawei.hicar.demoapp";

    public static final String PACKAGE_NAME_IFLY = "com.chinatsp.ifly";

    public static final String PACKAGE_NAME_VCAR = "tv.newtv.vcar";

    //唱吧包名
    public static final String PACKAGE_NAME_CHANGBA = "com.changba.sd";

    //控制右上角状态栏显示隐藏广播
    public static final String ACTION_SHOW_CUSTOM_STATUS_BAR = "com.android.intent.action.ACTION_SHOW_CUSTOM_STATUS_BAR";
    public static final String ACTION_HIDE_CUSTOM_STATUS_BAR = "com.android.intent.action.ACTION_HIDE_CUSTOM_STATUS_BAR";
    public static final String KEY_TTS_PROJECT_ID = "project_id";
    public static final String KEY_TTS_PROJECT_VERSION = "project_version";
    //add qlf 6.28
    public static final String KEY_TTS_CONTENT_VERSION = "content_version";
    //end
    public static final String ADVERTIS_STARTTIME = "advertis_startTime";

    //语音悬浮窗出现和消失广播
    public static final String ACTION_VOICE_AWARED = "com.chinatsp.intent.action_VOICE_AWARED";
    public static final String ACTION_VOICE_DISAPPEAR = "com.chinatsp.intent.action_VOICE_DISAPPEAR";
    //动作来源
    public static final int SOURCE_MWV = 0;
    public static final int SOURCE_SR = 1;

    //主动服务播报
    //开发环境
//    public static final String ACTIVE_SERVICE = "http://osiovdev.changan.com.cn/huService/ai/getAIData";
//    public static final String ACTIVE_SERVICE_TRACK_URL = "http://osiovdev.changan.com.cn/huService/ai/track";
    //正式环境
    public static final String ACTIVE_SERVICE = "http://osiov.changan.com.cn/huService/ai/getAIData";
    //测试环境
    public static final String ACTIVE_SERVICE_DEBUG = "http://osiovdev.changan.com.cn/huService/ai/getAIData";
    public static final String ACTIVE_SERVICE_TRACK_URL = "http://osiov.changan.com.cn/huService/ai/track";
    public static final String GetAdvertis_URL = "https://incall.changan.com.cn/huapi/api/hu/2.0/getStartPageData";

    public static final String ACTIVE_DestWeather = "com.chinatsp.ifly.action.ACTION_DEST_WEATHER";
    //开发环境
//    public static final String HU_TTS_DATA_URL = "http://osiovdev.changan.com.cn/huService/tts/getHuTtsData";
    //正式环境
//    public static final String HU_TTS_DATA_URL = "https://osiov.changan.com.cn/huService/tts/getHuTtsData";
    //ICA
    public static final String HU_TTS_DATA_URL = "https://osiov.changan.com.cn/huService/tts/getHuTtsData2";

    //test
    public static final String BACK_DATA_URL = "https://osiov.changan.com.cn/huService/tts/getHuVoiceAsssitContent";

    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED";

    //经纬度
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String CITY_NAME = "city";

    //新手引导
    public static final String PREFERENCE_NOVICE_GUIDE_KEY = "isFirstUse";
    public static final String PREFERENCE_NOVICE_GUIDE_TIMES_KEY = "identifyTimes";

    public static final String MY_TEST_BROADCAST_WAKEUP = "com.chinatsp.ifly.wakeup";
    public static final String MY_TEST_BROADCAST_OPEN_AC = "com.chinatsp.ifly.open.ac";
    public static final String MY_TEST_BROADCAST_MX_MAKETEAM = "com.chinatsp.ifly.mx.maketeam";
    public static final String SPEECH_IFLY_RESPONSE = "speech_ifly_response"; //语音返回的json字符串
    public static final String SPEECH_IFLY_PRIMITIVE = "speech_ifly_primitive";//语音原语

    //黑夜/白天模式
    public static final String SHOW_MODE = "show_mode";
    //发广播：禁止语音拉起空调控制APP
    public static final String VOICE_ACTION = "com.chinatsp.airconditoner.action";
    //触发“新手引导”的广播（仅供测试使用）
    public static final String ACTION_TEST_NOVICEGUIDE = "com.chinatsp.ifly.test.noviceguide";

    //车型配置
    public static String  DCT_LEV3 = "LEV3";
    public static String  DCT_LEV4 = "LEV4";
    public static String  DCT_LEV5 = "LEV5";

    public static final int  MODE_SLEEP= 1;
    public static final int  MODE_DRIVING= 2;

    public static final String URL_SPEAKTTS = "http://tts.txzing.com/OpenTts/ts";
    public static final String URL_GETTOKEN = "http://tts.txzing.com/OpenTts/oauth/2.0/token";
    public static final String CLIENT_ID = "f4c0be50c623aadce5f68a10d29afb46";
    public static final String CLIENT_SECRET = "9e89e482525e350d1c487c502eec0bb80085b27a";

    //同行者tts后端播报完之后延长的时间
    public static final int  TXZ_COMPELTED_0= 0;
    public static final int  TXZ_COMPELTED_150= 150;

    public static String hddRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String usbRoot = "/storage/udisk";

    //是否使用本地数据
    public static boolean DOWNLOAD_FINISEH = false;
    public static boolean  SPEAKTTSONCE_BTNC43 = false;
    public static boolean  SPEAKTTSONCE_BTNC44 = false;
    public static boolean  SPEAKTTSONCE_BTNC45 = false;
    public static boolean  SPEAKTTSONCE_BTNC46 = false;
    public static boolean  SPEAKTTSONCE_BTNC47 = false;
    public static boolean  SPEAKTTSONCE_BTNC48 = false;
    public static boolean  SPEAKTTSONCE_BTNC48_1 = false;
    public static boolean  SPEAKTTSONCE_BTNC49 = false;
    public static boolean  SPEAKTTSONCE_BTNC49_1 = false;
    public static boolean  SPEAKTTSONCE_BTNC50 = false;
    public static boolean  SPEAKTTSONCE_BTNC51 = false;
    public static boolean  SPEAKTTSONCE_BTNC52 = false;
    public static boolean  SPEAKTTSONCE_BTNC53 = false;
    public static boolean  SPEAKTTSONCE_BTNC54 = false;
    public static boolean  SPEAKTTSONCE_BTNC55 = false;
    public static boolean  SPEAKTTSONCE_BTNC57 = false;
    public static boolean  SPEAKTTSONCE_BTNC58 = false;
    public static boolean  SPEAKTTSONCE_BTNC59 = false;
    public static boolean  SPEAKTTSONCE_BTNC60 = false;
    public static boolean  isFirstUseCruise = true;
    public static boolean  isFirstAdjustCruiseSpeed = true;
    public static int  ccc10SpeakTimes = 0;
    public static boolean  isFirstPressBrake = true;

    public static String mCurrentPkg ;

    public static final int InOutWorkPriority = PriorityControler.PRIORITY_ONE;
    public static final int WelcomeUserPriority = PriorityControler.PRIORITY_TWO;
    public static final int WarnSaveSleepModePriority = PriorityControler.PRIORITY_THREE;
    public static final int KeyGuidePriority = PriorityControler.PRIORITY_THREE;
    public static final int SendToCarPriority = PriorityControler.PRIORITY_THREE;

    public static List<ContactEntity> mContactLists;

    public static boolean isfirst = false;
    public static boolean setMute = false;

    //配置字
    public static final String CCS_ENABLE = "persist.vendor.vehicle.CCS";//定速巡航
    public static final String IBCM_ENABLE = "persist.vendor.vehicle.IBCM";//新手引导及语音引导

    //按键引导的播报方式
    public static final int HIDE_BROADCAST = 1;//后台语音播报
    public static final int SHOW_BROADCAST = 2;//弹窗语音播报
    public static final int HIDE_GUIDE_COMMAND = 3;//后台语音引导（操作类）
    public static final int HIDE_GUIDE_ORDER = 4;//后台语音引导（指令类）
    public static final int SHOW_GUIDE_COMMAND = 5;//弹窗语音引导（操作类）
    public static final int SHOW_GUIDE_ORDER = 6;//弹窗语音引导（指令类）
    public static final int SHOW_BROADCAST_AND_SHOW_GUIDE_ORDER = 7;//弹窗语音播报（不限车速）+ 弹窗语音引导（指令类）

    //当前抽烟提醒是哪个条件下触发的
    public static final String SMOKECONDITION = "smokeCondition";

    //当前是否在拍照
    public static boolean isCallDVR = false;
}
