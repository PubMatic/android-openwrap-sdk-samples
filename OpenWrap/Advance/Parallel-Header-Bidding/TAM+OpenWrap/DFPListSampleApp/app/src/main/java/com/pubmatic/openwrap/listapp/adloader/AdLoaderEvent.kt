package com.pubmatic.openwrap.listapp.adloader

import com.pubmatic.sdk.openwrap.banner.POBBannerView

/**
 * Your Ad loader must implement this interface to manage ads loading with parallel header bidding.
 */
interface AdLoaderEvent {

    // Listener property to listen ad loader callbacks
    var listener: AdLoaderListener?

    // Banner view property
    var banner: POBBannerView?

    // Property flag to identify whether ad is received
    var isAdReceived : Boolean

    /**
     * Load Ad with parallel header bidding
     */
    fun loadAd()

    /**
     * To clean up all bidders
     */
    fun destroy()
}