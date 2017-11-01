package com.neusoft.oddc.multimedia.gles.program;

import android.opengl.GLES20;

import com.neusoft.oddc.multimedia.gles.GlUtil;

import java.nio.FloatBuffer;

public class BlendingProgram extends GLProgram implements GLBlendable {
    private static final String TAG = BlendingProgram.class.getSimpleName();


    private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTexMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
            "}\n";

    private static final String FRAGMENT_SHADER_EXT = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";


    private int programHandle;
    private int uMVPMatrixLoc;
    private int uTexMatrixLoc;
    private int aPositionLoc;
    private int aTextureCoordLoc;

    private int blendingSrcFactor = GLES20.GL_SRC_ALPHA; // GL_SRC_ALPHA_SATURATE // GL_SRC_ALPHA(default)
    private int blendingDestFactor = GLES20.GL_ONE;


    @Override
    public void init() {
        programHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT);
        if (programHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }

        aPositionLoc = GLES20.glGetAttribLocation(programHandle, "aPosition");
        GlUtil.checkLocation(aPositionLoc, "aPosition");
        aTextureCoordLoc = GLES20.glGetAttribLocation(programHandle, "aTextureCoord");
        GlUtil.checkLocation(aTextureCoordLoc, "aTextureCoord");
        uMVPMatrixLoc = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        GlUtil.checkLocation(uMVPMatrixLoc, "uMVPMatrix");
        uTexMatrixLoc = GLES20.glGetUniformLocation(programHandle, "uTexMatrix");
        GlUtil.checkLocation(uTexMatrixLoc, "uTexMatrix");
        this.isInitialized = true;
    }

    @Override
    public int draw(GLProgramCommand command) {
        return draw(command.getMvpMatrix(),
                command.getVertexBuffer(),
                command.getFirstVertexIndex(),
                command.getVertexCount(),
                command.getCoordsPerVertex(),
                command.getVertexStride(),
                command.getTexMatrix(),
                command.getTextureCoordBuffer(),
                command.getTextures()[0],
                command.getTextureStride());
    }

    protected int draw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex, int vertexCount, int coordsPerVertex, int vertexStride, float[] texMatrix, FloatBuffer texBuffer, int textureId, int texStride) {
        GlUtil.checkGlError("draw start");
        GLES20.glUseProgram(programHandle);
        GlUtil.checkGlError("glUseProgram");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");
        GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        GLES20.glVertexAttribPointer(aPositionLoc, coordsPerVertex, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");
        GLES20.glVertexAttribPointer(aTextureCoordLoc, 2, GLES20.GL_FLOAT, false, texStride, texBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(blendingSrcFactor, blendingDestFactor);
        // GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtil.checkGlError("glDrawArrays");
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);

        return 0;
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(programHandle);
        this.isInitialized = false;
        programHandle = -1;
    }

    public void setBlendingFunction(int src, int dest) {
        this.blendingSrcFactor = src;
        this.blendingDestFactor = dest;
    }
}
