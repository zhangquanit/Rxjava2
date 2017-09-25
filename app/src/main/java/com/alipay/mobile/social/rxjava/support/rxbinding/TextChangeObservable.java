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

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

/**
 * Example usage:
 * <pre class="prettyprint">
 * Disposable disposable = RxView.textChanges(editText)
 * .debounce(300, TimeUnit.MILLISECONDS)
 * .observeOn(Schedulers.io())
 * .map(new Funtion()) //rpc请求
 * .observeOn(AndroidSchedulers.mainThread())
 * .subscribe(new Consumer(), new Consumer<Throwable>());
 * </pre>
 */
public class TextChangeObservable extends RxViewObservable<CharSequence> {
    public TextChangeObservable(final View view) {
        super(view);
    }

    @Override
    void addViewListener(View view) {
        TextView editText = (TextView) view;
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                postEvent(s.toString());
            }
        });
    }
}
