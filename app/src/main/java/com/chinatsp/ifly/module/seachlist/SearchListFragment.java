package com.chinatsp.ifly.module.seachlist;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chinatsp.ifly.AppManager;
import com.chinatsp.ifly.DatastatManager;
import com.chinatsp.ifly.FloatViewManager;
import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.R;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.api.constantApi.TtsConstant;
import com.chinatsp.ifly.base.BaseApplication;
import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.base.BaseFragment;
import com.chinatsp.ifly.base.BaseRecyclerViewAdapter;
import com.chinatsp.ifly.entity.CheXinEntity;
import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.entity.FlightEntity;
import com.chinatsp.ifly.entity.MXPoiEntity;
import com.chinatsp.ifly.entity.MessageEvent;
import com.chinatsp.ifly.entity.MessageListEvent;
import com.chinatsp.ifly.entity.MultiChoiceEvent;
import com.chinatsp.ifly.entity.PoiEntity;
import com.chinatsp.ifly.entity.TrainEntity;
import com.chinatsp.ifly.utils.ConstantsApp;
import com.chinatsp.ifly.utils.GsonUtil;
import com.chinatsp.ifly.utils.ItemAnimationUtils;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.TspSceneManager;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.view.ItemRotateAnimation;
import com.chinatsp.ifly.view.PageNumIndicator;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.TTSController;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.ICheXinController;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IContactController;
import com.chinatsp.ifly.voice.platformadapter.controllerInterface.IMapController;
import com.chinatsp.ifly.voice.platformadapter.entity.Semantic;
import com.chinatsp.ifly.voice.platformadapter.manager.GDSdkManager;
import com.chinatsp.ifly.voice.platformadapter.manager.MXSdkManager;
import com.chinatsp.ifly.voice.platformadapter.utils.MultiInterfaceUtils;
import com.chinatsp.ifly.voice.platformadapter.utils.TtsUtils;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.common.TspSceneAdapter;
import com.iflytek.adapter.controllerInterface.IController;
import com.iflytek.adapter.mvw.MVWAgent;
import com.iflytek.adapter.sr.SRAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import butterknife.BindView;
public class SearchListFragment extends BaseFragment implements SearchListContract.View, View.OnClickListener,
        PageNumIndicator.InnerOnClickListener {

    private static final String TAG = "xyj_SearchListFragment";

    @BindView(R.id.layout_travel_header)
    View travelHeader;
    @BindView(R.id.tv_travel_origin)
    TextView tvTravelOrigin;
    @BindView(R.id.iv_travel_tool)
    ImageView ivTravelTool;
    @BindView(R.id.tv_travel_dest)
    TextView tvTravelDest;
    @BindView(R.id.tv_travel_date)
    TextView tvTravelDate;
    @BindView(R.id.list_recycler_view)
    RecyclerView listRecyclerView;
    @BindView(R.id.pageNumIndicator)
    PageNumIndicator pageNumIndicator;

    private FullScreenActivity activity;
    private SearchListContract.Presenter presenter = new SearchListPresenter(this);
    private List<BaseEntity> mListData;
    private SearchListAdapter mAdapter;
    private int pageNum;
    private int mType;
    private String topic;
    private String semantic;
    private int maxCountPerPage = 4;
    List<List<BaseEntity>> allList = new ArrayList<>();

    private final static  String POI = "#POI#";
    private final static  String MAXNUM = "#MAXNUM#";
    private final static  String NUM ="#NUM#";
    private final static  String CONTACT ="#CONTACT#";


    private static final int MSG_SR_TIME_OUT = 0;
    private static final int MSG_LAST_PAGE = 1;
    private static final int MSG_NEXT_PAGE = 2;
    private static final int DELAY_SR_TIME = 8*1000;
    private static final int MSG_DELAY_EXIT = 1011;
    public static boolean isShown = false;
    private int mSrTimeOutCount = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String conditionId;
            switch (msg.what){
                case MSG_SR_TIME_OUT:
                    mSrTimeOutCount++;
                    Log.d(TAG, "handleMessage() called with: mSrTimeOutCount = [" + mSrTimeOutCount + "]");
                    if (mSrTimeOutCount == 1) {
                        //                sendEmptyMessageDelayed(MSG_SR_TIME_OUT,DELAY_SR_TIME);
                    } else {
                        mSrTimeOutCount = 0;
                        removeCallbacksAndMessages(MSG_SR_TIME_OUT);
                    }
                    SRAgent.getInstance().srTimeout();
                    break;
                case MSG_LAST_PAGE:
                    try {
                        mListData.clear();
                        mListData.addAll(allList.get(pageNum));
                        highlightSelect(-1);
                        mAdapter.notifyDataSetChanged();
                        pageNumIndicator.setPageNum(pageNum);
                        conditionId = getLastPage(false);
                        String mainMsg =Utils.replaceTts(getString(R.string.switch_page_ok), NUM, ""+(pageNum + 1));
                        if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                            Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC37CONDITION, R.string.condition_navi27,mainMsg,true);
                            Utils.eventTrack(activity,R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_previous_page, conditionId, R.string.condition_navi27,mainMsg);
                        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                            Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC37CONDITION, R.string.condition_phoneC21,mainMsg,true);
                            Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_3, conditionId, R.string.condition_phoneC21,mainMsg);
                        }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                            Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_previous_page,conditionId,R.string.condition_chexinC25,mainMsg);
                        }
                        getTtsMessage(conditionId, mainMsg,(pageNum+1)+"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_NEXT_PAGE:
                    try {
                        mListData.clear();
                        mListData.addAll(allList.get(pageNum));
                        highlightSelect(-1);
                        mAdapter.notifyDataSetChanged();
                        pageNumIndicator.setPageNum(pageNum);
                        if (allList.size() - 1 == pageNum) {
                             conditionId = getNextPage(1);

//                            getTtsMessage(conditionId, getString(R.string.reach_final_page));

                            String defaultTts = getString(R.string.reach_final_page);
                            Utils.getMessageWithoutTtsSpeak(getContext(), conditionId, new TtsUtils.OnConfirmInterface() {
                                @Override
                                public void onConfirm(String tts) {
                                    String ttsText = tts;
                                    if (TextUtils.isEmpty(tts)) {
                                        ttsText = defaultTts;
                                    }
                                    Utils.startTTSOnly(ttsText);

                                    if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                                        Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC38CONDITION, R.string.condition_navi29,ttsText,true);
                                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_next_page,conditionId,R.string.condition_navi29, ttsText);
                                    } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                                        Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC38CONDITION, R.string.condition_phoneC23,ttsText,true);
                                        Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_4, conditionId, R.string.condition_phoneC23, ttsText);
                                    }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                                        Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_next_page,conditionId,R.string.condition_chexinC27, ttsText);
                                    }
                                }
                            });
                        } else {
                             conditionId = getNextPage(0);
                            String mainMsg =Utils.replaceTts(getString(R.string.switch_page_ok), NUM, ""+(pageNum + 1));

//                            getTtsMessage(conditionId, mainMsg,(pageNum+1)+"");

                            String page = (pageNum+1)+"";
                            Utils.getMessageWithoutTtsSpeak(getContext(), conditionId, new TtsUtils.OnConfirmInterface() {
                                @Override
                                public void onConfirm(String tts) {
                                    String ttsText = tts;
                                    if (TextUtils.isEmpty(tts)) {
                                        ttsText = mainMsg;
                                    } else {
                                        ttsText = Utils.replaceTts(tts, NUM, page);
                                    }
                                    Utils.startTTSOnly(ttsText);

                                    if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                                        Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC38CONDITION, R.string.condition_navi28,ttsText,true);
                                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_next_page,conditionId,R.string.condition_navi28,ttsText);
                                    } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                                        Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC38CONDITION, R.string.condition_phoneC22,ttsText,true);
                                        Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_4, conditionId, R.string.condition_phoneC22,ttsText);
                                    }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                                        Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_next_page,conditionId,R.string.condition_chexinC26,ttsText);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_DELAY_EXIT:
                    if (!FloatViewManager.getInstance(BaseApplication.getInstance()).isHide()){
                        FloatViewManager.getInstance(BaseApplication.getInstance()).hide(FloatViewManager.TYPE_HIDE_PHONE);
                    }
                    break;
            }

        }
    };

    public static SearchListFragment newInstance(int type, String dataList, String semantic, String topic) {
        SearchListFragment fragment = new SearchListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(FullScreenActivity.TYPE, type);
        bundle.putString(FullScreenActivity.DATA_LIST_STR, dataList);
        bundle.putString(FullScreenActivity.SEMANTIC_STR, semantic);
        bundle.putString(FullScreenActivity.TOPIC, topic);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (FullScreenActivity) context;
        presenter.bindActivity(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.subscribe();
        EventBus.getDefault().register(this);
    }

    // 需要静态，重复调起界面时会多次创建实例
    static int currentScene = -1;
    @Override
    public void onResume() {
        super.onResume();

        // 保存当前场景
        int scene = TspSceneAdapter.getTspScene(getContext());
        // 如果当前已经是选择场景，则不保存
        if (scene != TspSceneAdapter.TSP_SCENE_SELECT) {
            currentScene = scene;
        }

        //todo 确保当前是在SELECT场景
        Log.d(TAG, "-----onResume------  " + currentScene + "    " + this);
        MVWAgent.getInstance().stopMVWSession();
        MVWAgent.getInstance().startMVWSession(TspSceneAdapter.TSP_SCENE_SELECT);
        isShown = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");

        if (mHandler.hasMessages(MSG_SR_TIME_OUT))
            mHandler.removeMessages(MSG_SR_TIME_OUT);
        SRAgent.getInstance().resetSrTimeCount();
        isShown = false;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateFloatView(MessageEvent messageEvent) {
        Log.d(TAG, "updateFloatView() called with: messageEvent = [" + messageEvent.eventType + "]");
        if (!isShown) return;
        if (messageEvent.eventType == MessageEvent.EventType.ENDSPEECH) {
            mHandler.removeMessages(MSG_SR_TIME_OUT);
            mHandler.sendEmptyMessageDelayed(MSG_SR_TIME_OUT, DELAY_SR_TIME);
        } else if (messageEvent.eventType == MessageEvent.EventType.RESTARTSPEECH)
            mHandler.removeMessages(MSG_SR_TIME_OUT);
        else if (messageEvent.eventType == MessageEvent.EventType.SPEECHING)
            mHandler.removeMessages(MSG_SR_TIME_OUT);

    }

    @Override
    protected void initData() {
        List<BaseEntity> listData = new ArrayList<>();
        String result = getArguments().getString(FullScreenActivity.DATA_LIST_STR);
        String semantic = getArguments().getString(FullScreenActivity.SEMANTIC_STR);
        Log.d(TAG, "--------------semantic-" + semantic);
        Log.d(TAG, "--------------result-" + result);
        if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
            if(semantic==null){
                listData.clear();
                listData.addAll(AppConstant.mContactLists);
            }else {
                //上传MoreContact状态
                MultiInterfaceUtils.getInstance(activity).uploadTelephoneMoreContactData(result, semantic);
                List<ContactEntity> contactList = GsonUtil.stringToList(result, ContactEntity.class);
                listData.clear();
                listData.addAll(contactList);
            }

        } else  if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN) {
            //上传MoreContact状态
            //  MultiInterfaceUtils.getInstance(activity).uploadTelephoneMoreContactData(result, semantic);
            List<CheXinEntity> chexinList = GsonUtil.stringToList(result, CheXinEntity.class);
            listData.clear();
            listData.addAll(chexinList);
        }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI
                || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI
                || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {

            //上传moreTarget状态
            MultiInterfaceUtils.getInstance(activity).uploadMapUMoreTargetData(result, semantic);
            if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI) {
                List<PoiEntity> poiList = GsonUtil.stringToList(result, PoiEntity.class);
                listData.clear();
                listData.addAll(poiList);
            } else {
                List<MXPoiEntity> mxpoiList = GsonUtil.stringToList(result, MXPoiEntity.class);
                listData.clear();
                listData.addAll(mxpoiList);
            }

        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_PLANE) {
//            listData = presenter.getSearchListPlaneData();
            if (!TextUtils.isEmpty(semantic)) {
                Semantic flightSemantic = GsonUtil.stringToObject(semantic, Semantic.class);
                if (flightSemantic != null) {
                    if (flightSemantic.slots != null) {
                        if (flightSemantic.slots.endLoc != null) {
                            String arrivalCity = flightSemantic.slots.endLoc.cityAddr;
                            tvTravelDest.setText(arrivalCity);
                        }
                        if (flightSemantic.slots.startLoc != null) {
                            String startCity = flightSemantic.slots.startLoc.cityAddr;
                            tvTravelOrigin.setText(startCity);
                        }
                        if (flightSemantic.slots.startDate != null) {
                            String date = flightSemantic.slots.startDate.date;
                            String value = getWeek(date);
                            tvTravelDate.setText(value);
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(result)) {
                List<FlightEntity> planeList = GsonUtil.stringToList(result, FlightEntity.class);
                listData.clear();
                listData.addAll(planeList);
            }
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_TRAIN) {
            if (!TextUtils.isEmpty(semantic)) {
                Semantic trainSemantic = GsonUtil.stringToObject(semantic, Semantic.class);
                if (trainSemantic != null) {
                    if (trainSemantic.slots != null) {
                        if (trainSemantic.slots.endLoc != null) {
                            String arrivalCity = trainSemantic.slots.endLoc.cityAddr;
                            tvTravelDest.setText(arrivalCity);
                        }
                        if (trainSemantic.slots.startLoc != null) {
                            String startCity = trainSemantic.slots.startLoc.cityAddr;
                            tvTravelOrigin.setText(startCity);
                        }
                        if (trainSemantic.slots.startDate != null) {
                            String date = trainSemantic.slots.startDate.date;
                            String value = getWeek(date);
                            tvTravelDate.setText(value);
                        }
                    }
                }
            }
//            String message = getArguments().getString(FullScreenActivity.ANSWER_STR);
//            startTTS(message);
            if (!TextUtils.isEmpty(result)) {
                List<TrainEntity> planeList = GsonUtil.stringToList(result, TrainEntity.class);
                listData.clear();
                listData.addAll(planeList);
            }
        }
        mListData.clear();
        mListData.addAll(listData);
        mAdapter.notifyDataSetChanged();
        ItemAnimationUtils.startFallDownAnimation(activity,listRecyclerView);
        //填充每页的数据并更新指示器
        fillUpPageDatas();
    }

    //组装日期
    public String getWeek(String date) {
        if (TextUtils.isEmpty(date)) return "";
        String value = "";
        String[] tmpValue = date.split("-");
        if (tmpValue.length > 2) {
            value = tmpValue[1] + "月" + tmpValue[2] + "日";
        } else if (tmpValue.length == 2) {
            value = tmpValue[1] + "日";
        } else {
            if (tmpValue.length > 0) {
                value = tmpValue[0] + "日";
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");//定义日期格式

        Date date1 = null;
        try {
            date1 = format.parse(date);//将字符串转换为日期
        } catch (ParseException e) {
            System.out.println("输入的日期格式不合理！");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date1);
        value = value + " " + week;
        return value;
    }

    @Override
    protected void initListener() {
        pageNumIndicator.setInnerOnClickListener(this);
    }

    @Override
    protected void initView(View view) {
        mType = getArguments().getInt(FullScreenActivity.TYPE);
        topic = getArguments().getString(FullScreenActivity.TOPIC);
        semantic=getArguments().getString(FullScreenActivity.SEMANTIC_STR);
        listRecyclerView.setNestedScrollingEnabled(false);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        mListData = new ArrayList<>();
        mAdapter = new SearchListAdapter(mType, mListData);
        listRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    presenter.onViewClick(allList.get(pageNum).get(position), mType);
                }catch (Exception e){}

            }
        });
        mAdapter.setOnItemOpeButtonClickListener(new BaseRecyclerViewAdapter.OnItemChildViewClickListener() {
            @Override
            public void onItemChildViewClick(AdapterView<?> adapterView, View view, int position, long id) {
                int allPos = position + pageNum * maxCountPerPage;
                presenter.onViewChildClick(mListData.get(allPos), mType);
                mAdapter.selectedItem(allPos);
            }
        });

        if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_PLANE || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_TRAIN) {
            travelHeader.setVisibility(View.VISIBLE);
            ivTravelTool.setImageResource(mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_PLANE ? R.drawable.ic_plane : R.drawable.ic_train);
            ((RelativeLayout.LayoutParams) listRecyclerView.getLayoutParams()).topMargin = 262;
            maxCountPerPage = 3;
        } else {
            travelHeader.setVisibility(View.GONE);
            maxCountPerPage = 4;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_search_list;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Activity current =  AppManager.getAppManager().currentActivity();
        Activity activity = getActivity();
        Log.d(TAG, "-----onDestroy------ "  + activity);
        Log.d(TAG, "-----onDestroy------ "  + current);
        presenter.unSubscribe();
        allList = null;
        mListData.clear();
        EventBus.getDefault().unregister(this);

        if (activity == current) {
            // 如果栈顶还是选择列表,说明又发起了选择，不执行还原场景。避免生命周期混乱导致场景错误
        } else {
            //todo
            resetMVWsession();
        }


        mHandler.removeCallbacksAndMessages(null);
    }

    private void resetMVWsession() {
        MultiInterfaceUtils.getInstance(activity).uploadCmdDefaultData();
        //退出选择场景
        Log.d(TAG, "-----resetMVWsession------ "  + currentScene);
        TspSceneManager.getInstance().resetScrene(activity,currentScene);
        currentScene = -1;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onLast() {
        if (pageNum > 0) {
            pageNum--;
            ItemAnimationUtils.startRotateAnimation(true,listRecyclerView);
            mHandler.sendEmptyMessageDelayed(MSG_LAST_PAGE,ItemRotateAnimation.durationMillis+30);
        } else {
            highlightSelect(-1);
            mAdapter.notifyDataSetChanged();
            String conditionId = getLastPage(true);
            String defaultString = getString(R.string.pre_page_fail);
            if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC37CONDITION, R.string.condition_navi26,defaultString,true);
                Utils.eventTrack(activity,R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_previous_page, conditionId, R.string.condition_navi26, defaultString);
            } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC37CONDITION, R.string.condition_phoneC20,defaultString,true);
                Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_3, conditionId, R.string.condition_phoneC20, defaultString);
            }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_previous_page,conditionId,R.string.condition_chexinC24, defaultString);
            }
            Utils.getMessageWithTtsSpeakOnly(false,getContext(), conditionId, defaultString);
        }
    }

    private void getTtsMessage(String conditionId, final String defaultTts,final String page) {
        Utils.getMessageWithoutTtsSpeak(getContext(), conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                } else {
                    ttsText = Utils.replaceTts(tts, NUM, page);
                }
                Utils.startTTSOnly(ttsText);
            }
        });
    }

    private void getTtsMessage(String conditionId, final String defaultTts) {
        Utils.getMessageWithoutTtsSpeak(getContext(), conditionId, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }
                Utils.startTTSOnly(ttsText);
            }
        });
    }

    /*
     * 获取用户说上一页时的coditionId
     * isFirstPage当前是否是第一页;
     */
    private String getLastPage(boolean isFirstPage) {
        String conditionId = "";
        if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
            //联系人
            if (isFirstPage) {
                conditionId = TtsConstant.PHONEC20CONDITION;
            } else {
                conditionId = TtsConstant.PHONEC21CONDITION;
            }
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
            if (isFirstPage) {
                conditionId = TtsConstant.NAVIC26CONDITION;
            } else {
                conditionId = TtsConstant.NAVIC27CONDITION;
            }
        } else  if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN) {
            if (isFirstPage) {
                conditionId = TtsConstant.CHEXINC24CONDITION;
            } else {
                conditionId = TtsConstant.CHEXINC25CONDITION;
            }
        }
        return conditionId;
    }

    /*
     * 获取用户说最后一页时的coditionId
     * isFinalPage当前是否最后一页
     */
    private String getFinalPage(Boolean isFinalPage) {
        String conditionId = "";
        if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
            //联系人
            if (isFinalPage) {
                conditionId = TtsConstant.PHONEC26CONDITION;
            } else {
                conditionId = TtsConstant.PHONEC25CONDITION;
            }
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
            if (isFinalPage) {
                conditionId = TtsConstant.NAVIC32CONDITION;
            } else {
                conditionId = TtsConstant.NAVIC31CONDITION;
            }
        }else  if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN) {
            if (isFinalPage) {
                conditionId = TtsConstant.CHEXINC30CONDITION;
            } else {
                conditionId = TtsConstant.CHEXINC29CONDITION;
            }
        }
        return conditionId;
    }

    /*
     * 获取用户说第X页时的coditionId
     * isOutOfBoundary: true:超出范围,false:未超出范围
     */
    private String getChoosePage(Boolean isOutOfBoundary) {
        String conditionId = "";
        if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
            //联系人
            if (isOutOfBoundary) {
                conditionId = TtsConstant.PHONEC28CONDITION;
            } else {
                conditionId = TtsConstant.PHONEC27CONDITION;
            }
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
            if (isOutOfBoundary) {
                conditionId = TtsConstant.NAVIC34CONDITION;
            } else {
                conditionId = TtsConstant.NAVIC33CONDITION;
            }
        }else  if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN) {
            if (isOutOfBoundary) {
                conditionId = TtsConstant.CHEXINC32CONDITION;
            } else {
                conditionId = TtsConstant.CHEXINC31CONDITION;
            }
        }
        return conditionId;
    }


    /*
     * 获取用户说下一页时的coditionId
     * page: 0表示默认,1：当前为倒数第二页,2：当前为最后一页
     */
    private String getNextPage(int page) {
        String conditionId = "";
        if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
            //联系人
            if (page == 0) {
                conditionId = TtsConstant.PHONEC22CONDITION;
            } else if (page == 1) {
                conditionId = TtsConstant.PHONEC23CONDITION;
            } else if (page == 2) {
                conditionId = TtsConstant.PHONEC24CONDITION;
            }
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
            if (page == 0) {
                conditionId = TtsConstant.NAVIC28CONDITION;
            } else if (page == 1) {
                conditionId = TtsConstant.NAVIC29CONDITION;
            } else if (page == 2) {
                conditionId = TtsConstant.NAVIC30CONDITION;
            }
        }else  if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN) {
            if (page == 0) {
                conditionId = TtsConstant.CHEXINC26CONDITION;
            } else if (page == 1) {
                conditionId = TtsConstant.CHEXINC27CONDITION;
            } else if (page == 2) {
                conditionId = TtsConstant.CHEXINC28CONDITION;
            }
        }
        return conditionId;
    }


    @Override
    public void onNext() {
        pageNum++;
        if (allList.size() > pageNum) {
            ItemAnimationUtils.startRotateAnimation(true,listRecyclerView);
            mHandler.sendEmptyMessageDelayed(MSG_NEXT_PAGE,ItemRotateAnimation.durationMillis+30);
        } else {
            highlightSelect(-1);
            mAdapter.notifyDataSetChanged();
            pageNum = allList.size() - 1;
            String conditionId = getNextPage(2);

//            getTtsMessage(conditionId, getString(R.string.next_page_fail));

            String defaultTts = getString(R.string.next_page_fail);
            Utils.getMessageWithoutTtsSpeak(getContext(), conditionId, new TtsUtils.OnConfirmInterface() {
                @Override
                public void onConfirm(String tts) {
                    String ttsText = tts;
                    if (TextUtils.isEmpty(tts)) {
                        ttsText = defaultTts;
                    }
                    Utils.startTTSOnly(ttsText);

                    if (mListData.get(0) instanceof PoiEntity || mListData.get(0) instanceof MXPoiEntity) {
                        Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC38CONDITION, R.string.condition_navi30,ttsText,true);
                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_next_page,conditionId,R.string.condition_navi30, ttsText);
                    } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                        Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_page,DatastatManager.response,TtsConstant.MHXC38CONDITION, R.string.condition_phoneC24,ttsText,true);
                        Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_4, conditionId, R.string.condition_phoneC24, ttsText);
                    }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                        Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_next_page,conditionId,R.string.condition_chexinC28, ttsText);
                    }
                }
            });
        }
    }

    private void highlightSelect(int position) {
        mAdapter.selectedItem(position);
    }

    //上下页第几个选择逻辑
    public void onSelectWhichOne(final int nMvwId) {
        try {
            String mainMessage = "";
            LogUtils.w(TAG, "size:"+mListData.size());
            if (mListData.size() > 0 && mListData.size() <= nMvwId) {
                LogUtils.w(TAG, "curEntityList.size()<=nMvwId");
                highlightSelect(-1);
                mAdapter.notifyDataSetChanged();
                String defaultTts = getString(R.string.choice_out_of_range);
                String conditionId = "";
                if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI || mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                    conditionId = TtsConstant.NAVIC25CONDITION;
                } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                    conditionId = TtsConstant.PHONEC14CONDITION;
                }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                    conditionId = TtsConstant.CHEXINC23CONDITION;
                }
                String finalConditionId = conditionId;
                Utils.getMessageWithoutTtsSpeak(activity, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
                    @Override
                    public void onConfirm(String tts) {
                        tts = Utils.replaceTts(tts, MAXNUM, "" + mListData.size());
                        if(TtsConstant.NAVIC25CONDITION.equals(finalConditionId)){
                            Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_the_number,DatastatManager.response,TtsConstant.MHXC35CONDITION, R.string.condition_navi25,tts,true);
                            Utils.eventTrack(activity, R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_choose_the_number, finalConditionId, R.string.condition_navi25,tts);
                        } else if(TtsConstant.PHONEC14CONDITION.equals(finalConditionId)){
                            Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_the_number,DatastatManager.response,TtsConstant.MHXC35CONDITION, R.string.object_phone_select_1,tts,true);
                            Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_1, finalConditionId, R.string.condition_phoneC14,tts);
                        }
                        Utils.startTTSOnly(tts);
                    }
                });
                return;
            } else if (mListData.size() > nMvwId) {
                String conditionId = "";
                String name = "";
                boolean isNaviWithPoint = isNaviWithPoint(semantic);
                boolean isAddPoints = isAddPoints(semantic);
                LogUtils.w(TAG, "isNaviWithPoint: " + isNaviWithPoint + "   isAddPoints: " + isAddPoints+"  topic: " + topic);

                if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                    if (PlatformConstant.Topic.GAS_STATION.equals(topic) || PlatformConstant.Topic.CHARGING_PILE.equals(topic)) {
                        mainMessage = getString(R.string.map_navi_to_station);
                        conditionId = TtsConstant.NAVIC22CONDITION;
//                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi22);
                        startNavigationTTS(conditionId,mainMessage,nMvwId, R.string.condition_navi22);
                    } else if (PlatformConstant.Topic.TOILET.equals(topic)
                            ||(semantic!=null&&semantic.contains("厕所"))
                            ||(semantic!=null&&semantic.contains("洗手间"))) {
                        conditionId = TtsConstant.NAVIC21CONDITION;
                        mainMessage = getString(R.string.map_navi_to_toilet);
//                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi21);
                        startNavigationTTS(conditionId,mainMessage,nMvwId, R.string.condition_navi21);
                    } else if (PlatformConstant.Topic.RESORT.equals(topic)||PlatformConstant.Topic.SPOT.equals(topic)) {
                        conditionId = TtsConstant.NAVIC23CONDITION;
                        mainMessage = getString(R.string.map_navi_to_resort);
//                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi23);
                        startNavigationTTS(conditionId,mainMessage,nMvwId, R.string.condition_navi23);
                    }else if (PlatformConstant.Topic.COMPANY.equals(topic)) {
                        name = getName(nMvwId);
                        mainMessage = Utils.replaceTts(getString(R.string.map_set_company_site), POI, name);
                        conditionId = TtsConstant.NAVIC20CONDITION;
//                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi20);
                        startNavigationTTS(conditionId,mainMessage,name,nMvwId, R.string.condition_navi20);
                    } else if (PlatformConstant.Topic.HOME.equals(topic)) {
                        name = getName(nMvwId);
                        mainMessage = Utils.replaceTts(getString(R.string.map_set_home_site), POI, name);
                        conditionId = TtsConstant.NAVIC19CONDITION;
//                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi19);
                        startNavigationTTS(conditionId,mainMessage,name,nMvwId, R.string.condition_navi19);
                    } else if (PlatformConstant.Topic.ROUTEP.equals(topic)) {

                        if (isNaviWithPoint) {
                            EventBus.getDefault().post(new MultiChoiceEvent(MultiChoiceEvent.EVENT_VIA, mListData.get(nMvwId)));
                        } else if (isAddPoints) {
                            EventBus.getDefault().post(new MultiChoiceEvent(MultiChoiceEvent.EVENT_ADD_POINTS, mListData.get(nMvwId)));
                        } else {
                            conditionId = TtsConstant.NAVIC24CONDITION;
                            name = getName(nMvwId);
                            mainMessage = Utils.replaceTts(getString(R.string.map_set_Point_site), POI, name);
//                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi24);
                            startNavigationTTS(conditionId,mainMessage,name,nMvwId, R.string.condition_navi24);
                        }

                    }  else {

                        if (isNaviWithPoint) {
                            EventBus.getDefault().post(new MultiChoiceEvent(MultiChoiceEvent.EVENT_END, mListData.get(nMvwId)));
                        } else {
                            conditionId = TtsConstant.NAVIC18CONDITION;
                            name = getName(nMvwId);
                            mainMessage = Utils.replaceTts(getString(R.string.navi_to_soon), POI, name);
//                        Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,R.string.condition_navi18);
                            startNavigationTTS(conditionId,mainMessage,name,nMvwId, R.string.condition_navi18);
                        }

                    }
                    // 解决连续说“确认”导致重复播报问题
                    resetMVWsession();
                } else if(mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                    conditionId = TtsConstant.PHONEC12CONDITION;
                    name = ((ContactEntity) (mListData.get(nMvwId))).name;
                    mainMessage = Utils.replaceTts(getString(R.string.one_number_search_result), CONTACT, name);
                    startDialTTS(conditionId,mainMessage,name,nMvwId);
                    Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_1, conditionId, R.string.condition_phoneC12,mainMessage);
                } else if(mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN) {
                    Semantic mSemantic= GsonUtil.stringToObject(semantic,Semantic.class);
                    if(PlatformConstant.ContentType.TEXT.equals(mSemantic.slots.contentType)) {
                        if (mSemantic.slots.content==null){
                            conditionId = TtsConstant.CHEXINC3CONDITION;
                            mainMessage = getString(R.string.chexin_leave_message_after_drop);
                            Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_which,conditionId,R.string.condition_chexinC22);
                            startCheXinTTS(conditionId,mainMessage,nMvwId);
                        }else {
                            conditionId = TtsConstant.CHEXINC6CONDITION;
                            name = ((CheXinEntity) (mListData.get(0))).name;
                            mainMessage = getString(R.string.chexin_sending_message);
                            Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_which,conditionId,R.string.condition_chexinC22);
                            startCheXinTTS(conditionId,mainMessage,name,nMvwId);
                        }
                    }else if(PlatformConstant.ContentType.REDPACKET.equals(mSemantic.slots.contentType)) {
                        if (mSemantic.slots.content==null) {
                            conditionId = TtsConstant.CHEXINC11CONDITION;
                            mainMessage = getString(R.string.chexin_input_amount);
                            Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_which,conditionId,R.string.condition_chexinC22);
                            startCheXinTTS(conditionId,mainMessage,nMvwId);
                        }else {
                            conditionId = TtsConstant.CHEXINC14CONDITION;
                            mainMessage = getString(R.string.chexin_confirmation_amount);
                            Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_which,conditionId,R.string.condition_chexinC22);
                            startCheXinTTS(conditionId,mainMessage,nMvwId);
                        }
                    }else{
                        conditionId = TtsConstant.CHEXINC19CONDITION;
                        name = ((CheXinEntity) (mListData.get(0))).name;
                        mainMessage = Utils.replaceTts(getString(R.string.chexin_send_position), CONTACT, name);
                        Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_which,conditionId,R.string.condition_chexinC22);
                        startCheXinTTS(conditionId,mainMessage,name,nMvwId);
                    }
                }else {
                    LogUtils.d(TAG, "unhandle case");
                }

            } else {
                LogUtils.d(TAG, "exception unhandle case");
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "e = " + e.toString());
        }

        highlightSelect(nMvwId);

    }

    /**
     * 是否带途径的导航
     */
    public static boolean isNaviWithPoint(String semantic) {
        try {
            Semantic semanticBean = GsonUtil.stringToObject(semantic, Semantic.class);
            return semanticBean.slots.endLoc != null && semanticBean.slots.viaLoc != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否添加多个途经点
     */
    public static boolean isAddPoints(String semantic) {
        try {
            Semantic semanticBean = GsonUtil.stringToObject(semantic, Semantic.class);
            return semanticBean.slots.viaLocNext != null && semanticBean.slots.viaLoc != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String getName(int nMvwId) {
        String name = "";
        if (mListData.get(nMvwId) instanceof PoiEntity){
            name = ((PoiEntity) (mListData.get(nMvwId))).getName();
        }else {
            name = ((MXPoiEntity) (mListData.get(nMvwId))).getName();
        }

        return name;
    }

    private void startCheXinTTS(String conditionId, final String defaultTts, final int nMvwId) {
        Utils.getMessageWithoutTtsSpeak(activity, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }
                Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (mListData == null || mListData.size() == 0 || nMvwId >= mListData.size())
                            return;
                        if (mListData.get(nMvwId) instanceof CheXinEntity) {
                            chexinSend(nMvwId);
                        }
                    }
                });
            }
        });
    }


    private void startCheXinTTS(String conditionId, final String defaultTts, final String replaceName, final int nMvwId) {
        Utils.getMessageWithoutTtsSpeak(activity, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }else {
                    ttsText =Utils.replaceTts(tts, CONTACT, replaceName);
                }
                Utils.startTTS(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (mListData == null || mListData.size() == 0 || nMvwId >= mListData.size())
                            return;
                        if (mListData.get(nMvwId) instanceof CheXinEntity) {
                            chexinSend(nMvwId);
                        }
                    }
                });
            }
        });
    }

    private void startDialTTS(String conditionId, final String defaultTts, final String replaceName, final int nMvwId) {
        Utils.getMessageWithoutTtsSpeak(activity, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                //呼叫XX
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }else {
                    ttsText =Utils.replaceTts(tts, CONTACT, replaceName);
                }
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
                        if (mListData == null || mListData.size() == 0 || nMvwId >= mListData.size())
                            return;
                        if (mListData.get(nMvwId) instanceof ContactEntity) {
                            dial(nMvwId);
                        }
                    }
                });
            }
        });
    }

    public void  navigationPlayStopped(int nMvwId){
            if (mListData == null || mListData.size() == 0 || nMvwId >= mListData.size()) return;
            if (mListData.get(nMvwId) instanceof PoiEntity) {
                naviToPoi(nMvwId);
            } else if (mListData.get(nMvwId) instanceof MXPoiEntity) {
                if (PlatformConstant.Topic.ROUTEP.equals(topic)) {
                    requestAddPass(nMvwId);
                } else {
                    naviToPoi(nMvwId);
                    if (PlatformConstant.Topic.COMPANY.equals(topic)) {
                        collectByPoi(nMvwId, ConstantsApp.MAP_COLLECT_COMPANY);
                    } else if (PlatformConstant.Topic.HOME.equals(topic)) {
                        collectByPoi(nMvwId, ConstantsApp.MAP_COLLECT_HOME);
                    }
                }
            }
    }

    public void startNavigationTTS(String conditionId, String defaultTts, final int nMvwId, final int condition) {
        Utils.getMessageWithoutTtsSpeak(activity, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
//                        navigationPlayStopped(nMvwId);
                        Utils.exitVoiceAssistant();
                    }
                });
                AppManager.getAppManager().finishListActivity();
                navigationPlayStopped(nMvwId);// 播报的同时开始导航
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_the_number,DatastatManager.response,TtsConstant.MHXC35CONDITION, condition,ttsText,true);
                Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,condition, ttsText);
            }
        });
    }


    private void startNavigationTTS(String conditionId, final String defaultTts, final String replaceName, final int nMvwId, final int condition) {
        Utils.getMessageWithoutTtsSpeak(activity, conditionId, defaultTts, new TtsUtils.OnConfirmInterface() {
            @Override
            public void onConfirm(String tts) {
                String ttsText = tts;
                if (TextUtils.isEmpty(tts)) {
                    ttsText = defaultTts;
                }else {
                    ttsText = Utils.replaceTts(tts, POI, replaceName);
                }
                Utils.startTTSOnly(ttsText, new TTSController.OnTtsStoppedListener() {
                    @Override
                    public void onPlayStopped() {
//                        navigationPlayStopped(nMvwId);
                        Utils.exitVoiceAssistant();
                    }
                });
                AppManager.getAppManager().finishListActivity();
                navigationPlayStopped(nMvwId);// 播报的同时开始导航
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_the_number,DatastatManager.response,TtsConstant.MHXC35CONDITION, condition,ttsText,true);
                Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_choose_the_number,conditionId,condition, ttsText);
            }
        });
    }

    private void dial(int nMvwId) {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
            if (iController != null) {
                if (iController instanceof IContactController) {
                    ((IContactController) iController).startDial(((ContactEntity) mListData.get(nMvwId)));
                    if(mHandler.hasMessages(MSG_DELAY_EXIT)) //延迟 500ms 隐藏显示框，防止有短暂的媒体音
                        mHandler.removeMessages(MSG_DELAY_EXIT);
                    mHandler.sendEmptyMessageDelayed(MSG_DELAY_EXIT,500);
                }
            } else {
                LogUtils.w(TAG, "iController==null");
            }
        }
    }

    private void chexinSend(int nMvwId) {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
            if (iController != null) {
                if (iController instanceof ICheXinController) {
                    ((ICheXinController) iController).startSend(nMvwId);
                }
            } else {
                LogUtils.w(TAG, "iController==null");
            }
        }
    }


    private void naviToPoi(int nMvwId) {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
            if (iController != null) {
                if (iController instanceof IMapController) {
                    ((IMapController) iController).startNavigation(mListData.get(nMvwId));
                }
            } else {
                LogUtils.w(TAG, "iController==null");
            }
        }
    }

    private void requestAddPass(int nMvwId) {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
            if (iController != null) {
                if (iController instanceof IMapController) {
                    MXPoiEntity entity = (MXPoiEntity) mListData.get(nMvwId);
                    /***********欧尚修改开始*****************/
//                    ((IMapController) iController).requestAddPass(entity.getName());
                    ((IMapController) iController).requestAddPass(entity);
                    /***********欧尚修改结束*****************/
                }
            } else {
                LogUtils.w(TAG, "iController==null");
            }
        }
    }

    /**
     * 根据poi进行收藏
     *
     * @param nMvwId
     * @param type   0=普通收藏点  1=家  2=公司
     */
    private void collectByPoi(int nMvwId, int type) {
        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
            if (iController != null) {
                if (iController instanceof IMapController) {
                    MXPoiEntity entity = (MXPoiEntity) mListData.get(nMvwId);
                    ((IMapController) iController).collectByPoi(entity, type);
                }
            } else {
                LogUtils.w(TAG, "iController==null");
            }
        }
    }

    private void onSelectWhichPage(int page) {
        if (page > allList.size()) {
            highlightSelect(-1);
            mAdapter.notifyDataSetChanged();
            String conditionId=getChoosePage(true);
            String defaults = Utils.replaceTts(getString(R.string.turn_off_out_of_boundary), NUM, ""+page);
            if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_the_page,DatastatManager.response,TtsConstant.MHXC36CONDITION, R.string.condition_navi3,defaults,true);
                Utils.eventTrack(activity,R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_which_page, conditionId, R.string.condition_navi34,defaults);
            } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_the_page,DatastatManager.response,TtsConstant.MHXC36CONDITION, R.string.condition_phoneC28,defaults,true);
                Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_6, conditionId, R.string.condition_phoneC28,defaults);
            }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_which_page,conditionId,R.string.condition_chexinC32,defaults);
            }
            getTtsMessage(conditionId,defaults,page+"");
        } else {
            pageNum = page - 1;
            mListData.clear();
            mListData.addAll(allList.get(pageNum));
            highlightSelect(-1);
            mAdapter.notifyDataSetChanged();
            pageNumIndicator.setPageNum(pageNum);
            String conditionId=getChoosePage(false);
            String defaults = Utils.replaceTts(getString(R.string.turn_off_in_boundary), NUM, ""+page);
            if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_choose_the_page,DatastatManager.response,TtsConstant.MHXC36CONDITION, R.string.condition_navi33,defaults,true);
                Utils.eventTrack(activity,R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_which_page, conditionId, R.string.condition_navi33,defaults);
            } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_choose_the_page,DatastatManager.response,TtsConstant.MHXC36CONDITION, R.string.condition_phoneC27,defaults,true);
                Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_6, conditionId, R.string.condition_phoneC27,defaults);
            }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_which_page,conditionId,R.string.condition_chexinC31,defaults);
            }
            getTtsMessage(conditionId,defaults,page+"");
        }
    }

    private void onFinalPage() {
        if (pageNum == allList.size() - 1) {
            highlightSelect(-1);
            mAdapter.notifyDataSetChanged();
            String conditionId=getFinalPage(true);
            String defaultMsg = getString(R.string.next_page_fail);
            if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_last_page2,DatastatManager.response,TtsConstant.MHXC39CONDITION, R.string.condition_navi32,defaultMsg,true);
                Utils.eventTrack(activity,R.string.skill_navi,R.string.scene_navi_select_destination,R.string.object_last_page,conditionId,R.string.condition_navi32, defaultMsg);
            } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_last_page2,DatastatManager.response,TtsConstant.MHXC39CONDITION, R.string.condition_phoneC26,defaultMsg,true);
                Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_5, conditionId, R.string.condition_phoneC26, defaultMsg);
            }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_last_page,conditionId,R.string.condition_chexinC30, defaultMsg);
            }
            getTtsMessage(conditionId, defaultMsg);
        } else {
            pageNum = allList.size() - 1;
            mListData.clear();
            mListData.addAll(allList.get(pageNum));
            highlightSelect(-1);
            mAdapter.notifyDataSetChanged();
            pageNumIndicator.setPageNum(pageNum);
            String conditionId=getFinalPage(false);
            String defaultMsg = getString(R.string.reach_final_page);
            if (mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI||mType==AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_navi, DatastatManager.primitive, R.string.object_last_page2,DatastatManager.response,TtsConstant.MHXC39CONDITION, R.string.condition_navi31,defaultMsg,true);
                Utils.eventTrack(activity,R.string.skill_navi, R.string.scene_navi_select_destination, R.string.object_last_page, conditionId, R.string.condition_navi31, defaultMsg);
            } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
                Utils.eventTrack(activity,R.string.skill_global_nowake, R.string.scene_call, DatastatManager.primitive, R.string.object_last_page2,DatastatManager.response,TtsConstant.MHXC39CONDITION, R.string.condition_phoneC25,defaultMsg,true);
                Utils.eventTrack(activity, R.string.skill_phone, R.string.scene_phone_select, R.string.object_phone_select_5, conditionId, R.string.condition_phoneC25, defaultMsg);
            }else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN){
                Utils.eventTrack(activity,R.string.skill_chexin,R.string.scene_select_contacts,R.string.object_chexin_select_last_page,conditionId,R.string.condition_chexinC29, defaultMsg);
            }
            getTtsMessage(conditionId,defaultMsg);

        }

    }


    private void fillUpPageDatas() {
        int itemMaxCount = 4;
        int flag1Count = 0;
        int flag0Count = 0;
        int pageCount = 1;
        int count = 0;
        List<BaseEntity> itemList = new ArrayList<>();
        for (int i = 0; i < mListData.size(); i++) {
            itemList.add(mListData.get(i));
            if (mListData.get(i) instanceof PoiEntity) {
                if (((PoiEntity) mListData.get(i)).flag == 1) { //1个poiEntity占用两行情况
                    flag1Count++;
                } else {
                    flag0Count++;
                }
            } else { //其它entity数据默认占一行
                flag0Count++;
            }
            count++;
            if (count >= mListData.size()) {
                allList.add(itemList);
            } else if (flag1Count * 2 + flag0Count >= itemMaxCount) {
                //当前页已满并且还有剩余数据
                allList.add(itemList);
                pageCount++;
                flag1Count = 0;
                flag0Count = 0;
                itemList = new ArrayList<>();
            }
        }
        mListData.clear();
        if (allList.size() > pageNum) {
            mListData.addAll(allList.get(pageNum));
        }
        mAdapter.notifyDataSetChanged();
        pageNumIndicator.setPageTotal(pageCount);
        pageNumIndicator.setPageNum(pageNum);
    }

    @Subscribe
    public void updateVoiceControlView(MessageListEvent messageListEvent) {
        switch (messageListEvent.eventType) {
            case LAST_PAGE:
                onLast();
                break;
            case NEXT_PAGE:
                onNext();
                break;
            case FINAL_PAGE:
                onFinalPage();
                break;
            case SELECT_WHICH_ONE:
                onSelectWhichOne(messageListEvent.index);
                break;
            case SELECT_WHICH_PAGE:
                onSelectWhichPage(messageListEvent.index);
                break;
        }
    }


}
