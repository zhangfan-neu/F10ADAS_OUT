package com.neusoft.oddc.activity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.neusoft.oddc.R;


public class SettingVersionActivity extends BaseActivity implements View.OnClickListener {
    static Context mContext;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_version);

        setCustomTitle(R.string.setting_group_item7);
        initBackButton();

        mContext = getApplicationContext();
        String tVer = "";
        try {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            tVer = pinfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e){}

        TextView textView = (TextView) findViewById(R.id.settingVersion);
        if (null != textView) {
            textView.setText("Version  " + tVer);
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_title_left_button:
                finish();
                break;
            default:
                break;
        }
    }

    private void initBackButton()
    {
        Button button = (Button) findViewById(R.id.custom_title_left_button);
        if (null != button) {
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.back);
            button.setOnClickListener(this);
        }
    }
}
