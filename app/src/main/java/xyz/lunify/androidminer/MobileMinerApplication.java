// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import org.acra.ACRA;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraMailSender;

import static xyz.lunify.androidminer.MainActivity.contextOfApplication;

@AcraCore(buildConfigClass = BuildConfig.class)
@AcraMailSender(mailTo = "hello@lunify.xyz")
public class MobileMinerApplication extends Application implements LifecycleObserver {
    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

        ACRA.init(this);

        SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        Config.initialize(preferences);

        ACRA.getErrorReporter().setEnabled(Config.read(Config.CONFIG_SEND_DEBUG_INFO, "0").equals("1"));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        if(MainActivity.isDeviceMiningBackground())
            Utils.showToast(contextOfApplication, getResources().getString(R.string.miningbackground), Toast.LENGTH_SHORT);
    }
}