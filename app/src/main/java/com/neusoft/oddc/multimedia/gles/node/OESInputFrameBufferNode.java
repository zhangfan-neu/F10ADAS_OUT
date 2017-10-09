package com.neusoft.oddc.multimedia.gles.node;

import com.neusoft.oddc.multimedia.gles.cropper.BaseTextureCropper;

public class OESInputFrameBufferNode extends OESInputRectNode implements GLFrameBuffer {

    private FrameBufferWrapper frameBuffer;
    private int outputWidth = DEFAULT_WIDTH;
    private int outputHeight = DEFAULT_HEIGHT;

    public OESInputFrameBufferNode(BaseTextureCropper corpper) {
        super(corpper);
        frameBuffer = new FrameBufferWrapper();
    }

    @Override
    public void init() {
        super.init();
        frameBuffer.create(this.outputWidth, this.outputHeight, true);
    }

    @Override
    public void release() {
        super.release();
        frameBuffer.release();
    }

    @Override
    public int draw() {
        frameBuffer.bind(true);
        super.draw();

        frameBuffer.unbind();
        return frameBuffer.getTextureId();
    }

    @Override
    public void setOutputSize(int width, int height) {
        this.outputWidth = width;
        this.outputHeight = height;
        setFullFrameSize(this.outputWidth, this.outputHeight);
    }

    public int getTextureID() {
        return frameBuffer.getTextureId();
    }
}
