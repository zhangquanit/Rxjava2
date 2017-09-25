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
package com.alipay.mobile.social.rxjava.support.rxbinding;

import android.view.View;

/**
 * Example usage:
 * <pre class="prettyprint">
 * Disposable disposable = RxView.clicks(clickView)
 * .throttleFirst(1, TimeUnit.SECONDS) //只响应1s内的第一次点击事件
 * .observeOn(Schedulers.io())
 * .map(new Funtion()) //rpc请求
 * .observeOn(AndroidSchedulers.mainThread())
 * .subscribe(new Consumer(), new Consumer<Throwable>());
 * </pre>
 */
public class ViewClickObservable extends RxViewObservable<View> {

    public ViewClickObservable(View view) {
        super(view);
    }

    @Override
    void addViewListener(final View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postEvent(v);
            }
        });
    }
}
