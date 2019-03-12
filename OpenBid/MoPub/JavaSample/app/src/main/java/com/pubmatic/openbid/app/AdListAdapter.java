package com.pubmatic.openbid.app;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AdListAdapter extends RecyclerView.Adapter<AdListAdapter.AdViewHolder>{

    private final String TAG = "AdListAdapter";
    private final List<String> itemList;
    private OnItemClickListener itemClickListener;

    AdListAdapter(@NonNull List<String> list){
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
        String item = getItem(position);
        holder.title.setText(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private String getItem(int position){
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
