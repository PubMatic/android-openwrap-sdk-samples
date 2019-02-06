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

package com.pubmatic.openbid.app.dfpevent;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.openbid.core.POBBid;
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEvent;
import com.pubmatic.sdk.openbid.interstitial.POBInterstitialEventListener;
import com.pubmatic.sdk.webrendering.ui.POBInterstitialRendering;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class implements the communication between the OpenBid SDK and the DFP SDK for a given ad
 * unit. It implements the PubMatic's OpenBid interface. POB SDK notifies (using OpenBid interface)
 * to make a request to DFP SDK and pass the targeting parameters. This class also creates the DFP's
 * PublisherInterstitialAd, initialize it and listen for the callback methods. And pass the DFP ad
 * event to OpenBid SDK via POBInterstitialEventListener.
 */
public class DFPInterstitialEventHandler extends AdListener implements POBInterstitialEvent, AppEventListener {

    private static final String TAG = "DFPInstlEventHandler";

    /**
     * For every winning bid, a DFP SDK gives callback with below key via AppEventListener (from
     * DFP SDK). This key can be changed at DFP's line item.
     */
    private static final String PUBMATIC_WIN_KEY = "pubmaticdm";

    /**
     * Flag to identify if PubMatic bid wins the current impression
     */
    private Boolean notifiedBidWin;

    private boolean isAppEventExpected;
    /**
     * Timer object to synchronize the onAppEvent() of DFP SDK with onAdLoaded()
     */
    private Timer timer;

    /**
     *
     */
    private Context context;

    /**
     *
     */
    private String adUnitId;
    /**
     * DFP Interstitial ad
     */
    private PublisherInterstitialAd dfpAdView;

    /**
     * Interface to pass the DFP ad event to OpenBid SDK
     */
    private POBInterstitialEventListener eventListener;

    /**
     * Constructor
     *
     * @param context  Activity context
     * @param adUnitId SDP ad unit ID
     */
    public DFPInterstitialEventHandler(Context context, String adUnitId) {
        this.context = context;
        this.adUnitId = adUnitId;

    }

    private void initializeDFView() {
        if (dfpAdView != null) {
            dfpAdView = null;
        }
        dfpAdView = new PublisherInterstitialAd(context.getApplicationContext());
        dfpAdView.setAdUnitId(adUnitId);
        dfpAdView.setAdListener(this);
        dfpAdView.setAppEventListener(this);
    }

    private void resetDelay() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    private void scheduleDelay() {
        resetDelay();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                notifyPOBAboutAdReceived();
            }
        };
        timer = new Timer();
        timer.schedule(task, 400);

    }

    private void notifyPOBAboutAdReceived() {
        // If onAppEvent is not called within 400 milli-sec, consider that DFP wins
        if (notifiedBidWin == null) {
            // Notify POB SDK about DFP ad win state and set the state
            notifiedBidWin = false;
            if (eventListener != null) {
                eventListener.onAdServerWin();
            }
        }
    }

    private void sendErrorToPOB(POBError error) {
        if (eventListener != null && error != null) {
            eventListener.onFailed(error);
        }
    }

    // Overridden methods from POBInterstitialEvent
    @Override
    public void requestAd(POBBid bid) {
        // Reset the flag
        isAppEventExpected = false;

        PublisherAdRequest.Builder requestBuilder = new PublisherAdRequest.Builder();

        if (null != bid) {

            // Logging details of bid objects for debug purpose.
            Log.d(TAG, bid.toString());

            Map<String, String> targeting = bid.getTargetingInfo();
            if (targeting != null && !targeting.isEmpty()) {
                // using for-each loop for iteration over Map.entrySet()
                for (Map.Entry<String, String> entry : targeting.entrySet()) {
                    requestBuilder.addCustomTargeting(entry.getKey(), entry.getValue());
                    Log.d(TAG, "DFP custom param [" + entry.getKey() + "] = " + entry.getValue());
                }
            }

            // Save this flag for future reference. It will be referred to wait for onAppEvent, only
            // if POB delivers non-zero bid to DFP SDK.
            double price = bid.getPrice();
            if (price > 0.0d) {
                isAppEventExpected = true;
            }
        }

        final PublisherAdRequest adRequest = requestBuilder.build();

        // Publisher/App developer can add extra targeting parameters to dfpAdView here.
        notifiedBidWin = null;

        initializeDFView();
        // Load DFP ad request
        dfpAdView.loadAd(adRequest);
    }

    @Override
    public void setEventListener(POBInterstitialEventListener listener) {
        eventListener = listener;
    }

    @Override
    public POBInterstitialRendering getRenderer(String partnerName) {
        return null;
    }

    @Override
    public void destroy() {
        //Do Final cleaup
        resetDelay();
        dfpAdView = null;
        eventListener = null;
        context = null;
    }

    @Override
    public void show() {
        if (null != dfpAdView && dfpAdView.isLoaded()) {
            dfpAdView.show();
        } else {
            Log.e(TAG, "DFP SDK is not ready to show Interstitial Ad.");
            if (null != eventListener) {
                sendErrorToPOB(new POBError(POBError.INTERSTITIAL_NOT_READY, "DFP SDK is not ready to show Interstitial Ad."));
            }
        }
    }

    // Overridden Method from DFP App Event listener
    @Override
    public void onAppEvent(String key, String s1) {
        Log.d(TAG, hashCode() + " onAppEvent() key=" + key);

        if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {
            // If onAppEvent is called before onAdLoaded(), it means POB bid wins
            if (notifiedBidWin == null) {
                notifiedBidWin = true;
                eventListener.onOpenBidPartnerWin();
            } else if (!notifiedBidWin) {
                // In this case onAppEvent is called in wrong order and within 400 milli-sec
                // Hence, notify POB SDK about DFP ad win state
                sendErrorToPOB(new POBError(POBError.OPEN_BID_SIGNALING_ERROR,
                        "DFP ad server mismatched bid win signal"));
            }
        }
    }

    // Override Methods from DFP Ad view's AdListener
    @Override
    public void onAdFailedToLoad(int errCode) {
        Log.d(TAG, "onAdFailedToLoad()");

        if (eventListener != null) {
            switch (errCode) {
                case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                    eventListener.onFailed(new POBError(POBError.INVALID_REQUEST, "DFP SDK gives invalid request error"));
                    break;
                case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                    eventListener.onFailed(new POBError(POBError.NETWORK_ERROR, "DFP SDK gives network error"));
                    break;
                case PublisherAdRequest.ERROR_CODE_NO_FILL:
                    eventListener.onFailed(new POBError(POBError.NO_ADS_AVAILABLE, "DFP SDK gives no fill error"));
                    break;
                default:
                    eventListener.onFailed(new POBError(POBError.INTERNAL_ERROR, "DFP SDK gives internal error"));
                    break;
            }
        }
    }

    @Override
    public void onAdOpened() {
        if (eventListener != null) {
            eventListener.onAdOpened();
        }
    }

    @Override
    public void onAdClosed() {
        if (eventListener != null) {
            eventListener.onAdClosed();
        }
    }

    @Override
    public void onAdLoaded() {
        Log.d(TAG, "onAdLoaded()");
        if (eventListener != null) {

            // Wait only if onAppEvent() is not already called.
            if (notifiedBidWin == null) {

                // Check if POB bid delivers non-zero bids to DFP, then only wait
                if (isAppEventExpected) {
                    // Wait for 400 milli-sec to get onAppEvent before conveying to POB SDK
                    scheduleDelay();
                } else {
                    notifyPOBAboutAdReceived();
                }
            }
        }
    }

    @Override
    public void onAdLeftApplication() {
        super.onAdLeftApplication();
        if (eventListener != null) {
            eventListener.onAdLeftApplication();
        }
    }
}
