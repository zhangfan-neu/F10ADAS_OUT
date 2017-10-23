package com.neusoft.oddc.ui.continous;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.DataConverter;
import com.neusoft.oddc.widget.expandablerecycler.common.ExpandableRecyclerViewAdapter;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

import java.util.List;

public class ContinousRecyclerAdapter extends ExpandableRecyclerViewAdapter<ContinousGroupViewHolder, ContinousChildViewHolder> {

    private LayoutInflater inflater;

    public ContinousRecyclerAdapter(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ContinousGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_item_continous_group, parent, false);
        return new ContinousGroupViewHolder(view);
    }

    @Override
    public ContinousChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_item_continous_child, parent, false);
        return new ContinousChildViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(ContinousChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final EntityContinousChild item = (EntityContinousChild) group.getItems().get(childIndex);
        holder.setParam1Text(item.getSessionId());
        holder.setParam2Text(item.getFileName());
        holder.setParam3Text(DataConverter.getEventTypeStr(item.getEventTypes()));
        holder.setParam4Text(DataConverter.getFileStateStr(item.isMediaUploaded(), item.isMediaDeleted(), item.isDataUploaded()));
        holder.setParam5Text(item.getTime());
    }

    @Override
    public void onBindGroupViewHolder(ContinousGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setText(group);
    }
}