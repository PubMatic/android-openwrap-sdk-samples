/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2021 PubMatic, All Rights Reserved.
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

import android.content.Context
import android.util.Log
import com.amazon.device.ads.DTBAdSize
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.pubmatic.openwrap.listapp.Constants
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.banner.POBBannerView
import com.pubmatic.sdk.openwrap.core.POBBid
import com.pubmatic.sdk.openwrap.core.POBBidEvent
import com.pubmatic.sdk.openwrap.core.POBBidEventListener
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeEventHandler
import java.net.MalformedURLException
import java.net.URL


/**
 * This class is responsible to handle OW SDK + TAM parallel header bidding into GAM
 * by loading bid from TAM, OpenWrap in parallel manner and provide those bids to GAM SDK.
 * After the completion of whole header bidding execution this class provides either success or
 * failure callback to it's listener. It can provide banner, native and custom native ads callback.
 */
class GAMNativeBannerAdLoader(val appContext: Context, val adSize: AdSize, val slotId: String,
                              val profileId: Int, val pubId: String, val owAdUnitId: String,
                              val gamAdUnitId: String) : POBBidEventListener, BiddingManagerListener, AdLoaderEvent{


    private val biddingManager: BiddingManager

    // Map to maintain response from different partners
    private var partnerTargeting: MutableMap<String?, Map<String?, List<String?>?>?>? = null

    // Banner view property
    override var banner: POBBannerView? = null

    // Listener property to listen ad loader callbacks
    override var listener: AdLoaderListener? = null

    // Property flag to identify whether ad is received
    override var isAdReceived = false

    init {
        // Create bidding manager
        biddingManager = BiddingManager()
        // Set listener to bidding manager events.
        biddingManager.setBiddingManagerListener(this)
    }

    /**
     * Load Ad with parallel header bidding
     */
    override fun loadAd(){
        // Load TAM Bids
        val tamAdLoader = TAMAdLoader(DTBAdSize(adSize.width, adSize.height, slotId))
        biddingManager.registerBidder(tamAdLoader)
        biddingManager.loadBids()

        // Load OpenWrap bids
        // A valid Play Store Url of an Android application is required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.storeURL =
                URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, The code below creates an event handler for DFP ad server.
        val eventHandler = GAMNativeEventHandler(appContext, gamAdUnitId, adSize)

        // Prepares handler to request GAM's NativeAd
        eventHandler.configureNativeAd(object : GAMNativeEventHandler.NativeAdListener() {
            // Callback method notifies that GAM native ad has been successfully loaded.
            override fun onAdReceived(nativeAd : NativeAd) {
                Log.d(TAG, "Native Ad Received")
                isAdReceived = true
                listener?.onNativeAdReceived(nativeAd)
            }
        })

        // Prepares handler to request GAM's NativeCustomFormatAd
        eventHandler.configureNativeCustomFormatAd(Constants.CUSTOM_NATIVE_FORMAT, object :
            GAMNativeEventHandler.NativeCustomFormatAdListener() {
            // Callback method notifies that GAM custom native ad has been successfully loaded.
            override fun onAdReceived(customNativeAd: NativeCustomFormatAd) {
                Log.d(TAG, "Custom Native Ad Received")
                isAdReceived = true
                listener?.onCustomNativeAdReceived(customNativeAd)
            }
        }, null)


        // Set config block to event handler in order to provide TAM's ads bid targeting to DFP
        eventHandler.setConfigListener(object : GAMNativeEventHandler.GAMConfigListener{
            override fun configure(builder: AdManagerAdRequest.Builder, p1: POBBid?) {
                // By this time adloader assume partner targeting includes TAM's bid
                partnerTargeting?.keys?.forEach {
                    val bidderResponse: Map<String?, List<String?>?>? = partnerTargeting?.get(it)
                    bidderResponse?.keys?.forEach{ it ->
                        val targeting =  bidderResponse.get(it);
                        if(it!=null && targeting!=null) {
                            builder.addCustomTargeting(it, targeting)
                        }
                    }
                }?: kotlin.run {
                    Log.e(TAG, "Failed to add targeting from partners.")
                }
                partnerTargeting?.clear()
            }
        })

        // Initialise banner view
        banner = POBBannerView(appContext)
        banner?.init(pubId, profileId, owAdUnitId, eventHandler)

        // Optional listener to listen banner events
        banner?.setListener(object : POBBannerView.POBBannerViewListener(){
            override fun onAdReceived(bannerView: POBBannerView) {
                isAdReceived = true
                listener?.onAdReceived(bannerView)
            }

            override fun onAdFailed(bannerView: POBBannerView, pobError: POBError) {
                Log.e(TAG, pobError.toString())
                isAdReceived = false
                listener?.onAdFailed(pobError)
            }
        })

        // Set listener to get get bid details
        banner?.setBidEventListener(this)

        // Request bids from PubMatic OpenWrap SDK & Load Ad
        banner?.loadAd()
    }

    /**
     * Cleans up banner
     */
    override fun destroy(){
        banner?.destroy()
    }

    override fun onBidFailed(pobBidEvent: POBBidEvent, pobError: POBError) {
        Log.e(TAG, "Failed to receive bids from OpenWrap. Error: $pobError")
        // Notify bidding manager that OpenWrap's response is received.
        biddingManager.notifyOpenWrapBidEvent()
    }

    override fun onBidReceived(pobBidEvent: POBBidEvent, bid: POBBid) {
        // No need to pass OW's targeting info to bidding manager, as it will be passed to DFP internally.
        Log.d(TAG, "Successfully received bids from OpenWrap.")
        // Notify bidding manager that OpenWrap's response is received.
        biddingManager.notifyOpenWrapBidEvent()
    }

    override fun onResponseReceived(response: MutableMap<String?, Map<String?, List<String?>?>?>?) {
        // This method will be invoked as soon as responses from all the bidders are received.
        // Here, client side auction can be performed between the bids available in response map.

        // To send the bids' targeting to DFP, add targeting from received response in
        // partnerTargeting map. This will be sent to DFP request using config listener,
        // which is set in onCreate() method of this activity.
        // Config listener will be called just before making an ad request to DFP.
        if (response != null) {
            partnerTargeting = response
        }
        banner?.proceedToLoadAd()
    }

    override fun onResponseFailed(error: Any?) {
        // No response is available from other bidders, so no need to do anything.
        // Just call proceedToLoadAd. OpenWrap SDK will have it's response saved internally
        // so it can proceed accordingly.
        banner?.proceedToLoadAd()
    }

    // Ad loader constants.
    companion object{
        const val TAG = "GAMNativeBannerAdLoader"
    }

}