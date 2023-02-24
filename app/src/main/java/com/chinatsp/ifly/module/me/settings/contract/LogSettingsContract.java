package com.chinatsp.ifly.module.me.settings.contract;

import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

public interface LogSettingsContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(SettingsActivity activity);
    }
}
