/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2020 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access or to such other persons whom are directly authorized by PubMatic to access the source code and are subject to confidentiality and nondisclosure obligations with respect to the source code.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PUBMATIC IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package sdk.pubmatic.com.javasample.customhandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.pubmatic.sdk.common.POBAdSize;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.ui.POBBannerRendering;
import com.pubmatic.sdk.openwrap.banner.POBBannerEvent;
import com.pubmatic.sdk.openwrap.banner.POBBannerEventListener;
import com.pubmatic.sdk.openwrap.core.POBBid;

import sdk.pubmatic.com.javasample.dummyadserver.DummyAdServerSDK;

/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class is responsible for communication between OpenWrap banner view and banner view from your ad server SDK(in this case DummyAdServerSDK).
 * It implements the POBBannerEvent protocol. it notifies event back to OpenWrap SDK using POBBannerEventListener methods
 */
@SuppressLint("LongLogTag")
public class CustomBannerEventHandler extends DummyAdServerSDK.DummyAdServerEventListener implements POBBannerEvent {

    private static final String TAG = "CustomBannerEventHandler";

    private POBBannerEventListener eventListener;

    private DummyAdServerSDK adServerSDK;

    private POBAdSize adSize;


    /**
     * Constructor
     *
     * @param context  android context
     * @param adUnitId ad server ad unit ID
     * @param adSize ad size for custom ad server
     */
    public CustomBannerEventHandler(Context context, String adUnitId, POBAdSize adSize) {
        adServerSDK = new DummyAdServerSDK(context, adUnitId);
        adServerSDK.setAdServerEventListener(this);
        this.adSize = adSize;
    }

    /**
     * OpenWrap SDK passes its bids through this method. You should request an ad from your ad server here.
     *
     * @param bid POBBid for targetting parameter
     */
    @Override
    public void requestAd(POBBid bid) {
        // If bid is valid, add bid related custom targeting on the ad request
        if (null != bid) {
            Log.d(TAG, bid.toString());
            adServerSDK.setCustomTargetting(bid.getTargetingInfo().toString());
        }
        // Load ad from the Ad server
        adServerSDK.loadBannerAd();
    }

    /**
     * Setter method
     *
     * @param listener reference of POBBannerEventListener
     */
    @Override
    public void setEventListener(POBBannerEventListener listener) {
        eventListener = listener;
    }

    @Override
    public POBBannerRendering getRenderer(String partnerName) {
        return null;
    }

    /**
     * @return the content size of the ad received from the ad server
     */
    @Override
    public POBAdSize getAdSize() {
            return adSize;
    }

    /**
     * @return requested ad sizes for the bid request
     */
    @Override
    public POBAdSize[] requestedAdSizes() {
        return new POBAdSize[]{adSize};
    }

    /**
     * A dummy custom event triggered based on targeting information sent in the request.
     * This sample uses this event to determine if the partner ad should be served.
     *
     * @param event string value
     */
    @Override
    public void onCustomEventReceived(String event) {
        // Identify if the ad from OpenWrap partner is to be served and, if so, call 'openBidPartnerDidWin'
        if ("SomeCustomEvent".equals(event) && null != eventListener) {
            eventListener.onOpenWrapPartnerWin();
        }
    }

    /**
     * Called when the banner ad is loaded from ad server.
     *
     * @param banner View class
     */
    @Override
    public void onBannerLoaded(View banner) {
        if (null != eventListener) {
            eventListener.onAdServerWin(banner);
        }
    }


    /**
     * Tells the listener that an ad request failed. The failure is normally due to
     * network connectivity or ad availability (i.e., no fill).
     *
     * @param dummyError value of DummyError
     */
    @Override
    public void onAdFailed(DummyAdServerSDK.DummyError dummyError) {
        if (null != eventListener) {
            eventListener.onFailed(new POBError(dummyError.getErrorCode(), dummyError.getErrorMsg()));
        }
    }

    /**
     * Similarly you can implement all the other ad flow events
     * Method to do clean up
     */
    @Override
    public void destroy() {
        adServerSDK.destroy();
        eventListener = null;
    }

}
