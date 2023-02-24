package com.chinatsp.ifly.module.weather;

import com.chinatsp.ifly.FullScreenActivity;

import io.reactivex.disposables.CompositeDisposable;

public class WeatherPresenter implements WeatherContract.Presenter {

    private WeatherContract.View mView;
    private CompositeDisposable mSubscriptions;
    private FullScreenActivity activity;

    public WeatherPresenter(WeatherContract.View androidView) {
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
    public void bindActivity(FullScreenActivity activity) {
        this.activity = activity;
    }
}
