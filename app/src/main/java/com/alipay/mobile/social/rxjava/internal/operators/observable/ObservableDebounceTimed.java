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
import com.alipay.mobile.social.rxjava.Scheduler.Worker;
import com.alipay.mobile.social.rxjava.disposables.Disposable;
import com.alipay.mobile.social.rxjava.internal.disposables.DisposableHelper;
import com.alipay.mobile.social.rxjava.observers.SerializedObserver;
import com.alipay.mobile.social.rxjava.plugins.RxJavaPlugins;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class ObservableDebounceTimed<T> extends AbstractObservableWithUpstream<T, T> {
    final long timeout;
    final TimeUnit unit;
    final Scheduler scheduler;

    public ObservableDebounceTimed(ObservableSource<T> source, long timeout, TimeUnit unit, Scheduler scheduler) {
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
            implements Observer<T>, Disposable {
        final Observer<? super T> actual;
        final long timeout;
        final TimeUnit unit;
        final Scheduler.Worker worker;

        Disposable s;

        final AtomicReference<Disposable> timer = new AtomicReference<Disposable>();

        volatile long index;

        boolean done;

        DebounceTimedObserver(Observer<? super T> actual, long timeout, TimeUnit unit, Worker worker) {
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
            if (done) {
                return;
            }
            long idx = index + 1;
            index = idx;

            Disposable d = timer.get();
            //取消上次请求
            if (d != null) {
                d.dispose();
            }
            //定时当前请求
            DebounceEmitter<T> de = new DebounceEmitter<T>(t, idx, this);
            if (timer.compareAndSet(d, de)) {
                d = worker.schedule(de, timeout, unit);

                de.setResource(d);
            }

        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            actual.onError(t);
            worker.dispose();
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;

            Disposable d = timer.get();
            if (d != DisposableHelper.DISPOSED) {
                @SuppressWarnings("unchecked")
                DebounceEmitter<T> de = (DebounceEmitter<T>) d;
                if (de != null) {
                    de.run();
                }
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

        void emit(long idx, T t, DebounceEmitter<T> emitter) {
            //检查
            if (idx == index) {
                actual.onNext(t);
                emitter.dispose();
            }
        }
    }

    static final class DebounceEmitter<T> extends AtomicReference<Disposable> implements Runnable, Disposable {

        private static final long serialVersionUID = 6812032969491025141L;

        final T value;
        final long idx;
        final DebounceTimedObserver<T> parent;

        final AtomicBoolean once = new AtomicBoolean();

        DebounceEmitter(T value, long idx, DebounceTimedObserver<T> parent) {
            this.value = value;
            this.idx = idx;
            this.parent = parent;
        }

        @Override
        public void run() {
            if (once.compareAndSet(false, true)) {
                parent.emit(idx, value, this);
            }
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return get() == DisposableHelper.DISPOSED;
        }

        public void setResource(Disposable d) {
            DisposableHelper.replace(this, d);
        }
    }
}
