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
package com.pubmatic.openwrap.listapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.pubmatic.openwrap.listapp.adloader.AdLoader
import com.pubmatic.openwrap.listapp.adloader.AdLoaderEvent
import com.pubmatic.openwrap.listapp.adloader.AdLoaderListener
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.openwrap.banner.POBBannerView

/**
 * Class definition demonstrate recycler adapter to show feed list
 */
class FeedListAdapter(val feedItems: ArrayList<FeedItem>): RecyclerView.Adapter<ViewHolder>() {

    companion object{
        val TAG: String = "FeedListAdapter"
    }

    private var onBannerBindListener: OnBannerBindListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create view holder according to feed type
        if(viewType == FeedItem.FeedType.BANNER.ordinal){
            val view = (LayoutInflater.from(parent.context).
                inflate(R.layout.item_banner_feed, parent, false) as? LinearLayout)
                ?: LinearLayout(parent.context)
            return BannerViewHolder(view)
        }else {
            val view = (LayoutInflater.from(parent.context).
                inflate(R.layout.item_news_feed, parent, false) as? LinearLayout)
                ?: LinearLayout(parent.context)
            return NewsViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return feedItems.get(position).feedType.ordinal
    }

    override fun getItemCount(): Int {
        return feedItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedItem = feedItems[position]
        if(holder is BannerViewHolder){
            Log.d(AdLoader.TAG, "position ${position+1} onBindViewHolder position: ${position+1}")

            // Get banner from feed item
            val adLoader = feedItem.adLoader

            // Set adLoader listener if not available
            if(adLoader?.listener == null){
                setAdLoaderListener(holder, adLoader, position)
            }

            // Notify banner is getting bind
            onBannerBindListener?.onBannerBind(feedItem)

            // If you are scrolling so fast sometime remove view takes time before that you can not
            // add before remove it parent, hence exception occurs
            try {
                // Attach banner to view holder view
                adLoader?.banner?.let {
                    Log.d(TAG, "position ${position+1} Banner instance attaching : ${it.hashCode()}")
                    feedItem.view?.let {
                        holder.displayAdView(it)
                    }
                }
            }catch (e: IllegalStateException){
                Log.e(TAG, "position ${position+1} Not able to render banner: "+e.message)
            }
        }else if(holder is NewsViewHolder){
            holder.textView.text = feedItem.title
        }
    }

    /**
     * Setter to provide banner is getting bind on
     */
    fun setBannerLoadedListener(listenerOn: OnBannerBindListener){
        onBannerBindListener = listenerOn
    }

    // Load and update banner dimension on AdLoader receive callback
    private fun setAdLoaderListener(adholder: BannerViewHolder, adLoader: AdLoaderEvent?, position: Int){
        Log.d(TAG, "position ${position+1} setBannerListener $adLoader")
        adLoader?.listener = object : AdLoaderListener{

            override fun onAdReceived(view: POBBannerView) {
                Log.d(TAG, "position ${position+1} on Ad Received $adLoader")
                // Update feed item view with banner view
                feedItems.get(position).view = view
                adholder.displayAdView(view)
            }

            override fun onNativeAdReceived(nativeAd: NativeAd) {
                Log.d(TAG, "position ${position+1} on Native Ad Received $adLoader")
                val styles = NativeTemplateStyle.Builder().build()
                val templateView: TemplateView  = LayoutInflater.from(adholder.container.context).
                inflate(R.layout.layout_native_ad_template, adholder.container, false) as TemplateView
                templateView.setStyles(styles)
                templateView.setNativeAd(nativeAd)
                // Update feed item view with template view
                feedItems.get(position).view = templateView
                adholder.displayAdView(templateView)
            }

            override fun onCustomNativeAdReceived(customNative: NativeCustomFormatAd) {
                Log.d(TAG, "position ${position+1} on Custom Native Ad Received $adLoader")
                val customNativeAd = LayoutInflater.from(adholder.container.context).
                inflate(R.layout.layout_custom_native, adholder.container, false)

                // Update feed item view with custom native views
                feedItems.get(position).view = customNativeAd

                renderCustomNative(customNativeAd, customNative)

                // Create cusotm native view from inflater
                adholder.displayAdView(customNativeAd)

                // Since this is custom native ad format, app is responsible for recording impressions and
                // reporting click events to the Google Mobile Ads SDK.
                customNative.recordImpression()
            }

            override fun onAdFailed(error: POBError) {
                Log.d(TAG, "position ${position+1} Unable to load ad, Error: ${error.errorMessage}")
            }
        }
    }

    /**
     * Populates and render's custom native ad
     */
    fun renderCustomNative(adView: View, nativeCustomFormatAd: NativeCustomFormatAd) {
        val imageView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val image = nativeCustomFormatAd.getImage("MainImage")
        if (image != null) {
            imageView.setImageDrawable(image.drawable)
        }
        var textView = adView.findViewById<TextView>(R.id.ad_headline)
        textView.text = nativeCustomFormatAd.getText("Title")
        textView = adView.findViewById(R.id.ad_description)
        textView.text = nativeCustomFormatAd.getText("description")
        val button = adView.findViewById<Button>(R.id.ad_button)
        val clickThroughAssetName = "ClickThroughText"
        button.text = nativeCustomFormatAd.getText(clickThroughAssetName)
        button.setOnClickListener(View.OnClickListener {
            nativeCustomFormatAd.performClick(clickThroughAssetName)
        })
    }

    /**
     * Interface definition to notify banner is getting bind to view holder
     */
    interface OnBannerBindListener{
        /**
         * Notifies banner is about to get bind to view holder
         */
        fun onBannerBind(feedItem : FeedItem)
    }

    /**
     * View holder class to manager news
     */
    class NewsViewHolder(view: View): ViewHolder(view){
        var textView: TextView = view.findViewById(R.id.news_title)
    }

    /**
     * View holder class to manager Banner
     */
    class BannerViewHolder(view: View): ViewHolder(view) {
        var container : LinearLayout = view.findViewById(R.id.container)
        /**
         * It display's one of the native, custom native and banner ad
         */
        fun displayAdView (view : View) {
            container.removeAllViews()
            container.addView(view)
        }
    }
}