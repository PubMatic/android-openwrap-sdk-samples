/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2023 PubMatic, All Rights Reserved.
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
package com.pubmatic.openwrap.listapp.adloader

import android.util.Log
import com.amazon.device.ads.*
import java.util.*

/**
 * Bidder class to load bids from A9 TAM SDK.
 */
class TAMAdLoader(val adSize: DTBAdSize) : Bidding {

    /** Instance of bidding manager  */
    private var listener: BiddingListener? = null
    //region Bidding
    /**
     * Method to get the instance of class managing bids from various partners.
     * This instance can be used to notify various events to bidding manager
     * e.g bids received, bids failed etc.
     *
     * @param listener Instance of a class implementing
     * BiddingListener interface
     */
    override fun setBiddingListener(listener: BiddingListener?) {
        this.listener = listener
    }

    /**
     * Method to instruct bidder class to load the bid.
     */
    override fun loadBid() {
        val loader = DTBAdRequest()
        loader.setSizes(adSize)
        Log.d(TAG, "Loading ad from A9 TAM SDK")

        // Send ad request to Amazon
        loader.loadAd(object : DTBAdCallback {
            override fun onFailure(adError: AdError) {
                Log.e(TAG, "Failed on getting ad from A9 TAM SDK: " + adError.getMessage())
                // Notify failure to bidding manager
                listener?.onResponseFailed(this@TAMAdLoader, adError)
            }

            override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                Log.d(TAG, "Successfully received ad from A9 TAM SDK")
                // Pass A9 TAM custom targeting parameters to bidding manager.
                listener?.let {
                    val a9TargetingParams: Map<String?, List<String?>?> =
                        dtbAdResponse.getDefaultDisplayAdsRequestCustomParams()
                    val partnerTargeting: MutableMap<String?, Map<String?, List<String?>?>?> =
                        HashMap()
                    partnerTargeting["A9 TAM"] = a9TargetingParams
                    it.onResponseReceived(this@TAMAdLoader, partnerTargeting)
                }
            }
        })
    } //endregion

    companion object {
        private const val TAG = "TAMAdLoader"
    }
}