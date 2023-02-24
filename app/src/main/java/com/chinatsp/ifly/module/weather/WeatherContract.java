package com.chinatsp.ifly.module.weather;

import com.chinatsp.ifly.FullScreenActivity;
import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

public interface WeatherContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(FullScreenActivity activity);
    }
}
