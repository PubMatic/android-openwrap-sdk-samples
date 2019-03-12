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
