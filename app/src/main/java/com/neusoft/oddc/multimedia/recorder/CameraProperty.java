package com.neusoft.oddc.multimedia.recorder;

import android.hardware.Camera;

import java.util.List;

public class CameraProperty {

    private int cameraID;

    // package access properties
    List<Camera.Size> supportedPreviewSizes;
    List<Camera.Size> supportedPictureSizes;
    List<int[]> supportedFPSRange;


    boolean flashSupported;


    CameraProperty(int cameraID) {
        this.cameraID = cameraID;
    }


}
