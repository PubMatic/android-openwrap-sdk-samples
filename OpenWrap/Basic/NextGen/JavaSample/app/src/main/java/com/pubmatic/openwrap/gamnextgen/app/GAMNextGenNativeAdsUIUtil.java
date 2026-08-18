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

package com.pubmatic.openwrap.gamnextgen.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.libraries.ads.mobile.sdk.common.Image;
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd;
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd;
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView;

/**
 * Utility to render GAM Next-Gen {@link NativeAd} and {@link CustomNativeAd} into sample app layouts.
 */
public final class GAMNextGenNativeAdsUIUtil {

    private static final String CUSTOM_NATIVE_TITLE_ASSET = "Title";
    private static final String CUSTOM_NATIVE_DESCRIPTION_ASSET = "description";
    private static final String CUSTOM_NATIVE_ICON_ASSET = "Icon";
    private static final String CUSTOM_NATIVE_MAIN_IMAGE_ASSET = "MainImage";
    private static final String CUSTOM_NATIVE_CLICK_THROUGH_ASSET = "ClickThroughText";

    private GAMNextGenNativeAdsUIUtil() {
    }

    public static void renderNativeAd(
            NativeAd nativeAd,
            ViewGroup container,
            LayoutInflater layoutInflater
    ) {
        NativeAdView adView = (NativeAdView) layoutInflater.inflate(R.layout.ad_unified, container, false);
        populateNativeAdView(nativeAd, adView);
        container.removeAllViews();
        container.addView(adView);
    }

    public static void renderCustomNativeAd(
            CustomNativeAd customNativeAd,
            ViewGroup container,
            LayoutInflater layoutInflater
    ) {
        View adView = layoutInflater.inflate(R.layout.ad_unified, container, false);
        populateCustomNativeTemplate(customNativeAd, adView);
        container.removeAllViews();
        container.addView(adView);
        customNativeAd.recordImpression();
    }

    private static void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        TextView headlineView = adView.findViewById(R.id.ad_title);
        TextView bodyView = adView.findViewById(R.id.ad_body);
        Button callToActionView = adView.findViewById(R.id.ad_click_to_action);
        ImageView iconView = adView.findViewById(R.id.ad_icon_image);
        RatingBar starRatingView = adView.findViewById(R.id.ad_ratingbar);

        adView.setHeadlineView(headlineView);
        adView.setBodyView(bodyView);
        adView.setCallToActionView(callToActionView);
        adView.setIconView(iconView);
        adView.setStarRatingView(starRatingView);

        hideOrBindText(headlineView, nativeAd.getHeadline());
        hideOrBindText(bodyView, nativeAd.getBody());
        hideOrBindText(callToActionView, nativeAd.getCallToAction());

        Image icon = nativeAd.getIcon();
        if (icon == null || icon.getDrawable() == null) {
            iconView.setVisibility(View.GONE);
        } else {
            iconView.setVisibility(View.VISIBLE);
            iconView.setImageDrawable(icon.getDrawable());
        }

        Double starRating = nativeAd.getStarRating();
        if (starRating == null) {
            starRatingView.setVisibility(View.GONE);
        } else {
            starRatingView.setVisibility(View.VISIBLE);
            starRatingView.setRating(starRating.floatValue());
        }

        adView.registerNativeAd(nativeAd, mediaView);
    }

    private static void populateCustomNativeTemplate(CustomNativeAd customNativeAd, View adView) {
        TextView titleTextView = adView.findViewById(R.id.ad_title);
        TextView descriptionTextView = adView.findViewById(R.id.ad_body);
        hideOrBindText(titleTextView, customNativeAd.getText(CUSTOM_NATIVE_TITLE_ASSET));
        titleTextView.setOnClickListener(v -> customNativeAd.performClick(CUSTOM_NATIVE_TITLE_ASSET));

        hideOrBindText(descriptionTextView, customNativeAd.getText(CUSTOM_NATIVE_DESCRIPTION_ASSET));

        ImageView iconImage = adView.findViewById(R.id.ad_icon_image);
        Image iconNativeImage = customNativeAd.getImage(CUSTOM_NATIVE_ICON_ASSET);
        if (iconNativeImage != null && iconNativeImage.getDrawable() != null) {
            iconImage.setImageDrawable(iconNativeImage.getDrawable());
            iconImage.setOnClickListener(v -> customNativeAd.performClick(CUSTOM_NATIVE_ICON_ASSET));
        }

        ImageView mainImage = adView.findViewById(R.id.ad_main_image);
        Image mainImageNativeImage = customNativeAd.getImage(CUSTOM_NATIVE_MAIN_IMAGE_ASSET);
        if (mainImageNativeImage != null && mainImageNativeImage.getDrawable() != null) {
            mainImage.setVisibility(View.VISIBLE);
            mainImage.setImageDrawable(mainImageNativeImage.getDrawable());
            mainImage.setOnClickListener(v -> customNativeAd.performClick(CUSTOM_NATIVE_MAIN_IMAGE_ASSET));
        } else {
            mainImage.setVisibility(View.GONE);
        }

        adView.findViewById(R.id.ad_media).setVisibility(View.GONE);

        Button clickToActionButton = adView.findViewById(R.id.ad_click_to_action);
        hideOrBindText(clickToActionButton, customNativeAd.getText(CUSTOM_NATIVE_CLICK_THROUGH_ASSET));
        clickToActionButton.setOnClickListener(v -> customNativeAd.performClick(CUSTOM_NATIVE_CLICK_THROUGH_ASSET));
        adView.findViewById(R.id.ad_ratingbar).setVisibility(View.INVISIBLE);
    }

    private static void hideOrBindText(TextView textView, CharSequence text) {
        if (text == null || text.length() == 0) {
            textView.setVisibility(View.INVISIBLE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }
}
