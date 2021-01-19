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

package com.pubmatic.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.amazon.device.ads.DTBAdSize;
import com.pubmatic.app.adloader.BiddingManager;
import com.pubmatic.app.adloader.BiddingManagerListener;
import com.pubmatic.app.adloader.TAMAdLoader;
import com.pubmatic.appapp.R;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.core.POBBid;
import com.pubmatic.sdk.openwrap.core.POBBidEvent;
import com.pubmatic.sdk.openwrap.core.POBBidEventListener;
import com.pubmatic.sdk.openwrap.eventhandler.mopub.MoPubInterstitialEventHandler;
import com.pubmatic.sdk.openwrap.interstitial.POBInterstitial;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity to display interstitial ad.
 */
public class MoPubInterstitialActivity extends AppCompatActivity implements POBBidEventListener, BiddingManagerListener {

    private static final String TAG = "MoPubInterstitialActivity";

    // PubMatic Ad tag details
    private static final String OPENWRAP_AD_UNIT_ONE = "2b5c583ad21c4e32ba5e00e70185bfc9";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1302;

    // MoPub Ad unit id
    private static final String MOPUB_AD_UNIT_ID = "0ff3b569de0e49dcb42bd654c326ff1e";

    // A9 TAM slot id
    private static final String SLOT_ID = "4e918ac0-5c68-4fe1-8d26-4e76e8f74831";

    // Request timeout in seconds
    private static final int REQUEST_TIMEOUT = 3;

    // OpenWrap SDK's interstitial view
    private POBInterstitial interstitial;
    // OpenWrap SDK's event handler for MoPub
    private MoPubInterstitialEventHandler eventHandler;

    // Bidding manager to manage bids from various partners. e.g. OpenWrap, A9 TAM
    @Nullable
    private BiddingManager biddingManager;

    // Map to maintain response from different partners
    @Nullable
    private Map<String, Map<String, List<String>>> partnerTargeting;

    //region Activity Creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        // Initialize partner targeting map
        partnerTargeting = new HashMap<>();

        // Load Ad button
        findViewById(R.id.loadAdBtn).setOnClickListener(v -> {
            findViewById(R.id.showAdBtn).setEnabled(false);

            /*
             * Fetch bids, simultaneously, from other partners e.g. A9 TAM (using DTB SDK) & PubMatic OpenWrap
             * partners(using OpenWrap SDK).
             * As per this implementation, both A9 TAM and OpenWrap's bids are added in MoPub custom targeting after they are available
             * before MoPub ad call is initiated by the MoPubInterstitialEventHandler.
             */
            loadBids();
        });

        // Show button
        findViewById(R.id.showAdBtn).setOnClickListener(v -> showInterstitialAd());

        // Create bidding manager
        biddingManager = new BiddingManager();

        // Set listener to bidding manager events.
        biddingManager.setBiddingManagerListener(this);

        // Register bidders to bidding manager
        registerBidders();

        // Create OpenWrap interstitial object
        // MoPub ad call is initiated once the bid response is received from PubMatic.
        createOpenWrapInterstitial();

        // Set config listener on POBInterstitial to pass partner targeting to MoPub
        setConfigListener();
    }
    //endregion

    //region Private Methods
    /**
     * Create instance of PubMatic OpenWrap interstitial ad
     */
    private void createOpenWrapInterstitial(){
        // A valid Play Store Url of an Android application is required.
        POBApplicationInfo appInfo = new POBApplicationInfo();
        try {
            appInfo.setStoreURL(new URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo);

        // Create an interstitial custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each interstitial ad instance.
        // For example, The code below creates an event handler for MoPub ad server.
        eventHandler = new MoPubInterstitialEventHandler(this, MOPUB_AD_UNIT_ID);

        // Create  interstitial instance by passing activity context and
        interstitial = new POBInterstitial(this, PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ONE,
                eventHandler);

        // Set Optional listener
        interstitial.setListener(new POBInterstitialListener());

        // Set listener to get get bid details
        interstitial.setBidEventListener(this);

        // Set request timeout
        interstitial.getAdRequest().setNetworkTimeout(REQUEST_TIMEOUT);
    }

    /**
     * Register bidders to bidding manager
     */
    private void registerBidders() {
        // You can create other bidders here and register to bidding manager.
        // Add A9 TAM bidder
        if (biddingManager != null) {
            TAMAdLoader tamAdLoader = new TAMAdLoader(new DTBAdSize.DTBInterstitialAdSize(SLOT_ID));
            biddingManager.registerBidder(tamAdLoader);
        }
    }

    /**
     * To show interstitial ad call this method
     */
    private void showInterstitialAd() {
        // check if the interstitial is ready
        if (null != interstitial && interstitial.isReady()) {
            // Call show on interstitial
            interstitial.show();
        }
    }

    /**
     * Load bids from all the bidders and OpenWrap SDK
     */
    private void loadBids() {
        // Load bids from all the registered bidders e.g. A9 TAM
        if (biddingManager != null) {
            biddingManager.loadBids();
        }
        // Load bids from OpenWrap
        interstitial.loadAd();
    }

    /**
     * Set config listener on POBInterstitial instance
     */
    @SuppressLint("LongLogTag")
    private void setConfigListener() {
        eventHandler.setConfigListener((moPubInterstitial, pobBid) -> {
            if (partnerTargeting != null && partnerTargeting.size() > 0) {
                StringBuilder keywords = new StringBuilder();

                // Iterate partnerTargeting map for all the key-value pairs
                for (String partnerName : partnerTargeting.keySet()) {
                    // Get key-value targeting for partner with name partnerName
                    Map<String, List<String>> bidderResponse = partnerTargeting.get(partnerName);
                    // Join key value pairs in a string
                    if (bidderResponse != null) {
                        for (String key : bidderResponse.keySet()) {
                            List<String> values = bidderResponse.get(key);
                            if (values != null && values.size() > 0) {
                                String keyValuePair = key + ":" + values.get(0);

                                if (keywords.length() != 0) {
                                    keywords.append(",").append(keyValuePair);
                                } else {
                                    keywords = new StringBuilder(keyValuePair);
                                }
                            }
                        }
                    }
                }
                Log.d(TAG, "MoPub Targeting param = " + keywords);
                moPubInterstitial.setKeywords(keywords.toString());
                Log.d(TAG, "Successfully added targeting from all partners.");
                partnerTargeting.clear();
            } else {
                Log.e(TAG, "Failed to add targeting from partners.");
            }
        });
    }
    //endregion

    //region Protected Methods
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != interstitial) {
            interstitial.destroy();
        }
    }
    //endregion

    //region POBBidEventListener
    @SuppressLint("LongLogTag")
    @Override
    public void onBidReceived(@NonNull POBBidEvent pobBidEvent, @NonNull POBBid pobBid) {
        // No need to pass OW's targeting info to bidding manager, as it will be passed to MoPub internally.
        Log.d(TAG, "Successfully received bids from OpenWrap.");
        if (biddingManager != null) {
            biddingManager.notifyOpenWrapBidEvent();
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBidFailed(@NonNull POBBidEvent pobBidEvent, @NonNull POBError pobError) {
        Log.d(TAG, "Failed to receive bids from OpenWrap. Error: " + pobError);
        if (biddingManager != null) {
            biddingManager.notifyOpenWrapBidEvent();
        }
    }
    //endregion

    //region BiddingManagerListener
    /**
     * Manager class uses this method to notify once response is received from all the
     * registered bidders of different partners and at least one of them is a
     * success response with non-null response body.
     *
     * @param response  Targeting information map. This map is in the form of
     *                  {partnerName1: response1. partnerName2: response2, ...}
     *                  response1, response2 etc. are key-value pairs of targeting information.
     */
    @Override
    public void onResponseReceived(@Nullable Map<String, Map<String, List<String>>> response) {
        // This method will be invoked as soon as responses from all the bidders are received.
        // Here, client side auction can be performed between the bids available in response map.

        // To send the bids' targeting to MoPub, add targeting from received response in
        // partnerTargeting map. This will be sent to MoPub request using config listener,
        // which is set in onCreate() method of this activity.
        // Config listener will be called just before making an ad request to MoPub.
        partnerTargeting = response;
        interstitial.proceedToLoadAd();
    }

    /**
     * Manager class uses this method to notify once response is received from all the
     * registered bidders of different partners and all of them have failed with error.
     * This callback method will also be used in cases, where bidders have responded with
     * success but no single non-null response body is available.
     *
     * @param error     Error object
     */
    @Override
    public void onResponseFailed(@Nullable Object error) {
        // No response is available from other bidders, so no need to do anything.
        // Just call proceedToLoadAd. OpenWrap SDK will have it's response saved internally
        // so it can proceed accordingly.
        interstitial.proceedToLoadAd();
    }
    //endregion

    //region POBInterstitialListener
    class POBInterstitialListener extends POBInterstitial.POBInterstitialListener {
        private final String TAG = "POBInterstitialListener";

        /**
         * Callback method notifies that an ad has been received successfully.
         * @param ad    Instance of POBInterstitial
         */
        @Override
        public void onAdReceived(POBInterstitial ad) {
            Log.d(TAG, "onAdReceived");
            // Method gets called when ad gets loaded in container
            // Here, you can show interstitial ad to user
            findViewById(R.id.showAdBtn).setEnabled(true);
        }

        /**
         * Callback method notifies an error encountered while loading or
         * rendering an ad.
         *
         * @param ad        Instance of POBInterstitial
         * @param error     Details of error occurred
         */
        @Override
        public void onAdFailed(POBInterstitial ad, POBError error) {
            // Method gets called when loadAd fails to load ad
            // Here, you can put logger and see why ad failed to load
            Log.d(TAG, "onAdFailed :" + error.toString());
        }

        /**
         * Callback method notifies that a user interaction will open another
         * app (for example, App Store), leaving the current app.
         *
         * @param ad    Instance of POBInterstitial
         */
        @Override
        public void onAppLeaving(POBInterstitial ad) {
            Log.d(TAG, "onAppLeaving");
        }

        /**
         * Callback method notifies that the interstitial ad will be presented as a
         * modal on top of the current view controller
         *
         * @param ad    Instance of POBInterstitial
         */
        @Override
        public void onAdOpened(POBInterstitial ad) {
            Log.d(TAG, "onAdOpened");
        }

        /**
         * Callback method notifies that the interstitial ad has been animated
         * off the screen.
         * @param ad    Instance of POBInterstitial
         */
        @Override
        public void onAdClosed(POBInterstitial ad) {
            Log.d(TAG, "onAdClosed");
        }

        /**
         * Callback method notifies ad click
         *
         * @param ad    Instance of POBInterstitial
         */
        @Override
        public void onAdClicked(POBInterstitial ad) {
            Log.d(TAG, "onAdClicked");
        }
    }
    //endregion
}
