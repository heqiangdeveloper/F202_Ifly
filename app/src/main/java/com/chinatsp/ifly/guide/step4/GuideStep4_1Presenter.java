package com.chinatsp.ifly.guide.step4;

import android.app.Activity;

import com.chinatsp.ifly.GuideMainActivity;

import io.reactivex.disposables.CompositeDisposable;

public class GuideStep4_1Presenter implements GuideStep4_1Contract.Presenter {

    private GuideStep4_1Contract.View mView;
    private CompositeDisposable mSubscriptions;
    private GuideMainActivity activity;

    public GuideStep4_1Presenter(GuideStep4_1Contract.View androidView) {
        this.mView = androidView;
        mSubscriptions = new CompositeDisposable();
    }

    @Override
    public void subscribe() {

    }


    @Override
    public void unSubscribe() {
        mSubscriptions.clear();
        if (activity != null) {
            activity = null;
        }
    }

    @Override
    public void bindActivity(GuideMainActivity activity) {
        this.activity = activity;
    }
}
