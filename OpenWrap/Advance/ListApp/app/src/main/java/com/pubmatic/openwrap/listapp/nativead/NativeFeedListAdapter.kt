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

package com.pubmatic.openwrap.listapp.nativead

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.pubmatic.openwrap.listapp.R

/**
 * Adapter to display the native ad in recycler view
 */
class NativeFeedListAdapter(private var nativeAdFeedItems: List<NativeFeedItem>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Listener to give call back to the NativeListActivity on the bind of the ad to prefetch the next native ad
     */
    private var onNativeAdBindListener: OnNativeAdBindListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == NativeFeedItem.FeedType.NATIVE.ordinal){
            val view = (LayoutInflater.from(parent.context).
            inflate(R.layout.item_ad_feed, parent, false) as? LinearLayout)
                ?: LinearLayout(parent.context)
            NativeViewHolder(view)
        }else {
            val view = (LayoutInflater.from(parent.context).
            inflate(R.layout.item_native_feed, parent, false) as? LinearLayout)
                ?: LinearLayout(parent.context)
            FeedItemViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return nativeAdFeedItems[position].feedType.ordinal
    }

    override fun getItemCount(): Int {
        return nativeAdFeedItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val feedItem = nativeAdFeedItems[position]
        if(holder is NativeViewHolder){
            val parent: LinearLayout = (holder.itemView as? LinearLayout)?: LinearLayout(holder.itemView.context)
            // Render the stored nativeAd and attach the received view to the parent view
            // Before you attach adview, remove earlier attach view and adview from parent.
            parent.removeAllViews()
            feedItem.nativeAd?.adView?.let{
                val adParent: ViewGroup? = it.parent as ViewGroup?
                adParent?.removeView(it)
                parent.addView(it)
            }
            onNativeAdBindListener?.onNativeAdBind(position)
        }
    }

    /**
     * Setter for the listener to give callback on the bind of the native ad to fetch the next native ad
     */
    fun setNativeAdLoadedListener(listenerOn: OnNativeAdBindListener){
        onNativeAdBindListener = listenerOn
    }

    /**
     * Interface to communicate the binding of the native ad view to the NativeListActivity
     */
    interface OnNativeAdBindListener{
        fun onNativeAdBind(position: Int)
    }

    /**
     * View holder class to manager Placement view
     */
    class FeedItemViewHolder(view: View): RecyclerView.ViewHolder(view)

    /**
     * View holder class to manager NativeAd view
     */
    class NativeViewHolder(view: View): RecyclerView.ViewHolder(view)
}
