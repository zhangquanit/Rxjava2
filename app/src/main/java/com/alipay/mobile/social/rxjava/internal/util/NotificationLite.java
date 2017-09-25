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
package com.alipay.mobile.social.rxjava.internal.util;

import com.alipay.mobile.social.rxjava.Observer;
import com.alipay.mobile.social.rxjava.disposables.Disposable;
import com.alipay.mobile.social.rxjava.internal.functions.ObjectHelper;

import java.io.Serializable;

/**
 * Lightweight notification handling utility class.
 */
public enum NotificationLite {
    COMPLETE;

    /**
     * Wraps a Throwable.
     */
    static final class ErrorNotification implements Serializable {

        private static final long serialVersionUID = -8759979445933046293L;
        final Throwable e;

        ErrorNotification(Throwable e) {
            this.e = e;
        }

        @Override
        public String toString() {
            return "NotificationLite.Error[" + e + "]";
        }

        @Override
        public int hashCode() {
            return e.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ErrorNotification) {
                ErrorNotification n = (ErrorNotification) obj;
                return ObjectHelper.equals(e, n.e);
            }
            return false;
        }
    }


    /**
     * Wraps a Disposable.
     */
    static final class DisposableNotification implements Serializable {

        private static final long serialVersionUID = -7482590109178395495L;
        final Disposable d;

        DisposableNotification(Disposable d) {
            this.d = d;
        }

        @Override
        public String toString() {
            return "NotificationLite.Disposable[" + d + "]";
        }
    }

    /**
     * Converts a value into a notification value.
     *
     * @param <T>   the actual value type
     * @param value the value to convert
     * @return the notification representing the value
     */
    public static <T> Object next(T value) {
        return value;
    }

    /**
     * Returns a complete notification.
     *
     * @return a complete notification
     */
    public static Object complete() {
        return COMPLETE;
    }

    /**
     * Converts a Throwable into a notification value.
     *
     * @param e the Throwable to convert
     * @return the notification representing the Throwable
     */
    public static Object error(Throwable e) {
        return new ErrorNotification(e);
    }


    /**
     * Converts a Disposable into a notification value.
     *
     * @param d the disposable to convert
     * @return the notification representing the Disposable
     */
    public static Object disposable(Disposable d) {
        return new DisposableNotification(d);
    }

    /**
     * Calls the appropriate Observer method based on the type of the notification.
     * <p>Does not check for a subscription notification.
     *
     * @param <T> the expected value type when unwrapped
     * @param o   the notification object
     * @param s   the Observer to call methods on
     * @return true if the notification was a terminal event (i.e., complete or error)
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean accept(Object o, Observer<? super T> s) {
        if (o == COMPLETE) {
            s.onComplete();
            return true;
        } else if (o instanceof ErrorNotification) {
            s.onError(((ErrorNotification) o).e);
            return true;
        }
        s.onNext((T) o);
        return false;
    }

    /**
     * Calls the appropriate Observer method based on the type of the notification.
     *
     * @param <T> the expected value type when unwrapped
     * @param o   the notification object
     * @param s   the subscriber to call methods on
     * @return true if the notification was a terminal event (i.e., complete or error)
     * @see #accept(Object, Observer)
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean acceptFull(Object o, Observer<? super T> s) {
        if (o == COMPLETE) {
            s.onComplete();
            return true;
        } else if (o instanceof ErrorNotification) {
            s.onError(((ErrorNotification) o).e);
            return true;
        } else if (o instanceof DisposableNotification) {
            s.onSubscribe(((DisposableNotification) o).d);
            return false;
        }
        s.onNext((T) o);
        return false;
    }

    @Override
    public String toString() {
        return "NotificationLite.Complete";
    }
}
