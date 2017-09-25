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

package com.alipay.mobile.social.rxjava.annotations;

import com.alipay.mobile.social.rxjava.schedulers.Schedulers;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates what kind of scheduler the class or method uses.
 * <p>
 * Constants are provided for instances from {@link Schedulers} as well as values for
 * {@linkplain #NONE not using a scheduler} and #CUSTOM a manually-specified scheduler.
 * Libraries providing their own values should namespace them with their base package name followed
 * by a colon ({@code :}) and then a human-readable name (e.g., {@code com.example:ui-thread}).
 *
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SchedulerSupport {
    /**
     * A special value indicating the operator/class doesn't use schedulers.
     */
    String NONE = "none";

    /**
     * The operator/class runs on RxJava's {@linkplain Schedulers#io() I/O scheduler} or takes
     * timing information from it.
     */
    String IO = "io.reactivex:io";


    /**
     * The kind of scheduler the class or method uses.
     *
     * @return the name of the scheduler the class or method uses
     */
    String value();
}
