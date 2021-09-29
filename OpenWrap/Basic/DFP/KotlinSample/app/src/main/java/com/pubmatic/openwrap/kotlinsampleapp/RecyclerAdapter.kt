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
package com.pubmatic.openwrap.kotlinsampleapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Recycler adapter used for List Adapter implementation. This class shows the entry for the Ad
 * types for demonstrating the respective features.
 */
class RecyclerAdapter(val list: ArrayList<MainActivity.AdType>?, val itemClickListener: OnItemClickListener) :  RecyclerView.Adapter<RecyclerAdapter.AdViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): AdViewHolder {
        val view = LayoutInflater.from(parent.context).inflate( R.layout.layout_ad_item, parent, false)
        return AdViewHolder(view, itemClickListener)
    }

    override fun getItemCount(): Int {
        if(null != list){
            return list.size
        }else{
            return 0;
        }

    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.aditem.text = list?.get(position)?.displayName
    }

    /**
     * Holder class having the text for the ad types
     */
    class AdViewHolder(val view: View, val itemClickListener: OnItemClickListener): RecyclerView.ViewHolder(view){

        val aditem: TextView
        init {
            aditem = view.findViewById(R.id.ad_item)
            aditem.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }


        }
    }

    /**
     * Interface for providing the click notification callback to MainActivity
     */
    interface OnItemClickListener{
        /**
         * This method gets called from adapter when user clicks on any list items
         */
        fun onItemClick(position: Int)
    }

}