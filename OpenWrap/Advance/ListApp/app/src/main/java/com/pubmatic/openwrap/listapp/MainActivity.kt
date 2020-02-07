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

    // Array of permission required by app to load banner
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // Banner Ad Units
    private val OPENWRAP_AD_UNIT_ID = "OpenWrapBannerAdUnit"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1757

    // Feed list
    private var feedList = ArrayList<FeedItem>()

    // recycler view
    private var recyclerView: RecyclerView? = null

    // To check app already has the requested permission.
    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
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
