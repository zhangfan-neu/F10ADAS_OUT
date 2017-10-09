package com.neusoft.oddc.multimedia.gles.cropper;

import android.graphics.Matrix;
import android.util.Log;

import com.neusoft.oddc.multimedia.gles.GlUtil;

import java.nio.FloatBuffer;


public class BaseTextureCropper {

    private static final String TAG = BaseTextureCropper.class.getSimpleName();

    private static final float RECTANGLE_TEX_COORDS[] = {
            0.0f, 0.0f, // 0 bottom left
            1.0f, 0.0f, // 1 bottom right
            0.0f, 1.0f, // 2 top left
            1.0f, 1.0f // 3 top right
    };

    protected float textureWidth = 1280f;
    protected float textureHeight = 720f;
    protected float desiredWidth = 1280f;
    protected float desiredHeight = 720f;

    protected int videoOrientationHint = 0;

    protected float[] texCorrdArray = RECTANGLE_TEX_COORDS;
    protected FloatBuffer texCorrdBuffer = GlUtil.createFloatBuffer(texCorrdArray);

    public synchronized void updateTextureSize(float textureWidth, float textureHeight) {

        updateSize(textureWidth, textureHeight, this.desiredWidth, this.desiredHeight);
        Log.d(TAG, "crop trace -- > updateTextureSize : updateSize = textureWidth " + this.textureWidth
                + ", textureHeight = " + this.textureHeight
                + ", desiredWidth = " + this.desiredWidth
                + ", desiredHeight = " + this.desiredHeight);
    }

    public synchronized void updateDesiredSize(float desiredWidth, float desiredHeight) {

        updateSize(this.textureWidth, this.textureHeight, desiredWidth, desiredHeight);
        Log.d(TAG, "crop trace -- > updateDesiredSize : updateSize = textureWidth " + this.textureWidth
                + ", textureHeight = " + this.textureHeight
                + ", desiredWidth = " + this.desiredWidth
                + ", desiredHeight = " + this.desiredHeight);
    }

    public synchronized void updateSize(float textureWidth, float textureHeight,
                                        float desiredWidth, float desiredHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.desiredWidth = desiredWidth;
        this.desiredHeight = desiredHeight;
    }

    public synchronized FloatBuffer cropTexture(float[] srcPoints) {
        int length = srcPoints.length;
        float[] destPoints = new float[length];
        Matrix matrix = new Matrix();
        matrix.postTranslate(-0.5f, -0.5f);
        matrix.postRotate(videoOrientationHint);
        matrix.postTranslate(0.5f, 0.5f);
        matrix.mapPoints(destPoints, srcPoints);

//        Log.d(TAG, "crop trace -- > destVector start : ---------------");
//        for (float value : destPoints) {
//            Log.d(TAG, "crop trace -- > destVector : " + value);
//        }
//        Log.d(TAG, "crop trace -- > destVector end : ---------------");
        return GlUtil.createFloatBuffer(destPoints);
    }

    public synchronized FloatBuffer getCropTextureCoordBuffer() {
        return texCorrdBuffer;
    }

    public synchronized void updateVideoOrientaionHint(int orientationHint) {
        this.videoOrientationHint = orientationHint;
    }

}
