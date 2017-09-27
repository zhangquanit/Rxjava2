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

package com.alipay.mobile.social.rxjava.internal.operators.observable;

import com.alipay.mobile.social.rxjava.ObservableSource;
import com.alipay.mobile.social.rxjava.Observer;
import com.alipay.mobile.social.rxjava.Scheduler;
import com.alipay.mobile.social.rxjava.disposables.Disposable;
import com.alipay.mobile.social.rxjava.internal.disposables.DisposableHelper;
import com.alipay.mobile.social.rxjava.observers.SerializedObserver;
import com.alipay.mobile.social.rxjava.plugins.RxJavaPlugins;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class ObservableThrottleFirstTimed<T> extends AbstractObservableWithUpstream<T, T> {
    final long timeout;
    final TimeUnit unit;
    final Scheduler scheduler;

    public ObservableThrottleFirstTimed(ObservableSource<T> source,
                                        long timeout, TimeUnit unit, Scheduler scheduler) {
        super(source);
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    @Override
    public void subscribeActual(Observer<? super T> t) {
        source.subscribe(new DebounceTimedObserver<T>(
                new SerializedObserver<T>(t),
                timeout, unit, scheduler.createWorker()));
    }

    static final class DebounceTimedObserver<T>
            extends AtomicReference<Disposable>
            implements Observer<T>, Disposable, Runnable {
        private static final long serialVersionUID = 786994795061867455L;

        final Observer<? super T> actual;
        final long timeout;
        final TimeUnit unit;
        final Scheduler.Worker worker;

        Disposable s;

        volatile boolean gate;

        boolean done;

        DebounceTimedObserver(Observer<? super T> actual, long timeout, TimeUnit unit, Scheduler.Worker worker) {
            this.actual = actual;
            this.timeout = timeout;
            this.unit = unit;
            this.worker = worker;
        }

        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;
                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            if (!gate && !done) {
                gate = true;

                actual.onNext(t);

                Disposable d = get();
                if (d != null) {
                    d.dispose();
                }
                Disposable schedule = worker.schedule(this, timeout, unit);
                DisposableHelper.replace(this, schedule);
            }


        }

        @Override
        public void run() {
            gate = false;
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
            } else {
                done = true;
                actual.onError(t);
                worker.dispose();
            }
        }

        @Override
        public void onComplete() {
            if (!done) {
                done = true;
                actual.onComplete();
                worker.dispose();
            }
        }

        @Override
        public void dispose() {
            s.dispose();
            worker.dispose();
        }

        @Override
        public boolean isDisposed() {
            return worker.isDisposed();
        }
    }
}
