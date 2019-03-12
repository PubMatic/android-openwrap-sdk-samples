/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2019
 * PubMatic, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained herein are
 * proprietary to PubMatic and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from PubMatic.
 * Access to the source code contained herein is hereby forbidden to anyone
 * except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 *
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes information
 * that is confidential and/or proprietary, and is a trade secret, of PubMatic.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC
 * DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN
 * CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO
 * REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.pubmatic.openbid.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pubmatic.openbid.app.mopubevent.MoPubBannerEventHandler;
import com.pubmatic.sdk.common.OpenBidSDK;
import com.pubmatic.sdk.common.POBAdSize;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openbid.banner.POBBannerView;

import java.net.MalformedURLException;
import java.net.URL;

public class BannerActivity extends AppCompatActivity {

    private static final String TAG = "BannerActivity";

    private static final String OPENWRAP_AD_UNIT_ID = "1a4a0c6b94ad4217af017c932c3c898e";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1302;
    private static final String MOPUB_AD_UNIT_ID = "1a4a0c6b94ad4217af017c932c3c898e";

    private POBBannerView banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

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

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, The code below creates an event handler for MoPub ad server.
        MoPubBannerEventHandler eventHandler = new MoPubBannerEventHandler(this, MOPUB_AD_UNIT_ID, POBAdSize.BANNER_SIZE_320x50);

        // Initialise banner view
        banner = findViewById(R.id.banner);
        banner.init(PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ID,
                eventHandler);


        //optional listener to listen banner events
        banner.setListener(new POBBannerViewListener());

        // Call loadAd() on banner instance
        banner.loadAd();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destroy banner before destroying activity
        if (null != banner) {
            banner.destroy();
        }
    }


    class POBBannerViewListener extends POBBannerView.POBBannerViewListener {
        private final String TAG = "POBBannerViewListener";

        // Callback method Notifies that an ad has been successfully loaded and rendered.
        @Override
        public void onAdReceived(POBBannerView view) {
            Log.d(TAG, "Ad Received");
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailed(POBBannerView view, POBError error) {
            Log.e(TAG, error.toString());
        }

        // Callback method notifies ad click
        @Override
        public void onAdClick(POBBannerView view) {
            Log.d(TAG, "onAdClicked");
        }

        // Callback method Notifies that the banner ad view will launch a dialog on top of the current view
        @Override
        public void onAdOpened(POBBannerView view) {
            Log.d(TAG, "Ad Opened");
        }

        // Callback method Notifies that the banner ad view has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(POBBannerView view) {
            Log.d(TAG, "Ad Closed");
        }

    }



}

