package com.neusoft.oddc.ui.setting;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.ExpandableRecyclerViewAdapter;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

import java.util.List;

public class SettingRecyclerAdapter extends ExpandableRecyclerViewAdapter<SettingGroupViewHolder, SettingChildViewHolder> {

    private LayoutInflater inflater;

    public SettingRecyclerAdapter(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public SettingGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.recycler_item_setting_group, parent, false);
        return new SettingGroupViewHolder(view);
    }

    @Override
    public SettingChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_item_setting_child, parent, false);
        return new SettingChildViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(SettingChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final String child = (String) group.getItems().get(childIndex);
        holder.setText(child);
    }

    @Override
    public void onBindGroupViewHolder(SettingGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setText(group);
    }
}