package com.neusoft.oddc.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.neusoft.oddc.R;

import java.io.IOException;

public class CameraActivity extends BaseActivity {

    private Camera camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_camera);
    }

    @Override
    protected void onResume() {
        super.onResume();

        init();
    }

    private void init() {
        camera = Camera.open();
        SurfaceView textureView = (SurfaceView) findViewById(R.id.mediarecorder_surfaceview);
        final SurfaceHolder holder = textureView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(1280, 720);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });


    }
}
