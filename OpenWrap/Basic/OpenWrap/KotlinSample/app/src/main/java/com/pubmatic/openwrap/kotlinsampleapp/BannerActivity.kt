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
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBAdSize
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.banner.POBBannerView
import kotlinx.android.synthetic.main.activity_banner.*
import java.net.MalformedURLException
import java.net.URL

/**
 * Activity to show Banner Implementation
 */
class BannerActivity : AppCompatActivity() {

    private val OPENWRAP_AD_UNIT_ID = "OpenWrapBannerAdUnit"

    private var banner: POBBannerView ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.title_activity_banner)

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
        // For test IDs see - https://help.pubmatic.com/openwrap/docs/test-and-debug-your-integration#test-profileplacements
        banner = findViewById(R.id.banner)
        banner?.init(Constants.PUB_ID, Constants.PROFILE_ID, OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_320x50)

        //optional listener to listen banner events
        banner?.setListener(POBBannerViewListener())

        // Call loadAd() on banner instance
        banner?.loadAd()


    }

    /**
     * Implementation class to receive Banner ad interaction
     */
    class POBBannerViewListener : POBBannerView.POBBannerViewListener(){
        val TAG = "POBBannerViewListener"

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

        // Callback method Notifies whenever current app goes in the background due to user click
        override fun onAppLeaving(view: POBBannerView) {
            Log.d(TAG, "onAppLeaving")
        }

        // Callback method Notifies impression recorded on ad view
        override fun onAdImpression(view: POBBannerView) {
            Log.d(TAG, "onAdImpression")
        }


    }


    override fun onDestroy() {
        // destroy banner before onDestroy of Activity lifeCycle
        super.onDestroy()
        banner?.destroy()
    }
}
