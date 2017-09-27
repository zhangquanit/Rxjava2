package demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import com.alipay.mobile.social.rxjava.disposables.CompositeDisposable;
import com.alipay.mobile.social.rxjava.functions.Consumer;
import com.alipay.mobile.social.rxjava.support.rxbinding.RxView;
import com.rxjava2.demo.R;

import java.util.concurrent.TimeUnit;


public class ComputionSchedulerTest extends Activity {
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rxbinding);
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        for (int i = 0; i < 10; i++) {
            Button button = new Button(this);
            button.setText("num " + i);
            container.addView(button);
            compositeDisposable.add(RxView.clicks(button)
                    .throttleFirst(1, TimeUnit.SECONDS)
//                    .debounce(1, TimeUnit.SECONDS)
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            System.out.println("onClick ");
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
