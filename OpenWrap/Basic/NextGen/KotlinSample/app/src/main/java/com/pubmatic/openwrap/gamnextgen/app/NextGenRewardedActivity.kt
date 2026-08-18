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

package com.pubmatic.openwrap.gamnextgen.app

import android.os.Bundle
import android.widget.Toast
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityRewardedBinding
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.openwrap.core.POBReward
import com.pubmatic.sdk.openwrap.eventhandler.gamnextgen.GAMNextGenRewardedEventHandler
import com.pubmatic.sdk.rewardedad.POBRewardedAd
import java.lang.ref.WeakReference

/**
 * Demonstrates the Rewarded ad workflow using OpenWrap SDK with GAM Next-Gen SDK.
 */
class NextGenRewardedActivity : BaseSampleActivity(), LoggerListener {

    private lateinit var binding: ActivityRewardedBinding
    private var rewardedAd: POBRewardedAd? = null
    private var eventHandler: GAMNextGenRewardedEventHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge(binding.root, binding.includeToolbar.root, binding.controls.root)
        setSupportActionBar(binding.includeToolbar.toolbar)
        binding.includeToolbar.toolbar.setTitle(R.string.title_activity_rewarded)

        Logger.registerLoggerListener(this)
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion())

        // Create a rewarded custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each rewarded ad instance.
        // For example, the code below creates an event handler for GAM Next-Gen ad server.
        val rewardedEventHandler = GAMNextGenRewardedEventHandler(Constants.AD_UNIT_REWARDED)
        eventHandler = rewardedEventHandler

        // Create POBRewardedAd instance and set optional callback listener
        rewardedAd = POBRewardedAd.getRewardedAd(
            this,
            Constants.PUB_ID,
            Constants.PROFILE_ID_FOR_VIDEO,
            Constants.AD_UNIT_REWARDED,
            rewardedEventHandler,
        )?.apply {
            setListener(RewardedAdListener(this@NextGenRewardedActivity))
        }

        // Load rewarded Ad
        binding.controls.showAdBtn.isEnabled = false
        binding.controls.loadAdBtn.setOnClickListener {
            binding.controls.showAdBtn.isEnabled = false
            rewardedAd?.loadAd()
        }

        // Show rewarded Ad
        binding.controls.showAdBtn.setOnClickListener {
            showRewardedAd()
            binding.controls.showAdBtn.isEnabled = false
        }
    }

    /** To show rewarded ad call this method */
    private fun showRewardedAd() {
        // Call show when ad is ready
        if (rewardedAd?.isReady == true) {
            // Call show on rewardedAd
            rewardedAd?.show(this)
        }
    }

    override fun onDestroy() {
        rewardedAd?.destroy()
        eventHandler?.destroy()
        Logger.removeLoggerListener(this)
        super.onDestroy()
    }

    // Appends a log message to the log TextView.
    override fun log(message: String) {
        binding.controls.eventLogs.adEventLogs.append(message)
    }

    // Rewarded Ad listener callbacks
    private class RewardedAdListener(activity: NextGenRewardedActivity) :
        POBRewardedAd.POBRewardedAdListener() {

        private val activityRef = WeakReference(activity)

        // Callback method notifies that an ad has been received successfully.
        override fun onAdReceived(ad: POBRewardedAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdReceived")
            ActivityUiUtil.enableShowAdButton(activityRef.get())
        }

        override fun onAdFailedToLoad(ad: POBRewardedAd, error: POBError) {
            Logger.log(Logger.LogType.ERROR, TAG, "Rewarded:onAdFailedToLoad : Ad failed with load error - $error")
        }

        override fun onAdFailedToShow(ad: POBRewardedAd, error: POBError) {
            Logger.log(Logger.LogType.ERROR, TAG, "Rewarded:onAdFailedToShow : Ad failed with show error - $error")
        }

        override fun onAppLeaving(ad: POBRewardedAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAppLeaving")
        }

        override fun onAdOpened(ad: POBRewardedAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdOpened")
        }

        override fun onAdClosed(ad: POBRewardedAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdClosed")
        }

        override fun onAdClicked(ad: POBRewardedAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdClicked")
        }

        override fun onAdImpression(ad: POBRewardedAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Rewarded:onAdImpression")
        }

        // Callback method notifies that the user should be rewarded
        override fun onReceiveReward(ad: POBRewardedAd, reward: POBReward) {
            Logger.log(
                Logger.LogType.DEBUG,
                TAG,
                "Rewarded: Ad should Reward - ${reward.amount} (${reward.currencyType})",
            )
            val activity = activityRef.get()
            if (activity != null && !activity.isFinishing) {
                Toast.makeText(
                    activity.applicationContext,
                    "Congratulations! You are rewarded with ${reward.amount} ${reward.currencyType}",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "NextGenRewardedActivity"
    }
}
