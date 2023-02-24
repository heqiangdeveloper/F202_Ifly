package com.chinatsp.ifly.module.me.settings.presenter;

import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.module.me.settings.contract.VoiceSettingsContract;

import io.reactivex.disposables.CompositeDisposable;

public class VoiceSettingsPresenter implements VoiceSettingsContract.Presenter {

    private VoiceSettingsContract.View mView;
    private CompositeDisposable mSubscriptions;
    private SettingsActivity activity;

    public VoiceSettingsPresenter(VoiceSettingsContract.View androidView) {
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
