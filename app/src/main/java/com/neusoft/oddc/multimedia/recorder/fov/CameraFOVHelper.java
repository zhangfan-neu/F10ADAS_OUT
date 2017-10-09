package com.neusoft.oddc.multimedia.recorder.fov;

import android.hardware.Camera;

import com.neusoft.oddc.BuildConfig;

public class CameraFOVHelper {

    public static CameraFOV get() {
        CameraFOV cameraFOV = new CameraFOV();
        Camera camera = null;
        try {
            camera = Camera.open();
            Camera.Parameters cameraParameters = camera.getParameters();
            float verticalViewAngle = cameraParameters.getVerticalViewAngle();
            float horizontalViewAngle = cameraParameters.getHorizontalViewAngle();
            cameraFOV.setVerticalViewAngle(verticalViewAngle);
            cameraFOV.setHorizontalViewAngle(horizontalViewAngle);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                camera.release();
                camera = null;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        return cameraFOV;
    }
}
