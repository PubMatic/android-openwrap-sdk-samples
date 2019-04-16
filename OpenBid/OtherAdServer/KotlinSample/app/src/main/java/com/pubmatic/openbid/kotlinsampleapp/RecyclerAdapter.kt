package com.pubmatic.openbid.kotlinsampleapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pubmatic.sdk.common.utility.POBUtils
import java.util.*

/**
 * Recycler adapter used for List Adapter implementation
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
            return 0
        }
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.aditem.text = getItem(position)?.displayName
        if(getItem(position)?.activity == null){
            holder.aditem.setBackgroundColor(-0x2c2c2d)
            holder.aditem.layoutParams.height = POBUtils.convertDpToPixel(35)
            holder.aditem.textSize = POBUtils.convertDpToPixel(6).toFloat()
        }
    }

    /**
     * To get item of AdType
     */
    fun getItem(position: Int): MainActivity.AdType? {
        return list?.get(position)
    }

    /**
     * Class to provide ViewHolder implementation
     */
    class AdViewHolder(view: View, val itemClickListener: OnItemClickListener): RecyclerView.ViewHolder(view){

        val aditem: TextView
        init {
            aditem = view.findViewById(R.id.ad_item)
            aditem.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }
        }



    }

    /**
     * Listener interface to provide item interaction callbacks
     */
    interface OnItemClickListener{
        /**
         * Listener method to provide item click callback
         */
        fun onItemClick(adType: Int)
    }

}