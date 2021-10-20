package com.gmail.rami.abushaqra79;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;

public class MyApplication extends Application implements LifecycleObserver {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public static Context getAppContext() {
        return context;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public static void onAppBackgrounded() {
        FirebaseAuth.getInstance().signOut();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App in foreground
    }
}
