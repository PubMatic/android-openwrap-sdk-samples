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
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityBannerBinding
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.openwrap.banner.POBBannerView
import com.pubmatic.sdk.openwrap.eventhandler.gamnextgen.GAMNextGenBannerEventHandler

/**
 * Demonstrates the Banner ad workflow using OpenWrap SDK with GAM Next-Gen SDK as the primary
 * ad server via [GAMNextGenBannerEventHandler].
 */
class NextGenBannerActivity : BaseSampleActivity(), LoggerListener {

    private lateinit var binding: ActivityBannerBinding
    private var banner: POBBannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge(binding.root, binding.includeToolbar.root, binding.contentContainer)
        setSupportActionBar(binding.includeToolbar.toolbar)
        binding.includeToolbar.toolbar.setTitle(R.string.title_activity_banner)

        Logger.registerLoggerListener(this)
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion())

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, the code below creates an event handler for GAM Next-Gen ad server.
        val eventHandler = GAMNextGenBannerEventHandler(
            this,
            Constants.AD_UNIT_BANNER,
            listOf(AdSize(320, 50)),
        )

        // Initialise banner view, set optional listener, and load the ad
        banner = binding.banner.apply {
            init(Constants.PUB_ID, Constants.PROFILE_ID, Constants.AD_UNIT_BANNER, eventHandler)
            setListener(BannerViewListener())
            loadAd()
        }
    }

    override fun onDestroy() {
        // Destroy banner before destroying activity
        banner?.destroy()
        Logger.removeLoggerListener(this)
        super.onDestroy()
    }

    // Appends a log message to the log TextView.
    override fun log(message: String) {
        binding.eventLogs.adEventLogs.append(message)
    }

    // Banner Ad listener callbacks
    private class BannerViewListener : POBBannerView.POBBannerViewListener() {
        // Callback method notifies that an ad has been successfully loaded and rendered.
        override fun onAdReceived(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdReceived")
        }

        // Callback method notifies an error encountered while loading or rendering an ad.
        override fun onAdFailed(view: POBBannerView, error: POBError) {
            Logger.log(Logger.LogType.ERROR, TAG, "onAdFailed : Ad failed with error - $error")
        }

        // Callback method notifies whenever current app goes in the background due to user click
        override fun onAppLeaving(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAppLeaving")
        }

        // Callback method notifies that the banner ad view is clicked.
        override fun onAdClicked(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClicked")
        }

        // Callback method notifies that the banner ad view will launch a dialog on top of the current view
        override fun onAdOpened(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdOpened")
        }

        // Callback method notifies that the banner ad view has dismissed the modal on top of the current view
        override fun onAdClosed(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClosed")
        }

        override fun onAdExpired(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdExpired")
        }
    }

    companion object {
        private const val TAG = "NextGenBannerActivity"
    }
}
