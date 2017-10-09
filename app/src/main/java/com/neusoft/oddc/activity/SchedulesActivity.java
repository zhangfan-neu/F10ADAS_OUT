package com.neusoft.oddc.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.neusoft.oddc.R;

public class SchedulesActivity extends BaseActivity {

    private static final String TAG = SchedulesActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);

        setCustomTitle(R.string.main_button_schedules);
    }


}
