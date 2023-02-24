package com.chinatsp.ifly.guide.step3;

import android.app.Activity;

import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

public interface GuideStep3_1Contract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(GuideMainActivity activity);
    }
}
