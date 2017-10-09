package com.neusoft.oddc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.oddc.model.DataPackageType;

import java.util.ArrayList;
import java.util.List;

public class OAContinuousUploadLogSpinnerAdapter extends BaseAdapter {

    private static final String TAG = OAContinuousUploadLogSpinnerAdapter.class.getSimpleName();
    private List<DataPackageType> list;
    private Context context;

    public OAContinuousUploadLogSpinnerAdapter(Context context) {
        this.context = context;
        list = new ArrayList<>();
        list.add(DataPackageType.CONTINUOUS);
        list.add(DataPackageType.EVENT);
        list.add(DataPackageType.SELECTIVE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public DataPackageType getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        convertView = layoutInflater.inflate(R.layout.spinner_continuous_upload_log_item, null);
        if (convertView != null) {
            TextView accountTextView = (TextView) convertView.findViewById(R.id.row_spn_tv_continuous_upload_log);
            int textStrId = 0;
            DataPackageType dataPackageType = list.get(position);
            switch (dataPackageType) {
                case CONTINUOUS:
                    textStrId = R.string.oa_continuous_upload_log_data_package_type_1;
                    break;
                case EVENT:
                    textStrId = R.string.oa_continuous_upload_log_data_package_type_2;
                    break;
                case SELECTIVE:
                    textStrId = R.string.oa_continuous_upload_log_data_package_type_3;
                    break;
                default:
                    break;
            }
            accountTextView.setText(textStrId);
        }
        return convertView;
    }

}
