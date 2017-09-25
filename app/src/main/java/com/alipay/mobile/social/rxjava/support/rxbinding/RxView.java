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
import android.widget.Switch;
import android.widget.TextView;


public class RxView {

    /**
     * 点击事件
     *
     * @param view
     * @return
     */
    public static ViewClickObservable clicks(View view) {
        return new ViewClickObservable(view);

    }

    /**
     * 搜索框输入变化监听
     *
     * @param editText
     * @return
     */
    public static TextChangeObservable textChanges(TextView editText) {
        return new TextChangeObservable(editText);
    }

    /**
     * switch 切换监听
     *
     * @param switchView
     * @return
     */
//    public static SwitchChangeObservable switchChanges(APRadioTableView switchView) {
//        return new SwitchChangeObservable(switchView);
//    }
        public static SwitchChangeObservable switchChanges(Switch switchView) {
        return new SwitchChangeObservable(switchView);
    }


}
