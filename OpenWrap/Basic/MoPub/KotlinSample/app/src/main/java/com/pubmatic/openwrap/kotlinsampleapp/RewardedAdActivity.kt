/*
* PubMatic Inc. ("PubMatic") CONFIDENTIAL
* Unpublished Copyright (c) 2006-2021 PubMatic, All Rights Reserved.
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
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.log.POBLog
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.core.POBReward
import com.pubmatic.sdk.openwrap.eventhandler.mopub.MoPubRewardedEventHandler
import com.pubmatic.sdk.rewardedad.POBRewardedAd
import com.pubmatic.sdk.rewardedad.POBRewardedAd.POBRewardedAdListener
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * This class demonstrate the Rewarded Ad workflow via OW SDK where MoPub SDK is integrated
 * as a primary ad SDK.
 */
class RewardedAdActivity : AppCompatActivity() {
    private val TAG: String = "RewardedAdActivity"
    private var rewardedAd: POBRewardedAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewarded)

        // A valid Play Store Url of an Android application is required.
        val appInfo: POBApplicationInfo = POBApplicationInfo()
        try {
            appInfo.storeURL = URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo)

        // Create an rewarded custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each rewarded ad instance.
        // For example, The code below creates an event handler for MoPub ad server.
        val eventHandler: MoPubRewardedEventHandler = MoPubRewardedEventHandler.getHandler(MOPUB_AD_UNIT_ID)

        // Create rewarded ad instance by passing activity context, Publisher Credentials
        // and handler object
        rewardedAd = POBRewardedAd.getRewardedAd(this, PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ONE,
                eventHandler)

        // Set the optional listener to get the video events
        rewardedAd?.setListener(POBWrapperViewListener())

        // Load Ad button
        findViewById<View>(R.id.loadAdBtn).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                findViewById<View>(R.id.showAdBtn).isEnabled = false
                rewardedAd?.loadAd()
            }
        })

        // Show button
        findViewById<View>(R.id.showAdBtn).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                showRewardedAd()
            }
        })
    }

    /**
     * Method to show rewarded ad call
     **/
    private fun showRewardedAd() {
        findViewById<View>(R.id.showAdBtn).isEnabled = false
        // check if the rewarded ad is ready
        if (null != rewardedAd && rewardedAd?.isReady == true) {
            val customDataMap: HashMap<String, Any> = HashMap(1)

            // Pass the selected reward to customDataMap
            val availableRewards: List<POBReward>? = rewardedAd?.getAvailableRewards()
            if (availableRewards != null && availableRewards.size > 0) {
                customDataMap[OpenWrapSDK.KEY_SELECTED_REWARD] = availableRewards.get(0)
            }

            // Pass the custom data to be forwarded to MoPub SDK
            customDataMap[OpenWrapSDK.KEY_AD_SERVER_CUSTOM_DATA] = "Test mopub custom data"

            // Call show on rewarded
            rewardedAd?.show(customDataMap)
        } else {
            POBLog.debug(TAG, "RewardedAd not ready ")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rewardedAd?.destroy()
    }

    /**
     * Implementation class to receive the callback of VAST based video from Rewarded ad
     */
    internal inner class POBWrapperViewListener : POBRewardedAdListener() {
        /**
         * Notifies the listener that an ad has been received successfully.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         */
        override fun onAdReceived(rewardedAd: POBRewardedAd) {
            POBLog.debug(TAG, "onAdReceived is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "RewardedAd : Ad Received")
            findViewById<View>(R.id.showAdBtn).isEnabled = true
        }

        /**
         * Notifies the listener of an error encountered while loading or rendering an ad.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         * @param error      The error encountered while attempting to receive or render the ad.
         */
        override fun onAdFailedToLoad(rewardedAd: POBRewardedAd, error: POBError) {
            POBLog.debug(TAG, "onAdFailed is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "RewardedAd : Ad failed with error - $error")
        }

        /**
         * Notifies the listener that a user interaction will open another app (e.g. Chrome browser),
         * leaving the current app. To handle user clicks that open the landing page URL in the
         * internal browser, use 'onAdClicked()' instead.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         */
        override fun onAppLeaving(rewardedAd: POBRewardedAd) {
            POBLog.debug(TAG, "onAppLeave is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "RewardedAd : App Leave")
        }

        /**
         * Notifies that the OpenWrap view will open an ad on top of the current view.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         */
        override fun onAdOpened(rewardedAd: POBRewardedAd) {
            POBLog.debug(TAG, "onAdOpened is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "RewardedAd : Ad Opened")
        }

        /**
         * Notifies that the OpenWrap view has closed the ad on top of the current view.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         */
        override fun onAdClosed(rewardedAd: POBRewardedAd) {
            POBLog.debug(TAG, "onAdClosed is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "RewardedAd : Ad Closed")
        }

        /**
         * Notifies that the rewarded ad has been clicked
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         */
        override fun onAdClicked(rewardedAd: POBRewardedAd) {
            POBLog.debug(TAG, "onAdClicked is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "RewardedAd : Ad Clicked")
        }

        /**
         * Notifies that the rewarded ad has been expired. After this callback,
         * 'POBRewardedAd' instances marked as invalid and may not be presented and no impression
         * counting is considered. After Expiration callback, POBRewardedAd.isReady() returns
         * 'false'.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         */
        override fun onAdExpired(rewardedAd: POBRewardedAd) {
            POBLog.debug(TAG, "onAdExpired is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "RewardedAd : Ad Expired")
        }

        /**
         * Notifies that the rewarded ad has completed and user should be rewarded. It is called
         * when the Rewarded Ad playback is completed.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         * @param reward     Value of reward as an object of POBReward
         */
        override fun onReceiveReward(rewardedAd: POBRewardedAd, reward: POBReward) {
            // As this is callback method, No action Required
            POBLog.debug(TAG, "onReceiveReward is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            Toast.makeText(this@RewardedAdActivity, "Congratulation! You are rewarded with " + reward.amount + " " + reward.currencyType, Toast.LENGTH_LONG).show()
        }

        /**
         * Notifies the listener of an error encountered while rendering an ad.
         *
         * @param rewardedAd POBRewardedAd instance invoking this method.
         * @param error      The error encountered while attempting to rendering the ad.
         */
        override fun onAdFailedToShow(rewardedAd: POBRewardedAd, error: POBError) {
            POBLog.debug(TAG, "onAdFailedToShow is on Main thread: " + (Looper.myLooper() == Looper.getMainLooper()))
            POBLog.debug(TAG, "Rewarded Ad: Ad failed with error - $error")
        }
    }

    companion object {
        private val OPENWRAP_AD_UNIT_ONE: String = "2dff8350e1764c4d97e4e4da295020cc"
        private val PUB_ID: String = "156276"
        private val PROFILE_ID: Int = 1758
        private val MOPUB_AD_UNIT_ID: String = "2dff8350e1764c4d97e4e4da295020cc"
    }
}