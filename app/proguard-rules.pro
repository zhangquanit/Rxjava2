# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zhangquan/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

## Rxjava start
-keep public class com.alipay.mobile.social.rxjava.Observable {
   !private <fields>;
   !private <methods>;
}

-keep public interface com.alipay.mobile.social.rxjava.Emitter {
   *;
}
-keep public interface com.alipay.mobile.social.rxjava.ObservableEmitter {
   *;
}

-keep public interface com.alipay.mobile.social.rxjava.ObservableOnSubscribe {
   *;
}

-keep public interface com.alipay.mobile.social.rxjava.Observer {
   *;
}

-keep public class com.alipay.mobile.social.rxjava.android.schedulers.AndroidSchedulers {
   *;
}

-keep public class com.alipay.mobile.social.rxjava.disposables.CompositeDisposable{
   !private <fields>;
   !private <methods>;
}

-keep public interface com.alipay.mobile.social.rxjava.disposables.Disposable{
   *;
}

-keep public interface com.alipay.mobile.social.rxjava.functions.*{
   *;
}

-keep public class com.alipay.mobile.social.rxjava.internal.functions.Functions {
   !private <fields>;
   !private <methods>;
}

-keep public class com.alipay.mobile.social.rxjava.schedulers.* {
   !private <fields>;
   !private <methods>;
}

-keep public class com.alipay.mobile.social.rxjava.subjects.* {
   !private <fields>;
   !private <methods>;
}

-keep public class com.alipay.mobile.social.rxjava.support.RpcExceptionUncaughter {
   *;
}
-keep public class com.alipay.mobile.social.rxjava.support.rxbinding.** {
   public *;
}
-keep public class com.alipay.mobile.social.rxjava.support.RxActivity {
   public *;
}
## Rxjava end
