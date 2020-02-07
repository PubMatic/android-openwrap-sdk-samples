package com.pubmatic.openwrap.listapp

import com.pubmatic.sdk.openwrap.banner.POBBannerView

/**
 * Data class represent feed item.
 */
data class FeedItem(val title: String, val feedType: FeedType, val banner: POBBannerView?,
                    var isBannerLoaded: Boolean) {

    /**
     * Enum defines types of feed
      */
    enum class FeedType {
        NEWS,
        BANNER
    }
}