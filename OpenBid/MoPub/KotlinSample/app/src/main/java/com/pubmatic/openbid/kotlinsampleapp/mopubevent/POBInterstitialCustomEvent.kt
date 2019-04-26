package com.pubmatic.openbid.kotlinsampleapp.mopubevent

import android.content.Context
import com.mopub.mobileads.CustomEventInterstitial
import com.mopub.mobileads.MoPubErrorCode
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.base.POBAdDescriptor
import com.pubmatic.sdk.common.utility.POBUtils
import com.pubmatic.sdk.openbid.core.POBRenderer
import com.pubmatic.sdk.openbid.core.POBBid
import com.pubmatic.sdk.openbid.interstitial.POBInterstitial
import com.pubmatic.sdk.webrendering.ui.POBInterstitialRendererListener
import com.pubmatic.sdk.webrendering.ui.POBInterstitialRendering

/**
 * This class implements the CustomEventInterstitial and should be configured on MoPub's line item.
 * It implements the PubMatic's Wrapper renderer interface to display PubMatic Ads.
 */
open class POBInterstitialCustomEvent : CustomEventInterstitial() {

    /**
     * Context on which PubMatic interstitial Ad will get displayed.
     */
    private var context: Context? = null

    /**
     * Listener to notify events on Ad to MoPub SDK.
     */
    private var mopubCustomEventInterstitial: CustomEventInterstitialListener? = null

    /**
     * Wrapper renderer to display PubMatic Ad.
     */
    private var renderer: POBInterstitialRendering? = null

    private var orientation: Int? = 0

    //<editor-fold desc="CustomEventInterstitial overridden methods">
    override fun loadInterstitial(context: Context, customEventInterstitialListener: CustomEventInterstitialListener, localExtras: Map<String, Any>?, serverExtras: Map<String, String>) {
        this.context = context
        this.mopubCustomEventInterstitial = customEventInterstitialListener
        if (localExtras != null) {
            orientation = getOrientation(localExtras)
            if(localExtras.containsKey(MoPubInterstitialEventHandler.PUBMATIC_BID_KEY)){
                val bid = (localExtras.get(MoPubInterstitialEventHandler.PUBMATIC_BID_KEY) as? POBBid)
                bid?.let {
                    renderer = POBRenderer.getInterstitialRenderer(this.context)
                    renderer?.setAdRendererListener(WrapperRendererListener())
                    renderer?.renderAd(it)
                }
            }

        } else {
            this.mopubCustomEventInterstitial?.onInterstitialFailed(MoPubErrorCode.NO_FILL)
        }
    }
    private fun getOrientation(customData: Map<String, Any>?): Int {
        return if (null != customData && customData.containsKey(POBInterstitial.ORIENTATION_KEY)) {
            (customData.get(POBInterstitial.ORIENTATION_KEY) as? Int)?:POBUtils.getDeviceOrientation(context)
        } else {
            POBUtils.getDeviceOrientation(context)
        }
    }

    override fun showInterstitial() {
        renderer?.show((orientation) ?: POBUtils.getDeviceOrientation(context))
    }

    override fun onInvalidate() {
        this.context = null
        this.mopubCustomEventInterstitial = null
        this.renderer?.destroy();
        this.renderer = null
    }
    //</editor-fold>

    //<editor-fold desc="POBInterstitialRendererListener overridden methods">
    private inner class WrapperRendererListener : POBInterstitialRendererListener {

        override fun onAdRender(descriptor: POBAdDescriptor) {
            mopubCustomEventInterstitial?.onInterstitialLoaded()
        }

        override fun onAdRenderingFailed(error: POBError) {
            if (mopubCustomEventInterstitial != null) {
                val moPubErrorCode: MoPubErrorCode
                val errorCode = error.errorCode
                moPubErrorCode = when (errorCode) {
                    POBError.NO_ADS_AVAILABLE -> MoPubErrorCode.NETWORK_NO_FILL
                    POBError.NETWORK_ERROR -> MoPubErrorCode.NO_CONNECTION
                    POBError.SERVER_ERROR -> MoPubErrorCode.SERVER_ERROR
                    POBError.TIMEOUT_ERROR -> MoPubErrorCode.NETWORK_TIMEOUT
                    POBError.INTERNAL_ERROR -> MoPubErrorCode.INTERNAL_ERROR
                    POBError.REQUEST_CANCELLED -> MoPubErrorCode.CANCELLED
                    else -> MoPubErrorCode.UNSPECIFIED
                }
                mopubCustomEventInterstitial?.onInterstitialFailed(moPubErrorCode)
            }
        }

        override fun onAdClicked(url: String?) {
            mopubCustomEventInterstitial?.onInterstitialClicked()
        }

        override fun onAdInteractionStarted() {
            mopubCustomEventInterstitial?.onInterstitialShown()
        }

        override fun onAdInteractionStopped() {
            mopubCustomEventInterstitial?.onInterstitialDismissed()
        }

        override fun onAdUnload() {
            //No Actions required
        }

        override fun onLeavingApplication() {
            //No Actions required
        }
    }
    //</editor-fold>
}
