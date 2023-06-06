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
import androidx.appcompat.app.AppCompatActivity
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBAdSize
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.banner.POBBannerView
import com.pubmatic.sdk.openwrap.core.POBBid
import com.pubmatic.sdk.openwrap.core.POBBidEvent
import com.pubmatic.sdk.openwrap.core.POBBidEventListener
import kotlinx.android.synthetic.main.activity_banner.*
import java.net.MalformedURLException
import java.net.URL


/**
 * Activity to show Banner Implementation for bid caching flow
 */
class BannerActivity : AppCompatActivity() {

    private val TAG = "BannerActivity"

    private val OPENWRAP_AD_UNIT_ID = "OpenWrapBannerAdUnit"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1165

    private var banner: POBBannerView ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner)
        setSupportActionBar(toolbar)
        OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.All)

        // A valid Play Store Url of an Android app. Required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.setStoreURL(URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo)

        // Call init() to set tag information
        // For test IDs see - https://community.pubmatic.com/x/mQg5AQ#TestandDebugYourIntegration-TestWrapperProfile/Placement
        banner = findViewById(R.id.banner)
        banner?.init(PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_320x50)

        //optional listener to listen banner events
        banner?.setListener(POBBannerViewListener())

        // Below code block can be used get bid event callbacks.
        banner?.setBidEventListener(object : POBBidEventListener {
            override fun onBidReceived(bidEvent: POBBidEvent, bid: POBBid) {
                // Make use of the received bid,  e.g. perform auction with your setup
                Log.d(TAG, "Bid received.")
                // Notify banner view to proceed to load the ad after using the bid.
                useBidAndProceed()
            }

            override fun onBidFailed(bidEvent: POBBidEvent, error: POBError) {
                Log.d(TAG, String.format("Bid receive failed with error : %s",  error.toString()))
                banner?.proceedOnError(POBBidEvent.BidEventError.CLIENT_SIDE_AUCTION_LOSS)
            }
        })

        // Call loadAd() on banner instance
        banner?.loadAd()
    }

    private fun useBidAndProceed() {
        // Call banner?.proceedToLoadAd() to complete the flow
        Log.d(TAG, "Proceeding with load ad.")
        banner?.proceedToLoadAd()
    }

    /**
     * Implementation class to receive Banner ad interaction
     */
    inner class POBBannerViewListener : POBBannerView.POBBannerViewListener(){

        // Callback method Notifies that an  banner ad has been successfully loaded and rendered.
        override fun onAdReceived(view: POBBannerView) {
            Log.d(TAG, "onAdReceived")
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        override fun onAdFailed(view: POBBannerView, error: POBError) {
            Log.e(TAG, "onAdFailed : Ad failed with error - " + error.toString())
        }

        // Callback method Notifies that the  banner ad will launch a dialog on top of the current view
        override fun onAdOpened(view: POBBannerView) {
            Log.d(TAG, "onAdOpened")
        }

        // Callback method Notifies that the banner ad view is clicked.
        override fun onAdClicked(view: POBBannerView) {
            Log.d(TAG, "onAdClicked")
        }

        // Callback method Notifies that the  banner ad has dismissed the modal on top of the current view
        override fun onAdClosed(view: POBBannerView) {
            Log.d(TAG, "onAdClosed")
        }

        override fun onAppLeaving(view: POBBannerView) {
            Log.d(TAG, "Banner : App Leaving")
        }
    }

    override fun onDestroy() {
        // destroy banner before onDestroy of Activity lifeCycle
        super.onDestroy()
        banner?.destroy()
    }
}
