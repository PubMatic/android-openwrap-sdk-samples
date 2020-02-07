package com.pubmatic.openwrap.kotlinsampleapp.dfpevent

import android.content.Context
import android.text.TextUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.doubleclick.AppEventListener
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.log.PMLog
import com.pubmatic.sdk.common.ui.POBInterstitialRendering
import com.pubmatic.sdk.openwrap.core.POBBid
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitialEvent
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitialEventListener
import java.util.*

/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class implements the communication between the OpenWrap SDK and the DFP SDK for a given ad
 * unit. It implements the PubMatic's OpenWrap interface. OpenWrap SDK notifies (using OpenWrap interface)
 * to make a request to DFP SDK and pass the targeting parameters. This class also creates the DFP's
 * PublisherInterstitialAd, initialize it and listen for the callback methods. And pass the DFP ad
 * event to OpenWrap SDK via POBInterstitialEventListener.
 */
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
     * Interface to pass the DFP ad event to OpenWrap SDK
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
            PMLog.warn(TAG, "Do not set DFP listeners. These are used by DFPInterstitialEventHandler internally.")
        }

        if (null != bid) {

            // Logging details of bid objects for debug purpose.
            PMLog.debug(TAG, bid.toString())

            val targeting = bid.targetingInfo
            if (targeting != null && !targeting.isEmpty()) {
                // using for-each loop for iteration over Map.entrySet()
                for ((key, value) in targeting) {
                    requestBuilder.addCustomTargeting(key, value)
                    PMLog.debug(TAG, "DFP custom param [$key] = $value")
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
            val errMsg = "DFP SDK is not ready to show Interstitial Ad."
            sendErrorToPOB(POBError(POBError.INTERSTITIAL_NOT_READY, errMsg))
            PMLog.error(TAG, errMsg)
        }
    }

    override fun onAppEvent(key: String?, p1: String?) {
        PMLog.info(TAG, "onAppEvent()")
        if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {
            // If onAppEvent is called before onAdLoaded(), it means POB bid wins
            if (notifiedBidWin == null) {
                notifiedBidWin = true
                eventListener?.onOpenWrapPartnerWin()
            } else if (!(notifiedBidWin == true)) {
                // In this case onAppEvent is called in wrong order and within 400 milli-sec
                // Hence, notify POB SDK about DFP ad win state
                sendErrorToPOB(POBError(POBError.OPENWRAP_SIGNALING_ERROR,
                        "DFP ad server mismatched bid win signal"))
            }
        }
    }


    override fun onAdFailedToLoad(errCode: Int) {
        PMLog.info(TAG, "onAdFailedToLoad()")
        if(null != eventListener){
            when (errCode) {
                PublisherAdRequest.ERROR_CODE_INVALID_REQUEST -> eventListener?.onFailed(POBError(POBError.INVALID_REQUEST, "DFP SDK gives invalid request error"))
                PublisherAdRequest.ERROR_CODE_NETWORK_ERROR -> eventListener?.onFailed(POBError(POBError.NETWORK_ERROR, "DFP SDK gives network error"))
                PublisherAdRequest.ERROR_CODE_NO_FILL -> eventListener?.onFailed(POBError(POBError.NO_ADS_AVAILABLE, "DFP SDK gives no fill error"))
                else -> eventListener?.onFailed(POBError(POBError.INTERNAL_ERROR, "DFP SDK failed with error code: "+errCode))
            }
        }else{
            PMLog.error(TAG, "Can not call failure callback, POBInterstitialEventListener reference null. DFP error:$errCode")
        }
    }

    override fun onAdOpened() {
        eventListener?.onAdOpened()
    }

    override fun onAdClosed() {
        eventListener?.onAdClosed()
    }

    override fun onAdLoaded() {
        PMLog.info(TAG, "onAdServerWin()")
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
        //DFP interstitial does not provide onAdClick event, event handler
        // intercepts DFP's onAdLeftApplication and provide both callbacks onAdClick as well as onAdLeftApplication sequentially
        eventListener?.onAdClick()
        eventListener?.onAdLeftApplication()
    }
}