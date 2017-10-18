package com.neusoft.oddc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;

import com.neusoft.oddc.R;
import com.neusoft.oddc.videoplayer.MyVideoView;

public class VideoPlayerActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();

    private static final String KEY_VIDEO_PATH = "key_video_path";

    private MyVideoView videoView;
    private MediaController mediaController;
    private Button backBtn;

    public static final Intent createIntent(Context context, String filePath) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(KEY_VIDEO_PATH, filePath);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_player);
        initViews();
    }

    private void initViews() {
        videoView = (MyVideoView) findViewById(R.id.oddc_videoview);
        mediaController = new MediaController(this, false);
        backBtn = (Button) findViewById(R.id.video_player_activity_back_btn);
        backBtn.setOnClickListener(this);

        String filePath = getFilePath();

        if (!TextUtils.isEmpty(filePath)) {
            videoView.setVideoPath(filePath);
            videoView.setMediaController(mediaController);
            mediaController.setMediaPlayer(videoView);
            videoView.requestFocus();
            videoView.start();
        }

    }

    private String getFilePath() {
        String filePath = "";
        Intent intent = getIntent();
        if (null != intent) {
            filePath = intent.getStringExtra(KEY_VIDEO_PATH);
        }
        return filePath;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != videoView) {
            videoView.pause();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (null != videoView) {
            videoView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (null != videoView) {
            videoView.stopPlayback();
            videoView.suspend();
            videoView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_player_activity_back_btn:
                finish();
                break;
            default:
                break;
        }
    }
}
