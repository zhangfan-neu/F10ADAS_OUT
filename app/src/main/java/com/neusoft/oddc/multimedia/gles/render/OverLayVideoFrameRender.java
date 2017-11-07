package com.neusoft.oddc.multimedia.gles.render;

import android.opengl.GLES20;
import android.util.Log;

import com.neusoft.oddc.multimedia.gles.GLThreadTaskList;
import com.neusoft.oddc.multimedia.gles.node.FrameBufferWrapper;
import com.neusoft.oddc.multimedia.gles.node.GLNode;
import com.neusoft.oddc.multimedia.gles.node.ImageFilterFrameBufferNode;
import com.neusoft.oddc.multimedia.gles.node.SimpleBlendingRectNode;
import com.neusoft.oddc.multimedia.gles.program.BlendingProgram;

import java.util.ArrayList;

public class OverLayVideoFrameRender extends VideoFrameRender {
    private static final String TAG = OverLayVideoFrameRender.class.getSimpleName();

    protected GLThreadTaskList pendingTasks = new GLThreadTaskList();
    protected ArrayList<GLNode> overlayList = new ArrayList<GLNode>();
    protected FrameBufferWrapper overlayComposeFrameBuffer;
    protected SimpleBlendingRectNode overlayOutputFrame;

    protected int overlayFrameWidth = 0;
    protected int overlayFrameHeight = 0;

    public OverLayVideoFrameRender() {
        this(null);
    }

    public OverLayVideoFrameRender(ImageFilterFrameBufferNode filterNode) {
        super(filterNode);
    }

    @Override
    public void init() {
        super.init();
        this.overlayFrameWidth = outputWidth;
        this.overlayFrameHeight = outputHeight;
        if (null != overlayComposeFrameBuffer) {
            overlayComposeFrameBuffer.release();
            overlayComposeFrameBuffer = null;
        }
        overlayComposeFrameBuffer = new FrameBufferWrapper(0f, 0f, 0f, 0f);

        overlayComposeFrameBuffer.create(overlayFrameWidth, overlayFrameHeight, false);

        if (null != overlayOutputFrame) {
            overlayOutputFrame.release();
            overlayOutputFrame = null;
        }
        overlayOutputFrame = new SimpleBlendingRectNode(new BlendingProgram());
        overlayOutputFrame.setBlendingFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        overlayOutputFrame.setFullFrameSize(this.overlayFrameWidth, this.overlayFrameHeight);
        overlayOutputFrame.init();
        overlayOutputFrame.setOrientationHint(orientationHint);
        overlayOutputFrame.setFrameTexture(overlayComposeFrameBuffer.getTextureId());

        pendingTasks.runPendingOnDrawTasks();

        for (GLNode node : overlayList) {
            if (!node.isInitialized()) {
                setupOverlay(node);
            }
        }

        Log.d(TAG, "overlay trace -- > init : isInitialized = " + this.isInitialized());
    }

    @Override
    public void release() {
        super.release();
        if (null != overlayComposeFrameBuffer) {
            try {
                overlayComposeFrameBuffer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != overlayOutputFrame) {
            try {
                overlayOutputFrame.release();
                overlayOutputFrame = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        pendingTasks.clear();
        for (GLNode node : overlayList) {
            if (null != node && node.isInitialized()) {
                node.release();
            }
        }
    }

    @Override
    public int renderOutput() {
        pendingTasks.runPendingOnDrawTasks();
        super.renderOutput();
        if (null != overlayOutputFrame) {
            overlayOutputFrame.setUseOrientationHint(false);
            overlayOutputFrame.draw();

        }
        return 0;
    }

    @Override
    public int renderOutputWithOrientationHint() {
        pendingTasks.runPendingOnDrawTasks();
        super.renderOutputWithOrientationHint();
        if (null != overlayOutputFrame) {
            boolean needUpdateViewport = false;
            int[] viewport = null;
            if (orientationHint % 180 != 0) {
                needUpdateViewport = true;
                viewport = new int[4];
                GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, viewport, 0);
                GLES20.glViewport(0, 0, outputHeight, outputWidth);
            }
            overlayOutputFrame.setUseOrientationHint(true);
            overlayOutputFrame.draw();
            if (needUpdateViewport) {
                GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            }
        }
        return 0;
    }

    @Override
    public int renderInput(int textureId, float[] texMatrix) {
        int ret = super.renderInput(textureId, texMatrix);
        if (null != overlayComposeFrameBuffer) {
            overlayComposeFrameBuffer.bind(true);
            for (GLNode node : overlayList) {
                if (null != node && node.isInitialized()) {
                    node.draw();
                }
            }
            overlayComposeFrameBuffer.unbind();
        }
        return ret;
    }

    @Override
    public void updateOutputSize(int width, int height) {
        super.updateOutputSize(width, height);
    }

    public void addOverlay(final GLNode node) {
        this.overlayList.add(node);
        postSetupOverlay(node);
    }

    protected void postSetupOverlay(final GLNode node) {
        pendingTasks.runOnDraw(new Runnable() {
            @Override
            public void run() {
                setupOverlay(node);
            }
        });
    }

    private void setupOverlay(final GLNode node) {
        Log.d(TAG, "overlay trace -- > setupOverlay : enter !!!");
        if (null == node) {
            return;
        }
        if (null != node && overlayFrameHeight > 0 && overlayFrameWidth > 0) {
            node.setFullFrameSize(overlayFrameWidth, overlayFrameHeight);
        }
        node.init();
    }

}
