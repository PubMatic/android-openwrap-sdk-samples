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

package com.pubmatic.openwrap.listapp.banner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pubmatic.openwrap.listapp.Constants
import com.pubmatic.openwrap.listapp.R
import com.pubmatic.sdk.common.POBAdSize
import com.pubmatic.sdk.openwrap.banner.POBBannerView

/**
 * Activity definition to demonstrate how to add banner inside recycler view.
 */
class BannerListActivity : AppCompatActivity() {

    // Banner Ad Units
    private val OPENWRAP_AD_UNIT_ID = "OpenWrapBannerAdUnit"
    // Feed list
    private var feedList = ArrayList<BannerFeedItem>()

    // recycler view
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner_list)

        // Initialize the recycler view with necessary properties
        recyclerView = findViewById(R.id.feed_list)
        recyclerView?.setHasFixedSize(true)
        val  layoutManager = LinearLayoutManager(this)
        recyclerView?.layoutManager = layoutManager

        // Create divider and add it to recycler view
        val divider = DividerItemDecoration(this, layoutManager.orientation)
        recyclerView?.addItemDecoration(divider)

        // Create feeds
        feedList = createFeeds()

        // Create adapter by passing feedlist and set it to recycler view
        val adapter = BannerFeedListAdapter(feedList)

        // Set adapter callback to get banner bind callback
        adapter.setBannerLoadedListener(OnBannerBindCallback())

        recyclerView?.adapter = adapter

    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up, banner instance when not in use or before finishing current activity.
        for (i in Constants.AD_INTERVAL - 1 until feedList.size step Constants.AD_INTERVAL) {
            val feed = feedList[i]
            feed.banner?.destroy()
        }
    }

    // Create feed list by adding news and banner items
    // Here you can add multiple banner in any sequence
    private fun createFeeds(): ArrayList<BannerFeedItem>{
        val feedList = ArrayList<BannerFeedItem>()
        var isFirstBannerLoaded = false
        for (i in 1..Constants.NUMBER_OF_ITEMS) {
            if (i % (Constants.AD_INTERVAL + 1) == 0) {
                val banner = POBBannerView(this, Constants.PUB_ID, Constants.PROFILE_ID_FOR_VIDEO, OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_300x250)
                val feedItem = BannerFeedItem("Banner ${i / (Constants.AD_INTERVAL + 1)}", BannerFeedItem.FeedType.BANNER, banner, false)
                feedList.add(feedItem)
                if (!isFirstBannerLoaded) {
                    isFirstBannerLoaded = true
                    feedItem.isBannerLoaded = true
                    banner.loadAd()
                }
            } else {
                feedList.add(BannerFeedItem("News ${i - (i / (Constants.AD_INTERVAL + 1))}", BannerFeedItem.FeedType.NEWS, null, false))
            }
        }
        return feedList
    }

    /**
     * Inner class to get adapter callbacks
     */
    inner class OnBannerBindCallback: BannerFeedListAdapter.OnBannerBindListener {
        override fun onBannerBind(position: Int) {
            loadNextBanner(position)
        }
    }

    // Loads next banner if not loaded and mark it as loaded, else skip
    private fun loadNextBanner(position: Int){
        if ((position + Constants.AD_INTERVAL + 1) < feedList.size) {
            val feedItem = feedList[position + Constants.AD_INTERVAL + 1]
            if (feedItem.feedType == BannerFeedItem.FeedType.BANNER && !feedItem.isBannerLoaded) {
                feedItem.banner?.loadAd()
                feedItem.isBannerLoaded = true
            }
        }
    }


}
