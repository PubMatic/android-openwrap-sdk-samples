/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2021 PubMatic, All Rights Reserved.
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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pubmatic.sdk.common.POBError
import com.pubmatic.sdk.common.utility.POBUtils
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
        val feedItem = feedItems.get(position)
        if(holder is BannerViewHolder){
            val parent: LinearLayout = (holder.itemView as? LinearLayout)?: LinearLayout(holder.itemView.context)
            // Notify banner is getting bind
            onBannerBindListener?.onBannerBind()

            // Get banner from feed item
            val banner = feedItem.banner

            // Remove child banner view
            parent.removeAllViews()

            // If you are scrolling so fast sometime remove view takes time before that you can not
            // add before remove it parent, hence exception occurs
            try {
                // Attach banner to view holder view
                parent.addView(banner)
            }catch (e: IllegalStateException){
                Log.e(TAG, "Not able to render banner: "+e.message)
            }


            // Set banner listener
            setBannerListener(banner)

            // In case if banner is already loaded, view holder is recycled,
            // set banner dimension explicitly
            banner?.let{
                updateBannerDimensions(it)
            }

        }else if(holder is NewsViewHolder){
            holder.textView.setText(feedItem.title)
        }
    }

    /**
     * Setter to provide banner is getting bind on
     */
    fun setBannerLoadedListener(listenerOn: OnBannerBindListener){
        onBannerBindListener = listenerOn
    }

    // Load and update banner dimension on banner receive callback
    private fun setBannerListener(banner: POBBannerView?){
        banner?.setListener(object : POBBannerView.POBBannerViewListener(){
            override fun onAdReceived(view: POBBannerView) {
                updateBannerDimensions(view)
            }

            override fun onAdFailed(p0: POBBannerView, p1: POBError) {
                Log.d(TAG, "Unable to load ad, Error: "+p1.errorMessage)
            }
        })
    }



    private fun updateBannerDimensions(banner: POBBannerView){
        // Get ad size from banner
        val adSize = banner.creativeSize
        adSize?.let {
            val width: Int = POBUtils.convertDpToPixel(adSize.adWidth)
            val height: Int = POBUtils.convertDpToPixel(adSize.adHeight)
            // Create layout params which is required set banner dimensions
            val layoutParams = LinearLayout.LayoutParams(width, height)
            banner.layoutParams = layoutParams
        }
    }

    /**
     * Interface definition to notify banner is getting bind to view holder
     */
    interface OnBannerBindListener{
        /**
         * Notifies banner is about to get bind to view holder
         */
        fun onBannerBind()
    }

    /**
     * View holder class to manager news
     */
    class NewsViewHolder(view: View): ViewHolder(view){
        var textView: TextView
        init {
            textView = view.findViewById(R.id.news_title)
        }
    }

    /**
     * View holder class to manager Banner
     */
    class BannerViewHolder(view: View): ViewHolder(view)
}