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

import com.alipay.mobile.social.rxjava.Observable;
import com.alipay.mobile.social.rxjava.Observer;
import com.alipay.mobile.social.rxjava.Scheduler;
import com.alipay.mobile.social.rxjava.disposables.Disposable;
import com.alipay.mobile.social.rxjava.internal.disposables.DisposableHelper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class ObservableInterval extends Observable<Long> {
    final Scheduler scheduler;
    final long initialDelay;
    final long period;
    final TimeUnit unit;

    public ObservableInterval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
    }

    @Override
    public void subscribeActual(Observer<? super Long> s) {
        IntervalObserver is = new IntervalObserver(s);
        s.onSubscribe(is);

        Scheduler sch = scheduler;


        Disposable d = sch.schedulePeriodicallyDirect(is, initialDelay, period, unit);
        is.setResource(d);

    }

    static final class IntervalObserver
            extends AtomicReference<Disposable>
            implements Disposable, Runnable {


        private static final long serialVersionUID = 346773832286157679L;

        final Observer<? super Long> actual;

        long count;

        IntervalObserver(Observer<? super Long> actual) {
            this.actual = actual;
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return get() == DisposableHelper.DISPOSED;
        }

        @Override
        public void run() {
            if (get() != DisposableHelper.DISPOSED) {
                actual.onNext(count++);
            }
        }

        public void setResource(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }
    }
}
