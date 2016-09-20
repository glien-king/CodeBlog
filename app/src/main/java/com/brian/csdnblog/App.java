
package com.brian.csdnblog;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;

import com.brian.csdnblog.activity.MainTabActivity;
import com.brian.csdnblog.manager.BrianUncaughtExceptionHandler;
import com.brian.csdnblog.manager.BrianUncaughtExceptionHandler.OnAppCrashListener;
import com.brian.csdnblog.manager.PushManager;
import com.brian.csdnblog.util.AppInfoUtil;
import com.brian.csdnblog.util.ConfigHelper;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;

public class App extends Application {

    public static final String PROCESS_NAME_MAIN = "com.brian.csdnblog";

    @Override
    public void onCreate() {
        super.onCreate();
        Env.setContext(this);
        ConfigHelper.init(this);

        // init bugly
        CrashReport.initCrashReport(getApplicationContext(), "900033443", false);
        
        if (PROCESS_NAME_MAIN.equals(AppInfoUtil.getProcessName(getApplicationContext()))) {
            MobclickAgent.setCatchUncaughtExceptions(false);
            // 捕捉异常
            if (Config.isDebug) {
//                RefWatcherHelper.install(this); // 内存泄漏检测
                catchException(); // 本地记录crash log
            } else {
                // 友盟crash统计，目前使用bugly
//                MobclickAgent.setCatchUncaughtExceptions(true);
            }
            PushManager.getInstance().initPushMsg(this);
            setStrictModeEnable(Config.isDebug);
        }
    }

    // -------------------异常捕获-----捕获异常后重启应用-----------------//
    private void catchException() {
        Intent intent = new Intent();
        intent.setClassName(getPackageName(), MainTabActivity.class.getName());// 设置程序入口

        // 程序崩溃时触发线程
        BrianUncaughtExceptionHandler uncaughtExceptionHandler = new BrianUncaughtExceptionHandler();
        uncaughtExceptionHandler.setOnCrashListener(new OnAppCrashListener() {
            @Override
            public void onAppCrash() {
                // TODO
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // app start here
        Env.setAppStartTime();
        super.attachBaseContext(base);
    }

    private void setStrictModeEnable(boolean enable) {
        if (!enable) {
            return;
        }
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    public void exit() {
        MobclickAgent.onKillProcess(getApplicationContext());
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
