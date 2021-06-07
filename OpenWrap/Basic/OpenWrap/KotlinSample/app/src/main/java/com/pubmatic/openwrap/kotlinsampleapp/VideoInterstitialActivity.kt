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
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitial
import kotlinx.android.synthetic.main.activity_interstitial.*
import java.net.MalformedURLException
import java.net.URL

/**
 * Activity to show Interstitial Implementation
 */
class VideoInterstitialActivity : AppCompatActivity() {

    private val OPENWRAP_AD_UNIT_ID = "OpenWrapInterstitialAdUnit"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1757

    private var interstitial : POBInterstitial? = null
    private var loadAd: Button? = null;
    private var showAd: Button? = null;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial)
        setSupportActionBar(toolbar)

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

        // Initialise interstitial ad
        interstitial = POBInterstitial(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID)

        // Set optional listener
        interstitial?.setListener(POBInterstitialListener())

        // Set the optional listener to get the video events
        interstitial?.setVideoListener(POBInterstitialVideoListener())

        loadAd = findViewById(R.id.load_ad)
        loadAd?.setOnClickListener {
            // Call loadAd on interstitial
            interstitial?.loadAd()

        }

        showAd = findViewById(R.id.show_ad)
        showAd?.setEnabled(false)
        showAd?.setOnClickListener {
            // check if the interstitial is ready
            showInterstitialAd()
            showAd?.setEnabled(false)
        }

    }

    private fun showInterstitialAd(){
        // check if the interstitial is ready
        if(interstitial?.isReady == true){
            // Call show on interstitial
            interstitial?.show()
        }
    }

    /**
     * Implementation class to receive the callback of VAST based video from Interstitial ad
     */
    inner class POBInterstitialVideoListener : POBInterstitial.POBVideoListener() {
        val TAG = "POBVideoListener"

        // Callback method notifies that playback of the VAST video has been completed
        override fun onVideoPlaybackCompleted(ad: POBInterstitial) {
            Log.d(TAG, "onVideoPlaybackCompleted")
        }

    }

    /**
     * Implementation class to receive Interstitial ad interaction
     */
    inner class POBInterstitialListener : POBInterstitial.POBInterstitialListener() {
        val TAG = "POBInterstitialListener"


        // Callback method notifies that an ad has been received successfully.
        override fun onAdReceived(ad: POBInterstitial?) {
            Log.d(TAG, "onAdReceived")
            //Method gets called when ad gets loaded in container
            //Here, you can show interstitial ad to user
            showAd?.setEnabled(true)
        }

        // Callback method notifies an error encountered while loading an ad.
        override fun onAdFailedToLoad(ad: POBInterstitial, error: POBError) {
            Log.e(TAG, "Interstitial : Ad failed to load with error - " + error.toString())
            //Method gets called when it fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies an error encountered while showing an ad.
        override fun onAdFailedToShow(ad: POBInterstitial, error: POBError) {
            Log.e(TAG, "Interstitial : Ad failed to show with error - " + error.toString())
            //Method gets called when it fails to show ad
            //Here, you can put logger and see why ad failed to show
        }

        // Callback method notifies that the interstitial ad will be presented as a modal on top of the current view.
        override fun onAdOpened(ad: POBInterstitial?) {
            Log.d(TAG, "onAdOpened")
        }

        // Callback method notifies that the interstitial ad has been animated off the screen.
        override fun onAdClosed(ad: POBInterstitial?) {
            Log.d(TAG, "onAdClosed")
        }

        // Callback method notifies ad click
        override fun onAdClicked(ad: POBInterstitial?) {
            Log.d(TAG, "onAdClicked")
        }

        override fun onAppLeaving(ad: POBInterstitial?) {
            Log.d(TAG, "onAppLeaving")
        }

        // Callback method notifies ad expiration
        override fun onAdExpired(ad: POBInterstitial?) {
            Log.d(TAG, "onAdExpired");
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // destroy interstitial
        interstitial?.destroy()
    }
}