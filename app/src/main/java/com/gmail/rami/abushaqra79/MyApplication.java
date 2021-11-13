package com.gmail.rami.abushaqra79;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * This class is for getting application context.
 */
public class MyApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
