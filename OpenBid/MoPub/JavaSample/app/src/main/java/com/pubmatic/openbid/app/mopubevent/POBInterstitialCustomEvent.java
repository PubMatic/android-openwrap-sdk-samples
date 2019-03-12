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

package com.pubmatic.openbid.app.mopubevent;

import android.content.Context;
import android.support.annotation.Nullable;

import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.base.POBAdDescriptor;
import com.pubmatic.sdk.common.utility.POBUtils;
import com.pubmatic.sdk.openbid.core.POBRenderer;
import com.pubmatic.sdk.openbid.core.POBBid;
import com.pubmatic.sdk.openbid.interstitial.POBInterstitial;
import com.pubmatic.sdk.webrendering.ui.POBInterstitialRendererListener;
import com.pubmatic.sdk.webrendering.ui.POBInterstitialRendering;

import java.util.Map;

/**
 * This class implements the CustomEventInterstitial and should be configured on MoPub's line item.
 * It implements the PubMatic's Wrapper renderer interface to display PubMatic Ads.
 */
public class POBInterstitialCustomEvent extends CustomEventInterstitial {

    /**
     * Context on which PubMatic interstitial Ad will get displayed.
     */
    private Context context;

    /**
     * Listener to notify events on Ad to MoPub SDK.
     */
    private CustomEventInterstitialListener mopubCustomEventInterstitial;

    /**
     * Wrapper renderer to display PubMatic Ad.
     */
    private POBInterstitialRendering renderer;

    private int orientation;

    //<editor-fold desc="CustomEventInterstitial overridden methods">
    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        this.context = context;
        this.mopubCustomEventInterstitial = customEventInterstitialListener;
        if (localExtras != null) {
            orientation = getOrientation(localExtras);
            if(localExtras.containsKey(MoPubInterstitialEventHandler.PUBMATIC_BID_KEY)){
                POBBid bid = (POBBid) localExtras.get(MoPubInterstitialEventHandler.PUBMATIC_BID_KEY);
                renderer = POBRenderer.getInterstitialRenderer(this.context);
                renderer.setAdRendererListener(new WrapperRendererListener());
                renderer.renderAd(bid);
            }
        }else {
            this.mopubCustomEventInterstitial.onInterstitialFailed(MoPubErrorCode.NO_FILL);
        }
    }

    @Override
    protected void showInterstitial() {
        if (renderer != null) {
            renderer.show(orientation);
        }
    }

    private int getOrientation(Map<String, Object> customData){
        if(null != customData && customData.containsKey(POBInterstitial.ORIENTATION_KEY)){
            return (int)customData.get(POBInterstitial.ORIENTATION_KEY);
        }else {
            return POBUtils.getDeviceOrientation(context);
        }
    }

    @Override
    protected void onInvalidate() {
        this.context = null;
        this.mopubCustomEventInterstitial = null;
        this.renderer = null;
    }
    //</editor-fold>

    //<editor-fold desc="POBInterstitialRendererListener overridden methods">
    private class WrapperRendererListener implements POBInterstitialRendererListener {

        @Override
        public void onAdRender(POBAdDescriptor descriptor) {
            if(null != mopubCustomEventInterstitial){
                mopubCustomEventInterstitial.onInterstitialLoaded();
            }
        }

        @Override
        public void onAdRenderingFailed(POBError error) {
            if(mopubCustomEventInterstitial != null) {
                MoPubErrorCode moPubErrorCode;
                int errorCode = error.getErrorCode();
                switch (errorCode) {
                    case POBError.NO_ADS_AVAILABLE:
                        moPubErrorCode = MoPubErrorCode.NETWORK_NO_FILL;
                        break;
                    case POBError.NETWORK_ERROR:
                        moPubErrorCode = MoPubErrorCode.NO_CONNECTION;
                        break;
                    case POBError.SERVER_ERROR:
                        moPubErrorCode = MoPubErrorCode.SERVER_ERROR;
                        break;
                    case POBError.TIMEOUT_ERROR:
                        moPubErrorCode = MoPubErrorCode.NETWORK_TIMEOUT;
                        break;
                    case POBError.INTERNAL_ERROR:
                        moPubErrorCode = MoPubErrorCode.INTERNAL_ERROR;
                        break;
                    case POBError.REQUEST_CANCELLED:
                        moPubErrorCode = MoPubErrorCode.CANCELLED;
                        break;
                    default:
                        moPubErrorCode = MoPubErrorCode.UNSPECIFIED;
                        break;
                }
                mopubCustomEventInterstitial.onInterstitialFailed(moPubErrorCode);
            }
        }

        @Override
        public void onAdClicked(@Nullable String url) {
            if (mopubCustomEventInterstitial != null){
                mopubCustomEventInterstitial.onInterstitialClicked();
            }
        }

        @Override
        public void onAdInteractionStarted() {
            if (mopubCustomEventInterstitial != null){
                mopubCustomEventInterstitial.onInterstitialShown();
            }
        }

        @Override
        public void onAdInteractionStopped() {
            if(mopubCustomEventInterstitial != null) {
                mopubCustomEventInterstitial.onInterstitialDismissed();
            }
        }

        @Override
        public void onAdUnload() {
            //No Actions required
        }

        @Override
        public void onLeavingApplication() {
            // No Actions required
        }
    }
    //</editor-fold>
}
