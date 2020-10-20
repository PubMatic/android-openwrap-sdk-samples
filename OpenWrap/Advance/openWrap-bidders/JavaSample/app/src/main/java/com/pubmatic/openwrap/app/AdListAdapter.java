/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2020 PubMatic, All Rights Reserved.
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
package com.pubmatic.openwrap.app;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pubmatic.sdk.common.utility.POBUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdListAdapter extends RecyclerView.Adapter<AdListAdapter.AdViewHolder>{

    private final String TAG = "AdListAdapter";
    private final List<MainActivity.AdType> itemList;
    private OnItemClickListener itemClickListener;

    AdListAdapter(@NonNull List<MainActivity.AdType> list){
        itemList = list;
    }

    void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.ad_list_item, parent, false);
        return new AdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        MainActivity.AdType item = getItem(position);
        holder.title.setText(item.getDisplayName());
        if(item.getActivity() == null){
            holder.itemView.setBackgroundColor(0xFFD3D3D3);
            holder.itemView.getLayoutParams().height = POBUtils.convertDpToPixel(40);
            holder.title.setTextSize(POBUtils.convertDpToPixel(6));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private MainActivity.AdType getItem(int position){
        return itemList.get(position);
    }


    class AdViewHolder extends RecyclerView.ViewHolder{
        private final TextView title;
        AdViewHolder(View view){
            super(view);
            title = view.findViewById(R.id.ad_title);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(null != itemClickListener){
                        itemClickListener.onClick(view, getAdapterPosition());
                    }else {
                        Log.d(TAG, "OnItemClickListener not set");
                    }
                }
            });
        }

    }

    interface OnItemClickListener{
        void onClick(View view, int position);
    }
}
