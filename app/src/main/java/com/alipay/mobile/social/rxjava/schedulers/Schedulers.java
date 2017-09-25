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

package com.alipay.mobile.social.rxjava.schedulers;

import com.alipay.mobile.social.rxjava.Scheduler;
import com.alipay.mobile.social.rxjava.annotations.NonNull;
import com.alipay.mobile.social.rxjava.functions.Function;
import com.alipay.mobile.social.rxjava.internal.schedulers.ComputationScheduler;
import com.alipay.mobile.social.rxjava.internal.schedulers.IoScheduler;
import com.alipay.mobile.social.rxjava.plugins.RxJavaPlugins;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;


/**
 * Static factory methods for returning standard Scheduler instances.
 * <p>
 * The initial and runtime values of the various scheduler types can be overridden via the
 * {@code RxJavaPlugins.setInit(scheduler name)SchedulerHandler()} and
 * {@code RxJavaPlugins.set(scheduler name)SchedulerHandler()} respectively.
 * <p>
 * <strong>Supported system properties ({@code System.getProperty()}):</strong>
 */
public final class Schedulers {


    static final Scheduler IO;

    static final Scheduler COMPUTATION;

    static final class IoHolder {
        static final Scheduler DEFAULT = new IoScheduler();
    }

    static final class ComputationHolder {
        static final Scheduler DEFAULT = new ComputationScheduler();
    }

    static {
        COMPUTATION = RxJavaPlugins.initComputationScheduler(new ComputationTask());

        IO = RxJavaPlugins.initIoScheduler(new IOTask());
    }

    /**
     * Utility class.
     */
    private Schedulers() {
        throw new IllegalStateException("No instances!");
    }


    /**
     * Returns a default, shared {@link Scheduler} instance intended for IO-bound work.
     * <p>
     * This can be used for asynchronously performing blocking IO.
     * <p>
     * The implementation is backed by a pool of single-threaded {link ScheduledExecutorService} instances
     * that will try to reuse previoulsy started instances used by the worker
     * returned by {@link Scheduler#createWorker()} but otherwise will start a new backing
     * {link ScheduledExecutorService} instance. Note that this scheduler may create an unbounded number
     * of worker threads that can result in system slowdowns or {@code OutOfMemoryError}. Therefore, for casual uses
     * or when implementing an operator, the Worker instances must be disposed via {@link Scheduler.Worker#dispose()}.
     * <p>
     * It is not recommended to perform computational work on this scheduler.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link Thread.UncaughtExceptionHandler}.
     * <p>
     * You can control certain properties of this standard scheduler via system properties that have to be set
     * before the {@link Schedulers} class is referenced in your code.
     * <p><strong>Supported system properties ({@code System.getProperty()}):</strong>
     * <ul>
     * <li>{@code rx2.io-priority} (int): sets the thread priority of the {@link #io()} Scheduler, default is {@link Thread#NORM_PRIORITY}</li>
     * </ul>
     * <p>
     * The default value of this scheduler can be overridden at initialization time via the
     * {@link RxJavaPlugins#setInitIoSchedulerHandler(Function)} plugin method.
     * Note that due to possible initialization cycles, using any of the other scheduler-returning methods will
     * result in a {@code NullPointerException}.
     * Once the {@link Schedulers} class has been initialized, you can override the returned {@link Scheduler} instance
     * via the {@link RxJavaPlugins#setIoSchedulerHandler(Function)} method.
     * <p>
     * It is possible to create a fresh instance of this scheduler with a custom ThreadFactory, via the
     * {@link RxJavaPlugins#createIoScheduler(ThreadFactory)} method. Note that such custom
     * instances require a manual call to {@link Scheduler#shutdown()} to allow the JVM to exit or the
     * (J2EE) container to unload properly.
     *
     * @return a {@link Scheduler} meant for IO-bound work
     */
    public static Scheduler io() {
        return RxJavaPlugins.onIoScheduler(IO);
    }


    /**
     * Returns a default, shared {@link Scheduler} instance intended for computational work.
     * <p>
     * This can be used for event-loops, processing callbacks and other computational work.
     * <p>
     * It is not recommended to perform blocking, IO-bound work on this scheduler. Use {@link #io()} instead.
     * <p>
     * The default instance has a backing pool of single-threaded {@link ScheduledExecutorService} instances equal to
     * the number of available processors ({@link java.lang.Runtime#availableProcessors()}) to the Java VM.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link java.lang.Thread.UncaughtExceptionHandler}.
     * <p>
     * This type of scheduler is less sensitive to leaking Worker instances, although
     * not disposing a worker that has timed/delayed tasks not cancelled by other means may leak resources and/or
     * execute those tasks "unexpectedly".
     * <p>
     *
     * @return a {@link Scheduler} meant for computation-bound work
     */
    @NonNull
    public static Scheduler computation() {
        return RxJavaPlugins.onComputationScheduler(COMPUTATION);
    }

    static final class IOTask implements Callable<Scheduler> {
        @Override
        public Scheduler call() throws Exception {
            return IoHolder.DEFAULT;
        }
    }

    static final class ComputationTask implements Callable<Scheduler> {
        @Override
        public Scheduler call() throws Exception {
            return ComputationHolder.DEFAULT;
        }
    }


}
