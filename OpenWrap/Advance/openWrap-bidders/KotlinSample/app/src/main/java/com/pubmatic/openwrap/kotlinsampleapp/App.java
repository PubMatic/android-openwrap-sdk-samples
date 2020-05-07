package com.pubmatic.openwrap.kotlinsampleapp;

import android.app.Application;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AudienceNetworkAds.initialize(this);
        AdSettings.addTestDevice("a58e4475-c8e1-4471-8cf9-a6d8aa062c5a");
    }
}
