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
package com.alipay.mobile.social.rxjava.plugins;

import com.alipay.mobile.social.rxjava.Observable;
import com.alipay.mobile.social.rxjava.Observer;
import com.alipay.mobile.social.rxjava.Scheduler;
import com.alipay.mobile.social.rxjava.annotations.NonNull;
import com.alipay.mobile.social.rxjava.annotations.Nullable;
import com.alipay.mobile.social.rxjava.exceptions.CompositeException;
import com.alipay.mobile.social.rxjava.exceptions.OnErrorNotImplementedException;
import com.alipay.mobile.social.rxjava.exceptions.UndeliverableException;
import com.alipay.mobile.social.rxjava.functions.BiFunction;
import com.alipay.mobile.social.rxjava.functions.Consumer;
import com.alipay.mobile.social.rxjava.functions.Function;
import com.alipay.mobile.social.rxjava.internal.functions.ObjectHelper;
import com.alipay.mobile.social.rxjava.internal.schedulers.IoScheduler;
import com.alipay.mobile.social.rxjava.internal.util.ExceptionHelper;
import com.alipay.mobile.social.rxjava.schedulers.Schedulers;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

/**
 * Utility class to inject handlers to certain standard RxJava operations.
 */
public final class RxJavaPlugins {
    @Nullable
    static volatile Consumer<? super Throwable> errorHandler;

    @Nullable
    static volatile Function<? super Runnable, ? extends Runnable> onScheduleHandler;

    @Nullable
    static volatile Function<? super Callable<Scheduler>, ? extends Scheduler> onInitComputationHandler;

    @Nullable
    static volatile Function<? super Callable<Scheduler>, ? extends Scheduler> onInitIoHandler;

    @Nullable
    static volatile Function<? super Scheduler, ? extends Scheduler> onIoHandler;

    @Nullable
    static volatile Function<? super Scheduler, ? extends Scheduler> onComputationHandler;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile Function<? super Observable, ? extends Observable> onObservableAssembly;

    @SuppressWarnings("rawtypes")
    @Nullable
    static volatile BiFunction<? super Observable, ? super Observer, ? extends Observer> onObservableSubscribe;


    /**
     * Prevents changing the plugins.
     */
    static volatile boolean lockdown;

    /**
     * Calls the associated hook function.
     *
     * @param defaultScheduler a {@link Callable} which returns the hook's input value
     * @return the value returned by the hook, not null
     * @throws NullPointerException if the callable parameter or its result are null
     */
    @NonNull
    public static Scheduler initIoScheduler(@NonNull Callable<Scheduler> defaultScheduler) {
        ObjectHelper.requireNonNull(defaultScheduler, "Scheduler Callable can't be null");
        Function<? super Callable<Scheduler>, ? extends Scheduler> f = onInitIoHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler);
    }


    /**
     * Calls the associated hook function.
     *
     * @param defaultScheduler a {@link Callable} which returns the hook's input value
     * @return the value returned by the hook, not null
     * @throws NullPointerException if the callable parameter or its result are null
     */
    @NonNull
    public static Scheduler initComputationScheduler(@NonNull Callable<Scheduler> defaultScheduler) {
        ObjectHelper.requireNonNull(defaultScheduler, "Scheduler Callable can't be null");
        Function<? super Callable<Scheduler>, ? extends Scheduler> f = onInitComputationHandler;
        if (f == null) {
            return callRequireNonNull(defaultScheduler);
        }
        return applyRequireNonNull(f, defaultScheduler); // JIT will skip this
    }

    /**
     * Called when an undeliverable error occurs.
     *
     * @param error the error to report
     */
    public static void onError(@NonNull Throwable error) {
        Consumer<? super Throwable> f = errorHandler;

        if (error == null) {
            error = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        } else {
            if (!isBug(error)) {
                error = new UndeliverableException(error);
            }
        }

        if (f != null) {
            try {
                f.accept(error);
                return;
            } catch (Throwable e) {
                // Exceptions.throwIfFatal(e); TODO decide
//                e.printStackTrace(); // NOPMD
                uncaught(e);
            }
        }
//        error.printStackTrace(); // NOPMD
        uncaught(error);
    }

    /**
     * Checks if the given error is one of the already named
     * bug cases that should pass through {@link #onError(Throwable)}
     * as is.
     *
     * @param error the error to check
     * @return true if the error should pass through, false if
     * it may be wrapped into an UndeliverableException
     */
    static boolean isBug(Throwable error) {
        // user forgot to add the onError handler in subscribe
        if (error instanceof OnErrorNotImplementedException) {
            return true;
        }
        // general protocol violations
        // it's either due to an operator bug or concurrent onNext
        if (error instanceof IllegalStateException) {
            return true;
        }
        // nulls are generally not allowed
        // likely an operator bug or missing null-check
        if (error instanceof NullPointerException) {
            return true;
        }
        // bad arguments, likely invalid user input
        if (error instanceof IllegalArgumentException) {
            return true;
        }
        // Crash while handling an exception
        if (error instanceof CompositeException) {
            return true;
        }
        // everything else is probably due to lifecycle limits
        return false;
    }

    static void uncaught(@NonNull Throwable error) {
        Thread currentThread = Thread.currentThread();
        UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        handler.uncaughtException(currentThread, error);
    }

    /**
     * Calls the associated hook function.
     *
     * @param defaultScheduler the hook's input value
     * @return the value returned by the hook
     */
    @NonNull
    public static Scheduler onIoScheduler(@NonNull Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onIoHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * Calls the associated hook function.
     * @param defaultScheduler the hook's input value
     * @return the value returned by the hook
     */
    @NonNull
    public static Scheduler onComputationScheduler(@NonNull Scheduler defaultScheduler) {
        Function<? super Scheduler, ? extends Scheduler> f = onComputationHandler;
        if (f == null) {
            return defaultScheduler;
        }
        return apply(f, defaultScheduler);
    }

    /**
     * Called when a task is scheduled.
     *
     * @param run the runnable instance
     * @return the replacement runnable
     */
    @NonNull
    public static Runnable onSchedule(@NonNull Runnable run) {
        Function<? super Runnable, ? extends Runnable> f = onScheduleHandler;
        if (f == null) {
            return run;
        }
        return apply(f, run);
    }


    /**
     * Sets the specific hook function.
     *
     * @param handler the hook function to set, null allowed, but the function may not return null
     */
    public static void setInitIoSchedulerHandler(@Nullable Function<? super Callable<Scheduler>, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onInitIoHandler = handler;
    }

    /**
     * Sets the specific hook function.
     *
     * @param handler the hook function to set, null allowed
     */
    public static void setIoSchedulerHandler(@Nullable Function<? super Scheduler, ? extends Scheduler> handler) {
        if (lockdown) {
            throw new IllegalStateException("Plugins can't be changed anymore");
        }
        onIoHandler = handler;
    }


    /**
     * Calls the associated hook function.
     *
     * @param <T>      the value type
     * @param source   the hook's input value
     * @param observer the observer
     * @return the value returned by the hook
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @NonNull
    public static <T> Observer<? super T> onSubscribe(@NonNull Observable<T> source, @NonNull Observer<? super T> observer) {
        BiFunction<? super Observable, ? super Observer, ? extends Observer> f = onObservableSubscribe;
        if (f != null) {
            return apply(f, source, observer);
        }
        return observer;
    }

    /**
     * Calls the associated hook function.
     *
     * @param <T>    the value type
     * @param source the hook's input value
     * @return the value returned by the hook
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @NonNull
    public static <T> Observable<T> onAssembly(@NonNull Observable<T> source) {
        Function<? super Observable, ? extends Observable> f = onObservableAssembly;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * Create an instance of the default {@link Scheduler} used for {@link Schedulers#io()}
     * except using {@code threadFactory} for thread creation.
     * <p>History: 2.0.5 - experimental
     *
     * @param threadFactory thread factory to use for creating worker threads. Note that this takes precedence over any
     *                      system properties for configuring new thread creation. Cannot be null.
     * @return the created Scheduler instance
     * @since 2.1
     */
    @NonNull
    public static Scheduler createIoScheduler(@NonNull ThreadFactory threadFactory) {
        return new IoScheduler(ObjectHelper.requireNonNull(threadFactory, "threadFactory is null"));
    }

    /**
     * Wraps the call to the function in try-catch and propagates thrown
     * checked exceptions as RuntimeException.
     *
     * @param <T> the input type
     * @param <R> the output type
     * @param f   the function to call, not null (not verified)
     * @param t   the parameter value to the function
     * @return the result of the function call
     */
    @NonNull
    static <T, R> R apply(@NonNull Function<T, R> f, @NonNull T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the function in try-catch and propagates thrown
     * checked exceptions as RuntimeException.
     *
     * @param <T> the first input type
     * @param <U> the second input type
     * @param <R> the output type
     * @param f   the function to call, not null (not verified)
     * @param t   the first parameter value to the function
     * @param u   the second parameter value to the function
     * @return the result of the function call
     */
    @NonNull
    static <T, U, R> R apply(@NonNull BiFunction<T, U, R> f, @NonNull T t, @NonNull U u) {
        try {
            return f.apply(t, u);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the Scheduler creation callable in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     *
     * @param s the {@link Callable} which returns a {@link Scheduler}, not null (not verified). Cannot return null
     * @return the result of the callable call, not null
     * @throws NullPointerException if the callable parameter returns null
     */
    @NonNull
    static Scheduler callRequireNonNull(@NonNull Callable<Scheduler> s) {
        try {
            return ObjectHelper.requireNonNull(s.call(), "Scheduler Callable result can't be null");
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    /**
     * Wraps the call to the Scheduler creation function in try-catch and propagates thrown
     * checked exceptions as RuntimeException and enforces that result is not null.
     *
     * @param f the function to call, not null (not verified). Cannot return null
     * @param s the parameter value to the function
     * @return the result of the function call, not null
     * @throws NullPointerException if the function parameter returns null
     */
    @NonNull
    static Scheduler applyRequireNonNull(@NonNull Function<? super Callable<Scheduler>, ? extends Scheduler> f, Callable<Scheduler> s) {
        return ObjectHelper.requireNonNull(apply(f, s), "Scheduler Callable result can't be null");
    }

    /**
     * Helper class, no instances.
     */
    private RxJavaPlugins() {
        throw new IllegalStateException("No instances!");
    }
}
