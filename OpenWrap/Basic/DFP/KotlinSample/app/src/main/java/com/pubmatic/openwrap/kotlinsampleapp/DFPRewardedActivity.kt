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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.core.POBReward
import com.pubmatic.sdk.openwrap.eventhandler.dfp.DFPRewardedEventHandler
import com.pubmatic.sdk.rewardedad.POBRewardedAd
import kotlinx.android.synthetic.main.activity_dfp_rewarded.*
import java.net.MalformedURLException
import java.net.URL

/**
 * This class demonstrate the Rewarded Ad workflow via OW SDK where DFP / GAM SDK is integrated
 * as a primary ad SDK.
 */
class DFPRewardedActivity : AppCompatActivity() {

    private val OPENWRAP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-RewardedAd"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1757
    private val DFP_AD_UNIT = "/15671365/pm_sdk/PMSDK-Demo-App-RewardedAd"
    private var rewarded : POBRewardedAd? = null
    private var loadAd: Button? = null
    private var showAd: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dfp_rewarded)
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

        // Create an rewarded custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each rewarded ad instance.
        // For example, The code below creates an event handler for DFP ad server.
        val eventHandler = DFPRewardedEventHandler(this, DFP_AD_UNIT)

        // Initialise Rewarded ad
        rewarded = POBRewardedAd.getRewardedAd(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, eventHandler)

        // Set optional listener
        rewarded?.setListener(POBRewardedAdListener())


        //Load Ad Button
        loadAd = findViewById(R.id.loadAdBtn)
        loadAd?.setOnClickListener {
            showAd?.isEnabled = false
            rewarded?.loadAd()

        }

        //Show Ad Button
        showAd = findViewById(R.id.showAdBtn)
        showAd?.setOnClickListener{
            showRewardedAd()
        }
    }

    private fun showRewardedAd(){
        if(rewarded?.isReady() == true){
            rewarded?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //Destroy Rewarded Ad
        rewarded?.destroy()
    }

    /**
     * POBInterstitialAdListener instance for getting the ad workflow callcback from OW SDK
     */
    inner class POBRewardedAdListener : POBRewardedAd.POBRewardedAdListener() {
        val TAG = "POBRewardedAdListener"


        // Callback method notifies that an ad has been received successfully.
        override fun onAdReceived(ad: POBRewardedAd) {
            Log.d(TAG, "Rewarded:onAdReceived")
            //Method gets called when ad gets loaded in container
            //Here, you can show rewarded ad to user
            showAd?.setEnabled(true)
        }

        // Callback method notifies an error encountered while loading an ad.
        override fun onAdFailedToLoad(ad: POBRewardedAd, error: POBError) {
            Log.e(TAG, "Rewarded:onAdFailedToLoad : Ad failed with load error - " + error.toString())
            //Method gets called when loadAd fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies an error encountered while rendering an ad.
        override fun onAdFailedToShow(ad: POBRewardedAd, error: POBError) {
            Log.e(TAG, "Rewarded:onAdFailedToShow : Ad failed with show error - " + error.toString())
            //Method gets called when loadAd fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies that a user interaction will open another app (for example, App Store), leaving the current app.
        override fun onAppLeaving(ad: POBRewardedAd) {
            Log.d(TAG, "Rewarded:onAppLeaving")
        }

        // Callback method notifies that the rewarded ad will be presented as a modal on top of the current view.
        override fun onAdOpened(ad: POBRewardedAd) {
            Log.d(TAG, "Rewarded:onAdOpened")
        }

        // Callback method notifies that the rewarded ad has been animated off the screen.
        override fun onAdClosed(ad: POBRewardedAd) {
            Log.d(TAG, "Rewarded:onAdClosed")
        }

        // Callback method notifies ad click
        override fun onAdClicked(ad: POBRewardedAd) {
            Log.d(TAG, "Rewarded:onAdClicked")
        }

        // Callback method notifies on Ad Impression
        override fun onAdImpression(ad: POBRewardedAd) {
            Log.d(TAG, "Rewarded:onAdImpression")
        }

        //Callback method notifies about rewards received
        override fun onReceiveReward(ad: POBRewardedAd, reward: POBReward){

            // As this is callback method, No action Required
            Log.d(TAG, "Rewarded: Ad should Reward -"+reward.amount + "("+ reward.currencyType+")")
            Toast.makeText(applicationContext, "Congratulation! You are rewarded with " + reward.getAmount().toString() + " " + reward.getCurrencyType(), Toast.LENGTH_LONG).show()
        }
    }
}