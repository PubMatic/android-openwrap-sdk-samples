package com.pubmatic.openbid.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.pubmatic.openbid.app.dfpevent.DFPInterstitialEventHandler;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.OpenBidSDK;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openbid.interstitial.POBInterstitial;

import java.net.MalformedURLException;
import java.net.URL;

public class DFPInterstitialActivity extends AppCompatActivity {

    private static final String TAG = "DFPInterstitialActivity";

    private static final String OPENWRAP_AD_UNIT_ONE = "/15671365/pm_sdk/PMSDK-Demo-App-Interstitial";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1165;
    private static final String DFP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-Interstitial";

    private POBInterstitial interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        // A valid Play Store Url of an Android application is required.
        POBApplicationInfo appInfo = new POBApplicationInfo();
        try {
            appInfo.setStoreURL(new URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenBidSDK.setApplicationInfo(appInfo);

        // Create an interstitial custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each interstitial ad instance.
        // For example, The code below creates an event handler for DFP ad server.
        DFPInterstitialEventHandler eventHandler = new DFPInterstitialEventHandler(this, DFP_AD_UNIT_ID);

        // Create  interstitial instance by passing activity context and
        interstitial = new POBInterstitial(this, PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ONE,
                eventHandler);

        // Set Optional listener
        interstitial.setListener(new POBInterstitialListener());


        // Load Ad button
        findViewById(R.id.loadAdBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.showAdBtn).setEnabled(false);
                interstitial.loadAd();

            }
        });

        // Show button
        findViewById(R.id.showAdBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInterstitialAd();
            }
        });


    }

    /**
     * To show interstitial ad call this method
     **/
    private void showInterstitialAd() {
        // check if the interstitial is ready
        if (null != interstitial && interstitial.isReady()) {
            // Call show on interstitial
            interstitial.show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != interstitial) {
            interstitial.destroy();
        }

    }

    // Interstitial Ad listener callbacks
    class POBInterstitialListener implements POBInterstitial.POBInterstitialListener {
        private final String TAG = "POBInterstitialListener";
        // Callback method notifies that an ad has been received successfully.
        @Override
        public void onAdReceived(POBInterstitial ad) {
            Log.d(TAG, "onAdReceived");
            //Method gets called when ad gets loaded in container
            //Here, you can show interstitial ad to user
            findViewById(R.id.showAdBtn).setEnabled(true);
        }

        // Callback method notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailed(POBInterstitial ad, POBError error) {
            Log.d(TAG, "onAdFailed :" + error.toString());
            //Method gets called when loadAd fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies that a user interaction will open another app (for example, App Store), leaving the current app.
        @Override
        public void onAppLeaving(POBInterstitial ad) {
            Log.d(TAG, "onAppLeaving");
        }

        // Callback method notifies that the interstitial ad will be presented as a modal on top of the current view controller
        @Override
        public void onAdOpened(POBInterstitial ad) {
            Log.d(TAG, "onAdOpened");
        }

        // Callback method notifies that the interstitial ad has been animated off the screen.
        @Override
        public void onAdClosed(POBInterstitial ad) {
            Log.d(TAG, "onAdClosed");
        }

        // Callback method notifies ad click
        @Override
        public void onAdClicked(POBInterstitial ad) {
            Log.d(TAG, "onAdClicked");
        }
    }


}
