package com.pubmatic.openbid.kotlinsampleapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class RecyclerAdapter(val list: ArrayList<String>?, val itemClickListener: OnItemClickListener) :  RecyclerView.Adapter<RecyclerAdapter.AdViewHolder>(){


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
        holder.aditem.text = list?.get(position)
    }




    class AdViewHolder(val view: View, val itemClickListener: OnItemClickListener): RecyclerView.ViewHolder(view){

        val aditem: TextView
        init {
            aditem = view.findViewById(R.id.ad_item)
            aditem.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }


        }



    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

}