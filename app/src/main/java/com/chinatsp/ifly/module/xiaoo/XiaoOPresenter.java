package com.chinatsp.ifly.module.xiaoo;

import android.app.Activity;

import io.reactivex.disposables.CompositeDisposable;

public class XiaoOPresenter implements XiaoOContract.Presenter {

    private XiaoOContract.View mView;
    private CompositeDisposable mSubscriptions;
    private Activity activity;

    public XiaoOPresenter(XiaoOContract.View androidView) {
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
    public void bindActivity(Activity activity) {
        this.activity = activity;
    }
}
