package com.pubmatic.openwrap.kotlinsampleapp.customhandler

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.pubmatic.openwrap.kotlinsampleapp.dummyadserver.DummyAdServerSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.ui.POBInterstitialRendering
import com.pubmatic.sdk.openwrap.core.POBBid
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitialEvent
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitialEventListener

/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class is responsible for communication between OpenWrap interstitial and interstitial ad from your ad server SDK(in this case DummyAdServerSDK).
 * It implements the POBInterstitialEvent protocol. it notifies event back to OpenWrap SDK using POBInterstitialEventListener methods
 */
@SuppressLint("LongLogTag")
class CustomInterstitialEventHandler
(val context: Context, adUnitId: String) : POBInterstitialEvent,  DummyAdServerSDK.DummyAdServerEventListener(){

    private var eventListener: POBInterstitialEventListener? = null
    private val adServer: DummyAdServerSDK

    init {
        adServer = DummyAdServerSDK(context, adUnitId)
        adServer.setAdServerEventListener(this)
    }

    /**
     * OpenWrap SDK passes its bids through this method. You should request an ad from your ad server here.
     * @param bid POBBid for targetting parameter
     */
    override fun requestAd(bid: POBBid?) {
        Log.d(TAG, bid.toString())
        // If bid is valid, add bid related custom targeting on the ad request
        adServer.setCustomTargetting(bid?.targetingInfo.toString())
        // Load ad from the Ad server
        adServer.loadInterstitialAd()
    }

    /**
     * Setter method
     * @param listener reference of POBInterstitialEventListener
     */
    override fun setEventListener(listener: POBInterstitialEventListener) {
        eventListener = listener
    }

    /**
     * A dummy custom event triggered based on targeting information sent in the request.
     * This sample uses this event to determine if the partner ad should be served.
     * @param event event value
     */
    override fun onCustomEventReceived(event: String) {
        // Identify if the ad from OpenWrap partner is to be served and, if so, call 'OpenWrapPartnerDidWin'
        if(event.equals("SomeCustomEvent")){
            eventListener?.onOpenWrapPartnerWin()
        }
    }

    /**
     * Called when the interstitial ad is loaded from ad server.
     */
    override fun onInterstitialReceived() {
        eventListener?.onAdServerWin()
    }

    /**
     * Tells the listener that an ad request failed. The failure is normally due to
     * network connectivity or ad availability (i.e., no fill).
     * @param dummyError value of DummyError
     */
    override fun onAdFailed(dummyError: DummyAdServerSDK.DummyError) {
        val error = POBError(dummyError.errorCode, dummyError.errorMsg)
        eventListener?.onFailed(error)
    }

    override fun getRenderer(p0: String?): POBInterstitialRendering? {
        return null
    }

    /**
     * Called when the Interstitial ad is about to show.
     */
    override fun show() {
        adServer.showInterstitialAd()
    }

    /**
     * Similarly you can implement all the other ad flow events
     * Clean up method
     */
    override fun destroy() {
        adServer.destroy()
        eventListener = null
    }

    companion object {
        private val TAG = "CustomInterstitialEventHandler"
    }

}
