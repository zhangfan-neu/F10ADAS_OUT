package com.neusoft.oddc.ui.oddcagent;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.ExpandableRecyclerViewAdapter;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

import java.util.List;

public class OddcAgentRecyclerAdapter extends ExpandableRecyclerViewAdapter<OddcAgentGroupViewHolder, OddcAgentChildViewHolder> {

    private LayoutInflater inflater;

    public OddcAgentRecyclerAdapter(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public OddcAgentGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.recycler_item_oddc_agent_group, parent, false);
        return new OddcAgentGroupViewHolder(view);
    }

    @Override
    public OddcAgentChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_item_oddc_agent_child, parent, false);
        return new OddcAgentChildViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(OddcAgentChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final String child = (String) group.getItems().get(childIndex);
        holder.setText(child);
    }

    @Override
    public void onBindGroupViewHolder(OddcAgentGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setText(group);
    }
}