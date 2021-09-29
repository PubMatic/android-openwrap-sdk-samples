package com.pubmatic.openwrap.listapp.adloader

import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.openwrap.banner.POBBannerView

/**
 * Interface definition, to provide ad loading callbacks to user class.
 */
interface AdLoaderListener{
    /**
     * Gets called when ad is received successfully.
     * @param view the banner view
     */
    fun onAdReceived(view: POBBannerView)

    /**
     * Gets called when ad is received successfully.
     * @param nativeAd the banner view
     */
    fun onNativeAdReceived(nativeAd: NativeAd)

    /**
     * Gets called when ad is received successfully.
     * @param customNative the banner view
     */
    fun onCustomNativeAdReceived(customNative: NativeCustomFormatAd)

    /**
     * Gets called when ad loader failed to receive ad
     * @param error the specific error which includes error code and its reason
     */
    fun onAdFailed(error: POBError)
}