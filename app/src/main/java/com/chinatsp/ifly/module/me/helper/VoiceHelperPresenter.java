package com.chinatsp.ifly.module.me.helper;

import com.chinatsp.ifly.SettingsActivity;

import io.reactivex.disposables.CompositeDisposable;

public class VoiceHelperPresenter implements VoiceHelperContract.Presenter {

    private VoiceHelperContract.View mView;
    private CompositeDisposable mSubscriptions;
    private SettingsActivity activity;

    public VoiceHelperPresenter(VoiceHelperContract.View androidView) {
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
    public void bindActivity(SettingsActivity activity) {
        this.activity = activity;
    }
}
