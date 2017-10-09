package com.neusoft.oddc.ui.setting;


import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.GroupViewHolder;

public class SettingGroupViewHolder extends GroupViewHolder {

    private TextView groupTitle;

    public SettingGroupViewHolder(View itemView) {
        super(itemView);
        groupTitle = (TextView) itemView.findViewById(R.id.setting_item_group_textview);
    }

    public void setText(ExpandableGroup group) {
        groupTitle.setText(group.getTitle());
    }

}
