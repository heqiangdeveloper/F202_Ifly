package com.chinatsp.ifly.module.seachlist;

import android.util.Log;

import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.entity.FlightEntity;
import com.chinatsp.ifly.entity.MXPoiEntity;
import com.chinatsp.ifly.entity.MultiChoiceEvent;
import com.chinatsp.ifly.entity.PoiEntity;
import com.chinatsp.ifly.entity.TrainEntity;
import com.chinatsp.ifly.utils.LogUtils;
import com.chinatsp.ifly.utils.Utils;
import com.chinatsp.ifly.voice.platformadapter.PlatformConstant;
import com.chinatsp.ifly.voice.platformadapter.controller.ContactController;
import com.chinatsp.ifly.voice.platformadapter.controller.MapController;
import com.iflytek.adapter.PlatformHelp;
import com.iflytek.adapter.controllerInterface.IController;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

import static org.greenrobot.eventbus.EventBus.TAG;

public class SearchListPresenter implements SearchListContract.Presenter {

    private SearchListContract.View mView;
    private CompositeDisposable mSubscriptions;
    private FullScreenActivity activity;

    public SearchListPresenter(SearchListContract.View androidView) {
        this.mView = androidView;
        mSubscriptions = new CompositeDisposable();
    }

    @Override
    public void subscribe() {

    }

    public List<BaseEntity> getSearchListContactData() {
        List<BaseEntity> listData = new ArrayList<>();
        ContactEntity entity;
        for (int i = 0; i < 17; i++) {
            entity = new ContactEntity("肖永君" + (i + 1), "13078988899");
            listData.add(entity);
        }
        return listData;
    }

    @Override
    public List<BaseEntity> getSearchListPoiData() {
        List<BaseEntity> listData = new ArrayList<>();
        PoiEntity entity;
        for (int i = 0; i < 21; i++) {
//            if(i % 5 == 0) {
//                entity = new PoiEntity("观音桥" + i,"","","","",
//                        "","","", "商业步行街", "中国重庆市江北区观音桥",
//                        "", "", "");
//            } else {
//                entity = new PoiEntity("观音桥" + i,"","","","",
//                        "","","", "商业步行街", "中国重庆市江北区观音桥",
//                        "", "", "");
//            }
//
//            listData.add(entity);
        }
        return listData;
    }

    @Override
    public List<BaseEntity> getSearchListPlaneData() {

        List<BaseEntity> listData = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
//            entity = new PlaneEntity("12:45", "15:05", "江北 T3" + i,
//                    "首都 T3" + i, 1204+i, "东航 MU5647", "经济舱 8.8折",
//                    10+i, "60%");
//            listData.add(entity);
        }
        return listData;
    }

    @Override
    public List<BaseEntity> getSearchListTrainData() {
        List<BaseEntity> listData = new ArrayList<>();
        TrainEntity entity;
        for (int i = 0; i < 9; i++) {
//            entity = new TrainEntity("12:45", "11:05", "重庆西" + i, "北京西", "10小时20分钟",
//                    1204, "G5748", "二等座", 12, "一等座", 23, "软卧", 10,
//                    "无座", 235);
//
//            if (i == 0) {
//                entity.setTicket4(0);
//            }
//            if(i == 2) {
//                entity.setTrainInfo("T1678");
//                entity.setTicket2(0);
//            }
//            listData.add(entity);
        }
        return listData;
    }

    @Override
    public void onViewClick(BaseEntity entity, int type) {
        if (type == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
            ContactEntity contact = (ContactEntity) entity;
            Log.d(TAG, "播打电话:" + contact.toString());
        } else if (type == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI) {
            PoiEntity poi = (PoiEntity) entity;
            Log.d(TAG, "导航:" + poi.toString());
        }else if (type == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI) {
            MXPoiEntity poi = (MXPoiEntity) entity;
            Log.d(TAG, "导航:" + poi.toString());
        } else if (type == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_PLANE) {
            FlightEntity poi = (FlightEntity) entity;
            Log.d(TAG, "机票:" + poi.toString());
        }

        if (PlatformHelp.getInstance().getPlatformClient() != null) {
            IController iController = PlatformHelp.getInstance().getPlatformClient().getCurController();
            if (iController != null) {
                if (iController instanceof MapController) {
//                    Utils.exitVoiceAssistant();
//                    ((MapController) iController).startNavigation(entity);

                    if (SearchListFragment.isNaviWithPoint(activity.getSemanticStr())) {
                        if (PlatformConstant.Topic.ROUTEP.equals(activity.getTopic())) {
                            EventBus.getDefault().post(new MultiChoiceEvent(MultiChoiceEvent.EVENT_VIA, entity));
                        } else {
                            EventBus.getDefault().post(new MultiChoiceEvent(MultiChoiceEvent.EVENT_END, entity));
                        }
                        return;
                    } else if (SearchListFragment.isAddPoints(activity.getSemanticStr())) {
                        EventBus.getDefault().post(new MultiChoiceEvent(MultiChoiceEvent.EVENT_ADD_POINTS, entity));
                        return;
                    }

                    Utils.exitVoiceAssistant();
                    if (entity instanceof PoiEntity) {
                        ((MapController) iController).naviToPoi(entity);
                    } else if (entity instanceof MXPoiEntity) {
                        if (PlatformConstant.Topic.ROUTEP.equals(activity.getTopic())) {
                            ((MapController) iController).requestAddPass(entity);
                        } else {
                            ((MapController) iController).naviToPoi(entity);
                            if (PlatformConstant.Topic.COMPANY.equals(activity.getTopic())) {
                                ((MapController) iController).collectByPoi((MXPoiEntity) entity, 2);
                            } else if (PlatformConstant.Topic.HOME.equals(activity.getTopic())) {
                                ((MapController) iController).collectByPoi((MXPoiEntity) entity, 1);
                            }
                        }
                    }

                } else if (iController instanceof ContactController) {
                    ((ContactController) iController).startDial((ContactEntity) entity);
                    ((ContactController) iController).exitVoiceAssistant();
                }
            } else {
                LogUtils.w(TAG, "iController==null");
            }
        }
    }

    @Override
    public void onViewChildClick(BaseEntity entity, int type) {
        if (type == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI) {
            PoiEntity poi = (PoiEntity) entity;
            Log.d("xyj", "播打电话" + poi.toString());
        }
    }

    @Override
    public void unSubscribe() {
        mSubscriptions.clear();
        if (activity != null) {
            activity = null;
        }
    }

    @Override
    public void bindActivity(FullScreenActivity activity) {
        this.activity = activity;
    }
}
