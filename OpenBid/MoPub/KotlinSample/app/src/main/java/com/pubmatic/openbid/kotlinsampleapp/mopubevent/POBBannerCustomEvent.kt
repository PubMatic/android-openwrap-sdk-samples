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
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.mopub.mobileads.CustomEventBanner
import com.mopub.mobileads.MoPubErrorCode
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.base.POBAdDescriptor
import com.pubmatic.sdk.common.base.POBAdRendererListener
import com.pubmatic.sdk.common.utility.POBUtils
import com.pubmatic.sdk.openbid.core.POBRenderer
import com.pubmatic.sdk.openbid.core.POBBid
import com.pubmatic.sdk.webrendering.mraid.POBWebRenderer

class POBBannerCustomEvent : CustomEventBanner() {

    private val TAG = "POBBannerCustomEvent"
    private var customEventBannerListener: CustomEventBannerListener? = null
    private var bid: POBBid? = null

    protected override fun loadBanner(context: Context, customEventBannerListener: CustomEventBannerListener, localExtras: Map<String, Any>, serverExtras: Map<String, String>) {
        this.customEventBannerListener = customEventBannerListener
        Log.d(TAG, "loadBanner")
        bid = localExtras[BID_KEY] as POBBid?
        if (null != bid) {
            val renderer = POBRenderer.getBannerRenderer(context)
            (renderer as POBWebRenderer).setRefreshTimeoutInSec(bid!!.refreshInterval)
            renderer.setAdRendererListener(AdRendererListenerImp())
            renderer.renderAd(bid)
        } else {
            handlerFailure(POBError(POBError.NO_ADS_AVAILABLE, "Pubmatic Ads not available!"))
        }
    }


    protected override fun onInvalidate() {
        customEventBannerListener = null
        bid = null
    }

    private fun handlerFailure(error: POBError) {
        if (null != customEventBannerListener) {
            val moPubErrorCode: MoPubErrorCode
            when (error.errorCode) {
                POBError.NO_ADS_AVAILABLE -> moPubErrorCode = MoPubErrorCode.NETWORK_NO_FILL
                POBError.NETWORK_ERROR -> moPubErrorCode = MoPubErrorCode.NO_CONNECTION
                POBError.SERVER_ERROR -> moPubErrorCode = MoPubErrorCode.SERVER_ERROR
                POBError.TIMEOUT_ERROR -> moPubErrorCode = MoPubErrorCode.NETWORK_TIMEOUT
                POBError.INTERNAL_ERROR -> moPubErrorCode = MoPubErrorCode.INTERNAL_ERROR
                POBError.REQUEST_CANCELLED -> moPubErrorCode = MoPubErrorCode.CANCELLED
                POBError.INVALID_REQUEST -> moPubErrorCode = MoPubErrorCode.NETWORK_INVALID_STATE
                else -> moPubErrorCode = MoPubErrorCode.UNSPECIFIED
            }
            customEventBannerListener?.onBannerFailed(moPubErrorCode)
        } else {
            Log.e(TAG, "Can not call onAdRenderingFailed, CustomEventBannerListener reference null.")
        }
    }


    internal inner class AdRendererListenerImp : POBAdRendererListener {

        override fun onAdRender(view: View, descriptor: POBAdDescriptor) {
            if (null != customEventBannerListener) {
                val parent = FrameLayout(view.context)
                val params = FrameLayout.LayoutParams(POBUtils.convertDpToPixel(bid!!.width),
                        POBUtils.convertDpToPixel(bid!!.height))
                parent.addView(view, params)
                customEventBannerListener?.onBannerLoaded(parent)
            } else {
                Log.e(TAG, "Can not call onBannerLoaded, CustomEventBannerListener reference null.")
            }
        }

        override fun onAdRenderingFailed(error: POBError) {
            handlerFailure(error)
        }

        override fun onRenderAdClick() {
            if (null != customEventBannerListener) {
                customEventBannerListener?.onBannerClicked()
            } else {
                Log.e(TAG, "Can not call onBannerClicked, CustomEventBannerListener reference null.")
            }
        }

        override fun onAdInteractionStarted() {
            if (null != customEventBannerListener) {
                customEventBannerListener?.onBannerExpanded()
            } else {
                Log.e(TAG, "Can not call onBannerExpanded, CustomEventBannerListener reference null.")
            }

        }

        override fun onAdInteractionStopped() {
            if (null != customEventBannerListener) {
                customEventBannerListener?.onBannerCollapsed()
            } else {
                Log.e(TAG, "Can not call onBannerCollapsed, CustomEventBannerListener reference null.")
            }

        }

        override fun onMRAIDAdClick() {
            if (null != customEventBannerListener) {
                customEventBannerListener?.onBannerClicked()
            } else {
                Log.e(TAG, "Can not call onBannerClicked, CustomEventBannerListener reference null.")
            }

        }

        override fun onAdUnload() {
            //No action required
        }

        override fun onLeavingApplication() {
            // No action required
        }


    }

    companion object {
        internal val BID_KEY = "pubmatic_bid"
    }


}

