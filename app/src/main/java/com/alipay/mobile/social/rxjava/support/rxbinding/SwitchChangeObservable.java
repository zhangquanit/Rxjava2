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
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Example usage:
 * <pre class="prettyprint">
 * Disposable disposable = RxView.switchChanges(tableView)
 * .throttleFirst(1, TimeUnit.SECONDS) //只响应1s内的第一次switch切换事件
 * .observeOn(Schedulers.io())
 * .map(new Funtion()) //rpc请求
 * .observeOn(AndroidSchedulers.mainThread())
 * .subscribe(new Consumer(), new Consumer<Throwable>());
 * </pre>
 */
public class SwitchChangeObservable extends RxViewObservable<Boolean> {
    public SwitchChangeObservable(View view) {
        super(view);
    }

    @Override
    void addViewListener(View view) {
//        APRadioTableView tableView = (APRadioTableView) view;
//        tableView.setOnSwitchListener(new APAbsTableView.OnSwitchListener() {
//            @Override
//            public void onSwitchListener(boolean checked, View view) {
//                postEvent(checked);
//            }
//        });

        Switch switchView = (Switch) view;
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                postEvent(isChecked);
            }
        });
    }
}
