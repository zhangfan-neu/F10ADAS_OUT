package com.neusoft.oddc.multimedia.recorder.fov;

public class CameraFOV {

    float verticalViewAngle;
    float horizontalViewAngle;

    public CameraFOV() {

    }

    public CameraFOV(float verticalViewAngle, float horizontalViewAngle) {
        this.verticalViewAngle = verticalViewAngle;
        this.horizontalViewAngle = horizontalViewAngle;
    }

    public float getVerticalViewAngle() {
        return verticalViewAngle;
    }

    public void setVerticalViewAngle(float verticalViewAngle) {
        this.verticalViewAngle = verticalViewAngle;
    }

    public float getHorizontalViewAngle() {
        return horizontalViewAngle;
    }

    public void setHorizontalViewAngle(float horizontalViewAngle) {
        this.horizontalViewAngle = horizontalViewAngle;
    }
}
