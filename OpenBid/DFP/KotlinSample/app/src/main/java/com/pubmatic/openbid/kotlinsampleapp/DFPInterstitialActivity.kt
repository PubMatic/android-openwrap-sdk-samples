package com.pubmatic.openbid.kotlinsampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.pubmatic.openbid.kotlinsampleapp.dfpevent.DFPInterstitialEventHandler
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.OpenBidSDK
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.openbid.core.POBRequest
import com.pubmatic.sdk.openbid.interstitial.POBInterstitial
import kotlinx.android.synthetic.main.activity_interstitial.*
import java.net.MalformedURLException
import java.net.URL

class DFPInterstitialActivity : AppCompatActivity() {

    private val OPEN_BID_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-Interstitial"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1165
    private val DFP_AD_UNIT = "/15671365/pm_sdk/PMSDK-Demo-App-Interstitial"
    private var interstitial : POBInterstitial? = null
    private var loadAd: Button? = null;
    private var showAd: Button? = null;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial)
        setSupportActionBar(toolbar)

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

        // Create an interstitial custom event handler for your ad server. Make sure
        // you use separate event handler objects to create each interstitial ad instance.
        // For example, The code below creates an event handler for DFP ad server.
        val eventHandler = DFPInterstitialEventHandler(this, DFP_AD_UNIT)

        // Initialise interstitial ad
        interstitial = POBInterstitial(this, PUB_ID, PROFILE_ID, OPEN_BID_AD_UNIT_ID, eventHandler)

        // Set optional listener
        interstitial?.setListener(POBInterstitialListener())

        var request: POBRequest? = interstitial?.getAdRequest()
        request?.enableDebugState(true)


        loadAd = findViewById(R.id.load_ad)
        loadAd?.setOnClickListener {
                // Call loadAd on interstitial
                 interstitial?.loadAd()

        }

        showAd = findViewById(R.id.show_ad)
        showAd?.setEnabled(false)
        showAd?.setOnClickListener {
            // check if the interstitial is ready
            showInterstitialAd()
            showAd?.setEnabled(false)
        }

    }
    /**
     * To show interstitial ad call this method
     **/
    private fun showInterstitialAd(){
        // check if the interstitial is ready
        if(interstitial?.isReady == true){
            // Call show on interstitial
            interstitial?.show()
        }
    }

    // POBInterstitialAdListener listener
    inner class POBInterstitialListener : POBInterstitial.POBInterstitialListener {
        val TAG = "POBInterstitialListener"


        // Callback method notifies that an ad has been received successfully.
        override fun onAdReceived(ad: POBInterstitial?) {
            Log.d(TAG, "onAdReceived")
            //Method gets called when ad gets loaded in container
            //Here, you can show interstitial ad to user
            showAd?.setEnabled(true)
        }

        // Callback method notifies an error encountered while loading or rendering an ad.
        override fun onAdFailed(ad: POBInterstitial?, error: POBError?) {
            Log.d(TAG, "onAdFailed")
            //Method gets called when loadAd fails to load ad
            //Here, you can put logger and see why ad failed to load
        }

        // Callback method notifies that a user interaction will open another app (for example, App Store), leaving the current app.
        override fun onAppLeaving(ad: POBInterstitial?) {
            Log.d(TAG, "onAppLeaving")
        }

        // Callback method notifies that the interstitial ad will be presented as a modal on top of the current view.
        override fun onAdOpened(ad: POBInterstitial?) {
            Log.d(TAG, "onAdOpened")
        }

        // Callback method notifies that the interstitial ad has been animated off the screen.
        override fun onAdClosed(ad: POBInterstitial?) {
            Log.d(TAG, "onAdClosed")
        }

        // Callback method notifies ad click
        override fun onAdClicked(ad: POBInterstitial?) {
            Log.d(TAG, "onAdClicked")
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        // destroy interstitial
        interstitial?.destroy()
    }
}
