package com.neusoft.oddc.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.Spanned;

import android.widget.EditText;

import android.util.Log;

import com.neusoft.oddc.R;
import com.neusoft.oddc.oddc.neusoft.JobManager;


public class SettingIPaddrActivity extends BaseEdittableActivity {

    private EditText ipaddr_edittext;


    InputFilter[] filters = new InputFilter[1];


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_ipaddr);

        setCustomTitle(R.string.setting_group_item8);

        //BASE_URL = "http://54.218.79.209:8080/ODDCServer/";

        String b_url = com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL;
        int c = b_url.indexOf(':',6);
        Log.w("ALFREDO","SettingIPaddrActivity.onCreate "+c+" "+b_url);
        String url = b_url.substring(7,c);

        filters[0] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }
        };

        ipaddr_edittext = (EditText) findViewById(R.id.ipaddr_edittext);
        ipaddr_edittext.setFilters(filters);
        if (null != ipaddr_edittext) {
            ipaddr_edittext.setText(url);
        }
        Log.w("ALFREDO","SettingIPaddrActivity.onCreate newIP="+com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL);
    }

    @Override
    void OnStartEditMode() {
        ipaddr_edittext.setEnabled(true);
        Log.w("ALFREDO","SettingIPaddrActivity.OnStartEditMode");
    }

    @Override
    void OnEndEditMode(boolean save) {
        ipaddr_edittext.setEnabled(false);

        if (save) {
            String url = "http://" + ipaddr_edittext.getText().toString() + ":8080/ODDCServer/";
            com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL = url;
            JobManager.getInstance().reset(url);
        }
        finish();

        Log.w("ALFREDO","SettingIPaddrActivity.OnEndEditMode SAVE IP="+com.neusoft.oddc.oddc.neusoft.Constants.ODDCApp.BASE_URL);
    }

}
