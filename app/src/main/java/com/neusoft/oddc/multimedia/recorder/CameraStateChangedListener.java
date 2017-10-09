package com.neusoft.oddc.multimedia.recorder;

public interface CameraStateChangedListener {

    void onCameraOpened(CameraProperty cameraProperty);

    void onCameraPrepared(CameraProperty cameraProperty);

    void onCameraReleased(CameraProperty cameraProperty);

    void onPreviewStarted(boolean sizeChanged);

    void onPreviewStopped();

    void onCameraError();

}
