//package com.alipay.mobile.social.rxjava.support;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//
//import com.alipay.mobile.social.rxjava.disposables.CompositeDisposable;
//import com.alipay.mobile.social.rxjava.disposables.Disposable;
//
//
//public class RxFragment extends Fragment {
//
//    private CompositeDisposable mCompositeDisposable;
//
//    @Override
//    public void onCreate( Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mCompositeDisposable = new CompositeDisposable();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
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
