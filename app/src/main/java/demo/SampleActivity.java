package demo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.alipay.mobile.social.rxjava.Observable;
import com.alipay.mobile.social.rxjava.ObservableEmitter;
import com.alipay.mobile.social.rxjava.ObservableOnSubscribe;
import com.alipay.mobile.social.rxjava.android.schedulers.AndroidSchedulers;
import com.alipay.mobile.social.rxjava.disposables.CompositeDisposable;
import com.alipay.mobile.social.rxjava.disposables.Disposable;
import com.alipay.mobile.social.rxjava.functions.Action;
import com.alipay.mobile.social.rxjava.functions.Consumer;
import com.alipay.mobile.social.rxjava.functions.Function;
import com.alipay.mobile.social.rxjava.schedulers.Schedulers;
import com.alipay.mobile.social.rxjava.subjects.PublishSubject;
import com.alipay.mobile.social.rxjava.support.rxbinding.RxView;
import com.rxjava2.demo.R;

import java.util.concurrent.TimeUnit;

import static com.rxjava2.demo.R.id.textView;


public class SampleActivity extends Activity {
    private View btn1, btn2;
    private Switch switchBtn;
    private TextView inputTextView;
    private EditText et_input;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        switchBtn = (Switch) findViewById(R.id.switchBtn);
        inputTextView = (TextView) findViewById(textView);
        et_input = (EditText) findViewById(R.id.et);

//        doSth();
        ioSchedulerTest();
        rxbindingTest();
        cancelLastRequestTest();
        computionSchedulerTest();
        executorSchedulerTest();
    }

    Disposable subscribe;

    private void ioSchedulerTest() {
        if (null != subscribe && !subscribe.isDisposed()) {
            subscribe.dispose();
        }
        subscribe = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                System.out.println("subscribe start");
                emitter.onNext("a");
                Thread.sleep(5 * 1000);
                emitter.onComplete();
                System.out.println("subscribe end");
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("onNext " + s);
                    }
                });
    }

    private void computionSchedulerTest() {
        RxView.clicks(findViewById(R.id.btn_computionScheduler))
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
                        startActivity(new Intent(SampleActivity.this, ComputionSchedulerTest.class));

                    }
                });
    }

    private void executorSchedulerTest() {
        RxView.clicks(findViewById(R.id.btn_executorScheduler))
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
                        startActivity(new Intent(SampleActivity.this, ExecutorSchedulerTest.class));

                    }
                });
    }

    private int num = 0;

    private void rxbindingTest() {
        /**
         * 绑定点击事件
         */
        RxView.clicks(btn1)
                .subscribe(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
                        System.out.println("btn1 onNext ");
//                        doSth();
                        ioSchedulerTest();
                    }
                });

        /**
         * 绑定点击事件(防止重复点击)
         */
        RxView.clicks(btn2)
                .throttleFirst(1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
                        System.out.println("btn2 doOnNext " + Thread.currentThread().getName());

                    }
                })
                .observeOn(Schedulers.io())
                .map(new Function<View, Boolean>() {
                    @Override
                    public Boolean apply(View view) throws Exception {
                        System.out.println("btn2 map " + Thread.currentThread().getName());

                        return doRpcExecution();
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("btn2 doOnTerminate " + Thread.currentThread().getName());

                    }
                })
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("btn2 doAfterTerminate " + Thread.currentThread().getName());

                    }
                })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean rpcResult) throws Exception {
                        System.out.println("btn2 onNext " + rpcResult + " " + Thread.currentThread().getName());

                        num++;
                        if (num == 2) {
//                            Integer.valueOf("A");
                            num = 0;
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("btn2 onError " + throwable.getMessage() + " " + Thread.currentThread().getName());
                    }
                });

        //switch开关变化
        RxView.switchChanges(switchBtn)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isChecked) throws Exception {
                        System.out.println("switch  isChecked=" + isChecked);
                    }
                });

        //输入监听
        RxView.textChanges(et_input)
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(CharSequence charSequence) throws Exception {
                        inputTextView.setText(charSequence);
                    }
                });

    }

    private void doSth() {
        Observable.create(new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(ObservableEmitter<View> emitter) throws Exception {
                emitter.onNext(new View(SampleActivity.this));

                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
                        System.out.println("btn2 doOnNext " + Thread.currentThread().getName());
                        Integer.valueOf("a");
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Function<View, Boolean>() {
                    @Override
                    public Boolean apply(View view) throws Exception {
                        System.out.println("btn2 map " + Thread.currentThread().getName());
                        return doRpcExecution();
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("btn2 doOnTerminate " + Thread.currentThread().getName());
                    }
                })
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("btn2 doAfterTerminate " + Thread.currentThread().getName());

                    }
                })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean rpcResult) throws Exception {
                        System.out.println("btn2 onNext " + rpcResult + " " + Thread.currentThread().getName());
                        num++;
                        if (num == 1) {
//                            Integer.valueOf("A");
                            num = 0;
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("btn2 onError " + throwable.getMessage() + " " + Thread.currentThread().getName());
                    }
                });
    }

    /**
     * 取消上次请求
     */
    private void cancelLastRequestTest() {
        findViewById(R.id.btn_resubsribe)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doRequest();
                    }
                });

    }


    private boolean doRpcExecution() {

        return true;
    }

    private void doRpcExecution2() throws InterruptedException {
        Thread.sleep(5 * 1000);
    }

    Disposable rpcSubscription;

    private void doRequest() {
        /**
         * 异步线程，注销监听，防止内存泄漏
         * 调用流程：
         * dispose--->Scheduler.DisposeTask.dispose--shutdown
         */

        //取消上次请求
        if (null != rpcSubscription && !rpcSubscription.isDisposed()) {
            rpcSubscription.dispose();
        }
        //执行本次请求
        rpcSubscription = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                System.out.println("subscribe start");
                doRpcExecution2();
                System.out.println("subscribe end");
                emitter.onNext(true);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        System.out.println("onNext " + aBoolean);
                    }
                });

        //普通线程
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("thread start");
//                try {
//                    Thread.sleep(5 * 1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("thread end");
//            }
//        }).start();
    }


    //##################### AsynckTask ######################
    private void asyncTaskTest() {
        //AsyncTask执行
        AsyncTask<Void, Integer, Boolean> asyncTask = new AsyncTask<Void, Integer, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                //在子线程中执行rpc请求
                boolean successful = doRpcExecution();
                return successful;
            }

            @Override
            protected void onPostExecute(Boolean rcpResult) {
                //在主线程中处理rpc请求结果
            }
        };
        asyncTask.execute();

        //rxjava实现AsyncTask功能
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                //在子线程中执行rpc请求
                boolean successful = doRpcExecution();
                emitter.onNext(successful);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean rcpResult) throws Exception {
                        //在主线程中处理rpc请求结果
                    }
                });
    }

    //##################### PublishSubject 一对多 ######################
    private void subjectTest() {
        PublishSubject<String> publishSubject = PublishSubject.create();

        Disposable subscribe = publishSubject.observeOn(Schedulers.io())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("subject doOnext " + s);
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("subject onNext " + s);
                    }
                });
        publishSubject.onNext("a");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != rpcSubscription && !rpcSubscription.isDisposed()) {
            rpcSubscription.dispose();
        }
    }
}
