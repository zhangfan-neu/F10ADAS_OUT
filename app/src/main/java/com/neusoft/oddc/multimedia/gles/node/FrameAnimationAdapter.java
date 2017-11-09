package com.neusoft.oddc.multimedia.gles.node;

import android.graphics.Bitmap;

public interface FrameAnimationAdapter {

    Bitmap getFrameBitmap(int index);

    int getFrameTexture(int index, int textureID);

    int getNextFrameTexture();

    float[] getNextFrameTextureTransformMatrix();

    boolean isFinished();

}
