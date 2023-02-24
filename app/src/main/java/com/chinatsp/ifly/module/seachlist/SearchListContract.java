package com.chinatsp.ifly.module.seachlist;

import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

import java.util.List;

public interface SearchListContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(FullScreenActivity activity);
        List<BaseEntity> getSearchListContactData();
        List<BaseEntity> getSearchListPoiData();
        List<BaseEntity> getSearchListPlaneData();
        List<BaseEntity> getSearchListTrainData();
        void onViewClick(BaseEntity entity, int type);
        void onViewChildClick(BaseEntity entity, int type);
    }
}
