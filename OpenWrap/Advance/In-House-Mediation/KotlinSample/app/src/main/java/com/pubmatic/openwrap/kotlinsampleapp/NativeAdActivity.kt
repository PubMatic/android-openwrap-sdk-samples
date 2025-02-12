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

package com.pubmatic.openwrap.kotlinsampleapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.nativead.POBNativeAd
import com.pubmatic.sdk.nativead.POBNativeAdListener
import com.pubmatic.sdk.nativead.POBNativeAdLoader
import com.pubmatic.sdk.nativead.POBNativeAdLoaderListener
import com.pubmatic.sdk.nativead.datatype.POBNativeTemplateType
import com.pubmatic.sdk.openwrap.core.POBBid
import com.pubmatic.sdk.openwrap.core.POBBidEvent
import com.pubmatic.sdk.openwrap.core.POBBidEventListener
import kotlinx.android.synthetic.main.activity_native_ad.toolbar

import java.net.MalformedURLException
import java.net.URL

/**
 * Activity to show NativeAd Implementation for bid caching flow.
 */
class NativeAdActivity : AppCompatActivity() {

    private val TAG = "NativeAdActivity"
    private val OPENWRAP_AD_UNIT_ID = "OpenWrapNativeAdUnit"

    private lateinit var nativeAdLoader: POBNativeAdLoader

    private var nativeAd: POBNativeAd? = null

    private lateinit var renderAd: Button

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_ad)
        setSupportActionBar(toolbar)
        val loadAd = findViewById<Button>(R.id.load_ad)
        renderAd = findViewById(R.id.render_ad)
        container = findViewById(R.id.container)

        loadAd.setOnClickListener {
            // Load the native ad
            nativeAd = null
            container.removeAllViews()
            renderAd.setEnabled(false)
            nativeAdLoader.loadAd()
        }

        renderAd.setOnClickListener {
            // Set the native ad listener to listen the event callback and also to receive the
            // rendered native ad view.
            nativeAd?.renderAd(
                NativeAdListenerImpl()
            )
        }

        OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.All)

        // A valid Play Store Url of an Android app. Required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.storeURL =
                URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo)

        // Create native ad loader to make request to openWrap
        nativeAdLoader = POBNativeAdLoader(
            this, Constants.PUB_ID, Constants.PROFILE_ID,
            OPENWRAP_AD_UNIT_ID, POBNativeTemplateType.SMALL
        )

        //Set the ad loader listener to listens the ad received and ad failed to load callback
        nativeAdLoader.setAdLoaderListener(NativeAdLoaderListenerImpl())

        // Below code block can be used get bid event callbacks.
        nativeAdLoader.setBidEventListener(object : POBBidEventListener {
            override fun onBidReceived(bidEvent: POBBidEvent, bid: POBBid) {
                // Make use of the received bid,  e.g. perform auction with your setup
                Log.d(TAG, "Bid received.")
                // Notify native ad to proceed to load the ad after using the bid.
                Log.d(TAG, "Proceeding with load ad.")
                bidEvent.proceedToLoadAd()
            }

            override fun onBidFailed(bidEvent: POBBidEvent, error: POBError) {
                Log.d(TAG, String.format("Bid receive failed with error : %s", error.toString()))
                bidEvent.proceedOnError(
                    POBBidEvent.BidEventError.CLIENT_SIDE_AUCTION_LOSS,
                    "Bid lost client side auction."
                )
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd?.destroy()
    }


    /**
     * Listener to get callback for ad received and ad failed.
     */
    inner class NativeAdLoaderListenerImpl : POBNativeAdLoaderListener {
        override fun onAdReceived(nativeAdLoader: POBNativeAdLoader, nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Received")
            //Caching nativeAd instance to call renderAd method and to destroy it when activity get
            // destroyed.
            this@NativeAdActivity.nativeAd = nativeAd
            this@NativeAdActivity.renderAd.isEnabled = true
        }

        override fun onFailedToLoad(nativeAdLoader: POBNativeAdLoader, error: POBError) {
            Log.e(TAG, error.toString())
        }
    }

    /**
     * Listener to get callback for rendered native ad view and native events.
     */
    inner class NativeAdListenerImpl : POBNativeAdListener {
        override fun onNativeAdRendered(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Rendered")
            // Add the received rendered native ad view in your container
            container.addView(nativeAd.adView)
        }

        override fun onNativeAdRenderingFailed(nativeAd: POBNativeAd, error: POBError) {
            Log.e(TAG, error.toString())
        }

        override fun onNativeAdImpression(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad recorded Impression")
        }

        override fun onNativeAdClicked(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Clicked")
        }

        override fun onNativeAdClicked(nativeAd: POBNativeAd, assetId: String) {
            Log.d(TAG, "Ad clicked for asset id - $assetId")
        }

        override fun onNativeAdLeavingApplication(nativeAd: POBNativeAd) {
            Log.d(TAG, "App Leaving")
        }

        override fun onNativeAdOpened(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Opened")
        }

        override fun onNativeAdClosed(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Closed")
        }
    }
}
