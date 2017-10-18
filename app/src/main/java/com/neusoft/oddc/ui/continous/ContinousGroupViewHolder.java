package com.neusoft.oddc.ui.continous;


import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.GroupViewHolder;

public class ContinousGroupViewHolder extends GroupViewHolder {

    private TextView groupTitle;

    public ContinousGroupViewHolder(View itemView) {
        super(itemView);
        groupTitle = (TextView) itemView.findViewById(R.id.oa_continuous_upload_log_group_textview);
    }

    public void setText(ExpandableGroup group) {
        groupTitle.setText(group.getTitle());
    }

}
