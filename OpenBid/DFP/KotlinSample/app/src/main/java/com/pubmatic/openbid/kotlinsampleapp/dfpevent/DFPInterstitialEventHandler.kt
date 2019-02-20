/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2019 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.pubmatic.openbid.kotlinsampleapp.dfpevent

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.doubleclick.AppEventListener
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.openbid.core.POBBid
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEvent
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEventListener
import com.pubmatic.sdk.webrendering.ui.POBInterstitialRendering
import java.util.*

class DFPInterstitialEventHandler(val context: Context, val adUnitId: String) : AdListener(), POBInterstitialEvent, AppEventListener {


    private val TAG = "DFPInstlEventHandler"

    /**
     * Interface to get the DFP Interstitial ad and it's request builder, to configure the
     * properties.
     */
    interface DFPConfigListener {
        /**
         * This method is called before event handler makes ad request call to DFP SDK. It passes
         * DFP ad & request builder which will be used to make ad request. Publisher can
         * configure the ad request properties on the provided objects.
         * @param ad DFP Interstitial ad object
         * @param requestBuilder DFP Interstitial ad request builder
         */
        fun configure(ad: PublisherInterstitialAd,
                      requestBuilder: PublisherAdRequest.Builder)
    }

    /**
     * For every winning bid, a DFP SDK gives callback with below key via AppEventListener (from
     * DFP SDK). This key can be changed at DFP's line item.
     */
    private val PUBMATIC_WIN_KEY = "pubmaticdm"

    /**
     * Config listener to check if publisher want to config properties in DFP ad
     */
    private var dfpConfigListener: DFPConfigListener? = null

    /**
     * Flag to identify if PubMatic bid wins the current impression
     */
    private var notifiedBidWin: Boolean? = null

    private var isAppEventExpected: Boolean = false
    /**
     * Timer object to synchronize the onAppEvent() of DFP SDK with onAdLoaded()
     */
    private var timer: Timer? = null

    /**
     * DFP Banner ad view
     */
    private var dfpInterstitialAd: PublisherInterstitialAd? = null

    /**
     * Interface to pass the DFP ad event to OpenBid SDK
     */
    private var eventListener: POBInterstitialEventListener? = null

    private fun initializeDFP() {
        dfpInterstitialAd = PublisherInterstitialAd(context)
        dfpInterstitialAd?.adUnitId = adUnitId

        // DO NOT REMOVE/OVERRIDE BELOW LISTENERS
        dfpInterstitialAd?.adListener = this
        dfpInterstitialAd?.appEventListener = this
    }

    /**
     * Sets the Data listener object. Publisher should implement the DFPConfigListener and override
     * its method only when publisher needs to set the targeting parameters over DFP ad.
     *
     * @param listener DFP data listener
     */
    fun setConfigListener(listener: DFPConfigListener) {
        dfpConfigListener = listener
    }

    private fun resetDelay() {
        timer?.cancel()
        timer = null
    }

    private fun scheduleDelay() {

        resetDelay()

        val task = object : TimerTask() {
            override fun run() {
                notifyPOBAboutAdReceived()
            }
        }
        timer = Timer()
        timer?.schedule(task, 400)

    }

    private fun notifyPOBAboutAdReceived() {
        // If onAppEvent is not called within 400 milli-sec, consider that DFP wins
        notifiedBidWin.let {
            // Notify POB SDK about DFP ad win state and set the state
            notifiedBidWin = false
            eventListener?.onAdServerWin()
        }
    }

    private fun sendErrorToPOB(error: POBError?) {
        error.let {
            eventListener?.onFailed(error)
        }
    }


    override fun requestAd(bid: POBBid?) {
        // Reset the flag
        isAppEventExpected = false

        val requestBuilder = PublisherAdRequest.Builder()

        initializeDFP()

        // Check if publisher want to set any targeting data
        dfpInterstitialAd?.let { dfpConfigListener?.configure(it, requestBuilder) }

        // Warn publisher if he overrides the DFP listeners
        if (dfpInterstitialAd?.getAdListener() !== this || dfpInterstitialAd?.getAppEventListener() !== this) {
            Log.w(TAG, "Do not set DFP listeners. These are used by DFPInterstitialEventHandler internally.")
        }

        if (null != bid) {

            // Logging details of bid objects for debug purpose.
            Log.d(TAG, bid.toString())

            val targeting = bid.targetingInfo
            if (targeting != null && !targeting.isEmpty()) {
                // using for-each loop for iteration over Map.entrySet()
                for ((key, value) in targeting) {
                    requestBuilder.addCustomTargeting(key, value)
                    Log.d(TAG, "DFP custom param [$key] = $value")
                }
            }

            // Save this flag for future reference. It will be referred to wait for onAppEvent, only
            // if POB delivers non-zero bid to DFP SDK.
            val price = bid.price
            if (price > 0.0) {
                isAppEventExpected = true
            }
        }

        val adRequest = requestBuilder.build()

        // Publisher/App developer can add extra targeting parameters to dfpInterstitialAd here.
        notifiedBidWin = null

        // Load DFP ad request
        dfpInterstitialAd?.loadAd(adRequest)

    }

    override fun setEventListener(listener: POBInterstitialEventListener?) {
        eventListener = listener
    }

    override fun getRenderer(partnerName: String?): POBInterstitialRendering? {
        return null
    }

    override fun destroy() {
        resetDelay()
        dfpInterstitialAd = null
        dfpConfigListener = null
        eventListener = null
    }

    override fun show() {
        if (dfpInterstitialAd?.isLoaded == true) {
            dfpInterstitialAd?.show()
        } else {
            Log.e(TAG, "DFP SDK is not ready to show Interstitial Ad.")
            sendErrorToPOB(POBError(POBError.INTERSTITIAL_NOT_READY, "DFP SDK is not ready to show Interstitial Ad."))
        }
    }

    override fun onAppEvent(key: String?, p1: String?) {
        Log.d(TAG, "onAppEvent()")
        if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {
            // If onAppEvent is called before onAdLoaded(), it means POB bid wins
            if (notifiedBidWin == null) {
                notifiedBidWin = true
                eventListener?.onOpenBidPartnerWin()
            } else if (!(notifiedBidWin == true)) {
                // In this case onAppEvent is called in wrong order and within 400 milli-sec
                // Hence, notify POB SDK about DFP ad win state
                sendErrorToPOB(POBError(POBError.OPEN_BID_SIGNALING_ERROR,
                        "DFP ad server mismatched bid win signal"))
            }
        }
    }


    override fun onAdFailedToLoad(errCode: Int) {
        Log.d(TAG, "onAdFailedToLoad()")
        if(null != eventListener){
            when (errCode) {
                PublisherAdRequest.ERROR_CODE_INVALID_REQUEST -> eventListener?.onFailed(POBError(POBError.INVALID_REQUEST, "DFP SDK gives invalid request error"))
                PublisherAdRequest.ERROR_CODE_NETWORK_ERROR -> eventListener?.onFailed(POBError(POBError.NETWORK_ERROR, "DFP SDK gives network error"))
                PublisherAdRequest.ERROR_CODE_NO_FILL -> eventListener?.onFailed(POBError(POBError.NO_ADS_AVAILABLE, "DFP SDK gives no fill error"))
                else -> eventListener?.onFailed(POBError(POBError.INTERNAL_ERROR, "DFP SDK failed with error code: "+errCode))
            }
        }else{
            Log.e(TAG, "Can not call failure callback, POBInterstitialEventListener reference null. DFP error:$errCode")
        }
    }

    override fun onAdOpened() {
        eventListener?.onAdOpened()
    }

    override fun onAdClosed() {
        eventListener?.onAdClosed()
    }

    override fun onAdLoaded() {
        Log.d(TAG, "onAdServerWin()")
        if (eventListener != null) {

            // Wait only if onAppEvent() is not already called.
            if (notifiedBidWin == null) {

                // Check if POB bid delivers non-zero bids to DFP, then only wait
                if (isAppEventExpected) {
                    // Wait for 400 milli-sec to get onAppEvent before conveying to POB SDK
                    scheduleDelay()
                } else {
                    notifyPOBAboutAdReceived()
                }
            }
        }
    }

    override fun onAdLeftApplication() {
        super.onAdLeftApplication()
        eventListener?.onAdLeftApplication()
    }
}