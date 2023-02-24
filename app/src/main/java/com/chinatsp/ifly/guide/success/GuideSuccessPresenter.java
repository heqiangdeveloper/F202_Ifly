package com.chinatsp.ifly.guide.success;

import android.app.Activity;

import com.chinatsp.ifly.GuideMainActivity;

import io.reactivex.disposables.CompositeDisposable;

public class GuideSuccessPresenter implements GuideSuccessContract.Presenter {

    private GuideSuccessContract.View mView;
    private CompositeDisposable mSubscriptions;
    private GuideMainActivity activity;

    public GuideSuccessPresenter(GuideSuccessContract.View androidView) {
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
