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
package com.pubmatic.openwrap.listapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBAdSize
import com.pubmatic.sdk.openwrap.banner.POBBannerView


/**
 * Activity definition to demonstrate how to add banner inside recycler view.
 */
class MainActivity : AppCompatActivity() {

    // Banner Ad Units
    private val OPENWRAP_AD_UNIT_ID = "OpenWrapBannerAdUnit"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1757

    // Feed list
    private var feedList = ArrayList<FeedItem>()

    // recycler view
    private var recyclerView: RecyclerView? = null

    // To check app already has the requested permission.
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set SDK logs to all, in order to get all the logs
        OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.All)

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
        val adapter = FeedListAdapter(feedList)

        // Set adapter callback to get banner bind callback
        adapter.setBannerLoadedListener(OnBannerBindCallback())

        recyclerView?.adapter = adapter

        // Ask permission from user for location and write external storage
        val permissionList: MutableList<String> = ArrayList()
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        // Ask permission from user for READ_PHONE_STATE permission if api level 30 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE)
        }
        val PERMISSIONS: Array<String> = permissionList.toTypedArray()

        if (!hasPermissions(this, PERMISSIONS)) {
            val MULTIPLE_PERMISSIONS_REQUEST_CODE = 123
            ActivityCompat.requestPermissions(this, PERMISSIONS,
                MULTIPLE_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up, banner instance when not in use or before finishing current activity.
        feedList.forEach { feed ->
            feed.banner?.destroy()
        }
    }

    // Create feed list by adding news and banner items
    // Here you can add multiple banner in any sequence
    private fun createFeeds(): ArrayList<FeedItem>{
        val feedList = ArrayList<FeedItem>()
        // Initialize Banner no 1 and load it(Pre-load first banner)
        val banner1 = POBBannerView(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_300x250)
        banner1.loadAd()

        // Banner no 2
        val banner2 = POBBannerView(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_300x250)

        // Banner no 3
        val banner3 = POBBannerView(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, POBAdSize.BANNER_SIZE_300x250)

        feedList.add(FeedItem("News 1", FeedItem.FeedType.NEWS, null, false))
        feedList.add(FeedItem("News 2", FeedItem.FeedType.NEWS, null, false))

        // Since we are pre-loading banner 1, mark FeedItem's isBannerLoaded flag to true
        feedList.add(FeedItem("Banner 1", FeedItem.FeedType.BANNER, banner1, true))

        feedList.add(FeedItem("News 3", FeedItem.FeedType.NEWS, null, false))
        feedList.add(FeedItem("News 4", FeedItem.FeedType.NEWS, null, false))
        feedList.add(FeedItem("Banner 2", FeedItem.FeedType.BANNER, banner2, false))

        feedList.add(FeedItem("News 5", FeedItem.FeedType.NEWS, null, false))
        feedList.add(FeedItem("News 6", FeedItem.FeedType.NEWS, null, false))
        feedList.add(FeedItem("Banner 3", FeedItem.FeedType.BANNER, banner3, false))
        feedList.add(FeedItem("News 7", FeedItem.FeedType.NEWS, null, false))
        return feedList
    }

    /**
     * Inner class to get adapter callbacks
     */
    inner class OnBannerBindCallback: FeedListAdapter.OnBannerBindListener{
        override fun onBannerBind() {
            loadNextBanner()
        }
    }

    // Loads next banner if not loaded and mark it as loaded, else skip
    private fun loadNextBanner(){
        feedList.forEach { feedItem ->
            if(feedItem.feedType == FeedItem.FeedType.BANNER && !feedItem.isBannerLoaded){
                feedItem.banner?.loadAd()
                feedItem.isBannerLoaded = true
                return
            }
        }
    }


}
