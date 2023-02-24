package com.chinatsp.ifly.guide.settings;

import android.app.Activity;

import com.chinatsp.ifly.GuideMainActivity;

import io.reactivex.disposables.CompositeDisposable;

public class GuideSettingsPresenter implements GuideSettingsContract.Presenter {

    private GuideSettingsContract.View mView;
    private CompositeDisposable mSubscriptions;
    private GuideMainActivity activity;

    public GuideSettingsPresenter(GuideSettingsContract.View androidView) {
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
