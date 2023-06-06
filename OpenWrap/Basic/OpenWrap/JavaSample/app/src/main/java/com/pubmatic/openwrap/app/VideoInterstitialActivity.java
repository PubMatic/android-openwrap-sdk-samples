/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2023 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access or to such other persons whom are directly authorized by PubMatic to access the source code and are subject to confidentiality and nondisclosure obligations with respect to the source code.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PUBMATIC IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.pubmatic.openwrap.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitial;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoInterstitialActivity extends AppCompatActivity {

    private static final String OPENWRAP_AD_UNIT_ONE = "OpenWrapInterstitialAdUnit";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1757;

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
        OpenWrapSDK.setApplicationInfo(appInfo);


        // Create  interstitial instance by passing activity context
        interstitial = new POBInterstitial(this, PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ONE);

        // Set Optional listener
        interstitial.setListener(new VideoInterstitialActivity.POBInterstitialListener());

        // Set the optional listener to get the video events
        interstitial.setVideoListener(new POBInterstitialVideoListener());

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

    /**
     * Implementation class to receive the callback of VAST based video from Interstitial ad
     */
    class POBInterstitialVideoListener extends POBInterstitial.POBVideoListener {
        private final String TAG = "POBVideoListener";

        // Callback method notifies that playback of the VAST video has been completed
        @Override
        public void onVideoPlaybackCompleted(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onVideoPlaybackCompleted");
        }

    }

    // Interstitial Ad listener callbacks
    class POBInterstitialListener extends POBInterstitial.POBInterstitialListener {
        private final String TAG = "POBInterstitialListener";
        // Callback method notifies that an ad has been received successfully.
        @Override
        public void onAdReceived(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdReceived");
            //Method gets called when ad gets loaded in container
            //Here, you can show interstitial ad to user
            findViewById(R.id.showAdBtn).setEnabled(true);
        }

        // Callback method notifies an error encountered while loading an ad.
        @Override
        public void onAdFailedToLoad(@NonNull POBInterstitial ad, @NonNull POBError error) {
            Log.e(TAG, "onAdFailedToLoad : Ad failed to load with error -" + error.toString());
            //Method gets called when loadAd fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies an error encountered while showing an ad.
        @Override
        public void onAdFailedToShow(@NonNull POBInterstitial ad, @NonNull POBError error) {
            Log.e(TAG, "onAdFailedToShow : Ad failed to show with error -" + error.toString());
            //Method gets called when loadAd fails to show ad
            //Here, you can put logger and see why ad failed to show
        }

        // Callback method notifies that the interstitial ad will be presented as a modal on top of the current view controller
        @Override
        public void onAdOpened(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdOpened");
        }

        // Callback method notifies that the interstitial ad has been animated off the screen.
        @Override
        public void onAdClosed(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdClosed");
        }

        // Callback method notifies ad click
        @Override
        public void onAdClicked(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAdClicked");
        }

        // Callback method notifies ad expiration
        @Override
        public void onAdExpired(@NonNull POBInterstitial ad){
            Log.d(TAG, "onAdExpired");
        }

        // Callback method Notifies whenever current app goes in the background due to user click
        @Override
        public void onAppLeaving(@NonNull POBInterstitial ad) {
            Log.d(TAG, "onAppLeaving");
        }
    }
}