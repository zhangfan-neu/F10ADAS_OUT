package com.neusoft.oddc.activity;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.PreferencesUtils;

public class SettingDvrSettingActivity extends BaseEdittableActivity {

    public static final String KEY_PREF_RENDER_OVERLAY = "key_pref_render_overlay";

    private SwitchCompat ds_render_mode_switch;

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
        showTitleLeftButtons();
    }

    private void initViews() {
        ds_render_mode_switch = (SwitchCompat) findViewById(R.id.ds_render_mode_switch);
        boolean renderOverlay = PreferencesUtils.getBoolean(this, KEY_PREF_RENDER_OVERLAY, true);
        ds_render_mode_switch.setChecked(renderOverlay);
        ds_render_mode_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesUtils.putBoolean(SettingDvrSettingActivity.this, KEY_PREF_RENDER_OVERLAY, isChecked);
            }
        });
    }

    @Override
    void OnStartEditMode() {

    }

    @Override
    void OnEndEditMode(boolean save) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_title_left_button:
                finish();
                break;
            default:
                break;
        }
    }
}
