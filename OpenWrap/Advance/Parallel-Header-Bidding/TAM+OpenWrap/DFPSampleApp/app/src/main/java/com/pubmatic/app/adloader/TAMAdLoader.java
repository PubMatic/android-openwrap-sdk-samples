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

package com.pubmatic.app.adloader;

import android.util.Log;

import com.amazon.device.ads.AdError;
import com.amazon.device.ads.DTBAdCallback;
import com.amazon.device.ads.DTBAdRequest;
import com.amazon.device.ads.DTBAdResponse;
import com.amazon.device.ads.DTBAdSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Bidder class to load bids from A9 TAM SDK.
 */
public class TAMAdLoader implements Bidding {

    private static final String TAG = "TAMAdLoader";

    /** Ad Size */
    @NonNull
    private DTBAdSize adSize;

    /** Instance of bidding manager */
    @Nullable
    private BiddingListener listener;

    /**
     * Constructor.
     *
     * @param adSize    Ad size
     */
    public TAMAdLoader(@NonNull DTBAdSize adSize) {
        this.adSize = adSize;
    }

    //region Bidding
    /**
     * Method to get the instance of class managing bids from various partners.
     * This instance can be used to notify various events to bidding manager
     * e.g bids received, bids failed etc.
     *
     * @param listener Instance of a class implementing
     *                 BiddingListener interface
     */
    @Override
    public void setBiddingListener(@NonNull BiddingListener listener) {
        this.listener = listener;
    }

    /**
     * Method to instruct bidder class to load the bid.
     */
    @Override
    public void loadBid() {
        final DTBAdRequest loader = new DTBAdRequest();
        loader.setSizes(adSize);

        Log.d(TAG, "Loading ad from A9 TAM SDK");

        // Send ad request to Amazon
        loader.loadAd(new DTBAdCallback() {
            @Override
            public void onFailure(@NonNull AdError adError) {
                Log.e(TAG, "Failed on getting ad from A9 TAM SDK: " + adError.getMessage());
                // Notify failure to bidding manager
                if (listener != null) {
                    listener.onResponseFailed(TAMAdLoader.this, adError);
                }
            }

            @Override
            public void onSuccess(@NonNull DTBAdResponse dtbAdResponse) {
                Log.d(TAG, "Successfully received ad from A9 TAM SDK");

                // Pass A9 TAM custom targeting parameters to bidding manager.
                if (listener != null) {
                    Map<String, List<String>> a9TargetingParams = dtbAdResponse.getDefaultDisplayAdsRequestCustomParams();
                    Map<String, Map<String, List<String>>> partnerTargeting = new HashMap<>();
                    partnerTargeting.put("A9 TAM", a9TargetingParams);
                    listener.onResponseReceived(TAMAdLoader.this, partnerTargeting);
                }
            }
        });
    }
    //endregion
}
