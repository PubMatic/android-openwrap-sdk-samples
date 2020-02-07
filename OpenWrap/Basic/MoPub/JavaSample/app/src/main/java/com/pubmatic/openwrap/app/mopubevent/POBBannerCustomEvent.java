/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2020 PubMatic, All Rights Reserved.
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

package com.pubmatic.openwrap.app.mopubevent;

import android.content.Context;
import android.view.View;

import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;
import com.pubmatic.sdk.common.log.PMLog;
import com.pubmatic.sdk.openwrap.core.POBBid;

import java.util.Map;

/**
 * This class is compatible with OpenWrap SDK v1.5.0.
 * This class implements the CustomEventBanner and should be configured on MoPub's line item.
 * It implements the PubMatic's Wrapper renderer interface to display PubMatic Ads.
 */
public class POBBannerCustomEvent extends CustomEventBanner {

    private static final String TAG = "POBBannerCustomEvent";
    static final String BID_KEY = "pubmatic_bid";

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        PMLog.info(TAG, "loadBanner");
        // Get PubMatic bid form local extra passed from event handler
        POBBid bid = (POBBid) localExtras.get(BID_KEY);
        if (bid != null) {
            // PubMatic bid has won as mopub SDK calls custom event class. Hence setting bid status as won
            bid.setHasWon(true);
            // Passing dummy ad view to notify banner ad loaded to mopub sdk. This view will not be used to render ad.
            View view = new View(context);
            // PubMatic sdk should internally create a new ad view and render winning bid.
            customEventBannerListener.onBannerLoaded(view);
        }else{
            // Notifing ad failure if no bid are received from pubmatic ad server
            customEventBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
        }
    }

    @Override
    protected void onInvalidate() {
        //No action required
    }

}

