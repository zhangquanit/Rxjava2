package demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.alipay.mobile.social.rxjava.Observable;
import com.alipay.mobile.social.rxjava.ObservableEmitter;
import com.alipay.mobile.social.rxjava.ObservableOnSubscribe;
import com.alipay.mobile.social.rxjava.android.schedulers.AndroidSchedulers;
import com.alipay.mobile.social.rxjava.disposables.CompositeDisposable;
import com.alipay.mobile.social.rxjava.functions.Consumer;
import com.alipay.mobile.social.rxjava.schedulers.Schedulers;
import com.alipay.mobile.social.rxjava.support.rxbinding.RxView;
import com.rxjava2.demo.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ExecutorSchedulerTest extends Activity {
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
//    private ExecutorService executorService = Executors.newScheduledThreadPool(2);
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private int num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.executor_scheduler);
        compositeDisposable.add(RxView.clicks(findViewById(R.id.btn_executorScheudler))
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
                        doExecutorSchedulerTest();
                    }
                }));
        compositeDisposable.add(RxView.clicks(findViewById(R.id.btn_executorScheudler2))
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<View>() {
                    @Override
                    public void accept(View view) throws Exception {
                        doExecutorSchedulerTest();
                    }
                }));
    }

    private void doExecutorSchedulerTest() {


        for (int i = 0; i < 2; i++) {
            final int num =  i;
            compositeDisposable.add(Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                    System.out.println(num + " subscribe start " + Thread.currentThread().getName());
                    Thread.sleep(5 * 1000);
                    emitter.onNext("abc->" + num);
                    System.out.println(num + " subscribe end");
                    emitter.onComplete();


                }
            }).subscribeOn(Schedulers.from(executorService))
                    .observeOn(Schedulers.io())
                    .doOnNext(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            System.out.println("doOnNext " + s + " " + Thread.currentThread().getName());
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            System.out.println("onNext " + s + " " + Thread.currentThread().getName());
                        }
                    }));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != compositeDisposable && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }
}
