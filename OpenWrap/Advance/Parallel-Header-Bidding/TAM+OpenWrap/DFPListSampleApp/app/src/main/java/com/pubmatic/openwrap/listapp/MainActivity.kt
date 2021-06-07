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
package com.pubmatic.openwrap.listapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazon.device.ads.AdRegistration
import com.amazon.device.ads.MRAIDPolicy
import com.pubmatic.openwrap.listapp.adloader.AdLoader
import com.pubmatic.sdk.common.OpenWrapSDK


/**
 * Activity definition to demonstrate how to add banner inside recycler view.
 */
class MainActivity : AppCompatActivity() {
    // Feed list
    private var feedList = ArrayList<FeedItem>()

    // recycler view
    private var recyclerView: RecyclerView? = null

    // To check app already has the requested permission.
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        // Registers/Initialize TAM
        registerTAMAds()

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
        if (Build.VERSION.SDK_INT >= 30) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE)
        }
        val PERMISSIONS: Array<String> = permissionList.toTypedArray()

        if (!hasPermissions(this, PERMISSIONS)) {
            val MULTIPLE_PERMISSIONS_REQUEST_CODE = 123
            ActivityCompat.requestPermissions(this, PERMISSIONS,
                MULTIPLE_PERMISSIONS_REQUEST_CODE)
        }
    }


    /**
     * Initialize/Registers TAM App, only once whiling starting activity. Can also be done at Application's
     * onCreate() method
     */
    private fun registerTAMAds() {
        AdRegistration.getInstance(Constants.APP_KEY, this)
        //Please remove enableLogging and enableTesting in production mode
        AdRegistration.enableLogging(true)
        AdRegistration.enableTesting(true)
        //Optional. Highly recommended for Transparent Ad Marketplace users
        AdRegistration.useGeoLocation(true)
        // If you are using Google Play Services 15.0.0 and plus, please call the following
        AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
        AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up, banner instance when not in use or before finishing current activity.
        feedList.forEach { feed ->
            feed.adLoader?.destroy()
        }
    }

    // Create feed list by adding news and banner items
    private fun createFeeds(): ArrayList<FeedItem>{
        val feedList = ArrayList<FeedItem>()
        for (i in 1..Constants.MAX_FEEDS){
            // Condition to add banner AdLoader to every nth interval in the feed list
            if(i % Constants.BANNER_INTERVAL == 0){
                // Initialize ad loader with required details
                val adLoader = AdLoader(this.applicationContext, Constants.AD_SIZE, Constants.SLOT_ID,
                    Constants.PROFILE_ID, Constants.PUB_ID, Constants.OPENWRAP_AD_UNIT_ID,
                    Constants.DFP_AD_UNIT_ID)
                var isLoaded = false
                // Load first ad loader in order to Pre-load first banner
                if(i == Constants.BANNER_INTERVAL){
                    isLoaded = true
                    adLoader.loadAd()
                }
                // Add AdLoader into feedlist
                feedList.add(FeedItem("Banner", FeedItem.FeedType.BANNER, adLoader, isLoaded))
            }else{
                val itemTitle = "FeedItem: $i"
                feedList.add(FeedItem(itemTitle, FeedItem.FeedType.NEWS, null, false))
            }
        }
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

    // Loads next banner AdLoader if not loaded and mark it as loaded, else skip
    private fun loadNextBanner(){
        feedList.forEach { feedItem ->
            if(feedItem.feedType == FeedItem.FeedType.BANNER && !feedItem.isBannerLoaded){
                Log.d("MainActivity", "Next load ad :$feedItem")
                feedItem.adLoader?.loadAd()
                feedItem.isBannerLoaded = true
                return
            }
        }
    }


}
