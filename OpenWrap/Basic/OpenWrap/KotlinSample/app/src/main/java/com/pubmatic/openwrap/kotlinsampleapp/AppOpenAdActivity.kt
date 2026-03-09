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
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.pubmatic.sdk.appopenad.POBAppOpenAd
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import java.net.MalformedURLException
import java.net.URL

/**
 * Activity to show App Open Ad Implementation
 */
class AppOpenAdActivity : AppCompatActivity(), LoggerListener {

    private val OPENWRAP_AD_UNIT_ID = "OpenWrapAppOpenAdAdUnit"

    private var appopenad: POBAppOpenAd? = null
    private var loadAd: Button? = null
    private var showAd: Button? = null
    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appopenad)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.title_activity_app_open_ad_template)

        logTextView = findViewById(R.id.ad_event_logs)
        Logger.registerLoggerListener(this)
        Logger.log(
            Logger.LogType.INFO,
            Constants.TAG_OWSDK,
            Constants.OWSDK_VERSION_LOG + OpenWrapSDK.getVersion()
        )

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

        // Initialise app open ad
        appopenad = POBAppOpenAd(this, Constants.PUB_ID, Constants.PROFILE_ID, OPENWRAP_AD_UNIT_ID)

        // Set optional listener
        appopenad?.setListener(POBAppOpenAdListener())

        // Set the optional listener to get the video events
        appopenad?.setVideoListener(POBAppOpenAdVideoListener())

        loadAd = findViewById(R.id.load_ad)
        loadAd?.setOnClickListener {
            // Call loadAd on app open
            appopenad?.loadAd()

        }

        showAd = findViewById(R.id.show_ad)
        showAd?.setEnabled(false)
        showAd?.setOnClickListener {
            // check if the app open is ready
            showAppOpenAd()
        }

    }

    private fun showAppOpenAd() {
        // check if the app open ad is ready
        if (appopenad?.isReady() == true) {
            // Call show on app open ad
            appopenad?.showAd()
        }
    }

    /**
     * Implementation class to receive the callback of VAST based video from App Open ad
     */
    private class POBAppOpenAdVideoListener : POBAppOpenAd.POBVideoListener {
        private val TAG = "POBVideoListener"

        // Callback method notifies that playback of the VAST video has been completed
        override fun onVideoPlaybackCompleted(ad: POBAppOpenAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onVideoPlaybackCompleted")
        }
    }

    /**
     * Implementation class to receive app open ad interaction
     */
    private inner class POBAppOpenAdListener : POBAppOpenAd.POBAppOpenAdListener() {
        val TAG = "POBAppOpenAdListener"


        // Callback method notifies that an ad has been received successfully.
        override fun onAdReceived(ad: POBAppOpenAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdReceived")
            // Method gets called when ad gets loaded in container
            // Here, you can show app open ad to user
            showAd?.setEnabled(true)
        }

        // Callback method notifies an error encountered while loading an ad.
        override fun onAdFailedToLoad(ad: POBAppOpenAd, error: POBError) {
            Logger.log(
                Logger.LogType.ERROR,
                TAG,
                "App Open Ad : Ad failed to load with error - $error"
            )
            //Method gets called when it fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies an error encountered while showing an ad.
        override fun onAdFailedToShow(ad: POBAppOpenAd, error: POBError) {
            Logger.log(
                Logger.LogType.ERROR,
                TAG,
                "App Open Ad : Ad failed to Show with error - $error"
            )
            //Method gets called when it fails to show ad
            //Here, you can put logger and see why ad failed to show
        }

        // Callback method notifies that the app open ad will be presented as a modal on top of the current view.
        override fun onAdOpened(ad: POBAppOpenAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdOpened")
        }

        // Callback method notifies that the app open ad has been animated off the screen.
        override fun onAdClosed(ad: POBAppOpenAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClosed")
            showAd?.setEnabled(false)
        }

        // Callback method notifies ad click
        override fun onAdClicked(ad: POBAppOpenAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdClicked")
        }

        // Callback method Notifies whenever current app goes in the background due to user click
        override fun onAppLeaving(ad: POBAppOpenAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAppLeaving")
        }

        // Callback method Notifies impression recorded on ad view
        override fun onAdImpression(ad: POBAppOpenAd) {
            Logger.log(Logger.LogType.DEBUG, TAG, "onAdImpression")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // destroy app open
        appopenad?.destroy()
        Logger.removeLoggerListener(this)
    }

    // Appends a log message to the log TextView.
    override fun log(message: String) {
        logTextView.append(message + "\n")
    }
}
