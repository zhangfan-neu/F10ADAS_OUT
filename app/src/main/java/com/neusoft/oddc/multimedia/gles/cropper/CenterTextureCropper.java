package com.neusoft.oddc.multimedia.gles.cropper;

import android.graphics.Matrix;
import android.util.Log;

import com.neusoft.oddc.multimedia.gles.GlUtil;

import java.nio.FloatBuffer;

public class CenterTextureCropper extends BaseTextureCropper {

    private static final String TAG = CenterTextureCropper.class.getSimpleName();
    protected float scaleX = 1f;
    protected float scaleY = 1f;

    @Override
    public synchronized void updateSize(float textureWidth, float textureHeight, float desiredWidth,
                                        float desiredHeight) {
        super.updateSize(textureWidth, textureHeight, desiredWidth, desiredHeight);

        float inputWidth = textureWidth;
        float inputHeight = textureHeight;
        if (videoOrientationHint % 180 != 0) {
            inputWidth = textureHeight;
            inputHeight = textureWidth;
        }


        float ratioTexture = inputWidth / inputHeight;
        float radioDesire = this.desiredWidth / this.desiredHeight;

        Log.d(TAG, "crop trace -- > updateSize = ratioTexture " + ratioTexture
                + ", radioDesire = " + radioDesire
        );

        if (ratioTexture >= radioDesire) {
            this.scaleY = 1f;
            this.scaleX = (inputHeight * radioDesire) / inputWidth;
        } else {
            this.scaleX = 1f;
            this.scaleY = (inputWidth / radioDesire) / inputHeight;
        }


        Log.d(TAG, "crop trace -- > updateSize = inputWidth " + inputWidth
                + ", inputHeight = " + inputHeight
                + ", desiredWidth = " + this.desiredWidth
                + ", desiredHeight = " + this.desiredHeight
                + ", scaleX = " + this.scaleX
                + ", scaleY = " + this.scaleY);
        this.texCorrdBuffer = this.cropTexture(this.texCorrdArray);
    }

    @Override
    public synchronized FloatBuffer cropTexture(float[] srcPoints) {

        int length = srcPoints.length;
        float[] destPoints = new float[length];
        Matrix matrix = new Matrix();
        matrix.postTranslate(-0.5f, -0.5f);
        matrix.postScale(this.scaleX, this.scaleY);
        matrix.postRotate(videoOrientationHint);

        matrix.postTranslate(0.5f, 0.5f);
        matrix.mapPoints(destPoints, srcPoints);

//        Log.d(TAG, "crop trace -- > cropTexture = textureWidth " + this.textureWidth
//                + ", textureHeight = " + this.textureHeight
//                + ", desiredWidth = " + this.desiredWidth
//                + ", desiredHeight = " + this.desiredHeight
//                + ", scaleX = " + this.scaleX
//                + ", scaleY = " + this.scaleY
//                + ", videoOrientationHint = " + this.videoOrientationHint);

//        Log.d(TAG, "crop trace -- > destVector start : ---------------");
//        for (float value : destPoints) {
//            Log.d(TAG, "crop trace -- > destVector : " + value);
//        }
//        Log.d(TAG, "crop trace -- > destVector end : ---------------");
        return GlUtil.createFloatBuffer(destPoints);
    }

    public synchronized void updateVideoOrientaionHint(int orientationHint) {
        this.videoOrientationHint = orientationHint;
        updateSize(this.textureWidth, this.textureHeight, this.desiredWidth, this.desiredHeight);
    }

}
