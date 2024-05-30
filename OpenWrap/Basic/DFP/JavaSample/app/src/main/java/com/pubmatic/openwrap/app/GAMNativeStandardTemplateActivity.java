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

import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.common.utility.POBUtils;
import com.pubmatic.sdk.nativead.POBNativeAd;
import com.pubmatic.sdk.nativead.POBNativeAdListener;
import com.pubmatic.sdk.nativead.POBNativeAdLoader;
import com.pubmatic.sdk.nativead.POBNativeAdLoaderListener;
import com.pubmatic.sdk.nativead.datatype.POBNativeTemplateType;
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeConfiguration;
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeEventHandler;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class representing GAM Native Ad and OpenWrap SDK standard small template
 */
public class GAMNativeStandardTemplateActivity extends AppCompatActivity {

    private static final String TAG = "NativeStandardTemplate";

    private static final String PUB_ID = "156276";

    private static final int PROFILE_ID = 1165;

    private static final String OPENWRAP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-Native";

    private static final String DFP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-Native";

    private static final String OPENWRAP_CUSTOM_FORMAT_ID = "12260425";

    private static final String GAM_NATIVE_CUSTOM_FORMAT_ID = "12051535";

    //Asset names required for the rendering of GAM Native Custom format ad
    public static final String MAIN_IMAGE_ASSET_NAME = "MainImage";

    public static final String TITLE_ASSET_NAME = "Title";

    public static final String DESCRIPTION_ASSET_NAME = "description";

    public static final String CLICK_THROUGH_ASSET_NAME = "ClickThroughText";

    private POBNativeAdLoader nativeAdLoader;

    @Nullable
    private POBNativeAd nativeAd;

    private Button renderAd;

    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_standard);

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

        //Create nativeEventHandler to request ad from your GAM Ad Server
        GAMNativeEventHandler nativeEventHandler = new GAMNativeEventHandler(this, DFP_AD_UNIT_ID,
                OPENWRAP_CUSTOM_FORMAT_ID, GAMNativeEventHandler.GAMAdTypes.NativeAd,
                GAMNativeEventHandler.GAMAdTypes.NativeCustomFormatAd);

        //This step is optional and you can the set the Custom Format Id List here
        nativeEventHandler.addNativeCustomFormatAd(GAM_NATIVE_CUSTOM_FORMAT_ID, null);

        //Set the rendering listener for GAM Native Ad
        nativeEventHandler.setNativeAdRendererListener(new GAMNativeConfiguration.NativeAdRendererListener() {
            @Nullable
            @Override
            public NativeAdView prepareAdViewForRendering(@NonNull NativeAd nativeAd) {
                // Inflate a layout and add it to the parent ViewGroup.
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                NativeAdView adView = (NativeAdView) inflater
                        .inflate(R.layout.gam_native_ad_template, null);
                renderNativeAd(nativeAd, adView);
                return adView;
            }
        });


        //Set the rendering listener for GAM Native Ad
        nativeEventHandler.setNativeCustomFormatAdRendererListener(new GAMNativeConfiguration.NativeCustomFormatAdRendererListener() {
            @Nullable
            @Override
            public View prepareAdViewForRendering(@NonNull NativeCustomFormatAd nativeCustomFormatAd) {
                // Inflate a layout and add it to the parent ViewGroup.
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View adView = inflater
                        .inflate(R.layout.layout_custom_native, null);

                int width = POBUtils.convertDpToPixel(300);
                int height = POBUtils.convertDpToPixel(250);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
                adView.setLayoutParams(layoutParams);

                populateNativeCustomAd(nativeCustomFormatAd, adView);

                return adView;
            }
        });

        // Create nativeAdLoader to request ad from OpenWrap with GAM event handler
        nativeAdLoader = new POBNativeAdLoader(this, PUB_ID, PROFILE_ID,
                OPENWRAP_AD_UNIT_ID, POBNativeTemplateType.SMALL, nativeEventHandler);

        // Set the adLoaderListener to listens the callback for ad received or ad failed to load
        nativeAdLoader.setAdLoaderListener(new NativeAdLoaderListenerImpl());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up native ad, helps releasing utilized resources.
        if (nativeAd != null) {
            nativeAd.destroy();
        }
    }

    /**
     * Render the given NativeAd object with inflated NativeAdView
     * @param nativeAd Instance of {@link NativeAd}
     * @param nativeAdView Instance of {@link NativeAdView}
     */
    private void renderNativeAd(@NonNull NativeAd nativeAd, @NonNull NativeAdView nativeAdView) {
        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
        TemplateView template = nativeAdView.findViewById(R.id.my_template);
        template.setStyles(styles);
        template.setNativeAd(nativeAd);
        template.setVisibility(View.VISIBLE);
    }

    /**
     * Populate the nativeCustomFormatAd with inflated adview
     * @param nativeCustomFormatAd Instance of {@link NativeCustomFormatAd}
     * @param adView Instance of {@link View}
     */
    private void populateNativeCustomAd(@NonNull final NativeCustomFormatAd nativeCustomFormatAd, @NonNull View adView) {
        //Populate the inflated custom view with NativeCustomFormatAd object
        ImageView imageView = adView.findViewById(R.id.ad_app_icon);
        NativeAd.Image image = nativeCustomFormatAd.getImage(MAIN_IMAGE_ASSET_NAME);
        if (image != null ) {
            imageView.setImageDrawable(image.getDrawable());
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(MAIN_IMAGE_ASSET_NAME);
            }
        });

        TextView textView = adView.findViewById(R.id.ad_headline);
        textView.setText(nativeCustomFormatAd.getText(TITLE_ASSET_NAME));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(TITLE_ASSET_NAME);
            }
        });

        TextView descriptionTextView = adView.findViewById(R.id.ad_description);
        descriptionTextView.setText(nativeCustomFormatAd.getText(DESCRIPTION_ASSET_NAME));
        descriptionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(DESCRIPTION_ASSET_NAME);
            }
        });

        Button button = adView.findViewById(R.id.ad_button);
        button.setText(nativeCustomFormatAd.getText(CLICK_THROUGH_ASSET_NAME));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeCustomFormatAd.performClick(CLICK_THROUGH_ASSET_NAME);
            }
        });
    }

    /**
     * Listener to get callback for ad received and ad failed.
     */
    private class NativeAdLoaderListenerImpl implements POBNativeAdLoaderListener {

        @Override
        public void onAdReceived(@NonNull POBNativeAdLoader nativeAdLoader, @NonNull POBNativeAd nativeAd) {
            Log.d(TAG, "Ad Received");
            //Caching nativeAd instance to call renderAd method and to destroy it when activity get
            // destroyed.
            GAMNativeStandardTemplateActivity.this.nativeAd = nativeAd;
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
            Log.d(TAG, "Ad clicked for asset id - "+assetId);
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
