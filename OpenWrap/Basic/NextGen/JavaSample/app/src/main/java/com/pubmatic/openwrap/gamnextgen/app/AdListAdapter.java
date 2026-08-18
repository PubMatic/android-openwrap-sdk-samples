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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Recycler adapter that displays the list of ad format entries on the home screen.
 */
public class AdListAdapter extends RecyclerView.Adapter<AdListAdapter.AdViewHolder> {

    private static final String TAG = "AdListAdapter";

    private final List<MainActivity.AD_TYPE> itemList;
    private OnItemClickListener itemClickListener;

    AdListAdapter(@NonNull List<MainActivity.AD_TYPE> list) {
        itemList = list;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.ad_list_item, parent, false);
        return new AdViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @NonNull
    private String getItem(int position) {
        return itemList.get(position).getDisplayText();
    }

    static final class AdViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        AdViewHolder(@NonNull View view, OnItemClickListener itemClickListener) {
            super(view);
            this.title = view.findViewById(R.id.ad_title);
            view.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (itemClickListener != null && position != RecyclerView.NO_POSITION) {
                    itemClickListener.onClick(v, position);
                } else if (itemClickListener == null) {
                    Log.d(TAG, "OnItemClickListener not set");
                }
            });
        }

        void bind(@NonNull String titleText) {
            title.setText(titleText);
        }
    }

    public interface OnItemClickListener {
        void onClick(@NonNull View view, int position);
    }
}
