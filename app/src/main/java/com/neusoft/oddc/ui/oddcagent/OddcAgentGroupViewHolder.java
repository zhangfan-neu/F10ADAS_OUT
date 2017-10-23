package com.neusoft.oddc.ui.oddcagent;


import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.GroupViewHolder;

public class OddcAgentGroupViewHolder extends GroupViewHolder {

    private TextView groupTitle;

    public OddcAgentGroupViewHolder(View itemView) {
        super(itemView);
        groupTitle = (TextView) itemView.findViewById(R.id.oddc_agent_item_group_textview);
    }

    public void setText(ExpandableGroup group) {
        groupTitle.setText(group.getTitle());
    }

}
