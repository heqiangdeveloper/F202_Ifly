package com.chinatsp.ifly.module.xiaoo;

import android.app.Activity;

import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

public interface XiaoOContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(Activity activity);
    }
}
