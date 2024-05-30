/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2024 PubMatic, All Rights Reserved.
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
import com.google.android.gms.ads.AdSize;
import com.pubmatic.app.adloader.BiddingManager;
import com.pubmatic.app.adloader.BiddingManagerListener;
import com.pubmatic.app.adloader.TAMAdLoader;
import com.pubmatic.appapp.R;
import com.pubmatic.sdk.common.OpenWrapSDK;
import com.pubmatic.sdk.common.POBError;
import com.pubmatic.sdk.common.models.POBApplicationInfo;
import com.pubmatic.sdk.openwrap.banner.POBBannerView;
import com.pubmatic.sdk.openwrap.core.POBBid;
import com.pubmatic.sdk.openwrap.core.POBBidEvent;
import com.pubmatic.sdk.openwrap.core.POBBidEventListener;
import com.pubmatic.sdk.openwrap.eventhandler.dfp.DFPBannerEventHandler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Activity to display banner ad.
 */
public class DFPBannerActivity extends AppCompatActivity implements POBBidEventListener, BiddingManagerListener {

    private static final String TAG = "DFPBannerActivity";

    // PubMatic Ad tag details
    private static final String OPENWRAP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-Banner";
    private static final String PUB_ID = "156276";
    private static final int PROFILE_ID = 1165;

    // DFP Ad unit id
    private static final String DFP_AD_UNIT_ID = "/15671365/pm_sdk/A9_Demo";

    // A9 TAM slot id
    private final String SLOT_ID = "5ab6a4ae-4aa5-43f4-9da4-e30755f2b295";

    // OpenWrap SDK's banner view
    private POBBannerView banner;
    // OpenWrap SDK's event handler for DFP
    private DFPBannerEventHandler eventHandler;

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
        setContentView(R.layout.activity_dfp_wrapper);

        // Create bidding manager
        biddingManager = new BiddingManager();
        // Set listener to bidding manager events.
        biddingManager.setBiddingManagerListener(this);

        // Register bidders to bidding manager
        registerBidders();

        /*
         * Fetch bids, simultaneously, from other partners e.g. A9 TAM(using DTB SDK) & PubMatic OpenWrap
         * partners(using OpenWrap SDK).
         * As per this implementation, both A9 TAM and OpenWrap's bids are added in DFP custom targeting after they are available
         * before DFP ad call is initiated by the DFPBannerEventHandler.
         */
        // Request bidding manager to load bids from all the connected bidders.
        biddingManager.loadBids();

        // Create OpenWrap banner object and load ad
        // DFP ad call is initiated once the bid response is received from PubMatic.
        loadOpenWrapBids();

        // Set config listener on POBBannerView to pass partner targeting to DFP
        setConfigListener();
    }
    //endregion

    //region Private Methods
    /**
     * Fetch bid from OpenWrap SDK and load DFP ad.
     */
    private void loadOpenWrapBids(){
        // A valid Play Store Url of an Android application is required.
        POBApplicationInfo appInfo = new POBApplicationInfo();
        try {
            appInfo.setStoreURL(new URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)f
        OpenWrapSDK.setApplicationInfo(appInfo);

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, The code below creates an event handler for DFP ad server.
        eventHandler = new DFPBannerEventHandler(this ,DFP_AD_UNIT_ID, AdSize.BANNER);

        // Initialise banner view
        banner = findViewById(R.id.banner);
        banner.init(PUB_ID,
                PROFILE_ID,
                OPENWRAP_AD_UNIT_ID,
                eventHandler);


        // Optional listener to listen banner events
        banner.setListener(new POBBannerViewListener());

        // Set listener to get get bid details
        banner.setBidEventListener(this);

        // Request bids from PubMatic OpenWrapSDK &lLoad Ad
        banner.loadAd();
    }

    /**
     * Register bidders to bidding manager
     */
    private void registerBidders() {
        // You can create other bidders here and register to bidding manager.
        // Add A9 TAM bidder
        if (biddingManager != null) {
            TAMAdLoader tamAdLoader = new TAMAdLoader(new DTBAdSize(320, 50, SLOT_ID));
            biddingManager.registerBidder(tamAdLoader);
        }
    }

    /**
     * Set config listener on POBBannerView instance
     */
    @SuppressLint("LongLogTag")
    private void setConfigListener() {
        eventHandler.setConfigListener((dfpAdView, builder, pobBid) -> {
            if (partnerTargeting != null && partnerTargeting.size() > 0) {
                // Iterate partnerTargeting map for all the key-value pairs
                for (String partnerName : partnerTargeting.keySet()) {
                    // Get key-value targeting for partner with name partnerName
                    Map<String, List<String>> bidderResponse = partnerTargeting.get(partnerName);
                    if (bidderResponse != null) {
                        for (String key : bidderResponse.keySet()) {
                            builder.addCustomTargeting(key, bidderResponse.get(key));
                            Log.d(TAG,partnerName + " targeting param [" + key + "] = " + bidderResponse.get(key));
                        }
                    }
                }
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
        // Destroy banner before destroying activity
        if (null != banner) {
            banner.destroy();
        }
    }
    //endregion

    //region POBBidEventListener
    @Override
    public void onBidReceived(@NonNull POBBidEvent pobBidEvent, @NonNull POBBid pobBid) {
        // No need to pass OW's targeting info to bidding manager, as it will be passed to DFP internally.
        Log.d(TAG, "Successfully received bids from OpenWrap.");
        // Notify bidding manager that OpenWrap's success response is received.
        if (biddingManager != null) {
            biddingManager.notifyOpenWrapBidEvent();
        }
    }

    @Override
    public void onBidFailed(@NonNull POBBidEvent pobBidEvent, @NonNull POBError pobError) {
        Log.d(TAG, "Failed to receive bids from OpenWrap. Error: " + pobError);
        // Notify bidding manager that OpenWrap's success response is received.
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

        // To send the bids' targeting to DFP, add targeting from received response in
        // partnerTargeting map. This will be sent to DFP request using config listener,
        // which is set in onCreate() method of this activity.
        // Config listener will be called just before making an ad request to DFP.
        if (response != null) {
            partnerTargeting = response;
        }
        banner.proceedToLoadAd();
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
    public void onResponseFailed(Object error) {
        // No response is available from other bidders, so no need to do anything.
        // Just call proceedToLoadAd. OpenWrap SDK will have it's response saved internally
        // so it can proceed accordingly.
        banner.proceedToLoadAd();
    }
    //endregion

    //region POBBannerViewListener
    class POBBannerViewListener extends POBBannerView.POBBannerViewListener {
        private final String TAG = "POBBannerViewListener";

        /**
         * Callback method Notifies that an ad has been successfully
         * loaded and rendered.
         *
         * @param view  Instance of POBBannerView
         */
        @Override
        public void onAdReceived(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Received");

            // OpenWrap SDK will start refresh loop internally as soon as ad rendering succeeds/fails.
            // To include other partner bids in next refresh cycle, call loadBids on bidding manager.
            if (biddingManager != null) {
                biddingManager.loadBids();
            }
        }

        /**
         * Callback method Notifies an error encountered while loading
         * or rendering an ad.
         *
         * @param view      Instance of POBBannerView
         * @param error     Details of error occurred
         */
        @Override
        public void onAdFailed(@NonNull POBBannerView view, @NonNull POBError error) {
            Log.e(TAG, error.toString());

            // OpenWrap SDK will start refresh loop internally as soon as ad rendering succeeds/fails.
            // To include other partner bids in next refresh cycle, call loadBids on bidding manager.
            if (biddingManager != null) {
                biddingManager.loadBids();
            }
        }

        /**
         * Callback method Notifies whenever current app goes in the
         * background due to user click.
         *
         * @param view  Instance of POBBannerView
         */
        @Override
        public void onAppLeaving(@NonNull POBBannerView view) {
            Log.d(TAG, "App Leaving");
        }

        /**
         * Callback method Notifies that the banner ad view will launch a dialog
         * on top of the current view
         *
         * @param view  Instance of POBBannerView
         */
        @Override
        public void onAdOpened(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Opened");
        }

        /**
         * Callback method Notifies that the banner ad view is clicked.
         *
         * @param view  Instance of POBBannerView
         */
        @Override
        public void onAdClicked(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Clicked");
        }

        /**
         * Callback method Notifies that the banner ad view has dismissed the modal
         * on top of the current view.
         *
         * @param view  Instance of POBBannerView
         */
        @Override
        public void onAdClosed(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Closed");
        }

        @Override
        public void onAdImpression(@NonNull POBBannerView view) {
            Log.d(TAG, "Ad Impression");
        }

    }
    //endregion
}
