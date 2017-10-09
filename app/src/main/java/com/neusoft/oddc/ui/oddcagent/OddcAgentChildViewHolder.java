package com.neusoft.oddc.ui.oddcagent;


import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.ChildViewHolder;

public class OddcAgentChildViewHolder extends ChildViewHolder {

    private TextView childName;

    public OddcAgentChildViewHolder(View itemView) {
        super(itemView);
        childName = (TextView) itemView.findViewById(R.id.oddc_agent_item_child_textview);
    }

    public void setText(String str) {
        childName.setText(str);
    }

}
