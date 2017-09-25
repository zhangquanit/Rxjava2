package com.alipay.mobile.social.rxjava.support.rxbinding;

import android.view.View;

import com.alipay.mobile.social.rxjava.functions.Function;


public class WrappedObservabler<T, U> extends RxViewObservable<U> {
    private RxViewObservable<T> mSource;

    public WrappedObservabler(RxViewObservable<T> observable, Function<? super T, ? extends U> mapper) {
        super(observable.mView);
        mObservable = observable.mObservable.map(mapper);
        mSource = observable;
    }

    @Override
    void addViewListener(View view) {
        mSource.addViewListener(view);
    }
}
