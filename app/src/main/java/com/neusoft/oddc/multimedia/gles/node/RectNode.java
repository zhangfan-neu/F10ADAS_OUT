package com.neusoft.oddc.multimedia.gles.node;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.neusoft.oddc.multimedia.gles.GLThreadTaskList;
import com.neusoft.oddc.multimedia.gles.GlUtil;
import com.neusoft.oddc.multimedia.gles.MeshConstants;
import com.neusoft.oddc.multimedia.gles.cropper.BaseTextureCropper;
import com.neusoft.oddc.multimedia.gles.program.GLProgram;
import com.neusoft.oddc.multimedia.gles.program.GLProgramCommand;
import com.neusoft.oddc.multimedia.gles.program.SimpleProgram;

import java.nio.FloatBuffer;

public class RectNode implements GLNode {
    private static final String TAG = RectNode.class.getSimpleName();

    protected int left;
    protected int top;
    protected int right;
    protected int bottom;

    protected int fullFrameWidth;
    protected int fullFrameHeight;

//    protected float glLeft;
//    protected float glTop;
//    protected float glRight;
//    protected float glBottom;

    protected FloatBuffer vertexArray = GlUtil.createFloatBuffer(MeshConstants.RECTANGLE_COORDS);
    protected FloatBuffer texCoordArray = GlUtil.createFloatBuffer(MeshConstants.RECTANGLE_TEX_COORDS);

    protected BaseTextureCropper textureCropper = null;

    protected boolean useOrientationHint = false;

    protected int textureId = 0;
    protected float[] identityMatrix = GlUtil.createIdentityMatrix();
    protected float[] texMatrix = GlUtil.createIdentityMatrix();

    protected float[] mvpMatrix = GlUtil.createIdentityMatrix();
    protected float[] mvpWithOrientationHint = GlUtil.createIdentityMatrix();

    protected GLProgram glProgram = new SimpleProgram();

    protected Bitmap textureBitmap;

    protected GLThreadTaskList pendingTasks = new GLThreadTaskList();

    protected boolean isInitialized = false;

    protected boolean isBitmapTexture = false;

    protected int orientationHint = 0;

    protected float rotation = 0f;

    protected GLProgramCommand glProgramCommand;

    public RectNode() {
    }

    public RectNode(GLProgram program) {
        this.glProgram = program;
    }

    public RectNode(GLProgram program, BaseTextureCropper cropper) {
        this.glProgram = program;
        this.textureCropper = cropper;
    }

    public RectNode(int frameWidth, int frameHeight) {
        this.fullFrameWidth = frameWidth;
        this.fullFrameHeight = frameHeight;
    }

    @Override
    public void init() {

        if (null == this.glProgram) {
            new SimpleProgram();
        }
        glProgram.init();

        isInitialized = true;


    }

    @Override
    public void release() {
        isInitialized = false;
        if (isBitmapTexture) {
            if (GlUtil.NO_TEXTURE != textureId) {
                GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
            }

            textureId = GlUtil.NO_TEXTURE;
        }
        if (null != glProgram && glProgram.isInitialized()) {
            glProgram.release();
        }
    }

    public void setVertexArray(FloatBuffer vertexArray) {
        this.vertexArray = vertexArray;
    }

    public void layout(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        updateMVP();
    }

    @Override
    public int draw() {

        pendingTasks.runPendingOnDrawTasks();

        FloatBuffer texCorrds = null;

        if (null != textureCropper) {
            texCorrds = textureCropper.getCropTextureCoordBuffer();
        } else {
            texCorrds = texCoordArray;
        }

        float[] vertexMatrix = null;
        if (useOrientationHint) {
            vertexMatrix = mvpWithOrientationHint;
        } else {
            vertexMatrix = mvpMatrix;
        }

        if (null == glProgramCommand) {

            int vertexCount = MeshConstants.RECTANGLE_COORDS.length / MeshConstants.COORDS_COUNT_2;
            int vertextStride = MeshConstants.COORDS_COUNT_2 * MeshConstants.SIZEOF_FLOAT;

            glProgramCommand = new GLProgramCommand();

            glProgramCommand.setDrawType(GLProgramCommand.DrawType.DRAW_ARRAY);
            glProgramCommand.setFirstVertexIndex(0);
            glProgramCommand.setVertexStride(vertextStride);
            glProgramCommand.setTextureStride(vertextStride);
            glProgramCommand.setVertexCount(vertexCount);
            glProgramCommand.setCoordsPerVertex(MeshConstants.COORDS_COUNT_2);
        }

        glProgramCommand.setMvpMatrix(vertexMatrix);
        glProgramCommand.setVertexBuffer(vertexArray);
        glProgramCommand.setTextures(new int[]{textureId});
        glProgramCommand.setTexMatrix(texMatrix);
        glProgramCommand.setTextureCoordBuffer(texCorrds);

        glProgram.draw(glProgramCommand);

        return 0;
    }

    @Override
    public GLNode getParent() {
        return null;
    }

    public int getWidth() {
        return Math.abs(right - left);
    }

    public int getHeight() {
        return Math.abs(bottom - top);
    }

    @Override
    public void setFullFrameSize(int width, int height) {

        this.fullFrameWidth = width;
        this.fullFrameHeight = height;
        updateMVP();
    }

    @Override
    public int getFullFrameWidth() {
        return this.fullFrameWidth;
    }

    @Override
    public int getFullFrameHeight() {
        return this.fullFrameHeight;
    }

    public void setTextureBitmap(final Bitmap bitmap) {
        this.setTextureBitmap(bitmap, false, false);
    }

    public void setTextureBitmap(final Bitmap bitmap, final boolean flip) {
        this.setTextureBitmap(bitmap, flip, false);
    }

    public void setTextureBitmap(final Bitmap bitmap, final boolean flip, final boolean recycle) {

        pendingTasks.runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (!recycle) {
                    textureBitmap = bitmap;
                }
                textureId = GlUtil.loadTexture(bitmap, textureId, recycle);
                isBitmapTexture = true;
            }
        });
    }

    public void setFrameTexture(int textureId) {

        if (isBitmapTexture) {
            if (GlUtil.NO_TEXTURE != textureId) {
                GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
            }

            textureId = GlUtil.NO_TEXTURE;
        }

        this.textureId = textureId;
        isBitmapTexture = false;
    }

    public void setTextMatrix(float[] matrix) {
        this.texMatrix = matrix;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void setRotation(float rotation) {
        this.rotation = rotation;
        updateMVP();
    }

    @Override
    public float getRotation() {
        return this.rotation;
    }

    public void setOrientationHint(int orientation) {
        this.orientationHint = orientation;
        updateMVP();
    }

    public void setUseOrientationHint(boolean useOrientationHint) {
//		Log.d(TAG, "orientation trace -- > setUseOrientationHint : useOrientationHint = " + useOrientationHint);
        this.useOrientationHint = useOrientationHint;
    }

    protected void updateMVP() {
        updateStandardMVP();
        updateOrientationHintMVP();
    }

    private void updateStandardMVP() {
//        Log.d(TAG, "orientation trace -- > updateMVP : orientation = " + orientationHint + ", fullFrameWidth = " + fullFrameWidth + ", fullFrameHeight = " + fullFrameHeight
//                + ", top = " + top + ", left = " + left + ", right = " + right + ", bottom = " + bottom);
//        Log.d(TAG, "orientation trace -- > updateMVP : fullFrameWidth = " + fullFrameWidth + ", fullFrameHeight = " + fullFrameHeight);

        if (0 == fullFrameWidth || 0 == fullFrameHeight) {
            return;
        }

        float width = fullFrameWidth;
        float height = fullFrameHeight;

        float unit = Math.max(width, height) / 2f;

        float l;
        float r;
        float t;
        float b;
        float glLeft;
        float glTop;
        float glRight;
        float glBottom;

        float[] mMatrix = GlUtil.createIdentityMatrix();
        if (0 == left && 0 == right && 0 == top && 0 == bottom) {
            l = 0;
            t = 0;
            r = width;
            b = height;
        } else {
            l = left;
            t = top;
            r = right;
            b = bottom;
        }
        glLeft = (l - width / 2f) / unit;
        glRight = (r - width / 2f) / unit;
        glTop = (height / 2f - t) / unit;
        glBottom = (height / 2f - b) / unit;

        float centerX = (glLeft + glRight) / 2f;
        float centerY = (glTop + glBottom) / 2f;
        float scaleX = (glRight - glLeft) / 2f;
        float scaleY = (glTop - glBottom) / 2f;

        Matrix.translateM(mMatrix, 0, centerX, centerY, 0f);


        float[] rMatrix = GlUtil.createIdentityMatrix();
        float[] tempMatrix = GlUtil.createIdentityMatrix();

        Matrix.rotateM(rMatrix, 0, rotation, 0, 0, 1);
        Matrix.multiplyMM(tempMatrix, 0, mMatrix, 0, rMatrix, 0);
        System.arraycopy(tempMatrix, 0, mMatrix, 0, 16);

        Matrix.scaleM(mMatrix, 0, scaleX, scaleY, 1f);


        float[] vMatrix = GlUtil.createIdentityMatrix();
        Matrix.setLookAtM(vMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, 1.0f, 0.0f);

        float[] pMatrix = GlUtil.createIdentityMatrix();
        Matrix.orthoM(pMatrix, 0, -0.5f * width / unit, 0.5f * width / unit, -0.5f * height / unit, 0.5f * height / unit, -1, 10);


        float[] mvMatrix = GlUtil.createIdentityMatrix();
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0);

//        Log.d(TAG, "orientation trace -- > updateMVP : mvpMatrix :");
//        for (float v : mvpMatrix) {
//            Log.d(TAG, "orientation trace -- > updateMVP : mvpMatrix = " + v);
//        }
    }

    private void updateOrientationHintMVP() {
//        Log.d(TAG, "orientation trace -- > updateMVP : orientation = " + orientationHint + ", fullFrameWidth = " + fullFrameWidth + ", fullFrameHeight = " + fullFrameHeight
//                + ", top = " + top + ", left = " + left + ", right = " + right + ", bottom = " + bottom);
//        Log.d(TAG, "orientation trace -- > updateMVP : fullFrameWidth = " + fullFrameWidth + ", fullFrameHeight = " + fullFrameHeight);

        if (0 == fullFrameWidth || 0 == fullFrameHeight) {
            return;
        }

        float width = fullFrameWidth;
        float height = fullFrameHeight;

        float unit = Math.max(width, height) / 2f;

        float l;
        float r;
        float t;
        float b;
        float glLeft;
        float glTop;
        float glRight;
        float glBottom;

        float[] mMatrix = GlUtil.createIdentityMatrix();
        if (0 == left && 0 == right && 0 == top && 0 == bottom) {
            l = 0;
            t = 0;
            r = width;
            b = height;
        } else {
            l = left;
            t = top;
            r = right;
            b = bottom;
        }
        glLeft = (l - width / 2f) / unit;
        glRight = (r - width / 2f) / unit;
        glTop = (height / 2f - t) / unit;
        glBottom = (height / 2f - b) / unit;

        float centerX = (glLeft + glRight) / 2f;
        float centerY = (glTop + glBottom) / 2f;
        float scaleX = (glRight - glLeft) / 2f;
        float scaleY = (glTop - glBottom) / 2f;

        Matrix.translateM(mMatrix, 0, centerX, centerY, 0f);


        float[] rMatrix = GlUtil.createIdentityMatrix();
        float[] tempMatrix = GlUtil.createIdentityMatrix();

        Matrix.rotateM(rMatrix, 0, rotation, 0, 0, 1);
        Matrix.multiplyMM(tempMatrix, 0, mMatrix, 0, rMatrix, 0);
        System.arraycopy(tempMatrix, 0, mMatrix, 0, 16);

        Matrix.scaleM(mMatrix, 0, scaleX, scaleY, 1f);

        float[] vMatrix = GlUtil.createIdentityMatrix();
        float[] vOHintMatrix = GlUtil.createIdentityMatrix();
        Matrix.setLookAtM(vMatrix, 0, 0, 0, 3f, 0, 0, 0f, 0f, 1.0f, 0.0f);
        Matrix.rotateM(vOHintMatrix, 0, vMatrix, 0, -orientationHint, 0, 0, 1);

        float[] pMatrix = GlUtil.createIdentityMatrix();

        if (orientationHint % 180 == 0) {
            Matrix.orthoM(pMatrix, 0, -0.5f * width / unit, 0.5f * width / unit, -0.5f * height / unit, 0.5f * height / unit, -1, 10);
        } else {
            Matrix.orthoM(pMatrix, 0, -0.5f * height / unit, 0.5f * height / unit, -0.5f * width / unit, 0.5f * width / unit, -1, 10);
        }

        float[] mvMatrix = GlUtil.createIdentityMatrix();
        Matrix.multiplyMM(mvMatrix, 0, vOHintMatrix, 0, mMatrix, 0);
        Matrix.multiplyMM(mvpWithOrientationHint, 0, pMatrix, 0, mvMatrix, 0);

//        Log.d(TAG, "orientation trace -- > updateMVP : mvpWithOrientationHint :");
//        for (float v : mvpWithOrientationHint) {
//            Log.d(TAG, "orientation trace -- > updateMVP : mvpWithOrientationHint = " + v);
//        }
    }
}
