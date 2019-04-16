package com.pubmatic.openbid.kotlinsampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.pubmatic.openbid.kotlinsampleapp.customhandler.CustomBannerEventHandler
import com.pubmatic.sdk.common.OpenBidSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openbid.banner.POBBannerView
import kotlinx.android.synthetic.main.activity_banner.*
import java.net.MalformedURLException
import java.net.URL

/**
 * Banner Activity to show Banner Implementation
 */
class BannerActivity : AppCompatActivity() {

    private val OPENWRAP_AD_UNIT_ID = "OtherASBannerAdUnit"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1165
    private val DUMMY_AD_SERVER_AD_UNIT_ID = "OtherASBannerAdUnit"

    private var banner: POBBannerView ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner)
        setSupportActionBar(toolbar)
        OpenBidSDK.setLogLevel(OpenBidSDK.LogLevel.All)

        // A valid Play Store Url of an Android app. Required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.setStoreURL(URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en"))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenBidSDK.setApplicationInfo(appInfo)

        // Create a banner custom event handler for your ad server. Make sure you use
        // separate event handler objects to create each banner view.
        // For example, The code below creates an event handler for Primary ad server.
        val eventHandler = CustomBannerEventHandler(this, DUMMY_AD_SERVER_AD_UNIT_ID)

        // Call init() to set tag information
        // For test IDs see - https://community.pubmatic.com/x/mQg5AQ#TestandDebugYourIntegration-TestWrapperProfile/Placement
        banner = findViewById(R.id.banner)
        banner?.init(PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, eventHandler)

        //optional listener to listen banner events
        banner?.setListener(POBBannerViewListener())

        // Call loadAd() on banner instance
        banner?.loadAd()


    }

    /**
     * Implementation class to receive Banner ad interaction
     */
    class POBBannerViewListener : POBBannerView.POBBannerViewListener(){
        val TAG = "POBBannerViewListener"

        // Callback method Notifies that an  banner ad has been successfully loaded and rendered.
        override fun onAdReceived(view: POBBannerView?) {
            Log.d(TAG, "onAdReceived")
        }

        // Callback method Notifies an error encountered while loading or rendering an ad.
        override fun onAdFailed(view: POBBannerView?, error: POBError?) {
            Log.d(TAG, "onAdFailed")
        }

        // Callback method notifies ad click
        override fun onAdClick(view: POBBannerView?) {
            Log.d(TAG, "onAdClick")
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
