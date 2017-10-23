package com.neusoft.oddc.activity;

import android.os.Bundle;

import com.neusoft.oddc.R;

public class WelcomeActivity extends BaseActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);
    }


}
