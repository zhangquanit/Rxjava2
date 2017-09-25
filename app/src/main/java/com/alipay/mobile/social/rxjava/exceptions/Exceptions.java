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
 * Utility class to help propagate checked exceptions and rethrow exceptions
 * designated as fatal.
 */
public final class Exceptions {

    /**
     * Utility class.
     */
    private Exceptions() {
        throw new IllegalStateException("No instances!");
    }


    /**
     * Throws a particular {@code Throwable} only if it belongs to a set of "fatal" error varieties. These
     * varieties are as follows:
     * <ul>
     * <li>{@code VirtualMachineError}</li>
     * <li>{@code ThreadDeath}</li>
     * <li>{@code LinkageError}</li>
     * </ul>
     * This can be useful if you are writing an operator that calls user-supplied code, and you want to
     * notify subscribers of errors encountered in that code by calling their {@code onError} methods, but only
     * if the errors are not so catastrophic that such a call would be futile, in which case you simply want to
     * rethrow the error.
     *
     * @param t the {@code Throwable} to test and perhaps throw
     * @see <a href="https://github.com/ReactiveX/RxJava/issues/748#issuecomment-32471495">RxJava: StackOverflowError is swallowed (Issue #748)</a>
     */
    public static void throwIfFatal(@NonNull Throwable t) {
        // values here derived from https://github.com/ReactiveX/RxJava/issues/748#issuecomment-32471495
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        } else if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        } else if (t instanceof LinkageError) {
            throw (LinkageError) t;
        }
    }
}
