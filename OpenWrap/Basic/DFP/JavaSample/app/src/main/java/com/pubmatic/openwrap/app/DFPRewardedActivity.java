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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.core.POBReward;
import com.pubmatic.sdk.openwrap.eventhandler.dfp.DFPRewardedEventHandler;
import com.pubmatic.sdk.rewardedad.POBRewardedAd;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class demonstrate the Rewarded Ad workflow via OW SDK where DFP / GAM SDK is integrated
 * as a primary ad SDK.
 */
public class DFPRewardedActivity extends AppCompatActivity {

    private static final String OPENWRAP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-RewardedAd";
    private static final String DFP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-RewardedAd";

    private POBRewardedAd rewarded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfp_rewarded);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.rewarded_title);

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

        // Create an rewarded custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each rewarded ad instance.
        // For example, The code below creates an event handler for DFP ad server.
        DFPRewardedEventHandler eventHandler = new DFPRewardedEventHandler(this,DFP_AD_UNIT_ID);

        //Create POBRewardedAd instance by passing activity context and profile parameters
        rewarded = POBRewardedAd.getRewardedAd(this,
                Constants.PUB_ID,
                Constants.PROFILE_ID_FOR_VIDEO,
                OPENWRAP_AD_UNIT_ID,
                eventHandler
        );

        //Set Optional Callback Listener
        rewarded.setListener(new POBRewardedAdListenerImpl());

        //Load rewarded Ad
        findViewById(R.id.loadAdBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.showAdBtn).setEnabled(false);
                rewarded.loadAd();
            }
        });

        //show rewarded Ad
        findViewById(R.id.showAdBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardedAd();
            }
        });
    }

    /*
     * To show Rewarded Ad Call this method
     */
    private void showRewardedAd(){
        //Call showAd when Ad is ready
        if(rewarded != null && rewarded.isReady()){
            rewarded.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Destroy Rewarded Ad
        if(rewarded != null) {
            rewarded.destroy();
        }
    }

    class POBRewardedAdListenerImpl extends POBRewardedAd.POBRewardedAdListener {
        private final String TAG = "POBRewardedAdListener";

        // Callback method notifies that an ad has been received successfully.
        @Override
        public void onAdReceived(@NonNull POBRewardedAd ad) {
            Log.d(TAG, "Rewarded: onAdReceived");
            //Method gets called when ad gets loaded in container
            //Here, you can show Rewarded ad to user
            findViewById(R.id.showAdBtn).setEnabled(true);
        }

        // Callback method notifies an error encountered while loading an ad.
        @Override
        public void onAdFailedToLoad(@NonNull POBRewardedAd ad, @NonNull POBError error) {
            Log.d(TAG, "Rewarded : onAdFailedToLoad" );
            Log.e(TAG,"Ad failed with load error - " + error.toString());
            //Method gets called when sdk fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies an error encountered while rendering an ad.
        @Override
        public void onAdFailedToShow(@NonNull POBRewardedAd ad,@NonNull POBError error) {
            Log.e(TAG, "Rewarded : onAdFailedToShow");
            Log.e(TAG,"Ad failed with show error - " + error.toString());
            //Method gets called when sdk fails to show ad
            //Here, you can put logger and see why ad failed to show
        }


        // Callback method notifies that a user interaction will open another app (for example, App Store), leaving the current app.
        @Override
        public void onAppLeaving(@NonNull POBRewardedAd ad) {
            Log.d(TAG, "Rewarded : onAppLeaving");
        }

        // Callback method notifies that the rewarded ad will be presented as a modal on top of the current view controller
        @Override
        public void onAdOpened(@NonNull POBRewardedAd ad) {
            Log.d(TAG, "Rewarded : onAdOpened");
        }

        // Callback method notifies that the rewarded ad has been animated off the screen.
        @Override
        public void onAdClosed(@NonNull POBRewardedAd ad) {
            Log.d(TAG, "Rewarded : onAdClosed");
        }

        // Callback method notifies ad click
        @Override
        public void onAdClicked(@NonNull POBRewardedAd ad) {
            Log.d(TAG, "Rewarded : onAdClicked");
        }

        @Override
        public void onReceiveReward(@NonNull POBRewardedAd ad, @NonNull POBReward reward) {
            // As this is callback method, No action Required
            Log.d(TAG,"Rewarded : Ad should Reward -" + reward.getAmount() +"(" + reward.getCurrencyType()+")");
            Toast.makeText(getApplicationContext(), "Congratulation! You are rewarded with "+reward.getAmount()+" "+reward.getCurrencyType(), Toast.LENGTH_LONG).show();
        }

        // Callback method for ad Impression
        @Override
        public void onAdImpression(@NonNull POBRewardedAd ad) {
            Log.d(TAG, "Rewarded : onAdImpression");
        }
    }

}