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

import com.alipay.mobile.social.rxjava.annotations.SchedulerSupport;
import com.alipay.mobile.social.rxjava.disposables.Disposable;
import com.alipay.mobile.social.rxjava.exceptions.Exceptions;
import com.alipay.mobile.social.rxjava.exceptions.OnErrorNotImplementedException;
import com.alipay.mobile.social.rxjava.functions.Action;
import com.alipay.mobile.social.rxjava.functions.Consumer;
import com.alipay.mobile.social.rxjava.functions.Function;
import com.alipay.mobile.social.rxjava.functions.Predicate;
import com.alipay.mobile.social.rxjava.internal.functions.Functions;
import com.alipay.mobile.social.rxjava.internal.functions.ObjectHelper;
import com.alipay.mobile.social.rxjava.internal.observers.LambdaObserver;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableCreate;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableDebounceTimed;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableDelay;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableDoOnEach;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableEmpty;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableFilter;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableInterval;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableMap;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableObserveOn;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableSubscribeOn;
import com.alipay.mobile.social.rxjava.internal.operators.observable.ObservableThrottleFirstTimed;
import com.alipay.mobile.social.rxjava.internal.util.ExceptionHelper;
import com.alipay.mobile.social.rxjava.plugins.RxJavaPlugins;
import com.alipay.mobile.social.rxjava.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * The Observable class that is designed similar to the Reactive-Streams Pattern, minus the backpressure,
 * and offers factory methods, intermediate operators and the ability to consume reactive dataflows.
 * <p>
 * Reactive-Streams operates with {@code ObservableSource}s which {@code Observable} extends. Many operators
 * therefore accept general {@code ObservableSource}s directly and allow direct interoperation with other
 * Reactive-Streams implementations.
 * <p>
 * The documentation for this class makes use of marble diagrams. The following legend explains these diagrams:
 * <p>
 * <img width="640" height="317" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/legend.png" alt="">
 * <p>
 * For more information see the <a href="http://reactivex.io/documentation/ObservableSource.html">ReactiveX
 * documentation</a>.
 *
 * @param <T> the type of the items emitted by the Observablesub
 */
public abstract class Observable<T> implements ObservableSource<T> {
    static final int BUFFER_SIZE;

    static {
        BUFFER_SIZE = Math.max(1, Integer.getInteger("rx2.buffer-size", 128));
    }

    private static int bufferSize() {
        return BUFFER_SIZE;
    }

    /**
     * Provides an API (via a cold Observable) that bridges the reactive world with the callback-style world.
     * <p>
     * Example:
     * <pre><code>
     * Observable.&lt;Event&gt;create(emitter -&gt; {
     *     Callback listener = new Callback() {
     *         &#64;Override
     *         public void onEvent(Event e) {
     *             emitter.onNext(e);
     *             if (e.isLast()) {
     *                 emitter.onComplete();
     *             }
     *         }
     *
     *         &#64;Override
     *         public void onFailure(Exception e) {
     *             emitter.onError(e);
     *         }
     *     };
     *
     *     AutoCloseable c = api.someMethod(listener);
     *
     *     emitter.setCancellable(c::close);
     *
     * });
     * </code></pre>
     * <p>
     * <img width="640" height="200" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/create.png" alt="">
     * <p>
     * You should call the ObservableEmitter's onNext, onError and onComplete methods in a serialized fashion. The
     * rest of its methods are thread-safe.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code create} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <T>    the element type
     * @param source the emitter that is called when an Observer subscribes to the returned {@code Observable}
     * @return the new Observable instance
     * @see ObservableOnSubscribe
     * @see ObservableEmitter
     */
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        ObjectHelper.requireNonNull(source, "source is null");
        return RxJavaPlugins.onAssembly(new ObservableCreate<T>(source));
    }

    /**
     * Returns an Observable that emits no items to the {@link Observer} and immediately invokes its
     * {@link Observer#onComplete onComplete} method.
     * <p>
     * <img width="640" height="190" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/empty.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code empty} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <T> the type of the items (ostensibly) emitted by the ObservableSource
     * @return an Observable that emits no items to the {@link Observer} but immediately invokes the
     * {@link Observer}'s {@link Observer#onComplete() onComplete} method
     * @see <a href="http://reactivex.io/documentation/operators/empty-never-throw.html">ReactiveX operators documentation: Empty</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    @SuppressWarnings("unchecked")
    public static <T> Observable<T> empty() {
        return RxJavaPlugins.onAssembly((Observable<T>) ObservableEmpty.INSTANCE);
    }

    /**
     * Returns an Observable that emits a {@code 0L} after the {@code initialDelay} and ever increasing numbers
     * after each {@code period} of time thereafter.
     * <p>
     * <img width="640" height="200" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/timer.p.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code interval} operates by default on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     *
     * @param initialDelay the initial delay time to wait before emitting the first value of 0L
     * @param period       the period of time between emissions of the subsequent numbers
     * @param unit         the time unit for both {@code initialDelay} and {@code period}
     * @return an Observable that emits a 0L after the {@code initialDelay} and ever increasing numbers after
     * each {@code period} of time thereafter
     * @see <a href="http://reactivex.io/documentation/operators/interval.html">ReactiveX operators documentation: Interval</a>
     * @since 1.0.12
     */

    public static Observable<Long> interval(long initialDelay, long period, TimeUnit unit) {
        return interval(initialDelay, period, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits a {@code 0L} after the {@code initialDelay} and ever increasing numbers
     * after each {@code period} of time thereafter, on a specified {@link Scheduler}.
     * <p>
     * <img width="640" height="200" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/timer.ps.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param initialDelay the initial delay time to wait before emitting the first value of 0L
     * @param period       the period of time between emissions of the subsequent numbers
     * @param unit         the time unit for both {@code initialDelay} and {@code period}
     * @param scheduler    the Scheduler on which the waiting happens and items are emitted
     * @return an Observable that emits a 0L after the {@code initialDelay} and ever increasing numbers after
     * each {@code period} of time thereafter, while running on the given Scheduler
     * @see <a href="http://reactivex.io/documentation/operators/interval.html">ReactiveX operators documentation: Interval</a>
     * @since 1.0.12
     */

    public static Observable<Long> interval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        ObjectHelper.requireNonNull(unit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");

        return RxJavaPlugins.onAssembly(new ObservableInterval(Math.max(0L, initialDelay), Math.max(0L, period), unit, scheduler));
    }

    /**
     * Returns an Observable that emits a sequential number every specified interval of time.
     * <p>
     * <img width="640" height="195" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/interval.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code interval} operates by default on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     *
     * @param period the period size in time units (see below)
     * @param unit   time units to use for the interval size
     * @return an Observable that emits a sequential number each time interval
     * @see <a href="http://reactivex.io/documentation/operators/interval.html">ReactiveX operators documentation: Interval</a>
     */


    public static Observable<Long> interval(long period, TimeUnit unit) {
        return interval(period, period, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits a sequential number every specified interval of time, on a
     * specified Scheduler.
     * <p>
     * <img width="640" height="200" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/interval.s.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param period    the period size in time units (see below)
     * @param unit      time units to use for the interval size
     * @param scheduler the Scheduler to use for scheduling the items
     * @return an Observable that emits a sequential number each time interval
     * @see <a href="http://reactivex.io/documentation/operators/interval.html">ReactiveX operators documentation: Interval</a>
     */


    public static Observable<Long> interval(long period, TimeUnit unit, Scheduler scheduler) {
        return interval(period, period, unit, scheduler);
    }

    // ***************************************************************************************************
    // Instance operators
    // ***************************************************************************************************

    /**
     * Returns an Observable that mirrors the source ObservableSource, except that it drops items emitted by the
     * source ObservableSource that are followed by newer items before a timeout value expires. The timer resets on
     * each emission.
     * <p>
     * <em>Note:</em> If items keep being emitted by the source ObservableSource faster than the timeout then no items
     * will be emitted by the resulting ObservableSource.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/debounce.png" alt="">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>This version of {@code debounce} operates by default on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     *
     * @param timeout the time each item has to be "the most recent" of those emitted by the source ObservableSource to
     *                ensure that it's not dropped
     * @param unit    the {@link TimeUnit} for the timeout
     * @return an Observable that filters out items from the source ObservableSource that are too quickly followed by
     * newer items
     * @see <a href="http://reactivex.io/documentation/operators/debounce.html">ReactiveX operators documentation: Debounce</a>
     */

    public final Observable<T> debounce(long timeout, TimeUnit unit) {
        return debounce(timeout, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that mirrors the source ObservableSource, except that it drops items emitted by the
     * source ObservableSource that are followed by newer items before a timeout value expires on a specified
     * Scheduler. The timer resets on each emission.
     * <p>
     * <em>Note:</em> If items keep being emitted by the source ObservableSource faster than the timeout then no items
     * will be emitted by the resulting ObservableSource.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/debounce.s.png" alt="">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param timeout   the time each item has to be "the most recent" of those emitted by the source ObservableSource to
     *                  ensure that it's not dropped
     * @param unit      the unit of time for the specified timeout
     * @param scheduler the {@link Scheduler} to use internally to manage the timers that handle the timeout for each
     *                  item
     * @return an Observable that filters out items from the source ObservableSource that are too quickly followed by
     * newer items
     * @see <a href="http://reactivex.io/documentation/operators/debounce.html">ReactiveX operators documentation: Debounce</a>
     */

    public final Observable<T> debounce(long timeout, TimeUnit unit, Scheduler scheduler) {
        ObjectHelper.requireNonNull(unit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new ObservableDebounceTimed<T>(this, timeout, unit, scheduler));
    }

    /**
     * Returns an Observable that emits the items emitted by the source ObservableSource shifted forward in time by a
     * specified delay. Error notifications from the source ObservableSource are not delayed.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/delay.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>This version of {@code delay} operates by default on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     *
     * @param delay the delay to shift the source by
     * @param unit  the {@link TimeUnit} in which {@code period} is defined
     * @return the source ObservableSource shifted in time by the specified delay
     * @see <a href="http://reactivex.io/documentation/operators/delay.html">ReactiveX operators documentation: Delay</a>
     */


    public final Observable<T> delay(long delay, TimeUnit unit) {
        return delay(delay, unit, Schedulers.computation(), false);
    }

    /**
     * Returns an Observable that emits the items emitted by the source ObservableSource shifted forward in time by a
     * specified delay. Error notifications from the source ObservableSource are not delayed.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/delay.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>This version of {@code delay} operates by default on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     *
     * @param delay      the delay to shift the source by
     * @param unit       the {@link TimeUnit} in which {@code period} is defined
     * @param delayError if true, the upstream exception is signalled with the given delay, after all preceding normal elements,
     *                   if false, the upstream exception is signalled immediately
     * @return the source ObservableSource shifted in time by the specified delay
     * @see <a href="http://reactivex.io/documentation/operators/delay.html">ReactiveX operators documentation: Delay</a>
     */


    public final Observable<T> delay(long delay, TimeUnit unit, boolean delayError) {
        return delay(delay, unit, Schedulers.computation(), delayError);
    }

    /**
     * Returns an Observable that emits the items emitted by the source ObservableSource shifted forward in time by a
     * specified delay. Error notifications from the source ObservableSource are not delayed.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/delay.s.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param delay     the delay to shift the source by
     * @param unit      the time unit of {@code delay}
     * @param scheduler the {@link Scheduler} to use for delaying
     * @return the source ObservableSource shifted in time by the specified delay
     * @see <a href="http://reactivex.io/documentation/operators/delay.html">ReactiveX operators documentation: Delay</a>
     */


    public final Observable<T> delay(long delay, TimeUnit unit, Scheduler scheduler) {
        return delay(delay, unit, scheduler, false);
    }

    /**
     * Returns an Observable that emits the items emitted by the source ObservableSource shifted forward in time by a
     * specified delay. Error notifications from the source ObservableSource are not delayed.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/delay.s.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param delay      the delay to shift the source by
     * @param unit       the time unit of {@code delay}
     * @param scheduler  the {@link Scheduler} to use for delaying
     * @param delayError if true, the upstream exception is signalled with the given delay, after all preceding normal elements,
     *                   if false, the upstream exception is signalled immediately
     * @return the source ObservableSource shifted in time by the specified delay
     * @see <a href="http://reactivex.io/documentation/operators/delay.html">ReactiveX operators documentation: Delay</a>
     */


    public final Observable<T> delay(long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        ObjectHelper.requireNonNull(unit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");

        return RxJavaPlugins.onAssembly(new ObservableDelay<T>(this, delay, unit, scheduler, delayError));
    }

    /**
     * Modifies the source ObservableSource so that it invokes an action when it calls {@code onComplete} or
     * {@code onError}.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/doOnTerminate.png" alt="">
     * <p>
     * This differs from {@code doAfterTerminate} in that this happens <em>before</em> the {@code onComplete} or
     * {@code onError} notification.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code doOnTerminate} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param onTerminate the action to invoke when the source ObservableSource calls {@code onComplete} or {@code onError}
     * @return the source ObservableSource with the side-effecting behavior applied
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators documentation: Do</a>
     * @see #doAfterTerminate(Action)
     */
    @SchedulerSupport(SchedulerSupport.NONE)
    public final Observable<T> doOnTerminate(final Action onTerminate) {
        ObjectHelper.requireNonNull(onTerminate, "onTerminate is null");
        return doOnEach(Functions.emptyConsumer(),
                Functions.actionConsumer(onTerminate), onTerminate,
                Functions.EMPTY_ACTION);
    }

    /**
     * Registers an {@link Action} to be called when this ObservableSource invokes either
     * {@link Observer#onComplete onComplete} or {@link Observer#onError onError}.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/doAfterTerminate.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code doAfterTerminate} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param onFinally an {@link Action} to be invoked when the source ObservableSource finishes
     * @return an Observable that emits the same items as the source ObservableSource, then invokes the
     * {@link Action}
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators documentation: Do</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Observable<T> doAfterTerminate(Action onFinally) {
        ObjectHelper.requireNonNull(onFinally, "onFinally is null");
        return doOnEach(Functions.emptyConsumer(), Functions.emptyConsumer(), Functions.EMPTY_ACTION, onFinally);
    }


    /**
     * Calls the appropriate onXXX consumer (shared between all subscribers) whenever a signal with the same type
     * passes through, before forwarding them to downstream.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/doOnEach.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code doOnEach} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @return the source ObservableSource with the side-effecting behavior applied
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators documentation: Do</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    private Observable<T> doOnEach(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete, Action onAfterTerminate) {
        ObjectHelper.requireNonNull(onNext, "onNext is null");
        ObjectHelper.requireNonNull(onError, "onError is null");
        ObjectHelper.requireNonNull(onComplete, "onComplete is null");
        ObjectHelper.requireNonNull(onAfterTerminate, "onAfterTerminate is null");
        return RxJavaPlugins.onAssembly(new ObservableDoOnEach<T>(this, onNext, onError, onComplete, onAfterTerminate));
    }

    /**
     * Modifies the source ObservableSource so that it invokes an action when it calls {@code onNext}.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/doOnNext.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code doOnNext} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param onNext the action to invoke when the source ObservableSource calls {@code onNext}
     * @return the source ObservableSource with the side-effecting behavior applied
     * @see <a href="http://reactivex.io/documentation/operators/do.html">ReactiveX operators documentation: Do</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Observable<T> doOnNext(Consumer<? super T> onNext) {
        return doOnEach(onNext, Functions.emptyConsumer(), Functions.EMPTY_ACTION, Functions.EMPTY_ACTION);
    }

    /**
     * Filters items emitted by an ObservableSource by only emitting those that satisfy a specified predicate.
     * <p>
     * <img width="640" height="310" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/filter.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code filter} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param predicate a function that evaluates each item emitted by the source ObservableSource, returning {@code true}
     *                  if it passes the filter
     * @return an Observable that emits only those items emitted by the source ObservableSource that the filter
     * evaluates as {@code true}
     * @see <a href="http://reactivex.io/documentation/operators/filter.html">ReactiveX operators documentation: Filter</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Observable<T> filter(Predicate<? super T> predicate) {
        ObjectHelper.requireNonNull(predicate, "predicate is null");
        return RxJavaPlugins.onAssembly(new ObservableFilter<T>(this, predicate));
    }

    /**
     * Returns an Observable that applies a specified function to each item emitted by the source ObservableSource and
     * emits the results of these function applications.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/map.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code map} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <R>    the output type
     * @param mapper a function to apply to each item emitted by the ObservableSource
     * @return an Observable that emits the items from the source ObservableSource, transformed by the specified
     * function
     * @see <a href="http://reactivex.io/documentation/operators/map.html">ReactiveX operators documentation: Map</a>
     */
    @SchedulerSupport(SchedulerSupport.NONE)
    public final <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        ObjectHelper.requireNonNull(mapper, "mapper is null");
        return RxJavaPlugins.onAssembly(new ObservableMap<T, R>(this, mapper));
    }

    /**
     * Modifies an ObservableSource to perform its emissions and notifications on a specified {@link Scheduler}
     * <p>
     * <p>Note that onError notifications will cut ahead of onNext notifications on the emission thread if Scheduler is truly
     * asynchronous. If strict event ordering is required, consider using the {@link #observeOn(Scheduler, boolean)} overload.
     * <p>
     * <img width="640" height="308" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/observeOn.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     * <p>"Island size" indicates how large chunks the unbounded buffer allocates to store the excess elements waiting to be consumed
     * on the other side of the asynchronous boundary.
     *
     * @param scheduler the {@link Scheduler} to notify {@link Observer}s on
     * @return the source ObservableSource modified so that its {@link Observer}s are notified on the specified
     * {@link Scheduler}
     * @see <a href="http://reactivex.io/documentation/operators/observeon.html">ReactiveX operators documentation: ObserveOn</a>
     * @see <a href="http://www.grahamlea.com/2014/07/rxjava-threading-examples/">RxJava Threading Examples</a>
     * @see #subscribeOn
     * @see #observeOn(Scheduler, boolean)
     * @see #observeOn(Scheduler, boolean, int)
     */
    public final Observable<T> observeOn(Scheduler scheduler) {
        return observeOn(scheduler, false, bufferSize());
    }

    /**
     * Modifies an ObservableSource to perform its emissions and notifications on a specified {@link Scheduler}
     * <p>
     * <img width="640" height="308" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/observeOn.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     * <p>"Island size" indicates how large chunks the unbounded buffer allocates to store the excess elements waiting to be consumed
     * on the other side of the asynchronous boundary.
     *
     * @param scheduler  the {@link Scheduler} to notify {@link Observer}s on
     * @param delayError indicates if the onError notification may not cut ahead of onNext notification on the other side of the
     *                   scheduling boundary. If true a sequence ending in onError will be replayed in the same order as was received
     *                   from upstream
     * @return the source ObservableSource modified so that its {@link Observer}s are notified on the specified
     * {@link Scheduler}
     * @see <a href="http://reactivex.io/documentation/operators/observeon.html">ReactiveX operators documentation: ObserveOn</a>
     * @see <a href="http://www.grahamlea.com/2014/07/rxjava-threading-examples/">RxJava Threading Examples</a>
     * @see #subscribeOn
     * @see #observeOn(Scheduler)
     * @see #observeOn(Scheduler, boolean, int)
     */
    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError) {
        return observeOn(scheduler, delayError, bufferSize());
    }

    /**
     * Modifies an ObservableSource to perform its emissions and notifications on a specified {@link Scheduler},
     * asynchronously with an unbounded buffer of configurable "island size" and optionally delays onError notifications.
     * <p>
     * <img width="640" height="308" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/observeOn.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     * <p>"Island size" indicates how large chunks the unbounded buffer allocates to store the excess elements waiting to be consumed
     * on the other side of the asynchronous boundary. Values below 16 are not recommended in performance sensitive scenarios.
     *
     * @param scheduler  the {@link Scheduler} to notify {@link Observer}s on
     * @param delayError indicates if the onError notification may not cut ahead of onNext notification on the other side of the
     *                   scheduling boundary. If true a sequence ending in onError will be replayed in the same order as was received
     *                   from upstream
     * @param bufferSize the size of the buffer.
     * @return the source ObservableSource modified so that its {@link Observer}s are notified on the specified
     * {@link Scheduler}
     * @see <a href="http://reactivex.io/documentation/operators/observeon.html">ReactiveX operators documentation: ObserveOn</a>
     * @see <a href="http://www.grahamlea.com/2014/07/rxjava-threading-examples/">RxJava Threading Examples</a>
     * @see #subscribeOn
     * @see #observeOn(Scheduler)
     * @see #observeOn(Scheduler, boolean)
     */
    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
        return RxJavaPlugins.onAssembly(new ObservableObserveOn<T>(this, scheduler, delayError, bufferSize));
    }

    /**
     * Subscribes to an ObservableSource and provides a callback to handle the items it emits.
     * <p>
     * If the Observable emits an error, it is wrapped into an
     * {@link OnErrorNotImplementedException OnErrorNotImplementedException}
     * and routed to the RxJavaPlugins.onError handler.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code subscribe} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param onNext the {@code Consumer<T>} you have designed to accept emissions from the ObservableSource
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     * the ObservableSource has finished sending them
     * @throws NullPointerException if {@code onNext} is null
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Disposable subscribe(Consumer<? super T> onNext) {
        return subscribe(onNext, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer());
    }

    /**
     * Subscribes to an ObservableSource and provides callbacks to handle the items it emits and any error
     * notification it issues.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code subscribe} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param onNext  the {@code Consumer<T>} you have designed to accept emissions from the ObservableSource
     * @param onError the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *                ObservableSource
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     * the ObservableSource has finished sending them
     * @throws NullPointerException if {@code onNext} is null, or
     *                              if {@code onError} is null
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return subscribe(onNext, onError, Functions.EMPTY_ACTION, Functions.emptyConsumer());
    }

    /**
     * Subscribes to an ObservableSource and provides callbacks to handle the items it emits and any error or
     * completion notification it issues.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code subscribe} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param onNext     the {@code Consumer<T>} you have designed to accept emissions from the ObservableSource
     * @param onError    the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *                   ObservableSource
     * @param onComplete the {@code Action} you have designed to accept a completion notification from the
     *                   ObservableSource
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     * the ObservableSource has finished sending them
     * @throws NullPointerException if {@code onNext} is null, or
     *                              if {@code onError} is null, or
     *                              if {@code onComplete} is null
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                                      Action onComplete) {
        return subscribe(onNext, onError, onComplete, Functions.emptyConsumer());
    }

    /**
     * Subscribes to an ObservableSource and provides callbacks to handle the items it emits and any error or
     * completion notification it issues.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code subscribe} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param onNext      the {@code Consumer<T>} you have designed to accept emissions from the ObservableSource
     * @param onError     the {@code Consumer<Throwable>} you have designed to accept any error notification from the
     *                    ObservableSource
     * @param onComplete  the {@code Action} you have designed to accept a completion notification from the
     *                    ObservableSource
     * @param onSubscribe the {@code Consumer} that receives the upstream's Disposable
     * @return a {@link Disposable} reference with which the caller can stop receiving items before
     * the ObservableSource has finished sending them
     * @throws NullPointerException if {@code onNext} is null, or
     *                              if {@code onError} is null, or
     *                              if {@code onComplete} is null
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError,
                                      Action onComplete, Consumer<? super Disposable> onSubscribe) {
        if (null == onNext) {
            onNext = Functions.emptyConsumer();
        }
        if (null == onError) {
            onError = Functions.emptyConsumer();
        }
        if (null == onComplete) {
            onComplete = Functions.EMPTY_ACTION;
        }
        if (null == onSubscribe) {
            onSubscribe = Functions.emptyConsumer();
        }

        LambdaObserver<T> ls = new LambdaObserver<T>(onNext, onError, onComplete, onSubscribe);
        subscribe(ls);

        return ls;
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @Override
    public final void subscribe(Observer<? super T> observer) {
        ObjectHelper.requireNonNull(observer, "observer is null");
        try {
            observer = RxJavaPlugins.onSubscribe(this, observer);

            ObjectHelper.requireNonNull(observer, "Plugin returned null Observer");

            subscribeActual(observer);
        } catch (NullPointerException e) { // NOPMD
            throw e;
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // can't call onError because no way to know if a Disposable has been set or not
            // can't call onSubscribe because the call might have set a Subscription already
            RxJavaPlugins.onError(e);

            NullPointerException npe = new NullPointerException("Actually not, but can't throw other exceptions due to RS");
            npe.initCause(e);
            throw npe;
        }
    }

    /**
     * Operator implementations (both source and intermediate) should implement this method that
     * performs the necessary business logic.
     * <p>There is no need to call any of the plugin hooks on the current Observable instance or
     * the Subscriber.
     *
     * @param observer the incoming Observer, never null
     */
    protected abstract void subscribeActual(Observer<? super T> observer);

    /**
     * Asynchronously subscribes Observers to this ObservableSource on the specified {@link Scheduler}.
     * <p>
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/subscribeOn.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param scheduler the {@link Scheduler} to perform subscription actions on
     * @return the source ObservableSource modified so that its subscriptions happen on the
     * specified {@link Scheduler}
     * @see <a href="http://reactivex.io/documentation/operators/subscribeon.html">ReactiveX operators documentation: SubscribeOn</a>
     * @see <a href="http://www.grahamlea.com/2014/07/rxjava-threading-examples/">RxJava Threading Examples</a>
     * @see #observeOn
     */


    public final Observable<T> subscribeOn(Scheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new ObservableSubscribeOn<T>(this, scheduler));
    }

    /**
     * Returns an Observable that emits only the first item emitted by the source ObservableSource during sequential
     * time windows of a specified duration.
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/throttleFirst.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code throttleFirst} operates by default on the {@code computation} {@link Scheduler}.</dd>
     * </dl>
     *
     * @param windowDuration time to wait before emitting another item after emitting the last item
     * @param unit           the unit of time of {@code windowDuration}
     * @return an Observable that performs the throttle operation
     * @see <a href="http://reactivex.io/documentation/operators/sample.html">ReactiveX operators documentation: Sample</a>
     */


    public final Observable<T> throttleFirst(long windowDuration, TimeUnit unit) {
        return throttleFirst(windowDuration, unit, Schedulers.computation());
    }

    /**
     * Returns an Observable that emits only the first item emitted by the source ObservableSource during sequential
     * time windows of a specified duration, where the windows are managed by a specified Scheduler.
     * <img width="640" height="305" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/throttleFirst.s.png" alt="">
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>You specify which {@link Scheduler} this operator will use</dd>
     * </dl>
     *
     * @param skipDuration time to wait before emitting another item after emitting the last item
     * @param unit         the unit of time of {@code skipDuration}
     * @param scheduler    the {@link Scheduler} to use internally to manage the timers that handle timeout for each
     *                     event
     * @return an Observable that performs the throttle operation
     * @see <a href="http://reactivex.io/documentation/operators/sample.html">ReactiveX operators documentation: Sample</a>
     */


    public final Observable<T> throttleFirst(long skipDuration, TimeUnit unit, Scheduler scheduler) {
        ObjectHelper.requireNonNull(unit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new ObservableThrottleFirstTimed<T>(this, skipDuration, unit, scheduler));
    }

    /**
     * Calls the specified converter function during assembly time and returns its resulting value.
     * <p>
     * This allows fluent conversion to any other type.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code to} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param <R>       the resulting object type
     * @param converter the function that receives the current Observable instance and returns a value
     * @return the value returned by the function
     */

    @SchedulerSupport(SchedulerSupport.NONE)
    public final <R> R to(Function<? super Observable<T>, R> converter) {
        try {
            return ObjectHelper.requireNonNull(converter, "converter is null").apply(this);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

}
