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
import android.widget.LinearLayout
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.pubmatic.openwrap.gamnextgen.app.databinding.ActivityNativeBannerBinding
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.openwrap.banner.POBBannerView
import com.pubmatic.sdk.openwrap.eventhandler.gamnextgen.GAMNextGenNativeBannerEventHandler
import java.lang.ref.WeakReference

/**
 * Demonstrates the Native and Banner combined ad workflow using OpenWrap SDK with GAM Next-Gen SDK.
 */
class NextGenNativeBannerActivity : BaseSampleActivity(), LoggerListener {

    private lateinit var binding: ActivityNativeBannerBinding
    private var banner: POBBannerView? = null
    private var nativeAd: NativeAd? = null
    private var customNativeAd: CustomNativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeBannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge(binding.root, binding.includeToolbar.root, binding.container)
        setSupportActionBar(binding.includeToolbar.toolbar)
        binding.includeToolbar.toolbar.setTitle(R.string.title_activity_native_banner)

        Logger.registerLoggerListener(this)
        Logger.log(Logger.LogType.INFO, Constants.TAG_OWSDK, Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion())

        // Create a native banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        val eventHandler = GAMNextGenNativeBannerEventHandler(
            this,
            Constants.AD_UNIT_NATIVE_BANNER,
            listOf(AdSize(300, 250)),
        ).apply {
            // Prepares handler to request GAM Next-Gen NativeCustomFormatAd
            configureNativeCustomFormatAd(listOf(Constants.CUSTOM_NATIVE_FORMAT))
            // Prepares handler to receive GAM Next-Gen NativeAd and CustomNativeAd via callback
            gamNativeAdLoaderCallback = NativeAdLoaderCallback(this@NextGenNativeBannerActivity)
        }

        // Initialise banner view, set optional listener, and load the ad
        banner = POBBannerView(
            this,
            Constants.PUB_ID,
            Constants.PROFILE_ID,
            Constants.AD_UNIT_NATIVE_BANNER,
            eventHandler,
        ).apply {
            setListener(BannerViewListener(this@NextGenNativeBannerActivity))
            loadAd()
        }
    }

    private fun renderNativeAd(ad: NativeAd) {
        if (isDestroyed || isFinishing || isChangingConfigurations) {
            ad.destroy()
            return
        }
        nativeAd?.destroy()
        nativeAd = ad
        GAMNextGenNativeAdsUIUtil.renderNativeAd(ad, binding.container, layoutInflater)
    }

    private fun renderCustomNativeAd(ad: CustomNativeAd) {
        if (isDestroyed || isFinishing || isChangingConfigurations) {
            ad.destroy()
            return
        }
        customNativeAd?.destroy()
        customNativeAd = ad
        GAMNextGenNativeAdsUIUtil.renderCustomNativeAd(ad, binding.container, layoutInflater)
    }

    override fun onDestroy() {
        // Destroy banner and native ads before destroying activity
        banner?.destroy()
        nativeAd?.destroy()
        customNativeAd?.destroy()
        Logger.removeLoggerListener(this)
        super.onDestroy()
    }

    // Appends a log message to the log TextView.
    override fun log(message: String) {
        binding.eventLogs.adEventLogs.append(message)
    }

    fun getAdContainer(): LinearLayout = binding.container

    // Native Ad loader callbacks
    private class NativeAdLoaderCallback(activity: NextGenNativeBannerActivity) :
        GAMNextGenNativeBannerEventHandler.GAMNativeAdLoaderCallback {

        private val host = WeakReferenceHost(activity)

        // Callback method notifies that a standard native ad has been loaded.
        override fun onNativeAdLoaded(nativeAd: NativeAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Native Ad Received")
            val activity = host.get()
            if (activity != null) {
                activity.renderNativeAd(nativeAd)
            } else {
                nativeAd.destroy()
            }
        }

        // Callback method notifies that a custom native ad has been loaded.
        override fun onCustomNativeAdLoaded(customNativeAd: CustomNativeAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "Custom Native Ad Received")
            val activity = host.get()
            if (activity != null) {
                activity.renderCustomNativeAd(customNativeAd)
            } else {
                customNativeAd.destroy()
            }
        }
    }

    // Banner Ad listener callbacks
    private class BannerViewListener(activity: NextGenNativeBannerActivity) :
        POBBannerView.POBBannerViewListener() {

        private val host = WeakReferenceHost(activity)

        // Callback method notifies that an ad has been successfully loaded and rendered.
        override fun onAdReceived(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdReceived")
            val activity = host.get() ?: return
            val adSize = view.creativeSize ?: return
            Logger.log(Logger.LogType.DEBUG, TAG, "Banner : Ad Received with size {$adSize}")
            activity.getAdContainer().removeAllViews()
            activity.getAdContainer().addView(view)
        }

        override fun onAdFailed(view: POBBannerView, error: POBError) {
            Logger.log(Logger.LogType.ERROR, TAG, "onAdFailed : Ad failed with error - $error")
        }

        override fun onAppLeaving(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAppLeaving")
        }

        override fun onAdOpened(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdOpened")
        }

        override fun onAdClicked(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClicked")
        }

        override fun onAdClosed(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClosed")
        }

        override fun onAdExpired(view: POBBannerView) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdExpired")
        }
    }

    private class WeakReferenceHost(activity: NextGenNativeBannerActivity) {
        private val activityRef = WeakReference(activity)

        fun get(): NextGenNativeBannerActivity? {
            val activity = activityRef.get()
            return if (activity == null || activity.isFinishing) null else activity
        }
    }

    companion object {
        private const val TAG = "NextGenNativeBannerActivity"
    }
}
