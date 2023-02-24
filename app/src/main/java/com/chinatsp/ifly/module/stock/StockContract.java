package com.chinatsp.ifly.module.stock;

import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.base.BaseEntity;
import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

import java.util.List;

public interface StockContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(FullScreenActivity activity);
    }
}
