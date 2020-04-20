package com.pubmatic.openwrap.kotlinsampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.pubmatic.openwrap.kotlinsampleapp.mopubevent.MoPubBannerEventHandler
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBAdSize
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openwrap.banner.POBBannerView
import kotlinx.android.synthetic.main.activity_banner.*
import java.net.MalformedURLException
import java.net.URL


class BannerActivity : AppCompatActivity() {

    val TAG = "BannerActivity"
    private val OPENWRAP_AD_UNIT_ID = "625ca5d499ab435fa55c98065cc9b3c2"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1302
    private val MOPUB_AD_UNIT_ID = "625ca5d499ab435fa55c98065cc9b3c2"

    private var banner: POBBannerView ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner)
        setSupportActionBar(toolbar)
        OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.All)

        // A valid Play Store Url of an Android app. Required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.setStoreURL(URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo)

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, The code below creates an event handler for MoPub ad server.
        val eventHandler = MoPubBannerEventHandler(this, MOPUB_AD_UNIT_ID, POBAdSize(320, 50))

        // Call init() to set tag information
        // For test IDs see - https://community.pubmatic.com/x/mQg5AQ#TestandDebugYourIntegration-TestWrapperProfile/Placement
        banner = findViewById(R.id.banner)
        banner?.init(PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, eventHandler)

        //optional listener to listen banner events
        banner?.setListener(POBBannerViewListener())

        // Call loadAd() on banner instance
        banner?.loadAd()


    }

    // POBBannerViewListener listener
    class POBBannerViewListener : POBBannerView.POBBannerViewListener(){
        val TAG = "POBBannerViewListener"

        // Callback method Notifies that an  banner ad has been successfully loaded and rendered.
        override fun onAdReceived(view: POBBannerView?) {
            Log.d(TAG, "onAdReceived")
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        override fun onAdFailed(view: POBBannerView?, error: POBError?) {
            Log.e(TAG, "onAdFailed : Ad failed with error -" + error.toString())
        }

        // Callback method Notifies that the  banner ad will launch a dialog on top of the current view
        override fun onAdOpened(view: POBBannerView?) {
            Log.d(TAG, "onAdOpened")
        }

        // Callback method Notifies that the  banner ad has dismissed the modal on top of the current view
        override fun onAdClosed(view: POBBannerView?) {
            Log.d(TAG, "onAdClosed")
        }

    }


    override fun onDestroy() {
        // destroy banner before onDestroy of Activity lifeCycle
        super.onDestroy()
        banner?.destroy()
    }
}
