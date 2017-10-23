package com.neusoft.oddc.ui.setting;


import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.ChildViewHolder;

public class SettingChildViewHolder extends ChildViewHolder {

    private TextView childName;

    public SettingChildViewHolder(View itemView) {
        super(itemView);
        childName = (TextView) itemView.findViewById(R.id.setting_item_child_textview);
    }

    public void setText(String str) {
        childName.setText(str);
    }

}
