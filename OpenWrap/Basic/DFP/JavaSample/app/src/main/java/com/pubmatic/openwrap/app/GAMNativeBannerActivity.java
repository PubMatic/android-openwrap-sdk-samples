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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBAdSize;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.common.utility.POBUtils;
import com.pubmatic.sdk.openwrap.banner.POBBannerView;
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeBannerEventHandler;

import java.net.MalformedURLException;
import java.net.URL;

public class GAMNativeBannerActivity extends AppCompatActivity {

    private static final String OPENWRAP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-NativeAndBanner";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1165;
    private static final String DFP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-NativeAndBanner";
    public static final String CUSTOM_NATIVE_FORMAT = "12051535";

    private POBBannerView banner;
    private LinearLayout container;
    private TemplateView template;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gam_native);

        container = findViewById(R.id.container);
        template = findViewById(R.id.my_template);

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

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, The code below creates an event handler for DFP ad server.
        GAMNativeBannerEventHandler gamNativeEventHandler = new GAMNativeBannerEventHandler(this, DFP_AD_UNIT_ID, AdSize.MEDIUM_RECTANGLE);

        // Prepares handler to request GAM's NativeAd
        gamNativeEventHandler.configureNativeAd(new POBNativeListener());

        // Prepares handler to request GAM's NativeCustomFormatAd
        gamNativeEventHandler.configureNativeCustomFormatAd(CUSTOM_NATIVE_FORMAT, new POBCustomNativeListener(), null);

        // Initialise banner view
        banner = new POBBannerView(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, gamNativeEventHandler);

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

    private void renderNativeAd(@NonNull NativeAd nativeAd) {
        container.removeAllViews();
        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
        template.setStyles(styles);
        template.setNativeAd(nativeAd);
        template.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);
    }

    private void renderCustomNative(@NonNull final NativeCustomFormatAd nativeCustomFormatAd) {
        int width = POBUtils.convertDpToPixel(300);
        int height = POBUtils.convertDpToPixel(250);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        FrameLayout frameLayout = new FrameLayout(this);
        RelativeLayout adView = (RelativeLayout) getLayoutInflater()
                .inflate(R.layout.layout_custom_native, null);
        ImageView imageView = adView.findViewById(R.id.ad_app_icon);
        final String mainImageAssetName = "MainImage";
        NativeAd.Image image = nativeCustomFormatAd.getImage(mainImageAssetName);
        if (image != null ) {
            imageView.setImageDrawable(image.getDrawable());
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(mainImageAssetName);
            }
        });

        final String titleAssetName = "Title";
        TextView textView = adView.findViewById(R.id.ad_headline);
        textView.setText(nativeCustomFormatAd.getText(titleAssetName));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(titleAssetName);
            }
        });

        final String descriptionAssetName = "description";
        TextView descriptionTextView = adView.findViewById(R.id.ad_description);
        descriptionTextView.setText(nativeCustomFormatAd.getText(descriptionAssetName));
        descriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(descriptionAssetName);
            }
        });

        final String clickThroughAssetName = "ClickThroughText";
        Button button = adView.findViewById(R.id.ad_button);
        button.setText(nativeCustomFormatAd.getText(clickThroughAssetName));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(clickThroughAssetName);
            }
        });

        frameLayout.removeAllViews();
        frameLayout.addView(adView);

        container.removeAllViews();
        template.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        container.addView(frameLayout, layoutParams);
    }


    /**
     * POBBannerView Ad listener callbacks
     */
    private class POBBannerViewListener extends POBBannerView.POBBannerViewListener {
        private final String TAG = "POBBannerViewListener";

        // Callback method Notifies that an ad has been successfully loaded and rendered.
        @Override
        public void onAdReceived(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Received");
            template.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            POBAdSize adSize = view.getCreativeSize();
            if (adSize != null) {
                Log.d(TAG, "Banner : Ad Received with size {" + adSize.toString() + "}");
                int width = adSize.getAdWidth();
                int height = adSize.getAdHeight();
                width = POBUtils.convertDpToPixel(width);
                height = POBUtils.convertDpToPixel(height);
                ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
                container.removeAllViews();
                container.addView(view, layoutParams);
            }
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailed(@NonNull POBBannerView view, @NonNull POBError error) {
            Log.e(TAG, error.toString());
        }

        // Callback method Notifies whenever current app goes in the background due to user click
        @Override
        public void onAppLeaving(@NonNull POBBannerView view) {
            Log.d(TAG, "App Leaving");
        }

        // Callback method Notifies that the banner ad view will launch a dialog on top of the current view
        @Override
        public void onAdOpened(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Opened");
        }

        // Callback method Notifies that the banner ad view has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Closed");
        }

    }

    /**
     * GAM Native Ad listener callbacks
     */
    private class POBNativeListener extends GAMNativeBannerEventHandler.NativeAdListener {

        private static final String TAG = "POBNativeListener";

        // Callback method notifies that GAM native ad has been successfully loaded.
        @Override
        public void onAdReceived(@NonNull NativeAd nativeAd) {
            Log.d(TAG, "Native Ad Received");
            renderNativeAd(nativeAd);
        }

        // Callback method notifies whenever GAM native ad has been clicked
        @Override
        public void onAdClicked(@NonNull NativeAd nativeAd) {
            Log.d(TAG, "Native Ad Clicked");
        }

        // Callback method notifies about GAM native ad impression occurred
        @Override
        public void onAdImpression(@NonNull NativeAd nativeAd) {
            Log.d(TAG, "Native Ad Impression");
        }

        // Callback method notifies that the GAM native ad will launch a dialog on top of the current view
        @Override
        public void onAdOpened(@NonNull NativeAd nativeAd) {
            Log.d(TAG, "Native Ad Opened");
        }

        // Callback method notifies that the  banner ad has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(@NonNull NativeAd nativeAd) {
            Log.d(TAG, "Native Ad Closed");
        }
    }

    /**
     * GAM Custom Native Ad listener callbacks
     */
    private class POBCustomNativeListener extends GAMNativeBannerEventHandler.NativeCustomFormatAdListener {

        private static final String TAG = "POBCustomNativeListener";

        // Callback method notifies that GAM custom native ad has been successfully loaded.
        @Override
        public void onAdReceived(@NonNull NativeCustomFormatAd nativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Received");
            renderCustomNative(nativeCustomFormatAd);
            // Since this is custom native ad format, app is responsible for recording impressions and
            // reporting click events to the Google Mobile Ads SDK.
            nativeCustomFormatAd.recordImpression();
        }

        // Callback method notifies whenever GAM native ad has been clicked
        @Override
        public void onAdClicked(@NonNull NativeCustomFormatAd nativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Clicked");
        }

        // Callback method notifies about GAM native ad impression occurred
        @Override
        public void onAdImpression(@NonNull NativeCustomFormatAd nativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Impression");
        }

        // Callback method notifies that the GAM custom native ad will launch a dialog on top of the current view
        @Override
        public void onAdOpened(@NonNull NativeCustomFormatAd nativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Opened");
        }

        // Callback method notifies that the  banner ad has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(@NonNull NativeCustomFormatAd nativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Closed");
        }
    }
}