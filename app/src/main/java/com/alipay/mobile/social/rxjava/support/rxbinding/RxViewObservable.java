/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package com.alipay.mobile.social.rxjava.support.rxbinding;

import android.view.View;

import com.alipay.mobile.social.rxjava.Observable;
import com.alipay.mobile.social.rxjava.ObservableEmitter;
import com.alipay.mobile.social.rxjava.ObservableOnSubscribe;
import com.alipay.mobile.social.rxjava.Scheduler;
import com.alipay.mobile.social.rxjava.android.schedulers.AndroidSchedulers;
import com.alipay.mobile.social.rxjava.disposables.Disposable;
import com.alipay.mobile.social.rxjava.functions.Action;
import com.alipay.mobile.social.rxjava.functions.Consumer;
import com.alipay.mobile.social.rxjava.functions.Function;
import com.alipay.mobile.social.rxjava.internal.functions.Functions;

import java.util.concurrent.TimeUnit;

/**
 * 以View事件为观察者提供数据源
 * <ul>
 * <li>1、包装Observable，支持Observable方法链式调用</li>
 * <li>2、发生异常时，自动重新订阅，保证后续事件顺利得到执行</li>
 * </ul>
 * @param <T>
 */
public abstract class RxViewObservable<T> implements Disposable {
    protected Disposable mDisposable;
    protected Observable<T> mObservable;
    protected View mView;
    private ObservableEmitter<T> mEmitter;

    private Consumer<? super T> mOnNext;
    private Action mOnFinally;
    private Action mOnTerminate;
    private Consumer<? super Throwable> mOnError;

    public RxViewObservable(View view) {
        mObservable = Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> e) throws Exception {
                mEmitter = e;
            }
        });
        mView = view;
    }

    public RxViewObservable<T> throttleFirst(long windowDuration, TimeUnit unit) {
        mObservable = mObservable.throttleFirst(windowDuration, unit);
        return this;
    }

    public RxViewObservable<T> debounce(long timeout, TimeUnit unit) {
        mObservable = mObservable.debounce(timeout, unit);
        return this;
    }

    public RxViewObservable<T> observeOn(Scheduler scheduler) {
        mObservable = mObservable.observeOn(scheduler);
        return this;
    }

    public RxViewObservable<T> doOnNext(Consumer<? super T> onNext) {
        mObservable = mObservable.doOnNext(onNext);
        return this;
    }

    public RxViewObservable<T> doOnTerminate(Action onTerminate) {
        mOnTerminate = onTerminate;
        return this;
    }

    public RxViewObservable<T> doAfterTerminate(Action onFinally) {
        mOnFinally = onFinally;
        return this;
    }

    public <R> RxViewObservable<R> map(Function<? super T, ? extends R> mapper) {
        return new WrappedObservabler<T, R>(this, mapper);
    }

    public final Disposable subscribe(Consumer<? super T> onNext) {
        return subscribe(onNext, Functions.emptyConsumer());
    }

    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        mOnNext = onNext;
        mOnError = onError;
        addViewListener(mView);
        mObservable = mObservable.observeOn(AndroidSchedulers.mainThread()) //确保事件回调到主线程中
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (null != mOnTerminate) {
                            mOnTerminate.run();
                        }
                    }
                });
        doSubscribeActual();
        return this;
    }

    private void doSubscribeActual() {
        mDisposable = mObservable.subscribe(new Consumer<T>() {
            @Override
            public void accept(T t) throws Exception {
                if (null != mOnTerminate) {
                    mOnTerminate.run();
                }

                if (null != mOnNext) {
                    mOnNext.accept(t);
                }

                if (null != mOnFinally) {
                    try {
                        mOnFinally.run();
                    } catch (Exception e) { //doAfterTerminate中的异常应该抛出
                        Thread currentThread = Thread.currentThread();
                        Thread.UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
                        handler.uncaughtException(currentThread, e);
                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (null != mOnError) {
                    mOnError.accept(throwable);
                }

                if (null != mOnFinally) {
                    mOnFinally.run();
                }

                doSubscribeActual();
            }
        });
    }


    @Override
    public void dispose() {
        if (null != mDisposable) {
            mDisposable.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        if (null != mDisposable) {
            return mDisposable.isDisposed();
        }
        return false;
    }

    protected void postEvent(T value) {
        mEmitter.onNext(value);
    }

    abstract void addViewListener(View view);
}
