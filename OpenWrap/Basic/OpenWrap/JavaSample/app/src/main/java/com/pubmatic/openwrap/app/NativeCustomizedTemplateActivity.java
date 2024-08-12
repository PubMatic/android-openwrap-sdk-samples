/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2024 PubMatic, All Rights Reserved.
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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.nativead.POBNativeAd;
import com.pubmatic.sdk.nativead.POBNativeAdListener;
import com.pubmatic.sdk.nativead.POBNativeAdLoader;
import com.pubmatic.sdk.nativead.POBNativeAdLoaderListener;
import com.pubmatic.sdk.nativead.datatype.POBNativeTemplateType;
import com.pubmatic.sdk.nativead.views.POBNativeAdMediumTemplateView;
import com.pubmatic.sdk.nativead.views.POBNativeTemplateView;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Class representing Native standard custom template.
 */
public class NativeCustomizedTemplateActivity extends AppCompatActivity {

    private static final String TAG = "NativeCustomTemplate";

    private static final String PUB_ID = "156276";

    private static final int PROFILE_ID = 1165;

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

        Button loadAd = findViewById(R.id.load_ad);
        renderAd = findViewById(R.id.render_ad);
        container = findViewById(R.id.container);

        loadAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load the native ad
                nativeAd = null;
                container.removeAllViews();
                renderAd.setEnabled(false);
                nativeAdLoader.loadAd();
            }
        });

        renderAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the native ad listener to listen the event callback and also to receive the
                // rendered native ad view.
                if (nativeAd != null) {
                    nativeAd.renderAd(getNativeTemplateView(), new NativeAdListenerImpl());
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
        nativeAdLoader = new POBNativeAdLoader(this, PUB_ID, PROFILE_ID,
                OPENWRAP_AD_UNIT_ID, POBNativeTemplateType.MEDIUM);

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

    private POBNativeTemplateView getNativeTemplateView() {
        // Create the inflater to inflate your custom template
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Provide your xml custom template
        POBNativeAdMediumTemplateView adview = (POBNativeAdMediumTemplateView) inflater.inflate(R.layout.custom_medium_template, null);

        // Set the reference of asset views your inflated adView
        ImageView mainImage = adview.findViewById(R.id.main_image);
        adview.setMainImage(mainImage);

        TextView title = adview.findViewById(R.id.title);
        adview.setTitle(title);

        TextView description = adview.findViewById(R.id.description);
        adview.setDescription(description);

        ImageView imageView = adview.findViewById(R.id.icon_image);
        adview.setIconImage(imageView);

        Button CTA = adview.findViewById(R.id.cta_text);
        adview.setCta(CTA);

        ImageView privacyIcon = adview.findViewById(R.id.privacy_icon);
        adview.setPrivacyIcon(privacyIcon);

        ImageView dsaIcon = adview.findViewById(R.id.dsa_icon);
        adview.setDSAIcon(dsaIcon);

        // Set the layout params to your ad view with inflated xml width and height
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.pob_dimen_300dp),
                (int) getResources().getDimension(R.dimen.pob_dimen_250dp));
        adview.setLayoutParams(layoutParams);

        return adview;
    }

    /**
     * Listener to get callback for ad received and ad failed.
     */
    private class NativeAdLoaderListenerImpl implements POBNativeAdLoaderListener {

        @Override
        public void onAdReceived(@NonNull POBNativeAdLoader nativeAdLoader, @NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad Received");
            // Caching nativeAd instance to call renderAd method and also to destroy it when activity get 
            // destroyed.
            NativeCustomizedTemplateActivity.this.nativeAd = nativeAd;
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
            // Add the received rendered native ad view in your container
            container.addView(nativeAd.getAdView());
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
