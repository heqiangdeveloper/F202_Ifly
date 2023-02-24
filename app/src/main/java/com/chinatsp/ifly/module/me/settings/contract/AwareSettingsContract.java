package com.chinatsp.ifly.module.me.settings.contract;

import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;
import com.chinatsp.ifly.SettingsActivity;

public interface AwareSettingsContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(SettingsActivity activity);
    }
}
