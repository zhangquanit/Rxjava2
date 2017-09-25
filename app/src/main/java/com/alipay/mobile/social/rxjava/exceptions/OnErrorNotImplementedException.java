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

package com.alipay.mobile.social.rxjava.exceptions;

import com.alipay.mobile.social.rxjava.annotations.NonNull;

/**
 * Represents an exception used to signal to the {@code RxJavaPlugins.onError()} that a
 * callback-based subscribe() method on a base reactive type didn't specify
 * an onError handler.
 * <p>History: 2.0.6 - experimental
 *
 * @since 2.1 - beta
 */
public final class OnErrorNotImplementedException extends RuntimeException {

    private static final long serialVersionUID = -6298857009889503852L;

    /**
     * Wraps the {@code Throwable} before it
     * is signalled to the {@code RxJavaPlugins.onError()}
     * handler as {@code OnErrorNotImplementedException}.
     *
     * @param e the {@code Throwable} to signal; if null, a NullPointerException is constructed
     */
    public OnErrorNotImplementedException(@NonNull Throwable e) {
        super(e != null ? e.getMessage() : null, e != null ? e : new NullPointerException());
    }
}