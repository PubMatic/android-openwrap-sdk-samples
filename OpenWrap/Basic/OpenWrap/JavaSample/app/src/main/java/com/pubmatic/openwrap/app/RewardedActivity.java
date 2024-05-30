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

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.core.POBReward;
import com.pubmatic.sdk.rewardedad.POBRewardedAd;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to show Rewarded Ad Implementation
 */
public class RewardedActivity extends AppCompatActivity {

    private static final String TAG = "RewardedActivity";
    private static final String OPENWRAP_AD_UNIT_ONE = "OpenWrapRewardedAdUnit";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1757;

    private POBRewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewarded);

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

        // Create rewarded ad instance by passing activity context and tag params
        rewardedAd = POBRewardedAd.getRewardedAd(this, PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ONE);

        //Check if rewarded ad instance is null
        if(rewardedAd != null){
            // Set Optional listener
            rewardedAd.setListener(new RewardedAdListener());
        }

        // Load Ad button
        findViewById(R.id.loadAdBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.showAdBtn).setEnabled(false);
                if(rewardedAd != null) {
                    rewardedAd.loadAd();
                }
            }
        });

        // Show button
        findViewById(R.id.showAdBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardedAd();
            }
        });
    }

    /**
     * To show rewarded ad call this method
     **/
    private void showRewardedAd() {
        // check if the rewarded is ready
        if (rewardedAd != null && rewardedAd.isReady()) {
            // Call show on rewarded
            rewardedAd.show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // destroy rewarded
        if (rewardedAd != null) {
            rewardedAd.destroy();
        }
    }

    /**
     * Implementation class to receive Rewarded ad events
     */
    class RewardedAdListener extends POBRewardedAd.POBRewardedAdListener {

        // Callback method notifies that an ad has been received successfully.
        @Override
        public void onAdReceived(@NonNull POBRewardedAd rewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Received");
            // Method gets called when ad gets loaded in container
            // Here, you can show rewarded ad to user
            findViewById(R.id.showAdBtn).setEnabled(true);
        }

        // Callback method notifies an error encountered while loading or rendering an ad.
        @Override
        public void onAdFailedToLoad(@NonNull POBRewardedAd rewardedAd, @NonNull POBError error) {
            Log.e(TAG, "Rewarded Ad : Ad failed with error - " + error);
        }

        // Callback method notifies ad is about to leave app
        @Override
        public void onAppLeaving(@NonNull POBRewardedAd rewardedAd) {
            Log.d(TAG, "Rewarded Ad : App Leaving");
        }

        // Callback method notifies that the rewarded ad will be presented as a full screen modal on top of the current view.
        @Override
        public void onAdOpened(@NonNull POBRewardedAd rewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Opened");
        }

        // Callback method notifies that the rewarded ad has been animated off the screen.
        @Override
        public void onAdClosed(@NonNull POBRewardedAd rewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Closed");
        }

        // Callback method notifies ad click
        @Override
        public void onAdClicked(@NonNull POBRewardedAd rewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Clicked");
        }

        // Callback method notifies ad is about to expire
        @Override
        public void onAdExpired(@NonNull POBRewardedAd rewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Expired");
        }

        // Callback method notifies on Ad Impression
        @Override
        public void onAdImpression(@NonNull POBRewardedAd rewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Impression");
        }

        // Callback method notifies user will be rewarded once the ad is completely viewed
        @Override
        public void onReceiveReward(@NonNull POBRewardedAd rewardedAd, @NonNull POBReward reward) {
            Log.d(TAG, "Rewarded Ad : Ad should reward - "+ reward.getAmount()+ "(" + reward.getCurrencyType()+ ")");
        }

        // Callback method notifies that error is encountered while rendering an ad
        @Override
        public void onAdFailedToShow(@NonNull POBRewardedAd rewardedAd, @NonNull POBError error) {
            Log.d(TAG,"Rewarded Ad: Ad failed with error" + error.toString());
        }
    }
}
