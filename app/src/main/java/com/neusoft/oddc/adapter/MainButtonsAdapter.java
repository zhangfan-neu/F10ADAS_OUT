package com.neusoft.oddc.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.R;
import com.neusoft.oddc.entity.EntityMainButton;

import java.util.ArrayList;
import java.util.List;

public class MainButtonsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = MainButtonsAdapter.class.getSimpleName();

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, EntityMainButton entity);
    }

    public MainButtonsAdapter(Context context) {
        this.context = context;
    }


    private Context context;
    private OnRecyclerViewItemClickListener itemClickListener = null;
    private List<EntityMainButton> entities = new ArrayList<>();

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public ContentViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.main_recycler_item_tv);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_main_button, parent, false);
        return new ContentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        EntityMainButton entity = entities.get(position);
        ((ContentViewHolder) holder).textView.setText(context.getString(entity.getButtonStrId()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "MainButtonsAdapter onItemClick position = " + position);
                    Log.d(TAG, "MainButtonsAdapter onItemClick holder.getAdapterPosition() = " + holder.getAdapterPosition());
                    Log.d(TAG, "MainButtonsAdapter itemClickListener = " + itemClickListener);
                }
                if (null != itemClickListener) {
                    itemClickListener.onItemClick(v, entities.get(holder.getAdapterPosition()));
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == entities || entities.size() <= 0) {
            return 0;
        }
        return (entities.size());// + 1
    }

    public void setEntity(List<EntityMainButton> entities) {
        this.entities = entities;
        notifyDataSetChanged();
    }

}
