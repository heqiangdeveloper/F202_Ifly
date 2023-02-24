package com.chinatsp.ifly.module.me.settings.presenter;

import android.util.Log;

import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.module.me.settings.contract.AnswerSettingsContract;
import com.chinatsp.ifly.module.me.settings.contract.LogSettingsContract;
import com.chinatsp.ifly.module.me.settings.view.fragment.LogSettingsFragment;

import io.reactivex.disposables.CompositeDisposable;

public class LogSettingsPresenter implements AnswerSettingsContract.Presenter {

    private LogSettingsContract.View mView;
    private CompositeDisposable mSubscriptions;
    private SettingsActivity activity;

    public LogSettingsPresenter(LogSettingsContract.View androidView) {
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
