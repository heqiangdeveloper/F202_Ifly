package com.chinatsp.ifly.guide.step2;

import android.app.Activity;

import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

public interface GuideStep2Contract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(GuideMainActivity activity);
    }
}
