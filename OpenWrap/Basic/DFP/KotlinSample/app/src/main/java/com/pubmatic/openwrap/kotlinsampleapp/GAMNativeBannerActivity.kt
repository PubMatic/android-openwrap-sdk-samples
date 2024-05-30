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

package com.pubmatic.openwrap.kotlinsampleapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.common.utility.POBUtils
import com.pubmatic.sdk.openwrap.banner.POBBannerView
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeBannerEventHandler
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeBannerEventHandler.NativeAdListener
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeBannerEventHandler.NativeCustomFormatAdListener
import kotlinx.android.synthetic.main.activity_banner.*
import java.net.MalformedURLException
import java.net.URL

class GAMNativeBannerActivity : AppCompatActivity() {

    private val OPENWRAP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-NativeAndBanner"
    private val PUB_ID = "156276"
    private val PROFILE_ID = 1165
    private val DFP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-NativeAndBanner"
    private val CUSTOM_NATIVE_FORMAT = "12051535"

    private var banner: POBBannerView? = null
    private var container: LinearLayout? = null
    private var template: TemplateView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_banner)
        container = findViewById(R.id.container)
        template = findViewById(R.id.my_template)
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
        // For example, The code below creates an event handler for DFP ad server.
        val gamNativeBannerEventHandler = GAMNativeBannerEventHandler(this, DFP_AD_UNIT_ID, AdSize.MEDIUM_RECTANGLE)

        // Prepares handler to request GAM's NativeAd
        gamNativeBannerEventHandler.configureNativeAd(POBNativeListener())

        // Prepares handler to request GAM's NativeCustomFormatAd
        gamNativeBannerEventHandler.configureNativeCustomFormatAd(CUSTOM_NATIVE_FORMAT, POBCustomNativeListener(), null)
        
        // Initialise banner view
        banner = POBBannerView(this, PUB_ID, PROFILE_ID, OPENWRAP_AD_UNIT_ID, gamNativeBannerEventHandler)

        //optional listener to listen banner events
        banner?.setListener(POBBannerViewListener())

        // Call loadAd() on banner instance
        banner?.loadAd()
    }

    fun renderNativeAd(nativeAd: NativeAd) {
        val styles = NativeTemplateStyle.Builder().build()
        template?.setStyles(styles)
        template?.setNativeAd(nativeAd)
        template?.visibility = View.VISIBLE
        container?.removeAllViews()
        container?.visibility = View.GONE
    }

    fun renderCustomNative(nativeCustomFormatAd: NativeCustomFormatAd) {
        val width = POBUtils.convertDpToPixel(300)
        val height = POBUtils.convertDpToPixel(250)
        val layoutParams = FrameLayout.LayoutParams(width, height)
        val frameLayout = FrameLayout(this)
        val adView = layoutInflater
            .inflate(R.layout.layout_custom_native, null) as RelativeLayout
        val imageView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val mainImageAssetName = "MainImage"
        val image = nativeCustomFormatAd.getImage(mainImageAssetName)
        if (image != null) {
            imageView.setImageDrawable(image.drawable)
        }

        // Perform click when custom native asset click happens
        imageView.setOnClickListener(View.OnClickListener {
            nativeCustomFormatAd.performClick(mainImageAssetName)
        })

        val headLineTextView = adView.findViewById<TextView>(R.id.ad_headline)

        val titleAssetName = "Title"
        headLineTextView.text = nativeCustomFormatAd.getText(titleAssetName)
        // Perform click when custom native asset click happens
        headLineTextView.setOnClickListener { nativeCustomFormatAd.performClick(titleAssetName) }

        val descriptionAssetName = "description"
        val descriptionTextView = adView.findViewById<TextView>(R.id.ad_description)
        descriptionTextView.text = nativeCustomFormatAd.getText(descriptionAssetName)

        // Perform click when custom native asset click happens
        descriptionTextView.setOnClickListener { nativeCustomFormatAd.performClick(descriptionAssetName) }

        val button = adView.findViewById<Button>(R.id.ad_button)
        val clickThroughAssetName = "ClickThroughText"
        button.text = nativeCustomFormatAd.getText(clickThroughAssetName)
        button.setOnClickListener{ nativeCustomFormatAd.performClick(clickThroughAssetName) }
        frameLayout.removeAllViews()
        frameLayout.addView(adView)
        container?.removeAllViews()
        container?.addView(frameLayout, layoutParams)
        template?.visibility = View.GONE
        container?.visibility = View.VISIBLE
    }

    /**
     * POBBannerView Ad listener callbacks
     */
    inner class POBBannerViewListener : POBBannerView.POBBannerViewListener(){
        val TAG = "POBBannerViewListener"

        // Callback method Notifies that a banner ad has been successfully loaded and rendered.
        override fun onAdReceived(view: POBBannerView) {
            Log.d(TAG, "onAdReceived")
            template?.visibility = View.GONE
            container?.visibility = View.VISIBLE
            val adSize = view.creativeSize
            if (adSize != null) {
                Log.d(TAG, "Banner : Ad Received with size {$adSize}")
                var width = adSize.adWidth
                var height = adSize.adHeight
                width = POBUtils.convertDpToPixel(width)
                height = POBUtils.convertDpToPixel(height)
                val layoutParams: ViewGroup.LayoutParams = LinearLayout.LayoutParams(width, height)
                container?.removeAllViews()
                container?.addView(view, layoutParams)
            }
        }


        // Callback method Notifies an error encountered while loading or rendering an ad.
        override fun onAdFailed(view: POBBannerView, error: POBError) {
            Log.e(TAG, "onAdFailed : Ad failed with error - $error")
        }


        // Callback method Notifies whenever current app goes in the background due to user click
        override fun onAppLeaving(view: POBBannerView) {
            Log.d(TAG, "onAppLeaving")
        }


        // Callback method Notifies that the  banner ad will launch a dialog on top of the current view
        override fun onAdOpened(view: POBBannerView) {
            Log.d(TAG, "onAdOpened")
        }

        // Callback method Notifies that the banner ad view is clicked.
        override fun onAdClicked(view: POBBannerView) {
            Log.d(TAG, "onAdClicked")
        }

        // Callback method Notifies that the  banner ad has dismissed the modal on top of the current view
        override fun onAdClosed(view: POBBannerView) {
            Log.d(TAG, "onAdClosed")
        }

    }

    /**
     * GAM Native Ad listener callbacks
     */
    inner class POBNativeListener : NativeAdListener() {
        private val TAG = "POBNativeListener"

        // Callback method notifies that GAM native ad has been successfully loaded.
        override fun onAdReceived(nativeAd: NativeAd) {
            Log.d(TAG, "Native Ad Received")
            renderNativeAd(nativeAd)
        }

        // Callback method notifies whenever GAM native ad has been clicked
        override fun onAdClicked(nativeAd: NativeAd) {
            Log.d(TAG, "Native Ad Clicked")
        }

        // Callback method notifies about GAM native ad impression occurred
        override fun onAdImpression(nativeAd: NativeAd) {
            Log.d(TAG, "Native Ad Impression")
        }

        // Callback method notifies that the GAM native ad will launch a dialog on top of the current view
        override fun onAdOpened(nativeAd: NativeAd) {
            Log.d(TAG, "Native Ad Opened")
        }

        // Callback method notifies that the  banner ad has dismissed the modal on top of the current view
        override fun onAdClosed(nativeAd: NativeAd) {
            Log.d(TAG, "Native Ad Closed")
        }
    }

    /**
     * GAM Custom Native Ad listener callbacks
     */
    inner class POBCustomNativeListener : NativeCustomFormatAdListener() {

        private val TAG = "POBCustomNativeListener"

        // Callback method notifies that GAM custom native ad has been successfully loaded.
        override fun onAdReceived(nativeCustomFormatAd: NativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Received")
            renderCustomNative(nativeCustomFormatAd)
            // Since this is custom native ad format, app is responsible for recording impressions and
            // reporting click events to the Google Mobile Ads SDK.
            nativeCustomFormatAd.recordImpression()
        }

        // Callback method notifies whenever GAM native ad has been clicked
        override fun onAdClicked(nativeCustomFormatAd: NativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Clicked")
        }

        // Callback method notifies about GAM native ad impression occurred
        override fun onAdImpression(nativeCustomFormatAd: NativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Impression")
        }

        // Callback method notifies that the GAM custom native ad will launch a dialog on top of the current view
        override fun onAdOpened(nativeCustomFormatAd: NativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Opened")
        }

        // Callback method notifies that the  banner ad has dismissed the modal on top of the current view
        override fun onAdClosed(nativeCustomFormatAd: NativeCustomFormatAd) {
            Log.d(TAG, "Custom Native Ad Closed")
        }

    }


    override fun onDestroy() {
        // destroy banner before onDestroy of Activity lifeCycle
        super.onDestroy()
        banner?.destroy()
    }

}
