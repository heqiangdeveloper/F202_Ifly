package com.chinatsp.ifly.module.stock;

import com.chinatsp.ifly.FullScreenActivity;
import io.reactivex.disposables.CompositeDisposable;

public class StockPresenter implements StockContract.Presenter {

    private StockContract.View mView;
    private CompositeDisposable mSubscriptions;
    private FullScreenActivity activity;

    public StockPresenter(StockContract.View androidView) {
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
