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

/**
 * A linked-array-list implementation that only supports appending and consumption.
 *
 * @param <T> the value type
 */
public class AppendOnlyLinkedArrayList<T> {
    final int capacity;
    final Object[] head;
    Object[] tail;
    int offset;

    /**
     * Constructs an empty list with a per-link capacity.
     *
     * @param capacity the capacity of each link
     */
    public AppendOnlyLinkedArrayList(int capacity) {
        this.capacity = capacity;
        this.head = new Object[capacity + 1];
        this.tail = head;
    }

    /**
     * Append a non-null value to the list.
     * <p>Don't add null to the list!
     *
     * @param value the value to append
     */
    public void add(T value) {
        final int c = capacity;
        int o = offset;
        if (o == c) {
            Object[] next = new Object[c + 1];
            tail[c] = next;
            tail = next;
            o = 0;
        }
        tail[o] = value;
        offset = o + 1;
    }

    /**
     * Set a value as the first element of the list.
     *
     * @param value the value to set
     */
    public void setFirst(T value) {
        head[0] = value;
    }

    /**
     * Interprets the contents as NotificationLite objects and calls
     * the appropriate Observer method.
     *
     * @param <U>      the target type
     * @param observer the observer to emit the events to
     * @return true if a terminal event has been reached
     */
    public <U> boolean accept(Observer<? super U> observer) {
        Object[] a = head;
        final int c = capacity;
        while (a != null) {
            for (int i = 0; i < c; i++) {
                Object o = a[i];
                if (o == null) {
                    break;
                }

                if (NotificationLite.acceptFull(o, observer)) {
                    return true;
                }
            }
            a = (Object[]) a[c];
        }
        return false;
    }

}
