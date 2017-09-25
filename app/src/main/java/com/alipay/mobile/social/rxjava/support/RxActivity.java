//package com.alipay.mobile.social.rxjava.support;
//
//import android.os.Bundle;
//
//import com.alipay.mobile.personalbase.ui.SocialBaseActivity;
//import com.alipay.mobile.social.rxjava.disposables.CompositeDisposable;
//import com.alipay.mobile.social.rxjava.disposables.Disposable;
//
//public class RxActivity extends SocialBaseActivity {
//    private CompositeDisposable mCompositeDisposable;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mCompositeDisposable = new CompositeDisposable();
//    }
//
//    @Override
//    public void finish() {
//        super.finish();
//        removeAllSubscription();
//    }
//
//    /**
//     * 添加订阅
//     *
//     * @param disposable
//     */
//    public void addSubscription(Disposable disposable) {
//        if (null != disposable && !disposable.isDisposed()) {
//            if (null == mCompositeDisposable) {
//                mCompositeDisposable = new CompositeDisposable();
//            }
//            mCompositeDisposable.add(disposable);
//        }
//    }
//
//    /**
//     * 取消订阅
//     *
//     * @param disposable
//     */
//    public void removeSubscription(Disposable disposable) {
//        if (null != mCompositeDisposable && !mCompositeDisposable.isDisposed()) {
//            mCompositeDisposable.remove(disposable);
//        }
//    }
//
//    /**
//     * 取消所有的订阅
//     */
//    public void removeAllSubscription() {
//        if (null != mCompositeDisposable && !mCompositeDisposable.isDisposed()) {
//            mCompositeDisposable.dispose();
//        }
//    }
//}