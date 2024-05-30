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

package com.pubmatic.app.adloader;

import com.pubmatic.sdk.common.POBError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Bidding manager class to manage bids loaded from all the bidders
 * of various SDKs, e.g. A9 TAM
 * This class internally waits for all the bidders to respond with success/failure
 * and notifies the calling class with aggregated response once all responses are
 * received.
 */
public class BiddingManager implements BiddingListener {

    /** List of registered bidders. */
    @NonNull
    private List<Bidding> biddersList;

    /**
     * Queue of registered bidders. It will have same objects as biddersList
     * but, the bidders will be removed from biddersQueue once they respond.
     * Objects from biddersList will not be modified.
     */
    @Nullable
    private List<Bidding> biddersQueue;

    /**
     * Map of responses
     * {partnerName1: response1. partnerName2: response2, ...}
     * response1, response2 etc. are key-value pairs of targeting information.
     */
    @NonNull
    private Map<String, Map<String, List<String>>> responses;

    /**
     * Listener for BiddingManagerListener interface to get the callbacks
     * once response from all the bidders are received.
     */
    @Nullable
    private BiddingManagerListener biddingManagerListener;

    /** Flag to identify if response from OpenWrap SDK is received. */
    private boolean isOWResponseReceived = false;

    /**
     * Constructor.
     */
    public BiddingManager() {
        biddersList = new ArrayList<>();
        responses = new HashMap<>();
    }

    //region Public APIs
    /**
     * Method to register bidders in bidding manager.
     * Separate class should be created for each partner integration (e.g. A9 TAM)
     * implementing Bidding interface and should be registered in bidding manager.
     * Bidding manager keeps the bidders in a queue, sends load bids request simultaniously
     * to all the registered bidders
     *
     * @param bidder    Bidder object implementing Bidding interface
     */
    public void registerBidder(@NonNull Bidding bidder) {
        // Set bidding listener to received bidder.
        bidder.setBiddingListener(this);
        // Add bidder in bidder's list.
        // Please note, bidders should be registered only once.
        biddersList.add(bidder);
    }

    /**
     * Setter for listener for BiddingManagerListener interface to get the callbacks
     * once response from all the bidders are received.
     *
     * @param listener  Instance implementing BiddingManagerListener interface
     */
    public void setBiddingManagerListener(@Nullable BiddingManagerListener listener) {
        biddingManagerListener = listener;
    }

    /**
     * Method to notify that response from OpenWrap SDK is received.
     */
    public void notifyOpenWrapBidEvent() {
        isOWResponseReceived = true;
        // Call processResponse as OpenWrap's bids are received.
        processResponse();
    }


    /**
     * Method to instruct bidding manager to load bids
     * Bidding manager internally instructs all the registered bidders
     * to load bids using Bidding interface.
     */
    public void loadBids() {
        // Reset bidders queue
        biddersQueue = new ArrayList<>(biddersList);
        // Clear earlier response.
        responses.clear();

        // Send load bids request to all the bidders available in bidders queue.
        if (!biddersQueue.isEmpty()) {
            for (Bidding bidder : biddersQueue) {
                bidder.loadBid();
            }
        }
    }
    //endregion

    //region BiddingListener
    /**
     * Method to notify manager class on receiving bids successfully by bidder.
     *
     * @param bidder    Instance of a bidder sending notification
     * @param response  Targeting information map. This map is in the form of
     *                  partnerName: Response
     */
    @Override
    public void onResponseReceived(@NonNull Bidding bidder, @Nullable Map<String, Map<String, List<String>>> response) {
        // The bidder have responded with success, collect all the responses in responses map
        // and remove it from bidders queue.
        if (response != null) {
            responses.putAll(response);
        }
        removeRespondedBidder(bidder);
        processResponse();
    }

    /**
     * Method to notify manager class on receiving failure while fetching bids by bidder.
     *
     * @param bidder    Instance of a bidder sending notification
     * @param error     Error object
     */
    @Override
    public void onResponseFailed(@NonNull Bidding bidder, @Nullable Object error) {
        // The bidder have responded with failure, so remove it from bidders queue
        removeRespondedBidder(bidder);
        processResponse();
    }
    //endregion

    //region Private Methods
    /**
     * Method to remove bidder from biddersQueue once it responds
     * with success/failure.
     *
     * @param bidder    Bidder object to be removed from biddersQueue.
     */
    private void removeRespondedBidder(@NonNull Bidding bidder) {
        // Remove responded bidders from bidders queue
        if (biddersQueue != null && !biddersQueue.isEmpty()) {
            biddersQueue.remove(bidder);
        }
    }

    /**
     * Method to process received responses.
     * This internally checks if all the bidders have responded. If yes, it notifies
     * activity about aggregated responses using BiddingManagerListener interface.
     *
     * So, it is required to call this method everytime, response is
     * received from any bidder.
     */
    private void processResponse() {
        // Wait for all the bidders and OpenWrap to respond
        if (biddersQueue != null && biddersQueue.size() == 0 && isOWResponseReceived) {
            if (biddingManagerListener != null) {
                if (responses.size() > 0) {
                    biddingManagerListener.onResponseReceived(responses);
                } else {
                    POBError error = new POBError(-1, "Failed to receive targeting from all the bidders.");
                    biddingManagerListener.onResponseFailed(error);
                }
            }
            isOWResponseReceived = false;
        }
    }
    //endregion
}
