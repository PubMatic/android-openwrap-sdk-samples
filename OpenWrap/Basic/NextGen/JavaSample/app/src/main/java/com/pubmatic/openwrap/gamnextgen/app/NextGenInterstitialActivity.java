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

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.openwrap.eventhandler.gamnextgen.GAMNextGenInterstitialEventHandler;
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitial;
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityInterstitialBinding;

import java.lang.ref.WeakReference;

/**
 * Demonstrates the Interstitial Display ad workflow using OpenWrap SDK with GAM Next-Gen SDK as the
 * primary ad server via {@link GAMNextGenInterstitialEventHandler}. A {@link POBInterstitial} is
 * created with OpenWrap publisher/profile/ad unit IDs, loaded on demand, and shown fullscreen when ready.
 */
public class NextGenInterstitialActivity extends BaseSampleActivity implements LoggerListener {

    private static final String TAG = "NextGenInterstitialActivity";

    private ActivityInterstitialBinding binding;
    private POBInterstitial interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInterstitialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyEdgeToEdge(binding.getRoot(), binding.includeToolbar.getRoot(), binding.controls.getRoot());
        setSupportActionBar(binding.includeToolbar.toolbar);
        binding.includeToolbar.toolbar.setTitle(R.string.title_activity_interstitial);

        Logger.registerLoggerListener(this);
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion());

        // Create an interstitial custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each interstitial ad instance.
        // For example, the code below creates an event handler for GAM Next-Gen ad server.
        GAMNextGenInterstitialEventHandler eventHandler =
                new GAMNextGenInterstitialEventHandler(Constants.AD_UNIT_INTERSTITIAL);

        // Create interstitial instance and set optional listener
        interstitial = new POBInterstitial(
                this,
                Constants.PUB_ID,
                Constants.PROFILE_ID,
                Constants.AD_UNIT_INTERSTITIAL,
                eventHandler
        );
        interstitial.setListener(new InterstitialListener(this));

        // Load Ad button
        binding.controls.showAdBtn.setEnabled(false);
        binding.controls.loadAdBtn.setOnClickListener(v -> {
            binding.controls.showAdBtn.setEnabled(false);
            if (interstitial != null) {
                interstitial.loadAd();
            }
        });

        // Show button
        binding.controls.showAdBtn.setOnClickListener(v -> {
            showInterstitialAd();
            binding.controls.showAdBtn.setEnabled(false);
        });
    }

    /**
     * To show interstitial ad call this method
     */
    private void showInterstitialAd() {
        // Check if the interstitial is ready
        if (interstitial != null && interstitial.isReady()) {
            interstitial.show(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (interstitial != null) {
            interstitial.destroy();
        }
        Logger.removeLoggerListener(this);
        super.onDestroy();
    }

    // Appends a log message to the log TextView.
    @Override
    public void log(@NonNull String message) {
        binding.controls.eventLogs.adEventLogs.append(message);
    }

    // Interstitial Ad listener callbacks
    private static final class InterstitialListener extends POBInterstitial.POBInterstitialListener {

        private final WeakReference<NextGenInterstitialActivity> activityRef;

        InterstitialListener(NextGenInterstitialActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        // Callback method notifies that an ad has been received successfully.
        @Override
        public void onAdReceived(@NonNull POBInterstitial ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdReceived");
            // Method gets called when ad gets loaded; here you can enable show button
            ActivityUiUtil.enableShowAdButton(activityRef.get());
        }

        // Callback method notifies an error encountered while loading an ad.
        @Override
        public void onAdFailedToLoad(@NonNull POBInterstitial ad, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, "Interstitial : Ad failed to load with error - " + error);
        }

        // Callback method notifies an error encountered while showing an ad.
        @Override
        public void onAdFailedToShow(@NonNull POBInterstitial ad, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, "Interstitial : Ad failed to show with error - " + error);
        }

        // Callback method notifies that a user interaction will open another app, leaving the current app.
        @Override
        public void onAppLeaving(@NonNull POBInterstitial ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAppLeaving");
        }

        // Callback method notifies that the interstitial ad will be presented as a modal on top of the current view
        @Override
        public void onAdOpened(@NonNull POBInterstitial ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdOpened");
        }

        // Callback method notifies that the interstitial ad has been animated off the screen.
        @Override
        public void onAdClosed(@NonNull POBInterstitial ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClosed");
        }

        // Callback method notifies ad click
        @Override
        public void onAdClicked(@NonNull POBInterstitial ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClicked");
        }

        @Override
        public void onAdImpression(@NonNull POBInterstitial ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdImpression");
        }
    }
}
