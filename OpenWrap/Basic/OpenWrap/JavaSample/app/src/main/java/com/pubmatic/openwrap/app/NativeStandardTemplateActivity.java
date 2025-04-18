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
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.nativead.POBNativeAd;
import com.pubmatic.sdk.nativead.POBNativeAdListener;
import com.pubmatic.sdk.nativead.POBNativeAdLoader;
import com.pubmatic.sdk.nativead.POBNativeAdLoaderListener;
import com.pubmatic.sdk.nativead.datatype.POBNativeTemplateType;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class representing Native standard small template.
 */
public class NativeStandardTemplateActivity extends AppCompatActivity {

    private static final String TAG = "NativeStandardTemplate";

    private static final String OPENWRAP_AD_UNIT_ID = "OpenWrapNativeAdUnit";

    private POBNativeAdLoader nativeAdLoader;

    @Nullable
    private POBNativeAd nativeAd;

    private Button renderAd;

    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.native_standard_title);

        Button loadAd = findViewById(R.id.load_ad);
        renderAd = findViewById(R.id.render_ad);
        container = findViewById(R.id.container);

        loadAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the native ad
                nativeAd = null;
                container.removeAllViews();
                nativeAdLoader.loadAd();
            }
        });

        renderAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the native ad listener to listen the event callback and also to receive the
                // rendered native ad view.
                if (nativeAd != null) {
                    nativeAd.renderAd(new NativeAdListenerImpl());
                }
            }
        });

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

        // Create native ad loader to make request to openWrap
        nativeAdLoader = new POBNativeAdLoader(this, Constants.PUB_ID, Constants.PROFILE_ID,
                OPENWRAP_AD_UNIT_ID, POBNativeTemplateType.SMALL);

        // Set the ad loader listener to listens the ad received and ad failed to load callback
        nativeAdLoader.setAdLoaderListener(new NativeAdLoaderListenerImpl());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nativeAd != null) {
            nativeAd.destroy();
        }
    }

    /**
     * Listener to get callback for ad received and ad failed.
     */
    private class NativeAdLoaderListenerImpl implements POBNativeAdLoaderListener {

        @Override
        public void onAdReceived(@NonNull POBNativeAdLoader nativeAdLoader, @NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad Received");
            //Caching nativeAd instance to call renderAd method and also to destroy it when activity get 
            // destroyed.
            NativeStandardTemplateActivity.this.nativeAd = nativeAd;
            renderAd.setEnabled(true);
        }

        @Override
        public void onFailedToLoad(@NonNull POBNativeAdLoader nativeAdLoader, @NonNull POBError error) {
            Log.e(TAG, error.toString());
        }
    }

    /**
     * Listener to get callback for rendered native ad view and native events.
     */
    private class NativeAdListenerImpl implements POBNativeAdListener {

        @Override
        public void onNativeAdRendered(@NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad Rendered");
            //Set the received rendered native ad view in your container
            container.addView(nativeAd.getAdView());
            renderAd.setEnabled(false);
        }

        @Override
        public void onNativeAdRenderingFailed(@NonNull POBNativeAd nativeAd, @NonNull POBError error) {
            Log.e(TAG, error.toString());
        }

        @Override
        public void onNativeAdImpression(@NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad recorded Impression");
        }

        @Override
        public void onNativeAdClicked(@NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad Clicked");
        }

        @Override
        public void onNativeAdClicked(@NonNull POBNativeAd nativeAd, @NonNull String assetId) {
            Log.d(TAG, "Ad clicked for asset id - " + assetId);
        }

        @Override
        public void onNativeAdLeavingApplication(@NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "App Leaving");
        }

        @Override
        public void onNativeAdOpened(@NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad Opened");
        }

        @Override
        public void onNativeAdClosed(@NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad Closed");
        }
    }
}
