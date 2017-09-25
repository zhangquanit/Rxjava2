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
import com.alipay.mobile.social.rxjava.internal.disposables.EmptyDisposable;
import com.alipay.mobile.social.rxjava.internal.fuseable.ScalarCallable;

public final class ObservableEmpty extends Observable<Object> implements ScalarCallable<Object> {
    public static final Observable<Object> INSTANCE = new ObservableEmpty();

    private ObservableEmpty() {
    }

    @Override
    protected void subscribeActual(Observer<? super Object> o) {
        EmptyDisposable.complete(o);
    }

    @Override
    public Object call() {
        return null; // null scalar is interpreted as being empty
    }
}
