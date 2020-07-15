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
package com.pubmatic.openwrap.kotlinsampleapp.dummyadserver

import android.content.Context
import android.util.Log
import android.view.View

/**
 * Emulates Ad server SDK
 */
class DummyAdServerSDK(val context: Context, val adUnitId: String) {

    private var adServerEventListener: DummyAdServerEventListener? = null

    /**
     * Setter to set Listener
     * @param adServerEventListener reference of DummyAdServerEventListener
     */
    fun setAdServerEventListener(adServerEventListener: DummyAdServerEventListener) {
        this.adServerEventListener = adServerEventListener
    }


    /**
     * Method to set targetting info
     * @param customTargetting targetting value
     */
    fun setCustomTargetting(customTargetting: String) {
        //Sets custom targeting to be sent in the ad call
        Log.d("DummyAdServerSDK","targetting value: "+customTargetting)
    }



    /**
     * loads a banner ad from the ad server
     */
    fun loadBannerAd() {
        if (null == context) {
            adServerEventListener?.onAdFailed(DummyError(-1, "Internal Error: Context should not be null."))
            return
        }
        // Usually, the ad server determines whether the partner bid won in the
        // auction, based on provided targeting information. Then, the ad server SDK
        // will either render the banner ad or indicate that a partner ad should be
        // rendered.
        if(adUnitId.equals("OtherASBannerAdUnit")){
            adServerEventListener?.onCustomEventReceived("SomeCustomEvent")
        }else{
            adServerEventListener?.onBannerLoaded(View(context))
        }
    }

    /**
     * loads an interstitial ad from the ad server
     */
    fun loadInterstitialAd() {
        // Usually, the ad server determines whether the partner bid won in the
        // auction, based on provided targeting information. Then, the ad server SDK
        // will either load the interstitial ad or indicate that a partner ad should
        // be rendered.
        if(adUnitId.equals("OtherASInterstitialAdUnit")){
            adServerEventListener?.onCustomEventReceived("SomeCustomEvent")
        }else{
            adServerEventListener?.onInterstitialReceived()
        }
    }

    /**
     * Presents an interstitial ad
     */
    fun showInterstitialAd() {
        // This implementation of your ad server SDK's interstitial ad presents an
        // interstitial ad
    }

    /**
     * Clean up method
     */
    fun destroy() {
        adServerEventListener = null
    }

    /**
     * Listener to receive ad success/failure events.
     */
    open class DummyAdServerEventListener {

        /**
         * A dummy custom event triggered based on targeting information sent in the request.
         * @param event value
         */
        open fun onCustomEventReceived(event: String){}

        /**
         * called when a banner ad is loaded
         * @param view dummy view
         */
        open fun onBannerLoaded(view: View) {}

        /**
         * called when a interstitial ad is loaded
         */
        open fun onInterstitialReceived() {}
        /**
         * callback method when any failure happens
         * @param dummyError Dummy error
         */
        open fun onAdFailed(dummyError: DummyError) {}
    }

    /**
     * Dummy Error class to handler error
     */
    inner class DummyError internal constructor(val errorCode: Int, val errorMsg: String)
}