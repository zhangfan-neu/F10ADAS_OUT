package com.neusoft.oddc.multimedia.gles.node;

import com.neusoft.oddc.multimedia.gles.GlUtil;
import com.neusoft.oddc.multimedia.gles.MeshConstants;
import com.neusoft.oddc.multimedia.gles.program.GLProgram;
import com.neusoft.oddc.multimedia.gles.program.GLProgramCommand;
import com.neusoft.oddc.multimedia.gles.program.SimpleProgram;

import java.nio.FloatBuffer;
import java.util.LinkedList;


public class ImageFilterFrameBufferNode implements GLFrameBuffer {

    protected LinkedList<Runnable> runOnDraw;

    private FloatBuffer vertexArray;
    private FloatBuffer texCoordArray;

    private int textureId = 0;
    private float[] mvpMatrix = GlUtil.createIdentityMatrix();
    private float[] texMatrix = GlUtil.createIdentityMatrix();
    private FrameBufferWrapper frameBuffer;
    private int outputWidth = DEFAULT_WIDTH;
    private int outputHeight = DEFAULT_HEIGHT;


    private GLProgram gpuImageFilter = null;

    protected boolean isInitialized = false;

    protected GLProgramCommand glProgramCommand;

    public ImageFilterFrameBufferNode() {
        runOnDraw = new LinkedList<Runnable>();
        gpuImageFilter = new SimpleProgram();
        frameBuffer = new FrameBufferWrapper();
    }

    public ImageFilterFrameBufferNode(GLProgram filter) {
        runOnDraw = new LinkedList<Runnable>();
        gpuImageFilter = filter;
        frameBuffer = new FrameBufferWrapper();
    }

    @Override
    public void init() {
        gpuImageFilter.init();
        frameBuffer.create(this.outputWidth, this.outputHeight, true);
        vertexArray = GlUtil.createFloatBuffer(MeshConstants.RECTANGLE_COORDS);
        texCoordArray = GlUtil.createFloatBuffer(MeshConstants.RECTANGLE_TEX_COORDS);
        isInitialized = true;
    }

    @Override
    public void release() {
        isInitialized = false;
        if (null != gpuImageFilter && gpuImageFilter.isInitialized()) {
            gpuImageFilter.release();
        }
        frameBuffer.release();
    }

    @Override
    public int draw() {
        runPendingOnDrawTasks();
        frameBuffer.bind(true);

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

        glProgramCommand.setMvpMatrix(mvpMatrix);

        glProgramCommand.setVertexBuffer(vertexArray);
        glProgramCommand.setTextures(new int[]{textureId});
        glProgramCommand.setTexMatrix(texMatrix);
        glProgramCommand.setTextureCoordBuffer(texCoordArray);

        gpuImageFilter.draw(glProgramCommand);
//        gpuImageFilter.onDraw(mvpMatrix, textureId, vertexArray, texCoordArray, texMatrix);
        frameBuffer.unbind();
        return frameBuffer.getTextureId();
    }

    @Override
    public GLNode getParent() {
        return null;
    }

    @Override
    public void setFullFrameSize(int width, int height) {

    }

    @Override
    public int getFullFrameWidth() {
        return 0;
    }

    @Override
    public int getFullFrameHeight() {
        return 0;
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void setRotation(float rotation) {

    }

    @Override
    public float getRotation() {
        return 0;
    }

    public void setFrameTexture(int textureId, boolean flip) {
        this.textureId = textureId;
    }

    public void switchFilter(final GLProgram filter) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (null != filter && null != gpuImageFilter) {
                    gpuImageFilter.release();
                    gpuImageFilter = filter;
                    gpuImageFilter.init();
                }
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.addLast(runnable);
        }
    }

    protected void runPendingOnDrawTasks() {
        while (!runOnDraw.isEmpty()) {
            runOnDraw.removeFirst().run();
        }
    }

    @Override
    public void setOutputSize(int width, int height) {
        this.outputHeight = height;
        this.outputWidth = width;
    }

    public int getTextureID() {
        return frameBuffer.getTextureId();
    }


}
