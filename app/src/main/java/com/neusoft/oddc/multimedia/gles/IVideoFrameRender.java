package com.neusoft.oddc.multimedia.gles;

public interface IVideoFrameRender {

    void init();

    void release();

    void updateInputSize(int width, int height);

    void updateOutputSize(int width, int height);

    void updateVideoOritation(int videoOrientation);

    int renderInput(int textureId, float[] texMatrix);

    int renderOutput();

    int renderOutputWithOrientationHint();

    void setOrientationHint(int orientation);

    boolean isInitialized();

    int getInputFrameTexture();

    int getInputWidth();

    int getinputHeight();

    int getOutputWidth();

    int getOutputHeight();

    int getVideoOrientation();

}
