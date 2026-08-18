/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2026 PubMatic, All Rights Reserved.
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


package com.pubmatic.openwrap.gamnextgen.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

/**
 * Utility to render GAM Next-Gen [NativeAd] and [CustomNativeAd] into sample app layouts.
 */
object GAMNextGenNativeAdsUIUtil {

    private const val CUSTOM_NATIVE_TITLE_ASSET = "Title"
    private const val CUSTOM_NATIVE_DESCRIPTION_ASSET = "description"
    private const val CUSTOM_NATIVE_ICON_ASSET = "Icon"
    private const val CUSTOM_NATIVE_MAIN_IMAGE_ASSET = "MainImage"
    private const val CUSTOM_NATIVE_CLICK_THROUGH_ASSET = "ClickThroughText"

    fun renderNativeAd(
        nativeAd: NativeAd,
        container: ViewGroup,
        layoutInflater: LayoutInflater,
    ) {
        val adView = layoutInflater.inflate(R.layout.ad_unified, container, false) as NativeAdView
        populateNativeAdView(nativeAd, adView)
        container.removeAllViews()
        container.addView(adView)
    }

    fun renderCustomNativeAd(
        customNativeAd: CustomNativeAd,
        container: ViewGroup,
        layoutInflater: LayoutInflater,
    ) {
        val adView = layoutInflater.inflate(R.layout.ad_unified, container, false)
        populateCustomNativeTemplate(customNativeAd, adView)
        container.removeAllViews()
        container.addView(adView)
        customNativeAd.recordImpression()
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        val headlineView = adView.findViewById<TextView>(R.id.ad_title)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<Button>(R.id.ad_click_to_action)
        val iconView = adView.findViewById<ImageView>(R.id.ad_icon_image)
        val starRatingView = adView.findViewById<RatingBar>(R.id.ad_ratingbar)

        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = callToActionView
        adView.iconView = iconView
        adView.starRatingView = starRatingView

        headlineView.hideOrBindText(nativeAd.headline)
        bodyView.hideOrBindText(nativeAd.body)
        callToActionView.hideOrBindText(nativeAd.callToAction)

        val icon = nativeAd.icon
        if (icon?.drawable == null) {
            iconView.visibility = View.GONE
        } else {
            iconView.visibility = View.VISIBLE
            iconView.setImageDrawable(icon.drawable)
        }

        val starRating = nativeAd.starRating
        if (starRating == null) {
            starRatingView.visibility = View.GONE
        } else {
            starRatingView.visibility = View.VISIBLE
            starRatingView.rating = starRating.toFloat()
        }

        adView.registerNativeAd(nativeAd, mediaView)
    }

    private fun populateCustomNativeTemplate(customNativeAd: CustomNativeAd, adView: View) {
        val titleTextView = adView.findViewById<TextView>(R.id.ad_title)
        val descriptionTextView = adView.findViewById<TextView>(R.id.ad_body)
        titleTextView.hideOrBindText(customNativeAd.getText(CUSTOM_NATIVE_TITLE_ASSET))
        titleTextView.setOnClickListener {
            customNativeAd.performClick(CUSTOM_NATIVE_TITLE_ASSET)
        }

        descriptionTextView.hideOrBindText(customNativeAd.getText(CUSTOM_NATIVE_DESCRIPTION_ASSET))

        val iconImage = adView.findViewById<ImageView>(R.id.ad_icon_image)
        val iconNativeImage = customNativeAd.getImage(CUSTOM_NATIVE_ICON_ASSET)
        if (iconNativeImage?.drawable != null) {
            iconImage.setImageDrawable(iconNativeImage.drawable)
            iconImage.setOnClickListener {
                customNativeAd.performClick(CUSTOM_NATIVE_ICON_ASSET)
            }
        }

        val mainImage = adView.findViewById<ImageView>(R.id.ad_main_image)
        val mainImageNativeImage = customNativeAd.getImage(CUSTOM_NATIVE_MAIN_IMAGE_ASSET)
        if (mainImageNativeImage?.drawable != null) {
            mainImage.visibility = View.VISIBLE
            mainImage.setImageDrawable(mainImageNativeImage.drawable)
            mainImage.setOnClickListener {
                customNativeAd.performClick(CUSTOM_NATIVE_MAIN_IMAGE_ASSET)
            }
        } else {
            mainImage.visibility = View.GONE
        }

        adView.findViewById<MediaView>(R.id.ad_media).visibility = View.GONE

        val clickToActionButton = adView.findViewById<Button>(R.id.ad_click_to_action)
        clickToActionButton.hideOrBindText(customNativeAd.getText(CUSTOM_NATIVE_CLICK_THROUGH_ASSET))
        clickToActionButton.setOnClickListener {
            customNativeAd.performClick(CUSTOM_NATIVE_CLICK_THROUGH_ASSET)
        }
        adView.findViewById<RatingBar>(R.id.ad_ratingbar).visibility = View.INVISIBLE
    }

    private fun TextView.hideOrBindText(text: CharSequence?) {
        if (text.isNullOrEmpty()) {
            visibility = View.INVISIBLE
        } else {
            visibility = View.VISIBLE
            this.text = text
        }
    }
}
