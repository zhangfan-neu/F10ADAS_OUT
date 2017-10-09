package com.neusoft.oddc.ui.continous;

import android.view.View;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.ChildViewHolder;

public class ContinousChildViewHolder extends ChildViewHolder {

    private TextView param1;
    private TextView param2;
    private TextView param3;
    private TextView param4;
    private TextView param5;

    public ContinousChildViewHolder(View itemView) {
        super(itemView);
        param1 = (TextView) itemView.findViewById(R.id.oa_continuous_upload_log_child_textview1);
        param2 = (TextView) itemView.findViewById(R.id.oa_continuous_upload_log_child_textview2);
        param3 = (TextView) itemView.findViewById(R.id.oa_continuous_upload_log_child_textview3);
        param4 = (TextView) itemView.findViewById(R.id.oa_continuous_upload_log_child_textview4);
        param5 = (TextView) itemView.findViewById(R.id.oa_continuous_upload_log_child_textview5);
    }

    public void setParam1Text(String str) {
        param1.setText(str);
    }

    public void setParam2Text(String str) {
        param2.setText(str);
    }

    public void setParam3Text(String str) {
        param3.setText(str);
    }

    public void setParam4Text(String str) {
        param4.setText(str);
    }

    public void setParam5Text(String str) {
        param5.setText(str);
    }

}
