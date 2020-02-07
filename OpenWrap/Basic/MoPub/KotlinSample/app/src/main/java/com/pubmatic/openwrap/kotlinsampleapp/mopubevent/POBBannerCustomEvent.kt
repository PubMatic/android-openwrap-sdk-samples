package com.pubmatic.openwrap.kotlinsampleapp.mopubevent

import android.content.Context
import android.view.View
import com.mopub.mobileads.CustomEventBanner
import com.mopub.mobileads.MoPubErrorCode
import com.pubmatic.sdk.common.log.PMLog
import com.pubmatic.sdk.openwrap.core.POBBid


/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class implements the CustomEventBanner and should be configured on MoPub's line item.
 * It implements the PubMatic's Wrapper renderer interface to display PubMatic Ads.
 */
class POBBannerCustomEvent : CustomEventBanner() {

    private val TAG = "POBBannerCustomEvent"

    public override fun loadBanner(context: Context, customEventBannerListener: CustomEventBannerListener, localExtras: Map<String, Any>, serverExtras: Map<String, String>) {
        PMLog.info(TAG, "loadBanner")
        // Get PubMatic bid form local extra passed from event handler
        val bid : POBBid? = localExtras[BID_KEY] as? POBBid
        bid?.let {
            // PubMatic bid has won as mopub SDK calls custom event class. Hence setting bid status as won
            it.setHasWon(true)
            // Passing dummy ad view to notify banner ad loaded to mopub sdk. This view will not be used to render ad.
            val view = View(context)
            // PubMatic sdk should internally create a new ad view and render winning bid.
            customEventBannerListener.onBannerLoaded(view)
            return
        }
        customEventBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL)


    }

    public override fun onInvalidate() {
        // No action required
    }

    companion object {
        internal val BID_KEY = "pubmatic_bid"
    }


}

