/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.mobile.social.rxjava.android.schedulers;

import android.os.Handler;
import android.os.Looper;

import com.alipay.mobile.social.rxjava.Scheduler;

/**
 * Android-specific Schedulers.
 */
public final class AndroidSchedulers {

    private static final class MainHolder {
        static final Scheduler DEFAULT = new HandlerScheduler(new Handler(Looper.getMainLooper()));
    }

    /**
     * A {@link Scheduler} which executes actions on the Android main thread.
     */
    public static Scheduler mainThread() {
        return MainHolder.DEFAULT;
    }
}
