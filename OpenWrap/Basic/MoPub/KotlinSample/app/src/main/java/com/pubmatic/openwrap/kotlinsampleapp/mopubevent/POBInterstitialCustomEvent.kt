package com.pubmatic.openwrap.kotlinsampleapp.mopubevent

import android.content.Context
import com.mopub.mobileads.CustomEventInterstitial
import com.mopub.mobileads.MoPubErrorCode
import com.pubmatic.sdk.common.log.PMLog
import com.pubmatic.sdk.openwrap.core.POBBid

/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class implements the CustomEventInterstitial and should be configured on MoPub's line item.
 * It implements the PubMatic's Wrapper renderer interface to display PubMatic Ads.
 */
open class POBInterstitialCustomEvent : CustomEventInterstitial() {

    private val TAG = "POBBannerCustomEvent"

    override fun loadInterstitial(context: Context, customEventInterstitialListener: CustomEventInterstitialListener, localExtras: Map<String, Any>?, serverExtras: Map<String, String>) {
        PMLog.info(TAG, "loadInterstitial")
        if (localExtras != null) {
            if(localExtras.containsKey(BID_KEY)){
                val bid = (localExtras.get(BID_KEY) as? POBBid)
                bid?.let {
                    // PubMatic bid has won as mopub SDK calls custom event class. Hence setting bid status as won
                    it.setHasWon(true)
                    // PubMatic sdk should internally create a new ad view and render winning bid.
                    customEventInterstitialListener.onInterstitialLoaded()
                    return
                }
            }
        }
        customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL)
    }

    override fun showInterstitial() {
        //No action required
    }

    override fun onInvalidate() {
        //No action required
    }

    companion object {
        internal val BID_KEY = "pubmatic_bid"
    }
}