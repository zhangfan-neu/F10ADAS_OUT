package com.neusoft.oddc.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;

import com.neusoft.oddc.R;

public class SettingAdasCalibrationActivity extends BaseEdittableActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_adas_calibration_profile);

        setCustomTitle(R.string.setting_group_item3);

        initViews();
    }

    private void initViews() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideAllTitleButtons();
    }

    @Override
    void OnStartEditMode() {

    }

    @Override
    void OnEndEditMode(boolean save) {

    }

}
