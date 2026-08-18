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

import androidx.annotation.NonNull;

import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.openwrap.banner.POBBannerView;
import com.pubmatic.sdk.openwrap.eventhandler.gamnextgen.GAMNextGenBannerEventHandler;
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityMrecDisplayBinding;

import java.util.Collections;

/**
 * Demonstrates the MREC Display ad workflow using OpenWrap SDK with GAM Next-Gen SDK as the primary
 * ad server via {@link GAMNextGenBannerEventHandler}. A {@link POBBannerView} is configured with a
 * 300x250 ad size and loads medium-rectangle display creatives through the Next-Gen event handler.
 */
public class NextGenMRECDisplayActivity extends BaseSampleActivity implements LoggerListener {

    private static final String TAG = "NextGenMRECDisplayActivity";

    private ActivityMrecDisplayBinding binding;
    private POBBannerView banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMrecDisplayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyEdgeToEdge(binding.getRoot(), binding.includeToolbar.getRoot(), binding.banner);
        setSupportActionBar(binding.includeToolbar.toolbar);
        binding.includeToolbar.toolbar.setTitle(R.string.title_activity_mrec_display);

        Logger.registerLoggerListener(this);
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion());

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, the code below creates an event handler for GAM Next-Gen ad server.
        GAMNextGenBannerEventHandler eventHandler = new GAMNextGenBannerEventHandler(
                this,
                Constants.AD_UNIT_BANNER,
                Collections.singletonList(new AdSize(300, 250))
        );

        // Initialise banner view with MREC (300x250) ad size, set listener, and load the ad
        banner = binding.banner;
        banner.init(Constants.PUB_ID, Constants.PROFILE_ID, Constants.AD_UNIT_BANNER, eventHandler);
        banner.setListener(new BannerViewListener());
        banner.loadAd();
    }

    @Override
    protected void onDestroy() {
        // Destroy banner before destroying activity
        if (banner != null) {
            banner.destroy();
        }
        Logger.removeLoggerListener(this);
        super.onDestroy();
    }

    // Appends a log message to the log TextView.
    @Override
    public void log(@NonNull String message) {
        binding.eventLogs.adEventLogs.append(message);
    }

    // Banner Ad listener callbacks
    private static final class BannerViewListener extends POBBannerView.POBBannerViewListener {
        // Callback method notifies that an ad has been successfully loaded and rendered.
        @Override
        public void onAdReceived(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdReceived");
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

        // Callback method notifies that the banner ad view is clicked.
        @Override
        public void onAdClicked(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClicked");
        }

        // Callback method notifies that the banner ad view will launch a dialog on top of the current view
        @Override
        public void onAdOpened(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdOpened");
        }

        // Callback method notifies that the banner ad view has dismissed the modal on top of the current view
        @Override
        public void onAdClosed(@NonNull POBBannerView view) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClosed");
        }
    }
}
