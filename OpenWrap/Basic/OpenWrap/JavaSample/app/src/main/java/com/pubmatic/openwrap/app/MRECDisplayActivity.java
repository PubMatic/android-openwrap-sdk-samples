/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2025 PubMatic, All Rights Reserved.
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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBAdSize;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.banner.POBBannerView;

import java.net.MalformedURLException;
import java.net.URL;

public class MRECDisplayActivity extends AppCompatActivity {

    private static final String OPENWRAP_AD_UNIT_ID = "OpenWrapBannerAdUnit";

    private POBBannerView banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mrec_display);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.mrec_display_title);

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

        // Initialise banner view with MERCDisplay size i.e 300x250
        banner = findViewById(R.id.banner);
        banner.init(Constants.PUB_ID,
                Constants.PROFILE_ID,
                OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_300x250);

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
        public void onAdReceived(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Received");
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailed(@NonNull POBBannerView view, @NonNull POBError error) {
            Log.e(TAG, error.toString());
        }

        // Callback method Notifies that the banner ad view will launch a dialog on top of the current view
        @Override
        public void onAdOpened(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Opened");
        }

        // Callback method Notifies that the banner ad view is clicked.
        @Override
        public void onAdClicked(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Clicked");
        }

        // Callback method Notifies that the banner ad view has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Closed");
        }

        @Override
        public void onAppLeaving(@NonNull POBBannerView view) {
            // Implement your custom logic
            Log.d(TAG, "Banner : App Leaving");
        }

        @Override
        public void onAdImpression(@NonNull POBBannerView view) {
            // Implement your custom logic
            Log.d(TAG, "Ad Impression");
        }

    }
}
