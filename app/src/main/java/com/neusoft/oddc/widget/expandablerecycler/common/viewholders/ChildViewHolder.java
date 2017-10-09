package com.neusoft.oddc.widget.expandablerecycler.common.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.neusoft.oddc.widget.expandablerecycler.common.listeners.OnChildClickListener;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

/**
 * ViewHolder for {@link ExpandableGroup#items}
 */
public class ChildViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private OnChildClickListener listener;

    public ChildViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onChildClick(getAdapterPosition());
        }
    }

    public void setOnChildClickListener(OnChildClickListener listener) {
        this.listener = listener;
    }


}
