package com.neusoft.oddc.multimedia.interf;


public interface CameraStatusChangedListener {

    void onCameraOpened();

    void onCameraPrepared();

    void onCameraReleased();

    void onPreviewStarted();

    void onPreviewStopped();

    void onCameraError();
}
