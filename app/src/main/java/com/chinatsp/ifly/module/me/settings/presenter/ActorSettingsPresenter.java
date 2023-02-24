package com.chinatsp.ifly.module.me.settings.presenter;

import com.chinatsp.ifly.R;
import com.chinatsp.ifly.SettingsActivity;
import com.chinatsp.ifly.module.me.settings.contract.ActorSettingsContract;

import java.util.Arrays;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class ActorSettingsPresenter implements ActorSettingsContract.Presenter {

    private ActorSettingsContract.View mView;
    private CompositeDisposable mSubscriptions;
    private SettingsActivity activity;

    public ActorSettingsPresenter(ActorSettingsContract.View androidView) {
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

    @Override
    public List<String> getActors() {
        return Arrays.asList(activity.getResources().getStringArray(R.array.actor_names));
    }
}
