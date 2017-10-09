package com.neusoft.oddc.multimedia.gles.render;

import android.opengl.GLES20;
import android.util.Log;

import com.neusoft.oddc.multimedia.gles.IVideoFrameRender;
import com.neusoft.oddc.multimedia.gles.cropper.BaseTextureCropper;
import com.neusoft.oddc.multimedia.gles.cropper.CenterTextureCropper;
import com.neusoft.oddc.multimedia.gles.node.ImageFilterFrameBufferNode;
import com.neusoft.oddc.multimedia.gles.node.OESInputFrameBufferNode;
import com.neusoft.oddc.multimedia.gles.node.VideoFrameOutputNode;

public class VideoFrameRender implements IVideoFrameRender {
    public static final String TAG = VideoFrameRender.class.getSimpleName();

    protected OESInputFrameBufferNode inputFrame;
    protected VideoFrameOutputNode outputFrame;
    protected ImageFilterFrameBufferNode filterFrame;
    protected BaseTextureCropper encodeTextureCroper = new CenterTextureCropper();

    protected int inputWidth = 0;
    protected int inputHeight = 0;
    protected int outputWidth = 0;
    protected int outputHeight = 0;

    protected int videoOrientation = 0;

    protected boolean isInitialized = false;

    protected int orientationHint = 0;


    public VideoFrameRender() {

    }

    public VideoFrameRender(BaseTextureCropper croper) {
        this.encodeTextureCroper = croper;
    }

    public VideoFrameRender(ImageFilterFrameBufferNode filterNode) {
        filterFrame = filterNode;
    }

    public void init() {
        Log.d(TAG, "overlay trace -- > init : isInitialized = " + isInitialized);
        Log.d(TAG, "VideoFrameRender trace -- > init :  outputWidth ＝ " + outputWidth + ", + outputHeight = " + outputHeight);
        inputFrame = new OESInputFrameBufferNode(encodeTextureCroper);
        inputFrame.setOutputSize(this.outputWidth, this.outputHeight);
        inputFrame.init();
        Log.d(TAG, "videoFrameRender trace -- > setup: filterFrame is null ?  " + (filterFrame == null));
        if (null == filterFrame) {
            filterFrame = new ImageFilterFrameBufferNode();
        }

        filterFrame.setOutputSize(this.outputWidth, this.outputHeight);
        filterFrame.init();
        filterFrame.setFrameTexture(inputFrame.getTextureID(), false);

        outputFrame = new VideoFrameOutputNode();
        outputFrame.setFullFrameSize(this.outputWidth, this.outputHeight);
        outputFrame.init();
        outputFrame.setOrientationHint(this.orientationHint);
        outputFrame.setFrameTexture(filterFrame.getTextureID());
        isInitialized = true;
        Log.d(TAG, "overlay trace -- > init : isInitialized = " + isInitialized);
    }

    public void release() {
        isInitialized = false;
        if (inputFrame != null) {
            inputFrame.release();
//			inputFrame = null;
        }

        if (filterFrame != null) {
            filterFrame.release();
//			filterFrame = null;
        }

        if (null != outputFrame) {
            outputFrame.release();
//			outputFrame = null;
        }
    }

    public void updateInputSize(int width, int height) {
        Log.d(TAG, "VideoFrameRender trace -- > updateInputSize :  width = " + width + ", height = " + height);
        this.inputWidth = width;
        this.inputHeight = height;
        encodeTextureCroper.updateTextureSize(this.inputWidth, this.inputHeight);
    }

    public void updateOutputSize(int width, int height) {
        Log.d(TAG, "VideoFrameRender trace -- > updateOutputSize :  width = " + width + ", height = " + height);
        this.outputWidth = width;
        this.outputHeight = height;
        encodeTextureCroper.updateDesiredSize(this.outputWidth, this.outputHeight);
    }

    public void updateVideoOritation(int videoOrientation) {

        Log.d(TAG, "VideoFrameRender trace -- > updateOritation :  " + "videoOrientation = " + videoOrientation);

        this.videoOrientation = videoOrientation;
        encodeTextureCroper.updateVideoOrientaionHint(this.videoOrientation);
    }

    public int renderInput(int textureId, float[] texMatrix) {
        inputFrame.setFrameTexture(textureId, texMatrix);
        inputFrame.draw();
        filterFrame.draw();
        return 0;
    }

    @Override
    public int renderOutput() {

        outputFrame.setUseOrientationHint(false);
        outputFrame.draw();

        return 0;
    }

    @Override
    public int renderOutputWithOrientationHint() {

        boolean needUpdateViewport = false;
        int[] viewport = null;
        if (orientationHint % 180 != 0) {
            needUpdateViewport = true;
            viewport = new int[4];
            GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
            GLES20.glViewport(0, 0, outputHeight, outputWidth);
        }
        outputFrame.setUseOrientationHint(true);
        outputFrame.draw();
        if (needUpdateViewport) {
            GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        }
        return 0;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public int getInputFrameTexture() {
        if (isInitialized()) {
            return inputFrame.getTextureID();
        }
        return 0;
    }

    @Override
    public int getInputWidth() {
        return this.inputWidth;
    }

    @Override
    public int getinputHeight() {
        return this.inputHeight;
    }

    @Override
    public int getOutputWidth() {
        return this.outputWidth;
    }

    @Override
    public int getOutputHeight() {
        return this.outputHeight;
    }

    @Override
    public int getVideoOrientation() {
        return this.videoOrientation;
    }

    public void setOrientationHint(int orientation) {
        this.orientationHint = orientation;
        if (null != outputFrame) {
            outputFrame.setOrientationHint(this.orientationHint);
        }
    }

    public void setEncodeTextureCroper(BaseTextureCropper croper) {
        this.encodeTextureCroper = croper;
    }
}
