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
package com.pubmatic.openwrap.kotlinsampleapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.core.POBReward
import com.pubmatic.sdk.rewardedad.POBRewardedAd
import kotlinx.android.synthetic.main.activity_rewarded.*
import java.net.MalformedURLException
import java.net.URL


/**
 * Activity to show Rewarded Ad Implementation
 */
class RewardedActivity : AppCompatActivity() {

    val TAG = "RewardedActivity"

    private val OPENWRAP_AD_UNIT_ID = "OpenWrapRewardedAdUnit"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1757

    private var rewardedAd : POBRewardedAd? = null
    private var loadAd: Button? = null
    private var showAd: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewarded)
        setSupportActionBar(toolbar)

        // A valid Play Store Url of an Android app. Required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.storeURL = URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo)

        // Create rewarded ad instance by passing activity context and Publisher Credentials
        rewardedAd = POBRewardedAd.getRewardedAd(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID)

        // Set optional listener
        rewardedAd?.setListener(RewardedAdListener())

        loadAd = findViewById(R.id.load_ad)
        loadAd?.setOnClickListener {
                // Call loadAd on rewarded
                 rewardedAd?.loadAd()

        }

        showAd = findViewById(R.id.show_ad)
        showAd?.isEnabled = false
        showAd?.setOnClickListener {
            // check if the rewarded ad is ready
            showRewardedAd()
            showAd?.isEnabled = false
        }

    }

    private fun showRewardedAd(){
        // check if the rewarded ad is ready
        if(rewardedAd?.isReady == true){
            // Call show on rewarded ad
            rewardedAd?.show()
        }
    }

    private fun useBidAndProceed() {
        // Call rewardedAd?.proceedToLoadAd() to complete the flow
        Log.d(TAG, "Proceeding with load ad.")
        rewardedAd?.proceedToLoadAd()
    }


    /**
     * Implementation class to receive Rewarded ad events
     */
    inner class RewardedAdListener : POBRewardedAd.POBRewardedAdListener() {

        // Callback method notifies that an ad has been received successfully.
        override fun onAdReceived(rewardedAd: POBRewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Received")
            // Method gets called when ad gets loaded in container
            // Here, you can show rewarded ad to user
            showAd?.isEnabled = true
        }

        // Callback method notifies an error encountered while loading or rendering an ad.
        override fun onAdFailedToLoad(rewardedAd: POBRewardedAd, error: POBError) {
            Log.e(TAG, "Rewarded Ad : Ad failed with error - " + error.toString())
        }

        // Callback method notifies ad is about to leave app
        override fun onAppLeaving(rewardedAd: POBRewardedAd) {
            Log.d(TAG, "Rewarded Ad : App Leaving")
        }

        // Callback method notifies that the rewarded ad will be presented as a full screen modal on top of the current view.
        override fun onAdOpened(rewardedAd: POBRewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Opened")
        }

        // Callback method notifies that the rewarded ad has been animated off the screen.
        override fun onAdClosed(rewardedAd: POBRewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Closed")
        }

        // Callback method notifies ad click
        override fun onAdClicked(rewardedAd: POBRewardedAd) {
            Log.d(TAG, "Rewarded Ad : Ad Clicked")
        }

        // Callback method notifies ad is about to expire
        override fun onAdExpired(rewardedAd: POBRewardedAd) {
            // Implement your custom logic
            Log.d(TAG, "Rewarded Ad : Ad Expired")
        }

        // Callback method notifies user will be rewarded once the ad is completely viewed
        override fun onReceiveReward(rewardedAd: POBRewardedAd, reward: POBReward) {
            Log.d(TAG, "Rewarded Ad : Ad should reward - ${reward.amount}(${reward.currencyType})")
        }

        // Callback method notifies that error encountered while rendering an ad
        override fun onAdFailedToShow(rewardedAd: POBRewardedAd, error: POBError) {
            Log.d(TAG, "Rewarded Ad : Ad failed with error $error")
        }
    }

    // Call destroy method inside host activity/fragment lifecycle's destroy method
    override fun onDestroy() {
        super.onDestroy()
        rewardedAd?.destroy()
    }
}