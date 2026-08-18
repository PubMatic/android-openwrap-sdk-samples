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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.openwrap.core.POBReward;
import com.pubmatic.sdk.openwrap.eventhandler.gamnextgen.GAMNextGenRewardedEventHandler;
import com.pubmatic.sdk.rewardedad.POBRewardedAd;
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityRewardedBinding;

import java.lang.ref.WeakReference;

/**
 * Demonstrates the Rewarded ad workflow using OpenWrap SDK with GAM Next-Gen SDK as the primary ad
 * server via {@link GAMNextGenRewardedEventHandler}. A {@link POBRewardedAd} is obtained via the
 * factory, loaded on demand, and shown to the user who receives a {@link POBReward} upon completion.
 */
public class NextGenRewardedActivity extends BaseSampleActivity implements LoggerListener {

    private static final String TAG = "NextGenRewardedActivity";

    private ActivityRewardedBinding binding;
    private POBRewardedAd rewardedAd;
    private GAMNextGenRewardedEventHandler eventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRewardedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyEdgeToEdge(binding.getRoot(), binding.includeToolbar.getRoot(), binding.controls.getRoot());
        setSupportActionBar(binding.includeToolbar.toolbar);
        binding.includeToolbar.toolbar.setTitle(R.string.title_activity_rewarded);

        Logger.registerLoggerListener(this);
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion());

        // Create a rewarded custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each rewarded ad instance.
        // For example, the code below creates an event handler for GAM Next-Gen ad server.
        eventHandler = new GAMNextGenRewardedEventHandler(Constants.AD_UNIT_REWARDED);

        // Create POBRewardedAd instance and set optional callback listener
        rewardedAd = POBRewardedAd.getRewardedAd(
                this,
                Constants.PUB_ID,
                Constants.PROFILE_ID_FOR_VIDEO,
                Constants.AD_UNIT_REWARDED,
                eventHandler
        );
        if (rewardedAd != null) {
            rewardedAd.setListener(new RewardedAdListener(this));
        }

        // Load rewarded Ad
        binding.controls.showAdBtn.setEnabled(false);
        binding.controls.loadAdBtn.setOnClickListener(v -> {
            binding.controls.showAdBtn.setEnabled(false);
            if (rewardedAd != null) {
                rewardedAd.loadAd();
            }
        });

        // Show rewarded Ad
        binding.controls.showAdBtn.setOnClickListener(v -> {
            showRewardedAd();
            binding.controls.showAdBtn.setEnabled(false);
        });
    }

    /**
     * To show rewarded ad call this method
     */
    private void showRewardedAd() {
        // Call show when ad is ready
        if (rewardedAd != null && rewardedAd.isReady()) {
            rewardedAd.show(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (rewardedAd != null) {
            rewardedAd.destroy();
        }
        if (eventHandler != null) {
            eventHandler.destroy();
        }
        Logger.removeLoggerListener(this);
        super.onDestroy();
    }

    // Appends a log message to the log TextView.
    @Override
    public void log(@NonNull String message) {
        binding.controls.eventLogs.adEventLogs.append(message);
    }

    // Rewarded Ad listener callbacks
    private static final class RewardedAdListener extends POBRewardedAd.POBRewardedAdListener {

        private final WeakReference<NextGenRewardedActivity> activityRef;

        RewardedAdListener(NextGenRewardedActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        // Callback method notifies that an ad has been received successfully.
        @Override
        public void onAdReceived(@NonNull POBRewardedAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdReceived");
            // Method gets called when ad gets loaded; here you can enable show button
            ActivityUiUtil.enableShowAdButton(activityRef.get());
        }

        // Callback method notifies an error encountered while loading an ad.
        @Override
        public void onAdFailedToLoad(@NonNull POBRewardedAd ad, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, "Rewarded:onAdFailedToLoad : Ad failed with load error - " + error);
        }

        // Callback method notifies an error encountered while showing an ad.
        @Override
        public void onAdFailedToShow(@NonNull POBRewardedAd ad, @NonNull POBError error) {
            Logger.log(Logger.LogType.ERROR, TAG, "Rewarded:onAdFailedToShow : Ad failed with show error - " + error);
        }

        // Callback method notifies that a user interaction will open another app, leaving the current app.
        @Override
        public void onAppLeaving(@NonNull POBRewardedAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAppLeaving");
        }

        // Callback method notifies that the rewarded ad will be presented as a modal on top of the current view
        @Override
        public void onAdOpened(@NonNull POBRewardedAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdOpened");
        }

        // Callback method notifies that the rewarded ad has been animated off the screen.
        @Override
        public void onAdClosed(@NonNull POBRewardedAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdClosed");
        }

        // Callback method notifies ad click
        @Override
        public void onAdClicked(@NonNull POBRewardedAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdClicked");
        }

        @Override
        public void onAdImpression(@NonNull POBRewardedAd ad) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdImpression");
        }

        // Callback method notifies that the user should be rewarded
        @Override
        public void onReceiveReward(@NonNull POBRewardedAd ad, @NonNull POBReward reward) {
            Logger.log(
                    Logger.LogType.DEBUG,
                    TAG,
                    "Rewarded: Ad should Reward - " + reward.getAmount() + " (" + reward.getCurrencyType() + ")"
            );
            NextGenRewardedActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(
                        activity.getApplicationContext(),
                        "Congratulations! You are rewarded with " + reward.getAmount() + " " + reward.getCurrencyType(),
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}
