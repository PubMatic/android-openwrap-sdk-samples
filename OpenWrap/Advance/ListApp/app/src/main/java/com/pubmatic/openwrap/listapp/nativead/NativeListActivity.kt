/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2025 PubMatic, All Rights Reserved.
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

package com.pubmatic.openwrap.listapp.nativead

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pubmatic.openwrap.listapp.Constants
import com.pubmatic.openwrap.listapp.Constants.AD_INTERVAL
import com.pubmatic.openwrap.listapp.R
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.nativead.POBNativeAd
import com.pubmatic.sdk.nativead.POBNativeAdListener
import com.pubmatic.sdk.nativead.POBNativeAdLoader
import com.pubmatic.sdk.nativead.POBNativeAdLoaderListener
import com.pubmatic.sdk.nativead.datatype.POBNativeTemplateType

/**
 * Activity used to demonstrate the use of native-ad in recycler view
 */
class NativeListActivity : AppCompatActivity(), NativeFeedListAdapter.OnNativeAdBindListener{

    // Native Ad Units
    private val OPENWRAP_AD_UNIT_ID = "OpenWrapNativeAdUnit"

    // Feed list
    private var feedList = ArrayList<NativeFeedItem>()

    // recycler view
    private var recyclerView: RecyclerView? = null

    private var adRequestCount = 0

    private var adIndex = AD_INTERVAL

    private var nativeAdLoader: POBNativeAdLoader? = null

    private val AD_THRESHOLD = Constants.NUMBER_OF_ITEMS / Constants.AD_INTERVAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_list)

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
        val adapter = NativeFeedListAdapter(feedList)

        adapter.setNativeAdLoadedListener(this)

        recyclerView?.adapter = adapter

        nativeAdLoader = createNativeAdLoader()
        nativeAdLoader?.loadAd()
        adRequestCount++
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up, native ad related instances when not in use or before finishing current activity.
        for (i in AD_INTERVAL - 1 until feedList.size step AD_INTERVAL) {
            val feed = feedList[i]
            feed.nativeAd?.destroy()
        }
    }

    // Create feed list by adding placement and nativeAd items
    // Here you can add multiple nativeAd in any sequence
    private fun createFeeds(): ArrayList<NativeFeedItem>{
        val feedList = ArrayList<NativeFeedItem>()
        for (i in 1..Constants.NUMBER_OF_ITEMS) {
            if (i % (AD_INTERVAL) == 0) {
                feedList.add(NativeFeedItem(NativeFeedItem.FeedType.NATIVE,  null, i-1))
            } else {
                feedList.add(NativeFeedItem(NativeFeedItem.FeedType.PLACEHOLDER, null, i-1))
            }
        }
        return feedList
    }

    /**
     * Function creates the POBNativeAdLoader instance with the required ad placement details.
     */
    private fun createNativeAdLoader(): POBNativeAdLoader {
        val nativeAdLoader =  POBNativeAdLoader(this, Constants.PUB_ID, Constants.PROFILE_ID, OPENWRAP_AD_UNIT_ID,
            POBNativeTemplateType.MEDIUM)
        nativeAdLoader.setAdLoaderListener(object: POBNativeAdLoaderListener{
            override fun onAdReceived(adLoader: POBNativeAdLoader, nativeAd: POBNativeAd) {
                //Render native ad
                nativeAd.renderAd(POBNativeAdListenerImp())
            }

            override fun onFailedToLoad(adLoader: POBNativeAdLoader, error: POBError) {
                adRequestCount--
                Log.d(TAG, "Native : Ad failed to load with error - $error")
            }
        })
        return nativeAdLoader;
    }



    private inner class POBNativeAdListenerImp: POBNativeAdListener{
        override fun onNativeAdRendered(nativeAd: POBNativeAd) {
            // Update feedList with new native and notify adapter
            val nativeFeedItem: NativeFeedItem = feedList[adIndex-1]
            nativeFeedItem.nativeAd = nativeAd
            recyclerView?.adapter?.notifyItemChanged(nativeFeedItem.index)
            adIndex += AD_INTERVAL
        }

        override fun onNativeAdRenderingFailed(nativeAd: POBNativeAd, error: POBError) {
            adRequestCount--
            Log.d(TAG, "Native : Ad failed to show with error - $error")
        }

        override fun onNativeAdImpression(nativeAd: POBNativeAd) {
            Log.d(TAG, "Native : Ad recorded impression")
        }

        override fun onNativeAdClicked(nativeAd: POBNativeAd) {
            Log.d(TAG, "Native : Ad recorded clicked")
        }

        override fun onNativeAdClicked(nativeAd: POBNativeAd, assetId: String) {
            Log.d(TAG, "Native : Ad recorded clicked for asset id - $assetId")
        }

        override fun onNativeAdLeavingApplication(nativeAd: POBNativeAd) {
            Log.d(TAG, "Native : App Leaving")
        }

        override fun onNativeAdOpened(nativeAd: POBNativeAd) {
            Log.d(TAG, "Native : Ad Opened")
        }

        override fun onNativeAdClosed(nativeAd: POBNativeAd) {
            Log.d(TAG, "Native : Ad Closed")
        }

    }


    /**
     * Prefetch the next nativeAd on the callback of the bind nativeAd view from the adapter
     */
    override fun onNativeAdBind(position: Int) {
        // Load maximum which is less than AD_THRESHOLD
        if(adRequestCount < AD_THRESHOLD){
            adRequestCount++
            nativeAdLoader?.loadAd();
        }
    }


    companion object {
        val TAG = "NativeListActivity"
    }
}
