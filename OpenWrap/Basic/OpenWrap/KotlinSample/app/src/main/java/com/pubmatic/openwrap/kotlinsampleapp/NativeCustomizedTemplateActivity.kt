/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2023 PubMatic, All Rights Reserved.
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
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.nativead.POBNativeAd
import com.pubmatic.sdk.nativead.POBNativeAdListener
import com.pubmatic.sdk.nativead.POBNativeAdLoader
import com.pubmatic.sdk.nativead.POBNativeAdLoaderListener
import com.pubmatic.sdk.nativead.datatype.POBNativeTemplateType
import com.pubmatic.sdk.nativead.views.POBNativeAdMediumTemplateView

import java.net.MalformedURLException
import java.net.URL

/**
 * Class representing Native standard custom template.
 */
class NativeCustomizedTemplateActivity : AppCompatActivity() {

    private var nativeAd: POBNativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native)
        OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.All)

        // A valid Play Store Url of an Android app. Required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.storeURL = URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo)

        //Create native ad loader to make request to openWrap
        val nativeAdLoader = POBNativeAdLoader(this, PUB_ID, PROFILE_ID,
            OPENWRAP_AD_UNIT_ID, POBNativeTemplateType.MEDIUM)

        //Set the ad loader listener to listens the ad received and ad failed to load callback
        nativeAdLoader.setAdLoaderListener(NativeAdLoaderListenerImpl())

        nativeAdLoader.loadAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd?.destroy()
    }

    companion object {
        private const val TAG = "NativeCustomStandard"
        private const val PUB_ID = "156276"
        private const val PROFILE_ID = 1165
        private const val OPENWRAP_AD_UNIT_ID = "OpenWrapNativeAdUnit"
    }

    /**
     * Listener to get callback for ad received and ad failed.
     */
    inner class NativeAdLoaderListenerImpl : POBNativeAdLoaderListener {
        override fun onAdReceived(nativeAdLoader: POBNativeAdLoader, nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Received")

            //Caching nativeAd instance to destroy it when activity get destroyed
            this@NativeCustomizedTemplateActivity.nativeAd = nativeAd
            
            //region Creating custom standard view
            val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

            //provide your xml custom template
            val adview = inflater.inflate(R.layout.custom_medium_template, null) as POBNativeAdMediumTemplateView

            //Set the reference of asset views your inflated adView
            val mainImage = adview.findViewById<ImageView>(R.id.main_image)
            adview.mainImage = mainImage

            val title = adview.findViewById<TextView>(R.id.title)
            adview.title = title

            val description = adview.findViewById<TextView>(R.id.description)
            adview.description = description

            val imageView = adview.findViewById<ImageView>(R.id.icon_image)
            adview.iconImage = imageView

            val cta = adview.findViewById<Button>(R.id.cta_text)
            adview.cta = cta

            val privacyIcon = adview.findViewById<ImageView>(R.id.privacy_icon)
            adview.privacyIcon = privacyIcon

            //Set the layout params to the ad view with width x height as of the parent of inflated xml
            val layoutParams = FrameLayout.LayoutParams(
                resources.getDimension(R.dimen.pob_dimen_300dp).toInt(),
                resources.getDimension(R.dimen.pob_dimen_250dp).toInt())
            adview.layoutParams = layoutParams

            //Set the native ad listener to listen the event callback and also get rendered native ad view
            nativeAd.renderAd(adview, NativeAdListenerImpl())
        }

        override fun onFailedToLoad(nativeAdLoader: POBNativeAdLoader, error: POBError) {
            Log.e(TAG, error.toString())
        }
    }

    /**
     * Listener to get callback for rendered native ad view and native events.
     */
    inner class NativeAdListenerImpl : POBNativeAdListener {
        override fun onNativeAdRendered(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Rendered")
            //Add the received rendered native ad view in your container
            val container = findViewById<FrameLayout>(R.id.container)
            container.addView(nativeAd.adView)
        }

        override fun onNativeAdRenderingFailed(nativeAd: POBNativeAd, error: POBError) {
            Log.e(TAG, error.toString())
        }

        override fun onNativeAdImpression(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad recorded Impression")
        }

        override fun onNativeAdClicked(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Clicked")
        }

        override fun onNativeAdClicked(nativeAd: POBNativeAd, assetId: String) {
            Log.d(TAG, "Ad clicked for asset id - $assetId")
        }

        override fun onNativeAdLeavingApplication(nativeAd: POBNativeAd) {
            Log.d(TAG, "App Leaving")
        }

        override fun onNativeAdOpened(nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Opened")
        }

        override fun onNativeAdClosed(nativeAd: POBNativeAd) {
            Log.d(TAG, "App Closed")
        }
    }
}