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

package com.pubmatic.openwrap.kotlinsampleapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.pubmatic.sdk.common.OpenWrapSDK
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.models.POBApplicationInfo
import com.pubmatic.sdk.common.utility.POBUtils
import com.pubmatic.sdk.nativead.POBNativeAd
import com.pubmatic.sdk.nativead.POBNativeAdListener
import com.pubmatic.sdk.nativead.POBNativeAdLoader
import com.pubmatic.sdk.nativead.POBNativeAdLoaderListener
import com.pubmatic.sdk.nativead.datatype.POBNativeTemplateType
import com.pubmatic.sdk.nativead.views.POBNativeAdMediumTemplateView
import com.pubmatic.sdk.nativead.views.POBNativeTemplateView
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeConfiguration
import com.pubmatic.sdk.openwrap.eventhandler.dfp.GAMNativeEventHandler
import java.net.MalformedURLException
import java.net.URL

/**
 * Class representing GAM NativeCustomFormatAd with OpenWrap Native Standard custom template ad.
 */
class GAMNativeCustomizedTemplateActivity : AppCompatActivity() {

    private lateinit var nativeAdLoader: POBNativeAdLoader

    private var nativeAd: POBNativeAd? = null

    private lateinit var renderAd: Button

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_custom)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.title_activity_native_customized_template)

        val loadAd = findViewById<Button>(R.id.load_ad)
        renderAd = findViewById(R.id.render_ad)
        container = findViewById(R.id.container)

        loadAd.setOnClickListener {
            // Load the native ad
            nativeAd = null
            container.removeAllViews()
            renderAd.setEnabled(false)
            nativeAdLoader.loadAd()
        }

        renderAd.setOnClickListener{
            // Set the native ad listener to listen the event callback and also to receive the
            // rendered native ad view.
            nativeAd?.renderAd(
                getNativeTemplateView(),
                NativeAdListenerImpl()
            )
        }

        OpenWrapSDK.setLogLevel(OpenWrapSDK.LogLevel.All)

        // A valid Play Store Url of an Android app. Required.
        val appInfo = POBApplicationInfo()
        try {
            appInfo.storeURL =
                URL("https://play.google.com/store/apps/details?id=com.example.android&hl=en")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        // This app information is a global configuration & you
        // Need not set this for every ad request(of any ad type)
        OpenWrapSDK.setApplicationInfo(appInfo)

        // Create nativeEventHandler to request ad from your GAM Ad Server
        val nativeEventHandler = GAMNativeEventHandler(
            this, DFP_AD_UNIT_ID,
            OPENWRAP_CUSTOM_FORMAT_ID, GAMNativeConfiguration.GAMAdTypes.NativeCustomFormatAd
        )

        // This step is optional and you can the set the Custom Format Id List here
        nativeEventHandler.addNativeCustomFormatAd(GAM_NATIVE_CUSTOM_FORMAT_ID, null)

        // Set the rendering listener for GAM Native Ad
        nativeEventHandler.setNativeAdRendererListener(GAMNativeConfiguration.NativeAdRendererListener { nativeAd ->
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val adView = inflater
                .inflate(R.layout.gam_native_ad_template, null) as NativeAdView
            renderNativeAd(nativeAd, adView)
            adView
        })

        nativeEventHandler.setNativeCustomFormatAdRendererListener(GAMNativeConfiguration.NativeCustomFormatAdRendererListener { nativeCustomFormatAd ->
            // Inflate a layout and add it to the parent ViewGroup.
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val adView = inflater.inflate(R.layout.layout_custom_native, null)

            val width = POBUtils.convertDpToPixel(300)
            val height = POBUtils.convertDpToPixel(250)
            val layoutParams = FrameLayout.LayoutParams(width, height)
            adView.layoutParams = layoutParams

            populateNativeCustomAd(nativeCustomFormatAd, adView)

            adView
        })


        // Create nativeAdLoader to request ad from OpenWrap with GAM event handler
        nativeAdLoader = POBNativeAdLoader(
            this@GAMNativeCustomizedTemplateActivity, Constants.PUB_ID, Constants.PROFILE_ID,
            OPENWRAP_AD_UNIT_ID, POBNativeTemplateType.MEDIUM, nativeEventHandler
        )

        //Set the adLoaderListener to listens the callback for ad received or ad failed to load
        nativeAdLoader.setAdLoaderListener(NativeAdLoaderListenerImpl())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up native ad, helps releasing utilized resources.
        nativeAd?.destroy()
    }

    private fun getNativeTemplateView(): POBNativeTemplateView {
        // Create the inflater to inflate your custom template
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Provide your xml custom template
        val adview = inflater.inflate(R.layout.custom_medium_template, null)
                as POBNativeAdMediumTemplateView

        // Set the reference of asset views your inflated adView
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

        val dsaIcon = adview.findViewById<ImageView>(R.id.dsa_icon)
        adview.dsaIcon = dsaIcon

        // Set the layout param as per the width and height for OpenWrap SDK Custom Standard
        // template to fit them properly
        val layoutParams = FrameLayout.LayoutParams(
            resources.getDimension(R.dimen.pob_dimen_300dp).toInt(),
            resources.getDimension(R.dimen.pob_dimen_250dp).toInt()
        )
        adview.layoutParams = layoutParams

        return adview
    }

    companion object {
        private const val TAG = "GAMNativeCustomActivity"

        private const val OPENWRAP_AD_UNIT_ID = "OpenWrapNativeAdUnit"

        private const val DFP_AD_UNIT_ID = "/15671365/pm_sdk/PMSDK-Demo-App-Native"

        private const val OPENWRAP_CUSTOM_FORMAT_ID = "12260425"

        private const val GAM_NATIVE_CUSTOM_FORMAT_ID = "12051535"

        //Asset names required for the rendering of GAM Native Custom format ad

        private const val MAIN_IMAGE_ASSET_NAME = "MainImage"

        private const val TITLE_ASSET_NAME = "Title"

        private const val DESCRIPTION_ASSET_NAME = "description"

        private const val CLICK_THROUGH_ASSET_NAME = "ClickThroughText"
    }

    /**
     * Render the given NativeAd object with inflated NativeAdView
     * @param nativeAd Instance of [NativeAd]
     * @param nativeAdView Instance of [NativeAdView]
     */
    private fun renderNativeAd(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        val styles = NativeTemplateStyle.Builder().build()
        val template = nativeAdView.findViewById<TemplateView>(R.id.my_template)
        template.setStyles(styles)
        template.setNativeAd(nativeAd)
        template.visibility = View.VISIBLE
    }


    /**
     * Populate the nativeCustomFormatAd with inflated adview
     * @param nativeCustomFormatAd Instance of [NativeCustomFormatAd]
     * @param adView Instance of [View]
     */
    private fun populateNativeCustomAd(nativeCustomFormatAd: NativeCustomFormatAd, adView: View) {
        //Populate the inflated custom view with NativeCustomFormatAd object
        val imageView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val image = nativeCustomFormatAd.getImage(MAIN_IMAGE_ASSET_NAME)
        if (image != null) {
            imageView.setImageDrawable(image.drawable)
        }
        imageView.setOnClickListener(View.OnClickListener {
            nativeCustomFormatAd.performClick(MAIN_IMAGE_ASSET_NAME)
        })

        val textView = adView.findViewById<TextView>(R.id.ad_headline)
        textView.text = nativeCustomFormatAd.getText(TITLE_ASSET_NAME)
        textView.setOnClickListener(View.OnClickListener {
            nativeCustomFormatAd.performClick(TITLE_ASSET_NAME)
        })

        val descriptionTextView = adView.findViewById<TextView>(R.id.ad_description)
        descriptionTextView.text =
            nativeCustomFormatAd.getText(DESCRIPTION_ASSET_NAME)
        descriptionTextView.setOnClickListener(View.OnClickListener {
            nativeCustomFormatAd.performClick(DESCRIPTION_ASSET_NAME)
        })

        val button = adView.findViewById<Button>(R.id.ad_button)
        button.text = nativeCustomFormatAd.getText(CLICK_THROUGH_ASSET_NAME)
        button.setOnClickListener(View.OnClickListener {
            nativeCustomFormatAd.performClick(CLICK_THROUGH_ASSET_NAME)
        })
    }

    /**
     * Listener to get callback for ad received and ad failed.
     */
    inner class NativeAdLoaderListenerImpl : POBNativeAdLoaderListener {
        override fun onAdReceived(nativeAdLoader: POBNativeAdLoader, nativeAd: POBNativeAd) {
            Log.d(TAG, "Ad Received")
            // Caching nativeAd instance to call renderAd method and also to destroy it when activity get 
            // destroyed.
            this@GAMNativeCustomizedTemplateActivity.nativeAd = nativeAd
            this@GAMNativeCustomizedTemplateActivity.renderAd.isEnabled = true
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
            Log.d(TAG, "Ad Closed")
        }
    }
}
