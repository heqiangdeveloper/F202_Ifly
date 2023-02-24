package com.chinatsp.ifly.guide.step1;

import android.app.Activity;

import com.chinatsp.ifly.GuideMainActivity;

import io.reactivex.disposables.CompositeDisposable;

public class GuideStep1Presenter implements GuideStep1Contract.Presenter {

    private GuideStep1Contract.View mView;
    private CompositeDisposable mSubscriptions;
    private GuideMainActivity activity;

    public GuideStep1Presenter(GuideStep1Contract.View androidView) {
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
