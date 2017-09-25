package com.alipay.mobile.social.rxjava.support;

/**
 * 抛出RPC异常
 */
public class RpcExceptionUncaughter {

    public static void uncaughtException(final Throwable e) {
//        if (null != e && e instanceof RpcException) {
//            if (Looper.getMainLooper() == Looper.myLooper()) {
//                //如果当前线程为UI主线程，则在子线程中抛出异常，否则框架不会捕获
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
//                    }
//                }).start();
//            } else {
//                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
//            }
//        }
    }

}
