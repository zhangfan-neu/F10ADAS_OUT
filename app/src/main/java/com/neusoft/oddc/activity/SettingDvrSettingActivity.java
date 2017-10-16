package com.neusoft.oddc.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.neusoft.oddc.R;

public class SettingDvrSettingActivity extends BaseEdittableActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_dvr_setting_profile);

        setCustomTitle(R.string.setting_group_item5);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideAllTitleButtons();
    }

    private void initViews() {
    }

    @Override
    void OnStartEditMode() {

    }

    @Override
    void OnEndEditMode(boolean save) {

    }

}
