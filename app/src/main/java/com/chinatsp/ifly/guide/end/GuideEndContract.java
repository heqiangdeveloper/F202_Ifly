package com.chinatsp.ifly.guide.end;

import android.app.Activity;

import com.chinatsp.ifly.GuideMainActivity;
import com.chinatsp.ifly.base.BasePresenter;
import com.chinatsp.ifly.base.BaseView;

public interface GuideEndContract {
    interface View extends BaseView {

    }

    interface Presenter extends BasePresenter {
        void bindActivity(GuideMainActivity activity);
    }
}
