package com.cspl.paynpark;

import android.app.Application;

public class SDKDemoApp extends Application {
    private static final String TAG = SDKDemoApp.class.getSimpleName();

    private static SDKDemoApp instance;

    public static SDKDemoApp getInstance(){
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
