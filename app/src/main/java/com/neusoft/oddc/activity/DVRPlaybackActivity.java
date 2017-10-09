package com.neusoft.oddc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.neusoft.oddc.R;
import com.neusoft.oddc.fragment.DVRPlaybackFragment;

public class DVRPlaybackActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = DVRPlaybackActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dvr_playback);
        setCustomTitle(R.string.title_dvr_playback);

        if (findViewById(R.id.activity_dvr_playback_fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            DVRPlaybackFragment fragment = new DVRPlaybackFragment();
            startFragment(R.id.activity_dvr_playback_fragment_container, fragment);
        }

        Button button = (Button) findViewById(R.id.custom_title_left_button);
        if (null != button) {
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.back);
            button.setOnClickListener(this);
        }

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
