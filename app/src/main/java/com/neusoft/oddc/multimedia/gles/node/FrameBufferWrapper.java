package com.neusoft.oddc.multimedia.gles.node;

import android.opengl.GLES20;
import android.util.Log;

public class FrameBufferWrapper {

    private int[] bufferId;
    private int[] textureId;
    private int width = -1;
    private int height = -1;

    private int[] viewportCache = new int[4];

    private float red = 0f;
    private float green = 0f;
    private float blue = 0f;
    private float alpha = 1f;

    public FrameBufferWrapper() {

    }

    public FrameBufferWrapper(float r, float g, float b, float a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    public void create(int width, int height, boolean isNearest) {
        if (bufferId != null) {
            release();
        }
        this.width = width;
        this.height = height;

        bufferId = new int[1];
        textureId = new int[1];
        GLES20.glGenFramebuffers(1, bufferId, 0);
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, isNearest ? GLES20.GL_NEAREST : GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, isNearest ? GLES20.GL_NEAREST : GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId[0], 0);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("GroupFrameBuffer", "Error creating frame buffer " + bufferId[0] + " " + textureId[0]);
        }
        GLES20.glClearColor(this.red, this.green, this.blue, this.alpha);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Log.e("FRAME_BUFFER ", "BUF_ID " + bufferId[0] + " TEX_ID " + textureId[0]);
    }

    public void setColor(float r, float g, float b, float a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
        GLES20.glClearColor(this.red, this.green, this.blue, this.alpha);
    }

    public void bind(boolean isClear) {
        if (null == bufferId) {
            return;
        }

        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewportCache, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, bufferId[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId[0], 0);
//		GLES20.glViewport(0, 0, width, height);
        if (isClear) {
            GLES20.glClearColor(this.red, this.green, this.blue, this.alpha);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
        GLES20.glViewport(0, 0, width, height);
    }

    public void unbind() {
        if (null == bufferId) {
            return;
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glViewport(viewportCache[0], viewportCache[1], viewportCache[2], viewportCache[3]);
    }

    public void release() {
        if (textureId != null) {
            GLES20.glDeleteTextures(textureId.length, textureId, 0);
            textureId = null;
        }
        if (bufferId != null) {
            GLES20.glDeleteFramebuffers(bufferId.length, bufferId, 0);
            bufferId = null;
        }
    }

    public int getTextureId() {
        if (null == bufferId || null == textureId || 0 == textureId.length) {
            return -1;
        }
        return textureId[0];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
