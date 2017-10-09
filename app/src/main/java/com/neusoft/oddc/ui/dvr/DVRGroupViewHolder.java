package com.neusoft.oddc.ui.dvr;


import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.GroupViewHolder;

public class DVRGroupViewHolder extends GroupViewHolder {

    private TextView groupTitle;

    public DVRGroupViewHolder(View itemView) {
        super(itemView);
        groupTitle = (TextView) itemView.findViewById(R.id.dvr_playback_group_textview);
    }

    public void setText(ExpandableGroup group) {
        groupTitle.setText(group.getTitle());
    }

}
