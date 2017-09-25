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
package com.alipay.mobile.social.rxjava.internal.functions;

import com.alipay.mobile.social.rxjava.exceptions.OnErrorNotImplementedException;
import com.alipay.mobile.social.rxjava.functions.Action;
import com.alipay.mobile.social.rxjava.functions.Consumer;
import com.alipay.mobile.social.rxjava.plugins.RxJavaPlugins;

/**
 * Utility methods to convert the BiFunction, Function3..Function9 instances to Function of Object array.
 */
public final class Functions {

    /**
     * Utility class.
     */
    private Functions() {
        throw new IllegalStateException("No instances!");
    }

    public static final Action EMPTY_ACTION = new EmptyAction();

    static final Consumer<Object> EMPTY_CONSUMER = new EmptyConsumer();
    public static final Runnable EMPTY_RUNNABLE = new EmptyRunnable();
    /**
     * Returns an empty consumer that does nothing.
     *
     * @param <T> the consumed value type, the value is ignored
     * @return an empty consumer that does nothing.
     */
    @SuppressWarnings("unchecked")
    public static <T> Consumer<T> emptyConsumer() {
        return (Consumer<T>) EMPTY_CONSUMER;
    }

    public static <T> Consumer<T> actionConsumer(Action action) {
        return new ActionConsumer<T>(action);
    }

    /**
     * Wraps the consumed Throwable into an OnErrorNotImplementedException and
     * signals it to the plugin error handler.
     */
    public static final Consumer<Throwable> ON_ERROR_MISSING = new OnErrorMissingConsumer();
    static final class EmptyRunnable implements Runnable {
        @Override
        public void run() { }

        @Override
        public String toString() {
            return "EmptyRunnable";
        }
    }
    static final class EmptyAction implements Action {
        @Override
        public void run() {
        }

        @Override
        public String toString() {
            return "EmptyAction";
        }
    }

    static final class EmptyConsumer implements Consumer<Object> {
        @Override
        public void accept(Object v) {
        }

        @Override
        public String toString() {
            return "EmptyConsumer";
        }
    }


    static final class OnErrorMissingConsumer implements Consumer<Throwable> {
        @Override
        public void accept(Throwable error) {
            RxJavaPlugins.onError(new OnErrorNotImplementedException(error));
        }
    }

    static final class ActionConsumer<T> implements Consumer<T> {
        final Action action;

        ActionConsumer(Action action) {
            this.action = action;
        }

        @Override
        public void accept(T t) throws Exception {
            action.run();
        }
    }

}
