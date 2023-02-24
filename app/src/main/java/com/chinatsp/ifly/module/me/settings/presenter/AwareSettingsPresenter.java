package com.chinatsp.ifly.module.me.settings.presenter;

import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.module.me.settings.contract.AwareSettingsContract;

import io.reactivex.disposables.CompositeDisposable;

public class AwareSettingsPresenter implements AwareSettingsContract.Presenter {

    private AwareSettingsContract.View mView;
    private CompositeDisposable mSubscriptions;
    private SettingsActivity activity;

    public AwareSettingsPresenter(AwareSettingsContract.View androidView) {
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
