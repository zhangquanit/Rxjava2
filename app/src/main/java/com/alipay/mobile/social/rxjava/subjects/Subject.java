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

package com.alipay.mobile.social.rxjava.subjects;

import com.alipay.mobile.social.rxjava.Observable;
import com.alipay.mobile.social.rxjava.Observer;

/**
 * Represents an Observer and an Observable at the same time, allowing
 * multicasting events from a single source to multiple child Subscribers.
 * <p>All methods except the onSubscribe, onNext, onError and onComplete are thread-safe.
 *
 * @param <T> the item value type
 */
public abstract class Subject<T> extends Observable<T> implements Observer<T> {
    /**
     * Returns true if the subject has any Observers.
     * <p>The method is thread-safe.
     *
     * @return true if the subject has any Observers
     */
    public abstract boolean hasObservers();

    /**
     * Returns true if the subject has reached a terminal state through an error event.
     * <p>The method is thread-safe.
     *
     * @return true if the subject has reached a terminal state through an error event
     * @see #getThrowable()
     * &see {@link #hasComplete()}
     */
    public abstract boolean hasThrowable();

    /**
     * Returns true if the subject has reached a terminal state through a complete event.
     * <p>The method is thread-safe.
     *
     * @return true if the subject has reached a terminal state through a complete event
     * @see #hasThrowable()
     */
    public abstract boolean hasComplete();

    /**
     * Returns the error that caused the Subject to terminate or null if the Subject
     * hasn't terminated yet.
     * <p>The method is thread-safe.
     *
     * @return the error that caused the Subject to terminate or null if the Subject
     * hasn't terminated yet
     */
    public abstract Throwable getThrowable();

}
