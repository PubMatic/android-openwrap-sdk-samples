/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2026 PubMatic, All Rights Reserved.
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

package com.pubmatic.openwrap.gamnextgen.app;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.openwrap.banner.POBBannerView;
import com.pubmatic.sdk.openwrap.eventhandler.gamnextgen.GAMNextGenNativeBannerEventHandler;
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityNativeBannerBinding;

import java.util.Collections;

/**
 * Demonstrates the Native and Banner combined ad workflow using OpenWrap SDK with GAM Next-Gen SDK as
 * the primary ad server via {@link GAMNextGenNativeBannerEventHandler}. Supports both standard
 * {@link NativeAd} and {@link CustomNativeAd} responses through a native ad loader callback, while
 * banner fallback creatives are rendered in a {@link POBBannerView}.
 */
public class NextGenNativeBannerActivity extends BaseSampleActivity implements LoggerListener {

    private static final String TAG = "NextGenNativeBannerActivity";

    private ActivityNativeBannerBinding binding;
    private POBBannerView banner;
    private NativeAd nativeAd;
    private CustomNativeAd customNativeAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNativeBannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyEdgeToEdge(binding.getRoot(), binding.includeToolbar.getRoot(), binding.container);
        setSupportActionBar(binding.includeToolbar.toolbar);
        binding.includeToolbar.toolbar.setTitle(R.string.title_activity_native_banner);

        Logger.registerLoggerListener(this);
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion());

        // Create a native banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, the code below creates an event handler for GAM Next-Gen ad server.
        GAMNextGenNativeBannerEventHandler eventHandler = new GAMNextGenNativeBannerEventHandler(
                this,
                Constants.AD_UNIT_NATIVE_BANNER,
                Collections.singletonList(new AdSize(300, 250))
        );
        // Prepares handler to request GAM Next-Gen NativeCustomFormatAd
        eventHandler.configureNativeCustomFormatAd(Collections.singletonList(Constants.CUSTOM_NATIVE_FORMAT));
        // Prepares handler to receive GAM Next-Gen NativeAd and CustomNativeAd via callback
        eventHandler.setGamNativeAdLoaderCallback(new NativeAdLoaderCallback(this));

        // Initialise banner view, set optional listener, and load the ad
        banner = new POBBannerView(this, Constants.PUB_ID, Constants.PROFILE_ID, Constants.AD_UNIT_NATIVE_BANNER, eventHandler);
        banner.setListener(new BannerViewListener(this));
        banner.loadAd();
    }

    private void renderNativeAd(NativeAd ad) {
        if (isDestroyed() || isFinishing() || isChangingConfigurations()) {
            ad.destroy();
            return;
        }
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        nativeAd = ad;
        GAMNextGenNativeAdsUIUtil.renderNativeAd(ad, binding.container, getLayoutInflater());
    }

    private void renderCustomNativeAd(CustomNativeAd ad) {
        if (isDestroyed() || isFinishing() || isChangingConfigurations()) {
            ad.destroy();
            return;
        }
        if (customNativeAd != null) {
            customNativeAd.destroy();
        }
        customNativeAd = ad;
        GAMNextGenNativeAdsUIUtil.renderCustomNativeAd(ad, binding.container, getLayoutInflater());
    }

    @Override
    protected void onDestroy() {
        // Destroy banner and native ads before destroying activity
        if (banner != null) {
            banner.destroy();
        }
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        if (customNativeAd != null) {
            customNativeAd.destroy();
        }
        Logger.removeLoggerListener(this);
        super.onDestroy();
    }

    // Appends a log message to the log TextView.
    @Override
    public void log(@NonNull String message) {
        binding.eventLogs.adEventLogs.append(message);
    }

    LinearLayout getAdContainer() {
        return binding.container;
    }

    // Native Ad loader callbacks
    private static final class NativeAdLoaderCallback implements GAMNextGenNativeBannerEventHandler.GAMNativeAdLoaderCallback {

        private final WeakReferenceHost host;

        NativeAdLoaderCallback(NextGenNativeBannerActivity activity) {
            this.host = new WeakReferenceHost(activity);
        }

        // Callback method notifies that a standard native ad has been loaded.
        @Override
        public void onNativeAdLoaded(@NonNull NativeAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Native Ad Received");
            NextGenNativeBannerActivity activity = host.get();
            if (activity != null) {
                activity.renderNativeAd(ad);
            } else {
                ad.destroy();
            }
        }

        // Callback method notifies that a custom native ad has been loaded.
        @Override
        public void onCustomNativeAdLoaded(@NonNull CustomNativeAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Custom Native Ad Received");
            NextGenNativeBannerActivity activity = host.get();
            if (activity != null) {
                activity.renderCustomNativeAd(ad);
            } else {
                ad.destroy();
            }
        }
    }

    // Banner Ad listener callbacks
    private static final class BannerViewListener extends POBBannerView.POBBannerViewListener {

        private final WeakReferenceHost host;

        BannerViewListener(NextGenNativeBannerActivity activity) {
            this.host = new WeakReferenceHost(activity);
        }

        // Callback method notifies that an ad has been successfully loaded and rendered.
        @Override
        public void onAdReceived(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdReceived");
            NextGenNativeBannerActivity activity = host.get();
            if (activity == null || view.getCreativeSize() == null) {
                return;
            }
            Logger.log(Logger.LogType.DEBUG, TAG, "Banner : Ad Received with size {" + view.getCreativeSize() + "}");
            LinearLayout container = activity.getAdContainer();
            if (container == null) {
                return;
            }
            container.removeAllViews();
            container.addView(view);
        }

        // Callback method notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailed(@NonNull POBBannerView view, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, "onAdFailed : Ad failed with error - " + error);
        }

        // Callback method notifies whenever current app goes in the background due to user click
        @Override
        public void onAppLeaving(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAppLeaving");
        }

        // Callback method notifies that the banner ad view will launch a dialog on top of the current view
        @Override
        public void onAdOpened(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdOpened");
        }

        // Callback method notifies that the banner ad view is clicked.
        @Override
        public void onAdClicked(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClicked");
        }

        // Callback method notifies that the banner ad view has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClosed");
        }

        @Override
        public void onAdExpired(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdExpired");
        }
    }

    private static final class WeakReferenceHost {

        private final java.lang.ref.WeakReference<NextGenNativeBannerActivity> activityRef;

        WeakReferenceHost(NextGenNativeBannerActivity activity) {
            this.activityRef = new java.lang.ref.WeakReference<>(activity);
        }

        NextGenNativeBannerActivity get() {
            NextGenNativeBannerActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }
            return activity;
        }
    }
}
