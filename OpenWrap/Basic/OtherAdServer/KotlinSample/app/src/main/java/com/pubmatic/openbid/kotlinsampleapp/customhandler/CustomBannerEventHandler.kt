package com.pubmatic.openwrap.kotlinsampleapp.customhandler

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import com.pubmatic.openwrap.kotlinsampleapp.dummyadserver.DummyAdServerSDK
import com.pubmatic.sdk.common.POBAdSize
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.ui.POBBannerRendering
import com.pubmatic.sdk.openwrap.banner.POBBannerEvent
import com.pubmatic.sdk.openwrap.banner.POBBannerEventListener
import com.pubmatic.sdk.openwrap.core.POBBid

/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class is responsible for communication between OpenWrap banner view and banner view from your ad server SDK(in this case DummyAdServerSDK).
 * It implements the POBBannerEvent protocol. it notifies event back to OpenWrap SDK using POBBannerEventListener methods
 */
@SuppressLint("LongLogTag")
class CustomBannerEventHandler
(val context: Context, adUnitId: String, val adsize: POBAdSize) : POBBannerEvent,  DummyAdServerSDK.DummyAdServerEventListener(){

    private var eventListener: POBBannerEventListener? = null
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
        // If bid is valid, add bid related custom targeting on the ad request
        Log.d(TAG, bid.toString())
        adServer.setCustomTargetting(bid?.targetingInfo.toString())

        // Load ad from the Ad server
        adServer.loadBannerAd()
    }

    override fun setEventListener(listener: POBBannerEventListener) {
        eventListener = listener
    }

    override fun getRenderer(partnerName: String): POBBannerRendering? {
        return null
    }

    /**
     * @return the content size of the ad received from the ad server
     */
    override fun getAdSize(): POBAdSize {
        return adsize
    }

    /**
     * @return requested ad sizes for the bid request
     */
    override fun requestedAdSizes(): Array<POBAdSize?>? {
        val pobAdSizes = arrayOfNulls<POBAdSize>(1)
        pobAdSizes[0] = adsize
        return pobAdSizes
    }

    /**
     * A dummy custom event triggered based on targeting information sent in the request.
     * This sample uses this event to determine if the partner ad should be served.
     * @param event string value
     */
    override fun onCustomEventReceived(event: String) {
        if(event.equals("SomeCustomEvent") == true){
            eventListener?.onOpenWrapPartnerWin()
        }
    }

    /**
     * Called when the banner ad is loaded from ad server.
     * @param banner View class
     */
    override fun onBannerLoaded(view: View) {
        // Identify if the ad from OpenWrap partner is to be served and, if so, call ‘OpenWrapPartnerDidWin’
        eventListener?.onAdServerWin(view)
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

    /**
     * Similarly you can implement all the other ad flow events
     * Method to do clean up
     */
    override fun destroy() {
        adServer.destroy()
        eventListener = null
    }

    companion object {
        private val TAG = "CustomBannerEventHandler"
    }

}
