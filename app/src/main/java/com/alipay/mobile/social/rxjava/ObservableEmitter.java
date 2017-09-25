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

package com.alipay.mobile.social.rxjava;

import com.alipay.mobile.social.rxjava.disposables.Disposable;

/**
 * Abstraction over an RxJava {@link Observer} that allows associating
 * a resource with it.
 * <p>
 * The onNext, onError and onComplete methods should be called
 * in a sequential manner, just like the Observer's methods.
 * The other methods are thread-safe.
 *
 * @param <T> the value type to emit
 */
public interface ObservableEmitter<T> extends Emitter<T> {

    /**
     * Sets a Disposable on this emitter; any previous Disposable
     * or Cancellation will be unsubscribed/cancelled.
     *
     * @param d the disposable, null is allowed
     */
    void setDisposable(Disposable d);

    /**
     * Returns true if the downstream disposed the sequence.
     *
     * @return true if the downstream disposed the sequence
     */
    boolean isDisposed();

    /**
     * Attempts to emit the specified {@code Throwable} error if the downstream
     * hasn't cancelled the sequence or is otherwise terminated, returning false
     * if the emission is not allowed to happen due to lifecycle restrictions.
     * <p>
     * Unlike {@link #onError(Throwable)}, the {@code RxJavaPlugins.onError} is not called
     * if the error could not be delivered.
     *
     * @param t the throwable error to signal if possible
     * @return true if successful, false if the downstream is not able to accept further
     * events
     * @since 2.1.1 - experimental
     */
    boolean tryOnError(Throwable t);
}
