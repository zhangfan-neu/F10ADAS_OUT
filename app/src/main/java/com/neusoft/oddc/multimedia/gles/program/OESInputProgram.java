package com.neusoft.oddc.multimedia.gles.program;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.neusoft.oddc.multimedia.gles.GlUtil;

import java.nio.FloatBuffer;

public class OESInputProgram extends GLProgram {
    private static final String TAG = OESInputProgram.class.getSimpleName();

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
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    private int programHandle;
    private int uMVPMatrixLoc;
    private int uTexMatrixLoc;
    private int aPositionLoc;
    private int aTextureCoordLoc;

    private int textureTarget;

    @Override
    public void init() {
        textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
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

        // Select the program.
        GLES20.glUseProgram(programHandle);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(textureTarget, textureId);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect vertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(aPositionLoc, coordsPerVertex, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(aTextureCoordLoc, 2, GLES20.GL_FLOAT, false, texStride, texBuffer);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
        GLES20.glBindTexture(textureTarget, 0);
        GLES20.glUseProgram(0);

        return 0;
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(programHandle);
        this.isInitialized = false;
        programHandle = -1;
    }
}
